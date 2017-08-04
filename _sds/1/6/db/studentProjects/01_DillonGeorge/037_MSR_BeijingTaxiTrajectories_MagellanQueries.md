---
title: ""
permalink: /sds/1/6/db/studentProjects/01_DillonGeorge/037_MSR_BeijingTaxiTrajectories_MagellanQueries/
sidebar:
  nav: "lMenu-SDS-1.6"
---

// Databricks notebook source exported at Sun, 26 Jun 2016 01:48:36 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by Dillon George and [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/01_DillonGeorge/037_MSR_BeijingTaxiTrajectories_MagellanQueries.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/01_DillonGeorge/037_MSR_BeijingTaxiTrajectories_MagellanQueries](http://img.youtube.com/vi/0wKxVfeBQBc/0.jpg)](https://www.youtube.com/v/0wKxVfeBQBc?rel=0&autoplay=1&modestbranding=1&start=2056&end=5930)





# Scalable Spatio-Temporal Constraint Satisfaction Problem (ST-CSP)

Suppose you have base spatial sets in 2D space (points, lines, polygons, etc) given by: 

\\[\\{S\_i \\}\_{i=1}^m\\] 

and base temporal sets in 1D given by intervals in time:
\\[ \\{T\_j \\}\_{j=1}^n .\\] 

Then one can obtain space-time set in 3D as unions of Cartesian products of such base spatial and temporal sets.

A simple constraint satisfaction problem can be posed as a Boolean-valued statement involving such space-time sets.

Often, the first seps that a geospatial scientist or analyst needs to take in order to build intuition about the phenomenon of interest invovles such CSP problems.

Some simple examples of such CSP problems are:
* Given a set of GPS trajectories for multiple entities find the set of entities that intersect with a given space-time set or a collection of space-time sets.
* Find the set of entitites that were close to one another based on their trajectories during a given period of time.
* Find entities that are spatio-temporally inconsistent - such as being in two places at the same time.
* ...




 
## Microsoft Research's Beijing Taxicab Trajectories using Magellan

### T-Drive trajectory data sample

Yu Zheng

12 August 2011

**Abstract**

This is a sample of T-Drive trajectory dataset that contains a one-week trajectories of 10,357 taxis. The total number of points in this dataset is about 15 million and the total distance of the trajectories reaches 9 million kilometers.

Please cite the following papers when using the dataset: [1] Jing Yuan, Yu Zheng, Xing Xie, and Guangzhong Sun. Driving with knowledge from the physical world. In The 17th ACM SIGKDD international conference on Knowledge Discovery and Data mining, KDD'11, New York, NY, USA, 2011. ACM. [2] Jing Yuan, Yu Zheng, Chengyang Zhang, Wenlei Xie, Xing Xie, Guangzhong Sun, and Yan Huang. T-drive: driving directions based on taxi trajectories. In Proceedings of the 18th SIGSPATIAL International Conference on Advances in Geographic Information Systems, GIS '10, pages 99-108, New York, NY, USA,2010. ACM.

