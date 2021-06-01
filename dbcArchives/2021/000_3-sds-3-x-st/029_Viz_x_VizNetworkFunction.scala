// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Function for visualizing a Twitter network
// MAGIC 
// MAGIC by Olof Björck, Joakim Johansson, Rania Sahioun, Raazesh Sainudiin and Ivan Sadikov
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks, AWS and a Swedish VR grant.
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2016-2020 Olof Björck, Joakim Johansson, Rania Sahioun, Ivan Sadikov and Raazesh Sainudiin
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

/** Displays an interactive visualization of a Twitter network.
 *
 *  This function takes a DataFrame from TTTDFfunctions function tweetsDF2TTTDF()
 *  and displays it in an interactive visualization. The DataFrame content should
 *  be a collection of Twitter users.
 *  
 *  Visualization description:
 *
 *  This is a visualization of a Twitter network. Each circle represents a Twitter 
 *  user (that is, a unique Twitter account). The color of the user circle represents 
 *  how many followers the user has from blue (fewest) to red (most). The radius of 
 *  the user circle also represents how many followers the user has from small radius 
 *  (fewest) to large radius (most) on a log scale. The x-axis shows the number of 
 *  Retweets of a user within the Twitter network (that is, the number of times the 
 *  user was Retweeted by users in this specific Twitter network). The y-axis shows 
 *  the number of unique Retweeters within the Twitter network (that is, the number 
 *  of unique users in this specific Twitter network that Retweeted a user). If you 
 *  hover over a user circle, the user screen name (as specified in the data) will 
 *  appear by the user circle and the user circle you're hovering will become enlarged.
 *  
 *  @param TTTDF A DataFrame from TTTDFfunctions function tweetsDF2TTTDF() containing
 *               the data to be displayed.
 *  @param width The display width in pixels.
 *  @param height The display height in pixels.
 */
