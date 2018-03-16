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
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/Ib1BIbPDS6U/0.jpg)](https://www.youtube.com/embed/Ib1BIbPDS6U?start=0&end=2545&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/fxAlrRqwYRw/0.jpg)](https://www.youtube.com/embed/fxAlrRqwYRw?start=1488&end=2585&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Stage 3: Modeling Songs via k-means
// MAGIC 
// MAGIC ![Model](http://training.databricks.com/databricks_guide/end-to-end-03.png)
// MAGIC 
// MAGIC This is the third step into our project. In the first step we parsed raw text files and created a table. Then we explored different aspects of data and learned that things have been changing over time. In this step we attempt to gain deeper understanding of our data by categorizing (a.k.a. clustering) our data. For the sake of training we pick a fairly simple model based on only three parameters. We leave more sophisticated modeling as exercies to the reader

// COMMAND ----------

// MAGIC %md
// MAGIC We pick the most commonly used and simplest clustering algorithm (KMeans) for our job. The SparkML KMeans implementation expects input in a vector column. Fortunately, there are already utilities in SparkML that can help us convert existing columns in our table to a vector field. It is called `VectorAssembler`. Here we import that functionality and use it to create a new DataFrame

// COMMAND ----------

// Let's quickly do everything to register the tempView of the table here

// this is a case class for our row objects
case class Song(artist_id: String, artist_latitude: Double, artist_longitude: Double, artist_location: String, artist_name: String, duration: Double, end_of_fade_in: Double, key: Int, key_confidence: Double, loudness: Double, release: String, song_hotness: Double, song_id: String, start_of_fade_out: Double, tempo: Double, time_signature: Double, time_signature_confidence: Double, title: String, year: Double, partial_sequence: Int)

def parseLine(line: String): Song = {
  // this is robust parsing by try-catching type exceptions
  
  def toDouble(value: String, defaultVal: Double): Double = {
    try {
       value.toDouble
    } catch {
      case e: Exception => defaultVal
    }
  }

  def toInt(value: String, defaultVal: Int): Int = {
    try {
       value.toInt
      } catch {
      case e: Exception => defaultVal
    }
  }
  // splitting the sting of each line by the delimiter TAB character '\t'
  val tokens = line.split("\t")
  
  // making song objects
  Song(tokens(0), toDouble(tokens(1), 0.0), toDouble(tokens(2), 0.0), tokens(3), tokens(4), toDouble(tokens(5), 0.0), toDouble(tokens(6), 0.0), toInt(tokens(7), -1), toDouble(tokens(8), 0.0), toDouble(tokens(9), 0.0), tokens(10), toDouble(tokens(11), 0.0), tokens(12), toDouble(tokens(13), 0.0), toDouble(tokens(14), 0.0), toDouble(tokens(15), 0.0), toDouble(tokens(16), 0.0), tokens(17), toDouble(tokens(18), 0.0), toInt(tokens(19), -1))
}

// this is loads all the data - a subset of the 1M songs dataset
val dataRDD = sc.textFile("/databricks-datasets/songs/data-001/part-*") 

// .. fill in comment
val df = dataRDD.map(parseLine).toDF

// .. fill in comment
df.createOrReplaceTempView("songsTable")

// COMMAND ----------

import org.apache.spark.ml.feature.VectorAssembler

val trainingData = new VectorAssembler()
                      .setInputCols(Array("duration", "tempo", "loudness"))
                      .setOutputCol("features")
                      .transform(table("songsTable"))

// COMMAND ----------

// MAGIC %md
// MAGIC All we have done above with the `VectorAssembler` method is:
// MAGIC 
// MAGIC * created a DataFrame called `trainingData`
// MAGIC * that `transform`ed our `table` called `songsTable` 
// MAGIC * by adding an output column named `features` using `setOutputCol("features")`
// MAGIC * that was obtained from an `Array` of the `songsTable`'s columns named `duration`, `tempo` and `loudness` using
// MAGIC   * `setInputCols(Array("duration", "tempo", "loudness"))`.

// COMMAND ----------

trainingData.take(3) // see first 3 rows of trainingData DataFrame, notice the vectors in the last column

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Transformers
// MAGIC A Transformer is an abstraction that includes feature transformers and learned models. Technically, a Transformer implements a method transform(), which converts one DataFrame into another, generally by appending one or more columns. For example:
// MAGIC 
// MAGIC * A feature transformer might take a DataFrame, read a column (e.g., text), map it into a new column (e.g., feature vectors), and output a new DataFrame with the mapped column appended.
// MAGIC * A learning model might take a DataFrame, read the column containing feature vectors, predict the label for each feature vector, and output a new DataFrame with predicted labels appended as a column.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Estimators
// MAGIC An Estimator abstracts the concept of a learning algorithm or any algorithm that fits or trains on data. 
// MAGIC 
// MAGIC Technically, an Estimator implements a method `fit()`, which accepts a DataFrame and produces a Model, which is a Transformer. 
// MAGIC 
// MAGIC For example, a learning algorithm such as `LogisticRegression` is an Estimator, and calling `fit()` trains a `LogisticRegressionModel`, which is a Model and hence a Transformer.

// COMMAND ----------

display(trainingData.select("duration", "tempo", "loudness", "features").limit(5)) // see features in more detail

// COMMAND ----------

// MAGIC %md
// MAGIC #### [Demonstration of the standard algorithm](https://en.wikipedia.org/wiki/K-means_clustering#Initialization_methods)
// MAGIC 
// MAGIC (1) ![](https://upload.wikimedia.org/wikipedia/commons/5/5e/K_Means_Example_Step_1.svg)
// MAGIC (2) ![](https://upload.wikimedia.org/wikipedia/commons/a/a5/K_Means_Example_Step_2.svg)
// MAGIC (3) ![](https://upload.wikimedia.org/wikipedia/commons/3/3e/K_Means_Example_Step_3.svg)
// MAGIC (4) ![](https://upload.wikimedia.org/wikipedia/commons/d/d2/K_Means_Example_Step_4.svg)
// MAGIC 
// MAGIC 1. k initial "means" (in this case k=3) are randomly generated within the data domain (shown in color).
// MAGIC * k clusters are created by associating every observation with the nearest mean. The partitions here represent the [Voronoi diagram](https://en.wikipedia.org/wiki/Voronoi_diagram) generated by the means.
// MAGIC * The [centroid](https://en.wikipedia.org/wiki/Centroid) of each of the k clusters becomes the new mean.
// MAGIC * Steps 2 and 3 are repeated until **local** convergence has been reached.
// MAGIC 
// MAGIC The "assignment" step 2 is also referred to as expectation step, the "update step" 3 as maximization step, making this algorithm a variant of the *generalized* [expectation-maximization algorithm](https://en.wikipedia.org/wiki/Expectation-maximization_algorithm).
// MAGIC 
// MAGIC **Caveats: **
// MAGIC As k-means is a **heuristic algorithm**, there is **no guarantee that it will converge to the global optimum**, and the result may depend on the initial clusters. As the algorithm is usually very fast, it is common to run it multiple times with different starting conditions. However, in the worst case, k-means can be very slow to converge. For more details see [https://en.wikipedia.org/wiki/K-means_clustering](https://en.wikipedia.org/wiki/K-means_clustering) that is also embedded in-place below.

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://en.wikipedia.org/wiki/K-means_clustering#Standard_algorithm",500))

// COMMAND ----------

// MAGIC %md
// MAGIC ### CAUTION!
// MAGIC 
// MAGIC [Iris flower data set](https://en.wikipedia.org/wiki/Iris_flower_data_set), clustered using 
// MAGIC 
// MAGIC   * k-means (left) and 
// MAGIC   * true species in the data set (right). 
// MAGIC   
// MAGIC ![](https://upload.wikimedia.org/wikipedia/commons/1/10/Iris_Flowers_Clustering_kMeans.svg)
// MAGIC 
// MAGIC _k-means clustering result for the [Iris flower data set](https://en.wikipedia.org/wiki/Iris_flower_data_set) and actual species visualized using [ELKI](https://en.wikipedia.org/wiki/Environment_for_DeveLoping_KDD-Applications_Supported_by_Index-Structures). Cluster means are marked using larger, semi-transparent symbols_.
// MAGIC 
// MAGIC **Note** that k-means is non-determinicstic, so results vary. Cluster means are visualized using larger, semi-transparent markers. The visualization was generated using ELKI.
// MAGIC 
// MAGIC With some cautionary tales we go ahead with applying k-means to our dataset next.

// COMMAND ----------

// MAGIC %md
// MAGIC We can now pass this new DataFrame to the `KMeans` model and ask it to categorize different rows in our data to two different classes (`setK(2)`). We place the model in a immutable `val`ue named `model`.
// MAGIC 
// MAGIC **Note:** This command performs multiple spark jobs (one job per iteration in the KMeans algorithm). You will see the progress bar starting over and over again.

// COMMAND ----------

import org.apache.spark.ml.clustering.KMeans
val model = new KMeans().setK(2).fit(trainingData) // 37 seconds in academic shard

// COMMAND ----------

//model. // uncomment and place cursor next to . and hit Tab to see all methods on model

// COMMAND ----------

model.clusterCenters // get cluster centres

// COMMAND ----------

val modelTransformed = model.transform(trainingData) // to get predictions as last column

// COMMAND ----------

// MAGIC %md
// MAGIC Remember that ML Pipelines works with DataFrames. So, our trainingData and modelTransformed are both DataFrames

// COMMAND ----------

trainingData.printSchema

// COMMAND ----------

modelTransformed.printSchema

// COMMAND ----------

// MAGIC %md 
// MAGIC * The column `features` that we specified as output column to our `VectorAssembler` contains the features
// MAGIC * The new column `prediction` in modelTransformed contains the predicted output

// COMMAND ----------

val transformed = modelTransformed.select("duration", "tempo", "loudness", "prediction")

// COMMAND ----------

// MAGIC %md 
// MAGIC To comfortably visualize the data we produce a random sample. 
// MAGIC Remember the `display()` function? We can use it to produce a nicely rendered table of transformed DataFrame. 

// COMMAND ----------

// just sampling the fraction 0.005 of all the rows at random, 
// 'false' argument to sample is for sampling without replacement
display(transformed.sample(false, fraction = 0.005)) 

// COMMAND ----------

// MAGIC %md
// MAGIC To generate a scatter plot matrix, click on the plot button bellow the table and select `scatter`. That will transform your table to a scatter plot matrix. It automatically picks all numeric columns as values. To include predicted clusters, click on `Plot Options` and drag `prediction` to the list of Keys. You will get the following plot. On the diagonal panels you see the PDF of marginal distribution of each variable. Non-diagonal panels show a scatter plot between variables of the two variables of the row and column. For example the top right panel shows the scatter plot between duration and loudness. Each point is colored according to the cluster it is assigned to.

// COMMAND ----------

display(transformed.sample(false, fraction = 0.1)) // try fraction=1.0 as this dataset is small

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Euclidean_space",500))

// COMMAND ----------

// MAGIC %md 
// MAGIC Do you see the problem in our clusters based on the plot? 
// MAGIC 
// MAGIC As you can see there is very little "separation" (*in the sense of being separable into two point clouds, that represent our two identifed clusters, such that they have minimal overlay of these two features, i.e. tempo and loudness. NOTE that this sense of "pairwise separation" is a **2D projection of all three features in 3D** [Euclidean Space](https://en.wikipedia.org/wiki/Euclidean_space), i.e. loudness, tempo and duration, that depends directly on their two-dimensional visually sense-making projection of perhaps two important song features, as depicted in the corresponding 2D-scatter-plot of tempo versus loudness within the **2D scatter plot matrix** that is helping us to **partially visualize in the 2D-plane all of the three features in the three dimensional real-valued feature space** that was the input to our K-Means algorithm*) between loudness, and tempo and generated clusters. To see that, focus on the panels in the first and second columns of the scatter plot matrix. For varying values of loudness and tempo prediction does not change. Instead, duration of a song alone predicts what cluster it belongs to. Why is that?
// MAGIC 
// MAGIC To see the reason, let's take a look at the marginal distribution of duration in the next cell.
// MAGIC 
// MAGIC To produce this plot, we have picked histogram from the plots menu and in `Plot Options` we chose prediction as key and duration as value. The histogram plot shows significant skew in duration. Basically there are a few very long songs. These data points have large leverage on the mean function (what KMeans uses for clustering). 

// COMMAND ----------

display(transformed.sample(false, fraction = 1.0).select("duration", "prediction")) // plotting over all results

// COMMAND ----------

// MAGIC %md
// MAGIC There are known techniques for dealing with skewed features. A simple technique is applying a power transformation. We are going to use the simplest and most common power transformation: logarithm.
// MAGIC 
// MAGIC In following cell we repeat the clustering experiment with a transformed DataFrame that includes a new column called `log_duration`.

// COMMAND ----------

val df = table("songsTable").selectExpr("tempo", "loudness", "log(duration) as log_duration")
val trainingData2 = new VectorAssembler().
                  setInputCols(Array("log_duration", "tempo", "loudness")).
                  setOutputCol("features").
                  transform(df)
val model2 = new KMeans().setK(2).fit(trainingData2)
val transformed2 = model2.transform(trainingData2).select("log_duration", "tempo", "loudness", "prediction")
display(transformed2.sample(false, fraction = 0.1))

// COMMAND ----------

// MAGIC %md
// MAGIC The new clustering model makes much more sense. Songs with high tempo and loudness are put in one cluster and song duration does not affect song categories. 
// MAGIC 
// MAGIC To really understand how the points in 3D behave you need to see them in 3D interactively and understand the limits of its three 2D projections. For this let us spend some time and play in sageMath Worksheet in [CoCalc](https://cocalc.com/) (it is free for light-weight use and perhaps worth the 7 USD a month if you need more serious computing in mathmeatics, statistics, etc. in multiple languages!).
// MAGIC 
// MAGIC Let us take a look at this sageMath Worksheet published here:
// MAGIC 
// MAGIC * [https://cocalc.com/projects/ee9392a2-c83b-4eed-9468-767bb90fd12a/files/3DEuclideanSpace_1MSongsKMeansClustering.sagews](https://cocalc.com/projects/ee9392a2-c83b-4eed-9468-767bb90fd12a/files/3DEuclideanSpace_1MSongsKMeansClustering.sagews)
// MAGIC * and the accompanying datasets (downloaded from the `display`s in this notebook and uploaded to CoCalc as CSV files):
// MAGIC     * [https://cocalc.com/projects/ee9392a2-c83b-4eed-9468-767bb90fd12a/files/KMeansClusters10003DFeatures_loudness-tempologDuration_Of1MSongsKMeansfor_015_sds2-2.csv](https://cocalc.com/projects/ee9392a2-c83b-4eed-9468-767bb90fd12a/files/KMeansClusters10003DFeatures_loudness-tempologDuration_Of1MSongsKMeansfor_015_sds2-2.csv)
// MAGIC     * [https://cocalc.com/projects/ee9392a2-c83b-4eed-9468-767bb90fd12a/files/KMeansClusters10003DFeatures_loudness-tempoDuration_Of1MSongsKMeansfor_015_sds2-2.csv](https://cocalc.com/projects/ee9392a2-c83b-4eed-9468-767bb90fd12a/files/KMeansClusters10003DFeatures_loudness-tempoDuration_Of1MSongsKMeansfor_015_sds2-2.csv)
// MAGIC 
// MAGIC The point of the above little example is that you need to be able to tell a sensible story with your data science process and not just blindly apply a heuristic, but highly scalable, algorithm which depends on the notion of nearest neighborhoods defined by the metric (Euclidean distances in 3-dimensional real-valued spaces in this example) induced by the features you have engineered or have the power to re/re/...-engineer to increase the meaningfullness of the problem at hand.