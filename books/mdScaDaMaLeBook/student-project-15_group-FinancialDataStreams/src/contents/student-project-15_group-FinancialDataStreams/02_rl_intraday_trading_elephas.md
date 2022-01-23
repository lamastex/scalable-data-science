<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Reinforcement Learning - Distributed model tuning with Elephas
==============================================================

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Scala imports
import org.lamastex.spark.trendcalculus._
import spark.implicits._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import java.sql.Timestamp
import org.apache.spark.sql.expressions._
```

<div class="output execute_result plain_result" execution_count="1">

    import org.lamastex.spark.trendcalculus._
    import spark.implicits._
    import org.apache.spark.sql._
    import org.apache.spark.sql.functions._
    import java.sql.Timestamp
    import org.apache.spark.sql.expressions._

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    oilDS: org.apache.spark.sql.Dataset[org.lamastex.spark.trendcalculus.TickerPoint] = [ticker: string, x: timestamp ... 1 more field]
    windowSpec: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@6dd55d65
    oilDS1: org.apache.spark.sql.DataFrame = [ticker: string, x: timestamp ... 2 more fields]
    oilDS2: org.apache.spark.sql.DataFrame = [ticker: string, time: timestamp ... 2 more fields]
    oilDS3: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [ticker: string, time: timestamp ... 2 more fields]
    windowSpec1: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@5be434ff
    oilDS4: org.apache.spark.sql.DataFrame = [ticker: string, time: timestamp ... 3 more fields]
    oilDS5: org.apache.spark.sql.DataFrame = [time: timestamp, close: double ... 2 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
#Python imports
import datetime
import gym
import math
import random
import json
import collections
import numpy as np
import matplotlib.pyplot as plt
from keras.models import Sequential
from keras.layers.core import Dense
from keras.layers import Conv1D, MaxPool1D, Flatten, BatchNormalization
from keras import optimizers

from elephas.utils.rdd_utils import to_simple_rdd
from elephas.spark_model import SparkModel
```

<div class="output execute_result plain_result" execution_count="1">

    Using TensorFlow backend.
    WARNING

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Create Dataframe from temp data
oilDF_py = spark.table("temp")

# Select the 10 first Rows of data and print them
ten_oilDF_py = oilDF_py.limit(10)
ten_oilDF_py.show()

# Check number of data points
last_index = oilDF_py.count()
print("Number of data points: {}".format(last_index))

