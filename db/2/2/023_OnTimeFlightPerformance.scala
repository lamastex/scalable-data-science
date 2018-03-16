// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/FS6FdwNJDvY/0.jpg)](https://www.youtube.com/embed/FS6FdwNJDvY?start=2035&end=2276&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC This is a scala version of the python notebook in the following talk:
// MAGIC 
// MAGIC **Homework:**
// MAGIC 
// MAGIC See [https://www.brighttalk.com/webcast/12891/199003](https://www.brighttalk.com/webcast/12891/199003) (you need to subscribe *freely* to Bright Talk first).
// MAGIC Then go through this scala version of the notebook from the talk.

// COMMAND ----------

// MAGIC %md 
// MAGIC # On-Time Flight Performance with GraphFrames for Apache Spark
// MAGIC This notebook provides an analysis of On-Time Flight Performance and Departure Delays data using GraphFrames for Apache Spark.
// MAGIC 
// MAGIC Source Data: 
// MAGIC 
// MAGIC * [OpenFlights: Airport, airline and route data](http://openflights.org/data.html)
// MAGIC * [United States Department of Transportation: Bureau of Transportation Statistics (TranStats)](http://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=236&DB_Short_Name=On-Time)
// MAGIC  * Note, the data used here was extracted from the US DOT:BTS between 1/1/2014 and 3/31/2014*
// MAGIC 
// MAGIC References:
// MAGIC 
// MAGIC * [GraphFrames User Guide](http://graphframes.github.io/user-guide.html)
// MAGIC * [GraphFrames: DataFrame-based Graphs (GitHub)](https://github.com/graphframes/graphframes)
// MAGIC * [D3 Airports Example](http://mbostock.github.io/d3/talk/20111116/airports.html)

// COMMAND ----------

// MAGIC %md 
// MAGIC ### Preparation
// MAGIC Extract the Airports and Departure Delays information from S3 / DBFS

// COMMAND ----------

// Set File Paths
val tripdelaysFilePath = "/databricks-datasets/flights/departuredelays.csv"
val airportsnaFilePath = "/databricks-datasets/flights/airport-codes-na.txt"

// COMMAND ----------

// Obtain airports dataset
// Note that "spark-csv" package is built-in datasource in Spark 2.0
val airportsna = sqlContext.read.format("com.databricks.spark.csv").
  option("header", "true").
  option("inferschema", "true").
  option("delimiter", "\t").
  load(airportsnaFilePath)

airportsna.createOrReplaceTempView("airports_na")

// Obtain departure Delays data
val departureDelays = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").load(tripdelaysFilePath)
departureDelays.createOrReplaceTempView("departureDelays")
departureDelays.cache()

// Available IATA (International Air Transport Association) codes from the departuredelays sample dataset
val tripIATA = sqlContext.sql("select distinct iata from (select distinct origin as iata from departureDelays union all select distinct destination as iata from departureDelays) a")
tripIATA.createOrReplaceTempView("tripIATA")

// Only include airports with atleast one trip from the departureDelays dataset
val airports = sqlContext.sql("select f.IATA, f.City, f.State, f.Country from airports_na f join tripIATA t on t.IATA = f.IATA")
airports.createOrReplaceTempView("airports")
airports.cache()

// COMMAND ----------

// Build `departureDelays_geo` DataFrame
// Obtain key attributes such as Date of flight, delays, distance, and airport information (Origin, Destination)  
val departureDelays_geo = sqlContext.sql("select cast(f.date as int) as tripid, cast(concat(concat(concat(concat(concat(concat('2014-', concat(concat(substr(cast(f.date as string), 1, 2), '-')), substr(cast(f.date as string), 3, 2)), ' '), substr(cast(f.date as string), 5, 2)), ':'), substr(cast(f.date as string), 7, 2)), ':00') as timestamp) as `localdate`, cast(f.delay as int), cast(f.distance as int), f.origin as src, f.destination as dst, o.city as city_src, d.city as city_dst, o.state as state_src, d.state as state_dst from departuredelays f join airports o on o.iata = f.origin join airports d on d.iata = f.destination") 

