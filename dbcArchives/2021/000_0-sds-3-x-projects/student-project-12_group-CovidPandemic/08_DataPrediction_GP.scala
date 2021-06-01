// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md # Prediction with time series model - Gaussian Processes
// MAGIC This notebook contains time series prediction with gaussian processes. The data used for prediction is new cases (smoothed) and new deaths (smoothed) for both an aggregated number of countries in the world and for Sweden. To implement the gaussian process model, the python package Gpytorch is used.

// COMMAND ----------

// MAGIC %md ## 1. Install, import, load and preprocess data
// MAGIC Install, import and execute the other relevant notebooks here to load and preprocess data...

// COMMAND ----------

// MAGIC %python
// MAGIC pip install gpytorch

// COMMAND ----------

// MAGIC %python
// MAGIC # python imports
// MAGIC import gpytorch as gpth
// MAGIC import torch as th
// MAGIC import matplotlib.pyplot as plt
// MAGIC import numpy as np

// COMMAND ----------

// MAGIC %run "./02_DataPreprocess"

// COMMAND ----------

// MAGIC %md ## 2. Additional data preprocessing in Scala

// COMMAND ----------

// MAGIC %md ### 2.1 World data preprocessing

// COMMAND ----------

// define dataframe summing up the new cases smoothed for each date
val df_ncworld = df_cleaned_time_series.groupBy("date").sum("new_cases_smoothed").sort(col("date")).withColumnRenamed("sum(new_cases_smoothed)","new_cases_smoothed")
display(df_ncworld)

// COMMAND ----------

// define dataframe summing up the new deaths smoothed for each date
val df_ndworld = df_cleaned_time_series.groupBy("date").sum("new_deaths_smoothed").sort(col("date")).withColumnRenamed("sum(new_deaths_smoothed)","new_deaths_smoothed")
display(df_ndworld)

// COMMAND ----------

// Add a time index for the date
import org.apache.spark.sql.expressions.Window
val window_spec  = Window.orderBy($"date")

val df_ncworld_indexed = df_ncworld.withColumn("time_idx",row_number.over(window_spec))
val df_ndworld_indexed = df_ndworld.withColumn("time_idx",row_number.over(window_spec))
display(df_ncworld_indexed)

// COMMAND ----------

// Get max and min of time index
import org.apache.spark.sql.functions.{min, max}
import org.apache.spark.sql.Row

val id_maxmin = df_ncworld_indexed.agg(max("time_idx"), min("time_idx")).head()
val id_max: Int = id_maxmin.getInt(0)
val id_min: Int = id_maxmin.getInt(1)

// COMMAND ----------

// MAGIC %md Extract a window for prediction

// COMMAND ----------

// define training and test data intervalls. test data is set to 10% of the total dataset time length.
val test_wnd: Int = (0.1*id_max).toInt
val train_wnd: Int = (0.9*id_max).toInt

val df_ncworld_train = df_ncworld_indexed.where($"time_idx" > id_max-train_wnd-test_wnd && $"time_idx" <= id_max-test_wnd)
val df_ncworld_test = df_ncworld_indexed.where($"time_idx" > id_max-test_wnd && $"time_idx" <= id_max)
val df_ndworld_train = df_ndworld_indexed.where($"time_idx" > id_max-train_wnd-test_wnd && $"time_idx" <= id_max-test_wnd)
val df_ndworld_test = df_ndworld_indexed.where($"time_idx" > id_max-test_wnd && $"time_idx" <= id_max)
display(df_ncworld_test)

// COMMAND ----------

// MAGIC %md Convert to python for further processing

// COMMAND ----------

df_ncworld_train.createOrReplaceTempView("df_ncworld_train")
df_ncworld_test.createOrReplaceTempView("df_ncworld_test")
df_ndworld_train.createOrReplaceTempView("df_ndworld_train")
df_ndworld_test.createOrReplaceTempView("df_ndworld_test")

// COMMAND ----------

// MAGIC %python 
// MAGIC df_ncworld_train = spark.table("df_ncworld_train")
// MAGIC df_ncworld_test = spark.table("df_ncworld_test")
// MAGIC df_ndworld_train = spark.table("df_ndworld_train")
// MAGIC df_ndworld_test = spark.table("df_ndworld_test")

// COMMAND ----------

// MAGIC %md ### 2.2 Sweden preprocessing

// COMMAND ----------

val df_ncdenswe = df_cleaned_time_series.select($"location", $"date", $"new_cases_smoothed").where(expr("location = 'Sweden' or location = 'Denmark'"))
val df_nddenswe = df_cleaned_time_series.select($"location", $"date", $"new_deaths_smoothed").where(expr("location = 'Sweden' or location = 'Denmark'"))

// COMMAND ----------

// Add a time index for the date
import org.apache.spark.sql.expressions.Window
val window_spec  = Window.partitionBy("location").orderBy($"date")

val df_ncdenswe_indexed = df_ncdenswe.withColumn("time_idx",row_number.over(window_spec))
display(df_ncdenswe_indexed)

val df_nddenswe_indexed = df_nddenswe.withColumn("time_idx",row_number.over(window_spec))
display(df_nddenswe_indexed)

// COMMAND ----------

val test_wnd: Int = (0.1*id_max).toInt
val train_wnd: Int = (0.9*id_max).toInt
val df_ncdenswe_train = df_ncdenswe_indexed.where($"time_idx" > id_max-train_wnd-test_wnd && $"time_idx" <= id_max-test_wnd)
val df_ncdenswe_test = df_ncdenswe_indexed.where($"time_idx" > id_max-test_wnd && $"time_idx" <= id_max)
val df_nddenswe_train = df_nddenswe_indexed.where($"time_idx" > id_max-train_wnd-test_wnd && $"time_idx" <= id_max-test_wnd)
val df_nddenswe_test = df_nddenswe_indexed.where($"time_idx" > id_max-test_wnd && $"time_idx" <= id_max)
display(df_ncdenswe_test)

