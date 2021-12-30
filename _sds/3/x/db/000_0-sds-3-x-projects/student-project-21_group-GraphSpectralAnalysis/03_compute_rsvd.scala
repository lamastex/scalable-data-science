// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC #Compute RSVD
// MAGIC Here we read the preprcessed data and compute the rSVD

// COMMAND ----------

import com.criteo.rsvd._
import scala.util.Random
import org.apache.spark.mllib.linalg.distributed.MatrixEntry
import org.apache.spark.sql.functions.{min, max}

// COMMAND ----------

// MAGIC %md
// MAGIC ### Set up RSVD config with JSON file

// COMMAND ----------

// code snippet for saving config as json
val config_map = Map("embeddingDim" -> 100, "oversample" -> 30, "powerIter" -> 1, "seed" -> 0, "blockSize" -> 50000, "partitionWidthInBlocks" -> 35, "partitionHeightInBlocks" -> 10)
val config_spark_save = config_map.toSeq.toDF("key","value")
config_spark_save.write.mode("overwrite").json("/projects/group21/rsvd_config.json")

// COMMAND ----------

// load config from json (assuming only integer values)
val config_spark = spark.read.json("/projects/group21/rsvd_config.json").rdd.map(r => (r(0).toString -> r(1).toString.toInt)).collect.toMap

// COMMAND ----------

// Create RSVD configuration
val config = RSVDConfig(
  embeddingDim = config_spark("embeddingDim"),
  oversample = config_spark("oversample"),
  powerIter = config_spark("powerIter"),
  seed = config_spark("seed"),
  blockSize = config_spark("blockSize"),
  partitionWidthInBlocks = config_spark("partitionWidthInBlocks"),
  partitionHeightInBlocks = config_spark("partitionHeightInBlocks"),
  computeLeftSingularVectors = false,
  computeRightSingularVectors = false
)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Create pipeline for computing RSVD from dataframe of edge

// COMMAND ----------

def computeRSVD (groupedCanonicalEdges : org.apache.spark.sql.DataFrame, config : RSVDConfig): RsvdResults = {
  val matHeight = groupedCanonicalEdges.count()
  val Row(maxValue: Int) = groupedCanonicalEdges.agg(max("dst")).head
  val matWidth = maxValue
  val incidenceMatrixEntries = groupedCanonicalEdges.rdd.flatMap{
    case Row(src: Int, dst: Int, id: Int) => List(MatrixEntry(id-1, src-1, -1), MatrixEntry(id-1, dst-1, 1))
  }
  // Create block matrix and compute RSVD
  val matrixToDecompose = BlockMatrix.fromMatrixEntries(incidenceMatrixEntries, matHeight = matHeight, matWidth = matWidth, config.blockSize, config.partitionHeightInBlocks, config.partitionWidthInBlocks)
  return RSVD.run(matrixToDecompose, config, sc)
}

// COMMAND ----------

// MAGIC %md
// MAGIC ### Compute and save RSVD for Ethereum graph

// COMMAND ----------

val groupedCanonicalEdges = spark.read.format("parquet").load("/projects/group21/test_ethereum_canonical_edges").drop("flow")
val rsvd_results_path: String = "/projects/group21/test_ethereum_"

val RsvdResults(leftSingularVectors, singularValues, rightSingularVectors) = computeRSVD(groupedCanonicalEdges, config)
val singularDF = sc.parallelize(singularValues.toArray).toDF()

singularDF.write.format("parquet").mode("overwrite").save(rsvd_results_path + "SingularValues")

// COMMAND ----------

// MAGIC %md
// MAGIC ### Compute and save RSVD for Erdös-Renyi graphs

// COMMAND ----------

for(i <- 0 to 9) {
  val groupedCanonicalEdges = spark.read.format("parquet").load("/projects/group21/uniform_random_graph" + i)
  val rsvd_results_path: String = "/projects/group21/uniform_random_graph_"

  val RsvdResults(leftSingularVectors, singularValues, rightSingularVectors) = computeRSVD(groupedCanonicalEdges, config)
  
  val singularDF = sc.parallelize(singularValues.toArray).toDF()

  singularDF.write.format("parquet").mode("overwrite").save(rsvd_results_path + "SingularValues" + i)
}

// COMMAND ----------

// MAGIC %md
// MAGIC ### Compute and save RSVD for R-MAT graphs

// COMMAND ----------

for(i <- 0 to 9) {
  val groupedCanonicalEdges = spark.read.format("parquet").load("/projects/group21/rmat_random_graph" + i)
  val rsvd_results_path: String = "/projects/group21/rmat_random_graph_"

  val RsvdResults(leftSingularVectors, singularValues, rightSingularVectors) = computeRSVD(groupedCanonicalEdges, config)
  
  val singularDF = sc.parallelize(singularValues.toArray).toDF()

  singularDF.write.format("parquet").mode("overwrite").save(rsvd_results_path + "SingularValues" + i)
}

// COMMAND ----------

