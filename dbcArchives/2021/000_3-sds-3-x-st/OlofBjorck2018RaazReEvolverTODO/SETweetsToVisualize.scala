// Databricks notebook source
// MAGIC %md
// MAGIC # Example SE Tweets to Visualize

// COMMAND ----------

// Display first twitter collection
//display(dbutils.fs.ls("dbfs:/datasets/MEP/SE/Streaming/2018/04/08/13/1523195100000"))
//display(dbutils.fs.ls("dbfs:/datasets/MEP/SE/RetrospectiveMany1/alreadyDownloadedIDs"))
display(dbutils.fs.ls("dbfs:/datasets/MEP/SE/RetrospectiveMany/*"))

// COMMAND ----------

// Get dataframe from the parquet file by reading parquet file, then using Raaz's function json -> DF, then Raaz's function DF -> TTT
//val dfStream = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(spark.read.parquet("dbfs:/datasets/MEP/SE/Streaming/2018/04/08/13/1523195100000")))

// COMMAND ----------

// This is the result
//display(dfStream)

// COMMAND ----------

val df = spark.read.parquet("dbfs:/datasets/MEP/SE/RetrospectiveMany/*") // read all the parquet files
         .union(spark.read.parquet("dbfs:///mytmpdir-forUserTimeLine/*").filter($"timelineUserId" === 939230172280213504L))
//val df = spark.read.parquet("dbfs:/datasets/MEP/SE/RetrospectiveMany1/*") // read all the parquet files

// Raaz - Whats the difference between RetrospectiveMany and RetrospectiveMany1?
// vik_lundberg is available in RetrospectiveMany1 but df2TTT-function fails later on. Help..


// COMMAND ----------

val VLdf = spark.read.parquet("dbfs:///mytmpdir-forUserTimeLine/*").filter($"timelineUserId" === 939230172280213504L)

// COMMAND ----------

df.cache() // without viktor 1380945
  .count

// COMMAND ----------

display(df)

// COMMAND ----------

df.count

// COMMAND ----------

df.select("TweetId").distinct().count

// COMMAND ----------

// MAGIC %md
// MAGIC # Olof's secondary data for visualisation - retweet network

// COMMAND ----------

// MAGIC %md
// MAGIC ### Monday Apr 9
// MAGIC 
// MAGIC Olof will begin working on visualizing the TTT. As of today, my idea is to visualize the TTT as a Bubble graph similar to Hans Roslings (I'm a big fan) Health and Wealth of nations. This allows for displaying 3 quantitative variables for each user in the TTT: number of retweets (x-axis), number of unique retweeters (y-axis), and number of followers (bubble radius), which I think would be very informative to help understand the TTT. Furthermore, changes in time can be incorporated by displaying the mentioned data measured through time (I'm not sure if this is needed though. My guess is that the TTT won't change that much. However, if the TTT does change, it's super useful and informative.). Check out this link which shows my idea: https://bost.ocks.org/mike/nations/
// MAGIC 
// MAGIC Can we label every user? Then it would be nice coloring the users by say "MP", "Right-wing", and so on.
// MAGIC 
// MAGIC // Olof

// COMMAND ----------

// MAGIC %md
// MAGIC #### Let's get number of retweets, number of unique retweeters, and number of followers:

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

// Get the TTT 
val TTTDF = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(df.select("statusJsonString"))).cache()
TTTDF.count // Check how much we got

// COMMAND ----------

TTTDF.printSchema()

// COMMAND ----------

display(TTTDF)

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC ### For Thursday:
// MAGIC 
// MAGIC  - Select everything that has to do with VL. That is, select everything where his ID is contained.
// MAGIC  - Get the original tweets.
// MAGIC  - Now, everything that's left is other users interactions with VL.
// MAGIC  - Connect other users interactions with what tweets they're interacting with. Order by date
// MAGIC  - Visualize

// COMMAND ----------

// MAGIC %md
// MAGIC First off, let's get VLs DF

// COMMAND ----------

val userOfInterestID = 939230172280213504L; // Vik lundberg

// COMMAND ----------

val TTTForUserOfInterestDF = TTTDF.filter($"CPostUserID" === userOfInterestID)
display(TTTForUserOfInterestDF)

