// Databricks notebook source exported at Tue, 23 Feb 2016 08:20:46 UTC
// MAGIC %md
// MAGIC # sample
// MAGIC Return a new RDD containing a statistical sample of the original RDD.
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-51.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-52.png)

// COMMAND ----------

val x = sc.parallelize(Array(1, 2, 3, 4, 5))

// COMMAND ----------

//omitting seed will return different output
val y = x.sample(false, 0.4)

// COMMAND ----------

y.collect()

// COMMAND ----------

//omitting seed will return different output
x.sample(false, 0.4).collect()

// COMMAND ----------

//including seed will preserve output
sc.parallelize(Seq.range(0, 1000)).sample(false, 0.1, 124L).collect()

// COMMAND ----------

//including seed will preserve output
sc.parallelize(Seq.range(0, 1000)).sample(false, 0.1, 124L).collect()