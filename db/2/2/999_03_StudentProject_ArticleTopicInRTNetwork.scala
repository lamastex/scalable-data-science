// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/CTcIJcgk87s/0.jpg)](https://www.youtube.com/embed/CTcIJcgk87s?start=0&end=2216&autoplay=1)

// COMMAND ----------

// MAGIC %md 
// MAGIC # Article Topics in Retweet Network
// MAGIC ## Student Project 
// MAGIC by [Li Caldeira Balkeståhl](https://www.linkedin.com/in/li-caldeira-balkest%C3%A5hl-9b839412b/) and [Mariama Jaiteh](https://www.linkedin.com/in/mariama-jaiteh-a97ab373/)
// MAGIC 
// MAGIC ### Idea: Do topic modelling on web articles linked from twitter and see what topics are being tweeted by whom
// MAGIC 
// MAGIC The aim of the project is to apply topic modelling on web articles linked in tweets obtained from Twitter. Since each article (and its topics) is linked to a user, we hope to highlight the topics spread by every user in their Twitter network.
// MAGIC 
// MAGIC 
// MAGIC ## How to run the notebook
// MAGIC The repos contains the scala source code and databricks notebook of our project. 
// MAGIC 
// MAGIC ## Steps
// MAGIC #### 1. Get list of URLs from twitter
// MAGIC #### 2. Get the article content
// MAGIC #### 3. Extract features, clean data
// MAGIC #### 4. Topic modelling
// MAGIC #### 5. Connect to tweet network

// COMMAND ----------

// MAGIC %md 
// MAGIC ##Note
// MAGIC This projects uses research data collected from twitter by Raazesh Sainudiin and collaborators. This data is not freely available. 
// MAGIC 
// MAGIC If you provide your own list of URL's, in an apache spark DataFrame (as org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [URL: string]), you should be able to run the full notebook by changing the appropriate variables

// COMMAND ----------

// MAGIC %md
// MAGIC ## 1. Get list of URLs from twitter
// MAGIC 
// MAGIC * UK election twitter data (experiment designed by Raazesh Sainudiin and Joakim Johansson)
// MAGIC * Filter on only those that have URL
// MAGIC * We experiment on only a sample of the data by taking a 3% random subsample of the distinct URLSs 
// MAGIC * For illustration purposes, this notebook includes a smaller testsample of only 13 URL's

// COMMAND ----------

//the original location of our dataset
val retweetUrlNetwork = spark.read.parquet("/datasets/MEP/GB/RetweetUrlNetworkAsParquetDF")
val ratioNoURL=retweetUrlNetwork.filter($"URL"==="").count().toFloat / retweetUrlNetwork.count().toFloat 
val retweetWithUrl = retweetUrlNetwork.filter($"URL"=!="")
val numURLs=retweetWithUrl.cache.count

// COMMAND ----------

//change here for your own dataset
val distinctURLs = retweetWithUrl.select("URL").distinct().cache
val numdistinctURS=distinctURLs.count()
val sampleOfdistinctURls = distinctURLs.sample(false,0.03,12345L)
val num3persample = sampleOfdistinctURls.count()
val testsample = distinctURLs.sample(false,0.00006,12345L)
testsample.count

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. Get the article content
// MAGIC 
// MAGIC * Inspiration (and code) from Mastering Spark for Data Science by Andrew Morgan, Antoine Amend, Matthew Hallet, David George
// MAGIC   * Code for URL extension
// MAGIC   * Example on how to use Goose
// MAGIC * Goose web scraper (library package com.syncthemall : goose-2.1.25)
// MAGIC   * Fetches html content, returns cleaned version of the text and main image (we did not use the image)
// MAGIC   * For 1% sample took about 30 min
// MAGIC   * Several options could make this quicker, for example filter uninteresting domains before fetching, shorter timeouts... 
// MAGIC * Used DataFrames and userdefinedfunction, then flattening the resulting dataframe

// COMMAND ----------

// MAGIC %run ./999_03_StudentProject_ArticleTopicInRTNetwork_webScraping

// COMMAND ----------

val test_scraped = getContent(testsample).cache

// COMMAND ----------

display(test_scraped)

// COMMAND ----------

// MAGIC %md 
// MAGIC As can be seen, this is quite slow, it already takes 15 seconds for only 13 URL's. For a 1% sample of our dataset (about 2500 URL's), it took about 20 minutes.
// MAGIC 
// MAGIC We incrementally build of 3% sample and saved the DataFrame as a parquet file

