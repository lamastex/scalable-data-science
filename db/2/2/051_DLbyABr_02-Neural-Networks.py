# Databricks notebook source
# MAGIC %md
# MAGIC 
# MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
# MAGIC 
# MAGIC This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
# MAGIC This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. 
# MAGIC A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

# COMMAND ----------

# MAGIC %md
# MAGIC Archived YouTube video of this live unedited lab-lecture:
# MAGIC 
# MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/eJBR6sm4p2g/0.jpg)](https://www.youtube.com/embed/eJBR6sm4p2g?start=0&end=2654&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/TDisCsfbmYs/0.jpg)](https://www.youtube.com/embed/TDisCsfbmYs?start=0&end=2907&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/-LLL3MUl9ps/0.jpg)](https://www.youtube.com/embed/-LLL3MUl9ps?start=0&end=2467&autoplay=1)

# COMMAND ----------

# MAGIC %md 
# MAGIC # Artificial Neural Network - Perceptron
# MAGIC 
# MAGIC The field of artificial neural networks started out with an electromechanical binary unit called a perceptron.
# MAGIC 
# MAGIC The perceptron took a weighted set of input signals and chose an ouput state (on/off or high/low) based on a threshold.
# MAGIC 
# MAGIC <img src="http://i.imgur.com/c4pBaaU.jpg">

# COMMAND ----------

# MAGIC %md
# MAGIC (raaz) Thus, the perceptron is defined by:
# MAGIC 
# MAGIC $$
# MAGIC f(1, x\_1,x\_2,\ldots , x\_n \, ; \, w\_0,w\_1,w\_2,\ldots , w\_n) =
# MAGIC \begin{cases}
# MAGIC 1 & \text{if} \quad \sum\_{i=0}^n w\_i x\_i > 0 \\\\
# MAGIC 0 & \text{otherwise}
# MAGIC \end{cases}
# MAGIC $$
# MAGIC and implementable with the following arithmetical and logical unit (ALU) operations in a machine:
# MAGIC 
# MAGIC  * n inputs from one \\(n\\)-dimensional data point: \\( x_1,x_2,\ldots x_n \, \in \, \mathbb{R}^n\\)
# MAGIC  * arithmetic operations
# MAGIC    * n+1 multiplications
# MAGIC    * n additions
# MAGIC * boolean operations
# MAGIC   * one if-then on an inequality
# MAGIC * one output \\(o \in \\{0,1\\}\\), i.e., \\(o\\) belongs to the set containing \\(0\\) and \\(1\\)
# MAGIC * n+1 parameters of interest
# MAGIC 
# MAGIC This is just a hyperplane given by a dot product of \\(n+1\\) known inputs and \\(n+1\\) unknown parameters that can be estimated. This hyperplane can be used to define a hyperplane that partitions \\(\mathbb{R}^{n+1}\\), the real Euclidean space, into two parts labelled by the outputs \\(0\\) and \\(1\\).
# MAGIC 
# MAGIC The problem of finding estimates of the parameters, \\( (\hat{w}\_0,\hat{w}\_1,\hat{w}\_2,\ldots \hat{w}\_n) \in \mathbb{R}^{(n+1)} \\), in some statistically meaningful manner for a predicting task by using the training data given by, say \\(k\\) *labelled points*, where you know both the input and output:
# MAGIC $$
# MAGIC  \left( ( \, 1, x\_1^{(1)},x\_2^{(1)}, \ldots x\_n^{(1)}), (o^{(1)}) \, ), \, ( \, 1, x\_1^{(2)},x\_2^{(2)}, \ldots x\_n^{(2)}), (o^{(2)}) \, ), \, \ldots \, , ( \, 1, x\_1^{(k)},x\_2^{(k)}, \ldots x\_n^{(k)}), (o^{(k)}) \, ) \right) \, \in \, (\mathbb{R}^{n+1} \times \\{ 0,1 \\} )^k
# MAGIC $$
# MAGIC is the machine learning problem here. 
# MAGIC 
# MAGIC Succinctly, we are after a random mapping, denoted below by \\( \mapsto\_{\rightsquigarrow} \\), called the *estimator*:
# MAGIC $$
# MAGIC (\mathbb{R}^{n+1} \times \\{0,1\\})^k \mapsto_{\rightsquigarrow} \, \left( \, \mathtt{model}( (1,x\_1,x\_2,\ldots,x\_n) \,;\, (\hat{w}\_0,\hat{w}\_1,\hat{w}\_2,\ldots \hat{w}\_n)) : \mathbb{R}^{n+1} \to \\{0,1\\} \,  \right)
# MAGIC $$
# MAGIC which takes *random* labelled dataset (to understand random here think of two scientists doing independent experiments to get their own training datasets) of size \\(k\\) and returns a *model*. These mathematical notions correspond exactly to the `estimator` and `model` (which is a `transformer`) in the language of Apache Spark's Machine Learning Pipleines we have seen before.
# MAGIC 
# MAGIC We can use this `transformer` for *prediction* of *unlabelled data* where we only observe the input and what to know the output under some reasonable assumptions.  
# MAGIC 
# MAGIC Of course we want to be able to generalize so we don't overfit to the training data using some *empirical risk minisation rule* such as cross-validation. Again, we have seen these in Apache Spark for other ML methods like linear regression and decision trees.

