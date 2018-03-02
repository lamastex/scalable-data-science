# Databricks notebook source
# MAGIC %md
# MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
# MAGIC 
# MAGIC This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
# MAGIC This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. 
# MAGIC A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

# COMMAND ----------

# MAGIC %md
# MAGIC # Playing Games and Driving Cars: Reinforcement Learning
# MAGIC 
# MAGIC <img src="https://i.imgur.com/VKEJsy6.jpg" width=900>
# MAGIC 
# MAGIC ## In a Nutshell
# MAGIC 
# MAGIC __In reinforcement learning, an agent takes multiple actions, and the positive or negative outcome of those actions serves as a loss function for subsequent training.__
# MAGIC 
# MAGIC ## Training an Agent
# MAGIC 
# MAGIC What is an agent?
# MAGIC 
# MAGIC How is training an agent different from training the models we've used so far?
# MAGIC 
# MAGIC Most things stay the same, and we can use all of the knowledge we've built:
# MAGIC 
# MAGIC * We can use any or all of the network models, including feed-forward, convolutional, recurrent, and combinations of those.
# MAGIC * We will still train in batches using some variant of gradient descent
# MAGIC * As before, the model will ideally learn a complex non-obvious function of many parameters
# MAGIC 
# MAGIC A few things change ... well, not really change, but "specialize":
# MAGIC 
# MAGIC * The inputs may start out as entire frames (or frame deltas) of a video feed
# MAGIC     * We may feature engineer more explicitly (or not)
# MAGIC * The ouputs may be a low-cardinality set of categories that represent actions (e.g., direction of a digital joystick, or input to a small number of control systems)
# MAGIC * We may model state explicitly (outside the network) as well as implicitly (inside the network)
# MAGIC * The function we're learning is one which will "tell our agent what to do" or -- assuming there is no disconnect between knowing what to do and doing it, the function will essentially be the agent
# MAGIC * __The loss function depends on the outcome of the game, and the game requires many actions to reach an outcome, and so requires some slightly different approaches from the ones we've used before.__

# COMMAND ----------

