# Databricks notebook source exported at Mon, 14 Mar 2016 03:17:07 UTC
# MAGIC %md
# MAGIC **SOURCE:** This is from the Community Edition of databricks and has been added to this databricks shard at [/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x](/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x) as extra resources for the project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) that is prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand), and *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
# MAGIC and 
# MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome).

# COMMAND ----------

# MAGIC %md
# MAGIC <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License</a>.

# COMMAND ----------

# MAGIC %md
# MAGIC version 1.0.1
# MAGIC #![Spark Logo](http://spark-mooc.github.io/web-assets/images/ta_Spark-logo-small.png) + ![Python Logo](http://spark-mooc.github.io/web-assets/images/python-logo-master-v3-TM-flattened_small.png)
# MAGIC # **Web Server Log Analysis with Apache Spark**
# MAGIC  
# MAGIC This lab will demonstrate how easy it is to perform web server log analysis with Apache Spark.
# MAGIC  
# MAGIC Server log analysis is an ideal use case for Spark.  It's a very large, common data source and contains a rich set of information.  Spark allows you to store your logs in files on disk cheaply, while still providing a quick and simple way to perform data analysis on them.  This homework will show you how to use Apache Spark on real-world text-based production logs and fully harness the power of that data.  Log data comes from many sources, such as web, file, and compute servers, application logs, user-generated content,  and can be used for monitoring servers, improving business and customer intelligence, building recommendation systems, fraud detection, and much more.

# COMMAND ----------

# MAGIC %md
# MAGIC #### How to complete this assignment
# MAGIC  
# MAGIC This assignment is broken up into sections with bite-sized examples for demonstrating Spark functionality for log processing. For each problem, you should start by thinking about the algorithm that you will use to *efficiently* process the log in a parallel, distributed manner. This means using the various [RDD](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD) operations along with [`lambda` functions](https://docs.python.org/2/tutorial/controlflow.html#lambda-expressions) that are applied at each worker.
# MAGIC  
# MAGIC This assignment consists of 4 parts:
# MAGIC *Part 1*: Apache Web Server Log file format
# MAGIC *Part 2*: Sample Analyses on the Web Server Log File
# MAGIC *Part 3*: Analyzing Web Server Log File
# MAGIC *Part 4*: Exploring 404 Response Codes

# COMMAND ----------

# MAGIC %md
# MAGIC #### **Part 1: Apache Web Server Log file format**
# MAGIC The log files that we use for this assignment are in the [Apache Common Log Format (CLF)](http://httpd.apache.org/docs/1.3/logs.html#common). The log file entries produced in CLF will look something like this:
# MAGIC `127.0.0.1 - - [01/Aug/1995:00:00:01 -0400] "GET /images/launch-logo.gif HTTP/1.0" 200 1839`
# MAGIC  
# MAGIC Each part of this log entry is described below.
# MAGIC * `127.0.0.1`
# MAGIC This is the IP address (or host name, if available) of the client (remote host) which made the request to the server.
# MAGIC  
# MAGIC * `-`
# MAGIC The "hyphen" in the output indicates that the requested piece of information (user identity from remote machine) is not available.
# MAGIC  
# MAGIC * `-`
# MAGIC The "hyphen" in the output indicates that the requested piece of information (user identity from local logon) is not available.
# MAGIC  
# MAGIC * `[01/Aug/1995:00:00:01 -0400]`
# MAGIC The time that the server finished processing the request. The format is:
# MAGIC `[day/month/year:hour:minute:second timezone]`
# MAGIC   * day = 2 digits
# MAGIC   * month = 3 letters
# MAGIC   * year = 4 digits
# MAGIC   * hour = 2 digits
# MAGIC   * minute = 2 digits
# MAGIC   * second = 2 digits
# MAGIC   * zone = (\+ | \-) 4 digits
# MAGIC  
# MAGIC * `"GET /images/launch-logo.gif HTTP/1.0"`
# MAGIC This is the first line of the request string from the client. It consists of a three components: the request method (e.g., `GET`, `POST`, etc.), the endpoint (a [Uniform Resource Identifier](http://en.wikipedia.org/wiki/Uniform_resource_identifier)), and the client protocol version.
# MAGIC  
# MAGIC * `200`
# MAGIC This is the status code that the server sends back to the client. This information is very valuable, because it reveals whether the request resulted in a successful response (codes beginning in 2), a redirection (codes beginning in 3), an error caused by the client (codes beginning in 4), or an error in the server (codes beginning in 5). The full list of possible status codes can be found in the HTTP specification ([RFC 2616](https://www.ietf.org/rfc/rfc2616.txt) section 10).
# MAGIC  
# MAGIC * `1839`
# MAGIC The last entry indicates the size of the object returned to the client, not including the response headers. If no content was returned to the client, this value will be "-" (or sometimes 0).
# MAGIC  
# MAGIC Note that log files contain information supplied directly by the client, without escaping. Therefore, it is possible for malicious clients to insert control-characters in the log files, *so care must be taken in dealing with raw logs.*
# MAGIC  
# MAGIC #### NASA-HTTP Web Server Log
# MAGIC For this assignment, we will use a data set from NASA Kennedy Space Center WWW server in Florida. The full data set is freely available (http://ita.ee.lbl.gov/html/contrib/NASA-HTTP.html) and contains two month's of all HTTP requests. We are using a subset that only contains several days worth of requests.

# COMMAND ----------

# MAGIC %md
# MAGIC #### **(1a) Parsing Each Log Line**
# MAGIC Using the CLF as defined above, we create a regular expression pattern to extract the nine fields of the log line using the Python regular expression [`search` function](https://docs.python.org/2/library/re.html#regular-expression-objects). The function returns a pair consisting of a Row object and 1. If the log line fails to match the regular expression, the function returns a pair consisting of the log line string and 0. A '-' value in the content size field is cleaned up by substituting it with 0. The function converts the log line's date string into a Python `datetime` object using the given `parse_apache_time` function.

# COMMAND ----------

import re
import datetime

from pyspark.sql import Row

month_map = {'Jan': 1, 'Feb': 2, 'Mar':3, 'Apr':4, 'May':5, 'Jun':6, 'Jul':7,
    'Aug':8,  'Sep': 9, 'Oct':10, 'Nov': 11, 'Dec': 12}

def parse_apache_time(s):
    """ Convert Apache time format into a Python datetime object
    Args:
        s (str): date and time in Apache time format
    Returns:
        datetime: datetime object (ignore timezone for now)
    """
    return datetime.datetime(int(s[7:11]),
                             month_map[s[3:6]],
                             int(s[0:2]),
                             int(s[12:14]),
                             int(s[15:17]),
                             int(s[18:20]))


def parseApacheLogLine(logline):
    """ Parse a line in the Apache Common Log format
    Args:
        logline (str): a line of text in the Apache Common Log format
    Returns:
        tuple: either a dictionary containing the parts of the Apache Access Log and 1,
               or the original invalid log line and 0
    """
    match = re.search(APACHE_ACCESS_LOG_PATTERN, logline)
    if match is None:
        return (logline, 0)
    size_field = match.group(9)
    if size_field == '-':
        size = long(0)
    else:
        size = long(match.group(9))
    return (Row(
        host          = match.group(1),
        client_identd = match.group(2),
        user_id       = match.group(3),
        date_time     = parse_apache_time(match.group(4)),
        method        = match.group(5),
        endpoint      = match.group(6),
        protocol      = match.group(7),
        response_code = int(match.group(8)),
        content_size  = size
    ), 1)


# COMMAND ----------

# A regular expression pattern to extract fields from the log line
APACHE_ACCESS_LOG_PATTERN = '^(\S+) (\S+) (\S+) \[([\w:/]+\s[+\-]\d{4})\] "(\S+) (\S+)\s*(\S*)" (\d{3}) (\S+)'

# COMMAND ----------

# MAGIC %md
# MAGIC #### **(1b) Configuration and Initial RDD Creation**
# MAGIC We are ready to specify the input log file and create an RDD containing the parsed log file data. The log file has already been downloaded for you.
# MAGIC  
# MAGIC To create the primary RDD that we'll use in the rest of this assignment, we first load the text file using [`sc.textfile(logFile)`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.SparkContext.textFile) to convert each line of the file into an element in an RDD.
# MAGIC Next, we use [`map(parseApacheLogLine)`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD.map) to apply the parse function to each element (that is, a line from the log file) in the RDD and turn each line into a pair [`Row` object](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.Row).
# MAGIC Finally, we cache the RDD in memory since we'll use it throughout this notebook.

