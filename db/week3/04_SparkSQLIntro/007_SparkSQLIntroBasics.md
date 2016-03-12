// Databricks notebook source exported at Sat, 12 Mar 2016 19:57:26 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





#Introduction to Spark SQL
* This notebook explains the motivation behind Spark SQL
* It introduces interactive SparkSQL queries and visualizations
* This notebook uses content from Databricks SparkSQL notebook and [SparkSQL programming guide](http://spark.apache.org/docs/latest/sql-programming-guide.html)





Some resources on SQL
* [https://en.wikipedia.org/wiki/SQL](https://en.wikipedia.org/wiki/SQL)
* [https://en.wikipedia.org/wiki/Apache_Hive](https://en.wikipedia.org/wiki/Apache_Hive)
* [http://www.infoq.com/articles/apache-spark-sql](http://www.infoq.com/articles/apache-spark-sql)
* [https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html](https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html)
* **READ**: [https://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf](https://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf)

Some of them are embedded below in-place for your convenience.


```scala

%run "/scalable-data-science/xtraResources/support/sdsFunctions"

```
```scala

displayHTML(frameIt("https://en.wikipedia.org/wiki/SQL",500))

```
```scala

displayHTML(frameIt("https://en.wikipedia.org/wiki/Apache_Hive#HiveQL",175))

```
```scala

displayHTML(frameIt("https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html",600))

```



This is an elaboration of the [Apache Spark 1.6 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).

# Getting Started

## Starting Point: SQLContext

The entry point into all functionality in Spark SQL is 
* the [`SQLContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SQLContext) class, 
* or one of its descendants, e.g. [`HiveContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.hive.HiveContext). 

To create a basic `SQLContext`, all you need is a `SparkContext`. 

Conveniently, in Databricks notebook (similar to `spark-shell`) `SQLContext` is already created for you and is available as `sqlContext`.


```scala

// Cntrl+Enter will print sqlContext available in notebook
sqlContext

```


 
**Deeper Dive:** (beginners skip for now)

Usually, when building application (running on production-like on-premises cluster) you would manually create `SQLContext` using something like this:
```scala
// An existing SparkContext
val sc: SparkContext = ...
val sqlContext = new org.apache.spark.sql.SQLContext(sc)

// This is used to implicitly convert an RDD to a DataFrame (see examples below)
import sqlContext.implicits._
```

Note that SQLContext in notebook is actually HiveContext. The difference is that HiveContext provides richer functionality over standard SQLContext, e.g. window functions were only available with HiveContext up to Spark 1.5.2, or usage of Hive user-defined functions. This originates from the fact that Spark SQL parser was built based on HiveQL parser, so only HiveContext was supporting full HiveQL syntax. Now it is changing, and SQLContext supports most of the functionality of the descendant (window functions should be available in SQLContext in 1.6+).

> Note that you do not need Hive installation when working with SQLContext or HiveContext, Spark comes with built-in derby datastore.





## Creating DataFrames

With a [`SQLContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SQLContext), applications can create [`DataFrame`](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) 
* from an existing `RDD`, 
* from a Hive table, or 
* from various other data sources.

#### Just to recap: 
* A DataFrame is a distributed collection of data organized into named columns. 
* You can think of it as being organized into table RDD of case class `Row` (which is not exactly true). 
* DataFrames, in comparison to RDDs, are backed by rich optimizations, including:
  * tracking their own schema, 
  * adaptive query execution, 
  * code generation including whole stage codegen, 
  * extensible Catalyst optimizer, and 
  * project [Tungsten](https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html). 

> Note that performance for DataFrames is the same across languages Scala, Java, Python, and R. This is due to the fact that the only planning phase is language-specific (logical + physical SQL plan), not the actual execution of the SQL plan.

![DF speed across languages](https://databricks.com/wp-content/uploads/2015/02/Screen-Shot-2015-02-16-at-9.46.39-AM-1024x457.png)





## DataFrame Basics

#### 1. An empty DataFrame
#### 2. DataFrame from a range
#### 3. DataFrame from an RDD
#### 4. DataFrame from a table





#### 1. Making an empty DataFrame
Spark has some of the pre-built methods to create simple DataFrames
* let us make an Empty DataFrame


```scala

val emptyDF = sqlContext.emptyDataFrame // Ctrl+Enter to make an empty DataFrame

```



Not really interesting, or is it?

**You Try!**

Put your cursor after `emptyDF.` below and hit Tab to see what can be done with `emptyDF`.


```scala

emptyDF.

```



#### 2. Making a DataFrame from a range
Let us make a DataFrame next
* from a range of numbers, as follows:


```scala

val rangeDF = sqlContext.range(0, 3) // Ctrl+Enter to make DataFrame with 0,1,2

```



Note that Spark automatically names column as `id` and casts integers to type `bigint` for big integer or Long.

In order to get a preview of data in DataFrame use `show()` as follows:


```scala

rangeDF.show() // Ctrl+Enter

```



#### 3. Making a DataFrame from an RDD

* Make an RDD
* Conver the RDD into a DataFrame using the defualt `.toDF()` method
* Conver the RDD into a DataFrame using the non-default `.toDF(...)` method
* Do it all in one line





Let's first make an RDD using the `sc.parallelize` method, transform it by a `map` and perform the `collect` action to display it, as follows:


```scala

val rdd1 = sc.parallelize(1 to 5).map(i => (i, i*2))
rdd1.collect() // Ctrl+Enter

```



Next, let us convert the RDD into DataFrame using the `.toDF()` method, as follows:


```scala

val df1 = rdd1.toDF() // Ctrl+Enter 

```



As it is clear, the DataFrame has columns named `_1` and `_2`, each of type `int`.  Let us see its content using the `.show()` method next.


```scala

df1.show() // Ctrl+Enter

```



Note that by default, i.e. without specifying any options as in `toDF()`, the column names are given by `_1` and `_2`.

We can easily specify column names as follows:


```scala

val df1 = rdd1.toDF("single", "double") // Ctrl+Enter
df1.show()

```



Of course, we can do all of the above steps to make the DataFrame `df1` in one line and then show it, as follows:


```scala

val df1 = sc.parallelize(1 to 5).map(i => (i, i*2)).toDF("single", "double") //Ctrl+enter
df1.show()

```



**You Try!**

Fill in the `???` below to get a DataFrame `df2` whose first two columns are the same as `df1` and whose third column named triple has values that are three times the values in the first column.


```scala

val df2 = sc.parallelize(1 to 5).map(i => (i, i*2, ???)).toDF("single", "double", "triple") // Ctrl+enter after editing ???
df2.show()

```



#### 4. Making a DataFrame from a table
We can load existing tables as DataFrames.  We will later see how to create tables from RDDs or other sources of raw data, including csv files, etc.

First let's see what tables are available to us.


```scala

sqlContext.tables.show() // Ctrl+Enter to see available tables

```



Let us load the table with `tableName` `diamonds` as a DataFrame (assuming it exixts!, if not don't worry as diamonds is our next stop!), as follows:


```scala

val diamondsDF = sqlContext.table("diamonds") // Shift+Enter

```
```scala

display(diamondsDF)

```



### 5. Using SQL for interactively querying a table is very powerful!
Note `-- comments` are how you add `comments` in SQL cells beginning with `%sql`.

* You can run SQL `select *` statement to see all columns of the table, as follows:
  * This is equivalent to the above `display(diamondsDF)' with the DataFrame


```scala

%sql
-- Ctrl+Enter to select all columns of the table
select * from diamonds

```



***
***

## Next we will play with data

The data here is **semi-structured tabular data** (Tab-delimited text file in dbfs).
Let us see what Anthony Joseph in BerkeleyX/CS100.1x had to say about such data.

### Key Data Management Concepts: Semi-Structured Tabular Data

**(watch now 1:26)**:

[![Semi-Structured Tabular Data by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/G_67yUxdDbU/0.jpg)](https://www.youtube.com/v/G_67yUxdDbU?rel=0&autoplay=1&modestbranding=1&start=1)

***






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
