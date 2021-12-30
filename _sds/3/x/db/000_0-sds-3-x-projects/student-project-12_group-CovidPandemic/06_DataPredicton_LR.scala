// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Prediction with Linear Regression (LR) Model

// COMMAND ----------

// MAGIC %md
// MAGIC In this model, we use scala to process the data and predict the total cases.
// MAGIC In this data set, there are many features which are constant for each country and don’t change with time. So, we tried to predict the total cases on a selected date, from some countries to other countries, without considering the time series.

// COMMAND ----------

// MAGIC %md
// MAGIC ## 1. Import data and preprocess

// COMMAND ----------

// You need to uncomment this line if you haven't preprocess data yet.

// %run "./02_DataPreprocess"

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. Data process

// COMMAND ----------

display(df_cleaned_time_series)

// COMMAND ----------

df_cleaned_time_series.printSchema

// COMMAND ----------

// MAGIC %scala
// MAGIC import org.apache.spark.sql.functions._
// MAGIC 
// MAGIC for (c <- df_cleaned_time_series.columns) {
// MAGIC   println(c + ": " + df_cleaned_time_series.filter(col(c).isNull).count())
// MAGIC }

// COMMAND ----------

// MAGIC %md
// MAGIC Prepare the data for training. We choose a day we want to predict, and select the constant features, and select the target column for prediction. 

// COMMAND ----------

val df_by_location = df_cleaned_time_series.filter($"date" === "2020-12-01").sort($"continent").select($"iso_code",$"stringency_index", $"population",$"population_density",$"gdp_per_capita",$"diabetes_prevalence",$"total_cases_per_million",$"total_cases")
display(df_by_location)

// COMMAND ----------

df_by_location.count()

// COMMAND ----------

// MAGIC %md
// MAGIC Rescale the feature values and the target value.

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.Column
val min_str_index = df_by_location.select(min($"stringency_index")).first()(0)
val max_str_index = df_by_location.select(max($"stringency_index")).first()(0)
val min_population = df_by_location.select(min($"population")).first()(0)
val max_population = df_by_location.select(max($"population")).first()(0)
val min_population_density = 
df_by_location.select(min($"population_density")).first()(0)
val max_population_density = 
df_by_location.select(max($"population_density")).first()(0)
val min_gdp_per_capita = df_by_location.select(min($"gdp_per_capita")).first()(0)
val max_gdp_per_capita = df_by_location.select(max($"gdp_per_capita")).first()(0)
val min_diabetes_prevalence = 
df_by_location.select(min($"diabetes_prevalence")).first()(0)
val max_diabetes_prevalence = df_by_location.select(max($"diabetes_prevalence")).first()(0)

val df_by_location_normalized = df_by_location
  .withColumn("normal_stringency_index",($"stringency_index" -lit(min_str_index))/(lit(max_str_index)-lit(min_str_index)))
  .withColumn("normal_population", ($"population" - lit(min_population))/(lit(max_population)-lit(min_population)))
  .withColumn("normal_population_density",($"population_density" - lit(min_population_density))/(lit(max_population_density) - lit(min_population_density)))
  .withColumn("normal_gdp_per_capita", ($"gdp_per_capita" - lit(min_gdp_per_capita))/(lit(max_gdp_per_capita)- lit(min_gdp_per_capita)))
  .withColumn("normal_diabetes_prevalence", ($"diabetes_prevalence" - lit(min_diabetes_prevalence))/lit(max_diabetes_prevalence) - lit(min_diabetes_prevalence)).withColumn("log_total_cases_per_million", log($"total_cases_per_million")).toDF
display(df_by_location_normalized)

// COMMAND ----------

df_by_location_normalized.printSchema

// COMMAND ----------

val df_by_location_normalized_selected = df_by_location_normalized.select($"normal_stringency_index",$"normal_population",$"normal_population_density",$"normal_gdp_per_capita", $"normal_diabetes_prevalence",$"log_total_cases_per_million")

// COMMAND ----------

display(df_by_location_normalized_selected)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 3. Linear Regression from selected value to new cases
// MAGIC 
// MAGIC 
// MAGIC These values are irrelevant to time, but relevant to country. So we try to predict the total case in some contries from the data in other contries.

// COMMAND ----------

df_by_location_normalized_selected.createOrReplaceTempView("covid_table")

// COMMAND ----------

import org.apache.spark.ml.feature.VectorAssembler

val vectorizer =  new VectorAssembler()
.setInputCols(Array("normal_stringency_index", "normal_population", "normal_population_density", "normal_gdp_per_capita", "normal_diabetes_prevalence"))
.setOutputCol("features")

// make a DataFrame called dataset from the table
val dataset = vectorizer.transform(df_by_location_normalized_selected).select("features","log_total_cases_per_million") 


// COMMAND ----------

display(dataset)

// COMMAND ----------

var Array(split20, split80) = dataset.randomSplit(Array(0.20, 0.80), 1800009193L)

// COMMAND ----------

val testSet = split20.cache()

val trainingSet = split80.cache()

// COMMAND ----------

testSet.count() // action to actually cache

// COMMAND ----------

trainingSet.count() // action to actually cache

// COMMAND ----------

import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.ml.Pipeline

// Let's initialize our linear regression learner
val lr = new LinearRegression()
// We use explain params to dump the parameters we can use
lr.explainParams()
// Now we set the parameters for the method
lr.setPredictionCol("prediction")
  .setLabelCol("log_total_cases_per_million")
  .setMaxIter(100)
  .setRegParam(0.1)
val lrModel = lr.fit(trainingSet)

// COMMAND ----------

val trainingSummary = lrModel.summary

println(s"Coefficients: ${lrModel.coefficients}, Intercept: ${lrModel.intercept}")
println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")


// COMMAND ----------

import org.apache.spark.ml.evaluation.RegressionEvaluator

// make predictions on the test data
val predictions = lrModel.transform(testSet)
predictions.select("prediction", "log_total_cases_per_million", "features").show()

// select (prediction, true label) and compute test error.
val evaluator = new RegressionEvaluator()
  .setLabelCol("log_total_cases_per_million")
  .setPredictionCol("prediction")
  .setMetricName("rmse")
val rmse = evaluator.evaluate(predictions)
println(s"Root Mean Squared Error (RMSE) on test data = $rmse")

// COMMAND ----------

val predictions = lrModel.transform(testSet)
display(predictions)


// COMMAND ----------

val new_predictions = predictions.withColumn("new_prediction", exp($"prediction")).withColumn("total_cases_per_million",exp($"log_total_cases_per_million")).select("new_prediction", "total_cases_per_million", "features")
display(new_predictions)

// COMMAND ----------

// select (prediction, true label) and compute test error.
val evaluator = new RegressionEvaluator()
  .setLabelCol("total_cases_per_million")
  .setPredictionCol("new_prediction")
  .setMetricName("rmse")
val rmse = evaluator.evaluate(new_predictions)
println("Root Mean Squared Error (RMSE) on test data = $rmse")

// COMMAND ----------

// MAGIC %md
// MAGIC ## 4. Conclusion and Reflections

// COMMAND ----------

// MAGIC %md
// MAGIC We've tried several ways to preprocess the consant feature, but still didn't get a good result.
// MAGIC We came to the conclusion that only predict the total cases of a country from other countries without considering the history time series values are not resonable. This is because the constant feature columns cannot reflect the total cases well. Therefore, we decided to use some time series methods to predict the value from the history value.