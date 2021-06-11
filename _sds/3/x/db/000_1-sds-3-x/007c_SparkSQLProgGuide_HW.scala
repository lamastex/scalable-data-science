// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [http://spark.apache.org/docs/latest/sql-programming-guide.html](http://spark.apache.org/docs/latest/sql-programming-guide.html) by Ivan Sadikov and Raazesh Sainudiin.
// MAGIC 
// MAGIC # Getting Started - Exercise
// MAGIC 
// MAGIC After having gone through the simple example dataset in the programming guide, let's try a slightly larger dataset next.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Let us first create a table of social media usage from NYC 
// MAGIC 
// MAGIC > See the **Load Data** section to create this `social_media_usage` table from raw data.
// MAGIC 
// MAGIC First let's make sure this table is available for us. If you don't see `social_media_usage` as a `name`d table in the output of the next cell then we first need to ingest this dataset. Let's do it using the databricks' GUI for creating `Data` as done next.

// COMMAND ----------

// Let's find out what tables are already available for loading
spark.catalog.listTables.show(50)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## NYC Social Media Usage Data
// MAGIC 
// MAGIC This dataset is from [https://datahub.io/JohnSnowLabs/nyc-social-media-usage#readme](https://datahub.io/JohnSnowLabs/nyc-social-media-usage#readme)
// MAGIC 
// MAGIC The Demographic Reports are produced by the Economic, Demographic and Statistical Research unit within the Countywide Service Integration and Planning Management (CSIPM) Division of the Fairfax County Department of Neighborhood and Community Services. Information produced by the Economic, Demographic and Statistical Research unit is used by every county department, board, authority and the Fairfax County Public Schools. In addition to the small area estimates and forecasts, state and federal data on Fairfax County are collected and summarized, and special studies and Quantitative research are conducted by the unit.
// MAGIC 
// MAGIC We are going to fetch this data, with slightly simplified column names, from the following URL:
// MAGIC 
// MAGIC - http://lamastex.org/datasets/public/NYCUSA/social-media-usage.csv
// MAGIC 
// MAGIC To turn the dataset into a registered table we will load it using the GUI as follows:
// MAGIC 
// MAGIC - Download it to your local machine / laptop and then use the 'Data' button on the left to upload it (we will try this method now).
// MAGIC   - This will put your data in the `Filestore` in databricks' distributed file system.
// MAGIC 
// MAGIC ### Overview 
// MAGIC 
// MAGIC Below we will show you how to create and query a table or DataFrame that you uploaded to DBFS. [DBFS](https://docs.databricks.com/user-guide/dbfs-databricks-file-system.html) is a Databricks File System (their distributed file system) that allows you to store data for querying inside of Databricks. This notebook assumes that you have a file already inside of DBFS that you would like to read from.
// MAGIC 
// MAGIC In other setups, you can have the data in s3 (say in AWS) or in hdfs in your hadoop cluster, etc.
// MAGIC 
// MAGIC Alternatively, you can use `curl` or `wget` to download it to the local file system in `/databricks/driver` and then load it into `dbfs`, after this you can use read it via `spark` session into a dataframe and register it as a hive table. 
// MAGIC 
// MAGIC You can also get the data directly from here (but in this case you need to change the column names in the databricks Data upload GUI or programmatically to follow this notebook):
// MAGIC 
// MAGIC - http://datahub.io/JohnSnowLabs/nyc-social-media-usage

// COMMAND ----------

// MAGIC %md
// MAGIC ## Load Data
// MAGIC ### How to uoload csv file and make a table in databricks
// MAGIC 
// MAGIC Okay, so how did we actually make table `social_media_usage`? Databricks allows us to upload/link external data and make it available as registerd SQL table. It involves several steps:
// MAGIC 
// MAGIC 1. Dowload this `social-media-usage.csv` file from the following URL to your laptop:
// MAGIC   - http://lamastex.org/datasets/public/NYCUSA/social-media-usage.csv
// MAGIC - Go to Databricks cloud (where you log in to use Databricks notebooks) and open tab **Data** on the left panel
// MAGIC - On the very top of the left sub-menu you will see button **+Add Data**, click on it
// MAGIC - Choose **Upload File** for Data Sources by **Browse** or **Drag and Drop**, where **File** means any file (Parquet, Avro, CSV), but it works the best with CSV format
// MAGIC - Upload `social-media-usage.csv` file you just downloaded to databricks
// MAGIC - Just note the path to the uploaded file, for example in my case:
// MAGIC  > File uploaded to `/FileStore/tables/social_media_usage.csv`

