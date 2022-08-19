#! /bin/bash

set -x -v

pushd $localgitdockerComposeDIRPATH

rm -rf data
databricks fs cp -r dbfs:/datasets/sds data/
