// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/fXo7AfE3Umg/0.jpg)](https://www.youtube.com/embed/fXo7AfE3Umg?start=0&end=2670&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/sAttqpQq4nA/0.jpg)](https://www.youtube.com/embed/sAttqpQq4nA?start=0&end=2630&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/byAZtT_EdO4/0.jpg)](https://www.youtube.com/embed/byAZtT_EdO4?start=0&end=1045&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/bhHH74vkqHE/0.jpg)](https://www.youtube.com/embed/bhHH74vkqHE?start=0&end=487&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Structured Streaming - A Programming Guide Walkthrough
// MAGIC 
// MAGIC -   [Overview](https://spark.apache.org/docs/2.2.0/index.html)
// MAGIC -   [Programming Guides](structured-streaming-programming-guide.html#)
// MAGIC    
// MAGIC 
// MAGIC -   [Overview](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#overview)
// MAGIC -   [Quick
// MAGIC     Example](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#quick-example)
// MAGIC -   [Programming
// MAGIC     Model](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#programming-model)
// MAGIC     -   [Basic
// MAGIC         Concepts](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#basic-concepts)
// MAGIC     -   [Handling Event-time and Late
// MAGIC         Data](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#handling-event-time-and-late-data)
// MAGIC     -   [Fault Tolerance
// MAGIC         Semantics](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#fault-tolerance-semantics)
// MAGIC -   [API using Datasets and
// MAGIC     DataFrames](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#api-using-datasets-and-dataframes)
// MAGIC     -   [Creating streaming DataFrames and streaming
// MAGIC         Datasets](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#creating-streaming-dataframes-and-streaming-datasets)
// MAGIC         -   [Input
// MAGIC             Sources](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#input-sources)
// MAGIC         -   [Schema inference and partition of streaming
// MAGIC             DataFrames/Datasets](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#schema-inference-and-partition-of-streaming-dataframesdatasets)
// MAGIC     -   [Operations on streaming
// MAGIC         DataFrames/Datasets](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#operations-on-streaming-dataframesdatasets)
// MAGIC         -   [Basic Operations - Selection, Projection,
// MAGIC             Aggregation](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#basic-operations---selection-projection-aggregation)
// MAGIC         -   [Window Operations on Event
// MAGIC             Time](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#window-operations-on-event-time)
// MAGIC         -   [Handling Late Data and
// MAGIC             Watermarking](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#handling-late-data-and-watermarking)
// MAGIC         -   [Join
// MAGIC             Operations](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#join-operations)
// MAGIC         -   [Streaming
// MAGIC             Deduplication](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#streaming-deduplication)
// MAGIC         -   [Arbitrary Stateful
// MAGIC             Operations](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#arbitrary-stateful-operations)
// MAGIC         -   [Unsupported
// MAGIC             Operations](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#unsupported-operations)
// MAGIC     -   [Starting Streaming
// MAGIC         Queries](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#starting-streaming-queries)
// MAGIC         -   [Output
// MAGIC             Modes](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#output-modes)
// MAGIC         -   [Output
// MAGIC             Sinks](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#output-sinks)
// MAGIC         -   [Using
// MAGIC             Foreach](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#using-foreach)
// MAGIC     -   [Managing Streaming
// MAGIC         Queries](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#managing-streaming-queries)
// MAGIC     -   [Monitoring Streaming
// MAGIC         Queries](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#monitoring-streaming-queries)
// MAGIC         -   [Interactive
// MAGIC             APIs](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#interactive-apis)
// MAGIC         -   [Asynchronous
// MAGIC             API](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#asynchronous-api)
// MAGIC     -   [Recovering from Failures with
// MAGIC         Checkpointing](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#recovering-from-failures-with-checkpointing)
// MAGIC -   [Where to go from
// MAGIC     here](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#where-to-go-from-here)
// MAGIC     - [https://databricks.com/blog/2017/08/24/anthology-of-technical-assets-on-apache-sparks-structured-streaming.html](https://databricks.com/blog/2017/08/24/anthology-of-technical-assets-on-apache-sparks-structured-streaming.html)
// MAGIC     - An authoritative resource: [https://www.gitbook.com/book/jaceklaskowski/spark-structured-streaming/details](https://www.gitbook.com/book/jaceklaskowski/spark-structured-streaming/details)

// COMMAND ----------

// MAGIC %md
// MAGIC Overview
// MAGIC ========
// MAGIC 
// MAGIC Structured Streaming is a scalable and fault-tolerant stream processing
// MAGIC engine built on the Spark SQL engine. You can express your streaming
// MAGIC computation the same way you would express a batch computation on static
// MAGIC data. The Spark SQL engine will take care of running it incrementally
// MAGIC and continuously and updating the final result as streaming data
// MAGIC continues to arrive. You can use the [Dataset/DataFrame
// MAGIC API](https://spark.apache.org/docs/2.2.0/sql-programming-guide.html) in
// MAGIC Scala, Java, Python or R to express streaming aggregations, event-time
// MAGIC windows, stream-to-batch joins, etc. The computation is executed on the
// MAGIC same optimized Spark SQL engine. Finally, the system ensures end-to-end
// MAGIC exactly-once fault-tolerance guarantees through checkpointing and Write
// MAGIC Ahead Logs. In short, *Structured Streaming provides fast, scalable,
// MAGIC fault-tolerant, end-to-end exactly-once stream processing without the
// MAGIC user having to reason about streaming.*
// MAGIC 
// MAGIC In this guide, we are going to walk you through the programming model
// MAGIC and the APIs. First, let’s start with a simple example - a streaming
// MAGIC word count.

// COMMAND ----------

// MAGIC %md
// MAGIC Programming Model
// MAGIC =================
// MAGIC 
// MAGIC The key idea in Structured Streaming is to treat a live data stream as a
// MAGIC table that is being continuously appended. This leads to a new stream
// MAGIC processing model that is very similar to a batch processing model. You
// MAGIC will express your streaming computation as standard batch-like query as
// MAGIC on a static table, and Spark runs it as an *incremental* query on the
// MAGIC *unbounded* input table. Let’s understand this model in more detail.
// MAGIC 
// MAGIC Basic Concepts
// MAGIC --------------
// MAGIC 
// MAGIC Consider the input data stream as the “Input Table”. Every data item
// MAGIC that is arriving on the stream is like a new row being appended to the
// MAGIC Input Table.
// MAGIC 
// MAGIC ![Stream as a
// MAGIC Table](https://spark.apache.org/docs/2.2.0/img/structured-streaming-stream-as-a-table.png "Stream as a Table")
// MAGIC 
// MAGIC A query on the input will generate the “Result Table”. Every trigger
// MAGIC interval (say, every 1 second), new rows get appended to the Input
// MAGIC Table, which eventually updates the Result Table. Whenever the result
// MAGIC table gets updated, we would want to write the changed result rows to an
// MAGIC external sink.
// MAGIC 
// MAGIC ![Model](https://spark.apache.org/docs/2.2.0/img/structured-streaming-model.png)
// MAGIC 
// MAGIC The “Output” is defined as what gets written out to the external
// MAGIC storage. The output can be defined in a different mode:
// MAGIC 
// MAGIC -   *Complete Mode* - The entire updated Result Table will be written to
// MAGIC     the external storage. It is up to the storage connector to decide
// MAGIC     how to handle writing of the entire table.
// MAGIC 
// MAGIC -   *Append Mode* - Only the new rows appended in the Result Table since
// MAGIC     the last trigger will be written to the external storage. This is
// MAGIC     applicable only on the queries where existing rows in the Result
// MAGIC     Table are not expected to change.
// MAGIC 
// MAGIC -   *Update Mode* - Only the rows that were updated in the Result Table
// MAGIC     since the last trigger will be written to the external storage
// MAGIC     (available since Spark 2.1.1). Note that this is different from the
// MAGIC     Complete Mode in that this mode only outputs the rows that have
// MAGIC     changed since the last trigger. If the query doesn’t contain
// MAGIC     aggregations, it will be equivalent to Append mode.
// MAGIC 
// MAGIC Note that each mode is applicable on certain types of queries. This is
// MAGIC discussed in detail later on *output-modes*.
// MAGIC To illustrate the use of this model, let’s understand the model in
// MAGIC context of the **Quick Example** above. 
// MAGIC 
// MAGIC The first `streamingLines` DataFrame is the input table, and the final
// MAGIC `wordCounts` DataFrame is the result table. Note that the query on
// MAGIC `streamingLines` DataFrame to generate `wordCounts` is *exactly the
// MAGIC same* as it would be a static DataFrame. However, when this query is
// MAGIC started, Spark will continuously check for new data from the directory. 
// MAGIC If there is new data, Spark will run an “incremental” query
// MAGIC that combines the previous running counts with the new data to compute
// MAGIC updated counts, as shown below.
// MAGIC 
// MAGIC ![Model](https://spark.apache.org/docs/2.2.0/img/structured-streaming-example-model.png)
// MAGIC 
// MAGIC This model is significantly different from many other stream processing
// MAGIC engines. Many streaming systems require the user to maintain running
// MAGIC aggregations themselves, thus having to reason about fault-tolerance,
// MAGIC and data consistency (at-least-once, or at-most-once, or exactly-once).
// MAGIC In this model, Spark is responsible for updating the Result Table when
// MAGIC there is new data, thus relieving the users from reasoning about it. As
// MAGIC an example, let’s see how this model handles event-time based processing
// MAGIC and late arriving data.

