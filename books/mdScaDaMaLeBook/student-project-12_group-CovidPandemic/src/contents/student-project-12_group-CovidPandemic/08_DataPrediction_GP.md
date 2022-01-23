<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Prediction with time series model - Gaussian Processes
======================================================

This notebook contains time series prediction with gaussian processes. The data used for prediction is new cases (smoothed) and new deaths (smoothed) for both an aggregated number of countries in the world and for Sweden. To implement the gaussian process model, the python package Gpytorch is used.

</div>

<div class="cell markdown">

1. Install, import, load and preprocess data
--------------------------------------------

Install, import and execute the other relevant notebooks here to load and preprocess data...

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pip install gpytorch
```

<div class="output execute_result plain_result" execution_count="1">

    Python interpreter will be restarted.
    Collecting gpytorch
      Downloading gpytorch-1.3.0.tar.gz (283 kB)
    Building wheels for collected packages: gpytorch
      Building wheel for gpytorch (setup.py): started
      Building wheel for gpytorch (setup.py): finished with status 'done'
      Created wheel for gpytorch: filename=gpytorch-1.3.0-py2.py3-none-any.whl size=473796 sha256=5882e250a68a9042a1e51e11617837c2e922878bd22e515cf9459b217c96ba2b
      Stored in directory: /root/.cache/pip/wheels/1d/f0/2c/2146864c1f7bd8a844c4143115c05c392da763fd8b249adb9d
    Successfully built gpytorch
    Installing collected packages: gpytorch
    Successfully installed gpytorch-1.3.0
    Python interpreter will be restarted.

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# python imports
import gpytorch as gpth
import torch as th
import matplotlib.pyplot as plt
import numpy as np
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
"./02_DataPreprocess"
```

</div>

<div class="cell markdown">

2. Additional data preprocessing in Scala
-----------------------------------------

</div>

<div class="cell markdown">

### 2.1 World data preprocessing

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// define dataframe summing up the new cases smoothed for each date
val df_ncworld = df_cleaned_time_series.groupBy("date").sum("new_cases_smoothed").sort(col("date")).withColumnRenamed("sum(new_cases_smoothed)","new_cases_smoothed")
display(df_ncworld)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_1.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// define dataframe summing up the new deaths smoothed for each date
val df_ndworld = df_cleaned_time_series.groupBy("date").sum("new_deaths_smoothed").sort(col("date")).withColumnRenamed("sum(new_deaths_smoothed)","new_deaths_smoothed")
display(df_ndworld)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_2.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Add a time index for the date
import org.apache.spark.sql.expressions.Window
val window_spec  = Window.orderBy($"date")

val df_ncworld_indexed = df_ncworld.withColumn("time_idx",row_number.over(window_spec))
val df_ndworld_indexed = df_ndworld.withColumn("time_idx",row_number.over(window_spec))
display(df_ncworld_indexed)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Get max and min of time index
import org.apache.spark.sql.functions.{min, max}
import org.apache.spark.sql.Row

val id_maxmin = df_ncworld_indexed.agg(max("time_idx"), min("time_idx")).head()
val id_max: Int = id_maxmin.getInt(0)
val id_min: Int = id_maxmin.getInt(1)
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions.{min, max}
    import org.apache.spark.sql.Row
    id_maxmin: org.apache.spark.sql.Row = [316,1]
    id_max: Int = 316
    id_min: Int = 1

</div>

</div>

<div class="cell markdown">

Extract a window for prediction

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// define training and test data intervalls. test data is set to 10% of the total dataset time length.
val test_wnd: Int = (0.1*id_max).toInt
val train_wnd: Int = (0.9*id_max).toInt

val df_ncworld_train = df_ncworld_indexed.where($"time_idx" > id_max-train_wnd-test_wnd && $"time_idx" <= id_max-test_wnd)
val df_ncworld_test = df_ncworld_indexed.where($"time_idx" > id_max-test_wnd && $"time_idx" <= id_max)
val df_ndworld_train = df_ndworld_indexed.where($"time_idx" > id_max-train_wnd-test_wnd && $"time_idx" <= id_max-test_wnd)
val df_ndworld_test = df_ndworld_indexed.where($"time_idx" > id_max-test_wnd && $"time_idx" <= id_max)
display(df_ncworld_test)
```

</div>

<div class="cell markdown">

Convert to python for further processing

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_ncworld_train.createOrReplaceTempView("df_ncworld_train")
df_ncworld_test.createOrReplaceTempView("df_ncworld_test")
df_ndworld_train.createOrReplaceTempView("df_ndworld_train")
df_ndworld_test.createOrReplaceTempView("df_ndworld_test")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
df_ncworld_train = spark.table("df_ncworld_train")
df_ncworld_test = spark.table("df_ncworld_test")
df_ndworld_train = spark.table("df_ndworld_train")
df_ndworld_test = spark.table("df_ndworld_test")
```

</div>

<div class="cell markdown">