// COMMAND ----------


// File location and type
// You may need to change the file_location "social_media_usage-5dbee.csv" depending on your location given by
// File uploaded to /FileStore/tables/social_media_usage.csv
val file_location = "/FileStore/tables/social_media_usage.csv"
val file_type = "csv"

// CSV options
val infer_schema = "true"
val first_row_is_header = "true"
val delimiter = ","

// The applied options are for CSV files. For other file types, these will be ignored.
val socialMediaDF = spark.read.format(file_type) 
  .option("inferSchema", infer_schema) 
  .option("header", first_row_is_header) 
  .option("sep", delimiter) 
  .load(file_location)

socialMediaDF.show(10)

// COMMAND ----------

// Let's create a view or table

val temp_table_name = "social_media_usage"

socialMediaDF.createOrReplaceTempView(temp_table_name)

// COMMAND ----------

// Let's find out what tables are already available for loading
spark.catalog.listTables.show(100)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC With this registered as a temporary view, `social_media_usage` will only be available to this particular notebook. 
// MAGIC 
// MAGIC If you'd like other users to be able to query this table (in the databricks professional shard - not the free community edition; or in a managed on-premise cluster), you can also create a table from the DataFrame.
// MAGIC 
// MAGIC Once saved, this table will persist across cluster restarts as well as allow various users across different notebooks to query this data.
// MAGIC To do so, choose your table name and use `saveAsTable` as done in the next cell.

// COMMAND ----------

val permanent_table_name = "social_media_usage"
socialMediaDF.write.format("parquet").saveAsTable(permanent_table_name)

// COMMAND ----------

// Let's find out what tables are already available for loading
// spark.catalog.listTables.show(100)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC It looks like the table `social_media_usage` is available as a permanent table (`isTemporary` set as `false`), if you have not uncommented the last line in the previous cell (otherwise it will be available from a parquet file as a permanent table - we will see more about parquet in the sequel). 
// MAGIC 
// MAGIC Next let us do the following:
// MAGIC 
// MAGIC * load this table as a DataFrame (yes, the dataframe already exists as `socialMediaDF`, but we want to make a new DataFrame directly from the table)
// MAGIC * print its schema and
// MAGIC * show the first 20 rows.

// COMMAND ----------

spark.catalog.listTables.show(100)

// COMMAND ----------

val df = spark.table("social_media_usage") // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC As you can see the immutable value ``df`` is a DataFrame and more specifically it is:
// MAGIC 
// MAGIC > ``org.apache.spark.sql.DataFrame = [agency: string, platform: string, url: string, date: timestamp, visits: integer]``.

// COMMAND ----------

// MAGIC %md
// MAGIC Now let us print schema of the DataFrame ``df`` and have a look at the actual data:

// COMMAND ----------

// Ctrl+Enter
df.printSchema() // prints schema of the DataFrame
df.show() // shows first n (default is 20) rows

// COMMAND ----------

// MAGIC %md
// MAGIC > Note that `(nullable = true)` simply means if the value is allowed to be `null`.
// MAGIC 
// MAGIC Let us count the number of rows in ``df``.

// COMMAND ----------

df.count() // Ctrl+Enter to get 5898

// COMMAND ----------

// MAGIC %md
// MAGIC So there are 5899 records or rows in the DataFrame `df`. Pretty good! You can also select individual columns using so-called DataFrame API, as follows:

// COMMAND ----------

val platforms = df.select("platform") // Shift+Enter

// COMMAND ----------

platforms.count() // Shift+Enter to count the number of rows

// COMMAND ----------

platforms.show(5) // Ctrl+Enter to show top 5 rows

// COMMAND ----------

// MAGIC %md
// MAGIC You can also apply ``.distinct()`` to extract only unique entries as follows:

// COMMAND ----------

