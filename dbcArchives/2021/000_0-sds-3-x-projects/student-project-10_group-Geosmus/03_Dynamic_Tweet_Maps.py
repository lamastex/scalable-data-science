# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC # Dynamic Tweet Maps
# MAGIC In this notebook we are going to make some maps depicting when and where tweets are sent.
# MAGIC 
# MAGIC We will also use the sentiment classes from the previous notebooks to illustrate which type of tweets are frequent at different times in different countries.

# COMMAND ----------

# MAGIC %md
# MAGIC ## Dependencies:
# MAGIC In order to run this notebook you need to install some dependencies, via apt, pip and git.
# MAGIC To install the dependencies run the following three cells (it might take a few minutes).
# MAGIC 
# MAGIC (We have used the cluster  "small-2-8Ws-class-01-sp3-sc2-12" for our experiments.)

# COMMAND ----------

# MAGIC %sh
# MAGIC sudo apt-get -y install libproj-dev
# MAGIC sudo apt-get -y install libgeos++-dev

# COMMAND ----------

# MAGIC %python 
# MAGIC %pip install plotly
# MAGIC %pip install pycountry
# MAGIC %pip install geopandas
# MAGIC %pip install geoplot
# MAGIC %pip install imageio

# COMMAND ----------

# MAGIC %sh
# MAGIC git clone https://github.com/mthh/cartogram_geopandas.git
# MAGIC cd cartogram_geopandas/
# MAGIC python setup.py install

# COMMAND ----------

# MAGIC %md
# MAGIC ## Defining some functions
# MAGIC We will process the collected data a bit before making any plots.
# MAGIC To do this we will load some functions from notebook 06_appendix_tweet_carto_functions.

# COMMAND ----------

# MAGIC %run "./06_appendix_tweet_carto_functions"

# COMMAND ----------

# MAGIC %md 
# MAGIC ## Initial processing of dataframe
# MAGIC This step only needs to be run if you want to overwrite the existing preprocessed dataframea "processedDF.csv".
# MAGIC Otherwise you can just skip to the next cell and load the already generated dataframe.

# COMMAND ----------

path = "/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/{2020,2021}/*/*/*/*/*"
df = load_twitter_geo_data(path)
#Data collection was continuous during the 22nd December whereas for the remaining days we only streamed for 3 minutes per hour.
df = df[(df['day'] != 22) & (df['day'] != 2 )].reset_index() #Data collection was continuous during the 22nd December whereas for the remaining days we only streamed for 3 minutes per hour. Data collection ended in the middle of Jan second so to only have full day we disregard Jan 2.
pre_proc_path = "/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/tmp/processedDF.csv"
df.to_csv(pre_proc_path)

# COMMAND ----------

# MAGIC %md
# MAGIC ## Quick initial data exploration
# MAGIC Let's start by having a look at the first 5 elements.
# MAGIC 
# MAGIC For now we are only looking at country of origin and timestamp, so we have neither loaded the tweets or the derived sentiment classes from before.

# COMMAND ----------

# Load the data
pre_proc_path = "/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/tmp/processedDF.csv"
df = pd.read_csv(pre_proc_path)
display(df.head(5))

# COMMAND ----------

# MAGIC %md
# MAGIC The tweets we have loaded were sent between Dec 23 and Jan 1, and the timestamps are given in Greenwich Mean Time (GMT).
# MAGIC We can use the display function again to have a look at how tweets are distributed as a function of time of day. 
# MAGIC To get tweets from a single timezone we just have a look at tweets from the United Kingdom (which has the country code *GB*).
# MAGIC 
# MAGIC The histograms below show a clear dip in twitter activity from around eleven at night untill around eight in the morning.
# MAGIC The night between the 24th and 25th of December shows a small spike right after midnight and new years shows a large spike after midnight.
# MAGIC It seems that people like to tweet when celebrating!
# MAGIC 
# MAGIC There is an abnormal peak on December 27th at 21. We have not been able to determine what this is due to.
# MAGIC 
# MAGIC NOTE: The cell below has been configured (via the display user interface) to show histograms using date as key and hour as values.

