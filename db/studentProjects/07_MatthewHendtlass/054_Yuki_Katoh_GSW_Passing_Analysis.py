# Databricks notebook source exported at Tue, 28 Jun 2016 11:17:28 UTC
# MAGIC %md
# MAGIC # Analyzing Golden State Warriors' passing network using GraphFrames
# MAGIC 
# MAGIC ** This notebook is created by [Yuki Katoh](https://de.linkedin.com/in/yukiegosapporo) and is a modified version of the article originally posted to [Opiate for the masses](http://opiateforthemass.es/articles/analyzing-golden-state-warriors-passing-network-using-graphframes-in-spark/) **
# MAGIC 
# MAGIC ** Dataset: Golden State Warriors's pass data in 2015-16 regular season given by [NBA.com](http://stats.nba.com/) **
# MAGIC 
# MAGIC ** Source: http://stats.nba.com/ **
# MAGIC 
# MAGIC 
# MAGIC <img width="1000px" src="https://github.com/yukiegosapporo/gsw_passing_network/blob/master/network.png?raw=true"/>
# MAGIC 
# MAGIC <i>*This notebook requires Spark 1.6+</i>

# COMMAND ----------

# MAGIC %md
# MAGIC View the [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/07_MatthewHendtlass/054_Yuki_Katoh_GSW_Passing_Analysis.html) of this databricks notebook

# COMMAND ----------

# MAGIC %md **WARNING:** Install the *graphframe* library before running the following commands. For instructions, see [here](https://databricks-staging-cloudfront.staging.cloud.databricks.com/public/c65da9a2fa40e45a2028cddebe45b54c/8637560089690848/2998808121595179/6977722904629137/latest.html).

# COMMAND ----------

from graphframes import *
import pandas as pd
import os
import json

# COMMAND ----------

# Get player IDs of Golden State Warriors
playerids = [201575, 201578, 2738, 202691, 101106, 2760, 2571, 203949, 203546, 203110, 201939, 203105, 2733, 1626172, 203084]

# COMMAND ----------

# Call stats.nba.com API and save pass data for each player as local JSON files
for playerid in playerids:
    os.system('curl "http://stats.nba.com/stats/playerdashptpass?'
        'DateFrom=&'
        'DateTo=&'
        'GameSegment=&'
        'LastNGames=0&'
        'LeagueID=00&'
        'Location=&'
        'Month=0&'
        'OpponentTeamID=0&'
        'Outcome=&'
        'PerMode=Totals&'
        'Period=0&'
        'PlayerID={playerid}&'
        'Season=2015-16&'
        'SeasonSegment=&'
        'SeasonType=Regular+Season&'
        'TeamID=0&'
        'VsConference=&'
        'VsDivision=" > {playerid}.json'.format(playerid=playerid))

# COMMAND ----------

# Parse JSON files and create pandas DataFrame
raw = pd.DataFrame()
for playerid in playerids:
    with open("{playerid}.json".format(playerid=playerid)) as json_file:
        parsed = json.load(json_file)['resultSets'][0]
        raw = raw.append(
            pd.DataFrame(parsed['rowSet'], columns=parsed['headers']))

raw = raw.rename(columns={'PLAYER_NAME_LAST_FIRST': 'PLAYER'})
raw['id'] = raw['PLAYER'].str.replace(', ', '')

# COMMAND ----------

# Create passes
passes = raw[raw['PASS_TO']
.isin(raw['PLAYER'])][['PLAYER', 'PASS_TO','PASS']]

# COMMAND ----------

# Make raw vertices
pandas_vertices = raw[['PLAYER', 'id']].drop_duplicates()
pandas_vertices.columns = ['name', 'id']

# COMMAND ----------

# Make raw edges
pandas_edges = pd.DataFrame()
for passer in raw['id'].drop_duplicates():
    for receiver in raw[(raw['PASS_TO'].isin(raw['PLAYER'])) &
     (raw['id'] == passer)]['PASS_TO'].drop_duplicates():
        pandas_edges = pandas_edges.append(pd.DataFrame(
        	{'passer': passer, 'receiver': receiver
        	.replace(  ', ', '')}, 
        	index=range(int(raw[(raw['id'] == passer) &
        	 (raw['PASS_TO'] == receiver)]['PASS'].values))))

pandas_edges.columns = ['src', 'dst']

# COMMAND ----------

# Bring the local vertices and edges to Spark
vertices = sqlContext.createDataFrame(pandas_vertices)
edges = sqlContext.createDataFrame(pandas_edges)

# COMMAND ----------

# Create GraphFrame
g = GraphFrame(vertices, edges)

# COMMAND ----------

# Print vertices
g.vertices.show()

# COMMAND ----------

#Print edges
g.edges.show()

# COMMAND ----------

# Print inDegree
g.inDegrees.sort('inDegree', ascending=False).show()

# COMMAND ----------

# Print outDegrees
g.outDegrees.sort('outDegree', ascending=False).show()

# COMMAND ----------

# Print degree
g.degrees.sort('degree', ascending=False).show()

# COMMAND ----------

