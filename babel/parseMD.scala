import org.apache.hadoop.io.LongWritable

import org.apache.hadoop.io.Text

import org.apache.hadoop.conf.Configuration

import org.apache.hadoop.mapreduce.lib.input.TextInputFormat

val conf = new Configuration

conf.set("textinputformat.record.delimiter", "// COMMAND ----------")

val a = sc.newAPIHadoopFile("./IntroScalaNotebooks.scala", classOf[TextInputFormat], classOf[LongWritable], classOf[Text], conf).map(_._2.toString)

// the above code snippet is from http://stackoverflow.com/questions/25259425/spark-reading-files-using-different-delimiter-than-new-line

val b = a.map(x => x.replaceAll("// MAGIC ","")).map( x => { if (x.contains("%md")) x.replaceAll("%md","") else "```scala"+x+"```"})

b.coalesce(1,shuffle=true).saveAsTextFile("./tmp")

//-------TODO
//need to modularize this - currently running in spark-shell 
//spark-shell
//:load parseMD.scala
// $ cp tmp/part-00000 db/IntroScalaNotebooks.md && rm -r tmp/
//need to pass in argument fileName.scala to spark function and have it save fileName.md automagically!
//----------------

//val a = sc.wholeTextFiles("./IntroScalaNotebooks.md").map(x => x._2).map(x=>x.replaceAll("// MAGIC ","")).map(x => x.split("// COMMAND ----------"))
