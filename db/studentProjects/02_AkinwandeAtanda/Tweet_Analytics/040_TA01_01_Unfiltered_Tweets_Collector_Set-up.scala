// Databricks notebook source exported at Sat, 25 Jun 2016 02:23:44 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Generic Twitter Collector
// MAGIC 
// MAGIC 
// MAGIC Remeber that the use of twitter itself comes with various strings attached. 
// MAGIC - **Read:** [Twitter Rules](https://twitter.com/rules)
// MAGIC 
// MAGIC 
// MAGIC Crucially, the use of the content from twitter by you (as done in this worksheet) comes with some strings.
// MAGIC - **Read:** [Developer Agreement & Policy Twitter Developer Agreement](https://dev.twitter.com/overview/terms/agreement-and-policy)

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
// MAGIC ### Step 2: Configure where to output each unfiltered Batches of Tweet Stream and how often to compute them.
// MAGIC * Run this cell for the input cells to appear.
// MAGIC * Enter your credentials.
// MAGIC * Run the cell again to pick up your defaults.

// COMMAND ----------

60*60*24*7*1 // (a * b* c* d * e); where a = 60 mini-seconds; b = 60 secods; c = number of hours within a day (range(1,24)); d = numnber of days (range(1,7)); e = number of weeks (range(1,@));

// COMMAND ----------

val outputDirectory = getArgument("1. Output Directory", "/twitterNew2")
val slideInterval = new Duration(getArgument("2. Recompute the top hashtags every N seconds", "1").toInt * 1000)
val windowLength = new Duration(getArgument("3. Compute the top hashtags for the last N seconds", "5").toInt * 1000) // not used now
val timeoutJobLength = getArgument("4. Wait this many seconds before stopping the streaming job", "100").toInt * 1000

// COMMAND ----------

// Replace with your AWS S3 credentials
//
// NOTE: Set the access to this notebook appropriately to protect the security of your keys.
// Or you can delete this cell after you run the mount command below once successfully.

val AccessKey = getArgument("1. ACCESS_KEY", "REPLACE_WITH_YOUR_ACCESS_KEY")
val SecretKey = getArgument("2. SECRET_KEY", "REPLACE_WITH_YOUR_SECRET_KEY")
val EncodedSecretKey = SecretKey.replace("/", "%2F")
val AwsBucketName = getArgument("3. S3_BUCKET", "REPLACE_WITH_YOUR_S3_BUCKET")
val MountName = getArgument("4. MNT_NAME", "REPLACE_WITH_YOUR_MOUNT_NAME")
val s3Filename = "tweetDump"

// COMMAND ----------

dbutils.fs.unmount(s"/mnt/$MountName") // finally unmount when done

// COMMAND ----------

dbutils.fs.mount(s"s3a://$AccessKey:$EncodedSecretKey@$AwsBucketName", s"/mnt/$MountName")

// COMMAND ----------

// MAGIC %md **A directory can be created to save the Tweet Stream**

// COMMAND ----------

//dbutils.fs.mkdirs(s"/mnt/$MountName/twitterNew2/")

// COMMAND ----------

//dbutils.fs.rm("/mnt/$MountName/NAME_OF_DIRECTORY/",recurse=true) //Remove the directory if previously created and no longer required for further use. 

// COMMAND ----------

display(dbutils.fs.ls(s"/mnt/s3Data/twitterNew2"))

// COMMAND ----------

// MAGIC %md 
// MAGIC Create the function to set-up the Streaming Context and the streaming job.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Step 3: Run the Twitter Streaming job.

// COMMAND ----------

import com.google.gson.Gson // the Library has already been attached to this cluster (show live how to do this from scratch?)

var newContextCreated = false
var num = 0
var numTweetsCollected = 0L // track number of tweets collected

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
  
  val twitterStreamJson = twitterStream.map(x => { val gson = new Gson();
                                                 val xJson = gson.toJson(x)
                                                 xJson
                                               }) 
  
val partitionsEachInterval = 1 // This tells the number of partitions in each RDD of tweets in the DStream.

twitterStreamJson.foreachRDD((rdd, time) => { // for each RDD in the DStream
      val count = rdd.count()
      if (count > 0) {
        val outputRDD = rdd.repartition(partitionsEachInterval) // repartition as desired
        //outputRDD.saveAsTextFile(s"${outputDirectory}/tweets_" + time.milliseconds.toString) // save as textfile
        outputRDD.saveAsTextFile(s"/mnt/$MountName/${outputDirectory}" + "/tweets_" + time.milliseconds.toString+".txt") // save as textfile in s3
        numTweetsCollected += count // update with the latest count
      }
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

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Stop Streaming and/or Terminate the Spark Streaming Context (=true) and return when the Streaming job exits or return with the specified timeout.  

// COMMAND ----------

//ssc.start()
//ssc.awaitTerminationOrTimeout(timeoutJobLength)
ssc.stop(stopSparkContext = false)

// COMMAND ----------

// MAGIC %md
// MAGIC Check out the Clusters 'Streaming` UI as the job is running.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Stop any active Streaming Contexts, but don't stop the spark contexts they are attached to.

// COMMAND ----------

StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) }

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Step 4: View the Results.

// COMMAND ----------

display(dbutils.fs.ls(s"/mnt/$MountName/twitterNew/"))

// COMMAND ----------

// MAGIC %md Read each RDD in the DStream as Text File (Transform and )
// MAGIC 
// MAGIC   *Get the file name and edit

// COMMAND ----------

val rdd1 = sc.textFile(s"/mnt/s3Data/twitterNew2/tweets_1463704200000.txt/")

// COMMAND ----------

rdd1.count

// COMMAND ----------

rdd1.take(1)

// COMMAND ----------

display(dbutils.fs.ls(s"/mnt/$MountName/${outputDirectory}"))

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5. Read all the RDD as a Whole Text File

// COMMAND ----------

//val dStream = sc.wholeTextFiles(s"/mnt/$MountName/${outputDirectory}")
val dStream = sc.textFile(s"/mnt/s3Data/twitterNew2/*.txt/")

// COMMAND ----------

dStream.take(1)

// COMMAND ----------

dStream.count //This returns the number of events or tweets in all the RDD stream

// COMMAND ----------

// MAGIC %md A better way of Merging the Files

// COMMAND ----------

val dStreamw = sc.wholeTextFiles(s"/mnt/s3Data/twitterNew2/*.txt/")

// COMMAND ----------

val dStreamTitle = dStreamw.map(rdd => rdd._1).collect

// COMMAND ----------

val dStreamContent = dStreamw.map(rdd => rdd._2)
dStreamContent.cache

// COMMAND ----------

dStreamContent.take(1)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)