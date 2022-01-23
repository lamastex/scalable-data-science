<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

The two cultures - Classifying threads with logistic regression
===============================================================

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Imports
import org.apache.spark.ml.feature.StopWordsRemover
import org.apache.spark.ml.feature.RegexTokenizer
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.CountVectorizer
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.feature.StopWordsRemover
    import org.apache.spark.ml.feature.RegexTokenizer
    import org.apache.spark.ml.classification.LogisticRegression
    import org.apache.spark.ml.feature.CountVectorizer
    import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator

</div>

</div>

<div class="cell markdown">

1. Load the data
----------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
/scalable-data-science/000_0-sds-3-x-projects/student-project-01_group-TheTwoCultures/01_load_data
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions.{col, concat_ws, udf, flatten, explode, collect_list, collect_set, lit}
    import org.apache.spark.sql.types.{ArrayType, StructType, StructField, StringType, IntegerType}
    import com.databricks.spark.xml._
    import org.apache.spark.sql.functions._
    read_xml: (file_name: String)org.apache.spark.sql.DataFrame
    get_dataset: (file_name: String)org.apache.spark.sql.DataFrame

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    save_df: (df: org.apache.spark.sql.DataFrame, filePath: String)Unit
    load_df: (filePath: String)org.apache.spark.sql.DataFrame
    no_forums: (df: org.apache.spark.sql.DataFrame)Long

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

<div class="output execute_result plain_result" execution_count="1">

    dbfs:/datasets/student-project-01/flashback/familjeliv-allmanna-ekonomi_df
    familjeliv-allmanna-ekonomi_df already exists!
    dbfs:/datasets/student-project-01/flashback/familjeliv-sexsamlevnad_df
    familjeliv-sexsamlevnad_df already exists!
    dbfs:/datasets/student-project-01/flashback/flashback-ekonomi_df
    flashback-ekonomi_df already exists!
    dbfs:/datasets/student-project-01/flashback/flashback-sex_df
    flashback-sex_df already exists!
    fl_root: String = dbfs:/datasets/student-project-01/familjeliv/
    fb_root: String = dbfs:/datasets/student-project-01/flashback/
    fl_data: Array[String] = Array(familjeliv-allmanna-ekonomi, familjeliv-sexsamlevnad)
    fb_data: Array[String] = Array(flashback-ekonomi, flashback-sex)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Load dataframes
val file_path_familjeliv = "dbfs:/datasets/student-project-01/familjeliv/familjeliv-sexsamlevnad_df"
val file_path_flashback = "dbfs:/datasets/student-project-01/flashback/flashback-sex_df"
val df_familjeliv = load_df(file_path_familjeliv)
val df_flashback = load_df(file_path_flashback)
```

<div class="output execute_result plain_result" execution_count="1">

    file_path_familjeliv: String = dbfs:/datasets/student-project-01/familjeliv/familjeliv-sexsamlevnad_df
    file_path_flashback: String = dbfs:/datasets/student-project-01/flashback/flashback-sex_df
    df_familjeliv: org.apache.spark.sql.DataFrame = [thread_id: string, thread_title: string ... 5 more fields]
    df_flashback: org.apache.spark.sql.DataFrame = [thread_id: string, thread_title: string ... 5 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Extract the text
val df_text_flashback = df_flashback.select("w")
val df_text_familjeliv = df_familjeliv.select("w")
```

<div class="output execute_result plain_result" execution_count="1">

    df_text_flashback: org.apache.spark.sql.DataFrame = [w: string]
    df_text_familjeliv: org.apache.spark.sql.DataFrame = [w: string]

</div>

</div>

<div class="cell markdown">

2. Add labels
-------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Add label columns and make sure that we have exactly the same amount of data from both forums
val df_text_flashback_c = df_text_flashback.withColumn("c", lit(0))
val df_text_familjeliv_c = df_text_familjeliv.orderBy(rand()).limit(df_text_flashback_c.count().toInt).withColumn("c", lit(1))
val df_text_full = df_text_flashback_c.union(df_text_familjeliv_c)

