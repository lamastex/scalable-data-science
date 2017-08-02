// Databricks notebook source
sc.parallelize(1 to 100).map(_*2).collect()

// COMMAND ----------