# COMMAND ----------

# MAGIC %md 
# MAGIC If the output isn't right, we can adjust the weights, threshold, or bias (\\(x_0\\) above)
# MAGIC 
# MAGIC The model was inspired by discoveries about the neurons of animals, so hopes were quite high that it could lead to a sophisticated machine. This model can be extended by adding multiple neurons in parallel. And we can use linear output instead of a threshold if we like for the output.
# MAGIC 
# MAGIC If we were to do so, the output would look like \\({x \cdot w} + w_0\\) (this is where the vector multiplication and, eventually, matrix multiplication, comes in)
# MAGIC 
# MAGIC When we look at the math this way, we see that despite this being an interesting model, it's really just a fancy linear calculation.
# MAGIC 
# MAGIC And, in fact, the proof that this model -- being linear -- could not solve any problems whose solution was nonlinear ... led to the first of several "AI / neural net winters" when the excitement was quickly replaced by disappointment, and most research was abandoned.

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Linear Perceptron
# MAGIC 
# MAGIC We'll get to the non-linear part, but the linear perceptron model is a great way to warm up and bridge the gap from traditional linear regression to the neural-net flavor.
# MAGIC 
# MAGIC Let's look at a problem -- the diamonds dataset from R -- and analyze it using two traditional methods in Scikit-Learn, and then we'll start attacking it with neural networks!

# COMMAND ----------

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor
from sklearn.metrics import mean_squared_error

input_file = "/dbfs/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv"

df = pd.read_csv(input_file, header = 0)

# COMMAND ----------

import IPython.display as disp
pd.set_option('display.width', 200)
disp.display(df[:10])

# COMMAND ----------

df2 = df.drop(df.columns[0], axis=1)

disp.display(df2[:3])

# COMMAND ----------

df3 = pd.get_dummies(df2) # this gives a one-hot encoding of categorial variables

disp.display(df3[range(7,18)][:3])

# COMMAND ----------

# pre-process to get y
y = df3.iloc[:,3:4].as_matrix().flatten()
y.flatten()

# preprocess and reshape X as a matrix
X = df3.drop(df3.columns[3], axis=1).as_matrix()
np.shape(X)

# break the dataset into training and test set with a 75% and 25% split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=42)

# Define a decisoin tree model with max depth 10
dt = DecisionTreeRegressor(random_state=0, max_depth=10)

# fit the decision tree to the training data to get a fitted model
model = dt.fit(X_train, y_train)

