// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC 
// MAGIC This is part of [*Project MEP: Meme Evolution Programme*](http://lamastex.org/lmse/mep) and supported by databricks academic partners program.
// MAGIC 
// MAGIC # Map-matching Noisy Spatial Trajectories of Vehicles to Roadways in Open Street Map
// MAGIC 
// MAGIC ## Dillon George, Dan Lilja and Raazesh Sainudiin
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016-2019 Dillon George, Dan Lilja and Raazesh Sainudiin
// MAGIC 
// MAGIC Licensed under the Apache License, Version 2.0 (the "License");
// MAGIC you may not use this file except in compliance with the License.
// MAGIC You may obtain a copy of the License at
// MAGIC 
// MAGIC     http://www.apache.org/licenses/LICENSE-2.0
// MAGIC 
// MAGIC Unless required by applicable law or agreed to in writing, software
// MAGIC distributed under the License is distributed on an "AS IS" BASIS,
// MAGIC WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// MAGIC See the License for the specific language governing permissions and
// MAGIC limitations under the License.
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC This is the precursor 2016 presentation by Dillon George as part of *Scalable Data Science from Middle Earth* student project.
// MAGIC 
// MAGIC [![sds/uji/studentProjects/01_DillonGeorge/038_UberMapMatchingAndVisualization](http://img.youtube.com/vi/0wKxVfeBQBc/0.jpg)](https://www.youtube.com/watch?v=0wKxVfeBQBc?rel=0&autoplay=1&modestbranding=1&start=5931&end=8274)
// MAGIC 
// MAGIC Here we are updating it to more recent versions of the needed libraries.

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
// MAGIC ### The basic steps are the following:
// MAGIC 1. Preliminaries: 0. Attach needed libraries, load osm data and initialize graphhopper 
// MAGIC   - the two steps 0.1 and 0.2 need to be done only once per cluster
// MAGIC 2. Setting up leaflet for visualisation
// MAGIC 3. Load table of Uber Data from earlier analysis. Then convert to an RDD for mapmatching
// MAGIC 4. Start Map Matching
// MAGIC 5. Display Results of a map-matched trajectory

// COMMAND ----------

// MAGIC %md 
// MAGIC ## 1. Preliminaries

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Loading required libraries
// MAGIC 1. Launch a cluster using spark 2.4.3 (this is for compatibility with magellan built from the forked repos; see first notebook in this folder!).
// MAGIC 2. Attach following libraries if you have not already done so:
// MAGIC   * map_matching - `com.graphhopper:map-matching:0.6.0` (more recent libraries may work but are note tested yet!)
// MAGIC   * magellan - import custom-built jar by downloading locally from `https://github.com/lamastex/scalable-data-science/blob/master/custom-builds/jars/magellan/forks/` and then uploading to databricks
// MAGIC   * If needed only (this is already in databricks): spray-json `io.spray:spray-json_2.11:1.3.4`

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

// MAGIC %md 
// MAGIC ### Do Step 0 at the bottom of the notebook
// MAGIC #### Only once in shard per OSM file (ignore this step the second time!):
// MAGIC * follow section below on **Step 0.1: Loading our OSM Data ** 
// MAGIC * follow section below on **Step 0.2: Initialising GraphHopper**

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **NOTE**
// MAGIC 
// MAGIC If you loaded a smaller map so as to be able to analyze in the community edition, then you need the bounding box of this map to filter those trajectories that fall within this smaller map.
// MAGIC 
// MAGIC For example `SanfranciscoSmall` OSM map has the following bounding box:
// MAGIC 
// MAGIC - `-122.449,37.747` and `-122.397,37.772`
// MAGIC 
// MAGIC Let's put them in Scala `val`s as follows:

// COMMAND ----------

