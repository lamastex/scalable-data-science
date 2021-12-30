# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC ## CNN for MNIST
# MAGIC 
# MAGIC Let us move to a classic machine learning task: Image classification with Convolutional Neural Networks (CNN). The general idea is as follows:
# MAGIC 1. Train a CNN on normal training data. Evaluate its performance on a conventional ("unmixed") validation set and on a MixUp ("mixed") version of the same validation set.
# MAGIC 2. Train a CNN on MixUp training data. Evaluate its performance on both unmixed and mixed validation data.
# MAGIC 
# MAGIC When training on MixUp training data, we compute a new MixUp of each batch in every epoch. As explained in the introduction, this effectively augments the training set and hopefully makes the network more robust. Evaluating the performance of both networks on unmixed and mixed validation data allows us to compare the generalization properties of both networks, the working hypothesis being that training on MixUp data enhances generalization. To reduce the dependence of our results on the specific choice of hyperparameters, we train several CNNs with varying numbers of convolutional and dense layers. This is done for both kinds of training data (unmixed, mixed) in a distributed fashion using Ray Tune.
# MAGIC 
# MAGIC In this notebook, we train a simple MNIST classifier. This notebook runs on a CPU, but with a hyperparameter search method that can be scaled up to different workers and be run in parallel. 

# COMMAND ----------

# MAGIC %md
# MAGIC Import the necessary packages. 

# COMMAND ----------

import tensorflow as tf
import numpy as np
from tensorflow import keras
from tensorflow.keras import Sequential
from tensorflow.keras.layers import Dense,Conv2D,Flatten,BatchNormalization,Dropout
from ray import tune
from ray.tune import CLIReporter
from sklearn.metrics import confusion_matrix
#from sparkdl import HorovodRunner
from tensorflow.keras.preprocessing.image import ImageDataGenerator

import shutil
import os


# Fixes the issue "AttributeError: 'ConsoleBuffer has no attribute 'fileno'"
import sys
sys.stdout.fileno = lambda: False

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC A data generator class that performs MixUp in the loaded data. This is done with two Tensorflow data generators that both load data from our dataset in a shuffled manner and then linearly combined in order to construct the mixed data. The time complexity of this loader is at least twice the time as a normal Tensorflow data loader. 

# COMMAND ----------

class MixupImageDataGenerator_from_tensor(tf.keras.utils.Sequence):

    """
    A datagenerator that performs mixup on the input data. The input to the generator is numpy arrays with data and labels. 
    """
  
    def __init__(self, X,Y, batch_size, alpha=0.2, subset=None):
        self.batch_size = batch_size
        self.batch_index = 0
        self.alpha = alpha
        self.X = X
        self.Y = Y
        
        # First iterator yielding tuples of (x, y)
        ind = np.random.permutation(len(X))
        self.generator1 = iter(tf.data.Dataset.from_tensor_slices((X[ind],Y[ind])).batch(self.batch_size))
        
        
        # Second iterator yielding tuples of (x, y)
        ind = np.random.permutation(len(X))
        self.generator2 = iter(tf.data.Dataset.from_tensor_slices((X[ind],Y[ind])).batch(self.batch_size))

        # Number of images across all classes in image directory.
        self.n = len(X)


    def __len__(self):
        # returns the number of batches
        return (self.n + self.batch_size - 1) // self.batch_size

    def __getitem__(self, index):
        
        if self.batch_index >= self.__len__()-1:
          self.reset_index()
          self.batch_index = 0
        else:
          self.batch_index += 1
        
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

        # First iterator yielding tuples of (x, y)
        ind = np.random.permutation(len(self.X))
        self.generator1 = iter(tf.data.Dataset.from_tensor_slices((self.X[ind],self.Y[ind])).batch(self.batch_size))
        
        
        # Second iterator yielding tuples of (x, y)
        ind = np.random.permutation(len(self.X))
        self.generator2 = iter(tf.data.Dataset.from_tensor_slices((self.X[ind],self.Y[ind])).batch(self.batch_size))



    def on_epoch_end(self):
        return
        #self.reset_index()

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Two helping methods that create the model based on the hyperparameters "number_conv" and "number_dense" and create the dataloaders needed for training and validation.

# COMMAND ----------

"""
creates the CNN with number_conv convolutional layers followed by number_dense dense layers. THe model is compiled with a SGD optimizer and a categorical crossentropy loss.
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


"""
A method that gives us the different dataloaders that we need for training and validation.

train_mix_loader: A data loader that will give us mixes data for training
train_loader: A data loader that gives us the unmixed training data
val_mixed_loader: A data loader that gives us the mixed validation data
val_loader: A data loader with the unmixed validation data

