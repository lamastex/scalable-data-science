// Databricks notebook source exported at Fri, 14 Oct 2016 21:08:43 UTC
// MAGIC %md
// MAGIC # Exploratory Analysis of ISIS Tweets Data 
// MAGIC ## Via Apache Spark DataFrames and GraphFrames
// MAGIC 
// MAGIC ### 2016, Raazesh Sainudiin and Rania Sahioun
// MAGIC 
// MAGIC We explore the extracted *mentions network* using: 
// MAGIC * connected components, strongly connected components
// MAGIC * community detection via label propagation
// MAGIC * page rank
// MAGIC * and various SparkSQL queries in DataFrame
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program.
// MAGIC 
// MAGIC The analysis is available in the following databricks notebook:
// MAGIC * [http://lamastex.org/lmse/mep/fighting-hate/extremist-files/ideology/islamic-state/tweetsIsis.html](http://lamastex.org/lmse/mep/fighting-hate/extremist-files/ideology/islamic-state/tweetsIsis.html)
// MAGIC 
// MAGIC For details on the mathematical model motivating the exploratory data analysis in the notebook see:
// MAGIC * The Transmission Process: A Combinatorial Stochastic Process for the Evolution of Transmission Trees over Networks, Raazesh Sainudiin and David Welch, Journal of Theoretical Biology, Volume 410, Pages 137–170, [10.1016/j.jtbi.2016.07.038](http://dx.doi.org/10.1016/j.jtbi.2016.07.038), 2016 
// MAGIC * [preprint of the above paper as PDF 900KB](http://lamastex.org/preprints/20160806_transmissionProcJTBFinal.pdf).
// MAGIC 
// MAGIC Other resources:
// MAGIC * https://www.ctc.usma.edu/isil-resources
// MAGIC * https://www.ctc.usma.edu/v2/wp-content/uploads/2014/12/CTC-The-Group-That-Calls-Itself-A-State-December20141.pdf
// MAGIC * https://www.ctc.usma.edu/posts/communication-breakdown-unraveling-the-islamic-states-media-efforts
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
// MAGIC This data set is from [Khuram](https://www.kaggle.com/kzaman) of [Fifth Tribe](http://www.fifthtribe.com/). The following description is from: 
// MAGIC * [https://www.kaggle.com/kzaman/how-isis-uses-twitter](https://www.kaggle.com/kzaman/how-isis-uses-twitter).
// MAGIC 
// MAGIC # How ISIS Uses Twitter
// MAGIC ## Analyze how ISIS fanboys have been using Twitter since 2015 Paris Attacks
// MAGIC ### by Khuram
// MAGIC 
// MAGIC [Released Under CC0: Public Domain License](https://creativecommons.org/publicdomain/zero/1.0/)
// MAGIC 
// MAGIC 
// MAGIC ### Description
// MAGIC 
// MAGIC We scraped over 17,000 tweets from 100+ pro-ISIS fanboys from all over the world since the November 2015 Paris Attacks. We are working with content producers and influencers to develop effective counter-messaging measures against violent extremists at home and abroad. In order to maximize our impact, we need assistance in quickly analyzing message frames.
// MAGIC 
// MAGIC The dataset includes the following:
// MAGIC 
// MAGIC     Name
// MAGIC     Username
// MAGIC     Description
// MAGIC     Location
// MAGIC     Number of followers at the time the tweet was downloaded
// MAGIC     Number of statuses by the user when the tweet was downloaded
// MAGIC     Date and timestamp of the tweet
// MAGIC     The tweet itself
// MAGIC 
// MAGIC Based on this data, here are some useful ways of deriving insights and analysis:
// MAGIC 
// MAGIC     Social Network Cluster Analysis: Who are the major players in the pro-ISIS twitter network? Ideally, we would like this visualized via a cluster network with the biggest influencers scaled larger than smaller influencers.
// MAGIC     Keyword Analysis: Which keywords derived from the name, username, description, location, and tweets were the most commonly used by ISIS fanboys? Examples include: "baqiyah", "dabiq", "wilayat", "amaq"
// MAGIC     Data Categorization of Links: Which websites are pro-ISIS fanboys linking to? Categories include: Mainstream Media, Altermedia, Jihadist Websites, Image Upload, Video Upload,
// MAGIC     Sentiment Analysis: Which clergy do pro-ISIS fanboys quote the most and which ones do they hate the most? Search the tweets for names of prominent clergy and classify the tweet as positive, negative, or neutral and if negative, include the reasons why. Examples of clergy they like the most: "Anwar Awlaki", "Ahmad Jibril", "Ibn Taymiyyah", "Abdul Wahhab". Examples of clergy that they hate the most: "Hamza Yusuf", "Suhaib Webb", "Yaser Qadhi", "Nouman Ali Khan", "Yaqoubi".
// MAGIC     Timeline View: Visualize all the tweets over a timeline and identify peak moments
// MAGIC 
// MAGIC Further Reading: ["ISIS Has a Twitter Strategy and It is Terrifying [Infographic]"](https://medium.com/fifth-tribe-stories/isis-has-a-twitter-strategy-and-it-is-terrifying-7cc059ccf51b#.m3zeluykl).
// MAGIC 
// MAGIC ![image of isis from fifth tribe](https://cdn-images-1.medium.com/max/800/1*uvQTKOefNi8zMZz_kMBXTA.jpeg)
// MAGIC 
// MAGIC ### About Fifth Tribe
// MAGIC 
// MAGIC [Fifth Tribe](http://www.fifthtribe.com/) is a digital agency based out of DC that serves businesses, non-profits, and government agencies. We provide our clients with product development, branding, web/mobile development, and digital marketing services. Our client list includes Oxfam, Ernst and Young, Kaiser Permanente, Aetna Innovation Health, the U.S. Air Force, and the U.S. Peace Corps. Along with Goldman Sachs International and IBM, we serve on the Private Sector Committee of the Board of the Global Community Engagement and Resilience Fund (GCERF), the first global effort to support local, community-level initiatives aimed at strengthening resilience against violent extremism. In December 2014, we won the anti-ISIS "Hedaya Hack" organized by Affinis Labs and hosted at the "Global Countering Violent Extremism (CVE) Expo " in Abu Dhabi. Since then, we've been actively involved in working with the open-source community and community content producers in developing counter-messaging campaigns and tools.

