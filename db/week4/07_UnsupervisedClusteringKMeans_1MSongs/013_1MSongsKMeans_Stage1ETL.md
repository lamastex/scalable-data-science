// Databricks notebook source exported at Sat, 18 Jun 2016 11:05:13 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week4/07_UnsupervisedClusteringKMeans_1MSongs/013_1MSongsKMeans_Stage1ETL.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/week4/07_UnsupervisedClustering/013_KMeans_Stage1ETL](http://img.youtube.com/vi/_Lxtxmn0L-w/0.jpg)](https://www.youtube.com/v/_Lxtxmn0L-w?rel=0&autoplay=1&modestbranding=1&start=4823&end=5370)





**SOURCE:** This is the scala version of the python notebook from the databricks Community Edition that has been added to this databricks shard at [Workspace -> scalable-data-science -> xtraResources -> dbCE -> MLlib -> unsupervised -> clustering -> k-means -> 1MSongsPy_ETLExploreModel](/#workspace/scalable-data-science/xtraResources/dbCE/MLlib/unsupervised/clustering/k-means/1MSongsPy_ETLExploreModel) as extra resources for this project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/).





# Stage 1: Parsing songs data

![ETL](http://training.databricks.com/databricks_guide/end-to-end-01.png)

This is the first notebook in this tutorial. In this notebook we will read data from DBFS (DataBricks FileSystem). We will parse data and load it as a table that can be readily used in following notebooks.

By going through this notebook you can expect to learn how to read distributed data as an RDD, how to transform RDDs, and how to construct a Spark DataFrame from an RDD and register it as a table.

We first explore different files in our distributed file system. We use a header file to construct a Spark `Schema` object. We write a function that takes the header and casts strings in each line of our data to corresponding types. Once we run this function on the data we find that it fails on some corner caes. We update our function and finally get a parsed RDD. We combine that RDD and the Schema to construct a DataFame and register it as a temporary table in SparkSQL.





### Text data files are stored in `dbfs:/databricks-datasets/songs/data-001` 
You can conveniently list files on distributed file system (DBFS, S3 or HDFS) using `%fs` commands.


```scala

%fs ls /databricks-datasets/songs/data-001/

```



As you can see in the listing we have data files and a single header file. The header file seems interesting and worth a first inspection at first. The file is 377 bytes, therefore it is safe to collect the entire content of the file in the notebook. 


```scala

sc.textFile("databricks-datasets/songs/data-001/header.txt").collect()

```
```scala

//sc.textFile("databricks-datasets/songs/data-001/header.txt").collect.map(println) // uncomment to see line-by-line

```



As seen above each line in the header consists of a name and a type separated by colon. We will need to parse the header file as follows:


```scala

val header = sc.textFile("/databricks-datasets/songs/data-001/header.txt").map(line => {
                val headerElement = line.split(":")
                (headerElement(0), headerElement(1))
            }
           ).collect()

```



Let's define a `case class` called `Song` that will be used to represent each row of data in the files:
* `/databricks-datasets/songs/data-001/part-00000` through `/databricks-datasets/songs/data-001/part-00119` or the last `.../part-*****` file.


```scala

case class Song(artist_id: String, artist_latitude: Double, artist_longitude: Double, artist_location: String, artist_name: String, duration: Double, end_of_fade_in: Double, key: Int, key_confidence: Double, loudness: Double, release: String, song_hotness: Double, song_id: String, start_of_fade_out: Double, tempo: Double, time_signature: Double, time_signature_confidence: Double, title: String, year: Double, partial_sequence: Int)

```



Now we turn to data files. First, step is inspecting the first line of data to inspect its format.


```scala

// this is loads all the data - a subset of the 1M songs dataset
val dataRDD = sc.textFile("/databricks-datasets/songs/data-001/part-*") 

```
```scala

dataRDD.count // number of songs

```
```scala

dataRDD.take(3)

```



Each line of data consists of multiple fields separated by `\t`. With that information and what we learned from the header file, we set out to parse our data.
* We have already created a case class based on the header (which seems to agree with the 3 lines above).
* Next, we will create a function that takes each line as input and returns the case class as output.


```scala

def parseLine(line: String): Song = {
  
  val tokens = line.split("\t")
  Song(tokens(0), tokens(1).toDouble, tokens(2).toDouble, tokens(3), tokens(4), tokens(5).toDouble, tokens(6).toDouble, tokens(7).toInt, tokens(8).toDouble, tokens(9).toDouble, tokens(10), tokens(11).toDouble, tokens(12), tokens(13).toDouble, tokens(14).toDouble, tokens(15).toDouble, tokens(16).toDouble, tokens(17), tokens(18).toDouble, tokens(19).toInt)
}

```



With this function we can transform the dataRDD to another RDD that consists of Song case classes


```scala

val parsedRDD = dataRDD.map(parseLine)

```



To convert an RDD of case classes to a DataFrame, we just need to call the toDF method


```scala

val df = parsedRDD.toDF

```



Once we get a DataFrame we can register it as a temporary table. That will allow us to use its name in SQL queries.


```scala

df.registerTempTable("songsTable")

```



We can now cache our table. So far all operations have been lazy. This is the first time Spark will attempt to actually read all our data and apply the transformations. 

**If you are running Spark 1.6+ the next command will throw a parsing error.**


```scala

%sql cache table songsTable

```



The error means that we are trying to convert a missing value to a Double. Here is an updated version of the parseLine function to deal with missing values


```scala

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

```
```scala

val df = dataRDD.map(parseLine).toDF
df.registerTempTable("songsTable")

```



And let's try caching the table. We are going to access this data multiple times in following notebooks, therefore it is a good idea to cache it in memory for faster subsequent access.


```scala

%sql cache table songsTable

```



From now on we can easily query our data using the temporary table we just created and cached in memory. Since it is registered as a table we can conveniently use SQL as well as Spark API to access it.


```scala

%sql select * from songsTable limit 10

```



Next up is exploring this data. Click on the Exploration notebook to continue the tutorial.






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
