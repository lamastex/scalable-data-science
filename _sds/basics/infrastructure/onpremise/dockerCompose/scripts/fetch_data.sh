#! /bin/bash

set -x -v

pushd $localgitdockerComposeDIRPATH

databricks fs cp -r dbfs:/datasets/sds data/
