// Databricks notebook source exported at Thu, 7 Jul 2016 05:00:57 UTC
// MAGIC %md
// MAGIC Scalable Data Science - Course Overview
// MAGIC =======
// MAGIC 
// MAGIC Scalable data science is a technical course in the area of Big Data, aimed at the needs of the
// MAGIC emerging data industry in Christchurch and those of certain academic domain experts across
// MAGIC University of Canterbury's Colleges, including, Arts, Science and Engineering. This course uses
// MAGIC Apache Spark, a fast and general engine for large-scale data processing via databricks to compute
// MAGIC with datasets that won't fit in a single computer. The course will introduce Spark’s core concepts
// MAGIC via hands-on coding, including resilient distributed datasets and map-reduce algorithms, DataFrame
// MAGIC and Spark SQL on Catalyst, scalable machine-learning pipelines in MlLib and vertex programs using
// MAGIC the distributed graph processing framework of GraphX. We will solve instances of real-world big data
// MAGIC decision problems from various scientific domains.
// MAGIC 
// MAGIC This is being prepared by Raazesh Sainudiin and Sivanand Sivaram
// MAGIC with assistance from Paul Brouwers, Dillon George and Ivan Sadikov.
// MAGIC 
// MAGIC All course projects by seven enrolled and four observing students for Semester 1 of 2016 at UC, Ilam are part of this content.
// MAGIC 
// MAGIC ## How to self-learn this content?
// MAGIC 
// MAGIC The 2016 instance of this [scalable-data-science course](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) finished on June 30 2016.
// MAGIC 
// MAGIC To learn Apache Spark for free try **databricks Community edition** by starting from [https://databricks.com/try-databricks](https://databricks.com/try-databricks).
// MAGIC 
// MAGIC All course content can be uploaded for self-paced learning by copying the following [URL for 2016/Spark1_6_to_1_3/scalable-data-science.dbc archive](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/dbcArchives/2016/Spark1_6_to_1_3/scalable-data-science.dbc)
// MAGIC and importing it from the URL to your [free Databricks Community Edition](https://community.cloud.databricks.com).
// MAGIC 
// MAGIC The Gitbook version of this content is [https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details](https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details).
// MAGIC 
// MAGIC ## Contribute
// MAGIC 
// MAGIC All course content is currently being pushed by Raazesh Sainudiin after it has been tested in
// MAGIC Databricks cloud (mostly under Spark 1.6 and some involving Magellan under Spark 1.5.1).
// MAGIC 
// MAGIC The markdown version for `gitbook` is generated from the Databricks `.scala` and other source codes.
// MAGIC The gitbook version will lag behind the Databricks version available in the Databricks cloud. The following issues need to be resolved:
// MAGIC 
// MAGIC * dialects of Tex between databricks and github are different (need to babel out into github-compatible tex from databricks compatible tex)
// MAGIC * need to find a stable solution for the output of various databricks cells to be shown in gitbook, including those from `display_HTML` and `frameIt` with their in-place embeds of web content.
// MAGIC 
// MAGIC Please feel free to fork the github repository:
// MAGIC 
// MAGIC * [https://github.com/raazesh-sainudiin/scalable-data-science](https://github.com/raazesh-sainudiin/scalable-data-science).
// MAGIC 
// MAGIC Unfortunately, pull requests cannot be accepted until the end of June 2016 when the course completes
// MAGIC along with the course projects.
// MAGIC 
// MAGIC Furthermore, due to the anticipation of Spark 2.0 this mostly Spark 1.6 version could be enhanced with a 2.0 version-specific upgrade.
// MAGIC 
// MAGIC Please send any typos or suggestions to raazesh.sainudiin@gmail.com
// MAGIC 
// MAGIC Please read a note on [babel](https://github.com/raazesh-sainudiin/scalable-data-science/blob/master/babel/README.md) to understand how the gitbook is generated from the `.scala` source of the databricks notebook.
// MAGIC 
// MAGIC ## How to cite this work?
// MAGIC 
// MAGIC Scalable Data Science, Raazesh Sainudiin and Sivanand Sivaram, Published by GitBook [https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details](https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details), 787 pages, 30th June 2016.
// MAGIC 
// MAGIC ## Supported By
// MAGIC [Databricks Academic Partners Program](https://databricks.com/academic) and [Amazon Web Services Educate](https://www.awseducate.com/microsite/CommunitiesEngageHome).
// MAGIC 
// MAGIC Raazesh Sainudiin,
// MAGIC Laboratory for Mathematical Statistical Experiments, Christchurch Centre
// MAGIC and School of Mathematics and Statistics,
// MAGIC University of Canterbury,
// MAGIC Private Bag 4800,
// MAGIC Christchurch 8041,
// MAGIC Aotearoa New Zealand
// MAGIC 
// MAGIC Sun Jun 19 21:59:19 NZST 2016

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Summary
// MAGIC 
// MAGIC Note: these links need to be updated if `scalable-data-science` folder is not in `Workspace` folder.
// MAGIC 
// MAGIC * [Prelude](#workspace/scalable-data-science/sdsBlog)
// MAGIC 
// MAGIC * [Week 1: Introduction to Scalable Data Science](#workspace/scalable-data-science/week1)
// MAGIC     * [Scalable Data Science](#workspace/scalable-data-science/week1/01_introduction/000_scalableDataScience)
// MAGIC     * [Why Spark?](#workspace/scalable-data-science/week1/01_introduction/001_whySpark)
// MAGIC     * [Login to databricks](#workspace/scalable-data-science/week1/01_introduction/002_loginToDatabricks)
// MAGIC     * [Scala Crash Course](#workspace/scalable-data-science/week1/01_introduction/003_scalaCrashCourse)
// MAGIC 
// MAGIC * [Week 2: Introduction to Spark RDDs, Transformations and Actions and Word Count of the US State of the Union Addresses](#workspace/scalable-data-science/week2)
// MAGIC     * [RDDs, Transformations and Actions](#workspace/scalable-data-science/week2/02_SparkEssentials/004_RDDsTransformationsActions)
// MAGIC     * [HOMEWORK: RDDs, Transformations and Actions](#workspace/scalable-data-science/week2/02_SparkEssentials/005_RDDsTransformationsActionsHOMEWORK)
// MAGIC     * [Word Count: US State of Union Addesses](#workspace/scalable-data-science/week2/03_WordCount/006_WordCount)
// MAGIC     * [EXTRA_Word Count: ETL of US State of Union Addesses](#workspace/scalable-data-science/xtraResources/sdsDatasets/scraperUSStateofUnionAddresses)
// MAGIC 
// MAGIC * [Week 3: Introduction to Spark SQL, ETL and EDA of Diamonds, Power Plant and Wiki CLick Streams Data](#workspace/scalable-data-science/week3/)
// MAGIC     * [Spark SQL Introduction](#workspace/scalable-data-science/week3/04_SparkSQLIntro/007_SparkSQLIntroBasics)
// MAGIC         * [HOMEWORK: overview](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide)
// MAGIC         * [HOMEWORK: getting started](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide)
// MAGIC         * [HOMEWORK: data sources](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/003_dataSources_sqlProgGuide)
// MAGIC         * [HOMEWORK: performance tuning](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)
// MAGIC         * [HOMEWORK: distributed sql engine](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)
// MAGIC     * [ETL and EDA of Diamonds Data](#workspace/scalable-data-science/week3/05_SparkSQLETLEDA/008_DiamondsPipeline_01ETLEDA)
// MAGIC     * [ETL and EDA of Power Plant Data](#workspace/scalable-data-science/week3/05_SparkSQLETLEDA/009_PowerPlantPipeline_01ETLEDA)
// MAGIC     * [ETL and EDA of Wiki Click Stream Data](#workspace/scalable-data-science/week3/05_SparkSQLETLEDA/010_wikipediaClickStream_01ETLEDA)
// MAGIC 
// MAGIC * [Week 4: Introduction to Machine Learning - Unsupervised Clustering and Supervised Classification](#workspace/scalable-data-science/week4/)
// MAGIC     * [Introduction to Machine Learning](#workspace/scalable-data-science/week4/06_MLIntro/011_IntroToML)
// MAGIC     * [Unsupervised Clustering of 1 Million Songs via K-Means in 3 Stages](#workspace/scalable-data-science/week4/07_UnsupervisedClusteringKMeans_1MSongs/012_1MSongsKMeans_Intro)
// MAGIC         * [Stage 1: Extract-Transform-Load](#workspace/scalable-data-science/week4/07_UnsupervisedClusteringKMeans_1MSongs/013_1MSongsKMeans_Stage1ETL)
// MAGIC         * [Stage 2: Explore](#workspace/scalable-data-science/week4/07_UnsupervisedClusteringKMeans_1MSongs/014_1MSongsKMeans_Stage2Explore)
// MAGIC         * [Stage 3: Model](#workspace/scalable-data-science/week4/07_UnsupervisedClusteringKMeans_1MSongs/015_1MSongsKMeans_Stage3Model)
// MAGIC     * [Supervised Classification of Hand-written Digits via Decision Trees](#workspace/scalable-data-science/week4/08_SupervisedLearningDecisionTrees/016_DecisionTrees_HandWrittenDigitRecognition)
// MAGIC 
// MAGIC * [Week 5: Introduction to Non-distributed and Distributed Linear Algebra and Applied Linear Regression](#workspace/scalable-data-science/week5/)
// MAGIC     * [Linear Algebra Introduction](#workspace/scalable-data-science/week5/09_LinearAlgebraIntro/017_LAlgIntro)
// MAGIC         * [HOMEWORK: breeze linear algebra cheat sheet](#workspace/scalable-data-science/xtraResources/LinearAlgebra/LAlgCheatSheet)
// MAGIC     * [Linear Regression Introduction](#workspace/scalable-data-science/week5/10_LinearRegressionIntro/018_LinRegIntro)
// MAGIC     * [Distributed Linear Algebra for Linear Regression Introduction](#workspace/scalable-data-science/week5/10_LinearRegressionIntro/019_DistLAlgForLinRegIntro)
// MAGIC         * [HOMEWORK: Spark Data Types for Distributed Linear Algebra](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide)
// MAGIC             * [Local Vector](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/001_LocalVector)
// MAGIC             * [Labeled Point](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/002_LabeledPoint)
// MAGIC             * [Local Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/003_LocalMatrix)
// MAGIC             * [Distributed Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix)
// MAGIC             * [Row Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/005_RowMatrix)
// MAGIC             * [Indexed Row Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix)
// MAGIC             * [Coordinate Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix)
// MAGIC             * [Block Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/008_BlockMatrix)
// MAGIC     * [Power Plant Pipeline: Model, Tune, Evaluate](#workspace/scalable-data-science/week5/11_MLlibModelTuneEvaluate/020_PowerPlantPipeline_02ModelTuneEvaluate)
// MAGIC 
// MAGIC * [Week 6: Introduction to Spark Streaming, Twitter Collector, Top Hashtag Counter and Streaming Model-Prediction Server](#workspace/scalable-data-science/week6/)
// MAGIC     * [Introduction to Spark Streaming](#workspace/scalable-data-science/week6/12_SparkStreaming/021_SparkStreamingIntro)
// MAGIC     * [Tweet Collector - broken down](#workspace/scalable-data-science/week6/12_SparkStreaming/022_TweetCollector)
// MAGIC     * [Tweet Collector - Generic](#workspace/scalable-data-science/week6/12_SparkStreaming/022_TweetGenericCollector)
// MAGIC     * [Tweet Hashtag Counter](#workspace/scalable-data-science/week6/12_SparkStreaming/023_TweetHashtagCount)
// MAGIC     * [Streaming Model-Prediction Server, the Full Powerplant Pipeline](#workspace/scalable-data-science/week6/13_StreamingMLlib_ModelTuneEvaluateDeploy/024_PowerPlantPipeline_03ModelTuneEvaluateDeploy)
// MAGIC 
// MAGIC * [Week 7: Probabilistic Topic Modelling via Latent Dirichlet Allocation and Intro to XML-parsing of Old Bailey Online](#workspace/scalable-data-science/week7/)
// MAGIC     * [Probabilistic Topic Modelling](#workspace/scalable-data-science/week7/14_ProbabilisticTopicModels/025_LDA_20NewsGroupsSmall)
// MAGIC     * [HOMEWORK: Introduction to XML-parsing of Old Bailey Online](#workspace/scalable-data-science/xtraResources/OldBaileyOnline/OBO_LoadExtract)
// MAGIC 
// MAGIC * [Week 8: Graph Querying in GraphFrames and Distributed Vertex Programming in GraphX](#workspace/scalable-data-science/week8/)
// MAGIC     * [Introduction to GraphFrames](#workspace/scalable-data-science/week8/15_GraphX/026_GraphFramesUserGuide)
// MAGIC     * [HOMEWORK: On-Time Flight Performance with GraphFrames](#workspace/scalable-data-science/week8/15_GraphX/028_OnTimeFlightPerformance)
// MAGIC 
// MAGIC * [Week 9: Deep Learning, Convolutional Neural Nets, Sparkling Water and Tensor Flow](#workspace/scalable-data-science/week9/)
// MAGIC     * [Deep Learning, A Crash Introduction](#workspace/scalable-data-science/week9/16_Deep_learning/030_Deep_learning)
// MAGIC     * [H2O Sparkling Water](#workspace/scalable-data-science/week9/17_SparklingWater/031_H2O_sparkling_water)
// MAGIC     * [H2O Sparkling Water: Ham or Spam Example](#workspace/scalable-data-science/week9/17_SparklingWater/032_Deep_learning_ham_or_spam)
// MAGIC     * [Setting up TensorFlow Spark Cluster](#workspace/scalable-data-science/week9/18_sparklingTensorFlow/033_SetupCluster_SparkTensorFlow)
// MAGIC     * [Scalable Object Identification with Spark and TensorFlow](#workspace/scalable-data-science/week9/18_sparklingTensorFlow/034_SampleML_SparkTensorFlow)
// MAGIC 
// MAGIC 
// MAGIC * [Week 10: Scalable Geospatial Analytics with Magellan](#workspace/scalable-data-science/week10/)
// MAGIC     * [What is Scalable Geospatial Analytics](#workspace/scalable-data-science/week10/035_ScalableGeoSpatialComputing)
// MAGIC     * [Introduction to Magellan for Scalable Geospatial Analytics](#workspace/scalable-data-science/week10/036_IntroductionToMagellan)
// MAGIC 
// MAGIC * [Week 11 and 12: Student Projects](#workspace/scalable-data-science/studentProjects/)
// MAGIC     * [Student Projects](#workspace/scalable-data-science/studentProjects/00_studentPresentations)
// MAGIC     * [Dillon George, Scalable Geospatial Algorithms](#workspace/scalable-data-science/studentProjects/01_DillonGeorge)
// MAGIC         * [Scalable Spatio-temporal Constraint Satisfaction](#workspace/scalable-data-science/studentProjects/01_DillonGeorge/037_MSR_BeijingTaxiTrajectories_MagellanQueries)
// MAGIC         * [Map-matching](#workspace/scalable-data-science/studentProjects/01_DillonGeorge/038_UberMapMatchingAndVisualization)
// MAGIC         * [OpenStreetMap to GraphX](#workspace/scalable-data-science/studentProjects/01_DillonGeorge/039_OSMMap2GraphX)
// MAGIC     * [Akinwande Atanda, Twitter Analytics](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda)
// MAGIC         * [Chapter_Outline_and_Objectives](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/039_TA00_Chapter_Outline_and_Objectives)
// MAGIC         * [Unfiltered_Tweets_Collector_Set-up](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/040_TA01_01_Unfiltered_Tweets_Collector_Set-up)
// MAGIC         * [Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/041_TA01_02_Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags)
// MAGIC         * [Filtered_Tweets_Collector_Set-up_by_Class](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/042_TA01_03_Filtered_Tweets_Collector_Set-up_by_Class)
// MAGIC         * [ETL_Tweets](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/043_TA02_ETL_Tweets)
// MAGIC         * [binary_classification](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/045_TA03_02_binary_classification)
// MAGIC         * [binary_classification_with_Loop](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/046_TA03_03_binary_classification_with_Loop)
// MAGIC         * [binary_classification_with_Loop_TweetDataSet](#workspace/scalable-data-science/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/047_TA03_04_binary_classification_with_Loop_TweetDataSet)
// MAGIC 
// MAGIC     * [Yinnon Dolev, Deciphering Spider Vision](#workspace/scalable-data-science/studentProjects/03_YinnonDolev/048_decipheringSpiderVision)
// MAGIC     * [Xin Zhao, Higher Order Spectral CLustering](#workspace/scalable-data-science/studentProjects/04_XinZhao/049_Introduction_HighOrderSpectralClustering)
// MAGIC         * [Xin Zhao, Case-study](#workspace/scalable-data-science/studentProjects/04_XinZhao/050_CaseStudy_HighOrderSpectralClustering)
// MAGIC     * [Shanshan Zhou](#workspace/scalable-data-science/studentProjects/05_ShanshanZhou/051_EEG_Explore)
// MAGIC     * [Shakira Suwan, Change Detection in Random Graph Series](#workspace/scalable-data-science/studentProjects/06_ShakiraSuwan/052_ChangeDetectionInRandomGraphSeries)
// MAGIC     * [Matthew Hendtlass, The ATP graph](#workspace/scalable-data-science/studentProjects/07_MatthewHendtlass/053_The_ATP_graph)
// MAGIC         * [Yuki_Katoh_GSW_Passing_Analysis](#workspace/scalable-data-science/studentProjects/07_MatthewHendtlass/054_Yuki_Katoh_GSW_Passing_Analysis)
// MAGIC     * [Andrey Konstantinov, Keystroke Biometric](#workspace/scalable-data-science/studentProjects/08_AndreyKonstantinov/055_KeystrokeBiometric)
// MAGIC     * [Dominic Lee, Random Matrices](#workspace/scalable-data-science/studentProjects/09_DominicLee/056_RandomMatrices)
// MAGIC         * [Dominic Lee, References](#workspace/scalable-data-science/studentProjects/09_DominicLee/057_QuickReferences)
// MAGIC     * [Harry Wallace, Movie Recommender](#workspace/scalable-data-science/studentProjects/10_HarryWallace/058_MovieRecommender)
// MAGIC     * [Ivan Sadikov, Reading NetFlow Logs](#workspace/scalable-data-science/studentProjects/11_IvanSadikov/059_SparkNetFlow)
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC * [Extra Resources](#workspace/scalable-data-science/xtraResources)
// MAGIC     * [AWS Educate](#workspace/scalable-data-science/xtraResources/awsEducate/sharing)
// MAGIC     * Databricksified Spark SQL Programming Guide 1.6
// MAGIC         * [overview](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide)
// MAGIC         * [getting started](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide)
// MAGIC         * [data sources](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/003_dataSources_sqlProgGuide)
// MAGIC         * [performance tuning](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide)
// MAGIC         * [distributed sql engine](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide)
// MAGIC     * [Linear Algebra Cheat Sheet](#workspace/scalable-data-science/xtraResources/LinearAlgebra/LAlgCheatSheet)
// MAGIC     * Databricksified Data Types in MLLib Programming Guide 1.6
// MAGIC         * [Local Vector](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/001_LocalVector)
// MAGIC         * [Labeled Point](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/002_LabeledPoint)
// MAGIC         * [Local Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/003_LocalMatrix)
// MAGIC         * [Distributed Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix)
// MAGIC         * [Row Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/005_RowMatrix)
// MAGIC         * [Indexed Row Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix)
// MAGIC         * [Coordinate Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix)
// MAGIC         * [Block Matrix](#workspace/scalable-data-science/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/008_BlockMatrix)
// MAGIC     * [Introduction to XML-parsing of Old Bailey Online](#workspace/scalable-data-science/xtraResources/OldBaileyOnline/OBO_LoadExtract)