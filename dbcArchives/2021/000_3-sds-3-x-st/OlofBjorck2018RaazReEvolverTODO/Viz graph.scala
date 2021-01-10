// Databricks notebook source
// MAGIC %md
// MAGIC # Explore a Twitter graph
// MAGIC 
// MAGIC by Olof Björck, Joakim Johansson, Rania Sahioun, Raazesh Sainudiin and Ivan Sadikov
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program and AWS Cloud Computing Credits for Research.
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2018 Olof Björck, Joakim Johansson, Rania Sahioun, Raazesh Sainudiin and Ivan Sadikov
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
// MAGIC ### Read data

// COMMAND ----------

// Get data
val df_raw = spark.read.parquet("dbfs:/datasets/MEP/SE/RetrospectiveMany/*")

// COMMAND ----------

// MAGIC %md
// MAGIC Import some functions used for data cleaning

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

// Get cleaned data using functions imported above
val df = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(df_raw.select("statusJsonString"))).cache()
df.count

// COMMAND ----------

// Look at the data we're working with
display(df)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Get the network's primary IDs and screen names

// COMMAND ----------

// Get user info from the "primary" users in our network. They are the users we've specified.
val usersInfo = df
                  .select("CPostUserID", "CPostUserSN", "followersCount")
                  .groupBy("CPostUserID", "CPostUserSN")
                  .agg(max("followersCount").as("followersCount"))
                  .toDF(Seq("id", "ScreenName", "followersCount"): _*)
                  .select("id", "ScreenName")

usersInfo.show(1)

// COMMAND ----------

usersInfo.count

// COMMAND ----------

// MAGIC %md
// MAGIC ### Get display data (number of retweets, number of retweeters, ...)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Compute how many retweets each user have

// COMMAND ----------

// Get all retweets and their weights (weights == 1 as we haven't done anything yet)
val RTNetworkDF = df.filter($"TweetType"==="ReTweet").select("OPostUserSNinRT", "CPostUserSN", "OPostUserIdinRT","CPostUserId","Weight")

RTNetworkDF.show(1)

// COMMAND ----------

// How many rows do we have?
RTNetworkDF.count

// COMMAND ----------

// Get all (directed) retweet tuples
val weightedRTNetworkDF = RTNetworkDF
                          .groupBy("OPostUserSNinRT", "CPostUserSN", "OPostUserIdinRT","CPostUserId")
                          .agg(sum("Weight").as("Weight"))
                          .orderBy($"Weight".desc)
                          .toDF("source", "target", "sourceId", "targetId", "weight")

weightedRTNetworkDF.show(1)

// COMMAND ----------

// How many tuples do we have?
weightedRTNetworkDF.distinct.count

// COMMAND ----------

// Get the out degree of each user. That is, get only the number of times a user is retweeted. We're now losing the tuples.
val outDegreeOfOPostUserIdinRTNetwork = weightedRTNetworkDF
                                        .select("sourceId","weight")
                                        .groupBy("sourceId")
                                        .agg(sum("weight").as("weight"))
                                        .orderBy($"weight".desc)
                                        .toDF(Seq("id", "weight"): _*)

outDegreeOfOPostUserIdinRTNetwork.show(1)

// COMMAND ----------

// How many users do we have?
outDegreeOfOPostUserIdinRTNetwork.count

// COMMAND ----------

// MAGIC %md
// MAGIC ### Get nodes

// COMMAND ----------

// TODO: compute groups and add that info instead of lit(1)

// COMMAND ----------

var nodes = usersInfo
            .join(outDegreeOfOPostUserIdinRTNetwork, Seq("id")).withColumn("group", lit(1))
            .toDF(Seq("idNr", "id", "weight", "group"): _*)
            .where("id is not null")

nodes.show(1)

// COMMAND ----------

// How many nodes do we have?
nodes.count

// COMMAND ----------

display(nodes)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Get links 
// MAGIC *links == edges. D3 force graph uses the term links*

// COMMAND ----------

var linksSource = nodes.select("idNr")
                  .join(weightedRTNetworkDF, $"idNr" === $"sourceId", "left_outer")
                  .select("source", "target", "sourceId", "targetId", "weight")

linksSource.show(10)

// COMMAND ----------

linksSource.count

// COMMAND ----------

var links = nodes.select("idNr")
            .join(linksSource, $"idNr" === $"targetId", "left_outer")
            .select("source", "target", "weight")
            .where("Source is not null")

links.show(1000)

// COMMAND ----------

links.count

// COMMAND ----------

display(links)

// COMMAND ----------

//nodes.filter($"UserID" === 1077817430L).show

// COMMAND ----------

// MAGIC %md
// MAGIC ### Visualize

// COMMAND ----------

display(nodes.toJSON)

// COMMAND ----------

//links.head

// COMMAND ----------

// MAGIC %run Users/olof.bjorck@gmail.com/VizGraphFunction

// COMMAND ----------

visualizeGraph(df, 0, 1000, 1000)

// COMMAND ----------

// MAGIC %run Users/olof.bjorck@gmail.com/VizNetworkFunction

// COMMAND ----------

visualizeNetwork(df)

// COMMAND ----------

