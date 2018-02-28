[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Old Bailey Online Data Analysis in Apache Spark
===============================================

2016, by Raaz Sainudiin and James Smithies is licensed under [Creative Commons Attribution-NonCommercial 4.0 International License](http://creativecommons.org/licenses/by-nc/4.0/).

#### Old Bailey, London's Central Criminal Court, 1674 to 1913

-   with Full XML Data for another great project. This is a starting point for ETL of Old Bailey Online Data from <http://lamastex.org/datasets/public/OldBailey/index.html>.

This work merely builds on [Old Bailey Online by Clive Emsley, Tim Hitchcock and Robert Shoemaker](https://www.oldbaileyonline.org/) that is licensed under a Creative Commons Attribution-NonCommercial 4.0 International License. Permissions beyond the scope of this license may be available at https://www.oldbaileyonline.org/static/Legal-info.jsp.

<p class="htmlSandbox"><iframe 
 src="https://www.oldbaileyonline.org/"
 width="95%" height="450"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

### This exciting dataset is here for a course project in digital humanities

#### To understand the extraction job we are about to do here:

-   see [Jasper Mackenzie, Raazesh Sainudiin, James Smithies and Heather Wolffram, A nonparametric view of the civilizing process in London's Old Bailey, Research Report UCDMS2015/1, 32 pages, 2015](http://lamastex.org/preprints/20150828_civilizingProcOBO.pdf).

The data is already loaded in dbfs (see dowloading and loading section below for these details).

Analysing the Full Old Bailey Online Sessions Papers Dataset
============================================================

First **Step 0: Dowloading and Loading Data (The Full Dataset)** below should have been done on the shard.
This currently cannot be done in Community Edition as the dataset is not loaded into the dbfs available in CE yet. But the datset is in the academic shard and this is a walkthorugh of the Old Bailey Online data in the academic shard.

Let's first check that the datasets are there in the distributed file system.

``` scala
display(dbutils.fs.ls("dbfs:/datasets/obo/tei/")) // full data if you have it - not in CE!!
```

| path                                      | name               | size |
|-------------------------------------------|--------------------|------|
| dbfs:/datasets/obo/tei/ordinarysAccounts/ | ordinarysAccounts/ | 0.0  |
| dbfs:/datasets/obo/tei/sessionsPapers/    | sessionsPapers/    | 0.0  |

``` scala
display(dbutils.fs.ls("dbfs:/datasets/obo/tei/ordinarysAccounts"))
```

| path                                                    | name           | size    |
|---------------------------------------------------------|----------------|---------|
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16760517.xml | OA16760517.xml | 13712.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16760705.xml | OA16760705.xml | 13823.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16760830.xml | OA16760830.xml | 13602.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16761025.xml | OA16761025.xml | 15148.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16770316.xml | OA16770316.xml | 10591.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16770504.xml | OA16770504.xml | 12920.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16771017.xml | OA16771017.xml | 7400.0  |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16771219.xml | OA16771219.xml | 8828.0  |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16780123.xml | OA16780123.xml | 12283.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16780306.xml | OA16780306.xml | 13447.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16780417.xml | OA16780417.xml | 14149.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16780522.xml | OA16780522.xml | 14871.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16780906.xml | OA16780906.xml | 9038.0  |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16781216.xml | OA16781216.xml | 12746.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16790121.xml | OA16790121.xml | 15361.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16790122.xml | OA16790122.xml | 16586.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16790307.xml | OA16790307.xml | 11702.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16790509.xml | OA16790509.xml | 14698.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16791024.xml | OA16791024.xml | 15808.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16791219.xml | OA16791219.xml | 16256.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16800121.xml | OA16800121.xml | 14398.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16801027.xml | OA16801027.xml | 18430.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16840523.xml | OA16840523.xml | 18084.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16840526.xml | OA16840526.xml | 24861.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16840710.xml | OA16840710.xml | 20467.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16840917.xml | OA16840917.xml | 42803.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16841017.xml | OA16841017.xml | 28243.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16841217.xml | OA16841217.xml | 14500.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16841219.xml | OA16841219.xml | 32152.0 |
| dbfs:/datasets/obo/tei/ordinarysAccounts/OA16850304.xml | OA16850304.xml | 26938.0 |

Truncated to 30 rows

``` scala
display(dbutils.fs.ls("dbfs:/datasets/obo/tei/sessionsPapers"))
```

| path                                                | name          | size    |
|-----------------------------------------------------|---------------|---------|
| dbfs:/datasets/obo/tei/sessionsPapers/16740429.xml  | 16740429.xml  | 31068.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16740717.xml  | 16740717.xml  | 24513.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16740909.xml  | 16740909.xml  | 23753.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16741014.xml  | 16741014.xml  | 20711.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16741212.xml  | 16741212.xml  | 27040.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16750115.xml  | 16750115.xml  | 22886.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16750219.xml  | 16750219.xml  | 22574.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16750414.xml  | 16750414.xml  | 26252.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16750707.xml  | 16750707.xml  | 36843.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16750909.xml  | 16750909.xml  | 23963.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16751013.xml  | 16751013.xml  | 27492.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16751208.xml  | 16751208.xml  | 33176.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16760114.xml  | 16760114.xml  | 40756.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16760117.xml  | 16760117.xml  | 25941.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16760405.xml  | 16760405.xml  | 44263.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16760510.xml  | 16760510.xml  | 28347.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16760628.xml  | 16760628.xml  | 26840.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16760823.xml  | 16760823.xml  | 30461.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16761011.xml  | 16761011.xml  | 36544.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16761213.xml  | 16761213.xml  | 35569.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16770117.xml  | 16770117.xml  | 16318.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16770307.xml  | 16770307.xml  | 36113.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16770425.xml  | 16770425.xml  | 21072.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16770601.xml  | 16770601.xml  | 33137.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16770711.xml  | 16770711.xml  | 33781.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16770711A.xml | 16770711A.xml | 50298.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16770906.xml  | 16770906.xml  | 27708.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16771010.xml  | 16771010.xml  | 37695.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16771212.xml  | 16771212.xml  | 43713.0 |
| dbfs:/datasets/obo/tei/sessionsPapers/16780116.xml  | 16780116.xml  | 36628.0 |

Truncated to 30 rows

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/XML"
 width="95%" height="450"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

Step 1: Exploring data first: xml parsing in scala
--------------------------------------------------

But, first let's understand the data and its structure.

**Step 0: Dowloading and Loading Data (The Full Dataset)** should have been done already with data in dbfs alread.

``` scala
val raw = sc.wholeTextFiles("dbfs:/datasets/obo/tei/ordinarysAccounts/OA17070912.xml")
```

>     raw: org.apache.spark.rdd.RDD[(String, String)] = dbfs:/datasets/obo/tei/ordinarysAccounts/OA17070912.xml MapPartitionsRDD[33526] at wholeTextFiles at <console>:34

``` scala
val raw = sc.wholeTextFiles("dbfs:/datasets/obo/tei/sessionsPapers/17280717.xml") // has data on crimes and punishments
```

>     raw: org.apache.spark.rdd.RDD[(String, String)] = dbfs:/datasets/obo/tei/sessionsPapers/17280717.xml MapPartitionsRDD[33528] at wholeTextFiles at <console>:34

``` scala
//val oboTest = sc.wholeTextFiles("dbfs:/datasets/obo/tei/ordinaryAccounts/OA1693072*.xml")
val xml = raw.map( x => x._2 )
val x = xml.take(1)(0) // getting content of xml file as a string
```

>     xml: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[33529] at map at <console>:35
>     x: String =
>     <?xml version="1.0" encoding="UTF-8"?>
>     <TEI.2>
>        <text>
>           <body>
>              <div0 type="sessionsPaper" id="17280717">
>                 <interp inst="17280717" type="collection" value="BAILEY"/>
>                 <interp inst="17280717" type="year" value="1728"/>
>                 <interp inst="17280717" type="uri" value="sessionsPapers/17280717"/>
>                 <interp inst="17280717" type="date" value="17280717"/>
>                 <xptr type="transcription" doc="17280717"/>
>
>                 <div1 type="frontMatter" id="f17280717-1">
>                    <interp inst="f17280717-1" type="collection" value="BAILEY"/>
>                    <interp inst="f17280717-1" type="year" value="1728"/>
>                    <interp inst="f17280717-1" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="f17280717-1" type="date" value="17280717"/>
>
>                    <xptr type="pageFacsimile" doc="172807170001"/>THE PROCEEDINGS AT THE Sessions of the Peace, and Oyer and Terminer for the City of LONDON: AND
>              <p>On the King's Commission of Goal-Delivery of Newgate, held at Justice-Hall in the Old Baily, for the CITY of LONDON and COUNTY of MIDDLESEX.</p>
>                    <p>On Wednesday, Thursday, and Friday, being the 17th, 18th, and 19th of July, 1728, in the Second Year of His MAJESTY's Reign.</p>
>                    <p>(Price Six Pence)</p>
>                    <p>BEFORE the Right Honourable Sir
>                       <persName id="f17280717-1-person1" type="judiciaryName">
>                          EDWARD
>                          BECHER
>                       <interp inst="f17280717-1-person1" type="surname" value="BECHER"/>
>                          <interp inst="f17280717-1-person1" type="given" value="EDWARD"/>
>                          <interp inst="f17280717-1-person1" type="gender" value="male"/>
>                       </persName>
>                    , Lord Mayor of the City of London; the Right Honourable the Lord Chief
>                       <persName id="f17280717-1-person2" type="judiciaryName">
>                          Baron
>                          Pengelly
>                       <interp inst="f17280717-1-person2" type="surname" value="Pengelly"/>
>                          <interp inst="f17280717-1-person2" type="given" value="Baron"/>
>                          <interp inst="f17280717-1-person2" type="gender" value="male"/>
>                       </persName>
>                    ; Mr. Justice Reynolds; Mr.
>                       <persName id="f17280717-1-person3" type="judiciaryName">
>                          Baron
>                          Thompson
>                       <interp inst="f17280717-1-person3" type="surname" value="Thompson"/>
>                          <interp inst="f17280717-1-person3" type="given" value="Baron"/>
>                          <interp inst="f17280717-1-person3" type="gender" value="male"/>
>                       </persName>
>                    , Recorder of the City of London; and
>                       <persName id="f17280717-1-person4" type="judiciaryName">
>                          John
>                          Raby
>                       <interp inst="f17280717-1-person4" type="surname" value="Raby"/>
>                          <interp inst="f17280717-1-person4" type="given" value="John"/>
>                          <interp inst="f17280717-1-person4" type="gender" value="male"/>
>                       </persName>
>                    , Esq; Serjeant at Law; and other His Majesty's Justices of Goal-Delivery, and Oyer and Terminer aforesaid; Together with several of His Majesty's Justices of the Peace for the said City of London.</p>
>
>                    <p>London Jury.</p>
>                    <p>
>
>                       <persName id="f17280717-1-person5" type="jurorName">
>                          John
>                          Land
>                       <interp inst="f17280717-1-person5" type="surname" value="Land"/>
>                          <interp inst="f17280717-1-person5" type="given" value="John"/>
>                          <interp inst="f17280717-1-person5" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person6" type="jurorName">
>                          Nathaniel
>                          Mason
>                       <interp inst="f17280717-1-person6" type="surname" value="Mason"/>
>                          <interp inst="f17280717-1-person6" type="given" value="Nathaniel"/>
>                          <interp inst="f17280717-1-person6" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person7" type="jurorName">
>                          Benjamin
>                          Allibone
>                       <interp inst="f17280717-1-person7" type="surname" value="Allibone"/>
>                          <interp inst="f17280717-1-person7" type="given" value="Benjamin"/>
>                          <interp inst="f17280717-1-person7" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person8" type="jurorName">
>                          Joseph
>                          Westwood
>                       <interp inst="f17280717-1-person8" type="surname" value="Westwood"/>
>                          <interp inst="f17280717-1-person8" type="given" value="Joseph"/>
>                          <interp inst="f17280717-1-person8" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person9" type="jurorName">
>                          Gabriel
>                          Wittacre
>                       <interp inst="f17280717-1-person9" type="surname" value="Wittacre"/>
>                          <interp inst="f17280717-1-person9" type="given" value="Gabriel"/>
>                          <interp inst="f17280717-1-person9" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person10" type="jurorName">
>                          Samuel
>                          Tilley
>                       <interp inst="f17280717-1-person10" type="surname" value="Tilley"/>
>                          <interp inst="f17280717-1-person10" type="given" value="Samuel"/>
>                          <interp inst="f17280717-1-person10" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person11" type="jurorName">
>                          Robert
>                          Lathwell
>                       <interp inst="f17280717-1-person11" type="surname" value="Lathwell"/>
>                          <interp inst="f17280717-1-person11" type="given" value="Robert"/>
>                          <interp inst="f17280717-1-person11" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person12" type="jurorName">
>                          Edward
>                          Newman
>                       <interp inst="f17280717-1-person12" type="surname" value="Newman"/>
>                          <interp inst="f17280717-1-person12" type="given" value="Edward"/>
>                          <interp inst="f17280717-1-person12" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person13" type="jurorName">
>                          Nathaniel
>                          Pickering
>                       <interp inst="f17280717-1-person13" type="surname" value="Pickering"/>
>                          <interp inst="f17280717-1-person13" type="given" value="Nathaniel"/>
>                          <interp inst="f17280717-1-person13" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person14" type="jurorName">
>                          Simon
>                          Tunks
>                       <interp inst="f17280717-1-person14" type="surname" value="Tunks"/>
>                          <interp inst="f17280717-1-person14" type="given" value="Simon"/>
>                          <interp inst="f17280717-1-person14" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person15" type="jurorName">
>                          Thomas
>                          Maud
>                       <interp inst="f17280717-1-person15" type="surname" value="Maud"/>
>                          <interp inst="f17280717-1-person15" type="given" value="Thomas"/>
>                          <interp inst="f17280717-1-person15" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person16" type="jurorName">
>                          John
>                          Bond
>                       <interp inst="f17280717-1-person16" type="surname" value="Bond"/>
>                          <interp inst="f17280717-1-person16" type="given" value="John"/>
>                          <interp inst="f17280717-1-person16" type="gender" value="male"/>
>                       </persName>
>                    .</p>
>
>
>                    <p>Middlesex Jury.</p>
>                    <p>
>
>                       <persName id="f17280717-1-person17" type="jurorName">
>                          Elisha
>                          Impey
>                       <interp inst="f17280717-1-person17" type="surname" value="Impey"/>
>                          <interp inst="f17280717-1-person17" type="given" value="Elisha"/>
>                          <interp inst="f17280717-1-person17" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person18" type="jurorName">
>                          Christopher
>                          Harris
>                       <interp inst="f17280717-1-person18" type="surname" value="Harris"/>
>                          <interp inst="f17280717-1-person18" type="given" value="Christopher"/>
>                          <interp inst="f17280717-1-person18" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person19" type="jurorName">
>                          William
>                          Perkins
>                       <interp inst="f17280717-1-person19" type="surname" value="Perkins"/>
>                          <interp inst="f17280717-1-person19" type="given" value="William"/>
>                          <interp inst="f17280717-1-person19" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person20" type="jurorName">
>                          Gilbert
>                          Watson
>                       <interp inst="f17280717-1-person20" type="surname" value="Watson"/>
>                          <interp inst="f17280717-1-person20" type="given" value="Gilbert"/>
>                          <interp inst="f17280717-1-person20" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person21" type="jurorName">
>                          John
>                          Wells
>                       <interp inst="f17280717-1-person21" type="surname" value="Wells"/>
>                          <interp inst="f17280717-1-person21" type="given" value="John"/>
>                          <interp inst="f17280717-1-person21" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person22" type="jurorName">
>                          William
>                          Carpenter
>                       <interp inst="f17280717-1-person22" type="surname" value="Carpenter"/>
>                          <interp inst="f17280717-1-person22" type="given" value="William"/>
>                          <interp inst="f17280717-1-person22" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person23" type="jurorName">
>                          Allen
>                          Evans
>                       <interp inst="f17280717-1-person23" type="surname" value="Evans"/>
>                          <interp inst="f17280717-1-person23" type="given" value="Allen"/>
>                          <interp inst="f17280717-1-person23" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person24" type="jurorName">
>                          Henry
>                          Cowmbe
>                       <interp inst="f17280717-1-person24" type="surname" value="Cowmbe"/>
>                          <interp inst="f17280717-1-person24" type="given" value="Henry"/>
>                          <interp inst="f17280717-1-person24" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person25" type="jurorName">
>                          Simon
>                          Parsons
>                       <interp inst="f17280717-1-person25" type="surname" value="Parsons"/>
>                          <interp inst="f17280717-1-person25" type="given" value="Simon"/>
>                          <interp inst="f17280717-1-person25" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person26" type="jurorName">
>                          George
>                          Gilbert
>                       <interp inst="f17280717-1-person26" type="surname" value="Gilbert"/>
>                          <interp inst="f17280717-1-person26" type="given" value="George"/>
>                          <interp inst="f17280717-1-person26" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person27" type="jurorName">
>                          Nicholas
>                          Gardner
>                       <interp inst="f17280717-1-person27" type="surname" value="Gardner"/>
>                          <interp inst="f17280717-1-person27" type="given" value="Nicholas"/>
>                          <interp inst="f17280717-1-person27" type="gender" value="male"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName id="f17280717-1-person28" type="jurorName">
>                          Thomas
>                          Ireland
>                       <interp inst="f17280717-1-person28" type="surname" value="Ireland"/>
>                          <interp inst="f17280717-1-person28" type="given" value="Thomas"/>
>                          <interp inst="f17280717-1-person28" type="gender" value="male"/>
>                       </persName>
>                    .</p>
>
>                 </div1>
>
>
>                 <div1 type="trialAccount" id="t17280717-1">
>                    <interp inst="t17280717-1" type="collection" value="BAILEY"/>
>                    <interp inst="t17280717-1" type="year" value="1728"/>
>                    <interp inst="t17280717-1" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="t17280717-1" type="date" value="17280717"/>
>                    <join result="criminalCharge" id="t17280717-1-off2-c29" targOrder="Y"
>                          targets="t17280717-1-defend29 t17280717-1-off2 t17280717-1-verdict5"/>
>
>                    <p>
>
>                       <persName id="t17280717-1-defend29" type="defendantName">
>                       James
>                       Haddock
>                    <interp inst="t17280717-1-defend29" type="surname" value="Haddock"/>
>                          <interp inst="t17280717-1-defend29" type="given" value="James"/>
>                          <interp inst="t17280717-1-defend29" type="gender" value="male"/>
>                       </persName>
>                 , of <placeName id="t17280717-1-defloc1">St. Bennet's Paul's Wharf</placeName>
>                       <interp inst="t17280717-1-defloc1" type="placeName" value="St. Bennet's Paul's Wharf"/>
>                       <interp inst="t17280717-1-defloc1" type="type" value="defendantHome"/>
>                       <join result="persNamePlace" targOrder="Y"
>                             targets="t17280717-1-defend29 t17280717-1-defloc1"/>, was indicted for <rs id="t17280717-1-off2" type="offenceDescription">
>                          <interp inst="t17280717-1-off2" type="offenceCategory" value="theft"/>
>                          <interp inst="t17280717-1-off2" type="offenceSubcategory" value="theftFromPlace"/>
>                    feloniously stealing 2 Guineas, and 5 l. 16 s. in Silver, a Silver Cup, a Silver Cork Screw, 2 Silver Spoons, and a Nutmeg-grater, in the Dwelling-House of
>
>                             <persName id="t17280717-1-victim31" type="victimName">
>                                James
>                                Reeves
>                             <interp inst="t17280717-1-victim31" type="surname" value="Reeves"/>
>                             <interp inst="t17280717-1-victim31" type="given" value="James"/>
>                             <interp inst="t17280717-1-victim31" type="gender" value="male"/>
>                             <join result="offenceVictim" targOrder="Y"
>                                   targets="t17280717-1-off2 t17280717-1-victim31"/>
>                          </persName>
>
>
>
>                       </rs>, on the <rs id="t17280717-1-cd3" type="crimeDate">9th of April, and in the 13th Year of his late Majesty King George the First</rs>
>                       <join result="offenceCrimeDate" targOrder="Y"
>                             targets="t17280717-1-off2 t17280717-1-cd3"/>, the Property of
>                    <persName id="t17280717-1-person32">
>                       James
>                       Reeves
>                    <interp inst="t17280717-1-person32" type="surname" value="Reeves"/>
>                          <interp inst="t17280717-1-person32" type="given" value="James"/>
>                          <interp inst="t17280717-1-person32" type="gender" value="male"/>
>                       </persName>
>                  aforesaid.</p>
>                    <p>
>
>                       <persName id="t17280717-1-person33">
>                       Elizabeth
>                       Reeves
>                    <interp inst="t17280717-1-person33" type="surname" value="Reeves"/>
>                          <interp inst="t17280717-1-person33" type="given" value="Elizabeth"/>
>                          <interp inst="t17280717-1-person33" type="gender" value="female"/>
>                       </persName>
>                  depos'd, That the Prisoner was a Lodger at her House on <placeName id="t17280717-1-crimeloc4">Addle-Hill</placeName>
>                       <interp inst="t17280717-1-crimeloc4" type="placeName" value="Addle-Hill"/>
>                       <interp inst="t17280717-1-crimeloc4" type="type" value="crimeLocation"/>
>                       <join result="offencePlace" targOrder="Y"
>                             targets="t17280717-1-off2 t17280717-1-crimeloc4"/>, near Doctors Commons, when this Robbery was committed, and that it being on the Sabbath Day, she desired the Prisoner, if he did not go abroad, to have an Eye to her Room, which she locked up, and which, he promised to have an Eye to; but when she came home, the Chamber Door and the Corner-Cupboard had been forced open, which appeared by the Mark of an Instrument, the Hinges tore off and the Money gone, the Drawers rifled, and the Plate taken out, though some of the Drawers, out of which the Plate was taken, she left lock'd when she went from Home; upon which she cried out, saying, she was robb'd, and the Prisoner's Wife being above Stairs, came down, and said, her Husband was gone out, that he had been guilty of Failings, and desired her to be easy and she would Work early and late to make Satisfaction, though she did not know he had taken the Things, but his not coming home again confirmed them the more in this Suspicion; and there were other Witnesses, who depos'd, That the Prosecutor left the Prisoner in Care of her Door, and that he promised to look after it.</p>
>                    <p>The Prisoner said in his Defence, That Mrs. Reeves had lost a pair of Silver Buckles out of her Drawers a Fortnight before this Robbery, and she said, she did believe it was done by a Char-woman that she employ'd, which Mrs. Reeves acknowledging, and the Prisoner telling a plausible Story of his being in bad Circumstances, and that Day the Robbery was committed, one Diston pittying his Case, lent him Money to go down to Bristol, to Trade there, if possible to retrieve himself, and he not daring to go out of Doors in the Week Days, went out of the House unfortunately on that Afternoon: There being no positive Evidence against him, the Jury <rs id="t17280717-1-verdict5" type="verdictDescription">
>                          <interp inst="t17280717-1-verdict5" type="verdictCategory" value="notGuilty"/>
>                    acquitted
>                 </rs> him.</p>
>                 </div1>
>
>
>                 <div1 type="trialAccount" id="t17280717-2">
>                    <interp inst="t17280717-2" type="collection" value="BAILEY"/>
>                    <interp inst="t17280717-2" type="year" value="1728"/>
>                    <interp inst="t17280717-2" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="t17280717-2" type="date" value="17280717"/>
>                    <join result="criminalCharge" id="t17280717-2-off6-c33" targOrder="Y"
>                          targets="t17280717-2-defend35 t17280717-2-off6 t17280717-2-verdict7"/>
>
>                    <p>
>
>
>                       <persName id="t17280717-2-defend35" type="defendantName">
>                          David
>                          Ball
>                       <interp inst="t17280717-2-defend35" type="surname" value="Ball"/>
>                          <interp inst="t17280717-2-defend35" type="given" value="David"/>
>                          <interp inst="t17280717-2-defend35" type="gender" value="male"/>
>                       </persName>
>
>                  was indicted for <rs id="t17280717-2-off6" type="offenceDescription">
>                          <interp inst="t17280717-2-off6" type="offenceCategory" value="theft"/>
>                          <interp inst="t17280717-2-off6" type="offenceSubcategory" value="pettyLarceny"/>
>                    a Petty Larceny, in stealing a Yard and a Quarter of Linnen Cloth, value 11 d.
>                 </rs> the Goods of
>
>                       <persName id="t17280717-2-victim37" type="victimName">
>                          John
>                          Williams
>                       <interp inst="t17280717-2-victim37" type="surname" value="Williams"/>
>                          <interp inst="t17280717-2-victim37" type="given" value="John"/>
>                          <interp inst="t17280717-2-victim37" type="gender" value="male"/>
>                       </persName>
>
>                  and
>
>                       <persName id="t17280717-2-victim39" type="victimName">
>                          William
>                          Williams
>                       <interp inst="t17280717-2-victim39" type="surname" value="Williams"/>
>                          <interp inst="t17280717-2-victim39" type="given" value="William"/>
>                          <interp inst="t17280717-2-victim39" type="gender" value="male"/>
>                       </persName>
>
>                 ; to which Indictment he <rs id="t17280717-2-verdict7" type="verdictDescription">
>                          <interp inst="t17280717-2-verdict7" type="verdictCategory" value="guilty"/>
>                          <interp inst="t17280717-2-verdict7" type="verdictSubcategory" value="pleadedGuilty"/>
>                    pleaded Guilty
>                 </rs>.</p>
>                    <p>
>                       <rs id="t17280717-2-punish8" type="punishmentDescription">
>                          <interp inst="t17280717-2-punish8" type="punishmentCategory" value="transport"/>
>                          <join result="defendantPunishment" targOrder="Y"
>                                targets="t17280717-2-defend35 t17280717-2-punish8"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 type="trialAccount" id="t17280717-3">
>                    <interp inst="t17280717-3" type="collection" value="BAILEY"/>
>                    <interp inst="t17280717-3" type="year" value="1728"/>
>                    <interp inst="t17280717-3" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="t17280717-3" type="date" value="17280717"/>
>                    <join result="criminalCharge" id="t17280717-3-off10-c36" targOrder="Y"
>                          targets="t17280717-3-defend40 t17280717-3-off10 t17280717-3-verdict11"/>
>
>                    <p>
>                       <xptr type="pageFacsimile" doc="172807170002"/>
>
>                       <persName id="t17280717-3-defend40" type="defendantName">
>                       Nathaniel
>                       Mercy
>                    <interp inst="t17280717-3-defend40" type="surname" value="Mercy"/>
>                          <interp inst="t17280717-3-defend40" type="given" value="Nathaniel"/>
>                          <interp inst="t17280717-3-defend40" type="gender" value="male"/>
>                       </persName>
>                 , of <placeName id="t17280717-3-defloc9">St. James's Westminster</placeName>
>                       <interp inst="t17280717-3-defloc9" type="placeName" value="St. James's Westminster"/>
>                       <interp inst="t17280717-3-defloc9" type="type" value="defendantHome"/>
>                       <join result="persNamePlace" targOrder="Y"
>                             targets="t17280717-3-defend40 t17280717-3-defloc9"/>, was indicted for <rs id="t17280717-3-off10" type="offenceDescription">
>                          <interp inst="t17280717-3-off10" type="offenceCategory" value="theft"/>
>                          <interp inst="t17280717-3-off10" type="offenceSubcategory" value="grandLarceny"/>
>                    stealing a Coach Wheel, value 12 Shillings
>                 </rs>, the Goods of
>                    <persName id="t17280717-3-victim41" type="victimName">
>                       Thomas
>                       West
>                    <interp inst="t17280717-3-victim41" type="surname" value="West"/>
>                          <interp inst="t17280717-3-victim41" type="given" value="Thomas"/>
>                          <interp inst="t17280717-3-victim41" type="gender" value="male"/>
>                          <join result="offenceVictim" targOrder="Y"
>                                targets="t17280717-3-off10 t17280717-3-victim41"/>
>                       </persName>
>                  , on the 9th of this Instant, to which Indictment he <rs id="t17280717-3-verdict11" type="verdictDescription">
>                          <interp inst="t17280717-3-verdict11" type="verdictCategory" value="guilty"/>
>                          <interp inst="t17280717-3-verdict11" type="verdictSubcategory" value="pleadedGuilty"/>
>                    pleaded guilty
>                 </rs>.</p>
>                    <p>
>                       <rs id="t17280717-3-punish12" type="punishmentDescription">
>                          <interp inst="t17280717-3-punish12" type="punishmentCategory" value="transport"/>
>                          <join result="defendantPunishment" targOrder="Y"
>                                targets="t17280717-3-defend40 t17280717-3-punish12"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 type="trialAccount" id="t17280717-4">
>                    <interp inst="t17280717-4" type="collection" value="BAILEY"/>
>                    <interp inst="t17280717-4" type="year" value="1728"/>
>                    <interp inst="t17280717-4" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="t17280717-4" type="date" value="17280717"/>
>                    <join result="criminalCharge" id="t17280717-4-off14-c38" targOrder="Y"
>                          targets="t17280717-4-defend42 t17280717-4-off14 t17280717-4-verdict17"/>
>
>                    <p>
>
>                       <persName id="t17280717-4-defend42" type="defendantName">
>                       Margaret
>                       King
>                    <interp inst="t17280717-4-defend42" type="surname" value="King"/>
>                          <interp inst="t17280717-4-defend42" type="given" value="Margaret"/>
>                          <interp inst="t17280717-4-defend42" type="gender" value="female"/>
>                       </persName>
>                 , of <placeName id="t17280717-4-defloc13">St. Ann's Westminster</placeName>
>                       <interp inst="t17280717-4-defloc13" type="placeName" value="St. Ann's Westminster"/>
>                       <interp inst="t17280717-4-defloc13" type="type" value="defendantHome"/>
>                       <join result="persNamePlace" targOrder="Y"
>                             targets="t17280717-4-defend42 t17280717-4-defloc13"/>, was indicted for <rs id="t17280717-4-off14" type="offenceDescription">
>                          <interp inst="t17280717-4-off14" type="offenceCategory" value="theft"/>
>                          <interp inst="t17280717-4-off14" type="offenceSubcategory" value="grandLarceny"/>
>                    privately stealing a Gold Watch, value 16 l.
>                 </rs> on the <rs id="t17280717-4-cd15" type="crimeDate">first of May</rs>
>                       <join result="offenceCrimeDate" targOrder="Y"
>                             targets="t17280717-4-off14 t17280717-4-cd15"/> last, the Property of
>                    <persName id="t17280717-4-victim43" type="victimName">
>                       Timothy
>                       Conner
>                    <interp inst="t17280717-4-victim43" type="surname" value="Conner"/>
>                          <interp inst="t17280717-4-victim43" type="given" value="Timothy"/>
>                          <interp inst="t17280717-4-victim43" type="gender" value="male"/>
>                          <join result="offenceVictim" targOrder="Y"
>                                targets="t17280717-4-off14 t17280717-4-victim43"/>
>                       </persName>
>                  .</p>
>                    <p>The Prosecutor depos'd, That he met the Prisoner in the Street, and ask'd her to drink a Glass of Wine, to which she consented, and they went to the <placeName id="t17280717-4-crimeloc16">Swan Tavern in Newport Market</placeName>
>                       <interp inst="t17280717-4-crimeloc16" type="placeName"
>                               value="Swan Tavern in Newport Market"/>
>                       <interp inst="t17280717-4-crimeloc16" type="type" value="crimeLocation"/>
>                       <join result="offencePlace" targOrder="Y"
>                             targets="t17280717-4-off14 t17280717-4-crimeloc16"/>, and lovingly drank 4 Pints, the Prisoner asking him what it was a Clock, he pulled out his Gold Watch, and bid her look; that she took it in her Hand, but could not remember that she returned it again, neither could she say positively, that she had it, but as he mis'd it as soon as he parted with her he thought he had occasion of Suspicion; yet, said he, I had been drinking before, (tho' it was but Nine in the Morning)and can't tell directly how the Matter stood; which being all the Evidence he could give against her, she was <rs id="t17280717-4-verdict17" type="verdictDescription">
>                          <interp inst="t17280717-4-verdict17" type="verdictCategory" value="notGuilty"/>
>                    acquitted
>                 </rs>.</p>
>                 </div1>
>
>
>                 <div1 type="trialAccount" id="t17280717-5">
>                    <interp inst="t17280717-5" type="collection" value="BAILEY"/>
>                    <interp inst="t17280717-5" type="year" value="1728"/>
>                    <interp inst="t17280717-5" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="t17280717-5" type="date" value="17280717"/>
>                    <join result="criminalCharge" id="t17280717-5-off19-c40" targOrder="Y"
>                          targets="t17280717-5-defend44 t17280717-5-off19 t17280717-5-verdict22"/>
>
>                    <p>
>
>                       <persName id="t17280717-5-defend44" type="defendantName">
>                       Elizabeth
>                       Mould
>                    <interp inst="t17280717-5-defend44" type="surname" value="Mould"/>
>                          <interp inst="t17280717-5-defend44" type="given" value="Elizabeth"/>
>                          <interp inst="t17280717-5-defend44" type="gender" value="female"/>
>                       </persName>
>                 , of <placeName id="t17280717-5-defloc18">St. Martins in the Fields</placeName>
>                       <interp inst="t17280717-5-defloc18" type="placeName" value="St. Martins in the Fields"/>
>                       <interp inst="t17280717-5-defloc18" type="type" value="defendantHome"/>
>                       <join result="persNamePlace" targOrder="Y"
>                             targets="t17280717-5-defend44 t17280717-5-defloc18"/>, was indicted for <rs id="t17280717-5-off19" type="offenceDescription">
>                          <interp inst="t17280717-5-off19" type="offenceCategory" value="theft"/>
>                          <interp inst="t17280717-5-off19" type="offenceSubcategory" value="pocketpicking"/>
>                    privately stealing Fifty Pounds and eight Shillings, from the Person of
>                          <persName id="t17280717-5-victim45" type="victimName">
>                             John
>                             Coxall
>                          <interp inst="t17280717-5-victim45" type="surname" value="Coxall"/>
>                             <interp inst="t17280717-5-victim45" type="given" value="John"/>
>                             <interp inst="t17280717-5-victim45" type="gender" value="male"/>
>                             <join result="offenceVictim" targOrder="Y"
>                                   targets="t17280717-5-off19 t17280717-5-victim45"/>
>                          </persName>
>
>
>                       </rs>, on the <rs id="t17280717-5-cd20" type="crimeDate">24th of June</rs>
>                       <join result="offenceCrimeDate" targOrder="Y"
>                             targets="t17280717-5-off19 t17280717-5-cd20"/> last</p>
>                    <p>The Prosecutor depos'd, That he being a <rs id="t17280717-5-viclabel21" type="occupation">Bricklayer</rs>
>                       <join result="persNameOccupation" targOrder="Y"
>                             targets="t17280717-5-victim45 t17280717-5-viclabel21"/>, was endeavouring to get some Business to do, at the House where the Prisoner lived, and it being a Chandler's Shop, to obtain the Good-Will of the People, he call'd for several Drams, and treated the Mistress of the House and one Mrs. Greaves, who was his Customer, with Cyder, Brandy, &amp;c. that whilst they were drinking the Prisoner came into the Room, and he likewise treated her, and she, in return, wip'd him over the Face, and seem'd very fond of him, saying, she would give him a fine Nosegay, and something to cheer his Heart, if he would go with her to Covent-Garden Market, and then (as his Expression was) she weagled him down into the Cellar, and there kept him lock'd up in a back Passage, that he was very much in Liquor, and scarce sensible of what he did, but he found what she had done to his Sorrow, for she had taken all his Money, which he missing, made a Noise, and the People of the House hearing him, let him out of his Place of Confinement, by conducting him up the Back Stairs; that he got Officers and search'd the Cellar, but could not find the Prisoner. The Prisoner desiring he might be ask'd if they did not drink together in the Cellar, the Prosecutor answer'd, No, she would fetch no Drink, saying, she did not care to be seen by the Publicans; that she was all for dry Money, and she, and None but she, had his Fifty Pounds, for save only him and her, there were neither Man, Woman, nor Child, nor Dog nor Cat in the Cellar.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person46">
>                       Elizabeth
>                       Harper
>                    <interp inst="t17280717-5-person46" type="surname" value="Harper"/>
>                          <interp inst="t17280717-5-person46" type="given" value="Elizabeth"/>
>                          <interp inst="t17280717-5-person46" type="gender" value="female"/>
>                       </persName>
>                  depos'd, That the Prosecutor came into her Shop, and drank Cyder, Usquebaugh and Brandy, and being disguis'd in Liquor he pull'd out eight or ten Guineas and said, he was no Scoundrel; that she and Mrs. Greaves, whom he brought in to treat, begg'd he would put up his Money and take Care of it, and about that Time the Prisoner came into the Room, and familiarly stroaking his cheeks, persuaded him to go into the Cellar, saying, he should pay his Footing, that they staid half an Hour below, and this Deponent looking down, saw the Prisoner's Mother there, that he came up with her again, and treated her with Usquebaugh, and at 11 o' Clock, which was several Hours after, they heard him in a back Passage, where the House was supplied with Water, and letting him into their House, he said he was robb'd, at which this Deponent's Husband thrust him out of doors.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person47">
>                       Isabella
>                       Greaves
>                    <interp inst="t17280717-5-person47" type="surname" value="Greaves"/>
>                          <interp inst="t17280717-5-person47" type="given" value="Isabella"/>
>                          <interp inst="t17280717-5-person47" type="gender" value="female"/>
>                       </persName>
>                  likewise confirm'd every Part of this Deposition, adding, That the Prisoner was not to be seen that Night after the Robbery.</p>
>                    <p>The Prisoner said in her Defence, That the Prosecutor went down into her Cellar, and behaved himself so rudely, that she was forced to threaten to send for an Officer, that she knew nothing of his Money, and had not gone away that Night, but as she was oblig'd by her Husband, and that she came next Morning and set her Greens out.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person48">
>                       Anthony
>                       Dyer
>                    <interp inst="t17280717-5-person48" type="surname" value="Dyer"/>
>                          <interp inst="t17280717-5-person48" type="given" value="Anthony"/>
>                          <interp inst="t17280717-5-person48" type="gender" value="male"/>
>                       </persName>
>                  depos'd, That the Prosecutor told him he had lost his Money being Drunk, that he had been in a Cellar and in a Vault, where he fell asleep, and gave a very odd account of the Adventure.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person49">
>                       George
>                       Smith
>                    <interp inst="t17280717-5-person49" type="surname" value="Smith"/>
>                          <interp inst="t17280717-5-person49" type="given" value="George"/>
>                          <interp inst="t17280717-5-person49" type="gender" value="male"/>
>                       </persName>
>                  depos'd, That the Prosecutor said he fell asleep upon a Vault, and could charge no Body.</p>
>                    <p>The Constable, the Watchman, and others severally depos'd, That the Morning after this happened, he said he could charge no particular person, but he would indict the House. The Prisoner having a very good Character from several reputable Witnesses, the Jury <rs id="t17280717-5-verdict22" type="verdictDescription">
>                          <interp inst="t17280717-5-verdict22" type="verdictCategory" value="notGuilty"/>
>                    acquitted
>                 </rs> her.</p>
>                 </div1>
>
>
>                 <div1 type="trialAccount" id="t17280717-6">
>                    <interp inst="t17280717-6" type="collection" value="BAILEY"/>
>                    <interp inst="t17280717-6" type="year" value="1728"/>
>                    <interp inst="t17280717-6" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="t17280717-6" type="date" value="17280717"/>
>                    <join result="criminalCharge" id="t17280717-6-off24-c46" targOrder="Y"
>                          targets="t17280717-6-defend50 t17280717-6-off24 t17280717-6-verdict26"/>
>
>                    <p>
>
>                       <persName id="t17280717-6-defend50" type="defendantName">
>                       Phillip
>                       Hilliard
>                    <interp inst="t17280717-6-defend50" type="surname" value="Hilliard"/>
>                          <interp inst="t17280717-6-defend50" type="given" value="Phillip"/>
>                          <interp inst="t17280717-6-defend50" type="gender" value="male"/>
>                       </persName>
>                 , of <placeName id="t17280717-6-defloc23">St. James's Westminster</placeName>
>                       <interp inst="t17280717-6-defloc23" type="placeName" value="St. James's Westminster"/>
>                       <interp inst="t17280717-6-defloc23" type="type" value="defendantHome"/>
>                       <join result="persNamePlace" targOrder="Y"
>                             targets="t17280717-6-defend50 t17280717-6-defloc23"/>, was indicted for <rs id="t17280717-6-off24" type="offenceDescription">
>                          <interp inst="t17280717-6-off24" type="offenceCategory" value="theft"/>
>                          <interp inst="t17280717-6-off24" type="offenceSubcategory" value="grandLarceny"/>
>                    feloniously stealing a Silver Spoon, value eleven Shillings, the Property of
>                          <persName id="t17280717-6-victim51" type="victimName">
>                             Abraham
>                             Mannio
>                          <interp inst="t17280717-6-victim51" type="surname" value="Mannio"/>
>                             <interp inst="t17280717-6-victim51" type="given" value="Abraham"/>
>                             <interp inst="t17280717-6-victim51" type="gender" value="male"/>
>                             <join result="offenceVictim" targOrder="Y"
>                                   targets="t17280717-6-off24 t17280717-6-victim51"/>
>                          </persName>
>                        , on the <rs id="t17280717-6-cd25" type="crimeDate">27th of June</rs>
>                          <join result="offenceCrimeDate" targOrder="Y"
>                                targets="t17280717-6-off24 t17280717-6-cd25"/> last.
>                 </rs>
>                    </p>
>                    <p>Mr. Hayden depos'd, That the Prisoner brought the Spoon to him to pawn, and he suspecting it to be stolen, stopp'd him and carried him to the Round-House, where he confess'd he stole it at the Prosecutor's, he being invited there to Dinner with some Gentlemans Servants.</p>
>                    <p>
>
>                       <persName id="t17280717-6-person52">
>                       Robert
>                       Amey
>                    <interp inst="t17280717-6-person52" type="surname" value="Amey"/>
>                          <interp inst="t17280717-6-person52" type="given" value="Robert"/>
>                          <interp inst="t17280717-6-person52" type="gender" value="male"/>
>                       </persName>
>                  depos'd, That he attended the Gentlemens Servants at Dinner, and afterwards they miss'd a Spoon, that Mr. Haydon sent them word of the Spoon brought to him by the Prisoner, and he going to match it by the others, found it to be the same which they had lost, it being of the same Make and Mark: The Fact being thus plainly proved upon him, the Jury found him <rs id="t17280717-6-verdict26" type="verdictDescription">
>                          <interp inst="t17280717-6-verdict26" type="verdictCategory" value="guilty"/>
>                          <interp inst="t17280717-6-verdict26" type="verdictSubcategory" value="theftunder1s"/>
>                    guilty to the value of 10d.
>                 </rs>
>                    </p>
>                    <p>
>                       <rs id="t17280717-6-punish27" type="punishmentDescription">
>                          <interp inst="t17280717-6-punish27" type="punishmentCategory" value="transport"/>
>                          <join result="defendantPunishment" targOrder="Y"
>                                targets="t17280717-6-defend50 t17280717-6-punish27"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 type="trialAccount" id="t17280717-7">
>                    <interp inst="t17280717-7" type="collection" value="BAILEY"/>
>                    <interp inst="t17280717-7" type="year" value="1728"/>
>                    <interp inst="t17280717-7" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="t17280717-7" type="date" value="17280717"/>
>                    <join result="criminalCharge" id="t17280717-7-off29-c49" targOrder="Y"
>                          targets="t17280717-7-defend53 t17280717-7-off29 t17280717-7-verdict31"/>
>
>                    <p>
>
>                       <persName id="t17280717-7-defend53" type="defendantName">
>                       Robert
>                       Ashby
>                    <interp inst="t17280717-7-defend53" type="surname" value="Ashby"/>
>                          <interp inst="t17280717-7-defend53" type="given" value="Robert"/>
>                          <interp inst="t17280717-7-defend53" type="gender" value="male"/>
>                       </persName>
>                 , of <rs id="t17280717-7-deflabel28" type="occupation">St. Andrew's Holborn</rs>
>                       <join result="persNameOccupation" targOrder="Y"
>                             targets="t17280717-7-defend53 t17280717-7-deflabel28"/>, was indicted for <rs id="t17280717-7-off29" type="offenceDescription">
>                          <interp inst="t17280717-7-off29" type="offenceCategory" value="theft"/>
>                          <interp inst="t17280717-7-off29" type="offenceSubcategory" value="grandLarceny"/>
>                    stealing a Gold Watch, value 16 l. a Chain, value 4 l. a Cornelian Seal set in Gold, value 10s. the Property of
>                          <persName id="t17280717-7-victim54" type="victimName">
>                             Nicholas
>                             Roper
>                          <interp inst="t17280717-7-victim54" type="surname" value="Roper"/>
>                             <interp inst="t17280717-7-victim54" type="given" value="Nicholas"/>
>                             <interp inst="t17280717-7-victim54" type="gender" value="male"/>
>                          </persName>
>                         from the Person of
>                          <persName id="t17280717-7-victim55" type="victimName">
>                             Phoebe
>                             Thickpenny
>                          <interp inst="t17280717-7-victim55" type="surname" value="Thickpenny"/>
>                             <interp inst="t17280717-7-victim55" type="given" value="Phoebe"/>
>                             <interp inst="t17280717-7-victim55" type="gender" value="female"/>
>                          </persName>
>
>
>                       </rs>, who depos'd, That her Mistress being at Little Chelsea, sent her to their House in Lad-lane, for the Watch and other Things she had occasion for, that she call'd at the Prisoner's in Castle Yard, Chick-Lane, as she was going towards Chelsea with the Watch and the Bundle, where she drank part of a Pint of Beer, some Tea 2 Quarterns of Brandy, and a Bottle of Cyder; that the Prisoner would go part of the Way home with her, and in <placeName id="t17280717-7-crimeloc30">Leather-Lane</placeName>
>                       <interp inst="t17280717-7-crimeloc30" type="placeName" value="Leather-Lane"/>
>                       <interp inst="t17280717-7-crimeloc30" type="type" value="crimeLocation"/>
>                       <join result="offencePlace" targOrder="Y"
>                             targets="t17280717-7-off29 t17280717-7-crimeloc30"/>, he said, faith they would not part Dry lips, and accordingly they went into a publick House and drank a Pint of Twopenny, and two Quarterns of Brandy, that she had the Watch then, and at the Door the Prisoner kiss'd her, and gave her a shilling for a Coach, she having out-staid her Time; that when he kiss'd her, he put one Hand around her Waist, but what he did with the other she could not tell, that she then cross'd the Way to another House, and immediately miss'd the Watch, and she was sure she had pinn'd it so to her Side, that she could not drop it.</p>
>                    <p>
>
>                       <persName id="t17280717-7-person56">
>                       Margaret
>                       Nelson
>                    <interp inst="t17280717-7-person56" type="surname" value="Nelson"/>
>                          <interp inst="t17280717-7-person56" type="given" value="Margaret"/>
>                          <interp inst="t17280717-7-person56" type="gender" value="female"/>
>                       </persName>
>                  depos'd, That the Prisoner and Phoebe Thickpenny, came to her House in Leather-Lane, and he call'd for a private Room, to which the Girl would not consent, that the Girl wanted to go to, &amp;c. and she went with her, when the Girl said, she had her Mistress's Gold Watch, and seeming to look on it, told her it was 7 o' Clock, but this Deponent did not see the Watch, yet she said, she did verily believe she heard it beat, that they soon parted, and in a Quarter of an Hour the maid returned, and said she had lost the Watch.</p>
>                    <p>The Prisoner said in his Defence, That he knew nothing of it, any further than she said it was her Mistress's, that they parted very good Friends, and she having been an old Sweetheart of his, laid her Head upon his Shoulder, and said, She could live and die there, which would have been no little Aggravation to his Crime, had he wrong'd so good Natur'd a Creature: But the Evidence against him being weak, and several appearing to his Character, the Jury <rs id="t17280717-7-verdict31" type="verdictDescription">
>                          <interp inst="t17280717-7-verdict31" type="verdictCategory" value="notGuilty"/>
>                    acquitted
>                 </rs> him. </p>
>                 </div1>
>
>
>                 <div1 type="trialAccount" id="t17280717-8">
>                    <interp inst="t17280717-8" type="collection" value="BAILEY"/>
>                    <interp inst="t17280717-8" type="year" value="1728"/>
>                    <interp inst="t17280717-8" type="uri" value="sessionsPapers/17280717"/>
>                    <interp inst="t17280717-8" type="date" value="17280717"/>
>                    <join result="criminalCharge" id="t17280717-8-off33-c53" targOrder="Y"
>                          targets="t17280717-8-defend57 t17280717-8-off33 t17280717-8-verdict35"/>
>                    <join result="criminalCharge" id="t17280717-8-off33-c54" targOrder="Y"
>                          targets="t17280717-8-defend58 t17280717-8-off33 t17280717-8-verdict35"/>
>
>                    <p>
>
>                       <persName id="t17280717-8-defend57" type="def...

``` scala
val elem = scala.xml.XML.loadString(x)
```

>     elem: scala.xml.Elem =
>     <TEI.2>
>        <text>
>           <body>
>              <div0 id="17280717" type="sessionsPaper">
>                 <interp value="BAILEY" type="collection" inst="17280717"/>
>                 <interp value="1728" type="year" inst="17280717"/>
>                 <interp value="sessionsPapers/17280717" type="uri" inst="17280717"/>
>                 <interp value="17280717" type="date" inst="17280717"/>
>                 <xptr doc="17280717" type="transcription"/>
>
>                 <div1 id="f17280717-1" type="frontMatter">
>                    <interp value="BAILEY" type="collection" inst="f17280717-1"/>
>                    <interp value="1728" type="year" inst="f17280717-1"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="f17280717-1"/>
>                    <interp value="17280717" type="date" inst="f17280717-1"/>
>
>                    <xptr doc="172807170001" type="pageFacsimile"/>THE PROCEEDINGS AT THE Sessions of the Peace, and Oyer and Terminer for the City of LONDON: AND
>              <p>On the King's Commission of Goal-Delivery of Newgate, held at Justice-Hall in the Old Baily, for the CITY of LONDON and COUNTY of MIDDLESEX.</p>
>                    <p>On Wednesday, Thursday, and Friday, being the 17th, 18th, and 19th of July, 1728, in the Second Year of His MAJESTY's Reign.</p>
>                    <p>(Price Six Pence)</p>
>                    <p>BEFORE the Right Honourable Sir
>                       <persName type="judiciaryName" id="f17280717-1-person1">
>                          EDWARD
>                          BECHER
>                       <interp value="BECHER" type="surname" inst="f17280717-1-person1"/>
>                          <interp value="EDWARD" type="given" inst="f17280717-1-person1"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person1"/>
>                       </persName>
>                    , Lord Mayor of the City of London; the Right Honourable the Lord Chief
>                       <persName type="judiciaryName" id="f17280717-1-person2">
>                          Baron
>                          Pengelly
>                       <interp value="Pengelly" type="surname" inst="f17280717-1-person2"/>
>                          <interp value="Baron" type="given" inst="f17280717-1-person2"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person2"/>
>                       </persName>
>                    ; Mr. Justice Reynolds; Mr.
>                       <persName type="judiciaryName" id="f17280717-1-person3">
>                          Baron
>                          Thompson
>                       <interp value="Thompson" type="surname" inst="f17280717-1-person3"/>
>                          <interp value="Baron" type="given" inst="f17280717-1-person3"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person3"/>
>                       </persName>
>                    , Recorder of the City of London; and
>                       <persName type="judiciaryName" id="f17280717-1-person4">
>                          John
>                          Raby
>                       <interp value="Raby" type="surname" inst="f17280717-1-person4"/>
>                          <interp value="John" type="given" inst="f17280717-1-person4"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person4"/>
>                       </persName>
>                    , Esq; Serjeant at Law; and other His Majesty's Justices of Goal-Delivery, and Oyer and Terminer aforesaid; Together with several of His Majesty's Justices of the Peace for the said City of London.</p>
>
>                    <p>London Jury.</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person5">
>                          John
>                          Land
>                       <interp value="Land" type="surname" inst="f17280717-1-person5"/>
>                          <interp value="John" type="given" inst="f17280717-1-person5"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person5"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person6">
>                          Nathaniel
>                          Mason
>                       <interp value="Mason" type="surname" inst="f17280717-1-person6"/>
>                          <interp value="Nathaniel" type="given" inst="f17280717-1-person6"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person6"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person7">
>                          Benjamin
>                          Allibone
>                       <interp value="Allibone" type="surname" inst="f17280717-1-person7"/>
>                          <interp value="Benjamin" type="given" inst="f17280717-1-person7"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person7"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person8">
>                          Joseph
>                          Westwood
>                       <interp value="Westwood" type="surname" inst="f17280717-1-person8"/>
>                          <interp value="Joseph" type="given" inst="f17280717-1-person8"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person8"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person9">
>                          Gabriel
>                          Wittacre
>                       <interp value="Wittacre" type="surname" inst="f17280717-1-person9"/>
>                          <interp value="Gabriel" type="given" inst="f17280717-1-person9"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person9"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person10">
>                          Samuel
>                          Tilley
>                       <interp value="Tilley" type="surname" inst="f17280717-1-person10"/>
>                          <interp value="Samuel" type="given" inst="f17280717-1-person10"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person10"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person11">
>                          Robert
>                          Lathwell
>                       <interp value="Lathwell" type="surname" inst="f17280717-1-person11"/>
>                          <interp value="Robert" type="given" inst="f17280717-1-person11"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person11"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person12">
>                          Edward
>                          Newman
>                       <interp value="Newman" type="surname" inst="f17280717-1-person12"/>
>                          <interp value="Edward" type="given" inst="f17280717-1-person12"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person12"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person13">
>                          Nathaniel
>                          Pickering
>                       <interp value="Pickering" type="surname" inst="f17280717-1-person13"/>
>                          <interp value="Nathaniel" type="given" inst="f17280717-1-person13"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person13"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person14">
>                          Simon
>                          Tunks
>                       <interp value="Tunks" type="surname" inst="f17280717-1-person14"/>
>                          <interp value="Simon" type="given" inst="f17280717-1-person14"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person14"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person15">
>                          Thomas
>                          Maud
>                       <interp value="Maud" type="surname" inst="f17280717-1-person15"/>
>                          <interp value="Thomas" type="given" inst="f17280717-1-person15"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person15"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person16">
>                          John
>                          Bond
>                       <interp value="Bond" type="surname" inst="f17280717-1-person16"/>
>                          <interp value="John" type="given" inst="f17280717-1-person16"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person16"/>
>                       </persName>
>                    .</p>
>
>
>                    <p>Middlesex Jury.</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person17">
>                          Elisha
>                          Impey
>                       <interp value="Impey" type="surname" inst="f17280717-1-person17"/>
>                          <interp value="Elisha" type="given" inst="f17280717-1-person17"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person17"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person18">
>                          Christopher
>                          Harris
>                       <interp value="Harris" type="surname" inst="f17280717-1-person18"/>
>                          <interp value="Christopher" type="given" inst="f17280717-1-person18"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person18"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person19">
>                          William
>                          Perkins
>                       <interp value="Perkins" type="surname" inst="f17280717-1-person19"/>
>                          <interp value="William" type="given" inst="f17280717-1-person19"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person19"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person20">
>                          Gilbert
>                          Watson
>                       <interp value="Watson" type="surname" inst="f17280717-1-person20"/>
>                          <interp value="Gilbert" type="given" inst="f17280717-1-person20"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person20"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person21">
>                          John
>                          Wells
>                       <interp value="Wells" type="surname" inst="f17280717-1-person21"/>
>                          <interp value="John" type="given" inst="f17280717-1-person21"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person21"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person22">
>                          William
>                          Carpenter
>                       <interp value="Carpenter" type="surname" inst="f17280717-1-person22"/>
>                          <interp value="William" type="given" inst="f17280717-1-person22"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person22"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person23">
>                          Allen
>                          Evans
>                       <interp value="Evans" type="surname" inst="f17280717-1-person23"/>
>                          <interp value="Allen" type="given" inst="f17280717-1-person23"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person23"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person24">
>                          Henry
>                          Cowmbe
>                       <interp value="Cowmbe" type="surname" inst="f17280717-1-person24"/>
>                          <interp value="Henry" type="given" inst="f17280717-1-person24"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person24"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person25">
>                          Simon
>                          Parsons
>                       <interp value="Parsons" type="surname" inst="f17280717-1-person25"/>
>                          <interp value="Simon" type="given" inst="f17280717-1-person25"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person25"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person26">
>                          George
>                          Gilbert
>                       <interp value="Gilbert" type="surname" inst="f17280717-1-person26"/>
>                          <interp value="George" type="given" inst="f17280717-1-person26"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person26"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person27">
>                          Nicholas
>                          Gardner
>                       <interp value="Gardner" type="surname" inst="f17280717-1-person27"/>
>                          <interp value="Nicholas" type="given" inst="f17280717-1-person27"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person27"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person28">
>                          Thomas
>                          Ireland
>                       <interp value="Ireland" type="surname" inst="f17280717-1-person28"/>
>                          <interp value="Thomas" type="given" inst="f17280717-1-person28"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person28"/>
>                       </persName>
>                    .</p>
>
>                 </div1>
>
>
>                 <div1 id="t17280717-1" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-1"/>
>                    <interp value="1728" type="year" inst="t17280717-1"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-1"/>
>                    <interp value="17280717" type="date" inst="t17280717-1"/>
>                    <join targets="t17280717-1-defend29 t17280717-1-off2 t17280717-1-verdict5" targOrder="Y" id="t17280717-1-off2-c29" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-1-defend29">
>                       James
>                       Haddock
>                    <interp value="Haddock" type="surname" inst="t17280717-1-defend29"/>
>                          <interp value="James" type="given" inst="t17280717-1-defend29"/>
>                          <interp value="male" type="gender" inst="t17280717-1-defend29"/>
>                       </persName>
>                 , of <placeName id="t17280717-1-defloc1">St. Bennet's Paul's Wharf</placeName>
>                       <interp value="St. Bennet's Paul's Wharf" type="placeName" inst="t17280717-1-defloc1"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-1-defloc1"/>
>                       <join targets="t17280717-1-defend29 t17280717-1-defloc1" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-1-off2">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-1-off2"/>
>                          <interp value="theftFromPlace" type="offenceSubcategory" inst="t17280717-1-off2"/>
>                    feloniously stealing 2 Guineas, and 5 l. 16 s. in Silver, a Silver Cup, a Silver Cork Screw, 2 Silver Spoons, and a Nutmeg-grater, in the Dwelling-House of
>
>                             <persName type="victimName" id="t17280717-1-victim31">
>                                James
>                                Reeves
>                             <interp value="Reeves" type="surname" inst="t17280717-1-victim31"/>
>                             <interp value="James" type="given" inst="t17280717-1-victim31"/>
>                             <interp value="male" type="gender" inst="t17280717-1-victim31"/>
>                             <join targets="t17280717-1-off2 t17280717-1-victim31" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>
>
>
>                       </rs>, on the <rs type="crimeDate" id="t17280717-1-cd3">9th of April, and in the 13th Year of his late Majesty King George the First</rs>
>                       <join targets="t17280717-1-off2 t17280717-1-cd3" targOrder="Y" result="offenceCrimeDate"/>, the Property of
>                    <persName id="t17280717-1-person32">
>                       James
>                       Reeves
>                    <interp value="Reeves" type="surname" inst="t17280717-1-person32"/>
>                          <interp value="James" type="given" inst="t17280717-1-person32"/>
>                          <interp value="male" type="gender" inst="t17280717-1-person32"/>
>                       </persName>
>                  aforesaid.</p>
>                    <p>
>
>                       <persName id="t17280717-1-person33">
>                       Elizabeth
>                       Reeves
>                    <interp value="Reeves" type="surname" inst="t17280717-1-person33"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-1-person33"/>
>                          <interp value="female" type="gender" inst="t17280717-1-person33"/>
>                       </persName>
>                  depos'd, That the Prisoner was a Lodger at her House on <placeName id="t17280717-1-crimeloc4">Addle-Hill</placeName>
>                       <interp value="Addle-Hill" type="placeName" inst="t17280717-1-crimeloc4"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-1-crimeloc4"/>
>                       <join targets="t17280717-1-off2 t17280717-1-crimeloc4" targOrder="Y" result="offencePlace"/>, near Doctors Commons, when this Robbery was committed, and that it being on the Sabbath Day, she desired the Prisoner, if he did not go abroad, to have an Eye to her Room, which she locked up, and which, he promised to have an Eye to; but when she came home, the Chamber Door and the Corner-Cupboard had been forced open, which appeared by the Mark of an Instrument, the Hinges tore off and the Money gone, the Drawers rifled, and the Plate taken out, though some of the Drawers, out of which the Plate was taken, she left lock'd when she went from Home; upon which she cried out, saying, she was robb'd, and the Prisoner's Wife being above Stairs, came down, and said, her Husband was gone out, that he had been guilty of Failings, and desired her to be easy and she would Work early and late to make Satisfaction, though she did not know he had taken the Things, but his not coming home again confirmed them the more in this Suspicion; and there were other Witnesses, who depos'd, That the Prosecutor left the Prisoner in Care of her Door, and that he promised to look after it.</p>
>                    <p>The Prisoner said in his Defence, That Mrs. Reeves had lost a pair of Silver Buckles out of her Drawers a Fortnight before this Robbery, and she said, she did believe it was done by a Char-woman that she employ'd, which Mrs. Reeves acknowledging, and the Prisoner telling a plausible Story of his being in bad Circumstances, and that Day the Robbery was committed, one Diston pittying his Case, lent him Money to go down to Bristol, to Trade there, if possible to retrieve himself, and he not daring to go out of Doors in the Week Days, went out of the House unfortunately on that Afternoon: There being no positive Evidence against him, the Jury <rs type="verdictDescription" id="t17280717-1-verdict5">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-1-verdict5"/>
>                    acquitted
>                 </rs> him.</p>
>                 </div1>
>
>
>                 <div1 id="t17280717-2" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-2"/>
>                    <interp value="1728" type="year" inst="t17280717-2"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-2"/>
>                    <interp value="17280717" type="date" inst="t17280717-2"/>
>                    <join targets="t17280717-2-defend35 t17280717-2-off6 t17280717-2-verdict7" targOrder="Y" id="t17280717-2-off6-c33" result="criminalCharge"/>
>
>                    <p>
>
>
>                       <persName type="defendantName" id="t17280717-2-defend35">
>                          David
>                          Ball
>                       <interp value="Ball" type="surname" inst="t17280717-2-defend35"/>
>                          <interp value="David" type="given" inst="t17280717-2-defend35"/>
>                          <interp value="male" type="gender" inst="t17280717-2-defend35"/>
>                       </persName>
>
>                  was indicted for <rs type="offenceDescription" id="t17280717-2-off6">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-2-off6"/>
>                          <interp value="pettyLarceny" type="offenceSubcategory" inst="t17280717-2-off6"/>
>                    a Petty Larceny, in stealing a Yard and a Quarter of Linnen Cloth, value 11 d.
>                 </rs> the Goods of
>
>                       <persName type="victimName" id="t17280717-2-victim37">
>                          John
>                          Williams
>                       <interp value="Williams" type="surname" inst="t17280717-2-victim37"/>
>                          <interp value="John" type="given" inst="t17280717-2-victim37"/>
>                          <interp value="male" type="gender" inst="t17280717-2-victim37"/>
>                       </persName>
>
>                  and
>
>                       <persName type="victimName" id="t17280717-2-victim39">
>                          William
>                          Williams
>                       <interp value="Williams" type="surname" inst="t17280717-2-victim39"/>
>                          <interp value="William" type="given" inst="t17280717-2-victim39"/>
>                          <interp value="male" type="gender" inst="t17280717-2-victim39"/>
>                       </persName>
>
>                 ; to which Indictment he <rs type="verdictDescription" id="t17280717-2-verdict7">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-2-verdict7"/>
>                          <interp value="pleadedGuilty" type="verdictSubcategory" inst="t17280717-2-verdict7"/>
>                    pleaded Guilty
>                 </rs>.</p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-2-punish8">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-2-punish8"/>
>                          <join targets="t17280717-2-defend35 t17280717-2-punish8" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 id="t17280717-3" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-3"/>
>                    <interp value="1728" type="year" inst="t17280717-3"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-3"/>
>                    <interp value="17280717" type="date" inst="t17280717-3"/>
>                    <join targets="t17280717-3-defend40 t17280717-3-off10 t17280717-3-verdict11" targOrder="Y" id="t17280717-3-off10-c36" result="criminalCharge"/>
>
>                    <p>
>                       <xptr doc="172807170002" type="pageFacsimile"/>
>
>                       <persName type="defendantName" id="t17280717-3-defend40">
>                       Nathaniel
>                       Mercy
>                    <interp value="Mercy" type="surname" inst="t17280717-3-defend40"/>
>                          <interp value="Nathaniel" type="given" inst="t17280717-3-defend40"/>
>                          <interp value="male" type="gender" inst="t17280717-3-defend40"/>
>                       </persName>
>                 , of <placeName id="t17280717-3-defloc9">St. James's Westminster</placeName>
>                       <interp value="St. James's Westminster" type="placeName" inst="t17280717-3-defloc9"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-3-defloc9"/>
>                       <join targets="t17280717-3-defend40 t17280717-3-defloc9" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-3-off10">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-3-off10"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-3-off10"/>
>                    stealing a Coach Wheel, value 12 Shillings
>                 </rs>, the Goods of
>                    <persName type="victimName" id="t17280717-3-victim41">
>                       Thomas
>                       West
>                    <interp value="West" type="surname" inst="t17280717-3-victim41"/>
>                          <interp value="Thomas" type="given" inst="t17280717-3-victim41"/>
>                          <interp value="male" type="gender" inst="t17280717-3-victim41"/>
>                          <join targets="t17280717-3-off10 t17280717-3-victim41" targOrder="Y" result="offenceVictim"/>
>                       </persName>
>                  , on the 9th of this Instant, to which Indictment he <rs type="verdictDescription" id="t17280717-3-verdict11">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-3-verdict11"/>
>                          <interp value="pleadedGuilty" type="verdictSubcategory" inst="t17280717-3-verdict11"/>
>                    pleaded guilty
>                 </rs>.</p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-3-punish12">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-3-punish12"/>
>                          <join targets="t17280717-3-defend40 t17280717-3-punish12" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 id="t17280717-4" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-4"/>
>                    <interp value="1728" type="year" inst="t17280717-4"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-4"/>
>                    <interp value="17280717" type="date" inst="t17280717-4"/>
>                    <join targets="t17280717-4-defend42 t17280717-4-off14 t17280717-4-verdict17" targOrder="Y" id="t17280717-4-off14-c38" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-4-defend42">
>                       Margaret
>                       King
>                    <interp value="King" type="surname" inst="t17280717-4-defend42"/>
>                          <interp value="Margaret" type="given" inst="t17280717-4-defend42"/>
>                          <interp value="female" type="gender" inst="t17280717-4-defend42"/>
>                       </persName>
>                 , of <placeName id="t17280717-4-defloc13">St. Ann's Westminster</placeName>
>                       <interp value="St. Ann's Westminster" type="placeName" inst="t17280717-4-defloc13"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-4-defloc13"/>
>                       <join targets="t17280717-4-defend42 t17280717-4-defloc13" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-4-off14">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-4-off14"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-4-off14"/>
>                    privately stealing a Gold Watch, value 16 l.
>                 </rs> on the <rs type="crimeDate" id="t17280717-4-cd15">first of May</rs>
>                       <join targets="t17280717-4-off14 t17280717-4-cd15" targOrder="Y" result="offenceCrimeDate"/> last, the Property of
>                    <persName type="victimName" id="t17280717-4-victim43">
>                       Timothy
>                       Conner
>                    <interp value="Conner" type="surname" inst="t17280717-4-victim43"/>
>                          <interp value="Timothy" type="given" inst="t17280717-4-victim43"/>
>                          <interp value="male" type="gender" inst="t17280717-4-victim43"/>
>                          <join targets="t17280717-4-off14 t17280717-4-victim43" targOrder="Y" result="offenceVictim"/>
>                       </persName>
>                  .</p>
>                    <p>The Prosecutor depos'd, That he met the Prisoner in the Street, and ask'd her to drink a Glass of Wine, to which she consented, and they went to the <placeName id="t17280717-4-crimeloc16">Swan Tavern in Newport Market</placeName>
>                       <interp value="Swan Tavern in Newport Market" type="placeName" inst="t17280717-4-crimeloc16"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-4-crimeloc16"/>
>                       <join targets="t17280717-4-off14 t17280717-4-crimeloc16" targOrder="Y" result="offencePlace"/>, and lovingly drank 4 Pints, the Prisoner asking him what it was a Clock, he pulled out his Gold Watch, and bid her look; that she took it in her Hand, but could not remember that she returned it again, neither could she say positively, that she had it, but as he mis'd it as soon as he parted with her he thought he had occasion of Suspicion; yet, said he, I had been drinking before, (tho' it was but Nine in the Morning)and can't tell directly how the Matter stood; which being all the Evidence he could give against her, she was <rs type="verdictDescription" id="t17280717-4-verdict17">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-4-verdict17"/>
>                    acquitted
>                 </rs>.</p>
>                 </div1>
>
>
>                 <div1 id="t17280717-5" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-5"/>
>                    <interp value="1728" type="year" inst="t17280717-5"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-5"/>
>                    <interp value="17280717" type="date" inst="t17280717-5"/>
>                    <join targets="t17280717-5-defend44 t17280717-5-off19 t17280717-5-verdict22" targOrder="Y" id="t17280717-5-off19-c40" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-5-defend44">
>                       Elizabeth
>                       Mould
>                    <interp value="Mould" type="surname" inst="t17280717-5-defend44"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-5-defend44"/>
>                          <interp value="female" type="gender" inst="t17280717-5-defend44"/>
>                       </persName>
>                 , of <placeName id="t17280717-5-defloc18">St. Martins in the Fields</placeName>
>                       <interp value="St. Martins in the Fields" type="placeName" inst="t17280717-5-defloc18"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-5-defloc18"/>
>                       <join targets="t17280717-5-defend44 t17280717-5-defloc18" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-5-off19">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-5-off19"/>
>                          <interp value="pocketpicking" type="offenceSubcategory" inst="t17280717-5-off19"/>
>                    privately stealing Fifty Pounds and eight Shillings, from the Person of
>                          <persName type="victimName" id="t17280717-5-victim45">
>                             John
>                             Coxall
>                          <interp value="Coxall" type="surname" inst="t17280717-5-victim45"/>
>                             <interp value="John" type="given" inst="t17280717-5-victim45"/>
>                             <interp value="male" type="gender" inst="t17280717-5-victim45"/>
>                             <join targets="t17280717-5-off19 t17280717-5-victim45" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>
>
>                       </rs>, on the <rs type="crimeDate" id="t17280717-5-cd20">24th of June</rs>
>                       <join targets="t17280717-5-off19 t17280717-5-cd20" targOrder="Y" result="offenceCrimeDate"/> last</p>
>                    <p>The Prosecutor depos'd, That he being a <rs type="occupation" id="t17280717-5-viclabel21">Bricklayer</rs>
>                       <join targets="t17280717-5-victim45 t17280717-5-viclabel21" targOrder="Y" result="persNameOccupation"/>, was endeavouring to get some Business to do, at the House where the Prisoner lived, and it being a Chandler's Shop, to obtain the Good-Will of the People, he call'd for several Drams, and treated the Mistress of the House and one Mrs. Greaves, who was his Customer, with Cyder, Brandy, &amp;c. that whilst they were drinking the Prisoner came into the Room, and he likewise treated her, and she, in return, wip'd him over the Face, and seem'd very fond of him, saying, she would give him a fine Nosegay, and something to cheer his Heart, if he would go with her to Covent-Garden Market, and then (as his Expression was) she weagled him down into the Cellar, and there kept him lock'd up in a back Passage, that he was very much in Liquor, and scarce sensible of what he did, but he found what she had done to his Sorrow, for she had taken all his Money, which he missing, made a Noise, and the People of the House hearing him, let him out of his Place of Confinement, by conducting him up the Back Stairs; that he got Officers and search'd the Cellar, but could not find the Prisoner. The Prisoner desiring he might be ask'd if they did not drink together in the Cellar, the Prosecutor answer'd, No, she would fetch no Drink, saying, she did not care to be seen by the Publicans; that she was all for dry Money, and she, and None but she, had his Fifty Pounds, for save only him and her, there were neither Man, Woman, nor Child, nor Dog nor Cat in the Cellar.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person46">
>                       Elizabeth
>                       Harper
>                    <interp value="Harper" type="surname" inst="t17280717-5-person46"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-5-person46"/>
>                          <interp value="female" type="gender" inst="t17280717-5-person46"/>
>                       </persName>
>                  depos'd, That the Prosecutor came into her Shop, and drank Cyder, Usquebaugh and Brandy, and being disguis'd in Liquor he pull'd out eight or ten Guineas and said, he was no Scoundrel; that she and Mrs. Greaves, whom he brought in to treat, begg'd he would put up his Money and take Care of it, and about that Time the Prisoner came into the Room, and familiarly stroaking his cheeks, persuaded him to go into the Cellar, saying, he should pay his Footing, that they staid half an Hour below, and this Deponent looking down, saw the Prisoner's Mother there, that he came up with her again, and treated her with Usquebaugh, and at 11 o' Clock, which was several Hours after, they heard him in a back Passage, where the House was supplied with Water, and letting him into their House, he said he was robb'd, at which this Deponent's Husband thrust him out of doors.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person47">
>                       Isabella
>                       Greaves
>                    <interp value="Greaves" type="surname" inst="t17280717-5-person47"/>
>                          <interp value="Isabella" type="given" inst="t17280717-5-person47"/>
>                          <interp value="female" type="gender" inst="t17280717-5-person47"/>
>                       </persName>
>                  likewise confirm'd every Part of this Deposition, adding, That the Prisoner was not to be seen that Night after the Robbery.</p>
>                    <p>The Prisoner said in her Defence, That the Prosecutor went down into her Cellar, and behaved himself so rudely, that she was forced to threaten to send for an Officer, that she knew nothing of his Money, and had not gone away that Night, but as she was oblig'd by her Husband, and that she came next Morning and set her Greens out.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person48">
>                       Anthony
>                       Dyer
>                    <interp value="Dyer" type="surname" inst="t17280717-5-person48"/>
>                          <interp value="Anthony" type="given" inst="t17280717-5-person48"/>
>                          <interp value="male" type="gender" inst="t17280717-5-person48"/>
>                       </persName>
>                  depos'd, That the Prosecutor told him he had lost his Money being Drunk, that he had been in a Cellar and in a Vault, where he fell asleep, and gave a very odd account of the Adventure.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person49">
>                       George
>                       Smith
>                    <interp value="Smith" type="surname" inst="t17280717-5-person49"/>
>                          <interp value="George" type="given" inst="t17280717-5-person49"/>
>                          <interp value="male" type="gender" inst="t17280717-5-person49"/>
>                       </persName>
>                  depos'd, That the Prosecutor said he fell asleep upon a Vault, and could charge no Body.</p>
>                    <p>The Constable, the Watchman, and others severally depos'd, That the Morning after this happened, he said he could charge no particular person, but he would indict the House. The Prisoner having a very good Character from several reputable Witnesses, the Jury <rs type="verdictDescription" id="t17280717-5-verdict22">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-5-verdict22"/>
>                    acquitted
>                 </rs> her.</p>
>                 </div1>
>
>
>                 <div1 id="t17280717-6" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-6"/>
>                    <interp value="1728" type="year" inst="t17280717-6"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-6"/>
>                    <interp value="17280717" type="date" inst="t17280717-6"/>
>                    <join targets="t17280717-6-defend50 t17280717-6-off24 t17280717-6-verdict26" targOrder="Y" id="t17280717-6-off24-c46" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-6-defend50">
>                       Phillip
>                       Hilliard
>                    <interp value="Hilliard" type="surname" inst="t17280717-6-defend50"/>
>                          <interp value="Phillip" type="given" inst="t17280717-6-defend50"/>
>                          <interp value="male" type="gender" inst="t17280717-6-defend50"/>
>                       </persName>
>                 , of <placeName id="t17280717-6-defloc23">St. James's Westminster</placeName>
>                       <interp value="St. James's Westminster" type="placeName" inst="t17280717-6-defloc23"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-6-defloc23"/>
>                       <join targets="t17280717-6-defend50 t17280717-6-defloc23" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-6-off24">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-6-off24"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-6-off24"/>
>                    feloniously stealing a Silver Spoon, value eleven Shillings, the Property of
>                          <persName type="victimName" id="t17280717-6-victim51">
>                             Abraham
>                             Mannio
>                          <interp value="Mannio" type="surname" inst="t17280717-6-victim51"/>
>                             <interp value="Abraham" type="given" inst="t17280717-6-victim51"/>
>                             <interp value="male" type="gender" inst="t17280717-6-victim51"/>
>                             <join targets="t17280717-6-off24 t17280717-6-victim51" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>                        , on the <rs type="crimeDate" id="t17280717-6-cd25">27th of June</rs>
>                          <join targets="t17280717-6-off24 t17280717-6-cd25" targOrder="Y" result="offenceCrimeDate"/> last.
>                 </rs>
>                    </p>
>                    <p>Mr. Hayden depos'd, That the Prisoner brought the Spoon to him to pawn, and he suspecting it to be stolen, stopp'd him and carried him to the Round-House, where he confess'd he stole it at the Prosecutor's, he being invited there to Dinner with some Gentlemans Servants.</p>
>                    <p>
>
>                       <persName id="t17280717-6-person52">
>                       Robert
>                       Amey
>                    <interp value="Amey" type="surname" inst="t17280717-6-person52"/>
>                          <interp value="Robert" type="given" inst="t17280717-6-person52"/>
>                          <interp value="male" type="gender" inst="t17280717-6-person52"/>
>                       </persName>
>                  depos'd, That he attended the Gentlemens Servants at Dinner, and afterwards they miss'd a Spoon, that Mr. Haydon sent them word of the Spoon brought to him by the Prisoner, and he going to match it by the others, found it to be the same which they had lost, it being of the same Make and Mark: The Fact being thus plainly proved upon him, the Jury found him <rs type="verdictDescription" id="t17280717-6-verdict26">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-6-verdict26"/>
>                          <interp value="theftunder1s" type="verdictSubcategory" inst="t17280717-6-verdict26"/>
>                    guilty to the value of 10d.
>                 </rs>
>                    </p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-6-punish27">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-6-punish27"/>
>                          <join targets="t17280717-6-defend50 t17280717-6-punish27" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 id="t17280717-7" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-7"/>
>                    <interp value="1728" type="year" inst="t17280717-7"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-7"/>
>                    <interp value="17280717" type="date" inst="t17280717-7"/>
>                    <join targets="t17280717-7-defend53 t17280717-7-off29 t17280717-7-verdict31" targOrder="Y" id="t17280717-7-off29-c49" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-7-defend53">
>                       Robert
>                       Ashby
>                    <interp value="Ashby" type="surname" inst="t17280717-7-defend53"/>
>                          <interp value="Robert" type="given" inst="t17280717-7-defend53"/>
>                          <interp value="male" type="gender" inst="t17280717-7-defend53"/>
>                       </persName>
>                 , of <rs type="occupation" id="t17280717-7-deflabel28">St. Andrew's Holborn</rs>
>                       <join targets="t17280717-7-defend53 t17280717-7-deflabel28" targOrder="Y" result="persNameOccupation"/>, was indicted for <rs type="offenceDescription" id="t17280717-7-off29">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-7-off29"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-7-off29"/>
>                    stealing a Gold Watch, value 16 l. a Chain, value 4 l. a Cornelian Seal set in Gold, value 10s. the Property of
>                          <persName type="victimName" id="t17280717-7-victim54">
>                             Nicholas
>                             Roper
>                          <interp value="Roper" type="surname" inst="t17280717-7-victim54"/>
>                             <interp value="Nicholas" type="given" inst="t17280717-7-victim54"/>
>                             <interp value="male" type="gender" inst="t17280717-7-victim54"/>
>                          </persName>
>                         from the Person of
>                          <persName type="victimName" id="t17280717-7-victim55">
>                             Phoebe
>                             Thickpenny
>                          <interp value="Thickpenny" type="surname" inst="t17280717-7-victim55"/>
>                             <interp value="Phoebe" type="given" inst="t17280717-7-victim55"/>
>                             <interp value="female" type="gender" inst="t17280717-7-victim55"/>
>                          </persName>
>
>
>                       </rs>, who depos'd, That her Mistress being at Little Chelsea, sent her to their House in Lad-lane, for the Watch and other Things she had occasion for, that she call'd at the Prisoner's in Castle Yard, Chick-Lane, as she was going towards Chelsea with the Watch and the Bundle, where she drank part of a Pint of Beer, some Tea 2 Quarterns of Brandy, and a Bottle of Cyder; that the Prisoner would go part of the Way home with her, and in <placeName id="t17280717-7-crimeloc30">Leather-Lane</placeName>
>                       <interp value="Leather-Lane" type="placeName" inst="t17280717-7-crimeloc30"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-7-crimeloc30"/>
>                       <join targets="t17280717-7-off29 t17280717-7-crimeloc30" targOrder="Y" result="offencePlace"/>, he said, faith they would not part Dry lips, and accordingly they went into a publick House and drank a Pint of Twopenny, and two Quarterns of Brandy, that she had the Watch then, and at the Door the Prisoner kiss'd her, and gave her a shilling for a Coach, she having out-staid her Time; that when he kiss'd her, he put one Hand around her Waist, but what he did with the other she could not tell, that she then cross'd the Way to another House, and immediately miss'd the Watch, and she was sure she had pinn'd it so to her Side, that she could not drop it.</p>
>                    <p>
>
>                       <persName id="t17280717-7-person56">
>                       Margaret
>                       Nelson
>                    <interp value="Nelson" type="surname" inst="t17280717-7-person56"/>
>                          <interp value="Margaret" type="given" inst="t17280717-7-person56"/>
>                          <interp value="female" type="gender" inst="t17280717-7-person56"/>
>                       </persName>
>                  depos'd, That the Prisoner and Phoebe Thickpenny, came to her House in Leather-Lane, and he call'd for a private Room, to which the Girl would not consent, that the Girl wanted to go to, &amp;c. and she went with her, when the Girl said, she had her Mistress's Gold Watch, and seeming to look on it, told her it was 7 o' Clock, but this Deponent did not see the Watch, yet she said, she did verily believe she heard it beat, that they soon parted, and in a Quarter of an Hour the maid returned, and said she had lost the Watch.</p>
>                    <p>The Prisoner said in his Defence, That he knew nothing of it, any further than she said it was her Mistress's, that they parted very good Friends, and she having been an old Sweetheart of his, laid her Head upon his Shoulder, and said, She could live and die there, which would have been no little Aggravation to his Crime, had he wrong'd so good Natur'd a Creature: But the Evidence against him being weak, and several appearing to his Character, the Jury <rs type="verdictDescription" id="t17280717-7-verdict31">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-7-verdict31"/>
>                    acquitted
>                 </rs> him. </p>
>                 </div1>
>
>
>                 <div1 id="t17280717-8" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-8"/>
>                    <interp value="1728" type="year" inst="t17280717-8"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-8"/>
>                    <interp value="17280717" type="date" inst="t17280717-8"/>
>                    <join targets="t17280717-8-defend57 t17280717-8-off33 t17280717-8-verdict35" targOrder="Y" id="t17280717-8-off33-c53" result="criminalCharge"/>
>                    <join targets="t17280717-8-defend58 t17280717-8-off33 t17280717-8-verdict35" targOrder="Y" id="t17280717-8-off33-c54" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-8-defend57">
>                       Joseph
>                       Plummer
>                    <interp value="Plummer" type="surname" inst="t17280717-8-defend57"/>
>                          <interp value="Joseph" type="given" inst="t17280717-8-defend57"/>
>                          <interp value="male" type="gender" inst="t17280717-8-defend57"/>
>                       </persName>
>                  , and
>                    <persName type="defendantName" id="t17280717-8-defend58">
>                       Henry
>                       Coleman
>                    <interp value="Coleman" type="surname" inst="t17280717-8-defend58"/>
>                          <interp value="Henry" type="given" inst="t17280717-8-defend58"/>
>                          <interp value="male" type="gender" inst="t17280717-8-defend58"/>
>                       </persName>
>                  , of <placeName id="t17280717-8-defloc32">Edmonton</placeName>
>                       <interp value="Edmont...

``` scala
elem
```

>     res1: scala.xml.Elem =
>     <TEI.2>
>        <text>
>           <body>
>              <div0 id="17280717" type="sessionsPaper">
>                 <interp value="BAILEY" type="collection" inst="17280717"/>
>                 <interp value="1728" type="year" inst="17280717"/>
>                 <interp value="sessionsPapers/17280717" type="uri" inst="17280717"/>
>                 <interp value="17280717" type="date" inst="17280717"/>
>                 <xptr doc="17280717" type="transcription"/>
>
>                 <div1 id="f17280717-1" type="frontMatter">
>                    <interp value="BAILEY" type="collection" inst="f17280717-1"/>
>                    <interp value="1728" type="year" inst="f17280717-1"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="f17280717-1"/>
>                    <interp value="17280717" type="date" inst="f17280717-1"/>
>
>                    <xptr doc="172807170001" type="pageFacsimile"/>THE PROCEEDINGS AT THE Sessions of the Peace, and Oyer and Terminer for the City of LONDON: AND
>              <p>On the King's Commission of Goal-Delivery of Newgate, held at Justice-Hall in the Old Baily, for the CITY of LONDON and COUNTY of MIDDLESEX.</p>
>                    <p>On Wednesday, Thursday, and Friday, being the 17th, 18th, and 19th of July, 1728, in the Second Year of His MAJESTY's Reign.</p>
>                    <p>(Price Six Pence)</p>
>                    <p>BEFORE the Right Honourable Sir
>                       <persName type="judiciaryName" id="f17280717-1-person1">
>                          EDWARD
>                          BECHER
>                       <interp value="BECHER" type="surname" inst="f17280717-1-person1"/>
>                          <interp value="EDWARD" type="given" inst="f17280717-1-person1"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person1"/>
>                       </persName>
>                    , Lord Mayor of the City of London; the Right Honourable the Lord Chief
>                       <persName type="judiciaryName" id="f17280717-1-person2">
>                          Baron
>                          Pengelly
>                       <interp value="Pengelly" type="surname" inst="f17280717-1-person2"/>
>                          <interp value="Baron" type="given" inst="f17280717-1-person2"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person2"/>
>                       </persName>
>                    ; Mr. Justice Reynolds; Mr.
>                       <persName type="judiciaryName" id="f17280717-1-person3">
>                          Baron
>                          Thompson
>                       <interp value="Thompson" type="surname" inst="f17280717-1-person3"/>
>                          <interp value="Baron" type="given" inst="f17280717-1-person3"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person3"/>
>                       </persName>
>                    , Recorder of the City of London; and
>                       <persName type="judiciaryName" id="f17280717-1-person4">
>                          John
>                          Raby
>                       <interp value="Raby" type="surname" inst="f17280717-1-person4"/>
>                          <interp value="John" type="given" inst="f17280717-1-person4"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person4"/>
>                       </persName>
>                    , Esq; Serjeant at Law; and other His Majesty's Justices of Goal-Delivery, and Oyer and Terminer aforesaid; Together with several of His Majesty's Justices of the Peace for the said City of London.</p>
>
>                    <p>London Jury.</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person5">
>                          John
>                          Land
>                       <interp value="Land" type="surname" inst="f17280717-1-person5"/>
>                          <interp value="John" type="given" inst="f17280717-1-person5"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person5"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person6">
>                          Nathaniel
>                          Mason
>                       <interp value="Mason" type="surname" inst="f17280717-1-person6"/>
>                          <interp value="Nathaniel" type="given" inst="f17280717-1-person6"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person6"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person7">
>                          Benjamin
>                          Allibone
>                       <interp value="Allibone" type="surname" inst="f17280717-1-person7"/>
>                          <interp value="Benjamin" type="given" inst="f17280717-1-person7"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person7"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person8">
>                          Joseph
>                          Westwood
>                       <interp value="Westwood" type="surname" inst="f17280717-1-person8"/>
>                          <interp value="Joseph" type="given" inst="f17280717-1-person8"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person8"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person9">
>                          Gabriel
>                          Wittacre
>                       <interp value="Wittacre" type="surname" inst="f17280717-1-person9"/>
>                          <interp value="Gabriel" type="given" inst="f17280717-1-person9"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person9"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person10">
>                          Samuel
>                          Tilley
>                       <interp value="Tilley" type="surname" inst="f17280717-1-person10"/>
>                          <interp value="Samuel" type="given" inst="f17280717-1-person10"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person10"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person11">
>                          Robert
>                          Lathwell
>                       <interp value="Lathwell" type="surname" inst="f17280717-1-person11"/>
>                          <interp value="Robert" type="given" inst="f17280717-1-person11"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person11"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person12">
>                          Edward
>                          Newman
>                       <interp value="Newman" type="surname" inst="f17280717-1-person12"/>
>                          <interp value="Edward" type="given" inst="f17280717-1-person12"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person12"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person13">
>                          Nathaniel
>                          Pickering
>                       <interp value="Pickering" type="surname" inst="f17280717-1-person13"/>
>                          <interp value="Nathaniel" type="given" inst="f17280717-1-person13"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person13"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person14">
>                          Simon
>                          Tunks
>                       <interp value="Tunks" type="surname" inst="f17280717-1-person14"/>
>                          <interp value="Simon" type="given" inst="f17280717-1-person14"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person14"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person15">
>                          Thomas
>                          Maud
>                       <interp value="Maud" type="surname" inst="f17280717-1-person15"/>
>                          <interp value="Thomas" type="given" inst="f17280717-1-person15"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person15"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person16">
>                          John
>                          Bond
>                       <interp value="Bond" type="surname" inst="f17280717-1-person16"/>
>                          <interp value="John" type="given" inst="f17280717-1-person16"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person16"/>
>                       </persName>
>                    .</p>
>
>
>                    <p>Middlesex Jury.</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person17">
>                          Elisha
>                          Impey
>                       <interp value="Impey" type="surname" inst="f17280717-1-person17"/>
>                          <interp value="Elisha" type="given" inst="f17280717-1-person17"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person17"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person18">
>                          Christopher
>                          Harris
>                       <interp value="Harris" type="surname" inst="f17280717-1-person18"/>
>                          <interp value="Christopher" type="given" inst="f17280717-1-person18"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person18"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person19">
>                          William
>                          Perkins
>                       <interp value="Perkins" type="surname" inst="f17280717-1-person19"/>
>                          <interp value="William" type="given" inst="f17280717-1-person19"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person19"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person20">
>                          Gilbert
>                          Watson
>                       <interp value="Watson" type="surname" inst="f17280717-1-person20"/>
>                          <interp value="Gilbert" type="given" inst="f17280717-1-person20"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person20"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person21">
>                          John
>                          Wells
>                       <interp value="Wells" type="surname" inst="f17280717-1-person21"/>
>                          <interp value="John" type="given" inst="f17280717-1-person21"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person21"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person22">
>                          William
>                          Carpenter
>                       <interp value="Carpenter" type="surname" inst="f17280717-1-person22"/>
>                          <interp value="William" type="given" inst="f17280717-1-person22"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person22"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person23">
>                          Allen
>                          Evans
>                       <interp value="Evans" type="surname" inst="f17280717-1-person23"/>
>                          <interp value="Allen" type="given" inst="f17280717-1-person23"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person23"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person24">
>                          Henry
>                          Cowmbe
>                       <interp value="Cowmbe" type="surname" inst="f17280717-1-person24"/>
>                          <interp value="Henry" type="given" inst="f17280717-1-person24"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person24"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person25">
>                          Simon
>                          Parsons
>                       <interp value="Parsons" type="surname" inst="f17280717-1-person25"/>
>                          <interp value="Simon" type="given" inst="f17280717-1-person25"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person25"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person26">
>                          George
>                          Gilbert
>                       <interp value="Gilbert" type="surname" inst="f17280717-1-person26"/>
>                          <interp value="George" type="given" inst="f17280717-1-person26"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person26"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person27">
>                          Nicholas
>                          Gardner
>                       <interp value="Gardner" type="surname" inst="f17280717-1-person27"/>
>                          <interp value="Nicholas" type="given" inst="f17280717-1-person27"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person27"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person28">
>                          Thomas
>                          Ireland
>                       <interp value="Ireland" type="surname" inst="f17280717-1-person28"/>
>                          <interp value="Thomas" type="given" inst="f17280717-1-person28"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person28"/>
>                       </persName>
>                    .</p>
>
>                 </div1>
>
>
>                 <div1 id="t17280717-1" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-1"/>
>                    <interp value="1728" type="year" inst="t17280717-1"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-1"/>
>                    <interp value="17280717" type="date" inst="t17280717-1"/>
>                    <join targets="t17280717-1-defend29 t17280717-1-off2 t17280717-1-verdict5" targOrder="Y" id="t17280717-1-off2-c29" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-1-defend29">
>                       James
>                       Haddock
>                    <interp value="Haddock" type="surname" inst="t17280717-1-defend29"/>
>                          <interp value="James" type="given" inst="t17280717-1-defend29"/>
>                          <interp value="male" type="gender" inst="t17280717-1-defend29"/>
>                       </persName>
>                 , of <placeName id="t17280717-1-defloc1">St. Bennet's Paul's Wharf</placeName>
>                       <interp value="St. Bennet's Paul's Wharf" type="placeName" inst="t17280717-1-defloc1"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-1-defloc1"/>
>                       <join targets="t17280717-1-defend29 t17280717-1-defloc1" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-1-off2">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-1-off2"/>
>                          <interp value="theftFromPlace" type="offenceSubcategory" inst="t17280717-1-off2"/>
>                    feloniously stealing 2 Guineas, and 5 l. 16 s. in Silver, a Silver Cup, a Silver Cork Screw, 2 Silver Spoons, and a Nutmeg-grater, in the Dwelling-House of
>
>                             <persName type="victimName" id="t17280717-1-victim31">
>                                James
>                                Reeves
>                             <interp value="Reeves" type="surname" inst="t17280717-1-victim31"/>
>                             <interp value="James" type="given" inst="t17280717-1-victim31"/>
>                             <interp value="male" type="gender" inst="t17280717-1-victim31"/>
>                             <join targets="t17280717-1-off2 t17280717-1-victim31" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>
>
>
>                       </rs>, on the <rs type="crimeDate" id="t17280717-1-cd3">9th of April, and in the 13th Year of his late Majesty King George the First</rs>
>                       <join targets="t17280717-1-off2 t17280717-1-cd3" targOrder="Y" result="offenceCrimeDate"/>, the Property of
>                    <persName id="t17280717-1-person32">
>                       James
>                       Reeves
>                    <interp value="Reeves" type="surname" inst="t17280717-1-person32"/>
>                          <interp value="James" type="given" inst="t17280717-1-person32"/>
>                          <interp value="male" type="gender" inst="t17280717-1-person32"/>
>                       </persName>
>                  aforesaid.</p>
>                    <p>
>
>                       <persName id="t17280717-1-person33">
>                       Elizabeth
>                       Reeves
>                    <interp value="Reeves" type="surname" inst="t17280717-1-person33"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-1-person33"/>
>                          <interp value="female" type="gender" inst="t17280717-1-person33"/>
>                       </persName>
>                  depos'd, That the Prisoner was a Lodger at her House on <placeName id="t17280717-1-crimeloc4">Addle-Hill</placeName>
>                       <interp value="Addle-Hill" type="placeName" inst="t17280717-1-crimeloc4"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-1-crimeloc4"/>
>                       <join targets="t17280717-1-off2 t17280717-1-crimeloc4" targOrder="Y" result="offencePlace"/>, near Doctors Commons, when this Robbery was committed, and that it being on the Sabbath Day, she desired the Prisoner, if he did not go abroad, to have an Eye to her Room, which she locked up, and which, he promised to have an Eye to; but when she came home, the Chamber Door and the Corner-Cupboard had been forced open, which appeared by the Mark of an Instrument, the Hinges tore off and the Money gone, the Drawers rifled, and the Plate taken out, though some of the Drawers, out of which the Plate was taken, she left lock'd when she went from Home; upon which she cried out, saying, she was robb'd, and the Prisoner's Wife being above Stairs, came down, and said, her Husband was gone out, that he had been guilty of Failings, and desired her to be easy and she would Work early and late to make Satisfaction, though she did not know he had taken the Things, but his not coming home again confirmed them the more in this Suspicion; and there were other Witnesses, who depos'd, That the Prosecutor left the Prisoner in Care of her Door, and that he promised to look after it.</p>
>                    <p>The Prisoner said in his Defence, That Mrs. Reeves had lost a pair of Silver Buckles out of her Drawers a Fortnight before this Robbery, and she said, she did believe it was done by a Char-woman that she employ'd, which Mrs. Reeves acknowledging, and the Prisoner telling a plausible Story of his being in bad Circumstances, and that Day the Robbery was committed, one Diston pittying his Case, lent him Money to go down to Bristol, to Trade there, if possible to retrieve himself, and he not daring to go out of Doors in the Week Days, went out of the House unfortunately on that Afternoon: There being no positive Evidence against him, the Jury <rs type="verdictDescription" id="t17280717-1-verdict5">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-1-verdict5"/>
>                    acquitted
>                 </rs> him.</p>
>                 </div1>
>
>
>                 <div1 id="t17280717-2" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-2"/>
>                    <interp value="1728" type="year" inst="t17280717-2"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-2"/>
>                    <interp value="17280717" type="date" inst="t17280717-2"/>
>                    <join targets="t17280717-2-defend35 t17280717-2-off6 t17280717-2-verdict7" targOrder="Y" id="t17280717-2-off6-c33" result="criminalCharge"/>
>
>                    <p>
>
>
>                       <persName type="defendantName" id="t17280717-2-defend35">
>                          David
>                          Ball
>                       <interp value="Ball" type="surname" inst="t17280717-2-defend35"/>
>                          <interp value="David" type="given" inst="t17280717-2-defend35"/>
>                          <interp value="male" type="gender" inst="t17280717-2-defend35"/>
>                       </persName>
>
>                  was indicted for <rs type="offenceDescription" id="t17280717-2-off6">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-2-off6"/>
>                          <interp value="pettyLarceny" type="offenceSubcategory" inst="t17280717-2-off6"/>
>                    a Petty Larceny, in stealing a Yard and a Quarter of Linnen Cloth, value 11 d.
>                 </rs> the Goods of
>
>                       <persName type="victimName" id="t17280717-2-victim37">
>                          John
>                          Williams
>                       <interp value="Williams" type="surname" inst="t17280717-2-victim37"/>
>                          <interp value="John" type="given" inst="t17280717-2-victim37"/>
>                          <interp value="male" type="gender" inst="t17280717-2-victim37"/>
>                       </persName>
>
>                  and
>
>                       <persName type="victimName" id="t17280717-2-victim39">
>                          William
>                          Williams
>                       <interp value="Williams" type="surname" inst="t17280717-2-victim39"/>
>                          <interp value="William" type="given" inst="t17280717-2-victim39"/>
>                          <interp value="male" type="gender" inst="t17280717-2-victim39"/>
>                       </persName>
>
>                 ; to which Indictment he <rs type="verdictDescription" id="t17280717-2-verdict7">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-2-verdict7"/>
>                          <interp value="pleadedGuilty" type="verdictSubcategory" inst="t17280717-2-verdict7"/>
>                    pleaded Guilty
>                 </rs>.</p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-2-punish8">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-2-punish8"/>
>                          <join targets="t17280717-2-defend35 t17280717-2-punish8" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 id="t17280717-3" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-3"/>
>                    <interp value="1728" type="year" inst="t17280717-3"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-3"/>
>                    <interp value="17280717" type="date" inst="t17280717-3"/>
>                    <join targets="t17280717-3-defend40 t17280717-3-off10 t17280717-3-verdict11" targOrder="Y" id="t17280717-3-off10-c36" result="criminalCharge"/>
>
>                    <p>
>                       <xptr doc="172807170002" type="pageFacsimile"/>
>
>                       <persName type="defendantName" id="t17280717-3-defend40">
>                       Nathaniel
>                       Mercy
>                    <interp value="Mercy" type="surname" inst="t17280717-3-defend40"/>
>                          <interp value="Nathaniel" type="given" inst="t17280717-3-defend40"/>
>                          <interp value="male" type="gender" inst="t17280717-3-defend40"/>
>                       </persName>
>                 , of <placeName id="t17280717-3-defloc9">St. James's Westminster</placeName>
>                       <interp value="St. James's Westminster" type="placeName" inst="t17280717-3-defloc9"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-3-defloc9"/>
>                       <join targets="t17280717-3-defend40 t17280717-3-defloc9" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-3-off10">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-3-off10"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-3-off10"/>
>                    stealing a Coach Wheel, value 12 Shillings
>                 </rs>, the Goods of
>                    <persName type="victimName" id="t17280717-3-victim41">
>                       Thomas
>                       West
>                    <interp value="West" type="surname" inst="t17280717-3-victim41"/>
>                          <interp value="Thomas" type="given" inst="t17280717-3-victim41"/>
>                          <interp value="male" type="gender" inst="t17280717-3-victim41"/>
>                          <join targets="t17280717-3-off10 t17280717-3-victim41" targOrder="Y" result="offenceVictim"/>
>                       </persName>
>                  , on the 9th of this Instant, to which Indictment he <rs type="verdictDescription" id="t17280717-3-verdict11">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-3-verdict11"/>
>                          <interp value="pleadedGuilty" type="verdictSubcategory" inst="t17280717-3-verdict11"/>
>                    pleaded guilty
>                 </rs>.</p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-3-punish12">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-3-punish12"/>
>                          <join targets="t17280717-3-defend40 t17280717-3-punish12" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 id="t17280717-4" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-4"/>
>                    <interp value="1728" type="year" inst="t17280717-4"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-4"/>
>                    <interp value="17280717" type="date" inst="t17280717-4"/>
>                    <join targets="t17280717-4-defend42 t17280717-4-off14 t17280717-4-verdict17" targOrder="Y" id="t17280717-4-off14-c38" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-4-defend42">
>                       Margaret
>                       King
>                    <interp value="King" type="surname" inst="t17280717-4-defend42"/>
>                          <interp value="Margaret" type="given" inst="t17280717-4-defend42"/>
>                          <interp value="female" type="gender" inst="t17280717-4-defend42"/>
>                       </persName>
>                 , of <placeName id="t17280717-4-defloc13">St. Ann's Westminster</placeName>
>                       <interp value="St. Ann's Westminster" type="placeName" inst="t17280717-4-defloc13"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-4-defloc13"/>
>                       <join targets="t17280717-4-defend42 t17280717-4-defloc13" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-4-off14">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-4-off14"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-4-off14"/>
>                    privately stealing a Gold Watch, value 16 l.
>                 </rs> on the <rs type="crimeDate" id="t17280717-4-cd15">first of May</rs>
>                       <join targets="t17280717-4-off14 t17280717-4-cd15" targOrder="Y" result="offenceCrimeDate"/> last, the Property of
>                    <persName type="victimName" id="t17280717-4-victim43">
>                       Timothy
>                       Conner
>                    <interp value="Conner" type="surname" inst="t17280717-4-victim43"/>
>                          <interp value="Timothy" type="given" inst="t17280717-4-victim43"/>
>                          <interp value="male" type="gender" inst="t17280717-4-victim43"/>
>                          <join targets="t17280717-4-off14 t17280717-4-victim43" targOrder="Y" result="offenceVictim"/>
>                       </persName>
>                  .</p>
>                    <p>The Prosecutor depos'd, That he met the Prisoner in the Street, and ask'd her to drink a Glass of Wine, to which she consented, and they went to the <placeName id="t17280717-4-crimeloc16">Swan Tavern in Newport Market</placeName>
>                       <interp value="Swan Tavern in Newport Market" type="placeName" inst="t17280717-4-crimeloc16"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-4-crimeloc16"/>
>                       <join targets="t17280717-4-off14 t17280717-4-crimeloc16" targOrder="Y" result="offencePlace"/>, and lovingly drank 4 Pints, the Prisoner asking him what it was a Clock, he pulled out his Gold Watch, and bid her look; that she took it in her Hand, but could not remember that she returned it again, neither could she say positively, that she had it, but as he mis'd it as soon as he parted with her he thought he had occasion of Suspicion; yet, said he, I had been drinking before, (tho' it was but Nine in the Morning)and can't tell directly how the Matter stood; which being all the Evidence he could give against her, she was <rs type="verdictDescription" id="t17280717-4-verdict17">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-4-verdict17"/>
>                    acquitted
>                 </rs>.</p>
>                 </div1>
>
>
>                 <div1 id="t17280717-5" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-5"/>
>                    <interp value="1728" type="year" inst="t17280717-5"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-5"/>
>                    <interp value="17280717" type="date" inst="t17280717-5"/>
>                    <join targets="t17280717-5-defend44 t17280717-5-off19 t17280717-5-verdict22" targOrder="Y" id="t17280717-5-off19-c40" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-5-defend44">
>                       Elizabeth
>                       Mould
>                    <interp value="Mould" type="surname" inst="t17280717-5-defend44"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-5-defend44"/>
>                          <interp value="female" type="gender" inst="t17280717-5-defend44"/>
>                       </persName>
>                 , of <placeName id="t17280717-5-defloc18">St. Martins in the Fields</placeName>
>                       <interp value="St. Martins in the Fields" type="placeName" inst="t17280717-5-defloc18"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-5-defloc18"/>
>                       <join targets="t17280717-5-defend44 t17280717-5-defloc18" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-5-off19">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-5-off19"/>
>                          <interp value="pocketpicking" type="offenceSubcategory" inst="t17280717-5-off19"/>
>                    privately stealing Fifty Pounds and eight Shillings, from the Person of
>                          <persName type="victimName" id="t17280717-5-victim45">
>                             John
>                             Coxall
>                          <interp value="Coxall" type="surname" inst="t17280717-5-victim45"/>
>                             <interp value="John" type="given" inst="t17280717-5-victim45"/>
>                             <interp value="male" type="gender" inst="t17280717-5-victim45"/>
>                             <join targets="t17280717-5-off19 t17280717-5-victim45" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>
>
>                       </rs>, on the <rs type="crimeDate" id="t17280717-5-cd20">24th of June</rs>
>                       <join targets="t17280717-5-off19 t17280717-5-cd20" targOrder="Y" result="offenceCrimeDate"/> last</p>
>                    <p>The Prosecutor depos'd, That he being a <rs type="occupation" id="t17280717-5-viclabel21">Bricklayer</rs>
>                       <join targets="t17280717-5-victim45 t17280717-5-viclabel21" targOrder="Y" result="persNameOccupation"/>, was endeavouring to get some Business to do, at the House where the Prisoner lived, and it being a Chandler's Shop, to obtain the Good-Will of the People, he call'd for several Drams, and treated the Mistress of the House and one Mrs. Greaves, who was his Customer, with Cyder, Brandy, &amp;c. that whilst they were drinking the Prisoner came into the Room, and he likewise treated her, and she, in return, wip'd him over the Face, and seem'd very fond of him, saying, she would give him a fine Nosegay, and something to cheer his Heart, if he would go with her to Covent-Garden Market, and then (as his Expression was) she weagled him down into the Cellar, and there kept him lock'd up in a back Passage, that he was very much in Liquor, and scarce sensible of what he did, but he found what she had done to his Sorrow, for she had taken all his Money, which he missing, made a Noise, and the People of the House hearing him, let him out of his Place of Confinement, by conducting him up the Back Stairs; that he got Officers and search'd the Cellar, but could not find the Prisoner. The Prisoner desiring he might be ask'd if they did not drink together in the Cellar, the Prosecutor answer'd, No, she would fetch no Drink, saying, she did not care to be seen by the Publicans; that she was all for dry Money, and she, and None but she, had his Fifty Pounds, for save only him and her, there were neither Man, Woman, nor Child, nor Dog nor Cat in the Cellar.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person46">
>                       Elizabeth
>                       Harper
>                    <interp value="Harper" type="surname" inst="t17280717-5-person46"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-5-person46"/>
>                          <interp value="female" type="gender" inst="t17280717-5-person46"/>
>                       </persName>
>                  depos'd, That the Prosecutor came into her Shop, and drank Cyder, Usquebaugh and Brandy, and being disguis'd in Liquor he pull'd out eight or ten Guineas and said, he was no Scoundrel; that she and Mrs. Greaves, whom he brought in to treat, begg'd he would put up his Money and take Care of it, and about that Time the Prisoner came into the Room, and familiarly stroaking his cheeks, persuaded him to go into the Cellar, saying, he should pay his Footing, that they staid half an Hour below, and this Deponent looking down, saw the Prisoner's Mother there, that he came up with her again, and treated her with Usquebaugh, and at 11 o' Clock, which was several Hours after, they heard him in a back Passage, where the House was supplied with Water, and letting him into their House, he said he was robb'd, at which this Deponent's Husband thrust him out of doors.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person47">
>                       Isabella
>                       Greaves
>                    <interp value="Greaves" type="surname" inst="t17280717-5-person47"/>
>                          <interp value="Isabella" type="given" inst="t17280717-5-person47"/>
>                          <interp value="female" type="gender" inst="t17280717-5-person47"/>
>                       </persName>
>                  likewise confirm'd every Part of this Deposition, adding, That the Prisoner was not to be seen that Night after the Robbery.</p>
>                    <p>The Prisoner said in her Defence, That the Prosecutor went down into her Cellar, and behaved himself so rudely, that she was forced to threaten to send for an Officer, that she knew nothing of his Money, and had not gone away that Night, but as she was oblig'd by her Husband, and that she came next Morning and set her Greens out.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person48">
>                       Anthony
>                       Dyer
>                    <interp value="Dyer" type="surname" inst="t17280717-5-person48"/>
>                          <interp value="Anthony" type="given" inst="t17280717-5-person48"/>
>                          <interp value="male" type="gender" inst="t17280717-5-person48"/>
>                       </persName>
>                  depos'd, That the Prosecutor told him he had lost his Money being Drunk, that he had been in a Cellar and in a Vault, where he fell asleep, and gave a very odd account of the Adventure.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person49">
>                       George
>                       Smith
>                    <interp value="Smith" type="surname" inst="t17280717-5-person49"/>
>                          <interp value="George" type="given" inst="t17280717-5-person49"/>
>                          <interp value="male" type="gender" inst="t17280717-5-person49"/>
>                       </persName>
>                  depos'd, That the Prosecutor said he fell asleep upon a Vault, and could charge no Body.</p>
>                    <p>The Constable, the Watchman, and others severally depos'd, That the Morning after this happened, he said he could charge no particular person, but he would indict the House. The Prisoner having a very good Character from several reputable Witnesses, the Jury <rs type="verdictDescription" id="t17280717-5-verdict22">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-5-verdict22"/>
>                    acquitted
>                 </rs> her.</p>
>                 </div1>
>
>
>                 <div1 id="t17280717-6" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-6"/>
>                    <interp value="1728" type="year" inst="t17280717-6"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-6"/>
>                    <interp value="17280717" type="date" inst="t17280717-6"/>
>                    <join targets="t17280717-6-defend50 t17280717-6-off24 t17280717-6-verdict26" targOrder="Y" id="t17280717-6-off24-c46" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-6-defend50">
>                       Phillip
>                       Hilliard
>                    <interp value="Hilliard" type="surname" inst="t17280717-6-defend50"/>
>                          <interp value="Phillip" type="given" inst="t17280717-6-defend50"/>
>                          <interp value="male" type="gender" inst="t17280717-6-defend50"/>
>                       </persName>
>                 , of <placeName id="t17280717-6-defloc23">St. James's Westminster</placeName>
>                       <interp value="St. James's Westminster" type="placeName" inst="t17280717-6-defloc23"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-6-defloc23"/>
>                       <join targets="t17280717-6-defend50 t17280717-6-defloc23" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-6-off24">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-6-off24"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-6-off24"/>
>                    feloniously stealing a Silver Spoon, value eleven Shillings, the Property of
>                          <persName type="victimName" id="t17280717-6-victim51">
>                             Abraham
>                             Mannio
>                          <interp value="Mannio" type="surname" inst="t17280717-6-victim51"/>
>                             <interp value="Abraham" type="given" inst="t17280717-6-victim51"/>
>                             <interp value="male" type="gender" inst="t17280717-6-victim51"/>
>                             <join targets="t17280717-6-off24 t17280717-6-victim51" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>                        , on the <rs type="crimeDate" id="t17280717-6-cd25">27th of June</rs>
>                          <join targets="t17280717-6-off24 t17280717-6-cd25" targOrder="Y" result="offenceCrimeDate"/> last.
>                 </rs>
>                    </p>
>                    <p>Mr. Hayden depos'd, That the Prisoner brought the Spoon to him to pawn, and he suspecting it to be stolen, stopp'd him and carried him to the Round-House, where he confess'd he stole it at the Prosecutor's, he being invited there to Dinner with some Gentlemans Servants.</p>
>                    <p>
>
>                       <persName id="t17280717-6-person52">
>                       Robert
>                       Amey
>                    <interp value="Amey" type="surname" inst="t17280717-6-person52"/>
>                          <interp value="Robert" type="given" inst="t17280717-6-person52"/>
>                          <interp value="male" type="gender" inst="t17280717-6-person52"/>
>                       </persName>
>                  depos'd, That he attended the Gentlemens Servants at Dinner, and afterwards they miss'd a Spoon, that Mr. Haydon sent them word of the Spoon brought to him by the Prisoner, and he going to match it by the others, found it to be the same which they had lost, it being of the same Make and Mark: The Fact being thus plainly proved upon him, the Jury found him <rs type="verdictDescription" id="t17280717-6-verdict26">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-6-verdict26"/>
>                          <interp value="theftunder1s" type="verdictSubcategory" inst="t17280717-6-verdict26"/>
>                    guilty to the value of 10d.
>                 </rs>
>                    </p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-6-punish27">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-6-punish27"/>
>                          <join targets="t17280717-6-defend50 t17280717-6-punish27" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>
>
>
>                 <div1 id="t17280717-7" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-7"/>
>                    <interp value="1728" type="year" inst="t17280717-7"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-7"/>
>                    <interp value="17280717" type="date" inst="t17280717-7"/>
>                    <join targets="t17280717-7-defend53 t17280717-7-off29 t17280717-7-verdict31" targOrder="Y" id="t17280717-7-off29-c49" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-7-defend53">
>                       Robert
>                       Ashby
>                    <interp value="Ashby" type="surname" inst="t17280717-7-defend53"/>
>                          <interp value="Robert" type="given" inst="t17280717-7-defend53"/>
>                          <interp value="male" type="gender" inst="t17280717-7-defend53"/>
>                       </persName>
>                 , of <rs type="occupation" id="t17280717-7-deflabel28">St. Andrew's Holborn</rs>
>                       <join targets="t17280717-7-defend53 t17280717-7-deflabel28" targOrder="Y" result="persNameOccupation"/>, was indicted for <rs type="offenceDescription" id="t17280717-7-off29">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-7-off29"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-7-off29"/>
>                    stealing a Gold Watch, value 16 l. a Chain, value 4 l. a Cornelian Seal set in Gold, value 10s. the Property of
>                          <persName type="victimName" id="t17280717-7-victim54">
>                             Nicholas
>                             Roper
>                          <interp value="Roper" type="surname" inst="t17280717-7-victim54"/>
>                             <interp value="Nicholas" type="given" inst="t17280717-7-victim54"/>
>                             <interp value="male" type="gender" inst="t17280717-7-victim54"/>
>                          </persName>
>                         from the Person of
>                          <persName type="victimName" id="t17280717-7-victim55">
>                             Phoebe
>                             Thickpenny
>                          <interp value="Thickpenny" type="surname" inst="t17280717-7-victim55"/>
>                             <interp value="Phoebe" type="given" inst="t17280717-7-victim55"/>
>                             <interp value="female" type="gender" inst="t17280717-7-victim55"/>
>                          </persName>
>
>
>                       </rs>, who depos'd, That her Mistress being at Little Chelsea, sent her to their House in Lad-lane, for the Watch and other Things she had occasion for, that she call'd at the Prisoner's in Castle Yard, Chick-Lane, as she was going towards Chelsea with the Watch and the Bundle, where she drank part of a Pint of Beer, some Tea 2 Quarterns of Brandy, and a Bottle of Cyder; that the Prisoner would go part of the Way home with her, and in <placeName id="t17280717-7-crimeloc30">Leather-Lane</placeName>
>                       <interp value="Leather-Lane" type="placeName" inst="t17280717-7-crimeloc30"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-7-crimeloc30"/>
>                       <join targets="t17280717-7-off29 t17280717-7-crimeloc30" targOrder="Y" result="offencePlace"/>, he said, faith they would not part Dry lips, and accordingly they went into a publick House and drank a Pint of Twopenny, and two Quarterns of Brandy, that she had the Watch then, and at the Door the Prisoner kiss'd her, and gave her a shilling for a Coach, she having out-staid her Time; that when he kiss'd her, he put one Hand around her Waist, but what he did with the other she could not tell, that she then cross'd the Way to another House, and immediately miss'd the Watch, and she was sure she had pinn'd it so to her Side, that she could not drop it.</p>
>                    <p>
>
>                       <persName id="t17280717-7-person56">
>                       Margaret
>                       Nelson
>                    <interp value="Nelson" type="surname" inst="t17280717-7-person56"/>
>                          <interp value="Margaret" type="given" inst="t17280717-7-person56"/>
>                          <interp value="female" type="gender" inst="t17280717-7-person56"/>
>                       </persName>
>                  depos'd, That the Prisoner and Phoebe Thickpenny, came to her House in Leather-Lane, and he call'd for a private Room, to which the Girl would not consent, that the Girl wanted to go to, &amp;c. and she went with her, when the Girl said, she had her Mistress's Gold Watch, and seeming to look on it, told her it was 7 o' Clock, but this Deponent did not see the Watch, yet she said, she did verily believe she heard it beat, that they soon parted, and in a Quarter of an Hour the maid returned, and said she had lost the Watch.</p>
>                    <p>The Prisoner said in his Defence, That he knew nothing of it, any further than she said it was her Mistress's, that they parted very good Friends, and she having been an old Sweetheart of his, laid her Head upon his Shoulder, and said, She could live and die there, which would have been no little Aggravation to his Crime, had he wrong'd so good Natur'd a Creature: But the Evidence against him being weak, and several appearing to his Character, the Jury <rs type="verdictDescription" id="t17280717-7-verdict31">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-7-verdict31"/>
>                    acquitted
>                 </rs> him. </p>
>                 </div1>
>
>
>                 <div1 id="t17280717-8" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-8"/>
>                    <interp value="1728" type="year" inst="t17280717-8"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-8"/>
>                    <interp value="17280717" type="date" inst="t17280717-8"/>
>                    <join targets="t17280717-8-defend57 t17280717-8-off33 t17280717-8-verdict35" targOrder="Y" id="t17280717-8-off33-c53" result="criminalCharge"/>
>                    <join targets="t17280717-8-defend58 t17280717-8-off33 t17280717-8-verdict35" targOrder="Y" id="t17280717-8-off33-c54" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-8-defend57">
>                       Joseph
>                       Plummer
>                    <interp value="Plummer" type="surname" inst="t17280717-8-defend57"/>
>                          <interp value="Joseph" type="given" inst="t17280717-8-defend57"/>
>                          <interp value="male" type="gender" inst="t17280717-8-defend57"/>
>                       </persName>
>                  , and
>                    <persName type="defendantName" id="t17280717-8-defend58">
>                       Henry
>                       Coleman
>                    <interp value="Coleman" type="surname" inst="t17280717-8-defend58"/>
>                          <interp value="Henry" type="given" inst="t17280717-8-defend58"/>
>                          <interp value="male" type="gender" inst="t17280717-8-defend58"/>
>                       </persName>
>                  , of <placeName id="t17280717-8-defloc32">Edmonton</placeName>
>                       <interp value="Edmont...

Quick Preparation
-----------------

#### Some examples to learn xml and scala in a hurry

``` scala
val p = new scala.xml.PrettyPrinter(80, 2)

p.format(elem)
```

>     p: scala.xml.PrettyPrinter = scala.xml.PrettyPrinter@38ac7373
>     res2: String =
>     <TEI.2>
>       <text>
>         <body>
>           <div0 id="17280717" type="sessionsPaper">
>             <interp value="BAILEY" type="collection" inst="17280717"/>
>             <interp value="1728" type="year" inst="17280717"/>
>             <interp value="sessionsPapers/17280717" type="uri" inst="17280717"/>
>             <interp value="17280717" type="date" inst="17280717"/>
>             <xptr doc="17280717" type="transcription"/>
>             <div1 id="f17280717-1" type="frontMatter">
>               <interp value="BAILEY" type="collection" inst="f17280717-1"/>
>               <interp value="1728" type="year" inst="f17280717-1"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="f17280717-1"/>
>               <interp value="17280717" type="date" inst="f17280717-1"/>
>               <xptr doc="172807170001" type="pageFacsimile"/>
>               THE PROCEEDINGS AT THE Sessions of the Peace, and Oyer and Terminer for the City of LONDON: AND
>               <p>
>                 On the King's Commission of Goal-Delivery of Newgate, held at Justice-Hall in the Old Baily, for the CITY of LONDON and COUNTY of MIDDLESEX.
>               </p>
>               <p>
>                 On Wednesday, Thursday, and Friday, being the 17th, 18th, and 19th of July, 1728, in the Second Year of His MAJESTY's Reign.
>               </p>
>               <p>(Price Six Pence)</p>
>               <p>
>                 BEFORE the Right Honourable Sir
>                 <persName type="judiciaryName" id="f17280717-1-person1">
>                   EDWARD
>                          BECHER
>                   <interp value="BECHER" type="surname" inst="f17280717-1-person1"/>
>                   <interp value="EDWARD" type="given" inst="f17280717-1-person1"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person1"/>
>                 </persName>
>                 , Lord Mayor of the City of London; the Right Honourable the Lord Chief
>                 <persName type="judiciaryName" id="f17280717-1-person2">
>                   Baron
>                          Pengelly
>                   <interp value="Pengelly" type="surname" inst="f17280717-1-person2"/>
>                   <interp value="Baron" type="given" inst="f17280717-1-person2"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person2"/>
>                 </persName>
>                 ; Mr. Justice Reynolds; Mr.
>                 <persName type="judiciaryName" id="f17280717-1-person3">
>                   Baron
>                          Thompson
>                   <interp value="Thompson" type="surname" inst="f17280717-1-person3"/>
>                   <interp value="Baron" type="given" inst="f17280717-1-person3"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person3"/>
>                 </persName>
>                 , Recorder of the City of London; and
>                 <persName type="judiciaryName" id="f17280717-1-person4">
>                   John
>                          Raby
>                   <interp value="Raby" type="surname" inst="f17280717-1-person4"/>
>                   <interp value="John" type="given" inst="f17280717-1-person4"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person4"/>
>                 </persName>
>                 , Esq; Serjeant at Law; and other His Majesty's Justices of Goal-Delivery, and Oyer and Terminer aforesaid; Together with several of His Majesty's Justices of the Peace for the said City of London.
>               </p>
>               <p>London Jury.</p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person5">
>                   John
>                          Land
>                   <interp value="Land" type="surname" inst="f17280717-1-person5"/>
>                   <interp value="John" type="given" inst="f17280717-1-person5"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person5"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person6">
>                   Nathaniel
>                          Mason
>                   <interp value="Mason" type="surname" inst="f17280717-1-person6"/>
>                   <interp value="Nathaniel" type="given" inst="f17280717-1-person6"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person6"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person7">
>                   Benjamin
>                          Allibone
>                   <interp value="Allibone" type="surname" inst="f17280717-1-person7"/>
>                   <interp value="Benjamin" type="given" inst="f17280717-1-person7"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person7"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person8">
>                   Joseph
>                          Westwood
>                   <interp value="Westwood" type="surname" inst="f17280717-1-person8"/>
>                   <interp value="Joseph" type="given" inst="f17280717-1-person8"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person8"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person9">
>                   Gabriel
>                          Wittacre
>                   <interp value="Wittacre" type="surname" inst="f17280717-1-person9"/>
>                   <interp value="Gabriel" type="given" inst="f17280717-1-person9"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person9"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person10">
>                   Samuel
>                          Tilley
>                   <interp value="Tilley" type="surname" inst="f17280717-1-person10"/>
>                   <interp value="Samuel" type="given" inst="f17280717-1-person10"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person10"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person11">
>                   Robert
>                          Lathwell
>                   <interp value="Lathwell" type="surname" inst="f17280717-1-person11"/>
>                   <interp value="Robert" type="given" inst="f17280717-1-person11"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person11"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person12">
>                   Edward
>                          Newman
>                   <interp value="Newman" type="surname" inst="f17280717-1-person12"/>
>                   <interp value="Edward" type="given" inst="f17280717-1-person12"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person12"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person13">
>                   Nathaniel
>                          Pickering
>                   <interp value="Pickering" type="surname" inst="f17280717-1-person13"/>
>                   <interp value="Nathaniel" type="given" inst="f17280717-1-person13"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person13"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person14">
>                   Simon
>                          Tunks
>                   <interp value="Tunks" type="surname" inst="f17280717-1-person14"/>
>                   <interp value="Simon" type="given" inst="f17280717-1-person14"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person14"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person15">
>                   Thomas
>                          Maud
>                   <interp value="Maud" type="surname" inst="f17280717-1-person15"/>
>                   <interp value="Thomas" type="given" inst="f17280717-1-person15"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person15"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person16">
>                   John
>                          Bond
>                   <interp value="Bond" type="surname" inst="f17280717-1-person16"/>
>                   <interp value="John" type="given" inst="f17280717-1-person16"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person16"/>
>                 </persName>
>                 .
>               </p>
>               <p>Middlesex Jury.</p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person17">
>                   Elisha
>                          Impey
>                   <interp value="Impey" type="surname" inst="f17280717-1-person17"/>
>                   <interp value="Elisha" type="given" inst="f17280717-1-person17"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person17"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person18">
>                   Christopher
>                          Harris
>                   <interp value="Harris" type="surname" inst="f17280717-1-person18"/>
>                   <interp value="Christopher" type="given" inst="f17280717-1-person18"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person18"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person19">
>                   William
>                          Perkins
>                   <interp value="Perkins" type="surname" inst="f17280717-1-person19"/>
>                   <interp value="William" type="given" inst="f17280717-1-person19"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person19"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person20">
>                   Gilbert
>                          Watson
>                   <interp value="Watson" type="surname" inst="f17280717-1-person20"/>
>                   <interp value="Gilbert" type="given" inst="f17280717-1-person20"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person20"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person21">
>                   John
>                          Wells
>                   <interp value="Wells" type="surname" inst="f17280717-1-person21"/>
>                   <interp value="John" type="given" inst="f17280717-1-person21"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person21"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person22">
>                   William
>                          Carpenter
>                   <interp value="Carpenter" type="surname" inst="f17280717-1-person22"/>
>                   <interp value="William" type="given" inst="f17280717-1-person22"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person22"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person23">
>                   Allen
>                          Evans
>                   <interp value="Evans" type="surname" inst="f17280717-1-person23"/>
>                   <interp value="Allen" type="given" inst="f17280717-1-person23"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person23"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person24">
>                   Henry
>                          Cowmbe
>                   <interp value="Cowmbe" type="surname" inst="f17280717-1-person24"/>
>                   <interp value="Henry" type="given" inst="f17280717-1-person24"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person24"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person25">
>                   Simon
>                          Parsons
>                   <interp value="Parsons" type="surname" inst="f17280717-1-person25"/>
>                   <interp value="Simon" type="given" inst="f17280717-1-person25"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person25"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person26">
>                   George
>                          Gilbert
>                   <interp value="Gilbert" type="surname" inst="f17280717-1-person26"/>
>                   <interp value="George" type="given" inst="f17280717-1-person26"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person26"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person27">
>                   Nicholas
>                          Gardner
>                   <interp value="Gardner" type="surname" inst="f17280717-1-person27"/>
>                   <interp value="Nicholas" type="given" inst="f17280717-1-person27"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person27"/>
>                 </persName>
>                 ,
>               </p>
>               <p>
>                 <persName type="jurorName" id="f17280717-1-person28">
>                   Thomas
>                          Ireland
>                   <interp value="Ireland" type="surname" inst="f17280717-1-person28"/>
>                   <interp value="Thomas" type="given" inst="f17280717-1-person28"/>
>                   <interp value="male" type="gender" inst="f17280717-1-person28"/>
>                 </persName>
>                 .
>               </p>
>             </div1>
>             <div1 id="t17280717-1" type="trialAccount">
>               <interp value="BAILEY" type="collection" inst="t17280717-1"/>
>               <interp value="1728" type="year" inst="t17280717-1"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-1"/>
>               <interp value="17280717" type="date" inst="t17280717-1"/>
>               <join
>               targets="t17280717-1-defend29 t17280717-1-off2 t17280717-1-verdict5" targOrder="Y" id="t17280717-1-off2-c29" result="criminalCharge">
>     </join>
>               <p>
>                 <persName type="defendantName" id="t17280717-1-defend29">
>                   James
>                       Haddock
>                   <interp value="Haddock" type="surname" inst="t17280717-1-defend29"/>
>                   <interp value="James" type="given" inst="t17280717-1-defend29"/>
>                   <interp value="male" type="gender" inst="t17280717-1-defend29"/>
>                 </persName>
>                 , of
>                 <placeName id="t17280717-1-defloc1">St. Bennet's Paul's Wharf</placeName>
>                 <interp
>                 value="St. Bennet's Paul's Wharf" type="placeName" inst="t17280717-1-defloc1">
>     </interp>
>                 <interp value="defendantHome" type="type" inst="t17280717-1-defloc1"/>
>                 <join
>                 targets="t17280717-1-defend29 t17280717-1-defloc1" targOrder="Y" result="persNamePlace">
>     </join>
>                 , was indicted for
>                 <rs type="offenceDescription" id="t17280717-1-off2">
>                   <interp value="theft" type="offenceCategory" inst="t17280717-1-off2"/>
>                   <interp
>                   value="theftFromPlace" type="offenceSubcategory" inst="t17280717-1-off2">
>     </interp>
>                   feloniously stealing 2 Guineas, and 5 l. 16 s. in Silver, a Silver Cup, a Silver Cork Screw, 2 Silver Spoons, and a Nutmeg-grater, in the Dwelling-House of
>                   <persName type="victimName" id="t17280717-1-victim31">
>                     James
>                                Reeves
>                     <interp value="Reeves" type="surname" inst="t17280717-1-victim31"/>
>                     <interp value="James" type="given" inst="t17280717-1-victim31"/>
>                     <interp value="male" type="gender" inst="t17280717-1-victim31"/>
>                     <join
>                     targets="t17280717-1-off2 t17280717-1-victim31" targOrder="Y" result="offenceVictim">
>     </join>
>                   </persName>
>                 </rs>
>                 , on the
>                 <rs type="crimeDate" id="t17280717-1-cd3">
>                   9th of April, and in the 13th Year of his late Majesty King George the First
>                 </rs>
>                 <join
>                 targets="t17280717-1-off2 t17280717-1-cd3" targOrder="Y" result="offenceCrimeDate">
>     </join>
>                 , the Property of
>                 <persName id="t17280717-1-person32">
>                   James
>                       Reeves
>                   <interp value="Reeves" type="surname" inst="t17280717-1-person32"/>
>                   <interp value="James" type="given" inst="t17280717-1-person32"/>
>                   <interp value="male" type="gender" inst="t17280717-1-person32"/>
>                 </persName>
>                 aforesaid.
>               </p>
>               <p>
>                 <persName id="t17280717-1-person33">
>                   Elizabeth
>                       Reeves
>                   <interp value="Reeves" type="surname" inst="t17280717-1-person33"/>
>                   <interp value="Elizabeth" type="given" inst="t17280717-1-person33"/>
>                   <interp value="female" type="gender" inst="t17280717-1-person33"/>
>                 </persName>
>                 depos'd, That the Prisoner was a Lodger at her House on
>                 <placeName id="t17280717-1-crimeloc4">Addle-Hill</placeName>
>                 <interp value="Addle-Hill" type="placeName" inst="t17280717-1-crimeloc4"/>
>                 <interp value="crimeLocation" type="type" inst="t17280717-1-crimeloc4"/>
>                 <join
>                 targets="t17280717-1-off2 t17280717-1-crimeloc4" targOrder="Y" result="offencePlace">
>     </join>
>                 , near Doctors Commons, when this Robbery was committed, and that it being on the Sabbath Day, she desired the Prisoner, if he did not go abroad, to have an Eye to her Room, which she locked up, and which, he promised to have an Eye to; but when she came home, the Chamber Door and the Corner-Cupboard had been forced open, which appeared by the Mark of an Instrument, the Hinges tore off and the Money gone, the Drawers rifled, and the Plate taken out, though some of the Drawers, out of which the Plate was taken, she left lock'd when she went from Home; upon which she cried out, saying, she was robb'd, and the Prisoner's Wife being above Stairs, came down, and said, her Husband was gone out, that he had been guilty of Failings, and desired her to be easy and she would Work early and late to make Satisfaction, though she did not know he had taken the Things, but his not coming home again confirmed them the more in this Suspicion; and there were other Witnesses, who depos'd, That the Prosecutor left the Prisoner in Care of her Door, and that he promised to look after it.
>               </p>
>               <p>
>                 The Prisoner said in his Defence, That Mrs. Reeves had lost a pair of Silver Buckles out of her Drawers a Fortnight before this Robbery, and she said, she did believe it was done by a Char-woman that she employ'd, which Mrs. Reeves acknowledging, and the Prisoner telling a plausible Story of his being in bad Circumstances, and that Day the Robbery was committed, one Diston pittying his Case, lent him Money to go down to Bristol, to Trade there, if possible to retrieve himself, and he not daring to go out of Doors in the Week Days, went out of the House unfortunately on that Afternoon: There being no positive Evidence against him, the Jury
>                 <rs type="verdictDescription" id="t17280717-1-verdict5">
>                   <interp value="notGuilty" type="verdictCategory" inst="t17280717-1-verdict5"/>
>                   acquitted
>                 </rs>
>                 him.
>               </p>
>             </div1>
>             <div1 id="t17280717-2" type="trialAccount">
>               <interp value="BAILEY" type="collection" inst="t17280717-2"/>
>               <interp value="1728" type="year" inst="t17280717-2"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-2"/>
>               <interp value="17280717" type="date" inst="t17280717-2"/>
>               <join
>               targets="t17280717-2-defend35 t17280717-2-off6 t17280717-2-verdict7" targOrder="Y" id="t17280717-2-off6-c33" result="criminalCharge">
>     </join>
>               <p>
>                 <persName type="defendantName" id="t17280717-2-defend35">
>                   David
>                          Ball
>                   <interp value="Ball" type="surname" inst="t17280717-2-defend35"/>
>                   <interp value="David" type="given" inst="t17280717-2-defend35"/>
>                   <interp value="male" type="gender" inst="t17280717-2-defend35"/>
>                 </persName>
>                 was indicted for
>                 <rs type="offenceDescription" id="t17280717-2-off6">
>                   <interp value="theft" type="offenceCategory" inst="t17280717-2-off6"/>
>                   <interp value="pettyLarceny" type="offenceSubcategory" inst="t17280717-2-off6">
>                   </interp>
>                   a Petty Larceny, in stealing a Yard and a Quarter of Linnen Cloth, value 11 d.
>                 </rs>
>                 the Goods of
>                 <persName type="victimName" id="t17280717-2-victim37">
>                   John
>                          Williams
>                   <interp value="Williams" type="surname" inst="t17280717-2-victim37"/>
>                   <interp value="John" type="given" inst="t17280717-2-victim37"/>
>                   <interp value="male" type="gender" inst="t17280717-2-victim37"/>
>                 </persName>
>                 and
>                 <persName type="victimName" id="t17280717-2-victim39">
>                   William
>                          Williams
>                   <interp value="Williams" type="surname" inst="t17280717-2-victim39"/>
>                   <interp value="William" type="given" inst="t17280717-2-victim39"/>
>                   <interp value="male" type="gender" inst="t17280717-2-victim39"/>
>                 </persName>
>                 ; to which Indictment he
>                 <rs type="verdictDescription" id="t17280717-2-verdict7">
>                   <interp value="guilty" type="verdictCategory" inst="t17280717-2-verdict7"/>
>                   <interp
>                   value="pleadedGuilty" type="verdictSubcategory" inst="t17280717-2-verdict7">
>     </interp>
>                   pleaded Guilty
>                 </rs>
>                 .
>               </p>
>               <p>
>                 <rs type="punishmentDescription" id="t17280717-2-punish8">
>                   <interp value="transport" type="punishmentCategory" inst="t17280717-2-punish8">
>                   </interp>
>                   <join
>                   targets="t17280717-2-defend35 t17280717-2-punish8" targOrder="Y" result="defendantPunishment">
>     </join>
>                   <note>[Transportation. See summary.]</note>
>                 </rs>
>               </p>
>             </div1>
>             <div1 id="t17280717-3" type="trialAccount">
>               <interp value="BAILEY" type="collection" inst="t17280717-3"/>
>               <interp value="1728" type="year" inst="t17280717-3"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-3"/>
>               <interp value="17280717" type="date" inst="t17280717-3"/>
>               <join
>               targets="t17280717-3-defend40 t17280717-3-off10 t17280717-3-verdict11" targOrder="Y" id="t17280717-3-off10-c36" result="criminalCharge">
>     </join>
>               <p>
>                 <xptr doc="172807170002" type="pageFacsimile"/>
>                 <persName type="defendantName" id="t17280717-3-defend40">
>                   Nathaniel
>                       Mercy
>                   <interp value="Mercy" type="surname" inst="t17280717-3-defend40"/>
>                   <interp value="Nathaniel" type="given" inst="t17280717-3-defend40"/>
>                   <interp value="male" type="gender" inst="t17280717-3-defend40"/>
>                 </persName>
>                 , of
>                 <placeName id="t17280717-3-defloc9">St. James's Westminster</placeName>
>                 <interp
>                 value="St. James's Westminster" type="placeName" inst="t17280717-3-defloc9">
>     </interp>
>                 <interp value="defendantHome" type="type" inst="t17280717-3-defloc9"/>
>                 <join
>                 targets="t17280717-3-defend40 t17280717-3-defloc9" targOrder="Y" result="persNamePlace">
>     </join>
>                 , was indicted for
>                 <rs type="offenceDescription" id="t17280717-3-off10">
>                   <interp value="theft" type="offenceCategory" inst="t17280717-3-off10"/>
>                   <interp
>                   value="grandLarceny" type="offenceSubcategory" inst="t17280717-3-off10">
>     </interp>
>                   stealing a Coach Wheel, value 12 Shillings
>                 </rs>
>                 , the Goods of
>                 <persName type="victimName" id="t17280717-3-victim41">
>                   Thomas
>                       West
>                   <interp value="West" type="surname" inst="t17280717-3-victim41"/>
>                   <interp value="Thomas" type="given" inst="t17280717-3-victim41"/>
>                   <interp value="male" type="gender" inst="t17280717-3-victim41"/>
>                   <join
>                   targets="t17280717-3-off10 t17280717-3-victim41" targOrder="Y" result="offenceVictim">
>     </join>
>                 </persName>
>                 , on the 9th of this Instant, to which Indictment he
>                 <rs type="verdictDescription" id="t17280717-3-verdict11">
>                   <interp value="guilty" type="verdictCategory" inst="t17280717-3-verdict11"/>
>                   <interp
>                   value="pleadedGuilty" type="verdictSubcategory" inst="t17280717-3-verdict11">
>     </interp>
>                   pleaded guilty
>                 </rs>
>                 .
>               </p>
>               <p>
>                 <rs type="punishmentDescription" id="t17280717-3-punish12">
>                   <interp
>                   value="transport" type="punishmentCategory" inst="t17280717-3-punish12">
>     </interp>
>                   <join
>                   targets="t17280717-3-defend40 t17280717-3-punish12" targOrder="Y" result="defendantPunishment">
>     </join>
>                   <note>[Transportation. See summary.]</note>
>                 </rs>
>               </p>
>             </div1>
>             <div1 id="t17280717-4" type="trialAccount">
>               <interp value="BAILEY" type="collection" inst="t17280717-4"/>
>               <interp value="1728" type="year" inst="t17280717-4"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-4"/>
>               <interp value="17280717" type="date" inst="t17280717-4"/>
>               <join
>               targets="t17280717-4-defend42 t17280717-4-off14 t17280717-4-verdict17" targOrder="Y" id="t17280717-4-off14-c38" result="criminalCharge">
>     </join>
>               <p>
>                 <persName type="defendantName" id="t17280717-4-defend42">
>                   Margaret
>                       King
>                   <interp value="King" type="surname" inst="t17280717-4-defend42"/>
>                   <interp value="Margaret" type="given" inst="t17280717-4-defend42"/>
>                   <interp value="female" type="gender" inst="t17280717-4-defend42"/>
>                 </persName>
>                 , of
>                 <placeName id="t17280717-4-defloc13">St. Ann's Westminster</placeName>
>                 <interp
>                 value="St. Ann's Westminster" type="placeName" inst="t17280717-4-defloc13">
>     </interp>
>                 <interp value="defendantHome" type="type" inst="t17280717-4-defloc13"/>
>                 <join
>                 targets="t17280717-4-defend42 t17280717-4-defloc13" targOrder="Y" result="persNamePlace">
>     </join>
>                 , was indicted for
>                 <rs type="offenceDescription" id="t17280717-4-off14">
>                   <interp value="theft" type="offenceCategory" inst="t17280717-4-off14"/>
>                   <interp
>                   value="grandLarceny" type="offenceSubcategory" inst="t17280717-4-off14">
>     </interp>
>                   privately stealing a Gold Watch, value 16 l.
>                 </rs>
>                 on the
>                 <rs type="crimeDate" id="t17280717-4-cd15">first of May</rs>
>                 <join
>                 targets="t17280717-4-off14 t17280717-4-cd15" targOrder="Y" result="offenceCrimeDate">
>     </join>
>                 last, the Property of
>                 <persName type="victimName" id="t17280717-4-victim43">
>                   Timothy
>                       Conner
>                   <interp value="Conner" type="surname" inst="t17280717-4-victim43"/>
>                   <interp value="Timothy" type="given" inst="t17280717-4-victim43"/>
>                   <interp value="male" type="gender" inst="t17280717-4-victim43"/>
>                   <join
>                   targets="t17280717-4-off14 t17280717-4-victim43" targOrder="Y" result="offenceVictim">
>     </join>
>                 </persName>
>                 .
>               </p>
>               <p>
>                 The Prosecutor depos'd, That he met the Prisoner in the Street, and ask'd her to drink a Glass of Wine, to which she consented, and they went to the
>                 <placeName id="t17280717-4-crimeloc16">
>                   Swan Tavern in Newport Market
>                 </placeName>
>                 <interp
>                 value="Swan Tavern in Newport Market" type="placeName" inst="t17280717-4-crimeloc16">
>     </interp>
>                 <interp value="crimeLocation" type="type" inst="t17280717-4-crimeloc16"/>
>                 <join
>                 targets="t17280717-4-off14 t17280717-4-crimeloc16" targOrder="Y" result="offencePlace">
>     </join>
>                 , and lovingly drank 4 Pints, the Prisoner asking him what it was a Clock, he pulled out his Gold Watch, and bid her look; that she took it in her Hand, but could not remember that she returned it again, neither could she say positively, that she had it, but as he mis'd it as soon as he parted with her he thought he had occasion of Suspicion; yet, said he, I had been drinking before, (tho' it was but Nine in the Morning)and can't tell directly how the Matter stood; which being all the Evidence he could give against her, she was
>                 <rs type="verdictDescription" id="t17280717-4-verdict17">
>                   <interp value="notGuilty" type="verdictCategory" inst="t17280717-4-verdict17"/>
>                   acquitted
>                 </rs>
>                 .
>               </p>
>             </div1>
>             <div1 id="t17280717-5" type="trialAccount">
>               <interp value="BAILEY" type="collection" inst="t17280717-5"/>
>               <interp value="1728" type="year" inst="t17280717-5"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-5"/>
>               <interp value="17280717" type="date" inst="t17280717-5"/>
>               <join
>               targets="t17280717-5-defend44 t17280717-5-off19 t17280717-5-verdict22" targOrder="Y" id="t17280717-5-off19-c40" result="criminalCharge">
>     </join>
>               <p>
>                 <persName type="defendantName" id="t17280717-5-defend44">
>                   Elizabeth
>                       Mould
>                   <interp value="Mould" type="surname" inst="t17280717-5-defend44"/>
>                   <interp value="Elizabeth" type="given" inst="t17280717-5-defend44"/>
>                   <interp value="female" type="gender" inst="t17280717-5-defend44"/>
>                 </persName>
>                 , of
>                 <placeName id="t17280717-5-defloc18">St. Martins in the Fields</placeName>
>                 <interp
>                 value="St. Martins in the Fields" type="placeName" inst="t17280717-5-defloc18">
>     </interp>
>                 <interp value="defendantHome" type="type" inst="t17280717-5-defloc18"/>
>                 <join
>                 targets="t17280717-5-defend44 t17280717-5-defloc18" targOrder="Y" result="persNamePlace">
>     </join>
>                 , was indicted for
>                 <rs type="offenceDescription" id="t17280717-5-off19">
>                   <interp value="theft" type="offenceCategory" inst="t17280717-5-off19"/>
>                   <interp
>                   value="pocketpicking" type="offenceSubcategory" inst="t17280717-5-off19">
>     </interp>
>                   privately stealing Fifty Pounds and eight Shillings, from the Person of
>                   <persName type="victimName" id="t17280717-5-victim45">
>                     John
>                             Coxall
>                     <interp value="Coxall" type="surname" inst="t17280717-5-victim45"/>
>                     <interp value="John" type="given" inst="t17280717-5-victim45"/>
>                     <interp value="male" type="gender" inst="t17280717-5-victim45"/>
>                     <join
>                     targets="t17280717-5-off19 t17280717-5-victim45" targOrder="Y" result="offenceVictim">
>     </join>
>                   </persName>
>                 </rs>
>                 , on the
>                 <rs type="crimeDate" id="t17280717-5-cd20">24th of June</rs>
>                 <join
>                 targets="t17280717-5-off19 t17280717-5-cd20" targOrder="Y" result="offenceCrimeDate">
>     </join>
>                 last
>               </p>
>               <p>
>                 The Prosecutor depos'd, That he being a
>                 <rs type="occupation" id="t17280717-5-viclabel21">Bricklayer</rs>
>                 <join
>                 targets="t17280717-5-victim45 t17280717-5-viclabel21" targOrder="Y" result="persNameOccupation">
>     </join>
>                 , was endeavouring to get some Business to do, at the House where the Prisoner lived, and it being a Chandler's Shop, to obtain the Good-Will of the People, he call'd for several Drams, and treated the Mistress of the House and one Mrs. Greaves, who was his Customer, with Cyder, Brandy, &amp;c. that whilst they were drinking the Prisoner came into the Room, and he likewise treated her, and she, in return, wip'd him over the Face, and seem'd very fond of him, saying, she would give him a fine Nosegay, and something to cheer his Heart, if he would go with her to Covent-Garden Market, and then (as his Expression was) she weagled him down into the Cellar, and there kept him lock'd up in a back Passage, that he was very much in Liquor, and scarce sensible of what he did, but he found what she had done to his Sorrow, for she had taken all his Money, which he missing, made a Noise, and the People of the House hearing him, let him out of his Place of Confinement, by conducting him up the Back Stairs; that he got Officers and search'd the Cellar, but could not find the Prisoner. The Prisoner desiring he might be ask'd if they did not drink together in the Cellar, the Prosecutor answer'd, No, she would fetch no Drink, saying, she did not care to be seen by the Publicans; that she was all for dry Money, and she, and None but she, had his Fifty Pounds, for save only him and her, there were neither Man, Woman, nor Child, nor Dog nor Cat in the Cellar.
>               </p>
>               <p>
>                 <persName id="t17280717-5-person46">
>                   Elizabeth
>                       Harper
>                   <interp value="Harper" type="surname" inst="t17280717-5-person46"/>
>                   <interp value="Elizabeth" type="given" inst="t17280717-5-person46"/>
>                   <interp value="female" type="gender" inst="t17280717-5-person46"/>
>                 </persName>
>                 depos'd, That the Prosecutor came into her Shop, and drank Cyder, Usquebaugh and Brandy, and being disguis'd in Liquor he pull'd out eight or ten Guineas and said, he was no Scoundrel; that she and Mrs. Greaves, whom he brought in to treat, begg'd he would put up his Money and take Care of it, and about that Time the Prisoner came into the Room, and familiarly stroaking his cheeks, persuaded him to go into the Cellar, saying, he should pay his Footing, that they staid half an Hour below, and this Deponent looking down, saw the Prisoner's Mother there, that he came up with her again, and treated her with Usquebaugh, and at 11 o' Clock, which was several Hours after, they heard him in a back Passage, where the House was supplied with Water, and letting him into their House, he said he was robb'd, at which this Deponent's Husband thrust him out of doors.
>               </p>
>               <p>
>                 <persName id="t17280717-5-person47">
>                   Isabella
>                       Greaves
>                   <interp value="Greaves" type="surname" inst="t17280717-5-person47"/>
>                   <interp value="Isabella" type="given" inst="t17280717-5-person47"/>
>                   <interp value="female" type="gender" inst="t17280717-5-person47"/>
>                 </persName>
>                 likewise confirm'd every Part of this Deposition, adding, That the Prisoner was not to be seen that Night after the Robbery.
>               </p>
>               <p>
>                 The Prisoner said in her Defence, That the Prosecutor went down into her Cellar, and behaved himself so rudely, that she was forced to threaten to send for an Officer, that she knew nothing of his Money, and had not gone away that Night, but as she was oblig'd by her Husband, and that she came next Morning and set her Greens out.
>               </p>
>               <p>
>                 <persName id="t17280717-5-person48">
>                   Anthony
>                       Dyer
>                   <interp value="Dyer" type="surname" inst="t17280717-5-person48"/>
>                   <interp value="Anthony" type="given" inst="t17280717-5-person48"/>
>                   <interp value="male" type="gender" inst="t17280717-5-person48"/>
>                 </persName>
>                 depos'd, That the Prosecutor told him he had lost his Money being Drunk, that he had been in a Cellar and in a Vault, where he fell asleep, and gave a very odd account of the Adventure.
>               </p>
>               <p>
>                 <persName id="t17280717-5-person49">
>                   George
>                       Smith
>                   <interp value="Smith" type="surname" inst="t17280717-5-person49"/>
>                   <interp value="George" type="given" inst="t17280717-5-person49"/>
>                   <interp value="male" type="gender" inst="t17280717-5-person49"/>
>                 </persName>
>                 depos'd, That the Prosecutor said he fell asleep upon a Vault, and could charge no Body.
>               </p>
>               <p>
>                 The Constable, the Watchman, and others severally depos'd, That the Morning after this happened, he said he could charge no particular person, but he would indict the House. The Prisoner having a very good Character from several reputable Witnesses, the Jury
>                 <rs type="verdictDescription" id="t17280717-5-verdict22">
>                   <interp value="notGuilty" type="verdictCategory" inst="t17280717-5-verdict22"/>
>                   acquitted
>                 </rs>
>                 her.
>               </p>
>             </div1>
>             <div1 id="t17280717-6" type="trialAccount">
>               <interp value="BAILEY" type="collection" inst="t17280717-6"/>
>               <interp value="1728" type="year" inst="t17280717-6"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-6"/>
>               <interp value="17280717" type="date" inst="t17280717-6"/>
>               <join
>               targets="t17280717-6-defend50 t17280717-6-off24 t17280717-6-verdict26" targOrder="Y" id="t17280717-6-off24-c46" result="criminalCharge">
>     </join>
>               <p>
>                 <persName type="defendantName" id="t17280717-6-defend50">
>                   Phillip
>                       Hilliard
>                   <interp value="Hilliard" type="surname" inst="t17280717-6-defend50"/>
>                   <interp value="Phillip" type="given" inst="t17280717-6-defend50"/>
>                   <interp value="male" type="gender" inst="t17280717-6-defend50"/>
>                 </persName>
>                 , of
>                 <placeName id="t17280717-6-defloc23">St. James's Westminster</placeName>
>                 <interp
>                 value="St. James's Westminster" type="placeName" inst="t17280717-6-defloc23">
>     </interp>
>                 <interp value="defendantHome" type="type" inst="t17280717-6-defloc23"/>
>                 <join
>                 targets="t17280717-6-defend50 t17280717-6-defloc23" targOrder="Y" result="persNamePlace">
>     </join>
>                 , was indicted for
>                 <rs type="offenceDescription" id="t17280717-6-off24">
>                   <interp value="theft" type="offenceCategory" inst="t17280717-6-off24"/>
>                   <interp
>                   value="grandLarceny" type="offenceSubcategory" inst="t17280717-6-off24">
>     </interp>
>                   feloniously stealing a Silver Spoon, value eleven Shillings, the Property of
>                   <persName type="victimName" id="t17280717-6-victim51">
>                     Abraham
>                             Mannio
>                     <interp value="Mannio" type="surname" inst="t17280717-6-victim51"/>
>                     <interp value="Abraham" type="given" inst="t17280717-6-victim51"/>
>                     <interp value="male" type="gender" inst="t17280717-6-victim51"/>
>                     <join
>                     targets="t17280717-6-off24 t17280717-6-victim51" targOrder="Y" result="offenceVictim">
>     </join>
>                   </persName>
>                   , on the
>                   <rs type="crimeDate" id="t17280717-6-cd25">27th of June</rs>
>                   <join
>                   targets="t17280717-6-off24 t17280717-6-cd25" targOrder="Y" result="offenceCrimeDate">
>     </join>
>                   last.
>                 </rs>
>               </p>
>               <p>
>                 Mr. Hayden depos'd, That the Prisoner brought the Spoon to him to pawn, and he suspecting it to be stolen, stopp'd him and carried him to the Round-House, where he confess'd he stole it at the Prosecutor's, he being invited there to Dinner with some Gentlemans Servants.
>               </p>
>               <p>
>                 <persName id="t17280717-6-person52">
>                   Robert
>                       Amey
>                   <interp value="Amey" type="surname" inst="t17280717-6-person52"/>
>                   <interp value="Robert" type="given" inst="t17280717-6-person52"/>
>                   <interp value="male" type="gender" inst="t17280717-6-person52"/>
>                 </persName>
>                 depos'd, That he attended the Gentlemens Servants at Dinner, and afterwards they miss'd a Spoon, that Mr. Haydon sent them word of the Spoon brought to him by the Prisoner, and he going to match it by the others, found it to be the same which they had lost, it being of the same Make and Mark: The Fact being thus plainly proved upon him, the Jury found him
>                 <rs type="verdictDescription" id="t17280717-6-verdict26">
>                   <interp value="guilty" type="verdictCategory" inst="t17280717-6-verdict26"/>
>                   <interp
>                   value="theftunder1s" type="verdictSubcategory" inst="t17280717-6-verdict26">
>     </interp>
>                   guilty to the value of 10d.
>                 </rs>
>               </p>
>               <p>
>                 <rs type="punishmentDescription" id="t17280717-6-punish27">
>                   <interp
>                   value="transport" type="punishmentCategory" inst="t17280717-6-punish27">
>     </interp>
>                   <join
>                   targets="t17280717-6-defend50 t17280717-6-punish27" targOrder="Y" result="defendantPunishment">
>     </join>
>                   <note>[Transportation. See summary.]</note>
>                 </rs>
>               </p>
>             </div1>
>             <div1 id="t17280717-7" type="trialAccount">
>               <interp value="BAILEY" type="collection" inst="t17280717-7"/>
>               <interp value="1728" type="year" inst="t17280717-7"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-7"/>
>               <interp value="17280717" type="date" inst="t17280717-7"/>
>               <join
>               targets="t17280717-7-defend53 t17280717-7-off29 t17280717-7-verdict31" targOrder="Y" id="t17280717-7-off29-c49" result="criminalCharge">
>     </join>
>               <p>
>                 <persName type="defendantName" id="t17280717-7-defend53">
>                   Robert
>                       Ashby
>                   <interp value="Ashby" type="surname" inst="t17280717-7-defend53"/>
>                   <interp value="Robert" type="given" inst="t17280717-7-defend53"/>
>                   <interp value="male" type="gender" inst="t17280717-7-defend53"/>
>                 </persName>
>                 , of
>                 <rs type="occupation" id="t17280717-7-deflabel28">St. Andrew's Holborn</rs>
>                 <join
>                 targets="t17280717-7-defend53 t17280717-7-deflabel28" targOrder="Y" result="persNameOccupation">
>     </join>
>                 , was indicted for
>                 <rs type="offenceDescription" id="t17280717-7-off29">
>                   <interp value="theft" type="offenceCategory" inst="t17280717-7-off29"/>
>                   <interp
>                   value="grandLarceny" type="offenceSubcategory" inst="t17280717-7-off29">
>     </interp>
>                   stealing a Gold Watch, value 16 l. a Chain, value 4 l. a Cornelian Seal set in Gold, value 10s. the Property of
>                   <persName type="victimName" id="t17280717-7-victim54">
>                     Nicholas
>                             Roper
>                     <interp value="Roper" type="surname" inst="t17280717-7-victim54"/>
>                     <interp value="Nicholas" type="given" inst="t17280717-7-victim54"/>
>                     <interp value="male" type="gender" inst="t17280717-7-victim54"/>
>                   </persName>
>                   from the Person of
>                   <persName type="victimName" id="t17280717-7-victim55">
>                     Phoebe
>                             Thickpenny
>                     <interp value="Thickpenny" type="surname" inst="t17280717-7-victim55"/>
>                     <interp value="Phoebe" type="given" inst="t17280717-7-victim55"/>
>                     <interp value="female" type="gender" inst="t17280717-7-victim55"/>
>                   </persName>
>                 </rs>
>                 , who depos'd, That her Mistress being at Little Chelsea, sent her to their House in Lad-lane, for the Watch and other Things she had occasion for, that she call'd at the Prisoner's in Castle Yard, Chick-Lane, as she was going towards Chelsea with the Watch and the Bundle, where she drank part of a Pint of Beer, some Tea 2 Quarterns of Brandy, and a Bottle of Cyder; that the Prisoner would go part of the Way home with her, and in
>                 <placeName id="t17280717-7-crimeloc30">Leather-Lane</placeName>
>                 <interp value="Leather-Lane" type="placeName" inst="t17280717-7-crimeloc30"/>
>                 <interp value="crimeLocation" type="type" inst="t17280717-7-crimeloc30"/>
>                 <join
>                 targets="t17280717-7-off29 t17280717-7-crimeloc30" targOrder="Y" result="offencePlace">
>     </join>
>                 , he said, faith they would not part Dry lips, and accordingly they went into a publick House and drank a Pint of Twopenny, and two Quarterns of Brandy, that she had the Watch then, and at the Door the Prisoner kiss'd her, and gave her a shilling for a Coach, she having out-staid her Time; that when he kiss'd her, he put one Hand around her Waist, but what he did with the other she could not tell, that she then cross'd the Way to another House, and immediately miss'd the Watch, and she was sure she had pinn'd it so to her Side, that she could not drop it.
>               </p>
>               <p>
>                 <persName id="t17280717-7-person56">
>                   Margaret
>                       Nelson
>                   <interp value="Nelson" type="surname" inst="t17280717-7-person56"/>
>                   <interp value="Margaret" type="given" inst="t17280717-7-person56"/>
>                   <interp value="female" type="gender" inst="t17280717-7-person56"/>
>                 </persName>
>                 depos'd, That the Prisoner and Phoebe Thickpenny, came to her House in Leather-Lane, and he call'd for a private Room, to which the Girl would not consent, that the Girl wanted to go to, &amp;c. and she went with her, when the Girl said, she had her Mistress's Gold Watch, and seeming to look on it, told her it was 7 o' Clock, but this Deponent did not see the Watch, yet she said, she did verily believe she heard it beat, that they soon parted, and in a Quarter of an Hour the maid returned, and said she had lost the Watch.
>               </p>
>               <p>
>                 The Prisoner said in his Defence, That he knew nothing of it, any further than she said it was her Mistress's, that they parted very good Friends, and she having been an old Sweetheart of his, laid her Head upon his Shoulder, and said, She could live and die there, which would have been no little Aggravation to his Crime, had he wrong'd so good Natur'd a Creature: But the Evidence against him being weak, and several appearing to his Character, the Jury
>                 <rs type="verdictDescription" id="t17280717-7-verdict31">
>                   <interp value="notGuilty" type="verdictCategory" inst="t17280717-7-verdict31"/>
>                   acquitted
>                 </rs>
>                 him.
>               </p>
>             </div1>
>             <div1 id="t17280717-8" type="trialAccount">
>               <interp value="BAILEY" type="collection" inst="t17280717-8"/>
>               <interp value="1728" type="year" inst="t17280717-8"/>
>               <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-8"/>
>               <interp value="17280717" type="date" inst="t17280717-8"/>
>               <join
>               targets="t17280717-8-defend57 t17280717-8-off33 t17280717-8-verdict35" targOrder="Y" id="t17280717-8-off33-c53" result="criminalCharge">
>     </join>
>               <join
>               targets="t17280717-8-defend58 t17280717-8-off33 t17280717-8-verdict35" targOrder="Y" id="t17280717-8-off33-c54" result="criminalCharge">
>     </join>
>               <p>
>                 <persName type="defendantName" id="t17280717-8-defend57">
>                   Joseph
>                       Plummer
>                   <interp value="Plummer" type="surname" inst="t17280717-8-defend57"/>
>                   <interp value="Joseph" type="given" inst="t17280717-8-defend57"/>
>                   <interp value="male" type="gender" inst="t17280717-8-defend57"/>
>                 </persName>
>                 , and
>                 <persName type="defendantName" id="t17280717-8-defend58">
>                   Henry
>                       Coleman
>                   <interp value="Coleman" type="surname" inst="t17280717-8-defend58"/>
>                   <interp value="Henry" type="given" inst="t17280717-8-defend58"/>
>                   <interp value="male" type="gender" inst="t17280717-8-defend58"/>
>                 </persName>
>                 , of
>                 <placeName id="t17280717-8-defloc32">Edmonton</placeName>
>                 <interp value="Edmonton" type="placeName" inst="t17280717-8-defloc32"/>
>                 <interp value="defendantHome" type="type" inst="t17280717-8-defloc32"/>
>                 <join
>                 targets="t17280717-8-defend57 t17280717-8-defloc32" targOrder="Y" result="persNamePlace">
>     </join>
>                 <join
>                 targets="t17280717-8-defend58 t17280717-8-defloc32" targOrder="Y" result="persNamePlace">
>     </join>
>                 , were indicted for
>                 <rs type="offenceDescription" id="t17280717-8-off33">
>                   <interp value="theft" type="offenceCategory" inst="t17280717-8-off33"/>
>                   <interp
>                   value="pettyLarceny" type="offenceSubcategory" inst="t17280717-8-off33">
>     </interp>
>                   stealing 2 Shirts, value 8d. a Silk Handkerchief, value 1d. and a Pair of Stockings, value 1d.
>       ...

``` scala


%md
### Better examples:

http://alvinalexander.com/scala/how-to-extract-data-from-xml-nodes-in-scala

http://alvinalexander.com/scala/scala-xml-xpath-example

#### More advanced topics:

https://alvinalexander.com/scala/serializing-deserializing-xml-scala-classes

 

#### XML to JSON, if you want to go this route:

https://stackoverflow.com/questions/9516973/xml-to-json-with-scala
```

Our Parsing Problem
-------------------

Let's dive deep on this data right away. See links above to learn xml more systematically to be able to parse other subsets of the data for your own project.

For now, we will jump in to parse the input data of counts used in [Jasper Mackenzie, Raazesh Sainudiin, James Smithies and Heather Wolffram, A nonparametric view of the civilizing process in London's Old Bailey, Research Report UCDMS2015/1, 32 pages, 2015](http://lamastex.org/preprints/20150828_civilizingProcOBO.pdf).

``` scala
(elem \\ "div0").map(Node => (Node \ "@type").text) // types of div0 node, the singleton root node for the file
```

>     res48: scala.collection.immutable.Seq[String] = List(sessionsPaper)

``` scala
(elem \\ "div1").map(Node => (Node \ "@type").text) // types of div1 node
```

>     res49: scala.collection.immutable.Seq[String] = List(frontMatter, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, trialAccount, supplementaryMaterial)

``` scala
(elem \\ "div1")
```

>     res50: scala.xml.NodeSeq =
>     NodeSeq(<div1 id="f17280717-1" type="frontMatter">
>                    <interp value="BAILEY" type="collection" inst="f17280717-1"/>
>                    <interp value="1728" type="year" inst="f17280717-1"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="f17280717-1"/>
>                    <interp value="17280717" type="date" inst="f17280717-1"/>
>
>                    <xptr doc="172807170001" type="pageFacsimile"/>THE PROCEEDINGS AT THE Sessions of the Peace, and Oyer and Terminer for the City of LONDON: AND
>              <p>On the King's Commission of Goal-Delivery of Newgate, held at Justice-Hall in the Old Baily, for the CITY of LONDON and COUNTY of MIDDLESEX.</p>
>                    <p>On Wednesday, Thursday, and Friday, being the 17th, 18th, and 19th of July, 1728, in the Second Year of His MAJESTY's Reign.</p>
>                    <p>(Price Six Pence)</p>
>                    <p>BEFORE the Right Honourable Sir
>                       <persName type="judiciaryName" id="f17280717-1-person1">
>                          EDWARD
>                          BECHER
>                       <interp value="BECHER" type="surname" inst="f17280717-1-person1"/>
>                          <interp value="EDWARD" type="given" inst="f17280717-1-person1"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person1"/>
>                       </persName>
>                    , Lord Mayor of the City of London; the Right Honourable the Lord Chief
>                       <persName type="judiciaryName" id="f17280717-1-person2">
>                          Baron
>                          Pengelly
>                       <interp value="Pengelly" type="surname" inst="f17280717-1-person2"/>
>                          <interp value="Baron" type="given" inst="f17280717-1-person2"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person2"/>
>                       </persName>
>                    ; Mr. Justice Reynolds; Mr.
>                       <persName type="judiciaryName" id="f17280717-1-person3">
>                          Baron
>                          Thompson
>                       <interp value="Thompson" type="surname" inst="f17280717-1-person3"/>
>                          <interp value="Baron" type="given" inst="f17280717-1-person3"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person3"/>
>                       </persName>
>                    , Recorder of the City of London; and
>                       <persName type="judiciaryName" id="f17280717-1-person4">
>                          John
>                          Raby
>                       <interp value="Raby" type="surname" inst="f17280717-1-person4"/>
>                          <interp value="John" type="given" inst="f17280717-1-person4"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person4"/>
>                       </persName>
>                    , Esq; Serjeant at Law; and other His Majesty's Justices of Goal-Delivery, and Oyer and Terminer aforesaid; Together with several of His Majesty's Justices of the Peace for the said City of London.</p>
>
>                    <p>London Jury.</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person5">
>                          John
>                          Land
>                       <interp value="Land" type="surname" inst="f17280717-1-person5"/>
>                          <interp value="John" type="given" inst="f17280717-1-person5"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person5"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person6">
>                          Nathaniel
>                          Mason
>                       <interp value="Mason" type="surname" inst="f17280717-1-person6"/>
>                          <interp value="Nathaniel" type="given" inst="f17280717-1-person6"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person6"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person7">
>                          Benjamin
>                          Allibone
>                       <interp value="Allibone" type="surname" inst="f17280717-1-person7"/>
>                          <interp value="Benjamin" type="given" inst="f17280717-1-person7"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person7"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person8">
>                          Joseph
>                          Westwood
>                       <interp value="Westwood" type="surname" inst="f17280717-1-person8"/>
>                          <interp value="Joseph" type="given" inst="f17280717-1-person8"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person8"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person9">
>                          Gabriel
>                          Wittacre
>                       <interp value="Wittacre" type="surname" inst="f17280717-1-person9"/>
>                          <interp value="Gabriel" type="given" inst="f17280717-1-person9"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person9"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person10">
>                          Samuel
>                          Tilley
>                       <interp value="Tilley" type="surname" inst="f17280717-1-person10"/>
>                          <interp value="Samuel" type="given" inst="f17280717-1-person10"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person10"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person11">
>                          Robert
>                          Lathwell
>                       <interp value="Lathwell" type="surname" inst="f17280717-1-person11"/>
>                          <interp value="Robert" type="given" inst="f17280717-1-person11"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person11"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person12">
>                          Edward
>                          Newman
>                       <interp value="Newman" type="surname" inst="f17280717-1-person12"/>
>                          <interp value="Edward" type="given" inst="f17280717-1-person12"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person12"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person13">
>                          Nathaniel
>                          Pickering
>                       <interp value="Pickering" type="surname" inst="f17280717-1-person13"/>
>                          <interp value="Nathaniel" type="given" inst="f17280717-1-person13"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person13"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person14">
>                          Simon
>                          Tunks
>                       <interp value="Tunks" type="surname" inst="f17280717-1-person14"/>
>                          <interp value="Simon" type="given" inst="f17280717-1-person14"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person14"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person15">
>                          Thomas
>                          Maud
>                       <interp value="Maud" type="surname" inst="f17280717-1-person15"/>
>                          <interp value="Thomas" type="given" inst="f17280717-1-person15"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person15"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person16">
>                          John
>                          Bond
>                       <interp value="Bond" type="surname" inst="f17280717-1-person16"/>
>                          <interp value="John" type="given" inst="f17280717-1-person16"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person16"/>
>                       </persName>
>                    .</p>
>
>
>                    <p>Middlesex Jury.</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person17">
>                          Elisha
>                          Impey
>                       <interp value="Impey" type="surname" inst="f17280717-1-person17"/>
>                          <interp value="Elisha" type="given" inst="f17280717-1-person17"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person17"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person18">
>                          Christopher
>                          Harris
>                       <interp value="Harris" type="surname" inst="f17280717-1-person18"/>
>                          <interp value="Christopher" type="given" inst="f17280717-1-person18"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person18"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person19">
>                          William
>                          Perkins
>                       <interp value="Perkins" type="surname" inst="f17280717-1-person19"/>
>                          <interp value="William" type="given" inst="f17280717-1-person19"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person19"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person20">
>                          Gilbert
>                          Watson
>                       <interp value="Watson" type="surname" inst="f17280717-1-person20"/>
>                          <interp value="Gilbert" type="given" inst="f17280717-1-person20"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person20"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person21">
>                          John
>                          Wells
>                       <interp value="Wells" type="surname" inst="f17280717-1-person21"/>
>                          <interp value="John" type="given" inst="f17280717-1-person21"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person21"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person22">
>                          William
>                          Carpenter
>                       <interp value="Carpenter" type="surname" inst="f17280717-1-person22"/>
>                          <interp value="William" type="given" inst="f17280717-1-person22"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person22"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person23">
>                          Allen
>                          Evans
>                       <interp value="Evans" type="surname" inst="f17280717-1-person23"/>
>                          <interp value="Allen" type="given" inst="f17280717-1-person23"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person23"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person24">
>                          Henry
>                          Cowmbe
>                       <interp value="Cowmbe" type="surname" inst="f17280717-1-person24"/>
>                          <interp value="Henry" type="given" inst="f17280717-1-person24"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person24"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person25">
>                          Simon
>                          Parsons
>                       <interp value="Parsons" type="surname" inst="f17280717-1-person25"/>
>                          <interp value="Simon" type="given" inst="f17280717-1-person25"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person25"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person26">
>                          George
>                          Gilbert
>                       <interp value="Gilbert" type="surname" inst="f17280717-1-person26"/>
>                          <interp value="George" type="given" inst="f17280717-1-person26"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person26"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person27">
>                          Nicholas
>                          Gardner
>                       <interp value="Gardner" type="surname" inst="f17280717-1-person27"/>
>                          <interp value="Nicholas" type="given" inst="f17280717-1-person27"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person27"/>
>                       </persName>
>                    ,</p>
>                    <p>
>
>                       <persName type="jurorName" id="f17280717-1-person28">
>                          Thomas
>                          Ireland
>                       <interp value="Ireland" type="surname" inst="f17280717-1-person28"/>
>                          <interp value="Thomas" type="given" inst="f17280717-1-person28"/>
>                          <interp value="male" type="gender" inst="f17280717-1-person28"/>
>                       </persName>
>                    .</p>
>
>                 </div1>, <div1 id="t17280717-1" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-1"/>
>                    <interp value="1728" type="year" inst="t17280717-1"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-1"/>
>                    <interp value="17280717" type="date" inst="t17280717-1"/>
>                    <join targets="t17280717-1-defend29 t17280717-1-off2 t17280717-1-verdict5" targOrder="Y" id="t17280717-1-off2-c29" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-1-defend29">
>                       James
>                       Haddock
>                    <interp value="Haddock" type="surname" inst="t17280717-1-defend29"/>
>                          <interp value="James" type="given" inst="t17280717-1-defend29"/>
>                          <interp value="male" type="gender" inst="t17280717-1-defend29"/>
>                       </persName>
>                 , of <placeName id="t17280717-1-defloc1">St. Bennet's Paul's Wharf</placeName>
>                       <interp value="St. Bennet's Paul's Wharf" type="placeName" inst="t17280717-1-defloc1"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-1-defloc1"/>
>                       <join targets="t17280717-1-defend29 t17280717-1-defloc1" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-1-off2">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-1-off2"/>
>                          <interp value="theftFromPlace" type="offenceSubcategory" inst="t17280717-1-off2"/>
>                    feloniously stealing 2 Guineas, and 5 l. 16 s. in Silver, a Silver Cup, a Silver Cork Screw, 2 Silver Spoons, and a Nutmeg-grater, in the Dwelling-House of
>
>                             <persName type="victimName" id="t17280717-1-victim31">
>                                James
>                                Reeves
>                             <interp value="Reeves" type="surname" inst="t17280717-1-victim31"/>
>                             <interp value="James" type="given" inst="t17280717-1-victim31"/>
>                             <interp value="male" type="gender" inst="t17280717-1-victim31"/>
>                             <join targets="t17280717-1-off2 t17280717-1-victim31" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>
>
>
>                       </rs>, on the <rs type="crimeDate" id="t17280717-1-cd3">9th of April, and in the 13th Year of his late Majesty King George the First</rs>
>                       <join targets="t17280717-1-off2 t17280717-1-cd3" targOrder="Y" result="offenceCrimeDate"/>, the Property of
>                    <persName id="t17280717-1-person32">
>                       James
>                       Reeves
>                    <interp value="Reeves" type="surname" inst="t17280717-1-person32"/>
>                          <interp value="James" type="given" inst="t17280717-1-person32"/>
>                          <interp value="male" type="gender" inst="t17280717-1-person32"/>
>                       </persName>
>                  aforesaid.</p>
>                    <p>
>
>                       <persName id="t17280717-1-person33">
>                       Elizabeth
>                       Reeves
>                    <interp value="Reeves" type="surname" inst="t17280717-1-person33"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-1-person33"/>
>                          <interp value="female" type="gender" inst="t17280717-1-person33"/>
>                       </persName>
>                  depos'd, That the Prisoner was a Lodger at her House on <placeName id="t17280717-1-crimeloc4">Addle-Hill</placeName>
>                       <interp value="Addle-Hill" type="placeName" inst="t17280717-1-crimeloc4"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-1-crimeloc4"/>
>                       <join targets="t17280717-1-off2 t17280717-1-crimeloc4" targOrder="Y" result="offencePlace"/>, near Doctors Commons, when this Robbery was committed, and that it being on the Sabbath Day, she desired the Prisoner, if he did not go abroad, to have an Eye to her Room, which she locked up, and which, he promised to have an Eye to; but when she came home, the Chamber Door and the Corner-Cupboard had been forced open, which appeared by the Mark of an Instrument, the Hinges tore off and the Money gone, the Drawers rifled, and the Plate taken out, though some of the Drawers, out of which the Plate was taken, she left lock'd when she went from Home; upon which she cried out, saying, she was robb'd, and the Prisoner's Wife being above Stairs, came down, and said, her Husband was gone out, that he had been guilty of Failings, and desired her to be easy and she would Work early and late to make Satisfaction, though she did not know he had taken the Things, but his not coming home again confirmed them the more in this Suspicion; and there were other Witnesses, who depos'd, That the Prosecutor left the Prisoner in Care of her Door, and that he promised to look after it.</p>
>                    <p>The Prisoner said in his Defence, That Mrs. Reeves had lost a pair of Silver Buckles out of her Drawers a Fortnight before this Robbery, and she said, she did believe it was done by a Char-woman that she employ'd, which Mrs. Reeves acknowledging, and the Prisoner telling a plausible Story of his being in bad Circumstances, and that Day the Robbery was committed, one Diston pittying his Case, lent him Money to go down to Bristol, to Trade there, if possible to retrieve himself, and he not daring to go out of Doors in the Week Days, went out of the House unfortunately on that Afternoon: There being no positive Evidence against him, the Jury <rs type="verdictDescription" id="t17280717-1-verdict5">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-1-verdict5"/>
>                    acquitted
>                 </rs> him.</p>
>                 </div1>, <div1 id="t17280717-2" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-2"/>
>                    <interp value="1728" type="year" inst="t17280717-2"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-2"/>
>                    <interp value="17280717" type="date" inst="t17280717-2"/>
>                    <join targets="t17280717-2-defend35 t17280717-2-off6 t17280717-2-verdict7" targOrder="Y" id="t17280717-2-off6-c33" result="criminalCharge"/>
>
>                    <p>
>
>
>                       <persName type="defendantName" id="t17280717-2-defend35">
>                          David
>                          Ball
>                       <interp value="Ball" type="surname" inst="t17280717-2-defend35"/>
>                          <interp value="David" type="given" inst="t17280717-2-defend35"/>
>                          <interp value="male" type="gender" inst="t17280717-2-defend35"/>
>                       </persName>
>
>                  was indicted for <rs type="offenceDescription" id="t17280717-2-off6">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-2-off6"/>
>                          <interp value="pettyLarceny" type="offenceSubcategory" inst="t17280717-2-off6"/>
>                    a Petty Larceny, in stealing a Yard and a Quarter of Linnen Cloth, value 11 d.
>                 </rs> the Goods of
>
>                       <persName type="victimName" id="t17280717-2-victim37">
>                          John
>                          Williams
>                       <interp value="Williams" type="surname" inst="t17280717-2-victim37"/>
>                          <interp value="John" type="given" inst="t17280717-2-victim37"/>
>                          <interp value="male" type="gender" inst="t17280717-2-victim37"/>
>                       </persName>
>
>                  and
>
>                       <persName type="victimName" id="t17280717-2-victim39">
>                          William
>                          Williams
>                       <interp value="Williams" type="surname" inst="t17280717-2-victim39"/>
>                          <interp value="William" type="given" inst="t17280717-2-victim39"/>
>                          <interp value="male" type="gender" inst="t17280717-2-victim39"/>
>                       </persName>
>
>                 ; to which Indictment he <rs type="verdictDescription" id="t17280717-2-verdict7">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-2-verdict7"/>
>                          <interp value="pleadedGuilty" type="verdictSubcategory" inst="t17280717-2-verdict7"/>
>                    pleaded Guilty
>                 </rs>.</p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-2-punish8">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-2-punish8"/>
>                          <join targets="t17280717-2-defend35 t17280717-2-punish8" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>, <div1 id="t17280717-3" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-3"/>
>                    <interp value="1728" type="year" inst="t17280717-3"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-3"/>
>                    <interp value="17280717" type="date" inst="t17280717-3"/>
>                    <join targets="t17280717-3-defend40 t17280717-3-off10 t17280717-3-verdict11" targOrder="Y" id="t17280717-3-off10-c36" result="criminalCharge"/>
>
>                    <p>
>                       <xptr doc="172807170002" type="pageFacsimile"/>
>
>                       <persName type="defendantName" id="t17280717-3-defend40">
>                       Nathaniel
>                       Mercy
>                    <interp value="Mercy" type="surname" inst="t17280717-3-defend40"/>
>                          <interp value="Nathaniel" type="given" inst="t17280717-3-defend40"/>
>                          <interp value="male" type="gender" inst="t17280717-3-defend40"/>
>                       </persName>
>                 , of <placeName id="t17280717-3-defloc9">St. James's Westminster</placeName>
>                       <interp value="St. James's Westminster" type="placeName" inst="t17280717-3-defloc9"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-3-defloc9"/>
>                       <join targets="t17280717-3-defend40 t17280717-3-defloc9" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-3-off10">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-3-off10"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-3-off10"/>
>                    stealing a Coach Wheel, value 12 Shillings
>                 </rs>, the Goods of
>                    <persName type="victimName" id="t17280717-3-victim41">
>                       Thomas
>                       West
>                    <interp value="West" type="surname" inst="t17280717-3-victim41"/>
>                          <interp value="Thomas" type="given" inst="t17280717-3-victim41"/>
>                          <interp value="male" type="gender" inst="t17280717-3-victim41"/>
>                          <join targets="t17280717-3-off10 t17280717-3-victim41" targOrder="Y" result="offenceVictim"/>
>                       </persName>
>                  , on the 9th of this Instant, to which Indictment he <rs type="verdictDescription" id="t17280717-3-verdict11">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-3-verdict11"/>
>                          <interp value="pleadedGuilty" type="verdictSubcategory" inst="t17280717-3-verdict11"/>
>                    pleaded guilty
>                 </rs>.</p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-3-punish12">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-3-punish12"/>
>                          <join targets="t17280717-3-defend40 t17280717-3-punish12" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>, <div1 id="t17280717-4" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-4"/>
>                    <interp value="1728" type="year" inst="t17280717-4"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-4"/>
>                    <interp value="17280717" type="date" inst="t17280717-4"/>
>                    <join targets="t17280717-4-defend42 t17280717-4-off14 t17280717-4-verdict17" targOrder="Y" id="t17280717-4-off14-c38" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-4-defend42">
>                       Margaret
>                       King
>                    <interp value="King" type="surname" inst="t17280717-4-defend42"/>
>                          <interp value="Margaret" type="given" inst="t17280717-4-defend42"/>
>                          <interp value="female" type="gender" inst="t17280717-4-defend42"/>
>                       </persName>
>                 , of <placeName id="t17280717-4-defloc13">St. Ann's Westminster</placeName>
>                       <interp value="St. Ann's Westminster" type="placeName" inst="t17280717-4-defloc13"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-4-defloc13"/>
>                       <join targets="t17280717-4-defend42 t17280717-4-defloc13" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-4-off14">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-4-off14"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-4-off14"/>
>                    privately stealing a Gold Watch, value 16 l.
>                 </rs> on the <rs type="crimeDate" id="t17280717-4-cd15">first of May</rs>
>                       <join targets="t17280717-4-off14 t17280717-4-cd15" targOrder="Y" result="offenceCrimeDate"/> last, the Property of
>                    <persName type="victimName" id="t17280717-4-victim43">
>                       Timothy
>                       Conner
>                    <interp value="Conner" type="surname" inst="t17280717-4-victim43"/>
>                          <interp value="Timothy" type="given" inst="t17280717-4-victim43"/>
>                          <interp value="male" type="gender" inst="t17280717-4-victim43"/>
>                          <join targets="t17280717-4-off14 t17280717-4-victim43" targOrder="Y" result="offenceVictim"/>
>                       </persName>
>                  .</p>
>                    <p>The Prosecutor depos'd, That he met the Prisoner in the Street, and ask'd her to drink a Glass of Wine, to which she consented, and they went to the <placeName id="t17280717-4-crimeloc16">Swan Tavern in Newport Market</placeName>
>                       <interp value="Swan Tavern in Newport Market" type="placeName" inst="t17280717-4-crimeloc16"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-4-crimeloc16"/>
>                       <join targets="t17280717-4-off14 t17280717-4-crimeloc16" targOrder="Y" result="offencePlace"/>, and lovingly drank 4 Pints, the Prisoner asking him what it was a Clock, he pulled out his Gold Watch, and bid her look; that she took it in her Hand, but could not remember that she returned it again, neither could she say positively, that she had it, but as he mis'd it as soon as he parted with her he thought he had occasion of Suspicion; yet, said he, I had been drinking before, (tho' it was but Nine in the Morning)and can't tell directly how the Matter stood; which being all the Evidence he could give against her, she was <rs type="verdictDescription" id="t17280717-4-verdict17">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-4-verdict17"/>
>                    acquitted
>                 </rs>.</p>
>                 </div1>, <div1 id="t17280717-5" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-5"/>
>                    <interp value="1728" type="year" inst="t17280717-5"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-5"/>
>                    <interp value="17280717" type="date" inst="t17280717-5"/>
>                    <join targets="t17280717-5-defend44 t17280717-5-off19 t17280717-5-verdict22" targOrder="Y" id="t17280717-5-off19-c40" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-5-defend44">
>                       Elizabeth
>                       Mould
>                    <interp value="Mould" type="surname" inst="t17280717-5-defend44"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-5-defend44"/>
>                          <interp value="female" type="gender" inst="t17280717-5-defend44"/>
>                       </persName>
>                 , of <placeName id="t17280717-5-defloc18">St. Martins in the Fields</placeName>
>                       <interp value="St. Martins in the Fields" type="placeName" inst="t17280717-5-defloc18"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-5-defloc18"/>
>                       <join targets="t17280717-5-defend44 t17280717-5-defloc18" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-5-off19">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-5-off19"/>
>                          <interp value="pocketpicking" type="offenceSubcategory" inst="t17280717-5-off19"/>
>                    privately stealing Fifty Pounds and eight Shillings, from the Person of
>                          <persName type="victimName" id="t17280717-5-victim45">
>                             John
>                             Coxall
>                          <interp value="Coxall" type="surname" inst="t17280717-5-victim45"/>
>                             <interp value="John" type="given" inst="t17280717-5-victim45"/>
>                             <interp value="male" type="gender" inst="t17280717-5-victim45"/>
>                             <join targets="t17280717-5-off19 t17280717-5-victim45" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>
>
>                       </rs>, on the <rs type="crimeDate" id="t17280717-5-cd20">24th of June</rs>
>                       <join targets="t17280717-5-off19 t17280717-5-cd20" targOrder="Y" result="offenceCrimeDate"/> last</p>
>                    <p>The Prosecutor depos'd, That he being a <rs type="occupation" id="t17280717-5-viclabel21">Bricklayer</rs>
>                       <join targets="t17280717-5-victim45 t17280717-5-viclabel21" targOrder="Y" result="persNameOccupation"/>, was endeavouring to get some Business to do, at the House where the Prisoner lived, and it being a Chandler's Shop, to obtain the Good-Will of the People, he call'd for several Drams, and treated the Mistress of the House and one Mrs. Greaves, who was his Customer, with Cyder, Brandy, &amp;c. that whilst they were drinking the Prisoner came into the Room, and he likewise treated her, and she, in return, wip'd him over the Face, and seem'd very fond of him, saying, she would give him a fine Nosegay, and something to cheer his Heart, if he would go with her to Covent-Garden Market, and then (as his Expression was) she weagled him down into the Cellar, and there kept him lock'd up in a back Passage, that he was very much in Liquor, and scarce sensible of what he did, but he found what she had done to his Sorrow, for she had taken all his Money, which he missing, made a Noise, and the People of the House hearing him, let him out of his Place of Confinement, by conducting him up the Back Stairs; that he got Officers and search'd the Cellar, but could not find the Prisoner. The Prisoner desiring he might be ask'd if they did not drink together in the Cellar, the Prosecutor answer'd, No, she would fetch no Drink, saying, she did not care to be seen by the Publicans; that she was all for dry Money, and she, and None but she, had his Fifty Pounds, for save only him and her, there were neither Man, Woman, nor Child, nor Dog nor Cat in the Cellar.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person46">
>                       Elizabeth
>                       Harper
>                    <interp value="Harper" type="surname" inst="t17280717-5-person46"/>
>                          <interp value="Elizabeth" type="given" inst="t17280717-5-person46"/>
>                          <interp value="female" type="gender" inst="t17280717-5-person46"/>
>                       </persName>
>                  depos'd, That the Prosecutor came into her Shop, and drank Cyder, Usquebaugh and Brandy, and being disguis'd in Liquor he pull'd out eight or ten Guineas and said, he was no Scoundrel; that she and Mrs. Greaves, whom he brought in to treat, begg'd he would put up his Money and take Care of it, and about that Time the Prisoner came into the Room, and familiarly stroaking his cheeks, persuaded him to go into the Cellar, saying, he should pay his Footing, that they staid half an Hour below, and this Deponent looking down, saw the Prisoner's Mother there, that he came up with her again, and treated her with Usquebaugh, and at 11 o' Clock, which was several Hours after, they heard him in a back Passage, where the House was supplied with Water, and letting him into their House, he said he was robb'd, at which this Deponent's Husband thrust him out of doors.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person47">
>                       Isabella
>                       Greaves
>                    <interp value="Greaves" type="surname" inst="t17280717-5-person47"/>
>                          <interp value="Isabella" type="given" inst="t17280717-5-person47"/>
>                          <interp value="female" type="gender" inst="t17280717-5-person47"/>
>                       </persName>
>                  likewise confirm'd every Part of this Deposition, adding, That the Prisoner was not to be seen that Night after the Robbery.</p>
>                    <p>The Prisoner said in her Defence, That the Prosecutor went down into her Cellar, and behaved himself so rudely, that she was forced to threaten to send for an Officer, that she knew nothing of his Money, and had not gone away that Night, but as she was oblig'd by her Husband, and that she came next Morning and set her Greens out.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person48">
>                       Anthony
>                       Dyer
>                    <interp value="Dyer" type="surname" inst="t17280717-5-person48"/>
>                          <interp value="Anthony" type="given" inst="t17280717-5-person48"/>
>                          <interp value="male" type="gender" inst="t17280717-5-person48"/>
>                       </persName>
>                  depos'd, That the Prosecutor told him he had lost his Money being Drunk, that he had been in a Cellar and in a Vault, where he fell asleep, and gave a very odd account of the Adventure.</p>
>                    <p>
>
>                       <persName id="t17280717-5-person49">
>                       George
>                       Smith
>                    <interp value="Smith" type="surname" inst="t17280717-5-person49"/>
>                          <interp value="George" type="given" inst="t17280717-5-person49"/>
>                          <interp value="male" type="gender" inst="t17280717-5-person49"/>
>                       </persName>
>                  depos'd, That the Prosecutor said he fell asleep upon a Vault, and could charge no Body.</p>
>                    <p>The Constable, the Watchman, and others severally depos'd, That the Morning after this happened, he said he could charge no particular person, but he would indict the House. The Prisoner having a very good Character from several reputable Witnesses, the Jury <rs type="verdictDescription" id="t17280717-5-verdict22">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-5-verdict22"/>
>                    acquitted
>                 </rs> her.</p>
>                 </div1>, <div1 id="t17280717-6" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-6"/>
>                    <interp value="1728" type="year" inst="t17280717-6"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-6"/>
>                    <interp value="17280717" type="date" inst="t17280717-6"/>
>                    <join targets="t17280717-6-defend50 t17280717-6-off24 t17280717-6-verdict26" targOrder="Y" id="t17280717-6-off24-c46" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-6-defend50">
>                       Phillip
>                       Hilliard
>                    <interp value="Hilliard" type="surname" inst="t17280717-6-defend50"/>
>                          <interp value="Phillip" type="given" inst="t17280717-6-defend50"/>
>                          <interp value="male" type="gender" inst="t17280717-6-defend50"/>
>                       </persName>
>                 , of <placeName id="t17280717-6-defloc23">St. James's Westminster</placeName>
>                       <interp value="St. James's Westminster" type="placeName" inst="t17280717-6-defloc23"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-6-defloc23"/>
>                       <join targets="t17280717-6-defend50 t17280717-6-defloc23" targOrder="Y" result="persNamePlace"/>, was indicted for <rs type="offenceDescription" id="t17280717-6-off24">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-6-off24"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-6-off24"/>
>                    feloniously stealing a Silver Spoon, value eleven Shillings, the Property of
>                          <persName type="victimName" id="t17280717-6-victim51">
>                             Abraham
>                             Mannio
>                          <interp value="Mannio" type="surname" inst="t17280717-6-victim51"/>
>                             <interp value="Abraham" type="given" inst="t17280717-6-victim51"/>
>                             <interp value="male" type="gender" inst="t17280717-6-victim51"/>
>                             <join targets="t17280717-6-off24 t17280717-6-victim51" targOrder="Y" result="offenceVictim"/>
>                          </persName>
>                        , on the <rs type="crimeDate" id="t17280717-6-cd25">27th of June</rs>
>                          <join targets="t17280717-6-off24 t17280717-6-cd25" targOrder="Y" result="offenceCrimeDate"/> last.
>                 </rs>
>                    </p>
>                    <p>Mr. Hayden depos'd, That the Prisoner brought the Spoon to him to pawn, and he suspecting it to be stolen, stopp'd him and carried him to the Round-House, where he confess'd he stole it at the Prosecutor's, he being invited there to Dinner with some Gentlemans Servants.</p>
>                    <p>
>
>                       <persName id="t17280717-6-person52">
>                       Robert
>                       Amey
>                    <interp value="Amey" type="surname" inst="t17280717-6-person52"/>
>                          <interp value="Robert" type="given" inst="t17280717-6-person52"/>
>                          <interp value="male" type="gender" inst="t17280717-6-person52"/>
>                       </persName>
>                  depos'd, That he attended the Gentlemens Servants at Dinner, and afterwards they miss'd a Spoon, that Mr. Haydon sent them word of the Spoon brought to him by the Prisoner, and he going to match it by the others, found it to be the same which they had lost, it being of the same Make and Mark: The Fact being thus plainly proved upon him, the Jury found him <rs type="verdictDescription" id="t17280717-6-verdict26">
>                          <interp value="guilty" type="verdictCategory" inst="t17280717-6-verdict26"/>
>                          <interp value="theftunder1s" type="verdictSubcategory" inst="t17280717-6-verdict26"/>
>                    guilty to the value of 10d.
>                 </rs>
>                    </p>
>                    <p>
>                       <rs type="punishmentDescription" id="t17280717-6-punish27">
>                          <interp value="transport" type="punishmentCategory" inst="t17280717-6-punish27"/>
>                          <join targets="t17280717-6-defend50 t17280717-6-punish27" targOrder="Y" result="defendantPunishment"/>
>
>                          <note>[Transportation. See summary.]</note>
>
>                       </rs>
>                    </p>
>                 </div1>, <div1 id="t17280717-7" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-7"/>
>                    <interp value="1728" type="year" inst="t17280717-7"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-7"/>
>                    <interp value="17280717" type="date" inst="t17280717-7"/>
>                    <join targets="t17280717-7-defend53 t17280717-7-off29 t17280717-7-verdict31" targOrder="Y" id="t17280717-7-off29-c49" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-7-defend53">
>                       Robert
>                       Ashby
>                    <interp value="Ashby" type="surname" inst="t17280717-7-defend53"/>
>                          <interp value="Robert" type="given" inst="t17280717-7-defend53"/>
>                          <interp value="male" type="gender" inst="t17280717-7-defend53"/>
>                       </persName>
>                 , of <rs type="occupation" id="t17280717-7-deflabel28">St. Andrew's Holborn</rs>
>                       <join targets="t17280717-7-defend53 t17280717-7-deflabel28" targOrder="Y" result="persNameOccupation"/>, was indicted for <rs type="offenceDescription" id="t17280717-7-off29">
>                          <interp value="theft" type="offenceCategory" inst="t17280717-7-off29"/>
>                          <interp value="grandLarceny" type="offenceSubcategory" inst="t17280717-7-off29"/>
>                    stealing a Gold Watch, value 16 l. a Chain, value 4 l. a Cornelian Seal set in Gold, value 10s. the Property of
>                          <persName type="victimName" id="t17280717-7-victim54">
>                             Nicholas
>                             Roper
>                          <interp value="Roper" type="surname" inst="t17280717-7-victim54"/>
>                             <interp value="Nicholas" type="given" inst="t17280717-7-victim54"/>
>                             <interp value="male" type="gender" inst="t17280717-7-victim54"/>
>                          </persName>
>                         from the Person of
>                          <persName type="victimName" id="t17280717-7-victim55">
>                             Phoebe
>                             Thickpenny
>                          <interp value="Thickpenny" type="surname" inst="t17280717-7-victim55"/>
>                             <interp value="Phoebe" type="given" inst="t17280717-7-victim55"/>
>                             <interp value="female" type="gender" inst="t17280717-7-victim55"/>
>                          </persName>
>
>
>                       </rs>, who depos'd, That her Mistress being at Little Chelsea, sent her to their House in Lad-lane, for the Watch and other Things she had occasion for, that she call'd at the Prisoner's in Castle Yard, Chick-Lane, as she was going towards Chelsea with the Watch and the Bundle, where she drank part of a Pint of Beer, some Tea 2 Quarterns of Brandy, and a Bottle of Cyder; that the Prisoner would go part of the Way home with her, and in <placeName id="t17280717-7-crimeloc30">Leather-Lane</placeName>
>                       <interp value="Leather-Lane" type="placeName" inst="t17280717-7-crimeloc30"/>
>                       <interp value="crimeLocation" type="type" inst="t17280717-7-crimeloc30"/>
>                       <join targets="t17280717-7-off29 t17280717-7-crimeloc30" targOrder="Y" result="offencePlace"/>, he said, faith they would not part Dry lips, and accordingly they went into a publick House and drank a Pint of Twopenny, and two Quarterns of Brandy, that she had the Watch then, and at the Door the Prisoner kiss'd her, and gave her a shilling for a Coach, she having out-staid her Time; that when he kiss'd her, he put one Hand around her Waist, but what he did with the other she could not tell, that she then cross'd the Way to another House, and immediately miss'd the Watch, and she was sure she had pinn'd it so to her Side, that she could not drop it.</p>
>                    <p>
>
>                       <persName id="t17280717-7-person56">
>                       Margaret
>                       Nelson
>                    <interp value="Nelson" type="surname" inst="t17280717-7-person56"/>
>                          <interp value="Margaret" type="given" inst="t17280717-7-person56"/>
>                          <interp value="female" type="gender" inst="t17280717-7-person56"/>
>                       </persName>
>                  depos'd, That the Prisoner and Phoebe Thickpenny, came to her House in Leather-Lane, and he call'd for a private Room, to which the Girl would not consent, that the Girl wanted to go to, &amp;c. and she went with her, when the Girl said, she had her Mistress's Gold Watch, and seeming to look on it, told her it was 7 o' Clock, but this Deponent did not see the Watch, yet she said, she did verily believe she heard it beat, that they soon parted, and in a Quarter of an Hour the maid returned, and said she had lost the Watch.</p>
>                    <p>The Prisoner said in his Defence, That he knew nothing of it, any further than she said it was her Mistress's, that they parted very good Friends, and she having been an old Sweetheart of his, laid her Head upon his Shoulder, and said, She could live and die there, which would have been no little Aggravation to his Crime, had he wrong'd so good Natur'd a Creature: But the Evidence against him being weak, and several appearing to his Character, the Jury <rs type="verdictDescription" id="t17280717-7-verdict31">
>                          <interp value="notGuilty" type="verdictCategory" inst="t17280717-7-verdict31"/>
>                    acquitted
>                 </rs> him. </p>
>                 </div1>, <div1 id="t17280717-8" type="trialAccount">
>                    <interp value="BAILEY" type="collection" inst="t17280717-8"/>
>                    <interp value="1728" type="year" inst="t17280717-8"/>
>                    <interp value="sessionsPapers/17280717" type="uri" inst="t17280717-8"/>
>                    <interp value="17280717" type="date" inst="t17280717-8"/>
>                    <join targets="t17280717-8-defend57 t17280717-8-off33 t17280717-8-verdict35" targOrder="Y" id="t17280717-8-off33-c53" result="criminalCharge"/>
>                    <join targets="t17280717-8-defend58 t17280717-8-off33 t17280717-8-verdict35" targOrder="Y" id="t17280717-8-off33-c54" result="criminalCharge"/>
>
>                    <p>
>
>                       <persName type="defendantName" id="t17280717-8-defend57">
>                       Joseph
>                       Plummer
>                    <interp value="Plummer" type="surname" inst="t17280717-8-defend57"/>
>                          <interp value="Joseph" type="given" inst="t17280717-8-defend57"/>
>                          <interp value="male" type="gender" inst="t17280717-8-defend57"/>
>                       </persName>
>                  , and
>                    <persName type="defendantName" id="t17280717-8-defend58">
>                       Henry
>                       Coleman
>                    <interp value="Coleman" type="surname" inst="t17280717-8-defend58"/>
>                          <interp value="Henry" type="given" inst="t17280717-8-defend58"/>
>                          <interp value="male" type="gender" inst="t17280717-8-defend58"/>
>                       </persName>
>                  , of <placeName id="t17280717-8-defloc32">Edmonton</placeName>
>                       <interp value="Edmonton" type="placeName" inst="t17280717-8-defloc32"/>
>                       <interp value="defendantHome" type="type" inst="t17280717-8-defloc32"/>
>                       <join targets="t17280717-8-defend57 t17280717-8-defloc32" targOrder="Y" result="persNamePlace"/>
>                       <join targets="t17280717-8-defend58 t17280717-8-defloc32" targOrder="Y" result="persNamePlace"/>, were indicted for <rs type="offenceDescription" id="t17280717-8-off33">
>                          <interp value="theft" type="offenceCategory" inst="t17280717...

``` scala
(elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => (Node \ "@type", Node \ "@id" ))
```

>     res51: scala.collection.immutable.Seq[(scala.xml.NodeSeq, scala.xml.NodeSeq)] = List((trialAccount,t17280717-1), (trialAccount,t17280717-2), (trialAccount,t17280717-3), (trialAccount,t17280717-4), (trialAccount,t17280717-5), (trialAccount,t17280717-6), (trialAccount,t17280717-7), (trialAccount,t17280717-8), (trialAccount,t17280717-9), (trialAccount,t17280717-10), (trialAccount,t17280717-11), (trialAccount,t17280717-12), (trialAccount,t17280717-13), (trialAccount,t17280717-14), (trialAccount,t17280717-15), (trialAccount,t17280717-16), (trialAccount,t17280717-17), (trialAccount,t17280717-18), (trialAccount,t17280717-19), (trialAccount,t17280717-20), (trialAccount,t17280717-21), (trialAccount,t17280717-22), (trialAccount,t17280717-23), (trialAccount,t17280717-24), (trialAccount,t17280717-25), (trialAccount,t17280717-26), (trialAccount,t17280717-27), (trialAccount,t17280717-28), (trialAccount,t17280717-29), (trialAccount,t17280717-30), (trialAccount,t17280717-31), (trialAccount,t17280717-32), (trialAccount,t17280717-33), (trialAccount,t17280717-34), (trialAccount,t17280717-35), (trialAccount,t17280717-36), (trialAccount,t17280717-37), (trialAccount,t17280717-38), (trialAccount,t17280717-39), (trialAccount,t17280717-40), (trialAccount,t17280717-41), (trialAccount,t17280717-42), (trialAccount,t17280717-43), (trialAccount,t17280717-44), (trialAccount,t17280717-45), (trialAccount,t17280717-46), (trialAccount,t17280717-47), (trialAccount,t17280717-48), (trialAccount,t17280717-49), (trialAccount,t17280717-50), (trialAccount,t17280717-51), (trialAccount,t17280717-52), (trialAccount,t17280717-53), (trialAccount,t17280717-54), (trialAccount,t17280717-55), (trialAccount,t17280717-56), (trialAccount,t17280717-57))

``` scala
val trials = (elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => (Node \ "@type", Node \ "@id", (Node \\ "rs" \\ "interp").map( n => ((n \\ "@type").text, (n \\ "@value").text ))))
```

>     trials: scala.collection.immutable.Seq[(scala.xml.NodeSeq, scala.xml.NodeSeq, scala.collection.immutable.Seq[(String, String)])] = List((trialAccount,t17280717-1,List((offenceCategory,theft), (offenceSubcategory,theftFromPlace), (surname,Reeves), (given,James), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-2,List((offenceCategory,theft), (offenceSubcategory,pettyLarceny), (verdictCategory,guilty), (verdictSubcategory,pleadedGuilty), (punishmentCategory,transport))), (trialAccount,t17280717-3,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,pleadedGuilty), (punishmentCategory,transport))), (trialAccount,t17280717-4,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,notGuilty))), (trialAccount,t17280717-5,List((offenceCategory,theft), (offenceSubcategory,pocketpicking), (surname,Coxall), (given,John), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-6,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (surname,Mannio), (given,Abraham), (gender,male), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-7,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (surname,Roper), (given,Nicholas), (gender,male), (surname,Thickpenny), (given,Phoebe), (gender,female), (verdictCategory,notGuilty))), (trialAccount,t17280717-8,List((offenceCategory,theft), (offenceSubcategory,pettyLarceny), (verdictCategory,notGuilty))), (trialAccount,t17280717-9,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,notGuilty))), (trialAccount,t17280717-10,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,corporal), (punishmentSubcategory,whipping))), (trialAccount,t17280717-11,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,notGuilty))), (trialAccount,t17280717-12,List((offenceCategory,theft), (offenceSubcategory,pocketpicking), (surname,Bete), (given,Thomas), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-13,List((offenceCategory,theft), (offenceSubcategory,pocketpicking), (surname,Polliter), (given,John), (gender,male), (offenceCategory,theft), (offenceSubcategory,theftFromPlace), (verdictCategory,guilty), (verdictSubcategory,theftunder5s), (verdictCategory,notGuilty), (punishmentCategory,transport))), (trialAccount,t17280717-14,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (surname,Freeman), (given,Moses), (gender,male), (verdictCategory,guilty), (verdictSubcategory,theftunder5s), (punishmentCategory,transport))), (trialAccount,t17280717-15,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-16,List((offenceCategory,theft), (offenceSubcategory,theftFromPlace), (surname,Yew), (given,Thomas), (gender,male), (verdictCategory,guilty), (punishmentCategory,death))), (trialAccount,t17280717-17,List((offenceCategory,theft), (offenceSubcategory,animalTheft), (offenceCategory,theft), (offenceSubcategory,animalTheft), (verdictCategory,guilty), (punishmentCategory,death))), (trialAccount,t17280717-18,List((offenceCategory,theft), (offenceSubcategory,animalTheft), (verdictCategory,guilty), (punishmentCategory,death))), (trialAccount,t17280717-19,List((offenceCategory,theft), (offenceSubcategory,shoplifting), (surname,Harrison), (given,Mary), (gender,female), (verdictCategory,guilty), (verdictSubcategory,theftunder5s), (punishmentCategory,transport))), (trialAccount,t17280717-20,List((offenceCategory,theft), (offenceSubcategory,theftFromPlace), (surname,Ward), (given,Samuel), (gender,male), (surname,Harrison), (given,Joseph), (gender,male), (verdictCategory,guilty), (verdictSubcategory,theftunder5s), (punishmentCategory,transport))), (trialAccount,t17280717-21,List((offenceCategory,theft), (offenceSubcategory,shoplifting), (surname,Townshend), (given,George), (gender,male), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-22,List((offenceCategory,theft), (offenceSubcategory,pocketpicking), (surname,Butler), (given,James), (gender,male), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-23,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-24,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty))), (trialAccount,t17280717-25,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-26,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-27,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-28,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (surname,Swift), (given,Grace), (gender,female), (gender,female), (verdictCategory,guilty), (verdictSubcategory,theftunder40s), (punishmentCategory,transport))), (trialAccount,t17280717-29,List((offenceCategory,sexual), (offenceSubcategory,bigamy), (surname,Danbrine), (given,Abraham), (gender,male), (surname,Par), (given,Henry), (gender,male), (verdictCategory,guilty), (punishmentCategory,miscPunish), (punishmentSubcategory,branding))), (trialAccount,t17280717-30,List((offenceCategory,theft), (offenceSubcategory,pettyLarceny), (surname,Smith), (given,Richard), (gender,male), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-31,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,notGuilty))), (trialAccount,t17280717-32,List((offenceCategory,theft), (offenceSubcategory,pocketpicking), (surname,Butterfield), (given,Anthony), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-33,List((offenceCategory,violentTheft), (offenceSubcategory,highwayRobbery), (surname,Russel), (given,Nathaniel), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-34,List((offenceCategory,theft), (offenceSubcategory,pocketpicking), (surname,Dalby), (given,Mary), (gender,female), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-35,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,notGuilty))), (trialAccount,t17280717-36,List((offenceCategory,theft), (offenceSubcategory,other), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,corporal), (punishmentSubcategory,whipping))), (trialAccount,t17280717-37,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,notGuilty))), (trialAccount,t17280717-38,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-39,List((offenceCategory,theft), (offenceSubcategory,other), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-40,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,notGuilty))), (trialAccount,t17280717-41,List((offenceCategory,royalOffences), (offenceSubcategory,coiningOffences), (verdictCategory,notGuilty))), (trialAccount,t17280717-42,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,corporal), (punishmentSubcategory,whipping))), (trialAccount,t17280717-43,List((offenceCategory,violentTheft), (offenceSubcategory,robbery), (surname,Herbert), (given,Henry Arthur), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-44,List((offenceCategory,sexual), (offenceSubcategory,bigamy), (given,Way), (gender,indeterminate), (surname,Towers), (given,John), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-45,List((offenceCategory,theft), (offenceSubcategory,burglary), (surname,Tomkinson), (given,John), (gender,male), (surname,Tomkinson), (given,John), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-46,List((offenceCategory,theft), (offenceSubcategory,other), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (verdictCategory,notGuilty), (punishmentCategory,transport))), (trialAccount,t17280717-47,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-48,List((offenceCategory,theft), (offenceSubcategory,other), (surname,Dearing), (given,Samuel), (gender,male), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-49,List((offenceCategory,theft), (offenceSubcategory,burglary), (surname,Tilley), (given,William), (gender,male), (verdictCategory,notGuilty), (verdictCategory,guilty), (verdictSubcategory,theftunder1s), (punishmentCategory,transport))), (trialAccount,t17280717-50,List((offenceCategory,theft), (offenceSubcategory,grandLarceny), (surname,Mills), (given,Jane), (gender,female), (verdictCategory,notGuilty))), (trialAccount,t17280717-51,List((offenceCategory,theft), (offenceSubcategory,pocketpicking), (surname,Evans), (given,Joseph), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-52,List((offenceCategory,theft), (offenceSubcategory,other), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-53,List((offenceCategory,theft), (offenceSubcategory,other), (verdictCategory,notGuilty), (verdictSubcategory,noProsecutor))), (trialAccount,t17280717-54,List((offenceCategory,theft), (offenceSubcategory,theftFromPlace), (placeName,Belsize House), (type,crimeLocation), (verdictCategory,guilty), (punishmentCategory,imprison))), (trialAccount,t17280717-55,List((offenceCategory,sexual), (offenceSubcategory,bigamy), (surname,Pack), (given,William), (gender,male), (surname,Selby), (given,Francis), (gender,male), (verdictCategory,notGuilty))), (trialAccount,t17280717-56,List((offenceCategory,deception), (offenceSubcategory,perjury), (surname,Matthews), (given,Richard), (gender,male), (verdictCategory,guilty), (verdictSubcategory,pleadedGuilty), (punishmentCategory,corporal), (punishmentSubcategory,pillory))), (trialAccount,t17280717-57,List((offenceCategory,deception), (offenceSubcategory,perjury), (surname,Matthews), (given,Richard), (gender,male), (verdictCategory,guilty), (verdictSubcategory,pleadedGuilty))))

``` scala
val wantedFields = Seq("verdictCategory","punishmentCategory","offenceCategory").toSet
```

>     wantedFields: scala.collection.immutable.Set[String] = Set(verdictCategory, punishmentCategory, offenceCategory)

``` scala
val trials = (elem \\ "div1").filter(Node => ((Node \ "@type").text == "trialAccount"))
                 .map(Node => ((Node \ "@type").text, (Node \ "@id").text, (Node \\ "rs" \\ "interp")
                                                               .filter(n => wantedFields.contains( (n \\ "@type").text))
                                                               .map( n => ((n \\ "@type").text, (n \\ "@value").text ))))
```

>     trials: scala.collection.immutable.Seq[(String, String, scala.collection.immutable.Seq[(String, String)])] = List((trialAccount,t17280717-1,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-2,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-3,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-4,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-5,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-6,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-7,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-8,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-9,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-10,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,corporal))), (trialAccount,t17280717-11,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-12,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-13,List((offenceCategory,theft), (offenceCategory,theft), (verdictCategory,guilty), (verdictCategory,notGuilty), (punishmentCategory,transport))), (trialAccount,t17280717-14,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-15,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-16,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,death))), (trialAccount,t17280717-17,List((offenceCategory,theft), (offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,death))), (trialAccount,t17280717-18,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,death))), (trialAccount,t17280717-19,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-20,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-21,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-22,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-23,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-24,List((offenceCategory,theft), (verdictCategory,guilty))), (trialAccount,t17280717-25,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-26,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-27,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-28,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-29,List((offenceCategory,sexual), (verdictCategory,guilty), (punishmentCategory,miscPunish))), (trialAccount,t17280717-30,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-31,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-32,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-33,List((offenceCategory,violentTheft), (verdictCategory,notGuilty))), (trialAccount,t17280717-34,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-35,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-36,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,corporal))), (trialAccount,t17280717-37,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-38,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-39,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-40,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-41,List((offenceCategory,royalOffences), (verdictCategory,notGuilty))), (trialAccount,t17280717-42,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,corporal))), (trialAccount,t17280717-43,List((offenceCategory,violentTheft), (verdictCategory,notGuilty))), (trialAccount,t17280717-44,List((offenceCategory,sexual), (verdictCategory,notGuilty))), (trialAccount,t17280717-45,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-46,List((offenceCategory,theft), (verdictCategory,guilty), (verdictCategory,notGuilty), (punishmentCategory,transport))), (trialAccount,t17280717-47,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-48,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-49,List((offenceCategory,theft), (verdictCategory,notGuilty), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-50,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-51,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-52,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,transport))), (trialAccount,t17280717-53,List((offenceCategory,theft), (verdictCategory,notGuilty))), (trialAccount,t17280717-54,List((offenceCategory,theft), (verdictCategory,guilty), (punishmentCategory,imprison))), (trialAccount,t17280717-55,List((offenceCategory,sexual), (verdictCategory,notGuilty))), (trialAccount,t17280717-56,List((offenceCategory,deception), (verdictCategory,guilty), (punishmentCategory,corporal))), (trialAccount,t17280717-57,List((offenceCategory,deception), (verdictCategory,guilty))))

Since there can be more than one defendant in a trial, we need to reduce by key as follows.

``` scala
def reduceByKey(collection: Traversable[Tuple2[String, Int]]) = {    
    collection
      .groupBy(_._1)
      .map { case (group: String, traversable) => traversable.reduce{(a,b) => (a._1, a._2 + b._2)} }
  }
```

>     reduceByKey: (collection: Traversable[(String, Int)])scala.collection.immutable.Map[String,Int]

Let's process the coarsest data on the trial as json strings.

``` scala
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

>     trials: scala.collection.immutable.Seq[scala.util.parsing.json.JSONObject] = List({"theft" : 1, "notGuilty" : 1, "id" : "t17280717-1"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-2"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-3"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-4"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-5"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-6"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-7"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-8"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-9"}, {"corporal" : 1, "theft" : 1, "guilty" : 1, "id" : "t17280717-10"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-11"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-12"}, {"guilty" : 1, "theft" : 2, "notGuilty" : 1, "transport" : 1, "id" : "t17280717-13"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-14"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-15"}, {"theft" : 1, "guilty" : 1, "death" : 1, "id" : "t17280717-16"}, {"theft" : 2, "guilty" : 1, "death" : 1, "id" : "t17280717-17"}, {"theft" : 1, "guilty" : 1, "death" : 1, "id" : "t17280717-18"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-19"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-20"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-21"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-22"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-23"}, {"theft" : 1, "guilty" : 1, "id" : "t17280717-24"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-25"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-26"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-27"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-28"}, {"miscPunish" : 1, "sexual" : 1, "guilty" : 1, "id" : "t17280717-29"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-30"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-31"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-32"}, {"violentTheft" : 1, "notGuilty" : 1, "id" : "t17280717-33"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-34"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-35"}, {"corporal" : 1, "theft" : 1, "guilty" : 1, "id" : "t17280717-36"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-37"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-38"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-39"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-40"}, {"royalOffences" : 1, "notGuilty" : 1, "id" : "t17280717-41"}, {"corporal" : 1, "theft" : 1, "guilty" : 1, "id" : "t17280717-42"}, {"violentTheft" : 1, "notGuilty" : 1, "id" : "t17280717-43"}, {"notGuilty" : 1, "sexual" : 1, "id" : "t17280717-44"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-45"}, {"guilty" : 1, "theft" : 1, "notGuilty" : 1, "transport" : 1, "id" : "t17280717-46"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-47"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-48"}, {"guilty" : 1, "theft" : 1, "notGuilty" : 1, "transport" : 1, "id" : "t17280717-49"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-50"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-51"}, {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-52"}, {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-53"}, {"theft" : 1, "imprison" : 1, "guilty" : 1, "id" : "t17280717-54"}, {"notGuilty" : 1, "sexual" : 1, "id" : "t17280717-55"}, {"corporal" : 1, "deception" : 1, "guilty" : 1, "id" : "t17280717-56"}, {"deception" : 1, "guilty" : 1, "id" : "t17280717-57"})

``` scala
trials.foreach(println)
```

>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-1"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-2"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-3"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-4"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-5"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-6"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-7"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-8"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-9"}
>     {"corporal" : 1, "theft" : 1, "guilty" : 1, "id" : "t17280717-10"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-11"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-12"}
>     {"guilty" : 1, "theft" : 2, "notGuilty" : 1, "transport" : 1, "id" : "t17280717-13"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-14"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-15"}
>     {"theft" : 1, "guilty" : 1, "death" : 1, "id" : "t17280717-16"}
>     {"theft" : 2, "guilty" : 1, "death" : 1, "id" : "t17280717-17"}
>     {"theft" : 1, "guilty" : 1, "death" : 1, "id" : "t17280717-18"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-19"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-20"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-21"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-22"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-23"}
>     {"theft" : 1, "guilty" : 1, "id" : "t17280717-24"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-25"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-26"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-27"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-28"}
>     {"miscPunish" : 1, "sexual" : 1, "guilty" : 1, "id" : "t17280717-29"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-30"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-31"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-32"}
>     {"violentTheft" : 1, "notGuilty" : 1, "id" : "t17280717-33"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-34"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-35"}
>     {"corporal" : 1, "theft" : 1, "guilty" : 1, "id" : "t17280717-36"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-37"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-38"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-39"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-40"}
>     {"royalOffences" : 1, "notGuilty" : 1, "id" : "t17280717-41"}
>     {"corporal" : 1, "theft" : 1, "guilty" : 1, "id" : "t17280717-42"}
>     {"violentTheft" : 1, "notGuilty" : 1, "id" : "t17280717-43"}
>     {"notGuilty" : 1, "sexual" : 1, "id" : "t17280717-44"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-45"}
>     {"guilty" : 1, "theft" : 1, "notGuilty" : 1, "transport" : 1, "id" : "t17280717-46"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-47"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-48"}
>     {"guilty" : 1, "theft" : 1, "notGuilty" : 1, "transport" : 1, "id" : "t17280717-49"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-50"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-51"}
>     {"theft" : 1, "transport" : 1, "guilty" : 1, "id" : "t17280717-52"}
>     {"theft" : 1, "notGuilty" : 1, "id" : "t17280717-53"}
>     {"theft" : 1, "imprison" : 1, "guilty" : 1, "id" : "t17280717-54"}
>     {"notGuilty" : 1, "sexual" : 1, "id" : "t17280717-55"}
>     {"corporal" : 1, "deception" : 1, "guilty" : 1, "id" : "t17280717-56"}
>     {"deception" : 1, "guilty" : 1, "id" : "t17280717-57"}

Step 2: Extract, Transform and Load XML files to get DataFrame of counts
------------------------------------------------------------------------

We have played enough (see **Step 1: Exploring data first: xml parsing in scala** above first) to understand what to do now with our xml data in order to get it converted to counts of crimes, verdicts and punishments.

Let's parse the xml files and turn into Dataframe in one block.

``` scala
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

>     rawWTF: org.apache.spark.rdd.RDD[(String, String)] = dbfs:/datasets/obo/tei/sessionsPapers/*.xml MapPartitionsRDD[2613] at wholeTextFiles at command-4126293188797652:1
>     raw: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[2614] at map at command-4126293188797652:2
>     trials: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[2615] at flatMap at command-4126293188797652:3

``` scala
dbutils.fs.rm("dbfs:/datasets/obo/processed/trialCounts",recurse=true) // let's remove the files from the previous analysis
trials.saveAsTextFile("dbfs:/datasets/obo/processed/trialCounts") // now let's save the trial counts - aboout 220 seconds to pars all data and get counts
```

``` scala
display(dbutils.fs.ls("dbfs:/datasets/obo/processed/trialCounts"))
```

| path                                                | name       | size      |
|-----------------------------------------------------|------------|-----------|
| dbfs:/datasets/obo/processed/trialCounts/\_SUCCESS  | \_SUCCESS  | 0.0       |
| dbfs:/datasets/obo/processed/trialCounts/part-00000 | part-00000 | 6914194.0 |
| dbfs:/datasets/obo/processed/trialCounts/part-00001 | part-00001 | 6483617.0 |

``` scala
val trialCountsDF = sqlContext.read.json("dbfs:/datasets/obo/processed/trialCounts")
```

>     trialCountsDF: org.apache.spark.sql.DataFrame = [breakingPeace: bigint, corporal: bigint ... 18 more fields]

``` scala
trialCountsDF.printSchema
```

>     root
>      |-- breakingPeace: long (nullable = true)
>      |-- corporal: long (nullable = true)
>      |-- damage: long (nullable = true)
>      |-- death: long (nullable = true)
>      |-- deception: long (nullable = true)
>      |-- guilty: long (nullable = true)
>      |-- id: string (nullable = true)
>      |-- imprison: long (nullable = true)
>      |-- kill: long (nullable = true)
>      |-- miscPunish: long (nullable = true)
>      |-- miscVerdict: long (nullable = true)
>      |-- miscellaneous: long (nullable = true)
>      |-- noPunish: long (nullable = true)
>      |-- notGuilty: long (nullable = true)
>      |-- royalOffences: long (nullable = true)
>      |-- sexual: long (nullable = true)
>      |-- specialVerdict: long (nullable = true)
>      |-- theft: long (nullable = true)
>      |-- transport: long (nullable = true)
>      |-- violentTheft: long (nullable = true)

``` scala
trialCountsDF.count // total number of trials
```

>     res57: Long = 197751

``` scala
display(trialCountsDF)
```

| breakingPeace | corporal | damage | death | deception | guilty | id          | imprison | kill | miscPunish | miscVerdict | miscellaneous |
|---------------|----------|--------|-------|-----------|--------|-------------|----------|------|------------|-------------|---------------|
| null          | null     | null   | null  | null      | 1.0    | t16740429-1 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16740429-2 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740429-3 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | null   | t16740429-4 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740429-5 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740429-6 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740429-7 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740429-8 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16740429-9 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16740717-1 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16740717-2 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | null   | t16740717-3 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | null   | t16740717-4 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740717-5 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740717-6 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16740909-1 | null     | 1.0  | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16740909-2 | null     | 1.0  | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16740909-3 | null     | 1.0  | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740909-4 | null     | null | null       | null        | null          |
| null          | null     | null   | 3.0   | null      | 1.0    | t16740909-5 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16740909-6 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16741014-1 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16741014-2 | null     | 1.0  | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16741014-3 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | null   | t16741014-4 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | null   | t16741014-5 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | 1.0    | t16741014-6 | null     | null | null       | null        | null          |
| null          | null     | null   | null  | null      | null   | t16741014-7 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16741014-8 | null     | null | null       | null        | null          |
| null          | null     | null   | 1.0   | null      | 1.0    | t16741212-1 | null     | null | null       | null        | null          |

Truncated to 30 rows

Truncated to 12 cols

``` scala
val trDF = trialCountsDF.na.fill(0) // filling nulls with 0
```

>     trDF: org.apache.spark.sql.DataFrame = [breakingPeace: bigint, corporal: bigint ... 18 more fields]

``` scala
display(trDF)
```

| breakingPeace | corporal | damage | death | deception | guilty | id          | imprison | kill | miscPunish | miscVerdict | miscellaneous |
|---------------|----------|--------|-------|-----------|--------|-------------|----------|------|------------|-------------|---------------|
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740429-1 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16740429-2 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740429-3 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 0.0    | t16740429-4 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740429-5 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740429-6 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740429-7 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740429-8 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16740429-9 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16740717-1 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16740717-2 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 0.0    | t16740717-3 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 0.0    | t16740717-4 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740717-5 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740717-6 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16740909-1 | 0.0      | 1.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16740909-2 | 0.0      | 1.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16740909-3 | 0.0      | 1.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740909-4 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 3.0   | 0.0       | 1.0    | t16740909-5 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16740909-6 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16741014-1 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16741014-2 | 0.0      | 1.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16741014-3 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 0.0    | t16741014-4 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 0.0    | t16741014-5 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 1.0    | t16741014-6 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 0.0   | 0.0       | 0.0    | t16741014-7 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16741014-8 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |
| 0.0           | 0.0      | 0.0    | 1.0   | 0.0       | 1.0    | t16741212-1 | 0.0      | 0.0  | 0.0        | 0.0         | 0.0           |

Truncated to 30 rows

Truncated to 12 cols

Step 0: Dowloading and Loading Data (The Full Dataset)
------------------------------------------------------

First we will be downloading data from <http://lamastex.org/datasets/public/OldBailey/index.html>.

The steps below need to be done once for a give shard!

You can download the tiny dataset `obo-tiny/OB-tiny_tei_7-2_CC-BY-NC.zip` **to save time and space in db CE**

**Optional TODOs:** \* one could just read the zip files directly (see week 10 on Beijing taxi trajectories example from the scalable-data-science course in 2016 or read 'importing zip files' in the Guide). \* one could just download from s3 directly

``` sh
# if you want to download the tiny dataset
wget https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/datasets/obo-tiny/OB-tiny_tei_7-2_CC-BY-NC.zip
```

>     --2017-11-07 21:20:41--  https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/datasets/obo-tiny/OB-tiny_tei_7-2_CC-BY-NC.zip
>     Resolving raw.githubusercontent.com (raw.githubusercontent.com)... 151.101.52.133
>     Connecting to raw.githubusercontent.com (raw.githubusercontent.com)|151.101.52.133|:443... connected.
>     HTTP request sent, awaiting response... 200 OK
>     Length: 1209487 (1.2M) [application/zip]
>     Saving to: ‘OB-tiny_tei_7-2_CC-BY-NC.zip’
>
>          0K .......... .......... .......... .......... ..........  4% 2.76M 0s
>         50K .......... .......... .......... .......... ..........  8% 6.11M 0s
>        100K .......... .......... .......... .......... .......... 12% 6.21M 0s
>        150K .......... .......... .......... .......... .......... 16% 6.10M 0s
>        200K .......... .......... .......... .......... .......... 21% 6.25M 0s
>        250K .......... .......... .......... .......... .......... 25% 7.89M 0s
>        300K .......... .......... .......... .......... .......... 29% 7.56M 0s
>        350K .......... .......... .......... .......... .......... 33% 5.86M 0s
>        400K .......... .......... .......... .......... .......... 38% 7.21M 0s
>        450K .......... .......... .......... .......... .......... 42% 9.37M 0s
>        500K .......... .......... .......... .......... .......... 46% 8.63M 0s
>        550K .......... .......... .......... .......... .......... 50% 7.04M 0s
>        600K .......... .......... .......... .......... .......... 55% 8.96M 0s
>        650K .......... .......... .......... .......... .......... 59% 11.2M 0s
>        700K .......... .......... .......... .......... .......... 63% 9.67M 0s
>        750K .......... .......... .......... .......... .......... 67% 7.13M 0s
>        800K .......... .......... .......... .......... .......... 71% 12.1M 0s
>        850K .......... .......... .......... .......... .......... 76% 8.50M 0s
>        900K .......... .......... .......... .......... .......... 80% 8.91M 0s
>        950K .......... .......... .......... .......... .......... 84% 16.3M 0s
>       1000K .......... .......... .......... .......... .......... 88% 9.95M 0s
>       1050K .......... .......... .......... .......... .......... 93% 12.7M 0s
>       1100K .......... .......... .......... .......... .......... 97% 11.3M 0s
>       1150K .......... .......... .......... .                    100% 11.8M=0.2s
>
>     2017-11-07 21:20:41 (7.66 MB/s) - ‘OB-tiny_tei_7-2_CC-BY-NC.zip’ saved [1209487/1209487]

``` sh
# this is the full dataset - necessary for a project on this dataset
wget http://lamastex.org/datasets/public/OldBailey/OB_tei_7-2_CC-BY-NC.zip
```

>     --2017-11-07 21:42:53--  http://lamastex.org/datasets/public/OldBailey/OB_tei_7-2_CC-BY-NC.zip
>     Resolving lamastex.org (lamastex.org)... 166.62.28.100
>     Connecting to lamastex.org (lamastex.org)|166.62.28.100|:80... connected.
>     HTTP request sent, awaiting response... 200 OK
>     Length: 324723900 (310M) [application/zip]
>     Saving to: ‘OB_tei_7-2_CC-BY-NC.zip’
>
>          0K .......... .......... .......... .......... ..........  0%  125K 42m21s
>         50K .......... .......... .......... .......... ..........  0%  249K 31m46s
>        100K .......... .......... .......... .......... ..........  0%  250K 28m13s
>        150K .......... .......... .......... .......... ..........  0% 36.9M 21m12s
>        200K .......... .......... .......... .......... ..........  0%  250K 21m11s
>        250K .......... .......... .......... .......... ..........  0% 31.8M 17m41s
>        300K .......... .......... .......... .......... ..........  0% 54.5M 15m10s
>        350K .......... .......... .......... .......... ..........  0% 48.6M 13m17s
>        400K .......... .......... .......... .......... ..........  0%  252K 14m8s
>        450K .......... .......... .......... .......... ..........  0% 45.6M 12m43s
>        500K .......... .......... .......... .......... ..........  0% 44.4M 11m35s
>        550K .......... .......... .......... .......... ..........  0% 47.4M 10m37s
>        600K .......... .......... .......... .......... ..........  0%  254K 11m24s
>        650K .......... .......... .......... .......... ..........  0% 43.3M 10m35s
>        700K .......... .......... .......... .......... ..........  0% 43.9M 9m54s
>        750K .......... .......... .......... .......... ..........  0% 51.3M 9m17s
>        800K .......... .......... .......... .......... ..........  0% 46.2M 8m44s
>        850K .......... .......... .......... .......... ..........  0% 63.0M 8m15s
>        900K .......... .......... .......... .......... ..........  0%  256K 8m54s
>        950K .......... .......... .......... .......... ..........  0% 41.1M 8m28s
>       1000K .......... .......... .......... .......... ..........  0% 38.4M 8m4s
>       1050K .......... .......... .......... .......... ..........  0% 40.3M 7m42s
>       1100K .......... .......... .......... .......... ..........  0% 49.2M 7m22s
>       1150K .......... .......... .......... .......... ..........  0% 56.2M 7m4s
>       1200K .......... .......... .......... .......... ..........  0% 54.3M 6m47s
>       1250K .......... .......... .......... .......... ..........  0% 46.9M 6m32s
>       1300K .......... .......... .......... .......... ..........  0%  259K 7m2s
>       1350K .......... .......... .......... .......... ..........  0% 32.3M 6m48s
>       1400K .......... .......... .......... .......... ..........  0% 46.7M 6m34s
>       1450K .......... .......... .......... .......... ..........  0% 35.2M 6m21s
>       1500K .......... .......... .......... .......... ..........  0% 31.9M 6m9s
>       1550K .......... .......... .......... .......... ..........  0% 35.2M 5m57s
>       1600K .......... .......... .......... .......... ..........  0% 35.5M 5m47s
>       1650K .......... .......... .......... .......... ..........  0% 31.5M 5m37s
>       1700K .......... .......... .......... .......... ..........  0% 47.0M 5m27s
>       1750K .......... .......... .......... .......... ..........  0% 37.7M 5m18s
>       1800K .......... .......... .......... .......... ..........  0% 1.69M 5m15s
>       1850K .......... .......... .......... .......... ..........  0%  313K 5m33s
>       1900K .......... .......... .......... .......... ..........  0% 36.3M 5m24s
>       1950K .......... .......... .......... .......... ..........  0% 43.8M 5m16s
>       2000K .......... .......... .......... .......... ..........  0% 52.6M 5m9s
>       2050K .......... .......... .......... .......... ..........  0% 40.9M 5m2s
>       2100K .......... .......... .......... .......... ..........  0% 50.5M 4m55s
>       2150K .......... .......... .......... .......... ..........  0% 32.0M 4m48s
>       2200K .......... .......... .......... .......... ..........  0% 37.8M 4m42s
>       2250K .......... .......... .......... .......... ..........  0% 38.1M 4m36s
>       2300K .......... .......... .......... .......... ..........  0% 47.9M 4m30s
>       2350K .......... .......... .......... .......... ..........  0% 43.0M 4m25s
>       2400K .......... .......... .......... .......... ..........  0% 53.6M 4m19s
>       2450K .......... .......... .......... .......... ..........  0% 45.8M 4m14s
>       2500K .......... .......... .......... .......... ..........  0% 49.9M 4m9s
>       2550K .......... .......... .......... .......... ..........  0%  271K 4m27s
>       2600K .......... .......... .......... .......... ..........  0% 38.5M 4m22s
>       2650K .......... .......... .......... .......... ..........  0% 30.8M 4m17s
>       2700K .......... .......... .......... .......... ..........  0% 45.4M 4m13s
>       2750K .......... .......... .......... .......... ..........  0% 43.0M 4m8s
>       2800K .......... .......... .......... .......... ..........  0% 45.7M 4m4s
>       2850K .......... .......... .......... .......... ..........  0% 42.3M 4m0s
>       2900K .......... .......... .......... .......... ..........  0% 52.0M 3m56s
>       2950K .......... .......... .......... .......... ..........  0% 45.9M 3m52s
>       3000K .......... .......... .......... .......... ..........  0% 45.4M 3m48s
>       3050K .......... .......... .......... .......... ..........  0% 39.2M 3m45s
>       3100K .......... .......... .......... .......... ..........  0% 39.9M 3m41s
>       3150K .......... .......... .......... .......... ..........  1% 39.6M 3m38s
>       3200K .......... .......... .......... .......... ..........  1% 38.3M 3m34s
>       3250K .......... .......... .......... .......... ..........  1% 42.0M 3m31s
>       3300K .......... .......... .......... .......... ..........  1% 37.4M 3m28s
>       3350K .......... .......... .......... .......... ..........  1% 1.77M 3m28s
>       3400K .......... .......... .......... .......... ..........  1% 44.2M 3m25s
>       3450K .......... .......... .......... .......... ..........  1% 58.4M 3m22s
>       3500K .......... .......... .......... .......... ..........  1% 54.8M 3m19s
>       3550K .......... .......... .......... .......... ..........  1% 46.2M 3m16s
>       3600K .......... .......... .......... .......... ..........  1%  329K 3m27s
>       3650K .......... .......... .......... .......... ..........  1% 42.4M 3m24s
>       3700K .......... .......... .......... .......... ..........  1% 36.7M 3m21s
>       3750K .......... .......... .......... .......... ..........  1% 41.0M 3m19s
>       3800K .......... .......... .......... .......... ..........  1% 52.3M 3m16s
>       3850K .......... .......... .......... .......... ..........  1% 38.1M 3m14s
>       3900K .......... .......... .......... .......... ..........  1% 43.1M 3m11s
>       3950K .......... .......... .......... .......... ..........  1% 41.9M 3m9s
>       4000K .......... .......... .......... .......... ..........  1% 41.1M 3m7s
>       4050K .......... .......... .......... .......... ..........  1% 46.0M 3m5s
>       4100K .......... .......... .......... .......... ..........  1% 45.1M 3m2s
>       4150K .......... .......... .......... .......... ..........  1% 47.4M 3m0s
>       4200K .......... .......... .......... .......... ..........  1% 42.3M 2m58s
>       4250K .......... .......... .......... .......... ..........  1% 45.7M 2m56s
>       4300K .......... .......... .......... .......... ..........  1% 29.9M 2m54s
>       4350K .......... .......... .......... .......... ..........  1% 41.1M 2m52s
>       4400K .......... .......... .......... .......... ..........  1% 49.3M 2m50s
>       4450K .......... .......... .......... .......... ..........  1% 45.5M 2m49s
>       4500K .......... .......... .......... .......... ..........  1% 53.4M 2m47s
>       4550K .......... .......... .......... .......... ..........  1% 54.2M 2m45s
>       4600K .......... .......... .......... .......... ..........  1% 2.17M 2m45s
>       4650K .......... .......... .......... .......... ..........  1% 48.1M 2m43s
>       4700K .......... .......... .......... .......... ..........  1% 40.4M 2m41s
>       4750K .......... .......... .......... .......... ..........  1% 41.9M 2m40s
>       4800K .......... .......... .......... .......... ..........  1% 48.7M 2m38s
>       4850K .......... .......... .......... .......... ..........  1% 44.0M 2m36s
>       4900K .......... .......... .......... .......... ..........  1% 48.4M 2m35s
>       4950K .......... .......... .......... .......... ..........  1%  334K 2m43s
>       5000K .......... .......... .......... .......... ..........  1% 37.8M 2m41s
>       5050K .......... .......... .......... .......... ..........  1% 45.1M 2m40s
>       5100K .......... .......... .......... .......... ..........  1% 43.0M 2m38s
>       5150K .......... .......... .......... .......... ..........  1% 40.6M 2m37s
>       5200K .......... .......... .......... .......... ..........  1% 45.9M 2m35s
>       5250K .......... .......... .......... .......... ..........  1% 44.4M 2m34s
>       5300K .......... .......... .......... .......... ..........  1% 39.0M 2m32s
>       5350K .......... .......... .......... .......... ..........  1% 38.7M 2m31s
>       5400K .......... .......... .......... .......... ..........  1% 38.1M 2m30s
>       5450K .......... .......... .......... .......... ..........  1% 36.6M 2m28s
>       5500K .......... .......... .......... .......... ..........  1% 41.4M 2m27s
>       5550K .......... .......... .......... .......... ..........  1% 18.3M 2m26s
>       5600K .......... .......... .......... .......... ..........  1% 49.4M 2m25s
>       5650K .......... .......... .......... .......... ..........  1% 46.2M 2m23s
>       5700K .......... .......... .......... .......... ..........  1% 43.4M 2m22s
>       5750K .......... .......... .......... .......... ..........  1% 35.5M 2m21s
>       5800K .......... .......... .......... .......... ..........  1% 42.6M 2m20s
>       5850K .......... .......... .......... .......... ..........  1% 54.0M 2m19s
>       5900K .......... .......... .......... .......... ..........  1% 40.3M 2m18s
>       5950K .......... .......... .......... .......... ..........  1% 47.5M 2m16s
>       6000K .......... .......... .......... .......... ..........  1% 32.3M 2m15s
>       6050K .......... .......... .......... .......... ..........  1% 37.1M 2m14s
>       6100K .......... .......... .......... .......... ..........  1% 42.5M 2m13s
>       6150K .......... .......... .......... .......... ..........  1% 37.2M 2m12s
>       6200K .......... .......... .......... .......... ..........  1% 43.5M 2m11s
>       6250K .......... .......... .......... .......... ..........  1% 50.1M 2m10s
>       6300K .......... .......... .......... .......... ..........  2% 50.8M 2m9s
>       6350K .......... .......... .......... .......... ..........  2% 3.95M 2m9s
>       6400K .......... .......... .......... .......... ..........  2% 34.7M 2m8s
>       6450K .......... .......... .......... .......... ..........  2% 40.0M 2m7s
>       6500K .......... .......... .......... .......... ..........  2% 32.9M 2m6s
>       6550K .......... .......... .......... .......... ..........  2% 43.7M 2m5s
>       6600K .......... .......... .......... .......... ..........  2% 48.6M 2m4s
>       6650K .......... .......... .......... .......... ..........  2% 47.6M 2m3s
>       6700K .......... .......... .......... .......... ..........  2% 45.1M 2m2s
>       6750K .......... .......... .......... .......... ..........  2% 50.3M 2m1s
>       6800K .......... .......... .......... .......... ..........  2%  341K 2m7s
>       6850K .......... .......... .......... .......... ..........  2% 39.4M 2m6s
>       6900K .......... .......... .......... .......... ..........  2% 38.5M 2m5s
>       6950K .......... .......... .......... .......... ..........  2% 48.9M 2m5s
>       7000K .......... .......... .......... .......... ..........  2% 46.9M 2m4s
>       7050K .......... .......... .......... .......... ..........  2% 42.6M 2m3s
>       7100K .......... .......... .......... .......... ..........  2% 46.3M 2m2s
>       7150K .......... .......... .......... .......... ..........  2% 49.1M 2m1s
>       7200K .......... .......... .......... .......... ..........  2% 54.7M 2m0s
>       7250K .......... .......... .......... .......... ..........  2% 47.0M 2m0s
>       7300K .......... .......... .......... .......... ..........  2% 56.2M 1m59s
>       7350K .......... .......... .......... .......... ..........  2% 59.8M 1m58s
>       7400K .......... .......... .......... .......... ..........  2% 38.3M 1m57s
>       7450K .......... .......... .......... .......... ..........  2% 47.3M 1m56s
>       7500K .......... .......... .......... .......... ..........  2% 53.7M 1m56s
>       7550K .......... .......... .......... .......... ..........  2% 38.8M 1m55s
>       7600K .......... .......... .......... .......... ..........  2% 32.5M 1m54s
>       7650K .......... .......... .......... .......... ..........  2% 45.7M 1m54s
>       7700K .......... .......... .......... .......... ..........  2% 47.0M 1m53s
>       7750K .......... .......... .......... .......... ..........  2% 1.41M 1m53s
>       7800K .......... .......... .......... .......... ..........  2% 60.3M 1m53s
>       7850K .......... .......... .......... .......... ..........  2% 54.1M 1m52s
>       7900K .......... .......... .......... .......... ..........  2% 59.6M 1m51s
>       7950K .......... .......... .......... .......... ..........  2% 66.5M 1m51s
>       8000K .......... .......... .......... .......... ..........  2% 49.2M 1m50s
>       8050K .......... .......... .......... .......... ..........  2% 72.0M 1m49s
>       8100K .......... .......... .......... .......... ..........  2% 67.8M 1m49s
>       8150K .......... .......... .......... .......... ..........  2% 60.7M 1m48s
>       8200K .......... .......... .......... .......... ..........  2% 54.3M 1m47s
>       8250K .......... .......... .......... .......... ..........  2% 53.8M 1m47s
>       8300K .......... .......... .......... .......... ..........  2% 67.1M 1m46s
>       8350K .......... .......... .......... .......... ..........  2% 60.0M 1m46s
>       8400K .......... .......... .......... .......... ..........  2% 50.5M 1m45s
>       8450K .......... .......... .......... .......... ..........  2% 59.6M 1m44s
>       8500K .......... .......... .......... .......... ..........  2% 59.5M 1m44s
>       8550K .......... .......... .......... .......... ..........  2% 60.3M 1m43s
>       8600K .......... .......... .......... .......... ..........  2% 59.6M 1m43s
>       8650K .......... .......... .......... .......... ..........  2% 59.6M 1m42s
>       8700K .......... .......... .......... .......... ..........  2% 65.9M 1m41s
>       8750K .......... .......... .......... .......... ..........  2% 55.2M 1m41s
>       8800K .......... .......... .......... .......... ..........  2% 60.1M 1m40s
>       8850K .......... .......... .......... .......... ..........  2% 59.5M 1m40s
>       8900K .......... .......... .......... .......... ..........  2% 1.59M 1m40s
>       8950K .......... .......... .......... .......... ..........  2%  513K 1m43s
>       9000K .......... .......... .......... .......... ..........  2% 45.0M 1m42s
>       9050K .......... .......... .......... .......... ..........  2% 45.6M 1m42s
>       9100K .......... .......... .......... .......... ..........  2% 54.6M 1m41s
>       9150K .......... .......... .......... .......... ..........  2% 55.2M 1m41s
>       9200K .......... .......... .......... .......... ..........  2% 54.6M 1m40s
>       9250K .......... .......... .......... .......... ..........  2% 59.8M 1m40s
>       9300K .......... .......... .......... .......... ..........  2% 43.7M 99s
>       9350K .......... .......... .......... .......... ..........  2% 45.4M 99s
>       9400K .......... .......... .......... .......... ..........  2% 40.4M 98s
>       9450K .......... .......... .......... .......... ..........  2% 41.2M 98s
>       9500K .......... .......... .......... .......... ..........  3% 46.6M 97s
>       9550K .......... .......... .......... .......... ..........  3% 44.2M 97s
>       9600K .......... .......... .......... .......... ..........  3% 54.2M 96s
>       9650K .......... .......... .......... .......... ..........  3% 55.0M 96s
>       9700K .......... .......... .......... .......... ..........  3% 61.3M 95s
>       9750K .......... .......... .......... .......... ..........  3% 55.4M 95s
>       9800K .......... .......... .......... .......... ..........  3% 54.2M 94s
>       9850K .......... .......... .......... .......... ..........  3% 46.7M 94s
>       9900K .......... .......... .......... .......... ..........  3% 66.9M 93s
>       9950K .......... .......... .......... .......... ..........  3% 46.0M 93s
>      10000K .......... .......... .......... .......... ..........  3% 37.5M 93s
>      10050K .......... .......... .......... .......... ..........  3% 56.9M 92s
>      10100K .......... .......... .......... .......... ..........  3% 61.4M 92s
>      10150K .......... .......... .......... .......... ..........  3% 74.5M 91s
>      10200K .......... .......... .......... .......... ..........  3% 1.59M 92s
>      10250K .......... .......... .......... .......... ..........  3% 35.3M 91s
>      10300K .......... .......... .......... .......... ..........  3% 54.7M 91s
>      10350K .......... .......... .......... .......... ..........  3% 37.9M 90s
>      10400K .......... .......... .......... .......... ..........  3% 50.4M 90s
>      10450K .......... .......... .......... .......... ..........  3% 55.8M 90s
>      10500K .......... .......... .......... .......... ..........  3% 47.1M 89s
>      10550K .......... .......... .......... .......... ..........  3% 49.1M 89s
>      10600K .......... .......... .......... .......... ..........  3% 19.3M 88s
>      10650K .......... .......... .......... .......... ..........  3% 49.2M 88s
>      10700K .......... .......... .......... .......... ..........  3% 56.3M 88s
>      10750K .......... .......... .......... .......... ..........  3% 59.6M 87s
>      10800K .......... .......... .......... .......... ..........  3% 43.5M 87s
>      10850K .......... .......... .......... .......... ..........  3% 59.7M 86s
>      10900K .......... .......... .......... .......... ..........  3% 49.7M 86s
>      10950K .......... .......... .......... .......... ..........  3% 59.5M 86s
>      11000K .......... .......... .......... .......... ..........  3% 66.2M 85s
>      11050K .......... .......... .......... .......... ..........  3% 59.8M 85s
>      11100K .......... .......... .......... .......... ..........  3% 45.8M 85s
>      11150K .......... .......... .......... .......... ..........  3% 59.7M 84s
>      11200K .......... .......... .......... .......... ..........  3% 38.2M 84s
>      11250K .......... .......... .......... .......... ..........  3% 40.5M 83s
>      11300K .......... .......... .......... .......... ..........  3% 42.3M 83s
>      11350K .......... .......... .......... .......... ..........  3% 38.7M 83s
>      11400K .......... .......... .......... .......... ..........  3% 71.0M 82s
>      11450K .......... .......... .......... .......... ..........  3% 55.1M 82s
>      11500K .......... .......... .......... .......... ..........  3% 61.3M 82s
>      11550K .......... .......... .......... .......... ..........  3% 66.8M 81s
>      11600K .......... .......... .......... .......... ..........  3% 65.9M 81s
>      11650K .......... .......... .......... .......... ..........  3% 59.7M 81s
>      11700K .......... .......... .......... .......... ..........  3% 43.7M 80s
>      11750K .......... .......... .......... .......... ..........  3%  432K 83s
>      11800K .......... .......... .......... .......... ..........  3% 42.0M 83s
>      11850K .......... .......... .......... .......... ..........  3% 52.5M 82s
>      11900K .......... .......... .......... .......... ..........  3% 48.3M 82s
>      11950K .......... .......... .......... .......... ..........  3% 55.4M 82s
>      12000K .......... .......... .......... .......... ..........  3% 48.7M 81s
>      12050K .......... .......... .......... .......... ..........  3% 47.8M 81s
>      12100K .......... .......... .......... .......... ..........  3% 51.7M 81s
>      12150K .......... .......... .......... .......... ..........  3% 53.1M 80s
>      12200K .......... .......... .......... .......... ..........  3% 54.9M 80s
>      12250K .......... .......... .......... .......... ..........  3% 52.8M 80s
>      12300K .......... .......... .......... .......... ..........  3% 59.7M 79s
>      12350K .......... .......... .......... .......... ..........  3% 41.4M 79s
>      12400K .......... .......... .......... .......... ..........  3% 9.09M 79s
>      12450K .......... .......... .......... .......... ..........  3% 61.1M 79s
>      12500K .......... .......... .......... .......... ..........  3% 51.9M 78s
>      12550K .......... .......... .......... .......... ..........  3% 21.5M 78s
>      12600K .......... .......... .......... .......... ..........  3%  178M 78s
>      12650K .......... .......... .......... .......... ..........  4% 65.6M 77s
>      12700K .......... .......... .......... .......... ..........  4% 51.3M 77s
>      12750K .......... .......... .......... .......... ..........  4% 54.8M 77s
>      12800K .......... .......... .......... .......... ..........  4% 35.7M 77s
>      12850K .......... .......... .......... .......... ..........  4% 49.8M 76s
>      12900K .......... .......... .......... .......... ..........  4% 49.8M 76s
>      12950K .......... .......... .......... .......... ..........  4% 55.7M 76s
>      13000K .......... .......... .......... .......... ..........  4% 54.7M 75s
>      13050K .......... .......... .......... .......... ..........  4% 49.5M 75s
>      13100K .......... .......... .......... .......... ..........  4% 50.8M 75s
>      13150K .......... .......... .......... .......... ..........  4% 49.3M 75s
>      13200K .......... .......... .......... .......... ..........  4% 47.2M 74s
>      13250K .......... .......... .......... .......... ..........  4% 59.7M 74s
>      13300K .......... .......... .......... .......... ..........  4% 2.29M 74s
>      13350K .......... .......... .......... .......... ..........  4% 40.1M 74s
>      13400K .......... .......... .......... .......... ..........  4% 45.0M 74s
>      13450K .......... .......... .......... .......... ..........  4% 39.7M 73s
>      13500K .......... .......... .......... .......... ..........  4% 37.1M 73s
>      13550K .......... .......... .......... .......... ..........  4% 36.5M 73s
>      13600K .......... .......... .......... .......... ..........  4% 44.8M 73s
>      13650K .......... .......... .......... .......... ..........  4% 32.8M 72s
>      13700K .......... .......... .......... .......... ..........  4% 33.5M 72s
>      13750K .......... .......... .......... .......... ..........  4% 42.0M 72s
>      13800K .......... .......... .......... .......... ..........  4% 44.9M 72s
>      13850K .......... .......... .......... .......... ..........  4% 43.0M 71s
>      13900K .......... .......... .......... .......... ..........  4% 5.95M 71s
>      13950K .......... .......... .......... .......... ..........  4% 61.9M 71s
>      14000K .......... .......... .......... .......... ..........  4% 50.8M 71s
>      14050K .......... .......... .......... .......... ..........  4% 56.9M 71s
>      14100K .......... .......... .......... .......... ..........  4% 45.5M 70s
>      14150K .......... .......... .......... .......... ..........  4% 54.4M 70s
>      14200K .......... .......... .......... .......... ..........  4% 52.3M 70s
>      14250K .......... .......... .......... .......... ..........  4% 43.4M 70s
>      14300K .......... .......... .......... .......... ..........  4% 47.1M 69s
>      14350K .......... .......... .......... .......... ..........  4% 38.0M 69s
>      14400K .......... .......... .......... .......... ..........  4% 43.6M 69s
>      14450K .......... .......... .......... .......... ..........  4% 61.2M 69s
>      14500K .......... .......... .......... .......... ..........  4% 50.6M 69s
>      14550K .......... .......... .......... .......... ..........  4% 51.9M 68s
>      14600K .......... .......... .......... .......... ..........  4% 66.3M 68s
>      14650K .......... .......... .......... .......... ..........  4% 54.3M 68s
>      14700K .......... .......... .......... .......... ..........  4% 48.1M 68s
>      14750K .......... .......... .......... .......... ..........  4% 66.9M 67s
>      14800K .......... .......... .......... .......... ..........  4%  467K 69s
>      14850K .......... .......... .......... .......... ..........  4% 31.5M 69s
>      14900K .......... .......... .......... .......... ..........  4% 53.0M 69s
>      14950K .......... .......... .......... .......... ..........  4% 48.9M 69s
>      15000K .......... .......... .......... .......... ..........  4% 49.0M 68s
>      15050K .......... .......... .......... .......... ..........  4% 50.3M 68s
>      15100K .......... .......... .......... .......... ..........  4% 52.0M 68s
>      15150K .......... .......... .......... .......... ..........  4% 41.2M 68s
>      15200K .......... .......... .......... .......... ..........  4% 63.6M 68s
>      15250K .......... .......... .......... .......... ..........  4% 39.1M 67s
>      15300K .......... .......... .......... .......... ..........  4% 62.2M 67s
>      15350K .......... .......... .......... .......... ..........  4% 3.63M 67s
>      15400K .......... .......... .......... .......... ..........  4% 62.5M 67s
>      15450K .......... .......... .......... .......... ..........  4% 50.7M 67s
>      15500K .......... .......... .......... .......... ..........  4% 55.6M 67s
>      15550K .......... .......... .......... .......... ..........  4% 51.2M 66s
>      15600K .......... .......... .......... .......... ..........  4% 65.4M 66s
>      15650K .......... .......... .......... .......... ..........  4% 59.3M 66s
>      15700K .......... .......... .......... .......... ..........  4% 66.7M 66s
>      15750K .......... .......... .......... .......... ..........  4% 54.3M 66s
>
>     *** WARNING: skipped 436825 bytes of output ***
>
>     300800K .......... .......... .......... .......... .......... 94%  107M 1s
>     300850K .......... .......... .......... .......... .......... 94% 1.82M 1s
>     300900K .......... .......... .......... .......... .......... 94% 74.0M 1s
>     300950K .......... .......... .......... .......... .......... 94% 92.8M 1s
>     301000K .......... .......... .......... .......... .......... 94%  109M 1s
>     301050K .......... .......... .......... .......... .......... 94% 89.4M 1s
>     301100K .......... .......... .......... .......... .......... 94%  102M 1s
>     301150K .......... .......... .......... .......... .......... 94%  121M 1s
>     301200K .......... .......... .......... .......... .......... 94% 84.2M 1s
>     301250K .......... .......... .......... .......... .......... 95%  120M 1s
>     301300K .......... .......... .......... .......... .......... 95%  663K 1s
>     301350K .......... .......... .......... .......... .......... 95% 2.40M 1s
>     301400K .......... .......... .......... .......... .......... 95%  119M 1s
>     301450K .......... .......... .......... .......... .......... 95%  123M 1s
>     301500K .......... .......... .......... .......... .......... 95% 96.0M 1s
>     301550K .......... .......... .......... .......... .......... 95%  117M 1s
>     301600K .......... .......... .......... .......... .......... 95%  118M 1s
>     301650K .......... .......... .......... .......... .......... 95%  115M 1s
>     301700K .......... .......... .......... .......... .......... 95%  106M 1s
>     301750K .......... .......... .......... .......... .......... 95% 16.6M 1s
>     301800K .......... .......... .......... .......... .......... 95%  105M 1s
>     301850K .......... .......... .......... .......... .......... 95%  118M 1s
>     301900K .......... .......... .......... .......... .......... 95%  115M 1s
>     301950K .......... .......... .......... .......... .......... 95%  103M 1s
>     302000K .......... .......... .......... .......... .......... 95%  119M 1s
>     302050K .......... .......... .......... .......... .......... 95% 99.3M 1s
>     302100K .......... .......... .......... .......... .......... 95%  118M 1s
>     302150K .......... .......... .......... .......... .......... 95% 5.84M 1s
>     302200K .......... .......... .......... .......... .......... 95% 37.7M 1s
>     302250K .......... .......... .......... .......... .......... 95% 39.0M 1s
>     302300K .......... .......... .......... .......... .......... 95% 95.0M 1s
>     302350K .......... .......... .......... .......... .......... 95% 90.0M 1s
>     302400K .......... .......... .......... .......... .......... 95%  107M 1s
>     302450K .......... .......... .......... .......... .......... 95% 86.0M 1s
>     302500K .......... .......... .......... .......... .......... 95% 8.42M 1s
>     302550K .......... .......... .......... .......... .......... 95% 44.1M 1s
>     302600K .......... .......... .......... .......... .......... 95% 4.58M 1s
>     302650K .......... .......... .......... .......... .......... 95% 8.27M 1s
>     302700K .......... .......... .......... .......... .......... 95%  102M 1s
>     302750K .......... .......... .......... .......... .......... 95% 77.8M 1s
>     302800K .......... .......... .......... .......... .......... 95% 77.4M 1s
>     302850K .......... .......... .......... .......... .......... 95%  124M 1s
>     302900K .......... .......... .......... .......... .......... 95%  116M 1s
>     302950K .......... .......... .......... .......... .......... 95%  100M 1s
>     303000K .......... .......... .......... .......... .......... 95%  119M 1s
>     303050K .......... .......... .......... .......... .......... 95% 9.31M 1s
>     303100K .......... .......... .......... .......... .......... 95%  122M 1s
>     303150K .......... .......... .......... .......... .......... 95%  117M 1s
>     303200K .......... .......... .......... .......... .......... 95%  105M 1s
>     303250K .......... .......... .......... .......... .......... 95%  120M 1s
>     303300K .......... .......... .......... .......... .......... 95% 98.5M 1s
>     303350K .......... .......... .......... .......... .......... 95%  120M 1s
>     303400K .......... .......... .......... .......... .......... 95%  121M 1s
>     303450K .......... .......... .......... .......... .......... 95%  108M 1s
>     303500K .......... .......... .......... .......... .......... 95% 4.64M 1s
>     303550K .......... .......... .......... .......... .......... 95% 43.1M 1s
>     303600K .......... .......... .......... .......... .......... 95% 70.4M 1s
>     303650K .......... .......... .......... .......... .......... 95% 28.2M 1s
>     303700K .......... .......... .......... .......... .......... 95% 83.9M 1s
>     303750K .......... .......... .......... .......... .......... 95%  101M 1s
>     303800K .......... .......... .......... .......... .......... 95% 97.6M 1s
>     303850K .......... .......... .......... .......... .......... 95%  101M 1s
>     303900K .......... .......... .......... .......... .......... 95%  114M 1s
>     303950K .......... .......... .......... .......... .......... 95% 1.81M 1s
>     304000K .......... .......... .......... .......... .......... 95% 77.7M 1s
>     304050K .......... .......... .......... .......... .......... 95%  117M 1s
>     304100K .......... .......... .......... .......... .......... 95%  124M 1s
>     304150K .......... .......... .......... .......... .......... 95% 83.3M 1s
>     304200K .......... .......... .......... .......... .......... 95% 99.6M 1s
>     304250K .......... .......... .......... .......... .......... 95%  119M 1s
>     304300K .......... .......... .......... .......... .......... 95% 95.8M 1s
>     304350K .......... .......... .......... .......... .......... 95%  664K 1s
>     304400K .......... .......... .......... .......... .......... 96% 1.84M 1s
>     304450K .......... .......... .......... .......... .......... 96% 77.0M 1s
>     304500K .......... .......... .......... .......... .......... 96% 87.6M 1s
>     304550K .......... .......... .......... .......... .......... 96% 84.3M 1s
>     304600K .......... .......... .......... .......... .......... 96% 80.8M 1s
>     304650K .......... .......... .......... .......... .......... 96% 90.2M 1s
>     304700K .......... .......... .......... .......... .......... 96% 85.7M 1s
>     304750K .......... .......... .......... .......... .......... 96%  341K 1s
>     304800K .......... .......... .......... .......... .......... 96%  122M 1s
>     304850K .......... .......... .......... .......... .......... 96% 1.41M 1s
>     304900K .......... .......... .......... .......... .......... 96%  124M 1s
>     304950K .......... .......... .......... .......... .......... 96% 96.9M 1s
>     305000K .......... .......... .......... .......... .......... 96%  126M 1s
>     305050K .......... .......... .......... .......... .......... 96%  119M 1s
>     305100K .......... .......... .......... .......... .......... 96% 49.6M 1s
>     305150K .......... .......... .......... .......... .......... 96%  119M 1s
>     305200K .......... .......... .......... .......... .......... 96% 99.2M 1s
>     305250K .......... .......... .......... .......... .......... 96%  120M 1s
>     305300K .......... .......... .......... .......... .......... 96% 98.7M 1s
>     305350K .......... .......... .......... .......... .......... 96%  117M 1s
>     305400K .......... .......... .......... .......... .......... 96%  120M 1s
>     305450K .......... .......... .......... .......... .......... 96%  121M 1s
>     305500K .......... .......... .......... .......... .......... 96%  111M 1s
>     305550K .......... .......... .......... .......... .......... 96%  106M 1s
>     305600K .......... .......... .......... .......... .......... 96%  121M 1s
>     305650K .......... .......... .......... .......... .......... 96%  109M 1s
>     305700K .......... .......... .......... .......... .......... 96%  108M 1s
>     305750K .......... .......... .......... .......... .......... 96%  118M 1s
>     305800K .......... .......... .......... .......... .......... 96% 12.7M 1s
>     305850K .......... .......... .......... .......... .......... 96%  108M 1s
>     305900K .......... .......... .......... .......... .......... 96%  123M 1s
>     305950K .......... .......... .......... .......... .......... 96%  108M 1s
>     306000K .......... .......... .......... .......... .......... 96%  106M 1s
>     306050K .......... .......... .......... .......... .......... 96% 97.5M 1s
>     306100K .......... .......... .......... .......... .......... 96%  158M 1s
>     306150K .......... .......... .......... .......... .......... 96%  101M 1s
>     306200K .......... .......... .......... .......... .......... 96%  119M 1s
>     306250K .......... .......... .......... .......... .......... 96%  119M 1s
>     306300K .......... .......... .......... .......... .......... 96% 98.5M 1s
>     306350K .......... .......... .......... .......... .......... 96%  120M 1s
>     306400K .......... .......... .......... .......... .......... 96%  117M 1s
>     306450K .......... .......... .......... .......... .......... 96% 98.3M 1s
>     306500K .......... .......... .......... .......... .......... 96%  117M 1s
>     306550K .......... .......... .......... .......... .......... 96%  127M 1s
>     306600K .......... .......... .......... .......... .......... 96%  110M 1s
>     306650K .......... .......... .......... .......... .......... 96% 20.4M 1s
>     306700K .......... .......... .......... .......... .......... 96%  119M 1s
>     306750K .......... .......... .......... .......... .......... 96%  113M 1s
>     306800K .......... .......... .......... .......... .......... 96%  131M 1s
>     306850K .......... .......... .......... .......... .......... 96% 97.1M 1s
>     306900K .......... .......... .......... .......... .......... 96%  119M 1s
>     306950K .......... .......... .......... .......... .......... 96%  112M 1s
>     307000K .......... .......... .......... .......... .......... 96%  106M 1s
>     307050K .......... .......... .......... .......... .......... 96%  119M 1s
>     307100K .......... .......... .......... .......... .......... 96% 99.9M 1s
>     307150K .......... .......... .......... .......... .......... 96%  118M 1s
>     307200K .......... .......... .......... .......... .......... 96% 95.6M 1s
>     307250K .......... .......... .......... .......... .......... 96%  159M 1s
>     307300K .......... .......... .......... .......... .......... 96%  106M 1s
>     307350K .......... .......... .......... .......... .......... 96%  112M 1s
>     307400K .......... .......... .......... .......... .......... 96%  118M 1s
>     307450K .......... .......... .......... .......... .......... 96%  112M 1s
>     307500K .......... .......... .......... .......... .......... 96%  104M 1s
>     307550K .......... .......... .......... .......... .......... 97%  122M 1s
>     307600K .......... .......... .......... .......... .......... 97%  111M 1s
>     307650K .......... .......... .......... .......... .......... 97%  103M 1s
>     307700K .......... .......... .......... .......... .......... 97%  110M 1s
>     307750K .......... .......... .......... .......... .......... 97%  120M 1s
>     307800K .......... .......... .......... .......... .......... 97%  373K 1s
>     307850K .......... .......... .......... .......... .......... 97% 83.9M 1s
>     307900K .......... .......... .......... .......... .......... 97% 1.42M 1s
>     307950K .......... .......... .......... .......... .......... 97% 68.5M 1s
>     308000K .......... .......... .......... .......... .......... 97%  110M 1s
>     308050K .......... .......... .......... .......... .......... 97%  118M 1s
>     308100K .......... .......... .......... .......... .......... 97%  110M 1s
>     308150K .......... .......... .......... .......... .......... 97% 51.7M 1s
>     308200K .......... .......... .......... .......... .......... 97%  102M 1s
>     308250K .......... .......... .......... .......... .......... 97%  121M 1s
>     308300K .......... .......... .......... .......... .......... 97%  125M 1s
>     308350K .......... .......... .......... .......... .......... 97% 94.7M 1s
>     308400K .......... .......... .......... .......... .......... 97%  122M 1s
>     308450K .......... .......... .......... .......... .......... 97%  119M 1s
>     308500K .......... .......... .......... .......... .......... 97%  111M 1s
>     308550K .......... .......... .......... .......... .......... 97%  106M 1s
>     308600K .......... .......... .......... .......... .......... 97%  119M 1s
>     308650K .......... .......... .......... .......... .......... 97% 6.50M 1s
>     308700K .......... .......... .......... .......... .......... 97% 88.2M 1s
>     308750K .......... .......... .......... .......... .......... 97% 99.6M 1s
>     308800K .......... .......... .......... .......... .......... 97%  119M 1s
>     308850K .......... .......... .......... .......... .......... 97% 97.8M 1s
>     308900K .......... .......... .......... .......... .......... 97%  111M 1s
>     308950K .......... .......... .......... .......... .......... 97%  105M 1s
>     309000K .......... .......... .......... .......... .......... 97%  111M 1s
>     309050K .......... .......... .......... .......... .......... 97%  109M 1s
>     309100K .......... .......... .......... .......... .......... 97% 15.6M 1s
>     309150K .......... .......... .......... .......... .......... 97%  134M 1s
>     309200K .......... .......... .......... .......... .......... 97%  131M 1s
>     309250K .......... .......... .......... .......... .......... 97%  138M 1s
>     309300K .......... .......... .......... .......... .......... 97%  105M 1s
>     309350K .......... .......... .......... .......... .......... 97%  122M 1s
>     309400K .......... .......... .......... .......... .......... 97%  110M 1s
>     309450K .......... .......... .......... .......... .......... 97%  107M 1s
>     309500K .......... .......... .......... .......... .......... 97%  118M 1s
>     309550K .......... .......... .......... .......... .......... 97% 93.9M 1s
>     309600K .......... .......... .......... .......... .......... 97%  102M 1s
>     309650K .......... .......... .......... .......... .......... 97% 90.0M 1s
>     309700K .......... .......... .......... .......... .......... 97% 81.0M 1s
>     309750K .......... .......... .......... .......... .......... 97%  114M 1s
>     309800K .......... .......... .......... .......... .......... 97%  135M 1s
>     309850K .......... .......... .......... .......... .......... 97%  169M 1s
>     309900K .......... .......... .......... .......... .......... 97%  147M 1s
>     309950K .......... .......... .......... .......... .......... 97%  125M 1s
>     310000K .......... .......... .......... .......... .......... 97%  116M 1s
>     310050K .......... .......... .......... .......... .......... 97%  103M 1s
>     310100K .......... .......... .......... .......... .......... 97%  123M 1s
>     310150K .......... .......... .......... .......... .......... 97% 97.2M 1s
>     310200K .......... .......... .......... .......... .......... 97%  120M 1s
>     310250K .......... .......... .......... .......... .......... 97%  118M 1s
>     310300K .......... .......... .......... .......... .......... 97%  120M 1s
>     310350K .......... .......... .......... .......... .......... 97% 97.1M 1s
>     310400K .......... .......... .......... .......... .......... 97%  122M 1s
>     310450K .......... .......... .......... .......... .......... 97% 3.43M 1s
>     310500K .......... .......... .......... .......... .......... 97%  111M 1s
>     310550K .......... .......... .......... .......... .......... 97%  130M 0s
>     310600K .......... .......... .......... .......... .......... 97% 98.9M 0s
>     310650K .......... .......... .......... .......... .......... 97%  125M 0s
>     310700K .......... .......... .......... .......... .......... 97%  115M 0s
>     310750K .......... .......... .......... .......... .......... 98% 98.4M 0s
>     310800K .......... .......... .......... .......... .......... 98%  113M 0s
>     310850K .......... .......... .......... .......... .......... 98%  432K 0s
>     310900K .......... .......... .......... .......... .......... 98% 59.9M 0s
>     310950K .......... .......... .......... .......... .......... 98% 1.43M 0s
>     311000K .......... .......... .......... .......... .......... 98% 60.4M 0s
>     311050K .......... .......... .......... .......... .......... 98%  116M 0s
>     311100K .......... .......... .......... .......... .......... 98% 86.8M 0s
>     311150K .......... .......... .......... .......... .......... 98%  120M 0s
>     311200K .......... .......... .......... .......... .......... 98% 97.2M 0s
>     311250K .......... .......... .......... .......... .......... 98% 57.5M 0s
>     311300K .......... .......... .......... .......... .......... 98%  117M 0s
>     311350K .......... .......... .......... .......... .......... 98%  121M 0s
>     311400K .......... .......... .......... .......... .......... 98%  100M 0s
>     311450K .......... .......... .......... .......... .......... 98%  120M 0s
>     311500K .......... .......... .......... .......... .......... 98%  118M 0s
>     311550K .......... .......... .......... .......... .......... 98% 84.3M 0s
>     311600K .......... .......... .......... .......... .......... 98%  121M 0s
>     311650K .......... .......... .......... .......... .......... 98%  119M 0s
>     311700K .......... .......... .......... .......... .......... 98%  101M 0s
>     311750K .......... .......... .......... .......... .......... 98% 3.46M 0s
>     311800K .......... .......... .......... .......... .......... 98% 91.2M 0s
>     311850K .......... .......... .......... .......... .......... 98% 27.5M 0s
>     311900K .......... .......... .......... .......... .......... 98% 99.5M 0s
>     311950K .......... .......... .......... .......... .......... 98%  117M 0s
>     312000K .......... .......... .......... .......... .......... 98%  122M 0s
>     312050K .......... .......... .......... .......... .......... 98% 98.8M 0s
>     312100K .......... .......... .......... .......... .......... 98%  117M 0s
>     312150K .......... .......... .......... .......... .......... 98% 99.2M 0s
>     312200K .......... .......... .......... .......... .......... 98% 3.39M 0s
>     312250K .......... .......... .......... .......... .......... 98%  109M 0s
>     312300K .......... .......... .......... .......... .......... 98%  109M 0s
>     312350K .......... .......... .......... .......... .......... 98%  118M 0s
>     312400K .......... .......... .......... .......... .......... 98%  108M 0s
>     312450K .......... .......... .......... .......... .......... 98%  109M 0s
>     312500K .......... .......... .......... .......... .......... 98%  108M 0s
>     312550K .......... .......... .......... .......... .......... 98%  106M 0s
>     312600K .......... .......... .......... .......... .......... 98%  120M 0s
>     312650K .......... .......... .......... .......... .......... 98%  122M 0s
>     312700K .......... .......... .......... .......... .......... 98%  115M 0s
>     312750K .......... .......... .......... .......... .......... 98%  101M 0s
>     312800K .......... .......... .......... .......... .......... 98%  121M 0s
>     312850K .......... .......... .......... .......... .......... 98% 97.8M 0s
>     312900K .......... .......... .......... .......... .......... 98%  119M 0s
>     312950K .......... .......... .......... .......... .......... 98%  114M 0s
>     313000K .......... .......... .......... .......... .......... 98% 29.5M 0s
>     313050K .......... .......... .......... .......... .......... 98% 98.4M 0s
>     313100K .......... .......... .......... .......... .......... 98%  123M 0s
>     313150K .......... .......... .......... .......... .......... 98%  123M 0s
>     313200K .......... .......... .......... .......... .......... 98% 98.2M 0s
>     313250K .......... .......... .......... .......... .......... 98%  120M 0s
>     313300K .......... .......... .......... .......... .......... 98%  118M 0s
>     313350K .......... .......... .......... .......... .......... 98%  109M 0s
>     313400K .......... .......... .......... .......... .......... 98%  109M 0s
>     313450K .......... .......... .......... .......... .......... 98%  119M 0s
>     313500K .......... .......... .......... .......... .......... 98%  115M 0s
>     313550K .......... .......... .......... .......... .......... 98%  101M 0s
>     313600K .......... .......... .......... .......... .......... 98%  121M 0s
>     313650K .......... .......... .......... .......... .......... 98%  110M 0s
>     313700K .......... .......... .......... .......... .......... 98%  107M 0s
>     313750K .......... .......... .......... .......... .......... 98%  119M 0s
>     313800K .......... .......... .......... .......... .......... 98%  114M 0s
>     313850K .......... .......... .......... .......... .......... 98%  102M 0s
>     313900K .......... .......... .......... .......... .......... 99% 6.03M 0s
>     313950K .......... .......... .......... .......... .......... 99%  494K 0s
>     314000K .......... .......... .......... .......... .......... 99% 90.2M 0s
>     314050K .......... .......... .......... .......... .......... 99% 1.41M 0s
>     314100K .......... .......... .......... .......... .......... 99%  130M 0s
>     314150K .......... .......... .......... .......... .......... 99% 85.4M 0s
>     314200K .......... .......... .......... .......... .......... 99% 89.1M 0s
>     314250K .......... .......... .......... .......... .......... 99%  121M 0s
>     314300K .......... .......... .......... .......... .......... 99% 60.9M 0s
>     314350K .......... .......... .......... .......... .......... 99% 85.0M 0s
>     314400K .......... .......... .......... .......... .......... 99%  117M 0s
>     314450K .......... .......... .......... .......... .......... 99% 97.0M 0s
>     314500K .......... .......... .......... .......... .......... 99%  123M 0s
>     314550K .......... .......... .......... .......... .......... 99%  119M 0s
>     314600K .......... .......... .......... .......... .......... 99% 98.7M 0s
>     314650K .......... .......... .......... .......... .......... 99%  102M 0s
>     314700K .......... .......... .......... .......... .......... 99%  102M 0s
>     314750K .......... .......... .......... .......... .......... 99%  120M 0s
>     314800K .......... .......... .......... .......... .......... 99% 2.52M 0s
>     314850K .......... .......... .......... .......... .......... 99%  119M 0s
>     314900K .......... .......... .......... .......... .......... 99%  117M 0s
>     314950K .......... .......... .......... .......... .......... 99%  105M 0s
>     315000K .......... .......... .......... .......... .......... 99%  117M 0s
>     315050K .......... .......... .......... .......... .......... 99%  113M 0s
>     315100K .......... .......... .......... .......... .......... 99%  101M 0s
>     315150K .......... .......... .......... .......... .......... 99%  125M 0s
>     315200K .......... .......... .......... .......... .......... 99%  117M 0s
>     315250K .......... .......... .......... .......... .......... 99% 4.55M 0s
>     315300K .......... .......... .......... .......... .......... 99%  122M 0s
>     315350K .......... .......... .......... .......... .......... 99% 83.8M 0s
>     315400K .......... .......... .......... .......... .......... 99%  119M 0s
>     315450K .......... .......... .......... .......... .......... 99% 96.0M 0s
>     315500K .......... .......... .......... .......... .......... 99%  128M 0s
>     315550K .......... .......... .......... .......... .......... 99%  120M 0s
>     315600K .......... .......... .......... .......... .......... 99% 98.2M 0s
>     315650K .......... .......... .......... .......... .......... 99%  120M 0s
>     315700K .......... .......... .......... .......... .......... 99%  121M 0s
>     315750K .......... .......... .......... .......... .......... 99%  119M 0s
>     315800K .......... .......... .......... .......... .......... 99% 95.1M 0s
>     315850K .......... .......... .......... .......... .......... 99%  119M 0s
>     315900K .......... .......... .......... .......... .......... 99%  122M 0s
>     315950K .......... .......... .......... .......... .......... 99% 98.7M 0s
>     316000K .......... .......... .......... .......... .......... 99%  112M 0s
>     316050K .......... .......... .......... .......... .......... 99%  128M 0s
>     316100K .......... .......... .......... .......... .......... 99% 27.9M 0s
>     316150K .......... .......... .......... .......... .......... 99% 12.6M 0s
>     316200K .......... .......... .......... .......... .......... 99%  129M 0s
>     316250K .......... .......... .......... .......... .......... 99%  105M 0s
>     316300K .......... .......... .......... .......... .......... 99%  112M 0s
>     316350K .......... .......... .......... .......... .......... 99%  101M 0s
>     316400K .......... .......... .......... .......... .......... 99%  123M 0s
>     316450K .......... .......... .......... .......... .......... 99%  117M 0s
>     316500K .......... .......... .......... .......... .......... 99%  109M 0s
>     316550K .......... .......... .......... .......... .......... 99%  109M 0s
>     316600K .......... .......... .......... .......... .......... 99% 49.1M 0s
>     316650K .......... .......... .......... .......... .......... 99%  121M 0s
>     316700K .......... .......... .......... .......... .......... 99%  120M 0s
>     316750K .......... .......... .......... .......... .......... 99%  119M 0s
>     316800K .......... .......... .......... .......... .......... 99% 98.9M 0s
>     316850K .......... .......... .......... .......... .......... 99%  122M 0s
>     316900K .......... .......... .......... .......... .......... 99%  107M 0s
>     316950K .......... .......... .......... .......... .......... 99%  101M 0s
>     317000K .......... .......... .......... .......... .......... 99%  478K 0s
>     317050K .......... .......... .......... .......... .......... 99% 71.5M 0s
>     317100K .......... ...                                        100% 96.2M=24s
>
>     2017-11-07 21:43:18 (12.8 MB/s) - ‘OB_tei_7-2_CC-BY-NC.zip’ saved [324723900/324723900]

``` sh
pwd && ls -al
```

>     /databricks/driver
>     total 318332
>     drwxr-xr-x 1 root root      4096 Nov  7 21:42 .
>     drwxr-xr-x 1 root root      4096 Nov  7 20:38 ..
>     drwxr-xr-x 2 root root      4096 Oct 10 14:44 conf
>     -rw-r--r-- 1 root root       731 Nov  7 20:38 derby.log
>     drwxr-xr-x 3 root root      4096 Nov  7 20:38 eventlogs
>     drwxr-xr-x 2 root root      4096 Nov  7 21:30 ganglia
>     drwxr-xr-x 2 root root      4096 Nov  7 21:00 logs
>     drwxrwxr-x 3 root root      4096 Aug 26  2016 obo-tiny
>     -rw-r--r-- 1 root root 324723900 Sep  2  2016 OB_tei_7-2_CC-BY-NC.zip
>     -rw-r--r-- 1 root root   1209487 Nov  7 21:20 OB-tiny_tei_7-2_CC-BY-NC.zip

Make sure you comment/uncomment the right files depending on wheter you have downloaded the tiny dataset or the big one.

``` sh
# unzip OB-tiny_tei_7-2_CC-BY-NC.zip
unzip OB_tei_7-2_CC-BY-NC.zip
```

>     Archive:  OB_tei_7-2_CC-BY-NC.zip
>        creating: tei/
>        creating: tei/ordinarysAccounts/
>       inflating: tei/ordinarysAccounts/OA16930726.xml  
>       inflating: tei/ordinarysAccounts/OA17480622.xml  
>       inflating: tei/ordinarysAccounts/OA17150520.xml  
>       inflating: tei/ordinarysAccounts/OA17310514.xml  
>       inflating: tei/ordinarysAccounts/OA17160713.xml  
>       inflating: tei/ordinarysAccounts/OA17450709.xml  
>       inflating: tei/ordinarysAccounts/OA17070124.xml  
>       inflating: tei/ordinarysAccounts/OA16760830.xml  
>       inflating: tei/ordinarysAccounts/OA17261103.xml  
>       inflating: tei/ordinarysAccounts/OA17230909.xml  
>       inflating: tei/ordinarysAccounts/OA17500326.xml  
>       inflating: tei/ordinarysAccounts/OA16911218.xml  
>       inflating: tei/ordinarysAccounts/OA17280626.xml  
>       inflating: tei/ordinarysAccounts/OA16930127.xml  
>       inflating: tei/ordinarysAccounts/OA17250430.xml  
>       inflating: tei/ordinarysAccounts/OA17141222.xml  
>       inflating: tei/ordinarysAccounts/OA17511023.xml  
>       inflating: tei/ordinarysAccounts/OA17131223.xml  
>       inflating: tei/ordinarysAccounts/OA16860917.xml  
>       inflating: tei/ordinarysAccounts/OA17630504.xml  
>       inflating: tei/ordinarysAccounts/OA17280212.xml  
>       inflating: tei/ordinarysAccounts/OA16970915.xml  
>       inflating: tei/ordinarysAccounts/OA17370303.xml  
>       inflating: tei/ordinarysAccounts/OA17320605.xml  
>       inflating: tei/ordinarysAccounts/OA17150202.xml  
>       inflating: tei/ordinarysAccounts/OA17550728.xml  
>       inflating: tei/ordinarysAccounts/OA16900613.xml  
>       inflating: tei/ordinarysAccounts/OA17170626.xml  
>       inflating: tei/ordinarysAccounts/OA17060515.xml  
>       inflating: tei/ordinarysAccounts/OA16970528.xml  
>       inflating: tei/ordinarysAccounts/OA16840526.xml  
>       inflating: tei/ordinarysAccounts/OA17320809.xml  
>       inflating: tei/ordinarysAccounts/OA16971103.xml  
>       inflating: tei/ordinarysAccounts/OA17080428.xml  
>       inflating: tei/ordinarysAccounts/OA17081027.xml  
>       inflating: tei/ordinarysAccounts/OA16970423.xml  
>       inflating: tei/ordinarysAccounts/OA16911023.xml  
>       inflating: tei/ordinarysAccounts/OA17520713.xml  
>       inflating: tei/ordinarysAccounts/OA16870127.xml  
>       inflating: tei/ordinarysAccounts/OA16971222.xml  
>       inflating: tei/ordinarysAccounts/OA17120527a.xml  
>       inflating: tei/ordinarysAccounts/OA17581002.xml  
>       inflating: tei/ordinarysAccounts/OA16791219.xml  
>       inflating: tei/ordinarysAccounts/OA17121223.xml  
>       inflating: tei/ordinarysAccounts/OA17200413.xml  
>       inflating: tei/ordinarysAccounts/OA16910928.xml  
>       inflating: tei/ordinarysAccounts/OA17061213.xml  
>       inflating: tei/ordinarysAccounts/OA17301116.xml  
>       inflating: tei/ordinarysAccounts/OA17130313.xml  
>       inflating: tei/ordinarysAccounts/OA16931023.xml  
>       inflating: tei/ordinarysAccounts/OA17000720.xml  
>       inflating: tei/ordinarysAccounts/OA17470121.xml  
>       inflating: tei/ordinarysAccounts/OA17420712.xml  
>       inflating: tei/ordinarysAccounts/OA17290207.xml  
>       inflating: tei/ordinarysAccounts/OA17170520.xml  
>       inflating: tei/ordinarysAccounts/OA17180531.xml  
>       inflating: tei/ordinarysAccounts/OA17540626.xml  
>       inflating: tei/ordinarysAccounts/OA17121031.xml  
>       inflating: tei/ordinarysAccounts/OA17111024.xml  
>       inflating: tei/ordinarysAccounts/OA17321009.xml  
>       inflating: tei/ordinarysAccounts/OA17460425.xml  
>       inflating: tei/ordinarysAccounts/OA17521011.xml  
>       inflating: tei/ordinarysAccounts/OA17530528.xml  
>       inflating: tei/ordinarysAccounts/OA17340211.xml  
>       inflating: tei/ordinarysAccounts/OA17531210.xml  
>       inflating: tei/ordinarysAccounts/OA17380118.xml  
>       inflating: tei/ordinarysAccounts/OA17080924.xml  
>       inflating: tei/ordinarysAccounts/OA17320306.xml  
>       inflating: tei/ordinarysAccounts/OA17560920.xml  
>       inflating: tei/ordinarysAccounts/OA17230208.xml  
>       inflating: tei/ordinarysAccounts/OA17000419.xml  
>       inflating: tei/ordinarysAccounts/OA17230617.xml  
>       inflating: tei/ordinarysAccounts/OA16840710.xml  
>       inflating: tei/ordinarysAccounts/OA17560223.xml  
>       inflating: tei/ordinarysAccounts/OA17550512.xml  
>       inflating: tei/ordinarysAccounts/OA16950417.xml  
>       inflating: tei/ordinarysAccounts/OA16921026.xml  
>       inflating: tei/ordinarysAccounts/OA16850304.xml  
>       inflating: tei/ordinarysAccounts/OA17640611.xml  
>       inflating: tei/ordinarysAccounts/OA16920909.xml  
>       inflating: tei/ordinarysAccounts/OA17190722.xml  
>       inflating: tei/ordinarysAccounts/OA16891023.xml  
>       inflating: tei/ordinarysAccounts/OA17440217.xml  
>       inflating: tei/ordinarysAccounts/OA17420407.xml  
>       inflating: tei/ordinarysAccounts/OA17440315.xml  
>       inflating: tei/ordinarysAccounts/OA17530416.xml  
>       inflating: tei/ordinarysAccounts/OA17520601.xml  
>       inflating: tei/ordinarysAccounts/OA16841017.xml  
>       inflating: tei/ordinarysAccounts/OA16840917.xml  
>       inflating: tei/ordinarysAccounts/OA17190525.xml  
>       inflating: tei/ordinarysAccounts/OA17331219.xml  
>       inflating: tei/ordinarysAccounts/OA16780906.xml  
>       inflating: tei/ordinarysAccounts/OA17480318.xml  
>       inflating: tei/ordinarysAccounts/OA16870413.xml  
>       inflating: tei/ordinarysAccounts/OA17331006.xml  
>       inflating: tei/ordinarysAccounts/OA17020529.xml  
>       inflating: tei/ordinarysAccounts/OA17600428.xml  
>       inflating: tei/ordinarysAccounts/OA17420507.xml  
>       inflating: tei/ordinarysAccounts/OA17440608.xml  
>       inflating: tei/ordinarysAccounts/OA17251103.xml  
>       inflating: tei/ordinarysAccounts/OA17640711.xml  
>       inflating: tei/ordinarysAccounts/OA17500706.xml  
>       inflating: tei/ordinarysAccounts/OA17180317.xml  
>       inflating: tei/ordinarysAccounts/OA17310616.xml  
>       inflating: tei/ordinarysAccounts/OA17211222.xml  
>       inflating: tei/ordinarysAccounts/OA16790121.xml  
>       inflating: tei/ordinarysAccounts/OA16980126.xml  
>       inflating: tei/ordinarysAccounts/OA17050309.xml  
>       inflating: tei/ordinarysAccounts/OA17241207.xml  
>       inflating: tei/ordinarysAccounts/OA17000719.xml  
>       inflating: tei/ordinarysAccounts/OA17070718.xml  
>       inflating: tei/ordinarysAccounts/OA16940228.xml  
>       inflating: tei/ordinarysAccounts/OA17181031.xml  
>       inflating: tei/ordinarysAccounts/OA16901024.xml  
>       inflating: tei/ordinarysAccounts/OA17200919.xml  
>       inflating: tei/ordinarysAccounts/OA17360705.xml  
>       inflating: tei/ordinarysAccounts/OA16850724.xml  
>       inflating: tei/ordinarysAccounts/OA16970716.xml  
>       inflating: tei/ordinarysAccounts/OA17270811.xml  
>       inflating: tei/ordinarysAccounts/OA16771219.xml  
>       inflating: tei/ordinarysAccounts/OA17280911.xml  
>       inflating: tei/ordinarysAccounts/OA17210512.xml  
>       inflating: tei/ordinarysAccounts/OA17391221.xml  
>       inflating: tei/ordinarysAccounts/OA17250201.xml  
>       inflating: tei/ordinarysAccounts/OA17220208.xml  
>       inflating: tei/ordinarysAccounts/OA17090518.xml  
>       inflating: tei/ordinarysAccounts/OA17210403.xml  
>       inflating: tei/ordinarysAccounts/OA17140310.xml  
>       inflating: tei/ordinarysAccounts/OA17270918.xml  
>       inflating: tei/ordinarysAccounts/OA17520922.xml  
>       inflating: tei/ordinarysAccounts/OA17441005.xml  
>       inflating: tei/ordinarysAccounts/OA17460404.xml  
>       inflating: tei/ordinarysAccounts/OA17200815.xml  
>       inflating: tei/ordinarysAccounts/OA17000301.xml  
>       inflating: tei/ordinarysAccounts/OA17260803.xml  
>       inflating: tei/ordinarysAccounts/OA16790307.xml  
>       inflating: tei/ordinarysAccounts/OA17290324.xml  
>       inflating: tei/ordinarysAccounts/OA17050207.xml  
>       inflating: tei/ordinarysAccounts/OA17621013.xml  
>       inflating: tei/ordinarysAccounts/OA17170201.xml  
>       inflating: tei/ordinarysAccounts/OA17230408.xml  
>       inflating: tei/ordinarysAccounts/OA17420113.xml  
>       inflating: tei/ordinarysAccounts/OA17061025.xml  
>       inflating: tei/ordinarysAccounts/OA17481028.xml  
>       inflating: tei/ordinarysAccounts/OA17470731.xml  
>       inflating: tei/ordinarysAccounts/OA17591119.xml  
>       inflating: tei/ordinarysAccounts/OA16950712.xml  
>       inflating: tei/ordinarysAccounts/OA17390314.xml  
>       inflating: tei/ordinarysAccounts/OA17150511.xml  
>       inflating: tei/ordinarysAccounts/OA17130925.xml  
>       inflating: tei/ordinarysAccounts/OA16790122.xml  
>       inflating: tei/ordinarysAccounts/OA17050504.xml  
>       inflating: tei/ordinarysAccounts/OA17160608.xml  
>       inflating: tei/ordinarysAccounts/OA17110421.xml  
>       inflating: tei/ordinarysAccounts/OA16840523.xml  
>       inflating: tei/ordinarysAccounts/OA17030721.xml  
>       inflating: tei/ordinarysAccounts/OA17070912.xml  
>       inflating: tei/ordinarysAccounts/OA17240828.xml  
>       inflating: tei/ordinarysAccounts/OA17360726.xml  
>       inflating: tei/ordinarysAccounts/OA16900912.xml  
>       inflating: tei/ordinarysAccounts/OA17110811.xml  
>       inflating: tei/ordinarysAccounts/OA17260218.xml  
>       inflating: tei/ordinarysAccounts/OA17400507.xml  
>       inflating: tei/ordinarysAccounts/OA16790509.xml  
>       inflating: tei/ordinarysAccounts/OA17470617.xml  
>       inflating: tei/ordinarysAccounts/OA16801027.xml  
>       inflating: tei/ordinarysAccounts/OA17001220.xml  
>       inflating: tei/ordinarysAccounts/OA17640307.xml  
>       inflating: tei/ordinarysAccounts/OA17190223.xml  
>       inflating: tei/ordinarysAccounts/OA17291222.xml  
>       inflating: tei/ordinarysAccounts/OA17571123.xml  
>       inflating: tei/ordinarysAccounts/OA17360524.xml  
>       inflating: tei/ordinarysAccounts/OA17381222.xml  
>       inflating: tei/ordinarysAccounts/OA17031103.xml  
>       inflating: tei/ordinarysAccounts/OA17210705.xml  
>       inflating: tei/ordinarysAccounts/OA17120527.xml  
>       inflating: tei/ordinarysAccounts/OA17251222.xml  
>       inflating: tei/ordinarysAccounts/OA17220924.xml  
>       inflating: tei/ordinarysAccounts/OA17410914.xml  
>       inflating: tei/ordinarysAccounts/OA16910603.xml  
>       inflating: tei/ordinarysAccounts/OA17410612.xml  
>       inflating: tei/ordinarysAccounts/OA17270213.xml  
>       inflating: tei/ordinarysAccounts/OA17300512.xml  
>       inflating: tei/ordinarysAccounts/OA17260912.xml  
>       inflating: tei/ordinarysAccounts/OA17120919.xml  
>       inflating: tei/ordinarysAccounts/OA17040621.xml  
>       inflating: tei/ordinarysAccounts/OA16770316.xml  
>       inflating: tei/ordinarysAccounts/OA17260509.xml  
>       inflating: tei/ordinarysAccounts/OA17400806.xml  
>       inflating: tei/ordinarysAccounts/OA17140421.xml  
>       inflating: tei/ordinarysAccounts/OA17410731.xml  
>       inflating: tei/ordinarysAccounts/OA16940124.xml  
>       inflating: tei/ordinarysAccounts/OA17511111.xml  
>       inflating: tei/ordinarysAccounts/OA17160919.xml  
>       inflating: tei/ordinarysAccounts/OA17470729.xml  
>       inflating: tei/ordinarysAccounts/OA17290725.xml  
>       inflating: tei/ordinarysAccounts/OA17341002.xml  
>       inflating: tei/ordinarysAccounts/OA16860602.xml  
>       inflating: tei/ordinarysAccounts/OA17301007.xml  
>       inflating: tei/ordinarysAccounts/OA16780123.xml  
>       inflating: tei/ordinarysAccounts/OA17510325.xml  
>       inflating: tei/ordinarysAccounts/OA16920302.xml  
>       inflating: tei/ordinarysAccounts/OA17601208.xml  
>       inflating: tei/ordinarysAccounts/OA17380526.xml  
>       inflating: tei/ordinarysAccounts/OA17460801.xml  
>       inflating: tei/ordinarysAccounts/OA17140129.xml  
>       inflating: tei/ordinarysAccounts/OA16980309.xml  
>       inflating: tei/ordinarysAccounts/OA17501107.xml  
>       inflating: tei/ordinarysAccounts/OA17201026.xml  
>       inflating: tei/ordinarysAccounts/OA17630615.xml  
>       inflating: tei/ordinarysAccounts/OA17030811.xml  
>       inflating: tei/ordinarysAccounts/OA16980803.xml  
>       inflating: tei/ordinarysAccounts/OA17060719.xml  
>       inflating: tei/ordinarysAccounts/OA16851023.xml  
>       inflating: tei/ordinarysAccounts/OA17211023.xml  
>       inflating: tei/ordinarysAccounts/OA17171220.xml  
>       inflating: tei/ordinarysAccounts/OA17101215.xml  
>       inflating: tei/ordinarysAccounts/OA17591003.xml  
>       inflating: tei/ordinarysAccounts/OA17491018.xml  
>       inflating: tei/ordinarysAccounts/OA17520323.xml  
>       inflating: tei/ordinarysAccounts/OA17080128.xml  
>       inflating: tei/ordinarysAccounts/OA17330425.xml  
>       inflating: tei/ordinarysAccounts/OA17700604.xml  
>       inflating: tei/ordinarysAccounts/OA16800121.xml  
>       inflating: tei/ordinarysAccounts/OA17611005.xml  
>       inflating: tei/ordinarysAccounts/OA17371005.xml  
>       inflating: tei/ordinarysAccounts/OA17120801.xml  
>       inflating: tei/ordinarysAccounts/OA17570518.xml  
>       inflating: tei/ordinarysAccounts/OA17500207.xml  
>       inflating: tei/ordinarysAccounts/OA16910918.xml  
>       inflating: tei/ordinarysAccounts/OA16910717.xml  
>       inflating: tei/ordinarysAccounts/OA17540805.xml  
>       inflating: tei/ordinarysAccounts/OA16850904.xml  
>       inflating: tei/ordinarysAccounts/OA16930616.xml  
>       inflating: tei/ordinarysAccounts/OA17430412.xml  
>       inflating: tei/ordinarysAccounts/OA17110808.xml  
>       inflating: tei/ordinarysAccounts/OA16851216.xml  
>       inflating: tei/ordinarysAccounts/OA17600915.xml  
>       inflating: tei/ordinarysAccounts/OA17180317a.xml  
>       inflating: tei/ordinarysAccounts/OA17510211.xml  
>       inflating: tei/ordinarysAccounts/OA17340709.xml  
>       inflating: tei/ordinarysAccounts/OA17510729.xml  
>       inflating: tei/ordinarysAccounts/OA17430518.xml  
>       inflating: tei/ordinarysAccounts/OA16870525.xml  
>       inflating: tei/ordinarysAccounts/OA16910501.xml  
>       inflating: tei/ordinarysAccounts/OA17070912A.xml  
>       inflating: tei/ordinarysAccounts/OA17330528.xml  
>       inflating: tei/ordinarysAccounts/OA17370629.xml  
>       inflating: tei/ordinarysAccounts/OA17310726.xml  
>       inflating: tei/ordinarysAccounts/OA16861025.xml  
>       inflating: tei/ordinarysAccounts/OA16770504.xml  
>       inflating: tei/ordinarysAccounts/OA17091216.xml  
>       inflating: tei/ordinarysAccounts/OA17321016.xml  
>       inflating: tei/ordinarysAccounts/OA17521113.xml  
>       inflating: tei/ordinarysAccounts/OA17500808.xml  
>       inflating: tei/ordinarysAccounts/OA16760517.xml  
>       inflating: tei/ordinarysAccounts/OA16871216.xml  
>       inflating: tei/ordinarysAccounts/OA17110718.xml  
>       inflating: tei/ordinarysAccounts/OA17381108.xml  
>       inflating: tei/ordinarysAccounts/OA17291121.xml  
>       inflating: tei/ordinarysAccounts/OA17180127.xml  
>       inflating: tei/ordinarysAccounts/OA17580501.xml  
>       inflating: tei/ordinarysAccounts/OA17100726.xml  
>       inflating: tei/ordinarysAccounts/OA17141027.xml  
>       inflating: tei/ordinarysAccounts/OA17140922.xml  
>       inflating: tei/ordinarysAccounts/OA17650417.xml  
>       inflating: tei/ordinarysAccounts/OA17240904.xml  
>       inflating: tei/ordinarysAccounts/OA16901222.xml  
>       inflating: tei/ordinarysAccounts/OA17520113.xml  
>       inflating: tei/ordinarysAccounts/OA17020128.xml  
>       inflating: tei/ordinarysAccounts/OA17151223.xml  
>       inflating: tei/ordinarysAccounts/OA17431121.xml  
>       inflating: tei/ordinarysAccounts/OA17130429.xml  
>       inflating: tei/ordinarysAccounts/OA17490220.xml  
>       inflating: tei/ordinarysAccounts/OA17720527.xml  
>       inflating: tei/ordinarysAccounts/OA17501231.xml  
>       inflating: tei/ordinarysAccounts/OA16850506.xml  
>       inflating: tei/ordinarysAccounts/OA17330305.xml  
>       inflating: tei/ordinarysAccounts/OA16950918.xml  
>       inflating: tei/ordinarysAccounts/OA17180527.xml  
>       inflating: tei/ordinarysAccounts/OA17040922.xml  
>       inflating: tei/ordinarysAccounts/OA17200627.xml  
>       inflating: tei/ordinarysAccounts/OA17250524.xml  
>       inflating: tei/ordinarysAccounts/OA17180806.xml  
>       inflating: tei/ordinarysAccounts/OA17380308.xml  
>       inflating: tei/ordinarysAccounts/OA16950524.xml  
>       inflating: tei/ordinarysAccounts/OA16891221.xml  
>       inflating: tei/ordinarysAccounts/OA16791024.xml  
>       inflating: tei/ordinarysAccounts/OA17191106.xml  
>       inflating: tei/ordinarysAccounts/OA17081217.xml  
>       inflating: tei/ordinarysAccounts/OA17220521.xml  
>       inflating: tei/ordinarysAccounts/OA17621110.xml  
>       inflating: tei/ordinarysAccounts/OA17530212.xml  
>       inflating: tei/ordinarysAccounts/OA17090916.xml  
>       inflating: tei/ordinarysAccounts/OA17190213.xml  
>       inflating: tei/ordinarysAccounts/OA16900723.xml  
>       inflating: tei/ordinarysAccounts/OA17490426.xml  
>       inflating: tei/ordinarysAccounts/OA17100317.xml  
>       inflating: tei/ordinarysAccounts/OA17110302.xml  
>       inflating: tei/ordinarysAccounts/OA17450607.xml  
>       inflating: tei/ordinarysAccounts/OA17280327.xml  
>       inflating: tei/ordinarysAccounts/OA17100915.xml  
>       inflating: tei/ordinarysAccounts/OA17520427.xml  
>       inflating: tei/ordinarysAccounts/OA17230805.xml  
>       inflating: tei/ordinarysAccounts/OA17360204.xml  
>       inflating: tei/ordinarysAccounts/OA16910126.xml  
>       inflating: tei/ordinarysAccounts/OA16850506A.xml  
>       inflating: tei/ordinarysAccounts/OA17340308.xml  
>       inflating: tei/ordinarysAccounts/OA17151028.xml  
>       inflating: tei/ordinarysAccounts/OA16951213.xml  
>       inflating: tei/ordinarysAccounts/OA17540204.xml  
>       inflating: tei/ordinarysAccounts/OA17161219.xml  
>       inflating: tei/ordinarysAccounts/OA17280520.xml  
>       inflating: tei/ordinarysAccounts/OA17240203.xml  
>       inflating: tei/ordinarysAccounts/OA17040510.xml  
>       inflating: tei/ordinarysAccounts/OA17041220.xml  
>       inflating: tei/ordinarysAccounts/OA17120618.xml  
>       inflating: tei/ordinarysAccounts/OA16761025.xml  
>       inflating: tei/ordinarysAccounts/OA17721014.xml  
>       inflating: tei/ordinarysAccounts/OA17311220.xml  
>       inflating: tei/ordinarysAccounts/OA17631228.xml  
>       inflating: tei/ordinarysAccounts/OA17351110.xml  
>       inflating: tei/ordinarysAccounts/OA17260627.xml  
>       inflating: tei/ordinarysAccounts/OA17220718.xml  
>       inflating: tei/ordinarysAccounts/OA16910226.xml  
>       inflating: tei/ordinarysAccounts/OA17310308.xml  
>       inflating: tei/ordinarysAccounts/OA16860528.xml  
>       inflating: tei/ordinarysAccounts/OA17601025.xml  
>       inflating: tei/ordinarysAccounts/OA17630824.xml  
>       inflating: tei/ordinarysAccounts/OA17240429.xml  
>       inflating: tei/ordinarysAccounts/OA17531029.xml  
>       inflating: tei/ordinarysAccounts/OA17700419.xml  
>       inflating: tei/ordinarysAccounts/OA17700214.xml  
>       inflating: tei/ordinarysAccounts/OA17000524.xml  
>       inflating: tei/ordinarysAccounts/OA17390702.xml  
>       inflating: tei/ordinarysAccounts/OA16920520.xml  
>       inflating: tei/ordinarysAccounts/OA16781216.xml  
>       inflating: tei/ordinarysAccounts/OA17550317.xml  
>       inflating: tei/ordinarysAccounts/OA16960129.xml  
>       inflating: tei/ordinarysAccounts/OA16890715.xml  
>       inflating: tei/ordinarysAccounts/OA17500516.xml  
>       inflating: tei/ordinarysAccounts/OA17490317.xml  
>       inflating: tei/ordinarysAccounts/OA17030310.xml  
>       inflating: tei/ordinarysAccounts/OA16990802.xml  
>       inflating: tei/ordinarysAccounts/OA17410916.xml  
>       inflating: tei/ordinarysAccounts/OA17650213.xml  
>       inflating: tei/ordinarysAccounts/OA16900509.xml  
>       inflating: tei/ordinarysAccounts/OA17000906.xml  
>       inflating: tei/ordinarysAccounts/OA17110525.xml  
>       inflating: tei/ordinarysAccounts/OA17131024.xml  
>       inflating: tei/ordinarysAccounts/OA16850610.xml  
>       inflating: tei/ordinarysAccounts/OA17271120.xml  
>       inflating: tei/ordinarysAccounts/OA16920415.xml  
>       inflating: tei/ordinarysAccounts/OA16780306.xml  
>       inflating: tei/ordinarysAccounts/OA17390803.xml  
>       inflating: tei/ordinarysAccounts/OA17320726.xml  
>       inflating: tei/ordinarysAccounts/OA16760705.xml  
>       inflating: tei/ordinarysAccounts/OA17490703.xml  
>       inflating: tei/ordinarysAccounts/OA17361102.xml  
>       inflating: tei/ordinarysAccounts/OA17450315.xml  
>       inflating: tei/ordinarysAccounts/OA17111222.xml  
>       inflating: tei/ordinarysAccounts/OA17431021.xml  
>       inflating: tei/ordinarysAccounts/OA17210728.xml  
>       inflating: tei/ordinarysAccounts/OA17200129.xml  
>       inflating: tei/ordinarysAccounts/OA17151102.xml  
>       inflating: tei/ordinarysAccounts/OA17421118.xml  
>       inflating: tei/ordinarysAccounts/OA17241111.xml  
>       inflating: tei/ordinarysAccounts/OA16841217.xml  
>       inflating: tei/ordinarysAccounts/OA17541209.xml  
>       inflating: tei/ordinarysAccounts/OA17530806.xml  
>       inflating: tei/ordinarysAccounts/OA16771017.xml  
>       inflating: tei/ordinarysAccounts/OA17360927.xml  
>       inflating: tei/ordinarysAccounts/OA16860120.xml  
>       inflating: tei/ordinarysAccounts/OA17190608.xml  
>       inflating: tei/ordinarysAccounts/OA17631123.xml  
>       inflating: tei/ordinarysAccounts/OA17460620.xml  
>       inflating: tei/ordinarysAccounts/OA16780522.xml  
>       inflating: tei/ordinarysAccounts/OA17270322.xml  
>       inflating: tei/ordinarysAccounts/OA17600211.xml  
>       inflating: tei/ordinarysAccounts/OA17580701.xml  
>       inflating: tei/ordinarysAccounts/OA17300417.xml  
>       inflating: tei/ordinarysAccounts/OA17240615.xml  
>       inflating: tei/ordinarysAccounts/OA17611111.xml  
>       inflating: tei/ordinarysAccounts/OA17130131.xml  
>       inflating: tei/ordinarysAccounts/OA17210911.xml  
>       inflating: tei/ordinarysAccounts/OA17471116.xml  
>       inflating: tei/ordinarysAccounts/OA17400213.xml  
>       inflating: tei/ordinarysAccounts/OA17620719.xml  
>       inflating: tei/ordinarysAccounts/OA17140528.xml  
>       inflating: tei/ordinarysAccounts/OA17140716.xml  
>       inflating: tei/ordinarysAccounts/OA17520702.xml  
>       inflating: tei/ordinarysAccounts/OA17231106.xml  
>       inflating: tei/ordinarysAccounts/OA17450726.xml  
>       inflating: tei/ordinarysAccounts/OA17540605.xml  
>       inflating: tei/ordinarysAccounts/OA17720708.xml  
>       inflating: tei/ordinarysAccounts/OA17320214.xml  
>       inflating: tei/ordinarysAccounts/OA17631012.xml  
>       inflating: tei/ordinarysAccounts/OA17421122.xml  
>       inflating: tei/ordinarysAccounts/OA17630117.xml  
>       inflating: tei/ordinarysAccounts/OA17290822.xml  
>       inflating: tei/ordinarysAccounts/OA17010129.xml  
>       inflating: tei/ordinarysAccounts/OA17250104.xml  
>       inflating: tei/ordinarysAccounts/OA17021230.xml  
>       inflating: tei/ordinarysAccounts/OA16930201.xml  
>       inflating: tei/ordinarysAccounts/OA17300601.xml  
>       inflating: tei/ordinarysAccounts/OA17220504.xml  
>       inflating: tei/ordinarysAccounts/OA17330129.xml  
>       inflating: tei/ordinarysAccounts/OA16870715.xml  
>       inflating: tei/ordinarysAccounts/OA17471221.xml  
>       inflating: tei/ordinarysAccounts/OA17071024.xml  
>       inflating: tei/ordinarysAccounts/OA17571005.xml  
>       inflating: tei/ordinarysAccounts/OA17441107.xml  
>       inflating: tei/ordinarysAccounts/OA17310924.xml  
>       inflating: tei/ordinarysAccounts/OA17090624.xml  
>       inflating: tei/ordinarysAccounts/OA16930308.xml  
>       inflating: tei/ordinarysAccounts/OA17090323.xml  
>       inflating: tei/ordinarysAccounts/OA17010516.xml  
>       inflating: tei/ordinarysAccounts/OA17221109.xml  
>       inflating: tei/ordinarysAccounts/OA17350922.xml  
>       inflating: tei/ordinarysAccounts/OA17210208.xml  
>       inflating: tei/ordinarysAccounts/OA17040322.xml  
>       inflating: tei/ordinarysAccounts/OA16930517.xml  
>       inflating: tei/ordinarysAccounts/OA17610404.xml  
>       inflating: tei/ordinarysAccounts/OA17070606.xml  
>       inflating: tei/ordinarysAccounts/OA16930920.xml  
>       inflating: tei/ordinarysAccounts/OA17220924a.xml  
>       inflating: tei/ordinarysAccounts/OA16921221.xml  
>       inflating: tei/ordinarysAccounts/OA17150622.xml  
>       inflating: tei/ordinarysAccounts/OA17480511.xml  
>       inflating: tei/ordinarysAccounts/OA17510617.xml  
>       inflating: tei/ordinarysAccounts/OA17401124.xml  
>       inflating: tei/ordinarysAccounts/OA17080303b.xml  
>       inflating: tei/ordinarysAccounts/OA17230525.xml  
>       inflating: tei/ordinarysAccounts/OA17301223.xml  
>       inflating: tei/ordinarysAccounts/OA17160127.xml  
>       inflating: tei/ordinarysAccounts/OA17390530.xml  
>       inflating: tei/ordinarysAccounts/OA17320522.xml  
>       inflating: tei/ordinarysAccounts/OA17380719.xml  
>       inflating: tei/ordinarysAccounts/OA17070502.xml  
>       inflating: tei/ordinarysAccounts/OA16931220.xml  
>       inflating: tei/ordinarysAccounts/OA17221231.xml  
>       inflating: tei/ordinarysAccounts/OA17160921.xml  
>       inflating: tei/ordinarysAccounts/OA17220314.xml  
>       inflating: tei/ordinarysAccounts/OA16780417.xml  
>       inflating: tei/ordinarysAccounts/OA17150803.xml  
>       inflating: tei/ordinarysAccounts/OA16890531.xml  
>       inflating: tei/ordinarysAccounts/OA17530723.xml  
>       inflating: tei/ordinarysAccounts/OA17550804.xml  
>       inflating: tei/ordinarysAccounts/OA17410504.xml  
>       inflating: tei/ordinarysAccounts/OA17410318.xml  
>       inflating: tei/ordinarysAccounts/OA17170320.xml  
>       inflating: tei/ordinarysAccounts/OA17540401.xml  
>       inflating: tei/ordinarysAccounts/OA17150921.xml  
>       inflating: tei/ordinarysAccounts/OA17281111.xml  
>       inflating: tei/ordinarysAccounts/OA17260314.xml  
>       inflating: tei/ordinarysAccounts/OA17490804.xml  
>       inflating: tei/ordinarysAccounts/OA17001106.xml  
>       inflating: tei/ordinarysAccounts/OA17560628.xml  
>       inflating: tei/ordinarysAccounts/OA17041025.xml  
>       inflating: tei/ordinarysAccounts/OA17531001.xml  
>       inflating: tei/ordinarysAccounts/OA17670914.xml  
>       inflating: tei/ordinarysAccounts/OA17501003.xml  
>       inflating: tei/ordinarysAccounts/OA17090803.xml  
>       inflating: tei/ordinarysAccounts/OA17071217.xml  
>       inflating: tei/ordinarysAccounts/OA17080303a.xml  
>       inflating: tei/ordinarysAccounts/OA16841219.xml  
>       inflating: tei/ordinarysAccounts/OA17641217.xml  
>       inflating: tei/ordinarysAccounts/OA16980622.xml  
>       inflating: tei/ordinarysAccounts/OA17551112.xml  
>       inflating: tei/ordinarysAccounts/OA16861217.xml  
>       inflating: tei/ordinarysAccounts/OA17010319.xml  
>       inflating: tei/ordinarysAccounts/OA17231223.xml  
>       inflating: tei/ordinarysAccounts/OA17610420.xml  
>       inflating: tei/ordinarysAccounts/OA17171002.xml  
>       inflating: tei/ordinarysAccounts/OA17441224.xml  
>        creating: tei/sessionsPapers/
>       inflating: tei/sessionsPapers/18800322.xml  
>       inflating: tei/sessionsPapers/18791124.xml  
>       inflating: tei/sessionsPapers/18061029.xml  
>       inflating: tei/sessionsPapers/17790915.xml  
>
>     *** WARNING: skipped 76527 bytes of output ***
>
>       inflating: tei/sessionsPapers/17590530.xml  
>       inflating: tei/sessionsPapers/19101011.xml  
>       inflating: tei/sessionsPapers/18120115.xml  
>       inflating: tei/sessionsPapers/18850622.xml  
>       inflating: tei/sessionsPapers/18840225.xml  
>       inflating: tei/sessionsPapers/19080526.xml  
>       inflating: tei/sessionsPapers/18511124.xml  
>       inflating: tei/sessionsPapers/18730407.xml  
>       inflating: tei/sessionsPapers/18861025.xml  
>       inflating: tei/sessionsPapers/17821204.xml  
>       inflating: tei/sessionsPapers/18191201.xml  
>       inflating: tei/sessionsPapers/17870418.xml  
>       inflating: tei/sessionsPapers/18681123.xml  
>       inflating: tei/sessionsPapers/18670506.xml  
>       inflating: tei/sessionsPapers/18350615.xml  
>       inflating: tei/sessionsPapers/18960420.xml  
>       inflating: tei/sessionsPapers/19010204.xml  
>       inflating: tei/sessionsPapers/17350522.xml  
>       inflating: tei/sessionsPapers/18140914.xml  
>       inflating: tei/sessionsPapers/17600521.xml  
>       inflating: tei/sessionsPapers/17070903.xml  
>       inflating: tei/sessionsPapers/17950701.xml  
>       inflating: tei/sessionsPapers/17340424.xml  
>       inflating: tei/sessionsPapers/17760522.xml  
>       inflating: tei/sessionsPapers/17220907.xml  
>       inflating: tei/sessionsPapers/19020909.xml  
>       inflating: tei/sessionsPapers/18180909.xml  
>       inflating: tei/sessionsPapers/17690510.xml  
>       inflating: tei/sessionsPapers/17970920.xml  
>       inflating: tei/sessionsPapers/18910727.xml  
>       inflating: tei/sessionsPapers/17970426.xml  
>       inflating: tei/sessionsPapers/18510915.xml  
>       inflating: tei/sessionsPapers/16770601.xml  
>       inflating: tei/sessionsPapers/17210301.xml  
>       inflating: tei/sessionsPapers/17531024.xml  
>       inflating: tei/sessionsPapers/17890603.xml  
>       inflating: tei/sessionsPapers/19121008.xml  
>       inflating: tei/sessionsPapers/18371127.xml  
>       inflating: tei/sessionsPapers/18181028.xml  
>       inflating: tei/sessionsPapers/18210411.xml  
>       inflating: tei/sessionsPapers/18380129.xml  
>       inflating: tei/sessionsPapers/17991204.xml  
>       inflating: tei/sessionsPapers/18991023.xml  
>       inflating: tei/sessionsPapers/18391216.xml  
>       inflating: tei/sessionsPapers/18370918.xml  
>       inflating: tei/sessionsPapers/19020113.xml  
>       inflating: tei/sessionsPapers/17420428.xml  
>       inflating: tei/sessionsPapers/16840702.xml  
>       inflating: tei/sessionsPapers/18720506.xml  
>       inflating: tei/sessionsPapers/18100411.xml  
>       inflating: tei/sessionsPapers/16751013.xml  
>       inflating: tei/sessionsPapers/18990626.xml  
>       inflating: tei/sessionsPapers/17761204.xml  
>       inflating: tei/sessionsPapers/18930109.xml  
>       inflating: tei/sessionsPapers/16810413.xml  
>       inflating: tei/sessionsPapers/17641017.xml  
>       inflating: tei/sessionsPapers/18120701.xml  
>       inflating: tei/sessionsPapers/17850914.xml  
>       inflating: tei/sessionsPapers/16930531.xml  
>       inflating: tei/sessionsPapers/19040620.xml  
>       inflating: tei/sessionsPapers/18520105.xml  
>       inflating: tei/sessionsPapers/17190408.xml  
>       inflating: tei/sessionsPapers/17860531.xml  
>       inflating: tei/sessionsPapers/18680706.xml  
>       inflating: tei/sessionsPapers/18980425.xml  
>       inflating: tei/sessionsPapers/18580222.xml  
>       inflating: tei/sessionsPapers/16890516.xml  
>       inflating: tei/sessionsPapers/17390502.xml  
>       inflating: tei/sessionsPapers/18040215.xml  
>       inflating: tei/sessionsPapers/17330404.xml  
>       inflating: tei/sessionsPapers/18520510.xml  
>       inflating: tei/sessionsPapers/17780218.xml  
>       inflating: tei/sessionsPapers/19121201.xml  
>       inflating: tei/sessionsPapers/19090518.xml  
>       inflating: tei/sessionsPapers/18601022.xml  
>       inflating: tei/sessionsPapers/18580201.xml  
>       inflating: tei/sessionsPapers/18310217.xml  
>       inflating: tei/sessionsPapers/18780527.xml  
>       inflating: tei/sessionsPapers/18231203.xml  
>       inflating: tei/sessionsPapers/18200918.xml  
>       inflating: tei/sessionsPapers/17260420.xml  
>       inflating: tei/sessionsPapers/17971206.xml  
>       inflating: tei/sessionsPapers/18840128.xml  
>       inflating: tei/sessionsPapers/18430508.xml  
>       inflating: tei/sessionsPapers/18770528.xml  
>       inflating: tei/sessionsPapers/18530509.xml  
>       inflating: tei/sessionsPapers/18890107.xml  
>       inflating: tei/sessionsPapers/17760417.xml  
>       inflating: tei/sessionsPapers/17591024.xml  
>       inflating: tei/sessionsPapers/17540227.xml  
>       inflating: tei/sessionsPapers/17171016.xml  
>       inflating: tei/sessionsPapers/18780408.xml  
>       inflating: tei/sessionsPapers/17291203.xml  
>       inflating: tei/sessionsPapers/16900903.xml  
>       inflating: tei/sessionsPapers/17710911.xml  
>       inflating: tei/sessionsPapers/17040601.xml  
>       inflating: tei/sessionsPapers/17430114.xml  
>       inflating: tei/sessionsPapers/18680817.xml  
>       inflating: tei/sessionsPapers/18680127.xml  
>       inflating: tei/sessionsPapers/17710515.xml  
>       inflating: tei/sessionsPapers/17350911.xml  
>       inflating: tei/sessionsPapers/19120109.xml  
>       inflating: tei/sessionsPapers/18841117.xml  
>       inflating: tei/sessionsPapers/18871024.xml  
>       inflating: tei/sessionsPapers/18940528.xml  
>       inflating: tei/sessionsPapers/17280501.xml  
>       inflating: tei/sessionsPapers/17150223.xml  
>       inflating: tei/sessionsPapers/16821206.xml  
>       inflating: tei/sessionsPapers/17361208.xml  
>       inflating: tei/sessionsPapers/18590704.xml  
>       inflating: tei/sessionsPapers/16910909.xml  
>       inflating: tei/sessionsPapers/18920725.xml  
>       inflating: tei/sessionsPapers/19110110.xml  
>       inflating: tei/sessionsPapers/17521206.xml  
>       inflating: tei/sessionsPapers/18350921.xml  
>       inflating: tei/sessionsPapers/18370227.xml  
>       inflating: tei/sessionsPapers/18810228.xml  
>       inflating: tei/sessionsPapers/18210912.xml  
>       inflating: tei/sessionsPapers/18660409.xml  
>       inflating: tei/sessionsPapers/16910422.xml  
>       inflating: tei/sessionsPapers/18170219.xml  
>       inflating: tei/sessionsPapers/18770507.xml  
>       inflating: tei/sessionsPapers/19071021.xml  
>       inflating: tei/sessionsPapers/16840515.xml  
>       inflating: tei/sessionsPapers/19020210.xml  
>       inflating: tei/sessionsPapers/18111030.xml  
>       inflating: tei/sessionsPapers/17200907.xml  
>       inflating: tei/sessionsPapers/16820223.xml  
>       inflating: tei/sessionsPapers/16820906A.xml  
>       inflating: tei/sessionsPapers/19090202.xml  
>       inflating: tei/sessionsPapers/17180110.xml  
>       inflating: tei/sessionsPapers/18550226.xml  
>       inflating: tei/sessionsPapers/17930220.xml  
>       inflating: tei/sessionsPapers/18890729.xml  
>       inflating: tei/sessionsPapers/18700711.xml  
>       inflating: tei/sessionsPapers/17181205.xml  
>       inflating: tei/sessionsPapers/18410510.xml  
>       inflating: tei/sessionsPapers/19011021.xml  
>       inflating: tei/sessionsPapers/18180218.xml  
>       inflating: tei/sessionsPapers/16750707.xml  
>       inflating: tei/sessionsPapers/17160906.xml  
>       inflating: tei/sessionsPapers/19080303.xml  
>       inflating: tei/sessionsPapers/17580405.xml  
>       inflating: tei/sessionsPapers/17510523.xml  
>       inflating: tei/sessionsPapers/18430102.xml  
>       inflating: tei/sessionsPapers/17940716.xml  
>       inflating: tei/sessionsPapers/19101206.xml  
>       inflating: tei/sessionsPapers/17520625.xml  
>       inflating: tei/sessionsPapers/17820220.xml  
>       inflating: tei/sessionsPapers/16800117.xml  
>       inflating: tei/sessionsPapers/17720909.xml  
>       inflating: tei/sessionsPapers/18080713.xml  
>       inflating: tei/sessionsPapers/18460105.xml  
>       inflating: tei/sessionsPapers/18061203.xml  
>       inflating: tei/sessionsPapers/17270113.xml  
>       inflating: tei/sessionsPapers/18520816.xml  
>       inflating: tei/sessionsPapers/17210830.xml  
>       inflating: tei/sessionsPapers/17870115.xml  
>       inflating: tei/sessionsPapers/18291203.xml  
>       inflating: tei/sessionsPapers/18670107.xml  
>       inflating: tei/sessionsPapers/17211206.xml  
>       inflating: tei/sessionsPapers/18841215.xml  
>       inflating: tei/sessionsPapers/17690112.xml  
>       inflating: tei/sessionsPapers/18400106.xml  
>       inflating: tei/sessionsPapers/18100919.xml  
>       inflating: tei/sessionsPapers/17190514.xml  
>       inflating: tei/sessionsPapers/17120111.xml  
>       inflating: tei/sessionsPapers/17510703.xml  
>       inflating: tei/sessionsPapers/18460921.xml  
>       inflating: tei/sessionsPapers/18390204.xml  
>       inflating: tei/sessionsPapers/17500912.xml  
>       inflating: tei/sessionsPapers/17440113.xml  
>       inflating: tei/sessionsPapers/18550917.xml  
>       inflating: tei/sessionsPapers/18791020.xml  
>       inflating: tei/sessionsPapers/18290910.xml  
>       inflating: tei/sessionsPapers/18311201.xml  
>       inflating: tei/sessionsPapers/17640607.xml  
>       inflating: tei/sessionsPapers/18900303.xml  
>       inflating: tei/sessionsPapers/17700711.xml  
>       inflating: tei/sessionsPapers/19060305.xml  
>       inflating: tei/sessionsPapers/18560204.xml  
>       inflating: tei/sessionsPapers/18880528.xml  
>       inflating: tei/sessionsPapers/18000402.xml  
>       inflating: tei/sessionsPapers/18990912.xml  
>       inflating: tei/sessionsPapers/16830829.xml  
>       inflating: tei/sessionsPapers/19061022.xml  
>       inflating: tei/sessionsPapers/17470715.xml  
>       inflating: tei/sessionsPapers/18960323.xml  
>       inflating: tei/sessionsPapers/17830604.xml  
>       inflating: tei/sessionsPapers/18491029.xml  
>       inflating: tei/sessionsPapers/18951118.xml  
>       inflating: tei/sessionsPapers/18260406.xml  
>       inflating: tei/sessionsPapers/18030112.xml  
>       inflating: tei/sessionsPapers/19080908.xml  
>       inflating: tei/sessionsPapers/18560616.xml  
>       inflating: tei/sessionsPapers/18970628.xml  
>       inflating: tei/sessionsPapers/16761213.xml  
>       inflating: tei/sessionsPapers/17500117.xml  
>       inflating: tei/sessionsPapers/17601022.xml  
>       inflating: tei/sessionsPapers/18420103.xml  
>       inflating: tei/sessionsPapers/18240715.xml  
>       inflating: tei/sessionsPapers/18410823.xml  
>       inflating: tei/sessionsPapers/18831210.xml  
>       inflating: tei/sessionsPapers/18441125.xml  
>       inflating: tei/sessionsPapers/17960217.xml  
>       inflating: tei/sessionsPapers/16921207.xml  
>       inflating: tei/sessionsPapers/18970503.xml  
>       inflating: tei/sessionsPapers/19041017.xml  
>       inflating: tei/sessionsPapers/17120227.xml  
>       inflating: tei/sessionsPapers/16820712.xml  
>       inflating: tei/sessionsPapers/18360815.xml  
>       inflating: tei/sessionsPapers/17760221.xml  
>       inflating: tei/sessionsPapers/18351026.xml  
>       inflating: tei/sessionsPapers/18221204.xml  
>       inflating: tei/sessionsPapers/17900526.xml  
>       inflating: tei/sessionsPapers/16791015.xml  
>       inflating: tei/sessionsPapers/18420131.xml  
>       inflating: tei/sessionsPapers/19091012.xml  
>       inflating: tei/sessionsPapers/19100111.xml  
>       inflating: tei/sessionsPapers/16830418A.xml  
>       inflating: tei/sessionsPapers/17570420.xml  
>       inflating: tei/sessionsPapers/16871012.xml  
>       inflating: tei/sessionsPapers/16770425.xml  
>       inflating: tei/sessionsPapers/18230910.xml  
>       inflating: tei/sessionsPapers/17600709.xml  
>       inflating: tei/sessionsPapers/17640113.xml  
>       inflating: tei/sessionsPapers/17290226.xml  
>       inflating: tei/sessionsPapers/18360229.xml  
>       inflating: tei/sessionsPapers/17571207.xml  
>       inflating: tei/sessionsPapers/17200712.xml  
>       inflating: tei/sessionsPapers/17731020.xml  
>       inflating: tei/sessionsPapers/17560528.xml  
>       inflating: tei/sessionsPapers/19110207.xml  
>       inflating: tei/sessionsPapers/18650508.xml  
>       inflating: tei/sessionsPapers/19081208.xml  
>       inflating: tei/sessionsPapers/18470510.xml  
>       inflating: tei/sessionsPapers/17340630.xml  
>       inflating: tei/sessionsPapers/16991213A.xml  
>       inflating: tei/sessionsPapers/18631214.xml  
>       inflating: tei/sessionsPapers/18400511.xml  
>       inflating: tei/sessionsPapers/18680921.xml  
>       inflating: tei/sessionsPapers/16940524.xml  
>       inflating: tei/sessionsPapers/18440205.xml  
>       inflating: tei/sessionsPapers/17980523.xml  
>       inflating: tei/sessionsPapers/18591024.xml  
>       inflating: tei/sessionsPapers/19111010.xml  
>       inflating: tei/sessionsPapers/17470909.xml  
>       inflating: tei/sessionsPapers/17490405.xml  
>       inflating: tei/sessionsPapers/18690816.xml  
>       inflating: tei/sessionsPapers/17310115.xml  
>       inflating: tei/sessionsPapers/18190707.xml  
>       inflating: tei/sessionsPapers/17410225.xml  
>       inflating: tei/sessionsPapers/17720603.xml  
>       inflating: tei/sessionsPapers/18740202.xml  
>       inflating: tei/sessionsPapers/18761120.xml  
>       inflating: tei/sessionsPapers/16820116.xml  
>       inflating: tei/sessionsPapers/18390617.xml  
>       inflating: tei/sessionsPapers/18670610.xml  
>       inflating: tei/sessionsPapers/17161105.xml  
>       inflating: tei/sessionsPapers/18411213.xml  
>       inflating: tei/sessionsPapers/18570615.xml  
>       inflating: tei/sessionsPapers/18210214.xml  
>       inflating: tei/sessionsPapers/18660129.xml  
>       inflating: tei/sessionsPapers/18320906.xml  
>       inflating: tei/sessionsPapers/19080721.xml  
>       inflating: tei/sessionsPapers/16860414.xml  
>       inflating: tei/sessionsPapers/18610408.xml  
>       inflating: tei/sessionsPapers/17080707.xml  
>       inflating: tei/sessionsPapers/17890422.xml  
>       inflating: tei/sessionsPapers/17830726.xml  
>       inflating: tei/sessionsPapers/17750111.xml  
>       inflating: tei/sessionsPapers/18820626.xml  
>       inflating: tei/sessionsPapers/16750219.xml  
>       inflating: tei/sessionsPapers/18980913.xml  
>       inflating: tei/sessionsPapers/18440610.xml  
>       inflating: tei/sessionsPapers/18530103.xml  
>       inflating: tei/sessionsPapers/18930724.xml  
>       inflating: tei/sessionsPapers/18480821.xml  
>       inflating: tei/sessionsPapers/18790526.xml  
>       inflating: tei/sessionsPapers/18370130.xml  
>       inflating: tei/sessionsPapers/18421128.xml  
>       inflating: tei/sessionsPapers/19130401.xml  
>       inflating: tei/sessionsPapers/18140706.xml  
>       inflating: tei/sessionsPapers/18820227.xml  
>       inflating: tei/sessionsPapers/17751206.xml  
>       inflating: tei/sessionsPapers/17680413.xml  
>       inflating: tei/sessionsPapers/17810912.xml  
>       inflating: tei/sessionsPapers/18720108.xml  
>       inflating: tei/sessionsPapers/17461015.xml  
>       inflating: tei/sessionsPapers/18910504.xml  
>       inflating: tei/sessionsPapers/18930626.xml  
>       inflating: tei/sessionsPapers/18821016.xml  
>       inflating: tei/sessionsPapers/18891118.xml  
>       inflating: tei/sessionsPapers/17520914.xml  
>       inflating: tei/sessionsPapers/17400227.xml  
>       inflating: tei/sessionsPapers/17241204.xml  
>       inflating: tei/sessionsPapers/16811207.xml  
>       inflating: tei/sessionsPapers/18570406.xml  
>       inflating: tei/sessionsPapers/17450710.xml  
>       inflating: tei/sessionsPapers/19120611.xml  
>       inflating: tei/sessionsPapers/17850406.xml  
>       inflating: tei/sessionsPapers/18581213.xml  
>       inflating: tei/sessionsPapers/19050403.xml  
>       inflating: tei/sessionsPapers/18590815.xml  
>       inflating: tei/sessionsPapers/18861213.xml  
>       inflating: tei/sessionsPapers/17740518.xml  
>       inflating: tei/sessionsPapers/17110112.xml  
>       inflating: tei/sessionsPapers/17391017.xml  
>       inflating: tei/sessionsPapers/18201206.xml  
>       inflating: tei/sessionsPapers/17130708.xml  
>       inflating: tei/sessionsPapers/17320525.xml  
>       inflating: tei/sessionsPapers/18561027.xml  
>       inflating: tei/sessionsPapers/18921114.xml  
>       inflating: tei/sessionsPapers/17680518.xml  
>       inflating: tei/sessionsPapers/18540508.xml  
>       inflating: tei/sessionsPapers/19070225.xml  
>       inflating: tei/sessionsPapers/18250915.xml  
>       inflating: tei/sessionsPapers/17660903.xml  
>       inflating: tei/sessionsPapers/17740706.xml  
>       inflating: tei/sessionsPapers/17610116.xml  
>       inflating: tei/sessionsPapers/16780116.xml  
>       inflating: tei/sessionsPapers/18810627.xml  
>       inflating: tei/sessionsPapers/18081130.xml  
>       inflating: tei/sessionsPapers/18860628.xml  
>       inflating: tei/sessionsPapers/17800405.xml  
>       inflating: tei/sessionsPapers/18080914.xml  
>       inflating: tei/sessionsPapers/18710710.xml  
>       inflating: tei/sessionsPapers/18561215.xml  
>       inflating: tei/sessionsPapers/17471014.xml  
>       inflating: tei/sessionsPapers/18381231.xml  
>       inflating: tei/sessionsPapers/19020310.xml  
>       inflating: tei/sessionsPapers/18790805.xml  
>       inflating: tei/sessionsPapers/17240226.xml  
>       inflating: tei/sessionsPapers/18200416.xml  
>       inflating: tei/sessionsPapers/17450911.xml  
>       inflating: tei/sessionsPapers/19050502.xml  
>       inflating: tei/sessionsPapers/17760710.xml  
>       inflating: tei/sessionsPapers/17750913.xml  
>       inflating: tei/sessionsPapers/17920523.xml  
>       inflating: tei/sessionsPapers/18490611.xml  
>       inflating: tei/sessionsPapers/17561020.xml  
>       inflating: tei/sessionsPapers/18990109.xml  
>       inflating: tei/sessionsPapers/18560818.xml  
>       inflating: tei/sessionsPapers/17700221.xml  
>       inflating: tei/sessionsPapers/18960622.xml  
>       inflating: tei/sessionsPapers/18490507.xml  
>       inflating: tei/sessionsPapers/17161010.xml  
>       inflating: tei/sessionsPapers/17860719.xml  
>       inflating: tei/sessionsPapers/18171029.xml  
>       inflating: tei/sessionsPapers/17960511.xml  
>       inflating: tei/sessionsPapers/18750920.xml  
>       inflating: tei/sessionsPapers/17350702.xml  
>       inflating: tei/sessionsPapers/19090907.xml  
>       inflating: tei/sessionsPapers/19090323.xml  
>       inflating: tei/sessionsPapers/18800628.xml  
>       inflating: tei/sessionsPapers/18640919.xml  
>       inflating: tei/sessionsPapers/17980214.xml  
>       inflating: tei/sessionsPapers/17260425.xml  
>       inflating: tei/sessionsPapers/18630608.xml  
>       inflating: tei/sessionsPapers/17841208.xml  
>       inflating: tei/sessionsPapers/18690301.xml  
>       inflating: tei/sessionsPapers/18880702.xml  
>       inflating: tei/sessionsPapers/17710703.xml  
>       inflating: tei/sessionsPapers/17721209.xml  
>       inflating: tei/sessionsPapers/18650814.xml  
>       inflating: tei/sessionsPapers/17230710.xml  
>       inflating: tei/sessionsPapers/18740921.xml  
>       inflating: tei/sessionsPapers/17840601.xml  
>       inflating: tei/sessionsPapers/18511215.xml  
>       inflating: tei/sessionsPapers/16950703.xml  
>       inflating: tei/sessionsPapers/18880730.xml  
>       inflating: tei/sessionsPapers/18370508.xml  
>       inflating: tei/sessionsPapers/17061206.xml  
>       inflating: tei/sessionsPapers/17481012.xml  
>       inflating: tei/sessionsPapers/18091206.xml  
>       inflating: tei/sessionsPapers/18380226.xml  
>       inflating: tei/sessionsPapers/19000723.xml  
>       inflating: tei/sessionsPapers/17241014.xml  
>       inflating: tei/sessionsPapers/17611021.xml  
>       inflating: tei/sessionsPapers/17231204.xml  
>       inflating: tei/sessionsPapers/18940430.xml  
>       inflating: tei/sessionsPapers/18730113.xml  
>       inflating: tei/sessionsPapers/18440304.xml  
>       inflating: tei/sessionsPapers/18900728.xml  
>       inflating: tei/sessionsPapers/17090302.xml  
>       inflating: tei/sessionsPapers/17221205.xml  
>       inflating: tei/sessionsPapers/16861013.xml  
>       inflating: tei/sessionsPapers/16781016.xml  
>       inflating: tei/sessionsPapers/17930911.xml  
>       inflating: tei/sessionsPapers/18400817.xml  
>       inflating: tei/sessionsPapers/18751025.xml  
>       inflating: tei/sessionsPapers/19060625.xml  
>       inflating: tei/sessionsPapers/17860222.xml  
>       inflating: tei/sessionsPapers/17220510.xml  
>       inflating: tei/sessionsPapers/17460903.xml  
>       inflating: tei/sessionsPapers/16791210.xml  
>       inflating: tei/sessionsPapers/17670909.xml  
>       inflating: tei/sessionsPapers/19110905.xml  
>       inflating: tei/sessionsPapers/19130107.xml  
>       inflating: tei/sessionsPapers/17460702.xml  
>       inflating: tei/sessionsPapers/18670819.xml  
>       inflating: tei/sessionsPapers/19110328.xml  
>       inflating: tei/sessionsPapers/19120910.xml  
>       inflating: tei/sessionsPapers/18771210.xml  
>       inflating: tei/sessionsPapers/19001022.xml  
>       inflating: tei/sessionsPapers/18820911.xml  
>       inflating: tei/sessionsPapers/17400416.xml  
>       inflating: tei/sessionsPapers/18331017.xml  
>       inflating: tei/sessionsPapers/17031013.xml  
>       inflating: tei/sessionsPapers/17581025.xml  
>       inflating: tei/sessionsPapers/18000219.xml  
>       inflating: tei/sessionsPapers/18110109.xml  
>       inflating: tei/sessionsPapers/17740907.xml  
>       inflating: tei/sessionsPapers/18501021.xml  
>       inflating: tei/sessionsPapers/18920627.xml  
>       inflating: tei/sessionsPapers/18630817.xml  
>       inflating: tei/sessionsPapers/17421208.xml  
>       inflating: tei/sessionsPapers/17810110.xml  
>       inflating: tei/sessionsPapers/18931016.xml  
>       inflating: tei/sessionsPapers/17420224.xml  
>       inflating: tei/sessionsPapers/16980608.xml  
>       inflating: tei/sessionsPapers/17970712.xml  
>       inflating: tei/sessionsPapers/18961019.xml  
>       inflating: tei/sessionsPapers/18831119.xml  
>       inflating: tei/sessionsPapers/18491126.xml  
>       inflating: tei/sessionsPapers/18811121.xml  
>       inflating: tei/sessionsPapers/17790217.xml  
>       inflating: tei/sessionsPapers/18360613.xml  
>       inflating: tei/sessionsPapers/18820522.xml  
>       inflating: tei/sessionsPapers/18430612.xml  
>       inflating: tei/sessionsPapers/18660813.xml  
>       inflating: tei/sessionsPapers/19010107.xml  
>       inflating: tei/sessionsPapers/19110425.xml  
>       inflating: tei/sessionsPapers/18390304.xml  
>       inflating: tei/sessionsPapers/17720429.xml  
>       inflating: tei/sessionsPapers/17520116.xml  
>       inflating: tei/sessionsPapers/17520514.xml  
>       inflating: tei/sessionsPapers/18720226.xml  
>       inflating: tei/sessionsPapers/17990403.xml  
>       inflating: tei/sessionsPapers/18820109.xml  
>       inflating: tei/sessionsPapers/18460330.xml  
>       inflating: tei/sessionsPapers/18290716.xml  
>       inflating: tei/sessionsPapers/17510417.xml  
>       inflating: tei/sessionsPapers/17710220.xml  
>       inflating: tei/sessionsPapers/18330817.xml  
>       inflating: tei/sessionsPapers/17881210.xml  
>       inflating: tei/sessionsPapers/18660917.xml  
>       inflating: tei/sessionsPapers/18710109.xml  
>       inflating: tei/sessionsPapers/18040516.xml  
>       inflating: tei/sessionsPapers/17520219.xml  
>       inflating: tei/sessionsPapers/19021020.xml  
>       inflating: tei/sessionsPapers/19120423.xml  
>       inflating: tei/sessionsPapers/18440408.xml  
>       inflating: tei/sessionsPapers/17631019.xml  
>       inflating: tei/sessionsPapers/18030914.xml  
>       inflating: tei/sessionsPapers/17740413.xml  
>       inflating: tei/sessionsPapers/17870711.xml  
>       inflating: tei/sessionsPapers/18361212.xml  
>       inflating: tei/sessionsPapers/18520614.xml  
>       inflating: tei/sessionsPapers/17140908.xml  
>       inflating: tei/sessionsPapers/17990220.xml  
>       inflating: tei/sessionsPapers/16780828.xml  
>       inflating: tei/sessionsPapers/18340515.xml  
>       inflating: tei/sessionsPapers/18491217.xml  
>       inflating: tei/sessionsPapers/18540814.xml  
>       inflating: tei/sessionsPapers/17330221.xml  
>       inflating: tei/sessionsPapers/17800223.xml  
>       inflating: tei/sessionsPapers/18790303.xml  
>       inflating: tei/sessionsPapers/17590912.xml  
>       inflating: tei/sessionsPapers/18430918.xml  
>       inflating: tei/sessionsPapers/18570202.xml  
>       inflating: tei/sessionsPapers/18410301.xml  
>       inflating: tei/sessionsPapers/18980207.xml  
>       inflating: tei/sessionsPapers/18350202.xml  
>       inflating: tei/sessionsPapers/18780806.xml  
>       inflating: tei/sessionsPapers/16860707.xml  
>       inflating: tei/sessionsPapers/17160411.xml  
>       inflating: tei/sessionsPapers/18701024.xml  
>       inflating: tei/sessionsPapers/18821211.xml  
>       inflating: tei/sessionsPapers/18211205.xml  
>       inflating: tei/sessionsPapers/16860520.xml  
>       inflating: tei/sessionsPapers/19000625.xml  
>       inflating: tei/sessionsPapers/18411129.xml  
>       inflating: tei/sessionsPapers/18110220.xml  
>       inflating: tei/sessionsPapers/16961209.xml  
>       inflating: tei/sessionsPapers/18270405.xml  
>       inflating: tei/sessionsPapers/17940604.xml  
>       inflating: tei/sessionsPapers/17820515.xml  
>       inflating: tei/sessionsPapers/18301028.xml  
>       inflating: tei/sessionsPapers/18700404.xml  
>       inflating: tei/sessionsPapers/17700530.xml  
>       inflating: tei/sessionsPapers/18210606.xml  
>       inflating: tei/sessionsPapers/18100606.xml  
>       inflating: tei/sessionsPapers/18130602.xml  
>       inflating: tei/sessionsPapers/17230227.xml  
>       inflating: tei/sessionsPapers/17370706.xml  
>       inflating: tei/sessionsPapers/19060911.xml  
>       inflating: tei/sessionsPapers/17920215.xml  
>       inflating: tei/sessionsPapers/18950520.xml  
>       inflating: tei/sessionsPapers/18351214.xml  
>       inflating: tei/sessionsPapers/18761211.xml  
>       inflating: tei/sessionsPapers/18671216.xml  
>       inflating: tei/sessionsPapers/17841210.xml  
>       inflating: tei/sessionsPapers/19090112.xml  
>       inflating: tei/sessionsPapers/18530228.xml  
>       inflating: tei/sessionsPapers/17671021.xml  
>       inflating: tei/sessionsPapers/18510407.xml  
>       inflating: tei/sessionsPapers/18780114.xml  
>       inflating: tei/sessionsPapers/16931206.xml  
>       inflating: tei/sessionsPapers/17590228.xml  
>       inflating: tei/sessionsPapers/16860114.xml  
>       inflating: tei/sessionsPapers/18610923.xml  
>       inflating: tei/sessionsPapers/17560603.xml  
>       inflating: tei/sessionsPapers/17801206.xml  
>       inflating: tei/sessionsPapers/18201028.xml  
>       inflating: tei/sessionsPapers/17150114.xml  
>       inflating: tei/sessionsPapers/18671028.xml  
>       inflating: tei/sessionsPapers/16951014.xml  
>       inflating: tei/sessionsPapers/17611209.xml  
>       inflating: tei/sessionsPapers/16750115.xml  
>       inflating: tei/sessionsPapers/16951203.xml  
>       inflating: tei/sessionsPapers/18620707.xml  
>       inflating: tei/sessionsPapers/18860503.xml  
>       inflating: tei/sessionsPapers/18930911.xml  
>       inflating: tei/sessionsPapers/18140420.xml  
>       inflating: tei/sessionsPapers/17950416.xml  
>       inflating: tei/sessionsPapers/17330112.xml  
>       inflating: tei/sessionsPapers/16980223.xml  
>       inflating: tei/sessionsPapers/17760911.xml  
>       inflating: tei/sessionsPapers/18131027.xml  
>       inflating: tei/sessionsPapers/17300116.xml  

Let's put the files in dbfs.

``` scala
dbutils.fs.mkdirs("dbfs:/datasets/obo/tei") //need not be done again!
```

>     res26: Boolean = true

``` scala
//dbutils.fs.rm("dbfs:/datasets/obo/tei",true)
```

``` sh
ls 
#ls obo-tiny/tei
```

>     conf
>     derby.log
>     eventlogs
>     ganglia
>     logs
>     obo-tiny
>     OB_tei_7-2_CC-BY-NC.zip
>     OB-tiny_tei_7-2_CC-BY-NC.zip
>     tei

``` scala
 //dbutils.fs.cp("file:/databricks/driver/obo-tiny/tei", "dbfs:/datasets/obo/tei/",recurse=true) // already done and it takes 1500 seconds - a while!
 dbutils.fs.cp("file:/databricks/driver/tei", "dbfs:/datasets/obo/tei/",recurse=true) // already done and it takes 19 minutes - a while!
```

>     res40: Boolean = true

``` scala
//dbutils.fs.rm("dbfs:/datasets/tweets",true) // remove files to make room for the OBO dataset
```

>     res38: Boolean = true

``` scala
display(dbutils.fs.ls("dbfs:/datasets/"))
```

| path                             | name              | size |
|----------------------------------|-------------------|------|
| dbfs:/datasets/mini\_newsgroups/ | mini\_newsgroups/ | 0.0  |
| dbfs:/datasets/obo/              | obo/              | 0.0  |

``` scala
display(dbutils.fs.ls("dbfs:/datasets/obo/tei/"))
```

| path                                      | name               | size |
|-------------------------------------------|--------------------|------|
| dbfs:/datasets/obo/tei/ordinarysAccounts/ | ordinarysAccounts/ | 0.0  |
| dbfs:/datasets/obo/tei/sessionsPapers/    | sessionsPapers/    | 0.0  |

``` scala
util.Properties.versionString // check scala version
```

>     res5: String = version 2.11.8

