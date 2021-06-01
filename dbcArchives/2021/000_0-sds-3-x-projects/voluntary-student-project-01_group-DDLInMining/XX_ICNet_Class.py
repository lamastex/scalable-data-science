# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from tensorflow.keras.layers import *
from tensorflow.keras.models import *
import numpy as np
from tensorflow.keras.applications.resnet50 import ResNet50
import matplotlib.pyplot as plt
import matplotlib.image as mpimg


# COMMAND ----------

def normalize(input_image, input_mask):
  input_image = tf.cast(input_image, tf.float32) / 255.0
  input_mask -= 1
  return input_image, input_mask

# Function for resizing the train images to the desired input shape of HxW as well as augmenting the training images.
@tf.function
def load_image_train(datapoint, wanted_height: int, wanted_width: int):
  input_image = tf.image.resize(datapoint['image'], (wanted_height, wanted_width))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (wanted_height, wanted_width))

  if tf.random.uniform(()) > 0.5:
    input_image = tf.image.flip_left_right(input_image)
    input_mask = tf.image.flip_left_right(input_mask)

  input_image, input_mask = normalize(input_image, input_mask)
  input_mask = tf.math.round(input_mask)

  return input_image, input_mask

# Function for resizing the test images to the desired output shape (no augmenation).
def load_image_test(datapoint, wanted_height: int, wanted_width: int):
  input_image = tf.image.resize(datapoint['image'], (wanted_height, wanted_width))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (wanted_height, wanted_width))

  input_image, input_mask = normalize(input_image, input_mask)

  return input_image, input_mask

def load_image_train_noTf(datapoint, wanted_height: int, wanted_width: int):
  input_image = tf.image.resize(datapoint['image'], (wanted_height, wanted_width))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (wanted_height, wanted_width))

  if tf.random.uniform(()) > 0.5:
    input_image = tf.image.flip_left_right(input_image)
    input_mask = tf.image.flip_left_right(input_mask)

  input_image, input_mask = normalize(input_image, input_mask)
  input_mask = tf.math.round(input_mask)

  return input_image, input_mask

