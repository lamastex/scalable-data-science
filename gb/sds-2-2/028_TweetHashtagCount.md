[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Twitter Hashtag Count
=====================

Using Twitter Streaming is a great way to learn Spark Streaming if you don't have your streaming datasource and want a great rich input dataset to try Spark Streaming transformations on.

In this example, we show how to calculate the top hashtags seen in the last X window of time every Y time unit.

Extracting knowledge from tweets is "easy" using techniques shown here, but one has to take responsibility for the use of this knowledge and conform to the rules and policies linked below.

Remeber that the use of twitter itself comes with various strings attached. Read:

-   [Twitter Rules](https://twitter.com/rules)

Crucially, the use of the content from twitter by you (as done in this worksheet) comes with some strings. Read: - [Developer Agreement & Policy Twitter Developer Agreement](https://dev.twitter.com/overview/terms/agreement-and-policy)

``` scala
import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._


import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
```

>     import org.apache.spark._
>     import org.apache.spark.storage._
>     import org.apache.spark.streaming._
>     import org.apache.spark.streaming.twitter.TwitterUtils
>     import scala.math.Ordering
>     import twitter4j.auth.OAuthAuthorization
>     import twitter4j.conf.ConfigurationBuilder

### Step 1: Enter your Twitter API Credentials.

-   Go to https://apps.twitter.com and look up your Twitter API Credentials, or create an app to create them.
-   Run the code in a cell to Enter your own credentials.

``` %scala
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
```

The cell-below is hidden to not expose my Twitter API Credentials: `consumerKey`, `consumerSecret`, `accessToken` and `accessTokenSecret`. Use the code above to enter your own credentials!

>     import twitter4j._
>     import twitter4j.auth.Authorization
>     import twitter4j.conf.ConfigurationBuilder
>     import twitter4j.auth.OAuthAuthorization
>     import org.apache.spark.streaming._
>     import org.apache.spark.streaming.dstream._
>     import org.apache.spark.storage.StorageLevel
>     import org.apache.spark.streaming.receiver.Receiver

If you see warnings then ignore for now: <https://forums.databricks.com/questions/6941/change-in-getargument-for-notebook-input.html>.

### Step 2: Configure where to output the top hashtags and how often to compute them.

-   Run this cell for the input cells to appear.
-   Enter your credentials.
-   Run the cell again to pick up your defaults.

``` scala
val outputDirectory = "/datasets/tweetsStreamTmp" // output directory

//Recompute the top hashtags every N seconds. N=1
val slideInterval = new Duration(10 * 1000) // 1000 milliseconds is 1 second!

//Compute the top hashtags for the last M seconds. M=5
val windowLength = new Duration(30 * 1000)

// Wait W seconds before stopping the streaming job. W=100
val timeoutJobLength = 20 * 1000
```

>     outputDirectory: String = /datasets/tweetsStreamTmp
>     slideInterval: org.apache.spark.streaming.Duration = 10000 ms
>     windowLength: org.apache.spark.streaming.Duration = 30000 ms
>     timeoutJobLength: Int = 20000

### Step 3: Run the Twitter Streaming job.

Go to SparkUI and see if a streaming job is already running. If so you need to terminate it before starting a new streaming job. Only one streaming job can be run on the DB CE.

``` scala
// this will make sure all streaming job in the cluster are stopped
StreamingContext.getActive.foreach{ _.stop(stopSparkContext = false) } 
```

Clean up any old files.

``` scala
dbutils.fs.rm(outputDirectory, true)
```

>     res17: Boolean = true

Let us write the function that creates the Streaming Context and sets up the streaming job.

``` scala
var newContextCreated = false
var num = 0

// This is a helper class used for ordering by the second value in a (String, Int) tuple
import scala.math.Ordering
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
  val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth)
  
  // Parse the tweets and gather the hashTags.
  val hashTagStream = twitterStream.map(_.getText).flatMap(_.split(" ")).filter(_.startsWith("#"))
  
  // Compute the counts of each hashtag by window.
  // reduceByKey on a window of length windowLength
  // Once this is computed, slide the window by slideInterval and calculate reduceByKey again for the second window
  val windowedhashTagCountStream = hashTagStream.map((_, 1)).reduceByKeyAndWindow((x: Int, y: Int) => x + y, windowLength, slideInterval)

  // For each window, calculate the top hashtags for that time period.
  windowedhashTagCountStream.foreachRDD(hashTagCountRDD => {
    val topEndpoints = hashTagCountRDD.top(20)(SecondValueOrdering)
    dbutils.fs.put(s"${outputDirectory}/top_hashtags_${num}", topEndpoints.mkString("\n"), true)
    println(s"------ TOP HASHTAGS For window ${num}")
    println(topEndpoints.mkString("\n"))
    num = num + 1
  })
  
  newContextCreated = true
  ssc
}
```

>     newContextCreated: Boolean = false
>     num: Int = 0
>     import scala.math.Ordering
>     defined object SecondValueOrdering
>     creatingFunc: ()org.apache.spark.streaming.StreamingContext

Create the StreamingContext using getActiveOrCreate, as required when starting a streaming job in Databricks.

``` scala
val ssc = StreamingContext.getActiveOrCreate(creatingFunc)
```

>     ssc: org.apache.spark.streaming.StreamingContext = org.apache.spark.streaming.StreamingContext@3f9f7eb1

Start the Spark Streaming Context and return when the Streaming job exits or return with the specified timeout.

``` scala
ssc.start()
ssc.awaitTerminationOrTimeout(timeoutJobLength)
ssc.stop(stopSparkContext = false)
```

>     Wrote 477 bytes.
>     ------ TOP HASHTAGS For window 0
>     (#izmirescort,2)
>     (#YouAre,2)
>     (#갓세븐_YouAre_정오공개,1)
>     (#TFB,1)
>     (#Doctors,1)
>     (#CCTVDiskominfoMKS,1)
>     (#longsor,1)
>     (#MakeLifeBetter,1)
>     (#갓세븐
>     #7for7,1)
>     (#ㅅㅏ설토ㅌㅗ추천사ㅇㅣ트
>     #ㅅㅏ설ㅌㅗ토추천사ㅇㅣ트
>
>     ❕☽❇M☣️⛹
>     A
>     ☄
>
>     ➡️들어가기➡️,1)
>     (#ターナーアワード,1)
>     (#Zaragoza,1)
>     (#박우진,1)
>     (#DiaInternacionaldelaNena,1)
>     (#윤지성,1)
>     (#방탄소년단,1)
>     (#정국,1)
>     (#워너원…,1)
>     (#ﷺ,1)
>     (#JeuConcours,1)
>     Wrote 511 bytes.
>     ------ TOP HASHTAGS For window 1
>     (#YouAre,4)
>     (#izmirescort,4)
>     (#MPN,3)
>     (#シナモンメルツ,3)
>     (#방탄소년단,3)
>     (#LittleMix,2)
>     (#KCAArgentina,2)
>     (#恋愛,1)
>     (#안전공원
>     #ㅅㅏ설토토ㅅㅏㅇㅣ트추천
>
>     안-전-놀-이-터
>
>     ✩안전-놀-이-터✩
>
>     ㊙,1)
>     (#tspo,1)
>     (#BackTheBlue,1)
>     (#odaibako
>     帰りたいですね,1)
>     (#amwriting,1)
>     (#Resistencia,1)
>     (#拡散希望,1)
>     (#セクシー,1)
>     (#Passion
>
>     https://t.co/CBKI0z2rSR,1)
>     (#ゴーストホスピタル　療養病棟担当　オーレリー,1)
>     (#Dosogas,1)
>     (#BiggBoss11…,1)

Check out the Clusters 'Streaming\` UI as the job is running.

It should automatically stop the streaming job after `timeoutJobLength`.

If not, then stop any active Streaming Contexts, but don't stop the spark contexts they are attached to using the following command.

``` scala
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) }
```

### Step 4: View the Results.

``` scala
display(dbutils.fs.ls(outputDirectory))
```

| path                                             | name              | size  |
|--------------------------------------------------|-------------------|-------|
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_0  | top\_hashtags\_0  | 0.0   |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_1  | top\_hashtags\_1  | 30.0  |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_10 | top\_hashtags\_10 | 163.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_11 | top\_hashtags\_11 | 277.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_12 | top\_hashtags\_12 | 317.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_13 | top\_hashtags\_13 | 345.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_14 | top\_hashtags\_14 | 290.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_15 | top\_hashtags\_15 | 256.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_16 | top\_hashtags\_16 | 227.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_17 | top\_hashtags\_17 | 184.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_18 | top\_hashtags\_18 | 141.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_19 | top\_hashtags\_19 | 170.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_2  | top\_hashtags\_2  | 106.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_20 | top\_hashtags\_20 | 183.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_21 | top\_hashtags\_21 | 165.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_22 | top\_hashtags\_22 | 203.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_23 | top\_hashtags\_23 | 239.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_24 | top\_hashtags\_24 | 200.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_25 | top\_hashtags\_25 | 175.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_26 | top\_hashtags\_26 | 211.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_27 | top\_hashtags\_27 | 192.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_28 | top\_hashtags\_28 | 217.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_29 | top\_hashtags\_29 | 216.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_3  | top\_hashtags\_3  | 256.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_30 | top\_hashtags\_30 | 349.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_31 | top\_hashtags\_31 | 291.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_32 | top\_hashtags\_32 | 168.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_33 | top\_hashtags\_33 | 205.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_34 | top\_hashtags\_34 | 191.0 |
| dbfs:/datasets/tweetsStreamTmp/top\_hashtags\_35 | top\_hashtags\_35 | 239.0 |

Truncated to 30 rows

There should be 100 intervals for each second and the top hashtags for each of them should be in the file `top_hashtags_N` for `N` in 0,1,2,...,99 and the top hashtags should be based on the past 5 seconds window.

``` scala
dbutils.fs.head(s"${outputDirectory}/top_hashtags_11")
```

>     res15: String =
>     (#Nami,1)
>     (#WorldOctopusDay,1)
>     (#Terrebonne,1)
>     (#ヒグチユウコ,1)
>     (#シナモンメルツ,1)
>     (#مملكة_قلم_للدعم
>     #المقابيل_للدعم
>     #ذئب_سلمان_للدعم
>     #مملكه_شيخه_للدعم,1)
>     (#CCH32,1)
>     (#beBee,1)
>     (#WeeklyIdol,1)
>     (#SAMURAIBLUE,1)

>     defined class ExtendedTwitterReceiver

>     defined class ExtendedTwitterInputDStream

>     import twitter4j.Status
>     import twitter4j.auth.Authorization
>     import org.apache.spark.storage.StorageLevel
>     import org.apache.spark.streaming.StreamingContext
>     import org.apache.spark.streaming.dstream.{ReceiverInputDStream, DStream}
>     defined object ExtendedTwitterUtils

>     twitter OAuth Credentials loaded
>     MyconsumerKey: String
>     MyconsumerSecret: String
>     Mytoken: String
>     MytokenSecret: String
>     import twitter4j.auth.OAuthAuthorization
>     import twitter4j.conf.ConfigurationBuilder

>     done running the extendedTwitterUtils2run notebook - ready to stream from twitter

### Let's brainstorm a bit now

What could you do with this type of streaming capability?

-   marketing?
-   pharmaceutical vigilance?
-   linking twitter activity to mass media activity?
-   ...

Note that there are various Spark Streaming ML algorithms that one could easily throw at such `reduceByKeyAndWindow` tweet streams: \* [Frequent Pattern Mining](https://spark.apache.org/docs/latest/mllib-frequent-pattern-mining.html) \* [Streaming K-Means](https://databricks.com/blog/2015/01/28/introducing-streaming-k-means-in-spark-1-2.html) \* [Latent Dirichlet Allocation - Topic Modeling](https://spark.apache.org/docs/latest/ml-clustering.html#latent-dirichlet-allocation-lda)

Student Project or Volunteer for next Meetup - let's check it out now:

HOME-WORK: \* [Twitter Streaming Language Classifier](https://databricks.gitbooks.io/databricks-spark-reference-applications/content/twitter_classifier/index.html)