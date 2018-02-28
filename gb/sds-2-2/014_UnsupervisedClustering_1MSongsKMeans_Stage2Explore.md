[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Million Song Dataset - Kaggle Challenge
---------------------------------------

### Predict which songs a user will listen to.

**SOURCE:** This is just a *Scala*-rification of the *Python* notebook published in databricks community edition in 2016.

Stage 2: Exploring songs data
=============================

![Explore](http://training.databricks.com/databricks_guide/end-to-end-02.png)

This is the second notebook in this tutorial. In this notebook we do what any data scientist does with their data right after parsing it: exploring and understanding different aspect of data. Make sure you understand how we get the `songsTable` by reading and running the ETL notebook. In the ETL notebook we created and cached a temporary table named `songsTable`

Let's Do all the main bits in Stage 1 now before doing Stage 2 in this Notebook.
--------------------------------------------------------------------------------

``` scala
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
```

>     defined class Song
>     parseLine: (line: String)Song
>     dataRDD: org.apache.spark.rdd.RDD[String] = /databricks-datasets/songs/data-001/part-* MapPartitionsRDD[14036] at textFile at <console>:63
>     df: org.apache.spark.sql.DataFrame = [artist_id: string, artist_latitude: double ... 18 more fields]

``` scala
spark.catalog.listTables.show(false) // make sure the temp view of our table is there
```

>     +--------------------------+--------+-----------+---------+-----------+
>     |name                      |database|description|tableType|isTemporary|
>     +--------------------------+--------+-----------+---------+-----------+
>     |cities_csv                |default |null       |EXTERNAL |false      |
>     |cleaned_taxes             |default |null       |MANAGED  |false      |
>     |commdettrumpclintonretweet|default |null       |MANAGED  |false      |
>     |donaldtrumptweets         |default |null       |EXTERNAL |false      |
>     |linkage                   |default |null       |EXTERNAL |false      |
>     |nations                   |default |null       |EXTERNAL |false      |
>     |newmplist                 |default |null       |EXTERNAL |false      |
>     |ny_baby_names             |default |null       |MANAGED  |false      |
>     |nzmpsandparty             |default |null       |EXTERNAL |false      |
>     |pos_neg_category          |default |null       |EXTERNAL |false      |
>     |rna                       |default |null       |MANAGED  |false      |
>     |samh                      |default |null       |EXTERNAL |false      |
>     |table1                    |default |null       |EXTERNAL |false      |
>     |test_table                |default |null       |EXTERNAL |false      |
>     |uscites                   |default |null       |EXTERNAL |false      |
>     |songstable                |null    |null       |TEMPORARY|true       |
>     +--------------------------+--------+-----------+---------+-----------+

A first inspection
------------------

A first step to any data exploration is viewing sample data. For this purpose we can use a simple SQL query that returns first 10 rows.

``` sql
select * from songsTable limit 10
```

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

``` scala
table("songsTable").printSchema()
```

>     root
>      |-- artist_id: string (nullable = true)
>      |-- artist_latitude: double (nullable = false)
>      |-- artist_longitude: double (nullable = false)
>      |-- artist_location: string (nullable = true)
>      |-- artist_name: string (nullable = true)
>      |-- duration: double (nullable = false)
>      |-- end_of_fade_in: double (nullable = false)
>      |-- key: integer (nullable = false)
>      |-- key_confidence: double (nullable = false)
>      |-- loudness: double (nullable = false)
>      |-- release: string (nullable = true)
>      |-- song_hotness: double (nullable = false)
>      |-- song_id: string (nullable = true)
>      |-- start_of_fade_out: double (nullable = false)
>      |-- tempo: double (nullable = false)
>      |-- time_signature: double (nullable = false)
>      |-- time_signature_confidence: double (nullable = false)
>      |-- title: string (nullable = true)
>      |-- year: double (nullable = false)
>      |-- partial_sequence: integer (nullable = false)

``` sql
select count(*) from songsTable
```

| count(1) |
|----------|
| 31369.0  |

``` scala
table("songsTable").count() // or equivalently with DataFrame API - recall table("songsTable") is a DataFrame
```

>     res4: Long = 31369

``` scala
display(sqlContext.sql("SELECT duration, year FROM songsTable")) // Aggregation is set to 'Average' in 'Plot Options'
```

| duration  | year   |
|-----------|--------|
| 213.7073  | 2003.0 |
| 133.25016 | 2009.0 |
| 247.32689 | 0.0    |
| 337.05751 | 0.0    |
| 430.23628 | 2006.0 |
| 186.80118 | 0.0    |
| 361.89995 | 0.0    |
| 220.00281 | 2007.0 |
| 156.86485 | 2004.0 |
| 256.67873 | 0.0    |
| 204.64281 | 0.0    |
| 112.48281 | 0.0    |
| 170.39628 | 0.0    |
| 215.95383 | 0.0    |
| 303.62077 | 0.0    |
| 266.60526 | 0.0    |
| 326.19057 | 1997.0 |
| 51.04281  | 2009.0 |
| 129.4624  | 0.0    |
| 253.33506 | 2003.0 |
| 237.76608 | 2004.0 |
| 132.96281 | 0.0    |
| 399.35955 | 2006.0 |
| 168.75057 | 1991.0 |
| 396.042   | 0.0    |
| 192.10404 | 1968.0 |
| 12.2771   | 2006.0 |
| 367.56853 | 0.0    |
| 189.93587 | 0.0    |
| 233.50812 | 0.0    |

Truncated to 30 rows

Exercises
---------

1.  Why do you think average song durations increase dramatically in 70's?
2.  Add error bars with standard deviation around each average point in the plot.
3.  How did average loudness change over time?
4.  How did tempo change over time?
5.  What other aspects of songs can you explore with this technique?

Sampling and visualizing
------------------------

Another technique for visually exploring large data, which we are going to try, is sampling data. \* First step is generating a sample. \* With sampled data we can produce a scatter plot as follows.

``` python
# let's use ggplot from python
# note that this is second natural way to 'babble' between languages - using the right tool for the job!
#   recall: the first naive but solid way was to use parquet files to write and read from different languages 
#           with parquet files you can tackle the babbling problem when the table is too large to be 'Viewed'
from ggplot import *
sampled = sqlContext.sql("select year, duration from songsTable where year > 1930 and year < 2012")\
  .sample(withReplacement = False, fraction = 0.1).toPandas()
  
p = ggplot(sampled, aes(x = 'year', y = 'duration')) + ylim(0, 800) + \
  geom_smooth(size=3, span=0.3) + geom_point(aes(color = 'blue', size = 4))
display(p)
```

Exercises
---------

1.  Add jitter to year value in the plot above.
2.  Plot sampled points for other parameters in the data.

Next step is clustering the data. Click on the next notebook (Model) to follow the tutorial.