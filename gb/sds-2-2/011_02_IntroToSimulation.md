[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Introduction to Simulation
==========================

### breeze.stats.distributions

Breeze also provides a fairly large number of probability distributions. These come with access to probability density function for either discrete or continuous distributions. Many distributions also have methods for giving the mean and the variance.

``` scala
import breeze.stats.distributions._

val poi = new Poisson(3.0);
```

>     import breeze.stats.distributions._
>     poi: breeze.stats.distributions.Poisson = Poisson(3.0)

``` scala
val s = poi.sample(5); // let's draw five samples - black-box
```

>     s: IndexedSeq[Int] = Vector(1, 4, 8, 0, 6)

``` scala
s.map( x => poi.probabilityOf(x) ) // PMF
```

>     res36: IndexedSeq[Double] = Vector(0.14936120510359185, 0.16803135574154085, 0.008101511794681432, 0.049787068367863944, 0.05040940672246224)

Getting probabilities of the Poisson samples

``` scala
val doublePoi = for(x <- poi) yield x.toDouble // meanAndVariance requires doubles, but Poisson samples over Ints
```

>     doublePoi: breeze.stats.distributions.Rand[Double] = MappedRand(Poisson(3.0),<function1>)

``` scala
breeze.stats.meanAndVariance(doublePoi.samples.take(1000));
```

>     res40: breeze.stats.MeanAndVariance = MeanAndVariance(3.013999999999998,2.9787827827827824,1000)

``` scala
(poi.mean, poi.variance) // population mean and variance
```

>     res41: (Double, Double) = (3.0,3.0)

Exponential random Variable
---------------------------

Let's focus on getting our hands direty with a common random variable.

``` scala
val expo = new Exponential(0.5);
```

>     expo: breeze.stats.distributions.Exponential = Exponential(0.5)

``` scala
expo.rate // what is the rate parameter
```

>     res42: Double = 0.5

A characteristic of exponential distributions is its half-life, but we can compute the probability a value falls between any two numbers.

``` scala
expo.probability(0, math.log(2) * expo.rate)
```

>     res43: Double = 0.5

``` scala
expo.probability(0.0, 1.5)
```

>     res45: Double = 0.950212931632136

The above result means that approximately 95% of the draws from an exponential distribution fall between 0 and thrice the mean. We could have easily computed this with the cumulative distribution as well.

``` scala
1 - math.exp(-3.0) // the CDF of the Exponential RV with rate parameter 3
```

>     res47: Double = 0.950212931632136

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Exponential_distribution"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

NOTE: Below, there is a possibility of confusion for the term `rate` in the family of exponential distributions. Breeze parameterizes the distribution with the mean, but refers to it as the rate.

``` scala
val samples = expo.sample(2).sorted; // built-in black box - we will roll our own shortly in Spark
```

>     samples: IndexedSeq[Double] = Vector(0.5392432880681155, 2.0560411023149476)

``` scala
expo.probability(samples(0), samples(1));
```

>     res48: Double = 0.32373622100122956

``` scala
breeze.stats.meanAndVariance(expo.samples.take(10000)); // mean and variance of the sample
```

>     res49: breeze.stats.MeanAndVariance = MeanAndVariance(1.987723329390203,4.095923857112381,10000)

``` scala
(1 / expo.rate, 1 / (expo.rate * expo.rate)) // mean and variance of the population
```

>     res50: (Double, Double) = (2.0,4.0)

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Poisson_distribution"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

Core ideas in Monte Carlo simulation
------------------------------------

-   modular arithmetic gives pseudo-random streams that are indistiguishable from 'true' Uniformly distributed samples in integers from \\(\\{0,1,2,...,m\\}\\)
-   by diving the integer streams from above by \\(m\\) we get samples from \\(\\{0/m,1/m,...,(m-1)/m\\}\\) and "pretend" this to be samples from the Uniform(0,1) RV
-   we can use inverse distribution function of von Neumann's rejection sampler to convert samples from Uniform(0,1) RV to the following:
-   any other random variable
-   vector of random variables that could be dependent
-   or more generally other random structures:
    -   random graphs and networks
    -   random walks or (sensible perturbations of live traffic data on open street maps for hypothesis tests)
    -   models of interacting paticle systems in ecology / chemcal physics, etc...

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Uniform_distribution_(continuous)"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

-   <https://en.wikipedia.org/wiki/Inverse_transform_sampling>
-   <https://en.wikipedia.org/wiki/Rejection_sampling> - will revisit below for Expoential RV

### Drawing samples from Exponential RV

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Inverse_transform_sampling#Examples"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

Pseudo Random Numbers in Spark
------------------------------

``` scala
val dfRand = df.select($"Id", rand(seed=1234567) as "rand") // add a column of random numbers in (0,1)
```

>     dfRand: org.apache.spark.sql.DataFrame = [Id: bigint, rand: double]

``` scala
val df = spark.range(1000).toDF("Id") // just make a DF of 100 row indices
```

>     df: org.apache.spark.sql.DataFrame = [Id: bigint]

``` scala
import spark.implicits._
import org.apache.spark.sql.functions._
```

>     import spark.implicits._
>     import org.apache.spark.sql.functions._

``` scala
dfRand.show(5) 
```

>     +---+------------------+---+----+
>     | Id|              rand|one|rate|
>     +---+------------------+---+----+
>     |  0|0.2289181799234461|1.0| 0.5|
>     |  1|0.9756456161051732|1.0| 0.5|
>     |  2|0.7781702473664945|1.0| 0.5|
>     |  3|0.5585984240683788|1.0| 0.5|
>     |  4|0.8305446150005453|1.0| 0.5|
>     +---+------------------+---+----+
>     only showing top 5 rows

``` scala
val dfExpRand = dfRand.withColumn("expo_sample", -($"one" / $"rate") * log($"one" - $"rand")) // samples from expo(rate=0.5)
```

>     dfExpRand: org.apache.spark.sql.DataFrame = [Id: bigint, rand: double ... 3 more fields]

``` scala
dfExpRand.show(5)
```

>     +---+------------------+---+----+------------------+
>     | Id|              rand|one|rate|       expo_sample|
>     +---+------------------+---+----+------------------+
>     |  0|0.2289181799234461|1.0| 0.5| 0.519921578060948|
>     |  1|0.9756456161051732|1.0| 0.5| 7.430086817819349|
>     |  2|0.7781702473664945|1.0| 0.5|3.0116901426840474|
>     |  3|0.5585984240683788|1.0| 0.5|1.6356004297263063|
>     |  4|0.8305446150005453|1.0| 0.5|3.5503312043026414|
>     +---+------------------+---+----+------------------+
>     only showing top 5 rows

``` scala
val dfRand = df.select($"Id", rand(seed=1234567) as "rand") // add a column of random numbers in (0,1)
               .withColumn("one",lit(1.0))
               .withColumn("rate",lit(0.5))
```

>     dfRand: org.apache.spark.sql.DataFrame = [Id: bigint, rand: double ... 2 more fields]

Let's use the inverse CDF of the Exponential RV to transform these samples from the Uniform(0,1) RV into those from the Exponential RV.

``` scala
dfExpRand.describe().show() // look sensible
```

>     +-------+-----------------+--------------------+----+----+--------------------+
>     |summary|               Id|                rand| one|rate|         expo_sample|
>     +-------+-----------------+--------------------+----+----+--------------------+
>     |  count|             1000|                1000|1000|1000|                1000|
>     |   mean|            499.5| 0.49368134205225334| 1.0| 0.5|    1.98855225198386|
>     | stddev|288.8194360957494|  0.2925326105055967| 0.0| 0.0|   2.077189631386989|
>     |    min|                0|6.881987320686012E-4| 1.0| 0.5|0.001376871299039548|
>     |    max|              999|  0.9999092082356841| 1.0| 0.5|  18.613883955674265|
>     +-------+-----------------+--------------------+----+----+--------------------+

``` scala
expo.probability(math.log(2) * expo.rate, 10000.0)
```

>     res44: Double = 0.5

``` scala
df.show(5)
```

>     +---+
>     | Id|
>     +---+
>     |  0|
>     |  1|
>     |  2|
>     |  3|
>     |  4|
>     +---+
>     only showing top 5 rows

``` scala
dfRand.show(5) // these are first 5 of the 1000 samples from the Uniform(0,1) RV
```

>     +---+------------------+
>     | Id|              rand|
>     +---+------------------+
>     |  0|0.2289181799234461|
>     |  1|0.9756456161051732|
>     |  2|0.7781702473664945|
>     |  3|0.5585984240683788|
>     |  4|0.8305446150005453|
>     +---+------------------+
>     only showing top 5 rows

``` scala
display(dfExpRand)
```

| Id   | rand                  | one | rate | expo\_sample          |
|------|-----------------------|-----|------|-----------------------|
| 0.0  | 0.2289181799234461    | 1.0 | 0.5  | 0.519921578060948     |
| 1.0  | 0.9756456161051732    | 1.0 | 0.5  | 7.430086817819349     |
| 2.0  | 0.7781702473664945    | 1.0 | 0.5  | 3.0116901426840474    |
| 3.0  | 0.5585984240683788    | 1.0 | 0.5  | 1.6356004297263063    |
| 4.0  | 0.8305446150005453    | 1.0 | 0.5  | 3.5503312043026414    |
| 5.0  | 0.24509508429489502   | 1.0 | 0.5  | 0.5623269542551792    |
| 6.0  | 0.3261728210209841    | 1.0 | 0.5  | 0.7895632239182315    |
| 7.0  | 0.9048209688882228    | 1.0 | 0.5  | 4.7039912457536825    |
| 8.0  | 0.1734047711519603    | 1.0 | 0.5  | 0.38088029788287886   |
| 9.0  | 0.960550226313055     | 1.0 | 0.5  | 6.4654539368255834    |
| 10.0 | 0.8176919881907039    | 1.0 | 0.5  | 3.4041152995511794    |
| 11.0 | 0.9222866482773638    | 1.0 | 0.5  | 5.10945639905726      |
| 12.0 | 0.8439728956612385    | 1.0 | 0.5  | 3.7154510821267093    |
| 13.0 | 0.9976233807231735    | 1.0 | 0.5  | 12.084152546094039    |
| 14.0 | 0.5322121435087672    | 1.0 | 0.5  | 1.5194807678615738    |
| 15.0 | 0.9494337098572444    | 1.0 | 0.5  | 5.968940254825386     |
| 16.0 | 1.4876806392323583e-2 | 1.0 | 0.5  | 2.9977151956283435e-2 |
| 17.0 | 0.22846929665818771   | 1.0 | 0.5  | 0.5187576220548517    |
| 18.0 | 0.2645153481004022    | 1.0 | 0.5  | 0.6144512134640042    |
| 19.0 | 5.1002847933634965e-2 | 1.0 | 0.5  | 0.10469896272104277   |
| 20.0 | 0.854458723617456     | 1.0 | 0.5  | 3.8545910922480493    |
| 21.0 | 0.20279076904512927   | 1.0 | 0.5  | 0.4532762229793493    |
| 22.0 | 1.4440936997352383e-2 | 1.0 | 0.5  | 2.909244433664234e-2  |
| 23.0 | 0.5629216784468886    | 1.0 | 0.5  | 1.6552857488470558    |
| 24.0 | 0.900597803795409     | 1.0 | 0.5  | 4.617162141900199     |
| 25.0 | 8.100010994479523e-2  | 1.0 | 0.5  | 0.16893855252341536   |
| 26.0 | 0.6758501834752005    | 1.0 | 0.5  | 2.2530989467614595    |
| 27.0 | 0.9864261243185121    | 1.0 | 0.5  | 8.599216478637233     |
| 28.0 | 0.8629331422779548    | 1.0 | 0.5  | 3.974572919247493     |
| 29.0 | 0.1876358076401241    | 1.0 | 0.5  | 0.4156130532275476    |

Truncated to 30 rows

