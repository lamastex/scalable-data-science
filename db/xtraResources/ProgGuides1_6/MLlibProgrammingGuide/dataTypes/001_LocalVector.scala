// Databricks notebook source exported at Sun, 19 Jun 2016 03:00:13 UTC
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
// MAGIC Local vector in Scala
// MAGIC ------------
// MAGIC 
// MAGIC A local vector has integer-typed and 0-based indices and double-typed
// MAGIC values, stored on a single machine. 
// MAGIC 
// MAGIC MLlib supports two types of local vectors: 
// MAGIC * dense and 
// MAGIC * sparse. 
// MAGIC 
// MAGIC A dense vector is backed by a double array
// MAGIC representing its entry values, while a sparse vector is backed by two
// MAGIC parallel arrays: indices and values. 
// MAGIC 
// MAGIC For example, a vector
// MAGIC `(1.0, 0.0, 3.0)` can be represented:
// MAGIC * in dense format as `[1.0, 0.0, 3.0]` or 
// MAGIC * in sparse format as `(3, [0, 2], [1.0, 3.0])`, where `3` is the size of the vector.
// MAGIC 
// MAGIC The base class of local vectors is [`Vector`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vector), 
// MAGIC and we provide two implementations: [`DenseVector`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.DenseVector)
// MAGIC and [`SparseVector`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.SparseVector). 
// MAGIC We recommend using the factory methods implemented in
// MAGIC [`Vectors`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vectors$)
// MAGIC to create local vectors. 
// MAGIC Refer to the [`Vector` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vector)
// MAGIC and [`Vectors` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vectors$)
// MAGIC for details on the API.

// COMMAND ----------

import org.apache.spark.mllib.linalg.{Vector, Vectors}

// Create a dense vector (1.0, 0.0, 3.0).
val dv: Vector = Vectors.dense(1.0, 0.0, 3.0)

// COMMAND ----------

// Create a sparse vector (1.0, 0.0, 3.0) by specifying its indices and values corresponding to nonzero entries.
val sv1: Vector = Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0))

// COMMAND ----------

// Create a sparse vector (1.0, 0.0, 3.0) by specifying its nonzero entries.
val sv2: Vector = Vectors.sparse(3, Seq((0, 1.0), (2, 3.0)))

// COMMAND ----------

// MAGIC %md
// MAGIC ***Note:*** Scala imports `scala.collection.immutable.Vector` by
// MAGIC default, so you have to import `org.apache.spark.mllib.linalg.Vector`
// MAGIC explicitly to use MLlib’s `Vector`.

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC Local Vector in Python
// MAGIC ------

// COMMAND ----------

// MAGIC %md
// MAGIC **python**: MLlib recognizes the following types as dense vectors:
// MAGIC 
// MAGIC -   NumPy’s
// MAGIC     [`array`](http://docs.scipy.org/doc/numpy/reference/generated/numpy.array.html)
// MAGIC -   Python’s list, e.g., `[1, 2, 3]`
// MAGIC 
// MAGIC and the following as sparse vectors:
// MAGIC 
// MAGIC -   MLlib’s
// MAGIC     [`SparseVector`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.SparseVector).
// MAGIC -   SciPy’s
// MAGIC     [`csc_matrix`](http://docs.scipy.org/doc/scipy/reference/generated/scipy.sparse.csc_matrix.html#scipy.sparse.csc_matrix)
// MAGIC     with a single column
// MAGIC 
// MAGIC We recommend using NumPy arrays over lists for efficiency, and using the
// MAGIC factory methods implemented in
// MAGIC [`Vectors`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Vectors)
// MAGIC to create sparse vectors.
// MAGIC 
// MAGIC Refer to the [`Vectors` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Vectors)
// MAGIC for more details on the API.

// COMMAND ----------

// MAGIC %py
// MAGIC import numpy as np
// MAGIC import scipy.sparse as sps
// MAGIC from pyspark.mllib.linalg import Vectors
// MAGIC 
// MAGIC # Use a NumPy array as a dense vector.
// MAGIC dv1 = np.array([1.0, 0.0, 3.0])
// MAGIC # Use a Python list as a dense vector.
// MAGIC dv2 = [1.0, 0.0, 3.0]
// MAGIC # Create a SparseVector.
// MAGIC sv1 = Vectors.sparse(3, [0, 2], [1.0, 3.0])
// MAGIC # Use a single-column SciPy csc_matrix as a sparse vector.
// MAGIC sv2 = sps.csc_matrix((np.array([1.0, 3.0]), np.array([0, 2]), np.array([0, 2])), shape = (3, 1))

// COMMAND ----------

// MAGIC %py
// MAGIC print dv1
// MAGIC print dv2
// MAGIC print sv1
// MAGIC print sv2

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