//Check the counts
println(df_text_flashback_c.count())
println(df_text_familjeliv_c.count())
println(df_text_full.count())
```

<div class="output execute_result plain_result" execution_count="1">

    56621
    56621
    113242
    df_text_flashback_c: org.apache.spark.sql.DataFrame = [w: string, c: int]
    df_text_familjeliv_c: org.apache.spark.sql.DataFrame = [w: string, c: int]
    df_text_full: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [w: string, c: int]

</div>

</div>

<div class="cell markdown">

3. Extract single words
-----------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val tokenizer = new RegexTokenizer()
  .setPattern("(?U),") // break by whitespace
  .setMinTokenLength(5) // Filter away tokens with length < 5
  .setInputCol("w") // name of the input column
  .setOutputCol("text") // name of the output column
val tokenized_df = tokenizer.transform(df_text_full).select("c", "text")
tokenized_df.show(3, false)
```

</div>

<div class="cell markdown">

4. Remove stopwords
-------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Stopwordsremover (similar to lda notebook)
val stoppord = sc.textFile("dbfs:/datasets/student-project-01/stoppord.csv").collect()
val stopwordList = Array("bara","lite","finns","vill","samt","inga","även","finns","ganska","också","igen","just","that","with","http","jpg",  "kanske","tycker","gillar","bra","000","måste","tjej","tjejer","tjejen","tjejerna","kvinna","kvinnor","kille","killar","killen","män","rätt","män","com","and","html","många","aldrig","www","mpg","avi","wmv","riktigt","känner","väldigt","font","size","mms","2008","2009", "flashback", "familjeliv").union(stoppord).union(StopWordsRemover.loadDefaultStopWords("swedish"))

val remover = new StopWordsRemover()
  .setStopWords(stopwordList)
  .setInputCol("text")
  .setOutputCol("filtered")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Run the stopwordsremover
val removed_df = remover.transform(tokenized_df).select("c", "filtered")
```

<div class="output execute_result plain_result" execution_count="1">

    removed_df: org.apache.spark.sql.DataFrame = [c: int, filtered: array<string>]

</div>

</div>

<div class="cell markdown">

5. Count words and create vocabulary vector
-------------------------------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Unlimited size vocabulary just to see how much there is
val vectorizerall = new CountVectorizer()
   .setInputCol("filtered")
   .setOutputCol("features")
   .setMinDF(5) // Only count words that occur in at least 5 threadss
   .fit(removed_df) // returns CountVectorizerModel

//This is the one we use, limit size of vocabulary
val vectorizer = new CountVectorizer()
   .setInputCol("filtered")
   .setOutputCol("features")
   .setVocabSize(1000) // Size of dictonary
   .setMinDF(5) // Only count words that occur in at least 5 threadss
   .fit(removed_df) // returns CountVectorizerModel
```

<div class="output execute_result plain_result" execution_count="1">

    vectorizerall: org.apache.spark.ml.feature.CountVectorizerModel = CountVectorizerModel: uid=cntVec_e9a01f8ad0fe, vocabularySize=129204
    vectorizer: org.apache.spark.ml.feature.CountVectorizerModel = CountVectorizerModel: uid=cntVec_9d51ff227f19, vocabularySize=1000

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Let's take a look at the vocabulary
vectorizer.vocabulary
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Count the word frequencies
val tf = vectorizer.transform(removed_df.select("c", "filtered")).select("c", "features").cache()

//Print the feature vector to show what it looks like
tf.take(1).foreach(println)
```

<div class="output execute_result plain_result" execution_count="1">

    [0,(1000,[0,4,5,6,12,16,33,34,48,53,56,60,64,66,68,73,83,84,91,100,101,105,107,119,123,125,127,141,143,163,171,201,205,210,212,261,273,302,325,338,341,348,361,367,383,414,424,453,454,491,571,621,632,635,667,684,693,701,829,849,933,981],[1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,2.0,1.0,1.0,2.0,2.0,1.0,1.0,3.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,2.0,2.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0])]
    tf: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [c: int, features: vector]

</div>

</div>

<div class="cell markdown">

6. Split data into training and test data
-----------------------------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Train test split
val random_order = tf.orderBy(rand())
val splits = random_order.randomSplit(Array(0.8, 0.2), seed = 1337)
val training = splits(0)
val test = splits(1)
println(training.count())
println(test.count())
```

