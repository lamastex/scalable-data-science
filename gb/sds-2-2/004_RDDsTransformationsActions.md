[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

**Introduction to Spark**
=========================

Spark Essentials: RDDs, Transformations and Actions
---------------------------------------------------

-   This introductory notebook describes how to get started running Spark (Scala) code in Notebooks.
-   Working with Spark's Resilient Distributed Datasets (RDDs)
-   creating RDDs
-   performing basic transformations on RDDs
-   performing basic actions on RDDs

**RECOLLECT** from `001_WhySpark` notebook and AJ's videos (did you watch the ones marked *Watch Now*? if NOT we should watch NOW!!! This was last night's "Viewing Home-Work") that *Spark does fault-tolerant, distributed, in-memory computing*

Spark Cluster Overview:
=======================

Driver Program, Cluster Manager and Worker Nodes
------------------------------------------------

The *driver* does the following: 1. connects to a *cluster manager* to allocate resources across applications \* acquire *executors* on cluster nodes \* executor processs run compute tasks and cache data in memory or disk on a *worker node* \* sends *application* (user program built on Spark) to the executors \* sends *tasks* for the executors to run \* task is a unit of work that will be sent to one executor

![](http://spark.apache.org/docs/latest/img/cluster-overview.png)

See <http://spark.apache.org/docs/latest/cluster-overview.html> for an overview of the spark cluster.

The Abstraction of Resilient Distributed Dataset (RDD)
------------------------------------------------------

#### RDD is a fault-tolerant collection of elements that can be operated on in parallel

#### Two types of Operations are possible on an RDD

-   Transformations
-   Actions

**(watch now 2:26)**:

[![RDD in Spark by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/3nreQ1N7Jvk/0.jpg)](https://www.youtube.com/watch?v=3nreQ1N7Jvk?rel=0&autoplay=1&modestbranding=1&start=1&end=146)

------------------------------------------------------------------------

Transformations
---------------

**(watch now 1:18)**:

[![Spark Transformations by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/360UHWy052k/0.jpg)](https://www.youtube.com/watch?v=360UHWy052k?rel=0&autoplay=1&modestbranding=1)

------------------------------------------------------------------------

Actions
-------

**(watch now 0:48)**:

[![Spark Actions by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/F2G4Wbc5ZWQ/0.jpg)](https://www.youtube.com/watch?v=F2G4Wbc5ZWQ?rel=0&autoplay=1&modestbranding=1&start=1&end=48)

------------------------------------------------------------------------

**Key Points** \* Resilient distributed datasets (RDDs) are the primary abstraction in Spark. \* RDDs are immutable once created: \* can transform it. \* can perform actions on it. \* but cannot change an RDD once you construct it. \* Spark tracks each RDD's lineage information or recipe to enable its efficient recomputation if a machine fails. \* RDDs enable operations on collections of elements in parallel. \* We can construct RDDs by: \* parallelizing Scala collections such as lists or arrays \* by transforming an existing RDD, \* from files in distributed file systems such as (HDFS, S3, etc.). \* We can specify the number of partitions for an RDD \* The more partitions in an RDD, the more opportunities for parallelism \* There are **two types of operations** you can perform on an RDD: \* **transformations** (are lazily evaluated) \* map \* flatMap \* filter \* distinct \* ... \* **actions** (actual evaluation happens) \* count \* reduce \* take \* collect \* takeOrdered \* ... \* Spark transformations enable us to create new RDDs from an existing RDD. \* RDD transformations are lazy evaluations (results are not computed right away) \* Spark remembers the set of transformations that are applied to a base data set (this is the lineage graph of RDD) \* The allows Spark to automatically recover RDDs from failures and slow workers. \* The lineage graph is a recipe for creating a result and it can be optimized before execution. \* A transformed RDD is executed only when an action runs on it. \* You can also persist, or cache, RDDs in memory or on disk (this speeds up iterative ML algorithms that transforms the initial RDD iteratively). \* Here is a great reference URL for working with Spark. \* [The latest Spark programming guide](http://spark.apache.org/docs/latest/programming-guide.html).

Let us get our hands dirty in Spark implementing these ideas!
-------------------------------------------------------------

#### DO NOW

In your databricks community edition:

1.  In your \`WorkSpace' create a Folder named 'scalable-data-science'
2.  'Import' the databricks archive file at the following URL:
    -   <https://github.com/lamastex/scalable-data-science/raw/master/dbcArchives/2017/parts/xtraResources.dbc>
3.  This should open a structure of directories in with path: \`/Workspace/scalable-data-science/xtraResources/'

### Running **Spark**

The variable **sc** allows you to access a Spark Context to run your Spark programs. Recall `SparkContext` is in the Driver Program.

![](http://spark.apache.org/docs/latest/img/cluster-overview.png)

**NOTE: Do not create the *sc* variable - it is already initialized for you. **

### We will do the following next:

1.  Create an RDD using `sc.parallelize`

-   Perform the `collect` action on the RDD and find the number of partitions it is made of using `getNumPartitions` action
-   Perform the `take` action on the RDD
-   Transform the RDD by `map` to make another RDD
-   Transform the RDD by `filter` to make another RDD
-   Perform the `reduce` action on the RDD
-   Transform the RDD by `flatMap` to make another RDD
-   Create a Pair RDD
-   Perform some transformations on a Pair RDD
-   Where in the cluster is your computation running?
-   Shipping Closures, Broadcast Variables and Accumulator Variables
-   Spark Essentials: Summary
-   HOMEWORK

### 1. Create an RDD using `sc.parallelize`

First, let us create an RDD of three elements (of integer type `Int`) from a Scala `Seq` (or `List` or `Array`) with two partitions by using the `parallelize` method of the available Spark Context `sc` as follows:

``` scala
val x = sc.parallelize(Array(1, 2, 3), 2)    // <Ctrl+Enter> to evaluate this cell (using 2 partitions)
```

>     x: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[181331] at parallelize at <console>:34

``` scala
x.  // place the cursor after 'x.' and hit Tab to see the methods available for the RDD x we created
```

### 2. Perform the `collect` action on the RDD and find the number of partitions it is made of using `getNumPartitions` action

No action has been taken by `sc.parallelize` above. To see what is "cooked" by the recipe for RDD `x` we need to take an action.

The simplest is the `collect` action which returns all of the elements of the RDD as an `Array` to the driver program and displays it.

*So you have to make sure that all of that data will fit in the driver program if you call `collect` action!*

#### Let us look at the [collect action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/collect) and return here to try out the example codes.

![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-90.png)

Let us perform a `collect` action on RDD `x` as follows:

``` scala
x.collect()    // <Ctrl+Enter> to collect (action) elements of rdd; should be (1, 2, 3)
```

>     res0: Array[Int] = Array(1, 2, 3)

*CAUTION:* `collect` can crash the driver when called upon an RDD with massively many elements.
So, it is better to use other diplaying actions like `take` or `takeOrdered` as follows:

#### Let us look at the [getNumPartitions action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/getNumPartitions) and return here to try out the example codes.

![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-88.png)

``` scala
// <Ctrl+Enter> to evaluate this cell and find the number of partitions in RDD x
x.getNumPartitions 
```

>     res1: Int = 2

We can see which elements of the RDD are in which parition by calling `glom()` before `collect()`.

`glom()` flattens elements of the same partition into an `Array`.

``` scala
x.glom().collect() // glom() flattens elements on the same partition
```

>     res2: Array[Array[Int]] = Array(Array(1), Array(2, 3))

Thus from the output above, `Array[Array[Int]] = Array(Array(1), Array(2, 3))`, we know that `1` is in one partition while `2` and `3` are in another partition.

##### You Try!

Crate an RDD `x` with three elements, 1,2,3, and this time do not specifiy the number of partitions. Then the default number of partitions will be used. Find out what this is for the cluster you are attached to.

The default number of partitions for an RDD depends on the cluster this notebook is attached to among others - see [programming-guide](http://spark.apache.org/docs/latest/programming-guide.html).

``` scala
val x = sc.parallelize(Seq(1, 2, 3))    // <Shift+Enter> to evaluate this cell (using default number of partitions)
```

``` scala
x.getNumPartitions // <Shift+Enter> to evaluate this cell
```

``` scala
x.glom().collect() // <Ctrl+Enter> to evaluate this cell
```

### 3. Perform the `take` action on the RDD

The `.take(n)` action returns an array with the first `n` elements of the RDD.

``` scala
x.take(2) // Ctrl+Enter to take two elements from the RDD x
```

>     res5: Array[Int] = Array(1, 2)

##### You Try!

Fill in the parenthes `( )` below in order to `take` just one element from RDD `x`.

``` scala
//x.take(  ) // uncomment by removing '//' before x in the cell and fill in the parenthesis to take just one element from RDD x and Cntrl+Enter
```

------------------------------------------------------------------------

### 4. Transform the RDD by `map` to make another RDD

The `map` transformation returns a new RDD that's formed by passing each element of the source RDD through a function (closure). The closure is automatically passed on to the workers for evaluation (when an action is called later).

#### Let us look at the [map transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/map) and return here to try out the example codes.

![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-18.png)

``` scala
// Shift+Enter to make RDD x and RDD y that is mapped from x
val x = sc.parallelize(Array("b", "a", "c")) // make RDD x: [b, a, c]
val y = x.map(z => (z,1))                    // map x into RDD y: [(b, 1), (a, 1), (c, 1)]
```

>     x: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[734] at parallelize at <console>:37
>     y: org.apache.spark.rdd.RDD[(String, Int)] = MapPartitionsRDD[735] at map at <console>:38

``` scala
// Cntrl+Enter to collect and print the two RDDs
println(x.collect().mkString(", "))
println(y.collect().mkString(", "))
```

>     b, a, c
>     (b,1), (a,1), (c,1)

------------------------------------------------------------------------

### 5. Transform the RDD by `filter` to make another RDD

The `filter` transformation returns a new RDD that's formed by selecting those elements of the source RDD on which the function returns `true`.

#### Let us look at the [filter transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/filter) and return here to try out the example codes.

![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-24.png)

``` scala
//Shift+Enter to make RDD x and filter it by (n => n%2 == 1) to make RDD y
val x = sc.parallelize(Array(1,2,3))
// the closure (n => n%2 == 1) in the filter will 
// return True if element n in RDD x has remainder 1 when divided by 2 (i.e., if n is odd)
val y = x.filter(n => n%2 == 1) 
```

>     x: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[768] at parallelize at <console>:37
>     y: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[769] at filter at <console>:40

``` scala
// Cntrl+Enter to collect and print the two RDDs
println(x.collect().mkString(", "))
println(y.collect().mkString(", "))
```

>     1, 2, 3
>     1, 3

------------------------------------------------------------------------

### 6. Perform the `reduce` action on the RDD

Reduce aggregates a data set element using a function (closure). This function takes two arguments and returns one and can often be seen as a binary operator. This operator has to be commutative and associative so that it can be computed correctly in parallel (where we have little control over the order of the operations!).

### Let us look at the [reduce action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/reduce) and return here to try out the example codes.

![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-94.png)

``` scala
//Shift+Enter to make RDD x of inteegrs 1,2,3,4 and reduce it to sum
val x = sc.parallelize(Array(1,2,3,4))
val y = x.reduce((a,b) => a+b)
```

>     x: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[808] at parallelize at <console>:37
>     y: Int = 10

``` scala
//Cntrl+Enter to collect and print RDD x and the Int y, sum of x
println(x.collect.mkString(", "))
println(y)
```

>     1, 2, 3, 4
>     10

### 7. Transform an RDD by `flatMap` to make another RDD

`flatMap` is similar to `map` but each element from input RDD can be mapped to zero or more output elements. Therefore your function should return a sequential collection such as an `Array` rather than a single element as shown below.

### Let us look at the [flatMap transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/flatMap) and return here to try out the example codes.

![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-31.png)

``` scala
//Shift+Enter to make RDD x and flatMap it into RDD by closure (n => Array(n, n*100, 42))
val x = sc.parallelize(Array(1,2,3))
val y = x.flatMap(n => Array(n, n*100, 42))
```

>     x: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[844] at parallelize at <console>:37
>     y: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[845] at flatMap at <console>:38

``` scala
//Cntrl+Enter to collect and print RDDs x and y
println(x.collect().mkString(", "))
println(y.collect().mkString(", "))
```

>     1, 2, 3
>     1, 100, 42, 2, 200, 42, 3, 300, 42

### 8. Create a Pair RDD

Let's next work with RDD of `(key,value)` pairs called a *Pair RDD* or *Key-Value RDD*.

``` scala
// Cntrl+Enter to make RDD words and display it by collect
val words = sc.parallelize(Array("a", "b", "a", "a", "b", "b", "a", "a", "a", "b", "b"))
words.collect()
```

>     words: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[859] at parallelize at <console>:35
>     res13: Array[String] = Array(a, b, a, a, b, b, a, a, a, b, b)

Let's make a Pair RDD called `wordCountPairRDD` that is made of (key,value) pairs with key=word and value=1 in order to encode each occurrence of each word in the RDD `words`, as follows:

``` scala
// Cntrl+Enter to make and collect Pair RDD wordCountPairRDD
val wordCountPairRDD = words.map(s => (s, 1))
wordCountPairRDD.collect()
```

>     wordCountPairRDD: org.apache.spark.rdd.RDD[(String, Int)] = MapPartitionsRDD[867] at map at <console>:37
>     res14: Array[(String, Int)] = Array((a,1), (b,1), (a,1), (a,1), (b,1), (b,1), (a,1), (a,1), (a,1), (b,1), (b,1))

### 9. Perform some transformations on a Pair RDD

Let's next work with RDD of `(key,value)` pairs called a *Pair RDD* or *Key-Value RDD*.

Now some of the Key-Value transformations that we could perform include the following. \* **`reduceByKey` transformation** \* which takes an RDD and returns a new RDD of key-value pairs, such that: \* the values for each key are aggregated using the given reduced function \* and the reduce function has to be of the type that takes two values and returns one value. \* **`sortByKey` transformation** \* this returns a new RDD of key-value pairs that's sorted by keys in ascending order \* **`groupByKey` transformation** \* this returns a new RDD consisting of key and iterable-valued pairs.

Let's see some concrete examples next.

![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-44.png)

``` scala
// Cntrl+Enter to reduceByKey and collect wordcounts RDD
//val wordcounts = wordCountPairRDD.reduceByKey( _ + _ )
val wordcounts = wordCountPairRDD.reduceByKey( (v1,v2) => v1+v2 )
wordcounts.collect()
```

>     wordcounts: org.apache.spark.rdd.RDD[(String, Int)] = ShuffledRDD[908] at reduceByKey at <console>:42
>     res16: Array[(String, Int)] = Array((a,6), (b,5))

Now, let us do just the crucial steps and avoid collecting intermediate RDDs (something we should avoid for large datasets anyways, as they may not fit in the driver program).

``` scala
//Cntrl+Enter to make words RDD and do the word count in two lines
val words = sc.parallelize(Array("a", "b", "a", "a", "b", "b", "a", "a", "a", "b", "b"))
val wordcounts = words.map(s => (s, 1)).reduceByKey(_ + _).collect() 
```

>     words: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[914] at parallelize at <console>:37
>     wordcounts: Array[(String, Int)] = Array((a,6), (b,5))

##### You Try!

You try evaluating `sortByKey()` which will make a new RDD that consists of the elements of the original pair RDD that are sorted by Keys.

``` scala
// Shift+Enter and comprehend code
val words = sc.parallelize(Array("a", "b", "a", "a", "b", "b", "a", "a", "a", "b", "b"))
val wordCountPairRDD = words.map(s => (s, 1))
val wordCountPairRDDSortedByKey = wordCountPairRDD.sortByKey()
```

``` scala
wordCountPairRDD.collect() // Shift+Enter and comprehend code
```

``` scala
wordCountPairRDDSortedByKey.collect() // Cntrl+Enter and comprehend code
```

The next key value transformation we will see is `groupByKey`

When we apply the `groupByKey` transformation to `wordCountPairRDD` we end up with a new RDD that contains two elements. The first element is the tuple `b` and an iterable `CompactBuffer(1,1,1,1,1)` obtained by grouping the value `1` for each of the five key value pairs `(b,1)`. Similarly the second element is the key `a` and an iterable `CompactBuffer(1,1,1,1,1,1)` obtained by grouping the value `1` for each of the six key value pairs `(a,1)`.

*CAUTION*: `groupByKey` can cause a large amount of data movement across the network. It also can create very large iterables at a worker. Imagine you have an RDD where you have 1 billion pairs that have the key `a`. All of the values will have to fit in a single worker if you use group by key. So instead of a group by key, consider using reduced by key.

![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-45.png)

``` scala
val wordCountPairRDDGroupByKey = wordCountPairRDD.groupByKey() // <Shift+Enter> CAUTION: this transformation can be very wide!
```

>     wordCountPairRDDGroupByKey: org.apache.spark.rdd.RDD[(String, Iterable[Int])] = ShuffledRDD[984] at groupByKey at <console>:38

``` scala
wordCountPairRDDGroupByKey.collect()  // Cntrl+Enter
```

>     res19: Array[(String, Iterable[Int])] = Array((a,CompactBuffer(1, 1, 1, 1, 1, 1)), (b,CompactBuffer(1, 1, 1, 1, 1)))

### 10. Where in the cluster is your computation running?

``` scala
val list = 1 to 10
var sum = 0
list.map(x => sum = sum + x)
print(sum)
```

>     55list: scala.collection.immutable.Range.Inclusive = Range(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
>     sum: Int = 55

``` scala
val rdd = sc.parallelize(1 to 10)
var sum = 0
```

>     rdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[358776] at parallelize at <console>:34
>     sum: Int = 0

### 11. Shipping Closures, Broadcast Variables and Accumulator Variables

#### Closures, Broadcast and Accumulator Variables

**(watch now 2:06)**:

[![Closures, Broadcast and Accumulators by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/I9Zcr4R35Ao/0.jpg)](https://www.youtube.com/watch?v=I9Zcr4R35Ao?rel=0&autoplay=1&modestbranding=1)

We will use these variables in the sequel.

#### SUMMARY

Spark automatically creates closures \* for functions that run on RDDs at workers, \* and for any global variables that are used by those workers \* one closure per worker is sent with every task \* and there's no communication between workers \* closures are one way from the driver to the worker \* any changes that you make to the global variables at the workers \* are not sent to the driver or \* are not sent to other workers.

The problem we have is that these closures \* are automatically created are sent or re-sent with every job \* with a large global variable it gets inefficient to send/resend lots of data to each worker \* we cannot communicate that back to the driver

To do this, Spark provides shared variables in two different types. \* **broadcast variables** \* lets us to efficiently send large read-only values to all of the workers \* these are saved at the workers for use in one or more Spark operations.
\* **accumulator variables** \* These allow us to aggregate values from workers back to the driver. \* only the driver can access the value of the accumulator \* for the tasks, the accumulators are basically write-only

------------------------------------------------------------------------

\#\#\# 12. Spark Essentials: Summary **(watch now: 0:29)**

[![Spark Essentials Summary by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/F50Vty9Ia8Y/0.jpg)](https://www.youtube.com/watch?v=F50Vty9Ia8Y?rel=0&autoplay=1&modestbranding=1)

*NOTE:* In databricks cluster, we (the course coordinator/administrators) set the number of workers for you.

### 13. HOMEWORK

See the notebook in this folder named `005_RDDsTransformationsActionsHOMEWORK`. This notebook will give you more examples of the operations above as well as others we will be using later, including: \* Perform the `takeOrdered` action on the RDD \* Transform the RDD by `distinct` to make another RDD and \* Doing a bunch of transformations to our RDD and performing an action in a single cell.

------------------------------------------------------------------------

------------------------------------------------------------------------

### **Importing Standard Scala and Java libraries**

-   For other libraries that are not available by default, you can upload other libraries to the Workspace.
-   Refer to the **[Libraries](https://docs.databricks.com/user-guide/libraries.html)** guide for more details.

``` scala
import scala.math._
val x = min(1, 10)
```

>     import scala.math._
>     x: Int = 1

``` scala
import java.util.HashMap
val map = new HashMap[String, Int]()
map.put("a", 1)
map.put("b", 2)
map.put("c", 3)
map.put("d", 4)
map.put("e", 5)
```

>     import java.util.HashMap
>     map: java.util.HashMap[String,Int] = {a=1, b=2, c=3, d=4, e=5}
>     res9: Int = 0

``` scala

      %md
### Let us look at the legend and overview of the visual RDD Api by doing the following first:


![](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/db/visualapi/med/visualapi-1.png)
```

``` scala
val rdd1 = rdd.map(x => sum = sum + x)
```

>     rdd1: org.apache.spark.rdd.RDD[Unit] = MapPartitionsRDD[358778] at map at <console>:38

``` scala
rdd1.collect()
```

>     res24: Array[Unit] = Array((), (), (), (), (), (), (), (), (), ())

``` scala
val rdd1 = rdd.map(x => {var sum = 0;
                         sum = sum + x
                         sum}
                  )
```

>     rdd1: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[358779] at map at <console>:38

``` scala
rdd1.collect()
```

>     res25: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