### 2.2 Sweden preprocessing

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_ncdenswe = df_cleaned_time_series.select($"location", $"date", $"new_cases_smoothed").where(expr("location = 'Sweden' or location = 'Denmark'"))
val df_nddenswe = df_cleaned_time_series.select($"location", $"date", $"new_deaths_smoothed").where(expr("location = 'Sweden' or location = 'Denmark'"))
```

<div class="output execute_result plain_result" execution_count="1">

    df_ncdenswe: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [location: string, date: string ... 1 more field]
    df_nddenswe: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [location: string, date: string ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Add a time index for the date
import org.apache.spark.sql.expressions.Window
val window_spec  = Window.partitionBy("location").orderBy($"date")

val df_ncdenswe_indexed = df_ncdenswe.withColumn("time_idx",row_number.over(window_spec))
display(df_ncdenswe_indexed)

val df_nddenswe_indexed = df_nddenswe.withColumn("time_idx",row_number.over(window_spec))
display(df_nddenswe_indexed)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val test_wnd: Int = (0.1*id_max).toInt
val train_wnd: Int = (0.9*id_max).toInt
val df_ncdenswe_train = df_ncdenswe_indexed.where($"time_idx" > id_max-train_wnd-test_wnd && $"time_idx" <= id_max-test_wnd)
val df_ncdenswe_test = df_ncdenswe_indexed.where($"time_idx" > id_max-test_wnd && $"time_idx" <= id_max)
val df_nddenswe_train = df_nddenswe_indexed.where($"time_idx" > id_max-train_wnd-test_wnd && $"time_idx" <= id_max-test_wnd)
val df_nddenswe_test = df_nddenswe_indexed.where($"time_idx" > id_max-test_wnd && $"time_idx" <= id_max)
display(df_ncdenswe_test)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_ncdenswe_train.createOrReplaceTempView("df_ncdenswe_train")
df_ncdenswe_test.createOrReplaceTempView("df_ncdenswe_test")
df_nddenswe_train.createOrReplaceTempView("df_nddenswe_train")
df_nddenswe_test.createOrReplaceTempView("df_nddenswe_test")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
df_ncdenswe_train = spark.table("df_ncdenswe_train")
df_ncdenswe_test = spark.table("df_ncdenswe_test")
df_nddenswe_train = spark.table("df_nddenswe_train")
df_nddenswe_test = spark.table("df_nddenswe_test")
```

</div>

<div class="cell markdown">

3. Time series prediction with Gaussian Processes
-------------------------------------------------

In this section we perform predictions based on the input data. Some additional preprocessing in Python is done as well. The transition from Scala to Python is motivated by the use of the python package Gpytorch for implementing the gaussian process model.

</div>

<div class="cell markdown">

### 3.1 World multistep prediction

As similar operations are performed for processing data, a class is first defined to enable code reuse

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from pyspark.sql.functions import col
import matplotlib.pyplot as plt

