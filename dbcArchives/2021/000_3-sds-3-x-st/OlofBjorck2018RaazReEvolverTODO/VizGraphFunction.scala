// Databricks notebook source
// MAGIC %md
// MAGIC # Function for visualizing a Twitter graph
// MAGIC 
// MAGIC by Olof Björck, Joakim Johansson, Rania Sahioun, Raazesh Sainudiin and Ivan Sadikov
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program and AWS Cloud Computing Credits for Research.
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2018 Olof Björck, Joakim Johansson, Rania Sahioun, Raazesh Sainudiin and Ivan Sadikov
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

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

/** Displays an interactive visualization of a Twitter graph.
 *
 *  This function takes a DataFrame from TTTDFfunctions function tweetsDF2TTTDF()
 *  and displays it in an interactive visualization. The DataFrame content should
 *  be a collection of Twitter users. 
 *  
 *  Visualization description:
 *
 *  This is a graph visualization of a Twitter graph. Each circle represents a 
 *  Twitter user (that is, a unique Twitter account). The radius of the user circle 
 *  represents how many Retweets the user has within this specific Twitter network 
 *  from small radius (fewest Retweets) to large radius (most Retweets). If you 
 *  hover a user circle, the user screen name (as specified in the data) will appear 
 *  by the user circle. The line thickness connecting user circles represents how 
 *  many Retweets the user circle tuple tuple has between each other in total.
 *  Retweet direction is not accounted for. Connecting lines only appear if the user 
 *  circle tuple has at least tupleWeightCutOff Retweets.
 *  
 *  @param TTTDF A DataFrame from TTTDFfunctions function tweetsDF2TTTDF() containing
 *               the data to be displayed.
 *  @param tupleWeightCutOff The display width in pixels.
 *  @param width The display width in pixels.
 *  @param height The display height in pixels.
 */
