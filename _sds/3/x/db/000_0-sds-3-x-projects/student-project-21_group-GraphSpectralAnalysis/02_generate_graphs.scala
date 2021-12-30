// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Generate random graphs
// MAGIC Here random graphs are generated, first using Erdös-Renyi method and then using R-MAT.

// COMMAND ----------

import org.apache.spark.graphx.util.GraphGenerators
import scala.util.Random
import org.apache.spark.sql.{Row, DataFrame}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{functions => F}
import org.apache.spark.sql.types.{IntegerType, LongType, DoubleType, StringType, StructField, StructType}

// COMMAND ----------

// Values taken from the Ethereum graph
val numNodes = 1520925
val numEdges = 2152835

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ## Function for making a canonical ordering for the edges of a graph
// MAGIC - Input is a dataframe with rows of "src" and "dst" node numbers
// MAGIC - A new node id is computed such that the nodes have ids 0,1,2,...
// MAGIC - The canonical ordering is made such that each edge will point from lower to higher index

// COMMAND ----------

def makeEdgesCanonical (edgeDF : org.apache.spark.sql.DataFrame): org.apache.spark.sql.DataFrame = {
  // Remove self-loops
  val edgeDFClean = edgeDF.distinct().where(F.col("src") =!= F.col("dst"))
  
  // Provide each node with an index id
  val nodes = edgeDFClean.select(F.col("src").alias("node")).union(edgeDFClean.select(F.col("dst").alias("node"))).distinct()
  val nodes_window = Window.orderBy("node")
  val nodesWithids = nodes.withColumn("id", F.row_number().over(nodes_window))
  
  // Add the canonical node ids to the edgeDF and drop the old ids
  val dstNodes = nodesWithids.withColumnRenamed("node", "dst").withColumnRenamed("id", "dst__")
  val srcNodes = nodesWithids.withColumnRenamed("node", "src").withColumnRenamed("id", "src__")
  val edgesWithBothIds = edgeDFClean.join(dstNodes, dstNodes("dst") === edgeDFClean("dst"))
                           .join(srcNodes, srcNodes("src") === edgeDFClean("src"))
                           .drop("src").drop("dst")
  
  val edgesWithCanonicalIds = edgesWithBothIds.withColumn("src",
                    F.when(F.col("dst__") > F.col("src__"), F.col("src__")).otherwise(F.col("dst__"))
                  ).withColumn("dst",
                    F.when(F.col("dst__") > F.col("src__"), F.col("dst__")).otherwise(F.col("src__"))
                  ).drop("src__").drop("dst__").distinct().where(F.col("src") =!= F.col("dst"))
  
  val edges_window = Window.orderBy(F.col("src"), F.col("dst"))
  val GroupedCanonicalEdges = edgesWithCanonicalIds.withColumn("id", F.row_number().over(edges_window))
  return GroupedCanonicalEdges
}

// COMMAND ----------

// MAGIC %md
// MAGIC ## Generate Erdös-Renyi graph (uniform edge sampling)

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Function for sampling an Erdös-Renyi graph
// MAGIC The resulting graph will have at most the number of nodes given by numNodes and at most numEdges edges.
// MAGIC The number of nodes is less than numNodes if some nodes did not have an edge to another node.
// MAGIC The number of edges is less than numEdges if some edges are duplicates or if some edges are self-loops.

// COMMAND ----------

def sampleERGraph (numNodes : Int, numEdges : Int, iter : Int): org.apache.spark.sql.DataFrame = {
  val randomEdges = sc.parallelize(0 until numEdges).map {
    idx =>
      val random = new Random(42 + iter * numEdges + idx)
      val src = random.nextInt(numNodes)
      val dst = random.nextInt(numNodes)
      if (src > dst) Row(dst, src) else Row(src, dst)
  }

  val schema = new StructType()
    .add(StructField("src", IntegerType, true))
    .add(StructField("dst", IntegerType, true))

  val groupedCanonicalEdges = makeEdgesCanonical(spark.createDataFrame(randomEdges, schema))
  return groupedCanonicalEdges
}

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Sample and save 10 different Erdös-Renyi graphs with different seeds and save each to parquet

// COMMAND ----------

for(i <- 0 to 9) {
  val groupedCanonicalEdges = sampleERGraph(numNodes, numEdges, iter=i)
  groupedCanonicalEdges.write.format("parquet").mode("overwrite").save("/projects/group21/uniform_random_graph" + i)
}

// COMMAND ----------

// MAGIC %md
// MAGIC ## Generate R-MAT graph

// COMMAND ----------

// MAGIC %md
// MAGIC #### The default parameters for R-MAT generation

// COMMAND ----------

println("RMAT a: " + GraphGenerators.RMATa)
println("RMAT b: " + GraphGenerators.RMATb)
println("RMAT c: " + GraphGenerators.RMATc)
println("RMAT d: " + GraphGenerators.RMATd)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Function for generating a R-MAT graph, storing the edges as a Dataframe and applying makeEdgesCanonical

// COMMAND ----------

def sampleRMATGraph (numNodes : Int, numEdges : Int): org.apache.spark.sql.DataFrame = {
  val rmatGraphraw = GraphGenerators.rmatGraph(sc=spark.sparkContext, requestedNumVertices=numNodes, numEdges=numEdges)
  val rmatedges = rmatGraphraw.edges.map{ 
    edge => Row(edge.srcId, edge.dstId)
  }

  val schema = new StructType()
    .add(StructField("src", LongType, true))
    .add(StructField("dst", LongType, true))

  val rmatGroupedCanonicalEdges = makeEdgesCanonical(spark.createDataFrame(rmatedges, schema))
  return rmatGroupedCanonicalEdges
}

// COMMAND ----------

// MAGIC %md
// MAGIC #### Sample 10 R-MAT graphs and save each to parquet

// COMMAND ----------

for(i <- 0 to 9) {
  val groupedCanonicalEdges = sampleRMATGraph(numNodes, numEdges)
  groupedCanonicalEdges.write.format("parquet").mode("overwrite").save("/projects/group21/rmat_random_graph" + i)
}

// COMMAND ----------

