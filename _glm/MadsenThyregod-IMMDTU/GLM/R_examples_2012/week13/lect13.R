Ex<-expression(log((sin(phi*x+a))^2+x^2))
D(Ex,"x")

D(D(Ex,"x"),"x")

Ex<-expression(log((sin(phi*x+a))^2+x^2))
deriv(Ex,"x",func=function(x,a,phi,sigma){})



rm(list=ls())
library(numDeriv)

##################################################
# Data
##################################################

plate<-1:21
extract<-c(rep(0,11),rep(1,10))
seed<-c(rep(0,5),rep(1,6),rep(0,5),rep(1,5))
exse<-extract*seed
r<-c(10,23,23,26,17,5,53,55,32,46,10,8,10,8,23,0,3,22,15,
     32,3)
n<-c(39,62,81,51,39,6,74,72,51,79,13,16,30,28,45,4,12,41,
     30,51,7)

#source("~/tsmodels/graph.R")
#graph(width=14,height=9,type="eps",file="Germ")
plot(r/n,ylim=c(0,1),xlab="Plate",pch=19)
I<-1:21
lines(c(1,1)*(max(I[(seed+extract)==0])+0.5),c(-1,2),
      lty=2)
text(x=1,y=0.9,labels="Extract=0\nSeed=0",pos=4)
lines(c(1,1)*(max(I[seed==1 & extract==0])+0.5),c(-1,2),
      lty=2)
text(x=7,y=0.9,labels="Extract=0\nSeed=1",pos=4)
lines(c(1,1)*(max(I[seed==0 & extract==1])+0.5),c(-1,2),
      lty=2)
text(x=12,y=0.9,labels="Extract=1\nSeed=0",pos=4)
text(x=17,y=0.9,labels="Extract=1\nSeed=1",pos=4)
#dev.off()

##################################################
# numeric optimzation
##################################################
ll<-function(B,theta,x1,x2,x3,r,n){
  lin.pred<-theta["mu"]+x1*theta["beta1"]+
    x2*theta["beta2"]+x3*theta["beta3"]+B
  p<-1/(1+exp(-lin.pred))
  nll<--log(p)*r-log(1-p)*(n-r)+
    0.5*(log(theta["sig2"]^2)+B^2/theta["sig2"]^2)
  return(nll)
}

lTheta.num<-function(theta,B,extract,seed,exse,r,n,ll){
  # return Laplace approximation of negative log likelihood
  jnll<-0
  for(i in 1:length(B)){
    nll<-optim(0,ll,theta=theta,x1=extract[i],x2=seed[i],
               x3=exse[i],r=r[i],n=n[i],
               method="BFGS",hessian=TRUE)
    jnll<-jnll+nll$value+0.5*log(nll$hessian)
  }
  return(jnll)
}

theta<-c(mu=0,beta1=0,beta2=0,beta3=0,sig2=1)
B<-numeric(length(n))

system.time(OPnum<-nlminb(theta,lTheta.num,B=B,
                       extract=extract,seed=seed,exse=exse,
                       r=r,n=n,ll=ll))


##################################################
# log likelihood and derivatives
##################################################
obsll<-"-log(%s)*r-log(1-%s)*(n-r)+0.5*(log(sig2^2)+
         B^2/sig2^2)"
p<-"exp(mu+x1*beta1+x2*beta2+x3*beta3+B)/
         (1+exp(mu+x1*beta1+x2*beta2+x3*beta3+B))"

obsll<-sprintf(obsll,p,p) # replacing string 
obsll<-parse(text=obsll)  # Conveting to expression
# derivative of ll w.r.t. random effects
dobsll.B<-deriv(obsll,c("B"),
                function(B,mu,beta1,beta2,beta3,sig2,x1,
                         x2,x3,r,n){})
# Hessian of ll w.r.t random effects
dobsll.BEx<-D(obsll,c("B"))
Hobsll.B<-deriv(dobsll.BEx,c("B"),
          function(mu,beta1,beta2,beta3,sig2,B,x1,x2,x3,
                   r,n){})

lB<-function(B,mu,beta1,beta2,beta3,sig2,x1,x2,x3,r,n,
             dobsll.B){
  lin.pred<-mu+x1*beta1+x2*beta2+x3*beta3+B
  p<-1/(1+exp(-lin.pred))
  nll<--log(p)*r-log(1-p)*(n-r)+0.5*(log(sig2^2)+
                                     B^2/sig2^2)
  return(nll)
}


DlB<-function(B,mu,beta1,beta2,beta3,sig2,x1,x2,x3,r,n,
              dobsll.B){
  # return derivative of negative log-likelihod of B |
  # fixed effects
  attr(dobsll.B(B,mu,beta1,beta2,beta3,sig2,x1,x2,x3,r,n),
       "gradient")
}

