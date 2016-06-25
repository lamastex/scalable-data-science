// Databricks notebook source exported at Sat, 25 Jun 2016 04:35:58 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by Dominic Lee
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC ***

// COMMAND ----------

// MAGIC %md 
// MAGIC #Random Matrices
// MAGIC 
// MAGIC This project explores how the theory of random matrices can be used in a practical way for the decomposition of matrices. In partcular, we focus on an important practical application:
// MAGIC 
// MAGIC * **Separating "signal" from "noise":** Obtain a low-rank representation of a matrix that retains most of the "signal" and filters out most of the "noise" in the original matrix.

// COMMAND ----------

// MAGIC %md
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC ## References
// MAGIC 
// MAGIC * [Alan Edelman and Yuyang Wang (2013). Random matrix theory and its innovative applications.](http://www-math.mit.edu/~edelman/publications/random_matrix_theory_innovative.pdf)  
// MAGIC **Abstract** Recently more and more disciplines of science and engineering have found Random Matrix Theory valuable. Some disciplines use the limiting densities to indicate the cutoff between “noise” and “signal.” Other disciplines are finding eigenvalue repulsions a compelling model of reality. This survey introduces both the theory behind these applications and MATLAB experiments allowing a reader immediate access to the ideas.  
// MAGIC 
// MAGIC * [Alan Edelman and N. Raj Rao (2005). Random matrix theory. Acta Numerica, pp. 1-65.](http://web.eecs.umich.edu/~rajnrao/Acta05rmt.pdf)  
// MAGIC **Abstract:** Random matrix theory is now a big subject with applications in many disciplines of science, engineering and finance. This article is a survey specifically oriented towards the needs and interests of a numerical analyst. This survey includes some original material not found anywhere else. We include the important mathematics which is a very modern development, as well as the computational software that is transforming the theory into useful practice.  
// MAGIC **Contents:**  
// MAGIC 1 Introduction  
// MAGIC 2 Linear systems  
// MAGIC 3 Matrix calculus  
// MAGIC 4 Classical random matrix ensembles  
// MAGIC 5 Numerical algorithms stochastically  
// MAGIC 6 Classical orthogonal polynomials  
// MAGIC 7 Multivariate orthogonal polynomials  
// MAGIC 8 Hypergeometric functions of matrix argument  
// MAGIC 9 Painlev´e equations  
// MAGIC 10 Eigenvalues of a billion by billion matrix  
// MAGIC 11 Stochastic operators  
// MAGIC 12 Free probability and infinite random matrices  
// MAGIC 13 A random matrix calculator  
// MAGIC 14 Non-Hermitian and structured random matrices  
// MAGIC 15 A segue
// MAGIC 
// MAGIC ### Applications of Random Matrix Theory
// MAGIC * [Mark Buchanan (2010). Enter the matrix: the deep law that shapes our reality. New Scientist Vol. 206 Issue 2755, p. 28-31.](https://www.newscientist.com/article/mg20627550-200-enter-the-matrix-the-deep-law-that-shapes-our-reality/)  
// MAGIC **Abstract:** The article discusses random matrix theory, a facet of physics research said to be growing rapidly. Once applied only in the study of physics, the article reports that the concept is finding application in numerous other areas including finance and materials science. Electrical engineer Raj Nadakuditi is cited regarding his observation that random matrix theory is deeply rooted in nature. The history of application of random matrix theory to studies of complex atomic nuclei is noted. Physicist Eugene Wigner is mentioned for his insights into the quantum nature of atomic nuclei.

// COMMAND ----------

// MAGIC %md
// MAGIC ***

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }
displayHTML("https://en.wikipedia.org/wiki/Random_matrix")

// COMMAND ----------

// MAGIC %md
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC # Separating "Signal" from "Noise"
// MAGIC ***
// MAGIC From the decomposition of a matrix as a sum of rank-one matrices, the goal is to truncate the sum appropriately so as to obtain a low-rank representation of the original matrix that retains the "signal" (or structure) and filters out the "noise".

// COMMAND ----------

// MAGIC %md
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC ## Wigner's Theorem
// MAGIC 
// MAGIC [Wigner's theorem](http://www.jstor.org/stable/1970079), also known as the *semi-circle law*, states that for a real symmetric \\(n\times n\\) random matrix, \\(X\_{n}\\), whose entries are independent random variables with mean 0, variance 1 and have bounded moments, the distribution of the eigenvalues of \\(\frac{1}{\sqrt{n}}X\_{n}\\) approaches a semi-circle density on the support [-2,2] as \\(n\to\infty\\).  
// MAGIC Denoting an eigenvalue of \\(\frac{1}{\sqrt{n}}X\_{n}\\) by \\(\lambda\\), the asymptotic semi-circle density is
// MAGIC $$ f(\lambda)=\frac{1}{2\pi}\sqrt{4-\lambda^{2}}. $$

