// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC #Introduction to Spark SQL
// MAGIC 
// MAGIC * This notebook explains the motivation behind Spark SQL
// MAGIC * It introduces interactive SparkSQL queries and visualizations
// MAGIC * This notebook uses content from Databricks SparkSQL notebook and [SparkSQL programming guide](http://spark.apache.org/docs/latest/sql-programming-guide.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Some resources on SQL
// MAGIC 
// MAGIC * [https://en.wikipedia.org/wiki/SQL](https://en.wikipedia.org/wiki/SQL)
// MAGIC * [https://en.wikipedia.org/wiki/Apache_Hive](https://en.wikipedia.org/wiki/Apache_Hive)
// MAGIC * [http://www.infoq.com/articles/apache-spark-sql](http://www.infoq.com/articles/apache-spark-sql)
// MAGIC * [https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html](https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html)
// MAGIC * [https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html](https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html)
// MAGIC * **READ**: [https://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf](https://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf)
// MAGIC 
// MAGIC Some of them are embedded below in-place for your convenience.

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://en.wikipedia.org/wiki/SQL",500))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Apache_Hive#HiveQL",175))

// COMMAND ----------

displayHTML(frameIt("https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html",600))

// COMMAND ----------

displayHTML(frameIt("https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html",600))

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [Apache Spark 2.2 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).
// MAGIC 
// MAGIC # Overview
// MAGIC 
// MAGIC Spark SQL is a Spark module for structured data processing. Unlike the basic Spark RDD API, the interfaces provided by Spark SQL provide Spark with more information about the structure of both the data and the computation being performed. Internally, Spark SQL uses this extra information to perform extra optimizations. There are several ways to interact with Spark SQL including SQL and the Dataset API. When computing a result the same execution engine is used, independent of which API/language you are using to express the computation. This unification means that developers can easily switch back and forth between different APIs based on which provides the most natural way to express a given transformation.
// MAGIC 
// MAGIC All of the examples on this page use sample data included in the Spark distribution and can be run in the spark-shell, pyspark shell, or sparkR shell.
// MAGIC 
// MAGIC # Datasets and DataFrames
// MAGIC A Dataset is a distributed collection of data. Dataset is a new interface added in Spark 1.6 that provides the benefits of RDDs (strong typing, ability to use powerful lambda functions) with the benefits of Spark SQL’s optimized execution engine. A Dataset can be [constructed](http://spark.apache.org/docs/latest/sql-programming-guide.html#creating-datasets) from JVM objects and then manipulated using functional transformations (map, flatMap, filter, etc.). The Dataset API is available in [Scala](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) and [Java](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html). Python does not have the support for the Dataset API. But due to Python’s dynamic nature, many of the benefits of the Dataset API are already available (i.e. you can access the field of a row by name naturally `row.columnName`). The case for R is similar.
// MAGIC 
// MAGIC A DataFrame is a Dataset organized into named columns. It is conceptually equivalent to a table in a relational database or a data frame in R/Python, but with richer optimizations under the hood. DataFrames can be constructed from a wide array of [sources](http://spark.apache.org/docs/latest/sql-programming-guide.html#data-sources) such as: structured data files, tables in Hive, external databases, or existing RDDs. The DataFrame API is available in Scala, Java, [Python](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame), and [R](http://spark.apache.org/docs/latest/api/R/index.html). In Scala and Java, a DataFrame is represented by a Dataset of Rows. In the [Scala API](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset), DataFrame is simply a type alias of Dataset[Row]. While, in Java API, users need to use `Dataset<Row>` to represent a DataFrame.
// MAGIC 
// MAGIC Throughout this document, we will often refer to Scala/Java Datasets of `Rows` as DataFrames.

// COMMAND ----------

// MAGIC %md
// MAGIC # Getting Started in Spark 2.x
// MAGIC 
// MAGIC ## Starting Point: SparkSession
// MAGIC 
// MAGIC The entry point into all functionality in Spark is the [SparkSession](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SparkSession). To create a basic SparkSession in your scala Spark code, just use `SparkSession.builder()`:
// MAGIC 
// MAGIC ```
// MAGIC import org.apache.spark.sql.SparkSession
// MAGIC 
// MAGIC val spark = SparkSession
// MAGIC   .builder()
// MAGIC   .appName("Spark SQL basic example")
// MAGIC   .config("spark.some.config.option", "some-value")
// MAGIC   .getOrCreate()
// MAGIC 
// MAGIC // For implicit conversions like converting RDDs to DataFrames
// MAGIC import spark.implicits._
// MAGIC ```
// MAGIC 
// MAGIC Conveniently, in Databricks notebook (similar to `spark-shell`) `SparkSession` is already created for you and is available as `spark`.

