// Databricks notebook source
// MAGIC %md
// MAGIC # getNumPartitions
// MAGIC Return the number of partitions in RDD.
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-87.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-88.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3), 2) // RDD with 2 partitions

// COMMAND ----------

val y = x.partitions.size

// COMMAND ----------

//glom() flattens elements on the same partition
val xOut = x.glom().collect()