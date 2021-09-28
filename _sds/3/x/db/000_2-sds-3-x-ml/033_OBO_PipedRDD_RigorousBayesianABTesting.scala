// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Piped RDDs and Bayesian AB Testing
// MAGIC **Continued with Application to Old Bailey Online Data**

// COMMAND ----------

// MAGIC %md
// MAGIC This is a recall/repeat of `006a_PipedRDD`. After the recall, we continue with applying Bayesian A/B Testing to the data extracted from Old Bailey Online counts of crimes and punishments.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Here we will first take excerpts with minor modifications from the end of **Chapter 12. Resilient Distributed Datasets (RDDs)** of *Spark: The Definitive Guide*:
// MAGIC 
// MAGIC - https://learning.oreilly.com/library/view/spark-the-definitive/9781491912201/ch12.html
// MAGIC 
// MAGIC Next, we will do Bayesian AB Testing using PipedRDDs.

// COMMAND ----------

// MAGIC %md
// MAGIC First, we create the toy RDDs as in *The Definitive Guide*:
// MAGIC 
// MAGIC > # From a Local Collection
// MAGIC 
// MAGIC To create an RDD from a collection, you will need to use the parallelize method on a SparkContext (within a SparkSession). This turns a single node collection into a parallel collection. When creating this parallel collection, you can also explicitly state the number of partitions into which you would like to distribute this array. In this case, we are creating two partitions:

// COMMAND ----------

// in Scala
val myCollection = "Spark The Definitive Guide : Big Data Processing Made Simple"  .split(" ")
val words = spark.sparkContext.parallelize(myCollection, 2)

// COMMAND ----------

// MAGIC %python
// MAGIC # in Python
// MAGIC myCollection = "Spark The Definitive Guide : Big Data Processing Made Simple"\
// MAGIC   .split(" ")
// MAGIC words = spark.sparkContext.parallelize(myCollection, 2)
// MAGIC words

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC > # glom
// MAGIC 
// MAGIC > `glom` is an interesting function that takes every partition in your dataset and converts them to arrays. This can be useful if you’re going to collect the data to the driver and want to have an array for each partition. However, this can cause serious stability issues because if you have large partitions or a large number of partitions, it’s simple to crash the driver.

// COMMAND ----------

// MAGIC %md
// MAGIC Let's use `glom` to see how our `words` are distributed among the two partitions we used explicitly.

// COMMAND ----------

words.glom.collect 

// COMMAND ----------

// MAGIC %python
// MAGIC words.glom().collect()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC > # Checkpointing
// MAGIC > One feature not available in the DataFrame API is the concept of checkpointing. Checkpointing is the act of saving an RDD to disk so that future references to this RDD point to those intermediate partitions on disk rather than recomputing the RDD from its original source. This is similar to caching except that it’s not stored in memory, only disk. This can be helpful when performing iterative computation, similar to the use cases for caching:
// MAGIC 
// MAGIC 
// MAGIC Let's create a directory in `dbfs:///` for checkpointing of RDDs in the sequel. The following `%fs mkdirs /path_to_dir` is a shortcut to create a directory in `dbfs:///` 

// COMMAND ----------

// MAGIC %fs
// MAGIC mkdirs /datasets/ScaDaMaLe/checkpointing/

// COMMAND ----------

spark.sparkContext.setCheckpointDir("dbfs:///datasets/ScaDaMaLe/checkpointing")
words.checkpoint()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC > Now, when we reference this RDD, it will derive from the checkpoint instead of the source data. This can be a helpful optimization.

// COMMAND ----------

// MAGIC %md
// MAGIC ## YouTry
// MAGIC 
// MAGIC Just some more words in `haha_words` with `\n`, the End-Of-Line (EOL) characters, in-place.

// COMMAND ----------

val haha_words = sc.parallelize(Seq("ha\nha", "he\nhe\nhe", "ho\nho\nho\nho"),3)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's use `glom` to see how our `haha_words` are distributed among the partitions

// COMMAND ----------

haha_words.glom.collect

// COMMAND ----------

// MAGIC %md
// MAGIC > # Pipe RDDs to System Commands
// MAGIC 
// MAGIC > The pipe method is probably one of Spark’s more interesting methods. With pipe, you can return an RDD created by piping elements to a forked external process. The resulting RDD is computed by executing the given process once per partition. All elements of each input partition are written to a process’s stdin as lines of input separated by a newline. The resulting partition consists of the process’s stdout output, with each line of stdout resulting in one element of the output partition. A process is invoked even for empty partitions.
// MAGIC 
// MAGIC > The print behavior can be customized by providing two functions.
// MAGIC 
// MAGIC We can use a simple example and pipe each partition to the command wc. Each row will be passed in as a new line, so if we perform a line count, we will get the number of lines, one per partition:

// COMMAND ----------

// MAGIC %md
// MAGIC The following produces a `PipedRDD`:

// COMMAND ----------

val wc_l_PipedRDD = words.pipe("wc -l")

// COMMAND ----------

// MAGIC %python
// MAGIC wc_l_PipedRDD = words.pipe("wc -l")
// MAGIC wc_l_PipedRDD

// COMMAND ----------

// MAGIC %md
// MAGIC Now, we take an action via `collect` to bring the results to the Driver. 
// MAGIC 
// MAGIC NOTE: Be careful what you collect! You can always write the output to parquet of binary files in `dbfs:///` if the returned output is large.

// COMMAND ----------

wc_l_PipedRDD.collect

// COMMAND ----------

// MAGIC %python
// MAGIC wc_l_PipedRDD.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC In this case, we got the number of lines returned by `wc -l` per partition.

// COMMAND ----------

// MAGIC %md
// MAGIC ## YouTry
// MAGIC 
// MAGIC Try to make sense of the next few cells where we do NOT specifiy the number of partitions explicitly and let Spark decide on the number of partitions automatically.

// COMMAND ----------

val haha_words = sc.parallelize(Seq("ha\nha", "he\nhe\nhe", "ho\nho\nho\nho"),3)
haha_words.glom.collect
val wc_l_PipedRDD_haha_words = haha_words.pipe("wc -l")
wc_l_PipedRDD_haha_words.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Do you understand why the above `collect` statement returns what it does?

// COMMAND ----------

val haha_words_again = sc.parallelize(Seq("ha\nha", "he\nhe\nhe", "ho\nho\nho\nho"))
haha_words_again.glom.collect
val wc_l_PipedRDD_haha_words_again = haha_words_again.pipe("wc -l")
wc_l_PipedRDD_haha_words_again.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Did you understand why some of the results are `0` in the last `collect` statement?

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC > # mapPartitions
// MAGIC 
// MAGIC > The previous command revealed that Spark operates on a per-partition basis when it comes to actually executing code. You also might have noticed earlier that the return signature of a map function on an RDD is actually `MapPartitionsRDD`. 
// MAGIC 
// MAGIC Or `ParallelCollectionRDD` in our case.
// MAGIC 
// MAGIC > This is because map is just a row-wise alias for `mapPartitions`, which makes it possible for you to map an individual partition (represented as an iterator). That’s because physically on the cluster we operate on each partition individually (and not a specific row). A simple example creates the value “1” for every partition in our data, and the sum of the following expression will count the number of partitions we have:

// COMMAND ----------

// in Scala
words.mapPartitions(part => Iterator[Int](1)).sum() // 2.0

// COMMAND ----------

// MAGIC %python
// MAGIC # in Python
// MAGIC words.mapPartitions(lambda part: [1]).sum() # 2

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC > Naturally, this means that we operate on a per-partition basis and 
// MAGIC therefore it 
// MAGIC > allows us to perform an operation on that *entire* partition. This is valuable for performing something on an entire subdataset of your RDD. You can gather all values of a partition class or group into one partition and then operate on that entire group using arbitrary functions and controls. An example use case of this would be that you could pipe this through some custom machine learning algorithm and train an individual model for that company’s portion of the dataset. A Facebook engineer has an interesting demonstration of their particular implementation of the pipe operator with a similar use case demonstrated at [Spark Summit East 2017](https://spark-summit.org/east-2017/events/experiences-with-sparks-rdd-apis-for-complex-custom-applications/). 
// MAGIC 
// MAGIC > Other functions similar to `mapPartitions` include `mapPartitionsWithIndex`. With this you specify a function that accepts an index (within the partition) and an iterator that goes through all items within the partition. The partition index is the partition number in your RDD, which identifies where each record in our dataset sits (and potentially allows you to debug). You might use this to test whether your map functions are behaving correctly:

// COMMAND ----------

// in Scala
def indexedFunc(partitionIndex:Int, withinPartIterator: Iterator[String]) = {  withinPartIterator.toList.map(    
  value => s"Partition: $partitionIndex => $value").iterator
                                                                            }