val minLatInOSMMap = -122.449
val minLonInOSMMap = 37.747
val maxLatInOSMMap = -122.397
val maxLonInOSMMap = 37.772

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. Setting up leaflet and visualisation
// MAGIC 
// MAGIC 
// MAGIC ## 2.1 Setting up leaflet 
// MAGIC 
// MAGIC You need to go to the following URL and set-up access-token in map-box to use leaflet independently:
// MAGIC 
// MAGIC  - https://leafletjs.com/examples/quick-start/
// MAGIC  - Request access-token:
// MAGIC    - https://account.mapbox.com/auth/signin/?route-to=%22/access-tokens/%22

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2.2 Visualising with leaflet
// MAGIC Take an array of Strings in 'GeoJson' format, then insert this into a prebuild html string that contains all the code neccesary to display these features using Leaflet.
// MAGIC The resulting html can be displayed in databricks using the `displayHTML` function.
// MAGIC 
// MAGIC See http://leafletjs.com/examples/geojson.html for a detailed example of using GeoJson with Leaflet.

// COMMAND ----------

// DBTITLE 1,Leaflet Helper Function - Click Play button! 
def genLeafletHTML(features: Array[String]): String = {

  val featureArray = features.reduce(_ + "," +  _)
  // get your own access-token from https://leafletjs.com/examples/quick-start/
  // see request-access token link above at: https://account.mapbox.com/auth/signin/?route-to=%22/access-tokens/%22
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
// MAGIC ## 3. Load Uber Data as in earlier analysis. 
// MAGIC 
// MAGIC Then convert to an RDD for mapmatching

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

uberData.count()

// COMMAND ----------

uberData.show(5,false)

// COMMAND ----------

val uberOSMMapBoundingBoxFiltered = uberData
                                      .filter($"latlon"(0) >= minLatInOSMMap &&
                                              $"latlon"(0) <= maxLatInOSMMap &&
                                              $"latlon"(1) >= minLonInOSMMap &&
                                              $"latlon"(1) <= maxLonInOSMMap)
                                      .cache()
uberOSMMapBoundingBoxFiltered.count()

// COMMAND ----------

uberOSMMapBoundingBoxFiltered.show(5,false)

// COMMAND ----------

// MAGIC %md
// MAGIC The number of trajectory points that are not within our bounding box of the OSM is:

// COMMAND ----------

uberData.count() - uberOSMMapBoundingBoxFiltered.count()

// COMMAND ----------

// MAGIC %md
// MAGIC We will consider a trip to be invalid when it contains less that two data points, as this is required by Graph Hopper. First identify the all trips that are valid.

// COMMAND ----------

val uberCountsFiltered = uberOSMMapBoundingBoxFiltered
                          .groupBy($"tripId".alias("validTripId"))
                          .count.filter($"count" > 1)
                          .drop("count")

// COMMAND ----------

uberCountsFiltered.show(5, false)

// COMMAND ----------

// MAGIC %md
// MAGIC Next is to join this list of valid Ids with the original data set, only the entries for those trips contained in `uberCountsFiltered`.

// COMMAND ----------

val uberValidData = uberOSMMapBoundingBoxFiltered
  .join(uberCountsFiltered, uberOSMMapBoundingBoxFiltered("tripId") === uberCountsFiltered("validTripId")) // Only want trips with more than 2 data points
  .drop("validTripId").cache 

// COMMAND ----------

// MAGIC %md 
// MAGIC Now seeing how many data points were dropped:

// COMMAND ----------

uberOSMMapBoundingBoxFiltered.count - uberValidData.count

// COMMAND ----------

uberValidData.show(5,false)

// COMMAND ----------

// MAGIC %md 
// MAGIC Graphopper considers a trip to be a sequence of (latitude, longitude, time) tuples. First the relevant columns are selected from the DataFrame, and then the rows are mapped to key-value pairs with the tripId as the key. After this is done the `reduceByKey` step merges all the (lat, lon, time) arrays for each key (trip Id) so that there is one entry for each trip id containing all the relevant data points.

// COMMAND ----------

// To use sql api instead of rdd api
// val ubers = uberValidData.
//   select($"tripId", struct($"latlon"(0), $"latLon"(1), $"timeStamp").as("coord"))
//   .groupBy($"tripId")
//   .agg(collect_set("coord").as("coords"))

// COMMAND ----------

val ubers = uberValidData.select($"tripId", $"latlon", $"timeStamp")
  .map( row => {
        val id = row.get(0).asInstanceOf[Integer]
        val time = row.get(2).asInstanceOf[java.sql.Timestamp].getTime
        // Array(lat, lon)
        val latlon = row.get(1).asInstanceOf[scala.collection.mutable.WrappedArray[Double]] 
        val entry = Array((latlon(0), latlon(1), time))
        (id, entry)
        }
      )
.rdd.reduceByKey( (e1, e2) => e1 ++ e2) // Sequence of timespace tuples
.cache

// COMMAND ----------

ubers.count

// COMMAND ----------

ubers.take(1) // first of 8,321 trip ids prepped and ready for map-matching

// COMMAND ----------

// MAGIC %md 
// MAGIC ## 4. Start Map Matching

// COMMAND ----------

// MAGIC %md 
// MAGIC Now stepping into GraphHopper land we first define some utility functions for interfacing with the GraphHopper map matching library.
// MAGIC Attaching the following artefact:
// MAGIC - `com.graphhopper:map-matching:0.6.0`

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

//%fs ls /datasets/graphhopper/graphHopperData

// COMMAND ----------

// MAGIC %md 
// MAGIC This function returns a new GrapHopper object, with all settings defined and reading the graph from the location in dbfs. __Note__:  `setAllowWrites(false)` ensures that multiple GraphHopper objects can read from the same files simultaneously. 

// COMMAND ----------

def getHopper = {
    val enc = new CarFlagEncoder() // Vehicle type
    val hopp = new GraphHopper()
    .setStoreOnFlush(true)
    .setCHWeightings("shortest")    // Contraction Hierarchy settings
    .setAllowWrites(false)         // Avoids issues when reading graph object fom HDFS
    .setGraphHopperLocation("/dbfs/files/graphhopper/graphHopperData")
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

// MAGIC %md
// MAGIC 
// MAGIC Let's recall this most useful transformation first!
// MAGIC 
// MAGIC ## mapPartitions
// MAGIC 
// MAGIC Return a new RDD by applying a function to each partition of the RDD.
// MAGIC 
// MAGIC %md
// MAGIC ![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/visualapi/med/visualapi-47.png)

// COMMAND ----------

// let's look at a simple exmaple of mapPartitions in action
val x = sc.parallelize(Array(1,2,3), 2) // RDD with 2 partitions

// COMMAND ----------

// our baby function we will call
def f(i:Iterator[Int])={ (i.sum, 42).productIterator }

// COMMAND ----------

val y = x.mapPartitions(f)

// COMMAND ----------

// glom() flattens elements on the same partition
val xOut = x.glom().collect()

// COMMAND ----------

val yOut = y.glom().collect() // we can see the mapPartitions with f applied to each partition

// COMMAND ----------

// MAGIC %md
// MAGIC Having understood the basic power of `mapPartitions` transformation, let's get back to map-matching problem at hand.

// COMMAND ----------

val matchTrips = ubers
  .mapPartitions(partition => {
    // Create the map matching object only once for each partition
    val (hopp, enc) = getHopper

    val tripGraph = hopp.getGraphHopperStorage()
    val locationIndex = new LocationIndexMatch(tripGraph,
                                               hopp.getLocationIndex().asInstanceOf[LocationIndexTree])

    val mm = new MapMatching(tripGraph, locationIndex, enc)
    
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

    // Map matching parameters
    // Have not found any documention on what these do, other that comments in source code
    //   mm.setMaxSearchMultiplier(2000)
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

//         val time = hopp.route(req).getTime -- using new method
        val time = hopp.route(req).getBest.getTime
        time
      }
    
    val timeOffsets = times.scanLeft(0.toLong){ (a: Long, b: Long) => a + b }.toList
    
    (key, points.zip(timeOffsets)) // Return a tuple of (key, Array((lat, lon), timeOffSetFromStart))
  }}
  
  matchedPartition
}).cache

