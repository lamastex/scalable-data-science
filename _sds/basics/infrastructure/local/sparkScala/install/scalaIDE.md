---
title: Installing Scala-IDE on a Laptop
permalink: /sds/basics/infrastructure/local/sparkScala/install/scalaIDE/
sidebar:
  nav: "lMenu"
---

# Installing Apache Spark and Scala 
## in your Local Machine (PC or Laptop) 

# Windows 

1. Install a JDK (Java Development Kit) from http://www.oracle.com/technetwork/java/javase/downloads/index.html 
Keep track of where you installed the JDK as you'll need this information later.

2. Download a pre-built version of Apache Spark from https://spark.apache.org/downloads.html 

3. If necessary, download and install WinRAR from http://www.rarlab.com/download.htm so you can extract the .tgz file you downloaded 
4. Extract the Spark archive, and copy its contents into 'C:\spark' after creating that directory.  You should end up with directories like 'c:\spark\bin', 'c:\spark\conf', etc.
 
5. Download 
	* [64/winutils.exe](http://lamastex.org/downloads/software/sparkScala/local/64/winutils.exe) OR
	* [32/winutils.exe](http://lamastex.org/downloads/software/sparkScala/local/32/winutils.exe) 

and move it into a 'C:\winutils\bin' folder that you just created. 

6. Open the 'C:\spark\conf' folder and make sure 'FileNameExtensions' is checked in the 'view' tab of Windows Explorer. Then, 
	* Rename the 'log4j.properties.template' file to 'log4j.properties'. 
	* Edit this file (using Wordpad or another plain text editor) and change the error level from 'INFO' to 'ERROR' for 'log4j.rootCategory'. 

7. Right-click your Windows menu, select Control Panel, System and Security, and then System. Click on 'advanced System Settings' and then the 'Environment Variables' button and add the following new USER variables: 
	* 'SPARK_HOME'  'C:\spark' 
	* 'JAVA_HOME' (the file path where you installed JDK in step 1, for example 'C:\Program Files\Java\jdk1.8.0_101') 
	* 'HADDOP_HOME' 'C:\winutils' 

8. Add the following paths to your 'PATH' user variable: 
	* '%SPARK_HOME%\bin' 
	* '%JAVA_HOME%\bin' 

9. Close the environment variable screen and the control panels. 

10. Install the latest Scala IDE from http://scala-ide.org/download/sdk.html 

11. Test it out! 
	* Open up a Windows command prompt in administrator mode. 
	* Enter cd c:\spark and then dir to get a directory listing. 
	* Look for a text file we can play with, like README.md or CHANGES.txt 
	* Enter spark-shell 
	* At this point you should have a 'scala>' prompt. 
	* Enter 'val rdd = sc.textFile("README.md")' 
	* Enter 'rdd.count()' 
	* You should get a count of the number of lines in that file! 
	* Congratulations, you just ran your first Spark program! 
	* Hit 'control-D' to exit the spark shell, and close the console window 

12. You've got everything setup to run Spark locally!

# MacOS 

1.  Install Apache Spark using Homebrew. 
	* Install Homebrew if you don't have it already by entering this from a terminal prompt: 
	'/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"'
	* Enter 'brew install apache-spark' 
	* Create a log4j.properties file via  
	'''
	cd /usr/local/Cellar/apache-spark/2.2.0/libexec/conf 
	cp log4j.properties.template log4j.properties
	''' 
	(substituted 2.2.0 for the version actually installed) 
	* Edit the log4j.properties file and change the log level from 'INFO' to 'ERROR' on 'log4j.rootCategory'
 
2. Install the Scala IDE from http://scala-ide.org/download/sdk.html 
	* When importing spark jars in scala-ide type 'Shift-cmd-G' and put the path to the directory of spark jars: '/usr/local/Cellar/apache-spark/2.2.0/libexec/jars/'
	* Make sure that the 'Scala Compiler' in 'Properties for sparkScalaEclipseIDE' project is set to the same version of scala in Spark 2.2.0 (this is scala 2.11 as opposed to scala 2.12 that comes with scala-IDE)

3. Test it out! 
	* 'cd' to the directory apache-spark was installed (such as '/usr/local/Cellar/apache-spark/2.2.0/libexec/')  and then 'ls' to get a directory listing. 
	* Look for a text file we can play with, like 'README.md'
	* Enter spark-shell 
	* At this point you should have a 'scala>' prompt. 
	* Enter 'val rdd = sc.textFile("REDME.md")'  
	* Enter 'rdd.count()' 
	* You should get a count of the number of lines in that file! Congratulations, you just ran your first Spark program! 
	* Enter 'control-D' to exit the spark shell, and close the console window

4. You've got everything setup to run Spark locally!

# Linux 
1. Install Java, Scala, and Spark according to the particulars of your specific OS. A good starting 
point is http://www.tutorialspoint.com/apache_spark/apache_spark_installation.htm (but be sure to install Spark 2.0 or newer) 

2. Install the Scala IDE from http://scala-ide.org/download/sdk.html 

3. Test it out! 
	* 'cd' to the directory apache-spark was installed and then 'ls' to get a directory listing. 
	* Look for a text file we can play with, like 'README.md' 
	* Enter spark-shell 
	* At this point you should have a 'scala>' prompt. 
	* Enter 'val rdd = sc.textFile("REDME.md")' 
	* Enter rdd.count() 
	* You should get a count of the number of lines in that file! Congratulations, you just ran your first Spark program! 
	* Enter 'control-D' to exit the spark shell, and close the console window 

4. You've got everything setup to run Spark locally!


CREDITS: This is an adaptation of the installation guide from 'https://sundog-spark.s3.amazonaws.com/spark-install.pdf'
