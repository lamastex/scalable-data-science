# Databricks notebook source
# MAGIC %md
# MAGIC 
# MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
# MAGIC 
# MAGIC This is used in a non-profit educational setting with kind permission of [Adam Breindel](https://www.linkedin.com/in/adbreind).
# MAGIC This is not licensed by Adam for use in a for-profit setting. Please contact Adam directly at `adbreind@gmail.com` to request or report such use cases or abuses. 
# MAGIC A few minor modifications and additional mathematical statistical pointers have been added by Raazesh Sainudiin when teaching PhD students in Uppsala University.

# COMMAND ----------

# MAGIC %md #### We can also implement the model with mini-batches -- this will let us see matrix ops in action:
# MAGIC 
# MAGIC (N.b., feed_dict is intended for small data / experimentation. For more info on ingesting data at scale, see https://www.tensorflow.org/api_guides/python/reading_data)

# COMMAND ----------

import numpy as np

# COMMAND ----------

# we know these params, but we're making TF learn them

REAL_SLOPE_X1 = 2
REAL_SLOPE_X2 = 3
REAL_INTERCEPT = 5

# COMMAND ----------

# GENERATE a batch of true data, with a little Gaussian noise added

def make_mini_batch(size=10):
  X = np.random.rand(size, 2)
  Y = np.matmul(X, [REAL_SLOPE_X1, REAL_SLOPE_X2]) + REAL_INTERCEPT + 0.2 * np.random.randn(size) 
  return X.reshape(size,2), Y.reshape(size,1)

# COMMAND ----------

make_mini_batch()

# COMMAND ----------

import tensorflow as tf


batch = 5

tf.reset_default_graph()

x = tf.placeholder(tf.float32, shape=(batch, 2))
x_aug = tf.concat( (x, tf.ones((batch, 1))), 1 )

y = tf.placeholder(tf.float32, shape=(batch, 1))
model_params = tf.get_variable("model_params", [3,1])
y_model = tf.matmul(x_aug, model_params)

error = tf.reduce_sum(tf.square(y - y_model))/batch

train_op = tf.train.GradientDescentOptimizer(0.02).minimize(error)

init = tf.global_variables_initializer()

errors = []

with tf.Session() as session:
    session.run(init)    
    for i in range(500):
      x_data, y_data = make_mini_batch(batch) 
      _, error_val = session.run([train_op, error], feed_dict={x: x_data, y: y_data})
      errors.append(error_val)

    out = session.run(model_params)
    print(out)

# COMMAND ----------

import matplotlib.pyplot as plt

fig, ax = plt.subplots()
fig.set_size_inches((4,3))
plt.plot(errors)
display(fig)