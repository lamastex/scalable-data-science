# Using docker-compose to start hsdocker images and more

The required file `docker-compose.yml` is located in the folder
`/home/squid/dan/dockerfiles/composeFiles/local`. You can either `cd` to this
directory or run every command with `-f
/home/squid/dan/dockerfiles/composeFiles/local/docker-compose.yml`. For
simplicity I will assume that your working directory is the above folder.

Please do not change anything in the folder
`/home/squid/dan/dockerfiles/composeFiles/local`. If you want to change
something in the `docker-compsoe/yml` file to change its behavior copy the
entire folder and make your changes in your copy.

## Start and stop commands

To start everything use `docker-compose up`. This will print the output from all
the containers to your shell and attach to them. Using Ctrl-C will, like normal,
send the signal to stop the containers. If you do not want your shell to be
filled with output you can use `docker-compose -d up` to fork it to background.
With this command you can no longer use Ctrl-C to stop the containers. Ports
will automatically be published as defined in the `docker-compose.yml` file.

To stop and remove all containers use `docker-compose down`. To only stop but
not remove the containers, e.g. if you want to continue working later, use
`docker-compose stop`.

To start all stopped containers use `docker-compose start`.

If you only want to start a single container you can use `docker-compose run
<service>` where `<service>` is the name of one of the services defined in the
`docker-compose.yml` file you're using. For now the defined services are
`hadoop` for hdfs and Yarn, `zeppelin` for Zeppelin, `jupyter` for Jupyter and
`nifi` for Nifi. It works like the normal `docker run` command in that you have
to use `-p` to publish ports except that you don't need to use `-it` to access
the shell since it will automatically attach input and output. Instead you have
to use `-T` if you don't want it to attach input and output.

Lastly if you want to attach to a running container you can use `docker-compose
exec <service> <command>`. This will attach to the service `<service>` and run
the command `<command>`. For example, if you want to access a shell inside a
running `jupyter` service you can use `docker-compose exec jupyter bash`.

## Notes

Some important notes for each application included are:

- **Spark**: The Spark webUI is published to port 7070 to not collide with
  Zeppelin. The Spark worker webUI is not published by default.
- **hdfs**: By default the namenode will always be formatted when using
  `docker-compose up`. To change this go into `docker-compose.yml` and remove
  the line `command: --format` from the `hadoop` service.
- **Yarn**: By default only the port for the central resource manager webUI is
  published.
- **Zeppelin**: Nothing I can think of.
- **Jupyter**: The published port is 8889 instead of the standard 8888 but only
  because there's currently a running sagemath container using 8888. Since
  finding the token in the torrent of text being output to the shell when using
  `docker-compose up` I have set up the `docker-compose.yml` file so that it
  will define the password `lamastex` for the Jupyter notebook server. To change
  this password change the word `lamastex` in the line `command: --password
  lamastex` under the `jupyter` service. If you want the token instead of a
  password simply remove the entire line.
- **Nifi**: The port for the webUI is published to port 9090 to not collide with
  Zeppelin.
- **Kafka**: The default port for the built-in Zookeeper is 2181 and the default
  port for Kafka clients is 9092.
