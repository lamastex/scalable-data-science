#!/bin/bash

#loads DBC files from databricks shard and "zips" into dbc-archive
apt-get -y update && apt-get install zip unzip
cd /root/temp
mkdir -p dbc
databricks workspace ls --absolute /scalable-data-science > temp_modules.txt
while read module; do
    echo $module
    mkdir -p dbc$module
    databricks workspace ls $module > temp_notebooks.txt
    cat temp_notebooks.txt | xargs -I '{}' databricks workspace export --format DBC --overwrite $module/'{}' dbc$module
    cd dbc$module

    unzip -o \*.scala
    unzip -o \*.sql
    unzip -o \*.py
    #unzip .py generates .python files so .py files are not overwritten and thus removed
    rm *.py
    cd ..
    zip -r $(basename $module).dbc $(basename $module)/ 
    rm -r $(basename $module)
    cd ../..
    ls -l
    pwd
  
done < temp_modules.txt
rm temp_modules.txt && rm temp_notebooks.txt