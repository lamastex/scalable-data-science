
count<-read.table("./data/cells.txt",header=T)
attach(count)
names(count)

table(cells)

tapply(cells,smoker,mean)
 
tapply(cells,weight,mean)
 
tapply(cells,sex,mean)

tapply(cells,age,mean)

model1<-glm(cells~smoker*sex*age*weight,poisson)
summary(model1)

model2<-glm(cells~smoker*sex*age*weight,quasipoisson)
summary(model2)

model3<-update(model2, ~. -smoker:sex:age:weight)
summary(model3)

modelfinal <- glm(cells~smoker+weight+smoker:weight,
                  family=quasipoisson)

summary(modelfinal)

tapply(cells,list(smoker,weight),mean)

barplot(tapply(cells,list(smoker,weight),mean),beside=T)
legend(1.2,3.4,c("non","smoker"),fill=c(2,7))

