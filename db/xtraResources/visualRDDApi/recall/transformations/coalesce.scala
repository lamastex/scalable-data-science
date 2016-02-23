// Databricks notebook source exported at Tue, 23 Feb 2016 09:01:59 UTC
// MAGIC %md
// MAGIC # coalesce
// MAGIC Return a new RDD which is reduced to a smaller number of partitions.
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-64.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-65.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-66.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-67.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3,4,5), 3) // make RDD with 3 partitions

// COMMAND ----------

x.collect()

// COMMAND ----------

val y = x.coalesce(2) // coalesce RDD x from 3 to 2 partitions

// COMMAND ----------

y.collect() // x and y will yield the same list to driver upon collect

// COMMAND ----------

//glom() flattens elements on the same partition
val xOut = x.glom().collect() // x has three partitions

// COMMAND ----------

val yOut = y.glom().collect() // However y has only 2 partitions