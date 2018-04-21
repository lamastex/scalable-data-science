// Databricks notebook source
// MAGIC %md
// MAGIC # [SDS-2.2-360-in-525-01: Intro to Apache Spark for data Scientists](https://lamastex.github.io/scalable-data-science/360-in-525/2018/01/)
// MAGIC ### [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)

// COMMAND ----------

// MAGIC %md
// MAGIC This is *slightly* outdated, but good enough for our purposes as we will do it live on the latest databricks notebooks.
// MAGIC 
// MAGIC Please go here for a relaxed and detailed-enough tour (later):
// MAGIC 
// MAGIC * [https://docs.databricks.com/index.html](https://docs.databricks.com/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Notebooks
// MAGIC Write Spark code for processing your data in notebooks. 
// MAGIC 
// MAGIC **NOTE**: You should have already cloned this notebook and attached it to a cluster that you started in the Community Edition of databricks by now.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Notebooks can be written in **Python**, **Scala**, **R**, or **SQL**.
// MAGIC 
// MAGIC * This is a Scala notebook - which is indicated next to the title above by ``(Scala)``.

// COMMAND ----------

// MAGIC %md
// MAGIC ### **Creating a new Notebook**
// MAGIC 
// MAGIC  ![Change Name](http://training.databricks.com/databricks_guide/Notebook/createNotebook.png)
// MAGIC 
// MAGIC   * Click the tiangle on the right side of a folder to open the folder menu.
// MAGIC   * Select **Create > Notebook**.
// MAGIC   * Enter the name of the notebook, the language (Python, Scala, R or SQL) for the notebook, and a cluster to run it on.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### Cloning a Notebook
// MAGIC   * You can clone a notebook to create a copy of it, for example if you want to edit or run an Example notebook like this one.
// MAGIC   * Click **File > Clone** in the notebook context bar above.
// MAGIC   * Enter a new name and location for your notebook. If Access Control is enabled, you can only clone to folders that you have Manage permissions on.

// COMMAND ----------

// MAGIC %md
// MAGIC # Introduction to Scala through Scala Notebook
// MAGIC 
// MAGIC * This introduction notebook describes how to get started running Scala code in Notebooks.

// COMMAND ----------

// MAGIC %md
// MAGIC ### Clone Or Import This Notebook
// MAGIC * From the **File** menu at the top left of this notebook, choose **Clone** or click **Import Notebook** on the top right. This will allow you to interactively execute code cells as you proceed through the notebook.
// MAGIC 
// MAGIC ![Menu Bar Clone Notebook](http://training.databricks.com/databricks_guide/2.8/clone.png) 
// MAGIC * Enter a name and a desired location for your cloned notebook (i.e. Perhaps clone to your own user directory or the "Shared" directory.)
// MAGIC * Navigate to the location you selected (e.g. click Menu > Workspace > `Your cloned location`)

// COMMAND ----------

// MAGIC %md
// MAGIC ### **Attach** the Notebook to a **cluster**
// MAGIC * A **Cluster** is a group of machines which can run commands in cells.
// MAGIC * Check the upper left corner of your notebook to see if it is **Attached** or **Detached**.
// MAGIC * If **Detached**, click on the right arrow and select a cluster to attach your notebook to. 
// MAGIC   * If there is no running cluster, create one as described in the [Welcome to Databricks](/#workspace/databricks_guide/00 Welcome to Databricks) guide.
// MAGIC 
// MAGIC ![Attach Notebook](http://training.databricks.com/databricks_guide/2.8/detached.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC #### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Cells** are units that make up notebooks
// MAGIC ![A Cell](http://training.databricks.com/databricks_guide/cell.png)
// MAGIC 
// MAGIC Cells each have a type - including **scala**, **python**, **sql**, **R**, **markdown**, **filesystem**, and **shell**.
// MAGIC * While cells default to the type of the Notebook, other cell types are supported as well.
// MAGIC * This cell is in **markdown** and is used for documentation. [Markdown](http://en.wikipedia.org/wiki/Markdown) is a simple text formatting syntax.
// MAGIC ***

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ***
// MAGIC ### **Create** and **Edit** a New Markdown Cell in this Notebook
// MAGIC 
// MAGIC * When you mouse between cells, a + sign will pop up in the center that you can click on to create a new cell.
// MAGIC 
// MAGIC  ![New Cell](http://training.databricks.com/databricks_guide/create_new_cell.png)
// MAGIC * Type **``%md Hello, world!``** into your new cell (**``%md``** indicates the cell is markdown).
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC * Click out of the cell to see the cell contents update.
// MAGIC   
// MAGIC   ![Run cell](http://training.databricks.com/databricks_guide/run_cell.png)
// MAGIC   
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### **Running a cell in your notebook.**
// MAGIC * #### Press **Shift+Enter** when in the cell to **run** it and proceed to the next cell.
// MAGIC   * The cells contents should update.
// MAGIC   ![Run cell](http://training.databricks.com/databricks_guide/run_cell.png)
// MAGIC * **NOTE:** Cells are not automatically run each time you open it.
// MAGIC   * Instead, Previous results from running a cell are saved and displayed.
// MAGIC * #### Alternately, press **Ctrl+Enter** when in a cell to **run** it, but not proceed to the next cell.

