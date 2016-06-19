// Databricks notebook source exported at Sun, 19 Jun 2016 02:24:27 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/xtraResources/LinearAlgebra/LAlgCheatSheet.html) of this databricks HOMEWORK notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week5/09_LinearAlgebraIntro/017_LAlgIntro-Homework-db/xtraResources/LinearAlgebra/LAlgCheatSheet](http://img.youtube.com/vi/y6F-e6m1m2s/0.jpg)](https://www.youtube.com/v/y6F-e6m1m2s?rel=0&autoplay=1&modestbranding=1&start=1547&end=1673)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/week5/09_LinearAlgebraIntro/017_LAlgIntro.html) of the context/parent databricks notebook (for this databricks HOMEWORK notebook) and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/week5/09_LinearAlgebraIntro/017_LAlgIntro](http://img.youtube.com/vi/y6F-e6m1m2s/0.jpg)](https://www.youtube.com/v/y6F-e6m1m2s?rel=0&autoplay=1&modestbranding=1&start=0&end=2634)

// COMMAND ----------

// MAGIC %md
// MAGIC This is from 
// MAGIC * [https://github.com/scalanlp/breeze/wiki/Linear-Algebra-Cheat-Sheet](https://github.com/scalanlp/breeze/wiki/Linear-Algebra-Cheat-Sheet)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Core Concepts
// MAGIC 
// MAGIC Compared to other numerical computing environments, Breeze matrices
// MAGIC default to column major ordering, like Matlab, but indexing is 0-based,
// MAGIC like Numpy. Breeze has as its core concepts matrices and column vectors.
// MAGIC Row vectors are normally stored as matrices with a single row. This
// MAGIC allows for greater type safety with the downside that conversion of row
// MAGIC vectors to column vectors is performed using a transpose-slice
// MAGIC (`a.t(::,0)`) instead of a simple transpose (`a.t`).
// MAGIC 
// MAGIC [[UFunc|Universal Functions]]s are very important in Breeze. Once you get a feel for the syntax (i.e. what's in this section), it might be worthwhile to read the first half of the UFunc wiki page. (You can skip the last half that involves implementing your own UFuncs...until you're ready to contribute to Breeze!)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Quick Reference
// MAGIC 
// MAGIC The following table assumes that Numpy is used with `from numpy import *` and Breeze with:

// COMMAND ----------

import breeze.linalg._
import breeze.numerics._

// COMMAND ----------

// MAGIC %md
// MAGIC ### Creation
// MAGIC 
// MAGIC | Operation                       | Breeze                                        | Matlab            | Numpy           |R
// MAGIC | ------------------------------- | --------------------------------------------- | ----------------- | ----------------|-------------------
// MAGIC | Zeroed matrix                   | `DenseMatrix.zeros[Double](n,m)`              | `zeros(n,m)`      | `zeros((n,m))`  |`mat.or.vec(n, m)`
// MAGIC | Zeroed vector                   | `DenseVector.zeros[Double](n)`                | `zeros(n,1)`      | `zeros(n)`      |`mat.or.vec(n, 1)`
// MAGIC | Vector of ones                  | `DenseVector.ones[Double](n)`                 | `ones(n,1)`       | `ones(n)`       |`mat.or.vec(n, 1) + 1`
// MAGIC | Vector of particular number     | `DenseVector.fill(n){5.0}`                    | `ones(n,1) * 5`   | `ones(n) * 5`   |`(mat.or.vec(5, 1) + 1) * 5`
// MAGIC | range given stepsize            | `DenseVector.range(start,stop,step)` or `Vector.rangeD(start,stop,step)`          |                 ||`seq(start,stop,step)`
// MAGIC | n element range                 | `linspace(start,stop,numvals)`    | `linspace(0,20,15)`                |                             ||
// MAGIC | Identity matrix                 | `DenseMatrix.eye[Double](n)`                  | `eye(n)`          | `eye(n)`        |`identity(n)`
// MAGIC | Diagonal matrix                 | `diag(DenseVector(1.0,2.0,3.0))`              | `diag([1 2 3])`   | `diag((1,2,3))` |`diag(c(1,2,3))`
// MAGIC | Matrix inline creation          | `DenseMatrix((1.0,2.0), (3.0,4.0))`           | `[1 2; 3 4]`      | `array([ [1,2], [3,4] ])` |`matrix(c(1,2,3,4), nrow = 2, ncol = 2)`
// MAGIC | Column vector inline creation   | `DenseVector(1,2,3,4)`                        | `[1 2 3 4]`       | `array([1,2,3,4])`|`c(1,2,3,4)`
// MAGIC | Row vector inline creation      | `DenseVector(1,2,3,4).t`                      | `[1 2 3 4]'`      | `array([1,2,3]).reshape(-1,1)` |`t(c(1,2,3,4))`
// MAGIC | Vector from function            | `DenseVector.tabulate(3){i => 2*i}`           |                   |                                ||
// MAGIC | Matrix from function            | `DenseMatrix.tabulate(3, 2){case (i, j) => i+j}` |                |                                ||
// MAGIC | Vector creation from array      | `new DenseVector(Array(1, 2, 3, 4))`          |                   |                                ||
// MAGIC | Matrix creation from array      | `new DenseMatrix(2, 3, Array(11, 12, 13, 21, 22, 23))` |          |                                ||
// MAGIC | Vector of random elements from 0 to 1 | `DenseVector.rand(4)`                   |                   |                                |`runif(4)` (requires stats library)
// MAGIC | Matrix of random elements from 0 to 1 | `DenseMatrix.rand(2, 3)`                |                   |                                |`matrix(runif(6),2)` (requires stats library)

// COMMAND ----------

DenseMatrix.zeros[Double](2,3)

// COMMAND ----------

// MAGIC %py
// MAGIC import numpy as np

// COMMAND ----------

// MAGIC %py
// MAGIC np.zeros((2,3))

// COMMAND ----------

// MAGIC %r
// MAGIC mat.or.vec(2,3)

// COMMAND ----------

// MAGIC %md
// MAGIC #### Reading and writing Matrices
// MAGIC 
// MAGIC Currently, Breeze supports IO for Matrices in two ways: Java serialization and csv. The latter comes from two functions: `breeze.linalg.csvread` and `breeze.linalg.csvwrite`. `csvread` takes a File, and optionally parameters for how the CSV file is delimited (e.g. if it is actually a tsv file, you can set tabs as the field delimiter.) and returns a [DenseMatrix](Data-Structures#densematrix). Similarly, `csvwrite` takes a File and a DenseMatrix, and writes the contents of a matrix to a file.
// MAGIC 
// MAGIC ### Indexing and Slicing
// MAGIC 
// MAGIC |Operation                 |Breeze                                           |Matlab       |Numpy        |R
// MAGIC |--------------------------|-------------------------------------------------|-------------|-------------|-----------
// MAGIC |Basic Indexing            |`a(0,1)`                                         |`a(1,2)`     |`a[0,1]`     |`a[1,2]`
// MAGIC |Extract subset of vector  |`a(1 to 4)` or `a(1 until 5)` or `a.slice(1,5)`  |`a(2:5)`     |`a[1:5]`     |`a[2:5]`
// MAGIC |(negative steps)          |`a(5 to 0 by -1)`                                |`a(6:-1:1)`  |`a[5:0:-1]`  |
// MAGIC |(tail)                    |`a(1 to -1)`                                     |`a(2:end)`   |`a[1:]`      |`a[2:length(a)]` or ` tail(a,n=length(a)-1)`
// MAGIC |(last element)            |`a( -1 )`                                        |`a(end)`     |`a[-1]`      |`tail(a, n=1)`
// MAGIC |Extract column of matrix  |`a(::, 2)`                                       |`a(:,3)`     |`a[:,2]`     |`a[,2]`

