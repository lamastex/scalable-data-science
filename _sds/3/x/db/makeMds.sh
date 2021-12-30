#!/bin/bash
set -v -x
## to generate md files in a dir with html files do `./makeMds.sh 000_2-sds-3-x-ml`
for fileName in $1/*.html #$(ls $1/*.html | cut -d'/' -f 9-10 | head -n 100)
do
  echo "$fileName"
  hn=${fileName/.html/}
  echo "$hn"
  #strippedHn=$(echo $hn | cut -d'/' -f 10-11)
  strippedHn=$(echo $hn | cut -d'/' -f 10-12) # for 000_0-sds-3-x-projects/*/
  echo "$strippedHn"
  mdFileName=${hn}.md
  echo "$mdFileName"
  touch $mdFileName
  #fn=${fileName/.md/}
  cat xxxxMD.template > "$mdFileName"
  sed -i "s@xxxx@${strippedHn}@g" "$mdFileName" 
done
