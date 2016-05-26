# Databricks notebook source exported at Tue, 15 Mar 2016 02:23:18 UTC
# MAGIC %md
# MAGIC **SOURCE:** This is from the databricks Community Edition and has been added to this databricks shard at [Workspace -> scalable-data-science -> xtraResources -> dbCE -> MLlib -> unsupervised -> clustering -> k-means -> 1MSongsPy_ETLExploreModel](/#workspace/scalable-data-science/xtraResources/dbCE/MLlib/unsupervised/clustering/k-means/1MSongsPy_ETLExploreModel) as extra resources for the project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) that is prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand), and *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome). 

# COMMAND ----------

# MAGIC %md
# MAGIC **If you see ![](http://training.databricks.com/databricks_guide/ImportNotebookIcon3.png) at the top-left, click on the link to import this notebook in order to run it.** 

# COMMAND ----------

# MAGIC %md # Stage 2: Exploring songs data
# MAGIC 
# MAGIC ![Explore](http://training.databricks.com/databricks_guide/end-to-end-02.png)
# MAGIC 
# MAGIC 
# MAGIC This is the second notebook in this tutorial. In this notebook we do what any data scientist does with their data right after parsing it: exploring and understanding different aspect of data. Make sure you understand how we get the `songsTable` by reading and running the ETL notebook. In the ETL notebook we created and cached a temporary table named `songsTable`

# COMMAND ----------

# MAGIC %md ## A first inspection

# COMMAND ----------

# MAGIC %md A first step to any data exploration is viewing sample data. For this purpose we can use a simple SQL query that returns first 10 rows.

# COMMAND ----------

# MAGIC %sql select * from songsTable limit 10

# COMMAND ----------

# MAGIC %md We find 20 columns in our table. They are:

# COMMAND ----------

table("songsTable").printSchema()

# COMMAND ----------

# MAGIC %md Another important first step is know how much data we are dealing with

# COMMAND ----------

# MAGIC %sql select count(*) from songsTable

# COMMAND ----------

# MAGIC %md ## Summarizing and visualizing data

# COMMAND ----------

# MAGIC %md We have quite a few data points in our table. Obviously it is not feasable to visualize every data point. So we do the obvious: summarizing data and graphing the summary. An interesting question is how different parameters of songs change over time. For example, how did average song durations change over time.

# COMMAND ----------

from ggplot import *

# COMMAND ----------

# MAGIC %md Following query produces average of duration for each year. The result is a Spark `DataFrame`

# COMMAND ----------

baseQuery = sqlContext.sql("select avg(duration) as duration, year from songsTable group by year")

# COMMAND ----------

# MAGIC %md To plot the result, we will collect it to the driver. This can be done by calling `collect()` or `toPandas()`. We use `toPandas()` because it produces a Pandas data frame that can easily be consumed by the python ggplot package. We can pass the ggplot object to the `display()` function to render it in the notebook.
# MAGIC 
# MAGIC __NOTE__: `display()` is a versatile function in Databricks notebooks. You can pass different types of objects to it and it will render them nicely in the notebook. 

# COMMAND ----------

from ggplot import *
plot = ggplot(baseQuery.toPandas(), aes('year', 'duration')) + geom_point() + geom_line(color='blue')
display(plot)

# COMMAND ----------

# MAGIC %md The resulting graph is somewhat unusual. We do not expect to have a data point for year 0. But it is real data and there are always outliers (as a result of bad measurement or other sources of error). In this case we are going to ignore the outlier point by limiting our query to years greater than 0.

# COMMAND ----------

df_filtered = baseQuery.filter(baseQuery.year > 0).filter(baseQuery.year < 2010).toPandas()
plot = ggplot(df_filtered, aes('year', 'duration')) + geom_point() + geom_line(color='blue')
display(plot)

# COMMAND ----------

# MAGIC %md ## Exercises
# MAGIC 
# MAGIC 1. Why do you think average song durations increase dramatically in 70's?
# MAGIC 2. Add error bars with standard deviation around each average point in the plot.
# MAGIC 3. How did average loudness change over time?
# MAGIC 4. How did tempo change over time?
# MAGIC 5. What other aspects of songs can you explore with this technique?

# COMMAND ----------

# MAGIC %md ## Sampling and visualizing
# MAGIC 
# MAGIC Another technique for visually exploring large data, which we are going to try, is sampling data. First step is generating a sample.

# COMMAND ----------

sampled = sqlContext.sql("select year, duration from songsTable where year > 1930 and year < 2012")\
  .sample(withReplacement = False, fraction = 0.1).toPandas()

# COMMAND ----------

# MAGIC %md With sampled data we can produce a scatter plot as follows.

# COMMAND ----------

p = ggplot(sampled, aes(x = 'year', y = 'duration')) + ylim(0, 800) + \
  geom_smooth(size=3, span=0.3) + geom_point(aes(color = 'blue', size = 4))
display(p)

# COMMAND ----------

# MAGIC %md ## Exercises
# MAGIC 
# MAGIC 
# MAGIC 1. Add jitter to year value in the plot above.
# MAGIC 2. Plot sampled points for other parameters in the data.

# COMMAND ----------

# MAGIC %md Next step is clustering the data. Click on the next notebook (Model) to follow the tutorial.