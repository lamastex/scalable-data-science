// Databricks notebook source exported at Tue, 23 Feb 2016 07:27:51 UTC
// MAGIC %md
// MAGIC # groupByKey
// MAGIC Group the values for each key in the original RDD. Create a new pair where the original key corresponds to this collected group of values.
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-38.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-39.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-40.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-41.png)

// COMMAND ----------

val x = sc.parallelize(Array(('B',5),('B',4),('A',3),('A',2),('A',1)))
val y = x.groupByKey()

// COMMAND ----------

println(x.collect().mkString(", "))
println(y.collect().mkString(", "))

// COMMAND ----------

