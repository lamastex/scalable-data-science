// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Introduction
// MAGIC 
// MAGIC - **Course Name:** *Scalable Data Science and Distributed Machine Learning*
// MAGIC - **Course Acronym:** *ScaDaMaLe* or *sds-3.x*.
// MAGIC 
// MAGIC The course was designed to be the fifth and final mandatory course in the [AI-Track of the WASP Graduate School](https://wasp-sweden.org/graduate-school/ai-graduate-school-courses/) in 2021. *From 2022 ScaDaMaLe is an optional course for WASP students who have successfully completed the mandatory courses*. 
// MAGIC It is given in three modules. In addition to academic lectures there are invited guest speakers from industry.
// MAGIC 
// MAGIC The course can also be taken by select post-graduate students at Uppsala University as a *Special Topics Course* from the Department of Mathematics.
// MAGIC 
// MAGIC This site provides course contents for the three modules. This content is referred to as **sds-3.x** here.
// MAGIC 
// MAGIC **Module 1** – Introduction to Data Science: Introduction to fault-tolerant distributed file systems and computing.
// MAGIC 
// MAGIC The whole data science process illustrated with industrial case-studies. Practical introduction to scalable data processing to ingest, extract, load, transform, and explore (un)structured datasets. Scalable machine learning pipelines to model, train/fit, validate, select, tune, test and predict or estimate in an unsupervised and a supervised setting using nonparametric and partitioning methods such as random forests. Introduction to distributed vertex-programming.
// MAGIC 
// MAGIC **Module 2** – Distributed Deep Learning: Introduction to the theory and implementation of distributed deep learning.
// MAGIC 
// MAGIC Classification and regression using generalised linear models, including different learning, regularization, and hyperparameters tuning techniques. The feedforward deep network as a fundamental network, and the advanced techniques to overcome its main challenges, such as overfitting, vanishing/exploding gradient, and training speed. Various deep neural networks for various kinds of data. For example, the CNN for scaling up neural networks to process large images, RNN to scale up deep neural models to long temporal sequences, and autoencoder and GANs. 
// MAGIC 
// MAGIC **Module 3** – Decision-making with Scalable Algorithms
// MAGIC 
// MAGIC Theoretical foundations of distributed systems and analysis of their scalable algorithms for sorting, joining, streaming, sketching, optimising and computing in numerical linear algebra with applications in scalable machine learning pipelines for typical decision problems (eg. prediction, A/B testing, anomaly detection) with various types of data (eg. time-indexed, space-time-indexed and network-indexed). Privacy-aware decisions with sanitized (cleaned, imputed, anonymised) datasets and datastreams. Practical applications of these algorithms on real-world examples (eg. mobility, social media, machine sensors and logs). Illustration via industrial use-cases. 
// MAGIC 
// MAGIC 
// MAGIC ## Expected Reference Readings 
// MAGIC 
// MAGIC Note that you need to be logged into your library with access to these publishers:
// MAGIC 
// MAGIC * [https://learning.oreilly.com/library/view/high-performance-spark/9781491943199/](https://learning.oreilly.com/library/view/high-performance-spark/9781491943199/)
// MAGIC * [https://learning.oreilly.com/library/view/spark-the-definitive/9781491912201/](https://learning.oreilly.com/library/view/spark-the-definitive/9781491912201/)
// MAGIC * [https://learning.oreilly.com/library/view/learning-spark-2nd/9781492050032/](https://learning.oreilly.com/library/view/learning-spark-2nd/9781492050032/)
// MAGIC * Introduction to Algorithms, Third Edition, Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, and Clifford Stein from 
// MAGIC   - [https://ebookcentral.proquest.com/lib/uu/reader.action?docID=3339142](https://ebookcentral.proquest.com/lib/uu/reader.action?docID=3339142)
// MAGIC * [Reading Materials Provided](https://github.com/lamastex/scalable-data-science/tree/master/read)
// MAGIC 
// MAGIC ## Course Contents
// MAGIC The databricks notebooks will be made available as the course progresses in the :
// MAGIC - course site at:
// MAGIC   - [[site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)](https://lamastex.github.io/scalable-data-science/sds/3/x/) 
// MAGIC - and course book at:
// MAGIC   - [https://lamastex.github.io/ScaDaMaLe/index.html](https://lamastex.github.io/ScaDaMaLe/index.html)
// MAGIC 
// MAGIC - You may upload Course Content into Databricks Community Edition from:
// MAGIC   - [2021 dbc ARCHIVES](https://github.com/lamastex/scalable-data-science/tree/master/dbcArchives/2021)
// MAGIC 
// MAGIC ## Course Assessment
// MAGIC 
// MAGIC There will be minimal reading and coding exercises that will not be graded. The main assessment will be based on a peer-reviewed group project. 
// MAGIC The group project will include notebooks/codes along with a video of the project presentation. Each group cannot have more than four members and should be seen as an opportunity to do something you are passionate about or interested in, as opposed to completing and auto-gradeable programming assessment in the shortest amount of time. 
// MAGIC 
// MAGIC Detailed instructions will be given in the sequel.
// MAGIC   
// MAGIC ## Course Sponsors
// MAGIC 
// MAGIC The course builds on contents developed since 2016 with support from New Zealand's Data Industry. The 2017-2019 versions were academically sponsored by Uppsala University's Inter-Faculty Course grant, Department of Mathematics and The Centre for Interdisciplinary Mathematics and industrially sponsored by [databricks](https://databricks.com), [AWS](https://aws.amazon.com/) and Swedish data industry via [Combient AB](https://combient.com), [SEB](https://seb.se/) and [Combient Mix AB](https://combient.com/mix). This 2021 version is academically sponsored by [AI-Track of the WASP Graduate School](https://wasp-sweden.org/graduate-school/ai-graduate-school-courses/) and [Centre for Interdisciplinary Mathematics](https://www.math.uu.se/research/cim/) and industrially sponsored by [databricks](https://databricks.com) and [AWS](https://aws.amazon.com/) via *databricks University Alliance* and [Combient Mix AB](https://combient.com/mix) via industrial mentorships.
// MAGIC 
// MAGIC 
// MAGIC ## Course Instructor
// MAGIC 
// MAGIC I, Raazesh Sainudiin or **Raaz**, will be an instructor for the course.
// MAGIC 
// MAGIC I have 
// MAGIC 
// MAGIC * more than 15 years of academic research experience in applied mathematics and statistics and 
// MAGIC * over 3 and 5 years of full-time and part-time experience in the data industry.
// MAGIC 
// MAGIC I currently (2020) have an effective joint appointment as:
// MAGIC 
// MAGIC * [Associate Professor of Mathematics with specialisation in Data Science](https://katalog.uu.se/profile/?id=N17-214) at [Department of Mathematics](https://www.math.uu.se/), [Uppsala University](https://www.uu.se/), Uppsala, Sweden and
// MAGIC * Director, Technical Strategy and Research at [Combient Mix AB](https://combient.com/mix), Stockholm, Sweden
// MAGIC 
// MAGIC Quick links on Raaz's background:
// MAGIC 
// MAGIC * [https://www.linkedin.com/in/raazesh-sainudiin-45955845/](https://www.linkedin.com/in/raazesh-sainudiin-45955845/) 
// MAGIC * [Raaz's academic CV](https://lamastex.github.io/cv/) 
// MAGIC * [Raaz's publications list](https://lamastex.github.io/publications/)
// MAGIC 
// MAGIC 
// MAGIC ## Industrial Case Study
// MAGIC 
// MAGIC We will see an industrial case-study that will illustrate a concrete **data science process** in action in the sequel.

