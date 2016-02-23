// Databricks notebook source exported at Tue, 23 Feb 2016 09:44:54 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC # **Introduction to Spark through Scala Notebook** 
// MAGIC 
// MAGIC * This introduction notebook describes how to get started running Spark (Scala) code in Notebooks.
// MAGIC * Working with Spark's Resilient Distributed Datasets (RDDs)
// MAGIC   * creating RDDs
// MAGIC   * performing basic transformations on RDDs
// MAGIC   * performing basic actions on RDDs

// COMMAND ----------

// MAGIC %md
// MAGIC # Spark Cluster Overview:
// MAGIC ## Driver Program, Cluster Manager and Worker Nodes
// MAGIC 
// MAGIC The *driver* does the following:
// MAGIC 1. connects to a *cluster manager* to allocate resources across applications
// MAGIC * acquire *executors* on cluster nodes
// MAGIC   * executor processs run compute tasks and cache data in memory or disk on a *worker node*
// MAGIC * sends *application* (user program built on Spark) to the executors
// MAGIC * sends *tasks* for the executors to run
// MAGIC   * task is a unit of work that will sent to one executor
// MAGIC   
// MAGIC See [http://spark.apache.org/docs/latest/cluster-overview.html](http://spark.apache.org/docs/latest/cluster-overview.html) for an overview of the spark cluster. This is embeded in-place below for convenience. Scroll to the bottom to see a Glossary of terms used above and their meanings. You can right-click inside the embedded html ``<frame>...</frame>`` and use the left and right arrows to navigate within it!

// COMMAND ----------

// MAGIC %run "/scalable-data-science/xtraResources/support/sdsFunctions"

// COMMAND ----------

displayHTML(frameIt("http://spark.apache.org/docs/latest/cluster-overview.html",700))

// COMMAND ----------

// MAGIC %md
// MAGIC ## The Abstraction of Resilient Distributed Dataset (RDD)
// MAGIC 
// MAGIC #### RDD is a fault-tolerant collection of elements that can be operated on in parallel
// MAGIC 
// MAGIC #### Two types of Operations are possible on an RDD
// MAGIC * Transformations
// MAGIC * Actions
// MAGIC 
// MAGIC **(watch now 2:26)**:
// MAGIC 
// MAGIC [![RDD in Spark by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/3nreQ1N7Jvk/0.jpg)](https://www.youtube.com/v/3nreQ1N7Jvk?rel=0&autoplay=1&modestbranding=1&start=1&end=146)
// MAGIC 
// MAGIC ***
// MAGIC 
// MAGIC ## Transformations
// MAGIC **(watch now 1:18)**:
// MAGIC 
// MAGIC [![Spark Transformations by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/360UHWy052k/0.jpg)](https://www.youtube.com/v/360UHWy052k?rel=0&autoplay=1&modestbranding=1)
// MAGIC 
// MAGIC ***
// MAGIC 
// MAGIC 
// MAGIC ## Actions
// MAGIC **(watch now 0:48)**:
// MAGIC 
// MAGIC [![Spark Actions by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/F2G4Wbc5ZWQ/0.jpg)](https://www.youtube.com/v/F2G4Wbc5ZWQ?rel=0&autoplay=1&modestbranding=1&start=1&end=48)
// MAGIC 
// MAGIC ***
// MAGIC 
// MAGIC **Key Points**
// MAGIC * Resilient distributed datasets (RDDs) are the primary abstraction in Spark.
// MAGIC * RDDs are immutable once created:
// MAGIC     * can transform it.
// MAGIC     * can perform actions on it.
// MAGIC     * but cannot change an RDD once you construct it.
// MAGIC * Spark tracks each RDD's lineage information or recipe to enable its efficient recomputation if a machine fails.
// MAGIC * RDDs enable operations on collections of elements in parallel.
// MAGIC * We can construct RDDs by:
// MAGIC     * parallelizing Scala collections such as lists or arrays
// MAGIC     * by transforming an existing RDD,
// MAGIC     * from files in distributed file systems such as (HDFS, S3, etc.).
// MAGIC * We can specify the number of partitions for an RDD
// MAGIC * The more partitions in an RDD, the more opportunities for parallelism
// MAGIC * There are **two types of operations** you can perform on an RDD:
// MAGIC     * **transformations** (are lazily evaluated) 
// MAGIC       * map
// MAGIC       * flatMap
// MAGIC       * filter
// MAGIC       * distinct
// MAGIC       * ...
// MAGIC     * **actions** (actual evaluation happens)
// MAGIC       * count
// MAGIC       * reduce
// MAGIC       * take
// MAGIC       * collect
// MAGIC       * takeOrdered
// MAGIC       * ...
// MAGIC * Spark transformations enable us to create new RDDs from an existing RDD.
// MAGIC * RDD transformations are lazy evaluations (results are not computed right away)
// MAGIC * Spark remembers the set of transformations that are applied to a base data set (this is the lineage graph of RDD) 
// MAGIC * The allows Spark to automatically recover RDDs from failures and slow workers.
// MAGIC * The lineage graph is a recipe for creating a result and it can be optimized before execution.
// MAGIC * A transformed RDD is executed only when an action runs on it.
// MAGIC * You can also persist, or cache, RDDs in memory or on disk (this speeds up iterative ML algorithms that transforms the initial RDD iteratively).
// MAGIC * Here is a great reference URL for working with Spark.
// MAGIC     * [The latest Spark programming guide](http://spark.apache.org/docs/latest/programming-guide.html) and it is embedded below in-place for your convenience.
// MAGIC     