<div class="output execute_result plain_result" execution_count="1">

    90818
    22424
    random_order: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [c: int, features: vector]
    splits: Array[org.apache.spark.sql.Dataset[org.apache.spark.sql.Row]] = Array([c: int, features: vector], [c: int, features: vector])
    training: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [c: int, features: vector]
    test: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [c: int, features: vector]

</div>

</div>

<div class="cell markdown">

7. Logistic Regression model
----------------------------

\\[P(y = 1) = \frac{1}{1+exp(-\beta X^T)}\\]

\\[
X = [1,x_1, \dots, x_m], \quad
\beta = [\beta_0, \beta_1, ..., \beta_m]
\\]

where \\(x_i\\) is occurrence for word \\(i\\), \\(m\\) is 1000.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Logistic regression
val lr = new LogisticRegression()
  .setLabelCol("c")
  .setMaxIter(100) //Run for 100 iterations (not necessary but let's stay on safe side)
  .setRegParam(0.0001) //Just a tiny bit of regularization to avoid overfitting
  .setElasticNetParam(0.5) // 50-50 between L1 and L2 loss

// Fit the model
val lrModel = lr.fit(training)

// Print the coefficients and intercept for logistic regression
println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Check the training progress
lrModel.binarySummary.objectiveHistory.foreach(loss => println(loss))
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Ugly code to lookup maximum and minimum values
var maxarray = Array.ofDim[Double](5,2)
def findmax(idx: Int, value: Double) = {
  if (value > maxarray(4)(1)){
    maxarray(4)(0) = idx
    maxarray(4)(1) = value
    maxarray = maxarray.sortBy(- _(1))
  }
}
var minarray = Array.ofDim[Double](5,2)
def findmin(idx: Int, value: Double) = {
  if (value < minarray(4)(1)){
    minarray(4)(0) = idx
    minarray(4)(1) = value
    minarray = minarray.sortBy(_(1))
  }
}
```

<div class="output execute_result plain_result" execution_count="1">

    maxarray: Array[Array[Double]] = Array(Array(0.0, 0.0), Array(0.0, 0.0), Array(0.0, 0.0), Array(0.0, 0.0), Array(0.0, 0.0))
    findmax: (idx: Int, value: Double)Unit
    minarray: Array[Array[Double]] = Array(Array(0.0, 0.0), Array(0.0, 0.0), Array(0.0, 0.0), Array(0.0, 0.0), Array(0.0, 0.0))
    findmin: (idx: Int, value: Double)Unit

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Let's check which words are considered most important for classification
lrModel.coefficients.foreachActive((idx, value) => findmax(idx, value))

//First check familjeliv
println(maxarray.deep.foreach(println))
println(vectorizer.vocabulary(maxarray(0)(0).toInt))
println(vectorizer.vocabulary(maxarray(1)(0).toInt))
println(vectorizer.vocabulary(maxarray(2)(0).toInt))
println(vectorizer.vocabulary(maxarray(3)(0).toInt))
println(vectorizer.vocabulary(maxarray(4)(0).toInt))
lrModel.coefficients.foreachActive((idx, value) => findmin(idx, value))

//Check for flashback
println(minarray.deep.foreach(println))
println(vectorizer.vocabulary(minarray(0)(0).toInt))
println(vectorizer.vocabulary(minarray(1)(0).toInt))
println(vectorizer.vocabulary(minarray(2)(0).toInt))
println(vectorizer.vocabulary(minarray(3)(0).toInt))
println(vectorizer.vocabulary(minarray(4)(0).toInt))
```

