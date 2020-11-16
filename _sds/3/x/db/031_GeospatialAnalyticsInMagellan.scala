// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC # Note for Spark 2.4.5
// MAGIC 
// MAGIC The current (2020-11-16) latest maven coordinates for Magellan do not work for Spark 2.4+ and unsupported yet for spark 3.0+:
// MAGIC 
// MAGIC - [https://github.com/harsha2010/magellan](https://github.com/harsha2010/magellan)
// MAGIC 
// MAGIC Use the binary jar from [https://github.com/lamastex/scalable-data-science/tree/master/custom-builds/jars/magellan/forks](https://github.com/lamastex/scalable-data-science/tree/master/custom-builds/jars/magellan/forks) on Databricks Runtime 6.6, Apache Spark 2.4.5, Scala 2.11 cluster.
// MAGIC 
// MAGIC ## Instructions ##
// MAGIC 
// MAGIC 1. Download [https://github.com/lamastex/scalable-data-science/raw/master/custom-builds/jars/magellan/forks/magellan-assembly-1.0.7-SNAPSHOT.jar](https://github.com/lamastex/scalable-data-science/raw/master/custom-builds/jars/magellan/forks/magellan-assembly-1.0.7-SNAPSHOT.jar) to your loacl machine.
// MAGIC 3. In Databricks choose *Create -> Library* and upload the packaged jar.
// MAGIC 4. Create a spark 2.4.5 Scala 2.11 cluster with the uploaded Magellan library installed or if you are already running a cluster and installed the uploaded library to it you have to detach and re-attach any notebook currently using that cluster.
// MAGIC 
// MAGIC 
// MAGIC NOTE: The magellan library's usual maven coordinates `harsha2010:magellan:1.0.6-s_2.11` may be outdated, but it is here for your future reference. You can follow instructions here to assemble the master jar if needed:
// MAGIC    * https://github.com/lamastex/scalable-data-science/raw/master/custom-builds/jars/magellan/master

// COMMAND ----------

// MAGIC %md
// MAGIC # What is Geospatial Analytics?
// MAGIC 
// MAGIC 
// MAGIC **(watch  3 minutes and 23 seconds: 111-314 seconds)**:
// MAGIC 
// MAGIC [![Spark Summit East 2016 - What is Geospatial Analytics by Ram Sri Harsha](http://img.youtube.com/vi/1lF1oSjxMT4/0.jpg)](https://www.youtube.com/watch?v=1lF1oSjxMT4?rel=0&autoplay=1&modestbranding=1&start=111&end=314)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Some Concrete Examples of Scalable Geospatial Analytics
// MAGIC 
// MAGIC ### Let us check out cross-domain data fusion in MSR's Urban Computing Group 
// MAGIC * lots of interesting papers to read at [http://research.microsoft.com/en-us/projects/urbancomputing/](http://research.microsoft.com/en-us/projects/urbancomputing/).
// MAGIC 
// MAGIC ## Several sciences are naturally geospatial 
// MAGIC * forestry, 
// MAGIC * geography, 
// MAGIC * geology, 
// MAGIC * seismology, 
// MAGIC * ecology,
// MAGIC * etc. etc.
// MAGIC 
// MAGIC See for example the global EQ datastreams from US geological Service below.
// MAGIC 
// MAGIC For a global data source, see US geological Service's Earthquake hazards Program ["http://earthquake.usgs.gov/data/](http://earthquake.usgs.gov/data/).

// COMMAND ----------

