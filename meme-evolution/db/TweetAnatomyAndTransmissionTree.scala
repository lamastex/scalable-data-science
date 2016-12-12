// Databricks notebook source exported at Mon, 12 Dec 2016 15:10:36 UTC
// MAGIC %md
// MAGIC # Tweet Anatomy & Transmission Tree
// MAGIC 
// MAGIC ### 2016, Akinwande Atanda and Raazesh Sainudiin
// MAGIC 
// MAGIC 
// MAGIC This notebook describes the structure and key components of a tweet created by Twitter users. 
// MAGIC The components are used to generate unique categorizations of tweet types and construct the *Tweet Transmission Tree (TTT)*. 
// MAGIC The purpose of TTT (constructed in a Sprk Streaming job) is to encode various types of interactions betweet twitter users in continuous time by using appropriate attributes based on standard objects returned from [Twitter API for developers](https://dev.twitter.com/overview/api). 
// MAGIC 
// MAGIC TTT can be used to:
// MAGIC * define interractions among Twitter users as a tweet status is transmitted in continous time up to millisecond resolution
// MAGIC * exploit specific types of interactions among users to build networks and detect ideologically aligned communities 
// MAGIC * filter appropriate sets of tweets in the context of specific interaction for downstream Natural Language Processing (NLP), including sentiment analysis
// MAGIC * etc.
// MAGIC 
// MAGIC This is part of *[Project MEP: Meme Evolution Programme](http://lamastex.org/lmse/mep/)* and supported by databricks academic partners program.
// MAGIC 
// MAGIC The analysis is available in the following databricks notebook:
// MAGIC * [http://lamastex.org/lmse/mep/src/TweetAnatomyAndTransmissionTree.html](http://lamastex.org/lmse/mep/src/TweetAnatomyAndTransmissionTree.html)
// MAGIC 
// MAGIC For details on the mathematical model motivating the anatomy and categorizations of tweet transmission trees in the notebook see:
// MAGIC * The Transmission Process: A Combinatorial Stochastic Process for the Evolution of Transmission Trees over Networks, Raazesh Sainudiin and David Welch, Journal of Theoretical Biology, Volume 410, Pages 137–170, [10.1016/j.jtbi.2016.07.038](http://dx.doi.org/10.1016/j.jtbi.2016.07.038), 2016 
// MAGIC   * [preprint of the above paper as PDF 900KB](http://lamastex.org/preprints/20160806_transmissionProcJTBFinal.pdf).
// MAGIC 
// MAGIC Other resources that employ transmission trees and networks are summarized here:
// MAGIC * [http://lamastex.org/lmse/mep/](http://lamastex.org/lmse/mep/)
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016 Raazesh Sainudiin and Akinwande Atanda
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

// MAGIC %md
// MAGIC ### Developer Policy
// MAGIC 
// MAGIC Pay attention to Twitter's Developer Policy before reading this notebook further. 
// MAGIC There are limitations on the ways in which you may use the data from twitter.
// MAGIC 
// MAGIC * https://dev.twitter.com/overview/terms/policy
// MAGIC * https://dev.twitter.com/overview/terms/agreement

// COMMAND ----------

// MAGIC %md
// MAGIC Let us read in a collection tweets stored as json strings.

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/MEP/AkinTweet/sampleTweets@raazozone"))

// COMMAND ----------

// MAGIC %md
// MAGIC Let us read in this as a DataFrame by using the `.read.json` method as follows:

// COMMAND ----------

val tweetDF=sqlContext.read.json("dbfs:/datasets/MEP/AkinTweet/sampleTweets@raazozone/*")

// COMMAND ----------

// MAGIC %md
// MAGIC The `.read.json` method has automagically read in the attributes in the `json` string of each tweet. next let us find how many tweets there are by the `.count` action as follows:

// COMMAND ----------

tweetDF.count

// COMMAND ----------

// MAGIC %md
// MAGIC There are 12 rows or tweets in this `tweetDF`.  
// MAGIC Next, let us prin the schema of `tweetDF` to find out its various attributes.

// COMMAND ----------

tweetDF.printSchema()

// COMMAND ----------

// MAGIC %md
// MAGIC This gives us an idea of how much meta-data is contained in the 12 tweets we automagically read in from their json strings into the `tweetDF`.
// MAGIC 
// MAGIC We can view them using `display` command in databricks notebook as follows:

// COMMAND ----------

display(tweetDF)

// COMMAND ----------

// MAGIC %md
// MAGIC By using the horizontal and vertical scroll-bars above we can get a clear picture of the various columns or attributes of the 12 tweets.
// MAGIC 
// MAGIC Let us take a look at only some of the fields/attributes in more detail next.

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.ColumnName