// COMMAND ----------

display(matchTrips.toDF.limit(2))

// COMMAND ----------

// Define the schema of the points in a map matched trip
case class UberMatched(id: Int, lat: Double, lon: Double, time: Long) 

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

matchTripsDF.groupBy($"id").count.orderBy(-$"count").show(10)

// COMMAND ----------

// MAGIC %md 
// MAGIC Finally it is helpful to be able to visualise the results of the map matching.
// MAGIC 
// MAGIC These next few steps take the map matched trips and convert them into json using the Spray-Json library. See [here](https://github.com/spray/spray-json) for documentation on the library.

// COMMAND ----------

// MAGIC %md 
// MAGIC To make the visualisation less clutterd only two trips will be selected. Though little would have to be done to extend this to multiple/all of the trajectories.
// MAGIC 
// MAGIC Here we select only those points that belong to the trip with id `11721` and id `10858`, it is selected only because it contains the most points after map matching. 

// COMMAND ----------

val filterTrips = matchTrips.filter{case (id, values) => id == 11721 || id == 10858 }.cache

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

val originalTraj = uberData.filter($"tripId" === 11721 || $"tripId" == 10858)
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

displayHTML(trajHTML) // zoom and play - orange dots are raw and azure dots are map-matched

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
// MAGIC # Step 0. 
// MAGIC ## Do steps 0.1 and 0.2 once in a given shard per OSM Map

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 0.1: Loading our OSM Data 
// MAGIC 
// MAGIC ### (_Only needs to be done once per OSM Map_)
// MAGIC 
// MAGIC See [https://download.bbbike.org/osm/bbbike/SanFrancisco/](https://download.bbbike.org/osm/bbbike/SanFrancisco/) to download the `pbf` format of the OSM.

