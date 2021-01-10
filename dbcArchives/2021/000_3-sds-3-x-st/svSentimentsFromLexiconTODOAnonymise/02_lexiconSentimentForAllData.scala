// Databricks notebook source
// MAGIC %md
// MAGIC # Getting the Polarity of 3,702,707 Swedish tweets using LEXICON
// MAGIC The LEXICON was obtained from https://spraakbanken.gu.se/eng/resource/sentimentlex
// MAGIC 
// MAGIC ### !!You should not have to rerun this notebook!!
// MAGIC 
// MAGIC See `./exploreSentimentScores` to explore the results of the sentiment analysis on 3,702,707 sv-language status updates that was already done and written to a parquet file here.

// COMMAND ----------

val lex = spark.read.parquet("dbfs:///datasets/MEP/SE/spraakbankenSentimentLex").cache
lex.count // 2067

// COMMAND ----------

display(lex.orderBy($"lemgram_frequency".desc))

// COMMAND ----------

//Raaz is adding these 350 words to the streaming job
import scala.collection.mutable.ListBuffer
val lexSVWordsTopPosAndNeg = spark.read.parquet("dbfs:///datasets/MEP/SE/spraakbankenSentimentLex")
                            .orderBy($"lemgram_frequency".desc)
                            .limit(350)
                            .select("word")
                            //.filter($"polarity"==="neg")
                            .rdd
                            .map({case Row(val1: String) => val1})
                            .collect()
                            .to[ListBuffer]
lexSVWordsTopPosAndNeg.size // 350 = 241 pos + 109 neg

// COMMAND ----------

lexSVWordsTopPosAndNeg.slice(349,350)

// COMMAND ----------

//Simon we could be adding these NEXT 390 words to another streaming job, but may be overkill as the currents DIGSUM streamers is at 60 tweets per second
import scala.collection.mutable.ListBuffer
val lexSVWordsTopPosAndNeg351To740 = spark.read.parquet("dbfs:///datasets/MEP/SE/spraakbankenSentimentLex")
                            .orderBy($"lemgram_frequency".desc)
                            .limit(350+390)
                            .select("word")
                            //.filter($"polarity"==="pos")
                            .rdd
                            .map({case Row(val1: String) => val1})
                            .collect()
                            .to[ListBuffer]
                            .slice(350,350+390)

lexSVWordsTopPosAndNeg351To740.size // 390 = 506-241 pos + 234-109 neg

// Simon you could add these words with the three hashtags if you want like so (just copy paste this cell into your streaming job - stop and restart it with this list, then together we will be collecting the top most frequent ):

val TwitterStringsOfInterest: scala.collection.mutable.ListBuffer[String] = ListBuffer("#val2018","#svpol","#valet2018")
TwitterStringsOfInterest ++= lexSVWordsTopPosAndNeg351To740

// COMMAND ----------

390 == 506-241 + 234-109 // the number of pos and neg words out of the next 390 words

// COMMAND ----------

import scala.collection.mutable.ListBuffer
val lexSVWordsTopPos = spark.read.parquet("dbfs:///datasets/MEP/SE/spraakbankenSentimentLex")
                            .filter($"polarity"==="pos")
                            .orderBy($"lemgram_frequency".desc)
                            .select("word")
                            .rdd.map({case Row(val1: String) => val1}).collect().to[ListBuffer]
lexSVWordsTopPos.size // 1332

val lexSVWordsTopNeg = spark.read.parquet("dbfs:///datasets/MEP/SE/spraakbankenSentimentLex")
                            .filter($"polarity"==="neg")
                            .orderBy($"lemgram_frequency".desc)
                            .select("word")
                            .rdd.map({case Row(val1: String) => val1}).collect().to[ListBuffer]
lexSVWordsTopNeg.size // 735

// COMMAND ----------

display(lex)

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

// this is the latest archiving of all SV tweets from both streaming jobs
val TTTsDF_sv_All = spark.read.parquet("dbfs:///datasets/MEP/SE/svTTTsDFAll") //.cache
TTTsDF_sv_All.count // 10030290

