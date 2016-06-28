// Databricks notebook source exported at Sun, 26 Jun 2016 01:40:50 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/041_TA01_02_Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/041_TA01_02_Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=0&end=1611)





#Tweet Analytics

[Presentation contents](https://github.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016).






## Filtered Generic Twitter Collector by Keywords and Hashtags


Remeber that the use of twitter itself comes with various strings attached. 
- **Read:** [Twitter Rules](https://twitter.com/rules)


Crucially, the use of the content from twitter by you (as done in this worksheet) comes with some strings.
- **Read:** [Developer Agreement & Policy Twitter Developer Agreement](https://dev.twitter.com/overview/terms/agreement-and-policy)


```scala

import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter.TwitterUtils

import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

import com.google.gson.Gson

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






### Step 2: Configure where to output each filtered Batches of Tweet Stream and how often to compute them.
* Run this cell for the input cells to appear.
* Enter your credentials.
* Run the cell again to pick up your defaults.


```scala

val outputDirectory = getArgument("1. Output Directory", "twitterNew3")
val batchInterval = getArgument("2. Interval for each DStream in Minutes ", "1").toInt
val timeoutJobLength = getArgument("3. Max Time to fetch all batches of Dstream", "100").toInt * 1000

```
```scala

// Replace with your AWS S3 credentials
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

dbutils.fs.mount(s"s3a://$AccessKey:$EncodedSecretKey@$AwsBucketName", s"/mnt/$MountName") // mount if unmounted

```


 **A directory can be created to save the Tweet Stream**


```scala

dbutils.fs.mkdirs(s"/mnt/$MountName/twitterNew3/")

```
```scala

//dbutils.fs.rm("/mnt/$MountName/NAME_OF_DIRECTORY/",recurse=true) //Remove the directory if previously created and no longer required for further use. 

```
```scala

display(dbutils.fs.ls(s"/mnt/s3Data/twitterNew3"))

```




### Step 3: Run the Twitter Streaming job.




 
Create the function to set-up the Streaming Context and the streaming job.





###Keyword(s) and Hashtag(s) Tracking Set-up
* Create a list of keyword(s) or hashtag(s) to track from Twitter
* Twitter4j.Status returns ONLY tweets that contain any of the keyword(s) or hashtag(s) either in lower or upper case
* For example: We created a list of keywords and hashtags to track tweets about the US presumptive Republican Presidential Candidate, Donald J. Trump
* Tips: Search for popular hashtags on a specific topic on RiteTag.com
[![](http://www.wonderoftech.com/wp-content/uploads/2014/07/RiteTag-Logo.jpg)](https://ritetag.com/hashtag-search/Donald%20trump?green=0)





*** Read the Infographic on how to use twitter to increase the number of followers. This is relevant to those interested in marketing via social media ***


```scala

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
displayHTML(frameIt("http://www.wonderoftech.com/best-twitter-tips-followers/", 600))

```
```scala

 // the Library has already been attached to this cluster (show live how to do this from scratch?)

var newContextCreated = false
var numTweetsCollected = 0L // track number of tweets collected
//val conf = new SparkConf().setAppName("TrackedTweetCollector").setMaster("local")
// This is the function that creates the SteamingContext and sets up the Spark Streaming job.
def creatingFunc(): StreamingContext = {
  // Create a Spark Streaming Context.
  val ssc = new StreamingContext(sc, Minutes(batchInterval))
  // Create a Twitter Stream for the input source. 
  val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  
  val track = List("Trump2016", "#MakeAmericaGreatAgain", "Donald Trump","#lovetrumpshate")
  //List(“Hillary Clinton”, “#neverhillary”, “#hillaryclinton”, “#demthrones”)
  val twitterStream = TwitterUtils.createStream(ssc, auth, track)
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
        outputRDD.saveAsTextFile(s"/mnt/$MountName/${outputDirectory}" + "/tweets_" + time.milliseconds.toString+".txt") // save as textfile in s3
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

ssc.start()

```




Stop Streaming and/or Terminate the Spark Streaming Context (=true) and return when the Streaming job exits or return with the specified timeout.  


```scala

ssc.start()
ssc.awaitTerminationOrTimeout(timeoutJobLength)
//ssc.stop(stopSparkContext = false)

```



Check out the Clusters 'Streaming` UI as the job is running.






Stop any active Streaming Contexts, but don't stop the spark contexts they are attached to.






### Step 4: View the Results.


```scala

display(dbutils.fs.ls("/mnt/s3Data/twitterNew2/"))

```


 
Read each RDD in the DStream as Text File

  * Get the file name from the above `display` and edit the input string to `textFile` below.


```scala

val rdd1 = sc.textFile(s"/mnt/s3Data/twitterNew2/tweets_1463704200000.txt/")

```
```scala

rdd1.count

```
```scala

rdd1.take(1)

```
```scala

display(dbutils.fs.ls(s"/mnt/$MountName/${outputDirectory}"))

```



### 5. Read all the RDD as a Whole Text File


```scala

//val dStream = sc.wholeTextFiles(s"/mnt/$MountName/${outputDirectory}")
val dStream = sc.textFile(s"/mnt/s3Data/twitterNew2/*.txt/")

```
```scala

dStream.take(1)

```
```scala

dStream.count //This returns the number of events or tweets in all the RDD stream

```


 A better way of Merging the Files


```scala

val dStreamw = sc.wholeTextFiles(s"/mnt/s3Data/twitterNew2/*.txt/")

```
```scala

val dStreamTitle = dStreamw.map(rdd => rdd._1).collect

```
```scala

val dStreamContent = dStreamw.map(rdd => rdd._2)
dStreamContent.cache

```
```scala

dStreamContent.take(1)

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
