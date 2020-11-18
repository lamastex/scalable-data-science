// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC # Tweet Collector - capture live tweets 
// MAGIC ### by tracking a list of strings and following a list of users
// MAGIC 
// MAGIC In the previous notebook we were capturing tweets from the public streams (global collection of roughly 1% of all Tweets - note what's exactly available from the full twitter social media network, i.e. *all* status updates in the planet, for such free collection is not exactly known in terms of sub-sampling strategies, etc. This is Twitter's proprietary information. However, we can assume it is a random sample of roughly 1% of all tweets).
// MAGIC 
// MAGIC In this notebook, we can modify the collector to focus on specific communications of interest to us. Specifically, by including a list of strings to track and a list of twitter user-IDs to follow.
// MAGIC 
// MAGIC For this we will first `%run` the `ExtendedTwitterUtils` and `TTTDFfunctions` notebooks.

// COMMAND ----------

// MAGIC %run "./025_a_extendedTwitterUtils2run"

// COMMAND ----------

// MAGIC %run "./025_b_TTTDFfunctions"

// COMMAND ----------

// MAGIC %md
// MAGIC Go to SparkUI and see if a streaming job is already running. If so you need to terminate it before starting a new streaming job. Only one streaming job can be run on the DB CE.

// COMMAND ----------

// this will make sure all streaming job in the cluster are stopped
StreamingContext.getActive.foreach{ _.stop(stopSparkContext = false) } 

// COMMAND ----------

// MAGIC %md
// MAGIC Load your twitter credentials (secretly!).

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### First Step to Do Your Own Experiments in Twitter: Enter your Twitter API Credentials.
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
// MAGIC ## Using Twitter REST API
// MAGIC 
// MAGIC Next we import and instantiate for Twitter REST API.

// COMMAND ----------

// SOME IMPORTTS
import scala.collection.mutable.ArrayBuffer
import twitter4j._
import twitter4j.conf._
import scala.collection.JavaConverters._ 

import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.{StructType, StructField, StringType};
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import com.google.gson.Gson
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import com.google.gson.Gson
import org.apache.spark.sql.DataFrame

val cb = new ConfigurationBuilder()       

val twitter = {
  val c = new ConfigurationBuilder
    c.setDebugEnabled(false)
    .setOAuthConsumerKey(myAPIKey)
    .setOAuthConsumerSecret(myAPISecret)
    .setOAuthAccessToken(myAccessToken)
    .setOAuthAccessTokenSecret(myAccessTokenSecret);

  new TwitterFactory(c.build()).getInstance()
}

// COMMAND ----------

// MAGIC %md
// MAGIC Let's quickly test that the REST API calls can be made.

// COMMAND ----------

twitter.showUser("@raazozone").getId() // quick test that REST API works - should get 4173723312

// COMMAND ----------

twitter.showUser("@realDonaldTrump").getId() // quick test that REST API works - should get 25073877

// COMMAND ----------

twitter.showUser("@WASP_Research").getId() // quick test that REST API works - should get ?

// COMMAND ----------

// MAGIC %md
// MAGIC Let's import a list of twitterIDS of interest to us...

// COMMAND ----------

val screenNamesOfInterest = List("raazozone","realDonaldTrump","WASP_Research") // could be done from a large list of up to 4,000 or so accounts

// COMMAND ----------

def lookupUserSNs(Retweeterids:Seq[String])={
  val grouped=Retweeterids.grouped(100).toList 
  for {group<-grouped  
       users=twitter.lookupUsers(group:_*)
       user<-users.asScala 
   } yield user     
}// we loose some suspended accounts...

def lookupUsers(Retweeterids:Seq[Long])={
  val grouped=Retweeterids.grouped(100).toList 
  for {group<-grouped  
       users=twitter.lookupUsers(group:_*)
       user<-users.asScala 
   } yield user     
}// we loose some suspended accounts...

// COMMAND ----------

val twitterUsersOfInterest = lookupUserSNs(screenNamesOfInterest) // not displayed

// COMMAND ----------

println(twitterUsersOfInterest.size, screenNamesOfInterest.size) // we could lose users due to suspended accounts etc...

// COMMAND ----------

// just get their IDs
val seedIDs = twitterUsersOfInterest.map(u => u.getId()).toSet.toSeq.filter(_.isValidLong) // just get the IDs of the seed users who are valid

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Now, let's extend our function.

// COMMAND ----------

import com.google.gson.Gson 
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