# MAGIC %md 
# MAGIC # Principal Approaches: Deep Q-Learning and Policy Gradient Learning
# MAGIC 
# MAGIC * Policy Gradient is straightforward and shows a lot of research promise, but can be quite difficult to use. The challenge is less in the math, code, or concepts, and more in terms of effective training. We'll look very briefly at PG.
# MAGIC 
# MAGIC 
# MAGIC * Deep Q-Learning is more constrained and a little more complex mathematically. These factors would seem to cut against the use of DQL, but they allow for relatively fast and effective training, so they are very widely used. We'll go deeper into DQL and work with an example.
# MAGIC 
# MAGIC There are, of course, many variants on these as well as some other strategies.

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Policy Gradient Learning
# MAGIC 
# MAGIC With Policy Gradient Learning, we directly try to learn a "policy" function that selects a (possibly continuous-valued) move for an agent to make given the current state of the "world."
# MAGIC 
# MAGIC We want to maximize total discounted future reward, but we do not need discrete actions to take or a model that tells us a specific "next reward."
# MAGIC 
# MAGIC Instead, we can make fine-grained moves and we can collect all the moves that lead to a reward, and then apply that reward to all of them.
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC > __ASIDE:__ The term *gradient* here comes from a formula which indicates the gradient (or steepest direction) to improve the policy parameters with respect to the loss function. That is, which direction to adjust the parameters to maximize improvement in expected total reward.
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC In some sense, this is a more straightforward, direct approach than the other approach we'll work with, Deep Q-Learning. 
# MAGIC 
# MAGIC ### Challenges with Policy Gradients
# MAGIC 
# MAGIC Policy gradients, despite achieving remarkable results, are a form of brute-force solution.
# MAGIC 
# MAGIC Thus they require a large amount of input data and extraordinary amounts of training time.
# MAGIC 
# MAGIC Some of these challenges come down to the *credit assignment problem* -- properly attributing reward to actions in the past which may (or may not) be responsible for the reward -- and thus some mitigations include more complex reward functions, adding more frequent reward training into the system, and adding domain knowledge to the policy, or adding an entire separate network, called a "critic network" to learn to provide feedback to the actor network.
# MAGIC 
# MAGIC Another challenge is the size of the search space, and tractable approaches to exploring it.
# MAGIC 
# MAGIC PG is challenging to use in practice, though there are a number of "tricks" in various publications that you can try.
# MAGIC 
# MAGIC ### Next Steps
# MAGIC 
# MAGIC * Great post by Andrej Karpathy on policy gradient learning: http://karpathy.github.io/2016/05/31/rl/
# MAGIC 
# MAGIC * A nice first step on policy gradients with real code: *Using Keras and Deep Deterministic Policy Gradient to play TORCS*: https://yanpanlau.github.io/2016/10/11/Torcs-Keras.html
# MAGIC 
# MAGIC If you'd like to explore a variety of reinforcement learning techniques, Mattias Lappert at the Karlsruhe Institute of Technology, has created an add-on framework for Keras that implements a variety of state-of-the-art RL techniques, including discussed today.
# MAGIC 
# MAGIC His framework, KerasRL is at https://github.com/matthiasplappert/keras-rl and includes examples that integrate with OpenAI gym.

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Deep Q-Learning
# MAGIC 
# MAGIC Deep Q-Learning is deep learning applied to "Q-Learning." 
# MAGIC 
# MAGIC So what is Q-Learning?
# MAGIC 
# MAGIC Q-Learning is a model that posits a map for optimal actions for each possible state in a game.
# MAGIC 
# MAGIC Specifically, given a state and an action, there is a "Q-function" that provides the value or quality (the Q stands for quality) of that action taken in that state.
# MAGIC 
# MAGIC So, if an agent is in state s, choosing an action could be as simple as looking at Q(s, a) for all a, and choosing the highest "quality value" -- aka <img src="https://i.imgur.com/RerRQzk.png" width=110>
# MAGIC 
# MAGIC There are some other considerations, such as explore-exploit tradeoff, but let's focus on this Q function.
# MAGIC 
# MAGIC In small state spaces, this function can be represented as a table, a bit like basic strategy blackjack tables.
# MAGIC 
# MAGIC <img src="http://i.imgur.com/rfxLSls.png" width=400>
# MAGIC 
# MAGIC Even a simple Atari-style video game may have hundreds of thousands of states, though. This is where the neural network comes in.
# MAGIC 
# MAGIC __What we need is a way to learn a Q function, when we don't know what the error of a particular move is, since the error (loss) may be dependent on many future actions and can also be non-deterministic (e.g., if there are randomly generated enemies or conditions in the game).__
# MAGIC 
# MAGIC The tricks -- or insights -- here are:
# MAGIC 
# MAGIC [1] Model the total future reward -- what we're really after -- as a recursive calculation from the immediate reward (*r*) and future outcomes:
# MAGIC 
# MAGIC <img src="https://i.imgur.com/ePXoQfR.png" width=250>
# MAGIC     
# MAGIC * \\({\gamma}\\) is a "discount factor" on future reward
# MAGIC * Assume the game terminates or "effectively terminates" to make the recursion tractable
# MAGIC * This equation is a simplified case of the Bellman Equation
# MAGIC     
# MAGIC [2] Assume that if you iteratively run this process starting with an arbitrary Q model, and you train the Q model with actual outcomes, your Q model will eventually converge toward the "true" Q function
# MAGIC * This seems intuitively to resemble various Monte Carlo sampling methods (if that helps at all)
# MAGIC 
# MAGIC #### As improbable as this might seem at first for teaching an agent a complex game or task, it actually works, and in a straightforward way.
# MAGIC 
# MAGIC How do we apply this to our neural network code?
# MAGIC 
# MAGIC Unlike before, when we called "fit" to train a network automatically, here we'll need some interplay between the agent's behavior in the game and the training. That is, we need the agent to play some moves in order to get actual numbers to train with. And as soon as we have some actual numbers, we want to do a little training with them right away so that our Q function improves. So we'll alternate one or more in-game actions with a manual call to train on a single batch of data.
# MAGIC 
# MAGIC The algorithm looks like this (credit for the nice summary to Tambet Matiisen; read his longer explanation at https://neuro.cs.ut.ee/demystifying-deep-reinforcement-learning/ for review):
# MAGIC 
# MAGIC 1. Do a feedforward pass for the current state s to get predicted Q-values for all actions.
# MAGIC 2. Do a feedforward pass for the next state s′ and calculate maximum over all network outputs <img src="https://i.imgur.com/3QiH4Z1.png" width=110>
# MAGIC 3. Set Q-value target for action a to <img src="https://i.imgur.com/2RNmkl6.png" width=140> (use the max calculated in step 2). For all other actions, set the Q-value target to the same as originally returned from step 1, making the error 0 for those outputs.
# MAGIC 4. Update the weights using backpropagation.
# MAGIC 
# MAGIC If there is "reward" throughout the game, we can model the loss as 
# MAGIC 
# MAGIC <img src="https://i.imgur.com/OojwPbJ.png" width=350>
# MAGIC 
# MAGIC If the game is win/lose only ... most of the r's go away and the entire loss is based on a 0/1 or -1/1 score at the end of a game.
# MAGIC 
# MAGIC ### Practical Consideration 1: Experience Replay
# MAGIC 
# MAGIC To improve training, we cache all (or as much as possible) of the agent's state/move/reward/next-state data. Then, when we go to perform a training run, we can build a batch out of a subset of all previous moves. This provides diversity in the training data, whose value we discussed earlier.
# MAGIC 
# MAGIC ### Practical Consideration 2: Explore-Exploit Balance
# MAGIC 
# MAGIC In order to add more diversity to the agent's actions, we set a threshold ("epsilon") which represents the probability that the agent ignores its experience-based model and just chooses a random action. This also add diversity, by preventing the agent from taking an overly-narrow, 100% greedy (best-perfomance-so-far) path.
# MAGIC 
# MAGIC ### Let's Look at the Code!
# MAGIC 
# MAGIC Reinforcement learning code examples are a bit more complex than the other examples we've seen so far, because in the other examples, the data sets (training and test) exist outside the program as assets (e.g., the MNIST digit data).
# MAGIC 
# MAGIC In reinforcement learning, the training and reward data come from some environment that the agent is supposed to learn. Typically, the environment is simulated by local code, or *represented by local code* even if the real environment is remote or comes from the physical world via sensors. 
# MAGIC 
# MAGIC #### So the code contains not just the neural net and training logic, but part (or all) of a game world itself.
# MAGIC 
# MAGIC One of the most elegant small, but complete, examples comes courtesy of former Ph.D. researcher (Univ. of Florida) and Apple rockstar Eder Santana. It's a simplified catch-the-falling-brick game (a bit like Atari Kaboom! but even simpler) that nevertheless is complex enough to illustrate DQL and to be impressive in action.
# MAGIC 
# MAGIC When we're done, we'll basically have a game, and an agent that plays, which run like this:
# MAGIC 
# MAGIC <img src="http://i.imgur.com/PB6OgNF.gif">

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC First, let's get familiar with the game environment itself, since we'll need to see how it works, before we can focus on the reinforcement learning part of the program.

