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
###############################################################################################

###########-- Week7 --#########################################################################
###############################################################################################

###########-- Week8 --#########################################################################
###############################################################################################

###########-- Week9 --#########################################################################
###############################################################################################

###########-- Week10 --#########################################################################
###############################################################################################

###########-- Week11 --#########################################################################
###############################################################################################

###########-- Week12 --#########################################################################
###############################################################################################

###########-- xtraResources --#########################################################################
#FName=../db/xtraResources/awsEducate/sharing # done Thu Jun 16 21:00:53 NZST 2016
#FName=../db/xtraResources/LinearAlgebra/LAlgCheatSheet
################ to be done for below
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/001_LocalVector
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/002_LabeledPoint
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/003_LocalMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/004_DistributedMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/005_RowMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/006_IndexedRowMatrix
#FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/007_CoordinateMatrix
FName=../db/xtraResources/ProgGuides1_6/MLlibProgrammingGuide/dataTypes/008_BlockMatrix
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
