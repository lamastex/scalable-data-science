// Databricks notebook source exported at Tue, 28 Jun 2016 09:59:03 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by Dillon George, [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week10/036_IntroductionToMagellan.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week2/week10/036_IntroductionToMagellan](http://img.youtube.com/vi/0wKxVfeBQBc/0.jpg)](https://www.youtube.com/v/0wKxVfeBQBc?rel=0&autoplay=1&modestbranding=1&start=754&end=2056)

// COMMAND ----------

// MAGIC %md
// MAGIC # Introduction to Magellan for Scalable Geospatial Analytics
// MAGIC 
// MAGIC This is a minor  augmentation of Ram Harsha's Magellan code blogged here:
// MAGIC * [magellan geospatial analytics in spark](http://hortonworks.com/blog/magellan-geospatial-analytics-in-spark/)
// MAGIC 
// MAGIC First you need to attach the following two libraries:
// MAGIC * the magellan library 
// MAGIC   * Here we are using Spark 1.5.2 and the magellan library as a jar file that was built from [github magellan source](https://github.com/harsha2010/magellan).
// MAGIC     * we could not use the spark package: [http://spark-packages.org/package/harsha2010/magellan](http://spark-packages.org/package/harsha2010/magellan).
// MAGIC     * apparently magellan will be spark 2.0 ready in the future).
// MAGIC * and also the esri-geometry-api-1.2.1 library to databricks cluster.
// MAGIC 
// MAGIC See [using 3rd party libraries in databricks](https://databricks.com/blog/2015/07/28/using-3rd-party-libraries-in-databricks-spark-packages-and-maven-libraries.html) for help on attaching libraries in databricks.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Magellan-Spark as a Scalable Geospatial Analytics Engine 
// MAGIC 
// MAGIC HOMEWORK: Watch the [magellan presentation by Ram Harsha (Hortonworks) in Spark Summit East 2016](https://spark-summit.org/east-2016/events/magellan-spark-as-a-geospatial-analytics-engine/).
// MAGIC 
// MAGIC [![Ram harsha's Magellan Spark Summit East 2016 Talk]](http://img.youtube.com/vi/1lF1oSjxMT4/0.jpg)](https://www.youtube.com/watch?v=1lF1oSjxMT4)
// MAGIC 
// MAGIC   
// MAGIC Other resources for magellan:
// MAGIC * [Ram's blog in HortonWorks](http://hortonworks.com/blog/magellan-geospatial-analytics-in-spark/) and the [ZeppelinHub view of the demo code in video above](https://www.zeppelinhub.com/viewer/notebooks/aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL2hvcnRvbndvcmtzLWdhbGxlcnkvemVwcGVsaW4tbm90ZWJvb2tzL21hc3Rlci8yQjRUV0dDOE0vbm90ZS5qc29u)
// MAGIC * [Magellan as Spark project](http://spark-packages.org/package/harsha2010/magellan) and [Magellan github source](https://github.com/harsha2010/magellan)
// MAGIC * [shape files](https://en.wikipedia.org/wiki/Shapefile) developed by Environmental Systems Research Institute [(ESRI)](https://en.wikipedia.org/wiki/Esri). See ESRI's [what is a geospatial shape file?](https://www.esri.com/library/whitepapers/pdfs/shapefile.pdf)
// MAGIC * magellan builds on [http://esri.github.io/](http://esri.github.io/) a leading opensource geospatial library

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Do we need one more geospatial analytics library?
// MAGIC 
// MAGIC **(watch later 4 minutes and 10 seconds)**:
// MAGIC 
// MAGIC [![Spark Summit East 2016 - What is Geospatial Analytics by Ram Sri Harsha](http://img.youtube.com/vi/1lF1oSjxMT4/0.jpg)](https://www.youtube.com/v/1lF1oSjxMT4?rel=0&autoplay=1&modestbranding=1&start=318&end=568)
// MAGIC 
// MAGIC From [Ram's slide 4 of this Spark Summit East 2016 talk at slideshare](http://www.slideshare.net/SparkSummit/magellanspark-as-a-geospatial-analytics-engine-by-ram-sriharsha):
// MAGIC 
// MAGIC * Spatial Analytics at scale is challenging 
// MAGIC   * Simplicity + Scalability = Hard 
// MAGIC * Ancient Data Formats 
// MAGIC   * metadata, indexing not handled well, inefficient storage 
// MAGIC * Geospatial Analytics is not simply Business Intelligence anymore 
// MAGIC   * Statistical + Machine Learning being leveraged in geospatial 
// MAGIC * Now is the time to do it! 
// MAGIC   * Explosion of mobile data 
// MAGIC   * Finer granularity of data collection for geometries 
// MAGIC   * Analytics stretching the limits of traditional approaches 
// MAGIC   * Spark SQL + Catalyst + Tungsten makes extensible SQL engines easier than ever before! 

