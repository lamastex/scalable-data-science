[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This is an elaboration of the <http://spark.apache.org/docs/latest/sql-programming-guide.html> by Ivan Sadikov and Raazesh Sainudiin.

Performance Tuning
==================

Spark Sql Programming Guide
---------------------------

-   Performance Tuning
    -   Caching Data In Memory
    -   Other Configuration Options

Performance Tuning
==================

For some workloads it is possible to improve performance by either
caching data in memory, or by turning on some experimental options.

Caching Data In Memory
----------------------

Spark SQL can cache tables using an in-memory columnar format by calling
`spark.cacheTable("tableName")` or `dataset.cache()`. Then Spark
SQL will scan only required columns and will automatically tune
compression to minimize memory usage and GC pressure. You can call
`spark.uncacheTable("tableName")` to remove the table from memory.

Configuration of in-memory caching can be done using the `setConf`
method on `SparkSession` or by running `SET key=value` commands using SQL.

\| Property Name \| Default \| Meaning \|
\| --- \| --- \| --- \|
\| `spark.sql.inMemoryColumnarStorage.compressed` \| true \| When set to true Spark SQL will automatically select a compression codec for each column based on statistics of the data. \|
\| `spark.sql.inMemoryColumnarStorage.batchSize` \| 10000 \| Controls the size of batches for columnar caching. Larger batch sizes can improve memory utilization and compression, but risk OOMs when caching data. \|

Other Configuration Options
---------------------------

The following options can also be used to tune the performance of query
execution. It is possible that these options will be deprecated in
future release as more optimizations are performed automatically.

\| Property Name \| Default \| Meaning \|
\| ---\|---\|--- \|
\|`spark.sql.autoBroadcastJoinThreshold` \| 10485760 (10 MB) \| Configures the maximum size in bytes for a table that will be broadcast to all worker nodes when performing a join. By setting this value to -1 broadcasting can be disabled. Note that currently statistics are only supported for Hive Metastore tables where the command `ANALYZE TABLE <tableName> COMPUTE STATISTICS noscan` has been run. \|
\| `spark.sql.tungsten.enabled` \| true \| When true, use the optimized Tungsten physical execution backend which explicitly manages memory and dynamically generates bytecode for expression evaluation. \|
\| `spark.sql.shuffle.partitions` \| 200 \| Configures the number of partitions to use when shuffling data for joins or aggregations. \|