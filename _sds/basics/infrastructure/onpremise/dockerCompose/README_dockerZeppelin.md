# ScadaMaLe in Zeppelin Docker

### In the scripts folder is a collection of scripts to simplify the dbc-to-zeppelin coversion of the ScaDaMaLe course material

## 0 - Stand in the right place

    Run all the following scripts while standing in the dockerCompose/scripts folder. Should be updated to work from anywhere.

## 1 - Fetch archives from databricks

    Run fetch_dbc_notebook.sh This will download notebooks from the "scalable-data-science" workspace folder in the databricks shard and write them into 
    dockerCompose/dbc. 
    
    There will be some errors since the script so far can only search one level into a folder. So "projects", for example, is not fetched correctly. 

    And it is not possible to choose specific modules for downloads. To be added.

## 2 - Download data

    Run fetchData.sh This downloads everything from the path dbfs:/datasets/sds in the databricks shard and saves it in dockerCompose/data folder. Note that the wikipedia clickstream is 1+ gb and so this script will take some time as long as that dataset is included. (est time for whole script: 10+ minutes)

## 3 - Start/restart zeppelin server

    For all mounts to work correctly move up to the dockerCompose folder and run

    docker run -d -it -u $(id -u) -p 8080:8080 -p 4040:4040 --rm -v $PWD/logs:/logs -v $PWD/notebook:/notebook -v $PWD/data/data:/datasets/sds -e ZEPPELIN_LOG_DIR='/logs' -e ZEPPELIN_NOTEBOOK_DIR='/notebook' --name zeppelin lamastex/zeppelin-spark

    // It's annoying to have to stand in specific folders. $PWD should be changed to either a set path or to be read from an environment file. 


## 4 - Convert to zeppelin and upload to the running server

    Go back to the dockerCompose/scripts folder and run the import.sh. 



## Questions

- Should these dbc, zp, data, etc folders really be created in the dockerCompose folder? It kinda clutters things up and also might have to be ignored when commiting back to github!

- Shouldn't the scripts be able to be called from anywhere? Right now they have to be called while standing in a specific folder which seems quite annoying!