val uniquePlatforms = df.select("platform").distinct() // Shift+Enter

// COMMAND ----------

uniquePlatforms.count() // Ctrl+Enter to count the number of distinct platforms

// COMMAND ----------

// MAGIC %md
// MAGIC Let's see all the rows of the DataFrame `uniquePlatforms`. 
// MAGIC 
// MAGIC > Note that `display(uniquePlatforms)` unlike `uniquePlatforms.show()` displays all rows of the DataFrame + gives you ability to select different view, e.g. charts.

// COMMAND ----------

display(uniquePlatforms) // Ctrl+Enter to show all rows; use the scroll-bar on the right of the display to see all platforms

// COMMAND ----------

// MAGIC %md
// MAGIC ### Spark SQL and DataFrame API
// MAGIC Spark SQL provides DataFrame API that can perform relational operations on both external data sources and internal collections, which is similar to widely used data frame concept in R, but evaluates operations support lazily (remember RDDs?), so that it can perform relational optimizations. This API is also available in Java, Python and R, but some functionality may not be available, although with every release of Spark people minimize this gap. 
// MAGIC 
// MAGIC So we give some examples how to query data in Python and R, but continue with Scala. You can do all DataFrame operations in this notebook using Python or R.

// COMMAND ----------

// MAGIC %py
// MAGIC # Ctrl+Enter to evaluate this python cell, recall '#' is the pre-comment character in python
// MAGIC # Using Python to query our "social_media_usage" table
// MAGIC pythonDF = spark.table("social_media_usage").select("platform").distinct()
// MAGIC pythonDF.show(3)

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Ctrl+Enter to achieve the same result using standard SQL syntax!
// MAGIC select distinct platform from social_media_usage

// COMMAND ----------

// MAGIC %md
// MAGIC Now it is time for some tips around how you use ``select`` and what the difference is between ``$"a"``, ``col("a")``, ``df("a")``. 
// MAGIC 
// MAGIC As you probably have noticed by now, you can specify individual columns to select by providing String values in select statement. But sometimes you need to:
// MAGIC - distinguish between columns with the same name
// MAGIC - use it to filter (actually you can still filter using full String expression)
// MAGIC - do some "magic" with joins and user-defined functions (this will be shown later)
// MAGIC 
// MAGIC So Spark gives you ability to actually specify columns when you select. Now the difference between all those three notations is ... none, those things are just aliases for a `Column` in Spark SQL, which means following expressions yield the same result:

// COMMAND ----------

// Using string expressions
df.select("agency", "visits")

// Using "$" alias for column
df.select($"agency", $"visits")

// Using "col" alias for column
df.select(col("agency"), col("visits"))

// Using DataFrame name for column
df.select(df("agency"), df("visits"))


// COMMAND ----------

// MAGIC %md
// MAGIC This "same-difference" applies to filtering, i.e. you can either use full expression to filter, or column as shown in the following example:

// COMMAND ----------

// Using column to filter
df.select("visits").filter($"visits" > 100)

// Or you can use full expression as string
df.select("visits").filter("visits > 100")

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC > Note that ``$"visits" > 100`` expression looks amazing, but under the hood it is just another column, and it equals to ``df("visits").>(100)``, where, thanks to Scala paradigm ``>`` is just another function that you can define.

// COMMAND ----------

val sms = df.select($"agency", $"platform", $"visits").filter($"platform" === "SMS")
sms.show() // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC Again you could have written the query above using any column aliases or String names or even writing the query directly. 
// MAGIC 
// MAGIC For example, we can do it using String names, as follows:

// COMMAND ----------

// Ctrl+Enter Note that we are using "platform = 'SMS'" since it will be evaluated as actual SQL
val sms = df.select(df("agency"), df("platform"), df("visits")).filter("platform = 'SMS'")
sms.show(5)

// COMMAND ----------

// MAGIC %md 
// MAGIC Refer to the [DataFrame API](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) for more detailed API. In addition to simple column references and expressions, DataFrames also have a rich library of functions including string manipulation, date
// MAGIC arithmetic, common math operations and more. The complete list is available in the [DataFrame Function Reference](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$).

// COMMAND ----------

