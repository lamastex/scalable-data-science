// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/TgDwIhUduLs/0.jpg)](https://www.youtube.com/embed/TgDwIhUduLs?start=0&end=1817&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Introduction to Simulation

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Core ideas in Monte Carlo simulation
// MAGIC 
// MAGIC * modular arithmetic gives pseudo-random streams that are indistiguishable from 'true' Uniformly distributed samples in integers from \\(\\{0,1,2,...,m\\}\\)
// MAGIC * by diving the integer streams from above by \\(m\\) we get samples from \\(\\{0/m,1/m,...,(m-1)/m\\}\\) and "pretend" this to be samples from the Uniform(0,1) RV
// MAGIC * we can use inverse distribution function of von Neumann's rejection sampler to convert samples from Uniform(0,1) RV to the following:
// MAGIC   * any other random variable
// MAGIC   * vector of random variables that could be dependent
// MAGIC   * or more generally other random structures:
// MAGIC     * random graphs and networks
// MAGIC     * random walks or (sensible perturbations of live traffic data on open street maps for hypothesis tests)
// MAGIC     * models of interacting paticle systems in ecology / chemcal physics, etc...
// MAGIC   

// COMMAND ----------

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
displayHTML(frameIt("https://en.wikipedia.org/wiki/Uniform_distribution_(continuous)",500))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC * [https://en.wikipedia.org/wiki/Inverse_transform_sampling](https://en.wikipedia.org/wiki/Inverse_transform_sampling)
// MAGIC * [https://en.wikipedia.org/wiki/Rejection_sampling](https://en.wikipedia.org/wiki/Rejection_sampling) - will revisit below for Expoential RV

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### breeze.stats.distributions
// MAGIC 
// MAGIC Breeze also provides a fairly large number of probability distributions. These come with access to probability density function for either discrete or continuous distributions. Many distributions also have methods for giving the mean and the variance.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Poisson_distribution",500))

// COMMAND ----------

import breeze.stats.distributions._

val poi = new Poisson(3.0);

// COMMAND ----------

val s = poi.sample(5); // let's draw five samples - black-box

// COMMAND ----------

// MAGIC %md
// MAGIC Getting probabilities of the Poisson samples

// COMMAND ----------

s.map( x => poi.probabilityOf(x) ) // PMF

// COMMAND ----------

val doublePoi = for(x <- poi) yield x.toDouble // meanAndVariance requires doubles, but Poisson samples over Ints

// COMMAND ----------

breeze.stats.meanAndVariance(doublePoi.samples.take(1000));

// COMMAND ----------

(poi.mean, poi.variance) // population mean and variance

// COMMAND ----------

// MAGIC %md
// MAGIC ## Exponential random Variable
// MAGIC 
// MAGIC Let's focus on getting our hands direty with a common random variable.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Exponential_distribution",500))

// COMMAND ----------

// MAGIC %md
// MAGIC NOTE: Below, there is a possibility of confusion for the term `rate` in the family of exponential distributions. Breeze parameterizes the distribution with the mean, but refers to it as the rate.

// COMMAND ----------

val expo = new Exponential(0.5);

// COMMAND ----------

expo.rate // what is the rate parameter

// COMMAND ----------

// MAGIC %md
// MAGIC A characteristic of exponential distributions is its half-life, but we can compute the probability a value falls between any two numbers.

// COMMAND ----------

expo.probability(0, math.log(2) * expo.rate)

// COMMAND ----------

expo.probability(math.log(2) * expo.rate, 10000.0)

// COMMAND ----------

expo.probability(0.0, 1.5)

// COMMAND ----------

// MAGIC %md
// MAGIC The above result means that approximately 95% of the draws from an exponential distribution fall between 0 and thrice the mean. We could have easily computed this with the cumulative distribution as well.

// COMMAND ----------

1 - math.exp(-3.0) // the CDF of the Exponential RV with rate parameter 3

// COMMAND ----------

// MAGIC %md
// MAGIC ### Drawing samples from Exponential RV

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Inverse_transform_sampling#Examples",500))

// COMMAND ----------

val samples = expo.sample(2).sorted; // built-in black box - we will roll our own shortly in Spark

// COMMAND ----------

expo.probability(samples(0), samples(1));

// COMMAND ----------

breeze.stats.meanAndVariance(expo.samples.take(10000)); // mean and variance of the sample

// COMMAND ----------

(1 / expo.rate, 1 / (expo.rate * expo.rate)) // mean and variance of the population

// COMMAND ----------

// MAGIC %md
// MAGIC ## Pseudo Random Numbers in Spark

// COMMAND ----------

import spark.implicits._
import org.apache.spark.sql.functions._

// COMMAND ----------

val df = spark.range(1000).toDF("Id") // just make a DF of 100 row indices

// COMMAND ----------

df.show(5)

// COMMAND ----------

val dfRand = df.select($"Id", rand(seed=1234567) as "rand") // add a column of random numbers in (0,1)

// COMMAND ----------

dfRand.show(5) // these are first 5 of the 1000 samples from the Uniform(0,1) RV

// COMMAND ----------

// MAGIC %md
// MAGIC Let's use the inverse CDF of the Exponential RV to transform these samples from the Uniform(0,1) RV into those from the Exponential RV.

// COMMAND ----------

val dfRand = df.select($"Id", rand(seed=1234567) as "rand") // add a column of random numbers in (0,1)
               .withColumn("one",lit(1.0))
               .withColumn("rate",lit(0.5))

// COMMAND ----------

dfRand.show(5) 

// COMMAND ----------

val dfExpRand = dfRand.withColumn("expo_sample", -($"one" / $"rate") * log($"one" - $"rand")) // samples from expo(rate=0.5)

// COMMAND ----------

dfExpRand.show(5)

// COMMAND ----------

display(dfExpRand)

// COMMAND ----------

dfExpRand.describe().show() // look sensible

// COMMAND ----------

