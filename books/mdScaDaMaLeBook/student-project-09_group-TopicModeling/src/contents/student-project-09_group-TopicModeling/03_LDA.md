<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

LDA - Extract Features
======================

</div>

<div class="cell markdown">

### Load processed data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val k_mers_df_train = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping").cache()
val k_mers_df_test = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping").cache()
```

<div class="output execute_result plain_result" execution_count="1">

    k_mers_df_train: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [genome: string, label: string ... 1 more field]
    k_mers_df_test: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [genome: string, label: string ... 1 more field]

</div>

</div>

<div class="cell markdown">

### Prepare data for LDA

</div>

<div class="cell markdown">

This part is adapted from the LDA course tutorial '034*LDA*20NewsGroupsSmall'.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.ml.feature.RegexTokenizer

// Set params for RegexTokenizer
val tokenizer = new RegexTokenizer()
.setPattern("[\\W_]+") // break by white space character(s) 
.setInputCol("genome") // name of the input column
.setOutputCol("tokens") // name of the output column

// Tokenize train and test documents
val tokenized_df_train = tokenizer.transform(k_mers_df_train)
val tokenized_df_test = tokenizer.transform(k_mers_df_test)
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.feature.RegexTokenizer
    tokenizer: org.apache.spark.ml.feature.RegexTokenizer = RegexTokenizer: uid=regexTok_b3a99fc4c607, minTokenLength=1, gaps=true, pattern=[\W_]+, toLowercase=true
    tokenized_df_train: org.apache.spark.sql.DataFrame = [genome: string, label: string ... 2 more fields]
    tokenized_df_test: org.apache.spark.sql.DataFrame = [genome: string, label: string ... 2 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(tokenized_df_train.select("tokens"))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(tokenized_df_test.select("tokens"))
```

</div>

<div class="cell markdown">

Since there are 64 = 4\*4\*4 possible words (3-mers) for a genome (which consists of A-T-G-C, 4 letters), we initially planned to use a fixed vocabulary.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.ml.feature.CountVectorizerModel
// create a dictionary array from all possible k-mers 
val k = 3
val fixed_vocab = List.fill((k))(List("a","t","g","c")).flatten.combinations(k).flatMap(_.permutations).toArray.map(_.mkString("")) // https://stackoverflow.com/questions/38406959/creating-all-permutations-of-a-list-with-a-limited-range-in-scala
val fixed_vectorizer = new CountVectorizerModel(fixed_vocab)
.setInputCol("tokens")
.setOutputCol("features")
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.feature.CountVectorizerModel
    k: Int = 3
    fixed_vocab: Array[String] = Array(aaa, aat, ata, taa, aag, aga, gaa, aac, aca, caa, att, tat, tta, atg, agt, tag, tga, gat, gta, atc, act, tac, tca, cat, cta, agg, gag, gga, agc, acg, gac, gca, cag, cga, acc, cac, cca, ttt, ttg, tgt, gtt, ttc, tct, ctt, tgg, gtg, ggt, tgc, tcg, gtc, gct, ctg, cgt, tcc, ctc, cct, ggg, ggc, gcg, cgg, gcc, cgc, ccg, ccc)
    fixed_vectorizer: org.apache.spark.ml.feature.CountVectorizerModel = CountVectorizerModel: uid=cntVecModel_4bd2f56ddf2e, vocabularySize=64

</div>

</div>

<div class="cell markdown">

However, we observed that there are some unexpected rare k-mers such as aay, ktg in the genome sequences. If they are really rare (less than 10), we have decided to eliminate them. But if they are more common, with the intuition that this sequencing error (could not find a document indicating what they refer to so we assumed they are errors) might indicate some pattern for that sample, we keep them. This approach provided us better topic diversity and results.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.ml.feature.CountVectorizer

