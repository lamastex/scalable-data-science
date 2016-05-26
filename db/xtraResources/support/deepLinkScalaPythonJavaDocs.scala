// Databricks notebook source exported at Wed, 23 Mar 2016 20:09:17 UTC
// MAGIC %md
// MAGIC ### A Note on being able to link-in scala docs from URL
// MAGIC 
// MAGIC Unfortunately we cannot deep-link-in to scala docs as explained here: [http://www.scala-lang.org/old/node/8798.html](http://www.scala-lang.org/old/node/8798.html).
// MAGIC 
// MAGIC Our best option now seems to go to the following link and search for randomSplit in the search-box there:
// MAGIC * [http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame)
// MAGIC 
// MAGIC The syntax for randomSplit (obtained by converting the source of the highlighted html resulting from the search from the link above and converting it to markdown via pandoc in bash) is as follows:
// MAGIC 
// MAGIC #### def randomSplit(weights: Array[Double], seed: Long): Array[[DataFrame]()]
// MAGIC 
// MAGIC Randomly splits this [DataFrame]() with the provided weights.
// MAGIC 
// MAGIC weights
// MAGIC :   weights for splits, will be normalized if they don't sum to 1.
// MAGIC 
// MAGIC seed
// MAGIC :   Seed for sampling.
// MAGIC 
// MAGIC Since
// MAGIC :   1.4.0
// MAGIC 
// MAGIC 
// MAGIC Python is ok http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.DataFrame.randomSplit
// MAGIC 
// MAGIC Java too http://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/DataFrame.html#randomSplit(double%5B%5D%2C%20long)

// COMMAND ----------

// MAGIC %md
// MAGIC Can anyone help with being able to better link to syntax for scala docs?

// COMMAND ----------

// MAGIC %md
// MAGIC Confirming that ScalaDoc doesn’t support deep linking to class methods. L
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC http://www.scala-lang.org/old/node/8798.html
// MAGIC 
// MAGIC Daniel Dekany :
// MAGIC 
// MAGIC > You can't link to methods in Scaladoc 2, right? I tried
// MAGIC 
// MAGIC > [[com.example.Class#method]], [[com.example.Class.method]],
// MAGIC 
// MAGIC > [[com.example.Class.method()]] etc., and they just end up being plain
// MAGIC 
// MAGIC > text in the HTML. Other projects that I have looked at either didn't
// MAGIC 
// MAGIC > have links to methods or just used instead of [[...]].
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC It has been discussed. The major limitation of a simple solution would
// MAGIC 
// MAGIC be ignoring alternatives of multiple overloaded methods. Would you
// MAGIC 
// MAGIC mind opening a Trac enhancement request? Depending on the feedback
// MAGIC 
// MAGIC from EPFL guys I may provide a implementation.
// MAGIC 
// MAGIC PF
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC Even if you follow this, you can’t get deep links to methods, you just land on the class page:
// MAGIC 
// MAGIC http://docs.scala-lang.org/overviews/scaladoc/interface
// MAGIC 
// MAGIC The letters underneath the search box list all fields, methods and other tokens found during the creation of the Scaladoc. E.g. if you want to find where the .reverse method you are using is defined, click on R in the list of letters there, and then find in page to locate the .reverse method and the list of implementing classes/traits.

// COMMAND ----------

// MAGIC %md
// MAGIC #### databricks support
// MAGIC 
// MAGIC From: Brian Clapper at databricks 20160324
// MAGIC 
// MAGIC As near as I can tell, this issue (deep-linking to methods in a Scala class) is still not resolved. Methods do have HTML IDs, which means, in theory, you could construct a URL fragment to point to one. But Scaladoc already uses the fragment (the “#” sign part of the URL) to specify the class, and you can’t specify more than one fragment.
// MAGIC 
// MAGIC This, obviously, is not a problem we can solve. It’s a Scaladoc problem, and we have no control over that.