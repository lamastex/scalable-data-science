// Databricks notebook source exported at Mon, 14 Mar 2016 04:46:35 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





This is an elaboration of the [Apache Spark 1.6 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).

# [Distributed SQL Engine](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)

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





# [Distributed SQL Engine](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)

Spark SQL can also act as a distributed query engine using its JDBC/ODBC
or command-line interface. In this mode, end-users or applications can
interact with Spark SQL directly to run SQL queries, without the need to
write any code.

Running the Thrift JDBC/ODBC server
-----------------------------------

The Thrift JDBC/ODBC server implemented here corresponds to the
[`HiveServer2`](https://cwiki.apache.org/confluence/display/Hive/Setting+Up+HiveServer2)
in Hive 1.2.1 You can test the JDBC server with the beeline script that
comes with either Spark or Hive 1.2.1.

To start the JDBC/ODBC server, run the following in the Spark directory:

    ./sbin/start-thriftserver.sh

This script accepts all `bin/spark-submit` command line options, plus a
`--hiveconf` option to specify Hive properties. You may run
`./sbin/start-thriftserver.sh --help` for a complete list of all
available options. By default, the server listens on localhost:10000.
You may override this behaviour via either environment variables, i.e.:

    export HIVE_SERVER2_THRIFT_PORT=<listening-port>
    export HIVE_SERVER2_THRIFT_BIND_HOST=<listening-host>
    ./sbin/start-thriftserver.sh \
      --master <master-uri> \
      ...

or system properties:

    ./sbin/start-thriftserver.sh \
      --hiveconf hive.server2.thrift.port=<listening-port> \
      --hiveconf hive.server2.thrift.bind.host=<listening-host> \
      --master <master-uri>
      ...

Now you can use beeline to test the Thrift JDBC/ODBC server:

    ./bin/beeline

Connect to the JDBC/ODBC server in beeline with:

    beeline> !connect jdbc:hive2://localhost:10000

Beeline will ask you for a username and password. In non-secure mode,
simply enter the username on your machine and a blank password. For
secure mode, please follow the instructions given in the [beeline
documentation](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients).

Configuration of Hive is done by placing your `hive-site.xml`,
`core-site.xml` and `hdfs-site.xml` files in `conf/`.

You may also use the beeline script that comes with Hive.

Thrift JDBC server also supports sending thrift RPC messages over HTTP
transport. Use the following setting to enable HTTP mode as system
property or in `hive-site.xml` file in `conf/`:

    hive.server2.transport.mode - Set this to value: http
    hive.server2.thrift.http.port - HTTP port number fo listen on; default is 10001
    hive.server2.http.endpoint - HTTP endpoint; default is cliservice

To test, use beeline to connect to the JDBC/ODBC server in http mode
with:

    beeline> !connect jdbc:hive2://<host>:<port>/<database>?hive.server2.transport.mode=http;hive.server2.thrift.http.path=<http_endpoint>

Running the Spark SQL CLI
-------------------------

The Spark SQL CLI is a convenient tool to run the Hive metastore service
in local mode and execute queries input from the command line. Note that
the Spark SQL CLI cannot talk to the Thrift JDBC server.

To start the Spark SQL CLI, run the following in the Spark directory:

    ./bin/spark-sql

Configuration of Hive is done by placing your `hive-site.xml`,
`core-site.xml` and `hdfs-site.xml` files in `conf/`. You may run
`./bin/spark-sql --help` for a complete list of all available options.






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
