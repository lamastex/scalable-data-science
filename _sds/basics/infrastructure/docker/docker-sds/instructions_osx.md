This guide will teach you how to install Docker and a Docker image containing everything you will need to run Zeppelin with all required interpreters.

## Install git
## Install Docker
## Clone git repo
## Build Docker image
* cd into * scalable-data-science/\_sds/basics/infrastructure/docker/zeppelin/dockerfiles
* sudo docker build -t zeppelin .
## Start Zeppelin
* sudo docker run --rm -p 8080:8080 zeppelin
* Open browser and go to localhost:8080
* Do the Zeppelin thing