// COMMAND ----------

// MAGIC %md
// MAGIC Quick Example
// MAGIC =============
// MAGIC 
// MAGIC Let’s say you want to maintain a running word count of text data
// MAGIC received from a file writer that is writing files into a directory 
// MAGIC `datasets/streamingFiles` in the distributed file  system. Let’s see how you
// MAGIC can express this using Structured Streaming. 
// MAGIC 
// MAGIC Let’s walk through the
// MAGIC example step-by-step and understand how it works. 
// MAGIC 
// MAGIC **First** we need to start a file writing job in the companion notebook `037a_AnimalNamesStructStreamingFiles` and then return here.

// COMMAND ----------

display(dbutils.fs.ls("/datasets/streamingFiles"))

// COMMAND ----------

dbutils.fs.head("/datasets/streamingFiles/00_00.log")

// COMMAND ----------

// MAGIC %md
// MAGIC Next, let’s create a streaming DataFrame that represents text data
// MAGIC received from the directory, and transform the
// MAGIC DataFrame to calculate word counts.

// COMMAND ----------

import org.apache.spark.sql.types._

// Create DataFrame representing the stream of input lines from files in distributed file store
//val textFileSchema = new StructType().add("line", "string") // for a custom schema

val streamingLines = spark
  .readStream
  //.schema(textFileSchema) // using default -> makes a column of String named value
  .option("MaxFilesPerTrigger", 1) //  maximum number of new files to be considered in every trigger (default: no max) 
  .format("text")
  .load("/datasets/streamingFiles")

// COMMAND ----------

// MAGIC %md
// MAGIC This `streamingLines` DataFrame represents an unbounded table containing the
// MAGIC streaming text data. This table contains one column of strings named
// MAGIC “value”, and each line in the streaming text data becomes a row in the
// MAGIC table. Note, that this is not currently receiving any data as we are
// MAGIC just setting up the transformation, and have not yet started it. 

// COMMAND ----------

display(streamingLines)  // display will show you the contents of the DF

// COMMAND ----------

// MAGIC 
// MAGIC %md
// MAGIC Next, we will convert the DataFrame to a Dataset of String using
// MAGIC `.as[String]`, so that we can apply the `flatMap` operation to split
// MAGIC each line into multiple words. The resultant `words` Dataset contains
// MAGIC all the words. 

// COMMAND ----------

val words = streamingLines.as[String]
                          .map(line => line.split(";").drop(1)(0)) // this is to simply cut out the timestamp from this stream
                          .flatMap(_.split(" ")) // flat map by splitting the animal words separated by whitespace
                          .filter( _ != "") // remove empty words that may be artifacts of opening whitespace

// COMMAND ----------

// MAGIC %md
// MAGIC Finally, we define the `wordCounts` DataFrame by
// MAGIC grouping by the unique values in the Dataset and counting them. Note
// MAGIC that this is a streaming DataFrame which represents the **running word
// MAGIC counts of the stream**.

// COMMAND ----------

// Generate running word count
val wordCounts = words
                  .groupBy("value").count() // this does the word count
                  .orderBy($"count".desc) // we are simply sorting by the most frequent words

// COMMAND ----------

// MAGIC %md
// MAGIC We have now set up the query on the streaming data. All that is left is
// MAGIC to actually start receiving data and computing the counts. To do this,
// MAGIC we set it up to print the complete set of counts (specified by
// MAGIC `outputMode("complete")`) to the console every time they are updated.
// MAGIC And then start the streaming computation using `start()`.

// COMMAND ----------

// Start running the query that prints the running counts to the console
val query = wordCounts.writeStream
      .outputMode("complete")
      .format("console")
      .start()

query.awaitTermination() // hit cancel to terminate - killall the bash script in 037a_AnimalNamesStructStreamingFiles

// COMMAND ----------

// MAGIC %md
// MAGIC After this code is executed, the streaming computation will have started
// MAGIC in the background. The `query` object is a handle to that active
// MAGIC streaming query, and we have decided to wait for the termination of the
// MAGIC query using `awaitTermination()` to prevent the process from exiting
// MAGIC while the query is active.

// COMMAND ----------

