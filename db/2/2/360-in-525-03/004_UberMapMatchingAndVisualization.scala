// Databricks notebook source
// MAGIC %md
// MAGIC # Needs to be fixed for Spark 2.2 and latest magellan
// MAGIC 
// MAGIC Dillon George, Raazesh Sainudiin, Marina Toger (...)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/01_DillonGeorge/038_UberMapMatchingAndVisualization.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/studentProjects/01_DillonGeorge/038_UberMapMatchingAndVisualization](http://img.youtube.com/vi/0wKxVfeBQBc/0.jpg)](https://www.youtube.com/watch?v=0wKxVfeBQBc?rel=0&autoplay=1&modestbranding=1&start=5931&end=8274)

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
displayHTML(frameIt("https://en.wikipedia.org/wiki/Map_matching",600))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Why are we interested in map-matching?
// MAGIC 
// MAGIC Mainly because we can naturally deal with noise in raw GPS trajectories of entities moving along *mapped ways*, such as, vehicles, pedestrians or cyclists.  
// MAGIC 
// MAGIC * Trajectories from sources like Uber are typically noisy and we will map-match such trajectories in this worksheet.
// MAGIC * Often, such trajectories lead to significant *graph-dimensionality* reduction as you will see below.  
// MAGIC * More importantly, map-matching is a natural first step towards learning distributions over historical trajectories of an entity.
// MAGIC * Moreover, a set of map-matched trajectories (with additional work using kNN operations) can be turned into a graphX graph that can be vertex-programmed and joined with other graphX representations of the map itself.

// COMMAND ----------

// MAGIC %md
// MAGIC ## How are we map-matching?
// MAGIC 
// MAGIC We are using `graphHopper` for this for now. See [https://en.wikipedia.org/wiki/GraphHopper](https://en.wikipedia.org/wiki/GraphHopper).

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

// MAGIC %md
// MAGIC # Steps in this worksheet

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

// MAGIC %md 
// MAGIC ## 1. Preliminaries

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

import org.apache.spark.sql.functions._

// COMMAND ----------



// COMMAND ----------

// MAGIC %md 
// MAGIC ### Next, you need to do the following only once in a cluster (ignore this step the second time!):
// MAGIC * follow section below on **Step 1: Loading our OSM Data ** 
// MAGIC * follow section below on **Step 2: Initialising GraphHopper**

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. Setting up leaflet for visualisation

// COMMAND ----------

// MAGIC %md
// MAGIC Take an array of Strings in 'GeoJson' format, then insert this into a prebuild html string that contains all the code neccesary to display these features using Leaflet.
// MAGIC The resulting html can be displayed in DataBricks using the displayHTML function.
// MAGIC 
// MAGIC See http://leafletjs.com/examples/geojson.html for a detailed example of using GeoJson with Leaflet.

// COMMAND ----------