# COMMAND ----------

class Catch(object):
    def __init__(self, grid_size=10):
        self.grid_size = grid_size
        self.reset()

    def _update_state(self, action):
        """
        Input: action and states
        Ouput: new states and reward
        """
        state = self.state
        if action == 0:  # left
            action = -1
        elif action == 1:  # stay
            action = 0
        else:
            action = 1  # right
        f0, f1, basket = state[0]
        new_basket = min(max(1, basket + action), self.grid_size-1)
        f0 += 1
        out = np.asarray([f0, f1, new_basket])
        out = out[np.newaxis]

        assert len(out.shape) == 2
        self.state = out

    def _draw_state(self):
        im_size = (self.grid_size,)*2
        state = self.state[0]
        canvas = np.zeros(im_size)
        canvas[state[0], state[1]] = 1  # draw fruit
        canvas[-1, state[2]-1:state[2] + 2] = 1  # draw basket
        return canvas

    def _get_reward(self):
        fruit_row, fruit_col, basket = self.state[0]
        if fruit_row == self.grid_size-1:
            if abs(fruit_col - basket) <= 1:
                return 1
            else:
                return -1
        else:
            return 0

    def _is_over(self):
        if self.state[0, 0] == self.grid_size-1:
            return True
        else:
            return False

    def observe(self):
        canvas = self._draw_state()
        return canvas.reshape((1, -1))

    def act(self, action):
        self._update_state(action)
        reward = self._get_reward()
        game_over = self._is_over()
        return self.observe(), reward, game_over

    def reset(self):
        n = np.random.randint(0, self.grid_size-1, size=1)
        m = np.random.randint(1, self.grid_size-2, size=1)
        self.state = np.asarray([0, n, m])[np.newaxis]