display(tweetDf.select($"createdAt".as("CurrentTweetDate"),
                      $"id".as("CurrentTwID"),
                      $"retweetedStatus.createdAt".as("CreationDateOfOrgTwInRT"), 
                      $"retweetedStatus.id".as("OriginalTwIDinRT"),  
                      $"quotedStatus.createdAt".as("CreationDateOfOrgTwInQT"), 
                      $"quotedStatus.id".as("OriginalTwIDinQT"), 
                      $"inReplyToStatusId".as("OriginalTwIDinReply"), 
                      $"user.id".as("CPostUserId"), 
                      $"retweetedStatus.user.id".as("OPostUserIdinRT"), $"quotedStatus.user.id".as("OPostUserIdinQT"), $"user.name".as("CPostUserName"),$"retweetedStatus.user.name".as("OPostUserNameinRT"), $"quotedStatus.user.name".as("OPostUserNameinQT"), $"retweetedStatus.userMentionEntities.id".as("UMentionRTiD"), $"retweetedStatus.userMentionEntities.screenName".as("UMentionRTsN"), $"quotedStatus.userMentionEntities.id".as("UMentionQTiD"), $"quotedStatus.userMentionEntities.screenName".as("UMentionQTsN"), $"userMentionEntities.id".as("UMentionASiD"), $"userMentionEntities.screenName".as("UMentionASsN")))

// COMMAND ----------

// MAGIC %md
// MAGIC Let us select even fewer columns of the `tweetDF` and display them below.

// COMMAND ----------

display(tweetDf.select($"createdAt".as("CurrentTweetDate"),$"id".as("CurrentTwID"),$"retweetedStatus.createdAt".as("CreationDateOfOrgTwInRT"), $"retweetedStatus.id".as("OriginalTwIDinRT"),  $"quotedStatus.createdAt".as("CreationDateOfOrgTwInQT"), $"quotedStatus.id".as("OriginalTwIDinQT"), $"inReplyToStatusId".as("OriginalTwIDinReply"), $"text".as("CurrentTweet")))

// COMMAND ----------

