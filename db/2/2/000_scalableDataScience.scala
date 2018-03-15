// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube videos of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/zloLA6AyNqk/0.jpg)](https://www.youtube.com/embed/zloLA6AyNqk?start=0&end=3226&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/36tQR9UPXP4/0.jpg)](https://www.youtube.com/embed/36tQR9UPXP4?start=0&end=2341&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC ### A bit about your instructor:
// MAGIC 
// MAGIC I, Raazesh Sainudiin or **Raaz**, will be your instructor for the course in data science.
// MAGIC I have 
// MAGIC 
// MAGIC * more than 12 years of academic research experience in applied mathematics and statistics and 
// MAGIC * nearly 2 years of full-time experience in the data industry.
// MAGIC 
// MAGIC I currently (2017) have an effective joint appointment as:
// MAGIC 
// MAGIC * [Researcher in Applied Mathematics and Statistics](http://katalog.uu.se/profile/?id=N17-214) at [Department of Mathematics](http://www.math.uu.se/), [Uppsala University](http://www.uu.se/), Uppsala, Sweden and
// MAGIC * Data Science Consultant at AI and Analytics Centre of Excellence, [Combient AB](https://combient.com/), Stockholm, Sweden
// MAGIC 
// MAGIC Quick links on Raaz's background:
// MAGIC 
// MAGIC * [https://nz.linkedin.com/in/raazesh-sainudiin-45955845](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) 
// MAGIC * [Raaz's academic CV](https://lamastex.github.io/cv/) 
// MAGIC 
// MAGIC The inter-faculty course in the [Disciplinary Domain of Science and Technology, Uppsala University](http://www.teknat.uu.se/) is being assisted by Tilo Wiklund and Dan Lilja.

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

//This allows easy embedding of publicly available information into any other notebook
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
displayHTML(frameIt("https://en.wikipedia.org/wiki/Data_science",500))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Standing on shoulders of giants!
// MAGIC 
// MAGIC This course will build on two other edX courses where needed.  
// MAGIC 
// MAGIC * [BerkeleyX/CS100-1x, Introduction to Big Data Using Apache Spark by Anthony A Joseph, Chancellor's Professor, UC Berkeley](https://www.edx.org/course/introduction-big-data-apache-spark-uc-berkeleyx-cs100-1x)
// MAGIC * [BerkeleyX/CS190-1x, Scalable Machine Learning by Ameet Talwalkar, Ass. Prof., UC Los Angeles](https://www.edx.org/course/scalable-machine-learning-uc-berkeleyx-cs190-1x)
// MAGIC 
// MAGIC We encourage you to take these courses if you have more time.  For those of you (including the course coordinator) who have taken these courses formally in 2015, this course will be an *expanded scala version* with an emphasis on *individualized course project* as opposed to completing labs that test sytactic skills. 
// MAGIC 
// MAGIC We will also be borrowing more theoretical aspects from the following course:
// MAGIC 
// MAGIC * [Stanford/CME323, Distributed Algorithms and Optimization by Reza Zadeh, Ass. Prof., Institute for Computational and Mathematical Engineering, Stanford Univ.](http://stanford.edu/~rezab/dao/)
// MAGIC 
// MAGIC The first two recommended readings below are (already somewhat outdated!), the third one is advanced but current now and the fourth one is in progress:
// MAGIC 
// MAGIC * Learning Spark : lightning-fast data analytics by Holden Karau, Andy Konwinski, Patrick Wendell, and Matei Zaharia, O'Reilly, 2015.
// MAGIC * Advanced analytics with Spark : patterns for learning from data at scale, Sandy Ryza, Uri Laserson, Sean Owen and Josh Wills, O'Reilly, 2015.
// MAGIC * High Performance Spark: Best Practices for Scaling and Optimizing Apache Spark, Holden Karau, Rachel Warren, O'Reilly, 2017.
// MAGIC * Spark: The Definitive Guide, Big Data Processing Made Simple By Matei Zaharia, Bill Chambers, O'Reilly Media (due October 2017)
// MAGIC 
// MAGIC ![LS](http://www.syndetics.com/index.aspx?type=xw12&isbn=9781449358624/LC.GIF&client=ucanterburyl&upc=&oclc=) 
// MAGIC ![aas](http://t3.gstatic.com/images?q=tbn:ANd9GcSQs35NvHVozz77dhXYc2Ce8lKyJkR3oVwaxyA5Ub4W7Kvtvf9i "aas") 
// MAGIC ![hps](http://t2.gstatic.com/images?q=tbn:ANd9GcS7XN41_u0B8XehDmtXLJeuEPgnuULz16oFMRoANYz2e1-Vog3D "hps") 
// MAGIC ![sdg](https://covers.oreillystatic.com/images/0636920034957/rc_lrg.jpg "sdg")

// COMMAND ----------