// MAGIC %md
// MAGIC Let's next explore some of the functionality that is available by transforming this DataFrame `df` into a new DataFrame called `fixedDF`.
// MAGIC 
// MAGIC * First, note that some columns are not exactly what we want them to be. 
// MAGIC   * visits should not contain null values, but `0`s instead. 
// MAGIC   
// MAGIC * Let us fix it using some code that is briefly explained here (don't worry if you don't get it completely now, you will get the hang of it by playing more)
// MAGIC     * The `coalesce` function is similar to `if-else` statement, i.e., if first column in expression is `null`, then the value of the second column is used and so on. 
// MAGIC     * `lit` just means column of constant value (`lit`erally speaking!).
// MAGIC     * we also remove `TOTAL` value from `platform` column.

// COMMAND ----------

// Ctrl+Enter to make fixedDF

// import the needed sql functions
import org.apache.spark.sql.functions.{coalesce, lit}

// make the fixedDF DataFrame
val fixedDF = df.
   select(
     $"agency", 
     $"platform", 
     $"url", 
     $"date", 
     coalesce($"visits", lit(0)).as("visits"))
    .filter($"platform" =!= "TOTAL")

fixedDF.printSchema() // print its schema 
// and show the top 20 records fully
fixedDF.show(false) // the false argument does not truncate the rows, so you will not see something like this "anot..."

// COMMAND ----------

// MAGIC %md
// MAGIC Okay, this is better, but `url`s are still inconsistent. 
// MAGIC 
// MAGIC Let's fix this by writing our own UDF (user-defined function) to deal with special cases. 
// MAGIC 
// MAGIC Note that if you **CAN USE Spark functions library**, do it. But for the sake of the example, custom UDF is shown below.
// MAGIC 
// MAGIC We take value of a column as String type and return the same String type, but ignore values that do not start with `http`.

// COMMAND ----------

// Ctrl+Enter to evaluate this UDF which takes a input String called "value"
// and converts it into lower-case if it begins with http and otherwise leaves it as null, so we sort of remove non valid web-urls
val cleanUrl = udf((value: String) => if (value != null && value.startsWith("http")) value.toLowerCase() else null)

// COMMAND ----------

// MAGIC %md
// MAGIC Let us apply our UDF on `fixedDF` to create a new DataFrame called `cleanedDF` as follows:

// COMMAND ----------

// Ctrl+Enter
val cleanedDF = fixedDF.select($"agency", $"platform", cleanUrl($"url").as("url"), $"date", $"visits")

// COMMAND ----------

// MAGIC %md
// MAGIC Now, let's check that it actually worked by seeing the first 5 rows of the `cleanedDF` whose `url` `isNull` and `isNotNull`, as follows:

// COMMAND ----------

// Shift+Enter
cleanedDF.filter($"url".isNull).show(5)

// COMMAND ----------

// Ctrl+Enter
cleanedDF.filter($"url".isNotNull).show(5, false) // false in .show(5, false) shows rows untruncated

// COMMAND ----------

// MAGIC %md
// MAGIC Now there is a suggestion from you manager's manager's manager that due to some perceived privacy concerns we want to replace `agency` with some unique identifier.
// MAGIC 
// MAGIC So we need to do the following:
// MAGIC 
// MAGIC * create unique list of agencies with ids and 
// MAGIC * join them with main DataFrame. 
// MAGIC 
// MAGIC Sounds easy, right? Let's do it.

// COMMAND ----------

// Crtl+Enter
// Import Spark SQL function that will give us unique id across all the records in this DataFrame
import org.apache.spark.sql.functions.monotonically_increasing_id

// We append column as SQL function that creates unique ids across all records in DataFrames 
val agencies = cleanedDF.select(cleanedDF("agency"))
                        .distinct()
                        .withColumn("id", monotonically_increasing_id())
agencies.show(5)

// COMMAND ----------

// MAGIC %md
// MAGIC Those who want to understand left/right inner/outer joins can see the video lectures in Module 3 of Anthony Joseph's Introduction to Big data edX course.

// COMMAND ----------

// Ctrl+Enter
// And join with the rest of the data, note how join condition is specified 
val anonym = cleanedDF.join(agencies, cleanedDF("agency") === agencies("agency"), "inner").select("id", "platform", "url", "date", "visits")