// COMMAND ----------

// MAGIC %md
// MAGIC ## 3. Extract features, clean data
// MAGIC 
// MAGIC * Scraped dataframe saved as parquet for easy access
// MAGIC * Clean out stuff that is not articles
// MAGIC   * youtube, vimeo have videos, twitter has tweets, not articles
// MAGIC   * exceptions during scraping will give status "not found"
// MAGIC     * but also some "found" whith just "null" as body (we only noticed this later)  
// MAGIC * Transform the texts into feature vectors to use with SparkML - we use the built in transformers and estimators of sparkML
// MAGIC   * First tokenize the text into words - use RegexTokenizer
// MAGIC   * Then remove stopwords, words that don't carry meaning - use StopWordsRemover
// MAGIC   * Then transform the array of words into a feature vector (a vector of counts over the vocabulary, the vocabulary is generated from our corpus, then the feature vectors are created from the vocabulary)
// MAGIC     * use CountVectorizer, creates a vector of counts over the vocabulary (in one case scaled by TF-IDF approach)
// MAGIC     * use HashingTF, first hash the terms, then create a frequency vector over the hashed terms (scaled by TF-IDF approach)
// MAGIC   

// COMMAND ----------

//read the parquet file with our 3% sample, it's a DataFrame with the URL, content (body), etc
val scraped_readfromfile= spark.read.parquet("/datasets/Projects_Li_Mariama/scraped_new3percent")
//filter out the articles which gave exceptions
val found_articles=scraped_readfromfile.filter($"status"=!="not found")
//filter out videos and twitter
val filtered_articles=found_articles.filter(($"domain"=!="www.youtube.com" && $"domain"=!="twitter.com" && $"domain"=!="vimeo.com" )).cache

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Extract the features

// COMMAND ----------

// MAGIC %md 
// MAGIC ###Note
// MAGIC We use a list of stopwords saved as a text file, you can provide your own or use the default

// COMMAND ----------

import org.apache.spark.ml.feature.{RegexTokenizer,StopWordsRemover}
import org.apache.spark.sql.functions._
// Tokenize article bodies
val regexTokenizer = new RegexTokenizer()
  .setInputCol("body")
  .setOutputCol("tokens")
  .setPattern("\\W") 
  .setMinTokenLength(2) // Filter away tokens with length < 2
val wordsData = regexTokenizer.transform(filtered_articles)

// Obtain the stopwords
val stopwords = sc.textFile("/tmp/stopwords").collect()
val remover = new StopWordsRemover()
.setStopWords(stopwords) // This parameter is optional
.setInputCol("tokens")
.setOutputCol("words")

// Create new DataFrame with Stopwords removed
val filtered = remover.transform(wordsData)

import org.apache.spark.ml.feature.CountVectorizer
val vectorizer = new CountVectorizer()
.setInputCol("words")
.setOutputCol("features")
.setVocabSize(20000) //the size of the vocabulary
.setMinDF(5) // the minimum number of different documents a term must appear in to be included in the vocabulary.
.fit(filtered)

val countVectors = vectorizer.transform(filtered)

// COMMAND ----------

// MAGIC %md
// MAGIC ##  4. Topic modelling - usupervised learning
// MAGIC 
// MAGIC We want to find topics in our text collection by using unsupervised learning
// MAGIC 
// MAGIC * Latent Dirichlet allocation (LDA)
// MAGIC   * http://www.cs.columbia.edu/~blei/papers/Blei2012.pdf
// MAGIC   * Models the documents as coming from one or several topics (distributions over words)
// MAGIC   * Fit method fits the topics
// MAGIC   * Transform method tells how much of each document is from each topic
// MAGIC * K-means
// MAGIC   * Finds clusters by calculating distances between points
// MAGIC   * https://en.wikipedia.org/wiki/K-means_clustering#Algorithms

// COMMAND ----------

// MAGIC %md 
// MAGIC #LDA results

// COMMAND ----------

//running the LDA
import org.apache.spark.ml.clustering.LDA
val numTopics=10
val lda = new LDA()
.setK(numTopics)
.setMaxIter(10)

val model = lda.fit(countVectors)

// COMMAND ----------

