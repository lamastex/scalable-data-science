[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Archived YouTube video of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/FS6FdwNJDvY/0.jpg)](https://www.youtube.com/embed/FS6FdwNJDvY?start=2035&end=2276&autoplay=1)

This is a scala version of the python notebook in the following talk:

**Homework:**

See <https://www.brighttalk.com/webcast/12891/199003> (you need to subscribe *freely* to Bright Talk first).
Then go through this scala version of the notebook from the talk.

On-Time Flight Performance with GraphFrames for Apache Spark
============================================================

This notebook provides an analysis of On-Time Flight Performance and Departure Delays data using GraphFrames for Apache Spark.

Source Data:

-   [OpenFlights: Airport, airline and route data](http://openflights.org/data.html)
-   [United States Department of Transportation: Bureau of Transportation Statistics (TranStats)](http://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=236&DB_Short_Name=On-Time)
-   Note, the data used here was extracted from the US DOT:BTS between 1/1/2014 and 3/31/2014\*

References:

-   [GraphFrames User Guide](http://graphframes.github.io/user-guide.html)
-   [GraphFrames: DataFrame-based Graphs (GitHub)](https://github.com/graphframes/graphframes)
-   [D3 Airports Example](http://mbostock.github.io/d3/talk/20111116/airports.html)

### Preparation

Extract the Airports and Departure Delays information from S3 / DBFS

``` scala
// Set File Paths
val tripdelaysFilePath = "/databricks-datasets/flights/departuredelays.csv"
val airportsnaFilePath = "/databricks-datasets/flights/airport-codes-na.txt"
```

>     tripdelaysFilePath: String = /databricks-datasets/flights/departuredelays.csv
>     airportsnaFilePath: String = /databricks-datasets/flights/airport-codes-na.txt

``` scala
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
```

>     airportsna: org.apache.spark.sql.DataFrame = [City: string, State: string ... 2 more fields]
>     departureDelays: org.apache.spark.sql.DataFrame = [date: string, delay: string ... 3 more fields]
>     tripIATA: org.apache.spark.sql.DataFrame = [iata: string]
>     airports: org.apache.spark.sql.DataFrame = [IATA: string, City: string ... 2 more fields]
>     res0: airports.type = [IATA: string, City: string ... 2 more fields]

``` scala
// Build `departureDelays_geo` DataFrame
// Obtain key attributes such as Date of flight, delays, distance, and airport information (Origin, Destination)  
val departureDelays_geo = sqlContext.sql("select cast(f.date as int) as tripid, cast(concat(concat(concat(concat(concat(concat('2014-', concat(concat(substr(cast(f.date as string), 1, 2), '-')), substr(cast(f.date as string), 3, 2)), ' '), substr(cast(f.date as string), 5, 2)), ':'), substr(cast(f.date as string), 7, 2)), ':00') as timestamp) as `localdate`, cast(f.delay as int), cast(f.distance as int), f.origin as src, f.destination as dst, o.city as city_src, d.city as city_dst, o.state as state_src, d.state as state_dst from departuredelays f join airports o on o.iata = f.origin join airports d on d.iata = f.destination") 

// RegisterTempTable
departureDelays_geo.createOrReplaceTempView("departureDelays_geo")

// Cache and Count
departureDelays_geo.cache()
departureDelays_geo.count()
```

>     departureDelays_geo: org.apache.spark.sql.DataFrame = [tripid: int, localdate: timestamp ... 8 more fields]
>     res3: Long = 1361141

``` scala
display(departureDelays_geo)
```

| tripid    | localdate                    | delay | distance | src | dst | city\_src   | city\_dst           | state\_src | state\_dst |
|-----------|------------------------------|-------|----------|-----|-----|-------------|---------------------|------------|------------|
| 1011111.0 | 2014-01-01T11:11:00.000+0000 | -5.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1021111.0 | 2014-01-02T11:11:00.000+0000 | 7.0   | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1031111.0 | 2014-01-03T11:11:00.000+0000 | 0.0   | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1041925.0 | 2014-01-04T19:25:00.000+0000 | 0.0   | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1061115.0 | 2014-01-06T11:15:00.000+0000 | 33.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1071115.0 | 2014-01-07T11:15:00.000+0000 | 23.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1081115.0 | 2014-01-08T11:15:00.000+0000 | -9.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1091115.0 | 2014-01-09T11:15:00.000+0000 | 11.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1101115.0 | 2014-01-10T11:15:00.000+0000 | -3.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1112015.0 | 2014-01-11T20:15:00.000+0000 | -7.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1121925.0 | 2014-01-12T19:25:00.000+0000 | -5.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1131115.0 | 2014-01-13T11:15:00.000+0000 | -3.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1141115.0 | 2014-01-14T11:15:00.000+0000 | -6.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1151115.0 | 2014-01-15T11:15:00.000+0000 | -7.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1161115.0 | 2014-01-16T11:15:00.000+0000 | -3.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1171115.0 | 2014-01-17T11:15:00.000+0000 | 4.0   | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1182015.0 | 2014-01-18T20:15:00.000+0000 | -5.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1191925.0 | 2014-01-19T19:25:00.000+0000 | -7.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1201115.0 | 2014-01-20T11:15:00.000+0000 | -6.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1211115.0 | 2014-01-21T11:15:00.000+0000 | 0.0   | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1221115.0 | 2014-01-22T11:15:00.000+0000 | -4.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1231115.0 | 2014-01-23T11:15:00.000+0000 | -4.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1241115.0 | 2014-01-24T11:15:00.000+0000 | -3.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1252015.0 | 2014-01-25T20:15:00.000+0000 | -12.0 | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1261925.0 | 2014-01-26T19:25:00.000+0000 | -5.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1271115.0 | 2014-01-27T11:15:00.000+0000 | 0.0   | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1281115.0 | 2014-01-28T11:15:00.000+0000 | -8.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1291115.0 | 2014-01-29T11:15:00.000+0000 | -2.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1301115.0 | 2014-01-30T11:15:00.000+0000 | 0.0   | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |
| 1311115.0 | 2014-01-31T11:15:00.000+0000 | -3.0  | 221.0    | MSP | INL | Minneapolis | International Falls | MN         | MN         |

Truncated to 30 rows

Building the Graph
------------------

Now that we've imported our data, we're going to need to build our graph. To do so we're going to do two things. We are going to build the structure of the vertices (or nodes) and we're going to build the structure of the edges. What's awesome about GraphFrames is that this process is incredibly simple.

-   Rename IATA airport code to **id** in the Vertices Table
-   Start and End airports to **src** and **dst** for the Edges Table (flights)

These are required naming conventions for vertices and edges in GraphFrames as of the time of this writing (Feb. 2016).

**WARNING:** If the graphframes package, required in the cell below, is not installed, follow the instructions [here](http://cdn2.hubspot.net/hubfs/438089/notebooks/help/Setup_graphframes_package.html).

``` scala
// Note, ensure you have already installed the GraphFrames spack-package
import org.apache.spark.sql.functions._
import org.graphframes._

// Create Vertices (airports) and Edges (flights)
val tripVertices = airports.withColumnRenamed("IATA", "id").distinct()
val tripEdges = departureDelays_geo.select("tripid", "delay", "src", "dst", "city_dst", "state_dst")

// Cache Vertices and Edges
tripEdges.cache()
tripVertices.cache()
```

>     import org.apache.spark.sql.functions._
>     import org.graphframes._
>     tripVertices: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [id: string, City: string ... 2 more fields]
>     tripEdges: org.apache.spark.sql.DataFrame = [tripid: int, delay: int ... 4 more fields]
>     res5: tripVertices.type = [id: string, City: string ... 2 more fields]

``` scala
// Vertices
// The vertices of our graph are the airports
display(tripVertices)
```

| id  | City             | State | Country |
|-----|------------------|-------|---------|
| FAT | Fresno           | CA    | USA     |
| CMH | Columbus         | OH    | USA     |
| PHX | Phoenix          | AZ    | USA     |
| PAH | Paducah          | KY    | USA     |
| COS | Colorado Springs | CO    | USA     |
| MYR | Myrtle Beach     | SC    | USA     |
| RNO | Reno             | NV    | USA     |
| SRQ | Sarasota         | FL    | USA     |
| VLD | Valdosta         | GA    | USA     |
| PSC | Pasco            | WA    | USA     |
| BPT | Beaumont         | TX    | USA     |
| CAE | Columbia         | SC    | USA     |
| LAX | Los Angeles      | CA    | USA     |
| DAY | Dayton           | OH    | USA     |
| AVP | Wilkes-Barre     | PA    | USA     |
| MFR | Medford          | OR    | USA     |
| JFK | New York         | NY    | USA     |
| LAS | Las Vegas        | NV    | USA     |
| BNA | Nashville        | TN    | USA     |
| CLT | Charlotte        | NC    | USA     |
| BDL | Hartford         | CT    | USA     |
| ILG | Wilmington       | DE    | USA     |
| ACT | Waco             | TX    | USA     |
| ATW | Appleton         | WI    | USA     |
| RHI | Rhinelander      | WI    | USA     |
| PWM | Portland         | ME    | USA     |
| SJT | San Angelo       | TX    | USA     |
| GRB | Green Bay        | WI    | USA     |
| APN | Alpena           | MI    | USA     |
| MSY | New Orleans      | LA    | USA     |

Truncated to 30 rows

``` scala
// Edges
// The edges of our graph are the flights between airports
display(tripEdges)
```

| tripid    | delay | src | dst | city\_dst           | state\_dst |
|-----------|-------|-----|-----|---------------------|------------|
| 1011111.0 | -5.0  | MSP | INL | International Falls | MN         |
| 1021111.0 | 7.0   | MSP | INL | International Falls | MN         |
| 1031111.0 | 0.0   | MSP | INL | International Falls | MN         |
| 1041925.0 | 0.0   | MSP | INL | International Falls | MN         |
| 1061115.0 | 33.0  | MSP | INL | International Falls | MN         |
| 1071115.0 | 23.0  | MSP | INL | International Falls | MN         |
| 1081115.0 | -9.0  | MSP | INL | International Falls | MN         |
| 1091115.0 | 11.0  | MSP | INL | International Falls | MN         |
| 1101115.0 | -3.0  | MSP | INL | International Falls | MN         |
| 1112015.0 | -7.0  | MSP | INL | International Falls | MN         |
| 1121925.0 | -5.0  | MSP | INL | International Falls | MN         |
| 1131115.0 | -3.0  | MSP | INL | International Falls | MN         |
| 1141115.0 | -6.0  | MSP | INL | International Falls | MN         |
| 1151115.0 | -7.0  | MSP | INL | International Falls | MN         |
| 1161115.0 | -3.0  | MSP | INL | International Falls | MN         |
| 1171115.0 | 4.0   | MSP | INL | International Falls | MN         |
| 1182015.0 | -5.0  | MSP | INL | International Falls | MN         |
| 1191925.0 | -7.0  | MSP | INL | International Falls | MN         |
| 1201115.0 | -6.0  | MSP | INL | International Falls | MN         |
| 1211115.0 | 0.0   | MSP | INL | International Falls | MN         |
| 1221115.0 | -4.0  | MSP | INL | International Falls | MN         |
| 1231115.0 | -4.0  | MSP | INL | International Falls | MN         |
| 1241115.0 | -3.0  | MSP | INL | International Falls | MN         |
| 1252015.0 | -12.0 | MSP | INL | International Falls | MN         |
| 1261925.0 | -5.0  | MSP | INL | International Falls | MN         |
| 1271115.0 | 0.0   | MSP | INL | International Falls | MN         |
| 1281115.0 | -8.0  | MSP | INL | International Falls | MN         |
| 1291115.0 | -2.0  | MSP | INL | International Falls | MN         |
| 1301115.0 | 0.0   | MSP | INL | International Falls | MN         |
| 1311115.0 | -3.0  | MSP | INL | International Falls | MN         |

Truncated to 30 rows

``` scala
// Build `tripGraph` GraphFrame
// This GraphFrame builds up on the vertices and edges based on our trips (flights)
val tripGraph = GraphFrame(tripVertices, tripEdges)
println(tripGraph)

// Build `tripGraphPrime` GraphFrame
// This graphframe contains a smaller subset of data to make it easier to display motifs and subgraphs (below)
val tripEdgesPrime = departureDelays_geo.select("tripid", "delay", "src", "dst")
val tripGraphPrime = GraphFrame(tripVertices, tripEdgesPrime)
```

>     GraphFrame(v:[id: string, City: string ... 2 more fields], e:[src: string, dst: string ... 4 more fields])
>     tripGraph: org.graphframes.GraphFrame = GraphFrame(v:[id: string, City: string ... 2 more fields], e:[src: string, dst: string ... 4 more fields])
>     tripEdgesPrime: org.apache.spark.sql.DataFrame = [tripid: int, delay: int ... 2 more fields]
>     tripGraphPrime: org.graphframes.GraphFrame = GraphFrame(v:[id: string, City: string ... 2 more fields], e:[src: string, dst: string ... 2 more fields])

Simple Queries
--------------

Let's start with a set of simple graph queries to understand flight performance and departure delays

#### Determine the number of airports and trips

``` scala
println(s"Airports: ${tripGraph.vertices.count()}")
println(s"Trips: ${tripGraph.edges.count()}")
```

>     Airports: 279
>     Trips: 1361141

#### Determining the longest delay in this dataset

``` scala
// Finding the longest Delay
val longestDelay = tripGraph.edges.groupBy().max("delay")
display(longestDelay)
```

| max(delay) |
|------------|
| 1642.0     |

#### Determining the number of delayed vs. on-time / early flights

``` scala
// Determining number of on-time / early flights vs. delayed flights
println(s"On-time / Early Flights: ${tripGraph.edges.filter("delay <= 0").count()}")
println(s"Delayed Flights: ${tripGraph.edges.filter("delay > 0").count()}")
```

>     On-time / Early Flights: 780469
>     Delayed Flights: 580672

#### What flights departing SFO are most likely to have significant delays

Note, delay can be &lt;= 0 meaning the flight left on time or early

``` scala
val sfoDelayedTrips = tripGraph.edges.
  filter("src = 'SFO' and delay > 0").
  groupBy("src", "dst").
  avg("delay").
  sort(desc("avg(delay)"))
```

>     sfoDelayedTrips: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [src: string, dst: string ... 1 more field]

``` scala
display(sfoDelayedTrips)
```

| src | dst | avg(delay)         |
|-----|-----|--------------------|
| SFO | OKC | 59.073170731707314 |
| SFO | JAC | 57.13333333333333  |
| SFO | COS | 53.976190476190474 |
| SFO | OTH | 48.09090909090909  |
| SFO | SAT | 47.625             |
| SFO | MOD | 46.80952380952381  |
| SFO | SUN | 46.723404255319146 |
| SFO | CIC | 46.72164948453608  |
| SFO | ABQ | 44.8125            |
| SFO | ASE | 44.285714285714285 |
| SFO | PIT | 43.875             |
| SFO | MIA | 43.81730769230769  |
| SFO | FAT | 43.23972602739726  |
| SFO | MFR | 43.11848341232228  |
| SFO | SBP | 43.09770114942529  |
| SFO | MSP | 42.766917293233085 |
| SFO | BOI | 42.65482233502538  |
| SFO | RDM | 41.98823529411764  |
| SFO | AUS | 41.690677966101696 |
| SFO | SLC | 41.407272727272726 |
| SFO | JFK | 41.01379310344828  |
| SFO | PSP | 40.909909909909906 |
| SFO | PHX | 40.67272727272727  |
| SFO | MRY | 40.61764705882353  |
| SFO | ACV | 40.3728813559322   |
| SFO | LAS | 40.107602339181284 |
| SFO | TUS | 39.853658536585364 |
| SFO | SAN | 38.97361809045226  |
| SFO | SBA | 38.758620689655174 |
| SFO | BFL | 38.51136363636363  |

Truncated to 30 rows

#### What destinations tend to have delays

``` scala
// After displaying tripDelays, use Plot Options to set `state_dst` as a Key.
val tripDelays = tripGraph.edges.filter($"delay" > 0)
display(tripDelays)
```

#### What destinations tend to have significant delays departing from SEA

``` scala
// States with the longest cumulative delays (with individual delays > 100 minutes) (origin: Seattle)
display(tripGraph.edges.filter($"src" === "SEA" && $"delay" > 100))
```

Vertex Degrees
--------------

-   `inDegrees`: Incoming connections to the airport
-   `outDegrees`: Outgoing connections from the airport
-   `degrees`: Total connections to and from the airport

Reviewing the various properties of the property graph to understand the incoming and outgoing connections between airports.

``` scala
// Degrees
// The number of degrees - the number of incoming and outgoing connections - for various airports within this sample dataset
display(tripGraph.degrees.sort($"degree".desc).limit(20))
```

| id  | degree   |
|-----|----------|
| ATL | 179774.0 |
| DFW | 133966.0 |
| ORD | 125405.0 |
| LAX | 106853.0 |
| DEN | 103699.0 |
| IAH | 85685.0  |
| PHX | 79672.0  |
| SFO | 77635.0  |
| LAS | 66101.0  |
| CLT | 56103.0  |
| EWR | 54407.0  |
| MCO | 54300.0  |
| LGA | 50927.0  |
| SLC | 50780.0  |
| BOS | 49936.0  |
| DTW | 46705.0  |
| MSP | 46235.0  |
| SEA | 45816.0  |
| JFK | 43661.0  |
| BWI | 42526.0  |

City / Flight Relationships through Motif Finding
-------------------------------------------------

To more easily understand the complex relationship of city airports and their flights with each other, we can use motifs to find patterns of airports (i.e. vertices) connected by flights (i.e. edges). The result is a DataFrame in which the column names are given by the motif keys.

#### What delays might we blame on SFO

``` scala
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
```

Determining Airport Ranking using PageRank
------------------------------------------

There are a large number of flights and connections through these various airports included in this Departure Delay Dataset. Using the `pageRank` algorithm, Spark iteratively traverses the graph and determines a rough estimate of how important the airport is.

``` scala
// Determining Airport ranking of importance using `pageRank`
val ranks = tripGraph.pageRank.resetProbability(0.15).maxIter(5).run()
```

>     ranks: org.graphframes.GraphFrame = GraphFrame(v:[id: string, City: string ... 3 more fields], e:[src: string, dst: string ... 5 more fields])

``` scala
display(ranks.vertices.orderBy($"pagerank".desc).limit(20))
```

| id  | City           | State | Country | pagerank           |
|-----|----------------|-------|---------|--------------------|
| ATL | Atlanta        | GA    | USA     | 10.102340247485012 |
| DFW | Dallas         | TX    | USA     | 7.252067259651102  |
| ORD | Chicago        | IL    | USA     | 7.165214941662068  |
| DEN | Denver         | CO    | USA     | 5.041255573485869  |
| LAX | Los Angeles    | CA    | USA     | 4.178333397888139  |
| IAH | Houston        | TX    | USA     | 4.008169343175302  |
| SFO | San Francisco  | CA    | USA     | 3.518595203652925  |
| SLC | Salt Lake City | UT    | USA     | 3.3564822581626763 |
| PHX | Phoenix        | AZ    | USA     | 3.0896771274953343 |
| LAS | Las Vegas      | NV    | USA     | 2.437744837094217  |
| SEA | Seattle        | WA    | USA     | 2.372392233277875  |
| DTW | Detroit        | MI    | USA     | 2.1688712347162338 |
| MSP | Minneapolis    | MN    | USA     | 2.1574735230729862 |
| MCO | Orlando        | FL    | USA     | 2.10982981314059   |
| EWR | Newark         | NJ    | USA     | 2.0700271952450677 |
| CLT | Charlotte      | NC    | USA     | 2.0445115467872346 |
| LGA | New York       | NY    | USA     | 2.005137679218397  |
| BOS | Boston         | MA    | USA     | 1.7516230739068934 |
| BWI | Baltimore      | MD    | USA     | 1.703768581668634  |
| MIA | Miami          | FL    | USA     | 1.6479197970320616 |

BTW, A lot more delicate air-traffic arithmetic is possible for a full month of airplane co-trajectories over the radar range of Atlanta, Georgia!

See for instance:

-   Statistical regular pavings to analyze massive data of aircraft trajectories, Gloria Teng, Kenneth Kuhn and Raazesh Sainudiin, Journal of Aerospace Computing, Information, and Communication, Vol. 9, No. 1, pp. 14-25, [doi: 10.2514/1.I010015](http://arc.aiaa.org/doi/abs/10.2514/1.I010015), 2012. See free preprint: <http://lamastex.org/preprints/AAIASubPavingATC.pdf>.

Most popular flights (single city hops)
---------------------------------------

Using the `tripGraph`, we can quickly determine what are the most popular single city hop flights

``` scala
// Determine the most popular flights (single city hops)
import org.apache.spark.sql.functions._

val topTrips = tripGraph.edges.
  groupBy("src", "dst").
  agg(count("delay").as("trips"))
```

>     import org.apache.spark.sql.functions._
>     topTrips: org.apache.spark.sql.DataFrame = [src: string, dst: string ... 1 more field]

``` scala
// Show the top 20 most popular flights (single city hops)
display(topTrips.orderBy($"trips".desc).limit(20))
```

| src | dst | trips  |
|-----|-----|--------|
| SFO | LAX | 3232.0 |
| LAX | SFO | 3198.0 |
| LAS | LAX | 3016.0 |
| LAX | LAS | 2964.0 |
| JFK | LAX | 2720.0 |
| LAX | JFK | 2719.0 |
| ATL | LGA | 2501.0 |
| LGA | ATL | 2500.0 |
| LAX | PHX | 2394.0 |
| PHX | LAX | 2387.0 |
| HNL | OGG | 2380.0 |
| OGG | HNL | 2379.0 |
| LAX | SAN | 2215.0 |
| SAN | LAX | 2214.0 |
| SJC | LAX | 2208.0 |
| LAX | SJC | 2201.0 |
| ATL | MCO | 2136.0 |
| MCO | ATL | 2090.0 |
| JFK | SFO | 2084.0 |
| SFO | JFK | 2084.0 |

Top Transfer Cities
-------------------

Many airports are used as transfer points instead of the final Destination. An easy way to calculate this is by calculating the ratio of inDegree (the number of flights to the airport) / outDegree (the number of flights leaving the airport). Values close to 1 may indicate many transfers, whereas values &lt; 1 indicate many outgoing flights and &gt; 1 indicate many incoming flights. Note, this is a simple calculation that does not take into account of timing or scheduling of flights, just the overall aggregate number within the dataset.

``` scala
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
```

| id  | city        | degreeRatio         |
|-----|-------------|---------------------|
| GFK | Grand Forks | 1.3333333333333333  |
| FAI | Fairbanks   | 1.1232686980609419  |
| OME | Nome        | 0.5084745762711864  |
| BRW | Barrow      | 0.28651685393258425 |

``` scala
// Join back to the `airports` DataFrame (instead of registering temp table as above)
val transferAirports = degreeRatio.join(airports, degreeRatio("id") === airports("IATA")).
  selectExpr("id", "city", "degreeRatio").
  filter("degreeRatio between 0.9 and 1.1")
  
// List out the top 10 transfer city airports
display(transferAirports.orderBy("degreeRatio").limit(10))
```

| id  | city           | degreeRatio        |
|-----|----------------|--------------------|
| MSP | Minneapolis    | 0.9375183338222353 |
| DEN | Denver         | 0.958025717037065  |
| DFW | Dallas         | 0.964339653074092  |
| ORD | Chicago        | 0.9671063983310065 |
| SLC | Salt Lake City | 0.9827417906368358 |
| IAH | Houston        | 0.9846895050147083 |
| PHX | Phoenix        | 0.9891643572266746 |
| OGG | Kahului, Maui  | 0.9898718478710211 |
| HNL | Honolulu, Oahu | 0.990535889872173  |
| SFO | San Francisco  | 0.9909473252295224 |

Breadth First Search
--------------------

Breadth-first search (BFS) is designed to traverse the graph to quickly find the desired vertices (i.e. airports) and edges (i.e flights). Let's try to find the shortest number of connections between cities based on the dataset. Note, these examples do not take into account of time or distance, just hops between cities.

``` scala
// Example 1: Direct Seattle to San Francisco
// This method returns a DataFrame of valid shortest paths from vertices matching "fromExpr" to vertices matching "toExpr"
val filteredPaths = tripGraph.bfs.fromExpr((col("id") === "SEA")).toExpr(col("id") === "SFO").maxPathLength(1).run()
display(filteredPaths)
```

As you can see, there are a number of direct flights between Seattle and San Francisco.

``` scala
// Example 2: Direct San Francisco and Buffalo
// You can also specify expression as a String, instead of Column
val filteredPaths = tripGraph.bfs.fromExpr("id = 'SFO'").toExpr("id = 'BUF'").maxPathLength(1).run()
```

>     filteredPaths: org.apache.spark.sql.DataFrame = [id: string, City: string ... 2 more fields]

``` scala
filteredPaths.show()
```

>     +---+----+-----+-------+
>     | id|City|State|Country|
>     +---+----+-----+-------+
>     +---+----+-----+-------+

``` scala
display(filteredPaths)
```

But there are no direct flights between San Francisco and Buffalo.

``` scala
// Example 2a: Flying from San Francisco to Buffalo
val filteredPaths = tripGraph.bfs.fromExpr("id = 'SFO'").toExpr("id = 'BUF'").maxPathLength(2).run()
display(filteredPaths)
```

But there are flights from San Francisco to Buffalo with Minneapolis as the transfer point.

Loading the D3 Visualization
----------------------------

Using the airports D3 visualization to visualize airports and flight paths

>     Warning: classes defined within packages cannot be redefined without a cluster restart.
>     Compilation successful.

``` scala
d3a.graphs.help()
```

<p class="htmlSandbox">
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
</p></p>

#### Visualize On-time and Early Arrivals

``` scala
// On-time and Early Arrivals
import d3a._

graphs.force(
  height = 800,
  width = 1200,
  clicks = sql("select src, dst as dest, count(1) as count from departureDelays_geo where delay <= 0 group by src, dst").as[Edge])
```

#### Visualize Delayed Trips Departing from the West Coast

Notice that most of the delayed trips are with Western US cities

``` scala
// Delayed Trips from CA, OR, and/or WA
import d3a._

graphs.force(
  height = 800,
  width = 1200,
  clicks = sql("""select src, dst as dest, count(1) as count from departureDelays_geo where state_src in ('CA', 'OR', 'WA') and delay > 0 group by src, dst""").as[Edge])
```

#### Visualize All Flights (from this dataset)

``` scala
// Trips (from DepartureDelays Dataset)
import d3a._

graphs.force(
  height = 800,
  width = 1200,
  clicks = sql("""select src, dst as dest, count(1) as count from departureDelays_geo group by src, dst""").as[Edge])
```