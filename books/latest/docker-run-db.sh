#!/bin/bash
docker run --rm  -it --name=python-dbcli --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/db.sh
