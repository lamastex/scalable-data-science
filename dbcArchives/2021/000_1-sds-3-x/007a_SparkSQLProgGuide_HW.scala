// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC This and the next sequence of notebooks are an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC # Spark Sql Programming Guide
// MAGIC 
// MAGIC -   Overview
// MAGIC     -   SQL
// MAGIC     -   DataFrames
// MAGIC     -   Datasets
// MAGIC -   Getting Started
// MAGIC     -   Starting Point: SQLContext
// MAGIC     -   Creating DataFrames
// MAGIC     -   DataFrame Operations
// MAGIC     -   Running SQL Queries Programmatically
// MAGIC     -   Creating Datasets
// MAGIC     -   Interoperating with RDDs
// MAGIC         -   Inferring the Schema Using Reflection
// MAGIC         -   Programmatically Specifying the Schema
// MAGIC -   Data Sources
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
// MAGIC -   Performance Tuning
// MAGIC     -   Caching Data In Memory
// MAGIC     -   Other Configuration Options
// MAGIC -   Distributed SQL Engine
// MAGIC     -   Running the Thrift JDBC/ODBC server
// MAGIC     -   Running the Spark SQL CLI
// MAGIC -   SQL Reference
// MAGIC 
// MAGIC ### What could one do with these notebooks?
// MAGIC 
// MAGIC One could read the Spark SQL Programming Guide that is embedded below and also linked above while going through the cells and doing the YouTrys in the following notebooks.
// MAGIC 
// MAGIC ### Why might one do it?
// MAGIC 
// MAGIC This homework/self-study will help you solve the assigned lab and theory exercises in the sequel, much faster by introducing you to some basic knowledge you need about Spark SQL.
// MAGIC 
// MAGIC #### NOTE on intra-iframe html navigation within a notebook: 
// MAGIC 
// MAGIC - When navigating in the html-page embedded as an iframe, as in the cell below, you can:
// MAGIC   - click on a link in the displayed html page to see the content of the clicked link and 
// MAGIC   - then right-click on the page and click on the arrow keys `<-` and `->` to go back or forward. 

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
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
displayHTML(frameIt("https://spark.apache.org/docs/latest/sql-programming-guide.html",750))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Let's go through the programming guide in databricks now
// MAGIC 
// MAGIC This is an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC 
// MAGIC Spark SQL, DataFrames and Datasets Guide
// MAGIC ---
// MAGIC 
// MAGIC 
// MAGIC Spark SQL is a Spark module for structured data processing. Unlike the
// MAGIC basic Spark RDD API, the interfaces provided by Spark SQL provide Spark
// MAGIC with more information about the structure of both the data and the
// MAGIC computation being performed. Internally, Spark SQL uses this extra
// MAGIC information to perform extra optimizations. There are several ways to
// MAGIC interact with Spark SQL including SQL and the Dataset API. When
// MAGIC computing a result, the same execution engine is used, independent of
// MAGIC which API/language you are using to express the computation. This
// MAGIC unification means that developers can easily switch back and forth
// MAGIC between different APIs based on which provides the most natural way to
// MAGIC express a given transformation.
// MAGIC 
// MAGIC All of the examples on this page use sample data included in the Spark
// MAGIC distribution and can be run in the `spark-shell`, `pyspark` shell, or `sparkR`
// MAGIC shell.
// MAGIC 
// MAGIC SQL
// MAGIC ---
// MAGIC 
// MAGIC One use of Spark SQL is to execute SQL queries. Spark SQL can also be
// MAGIC used to read data from an existing Hive installation. For more on how to
// MAGIC configure this feature, please refer to the [Hive Tables](https://spark.apache.org/docs/latest/sql-data-sources-hive-tables.html)
// MAGIC section. When running SQL from within another programming language the
// MAGIC results will be returned as a
// MAGIC [Dataset/DataFrame](sql-programming-guide.html#datasets-and-dataframes).
// MAGIC You can also interact with the SQL interface using the
// MAGIC [command-line](https://spark.apache.org/docs/latest/sql-distributed-sql-engine.html#running-the-spark-sql-cli)
// MAGIC or over
// MAGIC [JDBC/ODBC](https://spark.apache.org/docs/latest/sql-distributed-sql-engine.html#running-the-thrift-jdbcodbc-server).
// MAGIC 
// MAGIC Datasets and DataFrames
// MAGIC -----------------------
// MAGIC 
// MAGIC A Dataset is a distributed collection of data. Dataset is a new
// MAGIC interface added in Spark 1.6 that provides the benefits of RDDs (strong
// MAGIC typing, ability to use powerful lambda functions) with the benefits of
// MAGIC Spark SQL’s optimized execution engine. A Dataset can be
// MAGIC [constructed](https://spark.apache.org/docs/latest/sql-getting-started.html#creating-datasets)
// MAGIC from JVM objects and then manipulated using functional transformations
// MAGIC (`map`, `flatMap`, `filter`, etc.). The Dataset API is available in
// MAGIC [Scala](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/Dataset.html)
// MAGIC and
// MAGIC [Java](https://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html).
// MAGIC Python does not have the support for the Dataset API. But due to
// MAGIC Python’s dynamic nature, many of the benefits of the Dataset API are
// MAGIC already available (i.e. you can access the field of a row by name
// MAGIC naturally `row.columnName`). The case for R is
// MAGIC similar.
// MAGIC 
// MAGIC A DataFrame is a *Dataset* organized into named columns. It is
// MAGIC conceptually equivalent to a table in a relational database or a data
// MAGIC frame in R/Python, but with richer optimizations under the hood.
// MAGIC DataFrames can be constructed from a wide array of
// MAGIC [sources](https://spark.apache.org/docs/latest/sql-data-sources.html)
// MAGIC such as: structured data files, tables in Hive, external databases, or
// MAGIC existing RDDs. The DataFrame API is available in Scala, Java,
// MAGIC [Python](https://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame),
// MAGIC and [R](https://spark.apache.org/docs/latest/api/R/index.html). In Scala
// MAGIC and Java, a DataFrame is represented by a Dataset of
// MAGIC `Row`s. In [the Scala API](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/Dataset.html),
// MAGIC `DataFrame` is simply a type alias of
// MAGIC `Dataset[Row]`. While, in [Java API](https://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html),
// MAGIC users need to use `Dataset<Row>` to represent a
// MAGIC `DataFrame`.
// MAGIC 
// MAGIC Throughout this document, we will often refer to Scala/Java Datasets of
// MAGIC `Row`s as DataFrames.

// COMMAND ----------

// MAGIC %md
// MAGIC Background and Preparation
// MAGIC ---
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC - If you are unfamiliar with SQL please brush-up from the basic links below.
// MAGIC - SQL allows one to systematically explore any structured data (i.e., tables) using queries. This is necessary part of the data science process.
// MAGIC 
// MAGIC One can use the **SQL Reference** at [https://spark.apache.org/docs/latest/sql-ref.html](https://spark.apache.org/docs/latest/sql-ref.html) to learn SQL quickly. 

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/SQL",500))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Apache_Hive#HiveQL",175))

// COMMAND ----------

displayHTML(frameIt("https://spark.apache.org/docs/latest/sql-ref.html",700))

// COMMAND ----------