# COMMAND ----------

# MAGIC %md **WARNING:** If *test_helper*, required in the cell below, is not installed, follow the instructions [here](https://databricks-staging-cloudfront.staging.cloud.databricks.com/public/c65da9a2fa40e45a2028cddebe45b54c/8637560089690848/4187311313936645/6977722904629137/05f3c2ecc3.html).

# COMMAND ----------

import sys
import os
from test_helper import Test

baseDir = os.path.join('databricks-datasets')
inputPath = os.path.join('cs100', 'lab2', 'data-001', 'apache.access.log.PROJECT')
logFile = os.path.join(baseDir, inputPath)

def parseLogs():
    """ Read and parse log file """
    parsed_logs = (sc
                   .textFile(logFile)
                   .map(parseApacheLogLine)
                   .cache())

    access_logs = (parsed_logs
                   .filter(lambda s: s[1] == 1)
                   .map(lambda s: s[0])
                   .cache())

    failed_logs = (parsed_logs
                   .filter(lambda s: s[1] == 0)
                   .map(lambda s: s[0]))
    failed_logs_count = failed_logs.count()
    if failed_logs_count > 0:
        print 'Number of invalid logline: %d' % failed_logs.count()
        for line in failed_logs.take(20):
            print 'Invalid logline: %s' % line

    print 'Read %d lines, successfully parsed %d lines, failed to parse %d lines' % (parsed_logs.count(), access_logs.count(), failed_logs.count())
    return parsed_logs, access_logs, failed_logs


parsed_logs, access_logs, failed_logs = parseLogs()

# COMMAND ----------

# MAGIC %md
# MAGIC #### **(1c) Data Cleaning**
# MAGIC Notice that there are a large number of log lines that failed to parse. Examine the sample of invalid lines and compare them to the correctly parsed line, an example is included below. Based on your observations, alter the `APACHE_ACCESS_LOG_PATTERN` regular expression below so that the failed lines will correctly parse, and press `Shift-Enter` to rerun `parseLogs()`.
# MAGIC  
# MAGIC `127.0.0.1 - - [01/Aug/1995:00:00:01 -0400] "GET /images/launch-logo.gif HTTP/1.0" 200 1839`
# MAGIC  
# MAGIC If you not familar with Python regular expression [`search` function](https://docs.python.org/2/library/re.html#regular-expression-objects), now would be a good time to check up on the [documentation](https://developers.google.com/edu/python/regular-expressions). One tip that might be useful is to use an online tester like http://pythex.org or http://www.pythonregex.com. To use it, copy and paste the regular expression string below (located between the single quotes ') and test it against one of the 'Invalid logline' above.

# COMMAND ----------

# ANSWER
# The issue here is that some of the clients' request strings included trailing spaces. We can safely ignore those spaces and allow the lines to be correctly parsed by adding a `\s*` to the regular expression spring before the closing double quote.
APACHE_ACCESS_LOG_PATTERN = '^(\S+) (\S+) (\S+) \[([\w:/]+\s[+\-]\d{4})\] "(\S+) (\S+)\s*(\S*)\s*" (\d{3}) (\S+)'

parsed_logs, access_logs, failed_logs = parseLogs()

# COMMAND ----------

# TEST Data cleaning (1c)
Test.assertEquals(failed_logs.count(), 0, 'incorrect failed_logs.count()')
Test.assertEquals(parsed_logs.count(), 1043177 , 'incorrect parsed_logs.count()')
Test.assertEquals(access_logs.count(), parsed_logs.count(), 'incorrect access_logs.count()')


# COMMAND ----------

# PRIVATE_TEST Data cleaning (1c)

Test.assertEquals(failed_logs.count(), 0, 'incorrect failed_logs.count()')
Test.assertEquals(parsed_logs.count(), 1043177 , 'incorrect parsed_logs.count()') # TODO Hash this
Test.assertEquals(access_logs.count(), parsed_logs.count(), 'incorrect access_logs.count()')

# access_logs.take(1)[0].user_id
user_id_count = access_logs.filter(lambda x: x.user_id == u'-').count()
Test.assertEquals(user_id_count, 1043177, 'incorrect user_id count')
host_count = access_logs.filter(lambda x: x.host  == u'in24.inetnebr.com').count()
Test.assertEquals(host_count, 55, 'incorrect host count')


# COMMAND ----------

# MAGIC %md
# MAGIC #### **Part 2: Sample Analyses on the Web Server Log File**
# MAGIC  
# MAGIC Now that we have an RDD containing the log file as a set of Row objects, we can perform various analyses.
# MAGIC  
# MAGIC **(2a) Example: Content Size Statistics**
# MAGIC  
# MAGIC Let's compute some statistics about the sizes of content being returned by the web server. In particular, we'd like to know what are the average, minimum, and maximum content sizes.
# MAGIC  
# MAGIC We can compute the statistics by applying a `map` to the `access_logs` RDD. The `lambda` function we want for the map is to extract the `content_size` field from the RDD. The map produces a new RDD containing only the `content_sizes` (one element for each Row object in the `access_logs` RDD). To compute the minimum and maximum statistics, we can use [`min()`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD.min) and [`max()`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD.max) functions on the new RDD. We can compute the average statistic by using the [`reduce`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD.reduce) function with a `lambda` function that sums the two inputs, which represent two elements from the new RDD that are being reduced together. The result of the `reduce()` is the total content size from the log and it is to be divided by the number of requests as determined using the [`count()`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD.count) function on the new RDD.

# COMMAND ----------

# Calculate statistics based on the content size.
content_sizes = access_logs.map(lambda log: log.content_size).cache()
print 'Content Size Avg: %i, Min: %i, Max: %s' % (
    content_sizes.reduce(lambda a, b : a + b) / content_sizes.count(),
    content_sizes.min(),
    content_sizes.max())

# COMMAND ----------

# MAGIC %md
# MAGIC **(2b) Example: Response Code Analysis**
# MAGIC Next, lets look at the response codes that appear in the log. As with the content size analysis, first we create a new RDD by using a `lambda` function to extract the `response_code` field from the `access_logs` RDD. The difference here is that we will use a [pair tuple](https://docs.python.org/2/tutorial/datastructures.html?highlight=tuple#tuples-and-sequences) instead of just the field itself. Using a pair tuple consisting of the response code and 1 will let us count how many records have a particular response code. Using the new RDD, we perform a [`reduceByKey`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD.reduceByKey) function. `reduceByKey` performs a reduce on a per-key basis by applying the `lambda` function to each element, pairwise with the same key. We use the simple `lambda` function of adding the two values. Then, we cache the resulting RDD and create a list by using the [`take`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD.take) function.

# COMMAND ----------

# Response Code to Count
responseCodeToCount = (access_logs
                       .map(lambda log: (log.response_code, 1))
                       .reduceByKey(lambda a, b : a + b)
                       .cache())
responseCodeToCountList = responseCodeToCount.take(100)
print 'Found %d response codes' % len(responseCodeToCountList)
print 'Response Code Counts: %s' % responseCodeToCountList
assert len(responseCodeToCountList) == 7
assert sorted(responseCodeToCountList) == [(200, 940847), (302, 16244), (304, 79824), (403, 58), (404, 6185), (500, 2), (501, 17)]


# COMMAND ----------

# MAGIC %md
# MAGIC **(2c) Example: Response Code Graphing with `matplotlib`**
# MAGIC Now, lets visualize the results from the last example. We can visualize the results from the last example using [`matplotlib`](http://matplotlib.org/). First we need to extract the labels and fractions for the graph. We do this with two separate `map` functions with a `lambda` functions. The first `map` function extracts a list of of the response code values, and the second `map` function extracts a list of the per response code counts  divided by the total size of the access logs. Next, we create a figure with `figure()` constructor and use the `pie()` method to create the pie plot.

# COMMAND ----------

labels = responseCodeToCount.map(lambda (x, y): x).collect()
print labels
count = access_logs.count()
fracs = responseCodeToCount.map(lambda (x, y): (float(y) / count)).collect()
print fracs


# COMMAND ----------

import matplotlib.pyplot as plt


def pie_pct_format(value):
    """ Determine the appropriate format string for the pie chart percentage label
    Args:
        value: value of the pie slice
    Returns:
        str: formated string label; if the slice is too small to fit, returns an empty string for label
    """
    return '' if value < 7 else '%.0f%%' % value

