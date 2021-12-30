// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md # Data Processing

// COMMAND ----------

// MAGIC %md ### Load datasets

// COMMAND ----------

// Paths to datasets of different regions.
val paths: List[String] = List("dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_oceania.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_northamerica.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_southamerica.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_europe.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_africa.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_asia.fasta")

// COMMAND ----------

import scala.util.matching.Regex

// regex pattern to take region name, label, from complete path name (Must be changed accordingly if path follows a different structure)
val pattern: Regex = "/[a-zA-Z]+_([a-zA-Z]+)\\.".r 

def read_datasets(paths:List[String]): List[RDD[(String,String)]] = {
  if (paths.size < 1) { // return an empty RDD
    return List.fill(0) (sc.emptyRDD)
  }
  else {
    pattern.findFirstMatchIn(paths.head) match { // extract the label based on the pattern defined above
      case Some(x) => {
        val label:String = x.group(1)  // create the label based on the path name
        return (sc.textFile(paths.head).filter(_ != "").map(_.trim()).map(s => (s,label)))::read_datasets(paths.tail) // read the file in path and attach the data with its label to RDD list
      }
      case None => throw new RuntimeException("no label found")
    }
  }
}

// COMMAND ----------

// read data and set the delimiter as ">" which seperates each sample in fasta format
sc.hadoopConfiguration.set("textinputformat.record.delimiter",">")
val datasets = read_datasets(paths)
  

// COMMAND ----------

datasets.length

// COMMAND ----------

datasets(0).take(1)

// COMMAND ----------

// combine the datasets into one and cache for optimization
val data = datasets.reduce( (a,b) => a++b).cache()

// COMMAND ----------

data.take(1)

// COMMAND ----------

// get the headers for each sample (the first line of each sample is a header)
val headers = data.map( {case (genome,label) => (genome.split("\n").head.split('|'),label)})
headers.count

// COMMAND ----------

headers.take(5)

// COMMAND ----------

// remove the headers and only get genome sequences of samples.
val samples = data.map( {case (genome,label) => (genome.split("\n").tail.mkString(""), label)}).cache()
samples.count

// COMMAND ----------

// get the genome lengths per sample (this is just to check if there are extreme cases so we would remove those)
val genome_length_per_s = samples.map({case (genome,label) => genome.length()})

// COMMAND ----------

// check the statistics if there is any significant variation
genome_length_per_s.stats

// COMMAND ----------

// MAGIC %md #### Extract (overlapping or nonoverlapping) 3-mers

// COMMAND ----------

// A tail recursive overlapping subsequence function 
// ex1: input: ("abcd", 2, true) -> output: "ab bc cd": 
// ex2: input: ("abcd", 2, false) -> output: "ab cd"
def subsequence_str( sequence:String, k:Int, overlapping:Boolean ): String = {
  def helper(seq:String, acc:String): String = {
    if (seq.length < k ) {
      return acc
    }
    else {
      val sub = seq.substring(0,k)
      if(overlapping) helper(seq.tail, acc + sub + " ")
      else helper(seq.substring(k), acc + sub + " ")
    }
  }
  return helper(sequence, "")
}

// COMMAND ----------

// Extract the subsequences, kmers, for each sample
val k_mers = samples.map( {case (genome,label) => (subsequence_str(genome, 3, false),label)} ).cache()

// COMMAND ----------

k_mers.take(1)

// COMMAND ----------

// index kmers
val kmers_df = k_mers.zipWithIndex.map({case ((a,b),c) => (a,b,c)}).toDF("genome", "label", "id").cache()

// COMMAND ----------

kmers_df.take(1)

// COMMAND ----------

// MAGIC %md ### Split dataset as train and test

// COMMAND ----------

// split train and test data
val split = kmers_df.randomSplit(Array(0.7, 0.3), seed=42)

// COMMAND ----------

val train = split(0).cache()
train.take(1)

// COMMAND ----------

train.count

// COMMAND ----------

val test = split(1).cache()
test.take(1)

// COMMAND ----------

test.count

// COMMAND ----------

// MAGIC %md ### Save the results

// COMMAND ----------

// save the results for the next notebook
dbutils.fs.rm("/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping", recurse=true) // remove existing folder
test.write.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping")

dbutils.fs.rm("/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping", recurse=true) // remove existing folder
train.write.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping")