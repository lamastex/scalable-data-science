// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC ### A bit about your instructor:
// MAGIC 
// MAGIC I, Raazesh Sainudiin or **Raaz**, will be your instructor for the module today.
// MAGIC 
// MAGIC I have 
// MAGIC 
// MAGIC * more than 15 years of academic research experience in applied mathematics and statistics and 
// MAGIC * over 3 and 5 years of full-time and part-time experience in the data industry.
// MAGIC 
// MAGIC I currently (2020) have an effective joint appointment as:
// MAGIC 
// MAGIC * [Associate Professor of Mathematics with specialisation in Data Science](http://katalog.uu.se/profile/?id=N17-214) at [Department of Mathematics](http://www.math.uu.se/), [Uppsala University](http://www.uu.se/), Uppsala, Sweden and
// MAGIC * Senior Researcher, Data Science at [Combient Mix AB](https://combient.com/mix), Stockholm, Sweden
// MAGIC 
// MAGIC Quick links on Raaz's background:
// MAGIC 
// MAGIC * [https://www.linkedin.com/in/raazesh-sainudiin-45955845/](https://www.linkedin.com/in/raazesh-sainudiin-45955845/) 
// MAGIC * [Raaz's academic CV](https://lamastex.github.io/cv/) 

// COMMAND ----------

// MAGIC %md 
// MAGIC # What is the [Data Science Process](https://en.wikipedia.org/wiki/Data_science) in one picture?
// MAGIC 
// MAGIC ![what is sds?](https://github.com/lamastex/scalable-data-science/raw/master/assets/images/sds.png "sds")
// MAGIC 
// MAGIC ---
// MAGIC 
// MAGIC # What is scalable data science and distributed machine learning?
// MAGIC 
// MAGIC Scalability merely refers to the ability of the data science process to scale to massive datasets (popularly known as *big data*). 
// MAGIC 
// MAGIC For this we need *distributed fault-tolerant computing* typically over large clusters of commodity computers -- the core infrastructure in a public cloud today.
// MAGIC 
// MAGIC *Distributed Machine Learning* allows the models in the data science process to be scalably trained and extract value from big data. 

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
// MAGIC # Data Engineering
// MAGIC 
// MAGIC There are several views on what a data engineer is supposed to do:
// MAGIC 
// MAGIC Some views are rather narrow and emphasise division of labour between data engineers and data scientists:
// MAGIC  
// MAGIC - https://www.oreilly.com/ideas/data-engineering-a-quick-and-simple-definition
// MAGIC   - Let's check out what skills a data engineer is expected to have according to the link above.
// MAGIC 
// MAGIC > "Ian Buss, principal solutions architect at Cloudera, notes that data scientists focus on finding new insights from a data set, while data engineers are concerned with the production readiness of that data and all that comes with it: formats, scaling, resilience, security, and more."
// MAGIC 
// MAGIC > What skills do data engineers need?
// MAGIC > Those “10-30 different big data technologies” Anderson references in “Data engineers vs. data scientists” can fall under numerous areas, such as file formats, > ingestion engines, stream processing, batch processing, batch SQL, data storage, cluster management, transaction databases, web frameworks, data visualizations, and machine learning. And that’s just the tip of the iceberg.
// MAGIC 
// MAGIC > Buss says data engineers should have the following skills and knowledge:
// MAGIC 
// MAGIC > - They need to know Linux and they should be comfortable using the command line.
// MAGIC > - They should have experience programming in at least Python or Scala/Java.
// MAGIC > - They need to know SQL.
// MAGIC > - They need some understanding of distributed systems in general and how they are different from traditional storage and processing systems.
// MAGIC > - They need a deep understanding of the ecosystem, including ingestion (e.g. Kafka, Kinesis), processing frameworks (e.g. Spark, Flink) and storage engines (e.g. S3, HDFS, HBase, Kudu). They should know the strengths and weaknesses of each tool and what it's best used for.
// MAGIC > - They need to know how to access and process data.
// MAGIC 
// MAGIC Let's dive deeper into such highly compartmentalised views of data engineers and data scientists and the so-called "machine learning engineers" according the following view:
// MAGIC 
// MAGIC - https://www.oreilly.com/ideas/data-engineers-vs-data-scientists
// MAGIC 
// MAGIC embedded below.

// COMMAND ----------

displayHTML(frameIt("https://www.oreilly.com/ideas/data-engineers-vs-data-scientists",500))

// COMMAND ----------

