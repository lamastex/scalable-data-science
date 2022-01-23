<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Distributed combinatorial bandits
=================================

Group Project Authors:

-   [Niklas Åkerblom](https://www.chalmers.se/en/Staff/Pages/akerblon.aspx)
-   Jonas Nordlöf
-   [Emilio Jorge](https://www.jorge.se)

Link to video presentation: [Video](https://youtu.be/uipIfFlluEI)

</div>

<div class="cell markdown">

Idea
----

We try to solve a routing problem, that is trying to tell a vechicle what the sportest path is to a destination. The problem is that the dispatcher knows the connections in the graph but not the length of each edge. The dispatcher learns how long it takes to traverse a path when a vehicle travels it. This makes the routing problem an online learning problem such that the dispatcher has to learn which paths to tell the vehicle to take in a way that finds the best path, both in terms of speed and gaining information about future good paths (more on this later). Additionally the edges are stochastic, such that one traversal is no enough to get perfect information.

This setting can be seen as a case of a combinatorial bandit where we have to select a set of edges that reach the destination from our start while balancing the need of getting a fast route with obtaining better estimates of edges such that future paths can be more efficient (this is known as exploration-explotation tradeoff).

Distributing this task could be an interesting idea, both since multiple dispatchers and vehicles could work in parallell (which we do not consider here) but also that large graphs can be sped up through distributed computations in the shortest path problems that arise.

### Practicalities

To make our task more realistic we have used data from OpenStreetMap, a collection of real world map data to create a graph consisting of real world roads. We also generate some synthetic data to experiment with.

The graph network then goes into our contextual bandit algorithm which samples edge weights from a belief and then selects the shortest path from this sampled graph. This leads to an algorithm with very nice theoretical properties in terms of online learning. This method is well known in the online learning community, but as far as we know has not been done in a distributed fashion.

</div>

<div class="cell markdown">

Loading of OpenStreetMap data
-----------------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql._
import scala.sys.process._
import org.apache.spark.sql.functions.{col}

def toMap(tupesArray: Seq[Row]): Option[Map[String, String]] = {
    if (tupesArray == null) {
      None
    } else {
      val tuples = tupesArray.map(e => {
        (
          e.getAs[String]("key"),
          e.getAs[String]("value")
        )
      })
      Some(tuples.toMap)
    }
  }

def handleCommon()(df:DataFrame):DataFrame = {
  val toMapUDF = udf(toMap _)
  df.drop("uid", "user_sid", "changeset", "version", "timestamp")
    .withColumn("tags", toMapUDF(col("tags")))
}
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql._
    import scala.sys.process._
    import org.apache.spark.sql.functions.col
    toMap: (tupesArray: Seq[org.apache.spark.sql.Row])Option[Map[String,String]]
    handleCommon: ()(df: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
sqlContext.setConf("spark.sql.parquet.binaryAsString","true")
val nodeDF = sqlContext.read.parquet("dbfs:/FileStore/group14/sweden-latest_osm_pbf_node.parquet").transform(handleCommon())

val wayDF = sqlContext.read.parquet("dbfs:/FileStore/group14/sweden_latest_osm_pbf_way.parquet").transform(handleCommon())
```

<div class="output execute_result plain_result" execution_count="1">

    nodeDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [id: bigint, tags: map<string,string> ... 2 more fields]
    wayDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [id: bigint, tags: map<string,string> ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql._
import org.apache.spark.sql.functions.{explode,arrays_zip, concat,array, lit}

val wayDF_exploded = wayDF.withColumn("exploded", explode(arrays_zip(concat($"nodes.nodeId",array(lit(-1L))), concat(array(lit(-1L)),$"nodes.nodeId"))))
val wayDF_filtered = wayDF_exploded.filter($"exploded.0" > 0 && $"exploded.1" > 0)
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql._
    import org.apache.spark.sql.functions.{explode, arrays_zip, concat, array, lit}
    wayDF_exploded: org.apache.spark.sql.DataFrame = [id: bigint, tags: map<string,string> ... 2 more fields]
    wayDF_filtered: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [id: bigint, tags: map<string,string> ... 2 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._

val wayNodeDF = wayDF_exploded.select($"exploded.0".as("start"), $"exploded.1".as("end"),$"tags.highway", $"tags.maxspeed")
.filter($"highway" isin ("motorway","trunk","primary","secondary", "tertiary", "unclassified", "residential","motorway_link", "trunk_link",  "primary_link", "secondary_link", "tertiary_link"))

wayNodeDF.createOrReplaceTempView("wayHighway")
val wayNodeDF_nonull = wayNodeDF.withColumn("maxspeed", when($"maxspeed".isNull && col("highway") == "motorway", 110)
                     .when($"maxspeed".isNull && col("highway")=="primary", 50).when($"maxspeed".isNull && col("highway")=="secondary", 50).when($"maxspeed".isNull && col("highway")=="motorway_link", 50)
                     .when($"maxspeed".isNull && col("highway")=="residential", 15).when($"maxspeed".isNull, 50)
                     .otherwise($"maxspeed"))
wayNodeDF_nonull.createOrReplaceTempView("wayHighway")


```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions._
    wayNodeDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [start: bigint, end: bigint ... 2 more fields]
    wayNodeDF_nonull: org.apache.spark.sql.DataFrame = [start: bigint, end: bigint ... 2 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._

val nodeLatLonDF = nodeDF
  .select($"id".as("nodeId"), $"latitude".as("startLat"), $"longitude".as("startLong"))

val endnodeLatLonDF = nodeDF
  .select($"id".as("nodeId2"), $"latitude".as("endLat"), $"longitude".as("endLong"))

val wayGeometryDF = wayNodeDF_nonull.join(nodeLatLonDF, $"start" === $"nodeId").join(endnodeLatLonDF, $"end" === $"nodeId2")

val wayGeometry_distDF = wayGeometryDF.withColumn("a", pow(sin(radians($"endLat" - $"startLat") / 2), 2) + cos(radians($"startLat")) * cos(radians($"endLat")) * pow(sin(radians($"endLong" - $"startLong") / 2), 2))
  .withColumn("distance", atan2(org.apache.spark.sql.functions.sqrt($"a"), org.apache.spark.sql.functions.sqrt(-$"a" + 1)) * 2 * 6371)
  .filter($"endLat"<55.4326186d && $"endLong">13.7d) //Small area south of sweden.
  .withColumn("time", $"distance"/$"maxspeed").select("time", "start", "end", "distance", "maxspeed")
wayGeometry_distDF.createOrReplaceTempView("wayGeometry_distDF")
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions._
    nodeLatLonDF: org.apache.spark.sql.DataFrame = [nodeId: bigint, startLat: double ... 1 more field]
    endnodeLatLonDF: org.apache.spark.sql.DataFrame = [nodeId2: bigint, endLat: double ... 1 more field]
    wayGeometryDF: org.apache.spark.sql.DataFrame = [start: bigint, end: bigint ... 8 more fields]
    wayGeometry_distDF: org.apache.spark.sql.DataFrame = [time: double, start: bigint ... 3 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
val eps = 0.000001
val edges: RDD[Edge[Double]]  =  wayGeometry_distDF
    .select("start", "end", "time").rdd.map(line => Edge(line.getAs("start"), line.getAs("end"), line.getAs("time")))


val graph = Graph.fromEdges(edges, "defaultname")
graph.cache()

println("Num edges:")
println(graph.edges.toDF.count())
println("Num vertices:")
println(graph.vertices.toDF.count())
```

<div class="output execute_result plain_result" execution_count="1">

    Num edges:
    4667
    Num vertices:
    4488
    import org.apache.spark.graphx._
    import org.apache.spark.rdd.RDD
    eps: Double = 1.0E-6
    edges: org.apache.spark.rdd.RDD[org.apache.spark.graphx.Edge[Double]] = MapPartitionsRDD[1075908] at map at command-2294440354339724:6
    graph: org.apache.spark.graphx.Graph[String,Double] = org.apache.spark.graphx.impl.GraphImpl@7f4c143a

</div>

</div>

<div class="cell markdown">

The shortest path algorithm
---------------------------

The implemented shorthest path alborithm uses the the distributed Pregel algorithm and is divided into two parts.

The first part is based on the code in <a href="http://lamastex.org/lmse/mep/src/GraphXShortestWeightedPaths.html" class="uri">http://lamastex.org/lmse/mep/src/GraphXShortestWeightedPaths.html</a>. As the original code did not have have all functionality desiered functionality, the algorithm did find the shortest distance but didn't keep track of the path itself, the algorithm was extend with this functionality.

The first part takes a graph, where the edges are double values representing the cost of trevelling between its connected nodes, and an array of the ids of each goal node. As output, it provides a graph where each node containse a Map-object of the different landmarks/goal nodes. When a lookup is made in the map from a specific node, a tuple contaning the shortest distance, the id of the next node in the path and the id of the current node. The last element serves no pupose in the final results but is used as a form of stopping critera in the algorithm.

The second part transforms the output of the first part to a "path graph" where each edge is marked with either a 1 or a 0 depending on if it is used in a path between a starting node and a goal node. Altough this recursion can be performed on a single machine for small examples, this procedure is also implemented using the Pregel algorithm to handle situations of millions of edges.

The input of the second part is the graph created in the first part as well as the id of a single goal node and a start node. The goal node has to be in the set of goal nodes used in the first part. This part outputs a "path graph" where each edge is given the value 1 or 0 depending on if it is on the shortest path or not.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import scala.reflect.ClassTag
import org.apache.spark.graphx._

/**
 * Computes shortest weighted paths to the given set of goal nodes, returning a graph where each
 * vertex attribute is a map containing the shortest-path distance to each reachable landmark.
 * Currently supports only Graph of [VD, Double], where VD is an arbitrary vertex type.
 *
 * The object also include a function which transforms the resulting graph into a path_graph between a 
 * specific starting node and goal node. Each edge in the path_grpah is either 1 or 0 depending if it is 
 * the shortest path or not.
 *
 */
object ShortestPath extends Serializable {

  // When finding the shortest path each node stores a map from the itself to each goal node.
  // The map returns an array includeing the total distance to the goal node as well as the
  // next node pn the shortest path to the goal node. The last value in the array is only 
  // populated with the nodes own id and is only used for computational convenience. 
  type SPMap = Map[VertexId, Tuple3[Double, VertexId, VertexId]]
  
  // PN holds the information of the path nodes which are used for creating a path graph
  // PN = ('Distance left to goal node', 'Next path node id', 'Goal node', 'Is on path')
  type PN = Tuple4[Double, VertexId, VertexId, Boolean] 
  
  private val INITIAL_DIST = 0.0
  private val DEFAULT_ID = -1L
  private val INFINITY = Int.MaxValue.toDouble

  private def makeMap(x: (VertexId, Tuple3[Double, VertexId, VertexId])*) = Map(x: _*)
  
  //private def incrementMap(spmap: SPMap, delta: Double, id: VertexId): SPMap = { 
  //  spmap.map { case (v, d) => v -> (Tuple3(d._1 + delta, d._3, id)) }
  //}
  private def incrementMap(spmap: SPMap, delta: Double, srcId: VertexId, dstId: VertexId): SPMap = { 
    spmap.map { case (v, d) => v -> (Tuple3(d._1 + delta, dstId, srcId)) }
  }

  private def addMaps(spmap1: SPMap, spmap2: SPMap): SPMap = {
    (spmap1.keySet ++ spmap2.keySet).map {
    k =>{
        if (spmap1.getOrElse(k, Tuple3(INFINITY, DEFAULT_ID, DEFAULT_ID))._1 < spmap2.getOrElse(k, Tuple3(INFINITY, DEFAULT_ID, DEFAULT_ID))._1) 
                k -> (Tuple3(spmap1.getOrElse(k, Tuple3(INFINITY, DEFAULT_ID, DEFAULT_ID))._1, 
                             spmap1.getOrElse(k, Tuple3(INFINITY, DEFAULT_ID, DEFAULT_ID))._2, 
                             spmap1.getOrElse(k, Tuple3(INFINITY, DEFAULT_ID, DEFAULT_ID))._3))
        else 
                k -> (Tuple3(spmap2.getOrElse(k, Tuple3(INFINITY, DEFAULT_ID, DEFAULT_ID))._1, 
                             spmap2.getOrElse(k, Tuple3(INFINITY, DEFAULT_ID, DEFAULT_ID))._2, 
                             spmap2.getOrElse(k, Tuple3(INFINITY, DEFAULT_ID, DEFAULT_ID))._3))
        }
    }.toMap
  }
  
  // at this point it does not really matter what vertex type is
  def run[VD](graph: Graph[VD, Double], landmarks: Seq[VertexId]): Graph[SPMap, Double] = {
    val spGraph = graph.mapVertices { (vid, attr) =>
      // initial value for itself is 0.0 as Double
      if (landmarks.contains(vid)) makeMap(vid -> Tuple3(INITIAL_DIST, DEFAULT_ID, DEFAULT_ID)) else makeMap()
    }

    val initMaps = makeMap()

    def vProg(id: VertexId, attr: SPMap, msg: SPMap): SPMap = {
      addMaps(attr, msg)
    }

    def sendMsg(edge: EdgeTriplet[SPMap, Double]): Iterator[(VertexId, SPMap)] = {
      val newAttr = incrementMap(edge.dstAttr, edge.attr, edge.srcId, edge.dstId)
      if (edge.srcAttr != addMaps(newAttr, edge.srcAttr)) Iterator((edge.srcId, newAttr))
      else Iterator.empty
    }

    Pregel(spGraph, initMaps)(vProg, sendMsg, addMaps)
  }
  
  def create_path_graph[VD](graph: Graph[SPMap, Double], goalId: VertexId, startId: VertexId): Graph[PN, Int] = {
    // For a given goal node we remove the lookup map and extend the state to a Tuple5 with the goal id and a boolean
    val path = graph.mapEdges(e => 0)
              .mapVertices((vertixId, attr) => {
                if (attr.contains(goalId)) {
                  val path_step = attr(goalId)
                  if (vertixId == path_step._3 && path_step._2 == -1L)
                    (path_step._1, goalId, goalId, false) // while we are at it, we clean up the state a bit
                  else  
                    (path_step._1, path_step._2, goalId, false)
                } else// If the vertice does not have a map to our goal we add a default value to it
                    (INFINITY, -1L, -1L, false)
              })

      def mergeMsg(msg1: VertexId, msg2: VertexId): VertexId = { // we should only get one msg
          msg2
      }

      def vprog(id: VertexId, attr: PN, msg: VertexId): PN = {
        // Check that the current node is the one adressed in the message
        if (id == msg)
          (attr._1, attr._2, attr._3, true)
        else // If the message is not addressed to the current node (happens for inital message), use the old value 
          attr
      }
      def sendMsg(triplet: EdgeTriplet[PN, Int]): Iterator[(VertexId, VertexId)] = {
        // If dstId is the next node on the path and has not yet been activated
        if (triplet.srcAttr._2 == triplet.dstId && triplet.srcAttr._4 && !triplet.dstAttr._4) 
          Iterator((triplet.dstId, triplet.dstId))// Send next msg
        else
          Iterator.empty// Do nothing
      }

      Pregel(path, startId)(vprog, sendMsg, mergeMsg).mapTriplets(triplet => {
        if(triplet.srcAttr._2 == triplet.dstId && triplet.srcAttr._4)
          1
        else
          0
      })
  }
}
```

<div class="output execute_result plain_result" execution_count="1">

    import scala.reflect.ClassTag
    import org.apache.spark.graphx._
    defined object ShortestPath

</div>

</div>

<div class="cell markdown">

To make the code somewhat more accessible, we wrap the execution of the two parts above in a new function called `shortestPath`. This new function takes the id of the start node and a single goal node as well as the input graph as input. The function then ouputs the path graph mentioned above.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import scala.util.Random

def shortestPath(srcId : Long, dstId : Long, graph : Graph[Long, Double], placeholder: Boolean) : Graph[Long, Double] = {
  if (placeholder) {
    return graph.mapEdges(e => Random.nextInt(2))
  } else {
    val distanceGraph = ShortestPath.run(graph, Seq(dstId))
    val pathGraph = ShortestPath.create_path_graph(distanceGraph, dstId, srcId)
    return pathGraph.mapVertices((vid, attr) => 0L).mapEdges(e => e.attr)
  }
}

def shortestPath(srcId : Long, dstId : Long, graph : Graph[Long, Double]) : Graph[Long, Double] = {
  return shortestPath(srcId, dstId, graph, false)
}
```

<div class="output execute_result plain_result" execution_count="1">

    import scala.util.Random
    shortestPath: (srcId: Long, dstId: Long, graph: org.apache.spark.graphx.Graph[Long,Double], placeholder: Boolean)org.apache.spark.graphx.Graph[Long,Double] <and> (srcId: Long, dstId: Long, graph: org.apache.spark.graphx.Graph[Long,Double])org.apache.spark.graphx.Graph[Long,Double]
    shortestPath: (srcId: Long, dstId: Long, graph: org.apache.spark.graphx.Graph[Long,Double], placeholder: Boolean)org.apache.spark.graphx.Graph[Long,Double] <and> (srcId: Long, dstId: Long, graph: org.apache.spark.graphx.Graph[Long,Double])org.apache.spark.graphx.Graph[Long,Double]

</div>

</div>

<div class="cell markdown">

Since we want to work with edge attributes rather than vertex attributes, we can't work directly with graph joins in GraphX, since they only join on vertices. This is a helper method to merge edge attributes of two graphs with identical structure, through an inner join on the respective edge RDDs and create a new graph with a tuple combining edge attributes from both graphs. This will only work if both graphs have identical partitioning strategies.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import scala.reflect.ClassTag

// # Merge edge attributes of two (identical in structure) graphs
def mergeEdgeAttributes[ED1 : ClassTag, ED2 : ClassTag](firstGraph : Graph[Long, ED1], secondGraph : Graph[Long, ED2]) : Graph[Long, (ED1, ED2)] = {
  return Graph(firstGraph.vertices, firstGraph.edges.innerJoin(secondGraph.edges) {(id1, id2, first, second) => (first, second)}) 
}
```

<div class="output execute_result plain_result" execution_count="1">

    import scala.reflect.ClassTag
    mergeEdgeAttributes: [ED1, ED2](firstGraph: org.apache.spark.graphx.Graph[Long,ED1], secondGraph: org.apache.spark.graphx.Graph[Long,ED2])(implicit evidence$1: scala.reflect.ClassTag[ED1], implicit evidence$2: scala.reflect.ClassTag[ED2])org.apache.spark.graphx.Graph[Long,(ED1, ED2)]

</div>

</div>

<div class="cell markdown">

In order to perform distributed sampling from Gaussian distributions in Spark in a reproducible way (specifically for stochastic edge weights in graphs), we want to be able to pass a seed to the random number generator. For this to work consistently, we use the Spark SQL `randn` function on the edge RDDs and subsequently build a new graph from the sampled weights.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import scala.util.Random

def graphRandomGaussian(graph : Graph[Long, (Double, Double)], seed : Int, eps : Double, sparkSqlRandom : Boolean) : Graph[Long, Double] = {
  if (sparkSqlRandom) {
    return Graph(graph.vertices, graph.edges.toDF.select($"srcId", $"dstId", $"attr._1" + org.apache.spark.sql.functions.sqrt($"attr._2") * org.apache.spark.sql.functions.randn(seed)).rdd.map(r => Edge(r.getLong(0), r.getLong(1), r.getDouble(2)))).mapEdges(e => scala.math.max(eps, e.attr))
  } else {
    return graph.mapEdges(e => scala.math.max(eps, e.attr._1 + Random.nextGaussian() * scala.math.sqrt(e.attr._2)))
  }
} 

def graphRandomGaussian(graph : Graph[Long, (Double, Double)], seed : Int, eps : Double) : Graph[Long, Double] = {
  return graphRandomGaussian(graph, seed, eps, true)
}
```

<div class="output execute_result plain_result" execution_count="1">

    import scala.util.Random
    graphRandomGaussian: (graph: org.apache.spark.graphx.Graph[Long,(Double, Double)], seed: Int, eps: Double, sparkSqlRandom: Boolean)org.apache.spark.graphx.Graph[Long,Double] <and> (graph: org.apache.spark.graphx.Graph[Long,(Double, Double)], seed: Int, eps: Double)org.apache.spark.graphx.Graph[Long,Double]
    graphRandomGaussian: (graph: org.apache.spark.graphx.Graph[Long,(Double, Double)], seed: Int, eps: Double, sparkSqlRandom: Boolean)org.apache.spark.graphx.Graph[Long,Double] <and> (graph: org.apache.spark.graphx.Graph[Long,(Double, Double)], seed: Int, eps: Double)org.apache.spark.graphx.Graph[Long,Double]

</div>

</div>

<div class="cell markdown">

RDDs in Spark contain lineage graphs with information about previous operations, for e.g. fault tolerance. These can increase significantly in size after many transformations which may result in reduced performance, especially in iterative algorithms (such as in GraphX). For this reason, we truncate the lineage graph by checkpointing the RDDs.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
def locallyCheckpointedGraph[VD : ClassTag, ED : ClassTag](graph : Graph[VD, ED]) : Graph[VD, ED] = {
  val mappedGraph = graph.mapEdges(e => e.attr)
  val edgeRdd = mappedGraph.edges.map(x => x)
  val vertexRdd = mappedGraph.vertices.map(x => x)
  edgeRdd.cache()
  edgeRdd.localCheckpoint()
  edgeRdd.count() // We need this line to force the RDD to evaluate, otherwise the truncation is not performed
  vertexRdd.cache()
  vertexRdd.localCheckpoint()
  vertexRdd.count() // We need this line to force the RDD to evaluate, otherwise the truncation is not performed
  return Graph(vertexRdd, edgeRdd)
}
```

</div>

<div class="cell markdown">

Distributed combinatorial bandit algorithm
------------------------------------------

The cell below contains the distributed combinatorial bandit algorithm, as well as the simulation framework. In a standard (stochastic) multi-armed bandit problem setting, there is a set of actions \\(\mathcal{A}\\) wherein each selected action \\(a \in \mathcal{A}\\) results in the agent receiving a random reward \\(r(a)\\) from the environment. The distributions of these rewards are unknown and it is the objective of an agent to select actions to learn enough information about the reward distributions such that the long-term rewards can be maximized.

Thompson Sampling is Bayesian bandit algorithm, where the assumption is that the parameters of the reward distributions are drawn from some known prior. By using observed rewards to compute a posterior distribution, the posterior can be used to let the agent explore actions which have a high probability of being optimal. In each iteration, parameters are sampled from the posterior for all actions. The action with the highest (sampled) expected reward is then selected. Thompson Sampling is straightforward to extend to a combinatorial setting, where instead of individual actions, subsets of actions subject to combinatorial constraints are selected in each iteration.

Under the assumption that the travel times individual edges in the road network graph are mutually independent, an online learning version of the shortest path problem can be cast into the combinatorial bandit setting. With the same assumption, the above operations can be performed using Spark and GraphX, in an iterative algorithm. With the exception of the step in which the distributed shortest path algorithm (through pregel) is used to find the path with the lowest (sampled from the posterior distribution) expected travel time, the rest of the steps can be performed almost exclusively by using `mapEdges` transformations. In this way, expensive repartitioning can be avoided.

As we see it, the main benefit with this approach is that if the road network graph is very large, sub-graphs can be located on different worker nodes.

NOTE: We run this with a toy example instead of the actual road network graph, since we had performance issues keeping us from evaluating it in a meaningful way.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import scala.math.sqrt
import scala.math.max

println("Starting experiment!")
val startTime = System.currentTimeMillis()

val seed = 1000
val eps = 0.00001

// # Horizon N
var N = 10

// # Source and destination node IDs
//val srcId = 0L
var srcId = 2010606671L
//val dstId = numVertices - 1L
var dstId = 2869293096L

var baseGraph: Graph[Long, Double] = null

// # Toy example
val useToyExample = true
if (useToyExample) {
  val numVertices = 10
  N = 100
  srcId = 0L
  dstId = numVertices - 1
  baseGraph = Graph.fromEdges(spark.sparkContext.parallelize(1 until numVertices-1).flatMap(vid => List(Edge(0L, vid.toLong, 100.0), Edge(vid.toLong, numVertices-1, 100.0))), 0L)
} else {
  baseGraph = graph.mapVertices((vid, attr) => 0L)
}

// # Assumption: Gaussian rewards with known variance
// # Prior graph (map weight to prior mean and variance)
val varFactor = 0.01
val prior = baseGraph.mapVertices((vid, attr) => 0L).mapEdges(e => (e.attr, varFactor * (e.attr * e.attr)))
var posterior = prior

// # Environment (sample true environment from prior)
val env = mergeEdgeAttributes(prior, graphRandomGaussian(prior, seed, eps)).mapEdges(e => (e.attr._2, e.attr._1._2))

// # For regret calculations
val optimal = shortestPath(srcId, dstId, env.mapEdges(e => e.attr._1))
val optimalExpectedCost = mergeEdgeAttributes(optimal, env).edges.map(e => e.attr._1 * e.attr._2._1).reduce(_ + _)
val optimalPathEdges = optimal.edges.filter(e => e.attr == 1).map(e => (e.srcId, e.dstId)).collect()
printf("Optimal path edges: [%s]\n", optimalPathEdges.mkString(","))

// # Array with instant regret values
var lastAction = optimal
var allActions = env.mapEdges(e => Array[Double](0))

// # Run experiment for N iterations
for (t <- 0 until N) {
  printf("Iteration %d, elapsed time: %d ms", t, System.currentTimeMillis() - startTime)
  
  // # Checkpoint to break lineage graph
  allActions = locallyCheckpointedGraph(allActions)
  posterior = locallyCheckpointedGraph(posterior)
  
  // # Find action (super arm) using posterior sampling
  val sampledParameters = graphRandomGaussian(posterior, seed+t*2+1, eps)
  val action = shortestPath(srcId, dstId, sampledParameters)
  lastAction = action
  
  // # Apply action on environments (assuming path is indicated by 1-valued edge attributes) and observe realized costs
  val realizedEnv = graphRandomGaussian(env, seed+t*2+2, eps)
  val observation = mergeEdgeAttributes(action, realizedEnv).mapEdges(e => e.attr._1 * e.attr._2)
  
  // # Update posterior
  posterior = mergeEdgeAttributes(env, mergeEdgeAttributes(action, mergeEdgeAttributes(observation, posterior))).mapEdges(e => {
    val trueVar = e.attr._1._2
    val act = e.attr._2._1
    val obs = e.attr._2._2._1
    val pMean = e.attr._2._2._2._1
    val pVar = e.attr._2._2._2._2
    if (act == 1) {
      val newVar = (1/(1/trueVar + 1/pVar))
      (newVar*(obs/trueVar + pMean/pVar), newVar)
    } else {
      (pMean, pVar)
    }  
  })
  
  // # Calculate regret
  allActions = mergeEdgeAttributes(allActions, action).mapEdges(e => e.attr._1 :+ e.attr._2)
  printf("\n")
}

printf("Starting aggregation of regret values, elapsed time: %d ms\n", System.currentTimeMillis() - startTime)

// # Aggregation of regret values
val countActions = allActions.mapEdges(e => e.attr.reduce(_ + _))
allActions = allActions.cache()
val instantRegretValues = new Array[Double](N)
for (t <- 0 until N) {
  val action = allActions.mapEdges(e => e.attr(t+1))
  val actionExpectedCost = mergeEdgeAttributes(action, env).edges.map(e => e.attr._1 * e.attr._2._1).reduce(_ + _)
  val instantRegret = actionExpectedCost - optimalExpectedCost
  instantRegretValues(t) = instantRegret
}

val endTime = System.currentTimeMillis()
printf("Finished experiment! Elapsed time:%d\n", endTime - startTime)
```

</div>

<div class="cell markdown">

Graph visualization
-------------------

In order to analys the data we visualize a graph which show the number times it has been visited by the exploration algorithm. When visualised, the edges with multiple visits are marked with a thicker line. Note that the graph will get very cluttered if more than 50 nodes are in the graph.

But first, we need to initialize the d3 package.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    Warning: classes defined within packages cannot be redefined without a cluster restart.
    Compilation successful.

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.graphframes.GraphFrame
import org.apache.spark.sql.functions.lit // import the lit function in sql

val visitedEdges = GraphFrame.fromGraphX(countActions.mapEdges(e => e.attr.toInt))
val visits = visitedEdges.edges.select($"attr".as("count"))
val maxVisits = visits.agg(org.apache.spark.sql.functions.max(visits("count")))
d3.graphs.force(
  height = 500,
  width = 1000,
  clicks = visitedEdges.edges.select($"src", $"dst".as("dest"), $"attr".divide(maxVisits.first().getInt(0)).multiply(500).cast("int").as("count")).as[d3.Edge])
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/14_1.JPG?raw=true)

</div>

<div class="cell markdown">

We can also visualize the shortest path in the graph.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val posteriorShortestPath = GraphFrame.fromGraphX(lastAction.mapEdges(e => e.attr.toInt*10000))

d3.graphs.force(
  height = 500,
  width = 1000,
  clicks = posteriorShortestPath.edges.select($"src", $"dst".as("dest"), $"attr".as("count")).as[d3.Edge])
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/14_2.JPG?raw=true)

</div>

<div class="cell markdown">

Now we visualize the instant regret. We should see a lot ot spikes in the beginning of the graph but the general trend should be that the curve decrease to zero while having fewer spikes as the algorithm gets closer to the optimal solutions. Note that there will always be some spikes as these corresponds to "exploratory" actions.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df = spark.sparkContext.parallelize((1 to N) zip instantRegretValues).toDF("Iteration (t)","Instant regret")
display(df)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/14_3.JPG?raw=true)

</div>

<div class="cell markdown">

We can also show the cumulative regret. As the algorithm reaches a final solution, the instantaneous regret should decrease and the cumulative regret should reach a plateau.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val cumulativeRegret = instantRegretValues.scanLeft(0.0)(_ + _)
val df = spark.sparkContext.parallelize((1 to N) zip cumulativeRegret).toDF("Iteration (t)","Cumulative regret")
display(df)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/14_4.JPG?raw=true)

</div>

<div class="cell markdown">

References
----------

Sadikov, I., & Sainudiin, R.. (2016). Distributed Weighted Shortest Paths. <a href="http://lamastex.org/lmse/mep/src/GraphXShortestWeightedPaths.html" class="uri">http://lamastex.org/lmse/mep/src/GraphXShortestWeightedPaths.html</a>.

Thompson, W. (1933). On the likelihood that one unknown probability exceeds another in view of the evidence of two samples. *Biometrika*, 25(3–4), 285-–294.

Wang, S., & Chen, W. (2018). Thompson sampling for combinatorial semi-bandits. In *International Conference on Machine Learning* (pp. 5114–5122).

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