// Create a dictionary of kmers
val vectorizer = new CountVectorizer()
      .setInputCol("tokens")
      .setOutputCol("features")
      .setMinDF(10)  // a term must appear at least in 10 documents to be included in the vocabulary.
      .fit(tokenized_df_train) // create the vocabulary based on the train data
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.feature.CountVectorizer
    vectorizer: org.apache.spark.ml.feature.CountVectorizerModel = CountVectorizerModel: uid=cntVec_cf83465b3abd, vocabularySize=241

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Vocabulary of k-mers which contains some weird nucleotides
val vocabList = vectorizer.vocabulary
```

<div class="output execute_result plain_result" execution_count="1">

    vocabList: Array[String] = Array(ttt, tgt, aaa, tta, aca, ttg, taa, att, aat, ctt, caa, tga, gtt, atg, act, aga, tat, tac, aac, tgg, tgc, aag, tca, cta, ttc, tct, gtg, agt, gaa, cat, gct, ctg, cac, gta, ata, tag, gat, ggt, cag, acc, gca, cca, atc, agg, gac, cct, agc, gag, gga, ctc, gtc, ggc, tcc, gcc, acg, cgt, ggg, ccc, tcg, cgc, cga, gcg, cgg, ccg, nnn, nna, naa, ntt, tnn, cnn, gnn, ann, nnt, nng, nnc, agn, ttn, aan, acn, tan, nat, tcn, ngt, nct, can, gtn, ctn, nta, atn, ana, tgn, nca, ggn, nga, tna, nac, ntg, gcn, gan, tgk, ngc, ccn, ncc, ngg, tnt, ntc, nag, agk, yta, cnt, ktt, aya, gkt, kta, gnt, nan, ytt, ktg, gkc, tty, ayt, tay, yaa, acy, gsc, aay, tgy, ggk, ant, tyt, yac, yat, tya, ang, anc, cay, tkt, cng, cak, rcc, cna, cgn, aty, akt, ggw, gyt, tng, raa, cyt, acw, ytg, aak, yca, ntn, gna, gay, cty, kat, kct, tkg, gnc, ngn, yag, tnc, kca, ayc, tyg, gka, ygt, aka, cnc, cya, ayg, ttk, gng, tth, maa, ncn, yga, tka, ama, aar, ytc, gtk, kag, cch, ncg, ctk, kaa, gty, yct, ara, rtg, ckt, tar, gya, tkc, tak, tgr, ccy, akg, kac, crc, grt, ggr, trt, gcy, tyc, ygg, gak, wga, ygc, cgk, gcr, kgt, wtc, tck, cwt, waa, tcy, vcc, tma, atr, agy, rgc, rac, tgs, kgc, gam, atk, cyc, haa, agr, tha, rgt, gwg, tra, cra, gtr, gkg, nam)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
vocabList.size
```

<div class="output execute_result plain_result" execution_count="1">

    res4: Int = 241

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Create vector of token counts
val countVectors_train = vectorizer.transform(tokenized_df_train).select("id", "features")
val countVectors_test = vectorizer.transform(tokenized_df_test).select("id", "features")
```

<div class="output execute_result plain_result" execution_count="1">

    countVectors_train: org.apache.spark.sql.DataFrame = [id: bigint, features: vector]
    countVectors_test: org.apache.spark.sql.DataFrame = [id: bigint, features: vector]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
tokenized_df_train.take(1)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
countVectors_train.take(5)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
countVectors_test.take(5)
```

</div>

<div class="cell markdown">

