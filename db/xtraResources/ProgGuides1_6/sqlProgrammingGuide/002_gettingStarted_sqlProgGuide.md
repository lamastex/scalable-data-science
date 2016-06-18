// Databricks notebook source exported at Mon, 14 Mar 2016 04:15:07 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





This is an elaboration of the [Apache Spark 1.6 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).

# [Getting Started](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide)

## [Spark Sql Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/000_sqlProgGuide)

-   [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide)
    -   SQL
    -   DataFrames
    -   Datasets
-   [Getting Started](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide)
    -   Starting Point: SQLContext
    -   Creating DataFrames
    -   DataFrame Operations
    -   Running SQL Queries Programmatically
    -   Creating Datasets
    -   Interoperating with RDDs
        -   Inferring the Schema Using Reflection
        -   Programmatically Specifying the Schema
-   [Data Sources](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/003_dataSources_sqlProgGuide)
    -   Generic Load/Save Functions
        -   Manually Specifying Options
        -   Run SQL on files directly
        -   Save Modes
        -   Saving to Persistent Tables
    -   Parquet Files
        -   Loading Data Programmatically
        -   Partition Discovery
        -   Schema Merging
        -   Hive metastore Parquet table conversion
            -   Hive/Parquet Schema Reconciliation
            -   Metadata Refreshing
        -   Configuration
    -   JSON Datasets
    -   Hive Tables
        -   Interacting with Different Versions of Hive Metastore
    -   JDBC To Other Databases
    -   Troubleshooting
-   [Performance Tuning](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)
    -   Caching Data In Memory
    -   Other Configuration Options
-   [Distributed SQL Engine](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)
    -   Running the Thrift JDBC/ODBC server
    -   Running the Spark SQL CLI






# [Getting Started](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide)

## Starting Point: SQLContext

The entry point into all functionality in Spark SQL is the
[`SQLContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SQLContext) class, or one of its descendants, e.g. [`HiveContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.hive.HiveContext). To create a basic `SQLContext`, all you need is a `SparkContext`. Usually, when building application (running on production-like on-premises cluster) you would manually create `SQLContext` using something like this:

```scala
// An existing SparkContext
val sc: SparkContext = ...
val sqlContext = new org.apache.spark.sql.SQLContext(sc)

// This is used to implicitly convert an RDD to a DataFrame (see examples below)
import sqlContext.implicits._
```

But in Databricks notebook (similar to `spark-shell`) `SQLContext` is already created for you and is available as `sqlContext`.


```scala

// Evaluation of the cell by Ctrl+Enter will print sqlContext available in notebook
sqlContext

```



After evaluation you should see something like this:
```
org.apache.spark.sql.hive.HiveContext = org.apache.spark.sql.hive.HiveContext@7a955f82
```
Note that SQLContext in notebook is actually HiveContext. The difference is that HiveContext provides richer functionality over standard SQLContext, e.g. window functions were only available with HiveContext up to Spark 1.5.2, or usage of Hive user-defined functions. This originates from the fact that Spark SQL parser was built based on HiveQL parser, so only HiveContext was supporting full HiveQL syntax. Now it is changing, and SQLContext supports most of the functionality of the descendant (window functions should be available in SQLContext in 1.6+)

> Note that Hive installation is not required when working with SQLContext or HiveContext, Spark comes with built-in derby database.





## Creating DataFrames

