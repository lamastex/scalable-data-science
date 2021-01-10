// Databricks notebook source
// MAGIC %md
// MAGIC # Function for visualizing a Twitter timeline
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

/** Displays an interactive visualization of a Twitter timeline.
 *
 *  This function takes a DataFrame from TTTDFfunctions function tweetsDF2TTTDF()
 *  and displays it in an interactive visualization. The DataFrame content should
 *  be a Twitter user timeline.
 *  
 *  Visualization description:
 *
 *
 *  This is a visualization of a Twitter user timeline.
 *  Each circle represents a Tweet. The x-axis shows the time a Tweet was posted and 
 *  the y-axis shows the Tweet type of the Tweet. The displayed time interval can be 
 *  zoomed by scrolling. If you hover over a Tweet circle, the Tweet text content 
 *  will appear in the upper left corner and the Tweet circle you're hovering will 
 *  become enlarged. Above the Tweet text in the upper left corner, there's a search 
 *  box that let's you search all Tweet text content. If the character sequence you're 
 *  searching for appears in a Tweet text, Tweet circle where the character sequence 
 *  appears is colored yellow.
 *  
 *  @param TTTDF A DataFrame from TTTDFfunctions function tweetsDF2TTTDF() containing
 *               the data to be displayed.
 *  @param width The display width in pixels.
 *  @param height The display height in pixels.
 */
