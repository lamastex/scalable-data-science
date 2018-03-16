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

Distributed matrix in Scala
---------------------------

A distributed matrix has **long-typed row and column indices** and
**double-typed values**, stored distributively in one or more RDDs.

It is **very important to choose the right format to store large and distributed
matrices**. Converting a distributed matrix to a different format may
require a global shuffle, which is quite expensive.

Three types of distributed matrices have been implemented so far.

1.  The basic type is called `RowMatrix`.

-   A `RowMatrix` is a row-oriented distributed matrix without meaningful row indices, e.g., a collection of feature vectors.
    It is backed by an RDD of its rows, where each row is a local vector.
-   We assume that the number of columns is not huge for a `RowMatrix` so that a single local vector can be reasonably communicated to the driver and can also be stored / operated on using a single node.
-   An `IndexedRowMatrix` is similar to a `RowMatrix` but with row indices, which can be used for identifying rows and executing joins.
-   A `CoordinateMatrix` is a distributed matrix stored in [coordinate list (COO)](https://en.wikipedia.org/wiki/Sparse_matrix#Coordinate_list_.28COO.29) format, backed by an RDD of its entries.

***Note***

The underlying RDDs of a distributed matrix must be deterministic,
because we cache the matrix size. In general the use of
non-deterministic RDDs can lead to errors.

***Remark:*** there is a huge difference in the orders of magnitude between the maximum size of local versus distributed matrices!

``` scala
print(Long.MaxValue.toDouble, Int.MaxValue.toDouble, Long.MaxValue.toDouble / Int.MaxValue.toDouble) // index ranges and ratio for local and distributed matrices
```

>     (9.223372036854776E18,2.147483647E9,4.294967298E9)