#!/bin/bash
# make ScaDaMaLe mdBook in progress
# USAGE: pinotMdBook.sh module_name
set -x -v
pushd $PINOTdir
#rm -r $MDBOOKdir/$1/src/contents/* #first clean the md files to avoid pre-pumped files
#ls -al /root/temp

stack exec pinot -- --from databricks --to mdbook $MDBOOKdir/zipped/$1.dbc -o $MDBOOKdir/mdbooks/000_0-sds-3-x-projects-2022/src/contents/$1

echo "in new mdbook dir"
ls -al $MDBOOKdir
ls $MDBOOKdir/mdbooks
echo "done with pinot'ing into mdbook for module ${1}"