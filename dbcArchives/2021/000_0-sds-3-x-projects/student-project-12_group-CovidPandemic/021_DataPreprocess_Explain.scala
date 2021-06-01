// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Loading data

// COMMAND ----------

display(dbutils.fs.ls("/datasets/group12/"))

// COMMAND ----------

// MAGIC %md
// MAGIC Load parquet file

// COMMAND ----------

val df = spark.read.parquet("dbfs:/datasets/group12/analysis/*.parquet")

display(df)


// COMMAND ----------

// MAGIC %md
// MAGIC Load csv file

// COMMAND ----------

// MAGIC %md -> %scala //if want to load csv
// MAGIC 
// MAGIC val file_location = "/datasets/group12/20_12_04_10_47_08.csv"
// MAGIC val file_type = "csv"
// MAGIC 
// MAGIC // CSV options
// MAGIC val infer_schema = "true"
// MAGIC val first_row_is_header = "true"
// MAGIC val delimiter = ","
// MAGIC 
// MAGIC // The applied options are for CSV files. For other file types, these will be ignored.
// MAGIC val df = spark.read.format(file_type)
// MAGIC   .option("inferSchema", infer_schema)
// MAGIC   .option("header", first_row_is_header)
// MAGIC   .option("sep", delimiter)
// MAGIC   .load(file_location)
// MAGIC 
// MAGIC display(df)

// COMMAND ----------

// MAGIC %md
// MAGIC Number of data

// COMMAND ----------

df.count()

// COMMAND ----------

// MAGIC %md
// MAGIC Missing Features in data due to multiple web resource

// COMMAND ----------

// MAGIC %scala
// MAGIC import org.apache.spark.sql.functions._
// MAGIC 
// MAGIC for (c <- df.columns) {
// MAGIC   println(c + ": " + df.filter(col(c).isNull).count())
// MAGIC }

// COMMAND ----------

// MAGIC %md
// MAGIC ## Preprocessing
// MAGIC ### 0. filter out data of HongKong and unknown location

// COMMAND ----------

// MAGIC %md
// MAGIC Here shows HK does not have meaningful value and there is one unknown international location in data.

// COMMAND ----------

display(df.filter($"location"==="Hong Kong" || $"iso_code".isNull)) //HK data iteself is not complete for all dates, and all available data is null! HAVE TO FILTER IT OUT COMPLETELY

// COMMAND ----------

// MAGIC %md
// MAGIC 190 valid countries data to continue

// COMMAND ----------

val df_filteredLocation = df.filter($"iso_code"=!="HKG").filter($"iso_code".isNotNull)
display(df_filteredLocation.select($"location").distinct()) // 190 valid countries 

// COMMAND ----------

// MAGIC %md
// MAGIC Fill missing continent value for World aggregate data
// MAGIC NOTE: it will be filled as "World"

// COMMAND ----------

display(df_filteredLocation.where($"continent".isNull))

// COMMAND ----------

val df_fillContinentNull = df_filteredLocation.na.fill("World",Array("continent"))
display(df_fillContinentNull)

// COMMAND ----------

df_fillContinentNull.count()

// COMMAND ----------

// MAGIC %scala
// MAGIC import org.apache.spark.sql.functions._
// MAGIC 
// MAGIC for (c <- df_fillContinentNull.columns) {
// MAGIC   println(c + ": " + df_fillContinentNull.filter(col(c).isNull).count())
// MAGIC }

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. filter dates only from 2020-01-23 (to ensure all countries having 316 days logging)

// COMMAND ----------

display(df_fillContinentNull.select($"date",$"iso_code").groupBy($"iso_code").count())  // some country starts logging data earlier

// COMMAND ----------

val df_filtered_date = df_fillContinentNull.filter($"date">"2020-01-22")

// COMMAND ----------

display(df_filtered_date.select($"date",$"iso_code").groupBy($"iso_code").count())  // all countries have 316 days logging

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Fill missing value for total_cases, total_deaths, new_cases_smoothed, new_deaths_smoothed

// COMMAND ----------

