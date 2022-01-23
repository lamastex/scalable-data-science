<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Classification CountVector
==========================

</div>

<div class="cell markdown">

#### Load processed data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val k_mers_df_train = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_train").cache()
val k_mers_df_test = spark.read.parquet("dbfs:/FileStore/shared_uploads/caylak@kth.se/data_test").cache()
```

<div class="output execute_result plain_result" execution_count="1">

    k_mers_df_train: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [genome: string, label: string ... 1 more field]
    k_mers_df_test: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [genome: string, label: string ... 1 more field]

</div>

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

#### Format data

Generate word count vectors

</div>

<div class="cell markdown">

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.ml.feature.RegexTokenizer

// Set params for RegexTokenizer
val tokenizer = new RegexTokenizer()
.setPattern("[\\W_]+") // break by white space character(s)  - try to remove emails and other patterns
.setInputCol("genome") // name of the input column
.setOutputCol("tokens") // name of the output column

// Tokenize document
val tokenized_df_train = tokenizer.transform(k_mers_df_train)
val tokenized_df_test = tokenizer.transform(k_mers_df_test)
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.feature.RegexTokenizer
    tokenizer: org.apache.spark.ml.feature.RegexTokenizer = RegexTokenizer: uid=regexTok_5df744efa843, minTokenLength=1, gaps=true, pattern=[\W_]+, toLowercase=true
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
import org.apache.spark.ml.feature.CountVectorizer
val vectorizer = new CountVectorizer()
      .setInputCol("tokens")
      .setOutputCol("features")
      .setMinDF(10)
      .fit(tokenized_df_train)
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.feature.CountVectorizer
    vectorizer: org.apache.spark.ml.feature.CountVectorizerModel = CountVectorizerModel: uid=cntVec_9226a16a835f, vocabularySize=241

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val vocabList = vectorizer.vocabulary
```

<div class="output execute_result plain_result" execution_count="1">

    vocabList: Array[String] = Array(ttt, tgt, aaa, tta, aca, ttg, taa, att, aat, ctt, caa, tga, gtt, atg, act, aga, tat, tac, aac, tgg, tgc, aag, tca, cta, ttc, tct, gtg, agt, gaa, cat, gct, ctg, cac, gta, ata, tag, gat, ggt, cag, acc, gca, cca, atc, agg, gac, cct, agc, gag, gga, ctc, gtc, ggc, tcc, gcc, acg, cgt, ggg, ccc, tcg, cgc, cga, gcg, cgg, ccg, nnn, nna, naa, ntt, tnn, cnn, gnn, ann, nnt, nng, nnc, agn, ttn, aan, acn, tan, nat, tcn, ngt, nct, can, gtn, ctn, nta, atn, tgn, ana, nca, ggn, nga, tna, nac, ntg, gcn, gan, tgk, ngc, ccn, ncc, ngg, tnt, ntc, nag, agk, yta, cnt, ktt, aya, gkt, kta, gnt, nan, ytt, ktg, gkc, tty, ayt, tay, yaa, acy, gsc, aay, tgy, ggk, ant, tyt, yac, tya, yat, anc, ang, cay, tkt, cak, cna, cng, rcc, akt, ggw, aty, cgn, gyt, tng, acw, aak, cyt, ytg, raa, yca, ntn, gay, gna, kat, kct, gnc, ngn, cty, tkg, tnc, yag, ayc, kca, aka, tyg, gka, ygt, cnc, cya, ttk, ayg, tka, gng, maa, tth, yga, ncn, ama, aar, kag, ytc, gtk, cch, ncg, kaa, ctk, gty, yct, ara, rtg, ckt, tar, gya, kac, tgr, crc, ccy, tkc, tak, akg, tyc, grt, gcy, trt, ggr, ygg, gak, gcr, kgt, ygc, cgk, wga, wtc, tma, tck, atr, waa, vcc, cwt, tcy, tgs, rgc, rac, agy, kgc, gam, haa, agr, rgt, tha, cyc, atk, gtr, tra, nam, gkg, gwg, cra)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
