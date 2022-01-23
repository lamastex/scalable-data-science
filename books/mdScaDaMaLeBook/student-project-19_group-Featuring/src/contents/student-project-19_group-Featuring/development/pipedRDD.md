<div class="cell markdown">

[ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)
==============================================================================================================================

</div>

<div class="cell markdown">

Piped RDDs and Bayesian AB Testing
==================================

</div>

<div class="cell markdown">

Here we will first take excerpts with minor modifications from the end of **Chapter 12. Resilient Distributed Datasets (RDDs)** of *Spark: The Definitive Guide*:

-   https://learning.oreilly.com/library/view/spark-the-definitive/9781491912201/ch12.html

Next, we will do Bayesian AB Testing using PipedRDDs.

</div>

<div class="cell markdown">

First, we create the toy RDDs as in *The Definitive Guide*:

> From a Local Collection
> =======================

To create an RDD from a collection, you will need to use the parallelize method on a SparkContext (within a SparkSession). This turns a single node collection into a parallel collection. When creating this parallel collection, you can also explicitly state the number of partitions into which you would like to distribute this array. In this case, we are creating two partitions:

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// in Scala
val myCollection = "Spark The Definitive Guide : Big Data Processing Made Simple"  .split(" ")
val words = spark.sparkContext.parallelize(myCollection, 2)
```

<div class="output execute_result plain_result" execution_count="1">

    myCollection: Array[String] = Array(Spark, The, Definitive, Guide, :, Big, Data, Processing, Made, Simple)
    words: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[0] at parallelize at command-1173833896357740:3

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# in Python
myCollection = "Spark The Definitive Guide : Big Data Processing Made Simple"\
  .split(" ")
words = spark.sparkContext.parallelize(myCollection, 2)
words
```

</div>

<div class="cell markdown">

> glom
> ====

> `glom` is an interesting function that takes every partition in your dataset and converts them to arrays. This can be useful if you’re going to collect the data to the driver and want to have an array for each partition. However, this can cause serious stability issues because if you have large partitions or a large number of partitions, it’s simple to crash the driver.

</div>

<div class="cell markdown">

Let's use `glom` to see how our `words` are distributed among the two partitions we used explicitly.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
words.glom.collect 
```

<div class="output execute_result plain_result" execution_count="1">

    res0: Array[Array[String]] = Array(Array(Spark, The, Definitive, Guide, :), Array(Big, Data, Processing, Made, Simple))

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
words.glom().collect()
```

</div>

<div class="cell markdown">

> Checkpointing
> =============
>
> One feature not available in the DataFrame API is the concept of checkpointing. Checkpointing is the act of saving an RDD to disk so that future references to this RDD point to those intermediate partitions on disk rather than recomputing the RDD from its original source. This is similar to caching except that it’s not stored in memory, only disk. This can be helpful when performing iterative computation, similar to the use cases for caching:

Let's create a directory in `dbfs:///` for checkpointing of RDDs in the sequel. The following `%fs mkdirs /path_to_dir` is a shortcut to create a directory in `dbfs:///`

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` fs
mkdirs /datasets/ScaDaMaLe/checkpointing/
```

<div class="output execute_result plain_result" execution_count="1">

    res1: Boolean = true

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
spark.sparkContext.setCheckpointDir("dbfs:///datasets/ScaDaMaLe/checkpointing")
words.checkpoint()
```

</div>

<div class="cell markdown">

> Now, when we reference this RDD, it will derive from the checkpoint instead of the source data. This can be a helpful optimization.

</div>

<div class="cell markdown">

YouTry
------

Just some more words in `haha_words` with `\n`, the End-Of-Line (EOL) characters, in-place.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val haha_words = sc.parallelize(Seq("ha\nha", "he\nhe\nhe", "ho\nho\nho\nho"),3)
```

<div class="output execute_result plain_result" execution_count="1">

    haha_words: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[4] at parallelize at command-1173833896357751:1

</div>

</div>

<div class="cell markdown">

Let's use `glom` to see how our `haha_words` are distributed among the partitions

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
haha_words.glom.collect
```

<div class="output execute_result plain_result" execution_count="1">

    res4: Array[Array[String]] =
    Array(Array(ha
    ha), Array(he
    he
    he), Array(ho
    ho
    ho
    ho))

</div>

</div>

<div class="cell markdown">

> Pipe RDDs to System Commands
> ============================

> The pipe method is probably one of Spark’s more interesting methods. With pipe, you can return an RDD created by piping elements to a forked external process. The resulting RDD is computed by executing the given process once per partition. All elements of each input partition are written to a process’s stdin as lines of input separated by a newline. The resulting partition consists of the process’s stdout output, with each line of stdout resulting in one element of the output partition. A process is invoked even for empty partitions.

> The print behavior can be customized by providing two functions.

We can use a simple example and pipe each partition to the command wc. Each row will be passed in as a new line, so if we perform a line count, we will get the number of lines, one per partition:

</div>

<div class="cell markdown">

The following produces a `PipedRDD`:

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val wc_l_PipedRDD = words.pipe("wc -l")
```