// COMMAND ----------

// MAGIC %md 
// MAGIC # What is the [Data Science Process](https://en.wikipedia.org/wiki/Data_science) 
// MAGIC 
// MAGIC **The Data Science Process in one picture**
// MAGIC 
// MAGIC ![what is sds?](https://github.com/lamastex/scalable-data-science/raw/master/assets/images/sds.png "sds")
// MAGIC 
// MAGIC ---
// MAGIC 
// MAGIC ## What is scalable data science and distributed machine learning?
// MAGIC 
// MAGIC Scalability merely refers to the ability of the data science process to scale to massive datasets (popularly known as *big data*). 
// MAGIC 
// MAGIC For this we need *distributed fault-tolerant computing* typically over large clusters of commodity computers -- the core infrastructure in a public cloud today.
// MAGIC 
// MAGIC *Distributed Machine Learning* allows the models in the data science process to be scalably trained and extract value from big data. 

// COMMAND ----------

// MAGIC %md
// MAGIC ## What is Data Science?
// MAGIC 
// MAGIC It is increasingly accepted that [Data Science](https://en.wikipedia.org/wiki/Data_science)
// MAGIC 
// MAGIC > is an inter-disciplinary field that uses scientific methods, processes, algorithms and systems to extract knowledge and insights from many structural and unstructured data. Data science is related to data mining, machine learning and big data.
// MAGIC 
// MAGIC > Data science is a "concept to unify statistics, data analysis and their related methods" in order to "understand and analyze actual phenomena" with data. It uses techniques and theories drawn from many fields within the context of mathematics, statistics, computer science, domain knowledge and information science. Turing award winner Jim Gray imagined data science as a "fourth paradigm" of science (empirical, theoretical, computational and now data-driven) and asserted that "everything about science is changing because of the impact of information technology" and the data deluge.
// MAGIC 
// MAGIC Now, let us look at two industrially-informed academic papers that influence the above quote on what is Data Science, but with a view towards the contents and syllabus of this course.
// MAGIC 
// MAGIC Source: [Vasant Dhar, Data Science and Prediction, Communications of the ACM, Vol. 56 (1). p. 64, DOI:10.1145/2500499](https://dl.acm.org/citation.cfm?id=2500499)
// MAGIC 
// MAGIC **key insights in the above paper** 
// MAGIC 
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
// MAGIC Source: [Machine learning: Trends, perspectives, and prospects, M. I. Jordan, T. M. Mitchell, Science  17 Jul 2015: Vol. 349, Issue 6245, pp. 255-260, DOI: 10.1126/science.aaa8415](https://www.science.org/doi/pdf/10.1126/science.aaa8415)
// MAGIC 
// MAGIC **key insights in the above paper** 
// MAGIC 
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
// MAGIC 
// MAGIC 
// MAGIC But what is Data Engineering (including Machine Learning Engineering and Operations) and how does it relate to Data Science?