// COMMAND ----------

//TTTForUserOfInterestDF.select("CPostUserId")

// COMMAND ----------

// MAGIC %md
// MAGIC 1. Let's begin by retrieving all IDs vik_lundberg has been interacting with

// COMMAND ----------

//val otherUsersIDs = TTTForUserOfInterestDF.select("UMentionASiD").distinct()
display(TTTForUserOfInterestDF.select("UMentionASiD").distinct())

// COMMAND ----------

// MAGIC %md
// MAGIC Now we need to clean the dataframe to get a list of the user IDs we can work with

// COMMAND ----------

// TTTForUserOfInterestDF.select("UMentionASiD").distinct().rdd.map(r => r(0)).collect() returns an array of WrappedArray
// Not sure how to work with WrappedArrays: To work around, make a string of the Array of WrappedArrays and remove all but
// the digits and commas (that is, remove all WrappedArray()-characters). Then create a new Array from that string with some regex. Then split on , and remove empty strings, then cast to Long
//val other_user_ids = Array(VL_other_users.rdd.map(r => r(0)).collect().mkString.replaceAll("[^0-9,. ]", ""))
val otherUsersIDs = TTTForUserOfInterestDF.select("UMentionASiD").distinct().rdd.map(r => r(0)).collect().mkString.replaceAll("[^0-9,)]", "").replaceAll("[)]", ",").split(",").filter(_.nonEmpty).map(_.toLong)

// COMMAND ----------

// MAGIC %md
// MAGIC 2. Let's retrieve all tweets from the other users

// COMMAND ----------

// Get TTT for all other users. Loop through array of other users ids, collect their tweets and put in an array. This array of dataframes reduces upon a union, resulting in a DF with all other users tweets.
val TTTForOtherUsersDFArray = new Array[DataFrame](otherUsersIDs.length)// = Array(otherUsersIDs.length);
for (i <- 0 to (otherUsersIDs.length - 1)) {
  TTTForOtherUsersDFArray(i) = TTTDF.filter($"CPostUserID" === otherUsersIDs(i))
}
//TTTForUserOfInterestDF.count
display(TTTForOtherUsersDFArray.reduce(_.union(_)))

// COMMAND ----------

// END OF THURSDAY WORK

// COMMAND ----------

import scala.collection.mutable.WrappedArray
val other_user_ids = Array();
for (wrapped_arr <- VL_other_users.rdd.map(r => r(0)).collect()) {
  println(wrapped_arr.mkString)
  /*for (id <- arr) {
    println(id)
    if (id && !other_user_ids.contains(id)) {
      other_user_ids :+ id;
    }
  }*/
}

// COMMAND ----------

// Select everything that has to do with VL
val VL_full_df = TTTDF.filter(/*$"OriginalTwIDinRT" === 939230172280213504L ||
                              $"OriginalTwIDinQT" === 939230172280213504L ||
                              $"OriginalTwIDinReply" === 939230172280213504L ||*/
                              $"CPostUserID" === 939230172280213504L ||
                              /*$"OPostUserIdinRT" === 939230172280213504L ||
                              $"OPostUserIdinQT".contains(939230172280213504L) ||
                              //$"OPostUserIdinReply".contains(939230172280213504L) ||
                              //$"OPostUserIdinReply".contains(939230172280213504L) ||
                              $"OPostUserIdinReply".contains(939230172280213504L) ||*/
                              $"CurrentTweet".contains("@vik_lundberg") ||
                              $"UMentionASsN".toString().contains("vik_lundberg") /*
                              $"UMentionQTiD".toString.contains("939230172280213504") || 
                              $"UMentionASiD".toString.contains("939230172280213504")*/
                              )
VL_full_df.count() // CPostUserID -> 200

// COMMAND ----------

display(VL_full_df.select("UMentionASiD"))

// COMMAND ----------

val VL_TTTDF =  TTTDF.filter($"CPostUserID" === 939230172280213504L).cache()
VL_TTTDF.count()

// COMMAND ----------

display(VL_TTTDF)

// COMMAND ----------

// Get names and screen name and number of followers for every user

