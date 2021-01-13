// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)
// MAGIC 
// MAGIC Johannes Graner, Albert Nilsson and Raazesh Sainudiin

// COMMAND ----------

// MAGIC %md
// MAGIC # Plugging into GDELT Streams - TODO - IN PROGRESS

// COMMAND ----------

// MAGIC %md
// MAGIC This is just a brief teaser into the world of the GDELT-project:
// MAGIC  * [https://www.gdeltproject.org/](https://www.gdeltproject.org/)
// MAGIC  
// MAGIC 
// MAGIC This exposition was originally from [Mastering Spark for Data Science](https://books.google.se/books/about/Mastering_Spark_for_Data_Science.html?id=prkrDwAAQBAJ&source=kp_cover&redir_esc=y) which we will try to dive into in the geospatial modules.
// MAGIC  
// MAGIC We will use a spark-gdelt library for Spark 3.0.1:
// MAGIC 
// MAGIC - [https://github.com/aamend/spark-gdelt](https://github.com/aamend/spark-gdelt)
// MAGIC 
// MAGIC 
// MAGIC SEE:
// MAGIC 
// MAGIC - https://github.com/lamastex/spark-gdelt-examples
// MAGIC - 

// COMMAND ----------

import com.aamend.spark.gdelt._


// COMMAND ----------

// MAGIC %md
// MAGIC # This is just dipping our pinky toe in this ocean of information!

// COMMAND ----------

// MAGIC %md
// MAGIC # Download from gdelt-project

// COMMAND ----------

// MAGIC %sh
// MAGIC ls -al 

// COMMAND ----------

// MAGIC %sh
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20190517121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20190523121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip 20190517121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip 20190523121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20180416121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip 20180416121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC ls -al

// COMMAND ----------

// MAGIC %fs
// MAGIC ls /datasets/ScaDaMaLe/

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20180416121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/"

// COMMAND ----------

// MAGIC %fs
// MAGIC ls /datasets/ScaDaMaLe/GDELT/

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190523121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190517121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/"

// COMMAND ----------

// MAGIC %fs ls "dbfs:///datasets/ScaDaMaLe/GDELT/"

// COMMAND ----------

import com.aamend.spark.gdelt._
// For implicit conversions like converting RDDs to DataFrames
import spark.implicits._
import org.apache.spark.sql.Dataset
//val gdeltEventDS: Dataset[Event] = spark.read.gdeltEvent("/path/to/event.csv")
//val gdeltGkgDS: Dataset[GKGEventV1] = spark.read.gdeltGkg("dbfs:/datasets/20190523121500.gkg.csv")
val gdeltGkgDS = spark.read.format("text").load("dbfs:/datasets/ScaDaMaLe/GDELT/20190523121500.gkg.csv")//.as[GKGEventV1]
//val gdeltMention: Dataset[Mention] = spark.read.gdeltMention("/path/to/mention.csv")

// COMMAND ----------

gdeltGkgDS.printSchema()

// COMMAND ----------

display(gdeltGkgDS)

// COMMAND ----------

gdeltGkgDS.count

// COMMAND ----------

val gdeltGkgDS = spark.read.format("text").load("dbfs:/datasets/ScaDaMaLe/GDELT/20190523121500.gkg.csv").as[GKGEventV2]


// COMMAND ----------

// MAGIC %md
// MAGIC Let's look a the locations field. 
// MAGIC 
// MAGIC We want to be able to filter by a country.

// COMMAND ----------

display(gdeltGkgDS.select($"locations"))

// COMMAND ----------

val USgdeltGkgDS = gdeltGkgDS.withColumn("loc",$"locations"(0))
          .filter($"loc.countryCode" contains "US").drop("loc")

// COMMAND ----------

val IEgdeltGkgDS = gdeltGkgDS.withColumn("loc",$"locations"(0))
          .filter($"loc.countryCode" contains "IE").drop("loc")

// COMMAND ----------

IEgdeltGkgDS.count

// COMMAND ----------

USgdeltGkgDS.count

// COMMAND ----------

display(USgdeltGkgDS)

// COMMAND ----------

// MAGIC %md
// MAGIC ## GDELT Reference data

// COMMAND ----------

val countryCodes: Dataset[CountryCode] = spark.loadCountryCodes
val gcam: Dataset[GcamCode] = spark.loadGcams
val cameoEvent: Dataset[CameoCode] = spark.loadCameoEventCodes
val cameoType: Dataset[CameoCode] = spark.loadCameoTypeCodes
val cameoGroup: Dataset[CameoCode] = spark.loadCameoGroupCodes
val cameoEthnic: Dataset[CameoCode] = spark.loadCameoEthnicCodes
val cameoReligion: Dataset[CameoCode] = spark.loadCameoReligionCodes
val cameoCountry: Dataset[CameoCode] = spark.loadCameoCountryCodes

// COMMAND ----------

display(countryCodes)

// COMMAND ----------

display(cameoEvent)
