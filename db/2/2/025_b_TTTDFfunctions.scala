// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Tweet Transmission Tree Function
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program.
// MAGIC 
// MAGIC 
// MAGIC Please see the following notebook to understand the rationale for the Tweet Transmission Tree Functions:
// MAGIC * [http://lamastex.org/lmse/mep/src/TweetAnatomyAndTransmissionTree.html](http://lamastex.org/lmse/mep/src/TweetAnatomyAndTransmissionTree.htmll)
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016 Akinwande Atanda and Raazesh Sainudiin
// MAGIC 
// MAGIC Licensed under the Apache License, Version 2.0 (the "License");
// MAGIC you may not use this file except in compliance with the License.
// MAGIC You may obtain a copy of the License at
// MAGIC 
// MAGIC     http://www.apache.org/licenses/LICENSE-2.0
// MAGIC 
// MAGIC Unless required by applicable law or agreed to in writing, software
// MAGIC distributed under the License is distributed on an "AS IS" BASIS,
// MAGIC WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// MAGIC See the License for the specific language governing permissions and
// MAGIC limitations under the License.
// MAGIC ```

// COMMAND ----------

import org.apache.spark.sql.types.{StructType, StructField, StringType};
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.ColumnName
import org.apache.spark.sql.DataFrame

def fromParquetFile2DF(InputDFAsParquetFilePatternString: String): DataFrame = {
      sqlContext.
        read.parquet(InputDFAsParquetFilePatternString)
}

def tweetsJsonStringDF2TweetsDF(tweetsAsJsonStringInputDF: DataFrame): DataFrame = {
      sqlContext
        .read
        .json(tweetsAsJsonStringInputDF.map({case Row(val1: String) => val1}))
      }

def tweetsIDLong_JsonStringPairDF2TweetsDF(tweetsAsIDLong_JsonStringInputDF: DataFrame): DataFrame = {
      sqlContext
        .read
        .json(tweetsAsIDLong_JsonStringInputDF.map({case Row(val0:Long, val1: String) => val1}))
      }

def tweetsDF2TTTDF(tweetsInputDF: DataFrame): DataFrame = {
 tweetsInputDF.select(
  unix_timestamp($"createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CurrentTweetDate"),
  $"id".as("CurrentTwID"),
  unix_timestamp($"retweetedStatus.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CreationDateOfOrgTwInRT"), 
  $"retweetedStatus.id".as("OriginalTwIDinRT"),  
  unix_timestamp($"quotedStatus.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CreationDateOfOrgTwInQT"), 
  $"quotedStatus.id".as("OriginalTwIDinQT"), 
  $"inReplyToStatusId".as("OriginalTwIDinReply"), 
  $"user.id".as("CPostUserId"),
  unix_timestamp($"user.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("userCreatedAtDate"),
  $"retweetedStatus.user.id".as("OPostUserIdinRT"), 
  $"quotedStatus.user.id".as("OPostUserIdinQT"),
  $"inReplyToUserId".as("OPostUserIdinReply"),
  $"user.name".as("CPostUserName"), 
  $"retweetedStatus.user.name".as("OPostUserNameinRT"), 
  $"quotedStatus.user.name".as("OPostUserNameinQT"), 
  $"user.screenName".as("CPostUserSN"), 
  $"retweetedStatus.user.screenName".as("OPostUserSNinRT"), 
  $"quotedStatus.user.screenName".as("OPostUserSNinQT"),
  $"inReplyToScreenName".as("OPostUserSNinReply"),
  $"user.favouritesCount",
  $"user.followersCount",
  $"user.friendsCount",
  $"user.isVerified",
  $"user.isGeoEnabled",
  $"text".as("CurrentTweet"), 
  $"retweetedStatus.userMentionEntities.id".as("UMentionRTiD"), 
  $"retweetedStatus.userMentionEntities.screenName".as("UMentionRTsN"), 
  $"quotedStatus.userMentionEntities.id".as("UMentionQTiD"), 
  $"quotedStatus.userMentionEntities.screenName".as("UMentionQTsN"), 
  $"userMentionEntities.id".as("UMentionASiD"), 
  $"userMentionEntities.screenName".as("UMentionASsN")
 ).withColumn("TweetType",
    when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" === -1,
      "Original Tweet")
    .when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" > -1,
      "Reply Tweet")
    .when($"OriginalTwIDinRT".isNotNull &&$"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" === -1,
      "ReTweet")
    .when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" === -1,
      "Quoted Tweet")
    .when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" === -1,
      "Retweet of Quoted Tweet")
    .when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" > -1,
      "Retweet of Reply Tweet")
    .when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" > -1,
      "Reply of Quoted Tweet")
    .when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" > -1,
      "Retweet of Quoted Rely Tweet")
      .otherwise("Unclassified"))
