// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Finding trends in oil price data.
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
// MAGIC This builds on the following library and its antecedents therein to find trends in historical oil prices:
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
// MAGIC When dealing with time series, it can be difficult to find a good way to find and analyze trends in the data. 
// MAGIC 
// MAGIC One approach is by using the Trend Calculus algorithm invented by Andrew Morgan. More information about Trend Calculus can be found at [https://lamastex.github.io/spark-trend-calculus-examples/](https://lamastex.github.io/spark-trend-calculus-examples/).

// COMMAND ----------

import org.lamastex.spark.trendcalculus._
import spark.implicits._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import java.sql.Timestamp

// COMMAND ----------

// MAGIC %md
// MAGIC The input to the algorithm is data in the format (ticker, time, value). In this example, ticker is `"BCOUSD"` (Brent Crude Oil), time is given in minutes and value is the closing price for Brent Crude Oil during that minute.
// MAGIC 
// MAGIC This data is historical data from 2010 to 2019 taken from [https://www.histdata.com/](https://www.histdata.com/) using methods from [FX-1-Minute-Data](https://github.com/philipperemy/FX-1-Minute-Data) by Philippe Remy. In this notebook, everything is done on static dataframes. We will soon see examples on streaming dataframes.
// MAGIC 
// MAGIC There are gaps in the data, notably during the weekends when no trading takes place, but this does not affect the algorithm as it is does not place any assumptions on the data other than that time is monotonically increasing.
// MAGIC 
// MAGIC The window size is set to 2, which is minimal, because we want to retain as much information as possible.

// COMMAND ----------

val windowSize = 2
val dataRootPath = TrendUtils.getFx1mPath
val oilDS = spark.read.fx1m(dataRootPath + "bcousd/*.csv.gz").toDF.withColumn("ticker", lit("BCOUSD")).select($"ticker", $"time" as "x", $"close" as "y").as[TickerPoint].orderBy("x")

// COMMAND ----------

// MAGIC %md
// MAGIC If we want to look at long term trends, we can use the output time series as input for another iteration. The output contains the points of the input where the trend changes (reversals). This can be repeated several times, resulting in longer term trends.
// MAGIC 
// MAGIC Here, we look at (up to) 15 iterations of the algorithm. It is no problem if the output of some iteration is too small to find a reversal in the next iteration, since the output will just be an empty dataframe in that case.

// COMMAND ----------

val numReversals = 15
val dfWithReversals = new TrendCalculus2(oilDS, windowSize, spark).nReversalsJoinedWithMaxRev(numReversals)

// COMMAND ----------

dfWithReversals.show(20, false)

// COMMAND ----------

// MAGIC %md
// MAGIC The number of reversals decrease rapidly as more iterations are done.

// COMMAND ----------

dfWithReversals.cache.count

// COMMAND ----------

(1 to numReversals).foreach( i => println(dfWithReversals.filter(s"reversal$i is not null").count))

// COMMAND ----------

// MAGIC %md
// MAGIC We write the resulting dataframe to parquet in order to produce visualizations using Python.

// COMMAND ----------

val checkPointPath = TrendUtils.getTrendCalculusCheckpointPath
dfWithReversals.write.mode(SaveMode.Overwrite).parquet(checkPointPath + "joinedDSWithMaxRev_new")
dfWithReversals.unpersist

// COMMAND ----------

// MAGIC %md
// MAGIC **Visualization**
// MAGIC 
// MAGIC The Python library plotly is used to make interactive visualizations.

// COMMAND ----------

// MAGIC %python
// MAGIC from plotly.offline import plot
// MAGIC from plotly.graph_objs import *
// MAGIC from datetime import *
// MAGIC checkPointPath = TrendUtils.getTrendCalculusCheckpointPath()
// MAGIC joinedDS = spark.read.parquet(checkPointPath + "joinedDSWithMaxRev_new").orderBy("x")

// COMMAND ----------

// MAGIC %md
// MAGIC We check the size of the dataframe to see if it possible to handle locally since plotly is not available for distributed data.

