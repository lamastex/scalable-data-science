// Databricks notebook source
// MAGIC %md
// MAGIC # Explore a Twitter network
// MAGIC 
// MAGIC by Olof Björck, Joakim Johansson, Rania Sahioun, Raazesh Sainudiin and Ivan Sadikov
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program and AWS Cloud Computing Credits for Research.
// MAGIC 
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

// MAGIC %md
// MAGIC ### Read data

// COMMAND ----------

// Get data
val df_raw = spark.read.parquet("dbfs:/datasets/MEP/SE/RetrospectiveMany/*")

// COMMAND ----------

// MAGIC %md
// MAGIC Import some functions used for data cleaning

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

// Get cleaned data using functions imported above
val df = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(df_raw.select("statusJsonString"))).cache()
df.count

// COMMAND ----------

// Look at the data we're working with
display(df)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Get display data (number of retweets, number of retweeters, ...)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Compute how many retweets each user have

// COMMAND ----------

// Get all retweets and their weights (weights == 1 as we haven't done anything yet)
val RTNetworkDF = df.filter($"TweetType"==="ReTweet").select("OPostUserIdinRT","CPostUserId","Weight")

RTNetworkDF.show(1)

// COMMAND ----------

// How many rows do we have?
RTNetworkDF.count

// COMMAND ----------

// Get all (directed) retweet tuples
val weightedRTNetworkDF = RTNetworkDF.groupBy("OPostUserIdinRT","CPostUserId").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc)

weightedRTNetworkDF.show(1)

// COMMAND ----------

// How many tuples do we have?
weightedRTNetworkDF.distinct.count

// COMMAND ----------

// Get the out degree of each user. That is, get only the number of times a user is retweeted. We're now losing the tuples.
val outDegreeOfOPostUserIdinRTNetwork = weightedRTNetworkDF.select("OPostUserIdinRT","Weight").groupBy("OPostUserIdinRT").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc)

outDegreeOfOPostUserIdinRTNetwork.show(1)

// COMMAND ----------

// How many users do we have?
outDegreeOfOPostUserIdinRTNetwork.count

// COMMAND ----------

// MAGIC %md
// MAGIC #### Copute the number of unique retweeters

// COMMAND ----------

weightedRTNetworkDF.show(5)

// COMMAND ----------

weightedRTNetworkDF.filter( $"OPostUserIdinRT" === 104778698L).count

// COMMAND ----------

// Get number of unique retweeters (out degree neighborhood)
val outNgbhdOfOPostUserIdinRTNetwork = weightedRTNetworkDF.drop("Weight").withColumn("Weight",lit(1L)).groupBy("OPostUserIdinRT").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc) // Use weightedRTNetwork, drop weight, add new weight of 1. We got all the retweet tuples with a weight of 1, then group by UserID to count number of unique retweeters

outNgbhdOfOPostUserIdinRTNetwork.show(10)

// COMMAND ----------

// How many users do we have? Should be the same count as outDegree count
outNgbhdOfOPostUserIdinRTNetwork.count

// COMMAND ----------

// MAGIC %md
// MAGIC #### Combine into one dataframe and rename columns

// COMMAND ----------

// Combine retweets and retweeters count and rename columns
val tweetsAndRetweets = outDegreeOfOPostUserIdinRTNetwork
                        .toDF(Seq("UserID", "NrOfRetweets"): _*)
                        .join(outNgbhdOfOPostUserIdinRTNetwork
                              .toDF(Seq("UserID", "NrOfRetweeters"): _*), "UserID"
                             ).orderBy($"NrOfRetweets".desc)
                        

tweetsAndRetweets.show(10)

// COMMAND ----------

// Should be the same count as outDegree count and outNgbhd count
tweetsAndRetweets.count

// COMMAND ----------

// MAGIC %md
// MAGIC ### Get followers count and screen names

// COMMAND ----------

// Get user info from the "primary" users in our network. They are the users we've specified.
val usersInfo = df.filter($"TweetType"==="ReTweet")
                  .select("CPostUserID", "CPostUserSN", "followersCount")
                  .groupBy("CPostUserID", "CPostUserSN")
                  .agg(max("followersCount").as("followersCount"))
                  .toDF(Seq("UserID", "ScreenName", "followersCount"): _*)

