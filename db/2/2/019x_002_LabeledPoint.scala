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
// MAGIC Labeled point in Scala
// MAGIC -------------
// MAGIC 
// MAGIC A labeled point is a local vector, either dense or sparse, associated
// MAGIC with a label/response. In MLlib, labeled points are used in supervised
// MAGIC learning algorithms. 
// MAGIC 
// MAGIC We use a double to store a label, so we can use
// MAGIC labeled points in both regression and classification. 
// MAGIC 
// MAGIC For binary classification, a label should be either `0` (negative) or `1`
// MAGIC (positive). For multiclass classification, labels should be class
// MAGIC indices starting from zero: `0, 1, 2, ...`.
// MAGIC 
// MAGIC A labeled point is represented by the case class
// MAGIC [`LabeledPoint`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.regression.LabeledPoint).
// MAGIC 
// MAGIC Refer to the [`LabeledPoint` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.regression.LabeledPoint)
// MAGIC for details on the API.

// COMMAND ----------

//import first
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

// COMMAND ----------

// Create a labeled point with a "positive" label and a dense feature vector.
val pos = LabeledPoint(1.0, Vectors.dense(1.0, 0.0, 3.0))

// COMMAND ----------

// Create a labeled point with a "negative" label and a sparse feature vector.
val neg = LabeledPoint(0.0, Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0)))

// COMMAND ----------

// MAGIC %md
// MAGIC ***Sparse data in Scala***
// MAGIC 
// MAGIC It is very common in practice to have sparse training data. MLlib
// MAGIC supports reading training examples stored in `LIBSVM` format, which is
// MAGIC the default format used by 
// MAGIC [`LIBSVM`](http://www.csie.ntu.edu.tw/~cjlin/libsvm/) and
// MAGIC [`LIBLINEAR`](http://www.csie.ntu.edu.tw/~cjlin/liblinear/). It is a
// MAGIC text format in which each line represents a labeled sparse feature
// MAGIC vector using the following format:
// MAGIC 
// MAGIC     label index1:value1 index2:value2 ...
// MAGIC 
// MAGIC where the indices are one-based and in ascending order. After loading,
// MAGIC the feature indices are converted to zero-based.
// MAGIC 
// MAGIC [`MLUtils.loadLibSVMFile`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.util.MLUtils$)
// MAGIC reads training examples stored in LIBSVM format.
// MAGIC 
// MAGIC Refer to the [`MLUtils` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.util.MLUtils)
// MAGIC for details on the API.

// COMMAND ----------

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

//val examples: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt") // from prog guide but no such data here - can wget from github 

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Load MNIST training and test datasets
// MAGIC 
// MAGIC Our datasets are vectors of pixels representing images of handwritten digits.  For example:
// MAGIC 
// MAGIC ![Image of a digit](http://training.databricks.com/databricks_guide/digit.png)
// MAGIC ![Image of all 10 digits](http://training.databricks.com/databricks_guide/MNIST-small.png)

// COMMAND ----------

display(dbutils.fs.ls("/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt"))

// COMMAND ----------

val examples: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc, "/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")

// COMMAND ----------

examples.take(1)

// COMMAND ----------

// MAGIC %md 
// MAGIC Display our data.  Each image has the true label (the `label` column) and a vector of `features` which represent pixel intensities (see below for details of what is in `training`).

// COMMAND ----------

display(examples.toDF) // covert to DataFrame and display for convenient db visualization

// COMMAND ----------

// MAGIC %md
// MAGIC The pixel intensities are represented in `features` as a sparse vector, for example the first observation, as seen in row 1 of the output to `display(training)` below, has `label` as `5`, i.e. the hand-written image is for the number 5.  And this hand-written image is the following sparse vector (just click the triangle to the left of the feature in first row to see the following):
// MAGIC 
// MAGIC ```
// MAGIC type: 0
// MAGIC size: 780
// MAGIC indices: [152,153,155,...,682,683]
// MAGIC values: [3, 18, 18,18,126,...,132,16]
// MAGIC ```
// MAGIC Here 
// MAGIC 
// MAGIC * `type: 0` says we hve a sparse vector.
// MAGIC * `size: 780` says the vector has 780 indices in total 
// MAGIC  * these indices from 0,...,779 are a unidimensional indexing of the two-dimensional array of pixels in the image
// MAGIC * `indices: [152,153,155,...,682,683]` are the indices from the `[0,1,...,779]` possible indices with non-zero values 
// MAGIC   * a value is an integer encoding the gray-level at the pixel index
// MAGIC * `values: [3, 18, 18,18,126,...,132,16]` are the actual gray level values, for example:
// MAGIC   * at pixed index `152` the gray-level value is `3`, 
// MAGIC   * at index `153` the gray-level value is `18`,
// MAGIC   * ..., and finally at
// MAGIC   * at index `683` the gray-level value is `18`

// COMMAND ----------

// MAGIC %md
// MAGIC We could also use the following method as done in notebook `016_*` already.

// COMMAND ----------

val training = spark.read.format("libsvm")
                    .option("numFeatures", "780")
                    .load("/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")

// COMMAND ----------

display(training)

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC Labeled point in Python
// MAGIC -------------
// MAGIC A labeled point is represented by
// MAGIC [`LabeledPoint`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.regression.LabeledPoint).
// MAGIC 
// MAGIC Refer to the [`LabeledPoint` Python
// MAGIC docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.regression.LabeledPoint)
// MAGIC for more details on the API.

// COMMAND ----------

// MAGIC %py
// MAGIC # import first
// MAGIC from pyspark.mllib.linalg import SparseVector
// MAGIC from pyspark.mllib.regression import LabeledPoint
// MAGIC 
// MAGIC # Create a labeled point with a positive label and a dense feature vector.
// MAGIC pos = LabeledPoint(1.0, [1.0, 0.0, 3.0])
// MAGIC 
// MAGIC # Create a labeled point with a negative label and a sparse feature vector.
// MAGIC neg = LabeledPoint(0.0, SparseVector(3, [0, 2], [1.0, 3.0]))

// COMMAND ----------

// MAGIC %md
// MAGIC ***Sparse data in Python***
// MAGIC 
// MAGIC 
// MAGIC [`MLUtils.loadLibSVMFile`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.util.MLUtils)
// MAGIC reads training examples stored in LIBSVM format.
// MAGIC 
// MAGIC Refer to the [`MLUtils` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.util.MLUtils)
// MAGIC for more details on the API.

// COMMAND ----------

// MAGIC %py
// MAGIC from pyspark.mllib.util import MLUtils
// MAGIC 
// MAGIC # examples = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt") #from prog guide but no such data here - can wget from github 
// MAGIC examples = MLUtils.loadLibSVMFile(sc, "/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")

// COMMAND ----------

examples.take(1)

// COMMAND ----------

