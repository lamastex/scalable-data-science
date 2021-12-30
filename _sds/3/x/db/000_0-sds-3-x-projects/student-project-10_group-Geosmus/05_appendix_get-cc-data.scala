// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC #Notebook for collecting tweets with country codes

// COMMAND ----------

// MAGIC %md ## This notebook is used for running jobs, do not edit without stopping the scheduled jobs!

// COMMAND ----------

// parameter for number of minutes of streaming (can be used in Jobs feature)
dbutils.widgets.text("nbr_minutes", "3", label = "Minutes of streaming (int)")
val nbr_minutes = dbutils.widgets.get("nbr_minutes").toInt

// COMMAND ----------

// MAGIC %md
// MAGIC If the cluster was shut down, then start a new cluster and install the following libraries on it (via maven).
// MAGIC 
// MAGIC * gson with maven coordinates `com.google.code.gson:gson:2.8.6` 
// MAGIC * twitter4j-examples with maven coordinates `org.twitter4j:twitter4j-examples:4.0.7`

// COMMAND ----------

// MAGIC %run "./07_a_appendix_extendedTwitterUtils2run"

// COMMAND ----------

// MAGIC %run "./07_b_appendix_TTTDFfunctions"

// COMMAND ----------

// MAGIC %md ###Loading twitter credentials
// MAGIC You need to have a twitter developer account to run the data collection. Save your credentials in a notebook called `KeysAndTokens`, in your user home directory.

// COMMAND ----------

// needs upgraded databricks subscription, works on project shard

var usr = dbutils.notebook.getContext.tags("user")
var keys_notebook_location = "/Users/" + usr + "/KeysAndTokens"
dbutils.notebook.run(keys_notebook_location, 100)

// COMMAND ----------

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

// COMMAND ----------

// Now just use the function to create a Spark Streaming Context
val ssc = StreamingContext.getActiveOrCreate(streamFuncWithProcessing)

// COMMAND ----------

// you only need one of these to start
ssc.start()
// ssc.awaitTerminationOrTimeout(30000) //time in milliseconds

// COMMAND ----------

// Note, this is not fool-proof...
Thread.sleep(nbr_minutes*60*1000) //time in milliseconds

// COMMAND ----------

ssc.stop(stopSparkContext = false)

// COMMAND ----------

numTweetsCollected // number of tweets collected so far