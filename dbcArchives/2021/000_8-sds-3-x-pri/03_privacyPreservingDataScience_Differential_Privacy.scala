// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// DBTITLE 1,Differential Privacy: An introduction
// MAGIC %md
// MAGIC 
// MAGIC ###### by [Christoffer Långström](https://www.linkedin.com/in/christoffer-l%C3%A5ngstr%C3%B6m-7a460514b/)
// MAGIC 
// MAGIC This is part 3 of the in a series of notebooks discussing different aspects on privacy preserving methods in data science. 
// MAGIC 
// MAGIC Differential privacy, originally proposed by Cynthia Dwork et. al (2006) treats the problem of privacy from the perspective that simply being included in a data set is a risk. The solution proposed through diffential privacy is to make it so that a data set that contains a certain individual is not meaningfully differentiable from the same data set with that person removed. 
// MAGIC 
// MAGIC The proposal was to make it so that a random algorithm could be considered epsilon diff. private if the distribution of the estimator does not "change much" with the inclusion/exclusion of any particular individual, with epsilon being the "privacy factor" that quantifies to what extent the distributions differ. See below for more details. 
// MAGIC 
// MAGIC The advantage of this approach is that we can now quantify the degree of privacy of a random algorithm, and start to develop methods that enforce a desired level of privacy in an algorithm. Further, there is research being done (Duchi et. al) on *local* differential privacy, where the true data is kept hidden from even the data scientist working on it, by applying differentially private mechanisms to the data, before any statistics is computed. The type of privacy preserving mechanism that is applied depends on the application, where min/max optimal estimators are found for several canonical statistical problems. 
// MAGIC 
// MAGIC ### Links:
// MAGIC 
// MAGIC * Introduction to DP in the context of network protocols:  
// MAGIC [Diffential Privacy at IETF104 by Amelia Andersdotter and Christoffer Långström](https://www.youtube.com/watch?v=PlSX-o36Wus&feature=youtu.be&t=3809)
// MAGIC 
// MAGIC * Locally private statistical procedures:  
// MAGIC [Minimax Optimal Procedures for Locally Private Estimation](https://arxiv.org/abs/1604.02390)
// MAGIC 
// MAGIC * Original DP paper:  
// MAGIC [Cynthia Dwork et. al](http://people.csail.mit.edu/asmith/PS/sensitivity-tcc-final.pdf)

// COMMAND ----------

// DBTITLE 1,e-Differential Privacy: Mathematical Definition
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

displayHTML(frameIt("https://en.wikipedia.org/wiki/Differential_privacy#Definition_of_ε-differential_privacy",350))

// COMMAND ----------

// DBTITLE 1,First Approach: Algorithmic Sanitation 
// MAGIC %md 
// MAGIC One of the first suggested approaches to enforcing e-differential privacy was by introducing "noise" to the ouput of estimators. This approach works as thus: For a dataset *X*, compute the statistic *T(X)*, generate some noise *W*, return *T(X) + W* as the result. If *W* is symmetric noise, and chosen in such a way that it does not greatly distort the data, the goal is to achieve reasonably good estimates that cannot be meaningfully reversed engineered to 
// MAGIC reveal information on the people included in the data set. 
// MAGIC 
// MAGIC ####The Laplace Mechanism
// MAGIC 
// MAGIC This approach to adding noisy data involves adding samples from a standard laplace distribution, with the scale parameter chosen depending on the sensitivity of the data and the desired privacy level. The laplace distribution is a symmetric "double exponential" distribution with slim tails and a sharp center, here taken to be at the origin. The algorithm for applying laplace noise to a data set is as follows:
// MAGIC 
// MAGIC Let *D* be a data set and *T* a statistic to be computed, and *epsilon* the desired degree of privacy. 
// MAGIC * Compute the sensitivity \\( \Delta(T(D)) \\) of the statistic T to changes in the data set D
// MAGIC * Generate noise W = \\( Laplace(\Delta(T(D))/ \epsilon) \\)
// MAGIC * Compute & return the result \\( T'(D) = T(D) + W \\)
// MAGIC 
// MAGIC The statistic *T'* is now an \\( \epsilon \\)- differentially private version of *T*, as defined above. 
// MAGIC 
// MAGIC ##### Some notes: 
// MAGIC * For small values of  \\( \epsilon \\) (i.e high degrees of privacy) significant levels of noise is added and so the values of *T'* will exhibit large variance for repeated queries and may provide unaccetably poor performance in most applications. 
// MAGIC 
// MAGIC * This approach only protects against information being divulged from the output of T by an adversiarl party, the data still needs to be stored safely by a trusted holder. This is the problem adressed by local DP, discussed below.  

