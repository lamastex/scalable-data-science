// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/GbyTz8Z6E-M/0.jpg)](https://www.youtube.com/embed/GbyTz8Z6E-M?start=0&end=2991&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/FS6FdwNJDvY/0.jpg)](https://www.youtube.com/embed/FS6FdwNJDvY?start=0&end=2035&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Distributed Vertex Programming using GraphX
// MAGIC 
// MAGIC This is an augmentation of [http://go.databricks.com/hubfs/notebooks/3-GraphFrames-User-Guide-scala.html](http://go.databricks.com/hubfs/notebooks/3-GraphFrames-User-Guide-scala.html)
// MAGIC 
// MAGIC See:
// MAGIC 
// MAGIC * [https://amplab.cs.berkeley.edu/wp-content/uploads/2014/09/graphx.pdf](https://amplab.cs.berkeley.edu/wp-content/uploads/2014/09/graphx.pdf)
// MAGIC * [https://amplab.github.io/graphx/](https://amplab.github.io/graphx/)
// MAGIC * [https://spark.apache.org/docs/latest/graphx-programming-guide.html](https://spark.apache.org/docs/latest/graphx-programming-guide.html)
// MAGIC * [https://databricks.com/blog/2016/03/03/introducing-graphframes.html](https://databricks.com/blog/2016/03/03/introducing-graphframes.html)
// MAGIC * [https://databricks.com/blog/2016/03/16/on-time-flight-performance-with-spark-graphframes.html](https://databricks.com/blog/2016/03/16/on-time-flight-performance-with-spark-graphframes.html)
// MAGIC * [http://ampcamp.berkeley.edu/big-data-mini-course/graph-analytics-with-graphx.html](http://ampcamp.berkeley.edu/big-data-mini-course/graph-analytics-with-graphx.html)
// MAGIC 
// MAGIC And of course the databricks guide:
// MAGIC * [https://docs.databricks.com/spark/latest/graph-analysis/index.html](https://docs.databricks.com/spark/latest/graph-analysis/index.html)

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

displayHTML(frameIt("https://spark.apache.org/docs/latest/graphx-programming-guide.html#optimized-representation",800))

// COMMAND ----------

// MAGIC %md 
// MAGIC # GraphFrames User Guide (Scala)
// MAGIC 
// MAGIC GraphFrames is a package for Apache Spark which provides DataFrame-based Graphs. It provides high-level APIs in Scala, Java, and Python. It aims to provide both the functionality of GraphX and extended functionality taking advantage of Spark DataFrames. This extended functionality includes motif finding, DataFrame-based serialization, and highly expressive graph queries.
// MAGIC 
// MAGIC The GraphFrames package is available from [Spark Packages](http://spark-packages.org/package/graphframes/graphframes).
// MAGIC 
// MAGIC This notebook demonstrates examples from the [GraphFrames User Guide](http://graphframes.github.io/user-guide.html).

// COMMAND ----------

displayHTML(frameIt("https://databricks.com/blog/2016/03/03/introducing-graphframes.html",500))

// COMMAND ----------

// we first need to install the library - graphframes as a Spark package - and attach it to our cluster
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import org.graphframes._

// COMMAND ----------

// MAGIC %md
// MAGIC ##Creating GraphFrames
// MAGIC 
// MAGIC Let us try to create an example social network from the blog: 
// MAGIC * [https://databricks.com/blog/2016/03/03/introducing-graphframes.html](https://databricks.com/blog/2016/03/03/introducing-graphframes.html).
// MAGIC 
// MAGIC ![https://databricks.com/wp-content/uploads/2016/03/social-network-graph-diagram.png](https://databricks.com/wp-content/uploads/2016/03/social-network-graph-diagram.png)
// MAGIC 
// MAGIC Users can create GraphFrames from vertex and edge DataFrames.
// MAGIC 
// MAGIC * **Vertex DataFrame:** A vertex DataFrame should contain a special column named `id` which specifies unique IDs for each vertex in the graph.
// MAGIC * **Edge DataFrame:** An edge DataFrame should contain two special columns: `src` (source vertex ID of edge) and `dst` (destination vertex ID of edge).
// MAGIC 
// MAGIC Both DataFrames can have arbitrary other columns. Those columns can represent vertex and edge attributes.
// MAGIC 
// MAGIC In our example, we can use a GraphFrame can store data or properties associated with each vertex and edge. 
// MAGIC 
// MAGIC In our social network, each user might have an age and name, and each connection might have a relationship type.