# predict the features or X values of the test data using the fitted model
y_pred = model.predict(X_test)

# print the MSE performance measure of the fit by comparing the predicted versus the observed values of y 
print("RMSE %f" % np.sqrt(mean_squared_error(y_test, y_pred)) )

# COMMAND ----------

from sklearn import linear_model

# Do the same with linear regression and not a worse MSE
lr = linear_model.LinearRegression()
linear_model = lr.fit(X_train, y_train)

y_pred = linear_model.predict(X_test)
print("RMSE %f" % np.sqrt(mean_squared_error(y_test, y_pred)) )

# COMMAND ----------

# MAGIC %md 
# MAGIC Now that we have a baseline, let's build a neural network -- linear at first -- and go further.

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Neural Network with Keras
# MAGIC 
# MAGIC ### Keras is a High-Level API for Neural Networks and Deep Learning
# MAGIC 
# MAGIC #### "*Being able to go from idea to result with the least possible delay is key to doing good research.*"
# MAGIC Maintained by Francois Chollet at Google, it provides
# MAGIC 
# MAGIC * High level APIs
# MAGIC * Pluggable backends for Theano, TensorFlow, CNTK, MXNet
# MAGIC * CPU/GPU support
# MAGIC * The now-officially-endorsed high-level wrapper for TensorFlow; a version ships in TF
# MAGIC * Model persistence and other niceties
# MAGIC * JavaScript, iOS, etc. deployment
# MAGIC * Interop with further frameworks, like DeepLearning4J, Spark DL Pipelines ...
# MAGIC 
# MAGIC Well, with all this, why would you ever *not* use Keras? 
# MAGIC 
# MAGIC As an API/Facade, Keras doesn't directly expose all of the internals you might need for something custom and low-level ... so you might need to implement at a lower level first, and then perhaps wrap it to make it easily usable in Keras.
# MAGIC 
# MAGIC Mr. Chollet compiles stats (roughly quarterly) on "[t]he state of the deep learning landscape: GitHub activity of major libraries over the past quarter (tickets, forks, and contributors)."
# MAGIC 
# MAGIC (October 2017: https://twitter.com/fchollet/status/915366704401719296; https://twitter.com/fchollet/status/915626952408436736)
# MAGIC <table><tr><td>__GitHub__<br>
# MAGIC <img src="https://i.imgur.com/Dru8N9K.jpg" width=600>
# MAGIC   </td><td>__Research__<br>
# MAGIC   <img src="https://i.imgur.com/i23TAwf.png" width=600></td></tr></table>

# COMMAND ----------

# MAGIC %md 
# MAGIC ### We'll build a "Dense Feed-Forward Shallow" Network:
# MAGIC (the number of units in the following diagram does not exactly match ours)
# MAGIC <img src="https://i.imgur.com/84fxFKa.png">
# MAGIC 
# MAGIC Grab a Keras API cheat sheet from https://s3.amazonaws.com/assets.datacamp.com/blog_assets/Keras_Cheat_Sheet_Python.pdf

# COMMAND ----------

from keras.models import Sequential
from keras.layers import Dense

# we are going to add layers sequentially one after the other (feed-forward) to our neural network model
model = Sequential()

# the first layer has 30 nodes (or neurons) with input dimension 26 for our diamonds data
# we will use Nomal or Guassian kernel to initialise the weights we want to estimate
# our activation function is linear (to mimic linear regression)
model.add(Dense(30, input_dim=26, kernel_initializer='normal', activation='linear'))
# the next layer is for the response y and has only one node
model.add(Dense(1, kernel_initializer='normal', activation='linear'))
# compile the model with other specifications for loss and type of gradient descent optimisation routine
model.compile(loss='mean_squared_error', optimizer='adam', metrics=['mean_squared_error'])
# fit the model to the training data using stochastic gradient descent with a batch-size of 200 and 10% of data held out for validation
history = model.fit(X_train, y_train, epochs=10, batch_size=200, validation_split=0.1)

