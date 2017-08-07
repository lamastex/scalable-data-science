---
title: ""
permalink: /sds/1/6/db/studentProjects/05_ShanshanZhou/051_EEG_Explore/
sidebar:
  nav: "lMenu-SDS-1.6"
---

# Databricks notebook source exported at Tue, 28 Jun 2016 10:38:24 UTC

# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)

## Student Project Presentation by Shanshan Zhou

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/05_ShanshanZhou/051_EEG_Explore.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/05_ShanshanZhou/051_EEG_Explore](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=4677&)





# Identify hand motions from EEG recordings
## by Shanshan Zhou

**Patients who have lost hand function due to amputation or neurological disabilities wake up to this reality everyday. **

* Restoring a patient's ability to perform these basic activities of daily life with a brain-computer interface (BCI) prosthetic device would greatly increase their independence and quality of life. 
* Currently, there are no realistic, affordable, or low-risk options for neurologically disabled patients to directly control external prosthetics with their brain activity.

**A possible solution ...**
* Recorded from the human scalp, EEG signals are evoked by brain activity. 
* The relationship between brain activity and EEG signals is complex and poorly understood outside of specific laboratory tests. 
* Providing affordable, low-risk, non-invasive BCI devices is dependent on further advancements in interpreting EEG signals. 




 
# A tutorial on how to process EEG data
## by Alexandre Barachant

http://blog.kaggle.com/2015/10/12/grasp-and-lift-eeg-winners-interview-1st-place-cat-dog/


```python

%scala
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
displayHTML(frameIt("http://blog.kaggle.com/2015/10/12/grasp-and-lift-eeg-winners-interview-1st-place-cat-dog/",600))

```
```python

display(dbutils.fs.ls("dbfs:/datasets/eeg/")) #data already in dbfs - see below for details

```
```python

testRdd = sc.textFile('dbfs:/datasets/eeg/test.zip')
trainRdd = sc.textFile('dbfs:/datasets/eeg/train.zip')

```
```python

%fs ls "dbfs:/home/ubuntu/databricks/EEG/train"

```
```python

subj3_series3_events_Path = "dbfs:/home/ubuntu/databricks/EEG/train/subj3_series3_events.csv"
subj3_series4_events_Path = "dbfs:/home/ubuntu/databricks/EEG/train/subj3_series4_events.csv" 

```
```python

subj3_series3_data_Path = "dbfs:/home/ubuntu/databricks/EEG/train/subj3_series3_data.csv"
subj3_series4_data_Path = "dbfs:/home/ubuntu/databricks/EEG/train/subj3_series4_data.csv"

```


 generate RDD


```python

subj3_series3_events = sc.textFile(subj3_series3_events_Path)
subj3_series4_events = sc.textFile(subj3_series4_events_Path)
subj3_series34_events = subj3_series3_events.union(subj3_series4_events)

```
```python

subj3_series3_data = sc.textFile(subj3_series3_data_Path)
subj3_series4_data = sc.textFile(subj3_series4_data_Path)
subj3_series34 = subj3_series3_data.union(subj3_series4_data)

```


 generate DataFrame from csv file


```python

subj3_series3_Raw_DF = sqlContext.read.format('com.databricks.spark.csv').options(header='true', inferSchema='true').load(subj3_series3_data_Path)

```
```python

subj3_series3_DF = subj3_series3_Raw_DF.drop('id')

```
```python

display(subj3_series3_DF)

```


 create DF from RDD


```python

subj3_series4_Raw_DF = subj3_series4_data.map(lambda x: (x, )).toDF()

```
```python

subj3_series3_events_Raw_DF = sqlContext.read.format('com.databricks.spark.csv').options(header='true', inferSchema='true').load(subj3_series3_events_Path)
subj3_series4_events_Raw_DF = subj3_series4_events.map(lambda x: (x, )).toDF()


```
```python

display(subj3_series3_events_DF)

```



#neural oscillation
* neural oscillationis characterized by change in signal power in specific frequency bands. These oscillations appear naturally in ongoing EEG activity, can be induced by a specific task, for example a hand movement, or mental calculus. 
* For each subject, we should see a spot over the electrode C3 (Left motor cortex,corresponding to a right hand movement), and a decrease of the signal power in 
10 and 20 Hz during the movement (by reference to after the movement).


```python

subj3_series3_events_DF.filter("HandStart = 1").count()

```
```python

subj3_series34.map(lambda x: (x, )).toDF().filter("HandStart = 1").count()

```
```python

raw = creat_mne_raw_object(subj3_series3_DF)

```
```python

# get chanel names
ch_names = list(subj3_series3_DF)
    
ch_names

```



### To get data to dbfs let's download and save.


```python

%sh
pwd

```
```python

%sh 
df -h /databricks/driver

```



This data in `http://www.math.canterbury.ac.nz/~r.sainudiin/tmp/` may be deleted in the future.


```python

%sh
wget http://www.math.canterbury.ac.nz/~r.sainudiin/tmp/test.zip

```
```python

%sh
wget http://www.math.canterbury.ac.nz/~r.sainudiin/tmp/train.zip

```
```python

dbutils.fs.mkdirs("dbfs:/datasets/eeg")

```
```python

dbutils.fs.cp("file:/databricks/driver/train.zip","dbfs:/datasets/eeg/")

```
```python

display(dbutils.fs.ls("dbfs:/datasets/eeg/"))

```
```python

testRdd = sc.textFile('dbfs:/datasets/eeg/test.zip')
trainRdd = sc.textFile('dbfs:/datasets/eeg/train.zip')

```
```python

testRdd.take(5)

```
```python

trainRdd.take(5)

```
```python

%sh
rm train.zip test.zip

```
```python



```
```python

dbutils.fs.help()

```



# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)

## Student Project Presentation by Shanshan Zhou

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
