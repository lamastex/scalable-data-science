// Databricks notebook source exported at Tue, 28 Jun 2016 10:36:27 UTC
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
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/04_XinZhao/049_Introduction_HighOrderSpectralClustering.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/studentProjects/04_XinZhao/049_Introduction_HighOrderSpectralClustering](http://img.youtube.com/vi/zJirlHAV6YU/0.jpg)](https://www.youtube.com/v/zJirlHAV6YU?rel=0&autoplay=1&modestbranding=1&start=3113&end=4018)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 1. Spectral clustering
// MAGIC #### 1.1 What is spectral clustering
// MAGIC In multivariate statistics and the clustering of data, spectral clustering techniques make use of the spectrum (eigenvalues) of the **similarity matrix** of the data to perform dimensionality reduction before clustering in fewer dimensions. 
// MAGIC 
// MAGIC The similarity matrix is provided as an input and consists of a quantitative assessment of the relative similarity of each pair of points in the dataset. Such measure is used to transform data to overcome difficulties related to lack of convexity in the shape of the data distribution. The measure gives rise to an (n, n)-sized similarity matrix for a set of (n,d) points, where the entry (i,j) in the matrix can be simply the Euclidean distance between i and j, or it can be a more complex measure of distance such as the **Gaussian**. Further modifying this result with network analysis techniques is also common.
// MAGIC 
// MAGIC In mathematics, spectral graph theory is the study of properties of a graph in relationship to the characteristic polynomial, **eigenvalues, and eigenvectors** of matrices associated to the graph, such as its adjacency matrix or **Laplacian** matrix.
// MAGIC 
// MAGIC 
// MAGIC <img src="http://image.slidesharecdn.com/11clusadvanced-140913212136-phpapp02/95/data-mining-concepts-and-techniques-chapter-11review-basic-cluster-analysis-methods-11-clusadvanced-45-638.jpg?cb=1410644502" width="800">
// MAGIC #### 1.2 Spectral clustering with different clustering method
// MAGIC The following two examples show two different Spectral clustering method as they uses different clustering algorithm:
// MAGIC 
// MAGIC ![](http://scikit-learn.org/stable/_images/plot_face_segmentation_001.png)   ![](http://scikit-learn.org/stable/_images/plot_face_segmentation_002.png)
// MAGIC 
// MAGIC #### 1.3 Compare variaty clustering methods
// MAGIC <img src="http://opensource.datacratic.com/mtlpy50/clustering.png" width="1200">

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. High Order Spectral Clustering
// MAGIC 
// MAGIC #### 2.1 Why high order
// MAGIC >- kernel principal component analysis (kernel PCA) in not enough in multivariate analysis
// MAGIC 
// MAGIC >- linkage to the original feature
// MAGIC 
// MAGIC #### 2.2 How
// MAGIC >- high order spectral cluster diagrams 
// MAGIC 
// MAGIC >- **Data --> Graph/Similarity Tensor --> Compute leading K (vector) svd and core tensor --> Clustering on the new latent tensor --> Project clusters back to original data**
// MAGIC 
// MAGIC #### 2.3 Tensor
// MAGIC >- What is tensor
// MAGIC 
// MAGIC <img src="http://i.stack.imgur.com/K4Cg9.png" width="800">
// MAGIC 
// MAGIC >- High order SVD
// MAGIC 
// MAGIC <img src="https://www.microsoft.com/en-us/research/wp-content/uploads/2016/03/trajectorycomputing-tensors.png" width="1200">
// MAGIC 
// MAGIC >- Tensor unfolding
// MAGIC 
// MAGIC <img src="https://s3-eu-west-1.amazonaws.com/ppreviews-plos-725668748/1522614/preview.jpg" width="400">
// MAGIC <img src="http://i.stack.imgur.com/K4Cg9.png" width="600">
// MAGIC 
// MAGIC Please refer to [De Lathauwer, L., De Moor, B. and Vandewalle, J. (2000)](http://www.sandia.gov/~tgkolda/tdw2004/ldl-94-31.pdf) for details in high order singular decomposition.
// MAGIC 
// MAGIC #### Disadvantage
// MAGIC >- The size of (n,d) increase to (n,n,d), which means very expensive in memory and computation performance. E.g, (100k,100k) double requires about 80G
// MAGIC >- Not suitble for large number of clusters
// MAGIC >- Difficult to do out of sample embedding.

// COMMAND ----------

// MAGIC %md
// MAGIC ### High Order Spectral Clustering Package
// MAGIC In this project, a high order spectral clustering package is developed as attached spectralclustering_01.jar. The package is implemented as:
// MAGIC >- Implement in Scala with Apache Spark
// MAGIC >- Tensor implemented as both distributed and non-distributed frame
// MAGIC >- The clustering method is K-means
// MAGIC >- The current implementation can handle continuous and discrete features but not catergorical features

// COMMAND ----------

// MAGIC %md
// MAGIC #### Sample code
// MAGIC >- The following is just some sample source code of the library "high order spectral clustering"
// MAGIC 
// MAGIC ```
// MAGIC package tensorSVD
// MAGIC 
// MAGIC import org.apache.spark.SparkContext
// MAGIC import org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix
// MAGIC import org.apache.spark.rdd.RDD
// MAGIC 
// MAGIC /**
// MAGIC   * Define RDD Tensor Class
// MAGIC   * @constructor create rdd tensor class which contains dimension, values in Rdd and others
// MAGIC   * @param dim1 first dimension
// MAGIC   * @param dim2 second dimension
// MAGIC   * @param dim3 third dimension, keep the smallest dimension as the third
// MAGIC   */
// MAGIC class RddTensor(dim1: Int, dim2: Int, dim3: Int){
// MAGIC   //tensor vale is defined as a RDD of Tuple3(dim3Index: Int, rowIndex: Int, rowValue: Array[Double])
// MAGIC   //Note: Keep dim3 as the smallest dimension
// MAGIC   //      Keep dim1 as the most important dimension such as along sample point
// MAGIC 
// MAGIC   //Second constructor
// MAGIC   def this(dims: Array[Int])=this(dims(0),dims(1),dims(2))
// MAGIC 
// MAGIC   val dim=Array(dim1,dim2,dim3)
// MAGIC   //class field
// MAGIC   //val dim=(dim1,dim2,dim3) //dimension of tensor
// MAGIC   var nonemptyDim3 = 0
// MAGIC   var values: RDD[(Int,Int,Array[Double])]=null
// MAGIC   var isSymmetric=true //if every matrix along dim3 A(:,:,i) is symetrix
// MAGIC 
// MAGIC 
// MAGIC   /**
// MAGIC     * The method add value into the rdd Tensor. The input must be dim1*dim2 values (as RddMatrix)
// MAGIC     * @param newRddmat the new value matrix to be added in
// MAGIC     * @param dim3Index the third dimension index
// MAGIC     */
// MAGIC 
// MAGIC   def addDim3(newRddmat: RddMatrix, dim3Index: Int) {
// MAGIC     if(dim3Index>=this.dim(2)){
// MAGIC       throw new IllegalArgumentException("arg 2 need be in range of 0 to dim3 of the tensor class")
// MAGIC     }
// MAGIC     if(newRddmat.cols!=this.dim(1) || newRddmat.rows!=this.dim(0)){
// MAGIC       throw new IllegalArgumentException("the adding matrix dimension is not match the tensor")
// MAGIC     }
// MAGIC     if(this.nonemptyDim3==0){
// MAGIC       this.values=newRddmat.values.map(v=>(dim3Index,v._1,v._2))
// MAGIC     }else{
// MAGIC       this.values=this.values.union(newRddmat.values.map(v=>(dim3Index,v._1,v._2)))
// MAGIC     }
// MAGIC     this.nonemptyDim3 +=1
// MAGIC   }
// MAGIC 
// MAGIC   /**
// MAGIC     * The method unfold the rdd tensor along the required order (0,1 or 2)
// MAGIC     * @param dimi the unfold direction/order (0,1 or 2)
// MAGIC     * @param sc the spark context
// MAGIC     * @return the result matrix (RddMatrix)
// MAGIC     */
// MAGIC   def unfoldTensor(dimi: Int, sc: SparkContext): RddMatrix = {
// MAGIC     val dims=this.dim
// MAGIC     var outmat:RddMatrix = null
// MAGIC 
// MAGIC     if(dimi==0){
// MAGIC       val rmat:RddMatrix = new RddMatrix(this.dim(dimi),0)
// MAGIC       rmat.setValues(this.values.map(v=>(v._2,(v._1,v._3))).groupByKey()
// MAGIC         .map(x=>(x._1,x._2.toArray.sortBy(_._1))).map(b=>(b._1,b._2.flatMap(bi=>bi._2))))
// MAGIC       outmat=rmat
// MAGIC     }
// MAGIC     if(dimi==1){
// MAGIC       val rmat:RddMatrix = new RddMatrix(this.dim(dimi),0)
// MAGIC       if(this.isSymmetric){
// MAGIC         val dd=2
// MAGIC         val temp1=Range(0,2).toArray
// MAGIC         val temp2=Range(0,3).toArray
// MAGIC         val indx: IndexedSeq[Int]=temp1.map(v=>(v,temp2)).flatMap(v=>v._2.map(_*dd+v._1))
// MAGIC         rmat.setValues(this.values.map(v=>(v._2,(v._1,v._3))).groupByKey()
// MAGIC           .map(x=>(x._1,x._2.toArray.sortBy(_._1)))
// MAGIC           .map(b=>(b._1,b._2.flatMap(bi=>bi._2.zipWithIndex)))
// MAGIC           .map(v=>(v._1,v._2.sortBy(_._2).map(vi=>vi._1))))
// MAGIC         outmat=rmat
// MAGIC       }else{
// MAGIC         throw new Exception("Folding for dim2 not apply to asymmetric tensor in dim1 by dim2")
// MAGIC       }
// MAGIC     }
// MAGIC     if(dimi==2){
// MAGIC       val rmat:RddMatrix = new RddMatrix(this.dim(0)*this.dim(1),this.dim(dimi))
// MAGIC       //Note: as dim(2) is small, this returns the transpose of the unfold matrix
// MAGIC       val cc=this.dim(1)
// MAGIC       rmat.setValues(this.values.flatMap(v=>v._3.zipWithIndex.map(vi=>(v._1,v._2,vi._2,vi._1)))
// MAGIC         .map(b=>((b._2,b._3),(b._1,b._4))).groupByKey()
// MAGIC         .map(x=>(x._1._1*cc+x._1._2,x._2.toArray.sortBy(_._1).map(b=>b._2))))
// MAGIC       outmat=rmat
// MAGIC 
// MAGIC     }
// MAGIC     outmat
// MAGIC   }
// MAGIC 
// MAGIC   /**
// MAGIC     * The method fold a matrix back to rdd tensor for along the required order/dim
// MAGIC     * @param rddMat the input rdd matrix to be folded
// MAGIC     * @param dimi the folding order
// MAGIC     * @return the folded rdd tensor
// MAGIC     */
// MAGIC   def foldTensorDimOne(rddMat:IndexedRowMatrix, dimi:Int) :RddTensor = {
// MAGIC     if(dimi!=1){
// MAGIC       throw new IllegalArgumentException("The fold method of rddTensor only available along the first dimension")
// MAGIC     }
// MAGIC     val ndim=this.dim.length
// MAGIC     val size=this.dim
// MAGIC     //val tempMat=rddMat.rows.map
// MAGIC     val result: RddTensor = null
// MAGIC     result
// MAGIC   }
// MAGIC 
// MAGIC }
// MAGIC 
// MAGIC ...
// MAGIC 
// MAGIC ```

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