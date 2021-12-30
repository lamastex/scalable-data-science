# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC ## CNN for Intel Image Classification
# MAGIC 
# MAGIC We will now implement and test the MixUp preprocessing method for a slightly harder CNN example, the Intel Image Classification data set. Again, this notebook runs on CPUs, but the hyperparameter search is scalable.

# COMMAND ----------

# Imports
import tensorflow as tf
import numpy as np
from tensorflow import keras
from tensorflow.keras.layers import Dense,Conv2D,Flatten,BatchNormalization,Dropout
from tensorflow.keras import Sequential
from ray import tune
from ray.tune import CLIReporter
from sklearn.metrics import confusion_matrix
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from functools import partial

# Fixes the issue "AttributeError: 'ConsoleBuffer has no attribute 'fileno'"
import sys
sys.stdout.fileno = lambda: False

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC We will use the Intel Image Classification data set [[3]]. It consists of 25k 150x150 RBG images from 6 different classes: buildings, forest, glacier, mountain, sea, or street. However when we load the data to our model we will rescale the images to 32x32 RBG images.
# MAGIC 
# MAGIC [3]: https://www.kaggle.com/puneet6060/intel-image-classification

# COMMAND ----------

"""
Global parameters for training.
"""

img_height,img_width,channels = 32,32,3
batch_size = 32
train_data_dir,test_data_dir = "/dbfs/FileStore/tables/Group20/seg_train/seg_train/", "dbfs/FileStore/tables/Group20/seg_test/seg_test/"
num_classes = 6
alpha = 0.2 # Degree of mixup is ~ Beta(alpha,alpha)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC To create MixUp data, we will define a custom data generator. It takes an underlying image generator as argument, and outputs convex combinations of two randomly selected (example,label) pairs drawn according to the underlying generator.
# MAGIC 
# MAGIC Note that, in order to speed up the data generators, we need to make the data more accessible. We do this by copying the data from the dbfs to the working directory. This is done with our copy_data function.

# COMMAND ----------

import os, shutil
def copy_data():
  src = "/dbfs/FileStore/tables/Group20/seg_train/seg_train"
  dst = os.path.join(os.getcwd(), 'seg_train')
  print("Copying data to working folder")
  shutil.copytree(src, dst)
  print("Done with copying!")
  train_data_dir = dst

  src = "/dbfs/FileStore/tables/Group20/seg_test/seg_test"
  dst = os.path.join(os.getcwd(), 'seg_test')
  print("Copying data to working folder")
  shutil.copytree(src, dst)
  print("Done with copying!")
  test_data_dir = dst

  return train_data_dir,test_data_dir

# COMMAND ----------

class MixupImageDataGenerator(tf.keras.utils.Sequence):
    def __init__(self, generator, directory, batch_size, img_height, img_width, alpha=0.2, subset=None):
        self.batch_size = batch_size
        self.batch_index = 0
        self.alpha = alpha

        # First iterator yielding tuples of (x, y)
        self.generator1 = generator.flow_from_directory(directory,
                                                        target_size=(
                                                            img_height, img_width),
                                                        class_mode="categorical",
                                                        batch_size=batch_size,
                                                        shuffle=True,
                                                        subset=subset)

        # Second iterator yielding tuples of (x, y)
        self.generator2 = generator.flow_from_directory(directory,
                                                        target_size=(
                                                            img_height, img_width),
                                                        class_mode="categorical",
                                                        batch_size=batch_size,
                                                        shuffle=True,
                                                        subset=subset)

        # Number of images across all classes in image directory.
        self.n = self.generator1.samples


    def __len__(self):
        # returns the number of batches
        return (self.n + self.batch_size - 1) // self.batch_size

    def __getitem__(self, index):
        # Get a pair of inputs and outputs from two iterators.
        X1, y1 = self.generator1.next()
        X2, y2 = self.generator2.next()


        # random sample the lambda value from beta distribution.
        l = np.random.beta(self.alpha, self.alpha, X1.shape[0])

        X_l = l.reshape(X1.shape[0], 1, 1, 1)
        y_l = l.reshape(X1.shape[0], 1)


        # Perform the mixup.
        X = X1 * X_l + X2 * (1 - X_l)
        y = y1 * y_l + y2 * (1 - y_l)
        return X, y

    def reset_index(self):
        """Reset the generator indexes array.
        """

        self.generator1._set_index_array()
        self.generator2._set_index_array()


    def on_epoch_end(self):
        self.reset_index()

# COMMAND ----------

