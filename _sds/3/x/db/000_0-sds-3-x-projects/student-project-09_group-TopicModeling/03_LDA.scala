// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md # LDA - Extract Features 

// COMMAND ----------

// MAGIC %md ### Load processed data

// COMMAND ----------

val k_mers_df_train = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping").cache()
val k_mers_df_test = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping").cache()

// COMMAND ----------

// MAGIC %md ### Prepare data for LDA

// COMMAND ----------

// MAGIC  %md This part is adapted from the LDA course tutorial '034_LDA_20NewsGroupsSmall'. 

// COMMAND ----------

import org.apache.spark.ml.feature.RegexTokenizer

// Set params for RegexTokenizer
val tokenizer = new RegexTokenizer()
.setPattern("[\\W_]+") // break by white space character(s) 
.setInputCol("genome") // name of the input column
.setOutputCol("tokens") // name of the output column

// Tokenize train and test documents
val tokenized_df_train = tokenizer.transform(k_mers_df_train)
val tokenized_df_test = tokenizer.transform(k_mers_df_test)

// COMMAND ----------

display(tokenized_df_train.select("tokens"))

// COMMAND ----------

display(tokenized_df_test.select("tokens"))

// COMMAND ----------

// MAGIC %md Since there are 64 = 4\*4\*4 possible words (3-mers) for a genome (which consists of A-T-G-C, 4 letters), we initially planned to use a fixed vocabulary.

// COMMAND ----------

import org.apache.spark.ml.feature.CountVectorizerModel
// create a dictionary array from all possible k-mers 
val k = 3
val fixed_vocab = List.fill((k))(List("a","t","g","c")).flatten.combinations(k).flatMap(_.permutations).toArray.map(_.mkString("")) // https://stackoverflow.com/questions/38406959/creating-all-permutations-of-a-list-with-a-limited-range-in-scala
val fixed_vectorizer = new CountVectorizerModel(fixed_vocab)
.setInputCol("tokens")
.setOutputCol("features")

// COMMAND ----------

// MAGIC %md However, we observed that there are some unexpected rare k-mers such as aay, ktg in the genome sequences. If they are really rare (less than 10), we have decided to eliminate them. But if they are more common, with the intuition that this sequencing error (could not find a document indicating what they refer to so we assumed they are errors) might indicate some pattern for that sample, we keep them. This approach provided us better topic diversity and results.

// COMMAND ----------

import org.apache.spark.ml.feature.CountVectorizer

// Create a dictionary of kmers
val vectorizer = new CountVectorizer()
      .setInputCol("tokens")
      .setOutputCol("features")
      .setMinDF(10)  // a term must appear at least in 10 documents to be included in the vocabulary.
      .fit(tokenized_df_train) // create the vocabulary based on the train data

// COMMAND ----------

// Vocabulary of k-mers which contains some weird nucleotides
val vocabList = vectorizer.vocabulary

// COMMAND ----------

vocabList.size

// COMMAND ----------

// Create vector of token counts
val countVectors_train = vectorizer.transform(tokenized_df_train).select("id", "features")
val countVectors_test = vectorizer.transform(tokenized_df_test).select("id", "features")

// COMMAND ----------

tokenized_df_train.take(1)

// COMMAND ----------

countVectors_train.take(5)

// COMMAND ----------

countVectors_test.take(5)

// COMMAND ----------

// MAGIC %md Fix the incompatibility between mllib Vector and ml Vector, which causes conflict when LDA topic distribution is given as RandomForestClassifier input

// COMMAND ----------

import org.apache.spark.ml.linalg.{Vector => MLVector}
import org.apache.spark.mllib.{linalg => mllib}
import org.apache.spark.ml.{linalg => ml}
// convert each sample from ml to mllib vectors (because this causes problems in classifier step)
val lda_countVector_train = countVectors_train.map { case Row(id: Long, countVector: MLVector) => (id, mllib.Vectors.fromML(countVector)) }.cache()
val lda_countVector_test = countVectors_test.map { case Row(id: Long, countVector: MLVector) => (id, mllib.Vectors.fromML(countVector)) }.cache()

