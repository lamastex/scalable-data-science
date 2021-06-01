// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Interactive Exploration in Twitter as Graph, Network and Timeline
// MAGIC 
// MAGIC by Olof Björck, Joakim Johansson, Rania Sahioun, Raazesh Sainudiin and Ivan Sadikov
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks, AWS and a Swedish VR grant.
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016-2020 Olof Björck, Joakim Johansson, Rania Sahioun, Ivan Sadikov and Raazesh Sainudiin
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
// MAGIC ## Run notebooks 
// MAGIC 
// MAGIC These notebooks have needed variables and functions.

// COMMAND ----------

// MAGIC %run ./025_b_TTTDFfunctions

// COMMAND ----------

// MAGIC %md
// MAGIC ## Read twitter data as TTTDF
// MAGIC 
// MAGIC We cache and count the data as a TTTDF or Twwet-Transmission-Tree dataFrame.
// MAGIC 
// MAGIC Make sure the count is not too big as the driver may crash. One can do a time-varying version that truncates the visualized data to an appropriate finite number.

// COMMAND ----------

// Get the TTT  
val rawTweetsDir = "PATH_To_TWEETS_From_StreamingJOB"
val TTTDF = tweetsDF2TTTDFWithURLsAndHashtagsLightWeight(tweetsJsonStringDF2TweetsDF(fromParquetFile2DF(rawTweetsDir))).cache()
TTTDF.count // Check how much we got (need to check size and auto-truncate to not crash driver program... all real for now)

// COMMAND ----------

TTTDF.rdd.getNumPartitions

// COMMAND ----------

// MAGIC %md
// MAGIC Next we first write the TTTDF to a parquet file and then read it into the visualizer. This will usually be done via a fast database.

// COMMAND ----------

TTTDF.write.mode("overwrite")
     .parquet("PATH_TO_PARQUET_TTTDF.parquet")

// COMMAND ----------

val TTTDFpq = spark.read.parquet("PATH_TO_PARQUET_TTTDF.parquet")

// COMMAND ----------

TTTDFpq.rdd.getNumPartitions

// COMMAND ----------

// MAGIC %md
// MAGIC ## Visualize a Twitter Graph
// MAGIC 
// MAGIC The graph shows the twitter users in the collection organized by their popularity in terms of various status updates.
// MAGIC 
// MAGIC See the function `visualizeGraph` for details. More sophisticated D3 interactive graphs are possible with the TTTDF, especially when filtered by time intervals or interactions of interest.

// COMMAND ----------

// MAGIC %run ./029_Viz_x_VizGraphFunction

// COMMAND ----------

visualizeGraph(TTTDFpq, 0, 700, 700)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Visualizing Retweet Network
// MAGIC 
// MAGIC Let us interactively explore the retweet network to identify tweets in terms of the number of retweets versus the number of its unique retweeeters.

// COMMAND ----------

// MAGIC %run ./029_Viz_x_VizNetworkFunction

// COMMAND ----------

visualizeNetwork(TTTDFpq)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Interactively Visualize a twitter Timeline 
// MAGIC 
// MAGIC Search through Tweets over a timeline.

// COMMAND ----------

// MAGIC %run ./029_Viz_x_VizTimelineFunction

// COMMAND ----------

visualizeTimeline(TTTDFpq) 

// COMMAND ----------

// MAGIC %md
// MAGIC ## What next?
// MAGIC 
// MAGIC Ideally the TTTDF and its precursor raw tweets are in silver and bronze delta.io tables and one can then use an interactive dashboard such as:
// MAGIC 
// MAGIC - [Apache Superset Overview](https://superset.apache.org/)
// MAGIC - [Apache Superset Gallery](https://superset.apache.org/gallery)
// MAGIC 
// MAGIC > Superset is fast, lightweight, intuitive, and loaded with options that make it easy for users of all skill sets to explore and visualize their data, from simple line charts to highly detailed geospatial charts.

// COMMAND ----------

def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """">
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://superset.apache.org/gallery",600))