Tweet Collector - capture live tweets
=====================================

Here are the main steps in this notebook:

1.  let's collect from the public twitter stream and write to DBFS as json strings in a boiler-plate manner to understand the componets better.

-   Then we will turn the collector into a function and use it
-   Finally we will use some DataFrame-based pipelines to convert the raw tweets into other structured content.

<!-- -->

    // Now just use the function to create a Spark Streaming Context
    val ssc = StreamingContext.getActiveOrCreate(streamFunc)

> ssc: org.apache.spark.streaming.StreamingContext = org.apache.spark.streaming.StreamingContext@3c75550d

    // you only need one of these to start
    ssc.start()
    //ssc.awaitTerminationOrTimeout(timeoutJobLength)

    // this will make sure all streaming job in the cluster are stopped
    // but let' run it for a few minutes before stopping it
    StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 

    display(dbutils.fs.ls(outputDirectoryRoot))

| dbfs:/datasets/tweetsStreamTmp/2017/ | 2017/ | 0.0 |
|--------------------------------------|-------|-----|

    val rawDF = fromParquetFile2DF("/datasets/tweetsStreamTmp/2017/10/*/*/*/*") //.cache()
    val TTTsDF = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(rawDF)).cache()

> rawDF: org.apache.spark.sql.DataFrame = \[tweetAsJsonString: string\] TTTsDF: org.apache.spark.sql.Dataset\[org.apache.spark.sql.Row\] = \[CurrentTweetDate: timestamp, CurrentTwID: bigint ... 32 more fields\]

    // this will delete what we collected to keep the disk usage tight and tidy
    dbutils.fs.rm(outputDirectoryRoot, true) 

> res67: Boolean = true

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
      // Create a Twitter Stream for the input source.  
      val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth)
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

> import com.google.gson.Gson import org.apache.spark.sql.functions.\_ import org.apache.spark.sql.types.\_ outputDirectoryRoot: String = /datasets/tweetsStreamTmp batchInterval: Int = 1 timeoutJobLength: Int = 5 newContextCreated: Boolean = false numTweetsCollected: Long = 0 streamFunc: ()org.apache.spark.streaming.StreamingContext

Go to SparkUI and see if a streaming job is already running. If so you need to terminate it before starting a new streaming job. Only one streaming job can be run on the DB CE.

    // this will make sure all streaming job in the cluster are stopped
    StreamingContext.getActive.foreach{ _.stop(stopSparkContext = false) }

We will call extendedTwitterUtils notebook from here.

But **first install** the following libraries: \* gson \* twitter4j-examples

### Making a function for Spark Streaming job

Let's try to throw the bits and bobs of code above into a function called `streamFunc` for simplicity and modularity.

    // this will make sure all streaming job in the cluster are stopped
    StreamingContext.getActive.foreach{ _.stop(stopSparkContext = false) } 

    display(TTTsDF.groupBy($"tweetType").count().orderBy($"count".desc))

| ReTweet                 | 8401.0 |
|-------------------------|--------|
| Original Tweet          | 6626.0 |
| Reply Tweet             | 3472.0 |
| Retweet of Quoted Tweet | 556.0  |
| Quoted Tweet            | 439.0  |
| Reply of Quoted Tweet   | 11.0   |

    TTTsDF.count()

> res63: Long = 19505

    display(TTTsDF)

    // to remove a pre-existing directory and start from scratch uncomment next line and evaluate this cell
    dbutils.fs.rm(outputDirectoryRoot, true) 

> res40: Boolean = true

    // Create a Twitter Stream for the input source. 
    val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
    val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth)

> auth: Some\[twitter4j.auth.OAuthAuthorization\] = Some(OAuthAuthorization{consumerKey='fo0EEh1tnH8WVJdgJPrZ47wD0', consumerSecret='\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*', oauthToken=AccessToken{screenName='null', userId=4173723312}}) twitterStream: org.apache.spark.streaming.dstream.ReceiverInputDStream\[twitter4j.Status\] = ExtendedTwitterInputDStream@571b3fd4

    // Create a Spark Streaming Context.
    val ssc = new StreamingContext(sc, slideInterval)

> ssc: org.apache.spark.streaming.StreamingContext = org.apache.spark.streaming.StreamingContext@ced654d

    val slideInterval = new Duration(1 * 1000) // 1 * 1000 = 1000 milli-seconds = 1 sec

> slideInterval: org.apache.spark.streaming.Duration = 1000 ms

Capture tweets in every sliding window of `slideInterval` many milliseconds.

Let's create a directory in dbfs for storing tweets in the cluster's distributed file system.

    val outputDirectoryRoot = "/datasets/tweetsStreamTmp" // output directory

> outputDirectoryRoot: String = /datasets/tweetsStreamTmp