Fix the incompatibility between mllib Vector and ml Vector, which causes conflict when LDA topic distribution is given as RandomForestClassifier input

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.ml.linalg.{Vector => MLVector}
import org.apache.spark.mllib.{linalg => mllib}
import org.apache.spark.ml.{linalg => ml}
// convert each sample from ml to mllib vectors (because this causes problems in classifier step)
val lda_countVector_train = countVectors_train.map { case Row(id: Long, countVector: MLVector) => (id, mllib.Vectors.fromML(countVector)) }.cache()
val lda_countVector_test = countVectors_test.map { case Row(id: Long, countVector: MLVector) => (id, mllib.Vectors.fromML(countVector)) }.cache()
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.linalg.{Vector=>MLVector}
    import org.apache.spark.mllib.{linalg=>mllib}
    import org.apache.spark.ml.{linalg=>ml}
    lda_countVector_train: org.apache.spark.sql.Dataset[(Long, org.apache.spark.mllib.linalg.Vector)] = [_1: bigint, _2: vector]
    lda_countVector_test: org.apache.spark.sql.Dataset[(Long, org.apache.spark.mllib.linalg.Vector)] = [_1: bigint, _2: vector]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// format: Array(id, (VocabSize, Array(indexedTokens), Array(Token Frequency)))
lda_countVector_test.take(1)
```

<div class="output execute_result plain_result" execution_count="1">

    res10: Array[(Long, org.apache.spark.mllib.linalg.Vector)] = Array((13340,(241,[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63],[334.0,211.0,327.0,282.0,288.0,277.0,95.0,278.0,315.0,259.0,245.0,70.0,310.0,311.0,291.0,129.0,213.0,181.0,189.0,99.0,87.0,221.0,189.0,183.0,136.0,217.0,181.0,152.0,251.0,136.0,265.0,150.0,108.0,174.0,183.0,89.0,213.0,245.0,163.0,99.0,158.0,138.0,107.0,78.0,142.0,146.0,53.0,116.0,98.0,93.0,101.0,90.0,56.0,73.0,42.0,67.0,34.0,31.0,39.0,29.0,28.0,37.0,20.0,19.0])))

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// The number of topics 
val num_topics = 20
```

<div class="output execute_result plain_result" execution_count="1">

    num_topics: Int = 20

</div>

</div>

<div class="cell markdown">

### Run LDA

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.mllib.clustering.{LDA, EMLDAOptimizer,DistributedLDAModel}

val lda = new LDA()
.setOptimizer(new EMLDAOptimizer())
.setK(num_topics)
.setMaxIterations(20000)
.setDocConcentration(-1) // use default values
.setTopicConcentration(-1) // use default values
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.mllib.clustering.{LDA, EMLDAOptimizer, DistributedLDAModel}
    lda: org.apache.spark.mllib.clustering.LDA = org.apache.spark.mllib.clustering.LDA@53a22046

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Run the LDA based on the model described  
val lda_countVector_train_mllib = lda_countVector_train.rdd
val lda_countVector_test_mllib = lda_countVector_test.rdd
val ldaModel = lda.run(lda_countVector_train_mllib)
```

<div class="output execute_result plain_result" execution_count="1">

    lda_countVector_train_mllib: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.linalg.Vector)] = MapPartitionsRDD[399] at rdd at command-685894176420037:2
    lda_countVector_test_mllib: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.linalg.Vector)] = MapPartitionsRDD[404] at rdd at command-685894176420037:3
    ldaModel: org.apache.spark.mllib.clustering.LDAModel = org.apache.spark.mllib.clustering.DistributedLDAModel@4e5ac90e

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Cast to distributed LDA model (which is possible through EMLDAOptimizer in the model) so we can get topic distributions
val distLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]
```

<div class="output execute_result plain_result" execution_count="1">

    distLDAModel: org.apache.spark.mllib.clustering.DistributedLDAModel = org.apache.spark.mllib.clustering.DistributedLDAModel@4e5ac90e

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val topicIndices = distLDAModel.describeTopics(maxTermsPerTopic = 10)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// https://spark.apache.org/docs/1.5.0/api/scala/index.html#org.apache.spark.mllib.clustering.DistributedLDAModel
// Get the topic distributions for each train document which we will use as features in the classification step
val topicDistributions_train = distLDAModel.topicDistributions.cache()
```

<div class="output execute_result plain_result" execution_count="1">

    topicDistributions_train: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.linalg.Vector)] = MapPartitionsRDD[828598] at map at LDAModel.scala:768

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
lda_countVector_test_mllib.take(1)
```