// MAGIC %md
// MAGIC REDO
// MAGIC 
// MAGIC https://magellan.ghost.io/how-does-magellan-scale-geospatial-queries/
// MAGIC 
// MAGIC # Introduction to Magellan for Scalable Geospatial Analytics
// MAGIC 
// MAGIC This is a minor  augmentation of Ram Harsha's Magellan code blogged here:
// MAGIC * [magellan geospatial analytics in spark](https://magellan.ghost.io/welcome-to-ghost/)

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
displayHTML(frameIt("https://magellan.ghost.io/how-does-magellan-scale-geospatial-queries/", 550))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Do we need one more geospatial analytics library?
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
// MAGIC 
// MAGIC This is an expansion oof of the following databricks notebook:
// MAGIC * [https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/137058993011870/882779309834027/6891974485343070/latest.html](https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/137058993011870/882779309834027/6891974485343070/latest.html) 
// MAGIC 
// MAGIC and look at the magellan README in github:
// MAGIC * [https://github.com/harsha2010/magellan](https://github.com/harsha2010/magellan)
// MAGIC 
// MAGIC **HOMEWORK**: Watch the [magellan presentation by Ram Harsha (Hortonworks) in Spark Summit East 2016](https://spark-summit.org/east-2016/events/magellan-spark-as-a-geospatial-analytics-engine/).
// MAGIC 
// MAGIC   
// MAGIC Other resources for magellan:
// MAGIC * [Ram's blog in HortonWorks](http://hortonworks.com/blog/magellan-geospatial-analytics-in-spark/) and the [ZeppelinHub view of the demo code in video above](https://www.zeppelinhub.com/viewer/notebooks/aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL2hvcnRvbndvcmtzLWdhbGxlcnkvemVwcGVsaW4tbm90ZWJvb2tzL21hc3Rlci8yQjRUV0dDOE0vbm90ZS5qc29u)
// MAGIC * [Magellan as Spark project](http://spark-packages.org/package/harsha2010/magellan) and [Magellan github source](https://github.com/harsha2010/magellan)
// MAGIC * [shape files](https://en.wikipedia.org/wiki/Shapefile) developed by Environmental Systems Research Institute [(ESRI)](https://en.wikipedia.org/wiki/Esri). See ESRI's [what is a geospatial shape file?](https://www.esri.com/library/whitepapers/pdfs/shapefile.pdf)
// MAGIC * magellan builds on [http://esri.github.io/](http://esri.github.io/) a leading opensource geospatial library

// COMMAND ----------

// MAGIC %md
// MAGIC Let's get our hands dirty with basics in magellan.
// MAGIC 
// MAGIC ### Spatial Data Structures
// MAGIC 
// MAGIC * Points
// MAGIC * Polygons
// MAGIC * lines
// MAGIC * Polylines
// MAGIC 
// MAGIC ### Users' View of Spatial Data Structures (details are typically "invisible" to user)
// MAGIC 
// MAGIC <img src="https://raw.githubusercontent.com/NEONScience/NEON-Data-Skills/dev-aten/graphics/vector-general/pnt_line_poly.png" width=555>
// MAGIC 
// MAGIC 
// MAGIC ### Predicates
// MAGIC 
// MAGIC * within
// MAGIC * intersects

// COMMAND ----------

// create a points DataFrame
val points = sc.parallelize(Seq((-1.0, -1.0), (-1.0, 1.0), (1.0, -1.0))).toDF("x", "y")

// COMMAND ----------

// transform (lat,lon) into Point using custom user-defined function
import magellan.Point // just Point
import org.apache.spark.sql.functions.udf
val toPointUDF = udf{(x:Double,y:Double) => Point(x,y) }

// COMMAND ----------

// let's show the results of the DF with a new column called point
points.withColumn("point", toPointUDF($"x", $"y")).show()

// COMMAND ----------

// let's show the results of the DF with a new column called point
// slicker with ' instead of $"" as follows for column names
points.withColumn("point", toPointUDF('x, 'y)).show()

// COMMAND ----------

points.show

// COMMAND ----------

// Let's instead use the built-in expression to do the same - it's much faster on larger DataFrames due to code-gen
import org.apache.spark.sql.magellan.dsl.expressions._
val points = sc.parallelize(Seq((-1.0, -1.0), (-1.0, 1.0), (1.0, -1.0))).toDF("x", "y").select(point($"x", $"y").as("point"))

points.show()

// COMMAND ----------

display(points) // busted in bleeding-edge magellan we need for computing

// COMMAND ----------

points.show()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC The latest version of magellan seems to have issues with the databricks `display` function. We will ignore this convenience of display and continue with our analysis.
// MAGIC 
// MAGIC This is a databricks display of magellan points when it is working properly in Spark 2.2.
// MAGIC 
// MAGIC <img src="https://raw.githubusercontent.com/lamastex/scalable-data-science/master/_360-in-525/2018/03/images/databricksDisplayOfMagellanPoints.png">

// COMMAND ----------

