// Databricks notebook source
// MAGIC %md
// MAGIC # DIGSUM Streaming Job Setup  - All sv tweets!
// MAGIC Simon Lindgren 180824
// MAGIC 
// MAGIC Raaz adapted on 20180903 to add top 400 most frequent words from sentiment lexicon (excluding even the election-related hashtags that is in the other streaming job).
// MAGIC 
// MAGIC Simon killed this also and I didn't notice for another 8 hours - big hole in election analysis again - last hole was 4 days long again caused by Simon....!!! Cannot do research under such perturbations on design space! Need to beg databricks for professional shard with permission control for other users.... 

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC This notebook is for setting up data collection jobs through Twitter's public Streaming API.
// MAGIC 
// MAGIC #### 1. Import the required libraries.

// COMMAND ----------

import org.apache.spark.streaming._
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import com.google.gson.Gson

// COMMAND ----------

// MAGIC %md
// MAGIC Also, run this notebook to add some needed utilities. **Raaz added language filters on 20180903.**

// COMMAND ----------

// MAGIC %run "scalable-data-science/meme-evolution/db/src2run/extendedTwitterUtils2runWithLangs" 

// COMMAND ----------

// MAGIC %md
// MAGIC #### 2. Set up Twitter credentials

// COMMAND ----------

//Raaz's twitter credentials for sds-2-2

val MyconsumerKey       = "W77DxuU6zCleueK2vyYbLvMdI"
val MyconsumerSecret    = "iRZYR5UuG1r1FdzZW3HOdSP5XWCAPhYQY15QcOP2OiUhykRw7o"
val Mytoken             = "4173723312-zXG4Glv7MU75m8Qbzlp3n3ZVNYlduq7BHdqOg8b"
val MytokenSecret       = "cGxWtkJRz3Iln68NfAx86LGBd3rRzT3nzBCjD2JeFihfi"


System.setProperty("twitter4j.oauth.consumerKey", MyconsumerKey)
System.setProperty("twitter4j.oauth.consumerSecret", MyconsumerSecret)
System.setProperty("twitter4j.oauth.accessToken", Mytoken)
System.setProperty("twitter4j.oauth.accessTokenSecret", MytokenSecret)

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3. Choose what to stream for
// MAGIC 
// MAGIC Two lists must be set up: One with strings, and one with user IDs. If we want to stream for only one of these types, an empty list must still be set up for both.

// COMMAND ----------

// MAGIC %md
// MAGIC ###### A list of TwitterIDsToFollow
// MAGIC 
// MAGIC This must be in list (buffer?) format.

// COMMAND ----------

//val TwitterIDsToFollow = List(878300089L, 3325105205L, 980415995629187072L)

// Or read ids from a file into a list.

val TwitterIDsToFollow = List.empty

// COMMAND ----------

// MAGIC %md
// MAGIC ###### A list of TwitterStringsToFollow

// COMMAND ----------

//val TwitterStringsToFollow = List("#val2018")

// Or read keywords from a file into a list.

val TwitterStringsToFollow = List.empty  

// COMMAND ----------

// MAGIC %md
// MAGIC  Let's find the most frequent swedish words from the 5.6M swedish tweets over the last few months' collection.

// COMMAND ----------

val sv_TTTsDF = spark.read.parquet("dbfs:///datasets/MEP/SE/svTTTsDFAll").cache()
sv_TTTsDF.count

// COMMAND ----------

sv_TTTsDF.count

// COMMAND ----------

import org.apache.spark.ml.feature.RegexTokenizer
//import org.apache.spark.sql.types.{StructType, StructField, StringType}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

// this is a user-defined function to be passed to clean the TweetText
val cleanTweet = udf((s: String) => 
                    if (s == null) null 
                    else s
                          .replaceAll("(?U)(\\w+:\\/\\/\\S+)","") // remove patters with :// typical of urls
                          .replaceAll("(?U)(#\\w+\\S+)","") // remove hashtags
                          .replaceAll("(?U)(@\\w+\\S+)","") // remove mentions
                          .replaceAll("(?U)(RT\\s+)","") // remove RT
                          .replaceAll("(?U)(\\s*\\S*\\d+\\S*\\s*)","") //remove strings containing 1 or more numerals
                          //.toLowerCase() // don't use lowercase if you want the frequency of words 'as is'
                   )

val regexTok = new RegexTokenizer("regexTok")
  .setInputCol("cleanedCurrentTweet")
  .setPattern("(?U)\\W+")