display(df_filtered_date.select($"date",$"iso_code", $"total_cases", $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", $"new_deaths_smoothed").filter($"new_cases_smoothed".isNull || $"new_deaths_smoothed".isNull))

// COMMAND ----------

// MAGIC %md
// MAGIC All missing data of new_cases_smoothed and new_deaths_smoothed from early, so just fill with 0

// COMMAND ----------

display(df_filtered_date.select($"date",$"iso_code", $"total_cases", $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", $"new_deaths_smoothed")
        .filter($"new_cases_smoothed".isNull || $"new_deaths_smoothed".isNull).select($"date").distinct())

// COMMAND ----------

val df_fillNullForSmooth = df_filtered_date.na.fill(0,Array("new_cases_smoothed"))
                           .na.fill(0,Array("new_deaths_smoothed"))
display(df_fillNullForSmooth)

// COMMAND ----------

// MAGIC %md 
// MAGIC Fill total_deaths and total_cases null value
// MAGIC 
// MAGIC Strictly, when new_cases is always 0, total_cases could be imputed as 0. The same apply to total_deaths

// COMMAND ----------

val df_NULL_total_cases = df_fillNullForSmooth.select($"date",$"iso_code", $"total_cases", $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", $"new_deaths_smoothed")
                          .filter($"total_cases".isNull)


display(df_NULL_total_cases.filter($"new_cases"===0).groupBy("iso_code").count())

// COMMAND ----------

// MAGIC %md
// MAGIC When total_case is Null, all previous new_cases is always 0.

// COMMAND ----------

df_NULL_total_cases.filter($"total_cases".isNull).groupBy("iso_code").count().except(df_NULL_total_cases.filter($"new_cases"===0).groupBy("iso_code").count()).show() // When total_case is Null, all new_cases is always 0

// COMMAND ----------

val df_fillNullForTotalCases = df_fillNullForSmooth.na.fill(0, Array("total_cases"))
                               
display(df_fillNullForTotalCases)

// COMMAND ----------

val df_NULL_total_death = df_fillNullForTotalCases.select($"date",$"iso_code", $"total_cases", $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", $"new_deaths_smoothed")
                          .filter($"total_deaths".isNull)


display(df_NULL_total_death.filter($"new_deaths"===0).groupBy("iso_code").count().sort())

// COMMAND ----------

// MAGIC %md
// MAGIC If total_deaths is Null when all new_deaths is always 0, then we could simply assign 0 for NULL, otherwise need to investigate more.
// MAGIC 
// MAGIC Three countries (ISL, PNG, SVK) have abnormal correction on new_cases data. 

// COMMAND ----------

val abnormal_countries = df_NULL_total_death.filter($"total_deaths".isNull).groupBy("iso_code").count().except(df_NULL_total_death.filter($"new_deaths"===0).groupBy("iso_code").count())
abnormal_countries.show()
df_NULL_total_death.filter($"new_deaths"===0).groupBy("iso_code").count().except(df_NULL_total_death.filter($"total_deaths".isNull).groupBy("iso_code").count()).show()


// COMMAND ----------

// MAGIC %md
// MAGIC show abnormal death correction

// COMMAND ----------

display(df_fillNullForSmooth.filter($"iso_code"==="ISL").sort("date").filter($"date">"2020-03-13" && $"date"<"2020-03-22")) // death data correction between 2020-03-14 and 2020-03-21, total_deaths -> all 0, new_deaths -> all 0, new_deaths_smoothed -> all 0

// COMMAND ----------

display(df_fillNullForSmooth.filter($"iso_code"==="PNG").sort("date").filter($"date">"2020-07-19" && $"date"<"2020-07-24" )) // death data correction between 2020-07-20 and 2020-07-22, total_deaths -> all 0, new_deaths -> all 0, new_deaths_smoothed -> all 0

// COMMAND ----------

display(df_fillNullForSmooth.filter($"iso_code"==="SVK").sort("date").filter($"date">"2020-03-16" && $"date"<"2020-03-23")) // death data correction between 2020-03-18 and 2020-03-22, total_deaths -> all 0, new_deaths -> all 0, new_deaths_smoothed -> all 0

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Correct new_deaths correction back to 0

// COMMAND ----------

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



// COMMAND ----------

// MAGIC %md
// MAGIC Expect to see an empty table, so correction is right

// COMMAND ----------

val df_NULL_total_death_ = df_fillNullForTotalDeathsSpecial.select($"date",$"iso_code", $"total_cases", $"total_deaths_correct", $"new_cases", $"new_deaths_correct", $"new_cases_smoothed", $"new_deaths_smoothed_correct")
                          .filter($"total_deaths_correct".isNull)


df_NULL_total_death_.filter($"total_deaths_correct".isNull).groupBy("iso_code").count().except(df_NULL_total_death_.filter($"new_deaths_correct"===0).groupBy("iso_code").count()).show()

// COMMAND ----------

// MAGIC %md
// MAGIC fill rest NULL value for total_death.

// COMMAND ----------

val df_fillNullForTotalDeaths = df_fillNullForTotalDeathsSpecial
                                  .drop("new_deaths", "total_deaths", "new_deaths_smoothed") // drop old column to rename
                                  .withColumnRenamed("new_deaths_correct","new_deaths")
                                  .withColumnRenamed("total_deaths_correct","total_deaths")
                                  .withColumnRenamed("new_deaths_smoothed_correct","new_deaths_smoothed")
                                  .na.fill(0, Array("total_deaths"))
                                  .select(df.columns.head, df.columns.tail: _*)
display(df_fillNullForTotalDeaths)

// COMMAND ----------

// MAGIC %md
// MAGIC ### All first 10 column is clean now! 
// MAGIC 
// MAGIC (All code above is for illustration, for processing just run cell below )

// COMMAND ----------


// filter unknow and HK data
val df_filteredLocation = df.filter($"iso_code"=!="HKG").filter($"iso_code".isNotNull)

// fill missing continent value for World data
val df_fillContinentNull = df_filteredLocation.na.fill("World",Array("continent")).cache
df_filteredLocation.unpersist()

// filter date before 2020-01-23
val df_filtered_date = df_fillContinentNull.filter($"date">"2020-01-22").cache
df_fillContinentNull.unpersist()

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

// MAGIC %scala
// MAGIC import org.apache.spark.sql.functions._
// MAGIC 
// MAGIC for (c <- df_cleaned.columns) {
// MAGIC   println(c + ": " + df_cleaned.filter(col(c).isNull).count())
// MAGIC }

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. select invariant (during pandemic) features for clustering
// MAGIC double check whether they are constant for each country, and if not, change all the value to mean
// MAGIC and filter out countries that have missing constant features
// MAGIC 
// MAGIC Candidate list:
// MAGIC - population
// MAGIC - population_density
// MAGIC - median_age
// MAGIC - aged_65_older 
// MAGIC - aged_70_older
// MAGIC - gdp_per_capita
// MAGIC 
// MAGIC - cardiovasc_death_rate
// MAGIC - diabetes_prevalence
// MAGIC - female_smokers
// MAGIC - male_smokers
// MAGIC - hospital_beds_per_thousand
// MAGIC - life_expectancy
// MAGIC - human_development_index

// COMMAND ----------

val df_invariantFeatures = df_cleaned.select($"location", $"population",$"population_density",
                                             $"median_age", $"aged_65_older",
                                             $"aged_70_older",$"gdp_per_capita",
                                             $"cardiovasc_death_rate",$"diabetes_prevalence",
                                             $"female_smokers",$"male_smokers",$"hospital_beds_per_thousand",
                                             $"life_expectancy",$"human_development_index")
display(df_invariantFeatures)

// COMMAND ----------

display(df_invariantFeatures.describe())

// COMMAND ----------

for (c <- df_invariantFeatures.columns) {
  println(c + ": " + df_invariantFeatures.filter(col(c).isNull).count())
}

// COMMAND ----------

// MAGIC %md
// MAGIC Although some countries seems like an outlier, it does have constant female_smokers and male_smokers

// COMMAND ----------

val constant_feature_checker = df_cleaned.groupBy("location")
          .agg(
              //stddev("stringency_index").as("std_si"),         
              stddev("population").as("std_pop"),           
              stddev("population_density").as("std_pd"),
              stddev("median_age").as("std_ma"),         
              stddev("aged_65_older").as("std_a65"),           
              stddev("aged_70_older").as("std_a70"),  
              stddev("gdp_per_capita").as("std_gdp"),
              stddev("cardiovasc_death_rate").as("std_cdr"),         
              stddev("diabetes_prevalence").as("std_dp"),           
              stddev("female_smokers").as("std_fs"),      
              stddev("male_smokers").as("std_ms"),        
              stddev("hospital_beds_per_thousand").as("std_hbpt"),           
              stddev("life_expectancy").as("std_le"),
              stddev("human_development_index").as("std_hdi")
            )
           .where(
                  (col("std_pop") > 0) || (col("std_pd") > 1e-20) || (col("std_ma") > 0) || (col("std_a65") > 0) || (col("std_a70") > 0) || (col("std_gdp") > 0 ||
                   col("std_cdr") > 0) || (col("std_dp") > 0) || (col("std_fs") > 0) || (col("std_ms") > 0) || (col("std_hbpt") > 0) || (col("std_le") > 0) || (col("std_hdi") > 0))

display(constant_feature_checker)

// COMMAND ----------

// MAGIC %md
// MAGIC Each country have some constant features always

// COMMAND ----------

val distinct_features = df_invariantFeatures.distinct()

display(distinct_features)

// COMMAND ----------

// MAGIC %md
// MAGIC In total, 126 countries have complete features

// COMMAND ----------

val valid_distinct_features = distinct_features.filter($"population".isNotNull && $"population_density".isNotNull && $"median_age".isNotNull && 
                         $"aged_65_older".isNotNull && $"aged_70_older".isNotNull && $"gdp_per_capita".isNotNull &&
                         $"cardiovasc_death_rate".isNotNull && $"diabetes_prevalence".isNotNull && $"female_smokers".isNotNull && 
                         $"male_smokers".isNotNull && $"hospital_beds_per_thousand".isNotNull && $"life_expectancy".isNotNull &&
                         $"human_development_index".isNotNull)
display(valid_distinct_features)

// COMMAND ----------

// MAGIC %md
// MAGIC country list

// COMMAND ----------

display(valid_distinct_features.select($"location"))

// COMMAND ----------

valid_distinct_features.select($"location").count()

// COMMAND ----------

val df_cleaned_feature = df_cleaned.filter($"location".isin(valid_distinct_features.select($"location").rdd.map(r => r(0)).collect().toSeq: _*))

display(df_cleaned_feature)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### All data contains complete list of invariant time feature
// MAGIC 
// MAGIC (All code above is for illustration, for processing just run cell below )

// COMMAND ----------

// select invariant features
val df_invariantFeatures = df_cleaned.select($"location", $"population",$"population_density",
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
                                 $"human_development_index".isNotNull).cache

df_invariantFeatures.unpersist()

// filter out NULL feature countries
val df_cleaned_feature = df_cleaned.filter($"location".isin(valid_distinct_features.select($"location").rdd.map(r => r(0)).collect().toSeq: _*)).cache

df_cleaned.unpersist()

display(df_cleaned_feature)

// COMMAND ----------

// MAGIC %scala
// MAGIC import org.apache.spark.sql.functions._
// MAGIC 
// MAGIC for (c <- df_cleaned_feature.columns) {
// MAGIC   println(c + ": " + df_cleaned_feature.filter(col(c).isNull).count())
// MAGIC }

// COMMAND ----------

// MAGIC %md
// MAGIC ### 4. Imputing missing time series data of 
// MAGIC - total_cases_per_million
// MAGIC - new_cases_per_million
// MAGIC - new_cases_smoothed_per_million
// MAGIC - total_deaths_per_million
// MAGIC - new_deaths_per_million
// MAGIC - new_deaths_smoothed_per_million

// COMMAND ----------

val per_million_data = df_cleaned_feature.select($"location", $"date", $"iso_code", $"total_cases", 
                                                 $"total_deaths", $"new_cases", $"new_deaths", $"new_cases_smoothed", 
                                                 $"new_deaths_smoothed", $"population", $"population_density", 
                                                 $"total_cases_per_million", $"new_cases_per_million", $"new_cases_smoothed_per_million", 
                                                 $"total_deaths_per_million", $"new_deaths_per_million", $"new_deaths_smoothed_per_million")

display(per_million_data)

// COMMAND ----------

val per_million_data_corrected = per_million_data.withColumn("total_cases_per_million_correct", per_million_data("total_cases")/per_million_data("population")*1000000)
                                                 .withColumn("new_cases_per_million_correct", per_million_data("new_cases")/per_million_data("population")*1000000)
                                                 .withColumn("new_cases_smoothed_per_million_correct", per_million_data("new_cases_smoothed")/per_million_data("population")*1000000)
                                                 .withColumn("total_deaths_per_million_correct", per_million_data("total_deaths")/per_million_data("population")*1000000)
                                                 .withColumn("new_deaths_per_million_correct", per_million_data("new_deaths")/per_million_data("population")*1000000)
                                                 .withColumn("new_deaths_smoothed_per_million_correct", per_million_data("new_deaths_smoothed")/per_million_data("population")*1000000)
                                                 .drop("total_cases_per_million", "new_cases_per_million", "new_cases_smoothed_per_million", 
                                                       "total_deaths_per_million", "new_deaths_per_million", "new_deaths_smoothed_per_million") // drop old column to rename
                                                 .withColumnRenamed("total_cases_per_million_correct","total_cases_per_million")
                                                 .withColumnRenamed("new_cases_per_million_correct","new_cases_per_million")
                                                 .withColumnRenamed("new_cases_smoothed_per_million_correct","new_cases_smoothed_per_million")
                                                 .withColumnRenamed("total_deaths_per_million_correct","total_deaths_per_million")
                                                 .withColumnRenamed("new_deaths_per_million_correct","new_deaths_per_million")
                                                 .withColumnRenamed("new_deaths_smoothed_per_million_correct","new_deaths_smoothed_per_million")

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
// MAGIC ### 5. Impute time series of 
// MAGIC - reproduction_rate
// MAGIC - total_tests
// MAGIC - stringency_index
// MAGIC - total_tests_per_thousand

// COMMAND ----------

// MAGIC %md
// MAGIC fill null in reproduction_rate by last available and next available value
// MAGIC 
// MAGIC All countries has missing data at beginning or in the end

// COMMAND ----------

display(df_cleaned_feature_permillion.select($"reproduction_rate", $"location", $"date").filter($"reproduction_rate".isNull).groupBy("location").count().sort("location"))

// COMMAND ----------

display(df_cleaned_feature_permillion.select($"reproduction_rate", $"location", $"date").filter($"reproduction_rate".isNull).groupBy("location").agg(max("date").as("max_date"), min("date").as("min_date")).sort("location"))

// COMMAND ----------

display(df_cleaned_feature_permillion.select($"reproduction_rate", $"location", $"date").filter($"location"==="Albania"))

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

val df_cleaned_reproduction_rate= df_cleaned_feature_permillion.select($"reproduction_rate", $"location", $"date")
                              .withColumn("reproduction_rate", last("reproduction_rate", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .withColumn("reproduction_rate", first("reproduction_rate", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(0, df_cleaned_feature_permillion.count())))
                              .na.fill(0, Array("reproduction_rate"))

// COMMAND ----------

// MAGIC %md
// MAGIC countries miss stringency_index value

// COMMAND ----------

display(df_cleaned_feature_permillion.select($"stringency_index", $"location", $"date").filter($"stringency_index".isNull).groupBy("location").count().sort("count"))

// COMMAND ----------

// MAGIC %md
// MAGIC start and end date for null value of stringency_index for each country

// COMMAND ----------

display(df_cleaned_feature_permillion.select($"stringency_index", $"location", $"date").filter($"stringency_index".isNull).groupBy("location").agg(max("date").as("max_date"), min("date").as("min_date")))

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

val df_cleaned_stringency = df_cleaned_feature_permillion.select($"stringency_index", $"location", $"date")
                              .withColumn("stringency_index_corect", last("stringency_index", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
display(df_cleaned_stringency.filter($"stringency_index".isNull).filter($"stringency_index_corect".isNull).groupBy("location").count())

// COMMAND ----------

// MAGIC %md
// MAGIC total_tests, impute by last available or next available value

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

val df_cleaned_total_cases = df_cleaned_feature_permillion.select($"total_tests", $"location", $"date")
                              .withColumn("total_tests", last("total_tests", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(-df_cleaned_feature_permillion.count(), 0)))
                              .withColumn("total_tests", first("total_tests", true)
                                         .over(Window.partitionBy("location").orderBy("date").rowsBetween(0, df_cleaned_feature_permillion.count())))
                              .na.fill(0, Array("total_tests"))

// COMMAND ----------

display(df_cleaned_feature_permillion.select($"total_tests", $"location", $"date").filter($"total_tests".isNull).groupBy("location").count())

// COMMAND ----------

val total_tests_date_maxmin = df_cleaned_feature_permillion.select($"total_tests", $"location", $"date").filter($"total_tests".isNull).groupBy("location").agg(max("date").as("max_date"), min("date").as("min_date"))
display(total_tests_date_maxmin)

// COMMAND ----------

// MAGIC %md
// MAGIC process stringency_index, reproduction_rate, total_tests, total_tests_per_thousand

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

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


// COMMAND ----------

display(df_cleaned_time_series)

// COMMAND ----------

// MAGIC %scala
// MAGIC import org.apache.spark.sql.functions._
// MAGIC 
// MAGIC for (c <- df_cleaned_time_series.columns) {
// MAGIC   println(c + ": " + df_cleaned_time_series.filter(col(c).isNull).count())
// MAGIC }

// COMMAND ----------

