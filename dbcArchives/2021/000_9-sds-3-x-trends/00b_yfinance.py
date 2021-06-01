# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC # yfinance Stock Data 
# MAGIC 
# MAGIC Johannes Graner ([LinkedIn](https://www.linkedin.com/in/johannes-graner-475677129/)), Albert Nilsson ([LinkedIn](https://www.linkedin.com/in/albert-nilsson-09b62b191/)) and Raazesh Sainudiin ([LinkedIn](https://www.linkedin.com/in/raazesh-sainudiin-45955845/))
# MAGIC 
# MAGIC 2020, Uppsala, Sweden
# MAGIC 
# MAGIC This project was supported by Combient Mix AB through summer internships at:
# MAGIC 
# MAGIC Combient Competence Centre for Data Engineering Sciences, 
# MAGIC Department of Mathematics, 
# MAGIC Uppsala University, Uppsala, Sweden
# MAGIC 
# MAGIC ---
# MAGIC 
# MAGIC This notebook builds on the following repositories in order to obtain SparkSQL DataSets and DataFrames of freely available [Yahoo! Finance Data](https://www.yahoo.com/author/yahoo-finance) so that they can be ingested into delta.io Tables for trend analysis and more:
# MAGIC 
# MAGIC **Resources:**
# MAGIC 
# MAGIC - [https://aroussi.com/post/python-yahoo-finance](https://aroussi.com/post/python-yahoo-finance)
# MAGIC - [https://github.com/ranaroussi/yfinance](https://github.com/ranaroussi/yfinance)

# COMMAND ----------

# MAGIC %md
# MAGIC **Yfinance** is a python library that makes it easy to download various financial data from Yahoo Finance.

# COMMAND ----------

pip install yfinance

# COMMAND ----------

# MAGIC %run "./000a_finance_utils"

# COMMAND ----------

# MAGIC %md
# MAGIC To illustrate the library, we use two stocks of the Swedish bank SEB. The (default) data resolution is one day so we use 20 years of data to get a lot of observations.

# COMMAND ----------

import yfinance as yf
dataSEBAST = yf.download("SEB-A.ST", start="2001-07-01", end="2020-07-12")
dataSEBCST = yf.download("SEB-C.ST", start="2001-07-01", end="2020-07-12")

# COMMAND ----------

dataSEBAST.size

# COMMAND ----------

dataSEBAST

# COMMAND ----------

# MAGIC %md
# MAGIC We can also download several tickers at once. 
# MAGIC 
# MAGIC Note that the result is in a pandas dataframe so we are not doing any distributed computing. 
# MAGIC 
# MAGIC This means that some care has to be taken to not overwhelm the local machine.

# COMMAND ----------

dataSEBAandCSTBST = yf.download("SEB-A.ST SEB-C.ST", start="2020-07-01", end="2020-07-12", group_by="ticker")
dataSEBAandCSTBST

# COMMAND ----------

type(dataSEBAST)

# COMMAND ----------

# MAGIC %md
# MAGIC Loading the data into a Spark DataFrame.

# COMMAND ----------

dataSEBAST_sp = spark.createDataFrame(dataSEBAandCSTBST)

# COMMAND ----------

dataSEBAST_sp.printSchema()

# COMMAND ----------

# MAGIC %md
# MAGIC The conversion to Spark DataFrame works but is quite messy. Just imagine if there were more tickers!

# COMMAND ----------

dataSEBAST_sp.show(20, False)

# COMMAND ----------

# MAGIC %md
# MAGIC When selecting a column with a dot in the name (as in `('SEB-A.ST', 'High')`) using PySpark, we have to enclose the column name in backticks `.

# COMMAND ----------

dataSEBAST_sp.select("`('SEB-A.ST', 'High')`")

# COMMAND ----------

# MAGIC %md
# MAGIC We can also get information about individual tickers.

# COMMAND ----------

