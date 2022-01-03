# Databricks notebook source
# MAGIC %md
# MAGIC # Implementation of [PSPNet](https://arxiv.org/pdf/1612.01105.pdf) with distributed training using Horovod
# MAGIC 
# MAGIC William Anzén ([Linkedin](https://www.linkedin.com/in/william-anz%C3%A9n-b52003199/)), Christian von Koch ([Linkedin](https://www.linkedin.com/in/christianvonkoch/))
# MAGIC 
# MAGIC 2021, Stockholm, Sweden
# MAGIC 
# MAGIC This project was supported by Combient Mix AB under the industrial supervision of Razesh Sainudiin and Max Fischer.
# MAGIC 
# MAGIC In this notebook, an implementation of [PSPNet](https://arxiv.org/pdf/1612.01105.pdf) is presented which is an architecture which uses scene parsing and evaluates the images at different scales and finally combines the different results to form a final prediction. The architecture is evaluated against the [Oxford-IIIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/) and is trained in a distributed manner using Horovod.

# COMMAND ----------

# MAGIC %md
# MAGIC Importing the required packages.

# COMMAND ----------

import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from tensorflow.keras.layers import AveragePooling2D, Conv2D, BatchNormalization, Activation, Concatenate, UpSampling2D, Reshape, TimeDistributed, ConvLSTM2D
from tensorflow.keras import Model
import numpy as np
from tensorflow.keras.applications.resnet50 import ResNet50
import horovod.tensorflow.keras as hvd


# COMMAND ----------

# MAGIC %md
# MAGIC Setting memory growth to the GPUs is recommended as the model is quite memory intensive.

# COMMAND ----------

gpus = tf.config.list_physical_devices('GPU')
if gpus:
  try:
    # Currently, memory growt*h needs to be the same across GPUs
    for gpu in gpus:
      tf.config.experimental.set_memory_growth(gpu, True)
    logical_gpus = tf.config.experimental.list_logical_devices('GPU')
    print(len(gpus), "Physical GPUs,", len(logical_gpus), "Logical GPUs")
  except RuntimeError as e:
    # Memory growth must be set before GPUs have been initialized
    print(e)

# COMMAND ----------

# MAGIC %md
# MAGIC Setting up checkpoint location... The next cell creates a directory for saved checkpoint models.

# COMMAND ----------

import os
import time

checkpoint_dir = '/dbfs/ml/OxfordDemo_Horovod/train/'

os.makedirs(checkpoint_dir)

# COMMAND ----------

# MAGIC %md
# MAGIC Defining functions for normalizing and transforming the images.

# COMMAND ----------

# Function for normalizing image_size so that pixel intensity is between 0 and 1
def normalize(input_image, input_mask):
  input_image = tf.cast(input_image, tf.float32) / 255.0
  input_mask -= 1
  return input_image, input_mask

# Function for resizing the train images to the desired input shape of 128x128 as well as augmenting the training images
def load_image_train(datapoint):
  input_image = tf.image.resize(datapoint['image'], (128, 128))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (128, 128))

  if tf.random.uniform(()) > 0.5:
    input_image = tf.image.flip_left_right(input_image)
    input_mask = tf.image.flip_left_right(input_mask)

  input_image, input_mask = normalize(input_image, input_mask)
  input_mask = tf.math.round(input_mask)

  return input_image, input_mask


# Function for resizing the test images to the desired output shape (no augmenation)
def load_image_test(datapoint):
  input_image = tf.image.resize(datapoint['image'], (128, 128))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (128, 128))

  input_image, input_mask = normalize(input_image, input_mask)

  return input_image, input_mask

# COMMAND ----------

# MAGIC %md
# MAGIC Defining the functions needed for the PSPNet.

# COMMAND ----------

def pool_block(cur_tensor: tf.Tensor,
               image_width: int,
               image_height: int,
               pooling_factor: int,
               activation: str = 'relu'
              ) -> tf.Tensor:
  """
  Parameters
  ----------
  cur_tensor : tf.Tensor
      Incoming tensor.

  image_width : int
      Width of the image.

  image_height : int
      Height of the image.

  pooling_factor : int
      Pooling factor to scale image.

  activation : str, default = 'relu'
      Activation function to be applied after the pooling operations.

  Returns
  -------
  tf.Tensor
      2D convolutional block.
  """
  
  #Calculate the strides with image size and pooling factor
  strides = [int(np.round(float(image_width)/pooling_factor)),
            int(np.round(float(image_height)/pooling_factor))]
  
  pooling_size = strides
  
  x = AveragePooling2D(pooling_size, strides=strides, padding='same')(cur_tensor)
  
  x = Conv2D(filters = 512, 
             kernel_size = (1,1),
             padding = 'same')(x)
  
  x = BatchNormalization()(x)
  x = Activation(activation)(x)
  
  # Resizing images to correct shape for future concat
  x = tf.keras.layers.experimental.preprocessing.Resizing(
    image_height, 
    image_width, 
    interpolation="bilinear")(x) 
  
  return x