// MAGIC %md
// MAGIC Our next objective is to appreciate what these various attributes actually mean.
// MAGIC We will accomplish this by first getting a **General Overview** of Tweet objects and entities from Twitter APIs with an emphasis on *Object: Users*. 
// MAGIC Links are provided for the reader to become familiar wit other objects and entities in twitter - a moving target!
// MAGIC 
// MAGIC Then, we will get a more specialized overview of the needed entities and objects as well as some key attributes for our purpose of constructing Tweet Transmission Trees (TTTs) that categorize tweets into various tweet-types.
// MAGIC 
// MAGIC # Tweet Anatomy - General Overview
// MAGIC 
// MAGIC ## API and Objects
// MAGIC 
// MAGIC * See https://dev.twitter.com/overview/api/
// MAGIC 
// MAGIC ### Object: Users
// MAGIC * most useful fields: https://dev.twitter.com/overview/api/users 
// MAGIC   * created_at,	String,	The UTC datetime that the user account was created on Twitter. 
// MAGIC   * description, String,	Nullable. The user-defined UTF-8 string describing their account. 
// MAGIC   * favourites_count, Int, The number of tweets this user has favorited in the account’s lifetime. 
// MAGIC   * followers_count,	Int,	The number of followers this account currently has. Under certain conditions of duress, this field will temporarily indicate “0.”
// MAGIC   * friends_count,	Int,	The number of users this account is following (AKA their “followings”). Under certain conditions of duress, this field will temporarily indicate “0.” 
// MAGIC   * geo_enabled,	Boolean,	When true it indicates that the user has enabled the possibility of geotagging their Tweets. This field must be true for the current user to attach geographic data when using POST statuses / update.
// MAGIC   * id,	Int64,	The integer representation of the unique identifier for this User.  
// MAGIC   * id_str,	String,	The string representation of the unique identifier for this User. 
// MAGIC   * lang,	String,	The BCP 47 code for the user’s self-declared user interface language. May or may not have anything to do with the content of their Tweets. 
// MAGIC   * listed_count,	Int,	The number of public lists that this user is a member of. 
// MAGIC   * location,	String,	Nullable. The user-defined location for this account’s profile. Not necessarily a location nor parseable. This field will occasionally be fuzzily interpreted by the Search service.
// MAGIC   * name,	String,	The name of the user as they’ve defined it. Not necessarily a person’s name. Typically capped at 20 characters, but subject to change.
// MAGIC   * protected,	Boolean,	When true it indicates that this user has chosen to protect their Tweets. See [About Public and Protected Tweets](https://support.twitter.com/articles/14016-about-public-and-protected-tweets). Protected Tweets may only be visible to your Twitter followers. When you protect your Tweets:
// MAGIC       * You’ll receive a request when new people want to follow you, which you can approve or deny. Learn more.
// MAGIC       * Your Tweets, including permanent links to your Tweets, will only be visible to your followers.
// MAGIC       * Your followers will not be able to use the Retweet button to Retweet or quote your Tweets. Learn more.
// MAGIC       * Protected Tweets will not appear in third-party search engines (like Google search or Bing).
// MAGIC       * Your protected Tweets will only be searchable on Twitter by you and your followers.
// MAGIC       * Replies you send to an account that isn’t following you will not be seen by that account (because only your followers will see your Tweets).
// MAGIC 
// MAGIC   * screen_name,	String,	The screen name, handle, or alias that this user identifies themselves with. screen_names are unique but subject to change. Use id_str as a user identifier whenever possible. 
// MAGIC   * status,	Tweets,	Nullable. If possible, the user’s most recent tweet or retweet. In some circumstances, this data cannot be provided and this field will be omitted, null, or empty. Perspectival attributes within tweets embedded within users cannot always be relied upon.
// MAGIC   * statuses_count,	Int,	The number of tweets (including retweets) issued by the user. 
// MAGIC   * time_zone,	String,	Nullable. A string describing the Time Zone this user declares themselves within. 
// MAGIC   * url,	String,	Nullable. A URL provided by the user in association with their profile. 
// MAGIC   * utc_offset,	Int,	Nullable. The offset from GMT/UTC in seconds. 
// MAGIC   * verified,	Boolean,	When true, indicates that the user has a verified account. 
// MAGIC   * withheld_in_countries,	Array of String,	When present, indicates a textual representation of the two-letter country codes this user is withheld from. 
// MAGIC   * withheld_scope,	String,	When present, indicates whether the content being withheld is the “status” or a “user.”
// MAGIC   
// MAGIC 
// MAGIC ### Object: Tweets
// MAGIC * see https://dev.twitter.com/overview/api/tweets
// MAGIC 
// MAGIC ### Object: Entitities
// MAGIC * see https://dev.twitter.com/overview/api/entities
// MAGIC   
// MAGIC ### Object: Entities in Objects
// MAGIC * see https://dev.twitter.com/overview/api/entities-in-twitter-objects
// MAGIC   
// MAGIC ### Object: Places
// MAGIC * see https://dev.twitter.com/overview/api/places
// MAGIC   
// MAGIC ## Working with twitter API and libraries
// MAGIC   In order to observe, extract, transform, load and interact with tweets we need to use the following REST and STreaming APIs and accompanying libraries.
// MAGIC   
// MAGIC ### REST API
// MAGIC 
// MAGIC * https://dev.twitter.com/rest/public
// MAGIC * https://dev.twitter.com/rest/reference/get/statuses/retweets/%3Aid
// MAGIC * https://dev.twitter.com/rest/reference/get/favorites/list
// MAGIC * https://dev.twitter.com/rest/reference/get/statuses/show/%3Aid
// MAGIC * https://dev.twitter.com/rest/reference/get/statuses/lookup
// MAGIC * https://dev.twitter.com/rest/reference/get/friends/list
// MAGIC * https://dev.twitter.com/rest/reference/get/statuses/user_timeline
// MAGIC 
// MAGIC ### Streaming API
// MAGIC 
// MAGIC * https://dev.twitter.com/streaming/overview
// MAGIC * https://dev.twitter.com/streaming/reference/post/statuses/filter
// MAGIC   * https://dev.twitter.com/streaming/overview/request-parameters#follow
// MAGIC   * https://dev.twitter.com/streaming/overview/request-parameters#track
// MAGIC   
// MAGIC ### Twitter Libraries
// MAGIC 
// MAGIC * https://dev.twitter.com/overview/api/twitter-libraries
// MAGIC   * http://twitter4j.org/
// MAGIC   * http://twitter4j.org/javadoc/index-all.html
// MAGIC   

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Tweet Anatomy - Specialized for Constructing TTT
// MAGIC 
// MAGIC The structure in terms of JSON objects and entities associated with major tweet or status actions are described as follows:
// MAGIC * I. Tweets
// MAGIC * II. Tweets: Reply
// MAGIC * III. Tweets: Quote (/ Comments)
// MAGIC * IV. Tweets: Retweet
// MAGIC 
// MAGIC ## Tweet Transmission Tree (TTT)
// MAGIC 
// MAGIC ### I. Tweets
// MAGIC When a tweet is created independnetly (such as **Authored Tweet**) or dependently with another previous tweet(s). Twitter Streaming API returns specific unique objects and entities.
// MAGIC 
// MAGIC #### Tweet Objects: Authored/ Original
// MAGIC * `text`: The actual text of the status update. Text post or status update with 140 characters limit per post. It can be truncated, replied to, like, unliked, deleted, and retweeted.
// MAGIC * `created_at`: Time when a Tweet was created
// MAGIC * `favorite_count`: Indicates approximately how many times this Tweet has been “liked” by Twitter users.
// MAGIC * `Id`: the unique identifier for this Tweet. It is also known as “Twitter ID”
// MAGIC * `User`: The user who posted this Tweet. The attributes embedded within this object include:
// MAGIC   * `Statuses_count`, `favourites_count`, `name`, `listed_count`, `description`, `followers_count`, `location`, `screen_name`, `friends_count`, `id`, `created_at`, `entities: hashtags, urls, and user_mentions`
// MAGIC 
// MAGIC #### Tweet Entities:
// MAGIC Entities provide metadata and additional information about content posted on Twitter. Entities are returned wherever Tweets are found. Some of the entities (Array of object) embedded with a tweet or status update are:
// MAGIC * `Hashtags`: Represents hashtags which have been parsed out of the Tweet text. 
// MAGIC * `Media`: Represents media elements uploaded with the Tweet.
// MAGIC * `Urls`: Represents URLs included in the text of a Tweet
// MAGIC * `User_mentions`: Represents other Twitter users mentioned in the text of the Tweet. The `user_mentions` contains keys like `name` (Display name of the referenced user), `screen_name` (Screen name of the referenced user) and `id` (ID of the mentioned user, as an integer).
// MAGIC 
// MAGIC #### User Entities:
// MAGIC Entities for User Objects describe information about the user as it appears in their defined profile. User entities can apply to multiple fields within its parent object. It can apply to authored tweet, retweet or quote. But id does not describe or appear in hashtags and user_mention entities. The entities either for a current status update or in retweeted_status contain the following keys: 
// MAGIC * `Id`
// MAGIC * `name`
// MAGIC * `screen_name`
// MAGIC * `location`
// MAGIC * `description`
// MAGIC 
// MAGIC ### II. Tweets: Reply
// MAGIC When a Tweet is replied to (whom, what), it comes with following entities:
// MAGIC * `in_reply_to_screen_name`: If the represented Tweet is a reply, this field will contain the screen name of the **original Tweet’s author**.
// MAGIC * `in_reply_to_status_id`: If the represented Tweet is a reply, this field will contain the integer representation of the **original Tweet’s ID**. Otherwise, an integer **-1** is returned.
// MAGIC * `in_reply_to_user_id`: If the represented Tweet is a reply, this field will contain the integer representation of the **original Tweet’s author ID**. This will not necessarily always be the user directly mentioned in the Tweet.
// MAGIC 
// MAGIC ### III. Tweets: Quote (/ Comments)
// MAGIC When an authored or original Tweet is quoted in a tweet/text/ status update (i.e. comments), it comes with the following entities:
// MAGIC * `quoted_status_id`: This field only surfaces when the Tweet is a quote Tweet. This field contains the integer value Tweet ID of the quoted Tweet. Otherwise, it returns **null**.
// MAGIC * `quoted_status`: This field only surfaces when the Tweet is a quote Tweet. This attribute contains the Tweet object of the original Tweet that was quoted.
// MAGIC 
// MAGIC ### IV. Tweets: Retweet
// MAGIC A Retweet is a special kind of Tweet that contains the original Tweet as an embedded **retweeted_status** object. For consistency, the top-level Retweet object also has a text property and associated entities. 
// MAGIC The Retweet text attribute is composed of the original Tweet text with “RT @username: ” prepended.
// MAGIC The best practice is to retrieve the text, original author and date from the original Tweet in **retweeted_status** whenever this exists.
// MAGIC * `retweet_count`: Number of times a particular Tweet has been retweeted
// MAGIC * `retweeted_status`: Users can amplify the broadcast of tweets authored by other users by retweeting. Retweets can be distinguished from typical Tweets by the existence of a retweeted_status attribute. This attribute contains a representation of the original Tweet that was retweeted. Note that **retweets of retweets do not show representations of the intermediary retweet**, but only the original tweet. 
// MAGIC * `retweeted_status_id`: The unique tweet ID of the original tweet is returned under this object. If a tweet is not retweeted, the object returns **null**
// MAGIC 
// MAGIC #### Retweet Entities:
// MAGIC All entities associated with a Retweet are embedded in the `retweeted_status` object. It contains information about the original tweet such as **text and original tweet entities** (`hashtags`, `urls`, and `user_mentions` (`screen_name`, `name`, `id`) ).