// COMMAND ----------

// MAGIC %md
// MAGIC Let's import the needed libraries.

// COMMAND ----------

import breeze.linalg._
import breeze.linalg.operators._
import breeze.numerics._
import breeze.numerics.constants._
import breeze.stats._
import breeze.stats.distributions._
import breeze.stats.DescriptiveStats._
sqlContext

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC ### Gaussian Example

// COMMAND ----------

// Gaussian example
val gaussianMean = 0.0
val gaussianSD = 1.0
val n = 4
val x = new DenseMatrix(n,n,new Gaussian(gaussianMean,gaussianSD).sample(n*n).toArray) // nxn Gaussian random matrix

// COMMAND ----------

val y = upperTriangular(x) + upperTriangular(x).t - diag(diag(x)) // symmetric Gaussian random matrix

// COMMAND ----------

val yEigval = eigSym.justEigenvalues(y/sqrt(n)).toArray // eigenvalues of scaled symmetric Gaussian random matrix

// COMMAND ----------

val n = 1000
val nSquared = n * n
val nIndex = DenseVector.range(1,n+1,1).toArray // array of integers from 1 to n
val nSquaredIndex = DenseVector.range(1,nSquared+1,1).toArray // array of integers from 1 to nSquared

// COMMAND ----------

val s = new Gaussian(gaussianMean,gaussianSD).sample(nSquared).toArray
val sDF = sc.parallelize(nSquaredIndex zip s).toDF("index","sample") // create RDD and convert to dataframe
sDF.show

// COMMAND ----------

display(sDF.select("sample"))

// COMMAND ----------

val x = new DenseMatrix(n,n,s) // nxn Gaussian random matrix
val y = upperTriangular(x) + upperTriangular(x).t - diag(diag(x)) // symmetric Gaussian random matrix
val yEigval = eigSym.justEigenvalues(y/sqrt(n)).toArray // eigenvalues of scaled symmetric Gaussian random matrix

// COMMAND ----------

