# Databricks notebook source exported at Tue, 28 Jun 2016 11:17:28 UTC

# Analyzing Golden State Warriors' passing network using GraphFrames

** This notebook is created by [Yuki Katoh](https://de.linkedin.com/in/yukiegosapporo) and is a modified version of the article originally posted to [Opiate for the masses](http://opiateforthemass.es/articles/analyzing-golden-state-warriors-passing-network-using-graphframes-in-spark/) **

** Dataset: Golden State Warriors's pass data in 2015-16 regular season given by [NBA.com](http://stats.nba.com/) **

** Source: http://stats.nba.com/ **


<img width="1000px" src="https://github.com/yukiegosapporo/gsw_passing_network/blob/master/network.png?raw=true"/>

<i>*This notebook requires Spark 1.6+</i>





View the [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/07_MatthewHendtlass/054_Yuki_Katoh_GSW_Passing_Analysis.html) of this databricks notebook




 **WARNING:** Install the *graphframe* library before running the following commands. For instructions, see [here](https://databricks-staging-cloudfront.staging.cloud.databricks.com/public/c65da9a2fa40e45a2028cddebe45b54c/8637560089690848/2998808121595179/6977722904629137/latest.html).


```python

from graphframes import *
import pandas as pd
import os
import json

```
```python

# Get player IDs of Golden State Warriors
playerids = [201575, 201578, 2738, 202691, 101106, 2760, 2571, 203949, 203546, 203110, 201939, 203105, 2733, 1626172, 203084]

```
```python

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

```
```python

# Parse JSON files and create pandas DataFrame
raw = pd.DataFrame()
for playerid in playerids:
    with open("{playerid}.json".format(playerid=playerid)) as json_file:
        parsed = json.load(json_file)['resultSets'][0]
        raw = raw.append(
            pd.DataFrame(parsed['rowSet'], columns=parsed['headers']))

raw = raw.rename(columns={'PLAYER_NAME_LAST_FIRST': 'PLAYER'})
raw['id'] = raw['PLAYER'].str.replace(', ', '')

```
```python

# Create passes
passes = raw[raw['PASS_TO']
.isin(raw['PLAYER'])][['PLAYER', 'PASS_TO','PASS']]

```
```python

# Make raw vertices
pandas_vertices = raw[['PLAYER', 'id']].drop_duplicates()
pandas_vertices.columns = ['name', 'id']

```
```python

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

```
```python

# Bring the local vertices and edges to Spark
vertices = sqlContext.createDataFrame(pandas_vertices)
edges = sqlContext.createDataFrame(pandas_edges)

```
```python

# Create GraphFrame
g = GraphFrame(vertices, edges)

```
```python

# Print vertices
g.vertices.show()

```
```python

#Print edges
g.edges.show()

```
```python

# Print inDegree
g.inDegrees.sort('inDegree', ascending=False).show()

```
```python

# Print outDegrees
g.outDegrees.sort('outDegree', ascending=False).show()

```
```python

# Print degree
g.degrees.sort('degree', ascending=False).show()

```
```python

pd.merge(left = g.outDegrees, right = g.inDegrees, on = 'id')


```
```python

# %fs rm -r /FileStore/groups

```
```python

# Print labelPropagation
lp = g.labelPropagation(maxIter=5)
lp.show()

```
```python

#Print pageRank
pr = g.pageRank(resetProbability = 0.15, tol = 0.01).vertices.sort(
    'pagerank', ascending = False)
pr.show()

```
```python

# Create a network
passes = sqlContext.createDataFrame(passes)
network = passes.join(lp, passes.PLAYER == lp.name, "inner")
network = network.join(pr,network.PLAYER == pr.name, "inner")
network = network[['PLAYER','PASS_TO','label','PASS','pagerank']]
network.collect()

```
```python

# Make network available as a SQL table.
network.registerTempTable("network")

```
```python

%sql select * from network

```
```python

%scala if (org.apache.spark.BuildInfo.sparkBranch < "1.6") sys.error("Attach this notebook to a cluster running Spark 1.6+")

```
```python

%scala
package d3
// We use a package object so that we can define top level classes like Edge that need to be used in other cells

import org.apache.spark.sql._
import com.databricks.backend.daemon.driver.EnhancedRDDFunctions.displayHTML

case class Edge(PLAYER: String, PASS_TO: String, PASS: Long, label: Long, pagerank: Double)

case class Node(name: String, label: Long, pagerank: Double)
case class Link(source: Int, target: Int, value: Long)
case class Graph(nodes: Seq[Node], links: Seq[Link])

object graphs {
val sqlContext = SQLContext.getOrCreate(org.apache.spark.SparkContext.getOrCreate())  
import sqlContext.implicits._
  
def force(network: Dataset[Edge], height: Int = 100, width: Int = 960): Unit = {
  val data = network.collect()
//   val nodes = (data.map(_.PLAYER) ++ data.map(_.PASS_TO)).map(_.replaceAll("_", " ")).toSet.toSeq.map(Node)
  val nodes = data.map { t =>
    Node(t.PLAYER, t.label, t.pagerank)}.distinct
  val links = data.map { t =>
    Link(nodes.indexWhere(_.name == t.PLAYER), nodes.indexWhere(_.name == t.PASS_TO), t.PASS / 20 + 1)
  }
  //     Link(nodes.indexWhere(_.name == t.PLAYER.replaceAll("_", " ")), nodes.indexWhere(_.name == t.PASS_TO.replaceAll("_", " ")), t.PASS / 20 + 1)
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

var color = d3.scale.category10();

var force = d3.layout.force()
    .charge(-700)
    .linkDistance(350)
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
    .attr("r", function(d) { return d.pagerank*10+4 ;})
    .style("fill", function(d) { return color(d.label);})
    .style("opacity", 0.5)

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
<tt><font color="#a71d5d">case class</font> <font color="#795da3">Edge</font>(<font color="#ed6a43">PLAYER</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">PASS_TO</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">PASS</font>: <font color="#a71d5d">Long</font>, <font color="#ed6a43">label</font>: <font color="#a71d5d">Double</font>, <font color="#ed6a43">pagerank</font>: <font color="#a71d5d">Double</font>)</tt>
</p>
<p>Usage:<br/>
<tt>%scala</tt></br>
<tt><font color="#a71d5d">import</font> <font color="#ed6a43">d3._</font></tt><br/>
<tt><font color="#795da3">graphs.force</font>(</br>
&nbsp;&nbsp;<font color="#ed6a43">height</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">width</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">clicks</font>: <font color="#795da3">Dataset</font>[<font color="#795da3">Edge</font>])</tt>
</p>""")
  }
}

```
```python

%scala 
import d3._
// print the help for the graphing library
d3.graphs.help()

```
```python

%scala
import d3._

graphs.force(
  height = 800,
  width = 1000,
  network = sql("""
    SELECT 
      PLAYER,
      PASS_TO,
      PASS,
      label,
      pagerank
      FROM network
    """).as[Edge])

```
```python

```
