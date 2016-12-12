// Databricks notebook source exported at Mon, 12 Dec 2016 16:35:17 UTC
// MAGIC %md
// MAGIC # An Analysis of Transmissions on Twitter Networks Characterized by Various Extremist Ideologies
// MAGIC 
// MAGIC ### 2016, Raazesh Sainudiin and Rania Sahioun
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program.
// MAGIC 
// MAGIC The analysis is available in the following databricks notebook:
// MAGIC * [http://lamastex.org/lmse/mep/fighting-hate/extremist-files/multiExtremistFollowersNetwork.html](http://lamastex.org/lmse/mep/fighting-hate/extremist-files/multiExtremistFollowersNetwork.html)
// MAGIC 
// MAGIC For details on the mathematical model motivating the exploratory data analysis in the notebook see:
// MAGIC * The Transmission Process: A Combinatorial Stochastic Process for the Evolution of Transmission Trees over Networks, Raazesh Sainudiin and David Welch, Journal of Theoretical Biology, Volume 410, Pages 137–170, [10.1016/j.jtbi.2016.07.038](http://dx.doi.org/10.1016/j.jtbi.2016.07.038), 2016 
// MAGIC * [preprint of the above paper as PDF 900KB](http://lamastex.org/preprints/20160806_transmissionProcJTBFinal.pdf).
// MAGIC 
// MAGIC This notebook is a periodic publicly shared snapshot of research in progress.
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016 Raazesh Sainudiin and Rania Sahioun
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
// MAGIC ![https://www.splcenter.org/sites/default/files/page_images/SPLC-Hate-Map-1280x720.jpg](https://www.splcenter.org/sites/default/files/page_images/SPLC-Hate-Map-1280x720.jpg)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Various extremist ideologies categorized by the US Southern Poverty Law Center (SPLC)

// COMMAND ----------

// MAGIC %md
// MAGIC * https://www.splcenter.org/fighting-hate/extremist-files/ideology

// COMMAND ----------

// MAGIC %md
// MAGIC ## Here are various groups that partition into the above ideologies.

// COMMAND ----------

// MAGIC %md
// MAGIC * https://www.splcenter.org/fighting-hate/extremist-files/groups

// COMMAND ----------

// MAGIC %md
// MAGIC ## Here are various extremists who are charismatic proponents of extremist ideologies

// COMMAND ----------

// MAGIC %md
// MAGIC * https://www.splcenter.org/fighting-hate/extremist-files/individual

// COMMAND ----------

// MAGIC %md
// MAGIC #### To gain more domain expertise for this preliminary data analytic and exploratory task from a historical/social/political perspective read:
// MAGIC * [http://www.vox.com/2016/9/19/12933072/far-right-white-riot-trump-brexit](http://www.vox.com/2016/9/19/12933072/far-right-white-riot-trump-brexit).

// COMMAND ----------

// MAGIC %md
// MAGIC #### This data analysis is live and in progress and our results and findigs will evolve as we collate more data and analyze it it more comprehensive ways.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Reading from parquet DF loaded from s3 
// MAGIC ... this needs to be done from this notebook with s3 credentials
// MAGIC https://academics.cloud.databricks.com/#notebook/137859
// MAGIC 
// MAGIC See the following section name of the above notebook:
// MAGIC 
// MAGIC ### Writing a single followers DF in dbfs as parquet by loading it from s3

// COMMAND ----------

display(dbutils.fs.ls(s"dbfs:/datasets/MEP/followers"))

// COMMAND ----------

val tweetsSPLCRawDF2 = sqlContext.read.parquet("/datasets/MEP/followers/*/*")
tweetsSPLCRawDF2.cache()
tweetsSPLCRawDF2.count()

// COMMAND ----------

val tweetsSPLCRawDF = sqlContext.read.parquet("/datasets/MEP/followers")

// COMMAND ----------

tweetsSPLCRawDF.cache()
tweetsSPLCRawDF.count()

// COMMAND ----------

display(tweetsSPLCRawDF.sample(false,0.0001,01234567L) )

// COMMAND ----------

// MAGIC %md 
// MAGIC ## SPLC Followers Network