// COMMAND ----------

df_ncdenswe_train.createOrReplaceTempView("df_ncdenswe_train")
df_ncdenswe_test.createOrReplaceTempView("df_ncdenswe_test")
df_nddenswe_train.createOrReplaceTempView("df_nddenswe_train")
df_nddenswe_test.createOrReplaceTempView("df_nddenswe_test")

// COMMAND ----------

// MAGIC %python 
// MAGIC df_ncdenswe_train = spark.table("df_ncdenswe_train")
// MAGIC df_ncdenswe_test = spark.table("df_ncdenswe_test")
// MAGIC df_nddenswe_train = spark.table("df_nddenswe_train")
// MAGIC df_nddenswe_test = spark.table("df_nddenswe_test")

// COMMAND ----------

// MAGIC %md ## 3. Time series prediction with Gaussian Processes
// MAGIC In this section we perform predictions based on the input data. Some additional preprocessing in Python is done as well. The transition from Scala to Python is motivated by the use of the python package Gpytorch for implementing the gaussian process model.

// COMMAND ----------

// MAGIC %md ### 3.1 World multistep prediction
// MAGIC As similar operations are performed for processing data, a class is first defined to enable code reuse

// COMMAND ----------

// MAGIC %python
// MAGIC from pyspark.sql.functions import col
// MAGIC import matplotlib.pyplot as plt
// MAGIC 
// MAGIC class GPDataSet():
// MAGIC   def __init__(self, df_train, df_test, datacol, filterloc = None, add_input = None):
// MAGIC     """
// MAGIC       class for processing input data to GP. As similar code is reused, this class enables some code reuse.
// MAGIC       
// MAGIC       param: 'df_train', training data dataframe
// MAGIC       param: 'df_test', test data dataframe
// MAGIC       param: 'datacol', data column in dataframe to perform predictions on, e.g. 'new_cases_smoothed'
// MAGIC       param: 'filterloc', location column in dataframe to perform predictions on, e.g. 'Sweden'
// MAGIC       param: 'add_input', additional location column in dataframe to use as input for predictions, e.g. 'Denmark'  
// MAGIC     """
// MAGIC     self.df_train = df_train
// MAGIC     self.df_test = df_test
// MAGIC     self.datacol = datacol
// MAGIC     self.filterloc = filterloc
// MAGIC     self.add_input = add_input
// MAGIC     self.num_xdim = None    
// MAGIC 
// MAGIC         
// MAGIC   def convert_to_numpy(self):
// MAGIC     """
// MAGIC       convert dataframe to numpy arrays. This process may takes a while.
// MAGIC     """
// MAGIC     # if no filter for location is specified
// MAGIC     if self.filterloc is None:
// MAGIC       x_train_np = np.array(self.df_train.orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
// MAGIC       x_test_np = np.array(self.df_test.orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
// MAGIC       y_train_np = np.array(self.df_train.orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())    
// MAGIC       y_test_np = np.array(self.df_test.orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())    
// MAGIC       num_xdim = 1      
// MAGIC       
// MAGIC     # if a filter for location is specified
// MAGIC     else:
// MAGIC       if self.add_input is None:
// MAGIC         x_train_np = np.array(self.df_train.filter(col("location") == self.filterloc).orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
// MAGIC         x_test_np = np.array(self.df_test.filter(col("location") == self.filterloc).orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
// MAGIC         num_xdim = 1        
// MAGIC      
// MAGIC       # if prediction should add additional input from e.g. a neighbouring country
// MAGIC       else: 
// MAGIC         x_train_time = np.array(self.df_train.filter(col("location") == self.filterloc).orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
// MAGIC         x_test_time = np.array(self.df_test.filter(col("location") == self.filterloc).orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())       
// MAGIC         x_train_add = np.array(self.df_train.filter(col("location") == self.add_input).orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())
// MAGIC         x_test_add = np.array(self.df_test.filter(col("location") == self.add_input).orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())    
// MAGIC         x_train = np.stack((x_train_time, x_train_add), axis=0)
// MAGIC         x_test = np.stack((x_test_time, x_test_add), axis=0)
// MAGIC         x_train_np = np.moveaxis(x_train, 1, 0)
// MAGIC         x_test_np = np.moveaxis(x_test, 1, 0)
// MAGIC         num_xdim = 2
// MAGIC                  
// MAGIC       # output data 
// MAGIC       y_train_np = np.array(self.df_train.filter(col("location") == self.filterloc).orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())
// MAGIC       y_test_np = np.array(self.df_test.filter(col("location") == self.filterloc).orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())
// MAGIC       
// MAGIC     self.x_train_np = x_train_np
// MAGIC     self.x_test_np = x_test_np
// MAGIC     self.y_train_np = y_train_np
// MAGIC     self.y_test_np = y_test_np
// MAGIC     self.num_xdim = num_xdim
// MAGIC       
// MAGIC   def plot_numpy_data(self):
// MAGIC     """ 
// MAGIC       plot numpy arrays 
// MAGIC     """   
// MAGIC     if self.num_xdim == 2:
// MAGIC       fig, (ax1, ax2) = plt.subplots(1,2, figsize=(12,6))
// MAGIC       ax1.plot(self.x_train_np[:,0], self.y_train_np, 'k*')
// MAGIC       ax1.legend(['train data'])
// MAGIC       ax1.set_xlabel('time [days]')
// MAGIC       ax1.set_ylabel('output')
// MAGIC       ax1.set_title('training data')
// MAGIC       ax1.grid()   
// MAGIC       ax2.plot(self.x_train_np[:,0], self.x_train_np[:,1], 'k*')
// MAGIC       ax2.legend(['train data'])
// MAGIC       ax2.set_xlabel('time [days]')
// MAGIC       ax2.set_ylabel('additional input')
// MAGIC       ax2.set_title('training data')
// MAGIC       ax2.grid()         
// MAGIC     else:
// MAGIC       fig, ax = plt.subplots(1,1, figsize=(12,6))
// MAGIC       ax.plot(self.x_train_np, self.y_train_np, 'k*')
// MAGIC       ax.legend(['train data'])
// MAGIC       ax.set_xlabel('time [days]')
// MAGIC       ax.set_ylabel('output')
// MAGIC       ax.set_title('training data')
// MAGIC       ax.grid()      
// MAGIC         
// MAGIC   def get_train_length(self):
// MAGIC       if self.num_xdim == 2:
// MAGIC         return len(self.x_train_np[:,0])
// MAGIC       else:
// MAGIC         return len(self.x_train_np)
// MAGIC 
// MAGIC   def process_numpy_data(self, nth_subsample = 4, window_red = 0.8):
// MAGIC     """
// MAGIC       reduction of data by subsampling data and reducing length of data window. 
// MAGIC     """
// MAGIC     assert window_red > 0 and window_red <= 1, "please adjust 'window_red' parameter to be between 0 and 1"
// MAGIC     start_idx = int((self.get_train_length())*window_red)
// MAGIC     self.x_train = th.tensor(self.x_train_np[start_idx::nth_subsample], dtype=th.float)
// MAGIC     self.x_test = th.tensor(self.x_test_np, dtype=th.float)
// MAGIC     self.y_train = th.tensor(self.y_train_np[start_idx::nth_subsample], dtype=th.float)
// MAGIC     self.y_test = th.tensor(self.y_test_np, dtype=th.float)    
// MAGIC     self.normalize()
// MAGIC     
// MAGIC   def set_time_to_zero(self):
// MAGIC     """
// MAGIC       sets the time vector to start at time zero
// MAGIC     """
// MAGIC     if self.num_xdim == 2:
// MAGIC       self.x_train_min = self.x_train[:,0].min()
// MAGIC       self.x_train[:,0] = self.x_train[:,0] - self.x_train_min
// MAGIC       self.x_test[:,0] = self.x_test[:,0] - self.x_train_min      
// MAGIC     else:
// MAGIC       self.x_train_min = self.x_train.min()
// MAGIC       self.x_train = self.x_train - self.x_train_min
// MAGIC       self.x_test = self.x_test - self.x_train_min
// MAGIC       
// MAGIC   def normalize(self):
// MAGIC     """
// MAGIC       normalize the data to improve predictions
// MAGIC     """
// MAGIC     self.set_time_to_zero()
// MAGIC     
// MAGIC     self.x_train_mean = self.x_train.mean()
// MAGIC     self.x_train_std = self.x_train.std()
// MAGIC     self.x_train = (self.x_train - self.x_train_mean) / self.x_train_std
// MAGIC     self.x_test = (self.x_test - self.x_train_mean) / self.x_train_std     
// MAGIC 
// MAGIC     self.y_train_mean = self.y_train.mean()
// MAGIC     self.y_train_std = self.y_train.std()
// MAGIC     self.y_train = (self.y_train - self.y_train_mean) / self.y_train_std
// MAGIC     self.y_test = (self.y_test - self.y_train_mean) / self.y_train_std 
// MAGIC     self.data_normalized = True
// MAGIC 
// MAGIC       
// MAGIC   def plot_reduced_data(self):
// MAGIC     """
// MAGIC       plots the reduced training data
// MAGIC     """
// MAGIC     with th.no_grad():      
// MAGIC       if self.num_xdim == 2:
// MAGIC         fig, (ax1, ax2) = plt.subplots(1,2, figsize=(12,6))
// MAGIC         ax1.plot(self.x_train[:,0], self.y_train, 'k*')
// MAGIC         ax1.legend(['train data'])
// MAGIC         ax1.set_xlabel('time [days]')
// MAGIC         ax1.set_ylabel('output')
// MAGIC         ax1.set_title('training data')
// MAGIC         ax1.grid()   
// MAGIC         ax2.plot(self.x_train[:,0], self.x_train[:,1], 'k*')
// MAGIC         ax2.legend(['train data'])
// MAGIC         ax2.set_xlabel('time [days]')
// MAGIC         ax2.set_ylabel('additional input')
// MAGIC         ax2.set_title('training data')
// MAGIC         ax2.grid()         
// MAGIC       else:
// MAGIC         fig, ax = plt.subplots(1,1, figsize=(12,6))
// MAGIC         ax.plot(self.x_train, self.y_train, 'k*')
// MAGIC         ax.legend(['train data'])
// MAGIC         ax.set_xlabel('time [days]')
// MAGIC         ax.set_ylabel('output')
// MAGIC         ax.set_title('training data')
// MAGIC         ax.grid()     
// MAGIC         

