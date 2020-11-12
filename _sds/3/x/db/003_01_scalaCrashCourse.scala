// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Scala Resources
// MAGIC 
// MAGIC You will not be learning scala systematically and thoroughly in this course.  You will learn *to use* Scala by doing various Spark jobs. 
// MAGIC 
// MAGIC If you are interested in learning scala properly, then there are various resources, including:
// MAGIC 
// MAGIC * [scala-lang.org](http://www.scala-lang.org/) is the **core Scala resource**. Bookmark the following three links:
// MAGIC   * [tour-of-scala](https://docs.scala-lang.org/tour/tour-of-scala.html) - Bite-sized introductions to core language features.
// MAGIC     - we will go through the tour in a hurry now as some Scala familiarity is needed immediately.
// MAGIC   * [scala-book](https://docs.scala-lang.org/overviews/scala-book/introduction.html) - An online book introducing the main language features
// MAGIC     - you are expected to use this resource to figure out Scala as needed.
// MAGIC   * [scala-cheatsheet](https://docs.scala-lang.org/cheatsheets/index.html) - A handy cheatsheet covering the basics of Scala syntax. 
// MAGIC   * [visual-scala-reference](https://superruzafa.github.io/visual-scala-reference/) - This guide collects some of the most common functions of the Scala Programming Language and explain them conceptual and graphically in a simple way.
// MAGIC * [Online Resources](https://docs.scala-lang.org/learn.html), including:
// MAGIC   * [courseera: Functional Programming Principles in Scala](https://www.coursera.org/course/progfun)
// MAGIC * [Books](http://www.scala-lang.org/documentation/books.html)
// MAGIC   * [Programming in Scala, 1st Edition, Free Online Reading](http://www.artima.com/pins1ed/)
// MAGIC   
// MAGIC The main sources for the following content are (you are encouraged to read them for more background):
// MAGIC 
// MAGIC * [Martin Oderski's Scala by example](https://www.scala-lang.org/old/sites/default/files/linuxsoft_archives/docu/files/ScalaByExample.pdf)
// MAGIC * [Scala crash course by Holden Karau](http://lintool.github.io/SparkTutorial/slides/day1_Scala_crash_course.pdf)
// MAGIC * [Darren's brief introduction to scala and breeze for statistical computing](https://darrenjw.wordpress.com/2013/12/30/brief-introduction-to-scala-and-breeze-for-statistical-computing/)

// COMMAND ----------

//%run "/scalable-data-science/xtraResources/support/sdsFunctions"
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

// COMMAND ----------

// MAGIC %md
// MAGIC # Let's continue to get our hands dirty in Scala
// MAGIC 
// MAGIC We will go through the **remaining** programming concepts and tasks by building on [https://docs.scala-lang.org/tour/basics.html](https://docs.scala-lang.org/tour/basics.html).
// MAGIC 
// MAGIC * Scala Types
// MAGIC * Expressions and Printing
// MAGIC * Naming and Assignments
// MAGIC * Functions and Methods in Scala
// MAGIC * Classes and Case Classes
// MAGIC * Methods and Tab-completion
// MAGIC * Objects and Traits
// MAGIC * **Collections in Scala and Type Hierarchy**
// MAGIC * **Functional Programming and MapReduce**
// MAGIC * **Lazy Evaluations and Recursions**
// MAGIC 
// MAGIC **Remark**: You need to take a computer science course (from CourseEra, for example) to properly learn Scala.  Here, we will learn to use Scala by example to accomplish our data science tasks at hand. You can learn more Scala as needed from various sources pointed out above in **Scala Resources**.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # Scala Type Hierarchy
// MAGIC 
// MAGIC In Scala, all values have a type, including numerical values and functions. The diagram below illustrates a subset of the type hierarchy. 
// MAGIC 
// MAGIC ![](https://docs.scala-lang.org/resources/images/tour/unified-types-diagram.svg)
// MAGIC 
// MAGIC For now, notice some common types we will be usinf including `Int`, `String`, `Double`, `Unit`, `Boolean`, `List`, etc. For more details see [https://docs.scala-lang.org/tour/unified-types.html](https://docs.scala-lang.org/tour/unified-types.html). 
// MAGIC 
// MAGIC Let us take a closer look at Scala Type Hierarchy now.

