// Databricks notebook source exported at Sun, 28 Feb 2016 05:28:33 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





# Word Count on US State of the Union Addresses

* Word Count in big data is the equivalent of 'Hello World' in programming
* We count the number of occurences of each word in a document






An interesting analysis of the textual content of the *State of the Union (SoU)* addresses by all US presidents was done in:
* [Alix Rule, Jean-Philippe Cointet, and Peter S. Bearman, Lexical shifts, substantive changes, and continuity in State of the Union discourse, 1790–2014, PNAS 2015 112 (35) 10837-10844; doi:10.1073/pnas.1512221112](http://www.pnas.org/content/112/35/10837.full).


![](http://www.pnas.org/content/112/35/10837/F5.large.jpg)

[Fig. 5](http://www.pnas.org/content/112/35/10837.full). A river network captures the flow across history of US political discourse, as perceived by contemporaries. Time moves along the x axis. Clusters on semantic networks of 300 most frequent terms for each of 10 historical periods are displayed as vertical bars. Relations between clusters of adjacent periods are indexed by gray flows, whose density reflects their degree of connection. Streams that connect at any point in history may be considered to be part of the same system, indicated with a single color. 

## Let us investigate this dataset ourselves!
1. We first get the source text data by scraping and parsig from [http://stateoftheunion.onetwothree.net/texts/index.html](http://stateoftheunion.onetwothree.net/texts/index.html) as explained in 
[scraping and parsing SoU addresses](/#workspace/scalable-data-science/week1/03_WordCount/scraperUSStateofUnionAddresses).
* This data is already made available in DBFS, our distributed file system.





###DBFS and dbutils
* Since we are on the databricks cloud, it has a file system called DBFS
* DBFS is similar to HDFS, the Hadoop distributed file system
* dbutils allows us to interact with dbfs.
* The 'display' command displays the list of files in a given directory in the file system.


```scala

display(dbutils.fs.ls("dbfs:/datasets/sou"))

```



Let us display the *head* or the first few lines of the file `dbfs:/datasets/sou/17900108.txt` to see what it contains using `dbutils.fs.head` method.  
`head(file: String, maxBytes: int = 65536): String` -> Returns up to the first 'maxBytes' bytes of the given file as a String encoded in UTF-8
as follows:


```scala

dbutils.fs.head("dbfs:/datasets/sou/17900108.txt",1000) // first 1000 bytes of the file

```



### Read the file into Spark
* The `textFile` method on the available `SparkContext` `sc` can read the text file `sou17900108` into Spark and return an RDD of Strings
* Each String represents one line of data from the file and can be displayed using `take` or `collect`.


```scala

val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt")

```
```scala

sou17900108.take(5)

```
```scala

sou17900108.collect

```



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
    .map(x => (x,1))
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