# COMMAND ----------

display(df.query("countryCode=='GB'"))

# COMMAND ----------

# MAGIC %md 
# MAGIC If we look at Saudi Arabia it seems that something is happening on December 31rst and December 28th.
# MAGIC 
# MAGIC * The timestamps are in GMT, so the smalller peak at 21:00 on December 31rst is actually located at midnight. Although Saudi Arabia follows the islamic calendar it looks like there are still people celebrating the gregorian new year. We do not know what could be the reason for the larger peak on December 31rst.
# MAGIC 
# MAGIC * The largest spike is located on December 28th. It covers tweets send between 12:00 and 16:00 GMT, which would correspond to 15:00-18:00 local time. We have tried to find the cause of this peak, and we think it might be due to the conviction of female rights activist Loujain al-Hathloul who was sentenced to five years and 8 months of prison on this date. We could not find an exact timestamp for this event, but the report from france24 is timestamped to 12:20 (presumably Western European time), which corresponds to 14:20 in Saudi Arabia. It might be that there was a bit of lag time between media starting to report on the events and the case gaining traction on social media. Of course the spike could also be due to something else, but this sentencing seems like a likely cause.
# MAGIC 
# MAGIC 
# MAGIC Links:
# MAGIC * https://www.thehindu.com/news/international/saudi-activist-loujain-al-hathloul-sentenced-to-5-years-8-months-in-prison/article33437467.ece Timestamp: 17:30 IST corresponding to 15:00 in Saudi Arabia
# MAGIC * https://www.aljazeera.com/news/2020/12/28/saudi-court-hands-jail-sentence-to-womens-rights-activist No timestamp
# MAGIC * https://www.france24.com/en/live-news/20201228-saudi-activist-loujain-al-hathloul-jailed-for-5-years-8-months Timestamp: 12:20 CET corresponding to 14:20 in Saudi Arabia
# MAGIC 
# MAGIC NOTE: The articles from thehindu.com and France24 are identical, as they both come from AFP.

# COMMAND ----------

display(df.query("countryCode=='SA'"))

# COMMAND ----------

# MAGIC %md
# MAGIC The display function produces normalized histograms, so the below cell prints the daily number of tweets. 
# MAGIC 
# MAGIC There is indeed a lot more tweets collected on the 28th and on the 31rst.

# COMMAND ----------

dateList = ["2020-12-23", "2020-12-24", "2020-12-25", "2020-12-26", "2020-12-27", "2020-12-28", "2020-12-29", "2020-12-30", "2020-12-31", "2021-01-01"]
for d in dateList:
  N = len(df.query("(countryCode=='SA') and (date=='%s')"%d))
  print("%d tweets were collected in Saudi Arabia on %s"%(N, d))

# COMMAND ----------

# MAGIC %md
# MAGIC ## Mapping the tweets 
# MAGIC To create maps using the tweets we will first group the tweets by country codes, producing a new dataframe with one row per country.
# MAGIC 
# MAGIC The map below is generated by selecting the map option in the display UI.
# MAGIC This view only offers a simple discrete colorbar, but we can see that the most tweet producing countries are the United States and Brazil. Hovering above countries gives more detailed information, and shows that the Japan, the UK and India also produce a lot of tweets.
# MAGIC 
# MAGIC We also get tweets from a number of countries which have blocked access to twitter (China: 1825, North Korea: 5, Iran: 1590 and Turkmenistan: 7).
# MAGIC 
# MAGIC https://en.wikipedia.org/wiki/Censorship_of_Twitter#Government_blocking_of_Twitter_access

# COMMAND ----------

# Group by country code
df_cc = country_code_grouping(df)
df_cc = add_iso_a3_col(df_cc)
df_cc = df_cc[["iso_a3", "count"]] #reorder to have iso_a3 as first column (required in order to use the map view in display). Also we don't need countryCode and index columns.

# Inspect result
display(df_cc)

# COMMAND ----------