// COMMAND ----------

// MAGIC %python
// MAGIC joinedDS.count()

// COMMAND ----------

// MAGIC %md
// MAGIC Almost 3 million rows might be too much for the driver! The timeseries has to be thinned out in order to display locally. 
// MAGIC 
// MAGIC No information about higher order trend reversals is lost since every higher order reversal is also a lower order reversal and the lowest orders of reversal are on the scale of minutes (maybe hours) and that is probably not very interesting considering that the data stretches over roughly 10 years!

// COMMAND ----------

// MAGIC %python
// MAGIC joinedDS.filter("maxRev > 2").count()

// COMMAND ----------

// MAGIC %md
// MAGIC Just shy of 100k rows is no problem for the driver.
// MAGIC 
// MAGIC We select the relevant information in the dataframe for visualization.

// COMMAND ----------

// MAGIC %python
// MAGIC fullTS = joinedDS.filter("maxRev > 2").select("x","y","maxRev").collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Picking an interval to focus on.
// MAGIC 
// MAGIC Start and end dates as (year, month, day, hour, minute, second).
// MAGIC Only year, month and day are required.
// MAGIC The interval from years 1800 to 2200 ensures all data is selected.

// COMMAND ----------

// MAGIC %python
// MAGIC startDate = datetime(1800,1,1)
// MAGIC endDate= datetime(2200,12,31)
// MAGIC TS = [row for row in fullTS if startDate <= row['x'] and row['x'] <= endDate]

// COMMAND ----------

// MAGIC %md
// MAGIC Setting up the visualization.

// COMMAND ----------

// MAGIC %python
// MAGIC numReversals = 15
// MAGIC startReversal = 7
// MAGIC 
// MAGIC allData = {'x': [row['x'] for row in TS], 'y': [row['y'] for row in TS], 'maxRev': [row['maxRev'] for row in TS]}
// MAGIC revTS = [row for row in TS if row[2] >= startReversal]
// MAGIC colorList = ['rgba(' + str(tmp) + ',' + str(255-tmp) + ',' + str(255-tmp) + ',1)' for tmp in [int(i*255/(numReversals-startReversal+1)) for i in range(1,numReversals-startReversal+2)]]
// MAGIC 
// MAGIC def getRevTS(tsWithRevMax, revMax):
// MAGIC   x = [row[0] for row in tsWithRevMax if row[2] >= revMax]
// MAGIC   y = [row[1] for row in tsWithRevMax if row[2] >= revMax]
// MAGIC   return x,y,revMax
// MAGIC 
// MAGIC reducedData = [getRevTS(revTS, i) for i in range(startReversal, numReversals+1)]
// MAGIC 
// MAGIC markerPlots = [Scattergl(x=x, y=y, mode='markers', marker=dict(color=colorList[i-startReversal], size=i), name='Reversal ' + str(i)) for (x,y,i) in [getRevTS(revTS, i) for i in range(startReversal, numReversals+1)]]

// COMMAND ----------

// MAGIC %md
// MAGIC **Plotting result as plotly graph**
// MAGIC 
// MAGIC The graph is interactive, one can drag to zoom in on an area (double-click to get back) and click on the legend to hide or show different series.
// MAGIC 
// MAGIC Note that we have left out many of the lower order reversals in order to not make the graph too cluttered. The seventh order reversal (the lowest order shown) is still on the scale of hours to a few days.

// COMMAND ----------

// MAGIC %python
// MAGIC p = plot(
// MAGIC   [Scattergl(x=allData['x'], y=allData['y'], mode='lines', name='Oil Price')] + markerPlots
// MAGIC   ,
// MAGIC   output_type='div'
// MAGIC )
// MAGIC 
// MAGIC displayHTML(p)

// COMMAND ----------

// MAGIC %md
// MAGIC ![trend_calculus_oil](https://github.com/lamastex/spark-trend-calculus-examples/blob/master/images/trend_calculus_oil.png?raw=true)

// COMMAND ----------

