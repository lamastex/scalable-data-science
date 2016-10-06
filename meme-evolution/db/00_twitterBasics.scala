// Databricks notebook source exported at Thu, 6 Oct 2016 07:56:34 UTC
// MAGIC %md
// MAGIC # Cheat Sheet for Basics of Twitter API.
// MAGIC 
// MAGIC ### 2016 Raazesh Sainudiin
// MAGIC 
// MAGIC Those working on Project MEP should be familiar with these basics or remedy it asap.
// MAGIC 
// MAGIC 
// MAGIC ## MUST READs!
// MAGIC ### Developer Policy
// MAGIC * https://dev.twitter.com/overview/terms/policy
// MAGIC 
// MAGIC ### Case Studies
// MAGIC * https://dev.twitter.com/overview/case-studies
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
// MAGIC Raaz Needs to work more from here - Akin/Rania, feel free to fill the notes below as I did above by cloning this into your own home area. We can eventually chelate it together if you have valuable information we will all need
// MAGIC 
// MAGIC ### Object: Tweets
// MAGIC * most useful fields https://dev.twitter.com/overview/api/tweets
// MAGIC   * 
// MAGIC 
// MAGIC ### Object: Entitities
// MAGIC * most useful fields in https://dev.twitter.com/overview/api/entities
// MAGIC   * 
// MAGIC   
// MAGIC ### Object: Entities in Objects
// MAGIC * most important fields in https://dev.twitter.com/overview/api/entities-in-twitter-objects
// MAGIC   * 
// MAGIC   
// MAGIC ### Object: Places
// MAGIC * most important fields in https://dev.twitter.com/overview/api/places
// MAGIC   * 
// MAGIC   
// MAGIC   
// MAGIC ## REST API
// MAGIC 
// MAGIC * https://dev.twitter.com/rest/public
// MAGIC * https://dev.twitter.com/rest/reference/get/statuses/retweets/%3Aid
// MAGIC * https://dev.twitter.com/rest/reference/get/favorites/list
// MAGIC * https://dev.twitter.com/rest/reference/get/statuses/show/%3Aid
// MAGIC * https://dev.twitter.com/rest/reference/get/statuses/lookup
// MAGIC * https://dev.twitter.com/rest/reference/get/friends/list
// MAGIC 
// MAGIC ## Streaming API
// MAGIC 
// MAGIC * https://dev.twitter.com/streaming/overview
// MAGIC * https://dev.twitter.com/streaming/reference/post/statuses/filter
// MAGIC   * https://dev.twitter.com/streaming/overview/request-parameters#follow
// MAGIC   * https://dev.twitter.com/streaming/overview/request-parameters#track
// MAGIC   
// MAGIC ## Twitter Libraries
// MAGIC 
// MAGIC * https://dev.twitter.com/overview/api/twitter-libraries
// MAGIC   * http://twitter4j.org/
// MAGIC 
// MAGIC ## Cost of buying tweets from gnip
// MAGIC * http://support.gnip.com/articles/translating-plain-language-to-powertrack-rules.html
// MAGIC * http://support.gnip.com/sources/twitter/data_format.html#SamplePayloads
// MAGIC * http://support.gnip.com/apis/powertrack/rules.html
// MAGIC 
// MAGIC ## Other Useful Links, Sentiment Analysis, chatbots, nlp
// MAGIC 
// MAGIC * http://dev.datasift.com/docs/platform/csdl/csdl-examples/filtering-twitter-spam
// MAGIC * https://github.com/apache/bahir/tree/master/streaming-twitter/examples/src/main/scala/org/apache/spark/examples/streaming/twitter
// MAGIC * http://ampcamp.berkeley.edu/3/exercises/realtime-processing-with-spark-streaming.html
// MAGIC * https://developer.ibm.com/clouddataservices/sentiment-analysis-of-twitter-hashtags/
// MAGIC * https://github.com/ibm-cds-labs/spark.samples/blob/master/streaming-twitter/src/main/scala/com/ibm/cds/spark/samples/StreamingTwitter.scala
// MAGIC 
// MAGIC * https://www.codecourse.com/library/lessons/build-a-twitter-bot-laravel/introduction
// MAGIC * https://blog.monkeylearn.com/building-twitter-bot-with-php-machine-learning/

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Reading List
// MAGIC 
// MAGIC * http://www.the-american-interest.com/2016/07/10/when-and-why-nationalism-beats-globalism/
// MAGIC * 

// COMMAND ----------

// MAGIC %md
// MAGIC ## Data 1 minute 1% sampled at random
// MAGIC 
// MAGIC * http://res.suroot.com/twitter/

// COMMAND ----------

