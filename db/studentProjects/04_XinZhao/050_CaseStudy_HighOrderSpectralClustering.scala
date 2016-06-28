// Databricks notebook source exported at Tue, 28 Jun 2016 10:37:19 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ## Course Project - High Order Spectral Clustering
// MAGIC ### by Xin Zhao
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/04_XinZhao/050_CaseStudy_HighOrderSpectralClustering.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/studentProjects/04_XinZhao/050_CaseStudy_HighOrderSpectralClustering](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=4019&end=4677)

// COMMAND ----------

// MAGIC %md
// MAGIC #Example of High Order Spectral Clustering
// MAGIC [Iris flower data set](https://en.wikipedia.org/wiki/Iris_flower_data_set):
// MAGIC The data set consists of 50 samples from each of three species of Iris (Iris setosa, Iris versicolor and Iris virginica). 
// MAGIC 
// MAGIC setosa: <img src="https://upload.wikimedia.org/wikipedia/commons/5/56/Kosaciec_szczecinkowaty_Iris_setosa.jpg" width="300">,
// MAGIC versicolor: <img src="https://upload.wikimedia.org/wikipedia/commons/4/41/Iris_versicolor_3.jpg" width="400">, and
// MAGIC virginica: <img src="https://upload.wikimedia.org/wikipedia/commons/9/9f/Iris_virginica.jpg" width="400">
// MAGIC 
// MAGIC Based on Fisher's linear discriminant model, this data set became a typical test case for many statistical classification techniques in machine learning such as support vector machines.
// MAGIC The use of this data set in cluster analysis however is uncommon, since the data set only contains two clusters with rather obvious separation. One of the clusters contains Iris setosa, while the other cluster contains both Iris virginica and Iris versicolor and is not separable without the species information Fisher used. 

// COMMAND ----------

// MAGIC %md
// MAGIC The table below shows the iris data set.

// COMMAND ----------

val fisheririsDF = sqlContext.table("fisheriris")
display(fisheririsDF)

// COMMAND ----------

// MAGIC %md
// MAGIC ###Scatter plot
// MAGIC Four features were measured from each sample: the length and the width of the sepals and petals, in centimetres.
// MAGIC 
// MAGIC <img src="https://upload.wikimedia.org/wikipedia/commons/5/56/Iris_dataset_scatterplot.svg" width="600">
// MAGIC 
// MAGIC Clustered using 
// MAGIC   * k-means (left) and 
// MAGIC   * true species in the data set (right). 
// MAGIC   
// MAGIC   K-mean result by wikipedia, but it seems wrong
// MAGIC   
// MAGIC ![](https://upload.wikimedia.org/wikipedia/commons/1/10/Iris_Flowers_Clustering_kMeans.svg)

// COMMAND ----------

// MAGIC %md
// MAGIC ##Section1. K-Mean cluster via Mllib kmeans

// COMMAND ----------

import org.apache.spark.mllib.clustering.{KMeansModel, KMeans}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.{sql, SparkContext}
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.functions.monotonicallyIncreasingId

// COMMAND ----------

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

// COMMAND ----------

val feasName=Array("sepalLength","sepalWidth","petalLength","petalWidth","id")
val nc=3
val fisherDFwithIndex=fisheririsDF.withColumn("id",monotonicallyIncreasingId).cache()
 //cluster on raw feature
val clusterOnRawFeature = getKMeanCluster(fisherDFwithIndex,feasName,"id",nc,sc,sqlContext)

// COMMAND ----------

// MAGIC %md
// MAGIC K-mean error ratio 11.33%

// COMMAND ----------

clusterOnRawFeature.registerTempTable("kmeanTable")
display(clusterOnRawFeature)

// COMMAND ----------

// MAGIC %py
// MAGIC #do plot with true label
// MAGIC import numpy as np
// MAGIC from mpl_toolkits.mplot3d import Axes3D
// MAGIC import matplotlib.pyplot as plt
// MAGIC 
// MAGIC df = sqlContext.table("fisheriris")
// MAGIC y=df.collect()
// MAGIC fea1 = np.asarray(map(lambda x: x[0], y))
// MAGIC fea2 = np.asarray(map(lambda x: x[1], y))
// MAGIC fea3 = np.asarray(map(lambda x: x[2], y))
// MAGIC tl= np.arange(150)
// MAGIC tl[range(0,50)]=0
// MAGIC tl[range(50,100)]=1
// MAGIC tl[range(100,150)]=2
// MAGIC 
// MAGIC fig1 = plt.figure(figsize=(20,10))
// MAGIC ax = fig1.add_subplot(121, projection='3d')
// MAGIC figpars=[('r', 'o'), ('m', 'x'), ('b','^')] #color and marker
// MAGIC symbol=['setosa','versicolor','virginica']
// MAGIC for i in range(0,3):
// MAGIC     xs = fea1[tl==i]
// MAGIC     ys = fea2[tl==i]
// MAGIC     zs = fea3[tl==i]
// MAGIC     ax.scatter(xs, ys, zs, c=figpars[i][0], marker=figpars[i][1])
// MAGIC     plt.legend(symbol)
// MAGIC     plt.hold(True)
// MAGIC 
// MAGIC plt.hold(False)
// MAGIC for ii in xrange(60,160,30):
// MAGIC     ax.view_init(elev=10., azim=ii)
// MAGIC plt.axis('tight')
// MAGIC plt.title('Iris flower class')
// MAGIC ax.set_xlabel('X Label')
// MAGIC ax.set_ylabel('Y Label')
// MAGIC ax.set_zlabel('Z Label')
// MAGIC 
// MAGIC #do plot with kmean
// MAGIC dfkm = sqlContext.table("kmeanTable")
// MAGIC yy=dfkm.collect()
// MAGIC fea1 = np.asarray(map(lambda x: x[0], yy))
// MAGIC fea2 = np.asarray(map(lambda x: x[1], yy))
// MAGIC fea3 = np.asarray(map(lambda x: x[2], yy))
// MAGIC tl = np.asarray(map(lambda x: x[6], yy))
// MAGIC 
// MAGIC ax = fig1.add_subplot(122, projection='3d')
// MAGIC figpars=[('r', 'o'), ('m', 'x'), ('b','^')] #color and marker
// MAGIC symbol=['cluster_0','cluster_1','cluster_2']
// MAGIC li=[0,1,2]#This is used just to match the marker of two figure below more comparable
// MAGIC for i in range(0,3):
// MAGIC     xs = fea1[tl==li[i]]
// MAGIC     ys = fea2[tl==li[i]]
// MAGIC     zs = fea3[tl==li[i]]
// MAGIC     ax.scatter(xs, ys, zs, c=figpars[i][0], marker=figpars[i][1])
// MAGIC     plt.legend(symbol)
// MAGIC     plt.hold(True)
// MAGIC 
// MAGIC plt.hold(False)
// MAGIC for ii in xrange(60,160,30):
// MAGIC     ax.view_init(elev=10., azim=ii)
// MAGIC plt.axis('tight')
// MAGIC ax.set_xlabel('X Label')
// MAGIC ax.set_ylabel('Y Label')
// MAGIC ax.set_zlabel('Z Label')
// MAGIC plt.title('Kmean-cluster')

// COMMAND ----------

// MAGIC %py
// MAGIC display(fig1)

// COMMAND ----------

// MAGIC %md
// MAGIC ###Section 2. High Order Spectral Clustering

// COMMAND ----------

import tensorSVD.{TensorSVD, RddMatrix, RddTensor}
import highOrderSpectralClustering.graph.ConstructGraph
import highOrderSpectralClustering.clustering.TensorSpectralClustering

// COMMAND ----------

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

// COMMAND ----------

    val K=Array(5,5,4)
    val nc=3
    val HOSpectralCluster=getSpectralCluster(fisherDFwithIndex,feasName,"id",nc,K,sc,sqlContext)

// COMMAND ----------

// MAGIC %md
// MAGIC High order spectral clustering error ratio 4%. It shows clearly that the high order spectral clustering done a better job in separating "versicolor" and "virginica" which seems hard to be distinguished by k-means in the original feature space.

