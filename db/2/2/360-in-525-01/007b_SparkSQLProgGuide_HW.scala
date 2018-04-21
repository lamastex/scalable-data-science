// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC # Overview
// MAGIC ## Spark Sql Programming Guide
// MAGIC 
// MAGIC -   Overview
// MAGIC     -   SQL
// MAGIC     -   DataFrames
// MAGIC     -   Datasets

// COMMAND ----------

// MAGIC %md
// MAGIC # Overview
// MAGIC 
// MAGIC Spark SQL is a Spark module for structured data processing. Unlike the
// MAGIC basic Spark RDD API, the interfaces provided by Spark SQL provide Spark
// MAGIC with more information about the structure of both the data and the
// MAGIC computation being performed. Internally, Spark SQL uses this extra
// MAGIC information to perform extra optimizations. There are several ways to
// MAGIC interact with Spark SQL including:
// MAGIC 
// MAGIC * SQL (SQL 2003 standard compliant)
// MAGIC * the DataFrames API (since Spark 1.4, was generalized in Spark 2.0 and is alias for `Dataset[Row]`)
// MAGIC * the Datasets API (offers strongly-typed interface)
// MAGIC 
// MAGIC When computing a result the same execution engine is used,
// MAGIC independent of which API/language you are using to express the
// MAGIC computation. This unification means that developers can easily switch
// MAGIC back and forth between the various APIs based on which provides the most
// MAGIC natural way to express a given transformation.
// MAGIC 
// MAGIC All of the examples on this page use sample data included in the Spark
// MAGIC distribution and can be run in the `spark-shell`, `pyspark` shell, or
// MAGIC `sparkR` shell.

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

// MAGIC %md
// MAGIC SQL
// MAGIC ---
// MAGIC 
// MAGIC One use of Spark SQL is to execute SQL queries written using either a
// MAGIC basic SQL syntax or HiveQL. Spark SQL can also be used to read data from
// MAGIC an existing Hive installation. For more on how to configure this
// MAGIC feature, please refer to the [Hive
// MAGIC Tables](sql-programming-guide.html#hive-tables) section. When running
// MAGIC SQL from within another programming language the results will be
// MAGIC returned as a [DataFrame](sql-programming-guide.html#DataFrames). You
// MAGIC can also interact with the SQL interface using the
// MAGIC [command-line](sql-programming-guide.html#running-the-spark-sql-cli) or
// MAGIC over
// MAGIC [JDBC/ODBC](sql-programming-guide.html#running-the-thrift-jdbcodbc-server).
// MAGIC 
// MAGIC Datasets and DataFrames
// MAGIC -----------------------
// MAGIC 
// MAGIC A Dataset is a distributed collection of data. Dataset is a new interface added in Spark 1.6 that provides the benefits of RDDs (strong typing, ability to use powerful lambda functions) with the benefits of Spark SQL’s optimized execution engine, which has been improved in 2.x versions. A Dataset can be [constructed (http)](http://spark.apache.org/docs/latest/sql-programming-guide.html#creating-datasets) from JVM objects and then manipulated using functional transformations (map, flatMap, filter, etc.). The Dataset API is available in [Scala (http)](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) and [Java (http)](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html). Python does not have the support for the Dataset API. But due to Python’s dynamic nature, many of the benefits of the Dataset API are already available (i.e. you can access the field of a row by name naturally row.columnName). The case for R is similar.
// MAGIC 
// MAGIC A DataFrame is a Dataset organized into named columns. It is conceptually equivalent to a table in a relational database or a data frame in R/Python, but with richer optimizations under the hood. DataFrames can be constructed from a wide array of [sources](http://spark.apache.org/docs/latest/sql-programming-guide.html#data-sources) such as: structured data files, tables in Hive, external databases, or existing RDDs. The DataFrame API is available in Scala, Java, [Python](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame), and [R](http://spark.apache.org/docs/latest/api/R/index.html). In Scala and Java, a DataFrame is represented by a Dataset of Rows. In [the Scala API](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset), DataFrame is simply a type alias of `Dataset[Row]`. While, in [Java API](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html), users need to use `Dataset<Row>` to represent a DataFrame.