// COMMAND ----------

displayHTML(frameIt("https://docs.scala-lang.org/tour/unified-types.html",550))

// COMMAND ----------

// MAGIC %md
// MAGIC # Scala Collections
// MAGIC  
// MAGIC Familiarize yourself with the main Scala collections classes here:
// MAGIC 
// MAGIC - [https://docs.scala-lang.org/overviews/scala-book/collections-101.html](https://docs.scala-lang.org/overviews/scala-book/collections-101.html)

// COMMAND ----------

displayHTML(frameIt("https://docs.scala-lang.org/overviews/scala-book/collections-101.html",550))

// COMMAND ----------

// MAGIC %md
// MAGIC ## List
// MAGIC 
// MAGIC Lists are one of the most basic data structures.
// MAGIC 
// MAGIC There are several other Scala collections and we will introduce them as needed.  The other most common ones are `Vector`, `Array` and `Seq` and the `ArrayBuffer`.
// MAGIC 
// MAGIC For details on list see:
// MAGIC  - [https://docs.scala-lang.org/overviews/scala-book/list-class.html](https://docs.scala-lang.org/overviews/scala-book/list-class.html)

// COMMAND ----------

// <Ctrl+Enter> to declare (an immutable) val lst as List of Int's 1,2,3
val lst = List(1, 2, 3)

// COMMAND ----------



// COMMAND ----------

// MAGIC %md
// MAGIC ## Vectors
// MAGIC 
// MAGIC > The Vector class is an indexed, immutable sequence. The “indexed” part of the description means that you can access Vector elements very rapidly by their index value, such as accessing listOfPeople(999999).
// MAGIC 
// MAGIC In general, except for the difference that Vector is indexed and List is not, the two classes work the same, so we’ll run through these examples quickly.
// MAGIC 
// MAGIC For details see:
// MAGIC  - [https://docs.scala-lang.org/overviews/scala-book/vector-class.html](https://docs.scala-lang.org/overviews/scala-book/vector-class.html)

// COMMAND ----------

val vec = Vector(1,2,3)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Arrays, Sequences and Tuples
// MAGIC 
// MAGIC See [https://www.scala-lang.org/api/current/scala/collection/index.html](https://www.scala-lang.org/api/current/scala/collection/index.html) for docs.

// COMMAND ----------

val arr = Array(1,2,3) // <Shift-Enter> to declare an Array

// COMMAND ----------

val seq = Seq(1,2,3)    // <Shift-Enter> to declare a Seq

// COMMAND ----------

// MAGIC %md
// MAGIC > A tuple is a neat class that gives you a simple way to store heterogeneous (different) items in the same container.
// MAGIC We will use tuples for key-value pairs in Spark.
// MAGIC 
// MAGIC See [https://docs.scala-lang.org/overviews/scala-book/tuples.html](https://docs.scala-lang.org/overviews/scala-book/tuples.html)

// COMMAND ----------

val myTuple = ('a',1) // a 2-tuple

// COMMAND ----------

myTuple._1 // accessing the first element of the tuple. NOTE index starts at 1 not 0 for tuples

// COMMAND ----------

myTuple._2 // accessing the second element of the tuple

// COMMAND ----------

