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

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/3x5iQoXm3cc/0.jpg)](https://www.youtube.com/embed/3x5iQoXm3cc?start=2050&end=2600&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Stage 2: Exploring songs data
// MAGIC 
// MAGIC ![Explore](http://training.databricks.com/databricks_guide/end-to-end-02.png)
// MAGIC 
// MAGIC 
// MAGIC This is the second notebook in this tutorial. In this notebook we do what any data scientist does with their data right after parsing it: exploring and understanding different aspect of data. Make sure you understand how we get the `songsTable` by reading and running the ETL notebook. In the ETL notebook we created and cached a temporary table named `songsTable`

// COMMAND ----------

// MAGIC %md
// MAGIC ## Let's Do all the main bits in Stage 1 now before doing Stage 2 in this Notebook.

// COMMAND ----------

// Let's quickly do everything to register the tempView of the table here

// fill in comment ... EXERCISE!
case class Song(artist_id: String, artist_latitude: Double, artist_longitude: Double, artist_location: String, artist_name: String, duration: Double, end_of_fade_in: Double, key: Int, key_confidence: Double, loudness: Double, release: String, song_hotness: Double, song_id: String, start_of_fade_out: Double, tempo: Double, time_signature: Double, time_signature_confidence: Double, title: String, year: Double, partial_sequence: Int)

def parseLine(line: String): Song = {
  // fill in comment ...
  
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
  // fill in comment ...
  val tokens = line.split("\t")
  Song(tokens(0), toDouble(tokens(1), 0.0), toDouble(tokens(2), 0.0), tokens(3), tokens(4), toDouble(tokens(5), 0.0), toDouble(tokens(6), 0.0), toInt(tokens(7), -1), toDouble(tokens(8), 0.0), toDouble(tokens(9), 0.0), tokens(10), toDouble(tokens(11), 0.0), tokens(12), toDouble(tokens(13), 0.0), toDouble(tokens(14), 0.0), toDouble(tokens(15), 0.0), toDouble(tokens(16), 0.0), tokens(17), toDouble(tokens(18), 0.0), toInt(tokens(19), -1))
}

// this is loads all the data - a subset of the 1M songs dataset
val dataRDD = sc.textFile("/databricks-datasets/songs/data-001/part-*") 

// .. fill in comment
val df = dataRDD.map(parseLine).toDF

// .. fill in comment
df.createOrReplaceTempView("songsTable")

// COMMAND ----------

spark.catalog.listTables.show(false) // make sure the temp view of our table is there

// COMMAND ----------

// MAGIC %md
// MAGIC ## A first inspection
// MAGIC A first step to any data exploration is viewing sample data. For this purpose we can use a simple SQL query that returns first 10 rows.

// COMMAND ----------

// MAGIC %sql select * from songsTable limit 10

// COMMAND ----------

table("songsTable").printSchema()

// COMMAND ----------

// MAGIC %sql select count(*) from songsTable

// COMMAND ----------

table("songsTable").count() // or equivalently with DataFrame API - recall table("songsTable") is a DataFrame

// COMMAND ----------

display(sqlContext.sql("SELECT duration, year FROM songsTable")) // Aggregation is set to 'Average' in 'Plot Options'

// COMMAND ----------

// MAGIC %md
// MAGIC ## Exercises
// MAGIC 
// MAGIC 1. Why do you think average song durations increase dramatically in 70's?
// MAGIC 2. Add error bars with standard deviation around each average point in the plot.
// MAGIC 3. How did average loudness change over time?
// MAGIC 4. How did tempo change over time?
// MAGIC 5. What other aspects of songs can you explore with this technique?

// COMMAND ----------

// MAGIC %md
// MAGIC ## Sampling and visualizing
// MAGIC 
// MAGIC Another technique for visually exploring large data, which we are going to try, is sampling data.
// MAGIC 
// MAGIC * First step is generating a sample.
// MAGIC * With sampled data we can produce a scatter plot as follows.

// COMMAND ----------

// MAGIC %python
// MAGIC # let's use ggplot from python
// MAGIC # note that this is second natural way to 'babble' between languages - using the right tool for the job!
// MAGIC #   recall: the first naive but solid way was to use parquet files to write and read from different languages 
// MAGIC #           with parquet files you can tackle the babbling problem when the table is too large to be 'Viewed'
// MAGIC from ggplot import *
// MAGIC sampled = sqlContext.sql("select year, duration from songsTable where year > 1930 and year < 2012")\
// MAGIC   .sample(withReplacement = False, fraction = 0.1).toPandas()
// MAGIC   
// MAGIC p = ggplot(sampled, aes(x = 'year', y = 'duration')) + ylim(0, 800) + \
// MAGIC   geom_smooth(size=3, span=0.3) + geom_point(aes(color = 'blue', size = 4))
// MAGIC display(p)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Exercises
// MAGIC 
// MAGIC 
// MAGIC 1. Add jitter to year value in the plot above.
// MAGIC 2. Plot sampled points for other parameters in the data.

// COMMAND ----------

// MAGIC %md
// MAGIC Next step is clustering the data. Click on the next notebook (Model) to follow the tutorial.