def modify_ResNet_Dilation(
  model: tf.keras.Model
) -> tf.keras.Model:
  """
  Modifies the ResNet to fit the PSPNet Paper with the described dilated strategy.
  
    Parameters
    ----------
    model : tf.keras.Model
        The ResNet-50 model to be modified.
        
    Returns
    -------
    tf.keras.Model
        Modified model.
  """
  for i in range(0,4):
    model.get_layer('conv4_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv4_block1_{}_conv'.format(i)).dilation_rate = 2
    model.get_layer('conv5_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv5_block1_{}_conv'.format(i)).dilation_rate = 4
  model.save('/tmp/my_model')
  new_model = tf.keras.models.load_model('/tmp/my_model')
  return new_model

def PSPNet(image_width: int,
           image_height: int,
           n_classes: int,
           kernel_size: tuple = (3,3),
           activation: str = 'relu',
           weights: str = 'imagenet',
           shallow_tuning: bool = False,
           isICNet: bool = False
          ) -> tf.keras.Model:
  """
  Setting up the PSPNet model
  
    Parameters
    ----------
    image_width : int
        Width of the image.
        
    image_height : int
        Height of the image.
        
    n_classes : int
        Number of classes.
        
    kernel_size : tuple, default = (3, 3)
        Size of the kernel.    
        
    activation : str, default = 'relu'
        Activation function to be applied after the pooling operations.
        
    weights: str, default = 'imagenet'
        String defining which weights to use for the backbone, 'imagenet' for ImageNet weights or None for normalized initialization.
        
    shallow_tuning : bool, default = True
        Boolean for using shallow tuning with pre-trained weights from ImageNet or not.
        
    isICNet : bool,  default = False   
        Boolean to determine if the PSPNet will be part of an ICNet.
        
    Returns
    -------
    tf.keras.Model
        The finished keras model for PSPNet.
  """
  if shallow_tuning and not weights:
    raise ValueError("Shallow tuning can not be performed without loading pre-trained weights. Please input 'imagenet' to argument weights...")
  #If the function is used for the ICNet input_shape is set to none as ICNet takes 3 inputs
  if isICNet:
    input_shape=(None, None, 3)
  else:
    input_shape=(image_height,image_width,3)
    
  #Initializing the ResNet50 Backbone  
  y=ResNet50(include_top=False, weights=weights, input_shape=input_shape)

  y=modify_ResNet_Dilation(y)
  if shallow_tuning:
    y.trainable=False
  
  pooling_layer=[]
  output=y.output

  pooling_layer.append(output)
  
  h = image_height//8
  w = image_width//8
  
  #Loop for calling the pool block functions for pooling factors [1,2,3,6]
  for i in [1,2,3,6]:
    pool = pool_block(output, h, w, i, activation)
    pooling_layer.append(pool)
    
  x=Concatenate()(pooling_layer)
  
  x=Conv2D(filters=n_classes, kernel_size=(1,1), padding='same')(x)
  
  x=UpSampling2D(size=(8,8), data_format='channels_last', interpolation='bilinear')(x)
  x=Reshape((image_height*image_width, n_classes))(x)
  x=Activation(tf.nn.softmax)(x)
  x=Reshape((image_height,image_width,n_classes))(x)

  final_model=tf.keras.Model(inputs=y.input, outputs=x)
                              
  return final_model

# COMMAND ----------

# MAGIC %md
# MAGIC A function called by the horovod runner creating the datasets for each worker. The dataset is split according to the amount of GPU's initialized in the horovod runner and distributed on each worker. The dataset is transformed to numpy.ndarray to enable the splitting, then transformed back into a tensorflow.dataset object and batched for training purposes.

# COMMAND ----------

def create_datasets_hvd_loop(BATCH_SIZE:int = 64, BUFFER_SIZE:int = 1000, rank=0, size=1):
  dataset, info = tfds.load('oxford_iiit_pet:3.*.*', data_dir='Oxford-%d' % rank, with_info=True)
  TRAIN_LENGTH = info.splits['train'].num_examples
  TEST_LENGTH = info.splits['test'].num_examples
  
  #Creating the ndarray in the correct shapes for training data
  train_original_img = np.ndarray(shape=(TRAIN_LENGTH, 128, 128, 3))
  train_original_mask = np.ndarray(shape=(TRAIN_LENGTH, 128, 128, 1))

  #Loading the data into the arrays 
  count = 0
  for datapoint in dataset['train']:
    img_orig, mask_orig = load_image_train(datapoint)
    train_original_img[count]=img_orig
    train_original_mask[count]=mask_orig

    count+=1
  
  #Creating the ndarrays in the correct shapes for test data  
  test_original_img = np.ndarray(shape=(TEST_LENGTH,128,128,3))
  test_original_mask = np.ndarray(shape=(TEST_LENGTH,128,128,1))
  
  #Loading the data into the arrays
  count=0
  for datapoint in dataset['test']:
    img_orig, mask_orig = load_image_test(datapoint)
    test_original_img[count]=img_orig
    test_original_mask[count]=mask_orig
  
    count+=1
    
  train_dataset = tf.data.Dataset.from_tensor_slices((train_original_img[rank::size], train_original_mask[rank::size]))
  orig_test_dataset = tf.data.Dataset.from_tensor_slices((test_original_img[rank::size], test_original_mask[rank::size]))
  
  train_dataset = train_dataset.shuffle(BUFFER_SIZE).cache().batch(BATCH_SIZE).repeat()
  train_dataset.prefetch(buffer_size=tf.data.experimental.AUTOTUNE)
  test_dataset = orig_test_dataset.batch(BATCH_SIZE)
  
  n_train = train_original_img[rank::size].shape[0]
  n_test = test_original_img[rank::size].shape[0]
  print(n_train)
  print(n_test)
  
  return train_dataset, test_dataset, n_train, n_test

