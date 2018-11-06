###################################################
### chunk number 1: Initialize
###################################################
library(numDeriv)
library(nlme)
Orange$dates <- as.Date(Orange$age,origin="1968-12-31")
Orange$season <- rep(c(-1,-1,1,1,-1,1,-1),5)/2
attach(Orange)
options(continue=" ")


###################################################
### chunk number 2: Orange-tree-data
###################################################
Orange


###################################################
### chunk number 3: Model-3
###################################################
f <- function(beta, b) {
     (beta["Asym"] + rep(b[1:5], each = 7))/
     (1 + exp((beta["xmid"] - age)/beta["scal"])) }
h <- function(b, beta, sigma, sigma.b) { 
     -sum(dnorm(x = circumference, mean = f(beta, b), 
                sd = sigma, log = TRUE)) - 
     sum(dnorm(x = b[1:5], sd = sigma.b, log = TRUE)) }
l.LA <- function(theta) {
     beta <- theta[1:3]
     sigma <- exp(theta[4])
     sigma.b <- exp(theta[5])
     est <- nlminb(start = rep(0,5), objective = h, beta = beta, 
                   sigma = sigma, sigma.b = sigma.b)
     b <- est$par
     h.b <- est$objective
     Jac.f <- jacobian(func = f, x = b, beta = beta)
     D <- crossprod(Jac.f)/sigma^2 + diag(1/sigma.b^2, 5)
     h.b + 1/2 * log(det(D/(2 * pi))) }


###################################################
### chunk number 4: Model-3-estimation
###################################################
theta0 <- c(Asym=170,xmid=700,scal=400,sigma=log(10),sigma.b=log(30))
nfit <- nlminb(theta0,l.LA)
hess1 <- hessian(l.LA,nfit$par)


###################################################
### chunk number 5: Model-3-mle
###################################################
nfit$objective
rbind(nfit$par,round(sqrt(diag(solve(hess1))),2))
rownames(hess1) <- colnames(hess1) <- names(theta0)
round(cov2cor(solve(hess1)),2)


###################################################
### chunk number 6: Model-3-fit
###################################################
ltyt=c(1,2,4,5,6,7)
bhat <- nlminb(rep(0,5),h,beta=nfit$par[1:3],
               sigma=exp(nfit$par[4]), sigma.b = exp(nfit$par[5]))$par
fit.values <- f(nfit$par[1:3],bhat)
par(mar=c(5,4,1,2)+.1)
plot.new()
plot.window(xlim=c(0,1700),ylim=c(0,220),yaxs="i",xaxs="i")
for(i in 1:5)
  points(age[1:7+(i-1)*7],Orange$circumference[1:7+(i-1)*7],pch=19,cex=.8,col=1+i)
for(i in 1:5)
  lines(age[1:7+(i-1)*7],fit.values[1:7+(i-1)*7],lty=ltyt[i],col=1+i)
box(bty="l"); axis(1); axis(2)
title(xlab='Days since 31 Dec 1968',ylab='Trunk circumference (mm)')
legend('topleft',legend=paste('Tree',1:5),lty=ltyt,bty="n",,col=1+1:5)


###################################################
### chunk number 7: Model-3-residuals
###################################################
fit.res <- circumference-fit.values
par(mar=c(5,4,1,2)+.1)
plot.new()
plot.window(xlim=c(0,1700),ylim=c(-18,18),xaxs="i")
points(age,fit.res,pch=19,cex=.8,col=rep(1+1:5,each=7))
title(xlab='',ylab='Residuals (mm)')
box(bty="l"); axis(2,las=2)
axis(1,age,paste(months(dates,abbreviate=TRUE),format(dates, '%d'),sep="-"),las=3,lwd=0)
rug(age,lwd=2,tck=-.03,lend=2)
abline(h=0)
rug(seq(from=0,by=365,to=2000),tck=0.05)
rug(seq(from=0,by=365/2,to=2000),tck=0.02)
text(x=seq(from=365,by=365,to=1500),y=-16.5,labels=c("'70","'71","'72","'73"))


###################################################
### chunk number 8: Model-4
###################################################
f2 <- function(beta,b)
  (  beta['Asym']+rep(b[1:5],each=7) + rep(b[6:12],5)) /
  (1+exp((beta['xmid']-age)/beta['scal'])) 
