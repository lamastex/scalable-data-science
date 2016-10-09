// Databricks notebook source exported at Wed, 5 Oct 2016 06:39:45 UTC
// MAGIC %md
// MAGIC # Extended spark.streaming.twitter.TwitterUtils
// MAGIC 
// MAGIC ### 2016, Ivan Sadikov and Raazesh Sainudiin
// MAGIC 
// MAGIC We extend twitter utils from Spark to allow for filtering by user-ids using `.follow` and strings in the tweet using `.track` method of `twitter4j`.
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program.
// MAGIC 
// MAGIC The analysis is available in the following databricks notebook:
// MAGIC * [http://lamastex.org/lmse/mep/src/extendedTwitterUtils.html](http://lamastex.org/lmse/mep/src/extendedTwitterUtil.html)
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016 Ivan Sadikov and Raazesh Sainudiin
// MAGIC 
// MAGIC Licensed under the Apache License, Version 2.0 (the "License");
// MAGIC you may not use this file except in compliance with the License.
// MAGIC You may obtain a copy of the License at
// MAGIC 
// MAGIC     http://www.apache.org/licenses/LICENSE-2.0
// MAGIC 
// MAGIC Unless required by applicable law or agreed to in writing, software
// MAGIC distributed under the License is distributed on an "AS IS" BASIS,
// MAGIC WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// MAGIC See the License for the specific language governing permissions and
// MAGIC limitations under the License.
// MAGIC ```

// COMMAND ----------

import twitter4j._
import twitter4j.auth.Authorization
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization

import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

// COMMAND ----------

// MAGIC %md
// MAGIC ### Twitter receiver and stream

// COMMAND ----------


class ExtendedTwitterReceiver(
    twitterAuth: Authorization,
    filters: Seq[String],
    userFilters: Seq[Long],
    storageLevel: StorageLevel
  ) extends Receiver[Status](storageLevel) {

  @volatile private var twitterStream: TwitterStream = _
  @volatile private var stopped = false

  def onStart() {
    try {
      val newTwitterStream = new TwitterStreamFactory().getInstance(twitterAuth)
      newTwitterStream.addListener(new StatusListener {
        def onStatus(status: Status): Unit = {
          store(status)
        }
        // Unimplemented
        def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
        def onTrackLimitationNotice(i: Int) {}
        def onScrubGeo(l: Long, l1: Long) {}
        def onStallWarning(stallWarning: StallWarning) {}
        def onException(e: Exception) {
          if (!stopped) {
            restart("Error receiving tweets", e)
          }
        }
      })

      // do filtering only when filters are available
      if (filters.nonEmpty || userFilters.nonEmpty) {
        val query = new FilterQuery()
        if (filters.nonEmpty) {
          query.track(filters.mkString(","))
        }

        if (userFilters.nonEmpty) {
          query.follow(userFilters: _*)
        }
        
        newTwitterStream.filter(query)
      } else {
        newTwitterStream.sample()
      }
      setTwitterStream(newTwitterStream)
      println("Twitter receiver started")
      stopped = false
    } catch {
      case e: Exception => restart("Error starting Twitter stream", e)
    }
  }

  def onStop() {
    stopped = true
    setTwitterStream(null)
    println("Twitter receiver stopped")
  }

  private def setTwitterStream(newTwitterStream: TwitterStream) = synchronized {
    if (twitterStream != null) {
      twitterStream.shutdown()
    }
    twitterStream = newTwitterStream
  }
}

// COMMAND ----------

class ExtendedTwitterInputDStream(
    ssc_ : StreamingContext,
    twitterAuth: Option[Authorization],
    filters: Seq[String],
    userFilters: Seq[Long],
    storageLevel: StorageLevel
  ) extends ReceiverInputDStream[Status](ssc_)  {

  private def createOAuthAuthorization(): Authorization = {
    new OAuthAuthorization(new ConfigurationBuilder().build())
  }

  private val authorization = twitterAuth.getOrElse(createOAuthAuthorization())

  override def getReceiver(): Receiver[Status] = {
    new ExtendedTwitterReceiver(authorization, filters, userFilters, storageLevel)
  }
}

// COMMAND ----------

// MAGIC %md
// MAGIC ### Extended twitter utils