# Functions for resizing the image to the desired size of factor 2 or 4 to be inputted to the ICNet architecture.
def resize_image16(img, mask, wanted_height: int, wanted_width: int):
  input_image = tf.image.resize(img, (wanted_height//16, wanted_width//16))
  input_mask=tf.image.resize(mask, (wanted_height//16, wanted_width//16))
  input_mask = tf.math.round(input_mask)
  return input_image, input_mask

def resize_image8(img, mask, wanted_height: int, wanted_width: int):
  input_image = tf.image.resize(img, (wanted_height//8, wanted_width//8))
  input_mask=tf.image.resize(mask, (wanted_height//8, wanted_width//8))
  input_mask = tf.math.round(input_mask)
  return input_image, input_mask

def resize_image4(img, mask, wanted_height: int, wanted_width: int):
  input_image = tf.image.resize(img, (wanted_height//4, wanted_width//4))
  input_mask=tf.image.resize(mask, (wanted_height//4, wanted_width//4))
  input_mask = tf.math.round(input_mask)
  return input_image, input_mask

# COMMAND ----------

def load_transform_data(input_height: int, input_width: int):
  dataset, info = tfds.load('oxford_iiit_pet:3.*.*', with_info=True)
  n_train = info.splits['train'].num_examples
  n_test = info.splits['test'].num_examples
  return(dataset, n_train, n_test)

def create_datasets(wanted_height:int, wanted_width:int, n_train:int, n_test:int, BATCH_SIZE:int = 64, BUFFER_SIZE:int = 1000):
  #Creating the ndarray in the correct shapes for training data
  train_original_img = np.ndarray(shape=(n_train, wanted_height, wanted_width, 3))
  
  train_original_mask = np.ndarray(shape=(n_train, wanted_height, wanted_width, 1))
  train16_mask = np.ndarray(shape=(n_train, wanted_height//16, wanted_width//16, 1))
  train8_mask = np.ndarray(shape=(n_train, wanted_height//8, wanted_width//8, 1))
  train4_mask = np.ndarray(shape=(n_train, wanted_height//4, wanted_width//4, 1))
  
  #Loading the data into the arrays 
  count = 0
  for datapoint in dataset['train']:
    img_orig, mask_orig = load_image_train_noTf(datapoint, wanted_height, wanted_width)
    train_original_img[count]=img_orig
    train_original_mask[count]=mask_orig
    
    img16, mask16 = resize_image16(img_orig, mask_orig, wanted_height, wanted_width)
    train16_mask[count]=(mask16)
    
    img8, mask8 = resize_image8(img_orig, mask_orig, wanted_height, wanted_width)
    train8_mask[count]=(mask8)
    
    img4, mask4 = resize_image4(img_orig, mask_orig, wanted_height, wanted_width)
    train4_mask[count]=(mask4)
    count+=1
  
  #Saving all img / mask in separate lists

  
  #Creating the ndarrays in the correct shapes for test data  
  test_original_img = np.ndarray(shape=(n_test,wanted_height,wanted_width,3))

  test16_mask = np.ndarray(shape=(n_test,wanted_height//16,wanted_width//16,1))
  test8_mask = np.ndarray(shape=(n_test,wanted_height//8,wanted_width//8,1))
  test4_mask = np.ndarray(shape=(n_test,wanted_height//4,wanted_width//4,1))
  
  #Loading the data into the arrays
  count=0
  for datapoint in dataset['test']:
    img_orig, mask_orig = load_image_test(datapoint, wanted_height, wanted_width)
    test_original_img[count]=(img_orig)
    test_original_mask[count]=(mask_orig)
    
    img16, mask16 = resize_image16(img_orig, mask_orig, wanted_height, wanted_width)
    test16_mask[count]=(mask16)
    #test16_img[count]=(img16)
    
    img8, mask8 = resize_image8(img_orig, mask_orig, wanted_height, wanted_width)
    test8_mask[count]=(mask8)
    #test8_img[count]=(img8)
    
    img4, mask4 = resize_image4(img_orig, mask_orig, wanted_height, wanted_width)
    test4_mask[count]=(mask4)
    #test4_img[count]=(img4)
    count+=1
    
  #Saving all img / mask in separate lists
  #test_img = [test_original_img, test16_img, test8_img, test4_img]
  #test_img = test_original_img
  #test_mask = [test_original_mask, test16_mask, test8_mask, test4_mask]
  #test_mask = [test16_mask, test8_mask, test4_mask]
  
  train_dataset = tf.data.Dataset.from_tensor_slices((train_original_img, {'output_1': train16_mask, 'output_2': train8_mask, 'output_3': train4_mask, 'output_4': train_original_mask}))
  test_dataset = tf.data.Dataset.from_tensor_slices((test_original_img, {'output_1': test16_mask, 'output_2': test8_mask, 'output_3': test4_mask, 'output_4': test_original_mask}))
  train_dataset = train_dataset.cache().shuffle(BUFFER_SIZE).batch(BATCH_SIZE).repeat()
  train_dataset.prefetch(buffer_size=tf.data.experimental.AUTOTUNE)
  test_dataset = test_dataset.batch(BATCH_SIZE)
  
  return train_dataset, test_dataset, train_original_mask[0], train_original_img[0]

# COMMAND ----------

dataset, n_train, n_test = load_transform_data(128,128) 

train_dataset, test_dataset, sample_mask, sample_image = create_datasets(128,128,n_train,n_test, 64, 1000)

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


sample_image, sample_mask = sample_image, sample_mask
display([sample_image, sample_mask])

# COMMAND ----------

class ICNet_model(tf.keras.Model):
  
  def __init__(self,
               encoder: tf.keras.Model,
               image_width: int,
               image_height: int,
               n_classes: int,
               n_filters: int = 16,
               kernel_size: tuple = (3,3),
               activation: str = 'relu'
              ):
    super(ICNet_model, self).__init__() #Skapar en input på self
    self.encoder = encoder
    self.n_classes = n_classes
    self.n_filters = n_filters
    self.kernel_size = kernel_size
    self.activation = activation
    self.image_width = image_width
    self.image_height = image_height
    
    # Defining the network
    input_shape = (self.image_height, self.image_width, 3)
    inputs = tf.keras.Input(shape=input_shape, name="input_img_1")
    input_obj_4 = tf.keras.layers.experimental.preprocessing.Resizing(
    self.image_height//4, self.image_width//4, interpolation="bilinear", name="input_img_4")(inputs)
    input_obj_2 = tf.keras.layers.experimental.preprocessing.Resizing(
    self.image_height//2, self.image_width//2, interpolation="bilinear", name="input_img_2")(inputs)
    ICNet_Model1=self.ICNet_1(inputs, self.n_filters, self.kernel_size, self.activation)   
    PSP_Model = self.PSPNet(self.encoder,self.n_classes, self.n_filters, self.kernel_size, self.activation, self.image_width//4, self.image_height//4, True)
    last_layer = PSP_Model.get_layer('conv3_block4_out').output
    PSPModel_2_4 = tf.keras.models.Model(inputs=PSP_Model.input, outputs=last_layer, name="JointResNet_2_4")
    ICNet_Model4 = PSPModel_2_4(input_obj_4)
    ICNet_Model2 = PSPModel_2_4(input_obj_2) 
    ICNet_4_rest = self.PSP_rest(ICNet_Model4)
    out1, last_layer = self.CFF(1, ICNet_4_rest, ICNet_Model2, self.n_classes, self.image_width//32, self.image_height//32)
    out2, last_layer = self.CFF(2, last_layer, ICNet_Model1, self.n_classes, self.image_width//16, self.image_height//16)
    upsample_2 = UpSampling2D(2, interpolation='bilinear', name="Upsampling_final_prediction")(last_layer)
    output = Conv2D(self.n_classes, 1, name="output_3", activation='softmax')(upsample_2)
    self.network = tf.keras.models.Model(inputs=input_obj, outputs=[out1, out2, output])
    
  # Function for the pooling module which takes the output of ResNet50 as input as well as its width and height and pool it with a factor.
  def pool_block(self,
                 cur_tensor,
                 image_width,
                 image_height,
                 pooling_factor,
                 activation):

    strides = [int(np.round(float(image_width)/pooling_factor)),
              int(np.round(float(image_height)/pooling_factor))]
    pooling_size = strides
    x = AveragePooling2D(pooling_size, strides=strides, padding='same')(cur_tensor)
    x = Conv2D(128,(1,1),padding='same')(x)
    x = BatchNormalization()(x)
    x = Activation(activation)(x)
    x = tf.keras.layers.experimental.preprocessing.Resizing(
      image_height, image_width, interpolation="bilinear")(x) # Resizing images to correct shape for future concat
    return x


  
  # Function for creating the PSPNet model. The inputs is the number of classes to classify, number of filters to use, kernel_size, activation function, 
  # input image width and height and a boolean for knowing if the module is part of the ICNet or not.
  def PSPNet(self, 
             encoder: tf.keras.Model,
             n_classes: int,
             n_filters: int,
             kernel_size: tuple,
             activation: str,
             image_width: int,
             image_height: int,
             dropout: bool = True,
             bn: bool = True):
    #encoder=ResNet50(include_top=False, weights='imagenet', input_shape=input_shape)
    #encoder=self.modify_ResNet_Dilation(encoder)
    #new_encoder = create_modified_encoder(encoder, dropout, bn)
    #encoder.trainable=False
    resnet_output=encoder.output
    #print(encoder.output)
    pooling_layer=[]
    pooling_layer.append(resnet_output)
    output=(resnet_output)
    h = image_height//8
    w = image_width//8
    for i in [1,2,3,6]:
      pool = self.pool_block(output, h, w, i, activation)
      pooling_layer.append(pool)
    concat=Concatenate()(pooling_layer)
    output_layer=Conv2D(filters=n_classes, kernel_size=(1,1), padding='same')(concat)
    final_layer=UpSampling2D(size=(8,8), data_format='channels_last', interpolation='bilinear')(output_layer)
    final_model=tf.keras.models.Model(inputs=encoder.input, outputs=final_layer)
    return final_model
  
  # Function for adding stage 4 and 5 of ResNet50 to the 1/4 image size branch of the ICNet.
  def PSP_rest(self, input_prev: tf.Tensor):

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
  def CFF(self, stage: int, F_small, F_large, n_classes: int, input_width_small: int, input_height_small: int):
    F_up = tf.keras.layers.experimental.preprocessing.Resizing(int(input_width_small*2), int(input_height_small*2), interpolation="bilinear",
                                                               name="Upsample_x2_small_{}".format(stage))(F_small)
    F_aux = Conv2D(n_classes, 1, name="CC_{}".format(stage), activation='softmax')(F_up)
    #y = ZeroPadding2D(padding=2, name='padding17')(F_up) ?? behövs denna?
    intermediate_f_small = Conv2D(128, 3, dilation_rate=2, padding='same', name="intermediate_f_small_{}".format(stage))(F_up)
    intermediate_f_small_bn = BatchNormalization(name="intermediate_f_small_bn_{}".format(stage))(intermediate_f_small)
    intermediate_f_large = Conv2D(128, 1, padding='same', name="intermediate_f_large_{}".format(stage))(F_large)
    intermediate_f_large_bn = BatchNormalization(name="intermediate_f_large_bn_{}".format(stage))(intermediate_f_large)
    intermediate_f_sum = Add(name="add_intermediates_{}".format(stage))([intermediate_f_small_bn,intermediate_f_large_bn])
    intermediate_f_relu = Activation('relu', name="activation_CFF_{}".format(stage))(intermediate_f_sum)
    return F_aux, intermediate_f_relu
  

  # Function for the high-res branch of ICNet where image is in scale 1:1. The inputs are the input image, number of filters, kernel size and desired activation function.
  def ICNet_1(self,
              input_shape,
              n_filters: int,
              kernel_size: tuple,
              activation: str):
    for i in range(1,4):
      conv1=Conv2D(filters=n_filters*2*i, kernel_size=kernel_size, strides=(2,2), padding='same', input_shape=input_shape)
      batch_norm1=BatchNormalization()(conv1)
      temp=Activation(activation)(batch_norm1)
    return temp  

  def call(self, inputs, training=False):
    input_obj_4 = tf.keras.layers.experimental.preprocessing.Resizing(
    self.image_height//4, self.image_width//4, interpolation="bilinear", name="input_img_4")(inputs)
    input_obj_2 = tf.keras.layers.experimental.preprocessing.Resizing(
    self.image_height//2, self.image_width//2, interpolation="bilinear", name="input_img_2")(inputs)
    ICNet_Model1=self.ICNet_1(inputs, self.n_filters, self.kernel_size, self.activation)   
    PSP_Model = self.PSPNet(self.encoder,self.n_classes, self.n_filters, self.kernel_size, self.activation, self.image_width//4, self.image_height//4, True)
    last_layer = PSP_Model.get_layer('conv3_block4_out').output
    PSPModel_2_4 = tf.keras.models.Model(inputs=PSP_Model.input, outputs=last_layer, name="JointResNet_2_4")
    ICNet_Model4 = PSPModel_2_4(input_obj_4)
    ICNet_Model2 = PSPModel_2_4(input_obj_2) 
    ICNet_4_rest = self.PSP_rest(ICNet_Model4)
    out1, last_layer = self.CFF(1, ICNet_4_rest, ICNet_Model2, self.n_classes, self.image_width//32, self.image_height//32)
    out2, last_layer = self.CFF(2, last_layer, ICNet_Model1, self.n_classes, self.image_width//16, self.image_height//16)
    upsample_2 = UpSampling2D(2, interpolation='bilinear', name="Upsampling_final_prediction")(last_layer)
    output = Conv2D(self.n_classes, 1, name="output_3", activation='softmax')(upsample_2)
    return out1, out2, output 

# COMMAND ----------

#Function for formatting the resnet model to a modified one which takes advantage of dilation rates instead of strides in the final blocks.

def modify_ResNet_Dilation(model):
  for i in range(0,4):
    model.get_layer('conv4_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv4_block1_{}_conv'.format(i)).dilation_rate = 2
    model.get_layer('conv5_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv5_block1_{}_conv'.format(i)).dilation_rate = 4
  model.save('/tmp/my_model')
  new_model = tf.keras.models.load_model('/tmp/my_model')
  return new_model

input_shape=(None, None, 3)
encoder=ResNet50(include_top=False, weights='imagenet', input_shape=input_shape)
encoder=modify_ResNet_Dilation(encoder)
model = ICNet_model(encoder, 128,128,3)

# COMMAND ----------

model.build([64,128,128,3])
model.summary()
model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(), loss_weights=[0.1,0.3,0.6],
              metrics="acc")

# COMMAND ----------

TRAIN_LENGTH = n_train
BATCH_SIZE = 64
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH // BATCH_SIZE

EPOCHS = 100
VAL_SUBSPLITS = 5
VALIDATION_STEPS = n_test//BATCH_SIZE//VAL_SUBSPLITS
res_eval_1 = []

class MyCustomCallback(tf.keras.callbacks.Callback):
    def on_epoch_end(self, epoch, logs=None):
        show_predictions()


model_history =  model.fit(train_dataset, epochs=EPOCHS, steps_per_epoch=STEPS_PER_EPOCH, callbacks=[MyCustomCallback()]) 

# COMMAND ----------