// RegisterTempTable
departureDelays_geo.createOrReplaceTempView("departureDelays_geo")

// Cache and Count
departureDelays_geo.cache()
departureDelays_geo.count()

// COMMAND ----------

display(departureDelays_geo)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Building the Graph
// MAGIC Now that we've imported our data, we're going to need to build our graph. To do so we're going to do two things. We are going to build the structure of the vertices (or nodes) and we're going to build the structure of the edges. What's awesome about GraphFrames is that this process is incredibly simple. 
// MAGIC 
// MAGIC * Rename IATA airport code to **id** in the Vertices Table
// MAGIC * Start and End airports to **src** and **dst** for the Edges Table (flights)
// MAGIC 
// MAGIC These are required naming conventions for vertices and edges in GraphFrames as of the time of this writing (Feb. 2016).

// COMMAND ----------

// MAGIC %md 
// MAGIC **WARNING:** If the graphframes package, required in the cell below, is not installed, follow the instructions [here](http://cdn2.hubspot.net/hubfs/438089/notebooks/help/Setup_graphframes_package.html).

// COMMAND ----------

// Note, ensure you have already installed the GraphFrames spack-package
import org.apache.spark.sql.functions._
import org.graphframes._

// Create Vertices (airports) and Edges (flights)
val tripVertices = airports.withColumnRenamed("IATA", "id").distinct()
val tripEdges = departureDelays_geo.select("tripid", "delay", "src", "dst", "city_dst", "state_dst")

// Cache Vertices and Edges
tripEdges.cache()
tripVertices.cache()

// COMMAND ----------

// Vertices
// The vertices of our graph are the airports
display(tripVertices)

// COMMAND ----------

// Edges
// The edges of our graph are the flights between airports
display(tripEdges)

// COMMAND ----------

// Build `tripGraph` GraphFrame
// This GraphFrame builds up on the vertices and edges based on our trips (flights)
val tripGraph = GraphFrame(tripVertices, tripEdges)
println(tripGraph)

// Build `tripGraphPrime` GraphFrame
// This graphframe contains a smaller subset of data to make it easier to display motifs and subgraphs (below)
val tripEdgesPrime = departureDelays_geo.select("tripid", "delay", "src", "dst")
val tripGraphPrime = GraphFrame(tripVertices, tripEdgesPrime)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Simple Queries
// MAGIC Let's start with a set of simple graph queries to understand flight performance and departure delays

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Determine the number of airports and trips

// COMMAND ----------

println(s"Airports: ${tripGraph.vertices.count()}")
println(s"Trips: ${tripGraph.edges.count()}")

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Determining the longest delay in this dataset

// COMMAND ----------

// Finding the longest Delay
val longestDelay = tripGraph.edges.groupBy().max("delay")
display(longestDelay)

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Determining the number of delayed vs. on-time / early flights

// COMMAND ----------

// Determining number of on-time / early flights vs. delayed flights
println(s"On-time / Early Flights: ${tripGraph.edges.filter("delay <= 0").count()}")
println(s"Delayed Flights: ${tripGraph.edges.filter("delay > 0").count()}")

// COMMAND ----------

// MAGIC %md 
// MAGIC #### What flights departing SFO are most likely to have significant delays
// MAGIC Note, delay can be <= 0 meaning the flight left on time or early

// COMMAND ----------

val sfoDelayedTrips = tripGraph.edges.
  filter("src = 'SFO' and delay > 0").
  groupBy("src", "dst").
  avg("delay").
  sort(desc("avg(delay)"))

// COMMAND ----------

display(sfoDelayedTrips)

// COMMAND ----------

// MAGIC %md 
// MAGIC #### What destinations tend to have delays

