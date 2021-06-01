// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md #Power Plant ML Pipeline Application
// MAGIC This is an end-to-end example of using a number of different machine learning algorithms to solve a supervised regression problem.
// MAGIC 
// MAGIC ###Table of Contents
// MAGIC 
// MAGIC - *Step 1: Business Understanding*
// MAGIC - *Step 2: Load Your Data*
// MAGIC - *Step 3: Explore Your Data*
// MAGIC - *Step 4: Visualize Your Data*
// MAGIC - *Step 5: Data Preparation*
// MAGIC - *Step 6: Data Modeling*
// MAGIC - *Step 7: Tuning and Evaluation*
// MAGIC - *Step 8: Deployment*
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC *We are trying to predict power output given a set of readings from various sensors in a gas-fired power generation plant.  Power generation is a complex process, and understanding and predicting power output is an important element in managing a plant and its connection to the power grid.*
// MAGIC 
// MAGIC More information about Peaker or Peaking Power Plants can be found on Wikipedia https://en.wikipedia.org/wiki/Peaking_power_plant
// MAGIC 
// MAGIC 
// MAGIC Given this business problem, we need to translate it to a Machine Learning task.  The ML task is regression since the label (or target) we are trying to predict is numeric.
// MAGIC 
// MAGIC 
// MAGIC The example data is provided by UCI at [UCI Machine Learning Repository Combined Cycle Power Plant Data Set](https://archive.ics.uci.edu/ml/datasets/Combined+Cycle+Power+Plant)
// MAGIC 
// MAGIC You can read the background on the UCI page, but in summary we have collected a number of readings from sensors at a Gas Fired Power Plant
// MAGIC 
// MAGIC (also called a Peaker Plant) and now we want to use those sensor readings to predict how much power the plant will generate.
// MAGIC 
// MAGIC 
// MAGIC More information about Machine Learning with Spark can be found in the [Spark MLLib Programming Guide](https://spark.apache.org/docs/latest/mllib-guide.html)
// MAGIC 
// MAGIC 
// MAGIC *Please note this example only works with Spark version 1.4 or higher*

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC To **Rerun Steps 1-4** done in the notebook at:
// MAGIC * `Workspace -> PATH_TO -> 009_PowerPlantPipeline_01ETLEDA]`
// MAGIC 
// MAGIC just `run` the following command as shown in the cell below: 
// MAGIC 
// MAGIC   ```%scala
// MAGIC   %run "PATH_TO/009_PowerPlantPipeline_01ETLEDA"
// MAGIC   ```
// MAGIC   
// MAGIC    * *Note:* If you already evaluated the `%run ...` command above then:
// MAGIC      * first delete the cell by pressing on `x` on the top-right corner of the cell and 
// MAGIC      * revaluate the `run` command above.

// COMMAND ----------

// MAGIC %run "../009_PowerPlantPipeline_01ETLEDA"

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC # Next we did the following Steps and saved our best model:
// MAGIC ## Step 5: Data Preparation, 
// MAGIC ## Step 6: Modeling, and 
// MAGIC ## Step 7: Tuning and Evaluation 
// MAGIC 
// MAGIC # Now we are going to load the model and deploy
// MAGIC ## Step 8: Deployment

// COMMAND ----------

//Let's quickly recall the schema
// the table is available
table("power_plant_table").printSchema

// COMMAND ----------

//the DataFrame should also be available
powerPlantDF 

// COMMAND ----------

// MAGIC %md
// MAGIC # Persisting Statistical Machine Learning Models

// COMMAND ----------

// MAGIC %md
// MAGIC See https://databricks.com/blog/2016/05/31/apache-spark-2-0-preview-machine-learning-model-persistence.html

// COMMAND ----------

