---
title: ""
permalink: /sds/1/6/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/045_TA03_02_binary_classification/
sidebar:
  nav: "lMenu-SDS-1.6"
---

# Databricks notebook source exported at Sun, 26 Jun 2016 01:45:30 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/045_TA03_02_binary_classification.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/045_TA03_02_binary_classification](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=0&end=1611)





#Tweet Analytics

[Presentation contents](https://github.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016).





## Creating Machine Learning Pipeline without Loop

* The elasticNetParam coefficient is fixed at 1.0
* Read the Spark ML documentation for Logistic Regression
* The dataset "pos_neg_category" can be split into two or three categories as done in the next note. In this note, the dataset is randomly split into training and testing data
* This notebook can be upload to create a job for scheduled training and testing of the logistic classifier algorithm





#### Import the required python libraries:
* From PySpark Machine Learning module import the following packages:
  * Pipeline; 
  * binarizer, tokenizer and hash tags from feature package; 
  * logistic regression from regression package;
  * Multi class evaluator from evaluation package
* Read the [PySpark ML package](http://spark.apache.org/docs/latest/ml-classification-regression.html#logistic-regression) documentation for more details


```python

from pyspark.ml import *
from pyspark.ml import Pipeline
from pyspark.ml.feature import *
from pyspark.ml.classification import *
from pyspark.ml.tuning import *
from pyspark.ml.evaluation import *
from pyspark.ml.regression import *

```



#### Set the Stages (Binarizer, Tokenizer, Hash Text Features, and Logistic Regression Classifier Model)


```python

bin = Binarizer(inputCol = "category", outputCol = "label", threshold = 0.5) # Positive reviews > 0.5 threshold
tok = Tokenizer(inputCol = "review", outputCol = "word") #Note: The column "words" in the original table can also contain sentences that can be tokenized
hashTF = HashingTF(inputCol = tok.getOutputCol(), numFeatures = 50000, outputCol = "features")
lr = LogisticRegression(maxIter = 10, regParam = 0.0001, elasticNetParam = 1.0)
pipeline = Pipeline(stages = [bin, tok, hashTF, lr])

```



#### Convert the imported featurized dataset to dataframe


```python

df = table("pos_neg_category")

```



#### Randomly split the dataframe into training and testing set


```python

(trainingData, testData) = df.randomSplit([0.7, 0.3])

```



#### Fit the training dataset into the pipeline


```python

model = pipeline.fit(trainingData)

```



#### Test the predictability of the fitted algorithm with test dataset


```python

predictionModel=model.transform(testData)

```
```python

display(predictionModel.select("label","prediction", "review", "probability")) # Prob of being 0 (negative) against 1 (positive)

```
```python

predictionModel.select("label","prediction", "review", "probability").show(10) # Prob of being 0 (negative) against 1 (positive)

```



#### Assess the accuracy of the algorithm 


```python

evaluator = MulticlassClassificationEvaluator(
    labelCol="label", predictionCol="prediction", metricName="precision")
accuracy = evaluator.evaluate(predictionModel)

print("Logistic Regression Classifier Accuracy Rate = %g " % (accuracy))
print("Test Error = %g " % (1.0 - accuracy))


```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