// COMMAND ----------

TTTsDF_sv_All.count // 5 second if cached BUT can crash if you keep cached....

// COMMAND ----------

TTTsDF_sv_All.printSchema

// COMMAND ----------

TTTsDF_sv_All.groupBy($"TweetType").count.orderBy($"count".desc).show(false)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Appreciate the complexity of the update types...

// COMMAND ----------

display(TTTsDF_sv_All.filter($"TweetType" === "ReTweet").select($"OriginalTwIDinRT".as("OriginalTwId"),$"CurrentTwID",$"CurrentTweet",$"URLs",$"hashtags")
        //.distinct()
        .orderBy("OriginalTwId"))

// COMMAND ----------

// MAGIC %md
// MAGIC The sentiment of `CurrentTweet` in a `Reply Tweet` will be the sentiment of the `Reply Tweet`. This is in contrast with `ReTweet` shown above with sentiment of original tweet mirrored back.

// COMMAND ----------

display(TTTsDF_sv_All.filter($"TweetType" === "Reply Tweet").select($"OriginalTwIDinReply".as("OriginalTwId"),$"CurrentTwID",$"CurrentTweet",$"URLs",$"hashtags")
        //.distinct()
        .orderBy("OriginalTwId"))

// COMMAND ----------

val sv_original_tweets = TTTsDF_sv_All.filter($"TweetType" === "Original Tweet").select($"CurrentTwID".as("OriginalTwId"),$"CurrentTweet",$"URLs",$"hashtags")
display(sv_original_tweets.orderBy("OriginalTwId"))

// COMMAND ----------

// MAGIC %md
// MAGIC Try other prominent Tweet Types...

// COMMAND ----------

display(TTTsDF_sv_All.filter($"TweetType" === "Quoted Tweet").select($"OriginalTwIDinQT".as("OriginalTwId"),$"CurrentTwID",$"CurrentTweet",$"URLs",$"hashtags")
        //.distinct()
       .orderBy("OriginalTwId"))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Now we join the exploded words with the Swedish lexicon with sentiment and polarity values

// COMMAND ----------

import org.apache.spark.ml.feature.RegexTokenizer

// this is a user-defined function to be passed to clean the TweetText
val cleanTweet = udf((s: String) => 
                    if (s == null) null 
                    else s
                          .replaceAll("(?U)(\\w+:\\/\\/\\S+)|(#\\w+\\S+)","") // remove patters with :// typical of urls
                          .replaceAll("(?U)(#\\w+\\S+)","") // remove hashtags
                          .replaceAll("(?U)(@\\w+\\S+)","") // remove mentions
                          .replaceAll("(?U)(RT\\s+)","") // remove RT
                          .toLowerCase()
                   )

val regexTok = new RegexTokenizer("regexTok")
  .setInputCol("cleanedCurrentTweet")
  .setPattern("(?U)\\W+")

val rDF = regexTok.transform(TTTsDF_sv_All
                              .withColumn("cleanedCurrentTweet", cleanTweet($"CurrentTweet"))
                            ).withColumn("TwWord", explode($"regexTok__output"))
                            .select("CurrentTwID","TwWord")
rDF.show(20)

// COMMAND ----------

rDF.count // number of words across all status updates' "CurrentTweet" 

// COMMAND ----------

val TweetIDAndPolarity = rDF
                           .join(lex,rDF.col("TwWord")===lex.col("word"))
                           .withColumn("strengthTimesConfidence",$"strength"*$"confidence")
                           .withColumn("strengthTimesRangedConfidence",$"strength"-(lit(1.0)-$"confidence")*($"strength"-($"strength"/abs($"strength"))))
                           .groupBy("CurrentTwID")
                           .agg(avg($"strength")
                                  .as("Avg_Polarity_Strength"),
                                avg($"strengthTimesConfidence")
                                  .as("Avg_Polarity_ConfWeightedStrength"),
                                avg($"strengthTimesRangedConfidence")
                                  .as("Avg_Polarity_RangedConfWeightedStrength")
                               ).withColumnRenamed("CurrentTwID","IDPCurrentTwID").cache

