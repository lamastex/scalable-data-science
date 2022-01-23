<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Implementation of [ICNet](https://arxiv.org/pdf/1704.08545.pdf)
===============================================================

In this notebook, an implementation of [ICNet](https://arxiv.org/pdf/1704.08545.pdf) is presented which is an architecture whitch uses a trade-off between complexity and inference time efficiently. The architecture is evaluated against the Oxford pets dataset.

</div>

<div class="cell markdown">

Below, functions for data manipulation are defined, ensuring that the images inputted to the model is of appropriate format.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from tensorflow.keras.layers import *
from tensorflow.keras.models import *
import numpy as np

# Function for normalizing image_size so that pixel intensity is between 0 and 1
def normalize(input_image, input_mask):
  input_image = tf.cast(input_image, tf.float32) / 255.0
  input_mask -= 1
  return input_image, input_mask

# Function for resizing the train images to the desired input shape of 128x128 as well as augmenting the training images
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

# Function for resizing the test images to the desired output shape (no augmenation)
def load_image_test(datapoint):
  input_image = tf.image.resize(datapoint['image'], (128, 128))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (128, 128))

  input_image, input_mask = normalize(input_image, input_mask)

  return input_image, input_mask

def load_image_train_noTf(datapoint):
  input_image = tf.image.resize(datapoint['image'], (128, 128))
  input_mask = tf.image.resize(datapoint['segmentation_mask'], (128, 128))

  if tf.random.uniform(()) > 0.5:
    input_image = tf.image.flip_left_right(input_image)
    input_mask = tf.image.flip_left_right(input_mask)

  input_image, input_mask = normalize(input_image, input_mask)
  input_mask = tf.math.round(input_mask)

  return input_image, input_mask

# Function for resizing the image to the desired size of factor 2 or 4 to be inputted to the ICNet architecture
def resize_image16(img, mask):
  input_image = tf.image.resize(img, (128//16, 128//16))
  input_mask=tf.image.resize(mask, (128//16, 128//16))
  input_mask = tf.math.round(input_mask)
  return input_image, input_mask

def resize_image8(img, mask):
  input_image = tf.image.resize(img, (128//8, 128//8))
  input_mask=tf.image.resize(mask, (128//8, 128//8))
  input_mask = tf.math.round(input_mask)
  return input_image, input_mask

def resize_image4(img, mask):
  input_image = tf.image.resize(img, (128//4, 128//4))
  input_mask=tf.image.resize(mask, (128//4, 128//4))
  input_mask = tf.math.round(input_mask)
  return input_image, input_mask

```

</div>

<div class="cell markdown">

Here the data is loaded from Tensorflow datasets.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import tensorflow_datasets as tfds

dataset, info = tfds.load('oxford_iiit_pet:3.*.*', with_info=True)

TRAIN_LENGTH = info.splits['train'].num_examples
BATCH_SIZE = 114
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH // BATCH_SIZE

#train = dataset['train'].map(load_image_train, num_parallel_calls=tf.data.experimental.AUTOTUNE)
#train16 = dataset['train'].map(resize_image16, num_parallel_calls=tf.data.experimental.AUTOTUNE)
#train8 = dataset['train'].map(resize_image8, num_parallel_calls=tf.data.experimental.AUTOTUNE)
#train4 = dataset['train'].map(resize_image4, num_parallel_calls=tf.data.experimental.AUTOTUNE)
#test = dataset['test'].map(load_image_test)

#train_dataset = train.cache().shuffle(BUFFER_SIZE).batch(BATCH_SIZE).repeat()
#train_dataset = train_dataset.prefetch(buffer_size=tf.data.experimental.AUTOTUNE)
#test_dataset = test.batch(BATCH_SIZE)

train_orig = np.ndarray(shape=(3680,128,128,3))
train_orig_mask = np.ndarray(shape=(3680,128,128,1))
train16_mask = np.ndarray(shape=(3680,8,8,1))
train8_mask = np.ndarray(shape=(3680,16,16,1))
train4_mask = np.ndarray(shape=(3680,32,32,1))

count = 0
for datapoint in dataset['train']:
  img_orig, mask_orig = load_image_train_noTf(datapoint)
  train_orig[count]=img_orig
  train_orig_mask[count]=(mask_orig)
  img, mask = resize_image16(img_orig, mask_orig)
  train16_mask[count]=(mask)
  img, mask = resize_image8(img_orig, mask_orig)
  train8_mask[count]=(mask)
  img, mask = resize_image4(img_orig, mask_orig)
  train4_mask[count]=(mask)
  count+=1
  
test_orig = np.ndarray(shape=(3669,128,128,3))
test_orig_mask = np.ndarray(shape=(3669,128,128,1))
test_orig_img = np.ndarray(shape=(3669,128,128,3))
test16_mask = np.ndarray(shape=(3669,8,8,1))
test16_img = np.ndarray(shape=(3669,8,8,3))
test8_mask = np.ndarray(shape=(3669,16,16,1))
test8_img = np.ndarray(shape=(3669,16,16,3))
test4_mask = np.ndarray(shape=(3669,32,32,1))
test4_img = np.ndarray(shape=(3669,32,32,3))

count=0
for datapoint in dataset['test']:
  img_orig, mask_orig = load_image_test(datapoint)
  test_orig[count]=(img_orig)
  test_orig_mask[count]=(mask_orig)
  img, mask = resize_image16(img_orig, mask_orig)
  test16_mask[count]=(mask)
  test16_img[count]=(img)
  img, mask = resize_image8(img_orig, mask_orig)
  test8_mask[count]=(mask)
  test8_img[count]=(img)
  img, mask = resize_image4(img_orig, mask_orig)
  test4_mask[count]=(mask)
  test4_img[count]=(img)
  count+=1

  
def display(display_list):
  plt.figure(figsize=(15, 15))

  title = ['Input Image', 'True Mask', 'Predicted Mask']

  for i in range(len(display_list)):
    plt.subplot(1, len(display_list), i+1)
    plt.title(title[i])
    plt.imshow(tf.keras.preprocessing.image.array_to_img(display_list[i]))
    plt.axis('off')
  plt.show()

sample_image, sample_mask = train_orig[0], train_orig_mask[0]
display([sample_image, sample_mask])
#for image, mask in train.take(1):
#  sample_image, sample_mask = image, mask
#display([sample_image, sample_mask])

#Keep shape with (Batch_SIZE, height,width, channels)
#in either np.array or try datasets.
#train = tfds.as_numpy(dataset['train']['image'])
#train16 = tfds.as_numpy(train16)
#train8 = tfds.as_numpy(train8)
#train4 = tfds.as_numpy(train4)
#truth16 = np.concatenate([y for x, y in train16], axis=0)
#truth8 = np.concatenate([y for x, y in train8], axis=0)
#truth4 = np.concatenate([y for x, y in train4], axis=0)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_1.png?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
test_dataset = dataset['test'].map(load_image_test)
test_dataset = test.batch(BATCH_SIZE)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print(test8_mask)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
train_orig = np.split(train_orig[0:3648], 114)
print("Finshed 1")
train16_mask = np.split(train16_mask[0:3648], 114)
train8_mask = np.split(train8_mask[0:3648], 114)
train4_mask = np.split(train4_mask[0:3648], 114)

test_orig = np.split(test_orig[0:3648], 114)
print("Finshed 1")
test16_mask = np.split(test16_mask[0:3648], 114)
test8_mask = np.split(test8_mask[0:3648], 114)
test4_mask = np.split(test4_mask[0:3648], 114)
test16_img = np.split(test16_img[0:3648], 114)
test8_img = np.split(test8_img[0:3648], 114)
test4_img = np.split(test4_img[0:3648], 114)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print(test16_mask)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
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
  
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import tensorflow as tf
from tensorflow.keras.applications.resnet50 import ResNet50

# Function for formatting the resnet model to a modified one which takes advantage of dilation rates instead of strides in the final blocks
def modify_ResNet_Dilation(model):
  for i in range(0,4):
    model.get_layer('conv4_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv4_block1_{}_conv'.format(i)).dilation_rate = 2
    model.get_layer('conv5_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv5_block1_{}_conv'.format(i)).dilation_rate = 4
  new_model = model_from_json(model.to_json())
  return new_model

def PSPNet(num_classes: int,
           n_filters: int,
           kernel_size: tuple,
           activation: str,
           image_width: int,
           image_height: int,
           isICNet: bool = False
          ):
  if isICNet:
    input_shape=(None, None, 3)
  else:
    input_shape=(image_height,image_width,3)
  encoder=ResNet50(include_top=False, weights='imagenet', input_shape=input_shape)
  encoder=modify_ResNet_Dilation(encoder)
  encoder.trainable=False
  resnet_output=encoder.output
  pooling_layer=[]
  pooling_layer.append(resnet_output)
  output=Dropout(rate=0.5)(resnet_output)
  h = image_height//8
  w = image_width//8
  for i in [1,2,3,6]:
    pool = pool_block(output, h, w, i, activation)
    pooling_layer.append(pool)
  concat=Concatenate()(pooling_layer)
  output_layer=Conv2D(filters=num_classes, kernel_size=(1,1), padding='same')(concat)
  final_layer=UpSampling2D(size=(8,8), data_format='channels_last', interpolation='bilinear')(output_layer)
  final_model=tf.keras.models.Model(inputs=encoder.input, outputs=final_layer)
  return final_model

PSP = PSPNet(3, 16, (3,3), 'relu', 128,128)
PSP.summary()
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def PSP_rest(input_prev: tf.Tensor):

  y_ = input_prev
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
        
```

</div>

<div class="cell markdown">

Method for performing cascade feature fusion. See https://arxiv.org/pdf/1704.08545.pdf

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def CFF(stage: int, F_small, F_large, n_classes: int, input_width_small: int, input_height_small: int):
  F_up = tf.keras.layers.experimental.preprocessing.Resizing(int(input_width_small*2), int(input_height_small*2), interpolation="bilinear", name="Upsample_x2_small_{}".format(stage))(F_small)
  F_aux = Conv2D(n_classes, 1, name="ClassifierConv_{}".format(stage), activation='softmax')(F_up)
  #y = ZeroPadding2D(padding=2, name='padding17')(F_up) ?? behövs denna?
  intermediate_f_small = Conv2D(128, 3, dilation_rate=2, padding='same', name="intermediate_f_small_{}".format(stage))(F_up)
  print(intermediate_f_small)
  intermediate_f_small_bn = BatchNormalization(name="intermediate_f_small_bn_{}".format(stage))(intermediate_f_small)
  intermediate_f_large = Conv2D(128, 1, padding='same', name="intermediate_f_large_{}".format(stage))(F_large)
  intermediate_f_large_bn = BatchNormalization(name="intermediate_f_large_bn_{}".format(stage))(intermediate_f_large)
  intermediate_f_sum = Add(name="add_intermediates_{}".format(stage))([intermediate_f_small_bn,intermediate_f_large_bn])
  intermediate_f_relu = Activation('relu', name="activation_CFF_{}".format(stage))(intermediate_f_sum)
  return F_aux, intermediate_f_relu
                
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
#%sh sudo apt-get install -y graphviz
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def ICNet_1(input_obj: tf.keras.Input,
           n_filters: int,
           kernel_size: tuple,
           activation: str):
  temp=input_obj
  for i in range(1,4):
    # Dropout layer on the hidden units, i.e. not on the input layer
    if i == 2 or i == 3:
      temp=Dropout(rate=0.5)(temp)
    conv1=Conv2D(filters=n_filters*2*i, kernel_size=kernel_size, strides=(2,2), padding='same')(temp)
    batch_norm1=BatchNormalization()(conv1)
    temp=Activation(activation)(batch_norm1)
  return temp  

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
  output = Conv2D(n_classes, 1, name="ClassifierConv_final_prediction", activation='softmax')(upsample_2)
  final_model = tf.keras.models.Model(inputs=input_obj, outputs=[out1, out2, output])
  return final_model

model=ICNet(128,128,3)
model.summary()
#final_model=tf.keras.models.Model(inputs=input_obj ,outputs=model)
#final_model.summary()
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from IPython.display import display as Display, Image
import matplotlib.pyplot as plt
import matplotlib.image as mpimg

tf.keras.utils.plot_model(model, to_file='/dbfs/FileStore/my_model.jpg', show_shapes=True)
img = mpimg.imread('/dbfs/FileStore/my_model.jpg')
plt.figure(figsize=(200,200))
imgplot = plt.imshow(img)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_2.png?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls /dbfs/FileStore
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
#import datetime


model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(), loss_weights=[0.1,0.3,0.6],
              metrics=tf.keras.metrics.SparseCategoricalAccuracy())

#log_dir = "logs/fit/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
#tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir=log_dir, histogram_freq=1)

def create_mask(pred_mask):
  pred_mask = tf.argmax(pred_mask, axis=-1)
  pred_mask = pred_mask[..., tf.newaxis]
  return pred_mask[0]

def show_predictions(dataset=None, num=1):
  if dataset:
    for image, mask in dataset.take(num):
      print(image)
      pred_mask = model.predict(image)[2]
      display([image[0], mask[0], create_mask(pred_mask)])
  else:
    display([sample_image, sample_mask,
             create_mask(model.predict(sample_image[tf.newaxis, ...])[2])])
    
show_predictions()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_3.png?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import mlflow.tensorflow
mlflow.tensorflow.autolog()
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def batch_generator(X, Y16, Y8, Y4, batch_size = BATCH_SIZE):
  indices = np.arange(len(X)) 
  batch=[]
  while True:
  # it might be a good idea to shuffle your data before each epoch
    np.random.shuffle(indices) 
    for i in indices:
      batch.append(i)
      if len(batch)==batch_size:
        yield X[batch], {'ClassifierConv_1': Y16[batch], 'ClassifierConv_2': Y8[batch], 'ClassifierConv_final_prediction': Y4[batch]}
        batch=[]
        
def batch_generator_eval(X, Y16, Y8, Y4, batch_size = BATCH_SIZE):
  indices = np.arange(len(X)) 
  batch=[]
  while True:
    for i in indices:
      batch.append(i)
      if len(batch)==batch_size:
        yield X[batch], {'ClassifierConv_1': Y16[batch], 'ClassifierConv_2': Y8[batch], 'ClassifierConv_final_prediction': Y4[batch]}
        batch=[]

class DisplayCallback(tf.keras.callbacks.Callback):
  def on_epoch_end(self, epoch, logs=None):
    show_predictions()

res_eval_1 = []
class MyCustomCallback(tf.keras.callbacks.Callback):
    def on_epoch_end(self, epoch, logs=None):
        res_eval_1.append(self.model.evaluate(test_orig, [test16_mask, test8_mask, test4_mask], batch_size=45, verbose=1))
        show_predictions()
        
TRAIN_LENGTH = info.splits['train'].num_examples
BATCH_SIZE = 64
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH//BATCH_SIZE

EPOCHS = 10
VAL_SUBSPLITS = 5
VALIDATION_STEPS = info.splits['test'].num_examples//BATCH_SIZE

train_generator = batch_generator(train_orig,train16_mask,train8_mask,train4_mask,batch_size=BATCH_SIZE)
eval_generator = batch_generator_eval(test_orig, test16_mask, test8_mask, test4_mask, batch_size=BATCH_SIZE)
model_history =  model.fit(train_generator, epochs=EPOCHS,steps_per_epoch=STEPS_PER_EPOCH,
                                     callbacks=[MyCustomCallback()],verbose=1)                        
#model_history = model.fit(x=train_orig, y=[train16_mask, train8_mask, train4_mask],
#                          epochs=EPOCHS,
#                          steps_per_epoch=STEPS_PER_EPOCH,
#                          callbacks=[MyCustomCallback()], verbose=1)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_4.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_5.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_6.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_7.png?raw=true)<br> ![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_8.png?raw=true)<br>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print(res_eval_1)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
loss = model_history.history['loss']
acc = model_history.history['ClassifierConv_final_prediction_sparse_categorical_accuracy']
val_loss = []
val_acc = []
val_loss1 = []
val_loss2 = []
val_loss3 = []
val_acc1 = []
val_acc2 = []
for i in range(EPOCHS):
  val_loss.append(res_eval_1[i][0])
  val_loss1.append(res_eval_1[i][1])
  val_loss2.append(res_eval_1[i][2])
  val_loss3.append(res_eval_1[i][3])
  val_acc.append(res_eval_1[i][6])
  val_acc1.append(res_eval_1[i][4])
  val_acc2.append(res_eval_1[i][5])

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
plt.plot(epochs, val_acc, 'bo', label="Validation accuracy")
plt.xlabel('Epoch')
plt.ylabel('Accuracy')
plt.legend()
plt.subplot(1,4,3)
plt.plot(epochs, val_loss1, 'b', label="Loss output 1")
plt.plot(epochs, val_loss2, 'g', label="Loss output 2")
plt.plot(epochs, val_loss3, 'y', label="Loss output 3")
plt.legend()
plt.subplot(1,4,4)
plt.plot(epochs, val_acc1, 'b', label="Acc output 1")
plt.plot(epochs, val_acc2, 'g', label="Acc output 2")
plt.plot(epochs, val_acc, 'y', label="Acc output 3")
plt.legend()
plt.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_09_9.png?raw=true)<br>

</div>
