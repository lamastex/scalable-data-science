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

### BlockMatrix in Scala

A `BlockMatrix` is a distributed matrix backed by an RDD of
`MatrixBlock`s, where a `MatrixBlock` is a tuple of
`((Int, Int), Matrix)`, where the `(Int, Int)` is the index of the
block, and `Matrix` is the sub-matrix at the given index with size
`rowsPerBlock` x `colsPerBlock`. `BlockMatrix` supports methods such as
`add` and `multiply` with another `BlockMatrix`. `BlockMatrix` also has
a helper function `validate` which can be used to check whether the
`BlockMatrix` is set up properly.

A [`BlockMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.BlockMatrix)
can be most easily created from an `IndexedRowMatrix` or
`CoordinateMatrix` by calling `toBlockMatrix`. `toBlockMatrix` creates
blocks of size 1024 x 1024 by default. Users may change the block size
by supplying the values through
`toBlockMatrix(rowsPerBlock, colsPerBlock)`.

Refer to the [`BlockMatrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.BlockMatrix)
for details on the API.

``` scala
//import org.apache.spark.mllib.linalg.{Matrix, Matrices}
import org.apache.spark.mllib.linalg.distributed.{BlockMatrix, CoordinateMatrix, MatrixEntry}
```

>     import org.apache.spark.mllib.linalg.distributed.{BlockMatrix, CoordinateMatrix, MatrixEntry}

``` scala
val entries: RDD[MatrixEntry] = sc.parallelize(Array(MatrixEntry(0, 0, 1.2), MatrixEntry(1, 0, 2.1), MatrixEntry(6, 1, 3.7))) // an RDD of matrix entries
```

>     entries: org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.distributed.MatrixEntry] = ParallelCollectionRDD[692] at parallelize at <console>:35

``` scala
// Create a CoordinateMatrix from an RDD[MatrixEntry].
val coordMat: CoordinateMatrix = new CoordinateMatrix(entries)
```

>     coordMat: org.apache.spark.mllib.linalg.distributed.CoordinateMatrix = org.apache.spark.mllib.linalg.distributed.CoordinateMatrix@68f1d303

``` scala
// Transform the CoordinateMatrix to a BlockMatrix
val matA: BlockMatrix = coordMat.toBlockMatrix().cache()
```

>     matA: org.apache.spark.mllib.linalg.distributed.BlockMatrix = org.apache.spark.mllib.linalg.distributed.BlockMatrix@1de2a311

``` scala
// Validate whether the BlockMatrix is set up properly. Throws an Exception when it is not valid.
// Nothing happens if it is valid.
matA.validate()
```

``` scala
// Calculate A^T A.
val ata = matA.transpose.multiply(matA)
```

>     ata: org.apache.spark.mllib.linalg.distributed.BlockMatrix = org.apache.spark.mllib.linalg.distributed.BlockMatrix@16a80e13

``` scala
ata.blocks.collect()
```

>     res1: Array[((Int, Int), org.apache.spark.mllib.linalg.Matrix)] = 
>     Array(((0,0),5.85  0.0                 
>     0.0   13.690000000000001  ))

``` scala
ata.toLocalMatrix()
```

>     res3: org.apache.spark.mllib.linalg.Matrix = 
>     5.85  0.0                 
>     0.0   13.690000000000001  

### BlockMatrix in Scala

A [`BlockMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.BlockMatrix)
can be created from an `RDD` of sub-matrix blocks, where a sub-matrix
block is a `((blockRowIndex, blockColIndex), sub-matrix)` tuple.

Refer to the [`BlockMatrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.distributed.BlockMatrix)
for more details on the API.

``` python
from pyspark.mllib.linalg import Matrices
from pyspark.mllib.linalg.distributed import BlockMatrix

# Create an RDD of sub-matrix blocks.
blocks = sc.parallelize([((0, 0), Matrices.dense(3, 2, [1, 2, 3, 4, 5, 6])),
                         ((1, 0), Matrices.dense(3, 2, [7, 8, 9, 10, 11, 12]))])

# Create a BlockMatrix from an RDD of sub-matrix blocks.
mat = BlockMatrix(blocks, 3, 2)

# Get its size.
m = mat.numRows() # 6
n = mat.numCols() # 2
print (m,n)

# Get the blocks as an RDD of sub-matrix blocks.
blocksRDD = mat.blocks

# Convert to a LocalMatrix.
localMat = mat.toLocalMatrix()

# Convert to an IndexedRowMatrix.
indexedRowMat = mat.toIndexedRowMatrix()

# Convert to a CoordinateMatrix.
coordinateMat = mat.toCoordinateMatrix()
```

>     (6L, 2L)