// COMMAND ----------

// After displaying tripDelays, use Plot Options to set `state_dst` as a Key.
val tripDelays = tripGraph.edges.filter($"delay" > 0)
display(tripDelays)

// COMMAND ----------

// MAGIC %md 
// MAGIC #### What destinations tend to have significant delays departing from SEA

// COMMAND ----------

// States with the longest cumulative delays (with individual delays > 100 minutes) (origin: Seattle)
display(tripGraph.edges.filter($"src" === "SEA" && $"delay" > 100))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Vertex Degrees
// MAGIC 
// MAGIC * `inDegrees`: Incoming connections to the airport
// MAGIC * `outDegrees`: Outgoing connections from the airport 
// MAGIC * `degrees`: Total connections to and from the airport
// MAGIC 
// MAGIC Reviewing the various properties of the property graph to understand the incoming and outgoing connections between airports.

// COMMAND ----------

// Degrees
// The number of degrees - the number of incoming and outgoing connections - for various airports within this sample dataset
display(tripGraph.degrees.sort($"degree".desc).limit(20))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## City / Flight Relationships through Motif Finding
// MAGIC To more easily understand the complex relationship of city airports and their flights with each other, we can use motifs to find patterns of airports (i.e. vertices) connected by flights (i.e. edges). The result is a DataFrame in which the column names are given by the motif keys.

// COMMAND ----------

// MAGIC %md 
// MAGIC #### What delays might we blame on SFO

// COMMAND ----------

/*
Using tripGraphPrime to more easily display 
- The associated edge (ab, bc) relationships 
- With the different the city / airports (a, b, c) where SFO is the connecting city (b)
- Ensuring that flight ab (i.e. the flight to SFO) occured before flight bc (i.e. flight leaving SFO)
- Note, TripID was generated based on time in the format of MMDDHHMM converted to int
- Therefore bc.tripid < ab.tripid + 10000 means the second flight (bc) occured within approx a day of the first flight (ab)
Note: In reality, we would need to be more careful to link trips ab and bc.
*/
val motifs = tripGraphPrime.
  find("(a)-[ab]->(b); (b)-[bc]->(c)").
  filter("(b.id = 'SFO') and (ab.delay > 500 or bc.delay > 500) and bc.tripid > ab.tripid and bc.tripid < ab.tripid + 10000")

display(motifs)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Determining Airport Ranking using PageRank
// MAGIC There are a large number of flights and connections through these various airports included in this Departure Delay Dataset.  Using the `pageRank` algorithm, Spark iteratively traverses the graph and determines a rough estimate of how important the airport is.

// COMMAND ----------

// Determining Airport ranking of importance using `pageRank`
val ranks = tripGraph.pageRank.resetProbability(0.15).maxIter(5).run()

// COMMAND ----------

display(ranks.vertices.orderBy($"pagerank".desc).limit(20))

// COMMAND ----------

// MAGIC %md
// MAGIC BTW, A lot more delicate air-traffic arithmetic is possible for a full month of airplane co-trajectories over the radar range of Atlanta, Georgia!
// MAGIC 
// MAGIC See for instance:
// MAGIC 
// MAGIC * Statistical regular pavings to analyze massive data of aircraft trajectories, Gloria Teng, Kenneth Kuhn and Raazesh Sainudiin, Journal of Aerospace Computing, Information, and Communication, Vol. 9, No. 1, pp. 14-25, [doi: 10.2514/1.I010015](http://arc.aiaa.org/doi/abs/10.2514/1.I010015), 2012. See free preprint: [http://lamastex.org/preprints/AAIASubPavingATC.pdf](http://lamastex.org/preprints/AAIASubPavingATC.pdf).

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Most popular flights (single city hops)
// MAGIC Using the `tripGraph`, we can quickly determine what are the most popular single city hop flights

// COMMAND ----------

