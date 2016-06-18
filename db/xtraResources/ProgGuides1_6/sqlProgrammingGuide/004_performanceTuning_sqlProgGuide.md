// Databricks notebook source exported at Mon, 14 Mar 2016 04:43:58 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





This is an elaboration of the [Apache Spark 1.6 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).

# [Performance Tuning](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)

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





# [Performance Tuning](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)

For some workloads it is possible to improve performance by either
caching data in memory, or by turning on some experimental options.

Caching Data In Memory
----------------------

Spark SQL can cache tables using an in-memory columnar format by calling
`sqlContext.cacheTable("tableName")` or `dataFrame.cache()`. Then Spark
SQL will scan only required columns and will automatically tune
compression to minimize memory usage and GC pressure. You can call
`sqlContext.uncacheTable("tableName")` to remove the table from memory.

Configuration of in-memory caching can be done using the `setConf`
method on `SQLContext` or by running `SET key=value` commands using SQL.

  Property Name                                    Default   Meaning
  ------------------------------------------------ --------- --------------------------------------------------------------------------------------------------------------------------------------------------------
  `spark.sql.inMemoryColumnarStorage.compressed`   true      When set to true Spark SQL will automatically select a compression codec for each column based on statistics of the data.
  `spark.sql.inMemoryColumnarStorage.batchSize`    10000     Controls the size of batches for columnar caching. Larger batch sizes can improve memory utilization and compression, but risk OOMs when caching data.

Other Configuration Options
---------------------------

The following options can also be used to tune the performance of query
execution. It is possible that these options will be deprecated in
future release as more optimizations are performed automatically.

  Property Name                            Default            Meaning
  ---------------------------------------- ------------------ -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  `spark.sql.autoBroadcastJoinThreshold`   10485760 (10 MB)   Configures the maximum size in bytes for a table that will be broadcast to all worker nodes when performing a join. By setting this value to -1 broadcasting can be disabled. Note that currently statistics are only supported for Hive Metastore tables where the command `ANALYZE TABLE <tableName> COMPUTE STATISTICS noscan` has been run.
  `spark.sql.tungsten.enabled`             true               When true, use the optimized Tungsten physical execution backend which explicitly manages memory and dynamically generates bytecode for expression evaluation.
  `spark.sql.shuffle.partitions`           200                Configures the number of partitions to use when shuffling data for joins or aggregations.






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
