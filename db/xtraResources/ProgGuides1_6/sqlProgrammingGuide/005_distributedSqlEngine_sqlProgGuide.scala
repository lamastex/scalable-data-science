// Databricks notebook source exported at Mon, 14 Mar 2016 04:46:35 UTC
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
// MAGIC # [Distributed SQL Engine](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)
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
// MAGIC # [Distributed SQL Engine](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)
// MAGIC 
// MAGIC Spark SQL can also act as a distributed query engine using its JDBC/ODBC
// MAGIC or command-line interface. In this mode, end-users or applications can
// MAGIC interact with Spark SQL directly to run SQL queries, without the need to
// MAGIC write any code.
// MAGIC 
// MAGIC Running the Thrift JDBC/ODBC server
// MAGIC -----------------------------------
// MAGIC 
// MAGIC The Thrift JDBC/ODBC server implemented here corresponds to the
// MAGIC [`HiveServer2`](https://cwiki.apache.org/confluence/display/Hive/Setting+Up+HiveServer2)
// MAGIC in Hive 1.2.1 You can test the JDBC server with the beeline script that
// MAGIC comes with either Spark or Hive 1.2.1.
// MAGIC 
// MAGIC To start the JDBC/ODBC server, run the following in the Spark directory:
// MAGIC 
// MAGIC     ./sbin/start-thriftserver.sh
// MAGIC 
// MAGIC This script accepts all `bin/spark-submit` command line options, plus a
// MAGIC `--hiveconf` option to specify Hive properties. You may run
// MAGIC `./sbin/start-thriftserver.sh --help` for a complete list of all
// MAGIC available options. By default, the server listens on localhost:10000.
// MAGIC You may override this behaviour via either environment variables, i.e.:
// MAGIC 
// MAGIC     export HIVE_SERVER2_THRIFT_PORT=<listening-port>
// MAGIC     export HIVE_SERVER2_THRIFT_BIND_HOST=<listening-host>
// MAGIC     ./sbin/start-thriftserver.sh \
// MAGIC       --master <master-uri> \
// MAGIC       ...
// MAGIC 
// MAGIC or system properties:
// MAGIC 
// MAGIC     ./sbin/start-thriftserver.sh \
// MAGIC       --hiveconf hive.server2.thrift.port=<listening-port> \
// MAGIC       --hiveconf hive.server2.thrift.bind.host=<listening-host> \
// MAGIC       --master <master-uri>
// MAGIC       ...
// MAGIC 
// MAGIC Now you can use beeline to test the Thrift JDBC/ODBC server:
// MAGIC 
// MAGIC     ./bin/beeline
// MAGIC 
// MAGIC Connect to the JDBC/ODBC server in beeline with:
// MAGIC 
// MAGIC     beeline> !connect jdbc:hive2://localhost:10000
// MAGIC 
// MAGIC Beeline will ask you for a username and password. In non-secure mode,
// MAGIC simply enter the username on your machine and a blank password. For
// MAGIC secure mode, please follow the instructions given in the [beeline
// MAGIC documentation](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients).
// MAGIC 
// MAGIC Configuration of Hive is done by placing your `hive-site.xml`,
// MAGIC `core-site.xml` and `hdfs-site.xml` files in `conf/`.
// MAGIC 
// MAGIC You may also use the beeline script that comes with Hive.
// MAGIC 
// MAGIC Thrift JDBC server also supports sending thrift RPC messages over HTTP
// MAGIC transport. Use the following setting to enable HTTP mode as system
// MAGIC property or in `hive-site.xml` file in `conf/`:
// MAGIC 
// MAGIC     hive.server2.transport.mode - Set this to value: http
// MAGIC     hive.server2.thrift.http.port - HTTP port number fo listen on; default is 10001
// MAGIC     hive.server2.http.endpoint - HTTP endpoint; default is cliservice
// MAGIC 
// MAGIC To test, use beeline to connect to the JDBC/ODBC server in http mode
// MAGIC with:
// MAGIC 
// MAGIC     beeline> !connect jdbc:hive2://<host>:<port>/<database>?hive.server2.transport.mode=http;hive.server2.thrift.http.path=<http_endpoint>
// MAGIC 
// MAGIC Running the Spark SQL CLI
// MAGIC -------------------------
// MAGIC 
// MAGIC The Spark SQL CLI is a convenient tool to run the Hive metastore service
// MAGIC in local mode and execute queries input from the command line. Note that
// MAGIC the Spark SQL CLI cannot talk to the Thrift JDBC server.
// MAGIC 
// MAGIC To start the Spark SQL CLI, run the following in the Spark directory:
// MAGIC 
// MAGIC     ./bin/spark-sql
// MAGIC 
// MAGIC Configuration of Hive is done by placing your `hive-site.xml`,
// MAGIC `core-site.xml` and `hdfs-site.xml` files in `conf/`. You may run
// MAGIC `./bin/spark-sql --help` for a complete list of all available options.

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