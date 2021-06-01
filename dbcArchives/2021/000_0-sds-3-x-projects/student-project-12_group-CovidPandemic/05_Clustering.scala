// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md # Clustering of country features in the Covid 19 dataset

// COMMAND ----------

// MAGIC %md Execute relevant notebooks to load and preprocess data

// COMMAND ----------

// MAGIC %run "./02_DataPreprocess"

// COMMAND ----------

// MAGIC %md ## Clustering using Kmeans

// COMMAND ----------

display(valid_distinct_features)

// COMMAND ----------

// transform to features in order to perform kmeans
import org.apache.spark.ml.feature.VectorAssembler

// define input cols and output (add additional columns here...)
val va = new VectorAssembler().setInputCols(Array("population","population_density","median_age","aged_65_older","aged_70_older","gdp_per_capita","cardiovasc_death_rate","diabetes_prevalence","female_smokers","male_smokers","hospital_beds_per_thousand","life_expectancy","human_development_index")).setOutputCol("features")

// create features
val df_feats = va.transform(valid_distinct_features)
display(df_feats)

// COMMAND ----------

import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.ClusteringEvaluator

// number of clusters
val num_clusters: Int = 6

// fixed seed for initialization
val seed: Int = 2

// init kmeans method
val kmeans = new KMeans().setK(num_clusters).setSeed(seed).setFeaturesCol("features")

// train kmeans cluster
val model = kmeans.fit(df_feats)

// cluster predictions
val preds = model.transform(df_feats)

// evaluate clustering base on Silhouette metric
val cluster_evaluator = new ClusteringEvaluator()
val silhouette_metric = cluster_evaluator.evaluate(preds)

// show evaluation and results
println(s"Silhouette metric: $silhouette_metric")

// cluster centers
println("Cluster centers:")
model.clusterCenters.foreach(println)

// COMMAND ----------

// check model parameters
model.extractParamMap

// COMMAND ----------

val df_clstr = preds.withColumnRenamed("prediction", "kmeans_class")
display(df_clstr)

// COMMAND ----------

// MAGIC %md ## Visualization
// MAGIC Based on each country's features, the countries can be clustered accordingly

// COMMAND ----------

val df_clstr_filtered = df_clstr.select($"iso_code",$"kmeans_class")
display(df_clstr_filtered)

// COMMAND ----------