fig = plt.figure(figsize=(4.5, 4.5), facecolor='white', edgecolor='white')
colors = ['yellowgreen', 'lightskyblue', 'gold', 'purple', 'lightcoral', 'yellow', 'black']
explode = (0.05, 0.05, 0.1, 0, 0, 0, 0)
patches, texts, autotexts = plt.pie(fracs, labels=labels, colors=colors,
                                    explode=explode, autopct=pie_pct_format,
                                    shadow=False,  startangle=125)
for text, autotext in zip(texts, autotexts):
    if autotext.get_text() == '':
        text.set_text('')  # If the slice is small to fit, don't show a text label
plt.legend(labels, loc=(0.80, -0.1), shadow=True)
display(fig)  
pass




# COMMAND ----------

# MAGIC %md
# MAGIC **(2c-dbc) Example: Response Code Graphing with Databricks Cloud Plots**
# MAGIC We can also use built-in Databricks Cloud plots with the `display` function to graph the results, but first we need to convert the RDD to a [DataFrame](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.SQLContext.createDataFrame). We do this by first using a `map` function with a `lambda` function that returns a Row object consisting of `response_code` and `count` fields. Next, we use the [`sqlContext.createDataFrame`](http://spark.apache.org/docs/latest/api/python/pyspark.sql.html#pyspark.sql.SQLContext.createDataFrame) function. Finally we can call the `display` function on the resulting DataFrame.
# MAGIC  
# MAGIC After running this sequence, select the "Pie" graph option, and then use "Plot Options..." and drag `response_code` to the key entry field and drag `count` to the value entry field.
# MAGIC  
# MAGIC Try other plot options (e.g., Bar and Line) and see how you can visualize the results in different ways.

# COMMAND ----------

# Create a DataFrame and visualize using display()
responseCodeToCountRow = responseCodeToCount.map(lambda (x, y): Row(response_code=x, count=y))
responseCodeToCountDF = sqlContext.createDataFrame(responseCodeToCountRow)
display(responseCodeToCountDF)

# COMMAND ----------

# MAGIC %md
# MAGIC **(2d) Example: Frequent Hosts**
# MAGIC Let's look at hosts that have accessed the server multiple times (e.g., more than ten times). As with the response code analysis in (2b), first we create a new RDD by using a `lambda` function to extract the `host` field from the `access_logs` RDD using a pair tuple consisting of the host and 1 which will let us count how many records were created by a particular host's request. Using the new RDD, we perform a `reduceByKey` function with a `lambda` function that adds the two values. We then filter the result based on the count of accesses by each host (the second element of each pair) being greater than ten. Next, we extract the host name by performing a `map` with a `lambda` function that returns the first element of each pair. Finally, we extract 20 elements from the resulting RDD - *note that the choice of which elements are returned is not guaranteed to be deterministic.*

# COMMAND ----------

# Any hosts that has accessed the server more than 10 times.
hostCountPairTuple = access_logs.map(lambda log: (log.host, 1))

hostSum = hostCountPairTuple.reduceByKey(lambda a, b : a + b)

hostMoreThan10 = hostSum.filter(lambda s: s[1] > 10)

hostsPick20 = (hostMoreThan10
               .map(lambda s: s[0])
               .take(20))

print 'Any 20 hosts that have accessed more then 10 times: %s' % hostsPick20
# An example: [u'204.120.34.185', u'204.243.249.9', u'slip1-32.acs.ohio-state.edu', u'lapdog-14.baylor.edu', u'199.77.67.3', u'gs1.cs.ttu.edu', u'haskell.limbex.com', u'alfred.uib.no', u'146.129.66.31', u'manaus.bologna.maraut.it', u'dialup98-110.swipnet.se', u'slip-ppp02.feldspar.com', u'ad03-053.compuserve.com', u'srawlin.opsys.nwa.com', u'199.202.200.52', u'ix-den7-23.ix.netcom.com', u'151.99.247.114', u'w20-575-104.mit.edu', u'205.25.227.20', u'ns.rmc.com']


# COMMAND ----------

# MAGIC %md
# MAGIC **(2e) Example: Visualizing Endpoints**
# MAGIC Now, lets visualize the number of hits to endpoints (URIs) in the log. To perform this task, we first create a new RDD by using a `lambda` function to extract the `endpoint` field from the `access_logs` RDD using a pair tuple consisting of the endpoint and 1 which will let us count how many records were created by a particular host's request. Using the new RDD, we perform a `reduceByKey` function with a `lambda` function that adds the two values. We then cache the results.
# MAGIC  
# MAGIC Next we visualize the results using `matplotlib`. We previously imported the `matplotlib.pyplot` library, so we do not need to import it again. We perform two separate `map` functions with `lambda` functions. The first `map` function extracts a list of endpoint values, and the second `map` function extracts a list of the visits per endpoint values. Next, we create a figure with `figure()` constructor, set various features of the plot (axis limits, grid lines, and labels), and use the `plot()` method to create the line plot.

# COMMAND ----------

endpoints = (access_logs
             .map(lambda log: (log.endpoint, 1))
             .reduceByKey(lambda a, b : a + b)
             .cache())
ends = endpoints.map(lambda (x, y): x).collect()
counts = endpoints.map(lambda (x, y): y).collect()

fig = plt.figure(figsize=(8,4.2), facecolor='white', edgecolor='white')
plt.axis([0, len(ends), 0, max(counts)])
plt.grid(b=True, which='major', axis='y')
plt.xlabel('Endpoints')
plt.ylabel('Number of Hits')
plt.plot(counts)
display(fig)  
pass

# COMMAND ----------

# MAGIC %md
# MAGIC **(2e-dbc) Example: Visualizing Endpoints using Databricks Cloud Plots**
# MAGIC We can also visualize the results using built-in Databricks Cloud Plots with the `display` function to graph the results. The first step is to convert the `endpoints` RDD to a DataFrame. We do this by first using a `map` function with a `lambda` function that returns a Row object consisting of `endpoint` and `num_hits` fields. Next, we use the `sqlContext.createDataFrame` function. Finally we can call the `display` function on the resulting DataFrame.
# MAGIC  
# MAGIC After running this sequence, select the "Line" graph option.
# MAGIC  
# MAGIC The graph is plotted using the first 1,000 rows of data. To see a more complete plot, click on the "Plot over all results" link.

# COMMAND ----------

# Create an RDD with Row objects
endpoint_counts_rdd = endpoints.map(lambda s: Row(endpoint = s[0], num_hits = s[1]))
endpoint_counts_schema_rdd = sqlContext.createDataFrame(endpoint_counts_rdd)

# Display a plot of the distribution of the number of hits across the endpoints.
display(endpoint_counts_schema_rdd)

# COMMAND ----------

# MAGIC %md
# MAGIC **(2f) Example: Top Endpoints**
# MAGIC For the final example, we'll look at the top endpoints (URIs) in the log. To determine them, we first create a new RDD by using a `lambda` function to extract the `endpoint` field from the `access_logs` RDD using a pair tuple consisting of the endpoint and 1 which will let us count how many records were created by a particular host's request. Using the new RDD, we perform a `reduceByKey` function with a `lambda` function that adds the two values. We then extract the top ten endpoints by performing a [`takeOrdered`](http://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.RDD.takeOrdered) with a value of 10 and a `lambda` function that multiplies the count (the second element of each pair) by -1 to create a sorted list with the top endpoints at the bottom.

# COMMAND ----------

# Top Endpoints
endpointCounts = (access_logs
                  .map(lambda log: (log.endpoint, 1))
                  .reduceByKey(lambda a, b : a + b))

topEndpoints = endpointCounts.takeOrdered(10, lambda s: -1 * s[1])

print 'Top Ten Endpoints: %s' % topEndpoints
assert topEndpoints == [(u'/images/NASA-logosmall.gif', 59737), (u'/images/KSC-logosmall.gif', 50452), (u'/images/MOSAIC-logosmall.gif', 43890), (u'/images/USA-logosmall.gif', 43664), (u'/images/WORLD-logosmall.gif', 43277), (u'/images/ksclogo-medium.gif', 41336), (u'/ksc.html', 28582), (u'/history/apollo/images/apollo-logo1.gif', 26778), (u'/images/launch-logo.gif', 24755), (u'/', 20292)], 'incorrect Top Ten Endpoints'


# COMMAND ----------