class GPDataSet():
  def __init__(self, df_train, df_test, datacol, filterloc = None, add_input = None):
    """
      class for processing input data to GP. As similar code is reused, this class enables some code reuse.
      
      param: 'df_train', training data dataframe
      param: 'df_test', test data dataframe
      param: 'datacol', data column in dataframe to perform predictions on, e.g. 'new_cases_smoothed'
      param: 'filterloc', location column in dataframe to perform predictions on, e.g. 'Sweden'
      param: 'add_input', additional location column in dataframe to use as input for predictions, e.g. 'Denmark'  
    """
    self.df_train = df_train
    self.df_test = df_test
    self.datacol = datacol
    self.filterloc = filterloc
    self.add_input = add_input
    self.num_xdim = None    

        
  def convert_to_numpy(self):
    """
      convert dataframe to numpy arrays. This process may takes a while.
    """
    # if no filter for location is specified
    if self.filterloc is None:
      x_train_np = np.array(self.df_train.orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
      x_test_np = np.array(self.df_test.orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
      y_train_np = np.array(self.df_train.orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())    
      y_test_np = np.array(self.df_test.orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())    
      num_xdim = 1      
      
    # if a filter for location is specified
    else:
      if self.add_input is None:
        x_train_np = np.array(self.df_train.filter(col("location") == self.filterloc).orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
        x_test_np = np.array(self.df_test.filter(col("location") == self.filterloc).orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
        num_xdim = 1        
     
      # if prediction should add additional input from e.g. a neighbouring country
      else: 
        x_train_time = np.array(self.df_train.filter(col("location") == self.filterloc).orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())
        x_test_time = np.array(self.df_test.filter(col("location") == self.filterloc).orderBy("time_idx").select("time_idx").rdd.map(lambda x: x[0]).collect())       
        x_train_add = np.array(self.df_train.filter(col("location") == self.add_input).orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())
        x_test_add = np.array(self.df_test.filter(col("location") == self.add_input).orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())    
        x_train = np.stack((x_train_time, x_train_add), axis=0)
        x_test = np.stack((x_test_time, x_test_add), axis=0)
        x_train_np = np.moveaxis(x_train, 1, 0)
        x_test_np = np.moveaxis(x_test, 1, 0)
        num_xdim = 2
                 
      # output data 
      y_train_np = np.array(self.df_train.filter(col("location") == self.filterloc).orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())
      y_test_np = np.array(self.df_test.filter(col("location") == self.filterloc).orderBy("time_idx").select(self.datacol).rdd.map(lambda x: x[0]).collect())
      
    self.x_train_np = x_train_np
    self.x_test_np = x_test_np
    self.y_train_np = y_train_np
    self.y_test_np = y_test_np
    self.num_xdim = num_xdim
      
  def plot_numpy_data(self):
    """ 
      plot numpy arrays 
    """   
    if self.num_xdim == 2:
      fig, (ax1, ax2) = plt.subplots(1,2, figsize=(12,6))
      ax1.plot(self.x_train_np[:,0], self.y_train_np, 'k*')
      ax1.legend(['train data'])
      ax1.set_xlabel('time [days]')
      ax1.set_ylabel('output')
      ax1.set_title('training data')
      ax1.grid()   
      ax2.plot(self.x_train_np[:,0], self.x_train_np[:,1], 'k*')
      ax2.legend(['train data'])
      ax2.set_xlabel('time [days]')
      ax2.set_ylabel('additional input')
      ax2.set_title('training data')
      ax2.grid()         
    else:
      fig, ax = plt.subplots(1,1, figsize=(12,6))
      ax.plot(self.x_train_np, self.y_train_np, 'k*')
      ax.legend(['train data'])
      ax.set_xlabel('time [days]')
      ax.set_ylabel('output')
      ax.set_title('training data')
      ax.grid()      
        
  def get_train_length(self):
      if self.num_xdim == 2:
        return len(self.x_train_np[:,0])
      else:
        return len(self.x_train_np)

  def process_numpy_data(self, nth_subsample = 4, window_red = 0.8):
    """
      reduction of data by subsampling data and reducing length of data window. 
    """
    assert window_red > 0 and window_red <= 1, "please adjust 'window_red' parameter to be between 0 and 1"
    start_idx = int((self.get_train_length())*window_red)
    self.x_train = th.tensor(self.x_train_np[start_idx::nth_subsample], dtype=th.float)
    self.x_test = th.tensor(self.x_test_np, dtype=th.float)
    self.y_train = th.tensor(self.y_train_np[start_idx::nth_subsample], dtype=th.float)
    self.y_test = th.tensor(self.y_test_np, dtype=th.float)    
    self.normalize()
    
  def set_time_to_zero(self):
    """
      sets the time vector to start at time zero
    """
    if self.num_xdim == 2:
      self.x_train_min = self.x_train[:,0].min()
      self.x_train[:,0] = self.x_train[:,0] - self.x_train_min
      self.x_test[:,0] = self.x_test[:,0] - self.x_train_min      
    else:
      self.x_train_min = self.x_train.min()
      self.x_train = self.x_train - self.x_train_min
      self.x_test = self.x_test - self.x_train_min
      
  def normalize(self):
    """
      normalize the data to improve predictions
    """
    self.set_time_to_zero()
    
    self.x_train_mean = self.x_train.mean()
    self.x_train_std = self.x_train.std()
    self.x_train = (self.x_train - self.x_train_mean) / self.x_train_std
    self.x_test = (self.x_test - self.x_train_mean) / self.x_train_std     

    self.y_train_mean = self.y_train.mean()
    self.y_train_std = self.y_train.std()
    self.y_train = (self.y_train - self.y_train_mean) / self.y_train_std
    self.y_test = (self.y_test - self.y_train_mean) / self.y_train_std 
    self.data_normalized = True

      
  def plot_reduced_data(self):
    """
      plots the reduced training data
    """
    with th.no_grad():      
      if self.num_xdim == 2:
        fig, (ax1, ax2) = plt.subplots(1,2, figsize=(12,6))
        ax1.plot(self.x_train[:,0], self.y_train, 'k*')
        ax1.legend(['train data'])
        ax1.set_xlabel('time [days]')
        ax1.set_ylabel('output')
        ax1.set_title('training data')
        ax1.grid()   
        ax2.plot(self.x_train[:,0], self.x_train[:,1], 'k*')
        ax2.legend(['train data'])
        ax2.set_xlabel('time [days]')
        ax2.set_ylabel('additional input')
        ax2.set_title('training data')
        ax2.grid()         
      else:
        fig, ax = plt.subplots(1,1, figsize=(12,6))
        ax.plot(self.x_train, self.y_train, 'k*')
        ax.legend(['train data'])
        ax.set_xlabel('time [days]')
        ax.set_ylabel('output')
        ax.set_title('training data')
        ax.grid()     
        
```

</div>

<div class="cell markdown">

Use class to convert dataframes to numpy arrays for further processing. Note, the conversion may take a while.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncworld = GPDataSet(df_ncworld_train, df_ncworld_test, datacol = 'new_cases_smoothed', filterloc = None, add_input=None)
ds_ndworld = GPDataSet(df_ndworld_train, df_ndworld_test, datacol = 'new_deaths_smoothed', filterloc = None, add_input=None)
ds_ncworld.convert_to_numpy()
ds_ndworld.convert_to_numpy()
```

</div>

<div class="cell markdown">

Plot

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncworld.plot_numpy_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_3.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ndworld.plot_numpy_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_4.JPG?raw=true)

</div>

<div class="cell markdown">

Process data by subsampling, reducing data window and normalize data. The gaussian process model is a so called non parametric model and will be mainly based on the data points. As such, to reduce the computation and the complexity of the model, we subsample and reduce the number of datapoints.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncworld.process_numpy_data(nth_subsample = 4, window_red = 0.8)
ds_ndworld.process_numpy_data(nth_subsample = 4, window_red = 0.8)
```

</div>

<div class="cell markdown">

Plot processed data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncworld.plot_reduced_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_5.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ndworld.plot_reduced_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_6.JPG?raw=true)

</div>

<div class="cell markdown">

Define gaussian process classes using Gpytorch and different kernels.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import gpytorch as gpth
class GPLinearRBF(gpth.models.ExactGP):
  def __init__(self, train_x, train_y, likelihood):
    super(GPLinearRBF, self).__init__(train_x, train_y, likelihood)
    self.mean_module = gpth.means.ConstantMean()
    self.covar_module = gpth.kernels.ScaleKernel(gpth.kernels.LinearKernel() + gpth.kernels.RBFKernel())
    
  def forward(self, x):
    x_mean = self.mean_module(x)
    x_covar = self.covar_module(x)
    return gpth.distributions.MultivariateNormal(x_mean, x_covar)    
  
class GPLinearMatern(gpth.models.ExactGP):
  def __init__(self, train_x, train_y, likelihood):
    super(GPLinearMatern, self).__init__(train_x, train_y, likelihood)
    self.mean_module = gpth.means.ConstantMean()
    self.covar_module = gpth.kernels.ScaleKernel(gpth.kernels.LinearKernel() + gpth.kernels.MaternKernel())
    
  def forward(self, x):
    x_mean = self.mean_module(x)
    x_covar = self.covar_module(x)
    return gpth.distributions.MultivariateNormal(x_mean, x_covar) 
```

</div>

<div class="cell markdown">

Define a training class for the Gaussian Process models

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import matplotlib.pyplot as plt
import math
from sklearn.metrics import mean_squared_error
from sklearn.metrics import mean_absolute_error

class GPTrainer():
  def __init__(self, gp_model, x_train, x_train_min, x_train_mean, x_train_std, x_test, y_train, y_test, y_train_mean, y_train_std, device='cpu', train_iter = 300, lr=0.1, verbose = True):
    """ 
      class to manage training and prediction of data
     
      param: 'gp_model', name of gaussian process model including kernel to use
      param: 'x_train', pytorch tensor (sequence, dim), normalized input training data, starting at time zero
      param: 'x_train_min', pytorch tensor, start time of input training data
      param: 'x_train_mean', pytorch tensor, mean used when normalizing input training data
      param: 'x_train_std', pytorch tensor, std deviation used when normalizing input training data
      param: 'x_test', pytorch tensor, normalized input test data, starting at time zero
      param: 'y_train', pytorch tensor, normalized output training data      
      param: 'y_train_mean', pytorch tensor, mean used when normalizing output training data
      param: 'y_train_std', pytorch tensor, std deviation used when normalizing output training data 
      param: 'y_test', pytorch tensor, normalized output test data     
      param: 'device', cpu or cuda. currently only tested for cpu.
      param: 'train_iter', number of training iterations to fit kernel parameters to data
      param: 'lr', learning rate
      param: 'verbose', print information such as loss during training
    """
    
    # data
    self.x_train = x_train.to(device)
    self.x_train_min = x_train_min
    self.x_train_mean = x_train_mean
    self.x_train_std = x_train_std    
    self.x_test = x_test.to(device)
    self.x_cat = th.cat((x_train,x_test),dim=0).to(device) 
    self.y_train = y_train.to(device)
    self.y_train_mean = y_train_mean
    self.y_train_std = y_train_std
    self.y_test = y_test.to(device)
    self.preds = None
    
    # define GP likelihood
    self.likelihood = gpth.likelihoods.GaussianLikelihood()    
    
    # GP model selection and init
    assert gp_model == 'GPLinearRBF' or 'GPLinearMatern', "Error: GP model selected is not defined"
    if gp_model == 'GPLinearRBF':
      self.model = GPLinearRBF(self.x_train, self.y_train, self.likelihood).to(device)
    if gp_model == 'GPLinearMatern':
      self.model = GPLinearMatern(self.x_train, self.y_train, self.likelihood).to(device)
      
    # training param
    self.train_iter = train_iter
    self.lr = lr
    self.device = device
    self.optimizer = th.optim.Adam(self.model.parameters(), lr=self.lr)
    self.loss_fn = gpth.mlls.ExactMarginalLogLikelihood(self.likelihood, self.model)   
    self.verbose = verbose
    
    # plots
    self.fig = None
    self.ax = None
      
  def train(self):
    """
      training of gaussian process model to fit kernel parameters to data
    """
    self.model.train()
    self.likelihood.train()
    
    for iter_idx in range(1,self.train_iter+1):
      self.optimizer.zero_grad()
      out = self.model(self.x_train)
      loss = -self.loss_fn(out, self.y_train).mean()
      loss.backward()
      self.optimizer.step()
      if iter_idx % 10 == 0 and self.verbose is True:
        print(f"Iter: {iter_idx}, train_loss: {loss.item()}")
        
  def prediction(self):
    """
      predict data
    """
    self.model.eval()
    self.likelihood.eval()
    with th.no_grad(): #, gpth.settings.fast_pred_var():  
      self.preds = self.likelihood(self.model(self.x_cat))
      
  def denormalize_y(self, data):
    """
      denormalize the output data
    """
    return data*self.y_train_std + self.y_train_mean
  
  def denormalize_x(self, data):
    """
      denormalize the input data
    """
    return data*self.x_train_std + self.x_train_mean  
      
  def plot(self):
    """
      plot the data
    """
    with th.no_grad():
      
      # extract time index dimension
      xdim = None
      try:
        _, xdim = self.x_train.shape
      except:
        pass
      if xdim == None or xdim == 1:
        x_train = self.denormalize_x(self.x_train)
        x_test = self.denormalize_x(self.x_test)
        x_cat = self.denormalize_x(self.x_cat)
      elif xdim > 1:
        x_train = self.denormalize_x(self.x_train)[:,0]
        x_test = self.denormalize_x(self.x_test)[:,0]
        x_cat = self.denormalize_x(self.x_cat)[:,0]
        
      # plot
      self.fig, self.ax = plt.subplots(1,1, figsize=(12,6))
      lower = self.denormalize_y(self.preds.mean - self.preds.variance.sqrt() * 1.96)
      upper = self.denormalize_y(self.preds.mean + self.preds.variance.sqrt() * 1.96)
      self.ax.plot(x_train.numpy()+self.x_train_min.numpy(), self.denormalize_y(self.y_train).numpy(), 'k*')
      self.ax.plot(x_test.numpy()+self.x_train_min.numpy(), self.denormalize_y(self.y_test).numpy(), 'r*')
      self.ax.plot(x_cat.numpy()+self.x_train_min.numpy(), self.denormalize_y(self.preds.mean).numpy(), 'b')
      self.ax.fill_between(x_cat.numpy()+self.x_train_min.numpy(), lower.numpy(), upper.numpy(), alpha=0.3)
      self.ax.legend(['train data', 'test data', 'predicted mean', 'predicted confidence 95%'])
      self.ax.set_xlabel('time [days]')
      self.ax.set_ylabel('prediction')
      self.ax.set_title('prediction')
      self.ax.grid()     
      
  def print_data_dim(self):
    """
      print shapes for debug purpose
    """
    print("data shapes:")
    print(f'x_train: {self.x_train.shape}')
    print(f'x_test: {self.x_test.shape}')
    print(f'x_cat: {self.x_cat.shape}')
    print(f'y_train: {self.y_train.shape}')
    print(f'y_test: {self.y_test.shape}')  
    try:
      print(f'preds mean: {self.preds.mean.shape}')
    except:
      pass

  def evaluate(self):
    """
      evaluation of predictions
    """
    with th.no_grad():
      # data to evaluate
      test_data = self.denormalize_y(self.y_test) 
      predictions = self.denormalize_y(self.preds.mean[-len(self.y_test):])
      
      # evaluate
      error_mse = mean_squared_error(test_data, predictions)
      error_rmse = math.sqrt(error_mse)
      error_abs = mean_absolute_error(test_data, predictions)
      avg_gt = test_data.sum() / len(test_data)
      mse_percentage = error_rmse / avg_gt * 100
      abs_percentage = error_abs / avg_gt * 100
      
      # print
      print('Average of groundtruth: %.3f' % avg_gt)
      print('Test MSE: %.3f' % error_mse)
      print('Test RMSE: %.3f' % error_rmse)
      print('RMSE percentage error: %.3f' % mse_percentage, '%')
      print('Test ABS: %.3f' % error_abs)
      print('ABS percentage error: %.3f' % abs_percentage, '%')      
```

</div>

<div class="cell markdown">

Init the training class for the Gaussian Process models

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncworld = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ncworld.x_train, x_train_min=ds_ncworld.x_train_min, x_train_mean=ds_ncworld.x_train_mean, x_train_std=ds_ncworld.x_train_std, x_test=ds_ncworld.x_test, y_train=ds_ncworld.y_train, y_test=ds_ncworld.y_test, y_train_mean=ds_ncworld.y_train_mean, y_train_std=ds_ncworld.y_train_std)

pred_ndworld = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ndworld.x_train, x_train_min=ds_ndworld.x_train_min, x_train_mean=ds_ndworld.x_train_mean, x_train_std=ds_ndworld.x_train_std, x_test=ds_ndworld.x_test, y_train=ds_ndworld.y_train, y_test=ds_ndworld.y_test, y_train_mean=ds_ndworld.y_train_mean, y_train_std=ds_ndworld.y_train_std)
```

</div>

<div class="cell markdown">

Training

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print('\ntraining new cases prediction model')
pred_ncworld.train()
print('\ntraining new deaths prediction model')
pred_ndworld.train()
```

</div>

<div class="cell markdown">

Prediction and plot

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncworld.prediction()
pred_ncworld.plot()
pred_ncworld.ax.set_ylabel('new cases smoothed')
pred_ncworld.ax.set_title('new cases smoothed')
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_7.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ndworld.prediction()
pred_ndworld.plot()
pred_ndworld.ax.set_ylabel('new deaths smoothed')
pred_ndworld.ax.set_title('new deaths smoothed')
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_8.JPG?raw=true)

</div>

<div class="cell markdown">

### 3.2 World onestep prediction

</div>

<div class="cell markdown">

To perform onestep ahead mean prediction, we define some additional functions

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def onestep_prediction(dataset):
  onestep = th.cat((dataset.y_train, dataset.y_test), dim=0) # output vector
  for idx in range(len(dataset.y_test)):
    
    # define training and test data. Training data is iteratively, step by step, expanded by the use of test data
    x_train = th.cat((dataset.x_train, dataset.x_test[:idx]), dim=0)
    x_test = dataset.x_test[idx:]
    y_train = th.cat((dataset.y_train, dataset.y_test[:idx]), dim=0)
    y_test = dataset.y_test[idx:]
    
    # create a gaussian process model, train and make predictions
    pred_model = GPTrainer(gp_model='GPLinearRBF', x_train=x_train, x_train_min=dataset.x_train_min, x_train_mean=dataset.x_train_mean, x_train_std=dataset.x_train_std, x_test=x_test, y_train=y_train, y_test=y_test, y_train_mean=dataset.y_train_mean, y_train_std=dataset.y_train_std, verbose=False)
    pred_model.train()
    pred_model.prediction()
    
    # store one step predictions
    onestep[len(dataset.y_train) + idx] = pred_model.preds.mean[len(dataset.x_train)+idx]

  # plot results
  fig, ax = plt.subplots(1,1, figsize=(12,6))
  ax.plot(pred_model.x_train_min + pred_model.denormalize_x(dataset.x_test), pred_model.denormalize_y(dataset.y_test),'*r', pred_model.x_train_min + pred_model.denormalize_x(dataset.x_test), pred_model.denormalize_y(onestep[len(dataset.y_train):]),'k*')
  ax.legend(['test data', 'prediction mean'])
  ax.set_xlabel('time [days]')
  ax.set_ylabel('prediction mean')
  ax.set_title('one step ahead prediction')
  ax.grid()   
  
  # return onestep prediction
  return onestep
```

</div>

<div class="cell markdown">

We iteratively predict the next one step ahead

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
onestep_pred_ncworld = onestep_prediction(ds_ncworld)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_99.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
onestep_pred_ndworld = onestep_prediction(ds_ndworld)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_9.JPG?raw=true)

</div>

<div class="cell markdown">

### 3.3 Sweden multistep prediction

Use class to convert dataframes to numpy arrays for further processing. Note, the conversion may take a while.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncswe = GPDataSet(df_ncdenswe_train, df_ncdenswe_test, datacol = 'new_cases_smoothed', filterloc = 'Sweden', add_input=None)
ds_ndswe = GPDataSet(df_nddenswe_train, df_nddenswe_test, datacol = 'new_deaths_smoothed', filterloc = 'Sweden', add_input=None)
ds_ncswe.convert_to_numpy()
ds_ndswe.convert_to_numpy()
```

</div>

<div class="cell markdown">

Plot data.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncswe.plot_numpy_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_10.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ndswe.plot_numpy_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_11.JPG?raw=true)

</div>

<div class="cell markdown">

Process data by subsampling, reducing data window and normalize data. The gaussian process model is a so called non parametric model and will be mainly based on the data points. As such, to reduce the computation and the complexity of the model, we subsample and reduce the number of datapoints.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncswe.process_numpy_data(nth_subsample = 4, window_red = 0.8)
ds_ndswe.process_numpy_data(nth_subsample = 4, window_red = 0.8)
```

</div>

<div class="cell markdown">

Plot processed data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncswe.plot_reduced_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_12.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ndswe.plot_reduced_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_13.JPG?raw=true)

</div>

<div class="cell markdown">

Init the training class for the Gaussian Process models

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncswe = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ncswe.x_train, x_train_min=ds_ncswe.x_train_min, x_train_mean=ds_ncswe.x_train_mean, x_train_std=ds_ncswe.x_train_std, x_test=ds_ncswe.x_test, y_train=ds_ncswe.y_train, y_test=ds_ncswe.y_test, y_train_mean=ds_ncswe.y_train_mean, y_train_std=ds_ncswe.y_train_std)