<div class="output execute_result plain_result" execution_count="1">

    wc_l_PipedRDD: org.apache.spark.rdd.RDD[String] = PipedRDD[19] at pipe at command-1173833896357756:1

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
wc_l_PipedRDD = words.pipe("wc -l")
wc_l_PipedRDD
```

</div>

<div class="cell markdown">

Now, we take an action via `collect` to bring the results to the Driver.

NOTE: Be careful what you collect! You can always write the output to parquet of binary files in `dbfs:///` if the returned output is large.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
wc_l_PipedRDD.collect
```

<div class="output execute_result plain_result" execution_count="1">

    res9: Array[String] = Array("      5       5      29", "      5       5      32")

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
wc_l_PipedRDD.collect()
```

</div>

<div class="cell markdown">

In this case, we got the number of lines returned by `wc -l` per partition.

</div>

<div class="cell markdown">

YouTry
------

Try to make sense of the next few cells where we do NOT specifiy the number of partitions explicitly and let Spark decide on the number of partitions automatically.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val haha_words = sc.parallelize(Seq("ha\nha", "he\nhe\nhe", "ho\nho\nho\nho"),3)
haha_words.glom.collect
val wc_l_PipedRDD_haha_words = haha_words.pipe("wc -l")
wc_l_PipedRDD_haha_words.collect()
```

<div class="output execute_result plain_result" execution_count="1">

    haha_words: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[8] at parallelize at command-1173833896357763:1
    wc_l_PipedRDD_haha_words: org.apache.spark.rdd.RDD[String] = PipedRDD[10] at pipe at command-1173833896357763:3
    res6: Array[String] = Array(2, 3, 4)

</div>

</div>

<div class="cell markdown">

Do you understand why the above `collect` statement returns what it does?

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val haha_words_again = sc.parallelize(Seq("ha\nha", "he\nhe\nhe", "ho\nho\nho\nho"))
haha_words_again.glom.collect
val wc_l_PipedRDD_haha_words_again = haha_words_again.pipe("wc -l")
wc_l_PipedRDD_haha_words_again.collect()
```

<div class="output execute_result plain_result" execution_count="1">

    haha_words_again: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[11] at parallelize at command-1173833896357765:1
    wc_l_PipedRDD_haha_words_again: org.apache.spark.rdd.RDD[String] = PipedRDD[13] at pipe at command-1173833896357765:3
    res7: Array[String] = Array(0, 0, 2, 0, 0, 3, 0, 4)

</div>

</div>

<div class="cell markdown">

Did you understand why some of the results are `0` in the last `collect` statement?

</div>

<div class="cell markdown">

> mapPartitions
> =============

> The previous command revealed that Spark operates on a per-partition basis when it comes to actually executing code. You also might have noticed earlier that the return signature of a map function on an RDD is actually `MapPartitionsRDD`.

Or `ParallelCollectionRDD` in our case.

> This is because map is just a row-wise alias for `mapPartitions`, which makes it possible for you to map an individual partition (represented as an iterator). That’s because physically on the cluster we operate on each partition individually (and not a specific row). A simple example creates the value “1” for every partition in our data, and the sum of the following expression will count the number of partitions we have:

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// in Scala
words.mapPartitions(part => Iterator[Int](1)).sum() // 2.0
```

<div class="output execute_result plain_result" execution_count="1">

    res8: Double = 2.0

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` python
# in Python
words.mapPartitions(lambda part: [1]).sum() # 2
```

</div>

<div class="cell markdown">

> Naturally, this means that we operate on a per-partition basis and therefore it allows us to perform an operation on that *entire* partition. This is valuable for performing something on an entire subdataset of your RDD. You can gather all values of a partition class or group into one partition and then operate on that entire group using arbitrary functions and controls. An example use case of this would be that you could pipe this through some custom machine learning algorithm and train an individual model for that company’s portion of the dataset. A Facebook engineer has an interesting demonstration of their particular implementation of the pipe operator with a similar use case demonstrated at [Spark Summit East 2017](https://spark-summit.org/east-2017/events/experiences-with-sparks-rdd-apis-for-complex-custom-applications/).

> Other functions similar to `mapPartitions` include `mapPartitionsWithIndex`. With this you specify a function that accepts an index (within the partition) and an iterator that goes through all items within the partition. The partition index is the partition number in your RDD, which identifies where each record in our dataset sits (and potentially allows you to debug). You might use this to test whether your map functions are behaving correctly:

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
