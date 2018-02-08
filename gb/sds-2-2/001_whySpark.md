[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Why Apache Spark?
=================

-   [Apache Spark: A Unified Engine for Big Data Processing](https://cacm.acm.org/magazines/2016/11/209116-apache-spark/fulltext) By Matei Zaharia, Reynold S. Xin, Patrick Wendell, Tathagata Das, Michael Armbrust, Ankur Dave, Xiangrui Meng, Josh Rosen, Shivaram Venkataraman, Michael J. Franklin, Ali Ghodsi, Joseph Gonzalez, Scott Shenker, Ion Stoica Communications of the ACM, Vol. 59 No. 11, Pages 56-65 10.1145/2934664

[![Apache Spark ACM Video](https://i.vimeocdn.com/video/597494216_640.jpg)](https://player.vimeo.com/video/185645796)

Right-click the above image-link, open in a new tab and watch the video (4 minutes) or read about it in the Communications of the ACM in the frame below or from the link above.

### The big data problem, Hardware, distributing work, handling failed and slow machines

#### by Anthony Joseph in BerkeleyX/CS100.1x

-   **(watch now 1:48)**: The Big Data Problem
-   [![The Big Data Problem by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/0JdJe5iehhw/0.jpg)](https://www.youtube.com/watch?v=0JdJe5iehhw&modestbranding=1&start=1)
-   **(watch now 1:43)**: Hardware for Big Data
-   [![Hardware for Big Data by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/KmIIMdsXGzc/0.jpg)](https://www.youtube.com/watch?v=KmIIMdsXGzc&rel=0&autoplay=1&modestbranding=1&start=1)
-   **(watch now 1:17)**: How to distribute work across a cluster of commodity machines?
-   [![How to distribute work across a cluster of commodity machines? by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/Euk1v3VtNcM/0.jpg)](https://www.youtube.com/watch?v=Euk1v3VtNcM&rel=0&autoplay=1&modestbranding=1&start=1)
-   **(watch now 0:36)**: How to deal with failures or slow machines?
-   [![How to deal with failures or slow machines? by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/NaHNsPEK3KA/0.jpg)](https://www.youtube.com/watch?v=NaHNsPEK3KA&rel=0&autoplay=1&modestbranding=1&start=1)

MapReduce and Apache Spark.
---------------------------

#### by Anthony Joseph in BerkeleyX/CS100.1x

-   **(watch now 1:48)**: Map Reduce (is bounded by Disk I/O)
-   [![The Big Data Problem by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/NqG_hYAKjYk/0.jpg)](https://www.youtube.com/watch?v=NqG_hYAKjYk&rel=0&autoplay=1&modestbranding=1&start=1)
-   **(watch now 2:49)**: Apache Spark (uses Memory instead of Disk)
-   [![Apache Spark by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/vat5Jki1lbI/0.jpg)](https://www.youtube.com/watch?v=vat5Jki1lbI&rel=0&autoplay=1&modestbranding=1&start=1)
-   **(watch now 3:00)**: Spark Versus MapReduce
-   [![Spark Versus MapReduce by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/Ddq3Gua2QFg/0.jpg)](https://www.youtube.com/watch?v=Ddq3Gua2QFg&rel=0&autoplay=1&modestbranding=1&start=1)
-   SUMMARY
    -   uses memory instead of disk alone and is thus fater than Hadoop MapReduce
    -   resilience abstraction is by RDD (resilient distributed dataset)
    -   RDDs can be recovered upon failures from their *lineage graphs*, the recipes to make them starting from raw data
    -   Spark supports a lot more than MapReduce, including streaming, interactive in-memory querying, etc.
    -   Spark demonstrated an unprecedented sort of 1 petabyte (1,000 terabytes) worth of data in 234 minutes running on 190 Amazon EC2 instances (in 2015).
    -   Spark expertise corresponds to the highest Median Salary in the US (~ 150K)

Key Papers
----------

-   Key Historical Milestones
-   1956-1979: [Stanford, MIT, CMU, and other universities develop set/list operations in LISP, Prolog, and other languages for parallel processing](http://www-formal.stanford.edu/jmc/history/lisp/lisp.html)
-   2004: **READ**: [Google's MapReduce: Simplified Data Processing on Large Clusters, by Jeffrey Dean and Sanjay Ghemawat](http://research.google.com/archive/mapreduce.html)
-   2006: [Yahoo!'s Apache Hadoop, originating from the Yahoo!’s Nutch Project, Doug Cutting](http://developer.yahoo.com/hadoop/)
-   2009: [Cloud computing with Amazon Web Services Elastic MapReduce](http://aws.amazon.com/elasticmapreduce/), a Hadoop version modified for Amazon Elastic Cloud Computing (EC2) and Amazon Simple Storage System (S3), including support for Apache Hive and Pig.
-   2010: **READ**: [The Hadoop Distributed File System, by Konstantin Shvachko, Hairong Kuang, Sanjay Radia, and Robert Chansler. IEEE MSST](http://dx.doi.org/10.1109/MSST.2010.5496972)
-   Apache Spark Core Papers
    </h1>
-   2010: [Spark: Cluster Computing with Working Sets, Matei Zaharia, Mosharaf Chowdhury, Michael J. Franklin, Scott Shenker, Ion Stoica. USENIX HotCloud](http://people.csail.mit.edu/matei/papers/2010/hotcloud_spark.pdf).
-   2012: **READ**: [Resilient Distributed Datasets: A Fault-Tolerant Abstraction for In-Memory Cluster Computing, Matei Zaharia, Mosharaf Chowdhury, Tathagata Das, Ankur Dave, Justin Ma, Murphy McCauley, Michael J. Franklin, Scott Shenker and Ion Stoica. NSDI](http://usenix.org/system/files/conference/nsdi12/nsdi12-final138.pdf)

![brief history of functional programming and big data by SparkCamp](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_BriefHistoryFuncProgBigData700x.png)

------------------------------------------------------------------------

------------------------------------------------------------------------

55 minutes 55 out of 90+10 minutes.

We have come to the end of this section.

**Next let us get everyone to login to databricks** to get our hands dirty with some Spark code!

10-15 minutes. Then break for 5. *** ***

To Stay Connected to Changes in Spark
=====================================

Subscribe to YouTube Channels:

-   <https://www.youtube.com/user/TheApacheSpark>
-   [databricks product-focused channel](https://www.youtube.com/channel/UC3q8O3Bh2Le8Rj1-Q-_UUbA)

EXTRA: For a historical insight see excerpts from an interview with Ion Stoica
------------------------------------------------------------------------------

#### Beginnings of Apache Spark and Databricks (academia-industry roots)

[![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/watch?v=bHH8sG-F9tg&rel=0&autoplay=1&modestbranding=1&start=231&end=365)

#### Advantages of Apache Spark: A Unified System for Batch, Stream, Interactive / Ad Hoc or Graph Processing

[![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/watch?v=bHH8sG-F9tg&rel=0&autoplay=1&modestbranding=1&start=458&end=726)

#### Main Goal of Databricks Cloud: To Make Big Data Easy

[![Ion Stoica on Starting Spark and DataBricks](http://img.youtube.com/vi/bHH8sG-F9tg/0.jpg)](https://www.youtube.com/watch?v=bHH8sG-F9tg&rel=0&autoplay=1&modestbranding=1&start=890&end=985)

------------------------------------------------------------------------

------------------------------------------------------------------------

Some BDAS History behind Apache Spark
=====================================

The Berkeley Data Analytics Stack is BDAS
-----------------------------------------

### Spark is a sub-stack of BDAS

**Source:** \* [Ion Stoica's State of Spark Union AmpCamp 6, Nov 2015](https://www.slideshare.net/secret/9ON8EEAlVKP3Sl) \* [Machine learning: Trends, perspectives, and prospects, M. I. Jordan, T. M. Mitchell, Science 17 Jul 2015: Vol. 349, Issue 6245, pp. 255-260, DOI: 10.1126/science.aaa8415](http://science.sciencemag.org/content/349/6245/255.full-text.pdf+html)

### BDAS State of The Union Talk by Ion Stoica, AMP Camp 6, Nov 2015

The followign talk outlines the motivation and insights behind BDAS' research approach and how they address the cross-disciplinary nature of Big Data challenges and current work. \* **watch later (5 mins.):**

[![Ion Stoica on State of Spark Union AmpCamp6](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/images/week1/stateofthebdasunionAmpCamp6Stoica-5_YTCover.png)](https://www.youtube.com/watch?v=s7kj9XzRBQk&start=91&end=386)

key points
----------

-   started in 2011 with strong public-private funding
-   Defense Advanced Research Projects Agency
-   Lawrance Berkeley Laboratory
-   National Science Foundation
-   Amazon Web Services
-   Google
-   SAP
-   The Berkeley AMPLab is creating a new approach to data analytics to seamlessly integrate the three main resources available for making sense of data at scale:
-   Algorithms (machine learning and statistical techniques),
-   Machines (in the form of scalable clusters and elastic cloud computing), and
-   People (both individually as analysts and in crowds).
-   The lab is realizing its ideas through the development of a freely-available Open Source software stack called BDAS: the Berkeley Data Analytics Stack.
-   Several components of BDAS have gained significant traction in industry and elsewhere, including:
-   the Mesos cluster resource manager,
-   the Spark in-memory computation framework, a sub-stack of the BDAS stack,
-   and more...

<a href="https://cacm.acm.org/magazines/2016/11/209116-apache-spark/fulltext">https://cacm.acm.org/magazines/2016/11/209116-apache-spark/fulltext</a>
