// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

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

val df = spark.range(1000).toDF("Id") // just make a DF of 1000 row indices

// COMMAND ----------

df.show(5)

// COMMAND ----------

val dfRand = df.select($"Id", rand(seed=1234567) as "rand") // add a column of random numbers in (0,1)

// COMMAND ----------

dfRand.show(5) // these are first 5 of the 1000 samples from the Uniform(0,1) RV

// COMMAND ----------

val dfRand = df.select($"Id", rand(seed=1234567) as "rand") // add a column of random numbers in (0,1)
dfRand.show(5) // these are first 5 of the 1000 samples from the Uniform(0,1) RV

// COMMAND ----------

val dfRand = df.select($"Id", rand(seed=879664) as "rand") // add a column of random numbers in (0,1)
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

val expoSamplesDF = spark.range(1000000000).toDF("Id") // just make a DF of 100 row indices
               .select($"Id", rand(seed=1234567) as "rand") // add a column of random numbers in (0,1)
               .withColumn("one",lit(1.0))
               .withColumn("rate",lit(0.5))
               .withColumn("expo_sample", -($"one" / $"rate") * log($"one" - $"rand"))

// COMMAND ----------

expoSamplesDF.describe().show()

// COMMAND ----------

// MAGIC %md
// MAGIC ## Approximating Pi with Monte Carlo simulaitons
// MAGIC 
// MAGIC Uisng RDDs directly, let's estimate Pi.

// COMMAND ----------

//Calculate pi with Monte Carlo estimation
import scala.math.random

//make a very large unique set of 1 -> n 
val partitions = 2 
val n = math.min(100000L * partitions, Int.MaxValue).toInt 
val xs = 1 until n 

//split up n into the number of partitions we can use 
val rdd = sc.parallelize(xs, partitions).setName("'N values rdd'")

//generate a random set of points within a 2x2 square
val sample = rdd.map { i =>
  val x = random * 2 - 1
  val y = random * 2 - 1
  (x, y)
}.setName("'Random points rdd'")

//points w/in the square also w/in the center circle of r=1
val inside = sample.filter { case (x, y) => (x * x + y * y < 1) }.setName("'Random points inside circle'")
val count = inside.count()
 
//Area(circle)/Area(square) = inside/n => pi=4*inside/n                        
println("Pi is roughly " + 4.0 * count / n)

// COMMAND ----------

// MAGIC %md
// MAGIC Doing it in PySpark is just as easy. This may be needed if there are pyhton libraries you want to take advantage of in Spark.

// COMMAND ----------

// MAGIC %py
// MAGIC # # Estimating $\pi$
// MAGIC #
// MAGIC # This PySpark example shows you how to estimate $\pi$ in parallel
// MAGIC # using Monte Carlo integration.
// MAGIC 
// MAGIC from __future__ import print_function
// MAGIC import sys
// MAGIC from random import random
// MAGIC from operator import add
// MAGIC 
// MAGIC partitions = 2
// MAGIC n = 100000 * partitions
// MAGIC 
// MAGIC def f(_):
// MAGIC     x = random() * 2 - 1
// MAGIC     y = random() * 2 - 1
// MAGIC     return 1 if x ** 2 + y ** 2 < 1 else 0
// MAGIC 
// MAGIC # To access the associated SparkContext
// MAGIC count = spark.sparkContext.parallelize(range(1, n + 1), partitions).map(f).reduce(add)
// MAGIC print("Pi is roughly %f" % (4.0 * count / n))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC The following is from this [google turotial](https://cloud.google.com/solutions/monte-carlo-methods-with-hadoop-spark#programming_a_monte_carlo_simulation_in_scala).
// MAGIC 
// MAGIC # Programming a Monte Carlo simulation in Scala
// MAGIC 
// MAGIC Monte Carlo, of course, is famous as a gambling destination. In this section, you use Scala to create a simulation that models the mathematical advantage that a casino enjoys in a game of chance. The "house edge" at a real casino varies widely from game to game; it can be over 20% in keno, for example. This tutorial creates a simple game where the house has only a one-percent advantage. Here's how the game works:
// MAGIC 
// MAGIC 1. The player places a bet, consisting of a number of chips from a bankroll fund.
// MAGIC - The player rolls a 100-sided die (how cool would that be?).
// MAGIC - If the result of the roll is a number from 1 to 49, the player wins.
// MAGIC - For results 50 to 100, the player loses the bet.
// MAGIC 
// MAGIC You can see that this game creates a one-percent disadvantage for the player: in 51 of the 100 possible outcomes for each roll, the player loses.
// MAGIC 
// MAGIC Follow these steps to create and run the game:

// COMMAND ----------

val STARTING_FUND = 10
val STAKE = 1   // the amount of the bet
val NUMBER_OF_GAMES = 25

def rollDie: Int = {
    val r = scala.util.Random
    r.nextInt(99) + 1
}

def playGame(stake: Int): (Int) = {
    val faceValue = rollDie
    if (faceValue < 50)
        (2*stake)
    else
        (0)
}

// Function to play the game multiple times
// Returns the final fund amount
def playSession(
   startingFund: Int = STARTING_FUND,
   stake: Int = STAKE,
   numberOfGames: Int = NUMBER_OF_GAMES):
   (Int) = {

    // Initialize values
    var (currentFund, currentStake, currentGame) = (startingFund, 0, 1)

    // Keep playing until number of games is reached or funds run out
    while (currentGame <= numberOfGames && currentFund > 0) {

        // Set the current bet and deduct it from the fund
        currentStake = math.min(stake, currentFund)
        currentFund -= currentStake

        // Play the game
        val (winnings) = playGame(currentStake)

        // Add any winnings
        currentFund += winnings

        // Increment the loop counter
        currentGame += 1
    }
    (currentFund)
}


// COMMAND ----------

// MAGIC %md
// MAGIC Enter the following code to play the game 25 times, which is the default value for `NUMBER_OF_GAMES`.

// COMMAND ----------

playSession()

// COMMAND ----------

// MAGIC %md
// MAGIC Your bankroll started with a value of 10 units. Is it higher or lower, now?

// COMMAND ----------

// MAGIC %md
// MAGIC Now simulate 10,000 players betting 100 chips per game. Play 10,000 games in a session. This Monte Carlo simulation calculates the probability of losing all your money before the end of the session. Enter the follow code:

// COMMAND ----------

(sc.parallelize(1 to 10000, 500)
  .map(i => playSession(100000, 100, 250000))
  .map(i => if (i == 0) 1 else 0)
  .reduce(_+_)/10000.0)


// COMMAND ----------

// MAGIC %md
// MAGIC Note that the syntax `.reduce(_+_)` is shorthand in Scala for aggregating by using a summing function. 
// MAGIC 
// MAGIC The preceding code performs the following steps:
// MAGIC 
// MAGIC 1. Creates an RDD with the results of playing the session.
// MAGIC - Replaces bankrupt players' results with the number 1 and nonzero results with the number 0.
// MAGIC - Sums the count of bankrupt players.
// MAGIC - Divides the count by the number of players.
// MAGIC 
// MAGIC A typical result might be:
// MAGIC 
// MAGIC ```%scala
// MAGIC res32: Double = 0.9992
// MAGIC ```
// MAGIC 
// MAGIC Which represents a near guarantee of losing all your money, even though the casino had only a one-percent advantage.

// COMMAND ----------

// MAGIC %md
// MAGIC # Project Ideas
// MAGIC 
// MAGIC Try to create a scalable simulation of interest to you.
// MAGIC 
// MAGIC Here are some mature projects:
// MAGIC 
// MAGIC - https://github.com/zishanfu/GeoSparkSim
// MAGIC - https://github.com/srbaird/mc-var-spark
// MAGIC 
// MAGIC See a more complete VaR modeling:
// MAGIC - https://databricks.com/blog/2020/05/27/modernizing-risk-management-part-1-streaming-data-ingestion-rapid-model-development-and-monte-carlo-simulations-at-scale.html