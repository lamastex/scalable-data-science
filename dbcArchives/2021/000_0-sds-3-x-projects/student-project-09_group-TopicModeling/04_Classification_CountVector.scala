// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md # Classification CountVector

// COMMAND ----------

// MAGIC %md #### Load processed data

// COMMAND ----------

val k_mers_df_train = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train").cache()
val k_mers_df_test = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test").cache()

// COMMAND ----------

val k_mers_df_train = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping").cache()
val k_mers_df_test = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping").cache()

// COMMAND ----------

// MAGIC %md #### Format data 
// MAGIC Generate word count vectors

// COMMAND ----------

// MAGIC %md 

// COMMAND ----------

import org.apache.spark.ml.feature.RegexTokenizer

// Set params for RegexTokenizer
val tokenizer = new RegexTokenizer()
.setPattern("[\\W_]+") // break by white space character(s)  - try to remove emails and other patterns
.setInputCol("genome") // name of the input column
.setOutputCol("tokens") // name of the output column

// Tokenize document
val tokenized_df_train = tokenizer.transform(k_mers_df_train)
val tokenized_df_test = tokenizer.transform(k_mers_df_test)

// COMMAND ----------

display(tokenized_df_train.select("tokens"))

// COMMAND ----------

import org.apache.spark.ml.feature.CountVectorizer
val vectorizer = new CountVectorizer()
      .setInputCol("tokens")
      .setOutputCol("features")
      .setMinDF(10)
      .fit(tokenized_df_train)

// COMMAND ----------

val vocabList = vectorizer.vocabulary

// COMMAND ----------

vocabList.size

// COMMAND ----------

// Create vector of token counts
val countVectors_train = vectorizer.transform(tokenized_df_train).select("id", "features")
val countVectors_test = vectorizer.transform(tokenized_df_test).select("id", "features")

// COMMAND ----------

import org.apache.spark.ml.linalg.{Vector => MLVector}
import org.apache.spark.mllib.{linalg => mllib}
import org.apache.spark.ml.{linalg => ml}

val lda_countVector_train = countVectors_train.map { case Row(id: Long, countVector: MLVector) => (id, mllib.Vectors.fromML(countVector)) }.cache()
val lda_countVector_test = countVectors_test.map { case Row(id: Long, countVector: MLVector) => (id, mllib.Vectors.fromML(countVector)) }.cache()

// COMMAND ----------

import org.apache.spark.mllib.linalg.{Vectors => OldVectors}
import org.apache.spark.ml.linalg.{Vectors => NewVectors}


val lda_countVector_train_1 = lda_countVector_train.map({case (a,b) =>(a,b.asML)})
val lda_countVector_test_1 = lda_countVector_test.map({case (a,b) =>(a,b.asML)})

// COMMAND ----------

val trainDF = lda_countVector_train_1.toDF()
val testDF = lda_countVector_test_1.toDF()



// COMMAND ----------

spark.conf.set("spark.sql.autoBroadcastJoinThreshold", -1)

val mergedTrainingData = trainDF.join(k_mers_df_train,trainDF("_1") === k_mers_df_train("id"),"inner").withColumnRenamed("_2","features").drop("_1")
val mergedTestData = testDF.join(k_mers_df_test,testDF("_1") === k_mers_df_test("id"),"inner").withColumnRenamed("_2","features").drop("_1")


// COMMAND ----------

// MAGIC %md ### Classification
// MAGIC The count vectors are used as features for classification 

// COMMAND ----------

import org.apache.spark.ml.feature.{StringIndexer,VectorAssembler}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.linalg.Vector

val transformers = Array(
              new StringIndexer().setInputCol("label").setOutputCol("label_id"))

// Train a RandomForest model.
val rf = new RandomForestClassifier() 
              .setLabelCol("label_id")
              .setFeaturesCol("features")
              .setNumTrees(500)
              .setFeatureSubsetStrategy("auto")
              .setImpurity("gini")
              .setMaxDepth(20)
              .setMaxBins(32)
              .setSeed(12345)

val model = new Pipeline().setStages(transformers :+ rf).fit(mergedTrainingData)

// COMMAND ----------

// MAGIC %md ### Evaluation

// COMMAND ----------

import org.apache.spark.mllib.evaluation.MulticlassMetrics

def evaluateModel(model: org.apache.spark.ml.PipelineModel, df: org.apache.spark.sql.DataFrame){
  val predictionsOnData = model.transform(df)
  val predictionAndLabelsRdd = predictionsOnData.select("prediction", "label_id").as[(Double,Double)].rdd

  val metricsMulti = new MulticlassMetrics(predictionAndLabelsRdd)
  val accuracy = metricsMulti.accuracy
  
  val fm0 = metricsMulti.fMeasure(0)
  val fm1 = metricsMulti.fMeasure(1)
  val fm2 = metricsMulti.fMeasure(2)
  val fm3 = metricsMulti.fMeasure(3)
  val fm4 = metricsMulti.fMeasure(4)
  val fm5 = metricsMulti.fMeasure(5)
  
  println("Confusion matrix:")
  println(metricsMulti.confusionMatrix)
  
  println("Summary Statistics")
  println(s"Accuracy = $accuracy")
  
  println(s"fm0 = $fm0")
  println(s"fm1 = $fm1")
  println(s"fm2 = $fm2")
  println(s"fm3 = $fm3")
  println(s"fm4 = $fm4")
  println(s"fm5 = $fm5")

}

// COMMAND ----------

// MAGIC %md ###### Evaluation Training data

// COMMAND ----------

evaluateModel(model, mergedTrainingData)

// COMMAND ----------

// MAGIC %md ##### Evaluation Test data

// COMMAND ----------

evaluateModel(model, mergedTestData)