// MAGIC %md
// MAGIC ## Functional Programming and MapReduce
// MAGIC 
// MAGIC *"Functional programming is a style of programming that emphasizes writing applications using only pure functions and immutable values. As Alvin Alexander wrote in Functional Programming, Simplified, rather than using that description, it can be helpful to say that functional programmers have an extremely strong desire to see their code as math — to see the combination of their functions as a series of algebraic equations. In that regard, you could say that functional programmers like to think of themselves as mathematicians. That’s the driving desire that leads them to use only pure functions and immutable values, because that’s what you use in algebra and other forms of math."* 
// MAGIC 
// MAGIC See [https://docs.scala-lang.org/overviews/scala-book/functional-programming.html](https://docs.scala-lang.org/overviews/scala-book/functional-programming.html) for short lessons in functional programming.
// MAGIC 
// MAGIC We will apply functions for processing elements of a scala collection to quickly demonstrate functional programming.
// MAGIC 
// MAGIC ### Five ways of adding 1
// MAGIC 
// MAGIC The first four use anonymous functions and the last one uses a named method.
// MAGIC 
// MAGIC 1. explicit version:
// MAGIC ```%scala
// MAGIC (x: Int) => x + 1  
// MAGIC ```
// MAGIC 
// MAGIC 2. type-inferred more intuitive version:
// MAGIC ```%scala
// MAGIC x => x + 1   
// MAGIC ```
// MAGIC 
// MAGIC 3. placeholder syntax (each argument must be used exactly once):
// MAGIC ```%scala
// MAGIC _ + 1 
// MAGIC ```
// MAGIC 
// MAGIC 4. type-inferred more intuitive version with code-block for larger function body:
// MAGIC ```%scala
// MAGIC x => { 
// MAGIC       // body is a block of code
// MAGIC       val integerToAdd = 1
// MAGIC       x + integerToAdd
// MAGIC }
// MAGIC ```
// MAGIC 
// MAGIC 5. as methods using ``def``:
// MAGIC ```%scala
// MAGIC def addOne(x: Int): Int = x + 1
// MAGIC  ```

// COMMAND ----------

displayHTML(frameIt("https://superruzafa.github.io/visual-scala-reference/map/",500))

// COMMAND ----------

// MAGIC %md
// MAGIC Now, let's do some functional programming over scala collection (`List`) using some of their methods: `map`, `filter` and `reduce`. In the end we will write our first mapReduce program!
// MAGIC 
// MAGIC For more details see:
// MAGIC 
// MAGIC - [https://docs.scala-lang.org/overviews/scala-book/collections-methods.html](https://docs.scala-lang.org/overviews/scala-book/collections-methods.html)

// COMMAND ----------

displayHTML(frameIt("https://superruzafa.github.io/visual-scala-reference/map/",500))

// COMMAND ----------

// <Shift+Enter> to map each value x of lst with x+10 to return a new List(11, 12, 13)
lst.map(x => x + 10)  

// COMMAND ----------

// <Shift+Enter> for the same as above using place-holder syntax
lst.map( _ + 10)  

// COMMAND ----------

displayHTML(frameIt("https://superruzafa.github.io/visual-scala-reference/filter/",600))

// COMMAND ----------

// <Shift+Enter> to return a new List(1, 3) after filtering x's from lst if (x % 2 == 1) is true
lst.filter(x => (x % 2 == 1) )

// COMMAND ----------

// <Shift+Enter> for the same as above using place-holder syntax
lst.filter( _ % 2 == 1 )

// COMMAND ----------

displayHTML(frameIt("https://superruzafa.github.io/visual-scala-reference/reduce/",600))

// COMMAND ----------

// <Shift+Enter> to use reduce to add elements of lst two at a time to return Int 6
lst.reduce( (x, y) => x + y )

// COMMAND ----------

// <Ctrl+Enter> for the same as above but using place-holder syntax
lst.reduce( _ + _ )

// COMMAND ----------

// MAGIC %md
// MAGIC Let's combine `map` and `reduce` programs above to find the sum of after 10 has been added to every element of the original List `lst` as follows:

// COMMAND ----------

lst.map(x => x+10)
   .reduce((x,y) => x+y) // <Ctrl-Enter> to get Int 36 = sum(1+10,2+10,3+10)

// COMMAND ----------

// MAGIC %md
// MAGIC # Exercise in Functional Programming
// MAGIC 
// MAGIC You should spend an hour or so going through the Functional Programming Section of the Scala Book:
// MAGIC 
// MAGIC - [https://docs.scala-lang.org/overviews/scala-book/functional-programming.html](https://docs.scala-lang.org/overviews/scala-book/functional-programming.html)

// COMMAND ----------

displayHTML(frameIt("https://docs.scala-lang.org/overviews/scala-book/functional-programming.html",700))

// COMMAND ----------

