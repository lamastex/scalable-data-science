// Databricks notebook source exported at Thu, 25 Feb 2016 21:25:02 UTC

# Extract, Transform and Load (ETL) of the SoU Addresses

### A bit of bash and lynx to achieve the scraping of the state of the union addresses of the US Presidents
#### by Paul Brouwers 
### And some Shell-level parsed-data exploration, injection into the distributed file system and testing
#### by Raazesh Sainudiin

The code below is mainly there to show how the text content of each state of the union address was scraped from the following URL:
* [http://stateoftheunion.onetwothree.net/texts/index.html](http://stateoftheunion.onetwothree.net/texts/index.html)

Such data acquisition task or ETL is usually the first and crucial step in a data scientist's workflow.
A data scientist generally does the scraping and parsing of the data by her/himself.  
Data ingestion not only allows the scientist to start the analysis but also determines the quality of the analysis by the limits it imposes on the accessible feature space.

We have done this and put the data in the distributed file system for easy loading into our notebooks for further analysis.  This keeps us from having to install unix programs like ``lynx``, ``sed``, etc. that are needed in the shell script below.

```%sh
for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html | grep texts | grep -v index | sed 's/.*http/http/') ; do lynx --dump $i | tail -n+13 | head -n-14 | sed 's/^\s\+//' | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt ; done
```

Or in a more atomic form:

```%sh
for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html \

        | grep texts \

        | grep -v index \

        | sed 's/.*http/http/')

do 

        lynx --dump $i \

               | tail -n+13 \

               | head -n-14 \

               | sed 's/^\s\+//' \

               | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' \

               > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt

done
```
**Don't re-evaluate!**

The following BASH (shell) script can be made to work on databricks cloud directly by installing the dependencies such as ``lynx``, etc.  Since we have already scraped it and put the data in our distributed file system **let's not evaluate or ``<Ctrl+Enter>`` the cell below**.  The cell is mainly there to show how it can be done (you may want to modify it to scrape other sites for other text data).


```scala

%sh
#remove the hash character from the line below to evaluate when needed
#for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html | grep texts | grep -v index | sed 's/.*http/http/') ; do lynx --dump $i | tail -n+13 | head -n-14 | sed 's/^\s\+//' | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt ; done

```
```scala

%sh
pwd && ls && du -sh .

```
```scala

%sh ls /home/ubuntu && du -sh /home/ubuntu

```



We can just grab the data as a tarball (gnuZipped tar archive) file ``sou.tar.gz`` using wget as follows:

%sh
wget http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/SOU/sou.tar.gz


```scala

%sh
df -h
pwd

```
```scala

%sh
wget http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/SOU/sou.tar.gz

```
```scala

%sh
ls

```
```scala

%sh
env

```
```scala

%sh
tar zxvf sou.tar.gz

```
```scala

%sh cd sou && ls

```
```scala

%sh head sou/17900108.txt

```
```scala

%sh tail sou/17900108.txt

```
```scala

%sh head sou/20150120.txt

```
```scala

%sh tail sou/20150120.txt

```
```scala

display(dbutils.fs.ls("dbfs:/"))

```
```scala

display(dbutils.fs.ls("dbfs:/datasets"))

```
```scala

dbutils.fs.mkdirs("dbfs:/datasets/sou") //need not be done again!

```
```scala

display(dbutils.fs.ls("dbfs:/datasets"))

```
```scala

%sh pwd && ls

```
```scala

dbutils.fs.help

```
```scala

dbutils.fs.cp("file:/databricks/driver/sou", "dbfs:/datasets/sou/",recurse=true)

```
```scala

display(dbutils.fs.ls("dbfs:/datasets/sou"))

```
```scala

display(dbutils.fs.ls("dbfs:/datasets/"))

```
```scala

val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt")

```
```scala

sou17900108.take(5)

```
```scala

sou17900108.collect

```
```scala

sou17900108.takeOrdered(5)

```
```scala

val souAll = sc.wholeTextFiles("dbfs:/datasets/sou/*.txt")

```
```scala

souAll.count

```
```scala

souAll.take(2)

```
```scala

```
