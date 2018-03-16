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

Local vector in Scala
---------------------

A local vector has integer-typed and 0-based indices and double-typed
values, stored on a single machine.

MLlib supports two types of local vectors:

-   dense and
-   sparse.

A dense vector is backed by a double array
representing its entry values, while a sparse vector is backed by two
parallel arrays: indices and values.

For example, a vector
`(1.0, 0.0, 3.0)` can be represented:

-   in dense format as `[1.0, 0.0, 3.0]` or
-   in sparse format as `(3, [0, 2], [1.0, 3.0])`, where `3` is the size of the vector.

The base class of local vectors is [`Vector`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vector),
and we provide two implementations: [`DenseVector`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.DenseVector)
and [`SparseVector`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.SparseVector).
We recommend using the factory methods implemented in
[`Vectors`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vectors$)
to create local vectors.
Refer to the [`Vector` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vector)
and [`Vectors` Scala docs](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vectors$)
for details on the API.

``` scala
import org.apache.spark.mllib.linalg.{Vector, Vectors}

// Create a dense vector (1.0, 0.0, 3.0).
val dv: Vector = Vectors.dense(1.0, 0.0, 3.0)
```

>     import org.apache.spark.mllib.linalg.{Vector, Vectors}
>     dv: org.apache.spark.mllib.linalg.Vector = [1.0,0.0,3.0]

``` scala
// Create a sparse vector (1.0, 0.0, 3.0) by specifying its indices and values corresponding to nonzero entries.
val sv1: Vector = Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0))
```

>     sv1: org.apache.spark.mllib.linalg.Vector = (3,[0,2],[1.0,3.0])

``` scala
// Create a sparse vector (1.0, 0.0, 3.0) by specifying its nonzero entries.
val sv2: Vector = Vectors.sparse(3, Seq((0, 1.0), (2, 3.0)))
```

>     sv2: org.apache.spark.mllib.linalg.Vector = (3,[0,2],[1.0,3.0])

***Note:*** Scala imports `scala.collection.immutable.Vector` by
default, so you have to import `org.apache.spark.mllib.linalg.Vector`
explicitly to use MLlib’s `Vector`.

------------------------------------------------------------------------

------------------------------------------------------------------------

Local Vector in Python
----------------------

**python**: MLlib recognizes the following types as dense vectors:

-   NumPy’s
    [`array`](http://docs.scipy.org/doc/numpy/reference/generated/numpy.array.html)
-   Python’s list, e.g., `[1, 2, 3]`

and the following as sparse vectors:

-   MLlib’s
    [`SparseVector`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.SparseVector).
-   SciPy’s
    [`csc_matrix`](http://docs.scipy.org/doc/scipy/reference/generated/scipy.sparse.csc_matrix.html#scipy.sparse.csc_matrix)
    with a single column

We recommend using NumPy arrays over lists for efficiency, and using the
factory methods implemented in
[`Vectors`](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Vectors)
to create sparse vectors.

Refer to the [`Vectors` Python docs](http://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.linalg.Vectors)
for more details on the API.

``` python
import numpy as np
import scipy.sparse as sps
from pyspark.mllib.linalg import Vectors

# Use a NumPy array as a dense vector.
dv1 = np.array([1.0, 0.0, 3.0])
# Use a Python list as a dense vector.
dv2 = [1.0, 0.0, 3.0]
# Create a SparseVector.
sv1 = Vectors.sparse(3, [0, 2], [1.0, 3.0])
# Use a single-column SciPy csc_matrix as a sparse vector.
sv2 = sps.csc_matrix((np.array([1.0, 3.0]), np.array([0, 2]), np.array([0, 2])), shape = (3, 1))
```

``` python
print dv1
print dv2
print sv1
print sv2
```

>     [ 1.  0.  3.]
>     [1.0, 0.0, 3.0]
>     (3,[0,2],[1.0,3.0])
>       (0, 0)	1.0
>       (2, 0)	3.0