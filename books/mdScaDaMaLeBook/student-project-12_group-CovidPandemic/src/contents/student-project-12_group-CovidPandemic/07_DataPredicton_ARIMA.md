<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Prediction with Time Series model - ARIMA
-----------------------------------------

</div>

<div class="cell markdown">

In this notebook, we do prediction with the time series method - Autoregressive integrated moving average (ARIMA). We preprocess the data and prepare for prediction. Then we predicted the new cases (smoothed) and new deaths (smoothed) for world and Sweden. We predict the future value from the history value. After the prediction part we evaluated our results.

</div>

<div class="cell markdown">

1. Import data and preprocess
-----------------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// You need to uncomment this line if you haven't preprocess data yet.

%run "./02_DataPreprocess"
```

</div>

<div class="cell markdown">

2. Prepare for data
-------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_cleaned_time_series)
```

</div>

<div class="cell code" execution_count="1" scrolled="true">

``` scala
df_cleaned_time_series.printSchema
```

<div class="output execute_result plain_result" execution_count="1">

    root
     |-- iso_code: string (nullable = true)
     |-- continent: string (nullable = false)
     |-- location: string (nullable = true)
     |-- date: string (nullable = true)
     |-- total_cases: double (nullable = false)
     |-- new_cases: double (nullable = true)
     |-- new_cases_smoothed: double (nullable = false)
     |-- total_deaths: double (nullable = false)
     |-- new_deaths: double (nullable = true)
     |-- new_deaths_smoothed: double (nullable = false)
     |-- reproduction_rate: double (nullable = false)
     |-- icu_patients: double (nullable = true)
     |-- icu_patients_per_million: double (nullable = true)
     |-- hosp_patients: double (nullable = true)
     |-- hosp_patients_per_million: double (nullable = true)
     |-- weekly_icu_admissions: double (nullable = true)
     |-- weekly_icu_admissions_per_million: double (nullable = true)
     |-- weekly_hosp_admissions: double (nullable = true)
     |-- weekly_hosp_admissions_per_million: double (nullable = true)
     |-- total_tests: double (nullable = false)
     |-- new_tests: double (nullable = true)
     |-- total_tests_per_thousand: double (nullable = true)
     |-- new_tests_per_thousand: double (nullable = true)
     |-- new_tests_smoothed: double (nullable = true)
     |-- new_tests_smoothed_per_thousand: double (nullable = true)
     |-- tests_per_case: double (nullable = true)
     |-- positive_rate: double (nullable = true)
     |-- tests_units: double (nullable = true)
     |-- stringency_index: double (nullable = false)
     |-- population: double (nullable = true)
     |-- population_density: double (nullable = true)
     |-- median_age: double (nullable = true)
     |-- aged_65_older: double (nullable = true)
     |-- aged_70_older: double (nullable = true)
     |-- gdp_per_capita: double (nullable = true)
     |-- extreme_poverty: double (nullable = true)
     |-- cardiovasc_death_rate: double (nullable = true)
     |-- diabetes_prevalence: double (nullable = true)
     |-- female_smokers: double (nullable = true)
     |-- male_smokers: double (nullable = true)
     |-- handwashing_facilities: double (nullable = true)
     |-- hospital_beds_per_thousand: double (nullable = true)
     |-- life_expectancy: double (nullable = true)
     |-- human_development_index: double (nullable = true)
     |-- total_cases_per_million: double (nullable = true)
     |-- new_cases_per_million: double (nullable = true)
     |-- new_cases_smoothed_per_million: double (nullable = true)
     |-- total_deaths_per_million: double (nullable = true)
     |-- new_deaths_per_million: double (nullable = true)
     |-- new_deaths_smoothed_per_million: double (nullable = true)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// There is no "World" in the 126 countries. we need to calculate it.
val countries = df_cleaned_time_series.groupBy("location").count().sort($"location")
display(countries)
```

</div>

<div class="cell markdown">

### 2.1 Data for all over the world

</div>

<div class="cell markdown">

#### 2.1.1 The smoothed new cases of the world.

We use the smoothed new cases because the raw data fluctuates greatly by day - on workdays, there are more new cases than on weekends.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// prediction for all over the world
import org.apache.spark.sql.functions._
// val df_world = df_cleaned_time_series.withColumn("date", (col("date").cast("Timestamp"))).where("location == 'World'").select($"date",$"new_cases_smoothed")

val df_world = df_cleaned_time_series.groupBy("date").sum("new_cases_smoothed").sort(col("date")).withColumnRenamed("sum(new_cases_smoothed)","new_cases_smoothed")

display(df_world)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_1.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_world.printSchema
```

<div class="output execute_result plain_result" execution_count="1">

    root
     |-- date: string (nullable = true)
     |-- new_cases_smoothed: double (nullable = true)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_world.createOrReplaceTempView("df_world")
