// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC # Getting Started
// MAGIC ## Spark Sql Programming Guide
// MAGIC 
// MAGIC -   Starting Point: SparkSession
// MAGIC -   Creating DataFrames
// MAGIC -   Untyped Dataset Operations (aka DataFrame Operations)
// MAGIC -   Running SQL Queries Programmatically
// MAGIC -   Global Temporary View
// MAGIC -   Creating Datasets
// MAGIC -   Interoperating with RDDs
// MAGIC     -   Inferring the Schema Using Reflection
// MAGIC     -   Programmatically Specifying the Schema
// MAGIC -   Scalar Functions
// MAGIC -   Aggregate Functions

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Getting Started
// MAGIC 
// MAGIC 
// MAGIC Starting Point: SparkSession
// MAGIC ----------------------------
// MAGIC 
// MAGIC 
// MAGIC The entry point into all functionality in Spark is the
// MAGIC [`SparkSession`](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/SparkSession.html) class and/or `SQLContext`/`HiveContext`. `SparkSession` is created for you as `spark` when you start **spark-shell** on command-line REPL or through a notebook server (databricks, zeppelin, jupyter, etc.). You will need to create `SparkSession` usually when building an application for submission to a Spark cluster. To create a basic `SparkSession`, just use `SparkSession.builder()`:
// MAGIC 
// MAGIC 
// MAGIC ``` 
// MAGIC import org.apache.spark.sql.SparkSession
// MAGIC 
// MAGIC val spark = SparkSession
// MAGIC   .builder()
// MAGIC   .appName("Spark SQL basic example")
// MAGIC   .config("spark.some.config.option", "some-value")
// MAGIC   .getOrCreate()
// MAGIC 
// MAGIC // For implicit conversions like converting RDDs to DataFrames
// MAGIC import spark.implicits._
// MAGIC ```
// MAGIC 
// MAGIC Find full example code in the Spark repo at:
// MAGIC 
// MAGIC - [https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala](https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala)
// MAGIC 
// MAGIC 
// MAGIC `SparkSession` in Spark 2.0 provides builtin support
// MAGIC for Hive features including the ability to write queries using HiveQL,
// MAGIC access to Hive UDFs, and the ability to read data from Hive tables. To
// MAGIC use these features, you do not need to have an existing Hive setup.
// MAGIC 
// MAGIC ```
// MAGIC // You could get SparkContext and SQLContext from SparkSession
// MAGIC val sc = spark.sparkContext
// MAGIC val sqlContext = spark.sqlContext
// MAGIC ```
// MAGIC 
// MAGIC But in Databricks notebook (similar to `spark-shell`) `SparkSession` is already created for you and is available as `spark` (similarly, `sc` and `sqlContext` are also available).

// COMMAND ----------

// Evaluation of the cell by Ctrl+Enter will print spark session available in notebook
spark

// COMMAND ----------

