#!/bin/bash

#loads DBC files from databricks shard and "zips" into dbc-archive

mkdir -p dbc                            #$1
#look into only files inside the folder
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
    #add this what the heck is it doing?
    rm *.py
    cd ..
    zip -r $(basename $module).dbc $(basename $module)/ 
    rm -r $(basename $module)
    cd ../..
  
  
done < temp_modules.txt
rm temp_modules.txt && rm temp_notebooks.txt


