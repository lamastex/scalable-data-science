// Databricks notebook source exported at Thu, 7 Jul 2016 05:49:25 UTC

# Scalable Data Science from Middle Earth

2016 Paul Brouwers, Raazesh Sainudiin and Sivanand Sivaram





* How the Course Actualized?
* Course Plan
* Logistics
* In News






## How the Course Actualized?

by Raazesh Sainudiin, 25th of June 2016, Christchurch, Aotearoa New Zealand.

**How did I get into scalable-data-science?**:
I spent the northern fall of 2014 on Sabbatical at the [Department of Mathematics, Malott Hall, Cornell University, Ithaca, NY USA 14853](https://www.math.cornell.edu/~raazesh/). 
During this time I attended weekly seminars in Computer Science, Machine Learning, Probability and Statistics. 
It become quite clear that computational methods that can scalably handle massive volumes of data will be essential for making decisions in all branches of science, engineering and technology.    
Coincidentally, I was offered a job as 'Intelligent Systems Specialist' in the R&D division of Wynyard Group, a nascent company that builds security-related software tools, in Christchurch New Zealand for 2015.  
Given that this role was all about integrating data from multiple sources on a massive scale, I was excited to extend my leave from academia and was kindly granted a year-long leave of absence (without pay) from the School of Mathematics and Statistics, College of Engineering, University of Canterbury (my home institution). 
Both my Head of School and the Pro-Vice Chancellor of the Engineering College kindly encouraged me to get industrial research experience. 

**First half of 2015 in industrial R&D**:
Learning Apache Spark was a steep climb initially. We were setting up our own hadoop clusters, spark clusters, etc from BASH shell on virtual machines that were provisioned to our team.  We learnt a lot about the nuts and bolts of creating, managing and running jobs on the cluster and worked through the *Learning Spark : lightning-fast data analytics by Holden Karau, Andy Konwinski, Patrick Wendell, and Matei Zaharia, O'Reilly, 2015* book in spark-shell.

From a data scientist's point of view, in terms of exploring data and gaining more intuition to improve the models, this approach using spark-shell was not only time-consuming but became quite difficult and somewhat intimidating for our data scientists (including myself), who are generally more mathematically prepared but typically more comfortable with graphical user interfaces for scientific computing, either through proprietary software such as MATLAB or through notebook-based open-source platforms such as [SageMath](http://www.sagemath.org/). Furthermore, algorithms developed in a single machine setting did not scale well. 
Therefore the usual paradigm of software developers taking the prototyped algorithms from environments like MATLAB (a directory of `.m` files) or Ipython/Jupyter notebooks written by data scientists just failed for massive data problems.  

It quickly became clear that one needed to not only prototype but also develop in the same distributed computing environment from the start. This is because arithmetic itself for matrix linear algebra and vertex programming blocks in graph algorithms had to be conceived, implemented, explored, experimented and developed in a distributed setting throughout the whole R&D process!

**Second half of 2015 in industrial R&D**:
During the second half of 2015 some of us started using the databricks shard for prototyping quickly. The ease with which one could ETL (extract-transform-load), interactively explore the data, model, tune and predict became apparent.  Moreover, the presentation to the research team could also be done in the same notebook with in-place code and `%md` or markdown cells with the results of images or just outputs of the `display` command.  The easily usable notebook interface in databricks allowed our data scientists to go from idea to a presentation in a few days as opposed to a few weeks in the spark-shell setup we were using in the first half of the year.  Getting help from data engineers who were just siting a few isles away was nearly instantaneous through databricks notebooks due to the ease with which our data scientists can chat and ask for help from our data engineers (who were holding the hotter pipes above the devOps layer) at the exact cells in the notebooks.  These collaborations and in-place and real-time syntactic asistance from data engineers allowed data scientists to significantly minimize the time needed to present a *minimum viable scalable prototype* to the the managing team .

As the company's R&D team grew and the more experienced scientists left us to pursue other irresistabe career oportunities, Roger Jarquin, our CTO, envisioned that we needed to create a streaming model to not only train honours students from computer science, statistics and mathematics at the University of Canterbury but also retrain the Spark-inexperienced data scientists in Christchurch's data and IT industry.  Co-incidentally, this was in-sync with my decision to return back to academia in order to enjoy the freedom of being able to do any research I felt like communicating mathematically to a non-empty set of human beings besides myself!

With the professional development arranged by Roger Jarquin (CTO, R&D Wynyard Group) for their data scientists and the partial academic scholarships arranged by Professor Jennifer Brown, Head of my School of Mathematics and Statistics, we finally had the minimum number of students needed from a financial perspective to introduce our new honours course: [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) in southern fall of 2016 (starting February 2016).

Around the same time Sivanand Sivaram was hired as a Data Engineer in Wynyard Group's R&D and we started going through the Databricks training materials from his San Francisco Spark Summit 2015 during week nights and started discussing them in more detail during our weekend mountain-hikes in the Southern Alps around Arthur's Pass. It became quite apparent that the course needed a data engineer to understand the hotter pipes under the hood of our vision for an applied and technical course in scalable data sciences.





## Course Plan 

by Raazesh Sainudiin and Sivanand Sivaram

We had a very mixed audience for the course: Data scientists from industry, data engineers familiar with other big data systems, research students in statistics and digital humanities, statisticians, machine learning experts, software engineers, software architects, computer science grad students and software testers. 
The audience became even more diverse when we opened up the class and made it a [Christchurch Spark Meetup](http://www.meetup.com/Christchurch-Apache-Spark-Meetup/) from February to June of 2016. 
The mixed audience gave us a unique challenge and we had to come up with something of interest to the whole group.  
Moreover, the chosen topics needed to satisfy the data-integrative needs of the sponsoring company's R&D team. 
Thus, we were requested to introduce NLP (natural language processing) and deep learning in the same course! 
We also had several academics and postgraduate students at our University (observing students) who wanted to be introduced to a wide set of topics ranging from twitter analytics, linear regression, multi-class classification, decision trees, to distributed graph algorithms like page-rank.

To address this in our course plan we decided to focus on solutions, use-cases and applications of Spark. 
In each week's meetup (lab-lecture) we decided to take a use-case and solve a problem. 
It is easier to relate to problems and see how technology enables us to solve a problem that was difficult to solve earlier (without Spark / big data). 
Depending on the use-case, we had to have introduced Spark topics that were necessary to solve that problem. 
Once we had as set of use-cases driven by the needs of the sponsoring industy and the keenest academics (domain experts), we mapped out the pre-requisite topics needed for each use-case.  
Quickly there was a lot of overlap of the pre-requisites and we merely arranged the most intersecting pre-requisites hierarchically to obtain the contents for the first 2-3 weeks.  
Thus, our goal was to introduce and talk about a variety of big data problems and cover Spark concepts in the process and make sure that we cover enough material to help prepare students for Spark certification. 

Our approach was breadth-first, we wanted to cover a whole bunch of topics in just sufficient detail so that students get to know the landscape around Spark. 
We wanted to give our students the ability to deal with many typical types of data and thereby giving the widest possible set of anchors and pointers into Apache Spark. 
This breadth-first approach with sufficient pointers and in-place wikipedia embeds that allowed one to dive as deep as one wished automatically prepared the student to work with a larger class of domain experts or for cross-domain data fusion tasks that is expected to become routine in data science and data engineering.  
On most worksheets, we have a number of references to further material that students can go to in case they need more details. 
We introduced RDDs and moved quickly to Spark SQL and DataFrames. 
We spent a bit of time on Machine learning and Spark's ML pipelines because this course is after all called *Scalable data science!*. 
In particular, we spent a bit of time around ETL, data exploration, data visualization and hyperparameter tuning because as a data engineer, you spend most of the time ETLing and fusing data and as a data scientist, you spend time exploring and visualising data and tuning hyper-parameters. We wanted to train better data scientists and data engineers in the process.

A lot of content for the course is borrowed from what others have done. 
This includes resources from Spark documentation, Spark guide, Spark source code, blogs, books (Learning Spark and Advanced Analytics with Spark), youtube videos from Spark summit, Advanced Dev Ops training with Spark that Siva attended at the 2015 Spark San Francisco summit and the bigData edX certification Raaz completed in 2015 by taking [BerkeleyX/CS100-1x, Introduction to Big Data Using Apache Spark by Anthony A Joseph, Chancellor's Professor, UC Berkeley](https://www.edx.org/course/introduction-big-data-apache-spark-uc-berkeleyx-cs100-1x) and [BerkeleyX/CS190-1x, Scalable Machine Learning by Ameet Talwalkar, Ass. Prof., UC Los Angeles](https://www.edx.org/course/scalable-machine-learning-uc-berkeleyx-cs190-1x).
We thought that we can add additional value to these 2015 edX courses by making our course to be an *expanded scala version* of various parts of the above two courses which were done in python/pySpark.  
We also decided to present our course content in the convenient notebook interface of databricks; thanks to the [databricks Academic Partners](https://databricks.com/)
and [Amazon Web Services Educate](https://www.awseducate.com/microsite/CommunitiesEngageHome) grants, but with a strong emphasis on *individualized course project* as opposed to completing labs that test sytactic skills in auto-graders.  This is a nice aspect of the Oxbridge honours system inherited by Canterbury. 


This above paragraphs discuss the expectations of our students from industry and acdemia and also reflects the way we ourselves learnt Spark (and were continuing to learn Spark as the course progressed). 
Both Raaz and Siva are self-taught and the way we learnt Spark is through finding material online and offline, collaborating with each other, teaching each other and mixing learnings from multiple sources. 
We attempt to present a more organized version of how we learnt Spark and hope to enable others to get an introduction to and take a deep-dive into Spark where they deem necessary. 
In most cases, we upgraded the code to be Spark 1.6 compatible and converted the code to Scala which was the primary language we used in our class. 
The reason for Scala was as Raaz explains in one of the earliest videos - to be as close as possible to the languge in which Spark itself is written. 

We also introduced third-party libraries on top of Spark (like TensorFlow and Magellen). 
This is because on real production systems, Spark is going to be one component of the data processing pipeline and will need to be integrated with other libraries. 

With the imminent release of Spark 2.0, when preparing for GraphX, we used the beta GraphFrames library so that the material remains relavent at least for a couple of more months.

For future versions of this course, we would like to expand on integration and inter-operation of Spark with its related ecosystem components, for example, Have a queue (Kafka) for ingestion and save distributed data onto a store (Cassandra). 
These topics can help us get deeper into Spark streaming and Datasource API and Catalyst. 
Another possibility which would be of interest to engineers is to go deeper into Spark architecture - its roots from Mesos, how RDDs work, DAG scheduler, Block manager, Catalyst and Tungsten optimizers, Spark memory management and contents from [Jacek Laskowski's gitbook](https://www.gitbook.com/book/jaceklaskowski/mastering-apache-spark/details). 
A different possibility which can interest statisticans and mathematicians is to build a databricksified scalarific variant on the [Distributed Algorithms and Optimization course by Reza Zadeh, Ass. Prof., Institute for Computational and Mathematical Engineering, Stanford University](http://stanford.edu/~rezab/dao/).





## Logistics
by Paul Brouwers and Raazesh Sainudiin

Froma system administrator's point of view we had to solve the following problems:

* AWS Educate 
  * AWS Educate credit pooling
  * Setting some safe-guards in AWS
  * AWS costs 
  * ...
* Databricks Academic Partners Program
  * Databricks Shard Settings
  
### AWS Educate

#### AWS Educate credit pooling

We [shared these 21 steps](https://github.com/raazesh-sainudiin/scalable-data-science/blob/master/db/xtraResources/awsEducate/sharing.md) each student of the course could follow in order to share their AWS credit codes with us. These AWS credits were pooled into the databricks' AWS account that we managed for the course.

#### Setting some safe-guards in AWS

To prevent a student from accidentally launching hundreds of nodes we set some limits in the AWS account (including maximum number of nodes that can be launched and the sizes of the nodes, etc).

#### AWS Costs

We typically launched a 4 node cluster 24/7 on spot-prices.  This cluster only costed a maximum of 316 USD/month and averaged around 100 USD/month.  Thus we only needed credits from about 3 students per month to pay for this. Since several faculty sat in on the course and two of them also donated their AWS credits we were able to provide the 24/7 service for the duration of the course.
  
During lab/lecture times we launched two larger clusters (with 9 nodes) on-demand to ensure uninterrupted course intreactions. The on-demand clusters costed 10x more per hour but was easily manageable within our budget for the few hours during and after our weekly meetings in order to handle the load from up to 30 students.

AWS will charge to your credit-card if the AWS credits you have pooled does not cover the monthly costs. There is no way to retroactively recover the charge from AWS credits pooled later.
It also paid to look at historical spot-pricing time series from various AWS availability zones to determine the currently cheapest places to launch our clusters on spot prices.

### Databricks Academic Partners Program

This course would not have happened without the remarkable support by Allison Kunz of the Databricks Academic Partners Program!

#### Databricks Shard Settings

The databricks shard for the course had to be 'Access Control Enabled' so as to allow students to choose who they want to collaborate with.

We did not allow students the ability to launch their own clusters. This was done to allow a simpler management model. Sometimes students would email if the 24/7 cluster went down due to spot-price exceeding the threshold. Then we could simply restart it.

For some of the student projects we needed to have nodes with upto 30GB of RAM per node and be able to scale up to several tens of nodes.  
The databricks team gave us a shard just for such research projects with cluster-creation capabilities that can be controlled by us. We had to be very careful when using this research shrad as some students who were initially given cluster-launching rights raked a larger bill and were not managing resources wisely. We finally, decided to administer the cluster launches ourselves.


## Community Edition

With support from Allison at databricks we got expedited community edition accounts for our students towards the second half of the course and encouraged them to work in CE. 
The community edition was a timely resource to support a long-term (and possibly life-long) learning strategy and allow the student to have a central place to continue learning and keeping up with Spark's rapid evolutions.
We have released the contents of the course as a `.dbc` archive that can be uploaded into anyone's databricks CE for self-learning as explained next.

## How to self-learn this content?

The 2016 instance of this [scalable-data-science course](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) finished on June 30 2016.

To learn Apache Spark for free try **databricks Community edition** by starting from [https://databricks.com/try-databricks](https://databricks.com/try-databricks).

All course content can be uploaded for self-paced learning by copying the following [URL for 2016/Spark1_6_to_1_3/scalable-data-science.dbc archive](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/dbcArchives/2016/Spark1_6_to_1_3/scalable-data-science.dbc)
and importing it from the URL to your [free Databricks Community Edition](https://community.cloud.databricks.com).

The Gitbook version of this content is [https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details](https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details).





## In News

by Raazesh Sainudiin

The course received some attention in Australasian Press.

The original draft that was sent to the Press appears below. Various substrings of it appeared in the following news feeds:

* [math.canterbury.ac.nz](http://www.math.canterbury.ac.nz/php/rss/news/?feed=news&articleId=1888), 
* [educators.co.nz](https://educators.co.nz/story/university-canterbury-embraces-cloud-computing-and-big-data/), 
* [scoop.co.nz](http://www.scoop.co.nz/stories/ED1605/S00003/uc-leads-cloud-computing-use-for-big-data-analytics.htm), 
* [itbrief.co.nz](https://itbrief.co.nz/article/university-canterbury-embraces-cloud-computing-and-big-data/), 
* [computerworld.co.nz](http://www.computerworld.co.nz/article/599291/aws-board-university-canterbury-leads-cloud-use-big-data-analytics/). 

### An Australasian First Story! UC leads cloud computing use for big data analytics

The University of Canterbury is the first university in Australasia to establish cutting edge training using cloud infrastructure to solve big-data analysis problems and give staff and students free access to this cloud infrastructure. 

UC Senior Lecturer from the School of Mathematics and Statistics Dr Raazesh Sainudiin secured grants from Databricks Academic Partners Program and Amazon Web Services Educate which enable free and ongoing access for all UC faculty, staff and students to use the enormous cloud-computing infrastructure for academic teaching and research. 

This provides UC with huge potential to emerge as a leader in big data analytics in this region of the globe, says Dr Sainudiin, who was formerly a Principal Data Scientist, Research and Development, at Wynyard Group and is now part of UC’s Big Data Working Group. He is giving a presentation about UC’s capabilities in industrial research and big data analytics to members of the local tech industry on 3 May.

“In today's digital world, data about every conceivable aspect of life is being collected and amassed at an unprecedented scale. To give you some idea of how much data we are talking about, it was estimated that a whopping 2.5 exabytes - or 2,500,000,000,000,000,000 bytes - of data were generated every single day, and that was back in 2012. This massive data could potentially hold answers for many critical questions and problems facing our world today. But to be able to get at these important answers, the first step is to be able to explore and analyse this gargantuan volume of data in a meaningful way,” he says.

“For example, what if all past and present recorded and real-time data of earthquakes on the planet could be analysed simultaneously? Or consider the live analysis of every tweet on Earth. There are on average 60 tweets per second (made publicly observable). The scale of such volumes of data is such that they can't be stored, let alone analysed, by one computer or even a 100 computers in any sort of reasonable timeframe. Cloud computing allows you to scale up access instantly to over 10,000 off-site computers simultaneously, as required by the scale of the real-world big data problem at hand, and complete the data analyses in the least amount of time needed - usually a matter of hours.”

UC has already established a research cluster (at [http:www.math.canterbury.ac.nz/databricks-projects](http://www.math.canterbury.ac.nz/databricks-projects)) with thousands of computer nodes running Apache Spark, a lightning-fast cluster computing engine for large-scale data processing. This locally set-up resource taps into the infrastructure being used by UC students in the course STAT478: Special Topics in Scalable Data Science, including several students who are full-time employees in the local tech industry. 

Students are trained to run their own big-data projects as part of their course requirements. This cutting-edge training using cloud infrastructure to solve big-data problems will generate globally competitive graduates for the data industry, Dr Sainudiin says.

With a curriculum created in consultation with the tech industry, the innovative course has received praise from Roger Jarquin, Chief Technical Officer of Wynyard Group. 

“Apache Spark is currently the fastest growing open source project. We at Wynyard rely on it for our successful products in advanced crime analytics. The syllabus of UC's course in scalable data science as well as the choice of the underlying technologies were designed with our R&D team's current needs in mind.
Wynyard’s data scientists and engineers are highly engaged with this course. 
Some of them have assited Raaz and Siva with the preparation and delivery of the course while some others are currently being trained in the course. 
We hope that such industry-academia collaborations will continue to be a dynamic training ground for future employees in our growing data industry,” says Jarquin, who is also an Adjunct Fellow of UC's School of Mathematics and Statistics.

Professor James Smithies, Director of King's Digital Lab, Department of Digital Humanities, King's College London, and former Senior Lecturer in History at UC, says the course in Scalable Data Science is “an excellent resource for the digital humanities, and sits very nicely beside activities occurring at King’s Digital Lab (KDL)”.

“The combination of AWS and Databricks is broadly in line with what we think digital humanities students and researchers will need, and benefits from excellent levels of usability and scalability. This kind of approach is of crucial importance to the future of digital humanities, as researchers move into big data analysis and we seek to provide our students with the tools and experiences they need to succeed in their careers both inside and outside university,” Prof Smithies says.

Designed to be industry oriented, this course complements courses from the Computer Science department and is also adaptable to distance learning. UC's Machine Learning expert and Senior Lecturer in Computer Science and Software Engineering, Dr. Kourosh Neshatian, says the course has students from a wide range of backgrounds and something for everyone to learn. 

“Most of the tools used in the course are state-of-the-art technology in this domain and are mostly free software. The course is very applied; every topic and subtopic is accompanied with its own relevant program which can be run by the teacher and student as the lecture progresses. From this point of view it nicely complements my Machine Learning (ML) course which is more concerned with designing ML algorithms,” says Dr. Neshatian.

UC Associate Professor Rick Beatson, recipient of the 2015 UC Innovation Medal, and Dr Sainudiin are making a technical presentation about UC’s capabilities in industrial research and big data analytics to Canterbury Tech (formerly Canterbury Software Cluster), a non-profit organisation of local tech insiders and entrepreneurs next month (http://canterburytech.nz/events/May-2016-event/). This technical presentation held at [UC's Centre for Entrepreneurship](http://www.uce.canterbury.ac.nz/) is an industrial outreach activity by UC's Big Data Working Group led by David Humm from [UC's Research & Innovation](http://www.research.canterbury.ac.nz/).

Dr Sainudiin is keen to introduce this freely available on-demand scalable computing resource to interested postgraduate students, faculty and staff across the University.

“While this Canterbury Tech outreach event is targeted at local industry, it is important to bring more awareness of the grant's infrastructure, potential benefits and utility to the UC community.”

Dr Sainudiin completed his PhD at Cornell University and a postdoctoral research fellowship at Oxford before joining UC in 2007.
