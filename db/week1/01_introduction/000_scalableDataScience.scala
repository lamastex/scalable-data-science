// Databricks notebook source exported at Thu, 16 Jun 2016 07:32:05 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week1/01_introduction/000_scalableDataScience.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week1/01_introduction/000_scalableDataScience](http://img.youtube.com/vi/O8JbxgPpAU8/0.jpg)](https://www.youtube.com/v/O8JbxgPpAU8?rel=0&autoplay=1&modestbranding=1&start=0&end=2064)

// COMMAND ----------

// MAGIC %md
// MAGIC ##### A bit about who your instructors are:
// MAGIC  * **Raaz** from academia (10 years + 1 year in industry) [https://nz.linkedin.com/in/raazesh-sainudiin-45955845](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Raaz's academic CV](http://www.math.canterbury.ac.nz/~r.sainudiin/cv.shtml) and 
// MAGIC  * **Siva** from industry (11 years) [https://www.linkedin.com/in/sivanand](https://www.linkedin.com/in/sivanand)

// COMMAND ----------

// MAGIC %md 
// MAGIC # What is Scalable [Data Science](https://en.wikipedia.org/wiki/Data_science) in one picture?
// MAGIC 
// MAGIC ![what is sds?](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/sds.png "sds")

// COMMAND ----------

// MAGIC %md
// MAGIC Source: [Vasant Dhar, Data Science and Prediction, Communications of the ACM, Vol. 56 (1). p. 64, DOI:10.1145/2500499](http://dl.acm.org/citation.cfm?id=2500499)
// MAGIC 
// MAGIC ### key insights
// MAGIC * Data Science is the study of *the generalizabile extraction of knowledge from data*.
// MAGIC * A common epistemic requirement in assessing whether new knowledge is actionable for decision making is its predictive power, not just its ability to explain the past.
// MAGIC * A *data scientist requires an integrated skill set spanning* 
// MAGIC   * mathematics, 
// MAGIC   * machine learning, 
// MAGIC   * artificial intelligence, 
// MAGIC   * statistics, 
// MAGIC   * databases, and 
// MAGIC   * optimization, 
// MAGIC   * along with a deep understanding of the craft of problem formulation to engineer effective solutions.
// MAGIC   
// MAGIC Source: [Machine learning: Trends, perspectives, and prospects, M. I. Jordan, T. M. Mitchell, Science  17 Jul 2015: Vol. 349, Issue 6245, pp. 255-260, DOI: 10.1126/science.aaa8415](http://science.sciencemag.org/content/349/6245/255.full-text.pdf+html)
// MAGIC 
// MAGIC ### key insights
// MAGIC * ML is concerned with the building of computers that improve automatically through experience
// MAGIC * ML lies at the intersection of computer science and statistics and at the core of artificial intelligence and data science
// MAGIC * Recent progress in ML is due to:
// MAGIC   * development of new algorithms and theory
// MAGIC   * ongoing explosion in the availability of online data
// MAGIC   * availability of low-cost computation (*through clusters of commodity hardware in the *cloud* )
// MAGIC * The adoption of data science and ML methods is leading to more evidence-based decision-making across:
// MAGIC   * health sciences (neuroscience research, )
// MAGIC   * manufacturing
// MAGIC   * robotics (autonomous vehicle)
// MAGIC   * vision, speech processing, natural language processing
// MAGIC   * education
// MAGIC   * financial modeling
// MAGIC   * policing
// MAGIC   * marketing

// COMMAND ----------

