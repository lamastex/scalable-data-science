// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %python
// MAGIC pip install plotly

// COMMAND ----------

// MAGIC 
// MAGIC %run "./DataPreprocess"

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Show reproduction rate of selected countries i.e. Sweden, Germany, Danmark, Finland, Norway

// COMMAND ----------

display(df_cleaned_time_series.select($"reproduction_rate", $"location", $"date").filter($"reproduction_rate".isNotNull).filter(
                                                                                       $"location"==="Sweden" ||
                                                                                       $"location"==="Germany" ||
                                                                                       $"location"==="Danmark" ||
                                                                                       $"location"==="Finland" ||
                                                                                       $"location"==="Norway").sort("date"))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Visualize total cases, total deaths, new cases and new deaths during pandemic

// COMMAND ----------

df_cleaned_time_series.createOrReplaceTempView("visual_rdd")

// COMMAND ----------

// MAGIC %python
// MAGIC import pandas as pd
// MAGIC import numpy as np
// MAGIC import plotly.express as px

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC test_table = spark.table("visual_rdd")
// MAGIC 
// MAGIC country = np.array(test_table.select("iso_code").rdd.map(lambda l: l[0]).collect())
// MAGIC dates = np.array(test_table.select("date").rdd.map(lambda l: l[0]).collect())
// MAGIC total_cases = np.array(test_table.select("total_cases").rdd.map(lambda l: l[0]).collect())
// MAGIC total_deaths = np.array(test_table.select("total_deaths").rdd.map(lambda l: l[0]).collect())
// MAGIC new_cases = np.array(test_table.select("new_cases").rdd.map(lambda l: l[0]).collect())
// MAGIC new_deaths = np.array(test_table.select("new_deaths").rdd.map(lambda l: l[0]).collect())
// MAGIC 
// MAGIC visual_data = {'country':country.tolist(), 'total_cases':total_cases, 'date':dates, 
// MAGIC              'total_deaths': total_deaths, 'new_cases': new_cases, 'new_deaths': new_deaths}
// MAGIC visual_df = pd.DataFrame(data = visual_data).sort_values(by='date')
// MAGIC visual_df

// COMMAND ----------

// MAGIC %md
// MAGIC Total Cases

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC fig = px.choropleth(visual_df[~visual_df.country.str.contains("WLD", na=False)], locations="country",
// MAGIC                     color="total_cases", # total_cases is a column of gapminder
// MAGIC                     hover_name="country", # column to add to hover information
// MAGIC                     color_continuous_scale=px.colors.sequential.Plasma,
// MAGIC                     animation_frame = 'date')
// MAGIC fig.show()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Total Deaths

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC fig = px.choropleth(visual_df[~visual_df.country.str.contains("WLD", na=False)], locations="country",
// MAGIC                     color="total_deaths", # total_deaths is a column of gapminder
// MAGIC                     hover_name="country", # column to add to hover information
// MAGIC                     color_continuous_scale=px.colors.sequential.Plasma,
// MAGIC                     animation_frame = 'date')
// MAGIC fig.show()

// COMMAND ----------

// MAGIC %md
// MAGIC ### New Cases

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC fig = px.choropleth(visual_df[~visual_df.country.str.contains("WLD", na=False)], locations="country",
// MAGIC                     color="new_cases", # new_cases is a column of gapminder
// MAGIC                     hover_name="country", # column to add to hover information
// MAGIC                     color_continuous_scale=px.colors.sequential.Plasma,
// MAGIC                     animation_frame = 'date')
// MAGIC fig.show()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### New Deaths

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC fig = px.choropleth(visual_df[~visual_df.country.str.contains("WLD", na=False)], locations="country",
// MAGIC                     color="new_deaths", # new_deaths is a column of gapminder
// MAGIC                     hover_name="country", # column to add to hover information
// MAGIC                     color_continuous_scale=px.colors.sequential.Plasma,
// MAGIC                     animation_frame = 'date')
// MAGIC fig.show()

// COMMAND ----------

