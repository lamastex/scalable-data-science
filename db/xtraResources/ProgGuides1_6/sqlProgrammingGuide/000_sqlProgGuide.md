// Databricks notebook source exported at Sun, 19 Jun 2016 02:52:45 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





This is an elaboration of the [Apache Spark 1.6 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).

Any contributions in this 'databricksification' of the programming guide are most welcome. Please feel free to send pull-requests or just fork and push yourself at [https://github.com/raazesh-sainudiin/scalable-data-science](https://github.com/raazesh-sainudiin/scalable-data-science).

# [Spark Sql Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/000_sqlProgGuide)

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






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
