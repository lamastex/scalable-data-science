<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Compute RSVD
============

Here we read the preprcessed data and compute the rSVD

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import com.criteo.rsvd._
import scala.util.Random
import org.apache.spark.mllib.linalg.distributed.MatrixEntry
import org.apache.spark.sql.functions.{min, max}
```

<div class="output execute_result plain_result" execution_count="1">

    import com.criteo.rsvd._
    import scala.util.Random
    import org.apache.spark.mllib.linalg.distributed.MatrixEntry
    import org.apache.spark.sql.functions.{min, max}

</div>

</div>

<div class="cell markdown">

### Set up RSVD config with JSON file

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// code snippet for saving config as json
val config_map = Map("embeddingDim" -> 100, "oversample" -> 30, "powerIter" -> 1, "seed" -> 0, "blockSize" -> 50000, "partitionWidthInBlocks" -> 35, "partitionHeightInBlocks" -> 10)
val config_spark_save = config_map.toSeq.toDF("key","value")
config_spark_save.write.mode("overwrite").json("/projects/group21/rsvd_config.json")
```

<div class="output execute_result plain_result" execution_count="1">

    config_map: scala.collection.immutable.Map[String,Int] = Map(seed -> 0, oversample -> 30, blockSize -> 50000, partitionWidthInBlocks -> 35, partitionHeightInBlocks -> 10, powerIter -> 1, embeddingDim -> 100)
    config_spark_save: org.apache.spark.sql.DataFrame = [key: string, value: int]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// load config from json (assuming only integer values)
val config_spark = spark.read.json("/projects/group21/rsvd_config.json").rdd.map(r => (r(0).toString -> r(1).toString.toInt)).collect.toMap
```

<div class="output execute_result plain_result" execution_count="1">

    config_spark: scala.collection.immutable.Map[String,Int] = Map(seed -> 0, oversample -> 30, blockSize -> 50000, partitionWidthInBlocks -> 35, partitionHeightInBlocks -> 10, powerIter -> 1, embeddingDim -> 100)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    config: com.criteo.rsvd.RSVDConfig = RSVDConfig(100,30,1,0,50000,35,10,false,false)

</div>

</div>

<div class="cell markdown">

### Create pipeline for computing RSVD from dataframe of edge

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    computeRSVD: (groupedCanonicalEdges: org.apache.spark.sql.DataFrame, config: com.criteo.rsvd.RSVDConfig)com.criteo.rsvd.RsvdResults

</div>

</div>

<div class="cell markdown">

### Compute and save RSVD for Ethereum graph

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val groupedCanonicalEdges = spark.read.format("parquet").load("/projects/group21/test_ethereum_canonical_edges").drop("flow")
val rsvd_results_path: String = "/projects/group21/test_ethereum_"

val RsvdResults(leftSingularVectors, singularValues, rightSingularVectors) = computeRSVD(groupedCanonicalEdges, config)
val singularDF = sc.parallelize(singularValues.toArray).toDF()

singularDF.write.format("parquet").mode("overwrite").save(rsvd_results_path + "SingularValues")
```

</div>

<div class="cell markdown">

### Compute and save RSVD for Erdös-Renyi graphs

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
for(i <- 0 to 9) {
  val groupedCanonicalEdges = spark.read.format("parquet").load("/projects/group21/uniform_random_graph" + i)
  val rsvd_results_path: String = "/projects/group21/uniform_random_graph_"

  val RsvdResults(leftSingularVectors, singularValues, rightSingularVectors) = computeRSVD(groupedCanonicalEdges, config)
  
  val singularDF = sc.parallelize(singularValues.toArray).toDF()

  singularDF.write.format("parquet").mode("overwrite").save(rsvd_results_path + "SingularValues" + i)
}
```

</div>

<div class="cell markdown">

### Compute and save RSVD for R-MAT graphs

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
for(i <- 0 to 9) {
  val groupedCanonicalEdges = spark.read.format("parquet").load("/projects/group21/rmat_random_graph" + i)
  val rsvd_results_path: String = "/projects/group21/rmat_random_graph_"

  val RsvdResults(leftSingularVectors, singularValues, rightSingularVectors) = computeRSVD(groupedCanonicalEdges, config)
  
  val singularDF = sc.parallelize(singularValues.toArray).toDF()

  singularDF.write.format("parquet").mode("overwrite").save(rsvd_results_path + "SingularValues" + i)
}
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
