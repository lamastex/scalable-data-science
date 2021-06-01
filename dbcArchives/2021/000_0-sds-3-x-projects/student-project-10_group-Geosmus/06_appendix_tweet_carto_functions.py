# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %python
# MAGIC import pandas as pd
# MAGIC import matplotlib
# MAGIC import matplotlib.pyplot as plt
# MAGIC import numpy as np
# MAGIC import pycountry
# MAGIC import geopandas
# MAGIC from cartogram_geopandas import make_cartogram
# MAGIC import imageio
# MAGIC 
# MAGIC def load_twitter_geo_data(path):
# MAGIC   df = spark.read.parquet(path)
# MAGIC   df = df.select('countryCode', "CurrentTweetDate")
# MAGIC   df = df.toPandas()
# MAGIC   
# MAGIC   # Add some new datetime derived columns.
# MAGIC   df["date"] = df["CurrentTweetDate"].dt.date
# MAGIC   df["year"] = df["CurrentTweetDate"].dt.year
# MAGIC   df["month"] = df["CurrentTweetDate"].dt.month
# MAGIC   df["day"] = df["CurrentTweetDate"].dt.day
# MAGIC   df["dayofweek"] = df["CurrentTweetDate"].dt.dayofweek
# MAGIC   df["hour"] = df["CurrentTweetDate"].dt.hour
# MAGIC   df["minute"] = df["CurrentTweetDate"].dt.minute
# MAGIC   df["second"] = df["CurrentTweetDate"].dt.second
# MAGIC   return df
# MAGIC 
# MAGIC def load_twitter_geo_data_with_filter(path, filter_str):
# MAGIC   df = spark.read.parquet(path)
# MAGIC   df = df.filter(filter_str).select('countryCode', "CurrentTweetDate")
# MAGIC   df = df.toPandas()
# MAGIC   
# MAGIC   # Add some new datetime derived columns.
# MAGIC   df["year"] = df["CurrentTweetDate"].dt.year
# MAGIC   df["month"] = df["CurrentTweetDate"].dt.month
# MAGIC   df["day"] = df["CurrentTweetDate"].dt.day
# MAGIC   df["dayofweek"] = df["CurrentTweetDate"].dt.dayofweek
# MAGIC   df["hour"] = df["CurrentTweetDate"].dt.hour
# MAGIC   df["minute"] = df["CurrentTweetDate"].dt.minute
# MAGIC   df["second"] = df["CurrentTweetDate"].dt.second
# MAGIC   return df
# MAGIC 
# MAGIC def country_code_grouping(df):
# MAGIC   df['count'] = df.groupby('countryCode')['countryCode'].transform('count') #The count inside the transform function calls pandas count function
# MAGIC   df_cc = df.drop_duplicates(subset=['countryCode'])
# MAGIC   df_cc = df_cc.filter(['countryCode', 'count']).reset_index()
# MAGIC   return df_cc
# MAGIC 
# MAGIC def country_code_grouping_extra(df, key):
# MAGIC   #df['count'] = df.groupby('countryCode')['countryCode'].transform('count') #The count inside the transform function calls pandas count function
# MAGIC   df_cc = df[["countryCode", key]].groupby('countryCode').sum().reset_index() #df.drop_duplicates(subset=['countryCode'])
# MAGIC   return df_cc
# MAGIC 
# MAGIC def add_iso_a3_col(df_cc):
# MAGIC   cc_dict = {}
# MAGIC   for country in pycountry.countries:
# MAGIC     cc_dict[country.alpha_2] = country.alpha_3
# MAGIC 
# MAGIC     df_cc["iso_a3"] = df_cc["countryCode"].map(cc_dict)
# MAGIC   return df_cc
# MAGIC 
# MAGIC def create_geo_df(df_cc):
# MAGIC   df_world = geopandas.read_file(geopandas.datasets.get_path('naturalearth_lowres'))
# MAGIC   # natural earth has missing iso_a3 names for France, Norway, Somalia, Kosovo and Northen Cypruys..
# MAGIC   # See the following issue: https://github.com/geopandas/geopandas/issues/1041
# MAGIC   # The following lines manually fixes it for all but Northern Cyprus, which does not have an iso_a3 code.
# MAGIC   df_world.loc[df_world['name'] == 'France', 'iso_a3'] = 'FRA'
# MAGIC   df_world.loc[df_world['name'] == 'Norway', 'iso_a3'] = 'NOR'
# MAGIC   df_world.loc[df_world['name'] == 'Somaliland', 'iso_a3'] = 'SOM'
# MAGIC   df_world.loc[df_world['name'] == 'Kosovo', 'iso_a3'] = 'RKS'
# MAGIC 
# MAGIC   numTweetDict = {}
# MAGIC   for countryCode in df_world["iso_a3"]:
# MAGIC       numTweetDict[countryCode] = 0
# MAGIC   for index, row in df_cc.iterrows():
# MAGIC       numTweetDict[row["iso_a3"]] = row["count"]
# MAGIC 
# MAGIC   df_world["numTweets"] = df_world["iso_a3"].map(numTweetDict)
# MAGIC   
# MAGIC   # Could be useful to throw away antarctica and antarctic isles.
# MAGIC   # df_world = df_world.query("(continent != 'Antarctica') or (continent != 'Seven seas (open ocean)')") 
# MAGIC 
# MAGIC   # Redundant
# MAGIC   # df_world_proj = df_world.to_crs({'init': 'EPSG:4326'})
# MAGIC   # df_world["area"] = df_world_proj['geometry'].area
# MAGIC   # df_world["tweetDensity"] = df_world["numTweets"]/df_world["area"]
# MAGIC   # df_world["tweetPerCapita"] = df_world["numTweets"]/df_world["pop_est"]
# MAGIC   return df_world
# MAGIC 
# MAGIC 
# MAGIC def create_geo_df_extra(df_cc, data_of_interest="count", default_value=0):
# MAGIC   df_world = geopandas.read_file(geopandas.datasets.get_path('naturalearth_lowres'))
# MAGIC   # natural earth has missing iso_a3 names for France, Norway, Somalia, Kosovo and Northen Cypruys..
# MAGIC   # See the following issue: https://github.com/geopandas/geopandas/issues/1041
# MAGIC   # The following lines manually fixes it for all but Northern Cyprus, which does not have an iso_a3 code.
# MAGIC   df_world.loc[df_world['name'] == 'France', 'iso_a3'] = 'FRA'
# MAGIC   df_world.loc[df_world['name'] == 'Norway', 'iso_a3'] = 'NOR'
# MAGIC   df_world.loc[df_world['name'] == 'Somaliland', 'iso_a3'] = 'SOM'
# MAGIC   df_world.loc[df_world['name'] == 'Kosovo', 'iso_a3'] = 'RKS'
# MAGIC 
# MAGIC   dataTweetDict = {}
# MAGIC   for countryCode in df_world["iso_a3"]:
# MAGIC       dataTweetDict[countryCode] = default_value
# MAGIC   for index, row in df_cc.iterrows():
# MAGIC       dataTweetDict[row["iso_a3"]] = row[data_of_interest]
# MAGIC 
# MAGIC   df_world[data_of_interest] = df_world["iso_a3"].map(dataTweetDict)
# MAGIC   
# MAGIC   # Could be useful to throw away antarctica and antarctic isles.
# MAGIC   # df_world = df_world.query("(continent != 'Antarctica') or (continent != 'Seven seas (open ocean)')") 
# MAGIC 
# MAGIC   # Redundant
# MAGIC   # df_world_proj = df_world.to_crs({'init': 'EPSG:4326'})
# MAGIC   # df_world["area"] = df_world_proj['geometry'].area
# MAGIC   # df_world["tweetDensity"] = df_world["numTweets"]/df_world["area"]
# MAGIC   # df_world["tweetPerCapita"] = df_world["numTweets"]/df_world["pop_est"]
# MAGIC   return df_world

