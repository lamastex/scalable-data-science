// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC # Data Sources
// MAGIC ## Spark Sql Programming Guide
// MAGIC 
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

// COMMAND ----------

// MAGIC %md 
// MAGIC # Data Sources
// MAGIC 
// MAGIC Spark SQL supports operating on a variety of data sources through the `DataFrame` or `DataFrame` interfaces. A Dataset can be operated on as normal RDDs and can also be registered as a temporary table. Registering a Dataset as a table allows you to run SQL queries over its data. But from time to time you would need to either load or save Dataset. Spark SQL provides built-in data sources as well as Data Source API to define your own data source and use it read / write data into Spark.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Overview
// MAGIC Spark provides some built-in datasources that you can use straight out of the box, such as [Parquet](https://parquet.apache.org/), [JSON](http://www.json.org/), [JDBC](https://en.wikipedia.org/wiki/Java_Database_Connectivity), [ORC](https://orc.apache.org/) (available with enabled Hive Support, but this is changing, and ORC will not require Hive support and will work with default Spark session starting from next release), and Text (since Spark 1.6) and CSV (since Spark 2.0, before that it is accessible as a package).
// MAGIC 
// MAGIC ## Third-party datasource packages
// MAGIC Community also have built quite a few datasource packages to provide easy access to the data from other formats. You can find list of those packages on http://spark-packages.org/, e.g. [Avro](http://spark-packages.org/package/databricks/spark-avro), [CSV](http://spark-packages.org/package/databricks/spark-csv), [Amazon Redshit](http://spark-packages.org/package/databricks/spark-redshift) (for Spark < 2.0), [XML](http://spark-packages.org/package/HyukjinKwon/spark-xml), [NetFlow](http://spark-packages.org/package/sadikovi/spark-netflow) and many others. 

// COMMAND ----------

// MAGIC %md
// MAGIC ## Generic Load/Save functions
// MAGIC In order to load or save DataFrame you have to call either ``read`` or ``write``. This will return [DataFrameReader](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrameReader) or [DataFrameWriter](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrameWriter) depending on what you are trying to achieve. Essentially these classes are entry points to the reading / writing actions. They allow you to specify writing mode or provide additional options to read data source. 

// COMMAND ----------

// This will return DataFrameReader to read data source
println(spark.read)

val df = spark.range(0, 10)

// This will return DataFrameWriter to save DataFrame
println(df.write)

// COMMAND ----------

// Saving Parquet table in Scala
val df_save = spark.table("social_media_usage").select("platform", "visits")
df_save.write.mode("overwrite").parquet("/tmp/platforms.parquet")

// Loading Parquet table in Scala
val df = spark.read.parquet("/tmp/platforms.parquet")
df.show(5)

// COMMAND ----------

// MAGIC %py
// MAGIC # Loading Parquet table in Python
// MAGIC dfPy = spark.read.parquet("/tmp/platforms.parquet")
// MAGIC dfPy.show(5)

// COMMAND ----------

// Saving JSON dataset in Scala
val df_save = spark.table("social_media_usage").select("platform", "visits")
df_save.write.json("/tmp/platforms.json")

// Loading JSON dataset in Scala
val df = spark.read.json("/tmp/platforms.json")
df.show(5)

// COMMAND ----------

// MAGIC %py
// MAGIC # Loading JSON dataset in Python
// MAGIC dfPy = spark.read.json("/tmp/platforms.json")
// MAGIC dfPy.show(5)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Manually Specifying Options
// MAGIC 
// MAGIC You can also manually specify the data source that will be used along with any extra options that you would like to pass to the data source. Data sources are specified by their fully qualified name (i.e., `org.apache.spark.sql.parquet`), but for built-in sources you can also use their short names (`json`, `parquet`, `jdbc`). DataFrames of any type can be converted into other types using this syntax.

// COMMAND ----------

val json = sqlContext.read.format("json").load("/tmp/platforms.json")
json.select("platform").show(10)

val parquet = sqlContext.read.format("parquet").load("/tmp/platforms.parquet")
parquet.select("platform").show(10)

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Run SQL on files directly
// MAGIC Instead of using read API to load a file into DataFrame and query it, you can also query that file directly with SQL.

// COMMAND ----------

val df = sqlContext.sql("SELECT * FROM parquet.`/tmp/platforms.parquet`")
df.printSchema()

// COMMAND ----------

// MAGIC %md
// MAGIC ### Save Modes
// MAGIC Save operations can optionally take a `SaveMode`, that specifies how to handle existing data if present. It is important to realize that these save modes do not utilize any locking and are not atomic. Additionally, when performing a `Overwrite`, the data will be deleted before writing out the new data.
// MAGIC 
// MAGIC | Scala/Java | Any language | Meaning |
// MAGIC | --- | --- | --- |
// MAGIC | `SaveMode.ErrorIfExists` (default) | `"error"` (default) | When saving a DataFrame to a data source, if data already exists, an exception is expected to be thrown. |
// MAGIC | `SaveMode.Append` | `"append"` | When saving a DataFrame to a data source, if data/table already exists, contents of the DataFrame are expected to be appended to existing data. |
// MAGIC | `SaveMode.Overwrite` | `"overwrite"` | Overwrite mode means that when saving a DataFrame to a data source, if data/table already exists, existing data is expected to be overwritten by the contents of the DataFrame. |
// MAGIC | `SaveMode.Ignore` | `"ignore"` | Ignore mode means that when saving a DataFrame to a data source, if data already exists, the save operation is expected to not save the contents of the DataFrame and to not change the existing data. This is similar to a `CREATE TABLE IF NOT EXISTS` in SQL. |

// COMMAND ----------

// MAGIC %md
// MAGIC ### Saving to Persistent Tables
// MAGIC `DataFrame` and `Dataset` can also be saved as persistent tables using the `saveAsTable` command. Unlike the `createOrReplaceTempView` command, `saveAsTable` will materialize the contents of the dataframe and create a pointer to the data in the metastore. Persistent tables will still exist even after your Spark program has restarted, as long as you maintain your connection to the same metastore. A DataFrame for a persistent table can be created by calling the `table` method on a `SparkSession` with the name of the table.
// MAGIC 
// MAGIC By default `saveAsTable` will create a “managed table”, meaning that the location of the data will be controlled by the metastore. Managed tables will also have their data deleted automatically when a table is dropped.

// COMMAND ----------

// First of all list tables to see that table we are about to create does not exist
spark.catalog.listTables.show()

// COMMAND ----------

// MAGIC %sql
// MAGIC drop table if exists simple_range

// COMMAND ----------

val df = spark.range(0, 100)
df.write.saveAsTable("simple_range")

// Verify that table is saved and it is marked as persistent ("isTemporary" value should be "false")
spark.catalog.listTables.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ## Parquet Files
// MAGIC [Parquet](http://parquet.io) is a columnar format that is supported by many other data processing systems. Spark SQL provides support for both reading and writing Parquet files that automatically preserves the schema of the original data. When writing Parquet files, all columns are automatically converted to be nullable for compatibility reasons.

// COMMAND ----------

// MAGIC %md
// MAGIC ### More on Parquet
// MAGIC [Apache Parquet](https://parquet.apache.org/) is a [columnar storage](http://en.wikipedia.org/wiki/Column-oriented_DBMS) format available to any project in the Hadoop ecosystem, regardless of the choice of data processing framework, data model or programming language. It is a more efficient way to store data frames.
// MAGIC 
// MAGIC * To understand the ideas read [Dremel: Interactive Analysis of Web-Scale Datasets, Sergey Melnik, Andrey Gubarev, Jing Jing Long, Geoffrey Romer, Shiva Shivakumar, Matt Tolton and Theo Vassilakis,Proc. of the 36th Int'l Conf on Very Large Data Bases (2010), pp. 330-339](http://research.google.com/pubs/pub36632.html), whose Abstract is as follows:
// MAGIC     * Dremel is a scalable, interactive ad-hoc query system for analysis of read-only nested data. By combining multi-level execution trees and columnar data layouts it is **capable of running aggregation queries over trillion-row tables in seconds**. The system **scales to thousands of CPUs and petabytes of data, and has thousands of users at Google**. In this paper, we describe the architecture and implementation of Dremel, and explain how it complements MapReduce-based computing. We present a novel columnar storage representation for nested records and discuss experiments on few-thousand node instances of the system.

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://parquet.apache.org/documentation/latest/",500))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Loading Data Programmatically

// COMMAND ----------

// Read in the parquet file created above. Parquet files are self-describing so the schema is preserved.
// The result of loading a Parquet file is also a DataFrame.
val parquetFile = sqlContext.read.parquet("/tmp/platforms.parquet")