lTheta<-function(theta,B,lB,DlB,extract,seed,exse,r,n,
                 dobsll.B){
  # return Laplace approximation of negative log likelihood
  jnll<-0
  for(i in 1:length(B)){
    ll<-nlminb(0,lB,gradient=DlB,mu=theta["mu"],
               beta1=theta["beta1"],beta2=theta["beta2"],
               beta3=theta["beta3"],sig2=theta["sig2"],
               x1=extract[i],x2=seed[i],x3=exse[i],r=r[i],
               n=n[i],dobsll.B=dobsll.B)
    jnll<-jnll+ll$objective
    B[i]<-ll$par
  }
  jnll<-jnll+0.5*sum(log(attr(Hobsll.B(mu=theta["mu"],
                 beta1=theta["beta1"],beta2=theta["beta2"],
                 beta3=theta["beta3"],sig2=theta["sig2"],
                 B=B,x1=extract,x2=seed,x3=exse,r=r,n=n),
                              "grad")))
  return(jnll)
}



##################################################
# Estimate parameters
##################################################

theta<-c(mu=0,beta1=0,beta2=0,beta3=0,sig2=1)
B<-numeric(length(n))

system.time(OP<-nlminb(theta,lTheta,B=B,lB=lB,DlB=DlB,
                extract=extract,seed=seed,exse=exse,
                r=r,n=n,dobsll.B=dobsll.B,
                        lower=c(-Inf,-Inf,-Inf,-Inf,0)))

system.time(OP2<-optim(OP$par,fn=lTheta,gr=NULL,B=B,
                lB=lB,DlB=DlB,extract=extract,seed=seed,
                exse=exse,r=r,n=n,dobsll.B=dobsll.B,
                lower=c(-Inf,-Inf,-Inf,-Inf,0),
                method="L-BFGS-B",hessian=T))

SE<-sqrt(diag(solve(OP2$hessian)))
Par<-cbind(OP2$par,SE,OP2$par-2*SE,OP2$par+2*SE)
Par

##################################################
# Plot results
##################################################

theta<-OP1$par
B<-numeric(length(n))
for(i in 1:length(B)){
  ll<-nlminb(0,lB,gradient=DlB,mu=theta["mu"],
             beta1=theta["beta1"],beta2=theta["beta2"],
             beta3=theta["beta3"],sig2=theta["sig2"],
             x1=extract[i],x2=seed[i],x3=exse[i],
             r=r[i],n=n[i],dobsll.B=dobsll.B)
  B[i]<-ll$par
}

#graph(width=14,height=12,type="eps",file="GermModel")
plot(plate,r/n,ylim=c(0,1))
phat<-exp(theta["mu"]+theta["beta1"]*extract+
          theta["beta2"]*seed+theta["beta3"]*exse)/
  (1+exp(theta["mu"]+theta["beta1"]*extract+
         theta["beta2"]*seed+theta["beta3"]*exse))
phat2<-exp(theta["mu"]+theta["beta1"]*extract+
           theta["beta2"]*seed+theta["beta3"]*exse+B)/
  (1+exp(theta["mu"]+theta["beta1"]*extract+
         theta["beta2"]*seed+theta["beta3"]*exse+B))
points(plate,phat,col="red",cex=2,pch=19)
points(plate,phat2,col="blue",pch=19)
for(i in 1:21){
  lines(c(i,i),qbinom(p=c(0.025,0.975),n[i],
                      prob=phat2[i])/n[i],col="blue")
}
points(plate,r/n,pch=19)
#dev.off()

##################################################
# Profile likelihood
##################################################

lThetaP<-function(theta,thetaP,B,lB,DlB,extract,seed,
                  exse,r,n,dobsll.B){
  # return Laplace approximation of negative log likelihood
  jnll<-0
  theta<-c(theta,thetaP)
  for(i in 1:length(B)){
    ll<-nlminb(0,lB,gradient=DlB,mu=theta["mu"],
               beta1=theta["beta1"],beta2=theta["beta2"],
               beta3=theta["beta3"],sig2=theta["sig2"],
               x1=extract[i],x2=seed[i],x3=exse[i],r=r[i],
               n=n[i],dobsll.B=dobsll.B)
    jnll<-jnll+ll$objective
    B[i]<-ll$par
  }
  jnll<-jnll+0.5*sum(log(attr(Hobsll.B(mu=theta["mu"],
               beta1=theta["beta1"],beta2=theta["beta2"],
               beta3=theta["beta3"],sig2=theta["sig2"],
               B=B,x1=extract,x2=seed,x3=exse,r=r,n=n),
                              "grad")))
  return(jnll)
}