// MAGIC %md
// MAGIC After evaluation you should see something like this, i.e., a reference to the `SparkSession` you just created:
// MAGIC 
// MAGIC ```
// MAGIC res0: org.apache.spark.sql.SparkSession = org.apache.spark.sql.SparkSession@5a289bf5
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC ## Creating DataFrames
// MAGIC 
// MAGIC With a `SparkSessions`, applications can create [Dataset](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) or [`DataFrame`](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) from an [existing `RDD`](https://spark.apache.org/docs/latest/sql-getting-started.html#interoperating-with-rdds), from a Hive table, or from various [datasources](https://spark.apache.org/docs/latest/sql-data-sources.html).
// MAGIC 
// MAGIC Just to recap, a DataFrame is a distributed collection of data organized into named columns. You can think of it as an organized into table RDD of case class `Row` (which is not exactly true). DataFrames, in comparison to RDDs, are backed by rich optimizations, including tracking their own schema, adaptive query execution, code generation including whole stage codegen, extensible Catalyst optimizer, and project [Tungsten](https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html). 
// MAGIC 
// MAGIC Dataset provides type-safety when working with SQL, since `Row` is mapped to a case class, so that each column can be referenced by property of that class.
// MAGIC 
// MAGIC > Note that performance for Dataset/DataFrames is the same across languages Scala, Java, Python, and R. This is due to the fact that the planning phase is just language-specific, only logical plan is constructed in Python, and all the physical execution is compiled and executed as JVM bytecode.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC As an example, the following creates a DataFrame based on the content of
// MAGIC a JSON file:
// MAGIC 
// MAGIC 
// MAGIC ``` 
// MAGIC val df = spark.read.json("examples/src/main/resources/people.json")
// MAGIC 
// MAGIC // Displays the content of the DataFrame to stdout
// MAGIC df.show()
// MAGIC // +----+-------+
// MAGIC // | age|   name|
// MAGIC // +----+-------+
// MAGIC // |null|Michael|
// MAGIC // |  30|   Andy|
// MAGIC // |  19| Justin|
// MAGIC // +----+-------+
// MAGIC ```
// MAGIC 
// MAGIC Find full example code at
// MAGIC - https://raw.githubusercontent.com/apache/spark/master/examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala
// MAGIC in the Spark repo.
// MAGIC 
// MAGIC To be able to try this example in databricks we need to load the `people.json` file into `dbfs`. Let us do this programmatically next.

// COMMAND ----------

// the following lines merely fetch the file from the URL and load it into the dbfs for us to try in databricks
// getLines from the file at the URL
val peopleJsonLinesFromURL = scala.io.Source.fromURL("https://raw.githubusercontent.com/apache/spark/master/examples/src/main/resources/people.json").getLines
// remove any pre-existing file at the dbfs location
dbutils.fs.rm("dbfs:///datasets/spark-examples/people.json",recurse=true)
// convert the lines fetched from the URL to a Seq, then make it a RDD of String and finally save it as textfile to dbfs
sc.parallelize(peopleJsonLinesFromURL.toSeq).saveAsTextFile("dbfs:///datasets/spark-examples/people.json")
// read the text file we just saved and see what it has
sc.textFile("dbfs:///datasets/spark-examples/people.json").collect.mkString("\n")

// COMMAND ----------

val df = spark.read.json("dbfs:///datasets/spark-examples/people.json")

// COMMAND ----------

// you can also read into df like this
val df = spark.read.format("json").load("dbfs:///datasets/spark-examples/people.json")

// COMMAND ----------

df.show()

// COMMAND ----------

// MAGIC %md
// MAGIC Untyped Dataset Operations (aka DataFrame Operations)
// MAGIC -----------------------------------------------------
// MAGIC 
// MAGIC DataFrames provide a domain-specific language for structured data manipulation in 
// MAGIC [Scala](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/Dataset.html),
// MAGIC [Java](https://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/sql/Dataset.html),
// MAGIC [Python](https://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame)
// MAGIC and [R](https://spark.apache.org/docs/latest/api/R/SparkDataFrame.html).
// MAGIC 
// MAGIC As mentioned above, in Spark 2.0, DataFrames are just Dataset of
// MAGIC `Row`s in Scala and Java API. These operations are
// MAGIC also referred as “untyped transformations” in contrast to “typed
// MAGIC transformations” come with strongly typed Scala/Java Datasets.
// MAGIC 
// MAGIC Here we include some basic examples of structured data processing using
// MAGIC Datasets:

// COMMAND ----------

// This import is needed to use the $-notation
import spark.implicits._
// Print the schema in a tree format
df.printSchema()

// COMMAND ----------

// Select only the "name" column
df.select("name").show()

// COMMAND ----------

// Select everybody, but increment the age by 1
df.select($"name", $"age" + 1).show()

// COMMAND ----------

// Select people older than 21
df.filter($"age" > 21).show()

