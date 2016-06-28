// Databricks notebook source exported at Tue, 28 Jun 2016 10:37:19 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


## Course Project - High Order Spectral Clustering
### by Xin Zhao

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/04_XinZhao/050_CaseStudy_HighOrderSpectralClustering.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/04_XinZhao/050_CaseStudy_HighOrderSpectralClustering](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=4019&end=4677)





#Example of High Order Spectral Clustering
[Iris flower data set](https://en.wikipedia.org/wiki/Iris_flower_data_set):
The data set consists of 50 samples from each of three species of Iris (Iris setosa, Iris versicolor and Iris virginica). 

setosa: <img src="https://upload.wikimedia.org/wikipedia/commons/5/56/Kosaciec_szczecinkowaty_Iris_setosa.jpg" width="300">,
versicolor: <img src="https://upload.wikimedia.org/wikipedia/commons/4/41/Iris_versicolor_3.jpg" width="400">, and
virginica: <img src="https://upload.wikimedia.org/wikipedia/commons/9/9f/Iris_virginica.jpg" width="400">

Based on Fisher's linear discriminant model, this data set became a typical test case for many statistical classification techniques in machine learning such as support vector machines.
The use of this data set in cluster analysis however is uncommon, since the data set only contains two clusters with rather obvious separation. One of the clusters contains Iris setosa, while the other cluster contains both Iris virginica and Iris versicolor and is not separable without the species information Fisher used. 





The table below shows the iris data set.


```scala

val fisheririsDF = sqlContext.table("fisheriris")
display(fisheririsDF)

```



###Scatter plot
Four features were measured from each sample: the length and the width of the sepals and petals, in centimetres.

<img src="https://upload.wikimedia.org/wikipedia/commons/5/56/Iris_dataset_scatterplot.svg" width="600">

Clustered using 
  * k-means (left) and 
  * true species in the data set (right). 
  
  K-mean result by wikipedia, but it seems wrong
  
![](https://upload.wikimedia.org/wikipedia/commons/1/10/Iris_Flowers_Clustering_kMeans.svg)





##Section1. K-Mean cluster via Mllib kmeans


```scala

import org.apache.spark.mllib.clustering.{KMeansModel, KMeans}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.{sql, SparkContext}
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.functions.monotonicallyIncreasingId

```
```scala

def getKMeanCluster(fisheririsDF:sql.DataFrame, feaNames:Array[String], idcolName: String,
                              nc:Int, sc: SparkContext, sqlContext: SQLContext ): sql.DataFrame ={
    val feasName0=feaNames
    val featureDF: sql.DataFrame = fisheririsDF.select(feasName0.head, feasName0.tail: _*).cache()
    val feasName=featureDF.columns.filter(x=>x!=idcolName)
    val feaRdd=featureDF.rdd.zipWithIndex()
    val outputID=feaRdd.map(v=>(v._2,v._1.getAs[Long](idcolName))).collect().sortBy(_._1).map(x=>x._2)

    def row2denseVector(row: Row, fname:Array[String]):Vector={
      val rowarray=Vectors.dense(Range(0,fname.length).toArray.map(v=>row.getAs[Double](fname(v))))
      rowarray
    }
    val dataArray=feaRdd.collect().sortBy(_._2).map(_._1).map(x=>row2denseVector(x,feasName))

    var mycluster=new KMeans()
    mycluster.setMaxIterations(10)
    mycluster.setK(nc)
    mycluster.setInitializationSteps(5)
    mycluster.setEpsilon(1e-4)

    val data=sc.parallelize(dataArray)

    val clusterModel:KMeansModel = mycluster.run(data)
    var labels: Array[(Int,Long)]=null
    if(outputID==null){
      labels = clusterModel.predict(data).collect().zipWithIndex.map(v=>(v._1,v._2.toLong))
    }else{
      labels = clusterModel.predict(data).collect().zip(outputID)
    }
    //disp labels
    import sqlContext.implicits._
    val labeldf = sc.makeRDD(labels).toDF("label", "id").cache()

    labeldf.registerTempTable("clusterLabel")
    fisheririsDF.registerTempTable("fisheriris")

    val fisheririsCluster=sqlContext.sql(
      "SELECT fisheriris.sepalLength, fisheriris.sepalWidth, " +
        "fisheriris.petalLength, fisheriris.petalWidth, " +
        "fisheriris.id, fisheriris.specie, clusterLabel.label FROM fisheriris, clusterLabel " +
        "WHERE fisheriris.id=clusterLabel.id"
    ).orderBy("id")

 
    //get unique class
    val groupCounts=fisheririsCluster.select("specie","label").groupBy("specie","label").count()
    groupCounts.show()

    fisheririsCluster

  }

```
```scala

val feasName=Array("sepalLength","sepalWidth","petalLength","petalWidth","id")
val nc=3
val fisherDFwithIndex=fisheririsDF.withColumn("id",monotonicallyIncreasingId).cache()
 //cluster on raw feature
val clusterOnRawFeature = getKMeanCluster(fisherDFwithIndex,feasName,"id",nc,sc,sqlContext)

```



K-mean error ratio 11.33%


```scala

clusterOnRawFeature.registerTempTable("kmeanTable")
display(clusterOnRawFeature)

```
```scala

%py
#do plot with true label
import numpy as np
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt

df = sqlContext.table("fisheriris")
y=df.collect()
fea1 = np.asarray(map(lambda x: x[0], y))
fea2 = np.asarray(map(lambda x: x[1], y))
fea3 = np.asarray(map(lambda x: x[2], y))
tl= np.arange(150)
tl[range(0,50)]=0
tl[range(50,100)]=1
tl[range(100,150)]=2

fig1 = plt.figure(figsize=(20,10))
ax = fig1.add_subplot(121, projection='3d')
figpars=[('r', 'o'), ('m', 'x'), ('b','^')] #color and marker
symbol=['setosa','versicolor','virginica']
for i in range(0,3):
    xs = fea1[tl==i]
    ys = fea2[tl==i]
    zs = fea3[tl==i]
    ax.scatter(xs, ys, zs, c=figpars[i][0], marker=figpars[i][1])
    plt.legend(symbol)
    plt.hold(True)

plt.hold(False)
for ii in xrange(60,160,30):
    ax.view_init(elev=10., azim=ii)
plt.axis('tight')
plt.title('Iris flower class')
ax.set_xlabel('X Label')
ax.set_ylabel('Y Label')
ax.set_zlabel('Z Label')

#do plot with kmean
dfkm = sqlContext.table("kmeanTable")
yy=dfkm.collect()
fea1 = np.asarray(map(lambda x: x[0], yy))
fea2 = np.asarray(map(lambda x: x[1], yy))
fea3 = np.asarray(map(lambda x: x[2], yy))
tl = np.asarray(map(lambda x: x[6], yy))

ax = fig1.add_subplot(122, projection='3d')
figpars=[('r', 'o'), ('m', 'x'), ('b','^')] #color and marker
symbol=['cluster_0','cluster_1','cluster_2']
li=[0,1,2]#This is used just to match the marker of two figure below more comparable
for i in range(0,3):
    xs = fea1[tl==li[i]]
    ys = fea2[tl==li[i]]
    zs = fea3[tl==li[i]]
    ax.scatter(xs, ys, zs, c=figpars[i][0], marker=figpars[i][1])
    plt.legend(symbol)
    plt.hold(True)

plt.hold(False)
for ii in xrange(60,160,30):
    ax.view_init(elev=10., azim=ii)
plt.axis('tight')
ax.set_xlabel('X Label')
ax.set_ylabel('Y Label')
ax.set_zlabel('Z Label')
plt.title('Kmean-cluster')

```
```scala

%py
display(fig1)

```



###Section 2. High Order Spectral Clustering


```scala

import tensorSVD.{TensorSVD, RddMatrix, RddTensor}
import highOrderSpectralClustering.graph.ConstructGraph
import highOrderSpectralClustering.clustering.TensorSpectralClustering

```
```scala

def getSpectralCluster(feadf:sql.DataFrame,feaNames:Array[String], idcolName: String,
                                 nc:Int,K:Array[Int],sc:SparkContext,sqlContext: SQLContext ):sql.DataFrame={

    val featureSelected: sql.DataFrame = feadf.select(feaNames.head, feaNames.tail: _*).cache()
    val mycluster= TensorSpectralClustering(featureSelected,nc,K,sc)
    val isLocalTensor=true //false means use distributed tensor method instead which is suggested for larger sample.
    mycluster.run(isLocalTensor,sc)

    import sqlContext.implicits._
    val labeldf = sc.makeRDD(mycluster.labels).toDF("label", "id").cache()

    labeldf.registerTempTable("clusterLabel")
    feadf.registerTempTable("fisheriris")

    val fisheririsCluster=sqlContext.sql(
      "SELECT fisheriris.sepalLength, fisheriris.sepalWidth, " +
        "fisheriris.petalLength, fisheriris.petalWidth, " +
        "fisheriris.id, fisheriris.specie, clusterLabel.label FROM fisheriris, clusterLabel " +
        "WHERE fisheriris.id=clusterLabel.id"
    ).orderBy("id")

    //get unique class
    val groupCounts=fisheririsCluster.select("specie","label").groupBy("specie","label").count().show()

    fisheririsCluster
  }

```
```scala

    val K=Array(5,5,4)
    val nc=3
    val HOSpectralCluster=getSpectralCluster(fisherDFwithIndex,feasName,"id",nc,K,sc,sqlContext)

```



High order spectral clustering error ratio 4%. It shows clearly that the high order spectral clustering done a better job in separating "versicolor" and "virginica" which seems hard to be distinguished by k-means in the original feature space.


```scala

HOSpectralCluster.registerTempTable("HOSpectralTable")
display(HOSpectralCluster)

```
```scala

%py
#do plot with true label
import numpy as np
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt

df = sqlContext.table("fisheriris")
y=df.collect()
fea1 = np.asarray(map(lambda x: x[0], y))
fea2 = np.asarray(map(lambda x: x[1], y))
fea3 = np.asarray(map(lambda x: x[2], y))
tl= np.arange(150)
tl[range(0,50)]=0
tl[range(50,100)]=1
tl[range(100,150)]=2

fig2 = plt.figure(figsize=(20,10))
ax = fig2.add_subplot(121, projection='3d')
figpars=[('r', 'o'), ('m', 'x'), ('b','^')] #color and marker
symbol=['setosa','versicolor','virginica']
for i in range(0,3):
    xs = fea1[tl==i]
    ys = fea2[tl==i]
    zs = fea3[tl==i]
    ax.scatter(xs, ys, zs, c=figpars[i][0], marker=figpars[i][1])
    plt.legend(symbol)
    plt.hold(True)

plt.hold(False)
for ii in xrange(60,160,30):
    ax.view_init(elev=10., azim=ii)
plt.axis('tight')
plt.title('Iris flower class')
ax.set_xlabel('X Label')
ax.set_ylabel('Y Label')
ax.set_zlabel('Z Label')

#do plot with kmean
dfkm = sqlContext.table("HOSpectralTable")
yy=dfkm.collect()
fea1 = np.asarray(map(lambda x: x[0], yy))
fea2 = np.asarray(map(lambda x: x[1], yy))
fea3 = np.asarray(map(lambda x: x[2], yy))
tl = np.asarray(map(lambda x: x[6], yy))

ax = fig2.add_subplot(122, projection='3d')
#figpars=[('r', 'o'), ('m', 'x'), ('b','^')] #color and marker
symbol=['cluster_0','cluster_1','cluster_2']
li=[2,1,0]#This is used just to match the marker of two figure below more comparable
for i in range(0,3):
    xs = fea1[tl==li[i]]
    ys = fea2[tl==li[i]]
    zs = fea3[tl==li[i]]
    ax.scatter(xs, ys, zs, c=figpars[i][0], marker=figpars[i][1])
    plt.legend(symbol)
    plt.hold(True)

plt.hold(False)
for ii in xrange(60,160,30):
    ax.view_init(elev=10., azim=ii)
plt.axis('tight')
ax.set_xlabel('X Label')
ax.set_ylabel('Y Label')
ax.set_zlabel('Z Label')
plt.title('HOSpectral-cluster')

```
```scala

%py
display(fig2)

```



The figures showed above shows a good performance of the high order spectral clustering algorithm.






# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)


## Course Project - High Order Spectral Clustering
### by Xin Zhao

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