h2 <- function(b,beta,sigma,sigma.b,sigma.b2)
  -sum(dnorm(x=circumference,mean=f2(beta,b),sd=sigma,log=TRUE))-
  sum(dnorm(x=b[1:5],sd=sigma.b,log=TRUE))-sum(dnorm(x=b[6:12],sd=sigma.b2,log=TRUE))
l2.LA <- function(theta) {
  beta <- theta[1:3]
  sigma <- exp(theta[4])
  sigma.b <- exp(theta[5])
  sigma.b2 <- exp(theta[6])
  est <- nlminb(rep(0,12),h2,beta=beta,sigma=sigma,sigma.b=sigma.b,sigma.b2=sigma.b2)
  b <- est$par
  h.b <- est$objective
  Jac.f <- jacobian(f2,x=est$par,beta=beta)#,method.arg=list(eps=1e-4,r=2,d=1))
  D <- crossprod(Jac.f)/sigma^2 + diag(c(rep(1/sigma.b^2,5),rep(1/sigma.b2^2,7)))
  h.b+1/2*log(det(D/(2*pi)))
}


###################################################
### chunk number 9: Model-4-estimation
###################################################
theta02 <- c(Asym=170,xmid=700,scal=400,sigma=log(10),sigma.b=log(30),sigma.b2=log(10))
nfit2 <- nlminb(theta02,l2.LA)
hess2 <- hessian(l2.LA,nfit2$par)


###################################################
### chunk number 10: Model-4-mle
###################################################
nfit2$objective
rbind(nfit2$par,sqrt(diag(solve(hess2))))
rownames(hess2) <- colnames(hess2) <- names(theta02)
round(cov2cor(solve(hess2)),2)


###################################################
### chunk number 11: Model-4-millar
###################################################
par.M <- c(Asym = 195.87, xmid = 747.61, scal = 352.68,
            sigma = log(sqrt(28.08)), sigma.b = log(sqrt(1059.79)), sigma.b2 = log(sqrt(109.10)))
(logLik.M <- -l2.LA(par.M))
(logLik.reported <- -125.447)
-nfit2$objective
-nfit2$objective-logLik.M
-nfit2$objective-logLik.reported
logLik.M-logLik.reported


###################################################
### chunk number 12: Model-4-fit
###################################################
bhat2 <- nlminb(rep(0,12),h2,beta=nfit2$par[1:3], sigma=exp(nfit2$par[4]),
               sigma.b = exp(nfit2$par[5]), sigma.b2 = exp(nfit2$par[6]))$par
fit2.values <- f2(nfit2$par[1:3],bhat2)
par(mar=c(5,4,1,2)+.1)
plot.new()
plot.window(xlim=c(0,1700),ylim=c(0,220),yaxs="i",xaxs="i")
for(i in 1:5)
  points(age[1:7+(i-1)*7],circumference[1:7+(i-1)*7],pch=19,cex=.8,col=1+i)
for(i in 1:5)
  lines(age[1:7+(i-1)*7],fit2.values[1:7+(i-1)*7],lty=ltyt[i],col=1+i)
box(bty="l"); axis(1); axis(2)
title(xlab='Days since 31 Dec 1968',ylab='Trunk circumference (mm)')
legend('topleft',legend=paste('Tree',1:5),lty=ltyt,bty="n")


###################################################
### chunk number 13: Model-4-res
###################################################
fit2.res <- circumference-fit2.values
par(mar=c(5,4,1,2)+.1)
plot.new()
plot.window(xlim=c(0,1700),ylim=c(-18,18),xaxs="i")
points(age,fit2.res,pch=19,cex=.8,col=rep(1+1:5,each=7))
for(i in 1:5)
  lines(age[1:7+(i-1)*7],fit2.res[1:7+(i-1)*7],lty=ltyt[i],col=i+1)
title(xlab='Days since 31 Dec 1968',ylab='Residuals (mm)')
box(bty="l"); axis(1); axis(2,las=2)
abline(h=0)


###################################################
### chunk number 14: Model-4-profile
###################################################
l2.LA.profile <- function(theta,sigma.b2) l2.LA(c(theta,sigma.b2))
sig.b2.vec <- rev(c(seq(from=-1,to=1.5,by=.5),seq(from=1.7,to=4,by=.2),
                    seq(from=4,to=5,by=.5)))
