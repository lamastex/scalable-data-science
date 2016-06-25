# Databricks notebook source exported at Sat, 25 Jun 2016 05:01:24 UTC
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
# MAGIC ## Creating Machine Learning Pipeline without Loop
# MAGIC 
# MAGIC * The elasticNetParam coefficient is fixed at 1.0
# MAGIC * Read the Spark ML documentation for Logistic Regression
# MAGIC * The dataset "pos_neg_category" can be split into two or three categories as done in the next note. In this note, the dataset is randomly split into training and testing data
# MAGIC * This notebook can be upload to create a job for scheduled training and testing of the logistic classifier algorithm

# COMMAND ----------

# MAGIC %md
# MAGIC #### Import the required python libraries:
# MAGIC * From PySpark Machine Learning module import the following packages:
# MAGIC   * Pipeline; 
# MAGIC   * binarizer, tokenizer and hash tags from feature package; 
# MAGIC   * logistic regression from regression package;
# MAGIC   * Multi class evaluator from evaluation package
# MAGIC * Read the [PySpark ML package](http://spark.apache.org/docs/latest/ml-classification-regression.html#logistic-regression) documentation for more details

# COMMAND ----------

from pyspark.ml import *
from pyspark.ml import Pipeline
from pyspark.ml.feature import *
from pyspark.ml.classification import *
from pyspark.ml.tuning import *
from pyspark.ml.evaluation import *
from pyspark.ml.regression import *

# COMMAND ----------

# MAGIC %md
# MAGIC #### Set the Stages (Binarizer, Tokenizer, Hash Text Features, and Logistic Regression Classifier Model)

# COMMAND ----------

bin = Binarizer(inputCol = "category", outputCol = "label", threshold = 0.5) # Positive reviews > 0.5 threshold
tok = Tokenizer(inputCol = "review", outputCol = "word") #Note: The column "words" in the original table can also contain sentences that can be tokenized
hashTF = HashingTF(inputCol = tok.getOutputCol(), numFeatures = 50000, outputCol = "features")
lr = LogisticRegression(maxIter = 10, regParam = 0.0001, elasticNetParam = 1.0)
pipeline = Pipeline(stages = [bin, tok, hashTF, lr])

# COMMAND ----------

# MAGIC %md
# MAGIC #### Convert the imported featurized dataset to dataframe

# COMMAND ----------

df = table("pos_neg_category")

# COMMAND ----------

# MAGIC %md
# MAGIC #### Randomly split the dataframe into training and testing set

# COMMAND ----------

(trainingData, testData) = df.randomSplit([0.7, 0.3])

# COMMAND ----------

# MAGIC %md
# MAGIC #### Fit the training dataset into the pipeline

# COMMAND ----------

model = pipeline.fit(trainingData)

# COMMAND ----------

# MAGIC %md
# MAGIC #### Test the predictability of the fitted algorithm with test dataset

# COMMAND ----------

predictionModel=model.transform(testData)

# COMMAND ----------

display(predictionModel.select("label","prediction", "review", "probability")) # Prob of being 0 (negative) against 1 (positive)

# COMMAND ----------

predictionModel.select("label","prediction", "review", "probability").show(10) # Prob of being 0 (negative) against 1 (positive)

# COMMAND ----------

# MAGIC %md
# MAGIC #### Assess the accuracy of the algorithm 

# COMMAND ----------

evaluator = MulticlassClassificationEvaluator(
    labelCol="label", predictionCol="prediction", metricName="precision")
accuracy = evaluator.evaluate(predictionModel)

print("Logistic Regression Classifier Accuracy Rate = %g " % (accuracy))
print("Test Error = %g " % (1.0 - accuracy))


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