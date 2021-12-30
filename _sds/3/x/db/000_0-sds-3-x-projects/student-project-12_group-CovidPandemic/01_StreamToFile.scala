// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Stream to parquet file
// MAGIC This notebook allows for setup and execution of the data streaming and querying into a parquet file. The idea is thereafter to perform analysis on the parquet file.
// MAGIC 
// MAGIC Note that this notebooks assumes one has already has downloaded several "Our World in Data" dataset csv files. This can be done by first running "DownloadFilesPeriodicallyScript" at least once.
// MAGIC 
// MAGIC Content is based on "038_StructuredStreamingProgGuide" by Raazesh Sainudiin. 

// COMMAND ----------

// MAGIC %md start by copying latest downloaded csv data to data analysis folder

// COMMAND ----------

dbutils.fs.cp("file:///databricks/driver/projects/group12/logsEveryXSecs/","/datasets/group12/",true)

// COMMAND ----------

// MAGIC %md check that data is in the group12 folder

// COMMAND ----------

display(dbutils.fs.ls("/datasets/group12/"))

// COMMAND ----------

// MAGIC %md check the schema for the csv files.

// COMMAND ----------

val df_csv = spark.read.format("csv").option("header", "true").option("inferSchema", "true").csv("/datasets/group12/21_01_07_09_05_33.csv")

// COMMAND ----------

df_csv.printSchema

// COMMAND ----------

// MAGIC %md The stream requires a user defined schema. Note that the January 2021 schema is different compared to the December 2020 schema. Below, the user defined schemas are created.

// COMMAND ----------

import org.apache.spark.sql.types._

val OurWorldinDataSchema2021 = new StructType()                      
                      .add("iso_code", "string")
                      .add("continent", "string")
                      .add("location", "string")
                      .add("date", "string")
                      .add("total_cases","double")
                      .add("new_cases","double")
                      .add("new_cases_smoothed","double")
                      .add("total_deaths","double")
                      .add("new_deaths","double")
                      .add("new_deaths_smoothed","double")
                      .add("total_cases_per_million","double")
                      .add("new_cases_per_million","double")
                      .add("new_cases_smoothed_per_million","double")
                      .add("total_deaths_per_million","double")
                      .add("new_deaths_per_million","double")
                      .add("new_deaths_smoothed_per_million","double")
                      .add("reproduction_rate", "double")
                      .add("icu_patients", "double")
                      .add("icu_patients_per_million", "double")
                      .add("hosp_patients", "double")
                      .add("hosp_patients_per_million", "double")
                      .add("weekly_icu_admissions", "double")
                      .add("weekly_icu_admissions_per_million", "double")
                      .add("weekly_hosp_admissions", "double")
                      .add("weekly_hosp_admissions_per_million", "double")
                      .add("new_tests", "double")
                      .add("total_tests", "double")
                      .add("total_tests_per_thousand", "double")
                      .add("new_tests_per_thousand", "double")
                      .add("new_tests_smoothed", "double")
                      .add("new_tests_smoothed_per_thousand", "double")
                      .add("positive_rate", "double")
                      .add("tests_per_case", "double")
                      .add("tests_units", "double")
                      .add("total_vaccinations", "double")
                      .add("new_vaccinations", "double")
                      .add("stringency_index","double")
                      .add("population","double")
                      .add("population_density","double")
                      .add("median_age", "double")
                      .add("aged_65_older", "double")
                      .add("aged_70_older", "double")
                      .add("gdp_per_capita","double")
                      .add("extreme_poverty","double")
                      .add("cardiovasc_death_rate","double")
                      .add("diabetes_prevalence","double")
                      .add("female_smokers", "double")
                      .add("male_smokers", "double")
                      .add("handwashing_facilities", "double")
                      .add("hospital_beds_per_thousand", "double")
                      .add("life_expectancy","double")
                      .add("human_development_index","double")

