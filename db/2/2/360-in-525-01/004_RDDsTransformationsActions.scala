// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC # Introduction to Spark
// MAGIC ## Spark Essentials: RDDs, Transformations and Actions
// MAGIC 
// MAGIC * This introductory notebook describes how to get started running Spark (Scala) code in Notebooks.
// MAGIC * Working with Spark's Resilient Distributed Datasets (RDDs)
// MAGIC   * creating RDDs
// MAGIC   * performing basic transformations on RDDs
// MAGIC   * performing basic actions on RDDs
// MAGIC 
// MAGIC **RECOLLECT** from `001_WhySpark` notebook and AJ's videos (did you watch the ones marked *Watch Now*? if NOT we should watch NOW!!! This was last night's "Viewing Home-Work") that *Spark does fault-tolerant, distributed, in-memory computing*

// COMMAND ----------

// MAGIC %md
// MAGIC # Spark Cluster Overview:
// MAGIC ## Driver Program, Cluster Manager and Worker Nodes
// MAGIC 
// MAGIC The *driver* does the following:
// MAGIC 
// MAGIC 1. connects to a *cluster manager* to allocate resources across applications
// MAGIC * acquire *executors* on cluster nodes
// MAGIC   * executor processs run compute tasks and cache data in memory or disk on a *worker node*
// MAGIC * sends *application* (user program built on Spark) to the executors
// MAGIC * sends *tasks* for the executors to run
// MAGIC   * task is a unit of work that will be sent to one executor
// MAGIC   
// MAGIC ![](http://spark.apache.org/docs/latest/img/cluster-overview.png)
// MAGIC 
// MAGIC See [http://spark.apache.org/docs/latest/cluster-overview.html](http://spark.apache.org/docs/latest/cluster-overview.html) for an overview of the spark cluster.

// COMMAND ----------

// MAGIC %md
// MAGIC ## The Abstraction of Resilient Distributed Dataset (RDD)
// MAGIC 
// MAGIC #### RDD is a fault-tolerant collection of elements that can be operated on in parallel
// MAGIC 
// MAGIC #### Two types of Operations are possible on an RDD
// MAGIC 
// MAGIC * Transformations
// MAGIC * Actions
// MAGIC 
// MAGIC **(watch now 2:26)**:
// MAGIC 
// MAGIC [![RDD in Spark by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/3nreQ1N7Jvk/0.jpg)](https://www.youtube.com/watch?v=3nreQ1N7Jvk?rel=0&autoplay=1&modestbranding=1&start=1&end=146)
// MAGIC 
// MAGIC 
// MAGIC ***
// MAGIC 
// MAGIC ## Transformations
// MAGIC **(watch now 1:18)**:
// MAGIC 
// MAGIC [![Spark Transformations by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/360UHWy052k/0.jpg)](https://www.youtube.com/watch?v=360UHWy052k?rel=0&autoplay=1&modestbranding=1)
// MAGIC 
// MAGIC ***
// MAGIC 
// MAGIC 
// MAGIC ## Actions
// MAGIC **(watch now 0:48)**:
// MAGIC 
// MAGIC [![Spark Actions by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/F2G4Wbc5ZWQ/0.jpg)](https://www.youtube.com/watch?v=F2G4Wbc5ZWQ?rel=0&autoplay=1&modestbranding=1&start=1&end=48)
// MAGIC 
// MAGIC ***
// MAGIC 
// MAGIC ## Key Points
// MAGIC 
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
// MAGIC     * [The latest Spark programming guide](http://spark.apache.org/docs/latest/programming-guide.html).
// MAGIC     

// COMMAND ----------

// MAGIC %md
// MAGIC ## Let us get our hands dirty in Spark implementing these ideas!

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #### DO NOW 
// MAGIC 
// MAGIC In your databricks community edition:
// MAGIC 
// MAGIC 1. In your `WorkSpace` create a Folder named `scalable-data-science`
// MAGIC 2. *Import* the databricks archive file at the following URL:
// MAGIC     * [https://github.com/lamastex/scalable-data-science/raw/master/dbcArchives/2017/parts/xtraResources.dbc](https://github.com/lamastex/scalable-data-science/raw/master/dbcArchives/2017/parts/xtraResources.dbc)
// MAGIC 3. This should open a structure of directories in with path: `/Workspace/scalable-data-science/xtraResources/`

