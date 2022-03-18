#!/bin/bash

set -x -v
set -o allexport
source env.list
set +o allexport

echo ${dbProfile0}
echo ${localgitdbcDIRPATH}
echo ${localgitsiteDIRPATH}
echo ${localgitdbcDIR}
echo "done echoing env variables in use inside docker"

## to download the source and html files of all notebook from a course module directory
## in a databricks workspace and have them update course site
docker run --rm  -it --name=python-dbcli --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/db.sh
#docker run --rm  -it --name=python-dbcli --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git/rex,destination=/root/GIT lamastex/python-dbcli:latest /bin/bash /root/GIT/rex/scalable-data-science/books/latest/db.sh

## docker runs as root, so we need to reown it
sudo chown -R $USER ../../dbcArchives/${localgitdbcDIR}
sudo chgrp -R $USER ../../dbcArchives/${localgitdbcDIR}
sudo chown -R $USER ../../_sds/3/x/db
sudo chgrp -R $USER ../../_sds/3/x/db
