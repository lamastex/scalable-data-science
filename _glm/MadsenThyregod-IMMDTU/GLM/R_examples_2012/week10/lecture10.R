rats.tmp<-read.table("rats.csv",sep=";",header=T)
rats<-c()
for(i in 1:dim(rats.tmp)[1]){
  rats<-rbind(rats,cbind(rats.tmp[i,1],rats.tmp[i,2],
                         1:10,t(rats.tmp[i,3:12])))
}

rats<-data.frame(treatm=rats[ ,1],cage=rats[ ,2],month=rats[ ,3],
                 lnc=log(rats[ ,4]))
library(lattice)

head(rats,3)

 plot(rats$lnc~rats$month, type='n')
 by(rats,rats$cage,
    function(X){
      lines(X$month,X$lnc, col=X$treatm[1])
    }
 )->out
 legend("topright", bty='n', legend=1:3, lty='solid', col=1:3)

 rats$treatm<-factor(rats$treatm)
 rats$month<-factor(rats$month)
 doone<-function(X){
   anova(lm(lnc~treatm,data=X))["F value"][1,1]
 }
 results<-by(rats,rats$month,doone)



 fun<-function(x){
  log(sum(exp(x)))
 }
 ratsSum<-aggregate(lnc ~ cage+treatm, data = rats,fun)
 names(ratsSum)<-c('cage','treatm','lnTot')
 fit<-lm(lnTot~treatm,data=ratsSum)
 anova(fit)

##################################################
 library(nlme)
 fit.mm<-lme(lnc~month+treatm+month:treatm, random = ~1|cage, data=rats)
##################################################

##################################################
fit.mm
##################################################


##################################################
fit.cs<-gls(lnc~month+treatm+month:treatm,
             correlation=corCompSymm(form=~1|cage),
             data=rats)
##################################################

##################################################
 fit.cs<-gls(lnc~month+treatm+month:treatm,
             correlation=corCompSymm(form=~1|cage),
             data=rats, method="ML")
 logLik(fit.cs)
 fit.mm<-lme(lnc~month+treatm+month:treatm, 
             random = ~1|cage, 
             data=rats, method="ML")
 logLik(fit.mm)
##################################################

##################################################
 fit.gau <- lme(lnc~month+treatm+month:treatm,
                random=~1|cage,
                correlation=corGaus(form=~as.numeric(month)|cage,nugget=TRUE),
                data=rats)
##################################################


##################################################
fit.gau
##################################################

##################################################
 nu.sq<-0.1404056^2
 sigma.sq<-0.2171559^2*0.2186743
 tau.sq<-0.2171559^2-sigma.sq
 rho.sq<-2.3863954
 c(nu.sq=nu.sq, sigma.sq=sigma.sq, tau.sq=tau.sq, rho.sq=rho.sq)

##################################################

##################################################
 fit.id <- lm(lnc~month+treatm+month:treatm, data=rats) # WRONG independent model 
 fit.mm <- lme(lnc~month+treatm+month:treatm, 
              random = ~1|cage, 
               data=rats, method="ML")
 fit.gau<- lme(lnc~month+treatm+month:treatm,
               random=~1|cage,
               correlation=corGaus(form=~as.numeric(month)|cage,nugget=TRUE),
               data=rats, method="ML")
 anova(fit.gau,fit.mm,fit.id)
##################################################

##################################################
 fit.gau<- lme(lnc~month+treatm+month:treatm, random=~1|cage,
               correlation=corGaus(form=~as.numeric(month)|cage,nugget=TRUE),
               data=rats)
 fit.exp<- lme(lnc~month+treatm+month:treatm, random=~1|cage,
               correlation=corExp(form=~as.numeric(month)|cage,nugget=TRUE),
               data=rats)
#par(mfrow=c(1,2))
#graph(width=7,height=7,type="eps",file="gau")
 plot(Variogram(fit.gau), main='Gaussian')
#dev.off()

#graph(width=7,height=7,type="eps",file="exp")
 plot(Variogram(fit.exp), main='Exponential')
#dev.off()
##################################################



#plot(as.numeric(rats[ ,"month"]),fit.exp$fitted[ ,2],col="white")
plot(as.numeric(rats[ ,"month"]),rats[ ,"lnc"],col="white")
for(i in 1:30){
  lines(as.numeric(rats[rats$cage==i,"month"]),rats[rats$cage==i,"lnc"],
        col=mean(as.numeric(rats[rats$cage==i,"treatm"])),lty=2)
}
for(i in 1:30){
  lines(as.numeric(rats[rats$cage==i,"month"]),fit.exp$fitted[rats$cage==i,2],
        col=mean(as.numeric(rats[rats$cage==i,"treatm"])))
}
for(i in 1:30){
  lines(as.numeric(rats[rats$cage==i,"month"]),fit.exp$fitted[rats$cage==i,1],
        col=4,#mean(as.numeric(rats[rats$cage==i,"treatm"])),
        lwd=2)
}


plot(as.numeric(rats[ ,"treatm"]),fit.exp$fitted[ ,2]-fit.exp$fitted[ ,1])

plot(rats[ ,"treatm"],fit.exp$fitted[ ,2]-fit.exp$fitted[ ,1])


x<-seq(-3,3,by=0.01)
gaussCor<-function(x){
  exp(-x*x)
}
y<-0.1+gaussCor(x)

#source("~/tsmodels/graph.R")
#graph(width=14,height=7,type="eps",file="xspgau")
plot(x,y,type="l",ylim=c(0,1.2),axes=F,
     xlab=expression(paste("Distance ",t[i[2]],"-",t[i[1]])),ylab="Covariance")
lines(range(x)*2,c(0.1,0.1),lty=2)
lines(range(x)*2,max(y)*c(1,1),lty=2)
box()
axis(1,at=c(-0.83,0,0.83),labels=c(expression(paste(-0.83,rho)),0,expression(paste(0.83,rho))))
axis(2,at=c(0,0.1,0.1+gaussCor(-0.83),max(y)),
     labels=c(0,expression(nu^2),expression(nu^2+tau^2/2),expression(nu^2+tau^2)))
lines(-0.83*c(1,1),c(-1,0.1+gaussCor(-0.83)),lty=2)
lines(0.83*c(1,1),c(-1,0.1+gaussCor(-0.83)),lty=2)
lines(c(-4,0.83),0.1+gaussCor(-0.83)*c(1,1),lty=2)
#dev.off()
