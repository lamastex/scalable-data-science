// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845)
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)

// COMMAND ----------

// MAGIC %md
// MAGIC #Topic Modeling with Latent Dirichlet Allocation
// MAGIC ### Let us cluster the conversations from different movies!
// MAGIC This notebook will provide a brief algorithm summary, links for further reading, and an example of how to use LDA for Topic Modeling.

// COMMAND ----------

// MAGIC %md
// MAGIC ##Algorithm Summary
// MAGIC - **Task**: Identify topics from a collection of text documents
// MAGIC - **Input**: Vectors of word counts
// MAGIC - **Optimizers**: 
// MAGIC     - EMLDAOptimizer using [Expectation Maximization](https://en.wikipedia.org/wiki/Expectation%E2%80%93maximization_algorithm)
// MAGIC     - OnlineLDAOptimizer using Iterative Mini-Batch Sampling for [Online Variational Bayes](https://www.cs.princeton.edu/~blei/papers/HoffmanBleiBach2010b.pdf)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Links
// MAGIC - Spark API docs
// MAGIC   - Scala: [LDA](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.clustering.LDA)
// MAGIC   - Python: [LDA](https://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.clustering.LDA)
// MAGIC - [MLlib Programming Guide](http://spark.apache.org/docs/latest/mllib-clustering.html#latent-dirichlet-allocation-lda)
// MAGIC - [ML Feature Extractors & Transformers](http://spark.apache.org/docs/latest/ml-features.html)
// MAGIC - [Wikipedia: Latent Dirichlet Allocation](https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Readings for LDA
// MAGIC 
// MAGIC * A high-level introduction to the topic from Communications of the ACM
// MAGIC     * [https://www.cs.princeton.edu/~blei/papers/Blei2012.pdf](https://www.cs.princeton.edu/~blei/papers/Blei2012.pdf)
// MAGIC * A very good high-level humanities introduction to the topic (recommended by Chris Thomson in English Department at UC, Ilam): 
// MAGIC     * [http://journalofdigitalhumanities.org/2-1/topic-modeling-and-digital-humanities-by-david-m-blei/](http://journalofdigitalhumanities.org/2-1/topic-modeling-and-digital-humanities-by-david-m-blei/)
// MAGIC 
// MAGIC Also read the methodological and more formal papers cited in the above links if you want to know more.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC Let's get a bird's eye view of LDA from [https://www.cs.princeton.edu/~blei/papers/Blei2012.pdf](https://www.cs.princeton.edu/~blei/papers/Blei2012.pdf) next.
// MAGIC 
// MAGIC * See pictures (hopefully you read the paper last night!)
// MAGIC * Algorithm of the generative model (this is unsupervised clustering)
// MAGIC * For a careful introduction to the topic see Section 27.3 and 27.4 (pages 950-970) pf Murphy's *Machine Learning: A Probabilistic Perspective, MIT Press, 2012*. 
// MAGIC * We will be quite application focussed or applied here!