// MAGIC %md
// MAGIC Let's verify empirically if it is indeed faster for larger DataFrames.

// COMMAND ----------

// to generate a sequence of pairs of random numbers we can do:
import util.Random.nextDouble
Seq.fill(10)((-1.0*nextDouble,+1.0*nextDouble))

// COMMAND ----------

// using the UDF method with 1 million points we can do a count action of the DF with point column
// don't add too many zeros as it may crash your driver program
sc.parallelize(Seq.fill(10000000)((-1.0*nextDouble,+1.0*nextDouble)))
  .toDF("x", "y")
  .withColumn("point", toPointUDF('x, 'y))
  .count()

// COMMAND ----------

// it should be twice as fast with code-gen especially when we are ingesting from dbfs as opposed to 
// using Seq.fill in the driver...
sc.parallelize(Seq.fill(10000000)((-1.0*nextDouble,+1.0*nextDouble)))
  .toDF("x", "y")
  .withColumn("point", point('x, 'y))
  .count()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC See https://databricks.com/blog/2015/04/13/deep-dive-into-spark-sqls-catalyst-optimizer.html

// COMMAND ----------

// MAGIC %md
// MAGIC Read the following for more on catalyst optimizer and whole-stage code generation.
// MAGIC 
// MAGIC * [https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-whole-stage-codegen.html](https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-whole-stage-codegen.html)
// MAGIC * [https://databricks.com/blog/2015/04/13/deep-dive-into-spark-sqls-catalyst-optimizer.html](https://databricks.com/blog/2015/04/13/deep-dive-into-spark-sqls-catalyst-optimizer.html)
// MAGIC * [https://databricks.com/blog/2016/05/23/apache-spark-as-a-compiler-joining-a-billion-rows-per-second-on-a-laptop.html](https://databricks.com/blog/2016/05/23/apache-spark-as-a-compiler-joining-a-billion-rows-per-second-on-a-laptop.html)
// MAGIC 
// MAGIC Try bench-marks here:
// MAGIC * [https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/6122906529858466/293651311471490/5382278320999420/latest.html](https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/6122906529858466/293651311471490/5382278320999420/latest.html)

// COMMAND ----------

// Create a Polygon DataFrame
import magellan.Polygon

case class PolygonExample(polygon: Polygon)

// do this in your head / pencil-paper / black-board going counter-clockwise
val ring = Array(Point(1.0, 1.0), Point(1.0, -1.0), Point(-1.0, -1.0), Point(-1.0, 1.0), Point(1.0, 1.0))
val polygon = Polygon(Array(0), ring)

val polygons = sc.parallelize(Seq(
  PolygonExample(Polygon(Array(0), ring))
  //Polygon(Array(0), ring)
)).toDF()


// COMMAND ----------

polygons.show(false)

// COMMAND ----------

display(polygons) // not much can be seen as its in the object

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC This is a databricks display of magellan polygon when it is working properly in Spark 2.2 on another databricks run-time.
// MAGIC 
// MAGIC <img src="https://raw.githubusercontent.com/lamastex/scalable-data-science/master/_360-in-525/2018/03/images/databricksDisplayOfMagellanPolygon.png">

// COMMAND ----------

// MAGIC %md
// MAGIC # Predicates

// COMMAND ----------

import org.apache.spark.sql.types._


// COMMAND ----------

// join points with polygons upon intersection
points.join(polygons)
      .where($"point" intersects $"polygon")
      .count()

// COMMAND ----------

points.show()

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **Pop Quiz:**
// MAGIC 
// MAGIC What are the three points intersect the polygon?

// COMMAND ----------

// MAGIC %md
// MAGIC More generally we can have more complex queries as the generic polygon need not even be a [convex set](https://en.wikipedia.org/wiki/Convex_set).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Convex_set",400))

// COMMAND ----------

// MAGIC %md
// MAGIC This is not an uncommon polygon - think of shapes of parks or lakes on a map.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC A bounding box for a non-covex polygon
// MAGIC 
// MAGIC <img src="https://magellan.ghost.io/content/images/2017/08/boundingbox-2.png">

// COMMAND ----------

// MAGIC %md
// MAGIC Let us consider our simple `points` and `polygons` we just made and consider the following `points within polygon` join query.