// COMMAND ----------

// MAGIC %md
// MAGIC ### Let us look at the legend and overview of the visual RDD Api by doing the following first:
// MAGIC 
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-1.png)

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
// MAGIC 
// MAGIC 1. Create an RDD using `sc.parallelize`
// MAGIC * Perform the `collect` action on the RDD and find the number of partitions it is made of using `getNumPartitions` action
// MAGIC * Perform the ``take`` action on the RDD
// MAGIC * Transform the RDD by ``map`` to make another RDD
// MAGIC * Transform the RDD by ``filter`` to make another RDD
// MAGIC * Perform the ``reduce`` action on the RDD
// MAGIC * Transform the RDD by ``flatMap`` to make another RDD
// MAGIC * Create a Pair RDD
// MAGIC * Perform some transformations on a Pair RDD
// MAGIC * Where in the cluster is your computation running?
// MAGIC * Shipping Closures, Broadcast Variables and Accumulator Variables
// MAGIC * Spark Essentials: Summary
// MAGIC * HOMEWORK

// COMMAND ----------

// MAGIC %md
// MAGIC ## Entry Point
// MAGIC 
// MAGIC Now we are ready to start programming in Spark!
// MAGIC 
// MAGIC Our entry point for Spark 2.x applications is the class `SparkSession`. An instance of this object is already instantiated for us which can be easily demonstrated by running the next cell
// MAGIC 
// MAGIC We will need these docs!
// MAGIC 
// MAGIC  * [RDD Scala Docs](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.rdd.RDD)
// MAGIC  * [RDD Python Docs](https://spark.apache.org/docs/2.2.0/api/python/index.html#org.apache.spark.rdd.RDD)
// MAGIC  * [DataFrame Scala Docs](https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.sql.Dataset)
// MAGIC  * [DataFrame Python Docs](https://spark.apache.org/docs/latest/api/python/pyspark.sql.html)

// COMMAND ----------

println(spark)

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC NOTE that in Spark 2.0 `SparkSession` is a replacement for the other entry points:
// MAGIC * `SparkContext`, available in our notebook as **sc**.
// MAGIC * `SQLContext`, or more specifically its subclass `HiveContext`, available in our notebook as **sqlContext**.

// COMMAND ----------

println(sc)
println(sqlContext)

// COMMAND ----------

// MAGIC %md
// MAGIC  
// MAGIC We will be using the pre-made SparkContext `sc` when learning about RDDs.

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. Create an RDD using `sc.parallelize`
// MAGIC 
// MAGIC First, let us create an RDD of three elements (of integer type ``Int``) from a Scala ``Seq`` (or ``List`` or ``Array``) with two partitions by using the ``parallelize`` method of the available Spark Context ``sc`` as follows:

// COMMAND ----------

val x = sc.parallelize(Array(1, 2, 3), 2)    // <Ctrl+Enter> to evaluate this cell (using 2 partitions)

// COMMAND ----------

x.  // place the cursor after 'x.' and hit Tab to see the methods available for the RDD x we created

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Perform the `collect` action on the RDD and find the number of partitions it is made of using `getNumPartitions` action
// MAGIC 
// MAGIC No action has been taken by ``sc.parallelize`` above.  To see what is "cooked" by the recipe for RDD ``x`` we need to take an action.  
// MAGIC 
// MAGIC The simplest is the ``collect`` action which returns all of the elements of the RDD as an ``Array`` to the driver program and displays it.
// MAGIC 
// MAGIC *So you have to make sure that all of that data will fit in the driver program if you call ``collect`` action!*

// COMMAND ----------

// MAGIC %md
// MAGIC #### Let us look at the [collect action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/collect) and return here to try out the example codes.
// MAGIC 
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-90.png)

// COMMAND ----------

// MAGIC %md
// MAGIC Let us perform a `collect` action on RDD `x` as follows: 

// COMMAND ----------

x.collect()    // <Ctrl+Enter> to collect (action) elements of rdd; should be (1, 2, 3)

// COMMAND ----------

// MAGIC %md
// MAGIC *CAUTION:* ``collect`` can crash the driver when called upon an RDD with massively many elements.  
// MAGIC So, it is better to use other diplaying actions like ``take`` or ``takeOrdered`` as follows:

// COMMAND ----------

