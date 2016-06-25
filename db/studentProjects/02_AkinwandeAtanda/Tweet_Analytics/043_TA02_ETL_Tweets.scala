// Databricks notebook source exported at Sat, 25 Jun 2016 04:59:42 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC #Tweet Analytics
// MAGIC 
// MAGIC [Presentation contents](https://github.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016).

// COMMAND ----------

// MAGIC %md
// MAGIC ## Extract-Transform-Load (ETL) Processing of Streamed Tweets
// MAGIC This notebook should be runned after the tweets have been collected in batches. The operations performed in this notebooks are:
// MAGIC   * Read/Load the Streamed Tweets in batches of RDD
// MAGIC   * Read/Load the Streamed Tweets in merged batches of RDDs
// MAGIC   * Save the Tweets in Parquet format, convert to Dataframe Table and run SQL queries
// MAGIC   * Explore the Streamed Tweets using SQL queries: Filter, Plot and Re-Shape

// COMMAND ----------

display(dbutils.fs.ls(s"/mnt/s3Data/filteredTweet"))

// COMMAND ----------

val tweet = sc.textFile(s"dbfs:/mnt/s3Data/filteredTweet/tweets_1463600700000").take(1)

// COMMAND ----------

val tweetDF = sqlContext.read.json(s"dbfs:/mnt/s3Data/filteredTweet/tweets_1463600700000")


// COMMAND ----------

tweetDF.show()

// COMMAND ----------

tweetDF.printSchema()

// COMMAND ----------

tweetDF.select("text").show

// COMMAND ----------

tweetDF.select("createdAt", "text", "favoriteCount","retweetCount").show

// COMMAND ----------

tweetDF.select(tweetDF("createdAt"), tweetDF("text"), tweetDF("favoriteCount")+1,tweetDF("retweetCount")+2).show

// COMMAND ----------

tweetDF.select(tweetDF("createdAt"), tweetDF("text"), tweetDF("favoriteCount")>1,tweetDF("retweetCount")).show

// COMMAND ----------

tweetDF.groupBy("favoriteCount").count().show

// COMMAND ----------

tweetDF.groupBy("retweetCount").count().show

// COMMAND ----------

tweetDF.groupBy("createdAt").count().show

// COMMAND ----------

tweetDF.groupBy("user").count().show

// COMMAND ----------

tweetDF.filter(tweetDF("favoriteCount")<1).show

// COMMAND ----------

tweetDF.filter(tweetDF("tex")==="trump").show

// COMMAND ----------

import sqlContext.implicits._

// COMMAND ----------

val tweedDS = tweetDF.select("text").show

// COMMAND ----------

// MAGIC %md
// MAGIC ### Merged RDD Streams

// COMMAND ----------

val tweetDF2 = sqlContext.read.json(s"dbfs:/mnt/s3Data/filteredTweet/*")


// COMMAND ----------

tweetDF2.show

// COMMAND ----------

tweetDF2.select("createdAt","text").show

// COMMAND ----------

tweetDF2.groupBy("createdAt").count.show

// COMMAND ----------

tweetDF2.groupBy("favoriteCount").count.show

// COMMAND ----------

tweetDF2.groupBy("retweetCount").count.show

// COMMAND ----------

tweetDF2.groupBy("text").count.show

// COMMAND ----------

// MAGIC %md
// MAGIC ### Saving the Tweet as a Table and Run SQL Queries

// COMMAND ----------

// MAGIC %md
// MAGIC * Unmerged Tweet (tweetDF)

// COMMAND ----------

tweetDF.registerTempTable("tweetTable") // Register the DataFrames as a table.

// COMMAND ----------

val tweetText = sqlContext.sql("SELECT text FROM tweetTable") //Run SQL query

// COMMAND ----------

tweetText.take(1).foreach(println)

// COMMAND ----------

tweetText.map(t=>(t,1)).take(1).foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Merged RDD Streams

// COMMAND ----------

val tweetDF2 = sqlContext.read.json(s"dbfs:/mnt/s3Data/filteredTweet/*")

// COMMAND ----------

// MAGIC %md
// MAGIC ### Saving the Tweet as a Table and Run SQL Queries

// COMMAND ----------

// MAGIC %md
// MAGIC ***Parquet Data format: Save, Preview from the Tables menu, and Query directly without transforming to DataFrame***

// COMMAND ----------

tweetDF2.select("createdAt", "text", "favoriteCount","retweetCount").write.save("filterTweet.parquet")  
//Save the filter Tweet as parquest data format and go to create table to load it from the directory.

// COMMAND ----------

val mergedTweets = sqlContext.read.format("parquet").load("dbfs:/filterTweet.parquet") 
//This reads all the tweets in the parquet data format

// COMMAND ----------

mergedTweets.registerTempTable("mergedTweetsTable") // Save as a Table

// COMMAND ----------

val mergedTweetQuery = sqlContext.sql("SELECT * FROM mergedTweetsTable")

// COMMAND ----------

// MAGIC %md ***Use SQL syntax to extract required fileds from the registered table***

// COMMAND ----------

// MAGIC %sql SELECT * FROM mergedTweetsTable LIMIT 1 