// MAGIC %md 
// MAGIC Access the topics from the fit, translate the word index to words (strings) and get a dataframe of topics described by the most probable words

// COMMAND ----------

val topicIndices = model.describeTopics(maxTermsPerTopic = 8)
val vocabList = vectorizer.vocabulary
val topics= topicIndices.map(
      row => ( row.getAs[Int](0),
              row.getAs[Seq[Int]]("termIndices").map(vocabList(_)).zip(row.getAs[Seq[Double]]("termWeights"))))
    .withColumnRenamed("_1","topic").withColumnRenamed("_2","description")
//can use display to see the topics

// COMMAND ----------

//transform the topics dataframe into a suitable format for the visualization with D3 (D3 code copied from course notebook) 
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._
//explode makes several rows for each element of "description"
val exploded_topics=topics.withColumn("description", explode($"description"))

//function to flatten the dataframe
implicit class DataFrameFlattener(df: DataFrame) {
  def flattenSchema: DataFrame = {
    df.select(flatten(Nil, df.schema): _*)
  }

  protected def flatten(path: Seq[String], schema: DataType): Seq[Column] = schema match {
    case s: StructType => s.fields.flatMap(f => flatten(path :+ f.name, f.dataType)) 
    case other => col(path.map(n => s"`$n`").mkString(".")).as(path.mkString(".")) :: Nil //original
  }
}

val flat_topics=exploded_topics.flattenSchema.withColumnRenamed("description._1","term").withColumnRenamed("description._2","probability")
// Create JSON data to be passed to D3 visualization
val rawJson = flat_topics.toJSON.collect().mkString(",\n")

// COMMAND ----------

displayHTML(s"""
<!DOCTYPE html>
<meta charset="utf-8">
<style>

circle {
  fill: rgb(31, 119, 180);
  fill-opacity: 0.5;
  stroke: rgb(31, 119, 180);
  stroke-width: 1px;
}

.leaf circle {
  fill: #ff7f0e;
  fill-opacity: 1;
}

text {
  font: 14px sans-serif;
}

</style>
<body>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js"></script>
<script>

var json = {
 "name": "data",
 "children": [
  {
     "name": "topics",
     "children": [
      ${rawJson}
     ]
    }
   ]
};

var r = 1200,
    format = d3.format(",d"),
    fill = d3.scale.category20c();

var bubble = d3.layout.pack()
    .sort(null)
    .size([r, r])
    .padding(1.5);

var vis = d3.select("body").append("svg")
    .attr("width", r)
    .attr("height", r)
    .attr("class", "bubble");

  
var node = vis.selectAll("g.node")
    .data(bubble.nodes(classes(json))
    .filter(function(d) { return !d.children; }))
    .enter().append("g")
    .attr("class", "node")
    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    color = d3.scale.category20();
  
  node.append("title")
      .text(function(d) { return d.className + ": " + format(d.value); });

  node.append("circle")
      .attr("r", function(d) { return d.r; })
      .style("fill", function(d) {return color(d.topicName);});

var text = node.append("text")
    .attr("text-anchor", "middle")
    .attr("dy", ".3em")
    .text(function(d) { return d.className.substring(0, d.r / 3)});
  
  text.append("tspan")
      .attr("dy", "1.2em")
      .attr("x", 0)
      .text(function(d) {return Math.ceil(d.value * 10000) /10000; });

// Returns a flattened hierarchy containing all leaf nodes under the root.
function classes(root) {
  var classes = [];

  function recurse(term, node) {
    if (node.children) node.children.forEach(function(child) { recurse(node.term, child); });
    else classes.push({topicName: node.topic, className: node.term, value: node.probability});
  }

  recurse(null, root);
  return {children: classes};
}
</script>
""")

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Need to clean better
// MAGIC 
// MAGIC * remove other languages
// MAGIC * remove non-articles
// MAGIC   * for example messages about cookies, about javascript, etc
// MAGIC   
// MAGIC For lack of time, we just cleaned the data by filtering out things we found that were not articles or that were other languages. For a code that works well with other document collections, more time should be put into finding suitable, general filters that do not remove the interesting articles

// COMMAND ----------