// COMMAND ----------

// MAGIC %md
// MAGIC ## Tweet Transmission Tree - with tweet-types
// MAGIC The described anatomy of tweets can be used to deterministically classify or categorize tweets into unique categories or *tweet-types* based on the objects/entities in a tweet status. Theoretically, each tweet status can be classified into eight (8) types or classes based on the unique values for its objects/entities:
// MAGIC 
// MAGIC * **$"retweetedStatus.id"**
// MAGIC * **$"quotedStatus.id"**
// MAGIC * **$"inReplyToStatusId"**
// MAGIC 
// MAGIC Each of the object has been given alias or renamed for the purpose of simplicity. The table below shows the 8 classes that are described below in more detail.
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/ClassesOfTweets.JPG)
// MAGIC 
// MAGIC 
// MAGIC ## 1. Original Tweet
// MAGIC 
// MAGIC A tweet is an original or authored tweet if it has not been previously retweeted, quoted or replied to. In this case all the three unique collection of status ID objects [**$"retweetedStatus.id"**,  **$"quotedStatus.id"**, and **$"inReplyToStatusId"**] are **null**
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/OriginalTweet.JPG)
// MAGIC 
// MAGIC ## 2. Retweet
// MAGIC 
// MAGIC A stream of tweet is classified as retweet if the retweeted status, **$"retweetedStatus.id"** is **Not Null**. This type of tweet can either be original tweet or reply tweet. The value of the object **$"retweetedStatus.id"** corresponds to the original tweet ID of the status being retweeted.
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/Retweet.JPG)
// MAGIC 
// MAGIC ## 3. Quoted Tweet
// MAGIC 
// MAGIC When a comment is made about a tweet or a previous tweet is quoted without using the web-link of such tweet, it is classified as quoted tweet. For this classification, the **$"quotedStatus.id"** is **Not Null** . The object, **$"quotedStatus.id"** returns the actual ID of the tweet being quoted.
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/QuotedTweet.JPG)
// MAGIC 
// MAGIC ## 4. Reply Tweet
// MAGIC 
// MAGIC Reply tweet is streamed with the classifer object, **$"inReplyToStatusId"** returned with an integer **greater than -1**. The object indicates the actual unique tweet ID being replied to  by the user. On a web browser, the reply tweet is published and treated as a new status for subsequent actions. This is explained further under the sequence of action section. 
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/ReplyTweet.JPG)
// MAGIC 
// MAGIC ## 5. Retweet of Quoted Tweet
// MAGIC 
// MAGIC For a retweet of quoted tweet, the returned values for the objects, **$"retweetedStatus.id"** and **$"quotedStatus.id"** are **Not Null**. The  **$"retweetedStatus.id"** indicates the ID of the comments and **$"quotedStatus.id"** shows the ID of the original tweet being commented about the second user before being retweeted by the third user (*see Sequence of Action for details*)
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/RetweetOfQuotedTweet.JPG)
// MAGIC 
// MAGIC ## 6. Reply of Quoted Tweet
// MAGIC 
// MAGIC If a tweet is replied to with a **web-link or embeded link of previous tweet**, the values of the objects, **$"quotedStatus.id"** and **$"inReplyToStatusId"** are respectively **Not Null** and **greater than -1**. The ID of the historical tweet quoted in the reply and the ID of the tweet being replied to by the user are returned. 
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/ReplyOfQuotedTweet.JPG)
// MAGIC 
// MAGIC ## 7. Retweet of Reply Tweet
// MAGIC 
// MAGIC Practically, this is treated as a **Retweet**. This makes the returned retweeted status object, **$"retweetedStatus.id"** to be **Not Null**, while the **$"inReplyToStatusId"** object is equal to **-1**
// MAGIC 
// MAGIC ## 8. Retweet of Quoted Reply Tweet
// MAGIC 
// MAGIC Similarly based on the operationalization of tweet eco-system, this category is not feasible practically as expected theoretically.
// MAGIC It is treated as **Retweet of Quoted Tweet**. 
// MAGIC 
// MAGIC 
// MAGIC ## Sequence of Actions in Twitter
// MAGIC 
// MAGIC To better understand how reply and comment are treated in a sequence of tweet actions, the table below provide more details.
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/actionsAndClasses.JPG)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## User Mention Interraction
// MAGIC 
// MAGIC The Tweet Transmission Tree (TTT) can further be extended to incorporate user mention interactions for filtering or building network purpose.
// MAGIC 
// MAGIC Irrespective of the type of tweets, all users mentioned or cited in a body of tweet text/ status are stored in the object **$"userMentionEntities"**. The entities has the objects:
// MAGIC 
// MAGIC * **$"userMentionEntities.id"**: ID of the mentioned user, as an integer.
// MAGIC * **$"userMentionEntities.name"**: Display name of the referenced user. 
// MAGIC * **$"userMentionEntities.screenName"**: Screen name of the referenced user.
// MAGIC 
// MAGIC However, user mentioned in a retweet or quoted tweet are in the Array objects under **$"retweetedStatus.userMentionEntities"** and **$"quotedStatus.userMentionEntities"** respectively. Since ID is unique to each Twitter user, we used it to design the mention classification as:
// MAGIC * **Authored Mention**:
// MAGIC 
// MAGIC When a user screen name is referenced in an original tweet or reply tweet, it is classified as Authored Mention. In this case, the ID of the user mentioned is only stated under the object **$"userMentionEntities.id"**. 
// MAGIC 
// MAGIC * **Retweet Mention**:
// MAGIC 
// MAGIC For all referenced user IDs in a retweet type only, the **$"userMentionEntities.id"** is **Not Null** under the object **$"retweetedStatus"**.
// MAGIC 
// MAGIC * **Quoted Mention**:
// MAGIC 
// MAGIC For all referenced user IDs in a quoted type only, the **$"userMentionEntities.id"** is **Not Null** under the object **$"quotedStatus"**.
// MAGIC 
// MAGIC * **Retweet-Quoted Mention**:
// MAGIC 
// MAGIC For all referenced user IDs in a retweet of quoted tweet type, the **$"userMentionEntities.id"** is **Not Null** under the objects **$"retweetedStatus"** and **$"quotedStatus"**.
// MAGIC 
// MAGIC * **No Mention**:
// MAGIC 
// MAGIC When no user is mentioned in an original, reply, quoted or rewteet tweets or any other classified types of tweets.
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/classesOfMention.JPG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Tweet Transmission Tree (TTT) Function
// MAGIC To perform the categorization of tweet-types, the `TTT functions` are implemented in the next cell to generate additional columns of **"TweetType"** and **"MentionType"**. The last three conditional classifications (`Retweet of Reply Tweet`, `Retweet of Quoted Reply Tweet`, and `Unclassified`) are to mainly capture future unique interactions and yet to be validated thoroughly. If the function is implemented for your streamed tweets and any of the three classifcations come-up, kindly contact the authors.
// MAGIC 
// MAGIC Examples of the retrieved tweets and corresponding classification type using the TTT functions in the following cell are:
// MAGIC 
// MAGIC ![](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/meme-evolution/db/figures/Capture.JPG)