// COMMAND ----------

// join points with polygons upon within or containment
points.join(polygons)
      .where($"point" within $"polygon")
      .count()

// COMMAND ----------

// MAGIC %md
// MAGIC ## Line

// COMMAND ----------

//creating line from two points
import magellan.Line

case class LineExample(line: Line)

val line = Line(Point(1.0, 1.0), Point(1.0, -1.0))

val lines = sc.parallelize(Seq(
      LineExample(line)
    )).toDF()

lines.show(false)

// COMMAND ----------

display(lines)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC This is a databricks display of magellan lines when it is working properly!
// MAGIC 
// MAGIC <img src="https://raw.githubusercontent.com/lamastex/scalable-data-science/master/_360-in-525/2018/03/images/databricksDisplayOfMagellanLines.png">

// COMMAND ----------

// MAGIC %md
// MAGIC ## PolyLine

// COMMAND ----------

// creating polyline
import magellan.PolyLine

case class PolyLineExample(polyline: PolyLine)

val ring = Array(Point(1.0, 1.0), Point(1.0, -1.0),
      Point(-1.0, -1.0), Point(-1.0, 1.0))

val polylines = sc.parallelize(Seq(
      PolyLineExample(PolyLine(Array(0), ring))
    )).toDF()

// COMMAND ----------

polylines.show(false)

// COMMAND ----------

// MAGIC %md
// MAGIC This is a databricks display of magellan polyline when it is working properly!
// MAGIC 
// MAGIC <img src="https://raw.githubusercontent.com/lamastex/scalable-data-science/master/_360-in-525/2018/03/images/databricksDisplayOfMagellanPolylines.png">

// COMMAND ----------

// now let's make a polyline with two or more lines out of the same ring
val polylines2 = sc.parallelize(Seq(
  PolyLineExample(PolyLine(Array(0,2), ring)) // first line starts at index 0 and second one starts at index 2
)).toDF()

polylines2.show(false)

// COMMAND ----------

// MAGIC %md
// MAGIC ## You can do a bit with magellan and esri under its hood

// COMMAND ----------

import magellan.Point

val p = Point(1.0, -1.0)

// COMMAND ----------

//p. // uncomment line and put the cursor next to the . and hit TAB to see available methods on the magellan Point p

// COMMAND ----------

(p.getX, p.getY) // for example we can getX and getY values of the Point p

// COMMAND ----------

val pc = Point(0.0,0.0)
p.withinCircle(pc, 5.0) // check if Point p iswith circle of radius 5.0 around Point pc

// COMMAND ----------

p.boundingBox // find the bounding box of p

// COMMAND ----------

import magellan.Point

// create a radius 0.5 buffered polygon about the centre given by Point(0.0, 1.0)
val aBufferedPolygon = Point(0.0, 1.0).buffer(0.5) 

// this used to fail, now it should work
magellan.esri.ESRIUtil.toESRIGeometry(aBufferedPolygon)

println(aBufferedPolygon)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Dive here for more on magellan Point:
// MAGIC 
// MAGIC - https://github.com/harsha2010/magellan/blob/master/src/main/scala/magellan/Point.scala
// MAGIC 
// MAGIC Knock yourself out on other Data Structures in the source.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Uber Trajectories in San Francisco
// MAGIC 
// MAGIC #### Dataset for the Demo done by Ram Sri Harsha in Europe Spark Summit 2015
// MAGIC 
// MAGIC **First** the datasets have to be loaded into distributed file store.  
// MAGIC - See **Step 0: Downloading datasets and loading into dbfs** below for doing this anew (This only needs to be done once if the data is persisted in the distributed file system).

// COMMAND ----------

// MAGIC %md 
// MAGIC After downloading the data, we expect to have the following files in distributed file system (dbfs):
// MAGIC 
// MAGIC * ```all.tsv``` is the file of all uber trajectories
// MAGIC * ```SFNbhd``` is the directory containing SF neighborhood shape files.

// COMMAND ----------

// display the contents of the dbfs directory "dbfs:/datasets/magellan/"
// - if you don't see files here then go to Step 0 below as explained above!
display(dbutils.fs.ls("dbfs:/datasets/magellan/")) 

// COMMAND ----------

