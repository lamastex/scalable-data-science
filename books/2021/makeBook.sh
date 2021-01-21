#!/bin/bash
# make ScaDaMaLe Book
rm -r ScaDaMaLe/ &&
jupyter-book create ScaDaMaLe &&
cp _config.yml ScaDaMaLe/_config.yml &&
convert -font Helvetica -pointsize 16 -draw "text 110,65 'V. $(date +%Y/%m/%d/%H:%M:%S_%Z)'" logo.png logo_withText.png && 
convert -font Courier -pointsize 14 -draw "text 2,12 'IN PROGRESS. Copyright: CC0 1.0 Universal'" logo_withText.png logoWithVersionAndCC.png &&
#convert -font Courier -pointsize 14 -draw "text 100,12 'Copyright: CC0 1.0 Universal'" logo_withText.png logoWithVersionAndCC.png &&
cp logoWithVersionAndCC.png ScaDaMaLe/logo.png &&
rm logo_withText.png logoWithVersionAndCC.png &&
pushd ~/all/git/tilowiklund/pinot &&
# to ignore temporarily the .dbc files as they change do
# git update-index --assume-unchanged ../../dbcArchives/2021/000_1-sds-3-x.dbc
# to undo the above do:
# git update-index --no-assume-unchanged ../../dbcArchives/2021/000_1-sds-3-x.dbc
stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_1-sds-3-x.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ &&
#stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_2-sds-3-x-ml.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ &&
##stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_0-sds-3-x-projects.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ &&
## to field unreachable files
stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_0-sds-3-x-projects.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ -R ~/all/git/lamastex/scalable-data-science/books/2021/extra-resources-student-projects &&
stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_7-sds-3-x-ddl.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ -R ~/all/git/lamastex/scalable-data-science/books/2021/extra-resources-student-projects &&
stack exec pinot -- --from databricks --to jupyter-book ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_9-sds-3-x-trends.dbc -o ~/all/git/lamastex/scalable-data-science/books/2021/ScaDaMaLe/ -R ~/all/git/lamastex/scalable-data-science/books/2021/extra-resources-student-projects &&
popd &&
# find ScaDaMaLe -iname '*.ipynb' | sort -h | cut -d'/' -f 2- | xargs printf "- file: %s\n" > ScaDaMaLe/_toc.yml
find ScaDaMaLe -iname '*.ipynb' | sort -h | cut -d'/' -f 2- | xargs printf "- file: %s\n" > ScaDaMaLe_all_toc.yml &&
find ScaDaMaLe -iname '*.ipynb' | sort -h | cut -d'/' -f 2- | xargs printf "- file: %s\n" > ScaDaMaLe_ALL_toc.yml &&
cat ScaDaMaLe_ALL_toc.yml | grep student-project | grep -v "sds-2-x-dl" | grep -v "development" > ScaDaMaLe_student-project_toc.yml &&
sed -i -e 's/^/  /' ScaDaMaLe_student-project_toc.yml && # prefix each line with two white-spaces to conform to yml

pwd &&
cp _toc.yml ScaDaMaLe/_toc.yml &&
jupyter-book build ScaDaMaLe #&&
#./pushBook
