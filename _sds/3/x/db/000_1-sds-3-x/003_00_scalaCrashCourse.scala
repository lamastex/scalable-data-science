// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Scala Crash Course
// MAGIC 
// MAGIC Here we take a minimalist approach to learning just enough Scala, the language that Apache Spark is written in, to be able to use Spark effectively.
// MAGIC 
// MAGIC In the sequel we can learn more Scala concepts as they arise. This learning can be done by chasing the pointers in this crash course for a detailed deeper dive on your own time.
// MAGIC 
// MAGIC There are two basic ways in which we can learn Scala:
// MAGIC 
// MAGIC **1. Learn Scala in a notebook environment**
// MAGIC 
// MAGIC For convenience we use databricks Scala notebooks like this one here.
// MAGIC 
// MAGIC You can learn Scala locally on your own computer using Scala REPL (and Spark using Spark-Shell).
// MAGIC 
// MAGIC **2. Learn Scala in your own computer**
// MAGIC 
// MAGIC The most easy way to get Scala locally is through sbt, the Scala Build Tool. You can also use an IDE that integrates sbt.
// MAGIC 
// MAGIC See: [https://docs.scala-lang.org/getting-started/index.html](https://docs.scala-lang.org/getting-started/index.html) to set up Scala in your own computer.
// MAGIC 
// MAGIC **Software Engineering NOTE:** If you completed TASK 2 for **Cloud-free Computing Environment** in the notebook prefixed `002_00` using dockerCompose (optional exercise) then you will have Scala 2.11 with sbt and Spark 2.4 inside the docker services you can start and stop locally. Using docker volume binds you can also connect the docker container and its services (including local zeppelin or jupyter notebook servers as well as hadoop file system) to IDEs on your machine, etc.
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
// MAGIC 
// MAGIC ## What is Scala?
// MAGIC "Scala  smoothly  integrates  object-oriented  and  functional  programming.  It is designed to express common programming patterns in a concise, elegant, and type-safe way." by Matrin Odersky.
// MAGIC 
// MAGIC * High-level language for the Java Virtual Machine (JVM)
// MAGIC * Object oriented + functional programming
// MAGIC * Statically typed
// MAGIC * Comparable in speed to Java
// MAGIC * Type inference saves us from having to write explicit types most of the time Interoperates with Java
// MAGIC * Can use any Java class (inherit from, etc.)
// MAGIC * Can be called from Java code
// MAGIC 
// MAGIC See a quick tour here:
// MAGIC 
// MAGIC * [https://docs.scala-lang.org/tour/tour-of-scala.html](https://docs.scala-lang.org/tour/tour-of-scala.html)
// MAGIC 
// MAGIC ## Why Scala?
// MAGIC 
// MAGIC * Spark was originally written in Scala, which allows concise function syntax and interactive use
// MAGIC * Spark APIs for other languages include:
// MAGIC   * Java API for standalone use
// MAGIC   * Python API added to reach a wider user community of programmes
// MAGIC   * R API added more recently to reach a wider community of data analyststs 
// MAGIC   * Unfortunately, Python and R APIs are generally behind Spark's native Scala (for eg. GraphX is only available in Scala currently and datasets are only available in Scala as of 20200918).
// MAGIC * See Darren Wilkinson's 11 reasons for [scala as a platform for statistical computing and data science](https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/). It is embedded in-place below for your convenience.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Learn Scala in Notebook Environment
// MAGIC 
// MAGIC ***
// MAGIC ### Run a **Scala Cell**
// MAGIC * Run the following scala cell.
// MAGIC * Note: There is no need for any special indicator (such as ``%md``) necessary to create a Scala cell in a Scala notebook.
// MAGIC * You know it is a scala notebook because of the `` (Scala)`` appended to the name of this notebook.
// MAGIC * Make sure the cell contents updates before moving on.
// MAGIC * Press **Shift+Enter** when in the cell to run it and proceed to the next cell.
// MAGIC   * The cells contents should update.
// MAGIC   * Alternately, press **Ctrl+Enter** when in a cell to **run** it, but not proceed to the next cell.
// MAGIC * characters following ``//`` are comments in scala.
// MAGIC ***

// COMMAND ----------

1+1

// COMMAND ----------

println(System.currentTimeMillis) // press Ctrl+Enter to evaluate println that prints its argument as a line

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
// MAGIC See [Scala as a platform for statistical computing and data science](https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/).

// COMMAND ----------

