// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/0RUyuELoNPw/0.jpg)](https://www.youtube.com/embed/0RUyuELoNPw?start=0&end=2083&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Activity Recognition from Accelerometer using Random Forest
// MAGIC 
// MAGIC This work is a simpler databricksification of Amira Lakhal's more complex framework for activity recognition:
// MAGIC 
// MAGIC * [https://github.com/MiraLak/activityRecognitionV2](https://github.com/MiraLak/activityRecognitionV2).

// COMMAND ----------

// MAGIC %md
// MAGIC [![Amira's video](http://img.youtube.com/vi/1DV9Kdec0-A/0.jpg)](https://www.youtube.com/watch?v=1DV9Kdec0-A)

// COMMAND ----------

// MAGIC %md
// MAGIC See Section below on **Download and Load Data** first.

// COMMAND ----------

val data = sc.textFile("dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv")

// COMMAND ----------

data.take(5).foreach(println)

// COMMAND ----------

val dataDF = sqlContext.read    
    .format("com.databricks.spark.csv") // use spark.csv package
    .option("header", "true") // Use first line of all files as header
    .option("inferSchema", "true") // Automatically infer data types
    .option("delimiter", ",") // Specify the delimiter as ','
    .load("dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv")

// COMMAND ----------

dataDF.printSchema()

// COMMAND ----------

display(dataDF)

// COMMAND ----------

dataDF.count()

// COMMAND ----------

dataDF.select($"user_id").distinct().show()

// COMMAND ----------

dataDF.select($"activity").distinct().show()

// COMMAND ----------

display(dataDF.sample(false,0.1))

// COMMAND ----------

// MAGIC %md
// MAGIC # Feature Selection on Running Windows
// MAGIC 
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

val splits = dataFeatDF.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))

// COMMAND ----------

// MAGIC %md
// MAGIC See
// MAGIC 
// MAGIC * [http://spark.apache.org/docs/latest/ml-classification-regression.html#random-forest-classifier](http://spark.apache.org/docs/latest/ml-classification-regression.html#random-forest-classifier)
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

val accuracy: Double = 1.0 * model.transform(testData)
                                  .select("activity","label","prediction")
                                  .filter($"label"===$"prediction").count() / testData.count() 

// COMMAND ----------

// MAGIC %md
// MAGIC We get 98% correct predictions and here are the mis-predicted ones.

// COMMAND ----------

display(model.transform(testData).select("activity","label","prediction").filter(not($"label"===$"prediction")))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Download and Load Data
// MAGIC 
// MAGIC The following anonymized dataset is used with kind permission of Amira Lakhal.

// COMMAND ----------

// MAGIC %sh
// MAGIC wget http://lamastex.org/datasets/public/ActivityRecognition/dataTraining.csv

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd && ls

// COMMAND ----------

//dbutils.fs.mkdirs("dbfs:///datasets/sds/ActivityRecognition")
dbutils.fs.mv("file:///databricks/driver/dataTraining.csv","dbfs:///datasets/sds/ActivityRecognition/")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:///datasets/sds/ActivityRecognition"))

// COMMAND ----------

