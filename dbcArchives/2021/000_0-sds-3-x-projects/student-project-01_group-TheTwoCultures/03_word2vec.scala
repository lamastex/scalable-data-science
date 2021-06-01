// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Classification using Word2Vec
// MAGIC 
// MAGIC ## Word embeddings
// MAGIC Word embeddings map words to vectors of real numbers. Frequency analysis, which we did in a another notebook, is an example of this. There, the 1000 most common words in a collection of text words were mapped to a 1000-dimensional space using one-hot encoding, while the other words were sent to the zero vector. An array of words is mapped to the sum of the one-hot encoded vectors.
// MAGIC 
// MAGIC A more sophisticated word embedding is Word2Vec, which uses the skip-gram model and hierarchical softmax. The idea is to map words to the vector so that it predicts the other words around it well. We refer to <a href="https://arxiv.org/abs/1301.3781">Efficient Estimation of Word Representations in Vector Space</a> and <a href="https://arxiv.org/abs/1310.4546">Distributed Representations of Words and Phrases and their Compositionality</a> for details.
// MAGIC 
// MAGIC The practical difference is that Word2Vec maps every word to a non-zero vector, and that the output dimension can be chosen freely. Also, the embedding itself has to be trained before use, using some large collection of words. An array of words is mapped to the average of these words.
// MAGIC 
// MAGIC This case study uses the sex forums on Flashback and Familjeliv. The aim is to determine which forum a thread comes from by using the resulting word embeddings, using logistic regression.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Preamble
// MAGIC 
// MAGIC This section loads libraries and imports functions from another notebook.

// COMMAND ----------

// import required libraries
import org.apache.spark.ml.feature.{Word2Vec,Word2VecModel}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.Row
import org.apache.spark.ml.feature.RegexTokenizer
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator

// COMMAND ----------

// MAGIC %run /scalable-data-science/000_0-sds-3-x-projects/student-project-01_group-TheTwoCultures/01_load_data

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Loading the data
// MAGIC 
// MAGIC To extract the data from the .xml-file we use get_dataset(). 
// MAGIC 
// MAGIC Scraping the data takes quite some time, so we also supply a second cell that loads saved results.

// COMMAND ----------

// DBTITLE 0,Scraping data from .xml files
// process .xml-files
val file_name = "dbfs:/datasets/student-project-01/familjeliv/familjeliv-sexsamlevnad.xml"
val df = get_dataset(file_name)
val file_name2 = "dbfs:/datasets/student-project-01/flashback/flashback-sex.xml"
val df2 = get_dataset(file_name2)

// COMMAND ----------

// paths to saved dataframes
val file_path_familjeliv = "dbfs:/datasets/student-project-01/familjeliv/familjeliv-sexsamlevnad_df" 
val file_path_flashback = "dbfs:/datasets/student-project-01/flashback/flashback-sex_df"

// load saved data frame
val df_familjeliv = load_df(file_path_familjeliv)
val df_flashback = load_df(file_path_flashback)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC The dataframes consist of 7 fields:
// MAGIC * thread_id - a unique numerical signifier for each thread
// MAGIC * thread_title - the title of the thread, set by the person who created it
// MAGIC * w - a comma separated string of all posts in a thread
// MAGIC * forum_id - a numerical forum signifier
// MAGIC * forum_title - name of the forum to which the thread belongs
// MAGIC * platform - the platform from which the thread comes (flashback or familjeliv)
// MAGIC * corpus_id - the corpus from which the data was gathered
// MAGIC 
// MAGIC Let's have a look at the dataframes.

// COMMAND ----------

display(df_familjeliv)

// COMMAND ----------

// MAGIC %md We add labels and merge the two dataframes.

// COMMAND ----------

val df = df_flashback.withColumn("c", lit(0.0)).union(df_familjeliv.withColumn("c", lit(1.0)))

// COMMAND ----------

// MAGIC %md ## Preprocessing the data
// MAGIC 
// MAGIC Next, we must split and clean the text. For this we use Regex Tokenizers. We do not eliminate stop words.

// COMMAND ----------

// define the tokenizer
val tokenizer = new RegexTokenizer()
  .setPattern("(?U),") // break by commas
  .setMinTokenLength(5) // Filter away tokens with length < 5
  .setInputCol("w") // name of the input column
  .setOutputCol("text") // name of the output column