# COMMAND ----------

# MAGIC %md
# MAGIC The training function run by each worker in a distributed horovod manner.

# COMMAND ----------

 def train_hvd(learning_rate):
  import tensorflow as tf
  import tensorflow_datasets as tfds
  
  # Initialize Horovod
  hvd.init()
  
  
  # Optimal batch size from previous notebooks hyperparameter search
  BATCH_SIZE = 16
  BUFFER_SIZE = 1000
  EPOCHS = 50

  # Pin GPU to be used to process local rank (one GPU per process)
  # These steps are skipped on a CPU cluster
  gpus = tf.config.experimental.list_physical_devices('GPU')
  for gpu in gpus:
    tf.config.experimental.set_memory_growth(gpu, True)
  if gpus:
    tf.config.experimental.set_visible_devices(gpus[hvd.local_rank()], 'GPU')
  
  train_dataset, test_dataset, n_train, n_test = create_datasets_hvd_loop(BATCH_SIZE = BATCH_SIZE, rank = hvd.rank(), size = hvd.size())
  
  STEPS_PER_EPOCH = n_train // BATCH_SIZE
  VALIDATION_STEPS = n_test//BATCH_SIZE
  
  model = PSPNet(128,128,3)
  
  # Adjust learning rate based on number of GPUs 
  optimizer = tf.keras.optimizers.Adam(learning_rate=learning_rate*hvd.size())
  # Use the Horovod Distributed Optimizer
  optimizer = hvd.DistributedOptimizer(optimizer)
  
  model.compile(optimizer=optimizer,
              loss=tf.keras.losses.SparseCategoricalCrossentropy(),
              metrics=tf.keras.metrics.SparseCategoricalAccuracy())
  
  #Create a callback to broadcast the initial variable states from rank 0 to all other processes.
  # This is required to ensure consistent initialization of all workers when training is started with random weights or restored from a checkpoint.
  callbacks = [
      hvd.callbacks.BroadcastGlobalVariablesCallback(0)
  ]
  
  if hvd.rank() == 0:
      callbacks.append(tf.keras.callbacks.ModelCheckpoint(checkpoint_dir + '/checkpoint-{epoch}.ckpt', save_weights_only = True, monitor='val_loss', save_best_only=True))
  
  #train_dataset = batch_generator(batch_size, X_train, Y_train)
  #test_dataset = batch_generator_eval(batch_size, X_test, Y_test)
  
  model_history =  model.fit(train_dataset, epochs=EPOCHS, steps_per_epoch=STEPS_PER_EPOCH, verbose = 1 if hvd.rank() == 0  else 0,
                           validation_steps=VALIDATION_STEPS, validation_data=test_dataset, callbacks=callbacks)

# COMMAND ----------

# MAGIC %md
# MAGIC Initialization of the horovod runner. Make sure to set np = "Amount of workers" that are available for your cluster.

# COMMAND ----------

from sparkdl import HorovodRunner

hr = HorovodRunner(np=4, driver_log_verbosity = "all")
# Optimal learning rate from previous notebooks hyperparameter search
hr.run(train_hvd, learning_rate=0.0001437661898681224)

# COMMAND ----------

# MAGIC %md
# MAGIC Reloading the test dataset to perform inference...

# COMMAND ----------

dataset, info = tfds.load('oxford_iiit_pet:3.*.*', with_info=True)

BATCH_SIZE = 16
test = dataset['test'].map(load_image_test)
test_dataset = test.batch(BATCH_SIZE)

TEST_LENGTH = info.splits['test'].num_examples
VALIDATION_STEPS = TEST_LENGTH//BATCH_SIZE

# COMMAND ----------

# MAGIC %md
# MAGIC Checking the latest checkpoint saved in the previous training.

# COMMAND ----------

# MAGIC %sh
# MAGIC ls /dbfs/ml/OxfordDemo_Horovod/train/

# COMMAND ----------

# MAGIC %md
# MAGIC Finally we evaluate the best performing model on the `test_dataset` and note that the model has quite successfully learned to segment the dataset.

# COMMAND ----------

model = PSPNet(128, 128, 3)
model_path = checkpoint_dir + 'checkpoint-49.ckpt'
model.load_weights(model_path)
model.compile(loss = tf.keras.losses.SparseCategoricalCrossentropy(), metrics='acc')
model.evaluate(test_dataset, steps = VALIDATION_STEPS)

# COMMAND ----------