# mu
thetaP<-c()
theta<-OP$par
mu.vec<-sort(c(Par["mu",1],
               seq(Par["mu",1]-4*Par["mu",2],
                   Par["mu",1]+4*Par["mu",2],length=9)))
theta<-theta[-1]
PL.mu<-numeric(length(mu.vec))
for(i in 1:length(mu.vec)){
  thetaP["mu"]<-mu.vec[i]
  PL.mu[i]<-nlminb(theta,lThetaP,thetaP=thetaP,B=B,
                   lB=lB,DlB=DlB,extract=extract,seed=seed,
                   exse=exse,r=r,n=n,
                   dobsll.B=dobsll.B)$objective
  print(i)
}

S.mu<-spline(mu.vec,exp(-PL.mu+OP$objective),n=100)
plot(mu.vec,exp(-PL.mu+OP$objective))
lines(S.mu$x,S.mu$y)
lines(range(S.mu$x),c(1,1)*0.1465)
lines(range(S.mu$x),c(1,1)*0.03625)

# beta 1
thetaP<-c()
theta<-OP$par
#beta1.vec<-theta["beta1"]+seq(-1,1,by=0.25)
beta1.vec<-sort(c(Par["beta1",1],
               seq(Par["beta1",1]-4*Par["beta1",2],
                   Par["beta1",1]+
                   4*Par["beta1",2],length=9)))
theta<-theta[-2]
PL.beta1<-numeric(length(beta1.vec))
for(i in 1:length(beta1.vec)){
  thetaP["beta1"]<-beta1.vec[i]
  PL.beta1[i]<-nlminb(theta,lThetaP,thetaP=thetaP,
                   B=B,lB=lB,DlB=DlB,extract=extract,
                   seed=seed,exse=exse,r=r,n=n,
                      dobsll.B=dobsll.B)$objective
  print(i)
}

S.beta1<-spline(beta1.vec,exp(-PL.beta1+OP$objective),
                n=100)
plot(beta1.vec,exp(-PL.beta1+OP$objective))
lines(S.beta1$x,S.beta1$y)
lines(range(S.beta1$x),c(1,1)*0.1465)
lines(range(S.beta1$x),c(1,1)*0.03625)

# beta 2
thetaP<-c()
theta<-OP$par
beta2.vec<-sort(c(Par["beta2",1],
               seq(Par["beta2",1]-4*Par["beta2",2],
                   Par["beta2",1]+4*Par["beta2",2],
                   length=9)))
theta<-theta[-3]
PL.beta2<-numeric(length(beta2.vec))
for(i in 1:length(beta2.vec)){
  thetaP["beta2"]<-beta2.vec[i]
  PL.beta2[i]<-nlminb(theta,lThetaP,thetaP=thetaP,B=B,
                      lB=lB,DlB=DlB,extract=extract,
                      seed=seed,exse=exse,r=r,n=n,
                   dobsll.B=dobsll.B)$objective
  print(i)
}

S.beta2<-spline(beta2.vec,exp(-PL.beta2+OP$objective),
                n=100)
plot(beta2.vec,exp(-PL.beta2+OP$objective))
lines(S.beta2$x,S.beta2$y)
lines(range(S.beta2$x),c(1,1)*0.1465)
lines(range(S.beta2$x),c(1,1)*0.03625)

# beta 3
thetaP<-c()
theta<-OP$par
#beta3.vec<-theta["beta3"]+seq(-2,2,by=0.5)
beta3.vec<-sort(c(Par["beta3",1],
               seq(Par["beta3",1]-4*Par["beta3",2],
                   Par["beta3",1]+4*Par["beta3",2],
                   length=9)))
theta<-theta[-4]
PL.beta3<-numeric(length(beta3.vec))
for(i in 1:length(beta3.vec)){
  thetaP["beta3"]<-beta3.vec[i]
  PL.beta3[i]<-nlminb(theta,lThetaP,thetaP=thetaP,B=B,
                      lB=lB,DlB=DlB,extract=extract,
                      seed=seed,exse=exse,r=r,n=n,
                      dobsll.B=dobsll.B)$objective
  print(i)
}

S.beta3<-spline(beta3.vec,exp(-PL.beta3+OP$objective),
                n=100)
plot(beta3.vec,exp(-PL.beta3+OP$objective))
lines(S.beta3$x,S.beta3$y)
lines(range(S.beta3$x),c(1,1)*0.1465)
lines(range(S.beta3$x),c(1,1)*0.03625)

