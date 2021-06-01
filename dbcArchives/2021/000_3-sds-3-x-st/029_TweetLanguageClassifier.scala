// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Twitter Streaming Language Classifier
// MAGIC 
// MAGIC This is a databricksification of [https://databricks.gitbooks.io/databricks-spark-reference-applications/content/twitter_classifier/index.html](https://databricks.gitbooks.io/databricks-spark-reference-applications/content/twitter_classifier/index.html) by Amendra Shreshta.
// MAGIC 
// MAGIC Note that you need to change the fields in background notebook `025_a_extendedTwitterUtils2run` so more fields for `lang` in Tweets are exposed. This is a good example of how one has to go deeper into the java code of `twitter4j` as new needs arise.

// COMMAND ----------

// MAGIC %run "./025_c_extendedTwitterUtils2runWithLangs"

// COMMAND ----------

import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._

import scala.math.Ordering

import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **Enter your Twitter API Credentials.**
// MAGIC * Go to https://apps.twitter.com and look up your Twitter API Credentials, or create an app to create them.
// MAGIC * Run the code in a cell to Enter your own credentials.
// MAGIC 
// MAGIC ```%scala
// MAGIC // put your own twitter developer credentials below instead of xxx
// MAGIC // instead of the '%run ".../secrets/026_secret_MyTwitterOAuthCredentials"' below
// MAGIC // you need to copy-paste the following code-block with your own Twitter credentials replacing XXXX
// MAGIC 
// MAGIC 
// MAGIC // put your own twitter developer credentials below 
// MAGIC 
// MAGIC import twitter4j.auth.OAuthAuthorization
// MAGIC import twitter4j.conf.ConfigurationBuilder
// MAGIC 
// MAGIC 
// MAGIC // These have been regenerated!!! - need to chane them
// MAGIC 
// MAGIC def myAPIKey       = "XXXX" // APIKey 
// MAGIC def myAPISecret    = "XXXX" // APISecretKey
// MAGIC def myAccessToken          = "XXXX" // AccessToken
// MAGIC def myAccessTokenSecret    = "XXXX" // AccessTokenSecret
// MAGIC 
// MAGIC 
// MAGIC System.setProperty("twitter4j.oauth.consumerKey", myAPIKey)
// MAGIC System.setProperty("twitter4j.oauth.consumerSecret", myAPISecret)
// MAGIC System.setProperty("twitter4j.oauth.accessToken", myAccessToken)
// MAGIC System.setProperty("twitter4j.oauth.accessTokenSecret", myAccessTokenSecret)
// MAGIC 
// MAGIC println("twitter OAuth Credentials loaded")
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC The cell-below will not expose my Twitter API Credentials: `myAPIKey`, `myAPISecret`, `myAccessToken` and `myAccessTokenSecret`. Use the code above to enter your own credentials in a scala cell.

// COMMAND ----------

// MAGIC %run "Users/raazesh.sainudiin@math.uu.se/scalable-data-science/secrets/026_secret_MyTwitterOAuthCredentials"

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 1. Collect Data
// MAGIC Start downloading tweets in order to start building a simple model for language classification.

// COMMAND ----------

// ## Let's create a directory in dbfs for storing tweets in the cluster's distributed file system.
val outputDirectoryRoot = "/datasets/tweetsStreamTmp" // output directory

// COMMAND ----------

// to remove a pre-existing directory and start from scratch uncomment next line and evaluate this cell
dbutils.fs.rm(outputDirectoryRoot, true) 

// COMMAND ----------

// ## Capture tweets in every sliding window of slideInterval many milliseconds.
val slideInterval = new Duration(1 * 1000) // 1 * 1000 = 1000 milli-seconds = 1 sec

// COMMAND ----------

// Our goal is to take each RDD in the twitter DStream and write it as a json file in our dbfs.
// Create a Spark Streaming Context.
val ssc = new StreamingContext(sc, slideInterval)

// COMMAND ----------

// Create a Twitter Stream for the input source. 
val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth)

// COMMAND ----------

// Let's import google's json library next.
import com.google.gson.Gson 
//Let's map the tweets into json formatted string (one tweet per line).
val twitterStreamJson = twitterStream.map(
                                            x => { val gson = new Gson();
                                                 val xJson = gson.toJson(x)
                                                 xJson
                                                 }
                                          ) 

// COMMAND ----------

val partitionsEachInterval = 1 

val batchInterval = 1 // in minutes
val timeoutJobLength =  batchInterval * 5

var newContextCreated = false
var numTweetsCollected = 0L // track number of tweets collected

twitterStreamJson.foreachRDD((rdd, time) => { // for each filtered RDD in the DStream
      val count = rdd.count()
      if (count > 0) {
        val outputRDD = rdd.repartition(partitionsEachInterval) // repartition as desired
        // to write to parquet directly in append mode in one directory per 'time'------------       
        val outputDF = outputRDD.toDF("tweetAsJsonString")
        // get some time fields from current `.Date()`
        val year = (new java.text.SimpleDateFormat("yyyy")).format(new java.util.Date())
        val month = (new java.text.SimpleDateFormat("MM")).format(new java.util.Date())
        val day = (new java.text.SimpleDateFormat("dd")).format(new java.util.Date())
        val hour = (new java.text.SimpleDateFormat("HH")).format(new java.util.Date())
        // write to a file with a clear time-based hierarchical directory structure for example
        outputDF.write.mode(SaveMode.Append)
                .parquet(outputDirectoryRoot+ "/"+ year + "/" + month + "/" + day + "/" + hour + "/" + time.milliseconds) 
        // end of writing as parquet file-------------------------------------
        numTweetsCollected += count // update with the latest count
      }
  })

// COMMAND ----------