// COMMAND ----------

// MAGIC %md
// MAGIC **You Try Now!** 
// MAGIC Just double-click the cell below, modify the text following ``%md`` and press **Ctrl+Enter** to evaluate it and see it's mark-down'd output.
// MAGIC ```
// MAGIC > %md Hello, world!
// MAGIC ```

// COMMAND ----------

// MAGIC %md 
// MAGIC Hello, world!

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC #### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Markdown Cell Tips**
// MAGIC * To change a non-markdown cell to markdown, add **%md** to very start of the cell.
// MAGIC * After updating the contents of a markdown cell, click out of the cell to update the formatted contents of a markdown cell.
// MAGIC * To edit an existing markdown cell, **doubleclick** the cell.
// MAGIC ***

// COMMAND ----------

// MAGIC %md
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

println(System.currentTimeMillis) // press Ctrl+Enter to evaluate println that prints its argument as a line

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## Scala Resources
// MAGIC 
// MAGIC You will not be learning scala systematically and thoroughly in this course.  You will learn *to use* Scala by doing various Spark jobs. 
// MAGIC 
// MAGIC If you are seriously interested in learning scala properly, then there are various resources, including:
// MAGIC 
// MAGIC * [scala-lang.org](http://www.scala-lang.org/) is the **core Scala resource**.
// MAGIC   * [tour-of-scala](http://docs.scala-lang.org/tutorials/tour/tour-of-scala)
// MAGIC * MOOC
// MAGIC   * [courseera: Functional Programming Principles in Scala](https://www.coursera.org/course/progfun)
// MAGIC * [Books](http://www.scala-lang.org/documentation/books.html)
// MAGIC   * [Programming in Scala, 1st Edition, Free Online Reading](http://www.artima.com/pins1ed/)
// MAGIC   
// MAGIC The main sources for the following content are (you are encouraged to read them for more background):
// MAGIC 
// MAGIC * [Martin Oderski's Scala by example](http://www.scala-lang.org/docu/files/ScalaByExample.pdf)
// MAGIC * [Scala crash course by Holden Karau](http://lintool.github.io/SparkTutorial/slides/day1_Scala_crash_course.pdf)
// MAGIC * [Darren's brief introduction to scala and breeze for statistical computing](https://darrenjw.wordpress.com/2013/12/30/brief-introduction-to-scala-and-breeze-for-statistical-computing/)
// MAGIC 
// MAGIC   

// COMMAND ----------

// MAGIC %md
// MAGIC #Introduction to Scala
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
// MAGIC ## Why Scala?
// MAGIC 
// MAGIC * Spark was originally written in Scala, which allows concise function syntax and interactive use
// MAGIC * Spark APIs for other languages include:
// MAGIC   * Java API for standalone use
// MAGIC   * Python API added to reach a wider user community of programmes
// MAGIC   * R API added more recently to reach a wider community of data analyststs 
// MAGIC   * Unfortunately, Python and R APIs are generally behind Spark's native Scala (for eg. GraphX is only available in Scala currently).
// MAGIC * See Darren Wilkinson's 11 reasons for [scala as a platform for statistical computing and data science](https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/). It is embedded in-place below for your convenience.

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

displayHTML(frameIt("https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/",500))

// COMMAND ----------