// COMMAND ----------

// MAGIC %md
// MAGIC Use class to convert dataframes to numpy arrays for further processing. Note, the conversion may take a while.

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncworld = GPDataSet(df_ncworld_train, df_ncworld_test, datacol = 'new_cases_smoothed', filterloc = None, add_input=None)
// MAGIC ds_ndworld = GPDataSet(df_ndworld_train, df_ndworld_test, datacol = 'new_deaths_smoothed', filterloc = None, add_input=None)
// MAGIC ds_ncworld.convert_to_numpy()
// MAGIC ds_ndworld.convert_to_numpy()

// COMMAND ----------

// MAGIC %md Plot

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncworld.plot_numpy_data()

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ndworld.plot_numpy_data()

// COMMAND ----------

// MAGIC %md
// MAGIC Process data by subsampling, reducing data window and normalize data. The gaussian process model is a so called non parametric model and will be mainly based on the data points. As such, to reduce the computation and the complexity of the model, we subsample and reduce the number of datapoints.

// COMMAND ----------

// MAGIC %python 
// MAGIC ds_ncworld.process_numpy_data(nth_subsample = 4, window_red = 0.8)
// MAGIC ds_ndworld.process_numpy_data(nth_subsample = 4, window_red = 0.8)

// COMMAND ----------

