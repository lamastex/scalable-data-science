// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/uj6UNCt2eRc/0.jpg)](https://www.youtube.com/embed/uj6UNCt2eRc?start=0&end=1380&autoplay=1) [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/1NICbbECaC0/0.jpg)](https://www.youtube.com/embed/1NICbbECaC0?start=0&end=872&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Linear Algebra Review / re-Introduction
// MAGIC ### by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC 
// MAGIC This is a `breeze`'y `scala`rific break-down of:
// MAGIC 
// MAGIC * Ameet's Linear Algebra Review in CS190.1x Scalable Machine Learning Course that is archived in edX from 2015 and
// MAGIC * **Home Work:** read this! [https://github.com/scalanlp/breeze/wiki/Quickstart](https://github.com/scalanlp/breeze/wiki/Quickstart)
// MAGIC 
// MAGIC Using the above resources we'll provide a review of basic linear algebra concepts that will recur throughout the course.  These concepts include:
// MAGIC 
// MAGIC 1. Matrices
// MAGIC * Vectors
// MAGIC * Arithmetic operations with vectors and matrices
// MAGIC 
// MAGIC We will see the accompanying Scala computations in the local or non-distributed setting.

// COMMAND ----------

// MAGIC %md
// MAGIC Let's get a quick visual geometric interpretation for vectors, matrices and matrix-vector multiplications, eigen systems with real and comples Eigen values **NOW** from the following interactive visual-cognitive aid at:
// MAGIC 
// MAGIC  * [http://setosa.io/ev/eigenvectors-and-eigenvalues/](http://setosa.io/ev/eigenvectors-and-eigenvalues/) just focus on geometric interpretation of vectors and matrices in Cartesian coordinates.
// MAGIC  

// COMMAND ----------

// MAGIC %md
// MAGIC #### Matrix by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch now 0-61 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

// MAGIC %md
// MAGIC Breeze is a linear algebra package in Scala. First, let us import it as follows:

// COMMAND ----------

import breeze.linalg._

// COMMAND ----------

// MAGIC %md
// MAGIC #### 1. Matrix: creation and element-access
// MAGIC 
// MAGIC A **matrix** is a two-dimensional array.
// MAGIC 
// MAGIC Let us denote matrices via bold uppercase letters as follows:
// MAGIC 
// MAGIC For instance, the matrix below is denoted with \\(\mathbf{A}\\), a capital bold A.
// MAGIC 
// MAGIC $$
// MAGIC \mathbf{A} = \begin{pmatrix}
// MAGIC a\_{11} & a\_{12} & a\_{13} \\\\
// MAGIC a\_{21} & a\_{22} & a\_{23} \\\\
// MAGIC a\_{31} & a\_{32} & a\_{33}
// MAGIC \end{pmatrix}
// MAGIC $$
// MAGIC 
// MAGIC We usually put commas between the row and column indexing sub-scripts, to make the possibly multi-digit indices distinguishable as follows:
// MAGIC 
// MAGIC $$
// MAGIC \mathbf{A} = \begin{pmatrix}
// MAGIC a\_{1,1} & a\_{1,2} & a\_{1,3} \\\\
// MAGIC a\_{2,1} & a\_{2,2} & a\_{2,3} \\\\
// MAGIC a\_{3,1} & a\_{3,2} & a\_{3,3}
// MAGIC \end{pmatrix}
// MAGIC $$
// MAGIC 
// MAGIC * \\( \mathbf{A}\_{i,j} \\) denotes the entry in \\(i\\)-th row and \\(j\\)-th column of the matrix \\(\mathbf{A}\\).
// MAGIC * So for instance, 
// MAGIC   * the first entry, the top left entry, is denoted by \\( \mathbf{A}\_{1,1} \\).
// MAGIC   * And the entry in the third row and second column is denoted by \\( \mathbf{A}\_{3,2} \\).
// MAGIC   * We say that a matrix with n rows and m columns is an \\(n\\) by \\(m\\) matrix and written as \\(n \times m \\)
// MAGIC     * The matrix \\(\mathbf{A}\\) shown above is a generic \\(3 \times 3\\) (pronounced 3-by-3) matrix.
// MAGIC     * And the matrix in Ameet's example in the video above, having 4 rows and 3 columns, is a 4 by 3 matrix.
// MAGIC   * If a matrix \\(\mathbf{A}\\) is \\(n \times m \\), we write:
// MAGIC     * \\(\mathbf{A} \in \mathbb{R}^{n \times m}\\) and say that \\(\mathbf{A}\\) is an \\(\mathbb{R}\\) to the power of  the n times m, 
// MAGIC         * where, \\(\mathbb{R}\\) here denotes the set of all real numbers in the line given by the open interval: \\( (-\infty,+\infty)\\).

// COMMAND ----------

// MAGIC %md
// MAGIC Let us created a matrix `A` as a `val` (that is immutable) in scala. The matrix we want to create is mathematically notated as follows:
// MAGIC 
// MAGIC $$
// MAGIC \mathbf{A} = \begin{pmatrix}
// MAGIC a\_{1,1} & a\_{1,2} & a\_{1,3} \\\\
// MAGIC a\_{2,1} & a\_{2,2} & a\_{2,3} 
// MAGIC \end{pmatrix}
// MAGIC  = 
// MAGIC \begin{pmatrix}
// MAGIC 1 & 2 & 3 \\\\
// MAGIC 4 & 5 & 6 
// MAGIC \end{pmatrix}
// MAGIC $$

// COMMAND ----------

val A = DenseMatrix((1, 2, 3), (4, 5, 6)) // let's create this 2 by 3 matrix

// COMMAND ----------

A.size

// COMMAND ----------

A.rows // number of rows

// COMMAND ----------

A.size / A.rows // num of columns

// COMMAND ----------

A.cols // also say

// COMMAND ----------

// MAGIC %md
// MAGIC Now, let's access the element \\(a_{1,1}\\), i.e., the element from the first row and first column of \\(\mathbf{A}\\), which in our `val A` matrix is the integer of type `Int` equalling `1`.

// COMMAND ----------

A(0, 0) // Remember elements are indexed by zero in scala

// COMMAND ----------

// MAGIC %md
// MAGIC **Gotcha:** indices in breeze matrices start at 0 as in numpy of python and not at 1 as in MATLAB!

// COMMAND ----------

// MAGIC %md
// MAGIC Of course if you assign the same dense matrix to a mutable `var` `B` then its entries can be modified as follows:

// COMMAND ----------

var B = DenseMatrix((1, 2, 3), (4, 5, 6))

// COMMAND ----------

B(0,0)=999; B(1,1)=969; B(0,2)=666
B

// COMMAND ----------

// MAGIC 
// MAGIC %md
// MAGIC #### Vector
// MAGIC **(watch now 0:31 = 62-93 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC * A vector is a matrix with many rows and one column.
// MAGIC * We'll denote a vector by bold lowercase letters:
// MAGIC   $$\mathbf{a} = \begin{pmatrix} 3.3 \\\\ 1.0 \\\\ 6.3 \\\\ 3.6 \end{pmatrix}$$
// MAGIC    
// MAGIC    So, the vector above is denoted by \\(\mathbf{a}\\), the lowercase, bold a.
// MAGIC * \\(a_i\\) denotes the i-th entry of a vector. So for instance:
// MAGIC   * \\(a_2\\) denotes the second entry of the vector and it is 1.0 for our vector.
// MAGIC * If a vector is m-dimensional, then we say that \\(\mathbf{a}\\) is in \\(\mathbb{R}^m\\) and write \\(\mathbf{a}  \in \  \mathbb{R}^m\\).
// MAGIC     * So our \\(\mathbf{a} \in \ \mathbb{R}^4\\).

// COMMAND ----------

val a = DenseVector(3.3, 1.0, 6.3, 3.6) // these are row vectors

// COMMAND ----------

a.size // a is a column vector of size 4

// COMMAND ----------

a(1) // the second element of a is indexed by 1 as the first element is indexed by 0

// COMMAND ----------

val a = DenseVector[Double](5, 4, -1) // this makes a vector of Doubles from input Int

// COMMAND ----------

val a = DenseVector(5.0, 4.0, -1.0) // this makes a vector of Doubles from type inference . NOTE "5.0" is needed not just "5."

// COMMAND ----------

val x = DenseVector.zeros[Double](5) // this will output x: breeze.linalg.DenseVector[Double] = DenseVector(0.0, 0.0, 0.0, 0.0, 0.0)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC #### Transpose
// MAGIC 
// MAGIC **(watch now 1:02 = 95-157 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

// MAGIC %md
// MAGIC Suggested Home Work for the linear-algebraically rusty (LiAlRusty): watch again and take notes.

// COMMAND ----------

val A = DenseMatrix((1, 4), (6, 1), (3, 5)) // let's create this 2 by 3 matrix

// COMMAND ----------

A.t // transpose of A

// COMMAND ----------

val a = DenseVector(3.0, 4.0, 1.0)

// COMMAND ----------

a.t

// COMMAND ----------

// MAGIC %md
// MAGIC #### Addition and Subtraction
// MAGIC 
// MAGIC **(watch now 0:59 = 157-216 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

// MAGIC %md
// MAGIC Suggested Home Work for LiAlRusty: watch again and take notes.
// MAGIC 
// MAGIC  **Pop Quiz:**
// MAGIC  
// MAGIC  * what is a natural geometric interpretation of vector addition, subtraction, matric addition and subtraction?

// COMMAND ----------

val A = DenseMatrix((1, 4), (6, 1), (3, 5)) 

// COMMAND ----------

val B = -A 

// COMMAND ----------

A + B // should be A-A=0

// COMMAND ----------

A - B // should be A+A=2A

// COMMAND ----------

B - A // should be -A-A=-2A

// COMMAND ----------

// MAGIC %md
// MAGIC ### Operators
// MAGIC 
// MAGIC All Tensors support a set of operators, similar to those used in Matlab or Numpy. 
// MAGIC 
// MAGIC For **HOMEWORK** see: `Workspace -> scalable-data-science -> xtraResources -> LinearAlgebra -> LAlgCheatSheet` for a list of most of the operators and various operations. 
// MAGIC 
// MAGIC Some of the basic ones are reproduced here, to give you an idea.
// MAGIC 
// MAGIC | Operation | Breeze | Matlab | Numpy |
// MAGIC |---|---|---|---|
// MAGIC | Elementwise addition | ``a + b`` | ``a + b`` | ``a + b`` |
// MAGIC | Elementwise multiplication | ``a :* b`` | ``a .* b`` | ``a * b`` |
// MAGIC | Elementwise comparison | ``a :< b`` | ``a < b`` (gives matrix of 1/0 instead of true/false)| ``a < b`` |
// MAGIC | Inplace addition | ``a :+= 1.0`` | ``a += 1`` | ``a += 1`` |
// MAGIC | Inplace elementwise multiplication | ``a :*= 2.0`` | ``a *= 2`` | ``a *= 2`` |
// MAGIC | Vector dot product | ``a dot b``,``a.t * b``<sup>†</sup> | ``dot(a,b)`` | ``dot(a,b)`` |
// MAGIC | Elementwise sum | ``sum(a)``| ``sum(sum(a))`` | ``a.sum()`` |
// MAGIC | Elementwise max | ``a.max``| ``max(a)`` | ``a.max()`` |
// MAGIC | Elementwise argmax | ``argmax(a)``| ``argmax(a)`` | ``a.argmax()`` |
// MAGIC | Ceiling | ``ceil(a)``| ``ceil(a)`` | ``ceil(a)`` |
// MAGIC | Floor | ``floor(a)``| ``floor(a)`` | ``floor(a)`` |

// COMMAND ----------

// MAGIC %md
// MAGIC #### Scalar multiplication, Dot Product and Matrix-Vector multiplication
// MAGIC **(watch now 2:26 = 218-377 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

// MAGIC %md
// MAGIC Suggested Home Work for LiAlRusty: watch again and take notes.
// MAGIC 
// MAGIC  **Pop Quiz:**
// MAGIC  
// MAGIC  * what is a natural geometric interpretation of scalar multiplication of a vector or a matrix and what about vector matrix multiplication?
// MAGIC  
// MAGIC  Let's get a quick visual geometric interpretation for vectors, matrices and matrix-vector multiplications from the first interactive visual-cognitive aid at:
// MAGIC  
// MAGIC   * [http://setosa.io/ev/eigenvectors-and-eigenvalues/](http://setosa.io/ev/eigenvectors-and-eigenvalues/) 

// COMMAND ----------

val A = DenseMatrix((1, 4), (6, 1), (3, 5)) 

// COMMAND ----------

5 * A

// COMMAND ----------

A * 5

// COMMAND ----------

// MAGIC %md
// MAGIC #### Dot product

// COMMAND ----------

val A = DenseMatrix((1, 4), (6, 1), (3, 5)) 

// COMMAND ----------

val B = DenseMatrix((3, 1), (2, 2), (1, 3)) 

// COMMAND ----------

A *:* B // element-wise multiplication

// COMMAND ----------

// MAGIC %md
// MAGIC #### Matrix Vector multiplication

// COMMAND ----------

val A = DenseMatrix((1, 4), (3, 1)) 

// COMMAND ----------

val a = DenseVector(1, -1) // is a column vector

// COMMAND ----------

a.size // a is a column vector of size 2

// COMMAND ----------

A * a

// COMMAND ----------

