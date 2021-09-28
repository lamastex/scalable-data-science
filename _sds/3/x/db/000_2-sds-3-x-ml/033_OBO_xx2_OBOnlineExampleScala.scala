// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Old Bailey Online Analysis - for time-varying Bayesian Binomial Partition Models
// MAGIC 
// MAGIC Benny Avelin and Raazesh Sainudiin
// MAGIC 
// MAGIC ## Analyse Data

// COMMAND ----------

// MAGIC %md
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

// MAGIC %fs ls "/FileStore/tables/IsIt1or2Coins"

// COMMAND ----------

dbutils.fs.cp("dbfs:/FileStore/tables/IsIt1or2Coins", "file:/tmp/IsIt1or2Coins")

// COMMAND ----------

// MAGIC %sh
// MAGIC chmod +x /tmp/IsIt1or2Coins

// COMMAND ----------

// MAGIC %py
// MAGIC dbutils.fs.put("dbfs:/tmp/suckAs.sh",
// MAGIC """#!/bin/bash
// MAGIC # this script will simply evaluate the string 'sucked in' As is!
// MAGIC # see http://unix.stackexchange.com/questions/61183/bash-script-that-reads-filenames-from-a-pipe-or-from-command-line-args
// MAGIC #http://stackoverflow.com/questions/2355148/run-a-string-as-a-command-within-a-bash-script
// MAGIC #http://tldp.org/LDP/Bash-Beginners-Guide/html/sect_03_04.html#sect_03_04_07
// MAGIC #http://tldp.org/LDP/Bash-Beginners-Guide/html/sect_08_02.html
// MAGIC #http://tldp.org/LDP/Bash-Beginners-Guide/html/sect_08_02.html
// MAGIC #USAGE: echo 'flow-cat bindata/S1.1 | ft2nfdump | nfdump -q -o "fmt: %ts, %sa, %da, %pkt, %byt, %fl"' | ./suckAs.sh
// MAGIC #OUTPUT: 2014-11-01 01:59:45.034,   203.35.135.168,   74.125.237.221,        1,       52,     1
// MAGIC IFS=$'\\n' read -d '' -r -a inputs
// MAGIC #echo "${inputs[0]}"
// MAGIC #echo "${inputs[1]}"
// MAGIC inpLen=${#inputs[@]}
// MAGIC for (( i=0; i<${inpLen}; i++));
// MAGIC do
// MAGIC 	eval "${inputs[i]}"
// MAGIC done
// MAGIC #eval "${inputs[0]}"
// MAGIC """, True)
// MAGIC 
// MAGIC dbutils.fs.cp("dbfs:/tmp/suckAs.sh", "file:/tmp/suckAs.sh")
// MAGIC 
// MAGIC import os
// MAGIC import shutil
// MAGIC   
// MAGIC num_worker_nodes = 3
// MAGIC 
// MAGIC def copyFile(filepath):
// MAGIC   shutil.copyfile("/dbfs%s" % filepath, filepath)
// MAGIC   os.system("chmod u+x %s" % filepath)
// MAGIC   
// MAGIC sc.parallelize(range(0, 2 * (1 + num_worker_nodes))).map(lambda s: copyFile("/tmp/IsIt1or2Coins")).count()
// MAGIC sc.parallelize(range(0, 2 * (1 + num_worker_nodes))).map(lambda s: copyFile("/tmp/suckAs.sh")).count()

// COMMAND ----------

val oboDF = spark.read.format("csv").option("header", "true").option("inferSchema","True").load("dbfs:/tmp/obo.csv")

// COMMAND ----------

import org.apache.spark.sql.functions._

val oboDFWI = oboDF.withColumn("ID0",monotonically_increasing_id)

// COMMAND ----------

display(oboDFWI)

// COMMAND ----------

oboDF.printSchema

// COMMAND ----------

// Goal, create a window function which takes blocks of times and produces a contingency table over death !death and kill !kill
// Step 1: Parse the timestamps so that we have a reasonable way of performing the window functions

// COMMAND ----------

display(oboDF.select($"id").groupBy($"id").count())

// COMMAND ----------

import org.apache.spark.sql.functions._
// This UDF removes the first t in the id string
val removeT = udf((id:String) => id.slice(1,200))
// This UDF takes the remainder id after the time string i.e. tyyyymmdd-id
val getID = udf((id:String) => id.split("-").last)

// COMMAND ----------

