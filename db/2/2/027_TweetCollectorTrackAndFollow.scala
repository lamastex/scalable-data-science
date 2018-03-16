// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/qgMIxwCA01w/0.jpg)](https://www.youtube.com/embed/qgMIxwCA01w?start=0&end=2991&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/J1NlUSz1MVk/0.jpg)](https://www.youtube.com/embed/J1NlUSz1MVk?start=0&end=2634&autoplay=1)

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

// MAGIC %run "scalable-data-science/sds-2-2/025_a_extendedTwitterUtils2run"

// COMMAND ----------

// MAGIC %run "scalable-data-science/sds-2-2/025_b_TTTDFfunctions"

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

// MAGIC %run "scalable-data-science/secrets/026_secret_MyTwitterOAuthCredentials"

// COMMAND ----------

// MAGIC %md
// MAGIC Let's import a list of twitterIDS of political interest in the UK.

// COMMAND ----------

import scala.collection.mutable.ListBuffer

val TwitterIdsDirectlyFromCsv = sqlContext.read.format("com.databricks.spark.csv")
                        .option("header", "false")
                        .option("inferSchema", "true")
                        .load("/FileStore/tables/zo6licf21496412465690/candidates_newspapers_bloggers_unique-741bb.txt")
                        .select($"_c0")//.filter($"C1".contains("@"))
                        .rdd.map({case Row(val1: Long) => val1}).collect().to[ListBuffer]

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
  
  val track = List("@raazozone", "#MakeDataGreatAgain","sds-2-2 rules!")// just added for some live tests
  //val track = List.empty // if you do not want to track by any string
  
  val follow = TwitterIdsDirectlyFromCsv //UKBuffList
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

display(dbutils.fs.ls(outputDirectoryRoot))

// COMMAND ----------

display(dbutils.fs.ls(outputDirectoryRoot+"/2017/10/05/09/")) // keep adding sub-dirs and descent into time-tree'd directory hierarchy

// COMMAND ----------



// COMMAND ----------

// this will make sure all streaming job in the cluster are stopped - raaz
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 

// COMMAND ----------

val rawDF = fromParquetFile2DF(outputDirectoryRoot+"/2017/10/05/09/*/*") //.cache()
val TTTsDF = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(rawDF)).cache()

// COMMAND ----------

display(TTTsDF)

// COMMAND ----------

// this will make sure all streaming job in the cluster are stopped - raaz
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 

// COMMAND ----------

// this will delete what we collected to keep the disk usage tight and tidy
dbutils.fs.rm(outputDirectoryRoot, true) 

// COMMAND ----------