"""
A method that gives us the different dataloaders that we need for training and validation.

With for_training set to True, the model gives us the dataloaders
* train_mix_loader: Gives us mixed data for training
* train_loader:     Gives us the unmixed training data
* val_mix_loader:   Gives us mixed validation data
* val_loader:       Gives us unmixed validation data

By setting for_training to False, the method gives us the dataloader
* test_loader: Unmixed and unshuffled dataloader for the testing data. The reason for not shuffeling the data is in order to simplify the validation process.
"""
def get_data_loaders(train_data_dir,test_data_dir,for_training = True):
  
    #For training data
    if for_training:
        datagen_train_val = ImageDataGenerator(rescale=1./255,
                                rotation_range=5,
                                width_shift_range=0.05,
                                height_shift_range=0,
                                shear_range=0.05,
                                zoom_range=0,
                                brightness_range=(1, 1.3),
                                horizontal_flip=True,
                                fill_mode='nearest',
                                validation_split=0.1)

        train_mix_loader = MixupImageDataGenerator(generator = datagen_train_val,
                                                   directory = train_data_dir,
                                                   batch_size = batch_size,
                                                   img_height = img_height,
                                                   img_width = img_width,
                                                   alpha=alpha,
                                                   subset="training")
        
        val_mix_loader = MixupImageDataGenerator(generator = datagen_train_val,
                                                 directory = train_data_dir,
                                                 batch_size = batch_size,
                                                 img_height = img_height,
                                                 img_width = img_width,
                                                 alpha=alpha,
                                                 subset="validation")

        train_loader = datagen_train_val.flow_from_directory(train_data_dir,
                                                        target_size=(img_height, img_width),
                                                        class_mode="categorical",
                                                        batch_size=batch_size,
                                                        shuffle=True,
                                                        subset="training")

        val_loader = datagen_train_val.flow_from_directory(train_data_dir,
                                                        target_size=(img_height, img_width),
                                                        class_mode="categorical",
                                                        batch_size=batch_size,
                                                        shuffle=True,
                                                        subset="validation")
        
        return train_mix_loader,train_loader, val_mix_loader, val_loader

    #For test data
    else:
        datagen_test = ImageDataGenerator(rescale=1./255,
                                rotation_range=0,
                                width_shift_range=0,
                                height_shift_range=0,
                                shear_range=0,
                                zoom_range=0,
                                brightness_range=(1, 1),
                                horizontal_flip=False,
                                fill_mode='nearest',
                                validation_split=0)

        test_loader = datagen_test.flow_from_directory(test_data_dir,
                                                    target_size=(img_height, img_width),
                                                        class_mode="categorical",
                                                        batch_size=batch_size,
                                                        shuffle=False,
                                                        subset=None)

        return test_loader

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Next, we define the function for creating the CNN.

# COMMAND ----------

"""
creates the CNN with number_conv convolutional layers followed by number_dense dense layers. The model is compiled with a SGD optimizer and a categorical crossentropy loss.
"""
def create_model(number_conv,number_dense):
    model = Sequential()
    model.add(Conv2D(24,kernel_size = 3, activation='relu',padding="same", input_shape=(img_height, img_width,channels)))
    model.add(BatchNormalization())
    for s in range(1,number_conv):
        model.add(Conv2D(24+12*s,kernel_size = 3,padding="same", activation = 'relu'))
        model.add(BatchNormalization())
    model.add(Flatten())
    model.add(Dropout(0.4))
    for s in range(number_dense):
        model.add(Dense(units=num_classes, activation='relu'))
        model.add(Dropout(0.4))
    model.add(BatchNormalization())
    model.add(Dense(num_classes,activation= "softmax"))
    model.compile(optimizer="adam", loss='categorical_crossentropy', metrics=['accuracy'])
    return model

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC This is the function that the ray.tune method will run. The steps in the function is to generate the dataloaders that will load the data from the working dictionary, create the model based on the hyperparameters given in the config dictionary, train the model and evaluate the model on the different datasets.  

# COMMAND ----------

