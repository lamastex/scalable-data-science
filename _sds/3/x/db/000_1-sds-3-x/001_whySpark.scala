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
// MAGIC [![Apache Spark ACM Video](https://i.vimeocdn.com/video/597494216-6f494f2fb4efb90efe6bb3206f3892bec6b202352951512dbde44c976549dd87-d.jpg?mw=240&q=255)](https://player.vimeo.com/video/185645796)
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
// MAGIC 
// MAGIC ## Alternatives to Apache Spark
// MAGIC 
// MAGIC There are several alternatives to Apache Spark, but none of them have the penetration and community of Spark as of 2021. 
// MAGIC 
// MAGIC For real-time streaming operations [Apache Flink](https://flink.apache.org/) is competitive. See [Apache Flink vs Spark – Will one overtake the other?](https://www.projectpro.io/article/apache-flink-vs-spark-will-one-overtake-the-other/282#toc-7) for a July 2021 comparison. Most scalable data science and engineering problems faced by several major industries in Sweden today are routinely solved using tools in the ecosystem around Apache Spark. Therefore, we will focus on Apache Spark here which still holds [the world record for 10TB or 10,000 GB sort](http://www.tpc.org/tpcds/results/tpcds_perf_results5.asp?spm=a2c65.11461447.0.0.626f184fy7PwOU&resulttype=all) by [Alibaba cloud](https://www.alibabacloud.com/blog/alibaba-cloud-e-mapreduce-sets-world-record-again-on-tpc-ds-benchmark_596195) in 06/17/2020.

// COMMAND ----------

// MAGIC %md
// MAGIC ## The big data problem
// MAGIC 
// MAGIC **Hardware, distributing work, handling failed and slow machines**
// MAGIC 
// MAGIC Let us recall and appreciate the following:
// MAGIC 
// MAGIC * The Big Data Problem
// MAGIC   * Many routine problems today involve dealing with "big data", operationally, this is a dataset that is larger than a few TBs and thus won't fit into a single commodity computer like a powerful desktop or laptop computer.
// MAGIC   
// MAGIC *  Hardware for Big Data
// MAGIC   * The best single commodity computer can not handle big data as it has limited hard-disk and memory
// MAGIC   * Thus, we need to break the data up into lots of commodity computers that are networked together via cables to communicate instructions and data between them - this can be thought of as *a cloud* 
// MAGIC * How to distribute work across a cluster of commodity machines?
// MAGIC   * We need a software-level framework for this.
// MAGIC * How to deal with failures or slow machines?
// MAGIC   * We also need a software-level framework for this.
// MAGIC   

// COMMAND ----------

// MAGIC %md
// MAGIC ## Key Papers
// MAGIC 
// MAGIC * Key Historical Milestones
// MAGIC   * 1956-1979: [Stanford, MIT, CMU, and other universities develop set/list operations in LISP, Prolog, and other languages for parallel processing](https://en.wikipedia.org/wiki/Lisp_(programming_language))
// MAGIC   * 2004: **READ**: [Google's MapReduce: Simplified Data Processing on Large Clusters, by Jeffrey Dean and Sanjay Ghemawat](https://research.google/pubs/pub62/)
// MAGIC   * 2006: [Yahoo!'s Apache Hadoop, originating from the Yahoo!’s Nutch Project, Doug Cutting - wikipedia](https://en.wikipedia.org/wiki/Apache_Hadoop)
// MAGIC   * 2009: [Cloud computing with Amazon Web Services Elastic MapReduce](https://aws.amazon.com/emr/), a Hadoop version modified for Amazon Elastic Cloud Computing (EC2) and Amazon Simple Storage System (S3), including support for Apache Hive and Pig.
// MAGIC   * 2010: **READ**: [The Hadoop Distributed File System, by Konstantin Shvachko, Hairong Kuang, Sanjay Radia, and Robert Chansler. IEEE MSST](https://dx.doi.org/10.1109/MSST.2010.5496972) 
// MAGIC * Apache Spark Core Papers
// MAGIC   * 2012: **READ**: [Resilient Distributed Datasets: A Fault-Tolerant Abstraction for In-Memory Cluster Computing, Matei Zaharia, Mosharaf Chowdhury, Tathagata Das, Ankur Dave, Justin Ma, Murphy McCauley, Michael J. Franklin, Scott Shenker and Ion Stoica. NSDI](https://www.usenix.org/system/files/conference/nsdi12/nsdi12-final138.pdf)
// MAGIC   * 2016: [Apache Spark: A Unified Engine for Big Data Processing](https://cacm.acm.org/magazines/2016/11/209116-apache-spark/fulltext) By Matei Zaharia, Reynold S. Xin, Patrick Wendell, Tathagata Das, Michael Armbrust, Ankur Dave, Xiangrui Meng, Josh Rosen, Shivaram Venkataraman, Michael J. Franklin, Ali Ghodsi, Joseph Gonzalez, Scott Shenker, Ion Stoica , Communications of the ACM, Vol. 59 No. 11, Pages 56-65, 10.1145/2934664
// MAGIC   
// MAGIC   ![brief history of functional programming and big data by SparkCamp](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/week1/dbTrImg_BriefHistoryFuncProgBigData700x.png) 
// MAGIC   
// MAGIC * A lot has happened since 2014 to improve efficiency of Spark and embed more into the big data ecosystem
// MAGIC   - See [Introducing Apache Spark 3.0 | Matei Zaharia and Brooke Wenig | Keynote Spark + AI Summit 2020](https://www.youtube.com/watch?v=p4PkA2huzVc).
// MAGIC 
// MAGIC * More research papers on Spark are available from here:
// MAGIC   - [https://databricks.com/resources?_sft_resource_type=research-papers](https://databricks.com/resources?_sft_resource_type=research-papers)

// COMMAND ----------

// MAGIC %md
// MAGIC ## MapReduce and Apache Spark.
// MAGIC 
// MAGIC MapReduce as we will see shortly in action is a framework for distributed fault-tolerant computing over a fault-tolerant distributed file-system, such as Google File System or open-source Hadoop for storage. 
// MAGIC 
// MAGIC * Unfortunately, Map Reduce is bounded by Disk I/O and can be slow
// MAGIC   * especially when doing a sequence of MapReduce operations requirinr multiple Disk I/O operations 
// MAGIC * Apache Spark can use Memory instead of Disk to speed-up MapReduce Operations
// MAGIC   * Spark Versus MapReduce - the speed-up is orders of magnitude faster
// MAGIC * SUMMARY
// MAGIC     * Spark uses memory instead of disk alone and is thus fater than Hadoop MapReduce
// MAGIC     * Spark's resilience abstraction is by RDD (resilient distributed dataset)
// MAGIC     * RDDs can be recovered upon failures from their *lineage graphs*, the recipes to make them starting from raw data
// MAGIC     * Spark supports a lot more than MapReduce, including streaming, interactive in-memory querying, etc.
// MAGIC     * Spark demonstrated an unprecedented sort of 1 petabyte (1,000 terabytes) worth of data in 234 minutes running on 190 Amazon EC2 instances (in 2015).
// MAGIC     * Spark expertise corresponds to the highest Median Salary in the US (~ 150K)

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC **Next let us get everyone to login to databricks** (or another Spark platform) to get our hands dirty with some Spark code! 
// MAGIC 
// MAGIC ***
// MAGIC ***
