// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// MAGIC %md
// MAGIC # Anomaly Detection with Iterative Quantile Estimation and T-digest
// MAGIC 
// MAGIC Project group 11 
// MAGIC 
// MAGIC Alexander Karlsson, Alvin Jin and George Osipov
// MAGIC 
// MAGIC 13 January 2021
// MAGIC  

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ### Links to videos
// MAGIC 
// MAGIC Link: https://liuonline-my.sharepoint.com/:f:/g/personal/geoos58_liu_se/El8VTHoZPVpDqpkdkpmJK8IB0Bd-YZw0t5-WRKeTXsqckA?e=faazbx (expires 28/02/2020).
// MAGIC 
// MAGIC The folder contains both the original recording (1x, ~25min) and the *recommended* sped-up recording (1.25x, ~20min). The latter is shorter and more fun.

// COMMAND ----------

// MAGIC %md
// MAGIC ## 1. Introduction
// MAGIC Anomaly detection is often implemented as a threshold detector where scalar valued scores (often computed from a higher dimensional sample) above a set threshold are classified as detections. It is often desired that the number of false alarms, i.e. non-anomalous samples that have higher score than the threshold, should be constant. This requires an adaptive threshold if the distribution of the scores varies with time. In this project we will look at two aspects of anomaly detection 
// MAGIC 1. How to calulate the threshold or quantile for a fixed distribution 
// MAGIC 2. How to apply this quantile to distributions that change over time using a simple filter
// MAGIC 
// MAGIC For quantile estimation (QE) we will use t-digest and compare it to a more naive approach which will be presented later. A problem for both these methods are data streams that arise from distributions that change over time. Assume that each sample received a time \\(t\\) can be written as
// MAGIC 
// MAGIC $$ x(t) = f(t) + w(t), $$
// MAGIC 
// MAGIC where \\(f(t)\\) is a trend value that varies with time and \\(w(t)\\) is a random variable with a distribution that may also vary with time. We are interested in finding anomalies in \\(w(t)\\). If we were to estimate a quantile from samples obtained from a time interval \\( T_s\\), the anomalies would depend on both \\(f(t)\\) and \\(w(t)\\), e.g. if \\(f(t)\\) is a linearly increasing function and \\(w(t)\\) is constant, most of the anomalous samples would be the more recent ones. This could be mitigated by taking samples from a small enough interval such that \\(f(t)\\) and \\(w(t)\\) can be considered constant during that time. This approach requires a continuous update of the estimated quantile, which we denote \\(q[n]\\), where \\(n\\) is the index of the time interval at time \\(nT_s\\). In some cases this may be a sufficently accurate solution. However, assume now that \\(w(t)\\) is constant but will occasionally change to a distribution with higher mean, i.e. this change is now the anomaly we are trying to detect. If we use the same target quantile in all time steps, these anomalies would go undetected. A compromise is to filter the stream of estimated quantiles \\(q[1],q[2],...,q[n]\\) in a manner that preserves scalability. The data stream we will look at will have the following form. Each time step yields \\(N\\) samples with Gaussian distribution with standard deviation \\( \sigma\\)=1 and mean 
// MAGIC $$ \mu[n]=\frac{n}{1000}, $$
// MAGIC and with 5\\(\%\\) probability, 1\\(\%\\) of the data will have mean
// MAGIC $$ \mu[n]=\frac{n}{1000} + 2. $$

// COMMAND ----------

// MAGIC %md
// MAGIC ## 2. QE with t-digest
// MAGIC With t-digest the distribution is estimated using a set of clusters where each cluster is represented by a mean value and weight (the number of samples assigned to the cluster). Clusters at the tail ends of the distribution will have smaller weights. This is determined by a non-decreasing function referred to as a scale function and will result in an error in the QE that is relative to the quantile rather than an absolute error, which is the fundamental idea with t-digest. Any quantile can be estimated by interpolating between cluster points. The algorithm is explained in detail in [Dunning]. The clusters can be computed in a scalable manner which makes the algorithm suitable for large datasets.

// COMMAND ----------

