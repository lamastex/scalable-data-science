// Databricks notebook source exported at Fri, 11 Nov 2016 03:20:18 UTC
// MAGIC %md
// MAGIC # Extending spark.graphx.lib.ShortestPaths to GraphXShortestWeightedPaths
// MAGIC 
// MAGIC ### 2016, Ivan Sadikov and Raazesh Sainudiin
// MAGIC 
// MAGIC We extend Shortest Paths algorithm in Spark's GraphX Library to allow for user-specified edge-weights as an edge attribute.
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program.
// MAGIC 
// MAGIC The analysis is available in the following databricks notebook:
// MAGIC * [http://lamastex.org/lmse/mep/src/GraphXShortestWeightedPaths.html](http://lamastex.org/lmse/mep/src/GraphXShortestWeightedPaths.html)
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016 Ivan Sadikov and Raazesh Sainudiin
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
// MAGIC ### Let's modify shortest paths algorithm to allow for user-specified edge-weights
// MAGIC Update shortest paths algorithm to work over edge attribute of edge-weights as Double, key concepts are:
// MAGIC - we increment map with delta, which is `edge.attr`
// MAGIC - edge attribute is anything numeric, tested on Double
// MAGIC - infinity value is not infinity, but `Integer.MAX_VALUE`
// MAGIC 
// MAGIC Modifying the following code:
// MAGIC * https://github.com/apache/spark/blob/master/graphx/src/main/scala/org/apache/spark/graphx/lib/ShortestPaths.scala
// MAGIC 
// MAGIC Explained here:
// MAGIC * http://note.yuhc.me/2015/03/graphx-pregel-shortest-path/

// COMMAND ----------

import scala.reflect.ClassTag
import org.apache.spark.graphx._

/**
 * Computes shortest weighted paths to the given set of landmark vertices, returning a graph where each
 * vertex attribute is a map containing the shortest-path distance to each reachable landmark.
 * Currently supports only Graph of [VD, Double], where VD is an arbitrary vertex type.
 */
object GraphXShortestWeightedPaths extends Serializable {
  /** Stores a map from the vertex id of a landmark to the distance to that landmark. */
  type SPMap = Map[VertexId, Double]
  // initial and infinity values, use to relax edges
  private val INITIAL = 0.0
  private val INFINITY = Int.MaxValue.toDouble

  private def makeMap(x: (VertexId, Double)*) = Map(x: _*)

  private def incrementMap(spmap: SPMap, delta: Double): SPMap = {
    spmap.map { case (v, d) => v -> (d + delta) }
  }

  private def addMaps(spmap1: SPMap, spmap2: SPMap): SPMap = {
    (spmap1.keySet ++ spmap2.keySet).map {
      k => k -> math.min(spmap1.getOrElse(k, INFINITY), spmap2.getOrElse(k, INFINITY))
    }.toMap
  }
  
  // at this point it does not really matter what vertex type is
  def run[VD](graph: Graph[VD, Double], landmarks: Seq[VertexId]): Graph[SPMap, Double] = {
    val spGraph = graph.mapVertices { (vid, attr) =>
      // initial value for itself is 0.0 as Double
      if (landmarks.contains(vid)) makeMap(vid -> INITIAL) else makeMap()
    }

    val initialMessage = makeMap()

    def vertexProgram(id: VertexId, attr: SPMap, msg: SPMap): SPMap = {
      addMaps(attr, msg)
    }

    def sendMessage(edge: EdgeTriplet[SPMap, Double]): Iterator[(VertexId, SPMap)] = {
      val newAttr = incrementMap(edge.dstAttr, edge.attr)
      if (edge.srcAttr != addMaps(newAttr, edge.srcAttr)) Iterator((edge.srcId, newAttr))
      else Iterator.empty
    }

    Pregel(spGraph, initialMessage)(vertexProgram, sendMessage, addMaps)
  }
}

println("Usage: val result = GraphXShortestWeightedPaths.run(graph, Seq(4L, 0L, 9L))")

// COMMAND ----------

// MAGIC %md
// MAGIC ### Generate test graph
// MAGIC Generate simple graph with double weights for edges

// COMMAND ----------

import scala.util.Random

import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.graphx.util.GraphGenerators

// A graph with edge attributes containing distances
val graph: Graph[Long, Double] = GraphGenerators.logNormalGraph(sc, numVertices = 10, seed=123L).mapEdges { e => 
  // to make things nicer we assign 0 distance to itself
  if (e.srcId == e.dstId) 0.0 else Random.nextDouble()
}

// COMMAND ----------

val result = GraphXShortestWeightedPaths.run(graph, Seq(4L, 0L, 9L))

// COMMAND ----------

// Found shortest paths
println(result.vertices.collect.mkString("\n"))

// COMMAND ----------

// edges with weights, make sure to check couple of shortest paths from above
display(result.edges.toDF)