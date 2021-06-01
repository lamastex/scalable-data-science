// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Creating a graph from OpenStreetMap (OSM) data with GraphX
// MAGIC 
// MAGIC This is part of [*Project MEP: Meme Evolution Programme*](http://lamastex.org/lmse/mep) and supported by databricks academic partners program.
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016-2018 Dillon George and Raazesh Sainudiin
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

// MAGIC %md 
// MAGIC ## Part I: Creating the Graph

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Part I(a): Ingest Data

// COMMAND ----------

displayHTML(frameIt("https://wiki.openstreetmap.org/wiki/Osmosis",500))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Google's Protocol Buffers (PBF)
// MAGIC 
// MAGIC Protocol buffers are a language-neutral, platform-neutral extensible mechanism for serializing structured data. See
// MAGIC 
// MAGIC - [https://developers.google.com/protocol-buffers/](https://developers.google.com/protocol-buffers/)
// MAGIC - [https://developers.google.com/protocol-buffers/docs/overview](https://developers.google.com/protocol-buffers/docs/overview)

// COMMAND ----------

// MAGIC %md 
// MAGIC OSM data comes in various formats such as XML and PBF, in this example data is read from PBF files. The method used to read this PBF files is based off of an outstanding pull request on the Magellan Github page, see here for more information see the [relevant page.](https://github.com/harsha2010/magellan/pull/44). But this is from **2019**.
// MAGIC 
// MAGIC __Note__: As this may eventually be merged into Magellan, and so parts of this worksheet might better be implemented using latest Magellan instead. It noetheless has pedagogical value...

// COMMAND ----------

// MAGIC %md 
// MAGIC To begin with download `Beijing.osm.pbf` into dbfs from:
// MAGIC 
// MAGIC - [https://download.bbbike.org/osm/bbbike/Beijing/](https://download.bbbike.org/osm/bbbike/Beijing/)

// COMMAND ----------

displayHTML(frameIt("https://download.bbbike.org/osm/bbbike/Beijing/",400))

// COMMAND ----------

// MAGIC %sh 
// MAGIC curl -O https://download.bbbike.org/osm/bbbike/Beijing/Beijing.osm.pbf
// MAGIC # lamastex back up copy can be wget'd as follows:
// MAGIC # wget wget http://lamastex.org/datasets/public/geospatial/osm/Beijing.osm.pbf

// COMMAND ----------

// MAGIC %md
// MAGIC Let's make a directory in `dbfs:/dataset/osm/beijing` as follows:

// COMMAND ----------

// MAGIC %fs mkdirs /datasets/osm/beijing/

// COMMAND ----------

// MAGIC %md
// MAGIC Let's move it from local file system to distributed file system as follows:

// COMMAND ----------

dbutils.fs.mv("file:/databricks/driver/Beijing.osm.pbf", "dbfs:/datasets/beijing/beijing.pbf")

// COMMAND ----------

// MAGIC %md
// MAGIC Let's make sure it exists in distributed file system:

// COMMAND ----------

// MAGIC %fs ls /datasets/beijing

// COMMAND ----------

// MAGIC %md
// MAGIC  ### Libraries for protocol buffer formats need to be installed first:
// MAGIC  
// MAGIC  - mvn coordinates: 
// MAGIC     - `org.openstreetmap.osmosis:osmosis-osm-binary:0.45`
// MAGIC     - `org.openstreetmap.osmosis:osmosis-pbf:0.45`
// MAGIC     - `com.esri.geometry:esri-geometry-api:2.1.0`

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