// Need this to be only unique users, done in next cell
val usersInfoDF = TTTDF.select("CPostUserId","CPostUserName", "CPostUserSN", "followersCount").toDF(Seq("UserID", "UserName", "ScreenName", "followersCount"): _*)
display(usersInfoDF)

// COMMAND ----------

usersInfoDF.count

// COMMAND ----------

// Inner join to get rid of duplicates (bad variable naming here...)
val userDF = usersInfoDF.dropDuplicates(Seq("UserID"))
userDF.count // Only 745 unique users?

// COMMAND ----------

// Ease up memory? Not sure what this does
TTTDF.unpersist

// COMMAND ----------

// Get all retweets
val RTNetworkDF = TTTDF.filter($"TweetType"==="ReTweet").select("OPostUserIdinRT","CPostUserId","Weight")
display(RTNetworkDF)

// COMMAND ----------

// Get count for every users's retweets of every other user
val weightedRTNetworkDF = RTNetworkDF.groupBy("OPostUserIdinRT","CPostUserId").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc)
display(weightedRTNetworkDF)

// COMMAND ----------



// COMMAND ----------

// Get count of retweets: how many times every user have been retweeted
val outDegreeOfOPostUserIdinRTNetwork = weightedRTNetworkDF.select("OPostUserIdinRT","Weight").groupBy("OPostUserIdinRT").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc)
display(outDegreeOfOPostUserIdinRTNetwork)

// COMMAND ----------

// Rename columns
outDegreeOfOPostUserIdinRTNetwork.toDF(Seq("OPostUserIdinRT", "NrOfRetweets"): _*)

// COMMAND ----------

// Get number of unique retweeters
val outNgbhdOfOPostUserIdinRTNetwork = weightedRTNetworkDF.drop("Weight").withColumn("Weight",lit(1L)).groupBy("OPostUserIdinRT").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc)
display(outNgbhdOfOPostUserIdinRTNetwork)

// COMMAND ----------

// Rename columns
outNgbhdOfOPostUserIdinRTNetwork.toDF(Seq("OPostUserIdinRT", "NrOfRetweeters"): _*)

// COMMAND ----------

outDegreeOfOPostUserIdinRTNetwork.count

// COMMAND ----------

outNgbhdOfOPostUserIdinRTNetwork.count

// COMMAND ----------

// Perform inner join with renamed TTTDFs to get TTTDF with retweets and retweeters count
val tweetsAndRetweetsCountDF = outDegreeOfOPostUserIdinRTNetwork.toDF(Seq("UserID", "NrOfRetweets"): _*).join(outNgbhdOfOPostUserIdinRTNetwork.toDF(Seq("UserID", "NrOfRetweeters"): _*), Seq("UserID"))

// COMMAND ----------

display(tweetsAndRetweetsCountDF)

// COMMAND ----------

tweetsAndRetweetsCountDF.count

// COMMAND ----------

// Now we need the screen names

// COMMAND ----------

// TODO: Get usernames. Good for the visualization. 
twitter.showUser(245467017L)

// COMMAND ----------

// Join counts with user info
val visualizeDF = usersInfoDF.join(tweetsAndRetweetsCountDF, Seq("UserID")).distinct()
display(visualizeDF)

// COMMAND ----------

visualizeDF.count

// COMMAND ----------

// MAGIC %md
// MAGIC ## Tuesday Apr 10
// MAGIC 
// MAGIC Today I built a visualizer of the visualizeDF. I'll be working on making it work here in Databricks. 
// MAGIC 
// MAGIC __Something's wrong I think: There are just 748 users - should be more than that, shouldn't it?__
// MAGIC 
// MAGIC _End of the day note:_
// MAGIC It's not going too well. I don't know how to get the Df-data in the javascript file inside the html-document.

// COMMAND ----------

val csv_df = visualizeDF.write.format("csv")
csv_df

// COMMAND ----------

