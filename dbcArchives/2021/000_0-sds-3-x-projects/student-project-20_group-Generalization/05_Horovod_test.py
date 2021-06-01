# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC ## CNNs and MixUp with Horovod
# MAGIC 
# MAGIC One of the arguments in favor for using MixUp is the data augmentation it provides. For iterative learning algorithms, such as CNNs trained with a variant of stochastic gradient descent, we can generate new MixUp data for each training batch. This effectively means that the network will never see any training example twice. To harness this positive aspect of MixUp to its fullest extent, we want our algorithm to be scalable in the data to use it efficiently. To train neural networks in a scalable way with respet to the data, one can use Horovod, which parallelizes the neural network training procedure.
# MAGIC 
# MAGIC In this notebook, we use Horovod to train a CNN on the CIFAR-10 data set, both without and with MixUp. While the notebook is executed with only one GPU, the code scales nicely if more GPUs are available.

# COMMAND ----------

# MAGIC %md
# MAGIC First, we import packages and check what computational resources are available. In this case, we have one GPU.

# COMMAND ----------

import horovod.tensorflow.keras as hvd
import tensorflow as tf
import numpy as np
from tensorflow import keras
from tensorflow.keras.layers import Dense,Conv2D,Flatten,BatchNormalization,Dropout
from tensorflow.keras import Sequential
from sklearn.metrics import confusion_matrix
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from functools import partial
import os
import time

print(tf.__version__)
from tensorflow.python.client import device_lib
local_device_protos = device_lib.list_local_devices()
print(local_device_protos)

checkpoint_dir = '/dbfs/ml/Group_20/train/{}/'.format(time.time())

os.makedirs(checkpoint_dir)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Next, we define the generator for our MixUp images.

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
# MAGIC We now define functions for creating the neural network and initializing the dataloaders. We will use dataloaders both with and without MixUp for both training and validation.

# COMMAND ----------

"""
creates the CNN with number_conv convolutional layers followed by number_dense dense layers. THe model is compiled with a SGD optimizer and a categorical crossentropy loss.
"""
def create_model(number_conv,number_dense,optimizer = "adam"):
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
    model.compile(optimizer=optimizer, loss='categorical_crossentropy', metrics=['accuracy'])
    return model


"""
A method that gives us the different dataloaders that we need for training and validation. with for_training set to True the model will give us the dataloades

train_mix_loader: A data loader that will give us mixes data for training
train_loader: A data loader that gives us the unmixed training data
val_mixed_loader: A data loader that gives us the mixed validation data
val_loader: A data loader with the unmixed validation data

By setting for_training to False the method will give us the dataloader

test_loader: Unmixed and unshuffled dataloader for the testing data. The reason for not shuffeling the data is in order to simplify the validation process.
"""
def get_cifar_dataloaders():
    (trainX,trainY),(testX,testY) = tf.keras.datasets.cifar10.load_data()
    trainX,testX = tf.cast(trainX,tf.float32),tf.cast(testX,tf.float32)
    #trainX,testX = tf.expand_dims(trainX, 3),tf.expand_dims(testX, 3)
    trainY_oh,testY_oh = tf.one_hot(trainY[:,0],10),tf.one_hot(testY[:,0],10)
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
# MAGIC Next, we define the training function that will be used by Horovod. Each worker uses the datagenerator to load data.

# COMMAND ----------

def train_hvd(learning_rate=1.0, train_with_mix = False):
  # Import tensorflow modules to each worker
  from tensorflow.keras import backend as K
  from tensorflow.keras.models import Sequential
  import tensorflow as tf
  from tensorflow import keras
  import horovod.tensorflow.keras as hvd
  
  # Initialize Horovod
  hvd.init()

  # Pin GPU to be used to process local rank (one GPU per process)
  # These steps are skipped on a CPU cluster
  gpus = tf.config.experimental.list_physical_devices('GPU')
  for gpu in gpus:
    tf.config.experimental.set_memory_growth(gpu, True)
  if gpus:
    tf.config.experimental.set_visible_devices(gpus[hvd.local_rank()], 'GPU')

  # Call the get_dataset function you created, this time with the Horovod rank and size
  train_mix_dataloader,train_dataloader,val_mix_dataloader,val_dataloader = get_cifar_dataloaders()
  model = create_model( number_conv,number_dense )

  # Adjust learning rate based on number of GPUs
  optimizer = keras.optimizers.Adadelta(lr=learning_rate * hvd.size())

  # Use the Horovod Distributed Optimizer
  optimizer = hvd.DistributedOptimizer(optimizer)

  model.compile(optimizer=optimizer,
                loss='categorical_crossentropy',
                metrics=['accuracy'])

  # Create a callback to broadcast the initial variable states from rank 0 to all other processes.
  # This is required to ensure consistent initialization of all workers when training is started with random weights or restored from a checkpoint.
  callbacks = [
      hvd.callbacks.BroadcastGlobalVariablesCallback(0),
  ]

  # Save checkpoints only on worker 0 to prevent conflicts between workers
  if hvd.rank() == 0:
      callbacks.append(keras.callbacks.ModelCheckpoint(checkpoint_dir + '/checkpoint-{epoch}.ckpt', save_weights_only = True))
      
  if train_with_mix:
    model.fit(train_mix_dataloader,
            batch_size=batch_size,
            callbacks=callbacks,
            epochs=epochs,
            verbose=2,
            validation_data=val_dataloader)
  else:
    model.fit(train_dataloader,
            batch_size=batch_size,
            callbacks=callbacks,
            epochs=epochs,
            verbose=2,
            validation_data=val_dataloader)
       

# COMMAND ----------

# MAGIC %md
# MAGIC Below, we give the parameters that control the training procedure.

# COMMAND ----------

"""
The global parameters for training.
"""

img_height,img_width,channels = 32,32,3
batch_size = 32
#train_data_dir,test_data_dir = "/content/seg_train/seg_train","/content/seg_test/seg_test"
#train_data_dir,test_data_dir = "dbfs/FileStore/tables/Group20/seg_train/seg_train/", "dbfs/FileStore/tables/Group20/seg_test/seg_test/"
#train_data_dir,test_data_dir = copy_data()
num_classes = 10
number_conv = 4
number_dense = 2
epochs = 30
alpha = 0.2
#train_with_mixed_data = True


# COMMAND ----------

# MAGIC %md
# MAGIC Now, let us run training with Horovod, first on MixUp data, then without MixUp.

# COMMAND ----------

from sparkdl import HorovodRunner

hr = HorovodRunner(np=2)
hr.run(train_hvd, learning_rate=0.1, train_with_mix = True)

# COMMAND ----------

from sparkdl import HorovodRunner

hr_nomix = HorovodRunner(np=2)
hr_nomix.run(train_hvd, learning_rate=0.1, train_with_mix = False)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC #### Conclusion
# MAGIC 
# MAGIC From our simulations on CIFAR-10 with and without MixUp it seems that MixUp provides stability against overfitting and has a bit higher top validation accuracy during training. Specifically, when using MixUp, we reach a validation accuracy around 75%, while we peak at 70% without MixUp. Furthermore, when not using MixUp, the validation accuracy starts to decrease after 20 epochs, while it continues to improve with MixUp. Since this is based on only one simulation, we cannot be fully certain about these conclusions. When it comes to the scalability of the model, Horovod provides beneficial scaling with the data and makes the code very simular to a regular single-machine training notebook. Horovod can also be combined with Ray Tune to also perform a hyperparameter search, but this was not done in this project.