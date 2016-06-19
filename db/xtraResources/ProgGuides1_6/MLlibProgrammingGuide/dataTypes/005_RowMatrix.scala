// Databricks notebook source exported at Sun, 19 Jun 2016 03:04:17 UTC
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
// MAGIC ### RowMatrix in Scala
// MAGIC 
// MAGIC A `RowMatrix` is a row-oriented distributed matrix without meaningful
// MAGIC row indices, backed by an RDD of its rows, where each row is a local
// MAGIC vector. Since each row is represented by a local vector, **the number of
// MAGIC columns is limited by the integer range but it should be much smaller in
// MAGIC practice**.
// MAGIC 
// MAGIC A [`RowMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.RowMatrix)
// MAGIC can be created from an `RDD[Vector]` instance. Then we can compute its
// MAGIC column summary statistics and decompositions. 
// MAGIC 
// MAGIC * [QR decomposition](https://en.wikipedia.org/wiki/QR_decomposition) is of the form A = QR where Q is an orthogonal matrix and R is an upper triangular matrix. 
// MAGIC * For [singular value decomposition (SVD)](https://en.wikipedia.org/wiki/Singular_value_decomposition) and [principal component analysis (PCA)](https://en.wikipedia.org/wiki/Principal_component_analysis), please refer to [Dimensionality reduction](http://spark.apache.org/docs/latest/mllib-dimensionality-reduction.html).
// MAGIC 
// MAGIC Refer to the [`RowMatrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.RowMatrix)
// MAGIC for details on the API.

// COMMAND ----------

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.RowMatrix

// COMMAND ----------

val rows: RDD[Vector] = sc.parallelize(Array(Vectors.dense(12.0, -51.0, 4.0), Vectors.dense(6.0, 167.0, -68.0), Vectors.dense(-4.0, 24.0, -41.0))) // an RDD of local vectors

// COMMAND ----------

// Create a RowMatrix from an RDD[Vector].
val mat: RowMatrix = new RowMatrix(rows)

// COMMAND ----------

mat.rows.collect

// COMMAND ----------

// Get its size.
val m = mat.numRows()
val n = mat.numCols()

// COMMAND ----------

// QR decomposition
val qrResult = mat.tallSkinnyQR(true)

// COMMAND ----------

qrResult.R

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC ### RowMatrix in Python
// MAGIC 
// MAGIC A [`RowMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.RowMatrix)
// MAGIC can be created from an `RDD` of vectors.
// MAGIC 
// MAGIC Refer to the [`RowMatrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.RowMatrix)
// MAGIC for more details on the API.

// COMMAND ----------

// MAGIC %py
// MAGIC from pyspark.mllib.linalg.distributed import RowMatrix
// MAGIC 
// MAGIC # Create an RDD of vectors.
// MAGIC rows = sc.parallelize([[1, 2, 3], [4, 5, 6], [7, 8, 9], [10, 11, 12]])
// MAGIC 
// MAGIC # Create a RowMatrix from an RDD of vectors.
// MAGIC mat = RowMatrix(rows)
// MAGIC 
// MAGIC # Get its size.
// MAGIC m = mat.numRows()  # 4
// MAGIC n = mat.numCols()  # 3
// MAGIC print m,'x',n
// MAGIC 
// MAGIC # Get the rows as an RDD of vectors again.
// MAGIC rowsRDD = mat.rows

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