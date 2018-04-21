// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This and the next five notebooks are an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
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