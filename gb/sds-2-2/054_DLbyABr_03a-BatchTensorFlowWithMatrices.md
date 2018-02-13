[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind). This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

``` md #### We can also implement the model with mini-batches -- this will let us see matrix ops in action:

(N.b., feed_dict is intended for small data / experimentation. For more info on ingesting data at scale, see https://www.tensorflow.org/api_guides/python/reading_data)
```

``` python
# we know these params, but we're making TF learn them

REAL_SLOPE_X1 = 2 # slope along axis 1 (x-axis)
REAL_SLOPE_X2 = 3 # slope along axis 2 (y-axis)
REAL_INTERCEPT = 5 # intercept along axis 3 (z-axis), think of (x,y,z) axes in the usual way
```

``` python
import numpy as np
# GENERATE a batch of true data, with a little Gaussian noise added

def make_mini_batch(size=10):
  X = np.random.rand(size, 2) # 
  Y = np.matmul(X, [REAL_SLOPE_X1, REAL_SLOPE_X2]) + REAL_INTERCEPT + 0.2 * np.random.randn(size) 
  return X.reshape(size,2), Y.reshape(size,1)
```

To digest what's going on inside the function above, let's take it step by step.

``` python
 Xex = np.random.rand(10, 2) # Xex is simulating PRNGs from independent Uniform [0,1] RVs
 Xex # visualize these as 10 orddered pairs of points in the x-y plane that makes up our x-axis and y-axis (or x1 and x2 axes)
```

>     Out[14]: 
>     array([[ 0.68729439,  0.58462379],
>            [ 0.93698634,  0.42744602],
>            [ 0.08603721,  0.67466094],
>            [ 0.17160026,  0.71457899],
>            [ 0.72199128,  0.72235838],
>            [ 0.58633688,  0.14721157],
>            [ 0.27425931,  0.60181525],
>            [ 0.5962165 ,  0.51132706],
>            [ 0.6869326 ,  0.28285958],
>            [ 0.9634012 ,  0.21305557]])

``` python
Yex = np.matmul(Xex, [REAL_SLOPE_X1, REAL_SLOPE_X2]) # + REAL_INTERCEPT + 0.2 * np.random.randn(size) 
Yex
```

>     Out[15]: 
>     array([ 3.12846017,  3.15631072,  2.19605725,  2.4869375 ,  3.61105769,
>             1.61430847,  2.35396437,  2.72641418,  2.22244393,  2.56596912])

The first entry in Yex is obtained as follows (change the numbers in the produc below if you reevaluated the cells above) and geometrically it is the location in z-axis of the plane with slopes given by REAL\_SLOPE\_X1 in the x-axis and REAL\_SLOPE\_X2 in the y-aixs with intercept 0 at the point in the x-y or x1-x2 plane given by (0.68729439, 0.58462379).

``` python
0.68729439*REAL_SLOPE_X1 +  0.58462379*REAL_SLOPE_X2 
```

>     Out[16]: 3.12846015

The next steps are adding an intercept term to translate the plane in the z-axis and then a scaled (the multiplication by 0.2 here) gaussian noise from independetly drawn pseudo-random samples from the standard normal or Normal(0,1) random variable via `np.random.randn(size)`.

``` python
Yex = np.matmul(Xex, [REAL_SLOPE_X1, REAL_SLOPE_X2]) + REAL_INTERCEPT # + 0.2 * np.random.randn(10) 
Yex
```

>     Out[19]: 
>     array([ 8.12846017,  8.15631072,  7.19605725,  7.4869375 ,  8.61105769,
>             6.61430847,  7.35396437,  7.72641418,  7.22244393,  7.56596912])

``` python
Yex = np.matmul(Xex, [REAL_SLOPE_X1, REAL_SLOPE_X2])  + REAL_INTERCEPT + 0.2 * np.random.randn(10) 
Yex # note how each entry in Yex is jiggled independently a bit by 0.2 * np.random.randn()
```

>     Out[20]: 
>     array([ 8.54725608,  8.21366699,  7.55268465,  7.34539416,  8.36644074,
>             6.42752603,  7.55499568,  7.71093193,  7.32762635,  7.29992345])

Thus we can now fully appreciate what is going on in `make_mini_batch`. This is meant to substitute for pulling random sub-samples of batches of the real data during stochastic gradient descent.

``` python
make_mini_batch() # our mini-batch of Xx and Ys
```

>     Out[21]: 
>     (array([[ 0.76270993,  0.21237438],
>             [ 0.93630169,  0.63585941],
>             [ 0.65131407,  0.62564024],
>             [ 0.50704891,  0.80775216],
>             [ 0.45825114,  0.9387936 ],
>             [ 0.65822646,  0.28507217],
>             [ 0.78834033,  0.43172133],
>             [ 0.85462969,  0.14751868],
>             [ 0.05519411,  0.81125067],
>             [ 0.10222417,  0.09151233]]), array([[ 7.68758883],
>             [ 8.75889251],
>             [ 8.07336849],
>             [ 8.23691867],
>             [ 8.90827379],
>             [ 6.93907587],
>             [ 7.52369029],
>             [ 7.00021765],
>             [ 7.60460715],
>             [ 5.65254595]]))

``` python
import tensorflow as tf


batch = 5 # size of batch

tf.reset_default_graph() # this is important to do before you do something new in TF

# we will work with single floating point precision and this is specified in the tf.float32 type argument to each tf object/method
x = tf.placeholder(tf.float32, shape=(batch, 2)) # placeholder node for the pairs of x variables (predictors) in batches of size batch
x_aug = tf.concat( (x, tf.ones((batch, 1))), 1 ) # x_aug is a concatenation of a vector of 1`s along the first dimension

y = tf.placeholder(tf.float32, shape=(batch, 1)) # placeholder node for the univariate response y with batch many rows and 1 column
model_params = tf.get_variable("model_params", [3,1]) # these are the x1 slope, x2 slope and the intercept (3 rows and 1 column)
y_model = tf.matmul(x_aug, model_params) # our two-factor regression model is defined by this matrix multiplication
# note that the noise is formally part of the model and what we are actually modeling is the mean response...

error = tf.reduce_sum(tf.square(y - y_model))/batch # this is mean square error where the sum is computed by a reduce call on addition

train_op = tf.train.GradientDescentOptimizer(0.02).minimize(error) # learning rate is set to 0.02

init = tf.global_variables_initializer() # our way into running the TF session

errors = [] # list to track errors over iterations

with tf.Session() as session:
    session.run(init)    
    for i in range(500):
      x_data, y_data = make_mini_batch(batch) # simulate the mini-batch of data x1,x2 and response y with noise
      _, error_val = session.run([train_op, error], feed_dict={x: x_data, y: y_data})
      errors.append(error_val)

    out = session.run(model_params)
    print(out)
```

>     [[ 2.37420893]
>      [ 2.94586873]
>      [ 4.8277688 ]]

``` python
REAL_SLOPE_X1, REAL_SLOPE_X2, REAL_INTERCEPT # compare with rue parameter values - it's not too far from the estimates
```

>     Out[23]: (2, 3, 5)

``` python
import matplotlib.pyplot as plt

fig, ax = plt.subplots()
fig.set_size_inches((4,3))
plt.plot(errors)
display(fig)
```

