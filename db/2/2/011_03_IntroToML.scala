// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/3x5iQoXm3cc/0.jpg)](https://www.youtube.com/embed/3x5iQoXm3cc?start=514&end=1219&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Introduction to Machine Learning
// MAGIC 
// MAGIC **Some very useful resources we will weave around** for Statistical Learning, Data Mining, Machine Learning:
// MAGIC 
// MAGIC *  January 2014, Stanford University professors Trevor Hastie and Rob Tibshirani (authors of the legendary Elements of Statistical Learning textbook) taught an online course based on their newest textbook, An Introduction to Statistical Learning with Applications in R (ISLR).
// MAGIC   * [http://www.r-bloggers.com/in-depth-introduction-to-machine-learning-in-15-hours-of-expert-videos/](http://www.r-bloggers.com/in-depth-introduction-to-machine-learning-in-15-hours-of-expert-videos/)
// MAGIC   * free PDF of the ISLR book: [http://www-bcf.usc.edu/~gareth/ISL/](http://www-bcf.usc.edu/~gareth/ISL/)
// MAGIC * A more theoretically sound book with interesting aplications is *Elements of Statistical Learning* by the Stanford Gang of 3 (Hastie, Tibshirani and Friedman):
// MAGIC   * free PDF of the 10th printing: [http://statweb.stanford.edu/~tibs/ElemStatLearn/printings/ESLII_print10.pdf](http://statweb.stanford.edu/~tibs/ElemStatLearn/printings/ESLII_print10.pdf)
// MAGIC   * Solutions: [http://waxworksmath.com/Authors/G_M/Hastie/WriteUp/weatherwax_epstein_hastie_solutions_manual.pdf](http://waxworksmath.com/Authors/G_M/Hastie/WriteUp/weatherwax_epstein_hastie_solutions_manual.pdf)
// MAGIC * 2015, UCLA Ass. Professor Ameet Talwalkar (from Spark team) in BerkeleyX: CS190.1x Scalable Machine Learning in edX Archive from 2015
// MAGIC * My preferred book for statistical learning is by [Kevin P. Murphy, Machine Learning: A Probabilistic Perspective](https://www.cs.ubc.ca/~murphyk/MLbook/). 
// MAGIC 
// MAGIC Deep Learning is a very popular method currently (2017) in Machine Learning and I recommend Andrew Ng's free course in Courseera for this.

// COMMAND ----------