val killDeath = oboDFWI
        .filter($"guilty" === true)
        .withColumn("TimeOfEvent",to_timestamp(removeT($"id"),"yyyyMMdd"))
        .withColumn("death",when($"death" > 0, 1).otherwise(0))
        .withColumn("kill",when($"kill" > 0, 1).otherwise(0))
        .withColumn("CrimeID",$"ID0")
        .select($"TimeOfEvent",$"CrimeID",$"death",$"kill")

display(killDeath)

// COMMAND ----------

display(killDeath.orderBy($"TimeOfEvent").filter($"TimeOfEvent" > "1758"))

// COMMAND ----------

// MAGIC %md # Window functions
// MAGIC We have a problem, to construct window functions our windows cannot be longer then 1 month, this is fairly rediculous in our setting since we have very few observations per time unit. Maybe if we extract the year and do it in blocks of 50 years or so?

// COMMAND ----------

val numYears = 2
val killDeathFalse = killDeath
        .withColumn("Killed",$"kill" > 0)
        .filter($"Killed" === false)
        .withColumn("Year",round(year($"TimeOfEvent")/numYears)*numYears).groupBy($"Year").agg(sum($"death") as "deathPenalty",count($"CrimeID") as "NumPenalties")
        .orderBy($"Year")
        .select($"Year", $"deathPenalty" as "deathPenaltyFalse", $"NumPenalties" as "NumPenaltiesFalse")

val killDeathTrue = killDeath
        .withColumn("Killed",$"kill" > 0)
        .filter($"Killed" === true)
        .withColumn("Year",round(year($"TimeOfEvent")/numYears)*numYears).groupBy($"Year").agg(sum($"death") as "deathPenalty",count($"CrimeID") as "NumPenalties")
        .orderBy($"Year")
        .select($"Year", $"deathPenalty" as "deathPenaltyTrue", $"NumPenalties" as "NumPenaltiesTrue")

val killDeathContingency = killDeathFalse.join(killDeathTrue,killDeathFalse("Year") === killDeathTrue("Year")).drop(killDeathTrue("Year"))

display(killDeathContingency)

// COMMAND ----------

// MAGIC %run ./PipedRDDIsit1

// COMMAND ----------

// MAGIC %md
// MAGIC `NumTosses1, NumHeads1` corresponds to `Total penalties, Death penalties` given that the offender did not kill someone.
// MAGIC 
// MAGIC `NumTosses2, NumHeads2` corresponds to `Total penalties, Death penalties` given that the offender did kill someone.

// COMMAND ----------

//What do we do when the number of trials exceed what can be run?
val killDeathInputOptsSmall = killDeathContingency
        //.withColumn("NumBoxes",lit(100000))
        .withColumn("NumBoxes",when(($"NumPenaltiesFalse" > 2000) or ($"NumPenaltiesFalse" > 2000),lit(1000000)) otherwise lit(100000))
        .withColumn("NumIter",lit(100000))
        .withColumn("Seed",lit(100))
        .withColumn("LogScaling",lit(1))
        .withColumn("NumTosses1",$"NumPenaltiesFalse")
        .withColumn("NumHeads1",$"deathPenaltyFalse")
        .select($"Year".cast("Long") as "ID", 
                $"NumBoxes",
                $"NumIter",
                $"Seed",
                $"NumTosses1".cast("Int") as "NumTosses1",$"NumHeads1".cast("Int") as "NumHeads1",
                $"NumPenaltiesTrue".cast("Int") as "NumTosses2",$"deathPenaltyTrue".cast("Int") as "NumHeads2",$"LogScaling").as[InputOpts]
display(killDeathInputOptsSmall)
//display(killDeathContingency.select(when($"NumPenaltiesFalse" > 5000,round($"NumPenaltiesFalse"/10)) otherwise $"NumPenaltiesFalse"))

// COMMAND ----------

val allKillDeathsSmall = killDeathInputOptsSmall.collect

// COMMAND ----------

val smallExecutionResult = execute(allKillDeathsSmall.toArray)

// COMMAND ----------

smallExecutionResult.filter(str => str contains " 528")

// COMMAND ----------

display(resultAsDF(parseOutput(allKillDeathsSmall.toArray,smallExecutionResult)).orderBy($"ID").withColumn("Year",$"ID"))

// COMMAND ----------

val result = resultAsDF(parseOutput(allKillDeathsSmall.toArray,smallExecutionResult)).orderBy($"ID").withColumn("Year",$"ID")

