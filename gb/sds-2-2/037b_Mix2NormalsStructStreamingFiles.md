Write files periodically with normal mixture samples for structured streaming
=============================================================================

This notebook can be used to write files every few seconds into the distributed file system where each of these files contains a time stamp field followed by randomly drawn words.

After running the commands in this notebook you should have a a set of files named by the minute and second for easy setting up of structured streaming jobs in another notebook.

Mixture of 2 Normals
--------------------

Here we will write some Gaussian mixture samples to files.

    import scala.util.Random
    import scala.util.Random._

    // make a sample to produce a mixture of two normal RVs with standard deviation 1 but with different location or mean parameters
    def myMixtureOf2Normals( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : (String, Double) = {
      val sample = if (r.nextDouble <= normalWeight) {r.nextGaussian+normalLocation } 
                   else {r.nextGaussian + abnormalLocation} 
      Thread.sleep(5L) // sleep 5 milliseconds
      val now = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new java.util.Date())
      return (now,sample)
       }

> import scala.util.Random import scala.util.Random.\_ myMixtureOf2Normals: (normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: scala.util.Random)(String, Double)

    val r = new Random(1L)
    println(myMixtureOf2Normals(1.0, 10.0, 0.99, r), myMixtureOf2Normals(1.0, 10.0, 0.99, r))
    // should always produce samples as (0.5876430182311466,-0.34037937678788865) when seed = 1L

> ((2017-11-22 09:55:37.997,0.5876430182311466),(2017-11-22 09:55:38.002,-0.34037937678788865)) r: scala.util.Random = scala.util.Random@44ccefc6

    dbutils.fs.rm("/datasets/streamingFilesNormalMixture/",true) // this is to delete the directory before staring a job

> res24: Boolean = true

    val r = new Random(12345L) // set seed for reproducibility
    var a = 0;
    // for loop execution to write files to distributed fs
    for( a <- 1 to 5){
      // make a DataSet
      val data = sc.parallelize(Vector.fill(100){myMixtureOf2Normals(1.0, 10.0, 0.99, r)}) // 100 samples from mixture
                   .coalesce(1) // this is to make sure that we have only one partition per dir
                   .toDF.as[(String,Double)]
      val minute = (new java.text.SimpleDateFormat("mm")).format(new java.util.Date())
      val second = (new java.text.SimpleDateFormat("ss")).format(new java.util.Date())
      // write to dbfs
      data.write.mode(SaveMode.Overwrite).csv("/datasets/streamingFilesNormalMixture/" + minute +"_" + second)
      Thread.sleep(5000L) // sleep 5 seconds
    }

> r: scala.util.Random = scala.util.Random@67919852 a: Int = 0

    display(dbutils.fs.ls("/datasets/streamingFilesNormalMixture/"))

| dbfs:/datasets/streamingFilesNormalMixture/57\_48/ | 57\_48/ | 0.0 |
|----------------------------------------------------|---------|-----|
| dbfs:/datasets/streamingFilesNormalMixture/57\_55/ | 57\_55/ | 0.0 |
| dbfs:/datasets/streamingFilesNormalMixture/58\_02/ | 58\_02/ | 0.0 |
| dbfs:/datasets/streamingFilesNormalMixture/58\_09/ | 58\_09/ | 0.0 |
| dbfs:/datasets/streamingFilesNormalMixture/58\_16/ | 58\_16/ | 0.0 |

    val df_csv = spark.read.option("inferSchema", "true").csv("/datasets/streamingFilesNormalMixture/57_48/*.csv")

> df\_csv: org.apache.spark.sql.DataFrame = \[\_c0: timestamp, \_c1: double\]

    df_csv.show(10,false) // first 10

> +-----------------------+---------------------+ |\_c0 |\_c1 | +-----------------------+---------------------+ |2017-11-22 09:57:48.039|0.2576188264990721 | |2017-11-22 09:57:48.044|-0.13149698512045327 | |2017-11-22 09:57:48.049|1.4139063973267458 | |2017-11-22 09:57:48.054|-0.023833875968513496| |2017-11-22 09:57:48.059|0.7274784426774964 | |2017-11-22 09:57:48.064|-1.0658630481235276 | |2017-11-22 09:57:48.069|0.746959841932221 | |2017-11-22 09:57:48.075|0.30477096247050206 | |2017-11-22 09:57:48.08 |-0.06407620682061621 | |2017-11-22 09:57:48.085|1.8464307210258604 | +-----------------------+---------------------+ only showing top 10 rows

[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Take a peek at what was written.

    display(dbutils.fs.ls("/datasets/streamingFilesNormalMixture/57_48/"))

| dbfs:/datasets/streamingFilesNormalMixture/57\_48/\_SUCCESS                                                                          | \_SUCCESS                                                                          | 0.0    |
|--------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|--------|
| dbfs:/datasets/streamingFilesNormalMixture/57\_48/\_committed\_3065630503555994154                                                   | \_committed\_3065630503555994154                                                   | 109.0  |
| dbfs:/datasets/streamingFilesNormalMixture/57\_48/\_started\_3065630503555994154                                                     | \_started\_3065630503555994154                                                     | 0.0    |
| dbfs:/datasets/streamingFilesNormalMixture/57\_48/part-00000-tid-3065630503555994154-a76cadd5-380e-4fe5-a4bf-962ca479c8de-0-c000.csv | part-00000-tid-3065630503555994154-a76cadd5-380e-4fe5-a4bf-962ca479c8de-0-c000.csv | 4313.0 |

    df_csv.count() // 100 samples per file

> res28: Long = 100

    display(sc.parallelize(Vector.fill(1000){myMixtureOf2Normals(1.0, 10.0, 0.99, r)}).toDF.select("_2")) // histogram of 1000 samples

| 1.63847575097573       |
|------------------------|
| 0.8497955378433464     |
| 1.0173381805959432     |
| -2.6960935205721848e-2 |
| -6.096818465288045e-2  |
| 0.6235321652739503     |
| 1.1594225593708558     |
| 2.6812781205628102     |
| 2.3144624015522943     |
| 3.2746230371718874     |
| 0.6239556140200029     |
| 0.6428284914761508     |
| -0.42618967795971496   |
| 0.4090774320731605     |
| 0.731226227370048      |
| 1.392728206581036      |
| 1.3354355936933495     |
| 0.17821385872329187    |
| -0.23317608061362294   |
| 0.47289802886431465    |
| -1.9401934414671596    |
| 10.214120281108658     |
| 1.892684662207417      |
| 1.0166947170672929     |
| 2.2709372842290798e-2  |
| 2.1067186310892803     |
| -0.2704224394550252    |
| 1.1899806078296409     |
| 1.9798405611441416     |
| 1.674277523545705      |

Truncated to 30 rows

