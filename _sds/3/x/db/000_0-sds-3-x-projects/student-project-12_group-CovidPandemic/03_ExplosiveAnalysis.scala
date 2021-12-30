// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### This notebook is for explosive analysis of features in data. 

// COMMAND ----------

// MAGIC %run "./DataPreprocess"

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Statistics of invariant features

// COMMAND ----------

display(valid_distinct_features.describe())

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"population"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"population_density"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"median_age"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"aged_65_older"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"aged_70_older"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"gdp_per_capita"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"cardiovasc_death_rate"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"diabetes_prevalence"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"female_smokers"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"male_smokers"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"hospital_beds_per_thousand"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"life_expectancy"))

// COMMAND ----------

display(valid_distinct_features.select($"iso_code", $"human_development_index"))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Correlation between invariant features
// MAGIC 
// MAGIC There are some pairs of features are highly correlated i.e.
// MAGIC 1. median_age, aged_65_older
// MAGIC 2. median_age, human_development_index
// MAGIC 3. median_age, life_expectancy
// MAGIC 4. gdp_per_capita, human_development_index
// MAGIC 5. gdp_per_capita, life_expectancy
// MAGIC 6. human_development_index, life_expectancy

// COMMAND ----------

display(valid_distinct_features.drop("iso_code","location"))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Correlation between new case per million, total case, new death per million, total death per million, reproduction rate and stringency index. 

// COMMAND ----------

display(df_cleaned_time_series.drop("iso_code", "continent", "location",
                                    "date", "icu_patients", "icu_patients_per_million",
                                    "hosp_patients", "hosp_patients_per_million", "weekly_icu_admissions",
                                    "weekly_icu_admissions_per_million", "weekly_hosp_admissions", "weekly_hosp_admissions_per_million",
                                    "total_tests", "new_tests", "total_tests_per_thousand",
                                    "new_tests_per_thousand", "new_tests_smoothed", "new_tests_smoothed_per_thousand",
                                    "positive_rate", "tests_per_case", "tests_units",
                                    "extreme_poverty", "handwashing_facilities"))



// COMMAND ----------