// MAGIC %md
// MAGIC ## 3. Naive QE
// MAGIC A simpler and perhaps more naive approach for empirical QE is to estimate the desired quantile as the the \\(k\\)'th ordered statistic, i.e. the value \\(q=x_k\\) for which \\(k\\) samples are smaller or equal to \\(q\\), e.g. the 95'th percentile from 1000 samples would then be estimated as the 950'th ordered statistic. If the data is i.i.d. the estimated quantile will then be a random variable with pdf [Rohling]
// MAGIC 
// MAGIC $$ p(x_k)= k  {N\choose k} \left[ P(x) \right]^{k-1} \left[ 1-P(x) \right]^{N-k} p(x), $$
// MAGIC 
// MAGIC where \\( x_k\\) is the \\(k\\)'th ordered statistic, \\(P(x)\\) is the cdf of the random variable \\(x\\) and \\(p(x)\\) is the pdf.
// MAGIC 
// MAGIC However, if the data is distributed, sorting becomes problematic. We therefore present an iterative, and more scalable, method of finding the desired quantile (or rather, the ordered statistic). We start with a random guess \\(\hat{q}\\) (e.g. the mean) and count the number of samples that are larger than \\(\hat{q}\\). This can be done in a distributed fashion where each node reports on the number of samples greater than \\(\hat{q}\\) as well as the total number of samples in each node. These number are then aggregated at the master node and the ratio yields an estimate of a quantile for \\(\hat{q}\\). If this is larger than the desired quantile, \\(\hat{q}\\) should be decreased and vice versa. One then proceeds by iteratively changing \\(\hat{q}\\) until the desired quantile is found. The search can be made efficient using the following steps
// MAGIC 
// MAGIC 1. Choose an integer \\(k\\) that correspends to the desired quantile, e.g. \\(k\\)=950 for \\(N\\)=1000 (95'th percentile).
// MAGIC 
// MAGIC 2. Arbitrarily choose an initial guess of \\(\hat{q}\\).
// MAGIC 
// MAGIC 3. Count the number of samples that are greater than \\(\hat{q}\\), call this \\(M\\). If  \\(M > N-k\\) increase \\(\hat{q}\\) by 1, then by 2,4,8,16 etc. until \\( M < N-k\\) (or reverse this process if \\(M\\) is initially lower than \\(N-k\\) ). We now have an upper limit (U) and a lower limit (L) for the desired quantile, \\(\hat{q}_U\\) and \\(\hat{q}_L\\). Let \\(d\\) = \\(\hat{q}_U\\) - \\(\hat{q}_L\\).
// MAGIC 
// MAGIC 4. Let \\(\hat{q} = \hat{q}_L\\). In each iteration update \\( \hat{q} \leftarrow \hat{q} + ud/2 \\) and then \\(d \leftarrow d/2\\) where  \\(u\\) is +1 if \\(M > N-k\\) and -1 otherwise. Stop iterating when \\(M=N-k\\).
// MAGIC 
// MAGIC 
// MAGIC This approach will converge to a solution in time proportional to log\\(_2(N)\\). For other types of iterative searches for finding an emperical quantile see [Möller].

// COMMAND ----------

