// Databricks notebook source
// MAGIC %md
// MAGIC # Explore a User Time-line
// MAGIC 
// MAGIC by Raazesh Sainudiin and Olof Björck
// MAGIC 
// MAGIC This is part of *Project MEP: Meme Evolution Programme* and supported by databricks academic partners program.
// MAGIC 
// MAGIC The analysis is available in the following databricks notebook:
// MAGIC * [http://lamastex.org/lmse/mep/src/extendedTwitterUtils.html ???](http://lamastex.org/lmse/mep/src/extendedTwitterUtil.html-???)
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC Copyright 2018 Raazesh Sainudiin and Olof Björck
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

// SOME IMPORTTS
import scala.collection.mutable.ArrayBuffer
import twitter4j._
import twitter4j.conf._
import scala.collection.JavaConverters._ 

import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.{StructType, StructField, StringType};
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import com.google.gson.Gson
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import com.google.gson.Gson
import org.apache.spark.sql.DataFrame


// COMMAND ----------

// Put your credentials ...
// Olof's credentials:
val consumerKey       = "fGeNVf4GzVnuBgqq26dwhPs8E"
val consumerSecret    = "oP9yHASkB9XcRJAJLHJjmcKSV7F8jTrRH6KGR7LmWKC9qSRn1o"
val token             = "976392982042759169-kp2fYHMCF1flyKQo8yuhSHKISwv4jgf"
val tokenSecret       = "IPPvpoLqjw5pE5k11aycZnJHDFUIwoPXIitj0CV8laR7w"

val cb = new ConfigurationBuilder()       

val twitter = {
  val c = new ConfigurationBuilder
    c.setDebugEnabled(false)
    .setOAuthConsumerKey(consumerKey)
    .setOAuthConsumerSecret(consumerSecret)
    .setOAuthAccessToken(token)
    .setOAuthAccessTokenSecret(tokenSecret);

  new TwitterFactory(c.build()).getInstance()
}

// COMMAND ----------

twitter.showUser("@raazozone").getId() // quick test that REST API works - should get 4173723312

// COMMAND ----------

def lookupUserSNs(Retweeterids:Seq[String])={
  val grouped=Retweeterids.grouped(100).toList 
  for {group<-grouped  
       users=twitter.lookupUsers(group:_*)
       user<-users.asScala 
   } yield user     
}// we loose some suspended accounts...

// COMMAND ----------

val userScreenNamesOfInterest = Seq("realDonaldTrump","raazozone")

// COMMAND ----------

val usersOfInterest = lookupUserSNs(userScreenNamesOfInterest)

// COMMAND ----------

val usersIDsOfInterest = usersOfInterest.map(u => u.getId()).toSet.toSeq.filter(_ != null) // just get the IDs of the seed users who are valid

// COMMAND ----------

//dbutils.fs.rm("dbfs:///mytmpdir-forUserTimeLine",recurse=true) //- DANGEROUS

// COMMAND ----------

val dirName = "dbfs:///mytmpdir-forUserTimeLine/"

// COMMAND ----------

dbutils.fs.mkdirs(dirName)

// COMMAND ----------

display(dbutils.fs.ls(dirName))

// COMMAND ----------

case class UserTimeLineStatus(userID: Long, status: twitter4j.Status)

def WriteToParquet(tweetsAllUsers: scala.collection.mutable.Buffer[UserTimeLineStatus], dirName: String, rep: String ) = {
              if( tweetsAllUsers.size > 0) {
                val tweetsOfUserId = sc.parallelize(tweetsAllUsers)
                val userIdAndStatusJsonString = tweetsOfUserId.map(x => { val gson = new Gson();
                                                               val toJson=  gson.toJson(x.status);
                                                               (x.userID, x.status.getId() , toJson)
                                                             }
                                                        ).toDF("timeLineUserId", "TweetId", "statusJsonString")     
                userIdAndStatusJsonString.write.mode(SaveMode.Append).parquet(dirName + rep.toString() + "/" ) 
              }
}

// COMMAND ----------

