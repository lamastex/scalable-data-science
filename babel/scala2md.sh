#!/bin/bash
# a shell script to do use spark to convert .scala from databricks into .md for github and gitbook
# you need to make this file executible '$ chmod 755 scala2ms.sh' first!
# very hacky... but ideal for jumping into spark-shell anytime to evolve syntax fast for new sources

#clean input scala code for spark-shell
rm -f nowparse.scala
# clean output directory for the output of spark-shell
rm -rf ./MDparsed

# name of the root (without .scala extension) of the input scala file 
# that needs to be parsed into markdown (.md file extension)
###########-- Week1 --#########################################################################
#FName=../db/week1/01_introduction/000_scalableDataScience # done Sun Feb 28 18:08:31 NZDT 2016
#FName=../db/week1/01_introduction/001_whySpark # done Sun Feb 28 18:14:20 NZDT 2016
#FName=../db/week1/01_introduction/002_loginToDatabricks # done Sun Feb 28 18:34:32 NZDT 2016
#FName=../db/week1/01_introduction/003_scalaCrashCourse # done Sun Feb 28 19:03:20 NZDT 2016
###############################################################################################

###########-- Week2 --#########################################################################
#FName=../db/week2/02_SparkEssentials/004_RDDsTransformationsActions # done 
FName=../db/week2/02_SparkEssentials/005_RDDsTransformationsActionsHOMEWORK # done 
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
