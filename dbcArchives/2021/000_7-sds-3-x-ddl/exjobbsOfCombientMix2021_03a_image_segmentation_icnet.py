# Databricks notebook source
# MAGIC %md
# MAGIC # Implementation of [ICNet](https://arxiv.org/pdf/1704.08545.pdf)
# MAGIC 
# MAGIC William Anzén ([Linkedin](https://www.linkedin.com/in/william-anz%C3%A9n-b52003199/)), Christian von Koch ([Linkedin](https://www.linkedin.com/in/christianvonkoch/))
# MAGIC 
# MAGIC 2021, Stockholm, Sweden
# MAGIC 
# MAGIC This project was supported by Combient Mix AB under the industrial supervision of Razesh Sainudiin and Max Fischer.
# MAGIC 
# MAGIC In this notebook, an implementation of [ICNet](https://arxiv.org/pdf/1704.08545.pdf) is presented which is an architecture which uses a trade-off between complexity and inference time efficiently. The architecture is evaluated against the [Oxford-IIIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/). This notebook has reused material from the [Image Segmentation Tutorial](https://www.tensorflow.org/tutorials/images/segmentation) on TensorFlow for loading the dataset and showing predictions.

# COMMAND ----------

# MAGIC %md
# MAGIC Importing the required packages.

# COMMAND ----------

import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from tensorflow.keras.layers import AveragePooling2D, Conv2D, BatchNormalization, Activation, Concatenate, UpSampling2D, Reshape, Add 
from tensorflow.keras import Model
import numpy as np
from tensorflow.keras.applications.resnet50 import ResNet50
from tensorflow.keras.optimizers import Adam
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
from typing import Tuple

# COMMAND ----------

# MAGIC %md
# MAGIC Setting memory growth to the GPUs is recommended as these model is quite memory intensive.

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
# MAGIC Defining functions for normalizing and transforming the images.

# COMMAND ----------

def normalize(input_image, input_mask):
  input_image = tf.cast(input_image, tf.float32) / 255.0
  input_mask -= 1
  return input_image, input_mask

# Function for resizing the train images to the desired input shape of HxW as well as augmenting the training images.
@tf.function
def load_image_train(datapoint):
  input_image = tf.image.resize(datapoint['image'], (128, 128))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (128, 128))

  if tf.random.uniform(()) > 0.5:
    input_image = tf.image.flip_left_right(input_image)
    input_mask = tf.image.flip_left_right(input_mask)

  input_image, input_mask = normalize(input_image, input_mask)
  input_mask = tf.math.round(input_mask)

  return input_image, input_mask

# Function for resizing the test images to the desired output shape (no augmentaion).
def load_image_test(datapoint):
  input_image = tf.image.resize(datapoint['image'], (128, 128))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (128, 128))

  input_image, input_mask = normalize(input_image, input_mask)

  return input_image, input_mask



