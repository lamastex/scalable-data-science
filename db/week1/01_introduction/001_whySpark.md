// Databricks notebook source exported at Thu, 16 Jun 2016 07:55:27 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week1/01_introduction/001_whySpark.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/week1/01_introduction/001_whySpark](http://img.youtube.com/vi/O8JbxgPpAU8/0.jpg)](https://www.youtube.com/v/O8JbxgPpAU8?rel=0&autoplay=1&modestbranding=1&start=2065&end=3330)





# The Berkeley Data Analytics Stack is BDAS
## Spark is a sub-stack of BDAS
**Source:** 
* [Ion Stoica's State of Spark Union AmpCamp 6, Nov 2015](https://www.slideshare.net/secret/9ON8EEAlVKP3Sl)
* [Machine learning: Trends, perspectives, and prospects, M. I. Jordan, T. M. Mitchell, Science  17 Jul 2015: Vol. 349, Issue 6245, pp. 255-260, DOI: 10.1126/science.aaa8415](http://science.sciencemag.org/content/349/6245/255.full-text.pdf+html)

### BDAS State of The Union Talk by Ion Stoica, AMP Camp 6, Nov 2015 
The followign talk outlines the motivation and insights behind BDAS' research approach and how they address the cross-disciplinary nature of Big Data challenges and current work.
* **watch now (5 mins.):** 

[![Ion Stoica on State of Spark Union AmpCamp6](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/images/week1/stateofthebdasunionAmpCamp6Stoica-5_YTCover.png)](https://www.youtube.com/v/s7kj9XzRBQk?rel=0&autoplay=1&modestbranding=1&start=91&end=386)
 
## key points
* started in 2011 with strong public-private funding
  * Defense Advanced Research Projects Agency
  * Lawrance Berkeley Laboratory
  * National Science Foundation
  * Amazon Web Services
  * Google
  * SAP
* The Berkeley AMPLab is creating a new approach to data analytics to seamlessly integrate the three main resources available for making sense of data at scale: 
  * Algorithms (machine learning and statistical techniques), 
  * Machines (in the form of scalable clusters and elastic cloud computing), and 
  * People (both individually as analysts and in crowds). 
* The lab is realizing its ideas through the development of a freely-available Open Source software stack called BDAS: the Berkeley Data Analytics Stack. 
* Several components of BDAS have gained significant traction in industry and elsewhere, including: 
  * the Mesos cluster resource manager, 
  * the Spark in-memory computation framework, a sub-stack of the BDAS stack, 
  * and more... 





### The big data problem, Hardware, distributing work, handling failed and slow machines
#### by Anthony Joseph in BerkeleyX/CS100.1x

* **(watch now 1:48)**: The Big Data Problem
  * [![The Big Data Problem by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/0JdJe5iehhw/0.jpg)](https://www.youtube.com/v/0JdJe5iehhw?rel=0&autoplay=1&modestbranding=1&start=1)
*  **(watch now 1:43)**: Hardware for Big Data
  * [![Hardware for Big Data by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/KmIIMdsXGzc/0.jpg)](https://www.youtube.com/v/KmIIMdsXGzc?rel=0&autoplay=1&modestbranding=1&start=1)
* **(watch now 1:17)**: How to distribute work across a cluster of commodity machines?
  * [![How to distribute work across a cluster of commodity machines? by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/Euk1v3VtNcM/0.jpg)](https://www.youtube.com/v/Euk1v3VtNcM?rel=0&autoplay=1&modestbranding=1&start=1)
* **(watch now 0:36)**: How to deal with failures or slow machines?
  * [![How to deal with failures or slow machines? by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/NaHNsPEK3KA/0.jpg)](https://www.youtube.com/v/NaHNsPEK3KA?rel=0&autoplay=1&modestbranding=1&start=1)
  





## MapReduce and Apache Spark.
#### by Anthony Joseph in BerkeleyX/CS100.1x

* **(watch now 1:48)**: Map Reduce (is bounded by Disk I/O)
  * [![The Big Data Problem by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/NqG_hYAKjYk/0.jpg)](https://www.youtube.com/v/NqG_hYAKjYk?rel=0&autoplay=1&modestbranding=1&start=1)
*  **(watch now 2:49)**: Apache Spark (uses Memory instead of Disk)
  * [![Apache Spark by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/vat5Jki1lbI/0.jpg)](https://www.youtube.com/v/vat5Jki1lbI?rel=0&autoplay=1&modestbranding=1&start=1)
* **(watch now 3:00)**: Spark Versus MapReduce
  * [![Spark Versus MapReduce by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/Ddq3Gua2QFg/0.jpg)](https://www.youtube.com/v/Ddq3Gua2QFg?rel=0&autoplay=1&modestbranding=1&start=1)
* SUMMARY
    * uses memory instead of disk alone and is thus fater than Hadoop MapReduce
    * resilience abstraction is by RDD (resilient distributed dataset)
    * RDDs can be recovered upon failures from their *lineage graphs*, the recipes to make them starting from raw data
    * Spark supports a lot more than MapReduce, including streaming, interactive in-memory querying, etc.
    * Spark demonstrated an unprecedented sort of 1 petabyte (1,000 terabytes) worth of data in 234 minutes running on 190 Amazon EC2 instances (in 2015).
    * Spark expertise corresponds to the highest Median Salary in the US (~ 150K)





## Key Papers

* Key Historical Milestones
  * 1956-1979: [Stanford, MIT, CMU, and other universities develop set/list operations in LISP, Prolog, and other languages for parallel processing](http://www-formal.stanford.edu/jmc/history/lisp/lisp.html)
  * 2004: **READ**: [Google's MapReduce: Simplified Data Processing on Large Clusters, by Jeffrey Dean and Sanjay Ghemawat](http://research.google.com/archive/mapreduce.html)
  * 2006: [Yahoo!'s Apache Hadoop, originating from the Yahoo!’s Nutch Project, Doug Cutting](http://developer.yahoo.com/hadoop/)
  * 2009: [Cloud computing with Amazon Web Services Elastic MapReduce](http://aws.amazon.com/elasticmapreduce/), a Hadoop version modified for Amazon Elastic Cloud Computing (EC2) and Amazon Simple Storage System (S3), including support for Apache Hive and Pig.
  * 2010: **READ**: [The Hadoop Distributed File System, by Konstantin Shvachko, Hairong Kuang, Sanjay Radia, and Robert Chansler. IEEE MSST](http://dx.doi.org/10.1109/MSST.2010.5496972) 
* Apache Spark Core Papers</h1>
  * 2010: [Spark: Cluster Computing with Working Sets, Matei Zaharia, Mosharaf Chowdhury, Michael J. Franklin, Scott Shenker, Ion Stoica. USENIX HotCloud](http://people.csail.mit.edu/matei/papers/2010/hotcloud_spark.pdf).
  * 2012: **READ**: [Resilient Distributed Datasets: A Fault-Tolerant Abstraction for In-Memory Cluster Computing, Matei Zaharia, Mosharaf Chowdhury, Tathagata Das, Ankur Dave, Justin Ma, Murphy McCauley, Michael J. Franklin, Scott Shenker and Ion Stoica. NSDI](http://usenix.org/system/files/conference/nsdi12/nsdi12-final138.pdf)
  
  ![brief history of functional programming and big data by SparkCamp](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_BriefHistoryFuncProgBigData700x.png)





***
***
55 minutes
55 out of 90+10 minutes.

We have come to the end of this section.

**Next let us get everyone to login to databricks** to get our hands dirty with some Spark code! 

10-15 minutes.
Then break for 5.
***
***






## EXTRA: For a historical insight see excerpts from an interview with Ion Stoica

#### Beginnings of Apache Spark and Databricks (academia-industry roots)
[![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/v/bHH8sG-F9tg?rel=0&autoplay=1&modestbranding=1&start=231&end=365)

#### Advantages of Apache Spark: A Unified System for Batch, Stream, Interactive / Ad Hoc or Graph Processing
[![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/v/bHH8sG-F9tg?rel=0&autoplay=1&modestbranding=1&start=458&end=726)

#### Main Goal of Databricks Cloud: To Make Big Data Easy
[![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/v/bHH8sG-F9tg?rel=0&autoplay=1&modestbranding=1&start=890&end=985)

***
***






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