// MAGIC %md Plot processed data

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncworld.plot_reduced_data()

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ndworld.plot_reduced_data()

// COMMAND ----------

// MAGIC %md Define gaussian process classes using Gpytorch and different kernels.

// COMMAND ----------

// MAGIC %python
// MAGIC import gpytorch as gpth
// MAGIC class GPLinearRBF(gpth.models.ExactGP):
// MAGIC   def __init__(self, train_x, train_y, likelihood):
// MAGIC     super(GPLinearRBF, self).__init__(train_x, train_y, likelihood)
// MAGIC     self.mean_module = gpth.means.ConstantMean()
// MAGIC     self.covar_module = gpth.kernels.ScaleKernel(gpth.kernels.LinearKernel() + gpth.kernels.RBFKernel())
// MAGIC     
// MAGIC   def forward(self, x):
// MAGIC     x_mean = self.mean_module(x)
// MAGIC     x_covar = self.covar_module(x)
// MAGIC     return gpth.distributions.MultivariateNormal(x_mean, x_covar)    
// MAGIC   
// MAGIC class GPLinearMatern(gpth.models.ExactGP):
// MAGIC   def __init__(self, train_x, train_y, likelihood):
// MAGIC     super(GPLinearMatern, self).__init__(train_x, train_y, likelihood)
// MAGIC     self.mean_module = gpth.means.ConstantMean()
// MAGIC     self.covar_module = gpth.kernels.ScaleKernel(gpth.kernels.LinearKernel() + gpth.kernels.MaternKernel())
// MAGIC     
// MAGIC   def forward(self, x):
// MAGIC     x_mean = self.mean_module(x)
// MAGIC     x_covar = self.covar_module(x)
// MAGIC     return gpth.distributions.MultivariateNormal(x_mean, x_covar) 

// COMMAND ----------

// MAGIC %md Define a training class for the Gaussian Process models

// COMMAND ----------