displayHTML(s"""

<!DOCTYPE html>
<html lang="en">
  <head>
  
  <style>
  
  path, line {
  fill: none;
  stroke: black;
}

.axis text {
	fill: black;
	font-size: 1em;
}
  
  </style>
    
  </head>
  <body>
  
  <div id="treeVisualizationDiv" class="visualization" align="center"></div>
  
  <script src="https://d3js.org/d3.v4.min.js"></script>
  
  <script>
  
  /*******************************************************************************

  This tree visualization visualizes a given twitter network.

  Contained in the visualization is, for each user:

   - Number of retweets
   - Number of individual retweeters
   - Number of followers

  *******************************************************************************/

  /*
  Create accessors that specify data
  */

  function x(d) { return d.NrOfRetweets; }
  function y(d) { return d.NrOfRetweeters; }
  function radius(d) { return d.followersCount; }
  function color(d) { return d.followersCount; } // What to do here?
  function name(d) { return d.UserName; }
  function key(d) { return d.UserID; }

  /*
  Specify chart dimensions
  */

  // Specify display sizes
  var margin = {top: 50, right: 50, bottom: 50, left: 50},
      width = 600 - margin.left - margin.right,
      height = 600 - margin.top - margin.bottom;

  // Get div
  var div = d3.select("#treeVisualizationDiv");

  // Create svg
  var svg = div.append('svg')
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append("g")
          .attr("transform",
              "translate(" + margin.left + "," + margin.top + ")");

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
  svg.append("text")
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
  svg.append("text")
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
      .range([0, 20])
      .domain([1, 100000])

  /*
  Create scale for color
  */
  var colorScale = d3.scaleLinear()
      .range(["red", "red"])
      .domain([1, 10000])

  var gColorBar = svg.append("g")
      .call(colorScale)

  // Add data. Each row represented as a "g" of class "node" inside the svg.
  var data = d3.csv(${visualizationDF}, function(error, data) {

      // TODO: Fix this. Should display based on data max/min
      xScale.domain([1, 10000])
      gXAxis.call(xAxis)
      yScale.domain([0, 350])
      gYAxis.call(yAxis)

      // Display the data
      svg.selectAll("g")
          .data(data).enter() // Enter the data

          // Create nodes
          .append("g")
              .attr("class", "node")

          // Create circles to display the data
          .append("circle")
              .call(setCircleAttributes)
              .sort(orderLargestBelow); // sort dataframe on radius size first instead?

  });

  function setCircleAttributes(circle) {
      circle
          .attr("opacity", 0.05)
          .attr("fill", function(d) { return colorScale(color(d)); })
          .attr("cx", function(d) { return xScale(x(d)); })
          .attr("cy", function(d) { return yScale(y(d)); })
          .attr("r", function(d) { return radiusScale(radius(d)); });
  }

  function orderLargestBelow(a, b) {
      return radius(b) - radius(a);
  }

  </script>
  
</body>
</html>

""")

// COMMAND ----------

// MAGIC %md
// MAGIC ## Below is Raaz's code from Sunday Apr 8

// COMMAND ----------

// MAGIC 
// MAGIC %md
// MAGIC ## Lets Use TTTDFs

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

val df2 = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(df.select("statusJsonString"))).cache()
df2.count

// COMMAND ----------

df.unpersist

// COMMAND ----------

df2.filter($"TweetType"==="ReTweet").count()

// COMMAND ----------

// 
val RTNetworkDF = df2.filter($"TweetType"==="ReTweet").select("OPostUserIdinRT","CPostUserId","Weight")
display(RTNetworkDF)

// COMMAND ----------

val weightedRTNetworkDF = RTNetworkDF.groupBy("OPostUserIdinRT","CPostUserId").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc)

// COMMAND ----------

display(weightedRTNetworkDF)

// COMMAND ----------

twitter.showUser(104778698L) // 

// COMMAND ----------

val outDegreeOfOPostUserIdinRTNetwork = weightedRTNetworkDF.select("OPostUserIdinRT","Weight").groupBy("OPostUserIdinRT").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc)
display(outDegreeOfOPostUserIdinRTNetwork)

// COMMAND ----------

val influentialOPostUserIDinRT_RTGT999 = outDegreeOfOPostUserIdinRTNetwork.filter($"Weight">999).select("OPostUserIDinRT").distinct() 

influentialOPostUserIDinRT_RTGT999.show

// COMMAND ----------

twitter.showUser(104778698L)

// COMMAND ----------

twitter.showUser(297838327L)

// COMMAND ----------

RTNetworkDF.show(5)

// COMMAND ----------