// MAGIC %md
// MAGIC Handling Event-time and Late Data
// MAGIC ---------------------------------
// MAGIC 
// MAGIC Event-time is the time embedded in the data itself. For many
// MAGIC applications, you may want to operate on this event-time. For example,
// MAGIC if you want to get the number of events generated by IoT devices every
// MAGIC minute, then you probably want to use the time when the data was
// MAGIC generated (that is, event-time in the data), rather than the time Spark
// MAGIC receives them. This event-time is very naturally expressed in this model
// MAGIC – each event from the devices is a row in the table, and event-time is a
// MAGIC column value in the row. This allows window-based aggregations (e.g.
// MAGIC number of events every minute) to be just a special type of grouping and
// MAGIC aggregation on the event-time column – each time window is a group and
// MAGIC each row can belong to multiple windows/groups. Therefore, such
// MAGIC event-time-window-based aggregation queries can be defined consistently
// MAGIC on both a static dataset (e.g. from collected device events logs) as
// MAGIC well as on a data stream, making the life of the user much easier.
// MAGIC 
// MAGIC Furthermore, this model naturally handles data that has arrived later
// MAGIC than expected based on its event-time. Since Spark is updating the
// MAGIC Result Table, it has full control over updating old aggregates when
// MAGIC there is late data, as well as cleaning up old aggregates to limit the
// MAGIC size of intermediate state data. Since Spark 2.1, we have support for
// MAGIC watermarking which allows the user to specify the threshold of late
// MAGIC data, and allows the engine to accordingly clean up old state. These are
// MAGIC explained later in more detail in the **Window Operations** section below.
// MAGIC 
// MAGIC Fault Tolerance Semantics
// MAGIC -------------------------
// MAGIC 
// MAGIC Delivering end-to-end exactly-once semantics was one of key goals behind
// MAGIC the design of Structured Streaming. To achieve that, we have designed
// MAGIC the Structured Streaming sources, the sinks and the execution engine to
// MAGIC reliably track the exact progress of the processing so that it can
// MAGIC handle any kind of failure by restarting and/or reprocessing. Every
// MAGIC streaming source is assumed to have offsets (similar to Kafka offsets,
// MAGIC or Kinesis sequence numbers) to track the read position in the stream.
// MAGIC The engine uses checkpointing and write ahead logs to record the offset
// MAGIC range of the data being processed in each trigger. The streaming sinks
// MAGIC are designed to be idempotent for handling reprocessing. Together, using
// MAGIC replayable sources and idempotent sinks, Structured Streaming can ensure
// MAGIC **end-to-end exactly-once semantics** under any failure.
// MAGIC 
// MAGIC API using Datasets and DataFrames
// MAGIC =================================
// MAGIC 
// MAGIC Since Spark 2.0, DataFrames and Datasets can represent static, bounded
// MAGIC data, as well as streaming, unbounded data. Similar to static
// MAGIC Datasets/DataFrames, you can use the common entry point `SparkSession`
// MAGIC ([Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.SparkSession)/[Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/SparkSession.html)/[Python](https://spark.apache.org/docs/2.2.0/api/python/pyspark.sql.html#pyspark.sql.SparkSession)/[R](https://spark.apache.org/docs/2.2.0/api/R/sparkR.session.html)
// MAGIC docs) to create streaming DataFrames/Datasets from streaming sources,
// MAGIC and apply the same operations on them as static DataFrames/Datasets. If
// MAGIC you are not familiar with Datasets/DataFrames, you are strongly advised
// MAGIC to familiarize yourself with them using the [DataFrame/Dataset
// MAGIC Programming
// MAGIC Guide](https://spark.apache.org/docs/2.2.0/sql-programming-guide.html).
// MAGIC 
// MAGIC Creating streaming DataFrames and streaming Datasets
// MAGIC ----------------------------------------------------
// MAGIC 
// MAGIC Streaming DataFrames can be created through the `DataStreamReader`
// MAGIC interface
// MAGIC ([Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.streaming.DataStreamReader)/[Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/streaming/DataStreamReader.html)/[Python](https://spark.apache.org/docs/2.2.0/api/python/pyspark.sql.html#pyspark.sql.streaming.DataStreamReader)
// MAGIC docs) returned by `SparkSession.readStream()`. In
// MAGIC [R](https://spark.apache.org/docs/2.2.0/api/R/read.stream.html), with
// MAGIC the `read.stream()` method. Similar to the read interface for creating
// MAGIC static DataFrame, you can specify the details of the source – data
// MAGIC format, schema, options, etc.
// MAGIC 
// MAGIC #### Input Sources
// MAGIC 
// MAGIC In Spark 2.0, there are a few built-in sources.
// MAGIC 
// MAGIC -   **File source** - Reads files written in a directory as a stream
// MAGIC     of data. Supported file formats are text, csv, json, parquet. See
// MAGIC     the docs of the DataStreamReader interface for a more up-to-date
// MAGIC     list, and supported options for each file format. Note that the
// MAGIC     files must be atomically placed in the given directory, which in
// MAGIC     most file systems, can be achieved by file move operations.
// MAGIC 
// MAGIC -   **Kafka source** - Poll data from Kafka. It’s compatible with Kafka
// MAGIC     broker versions 0.10.0 or higher. See the [Kafka Integration
// MAGIC     Guide](https://spark.apache.org/docs/2.2.0/structured-streaming-kafka-integration.html)
// MAGIC     for more details.
// MAGIC 
// MAGIC -   **Socket source (for testing)** - Reads UTF8 text data from a
// MAGIC     socket connection. The listening server socket is at the driver.
// MAGIC     Note that this should be used only for testing as this does not
// MAGIC     provide end-to-end fault-tolerance guarantees.
// MAGIC 
// MAGIC Some sources are not fault-tolerant because they do not guarantee that
// MAGIC data can be replayed using checkpointed offsets after a failure. See the
// MAGIC earlier section on [fault-tolerance
// MAGIC semantics](structured-streaming-programming-guide.html#fault-tolerance-semantics).
// MAGIC Here are the details of all the sources in Spark.
// MAGIC 
// MAGIC <table class="table">
// MAGIC   <tr>
// MAGIC     <th>Source</th>
// MAGIC     <th>Options</th>
// MAGIC     <th>Fault-tolerant</th>
// MAGIC     <th>Notes</th>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td><b>File source</b></td>
// MAGIC     <td>
// MAGIC         <code>path</code>: path to the input directory, and common to all file formats.
// MAGIC         <br />
// MAGIC         <code>maxFilesPerTrigger</code>: maximum number of new files to be considered in every trigger (default: no max)
// MAGIC         <br />
// MAGIC         <code>latestFirst</code>: whether to processs the latest new files first, useful when there is a large backlog of files (default: false)
// MAGIC         <br />
// MAGIC         <code>fileNameOnly</code>: whether to check new files based on only the filename instead of on the full path (default: false). With this set to `true`, the following files would be considered as the same file, because their filenames, "dataset.txt", are the same:
// MAGIC         <br />
// MAGIC         · "file:///dataset.txt"<br />
// MAGIC         · "s3://a/dataset.txt"<br />
// MAGIC         · "s3n://a/b/dataset.txt"<br />
// MAGIC         · "s3a://a/b/c/dataset.txt"<br />
// MAGIC         <br />
// MAGIC 
// MAGIC         <br />
// MAGIC         For file-format-specific options, see the related methods in <code>DataStreamReader</code>
// MAGIC         (<a href="https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.streaming.DataStreamReader">Scala</a>/<a href="https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/streaming/DataStreamReader.html">Java</a>/<a href="https://spark.apache.org/docs/2.2.0/api/python/pyspark.sql.html#pyspark.sql.streaming.DataStreamReader">Python</a>/<a href="https://spark.apache.org/docs/2.2.0/api/R/read.stream.html">R</a>).
// MAGIC         E.g. for "parquet" format options see <code>DataStreamReader.parquet()</code></td>
// MAGIC     <td>Yes</td>
// MAGIC     <td>Supports glob paths, but does not support multiple comma-separated paths/globs.</td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td><b>Socket Source</b></td>
// MAGIC     <td>
// MAGIC         <code>host</code>: host to connect to, must be specified<br />
// MAGIC         <code>port</code>: port to connect to, must be specified
// MAGIC     </td>
// MAGIC     <td>No</td>
// MAGIC     <td></td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td><b>Kafka Source</b></td>
// MAGIC     <td>
// MAGIC         See the <a href="https://spark.apache.org/docs/2.2.0/structured-streaming-kafka-integration.html">Kafka Integration Guide</a>.
// MAGIC     </td>
// MAGIC     <td>Yes</td>
// MAGIC     <td></td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC   </tr>
// MAGIC </table>
// MAGIC 
// MAGIC See [https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#input-sources](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#input-sources).
// MAGIC 
// MAGIC ### Schema inference and partition of streaming DataFrames/Datasets
// MAGIC 
// MAGIC By default, Structured Streaming from file based sources requires you to
// MAGIC specify the schema, rather than rely on Spark to infer it automatically (*this is what we did with `userSchema` above*).
// MAGIC This restriction ensures a consistent schema will be used for the
// MAGIC streaming query, even in the case of failures. For ad-hoc use cases, you
// MAGIC can reenable schema inference by setting
// MAGIC `spark.sql.streaming.schemaInference` to `true`.
// MAGIC 
// MAGIC Partition discovery does occur when subdirectories that are named
// MAGIC `/key=value/` are present and listing will automatically recurse into
// MAGIC these directories. If these columns appear in the user provided schema,
// MAGIC they will be filled in by Spark based on the path of the file being
// MAGIC read. The directories that make up the partitioning scheme must be
// MAGIC present when the query starts and must remain static. For example, it is
// MAGIC okay to add `/data/year=2016/` when `/data/year=2015/` was present, but
// MAGIC it is invalid to change the partitioning column (i.e. by creating the
// MAGIC directory `/data/date=2016-04-17/`).
// MAGIC 
// MAGIC Operations on streaming DataFrames/Datasets
// MAGIC -------------------------------------------
// MAGIC 
// MAGIC You can apply all kinds of operations on streaming DataFrames/Datasets –
// MAGIC ranging from untyped, SQL-like operations (e.g. `select`, `where`,
// MAGIC `groupBy`), to typed RDD-like operations (e.g. `map`, `filter`,
// MAGIC `flatMap`). See the [SQL programming
// MAGIC guide](https://spark.apache.org/docs/2.2.0/sql-programming-guide.html)
// MAGIC for more details. Let’s take a look at a few example operations that you
// MAGIC can use.
// MAGIC 
// MAGIC ### Basic Operations - Selection, Projection, Aggregation
// MAGIC 
// MAGIC Most of the common operations on DataFrame/Dataset are supported for
// MAGIC streaming. The few operations that are not supported are discussed
// MAGIC later in **unsupported-operations** section.
// MAGIC 
// MAGIC ```%scala
// MAGIC     case class DeviceData(device: String, deviceType: String, signal: Double, time: DateTime)
// MAGIC 
// MAGIC     val df: DataFrame = ... // streaming DataFrame with IOT device data with schema { device: string, deviceType: string, signal: double, time: string }
// MAGIC     val ds: Dataset[DeviceData] = df.as[DeviceData]    // streaming Dataset with IOT device data
// MAGIC 
// MAGIC     // Select the devices which have signal more than 10
// MAGIC     df.select("device").where("signal > 10")      // using untyped APIs
// MAGIC     ds.filter(_.signal > 10).map(_.device)         // using typed APIs
// MAGIC 
// MAGIC     // Running count of the number of updates for each device type
// MAGIC     df.groupBy("deviceType").count()                          // using untyped API
// MAGIC 
// MAGIC     // Running average signal for each device type
// MAGIC     import org.apache.spark.sql.expressions.scalalang.typed
// MAGIC     ds.groupByKey(_.deviceType).agg(typed.avg(_.signal))    // using typed API
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC ## A Quick Mixture Example
// MAGIC 
// MAGIC We will work below with a file stream that simulates random animal names or a simple mixture of two Normal Random Variables.
// MAGIC 
// MAGIC The two file streams can be acieved by running the codes in the following two databricks notebooks in the same cluster:
// MAGIC 
// MAGIC * `037a_AnimalNamesStructStreamingFiles`
// MAGIC * `037b_Mix2NormalsStructStreamingFiles`

// COMMAND ----------

// MAGIC %md
// MAGIC You should have the following set of csv files (it won't be exactly the same names depending on when you start the stream of files).

// COMMAND ----------

display(dbutils.fs.ls("/datasets/streamingFilesNormalMixture/"))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Static and Streaming DataFrames
// MAGIC 
// MAGIC Let's check out the files and their contents both via static as well as streaming DataFrames. 
// MAGIC 
// MAGIC This will also cement the fact that structured streaming allows interoperability between static and streaming data and can be useful for debugging.

// COMMAND ----------

val peekIn = spark.read.format("csv").load("/datasets/streamingFilesNormalMixture/*/*.csv")
peekIn.count() // total count of all the samples in all the files

// COMMAND ----------

peekIn.show(5, false) // let's take a quick peek at what's in the CSV files

// COMMAND ----------

// Read all the csv files written atomically from a directory
import org.apache.spark.sql.types._

//make a user-specified schema - this is needed for structured streaming from files
val userSchema = new StructType()
                      .add("time", "timestamp")
                      .add("score", "Double")

// a static DF is convenient 
val csvStaticDF = spark
  .read
  .option("sep", ",") // delimiter is ','
  .schema(userSchema) // Specify schema of the csv files as pre-defined by user
  .csv("/datasets/streamingFilesNormalMixture/*/*.csv")    // Equivalent to format("csv").load("/path/to/directory")

// streaming DF
val csvStreamingDF = spark
  .readStream
  .option("sep", ",") // delimiter is ','
  .schema(userSchema) // Specify schema of the csv files as pre-defined by user
  .option("MaxFilesPerTrigger", 1) //  maximum number of new files to be considered in every trigger (default: no max) 
  .csv("/datasets/streamingFilesNormalMixture/*/*.csv")    // Equivalent to format("csv").load("/path/to/directory")

// COMMAND ----------

csvStreamingDF.isStreaming    // Returns True for DataFrames that have streaming sources

// COMMAND ----------

csvStreamingDF.printSchema

// COMMAND ----------

//display(csvStreamingDF) // if you want to see the stream coming at you as csvDF

// COMMAND ----------

import org.apache.spark.sql.functions._

// Start running the query that prints the running counts to the console
val query = csvStreamingDF
                 // bround simply rounds the double to the desired decimal place - 0 in our case here. 
                   // see https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/functions.html#bround-org.apache.spark.sql.Column-
                   // we are using bround to simply coarsen out data into bins for counts
                 .select(bround($"score", 0).as("binnedScore")) 
                 .groupBy($"binnedScore")
                 .agg(count($"binnedScore") as "binnedScoreCounts")
                 .orderBy($"binnedScore")
                 .writeStream
                 .outputMode("complete")
                 .format("console")
                 .start()
                 