displayHTML(frameIt("https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/",500))

// COMMAND ----------

// MAGIC %md
// MAGIC ## Let's get our hands dirty in Scala
// MAGIC 
// MAGIC We will go through the **following** programming concepts and tasks by building on [https://docs.scala-lang.org/tour/basics.html](https://docs.scala-lang.org/tour/basics.html).
// MAGIC 
// MAGIC * **Scala Types**
// MAGIC * **Expressions and Printing**
// MAGIC * **Naming and Assignments**
// MAGIC * **Functions and Methods in Scala**
// MAGIC * **Classes and Case Classes**
// MAGIC * **Methods and Tab-completion**
// MAGIC * **Objects and Traits**
// MAGIC * Collections in Scala and Type Hierarchy
// MAGIC * Functional Programming and MapReduce
// MAGIC * Lazy Evaluations and Recursions
// MAGIC 
// MAGIC **Remark**: You need to take a computer science course (from CourseEra, for example) to properly learn Scala.  Here, we will learn to use Scala by example to accomplish our data science tasks at hand. You can learn more Scala as needed from various sources pointed out above in **Scala Resources**.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Scala Types
// MAGIC 
// MAGIC In Scala, all values have a type, including numerical values and functions. The diagram below illustrates a subset of the type hierarchy. 
// MAGIC 
// MAGIC ![](https://docs.scala-lang.org/resources/images/tour/unified-types-diagram.svg)
// MAGIC 
// MAGIC For now, notice some common types we will be usinf including `Int`, `String`, `Double`, `Unit`, `Boolean`, `List`, etc. For more details see [https://docs.scala-lang.org/tour/unified-types.html](https://docs.scala-lang.org/tour/unified-types.html). We will return to this at the end of the notebook after seeing a brief tour of Scala now.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Expressions
// MAGIC 
// MAGIC Expressions are computable statements such as the `1+1` we have seen before.

// COMMAND ----------

1+1

// COMMAND ----------

// MAGIC %md
// MAGIC We can print the output of a computed or evaluated expressions as a line using `println`:

// COMMAND ----------

println(1+1) // printing 2

// COMMAND ----------

println("hej hej!") // printing a string

// COMMAND ----------

// MAGIC %md
// MAGIC ### Naming and Assignments 
// MAGIC 
// MAGIC **value and variable as ``val`` and ``var``**
// MAGIC 
// MAGIC You can name the results of expressions using keywords `val` and `var`.
// MAGIC 
// MAGIC Let us assign the integer value ``5`` to `x` as follows:

// COMMAND ----------

val x : Int = 5 // <Ctrl+Enter> to declare a value x to be integer 5. 

// COMMAND ----------

// MAGIC %md
// MAGIC `x` is a named result and it is a value since we used the keyword `val` when naming it.

// COMMAND ----------

// MAGIC %md
// MAGIC Scala is statically typed, but it uses built-in type inference machinery to automatically figure out that ``x`` is an integer or ``Int`` type as follows. 
// MAGIC Let's declare a value ``x`` to be ``Int`` 5 next without explictly using ``Int``.

// COMMAND ----------

val x = 5    // <Ctrl+Enter> to declare a value x as Int 5 (type automatically inferred)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's declare ``x`` as a ``Double`` or double-precision floating-point type using decimal such as ``5.0`` (a digit has to follow the decimal point!)

// COMMAND ----------

val x = 5.0   // <Ctrl+Enter> to declare a value x as Double 5

// COMMAND ----------

// MAGIC %md
// MAGIC Alternatively, we can assign ``x`` as a ``Double`` explicitly.  Note that the decimal point is not needed in this case due to explicit typing as ``Double``.

// COMMAND ----------

val x :  Double = 5    // <Ctrl+Enter> to declare a value x as Double 5 (type automatically inferred)

// COMMAND ----------

// MAGIC %md
// MAGIC Next note that labels need to be declared on first use. We have declared `x` to be a `val` which is short for *value*. This makes `x` immutable (cannot be changed).
// MAGIC 
// MAGIC Thus, `x` cannot be just re-assigned, as the following code illustrates in the resulting error: `... error: reassignment to val`.

// COMMAND ----------

//x = 10    //  uncomment and <Ctrl+Enter> to try to reassign val x to 10

// COMMAND ----------

// MAGIC %md
// MAGIC Scala allows declaration of mutable variables as well using ``var``, as follows:

// COMMAND ----------

var y = 2    // <Shift+Enter> to declare a variable y to be integer 2 and go to next cell