// COMMAND ----------

HOSpectralCluster.registerTempTable("HOSpectralTable")
display(HOSpectralCluster)

// COMMAND ----------

// MAGIC %py
// MAGIC #do plot with true label
// MAGIC import numpy as np
// MAGIC from mpl_toolkits.mplot3d import Axes3D
// MAGIC import matplotlib.pyplot as plt
// MAGIC 
// MAGIC df = sqlContext.table("fisheriris")
// MAGIC y=df.collect()
// MAGIC fea1 = np.asarray(map(lambda x: x[0], y))
// MAGIC fea2 = np.asarray(map(lambda x: x[1], y))
// MAGIC fea3 = np.asarray(map(lambda x: x[2], y))
// MAGIC tl= np.arange(150)
// MAGIC tl[range(0,50)]=0
// MAGIC tl[range(50,100)]=1
// MAGIC tl[range(100,150)]=2
// MAGIC 
// MAGIC fig2 = plt.figure(figsize=(20,10))
// MAGIC ax = fig2.add_subplot(121, projection='3d')
// MAGIC figpars=[('r', 'o'), ('m', 'x'), ('b','^')] #color and marker
// MAGIC symbol=['setosa','versicolor','virginica']
// MAGIC for i in range(0,3):
// MAGIC     xs = fea1[tl==i]
// MAGIC     ys = fea2[tl==i]
// MAGIC     zs = fea3[tl==i]
// MAGIC     ax.scatter(xs, ys, zs, c=figpars[i][0], marker=figpars[i][1])
// MAGIC     plt.legend(symbol)
// MAGIC     plt.hold(True)
// MAGIC 
// MAGIC plt.hold(False)
// MAGIC for ii in xrange(60,160,30):
// MAGIC     ax.view_init(elev=10., azim=ii)
// MAGIC plt.axis('tight')
// MAGIC plt.title('Iris flower class')
// MAGIC ax.set_xlabel('X Label')
// MAGIC ax.set_ylabel('Y Label')
// MAGIC ax.set_zlabel('Z Label')
// MAGIC 
// MAGIC #do plot with kmean
// MAGIC dfkm = sqlContext.table("HOSpectralTable")
// MAGIC yy=dfkm.collect()
// MAGIC fea1 = np.asarray(map(lambda x: x[0], yy))
// MAGIC fea2 = np.asarray(map(lambda x: x[1], yy))
// MAGIC fea3 = np.asarray(map(lambda x: x[2], yy))
// MAGIC tl = np.asarray(map(lambda x: x[6], yy))
// MAGIC 
// MAGIC ax = fig2.add_subplot(122, projection='3d')
// MAGIC #figpars=[('r', 'o'), ('m', 'x'), ('b','^')] #color and marker
// MAGIC symbol=['cluster_0','cluster_1','cluster_2']
// MAGIC li=[2,1,0]#This is used just to match the marker of two figure below more comparable
// MAGIC for i in range(0,3):
// MAGIC     xs = fea1[tl==li[i]]
// MAGIC     ys = fea2[tl==li[i]]
// MAGIC     zs = fea3[tl==li[i]]
// MAGIC     ax.scatter(xs, ys, zs, c=figpars[i][0], marker=figpars[i][1])
// MAGIC     plt.legend(symbol)
// MAGIC     plt.hold(True)
// MAGIC 
// MAGIC plt.hold(False)
// MAGIC for ii in xrange(60,160,30):
// MAGIC     ax.view_init(elev=10., azim=ii)
// MAGIC plt.axis('tight')
// MAGIC ax.set_xlabel('X Label')
// MAGIC ax.set_ylabel('Y Label')
// MAGIC ax.set_zlabel('Z Label')
// MAGIC plt.title('HOSpectral-cluster')

// COMMAND ----------

// MAGIC %py
// MAGIC display(fig2)

// COMMAND ----------

// MAGIC %md
// MAGIC The figures showed above shows a good performance of the high order spectral clustering algorithm.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ## Course Project - High Order Spectral Clustering
// MAGIC ### by Xin Zhao
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)