// COMMAND ----------

// MAGIC %md
// MAGIC Create the vertices and edges

// COMMAND ----------

// Vertex DataFrame
val v = sqlContext.createDataFrame(List(
  ("a", "Alice", 34),
  ("b", "Bob", 36),
  ("c", "Charlie", 30),
  ("d", "David", 29),
  ("e", "Esther", 32),
  ("f", "Fanny", 36),
  ("g", "Gabby", 60)
)).toDF("id", "name", "age")
// Edge DataFrame
val e = sqlContext.createDataFrame(List(
  ("a", "b", "friend"),
  ("b", "c", "follow"),
  ("c", "b", "follow"),
  ("f", "c", "follow"),
  ("e", "f", "follow"),
  ("e", "d", "friend"),
  ("d", "a", "friend"),
  ("a", "e", "friend")
)).toDF("src", "dst", "relationship")

// COMMAND ----------

// MAGIC %md 
// MAGIC Let's create a graph from these vertices and these edges:

// COMMAND ----------

val g = GraphFrame(v, e)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's use the d3.graphs to visualise graphs (recall the D3 graphs in wiki-click example).

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

d3.graphs.help()

// COMMAND ----------

import org.apache.spark.sql.functions.lit // import the lit function in sql
val gE= g.edges.select($"src", $"dst".as("dest"), lit(1L).as("count")) // for us the column count is just an edge incidence

// COMMAND ----------

display(gE)

// COMMAND ----------

d3.graphs.force(
  height = 500,
  width = 500,
  clicks = gE.as[d3.Edge])

// COMMAND ----------

// This example graph also comes with the GraphFrames package.
val g0 = examples.Graphs.friends

// COMMAND ----------

d3.graphs.force( // let us see g0 now in one cell
  height = 500,
  width = 500,
  clicks = g0.edges.select($"src", $"dst".as("dest"), lit(1L).as("count")).as[d3.Edge])

// COMMAND ----------

// MAGIC %md
// MAGIC ## Basic graph and DataFrame queries
// MAGIC 
// MAGIC GraphFrames provide several simple graph queries, such as node degree.
// MAGIC 
// MAGIC Also, since GraphFrames represent graphs as pairs of vertex and edge DataFrames, it is easy to make powerful queries directly on the vertex and edge DataFrames. Those DataFrames are made available as vertices and edges fields in the GraphFrame.
// MAGIC 
// MAGIC ### Simple queries are simple
// MAGIC 
// MAGIC GraphFrames make it easy to express queries over graphs. Since GraphFrame vertices and edges are stored as DataFrames, many queries are just DataFrame (or SQL) queries.

// COMMAND ----------

display(g.vertices)

// COMMAND ----------

display(g0.vertices) // this is the same query on the graph loaded as an example from GraphFrame package

// COMMAND ----------

display(g.edges)

// COMMAND ----------

// MAGIC %md 
// MAGIC The incoming degree of the vertices:

// COMMAND ----------

display(g.inDegrees)

// COMMAND ----------

// MAGIC %md 
// MAGIC The outgoing degree of the vertices:

// COMMAND ----------

display(g.outDegrees)

// COMMAND ----------

// MAGIC %md 
// MAGIC The degree of the vertices:

// COMMAND ----------

display(g.degrees)

// COMMAND ----------