// COMMAND ----------

y = 3    // <Shift+Enter> to change the value of y to 3

// COMMAND ----------

y = y+1 // adds 1 to y

// COMMAND ----------

y += 2 // adds 2 to y

// COMMAND ----------

println(y) // the var y is 6 now

// COMMAND ----------

// MAGIC %md
// MAGIC ### Blocks
// MAGIC 
// MAGIC Just combine expressions by surrounding them with `{` and `}` called a block.

// COMMAND ----------

println({
  val x = 1+1
  x+2 // expression in last line is returned for the block
})// prints 4

// COMMAND ----------

println({ val x=22; x+2})

// COMMAND ----------

// MAGIC %md
// MAGIC ### Functions
// MAGIC 
// MAGIC Functions are expressions that have parameters. A function takes arguments as input and returns expressions as output.
// MAGIC 
// MAGIC A function can be nameless or *anonymous* and simply return an output from a given input. For example, the following annymous function returns the square of the input integer.

// COMMAND ----------

(x: Int) => x*x

// COMMAND ----------

// MAGIC %md
// MAGIC On the left of `=>` is a list of parameters with name and type. On the right is an expression involving the parameters.

// COMMAND ----------

// MAGIC %md
// MAGIC You can also name functions:

// COMMAND ----------

val multiplyByItself = (x: Int) => x*x

// COMMAND ----------

println(multiplyByItself(10))

// COMMAND ----------

// MAGIC %md
// MAGIC A function can have no parameters:

// COMMAND ----------

val howManyAmI = () => 1

// COMMAND ----------

println(howManyAmI()) // 1

// COMMAND ----------

// MAGIC %md
// MAGIC A function can have more than one parameter:

// COMMAND ----------

val multiplyTheseTwoIntegers = (a: Int, b: Int) => a*b

// COMMAND ----------

println(multiplyTheseTwoIntegers(2,4)) // 8

// COMMAND ----------

// MAGIC %md
// MAGIC ### Methods
// MAGIC 
// MAGIC Methods are very similar to functions, but a few key differences exist.
// MAGIC 
// MAGIC Methods use the `def` keyword followed by a name, parameter list(s), a return type, and a body.

// COMMAND ----------

def square(x: Int): Int = x*x    // <Shitf+Enter> to define a function named square

// COMMAND ----------

// MAGIC %md
// MAGIC Note that the return type `Int` is specified after the parameter list and a `:`.

// COMMAND ----------

square(5)    // <Shitf+Enter> to call this function on argument 5

// COMMAND ----------

val y = 3    // <Shitf+Enter> make val y as Int 3

// COMMAND ----------

square(y) // <Shitf+Enter> to call the function on val y of the right argument type Int

// COMMAND ----------

val x = 5.0     // let x be Double 5.0

// COMMAND ----------

//square(x) // <Shift+Enter> to call the function on val x of type Double will give type mismatch error

// COMMAND ----------

def square(x: Int): Int = { // <Shitf+Enter> to declare function in a block
  val answer = x*x
  answer // the last line of the function block is returned
}

// COMMAND ----------

square(5000)    // <Shift+Enter> to call the function

// COMMAND ----------

// <Shift+Enter> to define function with input and output type as String
def announceAndEmit(text: String): String = 
{
  println(text)
  text // the last line of the function block is returned
}

// COMMAND ----------

// MAGIC %md
// MAGIC Scala has a `return` keyword but it is rarely used as the expression in the last line of the multi-line block is the method's return value.

// COMMAND ----------

// <Ctrl+Enter> to call function which prints as line and returns as String
announceAndEmit("roger  roger")

// COMMAND ----------

// MAGIC %md
// MAGIC A method can have output expressions involving multiple parameter lists:

// COMMAND ----------

def multiplyAndTranslate(x: Int, y: Int)(translateBy: Int): Int = (x * y) + translateBy

// COMMAND ----------

println(multiplyAndTranslate(2, 3)(4))  // (2*3)+4 = 10

// COMMAND ----------

// MAGIC %md
// MAGIC A method can have no parameter lists at all:

// COMMAND ----------

def time: Long = System.currentTimeMillis

// COMMAND ----------

println("Current time in milliseconds is " + time)

// COMMAND ----------

println("Current time in milliseconds is " + time)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Classes
// MAGIC The `class` keyword followed by the name and constructor parameters is used to define a class.

// COMMAND ----------