query.awaitTermination() // hit cancel to terminate

// COMMAND ----------

// MAGIC %md
// MAGIC Once the above streaming job has processed all the files in the directory, it will continue to "listen" in for new files in the directory. You could for example return to the other notebook `037b_Mix2NormalsStructStreamingFiles` and rerun the cell that writes another lot of newer files into the directory and return to this notebook to watch the above streaming job continue with additional batches.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Static and Streaming DataSets
// MAGIC 
// MAGIC These examples generate streaming DataFrames that are untyped, meaning
// MAGIC that the schema of the DataFrame is not checked at compile time, only
// MAGIC checked at runtime when the query is submitted. Some operations like
// MAGIC `map`, `flatMap`, etc. need the type to be known at compile time. To do
// MAGIC those, you can convert these untyped streaming DataFrames to typed
// MAGIC streaming Datasets using the same methods as static DataFrame. See the
// MAGIC [SQL Programming Guide](https://spark.apache.org/docs/2.2.0/sql-programming-guide.html)
// MAGIC for more details. Additionally, more details on the supported streaming
// MAGIC sources are discussed later in the document.
// MAGIC 
// MAGIC Let us make a `dataset` version of the streaming dataframe.
// MAGIC 
// MAGIC But first let us try it make the datset from the static dataframe and then apply it to the streming dataframe.

// COMMAND ----------

csvStaticDF.printSchema // schema of the static DF

// COMMAND ----------

import org.apache.spark.sql.types._
import java.sql.Timestamp

// create a case class to make the datset
case class timedScores(time: Timestamp, score: Double)

val csvStaticDS = csvStaticDF.as[timedScores] // create a dataset from the dataframe

// COMMAND ----------

csvStaticDS.show(5,false) // looks like we got the dataset we want with strong typing

// COMMAND ----------

// MAGIC %md
// MAGIC Now let us use the same code for making a streaming dataset.

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import java.sql.Timestamp

// create a case class to make the datset
case class timedScores(time: Timestamp, score: Double)

val csvStreamingDS = csvStreamingDF.as[timedScores] // create a dataset from the dataframe

// Start running the query that prints the running counts to the console
val query = csvStreamingDS
                  // bround simply rounds the double to the desired decimal place - 0 in our case here. 
                   // see https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/functions.html#bround-org.apache.spark.sql.Column-
                   // we are using bround to simply coarsen out data into bins for counts
                 .select(bround($"score", 0).as("binnedScore")) 
                 .groupBy($"binnedScore")
                 .agg(count($"binnedScore") as "binnedScoreCounts")
                 .orderBy($"binnedScore")
                 .writeStream
                 .outputMode("complete")
                 .format("console")
                 .start()

query.awaitTermination() // hit cancel to terminate

// COMMAND ----------

// MAGIC %md
// MAGIC ### Window Operations on Event Time
// MAGIC 
// MAGIC Aggregations over a sliding event-time window are straightforward with
// MAGIC Structured Streaming and are very similar to grouped aggregations. In a
// MAGIC grouped aggregation, aggregate values (e.g. counts) are maintained for
// MAGIC each unique value in the user-specified grouping column. In case of
// MAGIC window-based aggregations, aggregate values are maintained for each
// MAGIC window the event-time of a row falls into. Let’s understand this with an
// MAGIC illustration.
// MAGIC 
// MAGIC Imagine our **quick example** is
// MAGIC modified and the stream now contains lines along with the time when the
// MAGIC line was generated. Instead of running word counts, we want to count
// MAGIC words within 10 minute windows, updating every 5 minutes. That is, word
// MAGIC counts in words received between 10 minute windows 
// MAGIC 12:00 - 12:10, 12:05 - 12:15, 12:10 - 12:20, etc. Note that 12:00 - 12:10 means data that
// MAGIC arrived after 12:00 but before 12:10. Now, consider a word that was
// MAGIC received at 12:07. This word should increment the counts corresponding
// MAGIC to two windows 12:00 - 12:10 and 12:05 - 12:15. So the counts will be
// MAGIC indexed by both, the grouping key (i.e. the word) and the window (can be
// MAGIC calculated from the event-time).
// MAGIC 
// MAGIC The result tables would look something like the following.
// MAGIC 
// MAGIC ![Window
// MAGIC Operations](https://spark.apache.org/docs/2.2.0/img/structured-streaming-window.png)
// MAGIC 
// MAGIC Since this windowing is similar to grouping, in code, you can use
// MAGIC `groupBy()` and `window()` operations to express windowed aggregations.
// MAGIC You can see the full code for the below examples in
// MAGIC [Scala](https://github.com/apache/spark/blob/v2.2.0/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredNetworkWordCountWindowed.scala)/[Java](https://github.com/apache/spark/blob/v2.2.0/examples/src/main/java/org/apache/spark/examples/sql/streaming/JavaStructuredNetworkWordCountWindowed.java)/[Python](https://github.com/apache/spark/blob/v2.2.0/examples/src/main/python/sql/streaming/structured_network_wordcount_windowed.py).

// COMMAND ----------

// MAGIC %md
// MAGIC Make sure the streaming job with animal names is running (or finished running) with files in `/datasets/streamingFiles` directory - this is the Quick Example in `037a_FilesForStructuredStreaming` notebook.

// COMMAND ----------

display(dbutils.fs.ls("/datasets/streamingFiles"))

// COMMAND ----------

spark.read.format("text").load("/datasets/streamingFiles").show(5,false) // let's just read five  entries

// COMMAND ----------

import spark.implicits._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import java.sql.Timestamp

// a static DS is convenient to work with
val csvStaticDS = spark
   .read
   .option("sep", ";") // delimiter is ';'
   .csv("/datasets/streamingFiles/*.log")    // Equivalent to format("csv").load("/path/to/directory")
   .toDF("time","animals")
   .as[(Timestamp, String)]
   .flatMap(
     line => line._2.split(" ")
                 .filter(_ != "") // Gustav's improvement
                 .map(animal => (line._1, animal))
    )
   //.filter(_._2 != "") // remove empty strings from the leading whitespaces
   .toDF("timestamp", "animal")
   .as[(Timestamp, String)]

// COMMAND ----------

csvStaticDS.show(5,false)

// COMMAND ----------

//make a user-specified schema for structured streaming
val userSchema = new StructType()
                      .add("time", "String") // we will read it as String and then convert into timestamp later
                      .add("animals", "String")

// streaming DS
val csvStreamingDS = spark
// the next three lines are needed for structured streaming from file streams
  .readStream // for streaming
  .option("MaxFilesPerTrigger", 1) //  for streaming
  .schema(userSchema) // for streaming
  .option("sep", ";") // delimiter is ';'
  .csv("/datasets/streamingFiles/*.log")    // Equivalent to format("csv").load("/path/to/directory")
  .toDF("time","animals")
  .as[(Timestamp, String)]
  .flatMap(
     line => line._2.split(" ").map(animal => (line._1, animal))
    )
  .filter(_._2 != "")
  .toDF("timestamp", "animal")
  .as[(Timestamp, String)]

// COMMAND ----------

display(csvStreamingDS) // evaluate to see the animal words with timestamps streaming in

// COMMAND ----------

// Group the data by window and word and compute the count of each group
val windowDuration = "180 seconds"
val slideDuration = "90 seconds"
val windowedCounts = csvStreamingDS.groupBy(
      window($"timestamp", windowDuration, slideDuration), $"animal"
    ).count().orderBy("window")

// Start running the query that prints the windowed word counts to the console
val query = windowedCounts.writeStream
      .outputMode("complete")
      .format("console")
      .option("truncate", "false")
      .start()

query.awaitTermination()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Handling Late Data and Watermarking
// MAGIC 
// MAGIC Now consider what happens if one of the events arrives late to the
// MAGIC application. For example, say, a word generated at 12:04 (i.e. event
// MAGIC time) could be received by the application at 12:11. The application
// MAGIC should use the time 12:04 instead of 12:11 to update the older counts
// MAGIC for the window `12:00 - 12:10`. This occurs naturally in our
// MAGIC window-based grouping – Structured Streaming can maintain the
// MAGIC intermediate state for partial aggregates for a long period of time such
// MAGIC that late data can update aggregates of old windows correctly, as
// MAGIC illustrated below.
// MAGIC 
// MAGIC ![Handling Late Data](https://spark.apache.org/docs/2.2.0/img/structured-streaming-late-data.png)
// MAGIC 
// MAGIC However, to run this query for days, it’s necessary for the system to
// MAGIC bound the amount of intermediate in-memory state it accumulates. This
// MAGIC means the system needs to know when an old aggregate can be dropped from
// MAGIC the in-memory state because the application is not going to receive late
// MAGIC data for that aggregate any more. To enable this, in Spark 2.1, we have
// MAGIC introduced **watermarking**, which lets the engine automatically track
// MAGIC the current event time in the data and attempt to clean up old state
// MAGIC accordingly. You can define the watermark of a query by specifying the
// MAGIC event time column and the threshold on how late the data is expected to
// MAGIC be in terms of event time. For a specific window starting at time `T`,
// MAGIC the engine will maintain state and allow late data to update the state
// MAGIC until `(max event time seen by the engine - late threshold > T)`. In
// MAGIC other words, late data within the threshold will be aggregated, but data
// MAGIC later than the threshold will be dropped. Let’s understand this with an
// MAGIC example. We can easily define watermarking on the previous example using
// MAGIC `withWatermark()` as shown below.

