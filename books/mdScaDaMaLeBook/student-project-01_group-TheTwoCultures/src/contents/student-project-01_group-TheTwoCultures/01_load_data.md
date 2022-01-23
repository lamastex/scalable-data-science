<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

3. Methods to load data
-----------------------

</div>

<div class="cell markdown">

### Preprocessing and loading the relevant data

Each forum comes as an XML-file with the structure given below:

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// Data comes in XML-files with the following structure.
/*
<corpus id="familjeliv-adoption">
<forum id="13-242" title="Adoption &gt; Intresserad" url="http://www.familjeliv.
se/forum/13/242">
<thread id="34277335" title="Tips för att välja land" postcount="25" lastpost="2
008-07-08 17:55:14" url="http://www.familjeliv.se/forum/thread/34277335-tips-for
-att-valja-land">
<text datefrom="20080707" dateto="20080707" timefrom="220854" timeto="220854" lix="30.55" ovix="60.74" nk="0.51" id="34284994" username="Miss TN" date="2008-07-07 22:08:54" url="http://www.familjeliv.se/forum/thread/34277335-tips-for-att-valja-land/2#anchor-m16">
<sentence id="bacc55562-baca83a75" _geocontext="|">
<w pos="VB" msd="VB.PRS.AKT" lemma="|känna|" lex="|känna..vb.2|känna..vb.1|" sense="|känna..1:0.522|känna..2:0.313|känna..4:0.158|känna..3:0.006|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="01" dephead="07" deprel="AA">Känner</w>
<w pos="PN" msd="PN.UTR.SIN.DEF.SUB" lemma="|ni|" lex="|ni..pn.1|" sense="|ni..1:-1.000|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="02" dephead="01" deprel="OO">ni</w>
<w pos="PP" msd="PP" lemma="|för|" lex="|för..pp.1|" sense="|för..1:-1.000|för..5:-1.000|för..6:-1.000|för..7:-1.000|för..9:-1.000|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="03" dephead="01" deprel="OA">för</w>
<w pos="DT" msd="DT.NEU.SIN.IND" lemma="|en|" lex="|en..al.1|" sense="|den..1:-1.000|en..2:-1.000|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="04" dephead="06" deprel="DT">ett</w>
<w pos="JJ" msd="JJ.POS.NEU.SIN.IND.NOM" lemma="|låg|" lex="|låg..av.1|" sense="|låg..1:-1.000|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="05" dephead="06" deprel="AT">lågt</w>
<w pos="NN" msd="NN.NEU.SIN.IND.NOM" lemma="|medgivande|" lex="|medgivande..nn.1|" sense="|medgivande..1:0.595|medgivande..2:0.405|" prefix="|medge..vb.1|mede..nn.1|" suffix="|givande..nn.1|ande..nn.1|" compwf="|medgiv+ande|med+givande|med+giv+ande|" complemgram="|medge..vb.1+ande..nn.1:4.632e-17|mede..nn.1+givande..nn.1:6.075e-27|mede..nn.1+giv..nn.1+ande..nn.1:5.387e-27|mede..nn.1+ge..vb.1+ande..nn.1:1.257e-29|" ref="06" dephead="03" deprel="PA">medgivande</w>
<w pos="VB" msd="VB.PRS.AKT" lemma="|skola|" lex="|skola..vb.2|" sense="|skola..4:-1.000|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="07" deprel="ROOT">ska</w>
<w pos="PN" msd="PN.UTR.PLU.DEF.SUB" lemma="|ni|" lex="|ni..pn.1|" sense="|ni..1:-1.000|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="08" dephead="07" deprel="SS">ni</w>
<w pos="AB" msd="AB" lemma="|verkligen|" lex="|verkligen..ab.1|" sense="|verkligen..1:-1.000|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="09" dephead="07" deprel="MA">verkligen</w>
<w pos="VB" msd="VB.INF.AKT" lemma="|sträva|" lex="|sträva..vb.1|" sense="|sträva..1:-1.000|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="10" dephead="07" deprel="VG">sträva</w>
.
.
.
</sentence>
</text>
</thread>
<thread id="42755312" title="Om vi planerar ett barn om sju år, när ska vi dra igång adoptionsprocessen?" postcount="3" lastpost="2009-04-01 18:39:55" url="http://www.familjeliv.se/forum/thread/42755312-om-vi-planerar-ett-barn-om-sju-ar-nar-ska-vi-dra-igang-adoptionsprocessen">
<text datefrom="20090331" dateto="20090331" timefrom="201724" timeto="201724" lix="27.48" ovix="50.60" nk="0.37" id="42755312" username="alvaereva" date="2009-03-31 20:17:24" url="http://www.familjeliv.se/forum/thread/42755312-om-vi-planerar-ett-barn-om-sju-ar-nar-ska-vi-dra-igang-adoptionsprocessen/1">
<sentence id="bac2ec05f-bace4e647" _geocontext="|">
<w pos="KN" msd="KN" lemma="|" lex="|" sense="|" prefix="|" suffix="|" compwf="|" complemgram="|" ref="01" deprel="ROOT">Som</w>
.
.
.
*/
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import org.apache.spark.sql.functions.{col,concat_ws, udf, flatten, explode, collect_list, collect_set, lit}
import org.apache.spark.sql.types.{ ArrayType, StructType, StructField, StringType, IntegerType }
import com.databricks.spark.xml._ // Add the DataFrame.read.xml() method
import org.apache.spark.sql.functions._


