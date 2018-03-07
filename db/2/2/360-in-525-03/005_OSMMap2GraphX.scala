// Databricks notebook source
// MAGIC %md
// MAGIC # Needs to be fixed for Spark 2.2 and latest magellan
// MAGIC 
// MAGIC Dillon George, Raazesh Sainudiin, Marina Toger (...)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/01_DillonGeorge/039_OSMMap2GraphX.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/studentProjects/01_DillonGeorge/039_OSMMap2GraphX](http://img.youtube.com/vi/0wKxVfeBQBc/0.jpg)](https://www.youtube.com/watch?v=0wKxVfeBQBc?rel=0&autoplay=1&modestbranding=1&start=8275)

// COMMAND ----------

// MAGIC %md 
// MAGIC #Creating a graph from OpenStreetMap (OSM) data with GraphX

// COMMAND ----------

// MAGIC %md 
// MAGIC ###A brief overview on OpenStreetMap and its data model.

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
displayHTML(frameIt("https://wiki.openstreetmap.org/wiki/About_OpenStreetMap",700))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Ways and Nodes
// MAGIC 
// MAGIC Ways and nodes are core elements of how OSM defines its data. They also map nicely to the notion of Vertices and Edges in GraphX and will be the OSM entity types of concern in this example.

// COMMAND ----------

displayHTML(frameIt("https://wiki.openstreetmap.org/wiki/Node",500))

// COMMAND ----------

displayHTML(frameIt("https://wiki.openstreetmap.org/wiki/Way",500))

// COMMAND ----------

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.collection.JavaConversions._

import org.apache.spark.graphx._

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Part I: Creating the Graph

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Part I(a): Ingest Data

// COMMAND ----------

// MAGIC %md 
// MAGIC To begin with download and copy an metro-extract of Beijing into dbfs.

// COMMAND ----------

// MAGIC %sh wget https://s3.amazonaws.com/metro-extracts.mapzen.com/beijing_china.osm.pbf

// COMMAND ----------

// MAGIC %fs mkdirs /datasets/osm/beijing/

// COMMAND ----------

dbutils.fs.mv("file:/databricks/driver/beijing_china.osm.pbf", "dbfs:/datasets/beijing/beijing.pbf")

// COMMAND ----------

// MAGIC %fs ls /datasets/beijing

// COMMAND ----------

// MAGIC %md 
// MAGIC OSM data comes in various formats such as XML and PBF, in this example data is read from PBF files. The method used to read this PBF files is based off of an outstanding pull request on the Magellan Github page, see here for more information see the [relevant page.](https://github.com/harsha2010/magellan/pull/44). 
// MAGIC 
// MAGIC __Note__: As this may eventually be merged into Magellan, and so parts of this worksheet might better be implemented using Magellan instead.

// COMMAND ----------

import crosby.binary.osmosis.OsmosisReader

import org.apache.hadoop.mapreduce.{TaskAttemptContext, JobContext}
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer
import org.openstreetmap.osmosis.core.domain.v0_6._
import org.openstreetmap.osmosis.core.task.v0_6.Sink

import sqlContext.implicits._

// COMMAND ----------

// MAGIC %md 
// MAGIC The following set represents those tags which are of interest. They are meant to encompass those tags which are associated with driveable ways. If a way contains a tag from this set it is most likely driveable.

// COMMAND ----------

val allowableWays = Set(
  "motorway",
  "motorway_link",
  "trunk",
  "trunk_link",
  "primary",
  "primary_link",
  "secondary",
  "secondary_link",
  "tertiary",
  "tertiary_link",
  "living_street",
  "residential",
  "road",
  "construction",
  "motorway_junction"
)

// COMMAND ----------

// MAGIC %md 
// MAGIC Create an Input stream for our pbf file, this can then be read by the OsmosisReader.

// COMMAND ----------

val fs = FileSystem.get(new Configuration())
val path = new Path("dbfs:/datasets/beijing/beijing.pbf")
val file = fs.open(path)

// COMMAND ----------

// MAGIC %md 
// MAGIC These containers will store the encountered nodes, ways and relations of interest and are appended to as the OsmosisReader processes the contents of the pbf file.

// COMMAND ----------

var nodes: ArrayBuffer[Node] = ArrayBuffer()
var ways: ArrayBuffer[Way] = ArrayBuffer()
var relations: ArrayBuffer[Relation] = ArrayBuffer()

// COMMAND ----------

