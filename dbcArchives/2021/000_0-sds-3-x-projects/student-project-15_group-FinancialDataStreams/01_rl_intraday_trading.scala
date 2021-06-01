// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Reinforcement Learning
// MAGIC 
// MAGIC In this project, our aim is to implement a Reinforcement Learning (RL) strategy for trading stocks. Adopting a learning-based approach, in particular using RL, entails several potential benefits over current approaches. Firstly, several ML methods allow learning-based pre-processing steps, such as convolutional layers which enable automatic feature extraction and detection, and may be used to focus the computation on the most relevant features. Secondly, constructing an end-to-end learning-based pipeline makes the prediction step implicit, and potentially reduces the problem complexity to predicting only certain aspects or features of the time series which are necessary for the control strategy, as opposed to attempting to predict the exact time series values. Thirdly, an end-to-end learning-based approach alleviates potential bounds of the step-wise modularization that a human-designed pipeline would entail, and allows the learning algorithm to automatically deduce the optimal strategy for utilizing any feature signal, in order to execute the most efficient control strategy.
// MAGIC 
// MAGIC The main idea behind RL algorithms is to learn by trial-and-error how to act optimally. An agent gathers experience by iteratively interacting with an environment. Starting in state S_t, the agent takes an action A_t and receives a reward R_t+1 as it moves to state S_t+1, as seen below ([source](https://upload.wikimedia.org/wikipedia/commons/d/da/Markov_diagram_v2.svg)). Using this experience, RL algorithms can learn either a value function or a policy directly. We learn the former, which can then be used to compute optimal actions, by chosing the action that maximizes the action value, Q. Specifically, we use the DQN -- Deep Q-Network -- algorithm to train an agent which trades Brent Crude Oil (BCOUSD) stocks, in order to maximize profit.
// MAGIC 
// MAGIC <img src=https://upload.wikimedia.org/wikipedia/commons/d/da/Markov_diagram_v2.svg width=600>

// COMMAND ----------

// Scala imports
import org.lamastex.spark.trendcalculus._
import spark.implicits._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import java.sql.Timestamp
import org.apache.spark.sql.expressions._

// COMMAND ----------

// MAGIC %md
// MAGIC ## Brent Crude Oil Dataset
// MAGIC 
// MAGIC The dataset consists of historical data starting from the *14th of October 2010* to the *21st of June 2019*. Since the data in the first and last day is incomplete, we remove it from the dataset. The BCUSD data is sampled approximatly every minute with a specific timestamp and registered in US dollars.
// MAGIC 
// MAGIC To read the BCUSD dataset, we use the same parsers provided by the [TrendCalculus](https://github.com/lamastex/spark-trend-calculus) library. This allows us to load the FX data into a Spark Dataset. The **fx1m** function returns the dataset as **TickerPoint** objects with values **x** and **y**, which are **time** and a **close** values respectively. The first consists of the name of the stock, the second is the timestamp of the data point and the latter consists of the value of the stock at the end of each 1 minute bin.
// MAGIC 
// MAGIC Finally we add the **index** column to facilitate retrieving values from the table, since there are gaps in the data meaning that not all minutes have an entry. Further a **diff_close** column was added, which consists of the relative difference between the **close** value at the current and the previous **time**. Note hat since **ticker** is always the same, we remove that column.

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

// MAGIC %md
// MAGIC ### Preparing the data in Python
// MAGIC 
// MAGIC Because the [TrendCalculus](https://github.com/lamastex/spark-trend-calculus) library we use is implemented in Scala and we want to do our implementation in Python, we have to make sure that the data loaded in Scala is correctly read in Python, before moving on. To that end, we select the first 10 data points and show them in a table. 
// MAGIC 
// MAGIC We can see that there are roughly **2.5 million data points** in the BCUSD dataset.

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

