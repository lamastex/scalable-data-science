To build bleeding-edge magellan that is not in maven central/spark-packages yet!

NOTE: this means you need not add other dependencies like esri-geometry, etc.

```
$ git clone https://github.com/harsha2010/magellan.git
$ cd magellan/
$ vim build.sbt # change testHadoopVersion “2.7.3” to “2.7.0”
$ sbt assembly
$ cp /home/raazesh/all/git/magellan/target/scala-2.11/magellan-assembly-1.0.6-SNAPSHOT.jar ~/all/git/scalable-data-science/custom-builds/jars/
```
