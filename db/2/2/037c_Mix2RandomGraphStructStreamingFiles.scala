// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/fXo7AfE3Umg/0.jpg)](https://www.youtube.com/embed/fXo7AfE3Umg?start=0&end=2670&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/sAttqpQq4nA/0.jpg)](https://www.youtube.com/embed/sAttqpQq4nA?start=0&end=2630&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/byAZtT_EdO4/0.jpg)](https://www.youtube.com/embed/byAZtT_EdO4?start=0&end=1045&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/bhHH74vkqHE/0.jpg)](https://www.youtube.com/embed/bhHH74vkqHE?start=0&end=487&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Task - do now
// MAGIC ## Write a mixture of two random graph models for file streaming later
// MAGIC 
// MAGIC We will use it as a basic simulator for timeseries of network data. This can be extended for specific domains like network security where extra fields can be added for protocols, ports, etc.
// MAGIC 
// MAGIC The raw ingredients are here... more or less.

// COMMAND ----------

// MAGIC %md
// MAGIC Read the code from github
// MAGIC 
// MAGIC * https://github.com/apache/spark/blob/master/graphx/src/main/scala/org/apache/spark/graphx/util/GraphGenerators.scala
// MAGIC * Also check out: https://github.com/graphframes/graphframes/blob/master/src/main/scala/org/graphframes/examples/Graphs.scala

// COMMAND ----------

// MAGIC %md
// MAGIC Let's focus on the two of the simplest (deterministic) graphs.

// COMMAND ----------

import scala.util.Random

import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.sql.functions.lit // import the lit function in sql
import org.graphframes._

/*
// A graph with edge attributes containing distances
val graph: Graph[Long, Double] = GraphGenerators.logNormalGraph(sc, numVertices = 50, seed=12345L).mapEdges { e => 
  // to make things nicer we assign 0 distance to itself
  if (e.srcId == e.dstId) 0.0 else Random.nextDouble()
}
*/

val graph: Graph[(Int,Int), Double] = GraphGenerators.gridGraph(sc, 5,5)


// COMMAND ----------

val g = GraphFrame.fromGraphX(graph)
val gE= g.edges.select($"src", $"dst".as("dest"), lit(1L).as("count")) // for us the column count is just an edge incidence

// COMMAND ----------

package d3
// We use a package object so that we can define top level classes like Edge that need to be used in other cells
// This was modified by Ivan Sadikov to make sure it is compatible the latest databricks notebook

import org.apache.spark.sql._
import com.databricks.backend.daemon.driver.EnhancedRDDFunctions.displayHTML

case class Edge(src: String, dest: String, count: Long)

case class Node(name: String)
case class Link(source: Int, target: Int, value: Long)
case class Graph(nodes: Seq[Node], links: Seq[Link])

object graphs {
// val sqlContext = SQLContext.getOrCreate(org.apache.spark.SparkContext.getOrCreate())  /// fix
val sqlContext = SparkSession.builder().getOrCreate().sqlContext
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

<div id="clicks-graph">
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

var svg = d3.select("#clicks-graph").append("svg")
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
</div>
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

d3.graphs.force(
  height = 500,
  width = 500,
  clicks = gE.as[d3.Edge])

// COMMAND ----------

val graphStar: Graph[Int, Int] = GraphGenerators.starGraph(sc, 10)
val gS = GraphFrame.fromGraphX(graphStar)
val gSE= gS.edges.select($"src", $"dst".as("dest"), lit(1L).as("count")) // for us the column count is just an edge incidence
d3.graphs.force(
  height = 500,
  width = 500,
  clicks = gSE.as[d3.Edge])

// COMMAND ----------

// MAGIC %md
// MAGIC # Now, write code to simulate from a mixture of graphs models
// MAGIC 
// MAGIC * See `037a_...` and `037b_...` notebooks for the file writing pattern.
// MAGIC * First try, grid and star with 98%-2% mixture, respectively
// MAGIC * Second, try a truly random graph like lognormal degree distributed random graph and star
// MAGIC * Try to make a simulation of random networks that is closer to your domain of application (you can always drop in to python and R for this part - even using non-distributed algorithms for simulating large enough networks per burst).

// COMMAND ----------

val graphGrid: Graph[(Int,Int), Double] = GraphGenerators.gridGraph(sc, 50,50)
val gG = GraphFrame.fromGraphX(graphGrid)
gG.edges.count

// COMMAND ----------

val graphStar: Graph[Int, Int] = GraphGenerators.starGraph(sc, 101)
val gS = GraphFrame.fromGraphX(graphStar)
gS.edges.count

// COMMAND ----------

val gAllEdges = gS.edges.union(gG.edges)
gAllEdges.count

// COMMAND ----------

100.0/5000.0

// COMMAND ----------