// COMMAND ----------

import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
//make a DataFrame of mentions ie a mentions network with edges given by {(username, mentioned_usernames)}
val followersNetworkDF = tweetsSPLCRawDF
                          .select($"ideology",$"twitterId",$"following")
                          .withColumn("weight", lit(1L))

// COMMAND ----------

display(followersNetworkDF)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Most heavy (Ideology,following) pairs

// COMMAND ----------

val IdeologyFollowingWeights = followersNetworkDF
  .groupBy("Ideology","following")
  .sum()
  .orderBy($"sum(weight)".desc)
.cache()

display(IdeologyFollowingWeights)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Most heavy Ideologies being followed

// COMMAND ----------

display(IdeologyFollowingWeights
        .groupBy("Ideology")
        .sum("sum(weight)")
        .orderBy($"sum(sum(weight))".desc)
  )

// COMMAND ----------

// MAGIC %md
// MAGIC ### Most heavy followings

// COMMAND ----------

display(followersNetworkDF
  .groupBy("following")
  .sum()
  .orderBy($"sum(weight)".desc)
)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Most heavyily followed ideologies

// COMMAND ----------

display(followersNetworkDF
  .groupBy("Ideology")
  .sum()
  .orderBy($"sum(weight)".desc)
)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Most "diverse" followers

// COMMAND ----------

val top20MostConnectedtwitterIds = followersNetworkDF
                                    .groupBy("twitterId")
                                    .sum()
                                    .orderBy($"sum(weight)".desc)
                                    .limit(20)

display(top20MostConnectedtwitterIds)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Top 20 most connected twitterIds

// COMMAND ----------

val top20MostConnectedEdges = followersNetworkDF
                                  .join(top20MostConnectedtwitterIds, followersNetworkDF("twitterId")===top20MostConnectedtwitterIds("twitterId"),"inner")
                                  .select(followersNetworkDF("twitterId"),followersNetworkDF("following"),followersNetworkDF("Ideology"),top20MostConnectedtwitterIds("sum(weight)"))
                                  .orderBy($"sum(weight)".desc)
                                  .cache()
display(top20MostConnectedEdges)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Load a visualization library
// MAGIC This code is copied after doing a live google search (by Michael Armbrust at Spark Summit East February 2016 
// MAGIC shared from [https://twitter.com/michaelarmbrust/status/699969850475737088](https://twitter.com/michaelarmbrust/status/699969850475737088)).

// COMMAND ----------

package d3
// We use a package object so that we can define top level classes like Edge that need to be used in other cells

import org.apache.spark.sql._
import com.databricks.backend.daemon.driver.EnhancedRDDFunctions.displayHTML

case class Edge(src: String, dest: String, count: Long)

case class Node(name: String)
case class Link(source: Int, target: Int, value: Long)
case class Graph(nodes: Seq[Node], links: Seq[Link])

