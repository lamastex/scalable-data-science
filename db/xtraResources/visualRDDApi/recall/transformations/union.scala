// Databricks notebook source exported at Tue, 23 Feb 2016 08:33:07 UTC
// MAGIC %md
// MAGIC # union
// MAGIC Return a new RDD containing all items from two original RDDs. Duplicates are not culled (this is concatenation not set-theoretic union).
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-53.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-54.png)

// COMMAND ----------

val x = sc.parallelize(Array(1, 2, 3), 2) // an RDD with 2 partitions
val y = sc.parallelize(Array(3, 4), 1) // another RDD with 1 partition
val z = x.union(y)

// COMMAND ----------

//glom() flattens elements on the same partition
val zOut = z.glom().collect()

// COMMAND ----------

