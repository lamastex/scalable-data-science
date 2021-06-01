// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Detecting Events of Interest to OIL/GAS Price Trends
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
// MAGIC Here we will build a pipeline to investigate possible events related to oil and gas that are reported in mass media around the world  and their possible co-occurrence with certain trends and trend-reversals in oil price.
// MAGIC 
// MAGIC ***Steps:***
// MAGIC 
// MAGIC - Step 0. Setting up and loading GDELT delta.io tables
// MAGIC - Step 1. Extracting coverage around gas and oil from each country
// MAGIC - Step 2. Extracting the news around dates with high coverage (big events)
// MAGIC - Step 3. Enrich oil price with trend calculus and comparing it to the coverage
// MAGIC 
// MAGIC **Resources:**
// MAGIC 
// MAGIC This builds on the following libraries and its antecedents therein:
// MAGIC 
// MAGIC - [https://github.com/aamend/spark-gdelt](https://github.com/aamend/spark-gdelt) 
// MAGIC - [https://github.com/lamastex/spark-trend-calculus](https://github.com/lamastex/spark-trend-calculus)
// MAGIC 
// MAGIC 
// MAGIC **This work was inspired by:**
// MAGIC 
// MAGIC - Antoine Aamennd's [texata-2017](https://github.com/aamend/texata-r2-2017)
// MAGIC - Andrew Morgan's [Trend Calculus Library](https://github.com/ByteSumoLtd/TrendCalculus-lua)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 0. Setting up and loading GDELT delta.io tables

// COMMAND ----------

// DBTITLE 0,Importing packages.
import spark.implicits._
import io.delta.tables._
import com.aamend.spark.gdelt._
import org.apache.spark.sql.{Dataset,DataFrame,SaveMode}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions._
import org.lamastex.spark.trendcalculus._
import java.sql.{Date,Timestamp}
import java.text.SimpleDateFormat

// COMMAND ----------

// MAGIC %md
// MAGIC **Loading in the data from our live-updating delta.io tables**

// COMMAND ----------

// MAGIC %run "./000a_finance_utils"

// COMMAND ----------

// MAGIC %run "./000b_gdelt_utils"

// COMMAND ----------

val rootPath = GdeltUtils.getGdeltV1Path
val rootCheckpointPath = GdeltUtils.getEOICheckpointPath
val gkgPath = rootPath+"gkg"
val eventPath = rootPath+"events"

// COMMAND ----------

// DBTITLE 0,Loading in the data.
val gkg_v1 = spark.read.format("delta").load(gkgPath).as[GKGEventV1]
val eve_v1 = spark.read.format("delta").load(eventPath).as[EventV1]

// COMMAND ----------

// MAGIC %md
// MAGIC ## 1. Extracting coverage around gas and oil from each country

// COMMAND ----------

// MAGIC %md
// MAGIC **Limits the data and extracts the events related to oil and gas theme**

// COMMAND ----------

// DBTITLE 0,Limits the data and extracts the events related to oil and gas theme.
val gkg_v1_filt = gkg_v1.filter($"publishDate">"2013-04-01 00:00:00" && $"publishDate"<"2019-12-31 00:00:00")
val oil_gas_themeGKG = gkg_v1_filt.filter(c =>c.themes.contains("ENV_GAS") || c.themes.contains("ENV_OIL"))
                              .select(explode($"eventIds"))
                              .toDF("eventId")
                              .groupBy($"eventId")
                              .agg(count($"eventId"))
                              .toDF("eventId","count") 
val oil_gas_eventDF = eve_v1.toDF()
                            .join( oil_gas_themeGKG, "eventId")

// COMMAND ----------

// MAGIC %md
// MAGIC **Checkpoint**

// COMMAND ----------

// DBTITLE 0,Checkpoint.
oil_gas_eventDF.write.parquet(rootCheckpointPath + "oil_gas_event_v1")

// COMMAND ----------

val  oil_gas_eventDF = spark.read.parquet(rootCheckpointPath + "oil_gas_event_v1")