// MAGIC %md
// MAGIC #### Let us look at the [getNumPartitions action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/getNumPartitions) and return here to try out the example codes.
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-88.png)

// COMMAND ----------

// <Ctrl+Enter> to evaluate this cell and find the number of partitions in RDD x
x.getNumPartitions 

// COMMAND ----------

// MAGIC %md
// MAGIC We can see which elements of the RDD are in which parition by calling `glom()` before `collect()`. 
// MAGIC 
// MAGIC `glom()` flattens elements of the same partition into an `Array`. 

// COMMAND ----------

x.glom().collect() // glom() flattens elements on the same partition

// COMMAND ----------

// MAGIC %md
// MAGIC Thus from the output above, `Array[Array[Int]] = Array(Array(1), Array(2, 3))`, we know that `1` is in one partition while `2` and `3` are in another partition.

// COMMAND ----------

// MAGIC %md
// MAGIC ##### You Try!
// MAGIC Crate an RDD `x` with three elements, 1,2,3, and this time do not specifiy the number of partitions.  Then the default number of partitions will be used.
// MAGIC Find out what this is for the cluster you are attached to. 
// MAGIC 
// MAGIC The default number of partitions for an RDD depends on the cluster this notebook is attached to among others - see [programming-guide](http://spark.apache.org/docs/latest/programming-guide.html).

// COMMAND ----------

val x = sc.parallelize(Seq(1, 2, 3))    // <Shift+Enter> to evaluate this cell (using default number of partitions)

// COMMAND ----------

x.getNumPartitions // <Shift+Enter> to evaluate this cell

// COMMAND ----------

x.glom().collect() // <Ctrl+Enter> to evaluate this cell

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. Perform the `take` action on the RDD
// MAGIC 
// MAGIC The ``.take(n)`` action returns an array with the first ``n`` elements of the RDD.

// COMMAND ----------

x.take(2) // Ctrl+Enter to take two elements from the RDD x

// COMMAND ----------

// MAGIC %md
// MAGIC ##### You Try!
// MAGIC Fill in the parenthes `( )` below in order to `take` just one element from RDD `x`.

// COMMAND ----------

//x.take(  ) // uncomment by removing '//' before x in the cell and fill in the parenthesis to take just one element from RDD x and Cntrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC 
// MAGIC ### 4. Transform the RDD by ``map`` to make another RDD
// MAGIC 
// MAGIC The ``map`` transformation returns a new RDD that's formed by passing each element of the source RDD through a function (closure). The closure is automatically passed on to the workers for evaluation (when an action is called later). 

// COMMAND ----------

// MAGIC %md
// MAGIC #### Let us look at the [map transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/map) and return here to try out the example codes.
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-18.png)

// COMMAND ----------

// Shift+Enter to make RDD x and RDD y that is mapped from x
val x = sc.parallelize(Array("b", "a", "c")) // make RDD x: [b, a, c]
val y = x.map(z => (z,1))                    // map x into RDD y: [(b, 1), (a, 1), (c, 1)]

// COMMAND ----------

// Cntrl+Enter to collect and print the two RDDs
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
// MAGIC #### Let us look at the [filter transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/filter) and return here to try out the example codes.
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-24.png)

// COMMAND ----------

//Shift+Enter to make RDD x and filter it by (n => n%2 == 1) to make RDD y
val x = sc.parallelize(Array(1,2,3))
// the closure (n => n%2 == 1) in the filter will 
// return True if element n in RDD x has remainder 1 when divided by 2 (i.e., if n is odd)
val y = x.filter(n => n%2 == 1) 

// COMMAND ----------

// Cntrl+Enter to collect and print the two RDDs
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
// MAGIC ### Let us look at the [reduce action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/reduce) and return here to try out the example codes.
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-94.png)

// COMMAND ----------

//Shift+Enter to make RDD x of inteegrs 1,2,3,4 and reduce it to sum
val x = sc.parallelize(Array(1,2,3,4))
val y = x.reduce((a,b) => a+b)

// COMMAND ----------

//Cntrl+Enter to collect and print RDD x and the Int y, sum of x
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
// MAGIC ### Let us look at the [flatMap transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/flatMap) and return here to try out the example codes.
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-31.png)

// COMMAND ----------

//Shift+Enter to make RDD x and flatMap it into RDD by closure (n => Array(n, n*100, 42))
val x = sc.parallelize(Array(1,2,3))
val y = x.flatMap(n => Array(n, n*100, 42))

