#
# Simulation til lect05 (lect04 ved Anders)

?drop1

?hatvalues

set.seed(123456)
 alt<-round(runif(100,0,4000))
 tmt<-as.factor(rep(c(0,1), each=50))
 sex<-as.factor(rep(c('M','F','M','F'), each=25))
 X<-model.matrix(~-1+sex+tmt:alt)
 y<-X%*%c(2.3,5,1/1000,3/1000)+rnorm(100,sd=.7)

fit0 <- lm(y~sex*tmt*alt)
drop1(fit0, test='F')

summary(fit0)
