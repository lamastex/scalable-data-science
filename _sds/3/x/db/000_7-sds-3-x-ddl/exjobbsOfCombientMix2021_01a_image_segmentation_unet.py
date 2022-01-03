# Databricks notebook source
# MAGIC %md
# MAGIC # U-Net model for image segmentation
# MAGIC 
# MAGIC William Anzén ([Linkedin](https://www.linkedin.com/in/william-anz%C3%A9n-b52003199/)), Christian von Koch ([Linkedin](https://www.linkedin.com/in/christianvonkoch/))
# MAGIC 
# MAGIC 2021, Stockholm, Sweden
# MAGIC 
# MAGIC This project was supported by Combient Mix AB under the industrial supervision of Razesh Sainudiin and Max Fischer.
# MAGIC 
# MAGIC This is a modified version of Tensorflows tutorial regarding image segmentation which can be found [here](https://www.tensorflow.org/tutorials/images/segmentation). In this notebook a modified [U-Net](https://arxiv.org/abs/1505.04597) was used.

# COMMAND ----------

# MAGIC %md
# MAGIC Importing the required packages...

# COMMAND ----------

import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from IPython.display import clear_output
from tensorflow.keras import Input
from tensorflow.keras.layers import Conv2D, BatchNormalization, Activation, MaxPooling2D, Concatenate, ReLU, Reshape, Conv2DTranspose
from tensorflow.keras import Model
from tensorflow.keras.applications import VGG16
from typing import Union, Tuple

# COMMAND ----------

# MAGIC %md
# MAGIC First, the [Oxford-IIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/) from the TensorFlow datasets is loaded and then the images are transformed in desired way and the datasets used for training and inference are created. Finally an example image is displayed. 

# COMMAND ----------

dataset, info = tfds.load('oxford_iiit_pet:3.*.*', with_info=True)

def normalize(input_image, input_mask):
  input_image = tf.cast(input_image, tf.float32) / 255.0
  input_mask -= 1
  return input_image, input_mask

@tf.function
def load_image_train(datapoint):
  input_image = tf.image.resize(datapoint['image'], (128, 128))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (128, 128))

  if tf.random.uniform(()) > 0.5:
    input_image = tf.image.flip_left_right(input_image)
    input_mask = tf.image.flip_left_right(input_mask)

  input_image, input_mask = normalize(input_image, input_mask)

  return input_image, input_mask

def load_image_test(datapoint):
  input_image = tf.image.resize(datapoint['image'], (128, 128))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (128, 128))

  input_image, input_mask = normalize(input_image, input_mask)

  return input_image, input_mask

TRAIN_LENGTH = info.splits['train'].num_examples
BATCH_SIZE = 64
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH // BATCH_SIZE

train = dataset['train'].map(load_image_train, num_parallel_calls=tf.data.experimental.AUTOTUNE)
test = dataset['test'].map(load_image_test)

train_dataset = train.cache().shuffle(BUFFER_SIZE).batch(BATCH_SIZE).repeat()
train_dataset = train_dataset.prefetch(buffer_size=tf.data.experimental.AUTOTUNE)
test_dataset = test.batch(BATCH_SIZE)

def display(display_list):
  plt.figure(figsize=(15, 15))

  title = ['Input Image', 'True Mask', 'Predicted Mask']

  for i in range(len(display_list)):
    plt.subplot(1, len(display_list), i+1)
    plt.title(title[i])
    plt.imshow(tf.keras.preprocessing.image.array_to_img(display_list[i]))
    plt.axis('off')
  plt.show()

for image, mask in train.take(1):
  sample_image, sample_mask = image, mask
display([sample_image, sample_mask])



# COMMAND ----------

# MAGIC %md
# MAGIC Now that the dataset has been loaded into memory, the functions needed for the modified U-Net model are defined. Note that the alternative of both using a VGG16 encoder and a regular one is provided.

# COMMAND ----------

