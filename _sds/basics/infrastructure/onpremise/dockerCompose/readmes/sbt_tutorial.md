---
title: sbt Tutorial
permalink: /sds/basics/infrastructure/onpremise/dockerCompose/readmes/sbt_tutorial/
sidebar:
  nav: "lMenu-SDS-2.x"
author: "Raazesh Sainudiin"
author_profile: true
---

We use `sbt` for building, testing, running and programming exercises. 
This tutorial explains all `sbt` commands that you will be needing here. 

**You should have already completed:**

- [Setup and Preparation for sds-2-x](/sds/basics/instructions/prep/) 

If not, then first complete the setup and preparation instructions.

The [toolsSetups.md](toolsSetups.md) file explains how to install `sbt`.

You will be doing the following steps:

1. Starting up `sbt`
* Running the Scala Interpreter


# 1. Starting up `sbt`
In order to start `sbt`, go to `dockerCompose/` directory from a terminal/console. 
Make sure the hadoop service is running (if you `stop`ped the service then `start` it again using the container-id from `docker ps` and if you took it `down` then bring it back `up` as we did before).

Now, you can execute the commands to navigate to an a directory inside `programs` that was mounted into the container with read and write access, and call `sbt` to open the `sbt` command prompt `sbt:ex1>` as shown below.

```
:dockerCompose raazsainudiin$ docker-compose exec hadoop bash
root@4bf966fb974f:~# ls
data  hadoop-2.9.2  programs  spark-2.3.0-bin-hadoop2.7  start.sh
root@4bf966fb974f:~# cd programs/exercises/tutorialSbtScalaSpark/
root@4bf966fb974f:~/programs/exercises/tutorialSbtScalaSpark# ls
README.md  build.sbt  project  src  target
root@4bf966fb974f:~/programs/exercises/tutorialSbtScalaSpark# pwd
/root/programs/exercises/tutorialSbtScalaSpark
```

Now you should be inside `/root/programs/exercises/tutorialSbtScalaSpark` directory of the container/servicae.

Next type `sbt` as follows:

```
root@4bf966fb974f:~/programs/exercises/tutorialSbtScalaSpark# sbt
[info] Loading settings for project tutorialsbtscalaspark-build from plugins.sbt,buildSettings.sbt ...
[info] Loading project definition from /root/programs/exercises/tutorialSbtScalaSpark/project
[info] Loading settings for project tutorialsbtscalaspark from build.sbt ...
[info] Set current project to tutorialsbtscalaspark (in build file:/root/programs/exercises/tutorialSbtScalaSpark/)
[info] sbt server started at local:///root/.sbt/1.0/server/39ce2246899aa258b9c5/sock
sbt:tutorialsbtscalaspark> 
```

Now, you are in the `sbt` shell of project `tutorialsbtscalaspark` as indicated by `sbt:tutorialsbtscalaspark> `.

# 2. Running the Scala Interpreter

You can start the Scala interpreter inside `sbt` using the `console` task. 
The interpreter (also called REPL, for "read-eval-print loop") is useful for trying out snippets of Scala code. 
Note that the interpreter can only be started if there are no compilation errors in your code.

```
sbt:tutorialsbtscalaspark> console
[info] Starting scala interpreter...
Welcome to Scala 2.11.12 (OpenJDK 64-Bit Server VM, Java 1.8.0_191).
Type in expressions for evaluation. Or try :help.

scala> 

```

Let's just try some Scala code next.

```
scala> println("this is the Scala REPL or read-evaluate-print-loop")
this is the Scala REPL or read-evaluate-print-loop

scala> println("this is the ideal place to learn and play with Scala - Eclipse and other GUI IDEs have a similar REPL too.")
this is the ideal place to learn and play with Scala - Eclipse and other GUI IDEs have a similar REPL too.

scala> 1+1
res2: Int = 2

scala> List(1,2,3,4,5,6,7)
res3: List[Int] = List(1, 2, 3, 4, 5, 6, 7)

scala> val a = 1 to 7
a: scala.collection.immutable.Range.Inclusive = Range(1, 2, 3, 4, 5, 6, 7)

scala> 
```

The Scala REPL is a great place to try out code snippets.

Now exit the REPL by typing `[ctrl+d]` and get back to `sbt` shell of our project.

```
scala> 
[success] Total time: 175 s, completed Apr 8, 2019 6:17:38 AM
sbt:tutorialsbtscalaspark> 
```


# 3. Compiling your Code
The compile task will compile the source code of the `example` project which is located in the directory `src/main/scala`.

```
sbt:tutorialsbtscalaspark> compile
[success] Total time: 1 s, completed Apr 8, 2019 6:18:46 AM
```


If the source code contains errors, the error messages from the compiler will be displayed.

# 4. Testing your Code
The directory src/test/scala contains unit tests for the project. In order to run these tests in sbt, you can use the `test` command.

```
sbt:tutorialsbtscalaspark> test
[info] ListReduceSuite:
[info] - one plus one is two
[info] - one plus one is three? *** FAILED ***
[info]   org.scalatest.exceptions.TestFailedException was thrown. (ListReduceSuite.scala:57)
[info] - details why one plus one is not three *** FAILED ***
[info]   2 did not equal 3 (ListReduceSuite.scala:82)
[info] - intNotZero throws an exception if its argument is 0
[info] - sum of a few numbers
[info] - max of a few numbers
[info] - min of a few numbers *** FAILED ***
[info]   7 did not equal 0 (ListReduceSuite.scala:132)
[info] Run completed in 972 milliseconds.
[info] Total number of tests run: 7
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 4, failed 3, canceled 0, ignored 0, pending 0
[info] *** 3 TESTS FAILED ***
[error] Failed tests:
[error] 	org.lamastex.ex01.ListReduceSuite
[error] (Test / test) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 2 s, completed Apr 8, 2019 6:19:22 AM
sbt:tutorialsbtscalaspark> 
```

You should now fix these errors in:

- `dockerCompose/programs/exercises/tutorialSbtScalaSpark/src/test/scala/example/ListReduceSuite.scala'
- `dockerCompose/programs/exercises/tutorialSbtScalaSpark/src/main/scala/example/ListReduce.scala` 

and run `test` again. One of the errors can only be fixed in the `main` function.
 
First let's run the program.

# 5. Running your Code
If your project has an object with a `main` method (or an object extending the trait `App`), then you can run the code in `sbt` easily by typing `run`. 
In case `sbt` finds multiple `main` methods, it will ask you which one you want to execute.

```
sbt:tutorialsbtscalaspark> run
[info] Running org.lamastex.ex01.ListReduce 
please fix one character in min(xs) to get the right minimum
For the List = List(42, 24, 3, 6, 9, -3, 26, 9)
 its sum, max and min respectively are:
 116, 42, and 42
[success] Total time: 2 s, completed Apr 8, 2019 6:22:53 AM
sbt:tutorialsbtscalaspark> 
```

Fix the `min` function in `src/main/scala/example/ListReduce.scala` so the minimum is `-3` instead of `42` when you `run` again.

# 6. Strongly Recommended for Engineers/Developers

Spend several hours reading details of docs by starting from here: 

 - [https://www.scala-sbt.org/1.x/docs/sbt-by-example.html](https://www.scala-sbt.org/1.x/docs/sbt-by-example.html)

Note that you can have `sbt` installed on your system outside the `dockerCompose` folder (even using Eclipse or IntelliJ) and can play with spark on the local file system in standalone model. 

But having the project directory inside `dockerCompose/programs` directory will allow for testing and developing code in an environemnt with the `docker-compose`d services.  
