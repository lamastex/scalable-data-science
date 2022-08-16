# ScadaMaLe in Zeppelin Docker

### In the scripts folder is a collection of scripts to simplify the dbc-to-zeppelin coversion of the ScaDaMaLe course material


## 1 - Fetch archives from databricks

    Run `fetch_dbc.sh` in docker. This will download notebooks from the "scalable-data-science" workspace folder in the databricks shard and write them locally. 
    
```
cp env.list.template env.list
```

Change  `${HOME}/all/git` in docker command below to the directory containing your git repos, including:

- tilowiklund/pinot
- lamastex/scalable-data-science

Other details include databricks config file in default loation of `${HOME}/.databrickscfg`.

```
docker run --rm -it --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:withzip /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/fetch_dbc.s
```

## 2 - Download data

    Run fetchData.sh This downloads everything from the path dbfs:/datasets/sds in the databricks shard and saves it in dockerCompose/data folder. 

```
docker run --rm -it --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:withzip /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/fetch_data.sh
```

## 3 - Start/restart zeppelin server

    For all mounts to work correctly move up to the dockerCompose folder and run

    docker run -d -it -u $(id -u) -p 8080:8080 -p 4040:4040 --rm -v $PWD/logs:/logs -v $PWD/notebook:/notebook -v $PWD/data/data:/datasets/sds -e ZEPPELIN_LOG_DIR='/logs' -e ZEPPELIN_NOTEBOOK_DIR='/notebook' --name zeppelin lamastex/zeppelin-spark

    // It's annoying to have to stand in specific folders. $PWD should be changed to either a set path or to be read from an environment file. 


## 4 - Convert to zeppelin and upload to the running server

```
docker run --rm  -it --name=haskell-pinot --env-file env.list --mount type=bind,source=${HOME}/all/git/,destination=/root/GIT lamastex/haskell-pinot:zeppelin /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/db2zp.sh
```
    Go back to the dockerCompose/scripts folder and run the import.sh. 


