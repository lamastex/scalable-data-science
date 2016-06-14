// Databricks notebook source exported at Tue, 14 Jun 2016 09:49:40 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by Dillon George
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC # Map-matching Noisy Spatial Trajectories of Vehicles to Roadways in Open Street Map
// MAGIC 
// MAGIC ## Dillon George and Raazesh Sainudiin

// COMMAND ----------

// MAGIC %md 
// MAGIC ## What is map-matching?
// MAGIC Map matching is the problem of how to match recorded geographic coordinates to a logical model of the real world, typically using some form of Geographic Information System. See [https://en.wikipedia.org/wiki/Map_matching](https://en.wikipedia.org/wiki/Map_matching).

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
displayHTML(frameIt("https://en.wikipedia.org/wiki/Map_matching",600))

// COMMAND ----------

// MAGIC %md ## Why are we interested in map-matching?
// MAGIC 
// MAGIC Mainly because we can naturally deal with naturally occuring noise in raw GPS trajectories of entities moving along *mapped ways*, such as, vehicles, pedestrians or cyclists.  
// MAGIC 
// MAGIC * Trajectories from sources like Uber are typically noisy and we will map-match such trajectories in this worksheet.
// MAGIC * Often, such trajectories lead to significant *graph-dimensionality* reduction as you will see below.  
// MAGIC * More importantly, map-matching is a natural first step towards learning distributions over historical trajectories of an entity.
// MAGIC * Moreover, a set of map-matched trajectories (with additional work using kNN operations) can be turned into a graphX graph that can vertex programmed and joined with other graphX representations of the map itself.

// COMMAND ----------

// MAGIC %md ## How are we map-matching?
// MAGIC 
// MAGIC We are using `graphHopper` for this for now.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/GraphHopper",600))

// COMMAND ----------

// MAGIC %md
// MAGIC ### The following alternatives need exploration:
// MAGIC * BMW's barefoot on OSM (with Spark integration)
// MAGIC   * [https://github.com/bmwcarit/barefoot](https://github.com/bmwcarit/barefoot)
// MAGIC   * [http://www.bmw-carit.com/blog/barefoot-release-an-open-source-java-library-for-map-matching-with-openstreetmap/](http://www.bmw-carit.com/blog/barefoot-release-an-open-source-java-library-for-map-matching-with-openstreetmap/) which seems to use a Hidden Markov Model from Microsoft Research.
// MAGIC     

// COMMAND ----------

// MAGIC %md # Steps in this worksheet

// COMMAND ----------

// MAGIC %md
// MAGIC The basic steps are the following:
// MAGIC 1. Preliminaries: Attach needed libraries, load osm data and initialize graphhopper (the last two steps need to be done only once per cluster)
// MAGIC 2. Setting up leaflet for visualisation
// MAGIC 3. Load table of Uber Data from earlier analysis. Then convert to an RDD for mapmatching
// MAGIC 4. Start Map Matching
// MAGIC 5. Display Results of a map-matched trajectory
// MAGIC 6. Do the following two steps once in a given cluster

// COMMAND ----------

// MAGIC %md ## 1. Preliminaries

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Loading required libraries
// MAGIC 1. Launch a cluster using spark 1.5.2 (this is for compatibility with magellan).
// MAGIC 2. Attach following libraries:
// MAGIC   * map_matching_2_11_0_1
// MAGIC   * magellan 
// MAGIC   * spray-json

// COMMAND ----------

import com.graphhopper.matching._
import com.graphhopper._
import com.graphhopper.routing.util.{EncodingManager, CarFlagEncoder}
import com.graphhopper.storage.index.LocationIndexTree
import com.graphhopper.util.GPXEntry

import magellan.Point

import scala.collection.JavaConverters._
import spray.json._
import DefaultJsonProtocol._

import scala.util.{Try, Success, Failure}

// COMMAND ----------

// MAGIC %md ### Next, you need to do the following only once in a cluster (ignore this step the second time!):
// MAGIC * follow section below on **Step 1: Loading our OSM Data ** 
// MAGIC * follow section below on **Step 2: Initialising GraphHopper**

// COMMAND ----------

// MAGIC %md ## 2. Setting up leaflet for visualisation

// COMMAND ----------

// MAGIC %md Take an array of Strings in 'GeoJson' format, then insert this into a prebuild html string that contains all the code neccesary to display these features using Leaflet.
// MAGIC The resulting html can be displayed in DataBricks using the displayHTML function.
// MAGIC 
// MAGIC See http://leafletjs.com/examples/geojson.html for a detailed example of using GeoJson with Leaflet.

// COMMAND ----------