// MAGIC %md 
// MAGIC We now create the osmosis reader object. This will process the pbf and call the process function on each OSM entity encountered.
// MAGIC 
// MAGIC Our process identifies the type of entity, and adds it to the relevant buffer. In the case of the ways only the ways containing a tag in the `allowableWays` set are added.

// COMMAND ----------

val osmosisReader = new OsmosisReader(file)

osmosisReader.setSink(
new Sink {
  override def process(entityContainer: EntityContainer): Unit = {

    if (entityContainer.getEntity.getType != EntityType.Bound) {
      val entity = entityContainer.getEntity
      entity match {
      case node: Node => nodes += node
      case relation: Relation => relations += relation
      case way: Way => {
        val tagSet = way.getTags.map(_.getValue).toSet
        // way has at least one tag of interest
        if ( !(tagSet & allowableWays).isEmpty ) {
          ways += way
        }
      }}
    }
  }

  override def initialize(map: java.util.Map[String, AnyRef]): Unit = {
    // Ensure our entity buffers are empty before processing
    nodes = ArrayBuffer()
    ways = ArrayBuffer()
    relations = ArrayBuffer()
  }

  override def complete(): Unit = {}

  override def release(): Unit = {}
})

// COMMAND ----------

// MAGIC %md 
// MAGIC The follwing cell runs the reader, and it is only after this call that any work will be done. This will take a second or two to complete.
// MAGIC 
// MAGIC __Note:__ When running the the reader multiple times start from the cell where the `file` is defined as it is consumed by running the reader.

// COMMAND ----------

osmosisReader.run()

// COMMAND ----------

// MAGIC %md 
// MAGIC Now an example of how a way is represented:

// COMMAND ----------

ways.head

// COMMAND ----------

// MAGIC %md 
// MAGIC To see the full list of tags call the `getTags` method on the way:

// COMMAND ----------

ways.head.getTags.toList

// COMMAND ----------

// MAGIC %md 
// MAGIC And doing the same for the nodes:

// COMMAND ----------

nodes.head

// COMMAND ----------

nodes.head.getTags.toList

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Part I(b): Turn ingested data into datasets

// COMMAND ----------

// MAGIC %md 
// MAGIC We are now at the stage where we have arrays of Nodes and Ways (and also relations but these will not be used further in this example). 

// COMMAND ----------

// MAGIC %md 
// MAGIC To start constructing the graph case classes for ways and nodes are defined. These take the fields from the Osmosis `Node` and `Way` objects so that these objects can be turned into Datasets.

// COMMAND ----------

case class WayEntry(wayId: Long, tags: Array[String], nodes: Array[Long])
case class NodeEntry(nodeId: Long, latitude: Double, longitude: Double, tags: Array[String])

// COMMAND ----------

// MAGIC %md 
// MAGIC Converting the nodes array into a Dataset of of `Node` case classes.

// COMMAND ----------

val nodeDS = nodes.map{node => 
  NodeEntry(node.getId, 
       node.getLatitude, 
       node.getLongitude, 
       node.getTags.map(_.getValue).toArray
)}.toDS.cache

// COMMAND ----------

// MAGIC %md 
// MAGIC What the dataset looks like:

// COMMAND ----------

display(nodeDS)

// COMMAND ----------

// MAGIC %md 
// MAGIC Note how many of the Nodes have no tags. These are nodes that are part of some way, any tags relevant to that node will be stored in the relevant way. The [OSM wiki.](http://wiki.openstreetmap.org/wiki/Node) covers this in more detail. 

// COMMAND ----------

// MAGIC %md 
// MAGIC The next step is to convert the way array to a dataset.

// COMMAND ----------

val wayDS = ways.map(way => 
  WayEntry(way.getId,
      way.getTags.map(_.getValue).toArray,
      way.getWayNodes.map(_.getNodeId).toArray)
).toDS.cache

// COMMAND ----------

// MAGIC %md 
// MAGIC We can see that the Nodes contained in a way are stored as a list of node ids.

// COMMAND ----------

display(wayDS)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Part II: Coarsen Graph

// COMMAND ----------

// MAGIC %md 
// MAGIC An important consideration when constructing a road network graph is the granularity of the stored map. Is it important for the graph to contain all nodes and ways or can coarser representation of the network be used?
// MAGIC 
// MAGIC One way to represent a coarse road graph is to have the vertices of the graph represent interesctions along the roads, and have the edges represent the way segment that connects two intersections. 

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Part II(a): Find Intersections

// COMMAND ----------

// MAGIC %md 
// MAGIC The first step in constructing such an intersection graph is to identify all the osm-nodes that are intersections. We can consider a node to be an intersection if it occurs in more than one way. Then the node of id of intersections will occur in multiple node list in our `wayDS`.