def visualizeNetwork(TTTDF: DataFrame, width: Int = 900, height: Int = 400): Unit = {
  
  // Get all retweets and their weights (weights == 1 as we haven't done anything yet)
  val RTNetworkDF = TTTDF.filter($"TweetType"==="ReTweet").select("OPostUserIdinRT","CPostUserId","Weight")
  
  // Get all (directed) retweet tuples
  val weightedRTNetworkDF = RTNetworkDF
                            .groupBy("OPostUserIdinRT","CPostUserId")
                            .agg(sum("Weight").as("Weight"))
                            .orderBy($"Weight".desc)
  
  // Get the out degree of each user. That is, get only the number of times a user is retweeted. We're now losing the tuples.
  val outDegreeOfOPostUserIdinRTNetwork = weightedRTNetworkDF
                                          .select("OPostUserIdinRT","Weight")
                                          .groupBy("OPostUserIdinRT")
                                          .agg(sum("Weight").as("Weight"))
                                          .orderBy($"Weight".desc)
  
  // Get number of unique retweeters (out degree neighborhood):
  // Use weightedRTNetwork, drop weight, add new weight of 1. 
  // We got all the retweet tuples with a weight of 1, then group by UserID to count number of unique retweeters
  val outNgbhdOfOPostUserIdinRTNetwork = weightedRTNetworkDF
                                          .drop("Weight")
                                          .withColumn("Weight",lit(1L))
                                          .groupBy("OPostUserIdinRT")
                                          .agg(sum("Weight").as("Weight"))
                                          .orderBy($"Weight".desc) 
  
  // Combine retweets and retweeters count and rename columns
  val tweetsAndRetweets = outDegreeOfOPostUserIdinRTNetwork
                          .toDF(Seq("UserID", "NrOfRetweets"): _*)
                          .join(outNgbhdOfOPostUserIdinRTNetwork
                                .toDF(Seq("UserID", "NrOfRetweeters"): _*), "UserID"
                               ).orderBy($"NrOfRetweets".desc)
  
  // Get user info from the "primary" users in our network. They are the users we've specified.
  val usersInfo = TTTDF.filter($"TweetType"==="ReTweet")
                  .select("CPostUserID", "CPostUserSN", "followersCount")
                  .groupBy("CPostUserID", "CPostUserSN")
                  .agg(max("followersCount").as("followersCount"))
                  .toDF(Seq("UserID", "ScreenName", "followersCount"): _*)
  
  // Get the final data to visualize
  val data = usersInfo.join(tweetsAndRetweets, Seq("UserID")).distinct.toJSON.collect
  
  // CSS code
  val visualizeNetworkCSS: String = 
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
  
  .title {
      fill: black;
      font-family: sans-serif;
      text-anchor: middle;
      text-align: center;
      font-size: 1.5em;
  }

  #tweet {
      position: absolute;
      z-index: 100;
      left: 80%;
      height: auto;
  }
  """;
  
  // JavaScript code
  val visualizeNetworkJS: String = 
  s"""
  /*******************************************************************************

  This visualization is an overview of a Twitter network.

  Contained in the visualisation is, for each user:

   - Number of retweets
   - Number of individual retweeters
   - Number of followers

  *******************************************************************************/

  // TODO: Add search option

  /*
  Create accessors that specify data from the csv-file
  */
  function x(d) { return d.NrOfRetweets; }
  function y(d) { return d.NrOfRetweeters; }
  function radius(d) { return d.followersCount; }
  function color(d) { return d.followersCount; } // What to do here?
  function name(d) { return d.ScreenName; }
  function id(d) { return d.UserID; }

  /*
  Create id-functions
  */
  function getCircleId(d) { return "circ" + id(d); }
  function getTextId(d) { return "text" + id(d); }

  /*
  Specify circle constants
  */
  var circleMaxRadius = 8;
  var circleMinRadius = 3;
  var circleEnlargeConstant = 2;
  var circleIdleOpacity = 0.2;
  var circleActiveOpacity = 1;
  var circleClickedStrokeWidth = 4;

  /*
  Create svg and specify display sizes
  */
      
  // Specify display sizes.
  var width = ${width}, 
      height = ${height},
      margin = {
                top: 0.1 * height,
                right: 0.05 * width, 
                bottom: 0.1 * height,
                left: 0.05 * width
            };
            
  width = width - margin.left - margin.right;
  height = height - margin.top - margin.bottom;

  // Get div
  var div = d3.select("#treeVisualizationDiv");

  // Create svg
  var svg = div.append('svg')
      .attr("class", "visualization")
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append("g").attr("id", "inner-space")
          .attr("transform",
              "translate(" + margin.left + "," + margin.top + ")");

  /*
  Create title
  */
  var title = svg.append("text")
      .attr("class", "title") // style in css
      .attr("x", width / 2)
      .attr("y", 0)
      .text("Twitter network");

  /*
  Create x-axis
  */

  // Create x-scale
  var xScale = d3.scaleLog()
      .range([0, width]);

  // Create x-axis
  var xAxis = d3.axisBottom(xScale)
      .ticks(5, d3.format(",d"))

  // Create "g" for displaying of x-axis
  var gXAxis = svg.append("g")
      .attr("class", "x axis")
      // Position at bottom
      .attr("transform", "translate(" + 0 + "," + height + ")")

  // Create x-axis label.
  var xAxisLabel = svg.append("text")
      .attr("class", "x label")
      .attr("text-anchor", "end")
      .attr("x", width)
      .attr("y", height - 6)
      .text("Number of retweets");

  /*
  Create y-axis
  */

  // Create y-scale
  var yScale = d3.scaleLinear()
      .range([height, 0]);

  // Create y-axis
  var yAxis = d3.axisLeft(yScale)

  // Create "g" for displaying of y-axis
  var gYAxis = svg.append("g")
      .attr("class", "y axis")

  // Create y-axis label
  var yAxisLabel = svg.append("text")
      .attr("class", "y label")
      .attr("text-anchor", "end")
      .attr("y", 6)
      .attr("dy", ".75em")
      .attr("transform", "rotate(-90)")
      .text("Number of unique retweeters");


  /*
  Create scale for radius
  */
  var radiusScale = d3.scaleLog()
      .base(10)
      .range([circleMinRadius, circleMaxRadius])

  /*
  Create scale for color
  */
  var colorScale = d3.scaleLinear()
      .range(["blue", "red"])

  /*
  Create zoom
  */
  var zoom = d3.zoom()
      .scaleExtent([0.5, Infinity])
      .on("zoom", zoomed);

  // Create zoomable area
  var zoomView = svg.append("rect")
      .attr("class", "zoom")
      .attr("width", width)
      .attr("height", height)
      .call(zoom)
      .on("click", clickView)

  // Add data. Each row represented as a "g" of class "node" inside the svg.
  var data = ${data.mkString("[", ",\n", "]")};

  xScale.domain([1, getMaxX(data)])
  gXAxis.call(xAxis)
  yScale.domain([0, getMaxY(data)])
  gYAxis.call(yAxis)
  radiusScale.domain([1, getMaxRadius(data)])
  colorScale.domain([1, getMaxColor(data)])

  // Enter the data
  var nodes = svg.append("g").selectAll("g")
      .data(data)
      .enter()

  // Create circles to display the data
  nodes.append("circle")
      .call(setCircleAttributes)
      .call(setCircleMouseEvents)
      .sort(orderLargestBelow)
  // Create tooltip that shows username
  nodes.append("text")
      .call(setTextAttributes)

  /**
   * Set attributes for circles (Twitter account nodes).
   */
  function setCircleAttributes(circle) {
      circle
          .attr("class", "nodeCircle")
          .attr("data-id", id)
          .attr("id", getCircleId)
          .attr("opacity", circleIdleOpacity)
          .attr("fill", function(d) { return colorScale(color(d)); })
          .attr("stroke", "black")
          .attr("stroke-width", 0)
          .attr("r", function(d) { return radiusScale(radius(d)); })
          .attr("cx", function(d) { return xScale(x(d)); })
          .attr("cy", function(d) { return yScale(y(d)); })
  }

  /**
   * Set mouse events for circles.
   */
  function setCircleMouseEvents(circle) {
      circle
          // Add tooltip and enlarge circle on mouse hover
          .on("mouseover", mouseoverCircle)
          // Remove tooltip and restore circle on mouseout
          .on("mouseout", mouseoutCircle)
          // Display timeline on click
          .on("click", clickCircle)
  }

  /**
   * Set attributes for tooltip (showing screen name) text.
   */
  function setTextAttributes(text) {
      text
          .attr("class", "hidden nodeText") // Set class to hidden upon creation
          .attr("data-id", id)
          .attr("id", getTextId)
          .attr("x", function(d) { return xScale(x(d)); })
          .attr("y", function(d) { return yScale(y(d)); })
          .attr("dy", function(d) { return - (circleMaxRadius * circleEnlargeConstant * 1.5); })
          .attr("text-anchor", "beginning")
          .text(function(d) { return name(d); })
  }

  /**
   * Order so that largest circle gets placed deepest.
   */
  function orderLargestBelow(a, b) {
      return radius(b) - radius(a);
  }

  /**
   * Handle mouse hover on circle. Display circle's screen name.
   */
  function mouseoverCircle() {

      // Get circle
      var circle = d3.select(this);

      // Display activated circle
      circle.attr("r", circle.attr("r") * circleEnlargeConstant);
      circle.attr("opacity", circleActiveOpacity);

      // Set text class to visible
      svg.select("#text" + circle.attr("data-id"))
          .classed("hidden", false)
          .classed("visible", true)

  }

  /**
   * Handle zoom. Zoom both x-axis and y-axis.
   */
  function mouseoutCircle() {

      // Get circle
      var circle = d3.select(this);

      // Display idle circle
      circle.attr("r", circle.attr("r") / circleEnlargeConstant);
      circle.attr("opacity", circleIdleOpacity);

      // Set text class to hidden
      svg.select("#text" + circle.attr("data-id"))
          .classed("visible", false)
          .classed("hidden", true)

  }

  /**
   * Handle zoom. Zoom both x-axis and y-axis.
   */
  function zoomed() {

      // Create new x- and y-scale
      var new_xScale = d3.event.transform.rescaleX(xScale);
      var new_yScale = d3.event.transform.rescaleY(yScale);

      // Display new axes
      gXAxis.call(xAxis.scale(new_xScale));
      gYAxis.call(yAxis.scale(new_yScale));

      // Reposition circles
      d3.selectAll(".nodeCircle")
          .attr("cx", function(d) { return new_xScale(x(d)); })
          .attr("cy", function(d) { return new_yScale(y(d)); })
          // To force constant circle radius on zoom:
          //.attr("r", function(d) { return d3.event.transform.scale(radiusScale(radius(d))).k; })

      // Reposition texts
      d3.selectAll(".nodeText")
          .attr("x", function(d) { return new_xScale(x(d)); })
          .attr("y", function(d) { return new_yScale(y(d)); })

  };

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
              userId: id(d)
          },
          document.getElementById("tweet"), // Tweet div
          {
              height: height
          }
      )
  }

  /**
   * Returns the largest x-value in the data.
   */
  function getMaxX(data) {

      return Math.max(...data.map(d => x(d)));
  }

  /**
   * Returns the largest y-value in the data.
   */
  function getMaxY(data) {

      return Math.max(...data.map(d => y(d)));
  }

  /**
   * Returns the largest radius in the data.
   */
  function getMaxRadius(data) {

      return Math.max(...data.map(d => radius(d)));
  }

  /**
   * Returns the "largest" color in the data.
   */
  function getMaxColor(data) {

      var maxColor = Math.max(...data.map(d => color(d)));
      var cutOff = 10000;

      if (maxColor > cutOff) return cutOff;
      return maxColor;
  }

  """
  
  // HTML code
  displayHTML(s"""
  <!DOCTYPE html>
  <html lang="en">

      <head>

          <meta charset="utf-8">
          <meta name="author" content="Olof Björck">
          <title>Network</title>

          <style>
            ${visualizeNetworkCSS}
          </style>

      </head>

      <body>

          <div id="treeVisualizationDiv" class="visualization" align="center">
              <div id="tweet"></div>
          </div>

          <script sync src="https://platform.twitter.com/widgets.js"></script>
          <script src="https://d3js.org/d3.v4.min.js"></script>
          <script>
            ${visualizeNetworkJS}
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

visualizeNetwork(TTTDF)

****************
""")

// COMMAND ----------