// COMMAND ----------

//Cntrl+Enter to collect and print RDDs x and y
println(x.collect().mkString(", "))
println(y.collect().mkString(", "))

// COMMAND ----------

// MAGIC %md
// MAGIC ### 8. Create a Pair RDD
// MAGIC 
// MAGIC Let's next work with RDD of ``(key,value)`` pairs called a *Pair RDD* or *Key-Value RDD*.

// COMMAND ----------

// Cntrl+Enter to make RDD words and display it by collect
val words = sc.parallelize(Array("a", "b", "a", "a", "b", "b", "a", "a", "a", "b", "b"))
words.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Let's make a Pair RDD called `wordCountPairRDD` that is made of (key,value) pairs with key=word and value=1 in order to encode each occurrence of each word in the RDD `words`, as follows:

// COMMAND ----------

// Cntrl+Enter to make and collect Pair RDD wordCountPairRDD
val wordCountPairRDD = words.map(s => (s, 1))
wordCountPairRDD.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 9. Perform some transformations on a Pair RDD
// MAGIC 
// MAGIC Let's next work with RDD of ``(key,value)`` pairs called a *Pair RDD* or *Key-Value RDD*.
// MAGIC 
// MAGIC Now some of the Key-Value transformations that we could perform include the following.
// MAGIC 
// MAGIC * **`reduceByKey` transformation**
// MAGIC   * which takes an RDD and returns a new RDD of key-value pairs, such that:
// MAGIC     * the values for each key are aggregated using the given reduced function
// MAGIC     * and the reduce function has to be of the type that takes two values and returns one value.
// MAGIC * **`sortByKey` transformation**
// MAGIC   * this returns a new RDD of key-value pairs that's sorted by keys in ascending order
// MAGIC * **`groupByKey` transformation**
// MAGIC   * this returns a new RDD consisting of key and iterable-valued pairs.
// MAGIC 
// MAGIC Let's see some concrete examples next.

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-44.png)

// COMMAND ----------

// Cntrl+Enter to reduceByKey and collect wordcounts RDD
//val wordcounts = wordCountPairRDD.reduceByKey( _ + _ )
val wordcounts = wordCountPairRDD.reduceByKey( (v1,v2) => v1+v2 )
wordcounts.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Now, let us do just the crucial steps and avoid collecting intermediate RDDs (something we should avoid for large datasets anyways, as they may not fit in the driver program).

// COMMAND ----------

//Cntrl+Enter to make words RDD and do the word count in two lines
val words = sc.parallelize(Array("a", "b", "a", "a", "b", "b", "a", "a", "a", "b", "b"))
val wordcounts = words.map(s => (s, 1)).reduceByKey(_ + _).collect() 

// COMMAND ----------

// MAGIC %md
// MAGIC ##### You Try!
// MAGIC You try evaluating `sortByKey()` which will make a new RDD that consists of the elements of the original pair RDD that are sorted by Keys.

// COMMAND ----------

// Shift+Enter and comprehend code
val words = sc.parallelize(Array("a", "b", "a", "a", "b", "b", "a", "a", "a", "b", "b"))
val wordCountPairRDD = words.map(s => (s, 1))
val wordCountPairRDDSortedByKey = wordCountPairRDD.sortByKey()

// COMMAND ----------

wordCountPairRDD.collect() // Shift+Enter and comprehend code

// COMMAND ----------

wordCountPairRDDSortedByKey.collect() // Cntrl+Enter and comprehend code

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC The next key value transformation we will see is `groupByKey`
// MAGIC 
// MAGIC When we apply the `groupByKey` transformation to `wordCountPairRDD` we end up with a new RDD that contains two elements.
// MAGIC The first element is the tuple `b` and an iterable `CompactBuffer(1,1,1,1,1)` obtained by grouping the value `1` for each of the five key value pairs `(b,1)`.
// MAGIC Similarly the second element is the key `a` and an iterable `CompactBuffer(1,1,1,1,1,1)` obtained by grouping the value `1` for each of the six key value pairs `(a,1)`.
// MAGIC 
// MAGIC *CAUTION*: `groupByKey` can cause a large amount of data movement across the network.
// MAGIC It also can create very large iterables at a worker.
// MAGIC Imagine you have an RDD where you have 1 billion pairs that have the key `a`.
// MAGIC All of the values will have to fit in a single worker if you use group by key.
// MAGIC So instead of a group by key, consider using reduced by key.

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-45.png)