# COMMAND ----------

def animate_cartogram(df, filterKey, filterList, out_path, nIter, cartogram_key, legend=""):
  vmax = max(df.groupby([filterKey, "countryCode"]).count()["index"]) # Get maximum count within a single country in a single hour. We will use this to fix the colorbar.
  images=[] # array for storing the png frames for the gif
  for i in filterList:
    # Load the data and add ISOa3 codes.
    df_filtered = df.query("%s==%d"%(filterKey, i)).reset_index()
    df_cc = country_code_grouping(df_filtered)
    df_cc = add_iso_a3_col(df_cc)

    # Create the geopandas dataframe
    df_world = create_geo_df(df_cc)
    #Create cartogram
    # The make_cartogram function can not handle a tweetcount of zero, so a not so elegant solution is to clip the tweet count at 1.
    # The alternative (to remove countries without tweets) is not elegant either, and causes problems when we look at the time evolution, since countries will be popping in and out of existence.
    df_world2 = df_world.copy(deep=True)
    df_world2["numTweets"] = df_world2["numTweets"].clip(lower=1)
    df_cartogram = make_cartogram(df_world2, cartogram_key, nIter, inplace=False)
    plot = df_cartogram.plot(column=cartogram_key, cmap='viridis', figsize=(20, 8), legend=True, vmin=0, vmax=vmax) 

    # Plot a vertical line indicating midnight. 360degrees/24hours = 15 degrees/hour
    if i<12:
      t_midnight = -15*i #15deg per hour
      t_noon = t_midnight + 180
    else:
      t_midnight = 180 - (i-12)*15
      t_noon = t_midnight - 180

    plt.axvline(x=t_midnight, ymin=-90, ymax=90, ls="--", c="black")
    plt.axvline(x=t_noon, ymin=-90, ymax=90, ls="--", c="yellow")
    plt.title(legend + "Time of day (GMT): %02d"%i, fontsize=24)
    plt.xlabel("Longitude $^\circ$", fontsize=20)
    plt.ylabel("Latitude $^\circ$", fontsize=20)
    plt.ylim(-90,90)
    plt.xlim(-180,180)

    #Save cartogram as a png
    fig = plot.get_figure()
    fig.savefig(out_path + "%d.png"%i)
    plt.close(fig)
    #Append images to image list
    images.append(imageio.imread(out_path + "%d.png"%i))
  #create gif from the image list
  imageio.mimsave(out_path + ".gif", images, duration=0.5)
  
