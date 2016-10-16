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