// MAGIC %python
// MAGIC import matplotlib.pyplot as plt
// MAGIC import math
// MAGIC from sklearn.metrics import mean_squared_error
// MAGIC from sklearn.metrics import mean_absolute_error
// MAGIC 
// MAGIC class GPTrainer():
// MAGIC   def __init__(self, gp_model, x_train, x_train_min, x_train_mean, x_train_std, x_test, y_train, y_test, y_train_mean, y_train_std, device='cpu', train_iter = 300, lr=0.1, verbose = True):
// MAGIC     """ 
// MAGIC       class to manage training and prediction of data
// MAGIC      
// MAGIC       param: 'gp_model', name of gaussian process model including kernel to use
// MAGIC       param: 'x_train', pytorch tensor (sequence, dim), normalized input training data, starting at time zero
// MAGIC       param: 'x_train_min', pytorch tensor, start time of input training data
// MAGIC       param: 'x_train_mean', pytorch tensor, mean used when normalizing input training data
// MAGIC       param: 'x_train_std', pytorch tensor, std deviation used when normalizing input training data
// MAGIC       param: 'x_test', pytorch tensor, normalized input test data, starting at time zero
// MAGIC       param: 'y_train', pytorch tensor, normalized output training data      
// MAGIC       param: 'y_train_mean', pytorch tensor, mean used when normalizing output training data
// MAGIC       param: 'y_train_std', pytorch tensor, std deviation used when normalizing output training data 
// MAGIC       param: 'y_test', pytorch tensor, normalized output test data     
// MAGIC       param: 'device', cpu or cuda. currently only tested for cpu.
// MAGIC       param: 'train_iter', number of training iterations to fit kernel parameters to data
// MAGIC       param: 'lr', learning rate
// MAGIC       param: 'verbose', print information such as loss during training
// MAGIC     """
// MAGIC     
// MAGIC     # data
// MAGIC     self.x_train = x_train.to(device)
// MAGIC     self.x_train_min = x_train_min
// MAGIC     self.x_train_mean = x_train_mean
// MAGIC     self.x_train_std = x_train_std    
// MAGIC     self.x_test = x_test.to(device)
// MAGIC     self.x_cat = th.cat((x_train,x_test),dim=0).to(device) 
// MAGIC     self.y_train = y_train.to(device)
// MAGIC     self.y_train_mean = y_train_mean
// MAGIC     self.y_train_std = y_train_std
// MAGIC     self.y_test = y_test.to(device)
// MAGIC     self.preds = None
// MAGIC     
// MAGIC     # define GP likelihood
// MAGIC     self.likelihood = gpth.likelihoods.GaussianLikelihood()    
// MAGIC     
// MAGIC     # GP model selection and init
// MAGIC     assert gp_model == 'GPLinearRBF' or 'GPLinearMatern', "Error: GP model selected is not defined"
// MAGIC     if gp_model == 'GPLinearRBF':
// MAGIC       self.model = GPLinearRBF(self.x_train, self.y_train, self.likelihood).to(device)
// MAGIC     if gp_model == 'GPLinearMatern':
// MAGIC       self.model = GPLinearMatern(self.x_train, self.y_train, self.likelihood).to(device)
// MAGIC       
// MAGIC     # training param
// MAGIC     self.train_iter = train_iter
// MAGIC     self.lr = lr
// MAGIC     self.device = device
// MAGIC     self.optimizer = th.optim.Adam(self.model.parameters(), lr=self.lr)
// MAGIC     self.loss_fn = gpth.mlls.ExactMarginalLogLikelihood(self.likelihood, self.model)   
// MAGIC     self.verbose = verbose
// MAGIC     
// MAGIC     # plots
// MAGIC     self.fig = None
// MAGIC     self.ax = None
// MAGIC       
// MAGIC   def train(self):
// MAGIC     """
// MAGIC       training of gaussian process model to fit kernel parameters to data
// MAGIC     """
// MAGIC     self.model.train()
// MAGIC     self.likelihood.train()
// MAGIC     
// MAGIC     for iter_idx in range(1,self.train_iter+1):
// MAGIC       self.optimizer.zero_grad()
// MAGIC       out = self.model(self.x_train)
// MAGIC       loss = -self.loss_fn(out, self.y_train).mean()
// MAGIC       loss.backward()
// MAGIC       self.optimizer.step()
// MAGIC       if iter_idx % 10 == 0 and self.verbose is True:
// MAGIC         print(f"Iter: {iter_idx}, train_loss: {loss.item()}")
// MAGIC         
// MAGIC   def prediction(self):
// MAGIC     """
// MAGIC       predict data
// MAGIC     """
// MAGIC     self.model.eval()
// MAGIC     self.likelihood.eval()
// MAGIC     with th.no_grad(): #, gpth.settings.fast_pred_var():  
// MAGIC       self.preds = self.likelihood(self.model(self.x_cat))
// MAGIC       
// MAGIC   def denormalize_y(self, data):
// MAGIC     """
// MAGIC       denormalize the output data
// MAGIC     """
// MAGIC     return data*self.y_train_std + self.y_train_mean
// MAGIC   
// MAGIC   def denormalize_x(self, data):
// MAGIC     """
// MAGIC       denormalize the input data
// MAGIC     """
// MAGIC     return data*self.x_train_std + self.x_train_mean  
// MAGIC       
// MAGIC   def plot(self):
// MAGIC     """
// MAGIC       plot the data
// MAGIC     """
// MAGIC     with th.no_grad():
// MAGIC       
// MAGIC       # extract time index dimension
// MAGIC       xdim = None
// MAGIC       try:
// MAGIC         _, xdim = self.x_train.shape
// MAGIC       except:
// MAGIC         pass
// MAGIC       if xdim == None or xdim == 1:
// MAGIC         x_train = self.denormalize_x(self.x_train)
// MAGIC         x_test = self.denormalize_x(self.x_test)
// MAGIC         x_cat = self.denormalize_x(self.x_cat)
// MAGIC       elif xdim > 1:
// MAGIC         x_train = self.denormalize_x(self.x_train)[:,0]
// MAGIC         x_test = self.denormalize_x(self.x_test)[:,0]
// MAGIC         x_cat = self.denormalize_x(self.x_cat)[:,0]
// MAGIC         
// MAGIC       # plot
// MAGIC       self.fig, self.ax = plt.subplots(1,1, figsize=(12,6))
// MAGIC       lower = self.denormalize_y(self.preds.mean - self.preds.variance.sqrt() * 1.96)
// MAGIC       upper = self.denormalize_y(self.preds.mean + self.preds.variance.sqrt() * 1.96)
// MAGIC       self.ax.plot(x_train.numpy()+self.x_train_min.numpy(), self.denormalize_y(self.y_train).numpy(), 'k*')
// MAGIC       self.ax.plot(x_test.numpy()+self.x_train_min.numpy(), self.denormalize_y(self.y_test).numpy(), 'r*')
// MAGIC       self.ax.plot(x_cat.numpy()+self.x_train_min.numpy(), self.denormalize_y(self.preds.mean).numpy(), 'b')
// MAGIC       self.ax.fill_between(x_cat.numpy()+self.x_train_min.numpy(), lower.numpy(), upper.numpy(), alpha=0.3)
// MAGIC       self.ax.legend(['train data', 'test data', 'predicted mean', 'predicted confidence 95%'])
// MAGIC       self.ax.set_xlabel('time [days]')
// MAGIC       self.ax.set_ylabel('prediction')
// MAGIC       self.ax.set_title('prediction')
// MAGIC       self.ax.grid()     
// MAGIC       
// MAGIC   def print_data_dim(self):
// MAGIC     """
// MAGIC       print shapes for debug purpose
// MAGIC     """
// MAGIC     print("data shapes:")
// MAGIC     print(f'x_train: {self.x_train.shape}')
// MAGIC     print(f'x_test: {self.x_test.shape}')
// MAGIC     print(f'x_cat: {self.x_cat.shape}')
// MAGIC     print(f'y_train: {self.y_train.shape}')
// MAGIC     print(f'y_test: {self.y_test.shape}')  
// MAGIC     try:
// MAGIC       print(f'preds mean: {self.preds.mean.shape}')
// MAGIC     except:
// MAGIC       pass
// MAGIC 
// MAGIC   def evaluate(self):
// MAGIC     """
// MAGIC       evaluation of predictions
// MAGIC     """
// MAGIC     with th.no_grad():
// MAGIC       # data to evaluate
// MAGIC       test_data = self.denormalize_y(self.y_test) 
// MAGIC       predictions = self.denormalize_y(self.preds.mean[-len(self.y_test):])
// MAGIC       
// MAGIC       # evaluate
// MAGIC       error_mse = mean_squared_error(test_data, predictions)
// MAGIC       error_rmse = math.sqrt(error_mse)
// MAGIC       error_abs = mean_absolute_error(test_data, predictions)
// MAGIC       avg_gt = test_data.sum() / len(test_data)
// MAGIC       mse_percentage = error_rmse / avg_gt * 100
// MAGIC       abs_percentage = error_abs / avg_gt * 100
// MAGIC       
// MAGIC       # print
// MAGIC       print('Average of groundtruth: %.3f' % avg_gt)
// MAGIC       print('Test MSE: %.3f' % error_mse)
// MAGIC       print('Test RMSE: %.3f' % error_rmse)
// MAGIC       print('RMSE percentage error: %.3f' % mse_percentage, '%')
// MAGIC       print('Test ABS: %.3f' % error_abs)
// MAGIC       print('ABS percentage error: %.3f' % abs_percentage, '%')      