.withColumn("MentionType", 
    when($"UMentionRTid".isNotNull && $"UMentionQTid".isNotNull, "RetweetAndQuotedMention")
    .when($"UMentionRTid".isNotNull && $"UMentionQTid".isNull, "RetweetMention")
    .when($"UMentionRTid".isNull && $"UMentionQTid".isNotNull, "QuotedMention")
    .when($"UMentionRTid".isNull && $"UMentionQTid".isNull, "AuthoredMention")
    .otherwise("NoMention"))
.withColumn("Weight", lit(1L))
}

def tweetsDF2TTTDFWithURLsAndHastags(tweetsInputDF: DataFrame): DataFrame = {
 tweetsInputDF.select(
  unix_timestamp($"createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CurrentTweetDate"),
  $"id".as("CurrentTwID"),
  unix_timestamp($"retweetedStatus.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CreationDateOfOrgTwInRT"), 
  $"retweetedStatus.id".as("OriginalTwIDinRT"),  
  unix_timestamp($"quotedStatus.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CreationDateOfOrgTwInQT"), 
  $"quotedStatus.id".as("OriginalTwIDinQT"), 
  $"inReplyToStatusId".as("OriginalTwIDinReply"), 
  $"user.id".as("CPostUserId"),
  unix_timestamp($"user.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("userCreatedAtDate"),
  $"retweetedStatus.user.id".as("OPostUserIdinRT"), 
  $"quotedStatus.user.id".as("OPostUserIdinQT"),
  $"inReplyToUserId".as("OPostUserIdinReply"),
  $"user.name".as("CPostUserName"), 
  $"retweetedStatus.user.name".as("OPostUserNameinRT"), 
  $"quotedStatus.user.name".as("OPostUserNameinQT"), 
  $"user.screenName".as("CPostUserSN"), 
  $"retweetedStatus.user.screenName".as("OPostUserSNinRT"), 
  $"quotedStatus.user.screenName".as("OPostUserSNinQT"),
  $"inReplyToScreenName".as("OPostUserSNinReply"),
  $"user.favouritesCount",
  $"user.followersCount",
  $"user.friendsCount",
  $"user.isVerified",
  $"user.isGeoEnabled",
  $"text".as("CurrentTweet"), 
  $"retweetedStatus.userMentionEntities.id".as("UMentionRTiD"), 
  $"retweetedStatus.userMentionEntities.screenName".as("UMentionRTsN"), 
  $"quotedStatus.userMentionEntities.id".as("UMentionQTiD"), 
  $"quotedStatus.userMentionEntities.screenName".as("UMentionQTsN"), 
  $"userMentionEntities.id".as("UMentionASiD"), 
  $"userMentionEntities.screenName".as("UMentionASsN"),
  $"urlEntities.expandedURL".as("URLs"),
  $"hashtagEntities.text".as("hashTags")
 ).withColumn("TweetType",
    when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" === -1,
      "Original Tweet")
    .when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" > -1,
      "Reply Tweet")
    .when($"OriginalTwIDinRT".isNotNull &&$"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" === -1,
      "ReTweet")
    .when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" === -1,
      "Quoted Tweet")
    .when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" === -1,
      "Retweet of Quoted Tweet")
    .when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" > -1,
      "Retweet of Reply Tweet")
    .when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" > -1,
      "Reply of Quoted Tweet")
    .when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" > -1,
      "Retweet of Quoted Rely Tweet")
      .otherwise("Unclassified"))
.withColumn("MentionType", 
    when($"UMentionRTid".isNotNull && $"UMentionQTid".isNotNull, "RetweetAndQuotedMention")
    .when($"UMentionRTid".isNotNull && $"UMentionQTid".isNull, "RetweetMention")
    .when($"UMentionRTid".isNull && $"UMentionQTid".isNotNull, "QuotedMention")
    .when($"UMentionRTid".isNull && $"UMentionQTid".isNull, "AuthoredMention")
    .otherwise("NoMention"))
.withColumn("Weight", lit(1L))
}

println("""USAGE: val df = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(fromParquetFile2DF("parquetFileName")))
                  val df = tweetsDF2TTTDF(tweetsIDLong_JsonStringPairDF2TweetsDF(fromParquetFile2DF("parquetFileName")))
                  """)