val OurWorldinDataSchema2020 = new StructType()                      
                      .add("iso_code", "string")
                      .add("continent", "string")
                      .add("location", "string")
                      .add("date", "string")
                      .add("total_cases","double")
                      .add("new_cases","double")
                      .add("new_cases_smoothed","double")
                      .add("total_deaths","double")
                      .add("new_deaths","double")
                      .add("new_deaths_smoothed","double")
                      .add("total_cases_per_million","double")
                      .add("new_cases_per_million","double")
                      .add("new_cases_smoothed_per_million","double")
                      .add("total_deaths_per_million","double")
                      .add("new_deaths_per_million","double")
                      .add("new_deaths_smoothed_per_million","double")
                      .add("reproduction_rate", "double")
                      .add("icu_patients", "double")
                      .add("icu_patients_per_million", "double")
                      .add("hosp_patients", "double")
                      .add("hosp_patients_per_million", "double")
                      .add("weekly_icu_admissions", "double")
                      .add("weekly_icu_admissions_per_million", "double")
                      .add("weekly_hosp_admissions", "double")
                      .add("weekly_hosp_admissions_per_million", "double")
                      .add("total_tests", "double")
                      .add("new_tests", "double")
                      .add("total_tests_per_thousand", "double")
                      .add("new_tests_per_thousand", "double")
                      .add("new_tests_smoothed", "double")
                      .add("new_tests_smoothed_per_thousand", "double")
                      .add("tests_per_case", "double")
                      .add("positive_rate", "double")
                      .add("tests_units", "double")
                      .add("stringency_index","double")
                      .add("population","double")
                      .add("population_density","double")
                      .add("median_age", "double")
                      .add("aged_65_older", "double")
                      .add("aged_70_older", "double")
                      .add("gdp_per_capita","double")
                      .add("extreme_poverty","double")
                      .add("cardiovasc_death_rate","double")
                      .add("diabetes_prevalence","double")
                      .add("female_smokers", "double")
                      .add("male_smokers", "double")
                      .add("handwashing_facilities", "double")
                      .add("hospital_beds_per_thousand", "double")
                      .add("life_expectancy","double")
                      .add("human_development_index","double")

// COMMAND ----------

// MAGIC %md ### Start stream
// MAGIC In January 2021, the schema was updated compared to the schema in December 2020. Below, one can choose which type of csv files to stream below.

// COMMAND ----------

// MAGIC %md Stream for 2020

// COMMAND ----------

import org.apache.spark.sql.types._

val OurWorldinDataStream = spark
  .readStream
  .schema(OurWorldinDataSchema2020) 
  .option("MaxFilesPerTrigger", 1)
  .option("latestFirst", "true")
  .format("csv")
  .option("header", "true")
  .load("/datasets/group12/20*.csv")
  .dropDuplicates()

// COMMAND ----------

// MAGIC %md Stream for 2021

// COMMAND ----------

import org.apache.spark.sql.types._

val OurWorldinDataStream2021 = spark
  .readStream
  .schema(OurWorldinDataSchema2021) 
  .option("MaxFilesPerTrigger", 1)
  .option("latestFirst", "true")
  .format("csv")
  .option("header", "true")
  .load("/datasets/group12/21*.csv")
  .dropDuplicates()

// COMMAND ----------

// MAGIC %md display stream 2020

// COMMAND ----------

OurWorldinDataStream.isStreaming

// COMMAND ----------

display(OurWorldinDataStream) 

// COMMAND ----------

// MAGIC %md ### Query to File (2020)
// MAGIC query that saves file into a parquet file at periodic intervalls. Analysis will thereafter be performed on the parquet file

// COMMAND ----------

// MAGIC %md create folders for parquet file and checkpoint data

// COMMAND ----------

// remove any previous folders if exists
dbutils.fs.rm("datasets/group12/chkpoint",recurse=true)
dbutils.fs.rm("datasets/group12/analysis",recurse=true)

// COMMAND ----------

dbutils.fs.mkdirs("datasets/group12/chkpoint")

// COMMAND ----------

dbutils.fs.mkdirs("/datasets/group12/analysis")

// COMMAND ----------

// MAGIC %md initialize query to store data in parquet files based on column selection

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

