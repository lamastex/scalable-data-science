// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Utilities Needed for Financial Data
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
// MAGIC Here, certain delta.io tables are loaded.
// MAGIC These tables have been prepared already.
// MAGIC 
// MAGIC You will not be able to load them directly but libraries used in the process are all open-sourced.

// COMMAND ----------

object TrendUtils {
  private val fx1mPath = "s3a://XXXXX/findata/com/histdata/free/FX-1-Minute-Data/"
  private val trendCalculusCheckpointPath = "s3a://XXXXX/summerinterns2020/trend-calculus-blog/public/"
  private val streamableTrendCalculusPath = "s3a://XXXXX/summerinterns2020/johannes/streamable-trend-calculus/"
  private val yfinancePath = "s3a://XXXXX/summerinterns2020/yfinance/"
  
  def getFx1mPath = fx1mPath
  def getTrendCalculusCheckpointPath = trendCalculusCheckpointPath
  def getStreamableTrendCalculusPath = streamableTrendCalculusPath
  def getYfinancePath = yfinancePath
}

// COMMAND ----------

// MAGIC %python
// MAGIC class TrendUtils:
// MAGIC   
// MAGIC   def getTrendCalculusCheckpointPath():
// MAGIC     return "s3a://XXXXX/summerinterns2020/trend-calculus-blog/public/"
// MAGIC   
// MAGIC   def getYfinancePath():
// MAGIC     return "s3a://XXXXX/summerinterns2020/yfinance/"