// COMMAND ----------

// MAGIC %md
// MAGIC The data has been written to parquet file. So let's just read it into a DataFrame (See **Writing Isis Tweets to a Parquet File** below to do this step in a new shard).

// COMMAND ----------

1+1 // sanity check!

// COMMAND ----------

val tweetsIsisRawDF = sqlContext.read.parquet("/datasets/tweets/isis/kzaman/version4/raw/tweetsIsisRaw")
tweetsIsisRawDF.count()

// COMMAND ----------

display(tweetsIsisRawDF)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Isis Mentions Network

// COMMAND ----------

//import sqlContext.implicits._
//import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

// COMMAND ----------

//make a DataFrame of mentions ie a mentions network with edges given by {(username, mentioned_usernames)}
val mentionsNetworkDF = tweetsIsisRawDF
                          .select($"username",$"tweets",$"time")
                          .filter($"tweets" rlike ".*@.*")
                          .explode($"tweets")( 
                                                 _.getAs[String](0).split(" ")
                                                .filter(a => a.matches("^@.*"))
                                                .map(a => a.replace("@",""))
                                                .map(a => a.replace(":",""))
                                                .map(Tuple1(_))
                                              )
                                              .select($"username",$"_1".as("mentions"),$"time",unix_timestamp($"time", "MM/dd/yyyy HH:mm").cast(TimestampType).as("timestamp"))
                                              .withColumn("weight", lit(1L))

// COMMAND ----------

display(mentionsNetworkDF)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Top 10 most active usernames

// COMMAND ----------

display(mentionsNetworkDF
  .select(mentionsNetworkDF("username"), mentionsNetworkDF("weight"))
  .groupBy("username")
  .sum()
  .orderBy($"sum(weight)".desc)
  .limit(10))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Top 10 most active (usernames,mentions) directed-edge pairs

