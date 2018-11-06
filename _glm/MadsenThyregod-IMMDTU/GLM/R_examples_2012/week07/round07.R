

##################################################
# Data
##################################################

V<-c(1065,1071,1075,1083,1089,1094,1100,1107,1111,
     1120,1128,1135)
BD<-c(2,3,5,11,10,21,29,48,56,88,98,99)
tr<-rep(100,12)
data=list(V=V,BD=BD,tr=tr)
plot(BD/tr~V,data=data)
##################################################

##################################################
# Slide 3 Canonical link
plot(log((BD/tr)/(1-BD/tr))~V,data=data)


##################################################
# Slide 11 Specification in R
model<-cbind(BD,tr-BD)~V

logis.glm<-glm(model,family=binomial,
               data=data)

lines(data$V,predict(logis.glm,type="link"))
##################################################


##################################################
# Slide 15 Observed information

mu<-predict(logis.glm,type="response")

j<-data$tr*(1/(mu*(1-mu))-(data$BD/data$tr-mu)*2*
                     mu/(mu*(1-mu))^2)
i<-data$tr/(mu*(1-mu))

plot(mu,j,type="l",ylab="information",xlab=expression(mu))
lines(mu,i,col="red")

##################################################
# Slide 16 Estimation
X<-cbind(1,V)
X.l<-X*mu*(1-mu)

t(X.l)%*%diag(i)%*%(BD/tr-mu)


##################################################
# Slide 18 Parameter estimates

beta<-coef(logis.glm)
W.beta<-diag(tr*mu*(1-mu))
V.beta<-solve(t(X)%*%W.beta%*%X)
sqrt(diag(V.beta))
summary(logis.glm)

eta<-X%*%coef(logis.glm)
h11<--sum(tr*exp(eta)/(1+exp(eta))^2)
h12<--sum(tr*exp(eta)/(1+exp(eta))^2*V)
h22<--sum(tr*exp(eta)/(1+exp(eta))^2*V^2)

H<-matrix(c(h11,h12,h12,h22),ncol=2)

-solve(H)
summary(logis.glm)$cov.unscaled

##################################################

##################################################
# Slide 19 Linear predictors
V.eta<-X%*%V.beta%*%t(X)
sd.eta<-sqrt(diag(V.eta))

matplot(V,cbind(eta,eta-2*sd.eta,eta+2*sd.eta),
        lty=c(1,2,2),col=c(1,2,2),type="l")

pred.link<-predict(logis.glm,type="link",se=TRUE)
matplot(V,cbind(pred.link$fit,pred.link$fit+2*pred.link$se.fit,
                pred.link$fit-2*pred.link$se.fit),
        lty=c(1,2,2),col=c(1,2,2),type="l")
##################################################

##################################################
# Slide 20 Linear fitted values
mu<-as.vector(exp(eta)/(1+exp(eta)))
V.mu<-diag((mu*(1-mu))^2)%*%X%*%V.beta%*%t(X)
sd.mu<-sqrt(diag(V.mu))

matplot(V,cbind(mu,mu-2*sd.mu,mu+2*sd.mu),
        lty=c(1,2,2),col=c(1,2,2),type="l")

pred.response<-predict(logis.glm,type="response",se=TRUE)
matplot(V,cbind(pred.response$fit,
                pred.response$fit+2*pred.response$se.fit,
                pred.response$fit-2*pred.response$se.fit),
        lty=c(1,2,2),col=c(1,2,2),type="l",ylab="p(V)")
points(V,BD/tr)
# but be carefull (see exercise of today) !!!
##################################################

##################################################
# Slide 21 Deviance
summary(logis.glm)
y<-BD/tr
d<-2*(y*log(y*(1-mu)/((1-y)*mu))+log((1-y)/(1-mu)))
D<-sum(tr*d)


##################################################
# Slide 22-23 Residuals 
rD<-sign(y-mu)*sqrt(tr*d)
rP<-(y-mu)/sqrt(mu*(1-mu)/tr)

matplot(V,cbind(rD,rP))

matplot(V,cbind(residuals(logis.glm,type="deviance"),
                residuals(logis.glm,type="pearson")))

# raw residuals
matplot(y-mu,ylim=max(abs(y-mu))*c(-1,1))
######################################

##################################################
# Slide 28 Sufficiency 

D<-21.018
1-pchisq(D,df=10)

# cloglog
plot(V,log(BD/tr/(1-BD/tr)),col=2)
points(V,log(-log(1-BD/tr)))

cloglog.glm<-glm(model,data=data,
                 family=binomial(cloglog))
summary(cloglog.glm)

#local design
eta<-as.vector(X%*%coef(cloglog.glm))
mu<-1-exp(-exp(eta))
X.beta<-diag(exp(eta-exp(eta)))%*%X

# "normal eq"
t(X.beta)%*%diag(tr/(mu*(1-mu)))%*%(BD/tr-mu)



summary(cloglog.glm)
D.loglog<-sum(residuals(cloglog.glm)^2)
1-pchisq(D.loglog,df=10)

matplot(V,cbind(residuals(logis.glm),residuals(cloglog.glm)))

anova(cloglog.glm)


# Data domain
pred<-predict(cloglog.glm,newdata=data.frame(V=c(1060:1140)),
              type="response",se=TRUE)
matplot(1060:1140,pred$fit+cbind(0,2*pred$se.fit,-2*pred$se.fit),
        type="l",lty=c(1,2,2),col=c(1,2,2),ylab="p(V)",xlab="V")
points(V,BD/tr)


# Linear domain
pred<-predict(cloglog.glm,newdata=data.frame(V=c(1060:1140)),
              type="link",se=TRUE)
matplot(1060:1140,pred$fit+cbind(0,2*pred$se.fit,-2*pred$se.fit),
        type="l",lty=c(1,2,2),col=c(1,2,2),ylab="p(V)",xlab="V")
points(V,log(-log(1-BD/tr)))