val yEigvalDF = sc.parallelize(nIndex zip yEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
yEigvalDF.show

// COMMAND ----------

display(yEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md
// MAGIC #### Gaussian example: Density histogram with semi-circle density.  
// MAGIC 
// MAGIC <img src ="https://cloud.githubusercontent.com/assets/19638932/15643528/03064b32-26a2-11e6-8761-b2da3b0b36f7.png" width="700">

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC ### Uniform Example

// COMMAND ----------

// Uniform example
val uniformMin = -sqrt(3)
val uniformMax = sqrt(3)
val uniformMean = Uniform(uniformMin,uniformMax).mean
val uniformVariance = Uniform(uniformMin,uniformMax).variance

// COMMAND ----------

val s = new Uniform(uniformMin,uniformMax).sample(nSquared).toArray
val sDF = sc.parallelize(nSquaredIndex zip s).toDF("index","sample") // create RDD and convert to dataframe
display(sDF.select("sample"))

// COMMAND ----------

val x = new DenseMatrix(n,n,s) // nxn uniform random matrix
val y = upperTriangular(x) + upperTriangular(x).t - diag(diag(x)) // symmetric uniform random matrix
val yEigval = eigSym.justEigenvalues(y/sqrt(n)).toArray // eigenvalues of scaled symmetric random matrix
val yEigvalDF = sc.parallelize(nIndex zip yEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(yEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC ### Laplacian (Double Exponential) Example

// COMMAND ----------

// Laplacian (double exponential) example
val exponentialRate = sqrt(2)
val a = new DenseMatrix(n,n,new Exponential(exponentialRate).sample(nSquared).toArray) // nxn exponential random matrix
implicit def bool2double(b:Boolean) = if (b) 1.0 else 0.0
val bernoulliProb = 0.5
val b = new DenseMatrix(n,n,new Bernoulli(bernoulliProb).sample(nSquared).map(b => 2 * bool2double(b) - 1).toArray) // nxn Bernoulli random matrix
val x = a :* b // nxn Laplacian random matrix
val s = x.toDenseVector.toArray
val sDF = sc.parallelize(nSquaredIndex zip s).toDF("index","sample") // create RDD and convert to dataframe
display(sDF.select("sample"))

// COMMAND ----------

val y = upperTriangular(x) + upperTriangular(x).t - diag(diag(x)) // symmetric Laplacian random matrix
val yEigval = eigSym.justEigenvalues(y/sqrt(n)).toArray // eigenvalues of scaled symmetric random matrix
val yEigvalDF = sc.parallelize(nIndex zip yEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(yEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC ### Does it work when the underlying density is non-symmetric?

// COMMAND ----------

// MAGIC %md
// MAGIC ### Exponential Example

// COMMAND ----------

// Exponential example
val exponentialRate = 0.5
val s = new Exponential(exponentialRate).sample(nSquared).toArray
val sDF = sc.parallelize(nSquaredIndex zip s).toDF("index","sample") // create RDD and convert to dataframe
display(sDF.select("sample"))

// COMMAND ----------

val x = new DenseMatrix(n,n,s) // nxn exponential random matrix
val y = upperTriangular(x) + upperTriangular(x).t - diag(diag(x)) // symmetric exponential random matrix
val z = (y - mean(y)) / sqrt(variance(y)) // symmetric random matrix standardized to have mean 0 and variance 1
val zEigval = eigSym.justEigenvalues(z/sqrt(n)).toArray // eigenvalues of scaled standardized symmetric random matrix
val zEigvalDF = sc.parallelize(nIndex zip zEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(zEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md
// MAGIC #### It still works!!! The underlying density can be non-symmetric.  
// MAGIC 
// MAGIC #### Exponential example: Density histogram with semi-circle density.  
// MAGIC 
// MAGIC <img src ="https://cloud.githubusercontent.com/assets/19638932/15643944/25fdb74a-26a4-11e6-9eba-81a1478db814.png" width="700">

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC ### Does it work when the underlying distribution is discrete?

// COMMAND ----------

// MAGIC %md
// MAGIC ### Poisson Example

// COMMAND ----------

// Poisson example
val poissonMean = 5
val t = new Poisson(poissonMean).sample(nSquared)
val s = t.map(ti => ti.toDouble).toArray
val sDF = sc.parallelize(nSquaredIndex zip s).toDF("index","sample") // create RDD and convert to dataframe
display(sDF.select("sample"))

// COMMAND ----------

val x = new DenseMatrix(n,n,s) // nxn Poisson random matrix
val y = upperTriangular(x) + upperTriangular(x).t - diag(diag(x)) // symmetric Poisson random matrix
val z = (y - mean(y)) / sqrt(variance(y)) // symmetric random matrix standardized to have mean 0 and variance 1
val zEigval = eigSym.justEigenvalues(z/sqrt(n)).toArray // eigenvalues of scaled standardized symmetric random matrix
val zEigvalDF = sc.parallelize(nIndex zip zEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(zEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md
// MAGIC #### It still works!!! The underlying distribution can be discrete.  

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC ### Does it still work when the matrix entries are independent but not identically distributed?

// COMMAND ----------

// MAGIC %md
// MAGIC ### Example: Gaussians with different means and variances

// COMMAND ----------

val s = new DenseMatrix(n,n,new Gaussian(gaussianMean,gaussianSD).sample(nSquared).toArray)
val t = new DenseMatrix(n,n,new Poisson(poissonMean).sample(nSquared).map(t => t.toDouble + 1.0).toArray)
val x = (s :* t) + t
val y = upperTriangular(x) + upperTriangular(x).t - diag(diag(x)) // symmetric Poisson random matrix
val z = (y - mean(y)) / sqrt(variance(y)) // symmetric random matrix standardized to have mean 0 and variance 1
val zEigval = eigSym.justEigenvalues(z/sqrt(n)).toArray // eigenvalues of scaled standardized symmetric random matrix
val zEigvalDF = sc.parallelize(nIndex zip zEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(zEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md
// MAGIC #### It still works!!! The independent matrix entries do not have to be identically distributed.

// COMMAND ----------

// MAGIC %md
// MAGIC ### What happens when the matrix contains non-random structure?

// COMMAND ----------

// MAGIC %md
// MAGIC ### Example: Exponential random matrix with strongly connected block

// COMMAND ----------

// Exponential example
val s = new Exponential(exponentialRate).sample(nSquared).toArray
val x = new DenseMatrix(n,n,s) // nxn exponential random matrix
var y = upperTriangular(x) + upperTriangular(x).t - diag(diag(x)) // symmetric exponential random matrix
y = y :/ max(y) // normalize largest entry to 1.0
y(0 to 99,0 to 99) := 0.7 // first 100 nodes form strongly connected block

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC <img src ="https://cloud.githubusercontent.com/assets/19638932/15701530/db167a5a-282e-11e6-9993-1fbdce834668.png" width="1000">

// COMMAND ----------

val z = (y - mean(y)) / sqrt(variance(y)) // symmetric random matrix standardized to have mean 0 and variance 1
val zEigval = eigSym.justEigenvalues(z/sqrt(n)).toArray // eigenvalues of scaled standardized symmetric random matrix
val zEigvalDF = sc.parallelize(nIndex zip zEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(zEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md
// MAGIC #### The eigenvalues do not have a semi-circle density.  
// MAGIC 
// MAGIC <img src ="https://cloud.githubusercontent.com/assets/19638932/15644472/6e556b70-26a7-11e6-9bf8-f480a37d046d.png" width="700">

// COMMAND ----------

// MAGIC %md
// MAGIC ### Separating "signal" from "noise"  
// MAGIC Let \\(Y\\) be a real symmetric \\(n\times n\\) matrix with eigenvalues, \\(\lambda\_{1},...,\lambda\_{n}\\), and corresponding eigenvectors, \\(u\_{1},...,u\_{n}\\). A decomposition of \\(Y\\) into a sum of rank-one matrices is given by
// MAGIC $$ Y = \displaystyle\sum\_{k=1}^{n} \lambda\_{k} u\_{k} u\_{k}^{T}. $$
// MAGIC 
// MAGIC \\(Y\\) can decomposed into "signal" plus "noise" by partitioning its component rank-one matrices into signal components and noise components:
// MAGIC $$ Y = Y\_{signal} + Y\_{noise} = \displaystyle\sum\_{i\in S} \lambda\_{i} u\_{i} u\_{i}^{T} + \displaystyle\sum\_{j\in N} \lambda\_{j} u\_{j} u\_{j}^{T}, $$
// MAGIC 
// MAGIC where \\(S\\) is an index set of signal components and \\(N\\) is an index set of noise components (\\(S\cup N =\\) {\\(1,...,n\\)}, \\(S\cap N=\emptyset \\)).

// COMMAND ----------

val yEig = eigSym(y)
val yEigval = yEig.eigenvalues
val yEigvec = yEig.eigenvectors
val yEigvalDF = sc.parallelize(nIndex zip yEigval.toArray).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(yEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md
// MAGIC #### \\(S\_{1}=\\) {1000} and \\(N\_{1}=\\) {1,...,999}

// COMMAND ----------

val yNoise = y - (yEigval(-1) * (yEigvec(::,-1) * yEigvec(::,-1).t))
val z = (yNoise - mean(yNoise)) / sqrt(variance(yNoise)) // symmetric random matrix standardized to have mean 0 and variance 1
val zEigval1 = eigSym.justEigenvalues(z/sqrt(n)) // eigenvalues of scaled standardized symmetric random matrix
val zEigval = zEigval1.toArray
val zEigvalDF = sc.parallelize(nIndex zip zEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(zEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md
// MAGIC #### Signal and noise not well-separated
// MAGIC <img src ="https://cloud.githubusercontent.com/assets/19638932/15703125/e067a660-2837-11e6-8735-189608200dbb.png" width="1000">

// COMMAND ----------

// MAGIC %md
// MAGIC #### \\(S\_{2}=\\) {999,1000} and \\(N\_{2}=\\) {1,...,998}

// COMMAND ----------

val yNoise = y - (yEigval(-1) * (yEigvec(::,-1) * yEigvec(::,-1).t)) - (yEigval(-2) * (yEigvec(::,-2) * yEigvec(::,-2).t))
val z = (yNoise - mean(yNoise)) / sqrt(variance(yNoise)) // symmetric random matrix standardized to have mean 0 and variance 1
val zEigval2 = eigSym.justEigenvalues(z/sqrt(n)) // eigenvalues of scaled standardized symmetric random matrix
val zEigval = zEigval2.toArray
val zEigvalDF = sc.parallelize(nIndex zip zEigval).toDF("index","eigenvalue") // create RDD and convert to dataframe
display(zEigvalDF.select("eigenvalue"))

// COMMAND ----------

// MAGIC %md
// MAGIC #### Signal and noise adequately separated
// MAGIC <img src ="https://cloud.githubusercontent.com/assets/19638932/15703140/f14c07c8-2837-11e6-8509-0f3d9e82a20a.png" width="1000">

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC #### Semi-Circle Cumulative Distribution Function

// COMMAND ----------

def semiCircleCDF(s:DenseVector[Double]):DenseVector[Double]={
  val n = s.length
  var cdf = (0.5 * (s :* sqrt(4.0 - (s :* s))) + 2.0 * (asin(0.5 * s) - asin(-1.0))) / (2.0 * Pi)
  for(i <- 0 to n-1){
    if(s(i) < -2.0)
     cdf(i) = 0.0
    if(s(i) > 2.0)
     cdf(i) = 1.0
  }
  cdf
}

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC #### Kolmogorov-Smirnov Test

// COMMAND ----------

def KolmogorovSmirnovTest(scdf:DenseVector[Double]):Double={
  val m = scdf.length
  val n = m.toDouble
  val indexInt = DenseVector.range(1,m+1,1)
  val indexDou = indexInt.map(i => i.toDouble)
  val index1 = indexDou / n
  val index2 = (indexDou - 1.0) / n
  val diff1 = max(abs(scdf - index1))
  val diff2 = max(abs(scdf - index2))
  val ksStat = max(diff1,diff2)
  val pValue = min(1.0,2.0 * exp(-2.0 * n * ksStat * ksStat))
  pValue
}

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC #### Test Noise 1

// COMMAND ----------

val zcdf1 = semiCircleCDF(zEigval1)
val pValue1 = KolmogorovSmirnovTest(zcdf1)

// COMMAND ----------

// MAGIC %md
// MAGIC #### p-value suggests that Noise 1 is not a random matrix.
// MAGIC 
// MAGIC <img src ="https://cloud.githubusercontent.com/assets/19638932/15709260/e8f5343a-2857-11e6-8fbd-86d23cc4f698.png" width="800">

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC #### Test Noise 2

// COMMAND ----------

val zcdf2 = semiCircleCDF(zEigval2)
val pValue2 = KolmogorovSmirnovTest(zcdf2)

// COMMAND ----------

// MAGIC %md
// MAGIC #### p-value suggests that Noise 2 appears to be a random matrix.
// MAGIC 
// MAGIC <img src ="https://cloud.githubusercontent.com/assets/19638932/15709273/f849f06a-2857-11e6-8665-0a7a9ca024ee.png" width="800">

// COMMAND ----------

// MAGIC %md

// COMMAND ----------

// MAGIC %md
// MAGIC ## Extension to Singular Values: Quarter-Circle Law
// MAGIC 
// MAGIC The *quarter-circle law* states that for a real \\(n\times n\\) random matrix, \\(X\_{n}\\), whose entries are independent random variables with mean 0, variance 1 and have bounded moments, the distribution of the singular values of \\(\frac{1}{\sqrt{n}}X\_{n}\\) approaches a quarter-circle density on the support [0,2] as \\(n\to\infty\\).  
// MAGIC Denoting an eigenvalue of \\(\frac{1}{\sqrt{n}}X\_{n}\\) by \\(\sigma\\), the asymptotic quarter-circle density is
// MAGIC $$f(\sigma)=\frac{1}{\pi}\sqrt{4-\sigma^{2}}.$$

// COMMAND ----------

// MAGIC %md
// MAGIC ## Extension to Rectangular Matrices: Marcenko-Pastur's Theorem
// MAGIC 
// MAGIC [Marcenko-Pastur's theorem](http://iopscience.iop.org/article/10.1070/SM1967v001n04ABEH001994/meta) states that for a real \\(m\times n\\) random matrix, \\(X\_{n}\\), with \\(m\geq n\\) satisfying \\(\frac{n}{m}\to r\leq 1\\) as \\(n\to\infty\\), and whose entries are independent random variables with mean 0, variance 1 and have bounded moments, the asymptotic density, as \\(n\to\infty\\), of the singular values of \\(\frac{1}{\sqrt{m}}X\_{n}\\) is
// MAGIC $$f(\sigma)=\frac{1}{\pi \sigma r}\sqrt{[\sigma^{2}-(1-\sqrt{r})^{2}][(1+\sqrt{r})^{2}-\sigma^{2}]},$$
// MAGIC on the interval, \\([1-\sqrt{r},1+\sqrt{r}]\\).  
// MAGIC Note that when \\(m=n\\), Marcenko-Pastur's theorem reduces to the quarter-circle law.

// COMMAND ----------

// MAGIC %md
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by Dominic Lee
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)