More details on the dataset and related papers are available [here](http://research.microsoft.com/apps/pubs/?id=152883).


```scala

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
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
displayHTML(frameIt("http://research.microsoft.com/apps/pubs/?id=152883",700))

```




## Steps in this notebook

1. Download the taxi trips in Beijing from MSR.
* Turn them into Dataframes.
* Define generic functions for the CSP problem.


```scala

import java.net.URL
import java.io.File
import org.apache.commons.io.FileUtils

import com.cotdp.hadoop.ZipFileInputFormat
import org.apache.hadoop.io.BytesWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job

```


 
### 1. Download the Taxi Trips




 
#### Load zip files directly into spark
To learn how to do load zip files directly from the distributed file system see 
* *Databricks Guide -> Accessing Data -> Common File Formats -> Zip Files - scala* 
* or the importable notebook:
  * [https://docs.cloud.databricks.com/docs/latest/databricks_guide/03%20Accessing%20Data/3%20Common%20File%20Formats/6%20Zip%20Files%20-%20scala.html](https://docs.cloud.databricks.com/docs/latest/databricks_guide/03%20Accessing%20Data/3%20Common%20File%20Formats/6%20Zip%20Files%20-%20scala.html).


```scala

val numZipFiles = 14

```
```scala

val zipURLs = (1 until numZipFiles + 1).map(i => new URL(f"http://research.microsoft.com/pubs/152883/0$i.zip"))

```


 
Load all these URLS into the distributed filesystem


```scala

val localZipFiles = (1 until numZipFiles+1).map(i => new File(f"/home/ubuntu/databricks/driver/drive0$i.zip"))

```


 
Download the files and copy them to the appropriate locations


```scala

val locations = (zipURLs, localZipFiles).zipped.par.toList

locations.par.foreach(location => location match {
  case (url, file) => {
    println("Doing: ", url)
    FileUtils.copyURLToFile(url, file)
  }
  case _ => Unit
})

```


 
###Load these zipfiles into DBFS


```scala

(1 until numZipFiles+1).foreach(i => dbutils.fs.mv(f"file:/home/ubuntu/databricks/driver/drive0$i.zip", f"dbfs:/home/ubuntu/databricks/driver/t-drive/drive0$i.zip"))

```
```scala

%fs ls "dbfs:/home/ubuntu/databricks/driver/t-drive/"

```


 
###Now turn these zip files into RDDs




 
###Turn into a (K, V) RDD
The Key is the name of the file,  Value is that files contents.


```scala

val zipFileRDDs = (1 until numZipFiles+1).map(i => sc.newAPIHadoopFile(
        f"dbfs:/home/ubuntu/databricks/driver/t-drive/drive0$i.zip",
        classOf[ZipFileInputFormat],
        classOf[Text],
        classOf[BytesWritable],
        new Job().getConfiguration()))

val zipFileRDD = sc.union(zipFileRDDs)

```
```scala

zipFileRDD.map(s => s._1.toString()).collect().foreach(println)

```
```scala

println("The file contents are: " + zipFileRDD.map(s => new String(s._2.getBytes())).first())

```
```scala

// Split the files into lines
val fileContentsRDD = zipFileRDD.map(s => new String(s._2.getBytes()))
val lines = fileContentsRDD.flatMap(l => l.split("\n"))

```
```scala

display(lines.toDF)

```



### Now that the data is in DBFS, lets turn it into a dataframe.


```scala

import magellan.{Point, Polygon, PolyLine}
import magellan.coord.NAD83

import org.apache.spark.sql.magellan.MagellanContext
import org.apache.spark.sql.magellan.dsl.expressions._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

import java.sql.Timestamp

```


 
Now we define the schema for our the rows in our taxi data frame. This follows directly from the Raam Sriharsha's Uber Example


```scala

case class taxiRecord(
  taxiId: Int,
  time: String,
  point: Point
  )

```


 
Use Java date/time utilities to parse the date strings in the zip files.




 

**An Ideal TODO:** See make unix timestamp function to avoid calling the java time library (as done in this notebook).

[https://spark.apache.org/docs/1.6.1/api/scala/index.html#org.apache.spark.sql.functions$](https://spark.apache.org/docs/1.6.1/api/scala/index.html#org.apache.spark.sql.functions$)


```scala

val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

```


 
Now parse the data line by line, splitting by commas and casting to the correct datatypes. Wrapping in a try-catch block will avoid crashes when invalid data is encountered. Currently this invalid entries are discarded, but may be of interest to see if some data can be recovered. For further information on data cleaning of location data in chapter 8 of [Advanced Analytics with Spark](http://shop.oreilly.com/product/0636920035091.do)


```scala

val taxiData = lines.map{line =>
  try {
    val parts = line.split(",")
    val id = parts(0).toInt
    val time = parts(1)
    
    val point = Point(parts(2).toDouble, 
                      parts(3).toDouble)
    
    taxiRecord(id, time, point)
    
  } catch {
    // Label invalid datapoints
      case e: Throwable => {
        val p = Point(-1.0, -1.0)
        val id = -1
        val time = "0000-00-00 00:00:00"
        taxiRecord( id, time, p)
      }
  }
}
.toDF
.select($"taxiId", to_utc_timestamp($"time", "yyyy-MM-dd HH:mm:ss").as("timeStamp"), $"point") //
.repartition(100)
.where($"taxiId" > -1)
.cache()

```
```scala

taxiData.show

```


 
To use this data in new versions of spark not supported by Magellan (yet!), the dataframe containing the trips can be saved to a parquet file and accessed in other notebooks.

__Note:__ The datatypes defined by Magellan cannot be stored in a parquet file. To work around this simply store the raw latitude and longitude values instead of the Magellan objects themselves.


```scala

// Helper function to extract latitude and longitued
val pointToTuple = udf( (point: Point) => Array(point.x, point.y))

val taxiDataNoMagellan = taxiData.select($"taxiId", $"timeStamp", pointToTuple($"point").as("latlon")).drop("point")

taxiDataNoMagellan.write.parquet("dbfs:/datasets/t-drive-trips")

```


 
###Now to do Some further work with this data 


```scala

taxiData.count

```


 
First for additional functionality we use the ESRI Geometry api. This is a Java library with various functions for working with geometric data.

Note that Magellan does built functionality ontop of this library but the functions we need are not exposed through Magellan.

For further information and documentation see [here.](https://github.com/Esri/geometry-api-java/wiki)


```scala

import org.apache.spark.sql.{DataFrame, Column, Row}

import com.esri.core.geometry.{Point => ESRIPoint}
import com.esri.core.geometry.{GeometryEngine, SpatialReference}
import com.esri.core.geometry.GeometryEngine.geodesicDistanceOnWGS84

```


 
Implicit conversion from a Magellan Point to a Esri Point. This makes things easier when going between Magellan and ESRI points.


```scala

implicit def toEsri(point: Point) = {
  new ESRIPoint(point.x, point.y)
}

```


 
###Outlining the Geospatial Constraint Satisfaction Problem (CSP)

The problem being addressed in this section can be considered as finding trajectories that satisfy some constraints on time and spatial location. These constraints can be visualised as a three dimensional object with dimensions of longitude, latitude and time. Then trajectory segments that intersect this object are those segments that satisfy the given constraints.




 
We wish to implement generic functions that find these trajectories of interest.

To begin with we first define a circle datatype to represent circular regions in space. The circle is defined in terms of its center, and radius.


```scala

case class Circle(radius: Double, center: Point)

```


 
A point then intersects the circle when the distance between the point and the circles center is less than its radius.

To codify this define a user defined function(udf) to act on the a column of points given a circle returning the geodesic distance of the points from the center returning true when the point lies within the center. For more information on using udfs see the follwing helpful [blogpost](http://www.sparktutorials.net/using-sparksql-udfs-to-create-date-times-in-spark-1.5), and the official [documentation.](https://spark.apache.org/docs/1.5.2/api/scala/#org.apache.spark.sql.UserDefinedFunction) 

For information on the geodesic distance function see the relevant ESRI documentation [here](https://github.com/Esri/geometry-api-java/wiki). The implicit function defined above allows us to call ESRI functions using magellan datatypes.


```scala

val intersectsCircle = udf( (center: Point, radius: Double, point: Point) => geodesicDistanceOnWGS84(center, point) <= radius )

```


 
Here below generic functions are defined for Geospatial Constraint Satisfaction problems.

The SpaceTimeVolume trait, provides an 'blackbox' interface that can be queried to find trajectories satisfying constraints.




 
What are traits in scala [official documentation](http://docs.scala-lang.org/tutorials/tour/traits.html)

Information on Sealed traits [here](http://underscore.io/blog/posts/2015/06/02/everything-about-sealed.html) and [here.](http://underscore.io/blog/posts/2015/06/04/more-on-sealed.html)




 
To make things generic we define a trait ```SpaceTimeVolume``` as the interface to the CSP functionality. Then specific functionality for each type of geometric region is defined in the case classes that extend this trait.


```scala

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

```


 
### __Example__: Taxis going past Tiananmen Square




 
To show the result of this consider the following polygon.


```scala

case class PolygonRecord(polygon: Polygon)

val tiananmenSquare = Array(
    Point(116.3931388,39.9063043),
    Point(116.3938048,39.8986498),
    Point(116.3892702,39.8980945),
    Point(116.3894568,39.9061898)
)

val polygonDF = PolygonContainer(
  sc.parallelize(Seq(
    PolygonRecord(new Polygon(Array(0), tiananmenSquare)))).toDF
  )

polygonDF.polygons.show()

```


 
This is a polygon covering an area approximating that around Tiananmen Square. And we wish to find all the all the taxis that travel around the square over a timeframe given below.


```scala

def genLeafletHTML(): String = {

  val accessToken = "pk.eyJ1IjoiZHRnIiwiYSI6ImNpaWF6MGdiNDAwanNtemx6MmIyNXoyOWIifQ.ndbNtExCMXZHKyfNtEN0Vg"

  val generatedHTML = f"""<!DOCTYPE html>
  <html>
  <head>
  <title>Maps</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/leaflet.css">
  <style>
  #map {width: 600px; height:400px;}
  </style>

  </head>
  <body>
  <div id="map" style="width: 1000px; height: 600px"></div>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/leaflet.js"></script>
  <script type="text/javascript">
  var map = L.map('map').setView([39.913818, 116.363625], 14);

  L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=$accessToken', {
  maxZoom: 18,
  attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
  '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
  'Imagery © <a href="http://mapbox.com">Mapbox</a>',
  id: 'mapbox.streets'
  }).addTo(map);

  var polygon = L.polygon([
    [39.9063043, 116.3931388],
    [39.8986498, 116.3938048],
    [39.8980945, 116.3892702],
    [39.9061898, 116.3894568]
    ]).addTo(map);
  </script>


  </body>
  """
  generatedHTML
}
displayHTML(genLeafletHTML)

```


 
Specifying the Time frame we are interested in.


```scala

val startTime: Timestamp = Timestamp.valueOf("2008-02-03 00:00:00.0")
val endTime: Timestamp = Timestamp.valueOf("2008-02-03 01:00:00.0")

```


 
Now the `getIntersectingTrips` function can be run and the data points that intersect the space time volume are found.


```scala

val intersectingTrips = polygonDF.getIntersectingTrips(taxiData, startTime, endTime)

```


 
Here are all the points that pass through the polygon:


```scala

display(intersectingTrips.select($"taxiId", $"timeStamp"))

```


 
A list of all the taxis that take a trip around the square:


```scala

display(intersectingTrips.select($"taxiId").distinct)

```
```scala

display(intersectingTrips.groupBy($"taxiId").count.orderBy(-$"count"))

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by Dillon George and [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