# MAGIC %md
# MAGIC Right now we are dealing with a regular dataframe. But to make some more advanced plots we will need information about the shapes of the countries (in the form of polygons).
# MAGIC We get the shapes via the function create_geo_df(), which relies on the geopandas library. When calling display on a geopandas dataframe we just get the raw table, and none of the premade visualizations we get when  displaying pandas dataframes.

# COMMAND ----------

# Create the geopandas dataframe
df_world = create_geo_df(df_cc)
# Inspect result
display(df_world.head())

# COMMAND ----------

# MAGIC %md
# MAGIC ### Choropleth maps
# MAGIC * A choropleth map is a map in which regions are assigned a color based on a numerical attribute. This could for example be each regions population, average life expectancy, or number of geo tagged tweets. 
# MAGIC 
# MAGIC ###  Cartograms
# MAGIC * A cartogram is a way to represent geograpic differences in some variable, by altering the size of regions. The areas and shapes of regions are distorted in order to create an approximately equal density of the selected variable across all regions. In our case there are a lot of tweets coming from brazil and the US so these regions will grow in order to produce a lower tweet density. There is a tradeoff between shape preservation and getting equal density. This is especially evident when the density you are trying to equalize differs by several orders of magnitude. 
# MAGIC * The available python cartogram package cartogram_geopandas is based on Dougenik, J. A, N. R. Chrisman, and D. R. Niemeyer: 1985. "An algorithm to construct continuous cartograms". It is very sparsely documented and does not report error scores and tends to diverge if allowed to run for too many iterations. We found the Gui program Scapetoad to offer superior performance. Scapetoad is based on Mark Newman's C-code "Cart", which is based on his and Michael T. Gastner's paper "Diffusion-based method for producingdensity-equalizing maps" from 2004. Scapetoad is working on a Python API, but it has not been released yet. So inspite of its shortcomings we will be using cartogram_geopandas, since Scapetoad can not be called from a notebook.
# MAGIC * Due to the limitations of the cartogram_geopandas library, we choose to run for a modest number of iterations. This means we do not have direct proportionality between distorted area and number of tweets, rather the distortions give us a qualitative representation of where tweeting is frequent.
# MAGIC 
# MAGIC Links:
# MAGIC * mthh, 2015: https://github.com/mthh/cartogram_geopandas
# MAGIC * Dougenik, Chrisman and Niemeyer, 1985: https://onlinelibrary.wiley.com/doi/epdf/10.1111/j.0033-0124.1985.00075.x
# MAGIC * Gastner and Newman: https://arxiv.org/abs/physics/0401102

# COMMAND ----------

# MAGIC %md
# MAGIC To begin with we will plot all the tweets collected between 23:00:00 and 23:59:59 (GMT) on December 31rst.
# MAGIC On the left side a world map with colors indicating number of tweets is shown, and on the right side a cartogram.
# MAGIC It seems the United kingdom is tweeting a lot at this moment, which makes sense as they are about to enter 2021. Happy new year!

# COMMAND ----------

df = pd.read_csv(pre_proc_path)
df = df.query("(day==31) and (hour==23)")
# Group by country code
df_cc = country_code_grouping(df)
df_cc = add_iso_a3_col(df_cc)
df_cc = df_cc[["iso_a3", "count"]] #reorder to have iso_a3 as first column (required in order to use the map view in display). Also we don't need countryCode and index columns.
# Create the geopandas dataframe
df_world = create_geo_df(df_cc)


#fig, axes = plt.subplots(nrows=1, ncols=2, figsize=(30,15))

# Make a choropleth plot
# df_world.plot(column='numTweets', cmap='viridis', ax=axes[0])

# The make_cartogram function can not handle a tweetcount of zero, so a not so elegant solution is to clip the tweet count at 1.
# The alternative (to remove countries without tweets) is not elegant either (and causes problems when we look at the time evolution, since countries will be popping in and out of existence).
df_world["numTweets"] = df_world["numTweets"].clip(1, max(df_world["numTweets"])) 

