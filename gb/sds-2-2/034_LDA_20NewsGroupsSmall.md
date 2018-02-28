[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Topic Modeling with Latent Dirichlet Allocation
===============================================

This is an augmentation of a notebook from Databricks Guide.
This notebook will provide a brief algorithm summary, links for further reading, and an example of how to use LDA for Topic Modeling.

Algorithm Summary
-----------------

-   **Task**: Identify topics from a collection of text documents
-   **Input**: Vectors of word counts
-   **Optimizers**:
    -   EMLDAOptimizer using [Expectation Maximization](https://en.wikipedia.org/wiki/Expectation%E2%80%93maximization_algorithm)
    -   OnlineLDAOptimizer using Iterative Mini-Batch Sampling for [Online Variational Bayes](https://www.cs.princeton.edu/~blei/papers/HoffmanBleiBach2010b.pdf)

Intro to LDA by David Blei
--------------------------

Watch at least the first 25 or so minutes of this video by David Blei on a crash introduction to topic modeling via Latent Dirichlet Allocation (LDA).

[![AJ's What is Collaborative Filtering](http://img.youtube.com/vi/FkckgwMHP2s/0.jpg)](https://www.youtube.com/watch?v=FkckgwMHP2s)

Links
-----

-   Spark API docs
-   Scala: [LDA](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.clustering.LDA)
-   Python: [LDA](https://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.clustering.LDA)
-   [MLlib Programming Guide](http://spark.apache.org/docs/latest/mllib-clustering.html#latent-dirichlet-allocation-lda)
-   [ML Feature Extractors & Transformers](http://spark.apache.org/docs/latest/ml-features.html)
-   [Wikipedia: Latent Dirichlet Allocation](https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation)

Readings for LDA
----------------

-   A high-level introduction to the topic from Communications of the ACM
    -   <http://www.cs.columbia.edu/~blei/papers/Blei2012.pdf>
-   A very good high-level humanities introduction to the topic (recommended by Chris Thomson in English Department at UC, Ilam):
    -   <http://journalofdigitalhumanities.org/2-1/topic-modeling-and-digital-humanities-by-david-m-blei/>

Also read the methodological and more formal papers cited in the above links if you want to know more.

Let's get a bird's eye view of LDA from <http://www.cs.columbia.edu/~blei/papers/Blei2012.pdf> next.

-   See pictures (hopefully you read the paper last night!)
-   Algorithm of the generative model (this is unsupervised clustering)
-   For a careful introduction to the topic see Section 27.3 and 27.4 (pages 950-970) pf Murphy's *Machine Learning: A Probabilistic Perspective, MIT Press, 2012*.
-   We will be quite application focussed or applied here!
-   Understand Expectation Maximization Algorithm read *Section 8.5 The EM Algorithm* in *The Elements of Statistical Learning* by Hastie, Tibshirani and Freidman (2001, Springer Series in Statistics). Read from free 21MB PDF of the book available from here <https://web.stanford.edu/~hastie/Papers/ESLII.pdf> or from its backup here <http://lamastex.org/research_events/Readings/StatLearn/ESLII.pdf>.

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA"
 width="95%" height="200"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Model"
 width="95%" height="600"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/ml-features.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Mathematical_definition"
 width="95%" height="910"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/ml-features.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

Probabilistic Topic Modeling Example
------------------------------------

This is an outline of our Topic Modeling workflow. Feel free to jump to any subtopic to find out more. - Step 0. Dataset Review - Step 1. Downloading and Loading Data into DBFS - (Step 1. only needs to be done once per shard - see details at the end of the notebook for Step 1.) - Step 2. Loading the Data and Data Cleaning - Step 3. Text Tokenization - Step 4. Remove Stopwords - Step 5. Vector of Token Counts - Step 6. Create LDA model with Online Variational Bayes - Step 7. Review Topics - Step 8. Model Tuning - Refilter Stopwords - Step 9. Create LDA model with Expectation Maximization - Step 10. Visualize Results

Step 0. Dataset Review
----------------------

In this example, we will use the mini [20 Newsgroups dataset](http://kdd.ics.uci.edu/databases/20newsgroups/20newsgroups.html), which is a random subset of the original 20 Newsgroups dataset. Each newsgroup is stored in a subdirectory, with each article stored as a separate file.

------------------------------------------------------------------------

------------------------------------------------------------------------

The following is the markdown file `20newsgroups.data.md` of the original details on the dataset, obtained as follows:

``` %sh
$ wget -k http://kdd.ics.uci.edu/databases/20newsgroups/20newsgroups.data.html
--2016-04-07 10:31:51--  http://kdd.ics.uci.edu/databases/20newsgroups/20newsgroups.data.html
Resolving kdd.ics.uci.edu (kdd.ics.uci.edu)... 128.195.1.95
Connecting to kdd.ics.uci.edu (kdd.ics.uci.edu)|128.195.1.95|:80... connected.
HTTP request sent, awaiting response... 200 OK
Length: 4371 (4.3K) [text/html]
Saving to: '20newsgroups.data.html’

100%[======================================>] 4,371       --.-K/s   in 0s      

2016-04-07 10:31:51 (195 MB/s) - '20newsgroups.data.html’ saved [4371/4371]

Converting 20newsgroups.data.html... nothing to do.
Converted 1 files in 0 seconds.

$ pandoc -f html -t markdown 20newsgroups.data.html > 20newsgroups.data.md
```

### 20 Newsgroups

#### Data Type

text

#### Abstract

This data set consists of 20000 messages taken from 20 newsgroups.

#### Sources

##### Original Owner and Donor

    Tom Mitchell
    School of Computer Science
    Carnegie Mellon University
    tom.mitchell@cmu.edu

**Date Donated:** September 9, 1999

#### Data Characteristics

One thousand Usenet articles were taken from each of the following 20 newsgroups.

        alt.atheism
        comp.graphics
        comp.os.ms-windows.misc
        comp.sys.ibm.pc.hardware
        comp.sys.mac.hardware
        comp.windows.x
        misc.forsale
        rec.autos
        rec.motorcycles
        rec.sport.baseball
        rec.sport.hockey
        sci.crypt
        sci.electronics
        sci.med
        sci.space
        soc.religion.christian
        talk.politics.guns
        talk.politics.mideast
        talk.politics.misc
        talk.religion.misc

Approximately 4% of the articles are crossposted. The articles are typical postings and thus have headers including subject lines, signature files, and quoted portions of other articles.

#### Data Format

Each newsgroup is stored in a subdirectory, with each article stored as a separate file.

#### Past Usage

T. Mitchell. Machine Learning, McGraw Hill, 1997.

T. Joachims (1996). [A probabilistic analysis of the Rocchio algorithm with TFIDF for text categorization](http://reports-archive.adm.cs.cmu.edu/anon/1996/CMU-CS-96-118.ps), Computer Science Technical Report CMU-CS-96-118. Carnegie Mellon University.

#### Acknowledgements, Copyright Information, and Availability

You may use this material free of charge for any educational purpose, provided attribution is given in any lectures or publications that make use of this material.

#### References and Further Information

Naive Bayes code for text classification is available from: <http://www.cs.cmu.edu/afs/cs/project/theo-11/www/naive-bayes.html>

------------------------------------------------------------------------

[The UCI KDD Archive](http://kdd.ics.uci.edu/)
[Information and Computer Science](http://www.ics.uci.edu/)
[University of California, Irvine](http://www.uci.edu/)
Irvine, CA 92697-3425
Last modified: September 9, 1999

------------------------------------------------------------------------

------------------------------------------------------------------------

**NOTE:** The mini dataset consists of 100 articles from the following 20 Usenet newsgroups:

    alt.atheism
    comp.graphics
    comp.os.ms-windows.misc
    comp.sys.ibm.pc.hardware
    comp.sys.mac.hardware
    comp.windows.x
    misc.forsale
    rec.autos
    rec.motorcycles
    rec.sport.baseball
    rec.sport.hockey
    sci.crypt
    sci.electronics
    sci.med
    sci.space
    soc.religion.christian
    talk.politics.guns
    talk.politics.mideast
    talk.politics.misc
    talk.religion.misc

Some of the newsgroups seem pretty similar on first glance, such as *comp.sys.ibm.pc.hardware* and *comp.sys.mac.hardware*, which may affect our results.

**NOTE:** A simpler and slicker version of the analysis is available in this notebook: \* <https://docs.cloud.databricks.com/docs/latest/sample_applications/07%20Sample%20ML/MLPipeline%20Newsgroup%20Dataset.html>

But, let's do it the hard way here so that we can do it on other arbitrary datasets.

Step 2. Loading the Data and Data Cleaning
------------------------------------------

We have already used the wget command to download the file, and put it in our distributed file system (this process takes about 10 minutes). To repeat these steps or to download data from another source follow the steps at the bottom of this worksheet on **Step 1. Downloading and Loading Data into DBFS**.

Let's make sure these files are in dbfs now:

``` scala
display(dbutils.fs.ls("dbfs:/datasets/mini_newsgroups")) // this is where the data resides in dbfs (see below to download it first, if you go to a new shard!)
```

| path                                                      | name                      | size |
|-----------------------------------------------------------|---------------------------|------|
| dbfs:/datasets/mini\_newsgroups/alt.atheism/              | alt.atheism/              | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.graphics/            | comp.graphics/            | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.os.ms-windows.misc/  | comp.os.ms-windows.misc/  | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.sys.ibm.pc.hardware/ | comp.sys.ibm.pc.hardware/ | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.sys.mac.hardware/    | comp.sys.mac.hardware/    | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.windows.x/           | comp.windows.x/           | 0.0  |
| dbfs:/datasets/mini\_newsgroups/misc.forsale/             | misc.forsale/             | 0.0  |
| dbfs:/datasets/mini\_newsgroups/rec.autos/                | rec.autos/                | 0.0  |
| dbfs:/datasets/mini\_newsgroups/rec.motorcycles/          | rec.motorcycles/          | 0.0  |
| dbfs:/datasets/mini\_newsgroups/rec.sport.baseball/       | rec.sport.baseball/       | 0.0  |
| dbfs:/datasets/mini\_newsgroups/rec.sport.hockey/         | rec.sport.hockey/         | 0.0  |
| dbfs:/datasets/mini\_newsgroups/sci.crypt/                | sci.crypt/                | 0.0  |
| dbfs:/datasets/mini\_newsgroups/sci.electronics/          | sci.electronics/          | 0.0  |
| dbfs:/datasets/mini\_newsgroups/sci.med/                  | sci.med/                  | 0.0  |
| dbfs:/datasets/mini\_newsgroups/sci.space/                | sci.space/                | 0.0  |
| dbfs:/datasets/mini\_newsgroups/soc.religion.christian/   | soc.religion.christian/   | 0.0  |
| dbfs:/datasets/mini\_newsgroups/talk.politics.guns/       | talk.politics.guns/       | 0.0  |
| dbfs:/datasets/mini\_newsgroups/talk.politics.mideast/    | talk.politics.mideast/    | 0.0  |
| dbfs:/datasets/mini\_newsgroups/talk.politics.misc/       | talk.politics.misc/       | 0.0  |
| dbfs:/datasets/mini\_newsgroups/talk.religion.misc/       | talk.religion.misc/       | 0.0  |

Now let us read in the data using `wholeTextFiles()`.

Recall that the `wholeTextFiles()` command will read in the entire directory of text files, and return a key-value pair of (filePath, fileContent).

As we do not need the file paths in this example, we will apply a map function to extract the file contents, and then convert everything to lowercase.

``` scala
// Load text file, leave out file paths, convert all strings to lowercase
val corpus = sc.wholeTextFiles("/datasets/mini_newsgroups/*").map(_._2).map(_.toLowerCase()).cache() // let's cache
```

>     corpus: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[33636] at map at <console>:43

``` scala
corpus.count // there are 2000 documents in total - this action will take about 2 minutes
```

>     res34: Long = 2000

Review first 5 documents to get a sense for the data format.

``` scala
corpus.take(5)
```

>     res35: Array[String] =
>     Array("xref: cantaloupe.srv.cs.cmu.edu alt.atheism:51121 soc.motss:139944 rec.scouting:5318
>     newsgroups: alt.atheism,soc.motss,rec.scouting
>     path: cantaloupe.srv.cs.cmu.edu!crabapple.srv.cs.cmu.edu!fs7.ece.cmu.edu!europa.eng.gtefsd.com!howland.reston.ans.net!wupost!uunet!newsgate.watson.ibm.com!yktnews.watson.ibm.com!watson!watson.ibm.com!strom
>     from: strom@watson.ibm.com (rob strom)
>     subject: re: [soc.motss, et al.] "princeton axes matching funds for boy scouts"
>     sender: @watson.ibm.com
>     message-id: <1993apr05.180116.43346@watson.ibm.com>
>     date: mon, 05 apr 93 18:01:16 gmt
>     distribution: usa
>     references: <c47efs.3q47@austin.ibm.com> <1993mar22.033150.17345@cbnewsl.cb.att.com> <n4hy.93apr5120934@harder.ccr-p.ida.org>
>     organization: ibm research
>     lines: 15
>
>     in article <n4hy.93apr5120934@harder.ccr-p.ida.org>, n4hy@harder.ccr-p.ida.org (bob mcgwier) writes:
>
>     |> [1] however, i hate economic terrorism and political correctness
>     |> worse than i hate this policy.
>
>
>     |> [2] a more effective approach is to stop donating
>     |> to any organizating that directly or indirectly supports gay rights issues
>     |> until they end the boycott on funding of scouts.
>
>     can somebody reconcile the apparent contradiction between [1] and [2]?
>
>     --
>     rob strom, strom@watson.ibm.com, (914) 784-7641
>     ibm research, 30 saw mill river road, p.o. box 704, yorktown heights, ny  10598
>     ", "path: cantaloupe.srv.cs.cmu.edu!crabapple.srv.cs.cmu.edu!fs7.ece.cmu.edu!europa.eng.gtefsd.com!howland.reston.ans.net!noc.near.net!news.centerline.com!uunet!olivea!sgigate!sgiblab!adagio.panasonic.com!nntp-server.caltech.edu!keith
>     from: keith@cco.caltech.edu (keith allan schneider)
>     newsgroups: alt.atheism
>     subject: re: >>>>>>pompous ass
>     message-id: <1pi9btinnqa5@gap.caltech.edu>
>     date: 2 apr 93 20:57:33 gmt
>     references: <1ou4koinne67@gap.caltech.edu> <1p72bkinnjt7@gap.caltech.edu> <93089.050046mvs104@psuvm.psu.edu> <1pa6ntinns5d@gap.caltech.edu> <1993mar30.210423.1302@bmerh85.bnr.ca> <1pcnqjinnpon@gap.caltech.edu> <kmr4.1344.733611641@po.cwru.edu>
>     organization: california institute of technology, pasadena
>     lines: 9
>     nntp-posting-host: punisher.caltech.edu
>
>     kmr4@po.cwru.edu (keith m. ryan) writes:
>
>     >>then why do people keep asking the same questions over and over?
>     >because you rarely ever answer them.
>
>     nope, i've answered each question posed, and most were answered multiple
>     times.
>
>     keith
>     ", "path: cantaloupe.srv.cs.cmu.edu!crabapple.srv.cs.cmu.edu!fs7.ece.cmu.edu!europa.eng.gtefsd.com!howland.reston.ans.net!noc.near.net!news.centerline.com!uunet!olivea!sgigate!sgiblab!adagio.panasonic.com!nntp-server.caltech.edu!keith
>     from: keith@cco.caltech.edu (keith allan schneider)
>     newsgroups: alt.atheism
>     subject: re: >>>>>>pompous ass
>     message-id: <1pi9jkinnqe2@gap.caltech.edu>
>     date: 2 apr 93 21:01:40 gmt
>     references: <1ou4koinne67@gap.caltech.edu> <1p72bkinnjt7@gap.caltech.edu> <93089.050046mvs104@psuvm.psu.edu> <1pa6ntinns5d@gap.caltech.edu> <1993mar30.205919.26390@blaze.cs.jhu.edu> <1pcnp3innpom@gap.caltech.edu> <1pdjip$jsi@fido.asd.sgi.com>
>     organization: california institute of technology, pasadena
>     lines: 14
>     nntp-posting-host: punisher.caltech.edu
>
>     livesey@solntze.wpd.sgi.com (jon livesey) writes:
>
>     >>>how long does it [the motto] have to stay around before it becomes the
>     >>>default?  ...  where's the cutoff point?
>     >>i don't know where the exact cutoff is, but it is at least after a few
>     >>years, and surely after 40 years.
>     >why does the notion of default not take into account changes
>     >in population makeup?
>
>     specifically, which changes are you talking about?  are you arguing
>     that the motto is interpreted as offensive by a larger portion of the
>     population now than 40 years ago?
>
>     keith
>     ", "path: cantaloupe.srv.cs.cmu.edu!crabapple.srv.cs.cmu.edu!fs7.ece.cmu.edu!europa.eng.gtefsd.com!howland.reston.ans.net!wupost!sdd.hp.com!sgiblab!adagio.panasonic.com!nntp-server.caltech.edu!keith
>     from: keith@cco.caltech.edu (keith allan schneider)
>     newsgroups: alt.atheism
>     subject: re: <political atheists?
>     date: 2 apr 1993 21:22:59 gmt
>     organization: california institute of technology, pasadena
>     lines: 44
>     message-id: <1piarjinnqsa@gap.caltech.edu>
>     references: <1p9bseinni6o@gap.caltech.edu> <1pamva$b6j@fido.asd.sgi.com> <1pcq4pinnqp1@gap.caltech.edu> <11702@vice.ico.tek.com>
>     nntp-posting-host: punisher.caltech.edu
>
>     bobbe@vice.ico.tek.com (robert beauchaine) writes:
>
>     >>but, you don't know that capital punishment is wrong, so it isn't the same
>     >>as shooting.  a better analogy would be that you continue to drive your car,
>     >>realizing that sooner or later, someone is going to be killed in an automobile
>     >>accident.  you *know* people get killed as a result of driving, yet you
>     >>continue to do it anyway.
>     >uh uh.  you do not know that you will be the one to do the
>     >killing.  i'm not sure i'd drive a car if i had sufficient evidence to
>     >conclude that i would necessarily kill someone during my lifetime.
>
>     yes, and everyone thinks as you do.  no one thinks that he is going to cause
>     or be involved in a fatal accident, but the likelihood is surprisingly high.
>     just because you are the man on the firing squad whose gun is shooting
>     blanks does not mean that you are less guilty.
>
>     >i don't know about jon, but i say *all* taking of human life is
>     >murder.  and i say murder is wrong in all but one situation:  when
>     >it is the only action that will prevent another murder, either of
>     >myself or another.
>
>     you mean that killing is wrong in all but one situtation?  and, you should
>     note that that situation will never occur.  there are always other options
>     thank killing.  why don't you just say that all killing is wrong.  this
>     is basically what you are saying.
>
>     >i'm getting a bit tired of your probabilistic arguments.
>
>     are you attempting to be condescending?
>
>     >that the system usually works pretty well is small consolation to
>     >the poor innocent bastard getting the lethal injection.  is your
>     >personal value of human life based solely on a statistical approach?
>     >you sound like an unswerving adherent to the needs of the many
>     >outweighing the needs of the few, so fuck the few.
>
>     but, most people have found the risk to be acceptable.  you are probably
>     much more likely to die in a plane crash, or even using an electric
>     blender, than you are to be executed as an innocent.  i personally think
>     that the risk is acceptable, but in an ideal moral system, no such risk
>     is acceptable.  "acceptable" is the fudge factor necessary in such an
>     approximation to the ideal.
>
>     keith
>     ", "path: cantaloupe.srv.cs.cmu.edu!das-news.harvard.edu!husc-news.harvard.edu!kuhub.cc.ukans.edu!wupost!howland.reston.ans.net!zaphod.mps.ohio-state.edu!sol.ctr.columbia.edu!ursa!pooh!halat
>     newsgroups: alt.atheism
>     subject: re: there must be a creator! (maybe)
>     message-id: <30066@ursa.bear.com>
>     from: halat@pooh.bears (jim halat)
>     date: 1 apr 93 21:24:35 gmt
>     reply-to: halat@pooh.bears (jim halat)
>     sender: news@bear.com
>     references: <16ba1e927.drporter@suvm.syr.edu>
>     lines: 24
>
>     in article <16ba1e927.drporter@suvm.syr.edu>, drporter@suvm.syr.edu (brad porter) writes:
>     >
>     >   science is wonderful at answering most of our questions.  i'm not the type
>     >to question scientific findings very often, but...  personally, i find the
>     >theory of evolution to be unfathomable.  could humans, a highly evolved,
>     >complex organism that thinks, learns, and develops truly be an organism
>     >that resulted from random genetic mutations and natural selection?
>
>     [...stuff deleted...]
>
>     computers are an excellent example...of evolution without "a" creator.
>     we did not "create" computers.  we did not create the sand that goes
>     into the silicon that goes into the integrated circuits that go into
>     processor board.  we took these things and put them together in an
>     interesting way. just like plants "create" oxygen using light through
>     photosynthesis.  it's a much bigger leap to talk about something that
>     created "everything" from nothing.  i find it unfathomable to resort
>     to believing in a creator when a much simpler alternative exists: we
>     simply are incapable of understanding our beginnings -- if there even
>     were beginnings at all.  and that's ok with me.  the present keeps me
>     perfectly busy.
>
>     -jim halat
>
>     ")

To review a random document in the corpus uncomment and evaluate the following cell.

``` scala
corpus.takeSample(false, 1)
```

>     res36: Array[String] =
>     Array("newsgroups: misc.forsale
>     path: cantaloupe.srv.cs.cmu.edu!das-news.harvard.edu!noc.near.net!uunet!mnemosyne.cs.du.edu!nyx!lgibb
>     from: lgibb@nyx.cs.du.edu (lance gibb)
>     subject: rc car for trade
>     message-id: <1993apr17.034639.28176@mnemosyne.cs.du.edu>
>     sender: usenet@mnemosyne.cs.du.edu (netnews admin account)
>     organization: nyx, public access unix @ u. of denver math/cs dept.
>     distribution: na
>     date: sat, 17 apr 93 03:46:39 gmt
>     lines: 30
>
>
>     title just 'bout says it all:
>
>     grasshopper remote controlled car for sale/trade
>
>     features:
>      -$75 racing engine installed (original included as well)
>      -2 sets of tires
>      -futaba 2 channel radio with servos/receiver
>      -body completly refinished - great shape
>      -battery and charger
>      -every thing you need to have it running right out of the box,
>       very fast
>      -everything 100%
>
>     i haven't run this thing in a long time.  i had it out the other
>     day just to check on it and everything is a-ok.
>
>     i'd listen to any cash offers, but am more interested in trading
>     for some extra storage for my computer.  if you have any of the
>     following and are interested in a trade, drop me a line:
>
>     ide hard drive 50+ megs (must be 3.5" wide, 1" tall)
>     scsi hard drive 50+ megs (must be 3.5" wide, 1" tall)
>     scsi tape backup (any make/size)
>     scsi cd-rom
>     9600 baud modem (external)
>
>     please leave any offers/questions in email to lgibb@nyx.cs.du.edu
>
>     ")

Note that the document begins with a header containing some metadata that we don't need, and we are only interested in the body of the document. We can do a bit of simple data cleaning here by removing the metadata of each document, which reduces the noise in our dataset. This is an important step as the accuracy of our models depend greatly on the quality of data used.

``` scala
// Split document by double newlines, drop the first block, combine again as a string and cache
val corpus_body = corpus.map(_.split("\\n\\n")).map(_.drop(1)).map(_.mkString(" ")).cache()
```

>     corpus_body: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[46143] at map at <console>:43

``` scala
corpus_body.count() // there should still be the same count, but now without meta-data block
```

>     res37: Long = 2000

Let's review first 5 documents with metadata removed.

``` scala
corpus_body.take(5)
```

>     res38: Array[String] =
>     Array("in article <n4hy.93apr5120934@harder.ccr-p.ida.org>, n4hy@harder.ccr-p.ida.org (bob mcgwier) writes: |> [1] however, i hate economic terrorism and political correctness
>     |> worse than i hate this policy.
>     |> [2] a more effective approach is to stop donating
>     |> to any organizating that directly or indirectly supports gay rights issues
>     |> until they end the boycott on funding of scouts.   can somebody reconcile the apparent contradiction between [1] and [2]? --
>     rob strom, strom@watson.ibm.com, (914) 784-7641
>     ibm research, 30 saw mill river road, p.o. box 704, yorktown heights, ny  10598
>     ", "kmr4@po.cwru.edu (keith m. ryan) writes: >>then why do people keep asking the same questions over and over?
>     >because you rarely ever answer them. nope, i've answered each question posed, and most were answered multiple
>     times. keith
>     ", "livesey@solntze.wpd.sgi.com (jon livesey) writes: >>>how long does it [the motto] have to stay around before it becomes the
>     >>>default?  ...  where's the cutoff point?
>     >>i don't know where the exact cutoff is, but it is at least after a few
>     >>years, and surely after 40 years.
>     >why does the notion of default not take into account changes
>     >in population makeup?      specifically, which changes are you talking about?  are you arguing
>     that the motto is interpreted as offensive by a larger portion of the
>     population now than 40 years ago? keith
>     ", "bobbe@vice.ico.tek.com (robert beauchaine) writes: >>but, you don't know that capital punishment is wrong, so it isn't the same
>     >>as shooting.  a better analogy would be that you continue to drive your car,
>     >>realizing that sooner or later, someone is going to be killed in an automobile
>     >>accident.  you *know* people get killed as a result of driving, yet you
>     >>continue to do it anyway.
>     >uh uh.  you do not know that you will be the one to do the
>     >killing.  i'm not sure i'd drive a car if i had sufficient evidence to
>     >conclude that i would necessarily kill someone during my lifetime. yes, and everyone thinks as you do.  no one thinks that he is going to cause
>     or be involved in a fatal accident, but the likelihood is surprisingly high.
>     just because you are the man on the firing squad whose gun is shooting
>     blanks does not mean that you are less guilty. >i don't know about jon, but i say *all* taking of human life is
>     >murder.  and i say murder is wrong in all but one situation:  when
>     >it is the only action that will prevent another murder, either of
>     >myself or another. you mean that killing is wrong in all but one situtation?  and, you should
>     note that that situation will never occur.  there are always other options
>     thank killing.  why don't you just say that all killing is wrong.  this
>     is basically what you are saying. >i'm getting a bit tired of your probabilistic arguments. are you attempting to be condescending? >that the system usually works pretty well is small consolation to
>     >the poor innocent bastard getting the lethal injection.  is your
>     >personal value of human life based solely on a statistical approach?
>     >you sound like an unswerving adherent to the needs of the many
>     >outweighing the needs of the few, so fuck the few. but, most people have found the risk to be acceptable.  you are probably
>     much more likely to die in a plane crash, or even using an electric
>     blender, than you are to be executed as an innocent.  i personally think
>     that the risk is acceptable, but in an ideal moral system, no such risk
>     is acceptable.  "acceptable" is the fudge factor necessary in such an
>     approximation to the ideal. keith
>     ", in article <16ba1e927.drporter@suvm.syr.edu>, drporter@suvm.syr.edu (brad porter) writes:
>     >
>     >   science is wonderful at answering most of our questions.  i'm not the type
>     >to question scientific findings very often, but...  personally, i find the
>     >theory of evolution to be unfathomable.  could humans, a highly evolved,
>     >complex organism that thinks, learns, and develops truly be an organism
>     >that resulted from random genetic mutations and natural selection? [...stuff deleted...] computers are an excellent example...of evolution without "a" creator.
>     we did not "create" computers.  we did not create the sand that goes
>     into the silicon that goes into the integrated circuits that go into
>     processor board.  we took these things and put them together in an
>     interesting way. just like plants "create" oxygen using light through
>     photosynthesis.  it's a much bigger leap to talk about something that
>     created "everything" from nothing.  i find it unfathomable to resort
>     to believing in a creator when a much simpler alternative exists: we
>     simply are incapable of understanding our beginnings -- if there even
>     were beginnings at all.  and that's ok with me.  the present keeps me
>     perfectly busy. -jim halat)

Feature extraction and transformation APIs
------------------------------------------

See <http://spark.apache.org/docs/latest/ml-features.html>

To use the convenient [Feature extraction and transformation APIs](http://spark.apache.org/docs/latest/ml-features.html), we will convert our RDD into a DataFrame.

We will also create an ID for every document using `zipWithIndex` \* for sytax and details search for `zipWithIndex` in <https://spark.apache.org/docs/latest/api/scala/org/apache/spark/rdd/RDD.html>

``` scala
// Convert RDD to DF with ID for every document 
val corpus_df = corpus_body.zipWithIndex.toDF("corpus", "id")
```

>     corpus_df: org.apache.spark.sql.DataFrame = [corpus: string, id: bigint]

``` scala
//display(corpus_df) // uncomment to see corpus 
// this was commented out after a member of the new group requested to remain anonymous on 20160525
```

Step 3. Text Tokenization
-------------------------

We will use the RegexTokenizer to split each document into tokens. We can setMinTokenLength() here to indicate a minimum token length, and filter away all tokens that fall below the minimum.

See <http://spark.apache.org/docs/latest/ml-features.html#tokenizer>.

``` scala
import org.apache.spark.ml.feature.RegexTokenizer

// Set params for RegexTokenizer
val tokenizer = new RegexTokenizer()
.setPattern("[\\W_]+") // break by white space character(s)  - try to remove emails and other patterns
.setMinTokenLength(4) // Filter away tokens with length < 4
.setInputCol("corpus") // name of the input column
.setOutputCol("tokens") // name of the output column

// Tokenize document
val tokenized_df = tokenizer.transform(corpus_df)
```

>     import org.apache.spark.ml.feature.RegexTokenizer
>     tokenizer: org.apache.spark.ml.feature.RegexTokenizer = regexTok_88ec7ca10d30
>     tokenized_df: org.apache.spark.sql.DataFrame = [corpus: string, id: bigint ... 1 more field]

``` scala
//display(tokenized_df) // uncomment to see tokenized_df 
// this was commented out after a member of the new group requested to remain anonymous on 20160525
```

``` scala
display(tokenized_df.select("tokens"))
```

Step 4. Remove Stopwords
------------------------

We can easily remove stopwords using the StopWordsRemover().

See <http://spark.apache.org/docs/latest/ml-features.html#stopwordsremover>.

If a list of stopwords is not provided, the StopWordsRemover() will use [this list of stopwords](http://ir.dcs.gla.ac.uk/resources/linguistic_utils/stop_words), also shown below, by default.

``` a,about,above,across,after,afterwards,again,against,all,almost,alone,along,already,also,although,always,am,among,amongst,amoungst,amount,an,and,another,any,anyhow,anyone,anything,anyway,anywhere,
are,around,as,at,back,be,became,because,become,becomes,becoming,been,before,beforehand,behind,being,below,beside,besides,between,beyond,bill,both,bottom,but,by,call,can,cannot,cant,co,computer,con,could,
couldnt,cry,de,describe,detail,do,done,down,due,during,each,eg,eight,either,eleven,else,elsewhere,empty,enough,etc,even,ever,every,everyone,everything,everywhere,except,few,fifteen,fify,fill,find,fire,first,
five,for,former,formerly,forty,found,four,from,front,full,further,get,give,go,had,has,hasnt,have,he,hence,her,here,hereafter,hereby,herein,hereupon,hers,herself,him,himself,his,how,however,hundred,i,ie,if,
in,inc,indeed,interest,into,is,it,its,itself,keep,last,latter,latterly,least,less,ltd,made,many,may,me,meanwhile,might,mill,mine,more,moreover,most,mostly,move,much,must,my,myself,name,namely,neither,never,
nevertheless,next,nine,no,nobody,none,noone,nor,not,nothing,now,nowhere,of,off,often,on,once,one,only,onto,or,other,others,otherwise,our,ours,ourselves,out,over,own,part,per,perhaps,please,put,rather,re,same,
see,seem,seemed,seeming,seems,serious,several,she,should,show,side,since,sincere,six,sixty,so,some,somehow,someone,something,sometime,sometimes,somewhere,still,such,system,take,ten,than,that,the,their,them,
themselves,then,thence,there,thereafter,thereby,therefore,therein,thereupon,these,they,thick,thin,third,this,those,though,three,through,throughout,thru,thus,to,together,too,top,toward,towards,twelve,twenty,two,
un,under,until,up,upon,us,very,via,was,we,well,were,what,whatever,when,whence,whenever,where,whereafter,whereas,whereby,wherein,whereupon,wherever,whether,which,while,whither,who,whoever,whole,whom,whose,why,will,
with,within,without,would,yet,you,your,yours,yourself,yourselves
```

You can use `getStopWords()` to see the list of stopwords that will be used.

In this example, we will specify a list of stopwords for the StopWordsRemover() to use. We do this so that we can add on to the list later on.

``` scala
display(dbutils.fs.ls("dbfs:/tmp/stopwords")) // check if the file already exists from earlier wget and dbfs-load
```

| path                | name      | size   |
|---------------------|-----------|--------|
| dbfs:/tmp/stopwords | stopwords | 2237.0 |

If the file `dbfs:/tmp/stopwords` already exists then skip the next two cells, otherwise download and load it into DBFS by uncommenting and evaluating the next two cells.

``` scala
//%sh wget http://ir.dcs.gla.ac.uk/resources/linguistic_utils/stop_words -O /tmp/stopwords # uncomment '//' at the beginning and repeat only if needed again
```

>     --2017-11-07 19:44:24--  http://ir.dcs.gla.ac.uk/resources/linguistic_utils/stop_words
>     Resolving ir.dcs.gla.ac.uk (ir.dcs.gla.ac.uk)... 130.209.240.253
>     Connecting to ir.dcs.gla.ac.uk (ir.dcs.gla.ac.uk)|130.209.240.253|:80... connected.
>     HTTP request sent, awaiting response... 200 OK
>     Length: 2237 (2.2K) [text/plain]
>     Saving to: ‘/tmp/stopwords’
>
>          0K ..                                                    100%  438M=0s
>
>     2017-11-07 19:44:24 (438 MB/s) - ‘/tmp/stopwords’ saved [2237/2237]

``` scala
//%fs cp file:/tmp/stopwords dbfs:/tmp/stopwords # uncomment '//' at the beginning and repeat only if needed again
```

``` scala
// List of stopwords
val stopwords = sc.textFile("/tmp/stopwords").collect()
```

>     stopwords: Array[String] = Array(a, about, above, across, after, afterwards, again, against, all, almost, alone, along, already, also, although, always, am, among, amongst, amoungst, amount, an, and, another, any, anyhow, anyone, anything, anyway, anywhere, are, around, as, at, back, be, became, because, become, becomes, becoming, been, before, beforehand, behind, being, below, beside, besides, between, beyond, bill, both, bottom, but, by, call, can, cannot, cant, co, computer, con, could, couldnt, cry, de, describe, detail, do, done, down, due, during, each, eg, eight, either, eleven, else, elsewhere, empty, enough, etc, even, ever, every, everyone, everything, everywhere, except, few, fifteen, fify, fill, find, fire, first, five, for, former, formerly, forty, found, four, from, front, full, further, get, give, go, had, has, hasnt, have, he, hence, her, here, hereafter, hereby, herein, hereupon, hers, herself, him, himself, his, how, however, hundred, i, ie, if, in, inc, indeed, interest, into, is, it, its, itself, keep, last, latter, latterly, least, less, ltd, made, many, may, me, meanwhile, might, mill, mine, more, moreover, most, mostly, move, much, must, my, myself, name, namely, neither, never, nevertheless, next, nine, no, nobody, none, noone, nor, not, nothing, now, nowhere, of, off, often, on, once, one, only, onto, or, other, others, otherwise, our, ours, ourselves, out, over, own, part, per, perhaps, please, put, rather, re, same, see, seem, seemed, seeming, seems, serious, several, she, should, show, side, since, sincere, six, sixty, so, some, somehow, someone, something, sometime, sometimes, somewhere, still, such, system, take, ten, than, that, the, their, them, themselves, then, thence, there, thereafter, thereby, therefore, therein, thereupon, these, they, thick, thin, third, this, those, though, three, through, throughout, thru, thus, to, together, too, top, toward, towards, twelve, twenty, two, un, under, until, up, upon, us, very, via, was, we, well, were, what, whatever, when, whence, whenever, where, whereafter, whereas, whereby, wherein, whereupon, wherever, whether, which, while, whither, who, whoever, whole, whom, whose, why, will, with, within, without, would, yet, you, your, yours, yourself, yourselves)

``` scala
stopwords.length // find the number of stopwords in the scala Array[String]
```

>     res43: Int = 319

Finally, we can just remove the stopwords using the `StopWordsRemover` as follows:

``` scala
import org.apache.spark.ml.feature.StopWordsRemover

// Set params for StopWordsRemover
val remover = new StopWordsRemover()
.setStopWords(stopwords) // This parameter is optional
.setInputCol("tokens")
.setOutputCol("filtered")

// Create new DF with Stopwords removed
val filtered_df = remover.transform(tokenized_df)
```

>     import org.apache.spark.ml.feature.StopWordsRemover
>     remover: org.apache.spark.ml.feature.StopWordsRemover = stopWords_e275b95ef77b
>     filtered_df: org.apache.spark.sql.DataFrame = [corpus: string, id: bigint ... 2 more fields]

Step 5. Vector of Token Counts
------------------------------

LDA takes in a vector of token counts as input. We can use the `CountVectorizer()` to easily convert our text documents into vectors of token counts.

The `CountVectorizer` will return `(VocabSize, Array(Indexed Tokens), Array(Token Frequency))`.

Two handy parameters to note: - `setMinDF`: Specifies the minimum number of different documents a term must appear in to be included in the vocabulary. - `setMinTF`: Specifies the minimum number of times a term has to appear in a document to be included in the vocabulary.

See <http://spark.apache.org/docs/latest/ml-features.html#countvectorizer>.

``` scala
import org.apache.spark.ml.feature.CountVectorizer

// Set params for CountVectorizer
val vectorizer = new CountVectorizer()
.setInputCol("filtered")
.setOutputCol("features")
.setVocabSize(10000) 
.setMinDF(5) // the minimum number of different documents a term must appear in to be included in the vocabulary.
.fit(filtered_df)
```

>     import org.apache.spark.ml.feature.CountVectorizer
>     vectorizer: org.apache.spark.ml.feature.CountVectorizerModel = cntVec_011159f8a9b4

``` scala
// Create vector of token counts
val countVectors = vectorizer.transform(filtered_df).select("id", "features")
```

>     countVectors: org.apache.spark.sql.DataFrame = [id: bigint, features: vector]

``` scala
// see the first countVectors
countVectors.take(2)
```

>     res45: Array[org.apache.spark.sql.Row] = Array([0,(6139,[0,1,147,231,315,496,497,527,569,604,776,835,848,858,942,1144,1687,1980,2051,2455,2756,3060,3465,3660,4506,5434,5599],[1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,2.0,1.0,1.0,1.0,1.0,2.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0])], [1,(6139,[0,2,43,135,188,239,712,786,936,963,1376,2144,2375,2792,5980,6078],[1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,2.0,1.0,1.0,1.0,1.0,2.0,1.0,1.0])])

To use the LDA algorithm in the MLlib library, we have to convert the DataFrame back into an RDD.

``` scala
// Convert DF to RDD
import org.apache.spark.ml.linalg.Vector

val lda_countVector = countVectors.map { case Row(id: Long, countVector: Vector) => (id, countVector) }
```

>     import org.apache.spark.ml.linalg.Vector
>     lda_countVector: org.apache.spark.sql.Dataset[(Long, org.apache.spark.ml.linalg.Vector)] = [_1: bigint, _2: vector]

``` scala
// format: Array(id, (VocabSize, Array(indexedTokens), Array(Token Frequency)))
lda_countVector.take(1)
```

>     res46: Array[(Long, org.apache.spark.ml.linalg.Vector)] = Array((0,(6139,[0,1,147,231,315,496,497,527,569,604,776,835,848,858,942,1144,1687,1980,2051,2455,2756,3060,3465,3660,4506,5434,5599],[1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,2.0,1.0,1.0,1.0,1.0,2.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0])))

Let's get an overview of LDA in Spark's MLLIB
---------------------------------------------

See <http://spark.apache.org/docs/latest/mllib-clustering.html#latent-dirichlet-allocation-lda>

Create LDA model with Online Variational Bayes
----------------------------------------------

We will now set the parameters for LDA. We will use the OnlineLDAOptimizer() here, which implements Online Variational Bayes.

Choosing the number of topics for your LDA model requires a bit of domain knowledge. As we know that there are 20 unique newsgroups in our dataset, we will set numTopics to be 20.

``` scala
val numTopics = 20
```

>     numTopics: Int = 20

We will set the parameters needed to build our LDA model. We can also setMiniBatchFraction for the OnlineLDAOptimizer, which sets the fraction of corpus sampled and used at each iteration. In this example, we will set this to 0.8.

``` scala
import org.apache.spark.mllib.clustering.{LDA, OnlineLDAOptimizer}

// Set LDA params
val lda = new LDA()
.setOptimizer(new OnlineLDAOptimizer().setMiniBatchFraction(0.8))
.setK(numTopics)
.setMaxIterations(3)
.setDocConcentration(-1) // use default values
.setTopicConcentration(-1) // use default values
```

>     import org.apache.spark.mllib.clustering.{LDA, OnlineLDAOptimizer}
>     lda: org.apache.spark.mllib.clustering.LDA = org.apache.spark.mllib.clustering.LDA@e6f4da4

Create the LDA model with Online Variational Bayes.

``` scala
// convert ML vectors into MLlib vectors
val lda_countVector_mllib = lda_countVector.map { case (id, vector) => (id, org.apache.spark.mllib.linalg.Vectors.fromML(vector)) }.rdd

val ldaModel = lda.run(lda_countVector_mllib)
```

>     lda_countVector_mllib: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.linalg.Vector)] = MapPartitionsRDD[46177] at rdd at <console>:53
>     ldaModel: org.apache.spark.mllib.clustering.LDAModel = org.apache.spark.mllib.clustering.LocalLDAModel@4a256bff

Watch **Online Learning for Latent Dirichlet Allocation** in NIPS2010 by Matt Hoffman (right click and open in new tab)

[\[Matt Hoffman's NIPS 2010 Talk Online LDA\]](http://videolectures.net/nips2010_hoffman_oll/thumb.jpg)\](http://videolectures.net/nips2010\_hoffman\_oll/)

Also see the paper on *Online varioational Bayes* by Matt linked for more details (from the above URL): <http://videolectures.net/site/normal_dl/tag=83534/nips2010_1291.pdf>

Note that using the OnlineLDAOptimizer returns us a [LocalLDAModel](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.clustering.LocalLDAModel), which stores the inferred topics of your corpus.

Review Topics
-------------

We can now review the results of our LDA model. We will print out all 20 topics with their corresponding term probabilities.

Note that you will get slightly different results every time you run an LDA model since LDA includes some randomization.

Let us review results of LDA model with Online Variational Bayes, step by step.

``` scala
val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 5)
```

>     topicIndices: Array[(Array[Int], Array[Double])] = Array((Array(1, 2, 0, 3, 5),Array(8.870502165836592E-4, 8.455512122993715E-4, 8.405442657208639E-4, 6.880855929957882E-4, 5.718170942715108E-4)), (Array(475, 6, 5, 672, 0),Array(6.604426891774711E-4, 5.040660436137734E-4, 4.398374691261006E-4, 4.394813806219008E-4, 4.2124884357849405E-4)), (Array(1, 0, 711, 8, 2),Array(0.001033078459354409, 8.602757889173593E-4, 5.736854591682394E-4, 4.907935055077068E-4, 4.857868868648681E-4)), (Array(463, 39, 138, 2, 0),Array(9.760486027002046E-4, 7.356034976488707E-4, 7.239124233785576E-4, 7.23810524674968E-4, 5.752062465268072E-4)), (Array(0, 4, 1, 6, 2),Array(4.519796011823995E-4, 4.1953622649778193E-4, 4.12775128155724E-4, 3.458728146542328E-4, 3.4417362935726674E-4)), (Array(4, 6, 5, 7, 112),Array(7.281583115218771E-4, 6.173575420434051E-4, 5.446232863317875E-4, 4.8419324275020514E-4, 4.7159207102780664E-4)), (Array(0, 1, 356, 2, 513),Array(4.733034964697487E-4, 3.9544324530315245E-4, 3.686144262487459E-4, 3.6692320934368156E-4, 3.5964257068090593E-4)), (Array(0, 1, 2, 4, 6),Array(0.00592800973162625, 0.004882544703013895, 0.003764325993857157, 0.0031299607030077004, 0.003056956734480784)), (Array(125, 0, 127, 1, 8),Array(9.6182657015117E-4, 6.421674907378156E-4, 4.3644138936543223E-4, 4.334198225704832E-4, 4.199051551109174E-4)), (Array(12, 4, 179, 1711, 3),Array(9.05637947324869E-4, 5.996310336598451E-4, 5.297038396447216E-4, 5.036513321557304E-4, 5.02113185094225E-4)), (Array(495, 20, 314, 552, 305),Array(0.001125385578150669, 9.571574594610244E-4, 8.615406536855199E-4, 7.405556744120801E-4, 6.652329886892783E-4)), (Array(0, 1, 15, 4, 225),Array(7.516992175130391E-4, 6.140288885874541E-4, 5.829267356713463E-4, 5.747119979824571E-4, 5.721076159794019E-4)), (Array(1672, 3, 0, 125, 362),Array(4.112185981690008E-4, 3.456543685473538E-4, 2.8558687330567083E-4, 2.753438152548804E-4, 2.7250597198466017E-4)), (Array(4, 5, 7, 0, 19),Array(0.0025479403655068876, 0.0024817077030723574, 0.00247469647505834, 0.0024514541398175053, 0.002439414557165773)), (Array(715, 1073, 0, 1, 16),Array(0.0010745774609429903, 9.570648146951217E-4, 7.200954586489735E-4, 6.614204057102692E-4, 5.538498177277739E-4)), (Array(2, 0, 4, 5, 20),Array(6.192379369537874E-4, 5.772171307365455E-4, 5.667533047321806E-4, 5.415894886445497E-4, 5.267512395965651E-4)), (Array(0, 1, 34, 2, 3),Array(7.200204681500483E-4, 5.961373118464498E-4, 5.493385406954403E-4, 5.459100014498796E-4, 4.896440669427867E-4)), (Array(141, 41, 1770, 3, 1),Array(8.615828269507997E-4, 8.13747388229561E-4, 6.053742808247455E-4, 5.739795414673994E-4, 5.64535148183101E-4)), (Array(29, 0, 35, 6, 72),Array(3.995318759378626E-4, 3.429095989923849E-4, 3.2859227732403895E-4, 3.132514176735476E-4, 3.05024146550321E-4)), (Array(2, 12, 5, 27, 327),Array(6.291593429473075E-4, 4.625230309067342E-4, 4.077659924037465E-4, 3.8565083284508854E-4, 3.8403791764251704E-4)))

``` scala
val vocabList = vectorizer.vocabulary
```

>     vocabList: Array[String] = Array(writes, article, people, just, know, like, think, does, time, good, make, used, windows, want, work, right, problem, need, really, image, said, data, going, information, better, believe, using, software, years, year, mail, sure, point, thanks, drive, program, available, space, file, power, help, government, things, question, doesn, number, case, world, look, read, line, version, come, thing, long, different, jpeg, best, fact, university, real, probably, didn, course, true, state, files, high, possible, actually, 1993, list, game, little, news, group, david, send, tell, wrong, graphics, based, support, able, place, free, called, subject, post, john, reason, color, second, great, card, having, public, email, info, following, start, hard, science, says, example, means, code, evidence, person, maybe, note, general, president, heard, quite, problems, mean, source, systems, life, price, order, window, standard, access, jesus, claim, paul, getting, looking, control, trying, disk, seen, simply, times, book, team, play, chip, local, encryption, idea, truth, opinions, issue, given, research, church, images, wrote, display, large, makes, remember, thought, national, doing, format, away, nasa, change, human, home, saying, small, mark, interested, current, internet, today, area, word, original, agree, left, memory, machine, works, microsoft, instead, working, hardware, kind, request, higher, sort, programs, questions, money, entry, later, israel, mike, pretty, hand, guess, include, netcom, address, technology, matter, cause, uiuc, type, video, speed, wire, days, server, usually, view, april, open, package, earth, stuff, unless, christian, told, important, similar, house, major, size, faith, known, provide, phone, body, michael, rights, ground, health, american, apple, feel, including, center, answer, bible, user, cost, text, lines, understand, check, anybody, security, mind, care, copy, wouldn, live, started, certainly, network, women, level, mouse, running, message, study, clinton, making, position, company, came, board, screen, groups, talking, single, common, white, test, wiring, christians, monitor, likely, black, special, quality, light, effect, nice, medical, members, certain, hope, sources, uucp, posted, canada, fine, hear, cars, write, clear, difference, police, love, history, couple, build, launch, press, situation, books, jewish, specific, sense, words, particular, anti, stop, posting, unix, talk, model, religion, discussion, school, contact, private, frank, turkish, keys, built, cable, taking, simple, legal, sound, consider, features, service, short, date, night, reference, argument, tools, comes, children, application, comments, device, scsi, clipper, applications, jews, doubt, tried, force, process, theory, objective, games, usenet, self, experience, steve, early, expect, needed, uses, tape, manager, interesting, station, killed, easy, value, death, exactly, turn, correct, response, needs, ones, according, amiga, drug, considered, language, reading, james, states, wanted, shuttle, goes, koresh, term, insurance, personal, strong, past, form, opinion, taken, result, future, sorry, mentioned, rules, especially, religious, hell, country, design, happy, went, society, plus, drivers, written, guns, various, author, haven, asked, results, analysis, gets, latest, longer, parts, advance, aren, previous, cases, york, laws, main, section, accept, input, looks, week, christ, weapons, required, mode, washington, community, robert, numbers, disease, head, fast, option, series, circuit, offer, macintosh, driver, office, israeli, range, exist, venus, andrew, period, clock, players, runs, values, department, moral, allow, organization, toronto, involved, knows, picture, colors, brian, sell, half, months, choice, dave, armenians, takes, currently, suggest, wasn, hockey, object, took, includes, individual, cards, federal, candida, policy, directly, total, title, protect, follow, americans, equipment, assume, close, food, purpose, recently, statement, present, devices, happened, deal, users, media, provides, happen, scientific, christianity, require, reasons, shall, dead, lost, action, speak, road, couldn, goal, bike, save, george, wants, city, details, california, mission, voice, useful, baseball, lead, obviously, completely, condition, complete, court, uunet, easily, terms, batf, engineering, league, responsible, administration, ways, international, compatible, sent, clearly, rest, algorithm, water, disclaimer, output, appreciated, freedom, digital, kill, issues, business, pass, hours, figure, error, fans, newsgroup, coming, operating, average, project, deleted, context, processing, companies, story, trade, appropriate, events, leave, port, berkeley, carry, season, face, basis, final, requires, building, heart, performance, difficult, addition, convert, political, page, lower, environment, player, king, points, armenian, volume, actual, resolution, field, willing, knowledge, apply, related, stanford, suppose, site, sale, advice, commercial, sounds, worth, orbit, lots, claims, limited, defense, entries, basic, supposed, designed, explain, directory, anonymous, handle, inside, ability, included, signal, young, turkey, family, reply, enforcement, radio, necessary, programming, wonder, suspect, wait, changes, neutral, forget, services, shot, greek, month, create, installed, printer, paper, friend, thinking, understanding, homosexuality, natural, morality, russian, finally, land, formats, names, machines, report, peter, setting, population, hold, break, comment, homosexual, normal, interface, eric, miles, product, rutgers, logic, reasonable, arab, communications, comp, percent, escrow, avoid, room, east, supply, types, lives, colorado, secure, million, developed, peace, cancer, multiple, allowed, library, cubs, expensive, agencies, cheap, recent, gary, soon, event, gives, soviet, looked, mention, supported, technical, street, caused, physics, happens, suggestions, doctor, release, obvious, choose, development, print, generally, outside, treatment, entire, bitnet, radar, chance, mass, table, friends, return, archive, install, folks, morning, member, electrical, illegal, diet, ideas, exists, muslim, jack, meaning, united, wish, smith, trouble, weeks, areas, social, concept, requests, straight, child, learn, supports, behavior, stand, engine, bring, thank, worked, unit, reality, remove, asking, appear, provided, pick, studies, possibly, practice, answers, drives, attempt, motif, west, modem, henry, trust, bits, existence, changed, decided, near, middle, belief, compound, continue, errors, false, extra, guys, arguments, proper, congress, particularly, class, yeah, safe, facts, loss, contains, thread, function, manual, attack, fonts, aware, privacy, andy, pages, operations, appears, worse, heat, command, drugs, wide, stupid, nature, constitution, institute, frame, armenia, wall, distribution, approach, hands, speaking, unfortunately, conference, independent, edge, division, shouldn, knew, effective, serial, added, compression, safety, crime, shows, indiana, bought, 1990, turks, modern, civil, ethernet, solution, 1992, abortion, cramer, blood, blue, letter, plastic, spend, allows, hello, utility, rate, appreciate, regular, writing, floppy, wondering, virginia, germany, simms, gave, operation, record, internal, faster, arms, giving, views, switch, tool, decision, playing, step, atheism, additional, method, described, base, concerned, stated, surface, kids, played, articles, scott, actions, font, capability, places, products, attitude, costs, patients, prevent, controller, fair, rule, buying, late, quote, highly, military, considering, keith, resources, cover, levels, connected, north, hate, countries, excellent, poor, market, necessarily, wires, created, shell, western, america, valid, turned, apparently, brought, functions, account, received, creation, watch, majority, cwru, driving, released, authority, committee, chips, quick, forward, student, protection, calls, richard, boston, complex, visual, absolutely, sold, arizona, produce, notice, plan, moon, minutes, lord, arabs, properly, fairly, boxes, murder, keyboard, greatly, killing, vote, panel, rangers, options, shareware)

``` scala
val topics = topicIndices.map { case (terms, termWeights) =>
  terms.map(vocabList(_)).zip(termWeights)
}
```

>     topics: Array[Array[(String, Double)]] = Array(Array((article,8.870502165836592E-4), (people,8.455512122993715E-4), (writes,8.405442657208639E-4), (just,6.880855929957882E-4), (like,5.718170942715108E-4)), Array((picture,6.604426891774711E-4), (think,5.040660436137734E-4), (like,4.398374691261006E-4), (morality,4.394813806219008E-4), (writes,4.2124884357849405E-4)), Array((article,0.001033078459354409), (writes,8.602757889173593E-4), (cancer,5.736854591682394E-4), (time,4.907935055077068E-4), (people,4.857868868648681E-4)), Array((period,9.760486027002046E-4), (power,7.356034976488707E-4), (play,7.239124233785576E-4), (people,7.23810524674968E-4), (writes,5.752062465268072E-4)), Array((writes,4.519796011823995E-4), (know,4.1953622649778193E-4), (article,4.12775128155724E-4), (think,3.458728146542328E-4), (people,3.4417362935726674E-4)), Array((know,7.281583115218771E-4), (think,6.173575420434051E-4), (like,5.446232863317875E-4), (does,4.8419324275020514E-4), (president,4.7159207102780664E-4)), Array((writes,4.733034964697487E-4), (article,3.9544324530315245E-4), (theory,3.686144262487459E-4), (people,3.6692320934368156E-4), (deal,3.5964257068090593E-4)), Array((writes,0.00592800973162625), (article,0.004882544703013895), (people,0.003764325993857157), (know,0.0031299607030077004), (think,0.003056956734480784)), Array((jesus,9.6182657015117E-4), (writes,6.421674907378156E-4), (paul,4.3644138936543223E-4), (article,4.334198225704832E-4), (time,4.199051551109174E-4)), Array((windows,9.05637947324869E-4), (know,5.996310336598451E-4), (microsoft,5.297038396447216E-4), (hospital,5.036513321557304E-4), (just,5.02113185094225E-4)), Array((candida,0.001125385578150669), (said,9.571574594610244E-4), (anti,8.615406536855199E-4), (league,7.405556744120801E-4), (launch,6.652329886892783E-4)), Array((writes,7.516992175130391E-4), (article,6.140288885874541E-4), (right,5.829267356713463E-4), (know,5.747119979824571E-4), (faith,5.721076159794019E-4)), Array((rendering,4.112185981690008E-4), (just,3.456543685473538E-4), (writes,2.8558687330567083E-4), (jesus,2.753438152548804E-4), (steve,2.7250597198466017E-4)), Array((know,0.0025479403655068876), (like,0.0024817077030723574), (does,0.00247469647505834), (writes,0.0024514541398175053), (image,0.002439414557165773)), Array((cubs,0.0010745774609429903), (suck,9.570648146951217E-4), (writes,7.200954586489735E-4), (article,6.614204057102692E-4), (problem,5.538498177277739E-4)), Array((people,6.192379369537874E-4), (writes,5.772171307365455E-4), (know,5.667533047321806E-4), (like,5.415894886445497E-4), (said,5.267512395965651E-4)), Array((writes,7.200204681500483E-4), (article,5.961373118464498E-4), (drive,5.493385406954403E-4), (people,5.459100014498796E-4), (just,4.896440669427867E-4)), Array((encryption,8.615828269507997E-4), (government,8.13747388229561E-4), (votes,6.053742808247455E-4), (just,5.739795414673994E-4), (article,5.64535148183101E-4)), Array((year,3.995318759378626E-4), (writes,3.429095989923849E-4), (program,3.2859227732403895E-4), (think,3.132514176735476E-4), (game,3.05024146550321E-4)), Array((people,6.291593429473075E-4), (windows,4.625230309067342E-4), (like,4.077659924037465E-4), (software,3.8565083284508854E-4), (keys,3.8403791764251704E-4)))

Feel free to take things apart to understand!

``` scala
topicIndices(0)
```

>     res47: (Array[Int], Array[Double]) = (Array(1, 2, 0, 3, 5),Array(8.870502165836592E-4, 8.455512122993715E-4, 8.405442657208639E-4, 6.880855929957882E-4, 5.718170942715108E-4))

``` scala
topicIndices(0)._1
```

>     res48: Array[Int] = Array(1, 2, 0, 3, 5)

``` scala
topicIndices(0)._1(0)
```

>     res49: Int = 1

``` scala
vocabList(topicIndices(0)._1(0))
```

>     res50: String = article

Review Results of LDA model with Online Variational Bayes - Doing all four steps earlier at once.

``` scala
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
```

>     20 topics:
>     TOPIC 0
>     article	8.870502165836592E-4
>     people	8.455512122993715E-4
>     writes	8.405442657208639E-4
>     just	6.880855929957882E-4
>     like	5.718170942715108E-4
>     ==========
>     TOPIC 1
>     picture	6.604426891774711E-4
>     think	5.040660436137734E-4
>     like	4.398374691261006E-4
>     morality	4.394813806219008E-4
>     writes	4.2124884357849405E-4
>     ==========
>     TOPIC 2
>     article	0.001033078459354409
>     writes	8.602757889173593E-4
>     cancer	5.736854591682394E-4
>     time	4.907935055077068E-4
>     people	4.857868868648681E-4
>     ==========
>     TOPIC 3
>     period	9.760486027002046E-4
>     power	7.356034976488707E-4
>     play	7.239124233785576E-4
>     people	7.23810524674968E-4
>     writes	5.752062465268072E-4
>     ==========
>     TOPIC 4
>     writes	4.519796011823995E-4
>     know	4.1953622649778193E-4
>     article	4.12775128155724E-4
>     think	3.458728146542328E-4
>     people	3.4417362935726674E-4
>     ==========
>     TOPIC 5
>     know	7.281583115218771E-4
>     think	6.173575420434051E-4
>     like	5.446232863317875E-4
>     does	4.8419324275020514E-4
>     president	4.7159207102780664E-4
>     ==========
>     TOPIC 6
>     writes	4.733034964697487E-4
>     article	3.9544324530315245E-4
>     theory	3.686144262487459E-4
>     people	3.6692320934368156E-4
>     deal	3.5964257068090593E-4
>     ==========
>     TOPIC 7
>     writes	0.00592800973162625
>     article	0.004882544703013895
>     people	0.003764325993857157
>     know	0.0031299607030077004
>     think	0.003056956734480784
>     ==========
>     TOPIC 8
>     jesus	9.6182657015117E-4
>     writes	6.421674907378156E-4
>     paul	4.3644138936543223E-4
>     article	4.334198225704832E-4
>     time	4.199051551109174E-4
>     ==========
>     TOPIC 9
>     windows	9.05637947324869E-4
>     know	5.996310336598451E-4
>     microsoft	5.297038396447216E-4
>     hospital	5.036513321557304E-4
>     just	5.02113185094225E-4
>     ==========
>     TOPIC 10
>     candida	0.001125385578150669
>     said	9.571574594610244E-4
>     anti	8.615406536855199E-4
>     league	7.405556744120801E-4
>     launch	6.652329886892783E-4
>     ==========
>     TOPIC 11
>     writes	7.516992175130391E-4
>     article	6.140288885874541E-4
>     right	5.829267356713463E-4
>     know	5.747119979824571E-4
>     faith	5.721076159794019E-4
>     ==========
>     TOPIC 12
>     rendering	4.112185981690008E-4
>     just	3.456543685473538E-4
>     writes	2.8558687330567083E-4
>     jesus	2.753438152548804E-4
>     steve	2.7250597198466017E-4
>     ==========
>     TOPIC 13
>     know	0.0025479403655068876
>     like	0.0024817077030723574
>     does	0.00247469647505834
>     writes	0.0024514541398175053
>     image	0.002439414557165773
>     ==========
>     TOPIC 14
>     cubs	0.0010745774609429903
>     suck	9.570648146951217E-4
>     writes	7.200954586489735E-4
>     article	6.614204057102692E-4
>     problem	5.538498177277739E-4
>     ==========
>     TOPIC 15
>     people	6.192379369537874E-4
>     writes	5.772171307365455E-4
>     know	5.667533047321806E-4
>     like	5.415894886445497E-4
>     said	5.267512395965651E-4
>     ==========
>     TOPIC 16
>     writes	7.200204681500483E-4
>     article	5.961373118464498E-4
>     drive	5.493385406954403E-4
>     people	5.459100014498796E-4
>     just	4.896440669427867E-4
>     ==========
>     TOPIC 17
>     encryption	8.615828269507997E-4
>     government	8.13747388229561E-4
>     votes	6.053742808247455E-4
>     just	5.739795414673994E-4
>     article	5.64535148183101E-4
>     ==========
>     TOPIC 18
>     year	3.995318759378626E-4
>     writes	3.429095989923849E-4
>     program	3.2859227732403895E-4
>     think	3.132514176735476E-4
>     game	3.05024146550321E-4
>     ==========
>     TOPIC 19
>     people	6.291593429473075E-4
>     windows	4.625230309067342E-4
>     like	4.077659924037465E-4
>     software	3.8565083284508854E-4
>     keys	3.8403791764251704E-4
>     ==========
>     topicIndices: Array[(Array[Int], Array[Double])] = Array((Array(1, 2, 0, 3, 5),Array(8.870502165836592E-4, 8.455512122993715E-4, 8.405442657208639E-4, 6.880855929957882E-4, 5.718170942715108E-4)), (Array(475, 6, 5, 672, 0),Array(6.604426891774711E-4, 5.040660436137734E-4, 4.398374691261006E-4, 4.394813806219008E-4, 4.2124884357849405E-4)), (Array(1, 0, 711, 8, 2),Array(0.001033078459354409, 8.602757889173593E-4, 5.736854591682394E-4, 4.907935055077068E-4, 4.857868868648681E-4)), (Array(463, 39, 138, 2, 0),Array(9.760486027002046E-4, 7.356034976488707E-4, 7.239124233785576E-4, 7.23810524674968E-4, 5.752062465268072E-4)), (Array(0, 4, 1, 6, 2),Array(4.519796011823995E-4, 4.1953622649778193E-4, 4.12775128155724E-4, 3.458728146542328E-4, 3.4417362935726674E-4)), (Array(4, 6, 5, 7, 112),Array(7.281583115218771E-4, 6.173575420434051E-4, 5.446232863317875E-4, 4.8419324275020514E-4, 4.7159207102780664E-4)), (Array(0, 1, 356, 2, 513),Array(4.733034964697487E-4, 3.9544324530315245E-4, 3.686144262487459E-4, 3.6692320934368156E-4, 3.5964257068090593E-4)), (Array(0, 1, 2, 4, 6),Array(0.00592800973162625, 0.004882544703013895, 0.003764325993857157, 0.0031299607030077004, 0.003056956734480784)), (Array(125, 0, 127, 1, 8),Array(9.6182657015117E-4, 6.421674907378156E-4, 4.3644138936543223E-4, 4.334198225704832E-4, 4.199051551109174E-4)), (Array(12, 4, 179, 1711, 3),Array(9.05637947324869E-4, 5.996310336598451E-4, 5.297038396447216E-4, 5.036513321557304E-4, 5.02113185094225E-4)), (Array(495, 20, 314, 552, 305),Array(0.001125385578150669, 9.571574594610244E-4, 8.615406536855199E-4, 7.405556744120801E-4, 6.652329886892783E-4)), (Array(0, 1, 15, 4, 225),Array(7.516992175130391E-4, 6.140288885874541E-4, 5.829267356713463E-4, 5.747119979824571E-4, 5.721076159794019E-4)), (Array(1672, 3, 0, 125, 362),Array(4.112185981690008E-4, 3.456543685473538E-4, 2.8558687330567083E-4, 2.753438152548804E-4, 2.7250597198466017E-4)), (Array(4, 5, 7, 0, 19),Array(0.0025479403655068876, 0.0024817077030723574, 0.00247469647505834, 0.0024514541398175053, 0.002439414557165773)), (Array(715, 1073, 0, 1, 16),Array(0.0010745774609429903, 9.570648146951217E-4, 7.200954586489735E-4, 6.614204057102692E-4, 5.538498177277739E-4)), (Array(2, 0, 4, 5, 20),Array(6.192379369537874E-4, 5.772171307365455E-4, 5.667533047321806E-4, 5.415894886445497E-4, 5.267512395965651E-4)), (Array(0, 1, 34, 2, 3),Array(7.200204681500483E-4, 5.961373118464498E-4, 5.493385406954403E-4, 5.459100014498796E-4, 4.896440669427867E-4)), (Array(141, 41, 1770, 3, 1),Array(8.615828269507997E-4, 8.13747388229561E-4, 6.053742808247455E-4, 5.739795414673994E-4, 5.64535148183101E-4)), (Array(29, 0, 35, 6, 72),Array(3.995318759378626E-4, 3.429095989923849E-4, 3.2859227732403895E-4, 3.132514176735476E-4, 3.05024146550321E-4)), (Array(2, 12, 5, 27, 327),Array(6.291593429473075E-4, 4.625230309067342E-4, 4.077659924037465E-4, 3.8565083284508854E-4, 3.8403791764251704E-4)))
>     vocabList: Array[String] = Array(writes, article, people, just, know, like, think, does, time, good, make, used, windows, want, work, right, problem, need, really, image, said, data, going, information, better, believe, using, software, years, year, mail, sure, point, thanks, drive, program, available, space, file, power, help, government, things, question, doesn, number, case, world, look, read, line, version, come, thing, long, different, jpeg, best, fact, university, real, probably, didn, course, true, state, files, high, possible, actually, 1993, list, game, little, news, group, david, send, tell, wrong, graphics, based, support, able, place, free, called, subject, post, john, reason, color, second, great, card, having, public, email, info, following, start, hard, science, says, example, means, code, evidence, person, maybe, note, general, president, heard, quite, problems, mean, source, systems, life, price, order, window, standard, access, jesus, claim, paul, getting, looking, control, trying, disk, seen, simply, times, book, team, play, chip, local, encryption, idea, truth, opinions, issue, given, research, church, images, wrote, display, large, makes, remember, thought, national, doing, format, away, nasa, change, human, home, saying, small, mark, interested, current, internet, today, area, word, original, agree, left, memory, machine, works, microsoft, instead, working, hardware, kind, request, higher, sort, programs, questions, money, entry, later, israel, mike, pretty, hand, guess, include, netcom, address, technology, matter, cause, uiuc, type, video, speed, wire, days, server, usually, view, april, open, package, earth, stuff, unless, christian, told, important, similar, house, major, size, faith, known, provide, phone, body, michael, rights, ground, health, american, apple, feel, including, center, answer, bible, user, cost, text, lines, understand, check, anybody, security, mind, care, copy, wouldn, live, started, certainly, network, women, level, mouse, running, message, study, clinton, making, position, company, came, board, screen, groups, talking, single, common, white, test, wiring, christians, monitor, likely, black, special, quality, light, effect, nice, medical, members, certain, hope, sources, uucp, posted, canada, fine, hear, cars, write, clear, difference, police, love, history, couple, build, launch, press, situation, books, jewish, specific, sense, words, particular, anti, stop, posting, unix, talk, model, religion, discussion, school, contact, private, frank, turkish, keys, built, cable, taking, simple, legal, sound, consider, features, service, short, date, night, reference, argument, tools, comes, children, application, comments, device, scsi, clipper, applications, jews, doubt, tried, force, process, theory, objective, games, usenet, self, experience, steve, early, expect, needed, uses, tape, manager, interesting, station, killed, easy, value, death, exactly, turn, correct, response, needs, ones, according, amiga, drug, considered, language, reading, james, states, wanted, shuttle, goes, koresh, term, insurance, personal, strong, past, form, opinion, taken, result, future, sorry, mentioned, rules, especially, religious, hell, country, design, happy, went, society, plus, drivers, written, guns, various, author, haven, asked, results, analysis, gets, latest, longer, parts, advance, aren, previous, cases, york, laws, main, section, accept, input, looks, week, christ, weapons, required, mode, washington, community, robert, numbers, disease, head, fast, option, series, circuit, offer, macintosh, driver, office, israeli, range, exist, venus, andrew, period, clock, players, runs, values, department, moral, allow, organization, toronto, involved, knows, picture, colors, brian, sell, half, months, choice, dave, armenians, takes, currently, suggest, wasn, hockey, object, took, includes, individual, cards, federal, candida, policy, directly, total, title, protect, follow, americans, equipment, assume, close, food, purpose, recently, statement, present, devices, happened, deal, users, media, provides, happen, scientific, christianity, require, reasons, shall, dead, lost, action, speak, road, couldn, goal, bike, save, george, wants, city, details, california, mission, voice, useful, baseball, lead, obviously, completely, condition, complete, court, uunet, easily, terms, batf, engineering, league, responsible, administration, ways, international, compatible, sent, clearly, rest, algorithm, water, disclaimer, output, appreciated, freedom, digital, kill, issues, business, pass, hours, figure, error, fans, newsgroup, coming, operating, average, project, deleted, context, processing, companies, story, trade, appropriate, events, leave, port, berkeley, carry, season, face, basis, final, requires, building, heart, performance, difficult, addition, convert, political, page, lower, environment, player, king, points, armenian, volume, actual, resolution, field, willing, knowledge, apply, related, stanford, suppose, site, sale, advice, commercial, sounds, worth, orbit, lots, claims, limited, defense, entries, basic, supposed, designed, explain, directory, anonymous, handle, inside, ability, included, signal, young, turkey, family, reply, enforcement, radio, necessary, programming, wonder, suspect, wait, changes, neutral, forget, services, shot, greek, month, create, installed, printer, paper, friend, thinking, understanding, homosexuality, natural, morality, russian, finally, land, formats, names, machines, report, peter, setting, population, hold, break, comment, homosexual, normal, interface, eric, miles, product, rutgers, logic, reasonable, arab, communications, comp, percent, escrow, avoid, room, east, supply, types, lives, colorado, secure, million, developed, peace, cancer, multiple, allowed, library, cubs, expensive, agencies, cheap, recent, gary, soon, event, gives, soviet, looked, mention, supported, technical, street, caused, physics, happens, suggestions, doctor, release, obvious, choose, development, print, generally, outside, treatment, entire, bitnet, radar, chance, mass, table, friends, return, archive, install, folks, morning, member, electrical, illegal, diet, ideas, exists, muslim, jack, meaning, united, wish, smith, trouble, weeks, areas, social, concept, requests, straight, child, learn, supports, behavior, stand, engine, bring, thank, worked, unit, reality, remove, asking, appear, provided, pick, studies, possibly, practice, answers, drives, attempt, motif, west, modem, henry, trust, bits, existence, changed, decided, near, middle, belief, compound, continue, errors, false, extra, guys, arguments, proper, congress, particularly, class, yeah, safe, facts, loss, contains, thread, function, manual, attack, fonts, aware, privacy, andy, pages, operations, appears, worse, heat, command, drugs, wide, stupid, nature, constitution, institute, frame, armenia, wall, distribution, approach, hands, speaking, unfortunately, conference, independent, edge, division, shouldn, knew, effective, serial, added, compression, safety, crime, shows, indiana, bought, 1990, turks, modern, civil, ethernet, solution, 1992, abortion, cramer, blood, blue, letter, plastic, spend, allows, hello, utility, rate, appreciate, regular, writing, floppy, wondering, virginia, germany, simms, gave, operation, record, internal, faster, arms, giving, views, switch, tool, decision, playing, step, atheism, additional, method, described, base, concerned, stated, surface, kids, played, articles, scott, actions, font, capability, places, products, attitude, costs, patients, prevent, controller, fair, rule, buying, late, quote, highly, military, considering, keith, resources, cover, levels, connected, north, hate, countries, excellent, poor, market, necessarily, wires, created, shell, western, america, valid, turned, apparently, brought, functions, account, received, creation, watch, majority, cwru, driving, released, authority, committee, chips, quick, forward, student, protection, calls, richard, boston, complex, visual, absolutely, sold, arizona, produce, notice, plan, moon, minutes, lord, arabs, properly, fairly, boxes, murder, keyboard, greatly, killing, vote, panel, rangers, options, shareware)
>     topics: Array[Array[(String, Double)]] = Array(Array((article,8.870502165836592E-4), (people,8.455512122993715E-4), (writes,8.405442657208639E-4), (just,6.880855929957882E-4), (like,5.718170942715108E-4)), Array((picture,6.604426891774711E-4), (think,5.040660436137734E-4), (like,4.398374691261006E-4), (morality,4.394813806219008E-4), (writes,4.2124884357849405E-4)), Array((article,0.001033078459354409), (writes,8.602757889173593E-4), (cancer,5.736854591682394E-4), (time,4.907935055077068E-4), (people,4.857868868648681E-4)), Array((period,9.760486027002046E-4), (power,7.356034976488707E-4), (play,7.239124233785576E-4), (people,7.23810524674968E-4), (writes,5.752062465268072E-4)), Array((writes,4.519796011823995E-4), (know,4.1953622649778193E-4), (article,4.12775128155724E-4), (think,3.458728146542328E-4), (people,3.4417362935726674E-4)), Array((know,7.281583115218771E-4), (think,6.173575420434051E-4), (like,5.446232863317875E-4), (does,4.8419324275020514E-4), (president,4.7159207102780664E-4)), Array((writes,4.733034964697487E-4), (article,3.9544324530315245E-4), (theory,3.686144262487459E-4), (people,3.6692320934368156E-4), (deal,3.5964257068090593E-4)), Array((writes,0.00592800973162625), (article,0.004882544703013895), (people,0.003764325993857157), (know,0.0031299607030077004), (think,0.003056956734480784)), Array((jesus,9.6182657015117E-4), (writes,6.421674907378156E-4), (paul,4.3644138936543223E-4), (article,4.334198225704832E-4), (time,4.199051551109174E-4)), Array((windows,9.05637947324869E-4), (know,5.996310336598451E-4), (microsoft,5.297038396447216E-4), (hospital,5.036513321557304E-4), (just,5.02113185094225E-4)), Array((candida,0.001125385578150669), (said,9.571574594610244E-4), (anti,8.615406536855199E-4), (league,7.405556744120801E-4), (launch,6.652329886892783E-4)), Array((writes,7.516992175130391E-4), (article,6.140288885874541E-4), (right,5.829267356713463E-4), (know,5.747119979824571E-4), (faith,5.721076159794019E-4)), Array((rendering,4.112185981690008E-4), (just,3.456543685473538E-4), (writes,2.8558687330567083E-4), (jesus,2.753438152548804E-4), (steve,2.7250597198466017E-4)), Array((know,0.0025479403655068876), (like,0.0024817077030723574), (does,0.00247469647505834), (writes,0.0024514541398175053), (image,0.002439414557165773)), Array((cubs,0.0010745774609429903), (suck,9.570648146951217E-4), (writes,7.200954586489735E-4), (article,6.614204057102692E-4), (problem,5.538498177277739E-4)), Array((people,6.192379369537874E-4), (writes,5.772171307365455E-4), (know,5.667533047321806E-4), (like,5.415894886445497E-4), (said,5.267512395965651E-4)), Array((writes,7.200204681500483E-4), (article,5.961373118464498E-4), (drive,5.493385406954403E-4), (people,5.459100014498796E-4), (just,4.896440669427867E-4)), Array((encryption,8.615828269507997E-4), (government,8.13747388229561E-4), (votes,6.053742808247455E-4), (just,5.739795414673994E-4), (article,5.64535148183101E-4)), Array((year,3.995318759378626E-4), (writes,3.429095989923849E-4), (program,3.2859227732403895E-4), (think,3.132514176735476E-4), (game,3.05024146550321E-4)), Array((people,6.291593429473075E-4), (windows,4.625230309067342E-4), (like,4.077659924037465E-4), (software,3.8565083284508854E-4), (keys,3.8403791764251704E-4)))

Going through the results, you may notice that some of the topic words returned are actually stopwords that are specific to our dataset (for eg: "writes", "article"...). Let's try improving our model.

Step 8. Model Tuning - Refilter Stopwords
-----------------------------------------

We will try to improve the results of our model by identifying some stopwords that are specific to our dataset. We will filter these stopwords out and rerun our LDA model to see if we get better results.

``` scala
val add_stopwords = Array("article", "writes", "entry", "date", "udel", "said", "tell", "think", "know", "just", "newsgroup", "line", "like", "does", "going", "make", "thanks")
```

>     add_stopwords: Array[String] = Array(article, writes, entry, date, udel, said, tell, think, know, just, newsgroup, line, like, does, going, make, thanks)

``` scala
// Combine newly identified stopwords to our exising list of stopwords
val new_stopwords = stopwords.union(add_stopwords)
```

>     new_stopwords: Array[String] = Array(a, about, above, across, after, afterwards, again, against, all, almost, alone, along, already, also, although, always, am, among, amongst, amoungst, amount, an, and, another, any, anyhow, anyone, anything, anyway, anywhere, are, around, as, at, back, be, became, because, become, becomes, becoming, been, before, beforehand, behind, being, below, beside, besides, between, beyond, bill, both, bottom, but, by, call, can, cannot, cant, co, computer, con, could, couldnt, cry, de, describe, detail, do, done, down, due, during, each, eg, eight, either, eleven, else, elsewhere, empty, enough, etc, even, ever, every, everyone, everything, everywhere, except, few, fifteen, fify, fill, find, fire, first, five, for, former, formerly, forty, found, four, from, front, full, further, get, give, go, had, has, hasnt, have, he, hence, her, here, hereafter, hereby, herein, hereupon, hers, herself, him, himself, his, how, however, hundred, i, ie, if, in, inc, indeed, interest, into, is, it, its, itself, keep, last, latter, latterly, least, less, ltd, made, many, may, me, meanwhile, might, mill, mine, more, moreover, most, mostly, move, much, must, my, myself, name, namely, neither, never, nevertheless, next, nine, no, nobody, none, noone, nor, not, nothing, now, nowhere, of, off, often, on, once, one, only, onto, or, other, others, otherwise, our, ours, ourselves, out, over, own, part, per, perhaps, please, put, rather, re, same, see, seem, seemed, seeming, seems, serious, several, she, should, show, side, since, sincere, six, sixty, so, some, somehow, someone, something, sometime, sometimes, somewhere, still, such, system, take, ten, than, that, the, their, them, themselves, then, thence, there, thereafter, thereby, therefore, therein, thereupon, these, they, thick, thin, third, this, those, though, three, through, throughout, thru, thus, to, together, too, top, toward, towards, twelve, twenty, two, un, under, until, up, upon, us, very, via, was, we, well, were, what, whatever, when, whence, whenever, where, whereafter, whereas, whereby, wherein, whereupon, wherever, whether, which, while, whither, who, whoever, whole, whom, whose, why, will, with, within, without, would, yet, you, your, yours, yourself, yourselves, article, writes, entry, date, udel, said, tell, think, know, just, newsgroup, line, like, does, going, make, thanks)

``` scala
import org.apache.spark.ml.feature.StopWordsRemover

// Set Params for StopWordsRemover with new_stopwords
val remover = new StopWordsRemover()
.setStopWords(new_stopwords)
.setInputCol("tokens")
.setOutputCol("filtered")

// Create new df with new list of stopwords removed
val new_filtered_df = remover.transform(tokenized_df)
```

>     import org.apache.spark.ml.feature.StopWordsRemover
>     remover: org.apache.spark.ml.feature.StopWordsRemover = stopWords_aa51b35b1c3f
>     new_filtered_df: org.apache.spark.sql.DataFrame = [corpus: string, id: bigint ... 2 more fields]

``` scala
// Set Params for CountVectorizer
val vectorizer = new CountVectorizer()
.setInputCol("filtered")
.setOutputCol("features")
.setVocabSize(10000)
.setMinDF(5)
.fit(new_filtered_df)

// Create new df of countVectors
val new_countVectors = vectorizer.transform(new_filtered_df).select("id", "features")
```

>     vectorizer: org.apache.spark.ml.feature.CountVectorizerModel = cntVec_40ff31b7e1d6
>     new_countVectors: org.apache.spark.sql.DataFrame = [id: bigint, features: vector]

``` scala
// Convert DF to RDD
val new_lda_countVector = new_countVectors.map { case Row(id: Long, countVector: Vector) => (id, countVector) }
```

>     new_lda_countVector: org.apache.spark.sql.Dataset[(Long, org.apache.spark.ml.linalg.Vector)] = [_1: bigint, _2: vector]

We will also increase MaxIterations to 10 to see if we get better results.

``` scala
// Set LDA parameters

val new_lda = new LDA()
.setOptimizer(new OnlineLDAOptimizer().setMiniBatchFraction(0.8))
.setK(numTopics)
.setMaxIterations(10) // more than 3 this time
.setDocConcentration(-1) // use default values
.setTopicConcentration(-1) // use default values
```

>     new_lda: org.apache.spark.mllib.clustering.LDA = org.apache.spark.mllib.clustering.LDA@12b6214

#### How to find what the default values are?

Dive into the source!!!

1.  Let's find the default value for `docConcentration` now.
2.  Got to Apache Spark package Root: <https://spark.apache.org/docs/latest/api/scala/#package>

-   search for 'ml' in the search box on the top left (ml is for ml library)
-   Then find the `LDA` by scrolling below on the left to mllib's `clustering` methods and click on `LDA`
-   Then click on the source code link which should take you here:
-   <https://github.com/apache/spark/blob/v2.2.0/mllib/src/main/scala/org/apache/spark/ml/clustering/LDA.scala>
-   Now, simply go to the right function and see the following comment block:

`/**    * Concentration parameter (commonly named "alpha") for the prior placed on documents'    * distributions over topics ("theta").    *    * This is the parameter to a Dirichlet distribution, where larger values mean more smoothing    * (more regularization).    *    * If not set by the user, then docConcentration is set automatically. If set to    * singleton vector [alpha], then alpha is replicated to a vector of length k in fitting.    * Otherwise, the [[docConcentration]] vector must be length k.    * (default = automatic)    *    * Optimizer-specific parameter settings:    *  - EM    *     - Currently only supports symmetric distributions, so all values in the vector should be    *       the same.    *     - Values should be > 1.0    *     - default = uniformly (50 / k) + 1, where 50/k is common in LDA libraries and +1 follows    *       from Asuncion et al. (2009), who recommend a +1 adjustment for EM.    *  - Online    *     - Values should be >= 0    *     - default = uniformly (1.0 / k), following the implementation from    *       [[https://github.com/Blei-Lab/onlineldavb]].    * @group param    */`

**HOMEWORK:** Try to find the default value for `TopicConcentration`.

``` scala
// convert ML vectors into MLlib vectors
val new_lda_countVector_mllib = new_lda_countVector.map { case (id, vector) => (id, org.apache.spark.mllib.linalg.Vectors.fromML(vector)) }.rdd

// Create LDA model with stopwords refiltered
val new_ldaModel = new_lda.run(new_lda_countVector_mllib)
```

>     new_lda_countVector_mllib: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.linalg.Vector)] = MapPartitionsRDD[46211] at rdd at <console>:54
>     new_ldaModel: org.apache.spark.mllib.clustering.LDAModel = org.apache.spark.mllib.clustering.LocalLDAModel@30380931

``` scala
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
```

>     20 topics:
>     TOPIC 0
>     objective	0.001573716137649793
>     physics	8.171842354064342E-4
>     mechanics	6.468028341940202E-4
>     play	6.30430833245285E-4
>     quantum	6.052223224756606E-4
>     ==========
>     TOPIC 1
>     image	0.00914666218125134
>     jpeg	0.0075959481279362666
>     file	0.004508462879354402
>     software	0.004003887418040551
>     data	0.0038039453160901887
>     ==========
>     TOPIC 2
>     april	9.653866412130766E-4
>     candida	7.868411016842201E-4
>     canada	6.164903461317084E-4
>     germany	6.051836064730708E-4
>     italy	5.945993174087172E-4
>     ==========
>     TOPIC 3
>     windows	0.005395120382532377
>     drive	0.0035203237900216186
>     used	0.003406322883592858
>     space	0.0033646378018193724
>     program	0.0032964253242499983
>     ==========
>     TOPIC 4
>     people	0.007067275568414384
>     time	0.003974168254106632
>     good	0.0035205532522993247
>     right	0.002783251220886123
>     really	0.002626899243440512
>     ==========
>     TOPIC 5
>     motorola	4.731374734335182E-4
>     lines	4.0042795862774047E-4
>     problem	3.942931472938178E-4
>     view	3.470517340869767E-4
>     people	3.357539216927033E-4
>     ==========
>     TOPIC 6
>     women	9.616599985490336E-4
>     soldiers	9.543015296303855E-4
>     israeli	7.750002463531064E-4
>     time	7.533468519274011E-4
>     clothing	7.23058588237262E-4
>     ==========
>     TOPIC 7
>     period	0.0014974929784185948
>     play	0.0010199243008771443
>     power	0.0010003386708143643
>     scoring	6.727933500992175E-4
>     frank	6.711922434444065E-4
>     ==========
>     TOPIC 8
>     encryption	0.005768139805886623
>     government	0.0033239275059923435
>     chip	0.003317765159489825
>     clipper	0.0031522998324713092
>     security	0.0028634563295130257
>     ==========
>     TOPIC 9
>     cubs	0.0011330224543942302
>     suck	0.0010921758766806995
>     people	5.737892706658735E-4
>     armenian	5.152999402133025E-4
>     state	3.7551934597496335E-4
>     ==========
>     TOPIC 10
>     pitt	6.239584762766691E-4
>     wash	3.440870969425429E-4
>     round	3.192232648267741E-4
>     space	3.102735603099136E-4
>     claim	2.932571417643342E-4
>     ==========
>     TOPIC 11
>     josephus	9.62832203015188E-4
>     people	7.705681839358579E-4
>     venus	7.702331520932623E-4
>     jews	7.327024010963066E-4
>     jesus	7.252784082361398E-4
>     ==========
>     TOPIC 12
>     card	4.37701117713015E-4
>     good	4.213184453843586E-4
>     started	3.414808605945308E-4
>     claim	3.3625793278089806E-4
>     drivers	3.357296318777979E-4
>     ==========
>     TOPIC 13
>     candida	0.002064124770603748
>     bacteria	0.0010507132990003817
>     tract	7.520636682451422E-4
>     vaginal	7.515549968270531E-4
>     doctor	7.210658898649873E-4
>     ==========
>     TOPIC 14
>     requests	0.0014109518998149256
>     send	0.0011148741166598713
>     request	0.001071486206523694
>     working	9.997874598956563E-4
>     mail	5.804107666599937E-4
>     ==========
>     TOPIC 15
>     science	7.037700961274344E-4
>     rochester	5.840974050613598E-4
>     armenia	5.161608322954119E-4
>     people	5.053884641599707E-4
>     april	4.8492941594830997E-4
>     ==========
>     TOPIC 16
>     time	3.8461668503900773E-4
>     people	3.803993758712947E-4
>     window	3.3525991527632676E-4
>     request	3.2898565537773234E-4
>     drug	3.1339200070042026E-4
>     ==========
>     TOPIC 17
>     work	3.451410415928852E-4
>     people	3.232728419768109E-4
>     henry	3.12040888024746E-4
>     tape	3.019875897894189E-4
>     opinions	2.998250815002998E-4
>     ==========
>     TOPIC 18
>     people	0.0020950548628823986
>     abortion	0.0015802096951437314
>     insurance	9.687867340751481E-4
>     health	9.331075537515061E-4
>     evidence	8.412272143043274E-4
>     ==========
>     TOPIC 19
>     drive	0.002450368725654479
>     scsi	0.0012481597191364678
>     theory	0.0011652306102529447
>     temperature	7.823154892683314E-4
>     facts	6.032501970333372E-4
>     ==========
>     topicIndices: Array[(Array[Int], Array[Double])] = Array((Array(341, 712, 3259, 126, 1682),Array(0.001573716137649793, 8.171842354064342E-4, 6.468028341940202E-4, 6.30430833245285E-4, 6.052223224756606E-4)), (Array(11, 45, 27, 17, 12),Array(0.00914666218125134, 0.0075959481279362666, 0.004508462879354402, 0.004003887418040551, 0.0038039453160901887)), (Array(199, 481, 279, 878, 3140),Array(9.653866412130766E-4, 7.868411016842201E-4, 6.164903461317084E-4, 6.051836064730708E-4, 5.945993174087172E-4)), (Array(4, 23, 3, 26, 24),Array(0.005395120382532377, 0.0035203237900216186, 0.003406322883592858, 0.0033646378018193724, 0.0032964253242499983)), (Array(0, 1, 2, 7, 10),Array(0.007067275568414384, 0.003974168254106632, 0.0035205532522993247, 0.002783251220886123, 0.002626899243440512)), (Array(1973, 230, 8, 196, 0),Array(4.731374734335182E-4, 4.0042795862774047E-4, 3.942931472938178E-4, 3.470517340869767E-4, 3.357539216927033E-4)), (Array(244, 1105, 448, 1, 2958),Array(9.616599985490336E-4, 9.543015296303855E-4, 7.750002463531064E-4, 7.533468519274011E-4, 7.23058588237262E-4)), (Array(445, 126, 28, 2039, 313),Array(0.0014974929784185948, 0.0010199243008771443, 0.0010003386708143643, 6.727933500992175E-4, 6.711922434444065E-4)), (Array(128, 30, 125, 336, 234),Array(0.005768139805886623, 0.0033239275059923435, 0.003317765159489825, 0.0031522998324713092, 0.0028634563295130257)), (Array(702, 1069, 0, 593, 53),Array(0.0011330224543942302, 0.0010921758766806995, 5.737892706658735E-4, 5.152999402133025E-4, 3.7551934597496335E-4)), (Array(1670, 4286, 1010, 26, 112),Array(6.239584762766691E-4, 3.440870969425429E-4, 3.192232648267741E-4, 3.102735603099136E-4, 2.932571417643342E-4)), (Array(2846, 0, 447, 334, 114),Array(9.62832203015188E-4, 7.705681839358579E-4, 7.702331520932623E-4, 7.327024010963066E-4, 7.252784082361398E-4)), (Array(81, 2, 241, 112, 404),Array(4.37701117713015E-4, 4.213184453843586E-4, 3.414808605945308E-4, 3.3625793278089806E-4, 3.357296318777979E-4)), (Array(481, 1732, 2248, 2570, 714),Array(0.002064124770603748, 0.0010507132990003817, 7.520636682451422E-4, 7.515549968270531E-4, 7.210658898649873E-4)), (Array(759, 65, 171, 170, 20),Array(0.0014109518998149256, 0.0011148741166598713, 0.001071486206523694, 9.997874598956563E-4, 5.804107666599937E-4)), (Array(89, 1633, 828, 0, 199),Array(7.037700961274344E-4, 5.840974050613598E-4, 5.161608322954119E-4, 5.053884641599707E-4, 4.8492941594830997E-4)), (Array(1, 0, 109, 171, 372),Array(3.8461668503900773E-4, 3.803993758712947E-4, 3.3525991527632676E-4, 3.2898565537773234E-4, 3.1339200070042026E-4)), (Array(6, 0, 788, 350, 135),Array(3.451410415928852E-4, 3.232728419768109E-4, 3.12040888024746E-4, 3.019875897894189E-4, 2.998250815002998E-4)), (Array(0, 866, 377, 219, 94),Array(0.0020950548628823986, 0.0015802096951437314, 9.687867340751481E-4, 9.331075537515061E-4, 8.412272143043274E-4)), (Array(23, 333, 340, 1310, 804),Array(0.002450368725654479, 0.0012481597191364678, 0.0011652306102529447, 7.823154892683314E-4, 6.032501970333372E-4)))
>     vocabList: Array[String] = Array(people, time, good, used, windows, want, work, right, problem, need, really, image, data, information, better, believe, using, software, years, year, mail, sure, point, drive, program, available, space, file, power, help, government, things, question, doesn, number, case, world, look, read, version, come, thing, long, different, best, jpeg, fact, university, real, probably, didn, true, course, state, files, high, possible, actually, 1993, list, game, little, news, group, david, send, wrong, based, graphics, able, support, place, free, called, subject, post, john, reason, color, great, second, card, public, having, email, info, following, start, hard, science, says, example, means, code, evidence, person, note, maybe, president, heard, general, problems, quite, mean, source, systems, life, price, order, window, standard, access, claim, paul, jesus, getting, looking, trying, control, simply, disk, seen, times, book, team, chip, play, local, encryption, idea, truth, given, church, issue, research, opinions, images, wrote, display, large, makes, remember, thought, national, doing, format, away, nasa, change, human, home, small, saying, interested, current, mark, area, internet, today, word, original, agree, left, memory, works, machine, microsoft, instead, hardware, kind, working, request, sort, higher, programs, money, later, questions, israel, mike, guess, hand, pretty, include, netcom, address, cause, matter, technology, uiuc, speed, wire, video, type, days, server, view, usually, open, april, earth, package, stuff, unless, told, christian, important, similar, house, size, major, faith, provide, known, phone, body, ground, rights, michael, health, american, apple, feel, center, including, bible, answer, cost, text, user, lines, understand, check, anybody, security, mind, care, copy, wouldn, live, certainly, started, level, network, women, running, message, mouse, study, clinton, making, position, company, came, groups, board, screen, talking, single, white, common, monitor, wiring, test, likely, christians, special, quality, black, light, nice, effect, members, medical, hope, sources, certain, posted, uucp, canada, fine, hear, clear, difference, cars, write, build, police, love, history, couple, press, launch, situation, books, jewish, specific, sense, particular, words, anti, stop, posting, unix, talk, model, religion, discussion, school, contact, private, turkish, keys, frank, cable, built, taking, simple, consider, service, sound, features, legal, short, night, comes, reference, argument, tools, children, device, application, comments, scsi, jews, applications, clipper, doubt, tried, process, theory, objective, force, self, experience, usenet, games, early, expect, steve, tape, uses, needed, interesting, station, exactly, easy, death, killed, value, turn, manager, ones, response, correct, according, needs, amiga, wanted, language, shuttle, states, drug, james, considered, reading, koresh, insurance, personal, term, strong, goes, past, form, opinion, result, future, taken, especially, religious, sorry, mentioned, rules, hell, went, country, design, plus, happy, society, written, various, author, guns, drivers, results, analysis, haven, asked, longer, gets, latest, parts, aren, advance, main, section, laws, previous, cases, york, input, looks, week, accept, weapons, christ, mode, required, washington, community, disease, robert, fast, numbers, head, option, series, circuit, offer, macintosh, driver, office, exist, andrew, period, range, venus, israeli, clock, players, department, runs, values, moral, allow, organization, involved, toronto, colors, knows, picture, sell, brian, months, choice, half, currently, suggest, dave, armenians, takes, object, took, cards, includes, federal, hockey, individual, wasn, directly, candida, title, policy, total, protect, follow, americans, purpose, assume, close, recently, equipment, food, devices, happened, statement, present, happen, users, media, provides, deal, require, reasons, scientific, christianity, shall, dead, lost, action, speak, road, wants, city, george, goal, couldn, bike, save, obviously, details, completely, baseball, california, voice, mission, useful, lead, uunet, terms, batf, court, condition, easily, league, complete, engineering, clearly, administration, ways, compatible, international, sent, rest, responsible, disclaimer, output, water, algorithm, digital, business, appreciated, issues, freedom, kill, pass, hours, figure, error, fans, project, deleted, companies, coming, operating, average, processing, context, story, trade, season, face, port, carry, events, appropriate, leave, berkeley, basis, final, requires, heart, addition, performance, building, difficult, lower, player, king, page, convert, environment, armenian, political, points, resolution, field, willing, volume, actual, apply, knowledge, site, sale, suppose, related, stanford, worth, orbit, lots, basic, defense, advice, commercial, sounds, entries, limited, designed, explain, anonymous, supposed, directory, claims, enforcement, turkey, reply, inside, family, ability, changes, handle, young, included, signal, wonder, suspect, radio, neutral, forget, wait, necessary, programming, paper, month, greek, friend, installed, create, thinking, printer, shot, services, understanding, homosexuality, natural, morality, finally, land, russian, setting, formats, names, peter, machines, report, population, hold, break, interface, comment, normal, eric, homosexual, arab, logic, miles, reasonable, rutgers, east, supply, comp, percent, avoid, product, lives, colorado, communications, room, escrow, types, secure, developed, multiple, peace, cancer, million, allowed, gary, soon, agencies, recent, cubs, library, expensive, cheap, supported, event, looked, gives, soviet, mention, physics, suggestions, doctor, caused, technical, happens, obvious, street, release, table, choose, entire, outside, mass, return, radar, archive, chance, treatment, bitnet, print, install, generally, development, friends, folks, jack, concept, meaning, weeks, united, social, wish, child, smith, straight, learn, supports, behavior, ideas, morning, member, diet, trouble, electrical, illegal, exists, requests, muslim, areas, drives, west, unit, remove, reality, engine, worked, stand, appear, provided, studies, motif, attempt, possibly, answers, asking, pick, practice, bring, thank, changed, decided, modem, bits, near, existence, henry, trust, compound, errors, false, belief, continue, middle, class, congress, guys, extra, particularly, arguments, proper, safe, facts, loss, yeah, contains, stupid, appears, thread, pages, function, andy, attack, fonts, manual, privacy, aware, operations, heat, worse, frame, command, drugs, wide, nature, institute, armenia, constitution, wall, distribution, approach, speaking, independent, unfortunately, hands, conference, knew, effective, edge, division, shouldn, modern, solution, compression, safety, crime, serial, added, indiana, shows, ethernet, turks, civil, bought, 1992, 1990, floppy, appreciate, blue, letter, plastic, regular, writing, allows, abortion, utility, hello, cramer, rate, blood, spend, faster, simms, operation, arms, internal, germany, gave, record, wondering, virginia, method, giving, step, views, decision, switch, tool, playing, actions, articles, font, additional, described, concerned, scott, played, stated, kids, atheism, surface, base, keith, levels, buying, places, capability, products, attitude, costs, patients, controller, quote, fair, late, prevent, rule, cover, considering, highly, resources, north, military, connected, released, valid, hate, shell, western, excellent, countries, market, necessarily, poor, created, wires, america, apparently, turned, brought, functions, received, account, creation, watch, cwru, majority, forward, student, driving, authority, committee, protection, richard, boston, quick, calls, chips, fairly, keyboard, arizona, complex, visual, absolutely, notice, produce, sold, minutes, murder, boxes, lord, properly, plan, moon, arabs, islam, vote, panel, rangers, effort, options, dangerous, greatly, holy, killing, review, shareware, larry, begin, property, damage, electronics, living, failed, acts, tests, nation, intelligence)
>     topics: Array[Array[(String, Double)]] = Array(Array((objective,0.001573716137649793), (physics,8.171842354064342E-4), (mechanics,6.468028341940202E-4), (play,6.30430833245285E-4), (quantum,6.052223224756606E-4)), Array((image,0.00914666218125134), (jpeg,0.0075959481279362666), (file,0.004508462879354402), (software,0.004003887418040551), (data,0.0038039453160901887)), Array((april,9.653866412130766E-4), (candida,7.868411016842201E-4), (canada,6.164903461317084E-4), (germany,6.051836064730708E-4), (italy,5.945993174087172E-4)), Array((windows,0.005395120382532377), (drive,0.0035203237900216186), (used,0.003406322883592858), (space,0.0033646378018193724), (program,0.0032964253242499983)), Array((people,0.007067275568414384), (time,0.003974168254106632), (good,0.0035205532522993247), (right,0.002783251220886123), (really,0.002626899243440512)), Array((motorola,4.731374734335182E-4), (lines,4.0042795862774047E-4), (problem,3.942931472938178E-4), (view,3.470517340869767E-4), (people,3.357539216927033E-4)), Array((women,9.616599985490336E-4), (soldiers,9.543015296303855E-4), (israeli,7.750002463531064E-4), (time,7.533468519274011E-4), (clothing,7.23058588237262E-4)), Array((period,0.0014974929784185948), (play,0.0010199243008771443), (power,0.0010003386708143643), (scoring,6.727933500992175E-4), (frank,6.711922434444065E-4)), Array((encryption,0.005768139805886623), (government,0.0033239275059923435), (chip,0.003317765159489825), (clipper,0.0031522998324713092), (security,0.0028634563295130257)), Array((cubs,0.0011330224543942302), (suck,0.0010921758766806995), (people,5.737892706658735E-4), (armenian,5.152999402133025E-4), (state,3.7551934597496335E-4)), Array((pitt,6.239584762766691E-4), (wash,3.440870969425429E-4), (round,3.192232648267741E-4), (space,3.102735603099136E-4), (claim,2.932571417643342E-4)), Array((josephus,9.62832203015188E-4), (people,7.705681839358579E-4), (venus,7.702331520932623E-4), (jews,7.327024010963066E-4), (jesus,7.252784082361398E-4)), Array((card,4.37701117713015E-4), (good,4.213184453843586E-4), (started,3.414808605945308E-4), (claim,3.3625793278089806E-4), (drivers,3.357296318777979E-4)), Array((candida,0.002064124770603748), (bacteria,0.0010507132990003817), (tract,7.520636682451422E-4), (vaginal,7.515549968270531E-4), (doctor,7.210658898649873E-4)), Array((requests,0.0014109518998149256), (send,0.0011148741166598713), (request,0.001071486206523694), (working,9.997874598956563E-4), (mail,5.804107666599937E-4)), Array((science,7.037700961274344E-4), (rochester,5.840974050613598E-4), (armenia,5.161608322954119E-4), (people,5.053884641599707E-4), (april,4.8492941594830997E-4)), Array((time,3.8461668503900773E-4), (people,3.803993758712947E-4), (window,3.3525991527632676E-4), (request,3.2898565537773234E-4), (drug,3.1339200070042026E-4)), Array((work,3.451410415928852E-4), (people,3.232728419768109E-4), (henry,3.12040888024746E-4), (tape,3.019875897894189E-4), (opinions,2.998250815002998E-4)), Array((people,0.0020950548628823986), (abortion,0.0015802096951437314), (insurance,9.687867340751481E-4), (health,9.331075537515061E-4), (evidence,8.412272143043274E-4)), Array((drive,0.002450368725654479), (scsi,0.0012481597191364678), (theory,0.0011652306102529447), (temperature,7.823154892683314E-4), (facts,6.032501970333372E-4)))

We managed to get better results here. We can easily infer that topic 3 is about space, topic 7 is about religion, etc.

    ==========
    TOPIC 3
    station	0.0022184815200582244
    launch	0.0020621309179376145
    shuttle	0.0019305627762549198
    space	0.0017600147075534092
    redesign	0.0014972130065346592
    ==========
    TOPIC 7
    people	0.0038165245379908675
    church	0.0036902650900400543
    jesus	0.0029942866750178893
    paul	0.0026144777524277044
    bible	0.0020476251853453016
    ==========

Step 9. Create LDA model with Expectation Maximization
------------------------------------------------------

Let's try creating an LDA model with Expectation Maximization on the data that has been refiltered for additional stopwords. We will also increase MaxIterations here to 100 to see if that improves results.

See <http://spark.apache.org/docs/latest/mllib-clustering.html#latent-dirichlet-allocation-lda>

``` scala
import org.apache.spark.mllib.clustering.EMLDAOptimizer

// Set LDA parameters
val em_lda = new LDA()
.setOptimizer(new EMLDAOptimizer())
.setK(numTopics)
.setMaxIterations(100)
.setDocConcentration(-1) // use default values
.setTopicConcentration(-1) // use default values
```

>     import org.apache.spark.mllib.clustering.EMLDAOptimizer
>     em_lda: org.apache.spark.mllib.clustering.LDA = org.apache.spark.mllib.clustering.LDA@5aeae61e

``` scala
val em_ldaModel = em_lda.run(new_lda_countVector_mllib)
```

>     em_ldaModel: org.apache.spark.mllib.clustering.LDAModel = org.apache.spark.mllib.clustering.DistributedLDAModel@4cb239da

Note that the EMLDAOptimizer produces a DistributedLDAModel, which stores not only the inferred topics but also the full training corpus and topic distributions for each document in the training corpus.

``` scala
val topicIndices = em_ldaModel.describeTopics(maxTermsPerTopic = 5)
```

>     topicIndices: Array[(Array[Int], Array[Double])] = Array((Array(23, 8, 81, 268, 192),Array(0.03696568415933789, 0.01629861090697588, 0.01433934010187806, 0.013707091530346492, 0.012972406811610632)), (Array(59, 31, 63, 20, 0),Array(0.016273251673940115, 0.013336093829266985, 0.011820421026993289, 0.010256327131358182, 0.009196940048172493)), (Array(5, 10, 184, 32, 1),Array(0.020875510622723917, 0.01597476209980187, 0.014524465702206254, 0.012746020817368222, 0.012666327258860768)), (Array(191, 264, 75, 214, 28),Array(0.014438050303754668, 0.012299617198701644, 0.012274987249971222, 0.012021448245551786, 0.009432038562323322)), (Array(128, 30, 126, 188, 332),Array(0.01826967988745936, 0.014064779884001608, 0.014059305626418857, 0.011287261911973878, 0.01076836731761516)), (Array(11, 45, 12, 27, 25),Array(0.02791131528914355, 0.017670605280479763, 0.014293908441743292, 0.013060907975843267, 0.011622390744664425)), (Array(98, 50, 1, 249, 176),Array(0.017859677939407154, 0.013614159584111107, 0.012347029991677042, 0.009190778360916498, 0.008859507080584084)), (Array(312, 36, 478, 65, 0),Array(0.011319396952828511, 0.009962877409195969, 0.008655676983870219, 0.00855725322313576, 0.007875034876943812)), (Array(26, 287, 147, 58, 443),Array(0.01727602824412315, 0.009510894679631877, 0.008174059835266893, 0.008021957117505637, 0.007554628674289004)), (Array(0, 30, 241, 377, 397),Array(0.01981226804171339, 0.011487664126618446, 0.008584553138876285, 0.008175885706453272, 0.007377072709520659)), (Array(4, 110, 17, 165, 69),Array(0.05626351594827812, 0.01973702915921722, 0.019425647730930192, 0.01829576253383801, 0.017092560057940024)), (Array(24, 85, 54, 27, 34),Array(0.02008913356682715, 0.015316236531297255, 0.011736648109897512, 0.011002859969161052, 0.010888936862946987)), (Array(178, 0, 444, 297, 330),Array(0.01655912523831529, 0.013839621872149849, 0.009740304134451903, 0.009293757920083023, 0.007738906079536397)), (Array(216, 57, 157, 74, 217),Array(0.012762222513670877, 0.012512033178943538, 0.010850406228207507, 0.009306501754714605, 0.008689054789025402)), (Array(273, 0, 218, 484, 186),Array(0.010062044101355121, 0.009878164955234142, 0.009242293597826808, 0.008717534535587714, 0.008630197138270254)), (Array(60, 19, 124, 127, 14),Array(0.022763279394979432, 0.021047821614531215, 0.014616528925887282, 0.014260492376515463, 0.00926312978729905)), (Array(116, 2, 281, 10, 512),Array(0.01697525538431182, 0.015502092777115166, 0.014169299624261323, 0.010474401518068752, 0.008500864994223083)), (Array(8, 318, 9, 3, 70),Array(0.022253445216367452, 0.014029804886164775, 0.012570596158871844, 0.011722771786015752, 0.011108297878074287)), (Array(0, 114, 132, 130, 113),Array(0.012489074839478006, 0.012002740560764811, 0.010792066199830157, 0.010401308111128224, 0.008785091860728546)), (Array(155, 339, 275, 91, 0),Array(0.017392671209746132, 0.013053985125033755, 0.012399632872577494, 0.012118025863373018, 0.010567878803828654)))

``` scala
val vocabList = vectorizer.vocabulary
```

>     vocabList: Array[String] = Array(people, time, good, used, windows, want, work, right, problem, need, really, image, data, information, better, believe, using, software, years, year, mail, sure, point, drive, program, available, space, file, power, help, government, things, question, doesn, number, case, world, look, read, version, come, thing, different, long, best, jpeg, fact, university, probably, real, didn, course, state, true, files, high, possible, actually, 1993, list, game, little, news, group, david, send, wrong, based, graphics, support, able, place, called, free, john, subject, post, reason, color, great, second, card, public, having, email, info, following, start, hard, science, example, says, means, code, evidence, person, note, maybe, president, heard, general, mean, problems, quite, source, systems, life, price, standard, order, window, access, claim, paul, jesus, getting, looking, trying, control, disk, seen, simply, times, book, team, local, chip, play, encryption, idea, truth, given, church, issue, research, opinions, wrote, images, large, display, makes, remember, thought, doing, national, format, away, nasa, human, home, change, small, saying, interested, current, mark, area, internet, today, word, original, agree, left, memory, works, microsoft, machine, instead, hardware, kind, working, request, higher, sort, programs, questions, money, later, israel, mike, guess, hand, pretty, include, netcom, address, cause, matter, technology, uiuc, speed, wire, video, type, days, server, view, usually, april, earth, package, open, told, christian, stuff, unless, similar, important, size, major, house, provide, known, faith, ground, rights, michael, phone, body, center, including, health, american, apple, feel, cost, text, user, lines, bible, answer, care, copy, wouldn, understand, check, anybody, security, mind, live, certainly, started, running, message, mouse, level, network, women, study, clinton, making, position, company, came, groups, board, screen, white, common, talking, single, special, quality, black, wiring, test, likely, christians, monitor, nice, effect, light, members, medical, posted, uucp, hope, sources, certain, clear, difference, cars, write, canada, fine, hear, press, launch, build, police, love, history, couple, situation, books, particular, words, jewish, specific, sense, model, religion, anti, stop, posting, unix, talk, private, discussion, school, contact, cable, turkish, keys, frank, built, consider, service, sound, features, legal, taking, simple, comes, reference, argument, tools, children, short, night, jews, applications, clipper, device, application, comments, scsi, process, theory, objective, force, doubt, tried, self, experience, games, early, usenet, expect, steve, needed, tape, uses, interesting, killed, station, exactly, easy, death, value, turn, manager, needs, correct, according, amiga, ones, response, wanted, shuttle, language, states, drug, james, considered, reading, strong, koresh, insurance, personal, term, goes, result, future, taken, past, form, opinion, especially, religious, sorry, mentioned, rules, hell, written, various, author, guns, drivers, went, country, design, plus, happy, society, longer, gets, latest, results, analysis, haven, asked, main, section, laws, previous, cases, york, parts, aren, advance, weapons, christ, mode, required, input, looks, week, accept, community, washington, option, series, circuit, disease, robert, fast, numbers, head, exist, andrew, period, range, venus, israeli, macintosh, driver, office, offer, moral, allow, organization, involved, toronto, clock, players, department, runs, values, months, choice, half, colors, knows, picture, sell, brian, object, took, cards, includes, federal, hockey, individual, wasn, currently, suggest, dave, armenians, takes, protect, follow, americans, directly, candida, title, policy, total, devices, happened, statement, present, purpose, assume, close, recently, equipment, food, require, reasons, scientific, christianity, happen, users, media, provides, deal, wants, city, george, goal, couldn, bike, save, shall, dead, lost, action, speak, road, uunet, terms, batf, court, condition, easily, league, complete, engineering, obviously, details, completely, baseball, california, voice, mission, useful, lead, disclaimer, output, water, algorithm, clearly, administration, ways, compatible, international, sent, rest, responsible, pass, hours, digital, business, appreciated, issues, freedom, kill, project, deleted, companies, coming, operating, average, processing, context, story, figure, error, fans, season, face, port, carry, events, appropriate, leave, berkeley, trade, lower, player, king, page, convert, environment, armenian, political, points, basis, final, requires, heart, addition, performance, building, difficult, site, sale, suppose, related, stanford, resolution, field, willing, volume, actual, apply, knowledge, designed, explain, anonymous, supposed, directory, claims, worth, orbit, lots, basic, defense, advice, commercial, sounds, entries, limited, changes, wonder, suspect, radio, turkey, neutral, forget, wait, necessary, programming, reply, enforcement, inside, family, ability, handle, young, included, signal, homosexuality, natural, morality, finally, land, russian, paper, month, greek, friend, installed, create, thinking, printer, shot, services, understanding, population, hold, break, interface, comment, normal, eric, homosexual, setting, formats, names, peter, machines, report, east, supply, comp, percent, avoid, product, lives, colorado, communications, room, escrow, types, secure, arab, logic, miles, reasonable, rutgers, multiple, gary, soon, agencies, developed, recent, cubs, library, peace, expensive, cheap, cancer, million, allowed, physics, suggestions, doctor, caused, supported, technical, happens, event, looked, obvious, gives, soviet, street, mention, release, outside, table, print, mass, return, radar, archive, chance, install, treatment, bitnet, generally, development, friends, folks, choose, entire, weeks, united, social, wish, smith, trouble, child, straight, learn, supports, behavior, ideas, morning, muslim, member, diet, electrical, illegal, exists, requests, jack, areas, concept, meaning, reality, drives, appear, provided, studies, motif, attempt, possibly, west, answers, asking, pick, practice, engine, worked, stand, bring, thank, unit, remove, near, compound, errors, false, belief, continue, middle, changed, decided, modem, bits, existence, henry, trust, congress, extra, safe, facts, loss, yeah, contains, guys, particularly, arguments, proper, class, manual, frame, command, drugs, stupid, wide, nature, institute, armenia, constitution, thread, pages, function, andy, attack, fonts, privacy, aware, operations, heat, worse, appears, distribution, knew, effective, edge, division, shouldn, wall, approach, speaking, independent, unfortunately, hands, conference, crime, indiana, modern, ethernet, solution, turks, civil, bought, 1992, 1990, compression, safety, serial, added, shows, letter, cramer, faster, simms, operation, arms, internal, germany, gave, record, wondering, virginia, floppy, appreciate, blue, plastic, regular, writing, allows, abortion, utility, hello, rate, blood, spend, views, articles, actions, font, additional, method, described, concerned, scott, played, stated, kids, atheism, surface, base, step, decision, switch, tool, playing, giving, attitude, quote, keith, cover, levels, considering, highly, resources, north, military, connected, buying, places, capability, products, costs, patients, controller, fair, late, prevent, rule, western, poor, brought, functions, received, account, creation, watch, cwru, majority, forward, student, released, driving, authority, committee, protection, richard, boston, quick, calls, chips, valid, hate, shell, excellent, countries, market, necessarily, created, wires, america, apparently, turned, complex, fairly, minutes, murder, boxes, lord, keyboard, properly, plan, moon, arabs, arizona, visual, absolutely, notice, produce, sold, panel, dangerous, killing, begin, property, damage, electronics, living, failed, acts, tests, nation, intelligence, islam, vote, rangers, effort, options, greatly, holy, review, shareware, larry)

``` scala
vocabList.size
```

>     res18: Int = 6122

``` scala
val topics = topicIndices.map { case (terms, termWeights) =>
  terms.map(vocabList(_)).zip(termWeights)
}
```

>     topics: Array[Array[(String, Double)]] = Array(Array((drive,0.03696568415933789), (problem,0.01629861090697588), (card,0.01433934010187806), (monitor,0.013707091530346492), (video,0.012972406811610632)), Array((list,0.016273251673940115), (things,0.013336093829266985), (group,0.011820421026993289), (mail,0.010256327131358182), (people,0.009196940048172493)), Array((want,0.020875510622723917), (really,0.01597476209980187), (netcom,0.014524465702206254), (question,0.012746020817368222), (time,0.012666327258860768)), Array((wire,0.014438050303754668), (wiring,0.012299617198701644), (subject,0.012274987249971222), (ground,0.012021448245551786), (power,0.009432038562323322)), Array((encryption,0.01826967988745936), (government,0.014064779884001608), (chip,0.014059305626418857), (technology,0.011287261911973878), (clipper,0.01076836731761516)), Array((image,0.02791131528914355), (jpeg,0.017670605280479763), (data,0.014293908441743292), (file,0.013060907975843267), (available,0.011622390744664425)), Array((president,0.017859677939407154), (didn,0.013614159584111107), (time,0.012347029991677042), (clinton,0.009190778360916498), (money,0.008859507080584084)), Array((turkish,0.011319396952828511), (world,0.009962877409195969), (armenians,0.008655676983870219), (send,0.00855725322313576), (people,0.007875034876943812)), Array((space,0.01727602824412315), (launch,0.009510894679631877), (nasa,0.008174059835266893), (1993,0.008021957117505637), (venus,0.007554628674289004)), Array((people,0.01981226804171339), (government,0.011487664126618446), (started,0.008584553138876285), (koresh,0.008175885706453272), (guns,0.007377072709520659)), Array((windows,0.05626351594827812), (window,0.01973702915921722), (software,0.019425647730930192), (microsoft,0.01829576253383801), (support,0.017092560057940024)), Array((program,0.02008913356682715), (info,0.015316236531297255), (files,0.011736648109897512), (file,0.011002859969161052), (number,0.010888936862946987)), Array((israel,0.01655912523831529), (people,0.013839621872149849), (israeli,0.009740304134451903), (jewish,0.009293757920083023), (jews,0.007738906079536397)), Array((michael,0.012762222513670877), (actually,0.012512033178943538), (internet,0.010850406228207507), (john,0.009306501754714605), (phone,0.008689054789025402)), Array((medical,0.010062044101355121), (people,0.009878164955234142), (body,0.009242293597826808), (candida,0.008717534535587714), (cause,0.008630197138270254)), Array((game,0.022763279394979432), (year,0.021047821614531215), (team,0.014616528925887282), (play,0.014260492376515463), (better,0.00926312978729905)), Array((looking,0.01697525538431182), (good,0.015502092777115166), (cars,0.014169299624261323), (really,0.010474401518068752), (bike,0.008500864994223083)), Array((problem,0.022253445216367452), (sound,0.014029804886164775), (need,0.012570596158871844), (used,0.011722771786015752), (able,0.011108297878074287)), Array((people,0.012489074839478006), (jesus,0.012002740560764811), (church,0.010792066199830157), (truth,0.010401308111128224), (paul,0.008785091860728546)), Array((mark,0.017392671209746132), (objective,0.013053985125033755), (uucp,0.012399632872577494), (says,0.012118025863373018), (people,0.010567878803828654)))

``` scala
vocabList(47) // 47 is the index of the term 'university' or the first term in topics - this may change due to randomness in algorithm
```

>     res19: String = university

This is just doing it all at once.

``` scala
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
```

>     20 topics:
>     TOPIC 0
>     drive	0.03696568415933789
>     problem	0.01629861090697588
>     card	0.01433934010187806
>     monitor	0.013707091530346492
>     video	0.012972406811610632
>     ==========
>     TOPIC 1
>     list	0.016273251673940115
>     things	0.013336093829266985
>     group	0.011820421026993289
>     mail	0.010256327131358182
>     people	0.009196940048172493
>     ==========
>     TOPIC 2
>     want	0.020875510622723917
>     really	0.01597476209980187
>     netcom	0.014524465702206254
>     question	0.012746020817368222
>     time	0.012666327258860768
>     ==========
>     TOPIC 3
>     wire	0.014438050303754668
>     wiring	0.012299617198701644
>     subject	0.012274987249971222
>     ground	0.012021448245551786
>     power	0.009432038562323322
>     ==========
>     TOPIC 4
>     encryption	0.01826967988745936
>     government	0.014064779884001608
>     chip	0.014059305626418857
>     technology	0.011287261911973878
>     clipper	0.01076836731761516
>     ==========
>     TOPIC 5
>     image	0.02791131528914355
>     jpeg	0.017670605280479763
>     data	0.014293908441743292
>     file	0.013060907975843267
>     available	0.011622390744664425
>     ==========
>     TOPIC 6
>     president	0.017859677939407154
>     didn	0.013614159584111107
>     time	0.012347029991677042
>     clinton	0.009190778360916498
>     money	0.008859507080584084
>     ==========
>     TOPIC 7
>     turkish	0.011319396952828511
>     world	0.009962877409195969
>     armenians	0.008655676983870219
>     send	0.00855725322313576
>     people	0.007875034876943812
>     ==========
>     TOPIC 8
>     space	0.01727602824412315
>     launch	0.009510894679631877
>     nasa	0.008174059835266893
>     1993	0.008021957117505637
>     venus	0.007554628674289004
>     ==========
>     TOPIC 9
>     people	0.01981226804171339
>     government	0.011487664126618446
>     started	0.008584553138876285
>     koresh	0.008175885706453272
>     guns	0.007377072709520659
>     ==========
>     TOPIC 10
>     windows	0.05626351594827812
>     window	0.01973702915921722
>     software	0.019425647730930192
>     microsoft	0.01829576253383801
>     support	0.017092560057940024
>     ==========
>     TOPIC 11
>     program	0.02008913356682715
>     info	0.015316236531297255
>     files	0.011736648109897512
>     file	0.011002859969161052
>     number	0.010888936862946987
>     ==========
>     TOPIC 12
>     israel	0.01655912523831529
>     people	0.013839621872149849
>     israeli	0.009740304134451903
>     jewish	0.009293757920083023
>     jews	0.007738906079536397
>     ==========
>     TOPIC 13
>     michael	0.012762222513670877
>     actually	0.012512033178943538
>     internet	0.010850406228207507
>     john	0.009306501754714605
>     phone	0.008689054789025402
>     ==========
>     TOPIC 14
>     medical	0.010062044101355121
>     people	0.009878164955234142
>     body	0.009242293597826808
>     candida	0.008717534535587714
>     cause	0.008630197138270254
>     ==========
>     TOPIC 15
>     game	0.022763279394979432
>     year	0.021047821614531215
>     team	0.014616528925887282
>     play	0.014260492376515463
>     better	0.00926312978729905
>     ==========
>     TOPIC 16
>     looking	0.01697525538431182
>     good	0.015502092777115166
>     cars	0.014169299624261323
>     really	0.010474401518068752
>     bike	0.008500864994223083
>     ==========
>     TOPIC 17
>     problem	0.022253445216367452
>     sound	0.014029804886164775
>     need	0.012570596158871844
>     used	0.011722771786015752
>     able	0.011108297878074287
>     ==========
>     TOPIC 18
>     people	0.012489074839478006
>     jesus	0.012002740560764811
>     church	0.010792066199830157
>     truth	0.010401308111128224
>     paul	0.008785091860728546
>     ==========
>     TOPIC 19
>     mark	0.017392671209746132
>     objective	0.013053985125033755
>     uucp	0.012399632872577494
>     says	0.012118025863373018
>     people	0.010567878803828654
>     ==========
>     topicIndices: Array[(Array[Int], Array[Double])] = Array((Array(23, 8, 81, 268, 192),Array(0.03696568415933789, 0.01629861090697588, 0.01433934010187806, 0.013707091530346492, 0.012972406811610632)), (Array(59, 31, 63, 20, 0),Array(0.016273251673940115, 0.013336093829266985, 0.011820421026993289, 0.010256327131358182, 0.009196940048172493)), (Array(5, 10, 184, 32, 1),Array(0.020875510622723917, 0.01597476209980187, 0.014524465702206254, 0.012746020817368222, 0.012666327258860768)), (Array(191, 264, 75, 214, 28),Array(0.014438050303754668, 0.012299617198701644, 0.012274987249971222, 0.012021448245551786, 0.009432038562323322)), (Array(128, 30, 126, 188, 332),Array(0.01826967988745936, 0.014064779884001608, 0.014059305626418857, 0.011287261911973878, 0.01076836731761516)), (Array(11, 45, 12, 27, 25),Array(0.02791131528914355, 0.017670605280479763, 0.014293908441743292, 0.013060907975843267, 0.011622390744664425)), (Array(98, 50, 1, 249, 176),Array(0.017859677939407154, 0.013614159584111107, 0.012347029991677042, 0.009190778360916498, 0.008859507080584084)), (Array(312, 36, 478, 65, 0),Array(0.011319396952828511, 0.009962877409195969, 0.008655676983870219, 0.00855725322313576, 0.007875034876943812)), (Array(26, 287, 147, 58, 443),Array(0.01727602824412315, 0.009510894679631877, 0.008174059835266893, 0.008021957117505637, 0.007554628674289004)), (Array(0, 30, 241, 377, 397),Array(0.01981226804171339, 0.011487664126618446, 0.008584553138876285, 0.008175885706453272, 0.007377072709520659)), (Array(4, 110, 17, 165, 69),Array(0.05626351594827812, 0.01973702915921722, 0.019425647730930192, 0.01829576253383801, 0.017092560057940024)), (Array(24, 85, 54, 27, 34),Array(0.02008913356682715, 0.015316236531297255, 0.011736648109897512, 0.011002859969161052, 0.010888936862946987)), (Array(178, 0, 444, 297, 330),Array(0.01655912523831529, 0.013839621872149849, 0.009740304134451903, 0.009293757920083023, 0.007738906079536397)), (Array(216, 57, 157, 74, 217),Array(0.012762222513670877, 0.012512033178943538, 0.010850406228207507, 0.009306501754714605, 0.008689054789025402)), (Array(273, 0, 218, 484, 186),Array(0.010062044101355121, 0.009878164955234142, 0.009242293597826808, 0.008717534535587714, 0.008630197138270254)), (Array(60, 19, 124, 127, 14),Array(0.022763279394979432, 0.021047821614531215, 0.014616528925887282, 0.014260492376515463, 0.00926312978729905)), (Array(116, 2, 281, 10, 512),Array(0.01697525538431182, 0.015502092777115166, 0.014169299624261323, 0.010474401518068752, 0.008500864994223083)), (Array(8, 318, 9, 3, 70),Array(0.022253445216367452, 0.014029804886164775, 0.012570596158871844, 0.011722771786015752, 0.011108297878074287)), (Array(0, 114, 132, 130, 113),Array(0.012489074839478006, 0.012002740560764811, 0.010792066199830157, 0.010401308111128224, 0.008785091860728546)), (Array(155, 339, 275, 91, 0),Array(0.017392671209746132, 0.013053985125033755, 0.012399632872577494, 0.012118025863373018, 0.010567878803828654)))
>     vocabList: Array[String] = Array(people, time, good, used, windows, want, work, right, problem, need, really, image, data, information, better, believe, using, software, years, year, mail, sure, point, drive, program, available, space, file, power, help, government, things, question, doesn, number, case, world, look, read, version, come, thing, different, long, best, jpeg, fact, university, probably, real, didn, course, state, true, files, high, possible, actually, 1993, list, game, little, news, group, david, send, wrong, based, graphics, support, able, place, called, free, john, subject, post, reason, color, great, second, card, public, having, email, info, following, start, hard, science, example, says, means, code, evidence, person, note, maybe, president, heard, general, mean, problems, quite, source, systems, life, price, standard, order, window, access, claim, paul, jesus, getting, looking, trying, control, disk, seen, simply, times, book, team, local, chip, play, encryption, idea, truth, given, church, issue, research, opinions, wrote, images, large, display, makes, remember, thought, doing, national, format, away, nasa, human, home, change, small, saying, interested, current, mark, area, internet, today, word, original, agree, left, memory, works, microsoft, machine, instead, hardware, kind, working, request, higher, sort, programs, questions, money, later, israel, mike, guess, hand, pretty, include, netcom, address, cause, matter, technology, uiuc, speed, wire, video, type, days, server, view, usually, april, earth, package, open, told, christian, stuff, unless, similar, important, size, major, house, provide, known, faith, ground, rights, michael, phone, body, center, including, health, american, apple, feel, cost, text, user, lines, bible, answer, care, copy, wouldn, understand, check, anybody, security, mind, live, certainly, started, running, message, mouse, level, network, women, study, clinton, making, position, company, came, groups, board, screen, white, common, talking, single, special, quality, black, wiring, test, likely, christians, monitor, nice, effect, light, members, medical, posted, uucp, hope, sources, certain, clear, difference, cars, write, canada, fine, hear, press, launch, build, police, love, history, couple, situation, books, particular, words, jewish, specific, sense, model, religion, anti, stop, posting, unix, talk, private, discussion, school, contact, cable, turkish, keys, frank, built, consider, service, sound, features, legal, taking, simple, comes, reference, argument, tools, children, short, night, jews, applications, clipper, device, application, comments, scsi, process, theory, objective, force, doubt, tried, self, experience, games, early, usenet, expect, steve, needed, tape, uses, interesting, killed, station, exactly, easy, death, value, turn, manager, needs, correct, according, amiga, ones, response, wanted, shuttle, language, states, drug, james, considered, reading, strong, koresh, insurance, personal, term, goes, result, future, taken, past, form, opinion, especially, religious, sorry, mentioned, rules, hell, written, various, author, guns, drivers, went, country, design, plus, happy, society, longer, gets, latest, results, analysis, haven, asked, main, section, laws, previous, cases, york, parts, aren, advance, weapons, christ, mode, required, input, looks, week, accept, community, washington, option, series, circuit, disease, robert, fast, numbers, head, exist, andrew, period, range, venus, israeli, macintosh, driver, office, offer, moral, allow, organization, involved, toronto, clock, players, department, runs, values, months, choice, half, colors, knows, picture, sell, brian, object, took, cards, includes, federal, hockey, individual, wasn, currently, suggest, dave, armenians, takes, protect, follow, americans, directly, candida, title, policy, total, devices, happened, statement, present, purpose, assume, close, recently, equipment, food, require, reasons, scientific, christianity, happen, users, media, provides, deal, wants, city, george, goal, couldn, bike, save, shall, dead, lost, action, speak, road, uunet, terms, batf, court, condition, easily, league, complete, engineering, obviously, details, completely, baseball, california, voice, mission, useful, lead, disclaimer, output, water, algorithm, clearly, administration, ways, compatible, international, sent, rest, responsible, pass, hours, digital, business, appreciated, issues, freedom, kill, project, deleted, companies, coming, operating, average, processing, context, story, figure, error, fans, season, face, port, carry, events, appropriate, leave, berkeley, trade, lower, player, king, page, convert, environment, armenian, political, points, basis, final, requires, heart, addition, performance, building, difficult, site, sale, suppose, related, stanford, resolution, field, willing, volume, actual, apply, knowledge, designed, explain, anonymous, supposed, directory, claims, worth, orbit, lots, basic, defense, advice, commercial, sounds, entries, limited, changes, wonder, suspect, radio, turkey, neutral, forget, wait, necessary, programming, reply, enforcement, inside, family, ability, handle, young, included, signal, homosexuality, natural, morality, finally, land, russian, paper, month, greek, friend, installed, create, thinking, printer, shot, services, understanding, population, hold, break, interface, comment, normal, eric, homosexual, setting, formats, names, peter, machines, report, east, supply, comp, percent, avoid, product, lives, colorado, communications, room, escrow, types, secure, arab, logic, miles, reasonable, rutgers, multiple, gary, soon, agencies, developed, recent, cubs, library, peace, expensive, cheap, cancer, million, allowed, physics, suggestions, doctor, caused, supported, technical, happens, event, looked, obvious, gives, soviet, street, mention, release, outside, table, print, mass, return, radar, archive, chance, install, treatment, bitnet, generally, development, friends, folks, choose, entire, weeks, united, social, wish, smith, trouble, child, straight, learn, supports, behavior, ideas, morning, muslim, member, diet, electrical, illegal, exists, requests, jack, areas, concept, meaning, reality, drives, appear, provided, studies, motif, attempt, possibly, west, answers, asking, pick, practice, engine, worked, stand, bring, thank, unit, remove, near, compound, errors, false, belief, continue, middle, changed, decided, modem, bits, existence, henry, trust, congress, extra, safe, facts, loss, yeah, contains, guys, particularly, arguments, proper, class, manual, frame, command, drugs, stupid, wide, nature, institute, armenia, constitution, thread, pages, function, andy, attack, fonts, privacy, aware, operations, heat, worse, appears, distribution, knew, effective, edge, division, shouldn, wall, approach, speaking, independent, unfortunately, hands, conference, crime, indiana, modern, ethernet, solution, turks, civil, bought, 1992, 1990, compression, safety, serial, added, shows, letter, cramer, faster, simms, operation, arms, internal, germany, gave, record, wondering, virginia, floppy, appreciate, blue, plastic, regular, writing, allows, abortion, utility, hello, rate, blood, spend, views, articles, actions, font, additional, method, described, concerned, scott, played, stated, kids, atheism, surface, base, step, decision, switch, tool, playing, giving, attitude, quote, keith, cover, levels, considering, highly, resources, north, military, connected, buying, places, capability, products, costs, patients, controller, fair, late, prevent, rule, western, poor, brought, functions, received, account, creation, watch, cwru, majority, forward, student, released, driving, authority, committee, protection, richard, boston, quick, calls, chips, valid, hate, shell, excellent, countries, market, necessarily, created, wires, america, apparently, turned, complex, fairly, minutes, murder, boxes, lord, keyboard, properly, plan, moon, arabs, arizona, visual, absolutely, notice, produce, sold, panel, dangerous, killing, begin, property, damage, electronics, living, failed, acts, tests, nation, intelligence, islam, vote, rangers, effort, options, greatly, holy, review, shareware, larry)
>     topics: Array[Array[(String, Double)]] = Array(Array((drive,0.03696568415933789), (problem,0.01629861090697588), (card,0.01433934010187806), (monitor,0.013707091530346492), (video,0.012972406811610632)), Array((list,0.016273251673940115), (things,0.013336093829266985), (group,0.011820421026993289), (mail,0.010256327131358182), (people,0.009196940048172493)), Array((want,0.020875510622723917), (really,0.01597476209980187), (netcom,0.014524465702206254), (question,0.012746020817368222), (time,0.012666327258860768)), Array((wire,0.014438050303754668), (wiring,0.012299617198701644), (subject,0.012274987249971222), (ground,0.012021448245551786), (power,0.009432038562323322)), Array((encryption,0.01826967988745936), (government,0.014064779884001608), (chip,0.014059305626418857), (technology,0.011287261911973878), (clipper,0.01076836731761516)), Array((image,0.02791131528914355), (jpeg,0.017670605280479763), (data,0.014293908441743292), (file,0.013060907975843267), (available,0.011622390744664425)), Array((president,0.017859677939407154), (didn,0.013614159584111107), (time,0.012347029991677042), (clinton,0.009190778360916498), (money,0.008859507080584084)), Array((turkish,0.011319396952828511), (world,0.009962877409195969), (armenians,0.008655676983870219), (send,0.00855725322313576), (people,0.007875034876943812)), Array((space,0.01727602824412315), (launch,0.009510894679631877), (nasa,0.008174059835266893), (1993,0.008021957117505637), (venus,0.007554628674289004)), Array((people,0.01981226804171339), (government,0.011487664126618446), (started,0.008584553138876285), (koresh,0.008175885706453272), (guns,0.007377072709520659)), Array((windows,0.05626351594827812), (window,0.01973702915921722), (software,0.019425647730930192), (microsoft,0.01829576253383801), (support,0.017092560057940024)), Array((program,0.02008913356682715), (info,0.015316236531297255), (files,0.011736648109897512), (file,0.011002859969161052), (number,0.010888936862946987)), Array((israel,0.01655912523831529), (people,0.013839621872149849), (israeli,0.009740304134451903), (jewish,0.009293757920083023), (jews,0.007738906079536397)), Array((michael,0.012762222513670877), (actually,0.012512033178943538), (internet,0.010850406228207507), (john,0.009306501754714605), (phone,0.008689054789025402)), Array((medical,0.010062044101355121), (people,0.009878164955234142), (body,0.009242293597826808), (candida,0.008717534535587714), (cause,0.008630197138270254)), Array((game,0.022763279394979432), (year,0.021047821614531215), (team,0.014616528925887282), (play,0.014260492376515463), (better,0.00926312978729905)), Array((looking,0.01697525538431182), (good,0.015502092777115166), (cars,0.014169299624261323), (really,0.010474401518068752), (bike,0.008500864994223083)), Array((problem,0.022253445216367452), (sound,0.014029804886164775), (need,0.012570596158871844), (used,0.011722771786015752), (able,0.011108297878074287)), Array((people,0.012489074839478006), (jesus,0.012002740560764811), (church,0.010792066199830157), (truth,0.010401308111128224), (paul,0.008785091860728546)), Array((mark,0.017392671209746132), (objective,0.013053985125033755), (uucp,0.012399632872577494), (says,0.012118025863373018), (people,0.010567878803828654)))

We've managed to get some good results here. For example, we can easily infer that Topic 0 is about computers, Topic 8 is about space, etc.

We still get some ambiguous results like Topic 17.

To improve our results further, we could employ some of the below methods: - Refilter data for additional data-specific stopwords - Use Stemming or Lemmatization to preprocess data - Experiment with a smaller number of topics, since some of these topics in the 20 Newsgroups are pretty similar - Increase model's MaxIterations

Visualize Results
-----------------

We will try visualizing the results obtained from the EM LDA model with a d3 bubble chart.

``` scala
// Zip topic terms with topic IDs
val termArray = topics.zipWithIndex
```

>     termArray: Array[(Array[(String, Double)], Int)] = Array((Array((drive,0.03696568415933789), (problem,0.01629861090697588), (card,0.01433934010187806), (monitor,0.013707091530346492), (video,0.012972406811610632)),0), (Array((list,0.016273251673940115), (things,0.013336093829266985), (group,0.011820421026993289), (mail,0.010256327131358182), (people,0.009196940048172493)),1), (Array((want,0.020875510622723917), (really,0.01597476209980187), (netcom,0.014524465702206254), (question,0.012746020817368222), (time,0.012666327258860768)),2), (Array((wire,0.014438050303754668), (wiring,0.012299617198701644), (subject,0.012274987249971222), (ground,0.012021448245551786), (power,0.009432038562323322)),3), (Array((encryption,0.01826967988745936), (government,0.014064779884001608), (chip,0.014059305626418857), (technology,0.011287261911973878), (clipper,0.01076836731761516)),4), (Array((image,0.02791131528914355), (jpeg,0.017670605280479763), (data,0.014293908441743292), (file,0.013060907975843267), (available,0.011622390744664425)),5), (Array((president,0.017859677939407154), (didn,0.013614159584111107), (time,0.012347029991677042), (clinton,0.009190778360916498), (money,0.008859507080584084)),6), (Array((turkish,0.011319396952828511), (world,0.009962877409195969), (armenians,0.008655676983870219), (send,0.00855725322313576), (people,0.007875034876943812)),7), (Array((space,0.01727602824412315), (launch,0.009510894679631877), (nasa,0.008174059835266893), (1993,0.008021957117505637), (venus,0.007554628674289004)),8), (Array((people,0.01981226804171339), (government,0.011487664126618446), (started,0.008584553138876285), (koresh,0.008175885706453272), (guns,0.007377072709520659)),9), (Array((windows,0.05626351594827812), (window,0.01973702915921722), (software,0.019425647730930192), (microsoft,0.01829576253383801), (support,0.017092560057940024)),10), (Array((program,0.02008913356682715), (info,0.015316236531297255), (files,0.011736648109897512), (file,0.011002859969161052), (number,0.010888936862946987)),11), (Array((israel,0.01655912523831529), (people,0.013839621872149849), (israeli,0.009740304134451903), (jewish,0.009293757920083023), (jews,0.007738906079536397)),12), (Array((michael,0.012762222513670877), (actually,0.012512033178943538), (internet,0.010850406228207507), (john,0.009306501754714605), (phone,0.008689054789025402)),13), (Array((medical,0.010062044101355121), (people,0.009878164955234142), (body,0.009242293597826808), (candida,0.008717534535587714), (cause,0.008630197138270254)),14), (Array((game,0.022763279394979432), (year,0.021047821614531215), (team,0.014616528925887282), (play,0.014260492376515463), (better,0.00926312978729905)),15), (Array((looking,0.01697525538431182), (good,0.015502092777115166), (cars,0.014169299624261323), (really,0.010474401518068752), (bike,0.008500864994223083)),16), (Array((problem,0.022253445216367452), (sound,0.014029804886164775), (need,0.012570596158871844), (used,0.011722771786015752), (able,0.011108297878074287)),17), (Array((people,0.012489074839478006), (jesus,0.012002740560764811), (church,0.010792066199830157), (truth,0.010401308111128224), (paul,0.008785091860728546)),18), (Array((mark,0.017392671209746132), (objective,0.013053985125033755), (uucp,0.012399632872577494), (says,0.012118025863373018), (people,0.010567878803828654)),19))

``` scala
// Transform data into the form (term, probability, topicId)
val termRDD = sc.parallelize(termArray)
val termRDD2 =termRDD.flatMap( (x: (Array[(String, Double)], Int)) => {
  val arrayOfTuple = x._1
  val topicId = x._2
  arrayOfTuple.map(el => (el._1, el._2, topicId))
})
```

>     termRDD: org.apache.spark.rdd.RDD[(Array[(String, Double)], Int)] = ParallelCollectionRDD[33511] at parallelize at <console>:45
>     termRDD2: org.apache.spark.rdd.RDD[(String, Double, Int)] = MapPartitionsRDD[33512] at flatMap at <console>:46

``` scala
// Create DF with proper column names
val termDF = termRDD2.toDF.withColumnRenamed("_1", "term").withColumnRenamed("_2", "probability").withColumnRenamed("_3", "topicId")
```

>     termDF: org.apache.spark.sql.DataFrame = [term: string, probability: double ... 1 more field]

``` scala
display(termDF)
```

| term       | probability           | topicId |
|------------|-----------------------|---------|
| drive      | 3.696568415933789e-2  | 0.0     |
| problem    | 1.629861090697588e-2  | 0.0     |
| card       | 1.433934010187806e-2  | 0.0     |
| monitor    | 1.3707091530346492e-2 | 0.0     |
| video      | 1.2972406811610632e-2 | 0.0     |
| list       | 1.6273251673940115e-2 | 1.0     |
| things     | 1.3336093829266985e-2 | 1.0     |
| group      | 1.1820421026993289e-2 | 1.0     |
| mail       | 1.0256327131358182e-2 | 1.0     |
| people     | 9.196940048172493e-3  | 1.0     |
| want       | 2.0875510622723917e-2 | 2.0     |
| really     | 1.597476209980187e-2  | 2.0     |
| netcom     | 1.4524465702206254e-2 | 2.0     |
| question   | 1.2746020817368222e-2 | 2.0     |
| time       | 1.2666327258860768e-2 | 2.0     |
| wire       | 1.4438050303754668e-2 | 3.0     |
| wiring     | 1.2299617198701644e-2 | 3.0     |
| subject    | 1.2274987249971222e-2 | 3.0     |
| ground     | 1.2021448245551786e-2 | 3.0     |
| power      | 9.432038562323322e-3  | 3.0     |
| encryption | 1.826967988745936e-2  | 4.0     |
| government | 1.4064779884001608e-2 | 4.0     |
| chip       | 1.4059305626418857e-2 | 4.0     |
| technology | 1.1287261911973878e-2 | 4.0     |
| clipper    | 1.076836731761516e-2  | 4.0     |
| image      | 2.791131528914355e-2  | 5.0     |
| jpeg       | 1.7670605280479763e-2 | 5.0     |
| data       | 1.4293908441743292e-2 | 5.0     |
| file       | 1.3060907975843267e-2 | 5.0     |
| available  | 1.1622390744664425e-2 | 5.0     |

Truncated to 30 rows

We will convert the DataFrame into a JSON format, which will be passed into d3.

``` scala
// Create JSON data
val rawJson = termDF.toJSON.collect().mkString(",\n")
```

>     rawJson: String =
>     {"term":"drive","probability":0.03696568415933789,"topicId":0},
>     {"term":"problem","probability":0.01629861090697588,"topicId":0},
>     {"term":"card","probability":0.01433934010187806,"topicId":0},
>     {"term":"monitor","probability":0.013707091530346492,"topicId":0},
>     {"term":"video","probability":0.012972406811610632,"topicId":0},
>     {"term":"list","probability":0.016273251673940115,"topicId":1},
>     {"term":"things","probability":0.013336093829266985,"topicId":1},
>     {"term":"group","probability":0.011820421026993289,"topicId":1},
>     {"term":"mail","probability":0.010256327131358182,"topicId":1},
>     {"term":"people","probability":0.009196940048172493,"topicId":1},
>     {"term":"want","probability":0.020875510622723917,"topicId":2},
>     {"term":"really","probability":0.01597476209980187,"topicId":2},
>     {"term":"netcom","probability":0.014524465702206254,"topicId":2},
>     {"term":"question","probability":0.012746020817368222,"topicId":2},
>     {"term":"time","probability":0.012666327258860768,"topicId":2},
>     {"term":"wire","probability":0.014438050303754668,"topicId":3},
>     {"term":"wiring","probability":0.012299617198701644,"topicId":3},
>     {"term":"subject","probability":0.012274987249971222,"topicId":3},
>     {"term":"ground","probability":0.012021448245551786,"topicId":3},
>     {"term":"power","probability":0.009432038562323322,"topicId":3},
>     {"term":"encryption","probability":0.01826967988745936,"topicId":4},
>     {"term":"government","probability":0.014064779884001608,"topicId":4},
>     {"term":"chip","probability":0.014059305626418857,"topicId":4},
>     {"term":"technology","probability":0.011287261911973878,"topicId":4},
>     {"term":"clipper","probability":0.01076836731761516,"topicId":4},
>     {"term":"image","probability":0.02791131528914355,"topicId":5},
>     {"term":"jpeg","probability":0.017670605280479763,"topicId":5},
>     {"term":"data","probability":0.014293908441743292,"topicId":5},
>     {"term":"file","probability":0.013060907975843267,"topicId":5},
>     {"term":"available","probability":0.011622390744664425,"topicId":5},
>     {"term":"president","probability":0.017859677939407154,"topicId":6},
>     {"term":"didn","probability":0.013614159584111107,"topicId":6},
>     {"term":"time","probability":0.012347029991677042,"topicId":6},
>     {"term":"clinton","probability":0.009190778360916498,"topicId":6},
>     {"term":"money","probability":0.008859507080584084,"topicId":6},
>     {"term":"turkish","probability":0.011319396952828511,"topicId":7},
>     {"term":"world","probability":0.009962877409195969,"topicId":7},
>     {"term":"armenians","probability":0.008655676983870219,"topicId":7},
>     {"term":"send","probability":0.00855725322313576,"topicId":7},
>     {"term":"people","probability":0.007875034876943812,"topicId":7},
>     {"term":"space","probability":0.01727602824412315,"topicId":8},
>     {"term":"launch","probability":0.009510894679631877,"topicId":8},
>     {"term":"nasa","probability":0.008174059835266893,"topicId":8},
>     {"term":"1993","probability":0.008021957117505637,"topicId":8},
>     {"term":"venus","probability":0.007554628674289004,"topicId":8},
>     {"term":"people","probability":0.01981226804171339,"topicId":9},
>     {"term":"government","probability":0.011487664126618446,"topicId":9},
>     {"term":"started","probability":0.008584553138876285,"topicId":9},
>     {"term":"koresh","probability":0.008175885706453272,"topicId":9},
>     {"term":"guns","probability":0.007377072709520659,"topicId":9},
>     {"term":"windows","probability":0.05626351594827812,"topicId":10},
>     {"term":"window","probability":0.01973702915921722,"topicId":10},
>     {"term":"software","probability":0.019425647730930192,"topicId":10},
>     {"term":"microsoft","probability":0.01829576253383801,"topicId":10},
>     {"term":"support","probability":0.017092560057940024,"topicId":10},
>     {"term":"program","probability":0.02008913356682715,"topicId":11},
>     {"term":"info","probability":0.015316236531297255,"topicId":11},
>     {"term":"files","probability":0.011736648109897512,"topicId":11},
>     {"term":"file","probability":0.011002859969161052,"topicId":11},
>     {"term":"number","probability":0.010888936862946987,"topicId":11},
>     {"term":"israel","probability":0.01655912523831529,"topicId":12},
>     {"term":"people","probability":0.013839621872149849,"topicId":12},
>     {"term":"israeli","probability":0.009740304134451903,"topicId":12},
>     {"term":"jewish","probability":0.009293757920083023,"topicId":12},
>     {"term":"jews","probability":0.007738906079536397,"topicId":12},
>     {"term":"michael","probability":0.012762222513670877,"topicId":13},
>     {"term":"actually","probability":0.012512033178943538,"topicId":13},
>     {"term":"internet","probability":0.010850406228207507,"topicId":13},
>     {"term":"john","probability":0.009306501754714605,"topicId":13},
>     {"term":"phone","probability":0.008689054789025402,"topicId":13},
>     {"term":"medical","probability":0.010062044101355121,"topicId":14},
>     {"term":"people","probability":0.009878164955234142,"topicId":14},
>     {"term":"body","probability":0.009242293597826808,"topicId":14},
>     {"term":"candida","probability":0.008717534535587714,"topicId":14},
>     {"term":"cause","probability":0.008630197138270254,"topicId":14},
>     {"term":"game","probability":0.022763279394979432,"topicId":15},
>     {"term":"year","probability":0.021047821614531215,"topicId":15},
>     {"term":"team","probability":0.014616528925887282,"topicId":15},
>     {"term":"play","probability":0.014260492376515463,"topicId":15},
>     {"term":"better","probability":0.00926312978729905,"topicId":15},
>     {"term":"looking","probability":0.01697525538431182,"topicId":16},
>     {"term":"good","probability":0.015502092777115166,"topicId":16},
>     {"term":"cars","probability":0.014169299624261323,"topicId":16},
>     {"term":"really","probability":0.010474401518068752,"topicId":16},
>     {"term":"bike","probability":0.008500864994223083,"topicId":16},
>     {"term":"problem","probability":0.022253445216367452,"topicId":17},
>     {"term":"sound","probability":0.014029804886164775,"topicId":17},
>     {"term":"need","probability":0.012570596158871844,"topicId":17},
>     {"term":"used","probability":0.011722771786015752,"topicId":17},
>     {"term":"able","probability":0.011108297878074287,"topicId":17},
>     {"term":"people","probability":0.012489074839478006,"topicId":18},
>     {"term":"jesus","probability":0.012002740560764811,"topicId":18},
>     {"term":"church","probability":0.010792066199830157,"topicId":18},
>     {"term":"truth","probability":0.010401308111128224,"topicId":18},
>     {"term":"paul","probability":0.008785091860728546,"topicId":18},
>     {"term":"mark","probability":0.017392671209746132,"topicId":19},
>     {"term":"objective","probability":0.013053985125033755,"topicId":19},
>     {"term":"uucp","probability":0.012399632872577494,"topicId":19},
>     {"term":"says","probability":0.012118025863373018,"topicId":19},
>     {"term":"people","probability":0.010567878803828654,"topicId":19}

We are now ready to use D3 on the rawJson data.

<p class="htmlSandbox">
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
      {"term":"drive","probability":0.03696568415933789,"topicId":0},
{"term":"problem","probability":0.01629861090697588,"topicId":0},
{"term":"card","probability":0.01433934010187806,"topicId":0},
{"term":"monitor","probability":0.013707091530346492,"topicId":0},
{"term":"video","probability":0.012972406811610632,"topicId":0},
{"term":"list","probability":0.016273251673940115,"topicId":1},
{"term":"things","probability":0.013336093829266985,"topicId":1},
{"term":"group","probability":0.011820421026993289,"topicId":1},
{"term":"mail","probability":0.010256327131358182,"topicId":1},
{"term":"people","probability":0.009196940048172493,"topicId":1},
{"term":"want","probability":0.020875510622723917,"topicId":2},
{"term":"really","probability":0.01597476209980187,"topicId":2},
{"term":"netcom","probability":0.014524465702206254,"topicId":2},
{"term":"question","probability":0.012746020817368222,"topicId":2},
{"term":"time","probability":0.012666327258860768,"topicId":2},
{"term":"wire","probability":0.014438050303754668,"topicId":3},
{"term":"wiring","probability":0.012299617198701644,"topicId":3},
{"term":"subject","probability":0.012274987249971222,"topicId":3},
{"term":"ground","probability":0.012021448245551786,"topicId":3},
{"term":"power","probability":0.009432038562323322,"topicId":3},
{"term":"encryption","probability":0.01826967988745936,"topicId":4},
{"term":"government","probability":0.014064779884001608,"topicId":4},
{"term":"chip","probability":0.014059305626418857,"topicId":4},
{"term":"technology","probability":0.011287261911973878,"topicId":4},
{"term":"clipper","probability":0.01076836731761516,"topicId":4},
{"term":"image","probability":0.02791131528914355,"topicId":5},
{"term":"jpeg","probability":0.017670605280479763,"topicId":5},
{"term":"data","probability":0.014293908441743292,"topicId":5},
{"term":"file","probability":0.013060907975843267,"topicId":5},
{"term":"available","probability":0.011622390744664425,"topicId":5},
{"term":"president","probability":0.017859677939407154,"topicId":6},
{"term":"didn","probability":0.013614159584111107,"topicId":6},
{"term":"time","probability":0.012347029991677042,"topicId":6},
{"term":"clinton","probability":0.009190778360916498,"topicId":6},
{"term":"money","probability":0.008859507080584084,"topicId":6},
{"term":"turkish","probability":0.011319396952828511,"topicId":7},
{"term":"world","probability":0.009962877409195969,"topicId":7},
{"term":"armenians","probability":0.008655676983870219,"topicId":7},
{"term":"send","probability":0.00855725322313576,"topicId":7},
{"term":"people","probability":0.007875034876943812,"topicId":7},
{"term":"space","probability":0.01727602824412315,"topicId":8},
{"term":"launch","probability":0.009510894679631877,"topicId":8},
{"term":"nasa","probability":0.008174059835266893,"topicId":8},
{"term":"1993","probability":0.008021957117505637,"topicId":8},
{"term":"venus","probability":0.007554628674289004,"topicId":8},
{"term":"people","probability":0.01981226804171339,"topicId":9},
{"term":"government","probability":0.011487664126618446,"topicId":9},
{"term":"started","probability":0.008584553138876285,"topicId":9},
{"term":"koresh","probability":0.008175885706453272,"topicId":9},
{"term":"guns","probability":0.007377072709520659,"topicId":9},
{"term":"windows","probability":0.05626351594827812,"topicId":10},
{"term":"window","probability":0.01973702915921722,"topicId":10},
{"term":"software","probability":0.019425647730930192,"topicId":10},
{"term":"microsoft","probability":0.01829576253383801,"topicId":10},
{"term":"support","probability":0.017092560057940024,"topicId":10},
{"term":"program","probability":0.02008913356682715,"topicId":11},
{"term":"info","probability":0.015316236531297255,"topicId":11},
{"term":"files","probability":0.011736648109897512,"topicId":11},
{"term":"file","probability":0.011002859969161052,"topicId":11},
{"term":"number","probability":0.010888936862946987,"topicId":11},
{"term":"israel","probability":0.01655912523831529,"topicId":12},
{"term":"people","probability":0.013839621872149849,"topicId":12},
{"term":"israeli","probability":0.009740304134451903,"topicId":12},
{"term":"jewish","probability":0.009293757920083023,"topicId":12},
{"term":"jews","probability":0.007738906079536397,"topicId":12},
{"term":"michael","probability":0.012762222513670877,"topicId":13},
{"term":"actually","probability":0.012512033178943538,"topicId":13},
{"term":"internet","probability":0.010850406228207507,"topicId":13},
{"term":"john","probability":0.009306501754714605,"topicId":13},
{"term":"phone","probability":0.008689054789025402,"topicId":13},
{"term":"medical","probability":0.010062044101355121,"topicId":14},
{"term":"people","probability":0.009878164955234142,"topicId":14},
{"term":"body","probability":0.009242293597826808,"topicId":14},
{"term":"candida","probability":0.008717534535587714,"topicId":14},
{"term":"cause","probability":0.008630197138270254,"topicId":14},
{"term":"game","probability":0.022763279394979432,"topicId":15},
{"term":"year","probability":0.021047821614531215,"topicId":15},
{"term":"team","probability":0.014616528925887282,"topicId":15},
{"term":"play","probability":0.014260492376515463,"topicId":15},
{"term":"better","probability":0.00926312978729905,"topicId":15},
{"term":"looking","probability":0.01697525538431182,"topicId":16},
{"term":"good","probability":0.015502092777115166,"topicId":16},
{"term":"cars","probability":0.014169299624261323,"topicId":16},
{"term":"really","probability":0.010474401518068752,"topicId":16},
{"term":"bike","probability":0.008500864994223083,"topicId":16},
{"term":"problem","probability":0.022253445216367452,"topicId":17},
{"term":"sound","probability":0.014029804886164775,"topicId":17},
{"term":"need","probability":0.012570596158871844,"topicId":17},
{"term":"used","probability":0.011722771786015752,"topicId":17},
{"term":"able","probability":0.011108297878074287,"topicId":17},
{"term":"people","probability":0.012489074839478006,"topicId":18},
{"term":"jesus","probability":0.012002740560764811,"topicId":18},
{"term":"church","probability":0.010792066199830157,"topicId":18},
{"term":"truth","probability":0.010401308111128224,"topicId":18},
{"term":"paul","probability":0.008785091860728546,"topicId":18},
{"term":"mark","probability":0.017392671209746132,"topicId":19},
{"term":"objective","probability":0.013053985125033755,"topicId":19},
{"term":"uucp","probability":0.012399632872577494,"topicId":19},
{"term":"says","probability":0.012118025863373018,"topicId":19},
{"term":"people","probability":0.010567878803828654,"topicId":19}
     ]
    }
   ]
};

var r = 1000,
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
</p>

### You try!

**NOW or Later as HOMEWORK**

1.  Try to do the same process for the State of the Union Addresses dataset from Week1. As a first step, first locate where that data is... Go to week1 and try to see if each SoU can be treated as a document for topic modeling and whether there is temporal clustering of SoU's within the same topic.

2.  Try to improve the tuning by elaborating the pipeline with stemming, lemmatization, etc in this news-group dataset (if you want to do a project based on this, perhaps). You can also parse the input to bring in the newsgroup id's from the directories (consider exploiting the file names in the `wholeTextFiles` method) as this will let you explore how well your unsupervised algorithm is doing relative to the known newsgroups each document falls in (note you generally won't have the luxury of knowing the topic labels for typical datasets in the unsupervised topic modeling domain).

3.  Try to parse the data closer to the clean dataset available in `/databricks-datasets/news20.binary/*` and walk through the following notebook (*but in Scala!*):
    -   <https://docs.cloud.databricks.com/docs/latest/sample_applications/07%20Sample%20ML/MLPipeline%20Newsgroup%20Dataset.html>

``` fs
ls /databricks-datasets/news20.binary/data-001
```

| path                                                       | name      | size |
|------------------------------------------------------------|-----------|------|
| dbfs:/databricks-datasets/news20.binary/data-001/test/     | test/     | 0.0  |
| dbfs:/databricks-datasets/news20.binary/data-001/training/ | training/ | 0.0  |

Step 1. Downloading and Loading Data into DBFS
----------------------------------------------

**you don't have to do the download in databricks if above cell has contents in `/databricks-datasets/news20.binary/data-001`**

Here are the steps taken for downloading and saving data to the distributed file system. Uncomment them for repeating this process on your databricks cluster or for downloading a new source of data.

``` scala
//%sh wget http://kdd.ics.uci.edu/databases/20newsgroups/mini_newsgroups.tar.gz -O /tmp/newsgroups.tar.gz
```

>     --2016-04-06 22:06:17--  http://kdd.ics.uci.edu/databases/20newsgroups/mini_newsgroups.tar.gz
>     Resolving kdd.ics.uci.edu (kdd.ics.uci.edu)... 128.195.1.95
>     Connecting to kdd.ics.uci.edu (kdd.ics.uci.edu)|128.195.1.95|:80... connected.
>     HTTP request sent, awaiting response... 200 OK
>     Length: 1860687 (1.8M) [application/x-gzip]
>     Saving to: '/tmp/newsgroups.tar.gz'
>
>          0K .......... .......... .......... .......... ..........  2%  134K 13s
>         50K .......... .......... .......... .......... ..........  5%  270K 10s
>        100K .......... .......... .......... .......... ..........  8% 15.8M 6s
>        150K .......... .......... .......... .......... .......... 11%  273K 6s
>        200K .......... .......... .......... .......... .......... 13% 25.1M 5s
>        250K .......... .......... .......... .......... .......... 16% 31.0M 4s
>        300K .......... .......... .......... .......... .......... 19%  276K 4s
>        350K .......... .......... .......... .......... .......... 22% 34.5M 3s
>        400K .......... .......... .......... .......... .......... 24% 31.4M 3s
>        450K .......... .......... .......... .......... .......... 27% 42.9M 2s
>        500K .......... .......... .......... .......... .......... 30% 53.9M 2s
>        550K .......... .......... .......... .......... .......... 33% 73.5M 2s
>        600K .......... .......... .......... .......... .......... 35%  275K 2s
>        650K .......... .......... .......... .......... .......... 38% 54.7M 2s
>        700K .......... .......... .......... .......... .......... 41% 46.4M 2s
>        750K .......... .......... .......... .......... .......... 44% 52.4M 1s
>        800K .......... .......... .......... .......... .......... 46% 32.4M 1s
>        850K .......... .......... .......... .......... .......... 49% 52.3M 1s
>        900K .......... .......... .......... .......... .......... 52% 66.7M 1s
>        950K .......... .......... .......... .......... .......... 55%  279K 1s
>       1000K .......... .......... .......... .......... .......... 57% 34.8M 1s
>       1050K .......... .......... .......... .......... .......... 60% 47.8M 1s
>       1100K .......... .......... .......... .......... .......... 63% 22.5M 1s
>       1150K .......... .......... .......... .......... .......... 66% 58.1M 1s
>       1200K .......... .......... .......... .......... .......... 68% 47.8M 1s
>       1250K .......... .......... .......... .......... .......... 71% 51.4M 1s
>       1300K .......... .......... .......... .......... .......... 74% 59.8M 0s
>       1350K .......... .......... .......... .......... .......... 77%  281K 0s
>       1400K .......... .......... .......... .......... .......... 79% 31.7M 0s
>       1450K .......... .......... .......... .......... .......... 82% 32.7M 0s
>       1500K .......... .......... .......... .......... .......... 85% 36.6M 0s
>       1550K .......... .......... .......... .......... .......... 88% 54.1M 0s
>       1600K .......... .......... .......... .......... .......... 90% 44.1M 0s
>       1650K .......... .......... .......... .......... .......... 93% 51.3M 0s
>       1700K .......... .......... .......... .......... .......... 96% 55.8M 0s
>       1750K .......... .......... .......... .......... .......... 99%  281K 0s
>       1800K .......... .......                                    100% 32.3M=1.7s
>
>     2016-04-06 22:06:20 (1.06 MB/s) - '/tmp/newsgroups.tar.gz' saved [1860687/1860687]

Untar the file into the /tmp/ folder.

``` scala
//%sh tar xvfz /tmp/newsgroups.tar.gz -C /tmp/
```

>     mini_newsgroups/alt.atheism/
>     mini_newsgroups/alt.atheism/51127
>     mini_newsgroups/alt.atheism/51310
>     mini_newsgroups/alt.atheism/53539
>     mini_newsgroups/alt.atheism/53336
>     mini_newsgroups/alt.atheism/53212
>     mini_newsgroups/alt.atheism/51199
>     mini_newsgroups/alt.atheism/54144
>     mini_newsgroups/alt.atheism/54170
>     mini_newsgroups/alt.atheism/51126
>     mini_newsgroups/alt.atheism/51313
>     mini_newsgroups/alt.atheism/51166
>     mini_newsgroups/alt.atheism/53760
>     mini_newsgroups/alt.atheism/53211
>     mini_newsgroups/alt.atheism/54251
>     mini_newsgroups/alt.atheism/53188
>     mini_newsgroups/alt.atheism/54237
>     mini_newsgroups/alt.atheism/51227
>     mini_newsgroups/alt.atheism/51146
>     mini_newsgroups/alt.atheism/53542
>     mini_newsgroups/alt.atheism/53291
>     mini_newsgroups/alt.atheism/53150
>     mini_newsgroups/alt.atheism/53427
>     mini_newsgroups/alt.atheism/53061
>     mini_newsgroups/alt.atheism/53564
>     mini_newsgroups/alt.atheism/53574
>     mini_newsgroups/alt.atheism/53351
>     mini_newsgroups/alt.atheism/53334
>     mini_newsgroups/alt.atheism/53610
>     mini_newsgroups/alt.atheism/51195
>     mini_newsgroups/alt.atheism/53753
>     mini_newsgroups/alt.atheism/53410
>     mini_newsgroups/alt.atheism/53303
>     mini_newsgroups/alt.atheism/53565
>     mini_newsgroups/alt.atheism/51170
>     mini_newsgroups/alt.atheism/51305
>     mini_newsgroups/alt.atheism/54137
>     mini_newsgroups/alt.atheism/53312
>     mini_newsgroups/alt.atheism/53575
>     mini_newsgroups/alt.atheism/53458
>     mini_newsgroups/alt.atheism/53249
>     mini_newsgroups/alt.atheism/53299
>     mini_newsgroups/alt.atheism/53393
>     mini_newsgroups/alt.atheism/54485
>     mini_newsgroups/alt.atheism/54254
>     mini_newsgroups/alt.atheism/54171
>     mini_newsgroups/alt.atheism/51281
>     mini_newsgroups/alt.atheism/53607
>     mini_newsgroups/alt.atheism/53606
>     mini_newsgroups/alt.atheism/53190
>     mini_newsgroups/alt.atheism/51223
>     mini_newsgroups/alt.atheism/51251
>     mini_newsgroups/alt.atheism/53525
>     mini_newsgroups/alt.atheism/53154
>     mini_newsgroups/alt.atheism/53126
>     mini_newsgroups/alt.atheism/53670
>     mini_newsgroups/alt.atheism/54250
>     mini_newsgroups/alt.atheism/53590
>     mini_newsgroups/alt.atheism/53512
>     mini_newsgroups/alt.atheism/53518
>     mini_newsgroups/alt.atheism/53284
>     mini_newsgroups/alt.atheism/54244
>     mini_newsgroups/alt.atheism/54215
>     mini_newsgroups/alt.atheism/54234
>     mini_newsgroups/alt.atheism/51121
>     mini_newsgroups/alt.atheism/53222
>     mini_newsgroups/alt.atheism/53433
>     mini_newsgroups/alt.atheism/53538
>     mini_newsgroups/alt.atheism/51203
>     mini_newsgroups/alt.atheism/53399
>     mini_newsgroups/alt.atheism/54222
>     mini_newsgroups/alt.atheism/51314
>     mini_newsgroups/alt.atheism/53358
>     mini_newsgroups/alt.atheism/53408
>     mini_newsgroups/alt.atheism/53599
>     mini_newsgroups/alt.atheism/51139
>     mini_newsgroups/alt.atheism/53369
>     mini_newsgroups/alt.atheism/53474
>     mini_newsgroups/alt.atheism/53623
>     mini_newsgroups/alt.atheism/51186
>     mini_newsgroups/alt.atheism/53653
>     mini_newsgroups/alt.atheism/53490
>     mini_newsgroups/alt.atheism/51191
>     mini_newsgroups/alt.atheism/53235
>     mini_newsgroups/alt.atheism/53633
>     mini_newsgroups/alt.atheism/54160
>     mini_newsgroups/alt.atheism/53420
>     mini_newsgroups/alt.atheism/51174
>     mini_newsgroups/alt.atheism/53558
>     mini_newsgroups/alt.atheism/51222
>     mini_newsgroups/alt.atheism/53123
>     mini_newsgroups/alt.atheism/54140
>     mini_newsgroups/alt.atheism/53659
>     mini_newsgroups/alt.atheism/53759
>     mini_newsgroups/alt.atheism/53603
>     mini_newsgroups/alt.atheism/53459
>     mini_newsgroups/alt.atheism/53062
>     mini_newsgroups/alt.atheism/51143
>     mini_newsgroups/alt.atheism/51131
>     mini_newsgroups/alt.atheism/54201
>     mini_newsgroups/alt.atheism/53509
>     mini_newsgroups/comp.graphics/
>     mini_newsgroups/comp.graphics/38464
>     mini_newsgroups/comp.graphics/38965
>     mini_newsgroups/comp.graphics/39659
>     mini_newsgroups/comp.graphics/38936
>     mini_newsgroups/comp.graphics/39008
>     mini_newsgroups/comp.graphics/39620
>     mini_newsgroups/comp.graphics/38980
>     mini_newsgroups/comp.graphics/39664
>     mini_newsgroups/comp.graphics/37916
>     mini_newsgroups/comp.graphics/38788
>     mini_newsgroups/comp.graphics/38867
>     mini_newsgroups/comp.graphics/39013
>     mini_newsgroups/comp.graphics/38755
>     mini_newsgroups/comp.graphics/38907
>     mini_newsgroups/comp.graphics/38853
>     mini_newsgroups/comp.graphics/38606
>     mini_newsgroups/comp.graphics/38998
>     mini_newsgroups/comp.graphics/39000
>     mini_newsgroups/comp.graphics/38571
>     mini_newsgroups/comp.graphics/38491
>     mini_newsgroups/comp.graphics/38421
>     mini_newsgroups/comp.graphics/38489
>     mini_newsgroups/comp.graphics/39027
>     mini_newsgroups/comp.graphics/38573
>     mini_newsgroups/comp.graphics/38693
>     mini_newsgroups/comp.graphics/37936
>     mini_newsgroups/comp.graphics/38470
>     mini_newsgroups/comp.graphics/38439
>     mini_newsgroups/comp.graphics/38636
>     mini_newsgroups/comp.graphics/38355
>     mini_newsgroups/comp.graphics/39675
>     mini_newsgroups/comp.graphics/39022
>     mini_newsgroups/comp.graphics/39017
>     mini_newsgroups/comp.graphics/38983
>     mini_newsgroups/comp.graphics/38839
>     mini_newsgroups/comp.graphics/38921
>     mini_newsgroups/comp.graphics/38925
>     mini_newsgroups/comp.graphics/38753
>     mini_newsgroups/comp.graphics/38880
>     mini_newsgroups/comp.graphics/39621
>     mini_newsgroups/comp.graphics/38264
>     mini_newsgroups/comp.graphics/38674
>     mini_newsgroups/comp.graphics/38843
>     mini_newsgroups/comp.graphics/39663
>     mini_newsgroups/comp.graphics/38244
>     mini_newsgroups/comp.graphics/38700
>     mini_newsgroups/comp.graphics/38459
>     mini_newsgroups/comp.graphics/38904
>     mini_newsgroups/comp.graphics/37930
>     mini_newsgroups/comp.graphics/38379
>     mini_newsgroups/comp.graphics/38670
>     mini_newsgroups/comp.graphics/38750
>     mini_newsgroups/comp.graphics/38942
>     mini_newsgroups/comp.graphics/38375
>     mini_newsgroups/comp.graphics/39049
>     mini_newsgroups/comp.graphics/37921
>     mini_newsgroups/comp.graphics/38380
>     mini_newsgroups/comp.graphics/38577
>     mini_newsgroups/comp.graphics/38758
>     mini_newsgroups/comp.graphics/39078
>     mini_newsgroups/comp.graphics/38409
>     mini_newsgroups/comp.graphics/38709
>     mini_newsgroups/comp.graphics/38968
>     mini_newsgroups/comp.graphics/38562
>     mini_newsgroups/comp.graphics/38370
>     mini_newsgroups/comp.graphics/38683
>     mini_newsgroups/comp.graphics/39048
>     mini_newsgroups/comp.graphics/38251
>     mini_newsgroups/comp.graphics/38220
>     mini_newsgroups/comp.graphics/38761
>     mini_newsgroups/comp.graphics/38224
>     mini_newsgroups/comp.graphics/38473
>     mini_newsgroups/comp.graphics/38386
>     mini_newsgroups/comp.graphics/39615
>     mini_newsgroups/comp.graphics/38266
>     mini_newsgroups/comp.graphics/38466
>     mini_newsgroups/comp.graphics/38622
>     mini_newsgroups/comp.graphics/38628
>     mini_newsgroups/comp.graphics/38603
>     mini_newsgroups/comp.graphics/39668
>     mini_newsgroups/comp.graphics/39072
>     mini_newsgroups/comp.graphics/37947
>     mini_newsgroups/comp.graphics/38613
>     mini_newsgroups/comp.graphics/38884
>     mini_newsgroups/comp.graphics/38369
>     mini_newsgroups/comp.graphics/38271
>     mini_newsgroups/comp.graphics/38402
>     mini_newsgroups/comp.graphics/38929
>     mini_newsgroups/comp.graphics/37944
>     mini_newsgroups/comp.graphics/38845
>     mini_newsgroups/comp.graphics/38846
>     mini_newsgroups/comp.graphics/38625
>     mini_newsgroups/comp.graphics/37942
>     mini_newsgroups/comp.graphics/38835
>     mini_newsgroups/comp.graphics/38893
>     mini_newsgroups/comp.graphics/38856
>     mini_newsgroups/comp.graphics/38454
>     mini_newsgroups/comp.graphics/38699
>     mini_newsgroups/comp.graphics/38704
>     mini_newsgroups/comp.graphics/38518
>     mini_newsgroups/comp.os.ms-windows.misc/
>     mini_newsgroups/comp.os.ms-windows.misc/9704
>     mini_newsgroups/comp.os.ms-windows.misc/10942
>     mini_newsgroups/comp.os.ms-windows.misc/9667
>     mini_newsgroups/comp.os.ms-windows.misc/9883
>     mini_newsgroups/comp.os.ms-windows.misc/10167
>     mini_newsgroups/comp.os.ms-windows.misc/9994
>     mini_newsgroups/comp.os.ms-windows.misc/9639
>     mini_newsgroups/comp.os.ms-windows.misc/9908
>     mini_newsgroups/comp.os.ms-windows.misc/10031
>     mini_newsgroups/comp.os.ms-windows.misc/9975
>     mini_newsgroups/comp.os.ms-windows.misc/10141
>     mini_newsgroups/comp.os.ms-windows.misc/10139
>     mini_newsgroups/comp.os.ms-windows.misc/9645
>     mini_newsgroups/comp.os.ms-windows.misc/10087
>     mini_newsgroups/comp.os.ms-windows.misc/9141
>     mini_newsgroups/comp.os.ms-windows.misc/9571
>     mini_newsgroups/comp.os.ms-windows.misc/9539
>     mini_newsgroups/comp.os.ms-windows.misc/9622
>     mini_newsgroups/comp.os.ms-windows.misc/10047
>     mini_newsgroups/comp.os.ms-windows.misc/9519
>     mini_newsgroups/comp.os.ms-windows.misc/10094
>     mini_newsgroups/comp.os.ms-windows.misc/9881
>     mini_newsgroups/comp.os.ms-windows.misc/10093
>     mini_newsgroups/comp.os.ms-windows.misc/10806
>     mini_newsgroups/comp.os.ms-windows.misc/9151
>     mini_newsgroups/comp.os.ms-windows.misc/10107
>     mini_newsgroups/comp.os.ms-windows.misc/9718
>     mini_newsgroups/comp.os.ms-windows.misc/9499
>     mini_newsgroups/comp.os.ms-windows.misc/10742
>     mini_newsgroups/comp.os.ms-windows.misc/10015
>     mini_newsgroups/comp.os.ms-windows.misc/10076
>     mini_newsgroups/comp.os.ms-windows.misc/9485
>     mini_newsgroups/comp.os.ms-windows.misc/10005
>     mini_newsgroups/comp.os.ms-windows.misc/9725
>     mini_newsgroups/comp.os.ms-windows.misc/9939
>     mini_newsgroups/comp.os.ms-windows.misc/9799
>     mini_newsgroups/comp.os.ms-windows.misc/10023
>     mini_newsgroups/comp.os.ms-windows.misc/10790
>     mini_newsgroups/comp.os.ms-windows.misc/10857
>     mini_newsgroups/comp.os.ms-windows.misc/9456
>     mini_newsgroups/comp.os.ms-windows.misc/9776
>     mini_newsgroups/comp.os.ms-windows.misc/10114
>     mini_newsgroups/comp.os.ms-windows.misc/9496
>     mini_newsgroups/comp.os.ms-windows.misc/10128
>     mini_newsgroups/comp.os.ms-windows.misc/9859
>     mini_newsgroups/comp.os.ms-windows.misc/9586
>     mini_newsgroups/comp.os.ms-windows.misc/10692
>     mini_newsgroups/comp.os.ms-windows.misc/10142
>     mini_newsgroups/comp.os.ms-windows.misc/9803
>     mini_newsgroups/comp.os.ms-windows.misc/9911
>     mini_newsgroups/comp.os.ms-windows.misc/9726
>     mini_newsgroups/comp.os.ms-windows.misc/9567
>     mini_newsgroups/comp.os.ms-windows.misc/9512
>     mini_newsgroups/comp.os.ms-windows.misc/10160
>     mini_newsgroups/comp.os.ms-windows.misc/9486
>     mini_newsgroups/comp.os.ms-windows.misc/9697
>     mini_newsgroups/comp.os.ms-windows.misc/9995
>     mini_newsgroups/comp.os.ms-windows.misc/9744
>     mini_newsgroups/comp.os.ms-windows.misc/9737
>     mini_newsgroups/comp.os.ms-windows.misc/9942
>     mini_newsgroups/comp.os.ms-windows.misc/10125
>     mini_newsgroups/comp.os.ms-windows.misc/10157
>     mini_newsgroups/comp.os.ms-windows.misc/9970
>     mini_newsgroups/comp.os.ms-windows.misc/9790
>     mini_newsgroups/comp.os.ms-windows.misc/10850
>     mini_newsgroups/comp.os.ms-windows.misc/9679
>     mini_newsgroups/comp.os.ms-windows.misc/10835
>     mini_newsgroups/comp.os.ms-windows.misc/9924
>     mini_newsgroups/comp.os.ms-windows.misc/10843
>     mini_newsgroups/comp.os.ms-windows.misc/10830
>     mini_newsgroups/comp.os.ms-windows.misc/10791
>     mini_newsgroups/comp.os.ms-windows.misc/9538
>     mini_newsgroups/comp.os.ms-windows.misc/10188
>     mini_newsgroups/comp.os.ms-windows.misc/10848
>     mini_newsgroups/comp.os.ms-windows.misc/10814
>     mini_newsgroups/comp.os.ms-windows.misc/9758
>     mini_newsgroups/comp.os.ms-windows.misc/9750
>     mini_newsgroups/comp.os.ms-windows.misc/9706
>     mini_newsgroups/comp.os.ms-windows.misc/10849
>     mini_newsgroups/comp.os.ms-windows.misc/9902
>     mini_newsgroups/comp.os.ms-windows.misc/10041
>     mini_newsgroups/comp.os.ms-windows.misc/9479
>     mini_newsgroups/comp.os.ms-windows.misc/10090
>     mini_newsgroups/comp.os.ms-windows.misc/10016
>     mini_newsgroups/comp.os.ms-windows.misc/10158
>     mini_newsgroups/comp.os.ms-windows.misc/10115
>     mini_newsgroups/comp.os.ms-windows.misc/9997
>     mini_newsgroups/comp.os.ms-windows.misc/9657
>     mini_newsgroups/comp.os.ms-windows.misc/10812
>     mini_newsgroups/comp.os.ms-windows.misc/10781
>     mini_newsgroups/comp.os.ms-windows.misc/10838
>     mini_newsgroups/comp.os.ms-windows.misc/10003
>     mini_newsgroups/comp.os.ms-windows.misc/10008
>     mini_newsgroups/comp.os.ms-windows.misc/9804
>     mini_newsgroups/comp.os.ms-windows.misc/9814
>     mini_newsgroups/comp.os.ms-windows.misc/9933
>     mini_newsgroups/comp.os.ms-windows.misc/9943
>     mini_newsgroups/comp.os.ms-windows.misc/9509
>     mini_newsgroups/comp.os.ms-windows.misc/9600
>     mini_newsgroups/comp.os.ms-windows.misc/9779
>     mini_newsgroups/comp.sys.ibm.pc.hardware/
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60369
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60393
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60543
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60842
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60389
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60232
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61094
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61076
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60481
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60691
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60425
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60475
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60735
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60732
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61019
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60304
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60882
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60992
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61046
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61120
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61044
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60859
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60838
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60137
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60235
>     mini_newsgroups/comp.sys.ibm.pc.hardware/58983
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61009
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60912
>     mini_newsgroups/comp.sys.ibm.pc.hardware/58831
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61090
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60134
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61168
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60652
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61164
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60377
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60684
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60150
>     mini_newsgroups/comp.sys.ibm.pc.hardware/58829
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60722
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60439
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60694
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60440
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60828
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61026
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61173
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60548
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60685
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60699
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60411
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61158
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60199
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60376
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60656
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60928
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60271
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60837
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61022
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60551
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61175
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60278
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60474
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61098
>     mini_newsgroups/comp.sys.ibm.pc.hardware/58922
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60998
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60409
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60663
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60724
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61154
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60159
>     mini_newsgroups/comp.sys.ibm.pc.hardware/58994
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60769
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60982
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60988
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60453
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60221
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61130
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61153
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60151
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60514
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60749
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60394
>     mini_newsgroups/comp.sys.ibm.pc.hardware/58966
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60961
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60934
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60945
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60457
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60509
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60191
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60404
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61003
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60695
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60841
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61060
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60766
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60273
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60698
>     mini_newsgroups/comp.sys.ibm.pc.hardware/61039
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60154
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60526
>     mini_newsgroups/comp.sys.ibm.pc.hardware/60156
>     mini_newsgroups/comp.sys.mac.hardware/
>     mini_newsgroups/comp.sys.mac.hardware/52155
>     mini_newsgroups/comp.sys.mac.hardware/52123
>     mini_newsgroups/comp.sys.mac.hardware/51752
>     mini_newsgroups/comp.sys.mac.hardware/51565
>     mini_newsgroups/comp.sys.mac.hardware/50473
>     mini_newsgroups/comp.sys.mac.hardware/51494
>     mini_newsgroups/comp.sys.mac.hardware/51867
>     mini_newsgroups/comp.sys.mac.hardware/50457
>     mini_newsgroups/comp.sys.mac.hardware/50419
>     mini_newsgroups/comp.sys.mac.hardware/50439
>     mini_newsgroups/comp.sys.mac.hardware/52342
>     mini_newsgroups/comp.sys.mac.hardware/52102
>     mini_newsgroups/comp.sys.mac.hardware/50546
>     mini_newsgroups/comp.sys.mac.hardware/52270
>     mini_newsgroups/comp.sys.mac.hardware/51928
>     mini_newsgroups/comp.sys.mac.hardware/52059
>     mini_newsgroups/comp.sys.mac.hardware/52113
>     mini_newsgroups/comp.sys.mac.hardware/51855
>     mini_newsgroups/comp.sys.mac.hardware/52156
>     mini_newsgroups/comp.sys.mac.hardware/52312
>     mini_newsgroups/comp.sys.mac.hardware/52231
>     mini_newsgroups/comp.sys.mac.hardware/51519
>     mini_newsgroups/comp.sys.mac.hardware/52246
>     mini_newsgroups/comp.sys.mac.hardware/51948
>     mini_newsgroups/comp.sys.mac.hardware/52335
>     mini_newsgroups/comp.sys.mac.hardware/51813
>     mini_newsgroups/comp.sys.mac.hardware/51751
>     mini_newsgroups/comp.sys.mac.hardware/51809
>     mini_newsgroups/comp.sys.mac.hardware/51963
>     mini_newsgroups/comp.sys.mac.hardware/51711
>     mini_newsgroups/comp.sys.mac.hardware/51763
>     mini_newsgroups/comp.sys.mac.hardware/51950
>     mini_newsgroups/comp.sys.mac.hardware/51846
>     mini_newsgroups/comp.sys.mac.hardware/52284
>     mini_newsgroups/comp.sys.mac.hardware/52094
>     mini_newsgroups/comp.sys.mac.hardware/52403
>     mini_newsgroups/comp.sys.mac.hardware/52269
>     mini_newsgroups/comp.sys.mac.hardware/50465
>     mini_newsgroups/comp.sys.mac.hardware/51707
>     mini_newsgroups/comp.sys.mac.hardware/51786
>     mini_newsgroups/comp.sys.mac.hardware/51539
>     mini_newsgroups/comp.sys.mac.hardware/51703
>     mini_newsgroups/comp.sys.mac.hardware/51962
>     mini_newsgroups/comp.sys.mac.hardware/52175
>     mini_newsgroups/comp.sys.mac.hardware/52296
>     mini_newsgroups/comp.sys.mac.hardware/51522
>     mini_newsgroups/comp.sys.mac.hardware/51805
>     mini_newsgroups/comp.sys.mac.hardware/52090
>     mini_newsgroups/comp.sys.mac.hardware/52190
>     mini_newsgroups/comp.sys.mac.hardware/51678
>     mini_newsgroups/comp.sys.mac.hardware/52069
>     mini_newsgroups/comp.sys.mac.hardware/51661
>     mini_newsgroups/comp.sys.mac.hardware/52276
>     mini_newsgroups/comp.sys.mac.hardware/51510
>     mini_newsgroups/comp.sys.mac.hardware/50533
>     mini_newsgroups/comp.sys.mac.hardware/52238
>     mini_newsgroups/comp.sys.mac.hardware/52065
>     mini_newsgroups/comp.sys.mac.hardware/52264
>     mini_newsgroups/comp.sys.mac.hardware/51613
>     mini_newsgroups/comp.sys.mac.hardware/52300
>     mini_newsgroups/comp.sys.mac.hardware/51996
>     mini_newsgroups/comp.sys.mac.hardware/51501
>     mini_newsgroups/comp.sys.mac.hardware/52079
>     mini_newsgroups/comp.sys.mac.hardware/50551
>     mini_newsgroups/comp.sys.mac.hardware/51799
>     mini_newsgroups/comp.sys.mac.hardware/52214
>     mini_newsgroups/comp.sys.mac.hardware/51750
>     mini_newsgroups/comp.sys.mac.hardware/51626
>     mini_newsgroups/comp.sys.mac.hardware/52223
>     mini_newsgroups/comp.sys.mac.hardware/51652
>     mini_newsgroups/comp.sys.mac.hardware/51832
>     mini_newsgroups/comp.sys.mac.hardware/52037
>     mini_newsgroups/comp.sys.mac.hardware/52163
>     mini_newsgroups/comp.sys.mac.hardware/51790
>     mini_newsgroups/comp.sys.mac.hardware/51782
>     mini_newsgroups/comp.sys.mac.hardware/52149
>     mini_newsgroups/comp.sys.mac.hardware/52071
>     mini_newsgroups/comp.sys.mac.hardware/52010
>     mini_newsgroups/comp.sys.mac.hardware/51808
>     mini_newsgroups/comp.sys.mac.hardware/52404
>     mini_newsgroups/comp.sys.mac.hardware/51595
>     mini_newsgroups/comp.sys.mac.hardware/51943
>     mini_newsgroups/comp.sys.mac.hardware/52234
>     mini_newsgroups/comp.sys.mac.hardware/50440
>     mini_newsgroups/comp.sys.mac.hardware/51770
>     mini_newsgroups/comp.sys.mac.hardware/51503
>     mini_newsgroups/comp.sys.mac.hardware/52081
>     mini_newsgroups/comp.sys.mac.hardware/51847
>     mini_newsgroups/comp.sys.mac.hardware/52045
>     mini_newsgroups/comp.sys.mac.hardware/52248
>     mini_newsgroups/comp.sys.mac.hardware/51929
>     mini_newsgroups/comp.sys.mac.hardware/52050
>     mini_newsgroups/comp.sys.mac.hardware/50518
>     mini_newsgroups/comp.sys.mac.hardware/51587
>     mini_newsgroups/comp.sys.mac.hardware/51675
>     mini_newsgroups/comp.sys.mac.hardware/51514
>     mini_newsgroups/comp.sys.mac.hardware/51509
>     mini_newsgroups/comp.sys.mac.hardware/51720
>     mini_newsgroups/comp.sys.mac.hardware/51908
>     mini_newsgroups/comp.sys.mac.hardware/52039
>     mini_newsgroups/comp.windows.x/
>     mini_newsgroups/comp.windows.x/67063
>     mini_newsgroups/comp.windows.x/66893
>     mini_newsgroups/comp.windows.x/67172
>     mini_newsgroups/comp.windows.x/67386
>     mini_newsgroups/comp.windows.x/66918
>     mini_newsgroups/comp.windows.x/67973
>     mini_newsgroups/comp.windows.x/67016
>     mini_newsgroups/comp.windows.x/66456
>     mini_newsgroups/comp.windows.x/67995
>     mini_newsgroups/comp.windows.x/68311
>     mini_newsgroups/comp.windows.x/67981
>     mini_newsgroups/comp.windows.x/67260
>     mini_newsgroups/comp.windows.x/67061
>     mini_newsgroups/comp.windows.x/68232
>     mini_newsgroups/comp.windows.x/67346
>     mini_newsgroups/comp.windows.x/67220
>     mini_newsgroups/comp.windows.x/68239
>     mini_newsgroups/comp.windows.x/66941
>     mini_newsgroups/comp.windows.x/66437
>     mini_newsgroups/comp.windows.x/67178
>     mini_newsgroups/comp.windows.x/67030
>     mini_newsgroups/comp.windows.x/66889
>     mini_newsgroups/comp.windows.x/67282
>     mini_newsgroups/comp.windows.x/68137
>     mini_newsgroups/comp.windows.x/66931
>     mini_newsgroups/comp.windows.x/67306
>     mini_newsgroups/comp.windows.x/67467
>     mini_newsgroups/comp.windows.x/67402
>     mini_newsgroups/comp.windows.x/68012
>     mini_newsgroups/comp.windows.x/68019
>     mini_newsgroups/comp.windows.x/67212
>     mini_newsgroups/comp.windows.x/66986
>     mini_newsgroups/comp.windows.x/67164
>     mini_newsgroups/comp.windows.x/67269
>     mini_newsgroups/comp.windows.x/68047
>     mini_newsgroups/comp.windows.x/67417
>     mini_newsgroups/comp.windows.x/66993
>     mini_newsgroups/comp.windows.x/66911
>     mini_newsgroups/comp.windows.x/67383
>     mini_newsgroups/comp.windows.x/66400
>     mini_newsgroups/comp.windows.x/67078
>     mini_newsgroups/comp.windows.x/67305
>     mini_newsgroups/comp.windows.x/66445
>     mini_newsgroups/comp.windows.x/66944
>     mini_newsgroups/comp.windows.x/67270
>     mini_newsgroups/comp.windows.x/68243
>     mini_newsgroups/comp.windows.x/67540
>     mini_newsgroups/comp.windows.x/66427
>     mini_newsgroups/comp.windows.x/67193
>     mini_newsgroups/comp.windows.x/67171
>     mini_newsgroups/comp.windows.x/67284
>     mini_newsgroups/comp.windows.x/67514
>     mini_newsgroups/comp.windows.x/66981
>     mini_newsgroups/comp.windows.x/67116
>     mini_newsgroups/comp.windows.x/67572
>     mini_newsgroups/comp.windows.x/67449
>     mini_newsgroups/comp.windows.x/67343
>     mini_newsgroups/comp.windows.x/66421
>     mini_newsgroups/comp.windows.x/66420
>     mini_newsgroups/comp.windows.x/67055
>     mini_newsgroups/comp.windows.x/67070
>     mini_newsgroups/comp.windows.x/67380
>     mini_newsgroups/comp.windows.x/67378
>     mini_newsgroups/comp.windows.x/67319
>     mini_newsgroups/comp.windows.x/66905
>     mini_newsgroups/comp.windows.x/66950
>     mini_newsgroups/comp.windows.x/66955
>     mini_newsgroups/comp.windows.x/68002
>     mini_newsgroups/comp.windows.x/67379
>     mini_newsgroups/comp.windows.x/67983
>     mini_newsgroups/comp.windows.x/66465
>     mini_newsgroups/comp.windows.x/67448
>     mini_newsgroups/comp.windows.x/66467
>     mini_newsgroups/comp.windows.x/67516
>     mini_newsgroups/comp.windows.x/67185
>     mini_newsgroups/comp.windows.x/68185
>     mini_newsgroups/comp.windows.x/67486
>     mini_newsgroups/comp.windows.x/66413
>     mini_newsgroups/comp.windows.x/66980
>     mini_newsgroups/comp.windows.x/66964
>     mini_newsgroups/comp.windows.x/67170
>     mini_newsgroups/comp.windows.x/67491
>     mini_newsgroups/comp.windows.x/68110
>     mini_newsgroups/comp.windows.x/66438
>     mini_newsgroups/comp.windows.x/67542
>     mini_newsgroups/comp.windows.x/67320
>     mini_newsgroups/comp.windows.x/67137
>     mini_newsgroups/comp.windows.x/67052
>     mini_newsgroups/comp.windows.x/68237
>     mini_newsgroups/comp.windows.x/67081
>     mini_newsgroups/comp.windows.x/68174
>     mini_newsgroups/comp.windows.x/67036
>     mini_newsgroups/comp.windows.x/64830
>     mini_newsgroups/comp.windows.x/66943
>     mini_newsgroups/comp.windows.x/67140
>     mini_newsgroups/comp.windows.x/66978
>     mini_newsgroups/comp.windows.x/66453
>     mini_newsgroups/comp.windows.x/68228
>     mini_newsgroups/comp.windows.x/67297
>     mini_newsgroups/comp.windows.x/67435
>     mini_newsgroups/misc.forsale/
>     mini_newsgroups/misc.forsale/74801
>     mini_newsgroups/misc.forsale/75941
>     mini_newsgroups/misc.forsale/76499
>     mini_newsgroups/misc.forsale/76460
>     mini_newsgroups/misc.forsale/76937
>     mini_newsgroups/misc.forsale/76299
>     mini_newsgroups/misc.forsale/70337
>     mini_newsgroups/misc.forsale/76927
>     mini_newsgroups/misc.forsale/76287
>     mini_newsgroups/misc.forsale/76062
>     mini_newsgroups/misc.forsale/76483
>     mini_newsgroups/misc.forsale/74745
>     ... skipped 28325 bytes ...
>     mini_newsgroups/sci.med/59212
>     mini_newsgroups/sci.med/59161
>     mini_newsgroups/sci.med/58951
>     mini_newsgroups/sci.med/58852
>     mini_newsgroups/sci.med/59218
>     mini_newsgroups/sci.med/59197
>     mini_newsgroups/sci.med/59001
>     mini_newsgroups/sci.med/59368
>     mini_newsgroups/sci.med/59111
>     mini_newsgroups/sci.space/
>     mini_newsgroups/sci.space/60821
>     mini_newsgroups/sci.space/61455
>     mini_newsgroups/sci.space/61087
>     mini_newsgroups/sci.space/61027
>     mini_newsgroups/sci.space/61277
>     mini_newsgroups/sci.space/60191
>     mini_newsgroups/sci.space/61401
>     mini_newsgroups/sci.space/61145
>     mini_newsgroups/sci.space/61335
>     mini_newsgroups/sci.space/60960
>     mini_newsgroups/sci.space/61440
>     mini_newsgroups/sci.space/61230
>     mini_newsgroups/sci.space/61038
>     mini_newsgroups/sci.space/61276
>     mini_newsgroups/sci.space/60937
>     mini_newsgroups/sci.space/60843
>     mini_newsgroups/sci.space/61189
>     mini_newsgroups/sci.space/62408
>     mini_newsgroups/sci.space/62480
>     mini_newsgroups/sci.space/60925
>     mini_newsgroups/sci.space/60976
>     mini_newsgroups/sci.space/61256
>     mini_newsgroups/sci.space/60962
>     mini_newsgroups/sci.space/61171
>     mini_newsgroups/sci.space/61293
>     mini_newsgroups/sci.space/61546
>     mini_newsgroups/sci.space/61352
>     mini_newsgroups/sci.space/61009
>     mini_newsgroups/sci.space/62477
>     mini_newsgroups/sci.space/61371
>     mini_newsgroups/sci.space/62398
>     mini_newsgroups/sci.space/61363
>     mini_newsgroups/sci.space/60243
>     mini_newsgroups/sci.space/60942
>     mini_newsgroups/sci.space/61461
>     mini_newsgroups/sci.space/60993
>     mini_newsgroups/sci.space/60946
>     mini_newsgroups/sci.space/61236
>     mini_newsgroups/sci.space/60237
>     mini_newsgroups/sci.space/60840
>     mini_newsgroups/sci.space/61484
>     mini_newsgroups/sci.space/60929
>     mini_newsgroups/sci.space/61316
>     mini_newsgroups/sci.space/60995
>     mini_newsgroups/sci.space/61505
>     mini_newsgroups/sci.space/61154
>     mini_newsgroups/sci.space/61271
>     mini_newsgroups/sci.space/62319
>     mini_newsgroups/sci.space/60950
>     mini_newsgroups/sci.space/62428
>     mini_newsgroups/sci.space/61344
>     mini_newsgroups/sci.space/60822
>     mini_newsgroups/sci.space/60229
>     mini_newsgroups/sci.space/61017
>     mini_newsgroups/sci.space/61353
>     mini_newsgroups/sci.space/61215
>     mini_newsgroups/sci.space/61459
>     mini_newsgroups/sci.space/60834
>     mini_newsgroups/sci.space/61324
>     mini_newsgroups/sci.space/61165
>     mini_newsgroups/sci.space/61404
>     mini_newsgroups/sci.space/61558
>     mini_newsgroups/sci.space/61160
>     mini_newsgroups/sci.space/61118
>     mini_newsgroups/sci.space/60827
>     mini_newsgroups/sci.space/60222
>     mini_newsgroups/sci.space/61136
>     mini_newsgroups/sci.space/60171
>     mini_newsgroups/sci.space/61180
>     mini_newsgroups/sci.space/61532
>     mini_newsgroups/sci.space/61224
>     mini_newsgroups/sci.space/61272
>     mini_newsgroups/sci.space/60913
>     mini_newsgroups/sci.space/60944
>     mini_newsgroups/sci.space/61253
>     mini_newsgroups/sci.space/60941
>     mini_newsgroups/sci.space/59848
>     mini_newsgroups/sci.space/61046
>     mini_newsgroups/sci.space/61362
>     mini_newsgroups/sci.space/61187
>     mini_newsgroups/sci.space/61205
>     mini_newsgroups/sci.space/60181
>     mini_newsgroups/sci.space/61262
>     mini_newsgroups/sci.space/61208
>     mini_newsgroups/sci.space/61265
>     mini_newsgroups/sci.space/60154
>     mini_newsgroups/sci.space/61106
>     mini_newsgroups/sci.space/61192
>     mini_newsgroups/sci.space/60972
>     mini_newsgroups/sci.space/60794
>     mini_newsgroups/sci.space/60804
>     mini_newsgroups/sci.space/61057
>     mini_newsgroups/sci.space/61318
>     mini_newsgroups/sci.space/59904
>     mini_newsgroups/sci.space/61191
>     mini_newsgroups/sci.space/61450
>     mini_newsgroups/sci.space/61051
>     mini_newsgroups/sci.space/61534
>     mini_newsgroups/sci.space/61066
>     mini_newsgroups/sci.space/61431
>     mini_newsgroups/soc.religion.christian/
>     mini_newsgroups/soc.religion.christian/20736
>     mini_newsgroups/soc.religion.christian/20801
>     mini_newsgroups/soc.religion.christian/20674
>     mini_newsgroups/soc.religion.christian/20896
>     mini_newsgroups/soc.religion.christian/21319
>     mini_newsgroups/soc.religion.christian/21672
>     mini_newsgroups/soc.religion.christian/20812
>     mini_newsgroups/soc.religion.christian/21451
>     mini_newsgroups/soc.religion.christian/20947
>     mini_newsgroups/soc.religion.christian/20850
>     mini_newsgroups/soc.religion.christian/20744
>     mini_newsgroups/soc.religion.christian/20774
>     mini_newsgroups/soc.religion.christian/21696
>     mini_newsgroups/soc.religion.christian/21419
>     mini_newsgroups/soc.religion.christian/20899
>     mini_newsgroups/soc.religion.christian/20710
>     mini_newsgroups/soc.religion.christian/21339
>     mini_newsgroups/soc.religion.christian/20629
>     mini_newsgroups/soc.religion.christian/21373
>     mini_newsgroups/soc.religion.christian/20743
>     mini_newsgroups/soc.religion.christian/20738
>     mini_newsgroups/soc.religion.christian/20571
>     mini_newsgroups/soc.religion.christian/20742
>     mini_newsgroups/soc.religion.christian/21329
>     mini_newsgroups/soc.religion.christian/21481
>     mini_newsgroups/soc.religion.christian/20634
>     mini_newsgroups/soc.religion.christian/21334
>     mini_newsgroups/soc.religion.christian/20866
>     mini_newsgroups/soc.religion.christian/20886
>     mini_newsgroups/soc.religion.christian/21578
>     mini_newsgroups/soc.religion.christian/20724
>     mini_newsgroups/soc.religion.christian/21453
>     mini_newsgroups/soc.religion.christian/20511
>     mini_newsgroups/soc.religion.christian/20800
>     mini_newsgroups/soc.religion.christian/20491
>     mini_newsgroups/soc.religion.christian/21784
>     mini_newsgroups/soc.religion.christian/20657
>     mini_newsgroups/soc.religion.christian/20976
>     mini_newsgroups/soc.religion.christian/21799
>     mini_newsgroups/soc.religion.christian/21407
>     mini_newsgroups/soc.religion.christian/21658
>     mini_newsgroups/soc.religion.christian/21777
>     mini_newsgroups/soc.religion.christian/21754
>     mini_newsgroups/soc.religion.christian/21559
>     mini_newsgroups/soc.religion.christian/20799
>     mini_newsgroups/soc.religion.christian/21800
>     mini_newsgroups/soc.religion.christian/20621
>     mini_newsgroups/soc.religion.christian/21648
>     mini_newsgroups/soc.religion.christian/20914
>     mini_newsgroups/soc.religion.christian/21396
>     mini_newsgroups/soc.religion.christian/20540
>     mini_newsgroups/soc.religion.christian/21558
>     mini_newsgroups/soc.religion.christian/21621
>     mini_newsgroups/soc.religion.christian/20965
>     mini_newsgroups/soc.religion.christian/21788
>     mini_newsgroups/soc.religion.christian/21505
>     mini_newsgroups/soc.religion.christian/20936
>     mini_newsgroups/soc.religion.christian/21580
>     mini_newsgroups/soc.religion.christian/21585
>     mini_newsgroups/soc.religion.christian/21699
>     mini_newsgroups/soc.religion.christian/21531
>     mini_newsgroups/soc.religion.christian/20689
>     mini_newsgroups/soc.religion.christian/21382
>     mini_newsgroups/soc.religion.christian/21773
>     mini_newsgroups/soc.religion.christian/20952
>     mini_newsgroups/soc.religion.christian/21493
>     mini_newsgroups/soc.religion.christian/20900
>     mini_newsgroups/soc.religion.christian/21418
>     mini_newsgroups/soc.religion.christian/20867
>     mini_newsgroups/soc.religion.christian/21761
>     mini_newsgroups/soc.religion.christian/20779
>     mini_newsgroups/soc.religion.christian/20503
>     mini_newsgroups/soc.religion.christian/21522
>     mini_newsgroups/soc.religion.christian/20767
>     mini_newsgroups/soc.religion.christian/21663
>     mini_newsgroups/soc.religion.christian/21709
>     mini_newsgroups/soc.religion.christian/21535
>     mini_newsgroups/soc.religion.christian/21702
>     mini_newsgroups/soc.religion.christian/21597
>     mini_newsgroups/soc.religion.christian/20719
>     mini_newsgroups/soc.religion.christian/21529
>     mini_newsgroups/soc.religion.christian/20603
>     mini_newsgroups/soc.religion.christian/20664
>     mini_newsgroups/soc.religion.christian/20960
>     mini_newsgroups/soc.religion.christian/20898
>     mini_newsgroups/soc.religion.christian/20798
>     mini_newsgroups/soc.religion.christian/20637
>     mini_newsgroups/soc.religion.christian/20602
>     mini_newsgroups/soc.religion.christian/20554
>     mini_newsgroups/soc.religion.christian/21618
>     mini_newsgroups/soc.religion.christian/21698
>     mini_newsgroups/soc.religion.christian/21544
>     mini_newsgroups/soc.religion.christian/21708
>     mini_newsgroups/soc.religion.christian/21524
>     mini_newsgroups/soc.religion.christian/21342
>     mini_newsgroups/soc.religion.christian/20890
>     mini_newsgroups/soc.religion.christian/20811
>     mini_newsgroups/soc.religion.christian/20626
>     mini_newsgroups/soc.religion.christian/20506
>     mini_newsgroups/soc.religion.christian/21804
>     mini_newsgroups/talk.politics.guns/
>     mini_newsgroups/talk.politics.guns/54196
>     mini_newsgroups/talk.politics.guns/54303
>     mini_newsgroups/talk.politics.guns/54117
>     mini_newsgroups/talk.politics.guns/54402
>     mini_newsgroups/talk.politics.guns/54843
>     mini_newsgroups/talk.politics.guns/54200
>     mini_newsgroups/talk.politics.guns/54630
>     mini_newsgroups/talk.politics.guns/54616
>     mini_newsgroups/talk.politics.guns/54592
>     mini_newsgroups/talk.politics.guns/54697
>     mini_newsgroups/talk.politics.guns/53302
>     mini_newsgroups/talk.politics.guns/54323
>     mini_newsgroups/talk.politics.guns/54169
>     mini_newsgroups/talk.politics.guns/54877
>     mini_newsgroups/talk.politics.guns/55115
>     mini_newsgroups/talk.politics.guns/54230
>     mini_newsgroups/talk.politics.guns/54138
>     mini_newsgroups/talk.politics.guns/54637
>     mini_newsgroups/talk.politics.guns/53373
>     mini_newsgroups/talk.politics.guns/54312
>     mini_newsgroups/talk.politics.guns/55073
>     mini_newsgroups/talk.politics.guns/54417
>     mini_newsgroups/talk.politics.guns/55468
>     mini_newsgroups/talk.politics.guns/54875
>     mini_newsgroups/talk.politics.guns/54590
>     mini_newsgroups/talk.politics.guns/54861
>     mini_newsgroups/talk.politics.guns/54297
>     mini_newsgroups/talk.politics.guns/55484
>     mini_newsgroups/talk.politics.guns/55063
>     mini_newsgroups/talk.politics.guns/54302
>     mini_newsgroups/talk.politics.guns/55123
>     mini_newsgroups/talk.politics.guns/55264
>     mini_newsgroups/talk.politics.guns/54956
>     mini_newsgroups/talk.politics.guns/55470
>     mini_newsgroups/talk.politics.guns/53328
>     mini_newsgroups/talk.politics.guns/54660
>     mini_newsgroups/talk.politics.guns/53304
>     mini_newsgroups/talk.politics.guns/54570
>     mini_newsgroups/talk.politics.guns/53329
>     mini_newsgroups/talk.politics.guns/54715
>     mini_newsgroups/talk.politics.guns/54429
>     mini_newsgroups/talk.politics.guns/54248
>     mini_newsgroups/talk.politics.guns/54243
>     mini_newsgroups/talk.politics.guns/54452
>     mini_newsgroups/talk.politics.guns/53358
>     mini_newsgroups/talk.politics.guns/53348
>     mini_newsgroups/talk.politics.guns/54479
>     mini_newsgroups/talk.politics.guns/54416
>     mini_newsgroups/talk.politics.guns/54279
>     mini_newsgroups/talk.politics.guns/54659
>     mini_newsgroups/talk.politics.guns/54728
>     mini_newsgroups/talk.politics.guns/54447
>     mini_newsgroups/talk.politics.guns/55080
>     mini_newsgroups/talk.politics.guns/54675
>     mini_newsgroups/talk.politics.guns/54239
>     mini_newsgroups/talk.politics.guns/54518
>     mini_newsgroups/talk.politics.guns/54342
>     mini_newsgroups/talk.politics.guns/54591
>     mini_newsgroups/talk.politics.guns/53325
>     mini_newsgroups/talk.politics.guns/54726
>     mini_newsgroups/talk.politics.guns/54357
>     mini_newsgroups/talk.politics.guns/54395
>     mini_newsgroups/talk.politics.guns/54276
>     mini_newsgroups/talk.politics.guns/55106
>     mini_newsgroups/talk.politics.guns/55231
>     mini_newsgroups/talk.politics.guns/54714
>     mini_newsgroups/talk.politics.guns/54611
>     mini_newsgroups/talk.politics.guns/54450
>     mini_newsgroups/talk.politics.guns/54634
>     mini_newsgroups/talk.politics.guns/55068
>     mini_newsgroups/talk.politics.guns/54164
>     mini_newsgroups/talk.politics.guns/54560
>     mini_newsgroups/talk.politics.guns/54446
>     mini_newsgroups/talk.politics.guns/53369
>     mini_newsgroups/talk.politics.guns/55116
>     mini_newsgroups/talk.politics.guns/54538
>     mini_newsgroups/talk.politics.guns/54469
>     mini_newsgroups/talk.politics.guns/54633
>     mini_newsgroups/talk.politics.guns/54860
>     mini_newsgroups/talk.politics.guns/55036
>     mini_newsgroups/talk.politics.guns/55278
>     mini_newsgroups/talk.politics.guns/54535
>     mini_newsgroups/talk.politics.guns/54211
>     mini_newsgroups/talk.politics.guns/55060
>     mini_newsgroups/talk.politics.guns/54404
>     mini_newsgroups/talk.politics.guns/54698
>     mini_newsgroups/talk.politics.guns/54322
>     mini_newsgroups/talk.politics.guns/54748
>     mini_newsgroups/talk.politics.guns/55260
>     mini_newsgroups/talk.politics.guns/55489
>     mini_newsgroups/talk.politics.guns/54238
>     mini_newsgroups/talk.politics.guns/54152
>     mini_newsgroups/talk.politics.guns/54154
>     mini_newsgroups/talk.politics.guns/55239
>     mini_newsgroups/talk.politics.guns/55249
>     mini_newsgroups/talk.politics.guns/54433
>     mini_newsgroups/talk.politics.guns/54449
>     mini_newsgroups/talk.politics.guns/54626
>     mini_newsgroups/talk.politics.guns/54586
>     mini_newsgroups/talk.politics.guns/54624
>     mini_newsgroups/talk.politics.mideast/
>     mini_newsgroups/talk.politics.mideast/76504
>     mini_newsgroups/talk.politics.mideast/76424
>     mini_newsgroups/talk.politics.mideast/76020
>     mini_newsgroups/talk.politics.mideast/77815
>     mini_newsgroups/talk.politics.mideast/77387
>     mini_newsgroups/talk.politics.mideast/75972
>     mini_newsgroups/talk.politics.mideast/76401
>     mini_newsgroups/talk.politics.mideast/76222
>     mini_newsgroups/talk.politics.mideast/76403
>     mini_newsgroups/talk.politics.mideast/76285
>     mini_newsgroups/talk.politics.mideast/75388
>     mini_newsgroups/talk.politics.mideast/76154
>     mini_newsgroups/talk.politics.mideast/76120
>     mini_newsgroups/talk.politics.mideast/76113
>     mini_newsgroups/talk.politics.mideast/76322
>     mini_newsgroups/talk.politics.mideast/76014
>     mini_newsgroups/talk.politics.mideast/76516
>     mini_newsgroups/talk.politics.mideast/75895
>     mini_newsgroups/talk.politics.mideast/75913
>     mini_newsgroups/talk.politics.mideast/76500
>     mini_newsgroups/talk.politics.mideast/77232
>     mini_newsgroups/talk.politics.mideast/75982
>     mini_newsgroups/talk.politics.mideast/76542
>     mini_newsgroups/talk.politics.mideast/77288
>     mini_newsgroups/talk.politics.mideast/77275
>     mini_newsgroups/talk.politics.mideast/76435
>     mini_newsgroups/talk.politics.mideast/75942
>     mini_newsgroups/talk.politics.mideast/75976
>     mini_newsgroups/talk.politics.mideast/76284
>     mini_newsgroups/talk.politics.mideast/77235
>     mini_newsgroups/talk.politics.mideast/77332
>     mini_newsgroups/talk.politics.mideast/75916
>     mini_newsgroups/talk.politics.mideast/76389
>     mini_newsgroups/talk.politics.mideast/75966
>     mini_newsgroups/talk.politics.mideast/75910
>     mini_newsgroups/talk.politics.mideast/76320
>     mini_newsgroups/talk.politics.mideast/76369
>     mini_newsgroups/talk.politics.mideast/76495
>     mini_newsgroups/talk.politics.mideast/77272
>     mini_newsgroups/talk.politics.mideast/75918
>     mini_newsgroups/talk.politics.mideast/75920
>     mini_newsgroups/talk.politics.mideast/77383
>     mini_newsgroups/talk.politics.mideast/76456
>     mini_newsgroups/talk.politics.mideast/75952
>     mini_newsgroups/talk.politics.mideast/76213
>     mini_newsgroups/talk.politics.mideast/76062
>     mini_newsgroups/talk.politics.mideast/76205
>     mini_newsgroups/talk.politics.mideast/75917
>     mini_newsgroups/talk.politics.mideast/75979
>     mini_newsgroups/talk.politics.mideast/76242
>     mini_newsgroups/talk.politics.mideast/76548
>     mini_newsgroups/talk.politics.mideast/75956
>     mini_newsgroups/talk.politics.mideast/76557
>     mini_newsgroups/talk.politics.mideast/76277
>     mini_newsgroups/talk.politics.mideast/76121
>     mini_newsgroups/talk.politics.mideast/76416
>     mini_newsgroups/talk.politics.mideast/75396
>     mini_newsgroups/talk.politics.mideast/76073
>     mini_newsgroups/talk.politics.mideast/77250
>     mini_newsgroups/talk.politics.mideast/76160
>     mini_newsgroups/talk.politics.mideast/75929
>     mini_newsgroups/talk.politics.mideast/77218
>     mini_newsgroups/talk.politics.mideast/76517
>     mini_newsgroups/talk.politics.mideast/76501
>     mini_newsgroups/talk.politics.mideast/75938
>     mini_newsgroups/talk.politics.mideast/76444
>     mini_newsgroups/talk.politics.mideast/76068
>     mini_newsgroups/talk.politics.mideast/76398
>     mini_newsgroups/talk.politics.mideast/76271
>     mini_newsgroups/talk.politics.mideast/76047
>     mini_newsgroups/talk.politics.mideast/76458
>     mini_newsgroups/talk.politics.mideast/76372
>     mini_newsgroups/talk.politics.mideast/75943
>     mini_newsgroups/talk.politics.mideast/76166
>     mini_newsgroups/talk.politics.mideast/77813
>     mini_newsgroups/talk.politics.mideast/76410
>     mini_newsgroups/talk.politics.mideast/76374
>     mini_newsgroups/talk.politics.mideast/75369
>     mini_newsgroups/talk.politics.mideast/76508
>     mini_newsgroups/talk.politics.mideast/77212
>     mini_newsgroups/talk.politics.mideast/76249
>     mini_newsgroups/talk.politics.mideast/76233
>     mini_newsgroups/talk.politics.mideast/77305
>     mini_newsgroups/talk.politics.mideast/76082
>     mini_newsgroups/talk.politics.mideast/77177
>     mini_newsgroups/talk.politics.mideast/75875
>     mini_newsgroups/talk.politics.mideast/77203
>     mini_newsgroups/talk.politics.mideast/75901
>     mini_newsgroups/talk.politics.mideast/76080
>     mini_newsgroups/talk.politics.mideast/77392
>     mini_newsgroups/talk.politics.mideast/76498
>     mini_newsgroups/talk.politics.mideast/76105
>     mini_newsgroups/talk.politics.mideast/76179
>     mini_newsgroups/talk.politics.mideast/76197
>     mini_newsgroups/talk.politics.mideast/77322
>     mini_newsgroups/talk.politics.mideast/76544
>     mini_newsgroups/talk.politics.mideast/75394
>     mini_newsgroups/talk.politics.mideast/75963
>     mini_newsgroups/talk.politics.mideast/76152
>     mini_newsgroups/talk.politics.mideast/76395
>     mini_newsgroups/talk.politics.misc/
>     mini_newsgroups/talk.politics.misc/178813
>     mini_newsgroups/talk.politics.misc/176916
>     mini_newsgroups/talk.politics.misc/178862
>     mini_newsgroups/talk.politics.misc/178661
>     mini_newsgroups/talk.politics.misc/178433
>     mini_newsgroups/talk.politics.misc/178327
>     mini_newsgroups/talk.politics.misc/176884
>     mini_newsgroups/talk.politics.misc/178865
>     mini_newsgroups/talk.politics.misc/178656
>     mini_newsgroups/talk.politics.misc/178631
>     mini_newsgroups/talk.politics.misc/176878
>     mini_newsgroups/talk.politics.misc/178775
>     mini_newsgroups/talk.politics.misc/176986
>     mini_newsgroups/talk.politics.misc/178564
>     mini_newsgroups/talk.politics.misc/176869
>     mini_newsgroups/talk.politics.misc/178390
>     mini_newsgroups/talk.politics.misc/176984
>     mini_newsgroups/talk.politics.misc/178745
>     mini_newsgroups/talk.politics.misc/178994
>     mini_newsgroups/talk.politics.misc/176895
>     mini_newsgroups/talk.politics.misc/178789
>     mini_newsgroups/talk.politics.misc/178678
>     mini_newsgroups/talk.politics.misc/178887
>     mini_newsgroups/talk.politics.misc/178945
>     mini_newsgroups/talk.politics.misc/178997
>     mini_newsgroups/talk.politics.misc/178788
>     mini_newsgroups/talk.politics.misc/178532
>     mini_newsgroups/talk.politics.misc/176926
>     mini_newsgroups/talk.politics.misc/178718
>     mini_newsgroups/talk.politics.misc/178824
>     mini_newsgroups/talk.politics.misc/178907
>     mini_newsgroups/talk.politics.misc/178569
>     mini_newsgroups/talk.politics.misc/178960
>     mini_newsgroups/talk.politics.misc/178765
>     mini_newsgroups/talk.politics.misc/178870
>     mini_newsgroups/talk.politics.misc/178489
>     mini_newsgroups/talk.politics.misc/178738
>     mini_newsgroups/talk.politics.misc/176904
>     mini_newsgroups/talk.politics.misc/178566
>     mini_newsgroups/talk.politics.misc/176930
>     mini_newsgroups/talk.politics.misc/176983
>     mini_newsgroups/talk.politics.misc/178682
>     mini_newsgroups/talk.politics.misc/178301
>     mini_newsgroups/talk.politics.misc/176956
>     mini_newsgroups/talk.politics.misc/179066
>     mini_newsgroups/talk.politics.misc/178368
>     mini_newsgroups/talk.politics.misc/178382
>     mini_newsgroups/talk.politics.misc/178906
>     mini_newsgroups/talk.politics.misc/179070
>     mini_newsgroups/talk.politics.misc/178481
>     mini_newsgroups/talk.politics.misc/178851
>     mini_newsgroups/talk.politics.misc/178799
>     mini_newsgroups/talk.politics.misc/178924
>     mini_newsgroups/talk.politics.misc/178721
>     mini_newsgroups/talk.politics.misc/178451
>     mini_newsgroups/talk.politics.misc/178792
>     mini_newsgroups/talk.politics.misc/176881
>     mini_newsgroups/talk.politics.misc/178654
>     mini_newsgroups/talk.politics.misc/176951
>     mini_newsgroups/talk.politics.misc/178801
>     mini_newsgroups/talk.politics.misc/179018
>     mini_newsgroups/talk.politics.misc/176886
>     mini_newsgroups/talk.politics.misc/178360
>     mini_newsgroups/talk.politics.misc/178699
>     mini_newsgroups/talk.politics.misc/176988
>     mini_newsgroups/talk.politics.misc/179097
>     mini_newsgroups/talk.politics.misc/179067
>     mini_newsgroups/talk.politics.misc/178318
>     mini_newsgroups/talk.politics.misc/178447
>     mini_newsgroups/talk.politics.misc/178455
>     mini_newsgroups/talk.politics.misc/177008
>     mini_newsgroups/talk.politics.misc/178527
>     mini_newsgroups/talk.politics.misc/178337
>     mini_newsgroups/talk.politics.misc/178556
>     mini_newsgroups/talk.politics.misc/178522
>     mini_newsgroups/talk.politics.misc/178517
>     mini_newsgroups/talk.politics.misc/178793
>     mini_newsgroups/talk.politics.misc/178579
>     mini_newsgroups/talk.politics.misc/178837
>     mini_newsgroups/talk.politics.misc/178731
>     mini_newsgroups/talk.politics.misc/178668
>     mini_newsgroups/talk.politics.misc/178939
>     mini_newsgroups/talk.politics.misc/178560
>     mini_newsgroups/talk.politics.misc/178571
>     mini_newsgroups/talk.politics.misc/178487
>     mini_newsgroups/talk.politics.misc/178965
>     mini_newsgroups/talk.politics.misc/178998
>     mini_newsgroups/talk.politics.misc/176982
>     mini_newsgroups/talk.politics.misc/178606
>     mini_newsgroups/talk.politics.misc/178341
>     mini_newsgroups/talk.politics.misc/178622
>     mini_newsgroups/talk.politics.misc/178309
>     mini_newsgroups/talk.politics.misc/178769
>     mini_newsgroups/talk.politics.misc/178927
>     mini_newsgroups/talk.politics.misc/178751
>     mini_newsgroups/talk.politics.misc/178349
>     mini_newsgroups/talk.politics.misc/179095
>     mini_newsgroups/talk.politics.misc/178993
>     mini_newsgroups/talk.politics.misc/178361
>     mini_newsgroups/talk.politics.misc/178610
>     mini_newsgroups/talk.religion.misc/
>     mini_newsgroups/talk.religion.misc/83682
>     mini_newsgroups/talk.religion.misc/83790
>     mini_newsgroups/talk.religion.misc/83751
>     mini_newsgroups/talk.religion.misc/83618
>     mini_newsgroups/talk.religion.misc/83535
>     mini_newsgroups/talk.religion.misc/84068
>     mini_newsgroups/talk.religion.misc/83732
>     mini_newsgroups/talk.religion.misc/83798
>     mini_newsgroups/talk.religion.misc/83564
>     mini_newsgroups/talk.religion.misc/84351
>     mini_newsgroups/talk.religion.misc/84212
>     mini_newsgroups/talk.religion.misc/83601
>     mini_newsgroups/talk.religion.misc/83775
>     mini_newsgroups/talk.religion.misc/83919
>     mini_newsgroups/talk.religion.misc/84355
>     mini_newsgroups/talk.religion.misc/82799
>     mini_newsgroups/talk.religion.misc/83518
>     mini_newsgroups/talk.religion.misc/84053
>     mini_newsgroups/talk.religion.misc/83866
>     mini_newsgroups/talk.religion.misc/83599
>     mini_newsgroups/talk.religion.misc/83788
>     mini_newsgroups/talk.religion.misc/84350
>     mini_newsgroups/talk.religion.misc/83641
>     mini_newsgroups/talk.religion.misc/84316
>     mini_newsgroups/talk.religion.misc/83771
>     mini_newsgroups/talk.religion.misc/83717
>     mini_newsgroups/talk.religion.misc/83645
>     mini_newsgroups/talk.religion.misc/83742
>     mini_newsgroups/talk.religion.misc/83979
>     mini_newsgroups/talk.religion.misc/84356
>     mini_newsgroups/talk.religion.misc/83567
>     mini_newsgroups/talk.religion.misc/84060
>     mini_newsgroups/talk.religion.misc/83843
>     mini_newsgroups/talk.religion.misc/84413
>     mini_newsgroups/talk.religion.misc/83474
>     mini_newsgroups/talk.religion.misc/84510
>     mini_newsgroups/talk.religion.misc/83748
>     mini_newsgroups/talk.religion.misc/83829
>     mini_newsgroups/talk.religion.misc/83548
>     mini_newsgroups/talk.religion.misc/84400
>     mini_newsgroups/talk.religion.misc/83827
>     mini_newsgroups/talk.religion.misc/84302
>     mini_newsgroups/talk.religion.misc/82796
>     mini_newsgroups/talk.religion.misc/84178
>     mini_newsgroups/talk.religion.misc/84398
>     mini_newsgroups/talk.religion.misc/84567
>     mini_newsgroups/talk.religion.misc/84324
>     mini_newsgroups/talk.religion.misc/82812
>     mini_newsgroups/talk.religion.misc/83817
>     mini_newsgroups/talk.religion.misc/83558
>     mini_newsgroups/talk.religion.misc/83683
>     mini_newsgroups/talk.religion.misc/84103
>     mini_newsgroups/talk.religion.misc/83791
>     mini_newsgroups/talk.religion.misc/84115
>     mini_newsgroups/talk.religion.misc/83797
>     mini_newsgroups/talk.religion.misc/83673
>     mini_newsgroups/talk.religion.misc/84255
>     mini_newsgroups/talk.religion.misc/84164
>     mini_newsgroups/talk.religion.misc/83977
>     mini_newsgroups/talk.religion.misc/84359
>     mini_newsgroups/talk.religion.misc/83773
>     mini_newsgroups/talk.religion.misc/84244
>     mini_newsgroups/talk.religion.misc/84194
>     mini_newsgroups/talk.religion.misc/84290
>     mini_newsgroups/talk.religion.misc/84317
>     mini_newsgroups/talk.religion.misc/84251
>     mini_newsgroups/talk.religion.misc/83898
>     mini_newsgroups/talk.religion.misc/83983
>     mini_newsgroups/talk.religion.misc/83807
>     mini_newsgroups/talk.religion.misc/84152
>     mini_newsgroups/talk.religion.misc/83480
>     mini_newsgroups/talk.religion.misc/82804
>     mini_newsgroups/talk.religion.misc/82758
>     mini_newsgroups/talk.religion.misc/83785
>     mini_newsgroups/talk.religion.misc/84309
>     mini_newsgroups/talk.religion.misc/84195
>     mini_newsgroups/talk.religion.misc/83678
>     mini_newsgroups/talk.religion.misc/84293
>     mini_newsgroups/talk.religion.misc/84345
>     mini_newsgroups/talk.religion.misc/83900
>     mini_newsgroups/talk.religion.misc/84120
>     mini_newsgroups/talk.religion.misc/83730
>     mini_newsgroups/talk.religion.misc/83575
>     mini_newsgroups/talk.religion.misc/83719
>     mini_newsgroups/talk.religion.misc/83574
>     mini_newsgroups/talk.religion.misc/84436
>     mini_newsgroups/talk.religion.misc/83861
>     mini_newsgroups/talk.religion.misc/83563
>     mini_newsgroups/talk.religion.misc/84278
>     mini_newsgroups/talk.religion.misc/83780
>     mini_newsgroups/talk.religion.misc/83862
>     mini_newsgroups/talk.religion.misc/83435
>     mini_newsgroups/talk.religion.misc/83763
>     mini_newsgroups/talk.religion.misc/83796
>     mini_newsgroups/talk.religion.misc/84083
>     mini_newsgroups/talk.religion.misc/83437
>     mini_newsgroups/talk.religion.misc/83750
>     mini_newsgroups/talk.religion.misc/83571
>     mini_newsgroups/talk.religion.misc/83981
>     mini_newsgroups/talk.religion.misc/82764

The below cell takes about 10mins to run.

NOTE: It is slow partly because each file is small and we are facing the 'small files problem' with distributed file systems that need meta-data for each file. If the file name is not needed then it may be better to create one large stream of the contents of all the files into dbfs. We leave this as it is to show what happens when we upload a dataset of lots of little files into dbfs.

``` scala
//%fs cp -r file:/tmp/mini_newsgroups dbfs:/datasets/mini_newsgroups
```

>     res8: Boolean = true

``` scala
display(dbutils.fs.ls("dbfs:/datasets/mini_newsgroups"))
```

| path                                                      | name                      | size |
|-----------------------------------------------------------|---------------------------|------|
| dbfs:/datasets/mini\_newsgroups/alt.atheism/              | alt.atheism/              | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.graphics/            | comp.graphics/            | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.os.ms-windows.misc/  | comp.os.ms-windows.misc/  | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.sys.ibm.pc.hardware/ | comp.sys.ibm.pc.hardware/ | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.sys.mac.hardware/    | comp.sys.mac.hardware/    | 0.0  |
| dbfs:/datasets/mini\_newsgroups/comp.windows.x/           | comp.windows.x/           | 0.0  |
| dbfs:/datasets/mini\_newsgroups/misc.forsale/             | misc.forsale/             | 0.0  |
| dbfs:/datasets/mini\_newsgroups/rec.autos/                | rec.autos/                | 0.0  |
| dbfs:/datasets/mini\_newsgroups/rec.motorcycles/          | rec.motorcycles/          | 0.0  |
| dbfs:/datasets/mini\_newsgroups/rec.sport.baseball/       | rec.sport.baseball/       | 0.0  |
| dbfs:/datasets/mini\_newsgroups/rec.sport.hockey/         | rec.sport.hockey/         | 0.0  |
| dbfs:/datasets/mini\_newsgroups/sci.crypt/                | sci.crypt/                | 0.0  |
| dbfs:/datasets/mini\_newsgroups/sci.electronics/          | sci.electronics/          | 0.0  |
| dbfs:/datasets/mini\_newsgroups/sci.med/                  | sci.med/                  | 0.0  |
| dbfs:/datasets/mini\_newsgroups/sci.space/                | sci.space/                | 0.0  |
| dbfs:/datasets/mini\_newsgroups/soc.religion.christian/   | soc.religion.christian/   | 0.0  |
| dbfs:/datasets/mini\_newsgroups/talk.politics.guns/       | talk.politics.guns/       | 0.0  |
| dbfs:/datasets/mini\_newsgroups/talk.politics.mideast/    | talk.politics.mideast/    | 0.0  |
| dbfs:/datasets/mini\_newsgroups/talk.politics.misc/       | talk.politics.misc/       | 0.0  |
| dbfs:/datasets/mini\_newsgroups/talk.religion.misc/       | talk.religion.misc/       | 0.0  |