// COMMAND ----------

spark // ready-made Spark-Session

// COMMAND ----------

// MAGIC %md
// MAGIC ## Creating DataFrames
// MAGIC 
// MAGIC With a [`SparkSession`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SparkSession) or [`SQLContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SQLContext), applications can create [`DataFrame`](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) 
// MAGIC 
// MAGIC * from an existing `RDD`, 
// MAGIC * from a Hive table, or 
// MAGIC * from various other data sources.
// MAGIC 
// MAGIC #### Just to recap: 
// MAGIC 
// MAGIC * A DataFrame is a distributed collection of data organized into named columns (it is not strogly typed). 
// MAGIC * You can think of it as being organized into table RDD of case class `Row` (which is not exactly true). 
// MAGIC * DataFrames, in comparison to RDDs, are backed by rich optimizations, including:
// MAGIC   * tracking their own schema, 
// MAGIC   * adaptive query execution, 
// MAGIC   * code generation including whole stage codegen, 
// MAGIC   * extensible Catalyst optimizer, and 
// MAGIC   * project [Tungsten](https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html) for optimized storage. 
// MAGIC 
// MAGIC > Note that performance for DataFrames is the same across languages Scala, Java, Python, and R. This is due to the fact that the only planning phase is language-specific (logical + physical SQL plan), not the actual execution of the SQL plan.
// MAGIC 
// MAGIC ![DF speed across languages](https://databricks.com/wp-content/uploads/2015/02/Screen-Shot-2015-02-16-at-9.46.39-AM-1024x457.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ## DataFrame Basics
// MAGIC 
// MAGIC #### 1. An empty DataFrame
// MAGIC #### 2. DataFrame from a range
// MAGIC #### 3. DataFrame from an RDD
// MAGIC #### 4. DataFrame Operations (aka Untyped Dataset Operations)
// MAGIC #### 5. Running SQL Queries Programmatically
// MAGIC #### 6. Creating Datasets

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. Making an empty DataFrame
// MAGIC 
// MAGIC Spark has some of the pre-built methods to create simple DataFrames
// MAGIC 
// MAGIC * let us make an Empty DataFrame

// COMMAND ----------

val emptyDF = spark.emptyDataFrame // Ctrl+Enter to make an empty DataFrame

// COMMAND ----------

// MAGIC %md
// MAGIC Not really interesting, or is it?
// MAGIC 
// MAGIC **You Try!**
// MAGIC 
// MAGIC Uncomment the following cell, put your cursor after `emptyDF.` below and hit Tab to see what can be done with `emptyDF`.

// COMMAND ----------

//emptyDF.

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Making a DataFrame from a range
// MAGIC 
// MAGIC Let us make a DataFrame next
// MAGIC 
// MAGIC * from a range of numbers, as follows:

// COMMAND ----------

val rangeDF = spark.range(0, 3).toDF() // Ctrl+Enter to make DataFrame with 0,1,2

// COMMAND ----------

// MAGIC %md
// MAGIC Note that Spark automatically names column as `id` and casts integers to type `bigint` for big integer or Long.
// MAGIC 
// MAGIC In order to get a preview of data in DataFrame use `show()` as follows:

// COMMAND ----------

rangeDF.show() // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. Making a DataFrame from an RDD
// MAGIC 
// MAGIC * Make an RDD
// MAGIC * Conver the RDD into a DataFrame using the defualt `.toDF()` method
// MAGIC * Conver the RDD into a DataFrame using the non-default `.toDF(...)` method
// MAGIC * Do it all in one line

// COMMAND ----------

// MAGIC %md
// MAGIC Let's first make an RDD using the `sc.parallelize` method, transform it by a `map` and perform the `collect` action to display it, as follows:

// COMMAND ----------

val rdd1 = sc.parallelize(1 to 5).map(i => (i, i*2))
rdd1.collect() // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC Next, let us convert the RDD into DataFrame using the `.toDF()` method, as follows:

// COMMAND ----------

val df1 = rdd1.toDF() // Ctrl+Enter 

// COMMAND ----------

