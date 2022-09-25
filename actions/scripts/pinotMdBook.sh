#!/bin/bash
# make ScaDaMaLe mdBook in progress
# USAGE: pinotMdBook.sh module_name
set -x -v
pushd $PINOTdir
#rm -r $MDBOOKdir/$1/src/contents/* #first clean the md files to avoid pre-pumped files

stack exec pinot -- --from databricks --to mdbook $MDBOOKdir/$1.dbc -o $MDBOOKdir/$1/src/contents

echo "done with pinot'ing into mdbook for module ${1}"