```

</div>

<div class="cell markdown">

#### 2.1.2 The smoothed new deaths of the world

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// val df_world_deaths = df_cleaned_time_series.withColumn("date", (col("date").cast("Timestamp"))).where("location == 'World'").select($"date",$"new_deaths_smoothed")
val df_world_deaths = df_cleaned_time_series.groupBy("date").sum("new_deaths_smoothed").sort(col("date")).withColumnRenamed("sum(new_deaths_smoothed)","new_deaths_smoothed")

df_world_deaths.createOrReplaceTempView("df_world_deaths")
```

<div class="output execute_result plain_result" execution_count="1">

    df_world_deaths: org.apache.spark.sql.DataFrame = [date: string, new_deaths_smoothed: double]

</div>

</div>

<div class="cell markdown">

### 2.2 Data for Sweden

</div>

<div class="cell markdown">

#### 2.2.1 The smoothed new cases of Sweden

In addition to the new cases all over the world, we also care about the cases in Sweden. Here we deal with smoothed new cases of Sweden.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Select one contry for prediction
import org.apache.spark.sql.functions._

val df_sw = df_cleaned_time_series.withColumn("date", (col("date").cast("Timestamp"))).where("location == 'Sweden'").select($"date",$"new_cases_smoothed")
display(df_sw)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_2.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_sw.printSchema
```

<div class="output execute_result plain_result" execution_count="1">

    root
     |-- date: timestamp (nullable = true)
     |-- new_cases_smoothed: double (nullable = false)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_sw.createOrReplaceTempView("df_sw")
```

</div>

<div class="cell markdown">

#### 2.2.2 The smoothed new deaths of Sweden

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_sw_deaths = df_cleaned_time_series.withColumn("date", (col("date").cast("Timestamp"))).where("location == 'Sweden'").select($"date",$"new_deaths_smoothed")
df_sw_deaths.createOrReplaceTempView("df_sw_deaths")
```

<div class="output execute_result plain_result" execution_count="1">

    df_sw_deaths: org.apache.spark.sql.DataFrame = [date: timestamp, new_deaths_smoothed: double]

</div>

</div>

<div class="cell markdown">

3. Time series regression with ARIMA
------------------------------------

ARIMA - Autoregressive Integrated Moving Average model. It's widely used in time series analysis. see defination here: https://en.wikipedia.org/wiki/Autoregressive*integrated*moving\_average

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# import some libraries
# dbutils.library.installPyPI('numpy','1.16.3')
# dbutils.library.installPyPI('pandas','1.1.5')
# dbutils.library.restartPython()
```

</div>

<div class="cell markdown">

### 3.1 Prediction for all over the world

</div>

<div class="cell markdown">

#### 3.1.1 Prediction of smoothed new cases (one\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import pandas
from matplotlib import pyplot
print(pandas.__version__)
data = spark.table("df_world")

print(type(data))
```

<div class="output execute_result plain_result" execution_count="1">

    1.0.1
    <class 'pyspark.sql.dataframe.DataFrame'>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from pyspark.sql.functions import *
from datetime import datetime
from pyspark.sql.functions import to_date, to_timestamp
data_pd = data.toPandas()
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import numpy as np
import pandas as pd
from statsmodels.tsa.arima_model import ARIMA
import sklearn
import statsmodels
from datetime import date
print(data_pd.columns)
data_pd['date'] = pd.to_datetime(data_pd['date'])
```

<div class="output execute_result plain_result" execution_count="1">

    Index(['date', 'new_cases_smoothed'], dtype='object')

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// # %python
// # data_pd.plot(x='date', y = 'new_cases_smoothed', figsize=(8,5))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_3.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import math
def Predict_by_ARIMA(data_pd, one_step = True, training_length = 0.9):
  data_pd1 = data_pd.set_index('date')
  X = data_pd1.values
  train_size = int(len(X) * training_length) #the length you need for training.
  train, test = X[0:train_size], X[train_size:len(X)]
  test_date = data_pd1.index[train_size:len(X)]
  history = [x for x in train]
  predictions = list()
  print("training_series_size: ", train_size)
  print("test_series_size: ", len(test))
  for t in range(len(test)):
    model = ARIMA(history, order=(2,1,0))
    model_fit = model.fit(disp=0)
    output = model_fit.forecast()
    yhat = output[0]
    predictions.append(yhat)
    if one_step:
      obs = test[t] # use real value, only predict next step
    else:
      obs = yhat # use predicted value, predict all test data
    history.append(obs)
    current_date = test_date[t]
    print(str(current_date.date()), 'pred=%f, gt=%f' % (yhat, obs))
  return test, predictions
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
test_world, predictions_world = Predict_by_ARIMA(data_pd, True, 0.9)
print("test size: ", len(test_world))
print("predicted size: ", len(predictions_world))
# plot
fig_world = pyplot.figure()  
pyplot.plot(test_world)
pyplot.plot(predictions_world, color='red')
pyplot.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_4.JPG?raw=true)

