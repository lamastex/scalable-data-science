// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/GFe7RQXn4Cs/0.jpg)](https://www.youtube.com/embed/GFe7RQXn4Cs?start=0&end=2811&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Sketching with T-digest for quantiles
// MAGIC ## A Toy Anomaly Detector
// MAGIC 
// MAGIC Fisher noticed the fundamental computational difference between mean, covariance, etc. and median, quantiles, in early 1900s.
// MAGIC 
// MAGIC The former ones are today called recursively computable statistics. When you take the memory footprint needed to keep these statistics updated then we get into the world of probabilistic datastructures...
// MAGIC 
// MAGIC The basic idea of sketching is formally conveyed in Chapter 6 of [Foundations of data Science](https://www.cs.cornell.edu/jeh/book.pdf).
// MAGIC 
// MAGIC Let's get a more informal view form the following sources.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC * Read now: https://medium.com/@muppal/probabilistic-data-structures-in-the-big-data-world-code-b9387cff0c55
// MAGIC * See Ted Dunning's explanation of t-digest now: https://www.youtube.com/watch?v=B0dMc0t7K1g

// COMMAND ----------

// MAGIC %md
// MAGIC # Demonstration of t-digest to detect anomalous scores

// COMMAND ----------

// MAGIC %md
// MAGIC Let us import the following scala implementation of t-digest:
// MAGIC 
// MAGIC * maven coordinates: `isarn-sketches-spark_2.11-0.3.0-sp2.2-py2.7`
// MAGIC 
// MAGIC See the library: [https://github.com/isarn/isarn-sketches-spark](https://github.com/isarn/isarn-sketches-spark)

// COMMAND ----------

import org.isarnproject.sketches._
import org.isarnproject.sketches.udaf._
import org.apache.spark.isarnproject.sketches.udt._
import org.isarnproject.sketches._
import org.isarnproject.sketches.udaf._
import org.apache.spark.isarnproject.sketches.udt._


// COMMAND ----------

import scala.util.Random
import scala.util.Random._ 