def visualizeGraph(TTTDF: DataFrame, tupleWeightCutOff: Int = 15, width: Int = 900, height: Int = 400): Unit = {

  // Get user info from the "primary" users in our network. They are the users we've specified.
  val usersInfo = TTTDF
                  .select("CPostUserID", "CPostUserSN", "followersCount")
                  .groupBy("CPostUserID", "CPostUserSN")
                  .agg(max("followersCount").as("followersCount"))
                  .toDF(Seq("id", "ScreenName", "followersCount"): _*)
                  .select("id", "ScreenName")
  
  // Get all retweets and their weights (weights == 1 as we haven't done anything yet)
  val RTNetworkDF = TTTDF
                    .filter($"TweetType"==="ReTweet") // TweetType PARAM
                    .select("OPostUserSNinRT", "CPostUserSN", "OPostUserIdinRT","CPostUserId","Weight")
  
  // Get all (directed) retweet tuples // PARAM - case by case depending on the TweetType PARAM since SN RT QT etc are Akin's TTTDF SQL names
  val weightedRTNetworkDF = RTNetworkDF
                            .groupBy("OPostUserSNinRT", "CPostUserSN", "OPostUserIdinRT","CPostUserId")
                            .agg(sum("Weight").as("Weight"))
                            .orderBy($"Weight".desc)
                            .toDF("source", "target", "sourceId", "targetId", "weight")
  
  // Get the out degree of each user. That is, get only the number of times a user is retweeted. We're now losing the tuples.
  val outDegreeOfOPostUserIdinRTNetwork = weightedRTNetworkDF
                                          .select("sourceId","weight")
                                          .groupBy("sourceId")
                                          .agg(sum("weight").as("weight"))
                                          .orderBy($"weight".desc)
                                          .toDF(Seq("id", "weight"): _*)

  // Get the nodes
  val nodes = usersInfo
              .join(outDegreeOfOPostUserIdinRTNetwork, Seq("id")).withColumn("group", lit(1))
              .toDF(Seq("idNr", "id", "weight", "group"): _*)
              .where("id is not null")
              .toDF(Seq("idNr", "id", "weight", "group"): _*)

  // Get links
  val linksSource = nodes.select("idNr")
                    .join(weightedRTNetworkDF, $"idNr" === $"sourceId", "left_outer")
                    .select("source", "target", "sourceId", "targetId", "weight")
  val links = nodes.select("idNr")
              .join(linksSource, $"idNr" === $"targetId", "left_outer")
              .select("source", "target", "weight")
              .where("source is not null")
              .where($"weight" >= tupleWeightCutOff)
              .toDF(Seq("source", "target", "weight"): _*)
 
  // Get JSON data
  val nodesData = nodes.toJSON.collect;
  val linksData = links.toJSON.collect;

  // CSS code
  val visualizeGraphCSS: String = 
  s"""
  /*
  General styling
  */

  body {
      font-family: Sans-Serif;
      width: 100%;
      height: 100%;
      margin: 0;
      margin-bottom: 100px;
      background-color: #FFF;
      overflow: scroll;
  }

  /*
  Page content
  */

  div.start {
      text-align: center;
  }

  ul.start {
      margin-top: 0;
      display: inline-block;
      text-align: left;
  }

  div.text {
      text-align: left;
      margin-left: 19.1%;
      margin-right: 19.1%;
  }

  /*
  For visualizations
  */

  .visualization {
      display: block;
      position: relative;
      align: center;
  }

  .hidden {
      visibility: hidden;
  }

  .visible {
      visibility: visible;
  }

  .zoom {
      cursor: move;
      fill: none;
      pointer-events: fill;
  }

  #graphVisualizationDiv {
      width: auto;
      height: auto;
      position: relative;
      align: center;
  }

  .links line {
      stroke: #999;
      stroke-opacity: 1;
  }

  .nodes circle {
      fill: black;
  }

  circle.clicked {
      fill: #1da1f2;
  }

  #tweet {
      z-index: 100;
      position: absolute;
      left: 80%;
      height: auto;
  }
  """
  
  // JavaScript code
  val visualizeGraphJS: String = 
  s"""
  /*******************************************************************************

  This visualisation is a Twitter graph.

  *******************************************************************************/

  var circleEnlargeConstant = 2;
  var circleClickedStrokeWidth = 5;
  var maxRadius = 10;
  
  // Specify display sizes.
  var width = ${width}, 
      height = ${height},
      margin = {
                top: 0.1 * height,
                right: 0 * width, 
                bottom: 0.1 * height,
                left: 0 * width
            };
            
  width = width - margin.left - margin.right;
  height = height - margin.top - margin.bottom;

  // Get div
  var div = d3.select("#graphVisualizationDiv");

  // Create svg
  var svg = div.append("svg")
      .attr("class", "visualization")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
          .attr("transform",
              "translate(" + margin.left + "," + margin.top + ")");

  // Create zoom
  var zoom = d3.zoom()
      .on("zoom", zoomed);

  // Create zoomable area
  var zoomView = svg.append("rect")
      .attr("class", "zoom")
      .attr("width", width)
      .attr("height", height)
      .on("click", clickView)
      .call(zoom)

  // Create simulation
  var simulation = d3.forceSimulation()
      .force("charge", d3.forceManyBody().strength(-10))
      .force("center", d3.forceCenter(width / 2, height / 2))

  // Create loading text
  var loading = svg.append("text")
      .attr("y", height / 2)
      .attr("x", width / 2)
      .attr("text-anchor", "middle")
      .text("Loading graph... Takes a couple of seconds");
 
  var nodes = ${nodesData.mkString("[", ",\n", "]")};
  var links = ${linksData.mkString("[", ",\n", "]")};

  // Create links
  var link = svg.append("g")
      .attr("class", "links")
      .selectAll("line")
      .data(links)
      .enter().append("line")
          .attr("stroke-width", function(d) { return Math.sqrt(d.weight / 1000); });

   // Create nodes
   var node = svg.append("g")
       .attr("class", "nodes")
       .selectAll("circle")
       .data(nodes)
       .enter().append("circle")
           //.attr("fill", function(d) { return color(d.group); })
           .attr("r", function(d) { return Math.sqrt(d.weight / 100) + 2 })
           .on("mouseover", mouseoverCircle)
           .on("mouseout", mouseoutCircle)
           .on("click", clickCircle)
           .call(d3.drag()
               .on("start", dragstarted)
               .on("drag", dragged)
               .on("end", dragended));

    // Add title as child to circle
    node.append("title")
        .text(function(d) { return d.id; });

    // Link nodes and links to the simulation
    simulation
        .nodes(nodes)
        .on("tick", ticked)
        .force('link', d3.forceLink(links).id(function(d) { return d.id; }));

    // Updates for each simulation tick
    function ticked() {
        link
            .attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });

         node
            .attr("cx", function(d) { return d.x; })
            .attr("cy", function(d) { return d.y; })
     }

     // Compute several steps before rendering
     loading.remove(); // Remove loading text
     for (var i = 0, n = 150; i < n; ++i) {
         simulation.tick();
     }

  /**
   * Handle mouse hover on circle. Enlarge circle.
   */
  function mouseoverCircle() {

      // Get circle
      var circle = d3.select(this);

      // Display activated circle
      circle.attr("r", circle.attr("r") * circleEnlargeConstant);
  }

  /**
   * Handle mouse out on circle. Resize circle.
   */
  function mouseoutCircle() {

      // Get circle
      var circle = d3.select(this);

      // Display idle circle
      circle.attr("r", circle.attr("r") / circleEnlargeConstant);

  }

  /**
   * Handle circle drag start.
   */
  function dragstarted(d) {
      if (!d3.event.active) simulation.alphaTarget(0.3).restart();
      d.fx = d.x;
      d.fy = d.y;
  }

  /**
   * Handle circle drag.
   */
  function dragged(d) {
      d.fx = d3.event.x;
      d.fy = d3.event.y;
  }

  /**
   * Handle circle drag end.
   */
  function dragended(d) {
      if (!d3.event.active) simulation.alphaTarget(0);
      d.fx = null;
      d.fy = null;
  }

  /**
   * Handle zoom. Zoom both x-axis and y-axis.
   */
  function zoomed() {
      d3.selectAll(".nodes").attr("transform", d3.event.transform)
      d3.selectAll(".links").attr("transform", d3.event.transform)
  }

  /**
   * Handle click on zoomable area. That is, handle click outside a node which
   * is considered a deselecting click => deselect previously clicked node
   * and remove displayed tweets.
   */
  function clickView() {

      // Remove clicked status on clicked nodes
      d3.selectAll(".clicked")
          .attr("stroke-width", "0")
          .classed("clicked", false)

      // Remove timeline
      document.getElementById("tweet").innerHTML = ""
  }

  /**
   * Handle click on a tweet circle. Display the clicked tweet and let the tweet
   * appear selected by adding a stroke to it.
   */
  function clickCircle(d) {

      // Remove results from old click
      clickView();

      // Add stroke width and set clicked class
      d3.select(this)
          .attr("stroke-width", circleClickedStrokeWidth)
          .classed("clicked", true);

      // Display tweet
      twttr.widgets.createTimeline(
          {
              sourceType: "profile",
              userId: d.idNr
          },
          document.getElementById("tweet"), // Tweet div
          {
              height: height
          }
      )
  }
  """

  // HTML code
  displayHTML(s"""
  <!DOCTYPE html>
  <html lang="en">

      <head>

          <meta charset="utf-8">
          <meta name="author" content="Olof Björck">
          <title>Graph</title>

          <style>
            ${visualizeGraphCSS}
          </style>

      </head>

      <body>

          <div id="graphVisualizationDiv" class="visualization" align="center">
              <div id="tweet"></div>
          </div>

          <script sync src="https://platform.twitter.com/widgets.js"></script>
          <script src="https://d3js.org/d3.v4.min.js"></script>
          <script>
            ${visualizeGraphJS}
          </script>

      </body>
  </html>
""")
}

// Info
println("""
*** WARNING: ***

THERE IS NO SIZE CHECKING! Driver might crash as DataFrame.collect is used.
Also, the display can only handle so and so many items at once. Too large
dataset and the visualization might be very laggy or crash.

***  USAGE:  ***

visualizeGraph(TTTDF)

****************
""")

// COMMAND ----------

