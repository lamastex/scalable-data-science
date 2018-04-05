// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/63E_Nps98Bk/0.jpg)](https://www.youtube.com/embed/63E_Nps98Bk?start=1912&end=2969&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Old Bailey Online Data Analysis in Apache Spark
// MAGIC 
// MAGIC 2016, by Raaz Sainudiin and James Smithies is licensed under [Creative Commons Attribution-NonCommercial 4.0 International License](http://creativecommons.org/licenses/by-nc/4.0/).
// MAGIC 
// MAGIC 
// MAGIC #### Old Bailey, London's Central Criminal Court, 1674 to 1913
// MAGIC 
// MAGIC * with Full XML Data for another great project. 
// MAGIC This is a starting point for ETL of Old Bailey Online Data from [http://lamastex.org/datasets/public/OldBailey/index.html](http://lamastex.org/datasets/public/OldBailey/index.html).
// MAGIC 
// MAGIC This work merely builds on [Old Bailey Online by Clive Emsley, Tim Hitchcock and Robert Shoemaker](https://www.oldbaileyonline.org/) that is licensed under a Creative Commons Attribution-NonCommercial 4.0 International License. Permissions beyond the scope of this license may be available at https://www.oldbaileyonline.org/static/Legal-info.jsp. 

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML(frameIt("https://www.oldbaileyonline.org/", 450))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### This exciting dataset is here for a course project in digital humanities
// MAGIC 
// MAGIC #### To understand the extraction job we are about to do here:
// MAGIC 
// MAGIC * see [Jasper Mackenzie, Raazesh Sainudiin, James Smithies and Heather Wolffram, A nonparametric view of the civilizing process in London's Old Bailey, Research Report UCDMS2015/1, 32 pages, 2015](http://lamastex.org/preprints/20150828_civilizingProcOBO.pdf).

// COMMAND ----------

// MAGIC %md 
// MAGIC The data is already loaded in dbfs (see dowloading and loading section below for these details).

// COMMAND ----------

// MAGIC %md
// MAGIC # Analysing the Full Old Bailey Online Sessions Papers Dataset 
// MAGIC First **Step 0: Dowloading and Loading Data (The Full Dataset)** below should have been done on the shard.  
// MAGIC This currently cannot be done in Community Edition as the dataset is not loaded into the dbfs available in CE yet.
// MAGIC But the datset is in the academic shard and this is a walkthorugh of the Old Bailey Online data in the academic shard.
// MAGIC 
// MAGIC Let's first check that the datasets are there in the distributed file system.

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/")) // full data if you have it - not in CE!!

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/ordinarysAccounts"))

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/sessionsPapers"))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/XML", 450))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Step 1: Exploring data first: xml parsing in scala
// MAGIC But, first let's understand the data and its structure.
// MAGIC 
// MAGIC **Step 0: Dowloading and Loading Data (The Full Dataset)** should have been done already with data in dbfs alread.

// COMMAND ----------

val raw = sc.wholeTextFiles("dbfs:/datasets/obo/tei/ordinarysAccounts/OA17070912.xml")

// COMMAND ----------

val raw = sc.wholeTextFiles("dbfs:/datasets/obo/tei/sessionsPapers/17280717.xml") // has data on crimes and punishments

// COMMAND ----------

//val oboTest = sc.wholeTextFiles("dbfs:/datasets/obo/tei/ordinaryAccounts/OA1693072*.xml")
val xml = raw.map( x => x._2 )
val x = xml.take(1)(0) // getting content of xml file as a string

// COMMAND ----------

val elem = scala.xml.XML.loadString(x)

// COMMAND ----------

elem

// COMMAND ----------

// MAGIC %md
// MAGIC ## Quick Preparation
// MAGIC #### Some examples to learn xml and scala in a hurry

// COMMAND ----------

val p = new scala.xml.PrettyPrinter(80, 2)

p.format(elem)

// COMMAND ----------

