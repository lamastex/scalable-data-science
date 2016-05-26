// Databricks notebook source exported at Wed, 27 Apr 2016 02:36:51 UTC
// MAGIC %md
// MAGIC 
// MAGIC # Old Bailey Online Data Analysis in Apache Spark
// MAGIC 
// MAGIC 2016, by Raaz Sainudiin and James Smithies is licensed under [Creative Commons Attribution-NonCommercial 4.0 International License](http://creativecommons.org/licenses/by-nc/4.0/).
// MAGIC 
// MAGIC This is a starting point for ETL of Old Bailey Online Data from [http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/index.html](http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/index.html).
// MAGIC 
// MAGIC This work merely builds on [Old Bailey Online by Clive Emsley, Tim Hitchcock and Robert Shoemaker](https://www.oldbaileyonline.org/) that is licensed under a Creative Commons Attribution-NonCommercial 4.0 International License. Permissions beyond the scope of this license may be available at https://www.oldbaileyonline.org/static/Legal-info.jsp. 

// COMMAND ----------

// MAGIC %md 
// MAGIC The data is already loaded in dbfs (see dowloading and loading section below for these details).

// COMMAND ----------

1+1 // sanity check!

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/"))

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/ordinarysAccounts"))

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/sessionsPapers"))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Extract, Transform and Load XML files to get DataFrame of counts
// MAGIC 
// MAGIC Let's parse the xml files and turn into Dataframe.
// MAGIC 
// MAGIC We have played enough (see **Exploring data first: xml parsing in scala** below first) to understand what to do now with our xml data in order to get it converted to counts of crimes, verdicts and punishments.

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

dbutils.fs.rm("dbfs:/datasets/obo/processed/trialCounts",recurse=true)
trials.saveAsTextFile("dbfs:/datasets/obo/processed/trialCounts")

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/processed/trialCounts"))

// COMMAND ----------

val trialCountsDF = sqlContext.read.json("dbfs:/datasets/obo/processed/trialCounts")

// COMMAND ----------

trialCountsDF.printSchema

// COMMAND ----------

trialCountsDF.count

// COMMAND ----------

display(trialCountsDF)

// COMMAND ----------

val trDF = trialCountsDF.na.fill(0)

// COMMAND ----------

display(trDF)

// COMMAND ----------

display(trDF)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Exploring data first: xml parsing in scala
// MAGIC But, first let's understand the data and its structure.

// COMMAND ----------

val raw = sc.wholeTextFiles("dbfs:/datasets/obo/tei/ordinarysAccounts/OA17261103.xml")

// COMMAND ----------

val raw = sc.wholeTextFiles("dbfs:/datasets/obo/tei/sessionsPapers/17930109.xml") // has data on crimes and punishments

// COMMAND ----------



// COMMAND ----------

//val oboTest = sc.wholeTextFiles("dbfs:/datasets/obo/tei/ordinaryAccounts/OA1693072*.xml")
val xml = raw.map( x => x._2 )
val x = xml.take(1)(0) // getting content of xml file as a string

// COMMAND ----------

val elem = scala.xml.XML.loadString(x)

// COMMAND ----------

elem

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
// MAGIC ## Some examples to learn xml and scala

// COMMAND ----------

val p = new scala.xml.PrettyPrinter(80, 2)

// COMMAND ----------

p.format(elem)

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC Better examples:
// MAGIC 
// MAGIC http://alvinalexander.com/scala/how-to-extract-data-from-xml-nodes-in-scala
// MAGIC 
// MAGIC http://alvinalexander.com/scala/scala-xml-xpath-example
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC  
// MAGIC 
// MAGIC XML to JSON, if you want to go this route:
// MAGIC 
// MAGIC http://scala-tools.org/mvnsites/liftweb-2.0/framework/scaladocs/index.html
// MAGIC 
// MAGIC https://mkaz.github.io/2011/05/23/how-to-convert-xml-to-json/

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Dowloading and Loading Data
// MAGIC 
// MAGIC First we will be downloading data from [http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/index.html](http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/index.html).
// MAGIC 
// MAGIC The steps nelow need to be done once for a give shard!

// COMMAND ----------

// MAGIC %sh
// MAGIC wget http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/OB_tei_7-2_CC-BY-NC.zip

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd && ls -al

// COMMAND ----------

// MAGIC %sh
// MAGIC unzip OB_tei_7-2_CC-BY-NC.zip

// COMMAND ----------

// MAGIC %md
// MAGIC Let's put the files in dbfs.

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/obo/tei") //need not be done again!

// COMMAND ----------

 dbutils.fs.cp("file:/databricks/driver/tei", "dbfs:/datasets/obo/tei/",recurse=true) // already done and it takes 1500 seconds - a while!

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/ordinarysAccounts"))

// COMMAND ----------

util.Properties.versionString // check scala version