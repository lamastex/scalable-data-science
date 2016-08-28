// Databricks notebook source exported at Sun, 28 Aug 2016 16:07:32 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week6/12_SparkStreaming/021_SparkStreamingIntro.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week6/12_SparkStreaming/021_SparkStreamingIntro](http://img.youtube.com/vi/jqLcr2eS-Vs/0.jpg)](https://www.youtube.com/v/jqLcr2eS-Vs?rel=0&autoplay=1&modestbranding=1&start=0&end=2111)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # **Spark Streaming**
// MAGIC Spark Streaming is an extension of the core Spark API that enables scalable, high-throughput, fault-tolerant stream processing of live data streams. 
// MAGIC 
// MAGIC This is an augmentation of the following resources:
// MAGIC * the Databricks Guide [Workspace -> Databricks_Guide -> 08 Spark Streaming -> 00 Spark Streaming](/#workspace/databricks_guide/08 Spark Streaming/00 Spark Streaming) and 
// MAGIC * [http://spark.apache.org/docs/latest/streaming-programming-guide.html](http://spark.apache.org/docs/latest/streaming-programming-guide.html)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Overview
// MAGIC ========
// MAGIC 
// MAGIC Spark Streaming is an extension of the core Spark API that enables
// MAGIC scalable, high-throughput, fault-tolerant stream processing of live data
// MAGIC streams. 
// MAGIC 
// MAGIC Data can be ingested from many sources like 
// MAGIC 
// MAGIC * [Kafka](http://kafka.apache.org/documentation.html#introduction), 
// MAGIC * [Flume](https://flume.apache.org/),
// MAGIC * [Twitter](https://twitter.com/) [Streaming](https://dev.twitter.com/streaming/overview) and [REST](https://dev.twitter.com/rest/public) APIs, 
// MAGIC * [ZeroMQ](http://zeromq.org/), 
// MAGIC * [Amazon Kinesis](https://aws.amazon.com/kinesis/streams/), or 
// MAGIC * [TCP sockets](http://www.gnu.org/software/mit-scheme/documentation/mit-scheme-ref/TCP-Sockets.html), 
// MAGIC 
// MAGIC and can be processed using
// MAGIC complex algorithms expressed with high-level functions like `map`,
// MAGIC `reduce`, `join` and `window`. 
// MAGIC   
// MAGIC Finally, processed data can be pushed out
// MAGIC to filesystems, databases, and live dashboards. In fact, you can apply Spark's 
// MAGIC * [machine learning](http://spark.apache.org/docs/latest/mllib-guide.html) and
// MAGIC * [graph processing](http://spark.apache.org/docs/latest/graphx-programming-guide.html) algorithms 
// MAGIC on data streams.
// MAGIC 
// MAGIC ![Spark Streaming architecture](http://spark.apache.org/docs/latest/img/streaming-arch.png)
// MAGIC 
// MAGIC #### Internally, it works as follows: 
// MAGIC * Spark Streaming receives live input data streams and 
// MAGIC * divides the data into batches, 
// MAGIC * which are then processed by the Spark engine 
// MAGIC * to generate the final stream of results in batches.
// MAGIC 
// MAGIC ![Spark Streaming data flow](http://spark.apache.org/docs/latest/img/streaming-flow.png)
// MAGIC 
// MAGIC Spark Streaming provides a high-level abstraction called **discretized
// MAGIC stream** or **DStream**, which represents a continuous stream of data.
// MAGIC DStreams can be created either from input data streams from sources such
// MAGIC as Kafka, Flume, and Kinesis, or by applying high-level operations on
// MAGIC other DStreams. Internally, a **DStream is represented as a sequence of
// MAGIC [RDDs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.RDD)**.
// MAGIC 
// MAGIC This guide shows you how to start writing Spark Streaming programs with
// MAGIC DStreams. You can write Spark Streaming programs in Scala, Java or
// MAGIC Python (introduced in Spark 1.2), all of which are presented in this
// MAGIC [guide](http://spark.apache.org/docs/latest/streaming-programming-guide.html). 
// MAGIC 
// MAGIC Here, we will focus on Streaming in Scala.
// MAGIC 
// MAGIC * * * * *

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #### Spark Streaming Resources
// MAGIC * [Spark Streaming Programming Guide](https://spark.apache.org/docs/latest/streaming-programming-guide.html) - The official Apache Spark Streaming programming guide.
// MAGIC * [Debugging Spark Streaming in Databricks](/#workspace/databricks_guide/08 Spark Streaming/02 Debugging Spark Streaming Application)
// MAGIC * [Streaming FAQs and Best Practices](/#workspace/databricks_guide/08 Spark Streaming/15 Streaming FAQs)