// COMMAND ----------

// MAGIC %md 
// MAGIC The next step does this counting. It first extracts the nodes column from `wayDS` and flatmapping those arrays so that we have a Dataset of all nodes in the ways. Then grouping by the node ids and counting gives the frequency of each node. 

// COMMAND ----------

wayDS

// COMMAND ----------

val nodeCounts = wayDS
  .flatMap(_.nodes)
//   .groupBy(node => identity(node))
//   .count

// COMMAND ----------

nodeCounts.collect

// COMMAND ----------

display(nodeCounts)

// COMMAND ----------

// MAGIC %md 
// MAGIC Now intersection nodes are found by filtering out all ids with a count less than two. The last map step simply removes the count field from the Dataset.

// COMMAND ----------

val intersectionNodes = nodeCounts.filter(_._2 >= 2).map(_._1)

// COMMAND ----------

display(intersectionNodes)

// COMMAND ----------

// MAGIC %md 
// MAGIC In order to share this data amongst all partitions create a broadcast variable containing the set of intersection node IDs. 
// MAGIC 
// MAGIC For a larger map broadcasting this array may not be feasible but in this case the number of intersection nodes is small enough to not cause any problems of note.

// COMMAND ----------

val intersectionSet = sc.broadcast(intersectionNodes.collect.toSet)

// COMMAND ----------

intersectionSet.value.take(10).foreach(println)

// COMMAND ----------

// MAGIC %md 
// MAGIC Just a reminder what `nodeDS` looks like.

// COMMAND ----------

display(nodeDS)

// COMMAND ----------

// MAGIC %md 
// MAGIC The next cell creates a Dataset of all the unique node IDs in our ways. 

// COMMAND ----------

val wayNodeIds = wayDS.flatMap(_.nodes).distinct

// COMMAND ----------

display(wayNodeIds)

// COMMAND ----------

// MAGIC %md 
// MAGIC Now join `nodeDS` with `wayNodeIds`, only retaining those nodes that occur in both. This gives a new Dataset containing only those nodes that are in a way, this includes all the information not just the ids as in `wayNodeIds`.

// COMMAND ----------

val wayNodes = nodeDS.as("nodes")
  .joinWith(wayNodeIds.as("ways"), $"ways.value" === $"nodes.nodeId")
  .map(_._1).cache

// COMMAND ----------

display(wayNodes)

// COMMAND ----------

// MAGIC %md 
// MAGIC Now identify and label any possible intersection vertices. Map `wayDS`, and for each node in each way tag each node as `true` if it is in the `intersectionSet` or `false` otherwise.
// MAGIC Then as an additional step the nodes at the start and end of the way list are set to be intersections to ensure that these perimeter ways are retained in the coarsening step.

// COMMAND ----------

case class LabeledWay(wayId: Long, labeledNodes: Array[(Long, Boolean)])

val labeledWays = wayDS.map{ way => {
  val nodesWithLabels = way.nodes.map(id => (id, intersectionSet.value contains id))
  
  nodesWithLabels(nodesWithLabels.size - 1) = (nodesWithLabels.last._1, true) // Add fake intersections and the end of a way
  nodesWithLabels(0) = (nodesWithLabels.head._1, true)
  LabeledWay(way.wayId, nodesWithLabels)
}}.cache

// COMMAND ----------

display(labeledWays)

// COMMAND ----------

display(labeledWays.map{way =>
  (way.wayId, way.labeledNodes.filter{_._2}.length)
})

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Part II(b): Segment Ways into Coarse Edges

// COMMAND ----------

// MAGIC %md 
// MAGIC After identifying all intersection nodes that are to be in our graph the next step is to segment our ways so that each edge edge in the intersection graph will contain the list of nodes along that way that lie between two intersections.

// COMMAND ----------

// MAGIC %md 
// MAGIC The Intersecton class contains the node ID of an intersection, as well as buffers storing ingoing and outgoing nodes. 

// COMMAND ----------

case class Intersection(OSMId: Long , inBuf: ArrayBuffer[Long], outBuf: ArrayBuffer[Long])

// COMMAND ----------

