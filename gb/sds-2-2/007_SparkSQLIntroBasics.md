[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Introduction to Spark SQL
=========================

-   This notebook explains the motivation behind Spark SQL
-   It introduces interactive SparkSQL queries and visualizations
-   This notebook uses content from Databricks SparkSQL notebook and [SparkSQL programming guide](http://spark.apache.org/docs/latest/sql-programming-guide.html)

### Some resources on SQL

-   <https://en.wikipedia.org/wiki/SQL>
-   <https://en.wikipedia.org/wiki/Apache_Hive>
-   <http://www.infoq.com/articles/apache-spark-sql>
-   <https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html>
-   <https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html>
-   **READ**: [https://people.csail.mit.edu/matei/papers/2015/sigmod*spark*sql.pdf](https://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf)

Some of them are embedded below in-place for your convenience.

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/SQL"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Apache_Hive#HiveQL"
 width="95%" height="175"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/ml-features.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

<p class="htmlSandbox"><iframe 
 src="https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html"
 width="95%" height="600"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

<p class="htmlSandbox"><iframe 
 src="https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html"
 width="95%" height="600"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

This is an elaboration of the [Apache Spark 2.2 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).

Overview
========

Spark SQL is a Spark module for structured data processing. Unlike the basic Spark RDD API, the interfaces provided by Spark SQL provide Spark with more information about the structure of both the data and the computation being performed. Internally, Spark SQL uses this extra information to perform extra optimizations. There are several ways to interact with Spark SQL including SQL and the Dataset API. When computing a result the same execution engine is used, independent of which API/language you are using to express the computation. This unification means that developers can easily switch back and forth between different APIs based on which provides the most natural way to express a given transformation.

All of the examples on this page use sample data included in the Spark distribution and can be run in the spark-shell, pyspark shell, or sparkR shell.

Datasets and DataFrames
=======================

A Dataset is a distributed collection of data. Dataset is a new interface added in Spark 1.6 that provides the benefits of RDDs (strong typing, ability to use powerful lambda functions) with the benefits of Spark SQL’s optimized execution engine. A Dataset can be [constructed](http://spark.apache.org/docs/latest/sql-programming-guide.html#creating-datasets) from JVM objects and then manipulated using functional transformations (map, flatMap, filter, etc.). The Dataset API is available in [Scala](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) and [Java](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html). Python does not have the support for the Dataset API. But due to Python’s dynamic nature, many of the benefits of the Dataset API are already available (i.e. you can access the field of a row by name naturally `row.columnName`). The case for R is similar.

A DataFrame is a Dataset organized into named columns. It is conceptually equivalent to a table in a relational database or a data frame in R/Python, but with richer optimizations under the hood. DataFrames can be constructed from a wide array of [sources](http://spark.apache.org/docs/latest/sql-programming-guide.html#data-sources) such as: structured data files, tables in Hive, external databases, or existing RDDs. The DataFrame API is available in Scala, Java, [Python](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame), and [R](http://spark.apache.org/docs/latest/api/R/index.html). In Scala and Java, a DataFrame is represented by a Dataset of Rows. In the [Scala API](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset), DataFrame is simply a type alias of Dataset\[Row\]. While, in Java API, users need to use `Dataset<Row>` to represent a DataFrame.

Throughout this document, we will often refer to Scala/Java Datasets of `Rows` as DataFrames.

Getting Started in Spark 2.x
============================

Starting Point: SparkSession
----------------------------

The entry point into all functionality in Spark is the [SparkSession](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SparkSession). To create a basic SparkSession in your scala Spark code, just use `SparkSession.builder()`:

\`\`\`
import org.apache.spark.sql.SparkSession

val spark = SparkSession
.builder()
.appName("Spark SQL basic example")
.config("spark.some.config.option", "some-value")
.getOrCreate()

// For implicit conversions like converting RDDs to DataFrames
import spark.implicits.\_
\`\`\`

Conveniently, in Databricks notebook (similar to `spark-shell`) `SparkSession` is already created for you and is available as `spark`.

``` scala
spark // ready-made Spark-Session
```

>     res6: org.apache.spark.sql.SparkSession = org.apache.spark.sql.SparkSession@70de6a8

Creating DataFrames
-------------------

With a [`SparkSession`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SparkSession) or [`SQLContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SQLContext), applications can create [`DataFrame`](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame)

-   from an existing `RDD`,
-   from a Hive table, or
-   from various other data sources.

#### Just to recap:

-   A DataFrame is a distributed collection of data organized into named columns (it is not strogly typed).
-   You can think of it as being organized into table RDD of case class `Row` (which is not exactly true).
-   DataFrames, in comparison to RDDs, are backed by rich optimizations, including:
    -   tracking their own schema,
    -   adaptive query execution,
    -   code generation including whole stage codegen,
    -   extensible Catalyst optimizer, and
    -   project [Tungsten](https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html) for optimized storage.

> Note that performance for DataFrames is the same across languages Scala, Java, Python, and R. This is due to the fact that the only planning phase is language-specific (logical + physical SQL plan), not the actual execution of the SQL plan.

![DF speed across languages](https://databricks.com/wp-content/uploads/2015/02/Screen-Shot-2015-02-16-at-9.46.39-AM-1024x457.png)

DataFrame Basics
----------------

#### 1. An empty DataFrame

#### 2. DataFrame from a range

#### 3. DataFrame from an RDD

#### 4. DataFrame Operations (aka Untyped Dataset Operations)

#### 5. Running SQL Queries Programmatically

#### 6. Creating Datasets

### 1. Making an empty DataFrame

Spark has some of the pre-built methods to create simple DataFrames

-   let us make an Empty DataFrame

``` scala
val emptyDF = spark.emptyDataFrame // Ctrl+Enter to make an empty DataFrame
```

>     emptyDF: org.apache.spark.sql.DataFrame = []

Not really interesting, or is it?

**You Try!**

Uncomment the following cell, put your cursor after `emptyDF.` below and hit Tab to see what can be done with `emptyDF`.

``` scala
//emptyDF.
```

### 2. Making a DataFrame from a range

Let us make a DataFrame next

-   from a range of numbers, as follows:

``` scala
val rangeDF = spark.range(0, 3).toDF() // Ctrl+Enter to make DataFrame with 0,1,2
```

>     rangeDF: org.apache.spark.sql.DataFrame = [id: bigint]

Note that Spark automatically names column as `id` and casts integers to type `bigint` for big integer or Long.

In order to get a preview of data in DataFrame use `show()` as follows:

``` scala
rangeDF.show() // Ctrl+Enter
```

>     +---+
>     | id|
>     +---+
>     |  0|
>     |  1|
>     |  2|
>     +---+

### 3. Making a DataFrame from an RDD

-   Make an RDD
-   Conver the RDD into a DataFrame using the defualt `.toDF()` method
-   Conver the RDD into a DataFrame using the non-default `.toDF(...)` method
-   Do it all in one line

Let's first make an RDD using the `sc.parallelize` method, transform it by a `map` and perform the `collect` action to display it, as follows:

``` scala
val rdd1 = sc.parallelize(1 to 5).map(i => (i, i*2))
rdd1.collect() // Ctrl+Enter
```

>     rdd1: org.apache.spark.rdd.RDD[(Int, Int)] = MapPartitionsRDD[358618] at map at <console>:34
>     res8: Array[(Int, Int)] = Array((1,2), (2,4), (3,6), (4,8), (5,10))

Next, let us convert the RDD into DataFrame using the `.toDF()` method, as follows:

``` scala
val df1 = rdd1.toDF() // Ctrl+Enter 
```

>     df1: org.apache.spark.sql.DataFrame = [_1: int, _2: int]

As it is clear, the DataFrame has columns named `_1` and `_2`, each of type `int`. Let us see its content using the `.show()` method next.

``` scala
df1.show() // Ctrl+Enter
```

>     +---+---+
>     | _1| _2|
>     +---+---+
>     |  1|  2|
>     |  2|  4|
>     |  3|  6|
>     |  4|  8|
>     |  5| 10|
>     +---+---+

Note that by default, i.e. without specifying any options as in `toDF()`, the column names are given by `_1` and `_2`.

We can easily specify column names as follows:

``` scala
val df1 = rdd1.toDF("once", "twice") // Ctrl+Enter
df1.show()
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   1|    2|
>     |   2|    4|
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+
>
>     df1: org.apache.spark.sql.DataFrame = [once: int, twice: int]

Of course, we can do all of the above steps to make the DataFrame `df1` in one line and then show it, as follows:

``` scala
val df1 = sc.parallelize(1 to 5).map(i => (i, i*2)).toDF("once", "twice") //Ctrl+enter
df1.show()
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   1|    2|
>     |   2|    4|
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+
>
>     df1: org.apache.spark.sql.DataFrame = [once: int, twice: int]

### 4. DataFrame Operations (aka Untyped Dataset Operations)

DataFrames provide a domain-specific language for structured data manipulation in [Scala](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset), [Java](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html), [Python](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame) and [R](http://spark.apache.org/docs/latest/api/R/SparkDataFrame.html).

As mentioned above, in Spark 2.0, DataFrames are just Dataset of Rows in Scala and Java API. These operations are also referred as “untyped transformations” in contrast to “typed transformations” come with strongly typed Scala/Java Datasets.

Here we include some basic examples of structured data processing using Datasets:

``` scala
// This import is needed to use the $-notation
import spark.implicits._
// Print the schema in a tree format
df1.printSchema()
```

>     root
>      |-- once: integer (nullable = false)
>      |-- twice: integer (nullable = false)
>
>     import spark.implicits._

``` scala
// Select only the "name" column
df1.select("once").show()
```

>     +----+
>     |once|
>     +----+
>     |   1|
>     |   2|
>     |   3|
>     |   4|
>     |   5|
>     +----+

``` scala
// Select both columns, but increment the double column by 1
df1.select($"once", $"once" + 1).show()
```

>     +----+----------+
>     |once|(once + 1)|
>     +----+----------+
>     |   1|         2|
>     |   2|         3|
>     |   3|         4|
>     |   4|         5|
>     |   5|         6|
>     +----+----------+

``` scala
// Select both columns, but increment the double column by 1 and rename it as "oncemore"
df1.select($"once", ($"once" + 1).as("oncemore")).show()
```

>     +----+--------+
>     |once|oncemore|
>     +----+--------+
>     |   1|       2|
>     |   2|       3|
>     |   3|       4|
>     |   4|       5|
>     |   5|       6|
>     +----+--------+

``` scala
df1.filter($"once" > 2).show()
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+

``` scala
// Count the number of distinct singles -  a bit boring
df1.groupBy("once").count().show()
```

>     +----+-----+
>     |once|count|
>     +----+-----+
>     |   1|    1|
>     |   3|    1|
>     |   5|    1|
>     |   4|    1|
>     |   2|    1|
>     +----+-----+

Let's make a more interesting DataFrame for `groupBy` with repeated elements so that the `count` will be more than `1`.

``` scala
df1.show()
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   1|    2|
>     |   2|    4|
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+

``` scala
val df11 = sc.parallelize(3 to 5).map(i => (i, i*2)).toDF("once", "twice") // just make a small one
df11.show()
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+
>
>     df11: org.apache.spark.sql.DataFrame = [once: int, twice: int]

``` scala
val df111 = df1.union(df11) // let's take the unionAll of df1 and df11 into df111
df111.show() // df111 is obtained by simply appending the rows of df11 to df1
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   1|    2|
>     |   2|    4|
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+
>
>     df111: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [once: int, twice: int]

``` scala
// Count the number of distinct singles -  a bit less boring
df111.groupBy("once").count().show()
```

>     +----+-----+
>     |once|count|
>     +----+-----+
>     |   1|    1|
>     |   3|    2|
>     |   5|    2|
>     |   4|    2|
>     |   2|    1|
>     +----+-----+

For a complete list of the types of operations that can be performed on a Dataset refer to the [API Documentation](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset).

In addition to simple column references and expressions, Datasets also have a rich library of functions including string manipulation, date arithmetic, common math operations and more. The complete list is available in the [DataFrame Function Reference](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$).

**You Try!**

Uncomment the two lines in the next cell, and then fill in the `???` below to get a DataFrame `df2` whose first two columns are the same as `df1` and whose third column named triple has values that are three times the values in the first column.

``` scala
//val df2 = sc.parallelize(1 to 5).map(i => (i, i*2, ???)).toDF("single", "double", "triple") // Ctrl+enter after editing ???
//df2.show()
```

### 5. Running SQL Queries Programmatically

The `sql` function on a `SparkSession` enables applications to run SQL queries programmatically and returns the result as a `DataFrame`.

``` scala
// Register the DataFrame as a SQL temporary view
df1.createOrReplaceTempView("SDTable")

val sqlDF = spark.sql("SELECT * FROM SDTable")
sqlDF.show()
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   1|    2|
>     |   2|    4|
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+
>
>     sqlDF: org.apache.spark.sql.DataFrame = [once: int, twice: int]

``` scala
spark.sql("SELECT * FROM SDTable WHERE once>2").show()
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+

### 5. Using SQL for interactively querying a table is very powerful!

Note `-- comments` are how you add `comments` in SQL cells beginning with `%sql`.

-   You can run SQL `select *` statement to see all columns of the table, as follows:
    -   This is equivalent to the above \`display(diamondsDF)' with the DataFrame

``` sql
-- Ctrl+Enter to select all columns of the table
select * from SDTable
```

| once | twice |
|------|-------|
| 1.0  | 2.0   |
| 2.0  | 4.0   |
| 3.0  | 6.0   |
| 4.0  | 8.0   |
| 5.0  | 10.0  |

``` sql
-- Ctrl+Enter to select all columns of the table
-- note table names of registered tables are case-insensitive
select * from sdtable
```

| once | twice |
|------|-------|
| 1.0  | 2.0   |
| 2.0  | 4.0   |
| 3.0  | 6.0   |
| 4.0  | 8.0   |
| 5.0  | 10.0  |

#### Global Temporary View

Temporary views in Spark SQL are session-scoped and will disappear if the session that creates it terminates. If you want to have a temporary view that is shared among all sessions and keep alive until the Spark application terminates, you can create a global temporary view. Global temporary view is tied to a system preserved database `global_temp`, and we must use the qualified name to refer it, e.g. `SELECT * FROM global_temp.view1`. See <http://spark.apache.org/docs/latest/sql-programming-guide.html#global-temporary-view> for details.

6. Creating Datasets
--------------------

Datasets are similar to RDDs, however, instead of using Java serialization or Kryo they use a specialized [Encoder](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Encoder) to serialize the objects for processing or transmitting over the network. While both encoders and standard serialization are responsible for turning an object into bytes, encoders are code generated dynamically and use a format that allows Spark to perform many operations like filtering, sorting and hashing without deserializing the bytes back into an object.

``` scala
val rangeDS = spark.range(0, 3) // Ctrl+Enter to make DataSet with 0,1,2; Note we added '.toDF()' to this to create a DataFrame
```

>     rangeDS: org.apache.spark.sql.Dataset[Long] = [id: bigint]

``` scala
rangeDS.show() // the column name 'id' is made by default here
```

>     +---+
>     | id|
>     +---+
>     |  0|
>     |  1|
>     |  2|
>     +---+

We can have more complicated objects in a `DataSet` too.

``` scala
// Note: Case classes in Scala 2.10 can support only up to 22 fields. To work around this limit,
// you can use custom classes that implement the Product interface
case class Person(name: String, age: Long)

// Encoders are created for case classes
val caseClassDS = Seq(Person("Andy", 32), Person("Erik",44), Person("Anna", 15)).toDS()
caseClassDS.show()
```

>     +----+---+
>     |name|age|
>     +----+---+
>     |Andy| 32|
>     |Erik| 44|
>     |Anna| 15|
>     +----+---+
>
>     defined class Person
>     caseClassDS: org.apache.spark.sql.Dataset[Person] = [name: string, age: bigint]

``` scala
// Encoders for most common types are automatically provided by importing spark.implicits._
val primitiveDS = Seq(1, 2, 3).toDS()
primitiveDS.map(_ + 1).collect() // Returns: Array(2, 3, 4)
```

>     primitiveDS: org.apache.spark.sql.Dataset[Int] = [value: int]
>     res80: Array[Int] = Array(2, 3, 4)

``` scala
df1
```

>     res81: org.apache.spark.sql.DataFrame = [once: int, twice: int]

``` scala
df1.show
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   1|    2|
>     |   2|    4|
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+

``` scala
// let's make a case class for our DF so we can convert it to Dataset
case class singleAndDoubleIntegers(once: Integer, twice: Integer)
```

>     defined class singleAndDoubleIntegers

``` scala
val ds1 = df1.as[singleAndDoubleIntegers]
```

>     ds1: org.apache.spark.sql.Dataset[singleAndDoubleIntegers] = [once: int, twice: int]

``` scala
ds1.show()
```

>     +----+-----+
>     |once|twice|
>     +----+-----+
>     |   1|    2|
>     |   2|    4|
>     |   3|    6|
>     |   4|    8|
>     |   5|   10|
>     +----+-----+

------------------------------------------------------------------------

------------------------------------------------------------------------

Next we will play with data
---------------------------

The data here is **semi-structured tabular data** (Tab-delimited text file in dbfs).
Let us see what Anthony Joseph in BerkeleyX/CS100.1x had to say about such data.

### Key Data Management Concepts: Semi-Structured Tabular Data

**(watch now 1:26)**:

[![Semi-Structured Tabular Data by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/G_67yUxdDbU/0.jpg)](https://www.youtube.com/watch?v=G_67yUxdDbU?rel=0&autoplay=1&modestbranding=1&start=1)

------------------------------------------------------------------------

Go through the databricks Introductions Now
-------------------------------------------

-   <https://docs.databricks.com/spark/latest/dataframes-datasets/introduction-to-dataframes-scala.html>

-   <https://docs.databricks.com/spark/latest/dataframes-datasets/introduction-to-datasets.html>

### Recommended Homework

This week's recommended homework is a deep dive into the [SparkSQL programming guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).

### Recommended Extra-work

Those who want to understand SparkSQL functionalities in more detail can see:

-   [video lectures in Module 3 of Anthony Joseph's Introduction to Big Data edX course](https://docs.databricks.com/spark/1.6/training/introduction-to-big-data-cs100x-2015/module-3.html).