# MAGIC %md
# MAGIC #### **Part 3: Analyzing Web Server Log File**
# MAGIC  
# MAGIC Now it is your turn to perform analyses on web server log files.

# COMMAND ----------

# MAGIC %md
# MAGIC **(3a) Exercise: Top Ten Error Endpoints**
# MAGIC What are the top ten endpoints which did not have return code 200? Create a sorted list containing top ten endpoints and the number of times that they were accessed with non-200 return code.
# MAGIC  
# MAGIC Think about the steps that you need to perform to determine which endpoints did not have a 200 return code, how you will uniquely count those endpoints, and sort the list.
# MAGIC  
# MAGIC You might want to refer back to the previous Lab (Lab 1 Word Count) for insights.

# COMMAND ----------

# ANSWER
# The basic algorithm is as follows:
# 1. Filter the access log by non-200 response code by using a `filter` with a `lambda` function that compares the `response_code` field to 200.
# 2. Extract the endpoint field from the non-200 records by using a `map` function with a `lambda` function that selects the `endpoint` field and creates a pair with 1.
# 3. Count the number of records per distinct endpoint by using a `reduceByKey` function with a `lambda` function that combines the two elements.
# 4. Return a list the top 10 endpoints by using the `takeOrdered` function with a `lambda` function that multiplies the count (second field) by -1.
#
#topTenErrURLs = (access_logs
#           .filter(lambda log: log.response_code != 200)
#           .map(lambda log: (log.endpoint, 1))
#           .reduceByKey(lambda a, b : a + b)
#           .takeOrdered(10, lambda s: -1 * s[1]))
not200 = (access_logs
          .filter(lambda log: log.response_code != 200))

endpointCountPairTuple = (not200
                     .map(lambda log: (log.endpoint, 1)))

endpointSum = (endpointCountPairTuple
               .reduceByKey(lambda a, b : a + b))

topTenErrURLs = (endpointSum
                 .takeOrdered(10, lambda s: -1 * s[1]))
print 'Top Ten failed URLs: %s' % topTenErrURLs


# COMMAND ----------

# TEST Top ten error endpoints (3a)
Test.assertEquals(endpointSum.count(), 7689, 'incorrect count for endpointSum')
Test.assertEquals(topTenErrURLs, [(u'/images/NASA-logosmall.gif', 8761), (u'/images/KSC-logosmall.gif', 7236), (u'/images/MOSAIC-logosmall.gif', 5197), (u'/images/USA-logosmall.gif', 5157), (u'/images/WORLD-logosmall.gif', 5020), (u'/images/ksclogo-medium.gif', 4728), (u'/history/apollo/images/apollo-logo1.gif', 2907), (u'/images/launch-logo.gif', 2811), (u'/', 2199), (u'/images/ksclogosmall.gif', 1622)], 'incorrect Top Ten failed URLs (topTenErrURLs)')

# COMMAND ----------

# PRIVATE_TEST Top ten error endpoints (3a)
Test.assertEquals(endpointSum.count(), 7689, 'incorrect count for endpointSum')
Test.assertEquals(topTenErrURLs, [(u'/images/NASA-logosmall.gif', 8761), (u'/images/KSC-logosmall.gif', 7236), (u'/images/MOSAIC-logosmall.gif', 5197), (u'/images/USA-logosmall.gif', 5157), (u'/images/WORLD-logosmall.gif', 5020), (u'/images/ksclogo-medium.gif', 4728), (u'/history/apollo/images/apollo-logo1.gif', 2907), (u'/images/launch-logo.gif', 2811), (u'/', 2199), (u'/images/ksclogosmall.gif', 1622)], 'incorrect Top Ten failed URLs (topTenErrURLs)')

reverseSortedIndexed = endpointSum.sortBy(lambda s: s[1]).zipWithIndex() # reverse sort
urlAtIndex = reverseSortedIndexed.filter(lambda (url, index): index == 7654).map(lambda (url, index): url).collect()
#print urlAtIndex
Test.assertEquals(urlAtIndex, [(u'/history/apollo/apollo.html', 456)], 'incorrect Top Ten failed URLs')

# COMMAND ----------

# MAGIC %md
# MAGIC **(3b) Exercise: Number of Unique Hosts**
# MAGIC How many unique hosts are there in the entire log?
# MAGIC  
# MAGIC Think about the steps that you need to perform to count the number of different hosts in the log.

# COMMAND ----------

# ANSWER
#The basic algorithm is as follows:
#1. Extract the host field from the `access_logs` records by using a `map` function with a `lambda` function that selects the `host` field.
#2. Use the `distinct` function to uniquify the hosts in the RDD.
hosts = (access_logs
         .map(lambda log: log.host))

uniqueHosts = (hosts
               .distinct())

uniqueHostCount = (uniqueHosts
                   .count())
print 'Unique hosts: %d' % uniqueHostCount


# COMMAND ----------

# TEST Number of unique hosts (3b)
Test.assertEquals(uniqueHostCount, 54507, 'incorrect uniqueHostCount')

# COMMAND ----------

# PRIVATE_TEST Number of unique hosts (3b)
Test.assertEquals(uniqueHostCount, 54507, 'incorrect uniqueHostCount')

# COMMAND ----------

# MAGIC %md
# MAGIC **(3c) Exercise: Number of Unique Daily Hosts**
# MAGIC For an advanced exercise, let's determine the number of unique hosts in the entire log on a day-by-day basis. This computation will give us counts of the number of unique daily hosts. We'd like a list sorted by increasing day of the month which includes the day of the month and the associated number of unique hosts for that day. Make sure you cache the resulting RDD `dailyHosts` so that we can reuse it in the next exercise.
# MAGIC  
# MAGIC Think about the steps that you need to perform to count the number of different hosts that make requests *each* day.
# MAGIC *Since the log only covers a single month, you can ignore the month.*

# COMMAND ----------

# ANSWER
# The basic algorithm is as follows:
#1. Extract the day *and* host fields from the `access_logs` RDD by using a `map` function with a `lambda` function that selects both the `day` and `host` fields and creates a pair with the day as key.
#2. Group together all the records for a particular day by using the `groupByKey` function. This function will create a new RDD with each element being a pair of the key and a list of the associated values (i.e., all the hosts with the associated day key).
#4->3. The easiest way to uniquify the hosts per day and count them is to perform a `map` function with a `lambda` function that creates a pair from the key and for the value converts the list of non-unique hosts into a set (which will uniquify the elements) and then computes the size using the `len` function.
#3->4. Sort the RDD in ascending order using the `sortByKey` function. This function will yield a new RDD that is in the correct date sorted order.
#5. Use the `take` function to convert the RDD to a list of day and host count pairs.

dayToHostPairTuple = (access_logs
                      .map(lambda log: (log.date_time.day, log.host)))

dayGroupedHosts = (dayToHostPairTuple
                   .groupByKey())

dayHostCount = (dayGroupedHosts
              .map(lambda s: (s[0], len(set(s[1])))))

dailyHosts = (dayHostCount
              .sortByKey()
              .cache())
dailyHostsList = (dailyHosts
                  .take(30))
print 'Unique hosts per day: %s' % dailyHostsList
assert dailyHostsList == [(1, 2582), (3, 3222), (4, 4190), (5, 2502), (6, 2537), (7, 4106), (8, 4406), (9, 4317), (10, 4523), (11, 4346), (12, 2864), (13, 2650), (14, 4454), (15, 4214), (16, 4340), (17, 4385), (18, 4168), (19, 2550), (20, 2560), (21, 4134), (22, 4456)]


# COMMAND ----------

# TEST Number of unique daily hosts (3c)
Test.assertEquals(dailyHosts.count(), 21, 'incorrect dailyHosts.count()')
Test.assertEquals(dailyHostsList, [(1, 2582), (3, 3222), (4, 4190), (5, 2502), (6, 2537), (7, 4106), (8, 4406), (9, 4317), (10, 4523), (11, 4346), (12, 2864), (13, 2650), (14, 4454), (15, 4214), (16, 4340), (17, 4385), (18, 4168), (19, 2550), (20, 2560), (21, 4134), (22, 4456)], 'incorrect dailyHostsList')
Test.assertTrue(dailyHosts.is_cached, 'incorrect dailyHosts.is_cached')

# COMMAND ----------