// COMMAND ----------

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
displayHTML(frameIt("http://journalofdigitalhumanities.org/2-1/topic-modeling-and-digital-humanities-by-david-m-blei/",900))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Model",600))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Mathematical_definition",910))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Probabilistic Topic Modeling Example
// MAGIC 
// MAGIC This is an outline of our Topic Modeling workflow. Feel free to jump to any subtopic to find out more.
// MAGIC - Step 0. Dataset Review
// MAGIC - Step 1. Downloading and Loading Data into DBFS 
// MAGIC     - (Step 1. only needs to be done once per shard - see details at the end of the notebook for Step 1.)
// MAGIC - Step 2. Loading the Data and Data Cleaning 
// MAGIC - Step 3. Text Tokenization
// MAGIC - Step 4. Remove Stopwords
// MAGIC - Step 5. Vector of Token Counts
// MAGIC - Step 6. Create LDA model with Online Variational Bayes
// MAGIC - Step 7. Review Topics
// MAGIC - Step 8. Model Tuning - Refilter Stopwords
// MAGIC - Step 9. Create LDA model with Expectation Maximization
// MAGIC - Step 10. Visualize Results

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ## Step 0. Dataset Review
// MAGIC 
// MAGIC In this example, we will use the [Cornell Movie Dialogs Corpus](https://people.mpi-sws.org/~cristian/Cornell_Movie-Dialogs_Corpus.html).
// MAGIC 
// MAGIC Here is the `README.txt`:
// MAGIC ***
// MAGIC ***
// MAGIC Cornell Movie-Dialogs Corpus
// MAGIC 
// MAGIC Distributed together with:
// MAGIC 
// MAGIC "Chameleons in imagined conversations: A new approach to understanding coordination of linguistic style in dialogs"
// MAGIC Cristian Danescu-Niculescu-Mizil and Lillian Lee
// MAGIC Proceedings of the Workshop on Cognitive Modeling and Computational Linguistics, ACL 2011.
// MAGIC 
// MAGIC (this paper is included in this zip file)
// MAGIC 
// MAGIC NOTE: If you have results to report on these corpora, please send email to cristian@cs.cornell.edu or llee@cs.cornell.edu so we can add you to our list of people using this data.  Thanks!
// MAGIC 
// MAGIC 
// MAGIC Contents of this README:
// MAGIC 
// MAGIC         A) Brief description
// MAGIC         B) Files description
// MAGIC         C) Details on the collection procedure
// MAGIC         D) Contact
// MAGIC 
// MAGIC 
// MAGIC A) Brief description:
// MAGIC 
// MAGIC This corpus contains a metadata-rich collection of fictional conversations extracted from raw movie scripts:
// MAGIC 
// MAGIC - 220,579 conversational exchanges between 10,292 pairs of movie characters
// MAGIC - involves 9,035 characters from 617 movies
// MAGIC - in total 304,713 utterances
// MAGIC - movie metadata included:
// MAGIC         - genres
// MAGIC         - release year
// MAGIC         - IMDB rating
// MAGIC         - number of IMDB votes
// MAGIC         - IMDB rating
// MAGIC - character metadata included:
// MAGIC         - gender (for 3,774 characters)
// MAGIC         - position on movie credits (3,321 characters)
// MAGIC 
// MAGIC 
// MAGIC B) Files description:
// MAGIC 
// MAGIC In all files the field separator is " +++$+++ "
// MAGIC 
// MAGIC - movie_titles_metadata.txt
// MAGIC         - contains information about each movie title
// MAGIC         - fields:
// MAGIC                 - movieID,
// MAGIC                 - movie title,
// MAGIC                 - movie year,
// MAGIC                 - IMDB rating,
// MAGIC                 - no. IMDB votes,
// MAGIC                 - genres in the format ['genre1','genre2',...,'genreN']
// MAGIC 
// MAGIC - movie_characters_metadata.txt
// MAGIC         - contains information about each movie character
// MAGIC         - fields:
// MAGIC                 - characterID
// MAGIC                 - character name
// MAGIC                 - movieID
// MAGIC                 - movie title
// MAGIC                 - gender ("?" for unlabeled cases)
// MAGIC                 - position in credits ("?" for unlabeled cases)
// MAGIC 
// MAGIC - movie_lines.txt
// MAGIC         - contains the actual text of each utterance
// MAGIC         - fields:
// MAGIC                 - lineID
// MAGIC                 - characterID (who uttered this phrase)
// MAGIC                 - movieID
// MAGIC                 - character name
// MAGIC                 - text of the utterance
// MAGIC 
// MAGIC - movie_conversations.txt
// MAGIC         - the structure of the conversations
// MAGIC         - fields
// MAGIC                 - characterID of the first character involved in the conversation
// MAGIC                 - characterID of the second character involved in the conversation
// MAGIC                 - movieID of the movie in which the conversation occurred
// MAGIC                 - list of the utterances that make the conversation, in chronological
// MAGIC                         order: ['lineID1','lineID2',...,'lineIDN']
// MAGIC                         has to be matched with movie_lines.txt to reconstruct the actual content
// MAGIC 
// MAGIC - raw_script_urls.txt
// MAGIC         - the urls from which the raw sources were retrieved
// MAGIC 
// MAGIC C) Details on the collection procedure:
// MAGIC 
// MAGIC We started from raw publicly available movie scripts (sources acknowledged in
// MAGIC raw_script_urls.txt).  In order to collect the metadata necessary for this study
// MAGIC and to distinguish between two script versions of the same movie, we automatically
// MAGIC  matched each script with an entry in movie database provided by IMDB (The Internet
// MAGIC  Movie Database; data interfaces available at http://www.imdb.com/interfaces). Some
// MAGIC  amount of manual correction was also involved. When  more than one movie with the same
// MAGIC  title was found in IMBD, the match was made with the most popular title
// MAGIC (the one that received most IMDB votes)
// MAGIC 
// MAGIC After discarding all movies that could not be matched or that had less than 5 IMDB
// MAGIC votes, we were left with 617 unique titles with metadata including genre, release
// MAGIC year, IMDB rating and no. of IMDB votes and cast distribution.  We then identified
// MAGIC the pairs of characters that interact and separated their conversations automatically
// MAGIC using simple data processing heuristics. After discarding all pairs that exchanged
// MAGIC less than 5 conversational exchanges there were 10,292 left, exchanging 220,579
// MAGIC conversational exchanges (304,713 utterances).  After automatically matching the names
// MAGIC of the 9,035 involved characters to the list of cast distribution, we used the
// MAGIC gender of each interpreting actor to infer the fictional gender of a subset of
// MAGIC 3,321 movie characters (we raised the number of gendered 3,774 characters through
// MAGIC  manual annotation). Similarly, we collected the end credit position of a subset
// MAGIC of 3,321 characters as a proxy for their status.
// MAGIC 
// MAGIC 
// MAGIC D) Contact:
// MAGIC 
// MAGIC Please email any questions to: cristian@cs.cornell.edu (Cristian Danescu-Niculescu-Mizil)
// MAGIC 
// MAGIC ***
// MAGIC ***

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 2. Loading the Data and Data Cleaning
// MAGIC 
// MAGIC We have already used the wget command to download the file, and put it in our distributed file system (this process takes about 1 minute). To repeat these steps or to download data from another source follow the steps at the bottom of this worksheet on **Step 1. Downloading and Loading Data into DBFS**.
// MAGIC 
// MAGIC Let's make sure these files are in dbfs now:

// COMMAND ----------

// this is where the data resides in dbfs (see below to download it first, if you go to a new shard!)
display(dbutils.fs.ls("dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/")) 

// COMMAND ----------

// MAGIC %md
// MAGIC ## Conversations Data

// COMMAND ----------

// Load text file, leave out file paths, convert all strings to lowercase
val conversationsRaw = sc.textFile("dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/movie_conversations.txt").zipWithIndex()

// COMMAND ----------

// MAGIC %md
// MAGIC Review first 5 lines to get a sense for the data format.

// COMMAND ----------

conversationsRaw.top(5).foreach(println) // the first five Strings in the RDD

// COMMAND ----------

conversationsRaw.count // there are over 83,000 conversations in total

// COMMAND ----------

import scala.util.{Failure, Success}

val regexConversation = """\s*(\w+)\s+(\+{3}\$\+{3})\s*(\w+)\s+(\2)\s*(\w+)\s+(\2)\s*(\[.*\]\s*$)""".r

