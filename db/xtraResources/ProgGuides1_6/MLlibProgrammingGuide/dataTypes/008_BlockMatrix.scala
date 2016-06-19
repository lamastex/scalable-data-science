// Databricks notebook source exported at Sun, 19 Jun 2016 03:06:55 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [Apache Spark 1.6 mllib-progamming-guide on mllib-data-types](http://spark.apache.org/docs/latest/mllib-data-types.html).
// MAGIC 
// MAGIC # [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/000_MLlibProgGuide)
// MAGIC 
// MAGIC ## [Data Types - MLlib Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)
// MAGIC 
// MAGIC -   [Local vector](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/001_LocalVector) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#local-vector)
// MAGIC -   [Labeled point](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/002_LabeledPoint) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#labeled-point)
// MAGIC -   [Local matrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/003_LocalMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#local-matrix)
// MAGIC -   [Distributed matrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#distributed-matrix)
// MAGIC     -   [RowMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/005_RowMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#rowmatrix)
// MAGIC     -   [IndexedRowMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#indexedrowmatrix)
// MAGIC     -   [CoordinateMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#coordinatematrix)
// MAGIC     -   [BlockMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/008_BlockMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#blockmatrix)
// MAGIC 
// MAGIC MLlib supports local vectors and matrices stored on a single machine, as
// MAGIC well as distributed matrices backed by one or more RDDs. Local vectors
// MAGIC and local matrices are simple data models that serve as public
// MAGIC interfaces. The underlying linear algebra operations are provided by
// MAGIC [Breeze](http://www.scalanlp.org/) and [jblas](http://jblas.org/). A
// MAGIC training example used in supervised learning is called a “labeled point”
// MAGIC in MLlib.

// COMMAND ----------

// MAGIC %md
// MAGIC ### BlockMatrix in Scala
// MAGIC 
// MAGIC A `BlockMatrix` is a distributed matrix backed by an RDD of
// MAGIC `MatrixBlock`s, where a `MatrixBlock` is a tuple of
// MAGIC `((Int, Int), Matrix)`, where the `(Int, Int)` is the index of the
// MAGIC block, and `Matrix` is the sub-matrix at the given index with size
// MAGIC `rowsPerBlock` x `colsPerBlock`. `BlockMatrix` supports methods such as
// MAGIC `add` and `multiply` with another `BlockMatrix`. `BlockMatrix` also has
// MAGIC a helper function `validate` which can be used to check whether the
// MAGIC `BlockMatrix` is set up properly.
// MAGIC 
// MAGIC A [`BlockMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.BlockMatrix)
// MAGIC can be most easily created from an `IndexedRowMatrix` or
// MAGIC `CoordinateMatrix` by calling `toBlockMatrix`. `toBlockMatrix` creates
// MAGIC blocks of size 1024 x 1024 by default. Users may change the block size
// MAGIC by supplying the values through
// MAGIC `toBlockMatrix(rowsPerBlock, colsPerBlock)`.
// MAGIC 
// MAGIC Refer to the [`BlockMatrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.BlockMatrix)
// MAGIC for details on the API.

// COMMAND ----------

//import org.apache.spark.mllib.linalg.{Matrix, Matrices}
import org.apache.spark.mllib.linalg.distributed.{BlockMatrix, CoordinateMatrix, MatrixEntry}

// COMMAND ----------

val entries: RDD[MatrixEntry] = sc.parallelize(Array(MatrixEntry(0, 0, 1.2), MatrixEntry(1, 0, 2.1), MatrixEntry(6, 1, 3.7))) // an RDD of matrix entries

// COMMAND ----------

// Create a CoordinateMatrix from an RDD[MatrixEntry].
val coordMat: CoordinateMatrix = new CoordinateMatrix(entries)

// COMMAND ----------

// Transform the CoordinateMatrix to a BlockMatrix
val matA: BlockMatrix = coordMat.toBlockMatrix().cache()

// COMMAND ----------

// Validate whether the BlockMatrix is set up properly. Throws an Exception when it is not valid.
// Nothing happens if it is valid.
matA.validate()

// COMMAND ----------

// Calculate A^T A.
val ata = matA.transpose.multiply(matA)

// COMMAND ----------

ata.blocks.collect()

// COMMAND ----------

ata.toLocalMatrix()

// COMMAND ----------

// MAGIC %md
// MAGIC ### BlockMatrix in Scala
// MAGIC A [`BlockMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.BlockMatrix)
// MAGIC can be created from an `RDD` of sub-matrix blocks, where a sub-matrix
// MAGIC block is a `((blockRowIndex, blockColIndex), sub-matrix)` tuple.
// MAGIC 
// MAGIC Refer to the [`BlockMatrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.BlockMatrix)
// MAGIC for more details on the API.

// COMMAND ----------

// MAGIC %py
// MAGIC from pyspark.mllib.linalg import Matrices
// MAGIC from pyspark.mllib.linalg.distributed import BlockMatrix
// MAGIC 
// MAGIC # Create an RDD of sub-matrix blocks.
// MAGIC blocks = sc.parallelize([((0, 0), Matrices.dense(3, 2, [1, 2, 3, 4, 5, 6])),
// MAGIC                          ((1, 0), Matrices.dense(3, 2, [7, 8, 9, 10, 11, 12]))])
// MAGIC 
// MAGIC # Create a BlockMatrix from an RDD of sub-matrix blocks.
// MAGIC mat = BlockMatrix(blocks, 3, 2)
// MAGIC 
// MAGIC # Get its size.
// MAGIC m = mat.numRows() # 6
// MAGIC n = mat.numCols() # 2
// MAGIC print (m,n)
// MAGIC 
// MAGIC # Get the blocks as an RDD of sub-matrix blocks.
// MAGIC blocksRDD = mat.blocks
// MAGIC 
// MAGIC # Convert to a LocalMatrix.
// MAGIC localMat = mat.toLocalMatrix()
// MAGIC 
// MAGIC # Convert to an IndexedRowMatrix.
// MAGIC indexedRowMat = mat.toIndexedRowMatrix()
// MAGIC 
// MAGIC # Convert to a CoordinateMatrix.
// MAGIC coordinateMat = mat.toCoordinateMatrix()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)