// MAGIC 
// MAGIC 
// MAGIC %md
// MAGIC ### Better examples:
// MAGIC 
// MAGIC http://alvinalexander.com/scala/how-to-extract-data-from-xml-nodes-in-scala
// MAGIC 
// MAGIC http://alvinalexander.com/scala/scala-xml-xpath-example
// MAGIC 
// MAGIC #### More advanced topics:
// MAGIC 
// MAGIC https://alvinalexander.com/scala/serializing-deserializing-xml-scala-classes
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC #### XML to JSON, if you want to go this route:
// MAGIC 
// MAGIC https://stackoverflow.com/questions/9516973/xml-to-json-with-scala

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Our Parsing Problem
// MAGIC 
// MAGIC Let's dive deep on this data right away. See links above to learn xml more systematically to be able to parse other subsets of the data for your own project.
// MAGIC 
// MAGIC For now, we will jump in to parse the input data of counts used in [Jasper Mackenzie, Raazesh Sainudiin, James Smithies and Heather Wolffram, A nonparametric view of the civilizing process in London's Old Bailey, Research Report UCDMS2015/1, 32 pages, 2015](http://lamastex.org/preprints/20150828_civilizingProcOBO.pdf).

// COMMAND ----------

(elem \\ "div0").map(Node => (Node \ "@type").text) // types of div0 node, the singleton root node for the file

// COMMAND ----------

(elem \\ "div1").map(Node => (Node \ "@type").text) // types of div1 node

// COMMAND ----------

(elem \\ "div1")

// COMMAND ----------

(elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => (Node \ "@type", Node \ "@id" ))

// COMMAND ----------

val trials = (elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => (Node \ "@type", Node \ "@id", (Node \\ "rs" \\ "interp").map( n => ((n \\ "@type").text, (n \\ "@value").text ))))

// COMMAND ----------

val wantedFields = Seq("verdictCategory","punishmentCategory","offenceCategory").toSet

// COMMAND ----------

val trials = (elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => ((Node \ "@type").text, (Node \ "@id").text, (Node \\ "rs" \\ "interp")
                                                               .filter(n => wantedFields.contains( (n \\ "@type").text))
                                                               .map( n => ((n \\ "@type").text, (n \\ "@value").text ))))

// COMMAND ----------

// MAGIC %md
// MAGIC Since there can be more than one defendant in a trial, we need to reduce by key as follows.

// COMMAND ----------

def reduceByKey(collection: Traversable[Tuple2[String, Int]]) = {    
    collection
      .groupBy(_._1)
      .map { case (group: String, traversable) => traversable.reduce{(a,b) => (a._1, a._2 + b._2)} }
  }

// COMMAND ----------

// MAGIC %md
// MAGIC Let's process the coarsest data on the trial as json strings.

// COMMAND ----------

val trials = (elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => {val trialId = (Node \ "@id").text;
                               val trialInterps = (Node \\ "rs" \\ "interp")
                                                                 .filter(n => wantedFields.contains( (n \\ "@type").text))
                                                                 //.map( n => ((n \\ "@type").text, (n \\ "@value").text ));
                                                                 .map( n => ((n \\ "@value").text , 1 ));
                               val trialCounts = reduceByKey(trialInterps).toMap;
                               //(trialId, trialInterps, trialCounts)
                               scala.util.parsing.json.JSONObject(trialCounts updated ("id", trialId))
                              })

// COMMAND ----------

trials.foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Step 2: Extract, Transform and Load XML files to get DataFrame of counts
// MAGIC 
// MAGIC We have played enough (see **Step 1: Exploring data first: xml parsing in scala** above first) to understand what to do now with our xml data in order to get it converted to counts of crimes, verdicts and punishments.
// MAGIC 
// MAGIC Let's parse the xml files and turn into Dataframe in one block.

// COMMAND ----------

