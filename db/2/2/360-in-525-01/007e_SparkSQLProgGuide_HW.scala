// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC # Performance Tuning
// MAGIC ## Spark Sql Programming Guide
// MAGIC 
// MAGIC -   Performance Tuning
// MAGIC     -   Caching Data In Memory
// MAGIC     -   Other Configuration Options

// COMMAND ----------

// MAGIC %md
// MAGIC # Performance Tuning
// MAGIC 
// MAGIC For some workloads it is possible to improve performance by either
// MAGIC caching data in memory, or by turning on some experimental options.
// MAGIC 
// MAGIC Caching Data In Memory
// MAGIC ----------------------
// MAGIC Spark SQL can cache tables using an in-memory columnar format by calling
// MAGIC `spark.cacheTable("tableName")` or `dataset.cache()`. Then Spark
// MAGIC SQL will scan only required columns and will automatically tune
// MAGIC compression to minimize memory usage and GC pressure. You can call
// MAGIC `spark.uncacheTable("tableName")` to remove the table from memory.
// MAGIC 
// MAGIC Configuration of in-memory caching can be done using the `setConf`
// MAGIC method on `SparkSession` or by running `SET key=value` commands using SQL.
// MAGIC 
// MAGIC | Property Name | Default | Meaning |
// MAGIC | --- | --- | --- |
// MAGIC | `spark.sql.inMemoryColumnarStorage.compressed` | true | When set to true Spark SQL will automatically select a compression codec for each column based on statistics of the data. |
// MAGIC | `spark.sql.inMemoryColumnarStorage.batchSize` | 10000 | Controls the size of batches for columnar caching. Larger batch sizes can improve memory utilization and compression, but risk OOMs when caching data. |
// MAGIC 
// MAGIC Other Configuration Options
// MAGIC ---------------------------
// MAGIC 
// MAGIC The following options can also be used to tune the performance of query
// MAGIC execution. It is possible that these options will be deprecated in
// MAGIC future release as more optimizations are performed automatically.
// MAGIC 
// MAGIC | Property Name | Default | Meaning |
// MAGIC | ---|---|--- |
// MAGIC |`spark.sql.autoBroadcastJoinThreshold` | 10485760 (10 MB) | Configures the maximum size in bytes for a table that will be broadcast to all worker nodes when performing a join. By setting this value to -1 broadcasting can be disabled. Note that currently statistics are only supported for Hive Metastore tables where the command `ANALYZE TABLE <tableName> COMPUTE STATISTICS noscan` has been run. |
// MAGIC | `spark.sql.tungsten.enabled` | true | When true, use the optimized Tungsten physical execution backend which explicitly manages memory and dynamically generates bytecode for expression evaluation. |
// MAGIC | `spark.sql.shuffle.partitions` | 200 | Configures the number of partitions to use when shuffling data for joins or aggregations. |