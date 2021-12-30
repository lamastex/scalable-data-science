// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Reinforcement Learning - Distributed model tuning with Elephas

// COMMAND ----------

// Scala imports
import org.lamastex.spark.trendcalculus._
import spark.implicits._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import java.sql.Timestamp
import org.apache.spark.sql.expressions._

// COMMAND ----------

// Load dataset
val oilDS = spark.read.fx1m("dbfs:/FileStore/shared_uploads/fabiansi@kth.se/*csv.gz").toDF.withColumn("ticker", lit("BCOUSD")).select($"ticker", $"time" as "x", $"close" as "y").as[TickerPoint].orderBy("time")

// Add column with difference from previous close value (expected 'x', 'y' column names)
val windowSpec = Window.orderBy("x")
val oilDS1 = oilDS 
.withColumn("diff_close", $"y" - when((lag("y", 1).over(windowSpec)).isNull, 0).otherwise(lag("y", 1).over(windowSpec)))

// Rename variables
val oilDS2 = oilDS1.withColumnRenamed("x","time").withColumnRenamed("y","close")

// Remove incomplete data from first day (2010-11-14) and last day (2019-06-21)
val oilDS3 = oilDS2.filter(to_date(oilDS2("time")) >= lit("2010-11-15") && to_date(oilDS2("time")) <= lit("2019-06-20"))

// Add index column
val windowSpec1 = Window.orderBy("time")
val oilDS4 = oilDS3
.withColumn("index", row_number().over(windowSpec1))

// Drop ticker column
val oilDS5 = oilDS4.drop("ticker")

// Store loaded data as temp view, to be accessible in Python
oilDS5.createOrReplaceTempView("temp")

// COMMAND ----------

// MAGIC %python
// MAGIC #Python imports
// MAGIC import datetime
// MAGIC import gym
// MAGIC import math
// MAGIC import random
// MAGIC import json
// MAGIC import collections
// MAGIC import numpy as np
// MAGIC import matplotlib.pyplot as plt
// MAGIC from keras.models import Sequential
// MAGIC from keras.layers.core import Dense
// MAGIC from keras.layers import Conv1D, MaxPool1D, Flatten, BatchNormalization
// MAGIC from keras import optimizers
// MAGIC 
// MAGIC from elephas.utils.rdd_utils import to_simple_rdd
// MAGIC from elephas.spark_model import SparkModel

// COMMAND ----------

// MAGIC %python
// MAGIC # Create Dataframe from temp data
// MAGIC oilDF_py = spark.table("temp")
// MAGIC 
// MAGIC # Select the 10 first Rows of data and print them
// MAGIC ten_oilDF_py = oilDF_py.limit(10)
// MAGIC ten_oilDF_py.show()
// MAGIC 
// MAGIC # Check number of data points
// MAGIC last_index = oilDF_py.count()
// MAGIC print("Number of data points: {}".format(last_index))
// MAGIC 
// MAGIC # Select the date of the last data point
// MAGIC print("Last data point: {}".format(np.array(oilDF_py.where(oilDF_py.index == last_index).select('time').collect()).item()))

// COMMAND ----------

