// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Prediction with Time Series model - ARIMA

// COMMAND ----------

// MAGIC %md
// MAGIC In this notebook, we do prediction with the time series method - Autoregressive integrated moving average (ARIMA).
// MAGIC We preprocess the data and prepare for prediction.
// MAGIC Then we predicted the new cases (smoothed) and new deaths (smoothed) for world and Sweden. We predict the future value from the history value. 
// MAGIC After the prediction part we evaluated our results.

// COMMAND ----------

// MAGIC %md
// MAGIC ## 1. Import data and preprocess

// COMMAND ----------

// You need to uncomment this line if you haven't preprocess data yet.

// %run "./02_DataPreprocess"

// COMMAND ----------

// MAGIC %md 
// MAGIC ## 2. Prepare for data

// COMMAND ----------

display(df_cleaned_time_series)

// COMMAND ----------

df_cleaned_time_series.printSchema

// COMMAND ----------

// There is no "World" in the 126 countries. we need to calculate it.
val countries = df_cleaned_time_series.groupBy("location").count().sort($"location")
display(countries)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2.1 Data for all over the world

// COMMAND ----------

// MAGIC %md
// MAGIC #### 2.1.1 The smoothed new cases of the world.
// MAGIC 
// MAGIC We use the smoothed new cases because the raw data fluctuates greatly by day - on workdays, there are more new cases than on weekends.

// COMMAND ----------

// prediction for all over the world
import org.apache.spark.sql.functions._
// val df_world = df_cleaned_time_series.withColumn("date", (col("date").cast("Timestamp"))).where("location == 'World'").select($"date",$"new_cases_smoothed")

val df_world = df_cleaned_time_series.groupBy("date").sum("new_cases_smoothed").sort(col("date")).withColumnRenamed("sum(new_cases_smoothed)","new_cases_smoothed")

display(df_world)

// COMMAND ----------

df_world.printSchema

// COMMAND ----------

df_world.createOrReplaceTempView("df_world")

// COMMAND ----------

// MAGIC %md
// MAGIC #### 2.1.2 The smoothed new deaths of the world

// COMMAND ----------

// val df_world_deaths = df_cleaned_time_series.withColumn("date", (col("date").cast("Timestamp"))).where("location == 'World'").select($"date",$"new_deaths_smoothed")
val df_world_deaths = df_cleaned_time_series.groupBy("date").sum("new_deaths_smoothed").sort(col("date")).withColumnRenamed("sum(new_deaths_smoothed)","new_deaths_smoothed")

df_world_deaths.createOrReplaceTempView("df_world_deaths")

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2.2 Data for Sweden

// COMMAND ----------

// MAGIC %md
// MAGIC #### 2.2.1 The smoothed new cases of Sweden
// MAGIC 
// MAGIC In addition to the new cases all over the world, we also care about the cases in Sweden. Here we deal with smoothed new cases of Sweden.

// COMMAND ----------

// Select one contry for prediction
import org.apache.spark.sql.functions._

val df_sw = df_cleaned_time_series.withColumn("date", (col("date").cast("Timestamp"))).where("location == 'Sweden'").select($"date",$"new_cases_smoothed")
display(df_sw)

// COMMAND ----------

df_sw.printSchema

// COMMAND ----------

df_sw.createOrReplaceTempView("df_sw")

// COMMAND ----------

// MAGIC %md
// MAGIC #### 2.2.2 The smoothed new deaths of Sweden

// COMMAND ----------

val df_sw_deaths = df_cleaned_time_series.withColumn("date", (col("date").cast("Timestamp"))).where("location == 'Sweden'").select($"date",$"new_deaths_smoothed")
df_sw_deaths.createOrReplaceTempView("df_sw_deaths")

// COMMAND ----------

// MAGIC %md
// MAGIC ## 3. Time series regression with ARIMA
// MAGIC 
// MAGIC ARIMA - Autoregressive Integrated Moving Average model. It's widely used in time series analysis. see defination here: https://en.wikipedia.org/wiki/Autoregressive_integrated_moving_average

// COMMAND ----------

