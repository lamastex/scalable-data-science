// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # The two cultures - Classifying threads with logistic regression

// COMMAND ----------

//Imports
import org.apache.spark.ml.feature.StopWordsRemover
import org.apache.spark.ml.feature.RegexTokenizer
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.CountVectorizer
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator

// COMMAND ----------

// MAGIC %md
// MAGIC ## 1. Load the data

// COMMAND ----------

// MAGIC %run /scalable-data-science/000_0-sds-3-x-projects/student-project-01_group-TheTwoCultures/01_load_data

// COMMAND ----------

//Load dataframes
val file_path_familjeliv = "dbfs:/datasets/student-project-01/familjeliv/familjeliv-sexsamlevnad_df"
val file_path_flashback = "dbfs:/datasets/student-project-01/flashback/flashback-sex_df"
val df_familjeliv = load_df(file_path_familjeliv)
val df_flashback = load_df(file_path_flashback)

// COMMAND ----------

//Extract the text
val df_text_flashback = df_flashback.select("w")
val df_text_familjeliv = df_familjeliv.select("w")


// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. Add labels

// COMMAND ----------

//Add label columns and make sure that we have exactly the same amount of data from both forums
val df_text_flashback_c = df_text_flashback.withColumn("c", lit(0))
val df_text_familjeliv_c = df_text_familjeliv.orderBy(rand()).limit(df_text_flashback_c.count().toInt).withColumn("c", lit(1))
val df_text_full = df_text_flashback_c.union(df_text_familjeliv_c)

//Check the counts
println(df_text_flashback_c.count())
println(df_text_familjeliv_c.count())
println(df_text_full.count())

// COMMAND ----------

// MAGIC %md
// MAGIC ## 3. Extract single words

// COMMAND ----------

val tokenizer = new RegexTokenizer()
  .setPattern("(?U),") // break by whitespace
  .setMinTokenLength(5) // Filter away tokens with length < 5
  .setInputCol("w") // name of the input column
  .setOutputCol("text") // name of the output column
val tokenized_df = tokenizer.transform(df_text_full).select("c", "text")
tokenized_df.show(3, false)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 4. Remove stopwords

// COMMAND ----------

//Stopwordsremover (similar to lda notebook)
val stoppord = sc.textFile("dbfs:/datasets/student-project-01/stoppord.csv").collect()
val stopwordList = Array("bara","lite","finns","vill","samt","inga","även","finns","ganska","också","igen","just","that","with","http","jpg",  "kanske","tycker","gillar","bra","000","måste","tjej","tjejer","tjejen","tjejerna","kvinna","kvinnor","kille","killar","killen","män","rätt","män","com","and","html","många","aldrig","www","mpg","avi","wmv","riktigt","känner","väldigt","font","size","mms","2008","2009", "flashback", "familjeliv").union(stoppord).union(StopWordsRemover.loadDefaultStopWords("swedish"))

val remover = new StopWordsRemover()
  .setStopWords(stopwordList)
  .setInputCol("text")
  .setOutputCol("filtered")


// COMMAND ----------

//Run the stopwordsremover
val removed_df = remover.transform(tokenized_df).select("c", "filtered")

// COMMAND ----------

// MAGIC %md
// MAGIC ## 5. Count words and create vocabulary vector

// COMMAND ----------

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


// COMMAND ----------

//Let's take a look at the vocabulary
vectorizer.vocabulary

// COMMAND ----------

// Count the word frequencies
val tf = vectorizer.transform(removed_df.select("c", "filtered")).select("c", "features").cache()

//Print the feature vector to show what it looks like
tf.take(1).foreach(println)


// COMMAND ----------

// MAGIC %md
// MAGIC ## 6. Split data into training and test data

// COMMAND ----------

//Train test split
val random_order = tf.orderBy(rand())
val splits = random_order.randomSplit(Array(0.8, 0.2), seed = 1337)
val training = splits(0)
val test = splits(1)
println(training.count())
println(test.count())

// COMMAND ----------

// MAGIC %md
// MAGIC ## 7. Logistic Regression model
// MAGIC 
// MAGIC $$P(y = 1) = \frac{1}{1+exp(-\beta X^T)}$$
// MAGIC 
// MAGIC $$
// MAGIC X = [1,x_1, \dots, x_m], \quad
// MAGIC \beta = [\beta_0, \beta_1, ..., \beta_m]
// MAGIC $$
// MAGIC 
// MAGIC where \\(x_i\\) is occurrence for word \\(i\\), \\(m\\) is 1000. 

// COMMAND ----------

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



// COMMAND ----------

//Check the training progress
lrModel.binarySummary.objectiveHistory.foreach(loss => println(loss))

// COMMAND ----------

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

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md
// MAGIC ## 8. Predict on test data

// COMMAND ----------

val predictions = lrModel.transform(test)
predictions.orderBy(rand()).select("c", "prediction", "probability").show(30, false)


// COMMAND ----------

//Check auroc value
val evaluator = new BinaryClassificationEvaluator().setLabelCol("c")
evaluator.evaluate(predictions)