# PRIVATE_TEST Number of unique daily hosts (3c)
Test.assertEquals(dailyHosts.count(), 21, 'incorrect dailyHosts.count()')
Test.assertEquals(dailyHostsList, [(1, 2582), (3, 3222), (4, 4190), (5, 2502), (6, 2537), (7, 4106), (8, 4406), (9, 4317), (10, 4523), (11, 4346), (12, 2864), (13, 2650), (14, 4454), (15, 4214), (16, 4340), (17, 4385), (18, 4168), (19, 2550), (20, 2560), (21, 4134), (22, 4456)], 'incorrect dailyHostsList')
Test.assertTrue(dailyHosts.is_cached, 'incorrect dailyHosts.is_cached')

# COMMAND ----------

# MAGIC %md
# MAGIC **(3d) Exercise: Visualizing the Number of Unique Daily Hosts**
# MAGIC Using the results from the previous exercise, use `matplotlib` to plot a "Line" graph of the unique hosts requests by day.
# MAGIC `daysWithHosts` should be a list of days and `hosts` should be a list of number of unique hosts for each corresponding day.
# MAGIC * How could you convert a RDD into a list? See the [`collect()` method](http://spark.apache.org/docs/latest/api/python/pyspark.html?highlight=collect#pyspark.RDD.collect)*

# COMMAND ----------

# ANSWER
#The results from the last exercise can be visualized using `matplotlib`. We perform two separate `map` functions with `lambda` functions. The first `map` function extracts a list of day values, and the second `map` function extracts a list of the hosts per day values. Next, we create a figure with `figure()` constructor, set various features of the plot (axis limits, grid lines, and labels), and use the `plot()` method to create the line plot. Finally, we call the `display` function on the resulting figure.
daysWithHosts = dailyHosts.map(lambda (x, y): x).collect()
hosts = dailyHosts.map(lambda (x, y): y).collect()


# COMMAND ----------

# TEST Visualizing unique daily hosts (3d)
test_days = range(1, 23)
test_days.remove(2)
Test.assertEquals(daysWithHosts, test_days, 'incorrect days')
Test.assertEquals(hosts, [2582, 3222, 4190, 2502, 2537, 4106, 4406, 4317, 4523, 4346, 2864, 2650, 4454, 4214, 4340, 4385, 4168, 2550, 2560, 4134, 4456], 'incorrect hosts')

# COMMAND ----------

# PRIVATE_TEST Visualizing unique daily hosts (3d)
test_days = range(1, 23)
test_days.remove(2)
Test.assertEquals(daysWithHosts, test_days, 'incorrect days')
Test.assertEquals(hosts, [2582, 3222, 4190, 2502, 2537, 4106, 4406, 4317, 4523, 4346, 2864, 2650, 4454, 4214, 4340, 4385, 4168, 2550, 2560, 4134, 4456], 'incorrect hosts')

# COMMAND ----------

fig = plt.figure(figsize=(8,4.5), facecolor='white', edgecolor='white')
plt.axis([min(daysWithHosts), max(daysWithHosts), 0, max(hosts)+500])
plt.grid(b=True, which='major', axis='y')
plt.xlabel('Day')
plt.ylabel('Hosts')
plt.plot(daysWithHosts, hosts)
display(fig)  
pass

# COMMAND ----------

# MAGIC %md
# MAGIC **(3d-dbc) Exercise (Optional): Visualizing the Number of Unique Daily Hosts using Databricks Cloud Plots**
# MAGIC Using the results from the exercise (3c), use Databricks Cloud plots to plot a "Line" graph of the unique hosts requests by day.

# COMMAND ----------

# ANSWER
#First use a `map` function with a `lambda` function that returns a Row object consisting of `day` and `hosts`, and then use the `sqlContext.createDataFrame` function. Call the `display` function on the resulting DataFrame.
#After running this sequence, select the "Line" graph option, and then use "Plot Options..." and drag `day` to the key entry field and drag `hosts` to the value entry field.
#Note that you can use your mouse to hover over points on the line and see the actual values.
dailyHostsDF = sqlContext.createDataFrame(dailyHosts.map(lambda (x, y): Row(day=x, hosts=y)))
display(dailyHostsDF)

# COMMAND ----------

# MAGIC %md
# MAGIC **(3e) Exercise: Average Number of Daily Requests per Hosts**
# MAGIC Next, let's determine the average number of requests on a day-by-day basis. We'd like a list by increasing day of the month and the associated average number of requests per host for that day. Make sure you cache the resulting RDD `avgDailyReqPerHost` so that we can reuse it in the next exercise.
# MAGIC To compute the average number of requests per host, get the total number of request across all hosts and divide that by the number of unique hosts.
# MAGIC *Since the log only covers a single month, you can skip checking for the month.*
# MAGIC *Also to keep it simple, when calculating the approximate average use the integer value - you do not need to upcast to float*

# COMMAND ----------

# ANSWER
#The basic algorithm is as follows:
#1. Extract the day *and* host fields from the `access_logs` RDD by using a `map` function with a `lambda` function that selects both the `day` and `host` fields and creates a pair with the day as key.
#2. Group together all the records for a particular day by using the `groupByKey` function. This function will create a new RDD with each element being a pair of the key and a list of the associated values (i.e., all the hosts with the associated day key).
#3. Sort the RDD in ascending order using the `sortByKey` function. This function will yield a new RDD that is in the correct date sorted order.
#4. The easiest way to compute average requests per unique hosts per day and count them is to perform a `map` function with a `lambda` function that creates a pair from the key and for the value takes the size of the lists and divides it by a converting the list of non-unique hosts into a set (which will uniquify the elements) and then computes the size using the `len` function.
#5. Use the `take` function to convert the RDD to a list of day and host count pairs.
dayAndHostTuple = (access_logs
                   .map(lambda log: (log.date_time.day, log.host)))

groupedByDay = (dayAndHostTuple
                .groupByKey())

sortedByDay = (groupedByDay
               .sortByKey())

avgDailyReqPerHost = (sortedByDay
                      .map(lambda (d, h): (d, len(h)/len(set(h))))
                      .cache())

avgDailyReqPerHostList = (avgDailyReqPerHost
                          .take(30))
print 'Hosts is %s' % avgDailyReqPerHostList

# COMMAND ----------

# TEST Average number of daily requests per hosts (3e)
Test.assertEquals(avgDailyReqPerHostList, [(1, 13), (3, 12), (4, 14), (5, 12), (6, 12), (7, 13), (8, 13), (9, 14), (10, 13), (11, 14), (12, 13), (13, 13), (14, 13), (15, 13), (16, 13), (17, 13), (18, 13), (19, 12), (20, 12), (21, 13), (22, 12)], 'incorrect avgDailyReqPerHostList')
Test.assertTrue(avgDailyReqPerHost.is_cached, 'incorrect avgDailyReqPerHost.is_cache')


# COMMAND ----------

# PRIVATE_TEST Average number of daily requests per hosts (3e)
Test.assertEquals(avgDailyReqPerHostList, [(1, 13), (3, 12), (4, 14), (5, 12), (6, 12), (7, 13), (8, 13), (9, 14), (10, 13), (11, 14), (12, 13), (13, 13), (14, 13), (15, 13), (16, 13), (17, 13), (18, 13), (19, 12), (20, 12), (21, 13), (22, 12)], 'incorrect avgDailyReqPerHostList')
Test.assertTrue(avgDailyReqPerHost.is_cached, 'incorrect avgDailyReqPerHost.is_cache')


# COMMAND ----------

# MAGIC %md
# MAGIC **(3f) Exercise: Visualizing the Average Daily Requests per Unique Host**
# MAGIC Using the result `avgDailyReqPerHost` from the previous exercise, use `matplotlib` to plot a "Line" graph of the average daily requests per unique host by day.
# MAGIC `daysWithAvg` should be a list of days and `avgs` should be a list of average daily requests per unique hosts for each corresponding day.

# COMMAND ----------

# ANSWER
#The results from the last exercise can be visualized using `matplotlib`. We already imported the `matplotlib.pylab` library, so we do not need to import it again. We perform two separate `map` functions with `lambda` functions. The first `map` function extracts a list of day values, and the second `map` function extracts a list of the average daily requests per unique host values. Next, we create a figure with `figure()` constructor, set various features of the plot (axis limits, grid lines, and labels), and use the `plot()` method to create the line plot. Finally, we call the `display` function on the resulting figure.
daysWithAvg = avgDailyReqPerHost.map(lambda (x, y): x).collect()
avgs = avgDailyReqPerHost.map(lambda (x, y): y).collect()

print daysWithAvg
print avgs