// COMMAND ----------

// MAGIC %md
// MAGIC ## Nuts and Bolts of Magellan

// COMMAND ----------

// MAGIC %md 
// MAGIC Let us first import what we will need for our geo-spatial analysis below.

// COMMAND ----------

// import statements are below NOTE: this magellan needs spark 1.5.1 and the jar was built from source and attached as a snapshot here
import magellan.{Point, Polygon, PolyLine}
import magellan.coord.NAD83
import org.apache.spark.sql.magellan.MagellanContext
import org.apache.spark.sql.magellan.dsl.expressions._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._ // this is needed for sql functions like explode, etc.

import java.sql.Timestamp
import java.text.SimpleDateFormat

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Data Structure: Point

// COMMAND ----------

val points = sc.parallelize(Seq((-1.0, -1.0), (-1.0, 1.0), (1.0, -1.0)))
               .toDF("x", "y")
               .select(point($"x", $"y").as("point"))

// COMMAND ----------

points.show(false)

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Data Structure: Polygon

// COMMAND ----------

case class PolygonRecord(polygon: Polygon) // let's create a case class for magellan polygon

val ring = Array(new Point(1.0, 1.0), new Point(1.0, -1.0),
                 new Point(-1.0, -1.0), new Point(-1.0, 1.0),
                 new Point(1.0, 1.0))

// COMMAND ----------

// MAGIC %md 
// MAGIC The `val` `ring` above encode the square polygon given by \\([-1,1]^2\\).

// COMMAND ----------

// let's creat a data frame of polygons
val polygons = sc.parallelize(Seq(
    PolygonRecord(new Polygon(Array(0), ring))
  )).toDF()

// COMMAND ----------

polygons.show(false)

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Predicate: within

// COMMAND ----------

polygons.select(point(0.5, 0.5) within $"polygon").show(false) //.count() 

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Predicate: intersects

// COMMAND ----------

// MAGIC %md
// MAGIC The `join` leverages SparkSQL's Catalyst and Tungsten.

// COMMAND ----------

points.join(polygons).show(false)

// COMMAND ----------

points.join(polygons).where($"point" intersects $"polygon").show(false) // all these points intersect the polygon

// COMMAND ----------

// MAGIC %md
// MAGIC Let's add another point \\( (-2,-2)\\) that lies outside our \\([-1,1]^2\\) polygon.

// COMMAND ----------

val morePoints = sc.parallelize(Seq((-1.0, -1.0), (-1.0, 1.0), (1.0, -1.0), (-2.0,-2.0)))
               .toDF("x", "y")
               .select(point($"x", $"y").as("point"))

// COMMAND ----------

morePoints.join(polygons).show(false)

// COMMAND ----------

morePoints.join(polygons).where($"point" intersects $"polygon").show(false) // (-2,-2) is not in the polygon

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Uber Dataset for the Demo done by Ram Harsha in Europe Spark Summit 2015
// MAGIC 
// MAGIC First the datasets have to be loaded.  See the section below on **Downloading datasets and putting them in distributed file system** for doing this anew (This only needs to be done once if the data is persisted in the distributed file system).

// COMMAND ----------

// MAGIC %md 
// MAGIC After downloading the data, we expect to have the following files in distributed file system (dbfs):
// MAGIC 
// MAGIC * ```all.tsv``` is the file of all uber trajectories
// MAGIC * ```SFNbhd``` is the directory containing SF neighborhood shape files.

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/magellan/")) // display the contents of the dbfs directory "dbfs:/datasets/magellan/"

// COMMAND ----------

// MAGIC %md 
// MAGIC First five lines or rows of the uber data containing: tripID, timestamp, Lon, Lat

// COMMAND ----------

