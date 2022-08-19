
cp env.list.template env.list

# fetch dbc files
docker run --rm -it --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:withzip /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/fetch_dbc.sh

# fetch datasets for sds
docker run --rm -it --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:withzip /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/fetch_data.sh

# remove tmp folder inside data
rm -rf datasets-sds/tmp


# run zeppelin from docker-compose
docker-compose -f docker-compose-sds-zeppelin.yml up -d

# convert to zpln and serve

docker run --rm  -it --name=haskell-pinot --env-file env.list --mount type=bind,source=${HOME}/all/git/,destination=/root/GIT lamastex/haskell-pinot:zeppelin /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/db2zp.sh

rm -rf notebook
echo "!!!!!! refresh notes in zeppelin server now"
sleep 10
mkdir notebook
bash scripts/serveZpNotes.sh

sleep 30
docker-compose -f docker-compose-sds-zeppelin.yml down

rm -rf ScaDaMaLeZp
mkdir ScaDaMaLeZp
mkdir ScaDaMaLeZp/logs
cp docker-compose-sds-zeppelin.yml ScaDaMaLeZp/docker-compose.yml
cp -r confZp ScaDaMaLeZp/ 
cp -r datasets-sds ScaDaMaLeZp/
rm -rf notebook/.git 
cp -r notebook ScaDaMaLeZp/
zip -r ScaDaMaLeZp.zip ScaDaMaLeZp

# Minimal Student Instructions

```
# install docker-compose with prerequisites from https://docs.docker.com/compose/install/

$ download ScaDaMaLeZp.zip from course website instructions
$ unzip ScaDaMaLeZp.zip
$ cd ScaDaMaLeZp
$ ls
confZp			docker-compose.yml	notebook
datasets-sds		logs
$ docker-compose up -d
Creating network "scadamalezp_default" with the default driver
Creating scadamalezp_zeppelin_1 ... done
$ # browse http://localhost:8080/ for zeppelin and http://localhost:4040/ for SparkUI
$ # when done with work
$ docker-compose down
```
