// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC # Extract, Transform and Load (ETL) of the SoU Addresses
// MAGIC 
// MAGIC ### A bit of bash and lynx to achieve the scraping of the state of the union addresses of the US Presidents
// MAGIC #### by Paul Brouwers 
// MAGIC ### And some Shell-level parsed-data exploration, injection into the distributed file system and testing
// MAGIC #### by Raazesh Sainudiin
// MAGIC 
// MAGIC This SoU dataset is used in the `006_WordCount` notebook.
// MAGIC 
// MAGIC The code below is mainly there to show how the text content of each state of the union address was scraped from the following URL:
// MAGIC * [http://stateoftheunion.onetwothree.net/texts/index.html](http://stateoftheunion.onetwothree.net/texts/index.html)
// MAGIC 
// MAGIC Such data acquisition task or ETL is usually the first and crucial step in a data scientist's workflow.
// MAGIC A data scientist generally does the scraping and parsing of the data by her/himself.  
// MAGIC Data ingestion not only allows the scientist to start the analysis but also determines the quality of the analysis by the limits it imposes on the accessible feature space.
// MAGIC 
// MAGIC We have done this and put the data in the distributed file system for easy loading into our notebooks for further analysis.  This keeps us from having to install unix programs like ``lynx``, ``sed``, etc. that are needed in the shell script below.
// MAGIC 
// MAGIC ```%sh
// MAGIC for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html | grep texts | grep -v index | sed 's/.*http/http/') ; do lynx --dump $i | tail -n+13 | head -n-14 | sed 's/^\s\+//' | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt ; done
// MAGIC ```
// MAGIC 
// MAGIC Or in a more atomic form:
// MAGIC 
// MAGIC ```%sh
// MAGIC for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html \
// MAGIC 
// MAGIC         | grep texts \
// MAGIC 
// MAGIC         | grep -v index \
// MAGIC 
// MAGIC         | sed 's/.*http/http/')
// MAGIC 
// MAGIC do 
// MAGIC 
// MAGIC         lynx --dump $i \
// MAGIC 
// MAGIC                | tail -n+13 \
// MAGIC 
// MAGIC                | head -n-14 \
// MAGIC 
// MAGIC                | sed 's/^\s\+//' \
// MAGIC 
// MAGIC                | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' \
// MAGIC 
// MAGIC                > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt
// MAGIC 
// MAGIC done
// MAGIC ```
// MAGIC **Don't re-evaluate!**
// MAGIC 
// MAGIC The following BASH (shell) script can be made to work on databricks cloud directly by installing the dependencies such as ``lynx``, etc.  Since we have already scraped it and put the data in our distributed file system **let's not evaluate or ``<Ctrl+Enter>`` the cell below**.  The cell is mainly there to show how it can be done (you may want to modify it to scrape other sites for other text data).

// COMMAND ----------

// MAGIC %sh
// MAGIC #remove the hash character from the line below to evaluate when needed
// MAGIC #for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html | grep texts | grep -v index | sed 's/.*http/http/') ; do lynx --dump $i | tail -n+13 | head -n-14 | sed 's/^\s\+//' | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt ; done

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd && ls && du -sh .

// COMMAND ----------

// MAGIC %sh ls /home/ubuntu && du -sh /home/ubuntu

// COMMAND ----------

// MAGIC %md
// MAGIC We can just grab the data as a tarball (gnuZipped tar archive) file ``sou.tar.gz`` using wget as follows:
// MAGIC 
// MAGIC %sh
// MAGIC wget http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/SOU/sou.tar.gz

// COMMAND ----------

// MAGIC %sh
// MAGIC df -h
// MAGIC pwd

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd
// MAGIC ls

// COMMAND ----------

// MAGIC %sh
// MAGIC wget http://lamastex.org/datasets/public/SOU/README.md
// MAGIC cat README.md

// COMMAND ----------

// MAGIC %sh
// MAGIC # wget http://lamastex.org/datasets/public/SOU/sou.tar.gz
// MAGIC wget http://lamastex.org/datasets/public/SOU/sou.tgz 

// COMMAND ----------

// MAGIC %sh
// MAGIC ls

// COMMAND ----------

// MAGIC %sh
// MAGIC env

// COMMAND ----------

// MAGIC %sh
// MAGIC tar zxvf sou.tgz

// COMMAND ----------

// MAGIC %sh cd sou && ls

// COMMAND ----------

// MAGIC %sh head sou/17900108.txt

// COMMAND ----------

// MAGIC %sh tail sou/17900108.txt

// COMMAND ----------

// MAGIC %sh head sou/20150120.txt

// COMMAND ----------

// MAGIC %sh tail sou/20150120.txt

// COMMAND ----------

// MAGIC %sh head sou/20170228.txt

// COMMAND ----------

// MAGIC %sh tail sou/20170228.txt

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/"))

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets"))

// COMMAND ----------

dbutils.fs.rm("dbfs:/datasets/sou/", recurse=true) //need to be done only if you want to delete such an existing directory, if any!

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/sou") //need not be done again if it already exists as desired!

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets"))

// COMMAND ----------

// MAGIC %sh pwd && ls

// COMMAND ----------

dbutils.fs.help

// COMMAND ----------

dbutils.fs.cp("file:/databricks/driver/sou", "dbfs:/datasets/sou/",recurse=true)

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/sou"))

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/"))

// COMMAND ----------

val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt")

// COMMAND ----------

sou17900108.take(5)

// COMMAND ----------

sou17900108.collect

// COMMAND ----------

sou17900108.takeOrdered(5)

// COMMAND ----------

val souAll = sc.wholeTextFiles("dbfs:/datasets/sou/*.txt")

// COMMAND ----------

souAll.count // should be 231

// COMMAND ----------

souAll.take(2)

// COMMAND ----------

