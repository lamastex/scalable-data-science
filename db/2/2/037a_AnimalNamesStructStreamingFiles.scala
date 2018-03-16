// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/fXo7AfE3Umg/0.jpg)](https://www.youtube.com/embed/fXo7AfE3Umg?start=0&end=2670&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/sAttqpQq4nA/0.jpg)](https://www.youtube.com/embed/sAttqpQq4nA?start=0&end=2630&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/byAZtT_EdO4/0.jpg)](https://www.youtube.com/embed/byAZtT_EdO4?start=0&end=1045&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/bhHH74vkqHE/0.jpg)](https://www.youtube.com/embed/bhHH74vkqHE?start=0&end=487&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC # Write files with animal names continuously for structured streaming
// MAGIC 
// MAGIC This notebook can be used to write files every 2 seconds into the distributed file system where each of these files contains a row given by the time stamp and two animals chosen at random from six animals in a `animals.txt` file in the driver.
// MAGIC 
// MAGIC After running the commands in this notebook you should have a a set of files named by the minute and second for easy setting up of structured streaming jobs in another notebook. This is mainly to create a structured streaming of files for learning purposes. In a real situation, you will have such streams coming from more robust ingestion frameworks such as kafka queues.
// MAGIC 
// MAGIC It is a good idea to understand how to run executibles from the driver to set up a stream of files for ingestion in structured streaming tasks down stream. 
// MAGIC 
// MAGIC The following *seven steps (Steps 0-6)* can be used in more complex situations like running a more complex simulator from an executible file.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 0
// MAGIC let's get our bearings and prepare for setting up a structured streaming from files.

// COMMAND ----------

// MAGIC %md
// MAGIC Just find the working directory using `%sh`.

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd

// COMMAND ----------

// MAGIC %md
// MAGIC We are in `databricks/driver` directory.

// COMMAND ----------

// MAGIC %md
// MAGIC To run the script and be able to kill it you need a few installs.

// COMMAND ----------

// MAGIC %sh
// MAGIC apt-get install -y psmisc

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 1
// MAGIC Let's first make the `animals.txt` file in the driver.

// COMMAND ----------

// MAGIC %sh
// MAGIC rm -f animals.txt &&
// MAGIC echo "cat" >> animals.txt &&
// MAGIC echo "dog" >> animals.txt &&
// MAGIC echo "owl" >> animals.txt &&
// MAGIC echo "pig" >> animals.txt &&
// MAGIC echo "bat" >> animals.txt &&
// MAGIC echo "rat" >> animals.txt &&
// MAGIC cat animals.txt

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 2
// MAGIC Now let's make a `bash` shell script that can be executed every two seconds to produce the desired `.log` files with names prepended by minute and second inside the local directory `logsEvery2Secs`. Each line the file `every2SecsRndWordsInFiles.sh` is explained line by line:
// MAGIC 
// MAGIC * `#!/bin/bash` is how we tell that this is a `bash` script which needs the `/bin/bash` binary. I remember the magic two characters `#!` as "SHA-BANG" for "hash" for `#` and "bang" for `!`
// MAGIC * `rm -f every2SecsRndWordsInFiles.sh &&` forcefully removes the file `every2SecsRndWordsInFiles.sh` and `&&` executes the command preceeding it before going to the next line
// MAGIC * `echo "blah" >> every2SecsRndWordsInFiles.sh` just spits out the content of the string, i.e., `blah`, in append mode due to `>>` into the file `every2SecsRndWordsInFiles.sh`
// MAGIC 
// MAGIC The rest of the commands simply create a frsh directory `logsEvery2Secs` and write two randomly chosen animals from the `animals.txt` file into the directory `logsEvery2Secs` with `.log` file names preceeded by minute and second of current time to make a finite number of file names (at most 3600 unique `.log` filenames).

// COMMAND ----------

// MAGIC %sh
// MAGIC rm -f every2SecsRndWordsInFiles.sh &&
// MAGIC echo "#!/bin/bash" >> every2SecsRndWordsInFiles.sh &&
// MAGIC echo "rm -rf logsEvery2Secs" >> every2SecsRndWordsInFiles.sh &&
// MAGIC echo "mkdir -p logsEvery2Secs" >> every2SecsRndWordsInFiles.sh &&
// MAGIC echo "while true; do echo \$( date --rfc-3339=second )\; | cat - <(shuf -n2 animals.txt) | sed '$!{:a;N;s/\n/ /;ta}' > logsEvery2Secs/\$( date '+%M_%S.log' ); sleep 2; done" >> every2SecsRndWordsInFiles.sh &&
// MAGIC cat every2SecsRndWordsInFiles.sh

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 3
// MAGIC Time to run the script!

// COMMAND ----------

// MAGIC %md
// MAGIC The next two cells in `%sh` do the following:
// MAGIC 
// MAGIC * makes sure the BASH script `every2SecsRndWordsInFiles.sh` is executible 
// MAGIC * run the script in the background without hangup

// COMMAND ----------

// MAGIC %sh 
// MAGIC chmod 744 every2SecsRndWordsInFiles.sh

// COMMAND ----------

// MAGIC %sh
// MAGIC nohup ./every2SecsRndWordsInFiles.sh & 

// COMMAND ----------

// MAGIC %md
// MAGIC After executing the above cell **hit the cancel button above** to get the notebook process back. The BASH shell will still be running in the background as you can verufy by evaluating the cell below to get the time-stamped file names inside the `logsEvery2Secs` directory.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 4
// MAGIC 
// MAGIC Check that everything is running as expected.

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd
// MAGIC ls -al logsEvery2Secs

// COMMAND ----------

// MAGIC %sh
// MAGIC cat logsEvery2Secs/25_46.log

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 5
// MAGIC 
// MAGIC Next, let us prepare the distibuted file system for ingesting this data by a simple `dbutils.cp` command in a for loop with a 5 second delay between each copy from the local file system where the BASH script is writing to.
// MAGIC 
// MAGIC We use this method of running a BASH script and copying from the local file system to the distributed one in order to mimic arbirary file contents by merely changing the bash script.

// COMMAND ----------

dbutils.fs.rm("/datasets/streamingFiles/",true) // this is to delete the directory before staring a job

// COMMAND ----------

var a = 0;
// for loop execution to move files from local fs to distributed fs
for( a <- 1 to 60*60/5){ 
  // you may need to replace 60*60/5 above by a smaller number like 10 or 20 in the CE depending on how many files of your quota you have used up already
  dbutils.fs.cp("file:///databricks/driver/logsEvery2Secs/","/datasets/streamingFiles/",true)
  Thread.sleep(5000L) // sleep 5 seconds
}

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 6
// MAGIC When you are done with this streaming job it is important that you `cancel` the above cell if it is still running and also terminate the BASH shell `every2SecsRndWordsInFiles.sh` in the cell below to prevent it from running "for ever"!
// MAGIC 
// MAGIC In fact, you can execture the next cell before leaving this notebook so that the job gets killed once the above `for` loop finishes after an hour. You may need to remove the `//` in the next cell before killing the bash job.

// COMMAND ----------

// MAGIC %sh
// MAGIC killall every2SecsRndWordsInFiles.sh