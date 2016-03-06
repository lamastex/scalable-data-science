// Databricks notebook source exported at Tue, 23 Feb 2016 07:46:12 UTC
// MAGIC %md
// MAGIC # collect
// MAGIC Return all items in the RDD to the driver in a single list.
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-89.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-90.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3), 2) // make a RDD with two partitions

// COMMAND ----------

// simply returns all elements in RDD x to the driver as an Array
val y = x.collect()

// COMMAND ----------

//glom() flattens elements on the same partition
val xOut = x.glom().collect() 