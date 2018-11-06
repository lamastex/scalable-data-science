
## Illustration of Profile Likelihood Confidence Intervals and
## comparison with Wald based CI.

## From example(glm) (needs MASS to be present on the system)
counts <- c(18,17,15,20,10,20,25,13,12)
outcome <- gl(3,1,9); treatment <- gl(3,3)
glm.D93 <- glm(counts ~ outcome + treatment, family=poisson())

glm.D93$coefficients

confint(glm.D93)  # based on profile likelihood
confint.default(glm.D93)  # based on asymptotic normality
