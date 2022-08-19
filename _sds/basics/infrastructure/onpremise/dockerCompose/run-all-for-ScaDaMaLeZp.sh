
cp env.list.template env.list

# fetch dbc files
docker run --rm -it --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:withzip /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/fetch_dbc.sh

# fetch data and rm data/tmp
docker run --rm -it --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:withzip /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/fetch_data.sh

# remove tmp folder inside data
rm -rf data/tmp


# run zeppelin
docker run -d -it -u $(id -u) --hostname localhost -p 8080:8080 -p 4040:4040 --rm -v $PWD/logs:/logs -v $PWD/notebook:/notebook -v $PWD/data:/datasets/sds  -e ZEPPELIN_LOG_DIR='/logs' -e ZEPPELIN_NOTEBOOK_DIR='/notebook' --name zeppelin lamastex/zeppelin-spark

# convert to zpln and serve

docker run --rm  -it --name=haskell-pinot --env-file env.list --mount type=bind,source=${HOME}/all/git/,destination=/root/GIT lamastex/haskell-pinot:zeppelin /bin/bash /root/GIT/lamastex/scalable-data-science/_sds/basics/infrastructure/onpremise/dockerCompose/scripts/db2zp.sh

rm -rf notebook
mkdir notebook
bash scripts/serveZpNotes.sh

rm -rf ScaDaMaLeZp
mkdir ScaDaMaLeZp
cp -r data ScaDaMaLeZp/
cp -r notebook ScaDaMaLeZp/
zip -r ScaDaMaLeZp.zip ScaDaMaLeZp