// MAGIC %md
// MAGIC ## Standing on shoulders of giants!
// MAGIC 
// MAGIC This course will build on two other edX courses where needed.  
// MAGIC * [BerkeleyX/CS100-1x, Introduction to Big Data Using Apache Spark by Anthony A Joseph, Chancellor's Professor, UC Berkeley](https://www.edx.org/course/introduction-big-data-apache-spark-uc-berkeleyx-cs100-1x)
// MAGIC * [BerkeleyX/CS190-1x, Scalable Machine Learning by Ameet Talwalkar, Ass. Prof., UC Los Angeles](https://www.edx.org/course/scalable-machine-learning-uc-berkeleyx-cs190-1x)
// MAGIC 
// MAGIC We encourage you to take these courses if you have more time.  For those of you (including the course coordinator) who have taken these courses formally in 2015, this course will be an *expanded scala version* with an emphasis on *individualized course project* as opposed to completing labs that test sytactic skills. 
// MAGIC 
// MAGIC We will also be borrowing more theoretical aspects from the following course:
// MAGIC * [Stanford/CME323, Distributed Algorithms and Optimization by Reza Zadeh, Ass. Prof., Institute for Computational and Mathematical Engineering, Stanford Univ.](http://stanford.edu/~rezab/dao/)
// MAGIC 
// MAGIC The two recommended readings are (already somewhat outdated!):
// MAGIC * Learning Spark : lightning-fast data analytics by Holden Karau, Andy Konwinski, Patrick Wendell, and Matei Zaharia, O'Reilly, 2015.
// MAGIC * Advanced analytics with Spark : patterns for learning from data at scale, O'Reilly, 2015.
// MAGIC 
// MAGIC ![LS](http://www.syndetics.com/index.aspx?type=xw12&isbn=9781449358624/LC.GIF&client=ucanterburyl&upc=&oclc=) and  ![aas](http://t3.gstatic.com/images?q=tbn:ANd9GcSQs35NvHVozz77dhXYc2Ce8lKyJkR3oVwaxyA5Ub4W7Kvtvf9i "aas")
// MAGIC 
// MAGIC **These are available at [UC Library](http://ipac.canterbury.ac.nz/ipac20/ipac.jsp?profile=a&npp=30&index=.CC&term=STAT478)**

// COMMAND ----------

// MAGIC %md
// MAGIC # How will you be assessed?
// MAGIC 
// MAGIC The course is extremely hands-on and therefore gives 50% of the final grade for attending each lab and completing it. Completing a lab essentially involves going through the cells in the cloned notebooks for each week to strengthen your understanding. This will ensure that the concept as well as the syntax is understood for the learning outcomes for each week. There are additional videos and exercises you are encouraged to watch/complete.  These additional exercises will not be graded. You may use 1800-1830 hours to ask any questions about the contents in the current or previous weeks. 
// MAGIC 
// MAGIC Each student will be working on a course project and present the findings to the class in the last week or two. The course project will be done in [http://www.math.canterbury.ac.nz/databricks-projects](http://www.math.canterbury.ac.nz/databricks-projects) and counts towards 50% of the final grade. The project will typically involve applying Spark on a publicly available dataset or writing a report to demonstrate in-depth understanding of appropriate literature of interest to the student’s immediate research goals in academia or industry. Oral presentation of the project will constitute 10% of the grade. The remaining 40% of the grade will be for the written part of the project, which will be graded for replicability and ease of understanding. The written report will be encouraged for publication in a technical blog format in public repositories such as GitHub through a publishable mark-down'd databricks notebook (this is intended to show-case the actual skills of the student to potential employers directly). Group work on projects may be considered for complex projects.

// COMMAND ----------

// MAGIC %md
// MAGIC ## A Brief History of Data Analysis and Where Does "Big Data" Come From?
// MAGIC #### by Anthony Joseph in BerkeleyX/CS100.1x
// MAGIC 
// MAGIC * **(watch now 1:53):** A Brief History of Data Analysis
// MAGIC   * [![A Brief History of Data Analysis by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/5fSSvYlDkag/0.jpg)](https://www.youtube.com/v/5fSSvYlDkag?rel=0&autoplay=1&modestbranding=1)
// MAGIC   
// MAGIC * **(watch now 5:05)**: Where does Data Come From?
// MAGIC   * [![Where Does Data Come From by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/eEJFlHE7Gt4/0.jpg)](https://www.youtube.com/v/eEJFlHE7Gt4?rel=0&autoplay=1&modestbranding=1)
// MAGIC   * SUMMARY of Some of the sources of big data.
// MAGIC      * online click-streams (a lot of it is recorded but a tiny amount is analyzed):
// MAGIC        * record every click
// MAGIC        * every ad you view
// MAGIC        * every billing event,
// MAGIC        * every transaction, every network message, and every fault.
// MAGIC      * User-generated content (on web and mobile devices):
// MAGIC        * every post that you make on Facebook 
// MAGIC        * every picture sent on Instagram
// MAGIC        * every review you write for Yelp or TripAdvisor
// MAGIC        * every tweet you send on Twitter
// MAGIC        * every video that you post to YouTube.
// MAGIC      * Science (for scientific computing):
// MAGIC        * data from various repositories for natural language processing:
// MAGIC           * Wikipedia,
// MAGIC           * the Library of Congress, 
// MAGIC           * twitter firehose and google ngrams and digital archives,
// MAGIC        * data from scientific instruments/sensors/computers:
// MAGIC          * the Large Hadron Collider (more data in a year than all the other data sources combined!)
// MAGIC          * genome sequencing data (sequencing cost is dropping much faster than Moore's Law!)
// MAGIC          * output of high-performance computers (super-computers) for data fusion, estimation/prediction and exploratory data analysis
// MAGIC     * Graphs are also an interesting source of big data (*network science*).
// MAGIC       * social networks (collaborations, followers, fb-friends or other relationships),
// MAGIC       * telecommunication networks, 
// MAGIC       * computer networks,
// MAGIC       * road networks
// MAGIC     * machine logs:
// MAGIC       * by servers around the internet (hundreds of millions of machines out there!)
// MAGIC       * internet of things.
// MAGIC     

