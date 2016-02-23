// Databricks notebook source exported at Tue, 23 Feb 2016 09:04:50 UTC
// MAGIC %md
// MAGIC # reduce
// MAGIC Aggregate all the elements of the RDD by applying a user function pairwise to elements and partial results, and returns a result to the driver.
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-91.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-92.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-93.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-94.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3,4))
val y = x.reduce((a,b) => a+b)

// COMMAND ----------

println(x.collect.mkString(", "))
println(y)