case class conversationLine(a: String, b: String, c: String, d: String)

val conversationsRaw = sc.textFile("dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/movie_conversations.txt")
 .zipWithIndex()
  .map(x => 
          {
            val id:Long = x._2
            val line = x._1
            val pLine = regexConversation.findFirstMatchIn(line)
                               .map(m => conversationLine(m.group(1), m.group(3), m.group(5), m.group(7))) 
                                  match {
                                    case Some(l) => Success(l)
                                    case None => Failure(new Exception(s"Non matching input: $line"))
                                  }
              (id,pLine)
           }
  )

// COMMAND ----------

conversationsRaw.filter(x => x._2.isSuccess).count()

// COMMAND ----------

conversationsRaw.filter(x => x._2.isFailure).count()

// COMMAND ----------

// MAGIC %md
// MAGIC The conversation number and line numbers of each conversation are in one line in `conversationsRaw`.

// COMMAND ----------

conversationsRaw.filter(x => x._2.isSuccess).take(5).foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's create `conversations` that have just the coversation id and line-number with order information.

// COMMAND ----------

val conversations 
    = conversationsRaw
      .filter(x => x._2.isSuccess)
      .flatMap { 
        case (id,Success(l))  
                  => { val conv = l.d.replace("[","").replace("]","").replace("'","").replace(" ","")
                       val convLinesIndexed = conv.split(",").zipWithIndex
                       convLinesIndexed.map( cLI => (id, cLI._2, cLI._1))
                      }
       }.toDF("conversationID","intraConversationID","lineID")

// COMMAND ----------

conversations.show(15)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Movie Titles

// COMMAND ----------

val moviesMetaDataRaw = sc.textFile("dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/movie_titles_metadata.txt")
moviesMetaDataRaw.top(5).foreach(println)

// COMMAND ----------

moviesMetaDataRaw.count() // number of movies

// COMMAND ----------

import scala.util.{Failure, Success}

/*  - contains information about each movie title
  - fields:
          - movieID,
          - movie title,
          - movie year,
          - IMDB rating,
          - no. IMDB votes,
          - genres in the format ['genre1','genre2',...,'genreN']
          */
val regexMovieMetaData = """\s*(\w+)\s+(\+{3}\$\+{3})\s*(.+)\s+(\2)\s+(.+)\s+(\2)\s+(.+)\s+(\2)\s+(.+)\s+(\2)\s+(\[.*\]\s*$)""".r

case class lineInMovieMetaData(movieID: String, movieTitle: String, movieYear: String, IMDBRating: String, NumIMDBVotes: String, genres: String)

val moviesMetaDataRaw = sc.textFile("dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/movie_titles_metadata.txt")
  .map(line => 
          {
            val pLine = regexMovieMetaData.findFirstMatchIn(line)
                               .map(m => lineInMovieMetaData(m.group(1), m.group(3), m.group(5), m.group(7), m.group(9), m.group(11))) 
                                  match {
                                    case Some(l) => Success(l)
                                    case None => Failure(new Exception(s"Non matching input: $line"))
                                  }
              pLine
           }
  )

// COMMAND ----------

moviesMetaDataRaw.count

// COMMAND ----------

moviesMetaDataRaw.filter(x => x.isSuccess).count()

// COMMAND ----------

moviesMetaDataRaw.filter(x => x.isSuccess).take(10).foreach(println)

// COMMAND ----------

//moviesMetaDataRaw.filter(x => x.isFailure).take(10).foreach(println) // to regex refine for casting

// COMMAND ----------

val moviesMetaData 
    = moviesMetaDataRaw
      .filter(x => x.isSuccess)
      .map { case Success(l) => l }
      .toDF().select("movieID","movieTitle","movieYear")

// COMMAND ----------

moviesMetaData.show(10,false)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Lines Data

// COMMAND ----------

val linesRaw = sc.textFile("dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/movie_lines.txt")

// COMMAND ----------

linesRaw.count() // number of lines making up the conversations

// COMMAND ----------

// MAGIC %md
// MAGIC Review first 5 lines to get a sense for the data format.

// COMMAND ----------

linesRaw.top(5).foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC To see 5 random lines in the `lines.txt` evaluate the following cell.

// COMMAND ----------

linesRaw.takeSample(false, 5).foreach(println)

// COMMAND ----------

import scala.util.{Failure, Success}

/*  field in line.txt are:
          - lineID
          - characterID (who uttered this phrase)
          - movieID
          - character name
          - text of the utterance
          */
val regexLine = """\s*(\w+)\s+(\+{3}\$\+{3})\s*(\w+)\s+(\2)\s*(\w+)\s+(\2)\s*(.+)\s+(\2)\s*(.*$)""".r

case class lineInMovie(lineID: String, characterID: String, movieID: String, characterName: String, text: String)

val linesRaw = sc.textFile("dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/movie_lines.txt")
  .map(line => 
          {
            val pLine = regexLine.findFirstMatchIn(line)
                               .map(m => lineInMovie(m.group(1), m.group(3), m.group(5), m.group(7), m.group(9))) 
                                  match {
                                    case Some(l) => Success(l)
                                    case None => Failure(new Exception(s"Non matching input: $line"))
                                  }
              pLine
           }
  )

// COMMAND ----------

linesRaw.filter(x => x.isSuccess).count()

// COMMAND ----------

linesRaw.filter(x => x.isFailure).count()

// COMMAND ----------

linesRaw.filter(x => x.isSuccess).take(5).foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's make a DataFrame out of the successfully parsed line.

// COMMAND ----------

