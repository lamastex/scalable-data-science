[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

This and the next five notebooks are an elaboration of the <http://spark.apache.org/docs/latest/sql-programming-guide.html> by Ivan Sadikov and Raazesh Sainudiin.

Spark Sql Programming Guide
===========================

-   Overview
    -   SQL
    -   DataFrames
    -   Datasets
-   Getting Started
    -   Starting Point: SQLContext
    -   Creating DataFrames
    -   DataFrame Operations
    -   Running SQL Queries Programmatically
    -   Creating Datasets
    -   Interoperating with RDDs
        -   Inferring the Schema Using Reflection
        -   Programmatically Specifying the Schema
-   Data Sources
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
-   Performance Tuning
    -   Caching Data In Memory
    -   Other Configuration Options
-   Distributed SQL Engine
    -   Running the Thrift JDBC/ODBC server
    -   Running the Spark SQL CLI