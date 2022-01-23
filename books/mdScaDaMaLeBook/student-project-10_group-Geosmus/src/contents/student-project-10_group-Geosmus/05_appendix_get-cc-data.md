<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Notebook for collecting tweets with country codes
=================================================

</div>

<div class="cell markdown">

This notebook is used for running jobs, do not edit without stopping the scheduled jobs!
----------------------------------------------------------------------------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// parameter for number of minutes of streaming (can be used in Jobs feature)
dbutils.widgets.text("nbr_minutes", "3", label = "Minutes of streaming (int)")
val nbr_minutes = dbutils.widgets.get("nbr_minutes").toInt
```

<div class="output execute_result plain_result" execution_count="1">

    nbr_minutes: Int = 2

</div>

</div>

<div class="cell markdown">

If the cluster was shut down, then start a new cluster and install the following libraries on it (via maven).

-   gson with maven coordinates `com.google.code.gson:gson:2.8.6`
-   twitter4j-examples with maven coordinates `org.twitter4j:twitter4j-examples:4.0.7`

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
"./07_a_appendix_extendedTwitterUtils2run"
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    import twitter4j._
    import twitter4j.auth.Authorization
    import twitter4j.conf.ConfigurationBuilder
    import twitter4j.auth.OAuthAuthorization
    import org.apache.spark.streaming._
    import org.apache.spark.streaming.dstream._
    import org.apache.spark.storage.StorageLevel
    import org.apache.spark.streaming.receiver.Receiver

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    defined class ExtendedTwitterReceiver

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    defined class ExtendedTwitterInputDStream

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    import twitter4j.Status
    import twitter4j.auth.Authorization
    import org.apache.spark.storage.StorageLevel
    import org.apache.spark.streaming.StreamingContext
    import org.apache.spark.streaming.dstream.{ReceiverInputDStream, DStream}
    defined object ExtendedTwitterUtils

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    done running the extendedTwitterUtils2run notebook - ready to stream from twitter

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
"./07_b_appendix_TTTDFfunctions"
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    USAGE: val df = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(fromParquetFile2DF("parquetFileName")))
                      val df = tweetsDF2TTTDF(tweetsIDLong_JsonStringPairDF2TweetsDF(fromParquetFile2DF("parquetFileName")))
                      
    import org.apache.spark.sql.types.{StructType, StructField, StringType}
    import org.apache.spark.sql.functions._
    import org.apache.spark.sql.types._
    import org.apache.spark.sql.ColumnName
    import org.apache.spark.sql.DataFrame
    fromParquetFile2DF: (InputDFAsParquetFilePatternString: String)org.apache.spark.sql.DataFrame
    tweetsJsonStringDF2TweetsDF: (tweetsAsJsonStringInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame
    tweetsIDLong_JsonStringPairDF2TweetsDF: (tweetsAsIDLong_JsonStringInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame
    tweetsDF2TTTDF: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame
    tweetsDF2TTTDFWithURLsAndHashtags: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    tweetsDF2TTTDFLightWeight: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame

</div>

</div>

<div class="cell markdown">

### Loading twitter credentials

You need to have a twitter developer account to run the data collection. Save your credentials in a notebook called `KeysAndTokens`, in your user home directory.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// needs upgraded databricks subscription, works on project shard

var usr = dbutils.notebook.getContext.tags("user")
var keys_notebook_location = "/Users/" + usr + "/KeysAndTokens"
dbutils.notebook.run(keys_notebook_location, 100)
```

<div class="output execute_result plain_result" execution_count="1">

    Warning: No value returned from the notebook run. To return a value from a notebook, use dbutils.notebook.exit(value)
    usr: String = bokman@chalmers.se
    keys_notebook_location: String = /Users/bokman@chalmers.se/KeysAndTokens
    res18: String = null

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import com.google.gson.Gson 
import org.apache.spark.sql.functions._
//import org.apache.spark.sql.types._

val outputDirectoryRoot = "/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus" // output directory


val batchInterval = 1 // in minutes
val timeoutJobLength =  batchInterval * 5

var newContextCreated = false
var numTweetsCollected = 0L // track number of tweets collected

// This is the function that creates the SteamingContext and sets up the Spark Streaming job.
def streamFuncWithProcessing(): StreamingContext = {
  // Create a Spark Streaming Context.
  val ssc = new StreamingContext(sc, Minutes(batchInterval))
  // Create the OAuth Twitter credentials 
  val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  
  // Create filter
  val locationsQuery = new FilterQuery().locations(Array(-180.0, -90.0), Array(180.0, 90.0)) // all locations
  
  // Create a Twitter Stream for the input source.  
  val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth, Some(locationsQuery))
  // Transform the discrete RDDs into JSON
  val twitterStreamJson = twitterStream.map(x => { val gson = new Gson();
                                                 val xJson = gson.toJson(x)
                                                 xJson
                                               }) 
  // take care
  val partitionsEachInterval = 1 // This tells the number of partitions in each RDD of tweets in the DStream.
  
  // get some time fields from current `.Date()`, use the same for each batch in the job
  val year = (new java.text.SimpleDateFormat("yyyy")).format(new java.util.Date())
  val month = (new java.text.SimpleDateFormat("MM")).format(new java.util.Date())
  val day = (new java.text.SimpleDateFormat("dd")).format(new java.util.Date())
  val hour = (new java.text.SimpleDateFormat("HH")).format(new java.util.Date())
  
  // what we want done with each discrete RDD tuple: (rdd, time)
  twitterStreamJson.foreachRDD((rdd, time) => { // for each filtered RDD in the DStream
      val count = rdd.count() //We count because the following operations can only be applied to non-empty RDD's
      if (count > 0) {
        val outputRDD = rdd.repartition(partitionsEachInterval) // repartition as desired
        // to write to parquet directly in append mode in one directory per 'time'------------       
        val outputDF = outputRDD.toDF("tweetAsJsonString")
        val processedDF = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(outputDF)).filter($"countryCode" =!= lit(""))

        
        // Writing the full processed df (We probably don't need it, but useful for exploring the data initially)
        processedDF.write.mode(SaveMode.Append)
                .parquet(outputDirectoryRoot + "/" + year + "/" + month + "/" + day + "/" + hour + "/" + time.milliseconds) 
        
        // end of writing as parquet file-------------------------------------
        numTweetsCollected += count // update with the latest count
      }
  })
  newContextCreated = true
  ssc
}
```

<div class="output execute_result plain_result" execution_count="1">

    import com.google.gson.Gson
    import org.apache.spark.sql.functions._
    outputDirectoryRoot: String = /datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus
    batchInterval: Int = 1
    timeoutJobLength: Int = 5
    newContextCreated: Boolean = false
    numTweetsCollected: Long = 0
    streamFuncWithProcessing: ()org.apache.spark.streaming.StreamingContext

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Now just use the function to create a Spark Streaming Context
val ssc = StreamingContext.getActiveOrCreate(streamFuncWithProcessing)
```

<div class="output execute_result plain_result" execution_count="1">

    ssc: org.apache.spark.streaming.StreamingContext = org.apache.spark.streaming.StreamingContext@212f8bef

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// you only need one of these to start
ssc.start()
// ssc.awaitTerminationOrTimeout(30000) //time in milliseconds
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Note, this is not fool-proof...
Thread.sleep(nbr_minutes*60*1000) //time in milliseconds
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
ssc.stop(stopSparkContext = false)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
numTweetsCollected // number of tweets collected so far
```

</div>
