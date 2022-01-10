---
title: SDS-3.x
permalink: /sds/3/x/
sidebar:
  nav: "lMenu-SDS-3.x"
header:
  overlay_color: "#5e616c"
  overlay_image: /assets/images/DISP-SDS-1600x524.jpg
  caption: 
excerpt: 'Scalable Data Science and Distributed Machine Learning with Apache Spark 3.x and 2.x.<br /><br /><br />{::nomarkdown}<iframe style="display: inline-block;" src="https://ghbtns.com/github-btn.html?user=lamastex&repo=scalable-data-science&type=star&count=true&size=large" frameborder="0" scrolling="0" width="160px" height="30px"></iframe> <iframe style="display: inline-block;" src="https://ghbtns.com/github-btn.html?user=lamastex&repo=scalable-data-science&type=fork&count=true&size=large" frameborder="0" scrolling="0" width="158px" height="30px"></iframe>{:/nomarkdown}'
---

{% include toc %}

# SDS-3.x: Scalable Data Science and Distributed Machine Learning

The course is developed for the [AI-Track of the WASP Graduate School](https://wasp-sweden.org/graduate-school/ai-graduate-school-courses/) and for the Centre for Interdisciplinary Mathematics at Uppsala University. 
It is given in several modules for the *ScadaMaLe-WASP* and *ScaDaMaLe-UU* course instances. 


This site provides course contents for the two instances with multiple deep-dive pathways. 
These contents, instances and pathways are packaged as modules and referred to as **sds-3.x** here.

## ScadaMaLe-UU

This is the instance of the course for students at Uppsala University. It is given in the following modules that are worth 1hp each.

In addition to academic lectures there will be invited guest speakers from industry.

**Module 01** (3 hp) – Introduction to Data Science: Introduction to fault-tolerant distributed file systems and computing.

Prerequisites: Programming experience in at least one programming language and permission of the instructor.

The whole data science process illustrated with industrial case-studies. Practical introduction to scalable data processing to ingest, extract, load, transform, and explore (un)structured datasets. Scalable machine learning pipelines to model, train/fit, validate, select, tune, test and predict or estimate in an unsupervised and a supervised setting using nonparametric and partitioning methods such as random forests. Introduction to distributed vertex-programming.

Contents: [000_1-sds-3-x](#000_1-sds-3-x)

**Module 02** (3 hp) – Distributed Deep Learning: Introduction to the theory and implementation of distributed deep learning.

Prerequisites: Passing Module 01 with active course participation.

Classification and regression using generalised linear models, including different learning, regularization, and hyperparameters tuning techniques. The feedforward deep network as a fundamental network, and the advanced techniques to overcome its main challenges, such as overfitting, vanishing/exploding gradient, and training speed. Various deep neural networks for various kinds of data. For example, the CNN for scaling up neural networks to process large images, RNN to scale up deep neural models to long temporal sequences, and autoencoder and GANs. 

**Module 03** (4 hp) – Project in Data Science

Prerequisites: Module 01 and Module 02 or Module 4.

This module will allow one to explore different domains to solve decision problems 
(eg. prediction, A/B testing, anomaly detection, etc.) with various types of data (eg. time-indexed, space-time-indexed and network-indexed). Privacy-aware decisions with sanitized (cleaned, imputed, anonymised) datasets and datastreams. Practical applications of these algorithms on real-world examples (eg. mobility, social media, machine sensors and logs). Illustration via industrial use-cases.

As we explore different domains, students are encouraged to form groups to do a group project in an application domain we have explored or another they can explore bsed on their preparedness from Modules 01, 02 and 03. Such projects are typically meant to be of direct relevance to a student's research area. 


**Module 04** (6 hp) – Distributed Algorithms and Optimisation (advanced)

This course is for advanced PhD students who have already taken a previous instance of ScaDaMaLe-WASP or ScaDaMaLe-UU (sds and 360-in-525 series in 2017 or 2018) and have the permission of the instructor.

Theoretical foundations of distributed systems and analysis of their scalable algorithms for sorting, joining, streaming, sketching, optimising and computing in numerical linear algebra with applications in scalable machine learning pipelines for typical decision problems.

Here we will be doing a reading course aimed to learn from and refine the 
[dao](https://github.com/lamastex/scalable-data-science/tree/master/read) 
lecture notes of Reza Zadeh at Stanford University.

Module 04 may be combined with Module 03 if the student wants to dive deeper on a theoretical project. 

# Course Content 

Upload Course Content as `.dbc` file into [Databricks Community Edition](https://community.cloud.databricks.com/login.html).

*  [2021 dbc ARCHIVES](https://github.com/lamastex/scalable-data-science/tree/master/dbcArchives/2021) - **to be updated by 20220315**

The databricks notebooks have been made available as the following course modules:

# [000_1-sds-3-x](db/000_1-sds-3-x)

## Introduction to Scalable Data Science and Distributed Machine Learning

**Topics:** *Apache Spark, Scala, RDD, map-reduce, Ingest, Extract, Load, Transform and Explore with noSQL in SparkSQL.*

# [000_2-sds-3-x-ml](db/000_2-sds-3-x-ml)

## Deeper Dive into Distributed Machine Learning

**Topics:**  *Distributed Simulation; various un/supervised ML Algorithms; Linear Algebra; Vertex Programming using SparkML, GraphX and piped-RDDs.*

# [000_3-sds-3-x-st](db/000_3-sds-3-x-st)

## Introduction to Spark Streaming

**Topics:** *Introduction to Spark Streaming with Discrete RDDs and live Experiments in Twitter with Interactive custom D3 Interactions.* 

# [000_4-sds-3-x-ss](db/000_4-sds-3-x-ss)

## Introduction to Spark Structured Streaming

**Topics:** *Introduction to Spark Structured Streaming and Sketching*  

# [000_6-sds-3-x-dl](db/000_6-sds-3-x-dl)

## Introduction to Deep Learning

**Topics:** *Introduction to Deep Learning with Keras, tensorflow, pytorch and PySpark. Topics: TensorFlow Basics, Artificial Neural Networks, Multilayer Deep Feed-Forward Neural Networks, Training, Convolutional Neural Networks, Recurrent Neural Networks like LSTM and GRU, Generative Networks or Patterns, Introduction to Reinforcement Learning, Real-world Operations and MLOps with PyTorch and MLflow for image classification.* 

# [000_7-sds-3-x-ddl](db/000_7-sds-3-x-ddl)

## Introduction to Distributed Deep Learning

**Topics:** *Introduction to Distributed Deep Learning (DDL) with Horovod over Tensorflow/keras and Pytorch. DDL of various CNN architectures for image segmentation with horovod, MLFlow and hyper-parameter tuning through SparkTrials.* 

# [000_8-sds-3-x-pri](db/000_8-sds-3-x-pri)

## Privacy-Preserving Data Science

**Topics:** *Introduction to Privacy-aware Scalable Data Science. Topics: Data Sanitization, Algorithmic Sanitization, GDPR law and its implications for AI/ML, Pseudonymzation and Differential Privacy, Minimax Optimal Procedures for Locally Private Estimation* 

# [000_9-sds-3-x-trends](db/000_9-sds-3-x-trends)

## Trends in Money Market and Media

**Topics:** *Trends in Financial Stocks and News Events using GDELT mass media metadata and yfinance via a Scalably Streamable Trend Calculus. Exploring Events and Persons of Interest at Time Points of Trend Reversals in Commodity Oil Price.*

<!--
15. Additional Topics to bolster by examples and help prepare for group-projects:
  * group-project-01 doing scalable XML-parsing 
    * [Old Bailey Online - ETL of XML](db/033_OBO_LoadExtract/)
    * group-project-17 doing distributed Topological Data Analysis with single-machine-processes via PipedRDDs
  * [Introduction to Piped RDDs](db/006a_PipedRDD/)
    * [Piped RDDs - Rigorous Bayesian AB Testing on Old Bailey Online Data](db/033_OBO_PipedRDD_RigorousBayesianABTesting/)
  * group-projects interested in integrating with mass-media data
    * [Introduction to Spark GDELT-project - Global Mass Media Monitoring](db/030_Spark-GDELT-project/)
  * group-projects doing Latent Dirichlet Allocation
    * [Latent Dirichlet Allocation of Cornell Movie Dialogs](db/035_LDA_CornellMovieDialogs/)
  *  [Spark SQL Windows and Activity Detection by Random Forest](db/021_recognizeActivityByRandomForest/)

7. ETL of GDELT Dataset and XML-structured Dataset
1. Introduction to Spark GDELT-project: Global Mass Media Monitoring
  * [030_Spark-GDELT-project](db/000_2-sds-3-x-ml/030_Spark-GDELT-project/)
  *  [GDELT dataset](db/030_Spark-gdelt-1.1-examples/)
  *  [Old Bailey Online - ETL of XML](db/033_OBO_LoadExtract/)
8. ETL, Exploration and Export of Structured, Semi-Structured and Unstructured Data and Models
  * [Latent Dirichlet Allocation of Cornell Movie Dialogs](db/035_LDA_CornellMovieDialogs/)
  * [MLeap Model Export Demo](db/080_MLeapModelExportDemo/)
  * [Market Basket Analysis via FP Growth](db/081_MarketBasketAnalysisByFPGrowth/)

9. Scalabe Geospatial Analytics
  *  [Geospatial Analytics in Magellan](db/031_GeospatialAnalyticsInMagellan/)
  *  [Open Street Map Ingestion in Magellan](db/031a_MagellanOSMIngestion/)
  *  [NY Taxi trips in Magellan](db/032_NYtaxisInMagellan/)
  *  [Querying Beijin Taxi Trajectories in Magellan](db/032a_MSR_BeijingTaxiTrajectories_MagellanQueries/)
  *  [Map-matching and Visualizing Uber Trajectories](db/032b_UberMapMatchingAndVisualization/)
12. Privacy and GDPR-compliant Machine Learning
  *  [Intro to Privacy-preserving Machine Learning](db/01_privacyPreservingDataScience/)
  *  [Privacy and Pseudonomyzation](db/02_privacyPreservingDataScience_pseudonomyzation/)
  *  [Differential Privacy](db/03_privacyPreservingDataScience_Differential_Privacy/)


**Possible Topics to Choose From:**

#### NOTE: Most links below will NOT work until we calibrate the content interactively. But you can get the contents from dbc ARCHIVES above.


23. Advise from Industry
  *  [2017 Advise from Data Industry](adviseFromIndustry/AndrewMorgan/)
24. Project Ideas
  *  [Potential Projects](db/998_01_PotentialProjectIdeas/)
25. Student Projects
  *  [Student Project 01 on Network Anomaly Detection](db/999_01_StudentProject_NetworkAnomalyDetection/)
  *  [Student Project 02 on Twitter UK Election](db/999_02_StudentProject_Twitter_UKElection2017/)
  *  [Student Project 03 on Article Topics in Retweet Networks](db/999_03_StudentProject_ArticleTopicInRTNetwork/)
  *  [Student Project 03 on Article Topics in Retweet Networks - scalable web scraper](db/999_03_StudentProject_ArticleTopicInRTNetwork_webScraping/)
  *  [Student Project 04 on Power Forecasting - Part 0](db/999_04_StudentProject_PowerForecasting_0_Introduction/)
  *  [Student Project 04 on Power Forecasting - Part 1](db/999_04_StudentProject_PowerForecasting_1_LoadDataFromJSON/)
  *  [Student Project 04 on Power Forecasting - Part 2](db/999_04_StudentProject_PowerForecasting_2_CreateTrainingData/)
  *  [Student Project 04 on Power Forecasting - Part 3](db/999_04_StudentProject_PowerForecasting_3_TrainAndValidate/)
  *  [Student Project 05 on Hail Scala for Population Genomics ETL](db/999_05_StudentProject_HailScalaGenomicsETLTutorial/)
-->

<!--
Optional materials 11. Spark Performance Tuning
  *  [Tuning Utilities](db/00_dbcTuningUtilities2run/)
  *  [Tuning Transformations and Actions](db/01_TransformationsActions/)
  *  [Tuning Utilities in Action](db/02_tuningUtilitiesInAction/)
  *  [Tuning for Caching](db/03_caching/)
  *  [Tuning for Partitioning](db/04_partitioning/)

10. Natural Language Processing
  *  [Intro to NLP](db/00_Intro2NLP)
  *  [Getting Started with SparkNLP](db/01_GettingStarted_SparkNLP/)
  *  [Annotations with Pretrained Pipelines](db/02_annotation_1_PreTrainedPipelines/)
  *  [Named Entity Recognitions with Pretrained Models](db/02_annotation_2_NerDL_WordEmbeddingsPreTrainedModels/)
  *  [Tain Lemmatizer Model in Italian](db/03_training_1_TrainLemmatizerModelInItalian/)
  *  [Training French POS Tagger](db/03_training_2_TrainPOSTagger_French_UniversalDependency/)
  *  [Evaluate French POS Model](db/04_EvaluateFrenchPOSModelBySparkMultiClassMetrics/)
-->

# Reference Readings

* [dao](https://github.com/lamastex/scalable-data-science/tree/master/read)
* [https://learning.oreilly.com/library/view/high-performance-spark/9781491943199/](https://learning.oreilly.com/library/view/high-performance-spark/9781491943199/)
* [https://learning.oreilly.com/library/view/spark-the-definitive/9781491912201/](https://learning.oreilly.com/library/view/spark-the-definitive/9781491912201/)
* [https://learning.oreilly.com/library/view/learning-spark-2nd/9781492050032/](https://learning.oreilly.com/library/view/learning-spark-2nd/9781492050032/)
* Introduction to Algorithms, Third Edition, Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, and Clifford Stein from 
  - [https://ebookcentral.proquest.com/lib/uu/reader.action?docID=3339142](https://ebookcentral.proquest.com/lib/uu/reader.action?docID=3339142)

## Supplements

Several freely available MOOCs, hyperlinks and reference books are used to bolster the learning experience. Plese see references to such additional supplemantary resources in the above content.

We will be supplementing the lecture notes with reading assignments from original sources.  

Here are some resources that may be of further help.

### Recommended Preparations for Data *Engineering* Scientists: 

- You should have already installed docker and gone through the [setup and preparation instructions for TASK 2](https://lamastex.github.io/scalable-data-science/sds/basics/instructions/prep/).
- Successfully complete at least the SKINNY `docker-compose` steps 1-5 in [Quick Start](https://lamastex.github.io/scalable-data-science/sds/basics/infrastructure/onpremise/dockerCompose/readmes/startingNotes/)

1. Complete the [sbt tutorial](https://lamastex.github.io/scalable-data-science/sds/basics/infrastructure/onpremise/dockerCompose/readmes/sbt_tutorial/)
1. Complete the exercises to [use sbt and spark-submit packaged jars to a yarn-managed hdfs-based spark cluster](https://github.com/lamastex/scalable-data-science/tree/master/_sds/basics/infrastructure/onpremise/dockerCompose/programs).

Please be self-directed and try out more complex exercises on your own either in the databricks community edition and/or in sbt in the local hadoop service.

### Mathematical Statistical Foundations
- Avrim Blum, John Hopcroft and Ravindran Kannan.  Foundations of Data Science. Freely available from: [https://www.cs.cornell.edu/jeh/book2016June9.pdf](https://www.cs.cornell.edu/jeh/book2016June9.pdf). It is  intended as a modern theoretical course in computer science and statistical learning.
- Kevin P. Murphy.  Machine Learning:  A Probabilistic Perspective.  ISBN 0262018020.  2013.
- Trevor  Hastie,  Robert  Tibshirani  and  Jerome  Friedman.   Elements  of  Statistical  Learning, Second Edition.  ISBN 0387952845.  2009.  Freely available from: [https://statweb.stanford.edu/~tibs/ElemStatLearn/](https://statweb.stanford.edu/~tibs/ElemStatLearn/).

### Data Science / Data Mining at Scale

- Jure Leskovek,  Anand Rajaraman and Jeffrey Ullman.  Mining of Massive Datasets.  v2.1, Cambridge University Press.  2014.  Freely available from: [http://www.mmds.org/#ver21](http://www.mmds.org/#ver21).
- Foster Provost and Tom Fawcett.  Data Science for Business:  What You Need to Know about Data Mining and Data-analytic Thinking.  ISBN 1449361323.  2013.
- Mohammed J. Zaki and Wagner Miera Jr.  Data Mining and Analysis: Fundamental Concepts and Algorithms.  Cambridge University Press.  2014.
- Cathy  O’Neil  and  Rachel  Schutt.   Doing  Data  Science,  Straight  Talk  From  The  Frontline. O’Reilly.  2014.

Here are some free online courses if you need quick refreshers or want to go indepth into specific subjects.


### Apache Spark / shell / github / Scala / Python / Tensorflow / R

- Learning Spark : lightning-fast data analytics by Holden Karau, Andy Konwinski, Patrick Wendell, and Matei Zaharia, O'Reilly, 2015.
- Advanced analytics with Spark : patterns for learning from data at scale, O'Reilly, 2015.
- Command-line Basics
  * [Linux Commnad-line Basics](https://www.udacity.com/course/linux-command-line-basics--ud595)
  * [Windows Command-line Bascis](https://www.lynda.com/-tutorials/Windows-command-line-basics/497312/513424-4.html)

- [How to use Git and GitHub: Version control for code](https://www.udacity.com/course/how-to-use-git-and-github--ud775)
- [Intro to Data Analysis: Using NumPy and Pandas](https://www.udacity.com/course/intro-to-data-analysis--ud170)
- [Data Analysis with R by facebook](https://www.udacity.com/course/data-analysis-with-r--ud651)
- [Machine Learning Crash Course with TensorFlow APIs by Google Developers](https://developers.google.com/machine-learning/crash-course/)
- [Data Visualization and D3.js](https://www.udacity.com/course/data-visualization-and-d3js--ud507)
- [Scala Programming](http://www.scala-lang.org/documentation/)
- [Scala for Data Science, Pascal Bugnion, Packt Publishing, 416 pages, 2016](http://shop.oreilly.com/product/9781785281372.do). 

### Computer Science Refreshers

* [Intro to Computer Science (with Python)](https://www.udacity.com/course/intro-to-computer-science--cs101)
* [Intro to Python Programming](https://www.udacity.com/course/programming-foundations-with-python--ud036)
* [Intro to Relational Databases](https://www.udacity.com/course/intro-to-relational-databases--ud197)


## ScadaMaLe-WASP 

This instance for  the [AI-Track of the WASP Graduate School](https://wasp-sweden.org/graduate-school/ai-graduate-school-courses/)is divided into the following three modules.

In addition to academic lectures there will be invited guest speakers from industry.

**Module 01** (2 hp) – Introduction to Data Science: Introduction to fault-tolerant distributed file systems and computing.

Prerequisites: Programming experience in at least one programming language and permission of the instructor.

The whole data science process illustrated with industrial case-studies. Practical introduction to scalable data processing to ingest, extract, load, transform, and explore (un)structured datasets. Scalable machine learning pipelines to model, train/fit, validate, select, tune, test and predict or estimate in an unsupervised and a supervised setting using nonparametric and partitioning methods such as random forests. Introduction to distributed vertex-programming.

**Module 02** (2 hp) – Distributed Deep Learning: Introduction to the theory and implementation of distributed deep learning.

Prerequisites: Passing Module 01 with active course participation.

Classification and regression using generalised linear models, including different learning, regularization, and hyperparameters tuning techniques. The feedforward deep network as a fundamental network, and the advanced techniques to overcome its main challenges, such as overfitting, vanishing/exploding gradient, and training speed. Various deep neural networks for various kinds of data. For example, the CNN for scaling up neural networks to process large images, RNN to scale up deep neural models to long temporal sequences, and autoencoder and GANs. 

**Module 03** (2 hp) – Decision-making with Scalable Algorithms

Theoretical foundations of distributed systems and analysis of their scalable algorithms for sorting, joining, streaming, sketching, optimising and computing in numerical linear algebra with applications in scalable machine learning pipelines for typical decision problems (eg. prediction, A/B testing, anomaly detection) with various types of data (eg. time-indexed, space-time-indexed and network-indexed). Privacy-aware decisions with sanitized (cleaned, imputed, anonymised) datasets and datastreams. Practical applications of these algorithms on real-world examples (eg. mobility, social media, machine sensors and logs). Illustration via industrial use-cases. 