</div>

<div class="cell markdown">

#### 3.1.2 Prediction of smoothed new cases (multi\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
print(data_pd)
_, predictions_world_multi = Predict_by_ARIMA(data_pd, False)
print("test size: ", len(test_world))
print("predicted size: ", len(predictions_world))
# plot
fig_world_multi = pyplot.figure()  
pyplot.plot(test_world)
pyplot.plot(predictions_world_multi, color='red')
pyplot.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_5.JPG?raw=true)

</div>

<div class="cell markdown">

#### 3.1.3 Prediction of smoothed new deaths (one\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
data_world_death = spark.table("df_world_deaths")
data_world_death_pd = data_world_death.toPandas()
print(data_world_death_pd.columns)
data_world_death_pd['date'] = pd.to_datetime(data_world_death_pd['date'])
```

<div class="output execute_result plain_result" execution_count="1">

    Index(['date', 'new_deaths_smoothed'], dtype='object')

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
test_world_death, predictions_world_death = Predict_by_ARIMA(data_world_death_pd)
print("test size: ", len(test_world_death))
print("predicted size: ", len(predictions_world_death))
# plot
fig_world_death = pyplot.figure()  
pyplot.plot(test_world_death)
pyplot.plot(predictions_world_death, color='red')
pyplot.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_6.JPG?raw=true)

</div>

<div class="cell markdown">

#### 3.1.4 Prediction of smoothed new deaths (multi\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
_, predictions_world_death_multi = Predict_by_ARIMA(data_world_death_pd, False)
print("test size: ", len(test_world_death))
print("predicted size: ", len(predictions_world_death_multi))
# plot
fig_world_death = pyplot.figure()  
pyplot.plot(test_world_death)
pyplot.plot(predictions_world_death_multi, color='red')
pyplot.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_7.JPG?raw=true)

</div>

<div class="cell markdown">

### 3.2 Prediction for Sweden

</div>

<div class="cell markdown">

#### 3.2.1 Prediction of smoothed new cases

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
from datetime import datetime
from datetime import date
from matplotlib import pyplot
import numpy as np
import pandas as pd
from pyspark.sql.functions import to_date, to_timestamp
from pyspark.sql.functions import *
from statsmodels.tsa.arima_model import ARIMA
import sklearn
import statsmodels

data_sw = spark.table("df_sw")
data_sw_pd = data_sw.toPandas()
print(data_sw_pd.columns)
data_sw_pd['date'] = pd.to_datetime(data_sw_pd['date'])
```

<div class="output execute_result plain_result" execution_count="1">

    Index(['date', 'new_cases_smoothed'], dtype='object')

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
data_sw_pd.plot(x='date', y = 'new_cases_smoothed', figsize=(8,5))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_8.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
test_sw, predictions_sw = Predict_by_ARIMA(data_sw_pd)
print("test size: ", len(test_sw))
print("predicted size: ", len(predictions_sw))
# plot
fig_sw = pyplot.figure()  
pyplot.plot(test_sw)
pyplot.plot(predictions_sw, color='red')
pyplot.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_9.JPG?raw=true)

</div>

<div class="cell markdown">

#### 3.2.2 Prediction of smoothed new cases (multi\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
_, predictions_sw_multi = Predict_by_ARIMA(data_sw_pd, False)
print("test size: ", len(test_sw))
print("predicted size: ", len(predictions_sw))
# plot
fig_sw = pyplot.figure()  
pyplot.plot(test_sw)
pyplot.plot(predictions_sw_multi, color='red')
pyplot.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_10.JPG?raw=true)

</div>

<div class="cell markdown">