# sig 2
thetaP<-c()
theta<-OP$par
#sig2.vec<-theta["sig2"]+seq(-8,2,by=0.5)
sig2.vec<-sort(c(Par["sig2",1],1E-6,1E-5,1E-4,1E-3,
               seq(1E-2,Par["sig2",1]+4*Par["sig2",2],
                   length=9)))
theta<-theta[-5]
PL.sig2<-numeric(length(sig2.vec))
for(i in 1:length(sig2.vec)){
  thetaP["sig2"]<-sig2.vec[i]
  PL.sig2[i]<-nlminb(theta,lThetaP,thetaP=thetaP,B=B,
                     lB=lB,DlB=DlB,extract=extract,
                     seed=seed,exse=exse,r=r,n=n,
                     dobsll.B=dobsll.B)$objective
  print(i)
}

S.sig2<-spline(sig2.vec,exp(-PL.sig2+OP$objective),n=100)
plot(sig2.vec,exp(-PL.sig2+OP$objective))
lines(S.sig2$x,S.sig2$y)
lines(range(S.sig2$x),c(1,1)*0.1465)
lines(range(S.sig2$x),c(1,1)*0.03625)

#graph(width=14,height=11,type="eps",file="ProfileFixed")
par(mfrow=c(2,2),mar=c(3,1,0,0),oma=c(0,2,1,1))
plot(mu.vec,exp(-PL.mu+OP$objective),axes=FALSE)
lines(S.mu$x,S.mu$y)
lines(range(S.mu$x),c(1,1)*0.1465)
lines(range(S.mu$x),c(1,1)*0.03625)
lines((Par["mu",1]-2*Par["mu",2])*c(1,1),c(-1,2),
      col="blue")
lines((Par["mu",1]+2*Par["mu",2])*c(1,1),c(-1,2),
      col="blue")
axis(1);axis(2);box()
mtext(expression(mu),side=1,line=2)
mtext("Relative profile likelihood",side=2,line=2)

plot(beta1.vec,exp(-PL.beta1+OP$objective),axes=FALSE)
lines(S.beta1$x,S.beta1$y)
lines(range(S.beta1$x),c(1,1)*0.1465)
lines(range(S.beta1$x),c(1,1)*0.03625)
lines((Par["beta1",1]-2*Par["beta1",2])*c(1,1),c(-1,2),
      col="blue")
lines((Par["beta1",1]+2*Par["beta1",2])*c(1,1),c(-1,2),
      col="blue")
mtext(expression(beta[1]),side=1,line=2)
axis(1);box()

plot(beta2.vec,exp(-PL.beta2+OP$objective),axes=FALSE)
lines(S.beta2$x,S.beta2$y)
lines(range(S.beta2$x),c(1,1)*0.1465)
lines(range(S.beta2$x),c(1,1)*0.03625)
lines((Par["beta2",1]-2*Par["beta2",2])*c(1,1),c(-1,2),
      col="blue")
lines((Par["beta2",1]+2*Par["beta2",2])*c(1,1),c(-1,2),
      col="blue")
mtext(expression(beta[2]),side=1,line=2)
axis(1);box();axis(2)
mtext("Relative profile likelihood",side=2,line=2)

plot(beta3.vec,exp(-PL.beta3+OP$objective),axes=FALSE)
lines(S.beta3$x,S.beta3$y)
lines(range(S.beta3$x),c(1,1)*0.1465)
lines(range(S.beta3$x),c(1,1)*0.03625)
lines((Par["beta3",1]-2*Par["beta3",2])*c(1,1),c(-1,2),
      col="blue")
lines((Par["beta3",1]+2*Par["beta3",2])*c(1,1),c(-1,2),
      col="blue")
mtext(expression(beta[3]),side=1,line=2)
axis(1);box()
#dev.off()

#graph(width=14,height=11,type="eps",file="ProfileRandom")
par(mfrow=c(1,1),mar=c(3,1,0,0),oma=c(0,2,1,1))
plot(sig2.vec,exp(-PL.sig2+OP$objective),axes=FALSE,
     xlab="",ylab="")
lines(S.sig2$x,S.sig2$y)
lines(range(S.sig2$x),c(1,1)*0.1465)
lines(range(S.sig2$x),c(1,1)*0.03625)
lines((Par["sig2",1]-2*Par["sig2",2])*c(1,1),c(-1,2),
      col="blue")
lines((Par["sig2",1]+2*Par["sig2",2])*c(1,1),c(-1,2),
      col="blue")
mtext(expression(sigma),side=1,line=2)
axis(1);box();axis(2)
mtext("Relative profile likelihood",side=2,line=2)
#dev.off()