pred_ndswe = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ndswe.x_train, x_train_min=ds_ndswe.x_train_min, x_train_mean=ds_ncswe.x_train_mean, x_train_std=ds_ncswe.x_train_std, x_test=ds_ndswe.x_test, y_train=ds_ndswe.y_train, y_test=ds_ndswe.y_test, y_train_mean=ds_ndswe.y_train_mean, y_train_std=ds_ndswe.y_train_std)
```

</div>

<div class="cell markdown">

Training

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print('\ntraining new cases prediction model')
pred_ncswe.train()
print('\ntraining new deaths prediction model')
pred_ndswe.train()
```

</div>

<div class="cell markdown">

Prediction and plot

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncswe.prediction()
pred_ncswe.plot()
pred_ncswe.ax.set_ylabel('new cases smoothed')
pred_ncswe.ax.set_title('new cases smoothed')
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_14.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ndswe.prediction()
pred_ndswe.plot()
pred_ndswe.ax.set_ylabel('new deaths smoothed')
pred_ndswe.ax.set_title('new deaths smoothed')
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_15.JPG?raw=true)

</div>

<div class="cell markdown">

### 3.4 Sweden onestep prediction

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
onestep_pred_ncswe = onestep_prediction(ds_ncswe)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_199.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
onestep_pred_ndswe = onestep_prediction(ds_ndswe)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_16.JPG?raw=true)

