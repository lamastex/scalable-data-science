#!/bin/bash

#This will make a gitbook by generating markdown from the .dbc archive

# cd to pinot directory
PINOT_DIR=~/all/git/pinot
sds_DIR=~/all/git/scalable-data-science

cd $PINOT_DIR &&

stack exec dbcflatten $sds_DIR/dbcArchives/2017/sds-2-2.dbc ~/all/git/scalable-data-science/gb/sds-2-2-flat.dbc &&

stack exec -- pinot -f databricks -t markdown $sds_DIR/gb/sds-2-2-flat.dbc -o $sds_DIR/gb/ &&

rm $sds_DIR/gb/sds-2-2-flat.dbc

#stack exec -- pinot -f databricks -t markdown $sds_DIR/dbcArchives/2017/sds-2-2.dbc -o $sds_DIR/gb/ &&

#rm -rf $sds_DIR/gb/db

#mv ~/all/git/scalable-data-science/gb/sds-2-2/ ~/all/git/scalable-data-science/gb/db 

