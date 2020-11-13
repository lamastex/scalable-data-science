// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Distributed Vertex Programming using GraphX
// MAGIC 
// MAGIC This is an augmentation of [http://go.databricks.com/hubfs/notebooks/3-GraphFrames-User-Guide-scala.html](http://go.databricks.com/hubfs/notebooks/3-GraphFrames-User-Guide-scala.html) that was last retrieved in 2019.
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
// MAGIC 
// MAGIC *Use the source, Luke/Lea!*
// MAGIC 
// MAGIC - [https://github.com/graphframes/graphframes](https://github.com/graphframes/graphframes)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Community Packages in Spark - more generally
// MAGIC 
// MAGIC Let us recall the following quoate in Chapter 10 of *High Performance Spark* book (needs access to Orielly publishers via your library/subscription):
// MAGIC  - https://learning.oreilly.com/library/view/high-performance-spark/9781491943199/ch10.html#components
// MAGIC  
// MAGIC  > Beyond the integrated components, the community packages can add important functionality to Spark, sometimes even superseding built-in functionality—like with GraphFrames. 
// MAGIC  
// MAGIC Here we introduce you to GraphFrames quickly so you don't need to drop down to the GraphX library that requires more understanding of caching and checkpointing to keep the vertex program's DAG from exploding or becoming inefficient.

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
// MAGIC This notebook demonstrates examples from the GraphFrames User Guide: [https://graphframes.github.io/graphframes/docs/_site/user-guide.html](https://graphframes.github.io/graphframes/docs/_site/user-guide.html).

// COMMAND ----------

sc.version // link the right library depending on Spark version of the cluster that's running
// spark version 2.3.0 works with graphframes:graphframes:0.7.0-spark2.3-s_2.11
// spark version 3.0.1 works with graphframes:graphframes:0.8.1-spark3.0-s_2.12

// COMMAND ----------

// MAGIC %md
// MAGIC Since databricks.com stopped allowing IFrame embeds we have to open it in a separate window now. The blog is insightful and worth a perusal:
// MAGIC 
// MAGIC - https://databricks.com/blog/2016/03/03/introducing-graphframes.html

// COMMAND ----------

// we first need to install the library - graphframes as a Spark package - and attach it to our cluster - see note two cells above!
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
// MAGIC Let's use the d3.graphs to visualise graphs (recall the D3 graphs in wiki-click example). You need the `Run Cell` below using that cell's *Play* button's drop-down menu.

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
// MAGIC Check out the GraphFrame User Guide at [https://graphframes.github.io/graphframes/docs/_site/user-guide.html](https://graphframes.github.io/graphframes/docs/_site/user-guide.html) for more details on the API.

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
//uncomment the next 2 lines and replace the "XXX" below
//val motifs3 = g.find("(a)-[e1]->(b); (b)-[e2]->(c); (c)-[e3]->(XXX)")
//display(motifs3)

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

chain4

// COMMAND ----------

chain4.printSchema

// COMMAND ----------

// MAGIC %md
// MAGIC ### An idea -- a diatribe into an AI security product.
// MAGIC Can you think of a way to use stateful queries in social media networks to find perpetrators of hate-speech online who are possibly worthy of an investigation by domain experts, say in the intelligence or security domain, for potential prosecution on charges of having incited another person to cause physical violence... This is a real problem today as Swedish law effectively prohibits certain forms of online hate-speech.
// MAGIC 
// MAGIC An idea for a product that can be used by Swedish security agencies?
// MAGIC 
// MAGIC See [https://näthatsgranskaren.se/](https://näthatsgranskaren.se/) for details of a non-profit in Sweden doing such operaitons mostly manually as of early 2020.

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
// MAGIC ## Standard graph algorithms in GraphX conveniently via GraphFrames
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
// MAGIC https://graphframes.github.io/graphframes/docs/_site/user-guide.html
// MAGIC 
// MAGIC * [graphframes user-guide breadth-first-search-bfs](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#breadth-first-search-bfs).

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
// MAGIC READ [https://graphframes.github.io/graphframes/docs/_site/user-guide.html#connected-components](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#connected-components).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Connected_component_(graph_theory)",500))

// COMMAND ----------

// MAGIC %md
// MAGIC From [https://graphframes.github.io/graphframes/docs/_site/user-guide.html#connected-components](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#connected-components):-
// MAGIC 
// MAGIC NOTE: With GraphFrames 0.3.0 and later releases, the default Connected Components algorithm requires setting a Spark checkpoint directory. Users can revert to the old algorithm using `.setAlgorithm("graphx")`.
// MAGIC 
// MAGIC Recall the following quote from [Chapter 5 on *Effective Transformations* of the *High Performance Spark* Book](https://learning.oreilly.com/library/view/high-performance-spark/9781491943199/ch05.html) why one needs to check-point to keep the RDD lineage DAGs from growing too large.
// MAGIC 
// MAGIC > **Types of Reuse: Cache, Persist, Checkpoint, Shuffle Files** 
// MAGIC > If you decide that you need to reuse your RDD, Spark provides a multitude of options for how to store the RDD. Thus it is important to understand when to use the various types of persistence.There are three primary operations that you can use to store your RDD: cache, persist, and checkpoint. In general, caching (equivalent to persisting with the in-memory storage) and persisting are most useful to avoid recomputation during one Spark job or to break RDDs with long lineages, since they keep an RDD on the executors during a Spark job. **Checkpointing is most useful to prevent failures and a high cost of recomputation by saving intermediate results. Like persisting, checkpointing helps avoid computation, thus minimizing the cost of failure, and avoids recomputation by breaking the lineage graph.**

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
// MAGIC READ [https://graphframes.github.io/graphframes/docs/_site/user-guide.html#strongly-connected-components](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#strongly-connected-components).

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
// MAGIC READ: [https://graphframes.github.io/graphframes/docs/_site/user-guide.html#label-propagation-algorithm-lpa](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#label-propagation-algorithm-lpa).

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
// MAGIC READ: [https://graphframes.github.io/graphframes/docs/_site/user-guide.html#pagerank](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#pagerank).

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
// MAGIC READ [https://graphframes.github.io/graphframes/docs/_site/user-guide.html#shortest-paths](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#shortest-paths).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Shortest_path_problem",500))

// COMMAND ----------

val paths = g.shortestPaths.landmarks(Seq("a", "d")).run()
display(paths)

// COMMAND ----------

g.edges.show()

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ### Triangle count
// MAGIC 
// MAGIC Computes the number of triangles passing through each vertex.

// COMMAND ----------

// MAGIC %md
// MAGIC See [https://graphframes.github.io/graphframes/docs/_site/user-guide.html#triangle-count](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#triangle-count)

// COMMAND ----------

val results = g.triangleCount.run()
display(results)

// COMMAND ----------

// MAGIC %md
// MAGIC ## YouTry
// MAGIC  Read about [https://graphframes.github.io/graphframes/docs/_site/user-guide.html#message-passing-via-aggregatemessages](https://graphframes.github.io/graphframes/docs/_site/user-guide.html#message-passing-via-aggregatemessages)
// MAGIC  
// MAGIC and undestand how the below code snippet shows how to use aggregateMessages to compute the sum of the ages of adjacent users.

// COMMAND ----------

import org.graphframes.{examples,GraphFrame}
import org.graphframes.lib.AggregateMessages
val g: GraphFrame = examples.Graphs.friends  // get example graph

// We will use AggregateMessages utilities later, so name it "AM" for short.
val AM = AggregateMessages

// For each user, sum the ages of the adjacent users.
val msgToSrc = AM.dst("age")
val msgToDst = AM.src("age")
val agg = { g.aggregateMessages
  .sendToSrc(msgToSrc)  // send destination user's age to source
  .sendToDst(msgToDst)  // send source user's age to destination
  .agg(sum(AM.msg).as("summedAges")) } // sum up ages, stored in AM.msg column
