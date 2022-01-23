<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Twitter Streaming Using Geolocation and Emoji Based Sentiment Analysis
======================================================================

### Georg Bökman & Rasmus Kjær Høier

</div>

<div class="cell code" execution_count="1" scrolled="auto">

<div class="output execute_result html_result" execution_count="1">

<iframe width="560" height="315" src="https://www.youtube.com/embed/HMNcVTqmEMM" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

</div>

</div>

<div class="cell markdown">

In this project we have used Spark Streaming and the twitter4j library to perform filtered streaming of tweets. As we were interested in combining location and sentiment information, we filtered for location tagged tweets. This was necessary as only around 1% of tweets coming straight from the twitter hose has information on the country of origin.

In particular we hoped to explore the following ideas/questions: \* Sentiment analysis of text can be difficult across different languages. However, the same emojis are used on twitter all over the world (although some emojis are more popular in some regions). Could this be used to compare sentiment across borders? \* From the filtered stream we get tweets containing information on country of origin and timestamps. What insight can we get by visualizing tweets as a function of time and space?

We saw this project as an opportunity to learn more about twitter and streaming in general as none of us had any prior experience with this.

</div>

<div class="cell markdown">

Contents
--------

Our project consists of 8 notebooks. We recommend you read through the first four, and if you are curious about some of the functions we use or how the data was collected, then have a look in the appendix notebooks as well. The appendices are not quite as tidy as the first four notebooks.

-   01 Introduction
-   02 Clustering emoticons based on tweets
-   03 Dynamic Tweet Maps
-   04 Conclusion
-   05 Appendix get cc data
-   06 Appendix Tweet carto functions
-   07a Appendix ExtendedTwitterUtils2run
-   07b Appendix TTTDFfunctions

</div>

<div class="cell markdown">

Notes on data collection
------------------------

Tweets were collected using functions from the course notebooks `07_a_appendix_extendedTwitterUtils` and `07_b_appendix_TTTDFfunctions` (originally numbered 025). Some minor changes were made in order to perform filtered streaming only of countries with a known country of origin.

In notebook `05_appendix_get-cc-data` we run the function `streamFuncWithProcessing()`. This function creates a new twitter stream by calling the createStream methods from the `ExtendedTwitterUtils` object in notebook 07\_a. One of the arguments to this method is a filterquery, which has been set to require that the tweet must have registered coordinates. Longitudes range from -180 to 180 degrees and latitudes range from -90 to 90 degrees, covering the entire globe.

    // Create filter

    val locationsQuery = new FilterQuery().locations(Array(-180.0, -90.0), Array(180.0, 90.0)) // all locations

    // Create a Twitter Stream for the input source.

    val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth, Some(locationsQuery))

We used the databricks jobs feature to automatically run the data acquisition for 3 minutes every hour from December 22nd 2020 until January 2nd 2021. We also acquired data continuously on the 22nd. In total this yielded around 2 million tweets.

</div>
