// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Plugging into GDELT Mass Media Streams
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
// MAGIC Also see the following that this work build on:
// MAGIC 
// MAGIC - https://github.com/lamastex/spark-gdelt-examples
// MAGIC 
// MAGIC ## What is The GDELT-Project?
// MAGIC 
// MAGIC From [https://www.gdeltproject.org/](https://www.gdeltproject.org/)
// MAGIC 
// MAGIC > **Watching our World Unfold**
// MAGIC 
// MAGIC > *A Global Database of Society*
// MAGIC 
// MAGIC > Supported by Google Jigsaw, the GDELT Project monitors the world's broadcast, print, and web news from nearly every corner of every country in over 100 languages and identifies the people, locations, organizations, themes, sources, emotions, counts, quotes, images and events driving our global society every second of every day, creating a free open platform for computing on the entire world.
// MAGIC 
// MAGIC 
// MAGIC ![spinningglobe gdelt project](https://www.gdeltproject.org/images/spinningglobe.gif)

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

import com.aamend.spark.gdelt._
import org.apache.spark.sql.Dataset
import spark.implicits._


// COMMAND ----------

// MAGIC %fs 
// MAGIC 
// MAGIC ls dbfs:/datasets/ScaDaMaLe/GDELT/

// COMMAND ----------

// MAGIC %md
// MAGIC ## Download from gdelt-project
// MAGIC 
// MAGIC 
// MAGIC **Just a pinky toe dip in this ocean of information!**
// MAGIC 
// MAGIC Here we just show briefly how you can download the zipped CSV files from the GDELT-project and turn them into DataSets using the libraries we have contributed to.
// MAGIC 
// MAGIC In order to scalably analyze this data one needs delta.io tables (just a couple more steps to turn these SparkSQL datasets actually). We use such delta.io tables we have built in another project to present more interesting detection of events and entities of interest in the sequel.
// MAGIC 
// MAGIC 
// MAGIC **Note** first that there are several types of GDELT datasets. There is indeed a large set of code-books and manuals one should first familiarise oneself with before trying to understand what all the structured data means - it is fairly detailed. These are available from manual searches through the gdelt project pages.
// MAGIC 
// MAGIC Here is a collection of them that will be immediately and minimally necessary to make cohesive sense of a large fraction of the datasets available to anyone:
// MAGIC 
// MAGIC - [https://github.com/lamastex/spark-gdelt-examples/tree/master/gdelt-docs](https://github.com/lamastex/spark-gdelt-examples/tree/master/gdelt-docs)

// COMMAND ----------

// MAGIC %fs
// MAGIC ls dbfs:///datasets/ScaDaMaLe/GDELT/

// COMMAND ----------

// MAGIC %md
// MAGIC ### Download Event v2 data from Gdelt.

// COMMAND ----------

// DBTITLE 0,Download Event v2 data from Gdelt.
// MAGIC %sh
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20190517121500.gkg.csv.zip
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20190523121500.gkg.csv.zip
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20180416121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip 20190517121500.gkg.csv.zip
// MAGIC unzip 20190523121500.gkg.csv.zip
// MAGIC unzip 20180416121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20180416121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/EventV2/20180416121500.gkg.csv"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190523121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/EventV2/20190523121500.gkg.csv"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190517121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/EventV2/20190517121500.gkg.csv"

// COMMAND ----------

val gdeltEventV2DS: Dataset[GKGEventV2] = spark.read.gdeltGkgV2("dbfs:/datasets/ScaDaMaLe/GDELT/EventV2/20190523121500.gkg.csv")

// COMMAND ----------

display(gdeltEventV2DS)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Download GKG v2 data from Gdelt.

// COMMAND ----------

// DBTITLE 0,Download GKG v2 data from Gdelt.
// MAGIC %sh
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20180417121500.gkg.csv.zip
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20190523121500.gkg.csv.zip
// MAGIC curl -O http://data.gdeltproject.org/gdeltv2/20190517121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %fs 
// MAGIC ls dbfs:///datasets/ScaDaMaLe/GDELT/

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip 20180417121500.gkg.csv.zip
// MAGIC unzip 20190523121500.gkg.csv.zip
// MAGIC unzip 20190517121500.gkg.csv.zip

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20180417121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/GKGV2/20180417121500.gkg.csv"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190523121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/GKGV2/20190523121500.gkg.csv"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190517121500.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/GKGV2/20190517121500.gkg.csv"