// COMMAND ----------

mergedTweetQuery.cache

// COMMAND ----------

mergedTweetQuery.map(c=>c(1)).foreach(println)

// COMMAND ----------

mergedTweetQuery.take(1)

// COMMAND ----------

val mergedTextQuery = sqlContext.sql("SELECT text FROM mergedTweetsTable").cache

// COMMAND ----------

mergedTextQuery.map(c=>c).take(1).foreach(println)

// COMMAND ----------

dbutils.fs.help

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/"))

// COMMAND ----------

dbutils.fs.rm("dbfs:/filterTweet.parquet",recurse=true)

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/"))

// COMMAND ----------

//display(dbutils.fs.ls("dbfs:/mnt/s3Data/filteredTweet/Trumps.txt"))

// COMMAND ----------

//dbutils.fs.rm("dbfs:/mnt/s3Data/filteredTweet/Trumps.txt",recurse=true)

// COMMAND ----------

// MAGIC %md
// MAGIC ### ETL Operationalization: Actual Tweeets Project

// COMMAND ----------

//dbutils.fs.rm(s"dbfs:/mnt/s3Data/TrumpTweetText.parquet",recurse=true)

// COMMAND ----------

display(dbutils.fs.ls(s"dbfs:/mnt/s3Data"))

// COMMAND ----------

dbutils.fs.rm(s"dbfs:/mnt/s3Data/tweetAll.parquet",recurse=true)

// COMMAND ----------

val tweetAll = sqlContext.read.json(s"dbfs:/mnt/s3Data/twitterNew/*")
tweetAll.cache

// COMMAND ----------

//Save the filter Tweet as parquest data format and go to create table to load it from the directory.
tweetAll.select("createdAt", "text", "favoriteCount","retweetCount").write.save(s"dbfs:/mnt/s3Data/tweetAll.parquet")  


// COMMAND ----------

val mergedTweetAll = sqlContext.read.format("parquet").load(s"dbfs:/mnt/s3Data/tweetAll.parquet") //This reads all the tweets in the parquet data format

// COMMAND ----------

mergedTweetAll.registerTempTable("mergedTweetTable")

// COMMAND ----------

// MAGIC %sql SELECT * FROM mergedTweetTable LIMIT 1

// COMMAND ----------

mergedTweetAll.show

// COMMAND ----------

// MAGIC %md
// MAGIC ## Returns the number of Tweets in the merged dataset (26.3million Tweets as at 3.30p.m today)

// COMMAND ----------

// MAGIC %sql SELECT COUNT(text) as TweetCount FROM mergedTweetTable 

// COMMAND ----------

// MAGIC %md
// MAGIC ## Returns the number of Tweets in the merged dataset group and sort by Date of Created Tweet

// COMMAND ----------

// MAGIC %sql SELECT COUNT(text) as TweetCount, createdAt FROM mergedTweetTable group by createdAt order by createdAt asc

// COMMAND ----------

// MAGIC %sql SELECT COUNT(text) as TweetCount, createdAt as DStreamTime FROM mergedTweetTable group by createdAt order by createdAt asc

// COMMAND ----------

// MAGIC %sql SELECT distinct createdAt FROM mergedTweetTable where favoriteCount == 1

// COMMAND ----------

// MAGIC %md
// MAGIC ## Filter Query by Keywords

// COMMAND ----------

// MAGIC %sql SELECT count(*) as TrumpsTweet FROM mergedTweetTable where text like "%trump%"

// COMMAND ----------

// MAGIC %sql SELECT COUNT(text) as TrumpsTweetCount, createdAt as DStreamTime FROM mergedTweetTable where text like "%trump%" group by createdAt order by createdAt asc

// COMMAND ----------

// MAGIC %sql SELECT createdAt as DStreamTime, text as TrumpsTweet FROM mergedTweetTable where text like "%trump%" order by createdAt asc limit 1

// COMMAND ----------

val TrumpTextQuery = sqlContext.sql("SELECT createdAt as date, text as review, favoriteCount as category FROM mergedTweetTable where text like '%trump%' order by createdAt asc").cache

// COMMAND ----------

val TrumpQuery = sqlContext.sql("SELECT createdAt as date, text as review, CAST(favoriteCount as FLOAT) as category FROM mergedTweetTable where text like '%trump%' order by createdAt asc").cache

// COMMAND ----------

TrumpTextQuery.select("date", "review", "category").write.save(s"dbfs:/mnt/s3Data/TrumpTweet.parquet")

// COMMAND ----------

TrumpQuery.select("date", "review", "category").write.save(s"dbfs:/mnt/s3Data/TrumpSentiment.parquet")

// COMMAND ----------

TrumpTextQuery.registerTempTable("TrumpTweetTable")

// COMMAND ----------

// MAGIC %sql ALTER TABLE TrumpTweetTable ALTER COLUMN category 

// COMMAND ----------

// MAGIC %sql SELECT * FROM TrumpTweetTable limit 3

// COMMAND ----------

TrumpTextQuery.count //Returns the number of tweets 

// COMMAND ----------

TrumpTextQuery.take(3).foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)