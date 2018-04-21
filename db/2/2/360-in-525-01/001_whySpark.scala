// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC # Why Apache Spark?
// MAGIC 
// MAGIC * [Apache Spark: A Unified Engine for Big Data Processing](https://cacm.acm.org/magazines/2016/11/209116-apache-spark/fulltext) By Matei Zaharia, Reynold S. Xin, Patrick Wendell, Tathagata Das, Michael Armbrust, Ankur Dave, Xiangrui Meng, Josh Rosen, Shivaram Venkataraman, Michael J. Franklin, Ali Ghodsi, Joseph Gonzalez, Scott Shenker, Ion Stoica 
// MAGIC Communications of the ACM, Vol. 59 No. 11, Pages 56-65
// MAGIC 10.1145/2934664
// MAGIC 
// MAGIC [![Apache Spark ACM Video](https://i.vimeocdn.com/video/597494216_640.jpg)](https://player.vimeo.com/video/185645796)
// MAGIC 
// MAGIC Right-click the above image-link, open in a new tab and watch the video (4 minutes) or read about it in the Communications of the ACM in the frame below or from the link above.

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """">
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://cacm.acm.org/magazines/2016/11/209116-apache-spark/fulltext",600))

// COMMAND ----------

// MAGIC %md
// MAGIC # Some BDAS History behind Apache Spark
// MAGIC ## The Berkeley Data Analytics Stack is BDAS
// MAGIC ### Spark is a sub-stack of BDAS
// MAGIC 
// MAGIC **Source:** 
// MAGIC 
// MAGIC * [Ion Stoica's State of Spark Union AmpCamp 6, Nov 2015](https://www.slideshare.net/secret/9ON8EEAlVKP3Sl)
// MAGIC * [Machine learning: Trends, perspectives, and prospects, M. I. Jordan, T. M. Mitchell, Science  17 Jul 2015: Vol. 349, Issue 6245, pp. 255-260, DOI: 10.1126/science.aaa8415](http://science.sciencemag.org/content/349/6245/255.full-text.pdf+html)
// MAGIC 
// MAGIC ### BDAS State of The Union Talk by Ion Stoica, AMP Camp 6, Nov 2015 
// MAGIC The followign talk outlines the motivation and insights behind BDAS' research approach and how they address the cross-disciplinary nature of Big Data challenges and current work.
// MAGIC * **watch later (5 mins.):** 
// MAGIC 
// MAGIC [![Ion Stoica on State of Spark Union AmpCamp6](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/images/week1/stateofthebdasunionAmpCamp6Stoica-5_YTCover.png)](https://www.youtube.com/watch?v=s7kj9XzRBQk&start=91&end=386)
// MAGIC  
// MAGIC ## key points
// MAGIC * started in 2011 with strong public-private funding
// MAGIC   * Defense Advanced Research Projects Agency
// MAGIC   * Lawrance Berkeley Laboratory
// MAGIC   * National Science Foundation
// MAGIC   * Amazon Web Services
// MAGIC   * Google
// MAGIC   * SAP
// MAGIC * The Berkeley AMPLab is creating a new approach to data analytics to seamlessly integrate the three main resources available for making sense of data at scale: 
// MAGIC   * Algorithms (machine learning and statistical techniques), 
// MAGIC   * Machines (in the form of scalable clusters and elastic cloud computing), and 
// MAGIC   * People (both individually as analysts and in crowds). 
// MAGIC * The lab is realizing its ideas through the development of a freely-available Open Source software stack called BDAS: the Berkeley Data Analytics Stack. 
// MAGIC * Several components of BDAS have gained significant traction in industry and elsewhere, including: 
// MAGIC   * the Mesos cluster resource manager, 
// MAGIC   * the Spark in-memory computation framework, a sub-stack of the BDAS stack, 
// MAGIC   * and more... 

// COMMAND ----------

// MAGIC %md
// MAGIC ### The big data problem, Hardware, distributing work, handling failed and slow machines
// MAGIC #### by Anthony Joseph in BerkeleyX/CS100.1x
// MAGIC 
// MAGIC * **(watch now 1:48)**: The Big Data Problem
// MAGIC   * [![The Big Data Problem by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/0JdJe5iehhw/0.jpg)](https://www.youtube.com/watch?v=0JdJe5iehhw&modestbranding=1&start=1)
// MAGIC *  **(watch now 1:43)**: Hardware for Big Data
// MAGIC   * [![Hardware for Big Data by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/KmIIMdsXGzc/0.jpg)](https://www.youtube.com/watch?v=KmIIMdsXGzc&rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC * **(watch now 1:17)**: How to distribute work across a cluster of commodity machines?
// MAGIC   * [![How to distribute work across a cluster of commodity machines? by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/Euk1v3VtNcM/0.jpg)](https://www.youtube.com/watch?v=Euk1v3VtNcM&rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC * **(watch now 0:36)**: How to deal with failures or slow machines?
// MAGIC   * [![How to deal with failures or slow machines? by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/NaHNsPEK3KA/0.jpg)](https://www.youtube.com/watch?v=NaHNsPEK3KA&rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC   

// COMMAND ----------

// MAGIC %md
// MAGIC ## MapReduce and Apache Spark.
// MAGIC #### by Anthony Joseph in BerkeleyX/CS100.1x
// MAGIC 
// MAGIC * **(watch now 1:48)**: Map Reduce (is bounded by Disk I/O)
// MAGIC   * [![The Big Data Problem by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/NqG_hYAKjYk/0.jpg)](https://www.youtube.com/watch?v=NqG_hYAKjYk&rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC *  **(watch now 2:49)**: Apache Spark (uses Memory instead of Disk)
// MAGIC   * [![Apache Spark by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/vat5Jki1lbI/0.jpg)](https://www.youtube.com/watch?v=vat5Jki1lbI&rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC * **(watch now 3:00)**: Spark Versus MapReduce
// MAGIC   * [![Spark Versus MapReduce by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/Ddq3Gua2QFg/0.jpg)](https://www.youtube.com/watch?v=Ddq3Gua2QFg&rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC * SUMMARY
// MAGIC     * uses memory instead of disk alone and is thus fater than Hadoop MapReduce
// MAGIC     * resilience abstraction is by RDD (resilient distributed dataset)
// MAGIC     * RDDs can be recovered upon failures from their *lineage graphs*, the recipes to make them starting from raw data
// MAGIC     * Spark supports a lot more than MapReduce, including streaming, interactive in-memory querying, etc.
// MAGIC     * Spark demonstrated an unprecedented sort of 1 petabyte (1,000 terabytes) worth of data in 234 minutes running on 190 Amazon EC2 instances (in 2015).
// MAGIC     * Spark expertise corresponds to the highest Median Salary in the US (~ 150K)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Key Papers
// MAGIC 
// MAGIC * Key Historical Milestones
// MAGIC   * 1956-1979: [Stanford, MIT, CMU, and other universities develop set/list operations in LISP, Prolog, and other languages for parallel processing](http://www-formal.stanford.edu/jmc/history/lisp/lisp.html)
// MAGIC   * 2004: **READ**: [Google's MapReduce: Simplified Data Processing on Large Clusters, by Jeffrey Dean and Sanjay Ghemawat](http://research.google.com/archive/mapreduce.html)
// MAGIC   * 2006: [Yahoo!'s Apache Hadoop, originating from the Yahoo!’s Nutch Project, Doug Cutting](http://developer.yahoo.com/hadoop/)
// MAGIC   * 2009: [Cloud computing with Amazon Web Services Elastic MapReduce](http://aws.amazon.com/elasticmapreduce/), a Hadoop version modified for Amazon Elastic Cloud Computing (EC2) and Amazon Simple Storage System (S3), including support for Apache Hive and Pig.
// MAGIC   * 2010: **READ**: [The Hadoop Distributed File System, by Konstantin Shvachko, Hairong Kuang, Sanjay Radia, and Robert Chansler. IEEE MSST](http://dx.doi.org/10.1109/MSST.2010.5496972) 
// MAGIC * Apache Spark Core Papers
// MAGIC   * 2010: [Spark: Cluster Computing with Working Sets, Matei Zaharia, Mosharaf Chowdhury, Michael J. Franklin, Scott Shenker, Ion Stoica. USENIX HotCloud](http://people.csail.mit.edu/matei/papers/2010/hotcloud_spark.pdf).
// MAGIC   * 2012: **READ**: [Resilient Distributed Datasets: A Fault-Tolerant Abstraction for In-Memory Cluster Computing, Matei Zaharia, Mosharaf Chowdhury, Tathagata Das, Ankur Dave, Justin Ma, Murphy McCauley, Michael J. Franklin, Scott Shenker and Ion Stoica. NSDI](http://usenix.org/system/files/conference/nsdi12/nsdi12-final138.pdf)
// MAGIC   * 2016: [Apache Spark: A Unified Engine for Big Data Processing](https://cacm.acm.org/magazines/2016/11/209116-apache-spark/fulltext) By Matei Zaharia, Reynold S. Xin, Patrick Wendell, Tathagata Das, Michael Armbrust, Ankur Dave, Xiangrui Meng, Josh Rosen, Shivaram Venkataraman, Michael J. Franklin, Ali Ghodsi, Joseph Gonzalez, Scott Shenker, Ion Stoica , Communications of the ACM, Vol. 59 No. 11, Pages 56-65, 10.1145/2934664
// MAGIC   
// MAGIC   ![brief history of functional programming and big data by SparkCamp](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_BriefHistoryFuncProgBigData700x.png) 
// MAGIC   
// MAGIC * Here are some directions the creators of Apache Spark at Berkeley and Stanford are currently (2018) taking:
// MAGIC   * [Stanford's Dawn Lab](http://dawn.cs.stanford.edu/)
// MAGIC   * [Berkeley's RISE lab](https://rise.cs.berkeley.edu/)
// MAGIC 
// MAGIC * Listen to [The state of machine learning in Apache Spark, The O’Reilly Data Show Podcast: Ion Stoica and Matei Zaharia explore the rich ecosystem of analytic tools around Apache Spark. By Ben Lorica September 14, 2017](https://www.oreilly.com/ideas/the-state-of-machine-learning-in-apache-spark) for staying up to date.

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 55 minutes
// MAGIC 55 out of 90+10 minutes.
// MAGIC 
// MAGIC We have come to the end of this section.
// MAGIC 
// MAGIC **Next let us get everyone to login to databricks** to get our hands dirty with some Spark code! 
// MAGIC 
// MAGIC 10-15 minutes.
// MAGIC Then break for 5.
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # To Stay Connected to Changes in Spark
// MAGIC 
// MAGIC Subscribe to YouTube Channels:
// MAGIC 
// MAGIC * [https://www.youtube.com/user/TheApacheSpark](https://www.youtube.com/user/TheApacheSpark)
// MAGIC * [databricks product-focused channel](https://www.youtube.com/channel/UC3q8O3Bh2Le8Rj1-Q-_UUbA)
// MAGIC 
// MAGIC ## EXTRA: For a historical insight see excerpts from an interview with Ion Stoica
// MAGIC 
// MAGIC #### Beginnings of Apache Spark and Databricks (academia-industry roots)
// MAGIC [![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/watch?v=bHH8sG-F9tg&rel=0&autoplay=1&modestbranding=1&start=231&end=365)
// MAGIC 
// MAGIC #### Advantages of Apache Spark: A Unified System for Batch, Stream, Interactive / Ad Hoc or Graph Processing
// MAGIC [![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/watch?v=bHH8sG-F9tg&rel=0&autoplay=1&modestbranding=1&start=458&end=726)
// MAGIC 
// MAGIC #### Main Goal of Databricks Cloud: To Make Big Data Easy
// MAGIC [![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/watch?v=bHH8sG-F9tg&rel=0&autoplay=1&modestbranding=1&start=890&end=985)
// MAGIC 
// MAGIC ***
// MAGIC ***