// MAGIC %python
// MAGIC # import some libraries
// MAGIC # dbutils.library.installPyPI('numpy','1.16.3')
// MAGIC # dbutils.library.installPyPI('pandas','1.1.5')
// MAGIC # dbutils.library.restartPython()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3.1 Prediction for all over the world

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3.1.1 Prediction of smoothed new cases (one_step)

// COMMAND ----------

// MAGIC %python
// MAGIC import pandas
// MAGIC from matplotlib import pyplot
// MAGIC print(pandas.__version__)
// MAGIC data = spark.table("df_world")
// MAGIC 
// MAGIC print(type(data))

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC from pyspark.sql.functions import *
// MAGIC from datetime import datetime
// MAGIC from pyspark.sql.functions import to_date, to_timestamp
// MAGIC data_pd = data.toPandas()

// COMMAND ----------

// MAGIC %python
// MAGIC import numpy as np
// MAGIC import pandas as pd
// MAGIC from statsmodels.tsa.arima_model import ARIMA
// MAGIC import sklearn
// MAGIC import statsmodels
// MAGIC from datetime import date
// MAGIC print(data_pd.columns)
// MAGIC data_pd['date'] = pd.to_datetime(data_pd['date'])

// COMMAND ----------

// MAGIC %python
// MAGIC data_pd.plot(x='date', y = 'new_cases_smoothed', figsize=(8,5))

// COMMAND ----------

// MAGIC %python
// MAGIC import math
// MAGIC def Predict_by_ARIMA(data_pd, one_step = True, training_length = 0.9):
// MAGIC   data_pd1 = data_pd.set_index('date')
// MAGIC   X = data_pd1.values
// MAGIC   train_size = int(len(X) * training_length) #the length you need for training.
// MAGIC   train, test = X[0:train_size], X[train_size:len(X)]
// MAGIC   test_date = data_pd1.index[train_size:len(X)]
// MAGIC   history = [x for x in train]
// MAGIC   predictions = list()
// MAGIC   print("training_series_size: ", train_size)
// MAGIC   print("test_series_size: ", len(test))
// MAGIC   for t in range(len(test)):
// MAGIC     model = ARIMA(history, order=(2,1,0))
// MAGIC     model_fit = model.fit(disp=0)
// MAGIC     output = model_fit.forecast()
// MAGIC     yhat = output[0]
// MAGIC     predictions.append(yhat)
// MAGIC     if one_step:
// MAGIC       obs = test[t] # use real value, only predict next step
// MAGIC     else:
// MAGIC       obs = yhat # use predicted value, predict all test data
// MAGIC     history.append(obs)
// MAGIC     current_date = test_date[t]
// MAGIC     print(str(current_date.date()), 'pred=%f, gt=%f' % (yhat, obs))
// MAGIC   return test, predictions

// COMMAND ----------

// MAGIC %python
// MAGIC test_world, predictions_world = Predict_by_ARIMA(data_pd, True, 0.9)
// MAGIC print("test size: ", len(test_world))
// MAGIC print("predicted size: ", len(predictions_world))
// MAGIC # plot
// MAGIC fig_world = pyplot.figure()  
// MAGIC pyplot.plot(test_world)
// MAGIC pyplot.plot(predictions_world, color='red')
// MAGIC pyplot.show()

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3.1.2 Prediction of smoothed new cases (multi_step)

// COMMAND ----------

// MAGIC %python
// MAGIC print(data_pd)
// MAGIC _, predictions_world_multi = Predict_by_ARIMA(data_pd, False)
// MAGIC print("test size: ", len(test_world))
// MAGIC print("predicted size: ", len(predictions_world))
// MAGIC # plot
// MAGIC fig_world_multi = pyplot.figure()  
// MAGIC pyplot.plot(test_world)
// MAGIC pyplot.plot(predictions_world_multi, color='red')
// MAGIC pyplot.show()

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3.1.3 Prediction of smoothed new deaths (one_step)

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC data_world_death = spark.table("df_world_deaths")
// MAGIC data_world_death_pd = data_world_death.toPandas()
// MAGIC print(data_world_death_pd.columns)
// MAGIC data_world_death_pd['date'] = pd.to_datetime(data_world_death_pd['date'])

