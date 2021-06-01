// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Streaming Trend Calculus with Maximum Necessary Reversals
// MAGIC 
// MAGIC Johannes Graner ([LinkedIn](https://www.linkedin.com/in/johannes-graner-475677129/)), Albert Nilsson ([LinkedIn](https://www.linkedin.com/in/albert-nilsson-09b62b191/)) and Raazesh Sainudiin ([LinkedIn](https://www.linkedin.com/in/raazesh-sainudiin-45955845/))
// MAGIC 
// MAGIC 2020, Uppsala, Sweden
// MAGIC 
// MAGIC This project was supported by Combient Mix AB through summer internships at:
// MAGIC 
// MAGIC Combient Competence Centre for Data Engineering Sciences, 
// MAGIC Department of Mathematics, 
// MAGIC Uppsala University, Uppsala, Sweden
// MAGIC 
// MAGIC ---
// MAGIC 
// MAGIC 
// MAGIC **Resources**
// MAGIC 
// MAGIC This builds on the following library and its antecedents therein:
// MAGIC 
// MAGIC - [https://github.com/lamastex/spark-trend-calculus](https://github.com/lamastex/spark-trend-calculus)
// MAGIC 
// MAGIC 
// MAGIC **This work was inspired by:**
// MAGIC 
// MAGIC - Antoine Aamennd's [texata-2017](https://github.com/aamend/texata-r2-2017)
// MAGIC - Andrew Morgan's [Trend Calculus Library](https://github.com/ByteSumoLtd/TrendCalculus-lua)

// COMMAND ----------

// MAGIC %run "./000a_finance_utils"

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC We use the spark-trend-calculus library and Spark structured streams over delta.io files to obtain a representation of the complete time series of trends with their k-th order reversal.
// MAGIC 
// MAGIC This representation is a sufficient statistic for a Markov model of trends that we show in the next notebook.

// COMMAND ----------

import java.sql.Timestamp
import io.delta.tables._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{GroupState, GroupStateTimeout, OutputMode, Trigger}
import org.apache.spark.sql.types._
import org.apache.spark.sql.expressions.{Window, WindowSpec}
import org.lamastex.spark.trendcalculus._

// COMMAND ----------

// MAGIC %md
// MAGIC Input data in s3. The data contains oil price data from 2010 to last month and gold price data from 2009 to last month.

// COMMAND ----------

val rootPath = "s3a://XXXXX/summerinterns2020/johannes/streamable-trend-calculus/"
val oilGoldPath = rootPath + "oilGoldDelta"

// COMMAND ----------

spark.read.format("delta").load(oilGoldPath).orderBy("x").show(20,false)

// COMMAND ----------

// MAGIC %md
// MAGIC Reading the data from s3 as a Structured Stream to simulate streaming.

// COMMAND ----------

val input = spark
  .readStream
  .format("delta")
  .load(oilGoldPath)
  .as[TickerPoint]

// COMMAND ----------

// MAGIC %md
// MAGIC Using the trendcalculus library to
// MAGIC 1. Apply Trend Calculus to the streaming dataset.
// MAGIC - Save the result as a delta table.
// MAGIC - Read the result as a stream.
// MAGIC - Repeat from 1. using the latest result as input. Stop when result is empty.

// COMMAND ----------

val windowSize = 2

// Initializing variables for while loop.
var i = 1
var prevSinkPath = ""
var sinkPath = rootPath + "multiSinks/reversal" + (i)
var chkptPath = rootPath + "multiSinks/checkpoint/" + (i)

// The first order reversal.
var stream = new TrendCalculus2(input, windowSize, spark)
  .reversals
  .select("tickerPoint.ticker", "tickerPoint.x", "tickerPoint.y", "reversal")
  .as[FlatReversal]
  .writeStream
  .format("delta")
  .option("path", sinkPath)
  .option("checkpointLocation", chkptPath)
  .trigger(Trigger.Once())
  .start

stream.processAllAvailable

i += 1

