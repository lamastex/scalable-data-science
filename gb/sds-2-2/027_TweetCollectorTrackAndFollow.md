[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Go to SparkUI and see if a streaming job is already running. If so you need to terminate it before starting a new streaming job. Only one streaming job can be run on the DB CE.

    // this will make sure all streaming job in the cluster are stopped
    StreamingContext.getActive.foreach{ _.stop(stopSparkContext = false) } 

Load your twitter credentials (secretly!).

    val ssc = StreamingContext.getActiveOrCreate(streamFunc)

> ssc: org.apache.spark.streaming.StreamingContext = org.apache.spark.streaming.StreamingContext@16152a25

    ssc.start()
    //ssc.awaitTerminationOrTimeout(timeoutJobLength) // you only need one of these to start

    // this will make sure all streaming job in the cluster are stopped - raaz
    StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 

    display(dbutils.fs.ls(outputDirectoryRoot))

| dbfs:/datasets/tweetsStreamTmp/2017/ | 2017/ | 0.0 |
|--------------------------------------|-------|-----|

    val rawDF = fromParquetFile2DF(outputDirectoryRoot+"/2017/10/05/09/*/*") //.cache()
    val TTTsDF = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(rawDF)).cache()

> rawDF: org.apache.spark.sql.DataFrame = \[tweetAsJsonString: string\] TTTsDF: org.apache.spark.sql.Dataset\[org.apache.spark.sql.Row\] = \[CurrentTweetDate: timestamp, CurrentTwID: bigint ... 32 more fields\]

Tweet Collector - capture live tweets
=====================================

### by tracking a list of strings and following a list of users

In the previous notebook we were capturing tweets from the public streams (global collection of roughly 1% of all Tweets - note what's exactly available from the full twitter social media network, i.e. *all* status updates in the planet, for such free collection is not exactly known in terms of sub-sampling strategies, etc. This is Twitter's proprietary information. However, we can assume it is a random sample of roughly 1% of all tweets).

In this notebook, we can modify the collector to focus on specific communications of interest to us. Specifically, by including a list of strings to track and a list of twitter user-IDs to follow.

For this we will first `%run` the `ExtendedTwitterUtils` and `TTTDFfunctions` notebooks.

Now, let's extend our function.

    import com.google.gson.Gson 
    import org.apache.spark.sql.functions._
    import org.apache.spark.sql.types._

    val outputDirectoryRoot = "/datasets/tweetsStreamTmp" // output directory
    val batchInterval = 1 // in minutes
    val timeoutJobLength =  batchInterval * 5

    var newContextCreated = false
    var numTweetsCollected = 0L // track number of tweets collected
    //val conf = new SparkConf().setAppName("TrackedTweetCollector").setMaster("local")
    // This is the function that creates the SteamingContext and sets up the Spark Streaming job.
    def streamFunc(): StreamingContext = {
      // Create a Spark Streaming Context.
      val ssc = new StreamingContext(sc, Minutes(batchInterval))
      // Create the OAuth Twitter credentials 
      val auth = Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
      
      val track = List("@raazozone", "#MakeDataGreatAgain","sds-2-2 rules!")// just added for some live tests
      //val track = List.empty // if you do not want to track by any string
      
      val follow = TwitterIdsDirectlyFromCsv //UKBuffList
      //val follow = List.empty // if you do not want to folow any specific twitter user
      
      // Create a Twitter Stream for the input source.  
      val twitterStream = ExtendedTwitterUtils.createStream(ssc, auth, track, follow)
      // Transform the discrete RDDs into JSON
      val twitterStreamJson = twitterStream.map(x => { val gson = new Gson();
                                                     val xJson = gson.toJson(x)
                                                     xJson
                                                   }) 
      // take care
      val partitionsEachInterval = 1 // This tells the number of partitions in each RDD of tweets in the DStream.
      
      // what we want done with each discrete RDD tuple: (rdd, time)
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
      newContextCreated = true
      ssc
    }