val TwWordFreqDF = regexTok.transform(sv_TTTsDF
                              .withColumn("cleanedCurrentTweet", cleanTweet($"CurrentTweet"))
                            ).withColumn("TwWord", explode($"regexTok__output"))
                             .filter(not($"TwWord" === " ️"))// a specific white-space-like character copy-pasted here escapes the regex-ing... filtering it explicitly
                             .select("TwWord").withColumn("weight",lit(1L))
                             .groupBy("TwWord").agg(sum($"weight").as("freq")).orderBy($"freq".desc).cache()
TwWordFreqDF.count

// COMMAND ----------

TwWordFreqDF.select("freq").agg(sum("freq")).show

// COMMAND ----------

display(TwWordFreqDF.withColumn("rel_freq",$"freq"/96081093.0).limit(400))

// COMMAND ----------

TwWordFreqDF.withColumn("rel_freq",$"freq"/96081093.0).select("rel_freq").limit(400).agg(sum("rel_freq")).show // the top most 400 frequent words are over 60% of most frequent words!!!

// COMMAND ----------

// MAGIC %md
// MAGIC Thus we get the top 400 most used Swedish words from our own collection of 5.6M Swedist tweets.

// COMMAND ----------

import scala.collection.mutable.ListBuffer

// we could definitely clean this up, remove single letters, numbers etc., but I am tired now...
val  TwitterStringsToFollow: scala.collection.mutable.ListBuffer[String] = TwWordFreqDF.select("TwWord").limit(400).rdd
                            .map({case Row(val1: String) => val1})
                            .collect()
                            .to[ListBuffer]


// COMMAND ----------

/*
//alternatively these top 200 words come from a website/book: https://www.smashwords.com/books/view/631270
// but it is upper-case and am not sure how this data was compiled...
// May be Simon has a better idea of getting the most frequently used 400 words in Swedish... Especially as used in Twitter...

"ÄR","INTE","DU","EN","ATT","PÅ","I","HAR","DET","KAN","VAR","HÄR","JAG","FÖR","OM","TILL","ETT","MED","MIG","DEN","VILL","KOMMER","MIN","AV","OCH","DIN","NI","SKA","SOM","MÅSTE","HAN","HA","DIG","SÅ","SKULLE","VI","BARA","GILLAR","SIG","GÖRA","VARA","BEHÖVER","MITT","UPP","DÄR","TYCKER","GICK","VÄLDIGT","ÄLSKAR","UT","VET","VAD","GÅ","MYCKET","FRÅN","GÅR","SIN","SER","BORDE","BLEV","LIGGER","EFTER","SÅG","HONOM","FINNS","HANS","GÖR","GJORDE","LITE","ALDRIG","TRODDE","DITT","TROR","ÖVER","VILJA","ELLER","HON","MÅNGA","ÅT","ER","TVÅ","NÅGON","HADE","KOM","FICK","DE","ÄN","INGEN","OSS","BOR","MINA","NÅGOT","FÅR","BRA","IN","KÄNNER","HENNE","BLIR","HJÄLPA","ALLA","TALAR","DETTA","FAKTISKT","VID","MOT","KOMMA","DINA","INGA","SE","VARIT","TOG","NÅGRA","SA","TA","INGET","HELA","BLI","SITT","FORTFARANDE","PRATA","HETER","TAR","HÅLLER","MAN","ALLT","KUNDE","VARJE","FÅ","HENNES","ALLTID","UTAN","VERKLIGEN","FAR","ÄTER","MER","KÖPTE","TALA","SINA","HUR","PRECIS","FÖRSTÅR","VERKAR","UNDER","ÅKA","ENGELSKA","SPELAR","TILLBAKA","KLOCKAN","GAMMAL","TID","DEM","LÅNG","HATAR","SPELA","TRE","HITTADE","KÖPA","OFTA","SNÄLL","NÄSTA","RÄDD","RIKTIGT","NER","SKREV","EMOT","NÄSTAN","ÅKTE","LÄNGE","VILLE","INGENTING","GE","BOKEN","BROR","UR","SETT","BÖRJAR","BORT","BOK","PRATAR","HÄNDE","HÖRDE","KLARAR","HELT","NÄR","STÅR","NYA","ANDRA","FEL","KÄNDE","HÄMTA","JUST","FRÅGA","GJORT","VISADE","MEN","VEM","EGEN","GILLADE","FÖRSÖKTE","VANN"
*/

// COMMAND ----------

