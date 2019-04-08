# Tutorial on sbt to build spark programs in scala

- See `dockerCompose/readmes/sbt_tutorial.md` for more details on `sbt`.
- Your task is to fix the error in test and one character in source.

This is a set of commands you need once you are in the docker container running hadoop service:

```
root@4bf966fb974f:~/programs/exercises/tutorialSbtScalaSpark# sbt
[info] Loading settings for project tutorialsbtscalaspark-build from plugins.sbt,buildSettings.sbt ...
[info] Loading project definition from /root/programs/exercises/tutorialSbtScalaSpark/project
[info] Loading settings for project tutorialsbtscalaspark from build.sbt ...
[info] Set current project to tutorialsbtscalaspark (in build file:/root/programs/exercises/tutorialSbtScalaSpark/)
[info] sbt server started at local:///root/.sbt/1.0/server/39ce2246899aa258b9c5/sock
sbt:tutorialsbtscalaspark> clean
[success] Total time: 1 s, completed Apr 8, 2019 5:41:22 AM
sbt:tutorialsbtscalaspark> compile
[info] Compiling 1 Scala source to /root/programs/exercises/tutorialSbtScalaSpark/target/scala-2.11/classes ...
[info] Done compiling.
[success] Total time: 10 s, completed Apr 8, 2019 5:41:35 AM
sbt:tutorialsbtscalaspark> test
[info] Compiling 1 Scala source to /root/programs/exercises/tutorialSbtScalaSpark/target/scala-2.11/test-classes ...
[info] Done compiling.
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
[info] Run completed in 962 milliseconds.
[info] Total number of tests run: 7
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 4, failed 3, canceled 0, ignored 0, pending 0
[info] *** 3 TESTS FAILED ***
[error] Failed tests:
[error] 	org.lamastex.ex01.ListReduceSuite
[error] (Test / test) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 4 s, completed Apr 8, 2019 5:41:43 AM
sbt:tutorialsbtscalaspark> run
[info] Packaging /root/programs/exercises/tutorialSbtScalaSpark/target/scala-2.11/tutorialsbtscalaspark_2.11-0.1.0-SNAPSHOT.jar ...
[info] Done packaging.
[info] Running org.lamastex.ex01.ListReduce 
please fix one character in min(xs) to get the right minimum
For the List = List(42, 24, 3, 6, 9, -3, 26, 9)
 its sum, max and min respectively are:
 116, 42, and 42
[success] Total time: 1 s, completed Apr 8, 2019 5:41:47 AM
sbt:tutorialsbtscalaspark> 
``` 