> import com.google.gson.Gson import org.apache.spark.sql.functions.\_ import org.apache.spark.sql.types.\_ outputDirectoryRoot: String = /datasets/tweetsStreamTmp batchInterval: Int = 1 timeoutJobLength: Int = 5 newContextCreated: Boolean = false numTweetsCollected: Long = 0 streamFunc: ()org.apache.spark.streaming.StreamingContext

    import scala.collection.mutable.ListBuffer

    val TwitterIdsDirectlyFromCsv = sqlContext.read.format("com.databricks.spark.csv")
                            .option("header", "false")
                            .option("inferSchema", "true")
                            .load("/FileStore/tables/zo6licf21496412465690/candidates_newspapers_bloggers_unique-741bb.txt")
                            .select($"_c0")//.filter($"C1".contains("@"))
                            .rdd.map({case Row(val1: Long) => val1}).collect().to[ListBuffer]

> import scala.collection.mutable.ListBuffer TwitterIdsDirectlyFromCsv: scala.collection.mutable.ListBuffer\[Long\] = ListBuffer(1002351, 100260784, 100520303, 1006402848, 1010045504, 101262304, 10126702, 1013045964, 1014064806, 101520906, 101712079, 1017280993, 101747500, 102016860, 102023574, 102107018, 1022855215, 102491550, 1026073136, 1026135084, 1028318222, 102958885, 103571919, 103620582, 1038286334, 103879974, 1040075106, 104130247, 104647856, 104794598, 1048278487, 104845115, 104868360, 1052958798, 105504412, 105587507, 105619565, 105800463, 105838665, 106115066, 106273221, 1067829343, 106831393, 107127155, 1071937050, 1072198009, 1072226575, 1074546476, 107722321, 1077971, 1080905119, 10835772, 108437760, 108587399, 108654496, 108882900, 1095280981, 10955042, 109845429, 110214790, 110268940, 110405901, 110544175, 110638978, 110686699, 110767433, 110789426, 111013369, 111411358, 1114522266, 1114608210, 1115385206, 1117307833, 112212596, 1123375357, 112398730, 112521557, 112761860, 112796637, 112835358, 1129434462, 1130818518, 1131868777, 1134752502, 113476721, 113491007, 113683399, 114037319, 1140991255, 1141363489, 114159336, 1142344064, 1143156416, 11435642, 114505454, 114521927, 114689761, 114704539, 114749134, 114781264, 114789749, 1148079204, 114873829, 115061265, 115494227, 11564602, 11578732, 115887294, 115913494, 11616772, 1163395104, 116459535, 1165771298, 11679892, 116832294, 116847269, 116864791, 116895547, 117110732, 117207983, 117489156, 117597342, 1176535470, 117777690, 118048017, 118117970, 1183017690, 118363891, 118379616, 118473432, 118984824, 1190173345, 11906032, 119074643, 1190945166, 119333464, 119710140, 12004752, 120236641, 1205894156, 120720108, 120750901, 1208909684, 121070777, 121109384, 121127090, 121192398, 1215900859, 1216506050, 121762421, 1226227994, 1227716558, 1235025320, 123828557, 124152473, 124154899, 124170781, 124270074, 124312153, 124499422, 1249324855, 1249425925, 125151345, 125196275, 125270251, 125294790, 125312818, 1254079789, 1258840818, 1261826862, 126393882, 12695662, 1269652682, 1271714653, 127551563, 127619354, 12790962, 12792012, 127970776, 128216887, 1284043356, 1284146730, 12851252, 128555095, 128558029, 128590458, 128642272, 128989290, 129175534, 129293635, 1294741, 129569947, 1296260905, 1296377024, 1296896923, 129951559, 1300328653, 130092487, 130133607, 130154450, 1302003805, 130255155, 130280613, 13052992, 130532192, 1305528373, 130564260, 130640704, 130778462, 130857126, 130992867, 131120978, 131191788, 1312102561, 131517399, 131862459, 131926473, 131957012, 1320215196, 1321222508, 132230647, 132236753, 132286037, 132490359, 1325563478, 132570708, 1328897358, 132929062, 133012476, 133107161, 133406325, 133519418, 133719418, 1337725045, 1339653144, 134066573, 1342705754, 1344130148, 134618982, 134738764, 134802103, 1350441445, 1351389578, 1354610803, 135474512, 135629930, 136004952, 136009310, 13666, 137055756, 1374483169, 13745872, 1374937922, 137708541, 1377364789, 13776852, 138179747, 1384470925, 13868362, 1386916794, 13870942, 1387141939, 138723904, 13939772, 1394164963, 1395516325, 140020746, 140024858, 14010702, 140113193, 140594592, 140604687, 14073364, 14077382, 1407768792, 140837603, 14085096, 14087783, 14091066, 140945084, 141015432, 14104027, 14128528, 14138785, 14146330, 1415275334, 14153187, 14157134, 1417251, 1417840321, 1417932692, 1418358025, 14190551, 14197060, 14201606, 1421461974, 14227196, 14260148, 142741036, 142776016, 14284260, 1430228162, 14307405, 143212610, 14321959, 14328066, 143386976, 143405324, 143508762, 14362873, 143739561, 1437815449, 143861319, 14395178, 1441634533, 1442028488, 1444635962, 144561613, 14467310, 144754071, 14476016, 144791011, 144929619, 14515799, 14523801, 145321979, 14561015, 1456266271, 145671928, 14567982, 14587402, 14590758, 1464467370, 146465594, 1466936317, 14685924, 14691032, 14692686, 1469686752, 14700117, 1470747960, 14710921, 14717104, 14728535, 14741951, 14758838, 147628329, 14766123, 147734164, 1478483473, 148276510, 14834340, 148380737, 148625997, 1486264482, 14871717, 1489691, 14933304, 149480292, 149689194, 1498213262, 14991331, 15010349, 150574892, 15082486, 15133808, 1513382018, 15143478, 15157283, 151962390, 15253147, 1530621674, 1531710163, 15348883, 15357423, 153810216, 153817010, 153914621, 1539233137, 1541364193, 15438913, 15439395, 1544053374, 15442465, 15453062, 1546130388, 154661114, 15480520, 1548114919, 1548391070, 15484198, 154852856, 154943205, 1551071150, 155150437, 1552552699, 1553831654, 15557246, 155704077, 155712003, 15572774, 15580900, 155892458, 155927976, 1560551628, 1565387353, 15672615, 1568971819, 15700177, 15710120, 15710574, 15712527, 15726425, 1574883223, 15778426, 15798091, 158021529, 158150328, 15820821, 158315486, 15850290, 15865878, 15865896, 15919119, 1593906744, 159467748, 15964196, 15975081, 1598090347, 1599131336, 159992511, 1606157958, 1608015174, 160856730, 160865889, 1608671497, 160926944, 16095047, 161056882, 1610769571, 16114437, 16133363, 16134235, 16139649, 16180961, 1619874846, 1622761, 16231640, 162472533, 16258968, 1628552390, 1628762916, 163175714, 1633543440, 16343974, 16364632, 16391110, 16394067, 16399949, 164208335, 164226176, 16454546, 1645585171, 1650144662, 1651132772, 1652897768, 1653808938, 165503961, 1655569938, 16596200, 166598394, 16667402, 16672510, 16681111, 16734751, 16745361, 167817393, 167867714, 1680364640, 1681525201, 16824319, 1685376607, 168592013, 16884084, 16887175, 1691801, 16935734, 16973333, 169864622, 169898513, 1699566222, 1699862954, 17020962, 1704239983, 1704469674, 17062358, 1707450434, 17113430, 17133897, 17166900, 1719936289, 17201036, 172090468, 1722565140, 1722829178, 17298241, 173089105, 173112173, 17315312, 1731554581, 173421592, 17369951, 17385903, 174396155, 17442320, 17484283, 175005016, 17534929, 175786071, 17645505, 176721099, 176835532, 17685009, 177180785, 17735590, 17753033, 17787845, 17799713, 178270189, 17865252, 17895820, 179278869, 17939037, 17963897, 179698135, 180013616, 18020612, 18029775, 18096679, 18099795, 181105871, 181243457, 18166561, 18196651, 182342346, 18355024, 18449366, 1849274167, 185306056, 18551433, 185790986, 185794012, 18627119, 18632946, 18650093, 1865540413, 18668857, 186890864, 18713254, 1871982156, 187210167, 1872769464, 187553649, 187632184, 18764841, 18765853, 18772184, 1879232120, 188080343, 18809812, 18874097, 18887526, 18904746, 189280488, 18949349, 18949452, 18951643, 18955506, 1897390027, 1897436946, 18979962, 18980276, 18981376, 189825312, 19058678, 19063664, 19087569, 19088015, 19092343, 19111384, 1912212774, 19126349, 1914854370, 1917155592, 19177609, 191807697, 19184154, 1923235243, 192935794, 19295262, 19303349, 193302048, 19335378, 19346439, 1935729446, 19364759, 19371828, 19407599, 194427765, 19447175, 1945812050, 1949509532, 194977936, 19530134, 19530289, 19530813, 19534396, 19534873, 19542502, 19561925, 19562228, 195752750, 19586695, 19588385, 19589086, 19589280, 19599333, 19608199, 19619404, 19620330, 1962850256, 1963428127, 19644592, 19647329, 19650715, 19660870, 19672313, 19715552, 1971643310, 19758148, 197804641, 1978270322, 19811190, 19817922, 19818494, 19825835, 198461357, 198527033, 19858924, 19900768, 19902709, 19915728, 19925839, 19945211, 19973305, 19977759, 19981832, 1998841, 2000001, 20000725, 20005728, 20034914, 20035457, 200519122, 20052899, 200538258, 20056279, 20056740, 200700960, 20132840, 20142835, 20148039, 20181974, 20187833, 20188620, 20202181, 202174004, 20225578, 20226550, 20228559, 20229729, 20233133, 20238327, 20255744, 202610289, 20281540, 20304026, 20317326, 20325923, 203488374, 20356313, 20362684, 20373578, 20392734, 20424362, 20428671, 20440432, 20440951, 20441118, 20442930, 20473134, 204983911, 20513820, 20516017, 205319795, 205770556, 20631374, 20668369, 20685935, 20688804, 206986175, 20703607, 20715695, 20720379, 20761862, 207697553, 207707329, 20807083, 20844418, 208484718, 208541506, 20856796, 209020126, 209029473, 20919048, 20974967, 20975999, 20992801, 20995648, 20995745, 21001266, 21019443, 21084719, 21106082, 21111483, 21112612, 21125405, 21129480, 21156414, 211626602, 21174263, 21188077, 211883434, 211994193, 21202851, 21205374, 212209552, 21235736, 21253, 21313645, 21346129, 21406457, 214066898, 21408041, 214128669, 214625728, 2147777113, 214902404, 21498258, 21527566, 21528045, 215344789, 215437218, 2157036506, 21575054, 216122857, 216299334, 216327829, 2164507202, 216516145, 21666641, 21713090, 217148014, 21736214, 2173779986, 2174023854, 217414958, 21744895, 21746513, 2174657848, 21750720, 2175891740, 21769986, 21772748, 217841824, 21801576, 21806432, 2180720323, 21807741, 2181921840, 2184358365, 218546028, 218789670, 2191049840, 21910500, 219298196, 219330249, 2196660992, 219976700, 22009086, 22016436, 22021978, 22026637, 2202981242, 2202984493, 22031058, 2204660084, 220480870, 2205165174, 2205752888, 22084741, 2208648430, 220974548, 221005000, 22126363, 22146373, 22159580, 2216423304, 221693300, 222058929, 222271232, 22239898, 222419520, 222477026, 222748037, 222762589, 22285702, 22294221, 2230539338, 2231516528, 2234881, 223539098, 2236032312, 22378075, 2238813028, 22398060, 224201085, 224260144, 2244704761, 2245759171, 2246002939, 224655400, 22474050, 22495264, 22504375, 22505462, 22553763, 225857392, 22636185, 226890552, 22702102, 227023883, 2271025438, 227330399, 2276518495, 227700499, 22770532, 2278921206, 22804395, 22812734, 2282676800, 228360358, 22844492, 22902578, 2291166992, 229425142, 2294672197, 22959752, 229643603, 229846131, 2300287434, 2303764736, 23055689, 23058983, 23065145, 2307400682, 23081797, 2308386679, 23148062, 231859658, 231884341, 231948144, 2319613536, 23225946, 232276399, 2323710210, 2327654917, 2327696834, 2328913238, 2329328754, 2330902064, 233310243, 2335638732, 23364828, 2338660974, 2341079723, 23424533, 2342586348, 23431198, 23452598, 23452834, 2347509589, 2348603018, 2350624098, 23507978, 2353225063, 2359940569, 23610891, 2362826880, 2363670413, 236395049, 23642018, 236588290, 23663242, 236786367, 2369038173, 236953412, 2369816126, 2370114460, 2371641993, 2373233302, 23749162, 2375470213, 2376645298, 23769106, 237700394, 2382227424, 238298521, 2386746563, 238696425, 2389047492, 2389307822, 2391791419, 2393265698, 23951776, 239584959, 23970391, 23984102, 2399289469, 240030844, 240039595, 240202308, 240422937, 24045870, 2406093795, 240738030, 240808845, 24083587, 241279770, 2416910936, 24181918, 24211594, 24228290, 24234737, 2424046740, 24246234, 2425571623, 242851222, 242943134, 243135658, 243306810, 243828715, 244025084, 2442275375, 24429963, 24444965, 24447643, 244489735, 244540519, 24467456, 2455397312, 245745762, 245849058, 2459629244, 2469000333, 2472200292, 24727170, 2472980792, 2474868364, 247511435, 2476759038, 247762852, 247833805, 2478823452, 2480128513, 248186795, 2483456906, 2485029506, 2486992562, 248908429, 24909051, 24909650, 2494392186, 249709671, 249845912, 2499497407, 2499857461, 250091875, 250311545, 250313359)