// COMMAND ----------

display(mentionsNetworkDF
  .select(mentionsNetworkDF("username"), mentionsNetworkDF("mentions"), mentionsNetworkDF("weight"))
  .groupBy("username","mentions")
  .sum()
  .orderBy($"sum(weight)".desc)
  .limit(10))

// COMMAND ----------

// MAGIC %md
// MAGIC Full network of mentions with weights

// COMMAND ----------

val mentionsWeightedNetworkDF = mentionsNetworkDF
  .select(mentionsNetworkDF("username"), mentionsNetworkDF("mentions"), mentionsNetworkDF("weight"))
  .groupBy("username","mentions")
  .sum()
  .orderBy($"sum(weight)".desc)
  .cache()

// COMMAND ----------

display(mentionsWeightedNetworkDF)

// COMMAND ----------

mentionsWeightedNetworkDF.count()

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
// MAGIC #### The weighted mentions network with top 20 most heavy directed edges

// COMMAND ----------

  val mentionsDS = mentionsWeightedNetworkDF
                              .select($"username".as("src"), $"mentions".as("dest"), $"sum(weight)".as("count"))
                              .orderBy($"count".desc)
                              .limit(20)
                              .as[d3.Edge]

// COMMAND ----------

mentionsDS.show(false)

// COMMAND ----------

d3.graphs.force( // self-loops are ignored
  height = 800,
  width = 1000,
  clicks = mentionsDS
)

// COMMAND ----------

// MAGIC %md
// MAGIC ### GraphFrame of Mentions Network

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://amplab.github.io/graphx/",700))

// COMMAND ----------

// MAGIC %md
// MAGIC Turn the DataFrame into GraphFrame for more exploration.

// COMMAND ----------

//need to attach graphframes library!
import org.graphframes._

// COMMAND ----------

// MAGIC %md
// MAGIC Obtain the distinct `id`s from each of the two columns: `username` and `mentions` in `mentionsWeightedNetworkDF` and take their distinct union to get the set of vertices as DataFrame `v`.

// COMMAND ----------

val v1 = mentionsWeightedNetworkDF.select($"username").distinct().toDF("id")
val v2 = mentionsWeightedNetworkDF.select($"mentions").distinct().toDF("id")
val v = v1.unionAll(v2).distinct().cache()
v.count

// COMMAND ----------

v.show(5,false)

// COMMAND ----------

mentionsWeightedNetworkDF.show(5,false)

// COMMAND ----------

// MAGIC %md
// MAGIC Now make the DataFrame of edges `e`.

// COMMAND ----------

val e = mentionsWeightedNetworkDF.select($"username".as("src"), $"mentions".as("dst"), $"sum(weight)".as("Num_src2dst_mentions")).withColumn("relationship", lit("mentions"))

// COMMAND ----------

e.show(10,false)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Making a GraphFrame.

// COMMAND ----------

val g = GraphFrame(v, e)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Exploring the in/out-Degrees
// MAGIC 
// MAGIC As is evident from the distribution of inDegrees and outDegrees, the dissemination or source of a transmission event via twitter is focussed on a few key usernames while reception of the transmission events as indicated by the distribution of indegrees is more concentrated on smaller numbers in {1,2,3,4,5}.

// COMMAND ----------

display(g.inDegrees
         .orderBy($"inDegree".desc)
       )

// COMMAND ----------

display(g.inDegrees
  .select($"inDegree")
  .withColumn("count", lit(1L))
  .groupBy("inDegree")
  .sum("count")
  .orderBy($"inDegree".asc)
  )

// COMMAND ----------

display(g.outDegrees.orderBy($"outDegree".desc))

// COMMAND ----------

display(g.outDegrees
  .select($"outDegree")
  .withColumn("count", lit(1L))
  .groupBy("outDegree")
  .sum("count")
  .orderBy($"outDegree".desc)
  )

// COMMAND ----------