// We also cache DataFrame since it can be quite expensive to recompute join
anonym.cache()

// Display result
anonym.show(5)

// COMMAND ----------

spark.catalog.listTables().show() // look at the available tables

// COMMAND ----------

// MAGIC %sql 
// MAGIC -- to remove a TempTable if it exists already
// MAGIC drop table if exists anonym

// COMMAND ----------

// Register table for Spark SQL, we also import "month" function 
import org.apache.spark.sql.functions.month

anonym.createOrReplaceTempView("anonym")

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Interesting. Now let's do some aggregation. Display platform, month, visits
// MAGIC -- Date column allows us to extract month directly
// MAGIC 
// MAGIC select platform, month(date) as month, sum(visits) as visits from anonym group by platform, month(date)

// COMMAND ----------

// MAGIC %md
// MAGIC Note, that we could have done aggregation using DataFrame API instead of Spark SQL.

// COMMAND ----------

// MAGIC %md
// MAGIC Alright, now let's see some *cool* operations with window functions. 
// MAGIC 
// MAGIC Our next task is to compute ``(daily visits / monthly average)`` for all platforms.

// COMMAND ----------

import org.apache.spark.sql.functions.{dayofmonth, month, row_number, sum}
import org.apache.spark.sql.expressions.Window

val coolDF = anonym.select($"id", $"platform", dayofmonth($"date").as("day"), month($"date").as("month"), $"visits").
  groupBy($"id", $"platform", $"day", $"month").agg(sum("visits").as("visits"))

// Run window aggregation on visits per month and platform
val window = coolDF.select($"id", $"day", $"visits", sum($"visits").over(Window.partitionBy("platform", "month")).as("monthly_visits"))

// Create and register percent table
val percent = window.select($"id", $"day", ($"visits" / $"monthly_visits").as("percent"))

percent.createOrReplaceTempView("percent")

// COMMAND ----------

// MAGIC %sql
// MAGIC 
// MAGIC -- A little bit of visualization as result of our efforts
// MAGIC select id, day, `percent` from percent where `percent` > 0.3 and day = 2

// COMMAND ----------

// MAGIC %sql
// MAGIC -- You also could just use plain SQL to write query above, note that you might need to group by id and day as well.
// MAGIC with aggr as (
// MAGIC   select id, dayofmonth(date) as day, visits / sum(visits) over (partition by (platform, month(date))) as percent
// MAGIC   from anonym
// MAGIC )
// MAGIC select * from aggr where day = 2 and percent > 0.3

// COMMAND ----------



// COMMAND ----------

// MAGIC %md 
// MAGIC ## Interoperating with RDDs
// MAGIC 
// MAGIC Spark SQL supports two different methods for converting existing RDDs into DataFrames. The first method uses reflection to infer the schema of an RDD that contains specific types of objects. This reflection based approach leads to more concise code and works well when you already know the schema. 
// MAGIC 
// MAGIC The second method for creating DataFrames is through a programmatic interface that allows you to construct a schema and then apply it to an
// MAGIC existing RDD. While this method is more verbose, it allows you to construct DataFrames when the columns and their types are not known until runtime.
// MAGIC 
// MAGIC ### Inferring the Schema Using Reflection
// MAGIC 
// MAGIC The Scala interface for Spark SQL supports automatically converting an RDD containing case classes to a DataFrame. The case class defines the
// MAGIC schema of the table. The names of the arguments to the case class are read using reflection and become the names of the columns. Case classes
// MAGIC can also be nested or contain complex types such as Sequences or Arrays. This RDD can be implicitly converted to a DataFrame and then be registered as a table.

// COMMAND ----------

// Define case class that will be our schema for DataFrame
case class Hubot(name: String, year: Int, manufacturer: String, version: Array[Int], details: Map[String, String])

// You can process a text file, for example, to convert every row to our Hubot, but we will create RDD manually
val rdd = sc.parallelize(
  Array(
    Hubot("Jerry", 2015, "LCorp", Array(1, 2, 3), Map("eat" -> "yes", "sleep" -> "yes", "drink" -> "yes")),
    Hubot("Mozart", 2010, "LCorp", Array(1, 2), Map("eat" -> "no", "sleep" -> "no", "drink" -> "no")),
    Hubot("Einstein", 2012, "LCorp", Array(1, 2, 3), Map("eat" -> "yes", "sleep" -> "yes", "drink" -> "no"))
  )
)