// an unresolved nullpointerexception bug - but all works agin from the point it breaks by updating whatNeedsToBeDownloaded in above three cells and then this one - 
// RAAZ TODO 
// - fix null-pointer exception from Andrew's page bookmarked physically
// make sure you grab everything
//----------------------
val tweetsAllUsers = scala.collection.mutable.Buffer[UserTimeLineStatus]()
tweetsAllUsers.clear
var rep = 0
usersIDsOfInterest.foreach(t => {
   val maxNumOfTweets = 200
   var lastPage = 1
   val firstPage = 1
   val userTwitterID = t
   val twitterUser = twitter.showUser(userTwitterID)
   val numberOfTweets = twitterUser.getStatusesCount()
   
   if( numberOfTweets >= 3200) { lastPage = 16 } 
    else if (numberOfTweets % maxNumOfTweets > 0 ){lastPage = 1 + (numberOfTweets / maxNumOfTweets)}
    else {lastPage = (numberOfTweets / maxNumOfTweets)}
  // getUserTimeLine(userTwitterID,firstPage,lastPage,maxNumOfTweets)
  var currentPage = firstPage
  if (twitterUser != null && !twitterUser.isProtected() && twitterUser.getStatusesCount() > 10 && twitterUser.getId() != null) {
      while ( currentPage != lastPage) { 
  
                val page = new Paging(currentPage, numberOfTweets);
        /////////////////////////
        try {
            val status = twitter.getUserTimeline(userTwitterID, page)
            if (status != null) {
              val xx =  status.asScala.map(x => UserTimeLineStatus(userTwitterID, x))
              tweetsAllUsers ++= xx
            }

            if (status.getRateLimitStatus().getRemaining() < 2) {
              val waitTime = status.getRateLimitStatus().getSecondsUntilReset + 10
              println("Waiting " + waitTime + " seconds ( "
                + waitTime/60.0 + " minutes) for rate limit reset.")
              Thread.sleep(waitTime*1000)
            }
          }
          catch {
            case te: TwitterException =>
            println("Failed to search tweets") //+ te.getMessage)
          }
         if(tweetsAllUsers.size > 10000){
           rep=rep+1
           WriteToParquet(tweetsAllUsers, dirName, rep.toString() )
           tweetsAllUsers.clear
          }
        currentPage = currentPage + 1
      }
    // write what you finished cleanly
    val justDownloaded = sc.parallelize(Seq(userTwitterID)).toDF("userId")
    justDownloaded.write.mode(SaveMode.Append).parquet(dirName + "alreadyDownloadedIDs" + "/" )
  }
}
)
WriteToParquet(tweetsAllUsers, dirName, rep.toString() )

// COMMAND ----------

spark.read.parquet(s"$dirName/alreadyDownloadedIDs").show()

// COMMAND ----------

dirName

// COMMAND ----------

val raazozoneDF = spark.read.parquet(s"$dirName/*").filter($"timelineUserId" === 4173723312L)

// COMMAND ----------

val df = spark.read.parquet(s"$dirName/*")

// COMMAND ----------

raazozoneDF.cache()
  .count()

// COMMAND ----------

val realDonaldTrumpDF = df.filter($"timelineUserId" === 25073877L).cache()
realDonaldTrumpDF.count

// COMMAND ----------

realDonaldTrumpDF.show

// COMMAND ----------

// MAGIC %run scalable-data-science/sds-2-2/025_b_TTTDFfunctions

// COMMAND ----------

////%run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

val a = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(realDonaldTrumpDF.select("statusJsonString")))

// COMMAND ----------

display(a)

// COMMAND ----------

// Get the TTT 
val TTTDF_realDonaldTrump = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(realDonaldTrumpDF.select("statusJsonString"))).cache()
TTTDF_realDonaldTrump.count // Check how much we got

// COMMAND ----------

display(TTTDF_realDonaldTrump)

// COMMAND ----------

TTTDF_realDonaldTrump.show(1)

// COMMAND ----------

val TTTDF_raazozone = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(raazozoneDF.select("statusJsonString"))).cache()
TTTDF_raazozone.count // Check how much we got

// COMMAND ----------

display(TTTDF_raazozone)

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd

// COMMAND ----------

val data = TTTDF_realDonaldTrump.toJSON.collect

// COMMAND ----------

// Data to draw on HTML page.
// As an alternative, you could try saving it as JSON/CSV and read the file in the Javascript code.
// Either way is fine, though saving into a file, might be better in terms of loading.
//TTTDF_raazozone.coalesce(1).write.csv("/databricks/driver/oneUserTimeLine.csv")

// COMMAND ----------