// COMMAND ----------

// MAGIC %md Init the training class for the Gaussian Process models

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncworld = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ncworld.x_train, x_train_min=ds_ncworld.x_train_min, x_train_mean=ds_ncworld.x_train_mean, x_train_std=ds_ncworld.x_train_std, x_test=ds_ncworld.x_test, y_train=ds_ncworld.y_train, y_test=ds_ncworld.y_test, y_train_mean=ds_ncworld.y_train_mean, y_train_std=ds_ncworld.y_train_std)
// MAGIC 
// MAGIC pred_ndworld = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ndworld.x_train, x_train_min=ds_ndworld.x_train_min, x_train_mean=ds_ndworld.x_train_mean, x_train_std=ds_ndworld.x_train_std, x_test=ds_ndworld.x_test, y_train=ds_ndworld.y_train, y_test=ds_ndworld.y_test, y_train_mean=ds_ndworld.y_train_mean, y_train_std=ds_ndworld.y_train_std)

// COMMAND ----------

// MAGIC %md Training

// COMMAND ----------

// MAGIC %python
// MAGIC print('\ntraining new cases prediction model')
// MAGIC pred_ncworld.train()
// MAGIC print('\ntraining new deaths prediction model')
// MAGIC pred_ndworld.train()

// COMMAND ----------

// MAGIC %md Prediction and plot

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncworld.prediction()
// MAGIC pred_ncworld.plot()
// MAGIC pred_ncworld.ax.set_ylabel('new cases smoothed')
// MAGIC pred_ncworld.ax.set_title('new cases smoothed')

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ndworld.prediction()
// MAGIC pred_ndworld.plot()
// MAGIC pred_ndworld.ax.set_ylabel('new deaths smoothed')
// MAGIC pred_ndworld.ax.set_title('new deaths smoothed')

// COMMAND ----------

// MAGIC %md ### 3.2 World onestep prediction

// COMMAND ----------

// MAGIC %md To perform onestep ahead mean prediction, we define some additional functions

// COMMAND ----------

// MAGIC %python
// MAGIC def onestep_prediction(dataset):
// MAGIC   onestep = th.cat((dataset.y_train, dataset.y_test), dim=0) # output vector
// MAGIC   for idx in range(len(dataset.y_test)):
// MAGIC     
// MAGIC     # define training and test data. Training data is iteratively, step by step, expanded by the use of test data
// MAGIC     x_train = th.cat((dataset.x_train, dataset.x_test[:idx]), dim=0)
// MAGIC     x_test = dataset.x_test[idx:]
// MAGIC     y_train = th.cat((dataset.y_train, dataset.y_test[:idx]), dim=0)
// MAGIC     y_test = dataset.y_test[idx:]
// MAGIC     
// MAGIC     # create a gaussian process model, train and make predictions
// MAGIC     pred_model = GPTrainer(gp_model='GPLinearRBF', x_train=x_train, x_train_min=dataset.x_train_min, x_train_mean=dataset.x_train_mean, x_train_std=dataset.x_train_std, x_test=x_test, y_train=y_train, y_test=y_test, y_train_mean=dataset.y_train_mean, y_train_std=dataset.y_train_std, verbose=False)
// MAGIC     pred_model.train()
// MAGIC     pred_model.prediction()
// MAGIC     
// MAGIC     # store one step predictions
// MAGIC     onestep[len(dataset.y_train) + idx] = pred_model.preds.mean[len(dataset.x_train)+idx]
// MAGIC 
// MAGIC   # plot results
// MAGIC   fig, ax = plt.subplots(1,1, figsize=(12,6))
// MAGIC   ax.plot(pred_model.x_train_min + pred_model.denormalize_x(dataset.x_test), pred_model.denormalize_y(dataset.y_test),'*r', pred_model.x_train_min + pred_model.denormalize_x(dataset.x_test), pred_model.denormalize_y(onestep[len(dataset.y_train):]),'k*')
// MAGIC   ax.legend(['test data', 'prediction mean'])
// MAGIC   ax.set_xlabel('time [days]')
// MAGIC   ax.set_ylabel('prediction mean')
// MAGIC   ax.set_title('one step ahead prediction')
// MAGIC   ax.grid()   
// MAGIC   
// MAGIC   # return onestep prediction
// MAGIC   return onestep

// COMMAND ----------

// MAGIC %md We iteratively predict the next one step ahead

// COMMAND ----------

// MAGIC %python
// MAGIC onestep_pred_ncworld = onestep_prediction(ds_ncworld)

// COMMAND ----------

// MAGIC %python
// MAGIC onestep_pred_ndworld = onestep_prediction(ds_ndworld)

// COMMAND ----------

// MAGIC %md ### 3.3 Sweden multistep prediction
// MAGIC Use class to convert dataframes to numpy arrays for further processing. Note, the conversion may take a while.

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncswe = GPDataSet(df_ncdenswe_train, df_ncdenswe_test, datacol = 'new_cases_smoothed', filterloc = 'Sweden', add_input=None)
// MAGIC ds_ndswe = GPDataSet(df_nddenswe_train, df_nddenswe_test, datacol = 'new_deaths_smoothed', filterloc = 'Sweden', add_input=None)
// MAGIC ds_ncswe.convert_to_numpy()
// MAGIC ds_ndswe.convert_to_numpy()

// COMMAND ----------

// MAGIC %md
// MAGIC Plot data.

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncswe.plot_numpy_data()

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ndswe.plot_numpy_data()

// COMMAND ----------