# Select the date of the last data point
print("Last data point: {}".format(np.array(oilDF_py.where(oilDF_py.index == last_index).select('time').collect()).item()))
```

<div class="output execute_result plain_result" execution_count="1">

    +-------------------+-----+--------------------+-----+
    |               time|close|          diff_close|index|
    +-------------------+-----+--------------------+-----+
    |2010-11-15 00:00:00| 86.6|-0.01000000000000...|    1|
    |2010-11-15 00:01:00| 86.6|                 0.0|    2|
    |2010-11-15 00:02:00|86.63|0.030000000000001137|    3|
    |2010-11-15 00:03:00|86.61|-0.01999999999999602|    4|
    |2010-11-15 00:05:00|86.61|                 0.0|    5|
    |2010-11-15 00:07:00| 86.6|-0.01000000000000...|    6|
    |2010-11-15 00:08:00|86.58|-0.01999999999999602|    7|
    |2010-11-15 00:09:00|86.58|                 0.0|    8|
    |2010-11-15 00:10:00|86.58|                 0.0|    9|
    |2010-11-15 00:12:00|86.57|-0.01000000000000...|   10|
    +-------------------+-----+--------------------+-----+

    Number of data points: 2523078
    Last data point: 2019-06-20 23:59:00

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Adapted from: https://github.com/kh-kim/stock_market_reinforcement_learning/blob/master/market_env.py


class MarketEnv(gym.Env):
    def __init__(self, full_data, start_date, end_date, episode_size=30*24*60, scope=60):
        self.episode_size = episode_size
        self.actions = ["LONG", "SHORT"] 
        self.action_space = gym.spaces.Discrete(len(self.actions))
        self.state_space = gym.spaces.Box(np.ones(scope) * -1, np.ones(scope))

        self.diff_close = np.array(full_data.filter(full_data["time"] > start_date).filter(full_data["time"] <= end_date).select('diff_close').collect())
        max_diff_close = np.max(self.diff_close)
        self.diff_close = self.diff_close*max_diff_close
        self.close = np.array(full_data.filter(full_data["time"] > start_date).filter(full_data["time"] <= end_date).select('close').collect())
        self.num_ticks_train = np.shape(self.diff_close)[0]

        self.scope = scope # N values to be included in a state vector
        self.time_index = self.scope  # start N steps in, to ensure that we have enough past values for history 
        self.episode_init_time = self.time_index  # initial time index of the episode


    def step(self, action):
        info = {'index': int(self.time_index), 'close': float(self.close[self.time_index])}
        self.time_index += 1
        self.state = self.diff_close[self.time_index - self.scope:self.time_index]
        self.reward = float( - (2 * action - 1) * self.state[-1] )
        
        # Check if done
        if self.time_index - self.episode_init_time > self.episode_size:
            self.done = True
        if self.time_index > self.diff_close.shape[0] - self.scope -1:
            self.done = True

        return self.state, self.reward, self.done, info

    def reset(self, random_starttime=True):
        self.done = False
        self.reward = 0.
        self.time_index = self.scope 
        self.state = self.diff_close[self.time_index - self.scope:self.time_index]
        
        if random_starttime:
            self.time_index += random.randint(0, self.num_ticks_train - self.scope)
        
        self.episode_init_time = self.time_index
        
        return self.state

    def seed(self):
        pass
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Adapted from: https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/4201196137758409/command/4201196137758410

class ExperienceReplay:
    def __init__(self, max_memory=100, discount=.9):
        self.max_memory = max_memory
        self.memory = list()
        self.discount = discount

    def remember(self, states, done):
        self.memory.append([states, done])
        if len(self.memory) > self.max_memory:
            del self.memory[0]

    def get_batch(self, model, batch_size=10):
        len_memory = len(self.memory)
        num_actions = model.output_shape[-1]

        env_dim = self.memory[0][0][0].shape[1]
        inputs = np.zeros((min(len_memory, batch_size), env_dim, 1))
        targets = np.zeros((inputs.shape[0], num_actions))
        for i, idx in enumerate(np.random.randint(0, len_memory, size=inputs.shape[0])):
            state_t, action_t, reward_t, state_tp1 = self.memory[idx][0]
            done = self.memory[idx][1]

            inputs[i:i + 1] = state_t
            # There should be no target values for actions not taken.
            targets[i] = model.predict(state_t)[0]
            Q_sa = np.max(model.predict(state_tp1)[0])
            if done: # if done is True
                targets[i, action_t] = reward_t
            else:
                # reward_t + gamma * max_a' Q(s', a')
                targets[i, action_t] = reward_t + self.discount * Q_sa
        return inputs, targets
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# Adapted from: https://dbc-635ca498-e5f1.cloud.databricks.com/?o=445287446643905#notebook/4201196137758409/command/4201196137758410

# RL parameters
epsilon = .5  # exploration
min_epsilon = 0.1
max_memory = 5000
batch_size = 512
discount = 0.8

# Environment parameters
num_actions = 2  # [long, short]
episodes = 500 # 100000
episode_size = 1 * 1 * 60  # roughly an hour worth of data in each training episode

# Define state sequence scope (approx. 1 hour)
sequence_scope = 60
input_shape = (batch_size, sequence_scope, 1)

# Create Q Network
hidden_size = 128
model = Sequential()
model.add(Conv1D(32, (5), strides=2, input_shape=input_shape[1:], activation='relu'))
model.add(MaxPool1D(pool_size=2, strides=1))
model.add(BatchNormalization())
model.add(Conv1D(32, (5), strides=1, activation='relu'))
model.add(MaxPool1D(pool_size=2, strides=1))
model.add(BatchNormalization())
model.add(Flatten())
model.add(Dense(hidden_size, activation='relu'))
model.add(BatchNormalization())
model.add(Dense(num_actions))
opt = optimizers.Adam(lr=0.01)
model.compile(loss='mse', optimizer=opt)

# Define training interval
start = datetime.datetime(2010, 11, 15, 0, 0)
end = datetime.datetime(2018, 12, 31, 23, 59)

# Initialize Environment
env = MarketEnv(oilDF_py, start, end, episode_size=episode_size, scope=sequence_scope)

# Initialize experience replay object
exp_replay = ExperienceReplay(max_memory=max_memory, discount=discount)
```

<div class="output execute_result plain_result" execution_count="1">

    WARNING:tensorflow:From /databricks/python/lib/python3.7/site-packages/tensorflow/python/framework/op_def_library.py:263: colocate_with (from tensorflow.python.framework.ops) is deprecated and will be removed in a future version.
    Instructions for updating:
    Colocations handled automatically by placer.
    /databricks/python/lib/python3.7/site-packages/gym/logger.py:30: UserWarning: <span class="ansi-yellow-fg">WARN: Box bound precision lowered by casting to float32</span>
      warnings.warn(colorize('%s: %s'%('WARN', msg % args), 'yellow'))

