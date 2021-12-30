# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC # U-Net model for image segmentation
# MAGIC 
# MAGIC This is a modified version of Tensorflows tutorial regarding image segmentation which can be found [here](https://www.tensorflow.org/tutorials/images/segmentation). Using a modified [U-Net](https://arxiv.org/abs/1505.04597) approach, with a [VGG16](https://arxiv.org/abs/1409.1556) as the encoder and then using traditional Conv2DTranspose layers for upsampling the dimensions. After 1 epoch a validation accuracy of 84.5 % was achieved on the [Oxford Pets Data Set](https://www.robots.ox.ac.uk/~vgg/data/pets/).

# COMMAND ----------

import tensorflow as tf
import tensorflow_datasets as tfds
import matplotlib.pyplot as plt
from IPython.display import clear_output

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
# MAGIC Now that the dataset has been loaded into memory, the model can further be defined.

# COMMAND ----------

from tensorflow.keras.layers import *
from tensorflow.keras.models import *
from tensorflow.keras.applications import VGG16

def encoder_VGG16(input_shape):
  base_model=VGG16(include_top=False, weights='imagenet', input_shape=input_shape)
  layers=[layer.output for layer in base_model.layers]
  base_model = tf.keras.Model(inputs=base_model.input, outputs=layers[-2])
  base_model.summary()
  
  x = []
  fourth_layer = base_model.get_layer('block1_conv1').output
  x.append(fourth_layer)
  third_layer = base_model.get_layer('block2_conv2').output
  x.append(third_layer)
  secondary_layer = base_model.get_layer('block3_conv3').output
  x.append(secondary_layer)
  last_layer = base_model.get_layer('block4_conv3').output
  x.append(last_layer)
  output_layer =  base_model.get_layer('block5_conv3').output
  x.append(output_layer)
  return base_model, x


# COMMAND ----------

# MAGIC %md
# MAGIC Here, the decoder part is defined where upsampling takes place to convert the encoded part to the same dimensions as the input image for height and width, with the same amount of channels as there are classes.

# COMMAND ----------

def unet(image_width: int,
         image_heigth: int,
         n_channels: int,
         n_depth: int,
         n_classes: int):
  #if n_depth<1 or n_depth>5: #+ add more cases
   # raise Exception("Unsupported number of layers/upsamples")
  input_shape = [image_heigth, image_width, n_channels]
  encoded_model, x = encoder_VGG16(input_shape)
  encoded_model.trainable=False
  intermediate_model = x[n_depth-1]
  intermediate_model = tf.keras.layers.Dropout(0.5)(intermediate_model)
  for i in reversed(range(0,n_depth-1)):
    next_filters = x[i+1].shape[3]/2
    intermediate_model = Conv2DTranspose(filters=next_filters ,kernel_size=3,strides=2,padding='same')(intermediate_model)
    intermediate_model = tf.keras.layers.Concatenate()([intermediate_model,x[i]])
    intermediate_model = tf.keras.layers.BatchNormalization()(intermediate_model)
    intermediate_model = tf.keras.layers.ReLU()(intermediate_model)
    intermediate_model = Conv2D(filters=next_filters, kernel_size=3, activation ='relu', padding='same')(intermediate_model)
    intermediate_model = Conv2D(filters=next_filters, kernel_size=3, activation ='relu', padding='same')(intermediate_model)
    
  outputs=Conv2D(filters=n_classes,kernel_size=(1,1),strides=(1),padding='same')(intermediate_model)
  x = Reshape((image_heigth*image_width, n_classes))(outputs)
  x = Activation(tf.nn.softmax)(x)
  outputs = Reshape((image_heigth,image_width, n_classes))(x)
  
  print(outputs.shape[2])
  final_model=tf.keras.models.Model(inputs=encoded_model.input ,outputs=[outputs])
  return(final_model)

shape=[128, 128, 3]
this_model = unet(shape[0],shape[1],shape[2],5,3)
this_model.summary()
this_model.outputs

# COMMAND ----------

# MAGIC %md
# MAGIC The model is then compiled.

# COMMAND ----------

this_model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['accuracy'])

def create_mask(pred_mask):
  pred_mask = tf.argmax(pred_mask, axis=-1)
  pred_mask = pred_mask[..., tf.newaxis]
  return pred_mask[0]

def show_predictions(dataset=None, num=1):
  if dataset:
    for image, mask in dataset.take(num):
      pred_mask = this_model.predict(image)
      display([image[0], mask[0], create_mask(pred_mask)])
  else:
    display([sample_image, sample_mask,
             create_mask(this_model.predict(sample_image[tf.newaxis, ...]))])
    
show_predictions()

# COMMAND ----------

# MAGIC %md
# MAGIC Below, the model is fitted against the training data and validated on the validation set after each epoch. A validation accuracy of 84.5 % is achieved after one epoch.

# COMMAND ----------

TRAIN_LENGTH = info.splits['train'].num_examples
BATCH_SIZE = 64
BUFFER_SIZE = 1000
STEPS_PER_EPOCH = TRAIN_LENGTH // BATCH_SIZE

EPOCHS = 20
VAL_SUBSPLITS = 5
VALIDATION_STEPS = info.splits['test'].num_examples//BATCH_SIZE//VAL_SUBSPLITS

model_history = this_model.fit(train_dataset, epochs=EPOCHS,
                          steps_per_epoch=STEPS_PER_EPOCH,
                          validation_steps=VALIDATION_STEPS,
                          validation_data=test_dataset)


#scores = model_history.evaluate(X_test, y_test, verbose=2)


# COMMAND ----------

show_predictions(test_dataset,num=10)

# COMMAND ----------

loss = model_history.history['loss']
val_loss = model_history.history['val_loss']

epochs = range(EPOCHS)

plt.figure()
plt.plot(epochs, loss, 'r', label='Training loss')
plt.plot(epochs, val_loss, 'bo', label='Validation loss')
plt.title('Training and Validation Loss')
plt.xlabel('Epoch')
plt.ylabel('Loss Value')
plt.ylim([0, 1])
plt.legend()
plt.show()

# COMMAND ----------