// COMMAND ----------

//TTT Functions
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

println("""USAGE: val df = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(fromParquetFile2DF("parquetFileName")))

                    """)

// COMMAND ----------

val tweetDF=sqlContext.read.json("dbfs:/datasets/MEP/AkinTweet/sampleTweets@raazozone/*")

// COMMAND ----------

val TTTDF = tweetsDF2TTTDF(tweetDF)

// COMMAND ----------

display(TTTDF)

// COMMAND ----------

display(TTTDF.select($"CurrentTweetDate",$"CurrentTwID",$"CurrentTweet",$"TweetType",$"MentionType"))

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC Count the number of tweets in the TTT Table

// COMMAND ----------

StreamedTweetsfClassDF.count()

// COMMAND ----------

// MAGIC %md
// MAGIC As shown in the schema and displayed table, the objects have been renamed. Details of the alias given to each selected object in the TTT function are described in Step 4 above.

// COMMAND ----------

StreamedTweetsfClassDF.printSchema()

// COMMAND ----------

StreamedTweetsfClassDF.take(5)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Step 5: Exploratory Analysis
// MAGIC The tweets dataset for the TTT table can be explored using either SQL operators or standard DataFrame operators

// COMMAND ----------

// MAGIC %md
// MAGIC **Step 5.1: Tweets Exploration using DataFrame Operators**

