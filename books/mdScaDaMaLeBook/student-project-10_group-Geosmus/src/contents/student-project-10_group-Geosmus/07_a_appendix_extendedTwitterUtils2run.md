<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

**Note, this notebook has been edited slightly from the course notebook supplied by Raaz.**

</div>

<div class="cell markdown">

[ScaDaMaLe, Scalable Data Science and Distributed Machine Learning](https://lamastex.github.io/scalable-data-science/sds/3/x/)
==============================================================================================================================

</div>

<div class="cell markdown">

Extended spark.streaming.twitter.TwitterUtils
=============================================

### 2016-2020 Ivan Sadikov and Raazesh Sainudiin

We extend twitter utils from Spark to allow for filtering by user-ids using `.follow` and strings in the tweet using `.track` method of `twitter4j`.

This is part of *Project MEP: Meme Evolution Programme* and supported by databricks, AWS and a Swedish VR grant.

The analysis is available in the following databricks notebook: \* [http://lamastex.org/lmse/mep/src/extendedTwitterUtils.html](http://lamastex.org/lmse/mep/src/extendedTwitterUtil.html)

    Copyright 2016-2020 Ivan Sadikov and Raazesh Sainudiin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import twitter4j._
import twitter4j.auth.Authorization
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization

import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
```

</div>

<div class="cell markdown">

### Twitter receiver and stream

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
// modifications inspired by https://github.com/apache/bahir/blob/master/streaming-twitter/src/main/scala/org/apache/spark/streaming/twitter/TwitterInputDStream.scala
class ExtendedTwitterReceiver(
    twitterAuth: Authorization,
    query: Option[FilterQuery],
    storageLevel: StorageLevel
  ) extends Receiver[Status](storageLevel) {

  @volatile private var twitterStream: TwitterStream = _
  @volatile private var stopped = false

  def onStart() {
    try {
      val newTwitterStream = new TwitterStreamFactory().getInstance(twitterAuth)
      newTwitterStream.addListener(new StatusListener {
        def onStatus(status: Status): Unit = {
          store(status)
        }
        // Unimplemented
        def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
        def onTrackLimitationNotice(i: Int) {}
        def onScrubGeo(l: Long, l1: Long) {}
        def onStallWarning(stallWarning: StallWarning) {}
        def onException(e: Exception) {
          if (!stopped) {
            restart("Error receiving tweets", e)
          }
        }
      })

      // do filtering only when filters are available
      if (query.isDefined) {
        newTwitterStream.filter(query.get)
      } else {
        newTwitterStream.sample()
      }
      setTwitterStream(newTwitterStream)
      println("Twitter receiver started")
      stopped = false
    } catch {
      case e: Exception => restart("Error starting Twitter stream", e)
    }
  }

  def onStop() {
    stopped = true
    setTwitterStream(null)
    println("Twitter receiver stopped")
  }

  private def setTwitterStream(newTwitterStream: TwitterStream) = synchronized {
    if (twitterStream != null) {
      twitterStream.shutdown()
    }
    twitterStream = newTwitterStream
  }
}
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
class ExtendedTwitterInputDStream(
    ssc_ : StreamingContext,
    twitterAuth: Option[Authorization],
    query: Option[FilterQuery],
    storageLevel: StorageLevel
  ) extends ReceiverInputDStream[Status](ssc_)  {

  private def createOAuthAuthorization(): Authorization = {
    new OAuthAuthorization(new ConfigurationBuilder().build())
  }

  private val authorization = twitterAuth.getOrElse(createOAuthAuthorization())

  override def getReceiver(): Receiver[Status] = {
    new ExtendedTwitterReceiver(authorization, query, storageLevel)
  }
}
```

</div>

<div class="cell markdown">

### Extended twitter utils

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
import twitter4j.Status
import twitter4j.auth.Authorization
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.{ReceiverInputDStream, DStream}

object ExtendedTwitterUtils {
  def createStream(
      ssc: StreamingContext,
      twitterAuth: Option[Authorization],
      query: Option[FilterQuery] = None,
      storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2
    ): ReceiverInputDStream[Status] = {
    new ExtendedTwitterInputDStream(ssc, twitterAuth, query, storageLevel)
  }
}
```

</div>

<div class="cell code" execution_count="1" scrolled="false">

``` scala
println("done running the extendedTwitterUtils2run notebook - ready to stream from twitter")
```

</div>
