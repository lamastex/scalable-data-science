<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Classification
==============

</div>

<div class="cell markdown">

#### Load processed data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Load LDA model or the topic distributions
// data format: org.apache.spark.sql.DataFrame = [genome:string, label:string, id:long]
val k_mers_df_train = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train_nonoverlapping")
val k_mers_df_test = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test_nonoverlapping")

// data format: org.apache.spark.sql.DataFrame = [_1: bigint, _2: vector] the vector part contains the topic distributions
val trainingData = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/topic_dist_train_t20_i20k_no_cv")
val testData = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/topic_dist_test_t20_i20k_no_cv")
```

<div class="output execute_result plain_result" execution_count="1">

    k_mers_df_train: org.apache.spark.sql.DataFrame = [genome: string, label: string ... 1 more field]
    k_mers_df_test: org.apache.spark.sql.DataFrame = [genome: string, label: string ... 1 more field]
    trainingData: org.apache.spark.sql.DataFrame = [_1: bigint, _2: vector]
    testData: org.apache.spark.sql.DataFrame = [_1: bigint, _2: vector]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Merge data sources to get labels 
spark.conf.set("spark.sql.autoBroadcastJoinThreshold", -1)
val mergedTrainingData = trainingData.join(k_mers_df_train,trainingData("_1") === k_mers_df_train("id"),"inner").withColumnRenamed("_2","features").drop("_1")
val mergedTestData = testData.join(k_mers_df_test,testData("_1") === k_mers_df_test("id"),"inner").withColumnRenamed("_2","features").drop("_1")
```

