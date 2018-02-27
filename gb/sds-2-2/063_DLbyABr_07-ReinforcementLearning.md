Playing Games and Driving Cars: Reinforcement Learning
======================================================

<img src="https://i.imgur.com/VKEJsy6.jpg" width=900>

In a Nutshell
-------------

**In reinforcement learning, an agent takes multiple actions, and the positive or negative outcome of those actions serves as a loss function for subsequent training.**

Training an Agent
-----------------

What is an agent?

How is training an agent different from training the models we've used so far?

Most things stay the same, and we can use all of the knowledge we've built: \* We can use any or all of the network models, including feed-forward, convolutional, recurrent, and combinations of those. \* We will still train in batches using some variant of gradient descent \* As before, the model will ideally learn a complex non-obvious function of many parameters

A few things change ... well, not really change, but "specialize": \* The inputs may start out as entire frames (or frame deltas) of a video feed \* We may feature engineer more explicitly (or not) \* The ouputs may be a low-cardinality set of categories that represent actions (e.g., direction of a digital joystick, or input to a small number of control systems) \* We may model state explicitly (outside the network) as well as implicitly (inside the network) \* The function we're learning is one which will "tell our agent what to do" or -- assuming there is no disconnect between knowing what to do and doing it, the function will essentially be the agent \* **The loss function depends on the outcome of the game, and the game requires many actions to reach an outcome, and so requires some slightly different approaches from the ones we've used before.**

Principal Approaches: Deep Q-Learning and Policy Gradient Learning
==================================================================

-   Policy Gradient is straightforward and shows a lot of research promise, but can be quite difficult to use. The challenge is less in the math, code, or concepts, and more in terms of effective training. We'll look very briefly at PG.

-   Deep Q-Learning is more constrained and a little more complex mathematically. These factors would seem to cut against the use of DQL, but they allow for relatively fast and effective training, so they are very widely used. We'll go deeper into DQL and work with an example.

There are, of course, many variants on these as well as some other strategies.

Policy Gradient Learning
------------------------

With Policy Gradient Learning, we directly try to learn a "policy" function that selects a (possibly continuous-valued) move for an agent to make given the current state of the "world."

We want to maximize total discounted future reward, but we do not need discrete actions to take or a model that tells us a specific "next reward."

Instead, we can make fine-grained moves and we can collect all the moves that lead to a reward, and then apply that reward to all of them.

------------------------------------------------------------------------

> **ASIDE:** The term *gradient* here comes from a formula which indicates the gradient (or steepest direction) to improve the policy parameters with respect to the loss function. That is, which direction to adjust the parameters to maximize improvement in expected total reward.

------------------------------------------------------------------------

In some sense, this is a more straightforward, direct approach than the other approach we'll work with, Deep Q-Learning.

### Challenges with Policy Gradients

Policy gradients, despite achieving remarkable results, are a form of brute-force solution.

Thus they require a large amount of input data and extraordinary amounts of training time.

Some of these challenges come down to the *credit assignment problem* -- properly attributing reward to actions in the past which may (or may not) be responsible for the reward -- and thus some mitigations include more complex reward functions, adding more frequent reward training into the system, and adding domain knowledge to the policy, or adding an entire separate network, called a "critic network" to learn to provide feedback to the actor network.

Another challenge is the size of the search space, and tractable approaches to exploring it.

PG is challenging to use in practice, though there are a number of "tricks" in various publications that you can try.

### Next Steps

-   Great post by Andrej Karpathy on policy gradient learning: http://karpathy.github.io/2016/05/31/rl/

-   A nice first step on policy gradients with real code: *Using Keras and Deep Deterministic Policy Gradient to play TORCS*: https://yanpanlau.github.io/2016/10/11/Torcs-Keras.html

If you'd like to explore a variety of reinforcement learning techniques, Mattias Lappert at the Karlsruhe Institute of Technology, has created an add-on framework for Keras that implements a variety of state-of-the-art RL techniques, including discussed today.

His framework, KerasRL is at https://github.com/matthiasplappert/keras-rl and includes examples that integrate with OpenAI gym.

Deep Q-Learning
---------------

Deep Q-Learning is deep learning applied to "Q-Learning."

So what is Q-Learning?

Q-Learning is a model that posits a map for optimal actions for each possible state in a game.

Specifically, given a state and an action, there is a "Q-function" that provides the value or quality (the Q stands for quality) of that action taken in that state.

So, if an agent is in state s, choosing an action could be as simple as looking at Q(s, a) for all a, and choosing the highest "quality value" -- aka <img src="https://i.imgur.com/RerRQzk.png" width=110>

There are some other considerations, such as explore-exploit tradeoff, but let's focus on this Q function.

In small state spaces, this function can be represented as a table, a bit like basic strategy blackjack tables.

<img src="http://i.imgur.com/rfxLSls.png" width=400>

Even a simple Atari-style video game may have hundreds of thousands of states, though. This is where the neural network comes in.

**What we need is a way to learn a Q function, when we don't know what the error of a particular move is, since the error (loss) may be dependent on many future actions and can also be non-deterministic (e.g., if there are randomly generated enemies or conditions in the game).**

The tricks -- or insights -- here are:

\[1\] Model the total future reward -- what we're really after -- as a recursive calculation from the immediate reward (*r*) and future outcomes:

<img src="https://i.imgur.com/ePXoQfR.png" width=250>

-   \\({}\\) is a "discount factor" on future reward
-   Assume the game terminates or "effectively terminates" to make the recursion tractable
-   This equation is a simplified case of the Bellman Equation

\[2\] Assume that if you iteratively run this process starting with an arbitrary Q model, and you train the Q model with actual outcomes, your Q model will eventually converge toward the "true" Q function \* This seems intuitively to resemble various Monte Carlo sampling methods (if that helps at all)

#### As improbable as this might seem at first for teaching an agent a complex game or task, it actually works, and in a straightforward way.

How do we apply this to our neural network code?

Unlike before, when we called "fit" to train a network automatically, here we'll need some interplay between the agent's behavior in the game and the training. That is, we need the agent to play some moves in order to get actual numbers to train with. And as soon as we have some actual numbers, we want to do a little training with them right away so that our Q function improves. So we'll alternate one or more in-game actions with a manual call to train on a single batch of data.

