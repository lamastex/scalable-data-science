// Databricks notebook source exported at Thu, 11 Feb 2016 01:58:49 UTC
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
// MAGIC # What is Scalable [Data Science](https://en.wikipedia.org/wiki/Data_science) in one picture?
// MAGIC 
// MAGIC ![what is sds?](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/sds.png "sds")

// COMMAND ----------

// MAGIC %md
// MAGIC Source: [Vasant Dhar, Data Science and Prediction, Communications of the ACM, Vol. 56 (1). p. 64, DOI:10.1145/2500499](http://dl.acm.org/citation.cfm?id=2500499)
// MAGIC 
// MAGIC ### key insights
// MAGIC * Data Science is the study of the generalizabile extraction of knowledge from data.
// MAGIC * A common epistemic requirement in assessing whether new knowledge is actionable for decision making is its predictive power, not just its ability to explain the past.
// MAGIC * A *data scientist requires an integrated skill set spanning* 
// MAGIC   * mathematics, 
// MAGIC   * machine learning, 
// MAGIC   * artificial intelligence, 
// MAGIC   * statistics, 
// MAGIC   * databases, and 
// MAGIC   * optimization, 
// MAGIC   * along with a deep understanding of the craft of problem formulation to engineer effective solutions.

// COMMAND ----------

// MAGIC %md 
// MAGIC Source: [Machine learning: Trends, perspectives, and prospects, M. I. Jordan, T. M. Mitchell, Science  17 Jul 2015: Vol. 349, Issue 6245, pp. 255-260, DOI: 10.1126/science.aaa8415](http://science.sciencemag.org/content/349/6245/255.full-text.pdf+html)
// MAGIC 
// MAGIC ### key insights
// MAGIC * ML is concerned with the building of computers that improve automatically through experience
// MAGIC * ML lies at the intersection of computer science and statistics and at the core of artificial intelligence and data science
// MAGIC * Rcent progress in ML is due to:
// MAGIC   * development of new algorithms and theory
// MAGIC   * ongoing explosion in the availability of online data
// MAGIC   * availability of low-cost computation (through clusters of commodity hardware in the *cloud*)
// MAGIC * The adoption of data science and ML methods is leading to more evidence-based decision-making across:
// MAGIC   * health sciences (neuroscience research, )
// MAGIC   * manufacturing
// MAGIC   * robotics (autonomous vehicle)
// MAGIC   * vision, speech processing, natural language processing
// MAGIC   * education
// MAGIC   * financial modeling
// MAGIC   * policing
// MAGIC   * marketing
// MAGIC   
// MAGIC   The following image is from Figure 1 of [DOI: 10.1126/science.aaa8415](http://science.sciencemag.org/content/349/6245/255.full-text.pdf+html)
// MAGIC   
// MAGIC   ![ML Fig 1](https://d2ufo47lrtsv5s.cloudfront.net/content/sci/349/6245/255/F1.large.jpg "ML Fig 1")

// COMMAND ----------

// MAGIC %md
// MAGIC ## Standing on shoulders of three giants!
// MAGIC 
// MAGIC This course will build on two other edX courses where needed.  
// MAGIC * [BerkeleyX/CS100-1x, Introduction to Big Data Using Apache Spark by Anthony A Joseph, Chancellor's Professor, UC Berkeley](https://www.edx.org/course/introduction-big-data-apache-spark-uc-berkeleyx-cs100-1x)
// MAGIC * [BerkeleyX/CS190-1x, Scalable Machine Learning by Ameet Talwalkar, Ass. Prof., UC Los Angeles](https://www.edx.org/course/scalable-machine-learning-uc-berkeleyx-cs190-1x)
// MAGIC 
// MAGIC We encourage you to take these courses if you have more time.  For those of you (including the course coordinator) who have taken these courses formally in 2015 this course will be an *expanded scala version* with an emphasis on *individualized course project* as opposed to completing labs that test sytactic skills. 
// MAGIC 
// MAGIC We will also be borrowing more theoretical aspects from the following course:
// MAGIC * [Stanford/CME323, Distributed Algorithms and Optimization by Reza Zadeh, Ass. Prof., Institute for Computational and Mathematical Engineering, Stanford Univ.](http://stanford.edu/~rezab/dao/)

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC ## A Brief History of Data Analysis and Where Does "Big Data" Come From?
// MAGIC #### by Anthony Joseph in BerkeleyX/CS100.1x
// MAGIC 
// MAGIC * **(watch now 1:53):** A Brief History of Data Analysis
// MAGIC   * [![A Brief History of Data Analysis by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/5fSSvYlDkag/0.jpg)](https://www.youtube.com/v/5fSSvYlDkag)
// MAGIC   
// MAGIC * **(watch now 5:05)**: Where does Data Come From?
// MAGIC   * [![Where Does Data Come From by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/eEJFlHE7Gt4/0.jpg)](https://www.youtube.com/v/eEJFlHE7Gt4)
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
// MAGIC   * [![Data Science Defined by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/g4ujW1m2QNc/0.jpg)](https://www.youtube.com/v/g4ujW1m2QNc)
// MAGIC *  **(watch now 1:11)**: Cloud Computing
// MAGIC   * [![Cloud Computing by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/TAZvh0WmOHM/0.jpg)](https://www.youtube.com/v/TAZvh0WmOHM)
// MAGIC   * In fact, if you are logged into `https://*.databricks.com/*` you are computing in the cloud!
// MAGIC   * The Scalable Data Science course is supported by Databricks Academic Partners Program and the AWS Educate Grant to University of Canterbury (applied by Raaz Sainudiin in 2015).
// MAGIC * **(watch now 3:31)**: What's hard about data science
// MAGIC   * [![What's hard about data science by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/MIqbwJ6AbIY/0.jpg)](https://www.youtube.com/v/MIqbwJ6AbIY)

// COMMAND ----------


    

// COMMAND ----------

// MAGIC %md
// MAGIC ## HOME WORK: Databases Versus Data Science
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
