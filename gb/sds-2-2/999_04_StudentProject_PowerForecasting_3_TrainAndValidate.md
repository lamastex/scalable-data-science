[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Archived YouTube video of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/xqy5geCNKCg/0.jpg)](https://www.youtube.com/embed/xqy5geCNKCg?start=0&end=1456&autoplay=1)

Power Forecasting
=================

Student Project
---------------

by [Gustav Björdal](https://www.linkedin.com/in/gustav-bj%C3%B6rdal-180461155/), [Mahmoud Shepero](https://www.linkedin.com/in/mahmoudshepero/) and [Dennis van der Meer](https://www.linkedin.com/in/dennis-van-der-meer-79463b94/)

### Run the create-training-data-21 notebook to create all the data

``` run
/scalable-data-science/streaming-forecast/02-create-training-data
```

| path                                                | name             | size        |
|-----------------------------------------------------|------------------|-------------|
| dbfs:/FileStore/tables/forecasting/clean\_data.json | clean\_data.json | 7.1327975e7 |

>     inputPath: String = /FileStore/tables/forecasting/
>     import org.apache.spark.sql.types._
>     jsonSchema: org.apache.spark.sql.types.StructType = StructType(StructField(ID,StringType,true), StructField(timeStamp,TimestampType,true), StructField(DataList,StructType(StructField(WXT530,StructType(StructField(DN,StringType,true), StructField(SN,StringType,true), StructField(GT3U,StringType,true), StructField(GM41,StringType,true), StructField(GP41,StringType,true), StructField(RC,StringType,true), StructField(RD,StringType,true), StructField(RI,StringType,true)),true), StructField(MX41,StructType(StructField(P,StringType,true)),true)),true))

>     DF: org.apache.spark.sql.DataFrame = [ID: string, timeStamp: timestamp ... 1 more field]
>     res67: Long = 367965

>     FinalDF: org.apache.spark.sql.DataFrame = [Power: double, WindDirection: double ... 8 more fields]

>     import org.apache.spark.sql.functions.{lead, lag}
>     w: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@7876017d
>     leadDf: org.apache.spark.sql.DataFrame = [Power: double, WindDirection: double ... 10 more fields]

>     import org.apache.spark.sql.{DataFrame, SparkSession}
>     import org.apache.spark.sql.functions._
>     averagedData: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 8 more fields]

>     import org.apache.spark.sql.expressions.Window
>     import org.apache.spark.sql.functions._
>     windowSize: Int = 60
>     wSpec1: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@6e7540e

>     dataFeatDF: org.apache.spark.sql.DataFrame = [window: struct<start: timestamp, end: timestamp>, Power: double ... 62 more fields]

>     Size of dataset: 61473

>     w: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@5599f9e6
>     leadSteps: Int = 120
>     dataset: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]

>     split20: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]
>     split80: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]

>     testSet: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]
>     trainingSet: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]

#### Set up the vectorizer

``` scala
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
```

>     import org.apache.spark.ml.feature.VectorAssembler
>     vectorizer: org.apache.spark.ml.feature.VectorAssembler = vecAssembler_d02669c9129a

#### Train a linear regression model

``` scala
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
```

>     Root Mean Squared Error: 5.371102763373557
>     Explained Variance: 183.27403720831734
>     R2: 0.8650238794181951
>     import org.apache.spark.ml.regression.LinearRegression
>     import org.apache.spark.ml.regression.LinearRegressionModel
>     import org.apache.spark.ml.Pipeline
>     import org.apache.spark.mllib.evaluation.RegressionMetrics
>     lr: org.apache.spark.ml.regression.LinearRegression = linReg_d056f664f4f3
>     lrPipeline: org.apache.spark.ml.Pipeline = pipeline_471de568ed2e
>     lrModel: org.apache.spark.ml.PipelineModel = pipeline_471de568ed2e
>     predictionsAndLabels: org.apache.spark.sql.DataFrame = [window: struct<start: timestamp, end: timestamp>, Power: double ... 65 more fields]
>     metrics: org.apache.spark.mllib.evaluation.RegressionMetrics = org.apache.spark.mllib.evaluation.RegressionMetrics@517e318c
>     rmse: Double = 5.371102763373557
>     explainedVariance: Double = 183.27403720831734
>     r2: Double = 0.8650238794181951

