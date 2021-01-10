// Databricks notebook source
// MAGIC %md
// MAGIC # Visualization functions examples
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

// Get data
val df_raw = spark.read.parquet("dbfs:/datasets/MEP/SE/RetrospectiveMany/*")

// COMMAND ----------

// MAGIC %md
// MAGIC Import functions to create TTTDF

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

// Get TTTDF using functions imported above
val TTTDF = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(df_raw.select("statusJsonString"))).cache()
TTTDF.count

// COMMAND ----------

// MAGIC %md
// MAGIC ## Let's look at a single user timeline with ID = 1947525476

// COMMAND ----------

// MAGIC %md
// MAGIC Import the user timeline function

// COMMAND ----------

// MAGIC %run Users/olof.bjorck@gmail.com/VizTimelineFunction

// COMMAND ----------

// visualize the timeline
visualizeTimeline(TTTDF.filter($"CPostUserId" === 1947525476L))

// COMMAND ----------

// MAGIC %md
// MAGIC Let's change the display size

// COMMAND ----------

visualizeTimeline(TTTDF.filter($"CPostUserId" === 1947525476L), 200, 200)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Let's now look at the Twitter data we're working with.

// COMMAND ----------

// MAGIC %md
// MAGIC First by a network

// COMMAND ----------

// MAGIC %run Users/olof.bjorck@gmail.com/VizNetworkFunction

// COMMAND ----------

visualizeNetwork(TTTDF)

// COMMAND ----------

visualizeNetwork(TTTDF, 800, 800)

// COMMAND ----------

// MAGIC %md
// MAGIC ... and now by a graph

// COMMAND ----------

// MAGIC %run Users/olof.bjorck@gmail.com/VizGraphFunction

// COMMAND ----------

visualizeGraph(TTTDF)

// COMMAND ----------

// MAGIC %md
// MAGIC That graph looks too messy. Let's cut off connections at 30

// COMMAND ----------

visualizeGraph(TTTDF, 30)

// COMMAND ----------

// MAGIC %md
// MAGIC We can change the display size here too

// COMMAND ----------

visualizeGraph(TTTDF, 30, 800, 800)

// COMMAND ----------