// Parquet files can also be registered as tables and then used in SQL statements.
parquetFile.createOrReplaceTempView("parquetFile")
val platforms = sqlContext.sql("SELECT platform FROM parquetFile WHERE visits > 0")
platforms.distinct.map(t => "Name: " + t(0)).collect().foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Partition Discovery
// MAGIC Table partitioning is a common optimization approach used in systems like Hive. In a partitioned table, data are usually stored in different directories, with partitioning column values encoded in the path of each partition directory. The Parquet data source is now able to discover and infer partitioning information automatically. For example, we can store all our previously used population data (from the programming guide example!) into a partitioned table using the following directory structure, with two extra columns, `gender` and `country` as partitioning columns:
// MAGIC ```
// MAGIC     path
// MAGIC     └── to
// MAGIC         └── table
// MAGIC             ├── gender=male
// MAGIC             │   ├── ...
// MAGIC             │   │
// MAGIC             │   ├── country=US
// MAGIC             │   │   └── data.parquet
// MAGIC             │   ├── country=CN
// MAGIC             │   │   └── data.parquet
// MAGIC             │   └── ...
// MAGIC             └── gender=female
// MAGIC                 ├── ...
// MAGIC                 │
// MAGIC                 ├── country=US
// MAGIC                 │   └── data.parquet
// MAGIC                 ├── country=CN
// MAGIC                 │   └── data.parquet
// MAGIC                 └── ...
// MAGIC ```
// MAGIC By passing `path/to/table` to either `SparkSession.read.parquet` or `SparkSession.read.load`, Spark SQL will automatically extract the partitioning information from the paths. Now the schema of the returned DataFrame becomes:
// MAGIC ```
// MAGIC     root
// MAGIC     |-- name: string (nullable = true)
// MAGIC     |-- age: long (nullable = true)
// MAGIC     |-- gender: string (nullable = true)
// MAGIC     |-- country: string (nullable = true)
// MAGIC ```
// MAGIC Notice that the data types of the partitioning columns are automatically inferred. Currently, numeric data types and string type are supported. Sometimes users may not want to automatically infer the data types of the partitioning columns. For these use cases, the automatic type inference can be configured by `spark.sql.sources.partitionColumnTypeInference.enabled`, which is default to `true`. When type inference is disabled, string type will be used for the partitioning columns.
// MAGIC 
// MAGIC Starting from Spark 1.6.0, partition discovery only finds partitions under the given paths by default. For the above example, if users pass `path/to/table/gender=male` to either `SparkSession.read.parquet` or `SparkSession.read.load`, `gender` will not be considered as a partitioning column. If users need to specify the base path that partition discovery should start with, they can set `basePath` in the data source options. For example, when `path/to/table/gender=male` is the path of the data and users set `basePath` to `path/to/table/`, `gender` will be a partitioning column.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Schema Merging
// MAGIC Like ProtocolBuffer, Avro, and Thrift, Parquet also supports schema evolution. Users can start with a simple schema, and gradually add more columns to the schema as needed. In this way, users may end up with multiple Parquet files with different but mutually compatible schemas. The Parquet data source is now able to automatically detect this case and merge schemas of all these files.
// MAGIC 
// MAGIC Since schema merging is a relatively expensive operation, and is not a necessity in most cases, we turned it off by default starting from 1.5.0. You may enable it by:
// MAGIC 
// MAGIC 1.  setting data source option `mergeSchema` to `true` when reading Parquet files (as shown in the examples below), or
// MAGIC 2.  setting the global SQL option `spark.sql.parquet.mergeSchema` to `true`.

// COMMAND ----------

// Create a simple DataFrame, stored into a partition directory
val df1 = sc.parallelize(1 to 5).map(i => (i, i * 2)).toDF("single", "double")
df1.write.mode("overwrite").parquet("/tmp/data/test_table/key=1")

// Create another DataFrame in a new partition directory, adding a new column and dropping an existing column
val df2 = sc.parallelize(6 to 10).map(i => (i, i * 3)).toDF("single", "triple")
df2.write.mode("overwrite").parquet("/tmp/data/test_table/key=2")

// Read the partitioned table
val df3 = spark.read.option("mergeSchema", "true").parquet("/tmp/data/test_table")
df3.printSchema()

