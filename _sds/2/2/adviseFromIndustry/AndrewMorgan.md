---
title: SDS-2.2 Advise to Students from London's Big Data Industry
permalink: /sds/2/2/adviseFromIndustry/AndrewMorgan/
sidebar:
  nav: "lMenu-SDS-2.2"
header:
  overlay_color: "#5e616c"
  overlay_image: /assets/images/DISP-SDS-blue-1600x524.jpg
  caption: 
excerpt: 'Scalable Data Science from Atlantis, A Big Data Course in Apache Spark 2.2.<br /><br /><br />{::nomarkdown}<iframe style="display: inline-block;" src="https://ghbtns.com/github-btn.html?user=lamastex&repo=scalable-data-science&type=star&count=true&size=large" frameborder="0" scrolling="0" width="160px" height="30px"></iframe> <iframe style="display: inline-block;" src="https://ghbtns.com/github-btn.html?user=lamastex&repo=scalable-data-science&type=fork&count=true&size=large" frameborder="0" scrolling="0" width="158px" height="30px"></iframe>{:/nomarkdown}'
---

{% include toc %}

# SDS-2.2 -- Advise to Students from London's Big Data Industry -- by Andrew Morgan

**Scalable Data Science from Atlantis**, *A Big Data Course in Apache Spark 2.2*

