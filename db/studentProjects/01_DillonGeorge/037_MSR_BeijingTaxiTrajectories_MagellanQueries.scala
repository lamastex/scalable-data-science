// Databricks notebook source exported at Tue, 14 Jun 2016 04:30:47 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by Dillon George and [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC # Scalable Spatio-Temporal Constraint Satisfaction Problem (ST-CSP)
// MAGIC 
// MAGIC Suppose you have base spatial sets in 2D space (points, lines, polygons, etc) given by: 
// MAGIC 
// MAGIC \\[\\{S\_i \\}\_{i=1}^m\\] 
// MAGIC 
// MAGIC and base temporal sets in 1D given by intervals in time:
// MAGIC \\[ \\{T\_j \\}\_{j=1}^n .\\] 
// MAGIC 
// MAGIC Then one can obtain space-time set in 3D as unions of Cartesian products of such base spatial and temporal sets.
// MAGIC 
// MAGIC A simple constraint satisfaction problem can be posed as a Boolean-valued statement involving such space-time sets.
// MAGIC 
// MAGIC Often, the first seps that a geospatial scientist or analyst needs to take in order to build intuition about the phenomenon of interest invovles such CSP problems.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Microsoft Research's Beijing Taxicab Trajectories using Magellan
// MAGIC 
// MAGIC More details on the dataset and related papers are available [here](http://research.microsoft.com/apps/pubs/?id=152883).

// COMMAND ----------

def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/ml-features.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("http://research.microsoft.com/apps/pubs/?id=152883",700))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Steps in this notebook
// MAGIC 
// MAGIC * 1. Download the taxi trips in Beiging
// MAGIC * 2. Turn into Dataframe
// MAGIC * 3. Define generic functions for CSP problem

// COMMAND ----------

import java.net.URL
import java.io.File
import org.apache.commons.io.FileUtils

import com.cotdp.hadoop.ZipFileInputFormat
import org.apache.hadoop.io.BytesWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job

// COMMAND ----------

// MAGIC %md 
// MAGIC ### 1. Download the Taxi Trips

// COMMAND ----------

// MAGIC %md ####Load directly into spark
// MAGIC See **[this post](https://sainudiin-canterbury-research.cloud.databricks.com/#externalnotebook/https%3A%2F%2Fdocs.cloud.databricks.com%2Fdocs%2Flatest%2Fdatabricks_guide%2Findex.html%2303%2520Accessing%2520Data%2F3%2520Common%2520File%2520Formats%2F6%2520Zip%2520Files%2520-%2520scala.html) for how to do so**

// COMMAND ----------

val numZipFiles = 14

// COMMAND ----------

val zipURLs = (1 until numZipFiles + 1).map(i => new URL(f"http://research.microsoft.com/pubs/152883/0$i.zip"))

// COMMAND ----------

// MAGIC %md Load all these URLS into the distributed filesystem

// COMMAND ----------

val localZipFiles = (1 until numZipFiles+1).map(i => new File(f"/home/ubuntu/databricks/driver/drive0$i.zip"))

// COMMAND ----------

// MAGIC %md Download the files and copy them to the appropriate locations

// COMMAND ----------

val locations = (zipURLs, localZipFiles).zipped.par.toList

locations.par.foreach(location => location match {
  case (url, file) => {
    println("Doing: ", url)
    FileUtils.copyURLToFile(url, file)  // This takes a long time, download in parallel not serial? Just wget it? 
  }
  case _ => Unit
})

// COMMAND ----------

// MAGIC %md ###Load these zipfiles into DBFS

// COMMAND ----------

(1 until numZipFiles+1).foreach(i => dbutils.fs.mv(f"file:/home/ubuntu/databricks/driver/drive0$i.zip", f"dbfs:/home/ubuntu/databricks/driver/t-drive/drive0$i.zip"))

// COMMAND ----------

// MAGIC %fs ls "dbfs:/home/ubuntu/databricks/driver/t-drive/"

// COMMAND ----------

// MAGIC %md ###Now turn these zip files into RDDs

// COMMAND ----------

// MAGIC %md ###Turn into a (K, V) RDD
// MAGIC The Key is the name of the file,  Value is that files contents.

// COMMAND ----------

val zipFileRDDs = (1 until numZipFiles+1).map(i => sc.newAPIHadoopFile(
        f"dbfs:/home/ubuntu/databricks/driver/t-drive/drive0$i.zip",
        classOf[ZipFileInputFormat],
        classOf[Text],
        classOf[BytesWritable],
        new Job().getConfiguration()))

val zipFileRDD = sc.union(zipFileRDDs)

// COMMAND ----------

 zipFileRDD.map(s => s._1.toString()).collect().foreach(println)

// COMMAND ----------

println("The file contents are: " + zipFileRDD.map(s => new String(s._2.getBytes())).first())

// COMMAND ----------

