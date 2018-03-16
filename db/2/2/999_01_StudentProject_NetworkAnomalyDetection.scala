// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/jpRpd8VlMYs/0.jpg)](https://www.youtube.com/embed/jpRpd8VlMYs?start=0&end=1713&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Network anomaly detection 
// MAGIC 
// MAGIC ## Student Project
// MAGIC 
// MAGIC by [Victor Ingman](https://www.linkedin.com/in/ingman/) and [Kasper Ramström](https://www.linkedin.com/in/kramstrom/)
// MAGIC 
// MAGIC This project set out to build an automatic network anomaly detection system for networks. Network threats are a major and growing concern for enterprises and private consumers all over the world. On average it takes 191 days for a company to detect a threat and another 66 days to contain the threat ([Enhancing Threat Detection with Big Data and AI](https://www.youtube.com/watch?v=i8___3GdxlQ)). In addition to taking long time to detect and contain threats, they also involve a ton of manual labour that require security experts. Thus, it should be a big priority for businesses to find solutions that not prevent malicious intrusions but also find these malicious activities in a fast and automated way, so that they can be dealt with swiftly.
// MAGIC 
// MAGIC An example of the threats we're facing today is the [WannaCry ransomware](https://www.symantec.com/blogs/threat-intelligence/wannacry-ransomware-attack) which spread rapidly throughout the world during 2017 and caused major havoc for companies and privates consumers throughout, including [Akademiska Sjukhuset](https://www.svt.se/nyheter/lokalt/uppsala/sjukhusledningen-i-forstarkningslage-efter-virusangreppet) here in Uppsala.
// MAGIC 
// MAGIC ![Super cool WannaCry screenshot](https://upload.wikimedia.org/wikipedia/en/1/18/Wana_Decrypt0r_screenshot.png)
// MAGIC 
// MAGIC With better security systems and automated ways of detecting malicious behaviour, many of these attacks could be prevented.
// MAGIC 
// MAGIC To gain inspiration for our project and find out how others have developed similar systems we've used the book [Advanced Analytics with Spark](http://shop.oreilly.com/product/0636920056591.do) which uses [k-means](https://en.wikipedia.org/wiki/K-means_clustering) clustering.
// MAGIC 
// MAGIC ![Advanced Analytics with Spark book](https://covers.oreillystatic.com/images/0636920056591/lrg.jpg)
// MAGIC 
// MAGIC In the book, the authors cluster different kinds of network events with the hopes of separating abnormal behaviour in clusters different from other events. The data used in the book is the publicly available [KDD Cup 1999 Data](https://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html), which is both quite dated and different from the data we've used, but it works well as a proof of concept for our project. The code accompanying the above mentioned book can be found at https://github.com/sryza/aas and for our project we've used a similar approach for clustering the data using k-means.
// MAGIC 
// MAGIC Below, we present the code for our project alongside with explanations for what we've done and how we've done it. This includes data collection, data visualization, clustering of data and possible improvements and future work.

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="https://en.wikipedia.org/wiki/Anomaly_detection">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Anomaly_detection",500))

// COMMAND ----------

// MAGIC %md
// MAGIC # Data Collection
// MAGIC 
// MAGIC To get data for our network security project we decided to generate it ourselves from our own networks and perform malicious activity as well.
// MAGIC 
// MAGIC Our basic idea for the data collection involved having one victim device, which would perform normal internet activity, including streaming to different media devices, transferring files and web surfing. During this, another device would (the attacker) would perform malicious activity such as port scans and fingerprinting of the victim. Our hopes were that the malicious activities would stand out from the other traffic and would hopefully be detectable for our anomaly detection models.
// MAGIC 
// MAGIC From the book [Network Security Through Analysis](http://shop.oreilly.com/product/0636920028444.do) we read about the tools [Wireshark](https://www.wireshark.org/) and [Nmap](https://nmap.org). For our project, we used Wireshark for collecting network data on the victim's computer and Nmap for performing malicious activity.
// MAGIC 
// MAGIC ![](https://covers.oreillystatic.com/images/0636920028444/lrg.jpg)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Data anonymization
// MAGIC 
// MAGIC As we collected data on our own private network and publish this notebook along with the data publicly, we decided to anonmyize our network data for privacy reasons. To do this, we followed the Databricks guide: https://databricks.com/blog/2017/02/13/anonymizing-datasets-at-scale-leveraging-databricks-interoperability.html
// MAGIC 
// MAGIC By using the package [Faker](https://faker.readthedocs.io/en/latest/index.html) we generated fake source IP's and destination IP's for our network traffic data and used this data for the remainder of the project. Since we didn't parse the packet details for our network traffic and since it can potentially include sensitive information about our connections, we decided to remove that data from the public dataset.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Data_anonymization",500))

