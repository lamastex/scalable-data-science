#!/bin/bash
# make ScaDaMaLe mdBook in progress

set -x
DBCarchives=~/all/git/lamastex/scalable-data-science/dbcArchives/2021
PINOTdir=~/all/git/tilowiklund/pinot
MDBOOKdir=~/all/git/lamastex/scalable-data-science/books/mdScaDaMaLeBook
##convert -font Helvetica -pointsize 16 -draw "text 110,65 'V. $(date +%Y/%m/%d/%H:%M:%S_%Z)'" logo.png logo_withText.png && 
##convert -font Courier -pointsize 14 -draw "text 2,12 'IN PROGRESS. Copyright: CC0 1.0 Universal'" logo_withText.png logoWithVersionAndCC.png &&
###convert -font Courier -pointsize 14 -draw "text 100,12 'Copyright: CC0 1.0 Universal'" logo_withText.png logoWithVersionAndCC.png &&
##cp logoWithVersionAndCC.png ScaDaMaLe/logo.png &&
##rm logo_withText.png logoWithVersionAndCC.png &&
pushd $PINOTdir &&
rm -r $MDBOOKdir/*-sds-*/src/contents/* && #first clean the md files to avoid pre-pumped files
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_1-sds-3-x.dbc -o $MDBOOKdir/000_1-sds-3-x/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_2-sds-3-x-ml.dbc -o $MDBOOKdir/000_2-sds-3-x-ml/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_3-sds-3-x-st.dbc -o $MDBOOKdir/000_3-sds-3-x-st/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_4-sds-3-x-ss.dbc -o $MDBOOKdir/000_4-sds-3-x-ss/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_5-sds-2-x-geo.dbc -o $MDBOOKdir/000_5-sds-2-x-geo/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_6-sds-3-x-dl.dbc -o $MDBOOKdir/000_6-sds-3-x-dl/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_7-sds-3-x-ddl.dbc -o $MDBOOKdir/000_7-sds-3-x-ddl/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_8-sds-3-x-pri.dbc -o $MDBOOKdir/000_8-sds-3-x-pri/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/xtraResources.dbc -o $MDBOOKdir/xtraResources/src/contents &&
stack exec pinot -- --from databricks --to mdbook $DBCarchives/000_9-sds-3-x-trends.dbc -o $MDBOOKdir/000_9-sds-3-x-trends/src/contents &&
##################################JupyterBookLessons-begin#################################################
###stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_0-sds-3-x-projects.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ &&
### to field unreachable files
##stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_0-sds-3-x-projects.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ -R ~/all/git/lamastex/scalable-data-science/books/2021/extra-resources-student-projects &&
##stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_7-sds-3-x-ddl.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ -R ~/all/git/lamastex/scalable-data-science/books/2021/extra-resources-student-projects &&
##stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_9-sds-3-x-trends.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ -R ~/all/git/lamastex/scalable-data-science/books/2021/extra-resources-student-projects &&
##### instructions-ignoring-student-bit-blow-up-fieldings in JupyterBook
##cat ScaDaMaLe_ALL_toc.yml | grep student-project | grep -v "sds-2-x-dl" | grep -v "development" > ScaDaMaLe_student-project_toc.yml &&
##################################JupyterBookLessons-end###################################################
popd &&
pwd
# make bigSUMMARY.md file - do it once when all content is fixed
#cd src
#find contents -iname '*.md' -type f | sort -h | while read f; do echo "- ["$(basename $f .md)"](./$f)"; done > bigSUMMARY.md
#cd ../
# make several SUMMARY.md files to allow javascript search index to be under 100MB under github.io constraints - one book for one .dbc archive
mkdir -p 000_1-sds-3-x/src && cat src/bigSUMMARY.md | grep 000_1-sds-3-x  > 000_1-sds-3-x/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_1-sds-3-x/ &&
mkdir -p 000_2-sds-3-x-ml/src && cat src/bigSUMMARY.md | grep 000_2-sds-3-x  > 000_2-sds-3-x-ml/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_2-sds-3-x-ml/ &&
mkdir -p 000_3-sds-3-x-st/src && cat src/bigSUMMARY.md | grep 000_3-sds-3-x  > 000_3-sds-3-x-st/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_3-sds-3-x-st/ &&
mkdir -p 000_4-sds-3-x-ss/src && cat src/bigSUMMARY.md | grep 000_4-sds-3-x  > 000_4-sds-3-x-ss/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_4-sds-3-x-ss/ && 
mkdir -p 000_5-sds-2-x-geo/src && cat src/bigSUMMARY.md | grep 000_5-sds-3-x  > 000_5-sds-2-x-geo/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_5-sds-2-x-geo/ &&
mkdir -p 000_6-sds-3-x-dl/src && cat src/bigSUMMARY.md | grep 000_6-sds-3-x  > 000_6-sds-3-x-dl/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_6-sds-3-x-dl/ &&
mkdir -p 000_7-sds-3-x-ddl/src && cat src/bigSUMMARY.md | grep 000_7-sds-3-x  > 000_7-sds-3-x-ddl/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_7-sds-3-x-ddl/ &&
mkdir -p 000_8-sds-3-x-pri/src && cat src/bigSUMMARY.md | grep 000_8-sds-3-x  > 000_8-sds-3-x-pri/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_8-sds-3-x-pri/ &&
mkdir -p xtraResources/src && cat src/bigSUMMARY.md | grep xtraResources > xtraResources/src/SUMMARY.md && cp scroll-mdbook-outputs.css xtraResources/ &&
mkdir -p 000_9-sds-3-x-trends/src && cat src/bigSUMMARY.md | grep 000_9-sds-3-x  > 000_9-sds-3-x-trends/src/SUMMARY.md && cp scroll-mdbook-outputs.css 000_9-sds-3-x-trends/ &&
pwd &&
pushd $MDBOOKdir/000_1-sds-3-x && mdbook build && popd && pwd &&
pushd $MDBOOKdir/000_2-sds-3-x-ml && mdbook build && popd && pwd &&
pushd $MDBOOKdir/000_3-sds-3-x-st && mdbook build && popd && pwd &&
pushd $MDBOOKdir/000_4-sds-3-x-ss && mdbook build && popd && pwd &&
pushd $MDBOOKdir/000_5-sds-2-x-geo && mdbook build && popd && pwd &&
pushd $MDBOOKdir/000_6-sds-3-x-dl && mdbook build && popd && pwd &&
pushd $MDBOOKdir/000_7-sds-3-x-ddl && mdbook build && popd && pwd &&
pushd $MDBOOKdir/000_8-sds-3-x-pri && mdbook build && popd && pwd &&
pushd $MDBOOKdir/xtraResources && mdbook build && popd && pwd &&
pushd $MDBOOKdir/000_9-sds-3-x-trends && mdbook build && popd && pwd &&
#./pushBook
echo done
