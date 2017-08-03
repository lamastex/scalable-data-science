---
title: ""
permalink: /sds/1/6/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/043_TA02_ETL_Tweets/
sidebar:
  nav: "lMenu-SDS-1.6"
---

// Databricks notebook source exported at Sun, 26 Jun 2016 01:43:20 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/043_TA02_ETL_Tweets.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/043_TA02_ETL_Tweets](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=0&end=1611)





#Tweet Analytics

[Presentation contents](https://github.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016).





## Extract-Transform-Load (ETL) Processing of Streamed Tweets
This notebook should be runned after the tweets have been collected in batches. The operations performed in this notebooks are:
  * Read/Load the Streamed Tweets in batches of RDD
  * Read/Load the Streamed Tweets in merged batches of RDDs
  * Save the Tweets in Parquet format, convert to Dataframe Table and run SQL queries
  * Explore the Streamed Tweets using SQL queries: Filter, Plot and Re-Shape


```scala

display(dbutils.fs.ls(s"/mnt/s3Data/filteredTweet"))

```
```scala

val tweet = sc.textFile(s"dbfs:/mnt/s3Data/filteredTweet/tweets_1463600700000").take(1)

```
```scala

val tweetDF = sqlContext.read.json(s"dbfs:/mnt/s3Data/filteredTweet/tweets_1463600700000")


```
```scala

tweetDF.show()

```
```scala

tweetDF.printSchema()

```
```scala

tweetDF.select("text").show

```
```scala

tweetDF.select("createdAt", "text", "favoriteCount","retweetCount").show

```
```scala

tweetDF.select(tweetDF("createdAt"), tweetDF("text"), tweetDF("favoriteCount")+1,tweetDF("retweetCount")+2).show

```
```scala

tweetDF.select(tweetDF("createdAt"), tweetDF("text"), tweetDF("favoriteCount")>1,tweetDF("retweetCount")).show

```
```scala

tweetDF.groupBy("favoriteCount").count().show

```
```scala

tweetDF.groupBy("retweetCount").count().show

```
```scala

tweetDF.groupBy("createdAt").count().show

```
```scala

tweetDF.groupBy("user").count().show

```
```scala

tweetDF.filter(tweetDF("favoriteCount")<1).show

```
```scala

tweetDF.filter(tweetDF("tex")==="trump").show

```
```scala

import sqlContext.implicits._

```
```scala

val tweedDS = tweetDF.select("text").show

```



### Merged RDD Streams


```scala

val tweetDF2 = sqlContext.read.json(s"dbfs:/mnt/s3Data/filteredTweet/*")


```
```scala

tweetDF2.show

```
```scala

tweetDF2.select("createdAt","text").show

```
```scala

tweetDF2.groupBy("createdAt").count.show

```
```scala

tweetDF2.groupBy("favoriteCount").count.show

```
```scala

tweetDF2.groupBy("retweetCount").count.show

```
```scala

tweetDF2.groupBy("text").count.show

```



### Saving the Tweet as a Table and Run SQL Queries





* Unmerged Tweet (tweetDF)


```scala

tweetDF.registerTempTable("tweetTable") // Register the DataFrames as a table.

```
```scala

val tweetText = sqlContext.sql("SELECT text FROM tweetTable") //Run SQL query

```
```scala

tweetText.take(1).foreach(println)

```
```scala

tweetText.map(t=>(t,1)).take(1).foreach(println)

```



### Merged RDD Streams


```scala

val tweetDF2 = sqlContext.read.json(s"dbfs:/mnt/s3Data/filteredTweet/*")

```



### Saving the Tweet as a Table and Run SQL Queries





***Parquet Data format: Save, Preview from the Tables menu, and Query directly without transforming to DataFrame***


```scala

tweetDF2.select("createdAt", "text", "favoriteCount","retweetCount").write.save("filterTweet.parquet")  
//Save the filter Tweet as parquest data format and go to create table to load it from the directory.

```
```scala

val mergedTweets = sqlContext.read.format("parquet").load("dbfs:/filterTweet.parquet") 
//This reads all the tweets in the parquet data format

```
```scala

mergedTweets.registerTempTable("mergedTweetsTable") // Save as a Table

```
```scala

val mergedTweetQuery = sqlContext.sql("SELECT * FROM mergedTweetsTable")

```


 ***Use SQL syntax to extract required fileds from the registered table***


```scala

%sql SELECT * FROM mergedTweetsTable LIMIT 1 

```
```scala

mergedTweetQuery.cache

```
```scala

mergedTweetQuery.map(c=>c(1)).foreach(println)

```
```scala

mergedTweetQuery.take(1)

```
```scala

val mergedTextQuery = sqlContext.sql("SELECT text FROM mergedTweetsTable").cache

```
```scala

mergedTextQuery.map(c=>c).take(1).foreach(println)

```
```scala

dbutils.fs.help

```
```scala

display(dbutils.fs.ls("dbfs:/"))

```
```scala

dbutils.fs.rm("dbfs:/filterTweet.parquet",recurse=true)

```
```scala

display(dbutils.fs.ls("dbfs:/"))

```
```scala

//display(dbutils.fs.ls("dbfs:/mnt/s3Data/filteredTweet/Trumps.txt"))

```
```scala

//dbutils.fs.rm("dbfs:/mnt/s3Data/filteredTweet/Trumps.txt",recurse=true)

```



### ETL Operationalization: Actual Tweeets Project


```scala

//dbutils.fs.rm(s"dbfs:/mnt/s3Data/TrumpTweetText.parquet",recurse=true)

```
```scala

display(dbutils.fs.ls(s"dbfs:/mnt/s3Data"))

```
```scala

dbutils.fs.rm(s"dbfs:/mnt/s3Data/tweetAll.parquet",recurse=true)

```
```scala

val tweetAll = sqlContext.read.json(s"dbfs:/mnt/s3Data/twitterNew/*")
tweetAll.cache

```
```scala

//Save the filter Tweet as parquest data format and go to create table to load it from the directory.
tweetAll.select("createdAt", "text", "favoriteCount","retweetCount").write.save(s"dbfs:/mnt/s3Data/tweetAll.parquet")  


```
```scala

val mergedTweetAll = sqlContext.read.format("parquet").load(s"dbfs:/mnt/s3Data/tweetAll.parquet") //This reads all the tweets in the parquet data format

```
```scala

mergedTweetAll.registerTempTable("mergedTweetTable")

```
```scala

%sql SELECT * FROM mergedTweetTable LIMIT 1

```
```scala

mergedTweetAll.show

```



## Returns the number of Tweets in the merged dataset (26.3million Tweets as at 3.30p.m today)


```scala

%sql SELECT COUNT(text) as TweetCount FROM mergedTweetTable 

```



## Returns the number of Tweets in the merged dataset group and sort by Date of Created Tweet


```scala

%sql SELECT COUNT(text) as TweetCount, createdAt FROM mergedTweetTable group by createdAt order by createdAt asc

```
```scala

%sql SELECT COUNT(text) as TweetCount, createdAt as DStreamTime FROM mergedTweetTable group by createdAt order by createdAt asc

```
```scala

%sql SELECT distinct createdAt FROM mergedTweetTable where favoriteCount == 1

```



## Filter Query by Keywords


```scala

%sql SELECT count(*) as TrumpsTweet FROM mergedTweetTable where text like "%trump%"

```
```scala

%sql SELECT COUNT(text) as TrumpsTweetCount, createdAt as DStreamTime FROM mergedTweetTable where text like "%trump%" group by createdAt order by createdAt asc

```
```scala

%sql SELECT createdAt as DStreamTime, text as TrumpsTweet FROM mergedTweetTable where text like "%trump%" order by createdAt asc limit 1

```
```scala

val TrumpTextQuery = sqlContext.sql("SELECT createdAt as date, text as review, favoriteCount as category FROM mergedTweetTable where text like '%trump%' order by createdAt asc").cache

```
```scala

val TrumpQuery = sqlContext.sql("SELECT createdAt as date, text as review, CAST(favoriteCount as FLOAT) as category FROM mergedTweetTable where text like '%trump%' order by createdAt asc").cache

```
```scala

TrumpTextQuery.select("date", "review", "category").write.save(s"dbfs:/mnt/s3Data/TrumpTweet.parquet")

```
```scala

TrumpQuery.select("date", "review", "category").write.save(s"dbfs:/mnt/s3Data/TrumpSentiment.parquet")

```
```scala

TrumpTextQuery.registerTempTable("TrumpTweetTable")

```
```scala

%sql ALTER TABLE TrumpTweetTable ALTER COLUMN category 

```
```scala

%sql SELECT * FROM TrumpTweetTable limit 3

```
```scala

TrumpTextQuery.count //Returns the number of tweets 

```
```scala

TrumpTextQuery.take(3).foreach(println)

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
