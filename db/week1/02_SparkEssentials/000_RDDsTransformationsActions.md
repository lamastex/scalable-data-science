// Databricks notebook source exported at Sat, 13 Feb 2016 20:10:06 UTC


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





## The Abstraction of Resilient Distributed Dataset (RDD)
**(watch now 2:26)**:

[![RDD in Spark by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/3nreQ1N7Jvk/0.jpg)](https://www.youtube.com/v/3nreQ1N7Jvk?rel=0&autoplay=1&modestbranding=1&start=1&end=146)

***

## Spark Transformations
**(watch now 1:18)**:

[![Spark Transformations by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/360UHWy052k/0.jpg)](https://www.youtube.com/v/360UHWy052k?rel=0&autoplay=1&modestbranding=1)

***


## Spark Actions
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
* Here are two reference URLs for working with Spark.
    * [The latest Spark programming guide](http://spark.apache.org/docs/latest/programming-guide.html)
    * And the second is the PySpark API.
    
Let us get our hands dirty in Spark implementing these ideas!





### Running **Spark**
The variable **sc** allows you to access a Spark Context to run your Spark programs.
* For more information about Spark, please refer to [Spark Overview](https://spark.apache.org/docs/latest/)

**NOTE: Do not create the *sc* variable - it is already initialized for you. **





**Create an RDD**

First, let us create an RDD of ``Int``s from a Scala ``Seq`` or ``List`` by using the ``parallelize`` method of the available Spark Context ``sc`` as follows:


```scala

val rdd = sc.parallelize(Seq(1, 2, 3, 4))    // <Ctrl+Enter> to evaluate this cell (using default number of partitions)

```



**Perform the ``collect`` action on the RDD**

No action has been taken by ``sc.parallelize`` above.  To see what is "cooked" by the recipe for ``rdd`` we need to take an action.  

The simplest is the ``collect`` action which returns all of the elements of the RDD as an ``Array`` to the driver program and displays it.

*So you have to make sure that all of that data will fit in the driver program if you call ``collect`` action!*


```scala

rdd.collect()    // <Ctrl+Enter> to collect (action) elements of rdd; should be (1, 2, 3, 4)

```



*CAUTION:* ``collect`` can crash the driver when called upon an RDD with massively many elements.  So, it is better to us other diplaying actions like ``take`` or ``takeOrdered`` as follows:





**Perform the ``take`` action on the RDD**

The ``.take(n)`` action returns an array with the first ``n`` elements of the RDD.


```scala

rdd.take(2)

```




**Perform the ``takeOrdered`` action on the RDD**

To illustrate ``take`` and ``takeOrdered`` actions, let's create a bigger RDD named ``rdd0_1000000`` that is made up of a million integers from 0 to 1000000.  
We will ``sc.parallelize`` the ``Seq`` Scala collection by using its ``.range(startInteger,stopInteger)`` method.


```scala

val rdd0_1000000 = sc.parallelize(Seq.range(0, 1000000)) // <Shift+Enter> to create an RDD of million integers: 0,1,2,...,10^6

```
```scala

rdd0_1000000.take(5) // <Ctrl+Enter> gives the first 5 elements of the RDD, (0, 1, 2, 3, 4)

```



``takeordered(n)`` returns ``n`` elements ordered in ascending order (by default) or as specified by the optional key function, as shown below.


```scala

rdd0_1000000.takeOrdered(5) // <Shift+Enter> is same as rdd0_1000000.take(5) 

```
```scala

rdd0_1000000.takeOrdered(5)(Ordering[Int].reverse) // <Ctrl+Enter> to get the last 5 elements of the RDD 999999, 999998, ..., 999995

```



**Transform the RDD by ``filter`` to make another RDD**

the ``filter`` transformation returns a new RDD that's formed by selecting those elements of the source RDD on which the function returns ``true``.
    
Let's declare another ``val`` RDD named ``rddFiltered`` by transforming our first RDD named ``rdd`` via the ``filter`` transformation ``x%2==0`` (of being even). 

This filter transformation based on the closure ``x => x%2==0`` will return ``true`` if the element, modulo two, equals zero. The closure is automatically passed on to the workers for evaluation (when an action is called later). 
So this will take our RDD of (1,2,3,4) and return RDD of (2, 4).


```scala

val rddFiltered = rdd.filter( x => x%2==0 )    // <Ctrl+Enter> to declare rddFiltered from transforming rdd

```
```scala

rddFiltered.collect()    // <Ctrl+Enter> to collect (action) elements of rddFiltered; should be (2, 4)

```



** Transform the RDD by ``map`` to make another RDD**

The ``map`` transformation returns a new RDD that's formed by passing each element of the source RDD through a function (closure). The closure is automatically passed on to the workers for evaluation (when an action is called later). 


```scala

rdd.map( x => x*2) // <Ctrl+Enter> to transform rdd by map that doubles each element

```



To see what's in the transformed RDD, let's perform the actions of ``count`` and ``collect`` on the ``rdd.map( x => x*2)``, the transformation of ``rdd`` by the ``map`` given by the closure ``x => x*2``.


```scala

rdd.map( x => x*2).count()    // <Shift+Enter> to perform count (action) the element of the RDD = 4

```
```scala

rdd.map( x => x*2).collect()    // <Ctrl+Enter> to perform collect (action) to show 2, 4, 6, 8

```



**Perform the ``reduce`` action on the RDD**

Reduce aggregates a data set element using a function (closure). 
This function takes two arguments and returns one and can often be seen as a binary operator. 
This operator has to be commutative and associative so that it can be computed correctly in parallel (where we have little control over the order of the operations!).


```scala

rdd.reduce( (x,y)=>x+y ) // <Ctrl+Enter> to do reduce (action) to sum and return Int 10

```
```scala

rdd.reduce( _ + _ )    // <Ctrl+Enter> to do same sum as above and return Int 10 (undescore syntax)

```
```scala

rdd.reduce( (x,y)=>x*y ) // <Ctrl+Enter> to do reduce (action) to multiply and return Int 24

```
```scala

rdd0_1000000.reduce( (x,y)=>x+y ) // <Ctrl+Enter> to do reduce (action) to sum and return Int 1783293664

```
```scala

// the following correctly returns Int = 0 although for wrong reason 
// we have flowed out of Int's numeric limits!!! (but got lucky with 0*x=0 for any Int x)
// <Shift+Enter> to do reduce (action) to multiply and return Int = 0
rdd0_1000000.reduce( (x,y)=>x*y ) 

```
```scala

// <Ctrl+Enter> to do reduce (action) to multiply 1*2*...*9*10 and return correct answer Int = 3628800
sc.parallelize(Seq.range(1, 11)).reduce( (x,y)=>x*y ) 

```



**CAUTION: Know the limits of your numeric types!**


```scala

// <Ctrl+Enter> to do reduce (action) to multiply 1*2*...*20 and return wrong answer as Int = -2102132736
//  we have overflowed out of Int's in a circle back to negative Ints!!! (rigorous distributed numerics, anyone?)
sc.parallelize(Seq.range(1, 21)).reduce( (x,y)=>x*y ) 

```
```scala

//<Ctrl+Enter> we can accomplish the multiplication using Long Integer types 
// by adding 'L' ro integer values, Scala infers that it is type Long
sc.parallelize(Seq.range(1L, 21L)).reduce( (x,y)=>x*y ) 

```



As the following products over Long Integers indicate, they are limited too!


```scala

 // <Shift+Enter> for wrong answer Long = -8718968878589280256 (due to Long's numeric limits)
sc.parallelize(Seq.range(1L, 61L)).reduce( (x,y)=>x*y )

```
```scala

// <Cntrl+Enter> for wrong answer Long = 0 (due to Long's numeric limits)
sc.parallelize(Seq.range(1L, 100L)).reduce( (x,y)=>x*y ) 

```



**Let us do a bunch of transformations to our RDD and perform an action** 

* start from a Scala ``Seq``,
* ``sc.parallelize`` the list to create an RDD,
* ``filter`` that RDD, creating a new filtered RDD,
* do a ``map`` transformation that maps that RDD to a new mapped RDD,
* and finally, perform a ``reduce`` action to sum the elements in the RDD.

This last ``reduce`` action causes the ``parallelize``, the ``filter``, and the ``map`` transformations to actually be executed, and return a result back to the driver machine.


```scala

sc.parallelize(Seq(1, 2, 3, 4))    // <Ctrl+Enter> will return Array(4, 8)
  .filter(x => x%2==0)             // (2, 4) is the filtered RDD
  .map(x => x*2)                   // (4, 8) is the mapped RDD
  .reduce(_+_)                     // 4+8=12 is the final result from reduce

```



**Transform the RDD by ``distinct`` to make another RDD**

Let's declare another RDD named ``rdd2`` that has some repeated elements to apply the ``distinct`` transformation to it. 
That would give us a new RDD that only contains the distinct elements of the input RDD.


```scala

val rdd2 = sc.parallelize(Seq(4, 1, 3, 2, 2, 2, 3, 4))    // <Ctrl+Enter> to declare rdd2

```



Let's apply the ``distinct`` transformation to ``rdd2`` and have it return a new RDD named ``rdd2Distinct`` that contains the distinct elements of the source RDD ``rdd2``.


```scala

val rdd2Distinct = rdd2.distinct() // <Ctrl+Enter> transformation: distinct gives distinct elements of rdd2

```
```scala

rdd2Distinct.collect()    // <Ctrl+Enter> to collect (action) as Array(4, 2, 1, 3)

```


 
**Transform the RDD by ``flatMap`` to make another RDD**

``flatMap`` is similar to ``map`` but each element from input RDD can be mapped to zero or more output elements. 
Therefore your function should return a sequential collection such as an ``Array``rather than a single element as shown below.


```scala

val rdd = sc. parallelize(Seq(1,2,3)) // <Shift+Enter> to create an RDD of three Int elements 1,2,3

```



Let us pass the ``rdd`` above to a map with a closure that will take in each element ``x`` and return ``Array(x, x+5)``.
So each element of the mapped RDD named ``rddOfArrays`` is an `Array[Int]`, an array of integers.


```scala

// <Shift+Enter> to make RDD of Arrays, i.e., RDD[Array[int]]
val rddOfArrays = rdd.map( x => Array(x, x+5) ) 

```
```scala

rddOfArrays.collect() // <Ctrl+Enter> to see it is RDD[Array[int]] = (Array(1, 6), Array(2, 7), Array(3, 8))

```



Now let's observer what happens when we use ``flatMap`` to transform the same ``rdd`` and create another RDD called ``rddfM``.

Interestingly, ``flatMap`` *flattens* our ``rdd`` by taking each ``Array`` (or sequence in general) and truning it into individual elements.

Thus, we end up with the RDD ``rddfM`` consisting of the elements (1, 6, 2, 7, 3, 8) as shown from the output of ``rddfM.collect`` below.


```scala

val rddfM = rdd.flatMap(x => Array(x, x+5))    // <Shift+Enter> to flatMap the rdd using closure (x => Array(x, x+5))

```
```scala

rddfM.collect    // <Ctrl+Enter> to collect rddfM = (1, 6, 2, 7, 3, 8)

```



**Perform the ``reduceByKey`` action on the RDD**

Let's next look at what happens when we transform an RDD of strings. 

We will learn an extremely useful action called ``reduceByKey`` where reduce operations are only performed on values with the same key from an RDD of ``(key,value)`` pairs.


```scala

val words = sc.parallelize(Array("hello", "world", "goodbye", "hello", "again"))
val wordcounts = words.map(s => (s, 1)).reduceByKey(_ + _).collect() 

```
```scala

// Exercise: Calculate the number of unique words in the "words" RDD here.
// (Hint: The answer should be 4.)

```
```scala

// Exercise: Create an RDD of numbers, and find the mean.

```



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