// COMMAND ----------

displayHTML(frameIt("http://spark.apache.org/docs/latest/programming-guide.html",800))

// COMMAND ----------

// MAGIC %md
// MAGIC # Let us get our hands dirty in Spark implementing these ideas!

// COMMAND ----------

// MAGIC %md
// MAGIC ### Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Running **Spark**
// MAGIC The variable **sc** allows you to access a Spark Context to run your Spark programs.
// MAGIC Recall ``SparkContext`` is in the Driver Program.
// MAGIC 
// MAGIC ![](http://spark.apache.org/docs/latest/img/cluster-overview.png)
// MAGIC 
// MAGIC **NOTE: Do not create the *sc* variable - it is already initialized for you. **

// COMMAND ----------

// MAGIC %md
// MAGIC ### We will do the following next:
// MAGIC 1. Create an RDD
// MAGIC * Perform the ``collect`` action on the RDD
// MAGIC * Perform the ``take`` action on the RDD
// MAGIC * Transform the RDD by ``map`` to make another RDD
// MAGIC * Transform the RDD by ``filter`` to make another RDD
// MAGIC * Perform the ``reduce`` action on the RDD
// MAGIC * Transform the RDD by ``flatMap`` to make another RDD
// MAGIC * Perform the ``reduceByKey`` action on the RDD
// MAGIC * HOMEWORK

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. Create an RDD
// MAGIC 
// MAGIC First, let us create an RDD of numbers (of integer type ``Int``) from a Scala ``Seq`` or ``List`` by using the ``parallelize`` method of the available Spark Context ``sc`` as follows:

// COMMAND ----------

val x = sc.parallelize(Seq(1, 2, 3))    // <Ctrl+Enter> to evaluate this cell (using default number of partitions)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Perform the `collect` action on the RDD
// MAGIC 
// MAGIC No action has been taken by ``sc.parallelize`` above.  To see what is "cooked" by the recipe for RDD ``x`` we need to take an action.  
// MAGIC 
// MAGIC The simplest is the ``collect`` action which returns all of the elements of the RDD as an ``Array`` to the driver program and displays it.
// MAGIC 
// MAGIC *So you have to make sure that all of that data will fit in the driver program if you call ``collect`` action!*

// COMMAND ----------

// MAGIC %md
// MAGIC #### Let us look at the [collect action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/collect).
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-90.png)

// COMMAND ----------

x.collect()    // <Ctrl+Enter> to collect (action) elements of rdd; should be (1, 2, 3)

// COMMAND ----------

// MAGIC %md
// MAGIC *CAUTION:* ``collect`` can crash the driver when called upon an RDD with massively many elements.  
// MAGIC So, it is better to use other diplaying actions like ``take`` or ``takeOrdered`` as follows:

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. Perform the `take` action on the RDD
// MAGIC 
// MAGIC The ``.take(n)`` action returns an array with the first ``n`` elements of the RDD.