</div>

</div>

<div class="cell markdown">

Elephas
=======

https://github.com/danielenricocahall/elephas

Elephas is a third party library that allows to train distributed Keras models on Spark. To run a distributed training session, a Keras model is first declared and compiled on one (singular) master node. Then, copies of the Master model are serialized and shipped to an arbitrary number of worker nodes. Elephas uses RDD's internally to make the data dynamically available to the workers when required. After gradient computating and the update of the weights, the updated model parameters are pushed to the master model.

<img src=https://raw.githubusercontent.com/danielenricocahall/elephas/master/elephas.gif>

For updating the parameters of the master\_model, Elephas provides three modes, Synchronous, Asynchronous and HOGWILD (https://arxiv.org/abs/1106.5730).

Integrating Distributed model training in our RL-framework
----------------------------------------------------------

Elephas supports in the current version only supervised model training. We therefore opt to distribute the supervised training step based on the experience replay buffer and keep the surrounding for loops from the previeous RL-implementation.

### Training data conversion

Data must be provided as either RDD or pyspark dataframe (that will internally be converted to RDD's). A more elaborate pipeline might slice and evaluate replay buffer instances from the original dataframe, however, since most of our implementation expects numpy arrays, we convert the buffer to an RDD manually each step.

### Elephas SparkModel for retraining in the RL-loop

When Elephas finished its training epochs (here, one Experiancereplay buffer training in one of the RL-loop sweeps), the used processes get terminated. This leads to a crash when trying to retrain a already trained model. As a workaround, we initialize theelephas in each training step newly by using the keras model from the previous training step.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# elephas variables
ele_epochs = 10
ele_batchsize = 32
ele_verbose = 0
ele_valsplit = 0.1

# Train
returns = []
for e in range(1, episodes):
    loss = 0.
    counter = 0
    reward_sum = 0.
    done = False
    
    state = env.reset()
    input_t = state.reshape(1, sequence_scope, 1) 
    
    while not done:     
        counter += 1
        input_tm1 = input_t
        # get next action
        if np.random.rand() <= epsilon:
            action = np.random.randint(0, num_actions, size=1)
        else:
            q = model.predict(input_tm1)
            action = np.argmax(q[0])

        # apply action, get rewards and new state
        state, reward, done, info = env.step(action)
        reward_sum += reward
        input_t = state.reshape(1, sequence_scope, 1)         

        # store experience
        exp_replay.remember([input_tm1, action, reward, input_t], done)

        # adapt model
        inputs, targets = exp_replay.get_batch(model, batch_size=batch_size)
        
        # elephas calls for distributed gradient optimization
        train_rdd = to_simple_rdd(sc, inputs, targets)  # note that we provide the spark context sc (sc variable automatically set in databricks)
        
        spark_model = SparkModel(model, frequency='epoch', mode='asynchronous')  # 'asynchronous', 'hogwild' or 'synchronous'
        spark_model.fit(train_rdd, epochs=ele_epochs, batch_size=ele_batchsize, verbose=ele_verbose, validation_split=ele_valsplit)
        model = spark_model._master_network # hacky!
        
        loss += model.train_on_batch(inputs, targets)
    
    
    print("Episode {:03d}/{:d} | Average Loss {:.4f} | Cumulative Reward {:.4f}".format(e, episodes, loss / counter, reward_sum))
    epsilon = max(min_epsilon, epsilon * 0.99)
    returns.append(reward_sum)
```

</div>

<div class="cell markdown">

Notes to the elephas training
=============================

The pipeline in this notebook serves as a proof of concept to demonstrate how RL-training can be distributed on a spark cluster. During testing, we observed that the experience replay is a bottleneck during distributed model training, when comparing to running keras out-of the box in parallel.

Error that sometimes appears when running on databricks
-------------------------------------------------------

We notice occasional crashes in the distributed training at line 180 in https://github.com/danielenricocahall/elephas/blob/master/elephas/spark\_model.py, more precisely at `rdd.mapPartitions(worker.train).collect()`, with a `Py4JJavaError`. Restarting the cluster does not resolve the issue, however sometimes it was possible to re-run a training succesfully after a bit of time. We assume that it is connected with the jvm, but lacking precise insight regarding the nature of this bug.

</div>
