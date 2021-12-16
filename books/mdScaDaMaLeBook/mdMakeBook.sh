#!/bin/bash
# make ScaDaMaLe mdBook in progress

#set -x
DBCarchives=~/all/git/lamastex/scalable-data-science/dbcArchives/2021-new
#DBCarchives=~/all/git/lamastex/scalable-data-science/dbcArchives/2021
PINOTdir=~/all/git/tilowiklund/pinot
MDBOOKdir=~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook
##convert -font Helvetica -pointsize 16 -draw "text 110,65 'V. $(date +%Y/%m/%d/%H:%M:%S_%Z)'" logo.png logo_withText.png && 
##convert -font Courier -pointsize 14 -draw "text 2,12 'IN PROGRESS. Copyright: CC0 1.0 Universal'" logo_withText.png logoWithVersionAndCC.png &&
###convert -font Courier -pointsize 14 -draw "text 100,12 'Copyright: CC0 1.0 Universal'" logo_withText.png logoWithVersionAndCC.png &&
##cp logoWithVersionAndCC.png ScaDaMaLe/logo.png &&
##rm logo_withText.png logoWithVersionAndCC.png &&
#rm -r $MDBOOKdir/*-sds-*/src/contents/* && #first clean the md files to avoid pre-pumped files
for module in 000_1-sds-3-x #000_2-sds-3-x-ml #000_3-sds-3-x-st 000_4-sds-3-x-ss 000_5-sds-2-x-geo 000_6-sds-3-x-dl 000_7-sds-3-x-ddl 000_8-sds-3-x-pri xtraResources 000_9-sds-3-x-trends
do
  pushd $PINOTdir &&
  echo $module
  rm -r $MDBOOKdir/$module/src/contents/* && #first clean the md files to avoid pre-pumped files
  stack exec pinot -- --from databricks --to mdbook $DBCarchives/$module.dbc -o $MDBOOKdir/$module/src/contents &&
  popd &&
  pwd &&
  mkdir -p $module/src && cat src/bigSUMMARY.md | grep "${module}"  > $module/src/SUMMARY.md && cp scroll-mdbook-outputs.css $module/ &&
  pushd $MDBOOKdir/$module && mdbook build && popd && pwd &&
  echo done $module
done
