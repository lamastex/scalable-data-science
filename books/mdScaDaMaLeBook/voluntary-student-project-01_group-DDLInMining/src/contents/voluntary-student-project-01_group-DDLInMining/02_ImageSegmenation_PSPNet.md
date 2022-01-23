<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Implementation of [PSPNet](https://arxiv.org/pdf/1612.01105.pdf)
================================================================

In this notebook, an implementation of [PSPNet](https://arxiv.org/pdf/1612.01105.pdf) is presented which is an architecture which uses scene parsing and evaluates the images at different scales and finally combines the different results to form a final prediction. The architecture is evaluated against the [Oxford-IIIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/). This notebook has reused material from the [Image Segmentation Tutorial](https://www.tensorflow.org/tutorials/images/segmentation) on Tensorflow for loading the dataset and showing predictions.

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
import tensorflow_datasets as tfds
from tensorflow.keras.applications.resnet50 import ResNet50
```

</div>

<div class="cell markdown">

Defining functions for normalizing and transforming the images.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
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
```

</div>

<div class="cell markdown">

Loading the datasets to memory and displaying an example of an image and an image mask.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
dataset, info = tfds.load('oxford_iiit_pet:3.*.*', with_info=True)

TRAIN_LENGTH = info.splits['train'].num_examples
BATCH_SIZE = 64
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH // BATCH_SIZE

train = dataset['train'].map(load_image_train, num_parallel_calls=tf.data.experimental.AUTOTUNE)
test = dataset['test'].map(load_image_test)

train_dataset = train.shuffle(BUFFER_SIZE).cache().batch(BATCH_SIZE).repeat()
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
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_1.png?raw=true)

</div>

<div class="cell markdown">

Defining the functions needed for the PSPNet.

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

# Function for formatting the resnet model to a modified one which takes advantage of dilation rates instead of strides in the final blocks
def modify_ResNet_Dilation(model):
  for i in range(0,4):
    model.get_layer('conv4_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv4_block1_{}_conv'.format(i)).dilation_rate = 2
    model.get_layer('conv5_block1_{}_conv'.format(i)).strides = 1
    model.get_layer('conv5_block1_{}_conv'.format(i)).dilation_rate = 4
  model.save('/tmp/my_model')
  new_model = tf.keras.models.load_model('/tmp/my_model')
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
  #encoder.trainable=False
  resnet_output=encoder.output
  pooling_layer=[]
  pooling_layer.append(resnet_output)
  #output=Dropout(rate=0.5)(resnet_output)
  output=resnet_output
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
```

</div>

<div class="cell markdown">

Creating the PSPModel with three classes, 16 filters, kernel size of (3,3), 'relu' as the activation function and with image height and width of 128 pixels.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
PSP = PSPNet(3, 16, (3,3), 'relu', 128,128)
```

</div>

<div class="cell markdown">

And here is the model summary.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
PSP.summary()
```

</div>

<div class="cell markdown">

Compiling the model.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
PSP.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['accuracy'])
```

</div>

<div class="cell markdown">

Below, functions needed to show the model's predictions against the true mask are defined.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def create_mask(pred_mask):
  pred_mask = tf.argmax(pred_mask, axis=-1)
  pred_mask = pred_mask[..., tf.newaxis]
  return pred_mask[0]

def show_predictions(dataset=None, num=1):
  if dataset:
    for image, mask in dataset.take(num):
      print(image)
      pred_mask = model.predict(image)
      display([image[0], mask[0], create_mask(pred_mask)])
  else:
    display([sample_image, sample_mask,
             create_mask(PSP.predict(sample_image[tf.newaxis, ...]))])
    
show_predictions()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_2.png?raw=true)

</div>

<div class="cell markdown">

A custom callback function is defined for showing how the model learns to predict while training.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
class DisplayCallback(tf.keras.callbacks.Callback):
  def on_epoch_end(self, epoch, logs=None):
    show_predictions()
```

</div>

<div class="cell markdown">

And finally the model is fitted against the training dataset and validated against the test dataset. .

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
TRAIN_LENGTH = info.splits['train'].num_examples
BATCH_SIZE = 64
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH // BATCH_SIZE

EPOCHS = 20
VAL_SUBSPLITS = 5
VALIDATION_STEPS = info.splits['test'].num_examples//BATCH_SIZE//VAL_SUBSPLITS

model_history = PSP.fit(train_dataset, epochs=EPOCHS,
                          steps_per_epoch=STEPS_PER_EPOCH,
                          validation_steps=VALIDATION_STEPS,
                          validation_data=test_dataset,
                          callbacks=[DisplayCallback()])                    
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_3.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_4.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_5.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_6.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_7.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_8.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_9.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_10.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_11.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_12.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_13.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_14.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_15.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_16.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_17.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_18.png?raw=true)

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_19.png?raw=true)

</div>

<div class="cell markdown">

The losses and accuracies of each epoch is plotted to visualize the performance of the model.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
loss = model_history.history['loss']
acc = model_history.history['accuracy']
val_loss = model_history.history['val_loss']
val_acc = model_history.history['val_accuracy']

epochs = range(EPOCHS)

plt.figure(figsize=(10,3))
plt.subplot(1,2,1)
plt.plot(epochs, loss, 'r', label='Training loss')
plt.plot(epochs, val_loss, 'b', label='Validation loss')
plt.ylim(0,1)
plt.title('Training and Validation Loss')
plt.xlabel('Epoch')
plt.ylabel('Loss Value')
plt.legend()
plt.subplot(1,2,2)
plt.plot(epochs, acc, 'r', label="Training accuracy")
plt.plot(epochs, val_acc, 'b', label="Validation accuracy")
plt.title('Training and Validation Accuracy')
plt.xlabel('Epoch')
plt.ylabel('Accuracy')
plt.legend()
plt.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/v_02_20.JPG?raw=true)

</div>