// Determine the most popular flights (single city hops)
import org.apache.spark.sql.functions._

val topTrips = tripGraph.edges.
  groupBy("src", "dst").
  agg(count("delay").as("trips"))

// COMMAND ----------

// Show the top 20 most popular flights (single city hops)
display(topTrips.orderBy($"trips".desc).limit(20))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Top Transfer Cities
// MAGIC Many airports are used as transfer points instead of the final Destination.  An easy way to calculate this is by calculating the ratio of inDegree (the number of flights to the airport) / outDegree (the number of flights leaving the airport).  Values close to 1 may indicate many transfers, whereas values < 1 indicate many outgoing flights and > 1 indicate many incoming flights.  Note, this is a simple calculation that does not take into account of timing or scheduling of flights, just the overall aggregate number within the dataset.

// COMMAND ----------

// Calculate the inDeg (flights into the airport) and outDeg (flights leaving the airport)
val inDeg = tripGraph.inDegrees
val outDeg = tripGraph.outDegrees

// Calculate the degreeRatio (inDeg/outDeg), perform inner join on "id" column
val degreeRatio = inDeg.join(outDeg, inDeg("id") === outDeg("id")).
  drop(outDeg("id")).
  selectExpr("id", "double(inDegree)/double(outDegree) as degreeRatio").
  cache()

// Join back to the `airports` DataFrame (instead of registering temp table as above)
val nonTransferAirports = degreeRatio.join(airports, degreeRatio("id") === airports("IATA")).
  selectExpr("id", "city", "degreeRatio").
  filter("degreeRatio < 0.9 or degreeRatio > 1.1")

// List out the city airports which have abnormal degree ratios
display(nonTransferAirports)

// COMMAND ----------

// Join back to the `airports` DataFrame (instead of registering temp table as above)
val transferAirports = degreeRatio.join(airports, degreeRatio("id") === airports("IATA")).
  selectExpr("id", "city", "degreeRatio").
  filter("degreeRatio between 0.9 and 1.1")
  
// List out the top 10 transfer city airports
display(transferAirports.orderBy("degreeRatio").limit(10))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Breadth First Search 
// MAGIC Breadth-first search (BFS) is designed to traverse the graph to quickly find the desired vertices (i.e. airports) and edges (i.e flights).  Let's try to find the shortest number of connections between cities based on the dataset.  Note, these examples do not take into account of time or distance, just hops between cities.

// COMMAND ----------

// Example 1: Direct Seattle to San Francisco
// This method returns a DataFrame of valid shortest paths from vertices matching "fromExpr" to vertices matching "toExpr"
val filteredPaths = tripGraph.bfs.fromExpr((col("id") === "SEA")).toExpr(col("id") === "SFO").maxPathLength(1).run()
display(filteredPaths)

// COMMAND ----------

// MAGIC %md 
// MAGIC As you can see, there are a number of direct flights between Seattle and San Francisco.

// COMMAND ----------

// Example 2: Direct San Francisco and Buffalo
// You can also specify expression as a String, instead of Column
val filteredPaths = tripGraph.bfs.fromExpr("id = 'SFO'").toExpr("id = 'BUF'").maxPathLength(1).run()

// COMMAND ----------

filteredPaths.show()

// COMMAND ----------

display(filteredPaths)

// COMMAND ----------

// MAGIC %md 
// MAGIC But there are no direct flights between San Francisco and Buffalo.

// COMMAND ----------

// Example 2a: Flying from San Francisco to Buffalo
val filteredPaths = tripGraph.bfs.fromExpr("id = 'SFO'").toExpr("id = 'BUF'").maxPathLength(2).run()
display(filteredPaths)

// COMMAND ----------

// MAGIC %md 
// MAGIC But there are flights from San Francisco to Buffalo with Minneapolis as the transfer point.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Loading the D3 Visualization
// MAGIC Using the airports D3 visualization to visualize airports and flight paths

