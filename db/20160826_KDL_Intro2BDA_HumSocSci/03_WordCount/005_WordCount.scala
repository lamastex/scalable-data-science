// Databricks notebook source exported at Sun, 28 Aug 2016 15:52:30 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Big Data Analysis for Humanities and Social Sciences](https://www.eventbrite.co.uk/e/big-data-analysis-for-the-humanities-and-social-sciences-tickets-26708754604)
// MAGIC 
// MAGIC ### August 26, 2016, King's Digital Lab, King's College London
// MAGIC #### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) 
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)

// COMMAND ----------

// MAGIC %md
// MAGIC The recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji") of an older version of this notebook:
// MAGIC 
// MAGIC [![sds/uji/week2/03_wordcount/006_wordcount](http://img.youtube.com/vi/zgkvusQdNLY/0.jpg)](https://www.youtube.com/v/zgkvusQdNLY?rel=0&autoplay=1&modestbranding=1&start=4613)

// COMMAND ----------

// MAGIC %md
// MAGIC # Word Count on US State of the Union (SoU) Addresses
// MAGIC 
// MAGIC * Word Count in big data is the equivalent of 'Hello World' in programming
// MAGIC * We count the number of occurences of each word in the first and last (2016) SoU addresses.

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
// MAGIC [scraping and parsing SoU addresses](/#workspace/scalable-data-science/xtraResources/sdsDatasets/scraperUSStateofUnionAddresses).
// MAGIC * This data is already made available in DBFS, our distributed file system.
// MAGIC * We only do the simplest word count with this data in this notebook and will do more sophisticated analyses in the sequel (including topic modeling, etc).

// COMMAND ----------

// MAGIC %md
// MAGIC ## Key Data Management Concepts 
// MAGIC 
// MAGIC ### The Structure Spectrum
// MAGIC 
// MAGIC **(watch now 1:10)**:
// MAGIC 
// MAGIC [![Structure Spectrum by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/pMSGGZVSwqo/0.jpg)](https://www.youtube.com/v/pMSGGZVSwqo?rel=0&autoplay=1&modestbranding=1&start=1&end=70)
// MAGIC 
// MAGIC Here we will be working with **unstructured** or **schema-never** data (plain text files).
// MAGIC ***
// MAGIC 
// MAGIC ### Files
// MAGIC 
// MAGIC **(watch later 1:43)**:
// MAGIC 
// MAGIC [![Files by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/NJyBQ-cQ3Ac/0.jpg)](https://www.youtube.com/v/NJyBQ-cQ3Ac?rel=0&autoplay=1&modestbranding=1&start=1)

// COMMAND ----------

// MAGIC %md
// MAGIC ###DBFS and dbutils - where is this dataset in our distributed file system?
// MAGIC * Since we are on the databricks cloud, it has a file system called DBFS
// MAGIC * DBFS is similar to HDFS, the Hadoop distributed file system
// MAGIC * dbutils allows us to interact with dbfs.
// MAGIC * The 'display' command displays the list of files in a given directory in the file system.

// COMMAND ----------

// MAGIC %md
// MAGIC # Let us get our hands dirty with SOUs

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Download and load data into distributed file system

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd
// MAGIC du -sh /databricks/driver # size available in this dir
// MAGIC du -sh /tmp # size available in this dir

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets"))

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/sou")

// COMMAND ----------

// MAGIC %md
// MAGIC Let us just copy the first SoU address by George Washington

// COMMAND ----------

import java.net.URL
import java.io.File
import org.apache.commons.io.FileUtils

val localFile = new File("/tmp/17900108.txt")
FileUtils.copyURLToFile(new URL("https://dl.dropboxusercontent.com/u/3531607/datasets/StateOfUnionAddressesUSPresidentsUntil2016/sou/17900108.txt"), localFile)
dbutils.fs.mv("file:/tmp/17900108.txt", "dbfs:/datasets/sou/")
display(dbutils.fs.ls("dbfs:/datasets/sou"))

// COMMAND ----------

// MAGIC %md
// MAGIC Let us display the *head* or the first few lines of the file `dbfs:/datasets/sou/17900108.txt` to see what it contains using `dbutils.fs.head` method.  
// MAGIC `head(file: String, maxBytes: int = 65536): String` -> Returns up to the first 'maxBytes' bytes of the given file as a String encoded in UTF-8
// MAGIC as follows:

// COMMAND ----------

dbutils.fs.head("dbfs:/datasets/sou/17900108.txt",673) // Cntrl+Enter to get the first 673 bytes of the file (which corresponds to the first five lines)

// COMMAND ----------

// MAGIC %md
// MAGIC ##### You Try!
// MAGIC Modify ``xxxx` in the cell below to read the first 1000 bytes from the file.

// COMMAND ----------

dbutils.fs.head("dbfs:/datasets/sou/17900108.txt", xxxx) // Cntrl+Enter to get the first 1000 bytes of the file

// COMMAND ----------

// MAGIC %md
// MAGIC ### Read the file into Spark Context as an RDD of Strings
// MAGIC * The `textFile` method on the available `SparkContext` `sc` can read the text file `dbfs:/datasets/sou/17900108.txt` into Spark and create an RDD of Strings
// MAGIC   * but this is done lazily until an action is taken on the RDD `sou17900108`!

// COMMAND ----------

val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt") // Cntrl+Enter to read in the textfile as RDD[String]

// COMMAND ----------

// MAGIC %md
// MAGIC ### Perform some actions on the RDD
// MAGIC * Each String in the RDD `sou17900108` represents one line of data from the file and can be made to perform one of the following actions:
// MAGIC   * count the number of elements in the RDD `sou17900108` (i.e., the number of lines in the text file `dbfs:/datasets/sou/17900108.txt`) using `sou17900108.count()`
// MAGIC   * display the contents of the RDD using `take` or `collect`.

// COMMAND ----------

sou17900108.count() // <Shift+Enter> to count the number of elements in the RDD

// COMMAND ----------

sou17900108.take(5) // <Shift+Enter> to display the first 5 elements of RDD

// COMMAND ----------

sou17900108.take(5).foreach(println) // <Shift+Enter> to display the first 5 elements of RDD line by line

// COMMAND ----------

sou17900108.collect // <Cntrl+Enter> to display all the elements of RDD

// COMMAND ----------

// MAGIC %md
// MAGIC ### Cache the RDD in (distributed) memory to avoid recreating it for each action
// MAGIC * Above, every time we took an action on the same RDD, the RDD was reconstructed from the textfile.  
// MAGIC   * Spark's advantage compared to Hadoop MapReduce is the ability to cache or store the RDD in distributed memory across the nodes.
// MAGIC * Let's use `.cache()` after creating an RDD so that it is in memory after the first action (and thus avoid reconstruction for subsequent actions).
// MAGIC   * count the number of elements in the RDD `sou17900108` (i.e., the number of lines in the text file `dbfs:/datasets/sou/17900108.txt`) using `sou17900108.count()`
// MAGIC   * display the contents of the RDD using `take` or `collect`.

// COMMAND ----------

// Shift+Enter to read in the textfile as RDD[String] and cache it in distributed memory
val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt")
sou17900108.cache() // cache the RDD in memory

// COMMAND ----------

sou17900108.count() // Shift+Enter during this count action the RDD is constructed from texfile and cached

// COMMAND ----------

sou17900108.count() // Shift+Enter during this count action the cached RDD is used (notice less time taken by the same command)

// COMMAND ----------

sou17900108.take(5) // <Cntrl+Enter> to display the first 5 elements of the cached RDD

// COMMAND ----------

// MAGIC %md
// MAGIC #### Lifecycle of a Spark Program
// MAGIC     
// MAGIC **(watch now 0:23)**:
// MAGIC 
// MAGIC [![Spark Program Lifecycle by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/HWZUqNYAJj4/0.jpg)](https://www.youtube.com/v/HWZUqNYAJj4?rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC 
// MAGIC ##### Summary
// MAGIC * create RDDs from:
// MAGIC   * some external data source (such as a distributed file system)
// MAGIC   * parallelized collection in your driver program
// MAGIC * lazily transform these RDDs into new RDDs
// MAGIC * cache some of those RDDs for future reuse
// MAGIC * you perform actions to execute parallel computation to produce results

// COMMAND ----------

// MAGIC %md
// MAGIC ### Transform lines to words
// MAGIC * We need to loop through each line and split the line into words
// MAGIC * For now, let us split using whitespace
// MAGIC * More sophisticated regular expressions can be used to split the line (as we will see soon)

// COMMAND ----------

