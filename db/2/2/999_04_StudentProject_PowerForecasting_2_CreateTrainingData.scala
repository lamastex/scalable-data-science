// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/xqy5geCNKCg/0.jpg)](https://www.youtube.com/embed/xqy5geCNKCg?start=0&end=1456&autoplay=1)

// COMMAND ----------

// MAGIC %md 
// MAGIC # Power Forecasting
// MAGIC ## Student Project 
// MAGIC by [Gustav Björdal](https://www.linkedin.com/in/gustav-bj%C3%B6rdal-180461155/), [Mahmoud Shepero](https://www.linkedin.com/in/mahmoudshepero/) and Dennis van der Meer

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC # Create labeled training data from timeseries dataset

// COMMAND ----------

// MAGIC %run /scalable-data-science/streaming-forecast/01-load-data-from-JSON

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Reduce noise in data
// MAGIC We reduce the noise in the data by averaging over every 30 seconds.
// MAGIC 
// MAGIC This replaces the old data

// COMMAND ----------

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

val averagedData = leadDf
      .groupBy(window(leadDf.col("timeStamp"),"30 seconds","30 seconds"))
      .agg(
        //Power
          avg("Power").as("Power"),
        //WindDir
          avg("WindDirection").as("WindDirection"),
        //Windspeed
          avg("WindSpeed").as("WindSpeed"),
        //Temperature
          avg("Temperature").as("Temperature"),
        //RH
          avg("RH").as("RH"),
        //AP
          avg("AP").as("AP"),
        //Rain cumulative
          avg("RainCumulative").as("RainCumulative"),
        //Rain Dur
          avg("RainDur").as("RainDur"),
        //Rain Intens
          avg("RainIntens").as("RainIntens")        
          )
.orderBy("window")

// COMMAND ----------

//display(averagedData)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Calculate mean and stddev using code from notebook 21 of the SDS course
// MAGIC 
// MAGIC Create a window of size 60.
// MAGIC 
// MAGIC Since every row in the dataset corresponds to the average over 30 seconds, a window size of 60 corresponds to a 30 minute time window.

// COMMAND ----------

 // Import the window functions.
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

// Create a window specification
val windowSize = 2*30
val wSpec1 = Window.orderBy("window").rowsBetween(-windowSize, 0)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC For every column in the dataset, compute the mean and stddev:

// COMMAND ----------

 // Calculate the moving window statistics from data
      val dataFeatDF = averagedData
      .withColumn( "meanPower", mean($"Power").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanPower", abs($"Power" - $"meanPower") )
      .withColumn( "meanAbsDevFromMeanPower", mean("absDevFromMeanPower").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanPower", pow($"absDevFromMeanPower",2.0) )
      .withColumn( "variancePower", mean("sqrDevFromMeanPower").over(wSpec1) )
      .withColumn( "stddevPower", pow($"variancePower",0.50) )

      .withColumn( "meanWindDirection", mean($"WindDirection").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanWindDirection", abs($"WindDirection" - $"meanWindDirection") )
      .withColumn( "meanAbsDevFromMeanWindDirection", mean("absDevFromMeanWindDirection").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanWindDirection", pow($"absDevFromMeanWindDirection",2.0) )
      .withColumn( "varianceWindDirection", mean("sqrDevFromMeanWindDirection").over(wSpec1) )
      .withColumn( "stddevWindDirection", pow($"varianceWindDirection",0.50) )

      .withColumn( "meanWindSpeed", mean($"WindSpeed").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanWindSpeed", abs($"WindSpeed" - $"meanWindSpeed") )
      .withColumn( "meanAbsDevFromMeanWindSpeed", mean("absDevFromMeanWindSpeed").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanWindSpeed", pow($"absDevFromMeanWindSpeed",2.0) )
      .withColumn( "varianceWindSpeed", mean("sqrDevFromMeanWindSpeed").over(wSpec1) )
      .withColumn( "stddevWindSpeed", pow($"varianceWindSpeed",0.50) )

.withColumn( "meanTemperature", mean($"Temperature").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanTemperature", abs($"Temperature" - $"meanTemperature") )
      .withColumn( "meanAbsDevFromMeanTemperature", mean("absDevFromMeanTemperature").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanTemperature", pow($"absDevFromMeanTemperature",2.0) )
      .withColumn( "varianceTemperature", mean("sqrDevFromMeanTemperature").over(wSpec1) )
      .withColumn( "stddevTemperature", pow($"varianceTemperature",0.50) )

.withColumn( "meanRH", mean($"RH").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanRH", abs($"RH" - $"meanRH") )
      .withColumn( "meanAbsDevFromMeanRH", mean("absDevFromMeanRH").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanRH", pow($"absDevFromMeanRH",2.0) )
      .withColumn( "varianceRH", mean("sqrDevFromMeanRH").over(wSpec1) )
      .withColumn( "stddevRH", pow($"varianceRH",0.50) )

      .withColumn( "meanAP", mean($"AP").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanAP", abs($"AP" - $"meanAP") )
      .withColumn( "meanAbsDevFromMeanAP", mean("absDevFromMeanAP").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanAP", pow($"absDevFromMeanAP",2.0) )
      .withColumn( "varianceAP", mean("sqrDevFromMeanAP").over(wSpec1) )
      .withColumn( "stddevAP", pow($"varianceAP",0.50) )

.withColumn( "meanRainCumulative", mean($"RainCumulative").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanRainCumulative", abs($"RainCumulative" - $"meanRainCumulative") )
      .withColumn( "meanAbsDevFromMeanRainCumulative", mean("absDevFromMeanRainCumulative").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanRainCumulative", pow($"absDevFromMeanRainCumulative",2.0) )
      .withColumn( "varianceRainCumulative", mean("sqrDevFromMeanRainCumulative").over(wSpec1) )
      .withColumn( "stddevRainCumulative", pow($"varianceRainCumulative",0.50) )

.withColumn( "meanRainDur", mean($"RainDur").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanRainDur", abs($"RainDur" - $"meanRainDur") )
      .withColumn( "meanAbsDevFromMeanRainDur", mean("absDevFromMeanRainDur").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanRainDur", pow($"absDevFromMeanRainDur",2.0) )
      .withColumn( "varianceRainDur", mean("sqrDevFromMeanRainDur").over(wSpec1) )
      .withColumn( "stddevRainDur", pow($"varianceRainDur",0.50) )

.withColumn( "meanRainIntens", mean($"RainIntens").over(wSpec1)  )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanRainIntens", abs($"RainIntens" - $"meanRainIntens") )
      .withColumn( "meanAbsDevFromMeanRainIntens", mean("absDevFromMeanRainIntens").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanRainIntens", pow($"absDevFromMeanRainIntens",2.0) )
      .withColumn( "varianceRainIntens", mean("sqrDevFromMeanRainIntens").over(wSpec1) )
      .withColumn( "stddevRainIntens", pow($"varianceRainIntens",0.50) )

// COMMAND ----------

//display(dataFeatDF)

// COMMAND ----------

println("Size of dataset: " + dataFeatDF.count)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Find the value to predict for every row
// MAGIC 
// MAGIC For every row, take the mean power 120 rows down as the power to predict.
// MAGIC This corresponds to predicting 60 minutes into the future.
// MAGIC 
// MAGIC We call this column the `future_power
// MAGIC `

// COMMAND ----------

val w = org.apache.spark.sql.expressions.Window.orderBy("window")    

val leadSteps = 2*60

val dataset = dataFeatDF.withColumn("future_power",lead("meanPower",leadSteps,0).over(w)).orderBy("window")

// COMMAND ----------

//display(dataset)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Split the data into a training and test set
// MAGIC 
// MAGIC This is for modelgeneralizability.

// COMMAND ----------

var Array(split20, split80) = dataset.randomSplit(Array(0.20, 0.80), 1800009193L)

// COMMAND ----------

// Let's cache these datasets for performance
val testSet = split20.cache()
val trainingSet = split80.cache()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ###Exporting:
// MAGIC 
// MAGIC * `testSet`
// MAGIC * `trainingSet`