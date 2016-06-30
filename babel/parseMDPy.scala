
//val ioFilenameRoot = "../db/week1/01_introduction/000_scalableDataScience"
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat

val conf = new Configuration
conf.set("textinputformat.record.delimiter", "# COMMAND ----------")

val a = sc.newAPIHadoopFile(ioFilenameRoot+".py", classOf[TextInputFormat], classOf[LongWritable], classOf[Text], conf).map(_._2.toString)

// the above code snippet is from http://stackoverflow.com/questions/25259425/spark-reading-files-using-different-delimiter-than-new-line

// this works but misses frameIt in git-book
val b = a.map(x => x.replaceAll("# MAGIC %md","MYMAGICMD")).map(x => x.replaceAll("# MAGIC ","")).map( x => { if (x.contains("MYMAGICMD")) x.replaceAll("MYMAGICMD","") else "```python"+x+"```"})

//TODO!!! -- may be... depending on what we want GitBook to render instead of in-place embedded URL's in-frame.
//val b = a.map(x => x.replaceAll("// MAGIC %md","MYMAGICMD")).map(x => x.replaceAll("// MAGIC ","")).map( x => { if (x.contains("MYMAGICMD")) x.replaceAll("MYMAGICMD","") else "```scala"+x+"```"}).map( x=> {if (x.contains("frameIt($url)")) or OHOHNONOYAYASEEININGITBOOKNOW  then just return '%md [url](url)})

b.coalesce(1,shuffle=true).saveAsTextFile("./MDparsed")

println("DONE")

System.exit(0)

//-------TODO
//need to modularize this - currently running in spark-shell - but easy to chase ascii
//spark-shell
//:load parseMD.scala
// $ cp tmp/part-00000 db/IntroScalaNotebooks.md && rm -r tmp/
//need to pass in argument fileName.scala to spark function and have it save fileName.md automagically!
//----------------

// When all works beautifully we can just use wholeTextFiles to process all of the .scala into .md by using the `val b` syntax above :)
//val a = sc.wholeTextFiles("./IntroScalaNotebooks.md").map(x => x._2).map(x=>x.replaceAll("// MAGIC ","")).map(x => x.split("// COMMAND ----------"))