val articles_clean=wordsData.filter($"body"=!="null") //our error handling from before means we have the string null for articles that threw exceptions (but we don't want them)
        .filter(not($"body".contains("We've noticed that you are using an ad blocker."))) //apparently we have some of these adblock messages
        .filter(not($"body"==="What term do you want to search?")) //we also have some of these...
        .filter(not($"body".contains("uses cookies"))) //lots of sites use cookies, some get as our articles...
        .filter(not($"body".contains("continuing to browse")))//more cookie stuff
        .filter(not($"body".contains(" das "))) //get rid of most german
        .filter(not($"body".contains(" det "))) //get rid of 7 swedish/danish
        .filter(not($"body".contains(" il "))) //get rid of italian and some more french
        .filter(not($"body".contains(" les "))) //some more french
        .filter(not($"body".contains(" nie "))) //one polish? article
        .filter(not($"body".contains(" Capitol Hill Publishing Corp"))) //"The contents of this site are ©2017 Capitol Hill Publishing Corp., a subsidiary of News Communications, Inc."
        .filter(not($"body".contains("You seem to be using an unsupported browser"))) //two flickr things
        .filter(not($"body".contains("Spotify Web Player")))//This browser doesn't support Spotify Web Player. Switch browsers or download Spotify for your desktop.
        .filter(not($"body".contains("See more of "))) //see moreof xxx on facebook
        .filter(not($"body".contains("enable JavaScript")))
        .filter(not($"body".contains("enable Javascript")))
        .filter(not($"body".contains("JavaScript isn't")))
        .filter(not($"body".contains("JavaScript seems")))
        .filter(not($"body".contains("Javascript functionality")))
        .filter(not($"body".contains("requires Javascript to be enabled")))
        .filter(not($"body".contains("To use this site")))
        .filter(not($"body".contains("For the best experience, please turn JavaScript on"))) //ted talks
        .filter(not($"body".contains("Sorry, this Silva Article is not viewable.")))//1 article
        .filter(not($"body".contains(" ist ")))//2 more german
        .filter(not($"body".contains(" che "))) //1 more italian
        .filter(not($"body".contains(" que "))) //1 more italian
        .filter(not($"body".contains("for this version"))) //	(or arXiv:1706.09254v2 arXiv:1706.09254v2 [cs.CL] for this version)
        .filter(not($"body".contains("Contact Us"))) //3 contact forms?
        .filter(not($"body".contains("Terms and conditions apply to all"))) //channel 5 competiontion winners?
        .filter(not($"body".contains("Terms and Conditions"))) //some terms and conditions
        .filter(not($"body".contains("If you followed a valid link")))

// COMMAND ----------

articles_clean.count()

// COMMAND ----------

// MAGIC %md
// MAGIC ### New stopwords
// MAGIC * remove more words that don't say much about the topics

// COMMAND ----------

//if we need to add stopwords
//from the words in the topics just identify said, mr, etc as not saying anything about the topic
val add_stopwords = Array("said","mr","new","people","just","year","like","told")
// Combine newly identified stopwords to our exising list of stopwords
val new_stopwords = stopwords.union(add_stopwords)

// COMMAND ----------

// Set params for StopWordsRemover
val remover = new StopWordsRemover()
.setStopWords(new_stopwords) // This parameter is optional
.setInputCol("tokens")
.setOutputCol("words")

// Create new DF with Stopwords removed
val filtered = remover.transform(articles_clean)
// Set params for CountVectorizer
val vectorizer = new CountVectorizer()
.setInputCol("words")
.setOutputCol("features")
//.setVocabSize(20000) //why limit the vocabulary in this way?
.setMinDF(3) // the minimum number of different documents a term must appear in to be included in the vocabulary.
.fit(filtered)
// get our countvector
val countVectors = vectorizer.transform(filtered)

// COMMAND ----------

val numTopics_new=7
val lda = new LDA()
//.setOptimizer(new OnlineLDAOptimizer().setMiniBatchFraction(0.8))
//.setFeaturesCol("features")
.setK(numTopics_new)
.setMaxIter(50)

val model = lda.fit(countVectors)

// COMMAND ----------

// Describe topics.
println("The topics described by their top-weighted terms:")
val topicIndices = model.describeTopics(maxTermsPerTopic = 5) //how to go from this DF to one whith the words for each term insted of just the indices? without going to rdd...
//topicIndices.show(false)
//topicIndices.printSchema()
val vocabList = vectorizer.vocabulary
val topics= topicIndices.map(
      row => ( row.getAs[Int](0),
              row.getAs[Seq[Int]]("termIndices").map(vocabList(_)).zip(row.getAs[Seq[Double]]("termWeights"))))
    .withColumnRenamed("_1","topic").withColumnRenamed("_2","description")