// COMMAND ----------

// MAGIC %md
// MAGIC ## Data Science Defined, Cloud Computing and What's Hard About Data Science?
// MAGIC #### by Anthony Joseph in BerkeleyX/CS100.1x
// MAGIC 
// MAGIC * **(watch now 2:03)**: Data Science Defined
// MAGIC   * [![Data Science Defined by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/g4ujW1m2QNc/0.jpg)](https://www.youtube.com/v/g4ujW1m2QNc?rel=0&autoplay=1&modestbranding=1)
// MAGIC *  **(watch now 1:11)**: Cloud Computing
// MAGIC   * [![Cloud Computing by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/TAZvh0WmOHM/0.jpg)](https://www.youtube.com/v/TAZvh0WmOHM?rel=0&autoplay=1&modestbranding=1)
// MAGIC   * In fact, if you are logged into `https://*.databricks.com/*` you are computing in the cloud!
// MAGIC   * The Scalable Data Science course is supported by Databricks Academic Partners Program and the AWS Educate Grant to University of Canterbury (applied by Raaz Sainudiin in 2015).
// MAGIC * **(watch now 3:31)**: What's hard about data science
// MAGIC   * [![What's hard about data science by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/MIqbwJ6AbIY/0.jpg)](https://www.youtube.com/v/MIqbwJ6AbIY?rel=0&autoplay=1&modestbranding=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # What should *you* be able to do at the end of this course?
// MAGIC * by following these sessions and doing some HOMEWORK expected of an honours UC student.
// MAGIC 
// MAGIC ## Understand the principles of fault-tolerant scalable computing in Spark
// MAGIC 
// MAGIC * in-memory and generic DAG extensions of Map-reduce
// MAGIC * resilient distributed datasets for fault-tolerance
// MAGIC * skills to process today's big data using state-of-the art techniques in Apache Spark 1.6, in terms of:
// MAGIC   * hands-on coding with real datasets
// MAGIC   * an intuitive (non-mathematical) understanding of the ideas behind the technology and methods
// MAGIC   * pointers to academic papers in the literature, technical blogs and video streams for *you to futher your theoretical understanding*.
// MAGIC 
// MAGIC # More concretely, you will be able to:
// MAGIC ### 1. Extract, Transform, Load, Interact, Explore and Analyze Data
// MAGIC 
// MAGIC #### (watch later) Exploring Apache Web Logs (semi-structured data) - topic of weeks 2/3
// MAGIC [![Databricks jump start](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/dataExploreWebLogsSQL.png)](https://vimeo.com/137874931)
// MAGIC 
// MAGIC #### (watch later) Exploring Wikipedia Click Streams (structured data) - topic of weeks 2/3
// MAGIC [![Michael Armbrust Spark Summit East](http://img.youtube.com/vi/35Y-rqSMCCA/0.jpg)](https://www.youtube.com/v/35Y-rqSMCCA)
// MAGIC 
// MAGIC 
// MAGIC ### 2. Build Scalable Machine Learning Pipelines
// MAGIC 
// MAGIC ### Apply standard learning methods via scalably servable *end-to-end industrial ML pipelines*
// MAGIC #### ETL, Model, Validate, Test, reETL (feature re-engineer), model validate, test,..., serve model to clients
// MAGIC ##### (we will choose from this list)
// MAGIC * Supervised Learning Methods: Regression /Classification
// MAGIC * Unsupervised Learning Methods: Clustering
// MAGIC * Recommedation systems
// MAGIC * Streaming
// MAGIC * Graph processing
// MAGIC * Geospatial data-processing
// MAGIC * Topic modeling
// MAGIC * Deep Learning
// MAGIC * ...
// MAGIC 
// MAGIC 
// MAGIC ####  (watch later) Spark Summit 2015 demo: Creating an end-to-end machine learning data pipeline with Databricks (Live Sentiment Analysis)
// MAGIC [![Ali G's :ive Sentiment Analysist](http://img.youtube.com/vi/NR1MYg_7oSg/0.jpg)](https://www.youtube.com/v/NR1MYg_7oSg)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC 20 minutes of 90+10 minutes are up!
// MAGIC     