class Box(h: Int, w: Int, d: Int) {
  def printVolume(): Unit = println(h*w*d)
}

// COMMAND ----------

// MAGIC %md
// MAGIC - The return type of the method `printVolume` is `Unit`.
// MAGIC - When the return type is `Unit` it indicates that there is nothing meaningful to return, similar to `void` in Java and C, but with a difference. 
// MAGIC - Because every Scala expression must have some value, there is actually a singleton value of type `Unit`, written `()` and carrying no information.

// COMMAND ----------

// MAGIC %md
// MAGIC We can make an instance of the class with the `new` keyword. 

// COMMAND ----------

val my1Cube = new Box(1,1,1)

// COMMAND ----------

// MAGIC %md
// MAGIC And call the method on the instance.

// COMMAND ----------

my1Cube.printVolume() // 1

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Our named instance `my1Cube` of the `Box` class is immutable due to `val`.
// MAGIC 
// MAGIC 
// MAGIC You can have mutable instances of the class using `var`.

// COMMAND ----------

var myVaryingCuboid = new Box(1,3,2)

// COMMAND ----------

myVaryingCuboid.printVolume()

// COMMAND ----------

myVaryingCuboid = new Box(1,1,1)

// COMMAND ----------

myVaryingCuboid.printVolume()

// COMMAND ----------

// MAGIC %md
// MAGIC See [https://docs.scala-lang.org/tour/classes.html](https://docs.scala-lang.org/tour/classes.html) for more details as needed.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Case Classes
// MAGIC 
// MAGIC Scala has a special type of class called a *case class* that can be defined with the `case class` keyword. 
// MAGIC 
// MAGIC Unlike classes, whose instances are compared by reference, instances of case classes are immutable by default and compared by value. This makes them useful for defining rows of typed values in Spark.

// COMMAND ----------

case class Point(x: Int, y: Int, z: Int)

// COMMAND ----------

// MAGIC %md
// MAGIC Case classes can be instantiated without the `new` keyword.

// COMMAND ----------

val point = Point(1, 2, 3)
val anotherPoint = Point(1, 2, 3)
val yetAnotherPoint = Point(2, 2, 2)

// COMMAND ----------

// MAGIC %md
// MAGIC Instances of case classes are compared by value and not by reference.

// COMMAND ----------

if (point == anotherPoint) {
  println(point + " and " + anotherPoint + " are the same.")
} else {
  println(point + " and " + anotherPoint + " are different.")
} // Point(1,2,3) and Point(1,2,3) are the same.

if (point == yetAnotherPoint) {
  println(point + " and " + yetAnotherPoint + " are the same.")
} else {
  println(point + " and " + yetAnotherPoint + " are different.")
} // Point(1,2,3) and Point(2,2,2) are different.


// COMMAND ----------

// MAGIC %md
// MAGIC By contrast, instances of classes are compared by reference.

// COMMAND ----------

myVaryingCuboid.printVolume() // should be 1 x 1 x 1

// COMMAND ----------

my1Cube.printVolume()  // should be 1 x 1 x 1

// COMMAND ----------

if (myVaryingCuboid == my1Cube) {
  println("myVaryingCuboid and my1Cube are the same.")
} else {
  println("myVaryingCuboid and my1Cube are different.")
} // they are compared by reference and are not the same.

// COMMAND ----------

// MAGIC %md
// MAGIC More about case classes here: [https://docs.scala-lang.org/tour/case-classes.html](https://docs.scala-lang.org/tour/case-classes.html).

// COMMAND ----------

// MAGIC %md
// MAGIC ### Methods and Tab-completion
// MAGIC 
// MAGIC Many methods of a class can be accessed by `.`.

// COMMAND ----------

val s  = "hi"    // <Ctrl+Enter> to declare val s to String "hi"

// COMMAND ----------

// MAGIC %md
// MAGIC You can place the cursor after ``.`` following a declared object and find out the methods available for it as shown in the image below.
// MAGIC 
// MAGIC ![tabCompletionAfterSDot PNG image](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/images/week1/tabCompletionAfterSDot.png)
// MAGIC 
// MAGIC **You Try** doing this next.

// COMMAND ----------

//s.  // place cursor after the '.' and press Tab to see all available methods for s 

// COMMAND ----------

