[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Archived YouTube video of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/1NICbbECaC0/0.jpg)](https://www.youtube.com/embed/1NICbbECaC0?start=2285&end=2880&autoplay=1)

This is an elaboration of the [Apache Spark mllib-progamming-guide on mllib-data-types](http://spark.apache.org/docs/latest/mllib-data-types.html).

[Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/000_MLlibProgGuide)
==================================================================================================================

[Data Types - MLlib Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)
------------------------------------------------------------------------------------------------------------------------------------------------------------

-   [Local vector](http://spark.apache.org/docs/latest/mllib-data-types.html#local-vector)
-   [Labeled point](http://spark.apache.org/docs/latest/mllib-data-types.html#labeled-point)
-   [Local matrix](http://spark.apache.org/docs/latest/mllib-data-types.html#local-matrix)
-   [Distributed matrix](http://spark.apache.org/docs/latest/mllib-data-types.html#distributed-matrix)
    -   [RowMatrix](http://spark.apache.org/docs/latest/mllib-data-types.html#rowmatrix)
    -   [IndexedRowMatrix](http://spark.apache.org/docs/latest/mllib-data-types.html#indexedrowmatrix)
    -   [CoordinateMatrix](http://spark.apache.org/docs/latest/mllib-data-types.html#coordinatematrix)
    -   [BlockMatrix](http://spark.apache.org/docs/latest/mllib-data-types.html#blockmatrix)

MLlib supports local vectors and matrices stored on a single machine, as
well as distributed matrices backed by one or more RDDs. Local vectors
and local matrices are simple data models that serve as public
interfaces. The underlying linear algebra operations are provided by
[Breeze](http://www.scalanlp.org/) and [jblas](http://jblas.org/). A
training example used in supervised learning is called a “labeled point”
in MLlib.

Labeled point in Scala
----------------------

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

``` scala
//import first
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
```

>     import org.apache.spark.mllib.linalg.Vectors
>     import org.apache.spark.mllib.regression.LabeledPoint

``` scala
// Create a labeled point with a "positive" label and a dense feature vector.
val pos = LabeledPoint(1.0, Vectors.dense(1.0, 0.0, 3.0))
```

>     pos: org.apache.spark.mllib.regression.LabeledPoint = (1.0,[1.0,0.0,3.0])

``` scala
// Create a labeled point with a "negative" label and a sparse feature vector.
val neg = LabeledPoint(0.0, Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0)))
```

>     neg: org.apache.spark.mllib.regression.LabeledPoint = (0.0,(3,[0,2],[1.0,3.0]))

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

``` scala
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

//val examples: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt") // from prog guide but no such data here - can wget from github 
```

>     import org.apache.spark.mllib.regression.LabeledPoint
>     import org.apache.spark.mllib.util.MLUtils
>     import org.apache.spark.rdd.RDD

Load MNIST training and test datasets
-------------------------------------

Our datasets are vectors of pixels representing images of handwritten digits. For example:

![Image of a digit](http://training.databricks.com/databricks_guide/digit.png)
![Image of all 10 digits](http://training.databricks.com/databricks_guide/MNIST-small.png)

``` scala
display(dbutils.fs.ls("/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt"))
```

| path                                                                   | name                   | size        |
|------------------------------------------------------------------------|------------------------|-------------|
| dbfs:/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt | mnist-digits-train.txt | 6.9430283e7 |

``` scala
val examples: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc, "/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")
```

>     examples: org.apache.spark.rdd.RDD[org.apache.spark.mllib.regression.LabeledPoint] = MapPartitionsRDD[3512] at map at MLUtils.scala:84

``` scala
examples.take(1)
```

>     res6: Array[org.apache.spark.mllib.regression.LabeledPoint] = Array((5.0,(780,[152,153,154,155,156,157,158,159,160,161,162,163,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,231,232,233,234,235,236,237,238,239,240,241,260,261,262,263,264,265,266,268,269,289,290,291,292,293,319,320,321,322,347,348,349,350,376,377,378,379,380,381,405,406,407,408,409,410,434,435,436,437,438,439,463,464,465,466,467,493,494,495,496,518,519,520,521,522,523,524,544,545,546,547,548,549,550,551,570,571,572,573,574,575,576,577,578,596,597,598,599,600,601,602,603,604,605,622,623,624,625,626,627,628,629,630,631,648,649,650,651,652,653,654,655,656,657,676,677,678,679,680,681,682,683],[3.0,18.0,18.0,18.0,126.0,136.0,175.0,26.0,166.0,255.0,247.0,127.0,30.0,36.0,94.0,154.0,170.0,253.0,253.0,253.0,253.0,253.0,225.0,172.0,253.0,242.0,195.0,64.0,49.0,238.0,253.0,253.0,253.0,253.0,253.0,253.0,253.0,253.0,251.0,93.0,82.0,82.0,56.0,39.0,18.0,219.0,253.0,253.0,253.0,253.0,253.0,198.0,182.0,247.0,241.0,80.0,156.0,107.0,253.0,253.0,205.0,11.0,43.0,154.0,14.0,1.0,154.0,253.0,90.0,139.0,253.0,190.0,2.0,11.0,190.0,253.0,70.0,35.0,241.0,225.0,160.0,108.0,1.0,81.0,240.0,253.0,253.0,119.0,25.0,45.0,186.0,253.0,253.0,150.0,27.0,16.0,93.0,252.0,253.0,187.0,249.0,253.0,249.0,64.0,46.0,130.0,183.0,253.0,253.0,207.0,2.0,39.0,148.0,229.0,253.0,253.0,253.0,250.0,182.0,24.0,114.0,221.0,253.0,253.0,253.0,253.0,201.0,78.0,23.0,66.0,213.0,253.0,253.0,253.0,253.0,198.0,81.0,2.0,18.0,171.0,219.0,253.0,253.0,253.0,253.0,195.0,80.0,9.0,55.0,172.0,226.0,253.0,253.0,253.0,253.0,244.0,133.0,11.0,136.0,253.0,253.0,253.0,212.0,135.0,132.0,16.0])))

Display our data. Each image has the true label (the `label` column) and a vector of `features` which represent pixel intensities (see below for details of what is in `training`).

``` scala
display(examples.toDF) // covert to DataFrame and display for convenient db visualization
```

The pixel intensities are represented in `features` as a sparse vector, for example the first observation, as seen in row 1 of the output to `display(training)` below, has `label` as `5`, i.e. the hand-written image is for the number 5. And this hand-written image is the following sparse vector (just click the triangle to the left of the feature in first row to see the following):

    type: 0
    size: 780
    indices: [152,153,155,...,682,683]
    values: [3, 18, 18,18,126,...,132,16]

Here

-   `type: 0` says we hve a sparse vector.
-   `size: 780` says the vector has 780 indices in total
-   these indices from 0,...,779 are a unidimensional indexing of the two-dimensional array of pixels in the image
-   `indices: [152,153,155,...,682,683]` are the indices from the `[0,1,...,779]` possible indices with non-zero values
    -   a value is an integer encoding the gray-level at the pixel index
-   `values: [3, 18, 18,18,126,...,132,16]` are the actual gray level values, for example:
    -   at pixed index `152` the gray-level value is `3`,
    -   at index `153` the gray-level value is `18`,
    -   ..., and finally at
    -   at index `683` the gray-level value is `18`

We could also use the following method as done in notebook `016_*` already.

``` scala
val training = spark.read.format("libsvm")
                    .option("numFeatures", "780")
                    .load("/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")
```

>     training: org.apache.spark.sql.DataFrame = [label: double, features: vector]

``` scala
display(training)
```

------------------------------------------------------------------------

------------------------------------------------------------------------

Labeled point in Python
-----------------------

A labeled point is represented by
[`LabeledPoint`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.regression.LabeledPoint).

Refer to the [`LabeledPoint` Python
docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.regression.LabeledPoint)
for more details on the API.

``` python
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

``` python
from pyspark.mllib.util import MLUtils

# examples = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt") #from prog guide but no such data here - can wget from github 
examples = MLUtils.loadLibSVMFile(sc, "/databricks-datasets/mnist-digits/data-001/mnist-digits-train.txt")
```

``` scala
examples.take(1)
```

>     res4: Array[org.apache.spark.mllib.regression.LabeledPoint] = Array((5.0,(780,[152,153,154,155,156,157,158,159,160,161,162,163,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,231,232,233,234,235,236,237,238,239,240,241,260,261,262,263,264,265,266,268,269,289,290,291,292,293,319,320,321,322,347,348,349,350,376,377,378,379,380,381,405,406,407,408,409,410,434,435,436,437,438,439,463,464,465,466,467,493,494,495,496,518,519,520,521,522,523,524,544,545,546,547,548,549,550,551,570,571,572,573,574,575,576,577,578,596,597,598,599,600,601,602,603,604,605,622,623,624,625,626,627,628,629,630,631,648,649,650,651,652,653,654,655,656,657,676,677,678,679,680,681,682,683],[3.0,18.0,18.0,18.0,126.0,136.0,175.0,26.0,166.0,255.0,247.0,127.0,30.0,36.0,94.0,154.0,170.0,253.0,253.0,253.0,253.0,253.0,225.0,172.0,253.0,242.0,195.0,64.0,49.0,238.0,253.0,253.0,253.0,253.0,253.0,253.0,253.0,253.0,251.0,93.0,82.0,82.0,56.0,39.0,18.0,219.0,253.0,253.0,253.0,253.0,253.0,198.0,182.0,247.0,241.0,80.0,156.0,107.0,253.0,253.0,205.0,11.0,43.0,154.0,14.0,1.0,154.0,253.0,90.0,139.0,253.0,190.0,2.0,11.0,190.0,253.0,70.0,35.0,241.0,225.0,160.0,108.0,1.0,81.0,240.0,253.0,253.0,119.0,25.0,45.0,186.0,253.0,253.0,150.0,27.0,16.0,93.0,252.0,253.0,187.0,249.0,253.0,249.0,64.0,46.0,130.0,183.0,253.0,253.0,207.0,2.0,39.0,148.0,229.0,253.0,253.0,253.0,250.0,182.0,24.0,114.0,221.0,253.0,253.0,253.0,253.0,201.0,78.0,23.0,66.0,213.0,253.0,253.0,253.0,253.0,198.0,81.0,2.0,18.0,171.0,219.0,253.0,253.0,253.0,253.0,195.0,80.0,9.0,55.0,172.0,226.0,253.0,253.0,253.0,253.0,244.0,133.0,11.0,136.0,253.0,253.0,253.0,212.0,135.0,132.0,16.0])))