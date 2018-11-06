######################################

library(nlme)
Rail

################################################3

dat <- read.table("Rail.txt",head=T)
names(dat)
dat$Rail<-factor(dat$Rail)
str(dat)
head(dat)

help(Rail)

dat2<-groupedData(travel ~ 1 | Rail,dat)

plot(dat2)


fit1<-lme(travel ~ 1, data = dat2, random = ~ 1|Rail)
summary(fit1)

# test of fixed part
anova(fit1)

# test for random effect
fit2<-lm(travel ~ 1,data = dat)
anova(fit1,fit2)

help(anova.lme)

fitted(fit1)
fitted(fit2)

ranef(fit1)
fixef(fit1)

summary(fit1)



plot(fit1)


##########################

# Stool

ergoStool

help(ergoStool)

dat <- read.table("ergoStool.txt",head=T)
names(dat)
dat$Type<-factor(dat$Type)
dat$Subject<-factor(dat$Subject)

dat2<-groupedData(effort ~ Type | Subject,data=dat)


plot.design(dat2)

names(dat)
fit1<-lme(effort~Type, data = dat2, random = ~1|Subject)
summary(fit1)

anova(fit1)
fit2<-lm(effort~Type, data = dat)
anova(fit1,fit2)

intervals(fit1)


plot(fit1)


plot(fit1,form=resid(.,type='p')~fitted(.)|Subject,abline=0)


plot(fit1,form=resid(.,type='p')~fitted(.)|Type,abline=0)



str(dat)