# COMMAND ----------

# MAGIC %md 
# MAGIC Next, let's look at the network itself -- it's super simple, so we can get that out of the way too:
# MAGIC 
# MAGIC ```
# MAGIC model = Sequential()
# MAGIC model.add(Dense(hidden_size, input_shape=(grid_size**2,), activation='relu'))
# MAGIC model.add(Dense(hidden_size, activation='relu'))
# MAGIC model.add(Dense(num_actions))
# MAGIC model.compile(sgd(lr=.2), "mse")
# MAGIC ```

# COMMAND ----------

# MAGIC %md 
# MAGIC Note that the output layer has `num_actions` neurons. 
# MAGIC 
# MAGIC We are going to implement the training target as 
# MAGIC 
# MAGIC * the estimated reward for the one action taken when the game doesn't conclude, or 
# MAGIC * error/reward for the specific action that loses/wins a game
# MAGIC 
# MAGIC In any case, we only train with an error/reward for actions the agent actually chose. We neutralize the hypothetical rewards for other actions, as they are not causally chained to any ground truth.
# MAGIC 
# MAGIC Next, let's zoom in on at the main game training loop:
# MAGIC 
# MAGIC ```
# MAGIC win_cnt = 0
# MAGIC for e in range(epoch):
# MAGIC     loss = 0.
# MAGIC     env.reset()
# MAGIC     game_over = False
# MAGIC     # get initial input
# MAGIC     input_t = env.observe()
# MAGIC 
# MAGIC     while not game_over:
# MAGIC         input_tm1 = input_t
# MAGIC         # get next action
# MAGIC         if np.random.rand() <= epsilon:
# MAGIC             action = np.random.randint(0, num_actions, size=1)
# MAGIC         else:
# MAGIC             q = model.predict(input_tm1)
# MAGIC             action = np.argmax(q[0])
# MAGIC 
# MAGIC         # apply action, get rewards and new state
# MAGIC         input_t, reward, game_over = env.act(action)
# MAGIC         if reward == 1:
# MAGIC             win_cnt += 1
# MAGIC 
# MAGIC         # store experience
# MAGIC         exp_replay.remember([input_tm1, action, reward, input_t], game_over)
# MAGIC 
# MAGIC         # adapt model
# MAGIC         inputs, targets = exp_replay.get_batch(model, batch_size=batch_size)
# MAGIC 
# MAGIC         loss += model.train_on_batch(inputs, targets)
# MAGIC     print("Epoch {:03d}/{:d} | Loss {:.4f} | Win count {}".format(e, epoch - 1, loss, win_cnt))
# MAGIC ```

# COMMAND ----------

# MAGIC %md 
# MAGIC The key bits are:
# MAGIC 
# MAGIC * Choose an action
# MAGIC * Act and collect the reward and new state
# MAGIC * Cache previous state, action, reward, and new state in "Experience Replay" buffer
# MAGIC * Ask buffer for a batch of action data to train on
# MAGIC * Call `model.train_on_batch` to perform one training batch
# MAGIC 
# MAGIC Last, let's dive into where the actual Q-Learning calculations occur, which happen, in this code to be in the `get_batch` call to the experience replay buffer object:

