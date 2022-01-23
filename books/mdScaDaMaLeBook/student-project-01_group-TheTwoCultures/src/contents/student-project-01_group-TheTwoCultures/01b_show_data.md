<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` run
/scalable-data-science/000_0-sds-3-x-projects/student-project-01_group-TheTwoCultures/01_load_data
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

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

<div class="output execute_result plain_result" execution_count="1">

    save_df: (df: org.apache.spark.sql.DataFrame, filePath: String)Unit
    load_df: (filePath: String)org.apache.spark.sql.DataFrame
    no_forums: (df: org.apache.spark.sql.DataFrame)Long

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

</div>

<div class="cell code" execution_count="1" scrolled="false">

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

<div class="cell code" execution_count="1" scrolled="false">

``` scala
var file_name = "dbfs:/datasets/student-project-01/familjeliv/familjeliv-allmanna-ekonomi.xml"
var xml_df = read_xml(file_name).cache()
var df = get_dataset(file_name).cache()
```

<div class="output execute_result plain_result" execution_count="1">

    file_name: String = dbfs:/datasets/student-project-01/familjeliv/familjeliv-allmanna-ekonomi.xml
    xml_df: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [_id: string, _title: string ... 2 more fields]
    df: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [thread_id: string, thread_title: string ... 5 more fields]

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
xml_df.printSchema()
```

<div class="output execute_result plain_result" execution_count="1">

    root
     |-- _id: string (nullable = false)
     |-- _title: string (nullable = false)
     |-- _url: string (nullable = false)
     |-- thread: array (nullable = false)
     |    |-- element: struct (containsNull = true)
     |    |    |-- _id: string (nullable = false)
     |    |    |-- _title: string (nullable = false)
     |    |    |-- _url: string (nullable = false)
     |    |    |-- text: struct (nullable = false)
     |    |    |    |-- sentence: array (nullable = false)
     |    |    |    |    |-- element: struct (containsNull = true)
     |    |    |    |    |    |-- w: array (nullable = true)
     |    |    |    |    |    |    |-- element: string (containsNull = true)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
xml_df.show(10)
```

<div class="output execute_result plain_result" execution_count="1">

    +------+--------------------+--------------------+--------------------+
    |   _id|              _title|                _url|              thread|
    +------+--------------------+--------------------+--------------------+
    |19-290|Allmänna rubriker...|http://www.familj...|[[70148929, Frivi...|
    |19-290|Allmänna rubriker...|http://www.familj...|[[58302374, Missh...|
    |19-290|Allmänna rubriker...|http://www.familj...|[[36330819, Är så...|
    |19-290|Allmänna rubriker...|http://www.familj...|[[75852809, Hur k...|
    |19-290|Allmänna rubriker...|http://www.familj...|[[42304381, Hej a...|
    |19-290|Allmänna rubriker...|http://www.familj...|[[41294375, när p...|
    |19-295|Allmänna rubriker...|http://www.familj...|[[47653437, Har v...|
    |19-295|Allmänna rubriker...|http://www.familj...|[[75266317, Anmäl...|
    |19-295|Allmänna rubriker...|http://www.familj...|[[76559817, Fel a...|
    |19-295|Allmänna rubriker...|http://www.familj...|[[62028128, Vad g...|
    +------+--------------------+--------------------+--------------------+
    only showing top 10 rows

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
df.printSchema()
```

<div class="output execute_result plain_result" execution_count="1">

    root
     |-- thread_id: string (nullable = true)
     |-- thread_title: string (nullable = true)
     |-- w: string (nullable = false)
     |-- forum_id: string (nullable = true)
     |-- forum_title: string (nullable = true)
     |-- platform: string (nullable = false)
     |-- corpus_id: string (nullable = false)

</div>

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
display(df)
```

</div>
