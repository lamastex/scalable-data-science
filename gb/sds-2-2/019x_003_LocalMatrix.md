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

Local Matrix in Scala
---------------------

A local matrix has integer-typed row and column indices and double-typed
values, **stored on a single machine**. MLlib supports:

-   dense matrices, whose entry values are stored in a single double array in column-major order, and
-   sparse matrices, whose non-zero entry values are stored in the Compressed Sparse Column (CSC) format in column-major order.

For example, the following dense matrix:
\\\[
\begin{pmatrix} 1.0 & 2.0 \\\ 3.0 & 4.0 \\\ 5.0 & 6.0 \end{pmatrix}
\\\]
is stored in a one-dimensional array `[1.0, 3.0, 5.0, 2.0, 4.0, 6.0]`
with the matrix size `(3, 2)`.

The base class of local matrices is
[`Matrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Matrix),
and we provide two implementations:
[`DenseMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.DenseMatrix),
and
[`SparseMatrix`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.SparseMatrix).
We recommend using the factory methods implemented in
[`Matrices`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Matrices$)
to create local matrices. Remember, local matrices in MLlib are stored
in column-major order.

Refer to the [`Matrix` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Matrix)
and [`Matrices` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Matrices)
for details on the API.

``` scala
Int.MaxValue // note the largest value an index can take
```

>     res0: Int = 2147483647

``` scala
import org.apache.spark.mllib.linalg.{Matrix, Matrices}

// Create a dense matrix ((1.0, 2.0), (3.0, 4.0), (5.0, 6.0))
val dm: Matrix = Matrices.dense(3, 2, Array(1.0, 3.0, 5.0, 2.0, 4.0, 6.0))
```

>     import org.apache.spark.mllib.linalg.{Matrix, Matrices}
>     dm: org.apache.spark.mllib.linalg.Matrix =
>     1.0  2.0
>     3.0  4.0
>     5.0  6.0

Next, let us create the following sparse local matrix:
\\\[
\begin{pmatrix} 9.0 & 0.0 \\\ 0.0 & 8.0 \\\ 0.0 & 6.0 \end{pmatrix}
\\\]

``` scala
// Create a sparse matrix ((9.0, 0.0), (0.0, 8.0), (0.0, 6.0))
val sm: Matrix = Matrices.sparse(3, 2, Array(0, 1, 3), Array(0, 2, 1), Array(9, 6, 8))
```

>     sm: org.apache.spark.mllib.linalg.Matrix =
>     3 x 2 CSCMatrix
>     (0,0) 9.0
>     (2,1) 6.0
>     (1,1) 8.0

Local Matrix in Python
----------------------

The base class of local matrices is
[`Matrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Matrix),
and we provide two implementations:
[`DenseMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.DenseMatrix),
and
[`SparseMatrix`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.SparseMatrix).
We recommend using the factory methods implemented in
[`Matrices`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Matrices)
to create local matrices. Remember, local matrices in MLlib are stored
in column-major order.

Refer to the [`Matrix` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Matrix)
and [`Matrices` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Matrices)
for more details on the API.

``` python
from pyspark.mllib.linalg import Matrix, Matrices

# Create a dense matrix ((1.0, 2.0), (3.0, 4.0), (5.0, 6.0))
dm2 = Matrices.dense(3, 2, [1, 2, 3, 4, 5, 6])
dm2
```

>     Out[1]: DenseMatrix(3, 2, [1.0, 2.0, 3.0, 4.0, 5.0, 6.0], False)

``` python
# Create a sparse matrix ((9.0, 0.0), (0.0, 8.0), (0.0, 6.0))
sm = Matrices.sparse(3, 2, [0, 1, 3], [0, 2, 1], [9, 6, 8])
sm
```

>     Out[2]: SparseMatrix(3, 2, [0, 1, 3], [0, 2, 1], [9.0, 6.0, 8.0], False)