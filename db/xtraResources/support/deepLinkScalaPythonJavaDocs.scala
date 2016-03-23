// Databricks notebook source exported at Wed, 23 Mar 2016 06:05:35 UTC
// MAGIC %md
// MAGIC ### A Note on being able to link-in scala docs from URL
// MAGIC 
// MAGIC Unfortunately we cannot deep-link-in to scala docs as explained here: [http://www.scala-lang.org/old/node/8798.html](http://www.scala-lang.org/old/node/8798.html).
// MAGIC 
// MAGIC Our best option now seems to go to the following link and search for randomSplit in the search-box there:
// MAGIC * [http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame)
// MAGIC 
// MAGIC The syntax for randomSplit (obtained by converting the source of the highlighted html resulting from the search from the link above and converting it to markdown via pandoc in bash) is as follows:
// MAGIC 
// MAGIC #### def randomSplit(weights: Array[Double], seed: Long): Array[[DataFrame]()]
// MAGIC 
// MAGIC Randomly splits this [DataFrame]() with the provided weights.
// MAGIC 
// MAGIC weights
// MAGIC :   weights for splits, will be normalized if they don't sum to 1.
// MAGIC 
// MAGIC seed
// MAGIC :   Seed for sampling.
// MAGIC 
// MAGIC Since
// MAGIC :   1.4.0
// MAGIC 
// MAGIC 
// MAGIC Python is ok http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame.randomSplit
// MAGIC 
// MAGIC Java too http://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/DataFrame.html#randomSplit(double%5B%5D%2C%20long)

// COMMAND ----------

// MAGIC %md
// MAGIC Can anyone help with being able to better link to syntax for scala docs?