est <- vector(length=length(sig.b2.vec),'list')
start <- nfit2$par[-6]
for(i in 1:length(sig.b2.vec)) {
  try(
      est[[i]] <- nlminb(start,l2.LA.profile,sigma.b2=sig.b2.vec[i])
      )
  if(!is.null(est[[i]]))
    start <- est[[i]]$par
}
vals <- numeric(length=length(sig.b2.vec))
for(i in 1:length(sig.b2.vec))
  vals[i] <- est[[i]]$objective


###################################################
### chunk number 15: Model-4-profile-ci
###################################################
sig.b2.sf <- splinefun(sig.b2.vec,-vals-(-nfit2$objective-1.920729))
(sig.b2.ci <- exp(c(uniroot(sig.b2.sf,c(0,2))$root,uniroot(sig.b2.sf,c(2,5))$root)))


###################################################
### chunk number 16: Model-4-profile-plot
###################################################
par(mar=c(3.5,4,.5,.8)+.1)
xvec <- exp(sig.b2.vec)
plot(spline(xvec,exp(-vals+nfit2$objective),n=1000),type="l",
     lwd=2,ylim=c(0,1),xlab='',ylab='Relative profile likelihood',
     yaxs="i",bty="l",xaxs="i",
     xlim=c(0,52),las=1)
abline(h=0.1465)
abline(h=0.03625)
text(52,0.1465,'95% limit',adj=c(1,-.3))
text(52,0.03625,'99% limit',adj=c(1,-.3))
mtext(expression(sigma[b2]),1,line=2.2)
abline(v=sig.b2.ci,lty=2)


###################################################
### chunk number 17: Model-4-est-lik
###################################################
scale=1.5
n=15
theta = nfit2$par
obs.I = hess2
len=length(theta)
pd = diag(solve(obs.I))
sd = sqrt(pd)
hd = diag(obs.I)
val <- x <- taylor <- profil <- matrix(0,nrow=len,ncol=n)
for(i in 1:len) {
  x[i,] = seq(from=theta[i]-sd[i]*scale,
     to=theta[i]+sd[i]*scale,length.out=n)
  x0 = x[i,(n+1)/2]
  for(j in 1:n) {
    tmp.th = theta
    tmp.th[i] = x[i,j]
    val[i,j] = -l2.LA(tmp.th)
  }
}


###################################################
### chunk number 18: Model-4-est-lik-plot
###################################################
  par(mfrow=c(2,3),mar=c(3.,1,.5,.5),oma=c(0,4,.5,0))
for(i in 1:len) {
  x0 = x[i,(n+1)/2]
  plot.new()
  plot.window(ylim=c(-127,-125),
              xlim=range(x[i,]),yaxs="i")
  lwdf=.75
  axis(1,padj=-.8,tck=-.07,lwd=1*lwdf)
  if(i %in% c(1,4))
    axis(2,-127:-125,las=2,lwd=1*lwdf)
  title(xlab=c(names(theta)[1:3],'log(sigma)','log(sigma.b)','log(sigma.b2)')[i],line=1.5)
  lines(spline(x[i,], val[i,], n = 201), lwd = 2*lwdf)
  abline(v=x0,lty=3,lwd=2*lwdf)
  abline(h=-nfit2$objective,lty=3,lwd=2*lwdf)
  abline(h=-127)
}


###################################################
### chunk number 19: Model-5
###################################################
f1b <- function(beta, b) {
     (beta["Asym"] + rep(b[1:5], each = 7))/
     (1 + exp((beta["xmid"] - age)/beta["scal"] - beta['season']*season)) }
h1b <- function(b, beta, sigma, sigma.b) { 
     -sum(dnorm(x = circumference, mean = f1b(beta, b), 
                sd = sigma, log = TRUE)) - 
     sum(dnorm(x = b[1:5], sd = sigma.b, log = TRUE)) }
l1b.LA <- function(theta) {
     beta <- theta[1:4]
     sigma <- exp(theta['sigma'])
     sigma.b <- exp(theta['sigma.b'])
     est <- nlminb(start = rep(0,5), objective = h1b, beta = beta, 
                   sigma = sigma, sigma.b = sigma.b)
     b <- est$par
     h.b <- est$objective
     Jac.f <- jacobian(func = f1b, x = b, beta = beta)
     D <- crossprod(Jac.f)/sigma^2 + diag(1/sigma.b^2, 5)
     h.b + 1/2 * log(det(D/(2 * pi))) }