// MAGIC %md
// MAGIC As it is clear, the DataFrame has columns named `_1` and `_2`, each of type `int`.  Let us see its content using the `.show()` method next.

// COMMAND ----------

df1.show() // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC Note that by default, i.e. without specifying any options as in `toDF()`, the column names are given by `_1` and `_2`.
// MAGIC 
// MAGIC We can easily specify column names as follows:

// COMMAND ----------

val df1 = rdd1.toDF("once", "twice") // Ctrl+Enter
df1.show()

// COMMAND ----------

// MAGIC %md
// MAGIC Of course, we can do all of the above steps to make the DataFrame `df1` in one line and then show it, as follows:

// COMMAND ----------

val df1 = sc.parallelize(1 to 5).map(i => (i, i*2)).toDF("once", "twice") //Ctrl+enter
df1.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 4. DataFrame Operations (aka Untyped Dataset Operations)
// MAGIC 
// MAGIC DataFrames provide a domain-specific language for structured data manipulation in [Scala](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset), [Java](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html), [Python](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame) and [R](http://spark.apache.org/docs/latest/api/R/SparkDataFrame.html).
// MAGIC 
// MAGIC As mentioned above, in Spark 2.0, DataFrames are just Dataset of Rows in Scala and Java API. These operations are also referred as “untyped transformations” in contrast to “typed transformations” come with strongly typed Scala/Java Datasets.
// MAGIC 
// MAGIC Here we include some basic examples of structured data processing using Datasets:

// COMMAND ----------

// This import is needed to use the $-notation
import spark.implicits._
// Print the schema in a tree format
df1.printSchema()

// COMMAND ----------

// Select only the "name" column
df1.select("once").show()

// COMMAND ----------

// Select both columns, but increment the double column by 1
df1.select($"once", $"once" + 1).show()

// COMMAND ----------

// Select both columns, but increment the double column by 1 and rename it as "oncemore"
df1.select($"once", ($"once" + 1).as("oncemore")).show()

// COMMAND ----------

df1.filter($"once" > 2).show()

// COMMAND ----------

// Count the number of distinct singles -  a bit boring
df1.groupBy("once").count().show()

// COMMAND ----------

// MAGIC %md
// MAGIC Let's make a more interesting DataFrame for `groupBy` with repeated elements so that the `count` will be more than `1`.

// COMMAND ----------

df1.show()

// COMMAND ----------

val df11 = sc.parallelize(3 to 5).map(i => (i, i*2)).toDF("once", "twice") // just make a small one
df11.show()

// COMMAND ----------

val df111 = df1.union(df11) // let's take the unionAll of df1 and df11 into df111
df111.show() // df111 is obtained by simply appending the rows of df11 to df1

// COMMAND ----------

// Count the number of distinct singles -  a bit less boring
df111.groupBy("once").count().show()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC For a complete list of the types of operations that can be performed on a Dataset refer to the [API Documentation](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset).
// MAGIC 
// MAGIC In addition to simple column references and expressions, Datasets also have a rich library of functions including string manipulation, date arithmetic, common math operations and more. The complete list is available in the [DataFrame Function Reference](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$).

// COMMAND ----------

// MAGIC %md
// MAGIC **You Try!**
// MAGIC 
// MAGIC Uncomment the two lines in the next cell, and then fill in the `???` below to get a DataFrame `df2` whose first two columns are the same as `df1` and whose third column named triple has values that are three times the values in the first column.

// COMMAND ----------

//val df2 = sc.parallelize(1 to 5).map(i => (i, i*2, ???)).toDF("single", "double", "triple") // Ctrl+enter after editing ???
//df2.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5. Running SQL Queries Programmatically
// MAGIC 
// MAGIC The `sql` function on a `SparkSession` enables applications to run SQL queries programmatically and returns the result as a `DataFrame`.

// COMMAND ----------

// Register the DataFrame as a SQL temporary view
df1.createOrReplaceTempView("SDTable")

val sqlDF = spark.sql("SELECT * FROM SDTable")
sqlDF.show()

// COMMAND ----------

spark.sql("SELECT * FROM SDTable WHERE once>2").show()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5. Using SQL for interactively querying a table is very powerful!
// MAGIC 
// MAGIC Note `-- comments` are how you add `comments` in SQL cells beginning with `%sql`.
// MAGIC 
// MAGIC * You can run SQL `select *` statement to see all columns of the table, as follows:
// MAGIC   * This is equivalent to the above `display(diamondsDF)' with the DataFrame

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Ctrl+Enter to select all columns of the table
// MAGIC select * from SDTable

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Ctrl+Enter to select all columns of the table
// MAGIC -- note table names of registered tables are case-insensitive
// MAGIC select * from sdtable