def animate_cartogram_extra(df, filterKey, filterList, out_path, nIter, cartogram_key, default_value, scale_factor=2, vmin=0.0, vmax=1.0):
  # uses scaling proportional to original area of country
  images=[] # array for storing the png frames for the gif
  
  for i in filterList:
    # Load the data and add ISOa3 codes.
    df_filtered = df.query("%s==%d"%(filterKey, i)).reset_index()
    df_cc = country_code_grouping_extra(df_filtered, cartogram_key)
    df_cc = add_iso_a3_col(df_cc)

    # Create the geopandas dataframe
    df_world = create_geo_df_extra(df_cc, cartogram_key, default_value)
     
    # scale by area
    df_world["__scaled"] = (scale_factor - 1) * df_world[cartogram_key] * pd.to_numeric(df_world['geometry'].area)
      
    # make sure the quantity of interest > 0, add area to every value
    df_world["__scaled"] = pd.to_numeric(df_world['geometry'].area) + df_world["__scaled"]
    
    #Create cartogram
    df_cartogram = make_cartogram(df_world, "__scaled", nIter, inplace=False)
    
    plot = df_cartogram.plot(column=cartogram_key, cmap='viridis', figsize=(20, 8), legend=cartogram_key, vmin=vmin, vmax=vmax)
    
    # Plot a vertical line indicating midnight and one indicating noon. 360degrees/24hours = 15 degrees/hour
    if i<12:
      t_midnight = -15*i #15deg per hour
      t_noon = t_midnight + 180
    else:
      t_midnight = 180 - (i-12)*15
      t_noon = t_midnight - 180

    plt.axvline(x=t_midnight, ymin=-90, ymax=90, ls="--", c="black")
    plt.axvline(x=t_noon, ymin=-90, ymax=90, ls="--", c="yellow")
    
    plt.title("Time of day (GMT): %02d"%i, fontsize=24)
    plt.xlabel("Longitude $^\circ$", fontsize=20)
    plt.ylabel("Latitude $^\circ$", fontsize=20)
    plt.ylim(-90,90)
    plt.xlim(-180,180)
    
    #Save cartogram as a png
    fig = plot.get_figure()
    fig.savefig(out_path + "%d.png"%i)
    plt.close(fig)
    #Append images to image list
    images.append(imageio.imread(out_path + "%d.png"%i))
  #create gif from the image list
  imageio.mimsave(out_path + ".gif", images, duration=0.5)

# COMMAND ----------

def load_twitter_geo_data_sentiment(path):
  df = spark.read.parquet(path)
  df = df.select('countryCode', "CurrentTweetDate", "prayer", "monkey", "happy", "SK", "cat", "notHappy")
  df = df.toPandas()
  
  # Add some new datetime derived columns.
  df["date"] = df["CurrentTweetDate"].dt.date
  df["year"] = df["CurrentTweetDate"].dt.year
  df["month"] = df["CurrentTweetDate"].dt.month
  df["day"] = df["CurrentTweetDate"].dt.day
  df["dayofweek"] = df["CurrentTweetDate"].dt.dayofweek
  df["hour"] = df["CurrentTweetDate"].dt.hour
  df["minute"] = df["CurrentTweetDate"].dt.minute
  df["second"] = df["CurrentTweetDate"].dt.second
  return df

