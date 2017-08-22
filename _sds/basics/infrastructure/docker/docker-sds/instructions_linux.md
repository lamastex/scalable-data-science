This guide will teach you how to install Docker and a Docker image containing everything you will need to run Zeppelin with all required interpreters.

## Install git
* Use your repo.
## Install Docker
1. Use your repo.
2. If not in your repo, do this.
## Clone git repo
* `git clone https://github.com/lamastex/scalable-data-science`
This will download a bunch of stuff so beware.
## Build Docker image
* `cd` into `scalable-data-science/\_sds/basics/infrastructure/docker/zeppelin/dockerfiles`
* `sudo docker build -t zeppelin .`, this will create the docker image.
This will download a bunch of stuff so beware.
## Start Zeppelin
* `sudo docker run -p 8080:8080 zeppelin`, this will create a container for the zeppelin image and start it.
* Open browser and go to `localhost:8080`
* Do the Zeppelin thing
## Restarting a Docker container
* Run `sudo docker ps -a` to list all available containers.
* Find the desired container and run `sudo docker start [name/id]` where `[name/id]` is the container name or id.
* To get access to the shell inside the running container run `sudo docker exec -it [name/id] bash` where `[name/id]` is the name or container id of the container. From bash you can then run any commands you like, e.g. starting a Spark shell by running `spark-shell`. Can also start the docker container with `sudo docker run -itp 8080:8080 zeppelin bash`. This does not start the zeppelin server automatically but rather starts a zeppelin container and puts you in bash. To start the zeppelin server you then need to run `/usr/zeppelin/bin/zeppelin.sh`. To get back to bash without closing the server you then need to use `ctrl-z` to escape the server and then run the command `bg` to allow it to run in the background. You can now use bash as normal inside the container.