// some basic imports
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import org.apache.spark.graphx._

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
  osmosisReader.setSink(new Sink {
    override def process(entityContainer: EntityContainer): Unit = {
      
      if (entityContainer.getEntity.getType != EntityType.Bound) {
        val entity = entityContainer.getEntity
        entity match {
          case node: Node => nodes += node
          case way: Way => {
            val tagSet = way.getTags.map(_.getValue).toSet
            if ( !(tagSet & allowableWays).isEmpty ) {
              // way has at least one tag of interest
              ways += way
            }
          }
          case relation: Relation => relations += relation
        }
      }
    }

    override def initialize(map: java.util.Map[String, AnyRef]): Unit = {
      nodes = ArrayBuffer()
      ways = ArrayBuffer()
      relations = ArrayBuffer()
    }

    override def complete(): Unit = {}

    override def release(): Unit = {} // this is 4.6 method
    
    def close(): Unit = {}
  })

// COMMAND ----------

osmosisReader.run() 

// COMMAND ----------

// MAGIC %md 
// MAGIC The follwing cell runs the reader, and it is only after this call that any work will be done. This will take a second or two to complete.
// MAGIC 
// MAGIC __Note:__ When running the the reader multiple times start from the cell where the `file` is defined as it is consumed by running the reader.
// MAGIC 
// MAGIC So we wrap us all these calls in one cell below.

// COMMAND ----------

// just doing all the steps above together here
// when everything works you can wrap these in a function for instance
val fs = FileSystem.get(new Configuration())
val path = new Path("dbfs:/datasets/beijing/beijing.pbf")
val file = fs.open(path)

var nodes: ArrayBuffer[Node] = ArrayBuffer()
var ways: ArrayBuffer[Way] = ArrayBuffer()
var relations: ArrayBuffer[Relation] = ArrayBuffer()

val osmosisReader = new OsmosisReader(file)
  osmosisReader.setSink(new Sink {
    override def process(entityContainer: EntityContainer): Unit = {
      
      if (entityContainer.getEntity.getType != EntityType.Bound) {
        val entity = entityContainer.getEntity
        entity match {
          case node: Node => nodes += node
          case way: Way => {
            val tagSet = way.getTags.map(_.getValue).toSet
            if ( !(tagSet & allowableWays).isEmpty ) {
              // way has at least one tag of interest
              ways += way
            }
          }
          case relation: Relation => relations += relation
        }
      }
    }

    override def initialize(map: java.util.Map[String, AnyRef]): Unit = {
      nodes = ArrayBuffer()
      ways = ArrayBuffer()
      relations = ArrayBuffer()
    }

    override def complete(): Unit = {}

    override def release(): Unit = {} // this is 4.6 method
    
    def close(): Unit = {}
  })

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

nodeDS.count

// COMMAND ----------

// MAGIC %md 
// MAGIC What the dataset looks like:

// COMMAND ----------

display(nodeDS.limit(10))

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

wayDS.count

// COMMAND ----------

// MAGIC %md 
// MAGIC We can see that the Nodes contained in a way are stored as a list of node ids.

// COMMAND ----------

display(wayDS.limit(5))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Part II: Coarsen Graph

// COMMAND ----------

// MAGIC %md 
// MAGIC An important consideration when constructing a road network graph is the granularity of the stored map. 
// MAGIC 
// MAGIC Is it important for the graph to contain all nodes and ways or can coarser representation of the network be used?
// MAGIC 
// MAGIC A notion called *Lumped Markov chains* was introduced by Kemeney and Snell and this allows one to project the map-matched trajectories in continuous time from the finest resolution of the OSM (represented as a state-space of the Markov chain on which trajectories move) to a coarser representation of the state space. Such a lumping need not be Markov and care must be taken to preserver the original Markov property. Our rationale for such a lumping is not only for computational efficiency but more crucially for privacy-preservation guarantees of any decision procedure based on a lumping, in an appropriate sense.
// MAGIC 
// MAGIC One way to represent a coarse road graph is to have the vertices of the graph represent interesctions along the roads, and have the edges represent the way segment that connects two intersections (this is not immediately privacy-preserving!). 

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Lumpability",400))

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Part II(a): Find Intersections

// COMMAND ----------

