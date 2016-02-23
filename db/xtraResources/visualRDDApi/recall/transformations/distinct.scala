// Databricks notebook source exported at Tue, 23 Feb 2016 08:52:17 UTC
// MAGIC %md
// MAGIC # distinct
// MAGIC Return a new RDD containing distinct items from the original RDD (omitting all duplicates).
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-60.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-61.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-62.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-63.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3,3,4))
val y = x.distinct()

// COMMAND ----------

y.collect()