// MAGIC %md
// MAGIC For example, 
// MAGIC 
// MAGIC * scroll down to ``contains`` and double-click on it.  
// MAGIC * This should lead to ``s.contains`` in your cell. 
// MAGIC * Now add an argument String to see if ``s`` contains the argument, for example, try:
// MAGIC   * ``s.contains("f")``
// MAGIC   * ``s.contains("")`` and
// MAGIC   * ``s.contains("i")``

// COMMAND ----------

//s    // <Shift-Enter> recall the value of String s

// COMMAND ----------

s.contains("f")     // <Shift-Enter> returns Boolean false since s does not contain the string "f"

// COMMAND ----------

s.contains("")    // <Shift-Enter> returns Boolean true since s contains the empty string ""

// COMMAND ----------

s.contains("i")    // <Ctrl+Enter> returns Boolean true since s contains the string "i"

// COMMAND ----------

// MAGIC %md
// MAGIC ### Objects
// MAGIC 
// MAGIC Objects are single instances of their own definitions using the `object` keyword. You can think of them as singletons of their own classes.

// COMMAND ----------

object IdGenerator {
  private var currentId = 0
  def make(): Int = {
    currentId += 1
    currentId
  }
}

// COMMAND ----------

// MAGIC %md
// MAGIC You can access an object through its name:

// COMMAND ----------

val newId: Int = IdGenerator.make()
val newerId: Int = IdGenerator.make()

// COMMAND ----------

println(newId) // 1
println(newerId) // 2

// COMMAND ----------

// MAGIC %md
// MAGIC For details see [https://docs.scala-lang.org/tour/singleton-objects.html](https://docs.scala-lang.org/tour/singleton-objects.html)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Traits
// MAGIC 
// MAGIC Traits are abstract data types containing certain fields and methods. They can be defined using the `trait` keyword.
// MAGIC 
// MAGIC In Scala inheritance, a class can only extend one other class, but it can extend multiple traits.

// COMMAND ----------

trait Greeter {
  def greet(name: String): Unit
}

// COMMAND ----------

// MAGIC %md
// MAGIC Traits can have default implementations also.

// COMMAND ----------

trait Greeter {
  def greet(name: String): Unit =
    println("Hello, " + name + "!")
}


// COMMAND ----------

// MAGIC %md
// MAGIC You can extend traits with the `extends` keyword and override an implementation with the `override` keyword:

// COMMAND ----------

class DefaultGreeter extends Greeter

class SwedishGreeter extends Greeter {
  override def greet(name: String): Unit = {
    println("Hej hej, " + name + "!")
  }
}

class CustomizableGreeter(prefix: String, postfix: String) extends Greeter {
  override def greet(name: String): Unit = {
    println(prefix + name + postfix)
  }
}

// COMMAND ----------

// MAGIC %md
// MAGIC Instantiate the classes.

// COMMAND ----------

val greeter = new DefaultGreeter()
val swedishGreeter = new SwedishGreeter()
val customGreeter = new CustomizableGreeter("How are you, ", "?")

// COMMAND ----------

// MAGIC %md
// MAGIC Call the `greet` method in each case.

// COMMAND ----------

greeter.greet("Scala developer") // Hello, Scala developer!
swedishGreeter.greet("Scala developer") // Hej hej, Scala developer!
customGreeter.greet("Scala developer") // How are you, Scala developer?

// COMMAND ----------

// MAGIC %md
// MAGIC A class can also be made to extend multiple traits. 
// MAGIC 
// MAGIC For more details see: [https://docs.scala-lang.org/tour/traits.html](https://docs.scala-lang.org/tour/traits.html).

// COMMAND ----------

// MAGIC %md
// MAGIC ### Main Method
// MAGIC 
// MAGIC The main method is the entry point of a Scala program. 
// MAGIC 
// MAGIC The Java Virtual Machine requires a main method, named `main`, that takes an array of strings as its only argument.
// MAGIC 
// MAGIC Using an object, you can define the main method as follows:

// COMMAND ----------

object Main {
  def main(args: Array[String]): Unit =
    println("Hello, Scala developer!")
}

// COMMAND ----------

// MAGIC %md
// MAGIC **What I try not do while learning a new language?**
// MAGIC 
// MAGIC 1. I don't immediately try to ask questions like: *how can I do this particular variation of some small thing I just learned so I can use patterns I am used to from another language I am hooked-on right now?*
// MAGIC 2. first go through the detailed Scala Tour on your own and then through the 50 odd lessons in the Scala Book
// MAGIC 3. then return to 1. and ask detailed cross-language comparison questions by diving deep as needed with the source and scala docs as needed (google or duck-duck-go search!).