// MAGIC %md 
// MAGIC The first step in constructing such an intersection graph is to identify all the osm-nodes that are intersections. We can consider a node to be an intersection if it occurs in more than one way. Then the node of id of intersections will occur in multiple node list in our `wayDS`.

// COMMAND ----------

wayDS.printSchema()

// COMMAND ----------

wayDS.show(2,false)

// COMMAND ----------

// MAGIC %md 
// MAGIC The next step does this counting. It first extracts the nodes column from `wayDS` and flatmapping those arrays so that we have a Dataset of all nodes in the ways.
// MAGIC Finally, let's do a grouping by the node ids and counting gives the frequency of each node. 

// COMMAND ----------

import org.apache.spark.sql.functions.explode

val nodeCounts = wayDS
                    .select(explode('nodes).as("node"))
                    .groupBy('node).count
nodeCounts.show(5)

// COMMAND ----------

// MAGIC %md 
// MAGIC Now intersection nodes are found by filtering out all node ids with a count less than two, i.e, keep all node ids with 2 or more counts, meaning they occur in 2 or more distinct ways. 
// MAGIC 
// MAGIC The last select step simply removes the count field from the Dataset.

// COMMAND ----------

val intersectionNodes = nodeCounts.filter('count >= 2).select('node).cache() 

// COMMAND ----------

intersectionNodes.count() // number of intersections in Beijing

// COMMAND ----------

intersectionNodes.show(5)

// COMMAND ----------

// MAGIC %md 
// MAGIC In order to share this data amongst all partitions create a broadcast variable containing the set of intersection node IDs. 
// MAGIC 
// MAGIC For a larger map broadcasting this array may not be feasible but in this case the number of intersection nodes is small enough to not cause any problems of note.

// COMMAND ----------

intersectionNodes.rdd.map(row => row(0).asInstanceOf[Long])

// COMMAND ----------

def intersectionSetVal: scala.collection.Set[Long] = intersectionNodes.as[Long].collect.toSet;

// COMMAND ----------

val intersectionSetBCV = sc.broadcast(intersectionSetVal)

// COMMAND ----------

// just have a DataFrame of the same intersection nodes
val intersectionSetDF = sc.parallelize(intersectionSetVal.toSeq).toDF("intersectionNodeIDs").cache()

// COMMAND ----------

intersectionSetDF.count

// COMMAND ----------

intersectionSetBCV.value // this is a scala.collection.immutable.Set[Long]
               .take(10)
               .foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC Version issues unresolved yet...

// COMMAND ----------

val rdd = sc.parallelize(Seq(2575647207L,-178798L))

// COMMAND ----------

// IVAN you want this set NOT intersectionSetBCV as it was already broadcast above
val intersectionSetVal: scala.collection.Set[Long] = intersectionNodes.as[Long].limit(20).collect.toSet;

// COMMAND ----------

// Ivan's cell - do not touch

// 1. Possible rewrite using join
val intersectionTable = intersectionNodes.as[Long]
val rddTable = rdd.toDF("value")

// Should trigger broadcast hash join
val res = rddTable.join(intersectionTable, $"value" === $"node", "left_outer").select($"value", $"node".isNotNull.as("contains"))
res.show

// It looks like there is a problem with figuring out the refernces for a closure. It works with simple example for Set(1, 2, 3), and then breaks if trying larger example that involves RDD, after that even simple case stops working. Hmm... will get back to it when I have more time.

// COMMAND ----------

//Ivan, alternatively, we could do some other operation - the real problem Dillon is solving is in cell below with string "IVAN-REAL-PROBLEM"

// Yes, I got it. I wrote alternative solution using a join. This will work for now, will have a look at the real-problem cell

// COMMAND ----------

intersectionSetBCV.value.contains(1L)

// COMMAND ----------

rdd.mapPartitions({row => // what's wrong here Ivan? I replied, see cell above
 row.map(x => (x, intersectionSetBCV.value.contains(x)))
}, preservesPartitioning = true)

// COMMAND ----------

rdd.map(x => intersectionSetBCV.value.contains(x))

// COMMAND ----------

// MAGIC %md
// MAGIC ### Recalling Broadcast Variables in Spark
// MAGIC Here is a brief excursion into broadcast variables in Spark taken from [here](http://www.sparktutorials.net/Spark+Broadcast+Variables+-+What+are+they+and+how+do+I+use+them).

// COMMAND ----------

val hoods = Seq((1, "Mission"), (2, "SOMA"), (3, "Sunset"), (4, "Haight Ashbury"))
val checkins = Seq((234, 1),(567, 2), (234, 3), (532, 2), (234, 4))
val hoodsRdd = sc.parallelize(hoods)
val checkRdd = sc.parallelize(checkins)

// COMMAND ----------

val broadcastedHoods = sc.broadcast(hoodsRdd.collectAsMap())

// COMMAND ----------

val checkinsWithHoods = checkRdd.mapPartitions({row =>
 row.map(x => (x._1, x._2, broadcastedHoods.value.getOrElse(x._2, -1)))
}, preservesPartitioning = true)


// COMMAND ----------

checkinsWithHoods.take(5)
// res3: Array[(Int, Int, Any)] =
// Array((234,1,Mission), (567,2,SOMA), (234,3,Sunset), (532,2,SOMA), (234,4,Haight Ashbury))

// COMMAND ----------

// MAGIC %md 
// MAGIC Just a reminder what `nodeDS` looks like.

// COMMAND ----------

display(nodeDS.limit(2))

// COMMAND ----------

// MAGIC %md 
// MAGIC The next cell creates a Dataset of all the unique node IDs in our ways. 

// COMMAND ----------

val wayNodeIds = wayDS.flatMap(_.nodes).distinct

// COMMAND ----------

wayNodeIds.count // number of distinct way nodes in Beijing

// COMMAND ----------

wayNodeIds.show(5)

// COMMAND ----------

// MAGIC %md 
// MAGIC Now join `nodeDS` with `wayNodeIds`, only retaining those nodes that occur in both. This gives a new Dataset containing only those nodes that are in a way, this includes all the information not just the ids as in `wayNodeIds`.

// COMMAND ----------

val wayNodes = nodeDS.as("nodes")
  .joinWith(wayNodeIds.as("ways"), $"ways.value" === $"nodes.nodeId")
  .map(_._1).cache

// COMMAND ----------

wayNodes.count()

// COMMAND ----------

display(wayNodes.limit(10))

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC **IVAN-REAL-PROBLEM**
// MAGIC 
// MAGIC Now identify and label any possible intersection vertices. Map `wayDS`, and for each node in each way tag each node as `true` if it is in the `intersectionSet` or `false` otherwise.
// MAGIC Then as an additional step the nodes at the start and end of the way list are set to be intersections to ensure that these perimeter ways are retained in the coarsening step.

// COMMAND ----------

//@transient val iSet = intersectionSet.value

// COMMAND ----------

wayDS.show(2,false) // reminder of what wayDS is

// COMMAND ----------

// Ivan's cell - do not touch

// The idea is the same, we would simply join with intersection set and then extract "contains" flag. 
// We would also maintain a boolean flag to indicate whether or not it is a start/end or the middle element.
// Note that it is good to ensure that such "nodes" are not very long.
// I also assume that "wayDS" is relatively small, so it does not take half of the century to run

// Assumption: we need to preserve the relative order of the "nodes" sequence

import org.apache.spark.sql.functions.{collect_list, map, udf}

// You could try using `getItem` methods
// I assume that each "nodes" sequence contains at least one node
// We do not really need first and last elements from the sequence and 
// when combining with original nodes, just we assign them "true"

val remove_first_and_last = udf((x: Seq[Long]) => x.drop(1).dropRight(1))

val tmp = wayDS

val nodes = tmp.
  select($"wayId", remove_first_and_last($"nodes").as("nodes")).
  withColumn("node", explode($"nodes")).
  drop("nodes")

// Turn intersection set into a dataset to join (all values must be unique)
val intersections = intersectionSet.value.toSeq.toDF("value")

case class MappedWay(wayId: Long, labels: Seq[Map[Long, Boolean]])
val maps = nodes.join(intersections, $"value" === $"node", "left_outer").
  // Here we include bound into consideration for "contains in intersection", 
  // because if node is a start/end, then it is always included
  select($"wayId", $"node", $"value".isNotNull.as("contains")).
  groupBy("wayId").agg(collect_list(map($"node", $"contains")).as("labels")).as[MappedWay]

// Now we need to join with original "wayDS" and merge labels with nodes
val combine = udf((nodes: Seq[Long], labels: Seq[scala.collection.immutable.Map[Long, Boolean]]) => {
  // If labels does not have "node", then it is either start/end - we assign true for it
  val m = labels.map(_.toSeq).flatten.toMap
  nodes.map { node => (node, m.getOrElse(node, true)) }
})

// Building resulting node ways
case class ResultWay(wayId: Long, tags: Seq[String], labeledNodes: Seq[(Long, Boolean)])
//case class ResultWay(wayId: Long, tags: Seq[String], nodes: Seq[(Long, Boolean)]) //labeledNodes
//val labeledWays = tmp.join(maps, "wayId").select($"wayId", $"tags", combine($"nodes", $"labels").as("nodes")).as[ResultWay].cache()
val labeledWays = tmp.join(maps, "wayId")
                     .select($"wayId", $"tags", combine($"nodes", $"labels").as("labeledNodes")).as[ResultWay].cache()


//res.show()
// res.collect - works

// foreach still fails with serialization error. Hmm...
// But anyway, this should give you an idea of how to do it, I might have missed something

// COMMAND ----------

labeledWays.count

// COMMAND ----------

labeledWays.take(2)

// COMMAND ----------

//IVAN-REAL-PROBLEM - we could use pure DataFrame or DataSet operations to avoid the broadcast issues..., but logic is smpler with broadcast variable as set
/*
case class LabeledWay(wayId: Long, labeledNodes: Array[(Long, Boolean)])

val labeledWays = wayDS.map{ way => {
  val nodesWithLabels = way.nodes.map(id => (id, intersectionSet.value contains id))
  
  nodesWithLabels(nodesWithLabels.size - 1) = (nodesWithLabels.last._1, true) // Add fake intersections and the end of a way
  nodesWithLabels(0) = (nodesWithLabels.head._1, true)
  LabeledWay(way.wayId, nodesWithLabels)
}}.cacheval labeledWays = wayDS.map{ way => {
  val nodesWithLabels: Array[(Long, Boolean)] = way.nodes.map(id => (id, iSet.contains(id)))
  
  nodesWithLabels(nodesWithLabels.size - 1) = (nodesWithLabels.last._1, true) // Add fake intersections and the end of a way
  nodesWithLabels(0) = (nodesWithLabels.head._1, true)
  LabeledWay(way.wayId, nodesWithLabels)
}}
*/

// COMMAND ----------

ResultWay.

// COMMAND ----------

// DBTITLE 1,Number of Intersection nodes in each way
labeledWays.select('labeledNodes ) //.take(5)
        
/*
  (way.wayId, way.labeledNodes//.filter{_._2}
                 .length)
})*/

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

def segmentWay(way: Seq[(Long, Boolean)]): Array[(Long, Array[Long], Array[Long])] = {
  
  val indexedNodes: Seq[((Long, Boolean), Int)] = way.zipWithIndex
  
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

val roadGraph = Grap h(intersectionVertices, edges.rdd).cache

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