// MAGIC %md 
// MAGIC ##Motif finding
// MAGIC 
// MAGIC More complex relationships involving edges and vertices can be built using motifs. 
// MAGIC 
// MAGIC The following cell finds the pairs of vertices with edges in both directions between them. 
// MAGIC 
// MAGIC The result is a dataframe, in which the column names are given by the motif keys.
// MAGIC 
// MAGIC Check out the [GraphFrame User Guide](http://graphframes.github.io/user-guide.html#motif-finding) for more details on the API.

// COMMAND ----------

// Search for pairs of vertices with edges in both directions between them, i.e., find undirected or bidirected edges.
val undirectedEdges = g.find("(a)-[e1]->(b); (b)-[e2]->(a)")
display(undirectedEdges)

// COMMAND ----------

//Search for all "directed triangles" or triplets of vertices: a,b,c with edges: a->b, b->c and c->a
//uncomment the next 2 lines and replace the "..." below
val motifs3 = g.find("(a)-[e1]->(b); (b)-[e2]->(c); (c)-[e3]->(a) ")
display(motifs3)

// COMMAND ----------

// MAGIC %md
// MAGIC ###Subgraphs
// MAGIC 
// MAGIC Subgraphs are built by filtering a subset of edges and vertices. For example, the following subgraph only contains people who are friends and who are more than 30 years old.

// COMMAND ----------

display(g.edges.limit(10))

// COMMAND ----------

// Select subgraph of edges of type "mentions" with "Num_src2dst_mentions > 100"
//val v2 = g.vertices.filter("followers > 30") // can be done in vertices have more properties

val e2 = g.edges.filter("relationship = 'mentions'").filter("Num_src2dst_mentions > 30")
val v2 = e2.select($"src").unionAll(e2.select($"dst")).distinct().toDF("id")
val g2 = GraphFrame(v2, e2)

// COMMAND ----------

g2.edges.show

// COMMAND ----------

d3.graphs.force( // let us see g2 now in one cell
  height = 600,
  width = 800,
  clicks = g2.edges.select($"src", $"dst".as("dest"), $"Num_src2dst_mentions".as("count")).as[d3.Edge])

// COMMAND ----------

// MAGIC %md 
// MAGIC ###Connected components
// MAGIC 
// MAGIC Compute the connected component membership of each vertex and return a graph with each vertex assigned a component ID.

// COMMAND ----------

val ccG = g.connectedComponents.run() // doesn't work on Spark 1.4

// COMMAND ----------

val top10CCDF = ccG.select("Id","component").withColumn("weight", lit(1L)).groupBy("component").sum("weight").orderBy($"sum(weight)".desc)//.limit(10)
top10CCDF.show()

// COMMAND ----------

val largestCCIds = ccG.filter("component = 0").select($"Id")
largestCCIds.show()

// COMMAND ----------

// MAGIC %md 
// MAGIC ##Strongly connected components
// MAGIC 
// MAGIC Compute the strongly connected component (SCC) of each vertex and return a graph with each vertex assigned to the SCC containing that vertex.

// COMMAND ----------

val sccG = g.stronglyConnectedComponents.maxIter(10).run()

// COMMAND ----------

display(sccG.orderBy("component"))

// COMMAND ----------

val top10SCCDF = sccG.select("Id","component").withColumn("weight", lit(1L)).groupBy("component").sum("weight").orderBy($"sum(weight)".desc)//.limit(10)
top10SCCDF.show()

// COMMAND ----------

val largestSCCIds = sccG.filter("component = 4").select($"Id")
largestSCCIds.show()

// COMMAND ----------

// MAGIC %md 
// MAGIC ##Label propagation
// MAGIC 
// MAGIC Run static Label Propagation Algorithm for detecting communities in networks.
// MAGIC 
// MAGIC Each node in the network is initially assigned to its own community. At every superstep, nodes send their community affiliation to all neighbors and update their state to the mode community affiliation of incoming messages.
// MAGIC 
// MAGIC LPA is a standard community detection algorithm for graphs. It is very inexpensive computationally, although 
// MAGIC * (1) convergence is not guaranteed and 
// MAGIC * (2) one can end up with trivial solutions (all nodes are identified into a single community).

