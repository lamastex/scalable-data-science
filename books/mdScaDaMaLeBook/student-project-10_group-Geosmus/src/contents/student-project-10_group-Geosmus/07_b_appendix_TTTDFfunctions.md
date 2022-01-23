<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

**Note, this notebook has been edited slightly from the course notebook supplied by Raaz.**

</div>

<div class="cell markdown">

[ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)
==============================================================================================================================

</div>

<div class="cell markdown">

### Tweet Transmission Tree Function

This is part of *Project MEP: Meme Evolution Programme* and supported by databricks, AWS and a Swedish VR grant.

Please see the following notebook to understand the rationale for the Tweet Transmission Tree Functions: \* [http://lamastex.org/lmse/mep/src/TweetAnatomyAndTransmissionTree.html](http://lamastex.org/lmse/mep/src/TweetAnatomyAndTransmissionTree.htmll)

    Copyright 2016-2020 Akinwande Atanda and Raazesh Sainudiin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.types.{StructType, StructField, StringType};
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.ColumnName
import org.apache.spark.sql.DataFrame

spark.sql("set spark.sql.legacy.timeParserPolicy=LEGACY")

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
  $"lang".as("lang"),
  $"place.countryCode".as("countryCode"),
  //$"geo.coordinates".as("coordinates"),
  //$"geoLocation.latitude".as("lat"),
  //$"geoLocation.longitude".as("lon"),
  //unix_timestamp($"retweetedStatus.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CreationDateOfOrgTwInRT"), 
  //$"retweetedStatus.id".as("OriginalTwIDinRT"),  
  //unix_timestamp($"quotedStatus.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CreationDateOfOrgTwInQT"), 
  //$"quotedStatus.id".as("OriginalTwIDinQT"), 
  //$"inReplyToStatusId".as("OriginalTwIDinReply"), 
  //$"user.id".as("CPostUserId"),
  //unix_timestamp($"user.createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("userCreatedAtDate"),
  //$"retweetedStatus.user.id".as("OPostUserIdinRT"), 
  //$"quotedStatus.user.id".as("OPostUserIdinQT"),
  //$"inReplyToUserId".as("OPostUserIdinReply"),
  //$"user.name".as("CPostUserName"), 
  //$"retweetedStatus.user.name".as("OPostUserNameinRT"), 
  //$"quotedStatus.user.name".as("OPostUserNameinQT"), 
  //$"user.screenName".as("CPostUserSN"), 
  //$"retweetedStatus.user.screenName".as("OPostUserSNinRT"), 
  //$"quotedStatus.user.screenName".as("OPostUserSNinQT"),
  //$"inReplyToScreenName".as("OPostUserSNinReply"),
  $"user.favouritesCount",
  $"user.followersCount",
  $"user.friendsCount",
  //$"user.isVerified",
  $"user.isGeoEnabled",
  $"text".as("CurrentTweet"),
  //$"retweetedStatus.userMentionEntities.id".as("UMentionRTiD"), 
  //$"retweetedStatus.userMentionEntities.screenName".as("UMentionRTsN"), 
  //$"quotedStatus.userMentionEntities.id".as("UMentionQTiD"), 
  //$"quotedStatus.userMentionEntities.screenName".as("UMentionQTsN"), 
  //$"userMentionEntities.id".as("UMentionASiD"), 
  //$"userMentionEntities.screenName".as("UMentionASsN")
 )//.withColumn("TweetType",
    //when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" === -1,
    //  "Original Tweet")
    //.when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" > -1,
    //  "Reply Tweet")
   // .when($"OriginalTwIDinRT".isNotNull &&$"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" === -1,
     // "ReTweet")
    //.when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" === -1,
    //  "Quoted Tweet")
    //.when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" === -1,
    //  "Retweet of Quoted Tweet")
    //.when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNull && $"OriginalTwIDinReply" > -1,
    //  "Retweet of Reply Tweet")
    //.when($"OriginalTwIDinRT".isNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" > -1,
    //  "Reply of Quoted Tweet")
    //.when($"OriginalTwIDinRT".isNotNull && $"OriginalTwIDinQT".isNotNull && $"OriginalTwIDinReply" > -1,
    //  "Retweet of Quoted Rely Tweet")
    //  .otherwise("Unclassified"))
//.withColumn("MentionType", 
//    when($"UMentionRTid".isNotNull && $"UMentionQTid".isNotNull, "RetweetAndQuotedMention")
//    .when($"UMentionRTid".isNotNull && $"UMentionQTid".isNull, "RetweetMention")
//    .when($"UMentionRTid".isNull && $"UMentionQTid".isNotNull, "QuotedMention")
//    .when($"UMentionRTid".isNull && $"UMentionQTid".isNull, "AuthoredMention")
//    .otherwise("NoMention"))
//.withColumn("Weight", lit(1L))
}

def tweetsDF2TTTDFWithURLsAndHashtags(tweetsInputDF: DataFrame): DataFrame = {
 tweetsInputDF.select(
  unix_timestamp($"createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CurrentTweetDate"),
  $"id".as("CurrentTwID"),
  $"lang".as("lang"),
  $"geoLocation.latitude".as("lat"),
  $"geoLocation.longitude".as("lon"),
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
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// try to modify the function tweetsDF2TTTDF so some fields are not necessarily assumed to be available
// there are better ways - https://stackoverflow.com/questions/35904136/how-do-i-detect-if-a-spark-dataframe-has-a-column

def tweetsDF2TTTDFLightWeight(tweetsInputDF: DataFrame): DataFrame = {
 tweetsInputDF.select(
  unix_timestamp($"createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("CurrentTweetDate"),
  $"id".as("CurrentTwID"),
  $"lang".as("lang"),
  //$"geoLocation.latitude".as("lat"),
  //$"geoLocation.longitude".as("lon"),
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
```

</div>