// MAGIC %md 
// MAGIC You can run queries directly on the vertices DataFrame. For example, we can find the age of the youngest person in the graph:

// COMMAND ----------

val youngest = g.vertices.groupBy().min("age")
display(youngest)

// COMMAND ----------

// MAGIC %md 
// MAGIC Likewise, you can run queries on the edges DataFrame. 
// MAGIC 
// MAGIC For example, let us count the number of 'follow' relationships in the graph:

// COMMAND ----------

val numFollows = g.edges.filter("relationship = 'follow'").count()

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
val motifs = g.find("(a)-[e1]->(b); (b)-[e2]->(a)")
display(motifs)

// COMMAND ----------

// MAGIC %md 
// MAGIC Since the result is a DataFrame, more complex queries can be built on top of the motif. 
// MAGIC 
// MAGIC Let us find all the reciprocal relationships in which one person is older than 30:

// COMMAND ----------

val filtered = motifs.filter("b.age > 30")
display(filtered)

// COMMAND ----------

// MAGIC %md
// MAGIC **You Try!**

// COMMAND ----------

//Search for all "directed triangles" or triplets of vertices: a,b,c with edges: a->b, b->c and c->a
//uncomment the next 2 lines and replace the "..." below
val motifs3 = g.find("(a)-[e1]->(b); (b)-[e2]->(c); (c)-[e3]->(a) ")
display(motifs3)

// COMMAND ----------

// MAGIC %md
// MAGIC **Stateful queries**
// MAGIC 
// MAGIC Many motif queries are stateless and simple to express, as in the examples above. The next examples demonstrate more complex queries which carry state along a path in the motif. These queries can be expressed by combining GraphFrame motif finding with filters on the result, where the filters use sequence operations to construct a series of DataFrame Columns.
// MAGIC 
// MAGIC For example, suppose one wishes to identify a chain of 4 vertices with some property defined by a sequence of functions. That is, among chains of 4 vertices `a->b->c->d`, identify the subset of chains matching this complex filter:
// MAGIC 
// MAGIC * Initialize state on path.
// MAGIC * Update state based on vertex a.
// MAGIC * Update state based on vertex b.
// MAGIC * Etc. for c and d.
// MAGIC * If final state matches some condition, then the chain is accepted by the filter.
// MAGIC 
// MAGIC The below code snippets demonstrate this process, where we identify chains of 4 vertices such that at least 2 of the 3 edges are `friend` relationships. In this example, the state is the current count of `friend` edges; in general, it could be any DataFrame Column.

// COMMAND ----------

// Find chains of 4 vertices.
val chain4 = g.find("(a)-[ab]->(b); (b)-[bc]->(c); (c)-[cd]->(d)")

// Query on sequence, with state (cnt)
//  (a) Define method for updating state given the next element of the motif.
def sumFriends(cnt: Column, relationship: Column): Column = {
  when(relationship === "friend", cnt + 1).otherwise(cnt)
}
//  (b) Use sequence operation to apply method to sequence of elements in motif.
//      In this case, the elements are the 3 edges.
val condition = Seq("ab", "bc", "cd").
  foldLeft(lit(0))((cnt, e) => sumFriends(cnt, col(e)("relationship")))
//  (c) Apply filter to DataFrame.
val chainWith2Friends2 = chain4.where(condition >= 2)
display(chainWith2Friends2)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Bold idea!
// MAGIC Can you think of a way to use stateful queries in social media networks to find perpetrators of hate-speech online who are possibly worthy of an investigation by domain experts, say in the intelligence or security domain, for potential prosecution on charges of having incited another person to cause physical violence... This is a real problem today!
// MAGIC 
// MAGIC An idea for a product?

// COMMAND ----------

// MAGIC %md
// MAGIC ###Subgraphs
// MAGIC 
// MAGIC Subgraphs are built by filtering a subset of edges and vertices. For example, the following subgraph only contains people who are friends and who are more than 30 years old.

// COMMAND ----------

