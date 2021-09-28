// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Quick Tutorial on PipedRDDs by Ivan Sadikov

// COMMAND ----------

// MAGIC %md Check if the script runs and prints the result

// COMMAND ----------

// MAGIC %fs ls /FileStore/tables/IsIt1or2Coins

// COMMAND ----------

// MAGIC %sh mkdir -p /tmp/ivan/ && cp /dbfs/FileStore/tables/IsIt1or2Coins /tmp/ivan/

// COMMAND ----------

// MAGIC %sh /tmp/ivan/IsIt1or2Coins 1000 100 234565432 1000 500 1200 600 1

// COMMAND ----------

// MAGIC %sh
// MAGIC 
// MAGIC # You can also do it like this
// MAGIC /dbfs/FileStore/tables/IsIt1or2Coins 1000 100 234565432 1000 500 1200 600 1

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC If you want to use local file copy, you can do it simpler like this:

// COMMAND ----------

import scala.sys.process._
import scala.concurrent.duration._

def copyFile(): Unit = {
  "mkdir -p /tmp/ivan".!!
  "cp /dbfs/FileStore/tables/IsIt1or2Coins /tmp/ivan/".!!
}

sc.runOnEachExecutor(copyFile, new FiniteDuration(1, HOURS))

// COMMAND ----------

val input = Seq("/tmp/ivan/IsIt1or2Coins 1000 100 234565432 1000 500 1200 600 1", "/tmp/ivan/IsIt1or2Coins 1000 100 234565432 1000 500 1200 600 1")

val res = sc
  .parallelize(input)
  .repartition(2)
  .pipe("bash")
  .collect()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC In fact, you can just use DBFS FUSE to run the commands without any file copy

// COMMAND ----------

val script = "/dbfs/FileStore/tables/IsIt1or2Coins"
val input = Seq(s"$script 1000 100 234565432 1000 500 1200 600 1", s"$script 1000 100 234565432 1000 500 1200 600 1")

val res = sc
  .parallelize(input)
  .repartition(2)
  .pipe("bash")
  .collect()

// COMMAND ----------

