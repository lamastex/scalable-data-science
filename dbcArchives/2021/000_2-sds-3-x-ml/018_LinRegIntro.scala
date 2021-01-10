// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

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
// MAGIC Please take notes if you want to. Most of the above should be review to most of you.

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


// COMMAND ----------

summ.coefficientStandardErrors

// COMMAND ----------

summ.pValues

// COMMAND ----------

summ.tValues

// COMMAND ----------

summ.predictions.show(5)

// COMMAND ----------

summ.residuals.show(5)

// COMMAND ----------

// MAGIC %md
// MAGIC This gives you more on doing generalised linear modelling in Scala. But let's go back to out pipeline using the power-plant data.

// COMMAND ----------

displayHTML(frameIt("https://darrenjw.wordpress.com/2017/06/21/scala-glm-regression-modelling-in-scala/",500))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Exercise: Writing your own Spark program for the least squares fit 
// MAGIC 
// MAGIC #### Your task is to use the syntactic sugar below to write your own linear regression function using `reduce` and `broadcast` operations
// MAGIC 
// MAGIC How would you write your own Spark program to find the *least squares fit* on the following 1000 data points in RDD `rddLR`, where the variable `y` is the response variable, and `X1` and `X2`  are independent variables?
// MAGIC 
// MAGIC 
// MAGIC More precisely, find \\( w_1, w_2 \\), such that,
// MAGIC 
// MAGIC \\( \sum_{i=1}^{10} (w_1 X1_i + w_2 X2_i - y_i)^2 \\) is minimized.
// MAGIC 
// MAGIC Report \\( w_1 \\), \\(w_2\\), and the Root Mean Square Error and submit code in Spark. Analyze the resulting algorithm in terms of all-to-all, one-to-all, and all-to-one communication patterns.

// COMMAND ----------

rddLR.count

// COMMAND ----------

rddLR.take(10)

// COMMAND ----------

val dfLR = rddLR.toDF("y","x1","x2")
dfLR.show(10)

// COMMAND ----------

rddLR.getNumPartitions

// COMMAND ----------

rddLR.map( yx1x2 => (yx1x2._2, yx1x2._3, yx1x2._1) ).take(10)

// COMMAND ----------

import breeze.linalg.DenseVector
    val pts = rddLR.map( yx1x2 => DenseVector(yx1x2._2, yx1x2._3, yx1x2._1) ).cache

// COMMAND ----------

// MAGIC %md
// MAGIC Now all we need to do is one-to-all and all-to-one broadcast of the gradient...

// COMMAND ----------

val w = DenseVector(0.0, 0.0, 0.0)
val w_bc = sc.broadcast(w)
val step = 0.1
val max_iter = 100

/*
YouTry: Fix the expressions for grad_w0, grad_w1 and grad_w2 to have w_bc.value be the same as that from ml lib's fit.coefficients
*/    
for (i <- 1 to max_iter) {
        val grad_w0 = pts.map(x => 2*(w_bc.value(0)*1.0 + w_bc.value(1)*x(0) + w_bc.value(2)*x(1) - x(2))*1.0).reduce(_+_)
        val grad_w1 = pts.map(x => 2*(w_bc.value(0)*1.0 + w_bc.value(1)*x(0) + w_bc.value(2)*x(1) - x(2))*x(0)).reduce(_+_)
        val grad_w2 = pts.map(x => 2*(w_bc.value(0)*1.0 + w_bc.value(1)*x(0) + w_bc.value(2)*x(1) - x(2))*x(1)).reduce(_+_)
        w_bc.value(0) = w_bc.value(0) - step*grad_w0
        w_bc.value(1) = w_bc.value(1) - step*grad_w1
        w_bc.value(2) = w_bc.value(2) - step*grad_w2
}

// COMMAND ----------

w_bc.value

// COMMAND ----------

fit.intercept

// COMMAND ----------

fit.coefficients

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Computing each of the gradients requires an all-to-one communication (due to the `.reduce(_+_)`). There are two of these per iteration. Broadcasting the updated `w_bc` requires one to all communication. 
// MAGIC     