def country_code_grouping_sentiment(df):
  df['count'] = df.groupby(['countryCode', 'sentiment'])['countryCode'].transform('count') #The count inside the transform function calls pandas count function
  df["sentiment"] = df.groupby(['countryCode'])["sentiment"].transform("mean")
  df = df.drop_duplicates("countryCode")
  df_cc = df.filter(['countryCode', 'count', "sentiment"]).reset_index()
  return df_cc

def create_geo_df_sentiment(df_cc):
  df_world = geopandas.read_file(geopandas.datasets.get_path('naturalearth_lowres'))
  # natural earth has missing iso_a3 names for France, Norway, Somalia, Kosovo and Northen Cypruys..
  # See the following issue: https://github.com/geopandas/geopandas/issues/1041
  # The following lines manually fixes it for all but Northern Cyprus, which does not have an iso_a3 code.
  df_world.loc[df_world['name'] == 'France', 'iso_a3'] = 'FRA'
  df_world.loc[df_world['name'] == 'Norway', 'iso_a3'] = 'NOR'
  df_world.loc[df_world['name'] == 'Somaliland', 'iso_a3'] = 'SOM'
  df_world.loc[df_world['name'] == 'Kosovo', 'iso_a3'] = 'RKS'

  numTweetDict = {}
  for countryCode in df_world["iso_a3"]:
      numTweetDict[countryCode] = 0
  for index, row in df_cc.iterrows():
      numTweetDict[row["iso_a3"]] = row["count"]
  df_world["numTweets"] = df_world["iso_a3"].map(numTweetDict)

  sentimentDict = {}
  for countryCode in df_world["iso_a3"]:
      sentimentDict[countryCode] = 0
  for index, row in df_cc.iterrows():
      sentimentDict[row["iso_a3"]] = row["sentiment"]
  df_world["sentiment"] = df_world["iso_a3"].map(sentimentDict)
  
  # Could be useful to throw away antarctica and antarctic isles.
  # df_world = df_world.query("(continent != 'Antarctica') or (continent != 'Seven seas (open ocean)')") 

  # Redundant
  # df_world_proj = df_world.to_crs({'init': 'EPSG:4326'})
  # df_world["area"] = df_world_proj['geometry'].area
  # df_world["tweetDensity"] = df_world["numTweets"]/df_world["area"]
  # df_world["tweetPerCapita"] = df_world["numTweets"]/df_world["pop_est"]
  return df_world

def animate_cartogram_sentiment(df, filterKey, filterList, out_path, nIter, cartogram_key, minSamples, cmap, vmin, vmax, legendList):

  images=[] # array for storing the png frames for the gif
  frameCount = 0
  for i in filterList:
    
    # Load the data and add ISOa3 codes.
    df_filtered = df.query("%s==%d"%(filterKey, i)).reset_index()
    df_cc = country_code_grouping_sentiment(df_filtered)
    df_cc = add_iso_a3_col(df_cc)

    # Create the geopandas dataframe
    df_world = create_geo_df_sentiment(df_cc)
    
    #Create cartogram
    # The make_cartogram function can not handle a tweetcount of zero, so a not so elegant solution is to clip the tweet count at 1.
    # The alternative (to remove countries without tweets) is not elegant either, and causes problems when we look at the time evolution, since countries will be popping in and out of existence.
    df_world2 = df_world.copy(deep=True)
    df_world2["numTweets"] = df_world2["numTweets"].clip(lower=1)
    
    # We want to color all countries with less than minSamples tweets grey.
    # The colormap will do this if these countries sentiment score is below vmin.
    df_world2.loc[df_world["numTweets"] < minSamples, 'sentiment'] = vmin -1
    df_cartogram = make_cartogram(df_world2, cartogram_key, nIter, inplace=False)
    plot = df_cartogram.plot(column="sentiment", cmap=cmap, figsize=(20, 8), legend=True, vmin=vmin, vmax=vmax)

    plt.title(legendList[frameCount], fontsize=24)
    plt.xlabel("Longitude $^\circ$", fontsize=20)
    plt.ylabel("Latitude $^\circ$", fontsize=20)
    plt.ylim(-90,90)
    plt.xlim(-180,180)
    
    frameCount += 1
    #Save cartogram as a png
    fig = plot.get_figure()
    fig.savefig(out_path + "%d.png"%i)
    plt.close(fig)
    #Append images to image list
    images.append(imageio.imread(out_path + "%d.png"%i))
  #create gif from the image list
  imageio.mimsave(out_path + ".gif", images, duration=1)