// Databricks notebook source
// MAGIC %md
// MAGIC # Network anomaly detection
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