// MAGIC %python
// MAGIC # Adapted from: https://github.com/kh-kim/stock_market_reinforcement_learning/blob/master/market_env.py
// MAGIC 
// MAGIC 
// MAGIC class MarketEnv(gym.Env):
// MAGIC     def __init__(self, full_data, start_date, end_date, episode_size=30*24*60, scope=60):
// MAGIC         self.episode_size = episode_size
// MAGIC         self.actions = ["LONG", "SHORT"] 
// MAGIC         self.action_space = gym.spaces.Discrete(len(self.actions))
// MAGIC         self.state_space = gym.spaces.Box(np.ones(scope) * -1, np.ones(scope))
// MAGIC 
// MAGIC         self.diff_close = np.array(full_data.filter(full_data["time"] > start_date).filter(full_data["time"] <= end_date).select('diff_close').collect())
// MAGIC         max_diff_close = np.max(self.diff_close)
// MAGIC         self.diff_close = self.diff_close*max_diff_close
// MAGIC         self.close = np.array(full_data.filter(full_data["time"] > start_date).filter(full_data["time"] <= end_date).select('close').collect())
// MAGIC         self.num_ticks_train = np.shape(self.diff_close)[0]
// MAGIC 
// MAGIC         self.scope = scope # N values to be included in a state vector
// MAGIC         self.time_index = self.scope  # start N steps in, to ensure that we have enough past values for history 
// MAGIC         self.episode_init_time = self.time_index  # initial time index of the episode
// MAGIC 
// MAGIC 
// MAGIC     def step(self, action):
// MAGIC         info = {'index': int(self.time_index), 'close': float(self.close[self.time_index])}
// MAGIC         self.time_index += 1
// MAGIC         self.state = self.diff_close[self.time_index - self.scope:self.time_index]
// MAGIC         self.reward = float( - (2 * action - 1) * self.state[-1] )
// MAGIC         
// MAGIC         # Check if done
// MAGIC         if self.time_index - self.episode_init_time > self.episode_size:
// MAGIC             self.done = True
// MAGIC         if self.time_index > self.diff_close.shape[0] - self.scope -1:
// MAGIC             self.done = True
// MAGIC 
// MAGIC         return self.state, self.reward, self.done, info
// MAGIC 
// MAGIC     def reset(self, random_starttime=True):
// MAGIC         self.done = False
// MAGIC         self.reward = 0.
// MAGIC         self.time_index = self.scope 
// MAGIC         self.state = self.diff_close[self.time_index - self.scope:self.time_index]
// MAGIC         
// MAGIC         if random_starttime:
// MAGIC             self.time_index += random.randint(0, self.num_ticks_train - self.scope)
// MAGIC         
// MAGIC         self.episode_init_time = self.time_index
// MAGIC         
// MAGIC         return self.state
// MAGIC 
// MAGIC     def seed(self):
// MAGIC         pass

// COMMAND ----------

// MAGIC %python
// MAGIC # Adapted from: https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/4201196137758409/command/4201196137758410
// MAGIC 
// MAGIC class ExperienceReplay:
// MAGIC     def __init__(self, max_memory=100, discount=.9):
// MAGIC         self.max_memory = max_memory
// MAGIC         self.memory = list()
// MAGIC         self.discount = discount
// MAGIC 
// MAGIC     def remember(self, states, done):
// MAGIC         self.memory.append([states, done])
// MAGIC         if len(self.memory) > self.max_memory:
// MAGIC             del self.memory[0]
// MAGIC 
// MAGIC     def get_batch(self, model, batch_size=10):
// MAGIC         len_memory = len(self.memory)
// MAGIC         num_actions = model.output_shape[-1]
// MAGIC 
// MAGIC         env_dim = self.memory[0][0][0].shape[1]
// MAGIC         inputs = np.zeros((min(len_memory, batch_size), env_dim, 1))
// MAGIC         targets = np.zeros((inputs.shape[0], num_actions))
// MAGIC         for i, idx in enumerate(np.random.randint(0, len_memory, size=inputs.shape[0])):
// MAGIC             state_t, action_t, reward_t, state_tp1 = self.memory[idx][0]
// MAGIC             done = self.memory[idx][1]
// MAGIC 
// MAGIC             inputs[i:i + 1] = state_t
// MAGIC             # There should be no target values for actions not taken.
// MAGIC             targets[i] = model.predict(state_t)[0]
// MAGIC             Q_sa = np.max(model.predict(state_tp1)[0])
// MAGIC             if done: # if done is True
// MAGIC                 targets[i, action_t] = reward_t
// MAGIC             else:
// MAGIC                 # reward_t + gamma * max_a' Q(s', a')
// MAGIC                 targets[i, action_t] = reward_t + self.discount * Q_sa
// MAGIC         return inputs, targets

