// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # The two cultures

// COMMAND ----------

// MAGIC %md
// MAGIC Link to video presentation:
// MAGIC https://www.youtube.com/watch?v=NzxBRxheJ9s&feature=youtu.be

// COMMAND ----------

// MAGIC %md
// MAGIC ## 1. Introduction
// MAGIC This is the final project in the Scalable Data Science and Distributed Machine Learning (6 credits) course. Our aim is to compare and distinguish  forum threads from two of the most popular forum sites in Sweden; Familjeliv and Flashback. For this we will use both logisitic regression models and topic modelling. We will compare two different feature approaches using logistic regression; one using word occurencies as our input features and one using more advanced word2vec features. For topic modelling we will use an LDA model and observe the most significant words in each forum.
// MAGIC 
// MAGIC ![fl_vs_fb](https://kwiss.me/assets/quiz/1512031277717651576.png)
// MAGIC 
// MAGIC **Project members:**
// MAGIC - Daniel Ahlsén
// MAGIC - Martin Andersson
// MAGIC - Niklas Gunnarsson
// MAGIC - Jonathan Styrud

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. Download data
// MAGIC 
// MAGIC For this project we use data resources from the swedish research unit Språkbanken (https://spraakbanken.gu.se/)

// COMMAND ----------

def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """">
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://spraakbanken.gu.se/en/about",600))

// COMMAND ----------

var root = "dbfs:/datasets/student-project-01"
try{
  dbutils.fs.ls(root)
} catch {
  case e: java.io.FileNotFoundException => dbutils.fs.mkdirs(root)
}

display(dbutils.fs.ls(root)) 

var fl_root = "dbfs:/datasets/student-project-01/familjeliv/"
try{
  dbutils.fs.ls(fl_root)
} catch {
  case e: java.io.FileNotFoundException => dbutils.fs.mkdirs(fl_root)
}

var fb_root = "dbfs:/datasets/student-project-01/flashback/"
try{
  dbutils.fs.ls(fb_root)
} catch {
  case e: java.io.FileNotFoundException => dbutils.fs.mkdirs(fb_root)
}

display(dbutils.fs.ls(root))

// COMMAND ----------

//[   ]	familjeliv-adoption.xml.bz2	            2017-07-23 17:39	195M	 
//[   ]	familjeliv-allmanna-ekonomi.xml.bz2	    2017-09-18 13:50	838M	 
//[   ]	familjeliv-allmanna-familjeliv.xml.bz2	2017-09-19 15:49	1.1G	 
//[   ]	familjeliv-allmanna-fritid.xml.bz2	    2017-09-19 17:30	588M	 
//[   ]	familjeliv-allmanna-husdjur.xml.bz2	    2017-09-20 15:31	846M	 
//[   ]	familjeliv-allmanna-hushem.xml.bz2	    2017-09-21 12:27	1.3G	 
//[   ]	familjeliv-allmanna-kropp.xml.bz2	    2017-09-21 18:41	2.3G	 
//[   ]	familjeliv-allmanna-noje.xml.bz2	    2017-09-21 19:57	1.6G	 
//[   ]	familjeliv-allmanna-samhalle.xml.bz2	2017-09-22 03:52	5.0G	 
//[   ]	familjeliv-allmanna-sandladan.xml.bz2	2017-09-22 10:43	778M	 
//[   ]	familjeliv-anglarum.xml.bz2	            2017-07-23 18:10	336M	 
//[   ]	familjeliv-expert.xml.bz2	            2017-07-20 11:32	142M	 
//[   ]	familjeliv-foralder.xml.bz2	            2017-08-04 19:16	10G	 
//[   ]	familjeliv-gravid.xml.bz2	            2017-07-15 04:50	7.5G	 
//[   ]	familjeliv-kansliga.xml.bz2	            2017-09-05 18:13	14G	 
//[   ]	familjeliv-medlem-allmanna.xml.bz2	    2017-07-24 03:47	4.4G	 
//[   ]	familjeliv-medlem-foraldrar.xml.bz2	    2017-07-20 22:11	4.5G	 
//[   ]	familjeliv-medlem-planerarbarn.xml.bz2	2017-07-18 15:08	1.9G	 
//[   ]	familjeliv-medlem-vantarbarn.xml.bz2	2017-07-17 08:04	4.5G	 
//[   ]	familjeliv-pappagrupp.xml.bz2	        2017-06-28 16:18	38M	 
//[   ]	familjeliv-planerarbarn.xml.bz2	        2017-08-28 20:55	2.8G	 
//[   ]	familjeliv-sexsamlevnad.xml.bz2	        2017-08-25 16:39	2.3G	 
//[   ]	familjeliv-svartattfabarn.xml.bz2	    2017-07-03 07:04	2.6G

//[   ]	flashback-dator.xml.bz2	                2017-04-06 09:08	4.5G	 
//[   ]	flashback-droger.xml.bz2	            2017-04-06 08:59	3.5G	 
//[   ]	flashback-ekonomi.xml.bz2	            2017-04-06 10:53	1.2G	 
//[   ]	flashback-flashback.xml.bz2	            2017-04-05 18:16	429M	 
//[   ]	flashback-fordon.xml.bz2	            2017-04-06 12:00	1.0G	 
//[   ]	flashback-hem.xml.bz2	                2017-04-07 03:10	4.6G	 
//[   ]	flashback-kultur.xml.bz2	            2017-04-06 22:51	5.5G	 
//[   ]	flashback-livsstil.xml.bz2	            2017-04-07 00:11	1.7G	 
//[   ]	flashback-mat.xml.bz2	                2017-04-07 08:52	1.0G	 
//[   ]	flashback-ovrigt.xml.bz2	            2017-04-07 18:54	1.9G	 
//[   ]	flashback-politik.xml.bz2	            2017-04-14 17:06	9.0G	 
//[   ]	flashback-resor.xml.bz2	                2017-04-09 15:52	566M	 
//[   ]	flashback-samhalle.xml.bz2	            2017-04-12 20:36	8.3G	 
//[   ]	flashback-sex.xml.bz2	                2017-04-11 20:32	1.3G	 
//[   ]	flashback-sport.xml.bz2	                2017-04-12 22:10	3.3G	 
//[   ]	flashback-vetenskap.xml.bz2	            2017-04-14 20:34	5.8G	 

// COMMAND ----------

import sys.process._

val fl_data = Array("familjeliv-allmanna-ekonomi.xml",
                    "familjeliv-sexsamlevnad.xml")

val fb_data = Array("flashback-ekonomi.xml",
                    "flashback-sex.xml")

val url = "http://spraakbanken.gu.se/lb/resurser/meningsmangder/"
val tmp_folder_fl = "/tmp/familjeliv/"
val tmp_folder_fb = "/tmp/flashback/"

s"rm -f -r ${tmp_folder_fl}" .!! // Remove tmp folder if exists
s"rm -f -r ${tmp_folder_fb}" .!!

for (name <- fl_data){
  try{
    dbutils.fs.ls(s"${fl_root}${name}")
    println(s"${name} already exists!")
  }
  catch{
    case e: java.io.FileNotFoundException => {
      println(s"Downloading ${name} ...")
      s"wget -P ${tmp_folder_fl} ${url}${name}.bz2" .!!
      println("Unzipping ...")
      s"bzip2 -d ${tmp_folder_fl}${name}.bz2" .!!
      println("Moving ... ")
      val localpath=s"file:${tmp_folder_fl}${name}"
      dbutils.fs.mv(localpath, fl_root)
      println(s"Done ${name}!")
    }
  }
}

s"rm -f -r ${tmp_folder_fl}" .!!

for (name <- fb_data){
  try{
    dbutils.fs.ls(s"${fb_root}${name}")
    println(s"${name} already exists!")
  }
  catch{
    case e: java.io.FileNotFoundException => {
      println(s"Downloading ${name} ...")
      s"wget -P ${tmp_folder_fb} ${url}${name}.bz2" .!!
      println("Unzipping ...")
      s"bzip2 -d ${tmp_folder_fb}${name}.bz2" .!!
      println("Moving ... ")
      val localpath=s"file:${tmp_folder_fb}${name}"
      dbutils.fs.mv(localpath, fb_root)
      println(s"Done ${name}!")
    }
  }
}
s"rm -f -r ${tmp_folder_fb}" .!!