words.mapPartitionsWithIndex(indexedFunc).collect()

// COMMAND ----------

// MAGIC %python
// MAGIC # in Python
// MAGIC def indexedFunc(partitionIndex, withinPartIterator):  
// MAGIC   return ["partition: {} => {}".format(partitionIndex,    x) for x in withinPartIterator]
// MAGIC words.mapPartitionsWithIndex(indexedFunc).collect()

// COMMAND ----------

// MAGIC %md
// MAGIC > # foreachPartition
// MAGIC 
// MAGIC > Although `mapPartitions` needs a return value to work properly, this next function does not. `foreachPartition` simply iterates over all the partitions of the data. The difference is that the function has no return value. This makes it great for doing something with each partition like writing it out to a database. In fact, this is how many data source connectors are written. You can create 
// MAGIC 
// MAGIC your 
// MAGIC 
// MAGIC > own text file source if you want by specifying outputs to the temp directory with a random ID:

// COMMAND ----------

words.foreachPartition { iter =>  
  import java.io._  
  import scala.util.Random  
  val randomFileName = new Random().nextInt()  
  val pw = new PrintWriter(new File(s"/tmp/random-file-${randomFileName}.txt"))  
  while (iter.hasNext) {
    pw.write(iter.next())  
  }  
  pw.close()
}

// COMMAND ----------

// MAGIC %md
// MAGIC > You’ll find these two files if you scan your /tmp directory.
// MAGIC 
// MAGIC You need to scan for the file across all the nodes. As the file may not be in the Driver node's `/tmp/` directory but in those of the executors that hosted the partition.

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd

// COMMAND ----------

// MAGIC %sh
// MAGIC ls /tmp/random-file-*.txt

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Numerically Rigorous Bayesian AB Testing
// MAGIC 
// MAGIC This is an example of Bayesian AB Testing with computer-aided proofs for the posterior samples.
// MAGIC 
// MAGIC The main learning goal for you is to use pipedRDDs to distribute, in an embarassingly paralle way, across all the worker nodes in the Spark cluster an executible `IsIt1or2Coins`.
// MAGIC 
// MAGIC ### What does `IsIt1or2Coins` do?
// MAGIC 
// MAGIC At a very high-level, to understand what `IsIt1or2Coins` does, imagine the following simple experiment.
// MAGIC 
// MAGIC We are given 
// MAGIC 
// MAGIC - the number of heads that result from a first sequence of independent and identical tosses of a coin and then 
// MAGIC - we are given the number of heads that result from a second sequence of independent and identical tosses of a coin  
// MAGIC 
// MAGIC Our decision problem is to do help shed light on whether both sequence of tosses came from the same coin or not (whatever the bias may be).
// MAGIC 
// MAGIC `IsIt1or2Coins` tries to help us decide if the two sequence of coin-tosses are based on one coin with an unknown bias or two coins with different biases.
// MAGIC 
// MAGIC If you are curious about details feel free to see:
// MAGIC 
// MAGIC - Exact Bayesian A/B testing using distributed fault-tolerant Moore rejection sampler, Benny Avelin and Raazesh Sainudiin, Extended Abstract, 2 pages, 2018 [(PDF 104KB)](http://lamastex.org/preprints/20180507_ABTestingViaDistributedMRS.pdf).
// MAGIC - which builds on: An auto-validating, trans-dimensional, universal rejection sampler for locally Lipschitz arithmetical expressions, Raazesh Sainudiin and Thomas York, [Reliable Computing, vol.18, pp.15-54, 2013](http://interval.louisiana.edu/reliable-computing-journal/volume-18/reliable-computing-18-pp-015-054.pdf) ([preprint: PDF 2612KB](http://lamastex.org/preprints/avs_rc_2013.pdf))
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC **See first about `PipedRDDs` excerpt from *Spark The Definitive Guide* earlier.**
// MAGIC 
// MAGIC ### Getting the executible `IsIt1or2Coins` into our Spark Cluster
// MAGIC 
// MAGIC **This has already been done in the project-shard. You need not do it again for this executible!**
// MAGIC 
// MAGIC You need to upload the C++ executible `IsIt1or2Coins` from:
// MAGIC  - https://github.com/lamastex/mrs2
// MAGIC  
// MAGIC 
// MAGIC Here, suppose you have an executible for linux x86 64 bit processor with all dependencies pre-compiled into one executibe.
// MAGIC 
// MAGIC Say this executible is `IsIt10r2Coins`.
// MAGIC 
// MAGIC This executible comes from the following dockerised build:
// MAGIC 
// MAGIC - https://github.com/lamastex/mrs2/tree/master/docker
// MAGIC - by statically compiling inside the docerised environment for mrs2:
// MAGIC   - https://github.com/lamastex/mrs2/tree/master/mrs-2.0/examples/MooreRejSam/IsIt1or2Coins
// MAGIC   
// MAGIC You can replace the executible with any other executible with appropriate I/O to it.
// MAGIC 
// MAGIC Then you upload the executible to databricks' `FileStore`.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Just note the path to the file and DO NOT click `Create Table` or other buttons!
// MAGIC 
// MAGIC ![creenShotOfUploadingStaticExecutibleIsIt1or2CoinsViaFileStore](https://raw.githubusercontent.com/lamastex/scalable-data-science/master/images/2020/ScaDaMaLe/screenShotOfUploadingStaticExecutibleIsIt1or2CoinsViaFileStore.png)