val lines 
    = linesRaw
      .filter(x => x.isSuccess)
      .map { case Success(l) => l }
      .toDF()
      .join(moviesMetaData, "movieID") // and join it to get movie meta data

// COMMAND ----------

lines.show(5)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Dialogs with Lines
// MAGIC Let's join ght two DataFrames on `lineID` next.

// COMMAND ----------

val convLines = conversations.join(lines, "lineID").sort($"conversationID", $"intraConversationID")

// COMMAND ----------

convLines.count

// COMMAND ----------

conversations.count

// COMMAND ----------

display(convLines)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's amalgamate the texts utered in the same conversations together.
// MAGIC 
// MAGIC By doing this we loose all the information in the order of utterance. 
// MAGIC 
// MAGIC But this is fine as we are going to do LDA with just the *first-order information of words uttered in each conversation* by anyone involved in the dialogue.

// COMMAND ----------

import org.apache.spark.sql.functions.{collect_list, udf, lit, concat_ws}

val corpusDF = convLines.groupBy($"conversationID",$"movieID")
  .agg(concat_ws(" :-()-: ",collect_list($"text")).alias("corpus"))
  .join(moviesMetaData, "movieID") // and join it to get movie meta data
  .select($"conversationID".as("id"),$"corpus",$"movieTitle",$"movieYear")
  .cache()

// COMMAND ----------

corpusDF.count()

// COMMAND ----------

corpusDF.take(5).foreach(println)

// COMMAND ----------

display(corpusDF)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Feature extraction and transformation APIs

// COMMAND ----------

displayHTML(frameIt("http://spark.apache.org/docs/latest/ml-features.html",800))

// COMMAND ----------

// MAGIC %md 
// MAGIC We will use the convenient [Feature extraction and transformation APIs](http://spark.apache.org/docs/latest/ml-features.html).

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 3. Text Tokenization
// MAGIC 
// MAGIC We will use the RegexTokenizer to split each document into tokens. We can setMinTokenLength() here to indicate a minimum token length, and filter away all tokens that fall below the minimum.

// COMMAND ----------

displayHTML(frameIt("http://spark.apache.org/docs/latest/ml-features.html#tokenizer",700))

// COMMAND ----------

import org.apache.spark.ml.feature.RegexTokenizer

// Set params for RegexTokenizer
val tokenizer = new RegexTokenizer()
.setPattern("[\\W_]+") // break by white space character(s)
.setMinTokenLength(4) // Filter away tokens with length < 4
.setInputCol("corpus") // name of the input column
.setOutputCol("tokens") // name of the output column

// Tokenize document
val tokenized_df = tokenizer.transform(corpusDF)

// COMMAND ----------

display(tokenized_df.sample(false,0.001,1234L)) 

// COMMAND ----------

display(tokenized_df.sample(false,0.001,1234L).select("tokens"))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 4. Remove Stopwords
// MAGIC 
// MAGIC We can easily remove stopwords using the StopWordsRemover(). 

// COMMAND ----------

displayHTML(frameIt("http://spark.apache.org/docs/latest/ml-features.html#stopwordsremover",600))

// COMMAND ----------

// MAGIC %md
// MAGIC If a list of stopwords is not provided, the StopWordsRemover() will use [this list of stopwords](http://ir.dcs.gla.ac.uk/resources/linguistic_utils/stop_words), also shown below, by default. 
// MAGIC 
// MAGIC ``` a,about,above,across,after,afterwards,again,against,all,almost,alone,along,already,also,although,always,am,among,amongst,amoungst,amount,an,and,another,any,anyhow,anyone,anything,anyway,anywhere,
// MAGIC are,around,as,at,back,be,became,because,become,becomes,becoming,been,before,beforehand,behind,being,below,beside,besides,between,beyond,bill,both,bottom,but,by,call,can,cannot,cant,co,computer,con,could,
// MAGIC couldnt,cry,de,describe,detail,do,done,down,due,during,each,eg,eight,either,eleven,else,elsewhere,empty,enough,etc,even,ever,every,everyone,everything,everywhere,except,few,fifteen,fify,fill,find,fire,first,
// MAGIC five,for,former,formerly,forty,found,four,from,front,full,further,get,give,go,had,has,hasnt,have,he,hence,her,here,hereafter,hereby,herein,hereupon,hers,herself,him,himself,his,how,however,hundred,i,ie,if,
// MAGIC in,inc,indeed,interest,into,is,it,its,itself,keep,last,latter,latterly,least,less,ltd,made,many,may,me,meanwhile,might,mill,mine,more,moreover,most,mostly,move,much,must,my,myself,name,namely,neither,never,
// MAGIC nevertheless,next,nine,no,nobody,none,noone,nor,not,nothing,now,nowhere,of,off,often,on,once,one,only,onto,or,other,others,otherwise,our,ours,ourselves,out,over,own,part,per,perhaps,please,put,rather,re,same,
// MAGIC see,seem,seemed,seeming,seems,serious,several,she,should,show,side,since,sincere,six,sixty,so,some,somehow,someone,something,sometime,sometimes,somewhere,still,such,system,take,ten,than,that,the,their,them,
// MAGIC themselves,then,thence,there,thereafter,thereby,therefore,therein,thereupon,these,they,thick,thin,third,this,those,though,three,through,throughout,thru,thus,to,together,too,top,toward,towards,twelve,twenty,two,
// MAGIC un,under,until,up,upon,us,very,via,was,we,well,were,what,whatever,when,whence,whenever,where,whereafter,whereas,whereby,wherein,whereupon,wherever,whether,which,while,whither,who,whoever,whole,whom,whose,why,will,
// MAGIC with,within,without,would,yet,you,your,yours,yourself,yourselves
// MAGIC ```
// MAGIC 
// MAGIC You can use `getStopWords()` to see the list of stopwords that will be used.
// MAGIC 
// MAGIC In this example, we will specify a list of stopwords for the StopWordsRemover() to use. We do this so that we can add on to the list later on.

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/tmp/stopwords")) // check if the file already exists from earlier wget and dbfs-load

