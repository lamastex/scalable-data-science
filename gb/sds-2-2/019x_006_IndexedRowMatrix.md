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

### IndexedRowMatrix in Scala

An `IndexedRowMatrix` is similar to a `RowMatrix` but with meaningful
row indices. It is backed by an RDD of indexed rows, so that each row is
represented by its index (long-typed) and a local vector.

An [`IndexedRowMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix)
can be created from an `RDD[IndexedRow]` instance, where
[`IndexedRow`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.IndexedRow)
is a wrapper over `(Long, Vector)`. An `IndexedRowMatrix` can be
converted to a `RowMatrix` by dropping its row indices.

Refer to the [`IndexedRowMatrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix)
for details on the API.

``` scala
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix, RowMatrix}
```

>     import org.apache.spark.mllib.linalg.{Vector, Vectors}
>     import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix, RowMatrix}

``` scala
Vector(12.0, -51.0, 4.0) // note Vector is a scala collection
```

>     res8: scala.collection.immutable.Vector[Double] = Vector(12.0, -51.0, 4.0)

``` scala
Vectors.dense(12.0, -51.0, 4.0) // while this is a mllib.linalg.Vector
```

>     res9: org.apache.spark.mllib.linalg.Vector = [12.0,-51.0,4.0]

``` scala
val rows: RDD[IndexedRow] = sc.parallelize(Array(IndexedRow(2, Vectors.dense(1,3)), IndexedRow(4, Vectors.dense(4,5)))) // an RDD of indexed rows
```

>     rows: org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.distributed.IndexedRow] = ParallelCollectionRDD[252] at parallelize at <console>:41

``` scala
// Create an IndexedRowMatrix from an RDD[IndexedRow].
val mat: IndexedRowMatrix = new IndexedRowMatrix(rows)
```

>     mat: org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix = org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix@2a57e8ca

``` scala
// Get its size.
val m = mat.numRows()
val n = mat.numCols()
```

>     m: Long = 5
>     n: Long = 2

``` scala
// Drop its row indices.
val rowMat: RowMatrix = mat.toRowMatrix()
```

>     rowMat: org.apache.spark.mllib.linalg.distributed.RowMatrix = org.apache.spark.mllib.linalg.distributed.RowMatrix@37fba875

``` scala
rowMat.rows.collect()
```

>     res11: Array[org.apache.spark.mllib.linalg.Vector] = Array([1.0,3.0], [4.0,5.0])

### IndexedRowMatrix in Python

An [`IndexedRowMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.IndexedRowMatrix)
can be created from an `RDD` of `IndexedRow`s, where
[`IndexedRow`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.IndexedRow)
is a wrapper over `(long, vector)`. An `IndexedRowMatrix` can be
converted to a `RowMatrix` by dropping its row indices.

Refer to the [`IndexedRowMatrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.IndexedRowMatrix)
for more details on the API.

``` python
from pyspark.mllib.linalg.distributed import IndexedRow, IndexedRowMatrix

# Create an RDD of indexed rows.
#   - This can be done explicitly with the IndexedRow class:
indexedRows = sc.parallelize([IndexedRow(0, [1, 2, 3]),
                              IndexedRow(1, [4, 5, 6]),
                              IndexedRow(2, [7, 8, 9]),
                              IndexedRow(3, [10, 11, 12])])

#   - or by using (long, vector) tuples:
indexedRows = sc.parallelize([(0, [1, 2, 3]), (1, [4, 5, 6]),
                              (2, [7, 8, 9]), (3, [10, 11, 12])])

# Create an IndexedRowMatrix from an RDD of IndexedRows.
mat = IndexedRowMatrix(indexedRows)

# Get its size.
m = mat.numRows()  # 4
n = mat.numCols()  # 3
print (m,n)

# Get the rows as an RDD of IndexedRows.
rowsRDD = mat.rows

# Convert to a RowMatrix by dropping the row indices.
rowMat = mat.toRowMatrix()

# Convert to a CoordinateMatrix.
coordinateMat = mat.toCoordinateMatrix()

# Convert to a BlockMatrix.
blockMat = mat.toBlockMatrix()
```

>     (4L, 3L)