pd.merge(left = g.outDegrees, right = g.inDegrees, on = 'id')


# COMMAND ----------

# %fs rm -r /FileStore/groups

# COMMAND ----------

# Print labelPropagation
lp = g.labelPropagation(maxIter=5)
lp.show()

# COMMAND ----------

#Print pageRank
pr = g.pageRank(resetProbability = 0.15, tol = 0.01).vertices.sort(
    'pagerank', ascending = False)
pr.show()

# COMMAND ----------

# Create a network
passes = sqlContext.createDataFrame(passes)
network = passes.join(lp, passes.PLAYER == lp.name, "inner")
network = network.join(pr,network.PLAYER == pr.name, "inner")
network = network[['PLAYER','PASS_TO','label','PASS','pagerank']]
network.collect()

# COMMAND ----------

# Make network available as a SQL table.
network.registerTempTable("network")

# COMMAND ----------

# MAGIC %sql select * from network

# COMMAND ----------

# MAGIC %scala if (org.apache.spark.BuildInfo.sparkBranch < "1.6") sys.error("Attach this notebook to a cluster running Spark 1.6+")

# COMMAND ----------

# MAGIC %scala
# MAGIC package d3
# MAGIC // We use a package object so that we can define top level classes like Edge that need to be used in other cells
# MAGIC 
# MAGIC import org.apache.spark.sql._
# MAGIC import com.databricks.backend.daemon.driver.EnhancedRDDFunctions.displayHTML
# MAGIC 
# MAGIC case class Edge(PLAYER: String, PASS_TO: String, PASS: Long, label: Long, pagerank: Double)
# MAGIC 
# MAGIC case class Node(name: String, label: Long, pagerank: Double)
# MAGIC case class Link(source: Int, target: Int, value: Long)
# MAGIC case class Graph(nodes: Seq[Node], links: Seq[Link])
# MAGIC 
# MAGIC object graphs {
# MAGIC val sqlContext = SQLContext.getOrCreate(org.apache.spark.SparkContext.getOrCreate())  
# MAGIC import sqlContext.implicits._
# MAGIC   
# MAGIC def force(network: Dataset[Edge], height: Int = 100, width: Int = 960): Unit = {
# MAGIC   val data = network.collect()
# MAGIC //   val nodes = (data.map(_.PLAYER) ++ data.map(_.PASS_TO)).map(_.replaceAll("_", " ")).toSet.toSeq.map(Node)
# MAGIC   val nodes = data.map { t =>
# MAGIC     Node(t.PLAYER, t.label, t.pagerank)}.distinct
# MAGIC   val links = data.map { t =>
# MAGIC     Link(nodes.indexWhere(_.name == t.PLAYER), nodes.indexWhere(_.name == t.PASS_TO), t.PASS / 20 + 1)
# MAGIC   }
# MAGIC   //     Link(nodes.indexWhere(_.name == t.PLAYER.replaceAll("_", " ")), nodes.indexWhere(_.name == t.PASS_TO.replaceAll("_", " ")), t.PASS / 20 + 1)
# MAGIC   showGraph(height, width, Seq(Graph(nodes, links)).toDF().toJSON.first())
# MAGIC }
# MAGIC 
# MAGIC /**
# MAGIC  * Displays a force directed graph using d3
# MAGIC  * input: {"nodes": [{"name": "..."}], "links": [{"source": 1, "target": 2, "value": 0}]}
# MAGIC  */
# MAGIC def showGraph(height: Int, width: Int, graph: String): Unit = {
# MAGIC 
# MAGIC displayHTML(s"""
# MAGIC <!DOCTYPE html>
# MAGIC <html>
# MAGIC <head>
# MAGIC   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
# MAGIC   <title>Polish Books Themes - an Interactive Map</title>
# MAGIC   <meta charset="utf-8">
# MAGIC <style>
# MAGIC 
# MAGIC .node_circle {
# MAGIC   stroke: #777;
# MAGIC   stroke-width: 1.3px;
# MAGIC }
# MAGIC 
# MAGIC .node_label {
# MAGIC   pointer-events: none;
# MAGIC }
# MAGIC 
# MAGIC .link {
# MAGIC   stroke: #777;
# MAGIC   stroke-opacity: .2;
# MAGIC }
# MAGIC 
# MAGIC .node_count {
# MAGIC   stroke: #777;
# MAGIC   stroke-width: 1.0px;
# MAGIC   fill: #999;
# MAGIC }
# MAGIC 
# MAGIC text.legend {
# MAGIC   font-family: Verdana;
# MAGIC   font-size: 13px;
# MAGIC   fill: #000;
# MAGIC }
# MAGIC 
# MAGIC .node text {
# MAGIC   font-family: "Helvetica Neue","Helvetica","Arial",sans-serif;
# MAGIC   font-size: 17px;
# MAGIC   font-weight: 200;
# MAGIC }
# MAGIC 
# MAGIC </style>
# MAGIC </head>
# MAGIC 
# MAGIC <body>
# MAGIC <script src="//d3js.org/d3.v3.min.js"></script>
# MAGIC <script>
# MAGIC 
# MAGIC var graph = $graph;
# MAGIC 
# MAGIC var width = $width,
# MAGIC     height = $height;
# MAGIC 
# MAGIC var color = d3.scale.category10();
# MAGIC 
# MAGIC var force = d3.layout.force()
# MAGIC     .charge(-700)
# MAGIC     .linkDistance(350)
# MAGIC     .size([width, height]);
# MAGIC 
# MAGIC var svg = d3.select("body").append("svg")
# MAGIC     .attr("width", width)
# MAGIC     .attr("height", height);
# MAGIC     
# MAGIC force
# MAGIC     .nodes(graph.nodes)
# MAGIC     .links(graph.links)
# MAGIC     .start();
# MAGIC 
# MAGIC var link = svg.selectAll(".link")
# MAGIC     .data(graph.links)
# MAGIC     .enter().append("line")
# MAGIC     .attr("class", "link")
# MAGIC     .style("stroke-width", function(d) { return Math.sqrt(d.value); });
# MAGIC 
# MAGIC var node = svg.selectAll(".node")
# MAGIC     .data(graph.nodes)
# MAGIC     .enter().append("g")
# MAGIC     .attr("class", "node")
# MAGIC     .call(force.drag);
# MAGIC 
# MAGIC node.append("circle")
# MAGIC     .attr("r", function(d) { return d.pagerank*10+4 ;})
# MAGIC     .style("fill", function(d) { return color(d.label);})
# MAGIC     .style("opacity", 0.5)
# MAGIC 
# MAGIC node.append("text")
# MAGIC       .attr("dx", 10)
# MAGIC       .attr("dy", ".35em")
# MAGIC       .text(function(d) { return d.name });
# MAGIC       
# MAGIC //Now we are giving the SVGs co-ordinates - the force layout is generating the co-ordinates which this code is using to update the attributes of the SVG elements
# MAGIC force.on("tick", function () {
# MAGIC     link.attr("x1", function (d) {
# MAGIC         return d.source.x;
# MAGIC     })
# MAGIC         .attr("y1", function (d) {
# MAGIC         return d.source.y;
# MAGIC     })
# MAGIC         .attr("x2", function (d) {
# MAGIC         return d.target.x;
# MAGIC     })
# MAGIC         .attr("y2", function (d) {
# MAGIC         return d.target.y;
# MAGIC     });
# MAGIC     d3.selectAll("circle").attr("cx", function (d) {
# MAGIC         return d.x;
# MAGIC     })
# MAGIC         .attr("cy", function (d) {
# MAGIC         return d.y;
# MAGIC     });
# MAGIC     d3.selectAll("text").attr("x", function (d) {
# MAGIC         return d.x;
# MAGIC     })
# MAGIC         .attr("y", function (d) {
# MAGIC         return d.y;
# MAGIC     });
# MAGIC });
# MAGIC </script>
# MAGIC </html>
# MAGIC """)
# MAGIC }
# MAGIC   
# MAGIC   def help() = {
# MAGIC displayHTML("""
# MAGIC <p>
# MAGIC Produces a force-directed graph given a collection of edges of the following form:</br>
# MAGIC <tt><font color="#a71d5d">case class</font> <font color="#795da3">Edge</font>(<font color="#ed6a43">PLAYER</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">PASS_TO</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">PASS</font>: <font color="#a71d5d">Long</font>, <font color="#ed6a43">label</font>: <font color="#a71d5d">Double</font>, <font color="#ed6a43">pagerank</font>: <font color="#a71d5d">Double</font>)</tt>
# MAGIC </p>
# MAGIC <p>Usage:<br/>
# MAGIC <tt>%scala</tt></br>
# MAGIC <tt><font color="#a71d5d">import</font> <font color="#ed6a43">d3._</font></tt><br/>
# MAGIC <tt><font color="#795da3">graphs.force</font>(</br>
# MAGIC &nbsp;&nbsp;<font color="#ed6a43">height</font> = <font color="#795da3">500</font>,<br/>
# MAGIC &nbsp;&nbsp;<font color="#ed6a43">width</font> = <font color="#795da3">500</font>,<br/>
# MAGIC &nbsp;&nbsp;<font color="#ed6a43">clicks</font>: <font color="#795da3">Dataset</font>[<font color="#795da3">Edge</font>])</tt>
# MAGIC </p>""")
# MAGIC   }
# MAGIC }

# COMMAND ----------

# MAGIC %scala 
# MAGIC import d3._
# MAGIC // print the help for the graphing library
# MAGIC d3.graphs.help()

# COMMAND ----------

# MAGIC %scala
# MAGIC import d3._
# MAGIC 
# MAGIC graphs.force(
# MAGIC   height = 800,
# MAGIC   width = 1000,
# MAGIC   network = sql("""
# MAGIC     SELECT 
# MAGIC       PLAYER,
# MAGIC       PASS_TO,
# MAGIC       PASS,
# MAGIC       label,
# MAGIC       pagerank
# MAGIC       FROM network
# MAGIC     """).as[Edge])

# COMMAND ----------