// COMMAND ----------

// MAGIC %md
// MAGIC Three Quick Examples
// MAGIC ===============
// MAGIC 
// MAGIC Before we go into the details of how to write your own Spark Streaming
// MAGIC program, let?s take a quick look at what a simple Spark Streaming
// MAGIC program looks like. 
// MAGIC 
// MAGIC We will choose the first two examples in Databricks notebooks below.

// COMMAND ----------

// MAGIC %md #### Spark Streaming Hello World Examples in Databricks Notebooks
// MAGIC 
// MAGIC 1. [Streaming Word Count (Scala)](/#workspace/databricks_guide/08 Spark Streaming/01 Streaming Word Count - Scala)
// MAGIC * Tweet Collector for Capturing Live Tweets
// MAGIC * [Twitter Hashtag Count (Scala)](/#workspace/databricks_guide/08 Spark Streaming/03 Twitter Hashtag Count - Scala)
// MAGIC 
// MAGIC Other examples we won't try here:
// MAGIC * [Kinesis Word Count (Scala)](/#workspace/databricks_guide/08 Spark Streaming/04 Kinesis Word Count - Scala)
// MAGIC * [Kafka Word Count (Scala)](/#workspace/databricks_guide/08 Spark Streaming/05 Kafka Word Count - Scala)
// MAGIC * [FileStream Word Count (Python)](/#workspace/databricks_guide/08 Spark Streaming/06 FileStream Word Count - Python)

// COMMAND ----------

// MAGIC %md ## 1. Streaming Word Count
// MAGIC 
// MAGIC This is a *hello world* example of Spark Streaming which counts words on 1 second batches of streaming data. 
// MAGIC 
// MAGIC It uses an in-memory string generator as a dummy source for streaming data.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Configurations
// MAGIC 
// MAGIC Configurations that control the streaming app in the notebook

// COMMAND ----------

// === Configuration to control the flow of the application ===
val stopActiveContext = true	 
// "true"  = stop if any existing StreamingContext is running;              
// "false" = dont stop, and let it run undisturbed, but your latest code may not be used

// === Configurations for Spark Streaming ===
val batchIntervalSeconds = 1 
val eventsPerSecond = 1000    // For the dummy source

// Verify that the attached Spark cluster is 1.4.0+
require(sc.version.replace(".", "").toInt >= 140, "Spark 1.4.0+ is required to run this notebook. Please attach it to a Spark 1.4.0+ cluster.")


// COMMAND ----------

// MAGIC %md 
// MAGIC ### Imports
// MAGIC 
// MAGIC Import all the necessary libraries. If you see any error here, you have to make sure that you have attached the necessary libraries to the attached cluster. 

// COMMAND ----------

import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._

// COMMAND ----------

// MAGIC %md
// MAGIC Discretized Streams (DStreams)
// MAGIC ------------------------------
// MAGIC 
// MAGIC **Discretized Stream** or **DStream** is the basic abstraction provided
// MAGIC by Spark Streaming. It represents a continuous stream of data, either
// MAGIC the input data stream received from source, or the processed data stream
// MAGIC generated by transforming the input stream. Internally, a DStream is
// MAGIC represented by a continuous series of RDDs, which is Spark?s abstraction
// MAGIC of an immutable, distributed dataset (see [Spark Programming
// MAGIC Guide](http://spark.apache.org/docs/latest/programming-guide.html#resilient-distributed-datasets-rdds)
// MAGIC for more details). Each RDD in a DStream contains data from a certain
// MAGIC interval, as shown in the following figure.
// MAGIC 
// MAGIC ![Spark
// MAGIC Streaming](http://spark.apache.org/docs/latest/img/streaming-dstream.png "Spark Streaming data flow")

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Setup: Define the function that sets up the StreamingContext
// MAGIC 
// MAGIC In this we will do two things. 
// MAGIC * Define a custom receiver as the dummy source (no need to understand this)
// MAGIC   * this custom receiver will have lines that end with a random number between 0 and 9 and read: 
// MAGIC    ```
// MAGIC    I am a dummy source 2
// MAGIC    I am a dummy source 8
// MAGIC    ...
// MAGIC    ```