</div>

<div class="cell markdown">

### 3.5 Sweden multistep prediction with additional data input from neighbouring country

Assuming we knew the results from a neighbouring country and if data is correlated, we could presumably improve the prediction

</div>

<div class="cell markdown">

Plot resulting data used for prediction. Both plots appears to follow a form of trend.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncswex = GPDataSet(df_ncdenswe_train, df_ncdenswe_test, datacol = 'new_cases_smoothed', filterloc = 'Sweden', add_input='Denmark')
ds_ndswex = GPDataSet(df_nddenswe_train, df_nddenswe_test, datacol = 'new_deaths_smoothed', filterloc = 'Sweden', add_input='Denmark')
ds_ncswex.convert_to_numpy()
ds_ndswex.convert_to_numpy()
```

</div>

<div class="cell markdown">

Plot data.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncswex.plot_numpy_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_17.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ndswex.plot_numpy_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_18.JPG?raw=true)

</div>

<div class="cell markdown">

Process data by subsampling, reducing data window and normalize data. The gaussian process model is a so called non parametric model and will be mainly based on the data points. As such, to reduce the computation and the complexity of the model, we subsample and reduce the number of datapoints.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncswex.process_numpy_data(nth_subsample = 4, window_red = 0.8)
ds_ndswex.process_numpy_data(nth_subsample = 4, window_red = 0.8)
```