"""
        
def get_mnist_dataloaders():
  (trainX,trainY),(testX,testY) = tf.keras.datasets.mnist.load_data()
  trainX,testX = tf.cast(trainX,tf.float32),tf.cast(testX,tf.float32)
  trainX,testX = tf.expand_dims(trainX, 3),tf.expand_dims(testX, 3)
  trainY_oh,testY_oh = tf.one_hot(trainY,10),tf.one_hot(testY,10)
  trainY_oh,testY_oh = tf.cast(trainY_oh,tf.float32).numpy(),tf.cast(testY_oh,tf.float32).numpy()
  trainX,testX = trainX.numpy()/255 * 2 - 2,testX.numpy()/255 * 2 - 2

  
  train_loader_mix = MixupImageDataGenerator_from_tensor(trainX,trainY_oh,batch_size)
  train_loader = tf.data.Dataset.from_tensor_slices((trainX,trainY_oh)).batch(batch_size)
  test_loader_mix = MixupImageDataGenerator_from_tensor(testX,testY_oh,batch_size)
  test_loader = tf.data.Dataset.from_tensor_slices((trainX,trainY_oh)).batch(batch_size)
  
  return train_loader_mix,train_loader,test_loader_mix,test_loader
  

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC The method that describes how to construct and train the model.
# MAGIC 
# MAGIC The steps here are, loading the data and generate the different data loaders, train the model on the preprocessed data and validate the method on the different data sets and report back to the scheduler.

# COMMAND ----------

def training_function(config, checkpoint_dir=None):
    # Hyperparameters
    number_conv, number_dense,train_with_mixed_data = config["number_conv"], config["number_dense"],config["train_with_mixed_data"]
    
     
    """
    Get the different dataloaders
    One with training data using mixing
    One with training without mixing
    One with validation data with mixing
    One with validation without mixing
    """
    #train_mix_dataloader,train_dataloader,val_mix_dataloader,val_dataloader = get_data_loaders(train_dir,test_dir,for_training = True)
    train_mix_dataloader,train_dataloader,val_mix_dataloader,val_dataloader = get_mnist_dataloaders()
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
      history = model.fit_generator(train_mix_dataloader, validation_data = val_mix_dataloader,callbacks = callbacks,verbose = False,epochs = 200)
    else:
      history = model.fit_generator(train_dataloader, validation_data = val_mix_dataloader,callbacks = callbacks,verbose = False,epochs = 200)
    
    """
    Logg the results
    """
    #x_mix, y_mix = mixup_data( x_val, y_val)
    #mix_loss, mix_acc = model.evaluate( x_mix, y_mix )
    #test_loss, test_acc = model.evaluate( x_val, y_val )
    ind_max = np.argmax(history.history['val_accuracy'])
    train_acc = history.history['accuracy'][ind_max]
    val_acc = history.history['val_accuracy'][ind_max]
    
    tune.report(mean_loss=train_acc,val_mix_accuracy = val_acc)


# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC The global hyperparameters that we need for training.

# COMMAND ----------

img_height,img_width,channels = 28,28,1
batch_size = 50
alpha = 0.2
num_classes = 10

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC The cell that runs the code. In order to train the different models in parallel, we use the ray.tune package that will schedule the training and split the available resources to the various workers. 

# COMMAND ----------

# Limit the number of rows.
reporter = CLIReporter(max_progress_rows=10)
# Add a custom metric column, in addition to the default metrics.
# Note that this must be a metric that is returned in your training results.
reporter.add_metric_column("val_mix_accuracy")
#reporter.add_metric_column("test_accuracy")

#config = {"number_conv" : 3,"number_dense" : 5}
#training_function(config)

#get_data_loaders()

analysis = tune.run(
    training_function,
    config={
        "number_conv": tune.grid_search(np.arange(2,5,1).tolist()),
        "number_dense": tune.grid_search(np.arange(0,3,1).tolist()),
        "train_with_mixed_data": tune.grid_search([True,False])
    },
    local_dir='ray_results',
    progress_reporter=reporter)

print("Best config: ", analysis.get_best_config(
    metric="mean_loss", mode="max"))

#Get a dataframe for analyzing trial results.
df = analysis.results_df


# COMMAND ----------

#print(df)
df

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC #### Conclusion
# MAGIC 
# MAGIC From the dataframe of the results shown above, we can see the accuracy on the validation dataset for the different settings. If we compare the runs with mixup against those without mixup for the different network architectures, we can investigate how much of an effect the mixup implementation has. As we can see, one of the runs did not converge at all. By not including that run, we can see that the average difference off accuracy is 0.01 to the advantage of unmixed data. Without any statistical analysis, we assume this difference is practically zero.
# MAGIC Our reasoning to why we don't see any impact of mixup in this simulation is that MNIST is such an easy task to train on that a mixup of the data will not affect the results much.