// COMMAND ----------

// MAGIC %md
// MAGIC **Extracting coverage for each country**

// COMMAND ----------

// DBTITLE 0,Extracting coverage for each country.
// Counting number of articles for each country, each day and applying a moving average on each country's coverage
def movingAverage(df:DataFrame,size:Int,avgOn:String):DataFrame = {
  val windowSpec = Window.partitionBy($"country").orderBy($"date").rowsBetween(-size/2, size/2)
  return df.withColumn("coverage",avg(avgOn).over(windowSpec))
}
val (mean_articles, std_articles) = oilEventTemp.select(mean("articles"), stddev("articles"))
  .as[(Double, Double)]
  .first() 
val oilEventTemp = oil_gas_eventDF
  .filter(length(col("eventGeo.countryCode")) > 0)
  .groupBy(
    col("eventGeo.countryCode").as("country"),
    col("eventDay").as("date")
  )
  .agg(
    sum(col("numArticles")).as("articles"),
  )
                                        
//Applying moving averge over weeks and of normalized number of articles.
val oilEventWeeklyCoverage = movingAverage(oilEventTemp.withColumn("normArticles",
                                                                   ($"articles"-mean_articles) / std_articles),
                                                                    7,
                                                                    "normArticles")
  .drop("normArticles")      

// COMMAND ----------

// MAGIC %md
// MAGIC **Checkpoint**

// COMMAND ----------

// DBTITLE 0,Checkpoint
oilEventWeeklyCoverage.write.parquet(rootCheckpointPath +"oil_gas_cov_norm")

// COMMAND ----------

val oil_gas_cov_norm = spark.read.parquet(rootCheckpointPath +"oil_gas_cov_norm")

// COMMAND ----------

// MAGIC %md
// MAGIC **Enrich the event data with the extracted coverage**

// COMMAND ----------

// DBTITLE 0,Enrich the event data with the extracted coverage.
val oilEventWeeklyCoverageC = oil_gas_cov_norm.drop($"articles").toDF("country","tempDate","coverage")
val oilEventCoverageDF = oilEventWeeklyCoverageC.join(oil_gas_eventDF,oil_gas_eventDF("eventDay") === oilEventWeeklyCoverageC("tempDate") && oil_gas_eventDF("eventGeo.countryCode")
                       === oilEventWeeklyCoverageC("country"))


// COMMAND ----------

// MAGIC %md
// MAGIC **Checkpoint**

// COMMAND ----------

// DBTITLE 0,Checkpoint
oilEventCoverageDF.write.parquet(rootCheckpointPath+"oil_gas_eve_cov")

// COMMAND ----------

val oilEventCoverageDF = spark.read.parquet(rootCheckpointPath+"oil_gas_eve_cov")

// COMMAND ----------

// MAGIC %md
// MAGIC **Let us look at 2018**

// COMMAND ----------

val tot_cov_2018 = oil_gas_cov_norm.filter($"date" >"2018-01-01" && $"date"<"2018-12-31").groupBy($"date").agg(sum($"coverage").as("coverage")).orderBy(desc("coverage"))

// COMMAND ----------

// MAGIC %md
// MAGIC **Total Coverage**

// COMMAND ----------

// DBTITLE 0,Total coverage
display(tot_cov_2018)

// COMMAND ----------

// MAGIC %md
// MAGIC ![trend_calculus_mc_model_performance](https://github.com/lamastex/spark-gdelt-examples/blob/master/images/coverage_tot.png?raw=true)

// COMMAND ----------

// MAGIC %md
// MAGIC **Coverage Grouped by Country**

// COMMAND ----------

// DBTITLE 0,Coverage grouped by country
display(oil_gas_cov_norm.filter($"date" >"2018-01-01" && $"date"<"2018-12-31").orderBy(desc("coverage")).limit(1000))

// COMMAND ----------

// MAGIC %md
// MAGIC ![coverage_by_country](https://github.com/lamastex/spark-gdelt-examples/blob/master/images/coverage_by_country.png?raw=true)

