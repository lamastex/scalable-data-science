
# Simulated input vectors
set.seed(134)
n<-10
x.tmp<-seq(1,n,by=1)
x1<-sample(x.tmp)
x2<-sample(x.tmp,replace=TRUE)

# Define parameters and design matrix
theta<-c(-1,2,0.5)
X<-cbind(level=1,Effect1=x1,Effect2=x2)
summary(X)

# See specifix elements of X
X[ ,2:3]
X[c(1:2,6), ]
X[X[ , "Effect1"]==2 |
  X[ , "Effect2"]==6, ]
X[order(X[ , "Effect1"]), ]

# Simulate output vector
y<-as.matrix(X)%*%theta+
  rnorm(n,mean=0,sd=1)

# Estimate parameters using glm
M<-glm(y~-1+X,family=gaussian)
summary(M)
glm(y~X[ ,-1],family=gaussian)# equivalent

Mydata<-data.frame(Effect1=x1,Effect2=x2,
                   y=y)#with data frame
attach(Mydata)
glm(y~Effect1+Effect2,data=Mydata)

?glm

# Estimate parameters by matrix
# multiplication
theta.hat<-solve(t(X)%*%X)%*%t(X)%*%y   
theta.hat1<-solve(crossprod(X))%*%t(X)%*%y
# Equvalent, but faster

theta.hat
dim(theta.hat)
row.names(theta.hat)<-
  c("beta0","beta1","beta2")
theta.hat

# Paramter covariance matrix
e<-y-X%*%theta.hat1
sigma<-t(e)%*%e/(10-3)
theta.var<-as.numeric(sigma)*
  solve(t(X)%*%X)

sqrt(diag(theta.var))

# test values
t<-theta.hat/sqrt(diag(theta.var))

# p-values
2*(1-pt(abs(t),df=7))

summary(M)

# Estimation using general purpose
# optimazation

# Negative log-likelihood function
# (with known sigma)
logL<-function(theta,x,y){
  n<-length(y)
  mu<-x%*%theta
  sigma<-1
  return(-sum(dnorm(y,mean=mu,
                    sd=sqrt(sigma),
                    log=TRUE)))
}

# Initial parameter values
theta0<-c(0,0,0)

# Optimization
M2<-optim(theta0,logL,x=X,y=y,
          method="L-BFGS-B",
          lower=c(-10,-10,-10,0.001),
           upper=c(10,10,10,10),
          hessian=TRUE)

# Residual variance
sigma2<-sum((y-X%*%M2$par)^2)/(n-3)

# Parameter covariance
theta.var2<-solve(M2$hessian)*sigma2

?optim

?glm
?install.packages
?sample


# A couple of plots for the model
plot(X%*%theta.hat,y,
     xlab=expression(X*hat(theta)))

y.plot<-seq(2,25,by=0.1)
d<-dnorm(y.plot,mean=X[1,]%*%theta,
         sd=sqrt(sigma))
plot(y.plot,d,type="l",xlab="y",
     ylab="Likelihood")
points(y[1],dnorm(y[1],mean=X[1,]%*%theta,
                  sd=sqrt(sigma)),pch=19)
lines(c(y[1],y[1]),
      c(0,dnorm(y[1],mean=X[1,]%*%theta,
                sd=sqrt(sigma))),
      lty=2)
lines(c(y[1],0),
      rep(dnorm(y[1],mean=X[1,]%*%theta,
                sd=sqrt(sigma)),2),lty=2)


i<-9;col="blue"
d<-dnorm(y.plot,mean=X[i,]%*%theta,
         sd=sqrt(sigma))
lines(y.plot,d,type="l",col=col)
points(y[i],
       dnorm(y[i],mean=X[i,]%*%theta,
             sd=sqrt(sigma)),pch=19,
       col=col)
lines(c(y[i],y[i]),
      c(0,dnorm(y[i],mean=X[i,]%*%theta,
                sd=sqrt(sigma))),
      lty=2,col=col)
lines(c(y[i],0),
      rep(dnorm(y[i],mean=X[i,]%*%theta,
                sd=sqrt(sigma)),2),
      lty=2,col=col)


i<-10;col="red"
d<-dnorm(y.plot,mean=X[i,]%*%theta,
         sd=sqrt(sigma))
lines(y.plot,d,type="l",col=col)
points(y[i],dnorm(y[i],mean=X[i,]%*%theta,
                  sd=sqrt(sigma)),pch=19,
       col=col)
lines(c(y[i],y[i]),
      c(0,dnorm(y[i],mean=X[i,]%*%theta,
                sd=sqrt(sigma))),
      lty=2,col=col)
lines(c(y[i],0),
      rep(dnorm(y[i],mean=X[i,]%*%theta,
                sd=sqrt(sigma)),2),
      lty=2,col=col)


legend(x=2,y=0.4,
       legend=c(expression(y[1]),
         expression(y[9]),
         expression(y[10])),
       col=c(1,"blue","red"),pch=19)


# Dianostic plot for glm
par(mfrow=c(2,2))
plot(M)


?postscript

?pdf