// MAGIC %md
// MAGIC There are lots of methods in Scala Collections. And much more in this *scalable language*.
// MAGIC See for example [http://docs.scala-lang.org/cheatsheets/index.html](http://docs.scala-lang.org/cheatsheets/index.html).

// COMMAND ----------

// MAGIC %md
// MAGIC # Lazy Evaluation 
// MAGIC 
// MAGIC Another powerful programming concept we will need is *lazy evaluation* -- a form of delayed evaluation. So the value of an expression that is lazily evaluated is only available when it is actually needed.
// MAGIC 
// MAGIC This is to be contrasted with *eager evaluation* that we have seen so far -- an expression is immediately evaluated.

// COMMAND ----------

val eagerImmutableInt = 1 // eagerly evaluated as 1

// COMMAND ----------

var eagerMutableInt = 2 // eagerly evaluated as 2

// COMMAND ----------

// MAGIC %md
// MAGIC Let's demonstrate lazy evaluation using a `getTime` method and the keyword `lazy`.

// COMMAND ----------

import java.util.Calendar

// COMMAND ----------

lazy val lazyImmutableTime = Calendar.getInstance.getTime // lazily defined and not evaluated immediately

// COMMAND ----------

val eagerImmutableTime = Calendar.getInstance.getTime // egaerly evaluated immediately

// COMMAND ----------

println(lazyImmutableTime) // evaluated when actully needed by println

// COMMAND ----------

println(eagerImmutableTime) // prints what was already evaluated eagerly

// COMMAND ----------

def lazyDefinedInt = 5 // you can also use method to lazily define 

// COMMAND ----------

lazyDefinedInt // only evaluated now

// COMMAND ----------

// MAGIC %md
// MAGIC See [https://www.scala-exercises.org/scala_tutorial/lazy_evaluation](https://www.scala-exercises.org/scala_tutorial/lazy_evaluation) for more details including the following example with `StringBuilder`.

// COMMAND ----------

val builder = new StringBuilder

// COMMAND ----------

val x = { builder += 'x'; 1 } // eagerly evaluates x as 1 after appending 'x' to builder. NOTE: ';' is used to separate multiple expressions on the same line

// COMMAND ----------

builder.result()

// COMMAND ----------

x

// COMMAND ----------

builder.result() // calling x again should not append x again to builder

// COMMAND ----------

lazy val y = { builder += 'y'; 2 } // lazily evaluate y later when it is called

// COMMAND ----------

builder.result() // builder should remain unchanged

// COMMAND ----------

def z = { builder += 'z'; 3 } // lazily evaluate z later when the method is called

// COMMAND ----------

builder.result() // builder should remain unchanged

// COMMAND ----------

// MAGIC %md
// MAGIC What should `builder.result()` be after the following arithmetic expression involving `x`,`y` and `z` is evaluated?

// COMMAND ----------

z + y + x + z + y + x

// COMMAND ----------

// MAGIC %md
// MAGIC ### Lazy Evaluation Exercise - You try Now!
// MAGIC 
// MAGIC Understand why the output above is what it is!
// MAGIC 
// MAGIC - Why is `z` different in its appearance in the final `builder` string when compared to `x` and `y` as we evaluated?
// MAGIC 
// MAGIC ```
// MAGIC z + y + x + z + y + x
// MAGIC ```

// COMMAND ----------

builder.result() 

// COMMAND ----------

// MAGIC %md
// MAGIC ## Why Lazy?
// MAGIC 
// MAGIC Imagine a more complex expression involving the evaluation of millions of values. Lazy evaluation will allow us to actually compute with big data when it may become impossible to hold all the values in memory. This is exactly what Apache Spark does as we will see.

// COMMAND ----------