// COMMAND ----------

// MAGIC %fs ls "/FileStore/tables/IsIt1or2Coins"

// COMMAND ----------

// MAGIC %md
// MAGIC Now copy the file from `dbfs://FileStore` that you just uploaded into the local file system of the Driver.

// COMMAND ----------

dbutils.fs.cp("dbfs:/FileStore/tables/IsIt1or2Coins", "file:/tmp/IsIt1or2Coins")

// COMMAND ----------

// MAGIC %sh
// MAGIC ls -al /tmp/IsIt1or2Coins

// COMMAND ----------

// MAGIC %md
// MAGIC Note it is a big static executible with all dependencies inbuilt (it uses GNU Scientific Library and a specialized C++ Library called C-XSC or C Extended for Scientific Computing to do hard-ware optimized rigorous numerical proofs using Interval-Extended Hessian Differentiation Arithmetics over Rounding-Controlled Hardware-Specified Machine Intervals).
// MAGIC 
// MAGIC Just note it is over 6.5MB. Also we need to change the permissions so it is indeed executible.

// COMMAND ----------

// MAGIC %sh
// MAGIC chmod +x /tmp/IsIt1or2Coins

// COMMAND ----------

// MAGIC %md # Usage instructions for IsIt1or2Coins
// MAGIC `./IsIt1or2Coins numboxes numiter seed numtosses1 heads1 numtosses2 heads2 logScale`
// MAGIC - numboxes = Number of boxes for Moore Rejection Sampling (Rigorous von Neumann Rejection Sampler)
// MAGIC - numiter = Number of samples drawn from posterior distribution to estimate the model probabilities
// MAGIC - seed = a random number seed
// MAGIC - numtosses1 = number of tosses for the first coin
// MAGIC - heads1 = number of heads shown up on the first coin
// MAGIC - numtosses2 = number of tosses for the second coin
// MAGIC - heads2 = number of heads shown up on the second coin
// MAGIC - logscale = True/False as Int
// MAGIC 
// MAGIC Don't worry about the details of what the executible `IsIt1or2Coins` is doing for now. Just realise that this executible takes some input on command-line and gives some output. 

// COMMAND ----------

// MAGIC %md
// MAGIC Let's make sure the executible takes input and returns output string on the Driver node.

// COMMAND ----------

// MAGIC %sh
// MAGIC /tmp/IsIt1or2Coins 1000 100 234565432 1000 500 1200 600 1

// COMMAND ----------

// MAGIC %sh
// MAGIC 
// MAGIC # You can also do it like this
// MAGIC 
// MAGIC /dbfs/FileStore/tables/IsIt1or2Coins 1000 100 234565432 1000 500 1200 600 1

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Moving the executables to the worker nodes

// COMMAND ----------

// MAGIC %md
// MAGIC To copy the executible from `dbfs` to the local drive of each executor you can use the following helper function.

// COMMAND ----------

import scala.sys.process._
import scala.concurrent.duration._
// from Ivan Sadikov

def copyFile(): Unit = {
  "mkdir -p /tmp/executor/bin".!!
  "cp /dbfs/FileStore/tables/IsIt1or2Coins /tmp/executor/bin/".!!
}

sc.runOnEachExecutor(copyFile, new FiniteDuration(1, HOURS))

// COMMAND ----------

// MAGIC %md
// MAGIC Now, let us use piped RDDs via `bash` to execute the given command in each partition as follows:

// COMMAND ----------

