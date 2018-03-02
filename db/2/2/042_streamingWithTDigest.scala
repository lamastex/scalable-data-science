// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md 
// MAGIC # Streaming TDigest with flatMapGroupsWithState
// MAGIC by [Benny Avelin](https://www.linkedin.com/in/benny-avelin-460b99121/) and [Håkan Persson](https://www.linkedin.com/in/håkan-persson-064b763/)
// MAGIC 
// MAGIC The idea with this sketch is to demonstrate how we can have a running t-Digest in a streaming context.
// MAGIC 
// MAGIC ## Arbitrary stateful aggregations in streaming
// MAGIC We have two stateful operations, the first is mapGroupsWithState and flatmapGroupsWithState. The Databricks blog have a relatively good explanation of the two operations in their blogpost https://databricks.com/blog/2017/10/17/arbitrary-stateful-processing-in-apache-sparks-structured-streaming.html.
// MAGIC However the concept is maybe not so easy to understand so I will try to give a simple explanation of what is going on with these two aggregations.
// MAGIC 
// MAGIC ### Structured streaming
// MAGIC For the purpose of this sketch we only need to know that new data will arrive as a batch, if we instead of a streaming dataframe just apply the aggregations on a dataframe then the entirety of the data will be in a single batch.
// MAGIC 
// MAGIC ### A running state
// MAGIC The way both mapGroupsWithState and flatMapGroupsWithState works is that we start with a key-value grouped datasets, when new data arrives it will be split into the groups prescribed by the key and each key will get a batch of data. The main important idea to realize is that for each key we have a running state, and there is no prerestriction to witch keys are ok and not so the number of keys can grow/shrink or whatever. If a new key appears, the first step in both mapGroupsWithState and flatmap... is to initialize a zero state before processing the first batch for this key, the next time a key appears it will have remembered the previous state and we can use the previous state and the added batch of data to compute the next state. What can a state be? Well an object of some class that has been predescribed, the simplest would be a running max/min/mean but also as we will see in this sketch a t-digest.
// MAGIC 
// MAGIC ### flatmapGroupsWithState vs mapGroupsWithState
// MAGIC The simple difference between these two can be infered from the name, but let us go into detail. If we are only interested in an aggregated "value" (could be a case class) from each key we should use mapGroupsWithState, however there are some interesting caveats with using mapGroupsWithState. For instance certain update-modes are not allowed as well as further aggregations are not allowed. flatmap... on the other hand can output any number of rows, allows more output-modes and allows for further aggregations, see the Structured Streaming programming guide.
// MAGIC 
// MAGIC <table>
// MAGIC   <tr>
// MAGIC     <td>Query type</td><td>Output mode</td><td>Operations allowed</td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td>mapGroupsWithState</td><td>Update</td><td>Aggregations not allowed</td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td>flatMapGroupsWithState</td><td>Append</td><td>Aggregations allowed after</td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td>flatMapGroupsWithState</td><td>Update</td><td>Aggregations not allowed</td>
// MAGIC   </tr>
// MAGIC </table>

// COMMAND ----------

// MAGIC %md # Some streaming input
// MAGIC We need to have a streaming source for our example, this can be done in a number of ways. Probably there is some nice way to do this simply but the few methods I know to generate test-samples is to get a running loop that writes files with data, so that each time a new file arrives Spark will consider it as an update and load it as a batch. We have provided some code to generate points sampled from a normal distribution with anomalies added as another normal distribution.

// COMMAND ----------

import scala.util.Random
import scala.util.Random._
import scala.util.{Success, Failure}

// make a sample to produce a mixture of two normal RVs with standard deviation 1 but with different location or mean parameters
def myMixtureOf2NormalsReg( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : (String, Double) = {
  val sample = if (r.nextDouble <= normalWeight) {r.nextGaussian+normalLocation } 
               else {r.nextGaussian + abnormalLocation} 
  Thread.sleep(5L) // sleep 5 milliseconds
  val now = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new java.util.Date())
  return (now,sample)
}

// COMMAND ----------

// MAGIC %md # The /tmp folder
// MAGIC Databricks community edition has a file-number limit to 10000 and after running databricks for a while one will start to notice that things fail, and skimming the stacktrace of the failure we realize that we have reached said limit. Deleting files that one has created does not seem to solve the issue, well... this is because the /tmp folder counts into the limit and this is not cleared nearly as often as would be good for our work. Therefore we just clear it before starting our job...
// MAGIC 
// MAGIC ps. If you have not cleared the tmp folder before this might take some time actually. ds.

// COMMAND ----------

dbutils.fs.rm("/datasets/streamingFiles/",true) 
//dbutils.fs.rm("/tmp",true) // this is to delete the directory before staring a job
val r = new Random(12345L)
var a = 0;
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
// for loop execution to write files to distributed fs
//We have made a Future out of this, which means that it runs concurrently with what we do next, i.e. essentially it is a seperate thread.

val writeStreamFuture = Future {
  for( a <- 1 to 10){
    val data = sc.parallelize(Vector.fill(1000){myMixtureOf2NormalsReg(1.0, 10.0, 0.99, r)}).coalesce(1).toDF.as[(String,Double)]
    val minute = (new java.text.SimpleDateFormat("mm")).format(new java.util.Date())
    val second = (new java.text.SimpleDateFormat("ss")).format(new java.util.Date())
    data.write.mode(SaveMode.Overwrite).csv("/datasets/streamingFiles/" + minute +"_" + second + ".csv")
    Thread.sleep(50000L) // sleep 5 seconds
  }
}

// COMMAND ----------

display(dbutils.fs.ls("/datasets/streamingFiles"))

// COMMAND ----------

// MAGIC %md # AWS eventually consistent
// MAGIC The AWS distributed filesystem is eventually consistent, this can mean for instance that a file just created will not be possible to read and if we are unlucky the following code will fail to run. 