// MAGIC %sh
// MAGIC ls /dbfs/datasets

// COMMAND ----------

// MAGIC %md 
// MAGIC First five lines or rows of the uber data containing: tripID, timestamp, Lon, Lat

// COMMAND ----------

sc.textFile("dbfs:/datasets/magellan/all.tsv").take(5).foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC The neighborhood shape files for Sanfrancisco will form the polygons of interest to us.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Shapefile", 400))

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/magellan/SFNbhd")) // legacy shape files - used in various sectors

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Homework
// MAGIC 
// MAGIC First watch the more technical magellan presentation by Ram Sri Harsha (Hortonworks) in Spark Summit Europe 2015
// MAGIC 
// MAGIC [![Ram Sri Harsha's Magellan Spark Summit EU 2015 Talk]](http://img.youtube.com/vi/rP8H-xQTuM0/0.jpg)](https://www.youtube.com/watch?v=rP8H-xQTuM0)
// MAGIC   
// MAGIC 
// MAGIC Let's repeat Ram's original analysis from the following blog as done below.
// MAGIC 
// MAGIC [Ram's blog in HortonWorks](http://hortonworks.com/blog/magellan-geospatial-analytics-in-spark/).

// COMMAND ----------

// MAGIC %md
// MAGIC This is just to get you started... You may need to moidfy this!

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
                    }
                     //.repartition(100) // using default repartition
                     .toDF()
                     .cache()

// COMMAND ----------

val uberRecordCount = uber.count() // how many Uber records?

// COMMAND ----------

val uberRecordCount = uber.count() // time for cched count

// COMMAND ----------

// MAGIC %md 
// MAGIC So there are over a million ```UberRecord```s.

// COMMAND ----------

sqlContext.read.format("magellan").load("dbfs:/datasets/magellan/SFNbhd/").printSchema()

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

neighborhoods.show(2,false) // see the first two neighbourhoods

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **You Try:**
// MAGIC 
// MAGIC Modify the next cell to see all 37 neighborhoods.

// COMMAND ----------

neighborhoods.show(37,false) // modify this cell to see all 37 neighborhoods

// COMMAND ----------

import org.apache.spark.sql.functions._ // this is needed for sql functions like explode, etc.

// COMMAND ----------

//names of all 37 neighborhoods of San Francisco
neighborhoods.select(explode($"metadata").as(Seq("k", "v"))).show(37,false)

// COMMAND ----------

// MAGIC %md
// MAGIC This join below yields nothing. 
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

displayHTML(frameIt("https://en.wikipedia.org/wiki/Geographic_coordinate_system",400))

// COMMAND ----------

// MAGIC %md
// MAGIC Need the right `transformer` to transform the points into the right coordinate system of the shape files.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/North_American_Datum#North_American_Datum_of_1983",400))

// COMMAND ----------

// DBTITLE 1,Let's roll out custom transformers between coordinate systems - Play!
// This code was removed from magellan in this commit:
// https://github.com/harsha2010/magellan/commit/8df0a62560116f8ed787fc7e86f190f8e2730826
// We bring this back to show how to roll our own transformations.
// EXERCISE: find existing transformers / methods in magellan or esri to go between coordinate systems 
import magellan.Point

class NAD83(params: Map[String, Any]) {
  val RAD = 180d / Math.PI
  val ER  = 6378137.toDouble  // semi-major axis for GRS-80
  val RF  = 298.257222101  // reciprocal flattening for GRS-80
  val F   = 1.toDouble / RF  // flattening for GRS-80
  val ESQ = F + F - (F * F)
  val E   = StrictMath.sqrt(ESQ)

  private val ZONES =  Map(
    401 -> Array(122.toDouble, 2000000.0001016,
      500000.0001016001, 40.0,
      41.66666666666667, 39.33333333333333),
    403 -> Array(120.5, 2000000.0001016,
      500000.0001016001, 37.06666666666667,
      38.43333333333333, 36.5)
  )

  def from() = {
    val zone = params("zone").asInstanceOf[Int]
    ZONES.get(zone) match {
      case Some(x) => if (x.length == 5) {
        toTransverseMercator(x)
      } else {
        toLambertConic(x)
      }
      case None => ???
    }
  }

