// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/1NICbbECaC0/0.jpg)](https://www.youtube.com/embed/1NICbbECaC0?start=872&end=2285&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Let us visit an interactive visual cognitive tool for the basics ideas in linear regression:
// MAGIC 
// MAGIC * [http://setosa.io/ev/ordinary-least-squares-regression/](http://setosa.io/ev/ordinary-least-squares-regression/)
// MAGIC 
// MAGIC The following video is a very concise and thorough treatment of linear regression for those who have taken the 200-level linear algebra. Others can fully understand it with some effort and revisiting. 

// COMMAND ----------

// MAGIC %md
// MAGIC #### Linear Regression by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch now 11:13)**:
// MAGIC 
// MAGIC [![Linear Regression by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/0wcMCQ8SyZM/0.jpg)](https://www.youtube.com/watch?v=0wcMCQ8SyZM)

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
// MAGIC [![Linear Regression by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/iS2QxI57OJs/0.jpg)](https://www.youtube.com/watch?v=iS2QxI57OJs)
// MAGIC 
// MAGIC 
// MAGIC Covers the training, test and validation and grid search... ridger regression...

// COMMAND ----------

// MAGIC %md
// MAGIC Take your own notes if you like.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Gradient Descent by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch later 11:19)**:
// MAGIC 
// MAGIC [![Gradient Descent by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/9AZYy36qLqU/0.jpg)](https://www.youtube.com/watch?v=9AZYy36qLqU)

// COMMAND ----------

// MAGIC %md
// MAGIC Please take notes if you want to.
// MAGIC 
// MAGIC **Now stop the stream and restart it to circumvent BerkeleyX CC claims**

// COMMAND ----------

// MAGIC %md
// MAGIC ### Getting our hands dirty with Darren Wilkinson's blog on regression
// MAGIC 
// MAGIC Let's follow the exposition in:
// MAGIC 
// MAGIC * [https://darrenjw.wordpress.com/2017/02/08/a-quick-introduction-to-apache-spark-for-statisticians/](https://darrenjw.wordpress.com/2017/02/08/a-quick-introduction-to-apache-spark-for-statisticians/)
// MAGIC * [https://darrenjw.wordpress.com/2017/06/21/scala-glm-regression-modelling-in-scala/](https://darrenjw.wordpress.com/2017/06/21/scala-glm-regression-modelling-in-scala/)
// MAGIC 
// MAGIC You need to scroll down the fitst link embedded in iframe below to get to the section on **Analysis of quantitative data** with **Descriptive statistics** and **Linear regression** sub-sections (you can skip the earlier part that shows you how to run everything locally in `spark-shell` - try this on your own later).

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
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
displayHTML(frameIt("https://darrenjw.wordpress.com/2017/02/08/a-quick-introduction-to-apache-spark-for-statisticians/",500))

// COMMAND ----------

// MAGIC %md
// MAGIC Let's do this live now...

// COMMAND ----------

import breeze.stats.distributions._
def x = Gaussian(1.0,2.0).sample(10000)
val xRdd = sc.parallelize(x)

// COMMAND ----------

println(xRdd.mean)
println(xRdd.sampleVariance)

// COMMAND ----------

val xStats = xRdd.stats
xStats.mean
xStats.sampleVariance
xStats.sum

// COMMAND ----------

val x2 = Gaussian(0.0,1.0).sample(10000) // 10,000 Gaussian samples with mean 0.0 and std dev 1.0
val xx = x zip x2 // creating tuples { (x,x2)_1, ... , (x,x2)_10000}
val lp = xx map {p => 2.0*p._1 + 1.0*p._2 + 1.5} // { lp_i := (2.0*x + 1.0 * x2 + 1.5)_i, i=1,...,10000 }
val eps = Gaussian(0.0,1.0).sample(10000) // standrad normal errors
val y = (lp zip eps) map (p => p._1 + p._2)
val yx = (y zip xx) map (p => (p._1, p._2._1, p._2._2))

// COMMAND ----------

val rddLR = sc.parallelize(yx)
rddLR.take(5)

// COMMAND ----------

val dfLR = rddLR.toDF("y","x1","x2")
dfLR.show
dfLR.show(5)

// COMMAND ----------

// importing for regression
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.linalg._

val lm = new LinearRegression
lm.explainParams
lm.getStandardization
lm.setStandardization(false)
lm.getStandardization
lm.explainParams

// COMMAND ----------

// Transform data frame to required format
val dflr = (dfLR map {row => (row.getDouble(0), 
           Vectors.dense(row.getDouble(1),row.getDouble(2)))}).
           toDF("label","features")
dflr.show(5)

// COMMAND ----------

// Fit model
val fit = lm.fit(dflr)
fit.intercept

// COMMAND ----------

fit.coefficients

// COMMAND ----------

val summ = fit.summary

// COMMAND ----------

summ.r2

// COMMAND ----------

summ.rootMeanSquaredError
summ.coefficientStandardErrors
summ.pValues
summ.tValues
summ.predictions
summ.residuals

// COMMAND ----------

// MAGIC %md
// MAGIC This gives you more on doing generalised linear modelling in Scala. But let's go back to out pipeline using the power-plant data.

// COMMAND ----------

displayHTML(frameIt("https://darrenjw.wordpress.com/2017/06/21/scala-glm-regression-modelling-in-scala/",500))

// COMMAND ----------