// COMMAND ----------

display(result)

// COMMAND ----------

display(result
       .withColumn("Model averaged mean0", $"Label0Prob"*$"Label0Mean0" + $"Label1Prob"*$"Label1Mean0")
       .withColumn("Model averaged mean1", $"Label0Prob"*$"Label0Mean1" + $"Label1Prob"*$"Label1Mean1"))

// COMMAND ----------

import org.apache.spark.sql.expressions.Window

// COMMAND ----------

display(killDeathInputOptsSmall)

// COMMAND ----------

import scala.math.pow

// COMMAND ----------

import scala.collection.mutable.WrappedArray
val r = 0.5
val funUDF = udf((a:WrappedArray[Int]) => a
                 .map(a => a
                      .asInstanceOf[Double])
                      .toArray
                      .reverse
                      .zipWithIndex
                      .map((input) => input._1*pow((1-r),input._2)*r).reduce((a,b)=> a+b))

// COMMAND ----------

val partitionWindow = Window.partitionBy().orderBy("ID").rowsBetween(-500, 0)
display(killDeathInputOptsSmall
        .withColumn("NumTosses1Tilde",collect_list($"NumTosses1") over partitionWindow).withColumn("NumTosses1Tilde",funUDF($"NumTosses1Tilde"))
        .withColumn("NumHeads1Tilde",collect_list($"NumHeads1") over partitionWindow).withColumn("NumHeads1Tilde",funUDF($"NumHeads1Tilde"))
        .withColumn("NumTosses2Tilde",collect_list($"NumTosses2") over partitionWindow).withColumn("NumTosses2Tilde",funUDF($"NumTosses2Tilde"))
        .withColumn("NumHeads2Tilde",collect_list($"NumHeads2") over partitionWindow).withColumn("NumHeads2Tilde",funUDF($"NumHeads2Tilde")))

// COMMAND ----------

import org.apache.spark.sql.functions._

// COMMAND ----------

val funAllKillDeathsSmall = killDeathInputOptsSmall
        .withColumn("NumTosses1Tilde",collect_list($"NumTosses1") over partitionWindow).withColumn("NumTosses1",ceil(funUDF($"NumTosses1Tilde")) cast "Int")
        .withColumn("NumHeads1Tilde",collect_list($"NumHeads1") over partitionWindow).withColumn("NumHeads1",ceil(funUDF($"NumHeads1Tilde"))cast "Int")
        .withColumn("NumTosses2Tilde",collect_list($"NumTosses2") over partitionWindow).withColumn("NumTosses2",ceil(funUDF($"NumTosses2Tilde"))cast "Int")
        .withColumn("NumHeads2Tilde",collect_list($"NumHeads2") over partitionWindow).withColumn("NumHeads2",ceil(funUDF($"NumHeads2Tilde"))cast "Int")
        .drop("NumTosses1Tilde")
        .drop("NumTosses2Tilde")
        .drop("NumHeads1Tilde")
        .drop("NumHeads2Tilde").as[InputOpts]

// COMMAND ----------

val funResult = execute(funAllKillDeathsSmall.collect.toArray)

// COMMAND ----------

val resultSmoothed = resultAsDF(parseOutput(funAllKillDeathsSmall.collect.toArray,funResult)).orderBy($"ID").withColumn("Year",$"ID")

// COMMAND ----------

display(resultSmoothed
       .withColumn("Model averaged mean0", $"Label0Prob"*$"Label0Mean0" + $"Label1Prob"*$"Label1Mean0")
       .withColumn("Model averaged mean1", $"Label0Prob"*$"Label0Mean1" + $"Label1Prob"*$"Label1Mean1"))

// COMMAND ----------

display(result
       .withColumn("Model averaged mean0", $"Label0Prob"*$"Label0Mean0" + $"Label1Prob"*$"Label1Mean0")
       .withColumn("Model averaged mean1", $"Label0Prob"*$"Label0Mean1" + $"Label1Prob"*$"Label1Mean1"))

// COMMAND ----------

// MAGIC %md #Experimenting with different seeds

// COMMAND ----------