<div class="output execute_result plain_result" execution_count="1">

    Array(46.0, 1.8647655096872564)
    Array(885.0, 1.275281418044729)
    Array(950.0, 1.224376631679196)
    Array(380.0, 0.9577079595234373)
    Array(32.0, 0.8432880748567715)
    ()
    anonym
    förlossningen
    maken
    sambon
    sambo
    Array(990.0, -2.2314223680319945)
    Array(664.0, -1.8291269454715258)
    Array(857.0, -1.4232104863197035)
    Array(275.0, -1.3427561053936439)
    Array(173.0, -1.1857533047141897)
    ()
    topic
    bruden
    vafan
    brudar
    jävligt

</div>

</div>

<div class="cell markdown">

8. Predict on test data
-----------------------

</div>

<div class="cell code" execution_count="1" scrolled="true">

``` scala
val predictions = lrModel.transform(test)
predictions.orderBy(rand()).select("c", "prediction", "probability").show(30, false)
```

<div class="output execute_result plain_result" execution_count="1">

    +---+----------+-------------------------------------------+
    |c  |prediction|probability                                |
    +---+----------+-------------------------------------------+
    |0  |0.0       |[0.9721401017870042,0.027859898212995764]  |
    |0  |0.0       |[0.975623998737009,0.02437600126299096]    |
    |1  |1.0       |[4.6730789993111417E-7,0.9999995326921002] |
    |0  |0.0       |[0.933249707175278,0.066750292824722]      |
    |0  |0.0       |[0.9902085789245901,0.009791421075409966]  |
    |0  |0.0       |[0.5279677569376853,0.47203224306231467]   |
    |0  |0.0       |[0.9932461412304279,0.00675385876957205]   |
    |1  |1.0       |[2.43269453308815E-5,0.9999756730546691]   |
    |1  |1.0       |[8.266454051870882E-10,0.9999999991733546] |
    |0  |0.0       |[0.9997151003194746,2.8489968052548283E-4] |
    |1  |0.0       |[0.5514931570249911,0.44850684297500887]   |
    |0  |0.0       |[0.5858664716477586,0.41413352835224143]   |
    |1  |1.0       |[0.002100566198113697,0.9978994338018863]  |
    |1  |1.0       |[0.07917634407205193,0.920823655927948]    |
    |0  |0.0       |[0.9970675008521647,0.0029324991478353007] |
    |0  |0.0       |[0.9999595461915014,4.045380849869759E-5]  |
    |0  |1.0       |[0.33337692071405434,0.6666230792859457]   |
    |1  |1.0       |[0.36761800025826114,0.6323819997417389]   |
    |1  |1.0       |[0.3245585295503879,0.6754414704496121]    |
    |1  |1.0       |[0.2355899833856519,0.7644100166143482]    |
    |0  |0.0       |[0.9999999999997755,2.2452150253864004E-13]|
    |1  |1.0       |[0.18608690603389255,0.8139130939661074]   |
    |0  |0.0       |[0.740890026139782,0.25910997386021795]    |
    |0  |0.0       |[0.963586227883629,0.036413772116371014]   |
    |1  |1.0       |[0.0021508873399861557,0.9978491126600139] |
    |1  |1.0       |[0.3858439417926455,0.6141560582073544]    |
    |1  |1.0       |[0.4517753939274335,0.5482246060725665]    |
    |1  |1.0       |[0.02573645474447229,0.9742635452555276]   |
    |0  |0.0       |[0.8022052550237544,0.19779474497624555]   |
    |1  |1.0       |[0.042382658471976975,0.9576173415280229]  |
    +---+----------+-------------------------------------------+
    only showing top 30 rows

    predictions: org.apache.spark.sql.DataFrame = [c: int, features: vector ... 3 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//Check auroc value
val evaluator = new BinaryClassificationEvaluator().setLabelCol("c")
evaluator.evaluate(predictions)
```

<div class="output execute_result plain_result" execution_count="1">

    evaluator: org.apache.spark.ml.evaluation.BinaryClassificationEvaluator = BinaryClassificationEvaluator: uid=binEval_0812f13ed2be, metricName=areaUnderROC, numBins=1000
    res19: Double = 0.928445521562674

</div>

</div>
