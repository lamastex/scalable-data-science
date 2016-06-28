// Databricks notebook source exported at Tue, 28 Jun 2016 06:58:47 UTC


# Old Bailey Online Data Analysis in Apache Spark

2016, by Raaz Sainudiin and James Smithies is licensed under [Creative Commons Attribution-NonCommercial 4.0 International License](http://creativecommons.org/licenses/by-nc/4.0/).

The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/xtraResources/OldBaileyOnline/OBO_LoadExtract.html) of this databricks notebook.

This is a starting point for ETL of Old Bailey Online Data from [http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/index.html](http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/index.html).

This work merely builds on [Old Bailey Online by Clive Emsley, Tim Hitchcock and Robert Shoemaker](https://www.oldbaileyonline.org/) that is licensed under a Creative Commons Attribution-NonCommercial 4.0 International License. Permissions beyond the scope of this license may be available at https://www.oldbaileyonline.org/static/Legal-info.jsp. 




 
The data is already loaded in dbfs (see dowloading and loading section below for these details).


```scala

1+1 // sanity check!

```
```scala

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/"))

```
```scala

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/ordinarysAccounts"))

```
```scala

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/sessionsPapers"))

```



## Extract, Transform and Load XML files to get DataFrame of counts

Let's parse the xml files and turn into Dataframe.

We have played enough (see **Exploring data first: xml parsing in scala** below first) to understand what to do now with our xml data in order to get it converted to counts of crimes, verdicts and punishments.


```scala

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

```
```scala

dbutils.fs.rm("dbfs:/datasets/obo/processed/trialCounts",recurse=true)
trials.saveAsTextFile("dbfs:/datasets/obo/processed/trialCounts")

```
```scala

display(dbutils.fs.ls("dbfs:/datasets/obo/processed/trialCounts"))

```
```scala

val trialCountsDF = sqlContext.read.json("dbfs:/datasets/obo/processed/trialCounts")

```
```scala

trialCountsDF.printSchema

```
```scala

trialCountsDF.count

```
```scala

display(trialCountsDF)

```
```scala

val trDF = trialCountsDF.na.fill(0)

```
```scala

display(trDF)

```
```scala

display(trDF)

```




## Exploring data first: xml parsing in scala
But, first let's understand the data and its structure.


```scala

val raw = sc.wholeTextFiles("dbfs:/datasets/obo/tei/ordinarysAccounts/OA17261103.xml")

```
```scala

val raw = sc.wholeTextFiles("dbfs:/datasets/obo/tei/sessionsPapers/17930109.xml") // has data on crimes and punishments

```
```scala

//val oboTest = sc.wholeTextFiles("dbfs:/datasets/obo/tei/ordinaryAccounts/OA1693072*.xml")
val xml = raw.map( x => x._2 )
val x = xml.take(1)(0) // getting content of xml file as a string

```
```scala

val elem = scala.xml.XML.loadString(x)

```
```scala

elem

```
```scala

(elem \\ "div0").map(Node => (Node \ "@type").text) // types of div0 node, the singleton root node for the file

```
```scala

(elem \\ "div1").map(Node => (Node \ "@type").text) // types of div1 node

```
```scala

(elem \\ "div1")

```
```scala

(elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => (Node \ "@type", Node \ "@id" ))

```
```scala

val trials = (elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => (Node \ "@type", Node \ "@id", (Node \\ "rs" \\ "interp").map( n => ((n \\ "@type").text, (n \\ "@value").text ))))

```
```scala

val wantedFields = Seq("verdictCategory","punishmentCategory","offenceCategory").toSet


```
```scala

val trials = (elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => ((Node \ "@type").text, (Node \ "@id").text, (Node \\ "rs" \\ "interp")
                                                               .filter(n => wantedFields.contains( (n \\ "@type").text))
                                                               .map( n => ((n \\ "@type").text, (n \\ "@value").text ))))

```



Since there can be more than one defendant in a trial, we need to reduce by key as follows.


```scala

def reduceByKey(collection: Traversable[Tuple2[String, Int]]) = {    
    collection
      .groupBy(_._1)
      .map { case (group: String, traversable) => traversable.reduce{(a,b) => (a._1, a._2 + b._2)} }
  }

```



Let's process the coarsest data on the trial as json strings.


```scala

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

```
```scala

trials.foreach(println)

```



## Some examples to learn xml and scala


```scala

val p = new scala.xml.PrettyPrinter(80, 2)

```
```scala

p.format(elem)

```



### Better examples:

http://alvinalexander.com/scala/how-to-extract-data-from-xml-nodes-in-scala

http://alvinalexander.com/scala/scala-xml-xpath-example

 

 

XML to JSON, if you want to go this route:

http://scala-tools.org/mvnsites/liftweb-2.0/framework/scaladocs/index.html

https://mkaz.github.io/2011/05/23/how-to-convert-xml-to-json/






## Dowloading and Loading Data

First we will be downloading data from [http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/index.html](http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/index.html).

The steps below need to be done once for a give shard!

**Optional TODOs:** 
* one could just read the zip files directly (see week 10 on Beijing taxi trajectories example).
* one could just download from s3 directly


```scala

%sh
wget http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/OldBailey/OB_tei_7-2_CC-BY-NC.zip

```
```scala

%sh
pwd && ls -al

```
```scala

%sh
unzip OB_tei_7-2_CC-BY-NC.zip

```



Let's put the files in dbfs.


```scala

dbutils.fs.mkdirs("dbfs:/datasets/obo/tei") //need not be done again!

```
```scala

 dbutils.fs.cp("file:/databricks/driver/tei", "dbfs:/datasets/obo/tei/",recurse=true) // already done and it takes 1500 seconds - a while!

```
```scala

display(dbutils.fs.ls("dbfs:/datasets/obo/tei/ordinarysAccounts"))

```
```scala

util.Properties.versionString // check scala version```
