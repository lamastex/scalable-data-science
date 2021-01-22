// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Login to databricks
// MAGIC 
// MAGIC We will use databricks community edition and later on the databricks project shard granted for this course under the **databricks university alliance** with cloud computing grants from databricks for waived DBU units and AWS.
// MAGIC 
// MAGIC 
// MAGIC Please go here for a relaxed and detailed-enough tour (later):
// MAGIC 
// MAGIC * [https://docs.databricks.com/index.html](https://docs.databricks.com/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ## databricks community edition
// MAGIC 
// MAGIC 1. First obtain a free Obtain a databricks community edition account at:
// MAGIC  * [https://community.cloud.databricks.com](https://community.cloud.databricks.com)
// MAGIC 2. Let's get an overview of the databricks managed cloud for processing big data with Apache Spark

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
// MAGIC **DBC Essentials: Team, State, Collaboration, Elastic Resources in one picture**
// MAGIC 
// MAGIC ![DB workspace, spark, platform](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_TeamStateCollaborationElasticResources700x.png)

// COMMAND ----------

// MAGIC %md
// MAGIC **You Should All Have databricks community edition account by now!** and have successfully logged in to it.

// COMMAND ----------

// MAGIC %md
// MAGIC # Import Course Content Now!
// MAGIC 
// MAGIC Two Steps:
// MAGIC 
// MAGIC 1. Create a folder named `scalable-data-science` in your `Workspace` (NO Typos due to hard-coding of paths in the sequel!)
// MAGIC 
// MAGIC - Import the following `.dbc` archives from the following URL into `Workspace/scalable-data-science` folder you just created:
// MAGIC   - [https://github.com/lamastex/scalable-data-science/raw/master/dbcArchives/2021/](https://github.com/lamastex/scalable-data-science/raw/master/dbcArchives/2021/)
// MAGIC   - start with the first file for now and import more as needed:
// MAGIC     - [https://github.com/lamastex/scalable-data-science/tree/master/dbcArchives/2021/000_1-sds-3-x](https://github.com/lamastex/scalable-data-science/tree/master/dbcArchives/2021/000_1-sds-3-x)
// MAGIC     - ...

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Cloud-free Computing Environment 
// MAGIC 
// MAGIC **(Optional but strongly recommended)**
// MAGIC 
// MAGIC Before we dive into Scala crash course in a notebook, let's take a look at  TASK 2 of the first step in the [instructions](https://lamastex.github.io/scalable-data-science/sds/basics/instructions/) to set up a local and "cloud-free" computing environment, say on your laptop computer here:
// MAGIC 
// MAGIC - TASK 2 at [https://lamastex.github.io/scalable-data-science/sds/basics/instructions/prep/](https://lamastex.github.io/scalable-data-science/sds/basics/instructions/prep/).
// MAGIC 
// MAGIC This can be handy for prototyping quickly and may even be necessary due to sensitivity of data in certain projects that mandate the data to be confined to some on-premise cluster, etc.
// MAGIC 
// MAGIC **NOTE:** This can be done as an optional exercise as it heavily depends on your local computing environment and your software skills or willingness to acquire them.
// MAGIC 
// MAGIC **CAVEAT:** The docker-compose prepared for your local environment uses Spark 2.x instead of 3.x, but most of the contents here would run in either version of Spark. 
// MAGIC - Feel free to make PR with latest versions of Spark :)