// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC 
// MAGIC By [Marina Toger](https://www.linkedin.com/in/marina-toger-29030320/)
// MAGIC 
// MAGIC TODO: Raaz - re-liven for 2021... 

// COMMAND ----------

// MAGIC %md 
// MAGIC ### OSM
// MAGIC 
// MAGIC 1.We define an area of interest and find coordinates of its boundary, AKA "bounding box". 
// MAGIC To do this go to https://www.openstreetmap.org and zoom roughly into the desired area.
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC 2.To ingest data from OSM we use `wget`, in the following format:
// MAGIC 
// MAGIC ``
// MAGIC wget -O MyFileName.osm "https://api.openstreetmap.org/api/0.6/map?bbox=l,b,r,t"``
// MAGIC 
// MAGIC * `MyFileName.osm` - give some informative file name
// MAGIC 
// MAGIC * l = longitude of the LEFT boundary of the bounding box
// MAGIC * b = lattitude of the BOTTOM boundary of the bounding box
// MAGIC * r = longitude of the RIGHT boundary of the bounding box
// MAGIC * t = lattitude of the TOP boundary of the bounding box
// MAGIC 
// MAGIC For instance if you know the bounding box, do: 
// MAGIC 
// MAGIC * `TinyUppsalaCentrumWgot.osm` - Tiny area in Uppsala Centrum
// MAGIC 
// MAGIC * l = `17.63514`
// MAGIC * b = `59.85739`
// MAGIC * r = `17.64154`
// MAGIC * t = `59.86011`
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC wget -O TinyUppsalaCentrumWgot.osm "https://api.openstreetmap.org/api/0.6/map?bbox=17.63514,59.85739,17.64154,59.86011"
// MAGIC ```

// COMMAND ----------

//Imports
import magellan._

// COMMAND ----------

// MAGIC %sh
// MAGIC ls

// COMMAND ----------

// MAGIC %sh
// MAGIC wget -O UppsalaCentrumWgot.osm "https://api.openstreetmap.org/api/0.6/map?bbox=17.6244,59.8464,17.6661,59.8643"

// COMMAND ----------

// MAGIC %sh 
// MAGIC pwd
// MAGIC ls

// COMMAND ----------

display(dbutils.fs.ls("dbfs:///datasets/"))

// COMMAND ----------

// making directory in distributed file system
//dbutils.fs.mkdirs("dbfs:///datasets/maps/")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:///datasets/maps/"))

// COMMAND ----------

//dbutils.fs.rm("file:///databricks/driver/UppsalaCentrumWgot.osm",true)

// COMMAND ----------

// copy file from local fs to dbfs
dbutils.fs.cp("file:///databricks/driver/UppsalaCentrumWgot.osm","dbfs:///datasets/maps/")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:///datasets/maps/"))

// COMMAND ----------

val path = "dbfs:/datasets/maps/UppsalaCentrumWgot.osm"
val uppsalaCentrumOsmDF = spark.read
      .format("magellan")
      .option("type", "osm")
      .load(path)

// COMMAND ----------

uppsalaCentrumOsmDF.show()

// COMMAND ----------

display(uppsalaCentrumOsmDF)

// COMMAND ----------

uppsalaCentrumOsmDF.count

// COMMAND ----------

// MAGIC %sh
// MAGIC wget -O TinyUppsalaCentrumWgot.osm "https://api.openstreetmap.org/api/0.6/map?bbox=17.63514,59.85739,17.64154,59.86011"

// COMMAND ----------

// MAGIC %sh 
// MAGIC pwd
// MAGIC ls

// COMMAND ----------

// copy file from local fs to dbfs
dbutils.fs.cp("file:///databricks/driver/TinyUppsalaCentrumWgot.osm","dbfs:///datasets/maps/")
display(dbutils.fs.ls("dbfs:///datasets/maps/"))

// COMMAND ----------

val path = "dbfs:/datasets/maps/TinyUppsalaCentrumWgot.osm"
val tinyUppsalaCentrumOsmDF = spark.read
      .format("magellan")
      .option("type", "osm")
      .load(path)
display(tinyUppsalaCentrumOsmDF)

// COMMAND ----------

tinyUppsalaCentrumOsmDF.count

// COMMAND ----------

// MAGIC %md
// MAGIC ## Setting up leaflet 
// MAGIC 
// MAGIC You need to go to the following URL and set-up access-token in map-box to use leaflet independently:
// MAGIC 
// MAGIC  - https://leafletjs.com/examples/quick-start/
// MAGIC  - Request access-token:
// MAGIC    - https://account.mapbox.com/auth/signin/?route-to=%22/access-tokens/%22

// COMMAND ----------

// MAGIC %md
// MAGIC #### Visualise with leaflet:
// MAGIC Take an array of Strings in 'GeoJson' format, then insert this into a prebuild html string that contains all the code neccesary to display these features using Leaflet.
// MAGIC The resulting html can be displayed in DataBricks using the displayHTML function.
// MAGIC 
// MAGIC See http://leafletjs.com/examples/geojson.html for a detailed example of using GeoJson with Leaflet.

// COMMAND ----------

//val point1 = sc.parallelize(Seq((59.839264, 17.647075),(59.9, 17.88))).toDF("x", "y")
val point1 = sc.parallelize(Seq((59.839264, 17.647075))).toDF("x", "y")
val point1c = point1.collect()
//val string_p1c = point1.mkString(", ")
val string2 = point1c.mkString(",")
//df.select(columns: _*).collect.map(_.toSeq)
val string22 = "'random_string'"

// COMMAND ----------

def genLeafletHTML(): String = {
  // you should get your own later!
  val accessToken = "pk.eyJ1IjoiZHRnIiwiYSI6ImNpaWF6MGdiNDAwanNtemx6MmIyNXoyOWIifQ.ndbNtExCMXZHKyfNtEN0Vg"

  
  val generatedHTML = f"""<!DOCTYPE html>  
  <html>
  <head>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/leaflet.css">
        <style>#map {width: 600px; height:400px;}</style>
  </head>
  
  <body>
      <div id="map" style="width: 600px; height: 400px"></div>
      <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/leaflet.js"></script>
      <script type="text/javascript">
          var map = L.map('map').setView([59.838, 17.646865], 16);

          L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=$accessToken', {
            maxZoom: 19
            , id: 'mapbox.streets'
            , attribution: '<a href="http://openstreetmap.org">OpenStreetMap</a> ' +
              '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a> ' +
              '| &copy; <a href="http://mapbox.com">Mapbox</a>'
          }).addTo(map);
          
          str1 = 'SDS<br>Ångströmlaboratoriet<br>59.839264, 17.647075<br>';
          str2 = ${string22};
          var popup = str1.concat(str2);
          
          L.marker(${string2}).addTo(map)
              .bindPopup(popup)
              .openPopup();

      </script>
  </body>  
  """
  generatedHTML
}

displayHTML(genLeafletHTML)

// COMMAND ----------

// MAGIC %sh
// MAGIC du -sh derby.log

// COMMAND ----------

// MAGIC %md
// MAGIC # Exercise
// MAGIC Repeat the same with ingesting data fro your own favourite city or a small enough region of earth.