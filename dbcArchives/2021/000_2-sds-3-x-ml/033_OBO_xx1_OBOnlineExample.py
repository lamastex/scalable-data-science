# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC # Old Bailey Online Analysis - for time-varying Bayesian Binomial Partition Models
# MAGIC 
# MAGIC Benny Avelin and Raazesh Sainudiin
# MAGIC 
# MAGIC ## Load Data

# COMMAND ----------

# MAGIC %md
# MAGIC Check that the csv file is there in dbfs, otherwise go to **Step 0** at the bottom of this notebook.

# COMMAND ----------

# MAGIC %fs
# MAGIC ls "dbfs:/tmp/obo.csv"

# COMMAND ----------

# MAGIC %fs
# MAGIC head "dbfs:/tmp/obo.csv"

# COMMAND ----------

oboDF = spark.read.format("csv").option("header", "true").load("dbfs:/tmp/obo.csv")

# COMMAND ----------

display(oboDF)

# COMMAND ----------

# MAGIC %md
# MAGIC # Step 0: Download CSV and Copy to dbfs

# COMMAND ----------

# MAGIC %sh
# MAGIC wget http://lamastex.org/datasets/public/OldBailey/oboOffencePunnishmentCountsFrom-sds-2-2-ApacheSparkScalaProcessingOfOBOXMLDoneByRaazOn20180405.csv

# COMMAND ----------

# MAGIC %sh
# MAGIC pwd && ls

# COMMAND ----------

# MAGIC %fs
# MAGIC cp "file:/databricks/driver/oboOffencePunnishmentCountsFrom-sds-2-2-ApacheSparkScalaProcessingOfOBOXMLDoneByRaazOn20180405.csv" "dbfs:/tmp/obo.csv"

# COMMAND ----------

# MAGIC %fs
# MAGIC ls "dbfs:/tmp/"

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Preparing the windowed version with ??
# MAGIC 
# MAGIC Scala is simpler here as done in next noteboo.
# MAGIC 
# MAGIC **TODO** Need to use the simpler method using dbfs FUSE that Ivan Sadikov showed earlier. However the results in the next notebook using `suckAs.sh` works and it's left for fun.

# COMMAND ----------