// Select subgraph of users older than 30, and edges of type "friend"
val v2 = g.vertices.filter("age > 30")
val e2 = g.edges.filter("relationship = 'friend'")
val g2 = GraphFrame(v2, e2)

// COMMAND ----------

display(g2.vertices)

// COMMAND ----------

display(g2.edges)

// COMMAND ----------

d3.graphs.force( // let us see g2 now in one cell
  height = 500,
  width = 500,
  clicks = g2.edges.select($"src", $"dst".as("dest"), lit(1L).as("count")).as[d3.Edge])

// COMMAND ----------

// MAGIC %md 
// MAGIC **Complex triplet filters**
// MAGIC 
// MAGIC The following example shows how to select a subgraph based upon triplet filters which operate on:
// MAGIC 
// MAGIC * an edge and 
// MAGIC * its src and 
// MAGIC * dst vertices. 
// MAGIC 
// MAGIC This example could be extended to go beyond triplets by using more complex motifs.

// COMMAND ----------

// Select subgraph based on edges "e" of type "follow"
// pointing from a younger user "a" to an older user "b".
val paths = g.find("(a)-[e]->(b)")
  .filter("e.relationship = 'follow'")
  .filter("a.age < b.age")
// "paths" contains vertex info. Extract the edges.
val e2 = paths.select("e.src", "e.dst", "e.relationship")
// In Spark 1.5+, the user may simplify this call:
//  val e2 = paths.select("e.*")

// Construct the subgraph
val g2 = GraphFrame(g.vertices, e2)

// COMMAND ----------

display(g2.vertices)

// COMMAND ----------

display(g2.edges)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Standard graph algorithms
// MAGIC 
// MAGIC GraphFrames comes with a number of standard graph algorithms built in:
// MAGIC 
// MAGIC * Breadth-first search (BFS)
// MAGIC * Connected components
// MAGIC * Strongly connected components
// MAGIC * Label Propagation Algorithm (LPA)
// MAGIC * PageRank
// MAGIC * Shortest paths
// MAGIC * Triangle count

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Breadth-first search (BFS)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Read
// MAGIC 
// MAGIC * [graphframes user-guide breadth-first-search-bfs](http://graphframes.github.io/user-guide.html#breadth-first-search-bfs).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Breadth-first_search",500))

// COMMAND ----------

// MAGIC %md
// MAGIC Search from "Esther" for users of age < 32.

// COMMAND ----------

// Search from "Esther" for users of age <= 32.
val paths: DataFrame = g.bfs.fromExpr("name = 'Esther'").toExpr("age < 32").run()
display(paths)

// COMMAND ----------

val paths: DataFrame = g.bfs.fromExpr("name = 'Esther' OR name = 'Bob'").toExpr("age < 32").run()
display(paths)

// COMMAND ----------

// MAGIC %md 
// MAGIC The search may also be limited by edge filters and maximum path lengths.

// COMMAND ----------

val filteredPaths = g.bfs.fromExpr("name = 'Esther'").toExpr("age < 32")
  .edgeFilter("relationship != 'friend'")
  .maxPathLength(3)
  .run()
display(filteredPaths)

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ### Connected components
// MAGIC 
// MAGIC Compute the connected component membership of each vertex and return a graph with each vertex assigned a component ID.
// MAGIC 
// MAGIC READ [http://graphframes.github.io/user-guide.html#connected-components](http://graphframes.github.io/user-guide.html#connected-components).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Connected_component_(graph_theory)",500))

// COMMAND ----------

// MAGIC %md
// MAGIC From [http://graphframes.github.io/user-guide.html#connected-components](http://graphframes.github.io/user-guide.html#connected-components):-
// MAGIC 
// MAGIC NOTE: With GraphFrames 0.3.0 and later releases, the default Connected Components algorithm requires setting a Spark checkpoint directory. Users can revert to the old algorithm using `.setAlgorithm("graphx")`.
// MAGIC 
// MAGIC See [https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-rdd-checkpointing.html](https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-rdd-checkpointing.html) to see why we need to check-point to keep the RDD lineage DAGs from growing out of control.

