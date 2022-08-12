#!/bin/bash

mkdir -p dbcExportTestDelete
databricks workspace ls --absolute /scalable-data-science > temp_modules.txt
while read module; do
    echo $module
    mkdir -p dbcExportTestDelete$module
    databricks workspace ls $module > temp_notebooks.txt
    cat temp_notebooks.txt | xargs -I '{}' databricks workspace export --format DBC --overwrite $module/'{}' dbcExportTestDelete$module
    cd dbcExportTestDelete$module

    unzip -o \*.scala
    unzip -o \*.sql
    unzip -o \*.py
    rm *.py
    cd ..
    zip -r $(basename $module).dbc $(basename $module)/ 
    rm -r $(basename $module)
    cd ../..
  
  
done < temp_modules.txt


