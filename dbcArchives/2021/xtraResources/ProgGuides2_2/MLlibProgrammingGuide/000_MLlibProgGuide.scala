// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the small subset of [Apache Spark 2.2 mllib-progamming-guide](http://spark.apache.org/docs/latest/mllib-guide.html) that one needs to dive a bit deeper into distributed linear algebra.
// MAGIC 
// MAGIC This is a huge task to complete for the entire mlib-programming-guide. Perhaps worth continuing for Spark 2.2. Any contributions in this 'databricksification' of the programming guide are most welcome. Please feel free to send pull-requests or just fork and push yourself at [https://github.com/raazesh-sainudiin/scalable-data-science](https://github.com/lamastex/scalable-data-science).
// MAGIC 
// MAGIC # [Overview](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/000_MLlibProgGuide)
// MAGIC 
// MAGIC - [Data Types - MLlib Programming Guide](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)
// MAGIC   -   [Local vector](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/001_LocalVector) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#local-vector)
// MAGIC   -   [Labeled point](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/002_LabeledPoint) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#labeled-point)
// MAGIC   -   [Local matrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/003_LocalMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#local-matrix)
// MAGIC   -   [Distributed matrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#distributed-matrix)
// MAGIC     -   [RowMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/005_RowMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#rowmatrix)
// MAGIC     -   [IndexedRowMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#indexedrowmatrix)
// MAGIC     -   [CoordinateMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#coordinatematrix)
// MAGIC     -   [BlockMatrix](/#workspace/scalable-data-science/xtraResources/ProgGuides2_2/MLlibProgrammingGuide/dataTypes/008_BlockMatrix) and [URL](http://spark.apache.org/docs/latest/mllib-data-types.html#blockmatrix)