// MAGIC 
// MAGIC %md
// MAGIC # The Data Engineering Scientist as "The Middle Way"
// MAGIC 
// MAGIC Here are some basic axioms that should be self-evident.
// MAGIC 
// MAGIC - Yes, there are differences in skillsets across humans
// MAGIC   - some humans will be better and have inclinations for engineering and others for pure mathematics by nature and nurture
// MAGIC   - one human cannot easily be a master of everything needed for innovating a new data-based product or service (very very rarely)
// MAGIC - Skills can be gained by any human who wants to learn to the extent s/he is able to expend time, energy, etc.
// MAGIC 
// MAGIC For the **Scalable Data Engineering Science Process:** *towards Production-Ready and Productisable Prototyping for the Data Factory* we need to allow each data engineer to be more of a data scientist and each data scientist to be more of a data engineer, up to each individual's *comfort zones* in technical and mathematical/conceptual and time-availability planes, but with some **minimal expectations** of mutual appreciation.
// MAGIC 
// MAGIC This course is designed to help you take the first minimal steps towards **data engineering science**.
// MAGIC 
// MAGIC In the sequel it will become apparent **why a team of data engineering scientists** with skills across the conventional (2019) spectrum of data engineer versus data scientist  **is crucial** for **Production-Ready and Productisable Prototyping for the Data Factory**.

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
// MAGIC * High Performance Spark: Best Practices for Scaling and Optimizing Apache Spark, Holden Karau, Rachel Warren, O'Reilly, 2017.
// MAGIC * Mastering Spark for Data Science By Andrew Morgan, Antoine Amend, David George, Matthew Hallett, Packt Publishing, 2017.
// MAGIC * Spark: The Definitive Guide, Big Data Processing Made Simple By Matei Zaharia, Bill Chambers, O'Reilly Media, 2018.
// MAGIC 
// MAGIC <img src="http://www.syndetics.com/index.aspx?type=xw12&isbn=9781449358624/LC.GIF&client=ucanterburyl&upc=&oclc="  height="150" width="150">
// MAGIC <img src="https://images-na.ssl-images-amazon.com/images/I/51wFajfF5oL._SX258_BO1,204,203,200_.jpg"  height="150" width="150">
// MAGIC <img src="https://covers.oreillystatic.com/images/0636920034957/lrg.jpg"  height="150" width="150">

// COMMAND ----------

// MAGIC %md
// MAGIC # How will you be assessed?
// MAGIC 
// MAGIC There will be minimal exercises and *possibly* an open mini-project you can do and present briefly to us with notebooks/codes and video.

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
// MAGIC * skills to process today's big data using state-of-the art techniques in Apache Spark 3.0, in terms of:
// MAGIC   * hands-on coding with realistic datasets
// MAGIC   * an intuitive understanding of the ideas behind the technology and methods
// MAGIC   * pointers to academic papers in the literature, technical blogs and video streams for *you to futher your theoretical understanding*.
// MAGIC 
// MAGIC # More concretely, you will be able to:
// MAGIC 
// MAGIC * Extract, Transform, Load, Interact, Explore and Analyze Data
// MAGIC * Build Scalable Machine Learning Pipelines (or help build them)
// MAGIC 
// MAGIC 
// MAGIC # How to keep up?
// MAGIC 
// MAGIC This is a fast-changing world. Recent videos are archived here (these videos are a great way to learn the latest happenings in industrial R&D today!):
// MAGIC 
// MAGIC  - https://databricks.com/sparkaisummit/north-america/sessions
// MAGIC 
// MAGIC # What is mathematically stable in the world of 'big data'?
// MAGIC 
// MAGIC - There is a growing body of work on the analysis of parallel and distributed algorithms, the work-horse of big data and AI. 
// MAGIC   - We will see some of this in a theoretical module later, but the focus here is on how to write programs and analyze data.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Recommended Light reading (18 minutes)
// MAGIC 
// MAGIC - [https://towardsdatascience.com/what-is-big-data-understanding-the-history-32078f3b53ce](https://towardsdatascience.com/what-is-big-data-understanding-the-history-32078f3b53ce)

// COMMAND ----------

displayHTML(frameIt("https://towardsdatascience.com/what-is-big-data-understanding-the-history-32078f3b53ce",800))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ---
// MAGIC 
// MAGIC ---

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC x minutes of 360 minutes are up!
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
// MAGIC # Industrial Case Study
// MAGIC 
// MAGIC We will see an industrial case-study that will illustrate a concrete data science process in action in the sequel.