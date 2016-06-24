// Databricks notebook source exported at Fri, 24 Jun 2016 01:27:57 UTC
// MAGIC %md
// MAGIC # Scalable Data Science from Middle Earth
// MAGIC 
// MAGIC 2016 Paul Brouwers, Raazesh Sainudiin and Sivanand Siavaram

// COMMAND ----------

// MAGIC %md
// MAGIC * How the Course Actualized
// MAGIC * Course Plan
// MAGIC * Logistics

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## How the Course Actualized?
// MAGIC 
// MAGIC by Raazesh Sainudiin
// MAGIC 
// MAGIC **How did I get into scalable-data-science?**:
// MAGIC I spent the northern fall of 2014 on Sabbatical at the [Department of Mathematics, Malott Hall, Cornell University, Ithaca, NY USA 14853](https://www.math.cornell.edu/~raazesh/). 
// MAGIC During this time I attended weekly seminars in Computer Science, Machine Learning, Probability and Statistics. 
// MAGIC It become quite clear that computational methods that can scalably handle massive volumes of data will be essential for making decisions in all branches of science, engineering and technology.    
// MAGIC Coincidentally, I was offered a job as 'Intelligent Systems Specialist' in the R&D Group of Wynyards Group, a nascent company that buils security-related software tools, in Christchurch New Zealand for 2015.  
// MAGIC Given that this role was all about integrating data from multiple sources on a massive scale, I was excited to extend my leave from academia and kindly granted a year-long leave of absence (without pay) from the School of Mathematics and Statistics, College of Engineering, University of Canterbury -- my home institution. 
// MAGIC Both my Head of School and the pro-Vice Chancellor of the Engineering College kindly encouraged me to get industrial research experience. 
// MAGIC 
// MAGIC **First half of 2015 in industrial R&D**:
// MAGIC Learning Apache Spark was a steep climb initially. We were setting up our own hadoop clusters, spark clusters, etc from BASH shell on VMS provisioned to our team.  
// MAGIC We learnt a lot about the nuts and bolts of creating, managing and running jobs on the cluster and worked through the Learning Spark book in spark-shell.
// MAGIC 
// MAGIC From a data scientist's point of view, in terms of exploring data and gaining more intuition to improve the models, this approach using spark-shell was not only time-consuming but became quite dificult and somewhat intimidating for our data scientists, who are generally more mathematically prepared but more comfortable with graphical user interfaces for scientific computing such as MATLAB or other interactive notebook-based interfaces such as Mathematica or SageMath. 
// MAGIC 
// MAGIC Furthermore, algorithms developed in a single machine setting did not scale well. 
// MAGIC Therefore the usual paradigm of software developers taking the prototyped algorithms from environments like MATLAB (a directory of `.m` files) or Ipython/Jupyter notebooks written by data scientists just failed for massive data problems.  
// MAGIC It quickly became clear that one needed to not only prototype but also develop in the same distributed computing environment from the start. 
// MAGIC This is because arithmetic itself for matrix linear algebra and vertex programming blocks in graph algorithms had to be conceived, experimented and implemented in a distributed setting throughout the whole R&D process.
// MAGIC 
// MAGIC **Second half of 2015 in industrial R&D**:
// MAGIC During the second half of 2015 some of us started using the databricks shard for prototyping quickly. The ease with which one could ETL (extract-transform-load), interactively explore the data, model, tune and predict became apparent.  
// MAGIC Moreover, the presentation to the research team could also be done in the same notebok with in-place code and `%md` or markdown cells with the results of images or just outputs of the `display` command.  
// MAGIC The easily usable notebook interface in databricks allowed our data scientists to go from idea to a presentation in a few days as opposed to a few weeks in the spark-shell setup we were using in the first half of the year.  
// MAGIC Getting help from data engineers who were just siting a few isles away was nearly instantaneous due to the ease with which our data scinetists can chat and ask for help from our data engineers (who were holding the hotter pipes above the devOps layer).  
// MAGIC These collaborations and in-place and real-time syntactic asistance from data engineers allowed data scientists to significantly minimise the time needed to present a *minimum viable scalable prototype* to the the managing teams.
// MAGIC 
// MAGIC As the company's R&D team grew and the more experienced scientists left us to pursue other irresistabe career oportunities, Roger Jarquin, our CTO, envisioned that we needed to create a streaming model to not only train honours students from computer science, statistics and mathematics at the University of Canterbury but also retrain the Spark-inexperienced data scientists in Christchurch's data and IT industry.  
// MAGIC Co-incidentally, this was in-sync with my decision to return back to academia in order to enjoy the fredom of being able to do any research I felt like communicating mathematically to a non-empty set of human beings!
// MAGIC 
// MAGIC With the professional development arranged by Roger Jarquin, CTO, R&D Wynyard Group for their data scientists and the partial academic scholarships arranged by Professor Jennifer Brow, Head of my School of Mathematics adn Statistics, we finally had the minimum number of students needed to introduce our new honours course: [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) in southern fall of 2016 (starting February 2016).

// COMMAND ----------

