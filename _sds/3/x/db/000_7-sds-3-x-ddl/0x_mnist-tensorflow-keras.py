# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)
# MAGIC 
# MAGIC 
# MAGIC 
# MAGIC The following is from databricks blog with minor adaptations with help from Tilo Wiklund.

# COMMAND ----------

# MAGIC %md
# MAGIC # Distributed deep learning training using TensorFlow and Keras with HorovodRunner
# MAGIC 
# MAGIC This notebook demonstrates how to train a model for the MNIST dataset using the `tensorflow.keras` API. 
# MAGIC It first shows how to train the model on a single node, and then shows how to adapt the code using HorovodRunner for distributed training. 
# MAGIC 
# MAGIC **Requirements**
# MAGIC 
# MAGIC * This notebook runs on CPU or GPU clusters.
# MAGIC * To run the notebook, create a cluster with
# MAGIC  - Two workers
# MAGIC  - Databricks Runtime 6.3 ML or above

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC ## Cluster Specs on databricks
# MAGIC 
# MAGIC Run on `tiny-debug-cluster-(no)gpu` or another cluster with the following runtime specifications with CPU/non-GPU and GPU clusters, respectively:
# MAGIC 
# MAGIC 
# MAGIC * Runs on non-GPU cluster with 3 (or more) nodes on 7.4 ML runtime (nodes are 1+2 x m4.xlarge)
# MAGIC * Runs on GPU cluster with 3 (or more) nodes on 7.4 ML GPU runtime (nodes are 1+2 x g4dn.xlarge)
# MAGIC 
# MAGIC You do not need to "install" anything else in databricks as everything needed is pre-installed in the runtime environment on the right nodes.

# COMMAND ----------

# MAGIC %md ## Set up checkpoint location
# MAGIC The next cell creates a directory for saved checkpoint models.

# COMMAND ----------

import os
import time

checkpoint_dir = '/dbfs/ml/MNISTDemo/train/{}/'.format(time.time())

os.makedirs(checkpoint_dir)

# COMMAND ----------

# MAGIC %md ## Create function to prepare data
# MAGIC 
# MAGIC This following cell creates a function that prepares the data for training. This function takes in `rank` and `size` arguments so it can be used for both single-node and distributed training. In Horovod, `rank` is a unique process ID and `size` is the total number of processes. 
# MAGIC 
# MAGIC This function downloads the data from `keras.datasets`, distributes the data across the available nodes, and converts the data to shapes and types needed for training.

# COMMAND ----------

