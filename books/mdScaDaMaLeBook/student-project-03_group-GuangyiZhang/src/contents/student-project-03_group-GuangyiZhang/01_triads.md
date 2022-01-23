<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Signed Triads in Social Media
=============================

</div>

<div class="cell markdown">

By **Guangyi Zhang** (guaz@kth.se)

Please click [HERE](https://drive.google.com/file/d/1TrxhdSxsU1qKk_SywKf2nUA8mUAO4CG4/view?usp=sharing) to watch the accompanying video.

</div>

<div class="cell markdown">

Introduction
------------

</div>

<div class="cell markdown">

This project aims to verify the friend-foe motifs in a large-scale signed social network.

A signed network is a graph that contains both positive and negative links. The sign of a link contains rich semantics in different appliations. For example, in a social network, positive links can indicate friendly relationships, while negative ones indicate antagonistic interactions.

In on-line discussion sites such as Slashdot, users can tag other users as “friends” and “foes”. These provide us exemplary datasets to study a online signed network. In this notebook we explore the a dataset from Epinions, which contains up to 119,217 nodes, 841,200 edges, and millions of motifs. Epinions is the trust network of the Epinions product review web site, where users can indicate their trust or distrust of the reviews of others. We analyze the network data in an undirected representation.

References:

Leskovec, Jure, Daniel Huttenlocher, and Jon Kleinberg. "Signed networks in social media." Proceedings of the SIGCHI conference on human factors in computing systems. 2010.

</div>

<div class="cell markdown">

Regarding the motifs, we investigate several interesting triads that are related to *structural balance theory* in an online social signed network. Structural balance originates in social psychology in the mid-20th-century, and considers the possible ways in which triangles on three individuals can be signed.

Let us explain different types of triads, which is shown in the figure below,

-   T3: “the friend of my friend is my friend”
-   T1: “the friend of my enemy is my enemy,” “the enemy of my friend is my enemy” and “the enemy of my enemy is my friend”
-   T2 and T0: does not quite make sense in social network. For example, two friends of mine are unlikely to be enemy to each other.

Our goal is to compare the numbers of different triads in our appointed dataset.

![triads](https://drive.google.com/uc?export=view&id=1QY9ouqxbVqpH3KLl-x-QyR72yxtE0vSX)

</div>

<div class="cell markdown">

Download dataset
----------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
pwd
```

<div class="output execute_result plain_result" execution_count="1">

    /databricks/driver

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
wget http://snap.stanford.edu/data/soc-sign-epinions.txt.gz
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls -l
```

<div class="output execute_result plain_result" execution_count="1">

    total 2924
    drwxr-xr-x  2 root root    4096 Jan  1  1970 conf
    -rw-r--r--  1 root root     733 Nov 24 15:24 derby.log
    drwxr-xr-x 10 root root    4096 Nov 24 15:24 eventlogs
    drwxr-xr-x  2 root root    4096 Nov 24 16:15 ganglia
    drwxr-xr-x  2 root root    4096 Nov 24 16:04 logs
    -rw-r--r--  1 root root 2972840 Dec  3  2009 soc-sign-epinions.txt.gz

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
gunzip soc-sign-epinions.txt.gz
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls -l
```

<div class="output execute_result plain_result" execution_count="1">

    total 11000
    drwxr-xr-x  2 root root     4096 Jan  1  1970 conf
    -rw-r--r--  1 root root      733 Nov 24 15:24 derby.log
    drwxr-xr-x 10 root root     4096 Nov 24 15:24 eventlogs
    drwxr-xr-x  2 root root     4096 Nov 24 16:15 ganglia
    drwxr-xr-x  2 root root     4096 Nov 24 16:04 logs
    -rw-r--r--  1 root root 11243141 Dec  3  2009 soc-sign-epinions.txt

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
head soc-sign-epinions.txt
```

<div class="output execute_result plain_result" execution_count="1">

    # Directed graph: soc-sign-epinions
    # Epinions signed social network
    # Nodes: 131828 Edges: 841372
    # FromNodeId	ToNodeId	Sign
    0	1	-1
    1	128552	-1
    2	3	1
    4	5	-1
    4	155	-1
    4	558	1

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
mkdir -p epinions
mv soc-sign-epinions.txt epinions/
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` sh
ls -l /dbfs/FileStore
mv epinions /dbfs/FileStore/
```

<div class="output execute_result plain_result" execution_count="1">

    total 33
    drwxrwxrwx 2 root root   24 May  1  2018 datasets_magellan
    drwxrwxrwx 2 root root 4096 Nov 24 11:14 DIGSUM-files
    drwxrwxrwx 2 root root 4096 Nov 24 11:14 import-stage
    drwxrwxrwx 2 root root 4096 Nov 24 11:14 jars
    drwxrwxrwx 2 root root 4096 Nov 24 11:14 plots
    drwxrwxrwx 2 root root 4096 Nov 24 11:14 shared_uploads
    drwxrwxrwx 2 root root 4096 Nov 24 11:14 simon_temp_files_feel_free_to_delete_any_time
    drwxrwxrwx 2 root root 4096 Nov 24 11:14 tables
    drwxrwxrwx 2 root root 4096 Nov 24 11:14 timelinesOfInterest
    mv: preserving permissions for ‘/dbfs/FileStore/epinions/soc-sign-epinions.txt’: Operation not permitted
    mv: preserving permissions for ‘/dbfs/FileStore/epinions’: Operation not permitted

</div>

</div>

<div class="cell code" execution_count="1" scrolled="true">

``` fs
ls /
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>path</th>
<th>name</th>
<th>size</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>dbfs:/FileStore/</td>
<td>FileStore/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/_checkpoint/</td>
<td>_checkpoint/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/databricks/</td>
<td>databricks/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/databricks-datasets/</td>
<td>databricks-datasets/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/databricks-results/</td>
<td>databricks-results/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/datasets/</td>
<td>datasets/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/digsum-dataframe.csv/</td>
<td>digsum-dataframe.csv/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/local_disk0/</td>
<td>local_disk0/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/ml/</td>
<td>ml/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/mnt/</td>
<td>mnt/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/mytmpdir-forUserTimeLine/</td>
<td>mytmpdir-forUserTimeLine/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/results/</td>
<td>results/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/test/</td>
<td>test/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/tmp/</td>
<td>tmp/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/tmpdir/</td>
<td>tmpdir/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/user/</td>
<td>user/</td>
<td>0.0</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` fs
ls /FileStore
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>path</th>
<th>name</th>
<th>size</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>dbfs:/FileStore/DIGSUM-files/</td>
<td>DIGSUM-files/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/datasets_magellan/</td>
<td>datasets_magellan/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/FileStore/epinions/</td>
<td>epinions/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/import-stage/</td>
<td>import-stage/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/FileStore/jars/</td>
<td>jars/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/plots/</td>
<td>plots/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/FileStore/shared_uploads/</td>
<td>shared_uploads/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/simon_temp_files_feel_free_to_delete_any_time/</td>
<td>simon_temp_files_feel_free_to_delete_any_time/</td>
<td>0.0</td>
</tr>
<tr class="odd">
<td>dbfs:/FileStore/tables/</td>
<td>tables/</td>
<td>0.0</td>
</tr>
<tr class="even">
<td>dbfs:/FileStore/timelinesOfInterest/</td>
<td>timelinesOfInterest/</td>
<td>0.0</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` fs
ls file:/databricks/driver
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>path</th>
<th>name</th>
<th>size</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>file:/databricks/driver/conf/</td>
<td>conf/</td>
<td>4096.0</td>
</tr>
<tr class="even">
<td>file:/databricks/driver/logs/</td>
<td>logs/</td>
<td>4096.0</td>
</tr>
<tr class="odd">
<td>file:/databricks/driver/derby.log</td>
<td>derby.log</td>
<td>733.0</td>
</tr>
<tr class="even">
<td>file:/databricks/driver/ganglia/</td>
<td>ganglia/</td>
<td>4096.0</td>
</tr>
<tr class="odd">
<td>file:/databricks/driver/eventlogs/</td>
<td>eventlogs/</td>
<td>4096.0</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
//%fs mv file:///databricks/driver/epinions /FileStore/
%fs ls /FileStore/epinions/
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>path</th>
<th>name</th>
<th>size</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>dbfs:/FileStore/epinions/soc-sign-epinions.txt</td>
<td>soc-sign-epinions.txt</td>
<td>1.1243141e7</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell markdown">

Preprocess dataset
------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import org.graphframes._

// This import is needed to use the $-notation
import spark.implicits._
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql._
    import org.apache.spark.sql.functions._
    import org.graphframes._
    import spark.implicits._

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
var df = spark.read.format("csv")
//   .option("header", "true")
  .option("inferSchema", "true")
  .option("comment", "#")
  .option("sep", "\t")
  .load("/FileStore/epinions")
```

<div class="output execute_result plain_result" execution_count="1">

    df: org.apache.spark.sql.DataFrame = [_c0: int, _c1: int ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df.count()
```

<div class="output execute_result plain_result" execution_count="1">

    res1: Long = 841372

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df.rdd.getNumPartitions 
```

<div class="output execute_result plain_result" execution_count="1">

    res36: Int = 3

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df.head(3)
```

<div class="output execute_result plain_result" execution_count="1">

    res37: Array[org.apache.spark.sql.Row] = Array([0,1,-1], [1,128552,-1], [2,3,1])

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df.printSchema()
```

<div class="output execute_result plain_result" execution_count="1">

    root
     |-- _c0: integer (nullable = true)
     |-- _c1: integer (nullable = true)
     |-- _c2: integer (nullable = true)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val newNames = Seq("src", "dst", "rela")
val e = df.toDF(newNames: _*)
```

<div class="output execute_result plain_result" execution_count="1">

    newNames: Seq[String] = List(src, dst, rela)
    e: org.apache.spark.sql.DataFrame = [src: int, dst: int ... 1 more field]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
e.printSchema()
```

<div class="output execute_result plain_result" execution_count="1">

    root
     |-- src: integer (nullable = true)
     |-- dst: integer (nullable = true)
     |-- rela: integer (nullable = true)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Vertex DataFrame
val v = spark.range(1, 131827).toDF("id")
```

<div class="output execute_result plain_result" execution_count="1">

    v: org.apache.spark.sql.DataFrame = [id: bigint]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val g = GraphFrame(v, e)
```

<div class="output execute_result plain_result" execution_count="1">

    g: org.graphframes.GraphFrame = GraphFrame(v:[id: bigint], e:[src: int, dst: int ... 1 more field])

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
g.edges.take(3)
```

<div class="output execute_result plain_result" execution_count="1">

    res15: Array[org.apache.spark.sql.Row] = Array([0,1,-1], [1,128552,-1], [2,3,1])

</div>

</div>

<div class="cell markdown">

Count triads
------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// val results = g.triangleCount.run()
```

</div>

<div class="cell markdown">

We can not make use of the convenient API `triangleCount()` because it does not take the sign of edges into consideration. We need to write our own code to find triads.

</div>

<div class="cell markdown">

First, a triad should be undirected, but our graph concists of only directed edges.

One strategy is to keep only bi-direction edges of the same sign. But we need to examine how large is the proportion of edges we will lose.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Search for pairs of vertices with edges in both directions between them, i.e., find undirected or bidirected edges.
val pair = g.find("(a)-[e1]->(b); (b)-[e2]->(a)")
println(pair.count())
val filtered = pair.filter("e1.rela == e2.rela")
println(filtered.count())
```

<div class="output execute_result plain_result" execution_count="1">

    259751
    254345
    pair: org.apache.spark.sql.DataFrame = [a: struct<id: bigint>, e1: struct<src: int, dst: int ... 1 more field> ... 2 more fields]
    filtered: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [a: struct<id: bigint>, e1: struct<src: int, dst: int ... 1 more field> ... 2 more fields]

</div>

</div>

<div class="cell markdown">

Fortunately, we only lose a very small amount of edges.

It also makes sense for this dataset, because if A trusts B, then it is quite unlikely that B does not trust A.

</div>

<div class="cell markdown">

In order to count different triads, first we have to find all triads.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val triad = g.find("(a)-[eab]->(b); (b)-[eba]->(a); (b)-[ebc]->(c); (c)-[ecb]->(b); (c)-[eca]->(a); (a)-[eac]->(c)")
println(triad.count())
```

<div class="output execute_result plain_result" execution_count="1">

    3314925
    triad: org.apache.spark.sql.DataFrame = [a: struct<id: bigint>, eab: struct<src: int, dst: int ... 1 more field> ... 7 more fields]

</div>

</div>

<div class="cell markdown">

After finding all triads, we find each type by filtering.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val t111 = triad.filter("eab.rela = 1 AND eab.rela = ebc.rela AND ebc.rela = eca.rela")
println(t111.count())
```

<div class="output execute_result plain_result" execution_count="1">

    3232357
    t111: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [a: struct<id: bigint>, eab: struct<src: int, dst: int ... 1 more field> ... 7 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val t000 = triad.filter("eab.rela = -1 AND eab.rela = ebc.rela AND ebc.rela = eca.rela")
println(t000.count())
```

<div class="output execute_result plain_result" execution_count="1">

    1610
    t000: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [a: struct<id: bigint>, eab: struct<src: int, dst: int ... 1 more field> ... 7 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val t110 = triad.filter("eab.rela + ebc.rela + eca.rela = 1")
println(t110.count())
```

<div class="output execute_result plain_result" execution_count="1">

    62634
    t110: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [a: struct<id: bigint>, eab: struct<src: int, dst: int ... 1 more field> ... 7 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val t001 = triad.filter("eab.rela + ebc.rela + eca.rela = -1")
println(t001.count())
```

<div class="output execute_result plain_result" execution_count="1">

    18324
    t001: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [a: struct<id: bigint>, eab: struct<src: int, dst: int ... 1 more field> ... 7 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val n111 = t111.count()
val n001 = t001.count()
val n000 = t000.count() 
val n110 = t110.count()
val imbalanced = n000 + n110
val balanced = n111 + n001
```

<div class="output execute_result plain_result" execution_count="1">

    n111: Long = 3232357
    n001: Long = 18324
    n000: Long = 1610
    n110: Long = 62634
    imbalanced: Long = 64244
    balanced: Long = 3250681

</div>

</div>

<div class="cell markdown">

As we can see, the number of balanced triads overwhelms the number of imbalanced ones, which verifies the effectiveness of structural balance theory.

</div>

<div class="cell markdown">

Duplicates
----------

</div>

<div class="cell markdown">

Some tests about duplicated motifs

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val g: GraphFrame = examples.Graphs.friends
```

<div class="output execute_result plain_result" execution_count="1">

    g: org.graphframes.GraphFrame = GraphFrame(v:[id: string, name: string ... 1 more field], e:[src: string, dst: string ... 1 more field])

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(g.edges)
```

<div class="output execute_result tabular_result" execution_count="1">

<table>
<thead>
<tr class="header">
<th>src</th>
<th>dst</th>
<th>relationship</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>a</td>
<td>b</td>
<td>friend</td>
</tr>
<tr class="even">
<td>b</td>
<td>c</td>
<td>follow</td>
</tr>
<tr class="odd">
<td>c</td>
<td>b</td>
<td>follow</td>
</tr>
<tr class="even">
<td>f</td>
<td>c</td>
<td>follow</td>
</tr>
<tr class="odd">
<td>e</td>
<td>f</td>
<td>follow</td>
</tr>
<tr class="even">
<td>e</td>
<td>d</td>
<td>friend</td>
</tr>
<tr class="odd">
<td>d</td>
<td>a</td>
<td>friend</td>
</tr>
<tr class="even">
<td>a</td>
<td>e</td>
<td>friend</td>
</tr>
</tbody>
</table>

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
val motifs = g.find("(a)-[e]->(b); (b)-[e2]->(a)")
motifs.show()
```

<div class="output execute_result plain_result" execution_count="1">

    +----------------+--------------+----------------+--------------+
    |               a|             e|               b|            e2|
    +----------------+--------------+----------------+--------------+
    |    [b, Bob, 36]|[b, c, follow]|[c, Charlie, 30]|[c, b, follow]|
    |[c, Charlie, 30]|[c, b, follow]|    [b, Bob, 36]|[b, c, follow]|
    +----------------+--------------+----------------+--------------+

    motifs: org.apache.spark.sql.DataFrame = [a: struct<id: string, name: string ... 1 more field>, e: struct<src: string, dst: string ... 1 more field> ... 2 more fields]

</div>

</div>

<div class="cell markdown">

As shown above, bi-direction edges are reported twice. Therefore, each triad is counted three times. However, this does not matter in our project, because the ratios between different triads remain the same.

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>
