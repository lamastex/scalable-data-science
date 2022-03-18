#!/bin/bash
# make ScaDaMaLe mdBook with rust from pinot mdbook output

set -x -v
pushd $MDBOOKdir
  ################ to generate SUMMARY.md for mdbook
  ##cd src
  ##find contents -iname '*.md' -type f | sort -h | while read f; do echo "- ["$(basename $f .md)"](./$f)"; done > SUMMARY.md
  ## cp SUMMARY.md bigSUMMARY.md
  ##cd ..
  ###################################################

#mkdir -p $1/src && cat src/bigSUMMARY.md | grep "${1}"  > $1/src/SUMMARY.md && cp scroll-mdbook-outputs.css $1/ &&
mkdir -p $1/src
cat src/bigSUMMARY.md | grep "${1}"  > $1/src/SUMMARY.md
cp scroll-mdbook-outputs.css $1/
cp book.toml $1/
pushd $MDBOOKdir/$1 #<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< !!!
rm -rf book
mdbook build
popd
pwd
echo "done with making mdbook for module ${1}"