  def to() = {
    val zone = params("zone").asInstanceOf[Int]
    ZONES.get(zone) match {
      case Some(x) => if (x.length == 5) {
        fromTransverseMercator(x)
      } else {
        fromLambertConic(x)
      }
      case None => ???
    }
  }

  def qqq(e: Double, s: Double) = {
    (StrictMath.log((1 + s) / (1 - s)) - e *
      StrictMath.log((1 + e * s) / (1 - e * s))) / 2
  }

  def toLambertConic(params: Array[Double]) = {
    val cm = params(0) / RAD  // CENTRAL MERIDIAN (CM)
    val eo = params(1)  // FALSE EASTING VALUE AT THE CM (METERS)
    val nb = params(2)  // FALSE NORTHING VALUE AT SOUTHERMOST PARALLEL (METERS), (USUALLY ZERO)
    val fis = params(3) / RAD  // LATITUDE OF SO. STD. PARALLEL
    val fin = params(4) / RAD  // LATITUDE OF NO. STD. PARALLEL
    val fib = params(5) / RAD // LATITUDE OF SOUTHERNMOST PARALLEL
    val sinfs = StrictMath.sin(fis)
    val cosfs = StrictMath.cos(fis)
    val sinfn = StrictMath.sin(fin)
    val cosfn = StrictMath.cos(fin)
    val sinfb = StrictMath.sin(fib)
    val qs = qqq(E, sinfs)
    val qn = qqq(E, sinfn)
    val qb = qqq(E, sinfb)
    val w1 = StrictMath.sqrt(1.toDouble - ESQ * sinfs * sinfs)
    val w2 = StrictMath.sqrt(1.toDouble - ESQ * sinfn * sinfn)
    val sinfo = StrictMath.log(w2 * cosfs / (w1 * cosfn)) / (qn - qs)
    val k = ER * cosfs * StrictMath.exp(qs * sinfo) / (w1 * sinfo)
    val rb = k / StrictMath.exp(qb * sinfo)

    (point: Point) => {
      val (long, lat) = (point.getX(), point.getY())
      val l = - long / RAD
      val f = lat / RAD
      val q = qqq(E, StrictMath.sin(f))
      val r = k / StrictMath.exp(q * sinfo)
      val gam = (cm - l) * sinfo
      val n = rb + nb - (r * StrictMath.cos(gam))
      val e = eo + (r * StrictMath.sin(gam))
      Point(e, n)
    }
  }

  def toTransverseMercator(params: Array[Double]) = {
    (point: Point) => {
      point
    }
  }

  def fromLambertConic(params: Array[Double]) = {
    val cm = params(0) / RAD  // CENTRAL MERIDIAN (CM)
    val eo = params(1)  // FALSE EASTING VALUE AT THE CM (METERS)
    val nb = params(2)  // FALSE NORTHING VALUE AT SOUTHERMOST PARALLEL (METERS), (USUALLY ZERO)
    val fis = params(3) / RAD  // LATITUDE OF SO. STD. PARALLEL
    val fin = params(4) / RAD  // LATITUDE OF NO. STD. PARALLEL
    val fib = params(5) / RAD // LATITUDE OF SOUTHERNMOST PARALLEL
    val sinfs = StrictMath.sin(fis)
    val cosfs = StrictMath.cos(fis)
    val sinfn = StrictMath.sin(fin)
    val cosfn = StrictMath.cos(fin)
    val sinfb = StrictMath.sin(fib)

    val qs = qqq(E, sinfs)
    val qn = qqq(E, sinfn)
    val qb = qqq(E, sinfb)
    val w1 = StrictMath.sqrt(1.toDouble - ESQ * sinfs * sinfs)
    val w2 = StrictMath.sqrt(1.toDouble - ESQ * sinfn * sinfn)
    val sinfo = StrictMath.log(w2 * cosfs / (w1 * cosfn)) / (qn - qs)
    val k = ER * cosfs * StrictMath.exp(qs * sinfo) / (w1 * sinfo)
    val rb = k / StrictMath.exp(qb * sinfo)
    (point: Point) => {
      val easting = point.getX()
      val northing = point.getY()
      val npr = rb - northing + nb
      val epr = easting - eo
      val gam = StrictMath.atan(epr / npr)
      val lon = cm - (gam / sinfo)
      val rpt = StrictMath.sqrt(npr * npr + epr * epr)
      val q = StrictMath.log(k / rpt) / sinfo
      val temp = StrictMath.exp(q + q)
      var sine = (temp - 1.toDouble) / (temp + 1.toDouble)
      var f1, f2 = 0.0
      for (i <- 0 until 2) {
        f1 = ((StrictMath.log((1.toDouble + sine) / (1.toDouble - sine)) - E *
          StrictMath.log((1.toDouble + E * sine) / (1.toDouble - E * sine))) / 2.toDouble) - q
        f2 = 1.toDouble / (1.toDouble - sine * sine) - ESQ / (1.toDouble - ESQ * sine * sine)
        sine -= (f1/ f2)
      }
      Point(StrictMath.toDegrees(lon) * -1, StrictMath.toDegrees(StrictMath.asin(sine)))
    }
  }