// MAGIC %md
// MAGIC ## RL Environment
// MAGIC 
// MAGIC In order to train RL agents, we first need to create the environment with which the agent will interact to gather experience. In our case, that consist of a stock market simulation which plays out historical data from the BCUSD dataset. This is valid, under the assumption that the trading on the part of our agent has no affect on the stock market. An RL problem can be formally defined by a Markov Decision Process (MDP). 
// MAGIC 
// MAGIC For our application, we have the following MDP:
// MAGIC - State, s: a window of **diff_close** values for a given **scope**, i.e. the current value and history leading up to it.
// MAGIC - Action, a: either **LONG** for buying stock, or **SHORT** for selling stock. Note that **PASS** is not required, since if stock is already owned, buying means holding and if stock is not owned then shorting means pass. 
// MAGIC - Reward, r: if a_t=**LONG**  r_t=s_t+1=**diff_close**; if a_t=**SHORT**  r_t=-s_t+1=-**diff_close**. Essentially, the reward is negative if we sell and the stock goes up or if we buy and the stock goes down in the next timestep. Conversely, the reward is positive if we buy and the stock goes up or if we sell and the stock goes down in the next timestep.
// MAGIC 
// MAGIC This environment is very simplified, with only binary actions. An alternative could be to use continuos actions to determine how much stock to buy or sell. However, since we aim to compare to TrendCalculus results which only predict reversals, these actions are more adequate. For the implementation, we used OpenAI Gym's formalism, which includes a **done** variable to indicate the end of an episode. In **MarketEnv**, by setting the **start_date** and **end_date** atttributes, we can select the part of the dataset we wish to use. Finally, the and **episode_size** parameter determines the episode size. An episode's starting point can be sampled at random or not, which is defined when calling **reset**.

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
// MAGIC states = []
// MAGIC actions = []
// MAGIC rewards = []
// MAGIC reward_sum = 0.
// MAGIC 
// MAGIC # Verify environment for 1 hour
// MAGIC start = datetime.datetime(2010, 11, 15, 0, 0)
// MAGIC end = datetime.datetime(2010, 11, 15, 1, 0)
// MAGIC 
// MAGIC env = MarketEnv(oilDF_py, start, end, episode_size=np.inf, scope=1)
// MAGIC state = env.reset(random_starttime=False)
// MAGIC done = False
// MAGIC while not done:
// MAGIC     states.append(state[-1])
// MAGIC     # Take random actions
// MAGIC     action = env.action_space.sample()
// MAGIC     actions.append(action)
// MAGIC     state, reward, done, info = env.step(action)
// MAGIC     rewards.append(reward)
// MAGIC     reward_sum += reward
// MAGIC print("Return = {}".format(reward_sum))

// COMMAND ----------

// MAGIC %python
// MAGIC # Plot samples
// MAGIC timesteps = np.linspace(1,len(states),len(states))
// MAGIC longs = np.argwhere(np.asarray(actions) ==  0)
// MAGIC shorts = np.argwhere(np.asarray(actions) ==  1)
// MAGIC states = np.asarray(states)
// MAGIC fig, ax = plt.subplots(2, 1, figsize=(16, 8))
// MAGIC ax[0].grid(True)
// MAGIC ax[0].plot(timesteps, states, label='diff_close')
// MAGIC ax[0].plot(timesteps[longs], states[longs].flatten(), '*g', markersize=12, label='long')
// MAGIC ax[0].plot(timesteps[shorts], states[shorts].flatten(), '*r', markersize=12, label='short')
// MAGIC ax[0].set_ylabel("(s,a)")
// MAGIC ax[0].set_xlabel("Timestep")
// MAGIC ax[0].set_xlim(1,len(states))
// MAGIC ax[0].set_xticks(np.arange(1, len(states), 1.0))
// MAGIC ax[0].legend()
// MAGIC ax[1].grid(True)
// MAGIC ax[1].plot(timesteps, rewards, 'o-r')
// MAGIC ax[1].set_ylabel("r")
// MAGIC ax[1].set_xlabel("Timestep")
// MAGIC ax[1].set_xlim(1,len(states))
// MAGIC ax[1].set_xticks(np.arange(1, len(states), 1.0))
// MAGIC plt.tight_layout()
// MAGIC display(fig)

// COMMAND ----------

// MAGIC %md
// MAGIC To illustarte how the reward works, we can look at timestep 9, where there is a large negative reward because the agent shorted (red) and at step 10 the stock went up. Conversely, at timestep 27 there is a large positive reward, since the agent decided to short and in the next timestep (28), the stock went down.

// COMMAND ----------

// MAGIC %md
// MAGIC ## DQN Algorithm
// MAGIC 
// MAGIC Since we have discrete actions, we can use Q-learning to train our agent. Specifically we use the DQN algorithm with Experience Replay, which was first described in DeepMind's: [Playing Atari with Deep Reinforcement Learning](https://arxiv.org/pdf/1312.5602.pdf). The algorithm is described below, where equation [3], refers to the gradient: <img src="https://imgur.com/eGhNC9m.png" width=650>
// MAGIC 
// MAGIC <img src="https://imgur.com/mvopoh8.png" width=800>