// COMMAND ----------

x.take(2)

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC 
// MAGIC ### 4. Transform the RDD by ``map`` to make another RDD
// MAGIC 
// MAGIC The ``map`` transformation returns a new RDD that's formed by passing each element of the source RDD through a function (closure). The closure is automatically passed on to the workers for evaluation (when an action is called later). 

// COMMAND ----------

// MAGIC %md
// MAGIC #### Let us look at the [map transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/map).
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-18.png)

// COMMAND ----------

val x = sc.parallelize(Array("b", "a", "c"))
val y = x.map(z => (z,1))

// COMMAND ----------

println(x.collect().mkString(", "))
println(y.collect().mkString(", "))

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC 
// MAGIC ### 5. Transform the RDD by ``filter`` to make another RDD
// MAGIC 
// MAGIC The ``filter`` transformation returns a new RDD that's formed by selecting those elements of the source RDD on which the function returns ``true``.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Let us look at the [filter transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/filter).
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-24.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3))
val y = x.filter(n => n%2 == 1)

// COMMAND ----------

println(x.collect().mkString(", "))
println(y.collect().mkString(", "))

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ### 6. Perform the ``reduce`` action on the RDD
// MAGIC 
// MAGIC Reduce aggregates a data set element using a function (closure). 
// MAGIC This function takes two arguments and returns one and can often be seen as a binary operator. 
// MAGIC This operator has to be commutative and associative so that it can be computed correctly in parallel (where we have little control over the order of the operations!).

// COMMAND ----------

// MAGIC %md
// MAGIC ### Let us look at the [reduce action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/reduce).
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-94.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3,4))
val y = x.reduce((a,b) => a+b)

// COMMAND ----------

println(x.collect.mkString(", "))
println(y)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 7. Transform an RDD by ``flatMap`` to make another RDD
// MAGIC 
// MAGIC ``flatMap`` is similar to ``map`` but each element from input RDD can be mapped to zero or more output elements. 
// MAGIC Therefore your function should return a sequential collection such as an ``Array`` rather than a single element as shown below.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Let us look at the [flatMap transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/flatMap).
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-31.png)

// COMMAND ----------

val x = sc.parallelize(Array(1,2,3))
val y = x.flatMap(n => Array(n, n*100, 42))

// COMMAND ----------

println(x.collect().mkString(", "))
println(y.collect().mkString(", "))

// COMMAND ----------

// MAGIC %md
// MAGIC ### 8. Perform the ``reduceByKey`` action on the RDD
// MAGIC 
// MAGIC Let's next look at what happens when we transform an RDD of strings. 
// MAGIC 
// MAGIC We will learn an extremely useful action called ``reduceByKey`` where reduce operations are only performed on values with the same key from an RDD of ``(key,value)`` pairs.

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-44.png)

// COMMAND ----------

val words = sc.parallelize(Array("a", "b", "a", "a", "b", "b", "a", "a", "a", "b", "b"))
val wordcounts = words.map(s => (s, 1)).reduceByKey(_ + _).collect() 

// COMMAND ----------

// MAGIC %md
// MAGIC ### 9. HOMEWORK 
// MAGIC See the notebook in this folder named `001_RDDsTransformationsActionsHOMEWORK`. 
// MAGIC This notebook will give you more examples of the operations above as well as others we will be using later, including:
// MAGIC * Perform the ``takeOrdered`` action on the RDD
// MAGIC * Transform the RDD by ``distinct`` to make another RDD and
// MAGIC * Let us do a bunch of transformations to our RDD and perform an action

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC ### **Importing Standard Scala and Java libraries**
// MAGIC * For other libraries that are not available by default, you can upload other libraries to the Workspace.
// MAGIC * Refer to the **[Libraries](/#workspace/databricks_guide/02 Product Overview/07 Libraries)** guide for more details.

// COMMAND ----------

import scala.math._
val x = min(1, 10)

// COMMAND ----------

import java.util.HashMap
val map = new HashMap[String, Int]()
map.put("a", 1)
map.put("b", 2)
map.put("c", 3)
map.put("d", 4)
map.put("e", 5)


// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)