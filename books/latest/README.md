# Present Course Contents

We will use the following three docker containers to present the ScaDaMaLe latest course contents, as:

- archives in databricks as .dbc archives
- book: as mdbook
- site: .html, .source (.scala,.python.R,.sql), and .md

```
docker run --rm -d -it --name=python-dbcli --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:latest

docker run --rm -d -it --name=haskell-pinot --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/haskell-pinot:latest

docker run --rm -d -it --name=rust-mdbook  --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/rust-mdbook:latest
```

Execute into each container from three different shells for interactive work-flow:

```
docker exec -it python-dbcli /bin/bash
docker exec -it rust-mdbook /bin/bash
docker exec -it haskell-pinot /bin/bash
```

"Architecture" is basically to have a BASH script for every modular dockerised process.

For example, to use the databricks CLI we have `db.sh`:

```
tor 16 dec 2021 22:23:03 CET
#!/bin/bash
databricks workspace list --profile dbua-us-west
```

To run a command by `exec` when it is already up in daemon mode with `-d` do:

```
docker exec -it python-dbcli /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/db.sh
```

To run the same command in one go when there is no running container in `-d` mode:

```
docker run --rm  -it --name=python-dbcli --env-file env.list --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:latest /bin/bash /root/GIT/lamastex/scalable-data-science/books/latest/db.sh
```