  def fromTransverseMercator(params: Array[Double]) = {
    val cm = params(0)  // CENTRAL MERIDIAN (CM)
    val fe = params(1)  // FALSE EASTING VALUE AT THE CM (METERS)
    val or = params(2) / RAD  // origin latitude
    val sf = 1.0 - (1.0 / params(3)) // scale factor
    val fn = params(4)  // false northing
    // translated from TCONPC subroutine
    val eps = ESQ / (1.0 - ESQ)
    val pr = (1.0 - F) * ER
    val en = (ER - pr) / (ER + pr)
    val en2 = en * en
    val en3 = en * en * en
    val en4 = en2 * en2

    var c2 = -3.0 * en / 2.0 + 9.0 * en3 / 16.0
    var c4 = 15.0d * en2 / 16.0d - 15.0d * en4 /32.0
    var c6 = -35.0 * en3 / 48.0
    var c8 = 315.0 * en4 / 512.0
    val u0 = 2.0 * (c2 - 2.0 * c4 + 3.0 * c6 - 4.0 * c8)
    val u2 = 8.0 * (c4 - 4.0 * c6 + 10.0 * c8)
    val u4 = 32.0 * (c6 - 6.0 * c8)
    val u6 = 129.0 * c8

    c2 = 3.0 * en / 2.0 - 27.0 * en3 / 32.0
    c4 = 21.0 * en2 / 16.0 - 55.0 * en4 / 32.0d
    c6 = 151.0 * en3 / 96.0
    c8 = 1097.0d * en4 / 512.0
    val v0 = 2.0 * (c2 - 2.0 * c4 + 3.0 * c6 - 4.0 * c8)
    val v2 = 8.0 * (c4 - 4.0 * c6 + 10.0 * c8)
    val v4 = 32.0 * (c6 - 6.0 * c8)
    val v6 = 128.0 * c8

    val r = ER * (1.0 - en) * (1.0 - en * en) * (1.0 + 2.25 * en * en + (225.0 / 64.0) * en4)
    val cosor = StrictMath.cos(or)
    val omo = or + StrictMath.sin(or) * cosor *
      (u0 + u2 * cosor * cosor + u4 * StrictMath.pow(cosor, 4) + u6 * StrictMath.pow(cosor, 6))
    val so = sf * r * omo

    (point: Point) => {
      val easting = point.getX()
      val northing = point.getY()
      // translated from TMGEOD subroutine
      val om = (northing - fn + so) / (r * sf)
      val cosom = StrictMath.cos(om)
      val foot = om + StrictMath.sin(om) * cosom *
        (v0 + v2 * cosom * cosom + v4 * StrictMath.pow(cosom, 4) + v6 * StrictMath.pow(cosom, 6))
      val sinf = StrictMath.sin(foot)
      val cosf = StrictMath.cos(foot)
      val tn = sinf / cosf
      val ts = tn * tn
      val ets = eps * cosf * cosf
      val rn = ER * sf / StrictMath.sqrt(1.0 - ESQ * sinf * sinf)
      val q = (easting - fe) / rn
      val qs = q * q
      val b2 = -tn * (1.0 + ets) / 2.0
      val b4 = -(5.0 + 3.0 * ts + ets * (1.0 - 9.0 * ts) - 4.0 * ets * ets) / 12.0
      val b6 = (61.0 + 45.0 * ts * (2.0 + ts) + ets * (46.0 - 252.0 * ts -60.0 * ts * ts)) / 360.0
      val b1 = 1.0
      val b3 = -(1.0 + ts + ts + ets) / 6.0
      val b5 = (5.0 + ts * (28.0 + 24.0 * ts) + ets * (6.0 + 8.0 * ts)) / 120.0
      val b7 = -(61.0 + 662.0 * ts + 1320.0 * ts * ts + 720.0 * StrictMath.pow(ts, 3)) / 5040.0
      val lat = foot + b2 * qs * (1.0 + qs * (b4 + b6 * qs))
      val l = b1 * q * (1.0 + qs * (b3 + qs * (b5 + b7 * qs)))
      val lon = -l / cosf + cm
      Point(StrictMath.toDegrees(lon) * -1, StrictMath.toDegrees(lat))
    }
  }
}