// ## Let's start the spark streaming context we have created next.
ssc.start()

// COMMAND ----------

// total tweets downloaded
numTweetsCollected

// COMMAND ----------

// ## Go to SparkUI and see if a streaming job is already running. If so you need to terminate it before starting a new streaming job. Only one streaming job can be run on the DB CE.
// #  let's stop the streaming job next.
ssc.stop(stopSparkContext = false) 
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 2: Explore Data

// COMMAND ----------

// MAGIC %run "./025_b_TTTDFfunctions"

// COMMAND ----------

// #Let's examine what was saved in dbfs
display(dbutils.fs.ls(outputDirectoryRoot))

// COMMAND ----------

// Replace the date with current date
val date = "/2020/11/*"
val rawDF = fromParquetFile2DF(outputDirectoryRoot + date +"/*/*") //.cache()

// COMMAND ----------

val TTTsDF = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(rawDF)).cache()

// COMMAND ----------

// Creating SQL table 
TTTsDF.createOrReplaceTempView("tbl_tweet")

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 3. Build Model
// MAGIC Let us use the structured data in `tbl_tweet` to build a simple classifier of the language using K-means.

// COMMAND ----------

sqlContext.sql("SELECT lang, CPostUserName, CurrentTweet FROM tbl_tweet LIMIT 10").collect.foreach(println)

// COMMAND ----------

// Checking the language of tweets
sqlContext.sql(
    "SELECT lang, COUNT(*) as cnt FROM tbl_tweet " +
    "GROUP BY lang ORDER BY cnt DESC limit 1000")
    .collect.foreach(println)

// COMMAND ----------

// extracting just tweets from the table and converting it to String
val texts = sqlContext
      .sql("SELECT CurrentTweet from tbl_tweet")
      .map(_.toString)

// COMMAND ----------

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}

// COMMAND ----------

// MAGIC %md
// MAGIC **Featurize as bigrams**
// MAGIC 
// MAGIC Create feature vectors by turning each tweet into bigrams of characters (an n-gram model)
// MAGIC and then hashing those to a length-1000 feature vector that we can pass to MLlib.

// COMMAND ----------


def featurize(s: String): Vector = {
  val n = 1000
  val result = new Array[Double](n)
  val bigrams = s.sliding(2).toArray
  for (h <- bigrams.map(_.hashCode % n)) {
    result(h) += 1.0 / bigrams.length
  }
  Vectors.sparse(n, result.zipWithIndex.filter(_._1 != 0).map(_.swap))
}

// COMMAND ----------

//Cache the vectors RDD since it will be used for all the KMeans iterations.
val vectors = texts.rdd
      .map(featurize)
      .cache()

// COMMAND ----------

// cache is lazy so count will force the data to store in memory
vectors.count()

// COMMAND ----------

vectors.first()

// COMMAND ----------

// MAGIC %md
// MAGIC **K-Means model** trained with 10 clusters and 10 iterations.

// COMMAND ----------

// Training model with 10 cluster and 10 iteration
val model = KMeans.train(vectors, k=10, maxIterations = 10)

// COMMAND ----------

// Sample 100 of the original set
val some_tweets = texts.take(100)

// COMMAND ----------

// iterate through the 100 samples and show which cluster they are in
for (i <- 0 until 10) {
  println(s"\nCLUSTER $i:")
  some_tweets.foreach { t =>
    if (model.predict(featurize(t)) == i) {
      println(t)
    }
  }
}

// COMMAND ----------

dbutils.fs.ls("/datasets/model/")

// COMMAND ----------

// to remove a pre-existing model and start from scratch
dbutils.fs.rm("/datasets/model", true) 

// COMMAND ----------

// save the model
sc.makeRDD(model.clusterCenters).saveAsObjectFile("/datasets/model")

// COMMAND ----------

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.clustering.KMeansModel

// COMMAND ----------

// Checking if the model works
val clusterNumber = 5

val modelFile = "/datasets/model"

val model: KMeansModel = new KMeansModel(sc.objectFile[Vector](modelFile).collect)
model.predict(featurize("واحد صاحبى لو حد يعرف اكونت وزير التعليم ")) == clusterNumber

// COMMAND ----------

model.predict(featurize("ご参加ありがとうございます❣")) == 2

// COMMAND ----------

model.predict(featurize("واحد صاحبى لو حد يعرف اكونت وزير التعليم ")) == 2

// COMMAND ----------

// MAGIC %md
// MAGIC **Loading model and printing tweets that matched the desired cluster.**

// COMMAND ----------


var newContextCreated = false
var num = 0

// Create a Spark Streaming Context.
@transient val ssc = new StreamingContext(sc, slideInterval)
// Create a Twitter Stream for the input source. 
@transient val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
@transient val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth)

//Replace the cluster number as you desire between 0 to 9
val clusterNumber = 2

//model location
val modelFile = "/datasets/model"

// Get tweets from twitter
val Tweet = twitterStream.map(_.getText)
//Tweet.print()

println("Initalizaing the the KMeans model...")
val model: KMeansModel = new KMeansModel(sc.objectFile[Vector](modelFile).collect)

//printing tweets that match our choosen cluster
Tweet.foreachRDD(rdd => {  
rdd.collect().foreach(i =>
    {
       val record = i
       if (model.predict(featurize(record)) == clusterNumber) {
       println(record)
    }
    })
})

// Start the streaming computation
println("Initialization complete.")
ssc.start()
ssc.awaitTermination()


// COMMAND ----------

// ## Go to SparkUI and see if a streaming job is already running. If so you need to terminate it before starting a new streaming job. Only one streaming job can be run on the DB CE.
// #  let's stop the streaming job next.
ssc.stop(stopSparkContext = false) 
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 