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
// MAGIC # Structured Streaming using Scala DataFrames API - Exercise
// MAGIC 
// MAGIC Apache Spark 2.0 adds the first version of a new higher-level stream processing API, Structured Streaming. In this notebook we are going to take a quick look at how to use DataFrame API to build Structured Streaming applications. We want to compute real-time metrics like running counts and windowed counts on a stream of timestamped actions (e.g. Open, Close, etc).
// MAGIC 
// MAGIC To run this notebook, import it to Databricks Community Edition and attach it to a **Spark 2.0 (Scala 2.10)** cluster.
// MAGIC 
// MAGIC This is built on the public databricks notebook [importable from here](https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/4012078893478893/295447656425301/5985939988045659/latest.html).

// COMMAND ----------

// To make sure that this notebook is being run on a Spark 2.0+ cluster, let's see if we can access the SparkSession - the new entry point of Apache Spark 2.0.
// If this fails, then you are not connected to a Spark 2.0 cluster. Please recreate your cluster and select the version to be "Spark 2.0 (Scala 2.10)".
spark

// COMMAND ----------

// MAGIC %md ## Sample Data
// MAGIC We have some sample action data as files in `/databricks-datasets/structured-streaming/events/` which we are going to use to build this appication. Let's take a look at the contents of this directory.

// COMMAND ----------

// MAGIC %fs ls /databricks-datasets/structured-streaming/events/

// COMMAND ----------

// MAGIC %md There are about 50 JSON files in the directory. Let's see what each JSON file contains.

// COMMAND ----------

// MAGIC %fs head /databricks-datasets/structured-streaming/events/file-0.json

// COMMAND ----------

// MAGIC %md 
// MAGIC Each line in the files contain a JSON record with two fields - `time` and `action`. Let's try to analyze these files interactively.

// COMMAND ----------

// MAGIC %md ## Batch/Interactive Processing
// MAGIC The usual first step in attempting to process the data is to interactively query the data. Let's define a static DataFrame on the files, and give it a table name.

// COMMAND ----------

import org.apache.spark.sql.types._

val inputPath = "/databricks-datasets/structured-streaming/events/"

// Since we know the data format already, let's define the schema to speed up processing (no need for Spark to infer schema)
val jsonSchema = new StructType().add("time", TimestampType).add("action", StringType)

val staticInputDF = 
  spark
    .read
    .schema(jsonSchema)
    .json(inputPath)

display(staticInputDF)

// COMMAND ----------

// MAGIC %md Now we can compute the number of "open" and "close" actions with one hour windows. To do this, we will group by the `action` column and 1 hour windows over the `time` column.

// COMMAND ----------

import org.apache.spark.sql.functions._

val staticCountsDF = 
  staticInputDF
    .groupBy($"action", window($"time", "1 hour"))
    .count()   

// Register the DataFrame as table 'static_counts'
staticCountsDF.createOrReplaceTempView("static_counts")

// COMMAND ----------

// MAGIC %md Now we can directly use SQL to query the table. For example, here are the total counts across all the hours.

// COMMAND ----------

// MAGIC %sql select action, sum(count) as total_count from static_counts group by action

// COMMAND ----------

// MAGIC %md How about a timeline of windowed counts?

// COMMAND ----------

// MAGIC %sql select action, date_format(window.end, "MMM-dd HH:mm") as time, count from static_counts order by time, action

// COMMAND ----------

// MAGIC %md Note the two ends of the graph. The close actions are generated such that they are after the corresponding open actions, so there are more "opens" in the beginning and more "closes" in the end.

// COMMAND ----------

// MAGIC %md ## Stream Processing 
// MAGIC Now that we have analyzed the data interactively, let's convert this to a streaming query that continuously updates as data comes. Since we just have a static set of files, we are going to emulate a stream from them by reading one file at a time, in the chronological order they were created. The query we have to write is pretty much the same as the interactive query above.

// COMMAND ----------

import org.apache.spark.sql.functions._

// Similar to definition of staticInputDF above, just using `readStream` instead of `read`
val streamingInputDF = 
  spark
    .readStream                       // `readStream` instead of `read` for creating streaming DataFrame
    .schema(jsonSchema)               // Set the schema of the JSON data
    .option("maxFilesPerTrigger", 1)  // Treat a sequence of files as a stream by picking one file at a time
    .json(inputPath)

// Same query as staticInputDF
val streamingCountsDF = 
  streamingInputDF
    .groupBy($"action", window($"time", "1 hour"))
    .count()

