# A Miniml Docker Container to Code in Scala 

This is from the free how-to at Linux Academy

* https://linuxacademy.com/howtoguides/posts/show/topic/11919-using-docker-for-scala

Here are the commands used (see link above to make sense!)

```
mv /Users/raazsainudiin/Downloads/scala-2.12.2.tar .
mv /Users/raazsainudiin/Downloads/jdk-8u131-linux-x64.tar .
vim Dockerfile 
docker build -t raazesh/docker-scala .
docker images
docker login
docker push raazesh/docker-scala
docker run -it raazesh/docker-scala
```

Inside the container try
```
scala

>scala val a = 1+2

>scala :quit
```

To compile and run scala projects.

```
$ vi ./scala_example/hello.scala
$ cat ./scala_example/hello.scala

object hello extends App {
  println("Hello, world")
}

```

Now, run docker interactively with the directory volume mounted

```
$ docker run -it -v /full-path-to/scala_example:/scala_example raazesh/docker-scala
root@7c416127140a:/# cd /scala_example/
root@7c416127140a:/scala_example# scalac hello.scala
root@7c416127140a:/scala_example# scala hello
Hello, world

```

http://docs.scala-lang.org/getting-started-sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html
