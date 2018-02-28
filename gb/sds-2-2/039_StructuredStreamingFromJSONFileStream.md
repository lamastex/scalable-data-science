[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Structured Streaming using Scala DataFrames API - Exercise
==========================================================

Apache Spark 2.0 adds the first version of a new higher-level stream processing API, Structured Streaming. In this notebook we are going to take a quick look at how to use DataFrame API to build Structured Streaming applications. We want to compute real-time metrics like running counts and windowed counts on a stream of timestamped actions (e.g. Open, Close, etc).

To run this notebook, import it to Databricks Community Edition and attach it to a **Spark 2.0 (Scala 2.10)** cluster.

This is built on the public databricks notebook [importable from here](https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/4012078893478893/295447656425301/5985939988045659/latest.html).

``` scala
// To make sure that this notebook is being run on a Spark 2.0+ cluster, let's see if we can access the SparkSession - the new entry point of Apache Spark 2.0.
// If this fails, then you are not connected to a Spark 2.0 cluster. Please recreate your cluster and select the version to be "Spark 2.0 (Scala 2.10)".
spark
```

>     res0: org.apache.spark.sql.SparkSession = org.apache.spark.sql.SparkSession@43f8a1be

Sample Data
-----------

We have some sample action data as files in `/databricks-datasets/structured-streaming/events/` which we are going to use to build this appication. Let's take a look at the contents of this directory.

``` fs
ls /databricks-datasets/structured-streaming/events/
```

| path                                                               | name         | size    |
|--------------------------------------------------------------------|--------------|---------|
| dbfs:/databricks-datasets/structured-streaming/events/file-0.json  | file-0.json  | 72530.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-1.json  | file-1.json  | 72961.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-10.json | file-10.json | 73025.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-11.json | file-11.json | 72999.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-12.json | file-12.json | 72987.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-13.json | file-13.json | 73006.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-14.json | file-14.json | 73003.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-15.json | file-15.json | 73007.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-16.json | file-16.json | 72978.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-17.json | file-17.json | 73008.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-18.json | file-18.json | 73002.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-19.json | file-19.json | 73014.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-2.json  | file-2.json  | 73007.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-20.json | file-20.json | 72987.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-21.json | file-21.json | 72983.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-22.json | file-22.json | 73009.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-23.json | file-23.json | 72985.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-24.json | file-24.json | 73020.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-25.json | file-25.json | 72980.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-26.json | file-26.json | 73002.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-27.json | file-27.json | 73013.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-28.json | file-28.json | 73005.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-29.json | file-29.json | 72977.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-3.json  | file-3.json  | 72996.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-30.json | file-30.json | 73009.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-31.json | file-31.json | 73008.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-32.json | file-32.json | 72982.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-33.json | file-33.json | 73033.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-34.json | file-34.json | 72985.0 |
| dbfs:/databricks-datasets/structured-streaming/events/file-35.json | file-35.json | 72974.0 |

Truncated to 30 rows

There are about 50 JSON files in the directory. Let's see what each JSON file contains.

``` fs
head /databricks-datasets/structured-streaming/events/file-0.json
```

>     [Truncated to first 65536 bytes]
>     {"time":1469501107,"action":"Open"}
>     {"time":1469501147,"action":"Open"}
>     {"time":1469501202,"action":"Open"}
>     {"time":1469501219,"action":"Open"}
>     {"time":1469501225,"action":"Open"}
>     {"time":1469501234,"action":"Open"}
>     {"time":1469501245,"action":"Open"}
>     {"time":1469501246,"action":"Open"}
>     {"time":1469501248,"action":"Open"}
>     {"time":1469501256,"action":"Open"}
>     {"time":1469501264,"action":"Open"}
>     {"time":1469501266,"action":"Open"}
>     {"time":1469501267,"action":"Open"}
>     {"time":1469501269,"action":"Open"}
>     {"time":1469501271,"action":"Open"}
>     {"time":1469501282,"action":"Open"}
>     {"time":1469501285,"action":"Open"}
>     {"time":1469501291,"action":"Open"}
>     {"time":1469501297,"action":"Open"}
>     {"time":1469501303,"action":"Open"}
>     {"time":1469501322,"action":"Open"}
>     {"time":1469501335,"action":"Open"}
>     {"time":1469501344,"action":"Open"}
>     {"time":1469501346,"action":"Open"}
>     {"time":1469501349,"action":"Open"}
>     {"time":1469501357,"action":"Open"}
>     {"time":1469501366,"action":"Open"}
>     {"time":1469501371,"action":"Open"}
>     {"time":1469501375,"action":"Open"}
>     {"time":1469501375,"action":"Open"}
>     {"time":1469501381,"action":"Open"}
>     {"time":1469501392,"action":"Open"}
>     {"time":1469501402,"action":"Open"}
>     {"time":1469501407,"action":"Open"}
>     {"time":1469501410,"action":"Open"}
>     {"time":1469501420,"action":"Open"}
>     {"time":1469501424,"action":"Open"}
>     {"time":1469501438,"action":"Open"}
>     {"time":1469501442,"action":"Close"}
>     {"time":1469501462,"action":"Open"}
>     {"time":1469501480,"action":"Open"}
>     {"time":1469501488,"action":"Open"}
>     {"time":1469501489,"action":"Open"}
>     {"time":1469501491,"action":"Open"}
>     {"time":1469501503,"action":"Open"}
>     {"time":1469501505,"action":"Open"}
>     {"time":1469501509,"action":"Open"}
>     {"time":1469501513,"action":"Open"}
>     {"time":1469501517,"action":"Open"}
>     {"time":1469501520,"action":"Open"}
>     {"time":1469501525,"action":"Open"}
>     {"time":1469501533,"action":"Open"}
>     {"time":1469501539,"action":"Open"}
>     {"time":1469501540,"action":"Open"}
>     {"time":1469501541,"action":"Open"}
>     {"time":1469501543,"action":"Open"}
>     {"time":1469501544,"action":"Open"}
>     {"time":1469501545,"action":"Close"}
>     {"time":1469501545,"action":"Open"}
>     {"time":1469501547,"action":"Open"}
>     {"time":1469501552,"action":"Open"}
>     {"time":1469501557,"action":"Open"}
>     {"time":1469501559,"action":"Open"}
>     {"time":1469501560,"action":"Open"}
>     {"time":1469501560,"action":"Open"}
>     {"time":1469501565,"action":"Open"}
>     {"time":1469501566,"action":"Open"}
>     {"time":1469501574,"action":"Open"}
>     {"time":1469501575,"action":"Open"}
>     {"time":1469501575,"action":"Open"}
>     {"time":1469501578,"action":"Open"}
>     {"time":1469501581,"action":"Open"}
>     {"time":1469501584,"action":"Open"}
>     {"time":1469501600,"action":"Open"}
>     {"time":1469501601,"action":"Open"}
>     {"time":1469501603,"action":"Open"}
>     {"time":1469501610,"action":"Open"}
>     {"time":1469501620,"action":"Open"}
>     {"time":1469501621,"action":"Open"}
>     {"time":1469501625,"action":"Open"}
>     {"time":1469501625,"action":"Close"}
>     {"time":1469501626,"action":"Open"}
>     {"time":1469501631,"action":"Open"}
>     {"time":1469501632,"action":"Open"}
>     {"time":1469501632,"action":"Open"}
>     {"time":1469501638,"action":"Open"}
>     {"time":1469501643,"action":"Open"}
>     {"time":1469501646,"action":"Open"}
>     {"time":1469501662,"action":"Open"}
>     {"time":1469501662,"action":"Open"}
>     {"time":1469501662,"action":"Open"}
>     {"time":1469501663,"action":"Open"}
>     {"time":1469501667,"action":"Open"}
>     {"time":1469501674,"action":"Open"}
>     {"time":1469501675,"action":"Open"}
>     {"time":1469501678,"action":"Close"}
>     {"time":1469501680,"action":"Open"}
>     {"time":1469501685,"action":"Open"}
>     {"time":1469501686,"action":"Open"}
>     {"time":1469501689,"action":"Open"}
>     {"time":1469501691,"action":"Open"}
>     {"time":1469501694,"action":"Open"}
>     {"time":1469501696,"action":"Close"}
>     {"time":1469501702,"action":"Open"}
>     {"time":1469501703,"action":"Open"}
>     {"time":1469501704,"action":"Open"}
>     {"time":1469501706,"action":"Open"}
>     {"time":1469501706,"action":"Open"}
>     {"time":1469501710,"action":"Open"}
>     {"time":1469501715,"action":"Open"}
>     {"time":1469501717,"action":"Open"}
>     {"time":1469501719,"action":"Open"}
>     {"time":1469501719,"action":"Open"}
>     {"time":1469501734,"action":"Open"}
>     {"time":1469501739,"action":"Open"}
>     {"time":1469501740,"action":"Open"}
>     {"time":1469501747,"action":"Open"}
>     {"time":1469501749,"action":"Open"}
>     {"time":1469501749,"action":"Close"}
>     {"time":1469501754,"action":"Open"}
>     {"time":1469501755,"action":"Open"}
>     {"time":1469501756,"action":"Open"}
>     {"time":1469501756,"action":"Open"}
>     {"time":1469501757,"action":"Open"}
>     {"time":1469501758,"action":"Open"}
>     {"time":1469501759,"action":"Open"}
>     {"time":1469501761,"action":"Open"}
>     {"time":1469501764,"action":"Open"}
>     {"time":1469501772,"action":"Open"}
>     {"time":1469501772,"action":"Open"}
>     {"time":1469501776,"action":"Close"}
>     {"time":1469501780,"action":"Open"}
>     {"time":1469501782,"action":"Open"}
>     {"time":1469501783,"action":"Open"}
>     {"time":1469501785,"action":"Open"}
>     {"time":1469501789,"action":"Open"}
>     {"time":1469501795,"action":"Open"}
>     {"time":1469501802,"action":"Open"}
>     {"time":1469501802,"action":"Open"}
>     {"time":1469501806,"action":"Open"}
>     {"time":1469501813,"action":"Open"}
>     {"time":1469501817,"action":"Open"}
>     {"time":1469501818,"action":"Open"}
>     {"time":1469501819,"action":"Close"}
>     {"time":1469501828,"action":"Open"}
>     {"time":1469501829,"action":"Open"}
>     {"time":1469501830,"action":"Open"}
>     {"time":1469501833,"action":"Open"}
>     {"time":1469501835,"action":"Open"}
>     {"time":1469501837,"action":"Open"}
>     {"time":1469501838,"action":"Open"}
>     {"time":1469501840,"action":"Open"}
>     {"time":1469501845,"action":"Open"}
>     {"time":1469501848,"action":"Open"}
>     {"time":1469501853,"action":"Open"}
>     {"time":1469501855,"action":"Open"}
>     {"time":1469501861,"action":"Close"}
>     {"time":1469501861,"action":"Open"}
>     {"time":1469501862,"action":"Open"}
>     {"time":1469501863,"action":"Open"}
>     {"time":1469501865,"action":"Open"}
>     {"time":1469501873,"action":"Open"}
>     {"time":1469501884,"action":"Open"}
>     {"time":1469501895,"action":"Open"}
>     {"time":1469501904,"action":"Open"}
>     {"time":1469501907,"action":"Open"}
>     {"time":1469501909,"action":"Close"}
>     {"time":1469501909,"action":"Open"}
>     {"time":1469501911,"action":"Open"}
>     {"time":1469501929,"action":"Open"}
>     {"time":1469501930,"action":"Open"}
>     {"time":1469501930,"action":"Open"}
>     {"time":1469501931,"action":"Open"}
>     {"time":1469501935,"action":"Open"}
>     {"time":1469501935,"action":"Open"}
>     {"time":1469501946,"action":"Open"}
>     {"time":1469501946,"action":"Open"}
>     {"time":1469501959,"action":"Open"}
>     {"time":1469501967,"action":"Open"}
>     {"time":1469501972,"action":"Close"}
>     {"time":1469501976,"action":"Open"}
>     {"time":1469501978,"action":"Open"}
>     {"time":1469501978,"action":"Open"}
>     {"time":1469501978,"action":"Open"}
>     {"time":1469501980,"action":"Open"}
>     {"time":1469501980,"action":"Open"}
>     {"time":1469501985,"action":"Open"}
>     {"time":1469501988,"action":"Open"}
>     {"time":1469501992,"action":"Open"}
>     {"time":1469501996,"action":"Open"}
>     {"time":1469502005,"action":"Open"}
>     {"time":1469502010,"action":"Open"}
>     {"time":1469502014,"action":"Close"}
>     {"time":1469502020,"action":"Open"}
>     {"time":1469502022,"action":"Open"}
>     {"time":1469502022,"action":"Open"}
>     {"time":1469502031,"action":"Open"}
>     {"time":1469502031,"action":"Open"}
>     {"time":1469502033,"action":"Open"}
>     {"time":1469502035,"action":"Open"}
>     {"time":1469502038,"action":"Open"}
>     {"time":1469502044,"action":"Open"}
>     {"time":1469502054,"action":"Open"}
>     {"time":1469502054,"action":"Open"}
>     {"time":1469502054,"action":"Open"}
>     {"time":1469502057,"action":"Open"}
>     {"time":1469502060,"action":"Open"}
>     {"time":1469502065,"action":"Open"}
>     {"time":1469502067,"action":"Open"}
>     {"time":1469502071,"action":"Open"}
>     {"time":1469502071,"action":"Open"}
>     {"time":1469502072,"action":"Close"}
>     {"time":1469502073,"action":"Open"}
>     {"time":1469502077,"action":"Open"}
>     {"time":1469502080,"action":"Open"}
>     {"time":1469502092,"action":"Open"}
>     {"time":1469502097,"action":"Open"}
>     {"time":1469502105,"action":"Open"}
>     {"time":1469502109,"action":"Open"}
>     {"time":1469502118,"action":"Open"}
>     {"time":1469502126,"action":"Open"}
>     {"time":1469502127,"action":"Open"}
>     {"time":1469502130,"action":"Open"}
>     {"time":1469502130,"action":"Open"}
>     {"time":1469502132,"action":"Open"}
>     {"time":1469502135,"action":"Open"}
>     {"time":1469502144,"action":"Open"}
>     {"time":1469502145,"action":"Open"}
>     {"time":1469502147,"action":"Open"}
>     {"time":1469502148,"action":"Close"}
>     {"time":1469502154,"action":"Open"}
>     {"time":1469502157,"action":"Open"}
>     {"time":1469502165,"action":"Open"}
>     {"time":1469502177,"action":"Open"}
>     {"time":1469502181,"action":"Open"}
>     {"time":1469502181,"action":"Open"}
>     {"time":1469502182,"action":"Open"}
>     {"time":1469502184,"action":"Open"}
>     {"time":1469502184,"action":"Open"}
>     {"time":1469502190,"action":"Open"}
>     {"time":1469502194,"action":"Open"}
>     {"time":1469502201,"action":"Open"}
>     {"time":1469502202,"action":"Open"}
>     {"time":1469502205,"action":"Open"}
>     {"time":1469502206,"action":"Open"}
>     {"time":1469502211,"action":"Open"}
>     {"time":1469502217,"action":"Open"}
>     {"time":1469502218,"action":"Open"}
>     {"time":1469502229,"action":"Open"}
>     {"time":1469502231,"action":"Open"}
>     {"time":1469502231,"action":"Open"}
>     {"time":1469502234,"action":"Open"}
>     {"time":1469502236,"action":"Open"}
>     {"time":1469502241,"action":"Open"}
>     {"time":1469502244,"action":"Open"}
>     {"time":1469502245,"action":"Open"}
>     {"time":1469502246,"action":"Open"}
>     {"time":1469502253,"action":"Open"}
>     {"time":1469502257,"action":"Open"}
>     {"time":1469502258,"action":"Open"}
>     {"time":1469502259,"action":"Open"}
>     {"time":1469502259,"action":"Open"}
>     {"time":1469502261,"action":"Close"}
>     {"time":1469502267,"action":"Open"}
>     {"time":1469502269,"action":"Open"}
>     {"time":1469502269,"action":"Open"}
>     {"time":1469502270,"action":"Open"}
>     {"time":1469502272,"action":"Open"}
>     {"time":1469502272,"action":"Open"}
>     {"time":1469502273,"action":"Open"}
>     {"time":1469502273,"action":"Open"}
>     {"time":1469502275,"action":"Open"}
>     {"time":1469502277,"action":"Open"}
>     {"time":1469502279,"action":"Open"}
>     {"time":1469502279,"action":"Open"}
>     {"time":1469502282,"action":"Close"}
>     {"time":1469502285,"action":"Open"}
>     {"time":1469502286,"action":"Open"}
>     {"time":1469502292,"action":"Open"}
>     {"time":1469502294,"action":"Open"}
>     {"time":1469502298,"action":"Open"}
>     {"time":1469502301,"action":"Open"}
>     {"time":1469502302,"action":"Open"}
>     {"time":1469502304,"action":"Open"}
>     {"time":1469502308,"action":"Open"}
>     {"time":1469502318,"action":"Open"}
>     {"time":1469502323,"action":"Open"}
>     {"time":1469502328,"action":"Open"}
>     {"time":1469502333,"action":"Open"}
>     {"time":1469502336,"action":"Close"}
>     {"time":1469502338,"action":"Close"}
>     {"time":1469502346,"action":"Open"}
>     {"time":1469502348,"action":"Open"}
>     {"time":1469502350,"action":"Open"}
>     {"time":1469502351,"action":"Close"}
>     {"time":1469502357,"action":"Close"}
>     {"time":1469502361,"action":"Open"}
>     {"time":1469502361,"action":"Open"}
>     {"time":1469502364,"action":"Open"}
>     {"time":1469502365,"action":"Open"}
>     {"time":1469502367,"action":"Open"}
>     {"time":1469502369,"action":"Open"}
>     {"time":1469502372,"action":"Open"}
>     {"time":1469502374,"action":"Open"}
>     {"time":1469502377,"action":"Open"}
>     {"time":1469502379,"action":"Close"}
>     {"time":1469502379,"action":"Open"}
>     {"time":1469502382,"action":"Open"}
>     {"time":1469502385,"action":"Open"}
>     {"time":1469502388,"action":"Open"}
>     {"time":1469502404,"action":"Open"}
>     {"time":1469502411,"action":"Open"}
>     {"time":1469502416,"action":"Open"}
>     {"time":1469502416,"action":"Open"}
>     {"time":1469502417,"action":"Close"}
>     {"time":1469502422,"action":"Open"}
>     {"time":1469502429,"action":"Open"}
>     {"time":1469502430,"action":"Open"}
>     {"time":1469502430,"action":"Open"}
>     {"time":1469502432,"action":"Open"}
>     {"time":1469502432,"action":"Open"}
>     {"time":1469502433,"action":"Open"}
>     {"time":1469502444,"action":"Open"}
>     {"time":1469502445,"action":"Open"}
>     {"time":1469502446,"action":"Open"}
>     {"time":1469502446,"action":"Open"}
>     {"time":1469502453,"action":"Open"}
>     {"time":1469502456,"action":"Close"}
>     {"time":1469502464,"action":"Open"}
>     {"time":1469502470,"action":"Open"}
>     {"time":1469502471,"action":"Open"}
>     {"time":1469502472,"action":"Open"}
>     {"time":1469502474,"action":"Open"}
>     {"time":1469502475,"action":"Open"}
>     {"time":1469502480,"action":"Open"}
>     {"time":1469502481,"action":"Open"}
>     {"time":1469502490,"action":"Open"}
>     {"time":1469502497,"action":"Close"}
>     {"time":1469502497,"action":"Open"}
>     {"time":1469502497,"action":"Close"}
>     {"time":1469502500,"action":"Close"}
>     {"time":1469502500,"action":"Open"}
>     {"time":1469502501,"action":"Open"}
>     {"time":1469502507,"action":"Close"}
>     {"time":1469502507,"action":"Open"}
>     {"time":1469502508,"action":"Open"}
>     {"time":1469502512,"action":"Open"}
>     {"time":1469502514,"action":"Open"}
>     {"time":1469502515,"action":"Open"}
>     {"time":1469502517,"action":"Close"}
>     {"time":1469502527,"action":"Open"}
>     {"time":1469502527,"action":"Open"}
>     {"time":1469502529,"action":"Open"}
>     {"time":1469502538,"action":"Open"}
>     {"time":1469502549,"action":"Open"}
>     {"time":1469502553,"action":"Open"}
>     {"time":1469502555,"action":"Open"}
>     {"time":1469502560,"action":"Open"}
>     {"time":1469502561,"action":"Open"}
>     {"time":1469502561,"action":"Open"}
>     {"time":1469502562,"action":"Open"}
>     {"time":1469502564,"action":"Close"}
>     {"time":1469502573,"action":"Open"}
>     {"time":1469502575,"action":"Open"}
>     {"time":1469502583,"action":"Open"}
>     {"time":1469502585,"action":"Open"}
>     {"time":1469502587,"action":"Open"}
>     {"time":1469502590,"action":"Open"}
>     {"time":1469502593,"action":"Open"}
>     {"time":1469502595,"action":"Close"}
>     {"time":1469502596,"action":"Open"}
>     {"time":1469502609,"action":"Open"}
>     {"time":1469502609,"action":"Open"}
>     {"time":1469502611,"action":"Open"}
>     {"time":1469502612,"action":"Open"}
>     {"time":1469502613,"action":"Open"}
>     {"time":1469502614,"action":"Open"}
>     {"time":1469502619,"action":"Open"}
>     {"time":1469502626,"action":"Close"}
>     {"time":1469502626,"action":"Open"}
>     {"time":1469502627,"action":"Open"}
>     {"time":1469502629,"action":"Open"}
>     {"time":1469502635,"action":"Open"}
>     {"time":1469502641,"action":"Open"}
>     {"time":1469502641,"action":"Open"}
>     {"time":1469502643,"action":"Close"}
>     {"time":1469502647,"action":"Open"}
>     {"time":1469502649,"action":"Open"}
>     {"time":1469502654,"action":"Open"}
>     {"time":1469502655,"action":"Open"}
>     {"time":1469502656,"action":"Open"}
>     {"time":1469502660,"action":"Close"}
>     {"time":1469502661,"action":"Close"}
>     {"time":1469502663,"action":"Open"}
>     {"time":1469502668,"action":"Open"}
>     {"time":1469502675,"action":"Open"}
>     {"time":1469502678,"action":"Open"}
>     {"time":1469502683,"action":"Open"}
>     {"time":1469502686,"action":"Open"}
>     {"time":1469502687,"action":"Open"}
>     {"time":1469502688,"action":"Open"}
>     {"time":1469502693,"action":"Open"}
>     {"time":1469502695,"action":"Open"}
>     {"time":1469502704,"action":"Open"}
>     {"time":1469502708,"action":"Close"}
>     {"time":1469502716,"action":"Open"}
>     {"time":1469502717,"action":"Open"}
>     {"time":1469502726,"action":"Open"}
>     {"time":1469502727,"action":"Open"}
>     {"time":1469502729,"action":"Open"}
>     {"time":1469502732,"action":"Open"}
>     {"time":1469502733,"action":"Open"}
>     {"time":1469502735,"action":"Open"}
>     {"time":1469502736,"action":"Open"}
>     {"time":1469502742,"action":"Open"}
>     {"time":1469502745,"action":"Open"}
>     {"time":1469502746,"action":"Open"}
>     {"time":1469502752,"action":"Open"}
>     {"time":1469502753,"action":"Open"}
>     {"time":1469502754,"action":"Open"}
>     {"time":1469502757,"action":"Open"}
>     {"time":1469502757,"action":"Open"}
>     {"time":1469502771,"action":"Open"}
>     {"time":1469502778,"action":"Open"}
>     {"time":1469502782,"action":"Open"}
>     {"time":1469502783,"action":"Close"}
>     {"time":1469502783,"action":"Open"}
>     {"time":1469502789,"action":"Open"}
>     {"time":1469502800,"action":"Open"}
>     {"time":1469502800,"action":"Open"}
>     {"time":1469502801,"action":"Open"}
>     {"time":1469502809,"action":"Close"}
>     {"time":1469502811,"action":"Open"}
>     {"time":1469502813,"action":"Close"}
>     {"time":1469502814,"action":"Open"}
>     {"time":1469502817,"action":"Open"}
>     {"time":1469502820,"action":"Open"}
>     {"time":1469502822,"action":"Close"}
>     {"time":1469502822,"action":"Open"}
>     {"time":1469502831,"action":"Close"}
>     {"time":1469502831,"action":"Open"}
>     {"time":1469502832,"action":"Close"}
>     {"time":1469502833,"action":"Open"}
>     {"time":1469502839,"action":"Open"}
>     {"time":1469502842,"action":"Close"}
>     {"time":1469502844,"action":"Open"}
>     {"time":1469502849,"action":"Open"}
>     {"time":1469502850,"action":"Open"}
>     {"time":1469502851,"action":"Open"}
>     {"time":1469502851,"action":"Open"}
>     {"time":1469502852,"action":"Open"}
>     {"time":1469502853,"action":"Open"}
>     {"time":1469502855,"action":"Open"}
>     {"time":1469502856,"action":"Open"}
>     {"time":1469502857,"action":"Open"}
>     {"time":1469502857,"action":"Open"}
>     {"time":1469502858,"action":"Open"}
>     {"time":1469502861,"action":"Open"}
>     {"time":1469502861,"action":"Open"}
>     {"time":1469502863,"action":"Close"}
>     {"time":1469502865,"action":"Open"}
>     {"time":1469502867,"action":"Open"}
>     {"time":1469502867,"action":"Open"}
>     {"time":1469502868,"action":"Open"}
>     {"time":1469502873,"action":"Open"}
>     {"time":1469502880,"action":"Close"}
>     {"time":1469502881,"action":"Close"}
>     {"time":1469502886,"action":"Open"}
>     {"time":1469502887,"action":"Open"}
>     {"time":1469502887,"action":"Open"}
>     {"time":1469502893,"action":"Close"}
>     {"time":1469502897,"action":"Open"}
>     {"time":1469502907,"action":"Open"}
>     {"time":1469502907,"action":"Open"}
>     {"time":1469502911,"action":"Close"}
>     {"time":1469502912,"action":"Open"}
>     {"time":1469502913,"action":"Open"}
>     {"time":1469502919,"action":"Open"}
>     {"time":1469502920,"action":"Open"}
>     {"time":1469502922,"action":"Open"}
>     {"time":1469502922,"action":"Open"}
>     {"time":1469502925,"action":"Open"}
>     {"time":1469502927,"action":"Open"}
>     {"time":1469502931,"action":"Open"}
>     {"time":1469502932,"action":"Open"}
>     {"time":1469502941,"action":"Open"}
>     {"time":1469502941,"action":"Open"}
>     {"time":1469502942,"action":"Open"}
>     {"time":1469502945,"action":"Close"}
>     {"time":1469502946,"action":"Close"}
>     {"time":1469502947,"action":"Open"}
>     {"time":1469502954,"action":"Close"}
>     {"time":1469502959,"action":"Open"}
>     {"time":1469502964,"action":"Close"}
>     {"time":1469502964,"action":"Open"}
>     {"time":1469502969,"action":"Close"}
>     {"time":1469502972,"action":"Close"}
>     {"time":1469502973,"action":"Close"}
>     {"time":1469502973,"action":"Open"}
>     {"time":1469502974,"action":"Open"}
>     {"time":1469502975,"action":"Close"}
>     {"time":1469502984,"action":"Open"}
>     {"time":1469502985,"action":"Open"}
>     {"time":1469502986,"action":"Close"}
>     {"time":1469502988,"action":"Open"}
>     {"time":1469502988,"action":"Open"}
>     {"time":1469502992,"action":"Open"}
>     {"time":1469502997,"action":"Open"}
>     {"time":1469503000,"action":"Open"}
>     {"time":1469503005,"action":"Open"}
>     {"time":1469503007,"action":"Open"}
>     {"time":1469503014,"action":"Open"}
>     {"time":1469503014,"action":"Open"}
>     {"time":1469503021,"action":"Open"}
>     {"time":1469503024,"action":"Open"}
>     {"time":1469503025,"action":"Open"}
>     {"time":1469503025,"action":"Open"}
>     {"time":1469503030,"action":"Open"}
>     {"time":1469503036,"action":"Open"}
>     {"time":1469503039,"action":"Open"}
>     {"time":1469503039,"action":"Open"}
>     {"time":1469503042,"action":"Open"}
>     {"time":1469503043,"action":"Open"}
>     {"time":1469503048,"action":"Open"}
>     {"time":1469503060,"action":"Open"}
>     {"time":1469503065,"action":"Close"}
>     {"time":1469503065,"action":"Open"}
>     {"time":1469503066,"action":"Open"}
>     {"time":1469503067,"action":"Open"}
>     {"time":1469503071,"action":"Open"}
>     {"time":1469503074,"action":"Open"}
>     {"time":1469503075,"action":"Open"}
>     {"time":1469503075,"action":"Open"}
>     {"time":1469503082,"action":"Close"}
>     {"time":1469503082,"action":"Open"}
>     {"time":1469503086,"action":"Open"}
>     {"time":1469503088,"action":"Close"}
>     {"time":1469503088,"action":"Open"}
>     {"time":1469503088,"action":"Open"}
>     {"time":1469503097,"action":"Open"}
>     {"time":1469503105,"action":"Open"}
>     {"time":1469503106,"action":"Close"}
>     {"time":1469503109,"action":"Open"}
>     {"time":1469503109,"action":"Open"}
>     {"time":1469503110,"action":"Close"}
>     {"time":1469503116,"action":"Close"}
>     {"time":1469503120,"action":"Open"}
>     {"time":1469503125,"action":"Open"}
>     {"time":1469503125,"action":"Open"}
>     {"time":1469503126,"action":"Close"}
>     {"time":1469503128,"action":"Open"}
>     {"time":1469503128,"action":"Open"}
>     {"time":1469503130,"action":"Open"}
>     {"time":1469503133,"action":"Open"}
>     {"time":1469503135,"action":"Open"}
>     {"time":1469503136,"action":"Close"}
>     {"time":1469503136,"action":"Open"}
>     {"time":1469503139,"action":"Open"}
>     {"time":1469503140,"action":"Close"}
>     {"time":1469503140,"action":"Close"}
>     {"time":1469503140,"action":"Open"}
>     {"time":1469503143,"action":"Open"}
>     {"time":1469503150,"action":"Open"}
>     {"time":1469503151,"action":"Close"}
>     {"time":1469503154,"action":"Close"}
>     {"time":1469503158,"action":"Open"}
>     {"time":1469503159,"action":"Open"}
>     {"time":1469503160,"action":"Close"}
>     {"time":1469503160,"action":"Close"}
>     {"time":1469503161,"action":"Open"}
>     {"time":1469503162,"action":"Open"}
>     {"time":1469503166,"action":"Open"}
>     {"time":1469503169,"action":"Open"}
>     {"time":1469503173,"action":"Open"}
>     {"time":1469503176,"action":"Open"}
>     {"time":1469503184,"action":"Open"}
>     {"time":1469503190,"action":"Close"}
>     {"time":1469503190,"action":"Open"}
>     {"time":1469503195,"action":"Close"}
>     {"time":1469503195,"action":"Open"}
>     {"time":1469503196,"action":"Open"}
>     {"time":1469503198,"action":"Open"}
>     {"time":1469503203,"action":"Open"}
>     {"time":1469503206,"action":"Open"}
>     {"time":1469503209,"action":"Open"}
>     {"time":1469503211,"action":"Open"}
>     {"time":1469503215,"action":"Open"}
>     {"time":1469503224,"action":"Close"}
>     {"time":1469503229,"action":"Open"}
>     {"time":1469503231,"action":"Close"}
>     {"time":1469503231,"action":"Open"}
>     {"time":1469503231,"action":"Open"}
>     {"time":1469503231,"action":"Open"}
>     {"time":1469503234,"action":"Open"}
>     {"time":1469503236,"action":"Open"}
>     {"time":1469503246,"action":"Close"}
>     {"time":1469503246,"action":"Open"}
>     {"time":1469503248,"action":"Open"}
>     {"time":1469503250,"action":"Close"}
>     {"time":1469503255,"action":"Open"}
>     {"time":1469503255,"action":"Open"}
>     {"time":1469503259,"action":"Open"}
>     {"time":1469503261,"action":"Open"}
>     {"time":1469503262,"action":"Open"}
>     {"time":1469503270,"action":"Open"}
>     {"time":1469503277,"action":"Open"}
>     {"time":1469503280,"action":"Close"}
>     {"time":1469503281,"action":"Open"}
>     {"time":1469503283,"action":"Open"}
>     {"time":1469503287,"action":"Open"}
>     {"time":1469503291,"action":"Close"}
>     {"time":1469503291,"action":"Open"}
>     {"time":1469503291,"action":"Open"}
>     {"time":1469503292,"action":"Open"}
>     {"time":1469503299,"action":"Open"}
>     {"time":1469503301,"action":"Open"}
>     {"time":1469503302,"action":"Close"}
>     {"time":1469503305,"action":"Open"}
>     {"time":1469503309,"action":"Open"}
>     {"time":1469503316,"action":"Open"}
>     {"time":1469503319,"action":"Open"}
>     {"time":1469503319,"action":"Open"}
>     {"time":1469503321,"action":"Open"}
>     {"time":1469503325,"action":"Close"}
>     {"time":1469503325,"action":"Open"}
>     {"time":1469503328,"action":"Open"}
>     {"time":1469503330,"action":"Open"}
>     {"time":1469503334,"action":"Open"}
>     {"time":1469503335,"action":"Close"}
>     {"time":1469503335,"action":"Open"}
>     {"time":1469503337,"action":"Open"}
>     {"time":1469503344,"action":"Close"}
>     {"time":1469503347,"action":"Open"}
>     {"time":1469503348,"action":"Open"}
>     {"time":1469503355,"action":"Open"}
>     {"time":1469503356,"action":"Close"}
>     {"time":1469503357,"action":"Close"}
>     {"time":1469503359,"action":"Open"}
>     {"time":1469503362,"action":"Close"}
>     {"time":1469503362,"action":"Open"}
>     {"time":1469503363,"action":"Close"}
>     {"time":1469503365,"action":"Open"}
>     {"time":1469503374,"action":"Open"}
>     {"time":1469503377,"action":"Open"}
>     {"time":1469503378,"action":"Open"}
>     {"time":1469503378,"action":"Open"}
>     {"time":1469503382,"action":"Open"}
>     {"time":1469503383,"action":"Open"}
>     {"time":1469503385,"action":"Close"}
>     {"time":1469503386,"action":"Open"}
>     {"time":1469503387,"action":"Open"}
>     {"time":1469503392,"action":"Open"}
>     {"time":1469503393,"action":"Open"}
>     {"time":1469503398,"action":"Open"}
>     {"time":1469503403,"action":"Close"}
>     {"time":1469503406,"action":"Close"}
>     {"time":1469503406,"action":"Open"}
>     {"time":1469503407,"action":"Open"}
>     {"time":1469503407,"action":"Open"}
>     {"time":1469503408,"action":"Open"}
>     {"time":1469503409,"action":"Open"}
>     {"time":1469503411,"action":"Open"}
>     {"time":1469503411,"action":"Open"}
>     {"time":1469503415,"action":"Open"}
>     {"time":1469503418,"action":"Close"}
>     {"time":1469503418,"action":"Open"}
>     {"time":1469503425,"action":"Close"}
>     {"time":1469503426,"action":"Close"}
>     {"time":1469503429,"action":"Open"}
>     {"time":1469503430,"action":"Open"}
>     {"time":1469503432,"action":"Open"}
>     {"time":1469503437,"action":"Close"}
>     {"time":1469503438,"action":"Open"}
>     {"time":1469503445,"action":"Open"}
>     {"time":1469503448,"action":"Open"}
>     {"time":1469503449,"action":"Close"}
>     {"time":1469503450,"action":"Open"}
>     {"time":1469503455,"action":"Open"}
>     {"time":1469503460,"action":"Open"}
>     {"time":1469503463,"action":"Open"}
>     {"time":1469503463,"action":"Open"}
>     {"time":1469503466,"action":"Open"}
>     {"time":1469503471,"action":"Close"}
>     {"time":1469503474,"action":"Open"}
>     {"time":1469503475,"action":"Open"}
>     {"time":1469503477,"action":"Open"}
>     {"time":1469503478,"action":"Open"}
>     {"time":1469503482,"action":"Open"}
>     {"time":1469503487,"action":"Close"}
>     {"time":1469503490,"action":"Open"}
>
>     *** WARNING: skipped 15627 bytes of output ***
>
>     {"time":1469504646,"action":"Open"}
>     {"time":1469504648,"action":"Open"}
>     {"time":1469504653,"action":"Open"}
>     {"time":1469504658,"action":"Open"}
>     {"time":1469504658,"action":"Open"}
>     {"time":1469504658,"action":"Open"}
>     {"time":1469504661,"action":"Close"}
>     {"time":1469504662,"action":"Open"}
>     {"time":1469504662,"action":"Open"}
>     {"time":1469504665,"action":"Open"}
>     {"time":1469504668,"action":"Close"}
>     {"time":1469504672,"action":"Open"}
>     {"time":1469504675,"action":"Open"}
>     {"time":1469504679,"action":"Open"}
>     {"time":1469504686,"action":"Open"}
>     {"time":1469504687,"action":"Open"}
>     {"time":1469504696,"action":"Close"}
>     {"time":1469504703,"action":"Open"}
>     {"time":1469504710,"action":"Open"}
>     {"time":1469504710,"action":"Open"}
>     {"time":1469504710,"action":"Open"}
>     {"time":1469504710,"action":"Open"}
>     {"time":1469504717,"action":"Open"}
>     {"time":1469504724,"action":"Close"}
>     {"time":1469504731,"action":"Open"}
>     {"time":1469504736,"action":"Open"}
>     {"time":1469504739,"action":"Open"}
>     {"time":1469504741,"action":"Close"}
>     {"time":1469504742,"action":"Close"}
>     {"time":1469504742,"action":"Close"}
>     {"time":1469504743,"action":"Close"}
>     {"time":1469504744,"action":"Open"}
>     {"time":1469504745,"action":"Open"}
>     {"time":1469504747,"action":"Close"}
>     {"time":1469504748,"action":"Open"}
>     {"time":1469504748,"action":"Open"}
>     {"time":1469504748,"action":"Close"}
>     {"time":1469504751,"action":"Open"}
>     {"time":1469504752,"action":"Open"}
>     {"time":1469504753,"action":"Close"}
>     {"time":1469504754,"action":"Open"}
>     {"time":1469504757,"action":"Open"}
>     {"time":1469504757,"action":"Open"}
>     {"time":1469504761,"action":"Close"}
>     {"time":1469504762,"action":"Open"}
>     {"time":1469504765,"action":"Close"}
>     {"time":1469504765,"action":"Open"}
>     {"time":1469504768,"action":"Close"}
>     {"time":1469504779,"action":"Open"}
>     {"time":1469504779,"action":"Open"}
>     {"time":1469504780,"action":"Close"}
>     {"time":1469504781,"action":"Open"}
>     {"time":1469504782,"action":"Close"}
>     {"time":1469504784,"action":"Close"}
>     {"time":1469504786,"action":"Close"}
>     {"time":1469504789,"action":"Open"}
>     {"time":1469504789,"action":"Open"}
>     {"time":1469504791,"action":"Open"}
>     {"time":1469504792,"action":"Open"}
>     {"time":1469504792,"action":"Open"}
>     {"time":1469504793,"action":"Open"}
>     {"time":1469504797,"action":"Close"}
>     {"time":1469504802,"action":"Open"}
>     {"time":1469504803,"action":"Close"}
>     {"time":1469504803,"action":"Open"}
>     {"time":1469504805,"action":"Open"}
>     {"time":1469504807,"action":"Close"}
>     {"time":1469504808,"action":"Close"}
>     {"time":1469504809,"action":"Open"}
>     {"time":1469504810,"action":"Open"}
>     {"time":1469504811,"action":"Open"}
>     {"time":1469504811,"action":"Open"}
>     {"time":1469504815,"action":"Open"}
>     {"time":1469504818,"action":"Open"}
>     {"time":1469504819,"action":"Open"}
>     {"time":1469504820,"action":"Close"}
>     {"time":1469504820,"action":"Open"}
>     {"time":1469504824,"action":"Close"}
>     {"time":1469504825,"action":"Open"}
>     {"time":1469504829,"action":"Open"}
>     {"time":1469504834,"action":"Close"}
>     {"time":1469504836,"action":"Open"}
>     {"time":1469504840,"action":"Open"}
>     {"time":1469504848,"action":"Open"}
>     {"time":1469504853,"action":"Close"}
>     {"time":1469504854,"action":"Close"}
>     {"time":1469504855,"action":"Open"}
>     {"time":1469504859,"action":"Open"}
>     {"time":1469504860,"action":"Close"}
>     {"time":1469504866,"action":"Close"}
>     {"time":1469504873,"action":"Close"}
>     {"time":1469504875,"action":"Open"}
>     {"time":1469504881,"action":"Open"}
>     {"time":1469504882,"action":"Close"}
>     {"time":1469504886,"action":"Open"}
>     {"time":1469504889,"action":"Open"}
>     {"time":1469504890,"action":"Close"}
>     {"time":1469504892,"action":"Open"}
>     {"time":1469504897,"action":"Close"}
>     {"time":1469504901,"action":"Close"}
>     {"time":1469504902,"action":"Open"}
>     {"time":1469504903,"action":"Close"}
>     {"time":1469504903,"action":"Open"}
>     {"time":1469504904,"action":"Open"}
>     {"time":1469504905,"action":"Open"}
>     {"time":1469504909,"action":"Close"}
>     {"time":1469504909,"action":"Open"}
>     {"time":1469504910,"action":"Open"}
>     {"time":1469504911,"action":"Close"}
>     {"time":1469504915,"action":"Open"}
>     {"time":1469504916,"action":"Open"}
>     {"time":1469504922,"action":"Close"}
>     {"time":1469504926,"action":"Close"}
>     {"time":1469504926,"action":"Open"}
>     {"time":1469504929,"action":"Open"}
>     {"time":1469504929,"action":"Open"}
>     {"time":1469504931,"action":"Open"}
>     {"time":1469504933,"action":"Close"}
>     {"time":1469504935,"action":"Open"}
>     {"time":1469504937,"action":"Close"}
>     {"time":1469504937,"action":"Open"}
>     {"time":1469504942,"action":"Open"}
>     {"time":1469504943,"action":"Open"}
>     {"time":1469504944,"action":"Open"}
>     {"time":1469504946,"action":"Close"}
>     {"time":1469504948,"action":"Open"}
>     {"time":1469504958,"action":"Open"}
>     {"time":1469504960,"action":"Close"}
>     {"time":1469504960,"action":"Open"}
>     {"time":1469504963,"action":"Open"}
>     {"time":1469504964,"action":"Close"}
>     {"time":1469504964,"action":"Open"}
>     {"time":1469504967,"action":"Open"}
>     {"time":1469504968,"action":"Close"}
>     {"time":1469504971,"action":"Close"}
>     {"time":1469504972,"action":"Close"}
>     {"time":1469504974,"action":"Close"}
>     {"time":1469504983,"action":"Close"}
>     {"time":1469504983,"action":"Close"}
>     {"time":1469504983,"action":"Open"}
>     {"time":1469504984,"action":"Open"}
>     {"time":1469504987,"action":"Close"}
>     {"time":1469504989,"action":"Open"}
>     {"time":1469504991,"action":"Open"}
>     {"time":1469504993,"action":"Open"}
>     {"time":1469504994,"action":"Open"}
>     {"time":1469504998,"action":"Open"}
>     {"time":1469505000,"action":"Open"}
>     {"time":1469505005,"action":"Open"}
>     {"time":1469505005,"action":"Open"}
>     {"time":1469505007,"action":"Open"}
>     {"time":1469505008,"action":"Open"}
>     {"time":1469505010,"action":"Open"}
>     {"time":1469505012,"action":"Open"}
>     {"time":1469505013,"action":"Close"}
>     {"time":1469505013,"action":"Open"}
>     {"time":1469505013,"action":"Open"}
>     {"time":1469505017,"action":"Open"}
>     {"time":1469505020,"action":"Close"}
>     {"time":1469505020,"action":"Open"}
>     {"time":1469505021,"action":"Close"}
>     {"time":1469505022,"action":"Close"}
>     {"time":1469505022,"action":"Close"}
>     {"time":1469505023,"action":"Close"}
>     {"time":1469505029,"action":"Open"}
>     {"time":1469505032,"action":"Open"}
>     {"time":1469505033,"action":"Open"}
>     {"time":1469505035,"action":"Close"}
>     {"time":1469505039,"action":"Close"}
>     {"time":1469505040,"action":"Close"}
>     {"time":1469505040,"action":"Open"}
>     {"time":1469505041,"action":"Open"}
>     {"time":1469505046,"action":"Close"}
>     {"time":1469505046,"action":"Open"}
>     {"time":1469505047,"action":"Open"}
>     {"time":1469505049,"action":"Close"}
>     {"time":1469505050,"action":"Open"}
>     {"time":1469505052,"action":"Open"}
>     {"time":1469505057,"action":"Close"}
>     {"time":1469505059,"action":"Open"}
>     {"time":1469505062,"action":"Close"}
>     {"time":1469505064,"action":"Close"}
>     {"time":1469505069,"action":"Open"}
>     {"time":1469505072,"action":"Close"}
>     {"time":1469505072,"action":"Close"}
>     {"time":1469505072,"action":"Close"}
>     {"time":1469505074,"action":"Open"}
>     {"time":1469505075,"action":"Open"}
>     {"time":1469505076,"action":"Open"}
>     {"time":1469505077,"action":"Open"}
>     {"time":1469505081,"action":"Open"}
>     {"time":1469505082,"action":"Open"}
>     {"time":1469505085,"action":"Open"}
>     {"time":1469505086,"action":"Open"}
>     {"time":1469505086,"action":"Open"}
>     {"time":1469505086,"action":"Open"}
>     {"time":1469505098,"action":"Open"}
>     {"time":1469505101,"action":"Open"}
>     {"time":1469505102,"action":"Open"}
>     {"time":1469505102,"action":"Open"}
>     {"time":1469505106,"action":"Close"}
>     {"time":1469505106,"action":"Close"}
>     {"time":1469505111,"action":"Open"}
>     {"time":1469505118,"action":"Open"}
>     {"time":1469505120,"action":"Close"}
>     {"time":1469505126,"action":"Open"}
>     {"time":1469505128,"action":"Close"}
>     {"time":1469505129,"action":"Close"}
>     {"time":1469505129,"action":"Open"}
>     {"time":1469505130,"action":"Open"}
>     {"time":1469505130,"action":"Open"}
>     {"time":1469505133,"action":"Open"}
>     {"time":1469505139,"action":"Close"}
>     {"time":1469505140,"action":"Open"}
>     {"time":1469505155,"action":"Open"}
>     {"time":1469505162,"action":"Open"}
>     {"time":1469505163,"action":"Close"}
>     {"time":1469505164,"action":"Open"}
>     {"time":1469505166,"action":"Open"}
>     {"time":1469505169,"action":"Open"}
>     {"time":1469505170,"action":"Open"}
>     {"time":1469505170,"action":"Open"}
>     {"time":1469505172,"action":"Open"}
>     {"time":1469505175,"action":"Open"}
>     {"time":1469505176,"action":"Open"}
>     {"time":1469505180,"action":"Close"}
>     {"time":1469505180,"action":"Close"}
>     {"time":1469505180,"action":"Open"}
>     {"time":1469505183,"action":"Close"}
>     {"time":1469505184,"action":"Open"}
>     {"time":1469505184,"action":"Open"}
>     {"time":1469505185,"action":"Close"}
>     {"time":1469505185,"action":"Close"}
>     {"time":1469505188,"action":"Close"}
>     {"time":1469505191,"action":"Open"}
>     {"time":1469505192,"action":"Open"}
>     {"time":1469505194,"action":"Close"}
>     {"time":1469505200,"action":"Open"}
>     {"time":1469505201,"action":"Close"}
>     {"time":1469505203,"action":"Close"}
>     {"time":1469505204,"action":"Close"}
>     {"time":1469505204,"action":"Open"}
>     {"time":1469505207,"action":"Close"}
>     {"time":1469505209,"action":"Open"}
>     {"time":1469505211,"action":"Open"}
>     {"time":1469505219,"action":"Open"}
>     {"time":1469505222,"action":"Close"}
>     {"time":1469505226,"action":"Close"}
>     {"time":1469505229,"action":"Close"}
>     {"time":1469505235,"action":"Open"}
>     {"time":1469505237,"action":"Close"}
>     {"time":1469505238,"action":"Open"}
>     {"time":1469505239,"action":"Open"}
>     {"time":1469505241,"action":"Open"}
>     {"time":1469505246,"action":"Open"}
>     {"time":1469505250,"action":"Open"}
>     {"time":1469505250,"action":"Open"}
>     {"time":1469505255,"action":"Open"}
>     {"time":1469505255,"action":"Open"}
>     {"time":1469505256,"action":"Open"}
>     {"time":1469505259,"action":"Close"}
>     {"time":1469505261,"action":"Open"}
>     {"time":1469505261,"action":"Open"}
>     {"time":1469505262,"action":"Close"}
>     {"time":1469505263,"action":"Close"}
>     {"time":1469505264,"action":"Open"}
>     {"time":1469505265,"action":"Open"}
>     {"time":1469505266,"action":"Open"}
>     {"time":1469505266,"action":"Open"}
>     {"time":1469505269,"action":"Open"}
>     {"time":1469505269,"action":"Open"}
>     {"time":1469505272,"action":"Open"}
>     {"time":1469505273,"action":"Close"}
>     {"time":1469505278,"action":"Close"}
>     {"time":1469505278,"action":"Open"}
>     {"time":1469505281,"action":"Open"}
>     {"time":1469505283,"action":"Close"}
>     {"time":1469505283,"action":"Close"}
>     {"time":1469505286,"action":"Open"}
>     {"time":1469505289,"action":"Open"}
>     {"time":1469505291,"action":"Close"}
>     {"time":1469505294,"action":"Close"}
>     {"time":1469505295,"action":"Close"}
>     {"time":1469505296,"action":"Close"}
>     {"time":1469505300,"action":"Open"}
>     {"time":1469505300,"action":"Open"}
>     {"time":1469505301,"action":"Open"}
>     {"time":1469505301,"action":"Open"}
>     {"time":1469505303,"action":"Open"}
>     {"time":1469505307,"action":"Close"}
>     {"time":1469505307,"action":"Open"}
>     {"time":1469505312,"action":"Close"}
>     {"time":1469505320,"action":"Close"}
>     {"time":1469505321,"action":"Open"}
>     {"time":1469505328,"action":"Close"}
>     {"time":1469505330,"action":"Open"}
>     {"time":1469505332,"action":"Close"}
>     {"time":1469505333,"action":"Open"}
>     {"time":1469505335,"action":"Open"}
>     {"time":1469505336,"action":"Close"}
>     {"time":1469505336,"action":"Open"}
>     {"time":1469505343,"action":"Close"}
>     {"time":1469505344,"action":"Open"}
>     {"time":1469505346,"action":"Open"}
>     {"time":1469505349,"action":"Open"}
>     {"time":1469505349,"action":"Open"}
>     {"time":1469505351,"action":"Close"}
>     {"time":1469505353,"action":"Close"}
>     {"time":1469505353,"action":"Open"}
>     {"time":1469505361,"action":"Open"}
>     {"time":1469505363,"action":"Open"}
>     {"time":1469505363,"action":"Open"}
>     {"time":1469505370,"action":"Open"}
>     {"time":1469505371,"action":"Close"}
>     {"time":1469505372,"action":"Open"}
>     {"time":1469505372,"action":"Close"}
>     {"time":1469505375,"action":"Close"}
>     {"time":1469505377,"action":"Close"}
>     {"time":1469505378,"action":"Open"}
>     {"time":1469505380,"action":"Close"}
>     {"time":1469505384,"action":"Open"}
>     {"time":1469505387,"action":"Close"}
>     {"time":1469505389,"action":"Close"}
>     {"time":1469505393,"action":"Close"}
>     {"time":1469505393,"action":"Close"}
>     {"time":1469505397,"action":"Open"}
>     {"time":1469505406,"action":"Open"}
>     {"time":1469505413,"action":"Close"}
>     {"time":1469505414,"action":"Close"}
>     {"time":1469505414,"action":"Open"}
>     {"time":1469505414,"action":"Open"}
>     {"time":1469505415,"action":"Open"}
>     {"time":1469505416,"action":"Open"}
>     {"time":1469505418,"action":"Open"}
>     {"time":1469505421,"action":"Open"}
>     {"time":1469505424,"action":"Open"}
>     {"time":1469505428,"action":"Open"}
>     {"time":1469505430,"action":"Open"}
>     {"time":1469505443,"action":"Open"}
>     {"time":1469505451,"action":"Close"}
>     {"time":1469505460,"action":"Open"}
>     {"time":1469505460,"action":"Open"}
>     {"time":1469505462,"action":"Close"}
>     {"time":1469505463,"action":"Close"}
>     {"time":1469505464,"action":"Open"}
>     {"time":1469505465,"action":"Close"}
>     {"time":1469505465,"action":"Close"}
>     {"time":1469505473,"action":"Open"}
>     {"time":1469505474,"action":"Open"}
>     {"time":1469505478,"action":"Open"}
>     {"time":1469505480,"action":"Close"}
>     {"time":1469505482,"action":"Open"}
>     {"time":1469505484,"action":"Close"}
>     {"time":1469505487,"action":"Open"}
>     {"time":1469505488,"action":"Open"}
>     {"time":1469505490,"action":"Open"}
>     {"time":1469505498,"action":"Open"}
>     {"time":1469505499,"action":"Open"}
>     {"time":1469505504,"action":"Open"}
>     {"time":1469505505,"action":"Open"}
>     {"time":1469505509,"action":"Open"}
>     {"time":1469505514,"action":"Close"}
>     {"time":1469505515,"action":"Open"}
>     {"time":1469505517,"action":"Open"}
>     {"time":1469505523,"action":"Close"}
>     {"time":1469505524,"action":"Open"}
>     {"time":1469505524,"action":"Open"}
>     {"time":1469505525,"action":"Open"}
>     {"time":1469505526,"action":"Close"}
>     {"time":1469505526,"action":"Open"}
>     {"time":1469505527,"action":"Open"}
>     {"time":1469505528,"action":"Open"}
>     {"time":1469505531,"action":"Close"}
>     {"time":1469505533,"action":"Open"}
>     {"time":1469505534,"action":"Close"}
>     {"time":1469505534,"action":"Open"}
>     {"time":1469505535,"action":"Open"}
>     {"time":1469505538,"action":"Open"}
>     {"time":1469505538,"action":"Open"}
>     {"time":1469505539,"action":"Close"}
>     {"time":1469505539,"action":"Open"}
>     {"time":1469505540,"action":"Close"}
>     {"time":1469505542,"action":"Open"}
>     {"time":1469505543,"action":"Open"}
>     {"time":1469505544,"action":"Close"}
>     {"time":1469505545,"action":"Open"}
>     {"time":1469505550,"action":"Close"}
>     {"time":1469505550,"action":"Open"}
>     {"time":1469505551,"action":"Close"}
>     {"time":1469505553,"action":"Open"}
>     {"time":1469505555,"action":"Open"}
>     {"time":1469505556,"action":"Open"}
>     {"time":1469505557,"action":"Open"}
>     {"time":1469505558,"action":"Close"}
>     {"time":1469505558,"action":"Open"}
>     {"time":1469505561,"action":"Close"}
>     {"time":1469505563,"action":"Close"}
>     {"time":1469505563,"action":"Open"}
>     {"time":1469505564,"action":"Close"}
>     {"time":1469505566,"action":"Close"}
>     {"time":1469505567,"action":"Open"}
>     {"time":1469505573,"action":"Open"}
>     {"time":1469505574,"action":"Open"}
>     {"time":1469505579,"action":"Close"}
>     {"time":1469505582,"action":"Open"}
>     {"time":1469505586,"action":"Open"}
>     {"time":1469505588,"action":"Open"}
>     {"time":1469505589,"action":"Open"}
>     {"time":1469505590,"action":"Close"}
>     {"time":1469505591,"action":"Close"}
>     {"time":1469505591,"action":"Open"}
>     {"time":1469505597,"action":"Close"}
>     {"time":1469505597,"action":"Close"}
>     {"time":1469505599,"action":"Open"}
>     {"time":1469505601,"action":"Open"}
>     {"time":1469505602,"action":"Close"}
>     {"time":1469505612,"action":"Close"}
>     {"time":1469505616,"action":"Close"}
>     {"time":1469505616,"action":"Open"}
>     {"time":1469505617,"action":"Open"}
>     {"time":1469505619,"action":"Close"}
>     {"time":1469505621,"action":"Open"}
>     {"time":1469505624,"action":"Open"}
>     {"time":1469505625,"action":"Open"}
>     {"time":1469505626,"action":"Close"}
>     {"time":1469505628,"action":"Close"}
>     {"time":1469505629,"action":"Open"}
>     {"time":1469505638,"action":"Close"}
>     {"time":1469505640,"action":"Open"}
>     {"time":1469505640,"action":"Open"}
>     {"time":1469505650,"action":"Open"}
>     {"time":1469505653,"action":"Open"}
>     {"time":1469505661,"action":"Close"}
>     {"time":1469505661,"action":"Open"}
>     {"time":1469505663,"action":"Open"}
>     {"time":1469505665,"action":"Open"}
>     {"time":1469505668,"action":"Open"}
>     {"time":1469505682,"action":"Open"}
>     {"time":1469505686,"action":"Open"}
>     {"time":1469505694,"action":"Close"}
>     {"time":1469505695,"action":"Open"}
>     {"time":1469505696,"action":"Open"}
>     {"time":1469505700,"action":"Open"}
>     {"time":1469505708,"action":"Open"}
>     {"time":1469505711,"action":"Close"}
>     {"time":1469505713,"action":"Close"}
>     {"time":1469505715,"action":"Close"}
>     {"time":1469505715,"action":"Open"}
>     {"time":1469505718,"action":"Close"}
>     {"time":1469505719,"action":"Open"}
>     {"time":1469505723,"action":"Open"}
>     {"time":1469505725,"action":"Open"}
>     {"time":1469505728,"action":"Close"}
>     {"time":1469505731,"action":"Open"}
>     {"time":1469505733,"action":"Close"}
>     {"time":1469505733,"action":"Open"}
>     {"time":1469505735,"action":"Open"}
>     {"time":1469505735,"action":"Open"}
>     {"time":1469505736,"action":"Close"}
>     {"time":1469505739,"action":"Close"}
>     {"time":1469505739,"action":"Close"}
>     {"time":1469505741,"action":"Open"}
>     {"time":1469505741,"action":"Open"}
>     {"time":1469505748,"action":"Close"}
>     {"time":1469505748,"action":"Open"}
>     {"time":1469505748,"action":"Open"}
>     {"time":1469505749,"action":"Close"}
>     {"time":1469505753,"action":"Open"}
>     {"time":1469505754,"action":"Open"}
>     {"time":1469505758,"action":"Open"}
>     {"time":1469505758,"action":"Open"}
>     {"time":1469505759,"action":"Close"}
>     {"time":1469505759,"action":"Open"}
>     {"time":1469505769,"action":"Open"}
>     {"time":1469505770,"action":"Close"}
>     {"time":1469505770,"action":"Open"}
>     {"time":1469505775,"action":"Open"}
>     {"time":1469505783,"action":"Open"}
>     {"time":1469505787,"action":"Close"}
>     {"time":1469505788,"action":"Open"}
>     {"time":1469505793,"action":"Open"}
>     {"time":1469505794,"action":"Open"}
>     {"time":1469505797,"action":"Close"}
>     {"time":1469505800,"action":"Close"}
>     {"time":1469505801,"action":"Close"}
>     {"time":1469505802,"action":"Close"}
>     {"time":1469505803,"action":"Open"}
>     {"time":1469505811,"action":"Close"}
>     {"time":1469505812,"action":"Open"}
>     {"time":1469505815,"action":"Close"}
>     {"time":1469505820,"action":"Close"}
>     {"time":1469505820,"action":"Open"}
>     {"time":1469505824,"action":"Close"}
>     {"time":1469505830,"action":"Open"}
>     {"time":1469505832,"action":"Close"}
>     {"time":1469505834,"action":"Open"}
>     {"time":1469505835,"action":"Close"}
>     {"time":1469505835,"action":"Open"}
>     {"time":1469505836,"action":"Open"}
>     {"time":1469505838,"action":"Open"}
>     {"time":1469505839,"action":"Close"}
>     {"time":1469505841,"action":"Open"}
>     {"time":1469505842,"action":"Close"}
>     {"time":1469505844,"action":"Close"}
>     {"time":1469505851,"action":"Open"}
>     {"time":1469505851,"action":"Open"}
>     {"time":1469505854,"action":"Open"}
>     {"time":1469505860,"action":"Open"}
>     {"time":1469505863,"action":"Open"}
>     {"time":1469505867,"action":"Open"}
>     {"time":1469505873,"action":"Open"}
>     {"time":1469505875,"action":"Close"}
>     {"time":1469505875,"action":"Open"}
>     {"time":1469505875,"action":"Open"}
>     {"time":1469505877,"action":"Close"}
>     {"time":1469505882,"action":"Close"}
>     {"time":1469505886,"action":"Open"}
>     {"time":1469505890,"action":"Close"}
>     {"time":1469505892,"action":"Open"}
>     {"time":1469505897,"action":"Open"}
>     {"time":1469505902,"action":"Close"}
>     {"time":1469505903,"action":"Open"}
>     {"time":1469505904,"action":"Open"}
>     {"time":1469505904,"action":"Open"}
>     {"time":1469505905,"action":"Close"}
>     {"time":1469505905,"action":"Open"}
>     {"time":1469505905,"action":"Open"}
>     {"time":1469505907,"action":"Close"}
>     {"time":1469505907,"action":"Open"}
>     {"time":1469505910,"action":"Open"}
>     {"time":1469505913,"action":"Open"}
>     {"time":1469505918,"action":"Close"}
>     {"time":1469505919,"action":"Open"}
>     {"time":1469505920,"action":"Open"}
>     {"time":1469505922,"action":"Open"}
>     {"time":1469505923,"action":"Close"}
>     {"time":1469505924,"action":"Open"}
>     {"time":1469505927,"action":"Open"}
>     {"time":1469505927,"action":"Open"}
>     {"time":1469505929,"action":"Open"}
>     {"time":1469505933,"action":"Open"}
>     {"time":1469505935,"action":"Open"}
>     {"time":1469505936,"action":"Close"}
>     {"time":1469505937,"action":"Close"}
>     {"time":1469505937,"action":"Open"}
>     {"time":1469505938,"action":"Open"}
>     {"time":1469505939,"action":"Close"}
>     {"time":1469505941,"action":"Open"}
>     {"time":1469505942,"action":"Close"}
>     {"time":1469505944,"action":"Open"}
>     {"time":1469505947,"action":"Close"}
>     {"time":1469505954,"action":"Close"}
>     {"time":1469505954,"action":"Open"}
>     {"time":1469505955,"action":"Close"}
>     {"time":1469505958,"action":"Open"}
>     {"time":1469505959,"action":"Close"}
>     {"time":1469505961,"action":"Close"}
>     {"time":1469505966,"action":"Open"}
>     {"time":1469505966,"action":"Open"}
>     {"time":1469505967,"action":"Close"}
>     {"time":1469505969,"action":"Open"}
>     {"time":1469505970,"action":"Close"}
>     {"time":1469505970,"action":"Open"}
>     {"time":1469505972,"action":"Close"}
>     {"time":1469505972,"action":"Close"}
>     {"time":1469505975,"action":"Close"}
>     {"time":1469505977,"action":"Close"}
>     {"time":1469505977,"action":"Open"}
>     {"time":1469505979,"action":"Open"}
>     {"time":1469505980,"action":"Open"}
>     {"time":1469505986,"action":"Open"}
>     {"time":1469505987,"action":"Open"}
>     {"time":1469505987,"action":"Open"}
>     {"time":1469505990,"action":"Open"}
>     {"time":1469505990,"action":"Open"}
>     {"time":1469505990,"action":"Open"}
>     {"time":1469505991,"action":"Open"}
>     {"time":1469505992,"action":"Open"}
>     {"time":1469505998,"action":"Open"}
>     {"time":1469506000,"action":"Open"}
>     {"time":1469506002,"action":"Close"}
>     {"time":1469506004,"action":"Open"}
>     {"time":1469506005,"action":"Close"}
>     {"time":1469506005,"action":"Close"}
>     {"time":1469506005,"action":"Open"}
>     {"time":1469506006,"action":"Close"}
>     {"time":1469506006,"action":"Close"}
>     {"time":1469506006,"action":"Open"}
>     {"time":1469506010,"action":"Open"}
>     {"time":1469506012,"action":"Open"}
>     {"time":1469506022,"action":"Close"}
>     {"time":1469506022,"action":"Open"}
>     {"time":1469506025,"action":"Open"}
>     {"time":1469506028,"action":"Open"}
>     {"time":1469506030,"action":"Open"}
>     {"time":1469506030,"action":"Open"}
>     {"time":1469506032,"action":"Open"}
>     {"time":1469506032,"action":"Open"}
>     {"time":1469506033,"action":"Close"}
>     {"time":1469506033,"action":"Open"}
>     {"time":1469506035,"action":"Close"}
>     {"time":1469506036,"action":"Close"}
>     {"time":1469506038,"action":"Open"}
>     {"time":1469506041,"action":"Open"}
>     {"time":1469506044,"action":"Close"}
>     {"time":1469506046,"action":"Open"}
>     {"time":1469506046,"action":"Open"}
>     {"time":1469506047,"action":"Close"}
>     {"time":1469506047,"action":"Open"}
>     {"time":1469506049,"action":"Open"}
>     {"time":1469506050,"action":"Close"}
>     {"time":1469506051,"action":"Close"}
>     {"time":1469506053,"action":"Open"}
>     {"time":1469506055,"action":"Close"}
>     {"time":1469506056,"action":"Open"}
>     {"time":1469506056,"action":"Open"}
>     {"time":1469506058,"action":"Open"}
>     {"time":1469506060,"action":"Open"}
>     {"time":1469506063,"action":"Open"}
>     {"time":1469506070,"action":"Close"}
>     {"time":1469506070,"action":"Open"}
>     {"time":1469506072,"action":"Open"}
>     {"time":1469506074,"action":"Open"}
>     {"time":1469506081,"action":"Close"}
>     {"time":1469506081,"action":"Open"}
>     {"time":1469506081,"action":"Open"}
>     {"time":1469506083,"action":"Open"}
>     {"time":1469506083,"action":"Open"}
>     {"time":1469506085,"action":"Close"}
>     {"time":1469506085,"action":"Open"}
>     {"time":1469506091,"action":"Close"}
>     {"time":1469506095,"action":"Open"}
>     {"time":1469506096,"action":"Close"}
>     {"time":1469506097,"action":"Close"}
>     {"time":1469506099,"action":"Close"}
>     {"time":1469506107,"action":"Close"}
>     {"time":1469506109,"action":"Close"}
>     {"time":1469506110,"action":"Close"}
>     {"time":1469506110,"action":"Open"}
>     {"time":1469506111,"action":"Open"}
>     {"time":1469506113,"action":"Open"}
>     {"time":1469506114,"action":"Open"}
>     {"time":1469506114,"action":"Open"}
>     {"time":1469506115,"action":"Open"}
>     {"time":1469506116,"action":"Close"}
>     {"time":1469506124,"action":"Open"}
>     {"time":1469506125,"action":"Close"}
>     {"time":1469506129,"action":"Open"}
>     {"time":1469506130,"action":"Open"}
>     {"time":1469506133,"action":"Close"}
>     {"time":1469506135,"action":"Open"}
>     {"time":1469506135,"action":"Open"}
>     {"time":1469506137,"action":"Close"}
>     {"time":1469506140,"action":"Open"}
>     {"time":1469506144,"action":"Open"}
>     {"time":1469506148,"action":"Open"}
>     {"time":1469506150,"action":"Open"}
>     {"time":1469506153,"action":"Open"}
>     {"time":1469506154,"action":"Open"}
>     {"time":1469506155,"action":"Close"}
>     {"time":1469506159,"action":"Open"}
>     {"time":1469506160,"action":"Open"}
>     {"time":1469506161,"action":"Open"}
>     {"time":1469506165,"action":"Open"}
>     {"time":1469506166,"action":"Close"}
>     {"time":1469506167,"action":"Open"}
>     {"time":1469506173,"action":"Open"}
>     {"time":1469506174,"action":"Open"}
>     {"time":1469506176,"action":"Close"}
>     {"time":1469506178,"action":"Close"}
>     {"time":1469506180,"action":"Close"}
>     {"time":1469506186,"action":"Close"}
>     {"time":1469506186,"action":"Close"}
>     {"time":1469506187,"action":"Open"}
>     {"time":1469506189,"action":"Close"}
>     {"time":1469506207,"action":"Close"}
>     {"time":1469506207,"action":"Open"}
>     {"time":1469506216,"action":"Open"}
>     {"time":1469506218,"action":"Open"}
>     {"time":1469506220,"action":"Close"}
>     {"time":1469506220,"action":"Open"}
>     {"time":1469506221,"action":"Open"}
>     {"time":1469506225,"action":"Open"}
>     {"time":1469506227,"action":"Close"}
>     {"time":1469506228,"action":"Open"}
>     {"time":1469506233,"action":"Open"}
>     {"time":1469506234,"action":"O

Each line in the files contain a JSON record with two fields - `time` and `action`. Let's try to analyze these files interactively.

Batch/Interactive Processing
----------------------------

The usual first step in attempting to process the data is to interactively query the data. Let's define a static DataFrame on the files, and give it a table name.

``` scala
import org.apache.spark.sql.types._

val inputPath = "/databricks-datasets/structured-streaming/events/"

// Since we know the data format already, let's define the schema to speed up processing (no need for Spark to infer schema)
val jsonSchema = new StructType().add("time", TimestampType).add("action", StringType)

val staticInputDF = 
  spark
    .read
    .schema(jsonSchema)
    .json(inputPath)

display(staticInputDF)
```

| time                         | action |
|------------------------------|--------|
| 2016-07-28T04:19:28.000+0000 | Close  |
| 2016-07-28T04:19:28.000+0000 | Close  |
| 2016-07-28T04:19:29.000+0000 | Open   |
| 2016-07-28T04:19:31.000+0000 | Close  |
| 2016-07-28T04:19:31.000+0000 | Open   |
| 2016-07-28T04:19:31.000+0000 | Open   |
| 2016-07-28T04:19:32.000+0000 | Close  |
| 2016-07-28T04:19:33.000+0000 | Close  |
| 2016-07-28T04:19:35.000+0000 | Close  |
| 2016-07-28T04:19:36.000+0000 | Open   |
| 2016-07-28T04:19:38.000+0000 | Close  |
| 2016-07-28T04:19:40.000+0000 | Open   |
| 2016-07-28T04:19:41.000+0000 | Close  |
| 2016-07-28T04:19:42.000+0000 | Open   |
| 2016-07-28T04:19:45.000+0000 | Open   |
| 2016-07-28T04:19:47.000+0000 | Open   |
| 2016-07-28T04:19:48.000+0000 | Open   |
| 2016-07-28T04:19:49.000+0000 | Open   |
| 2016-07-28T04:19:55.000+0000 | Open   |
| 2016-07-28T04:20:00.000+0000 | Close  |
| 2016-07-28T04:20:00.000+0000 | Open   |
| 2016-07-28T04:20:01.000+0000 | Open   |
| 2016-07-28T04:20:03.000+0000 | Close  |
| 2016-07-28T04:20:07.000+0000 | Open   |
| 2016-07-28T04:20:11.000+0000 | Open   |
| 2016-07-28T04:20:12.000+0000 | Close  |
| 2016-07-28T04:20:12.000+0000 | Open   |
| 2016-07-28T04:20:13.000+0000 | Close  |
| 2016-07-28T04:20:16.000+0000 | Open   |
| 2016-07-28T04:20:23.000+0000 | Close  |

Truncated to 30 rows

Now we can compute the number of "open" and "close" actions with one hour windows. To do this, we will group by the `action` column and 1 hour windows over the `time` column.

``` scala
import org.apache.spark.sql.functions._

val staticCountsDF = 
  staticInputDF
    .groupBy($"action", window($"time", "1 hour"))
    .count()   

// Register the DataFrame as table 'static_counts'
staticCountsDF.createOrReplaceTempView("static_counts")
```

>     import org.apache.spark.sql.functions._
>     staticCountsDF: org.apache.spark.sql.DataFrame = [action: string, window: struct<start: timestamp, end: timestamp> ... 1 more field]

Now we can directly use SQL to query the table. For example, here are the total counts across all the hours.

``` sql
select action, sum(count) as total_count from static_counts group by action
```

| action | total\_count |
|--------|--------------|
| Close  | 50000.0      |
| Open   | 50000.0      |

How about a timeline of windowed counts?

``` sql
select action, date_format(window.end, "MMM-dd HH:mm") as time, count from static_counts order by time, action
```

| action | time         | count  |
|--------|--------------|--------|
| Close  | Jul-26 03:00 | 11.0   |
| Open   | Jul-26 03:00 | 179.0  |
| Close  | Jul-26 04:00 | 344.0  |
| Open   | Jul-26 04:00 | 1001.0 |
| Close  | Jul-26 05:00 | 815.0  |
| Open   | Jul-26 05:00 | 999.0  |
| Close  | Jul-26 06:00 | 1003.0 |
| Open   | Jul-26 06:00 | 1000.0 |
| Close  | Jul-26 07:00 | 1011.0 |
| Open   | Jul-26 07:00 | 993.0  |
| Close  | Jul-26 08:00 | 989.0  |
| Open   | Jul-26 08:00 | 1008.0 |
| Close  | Jul-26 09:00 | 985.0  |
| Open   | Jul-26 09:00 | 996.0  |
| Close  | Jul-26 10:00 | 983.0  |
| Open   | Jul-26 10:00 | 1000.0 |
| Close  | Jul-26 11:00 | 1022.0 |
| Open   | Jul-26 11:00 | 1007.0 |
| Close  | Jul-26 12:00 | 1028.0 |
| Open   | Jul-26 12:00 | 991.0  |
| Close  | Jul-26 13:00 | 960.0  |
| Open   | Jul-26 13:00 | 996.0  |
| Close  | Jul-26 14:00 | 1028.0 |
| Open   | Jul-26 14:00 | 1006.0 |
| Close  | Jul-26 15:00 | 994.0  |
| Open   | Jul-26 15:00 | 991.0  |
| Close  | Jul-26 16:00 | 988.0  |
| Open   | Jul-26 16:00 | 1020.0 |
| Close  | Jul-26 17:00 | 984.0  |
| Open   | Jul-26 17:00 | 992.0  |

Truncated to 30 rows

Note the two ends of the graph. The close actions are generated such that they are after the corresponding open actions, so there are more "opens" in the beginning and more "closes" in the end.

Stream Processing
-----------------

Now that we have analyzed the data interactively, let's convert this to a streaming query that continuously updates as data comes. Since we just have a static set of files, we are going to emulate a stream from them by reading one file at a time, in the chronological order they were created. The query we have to write is pretty much the same as the interactive query above.

``` scala
import org.apache.spark.sql.functions._

// Similar to definition of staticInputDF above, just using `readStream` instead of `read`
val streamingInputDF = 
  spark
    .readStream                       // `readStream` instead of `read` for creating streaming DataFrame
    .schema(jsonSchema)               // Set the schema of the JSON data
    .option("maxFilesPerTrigger", 1)  // Treat a sequence of files as a stream by picking one file at a time
    .json(inputPath)

// Same query as staticInputDF
val streamingCountsDF = 
  streamingInputDF
    .groupBy($"action", window($"time", "1 hour"))
    .count()

// Is this DF actually a streaming DF?
streamingCountsDF.isStreaming
```

>     import org.apache.spark.sql.functions._
>     streamingInputDF: org.apache.spark.sql.DataFrame = [time: timestamp, action: string]
>     streamingCountsDF: org.apache.spark.sql.DataFrame = [action: string, window: struct<start: timestamp, end: timestamp> ... 1 more field]
>     res5: Boolean = true

As you can see, `streamingCountsDF` is a streaming Dataframe (`streamingCountsDF.isStreaming` was `true`). You can start streaming computation, by defining the sink and starting it. In our case, we want to interactively query the counts (same queries as above), so we will set the complete set of 1 hour counts to be a in a in-memory table (note that this for testing purpose only in Spark 2.0).

``` scala
spark.conf.set("spark.sql.shuffle.partitions", "1")  // keep the size of shuffles small

val query =
  streamingCountsDF
    .writeStream
    .format("memory")        // memory = store in-memory table (for testing only in Spark 2.0)
    .queryName("counts")     // counts = name of the in-memory table
    .outputMode("complete")  // complete = all the counts should be in the table
    .start()
```

>     query: org.apache.spark.sql.streaming.StreamingQuery = Streaming Query - counts [state = ACTIVE]

`query` is a handle to the streaming query that is running in the background. This query is continuously picking up files and updating the windowed counts.

Note the status of query in the above cell. Both the `Status: ACTIVE` and the progress bar shows that the query is active. Furthermore, if you expand the `>Details` above, you will find the number of files they have already processed.

Let's wait a bit for a few files to be processed and then query the in-memory `counts` table.

``` scala
Thread.sleep(5000) // wait a bit for computation to start
```

``` sql
select action, date_format(window.end, "MMM-dd HH:mm") as time, count from counts order by time, action
```

| action | time         | count  |
|--------|--------------|--------|
| Close  | Jul-26 03:00 | 11.0   |
| Open   | Jul-26 03:00 | 179.0  |
| Close  | Jul-26 04:00 | 344.0  |
| Open   | Jul-26 04:00 | 1001.0 |
| Close  | Jul-26 05:00 | 176.0  |
| Open   | Jul-26 05:00 | 289.0  |

We see the timeline of windowed counts (similar to the static one ealrier) building up. If we keep running this interactive query repeatedly, we will see the latest updated counts which the streaming query is updating in the background.

``` scala
Thread.sleep(5000)  // wait a bit more for more data to be computed
```

``` sql
select action, date_format(window.end, "MMM-dd HH:mm") as time, count from counts order by time, action
```

| action | time         | count  |
|--------|--------------|--------|
| Close  | Jul-26 03:00 | 11.0   |
| Open   | Jul-26 03:00 | 179.0  |
| Close  | Jul-26 04:00 | 344.0  |
| Open   | Jul-26 04:00 | 1001.0 |
| Close  | Jul-26 05:00 | 815.0  |
| Open   | Jul-26 05:00 | 999.0  |
| Close  | Jul-26 06:00 | 323.0  |
| Open   | Jul-26 06:00 | 328.0  |

``` scala
Thread.sleep(5000)  // wait a bit more for more data to be computed
```

``` sql
select action, date_format(window.end, "MMM-dd HH:mm") as time, count from counts order by time, action
```

| action | time         | count  |
|--------|--------------|--------|
| Close  | Jul-26 03:00 | 11.0   |
| Open   | Jul-26 03:00 | 179.0  |
| Close  | Jul-26 04:00 | 344.0  |
| Open   | Jul-26 04:00 | 1001.0 |
| Close  | Jul-26 05:00 | 815.0  |
| Open   | Jul-26 05:00 | 999.0  |
| Close  | Jul-26 06:00 | 1003.0 |
| Open   | Jul-26 06:00 | 1000.0 |
| Close  | Jul-26 07:00 | 1011.0 |
| Open   | Jul-26 07:00 | 993.0  |
| Close  | Jul-26 08:00 | 314.0  |
| Open   | Jul-26 08:00 | 330.0  |

Also, let's see the total number of "opens" and "closes".

``` sql
select action, sum(count) as total_count from counts group by action order by action
```

| action | total\_count |
|--------|--------------|
| Close  | 3498.0       |
| Open   | 4502.0       |

If you keep running the above query repeatedly, you will always find that the number of "opens" is more than the number of "closes", as expected in a data stream where a "close" always appear after corresponding "open". This shows that Structured Streaming ensures **prefix integrity**. Read the blog posts linked below if you want to know more.

Note that there are only a few files, so consuming all of them there will be no updates to the counts. Rerun the query if you want to interact with the streaming query again.

Finally, you can stop the query running in the background, either by clicking on the 'Cancel' link in the cell of the query, or by executing `query.stop()`. Either way, when the query is stopped, the status of the corresponding cell above will automatically update to `TERMINATED`.

What's next?
------------

If you want to learn more about Structured Streaming, here are a few pointers.

-   Databricks blog posts on Structured Streaming and Continuous Applications
-   Blog post 1: [Continuous Applications: Evolving Streaming in Apache Spark 2.0](https://databricks.com/blog/2016/07/28/continuous-applications-evolving-streaming-in-apache-spark-2-0.html)
-   Blog post 2: [Structured Streaming in Apache Spark](https://databricks.com/blog/2016/07/28/structured-streaming-in-apache-spark.html)

-   [Structured Streaming Programming Guide](http://spark.apache.org/docs/latest/structured-streaming-programming-guide.html)

-   Spark Summit 2016 Talks
-   [Structuring Spark: Dataframes, Datasets And Streaming](https://spark-summit.org/2016/events/structuring-spark-dataframes-datasets-and-streaming/)
-   [A Deep Dive Into Structured Streaming](https://spark-summit.org/2016/events/a-deep-dive-into-structured-streaming/)