displayHTML(s"""
<!DOCTYPE html>
<meta charset="utf-8">
<meta name="author" content="Olof Björck">
<title>User Timeline</title>

<link rel="stylesheet" type="text/css" href="../css/userTimeline.css">

    <style>
        body {
            font-family: Sans-Serif;
            width: 100%;
            height: 100%;
            margin: 0;
            overflow: scroll;
        }

        .ReTweet {
            fill: lightblue;
            opacity: 0.5;
        }

        .ReplyTweet {
            fill: lightblue;
            opacity: 0.5;
        }

        .OriginalTweet {
            fill: lightblue;
            opacity: 0.8;
        }

        .QuotedTweet {
            fill: lightblue;
            opacity: 0.5;
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

        .highlight {
            background-color: yellow;
        }

        path,
        line {
            fill: none;
            stroke: black;
        }

        .axis {
            position: relative;
            z-index: 1000000;
        }

        .axis text {
            fill: black;
            font-size: 1em;
        }

        .visualization {
            width: auto;
            height: auto;
            position: relative;
            align: center;
        }

        #userTimelineVisualizationDiv {
            width: auto;
            height: auto;
            margin-right: 500px;
            position: relative;
            align: center;
        }

        .visualization {
            font-family: sans-serif;
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

        .searchField {
            font-family: sans-serif;
        }

        #tweet {
            position: absolute;
            left: 100%;
            height: 100%;
        }
    </style>
<body>

    <div id="searchDiv" class="infoDisplay">
        Search: <input name="searchStr" type="text" id="searchInput" class="searchField">
    </div>

    <div id="tweetTextDiv" class="infoDisplay">
    </div>

    <div id="userTimelineVisualizationDiv" class="visualization">
        <div id="tweet"></div>
    </div>

    <script sync src="https://platform.twitter.com/widgets.js"></script>
    <script src="https://d3js.org/d3.v4.min.js"></script>
    <script type="text/javascript">
        /*******************************************************************************

                This user timeline visualisation let's you explore a Twitter users activity.

                Contained in the visualisation is:

                 - All Tweets in the data
                 - A search function to search Tweets
                 - A click option to view the original Tweet on Twitter

                *******************************************************************************/

        // Specify file name.
        var filename = "'data' variable";
        var filepath = "../data/" + filename;

        // TODO: Create accessors that acces data in the csv-file instead of
        // accessing csv-column names in the middle of the code

        var totalWidth = 1080, totalHeight = 700;

        // Don't modify these width and height directly.
        // Account for bunch of other elements on the webpage
        var width = totalWidth - 100, height = totalHeight - 300;

        // Specify display sizes.
        var margin = {
                top: 0.1 * height,
                right: 0.05 * width, // should it be width instead?
                bottom: 0.2 * height,
                left: 0.05 * width // should it be width instead?
            };
            /* In Databricks notebook it is better to set width and height manually
               otherwise, the cell will have small height.

            width = window.innerWidth - margin.left - margin.right,
            height = window.innerHeight - margin.top - margin.bottom -
            0.05 * window.innerHeight; // Compensate for searchDiv 
            */

        // Get div.
        var div = d3.select("#userTimelineVisualizationDiv");

        // Create svg.
        var svg = div.append('svg')
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
            .on("click", function() {
                clickView();
            })
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
            .text("Historic Twitter data from " + filename);

        // Create x-scale and set x-range.
        var xScale = d3.scaleTime()
            .range([0, width]);

        // Create xAxis.
        var xAxis = d3.axisBottom(xScale)
            .tickFormat(d3.timeFormat("%c")) // Set tick format date and time
            .ticks(4); // set ticks

        // Display x-axis.
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
        var yRange = [2, 3, 4, 5].map(function(x) {
            return x * height / 6;
        });

        // y-domain. Specifies which data should be placed where along y-axis.
        // Important: Matches with data from file.
        var yDomain = ["ReplyTweet",
            "QuotedTweet",
            "ReTweet",
            "OriginalTweet"
        ];

        // y-ticks to be displayed.
        var yTickValues = ["Reply",
            "Quote",
            "Retweet",
            "Tweet"
        ];

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
            .attr("stroke", "purple")
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
            .attr("x", -2 * height / 6)
            .attr("y", 6)
            .attr("dy", ".75em")
            .attr("transform", "rotate(-90)")
            .text("Type of Tweet");

        // Handle input search
        d3.select("#searchInput").on("input",
            function() {
                searchedStr = this.value.toLowerCase();
                searchTweets(data);
            }
        );

        /**
         * Searches all tweets for a specific string.
         *
         * @param {string} searchStr - The string to search for
         */
        function searchTweets(data) {

            // Perform search if searched string is at least 3 chars long
            if (searchedStr.length > 2) {

                // Loop through all rows
                for (i = 0; i < data.length - 1; i++) {

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
            // Link graphic to axis
            gXAxis.call(xAxis);

            // return the axis g element
            return gXAxis;
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
            gXAxis.call(xAxis.ticks(4).scale(new_xScale));

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
            var tweet = d3.select(this).attr("stroke-width", "10");

            // Set tweet to clicked
            tweet.classed("clicked", true);

            // Get tweet div
            // Cannot do d3.select because twttr doesn't handle D3 selections
            var tweetDiv = document.getElementById("tweet");

            // Display tweet
            twttr.widgets.createTweet(d.CurrentTwID, tweetDiv)
            //twttr.widgets.createTweet(d.OriginalT)
        }
    </script>

</body>
""")

// COMMAND ----------

