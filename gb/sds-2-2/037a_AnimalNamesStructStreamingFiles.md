[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Archived YouTube video of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/fXo7AfE3Umg/0.jpg)](https://www.youtube.com/embed/fXo7AfE3Umg?start=0&end=2670&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/sAttqpQq4nA/0.jpg)](https://www.youtube.com/embed/sAttqpQq4nA?start=0&end=2630&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/byAZtT_EdO4/0.jpg)](https://www.youtube.com/embed/byAZtT_EdO4?start=0&end=1045&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/bhHH74vkqHE/0.jpg)](https://www.youtube.com/embed/bhHH74vkqHE?start=0&end=487&autoplay=1)

Write files with animal names continuously for structured streaming
===================================================================

This notebook can be used to write files every 2 seconds into the distributed file system where each of these files contains a row given by the time stamp and two animals chosen at random from six animals in a `animals.txt` file in the driver.

After running the commands in this notebook you should have a a set of files named by the minute and second for easy setting up of structured streaming jobs in another notebook. This is mainly to create a structured streaming of files for learning purposes. In a real situation, you will have such streams coming from more robust ingestion frameworks such as kafka queues.

It is a good idea to understand how to run executibles from the driver to set up a stream of files for ingestion in structured streaming tasks down stream.

The following *seven steps (Steps 0-6)* can be used in more complex situations like running a more complex simulator from an executible file.

Step 0
------

let's get our bearings and prepare for setting up a structured streaming from files.

Just find the working directory using `%sh`.

``` sh
pwd
```

>     /databricks/driver

We are in `databricks/driver` directory.

To run the script and be able to kill it you need a few installs.

``` sh
apt-get install -y psmisc
```

>     Reading package lists...
>     Building dependency tree...
>     Reading state information...
>     psmisc is already the newest version (22.21-2.1build1).
>     The following package was automatically installed and is no longer required:
>       libgnutls-openssl27
>     Use 'sudo apt autoremove' to remove it.
>     0 upgraded, 0 newly installed, 0 to remove and 100 not upgraded.

Step 1
------

Let's first make the `animals.txt` file in the driver.

``` sh
rm -f animals.txt &&
echo "cat" >> animals.txt &&
echo "dog" >> animals.txt &&
echo "owl" >> animals.txt &&
echo "pig" >> animals.txt &&
echo "bat" >> animals.txt &&
echo "rat" >> animals.txt &&
cat animals.txt
```

>     cat
>     dog
>     owl
>     pig
>     bat
>     rat

Step 2
------

Now let's make a `bash` shell script that can be executed every two seconds to produce the desired `.log` files with names prepended by minute and second inside the local directory `logsEvery2Secs`. Each line the file `every2SecsRndWordsInFiles.sh` is explained line by line:

-   `#!/bin/bash` is how we tell that this is a `bash` script which needs the `/bin/bash` binary. I remember the magic two characters `#!` as "SHA-BANG" for "hash" for `#` and "bang" for `!`
-   `rm -f every2SecsRndWordsInFiles.sh &&` forcefully removes the file `every2SecsRndWordsInFiles.sh` and `&&` executes the command preceeding it before going to the next line
-   `echo "blah" >> every2SecsRndWordsInFiles.sh` just spits out the content of the string, i.e., `blah`, in append mode due to `>>` into the file `every2SecsRndWordsInFiles.sh`

The rest of the commands simply create a frsh directory `logsEvery2Secs` and write two randomly chosen animals from the `animals.txt` file into the directory `logsEvery2Secs` with `.log` file names preceeded by minute and second of current time to make a finite number of file names (at most 3600 unique `.log` filenames).

``` sh
rm -f every2SecsRndWordsInFiles.sh &&
echo "#!/bin/bash" >> every2SecsRndWordsInFiles.sh &&
echo "rm -rf logsEvery2Secs" >> every2SecsRndWordsInFiles.sh &&
echo "mkdir -p logsEvery2Secs" >> every2SecsRndWordsInFiles.sh &&
echo "while true; do echo \$( date --rfc-3339=second )\; | cat - <(shuf -n2 animals.txt) | sed '$!{:a;N;s/\n/ /;ta}' > logsEvery2Secs/\$( date '+%M_%S.log' ); sleep 2; done" >> every2SecsRndWordsInFiles.sh &&
cat every2SecsRndWordsInFiles.sh
```

>     #!/bin/bash
>     rm -rf logsEvery2Secs
>     mkdir -p logsEvery2Secs
>     while true; do echo $( date --rfc-3339=second )\; | cat - <(shuf -n2 animals.txt) | sed '{:a;N;s/\n/ /;ta}' > logsEvery2Secs/$( date '+%M_%S.log' ); sleep 2; done

Step 3
------

Time to run the script!

The next two cells in `%sh` do the following:

-   makes sure the BASH script `every2SecsRndWordsInFiles.sh` is executible
-   run the script in the background without hangup

``` sh
chmod 744 every2SecsRndWordsInFiles.sh
```

``` sh
nohup ./every2SecsRndWordsInFiles.sh & 
```

After executing the above cell **hit the cancel button above** to get the notebook process back. The BASH shell will still be running in the background as you can verufy by evaluating the cell below to get the time-stamped file names inside the `logsEvery2Secs` directory.

Step 4
------

Check that everything is running as expected.

``` sh
pwd
ls -al logsEvery2Secs
```

>     /databricks/driver
>     total 72
>     drwxr-xr-x 2 root root 4096 Nov 22 09:26 .
>     drwxr-xr-x 1 root root 4096 Nov 22 09:25 ..
>     -rw-r--r-- 1 root root   35 Nov 22 09:25 25_44.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:25 25_46.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:25 25_48.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:25 25_50.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:25 25_52.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:25 25_54.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:25 25_56.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:25 25_58.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:26 26_00.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:26 26_02.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:26 26_04.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:26 26_06.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:26 26_08.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:26 26_10.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:26 26_12.log
>     -rw-r--r-- 1 root root   35 Nov 22 09:26 26_14.log

``` sh
cat logsEvery2Secs/25_46.log
```

>     2017-11-22 09:25:46+00:00; bat pig

Step 5
------

Next, let us prepare the distibuted file system for ingesting this data by a simple `dbutils.cp` command in a for loop with a 5 second delay between each copy from the local file system where the BASH script is writing to.

We use this method of running a BASH script and copying from the local file system to the distributed one in order to mimic arbirary file contents by merely changing the bash script.

``` scala
dbutils.fs.rm("/datasets/streamingFiles/",true) // this is to delete the directory before staring a job
```

>     res6: Boolean = true

``` scala
var a = 0;
// for loop execution to move files from local fs to distributed fs
for( a <- 1 to 60*60/5){ 
  // you may need to replace 60*60/5 above by a smaller number like 10 or 20 in the CE depending on how many files of your quota you have used up already
  dbutils.fs.cp("file:///databricks/driver/logsEvery2Secs/","/datasets/streamingFiles/",true)
  Thread.sleep(5000L) // sleep 5 seconds
}
```

Step 6
------

When you are done with this streaming job it is important that you `cancel` the above cell if it is still running and also terminate the BASH shell `every2SecsRndWordsInFiles.sh` in the cell below to prevent it from running "for ever"!

In fact, you can execture the next cell before leaving this notebook so that the job gets killed once the above `for` loop finishes after an hour. You may need to remove the `//` in the next cell before killing the bash job.

``` sh
killall every2SecsRndWordsInFiles.sh
```

