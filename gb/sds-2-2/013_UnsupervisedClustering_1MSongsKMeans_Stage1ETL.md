[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Million Song Dataset - Kaggle Challenge
---------------------------------------

### Predict which songs a user will listen to.

**SOURCE:** This is just a *Scala*-rification of the *Python* notebook published in databricks community edition in 2016.

*CAUTION:* This notebook is expected to have an error in command 28 (`Cmd 28` in databricks notebook). You are meant to learn how to fix this error with simple exception-handling to become a better data scientist. So ignore this warning, if any.

Stage 1: Parsing songs data
===========================

![ETL](http://training.databricks.com/databricks_guide/end-to-end-01.png)

This is the first notebook in this tutorial. In this notebook we will read data from DBFS (DataBricks FileSystem). We will parse data and load it as a table that can be readily used in following notebooks.

By going through this notebook you can expect to learn how to read distributed data as an RDD, how to transform RDDs, and how to construct a Spark DataFrame from an RDD and register it as a table.

We first explore different files in our distributed file system. We use a header file to construct a Spark `Schema` object. We write a function that takes the header and casts strings in each line of our data to corresponding types. Once we run this function on the data we find that it fails on some corner caes. We update our function and finally get a parsed RDD. We combine that RDD and the Schema to construct a DataFame and register it as a temporary table in SparkSQL.

### Text data files are stored in `dbfs:/databricks-datasets/songs/data-001`

You can conveniently list files on distributed file system (DBFS, S3 or HDFS) using `%fs` commands.

| path                                                | name       | size    |
|-----------------------------------------------------|------------|---------|
| dbfs:/databricks-datasets/songs/data-001/header.txt | header.txt | 377.0   |
| dbfs:/databricks-datasets/songs/data-001/part-00000 | part-00000 | 52837.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00001 | part-00001 | 52469.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00002 | part-00002 | 51778.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00003 | part-00003 | 50551.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00004 | part-00004 | 53449.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00005 | part-00005 | 53301.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00006 | part-00006 | 54184.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00007 | part-00007 | 50924.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00008 | part-00008 | 52533.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00009 | part-00009 | 54570.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00010 | part-00010 | 54338.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00011 | part-00011 | 51836.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00012 | part-00012 | 52297.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00013 | part-00013 | 52044.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00014 | part-00014 | 50704.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00015 | part-00015 | 54158.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00016 | part-00016 | 50080.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00017 | part-00017 | 47708.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00018 | part-00018 | 8858.0  |
| dbfs:/databricks-datasets/songs/data-001/part-00019 | part-00019 | 53323.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00020 | part-00020 | 57877.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00021 | part-00021 | 52491.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00022 | part-00022 | 54791.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00023 | part-00023 | 50682.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00024 | part-00024 | 52863.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00025 | part-00025 | 47416.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00026 | part-00026 | 50130.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00027 | part-00027 | 53462.0 |
| dbfs:/databricks-datasets/songs/data-001/part-00028 | part-00028 | 54179.0 |

Truncated to 30 rows

As you can see in the listing we have data files and a single header file. The header file seems interesting and worth a first inspection at first. The file is 377 bytes, therefore it is safe to collect the entire content of the file in the notebook.

``` scala
sc.textFile("databricks-datasets/songs/data-001/header.txt").collect() 
```

>     res1: Array[String] = Array(artist_id:string, artist_latitude:double, artist_longitude:double, artist_location:string, artist_name:string, duration:double, end_of_fade_in:double, key:int, key_confidence:double, loudness:double, release:string, song_hotnes:double, song_id:string, start_of_fade_out:double, tempo:double, time_signature:double, time_signature_confidence:double, title:string, year:double, partial_sequence:int)

Remember you can `collect()` a huge RDD and crash the driver program - so it is a good practise to take a couple lines and count the number of lines, especially if you have no idea what file you are trying to read.

``` scala
sc.textFile("databricks-datasets/songs/data-001/header.txt").take(2)
```

>     res3: Array[String] = Array(artist_id:string, artist_latitude:double)

``` scala
sc.textFile("databricks-datasets/songs/data-001/header.txt").count()
```

>     res4: Long = 20

``` scala
//sc.textFile("databricks-datasets/songs/data-001/header.txt").collect.map(println) // uncomment to see line-by-line
```

As seen above each line in the header consists of a name and a type separated by colon. We will need to parse the header file as follows:

``` scala
val header = sc.textFile("/databricks-datasets/songs/data-001/header.txt").map(line => {
                val headerElement = line.split(":")
                (headerElement(0), headerElement(1))
            }
           ).collect()
```

>     header: Array[(String, String)] = Array((artist_id,string), (artist_latitude,double), (artist_longitude,double), (artist_location,string), (artist_name,string), (duration,double), (end_of_fade_in,double), (key,int), (key_confidence,double), (loudness,double), (release,string), (song_hotnes,double), (song_id,string), (start_of_fade_out,double), (tempo,double), (time_signature,double), (time_signature_confidence,double), (title,string), (year,double), (partial_sequence,int))

Let's define a `case class` called `Song` that will be used to represent each row of data in the files: \* `/databricks-datasets/songs/data-001/part-00000` through `/databricks-datasets/songs/data-001/part-00119` or the last `.../part-*****` file.

``` scala
case class Song(artist_id: String, artist_latitude: Double, artist_longitude: Double, artist_location: String, artist_name: String, duration: Double, end_of_fade_in: Double, key: Int, key_confidence: Double, loudness: Double, release: String, song_hotness: Double, song_id: String, start_of_fade_out: Double, tempo: Double, time_signature: Double, time_signature_confidence: Double, title: String, year: Double, partial_sequence: Int)
```

>     defined class Song

Now we turn to data files. First, step is inspecting the first line of data to inspect its format.

``` scala
// this is loads all the data - a subset of the 1M songs dataset
val dataRDD = sc.textFile("/databricks-datasets/songs/data-001/part-*") 
```

>     dataRDD: org.apache.spark.rdd.RDD[String] = /databricks-datasets/songs/data-001/part-* MapPartitionsRDD[13983] at textFile at <console>:35

``` scala
dataRDD.count // number of songs
```

>     res5: Long = 31369

``` scala
dataRDD.take(3)
```

>     res6: Array[String] = Array(AR81V6H1187FB48872	nan	nan		Earl Sixteen	213.7073	0.0	11	0.419	-12.106	Soldier of Jah Army	nan	SOVNZSZ12AB018A9B8	208.289	125.882	1	0.0	Rastaman	2003	--, ARVVZQP11E2835DBCB	nan	nan		Wavves	133.25016	0.0	0	0.282	0.596	Wavvves	0.471578247701	SOJTQHQ12A8C143C5F	128.116	89.519	1	0.0	I Want To See You (And Go To The Movies)	2009	--, ARFG9M11187FB3BBCB	nan	nan	Nashua USA	C-Side	247.32689	0.0	9	0.612	-4.896	Santa Festival Compilation 2008 vol.1	nan	SOAJSQL12AB0180501	242.196	171.278	5	1.0	Loose on the Dancefloor	0	225261)

Each line of data consists of multiple fields separated by `\t`. With that information and what we learned from the header file, we set out to parse our data. \* We have already created a case class based on the header (which seems to agree with the 3 lines above). \* Next, we will create a function that takes each line as input and returns the case class as output.

``` scala
// let's do this 'by hand' to re-flex our RDD-muscles :)
// although this is not a robust way to read from a data engineering perspective (without fielding exceptions)
def parseLine(line: String): Song = {
  
  val tokens = line.split("\t")
  Song(tokens(0), tokens(1).toDouble, tokens(2).toDouble, tokens(3), tokens(4), tokens(5).toDouble, tokens(6).toDouble, tokens(7).toInt, tokens(8).toDouble, tokens(9).toDouble, tokens(10), tokens(11).toDouble, tokens(12), tokens(13).toDouble, tokens(14).toDouble, tokens(15).toDouble, tokens(16).toDouble, tokens(17), tokens(18).toDouble, tokens(19).toInt)
}
```

>     parseLine: (line: String)Song

With this function we can transform the dataRDD to another RDD that consists of Song case classes

``` scala
val parsedRDD = dataRDD.map(parseLine)
```

>     parsedRDD: org.apache.spark.rdd.RDD[Song] = MapPartitionsRDD[13984] at map at <console>:36

To convert an RDD of case classes to a DataFrame, we just need to call the toDF method

``` scala
val df = parsedRDD.toDF
```

>     df: org.apache.spark.sql.DataFrame = [artist_id: string, artist_latitude: double ... 18 more fields]

Once we get a DataFrame we can register it as a temporary table. That will allow us to use its name in SQL queries.

``` scala
df.createOrReplaceTempView("songsTable")
```

We can now cache our table. So far all operations have been lazy. This is the first time Spark will attempt to actually read all our data and apply the transformations.

**If you are running Spark 1.6+ the next command will throw a parsing error.**

The error means that we are trying to convert a missing value to a Double. Here is an updated version of the parseLine function to deal with missing values

``` scala
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
```

>     parseLine: (line: String)Song

``` scala
val df = dataRDD.map(parseLine).toDF
df.createOrReplaceTempView("songsTable")
```

>     df: org.apache.spark.sql.DataFrame = [artist_id: string, artist_latitude: double ... 18 more fields]

And let's try caching the table. We are going to access this data multiple times in following notebooks, therefore it is a good idea to cache it in memory for faster subsequent access.

From now on we can easily query our data using the temporary table we just created and cached in memory. Since it is registered as a table we can conveniently use SQL as well as Spark API to access it.

| artist\_id         | artist\_latitude | artist\_longitude | artist\_location           | artist\_name      | duration  | end\_of\_fade\_in | key  | key\_confidence | loudness | release                                      | song\_hotness  |
|--------------------|------------------|-------------------|----------------------------|-------------------|-----------|-------------------|------|-----------------|----------|----------------------------------------------|----------------|
| AR81V6H1187FB48872 | 0.0              | 0.0               |                            | Earl Sixteen      | 213.7073  | 0.0               | 11.0 | 0.419           | -12.106  | Soldier of Jah Army                          | 0.0            |
| ARVVZQP11E2835DBCB | 0.0              | 0.0               |                            | Wavves            | 133.25016 | 0.0               | 0.0  | 0.282           | 0.596    | Wavvves                                      | 0.471578247701 |
| ARFG9M11187FB3BBCB | 0.0              | 0.0               | Nashua USA                 | C-Side            | 247.32689 | 0.0               | 9.0  | 0.612           | -4.896   | Santa Festival Compilation 2008 vol.1        | 0.0            |
| ARK4Z2O1187FB45FF0 | 0.0              | 0.0               |                            | Harvest           | 337.05751 | 0.247             | 4.0  | 0.46            | -9.092   | Underground Community                        | 0.0            |
| AR4VQSG1187FB57E18 | 35.25082         | -91.74015         | Searcy, AR                 | Gossip            | 430.23628 | 0.0               | 2.0  | 3.4e-2          | -6.846   | Yr  Mangled Heart                            | 0.0            |
| ARNBV1X1187B996249 | 0.0              | 0.0               |                            | Alex              | 186.80118 | 0.0               | 4.0  | 0.641           | -16.108  | Jolgaledin                                   | 0.0            |
| ARXOEZX1187B9B82A1 | 0.0              | 0.0               |                            | Elie Attieh       | 361.89995 | 0.0               | 7.0  | 0.863           | -4.919   | ELITE                                        | 0.0            |
| ARXPUIA1187B9A32F1 | 0.0              | 0.0               | Rome, Italy                | Simone Cristicchi | 220.00281 | 2.119             | 4.0  | 0.486           | -6.52    | Dall'Altra Parte Del Cancello                | 0.484225272411 |
| ARNPPTH1187B9AD429 | 51.4855          | -0.37196          | Heston, Middlesex, England | Jimmy Page        | 156.86485 | 0.334             | 7.0  | 0.493           | -9.962   | No Introduction Necessary \[Deluxe Edition\] | 0.0            |
| AROGWRA122988FEE45 | 0.0              | 0.0               |                            | Christos Dantis   | 256.67873 | 2.537             | 9.0  | 0.742           | -13.404  | Daktilika Apotipomata                        | 0.0            |

Truncated to 12 cols

Next up is exploring this data. Click on the Exploration notebook to continue the tutorial.