// COMMAND ----------

sc.setCheckpointDir("/_checkpoint") // just a directory in distributed file system
val result = g.connectedComponents.run() 
display(result)

// COMMAND ----------

// MAGIC %md
// MAGIC Fun Exercise: Try to modify the d3.graph function to allow a visualisation of a given Sequence of `component` ids in the above `result`.

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ## Strongly connected components
// MAGIC 
// MAGIC Compute the strongly connected component (SCC) of each vertex and return a graph with each vertex assigned to the SCC containing that vertex.
// MAGIC 
// MAGIC READ [http://graphframes.github.io/user-guide.html#strongly-connected-components](http://graphframes.github.io/user-guide.html#strongly-connected-components).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Strongly_connected_component",500))

// COMMAND ----------

val result = g.stronglyConnectedComponents.maxIter(10).run()
display(result.orderBy("component"))

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ## Label propagation
// MAGIC 
// MAGIC Run static Label Propagation Algorithm for detecting communities in networks.
// MAGIC 
// MAGIC Each node in the network is initially assigned to its own community. At every superstep, nodes send their community affiliation to all neighbors and update their state to the mode community affiliation of incoming messages.
// MAGIC 
// MAGIC LPA is a standard community detection algorithm for graphs. It is very inexpensive computationally, although 
// MAGIC 
// MAGIC * (1) convergence is not guaranteed and 
// MAGIC * (2) one can end up with trivial solutions (all nodes are identified into a single community).
// MAGIC 
// MAGIC READ: [http://graphframes.github.io/user-guide.html#label-propagation-algorithm-lpa](http://graphframes.github.io/user-guide.html#label-propagation-algorithm-lpa).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Label_Propagation_Algorithm",500))

// COMMAND ----------

val result = g.labelPropagation.maxIter(5).run()
display(result.orderBy("label"))

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ## PageRank
// MAGIC 
// MAGIC Identify important vertices in a graph based on connections.
// MAGIC 
// MAGIC READ: [http://graphframes.github.io/user-guide.html#pagerank](http://graphframes.github.io/user-guide.html#pagerank).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/PageRank",500))

// COMMAND ----------

// Run PageRank until convergence to tolerance "tol".
val results = g.pageRank.resetProbability(0.15).tol(0.01).run()
display(results.vertices)

// COMMAND ----------

display(results.edges)

// COMMAND ----------

// Run PageRank for a fixed number of iterations.
val results2 = g.pageRank.resetProbability(0.15).maxIter(10).run()
display(results2.vertices)

// COMMAND ----------

// Run PageRank personalized for vertex "a"
val results3 = g.pageRank.resetProbability(0.15).maxIter(10).sourceId("a").run()
display(results3.vertices)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Shortest paths
// MAGIC 
// MAGIC Computes shortest paths to the given set of landmark vertices, where landmarks are specified by vertex ID.
// MAGIC 
// MAGIC READ [http://graphframes.github.io/user-guide.html#shortest-paths](http://graphframes.github.io/user-guide.html#shortest-paths).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Shortest_path_problem",500))

// COMMAND ----------

val paths = g.shortestPaths.landmarks(Seq("a", "d")).run()
display(paths)

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ### Triangle count
// MAGIC 
// MAGIC Computes the number of triangles passing through each vertex.

// COMMAND ----------

// MAGIC %md
// MAGIC See [http://graphframes.github.io/user-guide.html#triangle-count](http://graphframes.github.io/user-guide.html#triangle-count)

// COMMAND ----------

val results = g.triangleCount.run()
display(results)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC There is a lot more... dig into the docs to find out about belief propogation algorithm now!

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC See [http://graphframes.github.io/user-guide.html#message-passing-via-aggregatemessages](http://graphframes.github.io/user-guide.html#message-passing-via-aggregatemessages)