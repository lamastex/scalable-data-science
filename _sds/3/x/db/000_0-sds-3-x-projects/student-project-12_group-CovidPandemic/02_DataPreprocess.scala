// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Loading data

// COMMAND ----------

display(dbutils.fs.ls("/datasets/group12/"))

// COMMAND ----------

val df = spark.read.parquet("dbfs:/datasets/group12/analysis/*.parquet")

display(df)

// COMMAND ----------

df.count()

// COMMAND ----------

// MAGIC %md
// MAGIC ## Preprocessing
// MAGIC ### 0. filter out data of HongKong and unknown location

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

// COMMAND ----------

// filter unknow and HK data
val df_filteredLocation = df.filter($"iso_code"=!="HKG").filter($"iso_code".isNotNull)

// fill missing continent value for World data
val df_fillContinentNull = df_filteredLocation.na.fill("World",Array("continent")).cache
df_filteredLocation.unpersist()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. fill dates from 2020-01-23 (to ensure all countries having 316 days loggings)

// COMMAND ----------

// filter date before 2020-01-23
val df_filtered_date = df_fillContinentNull.filter($"date">"2020-01-22").cache
df_fillContinentNull.unpersist()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Fill missing value for total_cases, total_deaths, new_cases_smoothed, new_deaths_smoothed

// COMMAND ----------



// fill missing for new_cases_smoothed and new_deaths_smoothed
val df_fillNullForSmooth = df_filtered_date.na.fill(0,Array("new_cases_smoothed"))
                           .na.fill(0,Array("new_deaths_smoothed"))
                           .cache
df_filtered_date.unpersist()

// fill missing for total_cases
val df_fillNullForTotalCases = df_fillNullForSmooth.na.fill(0, Array("total_cases")).cache
df_fillNullForSmooth.unpersist()

// correct total_deaths, new_deaths, new_deaths_smoothed
val df_fillNullForTotalDeathsSpecial = df_fillNullForTotalCases.withColumn("total_deaths_correct", 
                                        when(col("iso_code").equalTo("ISL")&&(col("date")>"2020-03-13" && col("date")<"2020-03-22"),0)
                                       .when(col("iso_code").equalTo("PNG")&&(col("date")>"2020-07-19" && col("date")<"2020-07-23"),0)
                                       .when(col("iso_code").equalTo("SVK")&&(col("date")>"2020-03-17" && col("date")<"2020-03-23"),0).otherwise(col("total_deaths")))
                            .withColumn("new_deaths_correct", 
                                        when(col("iso_code").equalTo("ISL")&&(col("date")>"2020-03-13" && col("date")<"2020-03-22"),0)
                                       .when(col("iso_code").equalTo("PNG")&&(col("date")>"2020-07-19" && col("date")<"2020-07-23"),0)
                                       .when(col("iso_code").equalTo("SVK")&&(col("date")>"2020-03-17" && col("date")<"2020-03-23"),0).otherwise(col("new_deaths")))
                            .withColumn("new_deaths_smoothed_correct", 
                                        when(col("iso_code").equalTo("ISL")&&(col("date")>"2020-03-13" && col("date")<"2020-03-22"),0)
                                       .when(col("iso_code").equalTo("PNG")&&(col("date")>"2020-07-19" && col("date")<"2020-07-23"),0)
                                       .when(col("iso_code").equalTo("SVK")&&(col("date")>"2020-03-17" && col("date")<"2020-03-23"),0).otherwise(col("new_deaths_smoothed")))
                            .cache
df_fillNullForTotalCases.unpersist()

val df_cleaned = df_fillNullForTotalDeathsSpecial
                                  .drop("new_deaths", "total_deaths", "new_deaths_smoothed") // drop old column to rename
                                  .withColumnRenamed("new_deaths_correct","new_deaths")
                                  .withColumnRenamed("total_deaths_correct","total_deaths")
                                  .withColumnRenamed("new_deaths_smoothed_correct","new_deaths_smoothed")
                                  .na.fill(0, Array("total_deaths"))
                                  .select(df.columns.head, df.columns.tail: _*)
                                  .cache
df_fillNullForTotalDeathsSpecial.unpersist()

display(df_cleaned)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. Select invariant (during pandemic) features for clustering and filter out countries that have missing invariant features
// MAGIC 
// MAGIC Invariant feature list:
// MAGIC - population
// MAGIC - population_density
// MAGIC - median_age
// MAGIC - aged_65_older 
// MAGIC - aged_70_older
// MAGIC - gdp_per_capita
// MAGIC - cardiovasc_death_rate
// MAGIC - diabetes_prevalence
// MAGIC - female_smokers
// MAGIC - male_smokers
// MAGIC - hospital_beds_per_thousand
// MAGIC - life_expectancy
// MAGIC - human_development_index