// make a sample to produce a mixture of two normal RVs with standard deviation 1 but with different location or mean parameters
def myMixtureOf2Normals( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : Double = {
  val sample = if (r.nextDouble <= normalWeight) {r.nextGaussian+normalLocation } 
               else {r.nextGaussian + abnormalLocation} 
  return sample
   }

// COMMAND ----------

// MAGIC %md
// MAGIC Here is a quick overview of the simple mixture of two Normal or Gaussian random variables we will be simulating from.

// COMMAND ----------

val r = new Random(1L)
println(myMixtureOf2Normals(1.0, 10.0, 0.99, r), myMixtureOf2Normals(1.0, 10.0, 0.99, r))
// should always produce (0.5876430182311466,-0.34037937678788865) when seed = 1L

// COMMAND ----------

val r = new Random(12345L)
val data = sc.parallelize(Vector.fill(10000){myMixtureOf2Normals(1.0, 10.0, 0.99, r)}).toDF.as[Double]

// COMMAND ----------

data.show(5)

// COMMAND ----------

display(data)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's t-digest this `data` using a user-defined function `udaf` evaluated below.

// COMMAND ----------

val udaf = tdigestUDAF[Double].delta(0.2)
                              //.maxDiscrete(25) // an additional optimisation with bins

// COMMAND ----------

// MAGIC %md
// MAGIC We can `agg` or aggregate the `data` DataFrame's `value` column of `Double`s that contain our data as follows.

// COMMAND ----------

val agg = data.agg(udaf($"value"))

// COMMAND ----------

// MAGIC %md
// MAGIC Next, let's get the t-digest of the aggregation as `td`.

// COMMAND ----------

val td = agg.first.getAs[TDigestSQL](0).tdigest // t-digest

// COMMAND ----------

// MAGIC %md
// MAGIC We can evaluate the t-digest `td` as a cummulative distribution function or CDF at `x` via the `.cdf(x)` method.

// COMMAND ----------

td.cdf(1.0)

// COMMAND ----------

// MAGIC %md
// MAGIC We can also get the inverse CDF at any `u` in the unit interval to get quantiles as follows.

// COMMAND ----------

val cutOff = td.cdfInverse(0.99)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's flag those points that cross the threshold determine dby the `cutOff`.

// COMMAND ----------

val dataFlagged = data.withColumn("anomalous",$"value">cutOff)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's show and display the anomalous points.
// MAGIC 
// MAGIC We are not interested in word-wars over anomalies and outliers here (at the end of the day we are really only interested in the real problem that these arithmetic and syntactic expressions will be used to solve, such as,:
// MAGIC 
// MAGIC * keep a washing machine running longer by shutting it down before it will break down (predictive maintenance)
// MAGIC * keep a network from being attacked by bots/malware/etc by flagging any unusual events worth escalating to the network security opes teams (without annoying them constantly!)
// MAGIC * etc.

// COMMAND ----------

data.withColumn("anomalous",$"value">cutOff).filter("anomalous").show(5)

// COMMAND ----------

display(dataFlagged)

// COMMAND ----------

// MAGIC %md
// MAGIC # Apply the batch-learnt T-Digest on a new stream of data
// MAGIC 
// MAGIC First let's simulate historical data for batch-processing.

// COMMAND ----------

import scala.util.Random
import scala.util.Random._

// simulate 5 bursts of historical data - emulate batch processing

// make a sample to produce a mixture of two normal RVs with standard deviation 1 but with different location or mean parameters
def myMixtureOf2Normals( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : (String, Double) = {
  val sample = if (r.nextDouble <= normalWeight) {r.nextGaussian+normalLocation } 
               else {r.nextGaussian + abnormalLocation} 
  Thread.sleep(5L) // sleep 5 milliseconds
  val now = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new java.util.Date())
  return (now,sample)
   }
   
 dbutils.fs.rm("/datasets/batchFiles/",true) // this is to delete the directory before staring a job
 
val r = new Random(123454321L)
var a = 0;
// for loop execution to write files to distributed fs
for( a <- 1 to 5){
  val data = sc.parallelize(Vector.fill(100){myMixtureOf2Normals(1.0, 10.0, 0.99, r)}).coalesce(1).toDF.as[(String,Double)]
  val minute = (new java.text.SimpleDateFormat("mm")).format(new java.util.Date())
  val second = (new java.text.SimpleDateFormat("ss")).format(new java.util.Date())
  data.write.mode(SaveMode.Overwrite).csv("/datasets/batchFiles/" + minute +"_" + second + ".csv")
  Thread.sleep(10L) // sleep 10 milliseconds
}

// COMMAND ----------

display(dbutils.fs.ls("/datasets/batchFiles/"))

// COMMAND ----------

// MAGIC %md
// MAGIC Now let's use a static DataFrame to process these files with t-digest and get the 0.99-th quantile based Cut-off.

// COMMAND ----------

// Read all the csv files written atomically in a directory
import org.apache.spark.sql.types._

val timedScore = new StructType().add("time", "timestamp").add("score", "Double")

import java.sql.{Date, Timestamp}
case class timedScoreCC(time: Timestamp, score: Double)

//val streamingLines = sc.textFile("/datasets/streamingFiles/*").toDF.as[String]
val staticLinesDS = spark
  .read
  .option("sep", ",")
  .schema(timedScore)      // Specify schema of the csv files
  .csv("/datasets/batchFiles/*").as[timedScoreCC]

val udaf = tdigestUDAF[Double].delta(0.2).maxDiscrete(25)

val batchLearntCutOff99 = staticLinesDS
                  .agg(udaf($"score").as("td"))
                  .first.getAs[TDigestSQL](0)
                  .tdigest
                  .cdfInverse(0.99)

// COMMAND ----------

// MAGIC %md
// MAGIC We will next execute the companion notebook `040a_TDigestInputStream` in order to generate the files with the Gaussian mixture for streaming jobs.
// MAGIC 
// MAGIC The code in the companion notebook is as follows for convenience (you could just copy-paste this code into another notebook in the same cluster with the same distributed file system):
// MAGIC 
// MAGIC ```%scala
// MAGIC import scala.util.Random
// MAGIC import scala.util.Random._
// MAGIC 
// MAGIC // make a sample to produce a mixture of two normal RVs with standard deviation 1 but with different location or mean parameters
// MAGIC def myMixtureOf2Normals( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : (String, Double) = {
// MAGIC   val sample = if (r.nextDouble <= normalWeight) {r.nextGaussian+normalLocation } 
// MAGIC                else {r.nextGaussian + abnormalLocation} 
// MAGIC   Thread.sleep(5L) // sleep 5 milliseconds
// MAGIC   val now = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new java.util.Date())
// MAGIC   return (now,sample)
// MAGIC    }
// MAGIC    
// MAGIC  dbutils.fs.rm("/datasets/streamingFiles/",true) // this is to delete the directory before staring a job
// MAGIC  
// MAGIC val r = new Random(12345L)
// MAGIC var a = 0;
// MAGIC // for loop execution to write files to distributed fs
// MAGIC for( a <- 1 to 20){
// MAGIC   val data = sc.parallelize(Vector.fill(100){myMixtureOf2Normals(1.0, 10.0, 0.99, r)}).coalesce(1).toDF.as[(String,Double)]
// MAGIC   val minute = (new java.text.SimpleDateFormat("mm")).format(new java.util.Date())
// MAGIC   val second = (new java.text.SimpleDateFormat("ss")).format(new java.util.Date())
// MAGIC   data.write.mode(SaveMode.Overwrite).csv("/datasets/streamingFiles/" + minute +"_" + second + ".csv")
// MAGIC   Thread.sleep(5000L) // sleep 5 seconds
// MAGIC }
// MAGIC ```
// MAGIC 
// MAGIC We will simply apply the batch-learnt t-digest as the threshold for determining if the streaming data is anomalous or not.

// COMMAND ----------

import org.apache.spark.sql.types._
import java.sql.{Date, Timestamp}

val timedScore = new StructType().add("time", "timestamp").add("score", "Double")
case class timedScoreCC(time: Timestamp, score: Double)

val streamingLinesDS = spark
  .readStream
  .option("sep", ",")
  .schema(timedScore)      // Specify schema of the csv files
  .csv("/datasets/streamingFiles/*").as[timedScoreCC]

// COMMAND ----------

//display(streamingLinesDS)

// COMMAND ----------

// MAGIC %md
// MAGIC Now, we can apply this batch-learnt cut-off from the static DataSet to the streaming DataSet. 
// MAGIC 
// MAGIC This is a simple example of learning in batch mode (say overnight or every few hours) and applying it to live streaming data.

// COMMAND ----------

// Start running the query that prints the running counts to the console
val dataFalgged = streamingLinesDS
      .withColumn("anomalous",$"score" > batchLearntCutOff99).filter($"anomalous")
      .writeStream
      //.outputMode("complete")
      .format("console")
      .start()

dataFalgged.awaitTermination() // hit cancel to terminate

// COMMAND ----------

// MAGIC %md
// MAGIC Although the above pattern of estimating the 99% Cut-Off periodically by batch-processing static DataSets from historical data and then applying these Cut-Offs to filter anamolous data points that are currently streaming at us is *good enough* for several applications, we may want to do *online estimation/learning* of the Cut-Off based on the 99% of all the data up to present time and use this *live* Cut-off to decide which point is anamolous now.
// MAGIC 
// MAGIC For this we need to use more delicate parts of Structured Streaming.

// COMMAND ----------

// MAGIC %md
// MAGIC # Streaming T-Digest - Online Updating of the Cut-Off
// MAGIC 
// MAGIC To impelment a streaming t-digest of the data that keeps the current threshold and a current t-digest, we need to get into more delicate parts of structured streaming and implement our own `flatMapgroupsWithState`.
// MAGIC 
// MAGIC Here are some starting points for diving deeper in this direction of *arbitrary stateful processing*:
// MAGIC 
// MAGIC * [https://databricks.com/blog/2017/10/17/arbitrary-stateful-processing-in-apache-sparks-structured-streaming.html](https://databricks.com/blog/2017/10/17/arbitrary-stateful-processing-in-apache-sparks-structured-streaming.html).
// MAGIC * Spark Summit EU Dublin 2017 Deep Dive: [https://databricks.com/session/deep-dive-stateful-stream-processing](https://databricks.com/session/deep-dive-stateful-stream-processing)
// MAGIC * See [https://youtu.be/JAb4FIheP28](https://youtu.be/JAb4FIheP28)
// MAGIC * Official [docs and examples of arbitrary stateful operations](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#arbitrary-stateful-operations)
// MAGIC   * doc: [https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.streaming.GroupState](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.streaming.GroupState)
// MAGIC   * example: [https://github.com/apache/spark/blob/v2.2.0/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredSessionization.scala](https://github.com/apache/spark/blob/v2.2.0/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredSessionization.scala)
// MAGIC 
// MAGIC * See [Part 14](http://blog.madhukaraphatak.com/introduction-to-spark-structured-streaming-part-14/) of [the 14-part series on structured streaming series in Madhukar's blog](http://blog.madhukaraphatak.com/categories/introduction-structured-streaming/) 
// MAGIC * Authoritative resource: [https://jaceklaskowski.gitbooks.io/spark-structured-streaming/content/spark-sql-streaming-KeyValueGroupedDataset-flatMapGroupsWithState.html](https://jaceklaskowski.gitbooks.io/spark-structured-streaming/content/spark-sql-streaming-KeyValueGroupedDataset-flatMapGroupsWithState.html)
// MAGIC 
// MAGIC # Streaming Machine Learning and Structured Streaming
// MAGIC 
// MAGIC Ultimately we want to use structured streaming for online machine learning algorithms and not just sketching.
// MAGIC 
// MAGIC * See Spark Summit 2016 video on streaming ML (streaming logistic regression): [https://youtu.be/r0hyjmLMMOc](https://youtu.be/r0hyjmLMMOc)
// MAGIC * [Holden Karau's codes from High Performance Spark Book](https://github.com/holdenk/spark-structured-streaming-ml/tree/master/src/main/scala/com/high-performance-spark-examples/structuredstreaming)
// MAGIC 
// MAGIC # Data Engineering Science Pointers
// MAGIC 
// MAGIC Using kafka, Cassandra and Spark Structured Streaming
// MAGIC 
// MAGIC * [https://github.com/ansrivas/spark-structured-streaming](https://github.com/ansrivas/spark-structured-streaming)
// MAGIC * [https://github.com/polomarcus/Spark-Structured-Streaming-Examples](https://github.com/polomarcus/Spark-Structured-Streaming-Examples)