// COMMAND ----------

val wordCountPairRDDGroupByKey = wordCountPairRDD.groupByKey() // <Shift+Enter> CAUTION: this transformation can be very wide!

// COMMAND ----------

wordCountPairRDDGroupByKey.collect()  // Cntrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC ### 10. Where in the cluster is your computation running?

// COMMAND ----------

val list = 1 to 10
var sum = 0
list.map(x => sum = sum + x)
print(sum)

// COMMAND ----------

val rdd = sc.parallelize(1 to 10)
var sum = 0

// COMMAND ----------

val rdd1 = rdd.map(x => sum = sum + x)

// COMMAND ----------

rdd1.collect()

// COMMAND ----------

val rdd1 = rdd.map(x => {var sum = 0;
                         sum = sum + x
                         sum}
                  )

// COMMAND ----------

rdd1.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### 11. Shipping Closures, Broadcast Variables and Accumulator Variables
// MAGIC 
// MAGIC #### Closures, Broadcast and Accumulator Variables
// MAGIC **(watch now 2:06)**:
// MAGIC 
// MAGIC [![Closures, Broadcast and Accumulators by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/I9Zcr4R35Ao/0.jpg)](https://www.youtube.com/watch?v=I9Zcr4R35Ao?rel=0&autoplay=1&modestbranding=1)
// MAGIC 
// MAGIC 
// MAGIC We will use these variables in the sequel.
// MAGIC 
// MAGIC #### SUMMARY
// MAGIC Spark automatically creates closures 
// MAGIC 
// MAGIC   * for functions that run on RDDs at workers,
// MAGIC   * and for any global variables that are used by those workers
// MAGIC   * one closure per worker is sent with every task
// MAGIC   * and there's no communication between workers
// MAGIC   * closures are one way from the driver to the worker
// MAGIC   * any changes that you make to the global variables at the workers 
// MAGIC     * are not sent to the driver or
// MAGIC     * are not sent to other workers.
// MAGIC   
// MAGIC     
// MAGIC  The problem we have is that these closures
// MAGIC  
// MAGIC    * are automatically created are sent or re-sent with every job
// MAGIC    * with a large global variable it gets inefficient to send/resend lots of data to each worker
// MAGIC    * we cannot communicate that back to the driver
// MAGIC   
// MAGIC   
// MAGIC  To do this, Spark provides shared variables in two different types.
// MAGIC  
// MAGIC   * **broadcast variables**
// MAGIC     * lets us to efficiently send large read-only values to all of the workers
// MAGIC     * these are saved at the workers for use in one or more Spark operations.    
// MAGIC   * **accumulator variables**
// MAGIC     * These allow us to aggregate values from workers back to the driver.
// MAGIC     * only the driver can access the value of the accumulator 
// MAGIC     * for the tasks, the accumulators are basically write-only
// MAGIC     
// MAGIC  ***
// MAGIC  
// MAGIC  ### 12. Spark Essentials: Summary
// MAGIC  **(watch now: 0:29)**
// MAGIC  
// MAGIC [![Spark Essentials Summary by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/F50Vty9Ia8Y/0.jpg)](https://www.youtube.com/watch?v=F50Vty9Ia8Y?rel=0&autoplay=1&modestbranding=1)
// MAGIC 
// MAGIC *NOTE:* In databricks cluster, we (the course coordinator/administrators) set the number of workers for you.

// COMMAND ----------

// MAGIC %md
// MAGIC ### 13. HOMEWORK 
// MAGIC See the notebook in this folder named `005_RDDsTransformationsActionsHOMEWORK`. 
// MAGIC This notebook will give you more examples of the operations above as well as others we will be using later, including:
// MAGIC 
// MAGIC * Perform the ``takeOrdered`` action on the RDD
// MAGIC * Transform the RDD by ``distinct`` to make another RDD and
// MAGIC * Doing a bunch of transformations to our RDD and performing an action in a single cell.

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC ### Importing Standard Scala and Java libraries
// MAGIC * For other libraries that are not available by default, you can upload other libraries to the Workspace.
// MAGIC * Refer to the **[Libraries](https://docs.databricks.com/user-guide/libraries.html)** guide for more details.

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
