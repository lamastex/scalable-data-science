# Project MEP: Meme Evolution Programme / research

This is the public research/learning/teaching repository for Project MEP: Meme Evolution Programme.

This programme is partly supported by:

* databricks academic partners program (for distributed cloud computing) 
	* through https://academics.cloud.databricks.com
* Research Chair in Mathematical Models of Biodiversity (for mathematical theorizing) held jointly by:
	* [Veolia Environnement](http://en.wikipedia.org/wiki/Veolia_Environnement), 
	* [French National Museum of Natural History](http://www.mnhn.fr/fr), Paris, France and 
	* [Centre for Mathematics and its Applications, Ecole Polytechnique](http://www.cmap.polytechnique.fr/), Palaiseau, France.

Raazesh Sainudiin
[Laboratory for Mathematical Statistical Experiments](http://lamastex.org)


## Tweet Transmission Tree

This is the job to get the 'interconnected set of tweets' from the streaming jobs already done. This can be formalized as **ttt := tweet transmission tree** depending on what is extractable from it.

See blackboard discussion notes at LMSE on 2016-10-10. 
We will eventually lua/la/ka-tex mathematically here..

### Tweet Transmission Tree (ttt)

theory being "tango-ed" with data still... 2016-10-10

Question:

* Can the ttt be extracted such that its leaves are the nodes of the follows network (see/add diagram on black-board)?

#### job 1

* Start with data in (closed dir here)[dbfs:/datasets/MEP/AkinTweet/sampleTweets@raazozone/] in (closed notebook)[https://academics.cloud.databricks.com/#notebook/139335]

* goal is to produce a "tweet-tweet table" with say 6 columns as a precursor to ttt

```
tweetID1, tweetID2, interactionType, userID1, userID2, timestamp2to1
where
tweetID1 is a past event at some time earlier than timestamp2to1 authored by userID1
tweetID2 by author userID2 is reacting to tweetID1 at time timestamp2to1.
```

Please use unixtime-stamps like in:
```
unix_timestamp($"createdAt", """MMM dd, yyyy hh:mm:ss a""").cast(TimestampType).as("timestamp")
```
interactionTypes could be
* reTweet
* reply
* retweetWithComment (please read the Object:? carefully first )
* etc.
* READ free2Read/miningSocialWeb\_Ch1.pdf

And use the terminology in the docs of the API, specifically:
* https://dev.twitter.com/overview/api/tweets
* https://dev.twitter.com/overview/api/entities
* https://dev.twitter.com/overview/api/entities-in-twitter-objects

When we are happy with the results on the ampleTweets@aazozone data, in terms of being able to get ttts from tweet-tweet table with 6 columns, 
then try the same for the following two bigger datasets that have been gathered over about 24 hours:

* NZMPs data (closed here)[dbfs:/datasets/MEP/NZGov/tweetsOfMPs20161006\_0900To2100hours] at (closed notebook)[https://academics.cloud.databricks.com/#notebook/140199].

* TrumpClinton20161006 data (closed here)[dbfs:/datasets/MEP/TrumpClinton20161006] at (closed notebook)[https://academics.cloud.databricks.com/#notebook/140298].



