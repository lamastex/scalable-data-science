// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Trends in Financial Stocks and News Events
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
// MAGIC According to [Merriam-Webster Dictionary](https://www.merriam-webster.com/dictionary/trend) the definitionof **trend** is as follows:
// MAGIC 
// MAGIC > a prevailing tendency or inclination : drift. How to use trend in a sentence. Synonym Discussion of trend.
// MAGIC 
// MAGIC Since people invest in financial stocks of publicly traded companies and make these decisions based on their understanding of current events reported in mass media, a natural question is:
// MAGIC 
// MAGIC *How can one try to represent and understand this interplay?*
// MAGIC 
// MAGIC The following material, first goes through the ETL process to ingest:
// MAGIC 
// MAGIC - financial data and then
// MAGIC - mass-media data
// MAGIC 
// MAGIC in a structured manner so that one can begin scalable data science processes upon them.
// MAGIC 
// MAGIC In the sequel two libraries are used to take advantage of SparkSQL and delta.io tables ("Spark on ACID"):
// MAGIC 
// MAGIC - for encoding and interpreting trends (so-called trend calculus) in any time-series, say financial stock prices, for instance.
// MAGIC - for structured representaiton of the worl'd largest open-sourced mass media data:
// MAGIC   - The GDELT Project: [https://www.gdeltproject.org/](https://www.gdeltproject.org/).
// MAGIC 
// MAGIC The last few notebooks show some simple data analytics to help extract and identify events that may be related to trends of interest.
// MAGIC 
// MAGIC We note that the sequel here is mainly focused on the data engineering science of ETL and basic ML Pipelines. We hope it will inspire others to do more sophisticated research, including scalable causal inference and various forms of distributed deep/reinforcement learning for more sophisticated decision problems.