// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
// MAGIC 
// MAGIC ## Million Song Dataset - Kaggle Challenge
// MAGIC 
// MAGIC ### Predict which songs a user will listen to.
// MAGIC 
// MAGIC **SOURCE:** This is just a *Scala*-rification of the *Python* notebook published in databricks community edition in 2016.
// MAGIC 
// MAGIC *CAUTION:* This notebook is expected to have an error in command 28 (`Cmd 28` in databricks notebook). You are meant to learn how to fix this error with simple exception-handling to become a better data scientist. So ignore this warning, if any.

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/3x5iQoXm3cc/0.jpg)](https://www.youtube.com/embed/3x5iQoXm3cc?start=1395&end=2050&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Stage 1: Parsing songs data
// MAGIC 
// MAGIC ![ETL](http://training.databricks.com/databricks_guide/end-to-end-01.png)
// MAGIC 
// MAGIC This is the first notebook in this tutorial. In this notebook we will read data from DBFS (DataBricks FileSystem). We will parse data and load it as a table that can be readily used in following notebooks.
// MAGIC 
// MAGIC By going through this notebook you can expect to learn how to read distributed data as an RDD, how to transform RDDs, and how to construct a Spark DataFrame from an RDD and register it as a table.
// MAGIC 
// MAGIC We first explore different files in our distributed file system. We use a header file to construct a Spark `Schema` object. We write a function that takes the header and casts strings in each line of our data to corresponding types. Once we run this function on the data we find that it fails on some corner caes. We update our function and finally get a parsed RDD. We combine that RDD and the Schema to construct a DataFame and register it as a temporary table in SparkSQL.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Text data files are stored in `dbfs:/databricks-datasets/songs/data-001` 
// MAGIC You can conveniently list files on distributed file system (DBFS, S3 or HDFS) using `%fs` commands.

// COMMAND ----------

// MAGIC %fs ls /databricks-datasets/songs/data-001/

// COMMAND ----------

// MAGIC %md
// MAGIC As you can see in the listing we have data files and a single header file. The header file seems interesting and worth a first inspection at first. The file is 377 bytes, therefore it is safe to collect the entire content of the file in the notebook. 

// COMMAND ----------

sc.textFile("databricks-datasets/songs/data-001/header.txt").collect() 

// COMMAND ----------

// MAGIC %md
// MAGIC Remember you can `collect()` a huge RDD and crash the driver program - so it is a good practise to take a couple lines and count the number of lines, especially if you have no idea what file you are trying to read.

// COMMAND ----------

sc.textFile("databricks-datasets/songs/data-001/header.txt").take(2)

// COMMAND ----------

sc.textFile("databricks-datasets/songs/data-001/header.txt").count()

// COMMAND ----------

//sc.textFile("databricks-datasets/songs/data-001/header.txt").collect.map(println) // uncomment to see line-by-line

// COMMAND ----------

// MAGIC %md
// MAGIC As seen above each line in the header consists of a name and a type separated by colon. We will need to parse the header file as follows:

// COMMAND ----------

val header = sc.textFile("/databricks-datasets/songs/data-001/header.txt").map(line => {
                val headerElement = line.split(":")
                (headerElement(0), headerElement(1))
            }
           ).collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Let's define a `case class` called `Song` that will be used to represent each row of data in the files:
// MAGIC 
// MAGIC * `/databricks-datasets/songs/data-001/part-00000` through `/databricks-datasets/songs/data-001/part-00119` or the last `.../part-*****` file.

// COMMAND ----------

case class Song(artist_id: String, artist_latitude: Double, artist_longitude: Double, artist_location: String, artist_name: String, duration: Double, end_of_fade_in: Double, key: Int, key_confidence: Double, loudness: Double, release: String, song_hotness: Double, song_id: String, start_of_fade_out: Double, tempo: Double, time_signature: Double, time_signature_confidence: Double, title: String, year: Double, partial_sequence: Int)

// COMMAND ----------

// MAGIC %md
// MAGIC Now we turn to data files. First, step is inspecting the first line of data to inspect its format.

// COMMAND ----------

// this is loads all the data - a subset of the 1M songs dataset
val dataRDD = sc.textFile("/databricks-datasets/songs/data-001/part-*") 

// COMMAND ----------

dataRDD.count // number of songs

// COMMAND ----------

dataRDD.take(3)

// COMMAND ----------

// MAGIC %md
// MAGIC Each line of data consists of multiple fields separated by `\t`. With that information and what we learned from the header file, we set out to parse our data.
// MAGIC 
// MAGIC * We have already created a case class based on the header (which seems to agree with the 3 lines above).
// MAGIC * Next, we will create a function that takes each line as input and returns the case class as output.

// COMMAND ----------

// let's do this 'by hand' to re-flex our RDD-muscles :)
// although this is not a robust way to read from a data engineering perspective (without fielding exceptions)
def parseLine(line: String): Song = {
  
  val tokens = line.split("\t")
  Song(tokens(0), tokens(1).toDouble, tokens(2).toDouble, tokens(3), tokens(4), tokens(5).toDouble, tokens(6).toDouble, tokens(7).toInt, tokens(8).toDouble, tokens(9).toDouble, tokens(10), tokens(11).toDouble, tokens(12), tokens(13).toDouble, tokens(14).toDouble, tokens(15).toDouble, tokens(16).toDouble, tokens(17), tokens(18).toDouble, tokens(19).toInt)
}

// COMMAND ----------

// MAGIC %md
// MAGIC With this function we can transform the dataRDD to another RDD that consists of Song case classes

// COMMAND ----------

val parsedRDD = dataRDD.map(parseLine)

// COMMAND ----------

// MAGIC %md
// MAGIC To convert an RDD of case classes to a DataFrame, we just need to call the toDF method

// COMMAND ----------

val df = parsedRDD.toDF

// COMMAND ----------

// MAGIC %md
// MAGIC Once we get a DataFrame we can register it as a temporary table. That will allow us to use its name in SQL queries.

// COMMAND ----------

df.createOrReplaceTempView("songsTable")

// COMMAND ----------

// MAGIC %md
// MAGIC We can now cache our table. So far all operations have been lazy. This is the first time Spark will attempt to actually read all our data and apply the transformations. 
// MAGIC 
// MAGIC **If you are running Spark 1.6+ the next command will throw a parsing error.**

// COMMAND ----------

// MAGIC %sql cache table songsTable

// COMMAND ----------

// MAGIC %md
// MAGIC The error means that we are trying to convert a missing value to a Double. Here is an updated version of the parseLine function to deal with missing values

// COMMAND ----------

// good data engineering science practise
def parseLine(line: String): Song = {
  
  
  def toDouble(value: String, defaultVal: Double): Double = {
    try {
       value.toDouble
    } catch {
      case e: Exception => defaultVal
    }
  }

  def toInt(value: String, defaultVal: Int): Int = {
    try {
       value.toInt
      } catch {
      case e: Exception => defaultVal
    }
  }
  
  val tokens = line.split("\t")
  Song(tokens(0), toDouble(tokens(1), 0.0), toDouble(tokens(2), 0.0), tokens(3), tokens(4), toDouble(tokens(5), 0.0), toDouble(tokens(6), 0.0), toInt(tokens(7), -1), toDouble(tokens(8), 0.0), toDouble(tokens(9), 0.0), tokens(10), toDouble(tokens(11), 0.0), tokens(12), toDouble(tokens(13), 0.0), toDouble(tokens(14), 0.0), toDouble(tokens(15), 0.0), toDouble(tokens(16), 0.0), tokens(17), toDouble(tokens(18), 0.0), toInt(tokens(19), -1))
}

// COMMAND ----------

val df = dataRDD.map(parseLine).toDF
df.createOrReplaceTempView("songsTable")

// COMMAND ----------

// MAGIC %md
// MAGIC And let's try caching the table. We are going to access this data multiple times in following notebooks, therefore it is a good idea to cache it in memory for faster subsequent access.

// COMMAND ----------

// MAGIC %sql cache table songsTable

// COMMAND ----------

// MAGIC %md
// MAGIC From now on we can easily query our data using the temporary table we just created and cached in memory. Since it is registered as a table we can conveniently use SQL as well as Spark API to access it.

// COMMAND ----------

// MAGIC %sql select * from songsTable limit 10

// COMMAND ----------

// MAGIC %md
// MAGIC Next up is exploring this data. Click on the Exploration notebook to continue the tutorial.