// COMMAND ----------

// MAGIC %sh
// MAGIC 
// MAGIC pip install unicodecsv Faker

// COMMAND ----------

// MAGIC %py
// MAGIC 
// MAGIC import unicodecsv as csv
// MAGIC from collections import defaultdict
// MAGIC from faker import Factory
// MAGIC 
// MAGIC def anonymize_rows(rows):
// MAGIC     """
// MAGIC     Rows is an iterable of dictionaries that contain name and
// MAGIC     email fields that need to be anonymized.
// MAGIC     """
// MAGIC     # Load faker
// MAGIC     faker  = Factory.create()
// MAGIC 
// MAGIC     # Create mappings of names, emails, social security numbers, and phone numbers to faked names & emails.
// MAGIC     sources  = defaultdict(faker.ipv4)
// MAGIC     destinations = defaultdict(faker.ipv4)
// MAGIC 
// MAGIC     # Iterate over the rows from the file and yield anonymized rows.
// MAGIC     for row in rows:
// MAGIC         # Replace name and email fields with faked fields.
// MAGIC         row["Source"]  = sources[row["Source"]]
// MAGIC         row["Destination"] = destinations[row["Destination"]]
// MAGIC 
// MAGIC         # Yield the row back to the caller
// MAGIC         yield row
// MAGIC     
// MAGIC def anonymize(source, target):
// MAGIC     """
// MAGIC     The source argument is a path to a CSV file containing data to anonymize,
// MAGIC     while target is a path to write the anonymized CSV data to.
// MAGIC     """
// MAGIC     with open(source, 'rU') as f:
// MAGIC         with open(target, 'w') as o:
// MAGIC             # Use the DictReader to easily extract fields
// MAGIC             reader = csv.DictReader(f)
// MAGIC             writer = csv.DictWriter(o, reader.fieldnames)
// MAGIC 
// MAGIC             # Read and anonymize data, writing to target file.
// MAGIC             for row in anonymize_rows(reader):
// MAGIC                 writer.writerow(row)
// MAGIC         
// MAGIC # anonymize("path-to-dataset-to-be-anonymized", "path-to-output-file")

// COMMAND ----------

// MAGIC %md
// MAGIC # Wireshark and Nmap
// MAGIC 
// MAGIC What is it?
// MAGIC https://www.wireshark.org/
// MAGIC 
// MAGIC Wireshark is a free and open source packet analyzer. It is used for network troubleshooting, analysis, software and communications protocol development, and education.
// MAGIC 
// MAGIC Our setup consisted of two computers, one as victim and one as attacker.
// MAGIC 
// MAGIC ## Step by step
// MAGIC - Opened up Wireshark on the victims computer as well as logging activity on the network
// MAGIC     - For a guide on how to log network info with wireshark, see the following:
// MAGIC     https://www.wireshark.org/docs/wsug_html_chunked/ChCapCapturingSection.html
// MAGIC - Started a lot of transfers and streams on the victims computer
// MAGIC     - Started a Chromecast stream of a workout video on Youtube to a TV on the network
// MAGIC     - Streaming music to speakers on the network via Spotify Connect
// MAGIC     - Sending large files via Apple Airdrop
// MAGIC - The attacker started Nmap and started a port scan against the victim
// MAGIC - The attacker did a thourough fingerprint of the victim, such as OS detection and software detection at the open ports, also with Nmap
// MAGIC - We exported the victims wireshark log as CSV by doing the following:
// MAGIC ![](https://www.sunlabs.se/assets/sds/wireshark-export-csv.png)
// MAGIC 
// MAGIC The following image visualizes the network environment
// MAGIC ![](https://www.sunlabs.se/assets/sds/graph-network-collection.png)
// MAGIC 
// MAGIC The dotted lines shows network communications
// MAGIC Filled lines shows local execution or communication between nodes
// MAGIC Lines with arrows shows directed communication
// MAGIC 
// MAGIC After that was done, about 30 minutes later, we exported the data to CSV-format. The CSV was formatted as follows:
// MAGIC 
// MAGIC No | Time | Source | Destination | Protocol | Length | Info
// MAGIC --- | --- | --- | --- | --- | --- | ---
// MAGIC 1 | 0.001237 | 10.0.0.66 | 10.0.0.1 | DNS | 54 | [Redacted]
// MAGIC ⫶ | ⫶ | ⫶ | ⫶ | ⫶ | ⫶ | ⫶
// MAGIC 
// MAGIC ## Description of collected data
// MAGIC - **No** = The id of the packet captured, starts from 0.
// MAGIC - **Time** = Number of seconds elapsed since the capture started
// MAGIC - **Source** = The IP address of the sender of the packet
// MAGIC - **Destination** = The IP address of the receiver of the packet
// MAGIC - **Protocol** = The protocol of the packet
// MAGIC - **Length** = Length of the packet
// MAGIC - **Info** = Data that is sent with the packet, redacted for privacy and anonymity
// MAGIC 
// MAGIC That way we are able to visualize the data collected in the form of a directed graph network and use the number of times a packet is sent identified by unique (source, destination, protocol).

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Download the network data
// MAGIC 
// MAGIC The data dump we collected is available for download at the following url
// MAGIC 
// MAGIC http://sunlabs.se/assets/sds/anon_data.csv