// MAGIC %md 
// MAGIC The following function is what actually does the work of segmenting the ways. As input it takes an array of 2-tuples, the first entry in the tuple is an node ID an the second is a Boolean which is `true` if that node is an intersection. As output it returns a tuple of the form `(OSMID, inBuf, outBuf)`.
// MAGIC 
// MAGIC The segmenation works by maintaining a maintaining a buffer of non-intersection nodes (`currentBuffer`). While iterating through all the nodes if an intersection nodes is encountered a new intersection tuple is appended to the intersection buffer, the current contents of the `currentBuffer` is copied to the `inBuf` of that intersection tuple and then cleared; otherwise any non-intersection nodes are appened to the `currentBuffer`.

// COMMAND ----------

def segmentWay(way: Array[(Long, Boolean)]): Array[(Long, Array[Long], Array[Long])] = {
  
  val indexedNodes: Array[((Long, Boolean), Int)] = way.zipWithIndex
  
  val intersections = ArrayBuffer[Intersection]()
  
  val currentBuffer = ArrayBuffer[Long]()
  
  // Only one node in the way
  if (way.length == 1) {
    val intersect = new Intersection(way(0)._1, ArrayBuffer(-1L), ArrayBuffer(-1L))
    return Array((intersect.OSMId, intersect.inBuf.toArray, intersect.outBuf.toArray))
  }
  
  indexedNodes.foreach{ case ((id, isIntersection), i) =>
    if (isIntersection) {
      val newEntry = new Intersection(id, currentBuffer.clone, ArrayBuffer[Long]())
      intersections += newEntry
      currentBuffer.clear
    }
    else {
        currentBuffer += id
    }
    
    // Reaches the end of the way while the outBuffer is not empty
    // Append the currentBuffer to the last intersection
    if (i == way.length - 1 && !currentBuffer.isEmpty) {
      if (intersections.isEmpty) intersections += new Intersection(-1L, ArrayBuffer[Long](), currentBuffer)
      else intersections.last.outBuf ++= currentBuffer
      currentBuffer.clear
    }
  }
  intersections.map(i => (i.OSMId, i.inBuf.toArray, i.outBuf.toArray)).toArray
}

// COMMAND ----------

// MAGIC %md 
// MAGIC Now applying the segmenting function for each array of nodes in the `labeledWays` Dataset.

// COMMAND ----------

val segmentedWays = labeledWays.map(way => (way.wayId, segmentWay(way.labeledNodes)))

// COMMAND ----------

// MAGIC %md 
// MAGIC Because the output from the display function is messy due the nested arrays and tuples it may helpful to examine the schema of the new Dataset.

// COMMAND ----------

segmentedWays.printSchema

// COMMAND ----------

display(segmentedWays)

// COMMAND ----------

// MAGIC %md 
// MAGIC In this step the nested structure of the `segmentedWays` is unwrapped and each segment is not an entry in a new Dataset, of the form `(wayId, IntersectionNode)`

// COMMAND ----------

case class IntersectionNode(id: Long, in: Array[Long], out: Array[Long])

val waySegmentDS = segmentedWays
  .flatMap(way => way._2.map(node => (way._1, node)))
  .map(node => (node._1, IntersectionNode(node._2._1, node._2._2, node._2._3))) // for each (wayId, (inBuf, outBuf)) => (wayId, IntersectionNode(nodeId, inBuf, outBuf))

// COMMAND ----------

display(coarseNodes)

// COMMAND ----------

import scala.collection.immutable.Map

// COMMAND ----------

// MAGIC %md 
// MAGIC Now having a sequence of way segments we extract the individual intersections from these segments and make tuples in the form of `(intersectionId, Map(WayId, (inBuffer, outBuffer)))`. These way ID maps store all the full information of the way segments. Reducing by key then merges all the way ID maps, so that for each intersection node there is one `Map` containing information about the way segments connected to that intersection.

// COMMAND ----------

val intersectionVertices = waySegmentDS
  .map(way => 
    (way._2.id, Map(way._1 -> (way._2.in, way._2.out))))
  .rdd
  .reduceByKey(_ ++ _)

// COMMAND ----------

// MAGIC %md 
// MAGIC Notice now that because of the reduceBy key each intersection vertex occurs only once in `intersectionVertices`, and that it is a `RDD[(VertexId, wayMap)]` which allows it to become a graphX vertexRDD

// COMMAND ----------

intersectionVertices.take(10).foreach(println)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Creating a graph from the processed data

// COMMAND ----------

// MAGIC %md 
// MAGIC Now that the we have proccessed the intersections to obtain the vertices of the intersection graph the next step is to process the data for the edges of the graph.

// COMMAND ----------