// COMMAND ----------

// Group the data by window and word and compute the count of each group
val windowDuration = "180 seconds"
val slideDuration = "90 seconds"
val watermarkDuration = "10 minutes"
val windowedCounts = csvStreamingDS
     .withWatermark("timestamp", watermarkDuration)
     .groupBy(
      window($"timestamp", windowDuration, slideDuration), $"animal"
    ).count().orderBy("window")

// Start running the query that prints the windowed word counts to the console
val query = windowedCounts.writeStream
      .outputMode("complete")
      .format("console")
      .option("truncate", "false")
      .start()

query.awaitTermination()

// COMMAND ----------

// MAGIC %md
// MAGIC In this example, we are defining the watermark of the query on the value
// MAGIC of the column “timestamp”, and also defining “10 minutes” as the
// MAGIC threshold of how late is the data allowed to be. If this query is run in
// MAGIC Update output mode (discussed later in [Output
// MAGIC Modes](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#output-modes)
// MAGIC section), the engine will keep updating counts of a window in the Result
// MAGIC Table until the window is older than the watermark, which lags behind
// MAGIC the current event time in column “timestamp” by 10 minutes. Here is an
// MAGIC illustration.
// MAGIC 
// MAGIC ![Watermarking in Update
// MAGIC Mode](https://spark.apache.org/docs/2.2.0/img/structured-streaming-watermark-update-mode.png)
// MAGIC 
// MAGIC As shown in the illustration, the maximum event time tracked by the
// MAGIC engine is the *blue dashed line*, and the watermark set as
// MAGIC `(max event time - '10 mins')` at the beginning of every trigger is the
// MAGIC red line For example, when the engine observes the data `(12:14, dog)`,
// MAGIC it sets the watermark for the next trigger as `12:04`. This watermark
// MAGIC lets the engine maintain intermediate state for additional 10 minutes to
// MAGIC allow late data to be counted. For example, the data `(12:09, cat)` is
// MAGIC out of order and late, and it falls in windows `12:05 - 12:15` and
// MAGIC `12:10 - 12:20`. Since, it is still ahead of the watermark `12:04` in
// MAGIC the trigger, the engine still maintains the intermediate counts as state
// MAGIC and correctly updates the counts of the related windows. However, when
// MAGIC the watermark is updated to `12:11`, the intermediate state for window
// MAGIC `(12:00 - 12:10)` is cleared, and all subsequent data (e.g.
// MAGIC `(12:04, donkey)`) is considered “too late” and therefore ignored. Note
// MAGIC that after every trigger, the updated counts (i.e. purple rows) are
// MAGIC written to sink as the trigger output, as dictated by the Update mode.
// MAGIC 
// MAGIC Some sinks (e.g. files) may not supported fine-grained updates that
// MAGIC Update Mode requires. To work with them, we have also support Append
// MAGIC Mode, where only the *final counts* are written to sink. This is
// MAGIC illustrated below.
// MAGIC 
// MAGIC Note that using `withWatermark` on a non-streaming Dataset is no-op. As
// MAGIC the watermark should not affect any batch query in any way, we will
// MAGIC ignore it directly.
// MAGIC 
// MAGIC ![Watermarking in Append
// MAGIC Mode](https://spark.apache.org/docs/2.2.0/img/structured-streaming-watermark-append-mode.png)
// MAGIC 
// MAGIC Similar to the Update Mode earlier, the engine maintains intermediate
// MAGIC counts for each window. However, the partial counts are not updated to
// MAGIC the Result Table and not written to sink. The engine waits for “10 mins”
// MAGIC for late date to be counted, then drops intermediate state of a window
// MAGIC &lt; watermark, and appends the final counts to the Result Table/sink.
// MAGIC For example, the final counts of window `12:00 - 12:10` is appended to
// MAGIC the Result Table only after the watermark is updated to `12:11`.
// MAGIC 
// MAGIC **Conditions for watermarking to clean aggregation state** It is
// MAGIC important to note that the following conditions must be satisfied for
// MAGIC the watermarking to clean the state in aggregation queries *(as of Spark
// MAGIC 2.1.1, subject to change in the future)*.
// MAGIC 
// MAGIC -   **Output mode must be Append or Update.** Complete mode requires all
// MAGIC     aggregate data to be preserved, and hence cannot use watermarking to
// MAGIC     drop intermediate state. See the [Output
// MAGIC     Modes](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#output-modes)
// MAGIC     section for detailed explanation of the semantics of each
// MAGIC     output mode.
// MAGIC 
// MAGIC -   The aggregation must have either the event-time column, or a
// MAGIC     `window` on the event-time column.
// MAGIC 
// MAGIC -   `withWatermark` must be called on the same column as the timestamp
// MAGIC     column used in the aggregate. For example,
// MAGIC     `df.withWatermark("time", "1 min").groupBy("time2").count()` is
// MAGIC     invalid in Append output mode, as watermark is defined on a
// MAGIC     different column from the aggregation column.
// MAGIC 
// MAGIC -   `withWatermark` must be called before the aggregation for the
// MAGIC     watermark details to be used. For example,
// MAGIC     `df.groupBy("time").count().withWatermark("time", "1 min")` is
// MAGIC     invalid in Append output mode.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Join Operations
// MAGIC 
// MAGIC Streaming DataFrames can be joined with static DataFrames to create new
// MAGIC streaming DataFrames. Here are a few examples.

// COMMAND ----------