vocabList.size
```

<div class="output execute_result plain_result" execution_count="1">

    res17: Int = 241

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
import org.apache.spark.ml.linalg.{Vector => MLVector}
import org.apache.spark.mllib.{linalg => mllib}
import org.apache.spark.ml.{linalg => ml}

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
import org.apache.spark.mllib.linalg.{Vectors => OldVectors}
import org.apache.spark.ml.linalg.{Vectors => NewVectors}


val lda_countVector_train_1 = lda_countVector_train.map({case (a,b) =>(a,b.asML)})
val lda_countVector_test_1 = lda_countVector_test.map({case (a,b) =>(a,b.asML)})
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.mllib.linalg.{Vectors=>OldVectors}
    import org.apache.spark.ml.linalg.{Vectors=>NewVectors}
    lda_countVector_train_1: org.apache.spark.sql.Dataset[(Long, org.apache.spark.ml.linalg.Vector)] = [_1: bigint, _2: vector]
    lda_countVector_test_1: org.apache.spark.sql.Dataset[(Long, org.apache.spark.ml.linalg.Vector)] = [_1: bigint, _2: vector]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val trainDF = lda_countVector_train_1.toDF()
val testDF = lda_countVector_test_1.toDF()

```

<div class="output execute_result plain_result" execution_count="1">

    trainDF: org.apache.spark.sql.DataFrame = [_1: bigint, _2: vector]
    testDF: org.apache.spark.sql.DataFrame = [_1: bigint, _2: vector]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
spark.conf.set("spark.sql.autoBroadcastJoinThreshold", -1)

val mergedTrainingData = trainDF.join(k_mers_df_train,trainDF("_1") === k_mers_df_train("id"),"inner").withColumnRenamed("_2","features").drop("_1")
val mergedTestData = testDF.join(k_mers_df_test,testDF("_1") === k_mers_df_test("id"),"inner").withColumnRenamed("_2","features").drop("_1")
```

<div class="output execute_result plain_result" execution_count="1">

    mergedTrainingData: org.apache.spark.sql.DataFrame = [features: vector, genome: string ... 2 more fields]
    mergedTestData: org.apache.spark.sql.DataFrame = [features: vector, genome: string ... 2 more fields]

</div>

</div>

<div class="cell markdown">

### Classification

The count vectors are used as features for classification

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
    transformers: Array[org.apache.spark.ml.feature.StringIndexer] = Array(strIdx_e8cf65204547)
    rf: org.apache.spark.ml.classification.RandomForestClassifier = rfc_0a45a46b7366
    model: org.apache.spark.ml.PipelineModel = pipeline_14ad91398432

</div>

</div>

<div class="cell markdown">

### Evaluation

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

</div>

<div class="cell markdown">

###### Evaluation Training data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
evaluateModel(model, mergedTrainingData)
```

<div class="output execute_result plain_result" execution_count="1">

    Confusion matrix:
    12776.0  0.0     0.0     0.0    0.0    0.0   
    70.0     6955.0  3.0     0.0    0.0    0.0   
    139.0    1.0     1002.0  1.0    0.0    0.0   
    133.0    0.0     4.0     756.0  0.0    0.0   
    36.0     0.0     0.0     0.0    175.0  0.0   
    33.0     0.0     0.0     0.0    0.0    71.0  
    Summary Statistics
    Accuracy = 0.981042654028436
    fm0 = 0.9841697800716405
    fm1 = 0.99470823798627
    fm2 = 0.9312267657992566
    fm3 = 0.9163636363636364
    fm4 = 0.9067357512953368
    fm5 = 0.8114285714285714

</div>

</div>

<div class="cell markdown">

##### Evaluation Test data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
evaluateModel(model, mergedTestData)
```

<div class="output execute_result plain_result" execution_count="1">

    Confusion matrix:
    5450.0  8.0     6.0    4.0    0.0   0.0   
    140.0   2743.0  8.0    0.0    0.0   0.0   
    189.0   7.0     314.0  6.0    0.0   0.0   
    116.0   1.0     7.0    269.0  0.0   0.0   
    44.0    2.0     1.0    3.0    46.0  0.0   
    9.0     0.0     0.0    0.0    0.0   22.0  
    Summary Statistics
    Accuracy = 0.9413517828632251
    fm0 = 0.9548002803083393
    fm1 = 0.9706298655343242
    fm2 = 0.7370892018779341
    fm3 = 0.7970370370370371
    fm4 = 0.647887323943662
    fm5 = 0.8301886792452831

</div>

</div>
