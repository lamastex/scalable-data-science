<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Classification using Word2Vec
=============================

Word embeddings
---------------

Word embeddings map words to vectors of real numbers. Frequency analysis, which we did in a another notebook, is an example of this. There, the 1000 most common words in a collection of text words were mapped to a 1000-dimensional space using one-hot encoding, while the other words were sent to the zero vector. An array of words is mapped to the sum of the one-hot encoded vectors.

A more sophisticated word embedding is Word2Vec, which uses the skip-gram model and hierarchical softmax. The idea is to map words to the vector so that it predicts the other words around it well. We refer to <a href="https://arxiv.org/abs/1301.3781">Efficient Estimation of Word Representations in Vector Space</a> and <a href="https://arxiv.org/abs/1310.4546">Distributed Representations of Words and Phrases and their Compositionality</a> for details.

The practical difference is that Word2Vec maps every word to a non-zero vector, and that the output dimension can be chosen freely. Also, the embedding itself has to be trained before use, using some large collection of words. An array of words is mapped to the average of these words.

This case study uses the sex forums on Flashback and Familjeliv. The aim is to determine which forum a thread comes from by using the resulting word embeddings, using logistic regression.

</div>

<div class="cell markdown">

Preamble
--------

This section loads libraries and imports functions from another notebook.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// import required libraries
import org.apache.spark.ml.feature.{Word2Vec,Word2VecModel}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.Row
import org.apache.spark.ml.feature.RegexTokenizer
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.ml.feature.{Word2Vec, Word2VecModel}
    import org.apache.spark.ml.{Pipeline, PipelineModel}
    import org.apache.spark.ml.linalg.{Vector, Vectors}
    import org.apache.spark.sql.Row
    import org.apache.spark.ml.feature.RegexTokenizer
    import org.apache.spark.ml.classification.LogisticRegression
    import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
/scalable-data-science/000_0-sds-3-x-projects/student-project-01_group-TheTwoCultures/01_load_data
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell markdown">

Loading the data
----------------

To extract the data from the .xml-file we use get\_dataset().

Scraping the data takes quite some time, so we also supply a second cell that loads saved results.

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// process .xml-files
val file_name = "dbfs:/datasets/student-project-01/familjeliv/familjeliv-sexsamlevnad.xml"
val df = get_dataset(file_name)
val file_name2 = "dbfs:/datasets/student-project-01/flashback/flashback-sex.xml"
val df2 = get_dataset(file_name2)
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// paths to saved dataframes
val file_path_familjeliv = "dbfs:/datasets/student-project-01/familjeliv/familjeliv-sexsamlevnad_df" 
val file_path_flashback = "dbfs:/datasets/student-project-01/flashback/flashback-sex_df"

// load saved data frame
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

<div class="cell markdown">

The dataframes consist of 7 fields: \* thread*id - a unique numerical signifier for each thread \* thread*title - the title of the thread, set by the person who created it \* w - a comma separated string of all posts in a thread \* forum*id - a numerical forum signifier \* forum*title - name of the forum to which the thread belongs \* platform - the platform from which the thread comes (flashback or familjeliv) \* corpus\_id - the corpus from which the data was gathered

Let's have a look at the dataframes.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df_familjeliv)
```

</div>

<div class="cell markdown">

We add labels and merge the two dataframes.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df = df_flashback.withColumn("c", lit(0.0)).union(df_familjeliv.withColumn("c", lit(1.0)))
```

<div class="output execute_result plain_result" execution_count="1">

    df: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [thread_id: string, thread_title: string ... 6 more fields]

</div>

</div>

<div class="cell markdown">

Preprocessing the data
----------------------

Next, we must split and clean the text. For this we use Regex Tokenizers. We do not eliminate stop words.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// define the tokenizer
val tokenizer = new RegexTokenizer()
  .setPattern("(?U),") // break by commas
  .setMinTokenLength(5) // Filter away tokens with length < 5
  .setInputCol("w") // name of the input column
  .setOutputCol("text") // name of the output column
```

<div class="output execute_result plain_result" execution_count="1">

    tokenizer: org.apache.spark.ml.feature.RegexTokenizer = regexTok_f0701eaf4f60

</div>

</div>

<div class="cell markdown">

Let's tokenize and check out the result.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// define the thread title tokenizer

val df_tokenized = tokenizer.transform(df)
display(df_tokenized.select("w","text"))
```