// COMMAND ----------

// MAGIC %md
// MAGIC ## EXTRA: Databases Versus Data Science
// MAGIC #### by Anthony Joseph in BerkeleyX/CS100.1x
// MAGIC 
// MAGIC * **(watch later 2:31)**: Why all the excitement about *Big Data Analytics*? (using google search to now-cast google flu-trends)
// MAGIC   * [![A Brief History of Data Analysis by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/16wqonWTAsI/0.jpg)](https://www.youtube.com/v/16wqonWTAsI)
// MAGIC * other interesting big data examples - recommender systems and netflix prize?
// MAGIC 
// MAGIC * **(watch later 10:41)**: Contrasting data science with traditional databases, ML, Scientific computing
// MAGIC   * [![Data Science Database Contrast by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/c7KG0c3ADk0/0.jpg)](https://www.youtube.com/v/c7KG0c3ADk0)
// MAGIC   * SUMMARY:
// MAGIC    * traditional databases versus data science
// MAGIC      * preciousness versus cheapness of the data
// MAGIC      * ACID and eventual consistency, CAP theorem, ...
// MAGIC      * interactive querying: SQL versus noSQL
// MAGIC      * querying the past versus querying/predicting the future
// MAGIC    * traditional scientific computing versus data science
// MAGIC      * science-based or mechanistic models versus data-driven black-box (deep-learning) statistical models (of course both schools co-exist)
// MAGIC      * super-computers in traditional science-based models versus cluster of commodity computers
// MAGIC    * traditional ML versus data science
// MAGIC      * smaller amounts of clean data in traditional ML versus massive amounts of dirty data in data science
// MAGIC      * traditional ML researchers try to publish academic papers versus data scientists try to produce actionable intelligent systems
// MAGIC * **(watch later 1:49)**: Three Approaches to Data Science
// MAGIC   * [![Approaches to Data Science by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/yAOEyeDVn8s/0.jpg)](https://www.youtube.com/v/yAOEyeDVn8s)
// MAGIC * **(watch later 4:29)**:  Performing Data Science and Preparing Data, Data Acquisition and Preparation, ETL, ...
// MAGIC   * [![Data Science Database Contrast by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/3V6ws_VEzaE/0.jpg)](https://www.youtube.com/v/3V6ws_VEzaE)
// MAGIC * **(watch later 2:01)**: Four Examples of Data Science Roles
// MAGIC   * [![Data Science Roles by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/gB-9rdM6W1A/0.jpg)](https://www.youtube.com/v/gB-9rdM6W1A)
// MAGIC   * SUMMARY of Data Science Roles.
// MAGIC    * individual roles:
// MAGIC      1. business person
// MAGIC      2. programmer
// MAGIC    * organizational roles:
// MAGIC      3. enterprise
// MAGIC      4. web company
// MAGIC   * Each role has it own unique set of:
// MAGIC     * data sources
// MAGIC     * Extract-Transform-Load (ETL) process
// MAGIC     * business intelligence and analytics tools
// MAGIC   * Most Maths/Stats/Computing programs cater to the *programmer* role
// MAGIC     * Numpy and Matplotlib, R, Matlab, and Octave.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)