sc.textFile("dbfs:/datasets/magellan/all.tsv").take(5).foreach(println)

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/magellan/SFNbhd")) // legacy shape files

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Homework
// MAGIC 
// MAGIC First watch the more technical magellan presentation by Ram Sri Harsha (Hortonworks) in Spark Summit Europe 2015
// MAGIC 
// MAGIC [![Ram Sri Harsha's Magellan Spark Summit EU 2015 Talk]](http://img.youtube.com/vi/rP8H-xQTuM0/0.jpg)](https://www.youtube.com/watch?v=rP8H-xQTuM0)
// MAGIC   
// MAGIC 
// MAGIC Second, carefully repeat Ram's original analysis from the following blog as done below.
// MAGIC 
// MAGIC [Ram's blog in HortonWorks](http://hortonworks.com/blog/magellan-geospatial-analytics-in-spark/) and the [ZeppelinHub view of the demo code in video above](https://www.zeppelinhub.com/viewer/notebooks/aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL2hvcnRvbndvcmtzLWdhbGxlcnkvemVwcGVsaW4tbm90ZWJvb2tzL21hc3Rlci8yQjRUV0dDOE0vbm90ZS5qc29u)

// COMMAND ----------

case class UberRecord(tripId: String, timestamp: String, point: Point) // a case class for UberRecord 

// COMMAND ----------

val uber = sc.textFile("dbfs:/datasets/magellan/all.tsv")
              .map { line =>
                      val parts = line.split("\t" )
                      val tripId = parts(0)
                      val timestamp = parts(1)
                      val point = Point(parts(3).toDouble, parts(2).toDouble)
                      UberRecord(tripId, timestamp, point)
                    }.repartition(100)
                     .toDF()
                     .cache()

// COMMAND ----------

val uberRecordCount = uber.count() // how many Uber records?

// COMMAND ----------

// MAGIC %md 
// MAGIC So there are over a million ```UberRecord```s.

// COMMAND ----------

val neighborhoods = sqlContext.read.format("magellan")
                                   .load("dbfs:/datasets/magellan/SFNbhd/")
                                   .select($"polygon", $"metadata")
                                   .cache()

// COMMAND ----------

neighborhoods.count() // how many neighbourhoods in SF?

// COMMAND ----------

neighborhoods.printSchema

// COMMAND ----------

neighborhoods.take(2) // see the first two neighbourhoods

// COMMAND ----------

neighborhoods.select(explode($"metadata").as(Seq("k", "v"))).show(5,false)

// COMMAND ----------

// MAGIC %md
// MAGIC This join yields nothing. 
// MAGIC 
// MAGIC So what's going on?
// MAGIC 
// MAGIC Watch Ram's 2015 Spark Summit talk for details on geospatial formats and transformations.

// COMMAND ----------

neighborhoods
  .join(uber)
  .where($"point" within $"polygon")
  .select($"tripId", $"timestamp", explode($"metadata").as(Seq("k", "v")))
  .withColumnRenamed("v", "neighborhood")
  .drop("k")
  .show(5)


// COMMAND ----------

// MAGIC %md
// MAGIC Need the right `transformer` to transform the points into the right coordinate system of the shape files.

// COMMAND ----------

val transformer: Point => Point = (point: Point) => 
{
    val from = new NAD83(Map("zone" -> 403)).from()
    val p = point.transform(from)
    new Point(3.28084 * p.x, 3.28084 * p.y)
}

// add a new column in nad83 coordinates
val uberTransformed = uber
                      .withColumn("nad83", $"point".transform(transformer))
                      .cache()

// COMMAND ----------

// MAGIC %md
// MAGIC Let' try the join again after appropriate transformation of coordinate system.

// COMMAND ----------

val joined = neighborhoods
              .join(uberTransformed)
              .where($"nad83" within $"polygon")
              .select($"tripId", $"timestamp", explode($"metadata").as(Seq("k", "v")))
              .withColumnRenamed("v", "neighborhood")
              .drop("k")
              .cache()

// COMMAND ----------

joined.show(5,false)

// COMMAND ----------

val UberRecordsInNbhdsCount = joined.count() // about 131 seconds for first action (doing broadcast hash join)

// COMMAND ----------

uberRecordCount - UberRecordsInNbhdsCount // records not in the neighbouthood shape files

// COMMAND ----------

joined
  .groupBy($"neighborhood")
  .agg(countDistinct("tripId")
  .as("trips"))
  .orderBy(col("trips").desc)
  .show(5,false)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Spatio-temporal Queries 
// MAGIC 
// MAGIC can be expressed in SQL using the Boolean predicates such as, \\(\in , \cap, \ldots \\), that operate over space-time sets given products of 2D magellan objects and 1D time intervals.
// MAGIC 
// MAGIC Want to scalably do the following:
// MAGIC * Given :
// MAGIC   * a set of trajectories as labelled points in space-time and 
// MAGIC   * a product of a time interval [ts,te] and a polygon P
// MAGIC * Find all labelled space-time points that satisfy the following relations:
// MAGIC     * intersect with [ts,te] X P
// MAGIC     * the start-time of the ride or the end time of the ride intersects with [ts,te] X P
// MAGIC     * intersect within a given distance d of any point or a given point in P (optional)
// MAGIC     
// MAGIC This will allow us to answer questions like:
// MAGIC * Where did the passengers who were using Uber and present in the SoMa neighbourhood in a given time interval get off?