// COMMAND ----------

val transformer: Point => Point = (point: Point) => {
  val from = new NAD83(Map("zone" -> 403)).from()
  val p = point.transform(from)
  Point(3.28084 * p.getX, 3.28084 * p.getY)
}

// add a new column in nad83 coordinates
val uberTransformed = uber
                      .withColumn("nad83", $"point".transform(transformer))
                      .cache()

// COMMAND ----------

uberTransformed.count()

// COMMAND ----------

uberTransformed.show(5,false) // nad83 transformed points

// COMMAND ----------

uberTransformed.select("tripId").distinct().count() // number of unique tripIds

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

val UberRecordsInNbhdsCount = joined.count() // about 131 seconds for first action (doing broadcast hash join)

// COMMAND ----------

joined.explain

// COMMAND ----------

joined.show(5,false)

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



// COMMAND ----------

// MAGIC %md
// MAGIC ## Other spatial Algorithms in Spark are being explored for generic and more efficient scalable geospatial analytic tasks
// MAGIC 
// MAGIC 
// MAGIC Read for more spatial indexing structures.
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
// MAGIC # Step 0: Downloading datasets and load into dbfs
// MAGIC 
// MAGIC * get the Uber data
// MAGIC * get the San Francisco neighborhood data

// COMMAND ----------

// MAGIC %md
// MAGIC ## getting uber data 
// MAGIC ### (This only needs to be done once per shard!)

// COMMAND ----------

// MAGIC %sh ls

// COMMAND ----------

// MAGIC %sh
// MAGIC wget https://raw.githubusercontent.com/dima42/uber-gps-analysis/master/gpsdata/all.tsv
// MAGIC #wget http://lamastex.org/datasets/public/geospatial/uber/all.tsv

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/magellan") //need not be done again!

// COMMAND ----------

dbutils.fs.cp("file:/databricks/driver/all.tsv", "dbfs:/datasets/magellan/") // load into dbfs

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/magellan/"))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Getting SF Neighborhood Data

// COMMAND ----------

// MAGIC %sh
// MAGIC wget http://www.lamastex.org/courses/ScalableDataScience/2016/datasets/magellan/UberSF/planning_neighborhoods.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip planning_neighborhoods.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC ls -al

// COMMAND ----------

// MAGIC %sh 
// MAGIC mv planning_neighborhoods.zip orig_planning_neighborhoods.zip

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Let's prepare the files in a local directory named `SFNbhd`
// MAGIC 
// MAGIC - make a directory called `SFNbhd` using the command `mkdir SFNbhd`
// MAGIC - after making the directory specified by `&&` move the files starting with `planning_nei` in to the directory we made `SFNbhd` by:
// MAGIC   - `mv planning_nei* SFNbhd` 
// MAGIC - list the contents of the current directory using `ls`
// MAGIC - finally list the contents of the directory `SFNbhd` inside current directory using `ls -al SFNbhd`

// COMMAND ----------

// MAGIC %sh
// MAGIC mkdir SFNbhd && mv planning_nei* SFNbhd && ls 
// MAGIC ls -al SFNbhd

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/magellan/SFNbhd") //make the directory in dbfs - need not be done again!

// COMMAND ----------

// just copy each file - done for pedantic reasons; we can do more sophisticated dbfs loads for large shape files
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
// MAGIC ### End of Step 0: downloading and putting data in dbfs