// MAGIC %md
// MAGIC # Recursions
// MAGIC 
// MAGIC Recursion is a powerful framework when a function calls another function, including itself, until some terminal condition is reached.
// MAGIC 
// MAGIC Here we want to distinguish between two ways of implementing a recursion using a simple example of factorial.
// MAGIC 
// MAGIC Recall that for any natural number \\(n\\), its factorial is denoted and defined as follows:
// MAGIC 
// MAGIC \\[
// MAGIC n!  :=  n \times (n-1) \times (n-2) \times \cdots \times 2 \times 1 
// MAGIC \\]
// MAGIC 
// MAGIC which has the following recursive expression:
// MAGIC 
// MAGIC \\[
// MAGIC n! = n*(n-1)! \, , \qquad  0! = 1
// MAGIC \\]
// MAGIC 
// MAGIC Let us implement it using two approaches: a naive approach that can run out of memory and another tail-recursive approach that uses constant memory. Read [https://www.scala-exercises.org/scala_tutorial/tail_recursion](https://www.scala-exercises.org/scala_tutorial/tail_recursion) for details.

// COMMAND ----------

def factorialNaive(n: Int): Int =
  if (n == 0) 1 else n * factorialNaive(n - 1)

// COMMAND ----------

factorialNaive(4)

// COMMAND ----------

// MAGIC %md
// MAGIC When `factorialNaive(4)` was evaluated above the following steps were actually done:
// MAGIC 
// MAGIC ```
// MAGIC factorial(4)
// MAGIC if (4 == 0) 1 else 4 * factorial(4 - 1)
// MAGIC 4 * factorial(3)
// MAGIC 4 * (3 * factorial(2))
// MAGIC 4 * (3 * (2 * factorial(1)))
// MAGIC 4 * (3 * (2 * (1 * factorial(0)))
// MAGIC 4 * (3 * (2 * (1 * 1)))
// MAGIC 24
// MAGIC ```

// COMMAND ----------

// MAGIC %md
// MAGIC Notice how we add one more element to our expression at each recursive call. Our expressions becomes bigger and bigger until we end by reducing it to the final value.
// MAGIC So the final expression given by a directed acyclic graph (DAG) of the pairwise multiplications given by the right-branching binary tree, whose leaves are input integers and internal nodes are the bianry `*` operator, can get very large when the input `n` is large.
// MAGIC 
// MAGIC *Tail recursion* is a sophisticated way of implementing certain recursions so that memory requirements can be kept constant, as opposed to naive recursions. 
// MAGIC 
// MAGIC > [Tail Recursion](https://www.scala-exercises.org/scala_tutorial/tail_recursion)
// MAGIC 
// MAGIC > That difference in the rewriting rules actually translates directly to a difference in the actual execution on a computer. In fact, it turns out that **if you have a recursive function that calls itself as its last action, then you can reuse the stack frame of that function. This is called tail recursion.**
// MAGIC 
// MAGIC > And by applying that trick, a tail recursive function can execute in constant stack space, so it's really just another formulation of an iterative process. We could say a tail recursive function is the functional form of a loop, and it executes just as efficiently as a loop.
// MAGIC 
// MAGIC 
// MAGIC Implementation of tail recursion in the Exercise below uses Scala [annotation](https://docs.scala-lang.org/tour/annotations.html), which is a way to associate meta-information with definitions. In our case, the annotation `@tailrec` ensures that a method is indeed [tail-recursive](https://en.wikipedia.org/wiki/Tail_call). See the last link to understand how memory requirements can be kept constant in tail recursions.
// MAGIC 
// MAGIC We mainly want you to know that tail recursions are an important functional programming concept.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Tail Recursion Exercise - You Try Now
// MAGIC 
// MAGIC Replace ``???`` with the correct values to make this a tail recursion for factorial.

// COMMAND ----------

import scala.annotation.tailrec

// replace ??? with the right values to make this a tail recursion for factorial
def factorialTail(n: Int): Int = {
  @tailrec
  def iter(x: Int, result: Int): Int =
    if ( x == ??? ) result
    else iter(x - 1, result * x)

  iter( n, ??? )
}

// COMMAND ----------

factorialTail(3) //shouldBe 6

// COMMAND ----------

factorialTail(4) //shouldBe 24

// COMMAND ----------

// MAGIC %md
// MAGIC Functional Programming is a vast subject and we are merely covering the fewest core ideas to get started with Apache Spark asap.
// MAGIC 
// MAGIC We will return to more concepts as we need them in the sequel.