object graphs {
val sqlContext = SQLContext.getOrCreate(org.apache.spark.SparkContext.getOrCreate())  
import sqlContext.implicits._
  
def force(clicks: Dataset[Edge], height: Int = 100, width: Int = 960): Unit = {
  val data = clicks.collect()
  val nodes = (data.map(_.src) ++ data.map(_.dest)).map(_.replaceAll("_", " ")).toSet.toSeq.map(Node)
  val links = data.map { t =>
    Link(nodes.indexWhere(_.name == t.src.replaceAll("_", " ")), nodes.indexWhere(_.name == t.dest.replaceAll("_", " ")), t.count / 20 + 1)
  }
  showGraph(height, width, Seq(Graph(nodes, links)).toDF().toJSON.first())
}

/**
 * Displays a force directed graph using d3
 * input: {"nodes": [{"name": "..."}], "links": [{"source": 1, "target": 2, "value": 0}]}
 */
def showGraph(height: Int, width: Int, graph: String): Unit = {

displayHTML(s"""
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Polish Books Themes - an Interactive Map</title>
  <meta charset="utf-8">
<style>

.node_circle {
  stroke: #777;
  stroke-width: 1.3px;
}

.node_label {
  pointer-events: none;
}

.link {
  stroke: #777;
  stroke-opacity: .2;
}

.node_count {
  stroke: #777;
  stroke-width: 1.0px;
  fill: #999;
}

text.legend {
  font-family: Verdana;
  font-size: 13px;
  fill: #000;
}

.node text {
  font-family: "Helvetica Neue","Helvetica","Arial",sans-serif;
  font-size: 17px;
  font-weight: 200;
}

</style>
</head>

<body>
<script src="//d3js.org/d3.v3.min.js"></script>
<script>

var graph = $graph;

var width = $width,
    height = $height;

var color = d3.scale.category20();

var force = d3.layout.force()
    .charge(-700)
    .linkDistance(180)
    .size([width, height]);

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);
    
force
    .nodes(graph.nodes)
    .links(graph.links)
    .start();

var link = svg.selectAll(".link")
    .data(graph.links)
    .enter().append("line")
    .attr("class", "link")
    .style("stroke-width", function(d) { return Math.sqrt(d.value); });

var node = svg.selectAll(".node")
    .data(graph.nodes)
    .enter().append("g")
    .attr("class", "node")
    .call(force.drag);

node.append("circle")
    .attr("r", 10)
    .style("fill", function (d) {
    if (d.name.startsWith("other")) { return color(1); } else { return color(2); };
})

node.append("text")
      .attr("dx", 10)
      .attr("dy", ".35em")
      .text(function(d) { return d.name });
      
//Now we are giving the SVGs co-ordinates - the force layout is generating the co-ordinates which this code is using to update the attributes of the SVG elements
force.on("tick", function () {
    link.attr("x1", function (d) {
        return d.source.x;
    })
        .attr("y1", function (d) {
        return d.source.y;
    })
        .attr("x2", function (d) {
        return d.target.x;
    })
        .attr("y2", function (d) {
        return d.target.y;
    });
    d3.selectAll("circle").attr("cx", function (d) {
        return d.x;
    })
        .attr("cy", function (d) {
        return d.y;
    });
    d3.selectAll("text").attr("x", function (d) {
        return d.x;
    })
        .attr("y", function (d) {
        return d.y;
    });
});
</script>
</html>
""")
}
  
  def help() = {
displayHTML("""
<p>
Produces a force-directed graph given a collection of edges of the following form:</br>
<tt><font color="#a71d5d">case class</font> <font color="#795da3">Edge</font>(<font color="#ed6a43">src</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">dest</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">count</font>: <font color="#a71d5d">Long</font>)</tt>
</p>
<p>Usage:<br/>
<tt><font color="#a71d5d">import</font> <font color="#ed6a43">d3._</font></tt><br/>
<tt><font color="#795da3">graphs.force</font>(</br>
&nbsp;&nbsp;<font color="#ed6a43">height</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">width</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">clicks</font>: <font color="#795da3">Dataset</font>[<font color="#795da3">Edge</font>])</tt>
</p>""")
  }
}

// COMMAND ----------

d3.graphs.help()

// COMMAND ----------

// MAGIC %md
// MAGIC #### The weighted followers network with top 20 most heavy directed edges

// COMMAND ----------

  val FollowersDS = top20MostConnectedEdges
                              .select($"twitterId".as("src"), $"following".as("dest"), $"sum(weight)".as("count"))
                              .orderBy($"count".desc)
                              .limit(20)
                              .as[d3.Edge]

// COMMAND ----------

FollowersDS.show(false)

// COMMAND ----------

d3.graphs.force( // self-loops are ignored
  height = 800,
  width = 1000,
  clicks = FollowersDS
)

// COMMAND ----------

// MAGIC %md
// MAGIC ### A randomly sampled subgraph of the followers graph shows ideological segregation

// COMMAND ----------

d3.graphs.force( // self-loops are ignored
  height = 800,
  width = 1000,
  clicks = followersNetworkDF.sample(false,0.0001,1223L) // for adding seed
                              .select($"twitterId".as("src"), $"following".as("dest"), $"weight".as("count"))
                              .orderBy($"count".desc)
                              .limit(30)
                              .as[d3.Edge]
  )