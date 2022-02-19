#!/bin/bash

set -x -v
set -o allexport
source env.list
set +o allexport

#echo ${dbProfile0}
echo ${localgitdbcDIRPATH}
echo ${localgitsiteDIRPATH}
echo ${localgitdbcDIR}
echo ${PINOTdir}
echo ${MDBOOKdir}

echo "done echoing env variables in use inside docker"

#modules="000_1-sds-3-x " #000_2-sds-3-x-ml #"000_3-sds-3-x-st " #000_4-sds-3-x-ss 000_5-sds-2-x-geo 000_6-sds-3-x-dl 000_7-sds-3-x-ddl 000_8-sds-3-x-pri xtraResources

######################################################################
#
#specify the correct name of the dbc file "WITHOUT .dbc" !
#modules=student-project-19_group-Featuring
#modules="student-project-01_group-TheTwoCultures student-project-02_group-LiUUmeaSceneGraphMotifs student-project-03_group-GuangyiZhang student-project-04_group-DistributedLinearAlgebra student-project-05_group-LundDirichletAnalysts student-project-06_group-ParticleClustering student-project-07_group-MathAtKTH student-project-08_group-DistributedEnsemble student-project-09_group-TopicModeling student-project-10_group-Geosmus student-project-11_group-Sketchings student-project-12_group-CovidPandemic student-project-13_group-Genomics student-project-14_group-NullHypothesisEvaluationCriteria student-project-15_group-FinancialDataStreams student-project-16_group-IntrusionDetection student-project-17_group-TowardsScalableTDA student-project-18_group-ProjectRL student-project-19_group-Featuring student-project-20_group-Generalization student-project-21_group-GraphSpectralAnalysis student-project-22_group-SwapWithDDP voluntary-student-project-01_group-DDLInMining"
#
######################################################################

#rm -r $MDBOOKdir/*-sds-*/src/contents/* && #first clean the md files to avoid pre-pumped files
#cd $MDBOOKdir <---- This is wrong ! We are not inside the container yet !

#docker run --rm  -it --name=haskell-pinot --env-file env.list --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/haskell-pinot:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/pinotMdBook.sh $module &&
#docker run --rm  -it --name=rust-mdbook --env-file env.list --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/rust-mdbook:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/rustMdBook.sh $module

modules='000_0-sds-3-x-projects'

for module in $modules
do

docker run --rm  -it --name=haskell-pinot --env-file env.list --mount type=bind,source=${HOME}/all/git/,destination=/root/GIT lamastex/haskell-pinot:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/pinotMdBook.sh $module

docker run --rm  -it --name=rust-mdbook --env-file env.list --mount type=bind,source=${HOME}/all/git/,destination=/root/GIT lamastex/rust-mdbook:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/rustMdBook.sh $module

#  echo done $module
done

pushd ${HOME}/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/
echo "pwd before chown chgrp"
pwd
## docker runs as root, so we need to reown it
sudo chown -R $USER ../mdScaDaMaLeBook/
sudo chgrp -R $USER ../mdScaDaMaLeBook/

popd

## to view the built book
#firefox ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_1-sds-3-x/book/index.html
