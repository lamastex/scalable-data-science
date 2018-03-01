[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Twitter Streaming Language Classifier
=====================================

This is a databricksification of <https://databricks.gitbooks.io/databricks-spark-reference-applications/content/twitter_classifier/index.html> by Amendra Shreshta.

Note that you need to change the fields in background notebooks like `025_a_extendedTwitterUtils2run` as explained in the corresponding videos by Amendra.

``` run
"scalable-data-science/sds-2-2/025_a_extendedTwitterUtils2run"
```

>     import twitter4j._
>     import twitter4j.auth.Authorization
>     import twitter4j.conf.ConfigurationBuilder
>     import twitter4j.auth.OAuthAuthorization
>     import org.apache.spark.streaming._
>     import org.apache.spark.streaming.dstream._
>     import org.apache.spark.storage.StorageLevel
>     import org.apache.spark.streaming.receiver.Receiver

``` scala
import org.apache.spark._
import org.apache.spark.storage._
import org.apache.spark.streaming._

import scala.math.Ordering

import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
```

>     import org.apache.spark._
>     import org.apache.spark.storage._
>     import org.apache.spark.streaming._
>     import scala.math.Ordering
>     import twitter4j.auth.OAuthAuthorization
>     import twitter4j.conf.ConfigurationBuilder

>     defined class ExtendedTwitterReceiver

>     defined class ExtendedTwitterInputDStream

>     import twitter4j.Status
>     import twitter4j.auth.Authorization
>     import org.apache.spark.storage.StorageLevel
>     import org.apache.spark.streaming.StreamingContext
>     import org.apache.spark.streaming.dstream.{ReceiverInputDStream, DStream}
>     defined object ExtendedTwitterUtils

>     done running the extendedTwitterUtils2run notebook - ready to stream from twitter

``` scala
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

def MyconsumerKey       = "fB9Ww8Z4TIauPWKNPL6IN7xqd"
def MyconsumerSecret    = "HQqiIs3Yx3Mnv5gZTwQ6H2DsTlae4UNry5uNgylsonpFr46qXy"
def Mytoken             = "28513570-BfZrGoswVp1bz11mhwbVIGoJwjWCWgGoZGQXAqCO8"
def MytokenSecret       = "7fvag0GcXRlv42yBaVDMAmL1bmPyMZzNrqioMY7UwGbxr"

System.setProperty("twitter4j.oauth.consumerKey", MyconsumerKey)
System.setProperty("twitter4j.oauth.consumerSecret", MyconsumerSecret)
System.setProperty("twitter4j.oauth.accessToken", Mytoken)
System.setProperty("twitter4j.oauth.accessTokenSecret", MytokenSecret)
```

>     import twitter4j.auth.OAuthAuthorization
>     import twitter4j.conf.ConfigurationBuilder
>     MyconsumerKey: String
>     MyconsumerSecret: String
>     Mytoken: String
>     MytokenSecret: String
>     res1: String = null

``` scala
// Downloading tweets and building model for clustering
```

``` scala
// ## Let's create a directory in dbfs for storing tweets in the cluster's distributed file system.
val outputDirectoryRoot = "/datasets/tweetsStreamTmp" // output directory
```

>     outputDirectoryRoot: String = /datasets/tweetsStreamTmp

``` scala
// to remove a pre-existing directory and start from scratch uncomment next line and evaluate this cell
dbutils.fs.rm(outputDirectoryRoot, true) 
```

>     res2: Boolean = false

``` scala
// ## Capture tweets in every sliding window of slideInterval many milliseconds.
val slideInterval = new Duration(1 * 1000) // 1 * 1000 = 1000 milli-seconds = 1 sec
```

>     slideInterval: org.apache.spark.streaming.Duration = 1000 ms

``` scala
// Our goal is to take each RDD in the twitter DStream and write it as a json file in our dbfs.
// Create a Spark Streaming Context.
val ssc = new StreamingContext(sc, slideInterval)
```

>     ssc: org.apache.spark.streaming.StreamingContext = org.apache.spark.streaming.StreamingContext@3e2a07fd

``` scala
// Create a Twitter Stream for the input source. 
val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth)
```

>     auth: Some[twitter4j.auth.OAuthAuthorization] = Some(OAuthAuthorization{consumerKey='fB9Ww8Z4TIauPWKNPL6IN7xqd', consumerSecret='******************************************', oauthToken=AccessToken{screenName='null', userId=28513570}})
>     twitterStream: org.apache.spark.streaming.dstream.ReceiverInputDStream[twitter4j.Status] = ExtendedTwitterInputDStream@2acbe39c

``` scala
// Let's import google's json library next.
import com.google.gson.Gson 
//Let's map the tweets into json formatted string (one tweet per line).
val twitterStreamJson = twitterStream.map(
                                            x => { val gson = new Gson();
                                                 val xJson = gson.toJson(x)
                                                 xJson
                                                 }
                                          ) 
```

>     import com.google.gson.Gson
>     twitterStreamJson: org.apache.spark.streaming.dstream.DStream[String] = org.apache.spark.streaming.dstream.MappedDStream@616fc13d

``` scala
val partitionsEachInterval = 1 

val batchInterval = 1 // in minutes
val timeoutJobLength =  batchInterval * 5

var newContextCreated = false
var numTweetsCollected = 0L // track number of tweets collected

twitterStreamJson.foreachRDD((rdd, time) => { // for each filtered RDD in the DStream
      val count = rdd.count()
      if (count > 0) {
        val outputRDD = rdd.repartition(partitionsEachInterval) // repartition as desired
        // to write to parquet directly in append mode in one directory per 'time'------------       
        val outputDF = outputRDD.toDF("tweetAsJsonString")
        // get some time fields from current `.Date()`
        val year = (new java.text.SimpleDateFormat("yyyy")).format(new java.util.Date())
        val month = (new java.text.SimpleDateFormat("MM")).format(new java.util.Date())
        val day = (new java.text.SimpleDateFormat("dd")).format(new java.util.Date())
        val hour = (new java.text.SimpleDateFormat("HH")).format(new java.util.Date())
        // write to a file with a clear time-based hierarchical directory structure for example
        outputDF.write.mode(SaveMode.Append)
                .parquet(outputDirectoryRoot+ "/"+ year + "/" + month + "/" + day + "/" + hour + "/" + time.milliseconds) 
        // end of writing as parquet file-------------------------------------
        numTweetsCollected += count // update with the latest count
      }
  })
```

>     partitionsEachInterval: Int = 1
>     batchInterval: Int = 1
>     timeoutJobLength: Int = 5
>     newContextCreated: Boolean = false
>     numTweetsCollected: Long = 0

``` scala
// ## Let's start the spark streaming context we have created next.
ssc.start()
```

``` scala
// total tweets downloaded
numTweetsCollected
```

>     res13: Long = 1836

``` scala
// ## Go to SparkUI and see if a streaming job is already running. If so you need to terminate it before starting a new streaming job. Only one streaming job can be run on the DB CE.
// #  let's stop the streaming job next.
ssc.stop(stopSparkContext = false) 
StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 
```

``` run
"scalable-data-science/sds-2-2/025_b_TTTDFfunctions"
```

``` scala
// #Let's examine what was saved in dbfs
display(dbutils.fs.ls(outputDirectoryRoot))
```

| path                                 | name  | size |
|--------------------------------------|-------|------|
| dbfs:/datasets/tweetsStreamTmp/2017/ | 2017/ | 0.0  |

``` scala
// Replace the date with current date
val date = "/2017/11/*"
val rawDF = fromParquetFile2DF(outputDirectoryRoot + date +"/*/*") //.cache()
val TTTsDF = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(rawDF)).cache()
```

>     date: String = /2017/11/*
>     rawDF: org.apache.spark.sql.DataFrame = [tweetAsJsonString: string]
>     TTTsDF: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [CurrentTweetDate: timestamp, CurrentTwID: bigint ... 33 more fields]

``` scala
// Creating SQL table 
TTTsDF.createOrReplaceTempView("tbl_tweet")
```

``` scala
sqlContext.sql("SELECT lang, CPostUserName, CurrentTweet FROM tbl_tweet LIMIT 10").collect.foreach(println)
```

>     [ja,⚫️🌏⚪️NAT💲UKI⚪️🌏⚫️,RT @she_is_lie: https://t.co/aGTKqpjHva]
>     [en,☆Tesia☆D-1 WSD📌,Not that it matters but 38 minutes until I turn 18]
>     [ja,ミナモン🦄🌙,@clubj_ 値段もそれなりだしね💦]
>     [en,Pratik Raj IN,@ZeeNewsHindi Is apna muh bhi kala karwana chahiye Tha agar asal ki virodhi Hai to @MamataOfficial]
>     [ja,なお,もういや。]
>     [it,.,RT @baciamicoglione: m
>     i
>
>     p
>     i
>     a
>     c
>     e
>
>     l
>     a
>
>     f
>     i
>     g
>     a]
>     [en,Mehboob,RT @raheelrana: میاں صاحب 
>
>     تسی وی شریف تواڈے وکیل وی شریف 
>
>     خدا دا واسطہ جے
>     آپ اپنے آپ کو مت بدلو 
>     مگر اپنے وکیل کو بدل لو
>     جس طرح… ]
>     [en,きろゼロ🇸🇬,RT @jlist: When things are going really bad at work. https://t.co/0cqPLeKcPX]
>     [ja,なべどん,AFA in 台湾✩
>     ずっと描いてた初めての海外ランウェイ。
>     イベントに申込んだ時からワクワクが止まらない。
>     皆さんの1pt1ptを、力を貸してください。
>     https://t.co/GcrQYqJ1MP
>     #えんな
>     https://t.co/XsCIFqxWbQ]
>     [ja,ソト(8割空元気。),RT @Kono0425_ry: 愛知県の方気おつけて下さい。
>     車に傷つける(一本線)の被害が立て続けに起きてます。
>     自分の近所は安全だからと安心せずに保険に入ったりドラレコつけたりする事をオススメします。
>     見積もりだすと何十万、何百万です。… ]

``` scala
// Checking the language of tweets
sqlContext.sql(
    "SELECT lang, COUNT(*) as cnt FROM tbl_tweet " +
    "GROUP BY lang ORDER BY cnt DESC limit 1000")
    .collect.foreach(println)
```

>     [en,626]
>     [ja,513]
>     [ko,142]
>     [ar,107]
>     [es,94]
>     [pt,72]
>     [th,67]
>     [fr,49]
>     [tr,38]
>     [ru,31]
>     [it,18]
>     [ca,17]
>     [id,16]
>     [en-gb,13]
>     [de,13]
>     [zh-cn,10]
>     [nl,8]
>     [zh-CN,3]
>     [fi,3]
>     [sr,2]
>     [hu,2]
>     [el,2]
>     [zh-TW,2]
>     [en-GB,2]
>     [pl,2]
>     [vi,2]
>     [zh-tw,1]
>     [ro,1]
>     [hr,1]
>     [uk,1]
>     [bg,1]
>     [en-AU,1]
>     [zh-Hant,1]
>     [hi,1]
>     [da,1]

``` scala
// extracting just tweets from the table and converting it to String
val texts = sqlContext
      .sql("SELECT CurrentTweet from tbl_tweet")
      .map(_.toString)
```

>     texts: org.apache.spark.sql.Dataset[String] = [value: string]

``` scala
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}
```

>     import org.apache.spark.mllib.clustering.KMeans
>     import org.apache.spark.mllib.linalg.{Vector, Vectors}

``` scala
/*Create feature vectors by turning each tweet into bigrams of characters (an n-gram model)
and then hashing those to a length-1000 feature vector that we can pass to MLlib.*/

def featurize(s: String): Vector = {
  val n = 1000
  val result = new Array[Double](n)
  val bigrams = s.sliding(2).toArray
  for (h <- bigrams.map(_.hashCode % n)) {
    result(h) += 1.0 / bigrams.length
  }
  Vectors.sparse(n, result.zipWithIndex.filter(_._1 != 0).map(_.swap))
}
```

>     featurize: (s: String)org.apache.spark.mllib.linalg.Vector

``` scala
//Cache the vectors RDD since it will be used for all the KMeans iterations.
val vectors = texts.rdd
      .map(featurize)
      .cache()
```

>     vectors: org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.Vector] = MapPartitionsRDD[787] at map at command-2771931608832193:3

``` scala
// cache is lazy so count will force the data to store in memory
vectors.count()
```

>     res21: Long = 1863

``` scala
vectors.first()
```

>     res22: org.apache.spark.mllib.linalg.Vector = (1000,[50,53,56,78,96,99,100,180,189,226,285,325,340,350,356,358,370,438,453,488,504,525,554,573,578,587,615,623,626,636,642,660,669,679,708,712,755,830,845,903],[0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025,0.025])

``` scala
// Training model with 10 cluster and 10 iteration
val model = KMeans.train(vectors, k=10, maxIterations = 10)
```

>     model: org.apache.spark.mllib.clustering.KMeansModel = org.apache.spark.mllib.clustering.KMeansModel@616925c8

``` scala
// Sample 100 of the original set
val some_tweets = texts.take(100)
```

>     some_tweets: Array[String] =
>     Array([RT @she_is_lie: https://t.co/aGTKqpjHva], [Not that it matters but 38 minutes until I turn 18], [@clubj_ 値段もそれなりだしね💦], [@ZeeNewsHindi Is apna muh bhi kala karwana chahiye Tha agar asal ki virodhi Hai to @MamataOfficial], [もういや。], [RT @baciamicoglione: m
>     i
>
>     p
>     i
>     a
>     c
>     e
>
>     l
>     a
>
>     f
>     i
>     g
>     a], [RT @raheelrana: میاں صاحب
>
>     تسی وی شریف تواڈے وکیل وی شریف
>
>     خدا دا واسطہ جے
>     آپ اپنے آپ کو مت بدلو
>     مگر اپنے وکیل کو بدل لو
>     جس طرح… ], [RT @jlist: When things are going really bad at work. https://t.co/0cqPLeKcPX], [AFA in 台湾✩
>     ずっと描いてた初めての海外ランウェイ。
>     イベントに申込んだ時からワクワクが止まらない。
>     皆さんの1pt1ptを、力を貸してください。
>     https://t.co/GcrQYqJ1MP
>     #えんな
>     https://t.co/XsCIFqxWbQ], [RT @Kono0425_ry: 愛知県の方気おつけて下さい。
>     車に傷つける(一本線)の被害が立て続けに起きてます。
>     自分の近所は安全だからと安心せずに保険に入ったりドラレコつけたりする事をオススメします。
>     見積もりだすと何十万、何百万です。… ], [カエル寄りのナメクジです
>
>     難解な腐女子 ～生命の数だけ性癖はある～ | かおもじ #pixivコミック https://t.co/UJOQWDqp58], [RT @whatgirIsIove: no offence to me but wtf am i doing], [RT @yuyu_d: #無言で過去絵をあげる見た人もやる https://t.co/UFiaVVfHcj], [(فلما جاء أمرنا جعلنا عاليها سافلها وأمطرنا عليها حجارة من سجيل منضود) [هود:82] https://t.co/HTLfiMcgb3], [ربي أعوذ بك من الكسل وسوء الكبر  https://t.co/jCbc2qxOlI], [RT @bellyinsmile: กล่อมน้องนอน #ชูใจ https://t.co/XmIecEtLLh], [RT @chortletown: 💵Pledge 4my life
>     😍ADOPT or FOSTER me
>     ❤️me pls
>     #A299815
>     https://t.co/IBTU2T7EkE  #memphis via https://t.co/P7SzHSaTOA https…], [RT @yu_mene: ทีมงานบอกให้รอแบบสบายๆ 😂 https://t.co/bueHSWEqlc], [RT @fukumenkeimovie: ＼14日と15日の夜はLINE LIVE／
>     志尊さん＆小関さんの「#さしめし」😋❤️
>     📱視聴予約をお忘れなく🎸キラキラ
>
>     #覆面系ノイズ https://t.co/No46H6mOgq], [RT @Nopinochos: @joluga68 @CiudadanosCs @policia @guardiacivil @HoraJaen @jusapol #equiparacionya], [I'm at 名師橋 in Nagoya-shi, 愛知県 https://t.co/JAvvHX85nt], [RT @KylesHotTakes: And Doc Halladay grounds out to end the inning], [تقولي ما تستحين على وجهج مصبغة أظافر رجولج شنو يعني اغراء ؟!
>     اذبحها], [bla bla bla https://t.co/1VmXZk9rRH], [@Aghaye_Biikhat اره دیدم😃], [@jalalaeddine @S_ALKarashi سبحان الله
>     تدافع عن الصوفية وحزب البعث الإشتراكي...
>     وتريد منا أن نقبل كلامك؟], [@Kiekkokirja Livetuloksista. En oo 100% varma onko tuo totta kun siellä on joskus väärää tietoa esim. kokoonpanoissa], [ガチマでは
>     ホコが勝てない(T^T)
>
>     リグマだとフレンドさんが
>     心強くて勝てますが(っ´ω`c)], [@carol19761112 お頼みして描いて頂きました☺️可愛いですよね✨], [@kero_hugu 😂残念！
>     あなたには特別クーポンをさしあげます❤
>     特別クーポンで、KFC秘伝の香りを楽しんでね🍗
>
>     明日もキャンペーンに参加して「チキンの香り？の入浴剤」をゲットしよう👍 #KFC https://t.co/1ESHjKp7di https://t.co/RPqtuDwyNE], [tesudo que só https://t.co/66yaIRfI6W], [RT @ciilat_gko: 訂正と補足。免疫が「発達した」は「低下した」の間違いですごめんなさい！
>     詳しく言うと、狩猟時代に男性のケガや病気が増加→免疫が過剰反応し、自己免疫異常が発生→進化の過程で男性の免疫システムが非活動的に変化した…という流れです。うろ覚えで文がおかし…], [RT @ktrsngofficial: フォロワー100万人突破！皆さまのパワーでありがとうございます！のんびり楽しんでイキましょう！
>     インスタグラマー香取慎吾！
>     本格始動です！
>     ハロー
>
>     #ホンネロス https://t.co/PDPINoUQPI], [My bunkmates now dw give me a new stick after i break it in half 😂😂😂😂], [RT @oz_il11: タオル全メンバー全完売は草 https://t.co/IgqiUtOPMS], [RT @Jawara_B411: Menyikapi kabar pembubaran Dakwah yang ramaikan diperbincangkan belakangan ini.
>     Kami dari DPP BRIGADE JAWARA BETAWI… ], [RT @euneun13st: แต่งใหญ่สุด หมายถึงชุดใหญ่สุด เข้าประตูมายังไงนิ https://t.co/ct3krCx0Hr], [RT @rolctSsnyBfVNAi: 무닌😭 https://t.co/GNceC6XwVd], [RT @serikon_mha: ヒーローは神ではない https://t.co/SLmxMMk6v2], [@_A_n_d_r_e_w_s @BoobPunchTina Rut-Roh. Set much of my first novel in #Newfoundland  I may be in deep trouble, puffin-wise...
>     #amwriting], [@gxgarea Temenin aku juga....], [@nyanmarubl にゃんちゃんたらwwwそうそう、お願い決まった？], [@geahcintun เป็นอยู่ทุกวัน..], [RT @Tumyad4: Sabah 6 da büyük kızı okula bırakıyor öglene kadar kagıt topluyor heryer arap, afgan ,pakistanlı dolu abi inşaata g… ], [(햇는데 아버지랑 떠야하는건아니겟지;)], [RT @thebtsmutuals: rt if u love min yoongi , follow whoever rts 🍁], [RT @DepreFogo: Hoje
>     Tem
>     Jogo
>     Do
>     Botafogo
>     O
>     Glorioso
>     É
>     O
>     Meu
>     Grande
>     Amor
>     Te
>     Amo
>     Fogo
>     🔥], [RT @Sanchovies: was dming this girl but I had to block her lol just seen she had "Matthew 6:8" in her bio no way I can compete with a fella…], [miga sai dessa que é furada, confia na tia], [@feiler_wt @marum0t1 ダメです https://t.co/biA9YW0Jof], [@ron_im_iz Will do :)], [RT #MPN #BadLiar #SelenaGomezInstagram https://t.co/zoj3xMZqrI], [RT @baejinyoung_00: 진영이 압구정역 광고 너무 예뻐💧
>     #배진영 #BAEJINYOUNG https://t.co/5iA3Wp8Eux], [ポチれよ], [RT @sapinker: Augmented Reality Glasses for Autism | Innovative new tech for kids, aids emotions, self-regulation, more. Indiegogo https://…], [Balkanci u Nemačkoj za pet godina zarade novac za koji u svojoj zemlji rade ceo život
>     https://t.co/c6vhcoa2zu], [alguien me mata para no ir al colegio], [RT @Story_terror: はいかわいい #cingeki https://t.co/gZFG9I9FmM], [古戦場はゆるゆるやる。
>     できればガチャ回しまくりたい], [RT @proofhealth: Proof #blockchain and Health Minister of Ontario, Dr. Eric Hoskins discuss #smartcontracts #privacy #security… ], [RT @GLove39: Siri, show me a visual metaphor for tax avoidance
>     #ParadisePapers https://t.co/wVdSy7QtMZ], [RT @Fnac: JEU CONCOURS: À l'occasion du #SalonPhotoParis qui débute demain, tente de gagner le @FujiInstaxFR mini 9 bleu givr… ], [https://t.co/0VPlrWxm0a], [RT @twittakarai: 今月発売のDear+でテンカウントが最終話となります(•ㅿ•.)(ㅍ_ㅍ)
>     4年強の間、応援のリプライやお手紙ややさしいイラストなどに心が救われる思いでした。本当にありがとうございました！
>     最終巻の6巻は春ごろ発売になります。ど… ], [@diary_ER1N_ 갠차노], [.明月不谙离恨苦，斜光到晓穿朱户.－晏殊《鹊踏枝》], [RT @_Tazmxni: Mon pote il m'accompagne au magasin et pour l'remercier j'lui dis prend un bail, ce fou il avait l'intention d'acheter une co…], [RT @taekookmoments: can't believe taekook checked what letter each other got to see which team they're in wHAT 👀👀 https://t.co/dsNi9QLzJS], [RT @famichikisenpai: #どん兵衛こわい
>     変なネタいっぱい送ってきた…皆さん早くもらってください → フォロー&RTで合計3万名様にどん兵衛プレゼント。2日目は9日11:59まで
>     #ファミどん なんて知らない
>     https://t.co/Mmvr5BeIzV h…], [jwu], [씻고오겟슴다], [RT @ksngtysofficial: ###ご機嫌斜め！@##
>     #ユーチューバー草彅 #ホンネテレビ https://t.co/ySvYTr4z52], [RT @mybraceteeth: คนไข้ผมวันนี้ 27ปี ฟันหน้ากร่อนเป็นหยักๆๆ ซักประวัติพบว่า ชอบดื่มน้ำอัดลมมาตั้งแต่เด็ก ดื่มแล้วจะจิบๆบริเวณฟันหน้า… ], [Bluetoothイヤホンなくした、、、こいつぁショックでかめ_(-ω-`_)⌒)_], [Sendo fofas na Disneyland!! 🎢🏰😍Apenas as melhores!! Já estou com saudades!! 😭😭😩 #disneyland… https://t.co/avY5bVcSmW], [RT @Diamond_97jk: 171104 WINGS Macau
>     #방타소년단 #BTS #정국 #JUNGKOOK https://t.co/qZFQOYGA09], [2 MCs are so done 😂their face lol], [RT @propositey: sou adolescente e odeio adolescente na adolescência], [RT @vthomasm: Corrupción. La caja B del PP. El policía que destapó Gürtel acusa al PP de haber intentado “desestabilizar la investigación”…], [RT @CrushOn2226: 20171107 #MONSTA_X  #몬스타엑스  #아이엠 #IM #기현 #KIHYUN #DRAMARAMA #드라마라마 @OfficialMonstaX SHOW CON DRAMARAMA 기현 focus ful… ], [RT @rcabrero75: Y Rivera y Pedro Sánchez ande andarán..  Ensordecedor silencio ante la gravedad de lo que ha ocurrido hoy en el Con… ], [@henriquemenes @oretalha ps4 mo ruim], [好み似てるところある], [@hayashida_sannn 元気もらえる😄], [(وإن الذين لا يؤمنون بالآخرة عن الصراط لناكبون) [المؤمنون:74] https://t.co/fRdUMQnNOD], [RT @PostMonstaX: [INFO] Hoje, às 8 da manhã (Horário de Brasília) vai ao ar o ShowChampion, sendo este o primeiro stage de… ], [Check out what I'm selling on my @depop shop 💸 https://t.co/Rkr3CFf14D], [RT @saku93: doaxシリーズ女天狗
>     #いいおっぱいの日 https://t.co/EMsvtdnWh3], [Vou volta a dormir muito tô cheio de sono], [RT @JosPastr: Por fin los Mossos hacen algo: proteger a los piquetes ante quien se atreva a encararse con ellos. https://t.co/tc5BLBCwKu], [RT @RaiLetteratura: 170 anni fa nasceva  Bram #Stoker autore di #Dracula il celeberrimo romanzo #gotico, ne parla Alessandro Zaccuri… ], [RT @TH3WH17ERABB17: What must occur to allow for civilian trials?
>     #followthewhiterabbit
>     🐇 https://t.co/MlGOGwp0e9], [@kr1h_ アーラシュくんが57で止まってる悲しい現実], [@valerie_expert @SophiePendevill @ARTEfr Julie Peyrard "L'accord pour le namming du musée cours pour 30 ans et a coûté 1 milliard tout accords compris." #louvreabudhabi], [パクりました
>     ドーンガード暁の護衛説 https://t.co/2hX7gDq3Xc], [デザイン気になるね😌 https://t.co/Oz1WbFpgYb], [#Criciuma #PracaChamine #Centenario #IoT #SSP #Comunidade https://t.co/uCVYeB7aZc], [[MV] 어반자카파 - 그때의 나, 그때의 우리 https://t.co/xL6snkfhho https://t.co/WfA8UslpS0], [나 빱빠YA ㅅㅏ랑함 https://t.co/SwnvJhU9ff], [JKの新教祖？イタイ歌詞が人気の歌手「阿部真央」])

``` scala
// iterate through the 100 samples and show which cluster they are in
for (i <- 0 until 10) {
  println(s"\nCLUSTER $i:")
  some_tweets.foreach { t =>
    if (model.predict(featurize(t)) == i) {
      println(t)
    }
  }
}
```

>     CLUSTER 0:
>     [RT @she_is_lie: https://t.co/aGTKqpjHva]
>     [RT @jlist: When things are going really bad at work. https://t.co/0cqPLeKcPX]
>     [AFA in 台湾✩
>     ずっと描いてた初めての海外ランウェイ。
>     イベントに申込んだ時からワクワクが止まらない。
>     皆さんの1pt1ptを、力を貸してください。
>     https://t.co/GcrQYqJ1MP
>     #えんな
>     https://t.co/XsCIFqxWbQ]
>     [カエル寄りのナメクジです
>
>     難解な腐女子 ～生命の数だけ性癖はある～ | かおもじ #pixivコミック https://t.co/UJOQWDqp58]
>     [RT @yuyu_d: #無言で過去絵をあげる見た人もやる https://t.co/UFiaVVfHcj]
>     [(فلما جاء أمرنا جعلنا عاليها سافلها وأمطرنا عليها حجارة من سجيل منضود) [هود:82] https://t.co/HTLfiMcgb3]
>     [ربي أعوذ بك من الكسل وسوء الكبر  https://t.co/jCbc2qxOlI]
>     [RT @bellyinsmile: กล่อมน้องนอน #ชูใจ https://t.co/XmIecEtLLh]
>     [RT @chortletown: 💵Pledge 4my life
>     😍ADOPT or FOSTER me
>     ❤️me pls
>     #A299815
>     https://t.co/IBTU2T7EkE  #memphis via https://t.co/P7SzHSaTOA https…]
>     [RT @yu_mene: ทีมงานบอกให้รอแบบสบายๆ 😂 https://t.co/bueHSWEqlc]
>     [I'm at 名師橋 in Nagoya-shi, 愛知県 https://t.co/JAvvHX85nt]
>     [bla bla bla https://t.co/1VmXZk9rRH]
>     [@kero_hugu 😂残念！
>     あなたには特別クーポンをさしあげます❤
>     特別クーポンで、KFC秘伝の香りを楽しんでね🍗
>
>     明日もキャンペーンに参加して「チキンの香り？の入浴剤」をゲットしよう👍 #KFC https://t.co/1ESHjKp7di https://t.co/RPqtuDwyNE]
>     [tesudo que só https://t.co/66yaIRfI6W]
>     [RT @oz_il11: タオル全メンバー全完売は草 https://t.co/IgqiUtOPMS]
>     [RT @euneun13st: แต่งใหญ่สุด หมายถึงชุดใหญ่สุด เข้าประตูมายังไงนิ https://t.co/ct3krCx0Hr]
>     [RT @rolctSsnyBfVNAi: 무닌😭 https://t.co/GNceC6XwVd]
>     [RT @serikon_mha: ヒーローは神ではない https://t.co/SLmxMMk6v2]
>     [@feiler_wt @marum0t1 ダメです https://t.co/biA9YW0Jof]
>     [RT #MPN #BadLiar #SelenaGomezInstagram https://t.co/zoj3xMZqrI]
>     [RT @baejinyoung_00: 진영이 압구정역 광고 너무 예뻐💧
>     #배진영 #BAEJINYOUNG https://t.co/5iA3Wp8Eux]
>     [RT @Story_terror: はいかわいい #cingeki https://t.co/gZFG9I9FmM]
>     [https://t.co/0VPlrWxm0a]
>     [RT @ksngtysofficial: ###ご機嫌斜め！@##
>     #ユーチューバー草彅 #ホンネテレビ https://t.co/ySvYTr4z52]
>     [RT @Diamond_97jk: 171104 WINGS Macau 
>     #방타소년단 #BTS #정국 #JUNGKOOK https://t.co/qZFQOYGA09]
>     [(وإن الذين لا يؤمنون بالآخرة عن الصراط لناكبون) [المؤمنون:74] https://t.co/fRdUMQnNOD]
>     [Check out what I'm selling on my @depop shop 💸 https://t.co/Rkr3CFf14D]
>     [RT @saku93: doaxシリーズ女天狗
>     #いいおっぱいの日 https://t.co/EMsvtdnWh3]
>     [パクりました
>     ドーンガード暁の護衛説 https://t.co/2hX7gDq3Xc]
>     [デザイン気になるね😌 https://t.co/Oz1WbFpgYb]
>     [#Criciuma #PracaChamine #Centenario #IoT #SSP #Comunidade https://t.co/uCVYeB7aZc]
>     [[MV] 어반자카파 - 그때의 나, 그때의 우리 https://t.co/xL6snkfhho https://t.co/WfA8UslpS0]
>     [나 빱빠YA ㅅㅏ랑함 https://t.co/SwnvJhU9ff]
>
>     CLUSTER 1:
>     [RT @baciamicoglione: m
>     i
>
>     p
>     i
>     a
>     c
>     e
>
>     l
>     a
>
>     f
>     i
>     g
>     a]
>     [RT @raheelrana: میاں صاحب 
>
>     تسی وی شریف تواڈے وکیل وی شریف 
>
>     خدا دا واسطہ جے
>     آپ اپنے آپ کو مت بدلو 
>     مگر اپنے وکیل کو بدل لو
>     جس طرح… ]
>     [RT @Kono0425_ry: 愛知県の方気おつけて下さい。
>     車に傷つける(一本線)の被害が立て続けに起きてます。
>     自分の近所は安全だからと安心せずに保険に入ったりドラレコつけたりする事をオススメします。
>     見積もりだすと何十万、何百万です。… ]
>     [RT @fukumenkeimovie: ＼14日と15日の夜はLINE LIVE／
>     志尊さん＆小関さんの「#さしめし」😋❤️
>     📱視聴予約をお忘れなく🎸キラキラ
>
>     #覆面系ノイズ https://t.co/No46H6mOgq]
>     [تقولي ما تستحين على وجهج مصبغة أظافر رجولج شنو يعني اغراء ؟! 
>     اذبحها]
>     [@jalalaeddine @S_ALKarashi سبحان الله
>     تدافع عن الصوفية وحزب البعث الإشتراكي...
>     وتريد منا أن نقبل كلامك؟]
>     [ガチマでは
>     ホコが勝てない(T^T)
>
>     リグマだとフレンドさんが
>     心強くて勝てますが(っ´ω`c)]
>     [@carol19761112 お頼みして描いて頂きました☺️可愛いですよね✨]
>     [RT @ciilat_gko: 訂正と補足。免疫が「発達した」は「低下した」の間違いですごめんなさい！
>     詳しく言うと、狩猟時代に男性のケガや病気が増加→免疫が過剰反応し、自己免疫異常が発生→進化の過程で男性の免疫システムが非活動的に変化した…という流れです。うろ覚えで文がおかし…]
>     [RT @ktrsngofficial: フォロワー100万人突破！皆さまのパワーでありがとうございます！のんびり楽しんでイキましょう！
>     インスタグラマー香取慎吾！
>     本格始動です！
>     ハロー
>
>     #ホンネロス https://t.co/PDPINoUQPI]
>     [@geahcintun เป็นอยู่ทุกวัน..]
>     [(햇는데 아버지랑 떠야하는건아니겟지;)]
>     [RT @DepreFogo: Hoje
>     Tem
>     Jogo
>     Do
>     Botafogo
>     O
>     Glorioso
>     É 
>     O
>     Meu
>     Grande
>     Amor
>     Te
>     Amo
>     Fogo
>     🔥]
>     [古戦場はゆるゆるやる。
>     できればガチャ回しまくりたい]
>     [RT @twittakarai: 今月発売のDear+でテンカウントが最終話となります(•ㅿ•.)(ㅍ_ㅍ) 
>     4年強の間、応援のリプライやお手紙ややさしいイラストなどに心が救われる思いでした。本当にありがとうございました！
>     最終巻の6巻は春ごろ発売になります。ど… ]
>     [@diary_ER1N_ 갠차노]
>     [.明月不谙离恨苦，斜光到晓穿朱户.－晏殊《鹊踏枝》]
>     [RT @famichikisenpai: #どん兵衛こわい
>     変なネタいっぱい送ってきた…皆さん早くもらってください → フォロー&RTで合計3万名様にどん兵衛プレゼント。2日目は9日11:59まで
>     #ファミどん なんて知らない
>     https://t.co/Mmvr5BeIzV h…]
>     [씻고오겟슴다]
>     [RT @mybraceteeth: คนไข้ผมวันนี้ 27ปี ฟันหน้ากร่อนเป็นหยักๆๆ ซักประวัติพบว่า ชอบดื่มน้ำอัดลมมาตั้งแต่เด็ก ดื่มแล้วจะจิบๆบริเวณฟันหน้า… ]
>     [Bluetoothイヤホンなくした、、、こいつぁショックでかめ_(-ω-`_)⌒)_]
>     [RT @CrushOn2226: 20171107 #MONSTA_X  #몬스타엑스  #아이엠 #IM #기현 #KIHYUN #DRAMARAMA #드라마라마 @OfficialMonstaX SHOW CON DRAMARAMA 기현 focus ful… ]
>     [JKの新教祖？イタイ歌詞が人気の歌手「阿部真央」]
>
>     CLUSTER 2:
>     [もういや。]
>
>     CLUSTER 3:
>
>     CLUSTER 4:
>
>     CLUSTER 5:
>     [jwu]
>
>     CLUSTER 6:
>     [Not that it matters but 38 minutes until I turn 18]
>     [@ZeeNewsHindi Is apna muh bhi kala karwana chahiye Tha agar asal ki virodhi Hai to @MamataOfficial]
>     [RT @whatgirIsIove: no offence to me but wtf am i doing]
>     [RT @Nopinochos: @joluga68 @CiudadanosCs @policia @guardiacivil @HoraJaen @jusapol #equiparacionya]
>     [RT @KylesHotTakes: And Doc Halladay grounds out to end the inning]
>     [@Kiekkokirja Livetuloksista. En oo 100% varma onko tuo totta kun siellä on joskus väärää tietoa esim. kokoonpanoissa]
>     [My bunkmates now dw give me a new stick after i break it in half 😂😂😂😂]
>     [RT @Jawara_B411: Menyikapi kabar pembubaran Dakwah yang ramaikan diperbincangkan belakangan ini.
>     Kami dari DPP BRIGADE JAWARA BETAWI… ]
>     [@_A_n_d_r_e_w_s @BoobPunchTina Rut-Roh. Set much of my first novel in #Newfoundland  I may be in deep trouble, puffin-wise...
>     #amwriting]
>     [@gxgarea Temenin aku juga....]
>     [RT @Tumyad4: Sabah 6 da büyük kızı okula bırakıyor öglene kadar kagıt topluyor heryer arap, afgan ,pakistanlı dolu abi inşaata g… ]
>     [RT @thebtsmutuals: rt if u love min yoongi , follow whoever rts 🍁]
>     [RT @Sanchovies: was dming this girl but I had to block her lol just seen she had "Matthew 6:8" in her bio no way I can compete with a fella…]
>     [miga sai dessa que é furada, confia na tia]
>     [RT @sapinker: Augmented Reality Glasses for Autism | Innovative new tech for kids, aids emotions, self-regulation, more. Indiegogo https://…]
>     [Balkanci u Nemačkoj za pet godina zarade novac za koji u svojoj zemlji rade ceo život
>     https://t.co/c6vhcoa2zu]
>     [alguien me mata para no ir al colegio]
>     [RT @proofhealth: Proof #blockchain and Health Minister of Ontario, Dr. Eric Hoskins discuss #smartcontracts #privacy #security… ]
>     [RT @GLove39: Siri, show me a visual metaphor for tax avoidance
>     #ParadisePapers https://t.co/wVdSy7QtMZ]
>     [RT @Fnac: JEU CONCOURS: À l'occasion du #SalonPhotoParis qui débute demain, tente de gagner le @FujiInstaxFR mini 9 bleu givr… ]
>     [RT @_Tazmxni: Mon pote il m'accompagne au magasin et pour l'remercier j'lui dis prend un bail, ce fou il avait l'intention d'acheter une co…]
>     [RT @taekookmoments: can't believe taekook checked what letter each other got to see which team they're in wHAT 👀👀 https://t.co/dsNi9QLzJS]
>     [Sendo fofas na Disneyland!! 🎢🏰😍Apenas as melhores!! Já estou com saudades!! 😭😭😩 #disneyland… https://t.co/avY5bVcSmW]
>     [2 MCs are so done 😂their face lol]
>     [RT @propositey: sou adolescente e odeio adolescente na adolescência]
>     [RT @vthomasm: Corrupción. La caja B del PP. El policía que destapó Gürtel acusa al PP de haber intentado “desestabilizar la investigación”…]
>     [RT @rcabrero75: Y Rivera y Pedro Sánchez ande andarán..  Ensordecedor silencio ante la gravedad de lo que ha ocurrido hoy en el Con… ]
>     [@henriquemenes @oretalha ps4 mo ruim]
>     [RT @PostMonstaX: [INFO] Hoje, às 8 da manhã (Horário de Brasília) vai ao ar o ShowChampion, sendo este o primeiro stage de… ]
>     [Vou volta a dormir muito tô cheio de sono]
>     [RT @JosPastr: Por fin los Mossos hacen algo: proteger a los piquetes ante quien se atreva a encararse con ellos. https://t.co/tc5BLBCwKu]
>     [RT @RaiLetteratura: 170 anni fa nasceva  Bram #Stoker autore di #Dracula il celeberrimo romanzo #gotico, ne parla Alessandro Zaccuri… ]
>     [RT @TH3WH17ERABB17: What must occur to allow for civilian trials?
>     #followthewhiterabbit
>     🐇 https://t.co/MlGOGwp0e9]
>     [@valerie_expert @SophiePendevill @ARTEfr Julie Peyrard "L'accord pour le namming du musée cours pour 30 ans et a coûté 1 milliard tout accords compris." #louvreabudhabi]
>
>     CLUSTER 7:
>
>     CLUSTER 8:
>     [@clubj_ 値段もそれなりだしね💦]
>     [@Aghaye_Biikhat اره دیدم😃]
>     [@nyanmarubl にゃんちゃんたらwwwそうそう、お願い決まった？]
>     [@ron_im_iz Will do :)]
>     [ポチれよ]
>     [好み似てるところある]
>     [@hayashida_sannn 元気もらえる😄]
>     [@kr1h_ アーラシュくんが57で止まってる悲しい現実]
>
>     CLUSTER 9:

``` scala
// to remove a pre-existing model and start from scratch
dbutils.fs.rm("/datasets/model", true) 
```

>     res24: Boolean = false

``` scala
// save the model
sc.makeRDD(model.clusterCenters).saveAsObjectFile("/datasets/model")
```

``` scala
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.clustering.KMeansModel
```

>     import org.apache.spark.mllib.clustering.KMeans
>     import org.apache.spark.mllib.linalg.{Vector, Vectors}
>     import org.apache.spark.mllib.clustering.KMeansModel

``` scala
// Checking if the model works
val clusterNumber = 5

val modelFile = "/datasets/model"

val model: KMeansModel = new KMeansModel(sc.objectFile[Vector](modelFile).collect)
model.predict(featurize("واحد صاحبى لو حد يعرف اكونت وزير التعليم ")) == clusterNumber
```

>     clusterNumber: Int = 5
>     modelFile: String = /datasets/model
>     model: org.apache.spark.mllib.clustering.KMeansModel = org.apache.spark.mllib.clustering.KMeansModel@4b53f956
>     res26: Boolean = false

>     USAGE: val df = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(fromParquetFile2DF("parquetFileName")))
>                       val df = tweetsDF2TTTDF(tweetsIDLong_JsonStringPairDF2TweetsDF(fromParquetFile2DF("parquetFileName")))
>                       
>     import org.apache.spark.sql.types.{StructType, StructField, StringType}
>     import org.apache.spark.sql.functions._
>     import org.apache.spark.sql.types._
>     import org.apache.spark.sql.ColumnName
>     import org.apache.spark.sql.DataFrame
>     fromParquetFile2DF: (InputDFAsParquetFilePatternString: String)org.apache.spark.sql.DataFrame
>     tweetsJsonStringDF2TweetsDF: (tweetsAsJsonStringInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame
>     tweetsIDLong_JsonStringPairDF2TweetsDF: (tweetsAsIDLong_JsonStringInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame
>     tweetsDF2TTTDF: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame
>     tweetsDF2TTTDFWithURLsAndHastags: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame

``` scala
// Loading model and printing tweets that matched the desired cluster
```

``` scala
var newContextCreated = false
var num = 0

// Create a Spark Streaming Context.
@transient val ssc = new StreamingContext(sc, slideInterval)
// Create a Twitter Stream for the input source. 
@transient val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
@transient val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth)

//Replace the cluster number as you desire between 0 to 9
val clusterNumber = 6

//model location
val modelFile = "/datasets/model"

// Get tweets from twitter
val Tweet = twitterStream.map(_.getText)
//Tweet.print()

println("Initalizaing the the KMeans model...")
val model: KMeansModel = new KMeansModel(sc.objectFile[Vector](modelFile).collect)

//printing tweets that match our choosen cluster
Tweet.foreachRDD(rdd => {  
rdd.collect().foreach(i =>
    {
       val record = i
       if (model.predict(featurize(record)) == clusterNumber) {
       println(record)
    }
    })
})

// Start the streaming computation
println("Initialization complete.")
ssc.start()
ssc.awaitTermination()
```

>     Initalizaing the the KMeans model...
>     Initialization complete.
>     @tiasakuraa_ @rnyx_x Yeahhhh geng. No more stress anymore. Let's happy2 till the end of this year 👰
>     RT @FlowerSree: “Go for it now...the future is promised to no one.”
>
>     ~Wayne Dyer~ https://t.co/t6XqmROnxg
>     Paket Nasi Putih 😋👍
>     Thanks ya Mba udh repeat orderannya😉😘
>     utk Thalilan Bapak… https://t.co/3T00NxVAxi
>     RT @davidschneider: Watch out, Brexiters! We Remoaners can force you to go to clandestine meetings then take control of your mouth and… 
>     @nitinkumarjdhav @nitin_gadkari @narendramodi @BJP4India @PMOIndia @PIB_India @MIB_India Bhai jio sim lene me hi 4 jaan chali gayi, Uska kya?
>     RT @xiskenderx: Nasılsınız? IYI'yim! İyiyim diyenler dikkat!
>      
>     ￼😀￼😀Bir kamyonun Çarpmasıyla yaralanmış olan çiftçi Mehmet amca... https://t…
>     RT @BoobsTitsOppai: @hondarb4p906i @OjifTakaoka @onitarou5678 @lcaligula @singlelife43 @EronaviInfo @JapaneseAVBabes @NaughtyTokyo… 
>     RT @sigCapricornio2: #Capricórnio "Ás vezes a vida perde a graça."
>     RT @buenos_nialles: jak harry nic nie wspomni o polsce mając taki widok to przechodze do fandomu krzysztofa krawczyka… 
>     RT @RakanMasjidUSM: Wahai mata, dah brp lama kau tidak menangis kerana memikirkan dosa, syurga, neraka & rahmat Dia?
>
>     Hati, kau apa khabar?…
>     RT @kiaralawhon: Toxic relationships hinder growth. & as women, we want to take care of our men and sometimes forget that we deserve… 
>     NTEC COMPACT MULTI PLAYER ADAPTOR - MULTI TAP - Brand new! For use with the PS2 https://t.co/g61m1o7CpF #ebay #gamer #gaming #games
>     RT @CaratLandPH: Seungcheol, Jeonghan & Jisoo riding a tricycle!🇵🇭 A concept PH-Carats live for.💖
>
>     “Kyah kyah penge pambarya”☺️ https://t.c…
>     @AndyMaherDFA @Telstra They are a genuine joke. Had a woman call me back twice because I couldn’t log into the Telstra app. She then advised me to seek help via the website live chat option. In the end I just thought fuck it, I don’t need the app. #easier
>     @iHyungseob_ Sa ae sia borokokok :( ,kangen ugha mwahh😝
>     2 math exams for the first week yEs polynomials !!!
>     RT @Assimalhakeem: You must at least make sure it is not pork you are eating. Avoiding it is best. https://t.co/k6VCgqWomd
>     RT @HQGatica: a @tvn le inyecten 47 millones de dólares y ni siquiera levantan crítica a la directiva por su pésima administración y sueldo…
>     RT @khalheda: g envie d’aller au bord d’un lac avc un beau garçon en décapotable et on regarde la lune et après jle noie et je pars avec la…
>     RT @SESAR_JU: J. Pie @ASDEurope how do we increase the appetite for European project for aviation? we need to work together to re… 
>     im gonna go ahead and say it..... kali in ST2 was done so dirty and should have had more screen time
>     There’s really no good reason not to do this https://t.co/uGO2Af5hTu
>     5分だった。結構近いな。
>     RT @zhukl: Haven't seen this kind of Islam in awhile. Welcome back. I missed you. https://t.co/7YuZ3iYvOG
>     @awlazel @Schroedinger99 @PsychFarmer @nick_gutteridge @valko665 If you're saying the EU would collectively boot out all Brits because we reject the ECJ, it says more about the EU than it does about the UK.
>     RT @maaripatriota: sinto sdd de umas fases da minha vida q eu rezava p passar logo e q hj em dia eu daria tudo p ter dnv
>     @Zoutdropje ik hou altijd 2 aan...
>
>     2 repen per persoon
>     @vladsavov Do you mind emailing me the first and the third in high-res? I would like to enjoy the full quality on my wallpaper. Thanks!
>     @missm_girl Se ainda fosse numas maminhas. Pfffff
>     @orangetwinspur Uu nga Ate C gusto ko na mag g!
>     Hip Hop Is Not Just "Rap".... It's A Whole Culture.
>     @Abyss_59_cide かちました
>     X el asma chota esta no dormi en toda la noche,pobres los que me tienen q ver la cara de culo
>     @Im_Myystic @unlikelyheroI
>     @thandamentalist dekho kya hota hai.
>     This site you can be purchased at cheap topic of PC games.
>     Discount code, there are deals such as the sale information.
>     https://t.co/UEpa4OHTpe
>     RT @ShaunKing: 38 year old Melvin Carter just became the first Black Mayor of St. Paul, Minnesota. https://t.co/rreMZAfBNa
>     @Cultura harry péteur ?
>     Yeah, I suck at RL but let's make a safer gaming community.. let's be a part of the solution not a part of the problem. …
>     RT @BUNDjugend: #pre2020 #climateactionsnow in front of entry to #BulaZone at #COP23 with @DClimateJustice and some oft us https://t.co/yTj…
>     RT @acgrayling: (2) But! - once the lunacy of Brexit is stopped, we can clean the Augean stables of our political order, & get our democrac…
>     Serious la lupa yang aku ni perempuan 😭
>     The Hornets are sadly not playing today so are off to do some bowling! 🐝 But Good Luck to all our @hudunisport teams today @HURLFC @UHRUFC @HULF1 @_HUFC @HUNetball @HuddsHawks @UoHHockey 💙 we'll see you all out later tonight 🍻 #huddersfield
>     Vroeger herkende je de Sint aan zijn schoenen en zwarte Piet aan zijn stem maar dat geheel terzijde. Er zijn daarentegen Pieten waarvan je denkt Who the Fuck is dat nou weer. https://t.co/nT7NmOso2T
>     RT @iamwilliewill: Need to vent? Get in the shower
>
>     Need to practice a future argument? Get in the shower 
>
>     Need some alone time? Get… 
>     @deanxshah lain baca lain keluar hanat
>     If @HEARTDEFENSOR and @ATelagaarta aren't goals, then idk what is. https://t.co/0NSfONADna
>     RT @La_Directa: @CDRCatOficial #VagaGeneral8N | L'avinguda Diagonal #BCN també està tallada per un piquet d'unes 200 persones… 
>     RT @7horseracing: WATCH: Take a 👀 at the #MelbourneCup from a jockey's perspective, on-board w/@dwaynedunn8 , as he guides home 8th p… 
>     RT @javierjuantur: Gallardón, es #CosaNostra.
>     Ignacio González acusado de tapar los delitos del Canal en la etapa de Gallardón.… 
>     I won 3 achievements in Wolfenstein II: The New Colossus for 26 #TrueAchievement pts https://t.co/K2re2ox59h
>     Ugo Ehiogu: Football Black List to name award after ex-England defender #Birmingham https://t.co/67SoR1RoK3
>     @Channel4News @carol580532 @EmilyThornberry @Anna_Soubry @BorisJohnson The time has come for @Boris Johnson to resign, he has do e enough damage no more allowances made 4 him
>     @HiRo_borderline きていません！←？
>     RT @caedeminthecity: @StarMusicPH @YouTube Wahhhhhhh! Yas! I guess this is the go ahead to use:
>
>     @mor1019 #MORPinoyBiga10 Alam Na This b… 
>     RT @cadenguezinha: só queria dormir pra sempre
>     tipo morrer sabe
>     Ranço tem sido meu alimento noite e dia.
>     RT @sayitoh: Fumen, chupen, cojan, se van a morir igual porque los Etchevehere del país ya les envenenaron el mayor río argentin… 
>     @Balancement @The_music_gala Probabilmente si , anche se alcuni storici nutrono dubbi. Ma non importa....lo ricordiamo per i suoi Capolavori
>     RT @SEGOB_mx: ¿Qué es la ceniza volcánica? y ¿Cómo puede afectar a tu salud? #Infórmate https://t.co/HxYVUMDqWb @PcSegob… 
>     Screaming told my grandma I’m working in Town tonight and I quote “TOON TOON TOON” was the message that followed ahahaaa
>     @_goodstranger nao da pra desperdiçar dinheiro beijando
>     RT @bellaspaisitas: ¡Impresionantes! Descubren las características de las mujeres más inteligentes!😱😱https://t.co/0zgpP6uKKS
>     Tanımadığım biri gelip hal hatır sorunca tanıyomuşum gibi konuşmayı devam  ettiririm, bu da benim serbest serseri stilim 🙄
>     RT @Dustman_Dj: when Your Dad's WhatsApp profile pic are kids you don't even know https://t.co/JfcPo017AJ
>     wait - forever na ba to ? may convo na kami ni crush 😂🙈
>     RT @Monica_Wilcox: That's the thing about books. They let you travel without moving your feet.
>     ~Jhumpa Lahiri, The Namesake
>
>     #art-The… 
>     @Kylapatricia4 @lnteGritty @succ_ulent_1 @Patrickesque @ZippersNYC @HumorlessKev @pang5 What are the policies that Our Revolution supports?
>     RT @cutenessAvie: SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME SHAME S… 
>     @cupnacional Huelga es actos vandalicos y obligar al resto a no circular o coger transportes? A ver si empieza la GC a poner orden y se os acaba la tonteria, q ya no queda paciencia y algun vecino os lo va a explicar de manera clara y rapida...
>     RT @JackPosobiec: Fmr Gillespie staffer to me: I walked out before the primary when I saw the campaign was run by NeverTrump losers
>     @AuthorSJBolton @JenLovesReading @ElaineEgan_ @sarahbenton @SamEades Amazing!
>     CHP'nin Meclis Başkan adayı farketmez, etkisi sıfır olacak çünkü.
>     RT @rasadangubic: Apsolutno se u ovaj dijalog moraju uključiti svi.Tu računam I na opozicione stranke #upitnik
>     RT @AmiralYamamoto: Utanmadan tuzak kurduk diyor zindik. Evet, coluga cocuga, el kadar sabiye tuzak kurdun, ne buyuk is yaptin. cehennemin…
>     RT @pastelvibrant: Happy one million followers, @maymayentrata07! Ngayon ko lang nakita omg wowwwwwww #MAYWARDMegaIGtorial
>     Sekarang ni musim convo kan? Plus akhir tahun banyak kenduri kan? Kalau pakai #lilybasickurung… https://t.co/ezaoNl9wlT
>     @mirzaiqbal80 his statement in itself iqbal bhai is a clear indication that he is next in line for Pakistan captaincy
>     RT @sosunm: buying another black hijab with the excuse "I don't have one in this fabric" https://t.co/ejy9mOp3Ef
>     RT @MonbebeSpain: ¡Monbebes! Enseguida tendréis el form de nuestro GO para el Álbum firmado 💕 https://t.co/vbUXavTPQp
>     RT @lnstantFoot: Voici le classement des valeurs des clubs en Europe avec, pour chacun d'eux, la valeur de leur joueur le plus cher.… 
>     se fosse pra dar certo tinha dado
>     please keep streaming fam
>     RT @nevergiveupgs: @bariscenkakkaya Bahsettiğiniz durumun psikoloji dilinde karşılığı "Öğrenilmiş Çaresizlik." Gelmeyen üyeler şöyle d… 
>     RT @PLAYMFS: Pltl followan kuy yg mau comeback, rt aja janji jfb @rvwendykr
>     RT @nedoliver: One year after Trump's victory, Virginia elected:
>
>     First transgender delegate in the country
>     First out-lesbian dele… 
>     Convidamos você, à uma Aula de Ritmos especial,é a nossa ''Quinta… https://t.co/gzm9FUNsDu
>     RT @Kon__K: Hundreds of men are starving right now on #Manus Island because the Prime Minister of Australia would rather they p… 
>     RT @elise_djl: @KhalidMescudi « Maintenant » mdrrr c des mutantes
>     @GirautaOficial Este hombre en lugar de llevar colgada una cruz Cristiana debería llevar una cruz gamada
>     El #pensamientodeldia de hoy tiene como objetivo que tu y yo aprendamos a ser un #testimoniovivo… https://t.co/XinP5HlTIX
>     RT @teukables: Hyungdon: Is it the first time Shindong, Yesung and Donghae is on the show?
>     Members: yes
>     Hyungdon: Why?
>     Donghae: I… 
>     追加  addition
>     All Americans MUST LOVE ISRAEL - and now news Raw: Trump, Xi Tour Beijing's Forbidden City https://t.co/Ewpzw1y2HM
>     RT @Maskied: Bientôt quelqu’un va nous sortir t’es un ancien si tu as connu les 140 caractères sur Twitter
>     @Soeroah Watch a LP, or wait for the ART :V
>     RT @OBerruyer: C'était l'anniversaire de BHL.  En cadeau, voici enfin le clip pour la belle chanson que Renaud lui a consacrée...… 
>     @Ttoms__ Thanks fam
>     RT @Kwamebonfit: *#PPHC-GETINSPIRED*
>
>     *Cleanse your heart daily with forgiveness. Avoid jealousy to keep ... #PPHC #PpHypeCrew_  https://t.…
>     RT @Hatunnderler: İnsanlara neden gözü kapalı güveniyorsunuz uykunuz mu var.
>     RT @Kwamebonfit: *#PPHC-GETINSPIRED*
>
>     *Cleanse your heart daily with forgiveness. Avoid jealousy to keep ... #PPHC #PpHypeCrew_  https://t.…
>     @MaviiiCinar @AdaletciZehra @ummuhanm @Omer_Zehra_86 @hazer61 @Esmer08837560 @nurselKazanc @adilazimdeilask @glhangler8 @Nuraykyc99 @aynurkarabacak1 İnşallah sevinçten yetmez 280 yoksa bizde tansiyon 180 şeker 380 olacak gibime geliyor🙄 #AdınıSenKoy #Zehöm
>     RT @Kwamebonfit: *#PPHC-GETINSPIRED*
>
>     *Cleanse your heart daily with forgiveness. Avoid jealousy to keep ... #PPHC #PpHypeCrew_  https://t.…
>     Bridge/Tunnel Stoppage: WB on I-64 at I-64 Tunnel-HRBT W in Norfolk. All travel lanes closed. Potential Delays.4:32AM
>     @1DUpdatesPolish Jaram się
>     Ülkenin birinde benzine 13 kuruş zam gelmiş,verginin vergisi alınmaya başlanmış,dolar 3.87 olmuş,eğitim çakılmış,işsizlik 10.2 nin üzerine çıkmış halk demiş ki hani benim cam filmim?
>     @venecasagrande O Léo Duarte tá relacionado pra hj?
>     RT @angelsmcastells: ANC i Òmnium volen convertir l’11 de novembre "en un nou 11-S" https://t.co/bxNXrG7rom via @ARApolitica
>     #NP @yukatamada28 - Senja Baru #SOREDOREMI
>     RT @Footaction: Basic instructions before leaving earth: listen to GZA's Liquid Swords one time. It released on this day back in 19… 
>     Coucou @obernasson : je te mets donc en relation avec @Nico_VanderB, qui pourra sans doute t’apporter son expertise !
>     RT @TennisTV: Group Pete Sampras
>
>     🇪🇸 Rafael Nadal
>     🇦🇹 Dominic Thiem
>     🇧🇬 Grigor Dimitrov
>     🇧🇪 David Goffin
>
>     #NittoATPFinals https://t.co/Dy1Ck3l…
>     RT @MaheshBabuFacts: Superstar @urstrulymahesh 's Sister #ManjulaDevi Garu Now On #Twitter !!
>
>     Follow Her Now ➡ @ManjulaOfficial ❤ https://…
>     RT @olverawill: me posing for the government watching me through my webcam https://t.co/FiRu89zYn9
>     Par contre pourquoi avoir remplacé le décompte de caractères par ce petit cercle de merde la?
>     Delegado comercial para el sector mascotas en provincia valencia https://t.co/PmLE57izK3 #empleo #trabajo #Valencia #trabajovalencia
>     @football_kudos @LiamPaulCanning We cant blame the manager on Mkhi's woes. Jose has tried everything
>     İnsanlar, zaten Gerçek aşkı bulunca Tamamlanır , diğer tarafta hep eksik kalır kendine, hep bir arayışın peşine düşerken, Başarısızlığa yol açan o bilmediğimiz eksik Yanımız, SÜREKLİ bizi Hatay'a marus bırakırken, Tamamlayan yine bulunmaz...
>     RT @fils2timp: Ptdrrr jlimagine bien avec sa ptite éponge en train d’essuyer le sol comme une salope https://t.co/WGCQHeiP74
>     @jonroiser @ShuhBillSkee @GordPennycook @dstephenlindsay @jpsimmon @uri_sohn Indeed! That's a good way of putting it (w/ certain constraints as we aren't expert in all areas of course). Incredibly rewarding process.
>     Vodimo vas u obilazak svetskih prestonica džeza #headliner https://t.co/UseBs6ADqE
>     @narendramodi177 Hallo sir
>     @caseygoettel5 Lol Cas im 20, i dont really fit in that category anymore
>     Bom dia o caraio tnc
>     RT @CEUAndalucia: Primeras sesiones del máster de Atención Temprana III #posgrado #atenciontemprana #salud #educacion @JMGAlorda… 
>     RT @burgerqueenn_: No son "denuncias falsas", es cultura de la violación. Es tener la violación tan normalizada que no eres consciente ni d…
>     RT @sarinot_sorry: This girl in my class gave a speech about how marijuana is bad, sat down looked at me and said "I'm high right now" I've…
>     Do you want to get these clothes for free? Please contact me.😛whatsapp: +8618595911030 #Germany #DE https://t.co/NfiK4efaVM
>     Unta magpadala si papa🙏😔
>     RT @batipark: Drama dersinde Şehit İbrahim Çoban 4/F sınıfı Pandomim çalışmaları çok keyifli.  @abbSosHiz @ABBTANITIM https://t.co/srIoJKXo…
>     RT @ethoneyyy: Ang dalas lumabas ni something went wrong sa kin ngayon. May problema ba sa site? O sa kin lang sya lumalabas nang lumalabas…
>     @lemondefr Déjà la COP21 n’étais qu’une mascarade, c’est bien fais Trump 👍🏻
>     RT @hoescrush: ain’t no such thing as a “slow texter” females will stop doing they hair & stop putting on makeup to text you back if they t…
>     I have a rising follower graph! 6 more followers in the past day. Get your stats right here https://t.co/J0EPUmfU3R
>     Alguém diz p bebezinho que a vida continua além da cama, pq ele não me deixar daqui
>     RT @twentypiIotos: separados por uma fantastica fabrica de chocolate https://t.co/XDXn9N2ZZu
>     Mon seul extra de ce mois ci, promis.
>     Sinabi ko na sa kanya😂
>     @Barnes_James93 @JGorrell_1 @HannahWood66 @jackreubenallen Michael Weetabix
>     RT @AMDWaters: Police priortise non-English speakers, schools & NHS prioritise migrant "children". Can it be any clearer? We mean… 
>     RT CNN "This Florida school is selling bulletproof panels that can be inserted into students' backpacks… https://t.co/r6zkeOzhNb"
>     RT @RRDefenders: Nothing, but heaven itself 💕😍
>
>     @rasheedriverooo @RicciRivero06 @priiincerivero @brentparaiso 
>
>     📷 @dori_ceL https://t.co/TK…
>     @GG_RMC @Slate @krstv que cet 'interlocuteur' appelle #besson , il avait dicté à sa fille @itinera_magica que le masculin l'emportait sur le féminin #joxe
>     RT @immarygracee: Tatay Baristo: "Ano? Nakita mo na ba si Tristan?"
>
>     Pakaganda ng punong bantay!!! 😍 https://t.co/620GM6pNUD
>     RT @sallymcmanus: 🎩Gment says all communities sector workers should have insecure jobs - 300 000 workers & families https://t.co/2aJBN09c5z…
>     RT @Houseof_CAT: Ha salido ya la oposición pidiendo la dimisión d M.Rajoy tras confirmarse los sobresueldos? ah vale, q no hay oposición, d…
>     RT @BVerfG: #BVerfG Personenstandsrecht muss weiteren positiven Geschlechtseintrag zulassen https://t.co/33HNaKlZYZ
>     RT @Python_Pro_Dev: Python for Data Science and Machine Learning Bootcamp
>     ☞ https://t.co/DM1aKG56L1
>     #Python #Pythontutorial
>     S1BmO6dHA- http…
>     RT @EtBipoolar: Não esqueci, só não faço mais questão de lembrar.
>     trop plaisir de voir pierrick
>     @Girl_Encounter いやおいw
>     #280karakter
>     Bugüne kadar istediklerimi yazamadim,simdide ben yazmıyorum.
>     RT @litoralepontino: A #Itri (LT) il 10.11 al Museo del Brigantaggio conclusione per la XIII ed. del #JazzFlirtFestival 2017 con Frances… 
>     RT @juiceDiem: OCEAN MAN 🌊 😍 Take me by the hand ✋ lead me to the land that you understand 🙌 🌊 OCEAN MAN 🌊 😍 The voyage 🚲 to the c… 
>     @Taruvi_es Gracias por seguirnos, ofrecemos el precio de todas las viviendas de España gratis. Síguenos en Fb https://t.co/2gtmYECCId
>     Una obra maestra que cambiará la percepción del arte griego prehistórico https://t.co/ZUTgwBLbyT
>     RT @La_Directa: @CDRSants #VagaGeneral8N | Els Mossos d'Esquadra es despleguen a la ronda de Dalt de #Barcelona i obren el lateral… 
>     Q berasa jadi pengkhianat beli album tim lawan 1 set sedangkan di tim sendiri cuma beli 1 pcs. I'm so sorry😢
>     peguei o 399
>     RT @fgautier26: West suffers from Christian superiority complex. More religious freedom in @narendramodi India than #France where H… 
>     @oldguy_jb Good Morning Handsome, hope you have a Wonderful #WickedWednesday, Enjoy HHD 😘💋
>     RT @elcosodelapizza: Si sos infiel sos una basura, no me importa cómo fue la situación. Nadie merece irse a dormir preguntándose por qué no…
>     RT @agarzon: Se confirma que M. Rajoy es Mariano Rajoy y que cobró sobresueldos de la caja B del PP. En suma: gobierno corrupto. https://t.…
>     RT @Jeff__Benjamin: Congratulations to @JYPETwice who are No. 1 on BOTH @Billboard's World Albums and World Digital Songs chart:… 
>     @romankemp Dude what happened with that girl after #FirstDates ???
>     RT @QdaFox: This little Super Mario mystery box that keep popping up is annoying as fuck 🤦🏾‍♂️ ( I️ )
>     Keep going it's not over.
>     #Koreaboo One Thing About Tzuyu’s Face Has Changed Since Debut And It’s Changed Her Look Drastically 
>
>     ➜ Read More… https://t.co/qCXzaxuwsE
>     RT @valencornella: Todo un lujo haber tenido la suerte de conocer a Jacinto
>     El otro fútbol también cuenta https://t.co/NZSDpIgG4k
>     RT @F_Desouche: « Il n’y aura pas de mosquées en Hongrie, c’est une question de sécurité nationale » déclare un député pro-Orbán -… 
>     meu cartão de memória apagou todas as minhas packs aaaaaaaa https://t.co/6CqpfnJUAb
>     Tô com tanto sono
>     alguém me aconselha um álbum de música maneiro ae ???
>     RT @GoNutritionUK: 🎉 200K GIVEAWAY! 🎉
>
>     WIN an incredible GN supplement stack worth £200!
>
>     ✅ Retweet
>     ✅ CLICK the link and enter your de… 
>     só tem 3 pessoas do nono na escola kkkkkmtfodidas
>     RT @jfalbertos: El inspector jefe de la UDEF encargado de Gürtel ha denunciado en el Congreso de los Diputados que que se intentó d… 
>     @MxolisiBob @ayikahlomi Pho why bemfake as DP? ANC iyadida yazi. Coz ibona abamvotela in Mangaung.
>     meu amor é  teu, mas dou te mais uma vez, meu bem saudade é  pra quem tem ... 🎶
>     RT @yphsig: Dim Light Melatonin Onset and Affect in Adolescents With an Evening Circadian Preference https://t.co/6W7mKDjDpN
>     RT @eat_and_roll: Ideas de propuestas diferentes para ofrecer en tu #foodtruck https://t.co/HDmgLW9zaq vía @foodtruckya #cocina… 
>     RT @_Christian__DO: Support EXO
>     Support Astro
>     Support Pentagon 
>     Support SF9
>     Support CLC
>     Support Ikon
>     Support BAP
>     Support Boyfriend
>     Supp… 
>     RT @Zurine3: Quiero que mi primer tuit largo sea para todas las mujeres:
>     Tenemos derecho a no tener miedo, a ser dueñas de nuest… 
>     I’d take a full day in bed about now. Can’t be fucked with cold winter weathers.
>     RT @jlichotti: Em um mundo onde a tristeza e a depressão dominam, é necessário a manifestação da alegria e do amor de Deus.
>     It'll make me happy if I get comforted when I feel sad...
>     PROZACK...
>     #fashionset #trendoftheday #rinascimento #style #sandroferrone #jacket 
>     Ci trovate anche al Centro... https://t.co/v5y0AdwIDM
>     RT @Glam_And_Gore: @Gabriole3 This post and these replies are all too sweet 🙈I am just a smol potato in makeup but thank youuuu!!!
>     Anyang Loyang
>     @heartdraco i can't believe i can finally give u the softest hug this month
>     He añadido un vídeo a una lista de reproducción de @YouTube (https://t.co/tXuVMwoGjB - Bedtime Is At 10 - Short Horror Film).
>     Now Playing on WNR: Caterpillars - Open Your Eyes - Click to listen to WNR: https://t.co/ZNJgRccx2n
>     RT @Ahmadd_Saleem: @khan_ki_cheeti @UmairSardarr @MAhmedbaig @AlishbaSharif @traders92 @PTI_ki_cheeti @iHUSB @DuaBhuttoPTI… 
>     Who is your #best #investment?
>     Sorriso dessa mina é lindo ..
>     RT @GobernoAlem: 14 minutos de telediario matinal y ha salido Puigdemont 10 veces, Junqueras 5, Rivera aupado por el CIS 1, la crisi… 
>     RT @blvcklucy: Iba talaga si Maymay eh noh? Nagta-transform kada mags & photoshoots.
>      
>     #MAYWARDMegaIGtorial
>     Los principales beneficios de la lectura para tu salud mental. https://t.co/2921RS5bK9  #MujeresEmpoderadas
>     RT @piersmorgan: Already massively bored by everyone’s mind-numbing 280 character tweets. This is a terrible mistake, @Twitter. 😡😡
>     RT @antoniobanos_: Una cosita de cara al 21D. Estos de Indra que untaban al PP están metidos en el recuento de los votos
>     Nada, por hac… 
>     RT @sharmilaxx: This was 3 years ago where we were hopeful  for NHS whistleblowers https://t.co/n9vVjQwKSb  @Jeremy_Hunt @EdPHJ11
>     @HoeWerktNL Ik blijf pleiten voor een maximaal 32-urige werkweek in deze 24/7 maatschappij. Is er voor iedereen in alles meer ruimte. Zorg, ouderschap, eigen tijd, meer mensen aan het werk, vrijwilligerswerk, etc. Minder = meer
>     RT @kasimgulpinar: Dünyanın en büyük turizm fuarlarından Londra WTM’ye katılarak Şanlıurfa’nın ve Göbeklitepenin tanıtımını yapan bir… 
>     @ZehraAydn399 @istedigimhersey @AlpNavruz Bu kadar umutsuzum
>     Mil cairão ao seu lado e dez mil à sua direita, mas tu não serás atingido. Salmos 91:7  🙏
>     Bom diaaaa 😘
>     RT @NaufalAntezem: ‘Kepala bapa dipenggal, tangan nenek & datuk ditetak putus serta adik perempuan ditikam’
>     Jgn pandang mudah hal kesi… 
>     twitter rolls out 280-character tweet but removes character countdown sad
>     "Do you know what is better than charity
>     and fasting 
>     and prayer? 
>     It is keeping peace 
>     and good relations between people,
>      as quarrels and bad feelings destroy mankind." 
>
>     (Muslims & Bukhari)
>     @silvith2 💀🙏
>     https://t.co/CMVgnV3xDp <-Asian Homemade 10 #porn #xxx #adult #naked #nude #teen #nsfw #bath #bubble #tits #boob
>     RT @iNerd__: Ni lah tattoo yg aku selalu main masa zaman kekanak. Beli je chewing gum mesti dapat benda ni. Zaman sekarang dah t… 
>     @MatheusFtw__ mas eh q comecei esses days, vou ta chegando na 3 e vai ta lançando a 7
>     RT @IvorianDoll_: They will turn you mad then call you mad
>     Armys really out here thinking the Ellen show and jimmy kimmel is more prestigious than actual Asian awards y'all fr some racists
>     RT @TeaPainUSA: Tonight in South Korea, Trump is layin' out a case for war with North Korea.  Let's show Trump what true bi-partisa… 
>     RT @theoposture: @cerealandsuch @feltusa @vkdesign @KAYTRANADA @placesplusfaces @GrandiorFree @RarePanther ✨ listen now… 
>     The watershed moment for me came when the GOP establishment attacked Paul Ryan for commenting favorably about Ayn Rand and forced him to issue a retraction. The Far Left and the GOP establishment have in common a hatred for Ayn Rand.  Think about that for a moment.
>     @7rakmae 記憶にない‼️😀
>     @lequipe qui nous fait encore un article sur #Neymar vide et racoleur pour qu'en conclusion il nous explique que même si Emery et Neymar se battent, Emerey reste jusqu'à la fin de saison... #Ligue1 #PSG
>     RT @Koreaboo: Eunhyuk Always Wanted Plastic Surgery, But SM Entertainment Forbid Him For This Reason 
>
>     ➜ Read More:… 
>     RT @jsanchezcristo: Congelaron la caja menor de los políticos...las CIC ? entonces el candidato Cristo volverá a hacer rueda de prensa… 
>     RT @kairyssdal: 280 is going to bring a lot more ill-considered/self-incriminating tweets that would’ve been abandoned under 140.
>     he would've had too much to stress over and its much better like this
>     RT @PodemosCongreso: Ruiz-Gallardón 
>
>     *** WARNING: skipped 191186 bytes of output ***
>
>     RT @Merck_Alergia: Recupera el blanco de tus ojos con la #inmunoterapia https://t.co/5H8o4mcmkp
>     RT @NengoFlowVoice: El que pierde su humildad, pierde su grandeza.
>     Nem acredito que vou vê lo por 2 semanas direto, meu coração tá tão eufórico com isso!!
>     CHORUS (menggantikan: 'take .... tanah dengan' butir pasir)
>     RT @RoadcareKuantan: Kerja mill & pave di FT02 Sek 315-319 Jln Ktn Maran siap dilaksanakn utk keselesaan pengguna jalnraya. @JkrKuantan… 
>     Colchón de muelles convencional constituido por un bloque de muelles con refuerzo lateral que le otorga estabilidad  https://t.co/WyQiTUSPuu https://t.co/ll7U6aD5AV
>     RT @dulceguia111: Resiliencia: confianza plena muy por encima de toda adversidad de tus capacidades y poder de hacer que lo que no te… 
>     RT @hunteryharris: sorry, no. there's a mistake. Moonlight, u guys won best picture. Moonlight won. this is not a joke. come up here.… 
>     @Uber_Pix not true we have several in Kenya
>     RT @janusmaclang: Congratulations to my friends taking AB DipIR who can finally tweet their course: Bachelor of Arts Major in Diploma… 
>     RT @UnJaponegre: I m a g i n e z seulement le bruit s’il avait fait ça dans une classe avec des noirs. https://t.co/2OBlj4903L
>     Dormi e deixei o Raphael falando sozinho
>     @VYKTORYA7 @carlos01101966 @ageyp @silviasebille @teresa_wells49 @melvinlvi09 @sophie_woolley @halemenos @Shu_la75 @zhuchenya2015 @n1234nina @PapaPorter1 @best_fynny @bgvalkyahoocom1 @Nevooogh @EmiFever10 @taxi_ede @aniluna8a @starseed009 @Boulevard_vo @Anderso49383620 @CarlosA92179369 @88Gsp @brasileiro1914 @BLuismbm @Isabel06657079 @sandraldantas13 @carlstittoni @PatrickGure @manin2505 @archikayani @DoraEx_skr @rosina_strebig @YNHallak @No__Dm @JuanCarlosG24 @Rod_1960 @FeerozMansoor @SilviaDaCruz9 @Nekane50A @peliblanca7 @joseexpositoo @Fbastidasar @francescofrong2 @steve64mil1 @belu18_ok @poupouch48  https://t.co/tJJ9zYTlV3
>     Allah 280 karakterlik dert vermesin
>     @PlayBoyManGames @femfreq You are my spirit animal.
>     RT @KEEMSTAR: Once upon a time in a far away land, a magic squirrel named flappy won the pie eating contest at the state fair. Al… 
>     RT @sillvalohany: @camlt91 Minha mãe só fala mal do DG kkkkkkkk
>     "Semiconductors" is back in stock at "Procyon / Hardwick Station" https://t.co/gahUnnhh33
>     RT @samuelinfirmier: Destroyed My #Laptop,Time 2 Use My #Mobile From #Vegas. Laptop From #VegasBaby Was Destroyed Last Year. DAMN a… 
>     @oriolguellipuig Jo de veritat que no entenc en que ajuden coses com les d'ahir. Crec que s'estant passant de frenada totalment i les simpaties que desperten son molt limitades. Nomes s'ha de veure qui estava ahir a l'acte
>     RT @biticonjustine: i do not want you anywhere near me if you do not support me. i do not have time to feel terrible because of you. im try…
>     @tigerpandaren הקשבת לפרק?
>     @womanfeeds @ohstrawberrx maksudnya bisa via shopee biar gratong ^ ^
>     RT @JajaPhD: I’m not rich (yet) but I have good packaging. Ever since I realised that I’m (also) a product/brand, I started taking my packa…
>     @TenereyDingess 💀😂😂 love you Girl 😘 glad to have you as a roomie 😘💕
>     @SofiaSabinot sempre!!!!
>     RT @Zulkarn_N: Selamat 3 Tahun terakhir, revolusi mental ketenagakerjaan Makin baik https://t.co/kIjo8Fcks9
>     @NendiaPrimarasa GedungPernikahan & Hotel utk grup besar+kecil & disewakan RuangSeminar/Manasik hp 0818716152 kami menyesuaikan BAJET https://t.co/KbjmYYe742
>     Acatarrado pero nada que un buen al café  no pueda solucionar. #Weekendnotardes 🤒 https://t.co/uss9Qk1MdV
>     RT @chuuzus: i need money to fully unlock my fashion style
>     @LVPibai @MrPiston_  Esta season hay que quedar alto
>     #kurumicigirisimcilik "Startup'ları satın alıp sahip olmayin, yatırım yapın!" #hasanaslanoba #KGK2017
>     That's jealousy, hon.
>     RT @iairsaid: Me tengo que operar de la ansiedad.
>     todd as the bachelor. i would watch. #gruen
>     (Ankara Friday: Checkout these Latest Ankara Styles for your weekend (PHOTOS) https://t.co/wwfrqPCLnD
>     「Aを差し控える」 refrain from A = keep from A
>     Maygad lapit na retreat
>     @Charlineaparis @LeaSalame Le vrai problème, c'est que #Macron n'a pas osé se positionner à l'extrême centre pour éviter que les branches ultra modérées ne l'encerclent par le milieu.
>     Vous voulez un dessin ?
>     RT @DrGPradhan: We are with you @narendramodi JI
>
>     #AntiBlackMoneyDay https://t.co/906t3sdDKf
>     RT @SaharaRayxXx: Demi Lovato Exposes Underboob And Sexy Toned Tum As She Sends A Message In Cropped Feminist Top In Sultry Shoot
>     https://t…
>     RT @SoVeryBritish: “Might go to the gym”
>
>     *4 minutes later in bed eating all the crisps*
>     RT @IISuperwomanII: You know what’s more productive than complaining? Improving.
>     RT @flowerene329: The team filming Peekaboo MV also filmed Dumb Dumb, NCT’s 7th Sense, and EXO’s Love Me Right
>     =
>     PRETTY MUSIC VIDEO
>     RT @uosIDS: Welcoming our Famous Five - discussing the developmental science of 'BUILDING SUPERHUMANS?' https://t.co/CIBFai1gNt… 
>     only 3o p for a large mocaa at waitrose even without one of their cards might have to make this a regular thing
>     RT @blushonpamore: Moments after this photo of us was taken by one of her cousins, Kisses said thank you to me so many times, kissed m… 
>     @soabcdefgh nn mais c’était pour m’faire mdr
>     RT @CharlieMaines: Sam Allardyce has just confirmed that no talks have taken place with majority shareholder Farhad Moshiri.
>
>     "Sky Sou… 
>     Nursery have been busy making some beautiful poppy pictures and learning about why we wear poppies! @WestonbirtPrep https://t.co/jqRZuIkoGK
>     HEALING MIND: Five Steps to Ultimate Healing, Four Rooms for Thoughts: Achieving Satisfaction through a Well Managed Mind
>     by Janice L. Mcdermott
>
>     Order now on Amazon!
>     https://t.co/iJOJyhC4QI
>
>     #HealingMind #HealingBooks #OrderBooks #JaniceMcdermottBooks
>     #JaniceMcdermott https://t.co/1Cia3fKj8m
>     @titafock não te abandonei nada 😒
>     RT @Geezajay2013: Let's help this woman by Retweeting her photo around the world. https://t.co/jH6bSPkiP7
>     RT @Panamza: Eugénie Bastié : journaliste au Figaro ou porte-parole du Crif ? https://t.co/fdKl3DAP6p cc @EugenieBastie… 
>     RT @mit_akbulut: UZMAN ÇAVUŞLAR 
>     3269 sayili içi boş olan uzman erbaş kanununu ya degistirin yada insanca yaşanabilecek yarina güvenle bakı…
>     RT @Geezajay2013: Let's help this woman by Retweeting her photo around the world. https://t.co/jH6bSPkiP7
>     RT @WeAreTennisFR: Vika #Azarenka s'est trouvé un nouveau partenaire de double 👶
>      (Vidéo @vika7) https://t.co/Xt1tkpJTD2
>     Burning midnight oil... More like burning your life soul.
>     @wahiyaatsociety happy birthday jani
>     RT @GrumpyScot: @skiljonlo @HeldinEU @aftabgujral @Petersbrooking @nickynoo007 @Ferretgrove @cats2home @alastair_hart @MariaRossall… 
>     Confesso que já matei alguns sentimentos por aí, mas foi em legítima defesa.
>     RT @rlkngdanil: Happy 1 month anniv to @nancymland & @KMJAEHWAN96 langgeng yo, jagain ade gua hyung kalau dia nakal cium ae?/ .ga l… 
>     RT @TodosxLibertad: Carmen Gutierrez tiene 2 años y 10 meses presa injustamente en el SEBIN. Es una presa política y merece pasar una… 
>     @sufirushk @2ttee @KafirDeplorable Real benefit of demonetization was only 1 and that we all know. Beyond that I don't know. But learnt 1 more from R Prasad today.Really!
>     RT @AP: BREAKING: Civil rights lawyer Larry Krasner, who wants to combat justice system inequalities, is elected Philly's top prosecutor.
>     @MadalynSklar Somehow feel that many channels are getting more and more alike, losing their original touch. I prefer Twitter being concise & precise.
>     RT @Jeff__Benjamin: So awesome to see @BTS_twt will be on @JimmyKimmelLive! The latest huge opportunity for BTS, can't wait for what's… 
>     RT @khairunnaim5: Orang yang suka durian je tahu betapa nikmatnya kalau dapat makan..
>
>     Ya allah,nikmat nya tak terkata😍 https://t.co/z7qsHH…
>     @korona_sila 250 tl çalışır, bu paraya çok güzel pansiyonlar var 😃😃
>     @gethardhero ils passent sur Chrome PC D:
>     @Q_madarame まだ覗いてなかったのに～(。>д<)
>     RT @newrulesdua: We were built to last
>     We were built like that
>     Baby take my hand
>     Tighten this romance
>     We could burn and crash
>     We cou… 
>     Hoy juega el ciclón
>     Nacional vs CP 
>     Defensores del Chaco 
>     A las 19:10hs
>     RT @adidasoriginals: Wet weather is coming, be prepared with a Goretex update to the #NMD CS1. Available from November 18th. https://t.co/i…
>     @mlawelshgirl Haha I'm still tired having a coffee! Xxxxx
>     @ccaitlinss Hot
>     RT @Cwedding1: Dear GOP,
>     Our thoughts & prayers go out to you tonight during UR time of sorrow. We know the loss of your candidate… 
>     Bom dia bem bad
>     @fdaxbaby @ISeeYourStops order blox, mitigation blox, and breakers, just support and resistance, pivots and pull backs rebranded for the Angry birds generation
>     RT @qbreexy: my mom said "how tf you picky & still pick the wrong ones" lmfaoooothatshithurtedoooo
>     @igmpbidec Hahaha you know what I meant
>     11 people followed me and 2 people unfollowed me // automatically checked by https://t.co/0Y7gV7w8eM
>     Having a good bookOops.
>     RT @RepJoeKennedy: Health care isn't political, it's personal. And tonight Maine chose to help 89,000 neighbors access care. https://t.co/8…
>     RT @BuAdamLan: - bu gece neye içiyoruz?
>     + aramızdaki en kıdemli derbedere.. https://t.co/AfbRe3SWdk
>     @CynthiaViannou on passe rapidement on ferme les yeux et l’oublie bahaha 😭 tu fais quelles dates?
>     RT @wagappe: Il n’y a pas de médicaments qui soulage la douleur de l’ame.
>     RT @C_catarina_: Quem me dera ter dúvidas destas https://t.co/6UZdwdLhUd
>     RT @dapperlaughs: Right! I'm thinking of throwing a big Xmas Party for everyone that's been tuned in this year! 1k RTS & it's on!… 
>     RT @Carola2hope: Cierto, ellos toman ordenes directamente desde La Habana, sus amos están en esa triste isla. https://t.co/3DSTL7Wcxr
>     @LaiaSz @ramontremosa Endavant Laia. Tot el meu suport. Volen un país d'ignorant si per poder-lo controlar com vulguin ells. Seguiu així.
>     I'll try to cook sinigang 😊
>     The latest The Susan Lewis Daily! https://t.co/KeLViquq0g Thanks to @Wildmoonsister @ScientologyVM @Tamaraw68415067 #amwriting #writerslife
>     @Soratania ...tutti scegliamo in base all'umore... mai promesse quando si è felici.. e mai decisioni quando si è incazzati...
>     @JustSafwan Diam le wahai anak engineering
>     'De Bill Cosby a Kevin Spacey: los escándalos sexuales de Hollywood a través del tiempo ' https://t.co/3wGStOqzVv vía @RevistaSemana
>     RT @andrewrodge6: college is the razor scooter & I am the ankle
>     Da hast du #280Zeichen zur Verfügung und denkst, das ist was besonderes, da hat's dann jeder. Nix mit Fame. Fast 7 Jahre Twitter und dann das. Fühlt sich verboten und falsch an. So wie hinterm Vorhang.
>     So, das war mein erster 280er Tweet.
>     Jetzt erstmal weiter frühstücken.
>     This Man?s Enormous Blackheads Were Popped Out And The Results Are Disturbing. https://t.co/BP1BD8lxrr
>     RT @Larissacostas: A ver si así se entiende, porque parece que no les queda claro 😒 https://t.co/JBSAXKly7T
>     @wepl4ydumb Ela dormiu de boca no Jubileu ???
>     RT @iaccrazyzhang: @harrypotterli1 @Gabrielleling1 @Fairchildma2 @eastmanzhao @damarissun2 6印度
>     RT @TheAndreG_: " Kailangan ko munang mahalin ang sarili ko ngayon.. Masyado yata'ng napunta sayo ang lahat ng bagay na dapat para sa saril…
>     RT @syahir_sageng: Sepupu aku wedding invitations card, sketch sendiri dan scan. Dia arkitek https://t.co/rQTLLZbYUk
>     RT @sorluciacaram: Nada justifica el odio, la obstinación y el enfrentamiento entre pueblos diferentes o hermanos. La convivencia mere… 
>     RT @cxpucinepatouet: Le froid jveux bien mais la pluie nn c trop
>     RT @IslamicTwee7s: One day you will leave this dunya, but your imprint will remain, so make it a good one. https://t.co/4Z8R7vjZQD
>     RT @LelakiJelita: Sekarang dia buang kau macam kaca pecah but one day akan ada orang kutip kau macam permata pulak. Trust me
>     we need to watch the flu, the flu 2 & world war z during our one week vacation next week so we'll understand the discussion and be able to answer the questions our health teacher will give us when we come back i live for this concept
>     RT @hidayahaaaaah: @kaidorable114 @EXOVotingSquad @weareoneEXO Are u using incognito mode? Can u switch off phone then on back. #EXO @weare…
>     Jadikan kepandaian sebagai kebahagiaan bersama, sehingga mampu meningkatkan rasa ikhlas tuk bersyukur atas kesuksesa
>     @DIVYAGARG11 @shivimomolove @RidaCreations They all know everything 😂😂
>     @BrawlJo Si, et crois moi que je vais en avoir besoin.
>     ¯\_(ツ)_/¯
>     @laurensell @sparkycollier @zehicle @emaganap @ashinclouds @davidmedberry @TomFifield Be there shortly!!
>     Esta noche en #ATV, Alsurcanalsur hará un recorrido por la extensa y variada programación del festivalsevilla de… https://t.co/OZYzf0Nb47
>     What a spectacular morning in the #LakeDistrict! This is Yew Tree Tarn near #Coniston right NOW! 🍂☀️ https://t.co/LyxjkuABD0
>     Tô com vontade de pastelzin, mas se eu parar pra comer, vou me atrasar muito mais
>     @AnnaRvr chaud 🥔
>     RT @CridaDemocracia: Quina casualitat. Cap d'aquets diaris obre portada amb la notícia que ahir es va acreditar al Congreso que… 
>     didn’t go to uni today and found out my groups been put in a seating plan, A SEATING PLAN!!!
>     RT @antynatalizm: za 5 mld lat słońce zamieni się w czerwonego olbrzyma, a kilkaset mln lat później i tak pochłonie naszą planetę, więc na…
>     @BigChang_ Anledning är helt enkelt att ett innehav inte ska få påverka i högre utsträckning relativt övriga andra.  Tanken är att alla innehav >
>     RT @ISzafranska: Do dopieri pierwsze poważne ostrzeżenie. Strach pomyśleć co mogą zawierać kolejne. https://t.co/ScLLtcD6gC
>     @LaurentM54 @CamilleTantale @GMeurice En cours..😂😂😂😂😂😂
>     Alf. ..
>     Ça veut dire ils sont en train de se branler ce qu'il leur reste de neurones , pas qu'ils ont réussi !
>      #edf #areva #hulot
>     RT @dalierzincanli: Oğluna pet şişe fırlatan babaya hapis veren yargı, bu şeref yoksunu magandaların dördünü serbest bıraktı. https://t.co/…
>     @tera_elin_jp @yue_Pfs 誰だお前
>     Se cubrirán las necesidades de empresarios afectados en la Av. Gobernadores y la Ría https://t.co/2B05BtMXPc #NoticiasDC #DiarioDeCampeche https://t.co/ATXBjzVl9q
>     Texas church shooter Devin Kelley has a history of domestic violence https://t.co/fo0rMyv7g6
>     @redy_eva_re hahhaa, iyaa bang .. ntr aku peluk dgn erat smbil pejamkan mata, smpe kita kebumi, asyiiik ya bang ?? 😀😂😜
>     En Vlamir a la 1 Usted puede detectar el @YonGoicoechea falso, aprovechador, irreverente repecto de @VoluntadPopular, con estocolmo o sin él. Iluso, táctico o estratega. Ud, decide. @Fundavade
>     RT @heldbybronnor: Petition for @TheVampsJames to sing higher on #NightAndDayTour rt to sign 🤘🤘🤘🤘🤘
>     RT @JeanetteEliz: @visconti1779 @TBGTNT @ky_33 @Estrella51Ahora @moonloght_22 @pouramours Thank you Meg. 😊 Have a beautiful day every… 
>     my sister measured me dicc
>     RT @CADCI: RECORDEU sempre que avui com el dia 3 els sindicats UGT i CCOO han fet costat a la patronal espanyola boicotejant… 
>     nude spy videos maria ozawa sex vids
>     RT @ARnews1936: US Navy tests hypersonic missiles that can hit any target on Earth in under an hour https://t.co/Pesi1SjrJo via  @IBTimesUK
>     @NamiqKocharli Ve çox maraqlıdı 140 simvola nece catdirmisan bunu?
>     The city was shining with the glory of God. It was shining bright like a very expensive jewel, like a jasper. It was clear as crystal.
>     @eysertwo ayynaa,hahahhaha welcome beeb basta ikaw hahaha
>     RT @JoyAnnReid: If Democrats DID take back the House, these ranking House members would become committee chairs ... with subpoena p… 
>     RT @HarryEStylesPH: 🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝🥝… 
>     RT @AfriqueTweet: 🔴 « Panama Papers » : Dan Gertler, roi de la #RDCongo et de l’Offshore avec la Bénédiction de Joseph Kabila https://t.co/…
>     @haries_rajawali BO aja
>     @i_bobbyk Sampe mules ga oppa?
>     RT @HugotDre: Gusto mo siyang tanungin about sa feelings niya kaya lang natatakot kang masaktan.
>     RT @SaharaRayxXx: Demi Rose Mawby Flaunts Her BIG Boobs In A Deep Cleavage As She Wears A Tiny Bikini Top Enjoying Ibiza Sun
>     https://t.co/3…
>     RT @MassVotingArmy: New Tagline will be release later at 5PM. Stay tuned! Lets do this ARMYs!
>     @czmoga @YouTube If you want to obtain good score then try #PTEAcademic. For sample Papers, visit https://t.co/szRJZmSaAs. Visit here https://t.co/NigQNQuM7C
>     Has she blamed her diary sec or researcher yet? #Patel
>     RT @aja_dreamer23: "I don't have a sugar daddy. I've never had a sugar daddy. If I wanted a sugar daddy, yes, I could probably go out… 
>     @TenguOficial Joe, haber preguntado que si la vendía y que por cuánto, y sobretodo si la entrega con monos o esos van a parte 😂😂
>     RT @bryanclemrj: Brant venceu por 250 sem contar a urna da discórdia. Olhem todas as urnas. Essa #URNA7 é uma vergonha. Nunca a famí… 
>     RT @MagazineCapital: Le Canard confirme les infos de Capital. En comptant les primes, elle aurait touché 116.000 euros de salaires indus. h…
>     @sportyfedz @delavinkisses @chinodelavin @imeecharlee @KissesNationPBB Wow all out support po. Ang gandang tignan.
>     RT @SSatsaheb: @realDonaldTrump @POTUS @EdWGillespie #जीने_की_राह
>     Who is the Great Saint according to Nostradamus nd other foretel… 
>     RT @NiallOfficial: Cannot wait for tomorrow ! @MarenMorris and I️ are super excited . @CountryMusic
>     RT @SamCoatesTimes: Will the Damian Green inquiry have concluded by the time decisions are announced about Priti Patel? https://t.co/xVcxy4…
>     Tweet tweet.
>     this is the first time in the history of me watching things that I saw two charas and brain just went 🤔
>     Also, it has snowed in parts of PA so no one better complain when I start singing Christmas music🎄❤️
>     Girls appreciate even the small efforts.
>
>     @mor1019 #MORPinoyBiga10 Di Ko Lang Masabi by Kisses Delavin
>     @KeirethLoL Le dijo la sartén al cazo.
>     RT @kuro_chrome: What a wonderful motivation message 😭💖
>     Let's all vote guys, no matter what! And even if we don't manage to close it… 
>     Siempre hay que sentirse positivo ⚡
>     Apoyar una huelga convocada por terroristas y llamar fascistas a los que salen a la calle con la rojigualda. Logica indepe.
>     @Real_A_Cullip1 My favourite was that South African rock dude 😂😂👏🏼👏🏼👏🏼👏🏼
>     @hatunturkish Kil yumagi amk az temiz ol temiz
>     RT @umvesgo: - vc parece ser nervosa kkkk
>     - kkkk sou nada sou tranquila https://t.co/76FPDgaQLu
>     @NashScarecrow89 @adriaraexx @Brazzers
>     これ。
>     RT @ObservadBinario: ¿Otro "sabotaje"? Nos van a dejar sin luz y sin agua en todo el país... Ya estamos sin dinero y in comida ni medici… 
>     RT @Punziella: Water. Earth. Fire. Air. Long ago, the four nations lived together in harmony. Then, everything changed when the Fi… 
>     RT @ramblingsloa: Be yourself;
>     Everyone else is already taken.
>
>     Oscar Wilde https://t.co/3n2VZMCKp4
>     RT @Autosport_Show: BREAKING: The 2018 @OfficialWRC season will be launched at #ASI18! 
>     All competing WRC teams, drivers, co-drivers an… 
>     RT @kayafoo: wash your face, drink water, eat healthy, be humble, stay positive and continue grinding.
>     RT @cristina_pardo: El jefe de la UDEF dice que Rajoy cobró en B y que Gürtel es "corrupción en estado puro", pero debatamos sobre la camis…
>     RT @rebeccaschreibt: Dirk und Stephan sind #obdachlos und suchen auf ungewöhnlichem Weg nach einer Bleibe. https://t.co/p8cg0wwN5l via @vic…
>     @MarcherLord1 Haha 😂 never heard so much fuss about absolutely  2 5ths of tit all
>     RT @rHarry_Styls: Here's a Harry Styles smiling appreciation tweet 
>     You're welcome https://t.co/uCnSy1olMI
>     RT @ronflexzer: c'est fou comme le soir je remets toute ma vie en question
>     RT @chadwickboseman: It's hard for a good man to be king...sat down with @CNET for an in-depth talk on all things #BlackPanther, tech, a… 
>     RT @beitacabronis: -Cariño , ¿por qué vienes tan contenta?
>     -Porque no sabía que era multiorgásmica
>     RT @UniofOxford: A clear vision for the future: can #AR give sight back to 100 million people? @OxSightUK #StartedInOxford… 
>     RT @ronflexzer: c'est fou comme le soir je remets toute ma vie en question
>     RT @bretmanrock: If you guys see me in public and my nose is oily.... please let me know.😫
>
>     I promise I won't think you're rude.. I'll hone…
>     RT @jcmaningat: Yesterday: Underground women's group MAKIBAKA joined the lightning rally in Mendiola to commemorate the victory of… 
>     @rezaafathurizqi @karman_mustamin Kalijodo izinnya sama preman", dan bnyak yg melanggar peraturan.
>     RT @MannyMua733: Are the Dolan twins really only 17? Asking for a friend
>     RT @XHSports: It's truely a fresh try for @andy_murray dressed like this in a Glasgow charity match vs. @rogerfederer #Federer. S… 
>     @RifantiFanti yaiyalahh..
>     Tb to DC’s first night out in Leeds when he was home by 1.30 and sick in my bed ahahahaha https://t.co/yrGihYVIak
>     @Alice_Staniford @BikeShedDevon How do you find the disk brakes in the wet weather??
>     Sigo con 140 caracteres. Gracias, Twitter. #TuitSerio
>     RT @cdrsabadell: 🎶🎶🎶
>     Som una gentada i ens acompanya bona música!
>     Lluita i alegria que és #VagaGeneral8N!!
>
>     #CDRenXarxa… 
>     RT @bhogleharsha: Only 3 days left for #InquizitiveMinds, India's biggest quiz contest. Catch me LIVE on Sunday, 12 Nov, in Bengaluru. http…
>     @5stars_hanaebi ラスボスにふさわしい
>     @HiKONiCO Is it not okay to be white?
>     Po tym wywiadzie nie przyjmuje juz zadnych argumentow broniacych decyzje Prezydenta.BASTA! https://t.co/e4dgkBWm2u
>     RT @Green20William: Our CLUB pays it's own way.Pays all its bills.Step up anyone who can prove otherwise👍😎 https://t.co/rVBc2Jw4mc
>     RT @siirist: "Varlığının birileri için kıymetli oluşu paha biçilemez. Çünkü senin tutunacak dalın kalmasa bile sen birileri için bir dalsın…
>     RT @EdMay_0328: Ang baba ng views.. We can do better.
>
>     #MAYWARDMegaIGtorial
>     @norimasa1914 しれとー笑
>     @QSwearyLIII Its going to end in tears
>     current mood: vampire princess
>
>     😘
>     RT @_amalves: Se até Jesus não agradou todo mundo quem sou eu pra agradar não é mesmo, os meme tá aí quem gostou usa, quem não gostou paciê…
>     NERV THIS! Hahahahahahahahahaa
>     RT @OlivierGuitta: #SaudiArabia government is aiming to confiscate cash and other assets worth as much as $800 billion in its broadeni… 
>     Dev_Fadnavis: It’s just one year and these huge numbers have started speaking already!
>     Maharashtra salutes Hon PM … https://t.co/IU0VgVFU8L
>     RT @JovenEuropeo: A ver cómo conjugáis lo de la libertad y el "derecho a decididir" con todas las coacciones y piquetes que estáis ej… 
>     @vitortardin Falta de dar né
>     RT @andevaa: Oli pakko tehdä tilastoja Divarin 5-minuutin rangaistuksista, kun alkoi pistämään silmään tämä isojen jäähyjen suma… 
>     릠들 aaa 6시 부터 레카시작임?
>     #REALFI Vilar NON på hanen eller kommer det ytterligare våg,,, minns pressen tidigare i Hamlet,,, https://t.co/cLNnmuOEui
>     RT @SokoAnalyst: We are going to see firms folding up because whether we like it or not, consumers listen to Raila & the impact is building…
>     Sad news this morning  - little Banty is missing, presumed picked up by either a Fox, Stoat or Tawny.  Had the freedom of the garden as the other chickens picked on her, but not in her shed at lock up last night. Hope she didn't suffer too much 😢😢😢 https://t.co/UTuYSeFXRM
>     RT @pemarisanchez: Feliz de compartir este nuevo proyecto con tan maravillosos compañeros, en la que ha sido tantas veces mi casa… 
>     RT @BangBangtan_Esp: [!] Se confirma que BTS será  invitado al programa "Jimmy Kimmel Live" mientras están en los Estados Unidos para la… 
>     stuck in this illusion called us, stuck in this puzzle and it doesn't get any better 🎧
>     RT @I_CSC: 📢 Recordeu: 
>     ⛔Cap mena d'acció violenta 
>     ✋Ignoreu les provocacions
>     🤝Suport mutu
>     📲No difondre rumors
>     👌Participeu de… 
>     @pvoorbach @JelmerGeerds Komt het dit seizoen nog goed met je club rooie vriend? Ik heb er  persoonlijk een hard hoofd in.
>     RT @CorreaDan: With gov’t funding plummeting, U.S. researchers need to find new ways to fund R&D - My article w/ @scott_andes
>
>     https://t.co…
>     I️ just wish the sleep would come...knock me out so I️ feel nothing...#insomnia
>     @riethegreat_ haboo ate oo create account, rpg game na sya and u get to socialize and stuff
>     Ecovention Europe: Art to Transform Ecologies, 1957-2017 (part 2) “Men always reach for technology, for development. They insist it will bring us to higher levels of progress. They haven’t the patience to work with slow-growing plants...” WMMNA - https://t.co/NA3spa1g4j https://t.co/gWUG4CxZll