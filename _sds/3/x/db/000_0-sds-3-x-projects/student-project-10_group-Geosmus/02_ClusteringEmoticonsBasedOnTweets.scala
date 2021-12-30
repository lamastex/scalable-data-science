// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md # Clustering emoticons based on tweets
// MAGIC In this notebook we will look at the symbols in the Unicode block *Emoticons*, which contains 80 commonly used emojis. The goal is to find out which emoticons are related to each other and hopefully finding clusters that correspond vaguely to some sentiment of an emoticon. We will do this in a fairly naïve way since our focus was on learning streaming, spark and scala. First let's have a look at the emojis in question, they are presented in the table from Wikipedia below.

// COMMAND ----------

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
displayHTML(frameIt("https://en.wikipedia.org/wiki/Emoticons_(Unicode_block)#Descriptions", 350))

// COMMAND ----------

// MAGIC %md In the following two cells we create a list of these emoticons and load the previously collected dataset of tweets.

// COMMAND ----------

val emoticonsList = List(
  "😀", "😁", "😂",	"😃", "😄",	"😅", "😆",	"😇", "😈",	"😉", "😊",	"😋", "😌", "😍", "😎", "😏",
  "😐",	"😑", "😒",	"😓", "😔", "😕", "😖", "😗", "😘", "😙", "😚", "😛", "😜", "😝", "😞", "😟",
  "😠",	"😡", "😢", "😣", "😤", "😥", "😦", "😧", "😨", "😩", "😪", "😫", "😬", "😭", "😮", "😯",
  "😰",	"😱", "😲", "😳", "😴", "😵", "😶", "😷", "😸", "😹", "😺", "😻", "😼", "😽", "😾", "😿",
  "🙀",	"🙁", "🙂", "🙃", "🙄", "🙅", "🙆", "🙇", "🙈", "🙉", "🙊", "🙋", "🙌", "🙍", "🙎", "🙏"
)

val emoticonsMap = emoticonsList.zipWithIndex.toMap
val nbrEmoticons = emoticonsList.length

// COMMAND ----------

val fullDF = sqlContext.read.parquet("/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/{2020,2021,continuous_22_12}/*/*/*/*/*")
println(fullDF.count)

// COMMAND ----------

// MAGIC %md
// MAGIC ### How to cluster emoticons
// MAGIC We could just look at the descriptions and appearances of the various emoticons and cluster them into broad categories based on that. However, instead we will try to use our collected tweet dataset to create a clustering. Then we will use the intuition based on the descriptions and appearances to judge how successful this approach was.
// MAGIC 
// MAGIC We will use the Jaccard distance between emoticons to try to cluster them. The Jaccard distance between emoticons \\(e_1\\) and \\(e_2\\) is given by
// MAGIC 
// MAGIC \\[
// MAGIC d(e_1, e_2) = 1 - \frac{\\# (e_1\wedge e_2)}{\\# (e_1) + \\# (e_2) - \\# (e_1\wedge e_2)},
// MAGIC \\]
// MAGIC 
// MAGIC where \\(\\#(e)\\) is the number of tweets collected containing the emoticon \\(e\\), and \\(\\# (e_1\wedge e_2)\\) is the number of tweets collected containing both \\(e_1\\) and \\(e_2\\).

// COMMAND ----------

// MAGIC %md In order to find the Jaccard distances between emoticons, we must create a matrix containing for each pair of emoticons how often they appear together in the dataset of tweets and also in how many tweets each emoticon appears individually. First we define a function to create such a matrix for an individual tweet. Then we will sum these matrices for all the tweets. The matrices will be represented by a 1D array following a certain indexing scheme and containing the entries of the upper triangular part of the matrix (there would be redundancy in finding the whole matrix since it will be symmetric).

// COMMAND ----------

def emoticonPairToIndex (a : Int, b : Int) : Int = { // helper function for indexing
  val i = if (a < b) a else b // makes sure j >= i
  val j = if (a < b) b else a
  return i*nbrEmoticons - (i * (i+1))/2 + j 
}

def createEmoticonIterator (s : String) : scala.util.matching.Regex.MatchIterator = { // helper function for iterating through the emoticons in a string s
  return s"""[${emoticonsList.mkString}]""".r.findAllIn(s)
}