val input = Seq("/tmp/executor/bin/IsIt1or2Coins 1000 100 234565432 1000 500 1200 600 1", "/tmp/executor/bin/IsIt1or2Coins 1000 100 234565432 1000 500 1200 600 1")

val output = sc
  .parallelize(input)
  .repartition(2)
  .pipe("bash")
  .collect()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC In fact, you can just use `DBFS FUSE` to run the commands without any file copy in databricks-provisioned Spark clusters we are on here:

// COMMAND ----------

val isIt1or2StaticExecutible = "/dbfs/FileStore/tables/IsIt1or2Coins"
val same_input = Seq(s"$isIt1or2StaticExecutible 1000 100 234565432 1000 500 1200 600 1", 
                     s"$isIt1or2StaticExecutible 1000 100 234565432 1000 500 1200 600 1")

val same_output = sc
  .parallelize(same_input)
  .repartition(2)
  .pipe("bash")
  .collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Thus by mixing several different executibles that are statically compiled for linux 64 bit machine, we can mix and match multiple executibles with appropriate inputs.
// MAGIC 
// MAGIC The resulting outputs can themselves be re-processed in Spark to feed into toher pipedRDDs or normal RDDs or DataFrames and DataSets.

// COMMAND ----------

// MAGIC %md
// MAGIC Finally, we can have more than one command per partition and then use `mapPartitions` to send all the executible commands within the input partition that is to be run by the executor in which that partition resides as follows:

// COMMAND ----------

val isIt1or2StaticExecutible = "/dbfs/FileStore/tables/IsIt1or2Coins"

// let us make 2 commands in each of the 2 input partitions
val same_input_mp = Seq(s"$isIt1or2StaticExecutible 1000 100 234565432 1000 500 1200 600 1", 
                        s"$isIt1or2StaticExecutible 1000 100 123456789 1000 500 1200 600 1",
                        s"$isIt1or2StaticExecutible 1000 100 123456789 1000 500 1200 600 1",
                        s"$isIt1or2StaticExecutible 1000 100 234565432 1000 500 1200 600 1")

val same_output_mp = sc
  .parallelize(same_input)
  .repartition(2)
  .pipe("bash")
  .mapPartitions(x => Seq(x.mkString("\n")).iterator)
  .collect()

// COMMAND ----------

// MAGIC %md allCatch is a useful tool to use as a filtering function when testing if a command will work without error.

// COMMAND ----------

import scala.util.control.Exception.allCatch
(allCatch opt " 12 ".trim.toLong).isDefined

// COMMAND ----------

// MAGIC %md
// MAGIC The following should only be done after you have been introduced to Notebook: `033_OBO_PipedRDD_RigorousBayesianABTesting` and the Old Bailey Online Data.
// MAGIC 
// MAGIC **TODO**: The below needs redo with DBFS FUSE.

// COMMAND ----------

// MAGIC %md # Parsing the output from `IsIt1or2Coins`

// COMMAND ----------

/**
 * Returns the label proportions from the output of IsIt1or2Coins
 *
 * This function takes an array of Strings, where each element
 * contains the whole output of one execution of IsIt1or2Coins.
 * It returns an Array[Array[Double]], where the first index denotes which
 * execution it belonged to, the second index is whether it is label0 or label1
 * i.e. it is a numExec x 2 array.
 */

def getLabelProps(input : Array[String]):Array[Array[Double]] = {
  input
  .map(out => out
       .split("\n")
       .filter(line => line.contains("label:"))
       .map(filtLine => filtLine
            .split(" ")
            .filter(line => (allCatch opt line.trim.toDouble).isDefined)
            .map(filtFiltLine => filtFiltLine.toDouble)))
  .map(trial => trial.map(labels => labels(1)))
}

/**
 * Returns the label means from the output of IsIt1or2Coins
 *
 * This function takes an array of Strings, where each element
 * contains the whole output of one execution of IsIt1or2Coins.
 * It returns an Array[Array[Double]], where the first index denotes which
 * execution it belonged to, the second index is whether it is label0 Mean 
 * or label1 Mean1 or label1 Mean2, i.e. it is a numExec x 3 array.
 */

def getLabelMeans(input : Array[String]):Array[Array[Double]] = {
  val output_pre = input
  .map(out => out.split("\n")
       .filter(line => (allCatch opt line.trim.toDouble).isDefined))
  .map(arr => arr.map(num => num.toDouble))
  // Some runs have such a low probability for a label that some end up being only 2 in length instead of three
  // That means we should pad with a 0 to fix this
  output_pre.map(trial => if (trial.length == 2) Array(0,trial(0),trial(1)) else trial)
}