</div>

<div class="cell markdown">

Plot processed data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ncswex.plot_reduced_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_19.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
ds_ndswex.plot_reduced_data()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_20.JPG?raw=true)

</div>

<div class="cell markdown">

Init the training class for the Gaussian Process models

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncswex = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ncswex.x_train, x_train_min=ds_ncswex.x_train_min, x_train_mean=ds_ncswex.x_train_mean, x_train_std=ds_ncswex.x_train_std, x_test=ds_ncswex.x_test, y_train=ds_ncswex.y_train, y_test=ds_ncswex.y_test, y_train_mean=ds_ncswex.y_train_mean, y_train_std=ds_ncswex.y_train_std)

pred_ndswex = GPTrainer(gp_model='GPLinearRBF', x_train=ds_ndswex.x_train, x_train_min=ds_ndswex.x_train_min, x_train_mean=ds_ndswex.x_train_mean, x_train_std=ds_ndswex.x_train_std, x_test=ds_ndswex.x_test, y_train=ds_ndswex.y_train, y_test=ds_ndswex.y_test, y_train_mean=ds_ndswex.y_train_mean, y_train_std=ds_ndswex.y_train_std)
```

</div>

<div class="cell markdown">

Training

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print('\ntraining new cases prediction model')
pred_ncswex.train()
print('\ntraining new deaths prediction model')
pred_ndswex.train()
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncswex.prediction()
pred_ncswex.plot()
pred_ncswex.ax.set_ylabel('new cases smoothed')
pred_ncswex.ax.set_title('new cases smoothed')
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_21.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ndswex.prediction()
pred_ndswex.plot()
pred_ndswex.ax.set_ylabel('new deaths smoothed')
pred_ndswex.ax.set_title('new deaths smoothed')
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_08_22.JPG?raw=true)

</div>

<div class="cell markdown">

4. Evaluation
-------------

</div>

<div class="cell markdown">

### 4.1 World multistep

</div>

<div class="cell markdown">

Evaluation of new cases smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncworld.evaluate()
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 554788.000
    Test MSE: 1850106112.000
    Test RMSE: 43012.860
    RMSE percentage error: 7.753 %
    Test ABS: 33457.289
    ABS percentage error: 6.031 %

</div>

</div>

<div class="cell markdown">

Evaluation of new deaths smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ndworld.evaluate()
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 8842.582
    Test MSE: 8629836.000
    Test RMSE: 2937.658
    RMSE percentage error: 33.222 %
    Test ABS: 2727.578
    ABS percentage error: 30.846 %

</div>

</div>

<div class="cell markdown">

### 4.2 World onestep

</div>

<div class="cell markdown">

To evaluate the onestep ahead prediction, we define an additional function

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
def evaluate(test_data, prediction):
  with th.no_grad():    
    # evaluate
    error_mse = mean_squared_error(test_data, prediction)
    error_rmse = math.sqrt(error_mse)
    error_abs = mean_absolute_error(test_data, prediction)
    avg_gt = test_data.sum() / len(test_data)
    mse_percentage = error_rmse / avg_gt * 100
    abs_percentage = error_abs / avg_gt * 100

    # print
    print('Average of groundtruth: %.3f' % avg_gt)
    print('Test MSE: %.3f' % error_mse)
    print('Test RMSE: %.3f' % error_rmse)
    print('RMSE percentage error: %.3f' % mse_percentage, '%')
    print('Test ABS: %.3f' % error_abs)
    print('ABS percentage error: %.3f' % abs_percentage, '%') 
```