// COMMAND ----------

// MAGIC %md
// MAGIC ## Other spatial Algorithms in Spark are being explored for generic and more efficient scalable geospatial analytic tasks
// MAGIC See the Spark Summit East 2016 Talk by Ram on "what next?"
// MAGIC 
// MAGIC * [SpatialSpark](http://spark-packages.org/package/syoummer/SpatialSpark) aims to provide efficient spatial operations using Apache Spark.
// MAGIC   * Spatial Partition 
// MAGIC       * Generate a spatial partition from input dataset, currently Fixed-Grid Partition (FGP), Binary-Split Partition (BSP) and Sort-Tile Partition (STP) are supported. 
// MAGIC   * Spatial Range Query
// MAGIC       *  includes both indexed and non-indexed query (useful for neighbourhood searches)
// MAGIC * [z-order Knn join](https://github.com/anantasty/SparkAlgorithms/tree/master/mllib/src/main/scala/org/sparkalgos/mllib/join)
// MAGIC   * A space-filling curve trick to index multi-dimensional metric data into 1 Dimension. See: [ieee paper](http://ieeexplore.ieee.org.ezproxy.canterbury.ac.nz/stamp/stamp.jsp?tp=&arnumber=5447837) and the [slides](http://www.slideshare.net/AshutoshTrivedi3/spark-algorithms).
// MAGIC   
// MAGIC * AkNN = All K Nearest Neighbours - identify the k nearesy neighbours for all nodes **simultaneously** (cont AkNN is the streaming form of AkNN)
// MAGIC   * need to identify the right resources to do this scalably.
// MAGIC * spark-knn-graphs: [https://github.com/tdebatty/spark-knn-graphs](https://github.com/tdebatty/spark-knn-graphs)
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md 
// MAGIC # Downloading datasets and putting them in distributed file system

// COMMAND ----------

// MAGIC %md
// MAGIC ## getting uber data 
// MAGIC ### (This only needs to be done once per shard!)

// COMMAND ----------

// MAGIC %sh ls

// COMMAND ----------

// MAGIC %sh
// MAGIC wget https://raw.githubusercontent.com/dima42/uber-gps-analysis/master/gpsdata/all.tsv

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/magellan") //need not be done again!

// COMMAND ----------

dbutils.fs.cp("file:/databricks/driver/all.tsv", "dbfs:/datasets/magellan/") 

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Getting SF Neighborhood Data

// COMMAND ----------

// MAGIC %sh
// MAGIC wget http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/2016/datasets/magellan/UberSF/planning_neighborhoods.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip planning_neighborhoods.zip

// COMMAND ----------

// MAGIC %sh 
// MAGIC mv planning_neighborhoods.zip orig_planning_neighborhoods.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC mkdir SFNbhd && mv planning_nei* SFNbhd && ls 
// MAGIC ls SFNbhd

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/magellan/SFNbhd") //need not be done again!

// COMMAND ----------

dbutils.fs.cp("file:/databricks/driver/SFNbhd/planning_neighborhoods.dbf", "dbfs:/datasets/magellan/SFNbhd/")
dbutils.fs.cp("file:/databricks/driver/SFNbhd/planning_neighborhoods.prj", "dbfs:/datasets/magellan/SFNbhd/")
dbutils.fs.cp("file:/databricks/driver/SFNbhd/planning_neighborhoods.sbn", "dbfs:/datasets/magellan/SFNbhd/")
dbutils.fs.cp("file:/databricks/driver/SFNbhd/planning_neighborhoods.sbx", "dbfs:/datasets/magellan/SFNbhd/")
dbutils.fs.cp("file:/databricks/driver/SFNbhd/planning_neighborhoods.shp", "dbfs:/datasets/magellan/SFNbhd/")
dbutils.fs.cp("file:/databricks/driver/SFNbhd/planning_neighborhoods.shp.xml", "dbfs:/datasets/magellan/SFNbhd/")
dbutils.fs.cp("file:/databricks/driver/SFNbhd/planning_neighborhoods.shx", "dbfs:/datasets/magellan/SFNbhd/")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/magellan/SFNbhd/"))

// COMMAND ----------

// MAGIC %md 
// MAGIC ### End of downloading and putting data in dbfs

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by Dillon George, [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)