scores = model.evaluate(X_test, y_test)
print()
print("test set RMSE: %f" % np.sqrt(scores[1]))

# COMMAND ----------

model.summary() # do you understand why the number of parameters in layer 1 is 810? 26*30+30=810

# COMMAND ----------

# MAGIC %md 
# MAGIC Notes:
# MAGIC 
# MAGIC * We didn't have to explicitly write the "input" layer, courtesy of the Keras API. We just said `input_dim=26` on the first (and only) hidden layer.
# MAGIC * `kernel_initializer='normal'` is a simple (though not always optimal) *weight initialization*
# MAGIC * Epoch: 1 pass over all of the training data
# MAGIC * Batch: Records processes together in a single training pass
# MAGIC 
# MAGIC How is our RMSE vs. the std dev of the response?

# COMMAND ----------

y.std()

# COMMAND ----------

# MAGIC %md 
# MAGIC Let's look at the error ...

# COMMAND ----------

import matplotlib.pyplot as plt

fig, ax = plt.subplots()
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'val'], loc='upper left')

display(fig)

# COMMAND ----------

# MAGIC %md 
# MAGIC Let's set up a "long-running" training. This will take a few minutes to converge to the same performance we got more or less instantly with our sklearn linear regression :)
# MAGIC 
# MAGIC While it's running, we can talk about the training.

# COMMAND ----------

from keras.models import Sequential
from keras.layers import Dense
import numpy as np
import pandas as pd

input_file = "/dbfs/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv"

df = pd.read_csv(input_file, header = 0)
df.drop(df.columns[0], axis=1, inplace=True)
df = pd.get_dummies(df, prefix=['cut_', 'color_', 'clarity_'])

y = df.iloc[:,3:4].as_matrix().flatten()
y.flatten()

X = df.drop(df.columns[3], axis=1).as_matrix()
np.shape(X)

from sklearn.model_selection import train_test_split

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=42)

model = Sequential()
model.add(Dense(30, input_dim=26, kernel_initializer='normal', activation='linear'))
model.add(Dense(1, kernel_initializer='normal', activation='linear'))

model.compile(loss='mean_squared_error', optimizer='adam', metrics=['mean_squared_error'])
history = model.fit(X_train, y_train, epochs=250, batch_size=100, validation_split=0.1, verbose=2)

scores = model.evaluate(X_test, y_test)
print("\nroot %s: %f" % (model.metrics_names[1], np.sqrt(scores[1])))

# COMMAND ----------