// COMMAND ----------

// MAGIC %md
// MAGIC This is the dummy source implemented as a custom receiver. **No need to understand this now.**

// COMMAND ----------

// This is the dummy source implemented as a custom receiver. No need to understand this.

import scala.util.Random
import org.apache.spark.streaming.receiver._

class DummySource(ratePerSec: Int) extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) {

  def onStart() {
    // Start the thread that receives data over a connection
    new Thread("Dummy Source") {
      override def run() { receive() }
    }.start()
  }

  def onStop() {
   // There is nothing much to do as the thread calling receive()
   // is designed to stop by itself isStopped() returns false
  }

  /** Create a socket connection and receive data until receiver is stopped */
  private def receive() {
    while(!isStopped()) {      
      store("I am a dummy source " + Random.nextInt(10))
      Thread.sleep((1000.toDouble / ratePerSec).toInt)
    }
  }
}

// COMMAND ----------

// MAGIC %md
// MAGIC Let's try to understand the following `creatingFunc` to create a new StreamingContext and setting it up for word count and registering it as temp table for each batch of 1000 lines per second in the stream.

// COMMAND ----------

var newContextCreated = false      // Flag to detect whether new context was created or not

// Function to create a new StreamingContext and set it up
def creatingFunc(): StreamingContext = {
    
  // Create a StreamingContext
  val ssc = new StreamingContext(sc, Seconds(batchIntervalSeconds))
  
  // Create a stream that generates 1000 lines per second
  val stream = ssc.receiverStream(new DummySource(eventsPerSecond))  
  
  // Split the lines into words, and then do word count
  val wordStream = stream.flatMap { _.split(" ")  }
  val wordCountStream = wordStream.map(word => (word, 1)).reduceByKey(_ + _)

  // Create temp table at every batch interval
  wordCountStream.foreachRDD { rdd => 
    rdd.toDF("word", "count").registerTempTable("batch_word_count")    
  }
  
  stream.foreachRDD { rdd =>
    System.out.println("# events = " + rdd.count())
    System.out.println("\t " + rdd.take(10).mkString(", ") + ", ...")
  }
  
  ssc.remember(Minutes(1))  // To make sure data is not deleted by the time we query it interactively
  
  println("Creating function called to create new StreamingContext")
  newContextCreated = true  
  ssc
}

// COMMAND ----------

// MAGIC %md
// MAGIC ## Transforming and Acting on the DStream of lines
// MAGIC 
// MAGIC Any operation applied on a DStream translates to operations on the
// MAGIC underlying RDDs. For converting
// MAGIC a stream of lines to words, the `flatMap` operation is applied on each
// MAGIC RDD in the `lines` DStream to generate the RDDs of the `wordStream` DStream.
// MAGIC This is shown in the following figure.
// MAGIC 
// MAGIC ![Spark
// MAGIC Streaming](http://spark.apache.org/docs/latest/img/streaming-dstream-ops.png "Spark Streaming data flow")
// MAGIC 
// MAGIC These underlying RDD transformations are computed by the Spark engine.
// MAGIC The DStream operations hide most of these details and provide the
// MAGIC developer with a higher-level API for convenience. 
// MAGIC 
// MAGIC Next `reduceByKey` is used to get `wordCountStream` that counts the words in `wordStream`.
// MAGIC 
// MAGIC Finally, this is registered as a temporary table for each RDD in the DStream.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Start Streaming Job: Stop existing StreamingContext if any and start/restart the new one
// MAGIC 
// MAGIC Here we are going to use the configurations at the top of the notebook to decide whether to stop any existing StreamingContext, and start a new one, or recover one from existing checkpoints.