</div>

<div class="cell markdown">

Define and training a Word2Vec model
------------------------------------

We use the text from the threads to train the Word2Vec model. First we define the model.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// define the model
val word2Vec = new Word2Vec()
  .setInputCol("text")
  .setOutputCol("result")
  .setVectorSize(200)
  .setMinCount(0)
```

<div class="output execute_result plain_result" execution_count="1">

    word2Vec: org.apache.spark.ml.feature.Word2Vec = w2v_945abe6fab57

</div>

</div>

<div class="cell markdown">

We train the model by fitting it to any dataframe we wish. Here, we use the tokenized one. Training the model takes roughly 2h30m, so we save the result to avoid the hassle of redoing calculations.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// train it
val word2Vec_model = word2Vec.fit(df_tokenized)

// save it
word2Vec_model.save("dbfs:/datasets/student-project-01/word2vec_model_sex")
```

</div>

<div class="cell markdown">

We can also load a saved model.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// load a saved model

val model = Word2VecModel.load("dbfs:/datasets/student-project-01/word2vec_model_sex")
```

<div class="output execute_result plain_result" execution_count="1">

    model: org.apache.spark.ml.feature.Word2VecModel = w2v_854b46dceacc

</div>

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

<div class="cell markdown">

Embedding using Word2Vec
------------------------

Let's embedd the text and view the results.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// transform the text using the model

val embedded_text = model.transform(df_tokenized)
```

<div class="output execute_result plain_result" execution_count="1">

    embedded_text: org.apache.spark.sql.DataFrame = [thread_id: string, thread_title: string ... 8 more fields]

</div>

</div>

<div class="cell markdown">

Let's have a look!

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(embedded_text.select("c","result","text"))
```

</div>

<div class="cell markdown">

Classification using Word2Vec
-----------------------------

For classification we use logistic regression to compare with results from earlier. First we define the logistic regression model, using the same settings as before.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Logistic regression
val logreg = new LogisticRegression()
  .setLabelCol("c")
  .setFeaturesCol("result")
  .setMaxIter(100)
  .setRegParam(0.0001)
  .setElasticNetParam(0.5)
```

<div class="output execute_result plain_result" execution_count="1">

    logreg: org.apache.spark.ml.classification.LogisticRegression = logreg_55ef614f783e

</div>

</div>

<div class="cell markdown">

The easiest way to do the classification is to gather the tokenizer, Word2Vec and logistic regression into a pipeline.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val pipeline = new Pipeline().setStages(Array(tokenizer, word2Vec, logreg))
```

<div class="output execute_result plain_result" execution_count="1">

    pipeline: org.apache.spark.ml.Pipeline = pipeline_2254409a91a2

</div>

</div>

<div class="cell markdown">

Split the data into training and test data

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val random_order = df.orderBy(rand())
val splits = random_order.randomSplit(Array(0.8, 0.2))
val training = splits(0)
val test = splits(1)
```

<div class="output execute_result plain_result" execution_count="1">

    random_order: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [thread_id: string, thread_title: string ... 6 more fields]
    splits: Array[org.apache.spark.sql.Dataset[org.apache.spark.sql.Row]] = Array([thread_id: string, thread_title: string ... 6 more fields], [thread_id: string, thread_title: string ... 6 more fields])
    training: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [thread_id: string, thread_title: string ... 6 more fields]
    test: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [thread_id: string, thread_title: string ... 6 more fields]

</div>

</div>

<div class="cell markdown">

Fit the model to the training data. This will take a while, so we make sure to save the result.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// fit the model to the training data
val logreg_model = pipeline.fit(training)

// save the model to filesystem
logreg_model.save("dbfs:/datasets/student-project-01/word2vec_logreg_model")
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// load saved model

val loaded_model = PipelineModel.load("dbfs:/datasets/student-project-01/word2vec_logreg_model")
```

<div class="output execute_result plain_result" execution_count="1">

    loaded_model: org.apache.spark.ml.PipelineModel = pipeline_25839437fccb

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val predictions = loaded_model.transform(test).orderBy(rand())
```

