# Databricks notebook source exported at Thu, 5 May 2016 04:13:21 UTC
# MAGIC %md
# MAGIC Just a copy of [http://go.databricks.com/hubfs/notebooks/3-GraphFrames-User-Guide-python.html](http://go.databricks.com/hubfs/notebooks/3-GraphFrames-User-Guide-python.html).

# COMMAND ----------

# MAGIC %md #GraphFrames User Guide (Python)
# MAGIC 
# MAGIC GraphFrames is a package for Apache Spark which provides DataFrame-based Graphs. It provides high-level APIs in Scala, Java, and Python. It aims to provide both the functionality of GraphX and extended functionality taking advantage of Spark DataFrames. This extended functionality includes motif finding, DataFrame-based serialization, and highly expressive graph queries.
# MAGIC 
# MAGIC The GraphFrames package is available from [Spark Packages](http://spark-packages.org/package/graphframes/graphframes).
# MAGIC 
# MAGIC This notebook demonstrates examples from the [GraphFrames User Guide](http://graphframes.github.io/user-guide.html).

# COMMAND ----------

from graphframes import *

# COMMAND ----------

# MAGIC %md ## Creating GraphFrames
# MAGIC 
# MAGIC Users can create GraphFrames from vertex and edge DataFrames.
# MAGIC 
# MAGIC * Vertex DataFrame: A vertex DataFrame should contain a special column named ?id? which specifies unique IDs for each vertex in the graph.
# MAGIC * Edge DataFrame: An edge DataFrame should contain two special columns: ?src? (source vertex ID of edge) and ?dst? (destination vertex ID of edge).
# MAGIC 
# MAGIC Both DataFrames can have arbitrary other columns. Those columns can represent vertex and edge attributes.

# COMMAND ----------

# MAGIC %md Create the vertices first:

# COMMAND ----------

vertices = sqlContext.createDataFrame([
  ("a", "Alice", 34),
  ("b", "Bob", 36),
  ("c", "Charlie", 30),
  ("d", "David", 29),
  ("e", "Esther", 32),
  ("f", "Fanny", 36),
  ("g", "Gabby", 60)], ["id", "name", "age"])

# COMMAND ----------

# MAGIC %md And then some edges:

# COMMAND ----------

edges = sqlContext.createDataFrame([
  ("a", "b", "friend"),
  ("b", "c", "follow"),
  ("c", "b", "follow"),
  ("f", "c", "follow"),
  ("e", "f", "follow"),
  ("e", "d", "friend"),
  ("d", "a", "friend"),
  ("a", "e", "friend")
], ["src", "dst", "relationship"])

# COMMAND ----------

# MAGIC %md Let's create a graph from these vertices and these edges:

# COMMAND ----------

g = GraphFrame(vertices, edges)
print g

# COMMAND ----------

# This example graph also comes with the GraphFrames package.
# from graphframes.examples import Graphs
# g = Graphs(sqlContext).friends()

# COMMAND ----------

# MAGIC %md ## Basic graph and DataFrame queries
# MAGIC 
# MAGIC GraphFrames provide several simple graph queries, such as node degree.
# MAGIC 
# MAGIC Also, since GraphFrames represent graphs as pairs of vertex and edge DataFrames, it is easy to make powerful queries directly on the vertex and edge DataFrames. Those DataFrames are made available as vertices and edges fields in the GraphFrame.

# COMMAND ----------

display(g.vertices)

# COMMAND ----------

display(g.edges)

# COMMAND ----------

# MAGIC %md The incoming degree of the vertices:

# COMMAND ----------

display(g.inDegrees)

# COMMAND ----------

# MAGIC %md The outgoing degree of the vertices:

# COMMAND ----------

display(g.outDegrees)

# COMMAND ----------

# MAGIC %md The degree of the vertices:

# COMMAND ----------

display(g.degrees)

# COMMAND ----------

# MAGIC %md You can run queries directly on the vertices DataFrame. For example, we can find the age of the youngest person in the graph:

# COMMAND ----------

youngest = g.vertices.groupBy().min("age")
display(youngest)

# COMMAND ----------

# MAGIC %md Likewise, you can run queries on the edges DataFrame. For example, let us count the number of 'follow' relationships in the graph:

# COMMAND ----------

numFollows = g.edges.filter("relationship = 'follow'").count()
print "The number of follow edges is", numFollows

# COMMAND ----------

# MAGIC %md ## Motif finding
# MAGIC 
# MAGIC More complex relationships involving edges and vertices can be build using motifs. The following cell finds the pairs of vertices with edges in both directions between them. The result is a dataframe, in which the column names are given by the motif keys.
# MAGIC 
# MAGIC Check out the [GraphFrame User Guide](http://graphframes.github.io/user-guide.html#motif-finding) for more details on the API.

