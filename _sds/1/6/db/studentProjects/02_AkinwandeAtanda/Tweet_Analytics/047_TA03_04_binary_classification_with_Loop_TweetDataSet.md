---
title: ""
permalink: /sds/1/6/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/047_TA03_04_binary_classification_with_Loop_TweetDataSet/
sidebar:
  nav: "lMenu-SDS-1.6"
---

# Databricks notebook source exported at Sun, 26 Jun 2016 01:47:13 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/047_TA03_04_binary_classification_with_Loop_TweetDataSet.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/047_TA03_04_binary_classification_with_Loop_TweetDataSet](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=0&end=1611)





#Tweet Analytics

[Presentation contents](https://github.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016).





## Creating Pipeline with Loop and Productionizing with Historical Tweets


```python

from pyspark.ml import *
from pyspark.ml import Pipeline
from pyspark.ml.feature import *
from pyspark.ml.classification import *
from pyspark.ml.tuning import *
from pyspark.ml.evaluation import *
from pyspark.ml.regression import *
from pyspark.sql.types import *

```
```python

df = table("pos_neg_category")

```
```python

df.dtypes

```
```python

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

```
```python

#display(pipeline)

```
```python

lrARValidate

```
```python

lrARTest

```
```python

print("Logistic Regression Classifier Accuracy Rate for Validation Dataset= ", lrARValidate)

```
```python

print("Logistic Regression Classifier Accuracy Rate for Test Dataset= ", lrARTest)

```


 
## Productionizing with Historical Tweets





**Load/Read the saved Tweets in Parquet format**


```python

trumpTweet = sqlContext.read.parquet("dbfs:/mnt/s3Data/TrumpSentiment.parquet")

```



**Convert to Table**


```python

trumpTweet.registerTempTable('TrumpTweetTable')

```
```python

tT=sqlContext.read.table('TrumpTweetTable')

```



**Read the data type of each column in the table**


```python

trumpTweet.dtypes

```
```python

# sqlContext.sql("SELECT COUNT(*) FROM TrumpTweetTable")

```



**Change the favourite count from double to float**


```python

sqlContext.sql("SELECT date, review, CAST(category as FLOAT) as category FROM TrumpTweetTable order by date asc").cache

```



**Randomly split Dataframe into two or three sets**


```python

(trump1, trump2, trump3) = trumpTweet.randomSplit([0.1, 0.5, 0.4])

```



**Transform the fitted algorithm to predict the category of the tweet being either positive or negative**


```python

#  tweetModel=model.transform(trump1)

```



**Transform the fitted algorithm to predict the category of the tweet being either positive or negative**


```python

 tweetModel=model.transform(trumpTweet)

```



**Determine the accuracy rate of the predicted sentiment**


```python

evaluator = MulticlassClassificationEvaluator(labelCol="label", predictionCol="prediction", metricName="precision")

```
```python

accuracytweetSet = evaluator.evaluate(tweetModel)

```
```python

accuracytweetSet

```
```python

# display(tweetModel.select("prediction", "review", "probability"))

```



**Display the predicted category, tweet and probability of the tweet being negative**


```python

tweetModel.select("prediction", "review", "probability").show(100)

```



**Save the sentiment category of the historical tweets for additional ETL**


```python

trumpSentiment=tweetModel.select("prediction", "review", "probability")

```
```python

trumpSentiment.write.save("dbfs:/mnt/s3Data/trumpSen.parquet")  

```
```python

trumpSentiment.show(50)

```
```python

display(dbutils.fs.ls("dbfs:/mnt/s3Data"))

```
```python

trumpSen= sqlContext.read.parquet("dbfs:/mnt/s3Data/trumpSen.parquet")

```
```python

trumpSen.registerTempTable('trumpSenTable')

```
```python

%sql SELECT COUNT(*) as TweetCount FROM trumpSenTable

```
```python

%sql SELECT * FROM trumpSenTable WHERE prediction ==1 LIMIT 5

```



**Count and plot the percentage of Tweets about Trump that is positive and negative**


```python

%sql SELECT if(prediction == 1, "positive", "negative") as Sentiment, count(*) as TweetCount FROM trumpSenTable GROUP BY prediction ORDER BY prediction

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
