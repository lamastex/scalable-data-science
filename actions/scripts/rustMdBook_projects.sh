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

pushd $MDBOOKdir/mdbooks/$1/src
  ################ to generate SUMMARY.md for mdbook

echo "we should find source and contents here"
pwd

echo "# Summary" > SUMMARY.md
echo "" >> SUMMARY.md
echo "[Introduction](./introduction.md)" >> SUMMARY.md
echo "" >> SUMMARY.md
echo "# Projects" >> SUMMARY.md
echo "" >> SUMMARY.md
for d in contents/*; do
#echo "# $(basename $d)">temp.md
#echo "">>temp.md
find $d -iname '*.md' -type f | sort -h | while read f; do echo "   - ["$(basename $f .md)"](./$f)"; done > temp.md
sed -i '1 s/^ *//' temp.md
sed -i '1 s/./&'"$(basename $d)"'_/3' temp.md
cat temp.md >> SUMMARY.md
echo "">>SUMMARY.md
echo "---">>SUMMARY.md
echo "" >> SUMMARY.md
done
#echo "---"
#find contents -iname '*.md' -type f | sort -h | while read f; do echo "- ["$(basename $f .md)"]($f)"; done > SUMMARY.md
#cp SUMMARY.md bigSUMMARY.md
###################################################

#mkdir -p $1/src && cat src/bigSUMMARY.md | grep "${1}"  > $1/src/SUMMARY.md && cp scroll-mdbook-outputs.css $1/ &&
#cat bigSUMMARY.md | grep "${1}"  > SUMMARY.md

#add editors
#echo "">>SUMMARY.md
#echo "---"
echo "# Editors" > editors.md
echo "Here is a list of the editors who have helped improve this book" >> editors.md
echo "- [Raazesh Sainudiin](https://www.linkedin.com/in/raazesh-sainudiin-45955845/)" >> editors.md
echo "- [Kristoffer Torp](https://github.com/kTorp)" >> editors.md
echo "- [Oskar Åsbrink](https://www.linkedin.com/in/oskar-åsbrink-847a76231/)" >> editors.md
echo "- [Tilo Wiklund](https://www.linkedin.com/in/tilo-wiklund-682aa496/)" >> editors.md
echo "- [Dan Lilja](https://www.linkedin.com/in/dan-lilja-a2ab8096/)" >> editors.md
echo "- [Editors](./editors.md)" >> SUMMARY.md
cat SUMMARY.md
cd ..
cp $MDBOOK_FILES_DIR/scroll-mdbook-outputs.css .
cp $MDBOOK_FILES_DIR/book.toml .
#pushd $MDBOOKdir/$1 #<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< !!!
rm -rf book
mdbook build
popd
pwd
echo "done with making mdbook for module ${1}"