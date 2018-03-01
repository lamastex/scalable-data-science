[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Sketching with T-digest for quantiles
=====================================

A Toy Anomaly Detector
----------------------

Fisher noticed the fundamental computational difference between mean, covariance, etc. and median, quantiles, in early 1900s.

The former ones are today called recursively computable statistics. When you take the memory footprint needed to keep these statistics updated then we get into the world of probabilistic datastructures...

The basic idea of sketching is formally conveyed in Chapter 6 of [Foundations of data Science](https://www.cs.cornell.edu/jeh/book.pdf).

Let's get a more informal view form the following sources.

-   Read now: https://medium.com/@muppal/probabilistic-data-structures-in-the-big-data-world-code-b9387cff0c55
-   See Ted Dunning's explanation of t-digest now: https://www.youtube.com/watch?v=B0dMc0t7K1g

Demonstration of t-digest to detect anomalous scores
====================================================

Let us import the following scala implementation of t-digest:

-   maven coordinates: `isarn-sketches-spark_2.11-0.3.0-sp2.2-py2.7`

See the library: <https://github.com/isarn/isarn-sketches-spark>

``` scala
import org.isarnproject.sketches._
import org.isarnproject.sketches.udaf._
import org.apache.spark.isarnproject.sketches.udt._
import org.isarnproject.sketches._
import org.isarnproject.sketches.udaf._
import org.apache.spark.isarnproject.sketches.udt._
```

>     import org.isarnproject.sketches._
>     import org.isarnproject.sketches.udaf._
>     import org.apache.spark.isarnproject.sketches.udt._
>     import org.isarnproject.sketches._
>     import org.isarnproject.sketches.udaf._
>     import org.apache.spark.isarnproject.sketches.udt._

``` scala
import scala.util.Random
import scala.util.Random._ 

// make a sample to produce a mixture of two normal RVs with standard deviation 1 but with different location or mean parameters
def myMixtureOf2Normals( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : Double = {
  val sample = if (r.nextDouble <= normalWeight) {r.nextGaussian+normalLocation } 
               else {r.nextGaussian + abnormalLocation} 
  return sample
   }
```

>     import scala.util.Random
>     import scala.util.Random._
>     myMixtureOf2Normals: (normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: scala.util.Random)Double

Here is a quick overview of the simple mixture of two Normal or Gaussian random variables we will be simulating from.

``` scala
val r = new Random(1L)
println(myMixtureOf2Normals(1.0, 10.0, 0.99, r), myMixtureOf2Normals(1.0, 10.0, 0.99, r))
// should always produce (0.5876430182311466,-0.34037937678788865) when seed = 1L
```

>     (0.5876430182311466,-0.34037937678788865)
>     r: scala.util.Random = scala.util.Random@2cf4bcc9

``` scala
val r = new Random(12345L)
val data = sc.parallelize(Vector.fill(10000){myMixtureOf2Normals(1.0, 10.0, 0.99, r)}).toDF.as[Double]
```

>     r: scala.util.Random = scala.util.Random@35499648
>     data: org.apache.spark.sql.Dataset[Double] = [value: double]

``` scala
data.show(5)
```

>     +--------------------+
>     |               value|
>     +--------------------+
>     |  0.2576188264990721|
>     |-0.13149698512045327|
>     |  1.4139063973267458|
>     |-0.02383387596851...|
>     |  0.7274784426774964|
>     +--------------------+
>     only showing top 5 rows

``` scala
display(data)
```

| value                  |
|------------------------|
| 0.2576188264990721     |
| -0.13149698512045327   |
| 1.4139063973267458     |
| -2.3833875968513496e-2 |
| 0.7274784426774964     |
| -1.0658630481235276    |
| 0.746959841932221      |
| 0.30477096247050206    |
| -6.407620682061621e-2  |
| 1.8464307210258604     |
| 2.0786529531264355     |
| 0.685838993990332      |
| 2.3056211153362485     |
| -0.7435548094085835    |
| -0.36946067155650786   |
| 1.1178132434092503     |
| 1.0672400098827672     |
| 2.403799182291664      |
| 2.7905949803662926     |
| 2.3901047303648846     |
| 2.2391322699010967     |
| 0.7102559487906945     |
| -0.1875570296359037    |
| 2.0036998039560725     |
| 2.028162246705019      |
| -1.1084782237141253    |
| 2.7320985336302965     |
| 1.7953021498619885     |
| 1.3332433299615185     |
| 1.2842120504662247     |

Truncated to 30 rows

Let's t-digest this `data` using a user-defined function `udaf` evaluated below.

``` scala
val udaf = tdigestUDAF[Double].delta(0.2)
                              //.maxDiscrete(25) // an additional optimisation with bins
```

>     udaf: org.isarnproject.sketches.udaf.TDigestUDAF[Double] = TDigestUDAF(0.2,0)

We can `agg` or aggregate the `data` DataFrame's `value` column of `Double`s that contain our data as follows.

``` scala
val agg = data.agg(udaf($"value"))
```

>     agg: org.apache.spark.sql.DataFrame = [tdigestudaf(value): tdigest]

Next, let's get the t-digest of the aggregation as `td`.

``` scala
val td = agg.first.getAs[TDigestSQL](0).tdigest // t-digest
```

>     td: org.isarnproject.sketches.TDigest = TDigest(0.2,0,137,TDigestMap(-2.795387521721169 -> (1.0, 1.0), -2.5827462010549587 -> (1.0, 2.0), -2.5483614528075127 -> (1.0, 3.0), -2.477169648218326 -> (1.0, 4.0), -2.3989148382735106 -> (1.0, 5.0), -2.3621428788859387 -> (1.0, 6.0), -2.3148374687684097 -> (0.6599375020366591, 6.659937502036659), -2.3125673391578063 -> (1.3301295134267586, 7.990067015463417), -2.2869139467179167 -> (1.7716040048548714, 9.761671020318289), -2.2636971919621813 -> (0.23832897968171096, 10.0), -2.0993694077900718 -> (0.9679652111755495, 10.96796521117555), -2.0374280877514073 -> (0.8036200489928786, 11.771585260168429), -2.008628128720943 -> (2.204295395214974, 13.975880655383403), -1.9799895567671855 -> (0.6717906689718269, 14.64767132435523), -1.9635404957133342 -> (2.8815532225111813, 17.52922454686641), -1.899364102428411 -> (3.5047976358149664, 21.034022182681376), -1.8935452617011608 -> (0.9659778173186226, 22.0), -1.7834073726649236 -> (4.0, 26.0), -1.6748447233354293 -> (4.368698519491373, 30.368698519491375), -1.64555306421358 -> (3.631301480508627, 34.0), -1.6091296440532281 -> (7.0, 41.0), -1.581533783623044 -> (4.0, 45.0), -1.5483981038434067 -> (6.560204096834605, 51.5602040968346), -1.5120146330181543 -> (7.62421745259823, 59.184421549432834), -1.4717418407937775 -> (8.815578450567166, 68.0), -1.4063760775866394 -> (12.800839656820326, 80.80083965682033), -1.3448698733594848 -> (16.023860049556692, 96.82469970637702), -1.2893622489786558 -> (9.858644542502482, 106.68334424887951), -1.2577068775157296 -> (16.550833463227963, 123.23417771210747), -1.2297589662542086 -> (5.463042043098952, 128.69721975520642), -1.2060997038692673 -> (20.302780244793585, 149.0), -1.1491161624092898 -> (25.76798602053494, 174.76798602053495), -1.1135641229645985 -> (12.423005143999912, 187.19099116453486), -1.0685344414176143 -> (26.783120524339587, 213.97411168887444), -1.0323848325430838 -> (16.63549107142857, 230.609602760303), -0.9866222212073446 -> (23.788016803291637, 254.39761956359465), -0.9497461655620497 -> (29.08674669226949, 283.48436625586413), -0.9118490862735472 -> (20.59159498877623, 304.0759612446404), -0.8694163862755389 -> (33.123564105489756, 337.19952535013016), -0.818736730934315 -> (52.1563476029216, 389.35587295305174), -0.7479408556355343 -> (61.925127078567286, 451.28100003161904), -0.6867731815523207 -> (48.07685810509796, 499.357858136717), -0.6281811941110975 -> (70.84176189108744, 570.1996200278045), -0.5734204802885597 -> (60.73927341849002, 630.9388934462945), -0.5253935153589147 -> (60.895384887279, 691.8342783335735), -0.4727181407508696 -> (84.65346692284842, 776.4877452564219), -0.4183225759962036 -> (57.14194932251596, 833.6296945789379), -0.3732605516445849 -> (71.21267638308262, 904.8423709620206), -0.32582691117268947 -> (92.32940794821484, 997.1717789102354), -0.27259472416178243 -> (91.70747316618615, 1088.8792520764216), -0.2230954128597663 -> (84.58384636648559, 1173.4630984429073), -0.15524161077336388 -> (156.72720326513735, 1330.1903017080447), -0.08845206643515205 -> (147.19847600015095, 1477.3887777081957), -0.02085650418093665 -> (176.55537984353873, 1653.9441575517344), 0.03893266279461198 -> (155.75159617985264, 1809.695753731587), 0.09938107165485015 -> (163.4762453741249, 1973.1719991057118), 0.1526216239516141 -> (170.59860607858832, 2143.7706051843), 0.21453126905557823 -> (176.55571400826986, 2320.32631919257), 0.2834394728937001 -> (213.57358132621525, 2533.899900518785), 0.36393016087166596 -> (279.12323268717813, 2813.0231332059634), 0.45679788946397837 -> (278.0902421901438, 3091.113375396107), 0.5330855831009225 -> (227.50480956676324, 3318.61818496287), 0.5968511484840918 -> (275.63904126007793, 3594.2572262229482), 0.6786380701725078 -> (326.55464408030315, 3920.8118703032515), 0.7625053055192722 -> (295.0568528562476, 4215.8687231594995), 0.8388772354285464 -> (258.41153144878285, 4474.280254608282), 0.9070689204546466 -> (326.76808910266044, 4801.048343710942), 1.0018427518639939 -> (422.5468200238942, 5223.595163734836), 1.1017961944715824 -> (409.87709458864884, 5633.472258323485), 1.206398134684977 -> (357.8525985596794, 5991.324856883165), 1.3011458883945075 -> (333.74799203938045, 6325.072848922546), 1.390097192526568 -> (359.4001599043703, 6684.473008826916), 1.4673753974641968 -> (194.71796878411135, 6879.190977611028), 1.530450897938683 -> (238.7420465487473, 7117.933024159775), 1.5912144507660058 -> (168.87739950717233, 7286.810423666948), 1.6545017805538234 -> (211.16935108645868, 7497.979774753407), 1.7155992131858704 -> (157.03121148726777, 7655.010986240674), 1.7781269703413782 -> (188.15654117816, 7843.167527418835), 1.848071347060917 -> (191.27664623192337, 8034.444173650758), 1.9254472410816295 -> (226.97702297155027, 8261.421196622308), 1.9969160162227046 -> (148.22756031130024, 8409.648756933608), 2.0714766462751193 -> (185.44546357626868, 8595.094220509876), 2.158990362568614 -> (151.4954386893494, 8746.589659199226), 2.243905017040895 -> (127.47207866240211, 8874.061737861628), 2.3175984415467035 -> (88.04157056026982, 8962.103308421898), 2.371803768181736 -> (107.28246020996545, 9069.385768631864), 2.444709595910291 -> (97.69039902579638, 9167.07616765766), 2.5211122778774384 -> (96.8891914206513, 9263.965359078311), 2.5983170298888094 -> (88.80246155925472, 9352.767820637566), 2.6555245160633634 -> (52.72532220594397, 9405.49314284351), 2.7096416373276084 -> (33.382789065635464, 9438.875931909146), 2.749125370761872 -> (61.62614292495636, 9500.502074834103), 2.8044848896450474 -> (49.39991763386941, 9549.901992467972), 2.875888748759476 -> (65.78546774441155, 9615.687460212384), 2.971742780087539 -> (51.68302203426054, 9667.370482246644), 3.0668184247986834 -> (45.939729735298556, 9713.310211981943), 3.1487058684289755 -> (17.546947507400326, 9730.857159489344), 3.218035693080918 -> (34.167423370384746, 9765.024582859729), 3.293985917642407 -> (18.39114989602229, 9783.415732755751), 3.365796545976432 -> (7.343282617430643, 9790.759015373182), 3.423680137621862 -> (30.285094984710557, 9821.044110357892), 3.5361960382135766 -> (16.022107226715036, 9837.066217584608), 3.708086046825087 -> (28.25855387318669, 9865.324771457796), 3.8172837056780202 -> (0.047583335239924196, 9865.372354793035), 4.128936063148887 -> (16.62764520696945, 9882.000000000004), 8.015570304845104 -> (7.0, 9889.000000000004), 8.744451287156746 -> (13.0, 9902.000000000004), 9.25232140353154 -> (16.0, 9918.000000000004), 9.530110739378491 -> (12.21860057292937, 9930.218600572933), 9.764762794436761 -> (9.854383912374931, 9940.072984485307), 9.817748101058383 -> (0.9270155146956984, 9941.000000000002), 9.897390259617893 -> (3.0, 9944.000000000002), 10.013981101637214 -> (5.806895868211391, 9949.806895868212), 10.089622842201312 -> (6.193104131788609, 9956.000000000002), 10.23781043312598 -> (1.0, 9957.000000000002), 10.328279750526765 -> (6.938509789117737, 9963.938509789119), 10.431120116662031 -> (5.061490210882263, 9969.000000000002), 10.466786830859942 -> (4.0, 9973.000000000002), 10.56075722636066 -> (2.0, 9975.000000000002), 10.722439204327575 -> (4.0, 9979.000000000002), 10.797997919393662 -> (1.0, 9980.000000000002), 10.914286987473139 -> (3.0, 9983.000000000002), 11.029145551894192 -> (3.0, 9986.000000000002), 11.15766037328291 -> (0.5600152717057256, 9986.560015271707), 11.160788156092288 -> (0.003690029453065069, 9986.56370530116), 11.160856304869647 -> (2.4384456998649116, 9989.002151001025), 11.191788227887427 -> (0.9978489989762978, 9990.000000000002), 11.260505056159252 -> (1.0, 9991.000000000002), 11.377434107003292 -> (1.0, 9992.000000000002), 11.443715653916865 -> (1.0, 9993.000000000002), 11.5156156303936 -> (1.0, 9994.000000000002), 11.539205812425335 -> (1.0, 9995.000000000002), 11.569770306228012 -> (1.0, 9996.000000000002), 11.700351579256392 -> (1.0, 9997.000000000002), 11.75051572042176 -> (1.0, 9998.000000000002), 12.004778690455263 -> (1.0, 9999.000000000002), 13.06055211943455 -> (1.0, 10000.000000000002)))

We can evaluate the t-digest `td` as a cummulative distribution function or CDF at `x` via the `.cdf(x)` method.

``` scala
td.cdf(1.0)
```

>     res22: Double = 0.5005037034803238

We can also get the inverse CDF at any `u` in the unit interval to get quantiles as follows.

``` scala
val cutOff = td.cdfInverse(0.99)
```

>     cutOff: Double = 9.072447729196986

Let's flag those points that cross the threshold determine dby the `cutOff`.

``` scala
val dataFlagged = data.withColumn("anomalous",$"value">cutOff)
```

>     dataFlagged: org.apache.spark.sql.DataFrame = [value: double, anomalous: boolean]

Let's show and display the anomalous points.

We are not interested in word-wars over anomalies and outliers here (at the end of the day we are really only interested in the real problem that these arithmetic and syntactic expressions will be used to solve, such as,:

-   keep a washing machine running longer by shutting it down before it will break down (predictive maintenance)
-   keep a network from being attacked by bots/malware/etc by flagging any unusual events worth escalating to the network security opes teams (without annoying them constantly!)
-   etc.

``` scala
data.withColumn("anomalous",$"value">cutOff).filter("anomalous").show(5)
```

>     +------------------+---------+
>     |             value|anomalous|
>     +------------------+---------+
>     | 9.639219241219372|     true|
>     |11.539205812425335|     true|
>     | 9.423175513609095|     true|
>     |10.174199861232976|     true|
>     |10.442627838980057|     true|
>     +------------------+---------+
>     only showing top 5 rows

``` scala
display(dataFlagged)
```

Apply the batch-learnt T-Digest on a new stream of data
=======================================================

First let's simulate historical data for batch-processing.

``` scala
import scala.util.Random
import scala.util.Random._

// simulate 5 bursts of historical data - emulate batch processing

// make a sample to produce a mixture of two normal RVs with standard deviation 1 but with different location or mean parameters
def myMixtureOf2Normals( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : (String, Double) = {
  val sample = if (r.nextDouble <= normalWeight) {r.nextGaussian+normalLocation } 
               else {r.nextGaussian + abnormalLocation} 
  Thread.sleep(5L) // sleep 5 milliseconds
  val now = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new java.util.Date())
  return (now,sample)
   }
   
 dbutils.fs.rm("/datasets/batchFiles/",true) // this is to delete the directory before staring a job
 
val r = new Random(123454321L)
var a = 0;
// for loop execution to write files to distributed fs
for( a <- 1 to 5){
  val data = sc.parallelize(Vector.fill(100){myMixtureOf2Normals(1.0, 10.0, 0.99, r)}).coalesce(1).toDF.as[(String,Double)]
  val minute = (new java.text.SimpleDateFormat("mm")).format(new java.util.Date())
  val second = (new java.text.SimpleDateFormat("ss")).format(new java.util.Date())
  data.write.mode(SaveMode.Overwrite).csv("/datasets/batchFiles/" + minute +"_" + second + ".csv")
  Thread.sleep(10L) // sleep 10 milliseconds
}
```

>     import scala.util.Random
>     import scala.util.Random._
>     myMixtureOf2Normals: (normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: scala.util.Random)(String, Double)
>     r: scala.util.Random = scala.util.Random@74ae0182
>     a: Int = 0

``` scala
display(dbutils.fs.ls("/datasets/batchFiles/"))
```

| path                                  | name        | size |
|---------------------------------------|-------------|------|
| dbfs:/datasets/batchFiles/07\_25.csv/ | 07\_25.csv/ | 0.0  |
| dbfs:/datasets/batchFiles/07\_29.csv/ | 07\_29.csv/ | 0.0  |
| dbfs:/datasets/batchFiles/07\_31.csv/ | 07\_31.csv/ | 0.0  |
| dbfs:/datasets/batchFiles/07\_33.csv/ | 07\_33.csv/ | 0.0  |
| dbfs:/datasets/batchFiles/07\_35.csv/ | 07\_35.csv/ | 0.0  |

Now let's use a static DataFrame to process these files with t-digest and get the 0.99-th quantile based Cut-off.

``` scala
// Read all the csv files written atomically in a directory
import org.apache.spark.sql.types._

val timedScore = new StructType().add("time", "timestamp").add("score", "Double")

import java.sql.{Date, Timestamp}
case class timedScoreCC(time: Timestamp, score: Double)

//val streamingLines = sc.textFile("/datasets/streamingFiles/*").toDF.as[String]
val staticLinesDS = spark
  .read
  .option("sep", ",")
  .schema(timedScore)      // Specify schema of the csv files
  .csv("/datasets/batchFiles/*").as[timedScoreCC]

val udaf = tdigestUDAF[Double].delta(0.2).maxDiscrete(25)

val batchLearntCutOff99 = staticLinesDS
                  .agg(udaf($"score").as("td"))
                  .first.getAs[TDigestSQL](0)
                  .tdigest
                  .cdfInverse(0.99)
```

>     import org.apache.spark.sql.types._
>     timedScore: org.apache.spark.sql.types.StructType = StructType(StructField(time,TimestampType,true), StructField(score,DoubleType,true))
>     import java.sql.{Date, Timestamp}
>     defined class timedScoreCC
>     staticLinesDS: org.apache.spark.sql.Dataset[timedScoreCC] = [time: timestamp, score: double]
>     udaf: org.isarnproject.sketches.udaf.TDigestUDAF[Double] = TDigestUDAF(0.2,25)
>     batchLearntCutOff99: Double = 8.681901452463396

We will next execute the companion notebook `040a_TDigestInputStream` in order to generate the files with the Gaussian mixture for streaming jobs.

The code in the companion notebook is as follows for convenience (you could just copy-paste this code into another notebook in the same cluster with the same distributed file system):

\`\`\`%scala
import scala.util.Random
import scala.util.Random.\_

// make a sample to produce a mixture of two normal RVs with standard deviation 1 but with different location or mean parameters
def myMixtureOf2Normals( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : (String, Double) = {
val sample = if (r.nextDouble &lt;= normalWeight) {r.nextGaussian+normalLocation }
else {r.nextGaussian + abnormalLocation}
Thread.sleep(5L) // sleep 5 milliseconds
val now = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new java.util.Date())
return (now,sample)
}

dbutils.fs.rm("/datasets/streamingFiles/",true) // this is to delete the directory before staring a job

val r = new Random(12345L)
var a = 0;
// for loop execution to write files to distributed fs
for( a &lt;- 1 to 20){
val data = sc.parallelize(Vector.fill(100){myMixtureOf2Normals(1.0, 10.0, 0.99, r)}).coalesce(1).toDF.as\[(String,Double)\]
val minute = (new java.text.SimpleDateFormat("mm")).format(new java.util.Date())
val second = (new java.text.SimpleDateFormat("ss")).format(new java.util.Date())
data.write.mode(SaveMode.Overwrite).csv("/datasets/streamingFiles/" + minute +"\_" + second + ".csv")
Thread.sleep(5000L) // sleep 5 seconds
}
\`\`\`

We will simply apply the batch-learnt t-digest as the threshold for determining if the streaming data is anomalous or not.

``` scala
import org.apache.spark.sql.types._
import java.sql.{Date, Timestamp}

val timedScore = new StructType().add("time", "timestamp").add("score", "Double")
case class timedScoreCC(time: Timestamp, score: Double)

val streamingLinesDS = spark
  .readStream
  .option("sep", ",")
  .schema(timedScore)      // Specify schema of the csv files
  .csv("/datasets/streamingFiles/*").as[timedScoreCC]
```

>     import org.apache.spark.sql.types._
>     import java.sql.{Date, Timestamp}
>     timedScore: org.apache.spark.sql.types.StructType = StructType(StructField(time,TimestampType,true), StructField(score,DoubleType,true))
>     defined class timedScoreCC
>     streamingLinesDS: org.apache.spark.sql.Dataset[timedScoreCC] = [time: timestamp, score: double]

``` scala
//display(streamingLinesDS)
```

Now, we can apply this batch-learnt cut-off from the static DataSet to the streaming DataSet.

This is a simple example of learning in batch mode (say overnight or every few hours) and applying it to live streaming data.

``` scala
// Start running the query that prints the running counts to the console
val dataFalgged = streamingLinesDS
      .withColumn("anomalous",$"score" > batchLearntCutOff99).filter($"anomalous")
      .writeStream
      //.outputMode("complete")
      .format("console")
      .start()

dataFalgged.awaitTermination() // hit cancel to terminate
```

>     -------------------------------------------
>     Batch: 0
>     -------------------------------------------
>     +--------------------+------------------+---------+
>     |                time|             score|anomalous|
>     +--------------------+------------------+---------+
>     |2017-11-23 14:18:...| 9.423175513609095|     true|
>     |2017-11-23 14:18:...|11.539205812425335|     true|
>     |2017-11-23 14:18:...| 9.639219241219372|     true|
>     +--------------------+------------------+---------+
>
>     -------------------------------------------
>     Batch: 1
>     -------------------------------------------
>     +----+-----+---------+
>     |time|score|anomalous|
>     +----+-----+---------+
>     +----+-----+---------+
>
>     -------------------------------------------
>     Batch: 2
>     -------------------------------------------
>     +--------------------+------------------+---------+
>     |                time|             score|anomalous|
>     +--------------------+------------------+---------+
>     |2017-11-23 14:19:...|  8.99959554980265|     true|
>     |2017-11-23 14:19:...|10.174199861232976|     true|
>     +--------------------+------------------+---------+
>
>     -------------------------------------------
>     Batch: 3
>     -------------------------------------------
>     +--------------------+------------------+---------+
>     |                time|             score|anomalous|
>     +--------------------+------------------+---------+
>     |2017-11-23 14:19:...|10.442627838980057|     true|
>     |2017-11-23 14:19:...|10.460772141286911|     true|
>     |2017-11-23 14:19:...|11.260505056159252|     true|
>     +--------------------+------------------+---------+
>
>     -------------------------------------------
>     Batch: 4
>     -------------------------------------------
>     +--------------------+-----------------+---------+
>     |                time|            score|anomalous|
>     +--------------------+-----------------+---------+
>     |2017-11-23 14:19:...|9.905282503779972|     true|
>     +--------------------+-----------------+---------+
>
>     -------------------------------------------
>     Batch: 5
>     -------------------------------------------
>     +----+-----+---------+
>     |time|score|anomalous|
>     +----+-----+---------+
>     +----+-----+---------+
>
>     -------------------------------------------
>     Batch: 6
>     -------------------------------------------
>     +--------------------+-----------------+---------+
>     |                time|            score|anomalous|
>     +--------------------+-----------------+---------+
>     |2017-11-23 14:19:...|9.102639076417908|     true|
>     +--------------------+-----------------+---------+
>
>     -------------------------------------------
>     Batch: 7
>     -------------------------------------------
>     +----+-----+---------+
>     |time|score|anomalous|
>     +----+-----+---------+
>     +----+-----+---------+
>
>     -------------------------------------------
>     Batch: 8
>     -------------------------------------------
>     +--------------------+------------------+---------+
>     |                time|             score|anomalous|
>     +--------------------+------------------+---------+
>     |2017-11-23 14:19:...| 9.695132992174205|     true|
>     |2017-11-23 14:19:...|10.439052640762693|     true|
>     +--------------------+------------------+---------+
>
>     -------------------------------------------
>     Batch: 9
>     -------------------------------------------
>     +--------------------+-----------------+---------+
>     |                time|            score|anomalous|
>     +--------------------+-----------------+---------+
>     |2017-11-23 14:19:...|10.02254460606071|     true|
>     |2017-11-23 14:19:...|9.311690918035534|     true|
>     +--------------------+-----------------+---------+
>
>     -------------------------------------------
>     Batch: 10
>     -------------------------------------------
>     +--------------------+-----------------+---------+
>     |                time|            score|anomalous|
>     +--------------------+-----------------+---------+
>     |2017-11-23 14:19:...|9.454926349089147|     true|
>     +--------------------+-----------------+---------+
>
>     -------------------------------------------
>     Batch: 11
>     -------------------------------------------
>     +--------------------+----------------+---------+
>     |                time|           score|anomalous|
>     +--------------------+----------------+---------+
>     |2017-11-23 14:20:...|9.87803253322451|     true|
>     +--------------------+----------------+---------+
>
>     -------------------------------------------
>     Batch: 12
>     -------------------------------------------
>     +----+-----+---------+
>     |time|score|anomalous|
>     +----+-----+---------+
>     +----+-----+---------+
>
>     -------------------------------------------
>     Batch: 13
>     -------------------------------------------
>     +--------------------+-----------------+---------+
>     |                time|            score|anomalous|
>     +--------------------+-----------------+---------+
>     |2017-11-23 14:20:...|9.858438409632281|     true|
>     |2017-11-23 14:20:...|10.45683581285141|     true|
>     +--------------------+-----------------+---------+
>
>     -------------------------------------------
>     Batch: 14
>     -------------------------------------------
>     +--------------------+-----------------+---------+
>     |                time|            score|anomalous|
>     +--------------------+-----------------+---------+
>     |2017-11-23 14:20:...|9.311726779124077|     true|
>     |2017-11-23 14:20:...|8.994959541314255|     true|
>     +--------------------+-----------------+---------+
>
>     -------------------------------------------
>     Batch: 15
>     -------------------------------------------
>     +----+-----+---------+
>     |time|score|anomalous|
>     +----+-----+---------+
>     +----+-----+---------+
>
>     -------------------------------------------
>     Batch: 16
>     -------------------------------------------
>     +----+-----+---------+
>     |time|score|anomalous|
>     +----+-----+---------+
>     +----+-----+---------+

Although the above pattern of estimating the 99% Cut-Off periodically by batch-processing static DataSets from historical data and then applying these Cut-Offs to filter anamolous data points that are currently streaming at us is *good enough* for several applications, we may want to do *online estimation/learning* of the Cut-Off based on the 99% of all the data up to present time and use this *live* Cut-off to decide which point is anamolous now.

For this we need to use more delicate parts of Structured Streaming.

Streaming T-Digest - Online Updating of the Cut-Off
===================================================

To impelment a streaming t-digest of the data that keeps the current threshold and a current t-digest, we need to get into more delicate parts of structured streaming and implement our own `flatMapgroupsWithState`.

Here are some starting points for diving deeper in this direction of *arbitrary stateful processing*:

-   <https://databricks.com/blog/2017/10/17/arbitrary-stateful-processing-in-apache-sparks-structured-streaming.html>.
-   Spark Summit EU Dublin 2017 Deep Dive: <https://databricks.com/session/deep-dive-stateful-stream-processing>
-   See <https://youtu.be/JAb4FIheP28>
-   Official [docs and examples of arbitrary stateful operations](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#arbitrary-stateful-operations)
    -   doc: <https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.streaming.GroupState>
    -   example: <https://github.com/apache/spark/blob/v2.2.0/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredSessionization.scala>
-   See [Part 14](http://blog.madhukaraphatak.com/introduction-to-spark-structured-streaming-part-14/) of [the 14-part series on structured streaming series in Madhukar's blog](http://blog.madhukaraphatak.com/categories/introduction-structured-streaming/)
-   Authoritative resource: <https://jaceklaskowski.gitbooks.io/spark-structured-streaming/content/spark-sql-streaming-KeyValueGroupedDataset-flatMapGroupsWithState.html>

Streaming Machine Learning and Structured Streaming
===================================================

Ultimately we want to use structured streaming for online machine learning algorithms and not just sketching.

-   See Spark Summit 2016 video on streaming ML (streaming logistic regression): <https://youtu.be/r0hyjmLMMOc>
-   [Holden Karau's codes from High Performance Spark Book](https://github.com/holdenk/spark-structured-streaming-ml/tree/master/src/main/scala/com/high-performance-spark-examples/structuredstreaming)

Data Engineering Science Pointers
=================================

Using kafka, Cassandra and Spark Structured Streaming

-   <https://github.com/ansrivas/spark-structured-streaming>
-   <https://github.com/polomarcus/Spark-Structured-Streaming-Examples>