// Split the files into lines
val fileContentsRDD = zipFileRDD.map(s => new String(s._2.getBytes()))
val lines = fileContentsRDD.flatMap(l => l.split("\n"))

// COMMAND ----------

display(lines.toDF)

// COMMAND ----------

// MAGIC %md
// MAGIC #Now that the data is in DBFS, lets turn it into a dataframe.

// COMMAND ----------

import java.util.Date

import magellan.{Point, Polygon, PolyLine}
import magellan.coord.NAD83

import org.apache.spark.sql.magellan.MagellanContext
import org.apache.spark.sql.magellan.dsl.expressions._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

import java.sql.Timestamp
import java.text.SimpleDateFormat

// COMMAND ----------

// MAGIC %md Now we define the schema for our the rows in our taxi data frame. This follows directly from the Raam Sriharsha's Uber Example

// COMMAND ----------

case class taxiRecord(
  taxiId: Int,
  timeStamp: Timestamp,
  point: Point
  )

// COMMAND ----------

// MAGIC %md Use Java date/time utilities to parse the date strings in the zip files.

// COMMAND ----------

// MAGIC %md #TODO
// MAGIC 
// MAGIC See make unix timestamp function to avoid calling java time library
// MAGIC 
// MAGIC https://spark.apache.org/docs/1.6.1/api/scala/index.html#org.apache.spark.sql.functions$

// COMMAND ----------

val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

// COMMAND ----------

// MAGIC %md Now parse the data line by line, splitting by commas and casting to the correct datatypes. Wrapping in a try-catch block will avoid crashes when invalid data is encountered. Currently this invalid entries are discarded, but may be of interest to see if some data can be recovered. For further information on data cleaning of location data see __TODO__ Chapter in Spark book

// COMMAND ----------

val taxiData = lines.map{line =>
  try {
    val parts = line.split(",")
    val id = parts(0).toInt
    
    val time: Date = dateFormat.parse(parts(1))
    
    val timeStamp = new Timestamp(time.getTime())
    
    val point = Point(parts(2).toDouble, 
                      parts(3).toDouble)
    
    taxiRecord(id, timeStamp, point)
    
  } catch {
    // Lable invalid datapoints
      case e: Throwable => {
        val p = Point(-1.0, -1.0)
        val id = -1
        val timeStamp = new Timestamp(0)
        taxiRecord( id, timeStamp, p)
      }
  }
}
.toDF
.repartition(100)
.where($"taxiId" > -1)
.cache()

// COMMAND ----------

taxiData.show

// COMMAND ----------

// MAGIC %md 
// MAGIC To use this data in new versions of spark not supported by Magellan (yet!), the dataframe containing the trips can be saved to a parquet file and accessed in other notebooks.
// MAGIC 
// MAGIC __Note:__ The datatypes defined by Magellan cannot be stored in a parquet file. To work around this simply store the raw latitude and longitude values instead of the Magellan objects themselves.

// COMMAND ----------

// Helper function to extract latitude and longitued
val pointToTuple = udf( (point: Point) => Array(point.x, point.y))

val taxiDataNoMagellan = taxiData.select($"taxiId", $"timeStamp", pointToTuple($"point").as("latlon")).drop("point")

taxiDataNoMagellan.write.parquet("dbfs:/datasets/t-drive-trips")

// COMMAND ----------

// MAGIC %md ###Now to do Some further work with this data 

// COMMAND ----------

taxiData.count

// COMMAND ----------

// MAGIC %md First for additional functionality we use the ESRI Geometry api. This is a Java library with various functions for working with geometric data.
// MAGIC 
// MAGIC Note that Magellan does built functionality ontop of this library but the functions we need are not exposed through Magellan.
// MAGIC 
// MAGIC For further information and documentation see [here.](https://github.com/Esri/geometry-api-java/wiki)

// COMMAND ----------

import org.apache.spark.sql.{DataFrame, Column, Row}

import com.esri.core.geometry.{Point => ESRIPoint}
import com.esri.core.geometry.{GeometryEngine, SpatialReference}
import com.esri.core.geometry.GeometryEngine.geodesicDistanceOnWGS84

// COMMAND ----------

// MAGIC %md Implicit conversion from a Magellan Point to a Esri Point. This makes things easier when going between Magellan and ESRI.

// COMMAND ----------

implicit def toEsri(point: Point) = {
  new ESRIPoint(point.x, point.y)
}

// COMMAND ----------

// MAGIC %md ###Outlining the Geospatial Constraint Satisfaction Problem (CSP)
// MAGIC 
// MAGIC The problem being addressed in this section can be considered as finding trajectories that satisfy some constraints on time and spatial location. These constraints can be visualised as a three dimensional object with dimensions of longitude, latitude and time. Then trajectory segments that intersect this object are those segments that satisfy the given constraints.

// COMMAND ----------

// MAGIC %md We wish to implement generic functions that find these trajectories of interest.
// MAGIC 
// MAGIC To begin with we first define a circle datatype to represent circular regions in space. The circle is defined in terms of its center, and radius.

