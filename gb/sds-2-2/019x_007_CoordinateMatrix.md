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

### CoordinateMatrix in Scala

A `CoordinateMatrix` is a distributed matrix backed by an RDD of its
entries. Each entry is a tuple of `(i: Long, j: Long, value: Double)`,
where `i` is the row index, `j` is the column index, and `value` is the
entry value. A `CoordinateMatrix` should be used only when both
dimensions of the matrix are huge and the matrix is very sparse.

A [`CoordinateMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.CoordinateMatrix)
can be created from an `RDD[MatrixEntry]` instance, where
[`MatrixEntry`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.MatrixEntry)
is a wrapper over `(Long, Long, Double)`. A `CoordinateMatrix` can be
converted to an `IndexedRowMatrix` with sparse rows by calling
`toIndexedRowMatrix`. Other computations for `CoordinateMatrix` are not
currently supported.

Refer to the [`CoordinateMatrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.CoordinateMatrix)
for details on the API.

``` scala
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
```

>     import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}

``` scala
val entries: RDD[MatrixEntry] = sc.parallelize(Array(MatrixEntry(0, 0, 1.2), MatrixEntry(1, 0, 2.1), MatrixEntry(6, 1, 3.7))) // an RDD of matrix entries
```

>     entries: org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.distributed.MatrixEntry] = ParallelCollectionRDD[454] at parallelize at <console>:35

``` scala
// Create a CoordinateMatrix from an RDD[MatrixEntry].
val mat: CoordinateMatrix = new CoordinateMatrix(entries)
```

>     mat: org.apache.spark.mllib.linalg.distributed.CoordinateMatrix = org.apache.spark.mllib.linalg.distributed.CoordinateMatrix@73dc93f3

``` scala
// Get its size.
val m = mat.numRows()
val n = mat.numCols()
```

>     m: Long = 7
>     n: Long = 2

``` scala
// Convert it to an IndexRowMatrix whose rows are sparse vectors.
val indexedRowMatrix = mat.toIndexedRowMatrix()
```

>     indexedRowMatrix: org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix = org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix@4a8e753a

``` scala
indexedRowMatrix.rows.collect()
```

>     res3: Array[org.apache.spark.mllib.linalg.distributed.IndexedRow] = Array(IndexedRow(0,(2,[0],[1.2])), IndexedRow(6,(2,[1],[3.7])), IndexedRow(1,(2,[0],[2.1])))

### CoordinateMatrix in Scala

A [`CoordinateMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.CoordinateMatrix)
can be created from an `RDD` of `MatrixEntry` entries, where
[`MatrixEntry`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.MatrixEntry)
is a wrapper over `(long, long, float)`. A `CoordinateMatrix` can be
converted to a `RowMatrix` by calling `toRowMatrix`, or to an
`IndexedRowMatrix` with sparse rows by calling `toIndexedRowMatrix`.

Refer to the [`CoordinateMatrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.CoordinateMatrix)
for more details on the API.

``` python
from pyspark.mllib.linalg.distributed import CoordinateMatrix, MatrixEntry

# Create an RDD of coordinate entries.
#   - This can be done explicitly with the MatrixEntry class:
entries = sc.parallelize([MatrixEntry(0, 0, 1.2), MatrixEntry(1, 0, 2.1), MatrixEntry(6, 1, 3.7)])

#   - or using (long, long, float) tuples:
entries = sc.parallelize([(0, 0, 1.2), (1, 0, 2.1), (2, 1, 3.7)])

# Create an CoordinateMatrix from an RDD of MatrixEntries.
mat = CoordinateMatrix(entries)

# Get its size.
m = mat.numRows()  # 3
n = mat.numCols()  # 2
print (m,n)

# Get the entries as an RDD of MatrixEntries.
entriesRDD = mat.entries

# Convert to a RowMatrix.
rowMat = mat.toRowMatrix()

# Convert to an IndexedRowMatrix.
indexedRowMat = mat.toIndexedRowMatrix()

# Convert to a BlockMatrix.
blockMat = mat.toBlockMatrix()
```

>     (3L, 2L)