[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses.
A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

Archived YouTube video of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/eJBR6sm4p2g/0.jpg)](https://www.youtube.com/embed/eJBR6sm4p2g?start=0&end=2654&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/TDisCsfbmYs/0.jpg)](https://www.youtube.com/embed/TDisCsfbmYs?start=0&end=2907&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/-LLL3MUl9ps/0.jpg)](https://www.youtube.com/embed/-LLL3MUl9ps?start=0&end=2467&autoplay=1)

Artificial Neural Network - Perceptron
======================================

The field of artificial neural networks started out with an electromechanical binary unit called a perceptron.

The perceptron took a weighted set of input signals and chose an ouput state (on/off or high/low) based on a threshold.

<img src="http://i.imgur.com/c4pBaaU.jpg">

(raaz) Thus, the perceptron is defined by:

$$
f(1, x\_1,x\_2,\ldots , x\_n \\, ; \\, w\_0,w\_1,w\_2,\ldots , w\_n) =
\begin{cases}
1 & \text{if} \quad \sum\_{i=0}^n w\_i x\_i > 0 \\\\
0 & \text{otherwise}
\end{cases}
$$
and implementable with the following arithmetical and logical unit (ALU) operations in a machine:

-   n inputs from one \\(n\\)-dimensional data point: \\( x*1,x*2,\ldots x\_n \\, \in \\, \mathbb{R}^n\\)
-   arithmetic operations
    -   n+1 multiplications
    -   n additions
-   boolean operations
    -   one if-then on an inequality
-   one output \\(o \in \\{0,1\\}\\), i.e., \\(o\\) belongs to the set containing \\(0\\) and \\(1\\)
-   n+1 parameters of interest

This is just a hyperplane given by a dot product of \\(n+1\\) known inputs and \\(n+1\\) unknown parameters that can be estimated. This hyperplane can be used to define a hyperplane that partitions \\(\mathbb{R}^{n+1}\\), the real Euclidean space, into two parts labelled by the outputs \\(0\\) and \\(1\\).

The problem of finding estimates of the parameters, \\( (\hat{w}\_0,\hat{w}\_1,\hat{w}\_2,\ldots \hat{w}\_n) \in \mathbb{R}^{(n+1)} \\), in some statistically meaningful manner for a predicting task by using the training data given by, say \\(k\\) *labelled points*, where you know both the input and output:
$$
\left( ( \\, 1, x\_1^{(1)},x\_2^{(1)}, \ldots x\_n^{(1)}), (o^{(1)}) \\, ), \\, ( \\, 1, x\_1^{(2)},x\_2^{(2)}, \ldots x\_n^{(2)}), (o^{(2)}) \\, ), \\, \ldots \\, , ( \\, 1, x\_1^{(k)},x\_2^{(k)}, \ldots x\_n^{(k)}), (o^{(k)}) \\, ) \right) \\, \in \\, (\mathbb{R}^{n+1} \times \\{ 0,1 \\} )^k
$$
is the machine learning problem here.

Succinctly, we are after a random mapping, denoted below by \\( \mapsto\_{\rightsquigarrow} \\), called the *estimator*:
$$
(\mathbb{R}^{n+1} \times \\{0,1\\})^k \mapsto\_{\rightsquigarrow} \\, \left( \\, \mathtt{model}( (1,x\_1,x\_2,\ldots,x\_n) \\,;\\, (\hat{w}\_0,\hat{w}\_1,\hat{w}\_2,\ldots \hat{w}\_n)) : \mathbb{R}^{n+1} \to \\{0,1\\} \\, \right)
$$
which takes *random* labelled dataset (to understand random here think of two scientists doing independent experiments to get their own training datasets) of size \\(k\\) and returns a *model*. These mathematical notions correspond exactly to the `estimator` and `model` (which is a `transformer`) in the language of Apache Spark's Machine Learning Pipleines we have seen before.

We can use this `transformer` for *prediction* of *unlabelled data* where we only observe the input and what to know the output under some reasonable assumptions.

Of course we want to be able to generalize so we don't overfit to the training data using some *empirical risk minisation rule* such as cross-validation. Again, we have seen these in Apache Spark for other ML methods like linear regression and decision trees.

If the output isn't right, we can adjust the weights, threshold, or bias (\\(x\_0\\) above)

The model was inspired by discoveries about the neurons of animals, so hopes were quite high that it could lead to a sophisticated machine. This model can be extended by adding multiple neurons in parallel. And we can use linear output instead of a threshold if we like for the output.

If we were to do so, the output would look like \\({x \cdot w} + w\_0\\) (this is where the vector multiplication and, eventually, matrix multiplication, comes in)

When we look at the math this way, we see that despite this being an interesting model, it's really just a fancy linear calculation.

And, in fact, the proof that this model -- being linear -- could not solve any problems whose solution was nonlinear ... led to the first of several "AI / neural net winters" when the excitement was quickly replaced by disappointment, and most research was abandoned.

### Linear Perceptron

We'll get to the non-linear part, but the linear perceptron model is a great way to warm up and bridge the gap from traditional linear regression to the neural-net flavor.

Let's look at a problem -- the diamonds dataset from R -- and analyze it using two traditional methods in Scikit-Learn, and then we'll start attacking it with neural networks!

``` python
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor
from sklearn.metrics import mean_squared_error

input_file = "/dbfs/databricks-datasets/Rdatasets/data-001/csv/ggplot2/diamonds.csv"

df = pd.read_csv(input_file, header = 0)
```

``` python
import IPython.display as disp
pd.set_option('display.width', 200)
disp.display(df[:10])
```

>        Unnamed: 0  carat        cut color clarity  depth  table  price     x     y     z
>     0           1   0.23      Ideal     E     SI2   61.5   55.0    326  3.95  3.98  2.43
>     1           2   0.21    Premium     E     SI1   59.8   61.0    326  3.89  3.84  2.31
>     2           3   0.23       Good     E     VS1   56.9   65.0    327  4.05  4.07  2.31
>     3           4   0.29    Premium     I     VS2   62.4   58.0    334  4.20  4.23  2.63
>     4           5   0.31       Good     J     SI2   63.3   58.0    335  4.34  4.35  2.75
>     5           6   0.24  Very Good     J    VVS2   62.8   57.0    336  3.94  3.96  2.48
>     6           7   0.24  Very Good     I    VVS1   62.3   57.0    336  3.95  3.98  2.47
>     7           8   0.26  Very Good     H     SI1   61.9   55.0    337  4.07  4.11  2.53
>     8           9   0.22       Fair     E     VS2   65.1   61.0    337  3.87  3.78  2.49
>     9          10   0.23  Very Good     H     VS1   59.4   61.0    338  4.00  4.05  2.39

``` python
df2 = df.drop(df.columns[0], axis=1)

disp.display(df2[:3])
```

>        carat      cut color clarity  depth  table  price     x     y     z
>     0   0.23    Ideal     E     SI2   61.5   55.0    326  3.95  3.98  2.43
>     1   0.21  Premium     E     SI1   59.8   61.0    326  3.89  3.84  2.31
>     2   0.23     Good     E     VS1   56.9   65.0    327  4.05  4.07  2.31

``` python
df3 = pd.get_dummies(df2) # this gives a one-hot encoding of categorial variables

disp.display(df3[range(7,18)][:3])
```

>        cut_Fair  cut_Good  cut_Ideal  cut_Premium  cut_Very Good  color_D  color_E  color_F  color_G  color_H  color_I
>     0       0.0       0.0        1.0          0.0            0.0      0.0      1.0      0.0      0.0      0.0      0.0
>     1       0.0       0.0        0.0          1.0            0.0      0.0      1.0      0.0      0.0      0.0      0.0
>     2       0.0       1.0        0.0          0.0            0.0      0.0      1.0      0.0      0.0      0.0      0.0

``` python
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
```

>     RMSE 726.921870

``` python
from sklearn import linear_model

# Do the same with linear regression and not a worse MSE
lr = linear_model.LinearRegression()
linear_model = lr.fit(X_train, y_train)

y_pred = linear_model.predict(X_test)
print("RMSE %f" % np.sqrt(mean_squared_error(y_test, y_pred)) )
```

>     RMSE 1124.105695

Now that we have a baseline, let's build a neural network -- linear at first -- and go further.

Neural Network with Keras
-------------------------

### Keras is a High-Level API for Neural Networks and Deep Learning

#### "*Being able to go from idea to result with the least possible delay is key to doing good research.*"

Maintained by Francois Chollet at Google, it provides

-   High level APIs
-   Pluggable backends for Theano, TensorFlow, CNTK, MXNet
-   CPU/GPU support
-   The now-officially-endorsed high-level wrapper for TensorFlow; a version ships in TF
-   Model persistence and other niceties
-   JavaScript, iOS, etc. deployment
-   Interop with further frameworks, like DeepLearning4J, Spark DL Pipelines ...

Well, with all this, why would you ever *not* use Keras?

As an API/Facade, Keras doesn't directly expose all of the internals you might need for something custom and low-level ... so you might need to implement at a lower level first, and then perhaps wrap it to make it easily usable in Keras.

Mr. Chollet compiles stats (roughly quarterly) on "\[t\]he state of the deep learning landscape: GitHub activity of major libraries over the past quarter (tickets, forks, and contributors)."

