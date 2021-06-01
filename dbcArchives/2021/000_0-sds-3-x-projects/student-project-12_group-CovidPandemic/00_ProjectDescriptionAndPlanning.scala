// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Project Description and Introduction

// COMMAND ----------

// MAGIC %md
// MAGIC Project Members: <br>
// MAGIC Chi Zhang, zchi@chalmers.se; <br>
// MAGIC Shuangshuang Chen, shuche@kth.se; <br>
// MAGIC Magnus Tarle, tarle@kth.se

// COMMAND ----------

// MAGIC %md
// MAGIC ## Project presentation video
// MAGIC The presentation record link is here: https://drive.google.com/drive/folders/13XwlItZ_qtOeBZ5TJfnCP1hqtQ9imRFq <br>
// MAGIC The final complete 20 minutes presentation is: "Project_complete_video.mp4" <br>
// MAGIC Because the cluster seems quite slow when we recording the video and there are too many things to run within 20 minutes, we recorded the video after we finish running all the codes. And some parts of the video are speeded up to meet the 20 minutes requirement. We also put the original raw video records in the folder, from 00 to 08 videos correspond to the script files from 00 to 08, in case you only want to know about a certain part.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Project Introduction - Analysis and Prediction of COVID-19 Data
// MAGIC In this project, we use both scala (data processing part) and python (algorithm part).
// MAGIC We deal with scalable data, and use what we learned from the course.

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. Project Plan 
// MAGIC In this project, we dealt with COVID-19 data, and do the following tasks:
// MAGIC 
// MAGIC 0. Introduction 
// MAGIC 1. Struct stream data - to update our database once a day.
// MAGIC 2. Preprocessing - clean data.
// MAGIC 3. Visualization - visualize new cases on a world map, with different time scope.
// MAGIC 4. Statistics analysis - get the distributions, mean and std of different varables.
// MAGIC 5. Model 1: K-means - clustering of different countries
// MAGIC 6. Model 2: Linear Regression - predict new cases of some contries from other countries.
// MAGIC 7. Model 3: Autoregressive integrated moving average (ARIMA) - prediction for new cases and new deaths from past values.
// MAGIC 8. Model 4: Gaussian Procecces (GP) - apply Gaussian Procecces (GP) to predict mean and covariance of new cases and new deaths from past values. 
// MAGIC 9. Ending

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Tools and methods
// MAGIC For the method part, this particular task is not suitable to use deep learning methods. We choose 4 methods related to Machine leanring for our clustering and prediction task:
// MAGIC 1. Clustering model - K-means
// MAGIC 2. Time series model - Autoregressive integrated moving average (ARIMA)
// MAGIC 3. Gaussian Procecces (GP)
// MAGIC 4. Linear Regression (LR)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. Data resources
// MAGIC We found the following data resources. And finally we chose the 3rd dataset because it has the most features for us to use. 
// MAGIC 
// MAGIC 1. WHO: https://covid19.who.int/WHO-COVID-19-global-data.csv
// MAGIC 
// MAGIC 2. COVID Tracking Project API at Cambridge: https://api.covidtracking.com
// MAGIC 
// MAGIC 3. Data on COVID-19 (coronavirus) by Our World in Data: git repo https://github.com/owid/covid-19-data/tree/master/public/data
// MAGIC 
// MAGIC 4. Sweden (proceesed by apify) API: https://api.apify.com/v2/datasets/Nq3XwHX262iDwsFJS/items?format=json&clean=1
// MAGIC 
// MAGIC Note that the preprocessing, visualization and analysis within these notebooks were made on this 3rd dataset, downloaded December 2020 to Databricks. One dataset from December 2020, "owid-covid-data.csv", can also be found in the same google drive folder as the video presentation.
// MAGIC 
// MAGIC <!-- ### Some additional ideas (added by Magnus)
// MAGIC 1. Join any of the above mentioned COVID-19 API:s with another API source to get new insights by looking at correlations, clustering. Might be difficult to find a good interesting source but there seems to be some datasources listed e.g. here: https://www.octoparse.com/blog/big-data-70-amazing-free-data-sources-you-should-know-for-2017. One could imagine e.g. crime vs covid or economic sector vs covid etc. 
// MAGIC 2. Perhaps a bit algorithm specific - apply Gaussian Procecces (GP) to predict mean and covariance. Reason being I have for some time wanted to learn GP as they seem pretty useful. If you want to have an intro to GP, you could look at: http://krasserm.github.io/2018/03/19/gaussian-processes/ and https://medium.com/@dan_90739/being-bayesian-and-thinking-deep-time-series-prediction-with-uncertainty-25ff581b056c  -->

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### 4. Useful links
// MAGIC 
// MAGIC 1. Description of each column in the "Our World in Data" dataset
// MAGIC https://github.com/owid/covid-19-data/blob/master/public/data/owid-covid-codebook.csv
// MAGIC 2. If you want to have an intro to GP, you could look at: http://krasserm.github.io/2018/03/19/gaussian-processes/ and https://medium.com/@dan_90739/being-bayesian-and-thinking-deep-time-series-prediction-with-uncertainty-25ff581b056c
// MAGIC 3. ARIMA - Autoregressive Integrated Moving Average model. It's widely used in time series analysis. see defination here: https://en.wikipedia.org/wiki/Autoregressive_integrated_moving_average

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5. Meeting Records:
// MAGIC Weekly meetings for group discussion:
// MAGIC 
// MAGIC 1. 2020-12-01 Tuesday:
// MAGIC   * Discussion about project topic
// MAGIC   * Find data resources
// MAGIC 
// MAGIC 2. 2020-12-04 Friday:
// MAGIC    * Understand dataset 3, and manage download data
// MAGIC    * Set up processing pipeline into dataframe
// MAGIC    * Prepared for statistics analysis
// MAGIC    * Selected the column for analysis and machine learning
// MAGIC    * Finished struct stream for updating data
// MAGIC 
// MAGIC 3. 2020-12-08 Tuesday:
// MAGIC    * Exploited each chosen column, plot statistics
// MAGIC    * Dealt with missing data
// MAGIC    * Post-process for useful features from existed column (for statistics analysis and prediction/regression)
// MAGIC    
// MAGIC 4. 2020-12-16 Wednesday:
// MAGIC   * Finished statistics analysis, which correlation would interesting to show
// MAGIC   * Progress on visulization
// MAGIC   * Progress on GP model
// MAGIC   * Progress on LR model
// MAGIC   * Progress on ARIMA model
// MAGIC   
// MAGIC 5. 2020-12-18 Friday:
// MAGIC   * Progress on K-means model
// MAGIC   * Finish visulization
// MAGIC   * Finish other models
// MAGIC   * Finish Evaluation
// MAGIC 
// MAGIC 6. 2021-01-07 Thursday:
// MAGIC   * Finish all models
// MAGIC   * Discussing about the final presentation

