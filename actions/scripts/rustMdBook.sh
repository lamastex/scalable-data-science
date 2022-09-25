#!/bin/bash
# make ScaDaMaLe mdBook with rust from pinot mdbook output
#echo "ls mannen man"
#ls
#set -x -v
#set -o allexport
#source env.list
#set +o allexport
cd /root/temp
ls -l
cd $1
ls -l
pushd $MDBOOKdir/$1/src
  ################ to generate SUMMARY.md for mdbook

echo "we should find source and contents here"
pwd
find contents -iname '*.md' -type f | sort -h | while read f; do echo "- ["$(basename $f .md)"](./$f)"; done > SUMMARY.md
cp SUMMARY.md bigSUMMARY.md
###################################################

#mkdir -p $1/src && cat src/bigSUMMARY.md | grep "${1}"  > $1/src/SUMMARY.md && cp scroll-mdbook-outputs.css $1/ &&
cat bigSUMMARY.md | grep "${1}"  > SUMMARY.md
cd ..
cp $MDBOOK_FILES_DIR/scroll-mdbook-outputs.css .
cp $MDBOOK_FILES_DIR/book.toml .
#pushd $MDBOOKdir/$1 #<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< !!!
rm -rf book
mdbook build
popd
pwd
echo "done with making mdbook for module ${1}"