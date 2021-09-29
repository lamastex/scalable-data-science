#!/bin/bash
## to generate md files in a dir do `./makeMds.sh 000_2-sds-3-x-ml`
for fileName in $1/*.md
do
  fn=${fileName/.md/}
  echo "$fileName"
  echo "$fn"
  cat xxxxMD.template > "$fileName"
  sed -i "s@xxxx@${fn}@g" "$fileName" 
done
