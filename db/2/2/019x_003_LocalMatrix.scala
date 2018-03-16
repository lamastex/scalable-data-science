// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/1NICbbECaC0/0.jpg)](https://www.youtube.com/embed/1NICbbECaC0?start=2285&end=2880&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [Apache Spark mllib-progamming-guide on mllib-data-types](http://spark.apache.org/docs/latest/mllib-data-types.html).
// MAGIC 
// MAGIC # [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/000_MLlibProgGuide)
// MAGIC 
// MAGIC ## [Data Types - MLlib Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)
// MAGIC 
// MAGIC -   [Local vector](http://spark.apache.org/docs/latest/mllib-data-types.html#local-vector)
// MAGIC -   [Labeled point](http://spark.apache.org/docs/latest/mllib-data-types.html#labeled-point)
// MAGIC -   [Local matrix](http://spark.apache.org/docs/latest/mllib-data-types.html#local-matrix)
// MAGIC -   [Distributed matrix](http://spark.apache.org/docs/latest/mllib-data-types.html#distributed-matrix)
// MAGIC     -   [RowMatrix](http://spark.apache.org/docs/latest/mllib-data-types.html#rowmatrix)
// MAGIC     -   [IndexedRowMatrix](http://spark.apache.org/docs/latest/mllib-data-types.html#indexedrowmatrix)
// MAGIC     -   [CoordinateMatrix](http://spark.apache.org/docs/latest/mllib-data-types.html#coordinatematrix)
// MAGIC     -   [BlockMatrix](http://spark.apache.org/docs/latest/mllib-data-types.html#blockmatrix)
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
// MAGIC Local Matrix in Scala
// MAGIC ------------
// MAGIC 
// MAGIC A local matrix has integer-typed row and column indices and double-typed
// MAGIC values, **stored on a single machine**. MLlib supports:
// MAGIC 
// MAGIC * dense matrices, whose entry values are stored in a single double array in column-major order, and 
// MAGIC * sparse matrices, whose non-zero entry values are stored in the Compressed Sparse Column (CSC) format in column-major order. 
// MAGIC 
// MAGIC For example, the following dense matrix:
// MAGIC \\[ \begin{pmatrix} 1.0 & 2.0 \\\ 3.0 & 4.0 \\\ 5.0 & 6.0 \end{pmatrix} \\]
// MAGIC is stored in a one-dimensional array `[1.0, 3.0, 5.0, 2.0, 4.0, 6.0]`
// MAGIC with the matrix size `(3, 2)`.
// MAGIC 
// MAGIC The base class of local matrices is
// MAGIC [`Matrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Matrix),
// MAGIC and we provide two implementations:
// MAGIC [`DenseMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.DenseMatrix),
// MAGIC and
// MAGIC [`SparseMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.SparseMatrix).
// MAGIC We recommend using the factory methods implemented in
// MAGIC [`Matrices`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Matrices$)
// MAGIC to create local matrices. Remember, local matrices in MLlib are stored
// MAGIC in column-major order.
// MAGIC 
// MAGIC Refer to the [`Matrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Matrix)
// MAGIC and [`Matrices` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Matrices)
// MAGIC for details on the API.

// COMMAND ----------

Int.MaxValue // note the largest value an index can take

// COMMAND ----------

import org.apache.spark.mllib.linalg.{Matrix, Matrices}

// Create a dense matrix ((1.0, 2.0), (3.0, 4.0), (5.0, 6.0))
val dm: Matrix = Matrices.dense(3, 2, Array(1.0, 3.0, 5.0, 2.0, 4.0, 6.0))

// COMMAND ----------

// MAGIC %md
// MAGIC Next, let us create the following sparse local matrix:
// MAGIC \\[ \begin{pmatrix} 9.0 & 0.0 \\\ 0.0 & 8.0 \\\ 0.0 & 6.0 \end{pmatrix} \\]

// COMMAND ----------

// Create a sparse matrix ((9.0, 0.0), (0.0, 8.0), (0.0, 6.0))
val sm: Matrix = Matrices.sparse(3, 2, Array(0, 1, 3), Array(0, 2, 1), Array(9, 6, 8))

// COMMAND ----------

// MAGIC %md
// MAGIC Local Matrix in Python
// MAGIC ------------
// MAGIC The base class of local matrices is
// MAGIC [`Matrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Matrix),
// MAGIC and we provide two implementations:
// MAGIC [`DenseMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.DenseMatrix),
// MAGIC and
// MAGIC [`SparseMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.SparseMatrix).
// MAGIC We recommend using the factory methods implemented in
// MAGIC [`Matrices`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Matrices)
// MAGIC to create local matrices. Remember, local matrices in MLlib are stored
// MAGIC in column-major order.
// MAGIC 
// MAGIC Refer to the [`Matrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Matrix)
// MAGIC and [`Matrices` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Matrices)
// MAGIC for more details on the API.

// COMMAND ----------

// MAGIC %py
// MAGIC from pyspark.mllib.linalg import Matrix, Matrices
// MAGIC 
// MAGIC # Create a dense matrix ((1.0, 2.0), (3.0, 4.0), (5.0, 6.0))
// MAGIC dm2 = Matrices.dense(3, 2, [1, 2, 3, 4, 5, 6])
// MAGIC dm2

// COMMAND ----------

// MAGIC %py
// MAGIC # Create a sparse matrix ((9.0, 0.0), (0.0, 8.0), (0.0, 6.0))
// MAGIC sm = Matrices.sparse(3, 2, [0, 1, 3], [0, 2, 1], [9, 6, 8])
// MAGIC sm

// COMMAND ----------

