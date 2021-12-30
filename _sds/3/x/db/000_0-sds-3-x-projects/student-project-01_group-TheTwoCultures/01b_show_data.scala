// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %run /scalable-data-science/000_0-sds-3-x-projects/student-project-01_group-TheTwoCultures/01_load_data

// COMMAND ----------

var file_name = "dbfs:/datasets/student-project-01/familjeliv/familjeliv-allmanna-ekonomi.xml"
var xml_df = read_xml(file_name).cache()
var df = get_dataset(file_name).cache()

// COMMAND ----------

xml_df.printSchema()

// COMMAND ----------

xml_df.show(10)

// COMMAND ----------

df.printSchema()

// COMMAND ----------

display(df)