// Databricks notebook source exported at Fri, 17 Jun 2016 03:29:41 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week1/01_introduction/002_loginToDatabricks.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/week1/01_introduction/001_whySpark](http://img.youtube.com/vi/O8JbxgPpAU8/0.jpg)](https://www.youtube.com/v/O8JbxgPpAU8?rel=0&autoplay=1&modestbranding=1&start=3330&end=4511)





# Outline

### I. 21 Easy Steps for Sharing your AWS Educate Credits
* [Workspace -> scalable-data-science -> xtraResources -> awsEducate -> sharing (relative to 'Workspace' link!)](/#workspace/scalable-data-science/xtraResources/awsEducate/sharing) 
* If you are not in `*.cloud.databricks` or the above link is useless then go to [html here](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/2016/S1/xtraResources/awsEducate/sharing.html).
    
### II. 7 Steps to the Databricks Cloud
### III. Essentials of the Databricks Cloud





## I. Contributing your AWS credits to the course's databricks cluster.

Paul, add the steps for AWS credit sharing here.





## II. 7 Steps to the Databricks Cloud

### Step 1: go to [http://www.math.canterbury.ac.nz/databricks](http://www.math.canterbury.ac.nz/databricks) and login using your email address and temporary password given to you in person (now).






![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_01_sds_2016S1.png)






![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_02_sds_2016S1.png)





### Step 2: Change your password immediately (now).

![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_pswdChange_sds_2016S1.png)





![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_pswdChanged_sds_2016S1.png)





### Step 3: recognize your ``Home`` area in ``Workspace`` where you can read and write.

![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_03_sds_2016S1.png)





### Step 4: cloning the ``scalable-data-science/week1`` folder
![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_04_sds_2016S1.png)





### Step 5: rename the cloned ``week1 (*)`` folder as ``week1`` for simplicity.
![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_05_sds_2016S1.png)

**Note**: From week 2 onwards, you only need to clone the folder for that week (to preserve any changes you made to the notebooks from previous weeks).





### Step 6: loading the ``003_scalaCrashCourse`` notebook
![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_06_sds_2016S1.png)






![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_07_sds_2016S1.png)





### Step 7: Attaching ``003_scalaCrashCourse`` notebook to the databricks clusters
#### UC-enrolled students connect to ``studentsEnrolled`` cluster.
#### others plese connect to ``studentsObserving1`` cluster.
#### in the example below our mock student has connected to the ``classCluster`` cluster.
![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_08_sds_2016S1.png)





![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_09_sds_2016S1.png)





![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_10_sds_2016S1.png)

**Finally**, you are ready to use the notebook in your own Workspace and follow along the material being covered, execute cells, modify examples and try them out right away, take extra notes in mark-down enhanced via latex, etc.





## III. Essentials of Databricks Cloud (DBC)





## DBC Essentials: What is Databricks Cloud?

![DB workspace, spark, platform](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_WorkspaceSparkPlatform700x.png)





## DBC Essentials: Shard, Cluster, Notebook and Dashboard

![DB workspace, spark, platform](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_ShardClusterNotebookDashboard700x.png)





## DBC Essentials: Team, State, Collaboration, Elastic Resources

![DB workspace, spark, platform](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_TeamStateCollaborationElasticResources700x.png)





Let us dive into Scala crash course in a notebook!






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
