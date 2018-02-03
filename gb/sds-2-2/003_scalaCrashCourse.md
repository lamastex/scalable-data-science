[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

**Notebooks**
=============

Write Spark code for processing your data in notebooks.

**NOTE**: You should have already cloned this notebook and attached it to a cluster that you started in the Community Edition of databricks by now.

### Notebooks can be written in **Python**, **Scala**, **R**, or **SQL**.

-   This is a Scala notebook - which is indicated next to the title above by `(Scala)`.

### **Creating a new Notebook**

![Change Name](http://training.databricks.com/databricks_guide/Notebook/createNotebook.png)

-   Click the tiangle on the right side of a folder to open the folder menu.
-   Select **Create &gt; Notebook**.
-   Enter the name of the notebook, the language (Python, Scala, R or SQL) for the notebook, and a cluster to run it on.

### \*\* Cloning a Notebook** \* You can clone a notebook to create a copy of it, for example if you want to edit or run an Example notebook like this one. \* Click **File &gt; Clone\*\* in the notebook context bar above.

-   Enter a new name and location for your notebook. If Access Control is enabled, you can only clone to folders that you have Manage permissions on.

**Introduction to Scala through Scala Notebook**
================================================

-   This introduction notebook describes how to get started running Scala code in Notebooks.

### Clone Or Import This Notebook

-   From the **File** menu at the top left of this notebook, choose **Clone** or click **Import Notebook** on the top right. This will allow you to interactively execute code cells as you proceed through the notebook.

![Menu Bar Clone Notebook](http://training.databricks.com/databricks_guide/2.8/clone.png) \* Enter a name and a desired location for your cloned notebook (i.e. Perhaps clone to your own user directory or the "Shared" directory.) \* Navigate to the location you selected (e.g. click Menu &gt; Workspace &gt; `Your cloned location`)

### **Attach** the Notebook to a **cluster**

-   A **Cluster** is a group of machines which can run commands in cells.
-   Check the upper left corner of your notebook to see if it is **Attached** or **Detached**.
-   If **Detached**, click on the right arrow and select a cluster to attach your notebook to.
-   If there is no running cluster, create one as described in the [Welcome to Databricks](/#workspace/databricks_guide/00%20Welcome%20to%20Databricks) guide.

![Attach Notebook](http://training.databricks.com/databricks_guide/2.8/detached.png)

------------------------------------------------------------------------

#### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Cells** are units that make up notebooks

![A Cell](http://training.databricks.com/databricks_guide/cell.png)

Cells each have a type - including **scala**, **python**, **sql**, **R**, **markdown**, **filesystem**, and **shell**. \* While cells default to the type of the Notebook, other cell types are supported as well. \* This cell is in **markdown** and is used for documentation. [Markdown](http://en.wikipedia.org/wiki/Markdown) is a simple text formatting syntax. \*\*\*

------------------------------------------------------------------------

### **Create** and **Edit** a New Markdown Cell in this Notebook

-   When you mouse between cells, a + sign will pop up in the center that you can click on to create a new cell.

![New Cell](http://training.databricks.com/databricks_guide/create_new_cell.png) \* Type **`%md Hello, world!`** into your new cell (**`%md`** indicates the cell is markdown).

-   Click out of the cell to see the cell contents update.

![Run cell](http://training.databricks.com/databricks_guide/run_cell.png) \*\*\*

### **Running a cell in your notebook.**

-   #### Press **Shift+Enter** when in the cell to **run** it and proceed to the next cell.

-   The cells contents should update. ![Run cell](http://training.databricks.com/databricks_guide/run_cell.png)
-   **NOTE:** Cells are not automatically run each time you open it.
-   Instead, Previous results from running a cell are saved and displayed.
-   #### Alternately, press **Ctrl+Enter** when in a cell to **run** it, but not proceed to the next cell.

<!-- -->

    Just double-click the cell below, modify the text following ``%md`` and press **Ctrl+Enter** to evaluate it and see it's mark-down'd output.
    ```
    > %md Hello, world!
    ```

------------------------------------------------------------------------

#### ![Quick Note](http://training.databricks.com/databricks_guide/icon_note3_s.png) **Markdown Cell Tips**

-   To change a non-markdown cell to markdown, add **%md** to very start of the cell.
-   After updating the contents of a markdown cell, click out of the cell to update the formatted contents of a markdown cell.
-   To edit an existing markdown cell, **doubleclick** the cell. \*\*\*

------------------------------------------------------------------------

### Run a **Scala Cell**

-   Run the following scala cell.
-   Note: There is no need for any special indicator (such as `%md`) necessary to create a Scala cell in a Scala notebook.
-   You know it is a scala notebook because of the `(Scala)` appended to the name of this notebook.
-   Make sure the cell contents updates before moving on.
-   Press **Shift+Enter** when in the cell to run it and proceed to the next cell.
-   The cells contents should update.
-   Alternately, press **Ctrl+Enter** when in a cell to **run** it, but not proceed to the next cell.
-   characters following `//` are comments in scala. \*\*\*

<!-- -->

    println(System.currentTimeMillis) // press Ctrl+Enter to evaluate println that prints its argument as a line

> 1456375909111

Scala Resources
---------------

You will not be learning scala systematically and thoroughly in this course. You will learn *to use* Scala by doing various Spark jobs.

If you are seriously interested in learning scala properly, then there are various resources, including:

-   [scala-lang.org](http://www.scala-lang.org/) is the **core Scala resource**.
-   [tour-of-scala](http://docs.scala-lang.org/tutorials/tour/tour-of-scala)
-   MOOC
-   [courseera: Functional Programming Principles in Scala](https://www.coursera.org/course/progfun)
-   [Books](http://www.scala-lang.org/documentation/books.html)
-   [Programming in Scala, 1st Edition, Free Online Reading](http://www.artima.com/pins1ed/)

The main sources for the following content are (you are encouraged to read them for more background):

-   [Martin Oderski's Scala by example](http://www.scala-lang.org/docu/files/ScalaByExample.pdf)
-   [Scala crash course by Holden Karau](http://lintool.github.io/SparkTutorial/slides/day1_Scala_crash_course.pdf)
-   [Darren's brief introduction to scala and breeze for statistical computing](https://darrenjw.wordpress.com/2013/12/30/brief-introduction-to-scala-and-breeze-for-statistical-computing/)

Introduction to Scala
=====================

What is Scala?
--------------

"Scala smoothly integrates object-oriented and functional programming. It is designed to express common programming patterns in a concise, elegant, and type-safe way." by Matrin Odersky.

-   High-level language for the Java Virtual Machine (JVM)
-   Object oriented + functional programming
-   Statically typed
-   Comparable in speed to Java
-   Type inference saves us from having to write explicit types most of the time Interoperates with Java
-   Can use any Java class (inherit from, etc.)
-   Can be called from Java code

Why Scala?
----------

-   Spark was originally written in Scala, which allows concise function syntax and interactive use
-   Spark APIs for other languages include:
-   Java API for standalone use
-   Python API added to reach a wider user community of programmes
-   R API added more recently to reach a wider community of data analyststs
-   Unfortunately, Python and R APIs are generally behind Spark's native Scala (for eg. GraphX is only available in Scala currently).
-   See Darren Wilkinson's 11 reasons for [scala as a platform for statistical computing and data science](https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/). It is embedded in-place below for your convenience.

<a href="https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/">https://darrenjw.wordpress.com/2013/12/23/scala-as-a-platform-for-statistical-computing-and-data-science/</a>

Let's get our hands dirty in Scala
==================================

We will go through the following programming concepts and tasks: \* Assignments \* Methods and Tab-completion \* Functions in Scala \* Collections in Scala \* Scala Closures for Functional Programming and MapReduce

**Remark**: You need to take a computer science course (from CourseEra, for example) to properly learn Scala. Here, we will learn to use Scala by example to accomplish our data science tasks at hand.

Assignments
-----------

### value and variable as `val` and `var`

Let us assign the integer value `5` to `x` as follows:

    val x : Int = 5 // <Ctrl+Enter> to declare a value x to be integer 5

> x: Int = 5

Scala is statically typed, but it uses built-in type inference machinery to automatically figure out that `x` is an integer or `Int` type as follows. Let's declare a value `x` to be `Int` 5 next without explictly using `Int`.

    val x = 5    // <Ctrl+Enter> to declare a value x as Int 5 (type automatically inferred)

> x: Int = 5

Let's declare `x` as a `Double` or double-precision floating-point type using decimal such as `5.0` (a digit has to follow the decimal point!)

    val x = 5.0   // <Ctrl+Enter> to declare a value x as Double 5

> x: Double = 5.0

Alternatively, we can assign `x` as a `Double` explicitly. Note that the decimal point is not needed in this case due to explicit typing as `Double`.

    val x :  Double = 5    // <Ctrl+Enter> to declare a value x as Double 5 (type automatically inferred)

> x: Double = 5.0

Next note that labels need to be declared on first use. We have declared x to be a `val` which is short for *value*. This makes `x` immutable (cannot be changed).

Thus, `x` cannot be just re-assigned, as the following code illustrates in the resulting error: `... error: reassignment to val`.

    //x = 10    //  uncomment and <Ctrl+Enter> to try to reassign val x to 10

Scala allows declaration of mutable variables as well using `var`, as follows:

    var y = 2    // <Shift+Enter> to declare a variable y to be integer 2 and go to next cell

> y: Int = 2

    y = 3    // <Shift+Enter> to change the value of y to 3

> y: Int = 3

Methods and Tab-completion
--------------------------

    val s = "hi"    // <Ctrl+Enter> to declare val s to String "hi"

> s: String = hi

You can place the cursor after `.` following a declared object and find out the methods available for it as shown in the image below.

![tabCompletionAfterSDot PNG image](https://github.com/raazesh-sainudiin/scalable-data-science/raw/master/images/week1/tabCompletionAfterSDot.png)

**You Try** doing this next.

    s.    // place cursor after the '.' and press Tab to see all available methods for s 

For example, \* scroll down to `contains` and double-click on it.
\* This should lead to `s.contains` in your cell. \* Now add an argument String to see if `s` contains the argument, for example, try: \* `s.contains("f")` \* `s.contains("")` and \* `s.contains("i")`

    s    // <Shift-Enter> recall the value of String s

> res5: String = hi

    s.contains("f")     // <Shift-Enter> returns Boolean false since s does not contain the string "f"

> res6: Boolean = false

    s.contains("")    // <Shift-Enter> returns Boolean true since s contains the empty string ""

> res7: Boolean = true

    s.contains("i")    // <Ctrl+Enter> returns Boolean true since s contains the string "i"

> res8: Boolean = true

Functions
---------

> frameIt: (u: String, h: Int)String

    def square(x: Int): Int = x*x    // <Shitf+Enter> to define a function named square

> square: (x: Int)Int

    square(5)    // <Shitf+Enter> to call this function on argument 5

> res9: Int = 25

    y    // <Shitf+Enter> to recall that val y is Int 3

> res10: Int = 3

    square(y) // <Shitf+Enter> to call the function on val y of the right argument type Int

> res11: Int = 9

    x    // <Shitf+Enter> to recall x is Double 5.0

    square(x) // <Shift+Enter> to call the function on val x of type Double will give type mismatch error

    def square(x: Int): Int = { // <Shitf+Enter> to declare function in a block
      val answer=x*x
      answer // the last line of the function block is returned
    }

> square: (x: Int)Int

    square(5000)    // <Shift+Enter> to call the function

> res12: Int = 25000000

    // <Shift+Enter> to define function with input and output type as String
    def announceAndEmit(text: String) = 
    {
      println(text)
      text // the last line of the function block is returned
    }

> announceAndEmit: (text: String)String

    // <Ctrl+Enter> to call function which prints as line and returns as String
    announceAndEmit("roger roger")

> roger roger res13: String = roger roger

Scala Collections
-----------------

See the [overview](http://docs.scala-lang.org/overviews/collections/overview) and [introduction](http://docs.scala-lang.org/overviews/collections/introduction) to scala collections, the building blocks of Spark.

    // <Ctrl+Enter> to declare (an immutable) val lst as List of Int's 1,2,3
    val lst = List(1, 2, 3)

> lst: List\[Int\] = List(1, 2, 3)

There are several other Scala collections and we will introduce them as needed. The two other most common ones are `Array` and `Seq`.

    val arr = Array(1,2,3) // <Shift-Enter> to declare an Array

> arr: Array\[Int\] = Array(1, 2, 3)

    val seq = Seq(1,2,3)    // <Shift-Enter> to declare a Seq

> seq: Seq\[Int\] = List(1, 2, 3)

Scala Closures for Functional Programming and MapReduce
-------------------------------------------------------

We will apply such *closures* for processing scala collections with functional programming.

### Five ways of adding 1

1.  explicit version:

    ``` %scala
    (x: Int) => x + 1  
    ```

2.  type-inferred more intuitive version:

    ``` %scala
    x => x + 1   
    ```

3.  placeholder syntax (each argument must be used exactly once):

    ``` %scala
    _ + 1 
    ```

4.  type-inferred more intuitive version with code-block for larger function body:

    ``` %scala
    x => { 
      // body is a block of code
      val integerToAdd = 1
      x + integerToAdd
    }
    ```

5.  regular functions using `def`: `%scala def addOne(x: Int): Int = x + 1`

Now, let's apply closures for functional programming over scala collection (`List`) using `foreach`, `map`, `filter` and `reduce`. In the end we will write out first mapReduce program!

    // <Shift+Enter> to call the foreach method and print its contents element-per-line using println function
    lst.foreach(x => println(x))

> 1 2 3

    // <Shift+Enter> for same output as above where println is applied to each element of List lst
    lst.foreach(println)

> 1 2 3

    // <Shift+Enter> to map each value x of lst with x+10 to return a new List(11, 12, 13)
    lst.map(x => x + 10)  

> res16: List\[Int\] = List(11, 12, 13)

    // <Shift+Enter> for the same as above using place-holder syntax
    lst.map(_ + 10)  

> res17: List\[Int\] = List(11, 12, 13)

    // <Shift+Enter> to return a new List(1, 3) after filtering x's from lst if (x % 2 == 1) is true
    lst.filter(x => (x % 2 == 1) )

> res18: List\[Int\] = List(1, 3)

    // <Shift+Enter> for the same as above using place-holder syntax
    lst.filter( _ % 2 == 1 )

> res19: List\[Int\] = List(1, 3)

    // <Shift+Enter> to use reduce to add elements of lst two at a time to return Int 6
    lst.reduce( (x, y) => x + y )

> res20: Int = 6

    // <Ctrl+Enter> for the same as above but using place-holder syntax
    lst.reduce( _ + _ )

> res21: Int = 6

Let's combine `map` and `reduce` programs above to find the sum of after 10 has been added to every element of the original List `lst` as follows:

    lst.map(x => x+10).reduce((x,y) => x+y) // <Ctrl-Enter> to get Int 36 = sum(1+10,2+10,3+10)

> res23: Int = 36

There are lots of methods in Scala Collections. And much more in this *scalable language*. See for example <http://docs.scala-lang.org/cheatsheets/index.html>.

Spark is written in Scala and the primary language for this course is Scala.
----------------------------------------------------------------------------

### However, let us use the best language for the job!

### Cells each have a type - **scala**, **python**, **r**, **sql**, **filesystem**, **command line** or **markdown**.

-   While cells default to the type of the Notebook, other cell types are supported as well.
-   For example, Python Notebooks can contain python, sql, markdown, and even Scala cells. This lets you write notebooks that do use multiple languages.
-   This cell is in **markdown** and is used for documentation purposes.

### All types of cells can be created in any notebook, regardless of the language.

To create a cell of another language, start the cell with: \* `%md` - Markdown \* `%sql` - SQL \* `%scala` - Scala \* `%py` - Python \* `%r` - R

### Cross-language cells can be used to mix commands from other languages.

Examples:

> For example, this is a scala notebook, but we can use %py to run python commands inline.

> ```
> ```
>
> ```
> [1] "We can also access other languages such as R."
> ```

### Command line cells can be used to work with local files on the Spark driver node.

-   Start a cell with `%sh` to run a command line command

<!-- -->

    # This is a command line cell. Commands you write here will be executed as if they were run on the command line.
    # For example, in this cell we access the help pages for the bash shell.
    man bash

> BASH(1) General Commands Manual BASH(1) NAME bash - GNU Bourne-Again SHell SYNOPSIS bash \[options\] \[command\_string | file\] COPYRIGHT Bash is Copyright (C) 1989-2013 by the Free Software Foundation, Inc. DESCRIPTION Bash is an sh-compatible command language interpreter that executes commands read from the standard input or from a file. Bash also incor porates useful features from the Korn and C shells (ksh and csh). Bash is intended to be a conformant implementation of the Shell and Utilities portion of the IEEE POSIX specification (IEEE Standard 1003.1). Bash can be configured to be POSIX-conformant by default. OPTIONS All of the single-character shell options documented in the descrip tion of the set builtin command can be used as options when the shell is invoked. In addition, bash interprets the following options when it is invoked: -c If the -c option is present, then commands are read from the first non-option argument command\_string. If there are argu ments after the command\_string, they are assigned to the positional parameters, starting with $0. -i If the -i option is present, the shell is interactive. -l Make bash act as if it had been invoked as a login shell (see INVOCATION below). -r If the -r option is present, the shell becomes restricted (see RESTRICTED SHELL below). -s If the -s option is present, or if no arguments remain after option processing, then commands are read from the standard input. This option allows the positional parameters to be set when invoking an interactive shell. -D A list of all double-quoted strings preceded by $ is printed on the standard output. These are the strings that are sub ject to language translation when the current locale is not C or POSIX. This implies the -n option; no commands will be executed. \[-+\]O \[shopt\_option\] shopt\_option is one of the shell options accepted by the shopt builtin (see SHELL BUILTIN COMMANDS below). If shopt\_option is present, -O sets the value of that option; +O unsets it. If shopt\_option is not supplied, the names and values of the shell options accepted by shopt are printed on the standard output. If the invocation option is +O, the output is displayed in a format that may be reused as input. -- A -- signals the end of options and disables further option processing. Any arguments after the -- are treated as file names and arguments. An argument of - is equivalent to --. Bash also interprets a number of multi-character options. These options must appear on the command line before the single-character options to be recognized. --debugger Arrange for the debugger profile to be executed before the shell starts. Turns on extended debugging mode (see the description of the extdebug option to the shopt builtin below). --dump-po-strings Equivalent to -D, but the output is in the GNU gettext po (por table object) file format. --dump-strings Equivalent to -D. --help Display a usage message on standard output and exit success fully. --init-file file --rcfile file Execute commands from file instead of the system wide initial ization file /etc/bash.bashrc and the standard personal initial ization file ~/.bashrc if the shell is interactive (see INVOCA TION below). --login Equivalent to -l. --noediting Do not use the GNU readline library to read command lines when the shell is interactive. --noprofile Do not read either the system-wide startup file /etc/profile or any of the personal initialization files ~/.bash\_profile, ~/.bash\_login, or ~/.profile. By default, bash reads these files when it is invoked as a login shell (see INVOCATION below). --norc Do not read and execute the system wide initialization file /etc/bash.bashrc and the personal initialization file ~/.bashrc if the shell is interactive. This option is on by default if the shell is invoked as sh. --posix Change the behavior of bash where the default operation differs from the POSIX standard to match the standard (posix mode). See SEE ALSO below for a reference to a document that details how posix mode affects bash's behavior. --restricted The shell becomes restricted (see RESTRICTED SHELL below). --verbose Equivalent to -v. --version Show version information for this instance of bash on the stan dard output and exit successfully. ARGUMENTS If arguments remain after option processing, and neither the -c nor the -s option has been supplied, the first argument is assumed to be the name of a file containing shell commands. If bash is invoked in this fashion, $0 is set to the name of the file, and the positional parame ters are set to the remaining arguments. Bash reads and executes com mands from this file, then exits. Bash's exit status is the exit sta tus of the last command executed in the script. If no commands are executed, the exit status is 0. An attempt is first made to open the file in the current directory, and, if no file is found, then the shell searches the directories in PATH for the script. INVOCATION A login shell is one whose first character of argument zero is a -, or one started with the --login option. An interactive shell is one started without non-option arguments and without the -c option whose standard input and error are both connected to terminals (as determined by isatty(3)), or one started with the -i option. PS1 is set and $- includes i if bash is interactive, allowing a shell script or a startup file to test this state. The following paragraphs describe how bash executes its startup files. If any of the files exist but cannot be read, bash reports an error. Tildes are expanded in filenames as described below under Tilde Expan sion in the EXPANSION section. When bash is invoked as an interactive login shell, or as a non-inter active shell with the --login option, it first reads and executes com mands from the file /etc/profile, if that file exists. After reading that file, it looks for ~/.bash\_profile, ~/.bash\_login, and ~/.profile, in that order, and reads and executes commands from the first one that exists and is readable. The --noprofile option may be used when the shell is started to inhibit this behavior. When a login shell exits, bash reads and executes commands from the file ~/.bash\_logout, if it exists. When an interactive shell that is not a login shell is started, bash reads and executes commands from /etc/bash.bashrc and ~/.bashrc, if these files exist. This may be inhibited by using the --norc option. The --rcfile file option will force bash to read and execute commands from file instead of /etc/bash.bashrc and ~/.bashrc. When bash is started non-interactively, to run a shell script, for example, it looks for the variable BASH\_ENV in the environment, expands its value if it appears there, and uses the expanded value as the name of a file to read and execute. Bash behaves as if the following com mand were executed: if \[ -n "$BASH\_ENV" \]; then . "$BASH\_ENV"; fi but the value of the PATH variable is not used to search for the file name. If bash is invoked with the name sh, it tries to mimic the startup behavior of historical versions of sh as closely as possible, while conforming to the POSIX standard as well. When invoked as an interac tive login shell, or a non-interactive shell with the --login option, it first attempts to read and execute commands from /etc/profile and ~/.profile, in that order. The --noprofile option may be used to inhibit this behavior. When invoked as an interactive shell with the name sh, bash looks for the variable ENV, expands its value if it is defined, and uses the expanded value as the name of a file to read and execute. Since a shell invoked as sh does not attempt to read and exe cute commands from any other startup files, the --rcfile option has no effect. A non-interactive shell invoked with the name sh does not attempt to read any other startup files. When invoked as sh, bash enters posix mode after the startup files are read. When bash is started in posix mode, as with the --posix command line option, it follows the POSIX standard for startup files. In this mode, interactive shells expand the ENV variable and commands are read and executed from the file whose name is the expanded value. No other startup files are read. Bash attempts to determine when it is being run with its standard input connected to a network connection, as when executed by the remote shell daemon, usually rshd, or the secure shell daemon sshd. If bash deter mines it is being run in this fashion, it reads and executes commands from ~/.bashrc and ~/.bashrc, if these files exist and are readable. It will not do this if invoked as sh. The --norc option may be used to inhibit this behavior, and the --rcfile option may be used to force another file to be read, but neither rshd nor sshd generally invoke the shell with those options or allow them to be specified. If the shell is started with the effective user (group) id not equal to the real user (group) id, and the -p option is not supplied, no startup files are read, shell functions are not inherited from the environment, the SHELLOPTS, BASHOPTS, CDPATH, and GLOBIGNORE variables, if they appear in the environment, are ignored, and the effective user id is set to the real user id. If the -p option is supplied at invocation, the startup behavior is the same, but the effective user id is not reset. DEFINITIONS The following definitions are used throughout the rest of this docu ment. blank A space or tab. word A sequence of characters considered as a single unit by the shell. Also known as a token. name A word consisting only of alphanumeric characters and under scores, and beginning with an alphabetic character or an under score. Also referred to as an identifier. metacharacter A character that, when unquoted, separates words. One of the following: | & ; ( ) &lt; &gt; space tab control operator A token that performs a control function. It is one of the fol lowing symbols: || & && ; ;; ( ) | |& &lt;newline&gt; RESERVED WORDS Reserved words are words that have a special meaning to the shell. The following words are recognized as reserved when unquoted and either the first word of a simple command (see SHELL GRAMMAR below) or the third word of a case or for command: ! case coproc do done elif else esac fi for function if in select then until while { } time \[\[ \]\] SHELL GRAMMAR Simple Commands A simple command is a sequence of optional variable assignments fol lowed by blank-separated words and redirections, and terminated by a control operator. The first word specifies the command to be executed, and is passed as argument zero. The remaining words are passed as arguments to the invoked command. The return value of a simple command is its exit status, or 128+n if the command is terminated by signal n. Pipelines A pipeline is a sequence of one or more commands separated by one of the control operators | or |&. The format for a pipeline is: \[time \[-p\]\] \[ ! \] command \[ \[|�|&\] command2 ... \] The standard output of command is connected via a pipe to the standard input of command2. This connection is performed before any redirec tions specified by the command (see REDIRECTION below). If |& is used, command's standard error, in addition to its standard output, is con nected to command2's standard input through the pipe; it is shorthand for 2&gt;&1 |. This implicit redirection of the standard error to the standard output is performed after any redirections specified by the command. The return status of a pipeline is the exit status of the last command, unless the pipefail option is enabled. If pipefail is enabled, the pipeline's return status is the value of the last (rightmost) command to exit with a non-zero status, or zero if all commands exit success fully. If the reserved word ! precedes a pipeline, the exit status of that pipeline is the logical negation of the exit status as described above. The shell waits for all commands in the pipeline to terminate before returning a value. If the time reserved word precedes a pipeline, the elapsed as well as user and system time consumed by its execution are reported when the pipeline terminates. The -p option changes the output format to that specified by POSIX. When the shell is in posix mode, it does not rec ognize time as a reserved word if the next token begins with a \`-'. The TIMEFORMAT variable may be set to a format string that specifies how the timing information should be displayed; see the description of TIMEFORMAT under Shell Variables below. When the shell is in posix mode, time may be followed by a newline. In this case, the shell displays the total user and system time consumed by the shell and its children. The TIMEFORMAT variable may be used to specify the format of the time information. Each command in a pipeline is executed as a separate process (i.e., in a subshell). Lists A list is a sequence of one or more pipelines separated by one of the operators ;, &, &&, or ||, and optionally terminated by one of ;, &, or &lt;newline&gt;. Of these list operators, && and || have equal precedence, followed by ; and &, which have equal precedence. A sequence of one or more newlines may appear in a list instead of a semicolon to delimit commands. If a command is terminated by the control operator &, the shell exe cutes the command in the background in a subshell. The shell does not wait for the command to finish, and the return status is 0. Commands separated by a ; are executed sequentially; the shell waits for each command to terminate in turn. The return status is the exit status of the last command executed. AND and OR lists are sequences of one of more pipelines separated by the && and || control operators, respectively. AND and OR lists are executed with left associativity. An AND list has the form command1 && command2 command2 is executed if, and only if, command1 returns an exit status of zero. An OR list has the form command1 || command2 command2 is executed if and only if command1 returns a non-zero exit status. The return status of AND and OR lists is the exit status of the last command executed in the list. Compound Commands A compound command is one of the following. In most cases a list in a command's description may be separated from the rest of the command by one or more newlines, and may be followed by a newline in place of a semicolon. (list) list is executed in a subshell environment (see COMMAND EXECU TION ENVIRONMENT below). Variable assignments and builtin com mands that affect the shell's environment do not remain in effect after the command completes. The return status is the exit status of list. { list; } list is simply executed in the current shell environment. list must be terminated with a newline or semicolon. This is known as a group command. The return status is the exit status of list. Note that unlike the metacharacters ( and ), { and } are reserved words and must occur where a reserved word is permitted to be recognized. Since they do not cause a word break, they must be separated from list by whitespace or another shell metacharacter. ((expression)) The expression is evaluated according to the rules described below under ARITHMETIC EVALUATION. If the value of the expres sion is non-zero, the return status is 0; otherwise the return status is 1. This is exactly equivalent to let "expression". \[\[ expression \]\] Return a status of 0 or 1 depending on the evaluation of the conditional expression expression. Expressions are composed of the primaries described below under CONDITIONAL EXPRESSIONS. Word splitting and pathname expansion are not performed on the words between the \[\[ and \]\]; tilde expansion, parameter and variable expansion, arithmetic expansion, command substitution, process substitution, and quote removal are performed. Condi tional operators such as -f must be unquoted to be recognized as primaries. When used with \[\[, the &lt; and &gt; operators sort lexicographically using the current locale. See the description of the test builtin command (section SHELL BUILTIN COMMANDS below) for the handling of parameters (i.e. missing parame ters). When the == and != operators are used, the string to the right of the operator is considered a pattern and matched according to the rules described below under Pattern Matching, as if the extglob shell option were enabled. The = operator is equivalent to ==. If the shell option nocasematch is enabled, the match is performed without regard to the case of alphabetic characters. The return value is 0 if the string matches (==) or does not match (!=) the pattern, and 1 otherwise. Any part of the pattern may be quoted to force the quoted portion to be matched as a string. An additional binary operator, =~, is available, with the same prece dence as == and !=. When it is used, the string to the right of the operator is considered an extended regular expression and matched accordingly (as in regex(3)). The return value is 0 if the string matches the pattern, and 1 otherwise. If the regular expression is syntactically incorrect, the conditional expression's return value is 2. If the shell option nocasematch is enabled, the match is performed without regard to the case of alphabetic characters. Any part of the pattern may be quoted to force the quoted portion to be matched as a string. Bracket expressions in regular expressions must be treated carefully, since normal quoting characters lose their meanings between brackets. If the pattern is stored in a shell variable, quoting the variable expansion forces the entire pattern to be matched as a string. Substrings matched by parenthesized subexpressions within the regular expression are saved in the array variable BASH\_REMATCH. The element of BASH\_REMATCH with index 0 is the portion of the string matching the entire regular expression. The element of BASH\_REMATCH with index n is the portion of the string matching the nth parenthesized subexpression. Expressions may be combined using the following operators, listed in decreasing order of precedence: ( expression ) Returns the value of expression. This may be used to override the normal precedence of operators. ! expression True if expression is false. expression1 && expression2 True if both expression1 and expression2 are true. expression1 || expression2 True if either expression1 or expression2 is true. The && and || operators do not evaluate expression2 if the value of expression1 is sufficient to determine the return value of the entire conditional expression. for name \[ \[ in \[ word ... \] \] ; \] do list ; done The list of words following in is expanded, generating a list of items. The variable name is set to each element of this list in turn, and list is executed each time. If the in word is omit ted, the for command executes list once for each positional parameter that is set (see PARAMETERS below). The return status is the exit status of the last command that executes. If the expansion of the items following in results in an empty list, no commands are executed, and the return status is 0. for (( expr1 ; expr2 ; expr3 )) ; do list ; done First, the arithmetic expression expr1 is evaluated according to the rules described below under ARITHMETIC EVALUATION. The arithmetic expression expr2 is then evaluated repeatedly until it evaluates to zero. Each time expr2 evaluates to a non-zero value, list is executed and the arithmetic expression expr3 is evaluated. If any expression is omitted, it behaves as if it evaluates to 1. The return value is the exit status of the last command in list that is executed, or false if any of the expres sions is invalid. select name \[ in word \] ; do list ; done The list of words following in is expanded, generating a list of items. The set of expanded words is printed on the standard error, each preceded by a number. If the in word is omitted, the positional parameters are printed (see PARAMETERS below). The PS3 prompt is then displayed and a line read from the stan dard input. If the line consists of a number corresponding to one of the displayed words, then the value of name is set to that word. If the line is empty, the words and prompt are dis played again. If EOF is read, the command completes. Any other value read causes name to be set to null. The line read is saved in the variable REPLY. The list is executed after each selection until a break command is executed. The exit status of select is the exit status of the last command executed in list, or zero if no commands were executed. case word in \[ \[(\] pattern \[ | pattern \] ... ) list ;; \] ... esac A case command first expands word, and tries to match it against \*\*\* WARNING: skipped 289459 bytes of output \*\*\* opportunity to re-edit a failed history substitution. histverify If set, and readline is being used, the results of his tory substitution are not immediately passed to the shell parser. Instead, the resulting line is loaded into the readline editing buffer, allowing further modi fication. hostcomplete If set, and readline is being used, bash will attempt to perform hostname completion when a word containing a @ is being completed (see Completing under READLINE above). This is enabled by default. huponexit If set, bash will send SIGHUP to all jobs when an inter active login shell exits. interactive\_comments If set, allow a word beginning with \# to cause that word and all remaining characters on that line to be ignored in an interactive shell (see COMMENTS above). This option is enabled by default. lastpipe If set, and job control is not active, the shell runs the last command of a pipeline not executed in the back ground in the current shell environment. lithist If set, and the cmdhist option is enabled, multi-line commands are saved to the history with embedded newlines rather than using semicolon separators where possible. login\_shell The shell sets this option if it is started as a login shell (see INVOCATION above). The value may not be changed. mailwarn If set, and a file that bash is checking for mail has been accessed since the last time it was checked, the message \`\`The mail in mailfile has been read'' is dis played. no\_empty\_cmd\_completion If set, and readline is being used, bash will not attempt to search the PATH for possible completions when completion is attempted on an empty line. nocaseglob If set, bash matches filenames in a case-insensitive fashion when performing pathname expansion (see Pathname Expansion above). nocasematch If set, bash matches patterns in a case-insensitive fashion when performing matching while executing case or \[\[ conditional commands. nullglob If set, bash allows patterns which match no files (see Pathname Expansion above) to expand to a null string, rather than themselves. progcomp If set, the programmable completion facilities (see Pro grammable Completion above) are enabled. This option is enabled by default. promptvars If set, prompt strings undergo parameter expansion, com mand substitution, arithmetic expansion, and quote removal after being expanded as described in PROMPTING above. This option is enabled by default. restricted\_shell The shell sets this option if it is started in restricted mode (see RESTRICTED SHELL below). The value may not be changed. This is not reset when the startup files are executed, allowing the startup files to dis cover whether or not a shell is restricted. shift\_verbose If set, the shift builtin prints an error message when the shift count exceeds the number of positional parame ters. sourcepath If set, the source (.) builtin uses the value of PATH to find the directory containing the file supplied as an argument. This option is enabled by default. xpg\_echo If set, the echo builtin expands backslash-escape sequences by default. suspend \[-f\] Suspend the execution of this shell until it receives a SIGCONT signal. A login shell cannot be suspended; the -f option can be used to override this and force the suspension. The return sta tus is 0 unless the shell is a login shell and -f is not sup plied, or if job control is not enabled. test expr \[ expr \] Return a status of 0 (true) or 1 (false) depending on the evalu ation of the conditional expression expr. Each operator and op erand must be a separate argument. Expressions are composed of the primaries described above under CONDITIONAL EXPRESSIONS. test does not accept any options, nor does it accept and ignore an argument of -- as signifying the end of options. Expressions may be combined using the following operators, listed in decreasing order of precedence. The evaluation depends on the number of arguments; see below. Operator prece dence is used when there are five or more arguments. ! expr True if expr is false. ( expr ) Returns the value of expr. This may be used to override the normal precedence of operators. expr1 -a expr2 True if both expr1 and expr2 are true. expr1 -o expr2 True if either expr1 or expr2 is true. test and \[ evaluate conditional expressions using a set of rules based on the number of arguments. 0 arguments The expression is false. 1 argument The expression is true if and only if the argument is not null. 2 arguments If the first argument is !, the expression is true if and only if the second argument is null. If the first argu ment is one of the unary conditional operators listed above under CONDITIONAL EXPRESSIONS, the expression is true if the unary test is true. If the first argument is not a valid unary conditional operator, the expression is false. 3 arguments The following conditions are applied in the order listed. If the second argument is one of the binary conditional operators listed above under CONDITIONAL EXPRESSIONS, the result of the expression is the result of the binary test using the first and third arguments as operands. The -a and -o operators are considered binary operators when there are three arguments. If the first argument is !, the value is the negation of the two-argument test using the second and third arguments. If the first argument is exactly ( and the third argument is exactly ), the result is the one-argument test of the second argument. Other wise, the expression is false. 4 arguments If the first argument is !, the result is the negation of the three-argument expression composed of the remaining arguments. Otherwise, the expression is parsed and eval uated according to precedence using the rules listed above. 5 or more arguments The expression is parsed and evaluated according to precedence using the rules listed above. When used with test or \[, the &lt; and &gt; operators sort lexico graphically using ASCII ordering. times Print the accumulated user and system times for the shell and for processes run from the shell. The return status is 0. trap \[-lp\] \[\[arg\] sigspec ...\] The command arg is to be read and executed when the shell receives signal(s) sigspec. If arg is absent (and there is a single sigspec) or -, each specified signal is reset to its original disposition (the value it had upon entrance to the shell). If arg is the null string the signal specified by each sigspec is ignored by the shell and by the commands it invokes. If arg is not present and -p has been supplied, then the trap commands associated with each sigspec are displayed. If no arguments are supplied or if only -p is given, trap prints the list of commands associated with each signal. The -l option causes the shell to print a list of signal names and their cor responding numbers. Each sigspec is either a signal name defined in &lt;signal.h&gt;, or a signal number. Signal names are case insensitive and the SIG prefix is optional. If a sigspec is EXIT (0) the command arg is executed on exit from the shell. If a sigspec is DEBUG, the command arg is exe cuted before every simple command, for command, case command, select command, every arithmetic for command, and before the first command executes in a shell function (see SHELL GRAMMAR above). Refer to the description of the extdebug option to the shopt builtin for details of its effect on the DEBUG trap. If a sigspec is RETURN, the command arg is executed each time a shell function or a script executed with the . or source builtins fin ishes executing. If a sigspec is ERR, the command arg is executed whenever a a pipeline (which may consist of a single simple command), a list, or a compound command returns a non-zero exit status, subject to the following conditions. The ERR trap is not executed if the failed command is part of the command list immediately following a while or until keyword, part of the test in an if statement, part of a command executed in a && or || list except the command following the final && or ||, any command in a pipeline but the last, or if the command's return value is being inverted using !. These are the same conditions obeyed by the errexit (-e) option. Signals ignored upon entry to the shell cannot be trapped or reset. Trapped signals that are not being ignored are reset to their original values in a subshell or subshell environment when one is created. The return status is false if any sigspec is invalid; otherwise trap returns true. type \[-aftpP\] name \[name ...\] With no options, indicate how each name would be interpreted if used as a command name. If the -t option is used, type prints a string which is one of alias, keyword, function, builtin, or file if name is an alias, shell reserved word, function, builtin, or disk file, respectively. If the name is not found, then nothing is printed, and an exit status of false is returned. If the -p option is used, type either returns the name of the disk file that would be executed if name were speci fied as a command name, or nothing if \`\`type -t name'' would not return file. The -P option forces a PATH search for each name, even if \`\`type -t name'' would not return file. If a command is hashed, -p and -P print the hashed value, which is not necessar ily the file that appears first in PATH. If the -a option is used, type prints all of the places that contain an executable named name. This includes aliases and functions, if and only if the -p option is not also used. The table of hashed commands is not consulted when using -a. The -f option suppresses shell function lookup, as with the command builtin. type returns true if all of the arguments are found, false if any are not found. ulimit \[-HSTabcdefilmnpqrstuvx \[limit\]\] Provides control over the resources available to the shell and to processes started by it, on systems that allow such control. The -H and -S options specify that the hard or soft limit is set for the given resource. A hard limit cannot be increased by a non-root user once it is set; a soft limit may be increased up to the value of the hard limit. If neither -H nor -S is speci fied, both the soft and hard limits are set. The value of limit can be a number in the unit specified for the resource or one of the special values hard, soft, or unlimited, which stand for the current hard limit, the current soft limit, and no limit, respectively. If limit is omitted, the current value of the soft limit of the resource is printed, unless the -H option is given. When more than one resource is specified, the limit name and unit are printed before the value. Other options are inter preted as follows: -a All current limits are reported -b The maximum socket buffer size -c The maximum size of core files created -d The maximum size of a process's data segment -e The maximum scheduling priority ("nice") -f The maximum size of files written by the shell and its children -i The maximum number of pending signals -l The maximum size that may be locked into memory -m The maximum resident set size (many systems do not honor this limit) -n The maximum number of open file descriptors (most systems do not allow this value to be set) -p The pipe size in 512-byte blocks (this may not be set) -q The maximum number of bytes in POSIX message queues -r The maximum real-time scheduling priority -s The maximum stack size -t The maximum amount of cpu time in seconds -u The maximum number of processes available to a single user -v The maximum amount of virtual memory available to the shell and, on some systems, to its children -x The maximum number of file locks -T The maximum number of threads If limit is given, and the -a option is not used, limit is the new value of the specified resource. If no option is given, then -f is assumed. Values are in 1024-byte increments, except for -t, which is in seconds; -p, which is in units of 512-byte blocks; and -T, -b, -n, and -u, which are unscaled values. The return status is 0 unless an invalid option or argument is sup plied, or an error occurs while setting a new limit. umask \[-p\] \[-S\] \[mode\] The user file-creation mask is set to mode. If mode begins with a digit, it is interpreted as an octal number; otherwise it is interpreted as a symbolic mode mask similar to that accepted by chmod(1). If mode is omitted, the current value of the mask is printed. The -S option causes the mask to be printed in sym bolic form; the default output is an octal number. If the -p option is supplied, and mode is omitted, the output is in a form that may be reused as input. The return status is 0 if the mode was successfully changed or if no mode argument was supplied, and false otherwise. unalias \[-a\] \[name ...\] Remove each name from the list of defined aliases. If -a is supplied, all alias definitions are removed. The return value is true unless a supplied name is not a defined alias. unset \[-fv\] \[-n\] \[name ...\] For each name, remove the corresponding variable or function. If the -v option is given, each name refers to a shell variable, and that variable is removed. Read-only variables may not be unset. If -f is specified, each name refers to a shell func tion, and the function definition is removed. If the -n option is supplied, and name is a variable with the nameref attribute, name will be unset rather than the variable it references. -n has no effect if the -f option is supplied. If no options are supplied, each name refers to a variable; if there is no vari able by that name, any function with that name is unset. Each unset variable or function is removed from the environment passed to subsequent commands. If any of COMP\_WORDBREAKS, RAN DOM, SECONDS, LINENO, HISTCMD, FUNCNAME, GROUPS, or DIRSTACK are unset, they lose their special properties, even if they are sub sequently reset. The exit status is true unless a name is read only. wait \[-n\] \[n ...\] Wait for each specified child process and return its termination status. Each n may be a process ID or a job specification; if a job spec is given, all processes in that job's pipeline are waited for. If n is not given, all currently active child pro cesses are waited for, and the return status is zero. If the -n option is supplied, wait waits for any job to terminate and returns its exit status. If n specifies a non-existent process or job, the return status is 127. Otherwise, the return status is the exit status of the last process or job waited for. RESTRICTED SHELL If bash is started with the name rbash, or the -r option is supplied at invocation, the shell becomes restricted. A restricted shell is used to set up an environment more controlled than the standard shell. It behaves identically to bash with the exception that the following are disallowed or not performed: � changing directories with cd � setting or unsetting the values of SHELL, PATH, ENV, or BASH\_ENV � specifying command names containing / � specifying a filename containing a / as an argument to the . builtin command � specifying a filename containing a slash as an argument to the -p option to the hash builtin command � importing function definitions from the shell environment at startup � parsing the value of SHELLOPTS from the shell environment at startup � redirecting output using the &gt;, &gt;|, &lt;&gt;, &gt;&, &&gt;, and &gt;&gt; redirect ion operators � using the exec builtin command to replace the shell with another command � adding or deleting builtin commands with the -f and -d options to the enable builtin command � using the enable builtin command to enable disabled shell builtins � specifying the -p option to the command builtin command � turning off restricted mode with set +r or set +o restricted. These restrictions are enforced after any startup files are read. When a command that is found to be a shell script is executed (see COM MAND EXECUTION above), rbash turns off any restrictions in the shell spawned to execute the script. SEE ALSO Bash Reference Manual, Brian Fox and Chet Ramey The Gnu Readline Library, Brian Fox and Chet Ramey The Gnu History Library, Brian Fox and Chet Ramey Portable Operating System Interface (POSIX) Part 2: Shell and Utili ties, IEEE -- http://pubs.opengroup.org/onlinepubs/9699919799/ http://tiswww.case.edu/~chet/bash/POSIX -- a description of posix mode sh(1), ksh(1), csh(1) emacs(1), vi(1) readline(3) FILES /bin/bash The bash executable /etc/profile The systemwide initialization file, executed for login shells /etc/bash.bashrc The systemwide per-interactive-shell startup file /etc/bash.bash.logout The systemwide login shell cleanup file, executed when a login shell exits ~/.bash\_profile The personal initialization file, executed for login shells ~/.bashrc The individual per-interactive-shell startup file ~/.bash\_logout The individual login shell cleanup file, executed when a login shell exits ~/.inputrc Individual readline initialization file AUTHORS Brian Fox, Free Software Foundation bfox@gnu.org Chet Ramey, Case Western Reserve University chet.ramey@case.edu BUG REPORTS If you find a bug in bash, you should report it. But first, you should make sure that it really is a bug, and that it appears in the latest version of bash. The latest version is always available from ftp://ftp.gnu.org/pub/gnu/bash/. Once you have determined that a bug actually exists, use the bashbug command to submit a bug report. If you have a fix, you are encouraged to mail that as well! Suggestions and \`philosophical' bug reports may be mailed to bug-bash@gnu.org or posted to the Usenet newsgroup gnu.bash.bug. ALL bug reports should include: The version number of bash The hardware and operating system The compiler used to compile A description of the bug behaviour A short script or \`recipe' which exercises the bug bashbug inserts the first three items automatically into the template it provides for filing a bug report. Comments and bug reports concerning this manual page should be directed to chet.ramey@case.edu. BUGS It's too big and too slow. There are some subtle differences between bash and traditional versions of sh, mostly because of the POSIX specification. Aliases are confusing in some uses. Shell builtin commands and functions are not stoppable/restartable. Compound commands and command sequences of the form \`a ; b ; c' are not handled gracefully when process suspension is attempted. When a process is stopped, the shell immediately executes the next command in the sequence. It suffices to place the sequence of commands between parentheses to force it into a subshell, which may be stopped as a unit. Array variables may not (yet) be exported. There may be only one active coprocess at a time. GNU Bash 4.3 2014 February 2 BASH(1)

### Filesystem cells allow access to the Databricks File System.

-   Start a cell with `%fs` to run DBFS commands
-   Type `%fs help` for a list of commands

Further Reference / Homework / Recurrrent Points of Reference
=============================================================

Please go here for a relaxed and detailed-enough tour (later): \* databricks \* <https://docs.databricks.com/index.html> \* scala \* <http://docs.scala-lang.org/>

### Notebooks can be run from other notebooks using **%run**

-   Syntax: `%run /full/path/to/notebook`
-   This is commonly used to import functions you defined in other notebooks.

<!-- -->

    // just see the guide for the introductory notebooks
    //%run "/databricks_guide/00 Welcome to Databricks" // running this cell will load databricks_guide/00 Welcome to Databricks notebook here

This is *slightly* outdated, but good enough for our purposes as we will do it live on the latest databricks notebooks.

Please go here for a relaxed and detailed-enough tour (later): \* <https://docs.databricks.com/index.html>