With a `SQLContext`, applications can create [`DataFrame`](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) from an [existing `RDD`](sql-programming-guide.html#interoperating-with-rdds), from a Hive table, or from various [datasources](sql-programming-guide.html#data-sources).

Just to recap, a DataFrame is a distributed collection of data organized into named columns. You can think of it as an organized into table RDD of case class `Row` (which is not exactly true). DataFrames, in comparison to RDDs, are backed by rich optimizations, including tracking their own schema, adaptive query execution, code generation including whole stage codegen, extensible Catalyst optimizer, and project [Tungsten](https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html). 

> Note that performance for DataFrames is the same across languages Scala, Java, Python, and R. This is due to the fact that the planning phase is just language-specific, only logical plan is constructed in Python, and all the physical execution is compiled and executed as JVM bytecode.


```scala

// Spark has some of the pre-built methods to create simple DataFrames

// 1. Empty DataFrame, not really interesting, is it?
val empty = sqlContext.emptyDataFrame

```
```scala

// 2. Range of numbers, note that Spark automatically names column as "id"
val range = sqlContext.range(0, 10)

// In order to get a preview of data in DataFrame use "show()"
range.show(3)

```



You can also use different datasources that will be shown later or load Hive tables directly into Spark.

We have already created a table of social media usage from NYC (you will see later how this table was built from raw data). 
> See the very bottom of this worksheet to see how this was done.

First let's make sure this table is available for us.


```scala

// Let's find out what tables are already available for loading
sqlContext.tables.show()

```



It looks like the table ``social_media_usage`` is available as a permanent table (``isTemporary`` set as ``false``). 

Next let us do the following:
* load this table as a DataFrame
* print its schema and
* show the first 20 rows.


```scala

val df = sqlContext.table("social_media_usage") // Ctrl+Enter

```



As you can see the immutable value ``df`` is a DataFrame and more specifically it is:

> ``org.apache.spark.sql.DataFrame = [agency: string, platform: string, url: string, visits: int]``.





Now let us print schema of the DataFrame ``df`` and have a look at the actual data:


```scala

// Ctrl+Enter
df.printSchema() // prints schema of the DataFrame
df.show() // shows first n (default is 20) rows

```



> Note that `(nullable = true)` simply means if the value is allowed to be `null`.

Let us count the number of rows in ``df``.


```scala

df.count() // Ctrl+Enter

```



So there are 5899 records or rows in the DataFrame `df`. Pretty good! You can also select individual columns using so-called DataFrame API, as follows:


```scala

val platforms = df.select("platform") // Shift+Enter

```
```scala

platforms.count() // Shift+Enter to count the number of rows

```
```scala

platforms.show(5) // Ctrl+Enter to show top 5 rows

```



You can also apply ``.distinct()`` to extract only unique entries as follows:


```scala

val uniquePlatforms = df.select("platform").distinct() // Shift+Enter

```
```scala

uniquePlatforms.count() // Ctrl+Enter to count the number of distinct platforms

```



Let's see all the rows of the DataFrame `uniquePlatforms`. 

> Note that `display(uniquePlatforms)` unlike `uniquePlatforms.show()` displays all rows of the DataFrame + gives you ability to select different view, e.g. charts.


```scala

display(uniquePlatforms) // Ctrl+Enter to show all rows; use the scroll-bar on the right of the display to see all platforms

```



### Spark SQL and DataFrame API
Spark SQL provides DataFrame API that can perform relational operations on both external data sources and internal collections, which is similar to widely used data frame concept in R, but evaluates operations support lazily (remember RDDs?), so that it can perform relational optimizations. This API is also available in Java, Python and R, but some functionality may not be available, although with every release of Spark people minimize this gap. 

So we give some examples how to query data in Python and R, but continue with Scala. You can do all DataFrame operations in this notebook using Python or R. 


```scala

%py
# Ctrl+Enter to evaluate this python cell, recall '#' is the pre-comment character in python
# Using Python to query our "social_media_usage" table
pythonDF = sqlContext.table("social_media_usage").select("platform").distinct()
pythonDF.show(3)

```
```scala

%r
## Shift+Enter to use R in this cell to query "social_media_usage" table
rDF <- table(sqlContext, "social_media_usage")
selectedRDF <- distinct(select(rDF, "platform"))

showDF(selectedRDF, 3)

```
```scala

%sql
-- Ctrl+Enter to achieve the same result using standard SQL syntax!
select distinct platform from social_media_usage

```



Now it is time for some tips around how you use ``select`` and what the difference is between ``$"a"``, ``col("a")``, ``df("a")``. 

As you probably have noticed by now, you can specify individual columns to select by providing String values in select statement. But sometimes you need to:
- distinguish between columns with the same name
- use it to filter (actually you can still filter using full String expression)
- do some "magic" with joins and user-defined functions (this will be shown later)

So Spark gives you ability to actually specify columns when you select. Now the difference between all those three notations is ... none, those things are just aliases for a `Column` in Spark SQL, which means following expressions yield the same result:
```scala
// Using string expressions
df.select("agency", "visits")

// Using "$" alias for column
df.select($"agency", $"visits")

// Using "col" alias for column
df.select(col("agency"), col("visits"))

// Using DataFrame name for column
df.select(df("agency"), df("visits"))
```

This "same-difference" applies to filtering, i.e. you can either use full expression to filter, or column as shown in the following example:
```scala
// Using column to filter
df.select("visits").filter($"visits" > 100)

// Or you can use full expression as string
df.select("visits").filter("visits > 100")
```

> Note that ``$"visits" > 100`` expression looks amazing, but under the hood it is just another column, and it equals to ``df("visits").>(100)``, where, thanks to Scala paradigm ``>`` is just another function that you can define.


```scala

val sms = df.select($"agency", $"platform", $"visits").filter($"platform" === "SMS")
sms.show() // Ctrl+Enter

```



Again you could have written the query above using any column aliases or String names or even writing the query directly. 

For example, we can do it using String names, as follows:


```scala

// Ctrl+Enter Note that we are using "platform = 'SMS'" since it will be evaluated as actual SQL
val sms = df.select(df("agency"), df("platform"), df("visits")).filter("platform = 'SMS'")
sms.show(5)

```


 
Refer to the [DataFrame API](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) for more detailed API. In addition to simple column references and expressions, DataFrames also have a rich library of functions including string manipulation, date
arithmetic, common math operations and more. The complete list is available in the [DataFrame Function Reference](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$).





Let's next explore some of the functionality that is available by transforming this DataFrame `df` into a new DataFrame called `fixedDF`.

* First, note that some columns are not exactly what we want them to be. 
  * For example date column should be standard Date/Timestamp SQL column, and 
  * visits should not contain null values, but `0`s instead. 
  
* Let us fix it using some code that is briefly explained here (don't worry if you don't get it completely now, you will get the hang of it by playing more)
    * The `coalesce` function is similar to `if-else` statement, i.e., if first column in expression is `null`, then the value of the second column is used and so on. 
    * `lit` just means column of constant value (`lit`erally speaking!).
    * the "funky" time conversion is essentially conversion from current format -> unix timestamp as a number -> Spark SQL Date format
    * we also remove `TOTAL` value from `platform` column.


```scala

// Ctrl+Enter to make fixedDF

// import the needed sql functions
import org.apache.spark.sql.functions.{coalesce, from_unixtime, lit, to_date, unix_timestamp}

// make the fixedDF DataFrame
val fixedDF = df.
   select(
     $"agency", 
     $"platform", 
     $"url", 
     to_date(from_unixtime(unix_timestamp($"date", "MM/dd/yyyy hh:mm:ss aaa"))).as("date"), 
     coalesce($"visits", lit(0)).as("visits")).
   filter($"platform" !== "TOTAL")

fixedDF.printSchema() // print its schema 
// and show the top 20 records fully
fixedDF.show(false) // the false argument does not truncate the rows, so you will not see something like this "anot..."

```



Okay, this is better, but `url`s are still inconsistent. 

Let's fix this by writing our own UDF (user-defined function) to deal with special cases. 

Note that if you **CAN USE Spark functions library**, do it. But for the sake of the example, custom UDF is shown below.

We take value of a column as String type and return the same String type, but ignore values that do not start with `http`.


```scala

// Ctrl+Enter to evaluate this UDF which takes a input String called "value"
// and converts it into lower-case if it begins with http and otherwise leaves it as null, so we sort of remove non valid web-urls
val cleanUrl = udf((value: String) => if (value.startsWith("http")) {
  value.toLowerCase()
} else {
  null
})

```



Let us apply our UDF on `fixedDF` to create a new DataFrame called `cleanedDF` as follows:


```scala

// Ctrl+Enter
val cleanedDF = fixedDF.select($"agency", $"platform", cleanUrl($"url").as("url"), $"date", $"visits")

```



Now, let's check that it actually worked by seeing the first 5 rows of the `cleanedDF` whose `url` `isNull` and `isNotNull`, as follows:


```scala

// Shift+Enter
cleanedDF.filter($"url".isNull).show(5)

```
```scala

// Ctrl+Enter
cleanedDF.filter($"url".isNotNull).show(5, false) // false in .show(5, false) shows rows untruncated

```



Now there is a suggestion from you manager's manager's manager that due to some perceived privacy concerns we want to replace `agency` with some unique identifier.

So we need to do the following:
* create unique list of agencies with ids and 
* join them with main DataFrame. 

Sounds easy, right? Let's do it.


```scala

// Crtl+Enter
// Import Spark SQL function that will give us unique id across all the records in this DataFrame
import org.apache.spark.sql.functions.monotonicallyIncreasingId

// We append column as SQL function that creates unique ids across all records in DataFrames 
val agencies = cleanedDF.select(cleanedDF("agency"))
                        .distinct()
                        .withColumn("id", monotonicallyIncreasingId())
agencies.show(5)

```



Those who want to understand left/right inner/outer joins can see the [video lectures in Module 3 of Anthony Joseph's Introduction to Big data edX course](/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x/Module 3: Lectures) from the Community Edition of databricks.  The course has been added to this databricks shard at [/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x](/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x) as extra resources for the project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/).


```scala

// Ctrl+Enter
// And join with the rest of the data, note how join condition is specified 
val anonym = cleanedDF.join(agencies, cleanedDF("agency") === agencies("agency"), "inner").select("id", "platform", "url", "date", "visits")

// We also cache DataFrame since it can be quite expensive to recompute join
anonym.cache()

// Display result
anonym.show(5)

```
```scala

sqlContext.tables.show() // look at the available tables

```
```scala

%sql 
-- to remove a TempTable if it exists already
drop table anonym 

```
```scala

// Register table for Spark SQL, we also import "month" function 
import org.apache.spark.sql.functions.month

anonym.registerTempTable("anonym")

```
```scala

%sql
-- Interesting. Now let's do some aggregation. Display platform, month, visits
-- Date column allows us to extract month directly

select platform, month(date) as month, sum(visits) as visits from anonym group by platform, month(date)

```



> Note, that we could have done aggregation using DataFrame API instead of Spark SQL.





Alright, now let's see some *cool* operations with window functions. 

Our next task is to compute ``(daily visits / monthly average)`` for all platforms.


```scala

import org.apache.spark.sql.functions.{dayofmonth, month, row_number, sum}
import org.apache.spark.sql.expressions.Window

val coolDF = anonym.select($"id", $"platform", dayofmonth($"date").as("day"), month($"date").as("month"), $"visits").
  groupBy($"id", $"platform", $"day", $"month").agg(sum("visits").as("visits"))

// Run window aggregation on visits per month and platform
val window = coolDF.select($"id", $"day", $"visits", sum($"visits").over(Window.partitionBy("platform", "month")).as("monthly_visits"))

// Create and register percent table
val percent = window.select($"id", $"day", ($"visits" / $"monthly_visits").as("percent"))

percent.registerTempTable("percent")

```
```scala

%sql

-- A little bit of visualization as result of our efforts
select id, day, `percent` from percent where `percent` > 0.3 and day = 2

```
```scala

%sql
-- You also could just use plain SQL to write query above, note that you might need to group by id and day as well.
with aggr as (
  select id, dayofmonth(date) as day, visits / sum(visits) over (partition by (platform, month(date))) as percent
  from anonym
)
select * from aggr where day = 2 and percent > 0.3

```


 
## Interoperating with RDDs

Spark SQL supports two different methods for converting existing RDDs into DataFrames. The first method uses reflection to infer the schema of an RDD that contains specific types of objects. This reflection based approach leads to more concise code and works well when you already know the schema. 

The second method for creating DataFrames is through a programmatic interface that allows you to construct a schema and then apply it to an
existing RDD. While this method is more verbose, it allows you to construct DataFrames when the columns and their types are not known until runtime.

### Inferring the Schema Using Reflection

The Scala interface for Spark SQL supports automatically converting an RDD containing case classes to a DataFrame. The case class defines the
schema of the table. The names of the arguments to the case class are read using reflection and become the names of the columns. Case classes
can also be nested or contain complex types such as Sequences or Arrays. This RDD can be implicitly converted to a DataFrame and then be registered as a table.


```scala

// Define case class that will be our schema for DataFrame
case class Hubot(name: String, year: Int, manufacturer: String, version: Array[Int], details: Map[String, String])

// You can process a text file, for example, to convert every row to our Hubot, but we will create RDD manually
val rdd = sc.parallelize(
  Array(
    Hubot("Jerry", 2015, "LCorp", Array(1, 2, 3), Map("eat" -> "yes", "sleep" -> "yes", "drink" -> "yes")),
    Hubot("Mozart", 2010, "LCorp", Array(1, 2), Map("eat" -> "no", "sleep" -> "no", "drink" -> "no")),
    Hubot("Einstein", 2012, "LCorp", Array(1, 2, 3), Map("eat" -> "yes", "sleep" -> "yes", "drink" -> "no"))
  )
)

```
```scala

// In order to convert RDD into DataFrame you need to do this:
val hubots = rdd.toDF()

// Display DataFrame, note how array and map fields are displayed
hubots.printSchema()
hubots.show()

```
```scala

// You can query complex type the same as you query any other column
// By the way you can use `sql` function to invoke Spark SQL to create DataFrame
hubots.registerTempTable("hubots")

val onesThatEat = sqlContext.sql("select name, details.eat from hubots where details.eat = 'yes'")

onesThatEat.show()

```



### Programmatically Specifying the Schema

When case classes cannot be defined ahead of time (for example, the structure of records is encoded in a string, or a text dataset will be parsed and fields will be projected differently for different users), a `DataFrame` can be created programmatically with three steps.

1.  Create an RDD of `Row`s from the original RDD
2.  Create the schema represented by a [StructType](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.types.StructType) and [StructField](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.types.StructField) classes matching the structure of `Row`s in the RDD created in Step 1.
3.  Apply the schema to the RDD of `Row`s via `createDataFrame` method provided by `SQLContext`.


```scala

import org.apache.spark.sql.types._

// Let's say we have an RDD of String and we need to convert it into a DataFrame with schema "name", "year", and "manufacturer"
// As you can see every record is space-separated.
val rdd = sc.parallelize(
  Array(
    "Jerry 2015 LCorp",
    "Mozart 2010 LCorp",
    "Einstein 2012 LCorp"
  )
)

// Create schema as StructType //
val schema = StructType(
  StructField("name", StringType, false) :: 
  StructField("year", IntegerType, false) :: 
  StructField("manufacturer", StringType, false) :: 
  Nil
)

// Prepare RDD[Row]
val rows = rdd.map { entry => 
  val arr = entry.split("\\s+")
  val name = arr(0)
  val year = arr(1).toInt
  val manufacturer = arr(2)
  
  Row(name, year, manufacturer)
}

// Create DataFrame
val bots = sqlContext.createDataFrame(rows, schema)
bots.printSchema()
bots.show()

```


 
## Creating Datasets
A [Dataset](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) is a strongly-typed, immutable collection of objects that are mapped to a relational schema. At the core of the Dataset API is a new concept called an [encoder](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Encoder), which is responsible for converting between JVM objects and tabular representation. The tabular representation is stored using Spark’s internal Tungsten binary format, allowing for operations on serialized data and improved memory utilization.  Spark 1.6 comes with support for automatically generating encoders for a wide variety of types, including primitive types (e.g. String, Integer, Long), and Scala case classes.

> Simply put, you will get all the benefits of DataFrames with fair amount of flexibility of RDD API.


```scala

// We can start working with Datasets by using our "hubots" table

// To create Dataset from DataFrame do this (assuming that case class Hubot exists):
val ds = hubots.as[Hubot]
ds.show()


```



> **Side-note:** Although, currently DataFrames and Datasets are separate classes in Spark, idiomatically DataFrame is just a special Dataset of a case class Row, and this is how it will be in the future releases of Spark > 1.6. Dataset API is considered experimental in Spark 1.6.





## Finally
DataFrames and Datasets can simplify and improve most of the applications pipelines by bringing concise syntax and performance optimizations. We would highly recommend you to check out the official API documentation, specifically around 
* [DataFrame API](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame), 
* [Spark SQL functions library](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$), 
* [GroupBy clause and aggregated functions](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.GroupedData). 

Unfortunately, this is just _a getting started quickly_ course, and we skip features like custom aggregations, types, pivoting, etc., but if you are keen to know then start from the links above and this notebook and others in this directory.





## Appendix
### How to download data and make a table

Okay, so how did we actually make table "social_media_usage"? Databricks allows us to upload/link external data and make it available as registerd SQL table. It involves several steps:
1. Find interesting set of data - Google can be your friend for most cases here, or you can have your own dataset as CSV file, for example. Good source of data can also be found here: http://www.data.gov/ 
2. Download / prepare it to be either on S3, or human-readable format like CSV, or JSON
3. Go to Databricks cloud (where you log in to use Databricks notebooks) and open tab **Tables**
4. On the very top of the left sub-menu you will see button **+ Create table**, click on it
5. You will see page with drop-down menu of the list of sources you can provide, **File** means any file (Parquet, Avro, CSV), but it works the best with CSV format
6. Once you have chosen file and loaded it, you can change column names, or tweak types (mainly for CSV format)
7. That is it. Just click on final button to create table. After that you can refer to the table using ``sqlContext.table("YOUR_TABLE_NAME")``






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
