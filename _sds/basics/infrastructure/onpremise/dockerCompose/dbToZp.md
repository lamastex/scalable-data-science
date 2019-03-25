# Databricks notebook to Zeppelin notebook guide

This is a step by step guide to making Databricks notebooks into imported and
ready to use Zeppelin notebooks using Pinot and `zimport.py`.

1. Export the Databricks notebooks as a dbcarchive `notebooks.dbc`.
2. Use Pinot to translate the Databricks notebooks into Zeppelin notebooks:
   `stack exec -- pinot -f databricks -t zeppelin notebook.dbc -o <folder1>`
   The translated Zeppelin notebooks are then in the folder `<folder1>`.
3. Start a fresh Zeppelin server with no existing notebooks.
4. Run `python3 zimport.py <folder1>` to import the Zeppelin notebooks into the
   Zeppelin server.

The imported notebooks can be found inside the `ZEPPELIN_NOTEBOOK_DIR` folder
defined in the `zeppelin-env.sh` configuration file for Zeppelin. By default
this is the folder `notebook` inside the Zeppelin root folder. The contents of
this folder can then be moved to the corresponding folder of any other Zeppelin
instance and the notebooks will be ready to use directly.

## Using the import notebooks in a docker container

If you want to use the imported notebooks inside a docker container running a
Zeppelin server you have to mount the contents of the `ZEPPELIN_NOTEBOOK_DIR`
that you got from above as a volume in the container to the corresponding
`ZEPPELIN_NOTEBOOK_DIR` inside the container. Assuming that you are starting a
container for the `lamastex/hszeppelin` image the command to do this is `docker
run -v <folder>:/root/zeppelin-0.8.0-bin-all/notebook -p 8080:8080
lamastex/hszeppelin` where you should replace `<folder>` with the path to the
folder containing the imported Zeppelin notebooks.

The same command also works with the `docker-compose.yml` file if you replace
`docker` with `docker-compose` and `lamastex/hszeppelin` with `zeppelin`. To use
the imported notebooks with `docker-compose up` to start all services defined in
the `docker-compose.ynl` file you have to edit the `docker-compose.yml` file to
add the volume to be mounted under the `zeppelin` service.