// Is this DF actually a streaming DF?
streamingCountsDF.isStreaming

// COMMAND ----------

// MAGIC %md As you can see, `streamingCountsDF` is a streaming Dataframe (`streamingCountsDF.isStreaming` was `true`). You can start streaming computation, by defining the sink and starting it. 
// MAGIC In our case, we want to interactively query the counts (same queries as above), so we will set the complete set of 1 hour counts to be a in a in-memory table (note that this for testing purpose only in Spark 2.0).

// COMMAND ----------

spark.conf.set("spark.sql.shuffle.partitions", "1")  // keep the size of shuffles small

val query =
  streamingCountsDF
    .writeStream
    .format("memory")        // memory = store in-memory table (for testing only in Spark 2.0)
    .queryName("counts")     // counts = name of the in-memory table
    .outputMode("complete")  // complete = all the counts should be in the table
    .start()

// COMMAND ----------

// MAGIC %md 
// MAGIC `query` is a handle to the streaming query that is running in the background. This query is continuously picking up files and updating the windowed counts. 
// MAGIC 
// MAGIC Note the status of query in the above cell. Both the `Status: ACTIVE` and the progress bar shows that the query is active. 
// MAGIC Furthermore, if you expand the `>Details` above, you will find the number of files they have already processed. 
// MAGIC 
// MAGIC Let's wait a bit for a few files to be processed and then query the in-memory `counts` table.

// COMMAND ----------

Thread.sleep(5000) // wait a bit for computation to start

// COMMAND ----------

// MAGIC %sql select action, date_format(window.end, "MMM-dd HH:mm") as time, count from counts order by time, action

// COMMAND ----------

// MAGIC %md We see the timeline of windowed counts (similar to the static one ealrier) building up. If we keep running this interactive query repeatedly, we will see the latest updated counts which the streaming query is updating in the background.

// COMMAND ----------

Thread.sleep(5000)  // wait a bit more for more data to be computed

// COMMAND ----------

// MAGIC %sql select action, date_format(window.end, "MMM-dd HH:mm") as time, count from counts order by time, action

// COMMAND ----------

Thread.sleep(5000)  // wait a bit more for more data to be computed

// COMMAND ----------

// MAGIC %sql select action, date_format(window.end, "MMM-dd HH:mm") as time, count from counts order by time, action

// COMMAND ----------

// MAGIC %md Also, let's see the total number of "opens" and "closes".

// COMMAND ----------

// MAGIC %sql select action, sum(count) as total_count from counts group by action order by action

// COMMAND ----------

// MAGIC %md 
// MAGIC If you keep running the above query repeatedly, you will always find that the number of "opens" is more than the number of "closes", as expected in a data stream where a "close" always appear after corresponding "open". This shows that Structured Streaming ensures **prefix integrity**. Read the blog posts linked below if you want to know more.
// MAGIC 
// MAGIC Note that there are only a few files, so consuming all of them there will be no updates to the counts. Rerun the query if you want to interact with the streaming query again.
// MAGIC 
// MAGIC Finally, you can stop the query running in the background, either by clicking on the 'Cancel' link in the cell of the query, or by executing `query.stop()`. Either way, when the query is stopped, the status of the corresponding cell above will automatically update to `TERMINATED`.

// COMMAND ----------

// MAGIC %md 
// MAGIC ##What's next?
// MAGIC If you want to learn more about Structured Streaming, here are a few pointers.
// MAGIC 
// MAGIC - Databricks blog posts on Structured Streaming and Continuous Applications
// MAGIC   - Blog post 1: [Continuous Applications: Evolving Streaming in Apache Spark 2.0](https://databricks.com/blog/2016/07/28/continuous-applications-evolving-streaming-in-apache-spark-2-0.html)
// MAGIC   - Blog post 2: [Structured Streaming in Apache Spark]( https://databricks.com/blog/2016/07/28/structured-streaming-in-apache-spark.html)
// MAGIC 
// MAGIC - [Structured Streaming Programming Guide](http://spark.apache.org/docs/latest/structured-streaming-programming-guide.html)
// MAGIC 
// MAGIC - Spark Summit 2016 Talks
// MAGIC   - [Structuring Spark: Dataframes, Datasets And Streaming](https://spark-summit.org/2016/events/structuring-spark-dataframes-datasets-and-streaming/)
// MAGIC   - [A Deep Dive Into Structured Streaming](https://spark-summit.org/2016/events/a-deep-dive-into-structured-streaming/)