// MAGIC %md
// MAGIC ```
// MAGIC val staticDf = spark.read. ...
// MAGIC val streamingDf = spark.readStream. ...
// MAGIC 
// MAGIC streamingDf.join(staticDf, "type")          // inner equi-join with a static DF
// MAGIC streamingDf.join(staticDf, "type", "right_join")  // right outer join with a static DF
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC ### Streaming Deduplication
// MAGIC 
// MAGIC You can deduplicate records in data streams using a unique identifier in
// MAGIC the events. This is exactly same as deduplication on static using a
// MAGIC unique identifier column. The query will store the necessary amount of
// MAGIC data from previous records such that it can filter duplicate records.
// MAGIC Similar to aggregations, you can use deduplication with or without
// MAGIC watermarking.
// MAGIC 
// MAGIC -   *With watermark* - If there is a upper bound on how late a duplicate
// MAGIC     record may arrive, then you can define a watermark on a event time
// MAGIC     column and deduplicate using both the guid and the event
// MAGIC     time columns. The query will use the watermark to remove old state
// MAGIC     data from past records that are not expected to get any duplicates
// MAGIC     any more. This bounds the amount of the state the query has
// MAGIC     to maintain.
// MAGIC 
// MAGIC -   *Without watermark* - Since there are no bounds on when a duplicate
// MAGIC     record may arrive, the query stores the data from all the past
// MAGIC     records as state.
// MAGIC 
// MAGIC ```
// MAGIC     val streamingDf = spark.readStream. ...  // columns: guid, eventTime, ...
// MAGIC 
// MAGIC     // Without watermark using guid column
// MAGIC     streamingDf.dropDuplicates("guid")
// MAGIC 
// MAGIC     // With watermark using guid and eventTime columns
// MAGIC     streamingDf
// MAGIC       .withWatermark("eventTime", "10 seconds")
// MAGIC       .dropDuplicates("guid", "eventTime")
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC ### Arbitrary Stateful Operations
// MAGIC 
// MAGIC Many uscases require more advanced stateful operations than
// MAGIC aggregations. For example, in many usecases, you have to track sessions
// MAGIC from data streams of events. For doing such sessionization, you will
// MAGIC have to save arbitrary types of data as state, and perform arbitrary
// MAGIC operations on the state using the data stream events in every trigger.
// MAGIC Since Spark 2.2, this can be done using the operation
// MAGIC `mapGroupsWithState` and the more powerful operation
// MAGIC `flatMapGroupsWithState`. Both operations allow you to apply
// MAGIC user-defined code on grouped Datasets to update user-defined state. For
// MAGIC more concrete details, take a look at the API documentation
// MAGIC ([Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.streaming.GroupState)/[Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/streaming/GroupState.html))
// MAGIC and the examples
// MAGIC ([Scala](https://github.com/apache/spark/blob/v2.2.0/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredSessionization.scala)/[Java](https://github.com/apache/spark/blob/v2.2.0/examples/src/main/java/org/apache/spark/examples/sql/streaming/JavaStructuredSessionization.java)).
// MAGIC 
// MAGIC ### Unsupported Operations
// MAGIC 
// MAGIC There are a few DataFrame/Dataset operations that are not supported with
// MAGIC streaming DataFrames/Datasets. Some of them are as follows.
// MAGIC 
// MAGIC -   Multiple streaming aggregations (i.e. a chain of aggregations on a
// MAGIC     streaming DF) are not yet supported on streaming Datasets.
// MAGIC 
// MAGIC -   Limit and take first N rows are not supported on streaming Datasets.
// MAGIC 
// MAGIC -   Distinct operations on streaming Datasets are not supported.
// MAGIC 
// MAGIC -   Sorting operations are supported on streaming Datasets only after an
// MAGIC     aggregation and in Complete Output Mode.
// MAGIC 
// MAGIC -   Outer joins between a streaming and a static Datasets are
// MAGIC     conditionally supported.
// MAGIC 
// MAGIC     -   Full outer join with a streaming Dataset is not supported
// MAGIC 
// MAGIC     -   Left outer join with a streaming Dataset on the right is not
// MAGIC         supported
// MAGIC 
// MAGIC     -   Right outer join with a streaming Dataset on the left is not
// MAGIC         supported
// MAGIC 
// MAGIC -   Any kind of joins between two streaming Datasets is not
// MAGIC     yet supported.
// MAGIC 
// MAGIC In addition, there are some Dataset methods that will not work on
// MAGIC streaming Datasets. They are actions that will immediately run queries
// MAGIC and return results, which does not make sense on a streaming Dataset.
// MAGIC Rather, those functionalities can be done by explicitly starting a
// MAGIC streaming query (see the next section regarding that).
// MAGIC 
// MAGIC -   `count()` - Cannot return a single count from a streaming Dataset.
// MAGIC     Instead, use `ds.groupBy().count()` which returns a streaming
// MAGIC     Dataset containing a running count.
// MAGIC 
// MAGIC -   `foreach()` - Instead use `ds.writeStream.foreach(...)` (see
// MAGIC     next section).
// MAGIC 
// MAGIC -   `show()` - Instead use the console sink (see next section).
// MAGIC 
// MAGIC If you try any of these operations, you will see an `AnalysisException`
// MAGIC like “operation XYZ is not supported with streaming
// MAGIC DataFrames/Datasets”. While some of them may be supported in future
// MAGIC releases of Spark, there are others which are fundamentally hard to
// MAGIC implement on streaming data efficiently. For example, sorting on the
// MAGIC input stream is not supported, as it requires keeping track of all the
// MAGIC data received in the stream. This is therefore fundamentally hard to
// MAGIC execute efficiently.
// MAGIC 
// MAGIC Starting Streaming Queries
// MAGIC --------------------------
// MAGIC 
// MAGIC Once you have defined the final result DataFrame/Dataset, all that is
// MAGIC left is for you to start the streaming computation. To do that, you have
// MAGIC to use the `DataStreamWriter`
// MAGIC ([Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.streaming.DataStreamWriter)/[Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/streaming/DataStreamWriter.html)/[Python](https://spark.apache.org/docs/2.2.0/api/python/pyspark.sql.html#pyspark.sql.streaming.DataStreamWriter)
// MAGIC docs) returned through `Dataset.writeStream()`. You will have to specify
// MAGIC one or more of the following in this interface.
// MAGIC 
// MAGIC -   *Details of the output sink:* Data format, location, etc.
// MAGIC 
// MAGIC -   *Output mode:* Specify what gets written to the output sink.
// MAGIC 
// MAGIC -   *Query name:* Optionally, specify a unique name of the query
// MAGIC     for identification.
// MAGIC 
// MAGIC -   *Trigger interval:* Optionally, specify the trigger interval. If it
// MAGIC     is not specified, the system will check for availability of new data
// MAGIC     as soon as the previous processing has completed. If a trigger time
// MAGIC     is missed because the previous processing has not completed, then
// MAGIC     the system will attempt to trigger at the next trigger point, not
// MAGIC     immediately after the processing has completed.
// MAGIC 
// MAGIC -   *Checkpoint location:* For some output sinks where the end-to-end
// MAGIC     fault-tolerance can be guaranteed, specify the location where the
// MAGIC     system will write all the checkpoint information. This should be a
// MAGIC     directory in an HDFS-compatible fault-tolerant file system. The
// MAGIC     semantics of checkpointing is discussed in more detail in the
// MAGIC     next section.
// MAGIC 
// MAGIC #### Output Modes
// MAGIC 
// MAGIC There are a few types of output modes.
// MAGIC 
// MAGIC -   **Append mode (default)** - This is the default mode, where only the
// MAGIC     new rows added to the Result Table since the last trigger will be
// MAGIC     outputted to the sink. This is supported for only those queries
// MAGIC     where rows added to the Result Table is never going to change.
// MAGIC     Hence, this mode guarantees that each row will be output only once
// MAGIC     (assuming fault-tolerant sink). For example, queries with only
// MAGIC     `select`, `where`, `map`, `flatMap`, `filter`, `join`, etc. will
// MAGIC     support Append mode.
// MAGIC 
// MAGIC -   **Complete mode** - The whole Result Table will be outputted to the
// MAGIC     sink after every trigger. This is supported for aggregation queries.
// MAGIC 
// MAGIC -   **Update mode** - (*Available since Spark 2.1.1*) Only the rows in
// MAGIC     the Result Table that were updated since the last trigger will be
// MAGIC     outputted to the sink. More information to be added in
// MAGIC     future releases.
// MAGIC 
// MAGIC Different types of streaming queries support different output modes.
// MAGIC Here is the compatibility matrix.
// MAGIC 
// MAGIC 
// MAGIC <table class="table">
// MAGIC   <tr>
// MAGIC     <th>Query Type</th>
// MAGIC     <th></th>
// MAGIC     <th>Supported Output Modes</th>
// MAGIC     <th>Notes</th>        
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td rowspan="2" style="vertical-align: middle;">Queries with aggregation</td>
// MAGIC     <td style="vertical-align: middle;">Aggregation on event-time with watermark</td>
// MAGIC     <td style="vertical-align: middle;">Append, Update, Complete</td>
// MAGIC     <td>
// MAGIC         Append mode uses watermark to drop old aggregation state. But the output of a 
// MAGIC         windowed aggregation is delayed the late threshold specified in `withWatermark()` as by
// MAGIC         the modes semantics, rows can be added to the Result Table only once after they are 
// MAGIC         finalized (i.e. after watermark is crossed). See the
// MAGIC         <a href="structured-streaming-programming-guide.html#handling-late-data-and-watermarking">Late Data</a> section for more details.
// MAGIC         <br /><br />
// MAGIC         Update mode uses watermark to drop old aggregation state.
// MAGIC         <br /><br />
// MAGIC         Complete mode does not drop old aggregation state since by definition this mode
// MAGIC         preserves all data in the Result Table.
// MAGIC     </td>    
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td style="vertical-align: middle;">Other aggregations</td>
// MAGIC     <td style="vertical-align: middle;">Complete, Update</td>
// MAGIC     <td>
// MAGIC         Since no watermark is defined (only defined in other category), 
// MAGIC         old aggregation state is not dropped.
// MAGIC         <br /><br />
// MAGIC         Append mode is not supported as aggregates can update thus violating the semantics of 
// MAGIC         this mode.
// MAGIC     </td>  
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td colspan="2" style="vertical-align: middle;">Queries with <code>mapGroupsWithState</code></td>
// MAGIC     <td style="vertical-align: middle;">Update</td>
// MAGIC     <td style="vertical-align: middle;"></td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td rowspan="2" style="vertical-align: middle;">Queries with <code>flatMapGroupsWithState</code></td>
// MAGIC     <td style="vertical-align: middle;">Append operation mode</td>
// MAGIC     <td style="vertical-align: middle;">Append</td>
// MAGIC     <td style="vertical-align: middle;">
// MAGIC       Aggregations are allowed after <code>flatMapGroupsWithState</code>.
// MAGIC     </td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td style="vertical-align: middle;">Update operation mode</td>
// MAGIC     <td style="vertical-align: middle;">Update</td>
// MAGIC     <td style="vertical-align: middle;">
// MAGIC       Aggregations not allowed after <code>flatMapGroupsWithState</code>.
// MAGIC     </td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td colspan="2" style="vertical-align: middle;">Other queries</td>
// MAGIC     <td style="vertical-align: middle;">Append, Update</td>
// MAGIC     <td style="vertical-align: middle;">
// MAGIC       Complete mode not supported as it is infeasible to keep all unaggregated data in the Result Table.
// MAGIC     </td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC   </tr>
// MAGIC </table>

// COMMAND ----------