// COMMAND ----------

// MAGIC %md # Providing case classes for input and output for easy spark communication

// COMMAND ----------

// MAGIC %scala
// MAGIC import org.apache.spark.sql.DataFrame
// MAGIC 
// MAGIC case class OutputRow(ID:Long, 
// MAGIC                      NumTosses1:Int, NumHeads1:Int,
// MAGIC                      NumTosses2:Int, NumHeads2:Int,
// MAGIC                      Label0Prob:Double,
// MAGIC                      Label1Prob:Double,
// MAGIC                      Label0Mean0:Double, Label0Mean1:Double, 
// MAGIC                      Label1Mean0:Double, Label1Mean1:Double)
// MAGIC 
// MAGIC case class InputOpts(ID:Long,NumBoxes:Int, NumIter:Int, Seed:Long, 
// MAGIC                      NumTosses1:Int, NumHeads1:Int,
// MAGIC                      NumTosses2:Int, NumHeads2:Int, 
// MAGIC                      LogScaling:Int) {
// MAGIC   def toExecutableString:String = {
// MAGIC     "/tmp/IsIt1or2Coins "+Array(NumBoxes, NumIter, Seed, NumTosses1, NumHeads1, NumTosses2, NumHeads2, LogScaling).mkString(" ")
// MAGIC   }
// MAGIC }
// MAGIC 
// MAGIC /**
// MAGIC  * Returns the result of running all trials in the array of InputOpts
// MAGIC  *
// MAGIC  * This function takes an Array[InputOpts], creates executable strings
// MAGIC  * via suckAs and runs all in a pipedRDD, after it creates this it will parse the output
// MAGIC  * and assemble it into the case class OutputRow, i.e. it returns an Array of OutputRow
// MAGIC  */
// MAGIC 
// MAGIC def execute(trials : Array[InputOpts]) = {
// MAGIC   sc.parallelize(trials.map(trial => trial.toExecutableString))
// MAGIC     .repartition(trials.length)
// MAGIC     .pipe("/tmp/suckAs.sh")
// MAGIC     .mapPartitions(x => Seq(x.mkString("\n").split("theSeed")).iterator) // We know that theSeed is included once in every output
// MAGIC     .collect
// MAGIC     .flatMap(x => x.filter(y => y.length > 0)) // Since each collection of outputs are split at theSeed we need to remove all empty strings and flatmap
// MAGIC }
// MAGIC 
// MAGIC def parseOutput(trials: Array[InputOpts], res:Array[String]) = {
// MAGIC   val labProp = getLabelProps(res)
// MAGIC   val labMean = getLabelMeans(res)
// MAGIC   (trials zip labProp zip labMean)
// MAGIC   .map(trial => (trial._1._1,trial._1._2,trial._2))
// MAGIC   .map(trial => OutputRow(trial._1.ID,
// MAGIC                           trial._1.NumTosses1,trial._1.NumHeads1,
// MAGIC                           trial._1.NumTosses2,trial._1.NumHeads2,
// MAGIC                           trial._2(0),trial._2(1),
// MAGIC                           trial._3(0),trial._3(0),
// MAGIC                           trial._3(1),trial._3(2)
// MAGIC                          ))
// MAGIC }
// MAGIC 
// MAGIC /* Returns a DataFrame of the Array[OutputRow] for ease of displaying in Databricks */
// MAGIC 
// MAGIC def resultAsDF(result:Array[OutputRow]):DataFrame = {
// MAGIC   sc.parallelize(result).toDF
// MAGIC }

// COMMAND ----------

// MAGIC %scala
// MAGIC val inputOpts = Array(InputOpts(1680,1000,1000,100,792,245,151,63,1), InputOpts(1690,1000,1000,100,1805,526,215,68,1), InputOpts(1700,1000,1000,100,1060,242,57,26,1),InputOpts(1710,1000,1000,100,1060,243,57,26,1),InputOpts(1720,1000,1000,100,1060,245,57,26,1),InputOpts(1730,1000,1000,100,1060,245,57,26,1))
// MAGIC val res = execute(inputOpts)

// COMMAND ----------

// MAGIC %scala
// MAGIC display(resultAsDF(parseOutput(inputOpts,res)))

// COMMAND ----------