var lastReversalSeries = spark.emptyDataset[TickerPoint]
while (!spark.read.format("delta").load(sinkPath).isEmpty) {
  
  prevSinkPath = rootPath + "multiSinks/reversal" + (i-1)
  sinkPath = rootPath + "multiSinks/reversal" + (i)
  chkptPath = rootPath + "multiSinks/checkpoint/" + (i)
  
  // Reading last result as stream
  lastReversalSeries = spark
    .readStream
    .format("delta")
    .load(prevSinkPath)
    .drop("reversal")
    .as[TickerPoint]

  // Writing next result
  stream = new TrendCalculus2(lastReversalSeries, windowSize, spark)
    .reversals
    .select("tickerPoint.ticker", "tickerPoint.x", "tickerPoint.y", "reversal")
    .as[FlatReversal]
    .map( rev => rev.copy(reversal=i*rev.reversal))
    .writeStream
    .format("delta")
    .option("path", sinkPath)
    .option("checkpointLocation", chkptPath)
    .partitionBy("ticker")
    .trigger(Trigger.Once())
    .start
  
  stream.processAllAvailable()
  
  i += 1
}

// COMMAND ----------

// MAGIC %md
// MAGIC Checking the total number of reversals written. The last sink is empty so the highest order reversal is $$\text{number of sinks} - 1$$.

// COMMAND ----------

val i = dbutils.fs.ls(rootPath + "multiSinks").length - 1

// COMMAND ----------

// MAGIC %md
// MAGIC The written delta tables can be read as streams but for now we read them as static datasets to be able to join them together.

// COMMAND ----------

val sinkPaths = (1 to i-1).map(rootPath + "multiSinks/reversal" + _)
val maxRevPath = rootPath + "maxRev"
val revTables = sinkPaths.map(DeltaTable.forPath(_).toDF.as[FlatReversal])
val oilGoldTable = DeltaTable.forPath(oilGoldPath).toDF.as[TickerPoint]

// COMMAND ----------

// MAGIC %md
// MAGIC The number of reversals decrease rapidly as the reversal order increases.

// COMMAND ----------

revTables.map(_.cache.count)

// COMMAND ----------

// MAGIC %md
// MAGIC Joining all results to get a dataset with all reversals in a single column.

// COMMAND ----------

def maxByAbs(a: Int, b: Int): Int = {
  Seq(a,b).maxBy(math.abs)
}

val maxByAbsUDF = udf((a: Int, b: Int) => maxByAbs(a,b))

val maxRevDS = revTables.foldLeft(oilGoldTable.toDF.withColumn("reversal", lit(0)).as[FlatReversal]){ (acc: Dataset[FlatReversal], ds: Dataset[FlatReversal]) => 
  acc
    .toDF
    .withColumnRenamed("reversal", "oldMaxRev")
    .join(ds.select($"ticker" as "tmpt", $"x" as "tmpx", $"reversal" as "newRev"), $"ticker" === $"tmpt" && $"x" === $"tmpx", "left")
    .drop("tmpt", "tmpx")
    .na.fill(0,Seq("newRev"))
    .withColumn("reversal", maxByAbsUDF($"oldMaxRev", $"newRev"))
    .select("ticker", "x", "y", "reversal")
    .as[FlatReversal]    
}

// COMMAND ----------

// MAGIC %md
// MAGIC Writing result as delta table.

// COMMAND ----------

maxRevDS.write.format("delta").partitionBy("ticker").save(maxRevPath)

// COMMAND ----------

// MAGIC %md
// MAGIC The reversal column in the joined dataset contains the information of all orders of reversals.
// MAGIC 
// MAGIC `0` indicates that no reversal happens while a non-zero value indicates that this is a reversal point for that order and every lower order.
// MAGIC 
// MAGIC For example, row 33 contains the value `-4`, meaning that this point is trend reversal downwards for orders 1, 2, 3, and 4.

// COMMAND ----------

DeltaTable.forPath(maxRevPath).toDF.as[FlatReversal].filter("ticker == 'BCOUSD'").orderBy("x").show(35, false)