// MAGIC %md
// MAGIC #### Output Sinks
// MAGIC 
// MAGIC There are a few types of built-in output sinks.
// MAGIC 
// MAGIC -   **File sink** - Stores the output to a directory.
// MAGIC 
// MAGIC ```
// MAGIC     writeStream
// MAGIC         .format("parquet")        // can be "orc", "json", "csv", etc.
// MAGIC         .option("path", "path/to/destination/dir")
// MAGIC         .start()
// MAGIC 
// MAGIC ```
// MAGIC -   **Foreach sink** - Runs arbitrary computation on the records in
// MAGIC     the output. See later in the section for more details.
// MAGIC 
// MAGIC ```
// MAGIC     writeStream
// MAGIC         .foreach(...)
// MAGIC         .start()
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC -   **Console sink (for debugging)** - Prints the output to the
// MAGIC     console/stdout every time there is a trigger. Both, Append and
// MAGIC     Complete output modes, are supported. This should be used for
// MAGIC     debugging purposes on low data volumes as the entire output is
// MAGIC     collected and stored in the driver’s memory after every trigger.
// MAGIC 
// MAGIC ```
// MAGIC     writeStream
// MAGIC         .format("console")
// MAGIC         .start()
// MAGIC ```
// MAGIC 
// MAGIC -   **Memory sink (for debugging)** - The output is stored in memory as
// MAGIC     an in-memory table. Both, Append and Complete output modes,
// MAGIC     are supported. This should be used for debugging purposes on low
// MAGIC     data volumes as the entire output is collected and stored in the
// MAGIC     driver’s memory. Hence, use it with caution.
// MAGIC 
// MAGIC ```
// MAGIC     writeStream
// MAGIC         .format("memory")
// MAGIC         .queryName("tableName")
// MAGIC         .start()
// MAGIC ```
// MAGIC 
// MAGIC Some sinks are not fault-tolerant because they do not guarantee
// MAGIC persistence of the output and are meant for debugging purposes only. See
// MAGIC the earlier section on [fault-tolerance
// MAGIC semantics](https://spark.apache.org/docs/2.2.0/structured-streaming-programming-guide.html#fault-tolerance-semantics).
// MAGIC Here are the details of all the sinks in Spark.
// MAGIC 
// MAGIC <table class="table">
// MAGIC   <tr>
// MAGIC     <th>Sink</th>
// MAGIC     <th>Supported Output Modes</th>
// MAGIC     <th>Options</th>
// MAGIC     <th>Fault-tolerant</th>
// MAGIC     <th>Notes</th>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td><b>File Sink</b></td>
// MAGIC     <td>Append</td>
// MAGIC     <td>
// MAGIC         <code>path</code>: path to the output directory, must be specified.
// MAGIC         <br /><br />
// MAGIC         For file-format-specific options, see the related methods in DataFrameWriter
// MAGIC         (<a href="https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.DataFrameWriter">Scala</a>/<a href="https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/DataFrameWriter.html">Java</a>/<a href="https://spark.apache.org/docs/2.2.0/api/python/pyspark.sql.html#pyspark.sql.DataFrameWriter">Python</a>/<a href="https://spark.apache.org/docs/2.2.0/api/R/write.stream.html">R</a>).
// MAGIC         E.g. for "parquet" format options see <code>DataFrameWriter.parquet()</code>
// MAGIC     </td>
// MAGIC     <td>Yes</td>
// MAGIC     <td>Supports writes to partitioned tables. Partitioning by time may be useful.</td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td><b>Foreach Sink</b></td>
// MAGIC     <td>Append, Update, Compelete</td>
// MAGIC     <td>None</td>
// MAGIC     <td>Depends on ForeachWriter implementation</td>
// MAGIC     <td>More details in the <a href="structured-streaming-programming-guide.html#using-foreach">next section</a></td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td><b>Console Sink</b></td>
// MAGIC     <td>Append, Update, Complete</td>
// MAGIC     <td>
// MAGIC         <code>numRows</code>: Number of rows to print every trigger (default: 20)
// MAGIC         <br />
// MAGIC         <code>truncate</code>: Whether to truncate the output if too long (default: true)
// MAGIC     </td>
// MAGIC     <td>No</td>
// MAGIC     <td></td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td><b>Memory Sink</b></td>
// MAGIC     <td>Append, Complete</td>
// MAGIC     <td>None</td>
// MAGIC     <td>No. But in Complete Mode, restarted query will recreate the full table.</td>
// MAGIC     <td>Table name is the query name.</td>
// MAGIC   </tr>
// MAGIC   <tr>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC     <td></td>
// MAGIC   </tr>
// MAGIC </table>

// COMMAND ----------

// MAGIC %md
// MAGIC Note that you have to call `start()` to actually start the execution of
// MAGIC the query. This returns a StreamingQuery object which is a handle to the
// MAGIC continuously running execution. You can use this object to manage the
// MAGIC query, which we will discuss in the next subsection. For now, let’s
// MAGIC understand all this with a few examples.
// MAGIC 
// MAGIC ```
// MAGIC     // ========== DF with no aggregations ==========
// MAGIC     val noAggDF = deviceDataDf.select("device").where("signal > 10")   
// MAGIC 
// MAGIC     // Print new data to console
// MAGIC     noAggDF
// MAGIC       .writeStream
// MAGIC       .format("console")
// MAGIC       .start()
// MAGIC 
// MAGIC     // Write new data to Parquet files
// MAGIC     noAggDF
// MAGIC       .writeStream
// MAGIC       .format("parquet")
// MAGIC       .option("checkpointLocation", "path/to/checkpoint/dir")
// MAGIC       .option("path", "path/to/destination/dir")
// MAGIC       .start()
// MAGIC 
// MAGIC     // ========== DF with aggregation ==========
// MAGIC     val aggDF = df.groupBy("device").count()
// MAGIC 
// MAGIC     // Print updated aggregations to console
// MAGIC     aggDF
// MAGIC       .writeStream
// MAGIC       .outputMode("complete")
// MAGIC       .format("console")
// MAGIC       .start()
// MAGIC 
// MAGIC     // Have all the aggregates in an in-memory table
// MAGIC     aggDF
// MAGIC       .writeStream
// MAGIC       .queryName("aggregates")    // this query name will be the table name
// MAGIC       .outputMode("complete")
// MAGIC       .format("memory")
// MAGIC       .start()
// MAGIC 
// MAGIC     spark.sql("select * from aggregates").show()   // interactively query in-memory table
// MAGIC     ```

// COMMAND ----------