The algorithm looks like this (credit for the nice summary to Tambet Matiisen; read his longer explanation at https://neuro.cs.ut.ee/demystifying-deep-reinforcement-learning/ for review):

1.  Do a feedforward pass for the current state s to get predicted Q-values for all actions.
2.  Do a feedforward pass for the next state s′ and calculate maximum over all network outputs <img src="https://i.imgur.com/3QiH4Z1.png" width=110>
3.  Set Q-value target for action a to <img src="https://i.imgur.com/2RNmkl6.png" width=140> (use the max calculated in step 2). For all other actions, set the Q-value target to the same as originally returned from step 1, making the error 0 for those outputs.
4.  Update the weights using backpropagation.

If there is "reward" throughout the game, we can model the loss as

<img src="https://i.imgur.com/OojwPbJ.png" width=350>

If the game is win/lose only ... most of the r's go away and the entire loss is based on a 0/1 or -1/1 score at the end of a game.

### Practical Consideration 1: Experience Replay

To improve training, we cache all (or as much as possible) of the agent's state/move/reward/next-state data. Then, when we go to perform a training run, we can build a batch out of a subset of all previous moves. This provides diversity in the training data, whose value we discussed earlier.

### Practical Consideration 2: Explore-Exploit Balance

In order to add more diversity to the agent's actions, we set a threshold ("epsilon") which represents the probability that the agent ignores its experience-based model and just chooses a random action. This also add diversity, by preventing the agent from taking an overly-narrow, 100% greedy (best-perfomance-so-far) path.

### Let's Look at the Code!

Reinforcement learning code examples are a bit more complex than the other examples we've seen so far, because in the other examples, the data sets (training and test) exist outside the program as assets (e.g., the MNIST digit data).

In reinforcement learning, the training and reward data come from some environment that the agent is supposed to learn. Typically, the environment is simulated by local code, or *represented by local code* even if the real environment is remote or comes from the physical world via sensors.

#### So the code contains not just the neural net and training logic, but part (or all) of a game world itself.

One of the most elegant small, but complete, examples comes courtesy of former Ph.D. researcher (Univ. of Florida) and Apple rockstar Eder Santana. It's a simplified catch-the-falling-brick game (a bit like Atari Kaboom! but even simpler) that nevertheless is complex enough to illustrate DQL and to be impressive in action.

When we're done, we'll basically have a game, and an agent that plays, which run like this:

<img src="http://i.imgur.com/PB6OgNF.gif">

First, let's get familiar with the game environment itself, since we'll need to see how it works, before we can focus on the reinforcement learning part of the program.

``` python
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
```

Next, let's look at the network itself -- it's super simple, so we can get that out of the way too:

    model = Sequential()
    model.add(Dense(hidden_size, input_shape=(grid_size**2,), activation='relu'))
    model.add(Dense(hidden_size, activation='relu'))
    model.add(Dense(num_actions))
    model.compile(sgd(lr=.2), "mse")

Note that the output layer has `num_actions` neurons.

We are going to implement the training target as \* the estimated reward for the one action taken when the game doesn't conclude, or \* error/reward for the specific action that loses/wins a game

In any case, we only train with an error/reward for actions the agent actually chose. We neutralize the hypothetical rewards for other actions, as they are not causally chained to any ground truth.

Next, let's zoom in on at the main game training loop:

    win_cnt = 0
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
        print("Epoch {:03d}/{:d} | Loss {:.4f} | Win count {}".format(e, epoch - 1, loss, win_cnt))

The key bits are: \* Choose an action \* Act and collect the reward and new state \* Cache previous state, action, reward, and new state in "Experience Replay" buffer \* Ask buffer for a batch of action data to train on \* Call `model.train_on_batch` to perform one training batch

Last, let's dive into where the actual Q-Learning calculations occur, which happen, in this code to be in the `get_batch` call to the experience replay buffer object:

``` python
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
```

The key bits here are: \* Set up "blank" buffers for a set of items of the requested batch size, or all memory, whichever is less (in case we don't have much data yet) \* one buffer is `inputs` -- it will contain the game state or screen before the agent acted \* the other buffer is `targets` -- it will contain a vector of rewards-per-action (with just one non-zero entry, for the action the agent actually took) \* Based on that batch size, randomly select records from memory \* For each of those cached records (which contain initial state, action, next state, and reward), \* Insert the initial game state into the proper place in the `inputs` buffer \* If the action ended the game then: \* Insert a vector into `targets` with the real reward in the position of the action chosen \* Else (if the action did not end the game): \* Insert a vector into `targets` with the following value in the position of the action taken: \* *(real reward)* \* **+** *(discount factor)(predicted-reward-for-best-action-in-the-next-state)* \* **Note**: although the Q-Learning formula is implemented in the general version here, this specific game only produces reward when the game is over, so the "real reward" in this branch will always be zero

``` python
mkdir /dbfs/keras_rl
```

``` python
mkdir /dbfs/keras_rl/images
```

Ok, now let's run the main training script and teach Keras to play Catch:

``` python
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
```

>     Epoch 000/399 | Loss 0.0045 | Win count 0 | Last 10 win rate 0.0
>     Epoch 001/399 | Loss 0.1900 | Win count 0 | Last 10 win rate 0.0
>     Epoch 002/399 | Loss 0.1805 | Win count 1 | Last 10 win rate 0.1
>     Epoch 003/399 | Loss 0.1129 | Win count 2 | Last 10 win rate 0.2
>     Epoch 004/399 | Loss 0.1337 | Win count 2 | Last 10 win rate 0.2
>     Epoch 005/399 | Loss 0.0792 | Win count 2 | Last 10 win rate 0.2
>     Epoch 006/399 | Loss 0.0973 | Win count 2 | Last 10 win rate 0.2
>     Epoch 007/399 | Loss 0.1345 | Win count 2 | Last 10 win rate 0.2
>     Epoch 008/399 | Loss 0.1037 | Win count 2 | Last 10 win rate 0.2
>     Epoch 009/399 | Loss 0.1151 | Win count 2 | Last 10 win rate 0.2
>     Epoch 010/399 | Loss 0.1187 | Win count 2 | Last 10 win rate 0.2
>     Epoch 011/399 | Loss 0.1733 | Win count 2 | Last 10 win rate 0.2
>     Epoch 012/399 | Loss 0.1186 | Win count 2 | Last 10 win rate 0.1
>     Epoch 013/399 | Loss 0.0900 | Win count 3 | Last 10 win rate 0.1
>     Epoch 014/399 | Loss 0.0824 | Win count 3 | Last 10 win rate 0.1
>     Epoch 015/399 | Loss 0.0650 | Win count 4 | Last 10 win rate 0.2
>     Epoch 016/399 | Loss 0.0463 | Win count 4 | Last 10 win rate 0.2
>     Epoch 017/399 | Loss 0.0521 | Win count 4 | Last 10 win rate 0.2
>     Epoch 018/399 | Loss 0.0642 | Win count 4 | Last 10 win rate 0.2
>     Epoch 019/399 | Loss 0.0674 | Win count 5 | Last 10 win rate 0.3
>     Epoch 020/399 | Loss 0.0573 | Win count 5 | Last 10 win rate 0.3
>     Epoch 021/399 | Loss 0.0599 | Win count 5 | Last 10 win rate 0.3
>     Epoch 022/399 | Loss 0.0568 | Win count 6 | Last 10 win rate 0.4
>     Epoch 023/399 | Loss 0.0732 | Win count 6 | Last 10 win rate 0.3
>     Epoch 024/399 | Loss 0.0603 | Win count 6 | Last 10 win rate 0.3
>     Epoch 025/399 | Loss 0.0554 | Win count 6 | Last 10 win rate 0.2
>     Epoch 026/399 | Loss 0.0619 | Win count 6 | Last 10 win rate 0.2
>     Epoch 027/399 | Loss 0.0397 | Win count 6 | Last 10 win rate 0.2
>     Epoch 028/399 | Loss 0.0417 | Win count 6 | Last 10 win rate 0.2
>     Epoch 029/399 | Loss 0.0541 | Win count 6 | Last 10 win rate 0.1
>     Epoch 030/399 | Loss 0.0303 | Win count 6 | Last 10 win rate 0.1
>     Epoch 031/399 | Loss 0.0263 | Win count 7 | Last 10 win rate 0.2
>     Epoch 032/399 | Loss 0.0183 | Win count 7 | Last 10 win rate 0.1
>     Epoch 033/399 | Loss 0.0232 | Win count 8 | Last 10 win rate 0.2
>     Epoch 034/399 | Loss 0.0203 | Win count 8 | Last 10 win rate 0.2
>     Epoch 035/399 | Loss 0.0198 | Win count 8 | Last 10 win rate 0.2
>     Epoch 036/399 | Loss 0.0178 | Win count 8 | Last 10 win rate 0.2
>     Epoch 037/399 | Loss 0.0234 | Win count 8 | Last 10 win rate 0.2
>     Epoch 038/399 | Loss 0.0260 | Win count 9 | Last 10 win rate 0.3
>     Epoch 039/399 | Loss 0.0911 | Win count 10 | Last 10 win rate 0.4
>     Epoch 040/399 | Loss 0.0904 | Win count 10 | Last 10 win rate 0.4
>     Epoch 041/399 | Loss 0.0636 | Win count 11 | Last 10 win rate 0.4
>     Epoch 042/399 | Loss 0.0539 | Win count 11 | Last 10 win rate 0.4
>     Epoch 043/399 | Loss 0.0503 | Win count 11 | Last 10 win rate 0.3
>     Epoch 044/399 | Loss 0.0507 | Win count 12 | Last 10 win rate 0.4
>     Epoch 045/399 | Loss 0.0959 | Win count 12 | Last 10 win rate 0.4
>     Epoch 046/399 | Loss 0.0415 | Win count 12 | Last 10 win rate 0.4
>     Epoch 047/399 | Loss 0.0503 | Win count 13 | Last 10 win rate 0.5
>     Epoch 048/399 | Loss 0.0627 | Win count 13 | Last 10 win rate 0.4
>     Epoch 049/399 | Loss 0.0569 | Win count 13 | Last 10 win rate 0.3
>     Epoch 050/399 | Loss 0.0659 | Win count 14 | Last 10 win rate 0.4
>     Epoch 051/399 | Loss 0.0629 | Win count 14 | Last 10 win rate 0.3
>     Epoch 052/399 | Loss 0.0543 | Win count 15 | Last 10 win rate 0.4
>     Epoch 053/399 | Loss 0.0309 | Win count 16 | Last 10 win rate 0.5
>     Epoch 054/399 | Loss 0.0497 | Win count 17 | Last 10 win rate 0.5
>     Epoch 055/399 | Loss 0.0215 | Win count 18 | Last 10 win rate 0.6
>     Epoch 056/399 | Loss 0.0631 | Win count 18 | Last 10 win rate 0.6
>     Epoch 057/399 | Loss 0.0329 | Win count 19 | Last 10 win rate 0.6
>     Epoch 058/399 | Loss 0.0481 | Win count 19 | Last 10 win rate 0.6
>     Epoch 059/399 | Loss 0.0282 | Win count 19 | Last 10 win rate 0.6
>     Epoch 060/399 | Loss 0.0253 | Win count 19 | Last 10 win rate 0.5
>     Epoch 061/399 | Loss 0.0304 | Win count 20 | Last 10 win rate 0.6
>     Epoch 062/399 | Loss 0.0490 | Win count 21 | Last 10 win rate 0.6
>     Epoch 063/399 | Loss 0.0416 | Win count 21 | Last 10 win rate 0.5
>     Epoch 064/399 | Loss 0.0414 | Win count 22 | Last 10 win rate 0.5
>     Epoch 065/399 | Loss 0.0308 | Win count 22 | Last 10 win rate 0.4
>     Epoch 066/399 | Loss 0.0462 | Win count 23 | Last 10 win rate 0.5
>     Epoch 067/399 | Loss 0.0333 | Win count 24 | Last 10 win rate 0.5
>     Epoch 068/399 | Loss 0.0482 | Win count 25 | Last 10 win rate 0.6
>     Epoch 069/399 | Loss 0.0359 | Win count 26 | Last 10 win rate 0.7
>     Epoch 070/399 | Loss 0.0506 | Win count 27 | Last 10 win rate 0.8
>     Epoch 071/399 | Loss 0.0424 | Win count 27 | Last 10 win rate 0.7
>     Epoch 072/399 | Loss 0.0423 | Win count 28 | Last 10 win rate 0.7
>     Epoch 073/399 | Loss 0.0531 | Win count 28 | Last 10 win rate 0.7
>     Epoch 074/399 | Loss 0.0606 | Win count 29 | Last 10 win rate 0.7
>     Epoch 075/399 | Loss 0.0381 | Win count 29 | Last 10 win rate 0.7
>     Epoch 076/399 | Loss 0.0580 | Win count 30 | Last 10 win rate 0.7
>     Epoch 077/399 | Loss 0.0548 | Win count 30 | Last 10 win rate 0.6
>     Epoch 078/399 | Loss 0.0511 | Win count 30 | Last 10 win rate 0.5
>     Epoch 079/399 | Loss 0.0362 | Win count 31 | Last 10 win rate 0.5
>     Epoch 080/399 | Loss 0.0326 | Win count 32 | Last 10 win rate 0.5
>     Epoch 081/399 | Loss 0.0731 | Win count 33 | Last 10 win rate 0.6
>     Epoch 082/399 | Loss 0.0619 | Win count 34 | Last 10 win rate 0.6
>     Epoch 083/399 | Loss 0.0462 | Win count 34 | Last 10 win rate 0.6
>     Epoch 084/399 | Loss 0.0442 | Win count 35 | Last 10 win rate 0.6
>     Epoch 085/399 | Loss 0.0463 | Win count 35 | Last 10 win rate 0.6
>     Epoch 086/399 | Loss 0.0309 | Win count 35 | Last 10 win rate 0.5
>     Epoch 087/399 | Loss 0.0505 | Win count 35 | Last 10 win rate 0.5
>     Epoch 088/399 | Loss 0.0317 | Win count 36 | Last 10 win rate 0.6
>     Epoch 089/399 | Loss 0.0357 | Win count 36 | Last 10 win rate 0.5
>     Epoch 090/399 | Loss 0.0367 | Win count 36 | Last 10 win rate 0.4
>     Epoch 091/399 | Loss 0.0445 | Win count 36 | Last 10 win rate 0.3
>     Epoch 092/399 | Loss 0.0501 | Win count 37 | Last 10 win rate 0.3
>     Epoch 093/399 | Loss 0.0396 | Win count 38 | Last 10 win rate 0.4
>     Epoch 094/399 | Loss 0.0432 | Win count 39 | Last 10 win rate 0.4
>     Epoch 095/399 | Loss 0.0402 | Win count 40 | Last 10 win rate 0.5
>     Epoch 096/399 | Loss 0.0384 | Win count 41 | Last 10 win rate 0.6
>     Epoch 097/399 | Loss 0.0394 | Win count 41 | Last 10 win rate 0.6
>     Epoch 098/399 | Loss 0.0357 | Win count 41 | Last 10 win rate 0.5
>     Epoch 099/399 | Loss 0.0835 | Win count 41 | Last 10 win rate 0.5
>     Epoch 100/399 | Loss 0.0511 | Win count 42 | Last 10 win rate 0.6
>     Epoch 101/399 | Loss 0.0407 | Win count 43 | Last 10 win rate 0.7
>     Epoch 102/399 | Loss 0.0512 | Win count 44 | Last 10 win rate 0.7
>     Epoch 103/399 | Loss 0.0591 | Win count 45 | Last 10 win rate 0.7
>     Epoch 104/399 | Loss 0.0528 | Win count 46 | Last 10 win rate 0.7
>     Epoch 105/399 | Loss 0.0395 | Win count 47 | Last 10 win rate 0.7
>     Epoch 106/399 | Loss 0.0425 | Win count 48 | Last 10 win rate 0.7
>     Epoch 107/399 | Loss 0.0343 | Win count 49 | Last 10 win rate 0.8
>     Epoch 108/399 | Loss 0.0261 | Win count 49 | Last 10 win rate 0.8
>     Epoch 109/399 | Loss 0.0483 | Win count 50 | Last 10 win rate 0.9
>     Epoch 110/399 | Loss 0.0345 | Win count 51 | Last 10 win rate 0.9
>     Epoch 111/399 | Loss 0.0197 | Win count 52 | Last 10 win rate 0.9
>     Epoch 112/399 | Loss 0.0268 | Win count 53 | Last 10 win rate 0.9
>     Epoch 113/399 | Loss 0.0211 | Win count 54 | Last 10 win rate 0.9
>     Epoch 114/399 | Loss 0.0496 | Win count 55 | Last 10 win rate 0.9
>     Epoch 115/399 | Loss 0.0308 | Win count 56 | Last 10 win rate 0.9
>     Epoch 116/399 | Loss 0.0335 | Win count 56 | Last 10 win rate 0.8
>     Epoch 117/399 | Loss 0.0232 | Win count 56 | Last 10 win rate 0.7
>     Epoch 118/399 | Loss 0.0375 | Win count 57 | Last 10 win rate 0.8
>     Epoch 119/399 | Loss 0.0338 | Win count 58 | Last 10 win rate 0.8
>     Epoch 120/399 | Loss 0.0230 | Win count 58 | Last 10 win rate 0.7
>     Epoch 121/399 | Loss 0.0271 | Win count 59 | Last 10 win rate 0.7
>     Epoch 122/399 | Loss 0.0239 | Win count 59 | Last 10 win rate 0.6
>     Epoch 123/399 | Loss 0.0269 | Win count 60 | Last 10 win rate 0.6
>     Epoch 124/399 | Loss 0.0190 | Win count 60 | Last 10 win rate 0.5
>     Epoch 125/399 | Loss 0.0229 | Win count 61 | Last 10 win rate 0.5
>     Epoch 126/399 | Loss 0.0446 | Win count 61 | Last 10 win rate 0.5
>     Epoch 127/399 | Loss 0.0436 | Win count 62 | Last 10 win rate 0.6
>     Epoch 128/399 | Loss 0.0150 | Win count 63 | Last 10 win rate 0.6
>     Epoch 129/399 | Loss 0.0115 | Win count 64 | Last 10 win rate 0.6
>     Epoch 130/399 | Loss 0.0299 | Win count 64 | Last 10 win rate 0.6
>     Epoch 131/399 | Loss 0.0246 | Win count 65 | Last 10 win rate 0.6
>     Epoch 132/399 | Loss 0.0349 | Win count 65 | Last 10 win rate 0.6
>     Epoch 133/399 | Loss 0.0355 | Win count 66 | Last 10 win rate 0.6
>     Epoch 134/399 | Loss 0.0308 | Win count 67 | Last 10 win rate 0.7
>     Epoch 135/399 | Loss 0.0190 | Win count 68 | Last 10 win rate 0.7
>     Epoch 136/399 | Loss 0.0224 | Win count 69 | Last 10 win rate 0.8
>     Epoch 137/399 | Loss 0.0223 | Win count 70 | Last 10 win rate 0.8
>     Epoch 138/399 | Loss 0.0165 | Win count 71 | Last 10 win rate 0.8
>     Epoch 139/399 | Loss 0.0179 | Win count 72 | Last 10 win rate 0.8
>     Epoch 140/399 | Loss 0.0197 | Win count 73 | Last 10 win rate 0.9
>     Epoch 141/399 | Loss 0.0184 | Win count 74 | Last 10 win rate 0.9
>     Epoch 142/399 | Loss 0.0133 | Win count 75 | Last 10 win rate 1.0
>     Epoch 143/399 | Loss 0.0144 | Win count 75 | Last 10 win rate 0.9
>     Epoch 144/399 | Loss 0.0133 | Win count 76 | Last 10 win rate 0.9
>     Epoch 145/399 | Loss 0.0114 | Win count 77 | Last 10 win rate 0.9
>     Epoch 146/399 | Loss 0.0161 | Win count 77 | Last 10 win rate 0.8
>     Epoch 147/399 | Loss 0.0098 | Win count 78 | Last 10 win rate 0.8
>     Epoch 148/399 | Loss 0.0096 | Win count 79 | Last 10 win rate 0.8
>     Epoch 149/399 | Loss 0.0081 | Win count 80 | Last 10 win rate 0.8
>     Epoch 150/399 | Loss 0.0138 | Win count 81 | Last 10 win rate 0.8
>     Epoch 151/399 | Loss 0.0092 | Win count 82 | Last 10 win rate 0.8
>     Epoch 152/399 | Loss 0.0054 | Win count 83 | Last 10 win rate 0.8
>     Epoch 153/399 | Loss 0.0103 | Win count 84 | Last 10 win rate 0.9
>     Epoch 154/399 | Loss 0.0081 | Win count 85 | Last 10 win rate 0.9
>     Epoch 155/399 | Loss 0.0070 | Win count 86 | Last 10 win rate 0.9
>     Epoch 156/399 | Loss 0.0057 | Win count 87 | Last 10 win rate 1.0
>     Epoch 157/399 | Loss 0.0070 | Win count 88 | Last 10 win rate 1.0
>     Epoch 158/399 | Loss 0.0057 | Win count 89 | Last 10 win rate 1.0
>     Epoch 159/399 | Loss 0.0062 | Win count 89 | Last 10 win rate 0.9
>     Epoch 160/399 | Loss 0.0061 | Win count 90 | Last 10 win rate 0.9
>     Epoch 161/399 | Loss 0.0272 | Win count 91 | Last 10 win rate 0.9
>     Epoch 162/399 | Loss 0.0384 | Win count 92 | Last 10 win rate 0.9
>     Epoch 163/399 | Loss 0.0477 | Win count 92 | Last 10 win rate 0.8
>     Epoch 164/399 | Loss 0.0253 | Win count 93 | Last 10 win rate 0.8
>     Epoch 165/399 | Loss 0.0402 | Win count 94 | Last 10 win rate 0.8
>     Epoch 166/399 | Loss 0.0296 | Win count 95 | Last 10 win rate 0.8
>     Epoch 167/399 | Loss 0.0295 | Win count 96 | Last 10 win rate 0.8
>     Epoch 168/399 | Loss 0.0253 | Win count 97 | Last 10 win rate 0.8
>     Epoch 169/399 | Loss 0.0148 | Win count 98 | Last 10 win rate 0.9
>     Epoch 170/399 | Loss 0.0234 | Win count 98 | Last 10 win rate 0.8
>     Epoch 171/399 | Loss 0.0184 | Win count 98 | Last 10 win rate 0.7
>     Epoch 172/399 | Loss 0.0251 | Win count 99 | Last 10 win rate 0.7
>     Epoch 173/399 | Loss 0.0122 | Win count 100 | Last 10 win rate 0.8
>     Epoch 174/399 | Loss 0.0150 | Win count 101 | Last 10 win rate 0.8
>     Epoch 175/399 | Loss 0.0154 | Win count 101 | Last 10 win rate 0.7
>     Epoch 176/399 | Loss 0.0168 | Win count 102 | Last 10 win rate 0.7
>     Epoch 177/399 | Loss 0.0354 | Win count 103 | Last 10 win rate 0.7
>     Epoch 178/399 | Loss 0.0297 | Win count 104 | Last 10 win rate 0.7
>     Epoch 179/399 | Loss 0.0131 | Win count 105 | Last 10 win rate 0.7
>     Epoch 180/399 | Loss 0.0118 | Win count 106 | Last 10 win rate 0.8
>     Epoch 181/399 | Loss 0.0097 | Win count 107 | Last 10 win rate 0.9
>     Epoch 182/399 | Loss 0.0101 | Win count 108 | Last 10 win rate 0.9
>     Epoch 183/399 | Loss 0.0185 | Win count 109 | Last 10 win rate 0.9
>     Epoch 184/399 | Loss 0.0078 | Win count 110 | Last 10 win rate 0.9
>     Epoch 185/399 | Loss 0.0072 | Win count 111 | Last 10 win rate 1.0
>     Epoch 186/399 | Loss 0.0140 | Win count 112 | Last 10 win rate 1.0
>     Epoch 187/399 | Loss 0.0130 | Win count 113 | Last 10 win rate 1.0
>     Epoch 188/399 | Loss 0.0163 | Win count 114 | Last 10 win rate 1.0
>     Epoch 189/399 | Loss 0.0136 | Win count 115 | Last 10 win rate 1.0
>     Epoch 190/399 | Loss 0.0190 | Win count 116 | Last 10 win rate 1.0
>     Epoch 191/399 | Loss 0.0265 | Win count 117 | Last 10 win rate 1.0
>     Epoch 192/399 | Loss 0.0175 | Win count 118 | Last 10 win rate 1.0
>     Epoch 193/399 | Loss 0.0116 | Win count 119 | Last 10 win rate 1.0
>     Epoch 194/399 | Loss 0.0125 | Win count 120 | Last 10 win rate 1.0
>     Epoch 195/399 | Loss 0.0122 | Win count 120 | Last 10 win rate 0.9
>     Epoch 196/399 | Loss 0.0085 | Win count 121 | Last 10 win rate 0.9
>     Epoch 197/399 | Loss 0.0064 | Win count 122 | Last 10 win rate 0.9
>     Epoch 198/399 | Loss 0.0106 | Win count 123 | Last 10 win rate 0.9
>     Epoch 199/399 | Loss 0.0079 | Win count 123 | Last 10 win rate 0.8
>     Epoch 200/399 | Loss 0.0185 | Win count 124 | Last 10 win rate 0.8
>     Epoch 201/399 | Loss 0.0117 | Win count 124 | Last 10 win rate 0.7
>     Epoch 202/399 | Loss 0.0134 | Win count 125 | Last 10 win rate 0.7
>     Epoch 203/399 | Loss 0.0094 | Win count 125 | Last 10 win rate 0.6
>     Epoch 204/399 | Loss 0.0197 | Win count 126 | Last 10 win rate 0.6
>     Epoch 205/399 | Loss 0.0123 | Win count 127 | Last 10 win rate 0.7
>     Epoch 206/399 | Loss 0.0121 | Win count 128 | Last 10 win rate 0.7
>     Epoch 207/399 | Loss 0.0076 | Win count 129 | Last 10 win rate 0.7
>     Epoch 208/399 | Loss 0.0071 | Win count 130 | Last 10 win rate 0.7
>     Epoch 209/399 | Loss 0.0037 | Win count 131 | Last 10 win rate 0.8
>     Epoch 210/399 | Loss 0.0034 | Win count 132 | Last 10 win rate 0.8
>     Epoch 211/399 | Loss 0.0045 | Win count 133 | Last 10 win rate 0.9
>     Epoch 212/399 | Loss 0.0040 | Win count 134 | Last 10 win rate 0.9
>     Epoch 213/399 | Loss 0.0025 | Win count 135 | Last 10 win rate 1.0
>     Epoch 214/399 | Loss 0.0045 | Win count 136 | Last 10 win rate 1.0
>     Epoch 215/399 | Loss 0.0043 | Win count 137 | Last 10 win rate 1.0
>     Epoch 216/399 | Loss 0.0053 | Win count 138 | Last 10 win rate 1.0
>     Epoch 217/399 | Loss 0.0031 | Win count 138 | Last 10 win rate 0.9
>     Epoch 218/399 | Loss 0.0030 | Win count 139 | Last 10 win rate 0.9
>     Epoch 219/399 | Loss 0.0055 | Win count 140 | Last 10 win rate 0.9
>     Epoch 220/399 | Loss 0.0079 | Win count 141 | Last 10 win rate 0.9
>     Epoch 221/399 | Loss 0.0057 | Win count 142 | Last 10 win rate 0.9
>     Epoch 222/399 | Loss 0.0041 | Win count 143 | Last 10 win rate 0.9
>     Epoch 223/399 | Loss 0.0114 | Win count 144 | Last 10 win rate 0.9
>     Epoch 224/399 | Loss 0.0051 | Win count 145 | Last 10 win rate 0.9
>     Epoch 225/399 | Loss 0.0070 | Win count 146 | Last 10 win rate 0.9
>     Epoch 226/399 | Loss 0.0061 | Win count 147 | Last 10 win rate 0.9
>     Epoch 227/399 | Loss 0.0046 | Win count 148 | Last 10 win rate 1.0
>     Epoch 228/399 | Loss 0.0063 | Win count 149 | Last 10 win rate 1.0
>     Epoch 229/399 | Loss 0.0045 | Win count 150 | Last 10 win rate 1.0
>     Epoch 230/399 | Loss 0.0027 | Win count 151 | Last 10 win rate 1.0
>     Epoch 231/399 | Loss 0.0024 | Win count 151 | Last 10 win rate 0.9
>     Epoch 232/399 | Loss 0.0215 | Win count 151 | Last 10 win rate 0.8
>     Epoch 233/399 | Loss 0.0223 | Win count 152 | Last 10 win rate 0.8
>     Epoch 234/399 | Loss 0.0164 | Win count 153 | Last 10 win rate 0.8
>     Epoch 235/399 | Loss 0.0135 | Win count 154 | Last 10 win rate 0.8
>     Epoch 236/399 | Loss 0.0223 | Win count 155 | Last 10 win rate 0.8
>     Epoch 237/399 | Loss 0.0247 | Win count 156 | Last 10 win rate 0.8
>     Epoch 238/399 | Loss 0.0128 | Win count 157 | Last 10 win rate 0.8
>     Epoch 239/399 | Loss 0.0091 | Win count 158 | Last 10 win rate 0.8
>     Epoch 240/399 | Loss 0.0184 | Win count 158 | Last 10 win rate 0.7
>     Epoch 241/399 | Loss 0.0158 | Win count 159 | Last 10 win rate 0.8
>     Epoch 242/399 | Loss 0.0525 | Win count 160 | Last 10 win rate 0.9
>     Epoch 243/399 | Loss 0.0275 | Win count 161 | Last 10 win rate 0.9
>     Epoch 244/399 | Loss 0.0271 | Win count 162 | Last 10 win rate 0.9
>     Epoch 245/399 | Loss 0.0208 | Win count 163 | Last 10 win rate 0.9
>     Epoch 246/399 | Loss 0.0177 | Win count 164 | Last 10 win rate 0.9
>     Epoch 247/399 | Loss 0.0269 | Win count 164 | Last 10 win rate 0.8
>     Epoch 248/399 | Loss 0.0165 | Win count 164 | Last 10 win rate 0.7
>     Epoch 249/399 | Loss 0.0458 | Win count 164 | Last 10 win rate 0.6
>     Epoch 250/399 | Loss 0.0469 | Win count 165 | Last 10 win rate 0.7
>     Epoch 251/399 | Loss 0.0230 | Win count 166 | Last 10 win rate 0.7
>     Epoch 252/399 | Loss 0.0219 | Win count 167 | Last 10 win rate 0.7
>     Epoch 253/399 | Loss 0.0168 | Win count 168 | Last 10 win rate 0.7
>     Epoch 254/399 | Loss 0.0218 | Win count 169 | Last 10 win rate 0.7
>     Epoch 255/399 | Loss 0.0132 | Win count 170 | Last 10 win rate 0.7
>     Epoch 256/399 | Loss 0.0239 | Win count 171 | Last 10 win rate 0.7
>     Epoch 257/399 | Loss 0.0150 | Win count 172 | Last 10 win rate 0.8
>     Epoch 258/399 | Loss 0.0167 | Win count 173 | Last 10 win rate 0.9
>     Epoch 259/399 | Loss 0.0091 | Win count 173 | Last 10 win rate 0.9
>     Epoch 260/399 | Loss 0.0336 | Win count 174 | Last 10 win rate 0.9
>     Epoch 261/399 | Loss 0.0083 | Win count 175 | Last 10 win rate 0.9
>     Epoch 262/399 | Loss 0.0098 | Win count 176 | Last 10 win rate 0.9
>     Epoch 263/399 | Loss 0.0251 | Win count 177 | Last 10 win rate 0.9
>     Epoch 264/399 | Loss 0.0211 | Win count 177 | Last 10 win rate 0.8
>     Epoch 265/399 | Loss 0.0494 | Win count 178 | Last 10 win rate 0.8
>     Epoch 266/399 | Loss 0.0476 | Win count 179 | Last 10 win rate 0.8
>     Epoch 267/399 | Loss 0.0116 | Win count 180 | Last 10 win rate 0.8
>     Epoch 268/399 | Loss 0.0357 | Win count 181 | Last 10 win rate 0.8
>     Epoch 269/399 | Loss 0.0184 | Win count 182 | Last 10 win rate 0.9
>     Epoch 270/399 | Loss 0.0241 | Win count 183 | Last 10 win rate 0.9
>     Epoch 271/399 | Loss 0.0243 | Win count 184 | Last 10 win rate 0.9
>     Epoch 272/399 | Loss 0.0153 | Win count 185 | Last 10 win rate 0.9
>     Epoch 273/399 | Loss 0.0164 | Win count 186 | Last 10 win rate 0.9
>     Epoch 274/399 | Loss 0.0115 | Win count 187 | Last 10 win rate 1.0
>     Epoch 275/399 | Loss 0.0072 | Win count 188 | Last 10 win rate 1.0
>     Epoch 276/399 | Loss 0.0156 | Win count 189 | Last 10 win rate 1.0
>     Epoch 277/399 | Loss 0.0133 | Win count 189 | Last 10 win rate 0.9
>     Epoch 278/399 | Loss 0.0067 | Win count 190 | Last 10 win rate 0.9
>     Epoch 279/399 | Loss 0.0224 | Win count 191 | Last 10 win rate 0.9
>     Epoch 280/399 | Loss 0.0273 | Win count 192 | Last 10 win rate 0.9
>     Epoch 281/399 | Loss 0.0089 | Win count 193 | Last 10 win rate 0.9
>     Epoch 282/399 | Loss 0.0087 | Win count 194 | Last 10 win rate 0.9
>     Epoch 283/399 | Loss 0.0051 | Win count 195 | Last 10 win rate 0.9
>     Epoch 284/399 | Loss 0.0096 | Win count 196 | Last 10 win rate 0.9
>     Epoch 285/399 | Loss 0.0112 | Win count 197 | Last 10 win rate 0.9
>     Epoch 286/399 | Loss 0.0158 | Win count 198 | Last 10 win rate 0.9
>     Epoch 287/399 | Loss 0.0112 | Win count 199 | Last 10 win rate 1.0
>     Epoch 288/399 | Loss 0.0139 | Win count 200 | Last 10 win rate 1.0
>     Epoch 289/399 | Loss 0.0081 | Win count 201 | Last 10 win rate 1.0
>     Epoch 290/399 | Loss 0.0107 | Win count 202 | Last 10 win rate 1.0
>     Epoch 291/399 | Loss 0.0095 | Win count 203 | Last 10 win rate 1.0
>     Epoch 292/399 | Loss 0.0091 | Win count 204 | Last 10 win rate 1.0
>     Epoch 293/399 | Loss 0.0105 | Win count 205 | Last 10 win rate 1.0
>     Epoch 294/399 | Loss 0.0071 | Win count 205 | Last 10 win rate 0.9
>     Epoch 295/399 | Loss 0.0081 | Win count 206 | Last 10 win rate 0.9
>     Epoch 296/399 | Loss 0.0059 | Win count 207 | Last 10 win rate 0.9
>     Epoch 297/399 | Loss 0.0027 | Win count 208 | Last 10 win rate 0.9
>     Epoch 298/399 | Loss 0.0060 | Win count 209 | Last 10 win rate 0.9
>     Epoch 299/399 | Loss 0.0039 | Win count 210 | Last 10 win rate 0.9
>     Epoch 300/399 | Loss 0.0039 | Win count 211 | Last 10 win rate 0.9
>     Epoch 301/399 | Loss 0.0037 | Win count 212 | Last 10 win rate 0.9
>     Epoch 302/399 | Loss 0.0043 | Win count 213 | Last 10 win rate 0.9
>     Epoch 303/399 | Loss 0.0050 | Win count 214 | Last 10 win rate 0.9
>     Epoch 304/399 | Loss 0.0083 | Win count 215 | Last 10 win rate 1.0
>     Epoch 305/399 | Loss 0.0088 | Win count 215 | Last 10 win rate 0.9
>     Epoch 306/399 | Loss 0.0086 | Win count 216 | Last 10 win rate 0.9
>     Epoch 307/399 | Loss 0.0063 | Win count 217 | Last 10 win rate 0.9
>     Epoch 308/399 | Loss 0.0057 | Win count 218 | Last 10 win rate 0.9
>     Epoch 309/399 | Loss 0.0064 | Win count 219 | Last 10 win rate 0.9
>     Epoch 310/399 | Loss 0.0060 | Win count 220 | Last 10 win rate 0.9
>     Epoch 311/399 | Loss 0.0049 | Win count 221 | Last 10 win rate 0.9
>     Epoch 312/399 | Loss 0.0043 | Win count 222 | Last 10 win rate 0.9
>     Epoch 313/399 | Loss 0.0031 | Win count 223 | Last 10 win rate 0.9
>     Epoch 314/399 | Loss 0.0032 | Win count 224 | Last 10 win rate 0.9
>     Epoch 315/399 | Loss 0.0028 | Win count 225 | Last 10 win rate 1.0
>     Epoch 316/399 | Loss 0.0030 | Win count 226 | Last 10 win rate 1.0
>     Epoch 317/399 | Loss 0.0022 | Win count 226 | Last 10 win rate 0.9
>     Epoch 318/399 | Loss 0.0021 | Win count 227 | Last 10 win rate 0.9
>     Epoch 319/399 | Loss 0.0021 | Win count 228 | Last 10 win rate 0.9
>     Epoch 320/399 | Loss 0.0116 | Win count 229 | Last 10 win rate 0.9
>     Epoch 321/399 | Loss 0.0055 | Win count 230 | Last 10 win rate 0.9
>     Epoch 322/399 | Loss 0.0041 | Win count 231 | Last 10 win rate 0.9
>     Epoch 323/399 | Loss 0.0041 | Win count 232 | Last 10 win rate 0.9
>     Epoch 324/399 | Loss 0.0043 | Win count 233 | Last 10 win rate 0.9
>     Epoch 325/399 | Loss 0.0023 | Win count 234 | Last 10 win rate 0.9
>     Epoch 326/399 | Loss 0.0041 | Win count 234 | Last 10 win rate 0.8
>     Epoch 327/399 | Loss 0.0048 | Win count 235 | Last 10 win rate 0.9
>     Epoch 328/399 | Loss 0.0082 | Win count 236 | Last 10 win rate 0.9
>     Epoch 329/399 | Loss 0.0054 | Win count 237 | Last 10 win rate 0.9
>     Epoch 330/399 | Loss 0.0027 | Win count 238 | Last 10 win rate 0.9
>     Epoch 331/399 | Loss 0.0068 | Win count 239 | Last 10 win rate 0.9
>     Epoch 332/399 | Loss 0.0081 | Win count 240 | Last 10 win rate 0.9
>     Epoch 333/399 | Loss 0.0126 | Win count 241 | Last 10 win rate 0.9
>     Epoch 334/399 | Loss 0.0097 | Win count 242 | Last 10 win rate 0.9
>     Epoch 335/399 | Loss 0.0083 | Win count 243 | Last 10 win rate 0.9
>     Epoch 336/399 | Loss 0.0073 | Win count 244 | Last 10 win rate 1.0
>     Epoch 337/399 | Loss 0.0108 | Win count 245 | Last 10 win rate 1.0
>     Epoch 338/399 | Loss 0.0184 | Win count 246 | Last 10 win rate 1.0
>     Epoch 339/399 | Loss 0.0087 | Win count 247 | Last 10 win rate 1.0
>     Epoch 340/399 | Loss 0.0082 | Win count 247 | Last 10 win rate 0.9
>     Epoch 341/399 | Loss 0.0080 | Win count 248 | Last 10 win rate 0.9
>     Epoch 342/399 | Loss 0.0080 | Win count 249 | Last 10 win rate 0.9
>     Epoch 343/399 | Loss 0.0100 | Win count 250 | Last 10 win rate 0.9
>     Epoch 344/399 | Loss 0.0077 | Win count 251 | Last 10 win rate 0.9
>     Epoch 345/399 | Loss 0.0081 | Win count 252 | Last 10 win rate 0.9
>     Epoch 346/399 | Loss 0.0078 | Win count 253 | Last 10 win rate 0.9
>     Epoch 347/399 | Loss 0.0107 | Win count 253 | Last 10 win rate 0.8
>     Epoch 348/399 | Loss 0.0088 | Win count 253 | Last 10 win rate 0.7
>     Epoch 349/399 | Loss 0.0077 | Win count 254 | Last 10 win rate 0.7
>     Epoch 350/399 | Loss 0.0154 | Win count 255 | Last 10 win rate 0.8
>     Epoch 351/399 | Loss 0.0089 | Win count 256 | Last 10 win rate 0.8
>     Epoch 352/399 | Loss 0.0131 | Win count 257 | Last 10 win rate 0.8
>     Epoch 353/399 | Loss 0.0159 | Win count 258 | Last 10 win rate 0.8
>     Epoch 354/399 | Loss 0.0103 | Win count 259 | Last 10 win rate 0.8
>     Epoch 355/399 | Loss 0.0074 | Win count 260 | Last 10 win rate 0.8
>     Epoch 356/399 | Loss 0.0051 | Win count 261 | Last 10 win rate 0.8
>     Epoch 357/399 | Loss 0.0051 | Win count 262 | Last 10 win rate 0.9
>     Epoch 358/399 | Loss 0.0051 | Win count 263 | Last 10 win rate 1.0
>     Epoch 359/399 | Loss 0.0082 | Win count 264 | Last 10 win rate 1.0
>     Epoch 360/399 | Loss 0.0175 | Win count 265 | Last 10 win rate 1.0
>     Epoch 361/399 | Loss 0.0089 | Win count 266 | Last 10 win rate 1.0
>     Epoch 362/399 | Loss 0.0069 | Win count 267 | Last 10 win rate 1.0
>     Epoch 363/399 | Loss 0.0062 | Win count 268 | Last 10 win rate 1.0
>     Epoch 364/399 | Loss 0.0063 | Win count 269 | Last 10 win rate 1.0
>     Epoch 365/399 | Loss 0.0089 | Win count 270 | Last 10 win rate 1.0
>     Epoch 366/399 | Loss 0.0073 | Win count 271 | Last 10 win rate 1.0
>     Epoch 367/399 | Loss 0.0048 | Win count 272 | Last 10 win rate 1.0
>     Epoch 368/399 | Loss 0.0053 | Win count 273 | Last 10 win rate 1.0
>     Epoch 369/399 | Loss 0.0041 | Win count 273 | Last 10 win rate 0.9
>     Epoch 370/399 | Loss 0.0054 | Win count 274 | Last 10 win rate 0.9
>     Epoch 371/399 | Loss 0.0370 | Win count 275 | Last 10 win rate 0.9
>     Epoch 372/399 | Loss 0.0404 | Win count 276 | Last 10 win rate 0.9
>     Epoch 373/399 | Loss 0.0178 | Win count 277 | Last 10 win rate 0.9
>     Epoch 374/399 | Loss 0.0075 | Win count 278 | Last 10 win rate 0.9
>     Epoch 375/399 | Loss 0.0064 | Win count 279 | Last 10 win rate 0.9
>     Epoch 376/399 | Loss 0.0050 | Win count 280 | Last 10 win rate 0.9
>     Epoch 377/399 | Loss 0.0046 | Win count 281 | Last 10 win rate 0.9
>     Epoch 378/399 | Loss 0.0039 | Win count 282 | Last 10 win rate 0.9
>     Epoch 379/399 | Loss 0.0030 | Win count 283 | Last 10 win rate 1.0
>     Epoch 380/399 | Loss 0.0033 | Win count 284 | Last 10 win rate 1.0
>     Epoch 381/399 | Loss 0.0036 | Win count 285 | Last 10 win rate 1.0
>     Epoch 382/399 | Loss 0.0038 | Win count 286 | Last 10 win rate 1.0
>     Epoch 383/399 | Loss 0.0036 | Win count 287 | Last 10 win rate 1.0
>     Epoch 384/399 | Loss 0.0038 | Win count 288 | Last 10 win rate 1.0
>     Epoch 385/399 | Loss 0.0025 | Win count 289 | Last 10 win rate 1.0
>     Epoch 386/399 | Loss 0.0022 | Win count 290 | Last 10 win rate 1.0
>     Epoch 387/399 | Loss 0.0018 | Win count 291 | Last 10 win rate 1.0
>     Epoch 388/399 | Loss 0.0013 | Win count 292 | Last 10 win rate 1.0
>     Epoch 389/399 | Loss 0.0020 | Win count 293 | Last 10 win rate 1.0
>     Epoch 390/399 | Loss 0.0023 | Win count 294 | Last 10 win rate 1.0
>     Epoch 391/399 | Loss 0.0010 | Win count 295 | Last 10 win rate 1.0
>     Epoch 392/399 | Loss 0.0014 | Win count 296 | Last 10 win rate 1.0
>     Epoch 393/399 | Loss 0.0010 | Win count 297 | Last 10 win rate 1.0
>     Epoch 394/399 | Loss 0.0015 | Win count 298 | Last 10 win rate 1.0
>     Epoch 395/399 | Loss 0.0012 | Win count 299 | Last 10 win rate 1.0
>     Epoch 396/399 | Loss 0.0010 | Win count 300 | Last 10 win rate 1.0
>     Epoch 397/399 | Loss 0.0010 | Win count 301 | Last 10 win rate 1.0
>     Epoch 398/399 | Loss 0.0011 | Win count 302 | Last 10 win rate 1.0
>     Epoch 399/399 | Loss 0.0007 | Win count 303 | Last 10 win rate 1.0

>     total 0
>     drwxr-xr-x 1 root root     0 Jan  1  1970 .
>     drwxrwxrwx 1 root root  4096 Jan  1  1970 ..
>     drwxr-xr-x 1 root root     0 Jan  1  1970 images
>     -rw-r--r-- 1 root root 95840 Jan  1  1970 model.h5
>     -rw-r--r-- 1 root root  1742 Jan  1  1970 model.json

``` python
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
```

>     total 0
>     drwxr-xr-x 1 root root    0 Jan  1  1970 .
>     drwxr-xr-x 1 root root    0 Jan  1  1970 ..
>     -rw-r--r-- 1 root root 7448 Jan  1  1970 000.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 001.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 002.png
>     -rw-r--r-- 1 root root 7367 Jan  1  1970 003.png
>     -rw-r--r-- 1 root root 7367 Jan  1  1970 004.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 005.png
>     -rw-r--r-- 1 root root 7401 Jan  1  1970 006.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 007.png
>     -rw-r--r-- 1 root root 7383 Jan  1  1970 008.png
>     -rw-r--r-- 1 root root 7356 Jan  1  1970 009.png
>     -rw-r--r-- 1 root root 7447 Jan  1  1970 010.png
>     -rw-r--r-- 1 root root 7368 Jan  1  1970 011.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 012.png
>     -rw-r--r-- 1 root root 7368 Jan  1  1970 013.png
>     -rw-r--r-- 1 root root 7368 Jan  1  1970 014.png
>     -rw-r--r-- 1 root root 7384 Jan  1  1970 015.png
>     -rw-r--r-- 1 root root 7400 Jan  1  1970 016.png
>     -rw-r--r-- 1 root root 7368 Jan  1  1970 017.png
>     -rw-r--r-- 1 root root 7371 Jan  1  1970 018.png
>     -rw-r--r-- 1 root root 7357 Jan  1  1970 019.png
>     -rw-r--r-- 1 root root 7430 Jan  1  1970 020.png
>     -rw-r--r-- 1 root root 7367 Jan  1  1970 021.png
>     -rw-r--r-- 1 root root 7367 Jan  1  1970 022.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 023.png
>     -rw-r--r-- 1 root root 7368 Jan  1  1970 024.png
>     -rw-r--r-- 1 root root 7384 Jan  1  1970 025.png
>     -rw-r--r-- 1 root root 7400 Jan  1  1970 026.png
>     -rw-r--r-- 1 root root 7368 Jan  1  1970 027.png
>     -rw-r--r-- 1 root root 7371 Jan  1  1970 028.png
>     -rw-r--r-- 1 root root 7357 Jan  1  1970 029.png
>     -rw-r--r-- 1 root root 7415 Jan  1  1970 030.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 031.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 032.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 033.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 034.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 035.png
>     -rw-r--r-- 1 root root 7380 Jan  1  1970 036.png
>     -rw-r--r-- 1 root root 7386 Jan  1  1970 037.png
>     -rw-r--r-- 1 root root 7392 Jan  1  1970 038.png
>     -rw-r--r-- 1 root root 7356 Jan  1  1970 039.png
>     -rw-r--r-- 1 root root 7465 Jan  1  1970 040.png
>     -rw-r--r-- 1 root root 7402 Jan  1  1970 041.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 042.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 043.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 044.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 045.png
>     -rw-r--r-- 1 root root 7382 Jan  1  1970 046.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 047.png
>     -rw-r--r-- 1 root root 7383 Jan  1  1970 048.png
>     -rw-r--r-- 1 root root 7356 Jan  1  1970 049.png
>     -rw-r--r-- 1 root root 7394 Jan  1  1970 050.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 051.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 052.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 053.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 054.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 055.png
>     -rw-r--r-- 1 root root 7400 Jan  1  1970 056.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 057.png
>     -rw-r--r-- 1 root root 7371 Jan  1  1970 058.png
>     -rw-r--r-- 1 root root 7336 Jan  1  1970 059.png
>     -rw-r--r-- 1 root root 7428 Jan  1  1970 060.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 061.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 062.png
>     -rw-r--r-- 1 root root 7367 Jan  1  1970 063.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 064.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 065.png
>     -rw-r--r-- 1 root root 7380 Jan  1  1970 066.png
>     -rw-r--r-- 1 root root 7365 Jan  1  1970 067.png
>     -rw-r--r-- 1 root root 7369 Jan  1  1970 068.png
>     -rw-r--r-- 1 root root 7336 Jan  1  1970 069.png
>     -rw-r--r-- 1 root root 7393 Jan  1  1970 070.png
>     -rw-r--r-- 1 root root 7365 Jan  1  1970 071.png
>     -rw-r--r-- 1 root root 7384 Jan  1  1970 072.png
>     -rw-r--r-- 1 root root 7384 Jan  1  1970 073.png
>     -rw-r--r-- 1 root root 7384 Jan  1  1970 074.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 075.png
>     -rw-r--r-- 1 root root 7401 Jan  1  1970 076.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 077.png
>     -rw-r--r-- 1 root root 7372 Jan  1  1970 078.png
>     -rw-r--r-- 1 root root 7338 Jan  1  1970 079.png
>     -rw-r--r-- 1 root root 7414 Jan  1  1970 080.png
>     -rw-r--r-- 1 root root 7364 Jan  1  1970 081.png
>     -rw-r--r-- 1 root root 7364 Jan  1  1970 082.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 083.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 084.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 085.png
>     -rw-r--r-- 1 root root 7380 Jan  1  1970 086.png
>     -rw-r--r-- 1 root root 7386 Jan  1  1970 087.png
>     -rw-r--r-- 1 root root 7392 Jan  1  1970 088.png
>     -rw-r--r-- 1 root root 7356 Jan  1  1970 089.png
>     -rw-r--r-- 1 root root 7430 Jan  1  1970 090.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 091.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 092.png
>     -rw-r--r-- 1 root root 7385 Jan  1  1970 093.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 094.png
>     -rw-r--r-- 1 root root 7384 Jan  1  1970 095.png
>     -rw-r--r-- 1 root root 7383 Jan  1  1970 096.png
>     -rw-r--r-- 1 root root 7366 Jan  1  1970 097.png
>     -rw-r--r-- 1 root root 7372 Jan  1  1970 098.png
>     -rw-r--r-- 1 root root 7338 Jan  1  1970 099.png

``` python
import imageio

images = []
filenames = ["/dbfs/keras_rl/images/{:03d}.png".format(x) for x in xrange(100)]
```

``` python
for filename in filenames:
    images.append(imageio.imread(filename))
imageio.mimsave('/dbfs/FileStore/movie.gif', images)
```

<img src="/files/movie.gif">

### Where to Go Next?

The following articles are great next steps:

-   Flappy Bird with DQL and Keras: https://yanpanlau.github.io/2016/07/10/FlappyBird-Keras.html
-   DQL with Keras and an Open AI Gym task: http://koaning.io/hello-deepq.html
-   Simple implementation with Open AI Gym support: https://github.com/sherjilozair/dqn

This project offers Keras add-on classes for simple experimentation with DQL: \* https://github.com/farizrahman4u/qlearning4k \* Note that you'll need to implement (or wrap) the "game" to plug into that framework

Try it at home: \* Hack the "Keras Plays Catch" demo to allow the ball to drift horizontally as it falls. Does it work? \* Try training the network on "delta frames" instead of static frames. This gives the network information about motion (implicitly). \* What if the screen is high-resolution? what happens? how could you handle it better?

And if you have the sneaking suspicion that there is a connection between PG and DQL, you'd be right: https://arxiv.org/abs/1704.06440

[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind). This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.