// MAGIC %md
// MAGIC ## Course Plan 
// MAGIC 
// MAGIC by Raazesh Sainudiin and Sivanand Sivaram
// MAGIC 
// MAGIC We had a very mixed audience for the course: Data scientists from industry, data engineers familiar with other big data systems, research students in statistics and digital humanities, statisticians, machine learning experts, software engineers, software architects, computer science grad students and software testers. 
// MAGIC The audience became even more diverse when we opened up the class and made it a [Christchurch Spark Meetup](http://www.meetup.com/Christchurch-Apache-Spark-Meetup/) from February to June of 2016. 
// MAGIC The mixed audience gave us a unique challenge and we had to come up with something of interest to the whole group.  
// MAGIC Moreover, the chosen topics needed to satisfy the data-integrative needs of the sponsoring company's R&D team. 
// MAGIC Thus, we were requested to introduce NLP (natural language processing) and deep learning in the same course! 
// MAGIC We also had several academics and postgraduate students at our University (observing students) who wanted to be introduced to a wide set of topics ranging from twitter analytics, linear regression, multi-class classification, decision trees, to distributed graph algorithms like page-rank.
// MAGIC 
// MAGIC To address this in our course plan we decided to focus on solutions, use-cases and applications of Spark. 
// MAGIC In each week's meetup (lab-lecture) we decided to take a use-case and solve a problem. 
// MAGIC It is easier to relate to problems and see how technology enables us to solve a problem that was difficult to solve earlier (without Spark / big data). 
// MAGIC Depending on the use-case, we had to have introduced Spark topics that were necessary to solve that problem. 
// MAGIC Once we had as set of use-cases driven by the needs of the sponsoring industy and the keenest academics (domain experts), we mapped out the pre-requisite topics needed for each use-case.  
// MAGIC Quickly there was a lot of overlap of the pre-requisites and we merely arranged the most intersecting pre-requisites hierarchically to obtain the contents for the first 2-3 weeks.  
// MAGIC Thus, our goal was to introduce and talk about a variety of big data problems and cover Spark concepts in the process and make sure that we cover enough material to help prepare students for Spark certification. 
// MAGIC 
// MAGIC Our approach was breadth-first, we wanted to cover a whole bunch of topics in just sufficient detail so that students get to know the landscape around Spark. 
// MAGIC We wanted to give our students the ability to deal with many typical types of data and thereby giving the widest possible set of anchors and pointers into Apache Spark. 
// MAGIC This breadth-first approach with sufficient pointers and in-place wikipedia embeds that allowed one to dive as deep as one wished automatically prepared the student to work with a larger class of domain experts or for cross-domain data fusion tasks that is expected to become routine in data science and data engineering.  
// MAGIC On most worksheets, we have a number of references to further material that students can go to in case they need more details. 
// MAGIC We introduced RDDs and moved quickly to Spark SQL and DataFrames. 
// MAGIC We spent a bit of time on Machine learning and Spark's ML pipelines because this course is after all called *Scalable data science!*. 
// MAGIC In particular, we spent a bit of time around ETL, data exploration, data visualization and hyperparameter tuning because as a data engineer, you spend most of the time ETLing and fusing data and as a data scientist, you spend time exploring and visualising data and tuning hyper-parameters. We wanted to train better data scientists and data engineers in the process.
// MAGIC 
// MAGIC A lot of content for the course is borrowed from what others have done. 
// MAGIC This includes resources from Spark documentation, Spark guide, Spark source code, blogs, books (Learning Spark and Advanced Analytics with Spark), youtube videos from Spark summit, Advanced Dev Ops training with Spark that Siva attended at the 2015 Spark San Francisco summit and the bigData edX certification Raaz completed in 2015 by taking [BerkeleyX/CS100-1x, Introduction to Big Data Using Apache Spark by Anthony A Joseph, Chancellor's Professor, UC Berkeley](https://www.edx.org/course/introduction-big-data-apache-spark-uc-berkeleyx-cs100-1x) and [BerkeleyX/CS190-1x, Scalable Machine Learning by Ameet Talwalkar, Ass. Prof., UC Los Angeles](https://www.edx.org/course/scalable-machine-learning-uc-berkeleyx-cs190-1x).
// MAGIC We thought that we can add additional value to these 2015 edX courses by making our course to be an *expanded scala version* of various parts of the above two courses which were done in python/pySpark.  
// MAGIC We also decided to present our course content in the convenient notebook interface of databricks; thanks to the [databricks Academic Partners](https://databricks.com/)
// MAGIC and [Amazon Web Services Educate](https://www.awseducate.com/microsite/CommunitiesEngageHome) grants, but with a strong emphasis on *individualized course project* as opposed to completing labs that test sytactic skills in auto-graders.  This is a nice aspect of the Oxbridge honours system inherited by Canterbury. 
// MAGIC 
// MAGIC 
// MAGIC This above paragraphs discuss the expectations of our students from industry and acdemia and also reflects the way we ourselves learnt Spark (and were continuing to learn Spark as the course progressed). 
// MAGIC Both Raaz and Siva are self-taught and the way we learnt Spark is through finding material online and offline, collaborating with each other, teaching each other and mixing learnings from multiple sources. 
// MAGIC We attempt to present a more organized version of how we learnt Spark and hope to enable others to get an introduction to and take a deep-dive into Spark where they deem necessary. 
// MAGIC In most cases, we upgraded the code to be Spark 1.6 compatible and converted the code to Scala which was the primary language we used in our class. 
// MAGIC The reason for Scala was as Raaz explains in one of the earliest videos - to be as close as possible to the languge in which Spark itself is written. 
// MAGIC 
// MAGIC We also introduced third-party libraries on top of Spark (like TensorFlow and Magellen). 
// MAGIC This is because on real production systems, Spark is going to be one component of the data processing pipeline and will need to be integrated with other libraries. 
// MAGIC 
// MAGIC With the imminent release of Spark 2.0, when preparing for GraphX, we used the beta GraphFrames library so that the material remains relavent at least for a couple of more months.
// MAGIC 
// MAGIC For future versions of this course, we would like to expand on integration and inter-operation of Spark with its related ecosystem components, for example, Have a queue (Kafka) for ingestion and save distributed data onto a store (Cassandra). 
// MAGIC These topics can help us get deeper into Spark streaming and Datasource API and Catalyst. 
// MAGIC Another possibility which would be of interest to engineers is to go deeper into Spark architecture - its roots from Mesos, how RDDs work, DAG scheduler, Block manager, Catalyst and Tungsten optimizers, Spark memory management and contents from [Jacek Laskowski's gitbook](https://www.gitbook.com/book/jaceklaskowski/mastering-apache-spark/details). 
// MAGIC A different possibility which can interest statisticans and mathematicians is to build a databricksified scalarific variant on the [Distributed Algorithms and Optimization course by Reza Zadeh, Ass. Prof., Institute for Computational and Mathematical Engineering, Stanford University](http://stanford.edu/~rezab/dao/).