# COMMAND ----------

# TEST Average Daily Requests per Unique Host (3f)
Test.assertEquals(daysWithAvg, [1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22], 'incorrect days')
Test.assertEquals(avgs, [13, 12, 14, 12, 12, 13, 13, 14, 13, 14, 13, 13, 13, 13, 13, 13, 13, 12, 12, 13, 12], 'incorrect avgs')


# COMMAND ----------

# PRIVATE_TEST Average Daily Requests per Unique Host (3f)
Test.assertEquals(daysWithAvg, [1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22], 'incorrect days')
Test.assertEquals(avgs, [13, 12, 14, 12, 12, 13, 13, 14, 13, 14, 13, 13, 13, 13, 13, 13, 13, 12, 12, 13, 12], 'incorrect avgs')

# COMMAND ----------

fig = plt.figure(figsize=(8,4.2), facecolor='white', edgecolor='white')
plt.axis([0, max(daysWithAvg), 0, max(avgs)+2])
plt.grid(b=True, which='major', axis='y')
plt.xlabel('Day')
plt.ylabel('Average')
plt.plot(daysWithAvg, avgs)
display(fig)  
pass

# COMMAND ----------

# MAGIC %md
# MAGIC **(3f-dbc) Exercise (Optional): Visualizing the Average Daily Requests per Unique Host using Databricks Cloud Plots**
# MAGIC As a comparison to (3f), with the results from exercise (3e), use Databricks Cloud plots to plot a "Line" graph of the average daily requests per unique host by day.

# COMMAND ----------

# ANSWER
#First use a `map` function with a `lambda` function that returns a Row object consisting of `day` and `averages`, and then use the `sqlContext.createDataFrame` function. Call the `display` function on the resulting DataFrame.
#After running this sequence, select the "Line" graph option, and then use "Plot Options..." and drag `day` to the key entry field and drag `averages` to the value entry field.
avgDailyReqDF = sqlContext.createDataFrame(avgDailyReqPerHost.map(lambda (x, y): Row(day=x, averages=y)))
display(avgDailyReqDF)

# COMMAND ----------

# MAGIC %md
# MAGIC #### **Part 4: Exploring 404 Response Codes**
# MAGIC  
# MAGIC Let's drill down and explore the error 404 response code records. 404 errors are returned when an endpoint is not found by the server (i.e., a missing page or object).

# COMMAND ----------

# MAGIC %md
# MAGIC **(4a) Exercise: Counting 404 Response Codes**
# MAGIC Create a RDD containing only log records with a 404 response code. Make sure you `cache()` the RDD `badRecords` as we will use it in the rest of this exercise.
# MAGIC  
# MAGIC How many 404 records are in the log?

# COMMAND ----------

# ANSWER
#Filter the access log by 404 response code by using a `filter` with a `lambda` function that compares the `response_code` field to 404, and cache the resulting RDD.
#Use the `count()` method to count the number of 404 response code records.

badRecords = (access_logs
              .filter(lambda log: log.response_code == 404)
              .cache())
print 'Found %d 404 URLs' % badRecords.count()

# COMMAND ----------

# TEST Counting 404 (4a)
Test.assertEquals(badRecords.count(), 6185, 'incorrect badRecords.count()')
Test.assertTrue(badRecords.is_cached, 'incorrect badRecords.is_cached')

# COMMAND ----------

# PRIVATE_TEST Counting 404 (4a)
Test.assertEquals(badRecords.count(), 6185, 'incorrect badRecords.count()')
Test.assertTrue(badRecords.is_cached, 'incorrect badRecords.is_cached')

# COMMAND ----------

# MAGIC %md
# MAGIC **(4b) Exercise: Listing 404 Response Code Records**
# MAGIC Using the RDD containing only log records with a 404 response code that you cached in part (4a), print out a list up to 40 **distinct** endpoints that generate 404 errors -  *no endpoint should appear more than once in your list.*

# COMMAND ----------

# ANSWER
#The basic algorithm is as follows:
#1. Extract the endpoint field from the RDD of 404 records you cached in part (a) by using a `map` function with a `lambda` function that selects the `endpoint` field.
#2. Uniquify the resulting RDD so that each endpoint appears only once by using the `distinct` function.
#4. Return a list of forty endpoints by using the `take` function.
badEndpoints = (badRecords
                .map(lambda log: log.endpoint))

badUniqueEndpoints = (badEndpoints
                      .distinct())

badUniqueEndpointsPick40 = (badUniqueEndpoints
                            .take(40))
print '404 URLS: %s' % badUniqueEndpointsPick40

# COMMAND ----------

# TEST Listing 404 records (4b)

badUniqueEndpointsSet40 = set(badUniqueEndpointsPick40)
Test.assertEquals(len(badUniqueEndpointsSet40), 40, 'badUniqueEndpointsPick40 not distinct')


# COMMAND ----------

# PRIVATE_TEST Listing 404 records (4b)

badUniqueEndpointsSet40 = set(badUniqueEndpointsPick40)
Test.assertEquals(len(badUniqueEndpointsSet40), 40, 'badUniqueEndpointsPick40 not distinct')

badUniqueEndpointsCount = badUniqueEndpoints.count()
badUniqueEndpointsSetAll = set(badUniqueEndpoints.collect())
Test.assertEquals(len(badUniqueEndpointsSetAll), badUniqueEndpointsCount, 'badUniqueEndpoints not distinct')
Test.assertTrue(badUniqueEndpointsSet40.issubset(badUniqueEndpointsSetAll), 'badUniqueEndpointsPick40 is not a subset')

# COMMAND ----------

# MAGIC %md
# MAGIC **(4c) Exercise: Listing the Top Twenty 404 Response Code Endpoints**
# MAGIC Using the RDD containing only log records with a 404 response code that you cached in part (4a), print out a list of the top twenty endpoints that generate the most 404 errors.
# MAGIC *Remember, top endpoints should be in sorted order*

# COMMAND ----------

# ANSWER
#The basic algorithm is as follows:
#1. Extract the endpoint field from the RDD of 404 records you cached in part (a) by using a `map` function with a `lambda` function that selects the `endpoint` field and creates a pair with 1.
#2. Count the number of records per distinct endpoint by using a `reduceByKey` function with a `lambda` function that combines the two elements.
#3. Return a list the top 20 endpoints by using the `takeOrdered` function with a `lambda` function that multiplies the count (second field) by -1.
badEndpointsCountPairTuple = (badRecords
                              .map(lambda log: (log.endpoint, 1)))

badEndpointsSum = (badEndpointsCountPairTuple
                   .reduceByKey(lambda a, b : a + b))

badEndpointsTop20 = (badEndpointsSum
                     .takeOrdered(20, lambda s: -1 * s[1]))
print 'Top Twenty 404 URLs: %s' % badEndpointsTop20

# COMMAND ----------

# TEST Top twenty 404 URLs (4c)
Test.assertEquals(badEndpointsTop20, [(u'/pub/winvn/readme.txt', 633), (u'/pub/winvn/release.txt', 494), (u'/shuttle/missions/STS-69/mission-STS-69.html', 431), (u'/images/nasa-logo.gif', 319), (u'/elv/DELTA/uncons.htm', 178), (u'/shuttle/missions/sts-68/ksc-upclose.gif', 156), (u'/history/apollo/sa-1/sa-1-patch-small.gif', 146), (u'/images/crawlerway-logo.gif', 120), (u'/://spacelink.msfc.nasa.gov', 117), (u'/history/apollo/pad-abort-test-1/pad-abort-test-1-patch-small.gif', 100), (u'/history/apollo/a-001/a-001-patch-small.gif', 97), (u'/images/Nasa-logo.gif', 85), (u'/shuttle/resources/orbiters/atlantis.gif', 64), (u'/history/apollo/images/little-joe.jpg', 62), (u'/images/lf-logo.gif', 59), (u'/shuttle/resources/orbiters/discovery.gif', 56), (u'/shuttle/resources/orbiters/challenger.gif', 54), (u'/robots.txt', 53), (u'/elv/new01.gif>', 43), (u'/history/apollo/pad-abort-test-2/pad-abort-test-2-patch-small.gif', 38)], 'incorrect badEndpointsTop20')

# COMMAND ----------