###################################################
### chunk number 20: Model-5-estimation
###################################################
theta01b <- c(Asym=170,xmid=700,scal=400,season=.1,sigma=log(10),sigma.b=log(30))
nfit1b <- nlminb(theta01b,l1b.LA, control = list(trace=0))
hess1b <- hessian(l1b.LA,nfit1b$par)


###################################################
### chunk number 21: Model-5-mle
###################################################
nfit1b$objective
rbind(nfit1b$par,sqrt(diag(solve(hess1b))))
rownames(hess1b) <- colnames(hess1b) <- names(theta01b)
round(cov2cor(solve(hess1b)),2)


###################################################
### chunk number 22: Model-5-fit
###################################################
bhat1b <- nlminb(rep(0,5),h1b,beta=nfit1b$par[1:4],
               sigma=exp(nfit1b$par['sigma']), sigma.b = exp(nfit1b$par['sigma.b']))$par
fit1b.values <- f1b(nfit1b$par[1:4],bhat1b)
par(mar=c(5,4,1,2)+.1)
plot.new()
plot.window(xlim=c(0,1700),ylim=c(0,220),yaxs="i",xaxs="i")
for(i in 1:5)
  points(age[1:7+(i-1)*7],circumference[1:7+(i-1)*7],pch=19,cex=.8,col=1+i)
for(i in 1:5)
  lines(age[1:7+(i-1)*7],fit1b.values[1:7+(i-1)*7],lty=ltyt[i],col=1+i)
box(bty="l"); axis(1); axis(2)
title(xlab='Days since 31 Dec 1968',ylab='Trunk circumference (mm)')
legend('topleft',legend=paste('Tree',1:5),lty=ltyt,bty="n",col=1+1:5)


###################################################
### chunk number 23: Model-5-res
###################################################
fit1b.res <- circumference-fit1b.values
par(mar=c(5,4,1,2)+.1)
plot.new()
plot.window(xlim=c(0,1700),ylim=c(-18,18),xaxs="i")
points(age,fit1b.res,pch=19,cex=.8,col=rep(1+1:5,each=7))
title(xlab='Days since 31 Dec 1968',ylab='Residuals (mm)')
box(bty="l"); axis(2,las=2)
axis(1)
abline(h=0)
rug(seq(from=0,by=365,to=2000),tck=0.05)
rug(seq(from=0,by=365/2,to=2000),tck=0.02)
text(x=seq(from=365,by=365,to=1500),y=-16.5,labels=c("'70","'71","'72","'73"))
for(i in 1:5)
  lines(age[1:7+(i-1)*7],fit1b.res[1:7+(i-1)*7],lty=ltyt[i],col=1+i)


###################################################
### chunk number 24: Model-4+6
###################################################
Sigma.CAR <- function(phi, sigma) {
     diff <- (age[1:7] - rep(age[1:7], each=7))
     delta.t <- matrix(diff / (365 / 2), 7, 7)
     P <- sigma^2 * exp( - phi * abs(delta.t))
     kronecker( diag(5), P) }

h4 <- function(b,beta,sigma,sigma.b,sigma.b2,phi) {
  Sigma <- Sigma.CAR(phi,sigma)
  resid <- circumference-f2(beta,b)
  .5*(log(det(2*pi*Sigma))+crossprod(resid,solve(Sigma,resid))) -
    sum(dnorm(x=b[1:5],sd=sigma.b,log=TRUE))-sum(dnorm(x=b[6:12],sd=sigma.b2,log=TRUE))
}

l4.LA <- function(theta) {
  beta <- theta[1:3]
  sigma <- exp(theta[4])
  sigma.b <- exp(theta[5])
  sigma.b2 <- exp(theta[6])
  phi <- exp(theta[7])
  est <- nlminb(rep(0,12), h4, beta=beta, sigma=sigma,
                sigma.b=sigma.b, sigma.b2=sigma.b2, phi=phi)
  b <- est$par
  h.b <- est$objective
  Jac.f <- jacobian(f2,x=est$par,beta=beta
                    ,method.arg=list(eps=1e-4,r=2,d=1))
  Sigma <- Sigma.CAR(phi,sigma)
  D <- crossprod(Jac.f,solve(Sigma,Jac.f)) + diag(c(rep(1/sigma.b^2,5),rep(1/sigma.b2^2,7)))
  h.b+1/2*log(det(D/(2*pi)))
}


