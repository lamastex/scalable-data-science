// Databricks notebook source exported at Tue, 23 Feb 2016 07:10:01 UTC
// MAGIC %md
// MAGIC # mapPartitions
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-47.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3), 2) // RDD with 2 partitions

// COMMAND ----------

def f(i:Iterator[Int])={ (i.sum, 42).productIterator }

// COMMAND ----------

val y = x.mapPartitions(f)

// COMMAND ----------

// glom() flattens elements on the same partition
val xOut = x.glom().collect()

// COMMAND ----------

val yOut = y.glom().collect()

// COMMAND ----------

