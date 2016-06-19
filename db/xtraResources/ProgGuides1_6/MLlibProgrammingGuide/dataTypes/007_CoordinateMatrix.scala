// Databricks notebook source exported at Sun, 19 Jun 2016 03:05:50 UTC
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
// MAGIC ### CoordinateMatrix in Scala
// MAGIC 
// MAGIC A `CoordinateMatrix` is a distributed matrix backed by an RDD of its
// MAGIC entries. Each entry is a tuple of `(i: Long, j: Long, value: Double)`,
// MAGIC where `i` is the row index, `j` is the column index, and `value` is the
// MAGIC entry value. A `CoordinateMatrix` should be used only when both
// MAGIC dimensions of the matrix are huge and the matrix is very sparse.
// MAGIC 
// MAGIC A [`CoordinateMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.CoordinateMatrix)
// MAGIC can be created from an `RDD[MatrixEntry]` instance, where
// MAGIC [`MatrixEntry`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.MatrixEntry)
// MAGIC is a wrapper over `(Long, Long, Double)`. A `CoordinateMatrix` can be
// MAGIC converted to an `IndexedRowMatrix` with sparse rows by calling
// MAGIC `toIndexedRowMatrix`. Other computations for `CoordinateMatrix` are not
// MAGIC currently supported.
// MAGIC 
// MAGIC Refer to the [`CoordinateMatrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.CoordinateMatrix)
// MAGIC for details on the API.

// COMMAND ----------

import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}

// COMMAND ----------

val entries: RDD[MatrixEntry] = sc.parallelize(Array(MatrixEntry(0, 0, 1.2), MatrixEntry(1, 0, 2.1), MatrixEntry(6, 1, 3.7))) // an RDD of matrix entries

// COMMAND ----------

// Create a CoordinateMatrix from an RDD[MatrixEntry].
val mat: CoordinateMatrix = new CoordinateMatrix(entries)

// COMMAND ----------

// Get its size.
val m = mat.numRows()
val n = mat.numCols()

// COMMAND ----------

// Convert it to an IndexRowMatrix whose rows are sparse vectors.
val indexedRowMatrix = mat.toIndexedRowMatrix()

// COMMAND ----------

indexedRowMatrix.rows.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### CoordinateMatrix in Scala
// MAGIC 
// MAGIC A [`CoordinateMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.CoordinateMatrix)
// MAGIC can be created from an `RDD` of `MatrixEntry` entries, where
// MAGIC [`MatrixEntry`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.MatrixEntry)
// MAGIC is a wrapper over `(long, long, float)`. A `CoordinateMatrix` can be
// MAGIC converted to a `RowMatrix` by calling `toRowMatrix`, or to an
// MAGIC `IndexedRowMatrix` with sparse rows by calling `toIndexedRowMatrix`.
// MAGIC 
// MAGIC Refer to the [`CoordinateMatrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.CoordinateMatrix)
// MAGIC for more details on the API.

// COMMAND ----------

// MAGIC %py
// MAGIC from pyspark.mllib.linalg.distributed import CoordinateMatrix, MatrixEntry
// MAGIC 
// MAGIC # Create an RDD of coordinate entries.
// MAGIC #   - This can be done explicitly with the MatrixEntry class:
// MAGIC entries = sc.parallelize([MatrixEntry(0, 0, 1.2), MatrixEntry(1, 0, 2.1), MatrixEntry(6, 1, 3.7)])
// MAGIC 
// MAGIC #   - or using (long, long, float) tuples:
// MAGIC entries = sc.parallelize([(0, 0, 1.2), (1, 0, 2.1), (2, 1, 3.7)])
// MAGIC 
// MAGIC # Create an CoordinateMatrix from an RDD of MatrixEntries.
// MAGIC mat = CoordinateMatrix(entries)
// MAGIC 
// MAGIC # Get its size.
// MAGIC m = mat.numRows()  # 3
// MAGIC n = mat.numCols()  # 2
// MAGIC print (m,n)
// MAGIC 
// MAGIC # Get the entries as an RDD of MatrixEntries.
// MAGIC entriesRDD = mat.entries
// MAGIC 
// MAGIC # Convert to a RowMatrix.
// MAGIC rowMat = mat.toRowMatrix()
// MAGIC 
// MAGIC # Convert to an IndexedRowMatrix.
// MAGIC indexedRowMat = mat.toIndexedRowMatrix()
// MAGIC 
// MAGIC # Convert to a BlockMatrix.
// MAGIC blockMat = mat.toBlockMatrix()

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