usersInfo.select("UserID").count

// COMMAND ----------

// MAGIC %md
// MAGIC ### Combine into one dataframe

// COMMAND ----------

val df_final = usersInfo.join(tweetsAndRetweets, Seq("UserID")).distinct

df_final.show(1)

// COMMAND ----------

df_final.printSchema

// COMMAND ----------

/*********************************************

NOTE: WE'RE ONLY LOOKING AT OUR ORIGINAL IDs, NOT EVERY USER IN THE NETWORK

*********************************************/
df_final.count 

// COMMAND ----------

// MAGIC %md
// MAGIC ### Visualize

// COMMAND ----------

// Data to draw on HTML page.
val data = df_final.toJSON.collect

// COMMAND ----------

displayHTML(s"""

<!DOCTYPE html>
<html lang="en">

	<head>

	    <meta charset="utf-8">
	    <meta name="author" content="Olof Björck">
	    <title>User Timeline</title>

	    <style>
        
        body {
          font-family: Sans-Serif;
          width: 100%;
          height: 100%;
          margin:0;
          overflow: scroll;
        }

        .title {
            fill: black;
            font-family: sans-serif;
            text-anchor: middle;
            text-align: center;
            font-size: 1.5em;
        }

        .zoom {
            cursor: move;
            fill: none;
            pointer-events: fill;
        }

        .hidden {
            visibility: hidden;
        }

        .visible {
            visibility: visible;
        }

        #tweet {
            position: absolute;
            float: right;
            left: 80%;
            height:100%;
        }
        
        </style

	</head>

	<body>

		<div id="treeVisualizationDiv" class="visualization" align="center">
			<div id="tweet"></div>
		</div>

		<script sync src="https://platform.twitter.com/widgets.js"></script>
		<script src="https://d3js.org/d3.v4.min.js"></script>
		<script type="text/javascript">
        
            /*******************************************************************************

            This visualization is an overview of a Twitter network.

            Contained in the visualisation is, for each user:

             - Number of retweets
             - Number of individual retweeters
             - Number of followers

            *******************************************************************************/

            // TODO: Set range on data input
            // TODO: Add search option
            // TODO: Fix Twitter timeline pop up on click

            /*
            Define our file name
            */
            var filename = "df_final";

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
            Create our user of interest
            */
            var userOfInterest = {
                UserID: "104778698",
            }

            /*
            Create id-functions
            */
            function getCircleId(d) { return "circ" + id(d); }
            function getTextId(d) { return "text" + id(d); }

            /*
            Specify circle constants
            */
            var circleMaxRadius = 5;
            var circleMinRadius = 0;
            var circleEnlargeConstant = 2;
            var circleIdleOpacity = 0.2;
            var circleActiveOpacity = 1;
            var circleClickedStrokeWidth = 4;

            /*
            Create svg and specify display sizes
            */

            var totalWidth = 1080, 
                totalHeight = 700;

            // Don't modify these width and height directly.
            // Account for bunch of other elements on the webpage
            var width = totalWidth - 100, 
                height = totalHeight - 200;

            // Specify display sizes.
            var margin = {
                    top: 0.05 * height,
                    right: 0.15 * width, 
                    bottom: 0.1 * height,
                    left: 0.15 * width 
            };

            // Get div
            var div = d3.select("#treeVisualizationDiv");

            // Create svg
            var svg = div.append('svg')
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
                .text("Overview of Twitter network in " + filename);

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
                .base(2)
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

                // TODO: Fix this. Should display based on data max/min
                xScale.domain([1, 10000])
                gXAxis.call(xAxis)
                yScale.domain([0, 350])
                gYAxis.call(yAxis)
                radiusScale.domain([1, 100000])
                colorScale.domain([1, 10000])

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

                // Set appearance for user of interest
                d3.select("#" + getCircleId(userOfInterest))
                    .attr("fill", "orange")

            

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

        
        </script>

	</body>
    
</html>

""")

// COMMAND ----------

// MAGIC %run Users/olof.bjorck@gmail.com/VizNetworkFunction

// COMMAND ----------

visualizeNetwork(df)

// COMMAND ----------

