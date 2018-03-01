[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Activity Recognition from Accelerometer using Random Forest
===========================================================

This work is a simpler databricksification of Amira Lakhal's more complex framework for activity recognition:
\* <https://github.com/MiraLak/activityRecognitionV2>.

[![Amira's video](http://img.youtube.com/vi/1DV9Kdec0-A/0.jpg)](https://www.youtube.com/watch?v=1DV9Kdec0-A)

See Section below on **Download and Load Data** first.

``` scala
val data = sc.textFile("dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv")
```

>     data: org.apache.spark.rdd.RDD[String] = dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv MapPartitionsRDD[45250] at textFile at <console>:34

``` scala
data.take(5).foreach(println)
```

>     "user_id","activity","timeStampAsLong","x","y","z"
>     "user_001","Jumping",1446047227606,"4.33079","-12.72175","-3.18118"
>     "user_001","Jumping",1446047227671,"0.575403","-0.727487","2.95007"
>     "user_001","Jumping",1446047227735,"-1.60885","3.52607","-0.1922"
>     "user_001","Jumping",1446047227799,"0.690364","-0.037722","1.72382"

``` scala
val dataDF = sqlContext.read    
    .format("com.databricks.spark.csv") // use spark.csv package
    .option("header", "true") // Use first line of all files as header
    .option("inferSchema", "true") // Automatically infer data types
    .option("delimiter", ",") // Specify the delimiter as ','
    .load("dbfs:///datasets/sds/ActivityRecognition/dataTraining.csv")
```

>     dataDF: org.apache.spark.sql.DataFrame = [user_id: string, activity: string ... 4 more fields]

``` scala
dataDF.printSchema()
```

>     root
>      |-- user_id: string (nullable = true)
>      |-- activity: string (nullable = true)
>      |-- timeStampAsLong: long (nullable = true)
>      |-- x: double (nullable = true)
>      |-- y: double (nullable = true)
>      |-- z: double (nullable = true)

``` scala
display(dataDF)
```

| user\_id  | activity | timeStampAsLong   | x         | y          | z        |
|-----------|----------|-------------------|-----------|------------|----------|
| user\_001 | Jumping  | 1.446047227606e12 | 4.33079   | -12.72175  | -3.18118 |
| user\_001 | Jumping  | 1.446047227671e12 | 0.575403  | -0.727487  | 2.95007  |
| user\_001 | Jumping  | 1.446047227735e12 | -1.60885  | 3.52607    | -0.1922  |
| user\_001 | Jumping  | 1.446047227799e12 | 0.690364  | -3.7722e-2 | 1.72382  |
| user\_001 | Jumping  | 1.446047227865e12 | 3.44943   | -1.68549   | 2.29862  |
| user\_001 | Jumping  | 1.44604722793e12  | 1.87829   | -1.91542   | 0.880768 |
| user\_001 | Jumping  | 1.446047227995e12 | 1.57173   | -5.86241   | -3.75599 |
| user\_001 | Jumping  | 1.446047228059e12 | 3.41111   | -17.93331  | 0.535886 |
| user\_001 | Jumping  | 1.446047228123e12 | 3.18118   | -19.58108  | 5.74745  |
| user\_001 | Jumping  | 1.446047228189e12 | 7.85626   | -19.2362   | 0.804128 |
| user\_001 | Jumping  | 1.446047228253e12 | 1.26517   | -8.85139   | 2.18366  |
| user\_001 | Jumping  | 1.446047228318e12 | 7.7239e-2 | 1.15021    | 1.53221  |
| user\_001 | Jumping  | 1.446047228383e12 | 0.230521  | 2.0699     | -1.41845 |
| user\_001 | Jumping  | 1.446047228447e12 | 0.652044  | -0.497565  | 1.76214  |
| user\_001 | Jumping  | 1.446047228512e12 | 1.53341   | -0.305964  | 1.41725  |
| user\_001 | Jumping  | 1.446047228578e12 | -1.07237  | -1.95374   | 0.191003 |
| user\_001 | Jumping  | 1.446047228642e12 | 2.75966   | -13.75639  | 0.191003 |
| user\_001 | Jumping  | 1.446047228707e12 | 7.43474   | -19.58108  | 2.95007  |
| user\_001 | Jumping  | 1.446047228771e12 | 5.63368   | -19.58108  | 5.59417  |
| user\_001 | Jumping  | 1.446047228836e12 | 5.02056   | -11.72542  | -1.38013 |
| user\_001 | Jumping  | 1.4460472289e12   | -2.10702  | 0.575403   | 1.07237  |
| user\_001 | Jumping  | 1.446047228966e12 | -1.30229  | 2.2615     | -1.26517 |
| user\_001 | Jumping  | 1.44604722903e12  | 1.68669   | -0.957409  | 1.57053  |
| user\_001 | Jumping  | 1.446047229095e12 | 2.60638   | -0.229323  | 2.14534  |
| user\_001 | Jumping  | 1.446047229159e12 | 1.30349   | -0.152682  | 0.497565 |
| user\_001 | Jumping  | 1.446047229224e12 | 1.64837   | -8.12331   | 1.60885  |
| user\_001 | Jumping  | 1.44604722929e12  | 0.15388   | -18.46979  | -1.03525 |
| user\_001 | Jumping  | 1.446047229354e12 | 4.98224   | -19.58108  | 0.995729 |
| user\_001 | Jumping  | 1.446047229419e12 | 5.17384   | -19.2362   | 0.612526 |
| user\_001 | Jumping  | 1.446047229483e12 | 2.03158   | -9.11964   | 1.34061  |

Truncated to 30 rows

``` scala
dataDF.count()
```

>     res2: Long = 13679

``` scala
dataDF.select($"user_id").distinct().show()
```

>     +--------+
>     | user_id|
>     +--------+
>     |user_002|
>     |user_006|
>     |user_005|
>     |user_001|
>     |user_007|
>     |user_003|
>     |user_004|
>     +--------+

``` scala
dataDF.select($"activity").distinct().show()
```

>     +--------+
>     |activity|
>     +--------+
>     | Sitting|
>     | Walking|
>     | Jumping|
>     |Standing|
>     | Jogging|
>     +--------+

``` scala
display(dataDF.sample(false,0.1))
```

| user\_id  | activity | timeStampAsLong   | x         | y          | z         |
|-----------|----------|-------------------|-----------|------------|-----------|
| user\_001 | Jumping  | 1.446047227865e12 | 3.44943   | -1.68549   | 2.29862   |
| user\_001 | Jumping  | 1.44604722793e12  | 1.87829   | -1.91542   | 0.880768  |
| user\_001 | Jumping  | 1.446047228836e12 | 5.02056   | -11.72542  | -1.38013  |
| user\_001 | Jumping  | 1.446047229159e12 | 1.30349   | -0.152682  | 0.497565  |
| user\_001 | Jumping  | 1.446047229419e12 | 5.17384   | -19.2362   | 0.612526  |
| user\_001 | Jumping  | 1.446047230066e12 | 7.89458   | -16.9753   | 0.229323  |
| user\_001 | Jumping  | 1.44604723026e12  | -1.95374  | -0.114362  | -0.268841 |
| user\_001 | Jumping  | 1.446047233368e12 | 1.83997   | -0.152682  | 1.41725   |
| user\_001 | Jumping  | 1.446047233692e12 | 1.91661   | -2.95007   | -0.345482 |
| user\_001 | Jumping  | 1.44604723531e12  | -2.18366  | 3.14286    | -0.1922   |
| user\_001 | Jumping  | 1.446047235504e12 | 1.95493   | -0.689167  | 1.49389   |
| user\_001 | Jumping  | 1.446047235828e12 | 10.577    | -19.4278   | -1.57173  |
| user\_001 | Jumping  | 1.446047236086e12 | 0.15388   | -0.305964  | 0.995729  |
| user\_001 | Jumping  | 1.446047237057e12 | 10.34708  | -19.58108  | 2.37526   |
| user\_001 | Jumping  | 1.446047237317e12 | -1.64717  | -0.650847  | 1.41725   |
| user\_001 | Jumping  | 1.446047237964e12 | 2.6447    | -3.7722e-2 | 1.18733   |
| user\_001 | Jumping  | 1.446047238481e12 | -0.152682 | -1.41725   | -1.22685  |
| user\_001 | Jumping  | 1.446047238806e12 | 5.99e-4   | -0.650847  | 0.497565  |
| user\_001 | Jumping  | 1.446047239647e12 | 9.58068   | -19.58108  | 1.53221   |
| user\_001 | Jumping  | 1.446047239841e12 | 0.11556   | 2.10822    | -2.6447   |
| user\_001 | Jumping  | 1.446047240618e12 | 0.268841  | -1.91542   | 3.06503   |
| user\_001 | Jumping  | 1.446047241654e12 | 10.15548  | -19.58108  | 8.58315   |
| user\_001 | Jumping  | 1.446047241977e12 | 0.15388   | -0.919089  | 2.18366   |
| user\_001 | Jumping  | 1.446047242948e12 | 13.02951  | -19.58108  | -0.498763 |
| user\_001 | Jumping  | 1.446047243207e12 | 7.7239e-2 | -0.344284  | 1.91542   |
| user\_001 | Jumping  | 1.446047243531e12 | 11.57333  | -19.58108  | 0.344284  |
| user\_003 | Jumping  | 1.446047778746e12 | -0.842448 | 0.11556    | -1.26517  |
| user\_003 | Jumping  | 1.44604777894e12  | -1.76214  | -15.44249  | -2.68302  |
| user\_003 | Jumping  | 1.446047779524e12 | -2.91174  | 3.10454    | -2.37646  |
| user\_003 | Jumping  | 1.44604778043e12  | -1.99206  | -14.44616  | -3.64103  |

Truncated to 30 rows

Feature Selection on Running Windows
====================================

A Markov Process Assumption
---------------------------

This is sensible since the subjects are not instantaneously changing between the activities of interest: sitting, walking, jogging, etc.
Thus it makes sense to try and use the most recent accelerometer readings (from the immediate past) to predict the current activity.

See the following for a crash introduction to windows:
\* <http://xinhstechblog.blogspot.co.nz/2016/04/spark-window-functions-for-dataframes.html>

``` scala
 // Import the window functions.
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

// Create a window specification
val windowSize = 10
val wSpec1 = Window.partitionBy("user_id","activity").orderBy("timeStampAsLong").rowsBetween(-windowSize, 0)
```

>     import org.apache.spark.sql.expressions.Window
>     import org.apache.spark.sql.functions._
>     windowSize: Int = 10
>     wSpec1: org.apache.spark.sql.expressions.WindowSpec = org.apache.spark.sql.expressions.WindowSpec@69e228a1

``` scala
 // Calculate the moving window statistics from data
val dataFeatDF = dataDF
      .withColumn( "meanX", mean($"x").over(wSpec1)  )
      .withColumn( "meanY", mean($"y").over(wSpec1)  )
      .withColumn( "meanZ", mean($"z").over(wSpec1)  ) 
//resultant = 1/n * ∑ √(x² + y² + z²)
      .withColumn( "SqX", pow($"x",2.0) )
      .withColumn( "SqY", pow($"y",2.0) )
      .withColumn( "SqZ", pow($"z",2.0) )
      .withColumn( "resultant", pow( $"SqX"+$"SqY"+$"SqZ",0.50 ) )
      .withColumn( "meanResultant", mean("resultant").over(wSpec1) )
// (1 / n ) * ∑ |b - mean_b|, for b in {x,y,z} 
      .withColumn( "absDevFromMeanX", abs($"x" - $"meanX") )
      .withColumn( "absDevFromMeanY", abs($"y" - $"meanY") )
      .withColumn( "absDevFromMeanZ", abs($"z" - $"meanZ") )
      .withColumn( "meanAbsDevFromMeanX", mean("absDevFromMeanX").over(wSpec1) )
      .withColumn( "meanAbsDevFromMeanY", mean("absDevFromMeanY").over(wSpec1) )
      .withColumn( "meanAbsDevFromMeanZ", mean("absDevFromMeanZ").over(wSpec1) )
//standard deviation  = √ variance = √ 1/n * ∑ (x - u)² with u = mean x
      .withColumn( "sqrDevFromMeanX", pow($"absDevFromMeanX",2.0) )
      .withColumn( "sqrDevFromMeanY", pow($"absDevFromMeanY",2.0) )
      .withColumn( "sqrDevFromMeanZ", pow($"absDevFromMeanZ",2.0) )
      .withColumn( "varianceX", mean("sqrDevFromMeanX").over(wSpec1) )
      .withColumn( "varianceY", mean("sqrDevFromMeanY").over(wSpec1) )
      .withColumn( "varianceZ", mean("sqrDevFromMeanZ").over(wSpec1) )
      .withColumn( "stddevX", pow($"varianceX",0.50) )
      .withColumn( "stddevY", pow($"varianceY",0.50) )
      .withColumn( "stddevZ", pow($"varianceZ",0.50) )
```

>     dataFeatDF: org.apache.spark.sql.DataFrame = [user_id: string, activity: string ... 27 more fields]

``` scala
display(dataFeatDF.sample(false,0.1))
```

| user\_id  | activity | timeStampAsLong   | x         | y         | z        | meanX               | meanY               | meanZ               | SqX                | SqY                   | SqZ                 |
|-----------|----------|-------------------|-----------|-----------|----------|---------------------|---------------------|---------------------|--------------------|-----------------------|---------------------|
| user\_003 | Jumping  | 1.446047779653e12 | 0.422122  | -0.114362 | 0.344284 | -2.6922735454545452 | -7.0398891818181815 | -1.9375156363636363 | 0.178186982884     | 1.3078667044000002e-2 | 0.11853147265599999 |
| user\_003 | Jumping  | 1.446047780884e12 | -3.40991  | -17.51178 | -4.59904 | -2.584279636363636  | -9.589930636363636  | -2.491419090909091  | 11.6274862081      | 306.6624387684001     | 21.151168921599997  |
| user\_003 | Jumping  | 1.446047780948e12 | -1.95374  | -10.61413 | -2.91294 | -2.3961623636363636 | -9.659604272727274  | -2.721340727272727  | 3.8170999876000002 | 112.65975565689999    | 8.485219443599998   |
| user\_003 | Jumping  | 1.446047781467e12 | -5.97737  | -18.16323 | -5.59536 | -2.981418818181818  | -10.97991272727273  | -2.8049481818181814 | 35.7289521169      | 329.90292403289993    | 31.308053529600002  |
| user\_003 | Jumping  | 1.446047781596e12 | -2.14534  | -8.88971  | -1.18853 | -2.922197           | -10.164735454545452 | -2.7387581818181816 | 4.6024837156       | 79.02694388409998     | 1.4126035609000003  |
| user\_003 | Jumping  | 1.446047781985e12 | -2.91174  | -18.35483 | -4.714   | -2.6469867272727274 | -9.426197727272728  | -2.8293346363636362 | 8.4782298276       | 336.8997843289        | 22.221796000000005  |
| user\_003 | Jumping  | 1.446047782244e12 | -1.45557  | -7.85507  | -1.22685 | -1.814390272727273  | -7.060789545454545  | -2.1012500909090908 | 2.1186840249000003 | 61.70212470490001     | 1.5051609225        |
| user\_003 | Jumping  | 1.446047783021e12 | -1.76214  | -12.22358 | -5.67201 | -2.2951368181818177 | -8.046666363636364  | -2.6028974545454546 | 3.1051373796000004 | 149.4159080164        | 32.171697440100004  |
| user\_003 | Jumping  | 1.446047783474e12 | -2.8351   | -4.63616  | -3.75599 | -1.9258677272727274 | -6.823900909090909  | -2.5610926363636364 | 8.03779201         | 21.493979545600002    | 14.107460880100001  |
| user\_003 | Jumping  | 1.446047783992e12 | -1.11069  | -2.2603   | -2.56806 | -2.2568150909090905 | -5.95298490909091   | -2.355556909090909  | 1.2336322760999998 | 5.1089560899999995    | 6.5949321636        |
| user\_003 | Jumping  | 1.446047784252e12 | -1.49389  | -6.7821   | -2.0699  | -2.1836587272727273 | -8.224333090909091  | -2.9861             | 2.2317073320999996 | 45.996880409999996    | 4.28448601          |
| user\_003 | Jumping  | 1.446047784575e12 | -4.02303  | -14.98264 | -2.2615  | -2.3125530909090912 | -8.830490727272728  | -2.8328186363636365 | 16.1847703809      | 224.4795013696        | 5.114382249999999   |
| user\_003 | Jumping  | 1.446047784834e12 | -2.4519   | 1.22685   | -1.41845 | -2.437966090909091  | -8.32535918181818   | -2.7561781818181816 | 6.011813610000001  | 1.5051609225          | 2.0120004025        |
| user\_003 | Jumping  | 1.446047785029e12 | -2.29862  | -4.3296   | -4.63736 | -2.2672662727272725 | -6.458115181818181  | -2.658635181818182  | 5.2836539044       | 18.74543616           | 21.505107769600002  |
| user\_003 | Jumping  | 1.446047785158e12 | -4.3296   | -19.38948 | -5.17384 | -2.127920545454545  | -8.778236090909092  | -3.0174524545454546 | 18.74543616        | 375.95193467039996    | 26.768620345600002  |
| user\_003 | Jumping  | 1.446047786582e12 | -1.60885  | -8.92803  | -1.15021 | -1.72033            | -7.0468553636363644 | -2.543672454545455  | 2.5883983225       | 79.7097196809         | 1.3229830441        |
| user\_003 | Jumping  | 1.446047787489e12 | -1.26397  | -9.34956  | -3.06622 | -2.2533315454545453 | -9.036027545454546  | -3.620126181818182  | 1.5976201609       | 87.41427219360001     | 9.4017050884        |
| user\_003 | Jumping  | 1.446047788071e12 | -0.382604 | -7.1653   | -1.83997 | -2.674855818181818  | -10.45388           | -3.4598772727272733 | 0.146385820816     | 51.34152409           | 3.3854896009        |
| user\_003 | Jumping  | 1.446047788719e12 | -2.10702  | -7.31858  | -2.10822 | -1.7900063636363635 | -6.74029190909091   | -2.6690856363636364 | 4.439533280399999  | 53.5616132164         | 4.444591568400001   |
| user\_003 | Jumping  | 1.446047789107e12 | -1.68549  | -16.36218 | -5.25048 | -2.5738300000000005 | -11.50246181818182  | -3.9475918181818175 | 2.8408765400999996 | 267.7209343524        | 27.567540230399995  |
| user\_003 | Jumping  | 1.44604779066e12  | -2.60518  | -15.05928 | -6.01689 | -2.5877650909090915 | -8.827006454545455  | -3.494715818181818  | 6.7869628323999995 | 226.78191411839998    | 36.2029652721       |
| user\_003 | Jumping  | 1.44604779079e12  | -3.25663  | -5.97737  | -3.0279  | -2.1906269090909096 | -7.339481909090909  | -3.2508585454545456 | 10.6056389569      | 35.7289521169         | 9.16817841          |
| user\_003 | Jumping  | 1.446047790984e12 | -3.94639  | -5.36425  | -3.75599 | -2.120953181818182  | -7.109561           | -3.2508581818181814 | 15.5739940321      | 28.7751780625         | 14.107460880100001  |
| user\_003 | Jumping  | 1.446047791179e12 | -2.52854  | -15.36585 | -6.66833 | -3.0545750000000003 | -11.485044545454546 | -4.7697354545454544 | 6.3935145316       | 236.1093462225        | 44.4666249889       |
| user\_003 | Jumping  | 1.446047791956e12 | -1.22565  | -8.16163  | -2.56806 | -1.9467694545454548 | -7.25935781818182   | -2.9965508181818183 | 1.5022179224999999 | 66.61220425690001     | 6.5949321636        |
| user\_003 | Jumping  | 1.446047792928e12 | -3.17999  | -6.20729  | -2.29982 | -2.2846846363636364 | -7.161816454545454  | -2.9268770909090907 | 10.1123364001      | 38.530449144100004    | 5.2891720324        |
| user\_003 | Jumping  | 1.446047793122e12 | -3.29495  | -3.90807  | -3.94759 | -2.758463545454546  | -7.562438181818181  | -3.372786181818181  | 10.856695502500001 | 15.2730111249         | 15.583466808099999  |
| user\_006 | Walking  | 1.446046893823e12 | -3.10335  | -4.86608  | -2.14654 | -3.7452125          | -7.615565           | -1.72501375         | 9.6307812225       | 23.678734566400003    | 4.607633971599999   |
| user\_006 | Walking  | 1.446046894342e12 | -4.67448  | -5.93905  | -1.15021 | -4.953174545454546  | -7.329032727272727  | -2.519289818181818  | 21.850763270399998 | 35.2723149025         | 1.3229830441        |
| user\_006 | Walking  | 1.4460468946e12   | -9.00468  | -11.11229 | -1.61005 | -6.134137272727273  | -8.196464545454544  | -2.0873150000000003 | 81.0842619024      | 123.4829890441        | 2.5922610025        |

Truncated to 30 rows

Truncated to 12 cols

``` scala
val splits = dataFeatDF.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))
```

>     splits: Array[org.apache.spark.sql.Dataset[org.apache.spark.sql.Row]] = Array([user_id: string, activity: string ... 27 more fields], [user_id: string, activity: string ... 27 more fields])
>     trainingData: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [user_id: string, activity: string ... 27 more fields]
>     testData: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [user_id: string, activity: string ... 27 more fields]

See
\* <http://spark.apache.org/docs/latest/ml-classification-regression.html#random-forest-classifier>
\* <http://blog.citizennet.com/blog/2012/11/10/random-forests-ensembles-and-performance-metrics>

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Random_forest"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

``` scala
import org.apache.spark.ml.feature.{StringIndexer,VectorAssembler}
import org.apache.spark.ml.Pipeline

import org.apache.spark.ml.classification.RandomForestClassifier

val transformers = Array(
              new StringIndexer().setInputCol("activity").setOutputCol("label"),
              new VectorAssembler()
                      .setInputCols(Array("meanX", "meanY", "meanZ", "stddevX", "stddevY","stddevZ"))
                      .setOutputCol("features")
)

// Train a RandomForest model.
val rf = new RandomForestClassifier() 
              .setLabelCol("label")
              .setFeaturesCol("features")
              .setNumTrees(10)
              .setFeatureSubsetStrategy("auto")
              .setImpurity("gini")
              .setMaxDepth(20)
              .setMaxBins(32)
              .setSeed(12345)

val model = new Pipeline().setStages(transformers :+ rf).fit(trainingData)
```

>     import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
>     import org.apache.spark.ml.Pipeline
>     import org.apache.spark.ml.classification.RandomForestClassifier
>     transformers: Array[org.apache.spark.ml.PipelineStage with org.apache.spark.ml.param.shared.HasOutputCol with org.apache.spark.ml.util.DefaultParamsWritable{def copy(extra: org.apache.spark.ml.param.ParamMap): org.apache.spark.ml.PipelineStage with org.apache.spark.ml.param.shared.HasOutputCol with org.apache.spark.ml.util.DefaultParamsWritable{def copy(extra: org.apache.spark.ml.param.ParamMap): org.apache.spark.ml.PipelineStage with org.apache.spark.ml.param.shared.HasOutputCol with org.apache.spark.ml.util.DefaultParamsWritable}}] = Array(strIdx_865d012b7f68, vecAssembler_7ddfcff33c0c)
>     rf: org.apache.spark.ml.classification.RandomForestClassifier = rfc_531a2c5f8138
>     model: org.apache.spark.ml.PipelineModel = pipeline_286ffbefb82f

``` scala
val accuracy: Double = 1.0 * model.transform(testData)
                                  .select("activity","label","prediction")
                                  .filter($"label"===$"prediction").count() / testData.count() 
```

>     accuracy: Double = 0.9795042897998093

We get 98% correct predictions and here are the mis-predicted ones.

``` scala
display(model.transform(testData).select("activity","label","prediction").filter(not($"label"===$"prediction")))
```

| activity | label | prediction |
|----------|-------|------------|
| Jumping  | 4.0   | 0.0        |
| Walking  | 2.0   | 1.0        |
| Walking  | 2.0   | 1.0        |
| Walking  | 2.0   | 0.0        |
| Walking  | 2.0   | 0.0        |
| Walking  | 2.0   | 0.0        |
| Walking  | 2.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jogging  | 0.0   | 4.0        |
| Jumping  | 4.0   | 0.0        |
| Jumping  | 4.0   | 0.0        |
| Jogging  | 0.0   | 2.0        |
| Jogging  | 0.0   | 4.0        |
| Jogging  | 0.0   | 2.0        |
| Jogging  | 0.0   | 4.0        |
| Jogging  | 0.0   | 4.0        |
| Jogging  | 0.0   | 4.0        |
| Jogging  | 0.0   | 1.0        |
| Jogging  | 0.0   | 1.0        |
| Jumping  | 4.0   | 0.0        |
| Walking  | 2.0   | 0.0        |

Truncated to 30 rows

Download and Load Data
----------------------

The following anonymized dataset is used with kind permission of Amira Lakhal.

``` sh
wget http://lamastex.org/datasets/public/ActivityRecognition/dataTraining.csv
```

>     --2017-01-09 23:39:33--  http://lamastex.org/datasets/public/ActivityRecognition/dataTraining.csv
>     Resolving lamastex.org (lamastex.org)... 166.62.28.100
>     Connecting to lamastex.org (lamastex.org)|166.62.28.100|:80... connected.
>     HTTP request sent, awaiting response... 200 OK
>     Length: 931349 (910K) [text/csv]
>     Saving to: ‘dataTraining.csv’
>
>          0K .......... .......... .......... .......... ..........  5%  131K 7s
>         50K .......... .......... .......... .......... .......... 10%  262K 5s
>        100K .......... .......... .......... .......... .......... 16%  263K 4s
>        150K .......... .......... .......... .......... .......... 21%  263K 3s
>        200K .......... .......... .......... .......... .......... 27% 57.5M 3s
>        250K .......... .......... .......... .......... .......... 32%  264K 2s
>        300K .......... .......... .......... .......... .......... 38% 65.2M 2s
>        350K .......... .......... .......... .......... .......... 43% 68.4M 1s
>        400K .......... .......... .......... .......... .......... 49%  264K 1s
>        450K .......... .......... .......... .......... .......... 54% 57.0M 1s
>        500K .......... .......... .......... .......... .......... 60% 54.2M 1s
>        550K .......... .......... .......... .......... .......... 65%  265K 1s
>        600K .......... .......... .......... .......... .......... 71% 55.4M 1s
>        650K .......... .......... .......... .......... .......... 76% 67.7M 0s
>        700K .......... .......... .......... .......... .......... 82% 49.9M 0s
>        750K .......... .......... .......... .......... .......... 87%  636K 0s
>        800K .......... .......... .......... .......... .......... 93%  455K 0s
>        850K .......... .......... .......... .......... .......... 98% 67.5M 0s
>        900K .........                                             100% 57.1M=1.7s
>
>     2017-01-09 23:39:35 (530 KB/s) - ‘dataTraining.csv’ saved [931349/931349]

``` sh
pwd && ls
```

>     /databricks/driver
>     dataTraining.csv
>     derby.log
>     eventlogs
>     logs

``` scala
//dbutils.fs.mkdirs("dbfs:///datasets/sds/ActivityRecognition")
dbutils.fs.mv("file:///databricks/driver/dataTraining.csv","dbfs:///datasets/sds/ActivityRecognition/")
```

>     res13: Boolean = true

``` scala
display(dbutils.fs.ls("dbfs:///datasets/sds/ActivityRecognition"))
```

| path                                                    | name             | size     |
|---------------------------------------------------------|------------------|----------|
| dbfs:/datasets/sds/ActivityRecognition/dataTraining.csv | dataTraining.csv | 931349.0 |