<div class="output execute_result plain_result" execution_count="1">

    res11: Array[(Long, org.apache.spark.mllib.linalg.Vector)] = Array((13340,(241,[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63],[334.0,211.0,327.0,282.0,288.0,277.0,95.0,278.0,315.0,259.0,245.0,70.0,310.0,311.0,291.0,129.0,213.0,181.0,189.0,99.0,87.0,221.0,189.0,183.0,136.0,217.0,181.0,152.0,251.0,136.0,265.0,150.0,108.0,174.0,183.0,89.0,213.0,245.0,163.0,99.0,158.0,138.0,107.0,78.0,142.0,146.0,53.0,116.0,98.0,93.0,101.0,90.0,56.0,73.0,42.0,67.0,34.0,31.0,39.0,29.0,28.0,37.0,20.0,19.0])))

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Get the topic distributions for each test document which we will use as features in the classification step
val topicDistributions_test = distLDAModel.toLocal.topicDistributions(lda_countVector_test_mllib).cache()
```

<div class="output execute_result plain_result" execution_count="1">

    topicDistributions_test: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.linalg.Vector)] = MapPartitionsRDD[828874] at map at LDAModel.scala:373

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
assert (topicDistributions_train.take(1)(0)._2.size == num_topics)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
topicDistributions_train.take(1)
```

<div class="output execute_result plain_result" execution_count="1">

    res13: Array[(Long, org.apache.spark.mllib.linalg.Vector)] = Array((31198,[0.050970660127433086,0.03854914539182694,0.04233208760127245,0.05520192993035042,0.04529229810049971,0.044746575004622195,0.04738680077580458,0.04917176116296172,0.028216072817023714,0.06235014298552372,0.07282634237929085,0.05367477019497316,0.02834755979614659,0.007634148695007966,0.0747900052332216,0.07432157320344387,0.05619372014685311,0.05917612984447815,0.059889692006826215,0.048928584602440005]))

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
topicDistributions_test.take(1)
```

<div class="output execute_result plain_result" execution_count="1">

    res14: Array[(Long, org.apache.spark.mllib.linalg.Vector)] = Array((13340,[0.05125119795748651,0.05590356021965471,0.057337790514388406,0.053942804029345516,0.03184697426952284,0.054473799789446706,0.055602712254395316,0.07875351104584705,0.05294681251741617,0.03326797507323466,0.04996448701691076,0.06303737755619679,0.07611049117795103,0.009242706964001621,0.017554379171811085,0.026326662949499827,0.040104268572631795,0.09709248658013904,0.03851877943786023,0.05672122290225994]))

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.mllib.linalg.{Vectors => OldVectors}
import org.apache.spark.ml.linalg.{Vectors => NewVectors}

val n_topicDistributions_train = topicDistributions_train.map({case (a,b) =>(a,b.asML)})
val n_topicDistributions_test = topicDistributions_test.map({case (a,b) =>(a,b.asML)})
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.mllib.linalg.{Vectors=>OldVectors}
    import org.apache.spark.ml.linalg.{Vectors=>NewVectors}
    n_topicDistributions_train: org.apache.spark.rdd.RDD[(Long, org.apache.spark.ml.linalg.Vector)] = MapPartitionsRDD[828876] at map at command-685894176420046:4
    n_topicDistributions_test: org.apache.spark.rdd.RDD[(Long, org.apache.spark.ml.linalg.Vector)] = MapPartitionsRDD[828877] at map at command-685894176420046:5

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// save the topic distributions for train and test with partitioning for the next notebook
dbutils.fs.rm("/FileStore/shared_uploads/caylak@kth.se/topic_dist_train_t20_i20k_no_cv", recurse=true) // remove existing folder
n_topicDistributions_train.toDF.write.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/topic_dist_train_t20_i20k_no_cv")

dbutils.fs.rm("/FileStore/shared_uploads/caylak@kth.se/topic_dist_test_t20_i20k_no_cv", recurse=true) // remove existing folder
n_topicDistributions_test.toDF.write.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/topic_dist_test_t20_i20k_no_cv")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Get the top word distributions for each topic
val topics = topicIndices.map { case (terms, termWeights) =>
  terms.map(vocabList(_)).zip(termWeights)
}
println(s"$num_topics topics:")
topics.zipWithIndex.foreach { case (topic, i) =>
  println(s"TOPIC $i")
  topic.foreach { case (term, weight) => println(s"$term\t$weight") }
  println(s"==========")
}
```

