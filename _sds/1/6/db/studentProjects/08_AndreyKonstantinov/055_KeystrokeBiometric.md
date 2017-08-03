---
title: ""
permalink: /sds/1/6/db/studentProjects/08_AndreyKonstantinov/055_KeystrokeBiometric/
sidebar:
  nav: "lMenu-SDS-1.6"
---

// Databricks notebook source exported at Tue, 28 Jun 2016 11:18:13 UTC


# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)

## Keystroke Biometric
### Scalable data science project by [Andrey Konstantinov](https://www.linkedin.com/in/andrey-konstantinov-38234531)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/08_AndreyKonstantinov/055_KeystrokeBiometric.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/08_AndreyKonstantinov/055_KeystrokeBiometric](http://img.youtube.com/vi/rnpa6YsDXWY/0.jpg)](https://www.youtube.com/v/rnpa6YsDXWY?rel=0&autoplay=1&modestbranding=1&start=2586&end=3956)





# Keystroke Biometric
Completed by Andrey Konstantinov for Scalable Data Science course for University of Canterbury, Christchurch
2016/05

### What is keystroke biometric?
https://en.wikipedia.org/wiki/Keystroke_dynamics

### Why?
- detect if an account is shared by multiple people (to enforce licensing)
- detect if an account is accidently used by a second person when a device is left unattended (to block it / reask for password)
- waive the second form authentication factor for less-risky opertions (when keystroke dynamic is strong)
- categorise PC user to experienced and not experienced

### Existing work
Comparing Anomaly-Detection Algorithms for Keystroke Dynamics
- http://www.cs.cmu.edu/~maxion/pubs/KillourhyMaxion09.pdf
- http://www.cs.cmu.edu/~keystroke/

