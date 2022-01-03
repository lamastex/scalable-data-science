# Databricks notebook source
# MAGIC %md
# MAGIC # Implementation of [PSPNet](https://arxiv.org/pdf/1612.01105.pdf) with Hyperparameter Tuning in Parallel
# MAGIC 
# MAGIC William Anzén ([Linkedin](https://www.linkedin.com/in/william-anz%C3%A9n-b52003199/)), Christian von Koch ([Linkedin](https://www.linkedin.com/in/christianvonkoch/))
# MAGIC 
# MAGIC 2021, Stockholm, Sweden
# MAGIC 
# MAGIC This project was supported by Combient Mix AB under the industrial supervision of Razesh Sainudiin and Max Fischer.
# MAGIC 
# MAGIC In this notebook, an implementation of [PSPNet](https://arxiv.org/pdf/1612.01105.pdf) is presented which is an architecture which uses scene parsing and evaluates the images at different scales and finally combines the different results to form a final prediction. The architecture is evaluated against the [Oxford-IIIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/) whilst some hyperparameters of the model are tunead parallely through Spark Trials.

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
from hyperopt import fmin, tpe, hp, Trials, STATUS_OK, SparkTrials

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
# MAGIC The [Oxford-IIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/) from the TensorFlow datasets is loaded and then the images are transformed in desired way and the datasets used for training and inference are created.

# COMMAND ----------

dataset, info = tfds.load('oxford_iiit_pet:3.*.*', with_info=True)

TRAIN_LENGTH = info.splits['train'].num_examples
TEST_LENGTH = info.splits['test'].num_examples
BATCH_SIZE = 64
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH // BATCH_SIZE

train = dataset['train'].map(load_image_train)
test = dataset['test'].map(load_image_test)


# COMMAND ----------

# MAGIC %md
# MAGIC The dataset is then converted to numpy format since Spark Trials does not yet handle the TensorFlow Dataset class.

# COMMAND ----------

train_numpy = tfds.as_numpy(train)
test_numpy = tfds.as_numpy(test)

# COMMAND ----------

X_train = np.array(list(map(lambda x: x[0], train_numpy)))
Y_train = np.array(list(map(lambda x: x[1], train_numpy)))

X_test = np.array(list(map(lambda x: x[0], test_numpy)))
Y_test = np.array(list(map(lambda x: x[1], test_numpy)))

# COMMAND ----------

# MAGIC %md
# MAGIC An example image is displayed. 

# COMMAND ----------

def display(display_list):
  plt.figure(figsize=(15, 15))

  title = ['Input Image', 'True Mask', 'Predicted Mask']

  for i in range(len(display_list)):
    plt.subplot(1, len(display_list), i+1)
    plt.title(title[i])
    plt.imshow(tf.keras.preprocessing.image.array_to_img(display_list[i]))
    plt.axis('off')
  plt.show()

it = iter(train)
#for image, mask in next(train):
sample_image, sample_mask = next(it)
display([sample_image, sample_mask])

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
# MAGIC Defining the custom data generators used for training the model.

# COMMAND ----------

def batch_generator(batch_size):
  indices = np.arange(len(X_train)) 
  batch=[]
  while True:
  # it might be a good idea to shuffle your data before each epoch
    np.random.shuffle(indices) 
    for i in indices:
      batch.append(i)
      if len(batch)==batch_size:
        yield X_train[batch], Y_train[batch]
        batch=[]
        
def batch_generator_eval(batch_size):
  indices = np.arange(len(X_test)) 
  batch=[]
  while True:
    for i in indices:
      batch.append(i)
      if len(batch)==batch_size:
        yield X_test[batch], Y_test[batch]
        batch=[]

# COMMAND ----------

# MAGIC %md
# MAGIC To exploit parallelism, Spark Trials require that you define a function to be used for loading the dataset as well as training and evaluating the model.

# COMMAND ----------

def train_spark(params):
  
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
  VAL_SUBSPLITS=5
  EPOCHS=50
  VALIDATION_STEPS = TEST_LENGTH//params['batch_size']//VAL_SUBSPLITS
  STEPS_PER_EPOCH = TRAIN_LENGTH // params['batch_size']
  BATCH_SIZE = params['batch_size']
  print(BATCH_SIZE)
  BUFFER_SIZE = 1000
  """
  An example train method that calls into HorovodRunner.
  This method is passed to hyperopt.fmin().
  
  :param params: hyperparameters. Its structure is consistent with how search space is defined. See below.
  :return: dict with fields 'loss' (scalar loss) and 'status' (success/failure status of run)
  """
  train_dataset = batch_generator(BATCH_SIZE)
  test_dataset = batch_generator_eval(BATCH_SIZE)
  model=PSPNet(128,128,3)
  model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=params['learning_rate']),
              loss=tf.keras.losses.SparseCategoricalCrossentropy(),
              metrics=tf.keras.metrics.SparseCategoricalAccuracy())
  
  model_history =  model.fit(train_dataset, epochs=EPOCHS, steps_per_epoch=STEPS_PER_EPOCH)
  loss = model.evaluate(test_dataset, steps=VALIDATION_STEPS)[0]
  return {'loss': loss, 'status': STATUS_OK}

# COMMAND ----------

# MAGIC %md
# MAGIC In this experiment, the hyperparameters `learning_rate` and `batch_size` were explored through random search in combination with an adaptive algorithm called Tree of Parzen Estimators (TPE). Since parallelism were set to 4 the choice of hyperparameters will be set randomly for the first 4 runs in parallel and then adaptively for the second pass of 4 runs.

# COMMAND ----------

space = {
  'learning_rate': hp.loguniform('learning_rate', np.log(1e-4), np.log(1e-1)),
  'batch_size': hp.choice('batch_size', [16, 32, 64]),
}

# COMMAND ----------

algo=tpe.suggest

spark_trials = SparkTrials(parallelism=4)
best_param = fmin(
  fn=train_spark,
  space=space,
  algo=algo,
  max_evals=8,
  return_argmin=False,
  trials = spark_trials,
)

print(best_param)

# COMMAND ----------

