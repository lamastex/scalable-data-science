// Databricks notebook source exported at Tue, 28 Jun 2016 09:54:51 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week10/035_ScalableGeoSpatialComputing.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/week2/week10/035_ScalableGeoSpatialComputing](http://img.youtube.com/vi/0wKxVfeBQBc/0.jpg)](https://www.youtube.com/v/0wKxVfeBQBc?rel=0&autoplay=1&modestbranding=1&start=0&end=753)





# What is Geospatial Analytics?


**(watch now 3 minutes and 23 seconds)**:

[![Spark Summit East 2016 - What is Geospatial Analytics by Ram Sri Harsha](http://img.youtube.com/vi/1lF1oSjxMT4/0.jpg)](https://www.youtube.com/v/1lF1oSjxMT4?rel=0&autoplay=1&modestbranding=1&start=111&end=314)






# Some Concrete Examples of Scalable Geospatial Analytics

## 1. Let us check out cross-domain data fusion in MSR's Urban Computing Group 
* lots of interesting papers to read at [http://research.microsoft.com/en-us/projects/urbancomputing/](http://research.microsoft.com/en-us/projects/urbancomputing/).


```scala

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("http://research.microsoft.com/en-us/projects/urbancomputing/",700))

```



## 1. Several sciences are naturally geospatial 
* forestry, 
* geography, 
* geology, 
* seismology, 
* etc. etc.

See for example the global EQ datastreams from US geological Service below.

**A bold idea: **
Imagine the non-parametric inference problem of estimating co-exciting Hawkes-like processes for modelling earth quakes on the entire planet! 

For a global data source, see US geological Service's Earthquake hazards Program ["http://earthquake.usgs.gov/data/](http://earthquake.usgs.gov/data/).


```scala

displayHTML(frameIt("http://earthquake.usgs.gov/data/",700))

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