// COMMAND ----------

package d3a
// We use a package object so that we can define top level classes like Edge that need to be used in other cells

import org.apache.spark.sql._
import com.databricks.backend.daemon.driver.EnhancedRDDFunctions.displayHTML

case class Edge(src: String, dest: String, count: Long)

case class Node(name: String)
case class Link(source: Int, target: Int, value: Long)
case class Graph(nodes: Seq[Node], links: Seq[Link])

object graphs {
val sqlContext = SparkSession.builder().getOrCreate().sqlContext
import sqlContext.implicits._

def force(clicks: Dataset[Edge], height: Int = 100, width: Int = 960): Unit = {
  val data = clicks.collect()
  val nodes = (data.map(_.src) ++ data.map(_.dest)).map(_.replaceAll("_", " ")).toSet.toSeq.map(Node)
  val links = data.map { t =>
    Link(nodes.indexWhere(_.name == t.src.replaceAll("_", " ")), nodes.indexWhere(_.name == t.dest.replaceAll("_", " ")), t.count / 20 + 1)
  }
  showGraph(height, width, Seq(Graph(nodes, links)).toDF().toJSON.first())
}

/**
 * Displays a force directed graph using d3
 * input: {"nodes": [{"name": "..."}], "links": [{"source": 1, "target": 2, "value": 0}]}
 */
def showGraph(height: Int, width: Int, graph: String): Unit = {

displayHTML(s"""<!DOCTYPE html>
<html>
  <head>
    <link type="text/css" rel="stylesheet" href="https://mbostock.github.io/d3/talk/20111116/style.css"/>
    <style type="text/css">
      #states path {
        fill: #ccc;
        stroke: #fff;
      }

      path.arc {
        pointer-events: none;
        fill: none;
        stroke: #000;
        display: none;
      }

      path.cell {
        fill: none;
        pointer-events: all;
      }

      circle {
        fill: steelblue;
        fill-opacity: .8;
        stroke: #fff;
      }

      #cells.voronoi path.cell {
        stroke: brown;
      }

      #cells g:hover path.arc {
        display: inherit;
      }
    </style>
  </head>
  <body>
    <script src="https://mbostock.github.io/d3/talk/20111116/d3/d3.js"></script>
    <script src="https://mbostock.github.io/d3/talk/20111116/d3/d3.csv.js"></script>
    <script src="https://mbostock.github.io/d3/talk/20111116/d3/d3.geo.js"></script>
    <script src="https://mbostock.github.io/d3/talk/20111116/d3/d3.geom.js"></script>
    <script>
      var graph = $graph;
      var w = $width;
      var h = $height;

      var linksByOrigin = {};
      var countByAirport = {};
      var locationByAirport = {};
      var positions = [];

      var projection = d3.geo.azimuthal()
          .mode("equidistant")
          .origin([-98, 38])
          .scale(1400)
          .translate([640, 360]);

      var path = d3.geo.path()
          .projection(projection);

      var svg = d3.select("body")
          .insert("svg:svg", "h2")
          .attr("width", w)
          .attr("height", h);

      var states = svg.append("svg:g")
          .attr("id", "states");

      var circles = svg.append("svg:g")
          .attr("id", "circles");

      var cells = svg.append("svg:g")
          .attr("id", "cells");

      var arc = d3.geo.greatArc()
          .source(function(d) { return locationByAirport[d.source]; })
          .target(function(d) { return locationByAirport[d.target]; });

      d3.select("input[type=checkbox]").on("change", function() {
        cells.classed("voronoi", this.checked);
      });

      // Draw US map.
      d3.json("https://mbostock.github.io/d3/talk/20111116/us-states.json", function(collection) {
        states.selectAll("path")
          .data(collection.features)
          .enter().append("svg:path")
          .attr("d", path);
      });

      // Parse links
      graph.links.forEach(function(link) {
        var origin = graph.nodes[link.source].name;
        var destination = graph.nodes[link.target].name;

        var links = linksByOrigin[origin] || (linksByOrigin[origin] = []);
        links.push({ source: origin, target: destination });

        countByAirport[origin] = (countByAirport[origin] || 0) + 1;
        countByAirport[destination] = (countByAirport[destination] || 0) + 1;
      });

      d3.csv("https://mbostock.github.io/d3/talk/20111116/airports.csv", function(data) {

        // Build list of airports.
        var airports = graph.nodes.map(function(node) {
          return data.find(function(airport) {
            if (airport.iata === node.name) {
              var location = [+airport.longitude, +airport.latitude];
              locationByAirport[airport.iata] = location;
              positions.push(projection(location));

              return true;
            } else {
              return false;
            }
          });
        });

        // Compute the Voronoi diagram of airports' projected positions.
        var polygons = d3.geom.voronoi(positions);

        var g = cells.selectAll("g")
            .data(airports)
          .enter().append("svg:g");

        g.append("svg:path")
            .attr("class", "cell")
            .attr("d", function(d, i) { return "M" + polygons[i].join("L") + "Z"; })
            .on("mouseover", function(d, i) { d3.select("h2 span").text(d.name); });

        g.selectAll("path.arc")
            .data(function(d) { return linksByOrigin[d.iata] || []; })
          .enter().append("svg:path")
            .attr("class", "arc")
            .attr("d", function(d) { return path(arc(d)); });

        circles.selectAll("circle")
            .data(airports)
            .enter().append("svg:circle")
            .attr("cx", function(d, i) { return positions[i][0]; })
            .attr("cy", function(d, i) { return positions[i][1]; })
            .attr("r", function(d, i) { return Math.sqrt(countByAirport[d.iata]); })
            .sort(function(a, b) { return countByAirport[b.iata] - countByAirport[a.iata]; });
      });
    </script>
  </body>
</html>""")
  }