// COMMAND ----------

// format: Array(id, (VocabSize, Array(indexedTokens), Array(Token Frequency)))
lda_countVector_test.take(1)

// COMMAND ----------

// The number of topics 
val num_topics = 20

// COMMAND ----------

// MAGIC %md ### Run LDA

// COMMAND ----------

import org.apache.spark.mllib.clustering.{LDA, EMLDAOptimizer,DistributedLDAModel}

val lda = new LDA()
.setOptimizer(new EMLDAOptimizer())
.setK(num_topics)
.setMaxIterations(20000)
.setDocConcentration(-1) // use default values
.setTopicConcentration(-1) // use default values

// COMMAND ----------

// Run the LDA based on the model described  
val lda_countVector_train_mllib = lda_countVector_train.rdd
val lda_countVector_test_mllib = lda_countVector_test.rdd
val ldaModel = lda.run(lda_countVector_train_mllib)

// COMMAND ----------

// Cast to distributed LDA model (which is possible through EMLDAOptimizer in the model) so we can get topic distributions
val distLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]

// COMMAND ----------

val topicIndices = distLDAModel.describeTopics(maxTermsPerTopic = 10)

// COMMAND ----------

// https://spark.apache.org/docs/1.5.0/api/scala/index.html#org.apache.spark.mllib.clustering.DistributedLDAModel
// Get the topic distributions for each train document which we will use as features in the classification step
val topicDistributions_train = distLDAModel.topicDistributions.cache()

// COMMAND ----------

lda_countVector_test_mllib.take(1)

// COMMAND ----------

// Get the topic distributions for each test document which we will use as features in the classification step
val topicDistributions_test = distLDAModel.toLocal.topicDistributions(lda_countVector_test_mllib).cache()

// COMMAND ----------

assert (topicDistributions_train.take(1)(0)._2.size == num_topics)

// COMMAND ----------

topicDistributions_train.take(1)

// COMMAND ----------

topicDistributions_test.take(1)

// COMMAND ----------

import org.apache.spark.mllib.linalg.{Vectors => OldVectors}
import org.apache.spark.ml.linalg.{Vectors => NewVectors}

val n_topicDistributions_train = topicDistributions_train.map({case (a,b) =>(a,b.asML)})
val n_topicDistributions_test = topicDistributions_test.map({case (a,b) =>(a,b.asML)})

// COMMAND ----------

// save the topic distributions for train and test with partitioning for the next notebook
dbutils.fs.rm("/FileStore/shared_uploads/caylak@kth.se/topic_dist_train_t20_i20k_no_cv", recurse=true) // remove existing folder
n_topicDistributions_train.toDF.write.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/topic_dist_train_t20_i20k_no_cv")

dbutils.fs.rm("/FileStore/shared_uploads/caylak@kth.se/topic_dist_test_t20_i20k_no_cv", recurse=true) // remove existing folder
n_topicDistributions_test.toDF.write.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/topic_dist_test_t20_i20k_no_cv")

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md ### Visualise Results

// COMMAND ----------

// Zip topic terms with topic IDs
val termArray = topics.zipWithIndex

// COMMAND ----------

// Transform data into the form (term, probability, topicId)
val termRDD = sc.parallelize(termArray)
val termRDD2 =termRDD.flatMap( (x: (Array[(String, Double)], Int)) => {
  val arrayOfTuple = x._1
  val topicId = x._2
  arrayOfTuple.map(el => (el._1, el._2, topicId))
})

// COMMAND ----------

// Create DF with proper column names
val termDF = termRDD2.toDF.withColumnRenamed("_1", "term").withColumnRenamed("_2", "probability").withColumnRenamed("_3", "topicId")

// COMMAND ----------

// Create JSON data
val rawJson = termDF.toJSON.collect().mkString(",\n")

// COMMAND ----------

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