// The final schema consists of all 3 columns in the Parquet files together
// with the partitioning column appeared in the partition directory paths.
// root
//  |-- single: integer (nullable = true)
//  |-- double: integer (nullable = true)
//  |-- triple: integer (nullable = true)
//  |-- key: integer (nullable = true))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Hive metastore Parquet table conversion
// MAGIC When reading from and writing to Hive metastore Parquet tables, Spark SQL will try to use its own Parquet support instead of Hive SerDe for better performance. This behavior is controlled by the `spark.sql.hive.convertMetastoreParquet` configuration, and is turned on by default.
// MAGIC 
// MAGIC #### Hive/Parquet Schema Reconciliation
// MAGIC There are two key differences between Hive and Parquet from the perspective of table schema processing.
// MAGIC 
// MAGIC 1.  Hive is case insensitive, while Parquet is not
// MAGIC 2.  Hive considers all columns nullable, while nullability in Parquet is significant
// MAGIC 
// MAGIC Due to this reason, we must reconcile Hive metastore schema with Parquet schema when converting a Hive metastore Parquet table to a Spark SQL Parquet table. The reconciliation rules are:
// MAGIC 
// MAGIC 1.  Fields that have the same name in both schema must have the same data type regardless of nullability. The reconciled field should have the data type of the Parquet side, so that nullability is respected.
// MAGIC 2.  The reconciled schema contains exactly those fields defined in Hive metastore schema.
// MAGIC   -   Any fields that only appear in the Parquet schema are dropped in the reconciled schema.
// MAGIC   -   Any fileds that only appear in the Hive metastore schema are added as nullable field in the reconciled schema.
// MAGIC 
// MAGIC #### Metadata Refreshing
// MAGIC Spark SQL caches Parquet metadata for better performance. When Hive metastore Parquet table conversion is enabled, metadata of those converted tables are also cached. If these tables are updated by Hive or other external tools, you need to refresh them manually to ensure consistent metadata.

// COMMAND ----------

// should refresh table metadata
spark.catalog.refreshTable("simple_range")

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Or you can use SQL to refresh table
// MAGIC REFRESH TABLE simple_range;

// COMMAND ----------

// MAGIC %md
// MAGIC ### Configuration
// MAGIC 
// MAGIC Configuration of Parquet can be done using the `setConf` method on
// MAGIC `SQLContext` or by running `SET key=value` commands using SQL.
// MAGIC 
// MAGIC | Property Name | Default | Meaning |
// MAGIC | --- | --- | --- | --- | 
// MAGIC | `spark.sql.parquet.binaryAsString` | false | Some other Parquet-producing systems, in particular Impala, Hive, and older versions of Spark SQL, do not differentiate between binary data and strings when writing out the Parquet schema. This flag tells Spark SQL to interpret binary data as a string to provide compatibility with these systems. |
// MAGIC | `spark.sql.parquet.int96AsTimestamp` | true | Some Parquet-producing systems, in particular Impala and Hive, store Timestamp into INT96. This flag tells Spark SQL to interpret INT96 data as a timestamp to provide compatibility with these systems. |
// MAGIC | `spark.sql.parquet.cacheMetadata` | true | Turns on caching of Parquet schema metadata. Can speed up querying of static data. |
// MAGIC | `spark.sql.parquet.compression.codec` | gzip | Sets the compression codec use when writing Parquet files. Acceptable values include: uncompressed, snappy, gzip, lzo. |
// MAGIC | `spark.sql.parquet.filterPushdown` | true | Enables Parquet filter push-down optimization when set to true. |
// MAGIC | `spark.sql.hive.convertMetastoreParquet` | true | When set to false, Spark SQL will use the Hive SerDe for parquet tables instead of the built in support. |
// MAGIC | `spark.sql.parquet.output.committer.class` | `org.apache.parquet.hadoop.ParquetOutputCommitter` | The output committer class used by Parquet. The specified class needs to be a subclass of `org.apache.hadoop.mapreduce.OutputCommitter`. Typically, it's also a subclass of `org.apache.parquet.hadoop.ParquetOutputCommitter`. Spark SQL comes with a builtin `org.apache.spark.sql.parquet.DirectParquetOutputCommitter`, which can be more efficient then the default Parquet output committer when writing data to S3. |
// MAGIC | `spark.sql.parquet.mergeSchema` | `false` | When true, the Parquet data source merges schemas collected from all data files, otherwise the schema is picked from the summary file or a random data file if no summary file is available. | 

// COMMAND ----------

// MAGIC %md
// MAGIC ## JSON Datasets
// MAGIC Spark SQL can automatically infer the schema of a JSON dataset and load it as a DataFrame. This conversion can be done using `SparkSession.read.json()` on either an RDD of String, or a JSON file.
// MAGIC 
// MAGIC Note that the file that is offered as *a json file* is not a typical JSON file. Each line must contain a separate, self-contained valid JSON object. As a consequence, a regular multi-line JSON file will most often fail.

