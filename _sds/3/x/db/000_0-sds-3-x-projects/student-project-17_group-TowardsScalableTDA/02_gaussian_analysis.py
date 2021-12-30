# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC # Gaussian Analysis

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Here we test the results for normally distributed points. 

# COMMAND ----------

import numpy as np
import os
import shutil
import glob
import matplotlib.pyplot as plt
import scipy as sp
import scipy.stats as stats

# COMMAND ----------

os.listdir('/dbfs/FileStore/group17/data/')

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC Reading the files.

# COMMAND ----------

def read_csv(data_name):
  results = glob.glob('/dbfs/FileStore/group17/data/' + data_name + '/*.csv')
  assert(len(results) == 1)
  filepath = results[0]
  
  csv = np.loadtxt(filepath, delimiter=',')
  csv = csv[csv[:, 0].argsort()]
  return csv

# COMMAND ----------

train_data = read_csv('gaussian_train')
test_data = read_csv('gaussian_test')
weights = read_csv('gaussian_weights')

# COMMAND ----------

def display_density(data, weights):
  fig = plt.figure(figsize=(10, 10))
  plt.scatter(data[:, 0], data[:, 1], weights / np.max(weights) * 50)
  display(fig)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC True density visualization.

# COMMAND ----------

true_density = stats.multivariate_normal.pdf(test_data[:, 1:], mean=np.zeros(2))

display_density(test_data[:, 1:], true_density)

# COMMAND ----------

# MAGIC %md
# MAGIC Density, obtained from our method.

# COMMAND ----------

display_density(test_data[:, 1:], weights[:, 1])

# COMMAND ----------

# MAGIC %md
# MAGIC Density, obtained from kernel density estimation with tophat kernel.

# COMMAND ----------

from sklearn.neighbors.kde import KernelDensity
kde = KernelDensity(kernel='tophat', bandwidth=0.13).fit(train_data[:, 1:])
kde_weights = kde.score_samples(test_data[:, 1:])
kde_weights = np.exp(kde_weights)

display_density(test_data[:, 1:], kde_weights)

# COMMAND ----------

# MAGIC %md
# MAGIC Density, obtained from kernel density estimation with gaussian kernel.

# COMMAND ----------

kde = KernelDensity(kernel='gaussian', bandwidth=0.13).fit(train_data[:, 1:])
gauss_weights = kde.score_samples(test_data[:, 1:])
gauss_weights = np.exp(kde_weights)

display_density(test_data[:, 1:], gauss_weights)


# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC A simple computation of the number of inverses.

# COMMAND ----------

def rank_loss(a, b):
  n = a.shape[0]
  assert(n == b.shape[0])
  ans = 0
  for i in range(n):
    for j in range(i + 1, n):
      if (a[i] - a[j]) * (b[i] - b[j]) < 0:
        ans += 1
  return ans

# COMMAND ----------

# MAGIC %md
# MAGIC Comparison of losses. On this one test, we get the smallest loss. 
# MAGIC 
# MAGIC One of the immediate futher works: do a proper statistical comparison, also on different sizes of data.

# COMMAND ----------

rank_loss(weights[:, 1], true_density)

# COMMAND ----------

rank_loss(kde_weights, true_density)

# COMMAND ----------



rank_loss(gauss_weights, true_density)

# COMMAND ----------