def conv_block(input_layer: tf.Tensor,
               filter_size: int, 
               kernel_size: tuple, 
               activation: str = 'relu'
              ) -> tf.Tensor:
  """
  Creates one concolutional block of the U-Net structure.
  
  Parameters
  ----------
  input_layer : tf.Tensor
      Input tensor.

  n_filters : int
      Number of filters/kernels.

   kernel_size : tuple
      Size of the kernel.

  activation : str, default='relu'
      Activation function to be applied after the pooling operations.

  Returns
  -------
  tf.Tensor
      2D convolutional block.
  """
  x = Conv2D(filters=filter_size, kernel_size=kernel_size, padding='same')(input_layer)
  x = BatchNormalization()(x)
  x = Activation(activation)(x)
  x = Conv2D(filters=filter_size, kernel_size=kernel_size,padding='same')(x)
  x = BatchNormalization()(x)
  x = Activation(activation)(x)
  return x

def encoder_VGG16(input_shape: list
                 ) -> Tuple[tf.keras.Model, list]:
  """
  Creates the encoder as a VGG16 network.
  
  Parameters
  ----------
  input_shape : list
      Input shape to initialize model.

  Returns
  -------
  tf.keras.Model
      Model instance from tf.keras.

  list
      List containing the layers to be concatenated in the upsampling phase.
  """
  base_model=VGG16(include_top=False, weights='imagenet', input_shape=input_shape)
  layers=[layer.output for layer in base_model.layers]
  base_model = tf.keras.Model(inputs=base_model.input, outputs=layers[-2])
  base_model.summary()
  
  x = []
  y = base_model.get_layer('block1_conv1').output
  x.append(y)
  y = base_model.get_layer('block2_conv2').output
  x.append(y)
  y = base_model.get_layer('block3_conv3').output
  x.append(y)
  y = base_model.get_layer('block4_conv3').output
  x.append(y)
  y =  base_model.get_layer('block5_conv3').output
  x.append(y)
  return base_model, x

def encoder_unet(input_shape: list
                ) -> Tuple[tf.keras.Input, list]:
  """
  Creates the encoder as the one described in the U-Net paper with slight modifications.
  
  Parameters
  ----------
  input_shape : tf.Tensor
      Shape of the inputted image to the model.

  Returns
  -------
  tf.keras.Input
      Input layer for the inputted image.

  list
      List containing the layers to be concatenated in the upsampling phase.
  """
  input_layer = tf.keras.Input(shape=input_shape)
  
  conv1 = conv_block(input_layer,4,3,'relu')
  pool1 = MaxPooling2D((2,2))(conv1)
  
  conv2 = conv_block(pool1,8,3,'relu')
  pool2 = MaxPooling2D((2,2))(conv2)
  
  conv3 = conv_block(pool2,16,3,'relu')
  pool3 = MaxPooling2D((2,2))(conv3)
  
  conv4 = conv_block(pool3,32,3,'relu')
  pool4 = MaxPooling2D((2,2))(conv4)
  
  conv5 = conv_block(pool4,64,3,'relu')  
  
  x = [conv1,conv2,conv3,conv4,conv5]
  return input_layer, x

def unet(image_width: int,
         image_heigth: int,
         n_channels: int,
         n_depth: int,
         n_classes: int,
         vgg16: bool = False,
         transfer_learning: bool = False
        ) -> tf.keras.Model:
  """
  Creates the U-Net architecture with slight modifications, using particularily less filters.
  
  Parameters
  ----------
  image_width : int
      Shape of the desired width for the inputted image.
      
  image_height : int
      Shape of the desired height for the inputted image.
      
  n_channels : int
      Number of channels of the inputted image.
      
  n_depth : int
      The desired depth level of the resulting U-Net architecture. 
      
  n_classes : int
      The number of classes to be predicted/number of filters for final prediction layer
      
  vgg16 : bool, default = False
      Boolean for using architecture VGG16 in encoder part of the model or not.
      
  transfer_learning : bool, default = True
      Boolean for using transfer learning with pre-trained weights from ImageNet or not.

  Returns
  -------
  tf.keras.Model
      The produced U-Net model.
  """
  if n_depth<1 or n_depth>5:
    raise Exception("Unsupported number of layers/upsamples")
  input_shape = [image_heigth, image_width, n_channels]
  if vgg16:
    encoded_model, x = encoder_VGG16(input_shape)
    if transfer_learning:
      encoded_model.trainable=False
  else:
    encoded_model, x = encoder_unet(input_shape)
  intermediate_model = x[n_depth-1]
  #Dropout
  for i in reversed(range(0,n_depth-1)):
    next_filters = x[i+1].shape[3]/2
    intermediate_model = Conv2DTranspose(filters=next_filters ,kernel_size=3,strides=2,padding='same')(intermediate_model)
    intermediate_model = tf.keras.layers.Concatenate()([intermediate_model,x[i]])
    intermediate_model = tf.keras.layers.BatchNormalization()(intermediate_model)
    intermediate_model = tf.keras.layers.ReLU()(intermediate_model)
    intermediate_model = conv_block(intermediate_model,next_filters,kernel_size=3,activation='relu')
    
  intermediate_model=Conv2D(filters=n_classes,kernel_size=(1,1),strides=(1),padding='same')(intermediate_model)
  intermediate_model = Reshape((image_heigth*image_width, n_classes))(intermediate_model)
  intermediate_model = Activation(tf.nn.softmax)(intermediate_model)
  intermediate_model = Reshape((image_heigth,image_width, n_classes))(intermediate_model)
  
  final_model=tf.keras.models.Model(inputs=encoded_model ,outputs=intermediate_model)
  return final_model

