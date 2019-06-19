To build bleeding-edge magellan that is not in maven central/spark-packages yet!

```
$ git clone https://github.com/harsha2010/magellan.git
$ cd magellan/
$ vim build.sbt # change testHadoopVersion “2.7.3” to “2.7.0”
$ sbt package
$ cp target/scala-2.11/magellan_2.11-1.0.6-SNAPSHOT.jar ~/all/git/scalable-data-science/custom-builds/jars
```
