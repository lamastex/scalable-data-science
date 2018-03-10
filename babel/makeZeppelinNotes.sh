#!/bin/bash

# This will make zeppelin notes as .json files from the .dbc archive 
## via [pinot](https://github.com/TiloWiklund/pinot)

# cd to pinot directory
PINOT_DIR=~/all/git/pinot
sds_DIR=~/all/git/scalable-data-science

cd $PINOT_DIR &&

## for file by file conversion
#stack exec pinot -- --from databricks-json --to zeppelin $sds_DIR/dbcArchives/2017/sds-2-2/003_scalaCrashCourse.scala -o $sds_DIR/dbcArchives/2017/zp-sds-2-2/

## for the whole dbc archive - we temporarily write in /tmp/zp while debugging
### /tmp/zp should be replaced by $sds_DIR/zp/2/2/ and zeppelin set to git the notebooks for sds-2-2 collaborators
#stack exec pinot -- --from databricks --to zeppelin $sds_DIR/dbcArchives/2017/sds-2-2.dbc -o /tmp/zp/ # $sds_DIR/zp/2/2/
stack exec pinot -- --from databricks --to zeppelin $sds_DIR/dbcArchives/2017/sds-2-2.dbc -o $sds_DIR/zp/2/2/

#stack exec -- pinot -f databricks -t markdown $sds_DIR/dbcArchives/2017/sds-2-2.dbc -o $sds_DIR/gb/ &&

#rm -rf $sds_DIR/gb/db

#mv ~/all/git/scalable-data-science/gb/sds-2-2/ ~/all/git/scalable-data-science/gb/db 