def visualizeTimeline(TTTDF: DataFrame, width: Int = 900, height: Int = 400): Unit = {
  
  // The CSS code
  val visualizeTimelineCSS: String = 
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

  .tweet {
	fill: #1da1f2;
	stroke: black;
	opacity: 0.5;
  }

  circle.clicked {
      stroke-width: 5;
  }

  .searchedTweet {
      fill: yellow;
      opacity: 0.9;
  }

  .infoDisplay {
      margin: 1%;
  }

  #tweetTextDiv {
      position: absolute;
      font-family: Helvetica;
      text-align: left;
      width: 20%;
  }

  #searchDiv {
      height: 20px;
  }

  .highlight {
    background-color: yellow;
  }

  path, line {
    fill: none;
    stroke: black;
  }

  .axis {
      position:relative;
      z-index:1000000;
  }

  .axis text {
      fill: black;
      font-size: 1em;
  }

  #userTimelineVisualizationDiv {
      width: auto;
      height: auto;
      position: relative;
      align: center;
  }

  .title {
      fill: black;
      font-family: sans-serif;
      text-anchor: middle;
      text-align: center;
      font-size: 1.5em;
  }

  .searchField {
      font-family: sans-serif;
  }

  #tweet {
      position: absolute;
      left: 60%;
      height: auto;
  }
  """;
  
  // Convert the timeline TTT DataFrame to a JSON object
  val data = TTTDF.toJSON.collect

  // The JavaScript code
  val visualizeTimelineJS: String = 
  s"""
  /*******************************************************************************

  This user timeline visualisation let's you explore a Twitter timeline.

  Contained in the visualisation is:

   - All Tweets in the data
   - A search function to search Tweets
   - A click option to view the original Tweet on Twitter

  *******************************************************************************/

  // Specify display sizes.
  var width = ${width}, 
      height = ${height},
      margin = {
                top: 0.1 * height,
                right: 0.05 * width, 
                bottom: 0.2 * height,
                left: 0.05 * width
            };

  // Get div.
  var div = d3.select("#userTimelineVisualizationDiv");

  // Create svg.
  var svg = div.append('svg')
      //.attr("class", "visualization")
      .style("z-index", "-1")
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append("g")
          .attr("transform",
              "translate(" + margin.left + "," + margin.top + ")");

  // Declare global searched string.
  var searchedStr = "";

  // Create zoom object. Zooms x-axis.
  var zoom = d3.zoom()
      .on("zoom", zoomed);

  // Create zoomable area. Basically just an overlaid rectangle.
  var view = svg.append("rect")
      .attr("class", "zoom")
      .attr("width", width)
      .attr("height", height)
      // Allow for zoom while hovering x-axis
      .attr("transform",
          "translate(" + 0 + "," + margin.top + ")")
      // Remove currently displayed tweet on click
      .on("click", function() { clickView(); })
      // Link to zoom
      .call(zoom);

  // Set various tweet radius
  var idleTweetRadius = 15;
  var activeTweetRadius = idleTweetRadius * 1.618;
  var highlightedActiveTweetRadius = activeTweetRadius * 1.618;

  // Add title to the figure.
  svg.append("text")
      .attr("class", "title") // style in css
      .attr("x", width / 2)
      .attr("y", margin.top)
      .text("Twitter Timeline Visualization");

  // Create x-scale and set x-range.
  var xScale = d3.scaleTime()
      .range([0, width]);

  // Create xAxis.
  var xAxis = d3.axisBottom(xScale)
      .tickFormat(d3.timeFormat("%c")) // Set tick format date and time

  // Create x-axis g.
  var gXAxis = svg.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(" + 0 + "," + height + ")")

  // Create x-axis label.
  svg.append("text")
      .attr("class", "x label")
      .attr("text-anchor", "end")
      .attr("x", width)
      .attr("y", height - 6)
      .text("Time of Tweet");

  // y-range. Sets data placement along y-axis.
  // y-axis is divided in 6 lines, including top/bottom of chart,
  // and data is placed in the middle, lines 2 to 5.
  var yRange = [2, 3, 4, 5].map(function(x) { return x * height / 6; });

  // y-domain. Specifies which data should be placed where along y-axis.
  // Important: Matches with data from file.
  var yDomain = ["ReplyTweet",
                  "QuotedTweet",
                  "ReTweet",
                  "OriginalTweet"];

  // y-ticks to be displayed.
  var yTickValues = ["Reply",
                      "Quote",
                      "Retweet",
                      "Tweet"];

  // Create the y-scale and set y-range
  var yScale = d3.scaleOrdinal()
      .range(yRange)
      .domain(yDomain);

  // Create y-axis.
  var yAxis = d3.axisLeft(yScale)
      .tickValues(yTickValues); // Set y-axis tick values

  // Display y-axis (and label) after circles are placed to put y-axis above the circles

  // Read data. Note: file needs to be chronologically structured so that
  // data[0] is newest and data[length - 1] is oldest
  var data = ${data.mkString("[", ",\n", "]")};
        
  // Create and display the x-axis
  createAndDisplayXAxis(data);

  // Create circle for each tweet
  svg.selectAll("g")
      .data(data)
      .enter().append("g").attr("id", function(d) {
          return getGID(d.CurrentTwID);
      })
          .append("circle")
          // Set class to tweet ID
          .attr("id", function(d) {
              return getTweetID(d.CurrentTwID);
          })
          // Set position
          .attr("cy", function(d) {
              return yScale(d.TweetType.replace(/\\s/g, ''));
          })
          .attr("cx", function(d) { // x-position by tweet date
              return xScale(new Date(d.CurrentTweetDate));
          })
          // Set circle radius
          .attr("r", idleTweetRadius)
          // Set stroke
          .attr("stroke", "black")
          .attr("stroke-width", "0")
          // Set color by tweet type
          .attr("class", function(d) {
              // remove whitespace and return TweetType
              return "tweet " + d.TweetType.replace(/\\s/g, '');
            })
            // Add tooltip and enlarge tweet on mouse hover
            .on("mouseover", mouseoverTweet)
            // Restore tweet on mouseout
            .on("mouseout", mouseoutTweet)
            // Show actual tweet on click
            .on("click", clickTweet);

      // Display y-axis.
      var gYAxis = svg.append("g")
          .attr("class", "y axis") // Set class to y and axis
          .call(yAxis);

      // Create y-axis label.
      svg.append("text")
          .attr("class", "y label")
          .attr("text-anchor", "end")
          .attr("x", - 2 * height / 6)
          .attr("y", 6)
          .attr("dy", ".75em")
          .attr("transform", "rotate(-90)")
          .text("Type of Tweet");
          
      // display x-axis
      xAxis.ticks(5);
      gXAxis.call(xAxis);

      // Handle input search
      d3.select("#searchInput").on("input",
          function() {
              searchedStr = this.value.toLowerCase();
              searchTweets(data);
          });

  /**
   * Searches all tweets for a specific string.
   *
   * @param {string} searchStr - The string to search for
   */
  function searchTweets(data) {

      // Perform search if searched string is at least 3 chars long
      if (searchedStr.length > 2) {

          // Loop through all rows
          for (i = 0; i < data.length; i++) {

              // Get tweet text
              var tweetText = data[i].CurrentTweet;
              var tweet = d3.select("#" + getTweetID(data[i].CurrentTwID));

              // If tweet includes search string, display
              if (tweetText.toLowerCase().includes(searchedStr)) {
                  // Set class to searched tweet and enlarge
                  tweet
                      .classed("searchedTweet", true)
                      .attr("r", activeTweetRadius)
              // else, restore tweet to normal
              } else {
                  tweet
                      .classed("searchedTweet", false)
                      .attr("r", idleTweetRadius)
              }
          }

          // Highlight the searched string
          highlight();

      // else, restore tweets and dehighlight
      } else {
          // Restore tweets
          d3.selectAll(".tweet")
              .classed("searchedTweet", false)
              .attr("r", idleTweetRadius)

          // Dehighlight the displayed tweet
          dehighlight();
      }
  }

  /**
   * Create and display x-axis based on newest
   * and oldest dates in the dataset. Also sets the x-scale domain.
   *
   * @param data - Twitter dataset
   */
  function createAndDisplayXAxis(data) {

      // Get oldest date (that is, date of first tweet in the data)
      var oldestDate = new Date(data[data.length - 1].CurrentTweetDate);
      // Get newest date (that is, date of latest tweet in the data)
      var newestDate = new Date(data[0].CurrentTweetDate);
      // Add 2 weeks at beginning and end of axis for prettier display
      oldestDate.setDate(oldestDate.getDate() - 14); // go back 14 days
      newestDate.setDate(newestDate.getDate() + 14); // go forward 14 days

      // Set x-scale domain from newest and oldest date
      xScale.domain([oldestDate, newestDate]);
  }

  /**
   * Handle mouseover for Tweet.
   *
   * @param {list} d - Row from Twitter dataset
   */
  function mouseoverTweet(d) {

      // Get tweet
      var tweet = d3.select(this)

      // Get tweet text div
      var tweetTextDiv = d3.select("#tweetTextDiv");

      // Remove old tweet
      tweetTextDiv.selectAll("span")
          .remove();

      // Display tweet text
      tweetTextDiv.append("span")
          .text(d.CurrentTweet);

      // Enlarge tweet
      tweet.attr("r", activeTweetRadius);

      // If the tweet is searched, highlight and enlarge it
      if (tweet.classed("searchedTweet")) {

          // Enlarge the tweet to active and highlighted
          tweet.attr("r", highlightedActiveTweetRadius);

          // Highlight the tweet
          highlight();

      // else (that is, tweet is not searched), just enlarge the tweet
      } else {

          // Enlarge tweet to active
          tweet.attr("r", activeTweetRadius);
      }
  }

  /**
   * Highlights the searched part of the tweet text.
   */
  function highlight() {

      // Get tweet text div
      var tweetTextDiv = d3.select("#tweetTextDiv");

      // Get tweet text (works although text is inside a <span>)
      var tweetText = tweetTextDiv.text();
      // Get tweet text in lower case (used to highlight without case sensitivity)
      var tweetTextLowerCase = tweetText.toLowerCase();

      // Highlight if string to highlight is currently displayed
      if (tweetTextLowerCase.includes(searchedStr)) {

          // Get string before the string to highlight
          var strBefore = tweetText.substr(0, (tweetTextLowerCase.indexOf(searchedStr)));
          // Get string after the string to highlight
          var strAfter = tweetText.substr((tweetTextLowerCase.indexOf(searchedStr) +
                                      searchedStr.length),
                                      (tweetText.length - 1));

          // Remove non highlighted tweet text (the old tweet text with 1 <span>)
          tweetTextDiv.selectAll("span").remove();

          // Append string before highlight
          tweetTextDiv.append("span")
              .text(strBefore);
          // Append highlighted string
          tweetTextDiv.append("span")
              .attr("class", "highlight")
              .text(searchedStr);
          // Append string after highlight
          tweetTextDiv.append("span")
              .text(strAfter);
      }
  }

  /**
   * Dehighlights the tweet text.
   */
  function dehighlight() {

      // Get tweet text div
      var tweetTextDiv = d3.select("#tweetTextDiv");

      // Get tweet text
      var tweetText = tweetTextDiv.text();

      // Remove highlighted text (the old tweet text with 3 <span>s)
      tweetTextDiv.selectAll("span").remove();

      // Add non highlighted text
      tweetTextDiv.append("span").text(tweetText);

      // Add actual tweet

  }

  /**
   * Handle mouseout for Tweet.
   * Removes the tooltip displaying the tweet.
   *
   * @param {list} d - Row from Twitter dataset
   */
  function mouseoutTweet(d) {

      // Get tweet
      var tweet = d3.select(this)

      // Restore tweet to idle unless the tweet is searched
      if (!tweet.classed("searchedTweet")) {

          // Restore tweet
          tweet.attr("r", idleTweetRadius);

      // else (that is, tweet is searched), restore to active radius
      } else {
          // Restore tweet
          tweet.attr("r", activeTweetRadius);
      }
  }

  /**
   * Removes tooltip by ID.
   *
   * @param {string} id - The tooltip ID.
   */
  function removeTooltip(id) {
      d3.select("#" + id).remove();
  }

  /**
   * Creates a tooltip ID from a raw data tweet ID.
   *
   * @param {string} id - The tweet ID.
   */
  function getTooltipID(currentTwID) {
      return "tt" + currentTwID;
  }

  /**
   * Creates a tweet ID from a raw data tweet ID.
   *
   * @param {string} id - The tweet ID.
   */
  function getTweetID(currentTwID) {
      return "tw" + currentTwID;
  }

  function getGID(currentTwID) {
      return "g" + currentTwID;
  }

  /**
   * Handle zoom: Zoom the x-axis.
   */
  function zoomed() {

      // Create new x-scale based on zoom
      var new_xScale = d3.event.transform.rescaleX(xScale);

      // Display new x-scale. .ticks(3) to prettify
      gXAxis.call(xAxis.ticks(5).scale(new_xScale));

      // Reposition tweets based on zoom
      var tweets = d3.selectAll(".tweet");
      tweets.attr("cx", function(d) {
          return new_xScale(new Date(d.CurrentTweetDate));
      });
  };

  /**
   * Handle click on zoomable area. That is, handle click outside a tweet which
   * is considered a deselecting click. So, deselect previously clicked tweets
   * and remove displayed tweets.
   */
  function clickView() {

      // Get all clicked tweets
      var clicked = d3.selectAll(".clicked");

      // Remove clicked status on clicked tweets
      clicked.attr("stroke-width", "0");
      clicked.classed("clicked", false);

      // Remove tweet
      document.getElementById("tweet").innerHTML = ""
  }

  /**
   * Handle click on a tweet circle. Display the clicked tweet and let the tweet
   * appear selected by adding a stroke to it.
   */
  function clickTweet(d) {

      // Remove results from old click
      clickView();

      // Get tweet
      var tweet = d3.select(this)

      // Set tweet to clicked
      tweet.classed("clicked", true);

      // Get tweet div
      // Cannot do d3.select because twttr doesn't handle D3 selections
      var tweetDiv = document.getElementById("tweet");

      // Display tweet
      twttr.widgets.createTweet(d.CurrentTwID, tweetDiv)
  }
  """
  
  // The HTML code
  displayHTML(s"""
  <!DOCTYPE html>
  <html lang="en">

      <head>

          <meta charset="utf-8">
          <meta name="author" content="Olof Björck">
          <title>User Timeline</title>

          <style>
            ${visualizeTimelineCSS}
          </style>

      </head>

      <body>

          <!-- The input where you can search the Tweets -->
          <div id="searchDiv" class="infoDisplay">
              Search: <input name="searchStr" type="text" id="searchInput" class="searchField">
          </div>

          <!-- The place where Tweet texts are displayed -->
          <div id="tweetTextDiv" class="infoDisplay">
          </div>

          <!-- The D3 visualization -->
          <div id="userTimelineVisualizationDiv" class="visualization">
              <div id="tweet"></div>
          </div>

          <script sync src="https://platform.twitter.com/widgets.js"></script>
          <script src="https://d3js.org/d3.v4.min.js"></script>
          <script>
            ${visualizeTimelineJS}
          </script>

      </body>
  </html>

  """)
}

// Info
println("""
*** WARNING: ***

BUG: The first Tweet in the dataset doesn't display for some reason.

THERE IS NO SIZE CHECKING! Driver might crash as DataFrame.collect is used.
Also, the display can only handle so and so many items at once. Too large
dataset and the visualization might be very laggy or crash.

***  USAGE:  ***

visualizeTimeline(TTTDF)
 
****************
""")

// COMMAND ----------