// MAGIC %md
// MAGIC #### Matrix-Matrix multiplication
// MAGIC **(watch now 1:59 = 380-499 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

val A = DenseMatrix((1,2,3),(1,1,1))

// COMMAND ----------

val B = DenseMatrix((4, 1), (9, 2), (8, 9))

// COMMAND ----------

A*B // 4+18+14

// COMMAND ----------

1*4 + 2*9 + 3*8 // checking first entry of A*B

// COMMAND ----------

// MAGIC %md
// MAGIC #### Identity Matrix
// MAGIC **(watch now 0:53 = 530-583 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

val A = DenseMatrix((1,2,3),(4,5,6))

// COMMAND ----------

DenseMatrix.eye[Int](3)

// COMMAND ----------

A * DenseMatrix.eye[Int](3)

// COMMAND ----------

DenseMatrix.eye[Int](2) * A

// COMMAND ----------

// MAGIC %md
// MAGIC #### Inverse
// MAGIC **(watch now 0:52 = 584-636 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

val D = DenseMatrix((2.0, 3.0), (4.0, 5.0))
val Dinv = inv(D)

// COMMAND ----------

D * Dinv

// COMMAND ----------

Dinv * D

// COMMAND ----------

// MAGIC %md
// MAGIC #### Eucledian distance / norm
// MAGIC **(watch now 0:52 = 638-682 seconds)**:
// MAGIC 
// MAGIC [![Linear Algebra Review by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/mnS0lJncJzw/0.jpg)](https://www.youtube.com/watch?v=mnS0lJncJzw)

// COMMAND ----------

val b = DenseVector(4, 3)
norm(b)

// COMMAND ----------

