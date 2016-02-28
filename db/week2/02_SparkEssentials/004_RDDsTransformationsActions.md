// Databricks notebook source exported at Sun, 28 Feb 2016 05:21:14 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





# **Introduction to Spark through Scala Notebook** 

* This introduction notebook describes how to get started running Spark (Scala) code in Notebooks.
* Working with Spark's Resilient Distributed Datasets (RDDs)
  * creating RDDs
  * performing basic transformations on RDDs
  * performing basic actions on RDDs





# Spark Cluster Overview:
## Driver Program, Cluster Manager and Worker Nodes

The *driver* does the following:
1. connects to a *cluster manager* to allocate resources across applications
* acquire *executors* on cluster nodes
  * executor processs run compute tasks and cache data in memory or disk on a *worker node*
* sends *application* (user program built on Spark) to the executors
* sends *tasks* for the executors to run
  * task is a unit of work that will sent to one executor
  
![](http://spark.apache.org/docs/latest/img/cluster-overview.png)

See [http://spark.apache.org/docs/latest/cluster-overview.html](http://spark.apache.org/docs/latest/cluster-overview.html) for an overview of the spark cluster. This is embeded in-place below for convenience. Scroll to the bottom to see a Glossary of terms used above and their meanings. You can right-click inside the embedded html ``<frame>...</frame>`` and use the left and right arrows to navigate within it!


```scala

%run "/scalable-data-science/xtraResources/support/sdsFunctions"

```
```scala

displayHTML(frameIt("http://spark.apache.org/docs/latest/cluster-overview.html",700))

```



## The Abstraction of Resilient Distributed Dataset (RDD)

#### RDD is a fault-tolerant collection of elements that can be operated on in parallel

#### Two types of Operations are possible on an RDD
* Transformations
* Actions

**(watch now 2:26)**:

[![RDD in Spark by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/3nreQ1N7Jvk/0.jpg)](https://www.youtube.com/v/3nreQ1N7Jvk?rel=0&autoplay=1&modestbranding=1&start=1&end=146)

***

## Transformations
**(watch now 1:18)**:

[![Spark Transformations by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/360UHWy052k/0.jpg)](https://www.youtube.com/v/360UHWy052k?rel=0&autoplay=1&modestbranding=1)

***


## Actions
**(watch now 0:48)**:

[![Spark Actions by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/F2G4Wbc5ZWQ/0.jpg)](https://www.youtube.com/v/F2G4Wbc5ZWQ?rel=0&autoplay=1&modestbranding=1&start=1&end=48)

***

**Key Points**
* Resilient distributed datasets (RDDs) are the primary abstraction in Spark.
* RDDs are immutable once created:
    * can transform it.
    * can perform actions on it.
    * but cannot change an RDD once you construct it.
* Spark tracks each RDD's lineage information or recipe to enable its efficient recomputation if a machine fails.
* RDDs enable operations on collections of elements in parallel.
* We can construct RDDs by:
    * parallelizing Scala collections such as lists or arrays
    * by transforming an existing RDD,
    * from files in distributed file systems such as (HDFS, S3, etc.).
* We can specify the number of partitions for an RDD
* The more partitions in an RDD, the more opportunities for parallelism
* There are **two types of operations** you can perform on an RDD:
    * **transformations** (are lazily evaluated) 
      * map
      * flatMap
      * filter
      * distinct
      * ...
    * **actions** (actual evaluation happens)
      * count
      * reduce
      * take
      * collect
      * takeOrdered
      * ...
* Spark transformations enable us to create new RDDs from an existing RDD.
* RDD transformations are lazy evaluations (results are not computed right away)
* Spark remembers the set of transformations that are applied to a base data set (this is the lineage graph of RDD) 
* The allows Spark to automatically recover RDDs from failures and slow workers.
* The lineage graph is a recipe for creating a result and it can be optimized before execution.
* A transformed RDD is executed only when an action runs on it.
* You can also persist, or cache, RDDs in memory or on disk (this speeds up iterative ML algorithms that transforms the initial RDD iteratively).
* Here is a great reference URL for working with Spark.
    * [The latest Spark programming guide](http://spark.apache.org/docs/latest/programming-guide.html) and it is embedded below in-place for your convenience.
    


```scala

displayHTML(frameIt("http://spark.apache.org/docs/latest/programming-guide.html",800))

```



# Let us get our hands dirty in Spark implementing these ideas!





### Let us look at the [legend and overview of the visual RDD Api](/#workspace/scalable-data-science/xtraResources/visualRDDApi/guide).

![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-1.png)





### Running **Spark**
The variable **sc** allows you to access a Spark Context to run your Spark programs.
Recall ``SparkContext`` is in the Driver Program.

![](http://spark.apache.org/docs/latest/img/cluster-overview.png)

**NOTE: Do not create the *sc* variable - it is already initialized for you. **





### We will do the following next:
1. Create an RDD
* Perform the ``collect`` action on the RDD
* Perform the ``take`` action on the RDD
* Transform the RDD by ``map`` to make another RDD
* Transform the RDD by ``filter`` to make another RDD
* Perform the ``reduce`` action on the RDD
* Transform the RDD by ``flatMap`` to make another RDD
* Perform the ``reduceByKey`` action on the RDD
* HOMEWORK





### 1. Create an RDD

First, let us create an RDD of numbers (of integer type ``Int``) from a Scala ``Seq`` or ``List`` by using the ``parallelize`` method of the available Spark Context ``sc`` as follows:


```scala

val x = sc.parallelize(Seq(1, 2, 3))    // <Ctrl+Enter> to evaluate this cell (using default number of partitions)

```



### 2. Perform the `collect` action on the RDD

No action has been taken by ``sc.parallelize`` above.  To see what is "cooked" by the recipe for RDD ``x`` we need to take an action.  

The simplest is the ``collect`` action which returns all of the elements of the RDD as an ``Array`` to the driver program and displays it.

*So you have to make sure that all of that data will fit in the driver program if you call ``collect`` action!*





#### Let us look at the [collect action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/collect).

![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-90.png)


```scala

x.collect()    // <Ctrl+Enter> to collect (action) elements of rdd; should be (1, 2, 3)

```



*CAUTION:* ``collect`` can crash the driver when called upon an RDD with massively many elements.  
So, it is better to use other diplaying actions like ``take`` or ``takeOrdered`` as follows:





### 3. Perform the `take` action on the RDD

The ``.take(n)`` action returns an array with the first ``n`` elements of the RDD.


```scala

x.take(2)

```



***

### 4. Transform the RDD by ``map`` to make another RDD

The ``map`` transformation returns a new RDD that's formed by passing each element of the source RDD through a function (closure). The closure is automatically passed on to the workers for evaluation (when an action is called later). 





#### Let us look at the [map transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/map).

![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-18.png)


```scala

val x = sc.parallelize(Array("b", "a", "c"))
val y = x.map(z => (z,1))

```
```scala

println(x.collect().mkString(", "))
println(y.collect().mkString(", "))

```



***

### 5. Transform the RDD by ``filter`` to make another RDD

The ``filter`` transformation returns a new RDD that's formed by selecting those elements of the source RDD on which the function returns ``true``.





#### Let us look at the [filter transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/filter).

![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-24.png)


```scala

val x = sc.parallelize(Array(1,2,3))
val y = x.filter(n => n%2 == 1)

```
```scala

println(x.collect().mkString(", "))
println(y.collect().mkString(", "))

```



***
### 6. Perform the ``reduce`` action on the RDD

Reduce aggregates a data set element using a function (closure). 
This function takes two arguments and returns one and can often be seen as a binary operator. 
This operator has to be commutative and associative so that it can be computed correctly in parallel (where we have little control over the order of the operations!).





### Let us look at the [reduce action in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/actions/reduce).

![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-94.png)


```scala

val x = sc.parallelize(Array(1,2,3,4))
val y = x.reduce((a,b) => a+b)

```
```scala

println(x.collect.mkString(", "))
println(y)

```



### 7. Transform an RDD by ``flatMap`` to make another RDD

``flatMap`` is similar to ``map`` but each element from input RDD can be mapped to zero or more output elements. 
Therefore your function should return a sequential collection such as an ``Array`` rather than a single element as shown below.





### Let us look at the [flatMap transformation in detail](/#workspace/scalable-data-science/xtraResources/visualRDDApi/recall/transformations/flatMap).

![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-31.png)


```scala

val x = sc.parallelize(Array(1,2,3))
val y = x.flatMap(n => Array(n, n*100, 42))

```
```scala

println(x.collect().mkString(", "))
println(y.collect().mkString(", "))

```



### 8. Perform the ``reduceByKey`` action on the RDD

Let's next look at what happens when we transform an RDD of strings. 

We will learn an extremely useful action called ``reduceByKey`` where reduce operations are only performed on values with the same key from an RDD of ``(key,value)`` pairs.





![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-44.png)


```scala

val words = sc.parallelize(Array("a", "b", "a", "a", "b", "b", "a", "a", "a", "b", "b"))
val wordcounts = words.map(s => (s, 1)).reduceByKey(_ + _).collect() 

```



### 9. HOMEWORK 
See the notebook in this folder named `001_RDDsTransformationsActionsHOMEWORK`. 
This notebook will give you more examples of the operations above as well as others we will be using later, including:
* Perform the ``takeOrdered`` action on the RDD
* Transform the RDD by ``distinct`` to make another RDD and
* Let us do a bunch of transformations to our RDD and perform an action





***
***
### **Importing Standard Scala and Java libraries**
* For other libraries that are not available by default, you can upload other libraries to the Workspace.
* Refer to the **[Libraries](/#workspace/databricks_guide/02 Product Overview/07 Libraries)** guide for more details.


```scala

import scala.math._
val x = min(1, 10)

```
```scala

import java.util.HashMap
val map = new HashMap[String, Int]()
map.put("a", 1)
map.put("b", 2)
map.put("c", 3)
map.put("d", 4)
map.put("e", 5)


```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