sou17900108
.flatMap(line => line.split(" "))
.take(100)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Naive word count
// MAGIC At a first glace, to do a word count of George Washingtons SoU address, we are templed to do the following:
// MAGIC  * just break each line by the whitespace character " " and find the words using a `flatMap`
// MAGIC  * then do the `map` with the closure `word => (word, 1)` to initialize each `word` with a integer count of `1` 
// MAGIC     * ie., transform each word to a *(key, value)* pair or `Tuple` such as `(word, 1)`
// MAGIC  * then count all *value*s with the same *key* (`word` is the Key in our case) by doing a
// MAGIC    * `reduceByKey(_+_)` 
// MAGIC  * and finally `collect()` to display the results.

// COMMAND ----------

sou17900108
.flatMap( line => line.split(" ") )
.map( word => (word, 1) )
.reduceByKey(_+_)
.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Unfortunately, as you can see from the `collect` above:
// MAGIC * the words have punctuations at the end which means that the same words are being counted as different words. Eg: importance
// MAGIC * empty words are being counted
// MAGIC 
// MAGIC So we need a bit of `regex`'ing or regular-expression matching (all readily available from Scala via Java String types).
// MAGIC 
// MAGIC We will cover the three things we want to do with a simple example from Middle Earth!
// MAGIC * replace all multiple whitespace characters with one white space character " "
// MAGIC * replace all punction characters we specify within `[` and `]` such as `[,?.!:;]` by the empty string "" (i.e., remove these punctuation characters)
// MAGIC * convert everything to lower-case.

// COMMAND ----------

val example = "Master, Master!   It's me, Sméagol... mhrhm*%* But they took away our precious, they wronged us. Gollum will protect us..., Master, it's me Sméagol."

// COMMAND ----------

example.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
       .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
       .toLowerCase() // converting to lower-case

// COMMAND ----------

// MAGIC %md
// MAGIC ### More sophisticated word count
// MAGIC We are now ready to do a word count of George Washington's SoU on January 8th 1790 as follows:

// COMMAND ----------

val wordCount_sou17900108 = 
  sou17900108
    .flatMap(line => 
         line.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
             .toLowerCase() // converting to lower-case
             .split(" "))
    .map(x => (x, 1))
    .reduceByKey(_+_)
    
wordCount_sou17900108.collect()

// COMMAND ----------

val top10 = wordCount_sou17900108.sortBy(_._2, false).collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### Doing it all together for George Washington

// COMMAND ----------

sc.textFile("dbfs:/datasets/sou/17900108.txt") // George Washington's first SoU
//sc.textFile("dbfs:/datasets/sou/20160112.txt")   // Barrack Obama's second SoU
    .flatMap(line => 
         line.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
             .toLowerCase() // converting to lower-case
             .split(" "))
    .map(x => (x,1))
    .reduceByKey(_+_)
    .sortBy(_._2, false)
    .collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### A hack to get all the SoUs into databricks CE
// MAGIC This is not necessary if you have S3 credentials and just mount your s3 file ssytem here.

// COMMAND ----------

dbutils.fs.rm("dbfs:/datasets/sou/17900108.txt") // let' remove the file we added and add all in a loop

// COMMAND ----------

import java.net.URL
import java.io.File
import org.apache.commons.io.FileUtils

for (name <- fileNames) {
  println(name)
  val localFileName = "/tmp/"+name
  val localFile = new File(localFileName)
  val url="https://dl.dropboxusercontent.com/u/3531607/datasets/StateOfUnionAddressesUSPresidentsUntil2016/sou/"+name
  FileUtils.copyURLToFile(new URL(url), localFile)
  val fLocalFileName="file:"+localFileName
  dbutils.fs.mv(fLocalFileName, "dbfs:/datasets/sou/")
}
display(dbutils.fs.ls("dbfs:/datasets/sou")) // this can take a couple minutes

// COMMAND ----------

// MAGIC %md
// MAGIC ### Doing it all together for Barrack Obama

// COMMAND ----------

sc.textFile("dbfs:/datasets/sou/20160112.txt")   // Barrack Obama's second SoU
    .flatMap(line => 
         line.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
             .toLowerCase() // converting to lower-case
             .split(" "))
    .map(x => (x,1))
    .reduceByKey(_+_)
    .sortBy(_._2, false)
    .collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### Reading all SoUs at once using `wholetextFiles`
