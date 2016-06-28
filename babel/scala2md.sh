#!/bin/bash
# a shell script to do use spark to convert .scala from databricks into .md for github and gitbook
# you need to make this file executible '$ chmod 755 scala2ms.sh' first!
# very hacky... but ideal for jumping into spark-shell anytime to evolve syntax fast for new sources
###############
# to get all scala files for markdown'ing do
# for i in $(find ../db -name "*.scala") ; do echo $i; done

#clean input scala code for spark-shell
rm -f nowparse.scala
# clean output directory for the output of spark-shell
rm -rf ./MDparsed

# name of the root (without .scala extension) of the input scala file 
# that needs to be parsed into markdown (.md file extension)

###########-- prelude --#########################################################################
#FName=../db/sdsBlog
###############################################################################################

###########-- Week1 --#########################################################################
#FName=../db/week1/01_introduction/000_scalableDataScience # done Thu Jun 16 19:03:47 NZST 2016
#FName=../db/week1/01_introduction/001_whySpark # done Thu Jun 16 20:00:33 NZST 2016
#FName=../db/week1/01_introduction/002_loginToDatabricks # done Thu Jun 16 20:12:39 NZST 2016
#FName=../db/week1/01_introduction/003_scalaCrashCourse # done Thu Jun 16 20:26:14 NZST 2016
###############################################################################################

###########-- Week2 --#########################################################################
#FName=../db/week2/02_SparkEssentials/004_RDDsTransformationsActions # done Sat Jun 18 16:29:12 NZST 2016
#FName=../db/week2/02_SparkEssentials/005_RDDsTransformationsActionsHOMEWORK # done  Sat Jun 18 16:29:31 NZST 2016
#FName=../db/xtraResources/sdsDatasets/scraperUSStateofUnionAddresses # done Sat Jun 18 17:00:40 NZST 2016
#FName=../db/week2/03_WordCount/006_WordCount # done Sat Jun 18 19:14:52 NZST 2016
###############################################################################################

###########-- Week3 --#########################################################################
############################# this block done Sat Jun 18 20:53:48 NZST 2016
#FName=../db/week3/04_SparkSQLIntro/007_SparkSQLIntroBasics #done Sat Jun 18 20:38:31 NZST 2016 
#FName=../db/week3/05_SparkSQLETLEDA/008_DiamondsPipeline_01ETLEDA # 
#FName=../db/week3/05_SparkSQLETLEDA/009_PowerPlantPipeline_01ETLEDA # 
#FName=../db/week3/05_SparkSQLETLEDA/010_wikipediaClickStream_01ETLEDA # 
######sql-programming guide HOMEWORK ### this block done Sat Jun 18 19:53:55 NZST 2016
#FName=../db/xtraResources/ProgGuides1_6/sqlProgrammingGuide/000_sqlProgGuide
#FName=../db/xtraResources/ProgGuides1_6/sqlProgrammingGuide/001_overview_sqlProgGuide
#FName=../db/xtraResources/ProgGuides1_6/sqlProgrammingGuide/002_gettingStarted_sqlProgGuide
#FName=../db/xtraResources/ProgGuides1_6/sqlProgrammingGuide/003_dataSources_sqlProgGuide
#FName=../db/xtraResources/ProgGuides1_6/sqlProgrammingGuide/004_performanceTuning_sqlProgGuide
#FName=../db/xtraResources/ProgGuides1_6/sqlProgrammingGuide/005_distributedSqlEngine_sqlProgGuide
###############################################################################################

###########-- Week4 --#########################################################################
# this block done around Sat Jun 18 20:38:31 NZST 2016 
#FName=../db/week4/06_MLIntro/011_IntroToML 
#FName=../db/week4/07_UnsupervisedClusteringKMeans_1MSongs/012_1MSongsKMeans_Intro
#FName=../db/week4/07_UnsupervisedClusteringKMeans_1MSongs/013_1MSongsKMeans_Stage1ETL
#FName=../db/week4/07_UnsupervisedClusteringKMeans_1MSongs/014_1MSongsKMeans_Stage2Explore
#FName=../db/week4/07_UnsupervisedClusteringKMeans_1MSongs/015_1MSongsKMeans_Stage3Model
#FName=../db/week4/08_SupervisedLearningDecisionTrees/016_DecisionTrees_HandWrittenDigitRecognition
###############################################################################################

###########-- Week5 --#########################################################################
# this block done around Sun Jun 19 12:43:57 NZST 2016
#FName=../db/week5/09_LinearAlgebraIntro/017_LAlgIntro
#FName=../db/week5/10_LinearRegressionIntro/018_LinRegIntro
#FName=../db/week5/10_LinearRegressionIntro/019_DistLAlgForLinRegIntro
#FName=../db/week5/11_MLlibModelTuneEvaluate/020_PowerPlantPipeline_02ModelTuneEvaluate
###############################################################################################

###########-- Week6 --#########################################################################
#FName=../db/sdsBlog # Tue Jun 28 17:44:48 NZST 2016
#FName=../db/week6/12_SparkStreaming/021_SparkStreamingIntro
#FName=../db/week6/12_SparkStreaming/022_TweetCollector
#FName=../db/week6/12_SparkStreaming/022_TweetGenericCollector
#FName=../db/week6/12_SparkStreaming/023_TweetHashtagCount
#FName=../db/week6/13_StreamingMLlib_ModelTuneEvaluateDeploy/024_PowerPlantPipeline_03ModelTuneEvaluateDeploy
###############################################################################################

###########-- Week7 --#########################################################################
#FName=../db/week7/14_ProbabilisticTopicModels/025_LDA_20NewsGroupsSmall # done Tue Jun 28 22:05:04 NZST 2016
#FName=../db/xtraResources/OldBaileyOnline/OBO_LoadExtract
###############################################################################################

###########-- Week8 --#########################################################################
#FName=../db/week8/15_GraphX/026_GraphFramesUserGuide # done Tue Jun 28 22:05:20 NZST 2016
#FName=../db/week8/15_GraphX/028_OnTimeFlightPerformance
###############################################################################################

###########-- Week9 --#########################################################################
#FName=../db/week9/16_Deep_learning/030_Deep_learning # done Tue Jun 28 22:21:30 NZST 2016
#FName=../db/week9/17_SparklingWater/031_H2O_sparkling_water 
#FName=../db/week9/17_SparklingWater/032_Deep_learning_ham_or_spam 
###############################################################################################
##FName=../db/week9/18_sparklingTensorFlow/033_SetupCluster_SparkTensorFlow.py# TODO py -> md
##FName=../db/week9/18_sparklingTensorFlow/034_SampleML_SparkTensorFlow.py # TODO py -> md
###############################################################################################

###########-- Week10 --#########################################################################
#FName=../db/week10/035_ScalableGeoSpatialComputing # done Tue Jun 28 22:25:31 NZST 2016
#FName=../db/week10/036_IntroductionToMagellan                      
###############################################################################################
FName=../db/studentProjects/00_studentPresentations
#FName=../db/studentProjects/01_DillonGeorge/037_MSR_BeijingTaxiTrajectories_MagellanQueries
#FName=../db/studentProjects/01_DillonGeorge/038_UberMapMatchingAndVisualization
#FName=../db/studentProjects/01_DillonGeorge/039_OSMMap2GraphX
###############################################################################################

###########-- Week11 --#########################################################################

#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/039_TA00_Chapter_Outline_and_Objectives
#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/040_TA01_01_Unfiltered_Tweets_Collector_Set-up
#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/041_TA01_02_Filtered_Tweets_Collector_Set-up_by_Keywords_and_Hashtags
#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/042_TA01_03_Filtered_Tweets_Collector_Set-up_by_Class
#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/043_TA02_ETL_Tweets
#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/044_TA03_01_binary_classification #.r
#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/045_TA03_02_binary_classification #.py
#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/046_TA03_03_binary_classification_with_Loop #.py
#FName=../db/studentProjects/02_AkinwandeAtanda/Tweet_Analytics/047_TA03_04_binary_classification_with_Loop_TweetDataSet #.py

#FName=../db/studentProjects/03_YinnonDolev/048_decipheringSpiderVision

#FName=../db/studentProjects/04_XinZhao/049_Introduction_HighOrderSpectralClustering
#FName=../db/studentProjects/04_XinZhao/050_CaseStudy_HighOrderSpectralClustering

#FName=../db/studentProjects/05_ShanshanZhou/051_EEG_Explore #.py

#FName=../db/studentProjects/06_ShakiraSuwan/052_ChangeDetectionInRandomGraphSeries

#FName=../db/studentProjects/07_MatthewHendtlass/053_The_ATP_graph
#FName=../db/studentProjects/07_MatthewHendtlass/054_Yuki_Katoh_GSW_Passing_Analysis #.py

#FName=../db/studentProjects/08_AndreyKonstantinov/055_KeystrokeBiometric

#FName=../db/studentProjects/09_DominicLee/056_RandomMatrices
#FName=../db/studentProjects/09_DominicLee/057_QuickReferences

#FName=../db/studentProjects/10_HarryWallace/058_MovieRecommender

#FName=../db/studentProjects/11_IvanSadikov/059_SparkNetFlow

###############################################################################################

###########-- Week12 --#########################################################################
###############################################################################################

###########-- xtraResources --#########################################################################
#FName=../db/xtraResources/awsEducate/sharing # done Thu Jun 16 21:00:53 NZST 2016
#FName=../db/xtraResources/LinearAlgebra/LAlgCheatSheet
################ done Sun Jun 19 20:45:20 NZST 2016
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/000_dataTypesProgGuide
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/001_LocalVector
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/002_LabeledPoint
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/003_LocalMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/005_RowMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/008_BlockMatrix
###############################################################################################

echo "val ioFilenameRoot = \"$FName\"" > nowparse.scala


# prepare the scala file nowparse.scala for loading into spark-shell
cat parseMD.scala >> nowparse.scala

# load it into spark-shell and finsih the job
spark-shell -i nowparse.scala

#copy md file to the same directory as .scala file 
mdFileName=$FName.md
cp ./MDparsed/part-00000 $mdFileName
#rm -r ./MDparsed