###################################################
### chunk number 25: Model-4+6-estimation
###################################################
theta04 <- c(nfit2$par,phi=log(5))
nfit4 <- nlminb(theta04,l4.LA)
hess4 <- hessian(l4.LA,nfit4$par)


###################################################
### chunk number 26: Model-4+6-mle
###################################################
nfit4$objective
rbind(nfit4$par,sqrt(diag(solve(hess4))))
rownames(hess4) <- colnames(hess4) <- names(theta04)
round(cov2cor(solve(hess4)),2)


###################################################
### chunk number 27: Model-5+6
###################################################
h4b <- function(b,beta,sigma,sigma.b,phi) {
  Sigma <- Sigma.CAR(phi,sigma)
  resid <- circumference-f1b(beta,b)
  .5*(log(det(2*pi*Sigma))+crossprod(resid,solve(Sigma,resid))) -
    sum(dnorm(x=b[1:5],sd=sigma.b,log=TRUE))
}
l4b.LA <- function(theta) {
  beta <- theta[1:4]
  sigma <- exp(theta['sigma'])
  sigma.b <- exp(theta['sigma.b'])
  phi <- exp(theta['phi'])
  est <- nlminb(rep(0,5), h4b, beta=beta, sigma=sigma,
                sigma.b=sigma.b, phi=phi)
  b <- est$par
  h.b <- est$objective
  Jac.f <- jacobian(f1b,x=b,beta=beta,
                    method.arg=list(eps=1e-4,r=2,d=1))
  Sigma <- Sigma.CAR(phi,sigma)
  D <- crossprod(Jac.f,solve(Sigma,Jac.f)) + diag(1/sigma.b^2, 5)
  h.b+1/2*log(det(D/(2*pi)))
}


###################################################
### chunk number 28: Model-5+6-estimation
###################################################
theta04b <- c(nfit1b$par,phi=log(5))
nfit4b <- nlminb(theta04b,l4b.LA, control = list(trace=0))
hess4b <- hessian(l4b.LA,nfit4b$par)


###################################################
### chunk number 29: Model-5+6-mle
###################################################
nfit4b$objective
rbind(nfit4b$par,sqrt(diag(solve(hess4b))))
rownames(hess4b) <- colnames(hess4b) <- names(theta04b)
round(cov2cor(solve(hess4b)),2)


###################################################
### chunk number 30: Model-5+6-AR1
###################################################
exp(-exp(nfit4b$par['phi']))


###################################################
### chunk number 31: Model-5+6-profile
###################################################
l4b.LA.profile <- function(theta,phi) l4b.LA(c(theta,phi=phi))
phi.vec4b <- rev(c(-7,-5,seq(from=-4,to=3,by=.25)))
est4b <- vector(length=length(phi.vec4b),'list')
start <- nfit4b$par[-7]
for(i in 1:length(phi.vec4b)) {
  try(
      est4b[[i]] <- nlminb(start,l4b.LA.profile,phi=phi.vec4b[i])
      )
  if(!is.null(est4b[[i]]))
    start <- est4b[[i]]$par
}
vals4b <- rep(NA,length(phi.vec4b))
for(i in 1:length(phi.vec4b)) {
  if(!is.null(est4b[[i]]))
    vals4b[i] <- est4b[[i]]$objective
}


###################################################
### chunk number 32: Model-5+6-profile-ci
###################################################
phi.season.sf <- splinefun(phi.vec4b,-vals4b-(-nfit4b$objective-1.920729))
log.phi.season.ci <- c(uniroot(phi.season.sf,c(-2,3))$root,uniroot(phi.season.sf,c(-4,-2))$root)
(rho.ci <- exp(-exp(log.phi.season.ci)))


###################################################
### chunk number 33: Model-5+6-prof-plot
###################################################
par(mar=c(3.5,4,.5,.8)+.1)
xvec <- exp(phi.vec4b)
plot(spline(exp(-xvec),exp(-vals4b+nfit4b$objective),n=1500),type="l",bty="l",
     lwd=2,ylim=c(0,1),xlim=c(0.2,1),xlab="",ylab='Relative profile likelihood',
     las=1,yaxs="i",xaxs="i")