df_cartogram = make_cartogram(df_world, 'numTweets', 5, inplace=False)
# df_cartogram.plot(column='numTweets', cmap='viridis', ax=axes[1]) 


# plt.show()

# COMMAND ----------

# MAGIC %md
# MAGIC <img src ='https://raw.githubusercontent.com/Rasmuskh/ScaDaMaLe_notebook_resources/main/choropleth_and_cartogram.png'>

# COMMAND ----------

# MAGIC %md
# MAGIC ### Animating the time evolution of tweets
# MAGIC Rather than looking at a snapshot of the worlds twitter activity it would be interesting to look at how twitter activity looks across hours and days.
# MAGIC 
# MAGIC The following cell generates a number of png cartograms. One for every hour between December 23rd 2020 and January 2nd 2021.
# MAGIC 
# MAGIC It takes quite long to generate them all (around eight minutes) so you might want to just load the pregenerated pngs in the cell below.
# MAGIC 
# MAGIC NOTE: Geopandas prints some warnings related to using unprojected geometries (We work in longitude and latitude rather than some standard 2D map projection). This is not an issue since we are not using the area function.

# COMMAND ----------

pre_proc_path = "/dbfs/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/tmp/processedDF.csv"
df = pd.read_csv(pre_proc_path)
key = "hour"
timeOfDayList = list(range(0, 24))
nIter = 5
cartogram_key = "numTweets"
for day in range(23,32):
  out_path = "/dbfs/FileStore/group10/cartogram/2020_Dec_%d_hour_"%day
  legend = "2020 December %d: "%day
  animate_cartogram(df.query("(day==%d) and (year==2020)"%day), key, timeOfDayList, out_path, nIter, cartogram_key, legend)
for day in range(1,2):
  out_path = "/dbfs/FileStore/group10/cartogram/2021_Jan_%d_hour_"%day
  legend = "2021 January %d: "%day
  animate_cartogram(df.query("(day==%d) and (year==2021)"%day), key, timeOfDayList, out_path, nIter, cartogram_key, legend)


# COMMAND ----------

# MAGIC %md
# MAGIC Now that we have generated the PNGs we can go ahead and combine them into a gif using the python imageIO library.

# COMMAND ----------

images=[] # array for storing the png frames for the gif
timeOfDayList = list(range(0, 24))
#Append images to image list
for day in range(23,32):
  for hour in timeOfDayList:
    out_path = "/dbfs/FileStore/group10/cartogram/2020_Dec_%d_hour_%d.png"%(day, hour)
    images.append(imageio.imread(out_path))
for day in range(1,2):
  for hour in timeOfDayList:
    out_path = "/dbfs/FileStore/group10/cartogram/2021_Jan_%d_hour_%d.png"%(day, hour)
    images.append(imageio.imread(out_path))
#create gif from the image list
imageio.mimsave("/dbfs/FileStore/group10/cartogram/many_days_cartogram.gif", images, duration=0.2)

# COMMAND ----------

# MAGIC %md
# MAGIC The result is the following animation.
# MAGIC The black vertical bar indicates where in the world it is midnight, the yellow vertical bar indicates where it is noon, and the time shown at the top is the current GMT time. The color/z-axis denotes the number of tweets produced in the given hour. Unsurprisingly we see countries inflate during the daytime and deflate close to midnight when people tend to sleep.

# COMMAND ----------

# MAGIC %md
# MAGIC <img src ='https://raw.githubusercontent.com/Rasmuskh/ScaDaMaLe_notebook_resources/main/cartogram.gif'>

# COMMAND ----------