// MAGIC %md
// MAGIC Since we saved our best model so we can load it without having to rerun the validation and training again.
// MAGIC 
// MAGIC ```
// MAGIC gbtModel.bestModel.asInstanceOf[PipelineModel] 
// MAGIC         //.stages.last.asInstanceOf[GBTRegressionModel]
// MAGIC         .write.overwrite().save("dbfs:///databricks/driver/MyTrainedBestPipelineModel")
// MAGIC ```

// COMMAND ----------

import org.apache.spark.ml.PipelineModel
// load the saved model from ...PowerPlantPipeline_02ModelTuneEvaluateDeploy notebook
val finalModel = PipelineModel.load("dbfs:///databricks/driver/MyTrainedBestPipelineModel/")

// COMMAND ----------

// making sure we have the same model loaded from the file
import org.apache.spark.ml.regression.GBTRegressionModel 
finalModel.stages.last.asInstanceOf[GBTRegressionModel].toDebugString

// COMMAND ----------

// MAGIC %md #Step 8: Deployment
// MAGIC 
// MAGIC Now that we have a predictive model it is time to deploy the model into an operational environment. 
// MAGIC 
// MAGIC In our example, let's say we have a series of sensors attached to the power plant and a monitoring station.
// MAGIC 
// MAGIC The monitoring station will need close to real-time information about how much power that their station will generate so they can relay that to the utility. 
// MAGIC 
// MAGIC So let's create a Spark Streaming utility that we can use for this purpose.
// MAGIC 
// MAGIC See [http://spark.apache.org/docs/latest/streaming-programming-guide.html](http://spark.apache.org/docs/latest/streaming-programming-guide.html) if you can't wait!

// COMMAND ----------

// MAGIC %md
// MAGIC After deployment you will be able to use the best predictions from gradient boosed regression trees to feed a real-time dashboard or feed the utility with information on how much power the peaker plant will deliver give current conditions.

// COMMAND ----------

// MAGIC %md
// MAGIC Let's create our table for predictions

// COMMAND ----------

// MAGIC %sql 
// MAGIC DROP TABLE IF EXISTS power_plant_predictions ;
// MAGIC CREATE TABLE power_plant_predictions(
// MAGIC   AT Double,
// MAGIC   V Double,
// MAGIC   AP Double,
// MAGIC   RH Double,
// MAGIC   PE Double,
// MAGIC   Predicted_PE Double
// MAGIC );

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC This should be updated to structured streaming - after the break.
// MAGIC 
// MAGIC Now let's create our streaming job to score new power plant readings in real-time.
// MAGIC 
// MAGIC **CAUTION**: There can be only one spark streaming context per cluster!!! So please check if a streaming context is already alive first.
// MAGIC 
// MAGIC ### Need another Library
// MAGIC 
// MAGIC - `net.liftweb:lift-json_2.12:3.4.2`

// COMMAND ----------

// this will make sure all streaming job in the cluster are stopped
//StreamingContext.getActive.foreach{ _.stop(stopSparkContext = false) }

// COMMAND ----------

import java.nio.ByteBuffer
import java.net._
import java.io._
import concurrent._
import scala.io._
import sys.process._
//import org.apache.spark.Logging
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.Minutes
import org.apache.spark.streaming.StreamingContext
//import org.apache.spark.streaming.StreamingContext.toPairDStreamFunctions
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.streaming.receiver.Receiver
import sqlContext._
import net.liftweb.json.DefaultFormats
import net.liftweb.json._

import scala.collection.mutable.SynchronizedQueue


val queue = new SynchronizedQueue[RDD[String]]()

val batchIntervalSeconds = 2

var newContextCreated = false      // Flag to detect whether new context was created or not