// COMMAND ----------

// MAGIC %md
// MAGIC If the file `dbfs:/tmp/stopwords` already exists then skip the next two cells, otherwise download and load it into DBFS by uncommenting and evaluating the next two cells.

// COMMAND ----------

//%sh wget http://ir.dcs.gla.ac.uk/resources/linguistic_utils/stop_words -O /tmp/stopwords # uncomment '//' at the beginning and repeat only if needed again

// COMMAND ----------

//%fs cp file:/tmp/stopwords dbfs:/tmp/stopwords # uncomment '//' at the beginning and repeat only if needed again

// COMMAND ----------

// List of stopwords
val stopwords = sc.textFile("/tmp/stopwords").collect()

// COMMAND ----------

stopwords.length // find the number of stopwords in the scala Array[String]

// COMMAND ----------

// MAGIC %md
// MAGIC Finally, we can just remove the stopwords using the `StopWordsRemover` as follows:

// COMMAND ----------

import org.apache.spark.ml.feature.StopWordsRemover

// Set params for StopWordsRemover
val remover = new StopWordsRemover()
.setStopWords(stopwords) // This parameter is optional
.setInputCol("tokens")
.setOutputCol("filtered")

// Create new DF with Stopwords removed
val filtered_df = remover.transform(tokenized_df)

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 5. Vector of Token Counts
// MAGIC 
// MAGIC LDA takes in a vector of token counts as input. We can use the `CountVectorizer()` to easily convert our text documents into vectors of token counts.
// MAGIC 
// MAGIC The `CountVectorizer` will return `(VocabSize, Array(Indexed Tokens), Array(Token Frequency))`.
// MAGIC 
// MAGIC Two handy parameters to note:
// MAGIC   - `setMinDF`: Specifies the minimum number of different documents a term must appear in to be included in the vocabulary.
// MAGIC   - `setMinTF`: Specifies the minimum number of times a term has to appear in a document to be included in the vocabulary.

// COMMAND ----------

displayHTML(frameIt("http://spark.apache.org/docs/latest/ml-features.html#countvectorizer",700))

// COMMAND ----------

import org.apache.spark.ml.feature.CountVectorizer

// Set params for CountVectorizer
val vectorizer = new CountVectorizer()
.setInputCol("filtered")
.setOutputCol("features")
.setVocabSize(10000) 
.setMinDF(5) // the minimum number of different documents a term must appear in to be included in the vocabulary.
.fit(filtered_df)

// COMMAND ----------

// Create vector of token counts
val countVectors = vectorizer.transform(filtered_df).select("id", "features")

// COMMAND ----------

// see the first countVectors
countVectors.take(1)

// COMMAND ----------

// MAGIC %md 
// MAGIC To use the LDA algorithm in the MLlib library, we have to convert the DataFrame back into an RDD.

// COMMAND ----------

// Convert DF to RDD
import org.apache.spark.mllib.linalg.Vector

val lda_countVector = countVectors.map { case Row(id: Long, countVector: Vector) => (id, countVector) }

// COMMAND ----------

// format: Array(id, (VocabSize, Array(indexedTokens), Array(Token Frequency)))
lda_countVector.take(1)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Let's get an overview of LDA in Spark's MLLIB

// COMMAND ----------

displayHTML(frameIt("http://spark.apache.org/docs/latest/mllib-clustering.html#latent-dirichlet-allocation-lda",800))

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Create LDA model with Online Variational Bayes
// MAGIC 
// MAGIC We will now set the parameters for LDA. We will use the OnlineLDAOptimizer() here, which implements Online Variational Bayes.
// MAGIC 
// MAGIC Choosing the number of topics for your LDA model requires a bit of domain knowledge. As we do not know the number of "topics", we will set numTopics to be 20.

// COMMAND ----------

val numTopics = 20

// COMMAND ----------

// MAGIC %md
// MAGIC We will set the parameters needed to build our LDA model. We can also setMiniBatchFraction for the OnlineLDAOptimizer, which sets the fraction of corpus sampled and used at each iteration. In this example, we will set this to 0.8.

// COMMAND ----------

import org.apache.spark.mllib.clustering.{LDA, OnlineLDAOptimizer}

// Set LDA params
val lda = new LDA()
.setOptimizer(new OnlineLDAOptimizer().setMiniBatchFraction(0.8))
.setK(numTopics)
.setMaxIterations(3)
.setDocConcentration(-1) // use default values
.setTopicConcentration(-1) // use default values

// COMMAND ----------

// MAGIC %md 
// MAGIC Create the LDA model with Online Variational Bayes.

// COMMAND ----------

val ldaModel = lda.run(lda_countVector)

// COMMAND ----------

// MAGIC %md
// MAGIC Watch **Online Learning for Latent Dirichlet Allocation** in NIPS2010 by Matt Hoffman (right click and open in new tab)
// MAGIC 
// MAGIC [![Matt Hoffman's NIPS 2010 Talk Online LDA]](http://videolectures.net/nips2010_hoffman_oll/thumb.jpg)](http://videolectures.net/nips2010_hoffman_oll/)
// MAGIC   
// MAGIC Also see the paper on *Online varioational Bayes* by Matt linked for more details (from the above URL): [http://videolectures.net/site/normal_dl/tag=83534/nips2010_1291.pdf](http://videolectures.net/site/normal_dl/tag=83534/nips2010_1291.pdf)

