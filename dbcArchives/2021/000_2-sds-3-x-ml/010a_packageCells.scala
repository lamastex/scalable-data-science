// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC This notebook is from databricks document:
// MAGIC  - [https://docs.databricks.com/_static/notebooks/package-cells.html](https://docs.databricks.com/_static/notebooks/package-cells.html)
// MAGIC  
// MAGIC  As you know, we need to eventually package and deploy our models, say using `sbt` (recall recommended home work 1).
// MAGIC  
// MAGIC  However it is nice to be in a notebook environment to prototype and build intuition and create better pipelines.
// MAGIC  
// MAGIC  Using package cells we can be in the best of both worlds to an extent.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Package Cells
// MAGIC **Package cells** are special cells that get compiled when executed. These cells have no visibility with respect to the rest of the notebook. You may think of them as separate scala files. 
// MAGIC 
// MAGIC This means that only `class` and `object` definitions may go inside this cell. You may not have any variable or function definitions lying around by itself. The following cell will not work.
// MAGIC 
// MAGIC If you wish to use custom classes and/or objects defined within notebooks reliably in Spark, and across notebook sessions, you must use package cells to define those classes.
// MAGIC 
// MAGIC Unless you use package cells to define classes, you may also come across obscure bugs as follows:

// COMMAND ----------

// We define a class
case class TestKey(id: Long, str: String)

// COMMAND ----------

// we use that class as a key in the group by
val rdd = sc.parallelize(Array((TestKey(1L, "abd"), "dss"), (TestKey(2L, "ggs"), "dse"), (TestKey(1L, "abd"), "qrf")))

rdd.groupByKey().collect

// COMMAND ----------

// MAGIC %md
// MAGIC What went wrong above? Even though we have two elements for the key `TestKey(1L, "abd")`, they behaved as two different keys resulting in:
// MAGIC 
// MAGIC ```
// MAGIC Array[(TestKey, Iterable[String])] = Array(
// MAGIC   (TestKey(2,ggs),CompactBuffer(dse)), 
// MAGIC   (TestKey(1,abd),CompactBuffer(dss)), 
// MAGIC   (TestKey(1,abd),CompactBuffer(qrf)))
// MAGIC ```
// MAGIC 
// MAGIC Once we define our case class within a package cell, we will not face this issue.

// COMMAND ----------

package com.databricks.example

case class TestKey(id: Long, str: String)

// COMMAND ----------

import com.databricks.example

val rdd = sc.parallelize(Array(
  (example.TestKey(1L, "abd"), "dss"), (example.TestKey(2L, "ggs"), "dse"), (example.TestKey(1L, "abd"), "qrf")))

rdd.groupByKey().collect

// COMMAND ----------

// MAGIC %md
// MAGIC As you can see above, the group by worked above, grouping two elements (`dss, qrf`) under `TestKey(1,abd)`.
// MAGIC 
// MAGIC These cells behave as individual source files, therefore only classes and objects can be defined inside these cells.

// COMMAND ----------

package x.y.z

val aNumber = 5 // won't work

def functionThatWillNotWork(a: Int): Int = a + 1

// COMMAND ----------

// MAGIC %md
// MAGIC The following cell is the way to go.

// COMMAND ----------

package x.y.z

object Utils {
  val aNumber = 5 // works!
  def functionThatWillWork(a: Int): Int = a + 1
}

// COMMAND ----------

import x.y.z.Utils

Utils.functionThatWillWork(Utils.aNumber)

// COMMAND ----------

// MAGIC %md
// MAGIC Why did we get the warning: `classes defined within packages cannot be redefined without a cluster restart`?
// MAGIC 
// MAGIC Classes that get compiled with the `package` cells get dynamically injected into Spark's classloader. Currently it's not possible to remove
// MAGIC classes from Spark's classloader. Any classes that you define and compile will have precedence in the classloader, therefore once you recompile,
// MAGIC it will not be visible to your application.
// MAGIC 
// MAGIC Well that kind of beats the purpose of iterative notebook development if I have to restart the cluster, right? In that case, you may just rename the package to `x.y.z2` during development/fast iteration and fix it once everything works.

// COMMAND ----------

// MAGIC %md
// MAGIC One thing to remember with package cells is that it has no visiblity regarding the notebook environment. 
// MAGIC 
// MAGIC  - The SparkContext will not be defined as `sc`. 
// MAGIC  - The SQLContext will not be defined as `sqlContext`. 
// MAGIC  - Did you import a package in a separate cell? Those imports will not be available in the package cell and have to be remade. 
// MAGIC  - Variables imported through `%run` cells will not be available.
// MAGIC  
// MAGIC It is really a standalone file that just looks like a cell in a notebook. This means that any function that uses anything that was defined in a separate cell, needs to take that variable as a parameter or the class needs to take it inside the constructor.

// COMMAND ----------

package x.y.zpackage

import org.apache.spark.SparkContext

case class IntArray(values: Array[Int])

class MyClass(sc: SparkContext) {
  def sparkSum(array: IntArray): Int = {
    sc.parallelize(array.values).reduce(_ + _)
  }
}

object MyClass {
  def sparkSum(sc: SparkContext, array: IntArray): Int = {
    sc.parallelize(array.values).reduce(_ + _)
  }
}

// COMMAND ----------

import x.y.zpackage._

val array = IntArray(Array(1, 2, 3, 4, 5))

val myClass = new MyClass(sc)
myClass.sparkSum(array)

// COMMAND ----------

MyClass.sparkSum(sc, array)

// COMMAND ----------