<div class="output execute_result plain_result" execution_count="1">

    mergedTrainingData: org.apache.spark.sql.DataFrame = [features: vector, genome: string ... 2 more fields]
    mergedTestData: org.apache.spark.sql.DataFrame = [features: vector, genome: string ... 2 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
mergedTrainingData.show(5)
```

<div class="output execute_result plain_result" execution_count="1">

    +--------------------+--------------------+-------+----+
    |            features|              genome|  label|  id|
    +--------------------+--------------------+-------+----+
    |[0.05124403025197...|CTT GTA GAT CTG T...|oceania|  26|
    |[0.04866065493877...|CTC TTG TAG ATC T...|oceania|  29|
    |[0.04856023665158...|ACT TTC GAT CTC T...|oceania| 474|
    |[0.05114514533282...|CTT GTA GAT CTG T...|oceania| 964|
    |[0.04856846330546...|ACT TTC GAT CTC T...|oceania|1677|
    +--------------------+--------------------+-------+----+
    only showing top 5 rows

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
mergedTestData.show(5)
```

<div class="output execute_result plain_result" execution_count="1">

    +--------------------+--------------------+-------+----+
    |            features|              genome|  label|  id|
    +--------------------+--------------------+-------+----+
    |[0.04856691976914...|ACT TTC GAT CTC T...|oceania|2250|
    |[0.04775129705622...|ACT TTC GAT CTC T...|oceania|3091|
    |[0.04856692420144...|ACT TTC GAT CTC T...|oceania|7279|
    |[0.04881282010425...|ACT TTC GAT CTC T...|oceania|8075|
    |[0.05110516016513...|CTT GTA GAT CTG T...|oceania|9458|
    +--------------------+--------------------+-------+----+
    only showing top 5 rows

</div>

</div>

<div class="cell markdown">

#### Explore data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions._
import org.apache.spark.ml._


//Split the feature vector into seprate columns
val vecToArray = udf( (xs: linalg.Vector) => xs.toArray )
val dfArr = mergedTrainingData.withColumn("featuresArr" , vecToArray($"features") )
val elements = Array("f1", "f2", "f3", "f4", "f5", "f6","f7", "f8", "f9","f10")
val sqlExpr = elements.zipWithIndex.map{ case (alias, idx) => col("featuresArr").getItem(idx).as(alias) }
val df_feats = dfArr.select((col("label") +: sqlExpr) : _*)
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions._
    import org.apache.spark.ml._
    vecToArray: org.apache.spark.sql.expressions.UserDefinedFunction = SparkUserDefinedFunction($Lambda$9301/1088827103@108f1ec1,ArrayType(DoubleType,false),List(Some(class[value[0]: vector])),None,true,true)
    dfArr: org.apache.spark.sql.DataFrame = [features: vector, genome: string ... 3 more fields]
    elements: Array[String] = Array(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    sqlExpr: Array[org.apache.spark.sql.Column] = Array(featuresArr[0] AS `f1`, featuresArr[1] AS `f2`, featuresArr[2] AS `f3`, featuresArr[3] AS `f4`, featuresArr[4] AS `f5`, featuresArr[5] AS `f6`, featuresArr[6] AS `f7`, featuresArr[7] AS `f8`, featuresArr[8] AS `f9`, featuresArr[9] AS `f10`)
    df_feats: org.apache.spark.sql.DataFrame = [label: string, f1: double ... 9 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df_feats.describe().show()
```

<div class="output execute_result plain_result" execution_count="1">

    +-------+------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
    |summary|       label|                  f1|                  f2|                  f3|                  f4|                  f5|                  f6|                  f7|                  f8|                  f9|                 f10|
    +-------+------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
    |  count|       22155|               22155|               22155|               22155|               22155|               22155|               22155|               22155|               22155|               22155|               22155|
    |   mean|        null| 0.04931437716125177| 0.05103219325119624| 0.05043777410343692| 0.04985720003755815| 0.05109828486193199|0.049969419011346175|0.049612622552246397| 0.05172608119915934| 0.05519918725594238|0.050902505322165705|
    | stddev|        null| 0.00376757883941906|0.012025788212709033|0.008575417672641356|0.007216341887817029|   0.017036551424972|0.006744772303600655|0.006141190111960429|0.015925101732795318|0.028248525286887288|0.011750518257958392|
    |    min|      africa|7.597652484573448E-5|7.931226826328689E-5|7.472127009799751E-5|8.488001247712824E-5|5.737788781311747E-5|7.510235436716417E-5| 8.01478429209781E-5|1.102376409869290...|7.060888276954563E-5|5.730971677795215...|
    |    max|southamerica| 0.07096255125893133| 0.08483680076143339| 0.07401129608174387| 0.09129137902172121| 0.07569705042586143| 0.09892558758161725| 0.12033929830179531| 0.08811599095688616| 0.09753703174079206| 0.08152012057643862|
    +-------+------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions.rand
display(df_feats.sample(true,0.5).orderBy(rand()))
```

</div>

<div class="cell markdown">

![](https://github.com/r-e-x-a-g-o-n/scalable-data-science/blob/master/images/ScaDaMaLe/000_0-sds-3-x-projects/9_05_1.JPG?raw=true)

</div>

<div class="cell markdown">

#### Classification

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
    import org.apache.spark.ml.Pipeline
    import org.apache.spark.ml.feature.LabeledPoint
    import org.apache.spark.ml.classification.RandomForestClassifier
    import org.apache.spark.ml.linalg.Vector
    transformers: Array[org.apache.spark.ml.feature.StringIndexer] = Array(strIdx_5c4c8beb7d03)
    rf: org.apache.spark.ml.classification.RandomForestClassifier = rfc_c92cc57e8554
    model: org.apache.spark.ml.PipelineModel = pipeline_fd80c4a01f49

</div>

</div>

<div class="cell markdown">

#### Evaluation

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
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
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.mllib.evaluation.MulticlassMetrics
    evaluateModel: (model: org.apache.spark.ml.PipelineModel, df: org.apache.spark.sql.DataFrame)Unit

</div>

</div>

<div class="cell markdown">

Review fit of training dataset

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
evaluateModel(model, mergedTrainingData)
```

<div class="output execute_result plain_result" execution_count="1">

    Confusion matrix:
    12688.0  80.0    7.0     1.0    0.0    0.0   
    137.0    6891.0  0.0     0.0    0.0    0.0   
    53.0     18.0    1071.0  1.0    0.0    0.0   
    47.0     3.0     0.0     843.0  0.0    0.0   
    16.0     2.0     0.0     1.0    192.0  0.0   
    19.0     0.0     0.0     0.0    0.0    85.0  
    Summary Statistics
    Accuracy = 0.9826224328593997
    fm0 = 0.9860118122474355
    fm1 = 0.9828840393667095
    fm2 = 0.9644304367402071
    fm3 = 0.9695227142035653
    fm4 = 0.9528535980148882
    fm5 = 0.8994708994708994

</div>

</div>

<div class="cell markdown">

Performance on Test dataset

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
evaluateModel(model, mergedTestData)
```

<div class="output execute_result plain_result" execution_count="1">

    Confusion matrix:
    5373.0  89.0    3.0   3.0    0.0  0.0   
    368.0   2522.0  0.0   1.0    0.0  0.0   
    478.0   15.0    20.0  3.0    0.0  0.0   
    193.0   8.0     0.0   192.0  0.0  0.0   
    89.0    5.0     0.0   1.0    1.0  0.0   
    14.0    0.0     0.0   0.0    0.0  17.0  
    Summary Statistics
    Accuracy = 0.8648217136774881
    fm0 = 0.896770424768422
    fm1 = 0.9121157323688969
    fm2 = 0.07421150278293136
    fm3 = 0.6475548060708264
    fm4 = 0.020618556701030924
    fm5 = 0.7083333333333333

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