// COMMAND ----------

// MAGIC %python
// MAGIC # Adapted from: /scalable-data-science/000_6-sds-3-x-dl/063_DLbyABr_07-ReinforcementLearning
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

// MAGIC %md
// MAGIC ## Training RL agent
// MAGIC 
// MAGIC In order to train the RL agent, we use the data from 2014 to 2018, leaving the data from 2019 for testing. RL implementations are quite difficult to train, due to the large amount of parameters which need to be tuned. We have spent little time seraching for better hyperparameters as this was beyond the scope of the course. We have picked parameters based on a similar implementation of RL for trading, however we have designed an new Q-network, since the state is different in our implementation. Sine we are dealing wih sequential data, we could have opted for an RNN, however 1-dimensional CNNs are also a common choice which is less computationally heavy.

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
// MAGIC         loss += model.train_on_batch(inputs, targets)
// MAGIC     
// MAGIC     
// MAGIC     print("Episode {:03d}/{:d} | Average Loss {:.4f} | Cumulative Reward {:.4f}".format(e, episodes, loss / counter, reward_sum))
// MAGIC     epsilon = max(min_epsilon, epsilon * 0.99)
// MAGIC     returns.append(reward_sum)

// COMMAND ----------

// MAGIC %python
// MAGIC # Plotting training results
// MAGIC fig, ax = plt.subplots(figsize=(16, 8))
// MAGIC ax.plot(returns)
// MAGIC ax.set_ylabel("Return")
// MAGIC ax.set_xlabel("Episode")
// MAGIC display(fig)

// COMMAND ----------

// MAGIC %md
// MAGIC We have trained our model for 500 episodes and the returns are plotted above. Note that the loss was still quite high at the end of training, which indicates that the algorithm hasn't converged. A possible explanation for this is that RL algorithms typically require significantly more steps to converge. Further, considering the size of the tranining dataset, the neural network used is very small. Besides that, DQN is known to be quite unstable and prone to diverge, which is why several new versions of this algorithm have been proposed since it was first introduced. A very common implementation consists of the Double DQN, which introduced a target Q-network used to compute the actions, which is updated at a lower rate than the main Q-network. In our implementation, the max operator uses the same network both to select and to evaluate an action. This may lead to wrongly selecting overestimated values. Having a separate target network can help prevent this, by decoupling the selection from the evaluation.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Testing RL agent
// MAGIC 
// MAGIC In order to test our agent, we select the whole data from the 1st of January 2019, which wasn't included during training. 

// COMMAND ----------

// MAGIC %python
// MAGIC done = False
// MAGIC states = []
// MAGIC actions = []
// MAGIC rewards = []
// MAGIC reward_sum = 0.
// MAGIC 
// MAGIC # Define testing interval, January 2019
// MAGIC start = datetime.datetime(2019, 1, 1, 0, 0)
// MAGIC end = datetime.datetime(2019, 1, 1, 23, 59)
// MAGIC 
// MAGIC # Test learned model
// MAGIC env = MarketEnv(oilDF_py, start, end, episode_size=np.inf, scope=sequence_scope)
// MAGIC state = env.reset(random_starttime=False)
// MAGIC input_t = state.reshape(1, sequence_scope, 1)
// MAGIC while not done:    
// MAGIC     states.append(state[-1])
// MAGIC     q = model.predict(input_t)
// MAGIC     action = np.argmax(q[0])
// MAGIC     actions.append(action)
// MAGIC     state, reward, done, info = env.step(action)
// MAGIC     rewards.append(reward)
// MAGIC     reward_sum += reward
// MAGIC     input_t = state.reshape(1, sequence_scope, 1)      
// MAGIC print("Return = {}".format(reward_sum))

// COMMAND ----------