//topics.show(false)
//topics.printSchema()
topics.collect.foreach{ row=> 
  println("Topic " + row.getAs[Int](0))
  row.getSeq[Int](1). //does not check the seq type, so I don't need to make it correct since i don't know how to...
        foreach(println)
  println("=========")
}
//explode makes several rows for each element of "description"
val exploded_topics=topics.withColumn("description", explode($"description"))

//the function to flatten the dataframe
implicit class DataFrameFlattener(df: DataFrame) {
  def flattenSchema: DataFrame = {
    df.select(flatten(Nil, df.schema): _*)
  }

  protected def flatten(path: Seq[String], schema: DataType): Seq[Column] = schema match {
    case s: StructType => s.fields.flatMap(f => flatten(path :+ f.name, f.dataType)) 
    case other => col(path.map(n => s"`$n`").mkString(".")).as(path.mkString(".")) :: Nil //original
  }
}

val flat_topics=exploded_topics.flattenSchema.withColumnRenamed("description._1","term").withColumnRenamed("description._2","probability")
// Create JSON data to be passed to D3 visualization
val rawJson = flat_topics.toJSON.collect().mkString(",\n")

// COMMAND ----------

displayHTML(s"""
<!DOCTYPE html>
<meta charset="utf-8">
<style>

circle {
  fill: rgb(31, 119, 180);
  fill-opacity: 0.5;
  stroke: rgb(31, 119, 180);
  stroke-width: 1px;
}

.leaf circle {
  fill: #ff7f0e;
  fill-opacity: 1;
}

text {
  font: 14px sans-serif;
}

</style>
<body>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js"></script>
<script>

var json = {
 "name": "data",
 "children": [
  {
     "name": "topics",
     "children": [
      ${rawJson}
     ]
    }
   ]
};

var r = 1200,
    format = d3.format(",d"),
    fill = d3.scale.category20c();

var bubble = d3.layout.pack()
    .sort(null)
    .size([r, r])
    .padding(1.5);

var vis = d3.select("body").append("svg")
    .attr("width", r)
    .attr("height", r)
    .attr("class", "bubble");

  
var node = vis.selectAll("g.node")
    .data(bubble.nodes(classes(json))
    .filter(function(d) { return !d.children; }))
    .enter().append("g")
    .attr("class", "node")
    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    color = d3.scale.category20();
  
  node.append("title")
      .text(function(d) { return d.className + ": " + format(d.value); });

  node.append("circle")
      .attr("r", function(d) { return d.r; })
      .style("fill", function(d) {return color(d.topicName);});

var text = node.append("text")
    .attr("text-anchor", "middle")
    .attr("dy", ".3em")
    .text(function(d) { return d.className.substring(0, d.r / 3)});
  
  text.append("tspan")
      .attr("dy", "1.2em")
      .attr("x", 0)
      .text(function(d) {return Math.ceil(d.value * 10000) /10000; });

// Returns a flattened hierarchy containing all leaf nodes under the root.
function classes(root) {
  var classes = [];

  function recurse(term, node) {
    if (node.children) node.children.forEach(function(child) { recurse(node.term, child); });
    else classes.push({topicName: node.topic, className: node.term, value: node.probability});
  }

  recurse(null, root);
  return {children: classes};
}
</script>
""")

// COMMAND ----------

// MAGIC %md
// MAGIC ### Conclusion for LDA topics
// MAGIC Some clear topics, but room for improvement.
// MAGIC 
// MAGIC For example, should investigate:
// MAGIC 
// MAGIC * Different LDA parameters (number of topics, number of iterations...)
// MAGIC * Different minimization procedure
// MAGIC * More stopwords
// MAGIC * Other ways of getting the feature vector (like using TF-IDF)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Access topic probabilities for our dataset

// COMMAND ----------

import org.apache.spark.ml.linalg.{Vector}
//do the LDA transformation on our text dataframe
val transformed2 = model.transform(countVectors)
//define userdefinedfunction to get the probability for the topic we want, in this example topic 0
val getVector0 = udf((v:Vector) => v.apply(0))
//define userdefinedfunction to get the most probable topic for each text
val getMostProbableTopic=udf((v:Vector) => v.argmax)
//transform the dataframe to add the new columns
val blabla=transformed2.withColumn("topic0probability", getVector0($"topicDistribution")).withColumn("Topic",getMostProbableTopic($"topicDistribution"))

