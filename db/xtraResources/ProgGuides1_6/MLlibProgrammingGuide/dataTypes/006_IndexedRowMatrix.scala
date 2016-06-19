// Databricks notebook source exported at Sun, 19 Jun 2016 03:05:13 UTC
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
// MAGIC ### IndexedRowMatrix in Scala
// MAGIC 
// MAGIC An `IndexedRowMatrix` is similar to a `RowMatrix` but with meaningful
// MAGIC row indices. It is backed by an RDD of indexed rows, so that each row is
// MAGIC represented by its index (long-typed) and a local vector.
// MAGIC 
// MAGIC An [`IndexedRowMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix)
// MAGIC can be created from an `RDD[IndexedRow]` instance, where
// MAGIC [`IndexedRow`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.IndexedRow)
// MAGIC is a wrapper over `(Long, Vector)`. An `IndexedRowMatrix` can be
// MAGIC converted to a `RowMatrix` by dropping its row indices.
// MAGIC 
// MAGIC Refer to the [`IndexedRowMatrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix)
// MAGIC for details on the API.

// COMMAND ----------

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix, RowMatrix}

// COMMAND ----------

Vector(12.0, -51.0, 4.0) // note Vector is a scala collection

// COMMAND ----------

Vectors.dense(12.0, -51.0, 4.0) // while this is a mllib.linalg.Vector

// COMMAND ----------

val rows: RDD[IndexedRow] = sc.parallelize(Array(IndexedRow(2, Vectors.dense(1,3)), IndexedRow(4, Vectors.dense(4,5)))) // an RDD of indexed rows

// COMMAND ----------

// Create an IndexedRowMatrix from an RDD[IndexedRow].
val mat: IndexedRowMatrix = new IndexedRowMatrix(rows)

// COMMAND ----------

// Get its size.
val m = mat.numRows()
val n = mat.numCols()

// COMMAND ----------

// Drop its row indices.
val rowMat: RowMatrix = mat.toRowMatrix()

// COMMAND ----------

rowMat.rows.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### IndexedRowMatrix in Python
// MAGIC 
// MAGIC An [`IndexedRowMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.IndexedRowMatrix)
// MAGIC can be created from an `RDD` of `IndexedRow`s, where
// MAGIC [`IndexedRow`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.IndexedRow)
// MAGIC is a wrapper over `(long, vector)`. An `IndexedRowMatrix` can be
// MAGIC converted to a `RowMatrix` by dropping its row indices.
// MAGIC 
// MAGIC Refer to the [`IndexedRowMatrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.IndexedRowMatrix)
// MAGIC for more details on the API.

// COMMAND ----------

// MAGIC %py
// MAGIC from pyspark.mllib.linalg.distributed import IndexedRow, IndexedRowMatrix
// MAGIC 
// MAGIC # Create an RDD of indexed rows.
// MAGIC #   - This can be done explicitly with the IndexedRow class:
// MAGIC indexedRows = sc.parallelize([IndexedRow(0, [1, 2, 3]),
// MAGIC                               IndexedRow(1, [4, 5, 6]),
// MAGIC                               IndexedRow(2, [7, 8, 9]),
// MAGIC                               IndexedRow(3, [10, 11, 12])])
// MAGIC 
// MAGIC #   - or by using (long, vector) tuples:
// MAGIC indexedRows = sc.parallelize([(0, [1, 2, 3]), (1, [4, 5, 6]),
// MAGIC                               (2, [7, 8, 9]), (3, [10, 11, 12])])
// MAGIC 
// MAGIC # Create an IndexedRowMatrix from an RDD of IndexedRows.
// MAGIC mat = IndexedRowMatrix(indexedRows)
// MAGIC 
// MAGIC # Get its size.
// MAGIC m = mat.numRows()  # 4
// MAGIC n = mat.numCols()  # 3
// MAGIC print (m,n)
// MAGIC 
// MAGIC # Get the rows as an RDD of IndexedRows.
// MAGIC rowsRDD = mat.rows
// MAGIC 
// MAGIC # Convert to a RowMatrix by dropping the row indices.
// MAGIC rowMat = mat.toRowMatrix()
// MAGIC 
// MAGIC # Convert to a CoordinateMatrix.
// MAGIC coordinateMat = mat.toCoordinateMatrix()
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