// COMMAND ----------

// MAGIC %md Let's tokenize and check out the result.

// COMMAND ----------

// define the thread title tokenizer

val df_tokenized = tokenizer.transform(df)
display(df_tokenized.select("w","text"))

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ## Define and training a Word2Vec model
// MAGIC 
// MAGIC We use the text from the threads to train the Word2Vec model. First we define the model.

// COMMAND ----------

// define the model
val word2Vec = new Word2Vec()
  .setInputCol("text")
  .setOutputCol("result")
  .setVectorSize(200)
  .setMinCount(0)

// COMMAND ----------

// MAGIC %md We train the model by fitting it to any dataframe we wish. Here, we use the tokenized one. Training the model takes roughly 2h30m, so we save the result to avoid the hassle of redoing calculations.

// COMMAND ----------

// train it
val word2Vec_model = word2Vec.fit(df_tokenized)

// save it
word2Vec_model.save("dbfs:/datasets/student-project-01/word2vec_model_sex")

// COMMAND ----------

// MAGIC %md We can also load a saved model.

// COMMAND ----------

// load a saved model

val model = Word2VecModel.load("dbfs:/datasets/student-project-01/word2vec_model_sex")

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Embedding using Word2Vec
// MAGIC 
// MAGIC Let's embedd the text and view the results.

// COMMAND ----------

// transform the text using the model

val embedded_text = model.transform(df_tokenized)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Let's have a look!

// COMMAND ----------

display(embedded_text.select("c","result","text"))

// COMMAND ----------

// MAGIC %md ## Classification using Word2Vec
// MAGIC 
// MAGIC For classification we use logistic regression to compare with results from earlier. First we define the logistic regression model, using the same settings as before.

// COMMAND ----------

// Logistic regression
val logreg = new LogisticRegression()
  .setLabelCol("c")
  .setFeaturesCol("result")
  .setMaxIter(100)
  .setRegParam(0.0001)
  .setElasticNetParam(0.5)

// COMMAND ----------

// MAGIC %md The easiest way to do the classification is to gather the tokenizer, Word2Vec and logistic regression into a pipeline.

// COMMAND ----------

val pipeline = new Pipeline().setStages(Array(tokenizer, word2Vec, logreg))

// COMMAND ----------

// MAGIC %md Split the data into training and test data

// COMMAND ----------

val random_order = df.orderBy(rand())
val splits = random_order.randomSplit(Array(0.8, 0.2))
val training = splits(0)
val test = splits(1)

// COMMAND ----------

// MAGIC %md Fit the model to the training data. This will take a while, so we make sure to save the result.

// COMMAND ----------

// fit the model to the training data
val logreg_model = pipeline.fit(training)

// save the model to filesystem
logreg_model.save("dbfs:/datasets/student-project-01/word2vec_logreg_model")

// COMMAND ----------

// load saved model

val loaded_model = PipelineModel.load("dbfs:/datasets/student-project-01/word2vec_logreg_model")

// COMMAND ----------

val predictions = loaded_model.transform(test).orderBy(rand())

// COMMAND ----------

predictions.select("c","prediction","probability").show(30,false)

// COMMAND ----------

val evaluator = new BinaryClassificationEvaluator().setLabelCol("c")
evaluator.evaluate(predictions)

// COMMAND ----------

// MAGIC %md An AUCROC of 0.95 is good, but not notably better than the other, conceptually simpler model. More is not always better!
// MAGIC 
// MAGIC Previously, we classified entire threads. Let's see if it works as well on thread titles.  

// COMMAND ----------

val df_threads = df.select("c","thread_title").withColumnRenamed("thread_title","w")
val evaluation = loaded_model.transform(df_threads).orderBy(rand())
evaluator.evaluate(evaluation)

// COMMAND ----------

// MAGIC %md This did not work at all. It is essentially equivalent to guessing randomly. Thread titles contain only a few words, so this is not surprising.
// MAGIC 
// MAGIC Note: the same model was used as for both classifying tasks. Since thread titles were not part of the threads, the entire dataset could conceivably be used for training. Whether or not this would improve results is unclear.