// COMMAND ----------

// MAGIC %md 
// MAGIC Note that using the OnlineLDAOptimizer returns us a [LocalLDAModel](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.clustering.LocalLDAModel), which stores the inferred topics of your corpus.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Review Topics
// MAGIC 
// MAGIC We can now review the results of our LDA model. We will print out all 20 topics with their corresponding term probabilities.
// MAGIC 
// MAGIC Note that you will get slightly different results every time you run an LDA model since LDA includes some randomization.
// MAGIC 
// MAGIC Let us review results of LDA model with Online Variational Bayes, step by step.

// COMMAND ----------

val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 5)

// COMMAND ----------

val vocabList = vectorizer.vocabulary

// COMMAND ----------

val topics = topicIndices.map { case (terms, termWeights) =>
  terms.map(vocabList(_)).zip(termWeights)
}

// COMMAND ----------

// MAGIC %md
// MAGIC Feel free to take things apart to understand!

// COMMAND ----------

topicIndices(0)

// COMMAND ----------

topicIndices(0)._1

// COMMAND ----------

topicIndices(0)._1(0)

// COMMAND ----------

vocabList(topicIndices(0)._1(0))

// COMMAND ----------

// MAGIC %md
// MAGIC Review Results of LDA model with Online Variational Bayes - Doing all four steps earlier at once.

// COMMAND ----------

val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 5)
val vocabList = vectorizer.vocabulary
val topics = topicIndices.map { case (terms, termWeights) =>
  terms.map(vocabList(_)).zip(termWeights)
}
println(s"$numTopics topics:")
topics.zipWithIndex.foreach { case (topic, i) =>
  println(s"TOPIC $i")
  topic.foreach { case (term, weight) => println(s"$term\t$weight") }
  println(s"==========")
}

// COMMAND ----------

// MAGIC %md
// MAGIC Going through the results, you may notice that some of the topic words returned are actually stopwords that are specific to our dataset (for eg: "writes", "article"...). Let's try improving our model.

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 8. Model Tuning - Refilter Stopwords
// MAGIC 
// MAGIC We will try to improve the results of our model by identifying some stopwords that are specific to our dataset. We will filter these stopwords out and rerun our LDA model to see if we get better results.

// COMMAND ----------

val add_stopwords = Array("whatever") // add  more stop-words like the name of your company!

// COMMAND ----------

// Combine newly identified stopwords to our exising list of stopwords
val new_stopwords = stopwords.union(add_stopwords)

// COMMAND ----------

import org.apache.spark.ml.feature.StopWordsRemover

// Set Params for StopWordsRemover with new_stopwords
val remover = new StopWordsRemover()
.setStopWords(new_stopwords)
.setInputCol("tokens")
.setOutputCol("filtered")

// Create new df with new list of stopwords removed
val new_filtered_df = remover.transform(tokenized_df)

// COMMAND ----------

// Set Params for CountVectorizer
val vectorizer = new CountVectorizer()
.setInputCol("filtered")
.setOutputCol("features")
.setVocabSize(10000)
.setMinDF(5)
.fit(new_filtered_df)

// Create new df of countVectors
val new_countVectors = vectorizer.transform(new_filtered_df).select("id", "features")

// COMMAND ----------

// Convert DF to RDD
val new_lda_countVector = new_countVectors.map { case Row(id: Long, countVector: Vector) => (id, countVector) }

// COMMAND ----------

// MAGIC %md
// MAGIC We will also increase MaxIterations to 10 to see if we get better results.

// COMMAND ----------

// Set LDA parameters
val new_lda = new LDA()
.setOptimizer(new OnlineLDAOptimizer().setMiniBatchFraction(0.8))
.setK(numTopics)
.setMaxIterations(10)
.setDocConcentration(-1) // use default values
.setTopicConcentration(-1) // use default values

// COMMAND ----------

// MAGIC %md
// MAGIC #### How to find what the default values are?
// MAGIC 
// MAGIC Dive into the source!!!
// MAGIC 
// MAGIC 1. Let's find the default value for `docConcentration` now.
// MAGIC 1. Got to Apache Spark package Root: [https://spark.apache.org/docs/latest/api/scala/#package](https://spark.apache.org/docs/latest/api/scala/#package)
// MAGIC * search for 'ml' in the search box on the top left (ml is for ml library)
// MAGIC * Then find the `LDA` by scrolling below on the left to mllib's `clustering` methods and click on `LDA`
// MAGIC * Then click on the source code link which should take you here:
// MAGIC   * [https://github.com/apache/spark/blob/v1.6.1/mllib/src/main/scala/org/apache/spark/ml/clustering/LDA.scala](https://github.com/apache/spark/blob/v1.6.1/mllib/src/main/scala/org/apache/spark/ml/clustering/LDA.scala)
// MAGIC   * Now, simply go to the right function and see the following comment block:
// MAGIC   
// MAGIC   ```
// MAGIC   /**
// MAGIC    * Concentration parameter (commonly named "alpha") for the prior placed on documents'
// MAGIC    * distributions over topics ("theta").
// MAGIC    *
// MAGIC    * This is the parameter to a Dirichlet distribution, where larger values mean more smoothing
// MAGIC    * (more regularization).
// MAGIC    *
// MAGIC    * If not set by the user, then docConcentration is set automatically. If set to
// MAGIC    * singleton vector [alpha], then alpha is replicated to a vector of length k in fitting.
// MAGIC    * Otherwise, the [[docConcentration]] vector must be length k.
// MAGIC    * (default = automatic)
// MAGIC    *
// MAGIC    * Optimizer-specific parameter settings:
// MAGIC    *  - EM
// MAGIC    *     - Currently only supports symmetric distributions, so all values in the vector should be
// MAGIC    *       the same.
// MAGIC    *     - Values should be > 1.0
// MAGIC    *     - default = uniformly (50 / k) + 1, where 50/k is common in LDA libraries and +1 follows
// MAGIC    *       from Asuncion et al. (2009), who recommend a +1 adjustment for EM.
// MAGIC    *  - Online
// MAGIC    *     - Values should be >= 0
// MAGIC    *     - default = uniformly (1.0 / k), following the implementation from
// MAGIC    *       [[https://github.com/Blei-Lab/onlineldavb]].
// MAGIC    * @group param
// MAGIC    */
// MAGIC   ```

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC **HOMEWORK:** Try to find the default value for `TopicConcentration`.

