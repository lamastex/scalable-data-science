// Databricks notebook source
// MAGIC %md
// MAGIC # partitionBy
// MAGIC 
// MAGIC Return a new RDD with the specified number of partitions, placing original
// MAGIC items into the partition returned by a user supplied function.
// MAGIC 
// MAGIC Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-73.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-74.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-75.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-76.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-77.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-78.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-79.png)

// COMMAND ----------

import org.apache.spark.Partitioner

// COMMAND ----------

val x = sc.parallelize(Array(('J',"James"),('F',"Fred"), ('A',"Anna"),('J',"John")), 3)
val y = x.partitionBy(new Partitioner() {
  val numPartitions = 2
  def getPartition(k:Any) = {
    if (k.asInstanceOf[Char] < 'H') 0 else 1
  }
})

// COMMAND ----------

val yOut = y.glom().collect()

// COMMAND ----------