val rawWTF = sc.wholeTextFiles("dbfs:/datasets/obo/tei/sessionsPapers/*.xml") // has all data on crimes and punishments
val raw = rawWTF.map( x => x._2 )
val trials = raw.flatMap( x => { 
                       val elem = scala.xml.XML.loadString(x);
                       val outJson = (elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                           .map(Node => {val trialId = (Node \ "@id").text;
                               val trialInterps = (Node \\ "rs" \\ "interp")
                                                                 .filter(n => wantedFields.contains( (n \\ "@type").text))
                                                                 //.map( n => ((n \\ "@type").text, (n \\ "@value").text ));
                                                                 .map( n => ((n \\ "@value").text , 1 ));
                               val trialCounts = reduceByKey(trialInterps).toMap;
                               //(trialId, trialInterps, trialCounts)
                               scala.util.parsing.json.JSONObject(trialCounts updated ("id", trialId)).toString()
                              })
  outJson
})

// COMMAND ----------

dbutils.fs.rm("dbfs:/datasets/obo/processed/trialCounts",recurse=true) // let's remove the files from the previous analysis
trials.saveAsTextFile("dbfs:/datasets/obo/processed/trialCounts") // now let's save the trial counts - aboout 220 seconds to pars all data and get counts

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/processed/trialCounts"))

// COMMAND ----------

val trialCountsDF = sqlContext.read.json("dbfs:/datasets/obo/processed/trialCounts")

// COMMAND ----------

trialCountsDF.printSchema

// COMMAND ----------

trialCountsDF.count // total number of trials = 197751

// COMMAND ----------

display(trialCountsDF)

// COMMAND ----------

val trDF = trialCountsDF.na.fill(0) // filling nulls with 0

// COMMAND ----------

display(trDF)

// COMMAND ----------

// MAGIC %md
// MAGIC This is already available as the following csv file:
// MAGIC 
// MAGIC - [http://lamastex.org/datasets/public/OldBailey/oboOffencePunnishmentCountsFrom-sds-2-2-ApacheSparkScalaProcessingOfOBOXMLDoneByRaazOn20180405.csv](http://lamastex.org/datasets/public/OldBailey/oboOffencePunnishmentCountsFrom-sds-2-2-ApacheSparkScalaProcessingOfOBOXMLDoneByRaazOn20180405.csv)
// MAGIC 
// MAGIC Please cite this URL if you use this data or the Apache licensed codes in the databricks notebook above for your own non-commerical analysis:
// MAGIC 
// MAGIC  - [http://lamastex.org/datasets/public/OldBailey/](http://lamastex.org/datasets/public/OldBailey/)
// MAGIC 
// MAGIC Raazesh Sainudiin generated this header **Old bailey Processing in Apache Spark** on Thu Apr  5 18:22:43 CEST 2018 in Uppsala, Sweden.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Step 0: Dowloading and Loading Data (The Full Dataset)
// MAGIC 
// MAGIC First we will be downloading data from [http://lamastex.org/datasets/public/OldBailey/index.html](http://lamastex.org/datasets/public/OldBailey/index.html).
// MAGIC 
// MAGIC The steps below need to be done once for a give shard!
// MAGIC 
// MAGIC You can download the tiny dataset `obo-tiny/OB-tiny_tei_7-2_CC-BY-NC.zip` **to save time and space in db CE**
// MAGIC 
// MAGIC **Optional TODOs:** 
// MAGIC 
// MAGIC * one could just read the zip files directly (see week 10 on Beijing taxi trajectories example from the scalable-data-science course in 2016 or read 'importing zip files' in the Guide).
// MAGIC * one could just download from s3 directly

// COMMAND ----------

// MAGIC %sh
// MAGIC # if you want to download the tiny dataset
// MAGIC wget https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/datasets/obo-tiny/OB-tiny_tei_7-2_CC-BY-NC.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC # this is the full dataset - necessary for a project on this dataset
// MAGIC wget http://lamastex.org/datasets/public/OldBailey/OB_tei_7-2_CC-BY-NC.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd && ls -al

// COMMAND ----------

// MAGIC %md
// MAGIC Make sure you comment/uncomment the right files depending on wheter you have downloaded the tiny dataset or the big one.

// COMMAND ----------

// MAGIC %sh
// MAGIC # unzip OB-tiny_tei_7-2_CC-BY-NC.zip
// MAGIC unzip OB_tei_7-2_CC-BY-NC.zip

// COMMAND ----------

// MAGIC %md
// MAGIC Let's put the files in dbfs.

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/obo/tei") //need not be done again!

// COMMAND ----------

//dbutils.fs.rm("dbfs:/datasets/obo/tei",true)

// COMMAND ----------

// MAGIC %sh
// MAGIC ls 
// MAGIC #ls obo-tiny/tei

// COMMAND ----------

 //dbutils.fs.cp("file:/databricks/driver/obo-tiny/tei", "dbfs:/datasets/obo/tei/",recurse=true) // already done and it takes 1500 seconds - a while!
 dbutils.fs.cp("file:/databricks/driver/tei", "dbfs:/datasets/obo/tei/",recurse=true) // already done and it takes 19 minutes - a while!

// COMMAND ----------

//dbutils.fs.rm("dbfs:/datasets/tweets",true) // remove files to make room for the OBO dataset

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/"))

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/"))

// COMMAND ----------

util.Properties.versionString // check scala version