### Related (mouse movements dynamics)
- bots filtering (https://www.google.com/recaptcha/intro/index.html)
- sliding bar on signin form (http://www.aliexpress.com/)

### Scope of this notebook
- overview of the data source
- statistical evaluation of an algorithm (improved anomaly detection algorithm based on Manhattan distance from average)
- simulation of 'creditability score' to reduce error rate
- results review

### Source code (scala) and interactive demo application (typescript/javascript)
- https://gitlab.com/avkonst/keystroke-dynamics-course


```scala

// NEEDED TO MOVE EVERYTHING TO ONE CELL TO WORKAROUND DATABRICKS COMPILATION ERROR
// so all documentation is in embeded comments

// load data source file (50 people, typed the same password 400 times, in 8 sessions)
import scala.io.Source
val datasource = Source.fromURL("https://gitlab.com/avkonst/keystroke-dynamics-course/raw/master/data/DSL-StrongPasswordData.csv").getLines()

// supportive case classes to handle input rows and results
case class Row(subject: String, session: Int, attempt: Int, timings: Array[Double])
case class Result(subject: String, threshold: Double,
        falseNegativesErrorRate: Double,
        falsePositivesErrorRate: Double,
        falseNegativesErrorSimulation: Double,
        falsePositivesErrorSimulation: Double,
        consistencyLevel: Double
    )

val rows = datasource
.drop(1) // skip header
.map(i => i.split(",")) // split into columns
.map(i => Row(i(0), i(1).toInt, i(2).toInt, i.drop(3).map(j => j.toDouble))) // extract rows data
.toArray

// supportive functions

    // returns factors to normalize timings data to the range from 0 to 1
    def getTimingsFactors(data: Array[Row]): Array[Double] = {
        val timingFactors = (0 until data.head.timings.length).map(i => 1.0).toArray
        (0 until data.head.timings.length).foreach(index => {
            data.foreach(row => {
                timingFactors(index) = Math.max(row.timings(index), timingFactors(index))
            })
        })
        timingFactors.map(i => 1.0/i)
    }

    // returns a distance between 2 vectors (using Manhattan distance and normalization factors)
    def getDistance(array1: Array[Double], array2: Array[Double], timingFactors: Array[Double]) = {
        var maxDistance = 0D
        val sum = (0 until array1.length).map(colInd => {
            val currentColDistance = Math.abs(array1(colInd) - array2(colInd)) * timingFactors(colInd)
            maxDistance = Math.max(maxDistance, currentColDistance)
            currentColDistance
        }).toArray.sum
        sum - maxDistance * 2 // maxDistance * 2 is for noise reduction, selected emprically
    }

    // returns an average vector of timings for the set of provided rows
    def getAverageTimings(data: Array[Row]) = {
        val sumTimings = data.head.timings.map(i => 0D)
        data.foreach(row => {
            (0 until sumTimings.length).foreach(colInd => {
                sumTimings(colInd) += row.timings(colInd)
            })
        })
        sumTimings.map(i => i / data.length)
    }

// higher-level supportive functions

    // identifies and removes outliers in rows incrementally up to the specified ratio
    def getRepresentativeSet(data: Array[Row], allowedFalseNegativeRate: Double): Array[Row] = {
        var result = data
        val numberOfRowsToRemove = (data.length * allowedFalseNegativeRate).toInt
        (0 until numberOfRowsToRemove).foreach(iteration => {
            val timingFactors = getTimingsFactors(result)
            val averageTimings = getAverageTimings(result)
            result = result.map(
                row => (getDistance(averageTimings, row.timings, timingFactors) -> row))
                .sortBy(i => i._1)
                .reverse
                .drop(1)
                .map(i => i._2)
        })
        result
    }

    // simulates a login process using 'creditability score',
    // allows to identify a case when a subject produces a 'strong' keystroke
    // and when it fails to sustain even 'weak' keystroke match continously
    def simulateCreditabilityScore(data: Array[Row], averageTimings: Array[Double], timingFactors: Array[Double], threshold: Double) = {
        var trials = 0
        var creditability = 0.0
        var blocked = false
        var interrupted = false
        data.foreach(row => {
            if (blocked == false && interrupted == false) {
                val distance = getDistance(averageTimings, row.timings, timingFactors)
                trials += 1
                if (trials > 4) {
                    // when a password typed 5 times with 'weak' keystroke, return 'rejected' status
                    blocked = true
                }
                else {
                    if (distance > threshold) {
                        creditability -= (distance / threshold - 1)
                        if (creditability < -1.0) {
                            // when creditability drops significantly, return 'rejected' status
                            blocked = true
                        }
                    }
                    else {
                        creditability += (1 - distance / threshold)
                        if (creditability > 0.15) {
                            // when a password is typed with 'strong' keystroke or creditability is recovered, return 'accepted' status
                            interrupted = true
                        }
                    }
                }
            }
        })
        blocked
    }

// main routine which analyses a given subject

    def analyseSubject(data: Array[Row], subject: String) = {
        // get 80% of data as train data for the specified subject
        val currentSubjectTrainData = data.filter(i => i.subject == subject && i.attempt <= 40)
        // get 20% as test data for the specified subject
        val currentSubjectTestData = data.filter(i => i.subject == subject && i.attempt > 40)

        //
        // BUILD MODEL
        // get representative set from the train data
        val representativeSet = getRepresentativeSet(currentSubjectTrainData, 0.2)
        // calculate subject specific parameters, representing digital keystroke signature
        val timingFactors = getTimingsFactors(representativeSet)
        val averageTimings = getAverageTimings(representativeSet)
        // threshold is set to a maximum value to reach 0% false-negative error rate for the rows from representative set
        val threshold = representativeSet.map(row => getDistance(averageTimings, row.timings, timingFactors)).max
        // consistency of a subject can be estimated by the following parameter (normalized)
        val consistencyLevel = threshold / averageTimings.zipWithIndex.map(i => i._1 * timingFactors(i._2)).sum

        //
        // APPLY THE MODEL ON TEST DATA
        // calculate ratio of rows REJECTED by the model
        val falseNegatives = currentSubjectTestData.map(
            row => getDistance(averageTimings, row.timings, timingFactors))
            .filter(i => i > threshold)
        val falseNegativesErrorRate = falseNegatives.length.toDouble / currentSubjectTestData.length

        //
        // APPLY THE MODEL ON OTHER SUBJECTS
        // calculate ratio of rows ACCEPTED by the model
        val otherSubjectsData = data.filter(i => i.subject != subject /*&& i.attempt < 2 && i.session == 1*/)
        val falsePositives = otherSubjectsData.map(
            row => getDistance(averageTimings, row.timings, timingFactors))
            .filter(i => i <= threshold)
        val falsePositivesErrorRate = falsePositives.length.toDouble / otherSubjectsData.length

        //
        // APPLY THE MODEL FOR CREDITABILITY SCORE SIMULATION ON TEST DATA
        // calculate ratio of sessions REJECTED by the model
        val currentSubjectTestDataGrouped = currentSubjectTestData.groupBy(i => i.subject + "_" + i.session)
            .map(i => (i._1 -> i._2.sortBy(j => j.attempt)))
        val currentSubjectTestDataSimulatedGroups = currentSubjectTestDataGrouped.map(
            group => simulateCreditabilityScore(group._2, averageTimings, timingFactors, threshold))
            .toArray
        val falseNegativesErrorRateWithCreditability = currentSubjectTestDataSimulatedGroups.count(i => i == true).toDouble / currentSubjectTestDataSimulatedGroups.length

        //
        // APPLY THE MODEL FOR CREDITABILITY SCORE SIMULATION ON OTHER SUBJECTS
        // calculate ratio of sessions ACCEPTED by the model
        val otherSubjectsDataGrouped = otherSubjectsData./*filter(i => i.session == 1).*/groupBy(i => i.subject + "_" + i.session)
            .map(i => (i._1 -> i._2.sortBy(j => j.attempt)))
        val otherSubjectsDataSimulatedGroups = otherSubjectsDataGrouped.map(
            group => simulateCreditabilityScore(group._2, averageTimings, timingFactors, threshold))
            .toArray
        val falsePositivesErrorRateWithCreditability = otherSubjectsDataSimulatedGroups.count(i => i == false).toDouble / otherSubjectsDataSimulatedGroups.length

        Result(
            subject,
            threshold,
            falseNegativesErrorRate,
            falsePositivesErrorRate,
            falseNegativesErrorRateWithCreditability,
            falsePositivesErrorRateWithCreditability,
            consistencyLevel
        )
    }

// get all unique subjects
val subjects = rows.map(row => row.subject).distinct

// calculate error rates per every subject
val errorRates = subjects.map(subject => analyseSubject(rows, subject))
    .sortBy(i => i.consistencyLevel).reverse // order by consistency descending

// print results per every subject
errorRates.foreach(i => {
        println(s"${i.subject}: Applied threshold ${i.threshold}")
        println(s"${i.subject}: False Negatives Error Rate ${i.falseNegativesErrorRate}")
        println(s"${i.subject}: False Positives Error Rate ${i.falsePositivesErrorRate}")
        println(s"${i.subject}: False Negatives Scores Simulation ${i.falseNegativesErrorSimulation}")
        println(s"${i.subject}: False Positives Scores Simulation ${i.falsePositivesErrorSimulation}")
        println(s"${i.subject}: Consistency level ${i.consistencyLevel}")
        println("")
    })

// print cummulative result
// note: calculating average over error rates and anomaly scores does not make much sense, but it is good enough to compare error rates of 2 different methods
val errorRatesForConsistent = errorRates//.filter(i => i.consistencyLevel < 0.27)
println(s"TOTAL: False Negatives ${errorRatesForConsistent.map(i => i.falseNegativesErrorRate).sum / errorRatesForConsistent.length}")
println(s"TOTAL: False Positives ${errorRatesForConsistent.map(i => i.falsePositivesErrorRate).sum / errorRatesForConsistent.length}")
println(s"TOTAL: False Negatives Simulation ${errorRatesForConsistent.map(i => i.falseNegativesErrorSimulation).sum / errorRatesForConsistent.length}")
println(s"TOTAL: False Positives Simulation ${errorRatesForConsistent.map(i => i.falsePositivesErrorSimulation).sum / errorRatesForConsistent.length}")

```




# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)

## Keystroke Biometric
### Scalable data science project by [Andrey Konstantinov](https://www.linkedin.com/in/andrey-konstantinov-38234531)

*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