// MAGIC %md
// MAGIC #### Using Foreach
// MAGIC 
// MAGIC The `foreach` operation allows arbitrary operations to be computed on
// MAGIC the output data. As of Spark 2.1, this is available only for Scala and
// MAGIC Java. To use this, you will have to implement the interface
// MAGIC `ForeachWriter`
// MAGIC ([Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.ForeachWriter)/[Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/ForeachWriter.html)
// MAGIC docs), which has methods that get called whenever there is a sequence of
// MAGIC rows generated as output after a trigger. Note the following important
// MAGIC points.
// MAGIC 
// MAGIC -   The writer must be serializable, as it will be serialized and sent
// MAGIC     to the executors for execution.
// MAGIC 
// MAGIC -   All the three methods, `open`, `process` and `close` will be called
// MAGIC     on the executors.
// MAGIC 
// MAGIC -   The writer must do all the initialization (e.g. opening connections,
// MAGIC     starting a transaction, etc.) only when the `open` method is called.
// MAGIC     Be aware that, if there is any initialization in the class as soon
// MAGIC     as the object is created, then that initialization will happen in
// MAGIC     the driver (because that is where the instance is being created),
// MAGIC     which may not be what you intend.
// MAGIC 
// MAGIC -   `version` and `partition` are two parameters in `open` that uniquely
// MAGIC     represent a set of rows that needs to be pushed out. `version` is a
// MAGIC     monotonically increasing id that increases with every trigger.
// MAGIC     `partition` is an id that represents a partition of the output,
// MAGIC     since the output is distributed and will be processed on
// MAGIC     multiple executors.
// MAGIC 
// MAGIC -   `open` can use the `version` and `partition` to choose whether it
// MAGIC     needs to write the sequence of rows. Accordingly, it can return
// MAGIC     `true` (proceed with writing), or `false` (no need to write). If
// MAGIC     `false` is returned, then `process` will not be called on any row.
// MAGIC     For example, after a partial failure, some of the output partitions
// MAGIC     of the failed trigger may have already been committed to a database.
// MAGIC     Based on metadata stored in the database, the writer can identify
// MAGIC     partitions that have already been committed and accordingly return
// MAGIC     false to skip committing them again.
// MAGIC 
// MAGIC -   Whenever `open` is called, `close` will also be called (unless the
// MAGIC     JVM exits due to some error). This is true even if `open`
// MAGIC     returns false. If there is any error in processing and writing the
// MAGIC     data, `close` will be called with the error. It is your
// MAGIC     responsibility to clean up state (e.g. connections,
// MAGIC     transactions, etc.) that have been created in `open` such that there
// MAGIC     are no resource leaks.
// MAGIC 
// MAGIC Managing Streaming Queries
// MAGIC --------------------------
// MAGIC 
// MAGIC The `StreamingQuery` object created when a query is started can be used
// MAGIC to monitor and manage the query.
// MAGIC 
// MAGIC ```
// MAGIC     val query = df.writeStream.format("console").start()   // get the query object
// MAGIC 
// MAGIC     query.id          // get the unique identifier of the running query that persists across restarts from checkpoint data
// MAGIC 
// MAGIC     query.runId       // get the unique id of this run of the query, which will be generated at every start/restart
// MAGIC 
// MAGIC     query.name        // get the name of the auto-generated or user-specified name
// MAGIC 
// MAGIC     query.explain()   // print detailed explanations of the query
// MAGIC 
// MAGIC     query.stop()      // stop the query
// MAGIC 
// MAGIC     query.awaitTermination()   // block until query is terminated, with stop() or with error
// MAGIC 
// MAGIC     query.exception       // the exception if the query has been terminated with error
// MAGIC 
// MAGIC     query.recentProgress  // an array of the most recent progress updates for this query
// MAGIC 
// MAGIC     query.lastProgress    // the most recent progress update of this streaming query
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC You can start any number of queries in a single SparkSession. They will
// MAGIC all be running concurrently sharing the cluster resources. You can use
// MAGIC `sparkSession.streams()` to get the `StreamingQueryManager`
// MAGIC ([Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.streaming.StreamingQueryManager)/[Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/streaming/StreamingQueryManager.html)/[Python](https://spark.apache.org/docs/2.2.0/api/python/pyspark.sql.html#pyspark.sql.streaming.StreamingQueryManager)
// MAGIC docs) that can be used to manage the currently active queries.
// MAGIC 
// MAGIC ```
// MAGIC     val spark: SparkSession = ...
// MAGIC 
// MAGIC     spark.streams.active    // get the list of currently active streaming queries
// MAGIC 
// MAGIC     spark.streams.get(id)   // get a query object by its unique id
// MAGIC 
// MAGIC     spark.streams.awaitAnyTermination()   // block until any one of them terminates
// MAGIC ```
// MAGIC 
// MAGIC Monitoring Streaming Queries
// MAGIC ----------------------------
// MAGIC 
// MAGIC There are two APIs for monitoring and debugging active queries -
// MAGIC interactively and asynchronously.
// MAGIC 
// MAGIC ### Interactive APIs
// MAGIC 
// MAGIC You can directly get the current status and metrics of an active query
// MAGIC using `streamingQuery.lastProgress()` and `streamingQuery.status()`.
// MAGIC `lastProgress()` returns a `StreamingQueryProgress` object in
// MAGIC [Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.streaming.StreamingQueryProgress)
// MAGIC and
// MAGIC [Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/streaming/StreamingQueryProgress.html)
// MAGIC and a dictionary with the same fields in Python. It has all the
// MAGIC information about the progress made in the last trigger of the stream -
// MAGIC what data was processed, what were the processing rates, latencies, etc.
// MAGIC There is also `streamingQuery.recentProgress` which returns an array of
// MAGIC last few progresses.
// MAGIC 
// MAGIC In addition, `streamingQuery.status()` returns a `StreamingQueryStatus`
// MAGIC object in
// MAGIC [Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.streaming.StreamingQueryStatus)
// MAGIC and
// MAGIC [Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/streaming/StreamingQueryStatus.html)
// MAGIC and a dictionary with the same fields in Python. It gives information
// MAGIC about what the query is immediately doing - is a trigger active, is data
// MAGIC being processed, etc.
// MAGIC 
// MAGIC Here are a few examples.
// MAGIC 
// MAGIC ```
// MAGIC     val query: StreamingQuery = ...
// MAGIC 
// MAGIC     println(query.lastProgress)
// MAGIC 
// MAGIC     /* Will print something like the following.
// MAGIC 
// MAGIC     {
// MAGIC       "id" : "ce011fdc-8762-4dcb-84eb-a77333e28109",
// MAGIC       "runId" : "88e2ff94-ede0-45a8-b687-6316fbef529a",
// MAGIC       "name" : "MyQuery",
// MAGIC       "timestamp" : "2016-12-14T18:45:24.873Z",
// MAGIC       "numInputRows" : 10,
// MAGIC       "inputRowsPerSecond" : 120.0,
// MAGIC       "processedRowsPerSecond" : 200.0,
// MAGIC       "durationMs" : {
// MAGIC         "triggerExecution" : 3,
// MAGIC         "getOffset" : 2
// MAGIC       },
// MAGIC       "eventTime" : {
// MAGIC         "watermark" : "2016-12-14T18:45:24.873Z"
// MAGIC       },
// MAGIC       "stateOperators" : [ ],
// MAGIC       "sources" : [ {
// MAGIC         "description" : "KafkaSource[Subscribe[topic-0]]",
// MAGIC         "startOffset" : {
// MAGIC           "topic-0" : {
// MAGIC             "2" : 0,
// MAGIC             "4" : 1,
// MAGIC             "1" : 1,
// MAGIC             "3" : 1,
// MAGIC             "0" : 1
// MAGIC           }
// MAGIC         },
// MAGIC         "endOffset" : {
// MAGIC           "topic-0" : {
// MAGIC             "2" : 0,
// MAGIC             "4" : 115,
// MAGIC             "1" : 134,
// MAGIC             "3" : 21,
// MAGIC             "0" : 534
// MAGIC           }
// MAGIC         },
// MAGIC         "numInputRows" : 10,
// MAGIC         "inputRowsPerSecond" : 120.0,
// MAGIC         "processedRowsPerSecond" : 200.0
// MAGIC       } ],
// MAGIC       "sink" : {
// MAGIC         "description" : "MemorySink"
// MAGIC       }
// MAGIC     }
// MAGIC     */
// MAGIC 
// MAGIC 
// MAGIC     println(query.status)
// MAGIC 
// MAGIC     /*  Will print something like the following.
// MAGIC     {
// MAGIC       "message" : "Waiting for data to arrive",
// MAGIC       "isDataAvailable" : false,
// MAGIC       "isTriggerActive" : false
// MAGIC     }
// MAGIC     */
// MAGIC ```
// MAGIC 
// MAGIC ### Asynchronous API
// MAGIC 
// MAGIC You can also asynchronously monitor all queries associated with a
// MAGIC `SparkSession` by attaching a `StreamingQueryListener`
// MAGIC ([Scala](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.streaming.StreamingQueryListener)/[Java](https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/sql/streaming/StreamingQueryListener.html)
// MAGIC docs). Once you attach your custom `StreamingQueryListener` object with
// MAGIC `sparkSession.streams.attachListener()`, you will get callbacks when a
// MAGIC query is started and stopped and when there is progress made in an
// MAGIC active query. Here is an example,
// MAGIC 
// MAGIC ```
// MAGIC     val spark: SparkSession = ...
// MAGIC 
// MAGIC     spark.streams.addListener(new StreamingQueryListener() {
// MAGIC         override def onQueryStarted(queryStarted: QueryStartedEvent): Unit = {
// MAGIC             println("Query started: " + queryStarted.id)
// MAGIC         }
// MAGIC         override def onQueryTerminated(queryTerminated: QueryTerminatedEvent): Unit = {
// MAGIC             println("Query terminated: " + queryTerminated.id)
// MAGIC         }
// MAGIC         override def onQueryProgress(queryProgress: QueryProgressEvent): Unit = {
// MAGIC             println("Query made progress: " + queryProgress.progress)
// MAGIC         }
// MAGIC     })
// MAGIC ```
// MAGIC 
// MAGIC Recovering from Failures with Checkpointing
// MAGIC -------------------------------------------
// MAGIC 
// MAGIC In case of a failure or intentional shutdown, you can recover the
// MAGIC previous progress and state of a previous query, and continue where it
// MAGIC left off. This is done using checkpointing and write ahead logs. You can
// MAGIC configure a query with a checkpoint location, and the query will save
// MAGIC all the progress information (i.e. range of offsets processed in each
// MAGIC trigger) and the running aggregates (e.g. word counts in the *quick
// MAGIC example* to
// MAGIC the checkpoint location. This checkpoint location has to be a path in an
// MAGIC HDFS compatible file system, and can be set as an option in the
// MAGIC DataStreamWriter when *starting a
// MAGIC query*.
// MAGIC 
// MAGIC ```
// MAGIC     aggDF
// MAGIC       .writeStream
// MAGIC       .outputMode("complete")
// MAGIC       .option("checkpointLocation", "path/to/HDFS/dir")
// MAGIC       .format("memory")
// MAGIC       .start()
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Where to go from here
// MAGIC =====================
// MAGIC 
// MAGIC -   Examples: See and run the
// MAGIC     [Scala](https://github.com/apache/spark/tree/master/examples/src/main/scala/org/apache/spark/examples/sql/streaming)/[Java](https://github.com/apache/spark/tree/master/examples/src/main/java/org/apache/spark/examples/sql/streaming)/[Python](https://github.com/apache/spark/tree/master/examples/src/main/python/sql/streaming)/[R](https://github.com/apache/spark/tree/master/examples/src/main/r/streaming) examples.
// MAGIC -   Spark Summit 2016 Talk - [A Deep Dive into Structured
// MAGIC     Streaming](https://spark-summit.org/2016/events/a-deep-dive-into-structured-streaming/)
// MAGIC -   [https://databricks.com/blog/2017/08/24/anthology-of-technical-assets-on-apache-sparks-structured-streaming.html](https://databricks.com/blog/2017/08/24/anthology-of-technical-assets-on-apache-sparks-structured-streaming.html)
// MAGIC -  An authoritative resource: [https://www.gitbook.com/book/jaceklaskowski/spark-structured-streaming/details](https://www.gitbook.com/book/jaceklaskowski/spark-structured-streaming/details)
// MAGIC 
// MAGIC ## 2017 Updated Pointers
// MAGIC 
// MAGIC * [https://medium.com/@jaykreps/exactly-once-support-in-apache-kafka-55e1fdd0a35f](https://medium.com/@jaykreps/exactly-once-support-in-apache-kafka-55e1fdd0a35f)
// MAGIC * [https://databricks.com/blog/2017/10/17/arbitrary-stateful-processing-in-apache-sparks-structured-streaming.html](https://databricks.com/blog/2017/10/17/arbitrary-stateful-processing-in-apache-sparks-structured-streaming.html).
// MAGIC * Spark Summit EU Dublin 2017 Deep Dive: [https://databricks.com/session/deep-dive-stateful-stream-processing](https://databricks.com/session/deep-dive-stateful-stream-processing)
// MAGIC * See [https://youtu.be/JAb4FIheP28](https://youtu.be/JAb4FIheP28)
// MAGIC * Official [docs and examples of arbitrary stateful operations](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#arbitrary-stateful-operations)
// MAGIC   * doc: [https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.streaming.GroupState](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.streaming.GroupState)
// MAGIC   * example: [https://github.com/apache/spark/blob/v2.2.0/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredSessionization.scala](https://github.com/apache/spark/blob/v2.2.0/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredSessionization.scala)
// MAGIC * See [Part 14](http://blog.madhukaraphatak.com/introduction-to-spark-structured-streaming-part-14/) of [the 14-part series on structured streaming series in Madhukar's blog](http://blog.madhukaraphatak.com/categories/introduction-structured-streaming/) 