# MAGIC %md
# MAGIC ### Sentiment mapping
# MAGIC This cartogram animation expresses the amount of tweets both through the size distortions and the z-axis (the color). In a way it is a bit redundant to illustrate the same things in two ways.
# MAGIC The plot would be more informative if instead the z-axis is used to express some measure of sentiment.
# MAGIC 
# MAGIC We will load a dataframe containing sentiment cluster information extracted in the previous notebook.
# MAGIC This dataframe only contains tweets which contained "Unicode block Emoticons", which is about 14% of the tweets we collected between December 23rd and January 1rst.
# MAGIC The dataframe has boolean columns indicating if an emoji from a certain cluster is present.
# MAGIC We will use the happy and not happy clusters found in the previous notebooks to express a single sentiment score:
# MAGIC > df["sentiment"] = (1 + df["happy"] - df["notHappy"])/2
# MAGIC This is useful since it allows us to make a map containing information about both clusters. A caveat is that the although these clusters mostly contain what we would consider happy and unhappy emojis respectively, they do also contain some rather ambivalent emojis. The unhappy cluster for example contains a smiley that is both smiling and crying. On top of this emojis can take on different meanings in different contexts.
# MAGIC Expressed in this way we get that:
# MAGIC * A sentiment value of 0 means that the tweet contains unhappy emojis and no happy emojis.
# MAGIC * A sentiment value of 0.5 means that the tweet either contain both happy and unhappy emojis or that it contains neither happy or unhappy emojis.
# MAGIC * A sentiment value of 1 means that the tweet did not contain unhappy emojis but did contain happy emojis.
# MAGIC 
# MAGIC The pie chart below reveals that unhappy tweets are significantly more common than happy ones.

# COMMAND ----------

path = "/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/processedEmoticonClusterParquets/emoticonCluster.parquet"
cluster_names = ["all", "happy", "notHappy", "cat", "monkey", "SK", "prayer"]
df = load_twitter_geo_data_sentiment(path)
df = df[(df['day'] != 22) & (df['day'] != 2 )] #Data collection was continuous during the 22nd December whereas for the remaining days we only streamed for 3 minutes per hour. Data collection ended in the middle of Jan second so to only have full day we disregard Jan 2.

# Let's try combining the happy and sad columns to one "sentiment" column. 
df["sentiment"] = (1 + df["happy"] - df["notHappy"])/2

display(df[["happy", "notHappy", "sentiment"]])

# COMMAND ----------

# MAGIC %md 
# MAGIC Next we will look at a choropleth world map using the "sentiment" score as z-axis. Countries with fewer than 100 tweets are not shown here.
# MAGIC 
# MAGIC It looks like the tweets from Africa, The US and the middle east are less happy than those from latin America, Europe, Asia and Oceania.

# COMMAND ----------

df_cc = country_code_grouping_sentiment(df)
df_cc = add_iso_a3_col(df_cc)
df_cc = df_cc[["iso_a3", "count", "sentiment"]] #reorder to have iso_a3 as first column (required in order to use the map view in display). Also we don't need countryCode and index columns.
df_cc
# Create the geopandas dataframe
df_world = create_geo_df_sentiment(df_cc)

vmin = min(df_world.query("numTweets>=20")["sentiment"])
vmax = max(df_world.query("numTweets>=20")["sentiment"])
cmap = matplotlib.colors.LinearSegmentedColormap.from_list("", ["red","yellow","green"])
cmap.set_under(color='gray', alpha=0.5)

#We filter out countries with very few emojii tweets
df_world.loc[df_world["numTweets"] < 100, 'sentiment'] = vmin -1
# df_world = df_world.query("numTweets>10").reset_index()

# Make a choropleth plot
# df_world.plot(column='sentiment', cmap=cmap, legend=True, vmin=vmin, vmax=vmax, figsize=(20,8))
# plt.title("Sentiment by country", fontsize=24)
# plt.xlabel("Longitude $^\circ$", fontsize=20)
# plt.ylabel("Latitude $^\circ$", fontsize=20)
# plt.show()

# COMMAND ----------

# MAGIC %md
# MAGIC <img src ='https://raw.githubusercontent.com/Rasmuskh/ScaDaMaLe_notebook_resources/main/choropleth_sentiment.png'>

# COMMAND ----------

# MAGIC %md
# MAGIC Let us have a look at how this sentiment score ranks the countries (with more than 100 emoji tweets) from happiest to unhappiest.
# MAGIC 
# MAGIC Japan, Sweden and Netherlands are in the lead.