// COMMAND ----------

// MAGIC %md
// MAGIC **Coverage without USA**

// COMMAND ----------

// DBTITLE 0,Coverage without USA
display(oil_gas_cov_norm.filter($"date" >"2018-01-01" && $"date"<"2018-12-31" && $"country" =!="US").orderBy(desc("coverage")).limit(1000))

// COMMAND ----------

// MAGIC %md
// MAGIC ![coverage_without_usa](https://github.com/lamastex/spark-gdelt-examples/blob/master/images/coverage_without_usa.png?raw=true)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 2. Extracting the news around dates with high coverage (big events)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Investigating a seemingly inflentiual (2018-10-18) in Saudi Arabia (SA) using the **scalable web scraper** library *goose*

// COMMAND ----------

// DBTITLE 0,Initializing the web scraper.
//Initializing the web scraper.
val urlContentFetcher = {new ContentFetcher()
    .setInputCol("sourceUrl")
    .setOutputTitleCol("title")
    .setOutputContentCol("content")
    .setOutputKeywordsCol("keywords")
    .setOutputPublishDateCol("publishDateCollected")
    .setOutputDescriptionCol("description")
    .setUserAgent("Mozilla/5.0 (X11; U; Linux x86_64; de; rv:1.9.2.8) Gecko/20100723 Ubuntu/10.04 Firefox/")
    .setConnectionTimeout(1000)
    .setSocketTimeout(1000)
                        }

// COMMAND ----------

val big_event_SA = oilEventCoverageDF.filter($"country" ==="SA" && $"eventDay" === "2018-10-18").orderBy(desc("coverage")).limit(100)
val SAEventURLS = urlContentFetcher.transform(big_event_SA.select($"country",$"coverage",$"date",$"sourceUrl",$"eventId")).filter(col("description") =!= "").orderBy(desc("coverage"))

// COMMAND ----------

SAEventURLS.printSchema

// COMMAND ----------

SAEventURLS.select($"description").show(6,false)

// COMMAND ----------

// MAGIC %md
// MAGIC Investigating seemingly inflentiual events in Iran (IR) using goose on:
// MAGIC 
// MAGIC  - 2018-05-10
// MAGIC  - 2018-09-25

// COMMAND ----------

// MAGIC %md
// MAGIC **Events on 2018-05-10**

// COMMAND ----------

val big_event_IR1 = oilEventCoverageDF.filter($"country" ==="IR" && $"eventDay" === "2018-05-10").orderBy(desc("coverage")).limit(100)
val IR1EventURLS = urlContentFetcher.transform(big_event_IR1.select($"country",$"coverage",$"date",$"sourceUrl",$"eventId")).filter(col("description") =!= "").orderBy(desc("coverage"))

// COMMAND ----------

IR1EventURLS.select($"description").show(10,false)

// COMMAND ----------

// MAGIC %md
// MAGIC **Events on 2018-09-25**

// COMMAND ----------

val big_event_IR2 = oilEventCoverageDF.filter($"country" ==="IR" && $"eventDay" === "2018-09-25").orderBy(desc("coverage")).limit(100)
val IR2EventURLS = urlContentFetcher.transform(big_event_IR2.select($"country",$"coverage",$"date",$"sourceUrl",$"eventId")).filter(col("description") =!= "").orderBy(desc("coverage"))

// COMMAND ----------

IR2EventURLS.select($"description").show(6,false)

// COMMAND ----------

// MAGIC %md 
// MAGIC **Enrich Data with Trend Calculus**
// MAGIC 
// MAGIC For more information about Trend Calculus, see [https://lamastex.github.io/spark-trend-calculus-examples/](https://lamastex.github.io/spark-trend-calculus-examples/)

// COMMAND ----------

val rootTrendPath = TrendUtils.getStreamableTrendCalculusPath
val oilGoldPath = rootTrendPath +"oilGoldDelta"


// COMMAND ----------