// COMMAND ----------

displayHTML(frameIt("http://graphframes.github.io/user-guide.html#label-propagation-algorithm-lpa",600))

// COMMAND ----------

val result = g.labelPropagation.maxIter(5).run()

// COMMAND ----------

// Create DF with proper column names
val top20communityDF = result.select("Id","label").withColumn("weight", lit(1L)).groupBy("label").sum("weight").orderBy($"sum(weight)".desc).limit(20)
top20communityDF.show()

// COMMAND ----------

// MAGIC %md
// MAGIC #### The community labels that the Ids in the largest strongly connected component belong to

// COMMAND ----------

display(largestSCCIds.join(result,largestSCCIds("id")===result("id"),"inner").select(result("id"),result("label")).orderBy(result("label").desc))

// COMMAND ----------

// MAGIC %md
// MAGIC #### The community lables that the top 50 most "influenceable" Ids (with top 50 indegrees in the mentions network) belong to

// COMMAND ----------

val gin=g.inDegrees
val nameGroupDF = gin.orderBy($"inDegree".desc).limit(50).join(result,gin("id")===result("id"))

// COMMAND ----------

val top50InfluencableIdsIndegreesLabels = nameGroupDF.orderBy($"inDegree".desc).select(gin("id"),gin("indegree"),result("label"))

// COMMAND ----------

display(top50InfluencableIdsIndegreesLabels)

// COMMAND ----------

// Create JSON data
val rawJson = top50InfluencableIdsIndegreesLabels.select($"Id".as("term"),$"Indegree".as("probability"),$"label".as("topicId")).toJSON.collect().mkString(",\n")

// COMMAND ----------

displayHTML(s"""
<!DOCTYPE html>
<meta charset="utf-8">
<style>

circle {
  fill: rgb(31, 119, 180);
  fill-opacity: 0.5;
  stroke: rgb(31, 119, 180);
  stroke-width: 1px;
}

.leaf circle {
  fill: #ff7f0e;
  fill-opacity: 1;
}

text {
  font: 14px sans-serif;
}

</style>
<body>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js"></script>
<script>

var json = {
 "name": "data",
 "children": [
  {
     "name": "topics",
     "children": [
      ${rawJson}
     ]
    }
   ]
};

var r = 1500,
    format = d3.format(",d"),
    fill = d3.scale.category20c();

var bubble = d3.layout.pack()
    .sort(null)
    .size([r, r])
    .padding(1.5);

var vis = d3.select("body").append("svg")
    .attr("width", r)
    .attr("height", r)
    .attr("class", "bubble");

  
var node = vis.selectAll("g.node")
    .data(bubble.nodes(classes(json))
    .filter(function(d) { return !d.children; }))
    .enter().append("g")
    .attr("class", "node")
    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    color = d3.scale.category20();
  
  node.append("title")
      .text(function(d) { return d.className + ": " + format(d.value); });

  node.append("circle")
      .attr("r", function(d) { return d.r; })
      .style("fill", function(d) {return color(d.topicName);});

var text = node.append("text")
    .attr("text-anchor", "middle")
    .attr("dy", ".3em")
    .text(function(d) { return d.className.substring(0, d.r / 3)});
  
  text.append("tspan")
      .attr("dy", "1.2em")
      .attr("x", 0)
      .text(function(d) {return Math.ceil(d.value * 10000) /10000; });

// Returns a flattened hierarchy containing all leaf nodes under the root.
function classes(root) {
  var classes = [];

  function recurse(term, node) {
    if (node.children) node.children.forEach(function(child) { recurse(node.term, child); });
    else classes.push({topicName: node.topicId, className: node.term, value: node.probability});
  }

  recurse(null, root);
  return {children: classes};
}
</script>
""")

// COMMAND ----------

// MAGIC %md 
// MAGIC ##PageRank
// MAGIC 
// MAGIC Identify important vertices in a graph based on connections.

// COMMAND ----------

displayHTML(frameIt("http://graphframes.github.io/user-guide.html#pagerank",600))