# COMMAND ----------

# Search for pairs of vertices with edges in both directions between them.
motifs = g.find("(a)-[e]->(b); (b)-[e2]->(a)")
display(motifs)

# COMMAND ----------

# MAGIC %md Since the result is a DataFrame, more complex queries can be built on top of the motif. Let us find all the reciprocal relationships in which one person is older than 30:

# COMMAND ----------

filtered = motifs.filter("b.age > 30 or a.age > 30")
display(filtered)

# COMMAND ----------

# MAGIC %md ## Subgraphs
# MAGIC 
# MAGIC Subgraphs are built by filtering a subset of edges and vertices. For example, the following subgraph only contains people who are friends and who are more than 30 years old.

# COMMAND ----------

paths = g.find("(a)-[e]->(b)")\
  .filter("e.relationship = 'follow'")\
  .filter("a.age < b.age")
# The `paths` variable contains the vertex information, which we can extract:
e2 = paths.select("e.src", "e.dst", "e.relationship")

# In Spark 1.5+, the user may simplify the previous call to:
# val e2 = paths.select("e.*")

# Construct the subgraph
g2 = GraphFrame(g.vertices, e2)

# COMMAND ----------

display(g2.vertices)

# COMMAND ----------

# MAGIC %md ## Standard graph algorithms
# MAGIC 
# MAGIC GraphFrames comes with a number of standard graph algorithms built in:
# MAGIC * Breadth-first search (BFS)
# MAGIC * Connected components
# MAGIC * Strongly connected components
# MAGIC * Label Propagation Algorithm (LPA)
# MAGIC * PageRank
# MAGIC * Shortest paths
# MAGIC * Triangle count

# COMMAND ----------

# MAGIC %md ###Breadth-first search (BFS)
# MAGIC 
# MAGIC Search from "Esther" for users of age < 32.

# COMMAND ----------

paths = g.bfs("name = 'Esther'", "age < 32")
display(paths)

# COMMAND ----------

# MAGIC %md The search may also be limited by edge filters and maximum path lengths.

# COMMAND ----------

filteredPaths = g.bfs(
  fromExpr = "name = 'Esther'",
  toExpr = "age < 32",
  edgeFilter = "relationship != 'friend'",
  maxPathLength = 3)
display(filteredPaths)

# COMMAND ----------

# MAGIC %md ## Connected components
# MAGIC 
# MAGIC Compute the connected component membership of each vertex and return a graph with each vertex assigned a component ID.

# COMMAND ----------

result = g.connectedComponents()
display(result)

# COMMAND ----------

# MAGIC %md ## Strongly connected components
# MAGIC 
# MAGIC Compute the strongly connected component (SCC) of each vertex and return a graph with each vertex assigned to the SCC containing that vertex.

# COMMAND ----------

result = g.stronglyConnectedComponents(maxIter=10)
display(result.select("id", "component"))

# COMMAND ----------

# MAGIC %md ## Label Propagation
# MAGIC 
# MAGIC Run static Label Propagation Algorithm for detecting communities in networks.
# MAGIC 
# MAGIC Each node in the network is initially assigned to its own community. At every superstep, nodes send their community affiliation to all neighbors and update their state to the mode community affiliation of incoming messages.
# MAGIC 
# MAGIC LPA is a standard community detection algorithm for graphs. It is very inexpensive computationally, although (1) convergence is not guaranteed and (2) one can end up with trivial solutions (all nodes are identified into a single community).

# COMMAND ----------

result = g.labelPropagation(maxIter=5)
display(result)

# COMMAND ----------

# MAGIC %md ## PageRank
# MAGIC 
# MAGIC Identify important vertices in a graph based on connections.

# COMMAND ----------

results = g.pageRank(resetProbability=0.15, tol=0.01)
display(results.vertices)

# COMMAND ----------

display(results.edges)

# COMMAND ----------

# Run PageRank for a fixed number of iterations.
g.pageRank(resetProbability=0.15, maxIter=10)

# COMMAND ----------

# Run PageRank personalized for vertex "a"
g.pageRank(resetProbability=0.15, maxIter=10, sourceId="a")

# COMMAND ----------

# MAGIC %md ## Shortest paths
# MAGIC 
# MAGIC Computes shortest paths to the given set of landmark vertices, where landmarks are specified by vertex ID.

# COMMAND ----------

results = g.shortestPaths(landmarks=["a", "d"])
display(results)

# COMMAND ----------

# MAGIC %md ###Triangle count
# MAGIC 
# MAGIC Computes the number of triangles passing through each vertex.

# COMMAND ----------

results = g.triangleCount()
display(results)