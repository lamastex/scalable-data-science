[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Article Topics in Retweet Network
=================================

Student Project
---------------

by [Li Caldeira Balkeståhl](https://www.linkedin.com/in/li-caldeira-balkest%C3%A5hl-9b839412b/) and [Mariama Jaiteh](https://www.linkedin.com/in/mariama-jaiteh-a97ab373/)

Scalable web scrapper
---------------------

### Based on [Mastering Spark for Data Science](https://www.packtpub.com/big-data-and-business-intelligence/mastering-spark-data-science) book (Chap. 6)

This cleaned up notebook contains code to perform web scraping, which should then be called from another notebook

Need to install the goose web scraper library, available with maven coordinate:

`com.syncthemall:goose:2.1.25`

``` scala
//import the web scraper
import com.gravity.goose._
```

>     import com.gravity.goose._

The functions for expanding the urls

``` scala
import org.apache.commons.lang3.StringUtils 
import java.net.HttpURLConnection;
import java.net.URL;
//function to expand once the urls
def expandUrl(url: String) : String = {
  var connection: HttpURLConnection = null
  try {
      connection = new URL(url)
                  .openConnection
                  .asInstanceOf[HttpURLConnection]
      connection.setInstanceFollowRedirects(false)
      connection.setUseCaches(false)
      connection.setRequestMethod("GET")
      connection.connect()
      val redirectedUrl = connection.getHeaderField("Location")
      if(StringUtils.isNotEmpty(redirectedUrl)){
        redirectedUrl
      } else {
        url
      }
  } catch {
      case e: Throwable => url
  } finally {
      if(connection != null)
        connection.disconnect()
   }
}

//function to keep expanding until you get the same
def expandUrluntilsame(url:String) : String ={
  var expanded=expandUrl(url)
  var unexpanded=url
  while(! StringUtils.equals(expanded,unexpanded)){
    unexpanded=expanded
    expanded=expandUrl(expanded)
  }
  expanded
}
```

>     import org.apache.commons.lang3.StringUtils
>     import java.net.HttpURLConnection
>     import java.net.URL
>     expandUrl: (url: String)String
>     expandUrluntilsame: (url: String)String

To get the articles from the web, use a dataframe of urls and return a dataframe with body, domain, title

-   First we make a case class Articles containing body, domain, title and meta description (from the HTML)
-   Use a function getArticles takes a string as input and return an Articles (filled with "null" string if any exception occured)
-   Then make UserDefinedFunction that takes a column name and returns Articles from stuff in that column (to be able to use directly on the DF)

``` scala
//the class for the web article
case class Articles(
  title: String, 
  body: String, 
  domain: String,  //problem: If we add more stuff to the Articles class,  getArticles  needs to be changed
  description: String,
  keywords: String,
  status: String
)

//the scraper
def getArticles(url_article: String) : Articles = {
  try{
    //using configuration to make goose skip retrieving images
    val conf: Configuration = new Configuration
    conf.setEnableImageFetching(false)
    val goosy = new Goose(conf)
    val article = goosy.extractContent(url_article) 
    val d = article.domain
    val t = article.title
    val b = article.cleanedArticleText
    val desc=article.metaDescription
    val keyw=article.metaKeywords
    Articles(t, b, d,desc,keyw, "found")
  }
  catch {
    case _: Throwable  => Articles("null","null","null","","","not found")
//    case uhe: java.net.UnknownHostException => Seq(Articles("null","null","null","not found")).toDF()

  }
}

//user defined function to be used with dataframes
val ArticleUserDefined = udf((s:String) => getArticles(expandUrluntilsame(s))) //this actually creates a new instance of goose for each article,this is slow and not scalable as they say in the book (i think)
```

>     defined class Articles
>     getArticles: (url_article: String)Articles
>     ArticleUserDefined: org.apache.spark.sql.expressions.UserDefinedFunction = UserDefinedFunction(<function1>,StructType(StructField(title,StringType,true), StructField(body,StringType,true), StructField(domain,StringType,true), StructField(description,StringType,true), StructField(status,StringType,true)),Some(List(StringType)))

Flattening the dataframe that does not need information on the number of new columns we want (copied with modification from notebook 007 of the course):

``` scala
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

implicit class DataFrameFlattener(df: DataFrame) {
  def flattenSchema: DataFrame = {
    df.select(flatten(Nil, df.schema): _*)
  }

  protected def flatten(path: Seq[String], schema: DataType): Seq[Column] = schema match {
    case s: StructType => s.fields.flatMap(f => flatten(path :+ f.name, f.dataType)) 
    //case other => col(path.map(n => s"`$n`").mkString(".")).as(path.mkString(".")) :: Nil //original
    case other => col(path.map(n => s"`$n`").mkString(".")).as(path.last) :: Nil //i just want the lowest nested name (is last too computationally expensive?)
  }
}
```

>     import org.apache.spark.sql._
>     import org.apache.spark.sql.functions._
>     import org.apache.spark.sql.types._
>     defined class DataFrameFlattener

Finally a function to do all the above:

``` scala
//just one function to be called on the URL dataframe
def getContent(urlDF:DataFrame): DataFrame = {
  //needs a column called URL with the URLs
    urlDF.withColumn("article",ArticleUserDefined($"URL")).flattenSchema  
}
```

>     getContent: (urlDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame