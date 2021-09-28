// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Activity Recognition from Accelerometer 
// MAGIC ## 1. ETL Using SparkSQL Windows 
// MAGIC ## 2. Prediction using Random Forest
// MAGIC 
// MAGIC This work is a simpler databricksification of Amira Lakhal's more complex framework for activity recognition: 
// MAGIC 
// MAGIC * [https://github.com/MiraLak/activityRecognitionV2](https://github.com/MiraLak/activityRecognitionV2).

// COMMAND ----------

// MAGIC %md
// MAGIC [![Amira's video](http://img.youtube.com/vi/1DV9Kdec0-A/0.jpg)](https://www.youtube.com/watch?v=1DV9Kdec0-A)

// COMMAND ----------

// MAGIC %md
// MAGIC See Section below on **0. Download and Load Data** first.
// MAGIC 
// MAGIC - Once data is loaded come back here (if you have not downloaded and loaded data into the distributed file system under your hood).

// COMMAND ----------

val data = sc.textFile("dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv") // assumes data is loaded

// COMMAND ----------

data.take(5).foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC #### HW (Optional): Repeat this analysis using java time instead of unix timestamp.

// COMMAND ----------

// MAGIC %md
// MAGIC ## We can read in the data either using `sqlContext` or with `spark`, our premade entry points.

// COMMAND ----------

val dataDF = sqlContext.read    
    .format("com.databricks.spark.csv") // use spark.csv package
    .option("header", "true") // Use first line of all files as header
    .option("inferSchema", "true") // Automatically infer data types
    .option("delimiter", ",") // Specify the delimiter as ','
    .load("dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv")

// COMMAND ----------

val dataDFnew = spark.read.format("csv") 
  .option("inferSchema", "true") 
  .option("header", "true") 
  .option("sep", ",") 
  .load("dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv")

// COMMAND ----------

dataDFnew.printSchema()

// COMMAND ----------

display(dataDF) // zp.show(dataDF)

// COMMAND ----------

dataDF.count()

// COMMAND ----------

dataDF.select($"user_id").distinct().show()

// COMMAND ----------

dataDF.select($"activity").distinct().show()

// COMMAND ----------

val sDF = dataDF.sample(false,0.105, 12345L)
sDF.count

// COMMAND ----------

sDF.show(5)

// COMMAND ----------

sDF.show(5)

// COMMAND ----------

display(dataDF.sample(false,0.1))

// COMMAND ----------

// MAGIC %md
// MAGIC # Feature Selection on Running Windows
// MAGIC ## 2. ETL Using SparkSQL Windows
// MAGIC ## A Markov Process Assumption
// MAGIC This is sensible since the subjects are not instantaneously changing between the activities of interest: sitting, walking, jogging, etc.
// MAGIC Thus it makes sense to try and use the most recent accelerometer readings (from the immediate past) to predict the current activity. 

// COMMAND ----------

// MAGIC %md
// MAGIC See the following for a crash introduction to windows:
// MAGIC 
// MAGIC * [http://xinhstechblog.blogspot.co.nz/2016/04/spark-window-functions-for-dataframes.html](http://xinhstechblog.blogspot.co.nz/2016/04/spark-window-functions-for-dataframes.html)

// COMMAND ----------

 // Import the window functions.
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

// Create a window specification
val windowSize = 10
val wSpec1 = Window.partitionBy("user_id","activity").orderBy("timeStampAsLong").rowsBetween(-windowSize, 0)

// COMMAND ----------

 // Calculate the moving window statistics from data
val dataFeatDF = dataDF
      .withColumn( "meanX", mean($"x").over(wSpec1)  )
      .withColumn( "meanY", mean($"y").over(wSpec1)  )
      .withColumn( "meanZ", mean($"z").over(wSpec1)  ) 
//resultant = 1/n * ∑ √(x² + y² + z²)
      .withColumn( "SqX", pow($"x",2.0) )
      .withColumn( "SqY", pow($"y",2.0) )
      .withColumn( "SqZ", pow($"z",2.0) )
      .withColumn( "resultant", pow( $"SqX"+$"SqY"+$"SqZ",0.50 ) )
      .withColumn( "meanResultant", mean("resultant").over(wSpec1) )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanX", abs($"x" - $"meanX") )
      .withColumn( "absDevFromMeanY", abs($"y" - $"meanY") )
      .withColumn( "absDevFromMeanZ", abs($"z" - $"meanZ") )
      .withColumn( "meanAbsDevFromMeanX", mean("absDevFromMeanX").over(wSpec1) )
      .withColumn( "meanAbsDevFromMeanY", mean("absDevFromMeanY").over(wSpec1) )
      .withColumn( "meanAbsDevFromMeanZ", mean("absDevFromMeanZ").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanX", pow($"absDevFromMeanX",2.0) )
      .withColumn( "sqrDevFromMeanY", pow($"absDevFromMeanY",2.0) )
      .withColumn( "sqrDevFromMeanZ", pow($"absDevFromMeanZ",2.0) )
      .withColumn( "varianceX", mean("sqrDevFromMeanX").over(wSpec1) )
      .withColumn( "varianceY", mean("sqrDevFromMeanY").over(wSpec1) )
      .withColumn( "varianceZ", mean("sqrDevFromMeanZ").over(wSpec1) )
      .withColumn( "stddevX", pow($"varianceX",0.50) )
      .withColumn( "stddevY", pow($"varianceY",0.50) )
      .withColumn( "stddevZ", pow($"varianceZ",0.50) )

// COMMAND ----------

display(dataFeatDF.sample(false,0.1))

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. Prediction using Random Forests
// MAGIC Instead of just stopping at ETL and feature engineering let's **quickly see without understanding the random forest model in detail** how simple it is to predict the activity using random forest model.

// COMMAND ----------

val splits = dataFeatDF.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))

// COMMAND ----------

// MAGIC %md
// MAGIC See
// MAGIC 
// MAGIC * [http://spark.apache.org/docs/latest/ml-classification-regression.html#random-forest-classifier](http://spark.apache.org/docs/latest/ml-classification-regression.html#random-forest-classifier)
// MAGIC * [https://spark.apache.org/docs/latest/mllib-ensembles.html#random-forests](https://spark.apache.org/docs/latest/mllib-ensembles.html#random-forests)
// MAGIC * [http://blog.citizennet.com/blog/2012/11/10/random-forests-ensembles-and-performance-metrics](http://blog.citizennet.com/blog/2012/11/10/random-forests-ensembles-and-performance-metrics)

// COMMAND ----------

def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://spark.apache.org/docs/latest/mllib-ensembles.html#random-forests",500))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Random_forest",500))

// COMMAND ----------

import org.apache.spark.ml.feature.{StringIndexer,VectorAssembler}
import org.apache.spark.ml.Pipeline

import org.apache.spark.ml.classification.RandomForestClassifier

val transformers = Array(
              new StringIndexer().setInputCol("activity").setOutputCol("label"),
              new VectorAssembler()
                      .setInputCols(Array("meanX", "meanY", "meanZ", "stddevX", "stddevY","stddevZ"))
                      .setOutputCol("features")
)

// Train a RandomForest model.
val rf = new RandomForestClassifier() 
              .setLabelCol("label")
              .setFeaturesCol("features")
              .setNumTrees(10)
              .setFeatureSubsetStrategy("auto")
              .setImpurity("gini")
              .setMaxDepth(20)
              .setMaxBins(32)
              .setSeed(12345)

val model = new Pipeline().setStages(transformers :+ rf).fit(trainingData)


// COMMAND ----------

// MAGIC %md
// MAGIC Before we go further let's find quickly which label is mapped to which activity by the `StringIndexer` and quickly check that the estimator we have built can transform the union of the training and test data. This is mostly for debugging... not something you would do in a real pipeline except during debugging stages.

// COMMAND ----------

val activityLabelDF = model.transform(trainingData.union(testData)).select("activity","label").distinct
display(activityLabelDF) // this just shows what activity is associated with what label

// COMMAND ----------

val myPredictionsOnTestData = model.transform(testData)
display(myPredictionsOnTestData)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's evaluate accuracy at a lower level...

// COMMAND ----------

val accuracy: Double = 1.0 * model.transform(testData)
                                  .select("activity","label","prediction")
                                  .filter($"label"===$"prediction").count() / testData.count() 

// COMMAND ----------

// MAGIC %md
// MAGIC We get 98% correct predictions and here are the mis-predicted ones.

// COMMAND ----------

display(model.transform(testData).select("activity","label","prediction").filter(not($"label"===$"prediction")))

// COMMAND ----------

testData.printSchema()

// COMMAND ----------

// MAGIC %md
// MAGIC Let's understand the mis-predicitons better by seeing the `user_id` also.

// COMMAND ----------

display(model.transform(testData).select("user_id","activity","label","prediction").filter(not($"label"===$"prediction")))

// COMMAND ----------

// MAGIC %md
// MAGIC Let's evaluate our model's accuracy using ML pipeline's `evaluation.MulticlassClassificationEvaluator`:

// COMMAND ----------

import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

// Select (prediction, true label) and compute test error.
val evaluator = new MulticlassClassificationEvaluator()
  .setLabelCol("label")
  .setPredictionCol("prediction")
  .setMetricName("accuracy")
val accuracy = evaluator.evaluate(myPredictionsOnTestData)
println(s"Test Error = ${(1.0 - accuracy)}")

// COMMAND ----------

// MAGIC %md
// MAGIC Note, it's the same 98% we got from our slightly lower-level operations earlier for accuracy.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Think!
// MAGIC What else do you need to investigate to try to understand the mis-predicitons better?
// MAGIC 
// MAGIC * time-line?
// MAGIC * are mis-predictions happening during transitions between users or activities of the same user or both, as our Markov process simply uses the same sliding window over the time-odered, user-grouped, activity streams...
// MAGIC * ...
// MAGIC 
// MAGIC Typically, in inustry you will have to improve on an existing algorithm that is in production and getting into the details under the given constraints needs some *thinking from scratch over the entire data science pipeline and process*.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Explaining the Model's Predictions to a Curious Client whose Activity is being Predicted
// MAGIC Now, let's try to **explain** the decision branches in each tree of our best-fitted random forest model. Often, being able to "explain" why the model "predicts" is *critical* for various businesses.
// MAGIC how it predicts.

// COMMAND ----------

import org.apache.spark.ml.classification.RandomForestClassificationModel
val rfModel = model.stages(2).asInstanceOf[RandomForestClassificationModel]
println(s"Learned classification forest model:\n ${rfModel.toDebugString}")

// COMMAND ----------

// MAGIC %md
// MAGIC ### Save and Load later to Serve Predictions
// MAGIC 
// MAGIC When you are satisfied with the model you have trained/fitted then you can save it to a file and load it again from another *low-latency prediciton-serving system* to serve predictions.

// COMMAND ----------

model

// COMMAND ----------

import org.apache.spark.ml.PipelineModel

// COMMAND ----------

// MAGIC %md
// MAGIC Save the model to a file in stable storage.

// COMMAND ----------

model.write.overwrite().save("/datasets/sds/ActivityRecognition/preTrainedModels/myRandomForestClassificationModelAsPipeLineModel")

// COMMAND ----------

// MAGIC %md
// MAGIC Load the model, typically from a *prediction or model serving layer/system*.

// COMMAND ----------

val same_rfModelFromSavedFile = PipelineModel.load("/datasets/sds/ActivityRecognition/preTrainedModels/myRandomForestClassificationModelAsPipeLineModel")

// COMMAND ----------

same_rfModelFromSavedFile

// COMMAND ----------

// MAGIC %md
// MAGIC Suppose new data is coming at us and we need to serve our predicitons of the activities 
// MAGIC 
// MAGIC Note: We only need accelerometer readings so prediciton pipeline can be simplified to only take in the needed features for prediction. For simplicity let's assume the test data is representative of the new data (previously unseen) that is coming at us for serving our activity predicitons.

// COMMAND ----------

val newDataForPrediction = testData.sample(0.0004,12348L) // get a random test data as new data for prediction
display(newDataForPrediction.select("meanX", "meanY", "meanZ", "stddevX", "stddevY","stddevZ")) // showing only features that will be used by model for prediction

// COMMAND ----------

// MAGIC %md
// MAGIC Here is another new data coming at you... you can predict the current activity being done by simply transforming the loaded model (estimator).

// COMMAND ----------

val newDataForPrediction = testData.sample(0.0004,1234L) // get a random test data as new data for prediction
display(newDataForPrediction.select("meanX", "meanY", "meanZ", "stddevX", "stddevY","stddevZ")) // showing only features that will be used by model for prediction

// COMMAND ----------

display(same_rfModelFromSavedFile.transform(newDataForPrediction).select("meanX", "meanY", "meanZ", "stddevX", "stddevY","stddevZ","prediction"))

// COMMAND ----------

// MAGIC %md
// MAGIC Some minor post-processing to connect prediction label to the activity string.
// MAGIC 
// MAGIC Typically one prediction is just a small part of larger business or science use-case. 

// COMMAND ----------

activityLabelDF.show

// COMMAND ----------

display(same_rfModelFromSavedFile
        .transform(newDataForPrediction)
        .select("meanX", "meanY", "meanZ", "stddevX", "stddevY","stddevZ","prediction")
        .join(activityLabelDF,$"prediction"===$"label")
        .drop("prediction","label")
        .withColumnRenamed("activity","Your current activity is likely to be")
       )

// COMMAND ----------

// MAGIC %md
// MAGIC **An activity-based music-recommendation system - a diatribe:**
// MAGIC 
// MAGIC For example, you may want to correlate historical music-listening habits for a user with their historical physical activities and then recommend music to them using a subsequent recommender system pipeline that uses activity prediciton as one of its input.
// MAGIC 
// MAGIC **NOTE:** One has to be legally compliant for using the data in such manner with client consent, depending on the jurisdiction of your operations
// MAGIC   - GDPR applies for EU operations, for example --- this is coming attraction!

// COMMAND ----------

display(same_rfModelFromSavedFile.transform(testData.sample(0.01))) // note more can be done with such data, time of day can be informative for other decision problems

// COMMAND ----------

// MAGIC %md
// MAGIC ## 0. Download and Load Data
// MAGIC 
// MAGIC The following anonymized dataset is used with kind permission of Amira Lakhal.

// COMMAND ----------

// MAGIC %sh
// MAGIC wget http://lamastex.org/datasets/public/ActivityRecognition/dataTraining.csv

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd && ls

// COMMAND ----------

//dbutils.fs.mkdirs("dbfs:///datasets/sds/ActivityRecognition") // make directory if needed
dbutils.fs.mv("file:///databricks/driver/dataTraining.csv","dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:///datasets/sds/ActivityRecognition"))

// COMMAND ----------

dbutils.fs.head("dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv")

// COMMAND ----------

