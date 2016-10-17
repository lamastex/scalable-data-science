Docs:
 https://docs.docker.com/

Understanding Docker:
 https://docs.docker.com/engine/understanding-docker/

Getting Started: 
 https://docs.docker.com/engine/getstarted/
 
Basics here:
 https://docs.docker.com/engine/installation/linux/ubuntulinux/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/

extra
 https://wiki.debian.org/Multiarch/HOWTO#Setting_up_apt_sources

Installed in 
raazesh-Inspiron-15-7579
Sun Oct  9 06:48:29 NZDT 2016


    Start the docker daemon.

     $ sudo service docker start

    Verify docker is installed correctly.

     $ sudo docker run hello-world

    This command downloads a test image and runs it in a container. When the container runs, it prints an informational message. Then, it exits.

ALL GOOD!!!

--------------------------------------------
raazesh@raazesh-Inspiron-15-7579:~$ sudo docker run hello-world
Unable to find image 'hello-world:latest' locally
latest: Pulling from library/hello-world
c04b14da8d14: Pull complete 
Digest: sha256:0256e8a36e2070f7bf2d0b0763dbabdd67798512411de4cdcf9431a1feb60fd9
Status: Downloaded newer image for hello-world:latest

Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker Hub account:
 https://hub.docker.com

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/
--------------------------------------------

--------------------------------------------
raazesh@raazesh-Inspiron-15-7579:~$ sudo docker run -it ubuntu bash
Unable to find image 'ubuntu:latest' locally
latest: Pulling from library/ubuntu
cad964aed91d: Pull complete 
3a80a22fea63: Pull complete 
50de990d7957: Pull complete 
61e032b8f2cb: Pull complete 
9f03ce1741bf: Pull complete 
Digest: sha256:28d4c5234db8d5a634d5e621c363d900f8f241240ee0a6a978784c978fe9c737
Status: Downloaded newer image for ubuntu:latest
root@9da40e135355:/# echo "hello world! from ubuntu image pulled by docer :)"
hello world! from ubuntu image pulled by docer :)
root@9da40e135355:/# exit
raazesh@raazesh-Inspiron-15-7579:~$ 

--------------------------------------------
Here is the full history of commands to install docker in hp-EliteBook running Ubuntu 16.04:

```
   11  sudo apt install git
   12  git clone https://github.com/raazesh-sainudiin/scalable-data-science.git
   13  sudo apt-get update
   14  sudo apt-get upgrade
   15  sudo apt-get install apt-transport-https ca-certificates
   16  sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
   17  sudo apt-get install vim
   18  sudo vim /etc/apt/sources.list.d/docker.list
   19  sudo apt-get update
   20  sudo apt-get purge lxc-docker
   21  apt-cache policy docker-engine
   22  sudo apt-get upgrade
   23  sudo apt-get install linux-image-extra-$(uname -r) linux-image-extra-virtual
   24  sudo apt-get update
   25  sudo apt-get upgrade
   26  sudo apt-get install docker-engine
   27  sudo service docker start
   28  sudo docker run hello-world
   29  sudo docker run -it ubuntu bash
   30  sudo usermod -aG docker $USER
   31  docker run hello-world
```

## Build your own Docker image

From:
 * https://docs.docker.com/engine/getstarted/step_four/

```
  298  mkdir mydockerbuild
  299  ls
  300  cd mydockerbuild/
  301  ls
  302  pwd
  303  touch Dockerfile
  304  ls
  305  cat Dockerfile
```

```
$ cat Dockerfile 
FROM docker/whalesay:latest
RUN apt-get -y update && apt-get install -y fortunes
CMD /usr/games/fortune -a | cowsay
```

``` 
  306  docker build -t docker-whale .
  307  docker images
  308  docker run docker-whale

```
## Create Docker Hub Account and Tag, Push and Pull your image
From
 * https://docs.docker.com/engine/getstarted/step_five/
 * https://docs.docker.com/engine/getstarted/step_six/
```
  335  docker images
  336  docker tag b4cc5ab92ae9 raazesh/docker-whale:latest
  337  docker images
  338  docker login
  339  docker push raazesh/docker-whale
  340  docker images
  341  docker rmi -f b4cc5ab92ae9
  342  docker rmi -f docker-whale
  343  docker images
  344  docker run raazesh/docker-whale
```