def createEmoticonMatrix (s : String) : Array[Int] = { // The pair (i, j) will be at location i*(nbrEmoticons - (i+1)/2) + j in this array (this is compatible with later scipy functions)
  var m = Array.fill((nbrEmoticons*nbrEmoticons + nbrEmoticons)/2)(0) // there are 80 emoticons and thus 80^2 / 2 + 80 / 2 emoticon pairs including pairs of the same emoticon
  val emoticonIterator = createEmoticonIterator(s)
  // sets m to 1 for each index corresponding to a pair of emoticons present in the string s (very hacky code...)
  emoticonIterator.zipWithIndex.foreach(em_pair => // iterate over emoticons in s
                                        (createEmoticonIterator(s).drop(em_pair._2)).foreach( // iterate over remaining emoticons in s
                                          second_em => 
                                            (m(emoticonPairToIndex(
                                                emoticonsMap(em_pair._1),
                                                emoticonsMap(second_em))
                                              )
                                            = 1) // set m to 1 for each emoticon pair found in s
                                         )
                                       )
  return m
}

// COMMAND ----------

// MAGIC %md
// MAGIC In the cell below we sum all the "occurence-matrices" and print the diagonal of the summed matrix, i.e. the number of tweets containing each individual emoticon. It is clear that some emoticons are used far more often than others.

// COMMAND ----------

val emoticonsMatrix = fullDF.select("CurrentTweet")
                            .filter($"CurrentTweet".rlike(emoticonsList.mkString("|"))) // filters tweets with emoticons
                            .map(row => createEmoticonMatrix(row.mkString)) // creates an "adjacency matrix" for each tweet
                            .reduce((_, _).zipped.map(_ + _)) // sums the matrices elementwise

emoticonsList.zipWithIndex.foreach({case (e, i) => println(e + ", " + Integer.toString(emoticonsMatrix(emoticonPairToIndex(i, i))) + " occurences")})

// COMMAND ----------

// MAGIC %md
// MAGIC In the following two cells we create the Jaccard distance matrix which we want to use to cluster the emoticons.

// COMMAND ----------

def jaccardDistance (e1 : Int, e2 : Int) : Double = { // specify the emojis in terms of their indices in the list
  return 1.0 - 1.0 * emoticonsMatrix(emoticonPairToIndex(e1, e2)) / 
    (emoticonsMatrix(emoticonPairToIndex(e1, e1)) +  emoticonsMatrix(emoticonPairToIndex(e2, e2)) - emoticonsMatrix(emoticonPairToIndex(e1, e2)))
}

// COMMAND ----------

var jaccardMatrix = Array.fill(emoticonsMatrix.length)(1.0)
(0 until nbrEmoticons).foreach(i => (i until nbrEmoticons).foreach(j => (jaccardMatrix(emoticonPairToIndex(i, j)) = jaccardDistance(i, j))))

// COMMAND ----------

// MAGIC %md
// MAGIC Finally we write the Jaccard distance matrix and the emoticon list to file so that we don't have to keep rerunning the above cells and so that we can load them into python cells next.

// COMMAND ----------

//scala.tools.nsc.io.File("/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/emoticonsList.txt").writeAll(emoticonsList.mkString("\n"))
//scala.tools.nsc.io.File("/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/jMatrix.txt").writeAll(jaccardMatrix.mkString("\n"))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Clustering using python
// MAGIC We now switch to python cells in order to use various clustering methods implemented in SciPy and scikit-learn. First we install and import some packages for later use, then we load the previously saved Jaccard matrix and emoticons list.

// COMMAND ----------

// MAGIC %python 
// MAGIC %pip install pycountry

// COMMAND ----------

// MAGIC %python
// MAGIC import json
// MAGIC import os
// MAGIC from matplotlib import font_manager as fm, pyplot as plt, rcParams
// MAGIC import numpy as np
// MAGIC import pandas as pd
// MAGIC import pycountry
// MAGIC from scipy.cluster.hierarchy import dendrogram, linkage
// MAGIC from sklearn.manifold import locally_linear_embedding, TSNE
// MAGIC from sklearn.neighbors import NearestNeighbors

// COMMAND ----------