// COMMAND ----------

// Stop any existing StreamingContext 
// The getActive function is proviced by Databricks to access active Streaming Contexts
if (stopActiveContext) {	
  StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) }
} 

// Get or create a streaming context
val ssc = StreamingContext.getActiveOrCreate(creatingFunc)
if (newContextCreated) {
  println("New context created from currently defined creating function") 
} else {
  println("Existing context running or recovered from checkpoint, may not be running currently defined creating function")
}

// Start the streaming context in the background.
ssc.start()

// This is to ensure that we wait for some time before the background streaming job starts. This will put this cell on hold for 5 times the batchIntervalSeconds.
ssc.awaitTerminationOrTimeout(batchIntervalSeconds * 5 * 1000)


// COMMAND ----------

// MAGIC %md 
// MAGIC ### Interactive Querying
// MAGIC 
// MAGIC Now let's try querying the table. You can run this command again and again, you will find the numbers changing.

// COMMAND ----------

// MAGIC %sql select * from batch_word_count

// COMMAND ----------

// MAGIC %md
// MAGIC Try again for current table.

// COMMAND ----------

// MAGIC %sql select * from batch_word_count 

// COMMAND ----------

// MAGIC %md ### Finally, if you want stop the StreamingContext, you can uncomment and execute the following
// MAGIC 
// MAGIC `StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) }`

// COMMAND ----------

StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) }

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC # Let's do two more example applications of streaming involving live tweets.

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # More Pointers to Spark Streaming

// COMMAND ----------

// MAGIC %md 
// MAGIC # Spark Streaming Common Sinks
// MAGIC 
// MAGIC * [Writing data to Kinesis](/#workspace/databricks_guide/08 Spark Streaming/07 Write Output To Kinesis)
// MAGIC * [Writing data to S3](/#workspace/databricks_guide/08 Spark Streaming/08 Write Output To S3)
// MAGIC * [Writing data to Kafka](/#workspace/databricks_guide/08 Spark Streaming/09 Write Output To Kafka)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Writing to S3
// MAGIC 
// MAGIC We will be storing large amounts of data in s3, [Amazon's simple storage service](https://aws.amazon.com/s3/).

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Spark Streaming Tutorials
// MAGIC * [Window Aggregations in Streaming](/#workspace/databricks_guide/08 Spark Streaming/10 Window Aggregations) - Has examples for the different window aggregations available in spark streaming
// MAGIC * [Global Aggregations using updateStateByKey](/#workspace/databricks_guide/08 Spark Streaming/11 Global Aggregations - updateStateByKey) - Provides an example of how to do global aggregations
// MAGIC * [Global Aggregations using mapWithState](/#workspace/databricks_guide/08 Spark Streaming/12 Global Aggregations - mapWithState) - From Spark 1.6, you can use the `mapWithState` interface to do global aggregations more efficiently.
// MAGIC * [Joining DStreams](/#workspace/databricks_guide/08 Spark Streaming/13 Joining DStreams) - Has an example for joining 2 dstreams
// MAGIC * [Joining DStreams with static datasets](/#workspace/databricks_guide/08 Spark Streaming/14 Joining DStreams With Static Datasets) - Builds on the previous example and shows how to join DStreams with static dataframes or RDDs efficiently

// COMMAND ----------

// MAGIC %md 
// MAGIC # Example Streaming Producers
// MAGIC * [Kinesis Word Producer](/#workspace/databricks_guide/08 Spark Streaming/Producers/1 Kinesis Word Producer)
// MAGIC * [Kafka Word Producer](/#workspace/databricks_guide/08 Spark Streaming/Producers/2 Kafka Word Producer)
// MAGIC * [Kafka Ads Data Producer](/#workspace/databricks_guide/08 Spark Streaming/Producers/3 Kafka Ads Data Producer)

// COMMAND ----------

// MAGIC %md 
// MAGIC # Spark Streaming Applications
// MAGIC 
// MAGIC * [Sessionization - Building Sessions from Streams](/#workspace/databricks_guide/08 Spark Streaming/Applications/01 Sessionization)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)