val outputDirectoryRoot = "/datasets/tweetsStreamTmp" // output directory
val batchInterval = 1 // in minutes
val timeoutJobLength =  batchInterval * 5

var newContextCreated = false
var numTweetsCollected = 0L // track number of tweets collected
//val conf = new SparkConf().setAppName("TrackedTweetCollector").setMaster("local")
// This is the function that creates the SteamingContext and sets up the Spark Streaming job.
def streamFunc(): StreamingContext = {
  // Create a Spark Streaming Context.
  val ssc = new StreamingContext(sc, Minutes(batchInterval))
  // Create the OAuth Twitter credentials 
  val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  
  val track = List("WASP rules!", "#MakeDataGreatAgain","sds-3-x rules!")//, "Hi")// just added for some live tests
  //val track = List.empty // if you do not want to track by any string
  
  val follow = seedIDs // who to follow in Twitter
  //val follow = List.empty // if you do not want to folow any specific twitter user
  
  // Create a Twitter Stream for the input source.  
  val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth, track, follow)
  // Transform the discrete RDDs into JSON
  val twitterStreamJson = twitterStream.map(x => { val gson = new Gson();
                                                 val xJson = gson.toJson(x)
                                                 xJson
                                               }) 
  // take care
  val partitionsEachInterval = 1 // This tells the number of partitions in each RDD of tweets in the DStream.
  
  // what we want done with each discrete RDD tuple: (rdd, time)
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
  newContextCreated = true
  ssc
}

// COMMAND ----------

val ssc = StreamingContext.getActiveOrCreate(streamFunc)

// COMMAND ----------

ssc.start()
//ssc.awaitTerminationOrTimeout(timeoutJobLength) // you only need one of these to start

// COMMAND ----------

display(dbutils.fs.ls("/datasets/tweetsStreamTmp/2020/"))

// COMMAND ----------

display(dbutils.fs.ls(outputDirectoryRoot+"/2020/11/14/14/1605363120000/")) // keep adding sub-dirs and descent into time-tree'd directory hierarchy

// COMMAND ----------

val rawDF = fromParquetFile2DF(outputDirectoryRoot+"/2020/11/*/*/*/*") //.cache()
rawDF.count

// COMMAND ----------

val TTTsDF = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(rawDF)).cache()

// COMMAND ----------

// MAGIC %md
// MAGIC Collect for a few minutes so all fields are availabale in the Tweets... otherwise you will get errors like this  (which may be unavidable if what you are tracking has no `geoLocation` informaiton for example, only a small fraction of Tweets have this information):
// MAGIC 
// MAGIC 
// MAGIC > "org.apache.spark.sql.AnalysisException: cannot resolve '`geoLocation.latitude`' given input columns: [contributorsIDs, createdAt, currentUserRetweetId, displayTextRangeEnd, displayTextRangeStart, favoriteCount, hashtagEntities, id, inReplyToScreenName, inReplyToStatusId, inReplyToUserId, isFavorited, isPossiblySensitive, isRetweeted, isTruncated, lang, mediaEntities, place, quotedStatus, quotedStatusId, quotedStatusPermalink, retweetCount, retweetedStatus, source, symbolEntities, text, urlEntities, user, userMentionEntities, withheldInCountries];;"
// MAGIC 
// MAGIC 
// MAGIC We can parse more robustly... but let's go and modify the function so it does not look for the missing fields in these tweeets...

// COMMAND ----------

// MAGIC %run "./025_b_TTTDFfunctions"

// COMMAND ----------

val TTTsDF = tweetsDF2TTTDFLightWeight(tweetsJsonStringDF2TweetsDF(rawDF)).cache()

// COMMAND ----------

display(TTTsDF)  // output not displayed to comply with Twitter Developer rules

// COMMAND ----------

// this will make sure all streaming job in the cluster are stopped 
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 

// COMMAND ----------

val a = TTTsDF.filter($"CurrentTweet" contains "WASP rules!")//.collect()

// COMMAND ----------

display(a)

// COMMAND ----------

val b = TTTsDF.filter($"CurrentTweet" contains "#MakeDataGreatAgain")//.collect()
display(b)

// COMMAND ----------

// this will make sure all streaming job in the cluster are stopped - raaz
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 

// COMMAND ----------

// this will delete what we collected to keep the disk usage tight and tidy
dbutils.fs.rm(outputDirectoryRoot, true) 

// COMMAND ----------

