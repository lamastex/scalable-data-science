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