// COMMAND ----------

// MAGIC %sh 
// MAGIC #curl -O https://download.bbbike.org/osm/bbbike/SanFrancisco/SanFrancisco.osm.pbf

// COMMAND ----------

// MAGIC %md
// MAGIC The below osm.pbf file was downloaded deom the above link as Marina pointed out yesterday and received via email:
// MAGIC 
// MAGIC ```
// MAGIC your requested OpenStreetMap area 'San Francisco' was extracted from planet.osm
// MAGIC To download the file, please click on the following link:
// MAGIC 
// MAGIC   https://download.bbbike.org/osm/extract/planet_-122.529,37.724_-122.352,37.811.osm.pbf
// MAGIC 
// MAGIC The file will be available for the next 48 hours. Please download the
// MAGIC file as soon as possible.
// MAGIC 
// MAGIC  Name: San Francisco
// MAGIC  Coordinates: -122.529,37.724 x -122.352,37.811
// MAGIC  Script URL: https://extract.bbbike.org/?sw_lng=-122.529&sw_lat=37.724&ne_lng=-122.352&ne_lat=37.811&format=osm.pbf&city=San%20Francisco
// MAGIC  Square kilometre: 150
// MAGIC  Granularity: 100 (1.1 cm)
// MAGIC  Format: osm.pbf
// MAGIC  File size: 8.5 MB
// MAGIC  SHA256 checksum: 8fe277a3b23ebd5a612d21cc50a5287bae3a169867c631353e9a1da3963cd617
// MAGIC  MD5 checksum: 9d2c5650547623bbca1656db84efeb7d
// MAGIC  Last planet.osm database update: Thu May  3 05:46:46 2018 UTC
// MAGIC  License: OpenStreetMap License
// MAGIC 
// MAGIC Please read the extract online help for more informations:
// MAGIC https://extract.bbbike.org/extract.html
// MAGIC ```
// MAGIC 
// MAGIC and the much smaller map has these details:
// MAGIC 
// MAGIC ```
// MAGIC your requested OpenStreetMap area 'San Francisco' was extracted from planet.osm
// MAGIC To download the file, please click on the following link:
// MAGIC 
// MAGIC   https://download.bbbike.org/osm/extract/planet_-122.449,37.747_-122.397,37.772.osm.pbf
// MAGIC 
// MAGIC The file will be available for the next 48 hours. Please download the
// MAGIC file as soon as possible.
// MAGIC 
// MAGIC  Name: San Francisco
// MAGIC  Coordinates: -122.449,37.747 x -122.397,37.772
// MAGIC  Script URL: https://extract.bbbike.org/?sw_lng=-122.449&sw_lat=37.747&ne_lng=-122.397&ne_lat=37.772&format=osm.pbf&city=San%20Francisco
// MAGIC  Square kilometre: 12
// MAGIC  Granularity: 100 (1.1 cm)
// MAGIC  Format: osm.pbf
// MAGIC  File size: 1.3 MB
// MAGIC  SHA256 checksum: 4fa2c4137e9eabdacc840ebcd9f741470c617c43d4d852d528e1baa44d2fb190
// MAGIC  MD5 checksum: 38f2954459efa8d95f65a16f844adebf
// MAGIC ```

