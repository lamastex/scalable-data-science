// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC # Classification

// COMMAND ----------

// MAGIC %md #### Load processed data

// COMMAND ----------

//Load LDA model or the topic distributions
// data format: org.apache.spark.sql.DataFrame = [genome:string, label:string, id:long]
val k_mers_df_train = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping")
val k_mers_df_test = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping")

// data format: org.apache.spark.sql.DataFrame = [_1: bigint, _2: vector] the vector part contains the topic distributions
val trainingData = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/topic_dist_train_t20_i20k_no_cv")
val testData = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/topic_dist_test_t20_i20k_no_cv")

// COMMAND ----------

//Merge data sources to get labels 
spark.conf.set("spark.sql.autoBroadcastJoinThreshold", -1)
val mergedTrainingData = trainingData.join(k_mers_df_train,trainingData("_1") === k_mers_df_train("id"),"inner").withColumnRenamed("_2","features").drop("_1")
val mergedTestData = testData.join(k_mers_df_test,testData("_1") === k_mers_df_test("id"),"inner").withColumnRenamed("_2","features").drop("_1")

// COMMAND ----------

mergedTrainingData.show(5)

// COMMAND ----------

mergedTestData.show(5)

// COMMAND ----------

// MAGIC %md #### Explore data

// COMMAND ----------

import org.apache.spark.sql.functions._
import org.apache.spark.ml._


//Split the feature vector into seprate columns
val vecToArray = udf( (xs: linalg.Vector) => xs.toArray )
val dfArr = mergedTrainingData.withColumn("featuresArr" , vecToArray($"features") )
val elements = Array("f1", "f2", "f3", "f4", "f5", "f6","f7", "f8", "f9","f10")
val sqlExpr = elements.zipWithIndex.map{ case (alias, idx) => col("featuresArr").getItem(idx).as(alias) }
val df_feats = dfArr.select((col("label") +: sqlExpr) : _*)

// COMMAND ----------

df_feats.describe().show()

// COMMAND ----------

import org.apache.spark.sql.functions.rand
display(df_feats.sample(true,0.5).orderBy(rand()))

// COMMAND ----------

// MAGIC %md #### Classification 

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

// MAGIC %md #### Evaluation

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

// MAGIC %md Review fit of training dataset

// COMMAND ----------

evaluateModel(model, mergedTrainingData)

// COMMAND ----------

// MAGIC %md Performance on Test dataset

// COMMAND ----------

evaluateModel(model, mergedTestData)

// COMMAND ----------