def genLeafletHTML(features: Array[String]): String = {

  val featureArray = features.reduce(_ + "," +  _)
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

// MAGIC %md 
// MAGIC ##3. Load Uber Data as in earlier analysis. Then convert to an RDD for mapmatching

// COMMAND ----------

case class UberRecord(tripId: Int, time: String, latlon: Array[Double])

val uberData = sc.textFile("dbfs:/datasets/magellan/all.tsv").map { line =>
  val parts = line.split("\t" )
  val tripId = parts(0).toInt
  val time  = parts(1)
  val latlon = Array(parts(3).toDouble, parts(2).toDouble)
  UberRecord(tripId, time, latlon)
}.
repartition(100).
toDF().
select($"tripId", to_utc_timestamp($"time", "yyyy-MM-dd'T'HH:mm:ss").as("timeStamp"), $"latlon").
cache()

// COMMAND ----------

display(uberData)

// COMMAND ----------

// MAGIC %md
// MAGIC We will consider a trip to be invalid when it contains less that two data points, as this is required by Graph Hopper. First identify the all trips that are valid.

// COMMAND ----------

val uberCountsFiltered = uberData.groupBy($"tripId".alias("validTripId")).count.filter($"count" > 1).drop("count")

// COMMAND ----------

display(uberCountsFiltered)

// COMMAND ----------

// MAGIC %md
// MAGIC Next is to join this list of valid Ids with the original data set, only the entries for those trips contained in `uberCountsFiltered`.

// COMMAND ----------

val uberValidData = uberData
  .join(uberCountsFiltered, uberData("tripId") === uberCountsFiltered("validTripId")) // Only want trips with more than 2 data points
  .drop("validTripId").cache 

// COMMAND ----------

// MAGIC %md 
// MAGIC Now seeing how many data points were dropped:

// COMMAND ----------

uberData.count - uberValidData.count

// COMMAND ----------

display(uberValidData)

// COMMAND ----------

// MAGIC %md 
// MAGIC Graphopper considers a trip to be a sequence of (latitude, longitude, time) tuples. First the relevant columns are selected from the DataFrame, and then the rows are mapped to key-value pairs with the tripId as the key. After this is done the `reduceByKey` step merges all the (lat, lon, time) arrays for each key (trip Id) so that there is one entry for each trip id containing all the relevant data points.

// COMMAND ----------

val ubers = uberValidData.select($"tripId", $"latlon", $"timeStamp")
  .map( row => {
    val id = row.get(0).asInstanceOf[Integer]
    val time = row.get(2).asInstanceOf[java.sql.Timestamp].getTime
    val latlon = row.get(1).asInstanceOf[scala.collection.mutable.WrappedArray[Double]] // Array(lat, lon)
    val entry = Array((latlon(0), latlon(1), time))

    (id, entry)})
.reduceByKey( (e1, e2) => e1 ++ e2 /* Sequence of timespace tuples */)
.cache

// COMMAND ----------

display(ubers.toDF)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## 4. Start Map Matching

// COMMAND ----------

// MAGIC %md 
// MAGIC Now stepping into GraphHopper land we first define som utility functions for interfacing with the GraphHopper map matching library.

// COMMAND ----------

// MAGIC %md 
// MAGIC This function takes a `MatchResult` from graphhopper and converts it into an Array of `LON,LAT` points.

// COMMAND ----------

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

// MAGIC %md 
// MAGIC Read [https://github.com/graphhopper/graphhopper/blob/0.6/docs/core/technical.md](https://github.com/graphhopper/graphhopper/blob/0.6/docs/core/technical.md).

// COMMAND ----------

// MAGIC %fs ls /datasets/graphhopper/graphHopperData

// COMMAND ----------

// MAGIC %md 
// MAGIC This function returns a new GrapHopper object, with all settings defined and reading the graph from the location in dbfs. __Note__:  `setAllowWrites(false)` ensures that multiple GraphHopper objects can read from the same files simultaneously. 

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
// MAGIC The next step does the actual map matching. It begins by creating a new GraphHopper object for each partition, this is done as the GraphHopper objects themselves are not Serializable and so must be created on the partitions themselves to avoid this serialization step.
// MAGIC 
// MAGIC Then once the all the GraphHopper and MapMatching objects are created and initialised map matching is run for each trajectory on that partition. The actual map matching is done in the `mm.doWork()` call, this returns a MatchResult object (it is wrapped in a Try statment as an exception is raised when no match is found). With this MatchResult, Failed matches are filtered out being replaced by dummy data, when it successful the coordinates of the matched points are extracted into an array of (latitude, longitude)
// MAGIC 
// MAGIC The last (optional) step estimates the time taken to get from one matched point to another as currently there is no time information retained after the data has been map matched. This is a rather crude way of doing this and more sophisticated methods would be preferable. 

// COMMAND ----------

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
    
    val mr = Try(mm.doWork(gpxEntries)) // mapMatch the trajectory, Try() wraps the exception when no match can be found
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
      case Failure(_) => Array[(Double, Double)]() // When no match can be made
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

case class UberMatched(id: Int, lat: Double, lon: Double, time: Long) // Define the schema of the points in a map matched trip

// COMMAND ----------

// MAGIC %md 
// MAGIC Here we convert the map matched points into a dataframe and explore certain things about the matched points

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

// MAGIC %md 
// MAGIC Finally it useful to be able to visualise the results of the map matching.
// MAGIC 
// MAGIC These next few steps take the map matched trips and convert them into json using the Spray-Json library. See [here](https://github.com/spray/spray-json) for documentation on the library.

// COMMAND ----------

// MAGIC %md 
// MAGIC To make the visualisation less clutterd only one trip will be selected. Though little would have to be done to extend this to multiple/all of the trajectories.
// MAGIC 
// MAGIC Here we select only those points that belong to the trip with id `10193`, it is selected only because it contains the most points after map matching. 

// COMMAND ----------

val filterTrips = matchTrips.filter{case (id, values) => id == 10193 || id == 11973 }.cache

// COMMAND ----------

// MAGIC %md 
// MAGIC Next a schema for the json representation of a trajectory. Then the filtered trips are collected to the master and converted to strings of Json

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

// MAGIC %md 
// MAGIC Now the same is done except using the original trajectory rather than the map matched one.

// COMMAND ----------

val originalTraj = uberData.filter($"tripId" === 10193 || $"tripId" == 11973)
    .select($"latlon").cache

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

// MAGIC %md 
// MAGIC ## 5. Display result of a map-matched trajectory

// COMMAND ----------

val trajHTML = genLeafletHTML(mapMatchedTrajectories ++ Array(originalJson))

// COMMAND ----------

displayHTML(trajHTML)

// COMMAND ----------

// MAGIC %md 
// MAGIC ###Visualization & MapMatching (Further things one could do).
// MAGIC - Show Direction of Travel
// MAGIC - Get timestamp for points, currently Graphhopper map matching does no preserve this information.
// MAGIC - Map the matched coordinates to OSM Way Ids. See [here](https://discuss.graphhopper.com/t/how-to-get-the-osm-resource-reference-for-a-node-edge-in-the-match-result/200) to extract OSM ids from the      graphhopper graph edges
// MAGIC   with GraphHopper, does however require 0.6 SNAPSHOT for it to work.
// MAGIC   
// MAGIC   Another potential way to do this is just to reverse geocode with something such as http://nominatim.openstreetmap.org/

// COMMAND ----------

// MAGIC %md 
// MAGIC # 6. Do the following two steps once in a given cluster

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 1: Loading our OSM Data (_Only needs to be done once_)

// COMMAND ----------

// MAGIC %sh wget https://s3.amazonaws.com/metro-extracts.mapzen.com/san-francisco-bay_california.osm.pbf

// COMMAND ----------

// MAGIC %sh ls

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/graphhopper/osm/")

// COMMAND ----------

dbutils.fs.mv("file:/databricks/driver/san-francisco-bay_california.osm.pbf", "dbfs:/datasets/graphhopper/osm/san-francisco-bay_california.osm.pbf")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/graphhopper/osm"))

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/graphhopper/graphHopperData") // Where graphhopper will store its data

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 2: Initialising GraphHopper  (_Only needs to be done once for each OSM file_)

// COMMAND ----------

// MAGIC %md 
// MAGIC Process an OSM file, creating from it a GraphHopper Graph. The contents of this graph are then stored in the distributed filesystem to be accessed for later use.
// MAGIC This ensures that the processing step only takes place once, and subsequent GraphHopper objects can simply read these files to start map matching.

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

// MAGIC %md 
// MAGIC Move the GraphHopper object to dbfs:

// COMMAND ----------

dbutils.fs.mv("file:/databricks/driver/graphhopper", "dbfs:/datasets/graphhopper/graphHopperData", recurse=true)

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/graphhopper/graphHopperData"))