</div>

<div class="cell markdown">

Evaluation of new cases smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# data to evaluate
test = pred_ncworld.denormalize_y(ds_ncworld.y_test) 
preds = pred_ncworld.denormalize_y(onestep_pred_ncworld[-len(ds_ncworld.y_test):])
evaluate(test, preds)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 554788.000
    Test MSE: 38076252.000
    Test RMSE: 6170.596
    RMSE percentage error: 1.112 %
    Test ABS: 4394.894
    ABS percentage error: 0.792 %

</div>

</div>

<div class="cell markdown">

Evaluation of new deaths smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# data to evaluate
test = pred_ndworld.denormalize_y(ds_ndworld.y_test) 
preds = pred_ndworld.denormalize_y(onestep_pred_ndworld[-len(ds_ndworld.y_test):])
evaluate(test, preds)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 8842.582
    Test MSE: 29195.436
    Test RMSE: 170.867
    RMSE percentage error: 1.932 %
    Test ABS: 137.978
    ABS percentage error: 1.560 %

</div>

</div>

<div class="cell markdown">

### 4.2 Sweden multistep

</div>

<div class="cell markdown">

Evaluation of new cases smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncswe.evaluate()
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 4238.664
    Test MSE: 4737463.500
    Test RMSE: 2176.572
    RMSE percentage error: 51.350 %
    Test ABS: 2068.833
    ABS percentage error: 48.809 %