</div>

<div class="cell markdown">

### Visualise Results

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Zip topic terms with topic IDs
val termArray = topics.zipWithIndex
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Transform data into the form (term, probability, topicId)
val termRDD = sc.parallelize(termArray)
val termRDD2 =termRDD.flatMap( (x: (Array[(String, Double)], Int)) => {
  val arrayOfTuple = x._1
  val topicId = x._2
  arrayOfTuple.map(el => (el._1, el._2, topicId))
})
```

<div class="output execute_result plain_result" execution_count="1">

    termRDD: org.apache.spark.rdd.RDD[(Array[(String, Double)], Int)] = ParallelCollectionRDD[829330] at parallelize at command-685894176420051:2
    termRDD2: org.apache.spark.rdd.RDD[(String, Double, Int)] = MapPartitionsRDD[829331] at flatMap at command-685894176420051:3

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Create DF with proper column names
val termDF = termRDD2.toDF.withColumnRenamed("_1", "term").withColumnRenamed("_2", "probability").withColumnRenamed("_3", "topicId")
```

<div class="output execute_result plain_result" execution_count="1">

    termDF: org.apache.spark.sql.DataFrame = [term: string, probability: double ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
Create JSON data
val rawJson = termDF.toJSON.collect().mkString(",\n")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
displayHTML(s"""
<!DOCTYPE html>
<meta charset="utf-8">
<style>

circle {
  fill: rgb(31, 119, 180);
  fill-opacity: 0.5;
  stroke: rgb(31, 119, 180);
  stroke-width: 1px;
}

.leaf circle {
  fill: #ff7f0e;
  fill-opacity: 1;
}

text {
  font: 14px sans-serif;
}

</style>
<body>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js"></script>
<script>

var json = {
 "name": "data",
 "children": [
  {
     "name": "topics",
     "children": [
      ${rawJson}
     ]
    }
   ]
};

var r = 1000,
    format = d3.format(",d"),
    fill = d3.scale.category20c();

var bubble = d3.layout.pack()
    .sort(null)
    .size([r, r])
    .padding(1.5);

var vis = d3.select("body").append("svg")
    .attr("width", r)
    .attr("height", r)
    .attr("class", "bubble");

  
var node = vis.selectAll("g.node")
    .data(bubble.nodes(classes(json))
    .filter(function(d) { return !d.children; }))
    .enter().append("g")
    .attr("class", "node")
    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    color = d3.scale.category20();
  
  node.append("title")
      .text(function(d) { return d.className + ": " + format(d.value); });

  node.append("circle")
      .attr("r", function(d) { return d.r; })
      .style("fill", function(d) {return color(d.topicName);});

var text = node.append("text")
    .attr("text-anchor", "middle")
    .attr("dy", ".3em")
    .text(function(d) { return d.className.substring(0, d.r / 3)});
  
  text.append("tspan")
      .attr("dy", "1.2em")
      .attr("x", 0)
      .text(function(d) {return Math.ceil(d.value * 10000) /10000; });

// Returns a flattened hierarchy containing all leaf nodes under the root.
function classes(root) {
  var classes = [];

  function recurse(term, node) {
    if (node.children) node.children.forEach(function(child) { recurse(node.term, child); });
    else classes.push({topicName: node.topicId, className: node.term, value: node.probability});
  }

  recurse(null, root);
  return {children: classes};
}
</script>
""")
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/9_03_1.JPG?raw=true)

</div>