// COMMAND ----------

// MAGIC %sh # smaller SF osm.pbf file as the driver crashes with the above larger map
// MAGIC  curl -O https://download.bbbike.org/osm/bbbike/SanFrancisco/SanFrancisco.osm.pbf # nearly 17MB and too big for community edition...
// MAGIC #curl -O https://download.bbbike.org/osm/extract/planet_-122.529,37.724_-122.352,37.811.osm.pbf
// MAGIC #curl -O https://download.bbbike.org/osm/extract/planet_-122.449,37.747_-122.397,37.772.osm.gz # much smaller map of SF
// MAGIC # backups in progress here... http://lamastex.org/.../SanFrancisco_-122.529_37.724__-122.352_37.811.osm.pbf

// COMMAND ----------

// MAGIC %sh 
// MAGIC ls

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/files/graphhopper/osm/")

// COMMAND ----------

//dbutils.fs.rm("dbfs:/datasets/graphhopper/osm/SanFrancisco.osm.pbf",recurse=true) // to remove any pre-existing file with same name in dbfs

// COMMAND ----------

dbutils.fs.mv("file:/databricks/driver/SanFrancisco.osm.pbf", "dbfs:/files/graphhopper/osm/SanFrancisco.osm.pbf") // too big for driver memory
//dbutils.fs.mv("file:/databricks/driver/planet_-122.529,37.724_-122.352,37.811.osm.pbf", "dbfs:/datasets/graphhopper/osm/SanFrancisco.osm.pbf")
//dbutils.fs.mv("file:/databricks/driver/planet_-122.449,37.747_-122.397,37.772.osm.gz", "dbfs:/files/graphhopper/osm/SanFranciscoSmall.osm.gz")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/files/graphhopper/osm"))

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/files/graphhopper/graphHopperData") // Where graphhopper will store its data

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 0.2: Initialising GraphHopper  
// MAGIC 
// MAGIC ### (_Only needs to be done once per shard for each OSM file_)

// COMMAND ----------

// MAGIC %md 
// MAGIC Process an OSM file, creating from it a GraphHopper Graph. The contents of this graph are then stored in the distributed filesystem to be accessed for later use.
// MAGIC This ensures that the processing step only takes place once, and subsequent GraphHopper objects can simply read these files to start map matching.

// COMMAND ----------

val osmPath = "/dbfs/files/graphhopper/osm/SanFrancisco.osm.pbf"
val graphHopperPath = "/dbfs/files/graphhopper/graphHopperData"

// COMMAND ----------

val encoder = new CarFlagEncoder()
 
val hopper = new GraphHopper()
      .setStoreOnFlush(true)
      .setEncodingManager(new EncodingManager(encoder))
      .setOSMFile(osmPath)
      .setCHWeightings("shortest")
      .setGraphHopperLocation("graphhopper/")

hopper.importOrLoad()

// COMMAND ----------

// MAGIC %md 
// MAGIC Move the GraphHopper object to dbfs:

// COMMAND ----------

dbutils.fs.mv("file:/databricks/driver/graphhopper", "dbfs:/files/graphhopper/graphHopperData", recurse=true)

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/files/graphhopper/graphHopperData"))

// COMMAND ----------

