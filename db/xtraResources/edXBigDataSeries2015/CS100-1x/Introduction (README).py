# Databricks notebook source exported at Mon, 14 Mar 2016 03:14:00 UTC
# MAGIC %md
# MAGIC **SOURCE:** This is from the Community Edition of databricks and has been added to this databricks shard at [/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x](/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x) as extra resources for the project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) that is prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand), and *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome). 

# COMMAND ----------

# MAGIC %md
# MAGIC <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License</a>.

# COMMAND ----------

# MAGIC %md
# MAGIC #![Spark Logo](http://spark-mooc.github.io/web-assets/images/ta_Spark-logo-small.png)
# MAGIC # Introduction to Big Data with Apache Spark

# COMMAND ----------

# MAGIC %md
# MAGIC Learn how to apply data science techniques using parallel programming in Apache Spark to explore big (and small) data.
# MAGIC 
# MAGIC >This course was taught in June, 2015 on the [EdX platform](https://www.class-central.com/mooc/3026/edx-cs100-1x-introduction-to-big-data-with-apache-spark) and made the list of [Class Central's 10 best online courses](https://www.class-central.com/report/best-free-online-courses-2015/). Over 76,000 students registered, out of which roughly 12% graduated, which is about twice the average of the MOOCs taught on EdX and other premier online education platforms.  

# COMMAND ----------

# MAGIC %md
# MAGIC Organizations use their data for decision support and to build data-intensive products and services, such as recommendation, prediction, and diagnostic systems. The collection of skills required by organizations to support these functions has been grouped under the term Data Science. This course will attempt to articulate the expected output of Data Scientists and then teach students how to use PySpark (part of Apache Spark) to deliver against these expectations. The course assignments include Log Mining, Textual Entity Recognition, Collaborative Filtering exercises that teach students how to manipulate data sets using parallel processing with PySpark.
# MAGIC 
# MAGIC This course covers advanced undergraduate-level material. It requires a programming background and experience with Python (or the ability to learn it quickly). All exercises will use PySpark (part of Apache Spark), but previous experience with Spark or distributed computing is NOT required. Students should take this Python [mini-course](http://ai.berkeley.edu/tutorial.html#PythonBasics) if they need to learn Python or refresh their Python knowledge.

# COMMAND ----------

# MAGIC %md
# MAGIC #### What you'll learn
# MAGIC * Learn how to use Apache Spark to perform data analysis
# MAGIC * How to use parallel programming to explore data sets
# MAGIC * Apply Log Mining, Textual Entity Recognition and Collaborative Filtering to real world data questions

# COMMAND ----------

# MAGIC %md
# MAGIC #### How to proceed
# MAGIC 
# MAGIC The course is separated into five modules.  The video lectures for each module can be found in the *Module [x]: Lecture(s)* notebook.  For example, *Module 1: Lectures* is the lecture notebook for the first module.
# MAGIC 
# MAGIC Modules two through five include labs.  These are named *Module [x]: [lab name] Lab*.  For example, *Module 2: Word Count Lab*.  Solutions to the labs can be found in the *Solutions* folder.
# MAGIC 
# MAGIC We recommend that you proceed through the modules in order.