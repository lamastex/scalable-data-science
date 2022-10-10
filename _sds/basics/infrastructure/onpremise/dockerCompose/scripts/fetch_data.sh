#! /bin/bash

set -x -v

pushd $localgitdockerComposeDIRPATH

rm -rf datasets-sds
databricks fs cp -r dbfs:/datasets/sds datasets-sds

# zip -vr datasets-sds.zip datasets-sds/ -x "*.DS_Store"
# cp datasets-sds.zip ~/all/git/lamastex/sds-datasets/ && git push the .zip file
# wget https://github.com/lamastex/sds-datasets/raw/master/datasets-sds.zip in 002_02_dbcCEdataLoader notebook