# MAGIC %md
# MAGIC After all this hard work we are closer to the MSE we got from linear regression, but purely using a shallow feed-forward neural network.

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Training: Gradient Descent
# MAGIC 
# MAGIC A family of numeric optimization techniques, where we solve a problem with the following pattern:
# MAGIC 
# MAGIC 1. Describe the error in the model output: this is usually some difference between the the true values and the model's predicted values, as a function of the model parameters (weights)
# MAGIC 
# MAGIC 2. Compute the gradient, or directional derivative, of the error -- the "slope toward lower error"
# MAGIC 
# MAGIC 4. Adjust the parameters of the model variables in the indicated direction
# MAGIC 
# MAGIC 5. Repeat
# MAGIC 
# MAGIC <img src="https://i.imgur.com/HOYViqN.png" width=500>
# MAGIC 
# MAGIC #### Some ideas to help build your intuition
# MAGIC 
# MAGIC * What happens if the variables (imagine just 2, to keep the mental picture simple) are on wildly different scales ... like one ranges from -1 to 1 while another from -1e6 to +1e6?
# MAGIC 
# MAGIC * What if some of the variables are correlated? I.e., a change in one corresponds to, say, a linear change in another?
# MAGIC 
# MAGIC * Other things being equal, an approximate solution with fewer variables is easier to work with than one with more -- how could we get rid of some less valuable parameters? (e.g., L1 penalty)
# MAGIC 
# MAGIC * How do we know how far to "adjust" our parameters with each step?
# MAGIC 
# MAGIC <img src="http://i.imgur.com/AvM2TN6.png" width=600>
# MAGIC 
# MAGIC What if we have billions of data points? Does it makes sense to use all of them for each update? Is there a shortcut?
# MAGIC 
# MAGIC Yes: *Stochastic Gradient Descent*
# MAGIC 
# MAGIC But SGD has some shortcomings, so we typically use a "smarter" version of SGD, which has rules for adjusting the learning rate and even direction in order to avoid common problems.
# MAGIC 
# MAGIC What about that "Adam" optimizer? Adam is short for "adaptive moment" and is a variant of SGD that includes momentum calculations that change over time. For more detail on optimizers, see the chapter "Training Deep Neural Nets" in Aurélien Géron's book: *Hands-On Machine Learning with Scikit-Learn and TensorFlow* (http://shop.oreilly.com/product/0636920052289.do)

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Training: Backpropagation
# MAGIC 
# MAGIC With a simple, flat model, we could use SGD or a related algorithm to derive the weights, since the error depends directly on those weights.
# MAGIC 
# MAGIC With a deeper network, we have a couple of challenges:
# MAGIC 
# MAGIC * The error is computed from the final layer, so the gradient of the error doesn't tell us immediately about problems in other-layer weights
# MAGIC * Our tiny diamonds model has almost a thousand weights. Bigger models can easily have millions of weights. Each of those weights may need to move a little at a time, and we have to watch out for underflow or undersignificance situations.
# MAGIC 
# MAGIC __The insight is to iteratively calculate errors, one layer at a time, starting at the output. This is called backpropagation. It is neither magical nor surprising. The challenge is just doing it fast and not losing information.__
# MAGIC 
# MAGIC <img src="http://i.imgur.com/bjlYwjM.jpg" width=800>

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Ok so we've come up with a very slow way to perform a linear regression. 
# MAGIC 
# MAGIC ### *Welcome to Neural Networks in the 1960s!*
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC ### Watch closely now because this is where the magic happens...
# MAGIC 
# MAGIC <img src="https://media.giphy.com/media/Hw5LkPYy9yfVS/giphy.gif">

# COMMAND ----------

# MAGIC %md 
# MAGIC # Non-Linearity + Perceptron = Universal Approximation

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Where does the non-linearity fit in?
# MAGIC 
# MAGIC * We start with the inputs to a perceptron -- these could be from source data, for example.
# MAGIC * We multiply each input by its respective weight, which gets us the \\(x \cdot w\\)
# MAGIC * Then add the "bias" -- an extra learnable parameter, to get \\({x \cdot w} + b\\)
# MAGIC     * This value (so far) is sometimes called the "pre-activation"
# MAGIC * Now, apply a non-linear "activation function" to this value, such as the logistic sigmoid
# MAGIC 
# MAGIC <img src="https://i.imgur.com/MhokAmo.gif">
# MAGIC 
# MAGIC ### Now the network can "learn" non-linear functions
# MAGIC 
# MAGIC To gain some intuition, consider that where the sigmoid is close to 1, we can think of that neuron as being "on" or activated, giving a specific output. When close to zero, it is "off." 
# MAGIC 
# MAGIC So each neuron is a bit like a switch. If we have enough of them, we can theoretically express arbitrarily many different signals. 
# MAGIC 
# MAGIC In some ways this is like the original artificial neuron, with the thresholding output -- the main difference is that the sigmoid gives us a smooth (arbitrarily differentiable) output that we can optimize over using gradient descent to learn the weights. 
# MAGIC 
# MAGIC ### Where does the signal "go" from these neurons?
# MAGIC 
# MAGIC * In a regression problem, like the diamonds dataset, the activations from the hidden layer can feed into a single output neuron, with a simple linear activation representing the final output of the calculation.
# MAGIC 
# MAGIC * Frequently we want a classification output instead -- e.g., with MNIST digits, where we need to choose from 10 classes. In that case, we can feed the outputs from these hidden neurons forward into a final layer of 10 neurons, and compare those final neurons' activation levels.
# MAGIC 
# MAGIC Ok, before we talk any more theory, let's run it and see if we can do better on our diamonds dataset adding this "sigmoid activation."
# MAGIC 
# MAGIC While that's running, let's look at the code:

# COMMAND ----------

from keras.models import Sequential
from keras.layers import Dense
import numpy as np
import pandas as pd

input_file = "/dbfs/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv"

df = pd.read_csv(input_file, header = 0)
df.drop(df.columns[0], axis=1, inplace=True)
df = pd.get_dummies(df, prefix=['cut_', 'color_', 'clarity_'])

y = df.iloc[:,3:4].as_matrix().flatten()
y.flatten()

X = df.drop(df.columns[3], axis=1).as_matrix()
np.shape(X)

from sklearn.model_selection import train_test_split

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=42)

model = Sequential()
model.add(Dense(30, input_dim=26, kernel_initializer='normal', activation='sigmoid')) # <- change to nonlinear activation
model.add(Dense(1, kernel_initializer='normal', activation='linear')) # <- activation is linear in output layer for this regression

model.compile(loss='mean_squared_error', optimizer='adam', metrics=['mean_squared_error'])
history = model.fit(X_train, y_train, epochs=2000, batch_size=100, validation_split=0.1, verbose=2)

scores = model.evaluate(X_test, y_test)
print("\nroot %s: %f" % (model.metrics_names[1], np.sqrt(scores[1])))

# COMMAND ----------

# MAGIC %md 
# MAGIC ##### What is different here?
# MAGIC 
# MAGIC * We've changed the activation in the hidden layer to "sigmoid" per our discussion.
# MAGIC * Next, notice that we're running 2000 training epochs!
# MAGIC 
# MAGIC Even so, it takes a looooong time to converge. If you experiment a lot, you'll find that ... it still takes a long time to converge. Around the early part of the most recent deep learning renaissance, researchers started experimenting with other non-linearities.
# MAGIC 
# MAGIC (Remember, we're talking about non-linear activations in the hidden layer. The output here is still using "linear" rather than "softmax" because we're performing regression, not classification.)
# MAGIC 
# MAGIC In theory, any non-linearity should allow learning, and maybe we can use one that "works better"
# MAGIC 
# MAGIC By "works better" we mean
# MAGIC 
# MAGIC * Simpler gradient - faster to compute
# MAGIC * Less prone to "saturation" -- where the neuron ends up way off in the 0 or 1 territory of the sigmoid and can't easily learn anything
# MAGIC * Keeps gradients "big" -- avoiding the large, flat, near-zero gradient areas of the sigmoid
# MAGIC 
# MAGIC Turns out that a big breakthrough and popular solution is a very simple hack:
# MAGIC 
# MAGIC ### Rectified Linear Unit (ReLU)
# MAGIC 
# MAGIC <img src="http://i.imgur.com/oAYh9DN.png" width=1000>

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Go change your hidden-layer activation from 'sigmoid' to 'relu'
# MAGIC 
# MAGIC Start your script and watch the error for a bit!

# COMMAND ----------

from keras.models import Sequential
from keras.layers import Dense
import numpy as np
import pandas as pd

input_file = "/dbfs/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv"

df = pd.read_csv(input_file, header = 0)
df.drop(df.columns[0], axis=1, inplace=True)
df = pd.get_dummies(df, prefix=['cut_', 'color_', 'clarity_'])

y = df.iloc[:,3:4].as_matrix().flatten()
y.flatten()

