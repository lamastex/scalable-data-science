// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
// MAGIC 
// MAGIC ## Russian Word Count
// MAGIC By Yevgen Ryeznik
// MAGIC 
// MAGIC And how to upload files into databricks distributed file system.

// COMMAND ----------

// MAGIC %md
// MAGIC Archived YouTube video of this live unedited lab-lecture:
// MAGIC 
// MAGIC [![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/fxAlrRqwYRw/0.jpg)](https://www.youtube.com/embed/fxAlrRqwYRw?start=0&end=1141&autoplay=1)

// COMMAND ----------

// MAGIC %md
// MAGIC In 1965, a Russian writer Mikhail Aleksandrovich Sholokhov (1905-1984) was awarded the *Nobel Prize for Literature* for the book
// MAGIC **"And Quiet Flows the Don"** (Tikhij Don).
// MAGIC 
// MAGIC This is a book about life and fate of Don Cossacks during the *Russian Revolution*, the *civil war* and the period of *collectivization*.
// MAGIC 
// MAGIC There existed a suspicion that the book had been written by a different writer. 
// MAGIC 
// MAGIC In 2007, Norwegian mathematician Nils Lid Hjort (https://en.wikipedia.org/wiki/Nils_Lid_Hjort) made a statistical analysis of Sholokhov texts which gave full support to Sholokhov.
// MAGIC 
// MAGIC In this notebook, I attempt to obtain some frequency tables used in this analysis.
// MAGIC 
// MAGIC 1. We use an online library "http://www.lib.ru/"
// MAGIC 2. txt file "http://www.lib.ru/PROZA/SHOLOHOW/sudbache.txt_Ascii.txt" -- "Fate of a Man" 
// MAGIC 3. txt file "http://www.lib.ru/PROZA/SHOLOHOW/tihijdon12.txt_Ascii.txt"  -- "And Quiet Flows the Don" (vol 1-2)
// MAGIC 4. txt file "http://www.lib.ru/PROZA/SHOLOHOW/tihijdon34.txt_Ascii.txt"  -- "And Quiet Flows the Don" (vol 3-4)

// COMMAND ----------

// MAGIC %sh
// MAGIC 
// MAGIC rm -rf books 
// MAGIC mkdir books && cd books
// MAGIC wget "http://www.lib.ru/PROZA/SHOLOHOW/sudbache.txt_Ascii.txt"    
// MAGIC wget "http://www.lib.ru/PROZA/SHOLOHOW/tihijdon12.txt_Ascii.txt"
// MAGIC wget "http://www.lib.ru/PROZA/SHOLOHOW/tihijdon34.txt_Ascii.txt"
// MAGIC 
// MAGIC cat tihijdon12.txt_Ascii.txt tihijdon34.txt_Ascii.txt > tihijdon.txt
// MAGIC rm tihijdon12.txt_Ascii.txt 
// MAGIC rm tihijdon34.txt_Ascii.txt
// MAGIC 
// MAGIC iconv -f KOI8-R -t utf8 sudbache.txt_Ascii.txt > sudba_cheloveka.txt
// MAGIC iconv -f KOI8-R -t utf8 tihijdon.txt > tihij_don.txt
// MAGIC 
// MAGIC rm sudbache.txt_Ascii.txt
// MAGIC rm tihijdon.txt
// MAGIC 
// MAGIC ls

// COMMAND ----------

// MAGIC %sh
// MAGIC cd books 
// MAGIC ls

// COMMAND ----------

dbutils.fs.mkdirs("dbfs:/datasets/books")
dbutils.fs.cp("file:/databricks/driver/books", "dbfs:/datasets/books", recurse=true)
display(dbutils.fs.ls("dbfs:/datasets/books"))

// COMMAND ----------

// We read a file sudba_cheloveka.txt using sc.textFile.
// The actual text starts from 24th line, so we have to skip them 
val sudba_cheloveka = sc.textFile("dbfs:/datasets/books/sudba_cheloveka.txt")
  .zipWithIndex()
  .filter(_._2 >= 24L)
  .map(_._1)
  .cache()