// COMMAND ----------

val gdeltGkgV2DS: Dataset[GKGEventV2] = spark.read.gdeltGkgV2("dbfs:///datasets/ScaDaMaLe/GDELT/GKGV2/20190517121500.gkg.csv")

// COMMAND ----------

display(gdeltGkgV2DS)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Dowload Gkg V1 Data from Gdelt

// COMMAND ----------

// DBTITLE 0,dowload Gkg V1data from Gdelt
// MAGIC %sh
// MAGIC curl -O http://data.gdeltproject.org/gkg/20190517.gkg.csv.zip
// MAGIC curl -O http://data.gdeltproject.org/gkg/20190523.gkg.csv.zip
// MAGIC curl -O http://data.gdeltproject.org/gkg/20180417.gkg.csv.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip 20190517.gkg.csv.zip
// MAGIC unzip 20190523.gkg.csv.zip
// MAGIC unzip 20180417.gkg.csv.zip

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20180417.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/GKGV1/20180417.gkg.csv"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver//20190523.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/GKGV1/20190523.gkg.csv"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190517.gkg.csv" "dbfs:///datasets/ScaDaMaLe/GDELT/GKGV1/20190517.gkg.csv"

// COMMAND ----------

val gdeltGkgV1DS: Dataset[GKGEventV1] = spark.read.gdeltGkgV1("dbfs:///datasets/ScaDaMaLe/GDELT/GKGV1/20190517.gkg.csv")

// COMMAND ----------

display(gdeltGkgV1DS)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Download Event v1 data from Gdelt

// COMMAND ----------

// DBTITLE 0,Download Event v1 data from Gdelt
// MAGIC %sh
// MAGIC curl -O http://data.gdeltproject.org/events/20190517.export.CSV.zip
// MAGIC curl -O http://data.gdeltproject.org/events/20190523.export.CSV.zip
// MAGIC curl -O http://data.gdeltproject.org/events/20180416.export.CSV.zip

// COMMAND ----------

// MAGIC %sh 
// MAGIC unzip 20190517.export.CSV.zip
// MAGIC unzip 20190523.export.CSV.zip
// MAGIC unzip 20180416.export.CSV.zip

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190517.export.CSV" "dbfs:///datasets/ScaDaMaLe/GDELT/EventV1/20190517.export.CSV"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20190523.export.CSV" "dbfs:///datasets/ScaDaMaLe/GDELT/EventV1/20190523.export.CSV"

// COMMAND ----------

// MAGIC %fs cp "file:///databricks/driver/20180416.export.CSV" "dbfs:///datasets/ScaDaMaLe/GDELT/EventV1/20180416.export.CSV"

// COMMAND ----------

// MAGIC %md
// MAGIC We have now downloaded and ingested the data in our distributed file store. It is time for some structured data processing using Datasets in Spark!

// COMMAND ----------

// MAGIC %md
// MAGIC ## GDELT SparkSQL Datasets
// MAGIC 
// MAGIC Let us turn these ingested CSV files into Spark Datasets next.

// COMMAND ----------

val gdeltEventV1DS: Dataset[EventV1] = spark.read.gdeltEventV1("dbfs:///datasets/ScaDaMaLe/GDELT/EventV1/20180416.export.CSV")

// COMMAND ----------

display(gdeltEventV1DS)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's look a the locations field. 
// MAGIC 
// MAGIC We want to be able to filter by a country.

// COMMAND ----------

val gdeltGkgDS: Dataset[GKGEventV2] = spark.read.gdeltGkgV2("dbfs:///datasets/ScaDaMaLe/GDELT/GKGV2/20190517121500.gkg.csv")

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
// MAGIC 
// MAGIC Here are various code-books used in the GDELT project. They are nicely available for you through the spark-gdelt library we have already loaded.

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

// COMMAND ----------

// MAGIC %fs
// MAGIC ls dbfs:/datasets/ScaDaMaLe/GDELT/