// COMMAND ----------

// MAGIC %python
// MAGIC # Adapted from: https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/4201196137758409/command/4201196137758410
// MAGIC 
// MAGIC # RL parameters
// MAGIC epsilon = .5  # exploration
// MAGIC min_epsilon = 0.1
// MAGIC max_memory = 5000
// MAGIC batch_size = 512
// MAGIC discount = 0.8
// MAGIC 
// MAGIC # Environment parameters
// MAGIC num_actions = 2  # [long, short]
// MAGIC episodes = 500 # 100000
// MAGIC episode_size = 1 * 1 * 60  # roughly an hour worth of data in each training episode
// MAGIC 
// MAGIC # Define state sequence scope (approx. 1 hour)
// MAGIC sequence_scope = 60
// MAGIC input_shape = (batch_size, sequence_scope, 1)
// MAGIC 
// MAGIC # Create Q Network
// MAGIC hidden_size = 128
// MAGIC model = Sequential()
// MAGIC model.add(Conv1D(32, (5), strides=2, input_shape=input_shape[1:], activation='relu'))
// MAGIC model.add(MaxPool1D(pool_size=2, strides=1))
// MAGIC model.add(BatchNormalization())
// MAGIC model.add(Conv1D(32, (5), strides=1, activation='relu'))
// MAGIC model.add(MaxPool1D(pool_size=2, strides=1))
// MAGIC model.add(BatchNormalization())
// MAGIC model.add(Flatten())
// MAGIC model.add(Dense(hidden_size, activation='relu'))
// MAGIC model.add(BatchNormalization())
// MAGIC model.add(Dense(num_actions))
// MAGIC opt = optimizers.Adam(lr=0.01)
// MAGIC model.compile(loss='mse', optimizer=opt)
// MAGIC 
// MAGIC # Define training interval
// MAGIC start = datetime.datetime(2010, 11, 15, 0, 0)
// MAGIC end = datetime.datetime(2018, 12, 31, 23, 59)
// MAGIC 
// MAGIC # Initialize Environment
// MAGIC env = MarketEnv(oilDF_py, start, end, episode_size=episode_size, scope=sequence_scope)
// MAGIC 
// MAGIC # Initialize experience replay object
// MAGIC exp_replay = ExperienceReplay(max_memory=max_memory, discount=discount)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Elephas
// MAGIC https://github.com/danielenricocahall/elephas
// MAGIC 
// MAGIC Elephas is a third party library that allows to train distributed Keras models on Spark. To run a distributed training session, a Keras model is first declared and compiled on one (singular) master node. Then, copies of the Master model are serialized and shipped to an arbitrary number of worker nodes. Elephas uses RDD's internally to make the data dynamically available to the workers when required. After gradient computating and the update of the weights, the updated model parameters are pushed to the master model.
// MAGIC 
// MAGIC 
// MAGIC <img src=https://raw.githubusercontent.com/danielenricocahall/elephas/master/elephas.gif>
// MAGIC 
// MAGIC For updating the parameters of the master_model, Elephas provides three modes, Synchronous, Asynchronous and HOGWILD (https://arxiv.org/abs/1106.5730). 
// MAGIC 
// MAGIC ## Integrating Distributed model training in our RL-framework
// MAGIC 
// MAGIC Elephas supports in the current version only supervised model training. We therefore opt to distribute the supervised training step based on the experience replay buffer and keep the surrounding for loops from the previeous RL-implementation.  
// MAGIC 
// MAGIC ### Training data conversion
// MAGIC 
// MAGIC Data must be provided as either RDD or pyspark dataframe (that will internally be converted to RDD's). A more elaborate pipeline might slice and evaluate replay buffer instances from the original dataframe, however, since most of our implementation expects numpy arrays, we convert the buffer to an RDD manually each step. 
// MAGIC 
// MAGIC ### Elephas SparkModel for retraining in the RL-loop
// MAGIC 
// MAGIC When Elephas finished its training epochs (here, one Experiancereplay buffer training in one of the RL-loop sweeps), the used processes get terminated. This leads to a crash when trying to retrain a already trained model. As a workaround, we initialize theelephas in each training step newly by using the keras model from the previous training step.  

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC # elephas variables
// MAGIC ele_epochs = 10
// MAGIC ele_batchsize = 32
// MAGIC ele_verbose = 0
// MAGIC ele_valsplit = 0.1
// MAGIC 
// MAGIC # Train
// MAGIC returns = []
// MAGIC for e in range(1, episodes):
// MAGIC     loss = 0.
// MAGIC     counter = 0
// MAGIC     reward_sum = 0.
// MAGIC     done = False
// MAGIC     
// MAGIC     state = env.reset()
// MAGIC     input_t = state.reshape(1, sequence_scope, 1) 
// MAGIC     
// MAGIC     while not done:     
// MAGIC         counter += 1
// MAGIC         input_tm1 = input_t
// MAGIC         # get next action
// MAGIC         if np.random.rand() <= epsilon:
// MAGIC             action = np.random.randint(0, num_actions, size=1)
// MAGIC         else:
// MAGIC             q = model.predict(input_tm1)
// MAGIC             action = np.argmax(q[0])
// MAGIC 
// MAGIC         # apply action, get rewards and new state
// MAGIC         state, reward, done, info = env.step(action)
// MAGIC         reward_sum += reward
// MAGIC         input_t = state.reshape(1, sequence_scope, 1)         
// MAGIC 
// MAGIC         # store experience
// MAGIC         exp_replay.remember([input_tm1, action, reward, input_t], done)
// MAGIC 
// MAGIC         # adapt model
// MAGIC         inputs, targets = exp_replay.get_batch(model, batch_size=batch_size)
// MAGIC         
// MAGIC         # elephas calls for distributed gradient optimization
// MAGIC         train_rdd = to_simple_rdd(sc, inputs, targets)  # note that we provide the spark context sc (sc variable automatically set in databricks)
// MAGIC         
// MAGIC         spark_model = SparkModel(model, frequency='epoch', mode='asynchronous')  # 'asynchronous', 'hogwild' or 'synchronous'
// MAGIC         spark_model.fit(train_rdd, epochs=ele_epochs, batch_size=ele_batchsize, verbose=ele_verbose, validation_split=ele_valsplit)
// MAGIC         model = spark_model._master_network # hacky!
// MAGIC         
// MAGIC         loss += model.train_on_batch(inputs, targets)
// MAGIC     
// MAGIC     
// MAGIC     print("Episode {:03d}/{:d} | Average Loss {:.4f} | Cumulative Reward {:.4f}".format(e, episodes, loss / counter, reward_sum))
// MAGIC     epsilon = max(min_epsilon, epsilon * 0.99)
// MAGIC     returns.append(reward_sum)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Notes to the elephas training
// MAGIC 
// MAGIC The pipeline in this notebook serves as a proof of concept to demonstrate how RL-training can be distributed on a spark cluster. During testing, we observed that the experience replay is a bottleneck during distributed model training, when comparing to running keras out-of the box in parallel. 
// MAGIC 
// MAGIC ## Error that sometimes appears when running on databricks
// MAGIC 
// MAGIC We notice occasional crashes in the distributed training at line 180 in https://github.com/danielenricocahall/elephas/blob/master/elephas/spark_model.py, more precisely at `rdd.mapPartitions(worker.train).collect()`, with a `Py4JJavaError`. Restarting the cluster does not resolve the issue, however sometimes it was possible to re-run a training succesfully after a bit of time. We assume that it is connected with the jvm, but lacking precise insight regarding the nature of this bug.