// COMMAND ----------

// MAGIC %md
// MAGIC **Number of Tweets by Type**

// COMMAND ----------

display(StreamedTweetsfClassDF.groupBy($"tweetType").count().orderBy($"count".desc))

// COMMAND ----------

display(StreamedTweetsfClassDF.groupBy($"tweetType").count().orderBy($"count".desc))

// COMMAND ----------

// MAGIC %md
// MAGIC **Number of Tweets by Verified Users**

// COMMAND ----------

display(StreamedTweetsfClassDF.filter($"IsVerified"===true).groupBy($"tweetType").count().orderBy($"count".desc))

// COMMAND ----------

// MAGIC %md
// MAGIC **Number of Tweets by Users who enabled their Geographical location**

// COMMAND ----------

display(StreamedTweetsfClassDF.filter($"IsGeoEnabled"===true).groupBy($"tweetType").count().orderBy($"count".desc))

// COMMAND ----------

// MAGIC %md
// MAGIC **Number of Retweets by Distinct Users for each Tweets Created by Democrats and Republicans Members**

// COMMAND ----------

display(
  StreamedTweetsfClassDF.filter($"tweetType"==="ReTweet")
  .groupBy($"OPostUserSNinRT",$"CPostUserSN")
  .agg(max("followersCount"),max("friendsCount"),sum("Weight").alias("ReTweetCount"))
  .orderBy($"ReTweetCount".desc)
)