// COMMAND ----------

// MAGIC %md
// MAGIC ## Logistics
// MAGIC by Paul Brouwers and Raazesh Sainudiin
// MAGIC 
// MAGIC Froma system administrator's point of view we had to solve the following problems:
// MAGIC 
// MAGIC * AWS Educate 
// MAGIC   * AWS Educate credit pooling
// MAGIC   * Setting some safe-guards in AWS
// MAGIC   * AWS costs 
// MAGIC   * ...
// MAGIC * Databricks Academic Partners Program
// MAGIC   * Databricks Shard Settings
// MAGIC   
// MAGIC ### AWS Educate
// MAGIC 
// MAGIC #### AWS Educate credit pooling
// MAGIC 
// MAGIC We had the following 21 steps each student of the course could follow to share their AWS credit codes with us. 
// MAGIC These AWS credits were pooled into the ....
// MAGIC 
// MAGIC #### Setting some safe-guards in AWS
// MAGIC 
// MAGIC To prevent a student from accidentally launching hundreds of nodes we set some limits in the AWS account. Details...
// MAGIC 
// MAGIC #### AWS Costs
// MAGIC 
// MAGIC We typically launched a 4 node cluster 24/7 on spot-prices.  This cluster only costed a maximum of 316 USD/month (recheck costs).
// MAGIC Thus we only needed credits from about 3 students per month to pay for this. Since several faculty sat in on the course and some of them also donated their credit we were able to provide the 24/7 service for the duration of the course.
// MAGIC   
// MAGIC During lab/lecture times we launched two larger clusters (details here on number of nodes, costs for the 2 hours, etc here) on-demand to ensure uninterrupted course intreactions.
// MAGIC 
// MAGIC For some of the student projects we needed to have nodes with upto 30GB of RAM per node and be able to scale up to several tens of nodes.  
// MAGIC The databricks Educate team gave us a shard for research projects with cluster-creation capabilities. We had to be very careful when using these 
// MAGIC 
// MAGIC charges if you misjudge costs - watchout! 
// MAGIC Some details here... 
// MAGIC It paid to look at historical spot-pricing time series to determine the appropriate zone.
// MAGIC 
// MAGIC ### Databricks Academic Partners Program
// MAGIC 
// MAGIC #### Databricks Shard Settings
// MAGIC 
// MAGIC The shard had to be 'Access Control Enabled' so as to allow students to choose who they want to collaborate with.
// MAGIC 
// MAGIC We did not allow students the ability to launch their own clusters. This was done to allow a simpler management model. Sometimes students would email if the 24/7 cluster went down due to spot-price exceeding the threshold. Then we could simply restart it.
// MAGIC 
// MAGIC ## Community edition
// MAGIC 
// MAGIC With support from daatbricks we got community edition account for each student towards the second half of the course and encouraged student to work in CE. 
// MAGIC This was the `exit strategy` so the student will have a central place to continue learning and keeping up with Spark's rapid evolutions.
// MAGIC We will be releasing the contants of the course as a `.dbc` archive that can be uploaded into anyone's databricks CE to learn by taking advantage of our course contents and notebooks.