# PRIVATE_TEST Top twenty 404 URLs (4c)
Test.assertEquals(badEndpointsTop20, [(u'/pub/winvn/readme.txt', 633), (u'/pub/winvn/release.txt', 494), (u'/shuttle/missions/STS-69/mission-STS-69.html', 431), (u'/images/nasa-logo.gif', 319), (u'/elv/DELTA/uncons.htm', 178), (u'/shuttle/missions/sts-68/ksc-upclose.gif', 156), (u'/history/apollo/sa-1/sa-1-patch-small.gif', 146), (u'/images/crawlerway-logo.gif', 120), (u'/://spacelink.msfc.nasa.gov', 117), (u'/history/apollo/pad-abort-test-1/pad-abort-test-1-patch-small.gif', 100), (u'/history/apollo/a-001/a-001-patch-small.gif', 97), (u'/images/Nasa-logo.gif', 85), (u'/shuttle/resources/orbiters/atlantis.gif', 64), (u'/history/apollo/images/little-joe.jpg', 62), (u'/images/lf-logo.gif', 59), (u'/shuttle/resources/orbiters/discovery.gif', 56), (u'/shuttle/resources/orbiters/challenger.gif', 54), (u'/robots.txt', 53), (u'/elv/new01.gif>', 43), (u'/history/apollo/pad-abort-test-2/pad-abort-test-2-patch-small.gif', 38)], 'incorrect badEndpointsTop20')

# COMMAND ----------

# MAGIC %md
# MAGIC **(4d) Exercise: Listing the Top Twenty-five 404 Response Code Hosts**
# MAGIC Instead of looking at the endpoints that generated 404 errors, let's look at the hosts that encountered 404 errors. Using the RDD containing only log records with a 404 response code that you cached in part (4a), print out a list of the top twenty-five hosts that generate the most 404 errors.

# COMMAND ----------

# ANSWER
#The basic algorithm is as follows:
#1. Extract the host field from the RDD of 404 records you cached in part (a) by using a `map` function with a `lambda` function that selects the `host` field and creates a pair with 1.
#2. Count the number of records per distinct host by using a `reduceByKey` function with a `lambda` function that combines the two elements.
#3. Return a list the top 25 hosts by using the `takeOrdered` function with a `lambda` function that multiplies the count (second field) by -1.
errHostsCountPairTuple = (badRecords
                          .map(lambda log: (log.host, 1)))

errHostsSum = (errHostsCountPairTuple
               .reduceByKey(lambda a, b : a + b))

errHostsTop25 = (errHostsSum
                 .takeOrdered(25, lambda s: -1 * s[1]))
print 'Top 25 hosts that generated errors: %s' % errHostsTop25

# COMMAND ----------

# TEST Top twenty-five 404 response code hosts (4d)

Test.assertEquals(len(errHostsTop25), 25, 'length of errHostsTop25 is not 25')
Test.assertEquals(len(set(errHostsTop25) - set([(u'maz3.maz.net', 39), (u'piweba3y.prodigy.com', 39), (u'gate.barr.com', 38), (u'm38-370-9.mit.edu', 37), (u'ts8-1.westwood.ts.ucla.edu', 37), (u'nexus.mlckew.edu.au', 37), (u'204.62.245.32', 33), (u'163.206.104.34', 27), (u'spica.sci.isas.ac.jp', 27), (u'www-d4.proxy.aol.com', 26), (u'www-c4.proxy.aol.com', 25), (u'203.13.168.24', 25), (u'203.13.168.17', 25), (u'internet-gw.watson.ibm.com', 24), (u'scooter.pa-x.dec.com', 23), (u'crl5.crl.com', 23), (u'piweba5y.prodigy.com', 23), (u'onramp2-9.onr.com', 22), (u'slip145-189.ut.nl.ibm.net', 22), (u'198.40.25.102.sap2.artic.edu', 21), (u'gn2.getnet.com', 20), (u'msp1-16.nas.mr.net', 20), (u'isou24.vilspa.esa.es', 19), (u'dial055.mbnet.mb.ca', 19), (u'tigger.nashscene.com', 19)])), 0, 'incorrect errHostsTop25')

# COMMAND ----------

# PRIVATE_TEST Top twenty-five 404 response code hosts (4d)

orderCount  = 1
for idx, val in enumerate(errHostsTop25):
    if idx > 0:
        orderCount += 1 if (errHostsTop25[idx-1][1] - errHostsTop25[idx][1] >= 0) else 0

Test.assertEquals(len(errHostsTop25), 25, 'length of errHostsTop25 is not 25')
Test.assertEquals(orderCount, len(errHostsTop25), 'errHostsTop25 not in order')
Test.assertEquals(len(set(errHostsTop25) - set([(u'maz3.maz.net', 39), (u'piweba3y.prodigy.com', 39), (u'gate.barr.com', 38), (u'm38-370-9.mit.edu', 37), (u'ts8-1.westwood.ts.ucla.edu', 37), (u'nexus.mlckew.edu.au', 37), (u'204.62.245.32', 33), (u'163.206.104.34', 27), (u'spica.sci.isas.ac.jp', 27), (u'www-d4.proxy.aol.com', 26), (u'www-c4.proxy.aol.com', 25), (u'203.13.168.24', 25), (u'203.13.168.17', 25), (u'internet-gw.watson.ibm.com', 24), (u'scooter.pa-x.dec.com', 23), (u'crl5.crl.com', 23), (u'piweba5y.prodigy.com', 23), (u'onramp2-9.onr.com', 22), (u'slip145-189.ut.nl.ibm.net', 22), (u'198.40.25.102.sap2.artic.edu', 21), (u'gn2.getnet.com', 20), (u'msp1-16.nas.mr.net', 20), (u'isou24.vilspa.esa.es', 19), (u'dial055.mbnet.mb.ca', 19), (u'tigger.nashscene.com', 19)])), 0, 'incorrect errHostsTop25')


# COMMAND ----------

# MAGIC %md
# MAGIC **(4e) Exercise: Listing 404 Response Codes per Day**
# MAGIC Let's explore the 404 records temporally. Break down the 404 requests by day (`cache()` the RDD `errDateSorted`) and get the daily counts sorted by day as a list.
# MAGIC *Since the log only covers a single month, you can ignore the month in your checks.*

# COMMAND ----------

# ANSWER
#The basic algorithm is as follows:
#1. Extract the day field from the RDD of 404 records you cached in part (a) by using a `map` function with a `lambda` function that selects the `day` field and creates a pair with 1.
#2. Count the number of 404 records per day by using a `reduceByKey` function with a `lambda` function that combines the two elements.
#3. Sort the RDD by the day field - the key (first) field of the pair.
errDateCountPairTuple = (badRecords
                         .map(lambda log: (log.date_time.day, 1)))

errDateSum = (errDateCountPairTuple
              .reduceByKey(lambda a, b : a + b))

errDateSorted = (errDateSum
                 .sortByKey()
                 .cache())

errByDate = (errDateSorted.take(100))
print '404 Errors by day: %s' % errByDate

# COMMAND ----------

# TEST 404 response codes per day (4e)
Test.assertEquals(errByDate, [(1, 243), (3, 303), (4, 346), (5, 234), (6, 372), (7, 532), (8, 381), (9, 279), (10, 314), (11, 263), (12, 195), (13, 216), (14, 287), (15, 326), (16, 258), (17, 269), (18, 255), (19, 207), (20, 312), (21, 305), (22, 288)], 'incorrect errByDate')
Test.assertTrue(errDateSorted.is_cached, 'incorrect errDateSorted.is_cached')

# COMMAND ----------

# PRIVATE_TEST 404 response codes per day (4e)
Test.assertEquals(errByDate, [(1, 243), (3, 303), (4, 346), (5, 234), (6, 372), (7, 532), (8, 381), (9, 279), (10, 314), (11, 263), (12, 195), (13, 216), (14, 287), (15, 326), (16, 258), (17, 269), (18, 255), (19, 207), (20, 312), (21, 305), (22, 288)], 'incorrect errByDate')
Test.assertTrue(errDateSorted.is_cached, 'incorrect errDateSorted.is_cached')

# COMMAND ----------

# MAGIC %md
# MAGIC **(4f) Exercise: Visualizing the 404 Response Codes by Day**
# MAGIC Using the results from the previous exercise, use `matplotlib` to plot a "Line" or "Bar" graph of the 404 response codes by day.

# COMMAND ----------

# ANSWER
daysWithErrors404 = errDateSorted.map(lambda (x, y): x).collect()
errors404ByDay = errDateSorted.map(lambda (x, y): y).collect()

print daysWithErrors404
print errors404ByDay