// MAGIC %python
// MAGIC jMatrix = np.loadtxt("/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/jMatrix.txt")
// MAGIC 
// MAGIC emoticonsList = []
// MAGIC 
// MAGIC with open("/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/emoticonsList.txt", 'r') as filehandle:
// MAGIC     for line in filehandle:
// MAGIC         e = line.strip() #remove line break
// MAGIC         emoticonsList.append(e)
// MAGIC 
// MAGIC nbrEmoticons = len(emoticonsList)
// MAGIC print(emoticonsList)

// COMMAND ----------

// MAGIC %md
// MAGIC Some of the SciPy clustering implementations require a full distance matrix, rather than the condensed representation consisting of only the upper triangular part which we have been using thus far. So we create a full matrix in the cell below. In the cell after that we define a helper function for plotting 2D embeddings of emoticons, note that this function loads the unifont-upper font for emoticon rendering, which can be downloaded from `http://unifoundry.com/unifont/index.html`.

// COMMAND ----------

// MAGIC %python
// MAGIC def emoticonPairToIndex(a, b): # same helper function as already defined in scala previously
// MAGIC   i = min(a, b) # makes sure j >= i
// MAGIC   j = max(a, b)
// MAGIC   return i * nbrEmoticons - (i * (i+1))//2 + j 
// MAGIC 
// MAGIC fullDistanceMatrix = np.zeros([nbrEmoticons, nbrEmoticons])
// MAGIC for r in range(nbrEmoticons):
// MAGIC   for c in range(nbrEmoticons):
// MAGIC     fullDistanceMatrix[r, c] = jMatrix[emoticonPairToIndex(r, c)]

// COMMAND ----------

// MAGIC %python
// MAGIC def scatterEmojis(emoticonsEmbedded):
// MAGIC   # This function plots a scatter plot of emoticons.
// MAGIC   # emoticonsEmbedded should be an 80x2 array 
// MAGIC   # containing 2D coordinates for each of the
// MAGIC   # 80 emoticons in the unicode emoticon block (in the correct order).
// MAGIC   
// MAGIC   # standardize the embedding for nicer plotting:
// MAGIC   emoticonsEmbedded = emoticonsEmbedded - np.mean(emoticonsEmbedded)
// MAGIC   emoticonsEmbedded = emoticonsEmbedded/np.std(emoticonsEmbedded) 
// MAGIC 
// MAGIC   # for proper emoji rendering change the font
// MAGIC   fpath = os.path.join(rcParams["datapath"], "/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/unifont_upper-13.0.05.ttf")
// MAGIC   prop = fm.FontProperties(fname=fpath, size=50)
// MAGIC 
// MAGIC   fig = plt.figure(figsize=(14, 14))
// MAGIC   for i, label in enumerate(emoticonsList):
// MAGIC       plt.text(emoticonsEmbedded[i, 0], emoticonsEmbedded[i, 1], label, fontproperties=prop)
// MAGIC   plt.setp(plt.gca(), frame_on=False, xticks=(), yticks=())

// COMMAND ----------

// MAGIC %md
// MAGIC ### Locally linear embedding
// MAGIC First off we will look at embedding the emoticons into 2D in ways that respect the Jaccard distances at least to a degree.
// MAGIC 
// MAGIC Locally linear embedding (LLE) is one such method, which is focused on good presentation of local neighborhoods. You can read more about it in the scikit-learn documentation embedded below.

// COMMAND ----------

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
displayHTML(frameIt("https://scikit-learn.org/stable/modules/manifold.html#locally-linear-embedding", 350))

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC nbrNeighbors = 8
// MAGIC emoticonsNeighbors = NearestNeighbors(n_neighbors=nbrNeighbors, metric="precomputed").fit(fullDistanceMatrix)
// MAGIC emoticonsEmbedded, err = locally_linear_embedding(emoticonsNeighbors, n_neighbors=nbrNeighbors, n_components=2)

// COMMAND ----------

// MAGIC %md
// MAGIC As we can see in the scatter plot below, LLE succeeds in separating the emoticons broadly into happy (down to the left), sad (up) and animal (down to the right) categories. Also some local clusters can be spotted, such as the three emoticons sticking out their tongues, close to the lower left corner.

// COMMAND ----------

// MAGIC %python
// MAGIC scatterEmojis(emoticonsEmbedded)