// COMMAND ----------

// MAGIC %sh
// MAGIC 
// MAGIC wget "http://sunlabs.se/assets/sds/anon_data.csv"

// COMMAND ----------

// MAGIC %sh
// MAGIC 
// MAGIC pwd
// MAGIC ls

// COMMAND ----------

val dataPath = "file:/databricks/driver/anon_data.csv"
spark.read.format("csv")
  .option("header","true")
  .option("inferSchema", "true")
  .load(dataPath)
  .createOrReplaceTempView("anonymized_data_raw")

// COMMAND ----------

// MAGIC %md
// MAGIC # Data visualization
// MAGIC 
// MAGIC To better understand our our network data, analyze it and verify its correctness, we decided to represent the data in a graph network. A graph is made up of vertices and edges and can be either directed or undirected. A visualization of an example graph can be seen in the picture below:
// MAGIC 
// MAGIC <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/1/1c/Directed_graph%2C_cyclic.svg/2000px-Directed_graph%2C_cyclic.svg.png" alt="Example Graph" style="width: 500px;"/>
// MAGIC 
// MAGIC And more information about graph theory can be found at https://en.wikipedia.org/wiki/Graph_theory.
// MAGIC 
// MAGIC In our context of network traffic, each connected device can be seen as a vertex in the graph and each packet sent between two devices is an edge. For our data a packet is always sent from one source node (vertex) to another destination node (vertex). Thus each edge is directed from and the whole graph is directed.
// MAGIC 
// MAGIC To use this graph representation for our network data we used the Spark package GraphFrames.
// MAGIC 
// MAGIC GraphFrames is a package for Apache Spark which provides DataFrame-based Graphs. It provides high-level APIs in Scala, Java, and Python. It aims to provide both the functionality of GraphX and extended functionality taking advantage of Spark DataFrames. This extended functionality includes motif finding, DataFrame-based serialization, and highly expressive graph queries.
// MAGIC 
// MAGIC The GraphFrames package is available from [Spark Packages](http://spark-packages.org/package/graphframes/graphframes).
// MAGIC 
// MAGIC This notebook demonstrates examples from the [GraphFrames User Guide](http://graphframes.github.io/user-guide.html).
// MAGIC 
// MAGIC (Above GraphFrames explanation taken from Raazesh Sainudiin's course [Scalable Data Science](https://lamastex.github.io/scalable-data-science/))
// MAGIC 
// MAGIC Using GraphFrames we can also see the the relationship between vertices using motifs, filter graphs and find the in- and outdegrees of vertices.
// MAGIC 
// MAGIC To visualize our graph network we decided to use the package JavaScript visualization package [D3](https://d3js.org/) which allows for complex visualizations of graph networks and tons of other applications.

// COMMAND ----------

displayHTML(frameIt("https://d3js.org",500))

// COMMAND ----------