// MAGIC %md
// MAGIC **Gather trend reversals of all oil price data**

// COMMAND ----------

// DBTITLE 0,Gather trend reversals of all oil price data
val oil_data_all = spark.read.format("delta").load(oilGoldPath).as[TickerPoint].filter($"ticker" === "BCOUSD")
val trend_oil_all = new TrendCalculus2(oil_data_all,2,spark).nReversalsJoinedWithMaxRev(15)


// COMMAND ----------

// DBTITLE 0,Untitled
trend_oil_all.write.parquet(rootCheckpointPath+"trend_oil_all")

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 3. Comparing the coverage with oil price

// COMMAND ----------

// MAGIC %md
// MAGIC **Code needed for the interactive plot below**

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC from plotly.offline import plot
// MAGIC from plotly.graph_objs import *
// MAGIC from datetime import *
// MAGIC from pyspark.sql import functions as F
// MAGIC import pyspark.sql.functions
// MAGIC from pyspark.sql.functions import col, avg

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC rootCheckpointPath = GdeltUtils.getEOICheckpointPath()

// COMMAND ----------

// MAGIC %python
// MAGIC trend_oil_all = spark.read.parquet(rootCheckpointPath+"trend_oil_all")
// MAGIC 
// MAGIC oil_gas_cov_us_2015_2018 = spark.read.parquet(rootCheckpointPath+"oil_gas_cov_norm").select(F.col('date'),F.col('country'),F.col('coverage')).filter(F.col('country') == 'US').drop('country').filter(F.col('date')>'2015-01-01').filter(F.col('date')<'2018-12-31')
// MAGIC 
// MAGIC trend_oil_2015_2018 = trend_oil_all.filter(F.col('x')>'2015-01-01').filter(F.col('x')<'2018-12-31').orderBy(F.col('x'))
// MAGIC 
// MAGIC max_price = trend_oil_2015_2018.agg({'y': 'max'}).first()[0]
// MAGIC min_price = trend_oil_2015_2018.agg({'y': 'min'}).first()[0]
// MAGIC trend_oil_2015_2018_2 =trend_oil_2015_2018.withColumn('sy', (F.col('y')-min_price)/(max_price-min_price))
// MAGIC 
// MAGIC fullTS = trend_oil_2015_2018_2.filter("maxRev > 2").select("x","sy","maxRev").collect()
// MAGIC coverage =oil_gas_cov_us_2015_2018.collect()
// MAGIC 
// MAGIC TS = [row for row in fullTS]

// COMMAND ----------

// MAGIC %python
// MAGIC numReversals = 15
// MAGIC startReversal = 7
// MAGIC 
// MAGIC allData = {'x': [row['x'] for row in TS], 'y': [row['sy'] for row in TS], 'maxRev': [row['maxRev'] for row in TS]}
// MAGIC allDataCov = {'x': [row['date'] for row in coverage], 'y': [row['coverage'] for row in coverage]}
// MAGIC 
// MAGIC temp2 = max(allDataCov['y'])-min(allDataCov['y'])
// MAGIC standardCoverage = list(map(lambda x: (x-min(allDataCov['y']))/temp2,allDataCov['y']))
// MAGIC 
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
// MAGIC **Plot of oil price together with oil and gas coverage for USA**

// COMMAND ----------

// DBTITLE 0,Plot of oil price together with oil and gas coverage for USA
// MAGIC %python
// MAGIC p = plot(
// MAGIC   [Scattergl(x=allData['x'], y=allData['y'], mode='lines', name='Oil Price'),Scattergl(x=allDataCov['x'], y=standardCoverage, mode='lines', name='Oil and gas coverage usa ')] + markerPlots 
// MAGIC   ,
// MAGIC   output_type='div'
// MAGIC )
// MAGIC displayHTML(p)

// COMMAND ----------

// MAGIC %md
// MAGIC ![oiltrendevents](https://github.com/lamastex/spark-gdelt-examples/blob/master/notebooks/db/images/oiltrendsevents.png?raw=true)