// MAGIC %md
// MAGIC **Note:** This is an applied course in data science and we will quickly move to doing things with data.  You have to do work to get a deeper mathematical understanding or take other courses.  We will focus on intution here and the distributed ML Pipeline in action.
// MAGIC 
// MAGIC *I am assuming several of you have or are taking ML courses from experts in IT Department at Uppsala (if not, you might want to consider this seriously). In this course we will focus on getting our hads dirty quickly with some level of common understanding across all the disciplines represented here. Please dive deep at your own time to desired depths and tangents based on your background.*
// MAGIC 
// MAGIC I may consider teaching a short theoretical course in statistical learning if there is enough interest for those with background in:
// MAGIC 
// MAGIC * Real Analysis, 
// MAGIC * Geometry, 
// MAGIC * Combinatorics and 
// MAGIC * Probability, 
// MAGIC in the future.
// MAGIC 
// MAGIC Such a course could be an expanded version of the following notes built on the classic works of [Luc Devroye](http://luc.devroye.org/) and the L1-School of Statistical Learning:
// MAGIC 
// MAGIC   * Short course on Non-parametric Density Estimation at Centre for Mathematics and its Applications, Ecole Polytechnique, Palaiseau, France 
// MAGIC      * [http://lamastex.org/courses/Short_Courses/EcolePolytechnique_MDE_2013.pdf](http://lamastex.org/courses/Short_Courses/EcolePolytechnique_MDE_2013.pdf)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Machine Learning Introduction 
// MAGIC 
// MAGIC #### ML Intro high-level by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch now 4:14)**:
// MAGIC 
// MAGIC Ameet's course is in databricks guide for your convenience:
// MAGIC 
// MAGIC * [https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html](https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html)
// MAGIC 
// MAGIC [![ML Intro high-level by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/NJZht7aV2NQ/0.jpg)](https://www.youtube.com/watch?v=NJZht7aV2NQ?rel=0&autoplay=1&modestbranding=1&start=1)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Ameet's Summary of Machine Learning at a High Level
// MAGIC 
// MAGIC * rough definition of machine learning.
// MAGIC     * constructing and studying methods that learn from and make predictions on data.
// MAGIC 
// MAGIC * This broad area involves tools and ideas from various domains, including:
// MAGIC   *  computer science, 
// MAGIC   * probability and statistics, 
// MAGIC   * optimization, and 
// MAGIC   * linear algebra.
// MAGIC 
// MAGIC  * Common examples of ML, include:
// MAGIC     * face recognition, 
// MAGIC     * link prediction, 
// MAGIC     * text or document classification, eg.::
// MAGIC       * spam detection, 
// MAGIC       * protein structure prediction
// MAGIC       * teaching computers to play games (go!)
// MAGIC     
// MAGIC #### Some common terminology 
// MAGIC ##### using example of spam detection as a running example.
// MAGIC     
// MAGIC * the data points we learn from are call observations:
// MAGIC     * they are items or entities used for::
// MAGIC       * learning or 
// MAGIC       * evaluation.
// MAGIC 
// MAGIC * in the context of spam detection,
// MAGIC     * emails are our observations.
// MAGIC     * Features are attributes used to represent an observation.
// MAGIC     * Features are typically numeric, 
// MAGIC       *  and in spam detection, they can be:
// MAGIC         * the length,
// MAGIC         * the date, or 
// MAGIC         * the presence or absence of keywords in emails.
// MAGIC     * Labels are values or categories assigned to observations.
// MAGIC       *  and in spam detection, they can be:
// MAGIC         * an email being defined as spam or not spam.
// MAGIC 
// MAGIC * Training and test data sets are the observations that we use to train and evaluate a learning algorithm.
// MAGIC     ...
// MAGIC     
// MAGIC * **Pop-Quiz**
// MAGIC   * What is the difference between supervised and unsupervised learning?

// COMMAND ----------

// MAGIC %md
// MAGIC ##### For a Stats@Stanford Hastie-Tibshirani Perspective on Supervised and Unsupervised Learning:
// MAGIC 
// MAGIC **(watch later 12:12)**:
// MAGIC 
// MAGIC [![Supervised and Unsupervised Learning (12:12)](http://img.youtube.com/vi/LvaTokhYnDw/0.jpg)](https://www.youtube.com/watch?v=LvaTokhYnDw?rel=0&autoplay=1&modestbranding=1&start=1)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch now 2:07)**:
// MAGIC 
// MAGIC This course is in databricks guide for your convenience:
// MAGIC 
// MAGIC * [https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html](https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html)
// MAGIC 
// MAGIC [![Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/XPxOL1--GXo/0.jpg)](https://www.youtube.com/watch?v=XPxOL1--GXo?rel=0&autoplay=1&modestbranding=1&start=1)

// COMMAND ----------

// MAGIC %md
// MAGIC Take your own notes if you want ....

// COMMAND ----------

// MAGIC %md
// MAGIC #### Sample Classification Pipeline (Spam Filter) by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC 
// MAGIC This course is in databricks guide for your convenience:
// MAGIC 
// MAGIC * [https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html](https://docs.databricks.com/spark/1.6/training/scalable-machine-learning-cs190x-2015/index.html)
// MAGIC 
// MAGIC **(watch later 7:48)**:
// MAGIC 
// MAGIC [![Typical Supervised Learning Pipeline by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/Q7hxwkG9hNE/0.jpg)](https://www.youtube.com/watch?v=Q7hxwkG9hNE?rel=0&autoplay=1&modestbranding=1&start=1)