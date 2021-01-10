// Databricks notebook source
// MAGIC %md
// MAGIC # Polarity of SWEDIESH tweets using LEXICON
// MAGIC THE LEXICON was obtained from https://spraakbanken.gu.se/eng/resource/sentimentlex

// COMMAND ----------

val lexAll = spark.sql("select * from sentimentlex_csv")
display(lexAll.select("*"))

// COMMAND ----------

display(lexAll.drop("example").describe())

// COMMAND ----------

lexAll.select("confidence").distinct.show

// COMMAND ----------

val lex = spark.sql("SELECT word,strength,confidence FROM sentimentlex_csv")
display(lex.select("*"))

// COMMAND ----------

lex.count

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

val rawDF = fromParquetFile2DF("/datasets/MEP/SE/Streaming/2018/06/03/*/*")
val tweetsDF = tweetsJsonStringDF2TweetsDF(rawDF)

// COMMAND ----------

display(tweetsDF.select("lang").groupBy("lang").count.orderBy($"count".desc))

// COMMAND ----------

val sv_tweets = tweetsDF.filter($"lang" === "sv")
//sv_tweets.count

// Make a smaller dataframe by selecting only some columns
// Do not .cache() if reading a LOT of data
val sv_TTTsDF = tweetsDF2TTTDFWithURLsAndHashtags(sv_tweets).cache()

// COMMAND ----------

sv_TTTsDF.count

// COMMAND ----------

sv_TTTsDF.printSchema

// COMMAND ----------

//display(sv_TTTsDF)

// COMMAND ----------

val sv_original_tweets = sv_TTTsDF.filter($"TweetType" === "Original Tweet" ).select("CurrentTwID","CurrentTweet","URLs","hashtags")
display(sv_original_tweets)

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

val rDF = regexTok.transform(sv_original_tweets
                              .withColumn("cleanedCurrentTweet", cleanTweet($"CurrentTweet"))
                            ).withColumn("TwWord", explode($"regexTok__output"))
                            .select("CurrentTwID","TwWord")
rDF.show(20)

// COMMAND ----------

// MAGIC %md
// MAGIC Read http://www.ep.liu.se/ecp/126/006/ecp16126006.pdf
// MAGIC 
// MAGIC Try to come up with a better interpretation of confidence-corrected polarity (CCP):
// MAGIC 
// MAGIC $$ s=strength , c=confidence, \qquad CCP = \begin{cases} s-(1-c)*(s-sign(s)) & if \quad c<1 \\\\ s & if \quad c =1 \end{cases}$$

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
                               )

TweetIDAndPolarity
  //.filter($"strength" < -2.0)
  //.sample(false,0.01)
  .show(40)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Finally we have the averaged strength (of the polarity) of all the words in the tweet, also weighted by word-specific confidence. 

// COMMAND ----------

val sv_original_tweets_withLexiconValues = sv_original_tweets.join(TweetIDAndPolarity,"CurrentTwID")
display(sv_original_tweets_withLexiconValues)

// COMMAND ----------

sv_original_tweets_withLexiconValues.show(100)

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

val df = Seq("RT please find it @done (and empty)", "About to be luguber #rich", "empty överraskad http://ggg blah https://hjhhj.com/ghghgh.html http://ddd.com ggggoooo")
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

