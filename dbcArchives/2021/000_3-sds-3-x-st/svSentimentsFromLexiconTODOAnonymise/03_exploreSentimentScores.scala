// Databricks notebook source
// MAGIC %md
// MAGIC # Exploration of Sentiment Scored Swedish Tweets
// MAGIC 
// MAGIC This is a continuation of the DataFrame with 3,702,707 sv-language status updates that were written from the other notebook at `Workspace/Users/akerlundmathilda@gmail.com/lexiconSentimentForAllData`.
// MAGIC 
// MAGIC Here you can explore the results of the sentiment analysis.

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

// MAGIC %md
// MAGIC # To formalize "nullable sentiment scores", just apply sentiment scores when sentiment-score-related columns are non-NULL
// MAGIC 
// MAGIC 
// MAGIC We currently have two datasets, but the "Small dataset" should be deleted once we learn to ignore via a filet the null-valued sentiment scoring tweets in the Bigger dataset 

// COMMAND ----------

// MAGIC %md
// MAGIC ## Smaller dataste

// COMMAND ----------

val TTTsDF_sv_All_withLexiconContainingTweets = spark.read.parquet("dbfs:///datasets/MEP/SE/svTTTsDFAllwithLexiconContainingTweets").cache()
TTTsDF_sv_All_withLexiconContainingTweets.count

// COMMAND ----------

TTTsDF_sv_All_withLexiconContainingTweets.unpersist

// COMMAND ----------

// MAGIC %md
// MAGIC ## Bigger dataset
// MAGIC 
// MAGIC Need to decide what the null polarity scores mean first...

// COMMAND ----------

val TTTsDF_sv_All_withLexiconContainingTweets = spark.read.parquet("dbfs:///datasets/MEP/SE/TTTsDF_sv_All_withLexiconContainingTweets") //.cache()
TTTsDF_sv_All_withLexiconContainingTweets.count // 10030290

// COMMAND ----------

TTTsDF_sv_All_withLexiconContainingTweets.printSchema 
// we have two additional fields for polarity of sentiment from lexicon containing tweets
//|-- Avg_Polarity_Strength: double (nullable = true)
//|-- Avg_Polarity_ConfWeightedStrength: double (nullable = true)

// COMMAND ----------

display(TTTsDF_sv_All_withLexiconContainingTweets)

// COMMAND ----------

// MAGIC %md
// MAGIC Note all TweetTypes are computed for sentiment scores. For example, retweets of the same original tweet wll have the same sentiment score as verified below.

// COMMAND ----------

display(TTTsDF_sv_All_withLexiconContainingTweets.filter($"TweetType"==="ReTweet").orderBy("OriginalTwIDinRT"))

// COMMAND ----------

// MAGIC %md
// MAGIC Perhaps this is useful...
// MAGIC 
// MAGIC We could do more once we decide how to understand and visualise this best.

// COMMAND ----------

display(TTTsDF_sv_All_withLexiconContainingTweets.select("CurrentTweetDate","CPostUserId","CPostUserSN","CurrentTweet","Avg_Polarity_Strength","Avg_Polarity_ConfWeightedStrength"))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC See: https://lamastex.github.io/scalable-data-science/sds/2/2/db/021_recognizeActivityByRandomForest/

// COMMAND ----------

