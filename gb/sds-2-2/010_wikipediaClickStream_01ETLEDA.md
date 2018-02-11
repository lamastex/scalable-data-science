[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Wiki Clickstream Analysis
=========================

\*\* Dataset: 3.2 billion requests collected during the month of February 2015 grouped by (src, dest) \*\*

\*\* Source: https://datahub.io/dataset/wikipedia-clickstream/ \*\*

![NY clickstream image](https://databricks-prod-cloudfront.s3.amazonaws.com/docs/images/ny.clickstream.png "NY clickstream image")

*This notebook requires Spark 1.6+.*

This notebook was originally a data analysis workflow developed with [Databricks Community Edition](https://databricks.com/blog/2016/02/17/introducing-databricks-community-edition-apache-spark-for-all.html), a free version of Databricks designed for learning [Apache Spark](https://spark.apache.org/).

Here we elucidate the original python notebook ([also linked here](/#workspace/scalable-data-science/xtraResources/sparkSummitEast2016/Wikipedia%20Clickstream%20Data)) used in the talk by Michael Armbrust at Spark Summit East February 2016 shared from <https://twitter.com/michaelarmbrust/status/699969850475737088> (watch later)

[![Michael Armbrust Spark Summit East](http://img.youtube.com/vi/35Y-rqSMCCA/0.jpg)](https://www.youtube.com/v/35Y-rqSMCCA)

### Data set

![Wikipedia Logo](http://sameerf-dbc-labs.s3-website-us-west-2.amazonaws.com/data/wikipedia/images/w_logo_for_labs.png)
=======================================================================================================================

The data we are exploring in this lab is the February 2015 English Wikipedia Clickstream data, and it is available here: http://datahub.io/dataset/wikipedia-clickstream/resource/be85cc68-d1e6-4134-804a-fd36b94dbb82.

According to Wikimedia:

> "The data contains counts of (referer, resource) pairs extracted from the request logs of English Wikipedia. When a client requests a resource by following a link or performing a search, the URI of the webpage that linked to the resource is included with the request in an HTTP header called the "referer". This data captures 22 million (referer, resource) pairs from a total of 3.2 billion requests collected during the month of February 2015."

The data is approximately 1.2GB and it is hosted in the following Databricks file: `/databricks-datasets/wikipedia-datasets/data-001/clickstream/raw-uncompressed`

### Let us first understand this Wikimedia data set a bit more

Let's read the datahub-hosted link <https://datahub.io/dataset/wikipedia-clickstream> in the embedding below. Also click the [blog](http://ewulczyn.github.io/Wikipedia_Clickstream_Getting_Started/) by Ellery Wulczyn, Data Scientist at The Wikimedia Foundation, to better understand how the data was generated (remember to Right-Click and use -&gt; and &lt;- if navigating within the embedded html frame below).

<p class="htmlSandbox"><iframe 
 src="https://datahub.io/dataset/wikipedia-clickstream"
 width="95%" height="500"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

Run the next two cells for some housekeeping.

    if (org.apache.spark.BuildInfo.sparkBranch < "1.6") sys.error("Attach this notebook to a cluster running Spark 1.6+")

### Loading and Exploring the data

    val data = sc.textFile("dbfs:///databricks-datasets/wikipedia-datasets/data-001/clickstream/raw-uncompressed")

> data: org.apache.spark.rdd.RDD\[String\] = dbfs:///databricks-datasets/wikipedia-datasets/data-001/clickstream/raw-uncompressed MapPartitionsRDD\[1\] at textFile at &lt;console&gt;:34

##### Looking at the first few lines of the data

    data.take(5).foreach(println) 

> prev\_id curr\_id n prev\_title curr\_title type 3632887 121 other-google !! other 3632887 93 other-wikipedia !! other 3632887 46 other-empty !! other 3632887 10 other-other !! other

    data.take(2)

> res3: Array\[String\] = Array(prev\_id curr\_id n prev\_title curr\_title type, " 3632887 121 other-google !! other")

-   The first line looks like a header
-   The second line (separated from the first by ",") contains data organized according to the header, i.e., `prev_id` = 3632887, `curr_id` = 121", and so on.

Actually, here is the meaning of each column:

-   `prev_id`: if the referer does not correspond to an article in the main namespace of English Wikipedia, this value will be empty. Otherwise, it contains the unique MediaWiki page ID of the article corresponding to the referer i.e. the previous article the client was on

-   `curr_id`: the MediaWiki unique page ID of the article the client requested

-   `prev_title`: the result of mapping the referer URL to the fixed set of values described below

-   `curr_title`: the title of the article the client requested

-   `n`: the number of occurrences of the (referer, resource) pair

-   `type`
-   "link" if the referer and request are both articles and the referer links to the request
-   "redlink" if the referer is an article and links to the request, but the request is not in the production enwiki.page table
-   "other" if the *referer* and request are both articles but the referer does not link to the request. This can happen when clients search or spoof their refer

Referers were mapped to a fixed set of values corresponding to internal traffic or external traffic from one of the top 5 global traffic sources to English Wikipedia, based on this scheme:

> -   an article in the main namespace of English Wikipedia -&gt; the article title
> -   any Wikipedia page that is not in the main namespace of English Wikipedia -&gt; `other-wikipedia`
> -   an empty referer -&gt; `other-empty`
> -   a page from any other Wikimedia project -&gt; `other-internal`
> -   Google -&gt; `other-google`
> -   Yahoo -&gt; `other-yahoo`
> -   Bing -&gt; `other-bing`
> -   Facebook -&gt; `other-facebook`
> -   Twitter -&gt; `other-twitter`
> -   anything else -&gt; `other-other`

In the second line of the file above, we can see there were 121 clicks from Google to the Wikipedia page on "!!" (double exclamation marks). People search for everything! \* prev\_id = *(nothing)* \* curr\_id = 3632887 *--&gt; (Wikipedia page ID)* \* n = 121 *(People clicked from Google to this page 121 times in this month.)* \* prev\_title = other-google *(This data record is for referals from Google.)* \* curr\_title = !! *(This Wikipedia page is about a double exclamation mark.)* \* type = other

### Create a DataFrame from this CSV

-   From the next Spark release - 2.0, CSV as a datasource will be part of Spark's standard release. But, we are using Spark 1.6

<!-- -->

    // Load the raw dataset stored as a CSV file
    val clickstream = sqlContext.
        read.
        format("com.databricks.spark.csv").
        options(Map("header" -> "true", "delimiter" -> "\t", "mode" -> "PERMISSIVE", "inferSchema" -> "true")).
        load("dbfs:///databricks-datasets/wikipedia-datasets/data-001/clickstream/raw-uncompressed")
      

> clickstream: org.apache.spark.sql.DataFrame = \[prev\_id: int, curr\_id: int ... 4 more fields\]

##### Print the schema

    clickstream.printSchema

> root |-- prev\_id: integer (nullable = true) |-- curr\_id: integer (nullable = true) |-- n: integer (nullable = true) |-- prev\_title: string (nullable = true) |-- curr\_title: string (nullable = true) |-- type: string (nullable = true)

#### Display some sample data

    display(clickstream)

Display is a utility provided by Databricks. If you are programming directly in Spark, use the show(numRows: Int) function of DataFrame

    clickstream.show(5)

> +-------+-------+---+------------------+----------+-----+ |prev\_id|curr\_id| n| prev\_title|curr\_title| type| +-------+-------+---+------------------+----------+-----+ | null|3632887|121| other-google| !!|other| | null|3632887| 93| other-wikipedia| !!|other| | null|3632887| 46| other-empty| !!|other| | null|3632887| 10| other-other| !!|other| | 64486|3632887| 11|!\_(disambiguation)| !!|other| +-------+-------+---+------------------+----------+-----+ only showing top 5 rows

### Reading from disk vs memory

The 1.2 GB Clickstream file is currently on S3, which means each time you scan through it, your Spark cluster has to read the 1.2 GB of data remotely over the network.

    clickstream.cache().count()

> res7: Long = 22509897

-   It took about several minutes to read the 1.2 GB file into your Spark cluster. The file has 22.5 million rows/lines.
-   Although we have called cache, remember that it is evaluated (cached) only when an action(count) is called

Now call count again to see how much faster it is to read from memory

    clickstream.count()

> res8: Long = 22509897

-   Orders of magnitude faster!
-   If you are going to be using the same data source multiple times, it is better to cache it in memory

### What are the top 10 articles requested?

To do this we also need to order by the sum of column `n`, in descending order.

    //Type in your answer here...
    display(clickstream
      .select(clickstream("curr_title"), clickstream("n"))
      .groupBy("curr_title")
      .sum()
      .orderBy($"sum(n)".desc)
      .limit(10))

| Main\_Page                                 | 1.2750062e8 |
|--------------------------------------------|-------------|
| 87th\_Academy\_Awards                      | 2559794.0   |
| Fifty\_Shades\_of\_Grey                    | 2326175.0   |
| Alive                                      | 2244781.0   |
| Chris\_Kyle                                | 1709341.0   |
| Fifty\_Shades\_of\_Grey\_(film)            | 1683892.0   |
| Deaths\_in\_2015                           | 1614577.0   |
| Birdman\_(film)                            | 1545842.0   |
| Islamic\_State\_of\_Iraq\_and\_the\_Levant | 1406530.0   |
| Stephen\_Hawking                           | 1384193.0   |

### Who sent the most traffic to Wikipedia in Feb 2015?

In other words, who were the top referers to Wikipedia?

    display(clickstream
      .select(clickstream("prev_title"), clickstream("n"))
      .groupBy("prev_title")
      .sum()
      .orderBy($"sum(n)".desc)
      .limit(10))

| other-google          | 1.496209976e9 |
|-----------------------|---------------|
| other-empty           | 3.47693595e8  |
| other-wikipedia       | 1.29772279e8  |
| other-other           | 7.7569671e7   |
| other-bing            | 6.5962792e7   |
| other-yahoo           | 4.8501171e7   |
| Main\_Page            | 2.9923502e7   |
| other-twitter         | 1.9241298e7   |
| other-facebook        | 2314026.0     |
| 87th\_Academy\_Awards | 1680675.0     |

As expected, the top referer by a large margin is Google. Next comes refererless traffic (usually clients using HTTPS). The third largest sender of traffic to English Wikipedia are Wikipedia pages that are not in the main namespace (ns = 0) of English Wikipedia. Learn about the Wikipedia namespaces here: https://en.wikipedia.org/wiki/Wikipedia:Project\_namespace

Also, note that Twitter sends 10x more requests to Wikipedia than Facebook.

### What were the top 5 trending articles people from Twitter were looking up in Wikipedia?

    //Type in your answer here...
    display(clickstream
      .select(clickstream("curr_title"), clickstream("prev_title"), clickstream("n"))
      .filter("prev_title = 'other-twitter'")
      .groupBy("curr_title")
      .sum()
      .orderBy($"sum(n)".desc)
      .limit(5))

| Johnny\_Knoxville         | 198908.0 |
|---------------------------|----------|
| Peter\_Woodcock           | 126259.0 |
| 2002\_Tampa\_plane\_crash | 119906.0 |
| Sơn\_Đoòng\_Cave          | 116012.0 |
| The\_boy\_Jones           | 114401.0 |

#### What percentage of page visits in Wikipedia are from other pages in Wikipedia itself?

    val allClicks = clickstream.selectExpr("sum(n)").first.getLong(0)
    val referals = clickstream.
                    filter(clickstream("prev_id").isNotNull).
                    selectExpr("sum(n)").first.getLong(0)
    (referals * 100.0) / allClicks

> allClicks: Long = 3283067885 referals: Long = 1095462001 res12: Double = 33.36702253416853

#### Register the DataFrame to perform more complex queries

    clickstream.createOrReplaceTempView("clicks")

#### Which Wikipedia pages have the most referrals to the Donald Trump page?

    SELECT *
    FROM clicks
    WHERE 
      curr_title = 'Donald_Trump' AND
      prev_id IS NOT NULL AND prev_title != 'Main_Page'
    ORDER BY n DESC
    LIMIT 20

| 1861441.0   | 4848272.0 | 4658.0 | Ivanka\_Trump                                      | Donald\_Trump | link |
|-------------|-----------|--------|----------------------------------------------------|---------------|------|
| 4848272.0   | 4848272.0 | 2212.0 | Donald\_Trump                                      | Donald\_Trump | link |
| 1209075.0   | 4848272.0 | 1855.0 | Melania\_Trump                                     | Donald\_Trump | link |
| 1057887.0   | 4848272.0 | 1760.0 | Ivana\_Trump                                       | Donald\_Trump | link |
| 5679119.0   | 4848272.0 | 1074.0 | Donald\_Trump\_Jr.                                 | Donald\_Trump | link |
| 2.1377251e7 | 4848272.0 | 918.0  | United\_States\_presidential\_election,\_2016      | Donald\_Trump | link |
| 8095589.0   | 4848272.0 | 728.0  | Eric\_Trump                                        | Donald\_Trump | link |
| 473806.0    | 4848272.0 | 652.0  | Marla\_Maples                                      | Donald\_Trump | link |
| 2565136.0   | 4848272.0 | 651.0  | The\_Trump\_Organization                           | Donald\_Trump | link |
| 9917693.0   | 4848272.0 | 599.0  | The\_Celebrity\_Apprentice                         | Donald\_Trump | link |
| 9289480.0   | 4848272.0 | 597.0  | The\_Apprentice\_(U.S.\_TV\_series)                | Donald\_Trump | link |
| 290327.0    | 4848272.0 | 596.0  | German\_American                                   | Donald\_Trump | link |
| 1.2643497e7 | 4848272.0 | 585.0  | Comedy\_Central\_Roast                             | Donald\_Trump | link |
| 3.7643999e7 | 4848272.0 | 549.0  | Republican\_Party\_presidential\_candidates,\_2016 | Donald\_Trump | link |
| 417559.0    | 4848272.0 | 543.0  | Alan\_Sugar                                        | Donald\_Trump | link |
| 1203316.0   | 4848272.0 | 489.0  | Fred\_Trump                                        | Donald\_Trump | link |
| 303951.0    | 4848272.0 | 426.0  | Vince\_McMahon                                     | Donald\_Trump | link |
| 6191053.0   | 4848272.0 | 413.0  | Jared\_Kushner                                     | Donald\_Trump | link |
| 1295216.0   | 4848272.0 | 412.0  | Trump\_Tower\_(New\_York\_City)                    | Donald\_Trump | link |
| 6509278.0   | 4848272.0 | 402.0  | Trump                                              | Donald\_Trump | link |

#### Top referrers to all presidential candidate pages

#### Load a visualization library

This code is copied after doing a live google search (by Michael Armbrust at Spark Summit East February 2016 shared from <https://twitter.com/michaelarmbrust/status/699969850475737088>). The `d3ivan` package is an updated version of the original package used by Michael Armbrust as it needed some TLC for Spark 2.2 on newer databricks notebook. These changes were kindly made by Ivan Sadikov from Middle Earth.

> Warning: classes defined within packages cannot be redefined without a cluster restart. Compilation successful.

    d3ivan.graphs.help()

<p class="htmlSandbox">
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
</p></p>

    d3ivan.graphs.force(
      height = 800,
      width = 1000,
      clicks = sql("""
        SELECT 
          prev_title AS src,
          curr_title AS dest,
          n AS count FROM clicks
        WHERE 
          curr_title IN ('Donald_Trump', 'Bernie_Sanders', 'Hillary_Rodham_Clinton', 'Ted_Cruz') AND
          prev_id IS NOT NULL AND prev_title != 'Main_Page'
        ORDER BY n DESC
        LIMIT 20""").as[d3ivan.Edge])

<p class="htmlSandbox">
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

var graph = {"nodes":[{"name":"Eric Trump"},{"name":"Hillary Rodham Clinton"},{"name":"Chelsea Clinton"},{"name":"Melania Trump"},{"name":"Ivana Trump"},{"name":"Barack Obama"},{"name":"Republican Party presidential candidates, 2016"},{"name":"Ivanka Trump"},{"name":"Donald Trump Jr."},{"name":"Democratic Party presidential candidates, 2016"},{"name":"Bernie Sanders"},{"name":"United States presidential election, 2016"},{"name":"Donald Trump"},{"name":"Barbara Bush"},{"name":"Ted Cruz"},{"name":"Condoleezza Rice"},{"name":"Laura Bush"},{"name":"Bill Clinton"},{"name":"John Kerry"}],"links":[{"source":17,"target":1,"value":355},{"source":7,"target":12,"value":233},{"source":11,"target":1,"value":144},{"source":11,"target":10,"value":132},{"source":12,"target":12,"value":111},{"source":3,"target":12,"value":93},{"source":4,"target":12,"value":89},{"source":18,"target":1,"value":76},{"source":5,"target":1,"value":64},{"source":9,"target":1,"value":60},{"source":15,"target":1,"value":59},{"source":11,"target":14,"value":55},{"source":16,"target":1,"value":54},{"source":8,"target":12,"value":54},{"source":13,"target":1,"value":54},{"source":2,"target":1,"value":48},{"source":11,"target":12,"value":46},{"source":9,"target":10,"value":41},{"source":6,"target":14,"value":40},{"source":0,"target":12,"value":37}]};

var width = 1000,
    height = 800;

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
</p>

### Convert raw data to parquet

[Apache Parquet](https://parquet.apache.org/) is a [columnar storage](http://en.wikipedia.org/wiki/Column-oriented_DBMS) format available to any project in the Hadoop ecosystem, regardless of the choice of data processing framework, data model or programming language. It is a more efficient way to store data frames.

-   To understand the ideas read [Dremel: Interactive Analysis of Web-Scale Datasets, Sergey Melnik, Andrey Gubarev, Jing Jing Long, Geoffrey Romer, Shiva Shivakumar, Matt Tolton and Theo Vassilakis,Proc. of the 36th Int'l Conf on Very Large Data Bases (2010), pp. 330-339](http://research.google.com/pubs/pub36632.html), whose Abstract is as follows:
    -   Dremel is a scalable, interactive ad-hoc query system for analysis of read-only nested data. By combining multi-level execution trees and columnar data layouts it is **capable of running aggregation queries over trillion-row tables in seconds**. The system **scales to thousands of CPUs and petabytes of data, and has thousands of users at Google**. In this paper, we describe the architecture and implementation of Dremel, and explain how it complements MapReduce-based computing. We present a novel columnar storage representation for nested records and discuss experiments on few-thousand node instances of the system.

<p class="htmlSandbox"><iframe 
 src="https://parquet.apache.org/documentation/latest/"
 width="95%" height="350"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

    // Convert the DatFrame to a more efficent format to speed up our analysis
    clickstream.
      write.
      mode(SaveMode.Overwrite).
      parquet("/datasets/wiki-clickstream") // warnings are harmless

#### Load parquet file efficiently and quickly into a DataFrame

Now we can simply load from this parquet file next time instead of creating the RDD from the text file (much slower).

Also using parquet files to store DataFrames allows us to go between languages quickly in a a scalable manner.

    val clicks = sqlContext.read.parquet("/datasets/wiki-clickstream")

> clicks: org.apache.spark.sql.DataFrame = \[prev\_id: int, curr\_id: int ... 4 more fields\]

    clicks.printSchema

> root |-- prev\_id: integer (nullable = true) |-- curr\_id: integer (nullable = true) |-- n: integer (nullable = true) |-- prev\_title: string (nullable = true) |-- curr\_title: string (nullable = true) |-- type: string (nullable = true)

    display(clicks)  // let's display this DataFrame

##### DataFrame in python

    clicksPy = sqlContext.read.parquet("/datasets/wiki-clickstream")

    clicksPy.show()

> +--------+-------+---+--------------------+--------------------+-----+ | prev\_id|curr\_id| n| prev\_title| curr\_title| type| +--------+-------+---+--------------------+--------------------+-----+ | 7009881| 164003| 21| Mayall| John\_Mayall| link| | 476786| 164003| 86| Mick\_Taylor| John\_Mayall| link| |19735547| 164003| 10|Peter\_Green\_disco...| John\_Mayall| link| | 244136| 164003| 10| Macclesfield| John\_Mayall| link| |33105755| 164003| 13| The\_Yardbirds| John\_Mayall| link| | 8910430| 164003| 34|The\_Turning\_Point...| John\_Mayall| link| | 329878| 164003| 10| Steve\_Marriott| John\_Mayall| link| | null| 164003|652| other-empty| John\_Mayall|other| | null| 147396|134| other-bing|John\_Mayall\_&\_the...|other| |17865484| 147396| 13|Timeline\_of\_heavy...|John\_Mayall\_&\_the...|other| |15580374| 147396| 94| Main\_Page|John\_Mayall\_&\_the...|other| | 168254| 147396| 23| Paul\_Butterfield|John\_Mayall\_&\_the...| link| | 322138| 147396|283|Peter\_Green\_(musi...|John\_Mayall\_&\_the...| link| | null| 147396| 79| other-other|John\_Mayall\_&\_the...|other| |12154926| 147396| 13|Marshall\_Bluesbre...|John\_Mayall\_&\_the...| link| | 223910| 147396| 12| Robben\_Ford|John\_Mayall\_&\_the...|other| |14433637| 147396| 10|Parchman\_Farm\_(song)|John\_Mayall\_&\_the...| link| | 476786| 147396|213| Mick\_Taylor|John\_Mayall\_&\_the...| link| |18952282| 147396| 13| Ric\_Grech|John\_Mayall\_&\_the...|other| | 4113741| 147396| 50|Rolling\_Stone's\_5...|John\_Mayall\_&\_the...| link| +--------+-------+---+--------------------+--------------------+-----+ only showing top 20 rows

Now you can continue from the original python notebook tweeted by Michael.

Recall from the beginning of this notebook that this python databricks notebook was used in the talk by Michael Armbrust at Spark Summit East February 2016 shared from <https://twitter.com/michaelarmbrust/status/699969850475737088>

(watch now, if you haven't already!)

[![Michael Armbrust Spark Summit East](http://img.youtube.com/vi/35Y-rqSMCCA/0.jpg)](https://www.youtube.com/watch?v=35Y-rqSMCCA)

**You Try!**

Try to laoad a DataFrame in R from the parquet file just as we did for python. Read the docs in databricks guide first: \* <https://docs.databricks.com/spark/latest/sparkr/overview.html>

And see the `R` example in the Programming Guide: \* <https://spark.apache.org/docs/latest/sql-programming-guide.html#parquet-files>.

    library(SparkR)

    # just a quick test
    df <- createDataFrame(faithful)
    head(df)

> ```
> Attaching package: ‘SparkR’
>
> The following objects are masked from ‘package:stats’:
>
>     cov, filter, lag, na.omit, predict, sd, var, window
>
> The following objects are masked from ‘package:base’:
>
>     as.data.frame, colnames, colnames<-, drop, intersect, rank, rbind,
>     sample, subset, summary, transform, union
> ```
>
> ```
>   eruptions waiting
> 1     3.600      79
> 2     1.800      54
> 3     3.333      74
> 4     2.283      62
> 5     4.533      85
> 6     2.883      55
> ```

    # Read in the Parquet file created above. Parquet files are self-describing so the schema is preserved.
    # The result of loading a parquet file is also a DataFrame.
    clicksR <- read.df("/datasets/wiki-clickstream", source = "parquet")
    clicksR # in R you need to put the object int its own line like this to get the type information

> ```
> ```
>
> ```
> SparkDataFrame[prev_id:int, curr_id:int, n:int, prev_title:string, curr_title:string, type:string]
> ```

    display(clicksR)

    # in Python you need to put the object int its own line like this to get the type information
    clicksPy 

> <span class="ansired">Out\[</span><span class="ansired">2</span><span class="ansired">\]: </span>DataFrame\[prev\_id: int, curr\_id: int, n: int, prev\_title: string, curr\_title: string, type: string\]

    head(clicksR)

> ```
> ```
>
> ```
>    prev_id curr_id  n                            prev_title  curr_title type
> 1  7009881  164003 21                                Mayall John_Mayall link
> 2   476786  164003 86                           Mick_Taylor John_Mayall link
> 3 19735547  164003 10               Peter_Green_discography John_Mayall link
> 4   244136  164003 10                          Macclesfield John_Mayall link
> 5 33105755  164003 13                         The_Yardbirds John_Mayall link
> 6  8910430  164003 34 The_Turning_Point_(John_Mayall_album) John_Mayall link
> ```

    -- FIXME (broke query, will get back to it later)
    SELECT *
    FROM clicks
    WHERE 
      prev_id IS NOT NULL
    ORDER BY n DESC
    LIMIT 20

| 1.5580374e7 | 4.4789934e7 | 769616.0 | Main\_Page                       | Deaths\_in\_2015                           | link  |
|-------------|-------------|----------|----------------------------------|--------------------------------------------|-------|
| 3.516685e7  | 4.0218034e7 | 368694.0 | Fifty\_Shades\_of\_Grey          | Fifty\_Shades\_of\_Grey\_(film)            | link  |
| 4.0218034e7 | 7000810.0   | 284352.0 | Fifty\_Shades\_of\_Grey\_(film)  | Dakota\_Johnson                            | link  |
| 3.5793706e7 | 3.7371793e7 | 253460.0 | Arrow\_(TV\_series)              | List\_of\_Arrow\_episodes                  | link  |
| 3.516685e7  | 4.3180929e7 | 249155.0 | Fifty\_Shades\_of\_Grey          | Fifty\_Shades\_Darker                      | link  |
| 4.0218034e7 | 6138391.0   | 228742.0 | Fifty\_Shades\_of\_Grey\_(film)  | Jamie\_Dornan                              | link  |
| 4.3180929e7 | 3.5910161e7 | 220788.0 | Fifty\_Shades\_Darker            | Fifty\_Shades\_Freed                       | link  |
| 2.7676616e7 | 4.0265175e7 | 192321.0 | The\_Walking\_Dead\_(TV\_series) | The\_Walking\_Dead\_(season\_5)            | link  |
| 6138391.0   | 1076962.0   | 185700.0 | Jamie\_Dornan                    | Amelia\_Warner                             | link  |
| 1.9376148e7 | 4.4375105e7 | 185449.0 | Stephen\_Hawking                 | Jane\_Wilde\_Hawking                       | link  |
| 2.7676616e7 | 2.8074027e7 | 161407.0 | The\_Walking\_Dead\_(TV\_series) | List\_of\_The\_Walking\_Dead\_episodes     | link  |
| 3.4149123e7 | 4.1844524e7 | 161081.0 | List\_of\_The\_Flash\_episodes   | The\_Flash\_(2014\_TV\_series)             | other |
| 1.1269605e7 | 1.3542396e7 | 156313.0 | The\_Big\_Bang\_Theory           | List\_of\_The\_Big\_Bang\_Theory\_episodes | link  |
| 3.9462431e7 | 3.4271398e7 | 152892.0 | American\_Sniper\_(film)         | Chris\_Kyle                                | link  |
| 1.5580374e7 | 1738148.0   | 148820.0 | Main\_Page                       | Limpet                                     | other |
| 1.5580374e7 | 4.5298077e7 | 140335.0 | Main\_Page                       | TransAsia\_Airways\_Flight\_235            | other |
| 7000810.0   | 484101.0    | 139682.0 | Dakota\_Johnson                  | Melanie\_Griffith                          | link  |
| 4.511931e7  | 4.256734e7  | 138179.0 | Take\_Me\_to\_Church             | Take\_Me\_to\_Church\_(Hozier\_song)       | link  |
| 3.8962787e7 | 4.1126542e7 | 136236.0 | The\_Blacklist\_(TV\_series)     | List\_of\_The\_Blacklist\_episodes         | link  |
| 3.2262767e7 | 4.5305174e7 | 135900.0 | Better\_Call\_Saul               | Uno\_(Better\_Call\_Saul)                  | link  |