segments(0,-nfit1b$objective,20,-nfit1b$objective)
segments(0,-nfit1b$objective,-1,-nfit1b$objective,lty="2121")
abline(h=-nfit4b$objective+qnorm(.025)^2/-2,lty=3,lwd=2)
title(xlab=expression(rho),line=2.2)
abline(h=0.1465)
abline(h=0.03625)
text(.22,0.1465,'95% limit',adj=c(0,-.3))
text(.22,0.03625,'99% limit',adj=c(0,-.3))
abline(v=rho.ci,lty=2)


###################################################
### chunk number 34: Model-10
###################################################
f5 <- function(beta,b)
  (  beta['Asym']+rep(b[1:5],each=7) ) /
  (1+exp((beta['xmid']-age)/beta['scal'] - rep(b[6:12],5))) 
h5 <- function(b,beta,sigma,sigma.b,sigma.b2)
  -sum(dnorm(x=circumference,mean=f5(beta,b),sd=sigma,log=TRUE))-
  sum(dnorm(x=b[1:5],sd=sigma.b,log=TRUE))-sum(dnorm(x=b[6:12],sd=sigma.b2,log=TRUE))
l5.LA <- function(theta) {
  beta <- theta[1:3]
  sigma <- exp(theta[4])
  sigma.b <- exp(theta[5])
  sigma.b2 <- exp(theta[6])
  est <- nlminb(rep(0,12),h5,beta=beta,sigma=sigma,sigma.b=sigma.b,sigma.b2=sigma.b2)
  b <- est$par
  h.b <- est$objective
  Jac.f <- jacobian(f5,x=est$par,beta=beta,method.arg=list(eps=1e-4,r=2,d=1))
  D <- crossprod(Jac.f)/sigma^2 + diag(c(rep(1/sigma.b^2,5),rep(1/sigma.b2^2,7)))
  h.b+1/2*log(det(D/(2*pi)))
}


###################################################
### chunk number 35: Model-10-estimation
###################################################
theta05 <- c(Asym=170,xmid=700,scal=400,sigma=log(10),sigma.b=log(30),sigma.b2=log(.1))
nfit5 <- nlminb(theta05,l5.LA) 


###################################################
### chunk number 36: Model-10-estimation2
###################################################
nfit5$objective
nfit5$message


###################################################
### chunk number 37: Model-10-estimation3
###################################################
ofit5 <- optim(theta05,l5.LA, method="BFGS",control = list(parscale=theta05))
hess5 <- hessian(l5.LA,nfit5$par)


###################################################
### chunk number 38: Model-10-mle
###################################################
ofit5$value
rbind(ofit5$par,sqrt(diag(solve(hess5))))
rownames(hess5) <- colnames(hess5) <- names(theta05)
round(cov2cor(solve(hess5)),2)


###################################################
### chunk number 39: Model-10-jle
###################################################
bfit5 <- nlminb(rep(0,12),h5,beta=ofit5$par[1:3],sigma=exp(ofit5$par[4]),
                sigma.b=exp(ofit5$par[5]),sigma.b2=exp(ofit5$par[6]))
bhat5 <- bfit5$par
hess.b5 <- hessian(h5,bhat5,beta=ofit5$par[1:3],sigma=exp(ofit5$par[4]),
                sigma.b=exp(ofit5$par[5]),sigma.b2=exp(ofit5$par[6]))


###################################################
### chunk number 40: Model-10-jle-plot
###################################################
par(mfrow=c(1,2))
for(b.nr in c(9,12)) {
  hess.b5.b.nr <- hess.b5[b.nr,b.nr]
  deltab <- sqrt(1/hess.b5.b.nr)*3.5
  bvec <- seq(from=bhat5[b.nr]-deltab,to=bhat5[b.nr]+deltab,length=51)
  h5val <- numeric(length(bvec))
  for(i in 1:length(bvec)) {
    tmp.b <- bhat5
    tmp.b[b.nr] <- bvec[i]
    h5val[i] <- -h5(tmp.b,beta=ofit5$par[1:3],sigma=exp(ofit5$par[4]),
                    sigma.b=exp(ofit5$par[5]),sigma.b2=exp(ofit5$par[6]))
  }
  plot(spline(bvec,exp(h5val)),type="l",las=1,ylab="",
       xlab=paste("Ran. ef.",b.nr),yaxs="i",bty="l",ylim=c(0,max(exp(h5val))))
  quadapp <- exp(-bfit5$objective -(bvec - bhat5[b.nr])^2/2*hess.b5.b.nr)
  lines(spline(bvec, quadapp), lty=2)
  abline(v=bhat5[b.nr],lty=3,lwd=2)
}