msft = yf.Ticker("MSFT")
print(msft)

# COMMAND ----------

msft.info

# COMMAND ----------

# MAGIC %md
# MAGIC We write a function to transform data downloaded by yfinance and write a better formatted Spark DataFrame.

# COMMAND ----------

import pandas as pd
import yfinance as yf
import sys, getopt

# example:
# python3 yfin_to_csv.py -i "60m" "SEB-A.ST INVE-A.ST" "2019-07-01" "2019-07-06" "/root/GIT/yfin_test.csv"
def ingest(interval, tickers, start, end, csv_path):
  df = yf.download(tickers, start=start, end=end, interval=interval, group_by='ticker')
  findf = df.unstack().unstack(1).sort_index(level=1)
  findf.reset_index(level=0, inplace=True)
  findf = findf.loc[start:end]
  findf.rename(columns={'level_0':'Ticker'}, inplace=True)
  findf.index.name='Time'
  findf['Volume'] = pd.to_numeric(findf['Volume'], downcast='integer')
  findf = findf.reset_index(drop=False)
  findf['Time'] = findf['Time'].map(lambda x: str(x))
  spark.createDataFrame(findf).write.mode('overwrite').save(csv_path, format='csv')
  return(findf)

# COMMAND ----------

# MAGIC %md
# MAGIC Let's look at some top value companies in the world as well as an assortment of Swedish Companies.
# MAGIC 
# MAGIC The number of tickers is now much larger, using the previous method would result in over 100 columns.
# MAGIC 
# MAGIC This would make it quite difficult to see what's going on, not to mention trying to analyze the data!
# MAGIC 
# MAGIC The data resolution is now one minute, meaning that 7 days gives a lot of observations.
# MAGIC 
# MAGIC Yfinance only allows downloading 1-minute data one week at a time and only for dates within the last 30 days.

# COMMAND ----------

topValueCompanies = 'MSFT AAPL AMZN GOOG BABA FB BRK-B BRK-A TSLA'
swedishCompanies = 'ASSA-B.ST ATCO-A.ST ALIV-SDB.ST ELUX-A.ST ELUX-B.ST EPI-A.ST EPI-B.ST ERIC-A.ST ERIC-B.ST FORTUM.HE HUSQ-A.ST HUSQ-B.ST INVE-A.ST INVE-B.ST KESKOA.HE KESKOB.HE KNEBV.HE KCR.HE MTRS.ST SAAB-B.ST SAS.ST SEB-A.ST SEB-C.ST SKF-A.ST SKF-B.ST STE-R.ST STERV.HE WRT1V.HE'
tickers = topValueCompanies + ' ' + swedishCompanies
interval = '1m'
start = '2021-01-01'
end = '2021-01-07'
csv_path = TrendUtils.getYfinancePath() + 'stocks_' + interval + '_' + start + '_' + end + '.csv'

# COMMAND ----------

dbutils.fs.rm(csv_path, recurse=True)

# COMMAND ----------

df = ingest(interval, tickers, start, end, csv_path)

# COMMAND ----------

# MAGIC %md
# MAGIC Having written the result to a csv file, we can use the parser in the Trend Calculus library [https://github.com/lamastex/spark-trend-calculus](https://github.com/lamastex/spark-trend-calculus) to read the data into a Dataset in Scala Spark.

# COMMAND ----------

# MAGIC %scala
# MAGIC import org.lamastex.spark.trendcalculus._

# COMMAND ----------

# MAGIC %scala
# MAGIC val rootPath = TrendUtils.getYfinancePath
# MAGIC val csv_path = rootPath + "stocks_1m_2021-01-01_2021-01-07.csv"
# MAGIC val yfinDF = spark.read.yfin(csv_path)

# COMMAND ----------

# MAGIC %scala
# MAGIC yfinDF.count

# COMMAND ----------

# MAGIC %scala
# MAGIC yfinDF.show(20, false)