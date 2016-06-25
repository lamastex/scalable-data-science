// Databricks notebook source exported at Sat, 25 Jun 2016 04:54:40 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC #Tweet Analytics
// MAGIC 
// MAGIC [Presentation contents](https://github.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016).

// COMMAND ----------

// MAGIC %md
// MAGIC ## Chapter Outline and Objectives
// MAGIC The objectives of this project are:
// MAGIC 1. Use Apache Spark Streaming API to collect Tweets
// MAGIC   * Unfiltered Tweets Collector Set-up
// MAGIC   * Filtered Tweets Collector Set-up by Keywords and Hashtags
// MAGIC   * Filtered Tweets Collector Set-up by Class
// MAGIC 2. Extract-Transform-Load (ETL) the collected Tweets:
// MAGIC   * Read/Load the Streamed Tweets in batches of RDD
// MAGIC   * Read/Load the Streamed Tweets in merged batches of RDDs
// MAGIC   * Save the Tweets in Parquet format, convert to Dataframe Table and run SQL queries
// MAGIC   * Explore the Streamed Tweets using SQL queries: Filter, Plot and Re-Shape
// MAGIC 3. Build a featurization dataset for Machine Learning Job
// MAGIC   * Explore the binarize classification
// MAGIC 4. Build a Machine Learning Classification Algorithm Pipeline
// MAGIC   * Pipeline without loop
// MAGIC   * Pipeline with loop
// MAGIC 5. Create job to continuously train the algorithm
// MAGIC 6. Productionize the fitted algorithm for Sentiment Analysis

// COMMAND ----------

// MAGIC %md
// MAGIC ## Notebook Execution Order and Description
// MAGIC The chronological order of executing the notebooks for this chapter are listed as follows with corresponidng title:
// MAGIC 1. Start a Spark Streaming Job
// MAGIC   * Execute either of the three collectors depending on the project objective:
// MAGIC   * 040_TA01_01_Unfiltered_Tweets_Collector_Set-up
// MAGIC   * 041_TA01_02_Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags
// MAGIC   * 042_TA01_03_Filtered_Tweets_Collector_Set-up_by_Class
// MAGIC 2. Process the collected Tweets using the ETL framework
// MAGIC   * 043_TA02_ETL_Tweets
// MAGIC 3. Download, Load and Explore the Features Dataset for the Machine Learning Job
// MAGIC   * 044_TA03_01_binary_classification
// MAGIC 4. Build a Machine Learning Classification Algorithm Pipeline
// MAGIC  * Run either of the notebooks depending on the choice of Elastic Parameter values
// MAGIC  * 045_TA03_02_binary_classification
// MAGIC  * 046_TA03_03_binary_classification_with_Loop
// MAGIC 5. Productionalize the trainned Algorithm with Historical Tweets
// MAGIC   * 047_TA03_04_binary_classification_with_Loop_TweetDataSet

// COMMAND ----------

// MAGIC %md
// MAGIC ##1. Use Apache Spark Streaming API to collect Tweets
// MAGIC   * Unfiltered Tweets Collector Set-up
// MAGIC   * Filtered Tweets Collector Set-up by Keywords and Hashtags
// MAGIC   * Filtered Tweets Collector Set-up by Class

// COMMAND ----------

// MAGIC %md
// MAGIC ## Unfiltered Tweet Collector

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/1.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Filtered Tweets Collector Set-up by Keywords and Hashtags

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/2-1.PNG)

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("http://www.wonderoftech.com/best-twitter-tips-followers/", 600))

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/3.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Filtered Tweets Collector Set-up by Class

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/4.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. Extract-Transform-Load (ETL) the collected Tweets:
// MAGIC   * Read/Load the Streamed Tweets in batches of RDD
// MAGIC   * Read/Load the Streamed Tweets in merged batches of RDDs
// MAGIC   * Save the Tweets in Parquet format, convert to Dataframe Table and run SQL queries
// MAGIC   * Explore the Streamed Tweets using SQL queries: Filter, Plot and Re-Shape
// MAGIC Extract--Transform--Load: Streamed Tweets

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/5.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/6.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/7.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/8.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Explore Streamed Tweets: SQL, Filter, Plot and Re-Shape

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/9.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/10-1.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/11.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/12.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/13.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 3. Featurization: Training and Testing Datasets sourced from:
// MAGIC * Amazon Products Reviews; 
// MAGIC * Vector of Positive & Negative Words [from "Workshop on text analysis" by Neal Caren](http://nbviewer.jupyter.org/github/nealcaren/workshop_2014/tree/master/notebooks/)
// MAGIC * [NLTK corpus movie reviews (Postive and Negative Reviews)](http://streamhacker.com/tag/sentiment/)
// MAGIC * [Random Products Reviews (Web Scraped by Sandtex)](https://pythonprogramming.net/static/downloads/short_reviews/)
// MAGIC * Download the featured dataset ["pos_neg_category"](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/pos_neg_category.csv)
// MAGIC * Upload as a Table and change the data type for the "category" column to double

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/14.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Exploration: Training and Testing Datasets in R

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/15.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ** Convert to DataFrame in R**

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/16.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 4. Build a Machine Learning Classification Algorithm Pipeline
// MAGIC   * Pipeline without loop
// MAGIC   * Pipeline with loop

// COMMAND ----------

// MAGIC %md
// MAGIC ## Machine Learning Pipeline: Training and Testing Datasets in Python 
// MAGIC * Randomly Split the Hashed Featuresets into Training and Testing Datasets
// MAGIC * Binary Classification Estimator (LogisticRegression)
// MAGIC * Fit the model with training dataset
// MAGIC * Transform the trainned model and evaluate using the testing dataset
// MAGIC * Compare the label and prediction
// MAGIC * Evaluate the designed ML Logistic Classifier Algorithm (using the Class Evaluator Module in Spark)
// MAGIC * Accuracy + Test Error = 1  
// MAGIC * It can also be implemented in Scala

// COMMAND ----------

// MAGIC %md
// MAGIC ## Machine Learning Pipeline without Loop

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/17.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/18.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/19.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Machine Learning Pipeline with Loop

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/26.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 5. Create job to continuously train the algorithm
// MAGIC * Create a job task, upload the pipeline notebook, schedule the job and set notification mail

// COMMAND ----------

// MAGIC %md
// MAGIC ## Production and Scheduled Job for Continous Training of the Algorithm

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/20.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Job notification and updates

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/25.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Track the progress of the job

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/21.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Compare the predicted features of each run

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/24.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Accuracy Rate from Run 1

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/22.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Accuracy Rate from Run 2

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/23.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 6. Productionize the fitted Algorithm for Sentiment Analysis

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/27.PNG)
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/28.PNG)
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/29.PNG)
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/30.PNG)
// MAGIC ![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/31.PNG)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)