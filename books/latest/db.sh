#!/bin/bash 

set -x -v

localgitdbcDIR=testing
localgitPATH=/root/GIT
databricks --profile dbua-us-west workspace list /scalable-data-science 
databricks --profile dbua-us-west workspace list /scalable-data-science/000_1-sds-3-x 
mkdir -p ${localgitPATH}/lamastex/scalable-data-science/dbcArchives/${localgitdbcDIR}/000_1-sds-3-x

