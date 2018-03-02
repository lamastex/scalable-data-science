[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is an elaboration of the <http://spark.apache.org/docs/latest/sql-programming-guide.html> by Ivan Sadikov and Raazesh Sainudiin.

Overview
========

Spark Sql Programming Guide
---------------------------

-   Overview
    -   SQL
    -   DataFrames
    -   Datasets

Overview
========

Spark SQL is a Spark module for structured data processing. Unlike the
basic Spark RDD API, the interfaces provided by Spark SQL provide Spark
with more information about the structure of both the data and the
computation being performed. Internally, Spark SQL uses this extra
information to perform extra optimizations. There are several ways to
interact with Spark SQL including:

-   SQL (SQL 2003 standard compliant)
-   the DataFrames API (since Spark 1.4, was generalized in Spark 2.0 and is alias for `Dataset[Row]`)
-   the Datasets API (offers strongly-typed interface)

When computing a result the same execution engine is used,
independent of which API/language you are using to express the
computation. This unification means that developers can easily switch
back and forth between the various APIs based on which provides the most
natural way to express a given transformation.

All of the examples on this page use sample data included in the Spark
distribution and can be run in the `spark-shell`, `pyspark` shell, or
`sparkR` shell.

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/SQL"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/ml-features.html">
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

SQL
---

One use of Spark SQL is to execute SQL queries written using either a
basic SQL syntax or HiveQL. Spark SQL can also be used to read data from
an existing Hive installation. For more on how to configure this
feature, please refer to the [Hive
Tables](sql-programming-guide.html#hive-tables) section. When running
SQL from within another programming language the results will be
returned as a [DataFrame](sql-programming-guide.html#DataFrames). You
can also interact with the SQL interface using the
[command-line](sql-programming-guide.html#running-the-spark-sql-cli) or
over
[JDBC/ODBC](sql-programming-guide.html#running-the-thrift-jdbcodbc-server).

Datasets and DataFrames
-----------------------

A Dataset is a distributed collection of data. Dataset is a new interface added in Spark 1.6 that provides the benefits of RDDs (strong typing, ability to use powerful lambda functions) with the benefits of Spark SQL’s optimized execution engine, which has been improved in 2.x versions. A Dataset can be [constructed (http)](http://spark.apache.org/docs/latest/sql-programming-guide.html#creating-datasets) from JVM objects and then manipulated using functional transformations (map, flatMap, filter, etc.). The Dataset API is available in [Scala (http)](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) and [Java (http)](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html). Python does not have the support for the Dataset API. But due to Python’s dynamic nature, many of the benefits of the Dataset API are already available (i.e. you can access the field of a row by name naturally row.columnName). The case for R is similar.

A DataFrame is a Dataset organized into named columns. It is conceptually equivalent to a table in a relational database or a data frame in R/Python, but with richer optimizations under the hood. DataFrames can be constructed from a wide array of [sources](http://spark.apache.org/docs/latest/sql-programming-guide.html#data-sources) such as: structured data files, tables in Hive, external databases, or existing RDDs. The DataFrame API is available in Scala, Java, [Python](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame), and [R](http://spark.apache.org/docs/latest/api/R/index.html). In Scala and Java, a DataFrame is represented by a Dataset of Rows. In [the Scala API](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset), DataFrame is simply a type alias of `Dataset[Row]`. While, in [Java API](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html), users need to use `Dataset<Row>` to represent a DataFrame.