// COMMAND ----------

// Create LDA model with stopwords refiltered
val new_ldaModel = new_lda.run(new_lda_countVector)

// COMMAND ----------

val topicIndices = new_ldaModel.describeTopics(maxTermsPerTopic = 5)
val vocabList = vectorizer.vocabulary
val topics = topicIndices.map { case (terms, termWeights) =>
  terms.map(vocabList(_)).zip(termWeights)
}
println(s"$numTopics topics:")
topics.zipWithIndex.foreach { case (topic, i) =>
  println(s"TOPIC $i")
  topic.foreach { case (term, weight) => println(s"$term\t$weight") }
  println(s"==========")
}

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 9. Create LDA model with Expectation Maximization
// MAGIC 
// MAGIC Let's try creating an LDA model with Expectation Maximization on the data that has been refiltered for additional stopwords. We will also increase MaxIterations here to 100 to see if that improves results.

// COMMAND ----------

displayHTML(frameIt("http://spark.apache.org/docs/latest/mllib-clustering.html#latent-dirichlet-allocation-lda",800))

// COMMAND ----------

import org.apache.spark.mllib.clustering.EMLDAOptimizer

// Set LDA parameters
val em_lda = new LDA()
.setOptimizer(new EMLDAOptimizer())
.setK(numTopics)
.setMaxIterations(100)
.setDocConcentration(-1) // use default values
.setTopicConcentration(-1) // use default values

// COMMAND ----------

val em_ldaModel = em_lda.run(new_lda_countVector)

// COMMAND ----------

 import org.apache.spark.mllib.clustering.DistributedLDAModel;
val em_DldaModel = em_ldaModel.asInstanceOf[DistributedLDAModel]

// COMMAND ----------

val top10ConversationsPerTopic = em_DldaModel.topDocumentsPerTopic(10)

// COMMAND ----------

top10ConversationsPerTopic.length // number of topics

// COMMAND ----------

//em_DldaModel.topicDistributions.take(10).foreach(println)

// COMMAND ----------

// MAGIC %md
// MAGIC Note that the EMLDAOptimizer produces a DistributedLDAModel, which stores not only the inferred topics but also the full training corpus and topic distributions for each document in the training corpus.

// COMMAND ----------

val topicIndices = em_ldaModel.describeTopics(maxTermsPerTopic = 5)

// COMMAND ----------

val vocabList = vectorizer.vocabulary

// COMMAND ----------

vocabList.size

// COMMAND ----------

val topics = topicIndices.map { case (terms, termWeights) =>
  terms.map(vocabList(_)).zip(termWeights)
}

// COMMAND ----------

vocabList(47) // 47 is the index of the term 'university' or the first term in topics - this may change due to randomness in algorithm

// COMMAND ----------

// MAGIC %md
// MAGIC This is just doing it all at once.

// COMMAND ----------

val topicIndices = em_ldaModel.describeTopics(maxTermsPerTopic = 5)
val vocabList = vectorizer.vocabulary
val topics = topicIndices.map { case (terms, termWeights) =>
  terms.map(vocabList(_)).zip(termWeights)
}
println(s"$numTopics topics:")
topics.zipWithIndex.foreach { case (topic, i) =>
  println(s"TOPIC $i")
  topic.foreach { case (term, weight) => println(s"$term\t$weight") }
  println(s"==========")
}

// COMMAND ----------

top10ConversationsPerTopic(2)

// COMMAND ----------

top10ConversationsPerTopic(2)._1

// COMMAND ----------

val scenesForTopic2 = sc.parallelize(top10ConversationsPerTopic(2)._1).toDF("id")

// COMMAND ----------

display(scenesForTopic2.join(corpusDF,"id"))

// COMMAND ----------

sc.parallelize(top10ConversationsPerTopic(2)._1).toDF("id").join(corpusDF,"id").show(10,false)

// COMMAND ----------

sc.parallelize(top10ConversationsPerTopic(5)._1).toDF("id").join(corpusDF,"id").show(10,false)

// COMMAND ----------

corpusDF.show(5)

// COMMAND ----------

// MAGIC %md 
// MAGIC We've managed to get some good results here. For example, we can easily infer that Topic 2 is about space, Topic 3 is about israel, etc. 
// MAGIC 
// MAGIC 
// MAGIC We still get some ambiguous results like Topic 0.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC To improve our results further, we could employ some of the below methods:
// MAGIC - Refilter data for additional data-specific stopwords
// MAGIC - Use Stemming or Lemmatization to preprocess data
// MAGIC - Experiment with a smaller number of topics, since some of these topics in the 20 Newsgroups are pretty similar
// MAGIC - Increase model's MaxIterations

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Visualize Results
// MAGIC 
// MAGIC We will try visualizing the results obtained from the EM LDA model with a d3 bubble chart.

// COMMAND ----------

// Zip topic terms with topic IDs
val termArray = topics.zipWithIndex

// COMMAND ----------

