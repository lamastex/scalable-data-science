// Databricks notebook source exported at Sat, 18 Jun 2016 05:09:42 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week2/03_WordCount/006_WordCount.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/week2/03_wordcount/006_wordcount](http://img.youtube.com/vi/zgkvusQdNLY/0.jpg)](https://www.youtube.com/v/zgkvusQdNLY?rel=0&autoplay=1&modestbranding=1&start=4613)





# Word Count on US State of the Union (SoU) Addresses

* Word Count in big data is the equivalent of 'Hello World' in programming
* We count the number of occurences of each word in the first and last (2016) SoU addresses.






An interesting analysis of the textual content of the *State of the Union (SoU)* addresses by all US presidents was done in:
* [Alix Rule, Jean-Philippe Cointet, and Peter S. Bearman, Lexical shifts, substantive changes, and continuity in State of the Union discourse, 1790–2014, PNAS 2015 112 (35) 10837-10844; doi:10.1073/pnas.1512221112](http://www.pnas.org/content/112/35/10837.full).


![](http://www.pnas.org/content/112/35/10837/F5.large.jpg)

[Fig. 5](http://www.pnas.org/content/112/35/10837.full). A river network captures the flow across history of US political discourse, as perceived by contemporaries. Time moves along the x axis. Clusters on semantic networks of 300 most frequent terms for each of 10 historical periods are displayed as vertical bars. Relations between clusters of adjacent periods are indexed by gray flows, whose density reflects their degree of connection. Streams that connect at any point in history may be considered to be part of the same system, indicated with a single color. 

## Let us investigate this dataset ourselves!
1. We first get the source text data by scraping and parsig from [http://stateoftheunion.onetwothree.net/texts/index.html](http://stateoftheunion.onetwothree.net/texts/index.html) as explained in 
[scraping and parsing SoU addresses](/#workspace/scalable-data-science/xtraResources/sdsDatasets/scraperUSStateofUnionAddresses).
* This data is already made available in DBFS, our distributed file system.
* We only do the simplest word count with this data in this notebook and will do more sophisticated analyses in the sequel (including topic modeling, etc).





## Key Data Management Concepts 

### The Structure Spectrum

**(watch now 1:10)**:

[![Structure Spectrum by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/pMSGGZVSwqo/0.jpg)](https://www.youtube.com/v/pMSGGZVSwqo?rel=0&autoplay=1&modestbranding=1&start=1&end=70)

Here we will be working with **unstructured** or **schema-never** data (plain text files).
***

### Files

**(watch later 1:43)**:

[![Files by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/NJyBQ-cQ3Ac/0.jpg)](https://www.youtube.com/v/NJyBQ-cQ3Ac?rel=0&autoplay=1&modestbranding=1&start=1)





###DBFS and dbutils - where is this dataset in our distributed file system?
* Since we are on the databricks cloud, it has a file system called DBFS
* DBFS is similar to HDFS, the Hadoop distributed file system
* dbutils allows us to interact with dbfs.
* The 'display' command displays the list of files in a given directory in the file system.


```scala

display(dbutils.fs.ls("dbfs:/datasets/sou")) // Cntrl+Enter to display the files in dbfs:/datasets/sou

```



Let us display the *head* or the first few lines of the file `dbfs:/datasets/sou/17900108.txt` to see what it contains using `dbutils.fs.head` method.  
`head(file: String, maxBytes: int = 65536): String` -> Returns up to the first 'maxBytes' bytes of the given file as a String encoded in UTF-8
as follows:


```scala

dbutils.fs.head("dbfs:/datasets/sou/17900108.txt",673) // Cntrl+Enter to get the first 673 bytes of the file (which corresponds to the first five lines)

```



##### You Try!
Modify ``xxxx` in the cell below to read the first 1000 bytes from the file.


```scala

dbutils.fs.head("dbfs:/datasets/sou/17900108.txt", xxxx) // Cntrl+Enter to get the first 1000 bytes of the file

```



### Read the file into Spark Context as an RDD of Strings
* The `textFile` method on the available `SparkContext` `sc` can read the text file `dbfs:/datasets/sou/17900108.txt` into Spark and create an RDD of Strings
  * but this is done lazily until an action is taken on the RDD `sou17900108`!


```scala

val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt") // Cntrl+Enter to read in the textfile as RDD[String]

```



### Perform some actions on the RDD
* Each String in the RDD `sou17900108` represents one line of data from the file and can be made to perform one of the following actions:
  * count the number of elements in the RDD `sou17900108` (i.e., the number of lines in the text file `dbfs:/datasets/sou/17900108.txt`) using `sou17900108.count()`
  * display the contents of the RDD using `take` or `collect`.


```scala

sou17900108.count() // <Shift+Enter> to count the number of elements in the RDD

```
```scala

sou17900108.take(5) // <Shift+Enter> to display the first 5 elements of RDD

```
```scala

sou17900108.take(5).foreach(println) // <Shift+Enter> to display the first 5 elements of RDD line by line

```
```scala

sou17900108.collect // <Cntrl+Enter> to display all the elements of RDD

```



### Cache the RDD in (distributed) memory to avoid recreating it for each action
* Above, every time we took an action on the same RDD, the RDD was reconstructed from the textfile.  
  * Spark's advantage compared to Hadoop MapReduce is the ability to cache or store the RDD in distributed memory across the nodes.
* Let's use `.cache()` after creating an RDD so that it is in memory after the first action (and thus avoid reconstruction for subsequent actions).
  * count the number of elements in the RDD `sou17900108` (i.e., the number of lines in the text file `dbfs:/datasets/sou/17900108.txt`) using `sou17900108.count()`
  * display the contents of the RDD using `take` or `collect`.


```scala

// Shift+Enter to read in the textfile as RDD[String] and cache it in distributed memory
val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt")
sou17900108.cache() // cache the RDD in memory

```
```scala

sou17900108.count() // Shift+Enter during this count action the RDD is constructed from texfile and cached

```
```scala

sou17900108.count() // Shift+Enter during this count action the cached RDD is used (notice less time taken by the same command)

```
```scala

sou17900108.take(5) // <Cntrl+Enter> to display the first 5 elements of the cached RDD

```



#### Lifecycle of a Spark Program
    
**(watch now 0:23)**:

[![Spark Program Lifecycle by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/HWZUqNYAJj4/0.jpg)](https://www.youtube.com/v/HWZUqNYAJj4?rel=0&autoplay=1&modestbranding=1&start=1)

##### Summary
* create RDDs from:
  * some external data source (such as a distributed file system)
  * parallelized collection in your driver program
* lazily transform these RDDs into new RDDs
* cache some of those RDDs for future reuse
* you perform actions to execute parallel computation to produce results





### Transform lines to words
* We need to loop through each line and split the line into words
* For now, let us split using whitespace
* More sophisticated regular expressions can be used to split the line (as we will see soon)


```scala

sou17900108
.flatMap(line => line.split(" "))
.take(100)

```



### Naive word count
At a first glace, to do a word count of George Washingtons SoU address, we are templed to do the following:
 * just break each line by the whitespace character " " and find the words using a `flatMap`
 * then do the `map` with the closure `word => (word, 1)` to initialize each `word` with a integer count of `1` 
    * ie., transform each word to a *(key, value)* pair or `Tuple` such as `(word, 1)`
 * then count all *value*s with the same *key* (`word` is the Key in our case) by doing a
   * `reduceByKey(_+_)` 
 * and finally `collect()` to display the results.


```scala

sou17900108
.flatMap( line => line.split(" ") )
.map( word => (word, 1) )
.reduceByKey(_+_)
.collect()

```



Unfortunately, as you can see from the `collect` above:
* the words have punctuations at the end which means that the same words are being counted as different words. Eg: importance
* empty words are being counted

So we need a bit of `regex`'ing or regular-expression matching (all readily available from Scala via Java String types).

We will cover the three things we want to do with a simple example from Middle Earth!
* replace all multiple whitespace characters with one white space character " "
* replace all punction characters we specify within `[` and `]` such as `[,?.!:;]` by the empty string "" (i.e., remove these punctuation characters)
* convert everything to lower-case.


```scala

val example = "Master, Master!   It's me, Sméagol... mhrhm*%* But they took away our precious, they wronged us. Gollum will protect us..., Master, it's me Sméagol."

```
```scala

example.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
       .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
       .toLowerCase() // converting to lower-case

```



### More sophisticated word count
We are now ready to do a word count of George Washington's SoU on January 8th 1790 as follows:


```scala

val wordCount_sou17900108 = 
sou17900108
    .flatMap(line => 
         line.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
             .toLowerCase() // converting to lower-case
             .split(" "))
    .map(x => (x, 1))
    .reduceByKey(_+_)
    
wordCount_sou17900108.collect()

```
```scala

val top10 = wordCount_sou17900108.sortBy(_._2, false).collect()

```



### Doing it all together for George Washington and Barrack Obama


```scala

//sc.textFile("dbfs:/datasets/sou/17900108.txt") // George Washington's first SoU
sc.textFile("dbfs:/datasets/sou/20160112.txt")   // Barrack Obama's second SoU
    .flatMap(line => 
         line.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
             .toLowerCase() // converting to lower-case
             .split(" "))
    .map(x => (x,1))
    .reduceByKey(_+_)
    .sortBy(_._2, false)
    .collect()

```



### Reading all SoUs at once using `wholetextFiles`

Let us next read all text files (ending with `.txt`) in the directory `dbfs:/datasets/sou/` at once!

`SparkContext.wholeTextFiles` lets you read a directory containing multiple small text files, and returns each of them as `(filename, content)` pairs of strings. 

This is in contrast with `textFile`, which would return one record per line in each file.


```scala

val souAll = sc.wholeTextFiles("dbfs:/datasets/sou/*.txt") // Shift+Enter to read all text files in dbfs:/datasets/sou/
souAll.cache() // let's cache this RDD for efficient reuse

```
```scala

souAll.count() // Shift+enter to count the number of entries in RDD[(String,String)]

```
```scala

souAll.count() // Cntrl+Enter to count the number of entries in cached RDD[(String,String)] again (much faster!)

```



Let's examine the first two elements of the RDD `souAll`.


```scala

souAll.take(2) // Cntr+Enter to see the first two elements of souAll

```



Clearly, the elements are a pair of Strings, where the first String gives the filename and the second String gives the contents in the file. 

this can be very helpful to simply loop through the files and take an action, such as counting the number of words per address, as folows:


```scala

// this just collects the file names which is the first element of the tuple given by "._1" 
souAll.map( fileContentsPair => fileContentsPair._1).collect()

```



Let us find the number of words in each of the SoU addresses next (we need to work with Strings inside the closure!).


```scala

val wcs = souAll.map( fileContentsPair => 
  {
    val wc = fileContentsPair._2
                             .replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
                             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
                             .toLowerCase() // converting to lower-case
                             .split(" ") // split each word separated by white space
                             .size // find the length of array
    wc
  }    
)      

```
```scala

wcs.collect()

```



## HOMEWORK 
* HOWEWORK WordCount 1: `sortBy`
* HOMEWROK WordCount 2: `dbutils.fs`





##### HOMEWORK WordCount 1. `sortBy`

Let's understand `sortBy` a bit more carefully.


```scala

val example = "Master, Master!   It's me, Sméagol... mhrhm*%* But they took away our precious, they wronged us. Gollum will protect us..., Master, it's me Sméagol."

```
```scala

val words = example.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
       .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
       .toLowerCase() // converting to lower-case
       .split(" ")

```
```scala

val rddWords = sc.parallelize(words)

```
```scala

rddWords.take(10)

```
```scala

val wordCounts = rddWords
                  .map(x => (x,1))
                  .reduceByKey(_+_)

```
```scala

val top10 = wordCounts.sortBy(_._2, false).take(10)

```



Make your code easy to read for other developers ;)  
Use 'case classes' with well defined variable names that everyone can understand


```scala

val top10 = wordCounts.sortBy({
  case (word, count) => count
}, false).take(10)

```



If you just want a total count of all words in the file


```scala

rddWords.count

```



##### HOMEWORK WordCount 2: `dbutils.fs`

Have a brief look at what other commands dbutils.fs supports.  We will introduce them as needed.


```scala

dbutils.fs.help // some of these were used to ETL this data into dbfs:/datasets/sou 

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