Math.sqrt(4*4 + 3*3) // check

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **HOMEWORK:** read this! [https://github.com/scalanlp/breeze/wiki/Quickstart](https://github.com/scalanlp/breeze/wiki/Quickstart)
// MAGIC 
// MAGIC  It is here in markdown'd via wget and pandoc for your convenience.
// MAGIC 
// MAGIC #### Scala / nlp / breeze / Quickstart
// MAGIC 
// MAGIC David Hall edited this page on 24 Dec 2015
// MAGIC 
// MAGIC   
// MAGIC Breeze is modeled on Scala, and so if you're familiar with it, you'll be familiar with Breeze. First, import the linear algebra package:
// MAGIC 
// MAGIC ```
// MAGIC scala> import breeze.linalg._
// MAGIC ```
// MAGIC 
// MAGIC Let's create a vector:
// MAGIC ```
// MAGIC scala> val x = DenseVector.zeros[Double](5)
// MAGIC x: breeze.linalg.DenseVector[Double] = DenseVector(0.0, 0.0, 0.0, 0.0, 0.0)
// MAGIC ```
// MAGIC   
// MAGIC Here we make a column vector of zeros of type Double.  And there are other ways we could create the vector - such as with a literal `DenseVector(1,2,3)` or with a call to `fill` or `tabulate`.  The vector is "dense" because it is backed by an `Array[Double]`, but could as well have created a `SparseVector.zeros[Double](5)`, which would not allocate memory for zeros. 
// MAGIC 
// MAGIC  **Unlike Scalala, all Vectors are column vectors.** Row vectors are represented as `Transpose[Vector[T]]`.
// MAGIC 
// MAGIC The vector object supports accessing and updating data elements by their index in `0` to `x.length-1`. Like Numpy, negative indices are supported, with the semantics that for an index `i < 0` we operate on the i-th element from the end (`x(i) == x(x.length + i)`).
// MAGIC 
// MAGIC ```
// MAGIC scala> x(0)
// MAGIC Double = 0.0
// MAGIC 
// MAGIC scala> x(1) = 2
// MAGIC 
// MAGIC scala> x
// MAGIC breeze.linalg.DenseVector[Double] = DenseVector(0.0, 2.0, 0.0, 0.0, 0.0)
// MAGIC ```
// MAGIC 
// MAGIC Breeze also supports slicing. **Note that slices using a Range are much, much faster than those with an arbitrary sequence.**
// MAGIC 
// MAGIC ```
// MAGIC scala> x(3 to 4) := .5
// MAGIC breeze.linalg.DenseVector[Double] = DenseVector(0.5, 0.5)
// MAGIC 
// MAGIC scala> x
// MAGIC breeze.linalg.DenseVector[Double] = DenseVector(0.0, 2.0, 0.0, 0.5, 0.5)
// MAGIC ```
// MAGIC 
// MAGIC The slice operator constructs a read-through and write-through view of the given elements in the underlying vector.  You set its values using the vectorized-set operator `:=`.  You could as well have set it to a compatibly sized Vector.
// MAGIC 
// MAGIC ```
// MAGIC scala> x(0 to 1) := DenseVector(.1,.2)
// MAGIC 
// MAGIC scala> x
// MAGIC breeze.linalg.DenseVector[Double] = DenseVector(0.1, 0.2, 0.0, 0.5, 0.5)
// MAGIC ```
// MAGIC 
// MAGIC Similarly, a DenseMatrix can be created with a constructor method call, and its elements can be accessed and updated.
// MAGIC 
// MAGIC ```
// MAGIC scala> val m = DenseMatrix.zeros[Int](5,5)
// MAGIC m: breeze.linalg.DenseMatrix[Int] = 
// MAGIC 0  0  0  0  0  
// MAGIC 0  0  0  0  0  
// MAGIC 0  0  0  0  0  
// MAGIC 0  0  0  0  0  
// MAGIC 0  0  0  0  0  
// MAGIC ```
// MAGIC 
// MAGIC The columns of `m` can be accessed as DenseVectors, and the rows as DenseMatrices.
// MAGIC 
// MAGIC ```
// MAGIC scala> (m.rows, m.cols)
// MAGIC (Int, Int) = (5,5)
// MAGIC 
// MAGIC scala> m(::,1)
// MAGIC breeze.linalg.DenseVector[Int] = DenseVector(0, 0, 0, 0, 0)
// MAGIC 
// MAGIC scala>  m(4,::) := DenseVector(1,2,3,4,5).t  // transpose to match row shape
// MAGIC breeze.linalg.DenseMatrix[Int] = 1  2  3  4  5 
// MAGIC 
// MAGIC scala> m
// MAGIC breeze.linalg.DenseMatrix[Int] = 
// MAGIC 0  0  0  0  0  
// MAGIC 0  0  0  0  0  
// MAGIC 0  0  0  0  0  
// MAGIC 0  0  0  0  0  
// MAGIC 1  2  3  4  5   
// MAGIC ```
// MAGIC 
// MAGIC Assignments with incompatible cardinality or a larger numeric type won't compile.  
// MAGIC 
// MAGIC ```
// MAGIC scala> m := x
// MAGIC <console>:13: error: could not find implicit value for parameter op: breeze.linalg.operators.BinaryUpdateOp[breeze.linalg.DenseMatrix[Int],breeze.linalg.DenseVector[Double],breeze.linalg.operators.OpSet]
// MAGIC               m := x
// MAGIC                 ^
// MAGIC ```
// MAGIC 
// MAGIC Assignments with incompatible size will throw an exception:
// MAGIC 
// MAGIC ```
// MAGIC scala> m := DenseMatrix.zeros[Int](3,3)
// MAGIC java.lang.IllegalArgumentException: requirement failed: Matrices must have same number of row
// MAGIC ```
// MAGIC 
// MAGIC Sub-matrices can be sliced and updated, and literal matrices can be specified using a simple tuple-based syntax. **Unlike Scalala, only range slices are supported, and only the columns (or rows for a transposed matrix) can have a Range step size different from 1.**
// MAGIC 
// MAGIC ```
// MAGIC scala> m(0 to 1, 0 to 1) := DenseMatrix((3,1),(-1,-2)) 
// MAGIC breeze.linalg.DenseMatrix[Int] = 
// MAGIC 3   1   
// MAGIC -1  -2  
// MAGIC 
// MAGIC scala> m
// MAGIC breeze.linalg.DenseMatrix[Int] = 
// MAGIC 3   1   0  0  0  
// MAGIC -1  -2  0  0  0  
// MAGIC 0   0   0  0  0  
// MAGIC 0   0   0  0  0  
// MAGIC 1   2   3  4  5  
// MAGIC ```
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC ### Broadcasting
// MAGIC 
// MAGIC Sometimes we want to apply an operation to every row or column of a matrix, as a unit. For instance, you might want to compute the mean of each row, or add a vector to every column. Adapting a matrix so that operations can be applied column-wise or row-wise is called **broadcasting**. Languages like R and numpy automatically and implicitly do broadcasting, meaning they won't stop you if you accidentally add a matrix and a vector. In Breeze, you have to signal your intent using the broadcasting operator `*`. The `*` is meant to evoke "foreach" visually. Here are some examples:
// MAGIC 
// MAGIC ```
// MAGIC scala> import breeze.stats.mean
// MAGIC 
// MAGIC scala> val dm = DenseMatrix((1.0,2.0,3.0),
// MAGIC                             (4.0,5.0,6.0))
// MAGIC 
// MAGIC scala> val res = dm(::, *) + DenseVector(3.0, 4.0)
// MAGIC breeze.linalg.DenseMatrix[Double] =
// MAGIC 4.0  5.0  6.0
// MAGIC 8.0  9.0  10.0
// MAGIC 
// MAGIC scala> res(::, *) := DenseVector(3.0, 4.0)
// MAGIC 
// MAGIC scala> res
// MAGIC breeze.linalg.DenseMatrix[Double] =
// MAGIC 3.0  3.0  3.0
// MAGIC 4.0  4.0  4.0
// MAGIC 
// MAGIC scala> mean(dm(*, ::))
// MAGIC breeze.linalg.DenseVector[Double] = DenseVector(2.0, 5.0)
// MAGIC ```
// MAGIC 
// MAGIC ### breeze.stats.distributions
// MAGIC 
// MAGIC Breeze also provides a fairly large number of probability distributions. These come with access to probability density function for either discrete or continuous distributions. Many distributions also have methods for giving the mean and the variance.
// MAGIC 
// MAGIC ```
// MAGIC scala> import breeze.stats.distributions._
// MAGIC 
// MAGIC scala> val poi = new Poisson(3.0);
// MAGIC poi: breeze.stats.distributions.Poisson = <function1>
// MAGIC 
// MAGIC scala> val s = poi.sample(5);
// MAGIC s: IndexedSeq[Int] = Vector(5, 4, 5, 7, 4)
// MAGIC 
// MAGIC scala> s map { poi.probabilityOf(_) }
// MAGIC IndexedSeq[Double] = Vector(0.10081881344492458, 0.16803135574154085, 0.10081881344492458, 0.02160403145248382, 0.16803135574154085)
// MAGIC 
// MAGIC scala> val doublePoi = for(x <- poi) yield x.toDouble // meanAndVariance requires doubles, but Poisson samples over Ints
// MAGIC doublePoi: breeze.stats.distributions.Rand[Double] = breeze.stats.distributions.Rand$$anon$11@1b52e04
// MAGIC 
// MAGIC scala> breeze.stats.meanAndVariance(doublePoi.samples.take(1000));
// MAGIC breeze.stats.MeanAndVariance = MeanAndVariance(2.9960000000000067,2.9669509509509533,1000)
// MAGIC 
// MAGIC scala> (poi.mean,poi.variance)
// MAGIC (Double, Double) = (3.0,3.0)
// MAGIC ```
// MAGIC 
// MAGIC NOTE: Below, there is a possibility of confusion for the term `rate` in the family of exponential distributions. Breeze parameterizes the distribution with the mean, but refers to it as the rate.
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC scala> val expo = new Exponential(0.5);
// MAGIC expo: breeze.stats.distributions.Exponential = Exponential(0.5)
// MAGIC 
// MAGIC scala> expo.rate
// MAGIC Double = 0.5
// MAGIC ```
// MAGIC 
// MAGIC A characteristic of exponential distributions is its half-life, but we can compute the probability a value falls between any two numbers.
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC scala> expo.probability(0, log(2) * expo.rate)
// MAGIC Double = 0.5
// MAGIC 
// MAGIC scala> expo.probability(0.0, 1.5)
// MAGIC Double = 0.950212931632136
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC This means that approximately 95% of the draws from an exponential distribution fall between 0 and thrice the mean. We could have easily computed this with the cumulative distribution as well
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC scala> 1 - exp(-3.0)
// MAGIC Double = 0.950212931632136
// MAGIC ```
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC scala> val samples = expo.sample(2).sorted;
// MAGIC samples: IndexedSeq[Double] = Vector(1.1891135726280517, 2.325607782657507)
// MAGIC 
// MAGIC scala> expo.probability(samples(0), samples(1));
// MAGIC Double = 0.08316481553047272
// MAGIC 
// MAGIC scala> breeze.stats.meanAndVariance(expo.samples.take(10000));
// MAGIC breeze.stats.MeanAndVariance = MeanAndVariance(2.029351863973081,4.163267835527843,10000)
// MAGIC 
// MAGIC scala> (1 / expo.rate, 1 / (expo.rate * expo.rate))
// MAGIC (Double, Double) = (2.0,4.0)
// MAGIC ```
// MAGIC 
// MAGIC ### breeze.optimize
// MAGIC 
// MAGIC TODO: document breeze.optimize.minimize, recommend that instead.
// MAGIC 
// MAGIC Breeze's optimization package includes several convex optimization routines and a simple linear program solver. Convex optimization routines typically take a
// MAGIC `DiffFunction[T]`, which is a `Function1` extended to have a `gradientAt` method, which returns the gradient at a particular point. Most routines will require
// MAGIC a `breeze.linalg`-enabled type: something like a `Vector` or a `Counter`.
// MAGIC 
// MAGIC Here's a simple `DiffFunction`: a parabola along each vector's coordinate.
// MAGIC 
// MAGIC ```
// MAGIC 
// MAGIC scala> import breeze.optimize._
// MAGIC 
// MAGIC scala>  val f = new DiffFunction[DenseVector[Double]] {
// MAGIC      |               def calculate(x: DenseVector[Double]) = {
// MAGIC      |                 (norm((x - 3d) :^ 2d,1d),(x * 2d) - 6d);
// MAGIC      |               }
// MAGIC      |             }
// MAGIC f: java.lang.Object with breeze.optimize.DiffFunction[breeze.linalg.DenseVector[Double]] = $anon$1@617746b2
// MAGIC ```
// MAGIC 
// MAGIC Note that this function takes its minimum when all values are 3. (It's just a parabola along each coordinate.)
// MAGIC 
// MAGIC ```
// MAGIC scala> f.valueAt(DenseVector(3,3,3))
// MAGIC Double = 0.0
// MAGIC 
// MAGIC scala> f.gradientAt(DenseVector(3,0,1))
// MAGIC breeze.linalg.DenseVector[Double] = DenseVector(0.0, -6.0, -4.0)
// MAGIC 
// MAGIC scala>  f.calculate(DenseVector(0,0))
// MAGIC (Double, breeze.linalg.DenseVector[Double]) = (18.0,DenseVector(-6.0, -6.0))
// MAGIC ```
// MAGIC 
// MAGIC You can also use approximate derivatives, if your function is easy enough to compute:
// MAGIC 
// MAGIC ```
// MAGIC scala> def g(x: DenseVector[Double]) = (x - 3.0):^ 2.0 sum
// MAGIC 
// MAGIC scala> g(DenseVector(0.,0.,0.))
// MAGIC Double = 27.0
// MAGIC 
// MAGIC scala> val diffg = new ApproximateGradientFunction(g)
// MAGIC 
// MAGIC scala> diffg.gradientAt(DenseVector(3,0,1))
// MAGIC breeze.linalg.DenseVector[Double] = DenseVector(1.000000082740371E-5, -5.999990000127297, -3.999990000025377)
// MAGIC ```
// MAGIC 
// MAGIC Ok, now let's optimize `f`. The easiest routine to use is just `LBFGS`, which is a quasi-Newton method that works well for most problems.
// MAGIC 
// MAGIC ```
// MAGIC scala> val lbfgs = new LBFGS[DenseVector[Double]](maxIter=100, m=3) // m is the memory. anywhere between 3 and 7 is fine. The larger m, the more memory is needed.
// MAGIC 
// MAGIC scala> val optimum = lbfgs.minimize(f,DenseVector(0,0,0))
// MAGIC optimum: breeze.linalg.DenseVector[Double] = DenseVector(2.9999999999999973, 2.9999999999999973, 2.9999999999999973)
// MAGIC 
// MAGIC scala> f(optimum)
// MAGIC Double = 2.129924444096732E-29
// MAGIC ```
// MAGIC 
// MAGIC That's pretty close to 0! You can also use a configurable optimizer, using `FirstOrderMinimizer.OptParams`. It takes several parameters:
// MAGIC 
// MAGIC ```
// MAGIC case class OptParams(batchSize:Int = 512,
// MAGIC                      regularization: Double = 1.0,
// MAGIC                      alpha: Double = 0.5,
// MAGIC                      maxIterations:Int = -1,
// MAGIC                      useL1: Boolean = false,
// MAGIC                      tolerance:Double = 1E-4,
// MAGIC                      useStochastic: Boolean= false) {
// MAGIC   // ...
// MAGIC }
// MAGIC ```
// MAGIC 
// MAGIC `batchSize` applies to `BatchDiffFunctions`, which support using small minibatches of a dataset. `regularization` integrates L2 or L1 (depending on `useL1`) regularization with constant lambda. `alpha` controls the initial stepsize for algorithms that need it. `maxIterations` is the maximum number of gradient steps to be taken (or -1 for until convergence). `tolerance` controls the sensitivity of the 
// MAGIC convergence check. Finally, `useStochastic` determines whether or not batch functions should be optimized using a stochastic gradient algorithm (using small batches), or using LBFGS (using the entire dataset).
// MAGIC 
// MAGIC `OptParams` can be controlled using `breeze.config.Configuration`, which we described earlier.
// MAGIC 
// MAGIC ### breeze.optimize.linear
// MAGIC 
// MAGIC We provide a DSL for solving linear programs, using Apache's Simplex Solver as the backend. This package isn't industrial strength yet by any means, but it's good for simple problems. The DSL is pretty simple:
// MAGIC 
// MAGIC ```
// MAGIC import breeze.optimize.linear._
// MAGIC val lp = new LinearProgram()
// MAGIC import lp._
// MAGIC val x0 = Real()
// MAGIC val x1 = Real()
// MAGIC val x2 = Real()
// MAGIC 
// MAGIC val lpp =  ( (x0 +  x1 * 2 + x2 * 3 )
// MAGIC     subjectTo ( x0 * -1 + x1 + x2 <= 20)
// MAGIC     subjectTo ( x0 - x1 * 3 + x2 <= 30)
// MAGIC     subjectTo ( x0 <= 40 )
// MAGIC )
// MAGIC 
// MAGIC val result = maximize( lpp)
// MAGIC 
// MAGIC assert( norm(result.result - DenseVector(40.0,17.5,42.5), 2) < 1E-4)
// MAGIC ```
// MAGIC 
// MAGIC We also have specialized routines for bipartite matching (`KuhnMunkres` and `CompetitiveLinking`) and flow problems.
// MAGIC 
// MAGIC ## Breeze-Viz
// MAGIC 
// MAGIC **This API is highly experimental. It may change greatly. **
// MAGIC 
// MAGIC Breeze continues most of the functionality of Scalala's plotting facilities, though the API is somewhat different (in particular, more object oriented.) These methods are documented in scaladoc for the traits in the `breeze.plot` package object. First, let's plot some lines and save the image to file. All the actual plotting work is done by the excellent [`JFreeChart`](http://www.jfree.org/jfreechart/) library.
// MAGIC 
// MAGIC ```
// MAGIC import breeze.linalg._
// MAGIC import breeze.plot._
// MAGIC 
// MAGIC val f = Figure()
// MAGIC val p = f.subplot(0)
// MAGIC val x = linspace(0.0,1.0)
// MAGIC p += plot(x, x :^ 2.0)
// MAGIC p += plot(x, x :^ 3.0, '.')
// MAGIC p.xlabel = "x axis"
// MAGIC p.ylabel = "y axis"
// MAGIC f.saveas("lines.png") // save current figure as a .png, eps and pdf also supported
// MAGIC ```
// MAGIC ![two functions](http://scalanlp.org/examples/lines.png)
// MAGIC 
// MAGIC Then we'll add a new subplot and plot a histogram of 100,000 normally distributed random numbers into 100 buckets.
// MAGIC 
// MAGIC ```
// MAGIC val p2 = f.subplot(2,1,1)
// MAGIC val g = breeze.stats.distributions.Gaussian(0,1)
// MAGIC p2 += hist(g.sample(100000),100)
// MAGIC p2.title = "A normal distribution"
// MAGIC f.saveas("subplots.png")
// MAGIC ```
// MAGIC ![two plots](http://scalanlp.org/examples/subplots.png)
// MAGIC 
// MAGIC Breeze also supports the Matlab-like "image" command, here imaging a random matrix.
// MAGIC 
// MAGIC ```
// MAGIC val f2 = Figure()
// MAGIC f2.subplot(0) += image(DenseMatrix.rand(200,200))
// MAGIC f2.saveas("image.png")
// MAGIC ```
// MAGIC ![two plots](http://scalanlp.org/examples/image.png)
// MAGIC 
// MAGIC ## Where to go next?
// MAGIC 
// MAGIC After reading this quickstart, you can go to other wiki pages, especially [Linear Algebra Cheat-Sheet](https://github.com/scalanlp/breeze/wiki/Linear-Algebra-Cheat-Sheet) and [Data Structures](https://github.com/scalanlp/breeze/wiki/Data-Structures).

// COMMAND ----------

// MAGIC %md
// MAGIC #### Big O Notation for Space and Time Complexity by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning
// MAGIC **(watch later 5:52)**:
// MAGIC 
// MAGIC [![Big O Notation for Space and Time Complexity by Ameet Talwalkar in BerkeleyX: CS190.1x Scalable Machine Learning](http://img.youtube.com/vi/SmsEzDXb3c0/0.jpg)](https://www.youtube.com/watch?v=SmsEzDXb3c0)

// COMMAND ----------

// MAGIC %md
// MAGIC Watch this and take notes!