// MAGIC 
// MAGIC Let us next read all text files (ending with `.txt`) in the directory `dbfs:/datasets/sou/` at once!
// MAGIC 
// MAGIC `SparkContext.wholeTextFiles` lets you read a directory containing multiple small text files, and returns each of them as `(filename, content)` pairs of strings. 
// MAGIC 
// MAGIC This is in contrast with `textFile`, which would return one record per line in each file.

// COMMAND ----------

val souAll = sc.wholeTextFiles("dbfs:/datasets/sou/*.txt") // Shift+Enter to read all text files in dbfs:/datasets/sou/
souAll.cache() // let's cache this RDD for efficient reuse

// COMMAND ----------

souAll.count() // Shift+enter to count the number of entries in RDD[(String,String)]

// COMMAND ----------

souAll.count() // Cntrl+Enter to count the number of entries in cached RDD[(String,String)] again (much faster!)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's examine the first two elements of the RDD `souAll`.

// COMMAND ----------

souAll.take(2) // Cntr+Enter to see the first two elements of souAll

// COMMAND ----------

// MAGIC %md
// MAGIC Clearly, the elements are a pair of Strings, where the first String gives the filename and the second String gives the contents in the file. 
// MAGIC 
// MAGIC this can be very helpful to simply loop through the files and take an action, such as counting the number of words per address, as folows:

// COMMAND ----------

// this just collects the file names which is the first element of the tuple given by "._1" 
souAll.map( fileContentsPair => fileContentsPair._1).collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Let us find the number of words in each of the SoU addresses next (we need to work with Strings inside the closure!).

// COMMAND ----------

val wcs = souAll.map( fileContentsPair => 
  {
    val wc = fileContentsPair._2
                             .replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
                             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
                             .toLowerCase() // converting to lower-case
                             .split(" ") // split each word separated by white space
                             .size // find the length of array
    wc
  }    
)      

// COMMAND ----------

wcs.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ## HOMEWORK 
// MAGIC * HOWEWORK WordCount 1: `sortBy`
// MAGIC * HOMEWROK WordCount 2: `dbutils.fs`

// COMMAND ----------

// MAGIC %md
// MAGIC ##### HOMEWORK WordCount 1. `sortBy`
// MAGIC 
// MAGIC Let's understand `sortBy` a bit more carefully.

// COMMAND ----------

val example = "Master, Master!   It's me, Sméagol... mhrhm*%* But they took away our precious, they wronged us. Gollum will protect us..., Master, it's me Sméagol."

// COMMAND ----------

val words = example.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
       .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
       .toLowerCase() // converting to lower-case
       .split(" ")

// COMMAND ----------

val rddWords = sc.parallelize(words)

// COMMAND ----------

rddWords.take(10)

// COMMAND ----------

val wordCounts = rddWords
                  .map(x => (x,1))
                  .reduceByKey(_+_)

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

rddWords.count

// COMMAND ----------

// MAGIC %md
// MAGIC ##### HOMEWORK WordCount 2: `dbutils.fs`
// MAGIC 
// MAGIC Have a brief look at what other commands dbutils.fs supports.  We will introduce them as needed.

// COMMAND ----------

dbutils.fs.help // some of these were used to ETL this data into dbfs:/datasets/sou 

// COMMAND ----------

// MAGIC %md
// MAGIC # To grab all the data 
// MAGIC #### This is not possible in CE due to file size limits in /tmp unless you use your AWS S3 keys - hence our hack over a loop grabbing one file at a time :)

// COMMAND ----------

// MAGIC %sh
// MAGIC wget https://dl.dropboxusercontent.com/u/3531607/datasets/StateOfUnionAddressesUSPresidentsUntil2016/sou.tar.gz

// COMMAND ----------

// MAGIC %sh
// MAGIC # uncompress the sou.tar.gv
// MAGIC tar zxvf sou.tar.gz

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/sou") // make a directory in dbfs:/datasets called sou
dbutils.fs.cp("file:/databricks/driver/sou", "dbfs:/datasets/sou/",recurse=true)
display(dbutils.fs.ls("dbfs:/datasets/sou")) // Cntrl+Enter to display the files in dbfs:/datasets/sou

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Big Data Analysis for Humanities and Social Sciences](https://www.eventbrite.co.uk/e/big-data-analysis-for-the-humanities-and-social-sciences-tickets-26708754604)
// MAGIC 
// MAGIC ### August 26, 2016, King's Digital Lab, King's College London
// MAGIC #### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) 
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)