# Databricks notebook source exported at Sat, 25 Jun 2016 03:53:46 UTC
# MAGIC %md
# MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
# MAGIC 
# MAGIC ## Student Project Presentation by Shanshan Zhou
# MAGIC 
# MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

# COMMAND ----------

# MAGIC %md
# MAGIC # Identify hand motions from EEG recordings
# MAGIC ## by Shanshan Zhou
# MAGIC 
# MAGIC **Patients who have lost hand function due to amputation or neurological disabilities wake up to this reality everyday. **
# MAGIC 
# MAGIC * Restoring a patient's ability to perform these basic activities of daily life with a brain-computer interface (BCI) prosthetic device would greatly increase their independence and quality of life. 
# MAGIC * Currently, there are no realistic, affordable, or low-risk options for neurologically disabled patients to directly control external prosthetics with their brain activity.
# MAGIC 
# MAGIC **A possible solution ...**
# MAGIC * Recorded from the human scalp, EEG signals are evoked by brain activity. 
# MAGIC * The relationship between brain activity and EEG signals is complex and poorly understood outside of specific laboratory tests. 
# MAGIC * Providing affordable, low-risk, non-invasive BCI devices is dependent on further advancements in interpreting EEG signals. 

# COMMAND ----------

# MAGIC %md 
# MAGIC # A tutorial on how to process EEG data
# MAGIC ## by Alexandre Barachant
# MAGIC 
# MAGIC http://blog.kaggle.com/2015/10/12/grasp-and-lift-eeg-winners-interview-1st-place-cat-dog/

# COMMAND ----------

# MAGIC %scala
# MAGIC //This allows easy embedding of publicly available information into any other notebook
# MAGIC //when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
# MAGIC //Example usage:
# MAGIC // displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
# MAGIC def frameIt( u:String, h:Int ) : String = {
# MAGIC       """<iframe 
# MAGIC  src=""""+ u+""""
# MAGIC  width="95%" height="""" + h + """"
# MAGIC  sandbox>
# MAGIC   <p>
# MAGIC     <a href="http://spark.apache.org/docs/latest/index.html">
# MAGIC       Fallback link for browsers that, unlikely, don't support frames
# MAGIC     </a>
# MAGIC   </p>
# MAGIC </iframe>"""
# MAGIC    }
# MAGIC displayHTML(frameIt("http://blog.kaggle.com/2015/10/12/grasp-and-lift-eeg-winners-interview-1st-place-cat-dog/",600))

# COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/eeg/")) #data already in dbfs - see below for details

# COMMAND ----------

testRdd = sc.textFile('dbfs:/datasets/eeg/test.zip')
trainRdd = sc.textFile('dbfs:/datasets/eeg/train.zip')

# COMMAND ----------

# MAGIC %fs ls "dbfs:/home/ubuntu/databricks/EEG/train"

# COMMAND ----------

subj3_series3_events_Path = "dbfs:/home/ubuntu/databricks/EEG/train/subj3_series3_events.csv"
subj3_series4_events_Path = "dbfs:/home/ubuntu/databricks/EEG/train/subj3_series4_events.csv" 

# COMMAND ----------

subj3_series3_data_Path = "dbfs:/home/ubuntu/databricks/EEG/train/subj3_series3_data.csv"
subj3_series4_data_Path = "dbfs:/home/ubuntu/databricks/EEG/train/subj3_series4_data.csv"

# COMMAND ----------

# MAGIC %md generate RDD

# COMMAND ----------

subj3_series3_events = sc.textFile(subj3_series3_events_Path)
subj3_series4_events = sc.textFile(subj3_series4_events_Path)
subj3_series34_events = subj3_series3_events.union(subj3_series4_events)

# COMMAND ----------

subj3_series3_data = sc.textFile(subj3_series3_data_Path)
subj3_series4_data = sc.textFile(subj3_series4_data_Path)
subj3_series34 = subj3_series3_data.union(subj3_series4_data)

# COMMAND ----------

# MAGIC %md generate DataFrame from csv file

# COMMAND ----------

subj3_series3_Raw_DF = sqlContext.read.format('com.databricks.spark.csv').options(header='true', inferSchema='true').load(subj3_series3_data_Path)

# COMMAND ----------

subj3_series3_DF = subj3_series3_Raw_DF.drop('id')

# COMMAND ----------

display(subj3_series3_DF)

# COMMAND ----------

# MAGIC %md create DF from RDD

# COMMAND ----------

subj3_series4_Raw_DF = subj3_series4_data.map(lambda x: (x, )).toDF()

# COMMAND ----------

subj3_series3_events_Raw_DF = sqlContext.read.format('com.databricks.spark.csv').options(header='true', inferSchema='true').load(subj3_series3_events_Path)
subj3_series4_events_Raw_DF = subj3_series4_events.map(lambda x: (x, )).toDF()


# COMMAND ----------

display(subj3_series3_events_DF)

# COMMAND ----------

# MAGIC %md
# MAGIC #neural oscillation
# MAGIC * neural oscillationis characterized by change in signal power in specific frequency bands. These oscillations appear naturally in ongoing EEG activity, can be induced by a specific task, for example a hand movement, or mental calculus. 
# MAGIC * For each subject, we should see a spot over the electrode C3 (Left motor cortex,corresponding to a right hand movement), and a decrease of the signal power in 
# MAGIC 10 and 20 Hz during the movement (by reference to after the movement).

# COMMAND ----------

subj3_series3_events_DF.filter("HandStart = 1").count()

# COMMAND ----------

subj3_series34.map(lambda x: (x, )).toDF().filter("HandStart = 1").count()

# COMMAND ----------

raw = creat_mne_raw_object(subj3_series3_DF)

# COMMAND ----------

# get chanel names
ch_names = list(subj3_series3_DF)
    
ch_names

# COMMAND ----------

# MAGIC %md
# MAGIC ### To get data to dbfs let's download and save.

# COMMAND ----------

# MAGIC %sh
# MAGIC pwd

# COMMAND ----------

# MAGIC %sh 
# MAGIC df -h /databricks/driver

# COMMAND ----------

# MAGIC %md
# MAGIC This data in `http://www.math.canterbury.ac.nz/~r.sainudiin/tmp/` may be deleted in the future.

# COMMAND ----------

# MAGIC %sh
# MAGIC wget http://www.math.canterbury.ac.nz/~r.sainudiin/tmp/test.zip

# COMMAND ----------

# MAGIC %sh
# MAGIC wget http://www.math.canterbury.ac.nz/~r.sainudiin/tmp/train.zip

# COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/eeg")

# COMMAND ----------

dbutils.fs.cp("file:/databricks/driver/train.zip","dbfs:/datasets/eeg/")

# COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/eeg/"))

# COMMAND ----------

testRdd = sc.textFile('dbfs:/datasets/eeg/test.zip')
trainRdd = sc.textFile('dbfs:/datasets/eeg/train.zip')

# COMMAND ----------

testRdd.take(5)

# COMMAND ----------

trainRdd.take(5)

# COMMAND ----------

# MAGIC %sh
# MAGIC rm train.zip test.zip

# COMMAND ----------



# COMMAND ----------

dbutils.fs.help()

# COMMAND ----------

# MAGIC %md
# MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
# MAGIC 
# MAGIC ## Student Project Presentation by Shanshan Zhou
# MAGIC 
# MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)