// MAGIC %md
// MAGIC # Let's get our hands dirty in Scala
// MAGIC 
// MAGIC We will go through the following programming concepts and tasks:
// MAGIC 
// MAGIC * Assignments
// MAGIC * Methods and Tab-completion
// MAGIC * Functions in Scala
// MAGIC * Collections in Scala
// MAGIC * Scala Closures for Functional Programming and MapReduce
// MAGIC 
// MAGIC **Remark**: You need to take a computer science course (from CourseEra, for example) to properly learn Scala.  Here, we will learn to use Scala by example to accomplish our data science tasks at hand.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Assignments 
// MAGIC ### value and variable as ``val`` and ``var``
// MAGIC 
// MAGIC Let us assign the integer value ``5`` to `x` as follows:

// COMMAND ----------

val x : Int = 5 // <Ctrl+Enter> to declare a value x to be integer 5

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

// MAGIC %md
// MAGIC ## Methods and Tab-completion

// COMMAND ----------

val s = "hi"    // <Ctrl+Enter> to declare val s to String "hi"

// COMMAND ----------

// MAGIC %md
// MAGIC You can place the cursor after ``.`` following a declared object and find out the methods available for it as shown in the image below.
// MAGIC 
// MAGIC ![tabCompletionAfterSDot PNG image](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/images/week1/tabCompletionAfterSDot.png)
// MAGIC 
// MAGIC **You Try** doing this next.

// COMMAND ----------

s.    // place cursor after the '.' and press Tab to see all available methods for s 

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

s    // <Shift-Enter> recall the value of String s

// COMMAND ----------

s.contains("f")     // <Shift-Enter> returns Boolean false since s does not contain the string "f"

// COMMAND ----------

s.contains("")    // <Shift-Enter> returns Boolean true since s contains the empty string ""

// COMMAND ----------

s.contains("i")    // <Ctrl+Enter> returns Boolean true since s contains the string "i"

// COMMAND ----------

// MAGIC %md
// MAGIC ## Functions

// COMMAND ----------

def square(x: Int): Int = x*x    // <Shitf+Enter> to define a function named square

// COMMAND ----------

square(5)    // <Shitf+Enter> to call this function on argument 5

// COMMAND ----------

y    // <Shitf+Enter> to recall that val y is Int 3

// COMMAND ----------

square(y) // <Shitf+Enter> to call the function on val y of the right argument type Int

// COMMAND ----------

x    // <Shitf+Enter> to recall x is Double 5.0

// COMMAND ----------

square(x) // <Shift+Enter> to call the function on val x of type Double will give type mismatch error

// COMMAND ----------

def square(x: Int): Int = { // <Shitf+Enter> to declare function in a block
  val answer=x*x
  answer // the last line of the function block is returned
}

// COMMAND ----------

square(5000)    // <Shift+Enter> to call the function

// COMMAND ----------

// <Shift+Enter> to define function with input and output type as String
def announceAndEmit(text: String) = 
{
  println(text)
  text // the last line of the function block is returned
}

// COMMAND ----------

// <Ctrl+Enter> to call function which prints as line and returns as String
announceAndEmit("roger roger")

// COMMAND ----------

// MAGIC %md
// MAGIC ## Scala Collections
// MAGIC 
// MAGIC See the [overview](http://docs.scala-lang.org/overviews/collections/overview) and [introduction](http://docs.scala-lang.org/overviews/collections/introduction) to scala collections, the building blocks of Spark.

// COMMAND ----------

// <Ctrl+Enter> to declare (an immutable) val lst as List of Int's 1,2,3
val lst = List(1, 2, 3)

// COMMAND ----------

// MAGIC %md
// MAGIC There are several other Scala collections and we will introduce them as needed.  The two other most common ones are ``Array`` and ``Seq``.

// COMMAND ----------

val arr = Array(1,2,3) // <Shift-Enter> to declare an Array

// COMMAND ----------

val seq = Seq(1,2,3)    // <Shift-Enter> to declare a Seq

// COMMAND ----------

// MAGIC %md
// MAGIC ##Scala Closures for Functional Programming and MapReduce
// MAGIC 
// MAGIC We will apply such *closures* for processing scala collections with functional programming.
// MAGIC 
// MAGIC ### Five ways of adding 1
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
// MAGIC 5. regular functions using ``def``:
// MAGIC ```%scala
// MAGIC def addOne(x: Int): Int = x + 1
// MAGIC  ```