# COMMAND ----------

# TEST Visualizing the 404 Response Codes by Day (4f)
Test.assertEquals(daysWithErrors404, [1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22], 'incorrect daysWithErrors404')
Test.assertEquals(errors404ByDay, [243, 303, 346, 234, 372, 532, 381, 279, 314, 263, 195, 216, 287, 326, 258, 269, 255, 207, 312, 305, 288], 'incorrect errors404ByDay')

# COMMAND ----------

# PRIVATE_TEST Visualizing the 404 Response Codes by Day (4f)
Test.assertEquals(daysWithErrors404, [1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22], 'incorrect daysWithErrors404')
Test.assertEquals(errors404ByDay, [243, 303, 346, 234, 372, 532, 381, 279, 314, 263, 195, 216, 287, 326, 258, 269, 255, 207, 312, 305, 288], 'incorrect errors404ByDay')

# COMMAND ----------

fig = plt.figure(figsize=(8,4.2), facecolor='white', edgecolor='white')
plt.axis([0, max(daysWithErrors404), 0, max(errors404ByDay)])
plt.grid(b=True, which='major', axis='y')
plt.xlabel('Day')
plt.ylabel('404 Errors')
plt.plot(daysWithErrors404, errors404ByDay)
display(fig)  
pass

# COMMAND ----------

# MAGIC %md
# MAGIC **(4f-dbc) Exercise (Optional): Visualizing the 404 Response Codes by Day using Databricks Cloud Plots**
# MAGIC Using the results from exercise (4e), use Databricks Cloud plots to plot a "Line" or "Bar" graph of the 404 response codes by day.

# COMMAND ----------

# ANSWER
#First use a `map` function with a `lambda` function that returns a Row object consisting of `day` and `errors404`, and then use the `sqlContext.createDataFrame` function. Call the `display` function on the resulting DataFrame.
#After running this sequence, select either the "Line" or "Bar" graph option, and then use "Plot Options..." and drag `day` to the key entry field and drag `key` to the value entry field.
errDateDF = sqlContext.createDataFrame(errDateSorted.map(lambda (x, y): Row(day=x, errors404=y)))
display(errDateDF)

# COMMAND ----------

# MAGIC %md
# MAGIC **(4g) Exercise: Top Five Days for 404 Response Codes **
# MAGIC Using the RDD `errDateSorted` you cached in the part (4e), what are the top five days for 404 response codes and the corresponding counts of 404 response codes?

# COMMAND ----------

# ANSWER
#Simply return a list the top five dates by using the `takeOrdered` function with a `lambda` function that multiplies the count (second field) by -1.
topErrDate = (errDateSorted
              .takeOrdered(5, lambda s: -1 * s[1]))
print 'Top Five dates for 404 requests: %s' % topErrDate

# COMMAND ----------

# TEST Five dates for 404 requests (4g)
Test.assertEquals(topErrDate, [(7, 532), (8, 381), (6, 372), (4, 346), (15, 326)], 'incorrect topErrDate')

# COMMAND ----------

# PRIVATE_TEST Five dates for 404 requests (4g)
Test.assertEquals(topErrDate, [(7, 532), (8, 381), (6, 372), (4, 346), (15, 326)], 'incorrect topErrDate')

# COMMAND ----------

# MAGIC %md
# MAGIC **(4h) Exercise: Hourly 404 Response Codes**
# MAGIC Using the RDD `badRecords` you cached in the part (4a) and by hour of the day and in increasing order, create an RDD containing how many requests had a 404 return code for each hour of the day (midnight starts at 0). Cache the resulting RDD hourRecordsSorted and print that as a list.

# COMMAND ----------

# ANSWER
#The basic algorithm is as follows:
#1. Extract the hour field from the RDD of 404 records you cached in part (a) by using a `map` function with a `lambda` function that selects the `hour` field and creates a pair with 1.
#2. Count the number of 404 records per hour by using a `reduceByKey` function with a `lambda` function that combines the two elements.
#3. Use the `sortByKey` function to sort the list by hour (key) and cache the result.
#4. Return a hourly sorted list by using the `take` function.
hourCountPairTuple = (badRecords
                      .map(lambda log: (log.date_time.hour, 1)))

hourRecordsSum = (hourCountPairTuple
                  .reduceByKey(lambda a, b : a + b))

hourRecordsSorted = (hourRecordsSum
                     .sortByKey()
                     .cache())

errHourList = (hourRecordsSorted
               .take(24))
print 'Top hours for 404 requests: %s' % errHourList

# COMMAND ----------

# TEST Hourly 404 response codes (4h)
Test.assertEquals(errHourList, [(0, 175), (1, 171), (2, 422), (3, 272), (4, 102), (5, 95), (6, 93), (7, 122), (8, 199), (9, 185), (10, 329), (11, 263), (12, 438), (13, 397), (14, 318), (15, 347), (16, 373), (17, 330), (18, 268), (19, 269), (20, 270), (21, 241), (22, 234), (23, 272)], 'incorrect errHourList')
Test.assertTrue(hourRecordsSorted.is_cached, 'incorrect hourRecordsSorted.is_cached')

# COMMAND ----------

# PRIVATE_TEST Hourly 404 response codes (4h)
Test.assertEquals(errHourList, [(0, 175), (1, 171), (2, 422), (3, 272), (4, 102), (5, 95), (6, 93), (7, 122), (8, 199), (9, 185), (10, 329), (11, 263), (12, 438), (13, 397), (14, 318), (15, 347), (16, 373), (17, 330), (18, 268), (19, 269), (20, 270), (21, 241), (22, 234), (23, 272)], 'incorrect errHourList')
Test.assertTrue(hourRecordsSorted.is_cached, 'incorrect hourRecordsSorted.is_cached')

# COMMAND ----------

# MAGIC %md
# MAGIC **(4i) Exercise: Visualizing the 404 Response Codes by Hour**
# MAGIC Using the results from the previous exercise, use `matplotlib` to plot a "Line" or "Bar" graph of the 404 response codes by hour.

# COMMAND ----------

# ANSWER
hoursWithErrors404 = hourRecordsSorted.map(lambda (x, y): x).collect()
errors404ByHours = hourRecordsSorted.map(lambda (x, y): y).collect()

print hoursWithErrors404
print errors404ByHours

# COMMAND ----------

# TEST Visualizing the 404 Response Codes by Hour (4i)
Test.assertEquals(hoursWithErrors404, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23], 'incorrect hoursWithErrors404')
Test.assertEquals(errors404ByHours, [175, 171, 422, 272, 102, 95, 93, 122, 199, 185, 329, 263, 438, 397, 318, 347, 373, 330, 268, 269, 270, 241, 234, 272], 'incorrect errors404ByHours')

# COMMAND ----------

# PRIVATE_TEST Visualizing the 404 Response Codes by Hour (4i)
Test.assertEquals(hoursWithErrors404, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23], 'incorrect hoursWithErrors404')
Test.assertEquals(errors404ByHours, [175, 171, 422, 272, 102, 95, 93, 122, 199, 185, 329, 263, 438, 397, 318, 347, 373, 330, 268, 269, 270, 241, 234, 272], 'incorrect errors404ByHours')

# COMMAND ----------

fig = plt.figure(figsize=(8,4.2), facecolor='white', edgecolor='white')
plt.axis([0, max(hoursWithErrors404), 0, max(errors404ByHours)])
plt.grid(b=True, which='major', axis='y')
plt.xlabel('Hour')
plt.ylabel('404 Errors')
plt.plot(hoursWithErrors404, errors404ByHours)
display(fig)  
pass

# COMMAND ----------

# MAGIC %md
# MAGIC **(4i-dbc) Exercise(Optional): Visualizing the 404 Response Codes by Hour using Databricks Cloud Plots**
# MAGIC Using the results from exercise (4h), plot a "Line" or "Bar" graph of the 404 response codes by hour.

# COMMAND ----------

# ANSWER
#First use a `map` function with a `lambda` function that returns a Row object consisting of `day` and `errors404`, and then use the `sqlContext.createDataFrame` function. Call the `display` function on the resulting DataFrame.
#After running this sequence, select either the "Line" or "Bar" graph option, and then use "Plot Options..." and drag `day` to the key entry field and drag `key` to the value entry field.
errHourDF = sqlContext.createDataFrame(hourRecordsSorted
                                       .map(lambda (x, y): Row(hour=x, errors404=y)))
display(errHourDF)