<div class="output execute_result plain_result" execution_count="1">

    predictions: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [thread_id: string, thread_title: string ... 11 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="true">

``` scala
predictions.select("c","prediction","probability").show(30,false)
```

<div class="output execute_result plain_result" execution_count="1">

    +---+----------+------------------------------------------+
    |c  |prediction|probability                               |
    +---+----------+------------------------------------------+
    |1.0|1.0       |[0.005855361335036372,0.9941446386649635] |
    |1.0|1.0       |[0.2712120894396273,0.7287879105603726]   |
    |1.0|1.0       |[0.0017886928649958375,0.9982113071350042]|
    |1.0|1.0       |[2.263165652125581E-4,0.9997736834347875] |
    |0.0|1.0       |[0.42059601285820825,0.5794039871417918]  |
    |1.0|1.0       |[6.566687042616189E-4,0.9993433312957383] |
    |0.0|0.0       |[0.7054463412596114,0.29455365874038864]  |
    |1.0|1.0       |[0.03103196407369915,0.9689680359263009]  |
    |0.0|0.0       |[0.9294663779954874,0.07053362200451263]  |
    |1.0|1.0       |[0.13974006800394764,0.8602599319960523]  |
    |1.0|1.0       |[0.08228914085494436,0.9177108591450557]  |
    |0.0|0.0       |[0.9788989176701534,0.021101082329846567] |
    |0.0|0.0       |[0.9975070728891363,0.0024929271108637065]|
    |1.0|1.0       |[0.0010781075297480556,0.998921892470252] |
    |0.0|0.0       |[0.9253825302681451,0.07461746973185476]  |
    |1.0|1.0       |[0.01751495449884683,0.9824850455011531]  |
    |1.0|0.0       |[0.9864736560167631,0.013526343983237045] |
    |1.0|1.0       |[0.002472519507918196,0.9975274804920817] |
    |0.0|0.0       |[0.6174112612306129,0.3825887387693872]   |
    |0.0|0.0       |[0.7130899106721519,0.2869100893278482]   |
    |0.0|0.0       |[0.9263664682801233,0.07363353171987672]  |
    |0.0|0.0       |[0.9561455191484204,0.04385448085157954]  |
    |0.0|0.0       |[0.5835745861693306,0.41642541383066944]  |
    |1.0|1.0       |[0.4296249407516458,0.5703750592483542]   |
    |1.0|1.0       |[0.0032969395487662213,0.9967030604512337]|
    |1.0|1.0       |[0.008645133666934816,0.9913548663330651] |
    |1.0|1.0       |[4.1492836709996625E-5,0.9999585071632902]|
    |0.0|1.0       |[0.43037903982909986,0.5696209601709002]  |
    |0.0|1.0       |[0.43707897641990706,0.562921023580093]   |
    |1.0|1.0       |[0.29846393214228517,0.7015360678577148]  |
    +---+----------+------------------------------------------+
    only showing top 30 rows

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val evaluator = new BinaryClassificationEvaluator().setLabelCol("c")
evaluator.evaluate(predictions)
```

<div class="output execute_result plain_result" execution_count="1">

    evaluator: org.apache.spark.ml.evaluation.BinaryClassificationEvaluator = binEval_0d518432d17a
    res13: Double = 0.9499399325125091

</div>

</div>

<div class="cell markdown">

An AUCROC of 0.95 is good, but not notably better than the other, conceptually simpler model. More is not always better!

Previously, we classified entire threads. Let's see if it works as well on thread titles.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val df_threads = df.select("c","thread_title").withColumnRenamed("thread_title","w")
val evaluation = loaded_model.transform(df_threads).orderBy(rand())
evaluator.evaluate(evaluation)
```

<div class="output execute_result plain_result" execution_count="1">

    df_threads: org.apache.spark.sql.DataFrame = [c: double, w: string]
    evaluation: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [c: double, w: string ... 5 more fields]
    res14: Double = 0.5261045467524708

</div>

</div>

<div class="cell markdown">

This did not work at all. It is essentially equivalent to guessing randomly. Thread titles contain only a few words, so this is not surprising.

Note: the same model was used as for both classifying tasks. Since thread titles were not part of the threads, the entire dataset could conceivably be used for training. Whether or not this would improve results is unclear.

</div>
