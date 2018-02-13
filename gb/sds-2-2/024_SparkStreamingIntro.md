[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

**Introduction to Spark Streaming**
===================================

Spark Streaming is an extension of the core Spark API that enables scalable, high-throughput, fault-tolerant stream processing of live data streams.

This is a walk-through of excerpts from the following resources: \* the Databricks Guide: \* [Spark Streaming - RDD-based](https://docs.databricks.com/spark/latest/rdd-streaming/index.html) and \* [Structured Streaming - DatFrame/Dataset-Based](https://docs.databricks.com/spark/latest/structured-streaming/index.html#structured-streaming) \* Spark programming guide:
\* <http://spark.apache.org/docs/latest/streaming-programming-guide.html> \* <http://spark.apache.org/docs/latest/structured-streaming-programming-guide.html>

Overview
========

Spark Streaming is an extension of the core Spark API that enables scalable, high-throughput, fault-tolerant stream processing of live data streams.

Data can be ingested from many sources like

-   [Kafka](http://kafka.apache.org/documentation.html#introduction),
-   [Flume](https://flume.apache.org/),
-   [Twitter](https://twitter.com/) [Streaming](https://dev.twitter.com/streaming/overview) and [REST](https://dev.twitter.com/rest/public) APIs,
-   [ZeroMQ](http://zeromq.org/),
-   [Amazon Kinesis](https://aws.amazon.com/kinesis/streams/), or
-   [TCP sockets](http://www.gnu.org/software/mit-scheme/documentation/mit-scheme-ref/TCP-Sockets.html),
-   etc

and can be processed using complex algorithms expressed with high-level functions like `map`, `reduce`, `join` and `window`.

Finally, processed data can be pushed out to filesystems, databases, and live dashboards. In fact, you can apply Spark's \* [machine learning](http://spark.apache.org/docs/latest/mllib-guide.html) and \* [graph processing](http://spark.apache.org/docs/latest/graphx-programming-guide.html) algorithms on data streams.

![Spark Streaming architecture](http://spark.apache.org/docs/latest/img/streaming-arch.png)

#### Internally, it works as follows:

-   Spark Streaming receives live input data streams and
-   divides the data into batches,
-   which are then processed by the Spark engine
-   to generate the final stream of results in batches.

![Spark Streaming data flow](http://spark.apache.org/docs/latest/img/streaming-flow.png)

Spark Streaming provides a high-level abstraction called **discretized stream** or **DStream**, which represents a continuous stream of data. DStreams can be created either from input data streams from sources such as Kafka, Flume, and Kinesis, or by applying high-level operations on other DStreams. Internally, a **DStream is represented as a sequence of [RDDs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.RDD)**.

This guide shows you how to start writing Spark Streaming programs with DStreams. You can write Spark Streaming programs in Scala, Java or Python (introduced in Spark 1.2), all of which are presented in this [guide](http://spark.apache.org/docs/latest/streaming-programming-guide.html).

Here, we will focus on Streaming in Scala.

------------------------------------------------------------------------

**Spark Streaming** is a near-real-time micro-batch stream processing engine as opposed to other real-time stream processing frameworks like [Apache Storm](http://storm.apache.org/). Typically 'near-real-time' in Spark Streaming can be in the order of seconds as opposed to milliseconds, for example.

Three Quick Examples
====================

Before we go into the details of how to write your own Spark Streaming program, let us take a quick look at what a simple Spark Streaming program looks like.

We will choose the first two examples in Databricks notebooks below.

``` md #### Spark Streaming Hello World Examples

These are adapted from several publicly available Databricks Notebooks

1. Streaming Word Count (Scala)
* Tweet Collector for Capturing Live Tweets
* Twitter Hashtag Count (Scala)

Other examples we won't try here:
* Kinesis Word Count (Scala)
* Kafka Word Count (Scala)
* FileStream Word Count (Python)
* etc.
```

``` md ## 1. Streaming Word Count

This is a *hello world* example of Spark Streaming which counts words on 1 second batches of streaming data. 

It uses an in-memory string generator as a dummy source for streaming data.
```

Configurations
--------------

Configurations that control the streaming app in the notebook

``` scala
// === Configuration to control the flow of the application ===
val stopActiveContext = true	 
// "true"  = stop if any existing StreamingContext is running;              
// "false" = dont stop, and let it run undisturbed, but your latest code may not be used

// === Configurations for Spark Streaming ===
val batchIntervalSeconds = 1 
val eventsPerSecond = 1000    // For the dummy source

// Verify that the attached Spark cluster is 1.4.0+
require(sc.version.replace(".", "").toInt >= 140, "Spark 1.4.0+ is required to run this notebook. Please attach it to a Spark 1.4.0+ cluster.")
```

>     stopActiveContext: Boolean = true
>     batchIntervalSeconds: Int = 1
>     eventsPerSecond: Int = 1000

### Imports

Import all the necessary libraries. If you see any error here, you have to make sure that you have attached the necessary libraries to the attached cluster.

``` scala
import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._
```

>     import org.apache.spark._
>     import org.apache.spark.storage._
>     import org.apache.spark.streaming._

Discretized Streams (DStreams)
------------------------------

**Discretized Stream** or **DStream** is the basic abstraction provided by Spark Streaming. It represents a continuous stream of data, either the input data stream received from source, or the processed data stream generated by transforming the input stream. Internally, a DStream is represented by a continuous series of RDDs, which is Spark's abstraction of an immutable, distributed dataset (see [Spark Programming Guide](http://spark.apache.org/docs/latest/programming-guide.html#resilient-distributed-datasets-rdds) for more details). Each RDD in a DStream contains data from a certain interval, as shown in the following figure.

![Spark Streaming](http://spark.apache.org/docs/latest/img/streaming-dstream.png "Spark Streaming data flow")

### Setup: Define the function that sets up the StreamingContext

In this we will do two things. \* Define a custom receiver as the dummy source (no need to understand this) \* this custom receiver will have lines that end with a random number between 0 and 9 and read: `I am a dummy source 2    I am a dummy source 8    ...`

This is the dummy source implemented as a custom receiver. **No need to understand this now.**

``` scala
// This is the dummy source implemented as a custom receiver. No need to fully understand this.

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
```

>     import scala.util.Random
>     import org.apache.spark.streaming.receiver._
>     defined class DummySource

Let's try to understand the following `creatingFunc` to create a new StreamingContext and setting it up for word count and registering it as temp table for each batch of 1000 lines per second in the stream.

``` scala
var newContextCreated = false      // Flag to detect whether new context was created or not

// Function to create a new StreamingContext and set it up
def creatingFunc(): StreamingContext = {
    
  // Create a StreamingContext - starting point for a Spark Streaming job
  val ssc = new StreamingContext(sc, Seconds(batchIntervalSeconds))
  
  // Create a stream that generates 1000 lines per second
  val stream = ssc.receiverStream(new DummySource(eventsPerSecond))  
  
  // Split the lines into words, and then do word count
  val wordStream = stream.flatMap { _.split(" ")  }
  val wordCountStream = wordStream.map(word => (word, 1)).reduceByKey(_ + _)

  // Create temp table at every batch interval
  wordCountStream.foreachRDD { rdd => 
    rdd.toDF("word", "count").createOrReplaceTempView("batch_word_count")    
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
```

>     newContextCreated: Boolean = false
>     creatingFunc: ()org.apache.spark.streaming.StreamingContext

Transforming and Acting on the DStream of lines
-----------------------------------------------

Any operation applied on a DStream translates to operations on the underlying RDDs. For converting a stream of lines to words, the `flatMap` operation is applied on each RDD in the `lines` DStream to generate the RDDs of the `wordStream` DStream. This is shown in the following figure.

![Spark Streaming](http://spark.apache.org/docs/latest/img/streaming-dstream-ops.png "Spark Streaming data flow")

These underlying RDD transformations are computed by the Spark engine. The DStream operations hide most of these details and provide the developer with a higher-level API for convenience.

Next `reduceByKey` is used to get `wordCountStream` that counts the words in `wordStream`.

Finally, this is registered as a temporary table for each RDD in the DStream.

Start Streaming Job: Stop existing StreamingContext if any and start/restart the new one
----------------------------------------------------------------------------------------

Here we are going to use the configurations at the top of the notebook to decide whether to stop any existing StreamingContext, and start a new one, or recover one from existing checkpoints.

``` scala
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
```

>     Creating function called to create new StreamingContext
>     New context created from currently defined creating function
>     # events = 34
>     	 I am a dummy source 0, I am a dummy source 9, I am a dummy source 9, I am a dummy source 5, I am a dummy source 9, I am a dummy source 9, I am a dummy source 0, I am a dummy source 2, I am a dummy source 9, I am a dummy source 5, ...
>     # events = 891
>     	 I am a dummy source 7, I am a dummy source 1, I am a dummy source 1, I am a dummy source 8, I am a dummy source 2, I am a dummy source 2, I am a dummy source 4, I am a dummy source 7, I am a dummy source 7, I am a dummy source 7, ...
>     # events = 891
>     	 I am a dummy source 9, I am a dummy source 1, I am a dummy source 8, I am a dummy source 4, I am a dummy source 6, I am a dummy source 6, I am a dummy source 0, I am a dummy source 5, I am a dummy source 2, I am a dummy source 4, ...
>     # events = 892
>     	 I am a dummy source 1, I am a dummy source 2, I am a dummy source 8, I am a dummy source 5, I am a dummy source 5, I am a dummy source 9, I am a dummy source 7, I am a dummy source 2, I am a dummy source 2, I am a dummy source 6, ...
>     # events = 886
>     	 I am a dummy source 5, I am a dummy source 4, I am a dummy source 7, I am a dummy source 8, I am a dummy source 9, I am a dummy source 1, I am a dummy source 6, I am a dummy source 2, I am a dummy source 4, I am a dummy source 6, ...
>     ssc: org.apache.spark.streaming.StreamingContext = org.apache.spark.streaming.StreamingContext@5b8ab021
>     res2: Boolean = false

### Interactive Querying

Now let's try querying the table. You can run this command again and again, you will find the numbers changing.

| word   | count |
|--------|-------|
| 0      | 82.0  |
| 1      | 87.0  |
| 2      | 88.0  |
| source | 893.0 |
| 3      | 89.0  |
| 4      | 95.0  |
| 5      | 86.0  |
| 6      | 91.0  |
| 7      | 91.0  |
| 8      | 90.0  |
| dummy  | 893.0 |
| a      | 893.0 |
| 9      | 94.0  |
| I      | 893.0 |
| am     | 893.0 |

Try again for current table.

| word   | count |
|--------|-------|
| 0      | 92.0  |
| 1      | 81.0  |
| 2      | 96.0  |
| source | 892.0 |
| 3      | 80.0  |
| 4      | 92.0  |
| 5      | 97.0  |
| 6      | 76.0  |
| 7      | 79.0  |
| 8      | 92.0  |
| dummy  | 892.0 |
| a      | 892.0 |
| 9      | 107.0 |
| I      | 892.0 |
| am     | 892.0 |

``` md ### Finally, if you want stop the StreamingContext, you can uncomment and execute the following

`StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) }`
```

``` scala
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } // please do this if you are done!
```

Next - Spark Streaming of live tweets.
======================================

Let's do two more example applications of streaming involving live tweets.

### Go to Spark UI now and see Streaming job running