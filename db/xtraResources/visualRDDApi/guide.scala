// Databricks notebook source exported at Wed, 24 Feb 2016 02:50:04 UTC
// MAGIC %md
// MAGIC # Here is a visual guide to the transformations and actions on RDDs
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-0.png)
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC # Transformations
// MAGIC 1. [map](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/map)
// MAGIC * [filter](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/filter)
// MAGIC * [flatMap](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/flatMap)
// MAGIC * [groupBy](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/groupBy)
// MAGIC * [groupByKey](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/groupByKey)
// MAGIC * [reduceByKey](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/reduceByKey)
// MAGIC * [mapPartitions](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/mapPartitions)
// MAGIC * [mapPartitionsWithIndex](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/mapPartitionsWithIndex)
// MAGIC * [sample](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/sample)
// MAGIC * [union](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/union)
// MAGIC * [join](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/join)
// MAGIC * [distinct](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/distinct)
// MAGIC * [coalesce](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/coalesce)
// MAGIC * ...
// MAGIC 
// MAGIC %md
// MAGIC # Actions
// MAGIC 1. [getNumPartitions](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/getNumPartitions)
// MAGIC * [collect](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/collect)
// MAGIC * [reduce](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/reduce)
// MAGIC * ...

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-3.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-4.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-5.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-6.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-7.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-8.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-9.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-10.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-11.png)

// COMMAND ----------

// MAGIC %md
// MAGIC # Transformations
// MAGIC 1. [map](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/map)
// MAGIC * [filter](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/filter)
// MAGIC * [flatMap](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/flatMap)
// MAGIC * [groupBy](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/groupBy)
// MAGIC * [groupByKey](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/groupByKey)
// MAGIC * [reduceByKey](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/reduceByKey)
// MAGIC * [mapPartitions](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/mapPartitions)
// MAGIC * [mapPartitionsWithIndex](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/mapPartitionsWithIndex)
// MAGIC * [sample](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/sample)
// MAGIC * [union](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/union)
// MAGIC * [join](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/join)
// MAGIC * [distinct](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/distinct)
// MAGIC * [coalesce](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/coalesce)

// COMMAND ----------

// MAGIC %md
// MAGIC Continue from:
// MAGIC 
// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-68.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-85.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-86.png)

// COMMAND ----------

// MAGIC %md
// MAGIC # Actions
// MAGIC 1. [getNumPartitions](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/getNumPartitions)
// MAGIC * [collect](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/collect)
// MAGIC * [reduce](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/reduce)

// COMMAND ----------

// MAGIC %md
// MAGIC Continue from:
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-95.png)