displayHTML(frameIt("http://graphframes.github.io/user-guide.html",500))

// COMMAND ----------

val sqlDF = spark.sql("SELECT * FROM anonymized_data_raw")

// COMMAND ----------

display(sqlDF)

// COMMAND ----------

import org.apache.spark.sql._
import org.apache.spark.sql.functions._

// Truncate the data for each millisecond
val truncData = sqlDF
  .select($"n", $"Source", $"Destination", round($"Time", 2).as("ts"), $"Protocol", $"Length")
  .groupBy($"ts", $"Source", $"Destination", $"Protocol")
  .agg(avg($"Length").as("len"), (avg("Length") / max($"Length")).as("local_anomalies"), count("*").as("count"))
  .sort($"ts")

truncData.show(5)

truncData.createOrReplaceTempView("anonymized_data")

// COMMAND ----------

import org.graphframes._

val v = truncData.select($"Source".as("id"), $"Source".as("src")).where("count > 10")
v.show()

val e = truncData.select($"Source".as("src"), $"Destination".as("dst"), $"Protocol", $"count").where("count > 10")
e.show()

val g = GraphFrame(v, e)

val gE= g.edges.select($"src", $"dst".as("dest"), $"count")
display(gE)

// COMMAND ----------

package d3
// We use a package object so that we can define top level classes like Edge that need to be used in other cells
// This was modified by Ivan Sadikov to make sure it is compatible the latest databricks notebook

import org.apache.spark.sql._
import com.databricks.backend.daemon.driver.EnhancedRDDFunctions.displayHTML

case class Edge(src: String, dest: String, count: Long)

case class Node(name: String)
case class Link(source: Int, target: Int, value: Long)
case class Graph(nodes: Seq[Node], links: Seq[Link])

object graphs {
// val sqlContext = SQLContext.getOrCreate(org.apache.spark.SparkContext.getOrCreate())  /// fix
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

displayHTML(s"""
<style>

.node_circle {
  stroke: #777;
  stroke-width: 1.3px;
}

.node_label {
  pointer-events: none;
}

.link {
  stroke: #777;
  stroke-opacity: .2;
}

.node_count {
  stroke: #777;
  stroke-width: 1.0px;
  fill: #999;
}

text.legend {
  font-family: Verdana;
  font-size: 13px;
  fill: #000;
}

.node text {
  font-family: "Helvetica Neue","Helvetica","Arial",sans-serif;
  font-size: 17px;
  font-weight: 200;
}

</style>

<div id="clicks-graph">
<script src="//d3js.org/d3.v3.min.js"></script>
<script>

var graph = $graph;

var width = $width,
    height = $height;

var color = d3.scale.category20();

var force = d3.layout.force()
    .charge(-700)
    .linkDistance(180)
    .size([width, height]);

var svg = d3.select("#clicks-graph").append("svg")
    .attr("width", width)
    .attr("height", height);
    
force
    .nodes(graph.nodes)
    .links(graph.links)
    .start();

var link = svg.selectAll(".link")
    .data(graph.links)
    .enter().append("line")
    .attr("class", "link")
    .style("stroke-width", function(d) { return Math.sqrt(d.value); });

var node = svg.selectAll(".node")
    .data(graph.nodes)
    .enter().append("g")
    .attr("class", "node")
    .call(force.drag);

node.append("circle")
    .attr("r", 10)
    .style("fill", function (d) {
    if (d.name.startsWith("other")) { return color(1); } else { return color(2); };
})

node.append("text")
      .attr("dx", 10)
      .attr("dy", ".35em")
      .text(function(d) { return d.name });
      
//Now we are giving the SVGs co-ordinates - the force layout is generating the co-ordinates which this code is using to update the attributes of the SVG elements
force.on("tick", function () {
    link.attr("x1", function (d) {
        return d.source.x;
    })
        .attr("y1", function (d) {
        return d.source.y;
    })
        .attr("x2", function (d) {
        return d.target.x;
    })
        .attr("y2", function (d) {
        return d.target.y;
    });
    d3.selectAll("circle").attr("cx", function (d) {
        return d.x;
    })
        .attr("cy", function (d) {
        return d.y;
    });
    d3.selectAll("text").attr("x", function (d) {
        return d.x;
    })
        .attr("y", function (d) {
        return d.y;
    });
});
</script>
</div>
""")
}
  
  def help() = {
displayHTML("""
<p>
Produces a force-directed graph given a collection of edges of the following form:</br>
<tt><font color="#a71d5d">case class</font> <font color="#795da3">Edge</font>(<font color="#ed6a43">src</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">dest</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">count</font>: <font color="#a71d5d">Long</font>)</tt>
</p>
<p>Usage:<br/>
<tt><font color="#a71d5d">import</font> <font color="#ed6a43">d3._</font></tt><br/>
<tt><font color="#795da3">graphs.force</font>(</br>
&nbsp;&nbsp;<font color="#ed6a43">height</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">width</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">clicks</font>: <font color="#795da3">Dataset</font>[<font color="#795da3">Edge</font>])</tt>
</p>""")
  }
}

