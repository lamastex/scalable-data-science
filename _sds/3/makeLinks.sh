#!/bin/bash
## will produce `nav.txt` ans `site.txt` to help with x.md and ../_data/navigation.yml 
## just do `./makeLinks.sh x/db/000_2-sds-3-x-ml` for links in 000_2-sds-3-x-ml, for instance.
rm -f nav.md
rm -f site.md
for fileName in $1/*.md
do
  fn0=${fileName/.md/}
  fn1=${fn0/x\//}
  fn2=$(echo $fn1 | cut -d'/' -f3)
  echo "$fileName"
  echo "$fn1" 
  echo "$fn2"
echo '      - title: ""' >> nav.md
echo "        url: /sds/3/x/${fn1}/" >> nav.md
echo "        * [${fn2}](${fn1}/)" >> site.md
done

