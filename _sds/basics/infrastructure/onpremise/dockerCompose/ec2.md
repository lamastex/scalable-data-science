# hsCompose on AWS EC2 #

## Single instance ##

- Start any Ubuntu 18.04 EC2 instance with at least 8GB of RAM (16 recommended)
  and enough disk space for your data sets. Remember to publish all the required
  ports: 
  * 8080
  * 8088
  * 8888
  * 50070
  * 8042
  * 4040
  * 4041
  * 9092
  * 2181 (maybe)
- ssh to your instance using the username `ubuntu`
- Install `docker` and `pip`: `sudo apt install -y docker.io python3-pip`
- Add the `ubuntu` user to the `docker` group: `sudo usermod -aG docker ubuntu`
- Restart the instance to update `docker` priviliges for the `ubuntu` user and
  ssh into it again. (This can be skipped but then all docker commands need to
  be run with sudo)
- Install `docker-compose`: `sudo pip3 install docker-compose`
- Clone the `hsCompose` repo: `git clone https://gitlab.com/dlilja/hscompose.git`
- `cd` into the `hscompose` folder.
- Pull all the images: `docker-compose pull`
- Start services: `docker-compose up -d`

## Scalable cluster ##