// COMMAND ----------

val matrix = DenseMatrix.rand(2, 3)

// COMMAND ----------

val two_one = matrix(1, 0) // Remember the index starts from zero

// COMMAND ----------

// MAGIC %md
// MAGIC ### Other Manipulation
// MAGIC 
// MAGIC |Operation                       |Breeze                                   |Matlab              |Numpy                          |R
// MAGIC |--------------------------------|-----------------------------------------|--------------------|-------------------------------|-------------
// MAGIC |Reshaping                       |`a.reshape(3, 2)`                        |`reshape(a, 3, 2)`  |`a.reshape(3,2)`               |`matrix(a,nrow=3,byrow=T)`
// MAGIC |Flatten matrix                  |`a.toDenseVector` (Makes copy)           |`a(:)`              |`a.flatten()`                  |`as.vector(a)`
// MAGIC |Copy lower triangle             |`lowerTriangular(a)`                     |`tril(a)`           |`tril(a)`                      |`a[upper.tri(a)] <- 0`
// MAGIC |Copy upper triangle             |`upperTriangular(a)`                     |`triu(a)`           |`triu(a)`                      |`a[lower.tri(a)] <- 0`
// MAGIC |Copy (note, no parens!!)        |`a.copy`                                 |                    |`np.copy(a)`                   |
// MAGIC |Create view of matrix diagonal  |`diag(a)`                                |NA                  |`diagonal(a)` (Numpy >= 1.9)   |
// MAGIC |Vector Assignment to subset     |`a(1 to 4) := 5.0`                       |`a(2:5) = 5`        |`a[1:4] = 5`                   |`a[2:5] = 5`
// MAGIC |Vector Assignment to subset     |`a(1 to 4) := DenseVector(1.0,2.0,3.0)`  |`a(2:5) = [1 2 3]`  |`a[1:4] = array([1,2,3])`      |`a[2:5] = c(1,2,3)`
// MAGIC |Matrix Assignment to subset     |`a(1 to 3,1 to 3) := 5.0`                |`a(2:4,2:4) = 5`    |`a[1:3,1:3] = 5`               |`a[2:4,2:4] = 5`
// MAGIC |Matrix Assignment to column     |`a(::, 2) := 5.0`                        |`a(:,3) = 5`        |`a[:,2] = 5`                   |`a[,3] = 5`
// MAGIC |Matrix vertical concatenate     |`DenseMatrix.vertcat(a,b)`               |`[a ; b]`           |`vstack((a,b))`                |`rbind(a, b)`
// MAGIC |Matrix horizontal concatenate   |`DenseMatrix.horzcat(d,e)`               |`[d , e]`           |`hstack((d,e))`                |`cbind(d, e)`
// MAGIC |Vector concatenate              |`DenseVector.vertcat(a,b)`               |`[a b]`             |`concatenate((a,b))`           |`c(a, b)`
// MAGIC 
// MAGIC ### Operations
// MAGIC 
// MAGIC |Operation                           |Breeze       |Matlab                                                 |Numpy        |R
// MAGIC |------------------------------------|-------------|-------------------------------------------------------|-------------|---------------------
// MAGIC |Elementwise addition                |`a + b`      |`a + b`                                                |`a + b`      |`a + b`
// MAGIC |Shaped/Matrix multiplication        |`a * b`      |`a * b`                                                |`dot(a, b)`  |`a %*% b`
// MAGIC |Elementwise multiplication          |`a :* b`     |`a .* b`                                               |`a * b`      |`a * b`
// MAGIC |Elementwise division                |`a :/ b`     |`a ./ b`                                               |`a / b`      |`a / b`
// MAGIC |Elementwise comparison              |`a :< b`     |`a < b` (gives matrix of 1/0 instead of true/false)    |`a < b`      |`a < b`
// MAGIC |Elementwise equals                  |`a :== b`    |`a == b` (gives matrix of 1/0 instead of true/false)   |`a == b`     |`a == b`
// MAGIC |Inplace addition                    |`a :+= 1.0`  |`a += 1`                                               |`a += 1`     |`a = a + 1`
// MAGIC |Inplace elementwise multiplication  |`a :*= 2.0`  |`a *= 2`                                               |`a *= 2`     |`a = a * 2`
// MAGIC |Vector dot product                  |`a dot b`, `a.t * b`<sup>†</sup>    |`dot(a,b)`                      |`dot(a,b)`   |`crossprod(a,b)`
// MAGIC |Elementwise max                     |`max(a)`     |`max(a)`                                               |`a.max()`    |`max(a)`
// MAGIC |Elementwise argmax                  |`argmax(a)`  |`[v i] = max(a); i`                                    |`a.argmax()` |`which.max(a)`
// MAGIC 
// MAGIC 
// MAGIC ### Sum
// MAGIC 
// MAGIC |Operation                                     |Breeze                                |Matlab          |Numpy         |R     
// MAGIC |----------------------------------------------|--------------------------------------|----------------|--------------|----------
// MAGIC |Elementwise sum                               |`sum(a)`                              |`sum(sum(a))`   |`a.sum()`     |`sum(a)`
// MAGIC |Sum down each column (giving a row vector)    |`sum(a, Axis._0)` or `sum(a(::, *))`  |`sum(a)`        |`sum(a,0)`    |`apply(a,2,sum)`
// MAGIC |Sum across each row (giving a column vector)  |`sum(a, Axis._1)` or `sum(a(*, ::))`  |`sum(a')`       |`sum(a,1)`    |`apply(a,1,sum)`
// MAGIC |Trace (sum of diagonal elements)              |`trace(a)`                            |`trace(a)`      |`a.trace()`   |`sum(diag(a))`
// MAGIC |Cumulative sum                                |`accumulate(a)`                       |`cumsum(a)`     |`a.cumsum()`  |`apply(a,2,cumsum)`
// MAGIC 
// MAGIC ### Boolean Operators
// MAGIC 
// MAGIC |Operation                                     |Breeze                                |Matlab            |Numpy       |R
// MAGIC |----------------------------------------------|--------------------------------------|------------------|------------|--------
// MAGIC |Elementwise and                               |`a :& b`                              |`a && b`          |`a & b`     |`a & b`
// MAGIC |Elementwise or                                |`a :| b`                              |`a || b`          |`a | b`     |`a | b`
// MAGIC |Elementwise not                               |`!a`                                  |`~a`              |`~a`        |`!a` 
// MAGIC |True if any element is nonzero                |`any(a)`                              |`any(a)`          |any(a)      |
// MAGIC |True if all elements are nonzero              |`all(a)`                              |`all(a)`          |all(a)      |
// MAGIC 
// MAGIC ### Linear Algebra Functions
// MAGIC 
// MAGIC |Operation                                     |Breeze                                                       |Matlab            |Numpy               |R
// MAGIC |----------------------------------------------|-------------------------------------------------------------|------------------|--------------------|-----------------
// MAGIC |Linear solve                                  |`a \ b`                                                      |`a \ b`           |`linalg.solve(a,b)` |`solve(a,b)`
// MAGIC |Transpose                                     |`a.t`                                                        |`a'`              |`a.conj.transpose()`|`t(a)`
// MAGIC |Determinant                                   |`det(a)`                                                     |`det(a)`          |`linalg.det(a)`     |`det(a)`
// MAGIC |Inverse                                       |`inv(a)`                                                     |`inv(a)`          |`linalg.inv(a)`     |`solve(a)`
// MAGIC |Moore-Penrose Pseudoinverse                   |`pinv(a)`                                                    |`pinv(a)`         |`linalg.pinv(a)`    |
// MAGIC |Vector Frobenius Norm                         |`norm(a)`                                                    |`norm(a)`         |`norm(a)`           |
// MAGIC |Eigenvalues (Symmetric)                       |`eigSym(a)`                                                  |`[v,l] = eig(a)`  |`linalg.eig(a)[0]`  |
// MAGIC |Eigenvalues                                   |`val (er, ei, _) = eig(a)` (separate real & imaginary part)  |`eig(a)`          |`linalg.eig(a)[0]`  |`eigen(a)$values`
// MAGIC |Eigenvectors                                  |`eig(a)._3`                                                  |`[v,l] = eig(a)`  |`linalg.eig(a)[1]`  |`eigen(a)$vectors`
// MAGIC |Singular Value Decomposition                  |`val svd.SVD(u,s,v) = svd(a)`                                |`svd(a)`          |`linalg.svd(a)`     |`svd(a)$d`
// MAGIC |Rank                                          |`rank(a)`                                                    |`rank(a)`         |`rank(a)`           |`rank(a)`
// MAGIC |Vector length                                 |`a.length`                                                   |`size(a)`         |`a.size`            |`length(a)`
// MAGIC |Matrix rows                                   |`a.rows`                                                     |`size(a,1)`       |`a.shape[0]`        |`nrow(a)`
// MAGIC |Matrix columns                                |`a.cols`                                                     |`size(a,2)`       |`a.shape[1]`        |`ncol(a)`
// MAGIC 
// MAGIC ### Rounding and Signs
// MAGIC 
// MAGIC |Operation                           |Breeze       |Matlab          |Numpy        |R
// MAGIC |------------------------------------|-------------|----------------|-------------|--------------
// MAGIC |Round                               |`round(a)`   |`round(a)`      |`around(a)`  |`round(a)`
// MAGIC |Ceiling                             |`ceil(a)`    |`ceil(a)`       |`ceil(a)`    |`ceiling(a)`
// MAGIC |Floor                               |`floor(a)`   |`floor(a)`      |`floor(a)`   |`floor(a)`
// MAGIC |Sign                                |`signum(a)`  |`sign(a)`       |`sign(a)`    |`sign(a)`
// MAGIC |Absolute Value                      |`abs(a)`     |`abs(a)`        |`abs(a)`     |`abs(a)`
// MAGIC 
// MAGIC ### Constants
// MAGIC 
// MAGIC |Operation     |Breeze          |Matlab    |Numpy     |R
// MAGIC |--------------|----------------|----------|----------|-----------
// MAGIC |Not a Number  |`NaN` or `nan`  |`NaN`     |`nan`     |`NA`
// MAGIC |Infinity      |`Inf` or `inf`  |`Inf`     |`inf`     |`Inf`
// MAGIC |Pi            |`Constants.Pi`  |`pi`      |`math.pi` |`pi`
// MAGIC |e             |`Constants.E`   |`exp(1)`  |`math.e`  |`exp(1)`
// MAGIC 
// MAGIC 
// MAGIC ## Complex numbers
// MAGIC 
// MAGIC If you make use of complex numbers, you will want to include a
// MAGIC `breeze.math._` import. This declares a `i` variable, and provides
// MAGIC implicit conversions from Scala’s basic types to complex types.
// MAGIC 
// MAGIC |Operation          |Breeze                       |Matlab     |Numpy                        |R
// MAGIC |-------------------|-----------------------------|-----------|-----------------------------|------------
// MAGIC |Imaginary unit     |`i`                          |`i`        |`z = 1j`                     |`1i`
// MAGIC |Complex numbers    |`3 + 4 * i` or `Complex(3,4)`|`3 + 4i`   |`z = 3 + 4j`                 |`3 + 4i`
// MAGIC |Absolute Value     |`abs(z)` or `z.abs`          |`abs(z)`   |`abs(z)`                     |`abs(z)`
// MAGIC |Real Component     |`z.real`                     |`real(z)`  |`z.real`                     |`Re(z)`
// MAGIC |Imaginary Component|`z.imag`                     |`imag(z)`  |`z.imag()`                   |`Im(z)`
// MAGIC |Imaginary Conjugate|`z.conjugate`                |`conj(z)`  |`z.conj()` or `z.conjugate()`|`Conj(z)`
// MAGIC 
// MAGIC ## Numeric functions
// MAGIC 
// MAGIC Breeze contains a fairly comprehensive set of special functions under
// MAGIC the `breeze.numerics._` import. These functions can be applied to single
// MAGIC elements, vectors or matrices of Doubles. This includes versions of the
// MAGIC special functions from `scala.math` that can be applied to vectors and
// MAGIC matrices. Any function acting on a basic numeric type can “vectorized”,
// MAGIC to a [[UFunc|Universal Functions]] function, which can act elementwise on vectors and matrices:
// MAGIC ```scala
// MAGIC val v = DenseVector(1.0,2.0,3.0)
// MAGIC exp(v) // == DenseVector(2.7182818284590455, 7.38905609893065, 20.085536923187668)
// MAGIC ```
// MAGIC 
// MAGIC UFuncs can also be used in-place on Vectors and Matrices:
// MAGIC ```scala
// MAGIC val v = DenseVector(1.0,2.0,3.0)
// MAGIC exp.inPlace(v) // == DenseVector(2.7182818284590455, 7.38905609893065, 20.085536923187668)
// MAGIC ```
// MAGIC 
// MAGIC See [[Universal Functions]] for more information.
// MAGIC 
// MAGIC Here is a (non-exhaustive) list of UFuncs in Breeze:
// MAGIC 
// MAGIC ### Trigonometry
// MAGIC * `sin`, `sinh`, `asin`, `asinh`
// MAGIC * `cos`, `cosh`, `acos`, `acosh`
// MAGIC * `tan`, `tanh`, `atan`, `atanh`
// MAGIC * `atan2`
// MAGIC * `sinc(x) == sin(x)/x`
// MAGIC * `sincpi(x) == sinc(x * Pi)`
// MAGIC 
// MAGIC ### Logarithm, Roots, and Exponentials
// MAGIC * `log`, `exp` `log10` 
// MAGIC * `log1p`, `expm1`
// MAGIC * `sqrt`, `sbrt`
// MAGIC * `pow`
// MAGIC 
// MAGIC ### Gamma Function and its cousins
// MAGIC 
// MAGIC The [gamma function](http://en.wikipedia.org/wiki/Gamma_function) is the extension of the factorial function to the reals.
// MAGIC Numpy needs `from scipy.special import *` for this and subsequent sections.
// MAGIC 
// MAGIC |Operation                           |Breeze              |Matlab                  |Numpy                   |R
// MAGIC |------------------------------------|--------------------|------------------------|------------------------|----------------
// MAGIC |Gamma function                      |`exp(lgamma(a))`    |`gamma(a)`              |`gamma(a)`              |`gamma(a)`  
// MAGIC |log Gamma function                  |`lgamma(a)`         |`gammaln(a)`            |`gammaln(a)`            |`lgamma(a)`
// MAGIC |Incomplete gamma function           |`gammp(a, x)`       |`gammainc(a, x)`        |`gammainc(a, x)`        |`pgamma(a, x)` (requires stats library)
// MAGIC |Upper incomplete gamma function     |`gammq(a, x)`       |`gammainc(a, x, tail)`  |`gammaincc(a, x)`       |`pgamma(x, a, lower = FALSE) * gamma(a)` (requires stats library)
// MAGIC |derivative of lgamma                |`digamma(a)`        |`psi(a)`                |`polygamma(0, a)`       |`digamma(a)`
// MAGIC |derivative of digamma               |`trigamma(a)`       |`psi(1, a)`             |`polygamma(1, a)`       |`trigama(a)`
// MAGIC |nth derivative of digamma           | na                 |`psi(n, a)`             |`polygamma(n, a)`       |`psigamma(a, deriv = n)`
// MAGIC |Log [Beta function](http://en.wikipedia.org/wiki/Beta_function)| lbeta(a,b)  |`betaln(a, b)` |`betaln(a,b)`|`lbeta(a, b)`
// MAGIC |Generalized Log [Beta function](http://en.wikipedia.org/wiki/Beta_function)| lbeta(a)  | na|na             |     
// MAGIC 
// MAGIC ### Error Function
// MAGIC 
// MAGIC The [error function](http://en.wikipedia.org/wiki/Error_function)...
// MAGIC 
// MAGIC |Operation                           |Breeze           |Matlab          |Numpy                |R
// MAGIC |------------------------------------|-----------------|----------------|---------------------|-------------
// MAGIC | error function                     |`erf(a)`         |`erf(a)`        |`erf(a)`             |`2 * pnorm(a * sqrt(2)) - 1`
// MAGIC | 1 - erf(a)                         |`erfc(a)`        |`erfc(a)`       |`erfc(a)`            |`2 * pnorm(a * sqrt(2), lower = FALSE)`
// MAGIC | inverse error function             |`erfinv(a)`      |`erfinv(a)`     |`erfinv(a)`          |`qnorm((1 + a) / 2) / sqrt(2)`
// MAGIC | inverse erfc                       |`erfcinv(a)`     |`erfcinv(a)`    |`erfcinv(a)`         |`qnorm(a / 2, lower = FALSE) / sqrt(2)`
// MAGIC 
// MAGIC ### Other functions
// MAGIC 
// MAGIC |Operation                           |Breeze           |Matlab          |Numpy                |R
// MAGIC |------------------------------------|-----------------|----------------|---------------------|------------
// MAGIC | logistic sigmoid                   |`sigmoid(a)`     | na             | `expit(a)`          |`sigmoid(a)` (requires pracma library) 
// MAGIC | Indicator function                 |`I(a)`           | not needed     | `where(cond, 1, 0)` |`0 + (a > 0)`
// MAGIC | Polynominal evaluation             |`polyval(coef,x)`|                |                     | 
// MAGIC 
// MAGIC ### Map and Reduce
// MAGIC 
// MAGIC For most simple mapping tasks, one can simply use vectorized, or universal functions. 
// MAGIC Given a vector `v`, we can simply take the log of each element of a vector with `log(v)`.
// MAGIC Sometimes, however, we want to apply a somewhat idiosyncratic function to each element of a vector.
// MAGIC For this, we can use the map function:
// MAGIC 
// MAGIC ```scala
// MAGIC val v = DenseVector(1.0,2.0,3.0)
// MAGIC v.map( xi => foobar(xi) )
// MAGIC ``` 
// MAGIC 
// MAGIC Breeze provides a number of built in reduction functions such as sum, mean.
// MAGIC You can implement a custom reduction using the higher order function `reduce`.
// MAGIC For instance, we can sum the first 9 integers as follows:
// MAGIC 
// MAGIC ```scala
// MAGIC val v = linspace(0,9,10)
// MAGIC val s = v.reduce( _ + _ )
// MAGIC ```
// MAGIC 
// MAGIC ## Broadcasting
// MAGIC 
// MAGIC Sometimes we want to apply an operation to every row or column of a
// MAGIC matrix, as a unit. For instance, you might want to compute the mean of
// MAGIC each row, or add a vector to every column. Adapting a matrix so that
// MAGIC operations can be applied columnwise or rowwise is called
// MAGIC **broadcasting**. Languages like R and numpy automatically and
// MAGIC implicitly do broadcasting, meaning they won’t stop you if you
// MAGIC accidentally add a matrix and a vector. In Breeze, you have to signal
// MAGIC your intent using the broadcasting operator `*`. The `*` is meant to
// MAGIC evoke “foreach” visually. Here are some examples:
// MAGIC 
// MAGIC ```scala
// MAGIC     val dm = DenseMatrix((1.0,2.0,3.0),
// MAGIC                          (4.0,5.0,6.0))
// MAGIC 
// MAGIC     val res = dm(::, *) + DenseVector(3.0, 4.0)
// MAGIC     assert(res === DenseMatrix((4.0, 5.0, 6.0), (8.0, 9.0, 10.0)))
// MAGIC 
// MAGIC     res(::, *) := DenseVector(3.0, 4.0)
// MAGIC     assert(res === DenseMatrix((3.0, 3.0, 3.0), (4.0, 4.0, 4.0)))
// MAGIC 
// MAGIC     val m = DenseMatrix((1.0, 3.0), (4.0, 4.0))
// MAGIC     // unbroadcasted sums all elements
// MAGIC     assert(sum(m) === 12.0)
// MAGIC     assert(mean(m) === 3.0)
// MAGIC 
// MAGIC     assert(sum(m(*, ::)) === DenseVector(4.0, 8.0))
// MAGIC     assert(sum(m(::, *)) === DenseMatrix((5.0, 7.0)))
// MAGIC 
// MAGIC     assert(mean(m(*, ::)) === DenseVector(2.0, 4.0))
// MAGIC     assert(mean(m(::, *)) === DenseMatrix((2.5, 3.5)))
// MAGIC 
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC The UFunc trait is similar to numpy’s ufunc.  See [[Universal Functions]] for more information on Breeze UFuncs.
// MAGIC 
// MAGIC ## Casting and type safety
// MAGIC 
// MAGIC Compared to Numpy and Matlab, Breeze requires you to be more explicit about the types of your variables. When you create a new vector for example, you must specify a type (such as in `DenseVector.zeros[Double](n)`) in cases where a type can not be inferred automatically. Automatic inference will occur when you create a vector by passing its initial values in (`DenseVector`). A common mistake is using integers for initialisation (e.g. `DenseVector`), which would give a matrix of integers instead of doubles. Both Numpy and Matlab would default to doubles instead.
// MAGIC 
// MAGIC Breeze will not convert integers to doubles for you in most expressions. Simple operations like `a :+
// MAGIC 3` when `a` is a `DenseVector[Double]` will not compile. Breeze provides a convert function, which can be used to explicitly cast. You can also use `v.mapValues(_.toDouble)`.
// MAGIC 
// MAGIC ### Casting
// MAGIC 
// MAGIC | Operation                       | Breeze                                        | Matlab              | Numpy           | R
// MAGIC | ------------------------------- | --------------------------------------------- | ------------------- | ----------------|--------------------
// MAGIC | Convert to Int                  | `convert(a, Int)`                             | `int(a)`            | `a.astype(int)` |`as.integer(a)`
// MAGIC 
// MAGIC ## Performance
// MAGIC 
// MAGIC Breeze uses [netlib-java](https://github.com/fommil/netlib-java/) for
// MAGIC its core linear algebra routines. This includes all the cubic time
// MAGIC operations, matrix-matrix and matrix-vector multiplication. Special
// MAGIC efforts are taken to ensure that arrays are not copied.
// MAGIC 
// MAGIC Netlib-java will attempt to load system optimised BLAS/LAPACK if they
// MAGIC are installed, falling back to the reference natives, falling back to
// MAGIC pure Java. Set your logger settings to `ALL` for the
// MAGIC `com.github.fommil.netlib` package to check the status, and to
// MAGIC `com.github.fommil.jniloader` for a more detailed breakdown. Read the
// MAGIC netlib-java project page for more details.
// MAGIC 
// MAGIC Currently vectors and matrices over types other than `Double`, `Float`
// MAGIC and `Int` are boxed, so they will typically be a lot slower. If you find
// MAGIC yourself needing other AnyVal types like `Long` or `Short`, please ask
// MAGIC on the list about possibly adding support for them.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)