// COMMAND ----------

// A JSON dataset is pointed to by path.
// The path can be either a single text file or a directory storing text files.
val path = "/tmp/platforms.json"
val platforms = spark.read.json(path)

// The inferred schema can be visualized using the printSchema() method.
platforms.printSchema()
// root
//  |-- platform: string (nullable = true)
//  |-- visits: long (nullable = true)

// Register this DataFrame as a table.
platforms.createOrReplaceTempView("platforms")

// SQL statements can be run by using the sql methods provided by sqlContext.
val facebook = spark.sql("SELECT platform, visits FROM platforms WHERE platform like 'Face%k'")
facebook.show()

// Alternatively, a DataFrame can be created for a JSON dataset represented by
// an RDD[String] storing one JSON object per string.
val rdd = sc.parallelize("""{"name":"IWyn","address":{"city":"Columbus","state":"Ohio"}}""" :: Nil)
val anotherPlatforms = spark.read.json(rdd)
anotherPlatforms.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ## Hive Tables
// MAGIC Spark SQL also supports reading and writing data stored in [Apache Hive](http://hive.apache.org/). However, since Hive has a large number of dependencies, it is not included in the default Spark assembly. Hive support is enabled by adding the `-Phive` and `-Phive-thriftserver` flags to Spark’s build. This command builds a new assembly jar that includes Hive. Note that this Hive assembly jar must also be present on all of the worker nodes, as they will need access to the Hive serialization and deserialization libraries (SerDes) in order to access data stored in Hive.
// MAGIC 
// MAGIC Configuration of Hive is done by placing your `hive-site.xml`, `core-site.xml` (for security configuration), `hdfs-site.xml` (for HDFS configuration) file in `conf/`. Please note when running the query on a YARN cluster (`cluster` mode), the `datanucleus` jars under the `lib_managed/jars` directory and `hive-site.xml` under `conf/` directory need to be available on the driver and all executors launched by the YARN cluster. The convenient way to do this is adding them through the `--jars` option and `--file` option of the `spark-submit` command.
// MAGIC 
// MAGIC When working with Hive one must construct a `HiveContext`, which inherits from `SQLContext`, and adds support for finding tables in the MetaStore and writing queries using HiveQL. Users who do not have an existing Hive deployment can still create a `HiveContext`. When not configured by the hive-site.xml, the context automatically creates `metastore_db` in the current directory and creates `warehouse` directory indicated by HiveConf, which defaults to `/user/hive/warehouse`. Note that you may need to grant write privilege on `/user/hive/warehouse` to the user who starts the spark application.
// MAGIC 
// MAGIC ```scala
// MAGIC val spark = SparkSession.builder.enableHiveSupport().getOrCreate()
// MAGIC 
// MAGIC spark.sql("CREATE TABLE IF NOT EXISTS src (key INT, value STRING)")
// MAGIC spark.sql("LOAD DATA LOCAL INPATH 'examples/src/main/resources/kv1.txt' INTO TABLE src")
// MAGIC 
// MAGIC // Queries are expressed in HiveQL
// MAGIC spark.sql("FROM src SELECT key, value").collect().foreach(println)
// MAGIC ```
// MAGIC 
// MAGIC ### Interacting with Different Versions of Hive Metastore
// MAGIC One of the most important pieces of Spark SQL’s Hive support is interaction with Hive metastore, which enables Spark SQL to access metadata of Hive tables. Starting from Spark 1.4.0, a single binary build of Spark SQL can be used to query different versions of Hive metastores, using the configuration described below. Note that independent of the version of Hive that is being used to talk to the metastore, internally Spark SQL will compile against Hive 1.2.1 and use those classes for internal execution (serdes, UDFs, UDAFs, etc).
// MAGIC 
// MAGIC The following options can be used to configure the version of Hive that is used to retrieve metadata:
// MAGIC 
// MAGIC | Property Name | Default | Meaning |
// MAGIC | --- | --- | --- |
// MAGIC | `spark.sql.hive.metastore.version` | `1.2.1` | Version of the Hive metastore. Available options are `0.12.0` through `1.2.1`. |
// MAGIC | `spark.sql.hive.metastore.jars` | `builtin` | Location of the jars that should be used to instantiate the HiveMetastoreClient. This property can be one of three options: `builtin`, `maven`, a classpath in the standard format for the JVM. This classpath must include all of Hive and its dependencies, including the correct version of Hadoop. These jars only need to be present on the driver, but if you are running in yarn cluster mode then you must ensure they are packaged with you application. |
// MAGIC | `spark.sql.hive.metastore.sharedPrefixes` | `com.mysql.jdbc,org.postgresql,com.microsoft.sqlserver,oracle.jdbc` | A comma separated list of class prefixes that should be loaded using the classloader that is shared between Spark SQL and a specific version of Hive. An example of classes that should be shared is JDBC drivers that are needed to talk to the metastore. Other classes that need to be shared are those that interact with classes that are already shared. For example, custom appenders that are used by log4j. |
// MAGIC | `spark.sql.hive.metastore.barrierPrefixes` | `(empty)` | A comma separated list of class prefixes that should explicitly be reloaded for each version of Hive that Spark SQL is communicating with. For example, Hive UDFs that are declared in a prefix that typically would be shared (i.e. `org.apache.spark.*`). |

