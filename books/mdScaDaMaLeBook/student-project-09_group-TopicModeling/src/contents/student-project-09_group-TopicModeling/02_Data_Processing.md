<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Data Processing
===============

</div>

<div class="cell markdown">

### Load datasets

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Paths to datasets of different regions.
val paths: List[String] = List("dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_oceania.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_northamerica.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_southamerica.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_europe.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_africa.fasta",
                               "dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_asia.fasta")
```

<div class="output execute_result plain_result" execution_count="1">

    paths: List[String] = List(dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_oceania.fasta, dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_northamerica.fasta, dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_southamerica.fasta, dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_europe.fasta, dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_africa.fasta, dbfs:/FileStore/shared_uploads/hugower@kth.se/sequences_asia.fasta)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    import scala.util.matching.Regex
    pattern: scala.util.matching.Regex = /[a-zA-Z]+_([a-zA-Z]+)\.
    read_datasets: (paths: List[String])List[org.apache.spark.rdd.RDD[(String, String)]]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// read data and set the delimiter as ">" which seperates each sample in fasta format
sc.hadoopConfiguration.set("textinputformat.record.delimiter",">")
val datasets = read_datasets(paths)
  
```

<div class="output execute_result plain_result" execution_count="1">

    datasets: List[org.apache.spark.rdd.RDD[(String, String)]] = List(MapPartitionsRDD[202189] at map at command-3103574048361361:13, MapPartitionsRDD[202196] at map at command-3103574048361361:13, MapPartitionsRDD[202205] at map at command-3103574048361361:13, MapPartitionsRDD[202210] at map at command-3103574048361361:13, MapPartitionsRDD[202215] at map at command-3103574048361361:13, MapPartitionsRDD[202220] at map at command-3103574048361361:13)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
datasets.length
```

<div class="output execute_result plain_result" execution_count="1">

    res0: Int = 6

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
datasets(0).take(1)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// combine the datasets into one and cache for optimization
val data = datasets.reduce( (a,b) => a++b).cache()
```

<div class="output execute_result plain_result" execution_count="1">

    data: org.apache.spark.rdd.RDD[(String, String)] = UnionRDD[202273] at $plus$plus at command-3103574048361373:1

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
data.take(1)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// get the headers for each sample (the first line of each sample is a header)
val headers = data.map( {case (genome,label) => (genome.split("\n").head.split('|'),label)})
headers.count
```

<div class="output execute_result plain_result" execution_count="1">

    headers: org.apache.spark.rdd.RDD[(Array[String], String)] = MapPartitionsRDD[35] at map at command-3103574048360661:1
    res2: Long = 31550

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
headers.take(5)
```

<div class="output execute_result plain_result" execution_count="1">

    res3: Array[(Array[String], String)] = Array((Array("MW320729.1 ", Severe acute respiratory syndrome coronavirus 2 isolate SARS-CoV-2/human/AUS/VIC16982/2020, complete genome),oceania), (Array("MW320730.1 ", Severe acute respiratory syndrome coronavirus 2 isolate SARS-CoV-2/human/AUS/VIC17307/2020, complete genome),oceania), (Array("MW320731.1 ", Severe acute respiratory syndrome coronavirus 2 isolate SARS-CoV-2/human/AUS/VIC17193/2020, complete genome),oceania), (Array("MW320733.1 ", Severe acute respiratory syndrome coronavirus 2 isolate SARS-CoV-2/human/AUS/VIC16732/2020, complete genome),oceania), (Array("MW320735.1 ", Severe acute respiratory syndrome coronavirus 2 isolate SARS-CoV-2/human/AUS/VIC16821/2020, complete genome),oceania))

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// remove the headers and only get genome sequences of samples.
val samples = data.map( {case (genome,label) => (genome.split("\n").tail.mkString(""), label)}).cache()
samples.count
```

<div class="output execute_result plain_result" execution_count="1">

    samples: org.apache.spark.rdd.RDD[(String, String)] = MapPartitionsRDD[202309] at map at command-3103574048360662:2
    res0: Long = 31550

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// get the genome lengths per sample (this is just to check if there are extreme cases so we would remove those)
val genome_length_per_s = samples.map({case (genome,label) => genome.length()})
```

<div class="output execute_result plain_result" execution_count="1">

    genome_length_per_s: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[14298] at map at command-3103574048360664:1

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// check the statistics if there is any significant variation
genome_length_per_s.stats
```

<div class="output execute_result plain_result" execution_count="1">

    res6: org.apache.spark.util.StatCounter = (count: 31550, mean: 29812.288621, stdev: 81.069114, max: 30018.000000, min: 28645.000000)

</div>

</div>

<div class="cell markdown">

#### Extract (overlapping or nonoverlapping) 3-mers

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    subsequence_str: (sequence: String, k: Int, overlapping: Boolean)String

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Extract the subsequences, kmers, for each sample
val k_mers = samples.map( {case (genome,label) => (subsequence_str(genome, 3, false),label)} ).cache()
```

<div class="output execute_result plain_result" execution_count="1">

    k_mers: org.apache.spark.rdd.RDD[(String, String)] = MapPartitionsRDD[202801] at map at command-3103574048360668:1

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
k_mers.take(1)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// index kmers
val kmers_df = k_mers.zipWithIndex.map({case ((a,b),c) => (a,b,c)}).toDF("genome", "label", "id").cache()
```

<div class="output execute_result plain_result" execution_count="1">

    kmers_df: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [genome: string, label: string ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
kmers_df.take(1)
```

</div>

<div class="cell markdown">

### Split dataset as train and test

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// split train and test data
val split = kmers_df.randomSplit(Array(0.7, 0.3), seed=42)
```

<div class="output execute_result plain_result" execution_count="1">

    split: Array[org.apache.spark.sql.Dataset[org.apache.spark.sql.Row]] = Array([genome: string, label: string ... 1 more field], [genome: string, label: string ... 1 more field])

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val train = split(0).cache()
train.take(1)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
train.count
```

<div class="output execute_result plain_result" execution_count="1">

    res6: Long = 22155

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val test = split(1).cache()
test.take(1)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
test.count
```

<div class="output execute_result plain_result" execution_count="1">

    res9: Long = 9395

</div>

</div>

<div class="cell markdown">

### Save the results

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// save the results for the next notebook
dbutils.fs.rm("/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping", recurse=true) // remove existing folder
test.write.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping")

dbutils.fs.rm("/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping", recurse=true) // remove existing folder
train.write.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping")
```

</div>
