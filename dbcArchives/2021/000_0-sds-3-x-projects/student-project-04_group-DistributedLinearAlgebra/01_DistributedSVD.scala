// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Distributed Linear Algebra#
// MAGIC ** Authors: ** 
// MAGIC - Måns Williamson
// MAGIC - Jonatan Vallin
// MAGIC 
// MAGIC This project consists of two parts. In the first part we consider the theory and algorithms for distributed singular value decomposition of matrices, whereas in the second part we implement a music recommendation system closely related to low rank matrix factorization.
// MAGIC 
// MAGIC The video presentation for this project can be found <a href="https://vimeo.com/499834997"> here</a>.

// COMMAND ----------

// MAGIC %md ##Distributed singular value decomposition
// MAGIC 
// MAGIC This part of the project deals with distributed singular value decomposition.
// MAGIC The singular value decomposition of a real matrix \\(A\\) is given by
// MAGIC \\[A= U S V^T,\\]
// MAGIC 
// MAGIC where \\(S\\) is a diagonal matrix of size \\( n\times n\\) and  \\(U\\) (\\(m\times n \\) ) and \\(V\\) (\\(n\times n\\)) are real matrices such that \\( U^T U =I \\) and \\(V^T V=I\\).
// MAGIC (See for example <a href="https://en.wikipedia.org/wiki/Singular_value_decomposition">wikipedia </a>,  <a href="https://web.mit.edu/be.400/www/SVD/Singular_Value_Decomposition.htm">MIT </a> and  <a href="https://mathworld.wolfram.com/SingularValueDecomposition.html">WolframMathworld </a>). A standard way of computing this is to first compute the product \\(A^T A= VDV^T\\). The matrix S is then obtained by taking the square root of the diagonal of \\(D\\) and finally we obtain \\(U\\) by computing \\(U = AV S^{-1}\\).
// MAGIC 
// MAGIC When one has large matrices and wants to compute the SVD distributed one takes into account the structure of the matrix and choose an algorithm that takes advantage of this.
// MAGIC 
// MAGIC One particular case is when one wants to compute the singular value decomposition of a so called "tall and skinny" matrix \\(A\\). 
// MAGIC This means that the number of rows \\(m\\) is much larger than the number of columns \\(n\\). An example of where this is the case is the Audioscrobbler recommender system  used by <a href="https://en.wikipedia.org/wiki/Last.fm">Last.fm </a>. The typicall dataset will be a tall and skinny matrix where each row contains three entries; an identifier for a song, an identifier for a user and a player count (so each row tells us how many times a user has played a song).
// MAGIC 
// MAGIC We will look at an algorithm in spark for computing the SVD where one make use of the structure of the tall and skinny matrix \\(A\\). The algorithm has the following steps: 
// MAGIC 
// MAGIC 
// MAGIC  - It is computationally expensive to compute the product \\( A^T A\\) so we compute this distributed (map-reduce).
// MAGIC  
// MAGIC  -  \\( A^T A\\) is of size \\(n\times n \\) ( \\(n \\) is small) so we can compute \\(V\\) and \\(S\\) locally by computing the eigenvectors and -values of \\(A^TA\\).
// MAGIC 
// MAGIC  - We then compute \\(U= AVS^{-1}\\) as distributed matrix multiplication by broadcasting  \\(VS^{-1}\\) to each partition and compute the multiplication with the rows of \\(A\\).
// MAGIC 
// MAGIC In the spark mllib library theres a package for distributed linear algebra (<a href="https://spark.apache.org/docs/2.2.0/mllib-data-types.html">Data Types</a>) and an object that we will use is the IndexedRow-object . This takes two parameters; a vector and an index that indicates on which row of the matrix the index is located. We can then create an RDD in spark of IndexedRow-objects. Below we use the matrix 
// MAGIC 
// MAGIC $$A = 
// MAGIC \begin{pmatrix} 1 & 2 \\\ 3& 4 \\\ 0& 0\\\0&0 \end{pmatrix}
// MAGIC $$
// MAGIC to test the algorithm on.
// MAGIC We start by creating the matrix as an array of tuples. We then map each partittion (tuple) to a dense vector that we zip with its index (so we have a (vector,index)-tuple) that we use to create an IndexedRow. (It's worth mentioning that there is an implementation of <a href="https://spark.apache.org/docs/2.2.0/mllib-dimensionality-reduction.html">SVD</a> in Spark for  <a href="https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.RowMatrix">RowMatrices</a> - an RDD of rows of a matrix without indices).