// COMMAND ----------

import twitter4j.Status
import twitter4j.auth.Authorization
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.{ReceiverInputDStream, DStream}

object ExtendedTwitterUtils {
  def createStream(
      ssc: StreamingContext,
      twitterAuth: Option[Authorization],
      filters: Seq[String] = Nil,
      userFilters: Seq[Long] = Nil,
      storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2
    ): ReceiverInputDStream[Status] = {
    new ExtendedTwitterInputDStream(ssc, twitterAuth, filters, userFilters, storageLevel)
  }
}

// COMMAND ----------

// MAGIC %md
// MAGIC Raaz's twitter credentials

// COMMAND ----------

import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter.TwitterUtils

import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

import com.google.gson.Gson

// COMMAND ----------

//put your own twitter credentials
val MyconsumerKey       = "..."
val MyconsumerSecret    = "..."
val Mytoken             = "..."
val MytokenSecret       = "..."

System.setProperty("twitter4j.oauth.consumerKey", MyconsumerKey)
System.setProperty("twitter4j.oauth.consumerSecret", MyconsumerSecret)
System.setProperty("twitter4j.oauth.accessToken", Mytoken)
System.setProperty("twitter4j.oauth.accessTokenSecret", MytokenSecret)

val outputDirectoryRoot = "dbfs:/datasets/MEP/AkinTweet/sampleTweets@raazozoneByuserId" // output directory
val batchInterval = 1 // in minutes
val timeoutJobLength =  batchInterval * 5

// COMMAND ----------

 // the Library has already been attached to this cluster (show live how to do this from scratch?)

var newContextCreated = false
var numTweetsCollected = 0L // track number of tweets collected
//val conf = new SparkConf().setAppName("TrackedTweetCollector").setMaster("local")
// This is the function that creates the SteamingContext and sets up the Spark Streaming job.
def streamFunc(): StreamingContext = {
  // Create a Spark Streaming Context.
  val ssc = new StreamingContext(sc, Minutes(batchInterval))
  // Create a Twitter Stream for the input source. 
  val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  
  val track = List("@raazozone")//,"Trump2016", "#MakeAmericaGreatAgain", "Donald Trump","#lovetrumpshate") 
  val follow = List(4173723312L)
  val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth, track, follow)
  val twitterStreamJson = twitterStream.map(x => { val gson = new Gson();
                                                 val xJson = gson.toJson(x)
                                                 xJson
                                               }) 
 
val partitionsEachInterval = 1 // This tells the number of partitions in each RDD of tweets in the DStream.

twitterStreamJson.foreachRDD((rdd, time) => { // for each filtered RDD in the DStream
      val count = rdd.count()
      if (count > 0) {
        val outputRDD = rdd.repartition(partitionsEachInterval) // repartition as desired
        //outputRDD.saveAsTextFile(s"${outputDirectory}/tweets_" + time.milliseconds.toString) // save as textfile
        outputRDD.saveAsTextFile(outputDirectoryRoot + "/tweets_" + time.milliseconds.toString) // save as textfile in s3
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

StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } // this will make sure all streaming job in the cluster are stopped - raaz

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/MEP/AkinTweet/sampleTweets@raazozoneByuserId"))

// COMMAND ----------

val a = sqlContext.read.json("dbfs:/datasets/MEP/AkinTweet/sampleTweets@raazozoneByuserId/*")

// COMMAND ----------

display(a.select($"id".as("TweetId"),$"retweetedStatus.id".as("IdOfTweetBeingRT"),$"inReplyToUserId",$"inReplyToStatusId",$"user.name".as("StatusAuthor"),$"retweetedStatus.user.name".as("NameOfAuthorWhoseTweetIsRT"),$"retweetedStatus.userMentionEntities.name".as("ScreenNameQuotedInRT"),$"user.id".as("UserId"),$"retweetedStatus.user.id".as("UserIdOfAuthorWhoseTweetIsRT"),$"retweetedStatus.userMentionEntities.id".as("IdOfScreenNameQuotedInRT"),$"quotedStatusId",$"text".as("currentTweet"), $"quotedStatus.text", $"quotedStatus.id", $"quotedStatus.user.id"))

// COMMAND ----------