</div>

</div>

<div class="cell markdown">

Evaluation of new deaths smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ndswe.evaluate()
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 26.254
    Test MSE: 755.362
    Test RMSE: 27.484
    RMSE percentage error: 104.686 %
    Test ABS: 22.457
    ABS percentage error: 85.540 %

</div>

</div>

<div class="cell markdown">

### 4.4 Sweden onestep

</div>

<div class="cell markdown">

Evaluation of new cases smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# data to evaluate
test = pred_ncswe.denormalize_y(ds_ncswe.y_test) 
preds = pred_ncswe.denormalize_y(onestep_pred_ncswe[-len(ds_ncswe.y_test):])
evaluate(test, preds)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 4238.664
    Test MSE: 77977.289
    Test RMSE: 279.244
    RMSE percentage error: 6.588 %
    Test ABS: 235.829
    ABS percentage error: 5.564 %

</div>

</div>

<div class="cell markdown">

Evaluation of new deaths smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# data to evaluate
test = pred_ndswe.denormalize_y(ds_ndswe.y_test) 
preds = pred_ndswe.denormalize_y(onestep_pred_ndswe[-len(ds_ndswe.y_test):])
evaluate(test, preds)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 26.254
    Test MSE: 33.230
    Test RMSE: 5.765
    RMSE percentage error: 21.957 %
    Test ABS: 3.928
    ABS percentage error: 14.963 %

</div>

</div>

<div class="cell markdown">

### 4.4 Sweden multistep with additional information

</div>

<div class="cell markdown">

Evaluation of new cases smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ncswex.evaluate()
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 4238.664
    Test MSE: 2658296.750
    Test RMSE: 1630.428
    RMSE percentage error: 38.466 %
    Test ABS: 1537.534
    ABS percentage error: 36.274 %

</div>

</div>

<div class="cell markdown">

Evaluation of new deaths smoothed

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
pred_ndswex.evaluate()
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 26.254
    Test MSE: 756.587
    Test RMSE: 27.506
    RMSE percentage error: 104.771 %
    Test ABS: 22.476
    ABS percentage error: 85.612 %

</div>

</div>

<div class="cell markdown">

5. Conclusions and reflections
------------------------------

</div>

<div class="cell markdown">

Predictions using gaussian processes were made for both new cases smoothed and new deaths smoothed. This included an aggregation of many countries within the world as well as for Sweden. Making single step ahead predictions resulted naturally in smaller errors compared to the multistep predictions. The multistep prediction for Sweden could be improved for new cases smoothed using correlated data from a neighbouring country.

We believe the Gaussian process model is a valuable tool for making predictions. With this work, we would like to highlight that the data points and kernel chosen for the Gaussian process heavily biases the model and strongly influences the predictions. In this project, we selected a combination of a linear kernel and a radial basis function. The reason being that there is a trend in the data and that nearby data points should be more similar than data points further away. By inspecting the data carefully, a more optimal kernel could likely be selected. Also, the confidence intervall provided with the gaussian process model is based on that the kernel is correctly representing the underlying distribution of data.

In terms of scalability, the predictions are somewhat scalable as a user can define a window of data for making the predictions. Furthermore, GPU support could be included and approximations to the gaussian process model could be made.

Compared to the ARIMA model, the gaussian process model performed in most cases slightly worse. However, this may be due to the selection of data points and kernel considering that the gaussian process model is heavily biased by these choices. One reflection is that if one approximately knows the distribution of the underlying data, a gaussian process model with a proper selected kernel may be a good choice.

</div>
