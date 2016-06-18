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
FName=../db/week2/02_SparkEssentials/005_RDDsTransformationsActionsHOMEWORK # done  Sat Jun 18 16:29:31 NZST 2016
#FName=../db/week2/03_WordCount/006_WordCount # done 
#FName=../db/xtraResources/sdsDatasets/scraperUSStateofUnionAddresses # done 
###############################################################################################

###########-- Week3 --#########################################################################
#FName=../db/week3/04_SparkSQLIntro/007_SparkSQLIntroBasics # done Sun Mar 13 11:55:14 NZDT 2016
#FName=../db/week3/05_SparkSQLETLEDA/008_DiamondsPipeline_01ETLEDA # done Sun Mar 13 12:34:20 NZDT 2016
#FName=../db/week3/05_SparkSQLETLEDA/009_PowerPlantPipeline_01ETLEDA # done Sun Mar 13 12:35:15 NZDT 2016
#FName=../db/week3/05_SparkSQLETLEDA/010_wikipediaClickStream_01ETLEDA # done Sun Mar 13 12:36:02 NZDT 2016
###############################################################################################

###########-- Week4 --#########################################################################
###############################################################################################

###########-- Week5 --#########################################################################
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