// MAGIC %python
// MAGIC # Plotting testing results
// MAGIC timesteps = np.linspace(1,len(states),len(states))
// MAGIC longs = np.argwhere(np.asarray(actions) ==  0)
// MAGIC shorts = np.argwhere(np.asarray(actions) ==  1)
// MAGIC states = np.asarray(states)
// MAGIC fig, ax = plt.subplots(2, 1, figsize=(16, 8))
// MAGIC ax[0].grid(True)
// MAGIC ax[0].plot(timesteps, states, label='diff_close')
// MAGIC ax[0].plot(timesteps[longs], states[longs].flatten(), '*g', markersize=12, label='long')
// MAGIC ax[0].plot(timesteps[shorts], states[shorts].flatten(), '*r', markersize=12, label='short')
// MAGIC ax[0].set_ylabel("(s,a)")
// MAGIC ax[0].set_xlabel("Timestep")
// MAGIC ax[0].set_xlim(1,len(states))
// MAGIC ax[0].legend()
// MAGIC ax[1].grid(True)
// MAGIC ax[1].plot(timesteps, rewards, 'o-r')
// MAGIC ax[1].set_ylabel("r")
// MAGIC ax[1].set_xlabel("Timestep")
// MAGIC ax[1].set_xlim(1,len(states))
// MAGIC plt.tight_layout()
// MAGIC display(fig)

// COMMAND ----------

// MAGIC %md
// MAGIC We can see that the policy converged to always shorting, meaning that the agent never buys any stock. While abstaining from investments in fossil fuels may be good advice, the result is not very useful for our intended application. Nevertherless, reaching a successful automatic intraday trading bot in the short time we spent implementing this project would be a high bar. After all, this is more or less the holy grail of computational economy.

// COMMAND ----------

// MAGIC %md
// MAGIC # Summary and Outlook
// MAGIC 
// MAGIC In this project we have trained and tested an RL agent, using DQN for intraday trading. We started by processing the data and adding a **diff_close** column which contains the differece of the closing stock value between two timesteps. We then implemented our own Gym environment **MarketEnv**, to be able to read data from the BCUSD dataset and feed it to an agents, as weel as compute the reward given the agent's action. We used a DQN implementation, to train a convolutional Q-Network. Since we are using TensorFlow in the background, the training is automatically scaled to use all cpu cores available (see [here](https://www.xspdf.com/resolution/52582340.html)). Finally, we have tested our agent on new data, and concluded that more work needs to be put into making the algorithm convege. 
// MAGIC 
// MAGIC As future work we believe we can still improve the state and reward definitions. For the state, used a window of **close_diff** values as our state definiion. However, augmenting the state with longer term trends computed by the TrendCalculus algorithm could yield significant improvements. The TrendCalculus algorithm provides an analytical framework effective for identifying trends in historical price data, including [trend pattern analysis](https://lamastex.github.io/spark-trend-calculus-examples/notebooks/db/01trend-calculus-showcase.html) and [prediction of trend changes](https://lamastex.github.io/spark-trend-calculus-examples/notebooks/db/03streamable-trend-calculus-estimators.html). Below we present the idea behind TrendCalculus and how it relates to **close_diff**, as well as a few ideas on how it could be used for our application.
// MAGIC 
// MAGIC ## Trend Calculus
// MAGIC 
// MAGIC Taken from: [https://github.com/lamastex/spark-trend-calculus-examples](https://github.com/lamastex/spark-trend-calculus-examples)
// MAGIC 
// MAGIC Trend Calculus is an algorithm invented by Andrew Morgan that is used to find trend changes in a time series (see [here](https://github.com/bytesumo/TrendCalculus/blob/master/HowToStudyTrends_v1.03.pdf)). It works by grouping the observations in the time series into windows and defining a trend upwards as “higher highs and higher lows” compared to the previous window. A downwards trend is similarly defined as “lower highs and lower lows”.
// MAGIC 
// MAGIC <img src=https://lamastex.github.io/spark-trend-calculus-examples/notebooks/db/images/HigherHighHigherLow.png width=300>
// MAGIC 
// MAGIC If there is a higher high and lower low (or lower high and higher low), no trend is detected. This is solved by introducing intermediate windows that split the non-trend into two trends, ensuring that every point can be labeled with either an up or down trend.
// MAGIC 
// MAGIC <img src=https://lamastex.github.io/spark-trend-calculus-examples/notebooks/db/images/OuterInnerBars.png  width=600>
// MAGIC 
// MAGIC When the trends have been calculated for all windows, the points where the trends change sign are labeled as reversals. If the reversal is from up to down, the previous high is the reversal point and if the reversal is from down to up, the previous low is the reversal. This means that the reversals always are the appropriate extrema (maximum for up to down, minimum for down to up).
// MAGIC 
// MAGIC <img src=https://lamastex.github.io/spark-trend-calculus-examples/notebooks/db/images/trendReversals.png width=600>
// MAGIC 
// MAGIC The output of the algorithm is a time series consisting of all the labelled reversal points. It is therefore possible to use this as the input for another run of the Trend Calculus algorithm, finding more long term trends. This can be seen when TrendCalculus is applied to the BCUSD datset, shown in column **reversal1** of the table below.

