// Databricks notebook source exported at Sat, 18 Jun 2016 07:18:41 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





This is an elaboration of the [Apache Spark 1.6 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).

# [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide)

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





# [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide)

Spark SQL is a Spark module for structured data processing. Unlike the
basic Spark RDD API, the interfaces provided by Spark SQL provide Spark
with more information about the structure of both the data and the
computation being performed. Internally, Spark SQL uses this extra
information to perform extra optimizations. There are several ways to
interact with Spark SQL including:
* SQL, 
* the DataFrames API and 
* the Datasets API. 

When computing a result the same execution engine is used,
independent of which API/language you are using to express the
computation. This unification means that developers can easily switch
back and forth between the various APIs based on which provides the most
natural way to express a given transformation.

All of the examples on this page use sample data included in the Spark
distribution and can be run in the `spark-shell`, `pyspark` shell, or
`sparkR` shell.


```scala

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

```
```scala

displayHTML(frameIt("https://en.wikipedia.org/wiki/Apache_Hive#HiveQL",175))

```



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

DataFrames
----------

A DataFrame is a distributed collection of data organized into named
columns. It is conceptually equivalent to a table in a relational
database or a data frame in R/Python, but with richer optimizations
under the hood. DataFrames can be constructed from a wide array of
[sources](sql-programming-guide.html#data-sources) such as: structured
data files, tables in Hive, external databases, or existing RDDs.

The DataFrame API is available in
[Scala](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame),
[Java](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/DataFrame.html),
[Python](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame),
and [R](http://spark.apache.org/docs/latest/api/R/index.html).

Datasets
--------

A Dataset is a new experimental interface added in Spark 1.6 that tries
to provide the benefits of RDDs (strong typing, ability to use powerful
lambda functions) with the benefits of Spark SQL’s optimized execution
engine. A Dataset can be
[constructed](sql-programming-guide.html#creating-datasets) from JVM
objects and then manipulated using functional transformations (map,
flatMap, filter, etc.).

The unified Dataset API can be used both in
[Scala](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset)
and
[Java](http://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html).
Python does not yet have support for the Dataset API, but due to its
dynamic nature many of the benefits are already available (i.e. you can
access the field of a row by name naturally `row.columnName`). Full
python support will be added in a future release.






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
