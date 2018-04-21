// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC # Distributed SQL Engine
// MAGIC ## Spark Sql Programming Guide
// MAGIC 
// MAGIC -   Distributed SQL Engine
// MAGIC     -   Running the Thrift JDBC/ODBC server
// MAGIC     -   Running the Spark SQL CLI

// COMMAND ----------

// MAGIC %md
// MAGIC # Distributed SQL Engine
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