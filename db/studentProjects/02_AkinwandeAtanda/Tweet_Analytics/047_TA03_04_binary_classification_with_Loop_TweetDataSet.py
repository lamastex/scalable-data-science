# Databricks notebook source exported at Sat, 25 Jun 2016 05:03:01 UTC
# MAGIC %md
# MAGIC 
# MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
# MAGIC 
# MAGIC 
# MAGIC ### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)
# MAGIC 
# MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

# COMMAND ----------

# MAGIC %md
# MAGIC #Tweet Analytics
# MAGIC 
# MAGIC [Presentation contents](https://github.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016).

# COMMAND ----------

# MAGIC %md
# MAGIC ## Creating Pipeline with Loop and Productionizing with Historical Tweets

# COMMAND ----------

from pyspark.ml import *
from pyspark.ml import Pipeline
from pyspark.ml.feature import *
from pyspark.ml.classification import *
from pyspark.ml.tuning import *
from pyspark.ml.evaluation import *
from pyspark.ml.regression import *
from pyspark.sql.types import *

# COMMAND ----------

df = table("pos_neg_category")

# COMMAND ----------

df.dtypes

# COMMAND ----------

lrARValidate =[]
lrARTest =[]
param = [0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]
for p in param:
  bin = Binarizer(inputCol = "category", outputCol = "label", threshold = 0.5) # Positive reviews > 0.5 threshold
  tok = Tokenizer(inputCol = "review", outputCol = "word") #Note: The column "words" in the original table can also contain sentences that will be tokenized
  hashTF = HashingTF(inputCol = tok.getOutputCol(), numFeatures = 5000, outputCol = "features")
  lr = LogisticRegression(maxIter = 10, regParam = 0.01, elasticNetParam = p)
  pipeline = Pipeline(stages = [bin, tok, hashTF, lr])
  (trainingData, validateData, testData) = df.randomSplit([0.6, 0.3, 0.1])
  model = pipeline.fit(trainingData)
  validateModel=model.transform(validateData)
  evaluator = MulticlassClassificationEvaluator(labelCol="label", predictionCol="prediction", metricName="precision")
  accuracyValidateSet = evaluator.evaluate(validateModel)
  testModel=model.transform(testData)
  accuracyTestSet = evaluator.evaluate(testModel)
#   print("Logistic Regression Classifier Accuracy Rate for Validation Dataset = %g " % (accuracyValidateSet))
#   print("Logistic Regression Classifier Accuracy Rate for Test Dataset = %g " % (accuracyTestSet))
#   print("Test Error = %g " % (1.0 - accuracy))
  lrARValidate +=[(p,accuracyValidateSet)]
  lrARTest +=[(p,accuracyTestSet)]

# COMMAND ----------

#display(pipeline)

# COMMAND ----------

lrARValidate

# COMMAND ----------

lrARTest

# COMMAND ----------

print("Logistic Regression Classifier Accuracy Rate for Validation Dataset= ", lrARValidate)

# COMMAND ----------

print("Logistic Regression Classifier Accuracy Rate for Test Dataset= ", lrARTest)

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Productionizing with Historical Tweets

# COMMAND ----------

# MAGIC %md
# MAGIC **Load/Read the saved Tweets in Parquet format**

# COMMAND ----------

trumpTweet = sqlContext.read.parquet("dbfs:/mnt/s3Data/TrumpSentiment.parquet")

# COMMAND ----------

# MAGIC %md
# MAGIC **Convert to Table**

# COMMAND ----------

trumpTweet.registerTempTable('TrumpTweetTable')

# COMMAND ----------

tT=sqlContext.read.table('TrumpTweetTable')

# COMMAND ----------

# MAGIC %md
# MAGIC **Read the data type of each column in the table**

# COMMAND ----------

trumpTweet.dtypes

# COMMAND ----------

# sqlContext.sql("SELECT COUNT(*) FROM TrumpTweetTable")

# COMMAND ----------

# MAGIC %md
# MAGIC **Change the favourite count from double to float**

# COMMAND ----------

sqlContext.sql("SELECT date, review, CAST(category as FLOAT) as category FROM TrumpTweetTable order by date asc").cache

# COMMAND ----------

# MAGIC %md
# MAGIC **Randomly split Dataframe into two or three sets**

# COMMAND ----------

(trump1, trump2, trump3) = trumpTweet.randomSplit([0.1, 0.5, 0.4])

# COMMAND ----------

# MAGIC %md
# MAGIC **Transform the fitted algorithm to predict the category of the tweet being either positive or negative**

# COMMAND ----------

#  tweetModel=model.transform(trump1)

# COMMAND ----------

# MAGIC %md
# MAGIC **Transform the fitted algorithm to predict the category of the tweet being either positive or negative**

# COMMAND ----------

 tweetModel=model.transform(trumpTweet)

# COMMAND ----------

# MAGIC %md
# MAGIC **Determine the accuracy rate of the predicted sentiment**

# COMMAND ----------

evaluator = MulticlassClassificationEvaluator(labelCol="label", predictionCol="prediction", metricName="precision")

# COMMAND ----------

accuracytweetSet = evaluator.evaluate(tweetModel)

# COMMAND ----------

accuracytweetSet

# COMMAND ----------

# display(tweetModel.select("prediction", "review", "probability"))

# COMMAND ----------

# MAGIC %md
# MAGIC **Display the predicted category, tweet and probability of the tweet being negative**

# COMMAND ----------

tweetModel.select("prediction", "review", "probability").show(100)

# COMMAND ----------

# MAGIC %md
# MAGIC **Save the sentiment category of the historical tweets for additional ETL**

# COMMAND ----------

trumpSentiment=tweetModel.select("prediction", "review", "probability")

# COMMAND ----------

trumpSentiment.write.save("dbfs:/mnt/s3Data/trumpSen.parquet")  

# COMMAND ----------

trumpSentiment.show(50)

# COMMAND ----------

display(dbutils.fs.ls("dbfs:/mnt/s3Data"))

# COMMAND ----------

trumpSen= sqlContext.read.parquet("dbfs:/mnt/s3Data/trumpSen.parquet")

# COMMAND ----------

trumpSen.registerTempTable('trumpSenTable')

# COMMAND ----------

# MAGIC %sql SELECT COUNT(*) as TweetCount FROM trumpSenTable

# COMMAND ----------

# MAGIC %sql SELECT * FROM trumpSenTable WHERE prediction ==1 LIMIT 5

# COMMAND ----------

# MAGIC %md
# MAGIC **Count and plot the percentage of Tweets about Trump that is positive and negative**

# COMMAND ----------

# MAGIC %sql SELECT if(prediction == 1, "positive", "negative") as Sentiment, count(*) as TweetCount FROM trumpSenTable GROUP BY prediction ORDER BY prediction

# COMMAND ----------

# MAGIC %md
# MAGIC 
# MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
# MAGIC 
# MAGIC 
# MAGIC ### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)
# MAGIC 
# MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)