// COMMAND ----------

//Ex: look at the titles of the articles with a high percentage of topic 0
display(blabla.select($"title",$"topic0probability").filter($"topic0probability">0.9))

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC # K-means clustering of the articles extracted

// COMMAND ----------

// Some pre-cleaning of the dataframe - mainly to remove unwanted domain names
// !! (this will be merge lately with the previous filtering)

val filtered_articles4Kmeans=found_articles
    .filter(($"domain"=!="www.youtube.com" 
             && $"domain"=!="twitter.com" 
             &&  $"domain"=!="vimeo.com"
             && $"domain" =!= "www.facebook.com"
             && $"domain"=!="thehill.com"
             && $"domain" =!= "www.mixcloud.com"))
  .filter($"body"=!="null")//our error handling from before means we have the string null for articles that threw exceptions (but we don't want them)
  .filter(not($"body"===""))
  .filter(not(length(col("body")) <= 200)) // count the number of characters
  .filter(not($"body"==="What term do you want to search?")) //we also have some of these...
  .filter(not($"body".contains("We've noticed that you are using an ad blocker."))) 
  .filter(not($"body".contains("é"))) //we also have some of these...
  .filter(not($"body".contains("è")))
  .filter(not($"body".contains("à")))
  .filter(not($"body".contains("á")))
  .filter(not($"body".contains("í")))
  .filter(not($"body".contains("ì")))
  .filter(not($"body".contains("ò")))
  .filter(not($"body".contains(" das ")))
  .filter(not($"body".contains("ö")))
  .filter(not($"body".contains("We use cookies ")))
  .filter(not($"body".contains("This site is marked private by its owner."))) // for wordpress
  .filter(not($"body".contains("We’ve noticed that JavaScript is ")))
  .filter(not($"body".contains("You seem to be using an unsupported browser")))
  .filter(not($"body".contains("Spotify Web Player")))
  .filter(not($"body".contains("We know that sometimes it’s easier for us to come to you with the news.")))
  .filter(not($"body".contains("enable JavaScript")))
  .filter(not($"body".contains("enable Javascript")))
  .filter(not($"body".contains("JavaScript isn't")))
  .filter(not($"body".contains("JavaScript seems")))
  .filter(not($"body".contains("Javascript functionality")))
  .filter(not($"body".contains("requires Javascript to be enabled")))
  .filter(not($"body".contains("To use this site")))
  .filter(not($"body".contains("For the best experience, please turn JavaScript on"))) //ted talks
  .filter(not($"body".contains("for this version"))) //	(or arXiv:1706.09254v2 arXiv:1706.09254v2 [cs.CL] for this version)
  .filter(not($"body".contains("Contact Us"))) //3 contact forms?
  .filter(not($"body".contains("Terms and conditions apply to all"))) //channel 5 competiontion winners?
  .filter(not($"body".contains("Terms and Conditions"))) //some terms and conditions
  .filter(not($"body".contains("If you followed a valid link")))
  .filter(not($"body".contains("turn off your ad blocker")))
//two URL columns
    //.join(retweetUrlNetwork.drop("Domain"), retweetUrlNetwork("URL") === found_articles("URL"), "inner") 
//.join(retweetUrlNetwork.drop("Domain"),Seq("URL)) // gives a inner join with no duplicated colums
    .cache

// COMMAND ----------

// Featurize using the CountVectorizer object
val CountV = new CountVectorizer()
  // each cell corresponds to an array of words
  .setInputCol("words")
  .setOutputCol("feature_wordfreq")
  .setMinDF(5)
  .fit(wordsDataf)
// transform the dataframe by adding a new column with the feature vectors
val tfcv = CountV.transform(wordsDataf)

// COMMAND ----------

// Remove stopwords

// Create new DF with Stopwords removed
val wordsDataf = remover.transform(wordsData2)

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ### 1.a. K-means using vectors of term frequencies.
// MAGIC 
// MAGIC As previously shown, we use CountVectorizer to convert the article body into a vector of term frequency.

// COMMAND ----------

