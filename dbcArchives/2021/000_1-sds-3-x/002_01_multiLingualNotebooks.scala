// Databricks notebook source
// MAGIC %md
// MAGIC # [ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)

// COMMAND ----------

// MAGIC %md
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
// MAGIC 
// MAGIC * While cells default to the type of the Notebook, other cell types are supported as well.
// MAGIC * This cell is in **markdown** and is used for documentation. [Markdown](http://en.wikipedia.org/wiki/Markdown) is a simple text formatting syntax.
// MAGIC 
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
// MAGIC Hello, world!

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
// MAGIC 
// MAGIC * To change a non-markdown cell to markdown, add **%md** to very start of the cell.
// MAGIC * After updating the contents of a markdown cell, click out of the cell to update the formatted contents of a markdown cell.
// MAGIC * To edit an existing markdown cell, **doubleclick** the cell.
// MAGIC 
// MAGIC Learn more about markdown:
// MAGIC 
// MAGIC * [https://guides.github.com/features/mastering-markdown/](https://guides.github.com/features/mastering-markdown/)
// MAGIC 
// MAGIC Note that there are flavours or minor variants and enhancements of markdown, including those specific to databricks, github, [pandoc](https://pandoc.org/MANUAL.html), etc.
// MAGIC 
// MAGIC It will be future-proof to remain in the syntactic zone of *pure markdown* (at the intersection of various flavours) as much as possible and go with [pandoc](https://pandoc.org/MANUAL.html)-compatible style if choices are necessary.
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

1+1

// COMMAND ----------

println(System.currentTimeMillis) // press Ctrl+Enter to evaluate println that prints its argument as a line

// COMMAND ----------

// MAGIC %scala
// MAGIC 1+1

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
// MAGIC ls

// COMMAND ----------

// MAGIC %sh
// MAGIC whoami

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