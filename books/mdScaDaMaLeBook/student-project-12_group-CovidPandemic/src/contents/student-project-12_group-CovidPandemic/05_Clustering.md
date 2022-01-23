<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Clustering of country features in the Covid 19 dataset
======================================================

</div>

<div class="cell markdown">

Execute relevant notebooks to load and preprocess data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
"./02_DataPreprocess"
```

</div>

<div class="cell markdown">

Clustering using Kmeans
-----------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(valid_distinct_features)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// transform to features in order to perform kmeans
import org.apache.spark.ml.feature.VectorAssembler

// define input cols and output (add additional columns here...)
val va = new VectorAssembler().setInputCols(Array("population","population_density","median_age","aged_65_older","aged_70_older","gdp_per_capita","cardiovasc_death_rate","diabetes_prevalence","female_smokers","male_smokers","hospital_beds_per_thousand","life_expectancy","human_development_index")).setOutputCol("features")

// create features
val df_feats = va.transform(valid_distinct_features)
display(df_feats)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    Silhouette metric: 0.8522450614906474
    Cluster centers:
    [4.610053152E7,113.00692000000001,31.668000000000003,9.45524,6.029160000000001,17977.45664,247.31187999999995,6.65,9.020000000000001,29.740000000000002,2.9476000000000004,73.7436,0.7251200000000002]
    [1.4096640795E9,299.0465,33.45,8.315,4.6715,10867.693,272.0895,10.065000000000001,1.9,34.5,2.435,73.285,0.696]
    [3.02263134E8,90.6665,33.8,10.366,6.3925,32707.095,246.9765,8.555,10.950000000000001,50.349999999999994,1.905,75.28999999999999,0.8089999999999999]
    [1.993803743333333E8,515.2163333333333,28.166666666666664,6.048333333333333,3.700666666666666,7554.047999999999,299.66499999999996,8.280000000000001,4.633333333333333,33.1,1.2,71.91333333333333,0.643]
    [7294451.1190476185,259.81845238095224,33.12142857142858,10.440523809523814,6.707988095238093,24778.540952380958,246.83450000000002,7.545952380952383,11.430952380952382,31.786904761904758,3.2384761904761903,74.73202380952378,0.7552380952380952]
    [1.07767729E8,167.7762,33.06,10.376800000000001,6.874000000000001,19659.705700000002,258.51500000000004,9.284,9.4,35.4,4.029000000000001,75.318,0.7576]
    import org.apache.spark.ml.clustering.KMeans
    import org.apache.spark.ml.evaluation.ClusteringEvaluator
    num_clusters: Int = 6
    seed: Int = 2
    kmeans: org.apache.spark.ml.clustering.KMeans = kmeans_46338dc75b58
    model: org.apache.spark.ml.clustering.KMeansModel = KMeansModel: uid=kmeans_46338dc75b58, k=6, distanceMeasure=euclidean, numFeatures=13
    preds: org.apache.spark.sql.DataFrame = [iso_code: string, location: string ... 15 more fields]
    cluster_evaluator: org.apache.spark.ml.evaluation.ClusteringEvaluator = ClusteringEvaluator: uid=cluEval_2943e2a697af, metricName=silhouette, distanceMeasure=squaredEuclidean
    silhouette_metric: Double = 0.8522450614906474

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// check model parameters
model.extractParamMap
```

<div class="output execute_result plain_result" execution_count="1">

    res20: org.apache.spark.ml.param.ParamMap =
    {
    	kmeans_46338dc75b58-distanceMeasure: euclidean,
    	kmeans_46338dc75b58-featuresCol: features,
    	kmeans_46338dc75b58-initMode: k-means||,
    	kmeans_46338dc75b58-initSteps: 2,
    	kmeans_46338dc75b58-k: 6,
    	kmeans_46338dc75b58-maxIter: 20,
    	kmeans_46338dc75b58-predictionCol: prediction,
    	kmeans_46338dc75b58-seed: 2,
    	kmeans_46338dc75b58-tol: 1.0E-4
    }

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_clstr = preds.withColumnRenamed("prediction", "kmeans_class")
display(df_clstr)
```

</div>

<div class="cell markdown">

Visualization
-------------

Based on each country's features, the countries can be clustered accordingly

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_clstr_filtered = df_clstr.select($"iso_code",$"kmeans_class")
display(df_clstr_filtered)
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/12_05_1.JPG?raw=true)

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