val query = OurWorldinDataStream
                 .select($"iso_code", $"continent", $"location", $"date", $"total_cases", $"new_cases", $"new_cases_smoothed", $"total_deaths", $"new_deaths",$"new_deaths_smoothed", $"total_cases_per_million", $"new_cases_per_million", $"new_cases_smoothed_per_million", $"total_deaths_per_million", $"new_deaths_per_million", $"new_deaths_smoothed_per_million", $"reproduction_rate", $"icu_patients", $"icu_patients_per_million", $"hosp_patients", $"hosp_patients_per_million", $"weekly_icu_admissions", $"weekly_icu_admissions_per_million", $"weekly_hosp_admissions", $"weekly_hosp_admissions_per_million", $"total_tests",$"new_tests", $"total_tests_per_thousand", $"new_tests_per_thousand", $"new_tests_smoothed",$"new_tests_smoothed_per_thousand", $"tests_per_case", $"positive_rate", $"tests_units", $"stringency_index", $"population", $"population_density", $"median_age", $"aged_65_older", $"aged_70_older", $"gdp_per_capita", $"extreme_poverty", $"cardiovasc_death_rate", $"diabetes_prevalence", $"female_smokers", $"male_smokers", $"handwashing_facilities", $"hospital_beds_per_thousand", $"life_expectancy", $"human_development_index")
                 .writeStream
                 //.trigger(Trigger.ProcessingTime("20 seconds")) // debugging
                 .trigger(Trigger.ProcessingTime("216000 seconds")) // for each day
                 .option("checkpointLocation", "/datasets/group12/chkpoint")
                 .format("parquet")  
                 .option("path", "/datasets/group12/analysis")
                 .start()
                 
query.awaitTermination() // hit cancel to terminate

// COMMAND ----------

// MAGIC %md check saved parquet file contents

// COMMAND ----------

display(dbutils.fs.ls("/datasets/group12/analysis"))

// COMMAND ----------

val parquetFileDF = spark.read.parquet("dbfs:/datasets/group12/analysis/*.parquet")

// COMMAND ----------

display(parquetFileDF.describe())

// COMMAND ----------

display(parquetFileDF.orderBy($"date".desc))

// COMMAND ----------

parquetFileDF.count()

// COMMAND ----------

// MAGIC %md ### Query to File (2021)
// MAGIC query that saves file into a parquet file at periodic intervalls. 

// COMMAND ----------

// remove any previous folders if exists
dbutils.fs.rm("datasets/group12/chkpoint2021",recurse=true)
dbutils.fs.rm("datasets/group12/analysis2021",recurse=true)

// COMMAND ----------

dbutils.fs.mkdirs("datasets/group12/chkpoint2021")
dbutils.fs.mkdirs("datasets/group12/analysis2021")

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

val query = OurWorldinDataStream2021
                 .select($"iso_code", $"continent", $"location", $"date", $"total_cases", $"new_cases", $"new_cases_smoothed", $"total_deaths", $"new_deaths",$"new_deaths_smoothed", $"total_cases_per_million", $"new_cases_per_million", $"new_cases_smoothed_per_million", $"total_deaths_per_million", $"new_deaths_per_million", $"new_deaths_smoothed_per_million", $"reproduction_rate", $"icu_patients", $"icu_patients_per_million", $"hosp_patients", $"hosp_patients_per_million", $"weekly_icu_admissions", $"weekly_icu_admissions_per_million", $"weekly_hosp_admissions", $"weekly_hosp_admissions_per_million", $"total_tests",$"new_tests", $"total_tests_per_thousand", $"new_tests_per_thousand", $"new_tests_smoothed",$"new_tests_smoothed_per_thousand", $"tests_per_case", $"positive_rate", $"tests_units", $"stringency_index", $"population", $"population_density", $"median_age", $"aged_65_older", $"aged_70_older", $"gdp_per_capita", $"extreme_poverty", $"cardiovasc_death_rate", $"diabetes_prevalence", $"female_smokers", $"male_smokers", $"handwashing_facilities", $"hospital_beds_per_thousand", $"life_expectancy", $"human_development_index")
                 .writeStream
                 //.trigger(Trigger.ProcessingTime("20 seconds")) // debugging
                 .trigger(Trigger.ProcessingTime("216000 seconds")) // each day
                 .option("checkpointLocation", "/datasets/group12/chkpoint2021")
                 .format("parquet")  
                 .option("path", "/datasets/group12/analysis2021")
                 .start()
                 
query.awaitTermination() // hit cancel to terminate

// COMMAND ----------

val parquetFile2021DF = spark.read.parquet("dbfs:/datasets/group12/analysis2021/*.parquet")

// COMMAND ----------

display(parquetFile2021DF.describe())

// COMMAND ----------

