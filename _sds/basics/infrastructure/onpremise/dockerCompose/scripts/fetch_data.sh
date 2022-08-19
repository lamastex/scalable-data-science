#! /bin/bash

set -x -v

pushd $localgitdockerComposeDIRPATH

rm -rf datasets-sds
databricks fs cp -r dbfs:/datasets/sds datasets-sds