// MAGIC %md 
// MAGIC Run K-means using the vectorial representation of the text as input (features).
// MAGIC 
// MAGIC We decided to generate 10 clusters (K=10), the articles will be clusterized around these clusters center using pairwise distances 

// COMMAND ----------

// When the filtering is completed and unified, we will use this assignation
//val tfcv = countVectors

// Building the K-Means model
import org.apache.spark.ml.clustering.KMeans
val model_tfcv = new KMeans()
  .setTol(0.01) // 
  .setK(10) // number of clusters
  .setMaxIter(1000) // number of iterative runs
  .setFeaturesCol("feature_wordfreq") 
  //.setFeaturesCol("features") // When the filtering is unified, we will use this
  .setPredictionCol("predictions_tfcv")
  .setSeed(123456) // the random seed for clustering can be removed 
  .fit(tfcv)

// transform the filtered dataframe and the predicted cluster (column "predictions_tfcv")
val K_modeling_TFCV = model_tfcv.transform(tfcv)

// COMMAND ----------

// MAGIC %md 
// MAGIC ### The K-means clusters center

// COMMAND ----------

// Look at cluster centers
println("Cluster Centers: ")
model_tfcv.clusterCenters.foreach(println)

// COMMAND ----------

// Sorting of clusters by population size
val cluster_TFCV_size = K_modeling_TFCV
  .groupBy("predictions_tfcv")
  .count()
  .sort("predictions_tfcv")
  .sort(desc("count"))

// COMMAND ----------

// Size of the cluster
display(cluster_TFCV_size)

// COMMAND ----------

//LDA topic clusters for comparison. Note, the LDA only had 7 topics
display(blabla.groupBy("Topic").count().sort("Topic").sort(desc("count")))

// COMMAND ----------

// Selection of one cluster, for example
display(K_modeling_TFCV
       .filter($"predictions_tfcv" === 8)
       .select("predictions_tfcv","title","body"))

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC By looking at some articles from each cluster:
// MAGIC 
// MAGIC * Cluster 0: Diverse
// MAGIC * Cluster 8: Taxes, Care, People, Politics -- Diverse
// MAGIC * Cluster 9: about Trump, Trump+Russia, US politics
// MAGIC * Cluster 3: Ecology, Climate change
// MAGIC Some clusters are singletons.

// COMMAND ----------

// MAGIC %md 
// MAGIC ### 1.b. K-means with TF-IDF
// MAGIC Can we improve the clustering with TF-IDF ?
// MAGIC 
// MAGIC We will apply the Inverse Document Frequency (IDF) to evaluate the importance of each word.
// MAGIC Using frequency vectors (TF), we will estimate the importance of the words in a corpus

// COMMAND ----------

import org.apache.spark.ml.feature.{IDF,Normalizer}

// Creating an IDF estimator
val idf_cv = new IDF()
  .setInputCol("feature_wordfreq") // to replace by "features" when filtering is unified
  .setOutputCol("features_cv")

// Generate an IDF model 
val idfModel2 = idf_cv.fit(tfcv)
// and scale the feature vector 
val rescaledData2 = idfModel2.transform(tfcv)
// normalize
val normalizer2 = new Normalizer()
  .setInputCol("features_cv")
  .setOutputCol("normFeatures_cv")
  .setP(1.0)

val sparseVectorCVTF_IDF = normalizer2.transform(rescaledData2)

// COMMAND ----------

// Building a new K-Means models using Term frequencies (from countVector) and IDF
// K-Means modeling is done as explained above
val model_tfcv_idf = new KMeans()
  .setTol(0.01)
  .setK(10)
  .setMaxIter(1000)
  .setFeaturesCol("normFeatures_cv")
  .setPredictionCol("predictions_tfcv_idf")
  .setSeed(123456)
  .fit(sparseVectorCVTF_IDF)

val K_modeling_TFCV_IDF = model_tfcv_idf.transform(sparseVectorCVTF_IDF)

// COMMAND ----------

val cluster_TFCV_IDF_size = K_modeling_TFCV_IDF
  .groupBy("predictions_tfcv_idf")
  .count()
  .sort("predictions_tfcv_idf")
  .sort(desc("count"))

// COMMAND ----------

display(cluster_TFCV_IDF_size)

// COMMAND ----------

display(K_modeling_TFCV_IDF
       .filter($"predictions_tfcv_idf" === 5)
       .select("predictions_tfcv_idf","title", "body"))