// COMMAND ----------

// MAGIC %md
// MAGIC **Trump's & Clinton's Status Retweeted by Users with 2 or More Followers and Friends**

// COMMAND ----------

val minimumAgeSinceAccountCreatedInDays=100

// COMMAND ----------

val TrumpClintonRetweets = StreamedTweetsfClassDF
  .withColumn("now", lit(current_timestamp()))
                                .withColumn("daysSinceUserCreated",datediff($"now",$"userCreatedAtDate"))
                                .drop($"now")
                                .filter($"daysSinceUserCreated">minimumAgeSinceAccountCreatedInDays)
  .filter($"tweetType"==="ReTweet")
  .filter($"OPostUserSNinRT"==="realDonaldTrump" || $"OPostUserSNinRT"==="HillaryClinton")
  .filter($"followersCount">2 && $"friendsCount">2)// filtering accounts with <3 friends or followers
  .cache()

display(TrumpClintonRetweets)

// COMMAND ----------

// MAGIC %md
// MAGIC ** Filtered Retweets for Trump and Clinton **

// COMMAND ----------

val TrumpClintonRetweetPairs = StreamedTweetsfClassDF
  .withColumn("now", lit(current_timestamp()))
                                .withColumn("daysSinceUserCreated",datediff($"now",$"userCreatedAtDate"))
                                .drop($"now")
                                .filter($"daysSinceUserCreated">minimumAgeSinceAccountCreatedInDays)
  .filter($"tweetType"==="ReTweet")
  .filter($"OPostUserSNinRT"==="realDonaldTrump" || $"OPostUserSNinRT"==="HillaryClinton")
  .groupBy($"userCreatedAtDate",$"daysSinceUserCreated",$"OPostUserSNinRT",$"CPostUserSN")
  .agg(max("favouritesCount"),max("followersCount"),max("friendsCount"),sum("Weight").alias("ReTweetCount"))
  .filter($"max(followersCount)">2 && $"max(friendsCount)">2)// filtering accounts with <3 friends or followers
  .orderBy($"ReTweetCount".desc)
  .cache()

display(TrumpClintonRetweetPairs)

// COMMAND ----------

TrumpClintonRetweetPairs.agg(sum($"RetweetCount")).show() //Sum of all retweets based on the >100 minimum account age filter

// COMMAND ----------

// MAGIC %md
// MAGIC **Filtered Retweets for Trump and Clinton by Geographical Enabled Users**

// COMMAND ----------

val TrumpClintonRetweetPairsGeoEnabled = StreamedTweetsfClassDF
  .withColumn("now", lit(current_timestamp()))
                                .withColumn("daysSinceUserCreated",datediff($"now",$"userCreatedAtDate"))
                                .drop($"now")
                                .filter($"daysSinceUserCreated">minimumAgeSinceAccountCreatedInDays)
  .filter($"IsGeoEnabled"===true)
  .filter($"tweetType"==="ReTweet")
  .filter($"OPostUserSNinRT"==="realDonaldTrump" || $"OPostUserSNinRT"==="HillaryClinton")
  .groupBy($"userCreatedAtDate",$"daysSinceUserCreated",$"OPostUserSNinRT",$"CPostUserSN")
  .agg(max("favouritesCount"),max("followersCount"),max("friendsCount"),sum("Weight").alias("ReTweetCount"))
  .filter($"max(followersCount)">2 && $"max(friendsCount)">2)// filtering accounts with <3 friends or followers
  .orderBy($"ReTweetCount".desc)
  .cache()

display(TrumpClintonRetweetPairsGeoEnabled)

// COMMAND ----------

TrumpClintonRetweetPairsGeoEnabled.agg(sum($"RetweetCount")).show() //Sum of all retweets based on the >100 minimum account age filter

// COMMAND ----------

// MAGIC %md
// MAGIC **Filtered Retweets for Trump and Clinton by Users with Verified Account**

// COMMAND ----------

