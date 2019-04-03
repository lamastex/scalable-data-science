# Set Up

Please setup for the workshop/course.

## 1. Cloud-full Computing Environment

### TASK 1. Create an account at databricks 

[databricks](https://databricks.com/), the commercial arm of the creators of Apache Spark, provides a free and managed Spark micro-cluster in the cloud (currently AWS) for learning purposes to anyone! 
They also intended to let these accounts remain open "indefinitely" (or at least in the forseeable future).
This is our most convenient way to learn Spark.

#### Steps to complete TASK 1

1. Go to *Databricks Community Edition* at the following URL:
  - https://community.cloud.databricks.com/
2. Signup for the databricks community edition account
  - use your private email address as your account name, so you can continue working on learning Spark
  - fill in your employer details, job description, etc.
  - say you are trying to learn Spark as the reason for requesting the community edition account
  - after submitting the form, follow instructions including checking your email address used for the account (check junk mailbox, in case it is not in your inbox)

## 2. Cloud-free Computing Environment

### TASK 2. Set up Docker for your laptop

#### Steps to complete TASK 2

##### Step 1 for TASK 2: Install docker

Follow the steps to install docker for your laptop (all three OSs are supported) from the following URL:

- https://docs.docker.com/install/

**NOTES:** 

1. Check if your laptop has virtual memory enabled.
2. You may need to increase the permitted size of the disk image to around 112GB if possible for the full docker compositions that are possible

**NOTES for Windows 7 OS**

* WIndows 7 OS is limited to docker-toolbox:
- Install docker toolbox for Windows 7 (currently the URL is the following):
  - https://docs.docker.com/toolbox/toolbox_install_windows/
- Overview of Docker-toolbox:
  - https://docs.docker.com/toolbox/overview/
- Manual for Docker:
  - https://docs.docker.com/docker-for-windows/
- Once you install, please run docker outside of closed networks so that it can download all necessary libraries.

##### Step 2 for TASK 2: Install docker

You next need to run `docker-compose` from an open network environment as instructed in the following URLs in more details:

But briefly, you need to minimally do these:
```
$ pwd
$ dockerCompose/
$ docker-compose up -d
```
after downloading the zip file below for `dockerCompose`

More details are here:

- You need to download the contents of the dockerCompose directory only (will be updated via this [zipped file]()):
  - https://github.com/lamastex/scalable-data-science/blob/master/_sds/basics/infrastructure/onpremise/dockerCompose/
- Quickly getting started once you have installed docker-compose and are inside the directory containing contents of `dockerCompose`:
  - https://github.com/lamastex/scalable-data-science/blob/master/_sds/basics/infrastructure/onpremise/dockerCompose/readmes/startingNotes.md 
- https://github.com/lamastex/scalable-data-science/blob/master/_sds/basics/infrastructure/onpremise/dockerCompose/readmes/README.md 
- A lot of publicly available learning/teaching/training/researching content:
  - https://github.com/lamastex/scalable-data-science/
 