// COMMAND ----------

// MAGIC %md 
// MAGIC By looking at some articles from each cluster:
// MAGIC 
// MAGIC * Cluster 0: Diverse
// MAGIC * Cluster 5: US related (politics, economics)

// COMMAND ----------

// MAGIC %md 
// MAGIC ### 2. K-means using TF-IDF with HashingTF
// MAGIC 
// MAGIC Instead of CountVectorizer, use a HashingTF on the texts (after tokenization and removal of stopwords)
// MAGIC 
// MAGIC For this case, we use a Hash vector = a fixed-sized feature vector that characterizes the article body ("a set of terms")

// COMMAND ----------

//Generate hashing vectors
import org.apache.spark.ml.feature.{HashingTF}
val hashingTF = new HashingTF()
  .setInputCol("words")
  .setOutputCol("rawFeatures")
  .setNumFeatures(18000) // number of features in the vector. We chose a value greater to the vocabulary size

val featurizedData = hashingTF.transform(wordsDataf)

val idf = new IDF()
  .setInputCol("rawFeatures")
  .setOutputCol("features")

val idfModel = idf.fit(featurizedData)

val rescaledData = idfModel.transform(featurizedData)

val normalizer = new Normalizer()
  .setInputCol("features")
  .setOutputCol("normFeatures")
  .setP(1.0)

val sparseVectorDF = normalizer.transform(rescaledData)

// COMMAND ----------

val model_TFIDF = new KMeans()
  .setTol(0.01)
  .setK(15)
  .setMaxIter(1000)
  .setFeaturesCol("normFeatures")
  .setPredictionCol("predictions")
  .setSeed(123456)
  .fit(sparseVectorDF)

val K_modeling_sparseVectorDF = model_TFIDF.transform(sparseVectorDF)

// COMMAND ----------

val cluster_sparseVectorDF_size = K_modeling_sparseVectorDF
  .groupBy("predictions")
  .count()
  .sort("predictions")
  .sort(desc("count"))

// COMMAND ----------

display(cluster_sparseVectorDF_size)

// COMMAND ----------

// Example of cluster
display(K_modeling_sparseVectorDF
        .filter($"predictions" ===10)
        .select("predictions","title","body")
        )

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC - Cluster 0: Diverse
// MAGIC - Cluster 10: London attacks, Manchester
// MAGIC - Cluster 5: Trump, US politics, Trump-Russia

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Conclusions about the K-Means clustering:
// MAGIC 
// MAGIC - The clustring needs to be improved:
// MAGIC   * a big cluster with diverse articles; suggest that the clustering has some flaws.
// MAGIC     - Some considerations have to be made regarding:
// MAGIC       - article body size, 
// MAGIC       - feature vectors (count vector vs. hashing features vector) 
// MAGIC       - ideal number of clusters, 
// MAGIC       - number of iterations, ...
// MAGIC   * few meaningful clusters are obtained
// MAGIC - Using Normalization and IDF improve the clustering output.

// COMMAND ----------

// MAGIC %md
// MAGIC ##  5. Connect to tweet network
// MAGIC 
// MAGIC * Which is the most probable topc or topics for each tweet with an article?
// MAGIC * We did not have time for this

// COMMAND ----------

//join our LDA results with K-means results
val joinedDF=blabla.select($"URL",$"title",$"body",$"domain",$"Topic").join(K_modeling_TFCV_IDF.drop($"tokens").drop($"words").drop($"description")
                                                                            .drop($"keywords").drop($"status").drop($"feature_wordfreq")
                                                                            .drop($"features_cv").drop($"normFeatures_cv"),  Seq("URL","title","body","domain"))

// COMMAND ----------

//join with the tweet dataframe (for example, includes original post user ID as OPostUserSNinRT)
val newjoinedDF= joinedDF.join(retweetWithUrl.drop("Domain"), Seq("URL"), "inner")

// COMMAND ----------

//What are the topics in the articles tweeted by Jeremy Corbin?
//We only have 1 in our small sample
display(newjoinedDF.filter($"OPostUserSNinRT"==="jeremycorbyn").select($"title",$"body",$"Topic", $"predictions_tfcv_idf",$"OPostUserSNinRT"))

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ### Future work:
// MAGIC 
// MAGIC - Do a better filtering of the urls without clear body article