agg.show()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC There is a lot more that can be done with aggregate messaging - let's get into belief propogation algorithm for a more complex example!
// MAGIC 
// MAGIC Belief propogation is a powerful computational framework for Graphical Models. 
// MAGIC 
// MAGIC - let's dive here:
// MAGIC   - [https://github.com/graphframes/graphframes/blob/master/src/main/scala/org/graphframes/examples/BeliefPropagation.scala](https://github.com/graphframes/graphframes/blob/master/src/main/scala/org/graphframes/examples/BeliefPropagation.scala)
// MAGIC 
// MAGIC as
// MAGIC > This provides a template for building customized BP algorithms for different types of graphical models.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Belief_propagation",500))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Ising_model",500))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Project Idea
// MAGIC 
// MAGIC Understand *parallel belief propagation using colored fields* in the Scala code linked above and also pasted below in one cell (for you to modify if you want to do it in a databricks or jupyter or zeppelin notebook) unless you want to fork and extend the github repo directly with your own example.
// MAGIC 
// MAGIC Then use it with necessary adaptations to be able to model your favorite interacting particle system. Don't just redo the Ising model done there!
// MAGIC 
// MAGIC This can be used to gain intuition for various real-world scenarios, including the mathematics in your head:
// MAGIC 
// MAGIC - Make a graph for contact network of a set of hosts 
// MAGIC - A simple model of COVID spreading in an SI or SIS or SIR or other epidemic models
// MAGIC   - this can be abstract and simply show your skills in programming, say create a random network
// MAGIC   - or be more explicit with some assumptions about the contact process (population sampled, in one or two cities, with some assumptions on contacts during transportation, school, work, etc)
// MAGIC   - show that you have a fully scalable simulation model that can theoretically scale to billions of hosts
// MAGIC   
// MAGIC The project does not have to be a recommendation to Swedish authorities! Just a start in the right direction, for instance.
// MAGIC 
// MAGIC Some readings that can help here include the following and references therein:
// MAGIC 
// MAGIC - The Transmission Process: A Combinatorial Stochastic Process for the Evolution of Transmission Trees over Networks, Raazesh Sainudiin and David Welch, Journal of Theoretical Biology, Volume 410, Pages 137–170, [10.1016/j.jtbi.2016.07.038](http://dx.doi.org/10.1016/j.jtbi.2016.07.038), 2016.
// MAGIC 
// MAGIC ## Other Project Ideas
// MAGIC 
// MAGIC - try to do a scalable inference algorithm for one of the graphical models that you already know...
// MAGIC - make a large simulaiton of your favourite *Finite Markov Information Exchange (FMIE)* process defined by Aldous (see reference in the above linked paper)
// MAGIC - anything else that fancies you or your research orientation/interests and can benefit from adapting the template for the *parallel belief propagation* algorithm here.

// COMMAND ----------

// MAGIC %md
// MAGIC If you want to do this project in databricks (or other) notebook then start by modifying the following code from the example and making it run... Then adapt... start in small steps... make a team with fellow students with complementary skills...

// COMMAND ----------

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphframes.examples

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx.{Graph, VertexRDD, Edge => GXEdge}
import org.apache.spark.sql.{Column, Row, SparkSession, SQLContext}
import org.apache.spark.sql.functions.{col, lit, sum, udf, when}

import org.graphframes.GraphFrame
import org.graphframes.examples.Graphs.gridIsingModel
import org.graphframes.lib.AggregateMessages


/**
 * Example code for Belief Propagation (BP)
 *
 * This provides a template for building customized BP algorithms for different types of
 * graphical models.
 *
 * This example:
 *  - Ising model on a grid
 *  - Parallel Belief Propagation using colored fields
 *
 * Ising models are probabilistic graphical models over binary variables x,,i,,.
 * Each binary variable x,,i,, corresponds to one vertex, and it may take values -1 or +1.
 * The probability distribution P(X) (over all x,,i,,) is parameterized by vertex factors a,,i,,
 * and edge factors b,,ij,,:
 * {{{
 *  P(X) = (1/Z) * exp[ \sum_i a_i x_i + \sum_{ij} b_{ij} x_i x_j ]
 * }}}
 * where Z is the normalization constant (partition function).
 * See [[https://en.wikipedia.org/wiki/Ising_model Wikipedia]] for more information on Ising models.
 *
 * Belief Propagation (BP) provides marginal probabilities of the values of the variables x,,i,,,
 * i.e., P(x,,i,,) for each i.  This allows a user to understand likely values of variables.
 * See [[https://en.wikipedia.org/wiki/Belief_propagation Wikipedia]] for more information on BP.
 *
 * We use a batch synchronous BP algorithm, where batches of vertices are updated synchronously.
 * We follow the mean field update algorithm in Slide 13 of the
 * [[http://www.eecs.berkeley.edu/~wainwrig/Talks/A_GraphModel_Tutorial  talk slides]] from:
 *  Wainwright. "Graphical models, message-passing algorithms, and convex optimization."
 *
 * The batches are chosen according to a coloring.  For background on graph colorings for inference,
 * see for example:
 *  Gonzalez et al. "Parallel Gibbs Sampling: From Colored Fields to Thin Junction Trees."
 *  AISTATS, 2011.
 *
 * The BP algorithm works by:
 *  - Coloring the graph by assigning a color to each vertex such that no neighboring vertices
 *    share the same color.
 *  - In each step of BP, update all vertices of a single color.  Alternate colors.
 */