// COMMAND ----------

import org.apache.spark.sql.types._
import java.sql.{Date, Timestamp}

/**
  * timedScore is the SQL schema for timedScoreCC, and the files written in the above code
  */
val timedScore = new StructType().add("time", "timestamp").add("score", "Double")
case class timedScoreCC(time: Timestamp, val score: Double) {
}

val streamingLinesDS = spark
  .readStream
  .option("sep", ",")
  .schema(timedScore)      // Specify schema of the csv files
  .option("MaxFilesPerTrigger", 1) //  maximum number of new files to be considered in every trigger (default: no max) 
  .csv("/datasets/streamingFiles/*").as[timedScoreCC]

// COMMAND ----------

// MAGIC %md # States and rows
// MAGIC To begin describing the code below, let us first look at what will be our running State. The `isarnproject` sketches packs the TDigest class into a TDigestSQL case class and provides encoders for this to be allowed in a Dataframe, therefore we can capitalize on this and use TDigestSQL as our running state (to be precise it is the TDigest wrapped by TDigestSQL that is the state but whatever.). The next thing to worry about is how should we output and what should we output? This example shows how to embed in a single row, the TDigest, the threshold value that comes from `cdfInverse(0.99)` and the actual data that is above the threshold. To do this we create a case class which will be the template for our row, in the code below it is called `TdigAndAnomaly`.
// MAGIC 
// MAGIC ## updateAcrossBatch
// MAGIC This is our main update-function that we send as a parameter to flatmapGroupsWithState. 
// MAGIC 
// MAGIC * It takes as first input the key-value, which we will not care about in this example and is just a dummy for us. 
// MAGIC * The second input is the `inputs : Iterator[timedScoreCC]`, this is an iterator over the batch of data that we have recieved. This is the type-safe version, i.e. we know that we have a `Dataset[timedScoreCC]`, if we dont and we instead have a `DataFrame = Dataset[Row]`, we have to use `inputs : Iterator[Row]`, and we have to extract the columns of interest cast into the appropriate types. 
// MAGIC * The third input is the running state variable, this is always wrapped in a `GroupState` wrapper class, i.e. since `TDigestSQL` was our state we need to have `GroupState[TDigestSQL]` as `oldstate`.
// MAGIC * Lastly we have the output, which is an iterator of the case class chosen as outputrow, in our case this is `Iterator[TdigAndAnomaly]`
// MAGIC 
// MAGIC Each time a batch gets processed, the batch data is in the `inputs` variable. We first make sure that the state is either the previous state (if it exists) or we set it to a zero state. Then we simply process the batch one datapoint at the time, and each time calling updateTDIG, which simply updates the state with the new data point (tDigest add point). Once we have added all the points to the t-Digest, we can compute the updated value of `threshold` using `cdfInverse(0.99)`, after that we simply filter the batch to obtain an iterator of the anomalies.
// MAGIC 
// MAGIC ### GroupStateTimeout
// MAGIC This is an interesting variable that you really should look into if you wish to understand structured streaming. Essentially it is the whole point of messing around with the structured streaming framework, see the programming guide.

// COMMAND ----------

import org.isarnproject.sketches._
import org.isarnproject.sketches.udaf._
import org.apache.spark.isarnproject.sketches.udt._
import org.isarnproject.sketches._
import org.isarnproject.sketches.udaf._
import org.apache.spark.isarnproject.sketches.udt._

case class TdigAndAnomaly(tDigSql:TDigestSQL, tDigThreshold:Double, time:Timestamp, score:Double)
//State definition

def updateTDIG(state:TDigestSQL, input:timedScoreCC):TDigestSQL = {
  //For each input let us update the TDigest
  TDigestSQL(state.tdigest + input.score)
}

import org.apache.spark.sql.streaming.{GroupStateTimeout, OutputMode, GroupState}
// Update function, takes a key, an iterator of events and a previous state, returns an iterator which represents the
// rows of the output from flatMapGroupsWithState
def updateAcrossBatch(dummy:Int, inputs: Iterator[timedScoreCC], oldState: GroupState[TDigestSQL]):Iterator[TdigAndAnomaly] = {
	// state is the oldState if it exists otherwise we create an empty state to start from
  var state:TDigestSQL = if (oldState.exists) oldState.get else TDigestSQL(TDigest.empty())
  // We copy the traversableOnce iterator inputs into inputs1 and inputs2, this implies we need to discard inputs
  val (inputs1,inputs2) = inputs.duplicate
  // Loop to update the state, i.e. the tDigest
  for (input <- inputs1) {
    state = updateTDIG(state, input)
    oldState.update(state)
  }
  //Precompute the threshold for which we will sort the anomalies
  val cdfInv:Double = state.tdigest.cdfInverse(0.99)
  // Yields an iterator of anomalies
  val anomalies:Iterator[TdigAndAnomaly] = for(input <- inputs2; if (input.score > cdfInv)) yield TdigAndAnomaly(state,cdfInv,input.time,input.score)
  //Return the anomalies iterator, each item in the iterator gives a row in the output
  anomalies
}

import org.apache.spark.sql.streaming.GroupStateTimeout

val query = streamingLinesDS
  .groupByKey(x => 1)
  .flatMapGroupsWithState(OutputMode.Append,GroupStateTimeout.NoTimeout)(updateAcrossBatch)
  .writeStream
  .outputMode("append")
  .format("console")
  .start()
query.awaitTermination()

// COMMAND ----------

// MAGIC %md # Have fun
// MAGIC Arbitrary stateful aggregations are very powerful and you can really do a lot, especially if you are allowed to perform aggregations afterwards (flatmapGroupsWithState with Append mode). This is some really cool stuff!