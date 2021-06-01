// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [Apache Spark mllib-progamming-guide on mllib-data-types](http://spark.apache.org/docs/latest/mllib-data-types.html).
// MAGIC 
// MAGIC # [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/000_MLlibProgGuide)
// MAGIC 
// MAGIC ## [Data Types - MLlib Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)
// MAGIC 
// MAGIC -   [Local vector](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/001_LocalVector) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#local-vector)
// MAGIC -   [Labeled point](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/002_LabeledPoint) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#labeled-point)
// MAGIC -   [Local matrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/003_LocalMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#local-matrix)
// MAGIC -   [Distributed matrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#distributed-matrix)
// MAGIC     -   [RowMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/005_RowMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#rowmatrix)
// MAGIC     -   [IndexedRowMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#indexedrowmatrix)
// MAGIC     -   [CoordinateMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#coordinatematrix)
// MAGIC     -   [BlockMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/008_BlockMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#blockmatrix)
// MAGIC 
// MAGIC MLlib supports local vectors and matrices stored on a single machine, as
// MAGIC well as distributed matrices backed by one or more RDDs. Local vectors
// MAGIC and local matrices are simple data models that serve as public
// MAGIC interfaces. The underlying linear algebra operations are provided by
// MAGIC [Breeze](http://www.scalanlp.org/) and [jblas](http://jblas.org/). A
// MAGIC training example used in supervised learning is called a “labeled point”
// MAGIC in MLlib.