// COMMAND ----------

// Count people by age
df.groupBy("age").count().show()

// COMMAND ----------

// MAGIC %md
// MAGIC Find full example code at
// MAGIC - https://raw.githubusercontent.com/apache/spark/master/examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala
// MAGIC in the Spark repo.
// MAGIC 
// MAGIC For a complete list of the types of operations that can be performed on
// MAGIC a Dataset, refer to the [API Documentation](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/Dataset.html).
// MAGIC 
// MAGIC In addition to simple column references and expressions, Datasets also
// MAGIC have a rich library of functions including string manipulation, date
// MAGIC arithmetic, common math operations and more. The complete list is
// MAGIC available in the [DataFrame Function Reference](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/functions$.html).

// COMMAND ----------

// MAGIC %md
// MAGIC Running SQL Queries Programmatically
// MAGIC ------------------------------------
// MAGIC 
// MAGIC The `sql` function on a
// MAGIC `SparkSession` enables applications to run SQL
// MAGIC queries programmatically and returns the result as a
// MAGIC `DataFrame`.

// COMMAND ----------

// Register the DataFrame as a SQL temporary view
df.createOrReplaceTempView("people")

// COMMAND ----------

val sqlDF = spark.sql("SELECT * FROM people")
sqlDF.show()

// COMMAND ----------

// MAGIC %md
// MAGIC Global Temporary View
// MAGIC ---------------------
// MAGIC 
// MAGIC Temporary views in Spark SQL are session-scoped and will disappear if
// MAGIC the session that creates it terminates. If you want to have a temporary
// MAGIC view that is shared among all sessions and keep alive until the Spark
// MAGIC application terminates, you can create a global temporary view. Global
// MAGIC temporary view is tied to a system preserved database
// MAGIC `global_temp`, and we must use the qualified name to
// MAGIC refer it, e.g. `SELECT * FROM global_temp.view1`.

// COMMAND ----------

// Register the DataFrame as a global temporary view
df.createGlobalTempView("people")

// COMMAND ----------

// Global temporary view is tied to a system preserved database `global_temp`
spark.sql("SELECT * FROM global_temp.people").show()

// COMMAND ----------

// Global temporary view is cross-session
spark.newSession().sql("SELECT * FROM global_temp.people").show()

// COMMAND ----------

// MAGIC %md
// MAGIC Creating Datasets
// MAGIC -----------------
// MAGIC 
// MAGIC Datasets are similar to RDDs, however, instead of using Java
// MAGIC serialization or Kryo they use a specialized [Encoder](https://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/Encoder.html)
// MAGIC to serialize the objects for processing or transmitting over the
// MAGIC network. While both encoders and standard serialization are responsible
// MAGIC for turning an object into bytes, encoders are code generated
// MAGIC dynamically and use a format that allows Spark to perform many
// MAGIC operations like filtering, sorting and hashing without deserializing the
// MAGIC bytes back into an object.

// COMMAND ----------

case class Person(name: String, age: Long)

// Encoders are created for case classes
val caseClassDS = Seq(Person("Andy", 32)).toDS()
caseClassDS.show()

// COMMAND ----------

// Encoders for most common types are automatically provided by importing spark.implicits._
val primitiveDS = Seq(1, 2, 3).toDS()
primitiveDS.map(_ + 1).collect() // Returns: Array(2, 3, 4)


// COMMAND ----------