// COMMAND ----------

// In order to convert RDD into DataFrame you need to do this:
val hubots = rdd.toDF()

// Display DataFrame, note how array and map fields are displayed
hubots.printSchema()
hubots.show()

// COMMAND ----------

// You can query complex type the same as you query any other column
// By the way you can use `sql` function to invoke Spark SQL to create DataFrame
hubots.createOrReplaceTempView("hubots")

val onesThatEat = sqlContext.sql("select name, details.eat from hubots where details.eat = 'yes'")

onesThatEat.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ### Programmatically Specifying the Schema
// MAGIC 
// MAGIC When case classes cannot be defined ahead of time (for example, the structure of records is encoded in a string, or a text dataset will be parsed and fields will be projected differently for different users), a `DataFrame` can be created programmatically with three steps.
// MAGIC 
// MAGIC 1.  Create an RDD of `Row`s from the original RDD
// MAGIC 2.  Create the schema represented by a [StructType](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.types.StructType) and [StructField](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.types.StructField) classes matching the structure of `Row`s in the RDD created in Step 1.
// MAGIC 3.  Apply the schema to the RDD of `Row`s via `createDataFrame` method provided by `SQLContext`.

// COMMAND ----------

import org.apache.spark.sql.types._

// Let's say we have an RDD of String and we need to convert it into a DataFrame with schema "name", "year", and "manufacturer"
// As you can see every record is space-separated.
val rdd = sc.parallelize(
  Array(
    "Jerry 2015 LCorp",
    "Mozart 2010 LCorp",
    "Einstein 2012 LCorp"
  )
)

// Create schema as StructType //
val schema = StructType(
  StructField("name", StringType, false) :: 
  StructField("year", IntegerType, false) :: 
  StructField("manufacturer", StringType, false) :: 
  Nil
)

// Prepare RDD[Row]
val rows = rdd.map { entry => 
  val arr = entry.split("\\s+")
  val name = arr(0)
  val year = arr(1).toInt
  val manufacturer = arr(2)
  
  Row(name, year, manufacturer)
}

// Create DataFrame
val bots = sqlContext.createDataFrame(rows, schema)
bots.printSchema()
bots.show()

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Creating Datasets
// MAGIC A [Dataset](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Dataset) is a strongly-typed, immutable collection of objects that are mapped to a relational schema. At the core of the Dataset API is a new concept called an [encoder](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.Encoder), which is responsible for converting between JVM objects and tabular representation. The tabular representation is stored using Spark’s internal Tungsten binary format, allowing for operations on serialized data and improved memory utilization.  Spark 2.2 comes with support for automatically generating encoders for a wide variety of types, including primitive types (e.g. String, Integer, Long), and Scala case classes.
// MAGIC 
// MAGIC > Simply put, you will get all the benefits of DataFrames with fair amount of flexibility of RDD API.

// COMMAND ----------

// We can start working with Datasets by using our "hubots" table

// To create Dataset from DataFrame do this (assuming that case class Hubot exists):
val ds = hubots.as[Hubot]
ds.show()


// COMMAND ----------

// MAGIC %md
// MAGIC > **Side-note:** Dataset API is first-class citizen in Spark, and DataFrame is an alias for Dataset[Row]. Note that Python and R use DataFrames (since they are dynamically typed), but it is essentially a Dataset.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Finally
// MAGIC DataFrames and Datasets can simplify and improve most of the applications pipelines by bringing concise syntax and performance optimizations. We would highly recommend you to check out the official API documentation, specifically around 
// MAGIC 
// MAGIC * [DataFrame API](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame), 
// MAGIC * [Spark SQL functions library](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$), 
// MAGIC * [GroupBy clause and aggregated functions](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.GroupedData). 
// MAGIC 
// MAGIC Unfortunately, this is just _a getting started quickly_ course, and we skip features like custom aggregations, types, pivoting, etc., but if you are keen to know then start from the links above and this notebook and others in this directory.