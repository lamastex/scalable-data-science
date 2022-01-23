<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Implementation of [ICNet](https://arxiv.org/pdf/1704.08545.pdf)
===============================================================

In this notebook, an implementation of [ICNet](https://arxiv.org/pdf/1704.08545.pdf) is presented which is an architecture which uses a trade-off between complexity and inference time efficiently. The architecture is evaluated against the Oxford pets dataset. This notebook has reused material from the [Image Segmentation Tutorial](https://www.tensorflow.org/tutorials/images/segmentation) on Tensorflow

</div>

<div class="cell markdown">

Importing the required packages.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from tensorflow.keras.layers import *
from tensorflow.keras.models import *
import numpy as np
from tensorflow.keras.applications.resnet50 import ResNet50
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import tensorflow_addons as tfa
from tensorflow.keras import backend as K
import horovod.tensorflow.keras as hvd
```

</div>

<div class="cell markdown">

Setting up checkpoint location... The next cell creates a directory for saved checkpoint models.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import os
import time

checkpoint_dir = '/dbfs/ml/OxfordDemo/train/{}/'.format(time.time())

os.makedirs(checkpoint_dir)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Including MLflow
import mlflow
import mlflow.tensorflow
import os
print("MLflow Version: %s" % mlflow.__version__)

# Configure Databricks MLflow environment
mlflow.set_tracking_uri("databricks")
DEMO_SCOPE_TOKEN_NAME = "databricksEducational"
databricks_host = 'https://dbc-635ca498-e5f1.cloud.databricks.com/'
databricks_token = dbutils.secrets.get(scope = DEMO_SCOPE_TOKEN_NAME, key = "databricksCLIToken")
os.environ['DATABRICKS_HOST'] = databricks_host
os.environ['DATABRICKS_TOKEN'] = databricks_token

# Configure output folder to store TF events
output_root = "/ml/OxfordDemo/logs/"
output_dir = "/dbfs" + output_root
os.environ['OUTPUT_DIR'] = output_dir
```

</div>

<div class="cell markdown">

Loading and transforming the dataset.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def normalize(input_image, input_mask):
  input_image = tf.cast(input_image, tf.float32) / 255.0
  input_mask -= 1
  return input_image, input_mask

# Function for resizing the train images to the desired input shape of HxW as well as augmenting the training images.
def load_image_train_noTf(datapoint, wanted_height: int, wanted_width: int):
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
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def create_datasets_hvd(wanted_height:int, wanted_width:int, BATCH_SIZE:int = 64, BUFFER_SIZE:int = 1000, rank=0, size=1):
  dataset, info = tfds.load('oxford_iiit_pet:3.*.*', data_dir='Oxford-%d' % rank, with_info=True)
  n_train = info.splits['train'].num_examples
  n_test = info.splits['test'].num_examples
  
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
  
  #Creating the ndarrays in the correct shapes for test data  
  test_original_img = np.ndarray(shape=(n_test,wanted_height,wanted_width,3))
  
  test_original_mask = np.ndarray(shape=(n_test,wanted_height,wanted_width,1))
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
    
  
  train_dataset = tf.data.Dataset.from_tensor_slices((train_original_img[rank::size], {'CC_1': train16_mask[rank::size], 'CC_2': train8_mask[rank::size], 'CC_fin': train4_mask[rank::size], 'final_output': train_original_mask[rank::size]}))
  orig_test_dataset = tf.data.Dataset.from_tensor_slices((test_original_img[rank::size], {'CC_1': test16_mask[rank::size], 'CC_2': test8_mask[rank::size], 'CC_fin': test4_mask[rank::size], 'final_output': test_original_mask[rank::size]}))
  train_dataset = train_dataset.shuffle(BUFFER_SIZE).cache().batch(BATCH_SIZE).repeat()
  train_dataset.prefetch(buffer_size=tf.data.experimental.AUTOTUNE)
  test_dataset = orig_test_dataset.batch(BATCH_SIZE)
  
  
  
  
  return train_dataset, test_dataset, train_original_mask[0], train_original_img[0], orig_test_dataset, n_train, n_test
```

</div>

<div class="cell markdown">

Defining the functions needed for the PSPNet module.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Function for the pooling module which takes the output of ResNet50 as input as well as its width and height and pool it with a factor.
def pool_block(cur_tensor,
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

# Function for formatting the resnet model to a modified one which takes advantage of dilation rates instead of strides in the final blocks.

def modify_ResNet_Dilation(model):
  for i in range(0,4):
    model.get_layer('conv4_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv4_block1_{}_conv'.format(i)).dilation_rate = 2
    model.get_layer('conv5_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv5_block1_{}_conv'.format(i)).dilation_rate = 4
  model.save('/tmp/my_model')
  new_model = tf.keras.models.load_model('/tmp/my_model')
  return new_model


# Function for creating the PSPNet model. The inputs is the number of classes to classify, number of filters to use, kernel_size, activation function, 
# input image width and height and a boolean for knowing if the module is part of the ICNet or not.
def PSPNet(n_classes: int,
           n_filters: int,
           kernel_size: tuple,
           activation: str,
           image_width: int,
           image_height: int,
           isICNet: bool = False,
           dropout: bool = True,
           bn: bool = True
          ):
  if isICNet:
    input_shape=(None, None, 3)
  else:
    input_shape=(image_height,image_width,3)
  encoder=ResNet50(include_top=False, weights='imagenet', input_shape=input_shape)
  encoder=modify_ResNet_Dilation(encoder)
  #encoder.trainable=False
  resnet_output=encoder.output
  pooling_layer=[]
  pooling_layer.append(resnet_output)
  output=(resnet_output)
  h = image_height//8
  w = image_width//8
  for i in [1,2,3,6]:
    pool = pool_block(output, h, w, i, activation)
    pooling_layer.append(pool)
  concat=Concatenate()(pooling_layer)
  output_layer=Conv2D(filters=n_classes, kernel_size=(1,1), padding='same')(concat)
  final_layer=UpSampling2D(size=(8,8), data_format='channels_last', interpolation='bilinear')(output_layer)
  final_model=tf.keras.models.Model(inputs=encoder.input, outputs=final_layer)
  return final_model

#model = PSPNet(3, 16, (3,3), 'relu', 128,128)
```

</div>

<div class="cell markdown">

Defining the functions needed for the ICNet.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Function for adding stage 4 and 5 of ResNet50 to the 1/4 image size branch of the ICNet.
def PSP_rest(input_prev: tf.Tensor):

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
def CFF(stage: int, F_small, F_large, n_classes: int, input_width_small: int, input_height_small: int):
  F_up = tf.keras.layers.experimental.preprocessing.Resizing(int(input_width_small*2), int(input_height_small*2), interpolation="bilinear", name="Upsample_x2_small_{}".format(stage))(F_small)
  F_aux = Conv2D(n_classes, 1, name="CC_{}".format(stage), activation='softmax')(F_up)
  intermediate_f_small = Conv2D(128, 3, dilation_rate=2, padding='same', name="intermediate_f_small_{}".format(stage))(F_up)
  intermediate_f_small_bn = BatchNormalization(name="intermediate_f_small_bn_{}".format(stage))(intermediate_f_small)
  intermediate_f_large = Conv2D(128, 1, padding='same', name="intermediate_f_large_{}".format(stage))(F_large)
  intermediate_f_large_bn = BatchNormalization(name="intermediate_f_large_bn_{}".format(stage))(intermediate_f_large)
  intermediate_f_sum = Add(name="add_intermediates_{}".format(stage))([intermediate_f_small_bn,intermediate_f_large_bn])
  intermediate_f_relu = Activation('relu', name="activation_CFF_{}".format(stage))(intermediate_f_sum)
  return F_aux, intermediate_f_relu

# Function for the high-res branch of ICNet where image is in scale 1:1. The inputs are the input image, number of filters, kernel size and desired activation function.
def ICNet_1(input_obj,
           n_filters: int,
           kernel_size: tuple,
           activation: str):
  temp=input_obj
  for i in range(1,4):
    conv1=Conv2D(filters=n_filters*2*i, kernel_size=kernel_size, strides=(2,2), padding='same')(temp)
    batch_norm1=BatchNormalization()(conv1)
    temp=Activation(activation)(batch_norm1)
  return temp  

# Function for creating the ICNet model. The inputs are the width and height of the images to be used by the model, number of classes, number of filters, kernel size and
# desired activation function. 
def ICNet(image_width: int,
         image_height: int,
         n_classes: int,
         n_filters: int = 16,
         kernel_size: tuple = (3,3),
         activation: str = 'relu'):
  input_shape=[image_width,image_height,3]
  input_obj = tf.keras.Input(shape=input_shape, name="input_img_1")
  input_obj_4 = tf.keras.layers.experimental.preprocessing.Resizing(
    image_width//4, image_height//4, interpolation="bilinear", name="input_img_4")(input_obj)
  input_obj_2 = tf.keras.layers.experimental.preprocessing.Resizing(
    image_width//2, image_height//2, interpolation="bilinear", name="input_img_2")(input_obj)
  ICNet_Model1=ICNet_1(input_obj, n_filters, kernel_size, activation)
  PSPModel = PSPNet(n_classes, n_filters, kernel_size, activation, image_width//4, image_height//4, True)
  last_layer = PSPModel.get_layer('conv4_block3_out').output
  PSPModel_2_4 = tf.keras.models.Model(inputs=PSPModel.input, outputs=last_layer, name="JointResNet_2_4")
  ICNet_Model4 = PSPModel_2_4(input_obj_4)
  ICNet_Model2 = PSPModel_2_4(input_obj_2) 
  ICNet_4_rest = PSP_rest(ICNet_Model4)
  out1, last_layer = CFF(1, ICNet_4_rest, ICNet_Model2, n_classes, image_width//32, image_height//32)
  out2, last_layer = CFF(2, last_layer, ICNet_Model1, n_classes, image_width//16, image_height//16)
  upsample_2 = UpSampling2D(2, interpolation='bilinear', name="Upsampling_final_prediction")(last_layer)
  output = Conv2D(n_classes, 1, name="CC_fin", activation='softmax')(upsample_2)
  final_output = UpSampling2D(4, interpolation='bilinear', name='final_output')(output)
  final_model = tf.keras.models.Model(inputs=input_obj, outputs=[out1, out2, output, final_output])
  return final_model

```

</div>

<div class="cell markdown">

Below we define a function to be called by the horovod instance which creates the dataset depending on the amount of workers as well as:

Compiling the model with optimizer *adam*, loss function *SparseCategoricalCrossentropy* and metrics *SparseCategoricalAccuracy*. We also add loss weights 0.1, 0.3 and 0.6 to the lower resolution output, medium resolution output and high resolution output respectively.

MLFlow is initialized to keep track of the experiments.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def train_hvd(learning_rate=1.0, batch_size:int =64, buffer_size:int=1000):

  # Initialize Horovod
  hvd.init()

  # Pin GPU to be used to process local rank (one GPU per process)
  # These steps are skipped on a CPU cluster
  gpus = tf.config.experimental.list_physical_devices('GPU')
  for gpu in gpus:
    tf.config.experimental.set_memory_growth(gpu, True)
  if gpus:
    tf.config.experimental.set_visible_devices(gpus[hvd.local_rank()], 'GPU')
    
  # Including MLflow
  import mlflow
  import mlflow.tensorflow
  import os

  # Configure Databricks MLflow environment
  #    This is my (denny.lee) personal token so you will want to generate yours
  mlflow.set_tracking_uri("databricks")
  os.environ['DATABRICKS_HOST'] = databricks_host
  os.environ['DATABRICKS_TOKEN'] = databricks_token
  mlflow.set_experiment("/scalable-data-science/000_0-sds-3-x-projects/voluntary-student-project-01_group-DDLInMining/04_ICNet_Function_hvd")

  # Call the get_dataset function you created, this time with the Horovod rank and size
  train_dataset, test_dataset, sample_mask, sample_image, orig_test_dataset, n_train, n_test = create_datasets_hvd(128,128, batch_size, buffer_size, hvd.rank(), hvd.size())
  model = ICNet(128,128,3)
  
  STEPS_PER_EPOCH = n_train // batch_size

  EPOCHS = 20
  VAL_SUBSPLITS = 5
  VALIDATION_STEPS = n_test//batch_size//VAL_SUBSPLITS
  
  # Adjust learning rate based on number of GPUs
  optimizer = tfa.optimizers.AdamW(lr=learning_rate * hvd.size(), weight_decay=0.0001)

  # Use the Horovod Distributed Optimizer
  optimizer = hvd.DistributedOptimizer(optimizer)

  model.compile(optimizer=optimizer,
              loss=tf.keras.losses.SparseCategoricalCrossentropy(), loss_weights=[0.4,0.4,1,0],
              metrics="acc")

  # Create a callback to broadcast the initial variable states from rank 0 to all other processes.
  # This is required to ensure consistent initialization of all workers when training is started with random weights or restored from a checkpoint.
  callbacks = [
      hvd.callbacks.BroadcastGlobalVariablesCallback(0)
  ]

  # Save checkpoints only on worker 0 to prevent conflicts between workers
  if hvd.rank() == 0:
      callbacks.append(tf.keras.callbacks.ModelCheckpoint(checkpoint_dir + '/checkpoint-{epoch}.ckpt', save_weights_only = True, monitor='val_final_output_loss', save_best_only=True))
      mlflow.tensorflow.autolog(every_n_iter=1)
        
  model_history =  model.fit(train_dataset, epochs=EPOCHS, steps_per_epoch=STEPS_PER_EPOCH, 
                           validation_steps=VALIDATION_STEPS, validation_data=test_dataset, callbacks=callbacks)
  
      
  
```

</div>

<div class="cell markdown">

Finally, we fit the model to the Oxford dataset.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from sparkdl import HorovodRunner

hr = HorovodRunner(np=2)
hr.run(train_hvd, learning_rate=0.001)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
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

epochs = range(16)

plt.figure(figsize=(20,3))
plt.subplot(1,4,1)
plt.plot(epochs, loss, 'r', label='Training loss')
plt.plot(epochs, val_loss, 'b', label='Validation loss')
plt.title('Training and Validation Loss')
plt.xlabel('Epoch')
plt.ylabel('Loss Value')
plt.legend()
plt.subplot(1,4,2)
plt.plot(epochs, acc, 'r', label="Training accuracy")
plt.plot(epochs, val_acc4, 'b', label="Validation accuracy")
plt.xlabel('Epoch')
plt.ylabel('Accuracy')
plt.legend()
plt.subplot(1,4,3)
plt.plot(epochs, val_loss1, 'b', label="Loss output 1")
plt.plot(epochs, val_loss2, 'g', label="Loss output 2")
plt.plot(epochs, val_loss3, 'y', label="Loss output 3")
plt.plot(epochs, val_loss4, 'y', label="Loss output 4")
plt.legend()
plt.subplot(1,4,4)
plt.plot(epochs, val_acc1, 'b', label="Acc output 1")
plt.plot(epochs, val_acc2, 'g', label="Acc output 2")
plt.plot(epochs, val_acc3, 'y', label="Acc output 3")
plt.plot(epochs, val_acc4, 'y', label="Acc output 4")
plt.legend()
plt.show()
```

</div>

<div class="cell markdown">

Finally, we visualize some predictions on the test dataset.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
show_predictions(orig_test_dataset, 20, 3)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_1.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_2.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_3.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_4.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_5.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_6.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_7.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_8.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_9.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_04_10.png?raw=true)

</div>