  def help() = {
displayHTML("""
<p>
Produces a force-directed graph given a collection of edges of the following form:</br>
<tt><font color="#a71d5d">case class</font> <font color="#795da3">Edge</font>(<font color="#ed6a43">src</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">dest</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">count</font>: <font color="#a71d5d">Long</font>)</tt>
</p>
<p>Usage:<br/>
<tt>%scala</tt></br>
<tt><font color="#a71d5d">import</font> <font color="#ed6a43">d3._</font></tt><br/>
<tt><font color="#795da3">graphs.force</font>(</br>
&nbsp;&nbsp;<font color="#ed6a43">height</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">width</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">clicks</font>: <font color="#795da3">Dataset</font>[<font color="#795da3">Edge</font>])</tt>
</p>""")
  }
}

// COMMAND ----------

d3a.graphs.help()

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Visualize On-time and Early Arrivals

// COMMAND ----------

// On-time and Early Arrivals
import d3a._

graphs.force(
  height = 800,
  width = 1200,
  clicks = sql("select src, dst as dest, count(1) as count from departureDelays_geo where delay <= 0 group by src, dst").as[Edge])

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Visualize Delayed Trips Departing from the West Coast
// MAGIC 
// MAGIC Notice that most of the delayed trips are with Western US cities

// COMMAND ----------

// Delayed Trips from CA, OR, and/or WA
import d3a._

graphs.force(
  height = 800,
  width = 1200,
  clicks = sql("""select src, dst as dest, count(1) as count from departureDelays_geo where state_src in ('CA', 'OR', 'WA') and delay > 0 group by src, dst""").as[Edge])

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Visualize All Flights (from this dataset)

// COMMAND ----------

// Trips (from DepartureDelays Dataset)
import d3a._

graphs.force(
  height = 800,
  width = 1200,
  clicks = sql("""select src, dst as dest, count(1) as count from departureDelays_geo group by src, dst""").as[Edge])