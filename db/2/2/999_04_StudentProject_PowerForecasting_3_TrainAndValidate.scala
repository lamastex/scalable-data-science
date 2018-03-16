// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/xqy5geCNKCg/0.jpg)](https://www.youtube.com/embed/xqy5geCNKCg?start=0&end=1456&autoplay=1)

// COMMAND ----------

// MAGIC %md 
// MAGIC # Power Forecasting
// MAGIC ## Student Project 
// MAGIC by [Gustav Björdal](https://www.linkedin.com/in/gustav-bj%C3%B6rdal-180461155/), [Mahmoud Shepero](https://www.linkedin.com/in/mahmoudshepero/) and [Dennis van der Meer](https://www.linkedin.com/in/dennis-van-der-meer-79463b94/)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Run the create-training-data-21 notebook to create all the data
// MAGIC   

// COMMAND ----------

// MAGIC %run /scalable-data-science/streaming-forecast/02-create-training-data

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #### Set up the vectorizer

// COMMAND ----------

import org.apache.spark.ml.feature.VectorAssembler

val vectorizer =  new VectorAssembler()
                      .setInputCols(
                        Array(
      "meanPower", "stddevPower",
      "meanWindDirection", "stddevWindDirection",
      "meanWindSpeed", "stddevWindSpeed", 
      "meanTemperature", "stddevTemperature", 
      "meanRH","stddevRH",
      "meanAP",  "stddevAP",
      "meanRainCumulative", "stddevRainCumulative",
      "meanRainDur", "stddevRainDur", 
      "meanRainIntens", "stddevRainIntens" ))
                      .setOutputCol("features")

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #### Train a linear regression model

// COMMAND ----------

// ***** LINEAR REGRESSION MODEL ****

import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.ml.Pipeline
import org.apache.spark.mllib.evaluation.RegressionMetrics 

// Let's initialize our linear regression learner
val lr = new LinearRegression()
// We use explain params to dump the parameters we can use
lr.explainParams()

// Now we set the parameters for the method
lr.setPredictionCol("Predicted_Power")
  .setLabelCol("future_power")
  .setMaxIter(100)
  .setRegParam(0.1)
// We will use the new spark.ml pipeline API. If you have worked with scikit-learn this will be very familiar.
val lrPipeline = new Pipeline()
lrPipeline.setStages(Array(vectorizer, lr))
// Let's first train on the entire dataset to see what we get
val lrModel = lrPipeline.fit(trainingSet)

val predictionsAndLabels = lrModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_Power", "future_power").map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])).rdd)

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2

println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")

// COMMAND ----------

// MAGIC %md 
// MAGIC Not too bad, but let's see if we can improve the results using cross validation.

// COMMAND ----------

// Copied from notebook 30

import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
import org.apache.spark.ml.evaluation._

//Let's set up our evaluator class to judge the model based on the best root mean squared error
val regEval = new RegressionEvaluator()
regEval.setLabelCol("future_power")
  .setPredictionCol("Predicted_Power")
  .setMetricName("rmse")

//Let's create our crossvalidator with 3 fold cross validation
val crossval = new CrossValidator()
crossval.setEstimator(lrPipeline)
crossval.setNumFolds(3)
crossval.setEvaluator(regEval)

val regParam = ((1 to 10) toArray).map(x => (x /100.0))

val paramGrid = new ParamGridBuilder()
                    .addGrid(lr.regParam, regParam)
                    .build()
crossval.setEstimatorParamMaps(paramGrid)

//Now let's create our model
val cvModel = crossval.fit(trainingSet)

val predictionsAndLabels = cvModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_Power", "future_power").rdd.map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])))

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ```
// MAGIC rmse: Double = 5.3684288676362915
// MAGIC explainedVariance: Double = 185.85849068387805
// MAGIC r2: Double = 0.8651582362729922
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #### Train a gradient boosted tree
// MAGIC 
// MAGIC This is basically just copied from notebook 20 (power plant pipeline)

// COMMAND ----------

import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
import org.apache.spark.ml.evaluation._
import org.apache.spark.ml.regression.GBTRegressor
import org.apache.spark.ml.Pipeline

val gbt = new GBTRegressor()
gbt.setLabelCol("future_power")
gbt.setPredictionCol("Predicted_Power")
gbt.setFeaturesCol("features")
gbt.setSeed(100088121L)
gbt.setMaxBins(100)
gbt.setMaxIter(120)

val gbtPipeline = new Pipeline()
gbtPipeline.setStages(Array(vectorizer, gbt))

val regEval = new RegressionEvaluator()
regEval.setLabelCol("future_power")
  .setPredictionCol("Predicted_Power")
  .setMetricName("rmse")

val crossval = new CrossValidator()
crossval.setNumFolds(3)
crossval.setEvaluator(regEval)
crossval.setEstimator(gbtPipeline)

val paramGrid = new ParamGridBuilder()
  .addGrid(gbt.maxDepth, Array(2, 3))
  .build()
crossval.setEstimatorParamMaps(paramGrid)

//gbt.explainParams
val gbtModel = crossval.fit(trainingSet)

// COMMAND ----------

import org.apache.spark.ml.regression.GBTRegressionModel 
import org.apache.spark.mllib.evaluation.RegressionMetrics 

val predictionsAndLabels = gbtModel.bestModel.transform(testSet)
val metrics = new RegressionMetrics(predictionsAndLabels.select("Predicted_Power", "future_power").map(r => (r(0).asInstanceOf[Double], r(1).asInstanceOf[Double])).rdd)

val rmse = metrics.rootMeanSquaredError
val explainedVariance = metrics.explainedVariance
val r2 = metrics.r2


println (f"Root Mean Squared Error: $rmse")
println (f"Explained Variance: $explainedVariance")  
println (f"R2: $r2")

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ```
// MAGIC rmse: Double = 2.863424384194055
// MAGIC explainedVariance: Double = 202.1087124482088
// MAGIC r2: Double = 0.961637980977275
// MAGIC ```

// COMMAND ----------

val results = predictionsAndLabels.withColumn("difference", $"Predicted_Power" - $"future_power").cache()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Displaying the results
// MAGIC ====
// MAGIC 
// MAGIC Note that the predicted power is 30 minutes into the future and you need to "visually shift" one of the lines

// COMMAND ----------

display(predictionsAndLabels)

// COMMAND ----------

