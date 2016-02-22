// Databricks notebook source exported at Mon, 22 Feb 2016 10:07:13 UTC
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
// MAGIC # Using Spark to perform Word Count on US State of the Union Addresses
// MAGIC 
// MAGIC * Word Count in big data is the equivalent of 'Hello World' in programming
// MAGIC * We count the number of occurences of each word in a document

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC An interesting analysis of the textual content of the *State of the Union (SoU)* addresses by all US presidents was done in:
// MAGIC * [Alix Rule, Jean-Philippe Cointet, and Peter S. Bearman, Lexical shifts, substantive changes, and continuity in State of the Union discourse, 1790–2014, PNAS 2015 112 (35) 10837-10844; doi:10.1073/pnas.1512221112](http://www.pnas.org/content/112/35/10837.full).
// MAGIC 
// MAGIC 
// MAGIC ![](http://www.pnas.org/content/112/35/10837/F5.large.jpg)
// MAGIC 
// MAGIC [Fig. 5](http://www.pnas.org/content/112/35/10837.full). A river network captures the flow across history of US political discourse, as perceived by contemporaries. Time moves along the x axis. Clusters on semantic networks of 300 most frequent terms for each of 10 historical periods are displayed as vertical bars. Relations between clusters of adjacent periods are indexed by gray flows, whose density reflects their degree of connection. Streams that connect at any point in history may be considered to be part of the same system, indicated with a single color. 
// MAGIC 
// MAGIC ## Let us investigate this dataset ourselves!
// MAGIC 1. We first get the source text data by scraping and parsig from [http://stateoftheunion.onetwothree.net/texts/index.html](http://stateoftheunion.onetwothree.net/texts/index.html) as explained in 
// MAGIC [scraping and parsing SoU addresses](/#workspace/scalable-data-science/week1/03_WordCount/scraperUSStateofUnionAddresses).
// MAGIC * This data is already made available in DBFS, our distributed file system.

// COMMAND ----------

// MAGIC %md
// MAGIC ###DBFS and dbutils
// MAGIC * Since we are on the databricks cloud, it has a file system called DBFS
// MAGIC * DBFS is similar to HDFS, the Hadoop distributed file system
// MAGIC * dbutils allows us to interact with dbfs.
// MAGIC * The 'display' command displays the list of files in a given directory in the file system.

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/sou"))

// COMMAND ----------

// MAGIC %md
// MAGIC Let us display the *head* or the first few lines of the file `dbfs:/datasets/sou/17900108.txt` to see what it contains using `dbutils.fs.head` method.  
// MAGIC `head(file: String, maxBytes: int = 65536): String` -> Returns up to the first 'maxBytes' bytes of the given file as a String encoded in UTF-8
// MAGIC as follows:

// COMMAND ----------

dbutils.fs.head("dbfs:/datasets/sou/17900108.txt",1000) // first 1000 bytes of the file

// COMMAND ----------

val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt")

// COMMAND ----------

sou17900108.take(5)

// COMMAND ----------

sou17900108.collect

// COMMAND ----------

sou17900108
.flatMap(line => line.split(" "))
//.filter(x => x == "I")
.map(x => (x,1)).reduceByKey(_+_).collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### For this toy example, let us use the file /databricks-datasets/README.md
// MAGIC Let us display the first few lines of the file to see what it contains.

// COMMAND ----------

dbutils.fs.head("/databricks-datasets/README.md")

// COMMAND ----------

// MAGIC %md
// MAGIC ### Read the file into Spark
// MAGIC * The textFile method on SparkContext reads a Text File into Spark and returns an RDD of Strings
// MAGIC * Each String represents one line of data from the file

// COMMAND ----------

val file = sc.textFile("/databricks-datasets/README.md")

// COMMAND ----------

// MAGIC %md
// MAGIC What does the RDD contain?

// COMMAND ----------

file.take(5)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Transform lines to words
// MAGIC * We need to loop through each line and split the line into words
// MAGIC * For now, let us split using whitespace
// MAGIC * More sophisticated REGEX expressions can be used to split the line, but we can resreve them for later

// COMMAND ----------

val words = file.flatMap(line => line.split(" "))

// COMMAND ----------

words.take(10)

// COMMAND ----------

// MAGIC %md
// MAGIC Let us remove words that have length 0

// COMMAND ----------

val filteredWords = words.filter(word => word.length > 0)

// COMMAND ----------

filteredWords.take(10)

// COMMAND ----------

// MAGIC %md
// MAGIC Let us make our count of words case-insensitive

// COMMAND ----------

val caseInsensitiveWords = filteredWords.map(word => word.toLowerCase)

// COMMAND ----------

// MAGIC %md
// MAGIC * Now, transform each word to a Tuple of (word, 1)
// MAGIC * We can then group all Tuples with the same 'Key' (word) and do a count

// COMMAND ----------

val wordTuples = caseInsensitiveWords.map(word => (word, 1))

// COMMAND ----------

wordTuples.take(5)

// COMMAND ----------

val wordCounts = wordTuples.reduceByKey(_ + _)

// COMMAND ----------

wordCounts.collect

// COMMAND ----------

val top10 = wordCounts.sortBy(_._2, false).take(10)

// COMMAND ----------

// MAGIC %md
// MAGIC Make your code easy to read for other developers ;)  
// MAGIC Use 'case classes' with well defined variable names that everyone can understand

// COMMAND ----------

val top10 = wordCounts.sortBy({
  case (word, count) => count
}, false).take(10)

// COMMAND ----------

// MAGIC %md
// MAGIC If you just want a total count of all words in the file

// COMMAND ----------

caseInsensitiveWords.count

// COMMAND ----------

// MAGIC %md
// MAGIC ## HOMEWORK
// MAGIC ### What other commands does dbutils.fs support?

// COMMAND ----------

dbutils.fs.help // some of these were used to ETL this data into dbfs:/datasets/sou 

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