// COMMAND ----------

// MAGIC %md
// MAGIC ## JDBC To Other Databases
// MAGIC Spark SQL also includes a data source that can read data from other databases using JDBC. This functionality should be preferred over using [JdbcRDD](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.JdbcRDD). This is because the results are returned as a DataFrame and they can easily be processed in Spark SQL or joined with other data sources. The JDBC data source is also easier to use from Java or Python as it does not require the user to provide a ClassTag. (Note that this is different than the Spark SQL JDBC server, which allows other applications to run queries using Spark SQL).
// MAGIC 
// MAGIC To get started you will need to include the JDBC driver for you particular database on the spark classpath. For example, to connect to postgres from the Spark Shell you would run the following command:
// MAGIC 
// MAGIC ```
// MAGIC SPARK_CLASSPATH=postgresql-9.3-1102-jdbc41.jar bin/spark-shell
// MAGIC ```
// MAGIC 
// MAGIC Tables from the remote database can be loaded as a DataFrame or Spark SQL Temporary table using the Data Sources API. The following options are supported:
// MAGIC 
// MAGIC | Property Name | Meaning |
// MAGIC | --- | --- | --- |
// MAGIC | `url` | The JDBC URL to connect to. |
// MAGIC | `dbtable` | The JDBC table that should be read. Note that anything that is valid in a `FROM` clause of a SQL query can be used. For example, instead of a full table you could also use a subquery in parentheses. |
// MAGIC | `driver` | The class name of the JDBC driver needed to connect to this URL. This class will be loaded on the master and workers before running an JDBC commands to allow the driver to register itself with the JDBC subsystem. |
// MAGIC | `partitionColumn, lowerBound, upperBound, numPartitions` | These options must all be specified if any of them is specified. They describe how to partition the table when reading in parallel from multiple workers. `partitionColumn` must be a numeric column from the table in question. Notice that `lowerBound` and `upperBound` are just used to decide the partition stride, not for filtering the rows in table. So all rows in the table will be partitioned and returned. |
// MAGIC | `fetchSize` | The JDBC fetch size, which determines how many rows to fetch per round trip. This can help performance on JDBC drivers which default to low fetch size (eg. Oracle with 10 rows). |
// MAGIC 
// MAGIC ```
// MAGIC // Example of using JDBC datasource
// MAGIC val jdbcDF = spark.read.format("jdbc").options(Map("url" -> "jdbc:postgresql:dbserver", "dbtable" -> "schema.tablename")).load()
// MAGIC ```
// MAGIC 
// MAGIC ```
// MAGIC -- Or using JDBC datasource in SQL
// MAGIC CREATE TEMPORARY TABLE jdbcTable
// MAGIC USING org.apache.spark.sql.jdbc
// MAGIC OPTIONS (
// MAGIC   url "jdbc:postgresql:dbserver",
// MAGIC   dbtable "schema.tablename"
// MAGIC )
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC ### Troubleshooting
// MAGIC - The JDBC driver class must be visible to the primordial class loader on the client session and on all executors. This is because Java’s DriverManager class does a security check that results in it ignoring all drivers not visible to the primordial class loader when one goes to open a connection. One convenient way to do this is to modify compute\_classpath.sh on all worker nodes to include your driver JARs.
// MAGIC - Some databases, such as H2, convert all names to upper case. You’ll need to use upper case to refer to those names in Spark SQL.