def genLeafletHTML(features: Array[String]): String = {

  val featureArray = features.reduce(_ + "," +  _)
  //TODO: Need to add own api key, current key is from leaflet tutorial
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
  var map = L.map('map').setView([37.77471008393265, -122.40422604391485], 14);

  L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=$accessToken', {
  maxZoom: 18,
  attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
  '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
  'Imagery © <a href="http://mapbox.com">Mapbox</a>',
  id: 'mapbox.streets'
  }).addTo(map);

  var features = [$featureArray];

 colors = features.map(function (_) {return rainbow(100, Math.floor(Math.random() * 100)); });

  for (var i = 0; i < features.length; i++) {
      console.log(i);
      L.geoJson(features[i], {
          pointToLayer: function (feature, latlng) {
              return L.circleMarker(latlng, {
                  radius: 4,
                  fillColor: colors[i],
                  color: colors[i],
                  weight: 1,
                  opacity: 1,
                  fillOpacity: 0.8
              });
          }
      }).addTo(map);
  }


  function rainbow(numOfSteps, step) {
  // This function generates vibrant, "evenly spaced" colours (i.e. no clustering). This is ideal for creating easily distinguishable vibrant markers in Google Maps and other apps.
  // Adam Cole, 2011-Sept-14
  // HSV to RBG adapted from: http://mjijackson.com/2008/02/rgb-to-hsl-and-rgb-to-hsv-color-model-conversion-algorithms-in-javascript
  var r, g, b;
  var h = step / numOfSteps;
  var i = ~~(h * 6);
  var f = h * 6 - i;
  var q = 1 - f;
  switch(i %% 6){
  case 0: r = 1; g = f; b = 0; break;
  case 1: r = q; g = 1; b = 0; break;
  case 2: r = 0; g = 1; b = f; break;
  case 3: r = 0; g = q; b = 1; break;
  case 4: r = f; g = 0; b = 1; break;
  case 5: r = 1; g = 0; b = q; break;
  }
  var c = "#" + ("00" + (~ ~(r * 255)).toString(16)).slice(-2) + ("00" + (~ ~(g * 255)).toString(16)).slice(-2) + ("00" + (~ ~(b * 255)).toString(16)).slice(-2);
  return (c);
  }
  </script>


  </body>
  """
  generatedHTML
}

// COMMAND ----------

// MAGIC %md ##3. Load table of Uber Data from earlier analysis. Then convert to an RDD for mapmatching

// COMMAND ----------

val uberData = sqlContext.sql("SELECT * from uberTable")

// COMMAND ----------

// MAGIC %md Do some data preprocessing and filter out all trips with less than two data points. This is needed for Graph Hopper to work. 

// COMMAND ----------

val UberCountsFiltered = uberData.groupBy($"tripId".alias("validTripId")).count.filter($"count" > 1)

// COMMAND ----------

val uberValidData = uberData
  .join(UberCountsFiltered, uberData("tripId") === UberCountsFiltered("validTripId")) // Only want trips with more than 2 data points
  .drop("validTripId").cache 

// COMMAND ----------

val ubers = uberValidData.select($"tripId", $"latlon", $"time")
  .map( row => {
    val id = row.get(0).asInstanceOf[Integer]
    val time = row.get(2).asInstanceOf[Long]
    val latlon = row.get(1).asInstanceOf[scala.collection.mutable.WrappedArray[Double]] // Array(lat, lon)
    val entry = Array((latlon(0), latlon(1), time))

    (id, entry)})
.reduceByKey( (e1, e2) => e1 ++ e2 /* Sequence of timespace tuples */)
.cache

// COMMAND ----------

ubers.take(2)

// COMMAND ----------

// MAGIC %md ## 4. Start Map Matching

// COMMAND ----------

// MAGIC %md The following function takes a `MatchResult` from graphhopper and converts it into an Array of `LON,LAT` points.

// COMMAND ----------

// Helper function to convert a MatchResult int a sequence of lon, lat points
def extractLatLong(mr: MatchResult): Array[(Double, Double)] = {
  val pointsList = mr.getEdgeMatches.asScala.zipWithIndex
                    .map{ case  (e, i) =>
                              if (i == 0) e.getEdgeState.fetchWayGeometry(3) // FetchWayGeometry returns vertices on graph if 2,
                              else e.getEdgeState.fetchWayGeometry(2) }      // and edges if 3 
                    .map{case pointList => pointList.asScala.toArray}
                    .flatMap{ case e => e}
  val latLongs = pointsList.map(point => (point.lon, point.lat)).toArray

  latLongs   
}

// COMMAND ----------

// MAGIC %md
// MAGIC The following creates returns a new GraphHopper object and encoder. It reads the pre generated graphhopper 'database' from the dbfs, this way multiple graphHopper objects can be created on the workers all reading from the same shared database.
// MAGIC 
// MAGIC Currently the documentation is scattered all over the place if it exists at all. The method to create the Graph as specified in the map-matching repository differs from the main GraphHopper repository. The API should hopefully converge as GraphHopper matures
// MAGIC 
// MAGIC See the main graphHopper documentation [here](https://github.com/graphhopper/graphhopper/blob/0.5/docs/core/low-level-api.md), and the map-matching documentation [here.](https://github.com/graphhopper/map-matching#java-usage)

// COMMAND ----------

// MAGIC %md https://github.com/graphhopper/graphhopper/blob/0.6/docs/core/technical.md

// COMMAND ----------

// MAGIC %fs ls /datasets/graphhopper/graphHopperData

// COMMAND ----------

def getHopper = {
    val enc = new CarFlagEncoder() // Vehicle type
    val hopp = new GraphHopper()
    .setStoreOnFlush(true)
    .setCHWeighting("shortest")    // Contraction Hierarchy settings
    .setAllowWrites(false)         // Avoids issues when reading graph object fom HDFS
    .setGraphHopperLocation("/dbfs/datasets/graphhopper/graphHopperData")
    .setEncodingManager(new EncodingManager(enc))
  hopp.importOrLoad()
  
  (hopp, enc)
}

// COMMAND ----------

// MAGIC %md
// MAGIC Not create a MapMatching Object for each partition of the RDD. Then for each partition map match the trajectories stored on it, using this MapMathching
// MAGIC 
// MAGIC Prior to this a graphHopper object was created once for every trajectory.

// COMMAND ----------

//TODO: Break up into smaller functions instead of one big blob
val matchTrips = ubers.mapPartitions(partition => {
  // Create the map matching object only once for each partition
  val (hopp, enc) = getHopper
  
  val tripGraph = hopp.getGraphHopperStorage()
  val locationIndex = new LocationIndexMatch(tripGraph,
                                             hopp.getLocationIndex().asInstanceOf[LocationIndexTree])
  
  val mm = new MapMatching(tripGraph, locationIndex, enc)
  
  // Map matching parameters
  // Have not found any documention on what these do, other that comments in source code
  mm.setMaxSearchMultiplier(2000)
  mm.setSeparatedSearchDistance(600)
  mm.setForceRepair(true)
  
  // Do the map matching for each trajectory
  val matchedPartition = partition.map{case (key, dataPoints) => {
    
    val sortedPoints = dataPoints.sortWith( (a, b) => a._3 < b._3) // Sort by time
    val gpxEntries = sortedPoints.map{ case (lat, lon, time) => new GPXEntry(lon, lat, time)}.toList.asJava
    
    val mr = Try(mm.doWork(gpxEntries)) // mapMatch the trajectory, Try() wraps the exception when no matching can be found
    val points = mr match {
      case Success(result) => {
        val pointsList = result.getEdgeMatches.asScala.zipWithIndex // (edge, index tuple)
                    .map{ case  (e, i) =>
                              if (i == 0) e.getEdgeState.fetchWayGeometry(3) // FetchWayGeometry returns verts on graph if 2,
                              else e.getEdgeState.fetchWayGeometry(2)        // and edges if 3 (I'm pretty sure that's the case)
                    }      
                    .map{case pointList => pointList.asScala.toArray}
                    .flatMap{ case e => e}
        val latLongs = pointsList.map(point => (point.lon, point.lat)).toArray
        
        latLongs
      }
      case Failure(_) => Array[(Double, Double)]() // When no match can be mde
    }
    
    // Use GraphHopper routing to get time estimates of the new matched trajcetory
    /// NOTE: Currently only calculates time offsets from 0
    val times = points.iterator.sliding(2).map{ pair =>
      val (lonFrom, latFrom) = pair(0)
      val (lonTo, latTo) = pair(1)

      val req = new GHRequest(latFrom, lonFrom, latTo, lonTo)
          .setWeighting("shortest")
          .setVehicle("car")
          .setLocale("US")

      val time = hopp.route(req).getTime
      time
    }
    
    val timeOffsets = times.scanLeft(0.toLong){ (a: Long, b: Long) => a + b }.toList
    
    (key, points.zip(timeOffsets)) // Return a tuple of (key, Array((lat, lon), timeOffSetFromStart))
  }}
  
  matchedPartition
}).cache

// COMMAND ----------

case class UberMatched(id: Int, lat: Double, lon: Double, time: Long)

// COMMAND ----------

// Create a dataframe to better explore the matched trajectories, make sure it is sensible
val matchTripsDF = matchTrips.map{case (id, points) => 
  points.map(point => UberMatched(id, point._1._1, point._1._2, point._2 ))
}
.flatMap(uberMatched => uberMatched)
.toDF.cache

// COMMAND ----------

display(matchTripsDF.groupBy($"id").count.orderBy(-$"count"))

// COMMAND ----------

// MAGIC %md Explore some potentiall anomalous matched trajectories 

// COMMAND ----------

val filterTrips = matchTrips.filter{case (id, values) => id == 10193}.cache

// COMMAND ----------

// Convert our Uber data points into GeoJson Geometries
// Is not fully compliant with the spec but in a format that Leaflet understands
case class UberData(`type`: String = "MultiPoint",
                    coordinates: Array[(Double, Double)])

object UberJsonProtocol extends DefaultJsonProtocol {
  implicit val uberDataFormat = jsonFormat2(UberData)
}

import UberJsonProtocol._

val mapMatchedTrajectories = filterTrips.collect.map{case (key, matchedPoints) => { // change filterTrips to matchTrip to get all matched trajectories as json
  val jsonString = UberData(coordinates = matchedPoints.map{case ((lat, lon), time) => (lat, lon)}).toJson.prettyPrint
  jsonString
}}

// COMMAND ----------

val originalTraj = uberData.filter($"tripId" === 10193)
    .select($"latlon").cache

// COMMAND ----------

originalTraj.count

// COMMAND ----------

// Convert our Uber data points into GeoJson Geometries 
case class UberData(`type`: String = "MultiPoint",
                    coordinates: Array[(Double, Double)])

object UberJsonProtocol extends DefaultJsonProtocol {
  implicit val uberDataFormat = jsonFormat2(UberData)
}

import UberJsonProtocol._

val originalLatLon = originalTraj
  .map(r => r.getAs[scala.collection.mutable.WrappedArray[Double]]("latlon"))
  .map(point => (point(0), point(1))).collect

val originalJson = UberData(coordinates = originalLatLon).toJson.prettyPrint  // Original Unmatched trajectories

// COMMAND ----------

// MAGIC %md ## 5. Display result of a map-matched trajectory

// COMMAND ----------

val trajHTML = genLeafletHTML(mapMatchedTrajectories ++ Array(originalJson))

// COMMAND ----------

displayHTML(trajHTML)

// COMMAND ----------

// MAGIC %md ###Visualization & MapMatching (Further things one could do).
// MAGIC - Show Direction of Travel
// MAGIC - Get timestamp for points, currently Graphhopper map matching does no preserve this information.
// MAGIC - Map the matched coordinates to OSM Way Ids. See [here](https://discuss.graphhopper.com/t/how-to-get-the-osm-resource-reference-for-a-node-edge-in-the-match-result/200) to extract OSM ids from the      graphhopper graph edges
// MAGIC   with GraphHopper, does however require 0.6 SNAPSHOT for it to work.
// MAGIC   
// MAGIC   Another potential way to do this is just to reverse geocode with something such as http://nominatim.openstreetmap.org/

// COMMAND ----------

// MAGIC %md # 6. Do the following two steps once in a given cluster

// COMMAND ----------

// MAGIC %md ## Step 1: Loading our OSM Data (_Only needs to be done once_)

// COMMAND ----------

// MAGIC %sh wget https://s3.amazonaws.com/metro-extracts.mapzen.com/san-francisco-bay_california.osm.pbf

// COMMAND ----------

// MAGIC %sh ls

// COMMAND ----------

dbutils.fs.mv("file:/databricks/driver/san-francisco-bay_california.osm.pbf", "dbfs:/datasets/graphhopper/osm/san-francisco-bay_california.osm.pbf")

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/graphhopper/osm/")

// COMMAND ----------

uberData.limit(10).show()

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/graphhopper/osm"))

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/graphhopper/graphHopperData") // Where graphhopper will store its data

// COMMAND ----------

// MAGIC %md ## Step 2: Initialising GraphHopper  (_Only needs to be done once for each OSM file_)

// COMMAND ----------

val osmPath = "/dbfs/datasets/graphhopper/osm/san-francisco-bay_california.osm.pbf"
val graphHopperPath = "/dbfs/datasets/graphhopper/graphHopperData"

// COMMAND ----------

val encoder = new CarFlagEncoder()
 
val hopper = new GraphHopper()
      .setStoreOnFlush(true)
      .setEncodingManager(new EncodingManager(encoder))
      .setOSMFile(osmPath)
      .setCHWeighting("shortest")
      .setGraphHopperLocation("graphhopper/")

hopper.importOrLoad()

// COMMAND ----------

dbutils.fs.mv("file:/databricks/driver/graphhopper", "dbfs:/datasets/graphhopper/graphHopperData", recurse=true)

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/graphhopper/graphHopperData"))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by Dillon George
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)