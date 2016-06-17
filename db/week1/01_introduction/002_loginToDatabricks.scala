// Databricks notebook source exported at Fri, 17 Jun 2016 03:29:41 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week1/01_introduction/002_loginToDatabricks.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week1/01_introduction/001_whySpark](http://img.youtube.com/vi/O8JbxgPpAU8/0.jpg)](https://www.youtube.com/v/O8JbxgPpAU8?rel=0&autoplay=1&modestbranding=1&start=3330&end=4511)

// COMMAND ----------

// MAGIC %md
// MAGIC # Outline
// MAGIC 
// MAGIC ### I. 21 Easy Steps for Sharing your AWS Educate Credits
// MAGIC * [Workspace -> scalable-data-science -> xtraResources -> awsEducate -> sharing (relative to 'Workspace' link!)](/#workspace/scalable-data-science/xtraResources/awsEducate/sharing) 
// MAGIC * If you are not in `*.cloud.databricks` or the above link is useless then go to [html here](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/2016/S1/xtraResources/awsEducate/sharing.html).
// MAGIC     
// MAGIC ### II. 7 Steps to the Databricks Cloud
// MAGIC ### III. Essentials of the Databricks Cloud

// COMMAND ----------

// MAGIC %md
// MAGIC ## I. Contributing your AWS credits to the course's databricks cluster.
// MAGIC 
// MAGIC Paul, add the steps for AWS credit sharing here.

// COMMAND ----------

// MAGIC %md
// MAGIC ## II. 7 Steps to the Databricks Cloud
// MAGIC 
// MAGIC ### Step 1: go to [http://www.math.canterbury.ac.nz/databricks](http://www.math.canterbury.ac.nz/databricks) and login using your email address and temporary password given to you in person (now).

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_01_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_02_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Step 2: Change your password immediately (now).
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_pswdChange_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_pswdChanged_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Step 3: recognize your ``Home`` area in ``Workspace`` where you can read and write.
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_03_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Step 4: cloning the ``scalable-data-science/week1`` folder
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_04_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Step 5: rename the cloned ``week1 (*)`` folder as ``week1`` for simplicity.
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_05_sds_2016S1.png)
// MAGIC 
// MAGIC **Note**: From week 2 onwards, you only need to clone the folder for that week (to preserve any changes you made to the notebooks from previous weeks).

// COMMAND ----------

// MAGIC %md
// MAGIC ### Step 6: loading the ``003_scalaCrashCourse`` notebook
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_06_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_07_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Step 7: Attaching ``003_scalaCrashCourse`` notebook to the databricks clusters
// MAGIC #### UC-enrolled students connect to ``studentsEnrolled`` cluster.
// MAGIC #### others plese connect to ``studentsObserving1`` cluster.
// MAGIC #### in the example below our mock student has connected to the ``classCluster`` cluster.
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_08_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_09_sds_2016S1.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbLogin_10_sds_2016S1.png)
// MAGIC 
// MAGIC **Finally**, you are ready to use the notebook in your own Workspace and follow along the material being covered, execute cells, modify examples and try them out right away, take extra notes in mark-down enhanced via latex, etc.

// COMMAND ----------

// MAGIC %md
// MAGIC ## III. Essentials of Databricks Cloud (DBC)

// COMMAND ----------

// MAGIC %md
// MAGIC ## DBC Essentials: What is Databricks Cloud?
// MAGIC 
// MAGIC ![DB workspace, spark, platform](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_WorkspaceSparkPlatform700x.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ## DBC Essentials: Shard, Cluster, Notebook and Dashboard
// MAGIC 
// MAGIC ![DB workspace, spark, platform](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_ShardClusterNotebookDashboard700x.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ## DBC Essentials: Team, State, Collaboration, Elastic Resources
// MAGIC 
// MAGIC ![DB workspace, spark, platform](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_TeamStateCollaborationElasticResources700x.png)

// COMMAND ----------

// MAGIC %md
// MAGIC Let us dive into Scala crash course in a notebook!

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)