// DataFrames can be converted to a Dataset by providing a class. Mapping will be done by name
val path = "dbfs:///datasets/spark-examples/people.json"
val peopleDS = spark.read.json(path).as[Person]
peopleDS.show()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **Dataset is not available directly in PySpark or SparkR**.
// MAGIC 
// MAGIC Interoperating with RDDs
// MAGIC ------------------------
// MAGIC 
// MAGIC Spark SQL supports two different methods for converting existing RDDs
// MAGIC into Datasets. The first method uses reflection to infer the schema of
// MAGIC an RDD that contains specific types of objects. This reflection-based
// MAGIC approach leads to more concise code and works well when you already know
// MAGIC the schema while writing your Spark application.
// MAGIC 
// MAGIC The second method for creating Datasets is through a programmatic
// MAGIC interface that allows you to construct a schema and then apply it to an
// MAGIC existing RDD. While this method is more verbose, it allows you to
// MAGIC construct Datasets when the columns and their types are not known until
// MAGIC runtime.
// MAGIC 
// MAGIC ### Inferring the Schema Using Reflection
// MAGIC 
// MAGIC 
// MAGIC The Scala interface for Spark SQL supports automatically converting an
// MAGIC RDD containing case classes to a DataFrame. The case class defines the
// MAGIC schema of the table. The names of the arguments to the case class are
// MAGIC read using reflection and become the names of the columns. Case classes
// MAGIC can also be nested or contain complex types such as
// MAGIC `Seq`s or `Array`s. This RDD can
// MAGIC be implicitly converted to a DataFrame and then be registered as a
// MAGIC table. Tables can be used in subsequent SQL statements.

// COMMAND ----------

// the following lines merely fetch the file from the URL and load it into the dbfs for us to try in databricks
// getLines from the file at the URL
val peopleTxtLinesFromURL = scala.io.Source.fromURL("https://raw.githubusercontent.com/apache/spark/master/examples/src/main/resources/people.txt").getLines
// remove any pre-existing file at the dbfs location
dbutils.fs.rm("dbfs:///datasets/spark-examples/people.txt",recurse=true)
// convert the lines fetched from the URL to a Seq, then make it a RDD of String and finally save it as textfile to dbfs
sc.parallelize(peopleTxtLinesFromURL.toSeq).saveAsTextFile("dbfs:///datasets/spark-examples/people.txt")
// read the text file we just saved and see what it has
sc.textFile("dbfs:///datasets/spark-examples/people.txt").collect.mkString("\n")

// COMMAND ----------

sc.textFile("dbfs:///datasets/spark-examples/people.txt").collect.mkString("\n")

// COMMAND ----------

// For implicit conversions from RDDs to DataFrames
import spark.implicits._

// make a case class
case class Person(name: String, age: Long)

// Create an RDD of Person objects from a text file, convert it to a Dataframe
val peopleDF = spark.sparkContext
  .textFile("dbfs:///datasets/spark-examples/people.txt")
  .map(_.split(","))
  .map(attributes => Person(attributes(0), attributes(1).trim.toLong))
  //.map(attributes => Person(attributes(0), attributes(1).trim.toLong))
  .toDF()

// COMMAND ----------

peopleDF.show

// COMMAND ----------

// Register the DataFrame as a temporary view
peopleDF.createOrReplaceTempView("people")

// COMMAND ----------

// SQL statements can be run by using the sql methods provided by Spark
val teenagersDF = spark.sql("SELECT name, age FROM people WHERE age BETWEEN 13 AND 19")

// COMMAND ----------

teenagersDF.show()

// COMMAND ----------

// The columns of a row in the result can be accessed by field index
teenagersDF.map(teenager => "Name: " + teenager(0)).show()

// COMMAND ----------

// or by field name
teenagersDF.map(teenager => "Name: " + teenager.getAs[String]("name")).show()

// COMMAND ----------

// No pre-defined encoders for Dataset[Map[K,V]], define explicitly
implicit val mapEncoder = org.apache.spark.sql.Encoders.kryo[Map[String, Any]]

// COMMAND ----------

// Primitive types and case classes can be also defined as
// import more classes here...
//implicit val stringIntMapEncoder: Encoder[Map[String, Any]] = ExpressionEncoder()

// COMMAND ----------