//What do we do when the number of trials exceed what can be run?
val scalingFactor = 2
val killDeathInputOptsSmallArray = Array(100,3123,1234123,1231235,12331,23123,1235326,67,45,2345,74,34,5,234,324,67,7,58).map(seed => killDeathContingency
        .withColumn("NumBoxes",lit(10000))
        .withColumn("NumIter",lit(1000000))
        .withColumn("Seed",lit(seed))
        .withColumn("LogScaling",lit(1))
        .withColumn("NumTosses1",when($"NumPenaltiesFalse" > 1000,round($"NumPenaltiesFalse"/scalingFactor)) otherwise $"NumPenaltiesFalse")
        .withColumn("NumHeads1",when($"NumPenaltiesFalse" > 1000,round($"deathPenaltyFalse"/scalingFactor)) otherwise $"deathPenaltyFalse")
        .select($"Year".cast("Long") as "ID", 
                $"NumBoxes",
                $"NumIter",
                $"Seed",
                $"NumTosses1".cast("Int") as "NumTosses1",$"NumHeads1".cast("Int") as "NumHeads1",
                $"NumPenaltiesTrue".cast("Int") as "NumTosses2",$"deathPenaltyTrue".cast("Int") as "NumHeads2",$"LogScaling").as[InputOpts])
//display(killDeathInputOptsSmall)
//display(killDeathContingency.select(when($"NumPenaltiesFalse" > 5000,round($"NumPenaltiesFalse"/10)) otherwise $"NumPenaltiesFalse"))

// COMMAND ----------

val severalRuns = killDeathInputOptsSmallArray.map(run => (run.collect,execute(run.collect.toArray)))

// COMMAND ----------

val severalRunsResult = severalRuns.map(run => resultAsDF(parseOutput(run._1,run._2)).orderBy($"ID").withColumn("Year",$"ID"))

// COMMAND ----------

import org.apache.spark.sql.DataFrame

val allTrialsUnion = severalRunsResult.reduceLeft((A:DataFrame,B:DataFrame) => A.union(B))

// COMMAND ----------

display(allTrialsUnion.groupBy($"ID").agg(mean($"Label0Prob"),stddev($"Label0Prob")))

// COMMAND ----------

// MAGIC %md ## Conclusion
// MAGIC There seems to be little to no standard deviation w.r.t. the seed, thus we can believe that the simulated mean is close to the actual average.

// COMMAND ----------

// MAGIC %md #Experimenting with different box-counts

// COMMAND ----------

//What do we do when the number of trials exceed what can be run?
val scalingFactor = 2
val killDeathInputOptsSmallArray = Array(1000,10000,100000).map(seed => killDeathContingency
        .withColumn("NumBoxes",lit(10000))
        .withColumn("NumIter",lit(1000000))
        .withColumn("Seed",lit(seed))
        .withColumn("LogScaling",lit(1))
        .withColumn("NumTosses1",when($"NumPenaltiesFalse" > 1000,round($"NumPenaltiesFalse"/scalingFactor)) otherwise $"NumPenaltiesFalse")
        .withColumn("NumHeads1",when($"NumPenaltiesFalse" > 1000,round($"deathPenaltyFalse"/scalingFactor)) otherwise $"deathPenaltyFalse")
        .select($"Year".cast("Long") as "ID", 
                $"NumBoxes",
                $"NumIter",
                $"Seed",
                $"NumTosses1".cast("Int") as "NumTosses1",$"NumHeads1".cast("Int") as "NumHeads1",
                $"NumPenaltiesTrue".cast("Int") as "NumTosses2",$"deathPenaltyTrue".cast("Int") as "NumHeads2",$"LogScaling").as[InputOpts])
//display(killDeathInputOptsSmall)
//display(killDeathContingency.select(when($"NumPenaltiesFalse" > 5000,round($"NumPenaltiesFalse"/10)) otherwise $"NumPenaltiesFalse"))

// COMMAND ----------

val severalRuns = killDeathInputOptsSmallArray.map(run => run.collect).map(run => (run,execute(run)))

// COMMAND ----------

import org.apache.spark.sql.DataFrame

val severalRunsResult = severalRuns.map(run => resultAsDF(parseOutput(run._1,run._2)).orderBy($"ID").withColumn("Year",$"ID"))
val allTrialsUnion = severalRunsResult.reduceLeft((A:DataFrame,B:DataFrame) => A.union(B))

// COMMAND ----------

display(allTrialsUnion.groupBy($"ID").agg(mean($"Label0Prob"),stddev($"Label0Prob")))

// COMMAND ----------

// MAGIC %md ## Conclusion
// MAGIC There seems to be little to no standard deviation w.r.t. the numBoxes, thus we can believe that the simulated mean is close to the actual average.

// COMMAND ----------

