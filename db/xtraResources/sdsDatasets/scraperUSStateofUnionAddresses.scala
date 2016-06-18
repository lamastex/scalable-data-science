// Databricks notebook source exported at Sat, 18 Jun 2016 05:08:17 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/xtraResources/sdsDatasets/scraperUSStateofUnionAddresses.html) of this databricks notebook and its recorded Uji in context ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/xtraResources/sdsDatasets/scraperUSStateofUnionAddresses](http://img.youtube.com/vi/zgkvusQdNLY/0.jpg)](https://www.youtube.com/v/zgkvusQdNLY?rel=0&autoplay=1&modestbranding=1&start=4613&end=5077)

// COMMAND ----------

// MAGIC %md
// MAGIC # Extract, Transform and Load (ETL) of the SoU Addresses
// MAGIC 
// MAGIC ### A bit of bash and lynx to achieve the scraping of the state of the union addresses of the US Presidents
// MAGIC #### by Paul Brouwers 
// MAGIC ### And some Shell-level parsed-data exploration, injection into the distributed file system and testing
// MAGIC #### by Raazesh Sainudiin
// MAGIC 
// MAGIC This SoU dataset is used in the following notebooks:
// MAGIC * [006_WordCount](/#workspace/scalable-data-science/week2/03_WordCount/006_WordCount).
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
// MAGIC wget http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/SOU/sou.tar.gz

// COMMAND ----------

// MAGIC %sh
// MAGIC ls

// COMMAND ----------

// MAGIC %sh
// MAGIC env

// COMMAND ----------

// MAGIC %sh
// MAGIC tar zxvf sou.tar.gz

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

display(dbutils.fs.ls("dbfs:/"))

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets"))

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/sou") //need not be done again!

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

souAll.count

// COMMAND ----------

souAll.take(2)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)