// MAGIC %md
// MAGIC ## 4. Filtering Time Varying Quantiles
// MAGIC An interesting problem with both the presented method for QE and t-digest is how to balance new and old estimates. One approach is to make one new estimate for each new batch. This will fail however if one batch suddenly contains a larger burst of  "outliers" as these might then go undetected due to the temporary change of the statistics. 
// MAGIC 
// MAGIC Another is to estimate the desired quantiles from one batch and then keep this estimate in the proceeding batches. This will also fail if the distribution varies slowly, i.e. with time the calculated parameters will correspond to different quantiles than what was originally desired. One could mitigate this effect by averaging or updating new estimates with older. However if we estimate the quantiles based on all samples from the beginning up until time \\(n\\) we need to weight them properly, otherwise we may still have large errors due to distributions that change with time.
// MAGIC 
// MAGIC A simple tradeoff is to introduce a filter with some forget rate \\(T\\), i.e. samples that are older than \\(T\\) no longer effect the current estimate while the more recent estimates are weighted together. A basic approach is an equally weighted sliding window of size \\(T\\) where
// MAGIC 
// MAGIC $$ \bar{q}[n]= \frac{1}{T}\sum_{i=0}^{T-1}q[n-i] $$
// MAGIC 
// MAGIC is the weighted estimate and \\(q[n]\\) is the quantile estimated from batch/time \\(n\\). This requires storing \\(T\\) samples in a memory and writing (i.e. replacing the oldest sample with the newest) in each time step, which may or may not be an issue. Another is to have a filter with exponential decay where each sample \\(q[i]\\) for \\(i=0,...,n\\) is weighted by a factor  \\( ce^{-(n-i)/\tau}\\) for \\(\tau>0\\) and 
// MAGIC 
// MAGIC $$ c= \left[\sum_{i=0}^{\infty}e^{-i/\tau}\right]^{-1} = 1 - e^{-1/\tau}. $$
// MAGIC 
// MAGIC The weights of previous samples at time \\(n=100\\) for two different values of \\(\tau\\) are shown below
// MAGIC 
// MAGIC 
// MAGIC ![Im1](https://raw.githubusercontent.com/gosip/wasp-scadamale-sketching/main/impRe.jpg)
// MAGIC 
// MAGIC 
// MAGIC This can be simply implemented with the filter
// MAGIC 
// MAGIC $$ \bar{q}[n]= \bar{q}[n-1]e^{-1/\tau} + cq[n] = \sum_{i=0}^{n}cq[i]e^{(i-n)/\tau}. $$
// MAGIC 
// MAGIC The sum of \\(n\\) weights is 
// MAGIC 
// MAGIC $$ \sum_{i=0}^{n} ce^{-i/\tau} = c\frac{1 - e^{-n/\tau}}{1-e^{-1/\tau}} = 1 - e^{-n/\tau} $$
// MAGIC 
// MAGIC which can be approximated as one, e.g. if \\(n=5\tau\\) the weights mass is greater than 0.99. If we regard \\(q[n]\\) as a random variable with expected value \\(\mu\\) and variance \\(\sigma^2\\) that have been approximately constant for a duration of \\(L\gg\tau\\) samples the filter will be asymptotically unbiased
// MAGIC 
// MAGIC $$ \mathbf{E}\left[q[n] \right] = \mathbf{E}\left[\sum_{i=0}^{n}cq[i]e^{(i-n)/\tau}  \right] = \mathbf{E}\left[q[i] \right] \sum_{i=0}^{n}ce^{-i/\tau} = \mu(1 - e^{-n/\tau})\approx \mu. $$
// MAGIC 
// MAGIC Assuming for the sake of analysis that \\(\mu=0\\) and that \\(q[n]\\) is uncorrelated, i.e. \\( \mathbf{E}\left[q[j]q[i] \right]= \sigma^2 \\) if \\(j=i\\) and  \\( \mathbf{E}\left[q[j]q[i] \right]= 0 \\) otherwise, we get the variance as
// MAGIC 
// MAGIC $$ \text{var}\left[q[n] \right] =  \mathbf{E}\left[\left(\sum_{i=0}^{n}cq[i]e^{(i-n)/\tau}  \right)^2\right] = \mathbf{E}\left[q[i]^2 \right]  \sum_{i=0}^{n}c^2e^{-2i/\tau}  = \sigma^2c^2\frac{(1 - e^{-2n/\tau})}{(1 - e^{-2/\tau})}   = \sigma^2\frac{(1 - e^{-1/\tau})^2(1 - e^{-2n/\tau})}{(1 - e^{-1/\tau})(1 + e^{-1/\tau})} \approx \sigma^2\frac{(1 - e^{-1/\tau})}{(1 + e^{-1/\tau})} = \gamma\sigma^2$$
// MAGIC 
// MAGIC i.e. a reduction by a factor \\(\gamma\\). This can be compared to the factor \\(1/T\\) which is the reduction in variance from a rectangular sliding window of length \\(T\\), e.g. if \\(T=100\\) we get the same steady state reduction in variance if \\(\tau=50\\). The value of \\(\gamma\\) as a function of \\(\tau\\) is shown below
// MAGIC 
// MAGIC ![Im2](https://raw.githubusercontent.com/gosip/wasp-scadamale-sketching/main/gammagainRe.jpg)
// MAGIC 
// MAGIC The transfer function for this filter in \\(z\\)-domain is 
// MAGIC $$    H(z) = \frac{\bar{Q}(z)}{Q(z)} = \frac{1-e^{-1/\tau}}{1 - e^{-1/\tau}z^{-1}}. $$ The frequency response, obtained by letting \\(z=e^{j2\pi\nu}\\) where the normalized frequency is \\( \nu = T_sf\\), \\(f\\) is the frequency and \\(T_s\\) is the time between samples, is shown below
// MAGIC 
// MAGIC ![Im3](https://raw.githubusercontent.com/gosip/wasp-scadamale-sketching/main/fresp2.jpg)
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC The figure shows the tradeoff between supressing (in magnitude) higher frequencies and following changes (i.e. keeping phase). The trend that the filter should follow should be slow enough such that it can be approximated as constant for a duration of \\(L\gg\tau \\), i.e. depending of the frequency of the desired trend, the sampling rate, \\(1/T_s\\), needs to be high enough to satisfy this. If the flow of samples is constant this will in turn limit the number of samples in each batch and the smallest quantile that can be estimated.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ## 5. Implementation 