(October 2017: https://twitter.com/fchollet/status/915366704401719296; https://twitter.com/fchollet/status/915626952408436736)
<table><tr><td>**GitHub**<br>
<img src="https://i.imgur.com/Dru8N9K.jpg" width=600>
</td><td>**Research**<br>
<img src="https://i.imgur.com/i23TAwf.png" width=600></td></tr></table>

### We'll build a "Dense Feed-Forward Shallow" Network:

(the number of units in the following diagram does not exactly match ours)
<img src="https://i.imgur.com/84fxFKa.png">

Grab a Keras API cheat sheet from https://s3.amazonaws.com/assets.datacamp.com/blog*assets/Keras*Cheat*Sheet*Python.pdf

``` python
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
```

>     Using TensorFlow backend.
>     Train on 36409 samples, validate on 4046 samples
>     Epoch 1/10
>       200/36409 [..............................] - ETA: 7s - loss: 41630676.0000 - mean_squared_error: 41630676.0000 8800/36409 [======>.......................] - ETA: 0s - loss: 31562354.3182 - mean_squared_error: 31562354.318215600/36409 [===========>..................] - ETA: 0s - loss: 31200873.7179 - mean_squared_error: 31200873.717923400/36409 [==================>...........] - ETA: 0s - loss: 31003871.1111 - mean_squared_error: 31003871.111131200/36409 [========================>.....] - ETA: 0s - loss: 30927694.8846 - mean_squared_error: 30927694.884636409/36409 [==============================] - 0s - loss: 30723977.1971 - mean_squared_error: 30723977.1971 - val_loss: 30140263.6332 - val_mean_squared_error: 30140263.6332
>     Epoch 2/10
>       200/36409 [..............................] - ETA: 0s - loss: 34870836.0000 - mean_squared_error: 34870836.0000 7800/36409 [=====>........................] - ETA: 0s - loss: 28371991.6923 - mean_squared_error: 28371991.692315200/36409 [===========>..................] - ETA: 0s - loss: 27323559.2105 - mean_squared_error: 27323559.210523400/36409 [==================>...........] - ETA: 0s - loss: 27285892.4615 - mean_squared_error: 27285892.461531400/36409 [========================>.....] - ETA: 0s - loss: 26618651.1975 - mean_squared_error: 26618651.197536409/36409 [==============================] - 0s - loss: 26190494.3583 - mean_squared_error: 26190494.3583 - val_loss: 23785609.9990 - val_mean_squared_error: 23785609.9990
>     Epoch 3/10
>       200/36409 [..............................] - ETA: 0s - loss: 19747330.0000 - mean_squared_error: 19747330.0000 8000/36409 [=====>........................] - ETA: 0s - loss: 22501587.9250 - mean_squared_error: 22501587.925015400/36409 [===========>..................] - ETA: 0s - loss: 21386750.7922 - mean_squared_error: 21386750.792223000/36409 [=================>............] - ETA: 0s - loss: 20798588.5043 - mean_squared_error: 20798588.504330600/36409 [========================>.....] - ETA: 0s - loss: 20488184.5294 - mean_squared_error: 20488184.529436409/36409 [==============================] - 0s - loss: 20013899.1387 - mean_squared_error: 20013899.1387 - val_loss: 18333722.3579 - val_mean_squared_error: 18333722.3579
>     Epoch 4/10
>       200/36409 [..............................] - ETA: 0s - loss: 15124398.0000 - mean_squared_error: 15124398.0000 8600/36409 [======>.......................] - ETA: 0s - loss: 17745193.1860 - mean_squared_error: 17745193.186017000/36409 [=============>................] - ETA: 0s - loss: 17291104.7294 - mean_squared_error: 17291104.729424400/36409 [===================>..........] - ETA: 0s - loss: 16911047.4262 - mean_squared_error: 16911047.426232400/36409 [=========================>....] - ETA: 0s - loss: 16449872.8210 - mean_squared_error: 16449872.821036409/36409 [==============================] - 0s - loss: 16320296.6595 - mean_squared_error: 16320296.6595 - val_loss: 16174183.2096 - val_mean_squared_error: 16174183.2096
>     Epoch 5/10
>       200/36409 [..............................] - ETA: 0s - loss: 15901581.0000 - mean_squared_error: 15901581.0000 8200/36409 [=====>........................] - ETA: 0s - loss: 15120474.8780 - mean_squared_error: 15120474.878016000/36409 [============>.................] - ETA: 0s - loss: 15257389.8125 - mean_squared_error: 15257389.812524000/36409 [==================>...........] - ETA: 0s - loss: 15216549.3250 - mean_squared_error: 15216549.325030000/36409 [=======================>......] - ETA: 0s - loss: 15251880.6800 - mean_squared_error: 15251880.680036409/36409 [==============================] - 0s - loss: 15258273.3781 - mean_squared_error: 15258273.3781 - val_loss: 15732586.1869 - val_mean_squared_error: 15732586.1869
>     Epoch 6/10
>       200/36409 [..............................] - ETA: 0s - loss: 16544201.0000 - mean_squared_error: 16544201.0000 8200/36409 [=====>........................] - ETA: 0s - loss: 15797484.9512 - mean_squared_error: 15797484.951215600/36409 [===========>..................] - ETA: 0s - loss: 15237369.1282 - mean_squared_error: 15237369.128223600/36409 [==================>...........] - ETA: 0s - loss: 15154847.3898 - mean_squared_error: 15154847.389831000/36409 [========================>.....] - ETA: 0s - loss: 14998747.6839 - mean_squared_error: 14998747.683936409/36409 [==============================] - 0s - loss: 15067242.1093 - mean_squared_error: 15067242.1093 - val_loss: 15622513.5002 - val_mean_squared_error: 15622513.5002
>     Epoch 7/10
>       200/36409 [..............................] - ETA: 0s - loss: 17243938.0000 - mean_squared_error: 17243938.0000 7400/36409 [=====>........................] - ETA: 0s - loss: 15400236.2973 - mean_squared_error: 15400236.297315800/36409 [============>.................] - ETA: 0s - loss: 15082681.5823 - mean_squared_error: 15082681.582323200/36409 [==================>...........] - ETA: 0s - loss: 15105369.8621 - mean_squared_error: 15105369.862129000/36409 [======================>.......] - ETA: 0s - loss: 14983111.0276 - mean_squared_error: 14983111.027634000/36409 [===========================>..] - ETA: 0s - loss: 14951375.8765 - mean_squared_error: 14951375.876536409/36409 [==============================] - 0s - loss: 14981165.4704 - mean_squared_error: 14981165.4704 - val_loss: 15534461.6772 - val_mean_squared_error: 15534461.6772
>     Epoch 8/10
>       200/36409 [..............................] - ETA: 0s - loss: 14311727.0000 - mean_squared_error: 14311727.0000 7400/36409 [=====>........................] - ETA: 0s - loss: 14943553.1081 - mean_squared_error: 14943553.108115800/36409 [============>.................] - ETA: 0s - loss: 15240938.1772 - mean_squared_error: 15240938.177223800/36409 [==================>...........] - ETA: 0s - loss: 14921181.1765 - mean_squared_error: 14921181.176531400/36409 [========================>.....] - ETA: 0s - loss: 14957169.5860 - mean_squared_error: 14957169.586036409/36409 [==============================] - 0s - loss: 14895407.4779 - mean_squared_error: 14895407.4779 - val_loss: 15439881.6915 - val_mean_squared_error: 15439881.6915
>     Epoch 9/10
>       200/36409 [..............................] - ETA: 0s - loss: 12958090.0000 - mean_squared_error: 12958090.0000 8000/36409 [=====>........................] - ETA: 0s - loss: 15037155.8250 - mean_squared_error: 15037155.825015200/36409 [===========>..................] - ETA: 0s - loss: 14812893.1316 - mean_squared_error: 14812893.131623000/36409 [=================>............] - ETA: 0s - loss: 14838274.1217 - mean_squared_error: 14838274.121731400/36409 [========================>.....] - ETA: 0s - loss: 14885261.1083 - mean_squared_error: 14885261.108336409/36409 [==============================] - 0s - loss: 14800760.7340 - mean_squared_error: 14800760.7340 - val_loss: 15339495.5161 - val_mean_squared_error: 15339495.5161
>     Epoch 10/10
>       200/36409 [..............................] - ETA: 0s - loss: 20651822.0000 - mean_squared_error: 20651822.0000 8000/36409 [=====>........................] - ETA: 0s - loss: 14836191.1000 - mean_squared_error: 14836191.100016200/36409 [============>.................] - ETA: 0s - loss: 14510127.2716 - mean_squared_error: 14510127.271624200/36409 [==================>...........] - ETA: 0s - loss: 14620299.3636 - mean_squared_error: 14620299.363632000/36409 [=========================>....] - ETA: 0s - loss: 14653516.7875 - mean_squared_error: 14653516.787536409/36409 [==============================] - 0s - loss: 14698424.9057 - mean_squared_error: 14698424.9057 - val_loss: 15227007.7899 - val_mean_squared_error: 15227007.7899
>        32/13485 [..............................] - ETA: 0s 2304/13485 [====>.........................] - ETA: 0s 4704/13485 [=========>....................] - ETA: 0s 6912/13485 [==============>...............] - ETA: 0s 9184/13485 [===================>..........] - ETA: 0s11296/13485 [========================>.....] - ETA: 0s()
>     test set RMSE: 3800.519708

``` python
model.summary() # do you understand why the number of parameters in layer 1 is 810? 26*30+30=810
```

>     _________________________________________________________________
>     Layer (type)                 Output Shape              Param #   
>     =================================================================
>     dense_1 (Dense)              (None, 30)                810       
>     _________________________________________________________________
>     dense_2 (Dense)              (None, 1)                 31        
>     =================================================================
>     Total params: 841
>     Trainable params: 841
>     Non-trainable params: 0
>     _________________________________________________________________

Notes:

-   We didn't have to explicitly write the "input" layer, courtesy of the Keras API. We just said `input_dim=26` on the first (and only) hidden layer.
-   `kernel_initializer='normal'` is a simple (though not always optimal) *weight initialization*
-   Epoch: 1 pass over all of the training data
-   Batch: Records processes together in a single training pass

How is our RMSE vs. the std dev of the response?

``` python
y.std()
```

>     Out[9]: 3989.4027576288736

Let's look at the error ...

``` python
import matplotlib.pyplot as plt

fig, ax = plt.subplots()
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'val'], loc='upper left')

display(fig)
```

Let's set up a "long-running" training. This will take a few minutes to converge to the same performance we got more or less instantly with our sklearn linear regression :)

While it's running, we can talk about the training.

``` python
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
```

>     Train on 36409 samples, validate on 4046 samples
>     Epoch 1/250
>     0s - loss: 28391144.1065 - mean_squared_error: 28391144.1065 - val_loss: 23805057.9229 - val_mean_squared_error: 23805057.9229
>     Epoch 2/250
>     0s - loss: 18247587.4769 - mean_squared_error: 18247587.4769 - val_loss: 16217050.4923 - val_mean_squared_error: 16217050.4923
>     Epoch 3/250
>     0s - loss: 15179588.2459 - mean_squared_error: 15179588.2459 - val_loss: 15630294.0064 - val_mean_squared_error: 15630294.0064
>     Epoch 4/250
>     0s - loss: 14948873.8676 - mean_squared_error: 14948873.8676 - val_loss: 15456338.4681 - val_mean_squared_error: 15456338.4681
>     Epoch 5/250
>     0s - loss: 14769464.5301 - mean_squared_error: 14769464.5301 - val_loss: 15254798.2793 - val_mean_squared_error: 15254798.2793
>     Epoch 6/250
>     0s - loss: 14556871.1373 - mean_squared_error: 14556871.1373 - val_loss: 15023575.9095 - val_mean_squared_error: 15023575.9095
>     Epoch 7/250
>     0s - loss: 14305287.6350 - mean_squared_error: 14305287.6350 - val_loss: 14731429.5413 - val_mean_squared_error: 14731429.5413
>     Epoch 8/250
>     0s - loss: 14005999.9106 - mean_squared_error: 14005999.9106 - val_loss: 14390014.4706 - val_mean_squared_error: 14390014.4706
>     Epoch 9/250
>     0s - loss: 13635220.2291 - mean_squared_error: 13635220.2291 - val_loss: 13965451.1448 - val_mean_squared_error: 13965451.1448
>     Epoch 10/250
>     0s - loss: 13181844.1010 - mean_squared_error: 13181844.1010 - val_loss: 13447188.5304 - val_mean_squared_error: 13447188.5304
>     Epoch 11/250
>     0s - loss: 12625388.0338 - mean_squared_error: 12625388.0338 - val_loss: 12812918.7079 - val_mean_squared_error: 12812918.7079
>     Epoch 12/250
>     0s - loss: 11952193.5958 - mean_squared_error: 11952193.5958 - val_loss: 12065516.3401 - val_mean_squared_error: 12065516.3401
>     Epoch 13/250
>     0s - loss: 11164978.3042 - mean_squared_error: 11164978.3042 - val_loss: 11190451.2155 - val_mean_squared_error: 11190451.2155
>     Epoch 14/250
>     0s - loss: 10283647.8857 - mean_squared_error: 10283647.8857 - val_loss: 10236634.1132 - val_mean_squared_error: 10236634.1132
>     Epoch 15/250
>     0s - loss: 9343756.9341 - mean_squared_error: 9343756.9341 - val_loss: 9249534.5126 - val_mean_squared_error: 9249534.5126
>     Epoch 16/250
>     0s - loss: 8405110.5361 - mean_squared_error: 8405110.5361 - val_loss: 8288550.0662 - val_mean_squared_error: 8288550.0662
>     Epoch 17/250
>     0s - loss: 7507658.2903 - mean_squared_error: 7507658.2903 - val_loss: 7380119.3124 - val_mean_squared_error: 7380119.3124
>     Epoch 18/250
>     0s - loss: 6677492.9130 - mean_squared_error: 6677492.9130 - val_loss: 6546181.0381 - val_mean_squared_error: 6546181.0381
>     Epoch 19/250
>     0s - loss: 5945344.7108 - mean_squared_error: 5945344.7108 - val_loss: 5824896.6070 - val_mean_squared_error: 5824896.6070
>     Epoch 20/250
>     0s - loss: 5318713.8564 - mean_squared_error: 5318713.8564 - val_loss: 5215748.3225 - val_mean_squared_error: 5215748.3225
>     Epoch 21/250
>     0s - loss: 4799188.3965 - mean_squared_error: 4799188.3965 - val_loss: 4702927.8323 - val_mean_squared_error: 4702927.8323
>     Epoch 22/250
>     0s - loss: 4380429.5115 - mean_squared_error: 4380429.5115 - val_loss: 4299127.2085 - val_mean_squared_error: 4299127.2085
>     Epoch 23/250
>     0s - loss: 4051324.9618 - mean_squared_error: 4051324.9618 - val_loss: 3974133.5971 - val_mean_squared_error: 3974133.5971
>     Epoch 24/250
>     0s - loss: 3795899.2021 - mean_squared_error: 3795899.2021 - val_loss: 3728008.2341 - val_mean_squared_error: 3728008.2341
>     Epoch 25/250
>     0s - loss: 3599222.7706 - mean_squared_error: 3599222.7706 - val_loss: 3527661.3891 - val_mean_squared_error: 3527661.3891
>     Epoch 26/250
>     0s - loss: 3442831.6155 - mean_squared_error: 3442831.6155 - val_loss: 3372141.6767 - val_mean_squared_error: 3372141.6767
>     Epoch 27/250
>     0s - loss: 3320119.1868 - mean_squared_error: 3320119.1868 - val_loss: 3243861.6679 - val_mean_squared_error: 3243861.6679
>     Epoch 28/250
>     0s - loss: 3219037.9462 - mean_squared_error: 3219037.9462 - val_loss: 3137886.6163 - val_mean_squared_error: 3137886.6163
>     Epoch 29/250
>     0s - loss: 3133816.8481 - mean_squared_error: 3133816.8481 - val_loss: 3054137.6498 - val_mean_squared_error: 3054137.6498
>     Epoch 30/250
>     0s - loss: 3061552.4367 - mean_squared_error: 3061552.4367 - val_loss: 2973105.0660 - val_mean_squared_error: 2973105.0660
>     Epoch 31/250
>     0s - loss: 2995945.7693 - mean_squared_error: 2995945.7693 - val_loss: 2902083.7578 - val_mean_squared_error: 2902083.7578
>     Epoch 32/250
>     0s - loss: 2937403.4223 - mean_squared_error: 2937403.4223 - val_loss: 2840573.7862 - val_mean_squared_error: 2840573.7862
>     Epoch 33/250
>     0s - loss: 2884063.4812 - mean_squared_error: 2884063.4812 - val_loss: 2785633.1196 - val_mean_squared_error: 2785633.1196
>     Epoch 34/250
>     0s - loss: 2834801.4430 - mean_squared_error: 2834801.4430 - val_loss: 2732642.2763 - val_mean_squared_error: 2732642.2763
>     Epoch 35/250
>     0s - loss: 2789694.4358 - mean_squared_error: 2789694.4358 - val_loss: 2697298.3859 - val_mean_squared_error: 2697298.3859
>     Epoch 36/250
>     0s - loss: 2747330.2878 - mean_squared_error: 2747330.2878 - val_loss: 2641666.0644 - val_mean_squared_error: 2641666.0644
>     Epoch 37/250
>     0s - loss: 2708606.0763 - mean_squared_error: 2708606.0763 - val_loss: 2604204.7766 - val_mean_squared_error: 2604204.7766
>     Epoch 38/250
>     0s - loss: 2673186.3503 - mean_squared_error: 2673186.3503 - val_loss: 2563141.6086 - val_mean_squared_error: 2563141.6086
>     Epoch 39/250
>     0s - loss: 2639097.8889 - mean_squared_error: 2639097.8889 - val_loss: 2530050.4402 - val_mean_squared_error: 2530050.4402
>     Epoch 40/250
>     0s - loss: 2609005.5433 - mean_squared_error: 2609005.5433 - val_loss: 2496483.8206 - val_mean_squared_error: 2496483.8206
>     Epoch 41/250
>     0s - loss: 2577944.2107 - mean_squared_error: 2577944.2107 - val_loss: 2468498.9548 - val_mean_squared_error: 2468498.9548
>     Epoch 42/250
>     0s - loss: 2551941.3955 - mean_squared_error: 2551941.3955 - val_loss: 2445204.1102 - val_mean_squared_error: 2445204.1102
>     Epoch 43/250
>     0s - loss: 2526269.1503 - mean_squared_error: 2526269.1503 - val_loss: 2416916.2443 - val_mean_squared_error: 2416916.2443
>     Epoch 44/250
>     0s - loss: 2503002.9816 - mean_squared_error: 2503002.9816 - val_loss: 2387209.4409 - val_mean_squared_error: 2387209.4409
>     Epoch 45/250
>     0s - loss: 2481669.7163 - mean_squared_error: 2481669.7163 - val_loss: 2365208.7405 - val_mean_squared_error: 2365208.7405
>     Epoch 46/250
>     0s - loss: 2461549.7710 - mean_squared_error: 2461549.7710 - val_loss: 2360469.7872 - val_mean_squared_error: 2360469.7872
>     Epoch 47/250
>     0s - loss: 2444457.2286 - mean_squared_error: 2444457.2286 - val_loss: 2328461.1115 - val_mean_squared_error: 2328461.1115
>     Epoch 48/250
>     0s - loss: 2426585.5808 - mean_squared_error: 2426585.5808 - val_loss: 2307659.2631 - val_mean_squared_error: 2307659.2631
>     Epoch 49/250
>     0s - loss: 2410939.9824 - mean_squared_error: 2410939.9824 - val_loss: 2293184.7122 - val_mean_squared_error: 2293184.7122
>     Epoch 50/250
>     0s - loss: 2396061.8240 - mean_squared_error: 2396061.8240 - val_loss: 2276056.6431 - val_mean_squared_error: 2276056.6431
>     Epoch 51/250
>     0s - loss: 2382396.7558 - mean_squared_error: 2382396.7558 - val_loss: 2262589.4527 - val_mean_squared_error: 2262589.4527
>     Epoch 52/250
>     0s - loss: 2370050.5907 - mean_squared_error: 2370050.5907 - val_loss: 2251566.6600 - val_mean_squared_error: 2251566.6600
>     Epoch 53/250
>     0s - loss: 2357992.4237 - mean_squared_error: 2357992.4237 - val_loss: 2237004.8927 - val_mean_squared_error: 2237004.8927
>     Epoch 54/250
>     0s - loss: 2346332.9767 - mean_squared_error: 2346332.9767 - val_loss: 2227334.5070 - val_mean_squared_error: 2227334.5070
>     Epoch 55/250
>     0s - loss: 2336237.3644 - mean_squared_error: 2336237.3644 - val_loss: 2215740.7243 - val_mean_squared_error: 2215740.7243
>     Epoch 56/250
>     0s - loss: 2325887.5632 - mean_squared_error: 2325887.5632 - val_loss: 2211963.1436 - val_mean_squared_error: 2211963.1436
>     Epoch 57/250
>     0s - loss: 2316279.5955 - mean_squared_error: 2316279.5955 - val_loss: 2195186.8332 - val_mean_squared_error: 2195186.8332
>     Epoch 58/250
>     0s - loss: 2308265.3096 - mean_squared_error: 2308265.3096 - val_loss: 2187544.9800 - val_mean_squared_error: 2187544.9800
>     Epoch 59/250
>     0s - loss: 2300135.1247 - mean_squared_error: 2300135.1247 - val_loss: 2186924.2155 - val_mean_squared_error: 2186924.2155
>     Epoch 60/250
>     0s - loss: 2292097.2923 - mean_squared_error: 2292097.2923 - val_loss: 2170321.4857 - val_mean_squared_error: 2170321.4857
>     Epoch 61/250
>     0s - loss: 2283638.5119 - mean_squared_error: 2283638.5119 - val_loss: 2162297.5582 - val_mean_squared_error: 2162297.5582
>     Epoch 62/250
>     0s - loss: 2275449.3533 - mean_squared_error: 2275449.3533 - val_loss: 2157108.7140 - val_mean_squared_error: 2157108.7140
>     Epoch 63/250
>     0s - loss: 2268524.3016 - mean_squared_error: 2268524.3016 - val_loss: 2150714.4436 - val_mean_squared_error: 2150714.4436
>     Epoch 64/250
>     0s - loss: 2261564.7490 - mean_squared_error: 2261564.7490 - val_loss: 2144484.5214 - val_mean_squared_error: 2144484.5214
>     Epoch 65/250
>     0s - loss: 2254543.1043 - mean_squared_error: 2254543.1043 - val_loss: 2141498.5713 - val_mean_squared_error: 2141498.5713
>     Epoch 66/250
>     0s - loss: 2248706.6245 - mean_squared_error: 2248706.6245 - val_loss: 2130693.1300 - val_mean_squared_error: 2130693.1300
>     Epoch 67/250
>     0s - loss: 2242354.7813 - mean_squared_error: 2242354.7813 - val_loss: 2127449.8672 - val_mean_squared_error: 2127449.8672
>     Epoch 68/250
>     0s - loss: 2237156.0903 - mean_squared_error: 2237156.0903 - val_loss: 2123885.7000 - val_mean_squared_error: 2123885.7000
>     Epoch 69/250
>     0s - loss: 2230431.1652 - mean_squared_error: 2230431.1652 - val_loss: 2116530.6997 - val_mean_squared_error: 2116530.6997
>     Epoch 70/250
>     0s - loss: 2224075.3194 - mean_squared_error: 2224075.3194 - val_loss: 2106804.6977 - val_mean_squared_error: 2106804.6977
>     Epoch 71/250
>     0s - loss: 2217860.5158 - mean_squared_error: 2217860.5158 - val_loss: 2100604.3791 - val_mean_squared_error: 2100604.3791
>     Epoch 72/250
>     0s - loss: 2212629.8358 - mean_squared_error: 2212629.8358 - val_loss: 2097511.3800 - val_mean_squared_error: 2097511.3800
>     Epoch 73/250
>     0s - loss: 2206417.6134 - mean_squared_error: 2206417.6134 - val_loss: 2091820.7825 - val_mean_squared_error: 2091820.7825
>     Epoch 74/250
>     0s - loss: 2200163.0572 - mean_squared_error: 2200163.0572 - val_loss: 2086788.1871 - val_mean_squared_error: 2086788.1871
>     Epoch 75/250
>     0s - loss: 2196599.8797 - mean_squared_error: 2196599.8797 - val_loss: 2093239.0468 - val_mean_squared_error: 2093239.0468
>     Epoch 76/250
>     0s - loss: 2189597.5485 - mean_squared_error: 2189597.5485 - val_loss: 2074867.2587 - val_mean_squared_error: 2074867.2587
>     Epoch 77/250
>     0s - loss: 2183969.8034 - mean_squared_error: 2183969.8034 - val_loss: 2072973.4616 - val_mean_squared_error: 2072973.4616
>     Epoch 78/250
>     0s - loss: 2178248.1172 - mean_squared_error: 2178248.1172 - val_loss: 2081187.3130 - val_mean_squared_error: 2081187.3130
>     Epoch 79/250
>     0s - loss: 2172964.7913 - mean_squared_error: 2172964.7913 - val_loss: 2066078.6356 - val_mean_squared_error: 2066078.6356
>     Epoch 80/250
>     0s - loss: 2169508.3369 - mean_squared_error: 2169508.3369 - val_loss: 2058138.5697 - val_mean_squared_error: 2058138.5697
>     Epoch 81/250
>     0s - loss: 2162642.2804 - mean_squared_error: 2162642.2804 - val_loss: 2051278.4873 - val_mean_squared_error: 2051278.4873
>     Epoch 82/250
>     0s - loss: 2157841.4636 - mean_squared_error: 2157841.4636 - val_loss: 2047034.7805 - val_mean_squared_error: 2047034.7805
>     Epoch 83/250
>     0s - loss: 2152525.6510 - mean_squared_error: 2152525.6510 - val_loss: 2042846.7672 - val_mean_squared_error: 2042846.7672
>     Epoch 84/250
>     0s - loss: 2148140.5886 - mean_squared_error: 2148140.5886 - val_loss: 2039080.7717 - val_mean_squared_error: 2039080.7717
>     Epoch 85/250
>     0s - loss: 2142804.5849 - mean_squared_error: 2142804.5849 - val_loss: 2034215.0007 - val_mean_squared_error: 2034215.0007
>     Epoch 86/250
>     0s - loss: 2137700.0286 - mean_squared_error: 2137700.0286 - val_loss: 2027682.9477 - val_mean_squared_error: 2027682.9477
>     Epoch 87/250
>     0s - loss: 2132687.5536 - mean_squared_error: 2132687.5536 - val_loss: 2023229.1527 - val_mean_squared_error: 2023229.1527
>     Epoch 88/250
>     0s - loss: 2127528.1239 - mean_squared_error: 2127528.1239 - val_loss: 2020156.0525 - val_mean_squared_error: 2020156.0525
>     Epoch 89/250
>     0s - loss: 2122246.7011 - mean_squared_error: 2122246.7011 - val_loss: 2014952.9193 - val_mean_squared_error: 2014952.9193
>     Epoch 90/250
>     0s - loss: 2117147.4203 - mean_squared_error: 2117147.4203 - val_loss: 2010286.4380 - val_mean_squared_error: 2010286.4380
>     Epoch 91/250
>     0s - loss: 2113171.9062 - mean_squared_error: 2113171.9062 - val_loss: 2017391.9330 - val_mean_squared_error: 2017391.9330
>     Epoch 92/250
>     0s - loss: 2108691.2232 - mean_squared_error: 2108691.2232 - val_loss: 2002256.7729 - val_mean_squared_error: 2002256.7729
>     Epoch 93/250
>     0s - loss: 2102821.3561 - mean_squared_error: 2102821.3561 - val_loss: 1997741.1898 - val_mean_squared_error: 1997741.1898
>     Epoch 94/250
>     0s - loss: 2098292.2893 - mean_squared_error: 2098292.2893 - val_loss: 1993643.2166 - val_mean_squared_error: 1993643.2166
>     Epoch 95/250
>     0s - loss: 2093748.8870 - mean_squared_error: 2093748.8870 - val_loss: 1990495.1320 - val_mean_squared_error: 1990495.1320
>     Epoch 96/250
>     0s - loss: 2088413.1235 - mean_squared_error: 2088413.1235 - val_loss: 1987745.0449 - val_mean_squared_error: 1987745.0449
>     Epoch 97/250
>     0s - loss: 2084661.7695 - mean_squared_error: 2084661.7695 - val_loss: 1982508.3959 - val_mean_squared_error: 1982508.3959
>     Epoch 98/250
>     0s - loss: 2079580.8995 - mean_squared_error: 2079580.8995 - val_loss: 1978024.0901 - val_mean_squared_error: 1978024.0901
>     Epoch 99/250
>     0s - loss: 2074897.3167 - mean_squared_error: 2074897.3167 - val_loss: 1984535.2159 - val_mean_squared_error: 1984535.2159
>     Epoch 100/250
>     0s - loss: 2070885.1541 - mean_squared_error: 2070885.1541 - val_loss: 1984779.1615 - val_mean_squared_error: 1984779.1615
>     Epoch 101/250
>     0s - loss: 2064975.9661 - mean_squared_error: 2064975.9661 - val_loss: 1964990.6893 - val_mean_squared_error: 1964990.6893
>     Epoch 102/250
>     0s - loss: 2061313.6743 - mean_squared_error: 2061313.6743 - val_loss: 1965428.2153 - val_mean_squared_error: 1965428.2153
>     Epoch 103/250
>     0s - loss: 2056843.4819 - mean_squared_error: 2056843.4819 - val_loss: 1957262.1677 - val_mean_squared_error: 1957262.1677
>     Epoch 104/250
>     0s - loss: 2051823.6640 - mean_squared_error: 2051823.6640 - val_loss: 1953116.2872 - val_mean_squared_error: 1953116.2872
>     Epoch 105/250
>     0s - loss: 2048080.1553 - mean_squared_error: 2048080.1553 - val_loss: 1950096.2130 - val_mean_squared_error: 1950096.2130
>     Epoch 106/250
>     0s - loss: 2042777.8602 - mean_squared_error: 2042777.8602 - val_loss: 1945416.0937 - val_mean_squared_error: 1945416.0937
>     Epoch 107/250
>     0s - loss: 2037973.2868 - mean_squared_error: 2037973.2868 - val_loss: 1941757.0948 - val_mean_squared_error: 1941757.0948
>     Epoch 108/250
>     0s - loss: 2033673.8567 - mean_squared_error: 2033673.8567 - val_loss: 1960756.8783 - val_mean_squared_error: 1960756.8783
>     Epoch 109/250
>     0s - loss: 2031221.0805 - mean_squared_error: 2031221.0805 - val_loss: 1937409.4617 - val_mean_squared_error: 1937409.4617
>     Epoch 110/250
>     0s - loss: 2025475.2850 - mean_squared_error: 2025475.2850 - val_loss: 1931608.1807 - val_mean_squared_error: 1931608.1807
>     Epoch 111/250
>     0s - loss: 2020990.3754 - mean_squared_error: 2020990.3754 - val_loss: 1926483.4402 - val_mean_squared_error: 1926483.4402
>     Epoch 112/250
>     0s - loss: 2017406.1112 - mean_squared_error: 2017406.1112 - val_loss: 1939580.3910 - val_mean_squared_error: 1939580.3910
>     Epoch 113/250
>     0s - loss: 2012408.5957 - mean_squared_error: 2012408.5957 - val_loss: 1921253.1673 - val_mean_squared_error: 1921253.1673
>     Epoch 114/250
>     0s - loss: 2008494.4790 - mean_squared_error: 2008494.4790 - val_loss: 1920345.2625 - val_mean_squared_error: 1920345.2625
>     Epoch 115/250
>     0s - loss: 2002928.1497 - mean_squared_error: 2002928.1497 - val_loss: 1919071.2380 - val_mean_squared_error: 1919071.2380
>     Epoch 116/250
>     0s - loss: 1999128.0301 - mean_squared_error: 1999128.0301 - val_loss: 1907501.6646 - val_mean_squared_error: 1907501.6646
>     Epoch 117/250
>     0s - loss: 1994491.1551 - mean_squared_error: 1994491.1551 - val_loss: 1902740.4435 - val_mean_squared_error: 1902740.4435
>     Epoch 118/250
>     0s - loss: 1990096.4401 - mean_squared_error: 1990096.4401 - val_loss: 1901343.9680 - val_mean_squared_error: 1901343.9680
>     Epoch 119/250
>     0s - loss: 1985582.0499 - mean_squared_error: 1985582.0499 - val_loss: 1894691.0402 - val_mean_squared_error: 1894691.0402
>     Epoch 120/250
>     0s - loss: 1982997.3631 - mean_squared_error: 1982997.3631 - val_loss: 1891173.9166 - val_mean_squared_error: 1891173.9166
>     Epoch 121/250
>     0s - loss: 1977409.3717 - mean_squared_error: 1977409.3717 - val_loss: 1887602.4860 - val_mean_squared_error: 1887602.4860
>     Epoch 122/250
>     0s - loss: 1973473.7715 - mean_squared_error: 1973473.7715 - val_loss: 1886082.5963 - val_mean_squared_error: 1886082.5963
>     Epoch 123/250
>     0s - loss: 1969322.9418 - mean_squared_error: 1969322.9418 - val_loss: 1881427.9912 - val_mean_squared_error: 1881427.9912
>     Epoch 124/250
>     0s - loss: 1964298.6379 - mean_squared_error: 1964298.6379 - val_loss: 1885338.7200 - val_mean_squared_error: 1885338.7200
>     Epoch 125/250
>     0s - loss: 1961138.1579 - mean_squared_error: 1961138.1579 - val_loss: 1881406.2233 - val_mean_squared_error: 1881406.2233
>     Epoch 126/250
>     0s - loss: 1956513.2392 - mean_squared_error: 1956513.2392 - val_loss: 1878919.4712 - val_mean_squared_error: 1878919.4712
>     Epoch 127/250
>     0s - loss: 1951644.1282 - mean_squared_error: 1951644.1282 - val_loss: 1868230.6144 - val_mean_squared_error: 1868230.6144
>     Epoch 128/250
>     0s - loss: 1947657.1681 - mean_squared_error: 1947657.1681 - val_loss: 1874104.9229 - val_mean_squared_error: 1874104.9229
>     Epoch 129/250
>     0s - loss: 1942816.4007 - mean_squared_error: 1942816.4007 - val_loss: 1859027.8497 - val_mean_squared_error: 1859027.8497
>     Epoch 130/250
>     0s - loss: 1939000.6680 - mean_squared_error: 1939000.6680 - val_loss: 1855019.9623 - val_mean_squared_error: 1855019.9623
>     Epoch 131/250
>     0s - loss: 1934481.7299 - mean_squared_error: 1934481.7299 - val_loss: 1850407.5507 - val_mean_squared_error: 1850407.5507
>     Epoch 132/250
>     0s - loss: 1930105.0439 - mean_squared_error: 1930105.0439 - val_loss: 1846656.7997 - val_mean_squared_error: 1846656.7997
>     Epoch 133/250
>     0s - loss: 1925464.7584 - mean_squared_error: 1925464.7584 - val_loss: 1843000.1738 - val_mean_squared_error: 1843000.1738
>     Epoch 134/250
>     0s - loss: 1921288.2179 - mean_squared_error: 1921288.2179 - val_loss: 1845584.3469 - val_mean_squared_error: 1845584.3469
>     Epoch 135/250
>     0s - loss: 1917176.3969 - mean_squared_error: 1917176.3969 - val_loss: 1843142.9595 - val_mean_squared_error: 1843142.9595
>     Epoch 136/250
>     0s - loss: 1912275.3311 - mean_squared_error: 1912275.3311 - val_loss: 1831316.6286 - val_mean_squared_error: 1831316.6286
>     Epoch 137/250
>     0s - loss: 1908472.1077 - mean_squared_error: 1908472.1077 - val_loss: 1828117.1033 - val_mean_squared_error: 1828117.1033
>     Epoch 138/250
>     0s - loss: 1904397.1385 - mean_squared_error: 1904397.1385 - val_loss: 1826863.4805 - val_mean_squared_error: 1826863.4805
>     Epoch 139/250
>     0s - loss: 1900317.1270 - mean_squared_error: 1900317.1270 - val_loss: 1820256.7097 - val_mean_squared_error: 1820256.7097
>     Epoch 140/250
>     0s - loss: 1895597.7805 - mean_squared_error: 1895597.7805 - val_loss: 1820516.1416 - val_mean_squared_error: 1820516.1416
>     Epoch 141/250
>     0s - loss: 1892106.7094 - mean_squared_error: 1892106.7094 - val_loss: 1815663.2201 - val_mean_squared_error: 1815663.2201
>     Epoch 142/250
>     0s - loss: 1887743.1140 - mean_squared_error: 1887743.1140 - val_loss: 1808838.1086 - val_mean_squared_error: 1808838.1086
>     Epoch 143/250
>     0s - loss: 1884434.4016 - mean_squared_error: 1884434.4016 - val_loss: 1805607.5365 - val_mean_squared_error: 1805607.5365
>     Epoch 144/250
>     0s - loss: 1880486.7248 - mean_squared_error: 1880486.7248 - val_loss: 1803834.6820 - val_mean_squared_error: 1803834.6820
>     Epoch 145/250
>     0s - loss: 1874587.4361 - mean_squared_error: 1874587.4361 - val_loss: 1800401.5900 - val_mean_squared_error: 1800401.5900
>     Epoch 146/250
>     0s - loss: 1871227.6304 - mean_squared_error: 1871227.6304 - val_loss: 1795104.4351 - val_mean_squared_error: 1795104.4351
>     Epoch 147/250
>     0s - loss: 1866929.3160 - mean_squared_error: 1866929.3160 - val_loss: 1794177.5061 - val_mean_squared_error: 1794177.5061
>     Epoch 148/250
>     0s - loss: 1862506.5834 - mean_squared_error: 1862506.5834 - val_loss: 1791402.7347 - val_mean_squared_error: 1791402.7347
>     Epoch 149/250
>     0s - loss: 1857797.5623 - mean_squared_error: 1857797.5623 - val_loss: 1785353.8964 - val_mean_squared_error: 1785353.8964
>     Epoch 150/250
>     0s - loss: 1853885.1878 - mean_squared_error: 1853885.1878 - val_loss: 1783920.2558 - val_mean_squared_error: 1783920.2558
>     Epoch 151/250
>     0s - loss: 1850040.0779 - mean_squared_error: 1850040.0779 - val_loss: 1790122.6186 - val_mean_squared_error: 1790122.6186
>     Epoch 152/250
>     0s - loss: 1846056.3143 - mean_squared_error: 1846056.3143 - val_loss: 1772536.7316 - val_mean_squared_error: 1772536.7316
>     Epoch 153/250
>     0s - loss: 1841607.3581 - mean_squared_error: 1841607.3581 - val_loss: 1772231.7202 - val_mean_squared_error: 1772231.7202
>     Epoch 154/250
>     0s - loss: 1838530.1937 - mean_squared_error: 1838530.1937 - val_loss: 1771063.8182 - val_mean_squared_error: 1771063.8182
>     Epoch 155/250
>     0s - loss: 1833882.1198 - mean_squared_error: 1833882.1198 - val_loss: 1761936.4340 - val_mean_squared_error: 1761936.4340
>     Epoch 156/250
>     0s - loss: 1831170.5717 - mean_squared_error: 1831170.5717 - val_loss: 1758615.8657 - val_mean_squared_error: 1758615.8657
>     Epoch 157/250
>     0s - loss: 1825270.1230 - mean_squared_error: 1825270.1230 - val_loss: 1754440.0421 - val_mean_squared_error: 1754440.0421
>     Epoch 158/250
>     0s - loss: 1821236.5270 - mean_squared_error: 1821236.5270 - val_loss: 1758477.4055 - val_mean_squared_error: 1758477.4055
>     Epoch 159/250
>     0s - loss: 1816967.7036 - mean_squared_error: 1816967.7036 - val_loss: 1749483.8612 - val_mean_squared_error: 1749483.8612
>     Epoch 160/250
>     0s - loss: 1814402.1376 - mean_squared_error: 1814402.1376 - val_loss: 1743606.2059 - val_mean_squared_error: 1743606.2059
>     Epoch 161/250
>     0s - loss: 1809474.4524 - mean_squared_error: 1809474.4524 - val_loss: 1741667.9150 - val_mean_squared_error: 1741667.9150
>     Epoch 162/250
>     0s - loss: 1805019.3385 - mean_squared_error: 1805019.3385 - val_loss: 1737294.9051 - val_mean_squared_error: 1737294.9051
>     Epoch 163/250
>     0s - loss: 1801671.8877 - mean_squared_error: 1801671.8877 - val_loss: 1743671.8553 - val_mean_squared_error: 1743671.8553
>     Epoch 164/250
>     0s - loss: 1797252.5162 - mean_squared_error: 1797252.5162 - val_loss: 1733976.4208 - val_mean_squared_error: 1733976.4208
>     Epoch 165/250
>     0s - loss: 1792333.8760 - mean_squared_error: 1792333.8760 - val_loss: 1725507.5209 - val_mean_squared_error: 1725507.5209
>     Epoch 166/250
>     0s - loss: 1789079.7542 - mean_squared_error: 1789079.7542 - val_loss: 1728887.8623 - val_mean_squared_error: 1728887.8623
>     Epoch 167/250
>     0s - loss: 1783785.4971 - mean_squared_error: 1783785.4971 - val_loss: 1721401.8059 - val_mean_squared_error: 1721401.8059
>     Epoch 168/250
>     0s - loss: 1780886.0457 - mean_squared_error: 1780886.0457 - val_loss: 1714989.4184 - val_mean_squared_error: 1714989.4184
>     Epoch 169/250
>     0s - loss: 1776550.5320 - mean_squared_error: 1776550.5320 - val_loss: 1711208.6094 - val_mean_squared_error: 1711208.6094
>     Epoch 170/250
>     0s - loss: 1771944.7308 - mean_squared_error: 1771944.7308 - val_loss: 1708818.0450 - val_mean_squared_error: 1708818.0450
>     Epoch 171/250
>     0s - loss: 1769168.2980 - mean_squared_error: 1769168.2980 - val_loss: 1704289.0087 - val_mean_squared_error: 1704289.0087
>     Epoch 172/250
>     0s - loss: 1764621.4207 - mean_squared_error: 1764621.4207 - val_loss: 1708725.4752 - val_mean_squared_error: 1708725.4752
>     Epoch 173/250
>     0s - loss: 1760440.8496 - mean_squared_error: 1760440.8496 - val_loss: 1696935.0940 - val_mean_squared_error: 1696935.0940
>     Epoch 174/250
>     0s - loss: 1756636.3376 - mean_squared_error: 1756636.3376 - val_loss: 1694643.4417 - val_mean_squared_error: 1694643.4417
>     Epoch 175/250
>     0s - loss: 1752069.5443 - mean_squared_error: 1752069.5443 - val_loss: 1690606.1489 - val_mean_squared_error: 1690606.1489
>     Epoch 176/250
>     0s - loss: 1748188.8602 - mean_squared_error: 1748188.8602 - val_loss: 1686171.0136 - val_mean_squared_error: 1686171.0136
>     Epoch 177/250
>     0s - loss: 1744580.1000 - mean_squared_error: 1744580.1000 - val_loss: 1686558.9331 - val_mean_squared_error: 1686558.9331
>     Epoch 178/250
>     0s - loss: 1740583.4021 - mean_squared_error: 1740583.4021 - val_loss: 1685593.8387 - val_mean_squared_error: 1685593.8387
>     Epoch 179/250
>     0s - loss: 1737085.9561 - mean_squared_error: 1737085.9561 - val_loss: 1677844.4322 - val_mean_squared_error: 1677844.4322
>     Epoch 180/250
>     0s - loss: 1732098.0847 - mean_squared_error: 1732098.0847 - val_loss: 1671988.9257 - val_mean_squared_error: 1671988.9257
>     Epoch 181/250
>     0s - loss: 1727689.5077 - mean_squared_error: 1727689.5077 - val_loss: 1668533.5755 - val_mean_squared_error: 1668533.5755
>     Epoch 182/250
>     0s - loss: 1724162.6299 - mean_squared_error: 1724162.6299 - val_loss: 1664830.9232 - val_mean_squared_error: 1664830.9232
>     Epoch 183/250
>     0s - loss: 1720617.8767 - mean_squared_error: 1720617.8767 - val_loss: 1662218.1707 - val_mean_squared_error: 1662218.1707
>     Epoch 184/250
>     0s - loss: 1716185.2657 - mean_squared_error: 1716185.2657 - val_loss: 1666908.7395 - val_mean_squared_error: 1666908.7395
>     Epoch 185/250
>     0s - loss: 1713034.7947 - mean_squared_error: 1713034.7947 - val_loss: 1655541.3076 - val_mean_squared_error: 1655541.3076
>     Epoch 186/250
>     0s - loss: 1708219.9019 - mean_squared_error: 1708219.9019 - val_loss: 1656198.0788 - val_mean_squared_error: 1656198.0788
>     Epoch 187/250
>     0s - loss: 1705381.3546 - mean_squared_error: 1705381.3546 - val_loss: 1651749.0208 - val_mean_squared_error: 1651749.0208
>     Epoch 188/250
>     0s - loss: 1700479.5275 - mean_squared_error: 1700479.5275 - val_loss: 1646332.5651 - val_mean_squared_error: 1646332.5651
>     Epoch 189/250
>     0s - loss: 1696043.7911 - mean_squared_error: 1696043.7911 - val_loss: 1640811.1282 - val_mean_squared_error: 1640811.1282
>     Epoch 190/250
>     0s - loss: 1692733.1851 - mean_squared_error: 1692733.1851 - val_loss: 1651686.4812 - val_mean_squared_error: 1651686.4812
>     Epoch 191/250
>     0s - loss: 1688549.4744 - mean_squared_error: 1688549.4744 - val_loss: 1636896.4333 - val_mean_squared_error: 1636896.4333
>     Epoch 192/250
>     0s - loss: 1684694.4559 - mean_squared_error: 1684694.4559 - val_loss: 1630168.6379 - val_mean_squared_error: 1630168.6379
>     Epoch 193/250
>     0s - loss: 1680762.7612 - mean_squared_error: 1680762.7612 - val_loss: 1626077.5331 - val_mean_squared_error: 1626077.5331
>     Epoch 194/250
>     0s - loss: 1676345.5460 - mean_squared_error: 1676345.5460 - val_loss: 1624581.3160 - val_mean_squared_error: 1624581.3160
>     Epoch 195/250
>     0s - loss: 1673510.1012 - mean_squared_error: 1673510.1012 - val_loss: 1619229.4794 - val_mean_squared_error: 1619229.4794
>     Epoch 196/250
>     0s - loss: 1668486.0620 - mean_squared_error: 1668486.0620 - val_loss: 1615355.0266 - val_mean_squared_error: 1615355.0266
>     Epoch 197/250
>     0s - loss: 1665494.9200 - mean_squared_error: 1665494.9200 - val_loss: 1612614.2963 - val_mean_squared_error: 1612614.2963
>     Epoch 198/250
>     0s - loss: 1661789.6739 - mean_squared_error: 1661789.6739 - val_loss: 1608492.1893 - val_mean_squared_error: 1608492.1893
>     Epoch 199/250
>     0s - loss: 1657211.4087 - mean_squared_error: 1657211.4087 - val_loss: 1604729.2507 - val_mean_squared_error: 1604729.2507
>     Epoch 200/250
>     0s - loss: 1652479.5938 - mean_squared_error: 1652479.5938 - val_loss: 1603913.2284 - val_mean_squared_error: 1603913.2284
>     Epoch 201/250
>     0s - loss: 1649808.1983 - mean_squared_error: 1649808.1983 - val_loss: 1597958.0552 - val_mean_squared_error: 1597958.0552
>     Epoch 202/250
>     0s - loss: 1646353.9084 - mean_squared_error: 1646353.9084 - val_loss: 1596598.9802 - val_mean_squared_error: 1596598.9802
>     Epoch 203/250
>     0s - loss: 1642181.7167 - mean_squared_error: 1642181.7167 - val_loss: 1591997.8792 - val_mean_squared_error: 1591997.8792
>     Epoch 204/250
>     0s - loss: 1637892.9418 - mean_squared_error: 1637892.9418 - val_loss: 1588234.8609 - val_mean_squared_error: 1588234.8609
>     Epoch 205/250
>     0s - loss: 1634239.4922 - mean_squared_error: 1634239.4922 - val_loss: 1584955.0452 - val_mean_squared_error: 1584955.0452
>     Epoch 206/250
>     0s - loss: 1630911.4169 - mean_squared_error: 1630911.4169 - val_loss: 1582626.3168 - val_mean_squared_error: 1582626.3168
>     Epoch 207/250
>     0s - loss: 1626677.3784 - mean_squared_error: 1626677.3784 - val_loss: 1578560.1458 - val_mean_squared_error: 1578560.1458
>     Epoch 208/250
>     0s - loss: 1623208.4904 - mean_squared_error: 1623208.4904 - val_loss: 1574092.4197 - val_mean_squared_error: 1574092.4197
>     Epoch 209/250
>     0s - loss: 1619272.8898 - mean_squared_error: 1619272.8898 - val_loss: 1578833.3190 - val_mean_squared_error: 1578833.3190
>     Epoch 210/250
>     0s - loss: 1616168.8813 - mean_squared_error: 1616168.8813 - val_loss: 1569387.3984 - val_mean_squared_error: 1569387.3984
>     Epoch 211/250
>     0s - loss: 1611747.2461 - mean_squared_error: 1611747.2461 - val_loss: 1565671.8923 - val_mean_squared_error: 1565671.8923
>     Epoch 212/250
>     0s - loss: 1608751.1833 - mean_squared_error: 1608751.1833 - val_loss: 1565295.7443 - val_mean_squared_error: 1565295.7443
>     Epoch 213/250
>     0s - loss: 1604035.7443 - mean_squared_error: 1604035.7443 - val_loss: 1559937.6121 - val_mean_squared_error: 1559937.6121
>     Epoch 214/250
>     0s - loss: 1600519.3572 - mean_squared_error: 1600519.3572 - val_loss: 1559307.1013 - val_mean_squared_error: 1559307.1013
>     Epoch 215/250
>     0s - loss: 1597665.7867 - mean_squared_error: 1597665.7867 - val_loss: 1551859.3835 - val_mean_squared_error: 1551859.3835
>     Epoch 216/250
>     0s - loss: 1593174.5948 - mean_squared_error: 1593174.5948 - val_loss: 1548270.1969 - val_mean_squared_error: 1548270.1969
>     Epoch 217/250
>     0s - loss: 1589483.6531 - mean_squared_error: 1589483.6531 - val_loss: 1543574.3039 - val_mean_squared_error: 1543574.3039
>     Epoch 218/250
>     0s - loss: 1586636.7558 - mean_squared_error: 1586636.7558 - val_loss: 1545432.8696 - val_mean_squared_error: 1545432.8696
>     Epoch 219/250
>     0s - loss: 1582200.0938 - mean_squared_error: 1582200.0938 - val_loss: 1537190.0554 - val_mean_squared_error: 1537190.0554
>     Epoch 220/250
>     0s - loss: 1578690.1081 - mean_squared_error: 1578690.1081 - val_loss: 1537982.0046 - val_mean_squared_error: 1537982.0046
>     Epoch 221/250
>     0s - loss: 1575309.8210 - mean_squared_error: 1575309.8210 - val_loss: 1533265.8010 - val_mean_squared_error: 1533265.8010
>     Epoch 222/250
>     0s - loss: 1571774.5191 - mean_squared_error: 1571774.5191 - val_loss: 1536715.4637 - val_mean_squared_error: 1536715.4637
>     Epoch 223/250
>     0s - loss: 1568046.3039 - mean_squared_error: 1568046.3039 - val_loss: 1524522.4489 - val_mean_squared_error: 1524522.4489
>     Epoch 224/250
>     0s - loss: 1564703.6763 - mean_squared_error: 1564703.6763 - val_loss: 1521190.4621 - val_mean_squared_error: 1521190.4621
>     Epoch 225/250
>     0s - loss: 1561841.9487 - mean_squared_error: 1561841.9487 - val_loss: 1517679.4793 - val_mean_squared_error: 1517679.4793
>     Epoch 226/250
>     0s - loss: 1558480.0124 - mean_squared_error: 1558480.0124 - val_loss: 1514301.8236 - val_mean_squared_error: 1514301.8236
>     Epoch 227/250
>     0s - loss: 1553986.6836 - mean_squared_error: 1553986.6836 - val_loss: 1512907.6079 - val_mean_squared_error: 1512907.6079
>     Epoch 228/250
>     0s - loss: 1550660.2036 - mean_squared_error: 1550660.2036 - val_loss: 1528588.9533 - val_mean_squared_error: 1528588.9533
>     Epoch 229/250
>     0s - loss: 1548342.0002 - mean_squared_error: 1548342.0002 - val_loss: 1505260.6224 - val_mean_squared_error: 1505260.6224
>     Epoch 230/250
>     0s - loss: 1543829.4989 - mean_squared_error: 1543829.4989 - val_loss: 1502883.2622 - val_mean_squared_error: 1502883.2622
>     Epoch 231/250
>     0s - loss: 1540070.4715 - mean_squared_error: 1540070.4715 - val_loss: 1500825.6327 - val_mean_squared_error: 1500825.6327
>     Epoch 232/250
>     0s - loss: 1537318.5786 - mean_squared_error: 1537318.5786 - val_loss: 1495349.7173 - val_mean_squared_error: 1495349.7173
>     Epoch 233/250
>     0s - loss: 1533328.3533 - mean_squared_error: 1533328.3533 - val_loss: 1492718.4103 - val_mean_squared_error: 1492718.4103
>     Epoch 234/250
>     0s - loss: 1529751.1481 - mean_squared_error: 1529751.1481 - val_loss: 1489131.4611 - val_mean_squared_error: 1489131.4611
>     Epoch 235/250
>     0s - loss: 1526945.7121 - mean_squared_error: 1526945.7121 - val_loss: 1486006.3363 - val_mean_squared_error: 1486006.3363
>     Epoch 236/250
>     0s - loss: 1523339.3769 - mean_squared_error: 1523339.3769 - val_loss: 1483489.6031 - val_mean_squared_error: 1483489.6031
>     Epoch 237/250
>     0s - loss: 1521281.2180 - mean_squared_error: 1521281.2180 - val_loss: 1479847.9110 - val_mean_squared_error: 1479847.9110
>     Epoch 238/250
>     0s - loss: 1517429.9417 - mean_squared_error: 1517429.9417 - val_loss: 1476639.8004 - val_mean_squared_error: 1476639.8004
>     Epoch 239/250
>     0s - loss: 1513520.3738 - mean_squared_error: 1513520.3738 - val_loss: 1477471.9424 - val_mean_squared_error: 1477471.9424
>     Epoch 240/250
>     0s - loss: 1510527.1940 - mean_squared_error: 1510527.1940 - val_loss: 1476546.8397 - val_mean_squared_error: 1476546.8397
>     Epoch 241/250
>     0s - loss: 1507055.2658 - mean_squared_error: 1507055.2658 - val_loss: 1467572.0840 - val_mean_squared_error: 1467572.0840
>     Epoch 242/250
>     0s - loss: 1503902.2881 - mean_squared_error: 1503902.2881 - val_loss: 1464283.6240 - val_mean_squared_error: 1464283.6240
>     Epoch 243/250
>     0s - loss: 1500750.6449 - mean_squared_error: 1500750.6449 - val_loss: 1461292.5081 - val_mean_squared_error: 1461292.5081
>     Epoch 244/250
>     0s - loss: 1497841.4096 - mean_squared_error: 1497841.4096 - val_loss: 1460373.1425 - val_mean_squared_error: 1460373.1425
>     Epoch 245/250
>     0s - loss: 1495059.6262 - mean_squared_error: 1495059.6262 - val_loss: 1455306.6539 - val_mean_squared_error: 1455306.6539
>     Epoch 246/250
>     0s - loss: 1491896.1983 - mean_squared_error: 1491896.1983 - val_loss: 1454405.3699 - val_mean_squared_error: 1454405.3699
>     Epoch 247/250
>     0s - loss: 1489123.8983 - mean_squared_error: 1489123.8983 - val_loss: 1449503.1236 - val_mean_squared_error: 1449503.1236
>     Epoch 248/250
>     0s - loss: 1485773.0232 - mean_squared_error: 1485773.0232 - val_loss: 1449819.3211 - val_mean_squared_error: 1449819.3211
>     Epoch 249/250
>     0s - loss: 1481980.0218 - mean_squared_error: 1481980.0218 - val_loss: 1444984.9032 - val_mean_squared_error: 1444984.9032
>     Epoch 250/250
>     0s - loss: 1479168.2856 - mean_squared_error: 1479168.2856 - val_loss: 1441408.0688 - val_mean_squared_error: 1441408.0688
>        32/13485 [..............................] - ETA: 0s 2400/13485 [====>.........................] - ETA: 0s 4832/13485 [=========>....................] - ETA: 0s 7136/13485 [==============>...............] - ETA: 0s 9472/13485 [====================>.........] - ETA: 0s11872/13485 [=========================>....] - ETA: 0s
>     root mean_squared_error: 1204.707714

After all this hard work we are closer to the MSE we got from linear regression, but purely using a shallow feed-forward neural network.

### Training: Gradient Descent

A family of numeric optimization techniques, where we solve a problem with the following pattern:

1.  Describe the error in the model output: this is usually some difference between the the true values and the model's predicted values, as a function of the model parameters (weights)

2.  Compute the gradient, or directional derivative, of the error -- the "slope toward lower error"

3.  Adjust the parameters of the model variables in the indicated direction

4.  Repeat

<img src="https://i.imgur.com/HOYViqN.png" width=500>

#### Some ideas to help build your intuition

-   What happens if the variables (imagine just 2, to keep the mental picture simple) are on wildly different scales ... like one ranges from -1 to 1 while another from -1e6 to +1e6?

-   What if some of the variables are correlated? I.e., a change in one corresponds to, say, a linear change in another?

-   Other things being equal, an approximate solution with fewer variables is easier to work with than one with more -- how could we get rid of some less valuable parameters? (e.g., L1 penalty)

-   How do we know how far to "adjust" our parameters with each step?

<img src="http://i.imgur.com/AvM2TN6.png" width=600>

What if we have billions of data points? Does it makes sense to use all of them for each update? Is there a shortcut?

Yes: *Stochastic Gradient Descent*

But SGD has some shortcomings, so we typically use a "smarter" version of SGD, which has rules for adjusting the learning rate and even direction in order to avoid common problems.

What about that "Adam" optimizer? Adam is short for "adaptive moment" and is a variant of SGD that includes momentum calculations that change over time. For more detail on optimizers, see the chapter "Training Deep Neural Nets" in Aurélien Géron's book: *Hands-On Machine Learning with Scikit-Learn and TensorFlow* (http://shop.oreilly.com/product/0636920052289.do)

### Training: Backpropagation

With a simple, flat model, we could use SGD or a related algorithm to derive the weights, since the error depends directly on those weights.

With a deeper network, we have a couple of challenges:

-   The error is computed from the final layer, so the gradient of the error doesn't tell us immediately about problems in other-layer weights
-   Our tiny diamonds model has almost a thousand weights. Bigger models can easily have millions of weights. Each of those weights may need to move a little at a time, and we have to watch out for underflow or undersignificance situations.

**The insight is to iteratively calculate errors, one layer at a time, starting at the output. This is called backpropagation. It is neither magical nor surprising. The challenge is just doing it fast and not losing information.**

<img src="http://i.imgur.com/bjlYwjM.jpg" width=800>

Ok so we've come up with a very slow way to perform a linear regression.
------------------------------------------------------------------------

### *Welcome to Neural Networks in the 1960s!*

------------------------------------------------------------------------

### Watch closely now because this is where the magic happens...

<img src="https://media.giphy.com/media/Hw5LkPYy9yfVS/giphy.gif">

Non-Linearity + Perceptron = Universal Approximation
====================================================

### Where does the non-linearity fit in?

-   We start with the inputs to a perceptron -- these could be from source data, for example.
-   We multiply each input by its respective weight, which gets us the \\(x \cdot w\\)
-   Then add the "bias" -- an extra learnable parameter, to get \\({x \cdot w} + b\\)
    -   This value (so far) is sometimes called the "pre-activation"
-   Now, apply a non-linear "activation function" to this value, such as the logistic sigmoid

<img src="https://i.imgur.com/MhokAmo.gif">

### Now the network can "learn" non-linear functions

To gain some intuition, consider that where the sigmoid is close to 1, we can think of that neuron as being "on" or activated, giving a specific output. When close to zero, it is "off."

So each neuron is a bit like a switch. If we have enough of them, we can theoretically express arbitrarily many different signals.

In some ways this is like the original artificial neuron, with the thresholding output -- the main difference is that the sigmoid gives us a smooth (arbitrarily differentiable) output that we can optimize over using gradient descent to learn the weights.

### Where does the signal "go" from these neurons?

-   In a regression problem, like the diamonds dataset, the activations from the hidden layer can feed into a single output neuron, with a simple linear activation representing the final output of the calculation.

-   Frequently we want a classification output instead -- e.g., with MNIST digits, where we need to choose from 10 classes. In that case, we can feed the outputs from these hidden neurons forward into a final layer of 10 neurons, and compare those final neurons' activation levels.

Ok, before we talk any more theory, let's run it and see if we can do better on our diamonds dataset adding this "sigmoid activation."

While that's running, let's look at the code:

``` python
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
```

>     Train on 36409 samples, validate on 4046 samples
>     Epoch 1/2000
>     0s - loss: 31381587.1800 - mean_squared_error: 31381587.1800 - val_loss: 32360299.1686 - val_mean_squared_error: 32360299.1686
>     Epoch 2/2000
>     0s - loss: 31300797.1342 - mean_squared_error: 31300797.1342 - val_loss: 32278330.3559 - val_mean_squared_error: 32278330.3559
>     Epoch 3/2000
>     0s - loss: 31219897.6074 - mean_squared_error: 31219897.6074 - val_loss: 32195603.7835 - val_mean_squared_error: 32195603.7835
>     Epoch 4/2000
>     0s - loss: 31136601.1793 - mean_squared_error: 31136601.1793 - val_loss: 32112058.5655 - val_mean_squared_error: 32112058.5655
>     Epoch 5/2000
>     0s - loss: 31055551.8570 - mean_squared_error: 31055551.8570 - val_loss: 32030912.2659 - val_mean_squared_error: 32030912.2659
>     Epoch 6/2000
>     0s - loss: 30975793.1928 - mean_squared_error: 30975793.1928 - val_loss: 31950492.1829 - val_mean_squared_error: 31950492.1829
>     Epoch 7/2000
>     0s - loss: 30896651.0774 - mean_squared_error: 30896651.0774 - val_loss: 31870584.3431 - val_mean_squared_error: 31870584.3431
>     Epoch 8/2000
>     0s - loss: 30818016.4486 - mean_squared_error: 30818016.4486 - val_loss: 31791332.6891 - val_mean_squared_error: 31791332.6891
>     Epoch 9/2000
>     0s - loss: 30735550.9157 - mean_squared_error: 30735550.9157 - val_loss: 31703859.8052 - val_mean_squared_error: 31703859.8052
>     Epoch 10/2000
>     0s - loss: 30651279.0310 - mean_squared_error: 30651279.0310 - val_loss: 31620582.6752 - val_mean_squared_error: 31620582.6752
>     Epoch 11/2000
>     0s - loss: 30569789.3866 - mean_squared_error: 30569789.3866 - val_loss: 31538533.7993 - val_mean_squared_error: 31538533.7993
>     Epoch 12/2000
>     0s - loss: 30489061.7882 - mean_squared_error: 30489061.7882 - val_loss: 31457150.9669 - val_mean_squared_error: 31457150.9669
>     Epoch 13/2000
>     0s - loss: 30403898.0232 - mean_squared_error: 30403898.0232 - val_loss: 31367506.7790 - val_mean_squared_error: 31367506.7790
>     Epoch 14/2000
>     0s - loss: 30318363.3834 - mean_squared_error: 30318363.3834 - val_loss: 31282520.9797 - val_mean_squared_error: 31282520.9797
>     Epoch 15/2000
>     0s - loss: 30235050.1550 - mean_squared_error: 30235050.1550 - val_loss: 31198653.9842 - val_mean_squared_error: 31198653.9842
>     Epoch 16/2000
>     0s - loss: 30152820.7281 - mean_squared_error: 30152820.7281 - val_loss: 31115690.9441 - val_mean_squared_error: 31115690.9441
>     Epoch 17/2000
>     0s - loss: 30071031.9635 - mean_squared_error: 30071031.9635 - val_loss: 31033041.3643 - val_mean_squared_error: 31033041.3643
>     Epoch 18/2000
>     0s - loss: 29989484.6358 - mean_squared_error: 29989484.6358 - val_loss: 30950466.7504 - val_mean_squared_error: 30950466.7504
>     Epoch 19/2000
>     0s - loss: 29908416.3809 - mean_squared_error: 29908416.3809 - val_loss: 30868649.6589 - val_mean_squared_error: 30868649.6589
>     Epoch 20/2000
>     0s - loss: 29825203.2455 - mean_squared_error: 29825203.2455 - val_loss: 30780312.3421 - val_mean_squared_error: 30780312.3421
>     Epoch 21/2000
>     0s - loss: 29738005.1170 - mean_squared_error: 29738005.1170 - val_loss: 30693943.9328 - val_mean_squared_error: 30693943.9328
>     Epoch 22/2000
>     0s - loss: 29653543.7768 - mean_squared_error: 29653543.7768 - val_loss: 30609034.0455 - val_mean_squared_error: 30609034.0455
>     Epoch 23/2000
>     0s - loss: 29570091.3154 - mean_squared_error: 29570091.3154 - val_loss: 30524862.7187 - val_mean_squared_error: 30524862.7187
>     Epoch 24/2000
>     0s - loss: 29487199.4127 - mean_squared_error: 29487199.4127 - val_loss: 30441033.5877 - val_mean_squared_error: 30441033.5877
>     Epoch 25/2000
>     0s - loss: 29404784.1041 - mean_squared_error: 29404784.1041 - val_loss: 30357765.7568 - val_mean_squared_error: 30357765.7568
>     Epoch 26/2000
>     0s - loss: 29322763.9729 - mean_squared_error: 29322763.9729 - val_loss: 30274714.6980 - val_mean_squared_error: 30274714.6980
>     Epoch 27/2000
>     0s - loss: 29241128.8220 - mean_squared_error: 29241128.8220 - val_loss: 30192343.4503 - val_mean_squared_error: 30192343.4503
>     Epoch 28/2000
>     0s - loss: 29159743.0085 - mean_squared_error: 29159743.0085 - val_loss: 30109902.0395 - val_mean_squared_error: 30109902.0395
>     Epoch 29/2000
>     0s - loss: 29078487.6691 - mean_squared_error: 29078487.6691 - val_loss: 30027773.9822 - val_mean_squared_error: 30027773.9822
>     Epoch 30/2000
>     0s - loss: 28997685.6543 - mean_squared_error: 28997685.6543 - val_loss: 29945946.7652 - val_mean_squared_error: 29945946.7652
>     Epoch 31/2000
>     0s - loss: 28917075.0465 - mean_squared_error: 28917075.0465 - val_loss: 29864382.3678 - val_mean_squared_error: 29864382.3678
>     Epoch 32/2000
>     0s - loss: 28836701.5028 - mean_squared_error: 28836701.5028 - val_loss: 29783096.8087 - val_mean_squared_error: 29783096.8087
>     Epoch 33/2000
>     0s - loss: 28756670.3322 - mean_squared_error: 28756670.3322 - val_loss: 29702090.3312 - val_mean_squared_error: 29702090.3312
>     Epoch 34/2000
>     0s - loss: 28676774.0041 - mean_squared_error: 28676774.0041 - val_loss: 29621255.3327 - val_mean_squared_error: 29621255.3327
>     Epoch 35/2000
>     0s - loss: 28597337.6203 - mean_squared_error: 28597337.6203 - val_loss: 29540875.0796 - val_mean_squared_error: 29540875.0796
>     Epoch 36/2000
>     0s - loss: 28518172.5469 - mean_squared_error: 28518172.5469 - val_loss: 29460713.3188 - val_mean_squared_error: 29460713.3188
>     Epoch 37/2000
>     0s - loss: 28439189.1379 - mean_squared_error: 28439189.1379 - val_loss: 29380786.2996 - val_mean_squared_error: 29380786.2996
>     Epoch 38/2000
>     0s - loss: 28360479.6124 - mean_squared_error: 28360479.6124 - val_loss: 29301105.3030 - val_mean_squared_error: 29301105.3030
>     Epoch 39/2000
>     0s - loss: 28281907.3676 - mean_squared_error: 28281907.3676 - val_loss: 29221703.4147 - val_mean_squared_error: 29221703.4147
>     Epoch 40/2000
>     0s - loss: 28203632.6317 - mean_squared_error: 28203632.6317 - val_loss: 29142388.8403 - val_mean_squared_error: 29142388.8403
>     Epoch 41/2000
>     0s - loss: 28125720.7635 - mean_squared_error: 28125720.7635 - val_loss: 29063440.0929 - val_mean_squared_error: 29063440.0929
>     Epoch 42/2000
>     0s - loss: 28047976.8294 - mean_squared_error: 28047976.8294 - val_loss: 28984765.3663 - val_mean_squared_error: 28984765.3663
>     Epoch 43/2000
>     0s - loss: 27970467.7133 - mean_squared_error: 27970467.7133 - val_loss: 28906314.5428 - val_mean_squared_error: 28906314.5428
>     Epoch 44/2000
>     0s - loss: 27893208.9718 - mean_squared_error: 27893208.9718 - val_loss: 28828061.9170 - val_mean_squared_error: 28828061.9170
>     Epoch 45/2000
>     0s - loss: 27816199.5963 - mean_squared_error: 27816199.5963 - val_loss: 28750083.9624 - val_mean_squared_error: 28750083.9624
>     Epoch 46/2000
>     0s - loss: 27739430.8050 - mean_squared_error: 27739430.8050 - val_loss: 28672421.9278 - val_mean_squared_error: 28672421.9278
>     Epoch 47/2000
>     0s - loss: 27663038.7478 - mean_squared_error: 27663038.7478 - val_loss: 28595037.7182 - val_mean_squared_error: 28595037.7182
>     Epoch 48/2000
>     0s - loss: 27586935.5379 - mean_squared_error: 27586935.5379 - val_loss: 28517943.5106 - val_mean_squared_error: 28517943.5106
>     Epoch 49/2000
>     0s - loss: 27510856.8313 - mean_squared_error: 27510856.8313 - val_loss: 28440910.3431 - val_mean_squared_error: 28440910.3431
>     Epoch 50/2000
>     0s - loss: 27434969.3792 - mean_squared_error: 27434969.3792 - val_loss: 28364074.4834 - val_mean_squared_error: 28364074.4834
>     Epoch 51/2000
>     0s - loss: 27359761.8920 - mean_squared_error: 27359761.8920 - val_loss: 28287944.5457 - val_mean_squared_error: 28287944.5457
>     Epoch 52/2000
>     0s - loss: 27284671.0409 - mean_squared_error: 27284671.0409 - val_loss: 28211912.3114 - val_mean_squared_error: 28211912.3114
>     Epoch 53/2000
>     0s - loss: 27209731.6875 - mean_squared_error: 27209731.6875 - val_loss: 28136038.9214 - val_mean_squared_error: 28136038.9214
>     Epoch 54/2000
>     0s - loss: 27135022.6682 - mean_squared_error: 27135022.6682 - val_loss: 28060352.3164 - val_mean_squared_error: 28060352.3164
>     Epoch 55/2000
>     0s - loss: 27060586.6654 - mean_squared_error: 27060586.6654 - val_loss: 27984961.3940 - val_mean_squared_error: 27984961.3940
>     Epoch 56/2000
>     0s - loss: 26986420.5133 - mean_squared_error: 26986420.5133 - val_loss: 27909845.0381 - val_mean_squared_error: 27909845.0381
>     Epoch 57/2000
>     0s - loss: 26912567.4855 - mean_squared_error: 26912567.4855 - val_loss: 27835084.2106 - val_mean_squared_error: 27835084.2106
>     Epoch 58/2000
>     0s - loss: 26838929.6976 - mean_squared_error: 26838929.6976 - val_loss: 27760426.7158 - val_mean_squared_error: 27760426.7158
>     Epoch 59/2000
>     0s - loss: 26765518.3272 - mean_squared_error: 26765518.3272 - val_loss: 27685967.5502 - val_mean_squared_error: 27685967.5502
>     Epoch 60/2000
>     0s - loss: 26692322.5589 - mean_squared_error: 26692322.5589 - val_loss: 27611845.6352 - val_mean_squared_error: 27611845.6352
>     Epoch 61/2000
>     0s - loss: 26619445.8204 - mean_squared_error: 26619445.8204 - val_loss: 27538052.1671 - val_mean_squared_error: 27538052.1671
>     Epoch 62/2000
>     0s - loss: 26546752.7932 - mean_squared_error: 26546752.7932 - val_loss: 27464435.1191 - val_mean_squared_error: 27464435.1191
>     Epoch 63/2000
>     0s - loss: 26474278.3343 - mean_squared_error: 26474278.3343 - val_loss: 27390983.6263 - val_mean_squared_error: 27390983.6263
>     Epoch 64/2000
>     0s - loss: 26402108.0075 - mean_squared_error: 26402108.0075 - val_loss: 27317847.3178 - val_mean_squared_error: 27317847.3178
>     Epoch 65/2000
>     0s - loss: 26330228.8938 - mean_squared_error: 26330228.8938 - val_loss: 27245058.3915 - val_mean_squared_error: 27245058.3915
>     Epoch 66/2000
>     0s - loss: 26258603.2822 - mean_squared_error: 26258603.2822 - val_loss: 27172347.2239 - val_mean_squared_error: 27172347.2239
>     Epoch 67/2000
>     0s - loss: 26187237.4161 - mean_squared_error: 26187237.4161 - val_loss: 27100076.9669 - val_mean_squared_error: 27100076.9669
>     Epoch 68/2000
>     0s - loss: 26116061.9901 - mean_squared_error: 26116061.9901 - val_loss: 27027934.4113 - val_mean_squared_error: 27027934.4113
>     Epoch 69/2000
>     0s - loss: 26045186.1251 - mean_squared_error: 26045186.1251 - val_loss: 26956112.5230 - val_mean_squared_error: 26956112.5230
>     Epoch 70/2000
>     0s - loss: 25974512.2642 - mean_squared_error: 25974512.2642 - val_loss: 26884513.2279 - val_mean_squared_error: 26884513.2279
>     Epoch 71/2000
>     0s - loss: 25904048.9737 - mean_squared_error: 25904048.9737 - val_loss: 26813062.3302 - val_mean_squared_error: 26813062.3302
>     Epoch 72/2000
>     0s - loss: 25833944.2477 - mean_squared_error: 25833944.2477 - val_loss: 26742054.0425 - val_mean_squared_error: 26742054.0425
>     Epoch 73/2000
>     0s - loss: 25763985.1848 - mean_squared_error: 25763985.1848 - val_loss: 26671110.2837 - val_mean_squared_error: 26671110.2837
>     Epoch 74/2000
>     0s - loss: 25694380.7631 - mean_squared_error: 25694380.7631 - val_loss: 26600537.6480 - val_mean_squared_error: 26600537.6480
>     Epoch 75/2000
>     0s - loss: 25625011.2783 - mean_squared_error: 25625011.2783 - val_loss: 26530268.1532 - val_mean_squared_error: 26530268.1532
>     Epoch 76/2000
>     0s - loss: 25555874.8731 - mean_squared_error: 25555874.8731 - val_loss: 26460171.9209 - val_mean_squared_error: 26460171.9209
>     Epoch 77/2000
>     0s - loss: 25487025.0542 - mean_squared_error: 25487025.0542 - val_loss: 26390316.4755 - val_mean_squared_error: 26390316.4755
>     Epoch 78/2000
>     0s - loss: 25418531.3160 - mean_squared_error: 25418531.3160 - val_loss: 26320852.4142 - val_mean_squared_error: 26320852.4142
>     Epoch 79/2000
>     0s - loss: 25350210.5903 - mean_squared_error: 25350210.5903 - val_loss: 26251538.8680 - val_mean_squared_error: 26251538.8680
>     Epoch 80/2000
>     0s - loss: 25282049.1800 - mean_squared_error: 25282049.1800 - val_loss: 26182489.6945 - val_mean_squared_error: 26182489.6945
>     Epoch 81/2000
>     0s - loss: 25214188.3406 - mean_squared_error: 25214188.3406 - val_loss: 26113652.7454 - val_mean_squared_error: 26113652.7454
>     Epoch 82/2000
>     0s - loss: 25146608.0514 - mean_squared_error: 25146608.0514 - val_loss: 26045098.6060 - val_mean_squared_error: 26045098.6060
>     Epoch 83/2000
>     0s - loss: 25079185.4670 - mean_squared_error: 25079185.4670 - val_loss: 25976794.9174 - val_mean_squared_error: 25976794.9174
>     Epoch 84/2000
>     0s - loss: 25012180.5501 - mean_squared_error: 25012180.5501 - val_loss: 25908739.9891 - val_mean_squared_error: 25908739.9891
>     Epoch 85/2000
>     0s - loss: 24945394.8712 - mean_squared_error: 24945394.8712 - val_loss: 25841031.2071 - val_mean_squared_error: 25841031.2071
>     Epoch 86/2000
>     0s - loss: 24878763.3877 - mean_squared_error: 24878763.3877 - val_loss: 25773424.6990 - val_mean_squared_error: 25773424.6990
>     Epoch 87/2000
>     0s - loss: 24812414.1923 - mean_squared_error: 24812414.1923 - val_loss: 25706155.2803 - val_mean_squared_error: 25706155.2803
>     Epoch 88/2000
>     0s - loss: 24746393.5639 - mean_squared_error: 24746393.5639 - val_loss: 25639127.6846 - val_mean_squared_error: 25639127.6846
>     Epoch 89/2000
>     0s - loss: 24680511.3288 - mean_squared_error: 24680511.3288 - val_loss: 25572305.1023 - val_mean_squared_error: 25572305.1023
>     Epoch 90/2000
>     0s - loss: 24614862.6489 - mean_squared_error: 24614862.6489 - val_loss: 25505664.8206 - val_mean_squared_error: 25505664.8206
>     Epoch 91/2000
>     0s - loss: 24549502.9600 - mean_squared_error: 24549502.9600 - val_loss: 25439369.7934 - val_mean_squared_error: 25439369.7934
>     Epoch 92/2000
>     0s - loss: 24484435.3058 - mean_squared_error: 24484435.3058 - val_loss: 25373345.9011 - val_mean_squared_error: 25373345.9011
>     Epoch 93/2000
>     0s - loss: 24419628.8287 - mean_squared_error: 24419628.8287 - val_loss: 25307573.8606 - val_mean_squared_error: 25307573.8606
>     Epoch 94/2000
>     0s - loss: 24354878.3664 - mean_squared_error: 24354878.3664 - val_loss: 25241880.7632 - val_mean_squared_error: 25241880.7632
>     Epoch 95/2000
>     0s - loss: 24290514.3154 - mean_squared_error: 24290514.3154 - val_loss: 25176630.9946 - val_mean_squared_error: 25176630.9946
>     Epoch 96/2000
>     0s - loss: 24226314.9534 - mean_squared_error: 24226314.9534 - val_loss: 25111430.9086 - val_mean_squared_error: 25111430.9086
>     Epoch 97/2000
>     0s - loss: 24162397.7644 - mean_squared_error: 24162397.7644 - val_loss: 25046495.4652 - val_mean_squared_error: 25046495.4652
>     Epoch 98/2000
>     0s - loss: 24098688.4221 - mean_squared_error: 24098688.4221 - val_loss: 24981928.8581 - val_mean_squared_error: 24981928.8581
>     Epoch 99/2000
>     0s - loss: 24035391.1100 - mean_squared_error: 24035391.1100 - val_loss: 24917760.3213 - val_mean_squared_error: 24917760.3213
>     Epoch 100/2000
>     0s - loss: 23972230.2597 - mean_squared_error: 23972230.2597 - val_loss: 24853616.6762 - val_mean_squared_error: 24853616.6762
>     Epoch 101/2000
>     0s - loss: 23909590.8422 - mean_squared_error: 23909590.8422 - val_loss: 24790037.9417 - val_mean_squared_error: 24790037.9417
>     Epoch 102/2000
>     0s - loss: 23847228.1092 - mean_squared_error: 23847228.1092 - val_loss: 24726649.7014 - val_mean_squared_error: 24726649.7014
>     Epoch 103/2000
>     0s - loss: 23784856.3157 - mean_squared_error: 23784856.3157 - val_loss: 24663355.7350 - val_mean_squared_error: 24663355.7350
>     Epoch 104/2000
>     0s - loss: 23722950.6600 - mean_squared_error: 23722950.6600 - val_loss: 24600491.8408 - val_mean_squared_error: 24600491.8408
>     Epoch 105/2000
>     0s - loss: 23661334.2745 - mean_squared_error: 23661334.2745 - val_loss: 24537880.6594 - val_mean_squared_error: 24537880.6594
>     Epoch 106/2000
>     0s - loss: 23599847.4301 - mean_squared_error: 23599847.4301 - val_loss: 24475505.9703 - val_mean_squared_error: 24475505.9703
>     Epoch 107/2000
>     0s - loss: 23538647.1586 - mean_squared_error: 23538647.1586 - val_loss: 24413273.0895 - val_mean_squared_error: 24413273.0895
>     Epoch 108/2000
>     0s - loss: 23477616.2478 - mean_squared_error: 23477616.2478 - val_loss: 24351396.4271 - val_mean_squared_error: 24351396.4271
>     Epoch 109/2000
>     0s - loss: 23416867.7006 - mean_squared_error: 23416867.7006 - val_loss: 24289602.6871 - val_mean_squared_error: 24289602.6871
>     Epoch 110/2000
>     0s - loss: 23356153.7201 - mean_squared_error: 23356153.7201 - val_loss: 24227909.5116 - val_mean_squared_error: 24227909.5116
>     Epoch 111/2000
>     0s - loss: 23295973.5913 - mean_squared_error: 23295973.5913 - val_loss: 24166859.7370 - val_mean_squared_error: 24166859.7370
>     Epoch 112/2000
>     0s - loss: 23235967.4578 - mean_squared_error: 23235967.4578 - val_loss: 24105847.7212 - val_mean_squared_error: 24105847.7212
>     Epoch 113/2000
>     0s - loss: 23176225.2630 - mean_squared_error: 23176225.2630 - val_loss: 24045218.5151 - val_mean_squared_error: 24045218.5151
>     Epoch 114/2000
>     0s - loss: 23116696.7973 - mean_squared_error: 23116696.7973 - val_loss: 23984707.7044 - val_mean_squared_error: 23984707.7044
>     Epoch 115/2000
>     0s - loss: 23057368.6356 - mean_squared_error: 23057368.6356 - val_loss: 23924403.3673 - val_mean_squared_error: 23924403.3673
>     Epoch 116/2000
>     0s - loss: 22998439.4265 - mean_squared_error: 22998439.4265 - val_loss: 23864501.8151 - val_mean_squared_error: 23864501.8151
>     Epoch 117/2000
>     0s - loss: 22939638.6464 - mean_squared_error: 22939638.6464 - val_loss: 23804788.5408 - val_mean_squared_error: 23804788.5408
>     Epoch 118/2000
>     0s - loss: 22881141.0625 - mean_squared_error: 22881141.0625 - val_loss: 23745308.4963 - val_mean_squared_error: 23745308.4963
>     Epoch 119/2000
>     0s - loss: 22822845.2814 - mean_squared_error: 22822845.2814 - val_loss: 23686132.7711 - val_mean_squared_error: 23686132.7711
>     Epoch 120/2000
>     0s - loss: 22764842.0059 - mean_squared_error: 22764842.0059 - val_loss: 23627160.9016 - val_mean_squared_error: 23627160.9016
>     Epoch 121/2000
>     0s - loss: 22707210.8534 - mean_squared_error: 22707210.8534 - val_loss: 23568566.3094 - val_mean_squared_error: 23568566.3094
>     Epoch 122/2000
>     0s - loss: 22649688.2488 - mean_squared_error: 22649688.2488 - val_loss: 23510090.5615 - val_mean_squared_error: 23510090.5615
>     Epoch 123/2000
>     0s - loss: 22592563.7960 - mean_squared_error: 22592563.7960 - val_loss: 23451980.6881 - val_mean_squared_error: 23451980.6881
>     Epoch 124/2000
>     0s - loss: 22535702.3114 - mean_squared_error: 22535702.3114 - val_loss: 23394214.7365 - val_mean_squared_error: 23394214.7365
>     Epoch 125/2000
>     0s - loss: 22479009.5278 - mean_squared_error: 22479009.5278 - val_loss: 23336568.7168 - val_mean_squared_error: 23336568.7168
>     Epoch 126/2000
>     0s - loss: 22422458.5094 - mean_squared_error: 22422458.5094 - val_loss: 23278954.6495 - val_mean_squared_error: 23278954.6495
>     Epoch 127/2000
>     0s - loss: 22366156.1953 - mean_squared_error: 22366156.1953 - val_loss: 23221759.3198 - val_mean_squared_error: 23221759.3198
>     Epoch 128/2000
>     0s - loss: 22310161.8677 - mean_squared_error: 22310161.8677 - val_loss: 23164858.2106 - val_mean_squared_error: 23164858.2106
>     Epoch 129/2000
>     0s - loss: 22254358.3193 - mean_squared_error: 22254358.3193 - val_loss: 23108104.7079 - val_mean_squared_error: 23108104.7079
>     Epoch 130/2000
>     0s - loss: 22198901.8524 - mean_squared_error: 22198901.8524 - val_loss: 23051760.5606 - val_mean_squared_error: 23051760.5606
>     Epoch 131/2000
>     0s - loss: 22143714.7405 - mean_squared_error: 22143714.7405 - val_loss: 22995514.3431 - val_mean_squared_error: 22995514.3431
>     Epoch 132/2000
>     0s - loss: 22088707.1019 - mean_squared_error: 22088707.1019 - val_loss: 22939713.3554 - val_mean_squared_error: 22939713.3554
>     Epoch 133/2000
>     0s - loss: 22033971.6862 - mean_squared_error: 22033971.6862 - val_loss: 22883918.2996 - val_mean_squared_error: 22883918.2996
>     Epoch 134/2000
>     0s - loss: 21979650.6890 - mean_squared_error: 21979650.6890 - val_loss: 22828597.4602 - val_mean_squared_error: 22828597.4602
>     Epoch 135/2000
>     0s - loss: 21925520.8753 - mean_squared_error: 21925520.8753 - val_loss: 22773625.7766 - val_mean_squared_error: 22773625.7766
>     Epoch 136/2000
>     0s - loss: 21871532.8938 - mean_squared_error: 21871532.8938 - val_loss: 22718625.6560 - val_mean_squared_error: 22718625.6560
>     Epoch 137/2000
>     0s - loss: 21817803.7248 - mean_squared_error: 21817803.7248 - val_loss: 22664075.8033 - val_mean_squared_error: 22664075.8033
>     Epoch 138/2000
>     0s - loss: 21764467.1258 - mean_squared_error: 21764467.1258 - val_loss: 22609741.8764 - val_mean_squared_error: 22609741.8764
>     Epoch 139/2000
>     0s - loss: 21711308.7076 - mean_squared_error: 21711308.7076 - val_loss: 22555604.8641 - val_mean_squared_error: 22555604.8641
>     Epoch 140/2000
>     0s - loss: 21658456.6061 - mean_squared_error: 21658456.6061 - val_loss: 22501788.9105 - val_mean_squared_error: 22501788.9105
>     Epoch 141/2000
>     0s - loss: 21605722.7064 - mean_squared_error: 21605722.7064 - val_loss: 22448095.3811 - val_mean_squared_error: 22448095.3811
>     Epoch 142/2000
>     0s - loss: 21553311.5529 - mean_squared_error: 21553311.5529 - val_loss: 22394828.1671 - val_mean_squared_error: 22394828.1671
>     Epoch 143/2000
>     0s - loss: 21501229.8916 - mean_squared_error: 21501229.8916 - val_loss: 22341691.4760 - val_mean_squared_error: 22341691.4760
>     Epoch 144/2000
>     0s - loss: 21449316.0052 - mean_squared_error: 21449316.0052 - val_loss: 22288862.1651 - val_mean_squared_error: 22288862.1651
>     Epoch 145/2000
>     0s - loss: 21397706.1472 - mean_squared_error: 21397706.1472 - val_loss: 22236245.6718 - val_mean_squared_error: 22236245.6718
>     Epoch 146/2000
>     0s - loss: 21346361.3128 - mean_squared_error: 21346361.3128 - val_loss: 22184055.7350 - val_mean_squared_error: 22184055.7350
>     Epoch 147/2000
>     0s - loss: 21295354.3939 - mean_squared_error: 21295354.3939 - val_loss: 22131986.7187 - val_mean_squared_error: 22131986.7187
>     Epoch 148/2000
>     0s - loss: 21244395.6517 - mean_squared_error: 21244395.6517 - val_loss: 22080122.0524 - val_mean_squared_error: 22080122.0524
>     Epoch 149/2000
>     0s - loss: 21193727.0721 - mean_squared_error: 21193727.0721 - val_loss: 22028436.5685 - val_mean_squared_error: 22028436.5685
>     Epoch 150/2000
>     0s - loss: 21143190.2549 - mean_squared_error: 21143190.2549 - val_loss: 21977066.4419 - val_mean_squared_error: 21977066.4419
>     Epoch 151/2000
>     0s - loss: 21093130.4987 - mean_squared_error: 21093130.4987 - val_loss: 21926002.7336 - val_mean_squared_error: 21926002.7336
>     Epoch 152/2000
>     0s - loss: 21043143.3594 - mean_squared_error: 21043143.3594 - val_loss: 21875097.8922 - val_mean_squared_error: 21875097.8922
>     Epoch 153/2000
>     0s - loss: 20993615.0956 - mean_squared_error: 20993615.0956 - val_loss: 21824673.8853 - val_mean_squared_error: 21824673.8853
>     Epoch 154/2000
>     0s - loss: 20944319.4637 - mean_squared_error: 20944319.4637 - val_loss: 21774413.0133 - val_mean_squared_error: 21774413.0133
>     Epoch 155/2000
>     0s - loss: 20895142.7848 - mean_squared_error: 20895142.7848 - val_loss: 21724231.7252 - val_mean_squared_error: 21724231.7252
>     Epoch 156/2000
>     0s - loss: 20846213.5803 - mean_squared_error: 20846213.5803 - val_loss: 21674480.5319 - val_mean_squared_error: 21674480.5319
>     Epoch 157/2000
>     0s - loss: 20797359.1857 - mean_squared_error: 20797359.1857 - val_loss: 21624625.4246 - val_mean_squared_error: 21624625.4246
>     Epoch 158/2000
>     0s - loss: 20748934.2318 - mean_squared_error: 20748934.2318 - val_loss: 21575243.2467 - val_mean_squared_error: 21575243.2467
>     Epoch 159/2000
>     0s - loss: 20700770.2956 - mean_squared_error: 20700770.2956 - val_loss: 21526152.1048 - val_mean_squared_error: 21526152.1048
>     Epoch 160/2000
>     0s - loss: 20652950.5965 - mean_squared_error: 20652950.5965 - val_loss: 21477293.1359 - val_mean_squared_error: 21477293.1359
>     Epoch 161/2000
>     0s - loss: 20605280.5117 - mean_squared_error: 20605280.5117 - val_loss: 21428738.2392 - val_mean_squared_error: 21428738.2392
>     Epoch 162/2000
>     0s - loss: 20557982.9172 - mean_squared_error: 20557982.9172 - val_loss: 21380518.6752 - val_mean_squared_error: 21380518.6752
>     Epoch 163/2000
>     0s - loss: 20510898.1579 - mean_squared_error: 20510898.1579 - val_loss: 21332542.6149 - val_mean_squared_error: 21332542.6149
>     Epoch 164/2000
>     0s - loss: 20463997.4100 - mean_squared_error: 20463997.4100 - val_loss: 21284604.7850 - val_mean_squared_error: 21284604.7850
>     Epoch 165/2000
>     0s - loss: 20417180.8386 - mean_squared_error: 20417180.8386 - val_loss: 21236831.0786 - val_mean_squared_error: 21236831.0786
>     Epoch 166/2000
>     0s - loss: 20370759.0229 - mean_squared_error: 20370759.0229 - val_loss: 21189484.0583 - val_mean_squared_error: 21189484.0583
>     Epoch 167/2000
>     0s - loss: 20324643.3205 - mean_squared_error: 20324643.3205 - val_loss: 21142425.3347 - val_mean_squared_error: 21142425.3347
>     Epoch 168/2000
>     0s - loss: 20278844.6109 - mean_squared_error: 20278844.6109 - val_loss: 21095718.6772 - val_mean_squared_error: 21095718.6772
>     Epoch 169/2000
>     0s - loss: 20233132.3584 - mean_squared_error: 20233132.3584 - val_loss: 21049068.1532 - val_mean_squared_error: 21049068.1532
>     Epoch 170/2000
>     0s - loss: 20187772.1110 - mean_squared_error: 20187772.1110 - val_loss: 21002712.0475 - val_mean_squared_error: 21002712.0475
>     Epoch 171/2000
>     0s - loss: 20142637.7407 - mean_squared_error: 20142637.7407 - val_loss: 20956691.0479 - val_mean_squared_error: 20956691.0479
>     Epoch 172/2000
>     0s - loss: 20097782.0015 - mean_squared_error: 20097782.0015 - val_loss: 20910924.6663 - val_mean_squared_error: 20910924.6663
>     Epoch 173/2000
>     0s - loss: 20053208.6498 - mean_squared_error: 20053208.6498 - val_loss: 20865415.1686 - val_mean_squared_error: 20865415.1686
>     Epoch 174/2000
>     0s - loss: 20008791.6528 - mean_squared_error: 20008791.6528 - val_loss: 20819986.0989 - val_mean_squared_error: 20819986.0989
>     Epoch 175/2000
>     0s - loss: 19964600.6521 - mean_squared_error: 19964600.6521 - val_loss: 20774969.7153 - val_mean_squared_error: 20774969.7153
>     Epoch 176/2000
>     0s - loss: 19920806.2669 - mean_squared_error: 19920806.2669 - val_loss: 20730169.7519 - val_mean_squared_error: 20730169.7519
>     Epoch 177/2000
>     0s - loss: 19877242.4940 - mean_squared_error: 19877242.4940 - val_loss: 20685607.3208 - val_mean_squared_error: 20685607.3208
>
>     *** WARNING: skipped 102808 bytes of output ***
>
>     Epoch 902/2000
>     0s - loss: 15919843.5900 - mean_squared_error: 15919843.5900 - val_loss: 16556705.3307 - val_mean_squared_error: 16556705.3307
>     Epoch 903/2000
>     0s - loss: 15919841.9568 - mean_squared_error: 15919841.9568 - val_loss: 16556726.9951 - val_mean_squared_error: 16556726.9951
>     Epoch 904/2000
>     0s - loss: 15919843.3843 - mean_squared_error: 15919843.3843 - val_loss: 16556725.9095 - val_mean_squared_error: 16556725.9095
>     Epoch 905/2000
>     0s - loss: 15919836.1696 - mean_squared_error: 15919836.1696 - val_loss: 16556709.0188 - val_mean_squared_error: 16556709.0188
>     Epoch 906/2000
>     0s - loss: 15919839.3615 - mean_squared_error: 15919839.3615 - val_loss: 16556717.1839 - val_mean_squared_error: 16556717.1839
>     Epoch 907/2000
>     0s - loss: 15919839.9579 - mean_squared_error: 15919839.9579 - val_loss: 16556713.5091 - val_mean_squared_error: 16556713.5091
>     Epoch 908/2000
>     0s - loss: 15919840.4877 - mean_squared_error: 15919840.4877 - val_loss: 16556708.2926 - val_mean_squared_error: 16556708.2926
>     Epoch 909/2000
>     0s - loss: 15919841.1513 - mean_squared_error: 15919841.1513 - val_loss: 16556708.1691 - val_mean_squared_error: 16556708.1691
>     Epoch 910/2000
>     0s - loss: 15919840.2287 - mean_squared_error: 15919840.2287 - val_loss: 16556699.7370 - val_mean_squared_error: 16556699.7370
>     Epoch 911/2000
>     0s - loss: 15919838.4660 - mean_squared_error: 15919838.4660 - val_loss: 16556705.3964 - val_mean_squared_error: 16556705.3964
>     Epoch 912/2000
>     0s - loss: 15919836.5301 - mean_squared_error: 15919836.5301 - val_loss: 16556698.5877 - val_mean_squared_error: 16556698.5877
>     Epoch 913/2000
>     0s - loss: 15919836.6192 - mean_squared_error: 15919836.6192 - val_loss: 16556698.2798 - val_mean_squared_error: 16556698.2798
>     Epoch 914/2000
>     0s - loss: 15919845.3570 - mean_squared_error: 15919845.3570 - val_loss: 16556677.7410 - val_mean_squared_error: 16556677.7410
>     Epoch 915/2000
>     0s - loss: 15919837.0561 - mean_squared_error: 15919837.0561 - val_loss: 16556692.0568 - val_mean_squared_error: 16556692.0568
>     Epoch 916/2000
>     0s - loss: 15919837.8057 - mean_squared_error: 15919837.8057 - val_loss: 16556704.5067 - val_mean_squared_error: 16556704.5067
>     Epoch 917/2000
>     0s - loss: 15919838.7489 - mean_squared_error: 15919838.7489 - val_loss: 16556695.5699 - val_mean_squared_error: 16556695.5699
>     Epoch 918/2000
>     0s - loss: 15919841.9352 - mean_squared_error: 15919841.9352 - val_loss: 16556679.5166 - val_mean_squared_error: 16556679.5166
>     Epoch 919/2000
>     0s - loss: 15919836.8235 - mean_squared_error: 15919836.8235 - val_loss: 16556687.9011 - val_mean_squared_error: 16556687.9011
>     Epoch 920/2000
>     0s - loss: 15919841.7907 - mean_squared_error: 15919841.7907 - val_loss: 16556701.8216 - val_mean_squared_error: 16556701.8216
>     Epoch 921/2000
>     0s - loss: 15919840.6013 - mean_squared_error: 15919840.6013 - val_loss: 16556698.0222 - val_mean_squared_error: 16556698.0222
>     Epoch 922/2000
>     0s - loss: 15919839.6217 - mean_squared_error: 15919839.6217 - val_loss: 16556710.5749 - val_mean_squared_error: 16556710.5749
>     Epoch 923/2000
>     0s - loss: 15919846.4129 - mean_squared_error: 15919846.4129 - val_loss: 16556698.2333 - val_mean_squared_error: 16556698.2333
>     Epoch 924/2000
>     0s - loss: 15919838.1337 - mean_squared_error: 15919838.1337 - val_loss: 16556707.9580 - val_mean_squared_error: 16556707.9580
>     Epoch 925/2000
>     0s - loss: 15919840.4201 - mean_squared_error: 15919840.4201 - val_loss: 16556706.3213 - val_mean_squared_error: 16556706.3213
>     Epoch 926/2000
>     0s - loss: 15919839.9335 - mean_squared_error: 15919839.9335 - val_loss: 16556700.9698 - val_mean_squared_error: 16556700.9698
>     Epoch 927/2000
>     0s - loss: 15919840.5632 - mean_squared_error: 15919840.5632 - val_loss: 16556697.4053 - val_mean_squared_error: 16556697.4053
>     Epoch 928/2000
>     0s - loss: 15919838.6560 - mean_squared_error: 15919838.6560 - val_loss: 16556689.2813 - val_mean_squared_error: 16556689.2813
>     Epoch 929/2000
>     0s - loss: 15919839.7693 - mean_squared_error: 15919839.7693 - val_loss: 16556712.4721 - val_mean_squared_error: 16556712.4721
>     Epoch 930/2000
>     0s - loss: 15919839.8359 - mean_squared_error: 15919839.8359 - val_loss: 16556713.7410 - val_mean_squared_error: 16556713.7410
>     Epoch 931/2000
>     0s - loss: 15919839.2583 - mean_squared_error: 15919839.2583 - val_loss: 16556717.8482 - val_mean_squared_error: 16556717.8482
>     Epoch 932/2000
>     0s - loss: 15919842.8227 - mean_squared_error: 15919842.8227 - val_loss: 16556733.4671 - val_mean_squared_error: 16556733.4671
>     Epoch 933/2000
>     0s - loss: 15919841.4161 - mean_squared_error: 15919841.4161 - val_loss: 16556736.0069 - val_mean_squared_error: 16556736.0069
>     Epoch 934/2000
>     0s - loss: 15919844.1217 - mean_squared_error: 15919844.1217 - val_loss: 16556737.9773 - val_mean_squared_error: 16556737.9773
>     Epoch 935/2000
>     0s - loss: 15919839.1149 - mean_squared_error: 15919839.1149 - val_loss: 16556727.5245 - val_mean_squared_error: 16556727.5245
>     Epoch 936/2000
>     0s - loss: 15919841.2645 - mean_squared_error: 15919841.2645 - val_loss: 16556723.7766 - val_mean_squared_error: 16556723.7766
>     Epoch 937/2000
>     0s - loss: 15919841.1313 - mean_squared_error: 15919841.1313 - val_loss: 16556706.9086 - val_mean_squared_error: 16556706.9086
>     Epoch 938/2000
>     0s - loss: 15919837.0888 - mean_squared_error: 15919837.0888 - val_loss: 16556712.3361 - val_mean_squared_error: 16556712.3361
>     Epoch 939/2000
>     0s - loss: 15919839.4791 - mean_squared_error: 15919839.4791 - val_loss: 16556722.5779 - val_mean_squared_error: 16556722.5779
>     Epoch 940/2000
>     0s - loss: 15919838.5991 - mean_squared_error: 15919838.5991 - val_loss: 16556727.4647 - val_mean_squared_error: 16556727.4647
>     Epoch 941/2000
>     0s - loss: 15919839.8708 - mean_squared_error: 15919839.8708 - val_loss: 16556722.1483 - val_mean_squared_error: 16556722.1483
>     Epoch 942/2000
>     0s - loss: 15919841.8367 - mean_squared_error: 15919841.8367 - val_loss: 16556730.3722 - val_mean_squared_error: 16556730.3722
>     Epoch 943/2000
>     0s - loss: 15919839.1199 - mean_squared_error: 15919839.1199 - val_loss: 16556724.7756 - val_mean_squared_error: 16556724.7756
>     Epoch 944/2000
>     0s - loss: 15919837.4214 - mean_squared_error: 15919837.4214 - val_loss: 16556716.1097 - val_mean_squared_error: 16556716.1097
>     Epoch 945/2000
>     0s - loss: 15919839.8151 - mean_squared_error: 15919839.8151 - val_loss: 16556719.8660 - val_mean_squared_error: 16556719.8660
>     Epoch 946/2000
>     0s - loss: 15919840.9469 - mean_squared_error: 15919840.9469 - val_loss: 16556740.2966 - val_mean_squared_error: 16556740.2966
>     Epoch 947/2000
>     0s - loss: 15919837.3664 - mean_squared_error: 15919837.3664 - val_loss: 16556745.9664 - val_mean_squared_error: 16556745.9664
>     Epoch 948/2000
>     0s - loss: 15919836.3783 - mean_squared_error: 15919836.3783 - val_loss: 16556720.6723 - val_mean_squared_error: 16556720.6723
>     Epoch 949/2000
>     0s - loss: 15919839.2918 - mean_squared_error: 15919839.2918 - val_loss: 16556724.7632 - val_mean_squared_error: 16556724.7632
>     Epoch 950/2000
>     0s - loss: 15919838.2604 - mean_squared_error: 15919838.2604 - val_loss: 16556699.6115 - val_mean_squared_error: 16556699.6115
>     Epoch 951/2000
>     0s - loss: 15919840.5279 - mean_squared_error: 15919840.5279 - val_loss: 16556704.1503 - val_mean_squared_error: 16556704.1503
>     Epoch 952/2000
>     0s - loss: 15919842.7607 - mean_squared_error: 15919842.7607 - val_loss: 16556712.3846 - val_mean_squared_error: 16556712.3846
>     Epoch 953/2000
>     0s - loss: 15919845.1093 - mean_squared_error: 15919845.1093 - val_loss: 16556720.6312 - val_mean_squared_error: 16556720.6312
>     Epoch 954/2000
>     0s - loss: 15919839.4524 - mean_squared_error: 15919839.4524 - val_loss: 16556698.7341 - val_mean_squared_error: 16556698.7341
>     Epoch 955/2000
>     0s - loss: 15919838.3247 - mean_squared_error: 15919838.3247 - val_loss: 16556684.4128 - val_mean_squared_error: 16556684.4128
>     Epoch 956/2000
>     0s - loss: 15919839.0095 - mean_squared_error: 15919839.0095 - val_loss: 16556682.5126 - val_mean_squared_error: 16556682.5126
>     Epoch 957/2000
>     0s - loss: 15919845.1882 - mean_squared_error: 15919845.1882 - val_loss: 16556662.4795 - val_mean_squared_error: 16556662.4795
>     Epoch 958/2000
>     0s - loss: 15919838.5643 - mean_squared_error: 15919838.5643 - val_loss: 16556667.4993 - val_mean_squared_error: 16556667.4993
>     Epoch 959/2000
>     0s - loss: 15919839.7602 - mean_squared_error: 15919839.7602 - val_loss: 16556669.4469 - val_mean_squared_error: 16556669.4469
>     Epoch 960/2000
>     0s - loss: 15919840.5657 - mean_squared_error: 15919840.5657 - val_loss: 16556671.6149 - val_mean_squared_error: 16556671.6149
>     Epoch 961/2000
>     0s - loss: 15919839.0706 - mean_squared_error: 15919839.0706 - val_loss: 16556672.0484 - val_mean_squared_error: 16556672.0484
>     Epoch 962/2000
>     0s - loss: 15919841.4150 - mean_squared_error: 15919841.4150 - val_loss: 16556665.8092 - val_mean_squared_error: 16556665.8092
>     Epoch 963/2000
>     0s - loss: 15919841.3326 - mean_squared_error: 15919841.3326 - val_loss: 16556670.1987 - val_mean_squared_error: 16556670.1987
>     Epoch 964/2000
>     0s - loss: 15919838.0294 - mean_squared_error: 15919838.0294 - val_loss: 16556654.0361 - val_mean_squared_error: 16556654.0361
>     Epoch 965/2000
>     0s - loss: 15919844.4718 - mean_squared_error: 15919844.4718 - val_loss: 16556663.8967 - val_mean_squared_error: 16556663.8967
>     Epoch 966/2000
>     0s - loss: 15919845.2737 - mean_squared_error: 15919845.2737 - val_loss: 16556665.8596 - val_mean_squared_error: 16556665.8596
>     Epoch 967/2000
>     0s - loss: 15919843.5095 - mean_squared_error: 15919843.5095 - val_loss: 16556664.5002 - val_mean_squared_error: 16556664.5002
>     Epoch 968/2000
>     0s - loss: 15919847.4543 - mean_squared_error: 15919847.4543 - val_loss: 16556658.1908 - val_mean_squared_error: 16556658.1908
>     Epoch 969/2000
>     0s - loss: 15919838.7048 - mean_squared_error: 15919838.7048 - val_loss: 16556662.1468 - val_mean_squared_error: 16556662.1468
>     Epoch 970/2000
>     0s - loss: 15919841.1627 - mean_squared_error: 15919841.1627 - val_loss: 16556676.3599 - val_mean_squared_error: 16556676.3599
>     Epoch 971/2000
>     0s - loss: 15919842.0291 - mean_squared_error: 15919842.0291 - val_loss: 16556687.4068 - val_mean_squared_error: 16556687.4068
>     Epoch 972/2000
>     0s - loss: 15919836.4638 - mean_squared_error: 15919836.4638 - val_loss: 16556682.0430 - val_mean_squared_error: 16556682.0430
>     Epoch 973/2000
>     0s - loss: 15919837.2341 - mean_squared_error: 15919837.2341 - val_loss: 16556690.0455 - val_mean_squared_error: 16556690.0455
>     Epoch 974/2000
>     0s - loss: 15919838.2425 - mean_squared_error: 15919838.2425 - val_loss: 16556694.2022 - val_mean_squared_error: 16556694.2022
>     Epoch 975/2000
>     0s - loss: 15919840.9025 - mean_squared_error: 15919840.9025 - val_loss: 16556692.2536 - val_mean_squared_error: 16556692.2536
>     Epoch 976/2000
>     0s - loss: 15919839.4944 - mean_squared_error: 15919839.4944 - val_loss: 16556692.7716 - val_mean_squared_error: 16556692.7716
>     Epoch 977/2000
>     0s - loss: 15919849.1469 - mean_squared_error: 15919849.1469 - val_loss: 16556672.7860 - val_mean_squared_error: 16556672.7860
>     Epoch 978/2000
>     0s - loss: 15919839.3687 - mean_squared_error: 15919839.3687 - val_loss: 16556680.3292 - val_mean_squared_error: 16556680.3292
>     Epoch 979/2000
>     0s - loss: 15919837.4643 - mean_squared_error: 15919837.4643 - val_loss: 16556692.6233 - val_mean_squared_error: 16556692.6233
>     Epoch 980/2000
>     0s - loss: 15919843.2906 - mean_squared_error: 15919843.2906 - val_loss: 16556699.0697 - val_mean_squared_error: 16556699.0697
>     Epoch 981/2000
>     0s - loss: 15919840.3506 - mean_squared_error: 15919840.3506 - val_loss: 16556695.3475 - val_mean_squared_error: 16556695.3475
>     Epoch 982/2000
>     0s - loss: 15919840.4095 - mean_squared_error: 15919840.4095 - val_loss: 16556672.7716 - val_mean_squared_error: 16556672.7716
>     Epoch 983/2000
>     0s - loss: 15919844.2154 - mean_squared_error: 15919844.2154 - val_loss: 16556681.8349 - val_mean_squared_error: 16556681.8349
>     Epoch 984/2000
>     0s - loss: 15919836.5471 - mean_squared_error: 15919836.5471 - val_loss: 16556693.9654 - val_mean_squared_error: 16556693.9654
>     Epoch 985/2000
>     0s - loss: 15919838.9118 - mean_squared_error: 15919838.9118 - val_loss: 16556702.1656 - val_mean_squared_error: 16556702.1656
>     Epoch 986/2000
>     0s - loss: 15919842.4789 - mean_squared_error: 15919842.4789 - val_loss: 16556695.3475 - val_mean_squared_error: 16556695.3475
>     Epoch 987/2000
>     0s - loss: 15919839.4961 - mean_squared_error: 15919839.4961 - val_loss: 16556701.4849 - val_mean_squared_error: 16556701.4849
>     Epoch 988/2000
>     0s - loss: 15919838.8019 - mean_squared_error: 15919838.8019 - val_loss: 16556697.1705 - val_mean_squared_error: 16556697.1705
>     Epoch 989/2000
>     0s - loss: 15919835.7436 - mean_squared_error: 15919835.7436 - val_loss: 16556691.8448 - val_mean_squared_error: 16556691.8448
>     Epoch 990/2000
>     0s - loss: 15919836.5202 - mean_squared_error: 15919836.5202 - val_loss: 16556677.8675 - val_mean_squared_error: 16556677.8675
>     Epoch 991/2000
>     0s - loss: 15919844.1906 - mean_squared_error: 15919844.1906 - val_loss: 16556688.5190 - val_mean_squared_error: 16556688.5190
>     Epoch 992/2000
>     0s - loss: 15919838.8634 - mean_squared_error: 15919838.8634 - val_loss: 16556692.8705 - val_mean_squared_error: 16556692.8705
>     Epoch 993/2000
>     0s - loss: 15919837.9150 - mean_squared_error: 15919837.9150 - val_loss: 16556687.9011 - val_mean_squared_error: 16556687.9011
>     Epoch 994/2000
>     0s - loss: 15919839.2722 - mean_squared_error: 15919839.2722 - val_loss: 16556712.8151 - val_mean_squared_error: 16556712.8151
>     Epoch 995/2000
>     0s - loss: 15919838.7999 - mean_squared_error: 15919838.7999 - val_loss: 16556700.8309 - val_mean_squared_error: 16556700.8309
>     Epoch 996/2000
>     0s - loss: 15919838.6425 - mean_squared_error: 15919838.6425 - val_loss: 16556721.4706 - val_mean_squared_error: 16556721.4706
>     Epoch 997/2000
>     0s - loss: 15919843.8874 - mean_squared_error: 15919843.8874 - val_loss: 16556710.2165 - val_mean_squared_error: 16556710.2165
>     Epoch 998/2000
>     0s - loss: 15919839.0948 - mean_squared_error: 15919839.0948 - val_loss: 16556714.8769 - val_mean_squared_error: 16556714.8769
>     Epoch 999/2000
>     0s - loss: 15919839.1214 - mean_squared_error: 15919839.1214 - val_loss: 16556691.3678 - val_mean_squared_error: 16556691.3678
>     Epoch 1000/2000
>     0s - loss: 15919843.9814 - mean_squared_error: 15919843.9814 - val_loss: 16556703.2852 - val_mean_squared_error: 16556703.2852
>     Epoch 1001/2000
>     0s - loss: 15919837.1706 - mean_squared_error: 15919837.1706 - val_loss: 16556725.3317 - val_mean_squared_error: 16556725.3317
>     Epoch 1002/2000
>     0s - loss: 15919842.0718 - mean_squared_error: 15919842.0718 - val_loss: 16556716.9842 - val_mean_squared_error: 16556716.9842
>     Epoch 1003/2000
>     0s - loss: 15919842.7672 - mean_squared_error: 15919842.7672 - val_loss: 16556721.4706 - val_mean_squared_error: 16556721.4706
>     Epoch 1004/2000
>     0s - loss: 15919836.5253 - mean_squared_error: 15919836.5253 - val_loss: 16556729.2269 - val_mean_squared_error: 16556729.2269
>     Epoch 1005/2000
>     0s - loss: 15919841.4145 - mean_squared_error: 15919841.4145 - val_loss: 16556727.7202 - val_mean_squared_error: 16556727.7202
>     Epoch 1006/2000
>     0s - loss: 15919838.1763 - mean_squared_error: 15919838.1763 - val_loss: 16556731.5897 - val_mean_squared_error: 16556731.5897
>     Epoch 1007/2000
>     0s - loss: 15919843.1474 - mean_squared_error: 15919843.1474 - val_loss: 16556728.3134 - val_mean_squared_error: 16556728.3134
>     Epoch 1008/2000
>     0s - loss: 15919838.3750 - mean_squared_error: 15919838.3750 - val_loss: 16556739.3119 - val_mean_squared_error: 16556739.3119
>     Epoch 1009/2000
>     0s - loss: 15919838.2583 - mean_squared_error: 15919838.2583 - val_loss: 16556745.2279 - val_mean_squared_error: 16556745.2279
>     Epoch 1010/2000
>     0s - loss: 15919841.1238 - mean_squared_error: 15919841.1238 - val_loss: 16556744.1196 - val_mean_squared_error: 16556744.1196
>     Epoch 1011/2000
>     0s - loss: 15919840.6053 - mean_squared_error: 15919840.6053 - val_loss: 16556738.9165 - val_mean_squared_error: 16556738.9165
>     Epoch 1012/2000
>     0s - loss: 15919839.0900 - mean_squared_error: 15919839.0900 - val_loss: 16556739.0371 - val_mean_squared_error: 16556739.0371
>     Epoch 1013/2000
>     0s - loss: 15919842.0409 - mean_squared_error: 15919842.0409 - val_loss: 16556750.1364 - val_mean_squared_error: 16556750.1364
>     Epoch 1014/2000
>     0s - loss: 15919840.4106 - mean_squared_error: 15919840.4106 - val_loss: 16556746.5452 - val_mean_squared_error: 16556746.5452
>     Epoch 1015/2000
>     0s - loss: 15919837.6779 - mean_squared_error: 15919837.6779 - val_loss: 16556740.7909 - val_mean_squared_error: 16556740.7909
>     Epoch 1016/2000
>     0s - loss: 15919843.2734 - mean_squared_error: 15919843.2734 - val_loss: 16556727.7079 - val_mean_squared_error: 16556727.7079
>     Epoch 1017/2000
>     0s - loss: 15919841.6282 - mean_squared_error: 15919841.6282 - val_loss: 16556741.3307 - val_mean_squared_error: 16556741.3307
>     Epoch 1018/2000
>     0s - loss: 15919838.1010 - mean_squared_error: 15919838.1010 - val_loss: 16556731.7998 - val_mean_squared_error: 16556731.7998
>     Epoch 1019/2000
>     0s - loss: 15919838.9824 - mean_squared_error: 15919838.9824 - val_loss: 16556752.4899 - val_mean_squared_error: 16556752.4899
>     Epoch 1020/2000
>     0s - loss: 15919839.9943 - mean_squared_error: 15919839.9943 - val_loss: 16556732.6649 - val_mean_squared_error: 16556732.6649
>     Epoch 1021/2000
>     0s - loss: 15919835.5637 - mean_squared_error: 15919835.5637 - val_loss: 16556754.9946 - val_mean_squared_error: 16556754.9946
>     Epoch 1022/2000
>     0s - loss: 15919839.1074 - mean_squared_error: 15919839.1074 - val_loss: 16556758.4068 - val_mean_squared_error: 16556758.4068
>     Epoch 1023/2000
>     0s - loss: 15919837.4119 - mean_squared_error: 15919837.4119 - val_loss: 16556763.8201 - val_mean_squared_error: 16556763.8201
>     Epoch 1024/2000
>     0s - loss: 15919843.7663 - mean_squared_error: 15919843.7663 - val_loss: 16556754.4736 - val_mean_squared_error: 16556754.4736
>     Epoch 1025/2000
>     0s - loss: 15919841.5021 - mean_squared_error: 15919841.5021 - val_loss: 16556766.6184 - val_mean_squared_error: 16556766.6184
>     Epoch 1026/2000
>     0s - loss: 15919839.0376 - mean_squared_error: 15919839.0376 - val_loss: 16556769.8453 - val_mean_squared_error: 16556769.8453
>     Epoch 1027/2000
>     0s - loss: 15919836.4219 - mean_squared_error: 15919836.4219 - val_loss: 16556763.2887 - val_mean_squared_error: 16556763.2887
>     Epoch 1028/2000
>     0s - loss: 15919840.6584 - mean_squared_error: 15919840.6584 - val_loss: 16556761.3411 - val_mean_squared_error: 16556761.3411
>     Epoch 1029/2000
>     0s - loss: 15919843.3653 - mean_squared_error: 15919843.3653 - val_loss: 16556775.1112 - val_mean_squared_error: 16556775.1112
>     Epoch 1030/2000
>     0s - loss: 15919839.2409 - mean_squared_error: 15919839.2409 - val_loss: 16556795.2452 - val_mean_squared_error: 16556795.2452
>     Epoch 1031/2000
>     0s - loss: 15919843.7620 - mean_squared_error: 15919843.7620 - val_loss: 16556775.6169 - val_mean_squared_error: 16556775.6169
>     Epoch 1032/2000
>     0s - loss: 15919843.4125 - mean_squared_error: 15919843.4125 - val_loss: 16556775.3080 - val_mean_squared_error: 16556775.3080
>     Epoch 1033/2000
>     0s - loss: 15919843.5943 - mean_squared_error: 15919843.5943 - val_loss: 16556767.7009 - val_mean_squared_error: 16556767.7009
>     Epoch 1034/2000
>     0s - loss: 15919842.2082 - mean_squared_error: 15919842.2082 - val_loss: 16556764.2022 - val_mean_squared_error: 16556764.2022
>     Epoch 1035/2000
>     0s - loss: 15919841.1044 - mean_squared_error: 15919841.1044 - val_loss: 16556775.7909 - val_mean_squared_error: 16556775.7909
>     Epoch 1036/2000
>     0s - loss: 15919837.7204 - mean_squared_error: 15919837.7204 - val_loss: 16556782.3885 - val_mean_squared_error: 16556782.3885
>     Epoch 1037/2000
>     0s - loss: 15919839.1673 - mean_squared_error: 15919839.1673 - val_loss: 16556776.1587 - val_mean_squared_error: 16556776.1587
>     Epoch 1038/2000
>     0s - loss: 15919846.6270 - mean_squared_error: 15919846.6270 - val_loss: 16556778.5635 - val_mean_squared_error: 16556778.5635
>     Epoch 1039/2000
>     0s - loss: 15919837.2989 - mean_squared_error: 15919837.2989 - val_loss: 16556772.1048 - val_mean_squared_error: 16556772.1048
>     Epoch 1040/2000
>     0s - loss: 15919838.8277 - mean_squared_error: 15919838.8277 - val_loss: 16556774.2615 - val_mean_squared_error: 16556774.2615
>     Epoch 1041/2000
>     0s - loss: 15919842.0086 - mean_squared_error: 15919842.0086 - val_loss: 16556770.6391 - val_mean_squared_error: 16556770.6391
>     Epoch 1042/2000
>     0s - loss: 15919838.8128 - mean_squared_error: 15919838.8128 - val_loss: 16556773.7064 - val_mean_squared_error: 16556773.7064
>     Epoch 1043/2000
>     0s - loss: 15919841.8081 - mean_squared_error: 15919841.8081 - val_loss: 16556769.8225 - val_mean_squared_error: 16556769.8225
>     Epoch 1044/2000
>     0s - loss: 15919837.7698 - mean_squared_error: 15919837.7698 - val_loss: 16556761.9219 - val_mean_squared_error: 16556761.9219
>     Epoch 1045/2000
>     0s - loss: 15919838.9376 - mean_squared_error: 15919838.9376 - val_loss: 16556772.1295 - val_mean_squared_error: 16556772.1295
>     Epoch 1046/2000
>     0s - loss: 15919840.5204 - mean_squared_error: 15919840.5204 - val_loss: 16556785.7009 - val_mean_squared_error: 16556785.7009
>     Epoch 1047/2000
>     0s - loss: 15919838.4786 - mean_squared_error: 15919838.4786 - val_loss: 16556803.9605 - val_mean_squared_error: 16556803.9605
>     Epoch 1048/2000
>     0s - loss: 15919838.7149 - mean_squared_error: 15919838.7149 - val_loss: 16556790.6466 - val_mean_squared_error: 16556790.6466
>     Epoch 1049/2000
>     0s - loss: 15919845.1238 - mean_squared_error: 15919845.1238 - val_loss: 16556795.4058 - val_mean_squared_error: 16556795.4058
>     Epoch 1050/2000
>     0s - loss: 15919838.8359 - mean_squared_error: 15919838.8359 - val_loss: 16556797.3638 - val_mean_squared_error: 16556797.3638
>     Epoch 1051/2000
>     0s - loss: 15919840.7287 - mean_squared_error: 15919840.7287 - val_loss: 16556796.8725 - val_mean_squared_error: 16556796.8725
>     Epoch 1052/2000
>     0s - loss: 15919838.7088 - mean_squared_error: 15919838.7088 - val_loss: 16556796.9323 - val_mean_squared_error: 16556796.9323
>     Epoch 1053/2000
>     0s - loss: 15919839.2940 - mean_squared_error: 15919839.2940 - val_loss: 16556798.3772 - val_mean_squared_error: 16556798.3772
>     Epoch 1054/2000
>     0s - loss: 15919846.0680 - mean_squared_error: 15919846.0680 - val_loss: 16556799.4647 - val_mean_squared_error: 16556799.4647
>     Epoch 1055/2000
>     0s - loss: 15919843.6236 - mean_squared_error: 15919843.6236 - val_loss: 16556787.4281 - val_mean_squared_error: 16556787.4281
>     Epoch 1056/2000
>     0s - loss: 15919840.2074 - mean_squared_error: 15919840.2074 - val_loss: 16556796.8725 - val_mean_squared_error: 16556796.8725
>     Epoch 1057/2000
>     0s - loss: 15919842.7169 - mean_squared_error: 15919842.7169 - val_loss: 16556782.1898 - val_mean_squared_error: 16556782.1898
>     Epoch 1058/2000
>     0s - loss: 15919839.8897 - mean_squared_error: 15919839.8897 - val_loss: 16556772.3900 - val_mean_squared_error: 16556772.3900
>     Epoch 1059/2000
>     0s - loss: 15919835.9401 - mean_squared_error: 15919835.9401 - val_loss: 16556787.6752 - val_mean_squared_error: 16556787.6752
>     Epoch 1060/2000
>     0s - loss: 15919841.5436 - mean_squared_error: 15919841.5436 - val_loss: 16556789.4859 - val_mean_squared_error: 16556789.4859
>     Epoch 1061/2000
>     0s - loss: 15919842.0457 - mean_squared_error: 15919842.0457 - val_loss: 16556797.3762 - val_mean_squared_error: 16556797.3762
>     Epoch 1062/2000
>     0s - loss: 15919837.5491 - mean_squared_error: 15919837.5491 - val_loss: 16556798.6253 - val_mean_squared_error: 16556798.6253
>     Epoch 1063/2000
>     0s - loss: 15919841.4918 - mean_squared_error: 15919841.4918 - val_loss: 16556796.1691 - val_mean_squared_error: 16556796.1691
>     Epoch 1064/2000
>     0s - loss: 15919840.4691 - mean_squared_error: 15919840.4691 - val_loss: 16556787.1325 - val_mean_squared_error: 16556787.1325
>     Epoch 1065/2000
>     0s - loss: 15919838.6524 - mean_squared_error: 15919838.6524 - val_loss: 16556801.6317 - val_mean_squared_error: 16556801.6317
>     Epoch 1066/2000
>     0s - loss: 15919838.6565 - mean_squared_error: 15919838.6565 - val_loss: 16556787.7731 - val_mean_squared_error: 16556787.7731
>     Epoch 1067/2000
>     0s - loss: 15919848.1406 - mean_squared_error: 15919848.1406 - val_loss: 16556797.1053 - val_mean_squared_error: 16556797.1053
>     Epoch 1068/2000
>     0s - loss: 15919838.3528 - mean_squared_error: 15919838.3528 - val_loss: 16556793.5551 - val_mean_squared_error: 16556793.5551
>     Epoch 1069/2000
>     0s - loss: 15919841.1798 - mean_squared_error: 15919841.1798 - val_loss: 16556795.6396 - val_mean_squared_error: 16556795.6396
>     Epoch 1070/2000
>     0s - loss: 15919842.8504 - mean_squared_error: 15919842.8504 - val_loss: 16556796.8087 - val_mean_squared_error: 16556796.8087
>     Epoch 1071/2000
>     0s - loss: 15919846.6807 - mean_squared_error: 15919846.6807 - val_loss: 16556797.1424 - val_mean_squared_error: 16556797.1424
>     Epoch 1072/2000
>     0s - loss: 15919842.2545 - mean_squared_error: 15919842.2545 - val_loss: 16556808.6960 - val_mean_squared_error: 16556808.6960
>     Epoch 1073/2000
>     0s - loss: 15919836.4392 - mean_squared_error: 15919836.4392 - val_loss: 16556800.5976 - val_mean_squared_error: 16556800.5976
>     Epoch 1074/2000
>     0s - loss: 15919842.0588 - mean_squared_error: 15919842.0588 - val_loss: 16556784.0519 - val_mean_squared_error: 16556784.0519
>     Epoch 1075/2000
>     0s - loss: 15919843.6129 - mean_squared_error: 15919843.6129 - val_loss: 16556791.4345 - val_mean_squared_error: 16556791.4345
>     Epoch 1076/2000
>     0s - loss: 15919842.1316 - mean_squared_error: 15919842.1316 - val_loss: 16556797.5007 - val_mean_squared_error: 16556797.5007
>     Epoch 1077/2000

##### What is different here?

-   We've changed the activation in the hidden layer to "sigmoid" per our discussion.
-   Next, notice that we're running 2000 training epochs!

Even so, it takes a looooong time to converge. If you experiment a lot, you'll find that ... it still takes a long time to converge. Around the early part of the most recent deep learning renaissance, researchers started experimenting with other non-linearities.

(Remember, we're talking about non-linear activations in the hidden layer. The output here is still using "linear" rather than "softmax" because we're performing regression, not classification.)

In theory, any non-linearity should allow learning, and maybe we can use one that "works better"

By "works better" we mean

-   Simpler gradient - faster to compute
-   Less prone to "saturation" -- where the neuron ends up way off in the 0 or 1 territory of the sigmoid and can't easily learn anything
-   Keeps gradients "big" -- avoiding the large, flat, near-zero gradient areas of the sigmoid

Turns out that a big breakthrough and popular solution is a very simple hack:

### Rectified Linear Unit (ReLU)

<img src="http://i.imgur.com/oAYh9DN.png" width=1000>

### Go change your hidden-layer activation from 'sigmoid' to 'relu'

Start your script and watch the error for a bit!

``` python
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
```

>     Using TensorFlow backend.
>     Train on 36409 samples, validate on 4046 samples
>     Epoch 1/2000
>     1s - loss: 30529190.1837 - mean_squared_error: 30529190.1837 - val_loss: 29411915.2308 - val_mean_squared_error: 29411915.2308
>     Epoch 2/2000
>     0s - loss: 24582836.8424 - mean_squared_error: 24582836.8424 - val_loss: 21489554.7059 - val_mean_squared_error: 21489554.7059
>     Epoch 3/2000
>     0s - loss: 17969291.6919 - mean_squared_error: 17969291.6919 - val_loss: 16793069.4123 - val_mean_squared_error: 16793069.4123
>     Epoch 4/2000
>     1s - loss: 15480715.8264 - mean_squared_error: 15480715.8264 - val_loss: 15789220.6599 - val_mean_squared_error: 15789220.6599
>     Epoch 5/2000
>     0s - loss: 15087230.8840 - mean_squared_error: 15087230.8840 - val_loss: 15629582.7598 - val_mean_squared_error: 15629582.7598
>     Epoch 6/2000
>     0s - loss: 14976512.4634 - mean_squared_error: 14976512.4634 - val_loss: 15517250.3969 - val_mean_squared_error: 15517250.3969
>     Epoch 7/2000
>     0s - loss: 14868614.6046 - mean_squared_error: 14868614.6046 - val_loss: 15397380.7054 - val_mean_squared_error: 15397380.7054
>     Epoch 8/2000
>     0s - loss: 14728096.9030 - mean_squared_error: 14728096.9030 - val_loss: 15226399.8082 - val_mean_squared_error: 15226399.8082
>     Epoch 9/2000
>     0s - loss: 14544699.6345 - mean_squared_error: 14544699.6345 - val_loss: 15021725.2996 - val_mean_squared_error: 15021725.2996
>     Epoch 10/2000
>     0s - loss: 14338732.8978 - mean_squared_error: 14338732.8978 - val_loss: 14795451.7776 - val_mean_squared_error: 14795451.7776
>     Epoch 11/2000
>     0s - loss: 14083039.6640 - mean_squared_error: 14083039.6640 - val_loss: 14499474.5907 - val_mean_squared_error: 14499474.5907
>     Epoch 12/2000
>     0s - loss: 13786505.1888 - mean_squared_error: 13786505.1888 - val_loss: 14177226.9422 - val_mean_squared_error: 14177226.9422
>     Epoch 13/2000
>     0s - loss: 13461889.7136 - mean_squared_error: 13461889.7136 - val_loss: 13829338.9822 - val_mean_squared_error: 13829338.9822
>     Epoch 14/2000
>     0s - loss: 13105571.6756 - mean_squared_error: 13105571.6756 - val_loss: 13448455.5764 - val_mean_squared_error: 13448455.5764
>     Epoch 15/2000
>     0s - loss: 12716089.7191 - mean_squared_error: 12716089.7191 - val_loss: 13023137.8398 - val_mean_squared_error: 13023137.8398
>     Epoch 16/2000
>     0s - loss: 12290125.0273 - mean_squared_error: 12290125.0273 - val_loss: 12562233.4394 - val_mean_squared_error: 12562233.4394
>     Epoch 17/2000
>     1s - loss: 11822929.5736 - mean_squared_error: 11822929.5736 - val_loss: 12057014.1997 - val_mean_squared_error: 12057014.1997
>     Epoch 18/2000
>     0s - loss: 11317678.6308 - mean_squared_error: 11317678.6308 - val_loss: 11520225.6560 - val_mean_squared_error: 11520225.6560
>     Epoch 19/2000
>     0s - loss: 10783899.8569 - mean_squared_error: 10783899.8569 - val_loss: 10952517.0124 - val_mean_squared_error: 10952517.0124
>     Epoch 20/2000
>     0s - loss: 10216105.8295 - mean_squared_error: 10216105.8295 - val_loss: 10356410.7963 - val_mean_squared_error: 10356410.7963
>     Epoch 21/2000
>     0s - loss: 9628171.1841 - mean_squared_error: 9628171.1841 - val_loss: 9726680.9822 - val_mean_squared_error: 9726680.9822
>     Epoch 22/2000
>     0s - loss: 9023221.4450 - mean_squared_error: 9023221.4450 - val_loss: 9096048.4340 - val_mean_squared_error: 9096048.4340
>     Epoch 23/2000
>     0s - loss: 8416738.1593 - mean_squared_error: 8416738.1593 - val_loss: 8468370.4330 - val_mean_squared_error: 8468370.4330
>     Epoch 24/2000
>     0s - loss: 7821609.4994 - mean_squared_error: 7821609.4994 - val_loss: 7855861.5640 - val_mean_squared_error: 7855861.5640
>     Epoch 25/2000
>     1s - loss: 7248328.2705 - mean_squared_error: 7248328.2705 - val_loss: 7278495.9419 - val_mean_squared_error: 7278495.9419
>     Epoch 26/2000
>     0s - loss: 6710899.9358 - mean_squared_error: 6710899.9358 - val_loss: 6730064.5484 - val_mean_squared_error: 6730064.5484
>     Epoch 27/2000
>     0s - loss: 6206590.9728 - mean_squared_error: 6206590.9728 - val_loss: 6215042.9160 - val_mean_squared_error: 6215042.9160
>     Epoch 28/2000
>     0s - loss: 5742481.1064 - mean_squared_error: 5742481.1064 - val_loss: 5746139.2269 - val_mean_squared_error: 5746139.2269
>     Epoch 29/2000
>     1s - loss: 5321099.9691 - mean_squared_error: 5321099.9691 - val_loss: 5330252.0373 - val_mean_squared_error: 5330252.0373
>     Epoch 30/2000
>     0s - loss: 4946282.0777 - mean_squared_error: 4946282.0777 - val_loss: 4951074.3423 - val_mean_squared_error: 4951074.3423
>     Epoch 31/2000
>     0s - loss: 4616674.2869 - mean_squared_error: 4616674.2869 - val_loss: 4618500.3978 - val_mean_squared_error: 4618500.3978
>     Epoch 32/2000
>     0s - loss: 4324035.9523 - mean_squared_error: 4324035.9523 - val_loss: 4320027.1683 - val_mean_squared_error: 4320027.1683
>     Epoch 33/2000
>     0s - loss: 4065116.8023 - mean_squared_error: 4065116.8023 - val_loss: 4057282.2952 - val_mean_squared_error: 4057282.2952
>     Epoch 34/2000
>     0s - loss: 3835387.2452 - mean_squared_error: 3835387.2452 - val_loss: 3826257.9152 - val_mean_squared_error: 3826257.9152
>     Epoch 35/2000
>     0s - loss: 3631992.2163 - mean_squared_error: 3631992.2163 - val_loss: 3610414.1173 - val_mean_squared_error: 3610414.1173
>     Epoch 36/2000
>     0s - loss: 3448402.6372 - mean_squared_error: 3448402.6372 - val_loss: 3418763.9795 - val_mean_squared_error: 3418763.9795
>     Epoch 37/2000
>     0s - loss: 3278971.0243 - mean_squared_error: 3278971.0243 - val_loss: 3237593.1367 - val_mean_squared_error: 3237593.1367
>     Epoch 38/2000
>     0s - loss: 3128815.8008 - mean_squared_error: 3128815.8008 - val_loss: 3080639.8597 - val_mean_squared_error: 3080639.8597
>     Epoch 39/2000
>     0s - loss: 2992299.2418 - mean_squared_error: 2992299.2418 - val_loss: 2930155.3342 - val_mean_squared_error: 2930155.3342
>     Epoch 40/2000
>     0s - loss: 2867563.9562 - mean_squared_error: 2867563.9562 - val_loss: 2800795.3187 - val_mean_squared_error: 2800795.3187
>     Epoch 41/2000
>     0s - loss: 2753425.1681 - mean_squared_error: 2753425.1681 - val_loss: 2676988.8693 - val_mean_squared_error: 2676988.8693
>     Epoch 42/2000
>     0s - loss: 2646957.5431 - mean_squared_error: 2646957.5431 - val_loss: 2560941.8130 - val_mean_squared_error: 2560941.8130
>     Epoch 43/2000
>     0s - loss: 2549308.6264 - mean_squared_error: 2549308.6264 - val_loss: 2456306.3591 - val_mean_squared_error: 2456306.3591
>     Epoch 44/2000
>     0s - loss: 2458692.7544 - mean_squared_error: 2458692.7544 - val_loss: 2357977.7906 - val_mean_squared_error: 2357977.7906
>     Epoch 45/2000
>     0s - loss: 2375298.8709 - mean_squared_error: 2375298.8709 - val_loss: 2264709.3060 - val_mean_squared_error: 2264709.3060
>     Epoch 46/2000
>     0s - loss: 2298140.5592 - mean_squared_error: 2298140.5592 - val_loss: 2179303.2582 - val_mean_squared_error: 2179303.2582
>     Epoch 47/2000
>     0s - loss: 2226112.5426 - mean_squared_error: 2226112.5426 - val_loss: 2098594.1712 - val_mean_squared_error: 2098594.1712
>     Epoch 48/2000
>     0s - loss: 2158821.3397 - mean_squared_error: 2158821.3397 - val_loss: 2021928.8887 - val_mean_squared_error: 2021928.8887
>     Epoch 49/2000
>     0s - loss: 2096003.2155 - mean_squared_error: 2096003.2155 - val_loss: 1963024.3772 - val_mean_squared_error: 1963024.3772
>     Epoch 50/2000
>     0s - loss: 2037802.3591 - mean_squared_error: 2037802.3591 - val_loss: 1887597.8419 - val_mean_squared_error: 1887597.8419
>     Epoch 51/2000
>     0s - loss: 1983289.4418 - mean_squared_error: 1983289.4418 - val_loss: 1828190.8424 - val_mean_squared_error: 1828190.8424
>     Epoch 52/2000
>     0s - loss: 1931221.8944 - mean_squared_error: 1931221.8944 - val_loss: 1771145.0791 - val_mean_squared_error: 1771145.0791
>     Epoch 53/2000
>     0s - loss: 1883715.2154 - mean_squared_error: 1883715.2154 - val_loss: 1718235.4591 - val_mean_squared_error: 1718235.4591
>     Epoch 54/2000
>     0s - loss: 1839273.1685 - mean_squared_error: 1839273.1685 - val_loss: 1671121.5743 - val_mean_squared_error: 1671121.5743
>     Epoch 55/2000
>     0s - loss: 1797173.2485 - mean_squared_error: 1797173.2485 - val_loss: 1621913.9522 - val_mean_squared_error: 1621913.9522
>     Epoch 56/2000
>     0s - loss: 1758594.5511 - mean_squared_error: 1758594.5511 - val_loss: 1578523.4256 - val_mean_squared_error: 1578523.4256
>     Epoch 57/2000
>     0s - loss: 1721428.3284 - mean_squared_error: 1721428.3284 - val_loss: 1538331.7044 - val_mean_squared_error: 1538331.7044
>     Epoch 58/2000
>     1s - loss: 1685960.0558 - mean_squared_error: 1685960.0558 - val_loss: 1496513.7130 - val_mean_squared_error: 1496513.7130
>     Epoch 59/2000
>     0s - loss: 1652609.1315 - mean_squared_error: 1652609.1315 - val_loss: 1460389.2633 - val_mean_squared_error: 1460389.2633
>     Epoch 60/2000
>     0s - loss: 1621241.8911 - mean_squared_error: 1621241.8911 - val_loss: 1425904.8545 - val_mean_squared_error: 1425904.8545
>     Epoch 61/2000
>     0s - loss: 1591281.8122 - mean_squared_error: 1591281.8122 - val_loss: 1390023.3080 - val_mean_squared_error: 1390023.3080
>     Epoch 62/2000
>     1s - loss: 1562332.0949 - mean_squared_error: 1562332.0949 - val_loss: 1358044.1967 - val_mean_squared_error: 1358044.1967
>     Epoch 63/2000
>     0s - loss: 1535279.0655 - mean_squared_error: 1535279.0655 - val_loss: 1333719.2630 - val_mean_squared_error: 1333719.2630
>     Epoch 64/2000
>     0s - loss: 1508471.2922 - mean_squared_error: 1508471.2922 - val_loss: 1299607.5494 - val_mean_squared_error: 1299607.5494
>     Epoch 65/2000
>     0s - loss: 1484107.5204 - mean_squared_error: 1484107.5204 - val_loss: 1270379.5545 - val_mean_squared_error: 1270379.5545
>     Epoch 66/2000
>     0s - loss: 1460236.2575 - mean_squared_error: 1460236.2575 - val_loss: 1250263.4871 - val_mean_squared_error: 1250263.4871
>     Epoch 67/2000
>     0s - loss: 1438408.6046 - mean_squared_error: 1438408.6046 - val_loss: 1220801.2046 - val_mean_squared_error: 1220801.2046
>     Epoch 68/2000
>     0s - loss: 1418556.1192 - mean_squared_error: 1418556.1192 - val_loss: 1200866.2105 - val_mean_squared_error: 1200866.2105
>     Epoch 69/2000
>     0s - loss: 1398928.1152 - mean_squared_error: 1398928.1152 - val_loss: 1184490.8020 - val_mean_squared_error: 1184490.8020
>     Epoch 70/2000
>     0s - loss: 1378654.7419 - mean_squared_error: 1378654.7419 - val_loss: 1166939.1661 - val_mean_squared_error: 1166939.1661
>     Epoch 71/2000
>     0s - loss: 1362087.0257 - mean_squared_error: 1362087.0257 - val_loss: 1140878.3833 - val_mean_squared_error: 1140878.3833
>     Epoch 72/2000
>     0s - loss: 1345053.9771 - mean_squared_error: 1345053.9771 - val_loss: 1117493.9098 - val_mean_squared_error: 1117493.9098
>     Epoch 73/2000
>     0s - loss: 1329383.8424 - mean_squared_error: 1329383.8424 - val_loss: 1100296.0289 - val_mean_squared_error: 1100296.0289
>     Epoch 74/2000
>     0s - loss: 1314769.3430 - mean_squared_error: 1314769.3430 - val_loss: 1090675.2377 - val_mean_squared_error: 1090675.2377
>     Epoch 75/2000
>     0s - loss: 1300116.6763 - mean_squared_error: 1300116.6763 - val_loss: 1070801.2416 - val_mean_squared_error: 1070801.2416
>     Epoch 76/2000
>     0s - loss: 1286312.1837 - mean_squared_error: 1286312.1837 - val_loss: 1053875.6109 - val_mean_squared_error: 1053875.6109
>     Epoch 77/2000
>     0s - loss: 1273160.2179 - mean_squared_error: 1273160.2179 - val_loss: 1037938.0667 - val_mean_squared_error: 1037938.0667
>     Epoch 78/2000
>     0s - loss: 1261378.3027 - mean_squared_error: 1261378.3027 - val_loss: 1024285.4398 - val_mean_squared_error: 1024285.4398
>     Epoch 79/2000
>     0s - loss: 1248579.9294 - mean_squared_error: 1248579.9294 - val_loss: 1012859.5022 - val_mean_squared_error: 1012859.5022
>     Epoch 80/2000
>     0s - loss: 1238060.1704 - mean_squared_error: 1238060.1704 - val_loss: 999940.8028 - val_mean_squared_error: 999940.8028
>     Epoch 81/2000
>     0s - loss: 1226710.3310 - mean_squared_error: 1226710.3310 - val_loss: 997011.4790 - val_mean_squared_error: 997011.4790
>     Epoch 82/2000
>     0s - loss: 1216503.9830 - mean_squared_error: 1216503.9830 - val_loss: 979560.1810 - val_mean_squared_error: 979560.1810
>     Epoch 83/2000
>     0s - loss: 1207466.0823 - mean_squared_error: 1207466.0823 - val_loss: 967885.1399 - val_mean_squared_error: 967885.1399
>     Epoch 84/2000
>     0s - loss: 1197912.0215 - mean_squared_error: 1197912.0215 - val_loss: 960060.7527 - val_mean_squared_error: 960060.7527
>     Epoch 85/2000
>     1s - loss: 1188698.5784 - mean_squared_error: 1188698.5784 - val_loss: 946013.3750 - val_mean_squared_error: 946013.3750
>     Epoch 86/2000
>     0s - loss: 1180624.7435 - mean_squared_error: 1180624.7435 - val_loss: 936205.9997 - val_mean_squared_error: 936205.9997
>     Epoch 87/2000
>     0s - loss: 1172187.9912 - mean_squared_error: 1172187.9912 - val_loss: 926906.3160 - val_mean_squared_error: 926906.3160
>     Epoch 88/2000
>     0s - loss: 1164308.6471 - mean_squared_error: 1164308.6471 - val_loss: 917909.9834 - val_mean_squared_error: 917909.9834
>     Epoch 89/2000
>     0s - loss: 1156432.0374 - mean_squared_error: 1156432.0374 - val_loss: 916962.4348 - val_mean_squared_error: 916962.4348
>     Epoch 90/2000
>     0s - loss: 1149588.5920 - mean_squared_error: 1149588.5920 - val_loss: 902020.6448 - val_mean_squared_error: 902020.6448
>     Epoch 91/2000
>     0s - loss: 1141697.6069 - mean_squared_error: 1141697.6069 - val_loss: 899969.7941 - val_mean_squared_error: 899969.7941
>     Epoch 92/2000
>     0s - loss: 1135376.9402 - mean_squared_error: 1135376.9402 - val_loss: 886258.1089 - val_mean_squared_error: 886258.1089
>     Epoch 93/2000
>     0s - loss: 1128707.3022 - mean_squared_error: 1128707.3022 - val_loss: 879147.4453 - val_mean_squared_error: 879147.4453
>     Epoch 94/2000
>     0s - loss: 1122919.1345 - mean_squared_error: 1122919.1345 - val_loss: 872178.3099 - val_mean_squared_error: 872178.3099
>     Epoch 95/2000
>     0s - loss: 1116093.4843 - mean_squared_error: 1116093.4843 - val_loss: 867554.2241 - val_mean_squared_error: 867554.2241
>     Epoch 96/2000
>     0s - loss: 1110064.9030 - mean_squared_error: 1110064.9030 - val_loss: 860917.7314 - val_mean_squared_error: 860917.7314
>     Epoch 97/2000
>     0s - loss: 1104758.6350 - mean_squared_error: 1104758.6350 - val_loss: 852799.7853 - val_mean_squared_error: 852799.7853
>     Epoch 98/2000
>     0s - loss: 1099425.5895 - mean_squared_error: 1099425.5895 - val_loss: 856817.3051 - val_mean_squared_error: 856817.3051
>     Epoch 99/2000
>     0s - loss: 1093828.6883 - mean_squared_error: 1093828.6883 - val_loss: 841480.3101 - val_mean_squared_error: 841480.3101
>     Epoch 100/2000
>     0s - loss: 1088180.9810 - mean_squared_error: 1088180.9810 - val_loss: 834687.8909 - val_mean_squared_error: 834687.8909
>     Epoch 101/2000
>     0s - loss: 1083653.7610 - mean_squared_error: 1083653.7610 - val_loss: 833769.2733 - val_mean_squared_error: 833769.2733
>     Epoch 102/2000
>     0s - loss: 1078172.9719 - mean_squared_error: 1078172.9719 - val_loss: 830076.4956 - val_mean_squared_error: 830076.4956
>     Epoch 103/2000
>     0s - loss: 1074056.6871 - mean_squared_error: 1074056.6871 - val_loss: 824557.3151 - val_mean_squared_error: 824557.3151
>     Epoch 104/2000
>     0s - loss: 1069408.4214 - mean_squared_error: 1069408.4214 - val_loss: 813986.4431 - val_mean_squared_error: 813986.4431
>     Epoch 105/2000
>     0s - loss: 1064633.5701 - mean_squared_error: 1064633.5701 - val_loss: 810625.9422 - val_mean_squared_error: 810625.9422
>     Epoch 106/2000
>     0s - loss: 1060451.4492 - mean_squared_error: 1060451.4492 - val_loss: 807411.5637 - val_mean_squared_error: 807411.5637
>     Epoch 107/2000
>     0s - loss: 1055070.0121 - mean_squared_error: 1055070.0121 - val_loss: 800269.3722 - val_mean_squared_error: 800269.3722
>     Epoch 108/2000
>     0s - loss: 1051289.7218 - mean_squared_error: 1051289.7218 - val_loss: 796192.5473 - val_mean_squared_error: 796192.5473
>     Epoch 109/2000
>     0s - loss: 1047134.9486 - mean_squared_error: 1047134.9486 - val_loss: 796099.9666 - val_mean_squared_error: 796099.9666
>     Epoch 110/2000
>     0s - loss: 1043713.0358 - mean_squared_error: 1043713.0358 - val_loss: 786266.5566 - val_mean_squared_error: 786266.5566
>     Epoch 111/2000
>     0s - loss: 1040135.1789 - mean_squared_error: 1040135.1789 - val_loss: 783085.7636 - val_mean_squared_error: 783085.7636
>     Epoch 112/2000
>     0s - loss: 1035284.3818 - mean_squared_error: 1035284.3818 - val_loss: 777880.1878 - val_mean_squared_error: 777880.1878
>     Epoch 113/2000
>     0s - loss: 1032071.1882 - mean_squared_error: 1032071.1882 - val_loss: 776901.0828 - val_mean_squared_error: 776901.0828
>     Epoch 114/2000
>     0s - loss: 1028314.1820 - mean_squared_error: 1028314.1820 - val_loss: 770272.8482 - val_mean_squared_error: 770272.8482
>     Epoch 115/2000
>     0s - loss: 1024605.2201 - mean_squared_error: 1024605.2201 - val_loss: 771550.4985 - val_mean_squared_error: 771550.4985
>     Epoch 116/2000
>     0s - loss: 1020956.9224 - mean_squared_error: 1020956.9224 - val_loss: 771078.2058 - val_mean_squared_error: 771078.2058
>     Epoch 117/2000
>     0s - loss: 1017373.2101 - mean_squared_error: 1017373.2101 - val_loss: 759206.0218 - val_mean_squared_error: 759206.0218
>     Epoch 118/2000
>     0s - loss: 1013974.9141 - mean_squared_error: 1013974.9141 - val_loss: 766833.1636 - val_mean_squared_error: 766833.1636
>     Epoch 119/2000
>     0s - loss: 1011226.6436 - mean_squared_error: 1011226.6436 - val_loss: 754195.5361 - val_mean_squared_error: 754195.5361
>     Epoch 120/2000
>     0s - loss: 1007590.5986 - mean_squared_error: 1007590.5986 - val_loss: 749585.2978 - val_mean_squared_error: 749585.2978
>     Epoch 121/2000
>     0s - loss: 1004742.8472 - mean_squared_error: 1004742.8472 - val_loss: 746638.3034 - val_mean_squared_error: 746638.3034
>     Epoch 122/2000
>     0s - loss: 1002097.2227 - mean_squared_error: 1002097.2227 - val_loss: 749246.3739 - val_mean_squared_error: 749246.3739
>     Epoch 123/2000
>     0s - loss: 997468.4410 - mean_squared_error: 997468.4410 - val_loss: 739173.0716 - val_mean_squared_error: 739173.0716
>     Epoch 124/2000
>     0s - loss: 995960.0903 - mean_squared_error: 995960.0903 - val_loss: 738538.3551 - val_mean_squared_error: 738538.3551
>     Epoch 125/2000
>     0s - loss: 992647.0254 - mean_squared_error: 992647.0254 - val_loss: 733149.6153 - val_mean_squared_error: 733149.6153
>     Epoch 126/2000
>     0s - loss: 989512.9631 - mean_squared_error: 989512.9631 - val_loss: 730371.7172 - val_mean_squared_error: 730371.7172
>     Epoch 127/2000
>     1s - loss: 987292.5769 - mean_squared_error: 987292.5769 - val_loss: 727789.4879 - val_mean_squared_error: 727789.4879
>     Epoch 128/2000
>     0s - loss: 983908.5191 - mean_squared_error: 983908.5191 - val_loss: 725163.6574 - val_mean_squared_error: 725163.6574
>     Epoch 129/2000
>     1s - loss: 980688.8651 - mean_squared_error: 980688.8651 - val_loss: 735055.0499 - val_mean_squared_error: 735055.0499
>     Epoch 130/2000
>     1s - loss: 977543.5753 - mean_squared_error: 977543.5753 - val_loss: 718021.9451 - val_mean_squared_error: 718021.9451
>     Epoch 131/2000
>     0s - loss: 975527.8766 - mean_squared_error: 975527.8766 - val_loss: 716360.9895 - val_mean_squared_error: 716360.9895
>     Epoch 132/2000
>     0s - loss: 971568.3896 - mean_squared_error: 971568.3896 - val_loss: 713884.6918 - val_mean_squared_error: 713884.6918
>     Epoch 133/2000
>     0s - loss: 969530.3081 - mean_squared_error: 969530.3081 - val_loss: 711160.6284 - val_mean_squared_error: 711160.6284
>     Epoch 134/2000
>     0s - loss: 965742.8541 - mean_squared_error: 965742.8541 - val_loss: 712647.2580 - val_mean_squared_error: 712647.2580
>     Epoch 135/2000
>     0s - loss: 963445.1666 - mean_squared_error: 963445.1666 - val_loss: 706814.0032 - val_mean_squared_error: 706814.0032
>     Epoch 136/2000
>     0s - loss: 961161.5273 - mean_squared_error: 961161.5273 - val_loss: 702164.6338 - val_mean_squared_error: 702164.6338
>     Epoch 137/2000
>     0s - loss: 958634.9046 - mean_squared_error: 958634.9046 - val_loss: 702586.3550 - val_mean_squared_error: 702586.3550
>     Epoch 138/2000
>     0s - loss: 956490.0375 - mean_squared_error: 956490.0375 - val_loss: 699241.8098 - val_mean_squared_error: 699241.8098
>     Epoch 139/2000
>     0s - loss: 953741.2099 - mean_squared_error: 953741.2099 - val_loss: 703639.7293 - val_mean_squared_error: 703639.7293
>     Epoch 140/2000
>     0s - loss: 951655.7896 - mean_squared_error: 951655.7896 - val_loss: 694373.7603 - val_mean_squared_error: 694373.7603
>     Epoch 141/2000
>     0s - loss: 949928.6329 - mean_squared_error: 949928.6329 - val_loss: 694936.6254 - val_mean_squared_error: 694936.6254
>     Epoch 142/2000
>     0s - loss: 947291.9678 - mean_squared_error: 947291.9678 - val_loss: 688758.1210 - val_mean_squared_error: 688758.1210
>     Epoch 143/2000
>     0s - loss: 945469.0256 - mean_squared_error: 945469.0256 - val_loss: 690589.6686 - val_mean_squared_error: 690589.6686
>     Epoch 144/2000
>     0s - loss: 942491.7948 - mean_squared_error: 942491.7948 - val_loss: 687723.9039 - val_mean_squared_error: 687723.9039
>     Epoch 145/2000
>     0s - loss: 940229.0547 - mean_squared_error: 940229.0547 - val_loss: 686608.0982 - val_mean_squared_error: 686608.0982
>     Epoch 146/2000
>     0s - loss: 938193.0882 - mean_squared_error: 938193.0882 - val_loss: 681033.8401 - val_mean_squared_error: 681033.8401
>     Epoch 147/2000
>     0s - loss: 935622.3159 - mean_squared_error: 935622.3159 - val_loss: 679763.9750 - val_mean_squared_error: 679763.9750
>     Epoch 148/2000
>     0s - loss: 933567.2888 - mean_squared_error: 933567.2888 - val_loss: 678661.4871 - val_mean_squared_error: 678661.4871
>     Epoch 149/2000
>     0s - loss: 931623.2877 - mean_squared_error: 931623.2877 - val_loss: 677185.6384 - val_mean_squared_error: 677185.6384
>     Epoch 150/2000
>     0s - loss: 929549.4450 - mean_squared_error: 929549.4450 - val_loss: 676078.5424 - val_mean_squared_error: 676078.5424
>     Epoch 151/2000
>     0s - loss: 927668.9164 - mean_squared_error: 927668.9164 - val_loss: 673022.8576 - val_mean_squared_error: 673022.8576
>     Epoch 152/2000
>     0s - loss: 925660.3101 - mean_squared_error: 925660.3101 - val_loss: 672886.8021 - val_mean_squared_error: 672886.8021
>     Epoch 153/2000
>     0s - loss: 924200.3007 - mean_squared_error: 924200.3007 - val_loss: 668558.8158 - val_mean_squared_error: 668558.8158
>     Epoch 154/2000
>     0s - loss: 921692.7262 - mean_squared_error: 921692.7262 - val_loss: 668513.5398 - val_mean_squared_error: 668513.5398
>     Epoch 155/2000
>     0s - loss: 920183.4840 - mean_squared_error: 920183.4840 - val_loss: 667841.3166 - val_mean_squared_error: 667841.3166
>     Epoch 156/2000
>     0s - loss: 917614.8720 - mean_squared_error: 917614.8720 - val_loss: 663273.4322 - val_mean_squared_error: 663273.4322
>     Epoch 157/2000
>     1s - loss: 916482.8381 - mean_squared_error: 916482.8381 - val_loss: 661577.9392 - val_mean_squared_error: 661577.9392
>     Epoch 158/2000
>     0s - loss: 913825.6307 - mean_squared_error: 913825.6307 - val_loss: 664099.3511 - val_mean_squared_error: 664099.3511
>     Epoch 159/2000
>     0s - loss: 912301.3019 - mean_squared_error: 912301.3019 - val_loss: 664097.2451 - val_mean_squared_error: 664097.2451
>     Epoch 160/2000
>     0s - loss: 910514.9805 - mean_squared_error: 910514.9805 - val_loss: 656311.2097 - val_mean_squared_error: 656311.2097
>     Epoch 161/2000
>     0s - loss: 908366.1180 - mean_squared_error: 908366.1180 - val_loss: 656586.4699 - val_mean_squared_error: 656586.4699
>     Epoch 162/2000
>     1s - loss: 906743.4299 - mean_squared_error: 906743.4299 - val_loss: 653436.7270 - val_mean_squared_error: 653436.7270
>     Epoch 163/2000
>     0s - loss: 905602.9347 - mean_squared_error: 905602.9347 - val_loss: 662333.1428 - val_mean_squared_error: 662333.1428
>     Epoch 164/2000
>     0s - loss: 903235.5695 - mean_squared_error: 903235.5695 - val_loss: 651544.3030 - val_mean_squared_error: 651544.3030
>     Epoch 165/2000
>     0s - loss: 900958.8944 - mean_squared_error: 900958.8944 - val_loss: 650214.4074 - val_mean_squared_error: 650214.4074
>     Epoch 166/2000
>     0s - loss: 899465.3712 - mean_squared_error: 899465.3712 - val_loss: 647452.0863 - val_mean_squared_error: 647452.0863
>     Epoch 167/2000
>     0s - loss: 898382.2543 - mean_squared_error: 898382.2543 - val_loss: 646790.7838 - val_mean_squared_error: 646790.7838
>     Epoch 168/2000
>     0s - loss: 896566.1781 - mean_squared_error: 896566.1781 - val_loss: 646370.3659 - val_mean_squared_error: 646370.3659
>     Epoch 169/2000
>     0s - loss: 894956.3169 - mean_squared_error: 894956.3169 - val_loss: 644248.0793 - val_mean_squared_error: 644248.0793
>     Epoch 170/2000
>     0s - loss: 893158.0623 - mean_squared_error: 893158.0623 - val_loss: 646306.1230 - val_mean_squared_error: 646306.1230
>     Epoch 171/2000
>     0s - loss: 891239.2930 - mean_squared_error: 891239.2930 - val_loss: 651033.8639 - val_mean_squared_error: 651033.8639
>     Epoch 172/2000
>     0s - loss: 890068.6909 - mean_squared_error: 890068.6909 - val_loss: 642275.0055 - val_mean_squared_error: 642275.0055
>     Epoch 173/2000
>     0s - loss: 888330.6315 - mean_squared_error: 888330.6315 - val_loss: 641747.6650 - val_mean_squared_error: 641747.6650
>     Epoch 174/2000
>     0s - loss: 886662.8453 - mean_squared_error: 886662.8453 - val_loss: 638039.8361 - val_mean_squared_error: 638039.8361
>     Epoch 175/2000
>     0s - loss: 884820.4993 - mean_squared_error: 884820.4993 - val_loss: 641601.1479 - val_mean_squared_error: 641601.1479
>     Epoch 176/2000
>     0s - loss: 883375.4455 - mean_squared_error: 883375.4455 - val_loss: 639074.1240 - val_mean_squared_error: 639074.1240
>     Epoch 177/2000
>     0s - loss: 881906.0859 - mean_squared_error: 881906.0859 - val_loss: 643688.5414 - val_mean_squared_error: 643688.5414
>     Epoch 178/2000
>     0s - loss: 880833.4184 - mean_squared_error: 880833.4184 - val_loss: 632596.0440 - val_mean_squared_error: 632596.0440
>     Epoch 179/2000
>     0s - loss: 879098.4255 - mean_squared_error: 879098.4255 - val_loss: 632726.8424 - val_mean_squared_error: 632726.8424
>     Epoch 180/2000
>     0s - loss: 877094.4152 - mean_squared_error: 877094.4152 - val_loss: 634957.6820 - val_mean_squared_error: 634957.6820
>     Epoch 181/2000
>     0s - loss: 875975.1167 - mean_squared_error: 875975.1167 - val_loss: 630530.7901 - val_mean_squared_error: 630530.7901
>     Epoch 182/2000
>     1s - loss: 874776.2910 - mean_squared_error: 874776.2910 - val_loss: 634374.8014 - val_mean_squared_error: 634374.8014
>     Epoch 183/2000
>     0s - loss: 873214.6594 - mean_squared_error: 873214.6594 - val_loss: 630436.6543 - val_mean_squared_error: 630436.6543
>     Epoch 184/2000
>     0s - loss: 871836.4334 - mean_squared_error: 871836.4334 - val_loss: 628659.0967 - val_mean_squared_error: 628659.0967
>
>     *** WARNING: skipped 115555 bytes of output ***
>
>     Epoch 1047/2000
>     0s - loss: 448708.3529 - mean_squared_error: 448708.3529 - val_loss: 397645.0453 - val_mean_squared_error: 397645.0453
>     Epoch 1048/2000
>     0s - loss: 448652.3875 - mean_squared_error: 448652.3875 - val_loss: 396915.6958 - val_mean_squared_error: 396915.6958
>     Epoch 1049/2000
>     0s - loss: 448358.5447 - mean_squared_error: 448358.5447 - val_loss: 397988.7590 - val_mean_squared_error: 397988.7590
>     Epoch 1050/2000
>     0s - loss: 448535.7140 - mean_squared_error: 448535.7140 - val_loss: 396797.4276 - val_mean_squared_error: 396797.4276
>     Epoch 1051/2000
>     0s - loss: 448058.2459 - mean_squared_error: 448058.2459 - val_loss: 398679.2582 - val_mean_squared_error: 398679.2582
>     Epoch 1052/2000
>     0s - loss: 447735.4888 - mean_squared_error: 447735.4888 - val_loss: 397448.0886 - val_mean_squared_error: 397448.0886
>     Epoch 1053/2000
>     1s - loss: 447248.8092 - mean_squared_error: 447248.8092 - val_loss: 403053.7328 - val_mean_squared_error: 403053.7328
>     Epoch 1054/2000
>     1s - loss: 447586.7259 - mean_squared_error: 447586.7259 - val_loss: 395991.0390 - val_mean_squared_error: 395991.0390
>     Epoch 1055/2000
>     0s - loss: 447705.1360 - mean_squared_error: 447705.1360 - val_loss: 396497.7584 - val_mean_squared_error: 396497.7584
>     Epoch 1056/2000
>     1s - loss: 447411.7250 - mean_squared_error: 447411.7250 - val_loss: 396778.1184 - val_mean_squared_error: 396778.1184
>     Epoch 1057/2000
>     0s - loss: 446958.8182 - mean_squared_error: 446958.8182 - val_loss: 396826.3279 - val_mean_squared_error: 396826.3279
>     Epoch 1058/2000
>     1s - loss: 447230.5651 - mean_squared_error: 447230.5651 - val_loss: 397424.1702 - val_mean_squared_error: 397424.1702
>     Epoch 1059/2000
>     0s - loss: 447019.8281 - mean_squared_error: 447019.8281 - val_loss: 395306.9387 - val_mean_squared_error: 395306.9387
>     Epoch 1060/2000
>     0s - loss: 446900.1380 - mean_squared_error: 446900.1380 - val_loss: 395692.5757 - val_mean_squared_error: 395692.5757
>     Epoch 1061/2000
>     1s - loss: 447349.5432 - mean_squared_error: 447349.5432 - val_loss: 402183.6305 - val_mean_squared_error: 402183.6305
>     Epoch 1062/2000
>     1s - loss: 446254.1720 - mean_squared_error: 446254.1720 - val_loss: 394893.1460 - val_mean_squared_error: 394893.1460
>     Epoch 1063/2000
>     1s - loss: 446608.9821 - mean_squared_error: 446608.9821 - val_loss: 396786.9203 - val_mean_squared_error: 396786.9203
>     Epoch 1064/2000
>     1s - loss: 446455.8434 - mean_squared_error: 446455.8434 - val_loss: 399541.4323 - val_mean_squared_error: 399541.4323
>     Epoch 1065/2000
>     1s - loss: 447147.4507 - mean_squared_error: 447147.4507 - val_loss: 395458.4744 - val_mean_squared_error: 395458.4744
>     Epoch 1066/2000
>     1s - loss: 446795.7664 - mean_squared_error: 446795.7664 - val_loss: 395006.8381 - val_mean_squared_error: 395006.8381
>     Epoch 1067/2000
>     0s - loss: 446345.1756 - mean_squared_error: 446345.1756 - val_loss: 396308.3275 - val_mean_squared_error: 396308.3275
>     Epoch 1068/2000
>     0s - loss: 445868.4012 - mean_squared_error: 445868.4012 - val_loss: 395240.9528 - val_mean_squared_error: 395240.9528
>     Epoch 1069/2000
>     0s - loss: 445790.4272 - mean_squared_error: 445790.4272 - val_loss: 394586.7619 - val_mean_squared_error: 394586.7619
>     Epoch 1070/2000
>     0s - loss: 445739.7976 - mean_squared_error: 445739.7976 - val_loss: 395176.6928 - val_mean_squared_error: 395176.6928
>     Epoch 1071/2000
>     0s - loss: 445262.6913 - mean_squared_error: 445262.6913 - val_loss: 394327.9859 - val_mean_squared_error: 394327.9859
>     Epoch 1072/2000
>     0s - loss: 445450.9844 - mean_squared_error: 445450.9844 - val_loss: 395853.7713 - val_mean_squared_error: 395853.7713
>     Epoch 1073/2000
>     0s - loss: 445338.9577 - mean_squared_error: 445338.9577 - val_loss: 394593.0756 - val_mean_squared_error: 394593.0756
>     Epoch 1074/2000
>     0s - loss: 445240.5588 - mean_squared_error: 445240.5588 - val_loss: 393931.9080 - val_mean_squared_error: 393931.9080
>     Epoch 1075/2000
>     0s - loss: 445193.3046 - mean_squared_error: 445193.3046 - val_loss: 393631.0668 - val_mean_squared_error: 393631.0668
>     Epoch 1076/2000
>     0s - loss: 444830.9233 - mean_squared_error: 444830.9233 - val_loss: 394164.0835 - val_mean_squared_error: 394164.0835
>     Epoch 1077/2000
>     0s - loss: 444557.3856 - mean_squared_error: 444557.3856 - val_loss: 393404.8981 - val_mean_squared_error: 393404.8981
>     Epoch 1078/2000
>     1s - loss: 444561.7360 - mean_squared_error: 444561.7360 - val_loss: 393408.9289 - val_mean_squared_error: 393408.9289
>     Epoch 1079/2000
>     0s - loss: 444369.6785 - mean_squared_error: 444369.6785 - val_loss: 393054.8646 - val_mean_squared_error: 393054.8646
>     Epoch 1080/2000
>     0s - loss: 444425.7589 - mean_squared_error: 444425.7589 - val_loss: 393396.9378 - val_mean_squared_error: 393396.9378
>     Epoch 1081/2000
>     0s - loss: 444301.5718 - mean_squared_error: 444301.5718 - val_loss: 393274.7883 - val_mean_squared_error: 393274.7883
>     Epoch 1082/2000
>     1s - loss: 444539.4252 - mean_squared_error: 444539.4252 - val_loss: 397069.3084 - val_mean_squared_error: 397069.3084
>     Epoch 1083/2000
>     0s - loss: 443841.7423 - mean_squared_error: 443841.7423 - val_loss: 392859.8594 - val_mean_squared_error: 392859.8594
>     Epoch 1084/2000
>     0s - loss: 443779.4353 - mean_squared_error: 443779.4353 - val_loss: 396164.9526 - val_mean_squared_error: 396164.9526
>     Epoch 1085/2000
>     1s - loss: 443560.6041 - mean_squared_error: 443560.6041 - val_loss: 392088.7061 - val_mean_squared_error: 392088.7061
>     Epoch 1086/2000
>     1s - loss: 443960.7723 - mean_squared_error: 443960.7723 - val_loss: 393519.0889 - val_mean_squared_error: 393519.0889
>     Epoch 1087/2000
>     1s - loss: 443726.8111 - mean_squared_error: 443726.8111 - val_loss: 392717.2577 - val_mean_squared_error: 392717.2577
>     Epoch 1088/2000
>     1s - loss: 443617.2078 - mean_squared_error: 443617.2078 - val_loss: 392414.0285 - val_mean_squared_error: 392414.0285
>     Epoch 1089/2000
>     1s - loss: 443093.9233 - mean_squared_error: 443093.9233 - val_loss: 402321.6948 - val_mean_squared_error: 402321.6948
>     Epoch 1090/2000
>     1s - loss: 443199.6452 - mean_squared_error: 443199.6452 - val_loss: 392822.6412 - val_mean_squared_error: 392822.6412
>     Epoch 1091/2000
>     1s - loss: 442961.5309 - mean_squared_error: 442961.5309 - val_loss: 391883.6868 - val_mean_squared_error: 391883.6868
>     Epoch 1092/2000
>     1s - loss: 443444.7158 - mean_squared_error: 443444.7158 - val_loss: 393683.5238 - val_mean_squared_error: 393683.5238
>     Epoch 1093/2000
>     1s - loss: 442817.7570 - mean_squared_error: 442817.7570 - val_loss: 393126.3681 - val_mean_squared_error: 393126.3681
>     Epoch 1094/2000
>     0s - loss: 442674.1041 - mean_squared_error: 442674.1041 - val_loss: 390995.2020 - val_mean_squared_error: 390995.2020
>     Epoch 1095/2000
>     0s - loss: 442782.8278 - mean_squared_error: 442782.8278 - val_loss: 392588.0330 - val_mean_squared_error: 392588.0330
>     Epoch 1096/2000
>     0s - loss: 442385.4127 - mean_squared_error: 442385.4127 - val_loss: 395269.3843 - val_mean_squared_error: 395269.3843
>     Epoch 1097/2000
>     0s - loss: 442846.8037 - mean_squared_error: 442846.8037 - val_loss: 393009.0379 - val_mean_squared_error: 393009.0379
>     Epoch 1098/2000
>     0s - loss: 441851.1748 - mean_squared_error: 441851.1748 - val_loss: 392899.0389 - val_mean_squared_error: 392899.0389
>     Epoch 1099/2000
>     0s - loss: 442607.4454 - mean_squared_error: 442607.4454 - val_loss: 391012.6635 - val_mean_squared_error: 391012.6635
>     Epoch 1100/2000
>     0s - loss: 442076.4401 - mean_squared_error: 442076.4401 - val_loss: 391271.0962 - val_mean_squared_error: 391271.0962
>     Epoch 1101/2000
>     0s - loss: 441868.9521 - mean_squared_error: 441868.9521 - val_loss: 393007.5933 - val_mean_squared_error: 393007.5933
>     Epoch 1102/2000
>     1s - loss: 441931.7717 - mean_squared_error: 441931.7717 - val_loss: 390933.2237 - val_mean_squared_error: 390933.2237
>     Epoch 1103/2000
>     1s - loss: 441602.6107 - mean_squared_error: 441602.6107 - val_loss: 391186.6329 - val_mean_squared_error: 391186.6329
>     Epoch 1104/2000
>     1s - loss: 441446.6056 - mean_squared_error: 441446.6056 - val_loss: 390107.8527 - val_mean_squared_error: 390107.8527
>     Epoch 1105/2000
>     1s - loss: 441775.3775 - mean_squared_error: 441775.3775 - val_loss: 396201.1136 - val_mean_squared_error: 396201.1136
>     Epoch 1106/2000
>     1s - loss: 441653.1059 - mean_squared_error: 441653.1059 - val_loss: 392071.5088 - val_mean_squared_error: 392071.5088
>     Epoch 1107/2000
>     1s - loss: 440968.7031 - mean_squared_error: 440968.7031 - val_loss: 392281.6282 - val_mean_squared_error: 392281.6282
>     Epoch 1108/2000
>     1s - loss: 441376.6249 - mean_squared_error: 441376.6249 - val_loss: 392275.8186 - val_mean_squared_error: 392275.8186
>     Epoch 1109/2000
>     1s - loss: 441048.1939 - mean_squared_error: 441048.1939 - val_loss: 390648.3780 - val_mean_squared_error: 390648.3780
>     Epoch 1110/2000
>     1s - loss: 440788.2728 - mean_squared_error: 440788.2728 - val_loss: 390637.6083 - val_mean_squared_error: 390637.6083
>     Epoch 1111/2000
>     1s - loss: 440653.0642 - mean_squared_error: 440653.0642 - val_loss: 389649.3571 - val_mean_squared_error: 389649.3571
>     Epoch 1112/2000
>     1s - loss: 440529.0107 - mean_squared_error: 440529.0107 - val_loss: 389533.8862 - val_mean_squared_error: 389533.8862
>     Epoch 1113/2000
>     1s - loss: 440836.0217 - mean_squared_error: 440836.0217 - val_loss: 391972.8724 - val_mean_squared_error: 391972.8724
>     Epoch 1114/2000
>     0s - loss: 440456.8698 - mean_squared_error: 440456.8698 - val_loss: 396324.8853 - val_mean_squared_error: 396324.8853
>     Epoch 1115/2000
>     1s - loss: 440344.1358 - mean_squared_error: 440344.1358 - val_loss: 391274.6101 - val_mean_squared_error: 391274.6101
>     Epoch 1116/2000
>     1s - loss: 440090.4299 - mean_squared_error: 440090.4299 - val_loss: 390682.2831 - val_mean_squared_error: 390682.2831
>     Epoch 1117/2000
>     0s - loss: 440212.5611 - mean_squared_error: 440212.5611 - val_loss: 388789.9295 - val_mean_squared_error: 388789.9295
>     Epoch 1118/2000
>     0s - loss: 439929.9561 - mean_squared_error: 439929.9561 - val_loss: 391158.7254 - val_mean_squared_error: 391158.7254
>     Epoch 1119/2000
>     0s - loss: 439984.9590 - mean_squared_error: 439984.9590 - val_loss: 390692.1666 - val_mean_squared_error: 390692.1666
>     Epoch 1120/2000
>     0s - loss: 440065.6956 - mean_squared_error: 440065.6956 - val_loss: 389659.0448 - val_mean_squared_error: 389659.0448
>     Epoch 1121/2000
>     0s - loss: 439657.8993 - mean_squared_error: 439657.8993 - val_loss: 391316.8566 - val_mean_squared_error: 391316.8566
>     Epoch 1122/2000
>     0s - loss: 439551.1748 - mean_squared_error: 439551.1748 - val_loss: 389237.6913 - val_mean_squared_error: 389237.6913
>     Epoch 1123/2000
>     1s - loss: 439547.9181 - mean_squared_error: 439547.9181 - val_loss: 389347.7998 - val_mean_squared_error: 389347.7998
>     Epoch 1124/2000
>     1s - loss: 439263.5763 - mean_squared_error: 439263.5763 - val_loss: 388459.8378 - val_mean_squared_error: 388459.8378
>     Epoch 1125/2000
>     1s - loss: 439983.8800 - mean_squared_error: 439983.8800 - val_loss: 388955.9193 - val_mean_squared_error: 388955.9193
>     Epoch 1126/2000
>     1s - loss: 439125.9408 - mean_squared_error: 439125.9408 - val_loss: 388215.7945 - val_mean_squared_error: 388215.7945
>     Epoch 1127/2000
>     0s - loss: 439072.6660 - mean_squared_error: 439072.6660 - val_loss: 389275.5239 - val_mean_squared_error: 389275.5239
>     Epoch 1128/2000
>     0s - loss: 439082.2742 - mean_squared_error: 439082.2742 - val_loss: 387741.0192 - val_mean_squared_error: 387741.0192
>     Epoch 1129/2000
>     1s - loss: 438992.1808 - mean_squared_error: 438992.1808 - val_loss: 389508.4793 - val_mean_squared_error: 389508.4793
>     Epoch 1130/2000
>     1s - loss: 438974.8968 - mean_squared_error: 438974.8968 - val_loss: 387553.9646 - val_mean_squared_error: 387553.9646
>     Epoch 1131/2000
>     1s - loss: 438772.9384 - mean_squared_error: 438772.9384 - val_loss: 387966.5390 - val_mean_squared_error: 387966.5390
>     Epoch 1132/2000
>     1s - loss: 438831.1522 - mean_squared_error: 438831.1522 - val_loss: 389404.8679 - val_mean_squared_error: 389404.8679
>     Epoch 1133/2000
>     1s - loss: 438894.3682 - mean_squared_error: 438894.3682 - val_loss: 387397.3828 - val_mean_squared_error: 387397.3828
>     Epoch 1134/2000
>     1s - loss: 438445.0020 - mean_squared_error: 438445.0020 - val_loss: 387149.7674 - val_mean_squared_error: 387149.7674
>     Epoch 1135/2000
>     1s - loss: 438367.9397 - mean_squared_error: 438367.9397 - val_loss: 387795.0138 - val_mean_squared_error: 387795.0138
>     Epoch 1136/2000
>     1s - loss: 437941.0239 - mean_squared_error: 437941.0239 - val_loss: 387905.1852 - val_mean_squared_error: 387905.1852
>     Epoch 1137/2000
>     0s - loss: 438017.2339 - mean_squared_error: 438017.2339 - val_loss: 387781.1100 - val_mean_squared_error: 387781.1100
>     Epoch 1138/2000
>     0s - loss: 437910.8881 - mean_squared_error: 437910.8881 - val_loss: 387629.3436 - val_mean_squared_error: 387629.3436
>     Epoch 1139/2000
>     0s - loss: 437879.1242 - mean_squared_error: 437879.1242 - val_loss: 386389.7122 - val_mean_squared_error: 386389.7122
>     Epoch 1140/2000
>     0s - loss: 437574.5218 - mean_squared_error: 437574.5218 - val_loss: 387512.7269 - val_mean_squared_error: 387512.7269
>     Epoch 1141/2000
>     0s - loss: 437616.7370 - mean_squared_error: 437616.7370 - val_loss: 386818.0406 - val_mean_squared_error: 386818.0406
>     Epoch 1142/2000
>     0s - loss: 437783.4627 - mean_squared_error: 437783.4627 - val_loss: 386352.4637 - val_mean_squared_error: 386352.4637
>     Epoch 1143/2000
>     0s - loss: 437651.1712 - mean_squared_error: 437651.1712 - val_loss: 387425.4666 - val_mean_squared_error: 387425.4666
>     Epoch 1144/2000
>     0s - loss: 437145.4606 - mean_squared_error: 437145.4606 - val_loss: 387901.2564 - val_mean_squared_error: 387901.2564
>     Epoch 1145/2000
>     1s - loss: 436830.8879 - mean_squared_error: 436830.8879 - val_loss: 390186.2310 - val_mean_squared_error: 390186.2310
>     Epoch 1146/2000
>     1s - loss: 437135.0890 - mean_squared_error: 437135.0890 - val_loss: 386384.2033 - val_mean_squared_error: 386384.2033
>     Epoch 1147/2000
>     1s - loss: 437533.3403 - mean_squared_error: 437533.3403 - val_loss: 386164.6373 - val_mean_squared_error: 386164.6373
>     Epoch 1148/2000
>     1s - loss: 437170.8061 - mean_squared_error: 437170.8061 - val_loss: 386217.6672 - val_mean_squared_error: 386217.6672
>     Epoch 1149/2000
>     1s - loss: 437050.6990 - mean_squared_error: 437050.6990 - val_loss: 386005.1633 - val_mean_squared_error: 386005.1633
>     Epoch 1150/2000
>     1s - loss: 436594.1112 - mean_squared_error: 436594.1112 - val_loss: 391340.8186 - val_mean_squared_error: 391340.8186
>     Epoch 1151/2000
>     1s - loss: 436613.8019 - mean_squared_error: 436613.8019 - val_loss: 386404.7569 - val_mean_squared_error: 386404.7569
>     Epoch 1152/2000
>     1s - loss: 436568.6422 - mean_squared_error: 436568.6422 - val_loss: 387760.0207 - val_mean_squared_error: 387760.0207
>     Epoch 1153/2000
>     0s - loss: 436510.0653 - mean_squared_error: 436510.0653 - val_loss: 385655.7977 - val_mean_squared_error: 385655.7977
>     Epoch 1154/2000
>     1s - loss: 436768.4509 - mean_squared_error: 436768.4509 - val_loss: 385625.8906 - val_mean_squared_error: 385625.8906
>     Epoch 1155/2000
>     1s - loss: 436526.5243 - mean_squared_error: 436526.5243 - val_loss: 386081.8106 - val_mean_squared_error: 386081.8106
>     Epoch 1156/2000
>     0s - loss: 435821.3603 - mean_squared_error: 435821.3603 - val_loss: 385642.9504 - val_mean_squared_error: 385642.9504
>     Epoch 1157/2000
>     1s - loss: 435698.3500 - mean_squared_error: 435698.3500 - val_loss: 385461.5550 - val_mean_squared_error: 385461.5550
>     Epoch 1158/2000
>     1s - loss: 435794.6638 - mean_squared_error: 435794.6638 - val_loss: 385255.9271 - val_mean_squared_error: 385255.9271
>     Epoch 1159/2000
>     0s - loss: 435669.8028 - mean_squared_error: 435669.8028 - val_loss: 384671.0114 - val_mean_squared_error: 384671.0114
>     Epoch 1160/2000
>     0s - loss: 435292.4434 - mean_squared_error: 435292.4434 - val_loss: 386095.7478 - val_mean_squared_error: 386095.7478
>     Epoch 1161/2000
>     0s - loss: 435484.2003 - mean_squared_error: 435484.2003 - val_loss: 386889.4218 - val_mean_squared_error: 386889.4218
>     Epoch 1162/2000
>     0s - loss: 435567.9129 - mean_squared_error: 435567.9129 - val_loss: 384818.3488 - val_mean_squared_error: 384818.3488
>     Epoch 1163/2000
>     0s - loss: 435865.1201 - mean_squared_error: 435865.1201 - val_loss: 387790.1154 - val_mean_squared_error: 387790.1154
>     Epoch 1164/2000
>     0s - loss: 435277.1939 - mean_squared_error: 435277.1939 - val_loss: 384297.1397 - val_mean_squared_error: 384297.1397
>     Epoch 1165/2000
>     0s - loss: 435107.3448 - mean_squared_error: 435107.3448 - val_loss: 387203.8749 - val_mean_squared_error: 387203.8749
>     Epoch 1166/2000
>     0s - loss: 434977.0513 - mean_squared_error: 434977.0513 - val_loss: 390491.1996 - val_mean_squared_error: 390491.1996
>     Epoch 1167/2000
>     1s - loss: 435116.3452 - mean_squared_error: 435116.3452 - val_loss: 385385.2107 - val_mean_squared_error: 385385.2107
>     Epoch 1168/2000
>     1s - loss: 434705.3953 - mean_squared_error: 434705.3953 - val_loss: 384249.3274 - val_mean_squared_error: 384249.3274
>     Epoch 1169/2000
>     1s - loss: 434935.9614 - mean_squared_error: 434935.9614 - val_loss: 384184.4113 - val_mean_squared_error: 384184.4113
>     Epoch 1170/2000
>     1s - loss: 434813.4597 - mean_squared_error: 434813.4597 - val_loss: 384637.9316 - val_mean_squared_error: 384637.9316
>     Epoch 1171/2000
>     1s - loss: 434363.7566 - mean_squared_error: 434363.7566 - val_loss: 383653.7414 - val_mean_squared_error: 383653.7414
>     Epoch 1172/2000
>     1s - loss: 434230.3009 - mean_squared_error: 434230.3009 - val_loss: 386624.2528 - val_mean_squared_error: 386624.2528
>     Epoch 1173/2000
>     1s - loss: 434291.1799 - mean_squared_error: 434291.1799 - val_loss: 383607.8880 - val_mean_squared_error: 383607.8880
>     Epoch 1174/2000
>     1s - loss: 434298.5793 - mean_squared_error: 434298.5793 - val_loss: 384439.1947 - val_mean_squared_error: 384439.1947
>     Epoch 1175/2000
>     1s - loss: 433843.1533 - mean_squared_error: 433843.1533 - val_loss: 384054.7458 - val_mean_squared_error: 384054.7458
>     Epoch 1176/2000
>     0s - loss: 433850.5587 - mean_squared_error: 433850.5587 - val_loss: 383713.4054 - val_mean_squared_error: 383713.4054
>     Epoch 1177/2000
>     0s - loss: 434164.7334 - mean_squared_error: 434164.7334 - val_loss: 383214.4588 - val_mean_squared_error: 383214.4588
>     Epoch 1178/2000
>     0s - loss: 433747.3666 - mean_squared_error: 433747.3666 - val_loss: 383898.5678 - val_mean_squared_error: 383898.5678
>     Epoch 1179/2000
>     0s - loss: 434729.3862 - mean_squared_error: 434729.3862 - val_loss: 383793.9898 - val_mean_squared_error: 383793.9898
>     Epoch 1180/2000
>     0s - loss: 433526.9054 - mean_squared_error: 433526.9054 - val_loss: 383169.2398 - val_mean_squared_error: 383169.2398
>     Epoch 1181/2000
>     1s - loss: 433397.1757 - mean_squared_error: 433397.1757 - val_loss: 384128.6558 - val_mean_squared_error: 384128.6558
>     Epoch 1182/2000
>     0s - loss: 433469.3902 - mean_squared_error: 433469.3902 - val_loss: 385585.5622 - val_mean_squared_error: 385585.5622
>     Epoch 1183/2000
>     0s - loss: 433460.7630 - mean_squared_error: 433460.7630 - val_loss: 383098.5334 - val_mean_squared_error: 383098.5334
>     Epoch 1184/2000
>     1s - loss: 433121.1504 - mean_squared_error: 433121.1504 - val_loss: 382921.4841 - val_mean_squared_error: 382921.4841
>     Epoch 1185/2000
>     1s - loss: 433377.0859 - mean_squared_error: 433377.0859 - val_loss: 383111.0821 - val_mean_squared_error: 383111.0821
>     Epoch 1186/2000
>     1s - loss: 432822.6601 - mean_squared_error: 432822.6601 - val_loss: 383402.5540 - val_mean_squared_error: 383402.5540
>     Epoch 1187/2000
>     1s - loss: 433614.2444 - mean_squared_error: 433614.2444 - val_loss: 383482.9750 - val_mean_squared_error: 383482.9750
>     Epoch 1188/2000
>     1s - loss: 433420.5411 - mean_squared_error: 433420.5411 - val_loss: 383253.3268 - val_mean_squared_error: 383253.3268
>     Epoch 1189/2000
>     1s - loss: 432911.2839 - mean_squared_error: 432911.2839 - val_loss: 382438.4638 - val_mean_squared_error: 382438.4638
>     Epoch 1190/2000
>     1s - loss: 432720.5398 - mean_squared_error: 432720.5398 - val_loss: 381967.2059 - val_mean_squared_error: 381967.2059
>     Epoch 1191/2000
>     1s - loss: 433099.6023 - mean_squared_error: 433099.6023 - val_loss: 383783.0852 - val_mean_squared_error: 383783.0852
>     Epoch 1192/2000
>     0s - loss: 432725.7271 - mean_squared_error: 432725.7271 - val_loss: 382395.0691 - val_mean_squared_error: 382395.0691
>     Epoch 1193/2000
>     1s - loss: 432202.1925 - mean_squared_error: 432202.1925 - val_loss: 383619.0545 - val_mean_squared_error: 383619.0545
>     Epoch 1194/2000
>     1s - loss: 432127.7509 - mean_squared_error: 432127.7509 - val_loss: 383975.8978 - val_mean_squared_error: 383975.8978
>     Epoch 1195/2000
>     1s - loss: 432504.1809 - mean_squared_error: 432504.1809 - val_loss: 381755.8165 - val_mean_squared_error: 381755.8165
>     Epoch 1196/2000
>     1s - loss: 432537.4333 - mean_squared_error: 432537.4333 - val_loss: 382009.6391 - val_mean_squared_error: 382009.6391
>     Epoch 1197/2000
>     1s - loss: 432263.6285 - mean_squared_error: 432263.6285 - val_loss: 381474.6103 - val_mean_squared_error: 381474.6103
>     Epoch 1198/2000
>     1s - loss: 432623.2332 - mean_squared_error: 432623.2332 - val_loss: 381895.7500 - val_mean_squared_error: 381895.7500
>     Epoch 1199/2000
>     1s - loss: 432163.5266 - mean_squared_error: 432163.5266 - val_loss: 381796.3819 - val_mean_squared_error: 381796.3819
>     Epoch 1200/2000
>     1s - loss: 432374.6826 - mean_squared_error: 432374.6826 - val_loss: 381536.5950 - val_mean_squared_error: 381536.5950
>     Epoch 1201/2000
>     1s - loss: 432300.5907 - mean_squared_error: 432300.5907 - val_loss: 384779.1769 - val_mean_squared_error: 384779.1769
>     Epoch 1202/2000
>     1s - loss: 432186.0193 - mean_squared_error: 432186.0193 - val_loss: 382155.9532 - val_mean_squared_error: 382155.9532
>     Epoch 1203/2000
>     1s - loss: 431467.0452 - mean_squared_error: 431467.0452 - val_loss: 381244.6170 - val_mean_squared_error: 381244.6170
>     Epoch 1204/2000
>     1s - loss: 431717.6627 - mean_squared_error: 431717.6627 - val_loss: 381075.5456 - val_mean_squared_error: 381075.5456
>     Epoch 1205/2000
>     1s - loss: 432301.8112 - mean_squared_error: 432301.8112 - val_loss: 383143.1678 - val_mean_squared_error: 383143.1678
>     Epoch 1206/2000
>     1s - loss: 431347.5124 - mean_squared_error: 431347.5124 - val_loss: 382015.9607 - val_mean_squared_error: 382015.9607
>     Epoch 1207/2000
>     0s - loss: 431545.9221 - mean_squared_error: 431545.9221 - val_loss: 383877.3698 - val_mean_squared_error: 383877.3698
>     Epoch 1208/2000
>     1s - loss: 430952.9830 - mean_squared_error: 430952.9830 - val_loss: 382286.5638 - val_mean_squared_error: 382286.5638
>     Epoch 1209/2000
>     1s - loss: 430845.6064 - mean_squared_error: 430845.6064 - val_loss: 380968.6288 - val_mean_squared_error: 380968.6288
>     Epoch 1210/2000
>     1s - loss: 430975.3650 - mean_squared_error: 430975.3650 - val_loss: 380553.9917 - val_mean_squared_error: 380553.9917
>     Epoch 1211/2000
>     1s - loss: 430989.3925 - mean_squared_error: 430989.3925 - val_loss: 380514.1260 - val_mean_squared_error: 380514.1260
>     Epoch 1212/2000
>     1s - loss: 430434.8692 - mean_squared_error: 430434.8692 - val_loss: 380112.7956 - val_mean_squared_error: 380112.7956
>     Epoch 1213/2000
>     1s - loss: 431186.3341 - mean_squared_error: 431186.3341 - val_loss: 383426.2781 - val_mean_squared_error: 383426.2781
>     Epoch 1214/2000
>     0s - loss: 431326.3985 - mean_squared_error: 431326.3985 - val_loss: 380398.5271 - val_mean_squared_error: 380398.5271
>     Epoch 1215/2000
>     0s - loss: 430378.1488 - mean_squared_error: 430378.1488 - val_loss: 381898.1101 - val_mean_squared_error: 381898.1101
>     Epoch 1216/2000
>     1s - loss: 430398.9187 - mean_squared_error: 430398.9187 - val_loss: 381926.6079 - val_mean_squared_error: 381926.6079
>     Epoch 1217/2000
>     1s - loss: 430598.7694 - mean_squared_error: 430598.7694 - val_loss: 379854.4487 - val_mean_squared_error: 379854.4487
>     Epoch 1218/2000
>     0s - loss: 430709.4146 - mean_squared_error: 430709.4146 - val_loss: 379604.9649 - val_mean_squared_error: 379604.9649
>     Epoch 1219/2000
>     1s - loss: 430305.9284 - mean_squared_error: 430305.9284 - val_loss: 382192.7405 - val_mean_squared_error: 382192.7405
>     Epoch 1220/2000
>     0s - loss: 430050.0108 - mean_squared_error: 430050.0108 - val_loss: 379713.9453 - val_mean_squared_error: 379713.9453
>     Epoch 1221/2000
>     0s - loss: 430238.9999 - mean_squared_error: 430238.9999 - val_loss: 380064.0118 - val_mean_squared_error: 380064.0118
>     Epoch 1222/2000
>     1s - loss: 430180.3207 - mean_squared_error: 430180.3207 - val_loss: 383675.2101 - val_mean_squared_error: 383675.2101
>     Epoch 1223/2000
>     0s - loss: 429992.6061 - mean_squared_error: 429992.6061 - val_loss: 384709.5035 - val_mean_squared_error: 384709.5035
>     Epoch 1224/2000
>     0s - loss: 429891.5782 - mean_squared_error: 429891.5782 - val_loss: 381848.1791 - val_mean_squared_error: 381848.1791
>     Epoch 1225/2000
>     1s - loss: 429721.5895 - mean_squared_error: 429721.5895 - val_loss: 379407.7591 - val_mean_squared_error: 379407.7591
>     Epoch 1226/2000
>     1s - loss: 429292.8431 - mean_squared_error: 429292.8431 - val_loss: 379616.7978 - val_mean_squared_error: 379616.7978
>     Epoch 1227/2000
>     1s - loss: 429354.0612 - mean_squared_error: 429354.0612 - val_loss: 379303.9550 - val_mean_squared_error: 379303.9550
>     Epoch 1228/2000
>     0s - loss: 429361.4462 - mean_squared_error: 429361.4462 - val_loss: 379753.1811 - val_mean_squared_error: 379753.1811
>     Epoch 1229/2000
>     0s - loss: 429263.3782 - mean_squared_error: 429263.3782 - val_loss: 381404.0883 - val_mean_squared_error: 381404.0883
>     Epoch 1230/2000
>     1s - loss: 429516.5472 - mean_squared_error: 429516.5472 - val_loss: 378945.6341 - val_mean_squared_error: 378945.6341
>     Epoch 1231/2000
>     1s - loss: 429423.9087 - mean_squared_error: 429423.9087 - val_loss: 381089.3098 - val_mean_squared_error: 381089.3098
>     Epoch 1232/2000

Would you look at that?!

-   We break $1000 RMSE around epoch 112
-   $900 around epoch 220
-   $800 around epoch 450
-   By around epoch 2000, my RMSE is &lt; $600

...

**Same theory; different activation function. Huge difference**

Multilayer Networks
===================

If a single-layer perceptron network learns the importance of different combinations of features in the data...

What would another network learn if it had a second (hidden) layer of neurons?

It depends on how we train the network. We'll talk in the next section about how this training works, but the general idea is that we still work backward from the error gradient.

That is, the last layer learns from error in the output; the second-to-last layer learns from error transmitted through that last layer, etc. It's a touch hand-wavy for now, but we'll make it more concrete later.

Given this approach, we can say that:

1.  The second (hidden) layer is learning features composed of activations in the first (hidden) layer
2.  The first (hidden) layer is learning feature weights that enable the second layer to perform best
    -   Why? Earlier, the first hidden layer just learned feature weights because that's how it was judged
    -   Now, the first hidden layer is judged on the error in the second layer, so it learns to contribute to that second layer
3.  The second layer is learning new features that aren't explicit in the data, and is teaching the first layer to supply it with the necessary information to compose these new features

### So instead of just feature weighting and combining, we have new feature learning!

This concept is the foundation of the "Deep Feed-Forward Network"

<img src="http://i.imgur.com/fHGrs4X.png">

------------------------------------------------------------------------

### Let's try it!

**Add a layer to your Keras network, perhaps another 20 neurons, and see how the training goes.**

if you get stuck, there is a solution in the Keras-DFFN notebook

------------------------------------------------------------------------

I'm getting RMSE &lt; $1000 by epoch 35 or so

&lt; $800 by epoch 90

In this configuration, mine makes progress to around 700 epochs or so and then stalls with RMSE around $560

### Our network has "gone meta"

It's now able to exceed where a simple decision tree can go, because it can create new features and then split on those

Congrats! You have built your first deep-learning model!
--------------------------------------------------------

So does that mean we can just keep adding more layers and solve anything?

Well, theoretically maybe ... try reconfiguring your network, watch the training, and see what happens.

<img src="http://i.imgur.com/BumsXgL.jpg" width=500>