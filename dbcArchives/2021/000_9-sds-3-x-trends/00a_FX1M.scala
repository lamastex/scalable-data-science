// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Historical FX-1-M Financial Data
// MAGIC 
// MAGIC Johannes Graner ([LinkedIn](https://www.linkedin.com/in/johannes-graner-475677129/)), Albert Nilsson ([LinkedIn](https://www.linkedin.com/in/albert-nilsson-09b62b191/)) and Raazesh Sainudiin ([LinkedIn](https://www.linkedin.com/in/raazesh-sainudiin-45955845/))
// MAGIC 
// MAGIC 2020, Uppsala, Sweden
// MAGIC 
// MAGIC This project was supported by Combient Mix AB through summer internships at:
// MAGIC 
// MAGIC Combient Competence Centre for Data Engineering Sciences, 
// MAGIC Department of Mathematics, 
// MAGIC Uppsala University, Uppsala, Sweden
// MAGIC 
// MAGIC ---
// MAGIC 
// MAGIC **Resources**
// MAGIC 
// MAGIC This notebook builds on the following repository in order to obtain SparkSQL DatSets and DataFrames of freely available *FX-1-M Data* so that they can be ingested into delta.io Tables:
// MAGIC 
// MAGIC - [https://github.com/philipperemy/FX-1-Minute-Data](https://github.com/philipperemy/FX-1-Minute-Data)

// COMMAND ----------

// MAGIC %run "./000a_finance_utils"

// COMMAND ----------

// MAGIC %md
// MAGIC The [Trend Calculus library](https://github.com/lamastex/spark-trend-calculus) is needed for case classes and parsers for the data.

// COMMAND ----------

import org.lamastex.spark.trendcalculus._

// COMMAND ----------

val filePathRoot = TrendUtils.getFx1mPath

// COMMAND ----------

// MAGIC %md
// MAGIC There are many pairs of currencies and/or commodities available.

// COMMAND ----------

dbutils.fs.ls(filePathRoot).foreach(fi => println("exchange pair: " + fi.name))

// COMMAND ----------

// MAGIC %md
// MAGIC Let's look at Brent Oil price in USD.

// COMMAND ----------

dbutils.fs.ls(filePathRoot + "bcousd/").foreach(fi => println("name: " + fi.name + ", size: " + fi.size))

// COMMAND ----------

// MAGIC %md
// MAGIC We use the parser available from Trend Calculus to read the csv files into a Spark Dataset.

// COMMAND ----------

val oilPath = filePathRoot + "bcousd/*.csv.gz"
val oilDS = spark.read.fx1m(oilPath).orderBy($"time")

// COMMAND ----------

oilDS.show(20, false)