// Databricks notebook source exported at Sat, 3 Dec 2016 14:00:12 UTC
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

val landMarkVertexIds = Seq(4L, 0L, 9L)
val result = GraphXShortestWeightedPaths.run(graph, landMarkVertexIds)

// COMMAND ----------

// Found shortest paths
println(result.vertices.collect.mkString("\n"))

// COMMAND ----------

// edges with weights, make sure to check couple of shortest paths from above
display(result.edges.toDF)

// COMMAND ----------

display(graph.edges.toDF) // this is the directed weighted edge of the graph

// COMMAND ----------

// now let us collect the shortest distance between every vertex and every landmark vertex
// to manipulate scala maps that are vertices of the result see: http://docs.scala-lang.org/overviews/collections/maps.html
// a quick point: http://stackoverflow.com/questions/28769367/scala-map-a-map-to-list-of-tuples
val shortestDistsVertex2Landmark = result.vertices.flatMap(GxSwpSPMap => {
  GxSwpSPMap._2.toSeq.map(x => (GxSwpSPMap._1, x._1, x._2)) // to get triples: vertex, landmarkVertex, shortest_distance
})

// COMMAND ----------

shortestDistsVertex2Landmark.collect.mkString("\n")

// COMMAND ----------

// MAGIC %md
// MAGIC #### Let's make a DataFrame for visualizing pairwise matrix plots
// MAGIC 
// MAGIC We want to make 4 columns in this example as follows (note actual values change for each realisation of graph!):
// MAGIC 
// MAGIC ```
// MAGIC landmark_Id1 ("0"),   landmarkID2 ("4"), landmarkId3 ("9"),  srcVertexId
// MAGIC ------------------------------------------------------------------------
// MAGIC 0.0,                  0.7425..,          0.8718,                0
// MAGIC 0.924...,             1.2464..,          1.0472,                1
// MAGIC ...
// MAGIC ```

// COMMAND ----------

// http://alvinalexander.com/scala/how-to-sort-map-in-scala-key-value-sortby-sortwith
// we need this to make sure that the maps are ordered by the keys for ensuring unique column values
import scala.collection.immutable.ListMap
import sqlContext.implicits._

// COMMAND ----------

 // recall our landmark vertices in landMarkVertexIds. let's use their Strings for names
val unorderedNamedLandmarkVertices = landMarkVertexIds.map(id => (id, id.toString) )
val orderedNamedLandmarkVertices = ListMap(unorderedNamedLandmarkVertices.sortBy(_._1):_*)
val orderedLandmarkVertexNames = orderedNamedLandmarkVertices.toSeq.map(x => x._2)
orderedLandmarkVertexNames.mkString(", ")

// COMMAND ----------

// this is going to be our column names
val columnNames:Seq[String] = orderedLandmarkVertexNames :+ "srcVertexId"

// COMMAND ----------

// a case class to make a data-frame quickly from the result
case class SeqOfDoublesAndsrcVertexId(shortestDistances: Seq[Double], srcVertexId: VertexId)

// COMMAND ----------

val shortestDistsSeqFromVertex2Landmark2DF = result.vertices.map(GxSwpSPMap => {
  //GxSwpSPMap._2.toSeq.map(x => (GxSwpSPMap._1, x._1, x._2)) // from before to get triples: vertex, landmarkVertex, shortest_distance
  val v = GxSwpSPMap._1
  val a = ListMap(GxSwpSPMap._2.toSeq.sortBy(_._1):_*).toSeq.map(x => x._2)
  val d = (a,v)
  d
}).map(x => SeqOfDoublesAndsrcVertexId(x._1, x._2)).toDF()

// COMMAND ----------

display(shortestDistsSeqFromVertex2Landmark2DF) // but this dataframe needs the first column exploded into 3 columns

// COMMAND ----------

// MAGIC %md
// MAGIC Now we want to make separate columns for each distance in the Sequence in column 'shortestDistances'.
// MAGIC 
// MAGIC Let us use the following ideas for this:
// MAGIC * https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/3741049972324885/2662535171379268/4413065072037724/latest.html

// COMMAND ----------

// this is from https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/3741049972324885/2662535171379268/4413065072037724/latest.html
import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions.{lit, udf}

// UDF to extract i-th element from array column
//val elem = udf((x: Seq[Int], y: Int) => x(y))
val elem = udf((x: Seq[Double], y: Int) => x(y)) // modified for Sequence of Doubles

// Method to apply 'elem' UDF on each element, requires knowing length of sequence in advance
def split(col: Column, len: Int): Seq[Column] = {
  for (i <- 0 until len) yield { elem(col, lit(i)).as(s"$col($i)") }
}

// Implicit conversion to make things nicer to use, e.g. 
// select(Column, Seq[Column], Column) is converted into select(Column*) flattening sequences
implicit class DataFrameSupport(df: DataFrame) {
  def select(cols: Any*): DataFrame = {
    var buffer: Seq[Column] = Seq.empty
    for (col <- cols) {
      if (col.isInstanceOf[Seq[_]]) {
        buffer = buffer ++ col.asInstanceOf[Seq[Column]]
      } else {
        buffer = buffer :+ col.asInstanceOf[Column]
      }
    }
    df.select(buffer:_*)
  }
}

// COMMAND ----------

val shortestDistsFromVertex2Landmark2DF = shortestDistsSeqFromVertex2Landmark2DF.select(split($"shortestDistances", 3), $"srcVertexId")

// COMMAND ----------

display(shortestDistsFromVertex2Landmark2DF)

// COMMAND ----------

// now let's give it our names based on the landmark vertex Ids
val shortestDistsFromVertex2Landmark2DF = shortestDistsSeqFromVertex2Landmark2DF.select(split($"shortestDistances", 3), $"srcVertexId").toDF(columnNames:_*)

// COMMAND ----------

display(shortestDistsFromVertex2Landmark2DF)

// COMMAND ----------

display(shortestDistsFromVertex2Landmark2DF.select($"0",$"4",$"9"))

// COMMAND ----------

