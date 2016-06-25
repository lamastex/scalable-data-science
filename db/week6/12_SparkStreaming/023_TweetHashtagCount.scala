// Databricks notebook source exported at Sat, 25 Jun 2016 00:25:30 UTC
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
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week6/12_SparkStreaming/023_TweetHashtagCount.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week6/12_SparkStreaming/023_TweetHashtagCount](http://img.youtube.com/vi/jqLcr2eS-Vs/0.jpg)](https://www.youtube.com/v/jqLcr2eS-Vs?rel=0&autoplay=1&modestbranding=1&start=3536&end=4519)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Twitter Hashtag Count
// MAGIC 
// MAGIC Using Twitter Streaming is a great way to learn Spark Streaming if you don't have your streaming datasource and want a great rich input dataset to try Spark Streaming transformations on.
// MAGIC 
// MAGIC In this example, we show how to calculate the top hashtags seen in the last X window of time every Y time unit.
// MAGIC 
// MAGIC Extracting knowledge from tweets is "easy" using techniques shown here, but one has to take responsibility for the use of this knowledge and conform to the rules and policies linked below.
// MAGIC 
// MAGIC Remeber that the use of twitter itself comes with various strings attached. Read:
// MAGIC 
// MAGIC - [Twitter Rules](https://twitter.com/rules)
// MAGIC 
// MAGIC 
// MAGIC Crucially, the use of the content from twitter by you (as done in this worksheet) comes with some strings.  Read:
// MAGIC - [Developer Agreement & Policy Twitter Developer Agreement](https://dev.twitter.com/overview/terms/agreement-and-policy)

// COMMAND ----------

import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter.TwitterUtils

import scala.math.Ordering

import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Step 1: Enter your Twitter API Credentials.
// MAGIC * Go to https://apps.twitter.com and look up your Twitter API Credentials, or create an app to create them.
// MAGIC * Run this cell for the input cells to appear.
// MAGIC * Enter your credentials.
// MAGIC * Run the cell again to pick up your defaults.
// MAGIC 
// MAGIC The cell-below is hidden to not expose the Twitter API Credentials: `consumerKey`, `consumerSecret`, `accessToken` and `accessTokenSecret`.

// COMMAND ----------

System.setProperty("twitter4j.oauth.consumerKey", getArgument("1. Consumer Key (API Key)", ""))
System.setProperty("twitter4j.oauth.consumerSecret", getArgument("2. Consumer Secret (API Secret)", ""))
System.setProperty("twitter4j.oauth.accessToken", getArgument("3. Access Token", ""))
System.setProperty("twitter4j.oauth.accessTokenSecret", getArgument("4. Access Token Secret", ""))

// COMMAND ----------

// MAGIC %md
// MAGIC If you see warnings then ignore for now:
// MAGIC [https://forums.databricks.com/questions/6941/change-in-getargument-for-notebook-input.html](https://forums.databricks.com/questions/6941/change-in-getargument-for-notebook-input.html).

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Step 2: Configure where to output the top hashtags and how often to compute them.
// MAGIC * Run this cell for the input cells to appear.
// MAGIC * Enter your credentials.
// MAGIC * Run the cell again to pick up your defaults.

// COMMAND ----------

val outputDirectory = getArgument("1. Output Directory", "/twitter")
val slideInterval = new Duration(getArgument("2. Recompute the top hashtags every N seconds", "1").toInt * 1000)
val windowLength = new Duration(getArgument("3. Compute the top hashtags for the last N seconds", "5").toInt * 1000)
val timeoutJobLength = getArgument("4. Wait this many seconds before stopping the streaming job", "100").toInt * 1000

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Step 3: Run the Twitter Streaming job.

// COMMAND ----------

// MAGIC %md
// MAGIC Clean up any old files.

// COMMAND ----------

dbutils.fs.rm(outputDirectory, true)

// COMMAND ----------

// MAGIC %md 
// MAGIC Create the function to that creates the Streaming Context and sets up the streaming job.

// COMMAND ----------

var newContextCreated = false
var num = 0

// This is a helper class used for 
object SecondValueOrdering extends Ordering[(String, Int)] {
  def compare(a: (String, Int), b: (String, Int)) = {
    a._2 compare b._2
  }
}

// This is the function that creates the SteamingContext and sets up the Spark Streaming job.
def creatingFunc(): StreamingContext = {
  // Create a Spark Streaming Context.
  val ssc = new StreamingContext(sc, slideInterval)
  // Create a Twitter Stream for the input source. 
  val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  val twitterStream = TwitterUtils.createStream(ssc, auth)
  
  // Parse the tweets and gather the hashTags.
  val hashTagStream = twitterStream.map(_.getText).flatMap(_.split(" ")).filter(_.startsWith("#"))
  
  // Compute the counts of each hashtag by window.
  // reduceByKey on a window of length windowLength
  // Once this is computed, slide the window by slideInterval and calculate reduceByKey again for the second window
  val windowedhashTagCountStream = hashTagStream.map((_, 1)).reduceByKeyAndWindow((x: Int, y: Int) => x + y, windowLength, slideInterval)

  // For each window, calculate the top hashtags for that time period.
  windowedhashTagCountStream.foreachRDD(hashTagCountRDD => {
    val topEndpoints = hashTagCountRDD.top(10)(SecondValueOrdering)
    dbutils.fs.put(s"${outputDirectory}/top_hashtags_${num}", topEndpoints.mkString("\n"), true)
    println(s"------ TOP HASHTAGS For window ${num}")
    println(topEndpoints.mkString("\n"))
    num = num + 1
  })
  
  newContextCreated = true
  ssc
}

// COMMAND ----------

// MAGIC %md 
// MAGIC Create the StreamingContext using getActiveOrCreate, as required when starting a streaming job in Databricks.

// COMMAND ----------

val ssc = StreamingContext.getActiveOrCreate(creatingFunc)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Start the Spark Streaming Context and return when the Streaming job exits or return with the specified timeout.  

// COMMAND ----------

ssc.start()
ssc.awaitTerminationOrTimeout(timeoutJobLength)
ssc.stop(stopSparkContext = false)

// COMMAND ----------

// MAGIC %md
// MAGIC Check out the Clusters 'Streaming` UI as the job is running.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC It should automatically stop the streaming job after `timeoutJobLength`.
// MAGIC 
// MAGIC If not, then stop any active Streaming Contexts, but don't stop the spark contexts they are attached to using the following command.

// COMMAND ----------

StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) }

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Step 4: View the Results.

// COMMAND ----------

display(dbutils.fs.ls(outputDirectory))

// COMMAND ----------

// MAGIC %md
// MAGIC There should be 100 intervals for each second and the top hashtage for each of them should be in the file `top_hashtags_N` for `N` in 0,1,2,...,99 and the top hashtags should be based on the past 5 seconds window.

// COMMAND ----------

dbutils.fs.head(s"${outputDirectory}/top_hashtags_11")

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