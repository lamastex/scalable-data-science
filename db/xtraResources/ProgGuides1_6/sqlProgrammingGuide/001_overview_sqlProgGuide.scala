// Databricks notebook source exported at Sat, 18 Jun 2016 07:18:41 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [Apache Spark 1.6 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).
// MAGIC 
// MAGIC # [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide)
// MAGIC 
// MAGIC ## [Spark Sql Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/000_sqlProgGuide)
// MAGIC 
// MAGIC -   [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide)
// MAGIC     -   SQL
// MAGIC     -   DataFrames
// MAGIC     -   Datasets
// MAGIC -   [Getting Started](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide)
// MAGIC     -   Starting Point: SQLContext
// MAGIC     -   Creating DataFrames
// MAGIC     -   DataFrame Operations
// MAGIC     -   Running SQL Queries Programmatically
// MAGIC     -   Creating Datasets
// MAGIC     -   Interoperating with RDDs
// MAGIC         -   Inferring the Schema Using Reflection
// MAGIC         -   Programmatically Specifying the Schema
// MAGIC -   [Data Sources](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/003_dataSources_sqlProgGuide)
// MAGIC     -   Generic Load/Save Functions
// MAGIC         -   Manually Specifying Options
// MAGIC         -   Run SQL on files directly
// MAGIC         -   Save Modes
// MAGIC         -   Saving to Persistent Tables
// MAGIC     -   Parquet Files
// MAGIC         -   Loading Data Programmatically
// MAGIC         -   Partition Discovery
// MAGIC         -   Schema Merging
// MAGIC         -   Hive metastore Parquet table conversion
// MAGIC             -   Hive/Parquet Schema Reconciliation
// MAGIC             -   Metadata Refreshing
// MAGIC         -   Configuration
// MAGIC     -   JSON Datasets
// MAGIC     -   Hive Tables
// MAGIC         -   Interacting with Different Versions of Hive Metastore
// MAGIC     -   JDBC To Other Databases
// MAGIC     -   Troubleshooting
// MAGIC -   [Performance Tuning](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)
// MAGIC     -   Caching Data In Memory
// MAGIC     -   Other Configuration Options
// MAGIC -   [Distributed SQL Engine](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)
// MAGIC     -   Running the Thrift JDBC/ODBC server
// MAGIC     -   Running the Spark SQL CLI

// COMMAND ----------

// MAGIC %md
// MAGIC # [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide)
// MAGIC 
// MAGIC Spark SQL is a Spark module for structured data processing. Unlike the
// MAGIC basic Spark RDD API, the interfaces provided by Spark SQL provide Spark
// MAGIC with more information about the structure of both the data and the
// MAGIC computation being performed. Internally, Spark SQL uses this extra
// MAGIC information to perform extra optimizations. There are several ways to
// MAGIC interact with Spark SQL including:
// MAGIC * SQL, 
// MAGIC * the DataFrames API and 
// MAGIC * the Datasets API. 
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
// MAGIC DataFrames
// MAGIC ----------
// MAGIC 
// MAGIC A DataFrame is a distributed collection of data organized into named
// MAGIC columns. It is conceptually equivalent to a table in a relational
// MAGIC database or a data frame in R/Python, but with richer optimizations
// MAGIC under the hood. DataFrames can be constructed from a wide array of
// MAGIC [sources](sql-programming-guide.html#data-sources) such as: structured
// MAGIC data files, tables in Hive, external databases, or existing RDDs.
// MAGIC 
// MAGIC The DataFrame API is available in
// MAGIC [Scala](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame),
// MAGIC [Java](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/DataFrame.html),
// MAGIC [Python](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame),
// MAGIC and [R](http://spark.apache.org/docs/latest/api/R/index.html).
// MAGIC 
// MAGIC Datasets
// MAGIC --------
// MAGIC 
// MAGIC A Dataset is a new experimental interface added in Spark 1.6 that tries
// MAGIC to provide the benefits of RDDs (strong typing, ability to use powerful
// MAGIC lambda functions) with the benefits of Spark SQL’s optimized execution
// MAGIC engine. A Dataset can be
// MAGIC [constructed](sql-programming-guide.html#creating-datasets) from JVM
// MAGIC objects and then manipulated using functional transformations (map,
// MAGIC flatMap, filter, etc.).
// MAGIC 
// MAGIC The unified Dataset API can be used both in
// MAGIC [Scala](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset)
// MAGIC and
// MAGIC [Java](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html).
// MAGIC Python does not yet have support for the Dataset API, but due to its
// MAGIC dynamic nature many of the benefits are already available (i.e. you can
// MAGIC access the field of a row by name naturally `row.columnName`). Full
// MAGIC python support will be added in a future release.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)