def get_dataset(num_classes, rank=0, size=1):
  from tensorflow import keras
  
  (x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data('MNIST-data-%d' % rank)
  x_train = x_train[rank::size]
  y_train = y_train[rank::size]
  x_test = x_test[rank::size]
  y_test = y_test[rank::size]
  x_train = x_train.reshape(x_train.shape[0], 28, 28, 1)
  x_test = x_test.reshape(x_test.shape[0], 28, 28, 1)
  x_train = x_train.astype('float32')
  x_test = x_test.astype('float32')
  x_train /= 255
  x_test /= 255
  y_train = keras.utils.to_categorical(y_train, num_classes)
  y_test = keras.utils.to_categorical(y_test, num_classes)
  return (x_train, y_train), (x_test, y_test)

# COMMAND ----------

# MAGIC %md ## Create function to train model
# MAGIC The following cell defines the model using the `tensorflow.keras` API. This code is adapted from the [Keras MNIST convnet example](https://keras.io/examples/vision/mnist_convnet/). The model consists of 2 convolutional layers, a max-pooling layer, two dropout layers, and a final dense layer.

# COMMAND ----------

def get_model(num_classes):
  from tensorflow.keras import models
  from tensorflow.keras import layers
  
  model = models.Sequential()
  model.add(layers.Conv2D(32, kernel_size=(3, 3),
                   activation='relu',
                   input_shape=(28, 28, 1)))
  model.add(layers.Conv2D(64, (3, 3), activation='relu'))
  model.add(layers.MaxPooling2D(pool_size=(2, 2)))
  model.add(layers.Dropout(0.25))
  model.add(layers.Flatten())
  model.add(layers.Dense(128, activation='relu'))
  model.add(layers.Dropout(0.5))
  model.add(layers.Dense(num_classes, activation='softmax'))
  return model

# COMMAND ----------

# MAGIC %md ## Run training on single node

# COMMAND ----------

# MAGIC %md
# MAGIC At this point, you have created functions to load and preprocess the dataset and to create the model.                   
# MAGIC This section illustrates single-node training code using `tensorflow.keras`. 

# COMMAND ----------

# Specify training parameters
batch_size = 128
epochs = 5
num_classes = 10        


def train(learning_rate=1.0):
  from tensorflow import keras
  
  (x_train, y_train), (x_test, y_test) = get_dataset(num_classes)
  model = get_model(num_classes)

  # Specify the optimizer (Adadelta in this example), using the learning rate input parameter of the function so that Horovod can adjust the learning rate during training
  optimizer = keras.optimizers.Adadelta(lr=learning_rate)

  model.compile(optimizer=optimizer,
                loss='categorical_crossentropy',
                metrics=['accuracy'])

  model.fit(x_train, y_train,
            batch_size=batch_size,
            epochs=epochs,
            verbose=2,
            validation_data=(x_test, y_test))

# COMMAND ----------

# MAGIC %md
# MAGIC Run the `train` function you just created to train a model on the driver node. The process takes several minutes. The accuracy improves with each epoch. 

# COMMAND ----------

# Runs in  23.67 seconds on 3-node     GPU
# Runs in 418.8  seconds on 3-node non-GPU
train(learning_rate=0.1)

# COMMAND ----------

# MAGIC %md
# MAGIC ## Migrate to HorovodRunner for distributed training
# MAGIC 
# MAGIC This section shows how to modify the single-node code to use Horovod. For more information about Horovod, see the [Horovod documentation](https://horovod.readthedocs.io/en/stable/).

# COMMAND ----------

def train_hvd(learning_rate=1.0):
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
  (x_train, y_train), (x_test, y_test) = get_dataset(num_classes, hvd.rank(), hvd.size())
  model = get_model(num_classes)

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

  model.fit(x_train, y_train,
            batch_size=batch_size,
            callbacks=callbacks,
            epochs=epochs,
            verbose=2,
            validation_data=(x_test, y_test))

# COMMAND ----------

# MAGIC %md
# MAGIC Now that you have defined a training function with Horovod,  you can use HorovodRunner to distribute the work of training the model. 
# MAGIC 
# MAGIC The HorovodRunner parameter `np` sets the number of processes. This example uses a cluster with two workers, each with a single GPU, so set `np=2`. (If you use `np=-1`, HorovodRunner trains using a single process on the driver node.)

# COMMAND ----------

# runs in  47.84 seconds on 3-node     GPU cluster
# Runs in 316.8  seconds on 3-node non-GPU cluster
from sparkdl import HorovodRunner

hr = HorovodRunner(np=2)
hr.run(train_hvd, learning_rate=0.1)

# COMMAND ----------

# MAGIC %md 
# MAGIC Under the hood, HorovodRunner takes a Python method that contains deep learning training code with Horovod hooks. HorovodRunner pickles the method on the driver and distributes it to Spark workers. A Horovod MPI job is embedded as a Spark job using the barrier execution mode. The first executor collects the IP addresses of all task executors using BarrierTaskContext and triggers a Horovod job using `mpirun`. Each Python MPI process loads the pickled user program, deserializes it, and runs it.
# MAGIC 
# MAGIC For more information, see [HorovodRunner API documentation](https://databricks.github.io/spark-deep-learning/#api-documentation). 