def training_function(config, checkpoint_dir=None):
    # Hyperparameters
    number_conv, number_dense = config["number_conv"], config["number_dense"]
    train_with_mixed_data = config["train_with_mixed_data"]
    
    
    """
    Get the different dataloaders
    One with training data using mixing
    One with training without mixing
    One with validation data with mixing
    One with validation without mixing
    Set for_training to False to get testing data
    """
    #train_data_dir,test_data_dir = "/dbfs/FileStore/tables/Group20/seg_train/seg_train","/dbfs/FileStore/tables/Group20/seg_test/seg_test"

    #train_data_dir, test_data_dir = copy_data()
    train_mix_dataloader,train_dataloader,val_mix_dataloader,val_dataloader = get_data_loaders(train_data_dir, test_data_dir, for_training = True)

    """
    Construct the model based on hyperparameters
    """
    model = create_model( number_conv,number_dense )

    
    """
    Adds earlystopping to training. This is based on the performance accuracy on the validation dataset. Chould we have validation loss here?
    """
    callbacks = [tf.keras.callbacks.EarlyStopping(patience=10,monitor="val_accuracy",min_delta=0.01,restore_best_weights=True)]

    """
    Train the model and give the training history.
    """
    if train_with_mixed_data:
      history = model.fit_generator(train_mix_dataloader, validation_data = val_dataloader,callbacks = callbacks,verbose = True,epochs = 200)
    else:
      history = model.fit_generator(train_dataloader, validation_data = val_dataloader,callbacks = callbacks,verbose = True,epochs = 200)
    
    """
    Logg the results
    """
    #x_mix, y_mix = mixup_data( x_val, y_val)
    #mix_loss, mix_acc = model.evaluate( x_mix, y_mix )
    train_loss_unmix, train_acc_unmix = model.evaluate( train_dataloader )
    val_mix_loss, val_mix_acc = model.evaluate( val_mix_dataloader )
    ind_max = np.argmax(history.history['val_accuracy'])
    train_mix_acc = history.history['accuracy'][ind_max]
    train_mix_loss = history.history["loss"][ind_max]
    train_loss = history.history['loss'][ind_max]
    val_acc = history.history['val_accuracy'][ind_max]
    val_loss = history.history['val_loss'][ind_max]
    
    tune.report(mean_loss=train_mix_loss, train_mix_accuracy = train_mix_acc, train_accuracy = train_acc_unmix, val_mix_accuracy = val_mix_acc, val_accuracy = val_acc)


# COMMAND ----------

train_data_dir,test_data_dir = copy_data()

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC First, we will train our neural networks using a standard procedure, with normal training data. We then measure their performance on a validation set as well as on a MixUp version of the same validation set, the idea being to study the connection between these metrics.

# COMMAND ----------

# Limit the number of rows.
reporter = CLIReporter(max_progress_rows=10)
# Add a custom metric column, in addition to the default metrics.
# Note that this must be a metric that is returned in your training results.
reporter.add_metric_column("val_mix_accuracy")
reporter.add_metric_column("val_accuracy")
reporter.add_metric_column("train_accuracy")
reporter.add_metric_column("train_mix_accuracy")

#config = {"number_conv" : 3,"number_dense" : 5}
#training_function(config)

#get_data_loaders()

analysis = tune.run(
    training_function,
    config={
        "number_conv": tune.grid_search(np.arange(2,7,3).tolist()),
        "number_dense": tune.grid_search(np.arange(0,3,2).tolist()),
        "train_with_mixed_data": False
    },
    local_dir='ray_results',
    progress_reporter=reporter
) 
  #resources_per_trial={'gpu': 1})

print("Best config: ", analysis.get_best_config(
    metric="val_accuracy", mode="max"))

#Get a dataframe for analyzing trial results.
df = analysis.results_df


# COMMAND ----------

df

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC We now check whether directly training on MixUp data has a positive effect on network performance.

# COMMAND ----------

# Limit the number of rows.
reporter = CLIReporter(max_progress_rows=10)
# Add a custom metric column, in addition to the default metrics.
# Note that this must be a metric that is returned in your training results.
reporter.add_metric_column("val_mix_accuracy")
reporter.add_metric_column("val_accuracy")
reporter.add_metric_column("train_accuracy")

#config = {"number_conv" : 3,"number_dense" : 5}
#training_function(config)

#get_data_loaders()

analysis = tune.run(
    training_function,
    config={
        "number_conv": tune.grid_search(np.arange(2,7,3).tolist()),
        "number_dense": tune.grid_search(np.arange(0,3,2).tolist()),
        "train_with_mixed_data": True
    },
    local_dir='ray_results',
    progress_reporter=reporter)
    
  #resources_per_trial={'gpu': 1})

print("Best config: ", analysis.get_best_config(
    metric="val_accuracy", mode="max"))

#Get a dataframe for analyzing trial results.
df = analysis.results_df


# COMMAND ----------

df

# COMMAND ----------

# MAGIC %md 
# MAGIC 
# MAGIC **Conclusions**
# MAGIC 
# MAGIC We found that training a CNN using the Ray package was harder than we thought from the beginning. This is probably due to the GPU usage and that we had problems assigning the Keras model to the correct GPU. In other words, Ray requested GPU usage but the code only ever ran on CPU, which took an unfeasible amount of time.

# COMMAND ----------