Recall that **Discretized Stream** or **DStream** is the basic abstraction provided by Spark Streaming. It represents a continuous stream of data, either the input data stream received from source, or the processed data stream generated by transforming the input stream. Internally, a DStream is represented by a continuous series of RDDs, which is Spark?s abstraction of an immutable, distributed dataset (see [Spark Programming Guide](http://spark.apache.org/docs/latest/programming-guide.html#resilient-distributed-datasets-rdds) for more details). Each RDD in a DStream contains data from a certain interval, as shown in the following figure.

![Spark Streaming](http://spark.apache.org/docs/latest/img/streaming-dstream.png "Spark Streaming data flow")

Let's import google's json library next.

    import com.google.gson.Gson 

> import com.google.gson.Gson

Our goal is to take each RDD in the twitter DStream and write it as a json file in our dbfs.

CAUTION
-------

Extracting knowledge from tweets is "easy" using techniques shown here, but one has to take legal responsibility for the use of this knowledge and conform to the rules and policies linked below.

Remeber that the use of twitter itself comes with various strings attached. Read:

-   [Twitter Rules](https://twitter.com/rules)

Crucially, the use of the content from twitter by you (as done in this worksheet) comes with some strings. Read: - [Developer Agreement & Policy Twitter Developer Agreement](https://dev.twitter.com/overview/terms/agreement-and-policy)

### Enter your own Twitter API Credentials.

-   Go to https://apps.twitter.com and look up your Twitter API Credentials, or create an app to create them.
-   Get your own Twitter API Credentials: `consumerKey`, `consumerSecret`, `accessToken` and `accessTokenSecret` and enter them in the cell below.

### Ethical/Legal Aspects

See Background Readings/Viewings in Project MEP: \* <https://lamastex.github.io/scalable-data-science/sds/research/mep/>

    // put your own twitter developer credentials below instead of xxx
    // instead of the '%run "scalable-data-science/secrets/026_secret_MyTwitterOAuthCredentials"' above
    // this notebook we just ran contains the following commented code block

    /*
    import twitter4j.auth.OAuthAuthorization
    import twitter4j.conf.ConfigurationBuilder

    def MyconsumerKey       = "xxx"
    def MyconsumerSecret    = "xxx"
    def Mytoken             = "xxx"
    def MytokenSecret       = "xxx"

    System.setProperty("twitter4j.oauth.consumerKey", MyconsumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", MyconsumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", Mytoken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", MytokenSecret)
    */

Let's map the tweets into json formatted string (one tweet per line).

    var numTweetsCollected = 0L // track number of tweets collected
    val partitionsEachInterval = 1 // This tells the number of partitions in each RDD of tweets in the DStream.

    twitterStreamJson.foreachRDD( 
      (rdd, time) => { // for each RDD in the DStream
          val count = rdd.count()
          if (count > 0) {
            val outputRDD = rdd.repartition(partitionsEachInterval) // repartition as desired
            outputRDD.saveAsTextFile(outputDirectoryRoot + "/tweets_" + time.milliseconds.toString) // save as textfile
            numTweetsCollected += count // update with the latest count
          }
      }
    )

> numTweetsCollected: Long = 0 partitionsEachInterval: Int = 1

    val twitterStreamJson = twitterStream.map(
                                                x => { val gson = new Gson();
                                                     val xJson = gson.toJson(x)
                                                     xJson
                                                     }
                                              ) 

> twitterStreamJson: org.apache.spark.streaming.dstream.DStream\[String\] = org.apache.spark.streaming.dstream.MappedDStream@6817a6ee

Nothing has actually happened yet.

Let's start the spark streaming context we have created next.

    ssc.start()

Let's look at the spark UI now and monitor the streaming job in action! Go to `Clusters` on the left and click on `UI` and then `Streaming`.

    numTweetsCollected // number of tweets collected so far

> res48: Long = 0

Note that you could easilt fill up disk space!!!

So let's stop the streaming job next.

Let's try seeing again in a few seconds how many tweets have been collected up to now.

    numTweetsCollected // number of tweets collected so far

> res11: Long = 187

    ssc.stop(stopSparkContext = false) // gotto stop soon!!!

Let's make sure that the `Streaming` UI is not active in the `Clusters` `UI`.

    StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } // extra cautious stopping of all active streaming contexts

Let's examine what was saved in dbfs
------------------------------------

    display(dbutils.fs.ls(outputDirectoryRoot))

| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184798000/ | tweets\_1507184798000/ | 0.0 |
|-------------------------------------------------------|------------------------|-----|
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184799000/ | tweets\_1507184799000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184800000/ | tweets\_1507184800000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184801000/ | tweets\_1507184801000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184802000/ | tweets\_1507184802000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184803000/ | tweets\_1507184803000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184804000/ | tweets\_1507184804000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184805000/ | tweets\_1507184805000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184806000/ | tweets\_1507184806000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184807000/ | tweets\_1507184807000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184808000/ | tweets\_1507184808000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184809000/ | tweets\_1507184809000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184810000/ | tweets\_1507184810000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184811000/ | tweets\_1507184811000/ | 0.0 |

    display(dbutils.fs.ls(tweetsDir)) 

| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184802000/\_SUCCESS  | \_SUCCESS  | 0.0      |
|-----------------------------------------------------------------|------------|----------|
| dbfs:/datasets/tweetsStreamTmp/tweets\_1507184802000/part-00000 | part-00000 | 125602.0 |

    val tweetsDir = outputDirectoryRoot+"/tweets_1507184802000/" // use an existing file, may have to rename folder based on output above!

> tweetsDir: String = /datasets/tweetsStreamTmp/tweets\_1507184802000/

    val outJson = sqlContext.read.json(tweetsDir+"part-00000")

> outJson: org.apache.spark.sql.DataFrame = \[contributorsIDs: array&lt;string&gt;, createdAt: string ... 25 more fields\]

    sc.textFile(tweetsDir+"part-00000").count()

> res16: Long = 36

    outJson.printSchema()

> root |-- contributorsIDs: array (nullable = true) | |-- element: string (containsNull = true) |-- createdAt: string (nullable = true) |-- currentUserRetweetId: long (nullable = true) |-- displayTextRangeEnd: long (nullable = true) |-- displayTextRangeStart: long (nullable = true) |-- favoriteCount: long (nullable = true) |-- hashtagEntities: array (nullable = true) | |-- element: struct (containsNull = true) | | |-- end: long (nullable = true) | | |-- start: long (nullable = true) | | |-- text: string (nullable = true) |-- id: long (nullable = true) |-- inReplyToScreenName: string (nullable = true) |-- inReplyToStatusId: long (nullable = true) |-- inReplyToUserId: long (nullable = true) |-- isFavorited: boolean (nullable = true) |-- isPossiblySensitive: boolean (nullable = true) |-- isRetweeted: boolean (nullable = true) |-- isTruncated: boolean (nullable = true) |-- lang: string (nullable = true) |-- mediaEntities: array (nullable = true) | |-- element: struct (containsNull = true) | | |-- displayURL: string (nullable = true) | | |-- end: long (nullable = true) | | |-- expandedURL: string (nullable = true) | | |-- id: long (nullable = true) | | |-- mediaURL: string (nullable = true) | | |-- mediaURLHttps: string (nullable = true) | | |-- sizes: struct (nullable = true) | | | |-- 0: struct (nullable = true) | | | | |-- height: long (nullable = true) | | | | |-- resize: long (nullable = true) | | | | |-- width: long (nullable = true) | | | |-- 1: struct (nullable = true) | | | | |-- height: long (nullable = true) | | | | |-- resize: long (nullable = true) | | | | |-- width: long (nullable = true) | | | |-- 2: struct (nullable = true) | | | | |-- height: long (nullable = true) | | | | |-- resize: long (nullable = true) | | | | |-- width: long (nullable = true) | | | |-- 3: struct (nullable = true) | | | | |-- height: long (nullable = true) | | | | |-- resize: long (nullable = true) | | | | |-- width: long (nullable = true) | | |-- start: long (nullable = true) | | |-- type: string (nullable = true) | | |-- url: string (nullable = true) | | |-- videoAspectRatioHeight: long (nullable = true) | | |-- videoAspectRatioWidth: long (nullable = true) | | |-- videoDurationMillis: long (nullable = true) | | |-- videoVariants: array (nullable = true) | | | |-- element: struct (containsNull = true) | | | | |-- bitrate: long (nullable = true) | | | | |-- contentType: string (nullable = true) | | | | |-- url: string (nullable = true) |-- quotedStatus: struct (nullable = true) | |-- contributorsIDs: array (nullable = true) | | |-- element: string (containsNull = true) | |-- createdAt: string (nullable = true) | |-- currentUserRetweetId: long (nullable = true) | |-- displayTextRangeEnd: long (nullable = true) | |-- displayTextRangeStart: long (nullable = true) | |-- favoriteCount: long (nullable = true) | |-- hashtagEntities: array (nullable = true) | | |-- element: struct (containsNull = true) | | | |-- end: long (nullable = true) | | | |-- start: long (nullable = true) | | | |-- text: string (nullable = true) | |-- id: long (nullable = true) | |-- inReplyToScreenName: string (nullable = true) | |-- inReplyToStatusId: long (nullable = true) | |-- inReplyToUserId: long (nullable = true) | |-- isFavorited: boolean (nullable = true) | |-- isPossiblySensitive: boolean (nullable = true) | |-- isRetweeted: boolean (nullable = true) | |-- isTruncated: boolean (nullable = true) | |-- lang: string (nullable = true) | |-- mediaEntities: array (nullable = true) | | |-- element: string (containsNull = true) | |-- quotedStatusId: long (nullable = true) | |-- retweetCount: long (nullable = true) | |-- source: string (nullable = true) | |-- symbolEntities: array (nullable = true) | | |-- element: string (containsNull = true) | |-- text: string (nullable = true) | |-- urlEntities: array (nullable = true) | | |-- element: struct (containsNull = true) | | | |-- displayURL: string (nullable = true) | | | |-- end: long (nullable = true) | | | |-- expandedURL: string (nullable = true) | | | |-- start: long (nullable = true) | | | |-- url: string (nullable = true) | |-- user: struct (nullable = true) | | |-- createdAt: string (nullable = true) | | |-- description: string (nullable = true) | | |-- descriptionURLEntities: array (nullable = true) | | | |-- element: string (containsNull = true) | | |-- favouritesCount: long (nullable = true) | | |-- followersCount: long (nullable = true) | | |-- friendsCount: long (nullable = true) | | |-- id: long (nullable = true) | | |-- isContributorsEnabled: boolean (nullable = true) | | |-- isDefaultProfile: boolean (nullable = true) | | |-- isDefaultProfileImage: boolean (nullable = true) | | |-- isFollowRequestSent: boolean (nullable = true) | | |-- isGeoEnabled: boolean (nullable = true) | | |-- isProtected: boolean (nullable = true) | | |-- isVerified: boolean (nullable = true) | | |-- lang: string (nullable = true) | | |-- listedCount: long (nullable = true) | | |-- location: string (nullable = true) | | |-- name: string (nullable = true) | | |-- profileBackgroundColor: string (nullable = true) | | |-- profileBackgroundImageUrl: string (nullable = true) | | |-- profileBackgroundImageUrlHttps: string (nullable = true) | | |-- profileBackgroundTiled: boolean (nullable = true) | | |-- profileBannerImageUrl: string (nullable = true) | | |-- profileImageUrl: string (nullable = true) | | |-- profileImageUrlHttps: string (nullable = true) | | |-- profileLinkColor: string (nullable = true) | | |-- profileSidebarBorderColor: string (nullable = true) | | |-- profileSidebarFillColor: string (nullable = true) | | |-- profileTextColor: string (nullable = true) | | |-- profileUseBackgroundImage: boolean (nullable = true) | | |-- screenName: string (nullable = true) | | |-- showAllInlineMedia: boolean (nullable = true) | | |-- statusesCount: long (nullable = true) | | |-- timeZone: string (nullable = true) | | |-- translator: boolean (nullable = true) | | |-- url: string (nullable = true) | | |-- utcOffset: long (nullable = true) | |-- userMentionEntities: array (nullable = true) | | |-- element: struct (containsNull = true) | | | |-- end: long (nullable = true) | | | |-- id: long (nullable = true) | | | |-- name: string (nullable = true) | | | |-- screenName: string (nullable = true) | | | |-- start: long (nullable = true) |-- quotedStatusId: long (nullable = true) |-- retweetCount: long (nullable = true) |-- retweetedStatus: struct (nullable = true) | |-- contributorsIDs: array (nullable = true) | | |-- element: string (containsNull = true) | |-- createdAt: string (nullable = true) | |-- currentUserRetweetId: long (nullable = true) | |-- displayTextRangeEnd: long (nullable = true) | |-- displayTextRangeStart: long (nullable = true) | |-- favoriteCount: long (nullable = true) | |-- hashtagEntities: array (nullable = true) | | |-- element: struct (containsNull = true) | | | |-- end: long (nullable = true) | | | |-- start: long (nullable = true) | | | |-- text: string (nullable = true) | |-- id: long (nullable = true) | |-- inReplyToScreenName: string (nullable = true) | |-- inReplyToStatusId: long (nullable = true) | |-- inReplyToUserId: long (nullable = true) | |-- isFavorited: boolean (nullable = true) | |-- isPossiblySensitive: boolean (nullable = true) | |-- isRetweeted: boolean (nullable = true) | |-- isTruncated: boolean (nullable = true) | |-- lang: string (nullable = true) | |-- mediaEntities: array (nullable = true) | | |-- element: struct (containsNull = true) | | | |-- displayURL: string (nullable = true) | | | |-- end: long (nullable = true) | | | |-- expandedURL: string (nullable = true) | | | |-- id: long (nullable = true) | | | |-- mediaURL: string (nullable = true) | | | |-- mediaURLHttps: string (nullable = true) | | | |-- sizes: struct (nullable = true) | | | | |-- 0: struct (nullable = true) | | | | | |-- height: long (nullable = true) | | | | | |-- resize: long (nullable = true) | | | | | |-- width: long (nullable = true) | | | | |-- 1: struct (nullable = true) | | | | | |-- height: long (nullable = true) | | | | | |-- resize: long (nullable = true) | | | | | |-- width: long (nullable = true) | | | | |-- 2: struct (nullable = true) | | | | | |-- height: long (nullable = true) | | | | | |-- resize: long (nullable = true) | | | | | |-- width: long (nullable = true) | | | | |-- 3: struct (nullable = true) | | | | | |-- height: long (nullable = true) | | | | | |-- resize: long (nullable = true) | | | | | |-- width: long (nullable = true) | | | |-- start: long (nullable = true) | | | |-- type: string (nullable = true) | | | |-- url: string (nullable = true) | | | |-- videoAspectRatioHeight: long (nullable = true) | | | |-- videoAspectRatioWidth: long (nullable = true) | | | |-- videoDurationMillis: long (nullable = true) | | | |-- videoVariants: array (nullable = true) | | | | |-- element: struct (containsNull = true) | | | | | |-- bitrate: long (nullable = true) | | | | | |-- contentType: string (nullable = true) | | | | | |-- url: string (nullable = true) | |-- place: struct (nullable = true) | | |-- boundingBoxCoordinates: array (nullable = true) | | | |-- element: array (containsNull = true) | | | | |-- element: struct (containsNull = true) | | | | | |-- latitude: double (nullable = true) | | | | | |-- longitude: double (nullable = true) | | |-- boundingBoxType: string (nullable = true) | | |-- country: string (nullable = true) | | |-- countryCode: string (nullable = true) | | |-- fullName: string (nullable = true) | | |-- id: string (nullable = true) | | |-- name: string (nullable = true) | | |-- placeType: string (nullable = true) | | |-- url: string (nullable = true) | |-- quotedStatus: struct (nullable = true) | | |-- contributorsIDs: array (nullable = true) | | | |-- element: string (containsNull = true) | | |-- createdAt: string (nullable = true) | | |-- currentUserRetweetId: long (nullable = true) | | |-- displayTextRangeEnd: long (nullable = true) | | |-- displayTextRangeStart: long (nullable = true) | | |-- favoriteCount: long (nullable = true) | | |-- hashtagEntities: array (nullable = true) | | | |-- element: struct (containsNull = true) | | | | |-- end: long (nullable = true) | | | | |-- start: long (nullable = true) | | | | |-- text: string (nullable = true) | | |-- id: long (nullable = true) | | |-- inReplyToScreenName: string (nullable = true) | | |-- inReplyToStatusId: long (nullable = true) | | |-- inReplyToUserId: long (nullable = true) | | |-- isFavorited: boolean (nullable = true) | | |-- isPossiblySensitive: boolean (nullable = true) | | |-- isRetweeted: boolean (nullable = true) | | |-- isTruncated: boolean (nullable = true) | | |-- lang: string (nullable = true) | | |-- mediaEntities: array (nullable = true) | | | |-- element: string (containsNull = true) | | |-- quotedStatusId: long (nullable = true) | | |-- retweetCount: long (nullable = true) | | |-- source: string (nullable = true) | | |-- symbolEntities: array (nullable = true) | | | |-- element: string (containsNull = true) | | |-- text: string (nullable = true) | | |-- urlEntities: array (nullable = true) | | | |-- element: struct (containsNull = true) | | | | |-- displayURL: string (nullable = true) | | | | |-- end: long (nullable = true) | | | | |-- expandedURL: string (nullable = true) | | | | |-- start: long (nullable = true) | | | | |-- url: string (nullable = true) | | |-- user: struct (nullable = true) | | | |-- createdAt: string (nullable = true) | | | |-- description: string (nullable = true) | | | |-- descriptionURLEntities: array (nullable = true) | | | | |-- element: string (containsNull = true) | | | |-- favouritesCount: long (nullable = true) | | | |-- followersCount: long (nullable = true) | | | |-- friendsCount: long (nullable = true) | | | |-- id: long (nullable = true) | | | |-- isContributorsEnabled: boolean (nullable = true) | | | |-- isDefaultProfile: boolean (nullable = true) | | | |-- isDefaultProfileImage: boolean (nullable = true) | | | |-- isFollowRequestSent: boolean (nullable = true) | | | |-- isGeoEnabled: boolean (nullable = true) | | | |-- isProtected: boolean (nullable = true) | | | |-- isVerified: boolean (nullable = true) | | | |-- lang: string (nullable = true) | | | |-- listedCount: long (nullable = true) | | | |-- location: string (nullable = true) | | | |-- name: string (nullable = true) | | | |-- profileBackgroundColor: string (nullable = true) | | | |-- profileBackgroundImageUrl: string (nullable = true) | | | |-- profileBackgroundImageUrlHttps: string (nullable = true) | | | |-- profileBackgroundTiled: boolean (nullable = true) | | | |-- profileBannerImageUrl: string (nullable = true) | | | |-- profileImageUrl: string (nullable = true) | | | |-- profileImageUrlHttps: string (nullable = true) | | | |-- profileLinkColor: string (nullable = true) | | | |-- profileSidebarBorderColor: string (nullable = true) | | | |-- profileSidebarFillColor: string (nullable = true) | | | |-- profileTextColor: string (nullable = true) | | | |-- profileUseBackgroundImage: boolean (nullable = true) | | | |-- screenName: string (nullable = true) | | | |-- showAllInlineMedia: boolean (nullable = true) | | | |-- statusesCount: long (nullable = true) | | | |-- timeZone: string (nullable = true) | | | |-- translator: boolean (nullable = true) | | | |-- url: string (nullable = true) | | | |-- utcOffset: long (nullable = true) | | |-- userMentionEntities: array (nullable = true) | | | |-- element: struct (containsNull = true) | | | | |-- end: long (nullable = true) | | | | |-- id: long (nullable = true) | | | | |-- name: string (nullable = true) | | | | |-- screenName: string (nullable = true) | | | | |-- start: long (nullable = true) | |-- quotedStatusId: long (nullable = true) | |-- retweetCount: long (nullable = true) | |-- source: string (nullable = true) | |-- symbolEntities: array (nullable = true) | | |-- element: string (containsNull = true) | |-- text: string (nullable = true) | |-- urlEntities: array (nullable = true) | | |-- element: struct (containsNull = true) | | | |-- displayURL: string (nullable = true) | | | |-- end: long (nullable = true) | | | |-- expandedURL: string (nullable = true) | | | |-- start: long (nullable = true) | | | |-- url: string (nullable = true) | |-- user: struct (nullable = true) | | |-- createdAt: string (nullable = true) | | |-- description: string (nullable = true) | | |-- descriptionURLEntities: array (nullable = true) | | | |-- element: string (containsNull = true) | | |-- favouritesCount: long (nullable = true) | | |-- followersCount: long (nullable = true) | | |-- friendsCount: long (nullable = true) | | |-- id: long (nullable = true) | | |-- isContributorsEnabled: boolean (nullable = true) | | |-- isDefaultProfile: boolean (nullable = true) | | |-- isDefaultProfileImage: boolean (nullable = true) | | |-- isFollowRequestSent: boolean (nullable = true) | | |-- isGeoEnabled: boolean (nullable = true) | | |-- isProtected: boolean (nullable = true) | | |-- isVerified: boolean (nullable = true) | | |-- lang: string (nullable = true) | | |-- listedCount: long (nullable = true) | | |-- location: string (nullable = true) | | |-- name: string (nullable = true) | | |-- profileBackgroundColor: string (nullable = true) | | |-- profileBackgroundImageUrl: string (nullable = true) | | |-- profileBackgroundImageUrlHttps: string (nullable = true) | | |-- profileBackgroundTiled: boolean (nullable = true) | | |-- profileBannerImageUrl: string (nullable = true) | | |-- profileImageUrl: string (nullable = true) | | |-- profileImageUrlHttps: string (nullable = true) | | |-- profileLinkColor: string (nullable = true) | | |-- profileSidebarBorderColor: string (nullable = true) | | |-- profileSidebarFillColor: string (nullable = true) | | |-- profileTextColor: string (nullable = true) | | |-- profileUseBackgroundImage: boolean (nullable = true) | | |-- screenName: string (nullable = true) | | |-- showAllInlineMedia: boolean (nullable = true) | | |-- statusesCount: long (nullable = true) | | |-- timeZone: string (nullable = true) | | |-- translator: boolean (nullable = true) | | |-- url: string (nullable = true) | | |-- utcOffset: long (nullable = true) | |-- userMentionEntities: array (nullable = true) | | |-- element: struct (containsNull = true) | | | |-- end: long (nullable = true) | | | |-- id: long (nullable = true) | | | |-- name: string (nullable = true) | | | |-- screenName: string (nullable = true) | | | |-- start: long (nullable = true) |-- source: string (nullable = true) |-- symbolEntities: array (nullable = true) | |-- element: string (containsNull = true) |-- text: string (nullable = true) |-- urlEntities: array (nullable = true) | |-- element: struct (containsNull = true) | | |-- displayURL: string (nullable = true) | | |-- end: long (nullable = true) | | |-- expandedURL: string (nullable = true) | | |-- start: long (nullable = true) | | |-- url: string (nullable = true) |-- user: struct (nullable = true) | |-- createdAt: string (nullable = true) | |-- description: string (nullable = true) | |-- descriptionURLEntities: array (nullable = true) | | |-- element: string (containsNull = true) | |-- favouritesCount: long (nullable = true) | |-- followersCount: long (nullable = true) | |-- friendsCount: long (nullable = true) | |-- id: long (nullable = true) | |-- isContributorsEnabled: boolean (nullable = true) | |-- isDefaultProfile: boolean (nullable = true) | |-- isDefaultProfileImage: boolean (nullable = true) | |-- isFollowRequestSent: boolean (nullable = true) | |-- isGeoEnabled: boolean (nullable = true) | |-- isProtected: boolean (nullable = true) | |-- isVerified: boolean (nullable = true) | |-- lang: string (nullable = true) | |-- listedCount: long (nullable = true) | |-- location: string (nullable = true) | |-- name: string (nullable = true) | |-- profileBackgroundColor: string (nullable = true) | |-- profileBackgroundImageUrl: string (nullable = true) | |-- profileBackgroundImageUrlHttps: string (nullable = true) | |-- profileBackgroundTiled: boolean (nullable = true) | |-- profileBannerImageUrl: string (nullable = true) | |-- profileImageUrl: string (nullable = true) | |-- profileImageUrlHttps: string (nullable = true) | |-- profileLinkColor: string (nullable = true) | |-- profileSidebarBorderColor: string (nullable = true) | |-- profileSidebarFillColor: string (nullable = true) | |-- profileTextColor: string (nullable = true) | |-- profileUseBackgroundImage: boolean (nullable = true) | |-- screenName: string (nullable = true) | |-- showAllInlineMedia: boolean (nullable = true) | |-- statusesCount: long (nullable = true) | |-- timeZone: string (nullable = true) | |-- translator: boolean (nullable = true) | |-- url: string (nullable = true) | |-- utcOffset: long (nullable = true) |-- userMentionEntities: array (nullable = true) | |-- element: struct (containsNull = true) | | |-- end: long (nullable = true) | | |-- id: long (nullable = true) | | |-- name: string (nullable = true) | | |-- screenName: string (nullable = true) | | |-- start: long (nullable = true)

    outJson.select("id","text").show(false)

> +------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------+ |id |text | +------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------+ |915825548626145280|one person followed me and 2 people unfollowed me // automatically checked by https://t.co/M7i6tIzxMk | |915825548609208320|Penawaran terbatas! Dptkan \#PromoTiket ke Tokyo 4.2Jt,hanya sampai 11Okt'17 ini Langsung hubungi 24hrs Hotline Ticketing kami 021-2963 1999. https://t.co/FKR4lz1KPJ| |915825548600979456|Let's tweet \#MonChevy pics/gifs this Friday at 8 PM Paris time (7 PM UK) to send healing thoughts to our \#Versailles sister @MadameLisabi https://t.co/muZenzineP | |915825552807706624|RT @travis999998: C#GDI
H'"I-@I2I'" 'H2'D!7-#0-DH 4I'A"I A#F -A0@*5"'!2 . H2#1-0. https://t.co/a1CPDgHVOa | |915825548605009922|Felt sick all day :( | |915825548621864961|!ô≠ÛâﬁÛí≠ìgãàLINEﬁÛ¨jâ YP!ôg,1qL≠Åã\#LINEﬁÛ¨ https://t.co/6DrFlGGqfs | |915825548605009921|RT @ar14design: '1DD+'I#.Y+1 @%"!5@-2@
7-I2CD91#@7H- 5555555555 https://t.co/FfCaPWAoH2 | |915825548605001728|‡HXü¿...¿¨tòi‹‰... | |915825548609359872|RT @RamIsRising: Follow everyone who retweets this Õ | |915825548600991744|RT @ZZayadh: 'D"F (UD2J'/) 9// E*'(9JFC(U t *'(9FJ #*'(9C ( u { 1*HJ* {( v *'(9 EF 9ED 1*HJ* (w %D*2E *3*AJ/ ;)October 05, 2017 at 08:3& | |915825548626202624|Design a new logo by ProjektMate: We are a non-profit organisation from India. We would& https://t.co/8xfL3QcleF | |915825548617592832|???\_   ???\_<   / /ÇÀÉ ÇÜÉ ÑåÑå`^  \\     L  i  „  „  ˝  ˝\_       | ˝ d\_???Œ  |6 /    ˝ö&lt;Ú  |?/ |    |    || | |915825548626100225|Build website with easy API installation by iircaz55: I need a website very likely the same& https://t.co/WbNC5g8L0z | |915825548592537601|´níèM~HjDÇnoÍ≈Yã https://t.co/hqfTqBcRSp | |915825548592545798|@aimeelizzette @HornyGlF I don't think I've ever retweeted it | |915825548617797632|#0C'1 'D#0'F:'DDGE 1( G0G 'D/9H) 'D*'E) H'D5D') 'DB'&E https://t.co/EmwkpQBKtf | |915825548609314816|RT @lespros\_tetsuya: o¸DK10/10kFåWDJÂâ[W~Yà¸ | |915825548613402624|RT @imagineforestcr: …∑Û…∑Û | |915825548605104128|SSkfLADA NIVAn∞ L¸egM~Y https://t.co/me8fB3qA2o | |915825548613570560|RT @EsseoNWE: Previews w/ @RealKlash https://t.co/497ZYhpUSM | +------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------+ only showing top 20 rows

    display(outJson)

Clearly there is a lot one can do with tweets!

Enspecially, after you can get a few more primitives under your belt from the following areas: \* Natural Language Processing (MLlib, beyond word counts of course), \* Distributed vertex programming (Graph Frames, which you already know), and \* Scalable geospatial computing with location data on open street maps (roughly a third of tweets are geo-enabled with Latitude and Longitude of the tweet location) - we will get into this.

Now, let's be good at house-keeping and clean-up the unnecessary data in dbfs, our distributed file system (in databricks).

    // to remove a pre-existing directory and start from scratch uncomment next line and evaluate this cell
    dbutils.fs.rm(outputDirectoryRoot, true) 

> res50: Boolean = false

<a href="https://en.wikipedia.org/wiki/JSON">https://en.wikipedia.org/wiki/JSON</a>

    display(dbutils.fs.ls(outputDirectoryRoot+"/2017/10/05/09/")) // keep adding sub-dirs and descent into time-tree'd directory hierarchy

| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196400000/ | 1507196400000/ | 0.0 |
|-------------------------------------------------------------|----------------|-----|
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196460000/ | 1507196460000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196520000/ | 1507196520000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196580000/ | 1507196580000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196640000/ | 1507196640000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196700000/ | 1507196700000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196760000/ | 1507196760000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196820000/ | 1507196820000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196880000/ | 1507196880000/ | 0.0 |
| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507196940000/ | 1507196940000/ | 0.0 |

Next, let us take a quick peek at the notebook `scalable-data-science/sds-2-2/025_b_TTTDFfunctions` to see how we have pipelined the JSON tweets into DataFrames.

Please see <http://lamastex.org/lmse/mep/src/TweetAnatomyAndTransmissionTree.html> to understand more deeply.

%md \#\# Next, let's write the tweets into a scalable commercial cloud storage system

We will make sure to write the tweets to AWS's simple storage service or S3, a scalable storage system in the cloud. See <https://aws.amazon.com/s3/>.

**skip this section if you don't have AWS account**.

But all the main syntactic bits are here for your future convenience :)

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

Now just mount s3 as follows:

    dbutils.fs.mount(s"s3a://$AccessKey:$EncodedSecretKey@$AwsBucketName", s"/mnt/$MountName")

Now you can use the `dbutils` commands freely to access data in the mounted S3.

    dbutils.fs.help()

copying:

    // to copy all the tweets to s3
    dbutils.fs.cp("dbfs:/rawTweets",s"/mnt/$MountName/rawTweetsInS3/",recurse=true) 

deleting:

    // to remove all the files from s3
    dbutils.fs.rm(s"/mnt/$MountName/rawTweetsInS3",recurse=true) 

unmounting:

    // finally unmount when done - IMPORTANT!
    dbutils.fs.unmount(s"/mnt/$MountName") 

[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

> import twitter4j.\_ import twitter4j.auth.Authorization import twitter4j.conf.ConfigurationBuilder import twitter4j.auth.OAuthAuthorization import org.apache.spark.streaming.\_ import org.apache.spark.streaming.dstream.\_ import org.apache.spark.storage.StorageLevel import org.apache.spark.streaming.receiver.Receiver

> defined class ExtendedTwitterReceiver

> defined class ExtendedTwitterInputDStream

> import twitter4j.Status import twitter4j.auth.Authorization import org.apache.spark.storage.StorageLevel import org.apache.spark.streaming.StreamingContext import org.apache.spark.streaming.dstream.{ReceiverInputDStream, DStream} defined object ExtendedTwitterUtils

> done running the extendedTwitterUtils2run notebook - ready to stream from twitter

> twitter OAuth Credentials loaded MyconsumerKey: String MyconsumerSecret: String Mytoken: String MytokenSecret: String import twitter4j.auth.OAuthAuthorization import twitter4j.conf.ConfigurationBuilder

> USAGE: val df = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(fromParquetFile2DF("parquetFileName"))) val df = tweetsDF2TTTDF(tweetsIDLong\_JsonStringPairDF2TweetsDF(fromParquetFile2DF("parquetFileName"))) import org.apache.spark.sql.types.{StructType, StructField, StringType} import org.apache.spark.sql.functions.\_ import org.apache.spark.sql.types.\_ import org.apache.spark.sql.ColumnName import org.apache.spark.sql.DataFrame fromParquetFile2DF: (InputDFAsParquetFilePatternString: String)org.apache.spark.sql.DataFrame tweetsJsonStringDF2TweetsDF: (tweetsAsJsonStringInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame tweetsIDLong\_JsonStringPairDF2TweetsDF: (tweetsAsIDLong\_JsonStringInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame tweetsDF2TTTDF: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame tweetsDF2TTTDFWithURLsAndHastags: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame

