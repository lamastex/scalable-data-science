// Databricks notebook source exported at Mon, 22 Feb 2016 04:22:14 UTC
// MAGIC %md
// MAGIC # reduceByKey
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).
// MAGIC 
// MAGIC Also see [groupByKey](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/groupByKey).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-42.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-43.png)

// COMMAND ----------

val words = Array("one", "two", "two", "three", "three", "three")
val wordPairsRDD = sc.parallelize(words).map(word => (word, 1))

// COMMAND ----------

val wordCountsWithReduce = wordPairsRDD
                              .reduceByKey(_ + _)
                              .collect()

// COMMAND ----------

val wordCountsWithGroup = wordPairsRDD
                              .groupByKey()
                              .map(t => (t._1, t._2.sum))
                              .collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-44.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-45.png)

// COMMAND ----------