# COMMAND ----------

class ExperienceReplay(object):
    def __init__(self, max_memory=100, discount=.9):
        self.max_memory = max_memory
        self.memory = list()
        self.discount = discount

    def remember(self, states, game_over):
        # memory[i] = [[state_t, action_t, reward_t, state_t+1], game_over?]
        self.memory.append([states, game_over])
        if len(self.memory) > self.max_memory:
            del self.memory[0]

    def get_batch(self, model, batch_size=10):
        len_memory = len(self.memory)
        num_actions = model.output_shape[-1]
        env_dim = self.memory[0][0][0].shape[1]
        inputs = np.zeros((min(len_memory, batch_size), env_dim))
        targets = np.zeros((inputs.shape[0], num_actions))
        for i, idx in enumerate(np.random.randint(0, len_memory,
                                                  size=inputs.shape[0])):
            state_t, action_t, reward_t, state_tp1 = self.memory[idx][0]
            game_over = self.memory[idx][1]

            inputs[i:i+1] = state_t
            # There should be no target values for actions not taken.
            # Thou shalt not correct actions not taken #deep
            targets[i] = model.predict(state_t)[0]
            Q_sa = np.max(model.predict(state_tp1)[0])
            if game_over:  # if game_over is True
                targets[i, action_t] = reward_t
            else:
                # reward_t + gamma * max_a' Q(s', a')
                targets[i, action_t] = reward_t + self.discount * Q_sa
        return inputs, targets

# COMMAND ----------

# MAGIC %md 
# MAGIC The key bits here are:
# MAGIC 
# MAGIC * Set up "blank" buffers for a set of items of the requested batch size, or all memory, whichever is less (in case we don't have much data yet)
# MAGIC     * one buffer is `inputs` -- it will contain the game state or screen before the agent acted
# MAGIC     * the other buffer is `targets` -- it will contain a vector of rewards-per-action (with just one non-zero entry, for the action the agent actually took)
# MAGIC * Based on that batch size, randomly select records from memory
# MAGIC * For each of those cached records (which contain initial state, action, next state, and reward),
# MAGIC     * Insert the initial game state into the proper place in the `inputs` buffer
# MAGIC     * If the action ended the game then:
# MAGIC         * Insert a vector into `targets` with the real reward in the position of the action chosen
# MAGIC     * Else (if the action did not end the game):
# MAGIC         * Insert a vector into `targets` with the following value in the position of the action taken:
# MAGIC             * *(real reward)*
# MAGIC             * __+__ *(discount factor)(predicted-reward-for-best-action-in-the-next-state)*
# MAGIC         * __Note__: although the Q-Learning formula is implemented in the general version here, this specific game only produces reward when the game is over, so the "real reward" in this branch will always be zero
# MAGIC         

# COMMAND ----------

mkdir /dbfs/keras_rl

# COMMAND ----------

mkdir /dbfs/keras_rl/images

# COMMAND ----------

# MAGIC %md 
# MAGIC Ok, now let's run the main training script and teach Keras to play Catch:

# COMMAND ----------

import json
import numpy as np
from keras.models import Sequential
from keras.layers.core import Dense
from keras.optimizers import sgd
import collections

epsilon = .1  # exploration
num_actions = 3  # [move_left, stay, move_right]
epoch = 400
max_memory = 500
hidden_size = 100
batch_size = 50
grid_size = 10

model = Sequential()
model.add(Dense(hidden_size, input_shape=(grid_size**2,), activation='relu'))
model.add(Dense(hidden_size, activation='relu'))
model.add(Dense(num_actions))
model.compile(loss='mse', optimizer='adam')

# Define environment/game
env = Catch(grid_size)

# Initialize experience replay object
exp_replay = ExperienceReplay(max_memory=max_memory)

# Train
win_cnt = 0
last_ten = collections.deque(maxlen=10)