object BeliefPropagation {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("BeliefPropagation example")
      .getOrCreate()

    val sql = spark.sqlContext

    // Create graphical model g of size 3 x 3.
    val g = gridIsingModel(sql, 3)

    println("Original Ising model:")
    g.vertices.show()
    g.edges.show()

    // Run BP for 5 iterations.
    val numIter = 5
    val results = runBPwithGraphX(g, numIter)

    // Display beliefs.
    val beliefs = results.vertices.select("id", "belief")
    println(s"Done with BP. Final beliefs after $numIter iterations:")
    beliefs.show()

    spark.stop()
  }

  /**
   * Given a GraphFrame, choose colors for each vertex.  No neighboring vertices will share the
   * same color.  The number of colors is minimized.
   *
   * This is written specifically for grid graphs. For non-grid graphs, it should be generalized,
   * such as by using a greedy coloring scheme.
   *
   * @param g  Grid graph generated by [[org.graphframes.examples.Graphs.gridIsingModel()]]
   * @return  Same graph, but with a new vertex column "color" of type Int (0 or 1)
   */
  private def colorGraph(g: GraphFrame): GraphFrame = {
    val colorUDF = udf { (i: Int, j: Int) => (i + j) % 2 }
    val v = g.vertices.withColumn("color", colorUDF(col("i"), col("j")))
    GraphFrame(v, g.edges)
  }

  /**
   * Run Belief Propagation.
   *
   * This implementation of BP shows how to use GraphX's aggregateMessages method.
   * It is simple to convert to and from GraphX format.  This method does the following:
   *  - Color GraphFrame vertices for BP scheduling.
   *  - Convert GraphFrame to GraphX format.
   *  - Run BP using GraphX's aggregateMessages API.
   *  - Augment the original GraphFrame with the BP results (vertex beliefs).
   *
   * @param g  Graphical model created by `org.graphframes.examples.Graphs.gridIsingModel()`
   * @param numIter  Number of iterations of BP to run.  One iteration includes updating each
   *                 vertex's belief once.
   * @return  Same graphical model, but with [[GraphFrame.vertices]] augmented with a new column
   *          "belief" containing P(x,,i,, = +1), the marginal probability of vertex i taking
   *          value +1 instead of -1.
   */
  def runBPwithGraphX(g: GraphFrame, numIter: Int): GraphFrame = {
    // Choose colors for vertices for BP scheduling.
    val colorG = colorGraph(g)
    val numColors: Int = colorG.vertices.select("color").distinct.count().toInt

    // Convert GraphFrame to GraphX, and initialize beliefs.
    val gx0 = colorG.toGraphX
    // Schema maps for extracting attributes
    val vColsMap = colorG.vertexColumnMap
    val eColsMap = colorG.edgeColumnMap
    // Convert vertex attributes to nice case classes.
    val gx1: Graph[VertexAttr, Row] = gx0.mapVertices { case (_, attr) =>
      // Initialize belief at 0.0
      VertexAttr(attr.getDouble(vColsMap("a")), 0.0, attr.getInt(vColsMap("color")))
    }
    // Convert edge attributes to nice case classes.
    val extractEdgeAttr: (GXEdge[Row] => EdgeAttr) = { e =>
      EdgeAttr(e.attr.getDouble(eColsMap("b")))
    }
    var gx: Graph[VertexAttr, EdgeAttr] = gx1.mapEdges(extractEdgeAttr)

    // Run BP for numIter iterations.
    for (iter <- Range(0, numIter)) {
      // For each color, have that color receive messages from neighbors.
      for (color <- Range(0, numColors)) {
        // Send messages to vertices of the current color.
        val msgs: VertexRDD[Double] = gx.aggregateMessages(
          ctx =>
            // Can send to source or destination since edges are treated as undirected.
            if (ctx.dstAttr.color == color) {
              val msg = ctx.attr.b * ctx.srcAttr.belief
              // Only send message if non-zero.
              if (msg != 0) ctx.sendToDst(msg)
            } else if (ctx.srcAttr.color == color) {
              val msg = ctx.attr.b * ctx.dstAttr.belief
              // Only send message if non-zero.
              if (msg != 0) ctx.sendToSrc(msg)
            },
          _ + _)
        // Receive messages, and update beliefs for vertices of the current color.
        gx = gx.outerJoinVertices(msgs) {
          case (vID, vAttr, optMsg) =>
            if (vAttr.color == color) {
              val x = vAttr.a + optMsg.getOrElse(0.0)
              val newBelief = math.exp(-log1pExp(-x))
              VertexAttr(vAttr.a, newBelief, color)
            } else {
              vAttr
            }
        }
      }
    }

    // Convert back to GraphFrame with a new column "belief" for vertices DataFrame.
    val gxFinal: Graph[Double, Unit] = gx.mapVertices((_, attr) => attr.belief).mapEdges(_ => ())
    GraphFrame.fromGraphX(colorG, gxFinal, vertexNames = Seq("belief"))
  }

  case class VertexAttr(a: Double, belief: Double, color: Int)

  case class EdgeAttr(b: Double)

  /**
   * Run Belief Propagation.
   *
   * This implementation of BP shows how to use GraphFrame's aggregateMessages method.
   *  - Color GraphFrame vertices for BP scheduling.
   *  - Run BP using GraphFrame's aggregateMessages API.
   *  - Augment the original GraphFrame with the BP results (vertex beliefs).
   *
   * @param g  Graphical model created by `org.graphframes.examples.Graphs.gridIsingModel()`
   * @param numIter  Number of iterations of BP to run.  One iteration includes updating each
   *                 vertex's belief once.
   * @return  Same graphical model, but with [[GraphFrame.vertices]] augmented with a new column
   *          "belief" containing P(x,,i,, = +1), the marginal probability of vertex i taking
   *          value +1 instead of -1.
   */
  def runBPwithGraphFrames(g: GraphFrame, numIter: Int): GraphFrame = {
    // Choose colors for vertices for BP scheduling.
    val colorG = colorGraph(g)
    val numColors: Int = colorG.vertices.select("color").distinct.count().toInt

    // TODO: Handle vertices without any edges.

    // Initialize vertex beliefs at 0.0.
    var gx = GraphFrame(colorG.vertices.withColumn("belief", lit(0.0)), colorG.edges)

    // Run BP for numIter iterations.
    for (iter <- Range(0, numIter)) {
      // For each color, have that color receive messages from neighbors.
      for (color <- Range(0, numColors)) {
        // Define "AM" for shorthand for referring to the src, dst, edge, and msg fields.
        // (See usage below.)
        val AM = AggregateMessages
        // Send messages to vertices of the current color.
        // We may send to source or destination since edges are treated as undirected.
        val msgForSrc: Column = when(AM.src("color") === color, AM.edge("b") * AM.dst("belief"))
        val msgForDst: Column = when(AM.dst("color") === color, AM.edge("b") * AM.src("belief"))
        val logistic = udf { (x: Double) => math.exp(-log1pExp(-x)) }
        val aggregates = gx.aggregateMessages
          .sendToSrc(msgForSrc)
          .sendToDst(msgForDst)
          .agg(sum(AM.msg).as("aggMess"))
        val v = gx.vertices
        // Receive messages, and update beliefs for vertices of the current color.
        val newBeliefCol = when(v("color") === color && aggregates("aggMess").isNotNull,
          logistic(aggregates("aggMess") + v("a")))
          .otherwise(v("belief"))  // keep old beliefs for other colors
        val newVertices = v
          .join(aggregates, v("id") === aggregates("id"), "left_outer")  // join messages, vertices
          .drop(aggregates("id"))  // drop duplicate ID column (from outer join)
          .withColumn("newBelief", newBeliefCol)  // compute new beliefs
          .drop("aggMess")  // drop messages
          .drop("belief")  // drop old beliefs
          .withColumnRenamed("newBelief", "belief")
        // Cache new vertices using workaround for SPARK-13346
        val cachedNewVertices = AM.getCachedDataFrame(newVertices)
        gx = GraphFrame(cachedNewVertices, gx.edges)
      }
    }

    // Drop the "color" column from vertices
    GraphFrame(gx.vertices.drop("color"), gx.edges)
  }

  /** More numerically stable `log(1 + exp(x))` */
  private def log1pExp(x: Double): Double = {
    if (x > 0) {
      x + math.log1p(math.exp(-x))
    } else {
      math.log1p(math.exp(x))
    }
  }
}


// COMMAND ----------