# COMMAND ----------

display(df_world.query("numTweets>=100").sort_values("sentiment", ascending=False)[["name", "continent", "numTweets", "sentiment"]])

# COMMAND ----------

# MAGIC %md 
# MAGIC We are now ready to generate an animated cartogram using the number of tweets to determine the area distortions and using the sentiment score as the color dimension.
# MAGIC 
# MAGIC We do not have as many emoji tweets, so here we limit ourselves to only one frame per day. We color countries with less than 30 tweets per day grey, since their sentiment score will be extremely unreliable.
# MAGIC 
# MAGIC The following cell generates the animation, but as we already have produced it you can skip straight to the next cell where it is displayed.

# COMMAND ----------

#Load data
path = "/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/processedEmoticonClusterParquets/emoticonCluster.parquet"
cluster_names = ["all", "happy", "notHappy", "cat", "monkey", "SK", "prayer"]
df = load_twitter_geo_data_sentiment(path)
df = df[(df['day'] != 22) & (df['day'] != 2 )] #Data collection was continuous during the 22nd December whereas for the remaining days we only streamed for 3 minutes per hour. Data collection ended in the middle of Jan second so to only have full day we disregard Jan 2.

# Combine the happy and sad columns into one "sentiment" column. 
df["sentiment"] = (1 + df["happy"] - df["notHappy"])/2

# Arguments for the function animate_cartogram_sentiment(...)
legendList = ["2020-12-23", "2020-12-24", "2020-12-25", "2020-12-26", "2020-12-27", "2020-12-28", "2020-12-29", "2020-12-30", "2020-12-31", "2021-01-01"]
key = "day"
nIter = 5
minSamples = 30
cartogram_key = "numTweets"
dayList = [23, 24, 25, 26, 27, 28, 29, 30, 31, 1]
cmap = matplotlib.colors.LinearSegmentedColormap.from_list("", ["red","yellow","green"])
cmap.set_under(color='gray', alpha=0.5)
cmap.set_bad(color='gray', alpha=0.5)

# Find upper and lower range for the color dimension.
# We want to utilize the full dynamic range of the z-axis.
vmin = 0.45
vmax = 0.55
for day in dayList:
  df_filtered = df.query("day==%d"%day).reset_index()
  df_cc = country_code_grouping_sentiment(df_filtered)
  df_cc = df_cc.query("count>%d"%minSamples)
  lower = min(df_cc["sentiment"])
  upper = max(df_cc["sentiment"])
  if lower<vmin:
    vmin = lower
  if upper>vmax:
    vmax = upper

out_path = "/dbfs/FileStore/group10/cartogram/sentiment"
animate_cartogram_sentiment(df.reset_index(), key, dayList, out_path, nIter, cartogram_key, minSamples, cmap, vmin, vmax, legendList)

# COMMAND ----------

# MAGIC %md
# MAGIC Unfortunately we do not have enough data to color all the countries, but some interesting things can still be observed.
# MAGIC * Most countries tweet happier at Christmas and New Years. Take a look at Spain and Brazil for example.
# MAGIC * Japan and most of Europe looks consistently happy, while Africa, Saudi Arabia and the US looks unhappy.
# MAGIC * The UK looks comparatively less happy than the rest of Europe.
# MAGIC 
# MAGIC We keep in mind that these differences may be caused by or exaggerated by differences in how emojis are used differently in different countries.

# COMMAND ----------

# MAGIC %md
# MAGIC <img src ='https://raw.githubusercontent.com/Rasmuskh/ScaDaMaLe_notebook_resources/main/cartogram_sentiment.gif'>

# COMMAND ----------