// COMMAND ----------

// MAGIC %python
// MAGIC test_world_death, predictions_world_death = Predict_by_ARIMA(data_world_death_pd)
// MAGIC print("test size: ", len(test_world_death))
// MAGIC print("predicted size: ", len(predictions_world_death))
// MAGIC # plot
// MAGIC fig_world_death = pyplot.figure()  
// MAGIC pyplot.plot(test_world_death)
// MAGIC pyplot.plot(predictions_world_death, color='red')
// MAGIC pyplot.show()

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3.1.4 Prediction of smoothed new deaths (multi_step)

// COMMAND ----------

// MAGIC %python
// MAGIC _, predictions_world_death_multi = Predict_by_ARIMA(data_world_death_pd, False)
// MAGIC print("test size: ", len(test_world_death))
// MAGIC print("predicted size: ", len(predictions_world_death_multi))
// MAGIC # plot
// MAGIC fig_world_death = pyplot.figure()  
// MAGIC pyplot.plot(test_world_death)
// MAGIC pyplot.plot(predictions_world_death_multi, color='red')
// MAGIC pyplot.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3.2 Prediction for Sweden

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3.2.1 Prediction of smoothed new cases

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC from datetime import datetime
// MAGIC from datetime import date
// MAGIC from matplotlib import pyplot
// MAGIC import numpy as np
// MAGIC import pandas as pd
// MAGIC from pyspark.sql.functions import to_date, to_timestamp
// MAGIC from pyspark.sql.functions import *
// MAGIC from statsmodels.tsa.arima_model import ARIMA
// MAGIC import sklearn
// MAGIC import statsmodels
// MAGIC 
// MAGIC data_sw = spark.table("df_sw")
// MAGIC data_sw_pd = data_sw.toPandas()
// MAGIC print(data_sw_pd.columns)
// MAGIC data_sw_pd['date'] = pd.to_datetime(data_sw_pd['date'])

// COMMAND ----------

// MAGIC %python
// MAGIC data_sw_pd.plot(x='date', y = 'new_cases_smoothed', figsize=(8,5))

// COMMAND ----------

// MAGIC %python
// MAGIC test_sw, predictions_sw = Predict_by_ARIMA(data_sw_pd)
// MAGIC print("test size: ", len(test_sw))
// MAGIC print("predicted size: ", len(predictions_sw))
// MAGIC # plot
// MAGIC fig_sw = pyplot.figure()  
// MAGIC pyplot.plot(test_sw)
// MAGIC pyplot.plot(predictions_sw, color='red')
// MAGIC pyplot.show()

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3.2.2 Prediction of smoothed new cases (multi_step)

// COMMAND ----------

// MAGIC %python
// MAGIC _, predictions_sw_multi = Predict_by_ARIMA(data_sw_pd, False)
// MAGIC print("test size: ", len(test_sw))
// MAGIC print("predicted size: ", len(predictions_sw))
// MAGIC # plot
// MAGIC fig_sw = pyplot.figure()  
// MAGIC pyplot.plot(test_sw)
// MAGIC pyplot.plot(predictions_sw_multi, color='red')
// MAGIC pyplot.show()

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3.2.3 Prediction of smoothed new deaths (one_step)

// COMMAND ----------

// MAGIC %python
// MAGIC data_sw_death = spark.table("df_sw_deaths")
// MAGIC data_sw_death_pd = data_sw_death.toPandas()
// MAGIC print(data_sw_death_pd.columns)
// MAGIC data_sw_death_pd['date'] = pd.to_datetime(data_sw_death_pd['date'])

// COMMAND ----------

// MAGIC %python
// MAGIC test_sw_death, predictions_sw_death = Predict_by_ARIMA(data_sw_death_pd)
// MAGIC print("test size: ", len(test_sw_death))
// MAGIC print("predicted size: ", len(predictions_sw_death))
// MAGIC # plot
// MAGIC fig_sw_death = pyplot.figure()  
// MAGIC pyplot.plot(test_sw_death)
// MAGIC pyplot.plot(predictions_sw_death, color='red')
// MAGIC pyplot.show()

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3.2.4 Prediction of smoothed new deaths (multi_step)

