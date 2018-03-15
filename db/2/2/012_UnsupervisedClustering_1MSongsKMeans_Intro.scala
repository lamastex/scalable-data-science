// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
// MAGIC 
// MAGIC ## Million Song Dataset - Kaggle Challenge
// MAGIC 
// MAGIC ### Predict which songs a user will listen to.
// MAGIC 
// MAGIC **SOURCE:** This is just a *Scala*-rification of the *Python* notebook published in databricks community edition in 2016.

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/3x5iQoXm3cc/0.jpg)](https://www.youtube.com/embed/3x5iQoXm3cc?start=1219&end=1395&autoplay=1)

// COMMAND ----------

// MAGIC %md 
// MAGIC When you first hear a song, do you ever categorize it as slow or fast in your head? Is it even a valid categorization? If so, can one do it automatically? I have always wondered about that. That is why I got excited when I learned about the [Million Songs Dataset  -Kaggle Challenge](https://www.kaggle.com/c/msdchallenge#description). 
// MAGIC 
// MAGIC In this tutorial we will walk through a practical example of a data science project with Apache Spark in Python. We are going to parse, explore and model a sample from the million songs dataset stored on distributed storage. This tutorial is organized into three sections:
// MAGIC 
// MAGIC 1. ETL: Parses raw texts and creates a cached table
// MAGIC 2. Explore: Explores different aspects of the songs table using graphs
// MAGIC 3. Model: Uses SparkML to cluster songs based on some of their attributes 
// MAGIC 
// MAGIC ![End to End Data Science](http://training.databricks.com/databricks_guide/end-to-end.png)
// MAGIC 
// MAGIC 
// MAGIC The goal of this tutorial is to prepare you for real world data science projects. Make sure you go through the tutorial in the above order and use the exercises to make yourself familiar further with the API. Also make sure you run these notebooks on a **1.6.x** cluster.

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. ETL
// MAGIC 
// MAGIC The first step of most data science projects is extracting, transforming and loading data into well formated tables. Our example starts with ETL as well. By following the ETL noteboook you can expect to learn about following Spark concepts:
// MAGIC 
// MAGIC * RDD: Resilient Distributed Dataset
// MAGIC * Reading and transforming RDDs
// MAGIC * Schema in Spark
// MAGIC * Spark DataFrame
// MAGIC * Temp tables
// MAGIC * Caching tables

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Explore
// MAGIC Exploratory analysis is a key step in any real data project. Data scientists use variety of tools to explore and visualize their data. In the second notebook of this tutorial we introduce several tools in Python and Databricks notebooks that can help you visually explore your large data. By reading this notebook and finishing its exercises you will become familiar with:
// MAGIC 
// MAGIC * How to view the schema of a table
// MAGIC * How to display ggplot and matplotlib figures in Notebooks
// MAGIC * How to summarize and visualize different aspects of large datasets
// MAGIC * How to sample and visualize large data

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. Model
// MAGIC The end goal of many data scientists is producing useful models. These models are often used for prediction of new and upcoming events in production. In our third notebook we construct a simple K-means clustering model. In this notebook you will learn about:
// MAGIC 
// MAGIC * Feature transformation
// MAGIC * Fitting a model using SparkML API
// MAGIC * Applying a model to data
// MAGIC * Visualizing model results
// MAGIC * Model tuning