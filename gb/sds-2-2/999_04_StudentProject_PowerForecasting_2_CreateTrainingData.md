[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Power Forecasting
=================

Student Project
---------------

by [Gustav Björdal](https://www.linkedin.com/in/gustav-bj%C3%B6rdal-180461155/), [Mahmoud Shepero](https://www.linkedin.com/in/mahmoudshepero/) and Dennis van der Meer

Create labeled training data from timeseries dataset
====================================================

``` run
/scalable-data-science/streaming-forecast/01-load-data-from-JSON
```

| path                                                | name             | size        |
|-----------------------------------------------------|------------------|-------------|
| dbfs:/FileStore/tables/forecasting/clean\_data.json | clean\_data.json | 7.1327975e7 |

>     inputPath: String = /FileStore/tables/forecasting/
>     import org.apache.spark.sql.types._
>     jsonSchema: org.apache.spark.sql.types.StructType = StructType(StructField(ID,StringType,true), StructField(timeStamp,TimestampType,true), StructField(DataList,StructType(StructField(WXT530,StructType(StructField(DN,StringType,true), StructField(SN,StringType,true), StructField(GT3U,StringType,true), StructField(GM41,StringType,true), StructField(GP41,StringType,true), StructField(RC,StringType,true), StructField(RD,StringType,true), StructField(RI,StringType,true)),true), StructField(MX41,StructType(StructField(P,StringType,true)),true)),true))

>     DF: org.apache.spark.sql.DataFrame = [ID: string, timeStamp: timestamp ... 1 more field]
>     res1: Long = 367965

>     FinalDF: org.apache.spark.sql.DataFrame = [Power: double, WindDirection: double ... 8 more fields]

>     import org.apache.spark.sql.functions.{lead, lag}
>     w: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@50b23287
>     leadDf: org.apache.spark.sql.DataFrame = [Power: double, WindDirection: double ... 10 more fields]

### Reduce noise in data

We reduce the noise in the data by averaging over every 30 seconds.

This replaces the old data

``` scala
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
```

>     import org.apache.spark.sql.{DataFrame, SparkSession}
>     import org.apache.spark.sql.functions._
>     averagedData: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 8 more fields]

``` scala
//display(averagedData)
```

Calculate mean and stddev using code from notebook 21 of the SDS course
-----------------------------------------------------------------------

Create a window of size 60.

Since every row in the dataset corresponds to the average over 30 seconds, a window size of 60 corresponds to a 30 minute time window.

``` scala
 // Import the window functions.
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

// Create a window specification
val windowSize = 2*30
val wSpec1 = Window.orderBy("window").rowsBetween(-windowSize, 0)
```

>     import org.apache.spark.sql.expressions.Window
>     import org.apache.spark.sql.functions._
>     windowSize: Int = 60
>     wSpec1: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@7050b68f

For every column in the dataset, compute the mean and stddev:

``` scala
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
```

>     dataFeatDF: org.apache.spark.sql.DataFrame = [window: struct<start: timestamp, end: timestamp>, Power: double ... 62 more fields]

``` scala
//display(dataFeatDF)
```

``` scala
println("Size of dataset: " + dataFeatDF.count)
```

>     res12: Long = 61473

Find the value to predict for every row
---------------------------------------

For every row, take the mean power 120 rows down as the power to predict.
This corresponds to predicting 60 minutes into the future.

We call this column the `future_power`

``` scala
val w = org.apache.spark.sql.expressions.Window.orderBy("window")    

val leadSteps = 2*60

val dataset = dataFeatDF.withColumn("future_power",lead("meanPower",leadSteps,0).over(w)).orderBy("window")
```

>     w: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@552488ce
>     leadSteps: Int = 120
>     dataset: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]

``` scala
//display(dataset)
```

### Split the data into a training and test set

``` scala
var Array(split20, split80) = dataset.randomSplit(Array(0.20, 0.80), 1800009193L)
```

>     split20: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]
>     split80: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]

``` scala
// Let's cache these datasets for performance
val testSet = split20.cache()
val trainingSet = split80.cache()
```

>     testSet: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]
>     trainingSet: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [window: struct<start: timestamp, end: timestamp>, Power: double ... 63 more fields]

### Exporting:

-   `testSet`
-   `trainingSet`