// COMMAND ----------

// DBTITLE 1,Example 1): R- Code for basic DP mean value estimation
// MAGIC %r 
// MAGIC #  Generate a data set
// MAGIC n = 200
// MAGIC heights = rnorm(n, 180, 5)
// MAGIC 
// MAGIC # Compute Sensitivity: 
// MAGIC #-------------------------------------------------------
// MAGIC f = rep(0, n)
// MAGIC for(i in 2:n-1){
// MAGIC     a = 1:(i-1)
// MAGIC     b = (i+1):n
// MAGIC     heights2 = c(heights[a], heights[b]) 
// MAGIC     f[i] = abs(mean(heights) - mean(heights2)) 
// MAGIC }
// MAGIC 
// MAGIC sensitivity = max(f)
// MAGIC print(max(f))
// MAGIC #-------------------------------------------------------
// MAGIC 
// MAGIC # Add noise: 
// MAGIC #-------------------------------------------------------
// MAGIC install.packages("rmutil")
// MAGIC install.packages("ggplot2")
// MAGIC require(rmutil)
// MAGIC require(ggplot2)
// MAGIC 
// MAGIC epsilon1 = 1
// MAGIC epsilon2 = 0.25
// MAGIC mse1 = rep(0,n)
// MAGIC mse2 = rep(0,n)
// MAGIC 
// MAGIC for(i in 1:n){
// MAGIC   dat = heights[1:i]
// MAGIC   out1 = mean(dat) + rlaplace(1, 0, sensitivity/epsilon1)
// MAGIC   out2 = mean(dat) + rlaplace(1, 0, sensitivity/epsilon2)
// MAGIC   mse1[i] = (180 - out1)^2
// MAGIC   mse2[i] = (180 - out2)^2
// MAGIC }
// MAGIC 
// MAGIC # Plot the results
// MAGIC x = 1:n
// MAGIC df = data.frame(x, mse1, mse2)
// MAGIC 
// MAGIC p = ggplot(df, aes(x)) +                    # basic graphical object
// MAGIC   geom_line(aes(y=mse1), colour= "blue") +  # first layer
// MAGIC   geom_line(aes(y=mse2), colour= "orange") # second layer
// MAGIC p + labs(title = "Effect of Laplace Noise on MSE(Sample Size)") + xlab("Sample Size") + ylab("MSE")
// MAGIC 
// MAGIC 
// MAGIC #-------------------------------------------------------

// COMMAND ----------

// DBTITLE 1,Example 2: Differentially Private SGD (Google Brain)
// MAGIC 
// MAGIC %md
// MAGIC <img src="http://i.imgur.com/eMoJkND.png" alt="drawing" width="500"/>
// MAGIC 
// MAGIC 
// MAGIC #### Read More in the paper:
// MAGIC [Additive Noise Models in Deep Learning](https://arxiv.org/abs/1607.00133)

// COMMAND ----------

// DBTITLE 1,Second Approach: Data Sanitation / Locally Private Estimation
// MAGIC %md
// MAGIC 
// MAGIC This approach, as suggested by Duchi, Jordan, et. al, is based on privatizing the data before any statistics are computed, effectively meaning that the data is hidden even from the data scientist. Their suggestion is to apply a differentially private mechanism to the data and provide such mechanisms for some canonical problems. 
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC ####Add more details

// COMMAND ----------