val TrumpClintonRetweetPairsVerified = StreamedTweetsfClassDF
  .withColumn("now", lit(current_timestamp()))
                                .withColumn("daysSinceUserCreated",datediff($"now",$"userCreatedAtDate"))
                                .drop($"now")
                                .filter($"daysSinceUserCreated">minimumAgeSinceAccountCreatedInDays)
  .filter($"IsVerified"===true)
  .filter($"tweetType"==="ReTweet")
  .filter($"OPostUserSNinRT"==="realDonaldTrump" || $"OPostUserSNinRT"==="HillaryClinton")
  .groupBy($"userCreatedAtDate",$"daysSinceUserCreated",$"OPostUserSNinRT",$"CPostUserSN",$"CPostUserID")
  .agg(max("favouritesCount"),max("followersCount"),max("friendsCount"),sum("Weight").alias("ReTweetCount"))
  .filter($"max(followersCount)">2 && $"max(friendsCount)">2)// filtering accounts with <3 friends or followers
  .orderBy($"ReTweetCount".desc)
  .cache()

display(TrumpClintonRetweetPairsVerified)

// COMMAND ----------

TrumpClintonRetweetPairsVerified.agg(sum($"RetweetCount")).show() //Sum of all retweets based on the >100 minimum account age filter

// COMMAND ----------

// MAGIC %md
// MAGIC **Step 5.2: Register the Data Frame as a Table and perform SQL operations**

// COMMAND ----------

StreamedTweetsfClassDF.registerTempTable("TTT")

// COMMAND ----------

// MAGIC %md
// MAGIC **Description of the SQL Table**

// COMMAND ----------

display(sql("""
DESCRIBE TTT
"""))

// COMMAND ----------

// MAGIC %md
// MAGIC **Number of Tweets by Type**

// COMMAND ----------

display(sql("""
SELECT count(*) as count, TweetType
FROM TTT
GROUP BY TweetType
ORDER BY count DESC
"""))

// COMMAND ----------

// MAGIC %md
// MAGIC **Number of User Mention by Type**

// COMMAND ----------

display(sql("""
SELECT count(*) as count, MentionType
FROM TTT
GROUP BY MentionType
ORDER BY count DESC
"""))

// COMMAND ----------

// MAGIC %md
// MAGIC **To validate the TTT function, the following number of tweet and mention type must be equal:**
// MAGIC 
// MAGIC * Retweet                                    ===  Retweet Mention
// MAGIC * Original + Reply Tweet                     === Authored Mention
// MAGIC * Quoted Tweet + Reply of Quoted Tweet       === Quoted Mention
// MAGIC * Retweet of Quoted Tweet                    === Retweet_and_Quoted Mention

// COMMAND ----------

// MAGIC %md
// MAGIC **Sources of Retweet and Retweet of Quoted Tweet by Original Authors**

// COMMAND ----------

display(sql("""
SELECT COUNT(OPostUserNameinRT) as Count, OPostUserNameinRT, tweettype
FROM TTT
GROUP BY OPostUserNameinRT, tweettype
ORDER BY Count DESC, OPostUserNameinRT DESC
LIMIT 10
"""))

// COMMAND ----------

display(sql("""
SELECT COUNT(OPostUserNameinRT) as Count, OPostUserNameinRT, tweettype
FROM TTT
GROUP BY OPostUserNameinRT, tweettype
ORDER BY Count DESC, OPostUserNameinRT DESC
LIMIT 5
"""))

// COMMAND ----------

// MAGIC %md
// MAGIC **Sources of Retweet Mentions by Users of Original Post**

// COMMAND ----------

display(sql("""
SELECT COUNT(OPostUserNameinRT) as Count, OPostUserNameinRT, MentionType
FROM TTT
GROUP BY OPostUserNameinRT, MentionType
ORDER BY Count DESC, OPostUserNameinRT DESC
LIMIT 10
"""))

// COMMAND ----------

// MAGIC %md
// MAGIC **Frequency of each distinct User Tweets being Retweeted**

// COMMAND ----------

display(sql("""
SELECT COUNT(OPostUserNameinRT) as Count, OPostUserNameinRT, tweettype
FROM TTT
WHERE tweettype == 'ReTweet'
GROUP BY OPostUserNameinRT, tweettype
ORDER BY Count DESC, OPostUserNameinRT DESC
LIMIT 10
"""))

// COMMAND ----------

// MAGIC %md
// MAGIC **Number of times a User (CPostUserSN) Retweeted other Users (OPostUserNameinRT) previous Status**

// COMMAND ----------

display(sql("""
SELECT COUNT(CPostUserSN) as Count, CPostUserSN, OPostUserNameinRT, TweetType
FROM TTT
WHERE TweetType == 'ReTweet'
GROUP BY CPostUserSN, OPostUserNameinRT, TweetType
ORDER BY CPostUserSN ASC, Count DESC
LIMIT 10
"""))

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// unpersist the cached DFs
StreamedTweetsPQ.unpersist()

StreamedTweetsfDF.unpersist()

StreamedTweetsfClassDF.unpersist()