Let's import a list of twitterIDS of political interest in the UK.

    display(dbutils.fs.ls(outputDirectoryRoot+"/2017/10/05/09/")) // keep adding sub-dirs and descent into time-tree'd directory hierarchy

| dbfs:/datasets/tweetsStreamTmp/2017/10/05/09/1507197540000/ | 1507197540000/ | 0.0 |
|-------------------------------------------------------------|----------------|-----|

> import twitter4j.\_ import twitter4j.auth.Authorization import twitter4j.conf.ConfigurationBuilder import twitter4j.auth.OAuthAuthorization import org.apache.spark.streaming.\_ import org.apache.spark.streaming.dstream.\_ import org.apache.spark.storage.StorageLevel import org.apache.spark.streaming.receiver.Receiver

> defined class ExtendedTwitterReceiver

> defined class ExtendedTwitterInputDStream

> import twitter4j.Status import twitter4j.auth.Authorization import org.apache.spark.storage.StorageLevel import org.apache.spark.streaming.StreamingContext import org.apache.spark.streaming.dstream.{ReceiverInputDStream, DStream} defined object ExtendedTwitterUtils

> done running the extendedTwitterUtils2run notebook - ready to stream from twitter

> USAGE: val df = tweetsDF2TTTDF(tweetsJsonStringDF2TweetsDF(fromParquetFile2DF("parquetFileName"))) val df = tweetsDF2TTTDF(tweetsIDLong\_JsonStringPairDF2TweetsDF(fromParquetFile2DF("parquetFileName"))) import org.apache.spark.sql.types.{StructType, StructField, StringType} import org.apache.spark.sql.functions.\_ import org.apache.spark.sql.types.\_ import org.apache.spark.sql.ColumnName import org.apache.spark.sql.DataFrame fromParquetFile2DF: (InputDFAsParquetFilePatternString: String)org.apache.spark.sql.DataFrame tweetsJsonStringDF2TweetsDF: (tweetsAsJsonStringInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame tweetsIDLong\_JsonStringPairDF2TweetsDF: (tweetsAsIDLong\_JsonStringInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame tweetsDF2TTTDF: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame tweetsDF2TTTDFWithURLsAndHastags: (tweetsInputDF: org.apache.spark.sql.DataFrame)org.apache.spark.sql.DataFrame

> twitter OAuth Credentials loaded MyconsumerKey: String MyconsumerSecret: String Mytoken: String MytokenSecret: String import twitter4j.auth.OAuthAuthorization import twitter4j.conf.ConfigurationBuilder

    display(TTTsDF)

    // this will make sure all streaming job in the cluster are stopped - raaz
    StreamingContext.getActive.foreach { _.stop(stopSparkContext = false) } 

    // this will delete what we collected to keep the disk usage tight and tidy
    dbutils.fs.rm(outputDirectoryRoot, true) 

> res26: Boolean = true