// COMMAND ----------

val windowSize = 2
val numReversals = 1 // we look at 1 iteration of the algorithm. 

val dfWithReversals = new TrendCalculus2(oilDS, windowSize, spark).nReversalsJoinedWithMaxRev(numReversals)
display(dfWithReversals)

val windowSpec = Window.orderBy("x")
val dfWithReversalsDiff = dfWithReversals 
.withColumn("diff_close", $"y" - when((lag("y", 1).over(windowSpec)).isNull, 0).otherwise(lag("y", 1).over(windowSpec)))

// Store loaded data as temp view, to be accessible in Python
dfWithReversalsDiff.createOrReplaceTempView("temp")

// COMMAND ----------

// MAGIC %md
// MAGIC In conjunction with TrendCalculus, a complete automatic trading pipeline can be constructed, consisting of (i) trend analysis with TrendCalculus (ii) time series prediction and (iii) control, i.e. buy or sell. Implementing and evaluating a pipeline such as the one outlined in the aforementioned steps is left as a suggestion for future work, and it is of particular interest to compare the performance of such a method to a learning-based one.
// MAGIC 
// MAGIC Below we show that sign(**diff_close**) is equivalent to sign of the output of a single iteration of TrendCalculus with window size 2, over our **scope**. A possible improvement of our algorithm would be to use TrendCalculus to compute long term trends from historical data and include it on our state definition. This way, if for example the agent observes a long term downward trend, then it can be encouraged to buy stock since it is bound to bounce up again. 

// COMMAND ----------

// MAGIC %python
// MAGIC # Taken from: https://lamastex.github.io/spark-trend-calculus-examples/notebooks/db/01trend-calculus-showcase.html
// MAGIC 
// MAGIC # Create Dataframe from temp data
// MAGIC fullDS = spark.table("temp")
// MAGIC fullTS = fullDS.select("x", "y", "reversal1", "diff_close").collect()
// MAGIC 
// MAGIC startDate = datetime.datetime(2019, 1, 1, 1, 0) # first window used as scope
// MAGIC endDate = datetime.datetime(2019, 1, 1, 23, 59)
// MAGIC TS = [row for row in fullTS if startDate <= row['x'] and row['x'] <= endDate]
// MAGIC 
// MAGIC allData = {'x': [row['x'] for row in TS], 'close': [row['y'] for row in TS], 'diff_close': [row['diff_close'] for row in TS], 'reversal1': [row['reversal1'] for row in TS]}
// MAGIC 
// MAGIC # Plot reversals
// MAGIC close = np.asarray(allData['close'])
// MAGIC diff_close = np.asarray(allData['diff_close'])
// MAGIC timesteps = np.linspace(1,len(diff_close),len(diff_close))
// MAGIC revs = np.asarray(allData['reversal1'])
// MAGIC pos_rev_ind = np.argwhere(revs ==  1)
// MAGIC neg_rev_ind = np.argwhere(revs ==  -1)
// MAGIC fig, ax = plt.subplots(2, 1, figsize=(16, 8))
// MAGIC ax[0].grid(True)
// MAGIC ax[0].plot(timesteps, close, label='close')
// MAGIC ax[0].plot(timesteps[pos_rev_ind], close[pos_rev_ind].flatten(), '*g', markersize=12, label='+ reversal')
// MAGIC ax[0].plot(timesteps[neg_rev_ind], close[neg_rev_ind].flatten(), '*r', markersize=12, label='- reversal')
// MAGIC ax[0].set_ylabel("close")
// MAGIC ax[0].set_xlabel("Timestep")
// MAGIC ax[0].set_xlim(1,len(close))
// MAGIC ax[0].legend()
// MAGIC ax[1].grid(True)
// MAGIC ax[1].plot(timesteps, diff_close, label='diff_close')
// MAGIC ax[1].plot(timesteps[pos_rev_ind], diff_close[pos_rev_ind].flatten(), '*g', markersize=12, label='+ reversal')
// MAGIC ax[1].plot(timesteps[neg_rev_ind], diff_close[neg_rev_ind].flatten(), '*r', markersize=12, label='- reversal')
// MAGIC ax[1].set_ylabel("diff_close")
// MAGIC ax[1].set_xlabel("Timestep")
// MAGIC ax[1].set_xlim(1,len(diff_close))
// MAGIC plt.tight_layout()
// MAGIC display(fig)