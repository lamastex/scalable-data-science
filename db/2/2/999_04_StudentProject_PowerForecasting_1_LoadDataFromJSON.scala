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
// MAGIC by [Gustav Björdal](https://www.linkedin.com/in/gustav-bj%C3%B6rdal-180461155/), [Mahmoud Shepero](https://www.linkedin.com/in/mahmoudshepero/) and [Dennis van der Meer](https://www.linkedin.com/in/dennis-van-der-meer-79463b94/)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #Loading data som JSON file
// MAGIC You should first import the cleaned data to `dbfs:/FileStore/tables/forecasting/`

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/FileStore/tables/forecasting"))

// COMMAND ----------

// Note the schema to read our data
// Read the JSON files (stored as tables in inputPath) using the schema
val inputPath = "/FileStore/tables/forecasting/"

import org.apache.spark.sql.types._

val jsonSchema = new StructType()
      .add("ID", StringType)
      //.add("timeStamp", StringType)
      .add("timeStamp", TimestampType)
      .add("DataList", 
        new StructType()
          .add("WXT530",
            new StructType()
              .add("DN", StringType)   // Wind direction (degrees) 
              .add("SN", StringType)   // Wind speed (m/s)
              .add("GT3U", StringType) // Ambient temperature (Celsius)
              .add("GM41", StringType) // Relative humidity (%)
              .add("GP41", StringType) // Air pressure (mbar?)
              .add("RC", StringType)   // Cumulative rain over the last month (L?)
              .add("RD", StringType)   // Rain duration (s)
              .add("RI", StringType)   // Rain intensity (mm/h)
               )
        
          .add("MX41",
            new StructType()
              .add("P", StringType)    // Power (kW) 
               )
           )

// COMMAND ----------

// Read a certain JSON file and turn it into a dataframe
val DF = spark.read
  .format("json")
  .schema(jsonSchema)
  .option("header", "true")
  .load(inputPath) // This immediately loads all files in inputPath
//DF.printSchema
DF.count

// COMMAND ----------

// Extract the columns that I want and rename them (timeStamp is now included, which saves some steps)
// In addition, cast them to double

val FinalDF =      DF.select($"DataList.MX41".getItem("P").alias("Power").cast(DoubleType),
                             $"DataList.WXT530".getItem("DN").alias("WindDirection").cast(DoubleType),
                             $"DataList.WXT530".getItem("SN").alias("WindSpeed").cast(DoubleType),
                             $"DataList.WXT530".getItem("GT3U").alias("Temperature").cast(DoubleType),
                             $"DataList.WXT530".getItem("GM41").alias("RH").cast(DoubleType),
                             $"DataList.WXT530".getItem("GP41").alias("AP").cast(DoubleType),
                             $"DataList.WXT530".getItem("RC").alias("RainCumulative").cast(DoubleType),
                             $"DataList.WXT530".getItem("RD").alias("RainDur").cast(DoubleType),
                             $"DataList.WXT530".getItem("RI").alias("RainIntens").cast(DoubleType),
                             $"timeStamp")

// COMMAND ----------

//display(FinalDF.select($"Power",$"WindSpeed"))
//FinalDF.orderBy("timeStamp").show()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Exports:
// MAGIC 
// MAGIC * `FinalDF` - Dataset 
// MAGIC * `leadDF` - Dataset with a one-step lag