// COMMAND ----------

// MAGIC %md
// MAGIC Now, let's apply closures for functional programming over scala collection (`List`) using `foreach`, `map`, `filter` and `reduce`. In the end we will write out first mapReduce program!

// COMMAND ----------

// <Shift+Enter> to call the foreach method and print its contents element-per-line using println function
lst.foreach(x => println(x))

// COMMAND ----------

// <Shift+Enter> for same output as above where println is applied to each element of List lst
lst.foreach(println)

// COMMAND ----------

// <Shift+Enter> to map each value x of lst with x+10 to return a new List(11, 12, 13)
lst.map(x => x + 10)  

// COMMAND ----------

// <Shift+Enter> for the same as above using place-holder syntax
lst.map(_ + 10)  

// COMMAND ----------

// <Shift+Enter> to return a new List(1, 3) after filtering x's from lst if (x % 2 == 1) is true
lst.filter(x => (x % 2 == 1) )

// COMMAND ----------

// <Shift+Enter> for the same as above using place-holder syntax
lst.filter( _ % 2 == 1 )

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

lst.map(x => x+10).reduce((x,y) => x+y) // <Ctrl-Enter> to get Int 36 = sum(1+10,2+10,3+10)

// COMMAND ----------

// MAGIC %md
// MAGIC There are lots of methods in Scala Collections. And much more in this *scalable language*.
// MAGIC See for example [http://docs.scala-lang.org/cheatsheets/index.html](http://docs.scala-lang.org/cheatsheets/index.html).

// COMMAND ----------

// MAGIC %md
// MAGIC ## Spark is written in Scala and the primary language for this course is Scala.
// MAGIC ### However, let us use the best language for the job!
// MAGIC 
// MAGIC ### Cells each have a type - **scala**, **python**, **r**, **sql**, **filesystem**, **command line** or **markdown**.
// MAGIC 
// MAGIC * While cells default to the type of the Notebook, other cell types are supported as well.
// MAGIC * For example, Python Notebooks can contain python, sql, markdown, and even Scala cells. This lets you write notebooks that do use multiple languages.
// MAGIC * This cell is in **markdown** and is used for documentation purposes.

// COMMAND ----------

// MAGIC %md
// MAGIC ### All types of cells can be created in any notebook, regardless of the language.
// MAGIC 
// MAGIC To create a cell of another language, start the cell with:
// MAGIC 
// MAGIC * `%md` - Markdown
// MAGIC * `%sql` - SQL
// MAGIC * `%scala` - Scala
// MAGIC * `%py` - Python
// MAGIC * `%r` - R

// COMMAND ----------

// MAGIC %md
// MAGIC ### Cross-language cells can be used to mix commands from other languages.
// MAGIC 
// MAGIC Examples:

// COMMAND ----------

// MAGIC %py 
// MAGIC print("For example, this is a scala notebook, but we can use %py to run python commands inline.")

// COMMAND ----------

// MAGIC %r 
// MAGIC print("We can also access other languages such as R.")

// COMMAND ----------

// MAGIC %md
// MAGIC ### Command line cells can be used to work with local files on the Spark driver node.
// MAGIC * Start a cell with `%sh` to run a command line command

// COMMAND ----------

// MAGIC %sh
// MAGIC # This is a command line cell. Commands you write here will be executed as if they were run on the command line.
// MAGIC # For example, in this cell we access the help pages for the bash shell.
// MAGIC man bash

// COMMAND ----------

// MAGIC %md
// MAGIC ### Filesystem cells allow access to the Databricks File System.
// MAGIC * Start a cell with `%fs` to run DBFS commands
// MAGIC * Type `%fs help` for a list of commands

// COMMAND ----------

// MAGIC %md
// MAGIC # Further Reference / Homework / Recurrrent Points of Reference
// MAGIC 
// MAGIC Please go here for a relaxed and detailed-enough tour (later):
// MAGIC 
// MAGIC * databricks
// MAGIC   * [https://docs.databricks.com/index.html](https://docs.databricks.com/index.html)
// MAGIC * scala
// MAGIC   * [http://docs.scala-lang.org/](http://docs.scala-lang.org/)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Notebooks can be run from other notebooks using **%run**
// MAGIC * Syntax: `%run /full/path/to/notebook`
// MAGIC * This is commonly used to import functions you defined in other notebooks.