def read_xml(file_name: String): org.apache.spark.sql.DataFrame = {
  val sentence_schema = StructType(Array(
    StructField("w", ArrayType(StringType, true), nullable=true)
  ))
  val text_schema = StructType(Array(
     StructField("sentence", ArrayType(sentence_schema), nullable=false)
  ))

  val thread_schema = StructType(Array(
    StructField("_id", StringType, nullable = false),
    StructField("_title", StringType, nullable = false),
    StructField("_url", StringType, nullable = false),
    StructField("text", text_schema, nullable=false)
  ))

  val forum_schema = StructType(Array(
    StructField("_id", StringType, nullable = false),
    StructField("_title", StringType, nullable = false),
    StructField("_url", StringType, nullable = false),
    StructField("thread", ArrayType(thread_schema), nullable=false)
  ))

  val corpus_schema = StructType(Array(
    StructField("_id", StringType, nullable = false),
    StructField("forum", forum_schema, nullable=false)
  ))

  spark.read
    .option("rowTag", "forum")
    .schema(forum_schema)
    .xml(file_name)
 }

def get_dataset(file_name: String) : org.apache.spark.sql.DataFrame = {
  val xml_df = read_xml(file_name)
  val ds = xml_df.withColumn("thread", explode($"thread"))
  val splitted_name = file_name.split("/")
  val forum = splitted_name(splitted_name.size-2)
  val corpus = splitted_name(splitted_name.size-1)
  val value = udf((arr: Seq[String]) => arr.mkString(","))
  ds.select(col("_id") as "forum_id",
                     col("_title") as "forum_title",
                     col("thread._id") as "thread_id",
                     col("thread._title") as "thread_title",
                     flatten(col("thread.text.sentence.w")) as "w")
                .withColumn("w", explode($"w"))
               .groupBy("thread_id")
               .agg(first("thread_title") as("thread_title"),
                    collect_list("w") as "w",
                    first("forum_id") as "forum_id",
                    first("forum_title") as "forum_title")
               .withColumn("w", concat_ws(",",col("w")))
               .withColumn("platform", lit(forum))
               .withColumn("corpus_id", lit(corpus))
}
```

<div class="output execute_result plain_result" execution_count="1">

    import org.apache.spark.sql.functions.{col, concat_ws, udf, flatten, explode, collect_list, collect_set, lit}
    import org.apache.spark.sql.types.{ArrayType, StructType, StructField, StringType, IntegerType}
    import com.databricks.spark.xml._
    import org.apache.spark.sql.functions._
    read_xml: (file_name: String)org.apache.spark.sql.DataFrame
    get_dataset: (file_name: String)org.apache.spark.sql.DataFrame

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
def save_df(df: org.apache.spark.sql.DataFrame, filePath:String){
  df.write.format("parquet").save(filePath)  
}

def load_df(filePath: String): org.apache.spark.sql.DataFrame = {
  spark.read.format("parquet").load(filePath)
 }

def no_forums(df: org.apache.spark.sql.DataFrame): Long = {
  val forums = df.select("forum_title").distinct()
  forums.show(false)
  forums.count()
}
```

<div class="output execute_result plain_result" execution_count="1">

    save_df: (df: org.apache.spark.sql.DataFrame, filePath: String)Unit
    load_df: (filePath: String)org.apache.spark.sql.DataFrame
    no_forums: (df: org.apache.spark.sql.DataFrame)Long

</div>

</div>

<div class="cell markdown">

4. Save preprocessed data
-------------------------

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
var fl_root = "dbfs:/datasets/student-project-01/familjeliv/"
var fb_root = "dbfs:/datasets/student-project-01/flashback/"

val fl_data = Array("familjeliv-allmanna-ekonomi",
                    "familjeliv-sexsamlevnad")

val fb_data = Array("flashback-ekonomi",
                    "flashback-sex")

for (name <- fl_data){
  try{
    println(s"${fb_root}${name}_df")
    dbutils.fs.ls(s"${fl_root}${name}_df")
    println(s"${name}_df already exists!")
  }
  catch{
    case e: java.io.FileNotFoundException => {
      val file_name = s"${fl_root}${name}.xml"
      val df = get_dataset(file_name)
      val file_path = s"${fl_root}${name}_df"
      save_df(df, file_path)
    }
  }
}

for (name <- fb_data){
  try{
    println(s"${fb_root}${name}_df")
    dbutils.fs.ls(s"${fb_root}${name}_df")
    println(s"${name}_df already exists!")
  }
  catch{
    case e: java.io.FileNotFoundException => {
      val file_name = s"${fb_root}${name}.xml"
      val df = get_dataset(file_name)
      val file_path = s"${fb_root}${name}_df"
      save_df(df, file_path)
    }
  }
}
```

<div class="output execute_result plain_result" execution_count="1">

    dbfs:/datasets/student-project-01/flashback/familjeliv-allmanna-ekonomi_df
    familjeliv-allmanna-ekonomi_df already exists!
    dbfs:/datasets/student-project-01/flashback/familjeliv-sexsamlevnad_df
    familjeliv-sexsamlevnad_df already exists!
    dbfs:/datasets/student-project-01/flashback/flashback-ekonomi_df
    flashback-ekonomi_df already exists!
    dbfs:/datasets/student-project-01/flashback/flashback-sex_df
    flashback-sex_df already exists!
    fl_root: String = dbfs:/datasets/student-project-01/familjeliv/
    fb_root: String = dbfs:/datasets/student-project-01/flashback/
    fl_data: Array[String] = Array(familjeliv-allmanna-ekonomi, familjeliv-sexsamlevnad)
    fb_data: Array[String] = Array(flashback-ekonomi, flashback-sex)

</div>

</div>