// MAGIC %md
// MAGIC # How will you be assessed?
// MAGIC 
// MAGIC The course is extremely hands-on and therefore gives 50% of the final grade for attending each lab and completing it. Completing a lab essentially involves going through the cells in the cloned notebooks for each week to strengthen your understanding. This will ensure that the concept as well as the syntax is understood for the learning outcomes for each week. There are additional videos and exercises you are encouraged to watch/complete.  Some of the assigned exercises will be auto-graded and count towards the remaining 50% of the grade for the first course in *Introduction to Data Science*. 
// MAGIC 
// MAGIC Each student taking the second course in *Fundamentals of Data Science* will be working on a course project and present the findings to the class in the last week or two. The course project will count towards 50% of the final grade. The project will typically involve applying Spark on a publicly available dataset or writing a report to demonstrate in-depth understanding of appropriate literature of interest to the student’s immediate research goals in academia or industry. Oral presentation of the project will constitute 10% of the grade. The remaining 40% of the grade will be for the written part of the project, which will be graded for replicability and ease of understanding. The written report will be encouraged for publication in a technical blog format in public repositories such as GitHub through a publishable mark-down'd databricks notebook (this is intended to show-case the actual skills of the student to potential employers directly). Group work on projects may be considered for complex projects.
// MAGIC 
// MAGIC **Our Ideal Goal** Publish a GitBook together (edited by Raaz, Tilo and Dan) with your co-authored course projects (*note* your project is a concrete protfolio you can show your potential employers!).

// COMMAND ----------

// MAGIC %md
// MAGIC ## A Brief History of Data Analysis and Where Does "Big Data" Come From?
// MAGIC #### by Anthony Joseph in BerkeleyX/CS100.1x
// MAGIC 
// MAGIC * **(watch now 1:53):** A Brief History of Data Analysis
// MAGIC   * [![A Brief History of Data Analysis by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/5fSSvYlDkag/0.jpg)](https://www.youtube.com/watch?v=5fSSvYlDkag)
// MAGIC   
// MAGIC   
// MAGIC * **(watch now 5:05)**: Where does Data Come From?
// MAGIC   * [![Where Does Data Come From by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/eEJFlHE7Gt4/0.jpg)](https://www.youtube.com/watch?v=eEJFlHE7Gt4?rel=0&autoplay=1&modestbranding=1)
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
// MAGIC   * [![Data Science Defined by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/g4ujW1m2QNc/0.jpg)](https://www.youtube.com/watch?v=g4ujW1m2QNc?rel=0&modestbranding=1)
// MAGIC *  **(watch now 1:11)**: Cloud Computing
// MAGIC   * [![Cloud Computing by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/TAZvh0WmOHM/0.jpg)](https://www.youtube.com/watch?v=TAZvh0WmOHM?rel=0&modestbranding=1)
// MAGIC   * In fact, if you are logged into `https://*.databricks.com/*` you are computing in the cloud!
// MAGIC   * The Scalable Data Science course is supported by Databricks Academic Partners Program and the AWS Educate Grant to University of Canterbury (applied by Raaz Sainudiin in 2015).
// MAGIC * **(watch now 3:31)**: What's hard about data science
// MAGIC   * [![What's hard about data science by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/MIqbwJ6AbIY/0.jpg)](https://www.youtube.com/watch?v=MIqbwJ6AbIY?rel=0&modestbranding=1)

// COMMAND ----------

// MAGIC %md
// MAGIC **(Watch later 0:52)**: What is Data Science? According to a Udacity Course.
// MAGIC 
// MAGIC [![What is Data Science? Udacity Course](https://img.youtube.com/vi/9PIqjaXJo7M/0.jpg)](https://www.youtube.com/watch?v=9PIqjaXJo7M)

// COMMAND ----------

