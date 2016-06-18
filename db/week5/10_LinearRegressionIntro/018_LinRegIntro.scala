// Databricks notebook source exported at Sat, 18 Jun 2016 23:27:13 UTC
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
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week5/10_LinearRegressionIntro/018_LinRegIntro.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week5/10_LinearRegressionIntro/018_LinRegIntro](http://img.youtube.com/vi/y6F-e6m1m2s/0.jpg)](https://www.youtube.com/v/y6F-e6m1m2s?rel=0&autoplay=1&modestbranding=1&start=2635&end=3918)

// COMMAND ----------

// MAGIC %md
// MAGIC Let us visit an interactive visual cognitive tool for the basics ideas in linear regression:
// MAGIC * [http://setosa.io/ev/ordinary-least-squares-regression/](http://setosa.io/ev/ordinary-least-squares-regression/)
// MAGIC 
// MAGIC The following video is a very concise and thorough treatment of linear regression for those who have taken the 200-level linear algebra. Others can fully understand it with some effort and revisiting. 

// COMMAND ----------

// MAGIC %md
// MAGIC #### Linear Regression by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch now 11:13)**:
// MAGIC 
// MAGIC [![Linear Regression by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/0wcMCQ8SyZM/0.jpg)](https://www.youtube.com/v/0wcMCQ8SyZM?rel=0&autoplay=1&modestbranding=1&start=1)

// COMMAND ----------

// MAGIC %md
// MAGIC Ridge regression has a Bayesian interpretation where the weights have a zero-mean Gaussian prior. See 7.5 in Murphy's Machine Learning: A Probabilistic Perspective for details.

// COMMAND ----------

// MAGIC %md
// MAGIC Please take notes in mark-down if you want.
// MAGIC 
// MAGIC For latex math within markdown you can do the following for in-line maths: \\( \mathbf{A}_{i,j} \in \mathbb{R}^1 \\). And to write maths in display mode do the following:
// MAGIC 
// MAGIC $$\mathbf{A} \in \mathbb{R}^{m \times d} $$
// MAGIC 
// MAGIC You will need to write such notes for your final project presentation!

// COMMAND ----------

// MAGIC %md
// MAGIC #### MillonSongs Ridge Regression by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch later 7:47)**:
// MAGIC 
// MAGIC [![Linear Regression by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/iS2QxI57OJs/0.jpg)](https://www.youtube.com/v/iS2QxI57OJs?rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC 
// MAGIC 
// MAGIC Covers the training, test and validation and grid search... ridger regression...

// COMMAND ----------

// MAGIC %md
// MAGIC Take your own notes if you like.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Gradient Descent by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch now 11:19)**:
// MAGIC 
// MAGIC [![Gradient Descent by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/9AZYy36qLqU/0.jpg)](https://www.youtube.com/v/9AZYy36qLqU?rel=0&autoplay=1&modestbranding=1&start=1)

// COMMAND ----------

// MAGIC %md
// MAGIC Please take notes if you want to.

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