// row.getValuesMap[T] retrieves multiple columns at once into a Map[String, T]
teenagersDF.map(teenager => teenager.getValuesMap[Any](List("name", "age"))).collect()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Programmatically Specifying the Schema
// MAGIC 
// MAGIC 
// MAGIC When case classes cannot be defined ahead of time (for example, the
// MAGIC structure of records is encoded in a string, or a text dataset will be
// MAGIC parsed and fields will be projected differently for different users), a
// MAGIC `DataFrame` can be created programmatically with
// MAGIC three steps.
// MAGIC 
// MAGIC 1.  Create an RDD of `Row`s from the original RDD;
// MAGIC 2.  Create the schema represented by a `StructType`
// MAGIC     matching the structure of `Row`s in the RDD
// MAGIC     created in Step 1.
// MAGIC 3.  Apply the schema to the RDD of `Row`s via
// MAGIC     `createDataFrame` method provided by
// MAGIC     `SparkSession`.
// MAGIC 
// MAGIC For example:

// COMMAND ----------

import org.apache.spark.sql.Row

import org.apache.spark.sql.types._

// Create an RDD
val peopleRDD = spark.sparkContext.textFile("dbfs:///datasets/spark-examples/people.txt")

// COMMAND ----------

// The schema is encoded in a string
val schemaString = "name age"

// COMMAND ----------

// Generate the schema based on the string of schema
val fields = schemaString.split(" ")
  .map(fieldName => StructField(fieldName, StringType, nullable = true))

// COMMAND ----------

val schema = StructType(fields)

// COMMAND ----------

// Convert records of the RDD (people) to Rows
val rowRDD = peopleRDD
  .map(_.split(","))
  .map(attributes => Row(attributes(0), attributes(1).trim))

// COMMAND ----------

// Apply the schema to the RDD
val peopleDF = spark.createDataFrame(rowRDD, schema)

// COMMAND ----------

peopleDF.show

// COMMAND ----------

// Creates a temporary view using the DataFrame
peopleDF.createOrReplaceTempView("people")

// COMMAND ----------

// SQL can be run over a temporary view created using DataFrames
val results = spark.sql("SELECT name FROM people")

// COMMAND ----------

results.show

// COMMAND ----------

// The results of SQL queries are DataFrames and support all the normal RDD operations
// The columns of a row in the result can be accessed by field index or by field name
results.map(attributes => "Name: " + attributes(0)).show()

// COMMAND ----------

// MAGIC %md
// MAGIC Find full example code at
// MAGIC - https://raw.githubusercontent.com/apache/spark/master/examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala
// MAGIC in the Spark repo.

// COMMAND ----------

// MAGIC %md
// MAGIC Scalar Functions
// MAGIC ----------------
// MAGIC 
// MAGIC Scalar functions are functions that return a single value per row, as
// MAGIC opposed to aggregation functions, which return a value for a group of
// MAGIC rows. Spark SQL supports a variety of [Built-in Scalar Functions](https://spark.apache.org/docs/latest/sql-ref-functions.html#scalar-functions).
// MAGIC It also supports [User Defined Scalar Functions](https://spark.apache.org/docs/latest/sql-ref-functions-udf-scalar.html).

// COMMAND ----------

// MAGIC %md
// MAGIC Aggregate Functions
// MAGIC -------------------
// MAGIC 
// MAGIC Aggregate functions are functions that return a single value on a group
// MAGIC of rows. The [Built-in Aggregation Functions](https://spark.apache.org/docs/latest/sql-ref-functions-builtin.html#aggregate-functions)
// MAGIC provide common aggregations such as `count()`, `countDistinct()`, `avg()`, `max()`, `min()`, etc. Users are
// MAGIC not limited to the predefined aggregate functions and can create their
// MAGIC own. For more details about user defined aggregate functions, please
// MAGIC refer to the documentation of [User Defined Aggregate Functions](https://spark.apache.org/docs/latest/sql-ref-functions-udf-aggregate.html).