// COMMAND ----------

d3.graphs.force(
  height = 1680,
  width = 1280,
  clicks = gE.as[d3.Edge])

// COMMAND ----------

display(g.inDegrees.orderBy($"inDegree".desc))

// COMMAND ----------

display(g.outDegrees.orderBy($"outDegree".desc))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Clustering
// MAGIC ## Pre-processing of data
// MAGIC 
// MAGIC We preprocessed the data logged from wireshark doing the following:
// MAGIC 
// MAGIC - Rounding timestamps by milliseconds, that would be four significant decimals.
// MAGIC - Group the data by (timestamp, source, destination, protocol) with a count of how many times these kind of packets was sent/received during a millisecond.
// MAGIC - One-hot encoded the protocol values
// MAGIC     - If you don't what that means, check this article out
// MAGIC     https://hackernoon.com/what-is-one-hot-encoding-why-and-when-do-you-have-to-use-it-e3c6186d008f
// MAGIC - Standardized features for count and length of packets
// MAGIC 
// MAGIC 
// MAGIC ## Setting up k-means clustering
// MAGIC - 23 features
// MAGIC - Filtering out features that are not numeric, example is destination and source

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/K-means_clustering",500))

// COMMAND ----------

// MAGIC %python
// MAGIC import pandas as pd
// MAGIC 
// MAGIC sampled = sqlContext.sql("SELECT * FROM anonymized_data").toPandas()

// COMMAND ----------

// MAGIC %python
// MAGIC # standardize features
// MAGIC from sklearn.preprocessing import StandardScaler
// MAGIC scaler = StandardScaler()

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC sample = sampled['len']
// MAGIC sample = sample.reshape(-1, 1) # one feature
// MAGIC scaler.fit(sample)
// MAGIC 
// MAGIC sampled['len'] = scaler.transform(sample)

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC sample = sampled['count']
// MAGIC sample = sample.reshape(-1, 1) # one feature
// MAGIC scaler.fit(sample)
// MAGIC 
// MAGIC sampled['count'] = scaler.transform(sample)

// COMMAND ----------

// MAGIC %python
// MAGIC 
// MAGIC df_count = sampled['count']
// MAGIC df_length = sampled['len']
// MAGIC df_proto = pd.get_dummies(sampled['Protocol'])
// MAGIC df_source = sampled['Source']
// MAGIC df_dest = sampled['Destination']
// MAGIC df_ts = sampled['ts']
// MAGIC 
// MAGIC onehot = pd.concat([df_proto, df_source, df_length, df_dest, df_ts, df_count], axis=1)
// MAGIC onehotDF = sqlContext.createDataFrame(onehot)
// MAGIC 
// MAGIC sqlContext.sql("DROP TABLE IF EXISTS anonymized_data_onehot")
// MAGIC onehotDF.write.saveAsTable('anonymized_data_onehot')

// COMMAND ----------

case class Packet(AJP13: Double, ALLJOYN_NS: Double, ARP: Double, DHCP: Double, DNS: Double, HTTP: Double, HTTP_XML: Double, ICMP: Double, ICMPv6: Double, IGMPv1: Double, IGMPv2: Double, IGMPv3: Double, MDNS: Double, NBNS: Double, NTP: Double, OCSP: Double, QUIC: Double, RTCP: Double, SIP: Double, SNMP: Double, SSDP: Double, STP: Double, STUN: Double, TCP: Double, TFTP: Double, TLSv1: Double, TLSv1_2: Double, UDP: Double, XMPP_XML: Double, Source: String, len: Double, Destination: String, ts: Double,
 count: Long)