# MAGIC %md
# MAGIC ### Looking at trends in emoticon use over times of the day
# MAGIC Next we aggregate all of the tweet data into one set of 24 hours, i.e. we merge all the days into one to try to see trends in emoticon use depending on time of day.
# MAGIC 
# MAGIC We want to visualize the different clusters in cartogram animations. First we filter the tweets by cluster so that we get a dataframe per cluster containing only the tweets wherein there is an emoticon from that cluster. In the cartograms we scale each country by how large a proportion of the total tweets from that country pertaining to that cluster are tweeted in a given hour. So if the area (in the current projection...) of a particular country is \\(A\\), then its "mass" (recall that these plots aim for equal "density") at hour \\(h\\) in these plots will be
# MAGIC \\[A + \sigma p_h A,\\]
# MAGIC where \\(p_h\\) is the proportion of tweets in that country and cluster that is tweeted at hour \\(h\\) and \\(\sigma\\) is a scaling factor which we set to 2. In order to reduce noise, all countries which have fewer than 100 tweets of a given cluster are set to have constant "mass" corresponding to their area.

# COMMAND ----------

path = "/datasets/ScaDaMaLe/twitter/student-project-10_group-Geosmus/processedEmoticonClusterParquets/emoticonCluster.parquet"
cluster_names = ["all", "happy", "notHappy", "cat", "monkey", "SK", "prayer"]

# COMMAND ----------

# # This cell might take 15-20 minutes to run

# for cluster_name in cluster_names:
#    if cluster_name == "all":
#      df = load_twitter_geo_data(path)
#    else:
#      df = load_twitter_geo_data_with_filter(path, cluster_name)
    
#    df = df[df['day'] != 22].reset_index() #Data collection was continuous during the 22nd December whereas for the remaining days we only streamed for 3 minutes per hour.
#    df = df[(df['day'] != 22) & (df['day'] != 2 )].reset_index() #Data collection was continuous during the 22nd December whereas for the remaining days we only streamed for 3 minutes per hour. Data collection ended in the middle of Jan second so to only have full day we disregard Jan 2.
  
#    # add column in order to be able to display ratio of total tweets
#    df['numTweets'] = df.groupby('countryCode')['countryCode'].transform('count')
#    df["proportion"] = 1 / df["numTweets"]
  
#    # filter out countries with very few tweets
#    df = df[df.numTweets >= 100]
#    # create cartogram
#    key = "hour"
#    timeOfDayList = list(range(0, 24))
#    out_path = "/dbfs/FileStore/group10/cartogram_" + cluster_name
#    nIter = 30
#    cartogram_key = "proportion"

#    animate_cartogram_extra(df[["index", "countryCode", "proportion", "hour"]], key, timeOfDayList, out_path, nIter, cartogram_key, default_value=0, scale_factor=3, vmin=0.0, vmax=0.15)


# COMMAND ----------

# MAGIC %md
# MAGIC Below are the obtained plots for emoticon use by time of day in the different countries. The colorbar corresponds to the proportion of tweets from a country tweeted in a given hour and the areas are scaled as described above. The black line is again midnight and the yellow line noon.
# MAGIC 
# MAGIC For some of the clusters, for instance "cat" and "monkey", it is clear that we have too little data to be able to say anything interesting. Perhaps the one conclusion one can draw there is that the monkey emoticons are not used very often in the US or Japan (since those countries tweet a lot but did not have more than 100 total tweets with monkey emoticons).
# MAGIC 
# MAGIC The other plots mostly show that people tweet more during the day than at night. Perhaps the amount of emoticons in each of the "happy" and "notHappy" clusters is too large to be able to find some distinctiveness in the time of day that people use them.
# MAGIC 
# MAGIC The most interesting cluster to look at in this way might be the prayer cluster where it appears that we can see glimpses of the regular prayers in countries such Egypt and Saudi Arabia. 

# COMMAND ----------

clusters_to_plot = ["happy", "notHappy", "SK", "cat", "monkey", "prayer"]
html_str = "\n".join([f"""
  <figure>
  <img src="/files/group10/cartogram_{cluster_name}.gif" style="width:60%">
  <figcaption>The "{cluster_name}" cluster.</figcaption>
  </figure>
  <hr style="height:3px;border:none;color:#333;background-color:#333;" />
  """ for cluster_name in clusters_to_plot])
  
displayHTML(html_str)

# COMMAND ----------

