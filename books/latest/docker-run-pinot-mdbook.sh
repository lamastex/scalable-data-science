#!/bin/bash

set -x -v
set -o allexport
source env.list 
set +o allexport

#echo ${dbProfile0}
echo ${localgitdbcDIRPATH}
echo ${localgitsiteDIRPATH}
echo ${localgitdbcDIR}
echo ${?}
echo ${PINOTdir}
echo ${MDBOOKdir}

echo "done echoing env variables in use inside docker"

modules="000_1-sds-3-x " #000_2-sds-3-x-ml #000_3-sds-3-x-st 000_4-sds-3-x-ss 000_5-sds-2-x-geo 000_6-sds-3-x-dl 000_7-sds-3-x-ddl 000_8-sds-3-x-pri xtraResources 000_9-sds-3-x-trends
#rm -r $MDBOOKdir/*-sds-*/src/contents/* && #first clean the md files to avoid pre-pumped files
cd $MDBOOKdir
for module in $modules
do
docker run --rm  -it --name=haskell-pinot --env-file env.list --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/haskell-pinot:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/pinotMdBook.sh $module &&
docker run --rm  -it --name=rust-mdbook --env-file env.list --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/rust-mdbook:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/rustMdBook.sh $module
#  echo done $module
done

## docker runs as root, so we need to reown it
sudo chown -R $USER ../mdScaDaMaLeBook/
sudo chgrp -R $USER ../mdScaDaMaLeBook/

## to view the built book 
#firefox ~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook/000_1-sds-3-x/book/index.html
