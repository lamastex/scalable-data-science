// Databricks notebook source exported at Tue, 28 Jun 2016 09:30:18 UTC
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
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week9/17_SparklingWater/031_H2O_sparkling_water.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week9/17_SparklingWater/031_H2O_sparkling_water](http://img.youtube.com/vi/iDyeK3GvFpo/0.jpg)](https://www.youtube.com/v/iDyeK3GvFpo?rel=0&autoplay=1&modestbranding=1&start=3093&end=3669)

// COMMAND ----------

// MAGIC %md
// MAGIC # Deep learning with H2O.ai and Spark
// MAGIC * This notebook provides an introduction to the use of Deep Learning algorithms with H2O.ai and Spark
// MAGIC * It introduces H2O.ai a distributed machine learning framework
// MAGIC * It shows an example deep learning application written in H2O.ai (Sparkling water) and Spark

// COMMAND ----------

// MAGIC %md
// MAGIC #### H2O features - overview
// MAGIC [http://www.h2o.ai/product/downloads/recommended-systems.pdf](http://www.h2o.ai/product/downloads/recommended-systems.pdf)
// MAGIC ***
// MAGIC 
// MAGIC * H2O Core
// MAGIC * H2O Flow
// MAGIC * Algorithms
// MAGIC * Sparkling Water
// MAGIC * H2O Frame

// COMMAND ----------

// MAGIC %md
// MAGIC ###H2O.ai - Architecture  
// MAGIC [https://github.com/h2oai/h2o-3/blob/master/h2o-docs/src/product/architecture/Architecture.md](https://github.com/h2oai/h2o-3/blob/master/h2o-docs/src/product/architecture/Architecture.md)
// MAGIC 
// MAGIC ![H2O.ai - Architecture](https://raw.githubusercontent.com/h2oai/h2o-3/master/h2o-docs/src/product/architecture/images/h2o_stack.png)
// MAGIC 
// MAGIC -- Image Credit: Sparkling water

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### H2O Flow
// MAGIC 
// MAGIC **Watch later (2:28 seconds)**:
// MAGIC 
// MAGIC [![H2O Flow](http://img.youtube.com/vi/wzeuFfbW7WE/0.jpg)](https://www.youtube.com/v/wzeuFfbW7WE?rel=0&autoplay=1&modestbranding=1)

// COMMAND ----------

// MAGIC %md
// MAGIC ###Algorithms  
// MAGIC 
// MAGIC ####Supervised Learning
// MAGIC * Generalized Linear Modeling (GLM): [Tutorial](http://learn.h2o.ai/content/tutorials/glm/glm.html?_ga=1.123084171.1438470624.1462131663) | [Reference](http://h2o-release.s3.amazonaws.com/h2o/latest_stable_GLM_booklet.html)
// MAGIC * Gradient Boosting Machine (GBM):  [Tutorial](http://learn.h2o.ai/content/tutorials/gbm-randomforest/index.html?_ga=1.60642349.1438470624.1462131663) | [Reference](http://h2o-release.s3.amazonaws.com/h2o/latest_stable_GBM_booklet.html)
// MAGIC * Deep Learning: [Tutorial](http://learn.h2o.ai/content/tutorials/deeplearning/index.html?_ga=1.127736589.1438470624.1462131663) | [Reference](http://h2o-release.s3.amazonaws.com/h2o/latest_stable_DeepLearning_booklet.html)
// MAGIC * Ensembles (Stacking): [Tutorial](http://learn.h2o.ai/content/tutorials/ensembles-stacking/index.html?_ga=1.127736589.1438470624.1462131663) |  [Reference](https://github.com/h2oai/h2o-3/blob/master/h2o-r/ensemble/README.md)
// MAGIC * Distributed Random Forest:  [Tutorial](https://github.com/h2oai/h2o-3/blob/master/h2o-docs/src/product/tutorials/rf/rf.md)
// MAGIC * Naive Bayes: [Reference](http://h2o-release.s3.amazonaws.com/h2o/rel-turchin/3/docs-website/h2o-docs/index.html#Data%20Science%20Algorithms-Na%C3%AFve%20Bayes)
// MAGIC ***
// MAGIC 
// MAGIC ####Unsupervised Learning
// MAGIC * Generalized Low Ranked Modeling (GLRM):  [Tutorial](http://learn.h2o.ai/content/tutorials/glrm/glrm-tutorial.html?_ga=1.127908877.1438470624.1462131663) |  [Reference](http://arxiv.org/abs/1410.0342)
// MAGIC * K-Means:  [Tutorial](https://github.com/h2oai/h2o-3/blob/master/h2o-docs/src/product/tutorials/kmeans/kmeans.md)
// MAGIC * PCA:  [Tutorial](https://github.com/h2oai/h2o-3/blob/master/h2o-docs/src/product/tutorials/pca/pca.md)
// MAGIC * Anomaly Detection via Deep Learning: [Tutorial](https://github.com/h2oai/h2o-training-book/blob/master/hands-on_training/anomaly_detection.md)

// COMMAND ----------

// MAGIC %md
// MAGIC ###H2O.ai - Sparkling water  
// MAGIC [https://github.com/h2oai/sparkling-water/blob/master/DEVEL.md](https://github.com/h2oai/sparkling-water/blob/master/DEVEL.md)
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC ![H2O.ai - Sparkling water](https://raw.githubusercontent.com/h2oai/sparkling-water/master/design-doc/images/Sparkling%20Water%20cluster.png)
// MAGIC 
// MAGIC -- Image Credit: Sparkling water

// COMMAND ----------

// MAGIC %md
// MAGIC #####Data sharing between RDD, DataFrame and H2OFrame
// MAGIC 
// MAGIC ![H2O.ai - Sparkling water](https://raw.githubusercontent.com/h2oai/sparkling-water/master/design-doc/images/DataShare.png)
// MAGIC 
// MAGIC -- Image Credit: Sparkling water

// COMMAND ----------

// MAGIC %md
// MAGIC ### APIs
// MAGIC * [Core API](http://h2o-release.s3.amazonaws.com/h2o/rel-turchin/3/docs-website/h2o-core/javadoc/index.html)  
// MAGIC * [Algorithms](http://h2o-release.s3.amazonaws.com/h2o/rel-turchin/3/docs-website/h2o-algos/javadoc/index.html)
// MAGIC * [Models](http://h2o-release.s3.amazonaws.com/h2o/rel-turchin/3/docs-website/h2o-genmodel/javadoc/index.html)    
// MAGIC ***
// MAGIC   
// MAGIC [Deep learning booklet](http://h2o-release.s3.amazonaws.com/h2o/rel-turchin/3/docs-website/h2o-docs/booklets/DeepLearning_Vignette.pdf)
// MAGIC   
// MAGIC   

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