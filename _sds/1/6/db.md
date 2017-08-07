---
title: SDS-1.6 on databricks 
permalink: /sds/1/6/db/
sidebar:
  nav: "lMenu-SDS-1.6"
header:
  overlay_color: "#5e616c"
  overlay_image: /assets/images/DISP-SDS-orange-1600x524.jpg
  caption: 
excerpt: 'Scalable Data Science from Middle Earth, A Big Data Course in Apache Spark 1.6 over databricks.<br /><br /><br />{::nomarkdown}<iframe style="display: inline-block;" src="https://ghbtns.com/github-btn.html?user=lamastex&repo=scalable-data-science&type=star&count=true&size=large" frameborder="0" scrolling="0" width="160px" height="30px"></iframe> <iframe style="display: inline-block;" src="https://ghbtns.com/github-btn.html?user=lamastex&repo=scalable-data-science&type=fork&count=true&size=large" frameborder="0" scrolling="0" width="158px" height="30px"></iframe>{:/nomarkdown}'
---

# SDS-1.6 on databricks

## Scalable Data Science from Middle Earth, A Big Data Course in Apache Spark 1.6 over databricks

## How to self-learn this content?

The 2016 instance of this [scalable-data-science course](http://lamastex.org/courses/ScalableDataScience/) finished on June 30 2016.

To learn Apache Spark for free try **databricks Community edition** by starting from [https://databricks.com/try-databricks](https://databricks.com/try-databricks).

All course content can be uploaded for self-paced learning by copying the following [URL for 2016/Spark1_6_to_1_3/scalable-data-science.dbc archive](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/dbcArchives/2016/Spark1_6_to_1_3/scalable-data-science.dbc)
and importing it from the URL to your [free Databricks Community Edition](https://community.cloud.databricks.com).

The Gitbook version of this content is [https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details](https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details).

The browsable git-pages version of the content is [http://raazesh-sainudiin.github.io/scalable-data-science/](http://raazesh-sainudiin.github.io/scalable-data-science/).

## How to cite this work?

Scalable Data Science, Raazesh Sainudiin and Sivanand Sivaram, Published by GitBook [https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details](https://www.gitbook.com/book/raazesh-sainudiin/scalable-data-science/details), 787 pages, 30th June 2016.

## Supported By
[Databricks Academic Partners Program](https://databricks.com/academic) and [Amazon Web Services Educate](https://www.awseducate.com/microsite/CommunitiesEngageHome).

## Summary of Contents

* [Prelude of 2016 Version ](sdsBlog/)

* [Week 1: Introduction to Scalable Data Science](week01/)
    * [Scalable Data Science](week01/01_introduction/000_scalableDataScience/)
    * [Why Spark?](week01/01_introduction/001_whySpark/)
    * [Login to databricks](week01/01_introduction/002_loginToDatabricks/)
    * [Scala Crash Course](week01/01_introduction/003_scalaCrashCourse/)

* [Week 2: Introduction to Spark RDDs, Transformations and Actions and Word Count of the US State of the Union Addresses](week02/)
    * [RDDs, Transformations and Actions](week02/02_SparkEssentials/004_RDDsTransformationsActions/)
    * [HOMEWORK: RDDs, Transformations and Actions](week02/02_SparkEssentials/005_RDDsTransformationsActionsHOMEWORK/)
    * [Word Count: US State of Union Addesses](week02/03_WordCount/006_WordCount/)
    * [EXTRA_Word Count: ETL of US State of Union Addesses](xtraResources/sdsDatasets/scraperUSStateofUnionAddresses/)

* [Week 3: Introduction to Spark SQL, ETL and EDA of Diamonds, Power Plant and Wiki CLick Streams Data](week03/)
    * [Spark SQL Introduction](week03/04_SparkSQLIntro/007_SparkSQLIntroBasics/)
        * [HOMEWORK: overview](xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide/)
        * [HOMEWORK: getting started](xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide/)
        * [HOMEWORK: data sources](xtraResources/ProgGuides1_6/sqlProgrammingGuide/003_dataSources_sqlProgGuide/)
        * [HOMEWORK: performance tuning](xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide/)
        * [HOMEWORK: distributed sql engine](xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide/)
    * [ETL and EDA of Diamonds Data](week03/05_SparkSQLETLEDA/008_DiamondsPipeline_01ETLEDA/)
    * [ETL and EDA of Power Plant Data](week03/05_SparkSQLETLEDA/009_PowerPlantPipeline_01ETLEDA/)
    * [ETL and EDA of Wiki Click Stream Data](week03/05_SparkSQLETLEDA/010_wikipediaClickStream_01ETLEDA/)

* [Week 4: Introduction to Machine Learning - Unsupervised Clustering and Supervised Classification](week04/)
    * [Introduction to Machine Learning](week04/06_MLIntro/011_IntroToML/)
    * [Unsupervised Clustering of 1 Million Songs via K-Means in 3 Stages](week04/07_UnsupervisedClusteringKMeans_1MSongs/012_1MSongsKMeans_Intro/)
        * [Stage 1: Extract-Transform-Load](week04/07_UnsupervisedClusteringKMeans_1MSongs/013_1MSongsKMeans_Stage1ETL/)
        * [Stage 2: Explore](week04/07_UnsupervisedClusteringKMeans_1MSongs/014_1MSongsKMeans_Stage2Explore/)
        * [Stage 3: Model](week04/07_UnsupervisedClusteringKMeans_1MSongs/015_1MSongsKMeans_Stage3Model/)
    * [Supervised Classification of Hand-written Digits via Decision Trees](week04/08_SupervisedLearningDecisionTrees/016_DecisionTrees_HandWrittenDigitRecognition/)

* [Week 5: Introduction to Non-distributed and Distributed Linear Algebra and Applied Linear Regression](week05/)
    * [Linear Algebra Introduction](week05/09_LinearAlgebraIntro/017_LAlgIntro/)
        * [HOMEWORK: breeze linear algebra cheat sheet](xtraResources/LinearAlgebra/LAlgCheatSheet/)
    * [Linear Regression Introduction](week05/10_LinearRegressionIntro/018_LinRegIntro/)
    * [Distributed Linear Algebra for Linear Regression Introduction](week05/10_LinearRegressionIntro/019_DistLAlgForLinRegIntro/)
        * [HOMEWORK: Spark Data Types for Distributed Linear Algebra](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide/)
            * [Local Vector](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/001_LocalVector/)
            * [Labeled Point](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/002_LabeledPoint/)
            * [Local Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/003_LocalMatrix/)
            * [Distributed Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix/)
            * [Row Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/005_RowMatrix/)
            * [Indexed Row Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix/)
            * [Coordinate Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix/)
            * [Block Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/008_BlockMatrix/)
    * [Power Plant Pipeline: Model, Tune, Evaluate](week05/11_MLlibModelTuneEvaluate/020_PowerPlantPipeline_02ModelTuneEvaluate)

* [Week 6: Introduction to Spark Streaming, Twitter Collector, Top Hashtag Counter and Streaming Model-Prediction Server](week06/)
    * [Introduction to Spark Streaming](week06/12_SparkStreaming/021_SparkStreamingIntro/)
    * [Tweet Collector - broken down](week06/12_SparkStreaming/022_TweetCollector/)
    * [Tweet Collector - Generic](week06/12_SparkStreaming/022_TweetGenericCollector/)
    * [Tweet Hashtag Counter](week06/12_SparkStreaming/023_TweetHashtagCount/)
    * [Streaming Model-Prediction Server, the Full Powerplant Pipeline](week06/13_StreamingMLlib_ModelTuneEvaluateDeploy/024_PowerPlantPipeline_03ModelTuneEvaluateDeploy/)

* [Week 7: Probabilistic Topic Modelling via Latent Dirichlet Allocation and Intro to XML-parsing of Old Bailey Online](week07/)
    * [Probabilistic Topic Modelling](week07/14_ProbabilisticTopicModels/025_LDA_20NewsGroupsSmall/)
    * [HOMEWORK: Introduction to XML-parsing of Old Bailey Online](xtraResources/OldBaileyOnline/OBO_LoadExtract/)

* [Week 8: Graph Querying in GraphFrames and Distributed Vertex Programming in GraphX](week08/)
    * [Introduction to GraphFrames](week08/15_GraphX/026_GraphFramesUserGuide/)
    * [HOMEWORK: On-Time Flight Performance with GraphFrames](week08/15_GraphX/028_OnTimeFlightPerformance/)

* [Week 9: Deep Learning, Convolutional Neural Nets, Sparkling Water and Tensor Flow](week09/)
    * [Deep Learning, A Crash Introduction](week09/16_Deep_learning/030_Deep_learning/)
    * [H2O Sparkling Water](week09/17_SparklingWater/031_H2O_sparkling_water/)
    * [H2O Sparkling Water: Ham or Spam Example](week09/17_SparklingWater/032_Deep_learning_ham_or_spam/)
    * [Setting up TensorFlow Spark Cluster](week09/18_sparklingTensorFlow/033_SetupCluster_SparkTensorFlow/)
    * [Scalable Object Identification with Sparkling TensorFlow](week09/18_sparklingTensorFlow/034_SampleML_SparkTensorFlow/)

* [Week 10: Scalable Geospatial Analytics with Magellan](week10/)
    * [What is Scalable Geospatial Analytics](week10/035_ScalableGeoSpatialComputing/)
    * [Introduction to Magellan for Scalable Geospatial Analytics](week10/036_IntroductionToMagellan/)

* [Week 11 and 12: Student Projects](studentProjects/)
    * [Student Projects](studentProjects/00_studentPresentations/)
    * [Dillon George, Scalable Geospatial Algorithms](studentProjects/01_DillonGeorge/)
        * [Scalable Spatio-temporal Constraint Satisfaction](studentProjects/01_DillonGeorge/037_MSR_BeijingTaxiTrajectories_MagellanQueries/)
        * [Map-matching](studentProjects/01_DillonGeorge/038_UberMapMatchingAndVisualization/)
        * [OpenStreetMap to GraphX](studentProjects/01_DillonGeorge/039_OSMMap2GraphX/)
    * [Akinwande Atanda, Twitter Analytics](studentProjects/02_AkinwandeAtanda/) 
        * [Chapter_Outline_and_Objectives](studentProjects/02_AkinwandeAtanda/Tweet_Analytics/039_TA00_Chapter_Outline_and_Objectives/)
        * [Unfiltered_Tweets_Collector_Set-up](studentProjects/02_AkinwandeAtanda/Tweet_Analytics/040_TA01_01_Unfiltered_Tweets_Collector_Set-up/)
        * [Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags](studentProjects/02_AkinwandeAtanda/Tweet_Analytics/041_TA01_02_Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags/)
        * [Filtered_Tweets_Collector_Set-up_by_Class](studentProjects/02_AkinwandeAtanda/Tweet_Analytics/042_TA01_03_Filtered_Tweets_Collector_Set-up_by_Class/)
        * [ETL_Tweets](studentProjects/02_AkinwandeAtanda/Tweet_Analytics/043_TA02_ETL_Tweets/)
        * [binary_classification](studentProjects/02_AkinwandeAtanda/Tweet_Analytics/045_TA03_02_binary_classification/)
        * [binary_classification_with_Loop](studentProjects/02_AkinwandeAtanda/Tweet_Analytics/046_TA03_03_binary_classification_with_Loop/)
        * [binary_classification_with_Loop_TweetDataSet](studentProjects/02_AkinwandeAtanda/Tweet_Analytics/047_TA03_04_binary_classification_with_Loop_TweetDataSet/)
	
    * [Yinnon Dolev, Deciphering Spider Vision](studentProjects/03_YinnonDolev/048_decipheringSpiderVision/)
    * [Xin Zhao, Higher Order Spectral CLustering](studentProjects/04_XinZhao/049_Introduction_HighOrderSpectralClustering/)
        * [Case-study](studentProjects/04_XinZhao/050_CaseStudy_HighOrderSpectralClustering/)
    * [Shanshan Zhou, Exploring EEG](studentProjects/05_ShanshanZhou/051_EEG_Explore/)
    * [Shakira Suwan, Change Detection in Random Graph Series](studentProjects/06_ShakiraSuwan/052_ChangeDetectionInRandomGraphSeries/)
    * [Matthew Hendtlass, The ATP graph](studentProjects/07_MatthewHendtlass/053_The_ATP_graph/)
        * [Yuki_Katoh_GSW_Passing_Analysis](studentProjects/07_MatthewHendtlass/054_Yuki_Katoh_GSW_Passing_Analysis/)
    * [Andrey Konstantinov, Keystroke Biometric](studentProjects/08_AndreyKonstantinov/055_KeystrokeBiometric/)
    * [Dominic Lee, Random Matrices](studentProjects/09_DominicLee/056_RandomMatrices/)
        * [References](studentProjects/09_DominicLee/057_QuickReferences/)
    * [Harry Wallace, Movie Recommender](studentProjects/10_HarryWallace/058_MovieRecommender/)
    * [Ivan Sadikov, Reading NetFlow Logs](studentProjects/11_IvanSadikov/059_SparkNetFlow/)

* [Extra Resources](xtraResources/)
    * [AWS Educate](xtraResources/awsEducate/sharing/)
    * Databricksified Spark SQL Programming Guide 1.6
        * [overview](xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide/)
        * [getting started](xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide/)
        * [data sources](xtraResources/ProgGuides1_6/sqlProgrammingGuide/003_dataSources_sqlProgGuide/)
        * [performance tuning](xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide/)
        * [distributed sql engine](xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide/)
    * [Linear Algebra Cheat Sheet](xtraResources/LinearAlgebra/LAlgCheatSheet/)
    * Databricksified Data Types in MLLib Programming Guide 1.6 
        * [Local Vector](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/001_LocalVector/)
        * [Labeled Point](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/002_LabeledPoint/)
        * [Local Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/003_LocalMatrix/)
        * [Distributed Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix/)
        * [Row Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/005_RowMatrix/)
        * [Indexed Row Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix/)
        * [Coordinate Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix/)
        * [Block Matrix](xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/008_BlockMatrix/)
    * [Introduction to XML-parsing of Old Bailey Online](xtraResources/OldBaileyOnline/OBO_LoadExtract/)

## Contribute

All course content is currently being pushed by Raazesh Sainudiin after it has been tested in
Databricks cloud (mostly under Spark 1.6 and some involving Magellan under Spark 1.5.1).

The markdown version for `gitbook` is generated from the Databricks `.scala`, `.py` and other source codes.
The gitbook is not a substitute for the Databricks notebooks available in the Databricks cloud. The following issues need to be resolved:

* need to find a stable solution for the output of various databricks cells to be shown in gitbook, including those from `display_HTML` and `frameIt` with their in-place embeds of web content.

Please feel free to fork the github repository: 

* [https://github.com/raazesh-sainudiin/scalable-data-science](https://github.com/raazesh-sainudiin/scalable-data-science).

Furthermore, due to the anticipation of Spark 2.0 this mostly Spark 1.6 version could be enhanced with a 2.0 version-specific upgrade. 

Please send any typos or suggestions to raazesh.sainudiin@gmail.com

Please read a note on [babel](https://github.com/raazesh-sainudiin/scalable-data-science/blob/master/babel/) to understand how the gitbook is generated from the `.scala` source of the databricks notebook.


Raazesh Sainudiin, 
Laboratory for Mathematical Statistical Experiments, Christchurch Centre 
and School of Mathematics and Statistics, 
University of Canterbury, 
Private Bag 4800, 
Christchurch 8041, 
Aotearoa New Zealand 

Sun Jun 19 21:59:19 NZST 2016