// COMMAND ----------

// MAGIC %md
// MAGIC ## Data Engineering
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
// MAGIC ## The Data Engineering Scientist as "The Middle Way"
// MAGIC 
// MAGIC Here are some basic axioms that should be self-evident.
// MAGIC 
// MAGIC - Yes, there are differences in skillsets across humans
// MAGIC   - some humans will be better and have inclinations for engineering and others for pure mathematics by nature and nurture
// MAGIC   - one human cannot easily be a master of everything needed for innovating a new data-based product or service (very very rarely though this happens)
// MAGIC - Skills can be gained by any human who wants to learn to the extent s/he is able to expend time, energy, etc.
// MAGIC 
// MAGIC For the **Scalable Data Engineering Science Process:** *towards Production-Ready and Productisable Prototyping for the Data-based Factory* we need to allow each data engineer to be more of a data scientist and each data scientist to be more of a data engineer, up to each individual's *comfort zones* in technical and mathematical/conceptual and time-availability planes, but with some **minimal expectations** of mutual appreciation.
// MAGIC 
// MAGIC This course is designed to help you take the first minimal steps towards such a **data engineering science**.
// MAGIC 
// MAGIC In the sequel it will become apparent **why a team of data engineering scientists** with skills across the conventional (2021) spectrum of data engineer versus data scientist  **is crucial** for **Production-Ready and Productisable Prototyping for the Data-based Factory**, whose outputs include standard AI products today.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Standing on shoulders of giants!
// MAGIC 
// MAGIC This course was originally structured from two other edX courses from 2015. Unfortunately, these courses and their content,including video lectures and slides, are not available openly any longer.
// MAGIC 
// MAGIC * BerkeleyX/CS100-1x, Introduction to Big Data Using Apache Spark by Anthony A Joseph, Chancellor's Professor, UC Berkeley
// MAGIC * BerkeleyX/CS190-1x, Scalable Machine Learning by Ameet Talwalkar, Ass. Prof., UC Los Angeles
// MAGIC 
// MAGIC This course will be an *expanded and up-to-date scala version* with an emphasis on *individualized course project* as opposed to completing labs that test sytactic skills that are auto-gradeable. 
// MAGIC 
// MAGIC We will also be borrowing more theoretical aspects from the following course:
// MAGIC 
// MAGIC * [Stanford/CME323, Distributed Algorithms and Optimization by Reza Zadeh, Ass. Prof., Institute for Computational and Mathematical Engineering, Stanford Univ.](https://stanford.edu/~rezab/dao/)
// MAGIC 
// MAGIC Note the **Expected Reference Readings** above for this course.

// COMMAND ----------

// MAGIC %md
// MAGIC # A Brief Tour of Data Science
// MAGIC ## History of Data Analysis and Where Does "Big Data" Come From?
// MAGIC 
// MAGIC *  A Brief History and Timeline of Data Analysis and Big Data
// MAGIC   * [https://en.wikipedia.org/wiki/Big_data](https://en.wikipedia.org/wiki/Big_data)
// MAGIC   * [https://whatis.techtarget.com/feature/A-history-and-timeline-of-big-data](https://whatis.techtarget.com/feature/A-history-and-timeline-of-big-data)
// MAGIC   
// MAGIC *  Where does Data Come From?
// MAGIC   
// MAGIC   * Some of the sources of big data.
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
// MAGIC ## Data Science with Cloud Computing and What's Hard about it?
// MAGIC 
// MAGIC * See [Cloud Computing](https://en.wikipedia.org/wiki/Cloud_computing) to understand the work-horse for analysing big data at [data centers](https://en.wikipedia.org/wiki/Data_center)
// MAGIC 
// MAGIC > Cloud computing is the on-demand availability of computer system resources, especially data storage (cloud storage) and computing power, without direct active management by the user. Large clouds often have functions distributed over multiple locations, each location being a data center. Cloud computing relies on sharing of resources to achieve coherence and economies of scale, typically using a "pay-as-you-go" model which can help in reducing capital expenses but may also lead to unexpected operating expenses for unaware users.
// MAGIC 
// MAGIC * In fact, if you are logged into `https://*.databricks.com/*` you are computing in the cloud! So the computations are actually running in an instance of the hardware available at a data center like the following:
// MAGIC 
// MAGIC   * ![](https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/CERN_Server_03.jpg/250px-CERN_Server_03.jpg)
// MAGIC 
// MAGIC * Here is a data center used by CERN in 2010.
// MAGIC   * ![](https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Cern_datacenter.jpg/450px-Cern_datacenter.jpg)
// MAGIC   
// MAGIC   
// MAGIC 
// MAGIC * What's hard about scalable data science in the cloud?
// MAGIC   *  To analyse datasets that are big, say more than a few TBs, we need to split the data and put it in several computers that are networked - *a typical cloud *
// MAGIC     * ![](https://upload.wikimedia.org/wikipedia/commons/thumb/d/de/Wikimedia_Foundation_Servers-8055_03_square.jpg/120px-Wikimedia_Foundation_Servers-8055_03_square.jpg)
// MAGIC   * However, as the number of computer nodes in such a network increases, the probability of hardware failure or fault (say the hard-disk or memory or CPU or switch breaking down) also increases and can happen while the computation is being performed
// MAGIC   * Therefore for scalable data science, i.e., data science that can scale with the size of the input data by adding more computer nodes, we need *fault-tolerant computing and storage framework at the software level* to ensure the computations finish even if there are hardware faults. 

// COMMAND ----------

// MAGIC %md
// MAGIC Here is a recommended light reading on **What is "Big Data" -- Understanding the History** (18 minutes):
// MAGIC   - [https://towardsdatascience.com/what-is-big-data-understanding-the-history-32078f3b53ce](https://towardsdatascience.com/what-is-big-data-understanding-the-history-32078f3b53ce)
// MAGIC   

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ---
// MAGIC 
// MAGIC ---

// COMMAND ----------

// MAGIC %md
// MAGIC # What should *you* be able to do at the end of this course?
// MAGIC 
// MAGIC By following these online interactions in the form of lab/lectures, asking questions, engaging in discussions, doing HOMEWORK assignments and completing the group project, you should be able to:
// MAGIC 
// MAGIC * Understand the principles of fault-tolerant scalable computing in Spark
// MAGIC   * in-memory and generic DAG extensions of Map-reduce
// MAGIC   * resilient distributed datasets for fault-tolerance
// MAGIC   * skills to process today's big data using state-of-the art techniques in Apache Spark 3.0, in terms of:
// MAGIC     * hands-on coding with realistic datasets
// MAGIC     * an intuitive understanding of the ideas behind the technology and methods
// MAGIC     * pointers to academic papers in the literature, technical blogs and video streams for *you to futher your theoretical understanding*.
// MAGIC * More concretely, you will be able to:
// MAGIC   * Extract, Transform, Load, Interact, Explore and Analyze Data
// MAGIC   * Build Scalable Machine Learning Pipelines (or help build them) using  Distributed Algorithms and Optimization
// MAGIC * How to keep up?
// MAGIC   - This is a fast-changing world. 
// MAGIC   - Recent videos around Apache Spark are archived here (these videos are a great way to learn the latest happenings in industrial R&D today!):
// MAGIC     - [https://databricks.com/sparkaisummit](https://databricks.com/sparkaisummit)
// MAGIC * What is mathematically stable in the world of 'big data'?
// MAGIC   - There is a growing body of work on the analysis of parallel and distributed algorithms, the work-horse of big data and AI. 
// MAGIC   - We will see some of this in a theoretical module later, but the immediate focus here is on how to write programs and analyze data.