Not too bad, but let's see if we can improve the results using cross validation.

``` scala
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
```

>     warning: there was one feature warning; re-run with -feature for details
>     import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
>     import org.apache.spark.ml.evaluation._
>     regEval: org.apache.spark.ml.evaluation.RegressionEvaluator = regEval_d62c017ca16e
>     crossval: org.apache.spark.ml.tuning.CrossValidator = cv_bbfe7b54e4c0
>     regParam: Array[Double] = Array(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1)
>     paramGrid: Array[org.apache.spark.ml.param.ParamMap] =
>     Array({
>     	linReg_d056f664f4f3-regParam: 0.01
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.02
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.03
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.04
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.05
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.06
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.07
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.08
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.09
>     }, {
>     	linReg_d056f664f4f3-regParam: 0.1
>     })
>     cvModel: org.apache.spark.ml.tuning.CrossValidatorModel = cv_bbfe7b54e4c0
>     predictionsAndLabels: org.apache.spark.sql.DataFrame = [window: struct<start: timestamp, end: timestamp>, Power: double ... 65 more fields]
>     metrics: org.apache.spark.mllib.evaluation.RegressionMetrics = org.apache.spark.mllib.evaluation.RegressionMetrics@5d2f15c0
>     rmse: Double = 5.3684288676362915
>     explainedVariance: Double = 185.85849068387805
>     r2: Double = 0.8651582362729922

`rmse: Double = 5.3684288676362915 explainedVariance: Double = 185.85849068387805 r2: Double = 0.8651582362729922`

#### Train a gradient boosted tree

This is basically just copied from notebook 20 (power plant pipeline)

``` scala
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
```

>     import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
>     import org.apache.spark.ml.evaluation._
>     import org.apache.spark.ml.regression.GBTRegressor
>     import org.apache.spark.ml.Pipeline
>     gbt: org.apache.spark.ml.regression.GBTRegressor = gbtr_775275335c61
>     gbtPipeline: org.apache.spark.ml.Pipeline = pipeline_7d65fadcdb83
>     regEval: org.apache.spark.ml.evaluation.RegressionEvaluator = regEval_59bac5a3de87
>     crossval: org.apache.spark.ml.tuning.CrossValidator = cv_3a60f9d0ffd5
>     paramGrid: Array[org.apache.spark.ml.param.ParamMap] =
>     Array({
>     	gbtr_775275335c61-maxDepth: 2
>     }, {
>     	gbtr_775275335c61-maxDepth: 3
>     })
>     gbtModel: org.apache.spark.ml.tuning.CrossValidatorModel = cv_3a60f9d0ffd5

``` scala
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
```

>     Root Mean Squared Error: 2.863424384194055
>     Explained Variance: 202.1087124482088
>     R2: 0.961637980977275
>     import org.apache.spark.ml.regression.GBTRegressionModel
>     import org.apache.spark.mllib.evaluation.RegressionMetrics
>     predictionsAndLabels: org.apache.spark.sql.DataFrame = [window: struct<start: timestamp, end: timestamp>, Power: double ... 65 more fields]
>     metrics: org.apache.spark.mllib.evaluation.RegressionMetrics = org.apache.spark.mllib.evaluation.RegressionMetrics@50154faf
>     rmse: Double = 2.863424384194055
>     explainedVariance: Double = 202.1087124482088
>     r2: Double = 0.961637980977275

`rmse: Double = 2.863424384194055 explainedVariance: Double = 202.1087124482088 r2: Double = 0.961637980977275`

``` scala
val results = predictionsAndLabels.withColumn("difference", $"Predicted_Power" - $"future_power").cache()
```

>     results: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 66 more fields]

Displaying the results
======================

Note that the predicted power is 30 minutes into the future and you need to "visually shift" one of the lines

``` scala
display(predictionsAndLabels)
```