// COMMAND ----------

// MAGIC %python
// MAGIC _, predictions_sw_death_multi = Predict_by_ARIMA(data_sw_death_pd, False)
// MAGIC print("test size: ", len(test_sw_death))
// MAGIC print("predicted size: ", len(predictions_sw_death_multi))
// MAGIC # plot
// MAGIC fig_sw_death = pyplot.figure()  
// MAGIC pyplot.plot(test_sw_death)
// MAGIC pyplot.plot(predictions_sw_death_multi, color='red')
// MAGIC pyplot.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ## 4. Evaluation

// COMMAND ----------

// MAGIC %md
// MAGIC ### 4.1 Evaluation of world result

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4.1.1 Evaluation of new cases smoothed (one_step)

// COMMAND ----------

// MAGIC %python
// MAGIC import math
// MAGIC from sklearn.metrics import mean_squared_error
// MAGIC from sklearn.metrics import mean_absolute_error
// MAGIC 
// MAGIC def Evaluation(test, predictions):
// MAGIC   error_mse = mean_squared_error(test, predictions)
// MAGIC   error_rmse = math.sqrt(error_mse)
// MAGIC   error_abs = mean_absolute_error(test, predictions)
// MAGIC   avg_gt = test[:,0].sum() / len(test)
// MAGIC 
// MAGIC   mse_percentage = error_rmse / avg_gt * 100
// MAGIC   abs_percentage = error_abs / avg_gt * 100
// MAGIC   print('Average of groundtruth: %.3f' % avg_gt)
// MAGIC   print('Test MSE: %.3f' % error_mse)
// MAGIC   print('Test RMSE: %.3f' % error_rmse)
// MAGIC   print('RMSE percentage error: %.3f' % mse_percentage, '%')
// MAGIC   print('Test ABS: %.3f' % error_abs)
// MAGIC   print('ABS percentage error: %.3f' % abs_percentage, '%')
// MAGIC 
// MAGIC Evaluation(test_world, predictions_world)

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4.1.2 Evaluation of new cases smoothed (multi_step)

// COMMAND ----------

// MAGIC %python
// MAGIC Evaluation(test_world, predictions_world_multi)

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4.1.3 Evaluation of new death smoothed (one_step)

// COMMAND ----------

// MAGIC %python
// MAGIC Evaluation(test_world_death, predictions_world_death)

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4.1.4 Evaluation of new death smoothed (multi_step)

// COMMAND ----------

// MAGIC %python
// MAGIC Evaluation(test_world_death, predictions_world_death_multi)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 4.2 Evaluation of Sweden results

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4.2.1 Evaluation of new cases smoothed (one_step)

// COMMAND ----------

// MAGIC %python
// MAGIC Evaluation(test_sw, predictions_sw)

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4.2.2 Evaluation of new cases smoothed (multi_step)

// COMMAND ----------

// MAGIC %python
// MAGIC Evaluation(test_sw, predictions_sw_multi)

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4.2.3 Evaluation of new death smoothed (one_step)

// COMMAND ----------

// MAGIC %python
// MAGIC Evaluation(test_sw_death, predictions_sw_death)

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4.2.4 Evaluation of new death smoothed (multi_step)

// COMMAND ----------

// MAGIC %python
// MAGIC Evaluation(test_sw_death, predictions_sw_death_multi)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 5. Conclusion and Reflections

// COMMAND ----------

// MAGIC %md
// MAGIC With this time series method - ARIMA, we can get quite resonable results. 
// MAGIC We predicted the new cases (smoothed) and new deaths (smoothed) for world and Sweden. 
// MAGIC The evaluation of one step shows that we can get good results with a small error. 
// MAGIC But the multi step results is not good when we want to predict long term results. 
// MAGIC The prediction model and evaluation function can also been used for other countries.