// MAGIC %md
// MAGIC Process data by subsampling, reducing data window and normalize data. The gaussian process model is a so called non parametric model and will be mainly based on the data points. As such, to reduce the computation and the complexity of the model, we subsample and reduce the number of datapoints.

// COMMAND ----------

// MAGIC %python 
// MAGIC ds_ncswe.process_numpy_data(nth_subsample = 4, window_red = 0.8)
// MAGIC ds_ndswe.process_numpy_data(nth_subsample = 4, window_red = 0.8)

// COMMAND ----------

// MAGIC %md Plot processed data

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncswe.plot_reduced_data()

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ndswe.plot_reduced_data()

// COMMAND ----------

// MAGIC %md Init the training class for the Gaussian Process models

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncswe = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ncswe.x_train, x_train_min=ds_ncswe.x_train_min, x_train_mean=ds_ncswe.x_train_mean, x_train_std=ds_ncswe.x_train_std, x_test=ds_ncswe.x_test, y_train=ds_ncswe.y_train, y_test=ds_ncswe.y_test, y_train_mean=ds_ncswe.y_train_mean, y_train_std=ds_ncswe.y_train_std)
// MAGIC 
// MAGIC pred_ndswe = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ndswe.x_train, x_train_min=ds_ndswe.x_train_min, x_train_mean=ds_ncswe.x_train_mean, x_train_std=ds_ncswe.x_train_std, x_test=ds_ndswe.x_test, y_train=ds_ndswe.y_train, y_test=ds_ndswe.y_test, y_train_mean=ds_ndswe.y_train_mean, y_train_std=ds_ndswe.y_train_std)

// COMMAND ----------

// MAGIC %md Training

// COMMAND ----------

// MAGIC %python
// MAGIC print('\ntraining new cases prediction model')
// MAGIC pred_ncswe.train()
// MAGIC print('\ntraining new deaths prediction model')
// MAGIC pred_ndswe.train()

// COMMAND ----------

// MAGIC %md Prediction and plot

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncswe.prediction()
// MAGIC pred_ncswe.plot()
// MAGIC pred_ncswe.ax.set_ylabel('new cases smoothed')
// MAGIC pred_ncswe.ax.set_title('new cases smoothed')

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ndswe.prediction()
// MAGIC pred_ndswe.plot()
// MAGIC pred_ndswe.ax.set_ylabel('new deaths smoothed')
// MAGIC pred_ndswe.ax.set_title('new deaths smoothed')

// COMMAND ----------

// MAGIC %md ### 3.4 Sweden onestep prediction

// COMMAND ----------

// MAGIC %python
// MAGIC onestep_pred_ncswe = onestep_prediction(ds_ncswe)

// COMMAND ----------

// MAGIC %python
// MAGIC onestep_pred_ndswe = onestep_prediction(ds_ndswe)

// COMMAND ----------

// MAGIC %md ### 3.5 Sweden multistep prediction with additional data input from neighbouring country
// MAGIC Assuming we knew the results from a neighbouring country and if data is correlated, we could presumably improve the prediction 

// COMMAND ----------

// MAGIC %md Plot resulting data used for prediction. Both plots appears to follow a form of trend.

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncswex = GPDataSet(df_ncdenswe_train, df_ncdenswe_test, datacol = 'new_cases_smoothed', filterloc = 'Sweden', add_input='Denmark')
// MAGIC ds_ndswex = GPDataSet(df_nddenswe_train, df_nddenswe_test, datacol = 'new_deaths_smoothed', filterloc = 'Sweden', add_input='Denmark')
// MAGIC ds_ncswex.convert_to_numpy()
// MAGIC ds_ndswex.convert_to_numpy()

// COMMAND ----------

// MAGIC %md
// MAGIC Plot data.

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncswex.plot_numpy_data()

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ndswex.plot_numpy_data()

// COMMAND ----------

// MAGIC %md
// MAGIC Process data by subsampling, reducing data window and normalize data. The gaussian process model is a so called non parametric model and will be mainly based on the data points. As such, to reduce the computation and the complexity of the model, we subsample and reduce the number of datapoints.

// COMMAND ----------

// MAGIC %python 
// MAGIC ds_ncswex.process_numpy_data(nth_subsample = 4, window_red = 0.8)
// MAGIC ds_ndswex.process_numpy_data(nth_subsample = 4, window_red = 0.8)

// COMMAND ----------

// MAGIC %md Plot processed data

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ncswex.plot_reduced_data()

// COMMAND ----------

// MAGIC %python
// MAGIC ds_ndswex.plot_reduced_data()

// COMMAND ----------

// MAGIC %md Init the training class for the Gaussian Process models

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncswex = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ncswex.x_train, x_train_min=ds_ncswex.x_train_min, x_train_mean=ds_ncswex.x_train_mean, x_train_std=ds_ncswex.x_train_std, x_test=ds_ncswex.x_test, y_train=ds_ncswex.y_train, y_test=ds_ncswex.y_test, y_train_mean=ds_ncswex.y_train_mean, y_train_std=ds_ncswex.y_train_std)
// MAGIC 
// MAGIC pred_ndswex = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ndswex.x_train, x_train_min=ds_ndswex.x_train_min, x_train_mean=ds_ndswex.x_train_mean, x_train_std=ds_ndswex.x_train_std, x_test=ds_ndswex.x_test, y_train=ds_ndswex.y_train, y_test=ds_ndswex.y_test, y_train_mean=ds_ndswex.y_train_mean, y_train_std=ds_ndswex.y_train_std)

// COMMAND ----------

// MAGIC %md Training

// COMMAND ----------

