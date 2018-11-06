
##  Data handling and sorting

worms<-read.table("./worms.csv",header=T,row.names=1)

attach(worms)
names(worms)

worms

summary(worms)

worms[,1:3]

worms[5:15,]

worms[Area>3 & Slope <3,]

worms[order(worms[,1]),1:6]  ## Sorting

worms[rev(order(worms[,4])),c(4,6)]  ## Sorting - descending order

rm(x,y,z)
detach(worms)


# multivariate regression

rm(ozone)

ozone.pollution<-read.table("./ozone.data.csv",header=T)
attach(ozone.pollution)
names(ozone.pollution)

pairs(ozone.pollution,panel=panel.smooth)

library(mgcv)
par(mfrow=c(2,2))
model<-gam(ozone~s(rad)+s(temp)+s(wind))
plot(model)
par(mfrow=c(1,1))

#  make sure that you have down-loaded the tree library from CRAN

# library(tree)
# model<-tree(ozone~.,data=ozone.pollution)
# plot(model)

# text(model)

model1<-lm(ozone~temp*wind*rad+I(rad^2)+I(temp^2)+I(wind^2))
summary(model1)

model2<-update(model1,~. -temp:wind:rad)
summary(model2)

model3<-update(model2,~. - wind:rad)
summary(model3)

model4<-update(model3,~. - temp:wind)
summary(model4)

model5<-update(model4,~. - I(rad^2))
summary(model5)

model6<-update(model5,~. - temp:rad)
summary(model6)

model7<-lm(log(ozone) ~ temp + wind + rad + I(temp^2) + I(wind^2))
summary(model7)

par(mfrow=c(2,2))

model8<-update(model7,~. - I(temp^2))
summary(model8)
plot(model8)


model9<-lm(log(ozone) ~ temp + wind + rad + I(wind^2),subset=(1:length(ozone)!=17))
summary(model9)
plot(model9)