// DBTITLE 1,Example 3 : Local DP Min/Max Mean Estimation 
// MAGIC %md 
// MAGIC #### Experiment setup: 
// MAGIC As an example application, [Duchi et. al](https://arxiv.org/abs/1604.02390) considers the problem of estimating means of drug use related hospitalizations. In the original problem, they have records of hospitalization of the form (drugs, year, # of hospitalizations) where the number of hospitalizations denotes how many people that year where admitted and used a certain drug. Many of the admitted individuals used several drugs, as the number of drugs were vastly larger than the number of admittances. We will use this information to generate a data set of measurements of the form  \\( X_i \in {0,1}^d \\) representing one admission; a component *j* is 1 if the patient used drug *j* and 0 else. The usage frequency will be uniformly distributed over the samples generated, with only the constraint 
// MAGIC that the total number of admittances using drug *j* matches a preset number. This means that if there were *n* patients and *k* of them used drug *j* then \\( X_ij = 1 \\) with probability *k/n*. 
// MAGIC 
// MAGIC 
// MAGIC #### Methods: 
// MAGIC We have N = 1000 measurements of \\( X_i \in {0,1}^d \\) for *d = 27*, with a preset vector of admittance frequency. The problem is to estimate the mean of this population, \\( \theta = E(X) \\) and we will use three different approaches: 
// MAGIC * Non-private estimation
// MAGIC * Min/Max optimal local privacy mechanism
// MAGIC * Local Laplace Noise 
// MAGIC 
// MAGIC Non-private estimation is simply the sample mean of the raw data, which is the "gold standard" of estimates that private methods are necesserally worse than. The min/max locally private mechanism consists of generating a new sample from the raw data which is sequentially dependent, in such a way that the new sample is differntially private and min/max optimal w.r.t to an arbitray loss function. This can be compared by the theoretical lower bound obtained in the paper. Lastly, one can compare this to a "naive" local privacy mechanism of adding *laplace(epsilon/d)* distributed noise to the data before estimation. Each method is analyzed through the l-infinity norm of the deviation from the full sample mean, as a function of the sample size.  

// COMMAND ----------

// DBTITLE 1,L-Inf Sampler Privacy Mechanism (Duchi. Et al)
// MAGIC %md
// MAGIC 
// MAGIC <img src="http://i.imgur.com/oimTT7o.png" alt="drawing" width="750"/>

// COMMAND ----------

// DBTITLE 1,Example 3: R functions for privacy sampling, data generation & component means
// MAGIC %r
// MAGIC 
// MAGIC #l-infinity sampling based privacy mechanism, as suggested by
// MAGIC l_inf_samp = function(X, alpha){
// MAGIC 
// MAGIC   n = length(X[,1])
// MAGIC   d = length(X[1,])
// MAGIC   samples = matrix(c(rep(0, n*d)), ncol = d, nrow = n)
// MAGIC   sample = rep(0,d)
// MAGIC   for(j in 1:n){
// MAGIC     x = X[j,]
// MAGIC     r = max(abs(x)) 
// MAGIC     x_hat = rep(0,d)
// MAGIC 
// MAGIC     for(i in 1:d){
// MAGIC       p = 0.5 + x[i]/(2*(r+(r==0)))
// MAGIC       y = rbinom(1, 1, p)
// MAGIC       x_hat[i] = (y==1)*r - (y==0)*r
// MAGIC     }
// MAGIC     
// MAGIC     A = 2^(d-1)
// MAGIC     
// MAGIC     if(d%%2 == 1){
// MAGIC       C = (1/A)*choose(d-1, 0.5*(d-1))
// MAGIC     } else{
// MAGIC       C = 1/(A+0.5*choose(d, 0.5*d))*choose(d-1, 0.5*d)
// MAGIC     }
// MAGIC     
// MAGIC     B = r*((exp(alpha)+1)/(exp(alpha)-1))*(1/C)
// MAGIC     
// MAGIC     pi_alph = exp(alpha)/(exp(alpha) + 1)
// MAGIC     T_alph = rbinom(1, 1, pi_alph)
// MAGIC     accept = FALSE
// MAGIC     
// MAGIC     while(!accept){
// MAGIC       z = runif(d, -B, B)
// MAGIC       dot = sum(z*x_hat)
// MAGIC       if((dot >= 0)&&(T_alph == 1)){
// MAGIC         sample = z
// MAGIC         accept = TRUE
// MAGIC       }
// MAGIC       else if((dot <= 0)&&(T_alph == 0)){
// MAGIC         sample = z 
// MAGIC         accept = TRUE
// MAGIC       }
// MAGIC     }
// MAGIC     samples[j,] = sample
// MAGIC   }
// MAGIC   return(samples)
// MAGIC }
// MAGIC 
// MAGIC 
// MAGIC my_mean = function(X){
// MAGIC   v_sum = rep(0,d)
// MAGIC   N = length(X[,1])
// MAGIC   for(i in 1:N){
// MAGIC     v_sum = v_sum + X[i,]  
// MAGIC   }
// MAGIC   return(v_sum/N)
// MAGIC }
// MAGIC 
// MAGIC 
// MAGIC gen_data = function(N,d,prop){
// MAGIC   X = matrix(c(rep(0,d*N)), ncol=d, nrow=N)
// MAGIC   for(i in 1:d){
// MAGIC     count = 0
// MAGIC     while(count < prop[i]){
// MAGIC       idx = floor(runif(1,1,N))
// MAGIC       if(X[idx,i] == 0){
// MAGIC         X[idx,i] = 1
// MAGIC         count = count + 1
// MAGIC       }
// MAGIC     }
// MAGIC   }
// MAGIC   return(X)
// MAGIC }

