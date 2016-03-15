# Databricks notebook source exported at Tue, 15 Mar 2016 02:25:34 UTC
# MAGIC %md
# MAGIC **SOURCE:** This is from the databricks Community Edition and has been added to this databricks shard at [Workspace -> scalable-data-science -> xtraResources -> dbCE -> MLlib -> unsupervised -> clustering -> k-means -> 1MSongsPy_ETLExploreModel](/#workspace/scalable-data-science/xtraResources/dbCE/MLlib/unsupervised/clustering/k-means/1MSongsPy_ETLExploreModel) as extra resources for the project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) that is prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand), and *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome).

# COMMAND ----------

# MAGIC %md
# MAGIC **If you see ![](http://training.databricks.com/databricks_guide/ImportNotebookIcon3.png) at the top-left, click on the link to import this notebook in order to run it.** 

# COMMAND ----------

# MAGIC %md # Stage 3: Modeling (clustering) Songs
# MAGIC 
# MAGIC ![Model](http://training.databricks.com/databricks_guide/end-to-end-03.png)
# MAGIC 
# MAGIC This is the third step into our project. In the first step we parsed raw text files and created a table. Then we explored different aspects of data and learned that things have been changing over time. In this step we attempt to gain deeper understanding of our data by categorizing (a.k.a. clustering) our data. For the sake of training we pick a fairly simple model based on only three parameters. We leave more sophisticated modeling as exercies to the reader

# COMMAND ----------

from pyspark.ml.clustering import KMeans
from pyspark.ml.feature import VectorAssembler

# COMMAND ----------

# MAGIC %md We pick the most commonly used and simplest clustering algorithm (KMeans) for our job. The SparkML KMeans implementation expects input in a a vector column. Fortunately, there are already utilities in SparkML that can help us convert existing columns in our table to a vector field. It is called `VectorAssembler`. Here we import that functionality and use it to create a new DataFrame

# COMMAND ----------

from pyspark.ml.feature import VectorAssembler
trainingData = VectorAssembler(inputCols = ["duration", "tempo", "loudness"], outputCol = "features").transform(table("songsTable"))

# COMMAND ----------

# MAGIC %md We can now pass this new DataFrame to the `KMeans` model and ask it to categorize different rows in our data to two different classes (`setK(2)`). We place the model in a variable named `model`.
# MAGIC 
# MAGIC **Note:** This command multiple spark jobs (one job per iteration in the KMeans algorithm). You will see the progress bar starting over and over again.

# COMMAND ----------

from pyspark.ml.clustering import KMeans
model = KMeans().setK(2).fit(trainingData)

# COMMAND ----------

# MAGIC %md To see the result of our clustering, we produce a scatter plot matrix that shows interaction between input variables and learned clusters. To get that we apply the model on the original data and pick four columns: `prediction` and the original features (`duration`, `tempo`, and `loudness`).

# COMMAND ----------

transformed = model.transform(trainingData).select("duration", "tempo", "loudness", "prediction")

# COMMAND ----------

# MAGIC %md To comfortably visualize the data we produce a random sample. 
# MAGIC Remember the `display()` function? We can use it to produce a nicely rendered table of transformed DataFrame. 

# COMMAND ----------

display(transformed.sample(False, fraction = 0.005))

# COMMAND ----------

# MAGIC %md To generate a scatter plot matrix, click on the plot button bellow the table and select `scatter`. That will transform your table to a scatter plot matrix. It automatically picks all numeric columns as values. To include predicted clusters, click on `Plot Options` and drag `prediction` to the list of Keys. You will get the following plot. On the diagonal panels you see the PDF of marginal distribution of each variable. Non-diagonal panels show a scatter plot between variables of the two variables of the row and column. For example the top right panel shows the scatter plot between duration and loudness. Each point is colored according to the cluster it is assigned to.

# COMMAND ----------

display(transformed.sample(False, fraction = 0.1))

# COMMAND ----------

# MAGIC %md Do you see the problem in our clusters based on the plot? 
# MAGIC 
# MAGIC As you can see there is very little correlation between loudness, and tempo and generated clusters. To see that, focus on the panels in the first and second columns of the scatter plot matrix. For varying values of loudness and tempo prediction does not change. Instead, duration of a song alone predicts what cluster it belongs to. Why is that?
# MAGIC 
# MAGIC To see the reason, let's take a look at the marginal distribution of duration in the next cell.
# MAGIC 
# MAGIC To produce this plot, we have picked histogram from the plots menu and in `Plot Options` we chose prediction as key and duration as value. The histogram plot shows significant skew in duration. Basically there are a few very long songs. These data points have large leverage on the mean function (what KMeans uses for clustering). 

# COMMAND ----------

display(transformed.sample(False, fraction = 0.1).select("duration", "prediction"))

# COMMAND ----------

# MAGIC %md There are known techniques for dealing with skewed features. A simple technique is applying a power transformation. We are going to use the simplest and most common power transformation: logarithm.
# MAGIC 
# MAGIC In following cell we repeat the clustering experiment with a transformed DataFrame that includes a new column called `log_duration`.

# COMMAND ----------

df = table("songsTable").selectExpr("tempo", "loudness","log(duration) as log_duration")
trainingData2 = VectorAssembler(inputCols = ["log_duration", "tempo", "loudness"], outputCol = "features").transform(df)
model2 = KMeans().setK(2).fit(trainingData2)
transformed2 = model2.transform(trainingData2).select("log_duration", "tempo", "loudness", "prediction")
display(transformed2.sample(False, fraction = 0.1))

# COMMAND ----------

# MAGIC %md The new clustering model makes much more sense. Songs with high tempo and loudness are put in one cluster and song duration does not affect song categories. 