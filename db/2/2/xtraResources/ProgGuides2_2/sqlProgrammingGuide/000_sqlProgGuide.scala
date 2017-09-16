// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC Any contributions in this 'databricksification' of the programming guide are most welcome. Please feel free to send pull-requests or just fork and push yourself at/from [https://github.com/lamastex/scalable-data-science](https://github.com/lamastex/scalable-data-science).
// MAGIC 
// MAGIC **NOTE:** The links that do not have standard URLs for hyper-text transfer protocol, qualified here by (http) or (https), are *in general* internal links and will/should work if you follow the instructions in the lectures (from the YouTube play list, watched sequential in chronological order that is linked from [https://lamastex.github.io/scalable-data-science/sds/2/2/](https://lamastex.github.io/scalable-data-science/sds/2/2/)) on how to download the `.dbc` archive for the course and upload it into your community edition with the correctly named expected directory structures.
// MAGIC 
// MAGIC # [Spark Sql Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/sqlProgrammingGuide/000_sqlProgGuide)
// MAGIC 
// MAGIC -   [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/sqlProgrammingGuide/001_overview_sqlProgGuide)
// MAGIC     -   SQL
// MAGIC     -   DataFrames
// MAGIC     -   Datasets
// MAGIC -   [Getting Started](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide)
// MAGIC     -   Starting Point: SQLContext
// MAGIC     -   Creating DataFrames
// MAGIC     -   DataFrame Operations
// MAGIC     -   Running SQL Queries Programmatically
// MAGIC     -   Creating Datasets
// MAGIC     -   Interoperating with RDDs
// MAGIC         -   Inferring the Schema Using Reflection
// MAGIC         -   Programmatically Specifying the Schema
// MAGIC -   [Data Sources](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/sqlProgrammingGuide/003_dataSources_sqlProgGuide)
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
// MAGIC -   [Performance Tuning](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)
// MAGIC     -   Caching Data In Memory
// MAGIC     -   Other Configuration Options
// MAGIC -   [Distributed SQL Engine](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)
// MAGIC     -   Running the Thrift JDBC/ODBC server
// MAGIC     -   Running the Spark SQL CLI