// COMMAND ----------

// neccessary imports
import org.isarnproject.sketches.java.TDigest
import org.isarnproject.sketches.spark.tdigest._
import scala.util.Random
import scala.util.Random._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.Dataset
import scala.math._
import org.apache.commons.math3.distribution.NormalDistribution

// COMMAND ----------

// Define 2-Gaussian mixture model
// This code is taken from [041_SketchingWithTDigest]

def myMixtureOf2Normals( normalLocation: Double, abnormalLocation: Double, normalWeight: Double, r: Random) : Double = {
  val sample = if (r.nextDouble <= normalWeight) {r.nextGaussian+normalLocation } 
               else {r.nextGaussian + abnormalLocation} 
  return sample
   }

// COMMAND ----------

// Define the naive QE function
def itrQuantEstimator[D](data:Dataset[D], target: Int): Double = {
  // find an interval
  var x = 0.0
  var x_old = 0.0
  var qfound=false
  var step = 1.0
  var Na_old = data.filter($"value">x).count() 
  var Na = Na_old
  var scale=1.0
  if (Na_old < 10){scale= (-1)}
  step = step*scale  
  var Nitr = 0
  while (qfound == false){
    // updata iteretion count
    Nitr = Nitr + 1
    // update x
    x = x + step
    // update step
    step = step*2
    Na = data.filter($"value">x).count()
    if (Na*scale < target*scale){
      qfound = true}
    else{
      Na_old=Na
      x_old=x
    }
  }

  // set upper and lower limit
  var UL = x_old
  var LL=x
  if (x_old < x){UL = x; LL=x_old }

  // Find the quantile for current batch
  var Int = UL - LL
  qfound = false
  x=LL
  scale=1
  while (qfound == false){
    // updata iteretion count
    Nitr = Nitr + 1
    // update x
    Int = Int/2
    x = x + scale*Int
    Na = data.filter($"value">x).count()
    if (Na == target){
      qfound = true}
    else if (Na < target){
           // decrease x
      scale= -1
    }
    else if(Na > target){
    // increase x
      scale= 1
    }
  
  }
  return x
}

// COMMAND ----------

// Estimate quantiles in loop 

val N = 100000 // samples per batch

// Naive QE parameters
val Nmk = 10 // integer "N - k"

// t-digest parameters
val targetQ = 1.0 - Nmk.toDouble/N.toDouble
val comp = 0.2 // Compression parameter
val Nc = 25 // Number of bins
val udf_tDigest = TDigestAggregator.udf[Double](comp,Nc)

// filter parameters
val tau = 20.0
val c = 1.0 - exp(-1.0/tau)
var q_tf = 0.0 //filterd t-digets quantile estimate
var q_nf = 0.0 //filterd naive quantile estimate

// loop parameters
val T = 500 // time or number of batches
var resMap = scala.collection.mutable.Map[Int,(Int,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double)]() // create an empty map for storing results
var q_true = 0.0 // true quantile
var Na_true = 0.0 // true number of 
var rr = 0.0 // realisation of anomalies
var Na_t1 = 0.0 // number of anomalies using t-digest estimate from first batch 
var Na_tf = 0.0 // number of anomalies using filtered t-digest estimate from current batch 
var Na_n1 = 0.0 // number of anomalies using naive estimate from first batch 
var Na_nf = 0.0 // number of anomalies using filtered naive estimate from current batch 
var q_t1 = 0.0 // first QE with t-digets
var q_n1 = 0.0 // first QE with naive QE
var q_t = 0.0 // t-digest quantile estimate
var q_n = 0.0 // naive quantile estimate