def parseRow(row: org.apache.spark.sql.Row): Packet = {
  
  def toDouble(value: Any): Double = {
    try {
       value.toString.toDouble
    } catch {
      case e: Exception => 0.0
    }
  }
  def toLong(value: Any): Long = {
    try {
       value.toString.toLong
    } catch {
      case e: Exception => 0
    }
  }
  
  Packet(toDouble(row(0)), toDouble(row(1)), toDouble(row(2)), toDouble(row(3)), toDouble(row(4)), toDouble(row(5)), toDouble(row(6)), toDouble(row(7)), toDouble(row(8)), toDouble(row(9)), toDouble(row(10)), toDouble(row(11)), toDouble(row(12)), toDouble(row(13)), toDouble(row(14)), toDouble(row(15)), toDouble(row(16)), toDouble(row(17)), toDouble(row(18)), toDouble(row(19)), toDouble(row(20)), toDouble(row(21)), toDouble(row(22)), toDouble(row(23)), toDouble(row(24)), toDouble(row(25)), toDouble(row(26)), toDouble(row(27)), toDouble(row(28)), row(29).toString, toDouble(row(30)), row(31).toString, toDouble(row(32)), toLong(row(33)))
}

val df = table("anonymized_data_onehot").map(parseRow).toDF
df.createOrReplaceTempView("packetsView")

// COMMAND ----------

import org.apache.spark.ml.feature.VectorAssembler

val list = ("Source, Destination")
val cols = df.columns

val filtered = cols.filter { el =>
  !list.contains(el)
}

val trainingData = new VectorAssembler()
                      .setInputCols(filtered)
                      .setOutputCol("features")
                      .transform(table("packetsView"))

// COMMAND ----------

import org.apache.spark.ml.clustering.KMeans

val model = new KMeans().setK(23).fit(trainingData)
val modelTransformed = model.transform(trainingData)

// COMMAND ----------

// MAGIC %md
// MAGIC # Improvements and future work
// MAGIC 
// MAGIC In this section we present possible improvements that could have been done for our project and future work to further build on the project, increase its usability and value.
// MAGIC ## Dimensionality improvements
// MAGIC 
// MAGIC 
// MAGIC We used k-means for clustering our network data which uses euclidean distance. Models using euclidean distance are susceptible to the [Curse of Dimensionality](https://en.wikipedia.org/wiki/Curse_of_dimensionality). With the 23 features we got after using one-hot encoding for the protocol column in the original dataset we are likely suffering from this high dimensionality. To improve the clustering one could an algorithm that doesn't use euclidean distance (or other distance measures that don't work well for high dimensionality). Another possible solution could be to to use [dimensionality reduction](dimensionality reduction using autoencoder) and try to retain as much information as possible with fewer features. This could be done using techniques such as [PCA](https://en.wikipedia.org/wiki/Principal_component_analysis) or [LDA](https://en.wikipedia.org/wiki/Linear_discriminant_analysis).
// MAGIC 
// MAGIC ## Parse packet contents
// MAGIC 
// MAGIC We didn't parse the packet information other than IP addresses, packet lengths and protocol. To gain further insights one could parse the additional packet contents and look for sensitive items, including usernames, passwords etc.
// MAGIC 
// MAGIC ## Graph Analysis
// MAGIC 
// MAGIC One could continue analyze the graph representation of the data. Examples of this could include looking for comlpex relationships in the graph using GraphFrames motifs.
// MAGIC 
// MAGIC ## Real time network analysis using Spark streaming
// MAGIC 
// MAGIC To make the project even more useful in a real environment, one could use [Spark Streaming k-means](https://databricks.com/blog/2015/01/28/introducing-streaming-k-means-in-spark-1-2.html) to cluster network traffic in real time and then perform anomaly detection in real time as well. An example approach of this can be seen in the following video: https://www.youtube.com/watch?v=i8___3GdxlQ
// MAGIC 
// MAGIC Additional continuations of this could include giving suggestions for actions to perform when deteching malicious activity.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Dimensionality_reduction",500))

// COMMAND ----------

displayHTML(frameIt("https://databricks.com/blog/2015/01/28/introducing-streaming-k-means-in-spark-1-2.html",500))

// COMMAND ----------