# Functions for resizing the mask to the desired size of factor 4, 8 or 16 to be used as cascade losses.
def resize_image(mask, wanted_height: int, wanted_width: int, resize_factor : int):
  input_mask=tf.image.resize(mask, (wanted_height//resize_factor, wanted_width//resize_factor))
  input_mask = tf.math.round(input_mask)
  return input_mask

# Function for resizing the masks used as cascade losses in the ICNet architecture.
def preprocess_icnet(image, 
                     mask,
                     image_height : int = 128,
                     image_width : int = 128):
  mask4 = resize_image(mask, image_height, image_width, 4)
  mask8 = resize_image(mask, image_height, image_width, 8)
  mask16 = resize_image(mask, image_height, image_width, 16)
  return image, {'CC_1': mask16, 'CC_2': mask8, 'CC_fin': mask4, 'final_output': mask}

# COMMAND ----------

# MAGIC %md
# MAGIC Separating dataset into input and multiple outputs of different sizes.

# COMMAND ----------

dataset, info = tfds.load('oxford_iiit_pet:3.*.*', with_info=True)
n_train = info.splits['train'].num_examples
n_test = info.splits['test'].num_examples

TRAIN_LENGTH = n_train
BATCH_SIZE = 64
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH // BATCH_SIZE

train = dataset['train'].map(load_image_train, num_parallel_calls=tf.data.experimental.AUTOTUNE)
test = dataset['test'].map(load_image_test)

train = train.map(preprocess_icnet, num_parallel_calls=tf.data.experimental.AUTOTUNE)
test = test.map(preprocess_icnet)

train_dataset = train.cache().shuffle(BUFFER_SIZE).batch(BATCH_SIZE).repeat()
train_dataset.prefetch(buffer_size=tf.data.experimental.AUTOTUNE)
test_dataset = test.batch(BATCH_SIZE)



# COMMAND ----------

# MAGIC %md
# MAGIC Defining the function for displaying images and the model's predictions jointly.

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

for image, mask in train.take(1):
  sample_image, sample_mask = image, mask['final_output']
display([sample_image, sample_mask])

# COMMAND ----------

# MAGIC %md
# MAGIC Defining the functions needed for the PSPNet module.

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

# Function for formatting the resnet model to a modified one which takes advantage of dilation rates instead of strides in the final blocks
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
  Setting up the PSPNet model.
  
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
# MAGIC Defining the functions needed for the ICNet.

# COMMAND ----------

def PSP_rest(input_prev: tf.Tensor
            ) -> tf.Tensor:
  """
  Function for adding stage 4 and 5 of ResNet50 to the 1/4 image size branch of the ICNet.
  
    Parameters
    ----------
    input_prev: tf.Tensor
        The incoming tensor from the output of the joint medium and low resolution branch.
    
    Returns
    -------
    tf.Tensor
        The tensor used to continue the 1/4th branch.
  """
  
  
  y_ = input_prev
  #Stage 4
  #Conv_Block
  y = Conv2D(256, 1, dilation_rate=2, padding='same', name='C4_block1_conv1')(y_)
  y = BatchNormalization(name='C4_block1_bn1')(y)
  y = Activation('relu', name='C4_block1_act1')(y)
  y = Conv2D(256, 3, dilation_rate=2, padding='same', name='C4_block1_conv2')(y)
  y = BatchNormalization(name='C4_block1_bn2')(y)
  y = Activation('relu', name='C4_block1_act2')(y)
  y_ = Conv2D(1024, 1, dilation_rate=2, padding='same', name='C4_block1_conv0')(y_)
  y = Conv2D(1024, 1, dilation_rate=2, padding='same', name='C4_block1_conv3')(y)
  y_ = BatchNormalization(name='C4_block1_bn0')(y_)
  y = BatchNormalization(name='C4_block1_bn3')(y)
  y = Add(name='C4_skip1')([y_,y])
  y_ = Activation('relu', name='C4_block1_act3')(y)
  #IDBLOCK1
  y = Conv2D(256, 1, dilation_rate=2, padding='same', name='C4_block2_conv1')(y_)
  y = BatchNormalization(name='C4_block2_bn1')(y)
  y = Activation('relu', name='C4_block2_act1')(y)
  y = Conv2D(256, 3, dilation_rate=2, padding='same', name='C4_block2_conv2')(y)
  y = BatchNormalization(name='C4_block2_bn2')(y)
  y = Activation('relu', name='C4_block2_act2')(y)
  y = Conv2D(1024,1, dilation_rate=2, padding='same', name='C4_block2_conv3')(y)
  y = BatchNormalization(name='C4_block2_bn3')(y)
  y = Add(name='C4_skip2')([y_,y])
  y_ = Activation('relu', name='C4_block2_act3')(y)
  #IDBLOCK2
  y = Conv2D(256, 1, dilation_rate=2, padding='same', name='C4_block3_conv1')(y_)
  y = BatchNormalization(name='C4_block3_bn1')(y)
  y = Activation('relu', name='C4_block3_act1')(y)
  y = Conv2D(256, 3, dilation_rate=2, padding='same', name='C4_block3_conv2')(y)
  y = BatchNormalization(name='C4_block3_bn2')(y)
  y = Activation('relu', name='C4_block3_act2')(y)
  y = Conv2D(1024,1, dilation_rate=2, padding='same', name='C4_block3_conv3')(y)
  y = BatchNormalization(name='C4_block3_bn3')(y)
  y = Add(name='C4_skip3')([y_,y])
  y_ = Activation('relu', name='C4_block3_act3')(y)
  #IDBlock3
  y = Conv2D(256, 1, dilation_rate=2, padding='same', name='C4_block4_conv1')(y_)
  y = BatchNormalization(name='C4_block4_bn1')(y)
  y = Activation('relu', name='C4_block4_act1')(y)
  y = Conv2D(256, 3, dilation_rate=2, padding='same', name='C4_block4_conv2')(y)
  y = BatchNormalization(name='C4_block4_bn2')(y)
  y = Activation('relu', name='C4_block4_act2')(y)
  y = Conv2D(1024,1, dilation_rate=2, padding='same', name='C4_block4_conv3')(y)
  y = BatchNormalization(name='C4_block4_bn3')(y)
  y = Add(name='C4_skip4')([y_,y])
  y_ = Activation('relu', name='C4_block4_act3')(y)
  #ID4
  y = Conv2D(256, 1, dilation_rate=2, padding='same', name='C4_block5_conv1')(y_)
  y = BatchNormalization(name='C4_block5_bn1')(y)
  y = Activation('relu', name='C4_block5_act1')(y)
  y = Conv2D(256, 3, dilation_rate=2, padding='same', name='C4_block5_conv2')(y)
  y = BatchNormalization(name='C4_block5_bn2')(y)
  y = Activation('relu', name='C4_block5_act2')(y)
  y = Conv2D(1024,1, dilation_rate=2, padding='same', name='C4_block5_conv3')(y)
  y = BatchNormalization(name='C4_block5_bn3')(y)
  y = Add(name='C4_skip5')([y_,y])
  y_ = Activation('relu', name='C4_block5_act3')(y)
  #ID5
  y = Conv2D(256, 1, dilation_rate=2, padding='same', name='C4_block6_conv1')(y_)
  y = BatchNormalization(name='C4_block6_bn1')(y)
  y = Activation('relu', name='C4_block6_act1')(y)
  y = Conv2D(256, 3, dilation_rate=2, padding='same', name='C4_block6_conv2')(y)
  y = BatchNormalization(name='C4_block6_bn2')(y)
  y = Activation('relu', name='C4_block6_act2')(y)
  y = Conv2D(1024,1, dilation_rate=2, padding='same', name='C4_block6_conv3')(y)
  y = BatchNormalization(name='C4_block6_bn3')(y)
  y = Add(name='C4_skip6')([y_,y])
  y_ = Activation('relu', name='C4_block6_act3')(y)
  
  #Stage 5
  #Conv
  y = Conv2D(512, 1, dilation_rate=4,padding='same', name='C5_block1_conv1')(y_)
  y = BatchNormalization(name='C5_block1_bn1')(y)
  y = Activation('relu', name='C5_block1_act1')(y)
  y = Conv2D(512, 3, dilation_rate=4,padding='same', name='C5_block1_conv2')(y)
  y = BatchNormalization(name='C5_block1_bn2')(y)
  y = Activation('relu', name='C5_block1_act2')(y)
  y_ = Conv2D(2048, 1, dilation_rate=4,padding='same', name='C5_block1_conv0')(y_)
  y = Conv2D(2048, 1, dilation_rate=4,padding='same', name='C5_block1_conv3')(y)
  y_ = BatchNormalization(name='C5_block1_bn0')(y_)
  y = BatchNormalization(name='C5_block1_bn3')(y)
  y = Add(name='C5_skip1')([y_,y])
  y_ = Activation('relu', name='C5_block1_act3')(y)
  
  #ID
  y = Conv2D(512, 1, dilation_rate=4,padding='same', name='C5_block2_conv1')(y_)
  y = BatchNormalization(name='C5_block2_bn1')(y)
  y = Activation('relu', name='C5_block2_act1')(y)
  y = Conv2D(512, 3, dilation_rate=4,padding='same', name='C5_block2_conv2')(y)
  y = BatchNormalization(name='C5_block2_bn2')(y)
  y = Activation('relu', name='C5_block2_act2')(y)
  y = Conv2D(2048, 1, dilation_rate=4,padding='same', name='C5_block2_conv3')(y)
  y = BatchNormalization(name='C5_block2_bn3')(y)
  y = Add(name='C5_skip2')([y_,y])
  y_ = Activation('relu', name='C5_block2_act3')(y)
  
  #ID
  y = Conv2D(512, 1, dilation_rate=4,padding='same', name='C5_block3_conv1')(y_)
  y = BatchNormalization(name='C5_block3_bn1')(y)
  y = Activation('relu', name='C5_block3_act1')(y)
  y = Conv2D(512, 3, dilation_rate=4,padding='same', name='C5_block3_conv2')(y)
  y = BatchNormalization(name='C5_block3_bn2')(y)
  y = Activation('relu', name='C5_block3_act2')(y)
  y = Conv2D(2048, 1, dilation_rate=4,padding='same', name='C5_block3_conv3')(y)
  y = BatchNormalization(name='C5_block3_bn3')(y)
  y = Add(name='C5_skip3')([y_,y])
  y_ = Activation('relu', name='C5_block3_act3')(y)
  
  return(y_)

# Function for the CFF module in the ICNet architecture. The inputs are which stage (1 or 2), the output from the smaller branch, the output from the
# larger branch, n_classes and the width and height of the output of the smaller branch.
def CFF(stage : int, 
        F_small : tf.Tensor, 
        F_large : tf.Tensor, 
        n_classes: int,
        input_height_small: int,
        input_width_small: int
        ) -> Tuple[tf.Tensor, tf.Tensor]:
  """
  Function for creating the cascade feature fusion (CFF) inside the ICNet model.
  
    Parameters
    ----------
    stage : int
        Integer determining the stage of the CFF in order to name the layers correctly.
        
     F_small : tf.Tensor
         The smaller tensor to be used in the CFF.
        
     F_large : tf.Tensor
         The larger tensor to be used in the CFF.
    
    n_classes : int
        Number of classes.
        
    input_height_small: int
        The height of the smaller tensor.
    
    input_width_small: int
        The width of the smaller tensor.
    -------
    tf.Tensor
        The tensor used to calculate the auxilliary loss.
        
    tf.Tensor    
        The tensor used to continue the model.
  """
  
  F_small = tf.keras.layers.experimental.preprocessing.Resizing(int(input_width_small*2), int(input_height_small*2), interpolation="bilinear", name="Upsample_x2_small_{}".format(stage))(F_small)
  F_aux = Conv2D(n_classes, 1, name="CC_{}".format(stage), activation='softmax')(F_small)
  
  F_small = Conv2D(128, 3, dilation_rate=2, padding='same', name="intermediate_f_small_{}".format(stage))(F_small)
  F_small = BatchNormalization(name="intermediate_f_small_bn_{}".format(stage))(F_small)
  
  F_large = Conv2D(128, 1, padding='same', name="intermediate_f_large_{}".format(stage))(F_large)
  F_large = BatchNormalization(name="intermediate_f_large_bn_{}".format(stage))(F_large)
  
  F_small = Add(name="add_intermediates_{}".format(stage))([F_small,F_large])
  F_small = Activation('relu', name="activation_CFF_{}".format(stage))(F_small)
  return F_aux, F_small

def ICNet_1(input_obj : tf.keras.Input,
            n_filters: int,
            kernel_size: tuple,
            activation: str
           ) -> tf.Tensor:
  """
  Function for the high-res branch of ICNet where image is in scale 1:1.
  
    Parameters
    ----------
    input_obj : tf.keras.Input
        The input object inputted to the branch.
        
    n_filters : int
        Number of filters.
        
    kernel_size : tuple
        Size of the kernel.    
        
    activation : str
        Activation function to be applied.
        
    Returns
    -------
    tf.Tensor
        The output of the original size branch as a tf.Tensor.
  """
  for i in range(1,4):
    input_obj=Conv2D(filters=n_filters*2*i, kernel_size=kernel_size, strides=(2,2), padding='same')(input_obj)
    input_obj=BatchNormalization()(input_obj)
    input_obj=Activation(activation)(input_obj)
  return input_obj  

def ICNet(image_height: int,
         image_width: int,
         n_classes: int,
         n_filters: int = 16,
         kernel_size: tuple = (3,3),
         activation: str = 'relu'
         ) -> tf.keras.Model:
  """
  Function for creating the ICNet model.
  
    Parameters
    ----------
    image_height : int
        Height of the image.
        
    image_width : int
        Width of the image.
        
    n_classes : int
        Number of classes.
        
    n_filters : int, default = 16
        Number of filters in the ICNet original size image branch.
        
    kernel_size : tuple, default = (3, 3)
        Size of the kernel.    
        
    activation : str, default = 'relu'
        Activation function to be applied after the pooling operations.
        
    Returns
    -------
    tf.keras.Model
        The finished keras model for the ICNet.
  """
  input_shape=[image_height,image_width,3]
  input_obj = tf.keras.Input(shape=input_shape, name="input_img_1")
  input_obj_4 = tf.keras.layers.experimental.preprocessing.Resizing(
    image_height//4, image_width//4, interpolation="bilinear", name="input_img_4")(input_obj)
  input_obj_2 = tf.keras.layers.experimental.preprocessing.Resizing(
    image_height//2, image_width//2, interpolation="bilinear", name="input_img_2")(input_obj)
  ICNet_Model1=ICNet_1(input_obj, n_filters, kernel_size, activation)
  PSPModel = PSPNet(image_height//4, image_width//4, n_classes, isICNet=True)
  PSPModel_2_4 = tf.keras.models.Model(inputs=PSPModel.input, outputs=PSPModel.get_layer('conv4_block3_out').output, name="JointResNet_2_4")
  
  ICNet_Model4 = PSPModel_2_4(input_obj_4)
  ICNet_Model2 = PSPModel_2_4(input_obj_2) 
  ICNet_4_rest = PSP_rest(ICNet_Model4)
  
  out1, last_layer = CFF(1, ICNet_4_rest, ICNet_Model2, n_classes, image_height//32, image_width//32)
  out2, last_layer = CFF(2, last_layer, ICNet_Model1, n_classes, image_height//16, image_width//16)
  upsample_2 = UpSampling2D(2, interpolation='bilinear', name="Upsampling_final_prediction")(last_layer)
  output = Conv2D(n_classes, 1, name="CC_fin", activation='softmax')(upsample_2)
  final_output = UpSampling2D(4, interpolation='bilinear', name='final_output')(output)
  final_model = tf.keras.models.Model(inputs=input_obj, outputs=[out1, out2, output, final_output])
  return final_model

# COMMAND ----------

# MAGIC %md
# MAGIC Let's call the ICNet function to create the model with input shape (128, 128, 3) and 3 classes with the standard values for number of filters, kernel size and activation function.

# COMMAND ----------

model=ICNet(128,128,3)

# COMMAND ----------

# MAGIC %md
# MAGIC Here is the summary of the model.

# COMMAND ----------

model.summary() 

# COMMAND ----------

# MAGIC %md
# MAGIC Compiling the model with optimizer `Adam`, loss function `SparseCategoricalCrossentropy` and metrics `SparseCategoricalAccuracy`. We also add loss weights 0.4, 0.4, 1 and 0 to the lower resolution output, medium resolution output and high resolution output and final output (only evaluated in testing phase) respectively as was done in the original ICNet paper. 

# COMMAND ----------

model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(), loss_weights=[0.4,0.4,1,0],
              metrics="acc")

# COMMAND ----------

# MAGIC %md
# MAGIC Below, the functions for displaying the predictions from the model against the true image are defined. 

# COMMAND ----------

# Function for creating the predicted image. It takes the max value between the classes and assigns the correct class label to the image, thus creating a predicted mask. 
def create_mask(pred_mask):
  pred_mask = tf.argmax(pred_mask, axis=-1)
  pred_mask = pred_mask[..., tf.newaxis]
  return pred_mask[0]

# Function for showing the model prediction. Output can be 0, 1 or 2 depending on if you want to see the low resolution, medium resolution or high resolution prediction respectively. 
def show_predictions(dataset=None, num=1, output=3):
  if dataset:
    for image, mask in dataset.take(num):
      pred_mask = model.predict(image[tf.newaxis,...])[output]
      display([image, mask['final_output'], create_mask(pred_mask)])
  else:
    display([sample_image, sample_mask,
             create_mask(model.predict(sample_image[tf.newaxis, ...])[output])])
    
show_predictions()

# COMMAND ----------

# MAGIC %md
# MAGIC Let's define the variables needed for training the model.

# COMMAND ----------

EPOCHS = 50
VAL_SUBSPLITS = 5
VALIDATION_STEPS = n_test//BATCH_SIZE//VAL_SUBSPLITS

# COMMAND ----------

# MAGIC %md
# MAGIC And here we define the custom callback function for showing how the model improves its predictions.

# COMMAND ----------

class MyCustomCallback(tf.keras.callbacks.Callback):
  def on_epoch_end(self, epoch, logs=None):
    show_predictions()
    

# COMMAND ----------

# MAGIC %md
# MAGIC Finally, we fit the model to the Oxford dataset.

# COMMAND ----------

model_history =  model.fit(train_dataset, epochs=EPOCHS, steps_per_epoch=STEPS_PER_EPOCH, 
                           validation_steps=VALIDATION_STEPS, validation_data=test_dataset, callbacks=[MyCustomCallback()]) 

# COMMAND ----------

# MAGIC %md
# MAGIC Finally, we visualize some predictions on the test dataset.

# COMMAND ----------

show_predictions(test, 10)

# COMMAND ----------

# MAGIC %md
# MAGIC We also visualize the accuracies and losses through the library `matplotlib`.

# COMMAND ----------

loss = model_history.history['loss']
acc = model_history.history['final_output_acc']
val_loss = model_history.history['val_loss']
val_loss1 = model_history.history['val_CC_1_loss']
val_loss2 = model_history.history['val_CC_2_loss']
val_loss3 = model_history.history['val_CC_fin_loss']
val_loss4 = model_history.history['val_final_output_loss']
val_acc1 = model_history.history['val_CC_1_acc']
val_acc2 = model_history.history['val_CC_2_acc']
val_acc3 = model_history.history['val_CC_fin_acc']
val_acc4 = model_history.history['val_final_output_acc']

epochs = range(EPOCHS)

plt.figure(figsize=(20,3))
plt.subplot(1,4,1)
plt.plot(epochs, loss, 'r', label='Training loss')
plt.plot(epochs, val_loss, 'bo', label='Validation loss')
plt.title('Training and Validation Loss')
plt.xlabel('Epoch')
plt.ylabel('Loss Value')
plt.legend()
plt.subplot(1,4,2)
plt.plot(epochs, acc, 'r', label="Training accuracy")
plt.plot(epochs, val_acc4, 'bo', label="Validation accuracy")
plt.xlabel('Epoch')
plt.ylabel('Accuracy')
plt.title('Training and Validation Accuracy')
plt.legend()
plt.subplot(1,4,3)
plt.plot(epochs, val_loss1, 'bo', label="CC_1")
plt.plot(epochs, val_loss2, 'go', label="CC_2")
plt.plot(epochs, val_loss3, 'yo', label="CC_fin")
plt.plot(epochs, val_loss4, 'yo', label="final_output")
plt.xlabel('Epoch')
plt.ylabel('Loss Value')
plt.title('Validation Loss for the Different Outputs')
plt.legend()
plt.subplot(1,4,4)
plt.plot(epochs, val_acc1, 'bo', label="CC_1")
plt.plot(epochs, val_acc2, 'go', label="CC_2")
plt.plot(epochs, val_acc3, 'yo', label="CC_fin")
plt.plot(epochs, val_acc4, 'yo', label="final_output")
plt.xlabel('Epoch')
plt.ylabel('Accuracy')
plt.title('Validation Accuracy for the Different Outputs')
plt.legend()
plt.show()

# COMMAND ----------

