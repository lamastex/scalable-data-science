// Databricks notebook source exported at Sat, 25 Jun 2016 00:08:01 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)






# Generic Twitter Collector


Remeber that the use of twitter itself comes with various strings attached. 

- **Read:** [Twitter Rules](https://twitter.com/rules)


Crucially, the use of the content from twitter by you (as done in this worksheet) comes with some strings.
- **Read:** [Developer Agreement & Policy Twitter Developer Agreement](https://dev.twitter.com/overview/terms/agreement-and-policy)


The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week6/12_SparkStreaming/022_TweetGenericCollector.html) of this databricks notebook and the recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji") of a closely related notebook:

[![sds/uji/week6/12_SparkStreaming/022_TweetCollector](http://img.youtube.com/vi/jqLcr2eS-Vs/0.jpg)](https://www.youtube.com/v/jqLcr2eS-Vs?rel=0&autoplay=1&modestbranding=1&start=2112&end=3535)

This notebook for collecting tweets is more robust than the notebook 022_TweetCollector.


```scala

import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter.TwitterUtils

import scala.math.Ordering

import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

```




### Step 1: Enter your Twitter API Credentials.
* Go to https://apps.twitter.com and look up your Twitter API Credentials, or create an app to create them.
* Run this cell for the input cells to appear.
* Enter your credentials.
* Run the cell again to pick up your defaults.

The cell-below is hidden to not expose the Twitter API Credentials: `consumerKey`, `consumerSecret`, `accessToken` and `accessTokenSecret`.


```scala

System.setProperty("twitter4j.oauth.consumerKey", getArgument("1. Consumer Key (API Key)", ""))
System.setProperty("twitter4j.oauth.consumerSecret", getArgument("2. Consumer Secret (API Secret)", ""))
System.setProperty("twitter4j.oauth.accessToken", getArgument("3. Access Token", ""))
System.setProperty("twitter4j.oauth.accessTokenSecret", getArgument("4. Access Token Secret", ""))

```



If you see warnings then ignore for now:
[https://forums.databricks.com/questions/6941/change-in-getargument-for-notebook-input.html](https://forums.databricks.com/questions/6941/change-in-getargument-for-notebook-input.html).






### Step 2: Configure how long to collect and how often to write tweets to a file in s3 or dbfs
* Run this cell for the input cells to appear.
* Enter your credentials.
* Run the cell again to pick up your defaults.


```scala

60*60*24 // seconds in one day

```
```scala

val outputDirectory = getArgument("1. Output Directory", "/myTwitterDir")
val slideInterval = new Duration(getArgument("2. Save to file every N seconds", "300").toInt * 1000) // 5 minutes by default
val timeoutJobLength = getArgument("4. Wait this many seconds before stopping the streaming job", "86400").toInt * 1000 // default is 1 day

```
```scala

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

```
```scala

dbutils.fs.unmount(s"/mnt/$MountName") // finally unmount when done

```
```scala

dbutils.fs.mount(s"s3a://$AccessKey:$EncodedSecretKey@$AwsBucketName", s"/mnt/$MountName")

```
```scala

//dbutils.fs.rm("/mnt/s3Data/twitterNew/",recurse=true)

```
```scala

display(dbutils.fs.ls("/mnt/"))

```




### Step 3: Run the Twitter Streaming job.





Clean up any old files.


```scala

//dbutils.fs.rm(outputDirectory, true) // do only if you want to remove

```
```scala

//dbutils.fs.rm("/mnt/s3Data/twitterNew/",recurse=true)

```


 
Create the function to that creates the Streaming Context and sets up the streaming job.


```scala

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
        outputRDD.saveAsTextFile(s"/mnt/$MountName/${outputDirectory}" + "/tweets_" + time.milliseconds.toString) // save as textfile in s3
        numTweetsCollected += count // update with the latest count
      }
  })
  
  newContextCreated = true
  ssc
}

```


 
Create the StreamingContext using getActiveOrCreate, as required when starting a streaming job in Databricks.


```scala

val ssc = StreamingContext.getActiveOrCreate(creatingFunc)

```




Start the Spark Streaming Context and return when the Streaming job exits or return with the specified timeout.  


```scala

//ssc.start()

```
```scala

ssc.start()
ssc.awaitTerminationOrTimeout(timeoutJobLength)
ssc.stop(stopSparkContext = false)

```



Check out the Clusters 'Streaming` UI as the job is running.






Stop any active Streaming Contexts, but don't stop the spark contexts they are attached to.


```scala

StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) }

```




### Step 4: View the Results.


```scala

display(dbutils.fs.ls("/mnt/s3Data/twitterNew/"))

```
```scala

val rdd1 = sc.textFile("/mnt/s3Data/twitterNew/tweets_1462008300000/part-00000")

```
```scala

rdd1.count

```
```scala

//rdd1.take(1) // uncomment to see first entry

```
```scala

display(dbutils.fs.ls(outputDirectory))

```
```scala

display(dbutils.fs.ls(s"${outputDirectory}/tweets_1461918420000/"))

```
```scala

val rdd1 = sc.textFile(s"${outputDirectory}/tweets_1461918420000/part-00000")

```
```scala

rdd1.count

```
```scala

// rdd1.top(1) // uncomment to see top first entry

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
