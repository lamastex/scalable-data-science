// Databricks notebook source exported at Tue, 23 Feb 2016 07:58:01 UTC
// MAGIC %md
// MAGIC # mapPartitionsWithIndex
// MAGIC Return a new RDD by applying a function to each partition of this RDD, while tracking the index of the original partition. 
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-48.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-50.png)

// COMMAND ----------

val x = sc. parallelize(Array(1,2,3), 2) // make an RDD with 2 partitions

// COMMAND ----------

def f(partitionIndex:Int, i:Iterator[Int]) = {
  (partitionIndex, i.sum).productIterator
}

// COMMAND ----------

val y = x.mapPartitionsWithIndex(f)

// COMMAND ----------

//glom() flattens elements on the same partition
val xout = x.glom().collect()

// COMMAND ----------

val yout = y.glom().collect()

// COMMAND ----------