// COMMAND ----------

// MAGIC %md
// MAGIC #### Global Temporary View
// MAGIC Temporary views in Spark SQL are session-scoped and will disappear if the session that creates it terminates. If you want to have a temporary view that is shared among all sessions and keep alive until the Spark application terminates, you can create a global temporary view. Global temporary view is tied to a system preserved database `global_temp`, and we must use the qualified name to refer it, e.g. `SELECT * FROM global_temp.view1`. See [http://spark.apache.org/docs/latest/sql-programming-guide.html#global-temporary-view](http://spark.apache.org/docs/latest/sql-programming-guide.html#global-temporary-view) for details.

// COMMAND ----------

// MAGIC %md
// MAGIC ## 6. Creating Datasets
// MAGIC Datasets are similar to RDDs, however, instead of using Java serialization or Kryo they use a specialized [Encoder](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Encoder) to serialize the objects for processing or transmitting over the network. While both encoders and standard serialization are responsible for turning an object into bytes, encoders are code generated dynamically and use a format that allows Spark to perform many operations like filtering, sorting and hashing without deserializing the bytes back into an object.

// COMMAND ----------

val rangeDS = spark.range(0, 3) // Ctrl+Enter to make DataSet with 0,1,2; Note we added '.toDF()' to this to create a DataFrame

// COMMAND ----------

rangeDS.show() // the column name 'id' is made by default here

// COMMAND ----------

// MAGIC %md
// MAGIC We can have more complicated objects in a `DataSet` too.

// COMMAND ----------

// Note: Case classes in Scala 2.10 can support only up to 22 fields. To work around this limit,
// you can use custom classes that implement the Product interface
case class Person(name: String, age: Long)

// Encoders are created for case classes
val caseClassDS = Seq(Person("Andy", 32), Person("Erik",44), Person("Anna", 15)).toDS()
caseClassDS.show()

// COMMAND ----------

// Encoders for most common types are automatically provided by importing spark.implicits._
val primitiveDS = Seq(1, 2, 3).toDS()
primitiveDS.map(_ + 1).collect() // Returns: Array(2, 3, 4)

// COMMAND ----------

df1

// COMMAND ----------

df1.show

// COMMAND ----------

// let's make a case class for our DF so we can convert it to Dataset
case class singleAndDoubleIntegers(once: Integer, twice: Integer)

// COMMAND ----------

val ds1 = df1.as[singleAndDoubleIntegers]

// COMMAND ----------

ds1.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC ## Next we will play with data
// MAGIC 
// MAGIC The data here is **semi-structured tabular data** (Tab-delimited text file in dbfs).
// MAGIC Let us see what Anthony Joseph in BerkeleyX/CS100.1x had to say about such data.
// MAGIC 
// MAGIC ### Key Data Management Concepts: Semi-Structured Tabular Data
// MAGIC 
// MAGIC **(watch now 1:26)**:
// MAGIC 
// MAGIC [![Semi-Structured Tabular Data by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/G_67yUxdDbU/0.jpg)](https://www.youtube.com/watch?v=G_67yUxdDbU?rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC 
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC ## Go through the databricks Introductions Now
// MAGIC 
// MAGIC * [https://docs.databricks.com/spark/latest/dataframes-datasets/introduction-to-dataframes-scala.html](https://docs.databricks.com/spark/latest/dataframes-datasets/introduction-to-dataframes-scala.html)
// MAGIC 
// MAGIC * [https://docs.databricks.com/spark/latest/dataframes-datasets/introduction-to-datasets.html](https://docs.databricks.com/spark/latest/dataframes-datasets/introduction-to-datasets.html)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Recommended Homework
// MAGIC This week's recommended homework is a deep dive into the [SparkSQL programming guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).
// MAGIC 
// MAGIC ### Recommended Extra-work
// MAGIC Those who want to understand SparkSQL functionalities in more detail can see:
// MAGIC 
// MAGIC * [video lectures in Module 3 of Anthony Joseph's Introduction to Big Data edX course](https://docs.databricks.com/spark/1.6/training/introduction-to-big-data-cs100x-2015/module-3.html).