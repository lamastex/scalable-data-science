build:
	@docker build -t sds-sagemath:latest -f Dockerfile .

# these need to be modified
run:
	@docker run \
	  -it --rm \
	  -v `pwd`:`pwd` -w `pwd` -p 8888:8888 \
	  --name sds-sagemath sds-sagemath:latest 

runSageMathBash:
	@docker run \
	  -it --rm \
	  -v `pwd`:`pwd` -w `pwd` \
	  --name SageMathBash sds-sagemath:latest bash 

clean:
	@docker rm sds-sagemath > /dev/null || true

stop:
	@docker stop sds-sagemath

start:
	@docker start sds-sagemath