TweetIDAndPolarity.count // only these tweets have at least one word from sentiment lexicon

// COMMAND ----------

display(TweetIDAndPolarity)

// COMMAND ----------

TTTsDF_sv_All.count

// COMMAND ----------

//sv_TTTsDF.filter($"CurrentTweet"===null).count
TTTsDF_sv_All.filter($"CurrentTweet"===null).count

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Finally we have the averaged strength (of the polarity) of all the words in the tweet, also weighted by word-specific confidence. 

// COMMAND ----------

val TTTsDF_sv_All_withLexiconContainingTweets = TTTsDF_sv_All.join(TweetIDAndPolarity,
                                                                   TTTsDF_sv_All.col("CurrentTwID")===TweetIDAndPolarity.col("IDPCurrentTwID"),
                                                                   "LeftOuter").drop("IDPCurrentTwID")
                                                                    //.cache
TTTsDF_sv_All_withLexiconContainingTweets.count

// COMMAND ----------

display(TTTsDF_sv_All_withLexiconContainingTweets)

// COMMAND ----------

TTTsDF_sv_All_withLexiconContainingTweets.write.parquet("dbfs:///datasets/MEP/SE/TTTsDF_sv_All_withLexiconContainingTweets")

// COMMAND ----------

TTTsDF_sv_All_withLexiconContainingTweets.rdd.getNumPartitions

// COMMAND ----------

val TTTsDF_sv_All_withLexiconContainingTweets = spark.read.parquet("dbfs:///datasets/MEP/SE/TTTsDF_sv_All_withLexiconContainingTweets").cache()
TTTsDF_sv_All_withLexiconContainingTweets.count // 10030290

// COMMAND ----------

TTTsDF_sv_All_withLexiconContainingTweets.rdd.getNumPartitions // partition size is 20 on a 9 node spark cluster when reading back from parquet...

// COMMAND ----------

// MAGIC %md
// MAGIC ## TODO
// MAGIC 
// MAGIC // delete this later ("dbfs:///datasets/MEP/SE/svTTTsDFAllwithLexiconContainingTweets")

// COMMAND ----------

// delete this later ("dbfs:///datasets/MEP/SE/svTTTsDFAllwithLexiconContainingTweets")

// COMMAND ----------

// MAGIC %md
// MAGIC ### Example of using RegexTokenizer in a baby dataframe

// COMMAND ----------

import org.apache.spark.ml.feature.RegexTokenizer

// this is a user-defined function to be passed to clean the TweetText
val cleanTweet = udf((s: String) => 
                    if (s == null) null 
                    else s
                          .replaceAll("(?U)(\\w+:\\/\\/\\S+)|(#\\w+\\S+)","") // remove patters with :// typical of urls
                          .replaceAll("(?U)(#\\w+\\S+)","") // remove hashtags
                          .replaceAll("(?U)(@\\w+\\S+)","") // remove mentions
                          .replaceAll("(?U)(RT\\s+)","") // remove RT
                          .toLowerCase()
                   )

val regexTok = new RegexTokenizer("regexTok")
  .setInputCol("cleanText")
  .setPattern("(?U)\\W+")

//import org.apache.spark.ml.feature.StopWordsRemover
//val stopWords = new StopWordsRemover("stopWords")
//  .setInputCol(regexTok.getOutputCol)

val df = Seq("RT @sss ff","RT please find it @done (and empty)", "About to be luguber #rich", "empty överraskad http://ggg blah https://hjhhj.com/ghghgh.html http://ddd.com ggggoooo")
  .zipWithIndex
  .toDF("text", "id")
  .withColumn("cleanText", cleanTweet($"text"))

//stopWords.transform(regexTok.transform(df)).show(false)
val rDF = regexTok
//.transform(regexSansURLTok.setGaps(true)
  .transform(df)
  .withColumn("element", explode($"regexTok__output"))
rDF.show(false)

// COMMAND ----------

rDF.join(lex,rDF.col("element")===lex.col("word"))
   .select("text","element","word","strength","confidence")
   .show

// COMMAND ----------

