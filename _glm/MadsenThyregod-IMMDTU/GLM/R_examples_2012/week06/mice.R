
# Code used in Lecture No. 6 - Motivating example with mices.
# (Note that the R code on the slides are code for direct input..)
#

mice <- data.frame(
 stillb=c(15, 17, 22, 38, 144),
 total=c(297, 242, 312, 299, 285),
 conc=c(0, 62.5, 125, 250, 500)
)

mice

mice$resp <- cbind(mice$stillb, mice$total-mice$stillb)

mice.glm <- glm(formula=resp ~ conc, family=binomial(link=logit),data=mice)
anova(mice.glm)
summary(mice.glm)

# Plot in the linear domain. Logit transformed observatoins and corresponding
# linear dose response assay

p <- mice$stillb/mice$total
logit <- log(p/(1-p))
plot(mice$conc, logit, las=1, ylim=c(-3.5,0.1), xlab='Conc', ylab="Logit(still born fraction)") 
abline(coef(mice.glm))

# Plot in the original scale - Observed fraction stillborn and
# corresponding fitted values under logistic regression for
# dose response assay

plot(mice$conc,p, las=1, ylim=c(0,.6), xlab='Concentration', ylab="Still born fraction")
xx<-seq(0,max(mice$conc))
yy<-coef(mice.glm)[1]+coef(mice.glm)[2]*xx
lines(xx,exp(yy)/(1+exp(yy)))

# The linear predictions plus standard error

mice.pred <- predict(mice.glm, type='link', se.fit=TRUE)

mice.pred$fit
mice.pred$se.fit
names(mice.pred)

# The fittet values plus standard error

mice.fit <- predict(mice.glm, type='response', se.fit=TRUE)
mice.fit$fit
mice.fit$se.fit
mice.fit$residual.scale

# The response residuals

residuals(mice.glm, type='response')

# The deviance residuals

residuals(mice.glm, type='deviance')

# The Pearson residuals

residuals(mice.glm, type='pearson')