// COMMAND ----------

// Run PageRank until convergence to tolerance "tol".
val results = g.pageRank.resetProbability(0.15).tol(0.01).run()
display(results.vertices)

// COMMAND ----------

display(results.vertices.orderBy($"pagerank".desc))

// COMMAND ----------

display(results.edges.orderBy($"Num_src2dst_mentions".desc)) // note the page-rank is run on the adjacency graph with no initial weights from Num_src2dst_mentions

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC ## Writing Isis Tweets to a Parquet File

// COMMAND ----------

// MAGIC %md
// MAGIC ### Download the csv file
// MAGIC This csv file has been obtained from the tweets.xls.zip file with all strings encapsulated by `"` in order to make reading into Spark easier (due to the end of line characters in the `tweet` field).

// COMMAND ----------

// MAGIC %sh
// MAGIC wget http://lamastex.org/lmse/mep/fighting-hate/extremist-files/ideology/islamic-state/tweets.csv.tgz

// COMMAND ----------

// MAGIC %sh
// MAGIC tar zxvf tweets.csv.tgz

// COMMAND ----------

dbutils.fs.mkdirs("/datasets/tweets/isis/kzaman/version4/raw") // make a directory in dbfs

// COMMAND ----------

// MAGIC %sh 
// MAGIC pwd # find working directory where the .tgz was downloaded

// COMMAND ----------

dbutils.fs.mv("file:///databricks/driver/tweets.csv","dbfs:///datasets/tweets/isis/kzaman/version4/raw/") // move it to dbfs

// COMMAND ----------

//dbutils.fs.rm("/datasets/tweets/isis/kzaman/version4/raw/tweets.csv.tgz",recurse=true)

// COMMAND ----------

display(dbutils.fs.ls("/datasets/tweets/isis/kzaman/version4/raw"))

// COMMAND ----------

val tweetIsisDF = sqlContext.read.format("com.databricks.spark.csv").options(Map("path" -> "dbfs:///datasets/tweets/isis/kzaman/version4/raw/tweets.csv", "header" -> "true", "inferSchema" -> "true", "delimiter" -> "," , "quote" -> "\"", "escape" -> "\\" ,"parserLib" -> "univocity" )).load()

// COMMAND ----------

tweetIsisDF.count() // couting the rows

// COMMAND ----------

tweetIsisDF.printSchema()

// COMMAND ----------

// Convert the DatFrame to a more efficent format to speed up our analysis
tweetIsisDF.
  write.
  mode(SaveMode.Overwrite).
  parquet("/datasets/tweets/isis/kzaman/version4/raw/tweetsIsisRaw") // warnings are harmless

// COMMAND ----------

import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
//we will convert price column from int to double later
val tweetsIsisDF = tweetIsisDF.select($"name", $"username", $"description", $"location", $"followers".cast(LongType), $"numberstatuses".cast(LongType),$"time",unix_timestamp($"time", "MM/dd/yyyy HH:mm").cast(TimestampType).as("timestamp"), $"tweets")
tweetsIsisDF.cache() // let's cache it for reuse
tweetsIsisDF.printSchema // print schema

// COMMAND ----------

// Convert the DatFrame to a more efficent format to speed up our analysis
tweetsIsisDF.
  write.
  mode(SaveMode.Overwrite).
  parquet("/datasets/tweets/isis/kzaman/version4/tweetsIsis") // warnings are harmless

// COMMAND ----------

display(dbutils.fs.ls("/datasets/tweets/isis/kzaman/version4/raw"))

// COMMAND ----------

// MAGIC %md
// MAGIC Now we can read directly from the parquet files into DataFrames.

// COMMAND ----------

val tweetsIsisDF = sqlContext.read.parquet("/datasets/tweets/isis/kzaman/version4/tweetsIsis")
tweetsIsisDF.printSchema

// COMMAND ----------

val tweetsIsisRawDF = sqlContext.read.parquet("/datasets/tweets/isis/kzaman/version4/raw/tweetsIsisRaw")
tweetsIsisRawDF.printSchema