###################################################
### chunk number 41: Model-10-jle-b24
###################################################
h5.b24 <- function(b24) -h5(b=c(bhat5[1:8],b24,bhat5[10:12]),
                               beta=ofit5$par[1:3],sigma=exp(ofit5$par[4]),
                               sigma.b=exp(ofit5$par[5]),sigma.b2=exp(ofit5$par[6]))
h5.b24v <- Vectorize(function(b24) exp(h5.b24(b24))*1e54)
(exact.b24 <- integrate(h5.b24v,-100,100,rel.tol = .Machine$double.eps*100))
(val.laplace.b24 <- exp(h5.b24(bhat5[9])-.5*log(-hessian(h5.b24,bhat5[9])/(2*pi))))
(val.exact.b24 <- exact.b24$value/1e54)
(error.b24.pct <- (val.exact.b24-val.laplace.b24)/val.exact.b24*100)


###################################################
### chunk number 42: Model-10-jle-b27
###################################################
h5.b27 <- function(b27) -h5(b=c(bhat5[1:11],b27),
                               beta=ofit5$par[1:3],sigma=exp(ofit5$par[4]),
                               sigma.b=exp(ofit5$par[5]),sigma.b2=exp(ofit5$par[6]))
h5.b27v <- Vectorize(function(b27) exp(h5.b27(b27))*1e54)
(exact.b27 <- integrate(h5.b27v,-100,100,rel.tol = .Machine$double.eps*100))
(val.laplace.b27 <- exp(h5.b27(bhat5[12])-.5*log(-hessian(h5.b27,bhat5[12])/(2*pi))))
(val.exact.b27 <- exact.b27$value/1e54)
(error.b27.pct <- (val.exact.b27-val.laplace.b27)/val.exact.b27*100)


###################################################
### chunk number 43: Model-10-H-D
###################################################
est <- with(ofit5, nlminb(rep(0,12),h5,beta=par[1:3], sigma=exp(par[4]),
                          sigma.b=exp(par[5]), sigma.b2=exp(par[6])))
Jac.f <- jacobian(f5,x=est$par,beta=ofit5$par[1:3],
                   method.arg=list(eps=1e-4,r=6,d=.01))
D <- crossprod(Jac.f)/exp(ofit5$par[4])^2 +
     diag(c(rep(1/exp(ofit5$par[5])^2,5), rep(1/exp(ofit5$par[6])^2,7)))
H <- with(ofit5, hessian(h5,x=est$par, beta=par[1:3], sigma=exp(par[4]),
                          sigma.b=exp(par[5]), sigma.b2=exp(par[6]),
                          method.arg=list(eps=1e-4,r=6,d=.01)))
all.equal(H, D)
all.equal(log(det(H/(2*pi)))/2, log(det(D/(2*pi)))/2) 
log(det(D/(2*pi)))/2 
log(det(H/(2*pi)))/2 
1/2*log(det(H/(2*pi))) - 1/2*log(det(D/(2*pi))) 
rm(est,Jac.f,D,H)


###################################################
### chunk number 44: nlme-model-3
###################################################
nlme(circumference ~ SSlogis(age,Asym,xmid,scal),
     data = Orange,
     fixed = Asym + xmid + scal ~ 1,
     random = Asym ~ 1,
     start = c(Asym = 190, xmid = 700, scal = 350) )


###################################################
### chunk number 45: nlme-model-5
###################################################
logist <- deriv( ~ Asym/(1 + exp(-(    (x-xmid)/scal+seas*y       ))),
                c('Asym','xmid','scal','seas'),
                function(x,y,Asym,xmid,scal,seas){} )
nlme(circumference ~ logist(age,season,Asym,xmid,scal,seas),
     data = Orange,
     fixed = Asym + xmid + scal + seas ~ 1,
     random = Asym ~ 1,
     start = c(Asym = 217, xmid = 857, scal = 436, seas = 0.3))


###################################################
### chunk number 46: nlme-model-5+6
###################################################
nlme(circumference ~ logist(age,season,Asym,xmid,scal,seas),
     data = Orange,
     fixed = Asym + xmid + scal + seas ~ 1,
     random = Asym ~ 1,
     correlation = corCAR1(value=0.5, ~age/365*2|Tree),
     start = c(Asym = 217, xmid = 857, scal = 436, seas = 0.3))


