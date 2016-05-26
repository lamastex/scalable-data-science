// Databricks notebook source exported at Mon, 14 Mar 2016 04:43:58 UTC
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
// MAGIC # [Performance Tuning](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)
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
// MAGIC # [Performance Tuning](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)
// MAGIC 
// MAGIC For some workloads it is possible to improve performance by either
// MAGIC caching data in memory, or by turning on some experimental options.
// MAGIC 
// MAGIC Caching Data In Memory
// MAGIC ----------------------
// MAGIC 
// MAGIC Spark SQL can cache tables using an in-memory columnar format by calling
// MAGIC `sqlContext.cacheTable("tableName")` or `dataFrame.cache()`. Then Spark
// MAGIC SQL will scan only required columns and will automatically tune
// MAGIC compression to minimize memory usage and GC pressure. You can call
// MAGIC `sqlContext.uncacheTable("tableName")` to remove the table from memory.
// MAGIC 
// MAGIC Configuration of in-memory caching can be done using the `setConf`
// MAGIC method on `SQLContext` or by running `SET key=value` commands using SQL.
// MAGIC 
// MAGIC   Property Name                                    Default   Meaning
// MAGIC   ------------------------------------------------ --------- --------------------------------------------------------------------------------------------------------------------------------------------------------
// MAGIC   `spark.sql.inMemoryColumnarStorage.compressed`   true      When set to true Spark SQL will automatically select a compression codec for each column based on statistics of the data.
// MAGIC   `spark.sql.inMemoryColumnarStorage.batchSize`    10000     Controls the size of batches for columnar caching. Larger batch sizes can improve memory utilization and compression, but risk OOMs when caching data.
// MAGIC 
// MAGIC Other Configuration Options
// MAGIC ---------------------------
// MAGIC 
// MAGIC The following options can also be used to tune the performance of query
// MAGIC execution. It is possible that these options will be deprecated in
// MAGIC future release as more optimizations are performed automatically.
// MAGIC 
// MAGIC   Property Name                            Default            Meaning
// MAGIC   ---------------------------------------- ------------------ -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// MAGIC   `spark.sql.autoBroadcastJoinThreshold`   10485760 (10 MB)   Configures the maximum size in bytes for a table that will be broadcast to all worker nodes when performing a join. By setting this value to -1 broadcasting can be disabled. Note that currently statistics are only supported for Hive Metastore tables where the command `ANALYZE TABLE <tableName> COMPUTE STATISTICS noscan` has been run.
// MAGIC   `spark.sql.tungsten.enabled`             true               When true, use the optimized Tungsten physical execution backend which explicitly manages memory and dynamically generates bytecode for expression evaluation.
// MAGIC   `spark.sql.shuffle.partitions`           200                Configures the number of partitions to use when shuffling data for joins or aggregations.

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