// COMMAND ----------


//Import the necessary objects:
import org.apache.spark.mllib.linalg.distributed.IndexedRow
import org.apache.spark.mllib.linalg.Matrices



//Create the matrix A above as a dense matrix:
val Amatrix = Matrices.dense(4,2,Array(1,3,0,0,2,4,0,0))

//Zip each row of a with its index and map it to an indexed row object (x._2 is the index and x._1 the array).
//Once we have an IndexedRow r we can get the index and vector by calling r.index and r.vector 
val A = sc.parallelize(Amatrix.rowIter.toArray.zipWithIndex.map(x=>new IndexedRow(x._2,x._1)))

 A.take(2)



// COMMAND ----------

// MAGIC %md
// MAGIC The first part of the algorithm that is "expensive" is the computation of the product \\(A^T A\\) (we compute  \\(A^T A\\) rather than  \\(A A^T\\) since the former has the shape \\( n \times n \\) which we assume is small enough to fit on a local machine and \\(A A^T\\) is of size \\(m\times m\\) ) . We note that  
// MAGIC $$ \left( A^T A\right)_{j,k} = \sum_{i=1}^m   a_{ij}  a_{ik}. $$
// MAGIC 
// MAGIC This means that we can compute \\(A^T A\\) by mapping a row (say the i:th row)
// MAGIC $$a_i = \left(a_{i1}, \dots , a_{in}  \right) $$
// MAGIC 
// MAGIC to all the products of its elements. We thus create a function that takes an IndexedRow $$a_i $$ and maps it to key-value pairs $$ ((j,k), a_{ij} a_{ik}), 1\leq j \leq m, 1\leq k \leq n. $$ 
// MAGIC 
// MAGIC We then have an key-value RDD of ((Int,Int),Double)-tuples:

// COMMAND ----------

import scala.collection.mutable.ArrayBuffer


//Function that maps an indexed row (index,(a_1,...,a_n)) to   ((j,k),a_j*a_k), j=1,..,n and k=1,...,n
def f(v: IndexedRow): Array[((Int,Int),Double)]={
var keyvaluepairs = ArrayBuffer[((Int,Int),Double)]()
for(j<-0 to v.vector.size-1){
  for(k<-0 to v.vector.size-1){
  keyvaluepairs.append(((j,k),v.vector(j)*v.vector(k)))
  }
}
keyvaluepairs.toArray
}

//map M to key-value rdd where key =(j,k) and value = a_ij*a_ik.
//We use flatmap since we don't need to keep the row structure.
val keyvalRDD = A.flatMap(row =>f(row))

keyvalRDD.take(5)

// COMMAND ----------

// MAGIC %md  
// MAGIC We can now perform a reduceByKey-operation (join on \\( (j,k) \\) ) and then sum 
// MAGIC over$$ ((j,k), a_{ij} a_{ik}) $$ for all \\(i\\) to compute
// MAGIC 
// MAGIC $$ \left( A^T A\right)_{j,k} = \sum_{i=1}^m   a_{ij}  a_{ik}. $$
// MAGIC 
// MAGIC We then have a key-value RDD of ((Int,Int),Double)-tuples, where the value is an entry in the matrix \\(A\\) and the key indicates on what position in the matrix it is located:
// MAGIC 
// MAGIC $$\left( (j,k), \left( A^T A\right)_{j,k} \right), 1\leq j \leq m, 1\leq k \leq n.$$

// COMMAND ----------


//Sum up all key-value pairs that have the same key (j,k) (corresponts to getting the element of A.T*A on the j:th row and k:th column).
val keyvalSum = keyvalRDD.reduceByKey((x,y)=>x+y)

keyvalSum.take(2)


// COMMAND ----------

