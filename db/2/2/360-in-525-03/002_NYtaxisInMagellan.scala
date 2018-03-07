// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
// MAGIC 
// MAGIC TODO: Raaz needs to descend into Ram's latest blog and introduce z-order curves form Samet's handbook on multi-dimensional metric data structures and a bit about catalyst optimisation in SparkSQL for contains queries...

// COMMAND ----------

// MAGIC %md
// MAGIC # Analysis of NY Taxi dataset in magellan
// MAGIC 
// MAGIC ### Replicating Ram Sriharsha's analysis by Victor Ingman.
// MAGIC 
// MAGIC See [https://magellan.ghost.io/magellan-geospatial-processing-made-easy/](https://magellan.ghost.io/magellan-geospatial-processing-made-easy/) for Ram's blog on this data analysis and learn about the dataset at [http://www.nyc.gov/html/tlc/html/about/trip_record_data.shtml](http://www.nyc.gov/html/tlc/html/about/trip_record_data.shtml).

// COMMAND ----------

// MAGIC %sh
// MAGIC # download data yall
// MAGIC # wget https://s3.amazonaws.com/nyc-tlc/trip+data/yellow_tripdata_2017-06.csv // the schema is different so this needs more work
// MAGIC wget https://s3.amazonaws.com/nyc-tlc/trip+data/yellow_tripdata_2015-07.csv

// COMMAND ----------

// MAGIC %sh
// MAGIC wget https://github.com/harsha2010/magellan/raw/master/examples/datasets/NYC-NEIGHBORHOODS/neighborhoods.geojson

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd

// COMMAND ----------

val PATH = "datasets/taxis" // dfs path
val LOCAL_PATH = "databricks/driver" // local shell path

val FILE = "yellow_tripdata_2015-07.csv"
val CSV_PATH = LOCAL_PATH + "/" + FILE

val NE_FILE = "neighborhoods.geojson"
val NE_FILE_PATH = LOCAL_PATH + "/" + NE_FILE

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/" + PATH)

// COMMAND ----------

dbutils.fs.cp("file:/" + CSV_PATH, "dbfs:/" + PATH)

// COMMAND ----------

dbutils.fs.cp("file:/" + NE_FILE_PATH, "dbfs:/" + PATH)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Parse data

// COMMAND ----------

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.magellan.dsl.expressions._
import org.apache.spark.sql.types._

import magellan.{Point, Polygon}

// COMMAND ----------

sc.textFile(PATH + "/" + FILE).take(1)

// COMMAND ----------

val schema = StructType(Array(
    StructField("vendorId", StringType, false),
    StructField("pickup_datetime", StringType, false),
    StructField("dropoff_datetime", StringType, false),
    StructField("passenger_count", IntegerType, false),
    StructField("trip_distance", DoubleType, false),
    StructField("pickup_longitude", DoubleType, false),
    StructField("pickup_latitude", DoubleType, false),
    StructField("rateCodeId", StringType, false),
    StructField("store_fwd", StringType, false),
    StructField("dropoff_longitude", DoubleType, false),
    StructField("dropoff_latitude", DoubleType, false),
    StructField("payment_type", StringType, false),
    StructField("fare_amount", StringType, false),
    StructField("extra", StringType, false),
    StructField("mta_tax", StringType, false),
    StructField("tip_amount", StringType, false),
    StructField("tolls_amount", StringType, false),
    StructField("improvement_surcharge", StringType, false),
    StructField("total_amount", DoubleType, false)))

// COMMAND ----------

val trips = sqlContext
  .read
  .format("com.databricks.spark.csv")
  .option("comment", "V")
  .option("mode", "DROPMALFORMED")
  .schema(schema)
  .load("dbfs:/" + PATH + "/" + FILE)
  .withColumn("point", point($"pickup_longitude", $"pickup_latitude"))
  .cache()

// COMMAND ----------

trips.count()
trips.take(1)

// COMMAND ----------

val neighbourhoods = sqlContext
  .read
  .format("magellan")
  .option("type", "geojson")
  .load("dbfs:/" + PATH + "/" + NE_FILE)
  .select($"polygon", $"metadata"("neighborhood").as("neighborhood"))
  .cache()

// COMMAND ----------

neighbourhoods.count()

// COMMAND ----------

neighbourhoods.printSchema()

// COMMAND ----------

val joined = trips
  .join(neighbourhoods)
  .where($"point" within $"polygon")

// COMMAND ----------

joined.printSchema

// COMMAND ----------

display(
  joined
    .groupBy('neighborhood)
    .count()
    .orderBy($"count".desc)
)