// data parameters
var mu1=0.0
var mu2=0.0
var wN=1.0
val seed = 10L
val r = new Random(seed) // create random instace with "seed"


// Start loop
for( t <- 1 to T){
  //get batch of data
  rr=r.nextFloat
  if( rr < 0.95 )
  {wN=1.0} // All data is normal
  else
  {wN=0.99} // 1% of data is anomalous
  mu1=t.toDouble/1000.0 
  mu2=mu1 + 2
  val data = sc.parallelize(Vector.fill(N){myMixtureOf2Normals(mu1, mu2, wN, r)}).toDF.as[Double]
  
  
  //do t-digest
  val agg = data.agg(udf_tDigest($"value"))
  val td = agg.first.getAs[TDigest](0) 
  q_t = td.cdfInverse(targetQ)
  
  if( t == 1 ){q_t1 = q_t} // save first quantile estimate  
  if( t == 1 ){q_tf = q_t}else{q_tf = q_t*c + exp(-1.0/tau)*resMap(t-1)._5}   // if first batch use no filter weight
  Na_t1 = data.filter($"value">q_t1).count()
  Na_tf = data.filter($"value">q_tf).count()
  
  //do naive QE
  q_n = itrQuantEstimator(data,Nmk)
  
  if( t == 1 ){q_n1 = q_n} // save first quantile estimate  
  if( t == 1 ){q_nf = q_n}else{q_nf = q_n*c + exp(-1.0/tau)*resMap(t-1)._9}   // if first batch use no filter weight
  Na_n1 = data.filter($"value">q_n1).count()
  Na_nf = data.filter($"value">q_nf).count()
  
  //get true quantile and true number of anomalies (ignoring anomalies) 
  val normalDataDistribution = new NormalDistribution(mu1, 1);
  q_true = normalDataDistribution.inverseCumulativeProbability(targetQ)

  val abnormalDataDistribution = new NormalDistribution(mu2, 1);
  var cdf_N = normalDataDistribution.cumulativeProbability(q_true)
  var cdf_A = abnormalDataDistribution.cumulativeProbability(q_true)

  Na_true = N*wN*(1.0-cdf_N) + N*(1.0-wN)*(1.0-cdf_A)

  
  // save results
  resMap += (t -> (t,q_true,Na_true,q_t,q_tf,Na_t1,Na_tf,q_n,q_nf,Na_n1,Na_nf))
  println("Batch Number: "+ t)

}

// Put results into dataframe for presentation
val resL = resMap.toList.map(_._2) // convert to list and extract data
val resS = resL.sortBy(x => x._1) // sort
val res_all = resS.toDF("Time index, n","true quantile","true number of anomalies","QE with t-digest","filtered QE with t-digest","number of anomalies with fix t-digest quantile","number of anomalies with filtered t-digest quantile","Naive QE","filtered naive QE","number of anomalies with fix naive QE","number of anomalies with filtered naive QE") // convert to DF


// COMMAND ----------

// MAGIC %md
// MAGIC ## 6. Results

// COMMAND ----------

// Plot estimated and true quantiles
display(res_all)

// COMMAND ----------

// MAGIC %md
// MAGIC We see that the filtered quantiles are not affected by the bursts of anomalies, seen as spikes in the instantaneous estimates. We also note that the difference between quantiles using the t-digest and naive method is small. The number of iterations in each time step for the naive method varied between  4 and 18 with a mean of 10 iterations. This can be reduced by using the estimated quantile in step \\(n-1\\) as the initial guess in step \\(n\\).

// COMMAND ----------

// Plot number of anomalies and true number of anomalies 
display(res_all)

// COMMAND ----------

// MAGIC %md
// MAGIC If a fixed quantile is used the number of anomalies will increase with time due to the changing statistics of the data distribution. By properly filtering the estimates, the number of anomalies are kept at a constant level (10 in this case) in time slots with the normal distribution while still detecting the bursts of outliers (seen as spikes with 52.7 calculated anomaleties) in time slots with the abnormal distribution. The emperical mean relative errors for the quantile defined as
// MAGIC 
// MAGIC $$ \varepsilon_q  = \frac{1}{T}\sum_{n=1}^{T}\frac{|q[n] - \hat{q}[n]|}{q[n]}$$ 
// MAGIC 
// MAGIC was 0.0052 with the naive QE and 0.0054 with t-digest, using the filtered quantiles. The corresponding error for the number of anomalies (this is ideally \\( N-k = 10 \\) in normal batches and 52.7 in abnormal batches), was 0.23 with naive QE and 0.24 with t-digest, i.e. both methods perform equally in this case. 