// 1) sentences are separated either with ? or ! or .
//    we replace these sighs with &
// 2) there are possible simbols inside a sentences: ",", ":", ";","-"
//    we replace them with empty string ""
val text_lines = sudba_cheloveka.map((line:String) => line
  .replaceAll("""([?!.])""", "&")
  .replaceAll("""([,:;-])""", "")
  .replaceAll("\\s+", " ")
  )

// collect all the test lines in Array[String] => we get sentences
val sentences = text_lines.collect.mkString(" ").replaceAll("\\s+", " ").split("&")

// COMMAND ----------

// obtaining a DataFrame of words' frequencies in sentences.
val word_frequency = sc.parallelize(
sentences.map(item => item.split(" ").drop(1).length).filter(_ != 0).
  map(item => {
    if (1 <= item && item <= 5) {"01 -- 05"}
    else if (6 <= item && item <= 10) {"06 -- 10"}
    else if (10 <= item && item <= 15) {"11 -- 15"}
    else if (16 <= item && item <= 20) {"16 -- 20"} 
    else if (21 <= item && item <= 25) {"21 -- 25"} 
    else if (26 <= item && item <= 30) {"26 -- 30"} 
    else if (31 <= item && item <= 35) {"31 -- 35"} 
    else if (36 <= item && item <= 40) {"36 -- 40"} 
    else if (41 <= item && item <= 45) {"41 -- 45"} 
    else if (45 <= item && item <= 50) {"45 -- 50"} 
    else {"50+"} 
  })).toDF("words").groupBy("words").count().orderBy("words")

word_frequency.show()


// COMMAND ----------

// All code above can be wrapped into a function

def frequency_count(file_path: String) = {
  // We read a file sudba_cheloveka.txt using sc.textFile.
  // The actual text starts from 24th line, so we have to skip them 
  val book = sc.textFile(file_path)
    .zipWithIndex()
    .filter(_._2 >= 24L)
    .map(_._1)
    .cache()

  // 1) sentences are separated either with ? or ! or .
  //    we replace these sighs with &
  // 2) there are possible simbols inside a sentences: ",", ":", ";","-"
  //    we replace them with empty string ""
  val text_lines = book.map((line:String) => line
    .replaceAll("""([?!.])""", "&")
    .replaceAll("""([,:;-])""", "")
    .replaceAll("\\s+", " ")
    )

  // collect all the test lines in Array[String] => we get sentences
  val sentences = text_lines.collect.mkString(" ").replaceAll("\\s+", " ").split("&")

  // obtaining a DataFrame of words' frequencies in sentences.
  val word_frequency = sc.parallelize(
  sentences.map(item => item.split(" ").drop(1).length).filter(_ != 0).
    map(item => {
      if (1 <= item && item <= 5) {"01 -- 05"}
      else if (6 <= item && item <= 10) {"06 -- 10"}
      else if (10 <= item && item <= 15) {"11 -- 15"}
      else if (16 <= item && item <= 20) {"16 -- 20"} 
      else if (21 <= item && item <= 25) {"21 -- 25"} 
      else if (26 <= item && item <= 30) {"26 -- 30"} 
      else if (31 <= item && item <= 35) {"31 -- 35"} 
      else if (36 <= item && item <= 40) {"36 -- 40"} 
      else if (41 <= item && item <= 45) {"41 -- 45"} 
      else if (45 <= item && item <= 50) {"45 -- 50"} 
      else {"50+"} 
    })).toDF("words").groupBy("words").count().orderBy("words")

  word_frequency.show()
}



// COMMAND ----------

frequency_count("dbfs:/datasets/books/sudba_cheloveka.txt")

// COMMAND ----------

// Now let us do the same but for the all content in books folder
val books = sc.wholeTextFiles("dbfs:/datasets/books/*.txt") 
books.cache() // let's cache this RDD for efficient reuse
books.count()

// COMMAND ----------

books.take(2).map(item => item._1).map(file_path => frequency_count(file_path))