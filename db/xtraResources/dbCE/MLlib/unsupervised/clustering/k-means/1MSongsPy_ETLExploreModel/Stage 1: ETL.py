# Databricks notebook source exported at Tue, 15 Mar 2016 02:22:28 UTC
# MAGIC %md
# MAGIC **SOURCE:** This is from the databricks Community Edition and has been added to this databricks shard at [Workspace -> scalable-data-science -> xtraResources -> dbCE -> MLlib -> unsupervised -> clustering -> k-means -> 1MSongsPy_ETLExploreModel](/#workspace/scalable-data-science/xtraResources/dbCE/MLlib/unsupervised/clustering/k-means/1MSongsPy_ETLExploreModel) as extra resources for the project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) that is prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand), and *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome). 

# COMMAND ----------

# MAGIC %md
# MAGIC **If you see ![](http://training.databricks.com/databricks_guide/ImportNotebookIcon3.png) at the top-left, click on the link to import this notebook in order to run it.** 

# COMMAND ----------

# MAGIC %md # Stage 1: Parsing songs data
# MAGIC 
# MAGIC ![ETL](http://training.databricks.com/databricks_guide/end-to-end-01.png)
# MAGIC 
# MAGIC This is the first notebook in this tutorial. In this notebook we will read data from DBFS (DataBricks FileSystem). We will parse data and load it as a table that can be readily used in following notebooks.
# MAGIC 
# MAGIC By going through this notebook you can expect to learn how to read distributed data as an RDD, how to transform RDDs, and how to construct a Spark DataFrame from an RDD and register it as a table.
# MAGIC 
# MAGIC We first explore different files in our distributed file system. We use a header file to construct a Spark `Schema` object. We write a function that takes the header and casts strings in each line of our data to corresponding types. Once we run this function on the data we find that it fails on some corner caes. We update our function and finally get a parsed RDD. We combine that RDD and the Schema to construct a DataFame and register it as a temporary table in SparkSQL.

# COMMAND ----------

# MAGIC %md ### Text data files are stored in `dbfs:/databricks-datasets/songs/data-001` 
# MAGIC You can conveniently list files on distributed file system (DBFS, S3 or HDFS) using `%fs` commands.

# COMMAND ----------

# MAGIC %fs ls /databricks-datasets/songs/data-001/

# COMMAND ----------

# MAGIC %md As you can see in the listing we have data files and a single header file. The header file seems interesting and worth a first inspection it first. The file is 377 bytes, therefore it is safe to collect the entire content of the file in the notebook. 

# COMMAND ----------

sc.textFile("databricks-datasets/songs/data-001/header.txt").collect()

# COMMAND ----------

# MAGIC %md As seen above each line in the header consists of a name and a type separated by colon. We will need to parse the header file as follows:

# COMMAND ----------

header = sc.textFile("/databricks-datasets/songs/data-001/header.txt").map(lambda line: line.split(":")).collect()

# COMMAND ----------

# MAGIC %md Now we turn to data files. First, step is inspecting the first line of data to inspect its format.

# COMMAND ----------

dataRDD = sc.textFile("/databricks-datasets/songs/data-001/part-000*")

# COMMAND ----------

dataRDD.take(3)

# COMMAND ----------

# MAGIC %md Each line of data consists of multiple fields separated by `\t`. With that information and what we learned from the header file, we set out to parse our data. To do so, we build a function that takes a line of text and returns an array of parsed fields.
# MAGIC * If header indicates the type is int, we cast the token to integer
# MAGIC * If header indicates the type is double, we cast the token to float
# MAGIC * Otherwise we return the string

# COMMAND ----------

def parseLine(line):
  tokens = zip(line.split("\t"), header)
  parsed_tokens = []
  for token in tokens:
    token_type = token[1][1]
    if token_type == 'double':
      parsed_tokens.append(float(token[0]))
    elif token_type == 'int':
      parsed_tokens.append(int(token[0]))
    else:
      parsed_tokens.append(token[0])
  return parsed_tokens

# COMMAND ----------

# MAGIC %md With this function we can transform the dataRDD to another RDD that consists of parsed arrays

# COMMAND ----------

parsedRDD = dataRDD.map(parseLine)

# COMMAND ----------

# MAGIC %md We have all the pieces to convert the text RDD to a Spark DataFrame: schema and parsed data. We generally prefer DataFrames because they are much easier to manipulate and because Spark knows about the types of data. Therefore it can do a better job processing them. Most of Spark's libraries (such as SparkML) take their input in the form of DataFrames.
# MAGIC 
# MAGIC We use `createDataFrame` function to combine these two pieces of information to construct a DataFrame.

# COMMAND ----------

# MAGIC %md Just before using our parsed header, we need to convert it to the type that SparkSQL expects. That entails using SQL types (`IntegerType`, `DoubleType`, and `StringType`) and using `StructType` instead of a normal python list.

# COMMAND ----------

from pyspark.sql.types import *

def strToType(str):
  if str == 'int':
    return IntegerType()
  elif str == 'double':
    return DoubleType()
  else:
    return StringType()

schema = StructType([StructField(t[0], strToType(t[1]), True) for t in header])

# COMMAND ----------

# MAGIC %md `createDataFrame` takes the parsedRDD as first argument and the schema as second argument. It returns a Spark `DataFrame`
# MAGIC 
# MAGIC **If you are running Spark 1.5 or older the next command will throw a parsing error.** Please continue to the next definition of `parseLine()` function below to find what the problem is.

# COMMAND ----------

df = sqlContext.createDataFrame(parsedRDD, schema)

# COMMAND ----------

# MAGIC %md Once we get a DataFrame we can register it as a temporary table. That will allow us to use its name in SQL queries.

# COMMAND ----------

df.registerTempTable("songsTable")

# COMMAND ----------

# MAGIC %md We can now cache our table. So far all operations have been lazy. This is the first time Spark will attempt to actually read all our data and apply the transformations. 
# MAGIC 
# MAGIC **If you are running Spark 1.6+ the next command will throw a parsing error.**

# COMMAND ----------

# MAGIC %sql cache table songsTable

# COMMAND ----------

# MAGIC %md The error indicates that we are attempting to parse `--` to an integer and that fails. By inspecting first few lines of our data again, we see that some columns have `--`. We need to watch for those. Here is an updated `parseLine` function.

# COMMAND ----------

def parseLine(line):
  tokens = zip(line.split("\t"), header)
  parsed_tokens = []
  for token in tokens:
    token_type = token[1][1]
    if token_type == 'double':
      parsed_tokens.append(float(token[0]))
    elif token_type == 'int':
      parsed_tokens.append(-1 if '-' in token[0] else int(token[0])) # Taking care of fields with --
    else:
      parsed_tokens.append(token[0])
  return parsed_tokens

# COMMAND ----------

df = sqlContext.createDataFrame(dataRDD.map(parseLine), schema)
df.registerTempTable("songsTable")

# COMMAND ----------

# MAGIC %md And let's try caching the table. We are going to access this data multiple times in following notebooks, therefore it is a good idea to cache it in memory for faster subsequent access.

# COMMAND ----------

# MAGIC %sql cache table songsTable

# COMMAND ----------

# MAGIC %md From now on we can easily query our data using the temporary table we just created and cached in memory. Since it is registered as a table we can conveniently use SQL as well as Spark API to access it.

# COMMAND ----------

# MAGIC %sql select * from songsTable limit 10

# COMMAND ----------

# MAGIC %md Next up is exploring this data. Click on the Exploration notebook to continue the tutorial.