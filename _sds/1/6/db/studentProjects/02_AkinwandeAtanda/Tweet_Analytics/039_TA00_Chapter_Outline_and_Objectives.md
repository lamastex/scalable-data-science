---
title: ""
permalink: /sds/1/6/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/039_TA00_Chapter_Outline_and_Objectives/
sidebar:
  nav: "lMenu-SDS-1.6"
---

// Databricks notebook source exported at Sun, 26 Jun 2016 01:34:53 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/039_TA00_Chapter_Outline_and_Objectives.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/039_TA00_Chapter_Outline_and_Objectives](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=0&end=1611)





#Tweet Analytics

[Presentation contents](https://github.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016).





## Chapter Outline and Objectives
The objectives of this project are:
1. Use Apache Spark Streaming API to collect Tweets
  * Unfiltered Tweets Collector Set-up
  * Filtered Tweets Collector Set-up by Keywords and Hashtags
  * Filtered Tweets Collector Set-up by Class
2. Extract-Transform-Load (ETL) the collected Tweets:
  * Read/Load the Streamed Tweets in batches of RDD
  * Read/Load the Streamed Tweets in merged batches of RDDs
  * Save the Tweets in Parquet format, convert to Dataframe Table and run SQL queries
  * Explore the Streamed Tweets using SQL queries: Filter, Plot and Re-Shape
3. Build a featurization dataset for Machine Learning Job
  * Explore the binarize classification
4. Build a Machine Learning Classification Algorithm Pipeline
  * Pipeline without loop
  * Pipeline with loop
5. Create job to continuously train the algorithm
6. Productionize the fitted algorithm for Sentiment Analysis





## Notebook Execution Order and Description
The chronological order of executing the notebooks for this chapter are listed as follows with corresponidng title:
1. Start a Spark Streaming Job
  * Execute either of the three collectors depending on the project objective:
  * 040_TA01_01_Unfiltered_Tweets_Collector_Set-up
  * 041_TA01_02_Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags
  * 042_TA01_03_Filtered_Tweets_Collector_Set-up_by_Class
2. Process the collected Tweets using the ETL framework
  * 043_TA02_ETL_Tweets
3. Download, Load and Explore the Features Dataset for the Machine Learning Job
  * 044_TA03_01_binary_classification
4. Build a Machine Learning Classification Algorithm Pipeline
 * Run either of the notebooks depending on the choice of Elastic Parameter values
 * 045_TA03_02_binary_classification
 * 046_TA03_03_binary_classification_with_Loop
5. Productionalize the trainned Algorithm with Historical Tweets
  * 047_TA03_04_binary_classification_with_Loop_TweetDataSet





##1. Use Apache Spark Streaming API to collect Tweets
  * Unfiltered Tweets Collector Set-up
  * Filtered Tweets Collector Set-up by Keywords and Hashtags
  * Filtered Tweets Collector Set-up by Class





## Unfiltered Tweet Collector





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/1.PNG)





## Filtered Tweets Collector Set-up by Keywords and Hashtags





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/2-1.PNG)


```scala

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

```



![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/3.PNG)





## Filtered Tweets Collector Set-up by Class





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/4.PNG)





## 2. Extract-Transform-Load (ETL) the collected Tweets:
  * Read/Load the Streamed Tweets in batches of RDD
  * Read/Load the Streamed Tweets in merged batches of RDDs
  * Save the Tweets in Parquet format, convert to Dataframe Table and run SQL queries
  * Explore the Streamed Tweets using SQL queries: Filter, Plot and Re-Shape
Extract--Transform--Load: Streamed Tweets





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/5.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/6.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/7.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/8.PNG)





## Explore Streamed Tweets: SQL, Filter, Plot and Re-Shape





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/9.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/10-1.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/11.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/12.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/13.PNG)





## 3. Featurization: Training and Testing Datasets sourced from:
* Amazon Products Reviews; 
* Vector of Positive & Negative Words [from "Workshop on text analysis" by Neal Caren](http://nbviewer.jupyter.org/github/nealcaren/workshop_2014/tree/master/notebooks/)
* [NLTK corpus movie reviews (Postive and Negative Reviews)](http://streamhacker.com/tag/sentiment/)
* [Random Products Reviews (Web Scraped by Sandtex)](https://pythonprogramming.net/static/downloads/short_reviews/)
* Download the featured dataset ["pos_neg_category"](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/pos_neg_category.csv)
* Upload as a Table and change the data type for the "category" column to double





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/14.PNG)





## Exploration: Training and Testing Datasets in R





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/15.PNG)





** Convert to DataFrame in R**





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/16.PNG)





## 4. Build a Machine Learning Classification Algorithm Pipeline
  * Pipeline without loop
  * Pipeline with loop





## Machine Learning Pipeline: Training and Testing Datasets in Python 
* Randomly Split the Hashed Featuresets into Training and Testing Datasets
* Binary Classification Estimator (LogisticRegression)
* Fit the model with training dataset
* Transform the trainned model and evaluate using the testing dataset
* Compare the label and prediction
* Evaluate the designed ML Logistic Classifier Algorithm (using the Class Evaluator Module in Spark)
* Accuracy + Test Error = 1  
* It can also be implemented in Scala





## Machine Learning Pipeline without Loop





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/17.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/18.PNG)





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/19.PNG)





## Machine Learning Pipeline with Loop





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/26.PNG)





## 5. Create job to continuously train the algorithm
* Create a job task, upload the pipeline notebook, schedule the job and set notification mail





## Production and Scheduled Job for Continous Training of the Algorithm





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/20.PNG)





## Job notification and updates





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/25.PNG)





## Track the progress of the job





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/21.PNG)





## Compare the predicted features of each run





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/24.PNG)





### Accuracy Rate from Run 1





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/22.PNG)





### Accuracy Rate from Run 2





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/23.PNG)





## 6. Productionize the fitted Algorithm for Sentiment Analysis





![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/27.PNG)
![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/28.PNG)
![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/29.PNG)
![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/30.PNG)
![](https://raw.githubusercontent.com/aaa121/Spark-Tweet-Streaming-Presentation-May-2016/master/31.PNG)






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### Course Project by [Akinwande Atanda](https://nz.linkedin.com/in/akinwande-atanda)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