// COMMAND ----------

// MAGIC %md
// MAGIC ## 7. Discussion
// MAGIC 
// MAGIC ### 7.1 Iterative method vs t-digest
// MAGIC 
// MAGIC One difference between the iterative method and t-digest is that the latter is updated with a single sample at a time, while the former must store the whole batch of \\(N\\) samples, and thus might require more space. On the other hand, the iterative method has a simple implementation that relies only on the basic arithmetic operations, while t-digest requires evaluations of scale functions which contain relatively expensive operations such as log and sin\\(^{-1}\\). This could suggest that iterative method can be implemented to work faster than t-digest. Developing an efficient implementation and comparing these methods in terms of time complexity would be an interesting future research direction.
// MAGIC 
// MAGIC 
// MAGIC ### 7.2 Sketching with the iterative method 
// MAGIC 
// MAGIC Another difference is that the t-digest outputs a sketch -- a compressed object that can be used to obtain estimates of different statistics of the original data stream, e.g. estimates of several different quantiles. On the other hand, the iterative method in the form presented above can only be used to estimate one quantile. A way to obtain a data sketch with the iterative method is to fix a step size \\(\alpha\\) and to maintain \\(\frac{1}{\alpha}\\) quantiles for \\(\alpha\\), \\(2\alpha\\), \\(3\alpha\\), ... If we want to get an estimate of a quantile that is not stored in the sketch, we can take two values from the sketch such that the desired quantile falls between them and then iterpolate (e.g. linearly). The space complexity of this sketch is \\(O(\frac{1}{\alpha})\\), i.e. it depends linearly on the desired level of accuracy. Having in mind that our target application is anomaly detection, one could modify this solution to better correspond to the task at hand: instead of evenly spacing the quantiles, one could be more fine-grained towards the endpoints of the interval [0,1] and have larger bins in the middle.
// MAGIC 
// MAGIC 
// MAGIC ### 7.3 Alternative approaches
// MAGIC 
// MAGIC An alternative approach to quantile estimation is KLL (see https://arxiv.org/pdf/1603.05346.pdf). 
// MAGIC It is a data-agnostic method, i.e. no prior distribution is assumed on the data stream.
// MAGIC The data points are only assumed to be pairwise comparable.
// MAGIC In this model, KLL is provably optimal.
// MAGIC The basic idea of the algorithm is the following.
// MAGIC The data is processes by _compactors_, which are subroutines that have arrays of given capacity. 
// MAGIC A compactor collects elements until the maximum capacity is reached, then it sorts the elements and removes either the odd-indexed ones or the even-indexed ones (deciding randomly).
// MAGIC Afterwards, the compactor passed the remaining elements to another compactor.
// MAGIC The full algorithm is a chain of compactors.
// MAGIC By choosing appropriate capacities for compactors at each level, KLL achieves provably optimal worst-case performance.
// MAGIC 
// MAGIC One might be interested in comparing KLL with t-digest and the iterative method empirically.
// MAGIC It would also be interesting to mathematically analyze t-digest and the naive QE in the distribution-agnostic model of KLL.

// COMMAND ----------

// MAGIC %md
// MAGIC ## 8. References
// MAGIC 
// MAGIC [Rohling] H. Rohling, "Radar CFAR Thresholding in Clutter and Multiple Target Situations," in IEEE Transactions on Aerospace and Electronic Systems, vol. AES-19, no. 4, pp. 608-621, July 1983, doi: 10.1109/TAES.1983.309350.
// MAGIC 
// MAGIC [Möller] Möller, E., Grieszbach, G., Shack, B., & Witte, H. (2000). Statistical properties and control algorithms of recursive quantile estimators. Biometrical Journal, 42(6), 729–746.
// MAGIC 
// MAGIC [Dunning] Dunning, T., & Ertl, O. (2019). Computing extremely accurate quantiles using t-digests. arXiv preprint arXiv:1902.04023.
// MAGIC 
// MAGIC [041_SketchingWithTDigest]  https://lamastex.github.io/scalable-data-science/sds/2/2/db/041_SketchingWithTDigest/