// MAGIC %md
// MAGIC We now make use of another object in the distributer linear algebra package in spark mllib;   <a href="https://spark.apache.org/docs/2.2.0/api/scala/index.html#org.apache.spark.mllib.linalg.distributed.MatrixEntry">MatrixEntry </a>. We map each key-value pair to a MatrixEntry-object (which has a row index, column index and a value).
// MAGIC With this we can create a   <a href="https://spark.apache.org/docs/2.2.0/mllib-data-types.html#coordinatematrix">CoordinateMatrix </a>. 
// MAGIC We can transform this to a  <a href="https://spark.apache.org/docs/2.2.0/mllib-data-types.html#rowmatrix">RowMatrix </a> that we finally collect.

// COMMAND ----------

import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import breeze.linalg.DenseMatrix

//map to matrix entries
val matrix = keyvalSum.map(el => MatrixEntry(el._1._1, el._1._2, el._2))   

//Create a CoordinateMatrix
val mat = new CoordinateMatrix(matrix)


//Transform to RowMatrix and collect.
val ATArowmatrix = mat.toRowMatrix().rows.collect()



// COMMAND ----------

// MAGIC %md
// MAGIC We now want to calculate the eigen values and eigen vectors of \\(A^T A\\) (locally) and in order to do this we transform it to a DenseMatrix (from the Breeze linear algebra package):

// COMMAND ----------

val m = mat.numRows()
val n = mat.numCols()

//Create an empty DenseMatrix (in which we will store the product A.T*A).
val ATA = DenseMatrix.zeros[Double](m.toInt,n.toInt)

//Each row will be a sparse vector. For each row we iterate over the non-zeros indices (foreachActive) and fill the i:th row of the ATA-matrix.
var i = 0
ATArowmatrix.foreach { vec =>
  vec.foreachActive { case (index, value) =>
    ATA(i, index) = value
  }
  i += 1
}

// COMMAND ----------

// MAGIC %md
// MAGIC We compute the eigenvalues and eigenvectors.
// MAGIC The matrix \\(S\\) in the SVD is obtained by computing the square root of the eigenvalues and inserting them in a diagonal matrix and the matrix \\(V\\) are the eigenvectors:

// COMMAND ----------


import scala.collection.mutable.ArrayBuffer
import breeze.linalg._, eigSym.EigSym

//lambda is a vector with the eigenvalues of A.T*A and evs the eigenvector matrix.
val EigSym(lambda, evs) = eigSym(ATA) 
//det(evs)

val w=lambda.map(x=>if(x >0) Math.sqrt(x) else 0) //square root of eigen values to compute the S matrix.
val S =diag(w)  
val V =evs



// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC In the last step we need to compute 
// MAGIC $$U = AVS^{-1}.$$
// MAGIC 
// MAGIC Since both \\(V\\) and \\(S^{-1}\\) are of size \\(n\times n\\) (and \\(n\\) is relatively small) we can compute the product \\(VS^{-1}\\) locally and then broadcast it to each partition of \\(A\\) (which is an RDD of IndexedRow).

// COMMAND ----------

//Compute the inverse of S.
val Sinv = S.map(x=>if(x==0) 0 else 1/x)   //invert the diagonal matrix.

//Compute the product of V and inverse of S.
val M = V*Sinv

//Broadcast to the spark context.
sc.broadcast(M)

// COMMAND ----------

// MAGIC %md 
// MAGIC We define a function that we can use to multiply an IndexedRow with a DenseMatrix on the left. We use this to map each row of \\(A\\) to its product with \\(VS^{-1}\\):

// COMMAND ----------

import org.apache.spark.mllib.linalg.distributed.{IndexedRowMatrix}

//Function that multiplies an indexedRow object with a DenseMatrix (from breeze.linalg.DenseMatrix) on the left and returns an Array.
def prod(u: IndexedRow, m: DenseMatrix[Double]): Array[Double]={
var w = ArrayBuffer[Double]()
for(i<-0 to m.cols-1){
  var x: Double =0
  for(j<-0 to m.rows-1){
      x=x+m(j,i)*u.vector(j)
  }
  w.append(x)
}
w.toArray
}


//COmpute the matrix product by multiplying each indexed row with the Matrix M (and then collect the result)
val Urows =A.map(row => prod(row,M)).collect() 

//Create a dense matrix U with the rows.
val U = DenseMatrix(Urows:_*)   



// COMMAND ----------

// MAGIC %md Finally we print the product \\(USV^T\\) and check that it corresponds to \\(A\\)

// COMMAND ----------

//Print the product USV.t to check that it equals A:
println(U*S*V.t)