#### 3.2.3 Prediction of smoothed new deaths (one\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
data_sw_death = spark.table("df_sw_deaths")
data_sw_death_pd = data_sw_death.toPandas()
print(data_sw_death_pd.columns)
data_sw_death_pd['date'] = pd.to_datetime(data_sw_death_pd['date'])
```

<div class="output execute_result plain_result" execution_count="1">

    Index(['date', 'new_deaths_smoothed'], dtype='object')

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
test_sw_death, predictions_sw_death = Predict_by_ARIMA(data_sw_death_pd)
print("test size: ", len(test_sw_death))
print("predicted size: ", len(predictions_sw_death))
# plot
fig_sw_death = pyplot.figure()  
pyplot.plot(test_sw_death)
pyplot.plot(predictions_sw_death, color='red')
pyplot.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_11.JPG?raw=true)

</div>

<div class="cell markdown">

#### 3.2.4 Prediction of smoothed new deaths (multi\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
_, predictions_sw_death_multi = Predict_by_ARIMA(data_sw_death_pd, False)
print("test size: ", len(test_sw_death))
print("predicted size: ", len(predictions_sw_death_multi))
# plot
fig_sw_death = pyplot.figure()  
pyplot.plot(test_sw_death)
pyplot.plot(predictions_sw_death_multi, color='red')
pyplot.show()
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_07_12.JPG?raw=true)

</div>

<div class="cell markdown">

4. Evaluation
-------------

</div>

<div class="cell markdown">

### 4.1 Evaluation of world result

</div>

<div class="cell markdown">

#### 4.1.1 Evaluation of new cases smoothed (one\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
import math
from sklearn.metrics import mean_squared_error
from sklearn.metrics import mean_absolute_error

def Evaluation(test, predictions):
  error_mse = mean_squared_error(test, predictions)
  error_rmse = math.sqrt(error_mse)
  error_abs = mean_absolute_error(test, predictions)
  avg_gt = test[:,0].sum() / len(test)

  mse_percentage = error_rmse / avg_gt * 100
  abs_percentage = error_abs / avg_gt * 100
  print('Average of groundtruth: %.3f' % avg_gt)
  print('Test MSE: %.3f' % error_mse)
  print('Test RMSE: %.3f' % error_rmse)
  print('RMSE percentage error: %.3f' % mse_percentage, '%')
  print('Test ABS: %.3f' % error_abs)
  print('ABS percentage error: %.3f' % abs_percentage, '%')

Evaluation(test_world, predictions_world)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 763744.916
    Test MSE: 1382104096.548
    Test RMSE: 37176.661
    RMSE percentage error: 4.868 %
    Test ABS: 19644.392
    ABS percentage error: 2.572 %

</div>

</div>

<div class="cell markdown">

#### 4.1.2 Evaluation of new cases smoothed (multi\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
Evaluation(test_world, predictions_world_multi)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 763744.916
    Test MSE: 16372759781.212
    Test RMSE: 127956.085
    RMSE percentage error: 16.754 %
    Test ABS: 114514.607
    ABS percentage error: 14.994 %

</div>

</div>

<div class="cell markdown">

#### 4.1.3 Evaluation of new death smoothed (one\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
Evaluation(test_world_death, predictions_world_death)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 11412.091
    Test MSE: 358938.532
    Test RMSE: 599.115
    RMSE percentage error: 5.250 %
    Test ABS: 290.352
    ABS percentage error: 2.544 %

</div>

</div>

<div class="cell markdown">

#### 4.1.4 Evaluation of new death smoothed (multi\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
Evaluation(test_world_death, predictions_world_death_multi)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 11412.091
    Test MSE: 5567620.973
    Test RMSE: 2359.581
    RMSE percentage error: 20.676 %
    Test ABS: 2110.218
    ABS percentage error: 18.491 %

</div>

</div>

<div class="cell markdown">

### 4.2 Evaluation of Sweden results

</div>

<div class="cell markdown">

#### 4.2.1 Evaluation of new cases smoothed (one\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
Evaluation(test_sw, predictions_sw)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 4564.277
    Test MSE: 26457.989
    Test RMSE: 162.659
    RMSE percentage error: 3.564 %
    Test ABS: 82.412
    ABS percentage error: 1.806 %

</div>

</div>

<div class="cell markdown">

#### 4.2.2 Evaluation of new cases smoothed (multi\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
Evaluation(test_sw, predictions_sw_multi)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 4564.277
    Test MSE: 1514028.102
    Test RMSE: 1230.458
    RMSE percentage error: 26.958 %
    Test ABS: 1172.056
    ABS percentage error: 25.679 %

</div>

</div>

<div class="cell markdown">

#### 4.2.3 Evaluation of new death smoothed (one\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
Evaluation(test_sw_death, predictions_sw_death)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 31.862
    Test MSE: 24.933
    Test RMSE: 4.993
    RMSE percentage error: 15.672 %
    Test ABS: 3.072
    ABS percentage error: 9.643 %

</div>

</div>

<div class="cell markdown">

#### 4.2.4 Evaluation of new death smoothed (multi\_step)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
Evaluation(test_sw_death, predictions_sw_death_multi)
```

<div class="output execute_result plain_result" execution_count="1">

    Average of groundtruth: 31.862
    Test MSE: 775.703
    Test RMSE: 27.851
    RMSE percentage error: 87.414 %
    Test ABS: 25.094
    ABS percentage error: 78.759 %

</div>

</div>

<div class="cell markdown">

5. Conclusion and Reflections
-----------------------------

</div>

<div class="cell markdown">

With this time series method - ARIMA, we can get quite resonable results. We predicted the new cases (smoothed) and new deaths (smoothed) for world and Sweden. The evaluation of one step shows that we can get good results with a small error. But the multi step results is not good when we want to predict long term results. The prediction model and evaluation function can also been used for other countries.

</div>