// MAGIC %python
// MAGIC print('\ntraining new cases prediction model')
// MAGIC pred_ncswex.train()
// MAGIC print('\ntraining new deaths prediction model')
// MAGIC pred_ndswex.train()

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncswex.prediction()
// MAGIC pred_ncswex.plot()
// MAGIC pred_ncswex.ax.set_ylabel('new cases smoothed')
// MAGIC pred_ncswex.ax.set_title('new cases smoothed')

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ndswex.prediction()
// MAGIC pred_ndswex.plot()
// MAGIC pred_ndswex.ax.set_ylabel('new deaths smoothed')
// MAGIC pred_ndswex.ax.set_title('new deaths smoothed')

// COMMAND ----------

// MAGIC %md ## 4. Evaluation

// COMMAND ----------

// MAGIC %md ### 4.1 World multistep

// COMMAND ----------

// MAGIC %md Evaluation of new cases smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncworld.evaluate()

// COMMAND ----------

// MAGIC %md Evaluation of new deaths smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ndworld.evaluate()

// COMMAND ----------

// MAGIC %md ### 4.2 World onestep

// COMMAND ----------

// MAGIC %md To evaluate the onestep ahead prediction, we define an additional function

// COMMAND ----------

// MAGIC %python
// MAGIC def evaluate(test_data, prediction):
// MAGIC   with th.no_grad():    
// MAGIC     # evaluate
// MAGIC     error_mse = mean_squared_error(test_data, prediction)
// MAGIC     error_rmse = math.sqrt(error_mse)
// MAGIC     error_abs = mean_absolute_error(test_data, prediction)
// MAGIC     avg_gt = test_data.sum() / len(test_data)
// MAGIC     mse_percentage = error_rmse / avg_gt * 100
// MAGIC     abs_percentage = error_abs / avg_gt * 100
// MAGIC 
// MAGIC     # print
// MAGIC     print('Average of groundtruth: %.3f' % avg_gt)
// MAGIC     print('Test MSE: %.3f' % error_mse)
// MAGIC     print('Test RMSE: %.3f' % error_rmse)
// MAGIC     print('RMSE percentage error: %.3f' % mse_percentage, '%')
// MAGIC     print('Test ABS: %.3f' % error_abs)
// MAGIC     print('ABS percentage error: %.3f' % abs_percentage, '%') 

// COMMAND ----------

// MAGIC %md Evaluation of new cases smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC # data to evaluate
// MAGIC test = pred_ncworld.denormalize_y(ds_ncworld.y_test) 
// MAGIC preds = pred_ncworld.denormalize_y(onestep_pred_ncworld[-len(ds_ncworld.y_test):])
// MAGIC evaluate(test, preds)

// COMMAND ----------

// MAGIC %md Evaluation of new deaths smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC # data to evaluate
// MAGIC test = pred_ndworld.denormalize_y(ds_ndworld.y_test) 
// MAGIC preds = pred_ndworld.denormalize_y(onestep_pred_ndworld[-len(ds_ndworld.y_test):])
// MAGIC evaluate(test, preds)

// COMMAND ----------

// MAGIC %md ### 4.2 Sweden multistep

// COMMAND ----------

// MAGIC %md Evaluation of new cases smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncswe.evaluate()

// COMMAND ----------

// MAGIC %md Evaluation of new deaths smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ndswe.evaluate()

// COMMAND ----------

// MAGIC %md ### 4.4 Sweden onestep

// COMMAND ----------

// MAGIC %md Evaluation of new cases smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC # data to evaluate
// MAGIC test = pred_ncswe.denormalize_y(ds_ncswe.y_test) 
// MAGIC preds = pred_ncswe.denormalize_y(onestep_pred_ncswe[-len(ds_ncswe.y_test):])
// MAGIC evaluate(test, preds)

// COMMAND ----------

// MAGIC %md Evaluation of new deaths smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC # data to evaluate
// MAGIC test = pred_ndswe.denormalize_y(ds_ndswe.y_test) 
// MAGIC preds = pred_ndswe.denormalize_y(onestep_pred_ndswe[-len(ds_ndswe.y_test):])
// MAGIC evaluate(test, preds)

// COMMAND ----------

// MAGIC %md ### 4.4 Sweden multistep with additional information

// COMMAND ----------

// MAGIC %md Evaluation of new cases smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ncswex.evaluate()

// COMMAND ----------

// MAGIC %md Evaluation of new deaths smoothed

// COMMAND ----------

// MAGIC %python
// MAGIC pred_ndswex.evaluate()

// COMMAND ----------

// MAGIC %md ## 5. Conclusions and reflections

// COMMAND ----------

// MAGIC %md
// MAGIC Predictions using gaussian processes were made for both new cases smoothed and new deaths smoothed. This included an aggregation of many countries within the world as well as for Sweden. Making single step ahead predictions resulted naturally in smaller errors compared to the multistep predictions. The multistep prediction for Sweden could be improved for new cases smoothed using correlated data from a neighbouring country.
// MAGIC 
// MAGIC We believe the Gaussian process model is a valuable tool for making predictions. With this work, we would like to highlight that the data points and kernel chosen for the Gaussian process heavily biases the model and strongly influences the predictions. In this project, we selected a combination of a linear kernel and a radial basis function. The reason being that there is a trend in the data and that nearby data points should be more similar than data points further away. By inspecting the data carefully, a more optimal kernel could likely be selected. Also, the confidence intervall provided with the gaussian process model is based on that the kernel is correctly representing the underlying distribution of data.
// MAGIC 
// MAGIC In terms of scalability, the predictions are somewhat scalable as a user can define a window of data for making the predictions. Furthermore, GPU support could be included and approximations to the gaussian process model could be made.
// MAGIC 
// MAGIC Compared to the ARIMA model, the gaussian process model performed in most cases slightly worse. However, this may be due to the selection of data points and kernel considering that the gaussian process model is heavily biased by these choices. One reflection is that if one approximately knows the distribution of the underlying data, a gaussian process model with a proper selected kernel may be a good choice.