// Transform data into the form (term, probability, topicId)
val termRDD = sc.parallelize(termArray)
val termRDD2 =termRDD.flatMap( (x: (Array[(String, Double)], Int)) => {
  val arrayOfTuple = x._1
  val topicId = x._2
  arrayOfTuple.map(el => (el._1, el._2, topicId))
})

// COMMAND ----------

// Create DF with proper column names
val termDF = termRDD2.toDF.withColumnRenamed("_1", "term").withColumnRenamed("_2", "probability").withColumnRenamed("_3", "topicId")

// COMMAND ----------

display(termDF)

// COMMAND ----------

// MAGIC %md 
// MAGIC We will convert the DataFrame into a JSON format, which will be passed into d3.

// COMMAND ----------

// Create JSON data
val rawJson = termDF.toJSON.collect().mkString(",\n")

// COMMAND ----------

// MAGIC %md 
// MAGIC We are now ready to use D3 on the rawJson data.

// COMMAND ----------

displayHTML(s"""
<!DOCTYPE html>
<meta charset="utf-8">
<style>

circle {
  fill: rgb(31, 119, 180);
  fill-opacity: 0.5;
  stroke: rgb(31, 119, 180);
  stroke-width: 1px;
}

.leaf circle {
  fill: #ff7f0e;
  fill-opacity: 1;
}

text {
  font: 14px sans-serif;
}

</style>
<body>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js"></script>
<script>

var json = {
 "name": "data",
 "children": [
  {
     "name": "topics",
     "children": [
      ${rawJson}
     ]
    }
   ]
};

var r = 1500,
    format = d3.format(",d"),
    fill = d3.scale.category20c();

var bubble = d3.layout.pack()
    .sort(null)
    .size([r, r])
    .padding(1.5);

var vis = d3.select("body").append("svg")
    .attr("width", r)
    .attr("height", r)
    .attr("class", "bubble");

  
var node = vis.selectAll("g.node")
    .data(bubble.nodes(classes(json))
    .filter(function(d) { return !d.children; }))
    .enter().append("g")
    .attr("class", "node")
    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    color = d3.scale.category20();
  
  node.append("title")
      .text(function(d) { return d.className + ": " + format(d.value); });

  node.append("circle")
      .attr("r", function(d) { return d.r; })
      .style("fill", function(d) {return color(d.topicName);});

var text = node.append("text")
    .attr("text-anchor", "middle")
    .attr("dy", ".3em")
    .text(function(d) { return d.className.substring(0, d.r / 3)});
  
  text.append("tspan")
      .attr("dy", "1.2em")
      .attr("x", 0)
      .text(function(d) {return Math.ceil(d.value * 10000) /10000; });

// Returns a flattened hierarchy containing all leaf nodes under the root.
function classes(root) {
  var classes = [];

  function recurse(term, node) {
    if (node.children) node.children.forEach(function(child) { recurse(node.term, child); });
    else classes.push({topicName: node.topicId, className: node.term, value: node.probability});
  }

  recurse(null, root);
  return {children: classes};
}
</script>
""")

// COMMAND ----------

// MAGIC %md 
// MAGIC ## Step 1. Downloading and Loading Data into DBFS
// MAGIC 
// MAGIC Here are the steps taken for downloading and saving data to the distributed file system.  Uncomment them for repeating this process on your databricks cluster or for downloading a new source of data.
// MAGIC 
// MAGIC Unfortunately, the original data at:
// MAGIC 
// MAGIC * [http://www.mpi-sws.org/~cristian/data/cornell_movie_dialogs_corpus.zip](http://www.mpi-sws.org/~cristian/data/cornell_movie_dialogs_corpus.zip) 
// MAGIC 
// MAGIC is not suited for manipulation and loading into dbfs easily. So the data has been downloaded, directory renamed without white spaces, superfluous OS-specific files removed, `dos2unix`'d, `tar -zcvf`'d and uploaded to the following URL for an easily dbfs-loadable download:
// MAGIC 
// MAGIC * [http://lamastex.org/datasets/public/nlp/cornell_movie_dialogs_corpus.tgz](http://lamastex.org/datasets/public/nlp/cornell_movie_dialogs_corpus.tgz)

// COMMAND ----------

//%sh wget http://lamastex.org/datasets/public/nlp/cornell_movie_dialogs_corpus.tgz

// COMMAND ----------

// MAGIC %md 
// MAGIC Untar the file.

// COMMAND ----------

//%sh tar zxvf cornell_movie_dialogs_corpus.tgz

// COMMAND ----------

// MAGIC %md
// MAGIC Let us list and load all the files into dbfs after `dbfs.fs.mkdirs(...)` to create the directory `dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/`.

// COMMAND ----------

//%sh pwd && ls -al cornell_movie_dialogs_corpus

// COMMAND ----------

/*
dbutils.fs.cp("file:///databricks/driver/cornell_movie_dialogs_corpus/movie_characters_metadata.txt","dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/")
dbutils.fs.cp("file:///databricks/driver/cornell_movie_dialogs_corpus/movie_conversations.txt","dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/")
dbutils.fs.cp("file:///databricks/driver/cornell_movie_dialogs_corpus/movie_lines.txt","dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/")
dbutils.fs.cp("file:///databricks/driver/cornell_movie_dialogs_corpus/movie_titles_metadata.txt","dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/")
dbutils.fs.cp("file:///databricks/driver/cornell_movie_dialogs_corpus/raw_script_urls.txt","dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/")
dbutils.fs.cp("file:///databricks/driver/cornell_movie_dialogs_corpus/README.txt","dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/")
*/

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/sds/nlp/cornell_movie_dialogs_corpus/"))

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)