Welcome! This is [a 2017 Uppsala Big Data Meetup](https://www.meetup.com/Uppsala-Big-Data-Meetup/) of a fast-paced PhD course sequel in data science.

Most lectures are live via HangoutsOnAir in Youtube at [this channel](https://www.youtube.com/channel/UCPJ5ALbDtuCA4DJmN3GvanA) and archived in [this playlist](https://www.youtube.com/playlist?list=PL_I1mOIPmfpawQcs9l1vYfh50RhK_UJfY). We are not set up for live interactions online.

This is a markdown of some resources Andrew Morgan from London'd Data Industry shared with us in a live hangout on air to give us a view from the real furnaces of science engineering and enterprise architectures.

Andrew is one of the co-authors of an excellent resource:
* [https://github.com/PacktPublishing/Mastering-Spark-for-Data-Science](https://github.com/PacktPublishing/Mastering-Spark-for-Data-Science)
* [https://www.amazon.com/Mastering-Spark-Science-Andrew-Morgan-ebook/dp/B01BWNXA82](https://www.amazon.com/Mastering-Spark-Science-Andrew-Morgan-ebook/dp/B01BWNXA82)

![Andrew Morgan's Diagram on Critical Data Exploitation Capabilities](https://github.com/lamastex/scalable-data-science/raw/master/_sds/2/2/adviseFromIndustry/AndrewMorgan/PNGs/shotOf_A3_ByteSumo_CriticalDataExploitationCapabilities_1_0_4_8.png)

For details see an A3-seized PDF of [Andrew Morgan's Diagram on Critical Data Exploitation Capabilities](https://github.com/lamastex/scalable-data-science/raw/master/_sds/2/2/adviseFromIndustry/AndrewMorgan/PDFs/A3_ByteSumo_CriticalDataExploitationCapabilities_1_0_4_8.pdf) (the above image).

# Videos of the Hang-out with Andrew Morgan

* [![Hangout with Andrew Morgan of ByteSumo from Lond's Big Data Industry - Part 1](http://img.youtube.com/vi/qZd-DKPKdeM/0.jpg)](https://www.youtube.com/watch?v=qZd-DKPKdeM&rel=0&autoplay=1&modestbranding=1&start=1)
* [![Hangout with Andrew Morgan of ByteSumo from Lond's Big Data Industry - Part 1](http://img.youtube.com/vi/Bp4vtT4h5m8/0.jpg)](https://www.youtube.com/watch?v=Bp4vtT4h5m8&rel=0&autoplay=1&modestbranding=1&start=1)

Here are some follow-up questions by students that occured off-line for others to absorb.

# Question on PMML from Kasper Ramström:


Kasper: You mentioned PMML and something about transfering models from a non-scalable python-ish environment to a scalable and production ready java/scala/similar environment. Did I understand that correctly? And if so, is there a point to learn spark or other scalable data science tools or frameworks if you can just use PMML’s instead? I get a feeling that I’m missing something here?

# Answer from Andrew Morgan


Andres: In short - PMML is effective but VERY limited. 
It is not a Spark Killer. Big Data Science is never going away. 
Tools like Apache Spark are only becoming increasingly important.
Keep learning Spark. 
Look at the PDF below !! click the links!!

The long answer requires some background information. Apologies this is quite long.

## The architecture behind PMML:

Several years ago AirBnB published the following article which was very influential in how to practically get machine learning models into production via PMML.

Here's the article - you should read it.

[https://medium.com/airbnb-engineering/architecting-a-machine-learning-system-for-risk-941abbba5a60](https://medium.com/airbnb-engineering/architecting-a-machine-learning-system-for-risk-941abbba5a60).

It's worth reviewing the following OpenScoring documentation too -- to understand the ideas behind the methods in openscoring: 
[http://openscoring.io/](http://openscoring.io/).


### The point is: 

AirBnB are describing an architecture for a certain class of problems, which I will categorise as **smallish machine learning problems**.

It's a very slick solution for this class of problem.  
No costs to go from prototype to production. 
On [Andrew's diagram on Critical Data Exploitation Capabilities](PDFs/A3_ByteSumo_CriticalDataExploitationCapabilities_1_0_4_8.pdf) that is also displayed in the bottom of this page, you may recall that as being a major part of the lifecycle.

*ALSO NOTE*: A side benefit is you can charge your customers on PAYG contracts for PMML scores (meaning people wanting predictions can pay by the API call). 
PMML APIs can be also backed by a commercial model too.

The end result is it means you don't have to have expensive data scientists, or expensive data engineers. 
It's very cost effective. 
You can bill your end users. 
A very tidy solution.

However, it is very deficient for lots of reasons too.

* Like, it doesn’t allow for complex feature building prior to model scoring (Fourier series transforms for example).
* The list of things it cannot do is large. It also doesn’t work for 
  * deep learning (no GPUs)
  * graph problems
  * unstructured data problems - video / voice 
  * NLP problems - chatbot / A.I. / watson style solutions
  * matrix factorisation problems (recommendation engines)
  * geographic problems
  * modelling or simulation workloads
  * RNN / LSTM learning problems

Because it doesn't do these things, it can be said to be a narrow and thin approach to Data Science.

Ultimately, it cannot deliver the wider Data Exploitation agenda I'm being asked to look at.

* What is that wider agenda?
  * Below are the slides to explain the wider agenda and scope of data science (A.I.) across enterprises as I see it.  
(I have circled the area where the PMML architecture works with a Red circle.) 
* Why is that agenda reasonable?
  * The "A.I. agenda" in the public mind is demanding that we move to a wider range of models, methods and more unstructured types of data.
  * It also demands we work on decisions having bigger dollar (or sek) values in data poor environments (search google for *digital twin*) while at the same time using big data where it’s available, to drive improvements across the whole of the curve.
  * These changing attitudes are driving a changing set of expanded expectations about what a data science team is, and does.
  * For instance, High Performance Computing and simulation based modelling is moving to *Apache Spark*! It means Spark will be good for Big Data, and HPC. My data science team is now starting to include HPC workloads into our offered services.
  * Soon, Big Data / Deep Learning / HPC will all need the same tooling - and these are going to blend together into combined solutions. (Recall Raaz's article from ACM communications on [Exascale Computing and Big Data](https://cacm.acm.org/magazines/2015/7/188732-exascale-computing-and-big-data/abstract))
Really this is happening. 
    * As an example - check out this Apache Spark based simulation / modelling solution that is hot right now: [http://www.simudyne.com/](http://www.simudyne.com/). It's proprietary software, but it can integrate easily with all Apache Spark workloads.
  * As we start to fold GPU computing into Spark too, we will find it becomes a go to technology for many very different things.


See [Andrew Morgan's PDF Slides of MFD-AI in the boardroom](https://github.com/lamastex/scalable-data-science/raw/master/_sds/2/2/adviseFromIndustry/AndrewMorgan/PDFs/MFD-AI-in-the-boardroom_Range.pdf).

I hope that explains things better.

Do ask me any questions on linkedin or email.

Andrew


Andrew J Morgan <br>
CEO, Bytesumo Limited <br>
Tel: +44 (0)7970130767 <br>
E-mail: andrew@bytesumo.com <br>

Bytesumo Limited - Registered Company in England and Wales 33 Brodrick Grove, London, SE2 0SR, UK. Company Number: 8505203 


*rescribed into markdown with minor edits by Raazesh Sainudiin, Fri Dec  1 08:30:58 CET 2017, Uppsala, Sweden.*
