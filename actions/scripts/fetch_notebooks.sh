apt-get -y update && apt-get install zip unzip
cd /root/temp
mkdir -p dbc
databricks workspace ls --absolute $DBC_SHARD_DIR/000_0-sds-3-x-projects-2022 > temp_modules.txt
mkdir -p /root/temp/dbc/scalable-data-science/zipped
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
    zip -r $(basename $module).dbc /root/temp/dbc/scalable-data-science/$(basename $module)/ 
    rm -r $(basename $module)
    cd ../../..
    ls -l
    pwd
  
done < temp_modules.txt
rm temp_modules.txt && rm temp_notebooks.txt
ls -al /root/temp/dbc
ls -al /root/temp/dbc/000_0-sds-3-x-projects-2022
ls -al /root/temp/dbc/scalable-data-science