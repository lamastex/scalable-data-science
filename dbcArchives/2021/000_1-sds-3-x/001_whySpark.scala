// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

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
// MAGIC 
// MAGIC **Key Insights from [Apache Spark: A Unified Engine for Big Data Processing](https://cacm.acm.org/magazines/2016/11/209116-apache-spark/fulltext) **
// MAGIC 
// MAGIC - A simple programming model can capture streaming, batch, and interactive workloads and enable new applications that combine them. 
// MAGIC - Apache Spark applications range from finance to scientific data processing and combine libraries for SQL, machine learning, and graphs. 
// MAGIC - In six years, Apache Spark has  grown to 1,000 contributors and thousands of deployments. 
// MAGIC 
// MAGIC ![Key Insights](https://dl.acm.org/cms/attachment/6f54b222-fe96-497a-8bfc-0e6ea250b05d/ins01.gif)

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
// MAGIC 
// MAGIC Spark 3.0 is the latest version now (20200918) and it should be seen as the latest step in the evolution of tools in the big data ecosystem as summarized in [https://towardsdatascience.com/what-is-big-data-understanding-the-history-32078f3b53ce](https://towardsdatascience.com/what-is-big-data-understanding-the-history-32078f3b53ce):
// MAGIC 
// MAGIC ![Spark in context](https://miro.medium.com/max/1200/1*0bWwqlOfjRqoUDqrH62GbQ.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ## The big data problem
// MAGIC 
// MAGIC **Hardware, distributing work, handling failed and slow machines**
// MAGIC 
// MAGIC The following content was created by Anthony Joseph and used in BerkeleyX/CS100.1x from 2015.
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
// MAGIC 
// MAGIC The following content was created by Anthony Joseph and used in BerkeleyX/CS100.1x from 2015.
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
// MAGIC   * 2006: [Yahoo!'s Apache Hadoop, originating from the Yahoo!’s Nutch Project, Doug Cutting - wikipedia](https://en.wikipedia.org/wiki/Apache_Hadoop)
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
// MAGIC * More research papers on Spark are available from here:
// MAGIC   - [https://databricks.com/resources?_sft_resource_type=research-papers](https://databricks.com/resources?_sft_resource_type=research-papers)

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC **Next let us get everyone to login to databricks** (or another Spark platform) to get our hands dirty with some Spark code! 
// MAGIC 
// MAGIC ***
// MAGIC ***