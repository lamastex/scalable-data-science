# Present Course Contents

We will use the following three docker containers to present the ScaDaMaLe latest course contents, as:

- archives in databricks as .dbc archives
- book: as mdbook
- site: .html, .source (.scala,.python.R,.sql), and .md

```
docker run --rm -d -it --name=python-dbcli --mount type=bind,readonly,source=${HOME}/.databrickscfg,destination=/root/.databrickscfg --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/python-dbcli:latest

docker run --rm -d -it --name=haskell-pinot --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/haskell-pinot:latest

docker run --rm -d -it --name=rust-mdbook  --mount type=bind,source=${HOME}/all/git,destination=/root/GIT lamastex/rust-mdbook:latest
```

Execute into each container from three different shells for interactive work-flow:

```
docker exec -it python-dbcli /bin/bash
docker exec -it rust-mdbook /bin/bash
docker exec -it haskell-pinot /bin/bash
```