// COMMAND ----------

// MAGIC %md
// MAGIC ### t-distributed Stochastic Neighbor Embedding (t-SNE)
// MAGIC 
// MAGIC Another approach for embedding the distances into 2D is t-SNE. You can read more about this method in the sk-learn documentation below.

// COMMAND ----------

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
displayHTML(frameIt("https://scikit-learn.org/stable/modules/manifold.html#t-sne", 350))

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC emoticonsEmbedded = TSNE(n_components=2, perplexity=20.0, early_exaggeration=12.0, learning_rate=2.0, n_iter=10000,
// MAGIC                          metric='precomputed', angle=0.01).fit_transform(fullDistanceMatrix)

// COMMAND ----------

// MAGIC %md
// MAGIC t-SNE also does a good job at showing a separation between happy and sad emojis but the result is not as convincing as the LLE case. One could spend more time on optimizing the hyperparameters and probably find a better embedding here.

// COMMAND ----------

// MAGIC %python
// MAGIC scatterEmojis(emoticonsEmbedded)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Hierarchical clustering
// MAGIC Instead of trying to embed the distances into 2D, we can also create a nice graphical representation in the form of a dendrogram or hierarchical clustering. For this we need to process the distance matrix somewhat again in the following cell.

// COMMAND ----------

// MAGIC %python
// MAGIC # remove diagonal from jMatrix, as this is expected by the scipy linkage function:
// MAGIC diagonalIndices = [emoticonPairToIndex(i, i) for i in range(nbrEmoticons)]
// MAGIC jMatrixUpper = jMatrix[[i for i in range((nbrEmoticons**2 + nbrEmoticons)//2) if not i in diagonalIndices]]
// MAGIC assert len(jMatrixUpper) == len(jMatrix) - nbrEmoticons, "the upper matrix should have exactly 80 elements fewer than the upper+diagonal"
// MAGIC 
// MAGIC # creating a linkage matrix
// MAGIC Z = linkage(jMatrixUpper, 'complete', optimal_ordering=True)

// COMMAND ----------

// MAGIC %md
// MAGIC Hierarchical clustering works by starting of with clusters of size one which are just the emoticons and then iteratively joining those clusters which are closest together. The distance between clusters can be defined in various ways, here we somewhat arbitrarily choose so called "complete linkage" which means that the distance between clusters \\(a\\) and \\(b\\) is given by the maximum Jaccard distance between some emoticon in \\(a\\) and some emoticon in \\(b\\).
// MAGIC 
// MAGIC We can use dendrograms to neatly represent hirearchical clusterings graphically. The closer two emoticons (or rather emoticon clusters) are to each other, the further down in the dendrogram their branches merge.
// MAGIC 
// MAGIC The interested WASP PhD student could consider taking the WASP Topological Data Analysis course to learn more about hierarchical clustering.

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC # plotting a dendrogram
// MAGIC fig = plt.figure(figsize=(40, 8))
// MAGIC dn = dendrogram(Z, labels=emoticonsList, leaf_rotation=0, color_threshold=1.)
// MAGIC ax = plt.gca()
// MAGIC 
// MAGIC # for proper emoji rendering change the font
// MAGIC fpath = os.path.join(rcParams["datapath"], "/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/unifont_upper-13.0.05.ttf")
// MAGIC prop = fm.FontProperties(fname=fpath, size=28)
// MAGIC x_labels = ax.get_xmajorticklabels()
// MAGIC for x in x_labels:
// MAGIC     x.set_fontproperties(prop)
// MAGIC 
// MAGIC ax.set_ylim([.85, 1.01])

// COMMAND ----------

// MAGIC %md
// MAGIC We identify six main clusters in the dendrogram above. From left to right:
// MAGIC 
// MAGIC * The green "prayer" cluster (🙌🙏😊🙇🙋😷) which also contains the mask emoji and a common smile emoji,
// MAGIC * the teal "happy" cluster (😝😛😜😎😉😏😈😌😋😍😘😇😀😃😄😁😆😅🙂🙃),
// MAGIC * the magenta "cat" cluster (😹😸😽😺😻😿😾🙀😼),
// MAGIC * the yellow "shocked and kisses" or "SK" cluster (😶😬😲😮😯😗😙😚),
// MAGIC * a combined "not happy" cluster consisting of the next black, green, red, teal and magenta clusters (😵😧😦😨😰😱😳😂😭😩😔😢😞😥😓😪😴😫😖😣😟🙁😕😐😑😒🙄😤😡😠),
// MAGIC * finally the yellow "monkey" cluster (🙈🙊🙉).
// MAGIC 
// MAGIC We proceed with these clusters as they appeal sufficiently to our intuition to seem worthwhile.
// MAGIC The observant reader will however have noted some curiosities such as the fact that the "not happy" cluster contains the crying laughing emoji 😂 which is the most popular emoticon in our tweet dataset and which might be used in both happy and not so happy contexts.
// MAGIC 
// MAGIC Next, we finish the clustering part of this notebook by saving the clusters to file.

// COMMAND ----------

// MAGIC %python
// MAGIC monkeyEmoticons = dn["leaves"][76:79]
// MAGIC prayerEmoticons = dn["leaves"][0:6]
// MAGIC shockedAndKissesEmoticons = dn["leaves"][38:46]
// MAGIC happyEmoticons = dn["leaves"][9:29]
// MAGIC notHappyEmoticons = dn["leaves"][46:76]
// MAGIC catEmoticons = dn["leaves"][29:38]
// MAGIC emoticonsDict = {"monkey" : monkeyEmoticons,
// MAGIC                  "prayer" : prayerEmoticons,
// MAGIC                  "SK" : shockedAndKissesEmoticons,
// MAGIC                 "happy" : happyEmoticons,
// MAGIC                 "notHappy" : notHappyEmoticons,
// MAGIC                 "cat" : catEmoticons}
// MAGIC print(emoticonsDict)

// COMMAND ----------

// MAGIC %python
// MAGIC #with open('/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/emoticonClusters.json', 'w+') as f:
// MAGIC #    json.dump(emoticonsDict, f)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Filtering the tweets by cluster
// MAGIC We return to scala cells to filter the original dataset by what emoticons are present in each tweet. First we load the clusters from the just created json-file.

// COMMAND ----------

import org.json4s.jackson.JsonMethods.parse
val jsonString = scala.io.Source.fromFile("/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/emoticonClusters.json").mkString 
val emoticonClusters = parse(jsonString).values.asInstanceOf[Map[String, List[BigInt]]]
emoticonClusters.foreach({case (key, list) => println(key + ": " + list.map(i => emoticonsList(i.toInt)).mkString)})

// COMMAND ----------

// MAGIC %md
// MAGIC Next, we create a dataframe `emoticonDF` with a row for each tweet containing at least one emoticon. We add a column for each cluster indicating if the cluster is represented by some emoticon in the tweet. This dataframe is saved to file to be used in the next notebook 03 which focuses more on data visualization. Here we will finish this notebook by using the databricks `display` function to plot geopraphic information.

// COMMAND ----------

val emoticonDF = fullDF.filter($"CurrentTweet".rlike(emoticonsList.mkString("|"))) // filter tweets with emoticons
                       .select(($"countryCode" :: // select the countryCode column
                                $"CurrentTweetDate" :: // and the timestamp
                                (for {(name, cluster) <- emoticonClusters.toList} yield  // also create a new column for each emoticon cluster indicating if the tweet contains an emoticon of that cluster
                                 $"CurrentTweet".rlike(cluster.map(i => emoticonsList(i.toInt)).mkString("|"))
                                                .alias(name))) // rename new column
                                                : _*) // expand list
      
emoticonDF.show(3)

// COMMAND ----------

// save to file
// emoticonDF.write.format("parquet").mode("overwrite").save("/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/processedEmoticonClusterParquets/emoticonCluster.parquet")

// COMMAND ----------

// MAGIC %md
// MAGIC The goal for the last part of this notebook will be to display for each country what proportion of its total tweets correspond to a certain cluster. First we create a dataframe `emoticonCCDF` which contains the total number of tweets with some emoticon for each country. Using that dataframe we create dataframes containing the described proportions for each cluster and transfer these dataframes from scala to python by using the `createOrReplaceTmpView` function.

// COMMAND ----------

val emoticonCCDF = emoticonDF.groupBy($"countryCode")
                             .count
emoticonCCDF.show(3)

// COMMAND ----------

def createPropClusterDF (cluster : org.apache.spark.sql.Column) : org.apache.spark.sql.DataFrame = {  
  // This function filters the emoticonDF by a cluster-column and then
  // creates a dataframe with a row per country and columns for the countryCode and proportion
  // of tweets from that country that fall into the cluster as well as the count of tweets
  // falling into the cluster.
  val nbrClusterTweets = emoticonDF.filter(cluster).count
  val clusterDF = emoticonDF.filter(cluster)
                            .groupBy($"countryCode")
                            .count
  val propDF = emoticonCCDF.alias("total")
                           .join(clusterDF.alias("cluster"), "countryCode")
                           .select($"countryCode", $"cluster.count".alias("count"), ($"cluster.count" / $"total.count").alias("proportion"))
  return propDF
}

// COMMAND ----------

// MAGIC %md
// MAGIC Below we see an example of the dataframes generated by `createPropClusterDF`.

// COMMAND ----------

val clusterColumn = $"notHappy"
val propClusterDF = createPropClusterDF(clusterColumn)
propClusterDF.show(3)

// COMMAND ----------

def createPropClusterDFAndCreateTmpView (clusterName : String) = {
  // function for creating proportion dataframes for each cluster and making them available for later python code
  val propClusterDF = createPropClusterDF(org.apache.spark.sql.functions.col(clusterName))
  // make df available to python/sql etc
  propClusterDF.createOrReplaceTempView(clusterName)
}

// COMMAND ----------

// create proportion dataframes for each cluster and make them available for later python code
emoticonClusters.keys.foreach(createPropClusterDFAndCreateTmpView _)

// COMMAND ----------

// MAGIC %md
// MAGIC Now we turn to python to use the `pycountry` package in order to translate the country codes into another standard (three letters instead of two) which makes plotting with the built in databricks `display` a breeze. The cell below contains some functions that read the dataframes from the temporary view created in scala and translate them to pandas dataframes with the three letter country codes. Also, we filter out countries for which there are fewer than 100 tweets.

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC def add_iso_a3_col(df_cc):
// MAGIC   cc_dict = {}
// MAGIC   for country in pycountry.countries:
// MAGIC     cc_dict[country.alpha_2] = country.alpha_3
// MAGIC 
// MAGIC     df_cc["iso_a3"] = df_cc["countryCode"].map(cc_dict)
// MAGIC   return df_cc
// MAGIC 
// MAGIC def cc_df_from_spark_to_pandas_and_process(df_cc, columnOfInterest):
// MAGIC   #df_cc should be a dataframe with a column "countryCode" and a column columnOfInterest which has some interesting numerical data
// MAGIC   df_cc = df_cc.toPandas()
// MAGIC   add_iso_a3_col(df_cc)
// MAGIC   df_cc = df_cc[["iso_a3", columnOfInterest]]  #reorder to have iso_a3 as first column (required in order to use the map view in display), and select the useful columns
// MAGIC   return df_cc
// MAGIC 
// MAGIC from pyspark.sql.functions import col
// MAGIC def createProps(clusterName):
// MAGIC   df = sql("select * from " + clusterName)
// MAGIC   return cc_df_from_spark_to_pandas_and_process(df.filter(col("count")/col("proportion") >= 100), "proportion") # filter so that only countries with at least 100 tweets in a given country are used

// COMMAND ----------

// MAGIC %md
// MAGIC Finally, we can show the proportion of tweets in each country that fall into each cluster. Make sure that the plot type is set to `map` in the outputs from the cells below. It is possible to hover over the countries to see the precise values.
// MAGIC 
// MAGIC If anything interesting can actually be read from these plots we leave for the reader to decide.

// COMMAND ----------

// MAGIC %python
// MAGIC display(createProps("happy"))

// COMMAND ----------

// MAGIC %python
// MAGIC display(createProps("notHappy"))

// COMMAND ----------

// MAGIC %python
// MAGIC display(createProps("monkey"))

// COMMAND ----------

// MAGIC %python
// MAGIC display(createProps("cat"))

// COMMAND ----------

// MAGIC %python
// MAGIC display(createProps("SK"))

// COMMAND ----------

// MAGIC %python
// MAGIC display(createProps("prayer"))