# COMMAND ----------

# MAGIC %md
# MAGIC Let's then create the model.

# COMMAND ----------

model = unet(128,128,3,5,3)

# COMMAND ----------

# MAGIC %md
# MAGIC And here is a summary of the model created...

# COMMAND ----------

model.summary()

# COMMAND ----------

# MAGIC %md
# MAGIC The model is then compiled and its prediction before training is shown.

# COMMAND ----------

model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['accuracy'])

def create_mask(pred_mask):
  pred_mask = tf.argmax(pred_mask, axis=-1)
  pred_mask = pred_mask[..., tf.newaxis]
  return pred_mask[0]

def show_predictions(dataset=None, num=1):
  if dataset:
    for image, mask in dataset.take(num):
      pred_mask = model.predict(image)
      display([image[0], mask[0], create_mask(pred_mask)])
  else:
    display([sample_image, sample_mask,
             create_mask(model.predict(sample_image[tf.newaxis, ...]))])
    
show_predictions()

# COMMAND ----------

# MAGIC %md
# MAGIC Below, the model is fitted against the training data and validated on the validation set after each epoch. A validation accuracy of 84.5 % is achieved after one epoch. A custom callback is added to trace the learning of the model througout its training.

# COMMAND ----------

class MyCustomCallback(tf.keras.callbacks.Callback):
  def on_epoch_end(self, epoch, logs=None):
    show_predictions()

EPOCHS = 50
VAL_SUBSPLITS = 5
VALIDATION_STEPS = info.splits['test'].num_examples//BATCH_SIZE//VAL_SUBSPLITS

model_history = model.fit(train_dataset, epochs=EPOCHS,
                          steps_per_epoch=STEPS_PER_EPOCH,
                          validation_steps=VALIDATION_STEPS,
                          validation_data=test_dataset,
                          callbacks = [MyCustomCallback()])


# COMMAND ----------

# MAGIC %md
# MAGIC Some predictions on the `test_dataset` are shown to showcase the performance of the model on images it has not been trained on.

# COMMAND ----------

show_predictions(test_dataset,num=10)

# COMMAND ----------

# MAGIC %md
# MAGIC Finally we plot the learning curves of the model in its 50 epochs of training. Both the loss curves as well as the accuracy curves are presented.

# COMMAND ----------

loss = model_history.history['loss']
val_loss = model_history.history['val_loss']
acc = model_history.history['accuracy']
val_acc = model_history.history['val_accuracy']

epochs = range(EPOCHS)

plt.figure(figsize=(10,5))
plt.subplot(1,2,1)
plt.plot(epochs, loss, 'r', label='Training loss')
plt.plot(epochs, val_loss, 'bo', label='Validation loss')
plt.title('Training and Validation Loss')
plt.xlabel('Epoch')
plt.ylabel('Loss Value')
plt.legend()
plt.subplot(1,2,2)
plt.plot(epochs, acc, 'r', label='Training accuracy')
plt.plot(epochs, val_acc, 'bo', label='Validation accuracy')
plt.title('Training and Validation Accuracy')
plt.xlabel('Epoch')
plt.ylabel('Accuracy')
plt.legend()
plt.show()

# COMMAND ----------

