# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)
# MAGIC 
# MAGIC This is a 2019-2021 augmentation and update of [Adam Breindel](https://www.linkedin.com/in/adbreind)'s initial notebooks.
# MAGIC 
# MAGIC _Thanks to [Christian von Koch](https://www.linkedin.com/in/christianvonkoch/) and [William Anzén](https://www.linkedin.com/in/william-anz%C3%A9n-b52003199/) for their contributions towards making these materials Spark 3.0.1 and Python 3+ compliant._

# COMMAND ----------

# MAGIC %md 
# MAGIC #### We can also implement the model with mini-batches -- this will let us see matrix ops in action:
# MAGIC 
# MAGIC (N.b., feed_dict is intended for small data / experimentation. For more info on ingesting data at scale, see https://www.tensorflow.org/api_guides/python/reading_data)

# COMMAND ----------

# we know these params, but we're making TF learn them

REAL_SLOPE_X1 = 2 # slope along axis 1 (x-axis)
REAL_SLOPE_X2 = 3 # slope along axis 2 (y-axis)
REAL_INTERCEPT = 5 # intercept along axis 3 (z-axis), think of (x,y,z) axes in the usual way

# COMMAND ----------

import numpy as np
# GENERATE a batch of true data, with a little Gaussian noise added

def make_mini_batch(size=10):
  X = np.random.rand(size, 2) # 
  Y = np.matmul(X, [REAL_SLOPE_X1, REAL_SLOPE_X2]) + REAL_INTERCEPT + 0.2 * np.random.randn(size) 
  return X.reshape(size,2), Y.reshape(size,1)

# COMMAND ----------

# MAGIC %md
# MAGIC To digest what's going on inside the function above, let's take it step by step.

# COMMAND ----------

 Xex = np.random.rand(10, 2) # Xex is simulating PRNGs from independent Uniform [0,1] RVs
 Xex # visualize these as 10 orddered pairs of points in the x-y plane that makes up our x-axis and y-axis (or x1 and x2 axes)

# COMMAND ----------

Yex = np.matmul(Xex, [REAL_SLOPE_X1, REAL_SLOPE_X2]) #+ REAL_INTERCEPT #+ 0.2 * np.random.randn(10) 
Yex

# COMMAND ----------

# MAGIC %md
# MAGIC The first entry in Yex is obtained as follows (change the numbers in the produc below if you reevaluated the cells above) and geometrically it is the location in z-axis of the plane with slopes given by REAL_SLOPE_X1 in the x-axis and REAL_SLOPE_X2 in the y-aixs with intercept 0 at the point in the x-y or x1-x2 plane given by (0.68729439,  0.58462379).

# COMMAND ----------

0.21757443*REAL_SLOPE_X1 +  0.01815727*REAL_SLOPE_X2 

# COMMAND ----------

# MAGIC %md
# MAGIC The next steps are adding an intercept term to translate the plane in the z-axis and then a scaled (the multiplication by 0.2 here) gaussian noise from independetly drawn pseudo-random samples from the standard normal or Normal(0,1) random variable via `np.random.randn(size)`.

# COMMAND ----------

Yex = np.matmul(Xex, [REAL_SLOPE_X1, REAL_SLOPE_X2]) + REAL_INTERCEPT # + 0.2 * np.random.randn(10) 
Yex

# COMMAND ----------

Yex = np.matmul(Xex, [REAL_SLOPE_X1, REAL_SLOPE_X2])  + REAL_INTERCEPT + 0.2 * np.random.randn(10) 
Yex # note how each entry in Yex is jiggled independently a bit by 0.2 * np.random.randn()

# COMMAND ----------

# MAGIC %md
# MAGIC Thus we can now fully appreciate what is going on in `make_mini_batch`. This is meant to substitute for pulling random sub-samples of batches of the real data during stochastic gradient descent.

# COMMAND ----------

make_mini_batch() # our mini-batch of Xx and Ys

# COMMAND ----------

import tensorflow as tf


batch = 10 # size of batch

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
    for i in range(1000):
      x_data, y_data = make_mini_batch(batch) # simulate the mini-batch of data x1,x2 and response y with noise
      _, error_val = session.run([train_op, error], feed_dict={x: x_data, y: y_data})
      errors.append(error_val)

    out = session.run(model_params)
    print(out)

# COMMAND ----------

REAL_SLOPE_X1, REAL_SLOPE_X2, REAL_INTERCEPT # compare with rue parameter values - it's not too far from the estimates

# COMMAND ----------

import matplotlib.pyplot as plt

fig, ax = plt.subplots()
fig.set_size_inches((4,3))
plt.plot(errors)
display(fig)

# COMMAND ----------