// COMMAND ----------

// select invariant features
val df_invariantFeatures = df_cleaned.select($"iso_code",$"location", $"population",$"population_density",
                                             $"median_age", $"aged_65_older",
                                             $"aged_70_older",$"gdp_per_capita",
                                             $"cardiovasc_death_rate",$"diabetes_prevalence",
                                             $"female_smokers",$"male_smokers",$"hospital_beds_per_thousand",
                                             $"life_expectancy",$"human_development_index").cache

// Extract valid distrinct features RDD
val valid_distinct_features = df_invariantFeatures.distinct()
                                 .filter($"population".isNotNull && $"population_density".isNotNull && $"median_age".isNotNull && 
                                 $"aged_65_older".isNotNull && $"aged_70_older".isNotNull && $"gdp_per_capita".isNotNull &&
                                 $"cardiovasc_death_rate".isNotNull && $"diabetes_prevalence".isNotNull && $"female_smokers".isNotNull && 
                                 $"male_smokers".isNotNull && $"hospital_beds_per_thousand".isNotNull && $"life_expectancy".isNotNull &&
                                 $"human_development_index".isNotNull)


// filter out NULL feature countries
val df_cleaned_feature = df_cleaned.filter($"location".isin(valid_distinct_features.select($"location").rdd.map(r => r(0)).collect().toSeq: _*)).cache

df_cleaned.unpersist()

display(df_cleaned_feature)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 4. Imputing missing data for 
// MAGIC - total_cases_per_million
// MAGIC - new_cases_per_million
// MAGIC - new_cases_smoothed_per_million
// MAGIC - total_deaths_per_million
// MAGIC - new_deaths_per_million
// MAGIC - new_deaths_smoothed_per_million

// COMMAND ----------

val df_cleaned_feature_permillion = df_cleaned_feature.withColumn("total_cases_per_million_correct", df_cleaned_feature("total_cases")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("new_cases_per_million_correct", df_cleaned_feature("new_cases")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("new_cases_smoothed_per_million_correct", df_cleaned_feature("new_cases_smoothed")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("total_deaths_per_million_correct", df_cleaned_feature("total_deaths")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("new_deaths_per_million_correct", df_cleaned_feature("new_deaths")/df_cleaned_feature("population")*1000000)
                                                   .withColumn("new_deaths_smoothed_per_million_correct", df_cleaned_feature("new_deaths_smoothed")/df_cleaned_feature("population")*1000000)
                                                   .drop("total_cases_per_million", "new_cases_per_million", "new_cases_smoothed_per_million", 
                                                         "total_deaths_per_million", "new_deaths_per_million", "new_deaths_smoothed_per_million") // drop old column to rename
                                                   .withColumnRenamed("total_cases_per_million_correct","total_cases_per_million")
                                                   .withColumnRenamed("new_cases_per_million_correct","new_cases_per_million")
                                                   .withColumnRenamed("new_cases_smoothed_per_million_correct","new_cases_smoothed_per_million")
                                                   .withColumnRenamed("total_deaths_per_million_correct","total_deaths_per_million")
                                                   .withColumnRenamed("new_deaths_per_million_correct","new_deaths_per_million")
                                                   .withColumnRenamed("new_deaths_smoothed_per_million_correct","new_deaths_smoothed_per_million")

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5. Impute time series data of 
// MAGIC - reproduction_rate
// MAGIC - total_tests
// MAGIC - stringency_index
// MAGIC - total_tests_per_thousand

// COMMAND ----------


val df_cleaned_time_series = df_cleaned_feature_permillion
                              .withColumn("reproduction_rate", last("reproduction_rate", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .withColumn("reproduction_rate", first("reproduction_rate", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(0, df_cleaned_feature_permillion.count())))
                              .na.fill(0, Array("reproduction_rate"))
                              .withColumn("stringency_index", last("stringency_index", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .na.fill(0, Array("stringency_index"))
                              .withColumn("total_tests", last("total_tests", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .withColumn("total_tests", first("total_tests", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(0, df_cleaned_feature_permillion.count())))
                              .na.fill(0, Array("total_tests"))
                              .withColumn("total_tests_per_thousand", col("total_tests")/col("population")*1000)
                              .cache

df_cleaned_feature_permillion.unpersist()

display(df_cleaned_time_series)

// COMMAND ----------