for e in range(epoch):
    loss = 0.
    env.reset()
    game_over = False
    # get initial input
    input_t = env.observe()

    while not game_over:
        input_tm1 = input_t
        # get next action
        if np.random.rand() <= epsilon:
            action = np.random.randint(0, num_actions, size=1)
        else:
            q = model.predict(input_tm1)
            action = np.argmax(q[0])

        # apply action, get rewards and new state
        input_t, reward, game_over = env.act(action)
        if reward == 1:
            win_cnt += 1            

        # store experience
        exp_replay.remember([input_tm1, action, reward, input_t], game_over)

        # adapt model
        inputs, targets = exp_replay.get_batch(model, batch_size=batch_size)

        loss += model.train_on_batch(inputs, targets)
        
    last_ten.append((reward+1)/2)
    print("Epoch {:03d}/{:d} | Loss {:.4f} | Win count {} | Last 10 win rate {}".format(e, epoch - 1, loss, win_cnt, sum(last_ten)/10.0))

# Save trained model weights and architecture
model.save_weights("/dbfs/keras_rl/model.h5", overwrite=True)
with open("/dbfs/keras_rl/model.json", "w") as outfile:
    json.dump(model.to_json(), outfile)

# COMMAND ----------

# MAGIC %sh ls -la /dbfs/keras_rl*

# COMMAND ----------

import json
import matplotlib.pyplot as plt
import numpy as np
from keras.models import model_from_json

grid_size = 10

with open("/dbfs/keras_rl/model.json", "r") as jfile:
    model = model_from_json(json.load(jfile))
model.load_weights("/dbfs/keras_rl/model.h5")
model.compile(loss='mse', optimizer='adam')

# Define environment, game
env = Catch(grid_size)
c = 0
for e in range(10):
    loss = 0.
    env.reset()
    game_over = False
    # get initial input
    input_t = env.observe()

    plt.imshow(input_t.reshape((grid_size,)*2),
               interpolation='none', cmap='gray')
    plt.savefig("/dbfs/keras_rl/images/%03d.png" % c)
    c += 1
    while not game_over:
        input_tm1 = input_t

        # get next action
        q = model.predict(input_tm1)
        action = np.argmax(q[0])

        # apply action, get rewards and new state
        input_t, reward, game_over = env.act(action)

        plt.imshow(input_t.reshape((grid_size,)*2),
                   interpolation='none', cmap='gray')
        plt.savefig("/dbfs/keras_rl/images/%03d.png" % c)
        c += 1


# COMMAND ----------

# MAGIC %sh ls -la /dbfs/keras_rl/images

# COMMAND ----------

import imageio

images = []
filenames = ["/dbfs/keras_rl/images/{:03d}.png".format(x) for x in xrange(100)]

# COMMAND ----------

for filename in filenames:
    images.append(imageio.imread(filename))
imageio.mimsave('/dbfs/FileStore/movie.gif', images)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC ```<img src="/files/movie.gif">```

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Where to Go Next?
# MAGIC 
# MAGIC The following articles are great next steps:
# MAGIC 
# MAGIC * Flappy Bird with DQL and Keras: https://yanpanlau.github.io/2016/07/10/FlappyBird-Keras.html
# MAGIC * DQL with Keras and an Open AI Gym task: http://koaning.io/hello-deepq.html
# MAGIC * Simple implementation with Open AI Gym support: https://github.com/sherjilozair/dqn
# MAGIC 
# MAGIC This project offers Keras add-on classes for simple experimentation with DQL:
# MAGIC 
# MAGIC * https://github.com/farizrahman4u/qlearning4k
# MAGIC * Note that you'll need to implement (or wrap) the "game" to plug into that framework
# MAGIC 
# MAGIC Try it at home:
# MAGIC 
# MAGIC * Hack the "Keras Plays Catch" demo to allow the ball to drift horizontally as it falls. Does it work?
# MAGIC * Try training the network on "delta frames" instead of static frames. This gives the network information about motion (implicitly).
# MAGIC * What if the screen is high-resolution? what happens? how could you handle it better?
# MAGIC 
# MAGIC And if you have the sneaking suspicion that there is a connection between PG and DQL, you'd be right: https://arxiv.org/abs/1704.06440