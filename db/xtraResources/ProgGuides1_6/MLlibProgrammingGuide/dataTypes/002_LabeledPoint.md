// Databricks notebook source exported at Sun, 19 Jun 2016 02:59:22 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





This is an elaboration of the [Apache Spark 1.6 mllib-progamming-guide on mllib-data-types](http://spark.apache.org/docs/latest/mllib-data-types.html).

# [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/000_MLlibProgGuide)

## [Data Types - MLlib Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)

-   [Local vector](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/001_LocalVector) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#local-vector)
-   [Labeled point](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/002_LabeledPoint) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#labeled-point)
-   [Local matrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/003_LocalMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#local-matrix)
-   [Distributed matrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#distributed-matrix)
    -   [RowMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/005_RowMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#rowmatrix)
    -   [IndexedRowMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#indexedrowmatrix)
    -   [CoordinateMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#coordinatematrix)
    -   [BlockMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/008_BlockMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#blockmatrix)

MLlib supports local vectors and matrices stored on a single machine, as
well as distributed matrices backed by one or more RDDs. Local vectors
and local matrices are simple data models that serve as public
interfaces. The underlying linear algebra operations are provided by
[Breeze](http://www.scalanlp.org/) and [jblas](http://jblas.org/). A
training example used in supervised learning is called a “labeled point”
in MLlib.





Labeled point in Scala
-------------

A labeled point is a local vector, either dense or sparse, associated
with a label/response. In MLlib, labeled points are used in supervised
learning algorithms. 

We use a double to store a label, so we can use
labeled points in both regression and classification. 

For binary classification, a label should be either `0` (negative) or `1`
(positive). For multiclass classification, labels should be class
indices starting from zero: `0, 1, 2, ...`.

A labeled point is represented by the case class
[`LabeledPoint`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.regression.LabeledPoint).

Refer to the [`LabeledPoint` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.regression.LabeledPoint)
for details on the API.


```scala

//import first
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

```
```scala

// Create a labeled point with a "positive" label and a dense feature vector.
val pos = LabeledPoint(1.0, Vectors.dense(1.0, 0.0, 3.0))

```
```scala

// Create a labeled point with a "negative" label and a sparse feature vector.
val neg = LabeledPoint(0.0, Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0)))

```



***Sparse data in Scala***

It is very common in practice to have sparse training data. MLlib
supports reading training examples stored in `LIBSVM` format, which is
the default format used by 
[`LIBSVM`](http://www.csie.ntu.edu.tw/~cjlin/libsvm/) and
[`LIBLINEAR`](http://www.csie.ntu.edu.tw/~cjlin/liblinear/). It is a
text format in which each line represents a labeled sparse feature
vector using the following format:

    label index1:value1 index2:value2 ...

where the indices are one-based and in ascending order. After loading,
the feature indices are converted to zero-based.

[`MLUtils.loadLibSVMFile`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.util.MLUtils$)
reads training examples stored in LIBSVM format.

Refer to the [`MLUtils` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.util.MLUtils)
for details on the API.


```scala

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

//val examples: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt") // from prog guide but no such data here - can wget from github 

```


 
## Load MNIST training and test datasets

Our datasets are vectors of pixels representing images of handwritten digits.  For example:

![Image of a digit](http://training.databricks.com/databricks_guide/digit.png)
![Image of all 10 digits](http://training.databricks.com/databricks_guide/MNIST-small.png)


```scala

display(dbutils.fs.ls("/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt"))

```
```scala

val examples: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc, "/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")

```
```scala

examples.take(1)

```


 
Display our data.  Each image has the true label (the `label` column) and a vector of `features` which represent pixel intensities (see below for details of what is in `training`).


```scala

display(examples.toDF) // covert to DataFrame and display for convenient db visualization

```



The pixel intensities are represented in `features` as a sparse vector, for example the first observation, as seen in row 1 of the output to `display(training)` below, has `label` as `5`, i.e. the hand-written image is for the number 5.  And this hand-written image is the following sparse vector (just click the triangle to the left of the feature in first row to see the following):
```
type: 0
size: 780
indices: [152,153,155,...,682,683]
values: [3, 18, 18,18,126,...,132,16]
```
Here 
* `type: 0` says we hve a sparse vector.
* `size: 780` says the vector has 780 indices in total 
 * these indices from 0,...,779 are a unidimensional indexing of the two-dimensional array of pixels in the image
* `indices: [152,153,155,...,682,683]` are the indices from the `[0,1,...,779]` possible indices with non-zero values 
  * a value is an integer encoding the gray-level at the pixel index
* `values: [3, 18, 18,18,126,...,132,16]` are the actual gray level values, for example:
  * at pixed index `152` the gray-level value is `3`, 
  * at index `153` the gray-level value is `18`,
  * ..., and finally at
  * at index `683` the gray-level value is `18`





***
***
Labeled point in Python
-------------
A labeled point is represented by
[`LabeledPoint`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.regression.LabeledPoint).

Refer to the [`LabeledPoint` Python
docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.regression.LabeledPoint)
for more details on the API.


```scala

%py
# import first
from pyspark.mllib.linalg import SparseVector
from pyspark.mllib.regression import LabeledPoint

# Create a labeled point with a positive label and a dense feature vector.
pos = LabeledPoint(1.0, [1.0, 0.0, 3.0])

# Create a labeled point with a negative label and a sparse feature vector.
neg = LabeledPoint(0.0, SparseVector(3, [0, 2], [1.0, 3.0]))

```



***Sparse data in Python***


[`MLUtils.loadLibSVMFile`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.util.MLUtils)
reads training examples stored in LIBSVM format.

Refer to the [`MLUtils` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.util.MLUtils)
for more details on the API.


```scala

%py
from pyspark.mllib.util import MLUtils

# examples = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt") #from prog guide but no such data here - can wget from github 
examples = MLUtils.loadLibSVMFile(sc, "/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")

```
```scala

examples.take(1)

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