// COMMAND ----------

// DBTITLE 1,Example 3 cont: R code
// MAGIC %r
// MAGIC library(rmutil)
// MAGIC library(ggplot2)
// MAGIC 
// MAGIC # Set Parameters 
// MAGIC d = 27
// MAGIC alpha = 0.5
// MAGIC 
// MAGIC N_max = 1000
// MAGIC N_low = 100
// MAGIC 
// MAGIC # Initialize storage
// MAGIC error1 = rep(0, N_max-N_low)
// MAGIC error2 = rep(0, N_max-N_low)
// MAGIC error3 = rep(0, N_max-N_low)
// MAGIC low_bound = rep(0, N_max-N_low)
// MAGIC 
// MAGIC # Generate Data 
// MAGIC proportions = c(6, 2, 4, 2, 1, 1, 2, 6, 5, 4, 1, 9, 2, 6, 2, 1, 6, 2, 8, 5, 2, 8, 7, 5, 6, 4, 2)*N_max/15
// MAGIC X = gen_data(N_max, d, proportions)
// MAGIC theta_true = my_mean(X)
// MAGIC 
// MAGIC for(i in N_low:N_max){
// MAGIC   N = i
// MAGIC   
// MAGIC   X_n = X[1:i,]
// MAGIC   Z = l_inf_samp(X_n, alpha)
// MAGIC   
// MAGIC   W = matrix(rlaplace(N*d, 0, d/alpha), ncol = d, nrow = N)
// MAGIC   
// MAGIC   theta1 = my_mean(X_n)
// MAGIC   theta2 = my_mean(Z)
// MAGIC   theta3 = my_mean(X_n + W)
// MAGIC   
// MAGIC   error1[i-N_low+1] = max(abs(theta_true - theta1))
// MAGIC   error2[i-N_low+1] = max(abs(theta_true - theta2)) 
// MAGIC   error3[i-N_low+1] = max(abs(theta_true - theta3)) 
// MAGIC 
// MAGIC   a = d*log(2*d)
// MAGIC   b = (exp(alpha)-1)^2
// MAGIC   low_bound[i-N_low+1]  = sqrt(a/(N*b))
// MAGIC }
// MAGIC 
// MAGIC # Plot the results
// MAGIC x = N_low:(N_max)
// MAGIC df = data.frame(x, error2, error3)
// MAGIC 
// MAGIC p = ggplot(df, aes(x)) +                    # basic graphical object
// MAGIC   geom_line(aes(y=error1), colour= "red") +  # Non-private estimate
// MAGIC   geom_line(aes(y=error2), colour= "blue") + # Locally Private estimate
// MAGIC   geom_line(aes(y=error3), colour= "orange") + # Laplace Mechanism
// MAGIC   geom_line(aes(y=low_bound), colour= "black")  # Min/Max optimal bound
// MAGIC 
// MAGIC p +  ggtitle("L-inf Error of different estimation methods as a function of sample size") +
// MAGIC   xlab("Sample Size") + ylab("l-inf Error") + labs()