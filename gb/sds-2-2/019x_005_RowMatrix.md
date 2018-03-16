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

### RowMatrix in Scala

A `RowMatrix` is a row-oriented distributed matrix without meaningful
row indices, backed by an RDD of its rows, where each row is a local
vector. Since each row is represented by a local vector, **the number of
columns is limited by the integer range but it should be much smaller in
practice**.

A [`RowMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.RowMatrix)
can be created from an `RDD[Vector]` instance. Then we can compute its
column summary statistics and decompositions.

-   [QR decomposition](https://en.wikipedia.org/wiki/QR_decomposition) is of the form A = QR where Q is an orthogonal matrix and R is an upper triangular matrix.
-   For [singular value decomposition (SVD)](https://en.wikipedia.org/wiki/Singular_value_decomposition) and [principal component analysis (PCA)](https://en.wikipedia.org/wiki/Principal_component_analysis), please refer to [Dimensionality reduction](http://spark.apache.org/docs/latest/mllib-dimensionality-reduction.html).

Refer to the [`RowMatrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.RowMatrix)
for details on the API.

``` scala
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.RowMatrix
```

>     import org.apache.spark.mllib.linalg.{Vector, Vectors}
>     import org.apache.spark.mllib.linalg.distributed.RowMatrix

``` scala
val rows: RDD[Vector] = sc.parallelize(Array(Vectors.dense(12.0, -51.0, 4.0), Vectors.dense(6.0, 167.0, -68.0), Vectors.dense(-4.0, 24.0, -41.0))) // an RDD of local vectors
```

>     rows: org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.Vector] = ParallelCollectionRDD[18] at parallelize at <console>:36

``` scala
// Create a RowMatrix from an RDD[Vector].
val mat: RowMatrix = new RowMatrix(rows)
```

>     mat: org.apache.spark.mllib.linalg.distributed.RowMatrix = org.apache.spark.mllib.linalg.distributed.RowMatrix@720029a1

``` scala
mat.rows.collect
```

>     res0: Array[org.apache.spark.mllib.linalg.Vector] = Array([12.0,-51.0,4.0], [6.0,167.0,-68.0], [-4.0,24.0,-41.0])

``` scala
// Get its size.
val m = mat.numRows()
val n = mat.numCols()
```

>     m: Long = 3
>     n: Long = 3

``` scala
// QR decomposition
val qrResult = mat.tallSkinnyQR(true)
```

>     qrResult: org.apache.spark.mllib.linalg.QRDecomposition[org.apache.spark.mllib.linalg.distributed.RowMatrix,org.apache.spark.mllib.linalg.Matrix] = 
>     QRDecomposition(org.apache.spark.mllib.linalg.distributed.RowMatrix@299d426,14.0  21.0                 -14.0                
>     0.0   -174.99999999999997  70.00000000000001    
>     0.0   0.0                  -35.000000000000014  )

``` scala
qrResult.R
```

>     res1: org.apache.spark.mllib.linalg.Matrix = 
>     14.0  21.0                 -14.0                
>     0.0   -174.99999999999997  70.00000000000001    
>     0.0   0.0                  -35.000000000000014  

------------------------------------------------------------------------

------------------------------------------------------------------------

### RowMatrix in Python

A [`RowMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.RowMatrix)
can be created from an `RDD` of vectors.

Refer to the [`RowMatrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.RowMatrix)
for more details on the API.

``` python
from pyspark.mllib.linalg.distributed import RowMatrix

# Create an RDD of vectors.
rows = sc.parallelize([[1, 2, 3], [4, 5, 6], [7, 8, 9], [10, 11, 12]])

# Create a RowMatrix from an RDD of vectors.
mat = RowMatrix(rows)

# Get its size.
m = mat.numRows()  # 4
n = mat.numCols()  # 3
print m,'x',n

# Get the rows as an RDD of vectors again.
rowsRDD = mat.rows
```

>     4 x 3