// COMMAND ----------

// MAGIC %md
// MAGIC ## Project Progress

// COMMAND ----------

// MAGIC %md
// MAGIC ### 0. Introduction of the data
// MAGIC 
// MAGIC The columns we selected to analyze are:
// MAGIC - continent
// MAGIC - location
// MAGIC - date
// MAGIC - total_cases
// MAGIC - new_cases
// MAGIC - total_deaths
// MAGIC - new_deaths
// MAGIC - reproduction_rate
// MAGIC - icu_patients 
// MAGIC - hosp_patients
// MAGIC - weekly_icu_admissions 
// MAGIC - weekly_hosp_admissions
// MAGIC - total_tests 
// MAGIC - new_tests
// MAGIC - stingency_index
// MAGIC - population
// MAGIC - population_density
// MAGIC - median_age
// MAGIC - aged_65_older 
// MAGIC - aged_70_older
// MAGIC - gdp_per_capita
// MAGIC - extreme_poverty
// MAGIC - cardiovasc_death_rate
// MAGIC - diabetes_prevalence
// MAGIC - female_smokers
// MAGIC - male_smokers
// MAGIC - hospital_beds_per_thousand
// MAGIC - life_expectancy
// MAGIC - human_development_index

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. Streaming
// MAGIC This part has been moved to separate files: "DownloadFilesPeriodicallyScript" and "StreamToFile".

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Preprocessing

// COMMAND ----------

// MAGIC %md
// MAGIC To **Rerun Steps 1-5** done in the notebook at:
// MAGIC * `Workspace -> PATH_TO -> DataPreprocess]`
// MAGIC 
// MAGIC just `run` the following command as shown in the cell below: 
// MAGIC 
// MAGIC   ```%scala
// MAGIC   %run "PATH_TO/DataPreprocess"
// MAGIC   ```
// MAGIC   
// MAGIC    * *Note:* If you already evaluated the `%run ...` command above then:
// MAGIC      * first delete the cell by pressing on `x` on the top-right corner of the cell and 
// MAGIC      * revaluate the `run` command above.

// COMMAND ----------

// MAGIC %run "./02_DataPreprocess"

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. Explosive Analysis

// COMMAND ----------

// MAGIC %run "./03_ExplosiveAnalysis"

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### 4. Visualization

// COMMAND ----------

// MAGIC %run "./04_DataVisualize"

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5. Model 1: Clustering K-means

// COMMAND ----------

// MAGIC %md
// MAGIC Clustering of countries based on the dataset features of each country.
// MAGIC 
// MAGIC This part is in notebook 05_Clustering.<br>
// MAGIC The code can be run either in here or in the 05_Clustering notebook (after data preprocessing). We run and show the results in the 05_Clustering notebook.

// COMMAND ----------

// MAGIC %run "./05_Clustering"

// COMMAND ----------

// MAGIC %md
// MAGIC ### 6. Model 2: Linear Regression (LR) Model

// COMMAND ----------

// MAGIC %md
// MAGIC Prediction with constant values, predict new cases of some contries from other countries.
// MAGIC 
// MAGIC This part is in notebook 06_DataPrediction_LR. <br>
// MAGIC The code can be run either in here or in the 06_DataPrediction_LR notebook (after data preprocessing). We run and show the results in the 06_DataPrediction_LR notebook.

// COMMAND ----------

// MAGIC %run "./06_DataPredicton_LR"

// COMMAND ----------

// MAGIC %md
// MAGIC ### 7. Model 3: Autoregressive integrated moving average (ARIMA)

// COMMAND ----------

// MAGIC %md 
// MAGIC prediction for new cases and new deaths from past values.
// MAGIC 
// MAGIC This part is in notebook 07_DataPrediction_ARIMA.<br>
// MAGIC The code can be run either in here or in the 07_DataPrediction_ARIMA notebook (after data preprocessing). We run and show the results in the 07_DataPrediction_ARIMA notebook.

// COMMAND ----------

// MAGIC %run "./07_DataPredicton_ARIMA"

// COMMAND ----------

// MAGIC %md
// MAGIC ### 8. Model 4: Gaussian Procecces (GP)

// COMMAND ----------

// MAGIC %md
// MAGIC apply Gaussian Procecces (GP) to predict mean and covariance of new cases and new deaths from past values. 
// MAGIC 
// MAGIC This part is in notebook 08_DataPrediction_GP. <br>
// MAGIC The code can be run either in here or in the 08_DataPrediction_GP notebook (after data preprocessing). We run and show the results in the 08_DataPrediction_GP notebook.

// COMMAND ----------

// MAGIC %run "./08_DataPrediction_GP"