// MAGIC %md
// MAGIC # What should *you* be able to do at the end of this course?
// MAGIC 
// MAGIC * by following these sessions and doing some HOMEWORK assignments.
// MAGIC 
// MAGIC ## Understand the principles of fault-tolerant scalable computing in Spark
// MAGIC 
// MAGIC * in-memory and generic DAG extensions of Map-reduce
// MAGIC * resilient distributed datasets for fault-tolerance
// MAGIC * skills to process today's big data using state-of-the art techniques in Apache Spark 2.2, in terms of:
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
// MAGIC [![Michael Armbrust Spark Summit East](http://img.youtube.com/vi/35Y-rqSMCCA/0.jpg)](https://www.youtube.com/watch?v=35Y-rqSMCCA)
// MAGIC 
// MAGIC 
// MAGIC ### 2. Build Scalable Machine Learning Pipelines
// MAGIC 
// MAGIC ### Apply standard learning methods via scalably servable *end-to-end industrial ML pipelines*
// MAGIC #### ETL, Model, Validate, Test, reETL (feature re-engineer), model validate, test,..., serve model to clients
// MAGIC ##### (we will choose from this list)
// MAGIC 
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
// MAGIC [![Ali G's Live Sentiment Analysist](http://img.youtube.com/vi/NR1MYg_7oSg/0.jpg)](https://www.youtube.com/watch?v=NR1MYg_7oSg)
// MAGIC 
// MAGIC #### (watch later) Spark Summit 2017 - Expanding Apache Spark Use Cases in 2.2 and Beyond - Matei Zaharia, Tim Hunter & Michael Armbrust - Deep Learning and Structured Streaming
// MAGIC [![Expanding Apache Spark Use Cases in 2.2 and Beyond - Matei Zaharia, Tim Hunter & Michael Armbrust - Spark Summit 2017 - Deep Learning and Structured Streaming](http://img.youtube.com/vi/qAZ5XUz32yM/0.jpg)](https://www.youtube.com/watch?v=qAZ5XUz32yM)
// MAGIC 
// MAGIC # What Do You Really Want to Do?
// MAGIC 
// MAGIC ## Let's Break-out and Introduce each other briefly - (15-20 1-minute pitches)
// MAGIC 
// MAGIC * **What is your name, area of research/interests, and what are you interested in getting from this course?**
// MAGIC Some example answers:
// MAGIC 
// MAGIC * I am Erik Eriksson, I am into population genomics, want to learn distributed computing frameworks for analysing genomes of entire populations for certain rare diseases.
// MAGIC * I am Pippi Halgrimsdottir and am into analysisng outputs of massive simulations of 
// MAGIC   * embarassingly parallel parametric simulations of interacting agents in ecology 
// MAGIC   * or chemical physics super-computer outputs?
// MAGIC * ...?

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ---
// MAGIC 
// MAGIC ---

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
// MAGIC   * [![A Brief History of Data Analysis by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/16wqonWTAsI/0.jpg)](https://www.youtube.com/watch?v=16wqonWTAsI)
// MAGIC * other interesting big data examples - recommender systems and netflix prize?
// MAGIC 
// MAGIC * **(watch later 10:41)**: Contrasting data science with traditional databases, ML, Scientific computing
// MAGIC   * [![Data Science Database Contrast by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/c7KG0c3ADk0/0.jpg)](https://www.youtube.com/watch?v=c7KG0c3ADk0)
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
// MAGIC   * [![Approaches to Data Science by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/yAOEyeDVn8s/0.jpg)](https://www.youtube.com/watch?v=yAOEyeDVn8s)
// MAGIC * **(watch later 4:29)**:  Performing Data Science and Preparing Data, Data Acquisition and Preparation, ETL, ...
// MAGIC   * [![Data Science Database Contrast by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/3V6ws_VEzaE/0.jpg)](https://www.youtube.com/watch?v=3V6ws_VEzaE)
// MAGIC * **(watch later 2:01)**: Four Examples of Data Science Roles
// MAGIC   * [![Data Science Roles by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/gB-9rdM6W1A/0.jpg)](https://www.youtube.com/watch?v=gB-9rdM6W1A)
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
// MAGIC ## What does a Data Scientist do today and how to prepare your mind for a career in data science?
// MAGIC 
// MAGIC Some nice readings about data science:
// MAGIC 
// MAGIC * [http://drewconway.com/zia/2013/3/26/the-data-science-venn-diagram](http://drewconway.com/zia/2013/3/26/the-data-science-venn-diagram)
// MAGIC * Highly Recommended: [Intro to Data Science for Academics](https://medium.com/@noahmp/intro-to-data-science-for-academics-d96639a3225c)
// MAGIC   * "It has been a little over three years since [Harvard Business Review](https://hbr.org/2012/10/data-scientist-the-sexiest-job-of-the-21st-century) called data science “The sexiest jobs of the 21st century” in late 2012. So far this appears a reasonable call, a few weeks ago [The Economist reported](http://www.economist.com/news/special-report/21714169-technological-change-demands-stronger-and-more-continuous-connections-between-education?frsc=dg%7Cc) that demand for data analysts has grown 372% while demand for data visualization skills (as a specialized field within data analysis) has grown 2,574%. We will likely see increasing demand for data scientist for years to come as the amount of data available for analysis and the number of automated systems in operation continue to climb. The biggest winners of the data science bonanza will be technical folks with strong communication skills and a domain of focus."
// MAGIC 
// MAGIC ## Some perspectives from current affairs
// MAGIC * [https://www.theguardian.com/politics/2017/jan/19/crisis-of-statistics-big-data-democracy](https://www.theguardian.com/politics/2017/jan/19/crisis-of-statistics-big-data-democracy)
// MAGIC 
// MAGIC * share others you find illuminating with your mates at our Meetup discussion board on [Current Affairs of Interest](https://www.meetup.com/Uppsala-Big-Data-Meetup/messages/boards/forum/23906792/)

// COMMAND ----------