// MAGIC %md 
// MAGIC An edge between two intersection nodes occurs when they follow eachother sequential along a way. The `segmentedWay` has any entry for the sequence of intersection nodes in that way. By taking a two element sliding window over this sequence, each of the two elements in the window connected and thus an edge exists between them. For this example oneway ways were ignored but could be filtered out by seeing of the way has a `oneway=*` tag, and only adding edges in the correct direction.

// COMMAND ----------

val edges = segmentedWays
  .filter(way => way._2.length > 1) // Filter out ways with no intersections. This may occur because of bad data.
  .flatMap{ case (wayId, ways) => { 
             ways.sliding(2)
               .flatMap(segment => 
                 // Add one edge in each direction
                 List(Edge(segment(0)._1, segment(1)._1, wayId), 
                      Edge(segment(1)._1, segment(0)._1, wayId))
               )
   }}

// COMMAND ----------

// MAGIC %md 
// MAGIC Examining the edges, see that the edge attribute is the way Id for the way that edge belongs to. 

// COMMAND ----------

display(edges)

// COMMAND ----------

// MAGIC %md 
// MAGIC Now that both edge and vertex RDDs have been made a graphX `Graph` can be created.

// COMMAND ----------

val roadGraph = Graph(intersectionVertices, edges.rdd).cache

// COMMAND ----------

// MAGIC %md 
// MAGIC Examining the edges and vertices of the road graph see that they look the same as the edge and vertex rdds that were used to create the graph.

// COMMAND ----------

roadGraph.edges.take(10).foreach(println)

// COMMAND ----------

roadGraph.vertices.take(10).foreach(println)

// COMMAND ----------

// MAGIC %md 
// MAGIC ####Adding Distance information to edges

// COMMAND ----------

import com.esri.core.geometry.GeometryEngine.geodesicDistanceOnWGS84
import com.esri.core.geometry.Point

// COMMAND ----------

// MAGIC %md 
// MAGIC The graph in its current state can be enriched with further data. An obvious thing to add is the distance between two connected intersections, this is a vital step for more involved operations sucha as path routing on the graph.

// COMMAND ----------

// MAGIC %md 
// MAGIC Recall our collection of way nodes:

// COMMAND ----------

display(wayNodes)

// COMMAND ----------

// MAGIC %md 
// MAGIC Here we use these way nodes to create a map from each node to its coordinates (latitude and longitude). This allows lookups of a nodes coordinates by other functions. 

// COMMAND ----------

val OSMNodes = wayNodes
  .map(node => (node.nodeId, (node.latitude, node.longitude)))
  .rdd.collectAsMap

// COMMAND ----------

// MAGIC %md 
// MAGIC The `dist` function takes two node ids, looks up their coordinates in the `OSMNodes` map converting them to ESRI points and finally returning the output of `geoDesicDistanceOnWGS84`, corresponding to the distance between the two points.

// COMMAND ----------

def dist(n1: Long, n2: Long): Double = {
  val n1Coord = OSMNodes(n1)
  val n2Coord = OSMNodes(n2)
  val p1 = new Point(n1Coord._1, n1Coord._2)
  val p2 = new Point(n2Coord._1, n2Coord._2)

  geodesicDistanceOnWGS84(p1, p2)
}

// COMMAND ----------

// MAGIC %md 
// MAGIC To add the distance as an edge attribute use `mapTriplets` to modify each of the edge attributes. `mapTriplets` is needed so that the vertex attributes of both the `src` and `dst` vertices.
// MAGIC 
// MAGIC To calculate the distance of the edge, take a sliding window (size 2) over the outgoing nodes of the destination vertex. Accumulate the distance between these sequential nodes, then add the distance from the destination vertex to the first vertex in the outgoing nodes as well as the distance from the source vertex to the destination vertex. 

// COMMAND ----------

val weightedRoadGraph = roadGraph.mapTriplets{triplet =>
  val wayNodes = triplet.dstAttr(triplet.attr)._1
  
  if (wayNodes.isEmpty) {
    // No nodes inbetween intersections
    // Distance is simply dist(src, dst)
    (triplet.attr, dist(triplet.srcId, triplet.dstId))
  } else {
    var distance: Double = 0.0
    distance += dist(triplet.srcId, wayNodes.first)
    distance += dist(triplet.dstId, wayNodes.last)
    
    if (wayNodes.length > 1) {
      distance += wayNodes.sliding(2).map{
        buff => dist(buff(0),buff(1))}
        .reduce(_ + _)
    }
    
    (triplet.attr, distance)
  }
}.cache

// COMMAND ----------

weightedRoadGraph.edges.take(10).foreach(println)

// COMMAND ----------

weightedRoadGraph.triplets.count