weightedRTNetworkDF.drop("Weight").withColumn("Weight",lit(1L)).show(5)

// COMMAND ----------

val outNgbhdOfOPostUserIdinRTNetwork = weightedRTNetworkDF.drop("Weight").withColumn("Weight",lit(1L)).groupBy("OPostUserIdinRT").agg(sum("Weight").as("Weight")).orderBy($"Weight".desc)
display(outNgbhdOfOPostUserIdinRTNetwork)

// COMMAND ----------

val influentialOPostUserIDinRT_RTOutNgbhdGT149 = outNgbhdOfOPostUserIdinRTNetwork.filter($"Weight">149).drop("Weight") //.count

// COMMAND ----------

influentialOPostUserIDinRT_RTOutNgbhdGT149.show() // count 100

// COMMAND ----------

influentialOPostUserIDinRT_RTGT999.show // 63

// COMMAND ----------

influentialOPostUserIDinRT_RTGT999.union(influentialOPostUserIDinRT_RTOutNgbhdGT149).distinct().count() // 117

// COMMAND ----------

val influentialUsers = influentialOPostUserIDinRT_RTGT999.union(influentialOPostUserIDinRT_RTOutNgbhdGT149).distinct()

// COMMAND ----------

influentialUsers.count

// COMMAND ----------

influentialUsers.write.parquet("dbfs:///FileStore/tables/twitterAccountsOfInterest_RTOPostUserIdinRT_InfluencersDF")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:///datasets/MEP/"))

// COMMAND ----------

display(influentialOPostUserIDinRT_RTGT999)

// COMMAND ----------

twitter.showUser(19778134L)

// COMMAND ----------

df2.distinct().count()

// COMMAND ----------

df2.select("TweetId").distinct().count()

// COMMAND ----------

// MAGIC %md
// MAGIC ### Just one user's timeline

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

twitter.showUser("@vik_lundberg").getId() // quick test that REST API works - should get 568088930 for @PaulinaForslund

// COMMAND ----------

twitter.showUser()

// COMMAND ----------

val oneuserTimeLine = df.filter($"timeLineUserId"===939230172280213504L).cache // Get vik_lundberg
oneuserTimeLine.count()

// COMMAND ----------

df.unpersist() // to save distributed memory - remmeber to unpersist DFs you cached before but are useless later

// COMMAND ----------

display(oneuserTimeLine)

// COMMAND ----------

// MAGIC %md
// MAGIC # Lets Use TTTDFs

// COMMAND ----------

// MAGIC %run scalable-data-science/meme-evolution/db/src2run/TTTDFfunctions

// COMMAND ----------

// MAGIC %md
// MAGIC ## Olof's primary data for visualisation - single user timeline

// COMMAND ----------

val oneUserTimeLineTTTDF = tweetsDF2TTTDFWithURLsAndHastags(tweetsJsonStringDF2TweetsDF(oneuserTimeLine.select("statusJsonString"))).cache()
oneUserTimeLineTTTDF.count

// COMMAND ----------

tweetsJsonStringDF2TweetsDF(oneuserTimeLine.select("statusJsonString")).show

// COMMAND ----------

display(oneUserTimeLineTTTDF)

// COMMAND ----------

oneUserTimeLineTTTDF.printSchema

// COMMAND ----------

oneUserTimeLineTTTDF.toJSON.coalesce(1).rdd.saveAsTextFile("/FileStore/timelinesOfInterest/oneUserTimeLineTTTDF_PaulinaForslund.json")

// COMMAND ----------

display(dbutils.fs.ls("/FileStore/timelinesOfInterest/oneUserTimeLineTTTDF_PaulinaForslund.json/part-00000/"))

// COMMAND ----------

dbutils.fs.head("dbfs:/FileStore/timelinesOfInterest/oneUserTimeLineTTTDF_PaulinaForslund.json/part-00000")

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Go to browser with the connection to the databricks instance open and type the following in URL window
// MAGIC 
// MAGIC - https://dbc-635ca498-e5f1.cloud.databricks.com/files/timelinesOfInterest/oneUserTimeLineTTTDF_PaulinaForslund.json/part-00000
// MAGIC 
// MAGIC for example, and then download.

// COMMAND ----------