// COMMAND ----------

case class Circle(radius: Double, center: Point)


// COMMAND ----------

// MAGIC %md A point then intersects the circle when the distance between the point and the circles center is less than its radius.
// MAGIC 
// MAGIC To codify this define a user defined function(udf) to act on the a column of points given a circle returning the geodesic distance of the points from the center returning true when the point lies within the center. For more information on using udfs see the follwing helpful [blogpost](http://www.sparktutorials.net/using-sparksql-udfs-to-create-date-times-in-spark-1.5), and the official [documentation.](https://spark.apache.org/docs/1.5.2/api/scala/#org.apache.spark.sql.UserDefinedFunction) 
// MAGIC 
// MAGIC For information on the geodesic distance function see the esri documentation. __TODO__ Add link to documentation. The implicit function defined above allows us to call ESRI functions using magellan datatypes.

// COMMAND ----------

val intersectsCircle = udf( (center: Point, radius: Double, point: Point) => geodesicDistanceOnWGS84(center, point) <= radius )

// COMMAND ----------

// MAGIC %md Here below generic functions are defined for Geospatial Constraint Satisfaction problems.
// MAGIC 
// MAGIC The SpaceTimeVolume trait, provides an 'blackbox' interface that can be queried to find trajectories satisfying constraints.

// COMMAND ----------

// MAGIC %md 
// MAGIC What are traits in scala [official documentation](http://docs.scala-lang.org/tutorials/tour/traits.html)
// MAGIC 
// MAGIC Information on Sealed traits [here](http://underscore.io/blog/posts/2015/06/02/everything-about-sealed.html) and [here.](http://underscore.io/blog/posts/2015/06/04/more-on-sealed.html)

// COMMAND ----------

// MAGIC %md To make things generic we define a trait ```SpaceTimeVolume``` as the interface to the CSP functionality. Then specific functionality for each type of geometric region is defined in the case classes that extend this trait.

// COMMAND ----------

sealed trait SpaceTimeVolume {  
  def getIntersectingTrips(trajectories: DataFrame, startTime: Timestamp, endTime:Timestamp): DataFrame
  
  def pointsIntersectingTime(trajectories: DataFrame, startTime: Timestamp, endTime:Timestamp) = {
      trajectories.filter($"timestamp" >= startTime && $"timestamp" <= endTime)
    }
}

case class CircleContainer(circles: DataFrame, startTime: Timestamp, endTime:Timestamp) extends SpaceTimeVolume {
  
  def getIntersectingTrips(trajectories: DataFrame, startTime: Timestamp, endTime:Timestamp) = {
      val tripsIntersectingTime = pointsIntersectingTime(trajectories, startTime, endTime)
    
      val numCircles = circles.count()
      val intersectingPoints = circles.join(tripsIntersectingTime).where(intersectsCircle($"center", $"radius", $"point")).cache()
  
      intersectingPoints

  }
}

case class PolygonContainer(polygons: DataFrame) extends SpaceTimeVolume {
  
    def getIntersectingTrips(trajectories: DataFrame, startTime: Timestamp, endTime:Timestamp) = {
      val tripsIntersectingTime = pointsIntersectingTime(trajectories, startTime, endTime)

      val numPolygons = polygons.count() //See if this can be avoided, possibly pass in this parameter and do count when polygon df is being created?

      val intersectingPoints = polygons.join(tripsIntersectingTime).where($"point" within $"polygon").cache()

      intersectingPoints
    }
}

// COMMAND ----------

// MAGIC %md ### __Example__: Taxis going past Tiananmen Square

// COMMAND ----------

// MAGIC %md __TODO__: Leaflet vis?

// COMMAND ----------

case class PolygonRecord(polygon: Polygon)

val tiananmenSquare = Array(
  Point(116.395674, 39.907510),
  Point(116.399375, 39.907650),
  Point(116.399858, 39.900259),
  Point(116.399858, 39.900259),
  Point(116.395674, 39.907510)
)

val polygonDF = PolygonContainer(
  sc.parallelize(Seq(
    PolygonRecord(new Polygon(Array(0), tiananmenSquare)))).toDF
  )

polygonDF.polygons.show()

// COMMAND ----------

// MAGIC %md Specifying the Time frame we are interested in.

// COMMAND ----------

val startTime: Timestamp = new Timestamp(dateFormat.parse("2008-02-03 00:00:00.0").getTime)
val endTime: Timestamp = new Timestamp(dateFormat.parse("2008-02-03 00:05:00.0").getTime)

// COMMAND ----------

// MAGIC %md Now the getIntersectingTrips function can be run and the data points that intersect the space time volume are found.

// COMMAND ----------

val intersectingTrips = polygonDF.getIntersectingTrips(taxiData, startTime, endTime)

// COMMAND ----------

display(intersectingTrips.select($"taxiId", $"timeStamp"))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by Dillon George and [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)