/*
// we are now collecting all sv language tweets...

import scala.collection.mutable.ListBuffer

val TwitterStringsToFollow: scala.collection.mutable.ListBuffer[String] = ListBuffer("#val2018","#svpol","#valet2018")
//Raaz is adding these 397 words to the streaming job
val lexSVWordsTopPosAndNeg0To397 = spark.read.parquet("dbfs:///datasets/MEP/SE/spraakbankenSentimentLex")
                            .orderBy($"lemgram_frequency".desc)
                            .limit(397)
                            .select("word")
                            //.filter($"polarity"==="neg")
                            .rdd
                            .map({case Row(val1: String) => val1})
                            .collect()
                            .to[ListBuffer]
TwitterStringsToFollow ++= lexSVWordsTopPosAndNeg0To397
TwitterStringsToFollow.size // first 350 words have 241 pos  and 109 neg words
*/

// COMMAND ----------

val TwitterLanguagesToFollow = List("sv")

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4. Run the streaming job

// COMMAND ----------

// MAGIC %md
// MAGIC Set output directory (create it if needed), and set the batch interval.

// COMMAND ----------

// Create directory
//dbutils.fs.mkdirs("dbfs:/datasets/MEP/SE/svLangStreamingByTop400SvTwWords/") // raaz

// COMMAND ----------

val outputDirectoryRoot = "/datasets/MEP/SE/svLangStreamingByTop400SvTwWords/" //raaz added this as this job is definietely redundant now
val batchInterval = 5 // minutes
val timeoutJobLength =  batchInterval * 5

// COMMAND ----------

// Delete directory
//dbutils.fs.rm("dbfs:/datasets/MEP/DIGSUM/Streaming/Test1", recurse = true)

// COMMAND ----------

// View the DIGSUM Streaming directory
display(dbutils.fs.ls("/datasets/MEP/SE"))

// COMMAND ----------

TwitterStringsToFollow

// COMMAND ----------

// MAGIC %md
// MAGIC The three cells below sets up, and starts the streaming job.

// COMMAND ----------

var newContextCreated = false
var numTweetsCollected = 0L // track number of tweets collected
//val conf = new SparkConf().setAppName("TrackedTweetCollector").setMaster("local")
// This is the function that creates the SteamingContext and sets up the Spark Streaming job.
def streamFunc(): StreamingContext = {
  // Create a Spark Streaming Context.
  val ssc = new StreamingContext(sc, Minutes(batchInterval))
  // Create a Twitter Stream for the input source. 
  val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  
  val track = TwitterStringsToFollow
  val follow = TwitterIDsToFollow
  val langs = TwitterLanguagesToFollow
  
  val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth, track, follow, langs)
  val twitterStreamJson = twitterStream.map(x => { val gson = new Gson();
                                                 val xJson = gson.toJson(x)
                                                 xJson
                                               }) 
 
val partitionsEachInterval = 1 // This tells the number of partitions in each RDD of tweets in the DStream.

twitterStreamJson.foreachRDD((rdd, time) => { // for each filtered RDD in the DStream
      val count = rdd.count()
      if (count > 0) {
        val outputRDD = rdd.repartition(partitionsEachInterval) // repartition as desired
        ////outputRDD.saveAsTextFile(s"${outputDirectory}/tweets_" + time.milliseconds.toString) // save as textfile
        //outputRDD.saveAsTextFile(outputDirectoryRoot + "/tweets_" + time.milliseconds.toString) // save as textfile in s3
        // to write to parquet directly in append mode as one file------------
        
        val outputDF = outputRDD.toDF("tweetAsJsonString")
        val year = (new java.text.SimpleDateFormat("yyyy")).format(new java.util.Date())
        val month = (new java.text.SimpleDateFormat("MM")).format(new java.util.Date())
        val day = (new java.text.SimpleDateFormat("dd")).format(new java.util.Date())
        val hour = (new java.text.SimpleDateFormat("HH")).format(new java.util.Date())
        outputDF.write.mode(SaveMode.Append).parquet(outputDirectoryRoot+ "/"+ year + "/" + month + "/" + day + "/" + hour + "/" + time.milliseconds) 
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

display(dbutils.fs.ls("/datasets/MEP/DIGSUM/Streaming"))

// COMMAND ----------

// MAGIC %md
// MAGIC ##### Stop all streaming jobs

// COMMAND ----------

// this will make sure all streaming job in the cluster are stopped
StreamingContext.getActive.foreach{ _.stop(stopSparkContext = false) }

// How to stop individual streaming jobs and keep others running?

// COMMAND ----------

