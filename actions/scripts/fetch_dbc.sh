#!/bin/bash

## loads DBC files from databricks shard and "zips" into dbc-archive

## how to run it in docker
# docker run --rm  --env-file env.list --mount 
#type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount 
#type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:withzip /bin/bash 
#/root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/fetch_dbc.sh

set -x -v
mkdir -p /root/temp/dbc/scalable-data-science
cd /root/temp/dbc
ls -al .
#pushd $localgitdockerComposeDIRPATH
#mkdir -p $localgitdbcDIRPATH

## get the source files from databricks workspace dir /scalable-data-science
#rm -r ./scalable-data-science*
apt-get -y update && apt-get install zip unzip
databricks workspace export_dir $DBC_SHARD_DIR  ./scalable-data-science
#cp -r scalable-data-science scalable-data-science-source 
find scalable-data-science/ -type f > sds-files

## loop through the files to fetch in DBC format
while read -a F
do 
echo $F
FILEtype="$( cut -d '.' -f 2 <<< "$F" )"
G="$( cut -d '.' -f 1 <<< "$F" )"
FILE2Export=/${G}
echo $FILE2Export
H="${G%/*}/"
echo $H
rm $F
databricks workspace export -f DBC -o $FILE2Export $H
done < sds-files 

## unzip DBC files
find scalable-data-science -mindepth 1 -type d -empty -delete
find scalable-data-science/* -depth -name '*.sql' -execdir unzip -o {} \; 
find scalable-data-science/* -depth -name '*.py' -execdir unzip -o {} \; 
find scalable-data-science/* -depth -name '*.scala' -execdir unzip -o {} \; 
##unzip .py generates .python files so .py files are not overwritten and thus removed
find scalable-data-science/* -depth -name '*.py' -execdir rm {} \; 

## zip into BDC archive files for importing as files into databricks
pushd scalable-data-science
#modules='000_5-sds-2-x-geo  000_4-sds-3-x-ss    000_3-sds-3-x-st'
# 000_1-sds-3-x-sql 000_2-sds-3-x-ml xtraResources 000_3-sds-3-x-st 000_4-sds-3-x-ss 000_5-sds-2-x-geo 000_6-sds-3-x-dl 000_7-sds-3-x-ddl 000_8-sds-3-x-pri 000_9-sds-3-x-trends do
modules='000_1-sds-3-x-spark 000_1-sds-3-x-sql 000_2-sds-3-x-ml'
#modules='001_1-sds-3-x-delta'

#echo $MODULES
echo $1
mkdir -p zipped

for module in $modules
do 
zip -r ${module}.dbc $module
cp ${module}.dbc zipped
done

pwd
ls zipped
popd

## replace older dirs with latest dirs 
#rm -r $localgitdbcDIRPATH/scalable-data-science*
#mv scalable-data-science* $localgitdbcDIRPATH