X = df.drop(df.columns[3], axis=1).as_matrix()
np.shape(X)

from sklearn.model_selection import train_test_split

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=42)

model = Sequential()
model.add(Dense(30, input_dim=26, kernel_initializer='normal', activation='relu')) # <--- CHANGE IS HERE
model.add(Dense(1, kernel_initializer='normal', activation='linear'))

model.compile(loss='mean_squared_error', optimizer='adam', metrics=['mean_squared_error'])
history = model.fit(X_train, y_train, epochs=2000, batch_size=100, validation_split=0.1, verbose=2)

scores = model.evaluate(X_test, y_test)
print("\nroot %s: %f" % (model.metrics_names[1], np.sqrt(scores[1])))

# COMMAND ----------

# MAGIC %md 
# MAGIC Would you look at that?! 
# MAGIC 
# MAGIC * We break $1000 RMSE around epoch 112
# MAGIC * $900 around epoch 220
# MAGIC * $800 around epoch 450
# MAGIC * By around epoch 2000, my RMSE is < $600
# MAGIC 
# MAGIC ...
# MAGIC 
# MAGIC 
# MAGIC __Same theory; different activation function. Huge difference__

# COMMAND ----------

# MAGIC %md 
# MAGIC # Multilayer Networks
# MAGIC 
# MAGIC If a single-layer perceptron network learns the importance of different combinations of features in the data...
# MAGIC 
# MAGIC What would another network learn if it had a second (hidden) layer of neurons?
# MAGIC 
# MAGIC It depends on how we train the network. We'll talk in the next section about how this training works, but the general idea is that we still work backward from the error gradient. 
# MAGIC 
# MAGIC That is, the last layer learns from error in the output; the second-to-last layer learns from error transmitted through that last layer, etc. It's a touch hand-wavy for now, but we'll make it more concrete later.
# MAGIC 
# MAGIC Given this approach, we can say that:
# MAGIC 
# MAGIC 1. The second (hidden) layer is learning features composed of activations in the first (hidden) layer
# MAGIC 2. The first (hidden) layer is learning feature weights that enable the second layer to perform best 
# MAGIC     * Why? Earlier, the first hidden layer just learned feature weights because that's how it was judged
# MAGIC     * Now, the first hidden layer is judged on the error in the second layer, so it learns to contribute to that second layer
# MAGIC 3. The second layer is learning new features that aren't explicit in the data, and is teaching the first layer to supply it with the necessary information to compose these new features
# MAGIC 
# MAGIC ### So instead of just feature weighting and combining, we have new feature learning!
# MAGIC 
# MAGIC This concept is the foundation of the "Deep Feed-Forward Network"
# MAGIC 
# MAGIC <img src="http://i.imgur.com/fHGrs4X.png">
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC ### Let's try it!
# MAGIC 
# MAGIC __Add a layer to your Keras network, perhaps another 20 neurons, and see how the training goes.__
# MAGIC 
# MAGIC if you get stuck, there is a solution in the Keras-DFFN notebook
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC I'm getting RMSE < $1000 by epoch 35 or so
# MAGIC 
# MAGIC < $800 by epoch 90
# MAGIC 
# MAGIC In this configuration, mine makes progress to around 700 epochs or so and then stalls with RMSE around $560

# COMMAND ----------

# MAGIC %md 
# MAGIC ### Our network has "gone meta"
# MAGIC 
# MAGIC It's now able to exceed where a simple decision tree can go, because it can create new features and then split on those
# MAGIC 
# MAGIC ## Congrats! You have built your first deep-learning model!
# MAGIC 
# MAGIC So does that mean we can just keep adding more layers and solve anything?
# MAGIC 
# MAGIC Well, theoretically maybe ... try reconfiguring your network, watch the training, and see what happens.
# MAGIC 
# MAGIC <img src="http://i.imgur.com/BumsXgL.jpg" width=500>