// Function to create a new StreamingContext and set it up
def creatingFunc(): StreamingContext = {
    
  // Create a StreamingContext
  val ssc = new StreamingContext(sc, Seconds(batchIntervalSeconds))
  val batchInterval = Seconds(1)
  ssc.remember(Seconds(300))
  val dstream = ssc.queueStream(queue)
  dstream.foreachRDD { 
    rdd =>
      // if the RDD has data
       if(!(rdd.isEmpty())) {
          // Use the final model to transform a JSON message into a dataframe and pass the dataframe to our model's transform method
           finalModel
             .transform(read.json(rdd.toDS).toDF())
         // Select only columns we are interested in
         .select("AT", "V", "AP", "RH", "PE", "Predicted_PE")
         // Append the results to our power_plant_predictions table
         .write.mode(SaveMode.Append).format("hive").saveAsTable("power_plant_predictions")
       } 
  }
  println("Creating function called to create new StreamingContext for Power Plant Predictions")
  newContextCreated = true  
  ssc
}

val ssc = StreamingContext.getActiveOrCreate(creatingFunc)
if (newContextCreated) {
  println("New context created from currently defined creating function") 
} else {
  println("Existing context running or recovered from checkpoint, may not be running currently defined creating function")
}

ssc.start()

// COMMAND ----------

// MAGIC %md 
// MAGIC Now that we have created and defined our streaming job, let's test it with some data. First we clear the predictions table.

// COMMAND ----------

// MAGIC %sql truncate table power_plant_predictions

// COMMAND ----------

// MAGIC %md 
// MAGIC Let's use data to see how much power output our model will predict.

// COMMAND ----------

// First we try it with a record from our test set and see what we get:
queue += sc.makeRDD(Seq(s"""{"AT":10.82,"V":37.5,"AP":1009.23,"RH":96.62,"PE":473.9}"""))

// We may need to wait a few seconds for data to appear in the table
Thread.sleep(Seconds(5).milliseconds)

// COMMAND ----------

// MAGIC %sql 
// MAGIC --and we can query our predictions table
// MAGIC select * from power_plant_predictions

// COMMAND ----------

// MAGIC %md 
// MAGIC Let's repeat with a different test measurement that our model has not seen before:

// COMMAND ----------

queue += sc.makeRDD(Seq(s"""{"AT":10.0,"V":40,"AP":1000,"RH":90.0,"PE":0.0}"""))
Thread.sleep(Seconds(5).milliseconds)

// COMMAND ----------

// MAGIC %sql 
// MAGIC --Note you may have to run this a couple of times to see the refreshed data...
// MAGIC select * from power_plant_predictions

// COMMAND ----------

// MAGIC %md 
// MAGIC As you can see the Predictions are very close to the real data points. 

// COMMAND ----------

// MAGIC %sql 
// MAGIC select * from power_plant_table where AT between 10 and 11 and AP between 1000 and 1010 and RH between 90 and 97 and v between 37 and 40 order by PE 

// COMMAND ----------

// MAGIC %md 
// MAGIC Now you use the predictions table to feed a real-time dashboard or feed the utility with information on how much power the peaker plant will deliver.

// COMMAND ----------

// MAGIC %md
// MAGIC Make sure the streaming context is stopped when you are done, as there can be only one such context per cluster!

// COMMAND ----------

ssc.stop(stopSparkContext = false) // gotto stop or it ill keep running!!!

// COMMAND ----------

// MAGIC %md 
// MAGIC Datasource References:
// MAGIC 
// MAGIC * Pinar Tüfekci, Prediction of full load electrical power output of a base load operated combined cycle power plant using machine learning methods, International Journal of Electrical Power & Energy Systems, Volume 60, September 2014, Pages 126-140, ISSN 0142-0615, [Web Link](http://www.journals.elsevier.com/international-journal-of-electrical-power-and-energy-systems/)
// MAGIC * Heysem Kaya, Pinar Tüfekci , Sadik Fikret Gürgen: Local and Global Learning Methods for Predicting Power of a Combined Gas & Steam Turbine, Proceedings of the International Conference on Emerging Trends in Computer and Electronics Engineering ICETCEE 2012, pp. 13-18 (Mar. 2012, Dubai) [Web Link](http://www.cmpe.boun.edu.tr/~kaya/kaya2012gasturbine.pdf)