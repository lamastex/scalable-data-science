# SDS Testing Dockerfile

The SBT project folder is `/mnt`. There's currently a file `SparkPi.scala` inside 
`/mnt/src/main/scala` which will automatically be packaged by running `sbt package` inside 
`/mnt`. To package other files, put them in `/mnt`, remove `SparkPi.scala` and then run `sbt 
package`.

The `build.sbt` file contains the Spark packages that are automatically downloaded and cached by 
sbt when building the Dockerfile. If additional packages are needed, add them there.
