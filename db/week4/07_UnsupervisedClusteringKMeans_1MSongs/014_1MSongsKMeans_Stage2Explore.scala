// Databricks notebook source exported at Fri, 18 Mar 2016 21:34:12 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC **SOURCE:** This is the scala version of the python notebook from the databricks Community Edition that has been added to this databricks shard at [Workspace -> scalable-data-science -> xtraResources -> dbCE -> MLlib -> unsupervised -> clustering -> k-means -> 1MSongsPy_ETLExploreModel](/#workspace/scalable-data-science/xtraResources/dbCE/MLlib/unsupervised/clustering/k-means/1MSongsPy_ETLExploreModel) as extra resources for this project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/).

// COMMAND ----------

// MAGIC %md # Stage 2: Exploring songs data
// MAGIC 
// MAGIC ![Explore](http://training.databricks.com/databricks_guide/end-to-end-02.png)
// MAGIC 
// MAGIC 
// MAGIC This is the second notebook in this tutorial. In this notebook we do what any data scientist does with their data right after parsing it: exploring and understanding different aspect of data. Make sure you understand how we get the `songsTable` by reading and running the ETL notebook. In the ETL notebook we created and cached a temporary table named `songsTable`

// COMMAND ----------

// MAGIC %md ## A first inspection

// COMMAND ----------

// MAGIC %md A first step to any data exploration is viewing sample data. For this purpose we can use a simple SQL query that returns first 10 rows.

// COMMAND ----------

// MAGIC %sql select * from songsTable limit 10

// COMMAND ----------

table("songsTable").printSchema()

// COMMAND ----------

// MAGIC %sql select count(*) from songsTable

// COMMAND ----------

table("songsTable").count() // or equivalently with DataFrame API - recall table("songsTable") is a DataFrame

// COMMAND ----------

display(sqlContext.sql("SELECT duration, year FROM songsTable")) // Aggregation is set to 'Average' in 'Plot Options'

// COMMAND ----------

// MAGIC %md ## Exercises
// MAGIC 
// MAGIC 1. Why do you think average song durations increase dramatically in 70's?
// MAGIC 2. Add error bars with standard deviation around each average point in the plot.
// MAGIC 3. How did average loudness change over time?
// MAGIC 4. How did tempo change over time?
// MAGIC 5. What other aspects of songs can you explore with this technique?

// COMMAND ----------

// MAGIC %md ## Sampling and visualizing
// MAGIC 
// MAGIC Another technique for visually exploring large data, which we are going to try, is sampling data. 
// MAGIC * First step is generating a sample.
// MAGIC * With sampled data we can produce a scatter plot as follows.

// COMMAND ----------

// MAGIC %python
// MAGIC # let's use ggplot from python
// MAGIC from ggplot import *
// MAGIC sampled = sqlContext.sql("select year, duration from songsTable where year > 1930 and year < 2012")\
// MAGIC   .sample(withReplacement = False, fraction = 0.1).toPandas()
// MAGIC   
// MAGIC p = ggplot(sampled, aes(x = 'year', y = 'duration')) + ylim(0, 800) + \
// MAGIC   geom_smooth(size=3, span=0.3) + geom_point(aes(color = 'blue', size = 4))
// MAGIC display(p)

// COMMAND ----------

// MAGIC %md ## Exercises
// MAGIC 
// MAGIC 
// MAGIC 1. Add jitter to year value in the plot above.
// MAGIC 2. Plot sampled points for other parameters in the data.

// COMMAND ----------

// MAGIC %md Next step is clustering the data. Click on the next notebook (Model) to follow the tutorial.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)