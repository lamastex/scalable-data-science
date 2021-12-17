#!/bin/bash 

set -x -v

echo ${dbProfile0}
echo ${localgitdbcDIRPATH}
echo ${localgitsiteDIRPATH}
echo "done echoing env variables in use inside docker"

#databricks --profile ${dbProfile0} workspace list /scalable-data-science && 
#databricks --profile ${dbProfile0} workspace list /scalable-data-science/000_1-sds-3-x &&
#mkdir -p ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp &&
#databricks --profile ${dbProfile0} workspace export_dir -o /scalable-data-science/000_1-sds-3-x ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp &&
#rm -rf ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp/html && mkdir -p ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp/html && for filename in $(ls ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp/*.* | cut -d'/' -f12 | cut -d'.' -f1 | head -n 100); do echo $filename && databricks --profile ${dbProfile0} workspace export -f HTML -o /scalable-data-science/000_1-sds-3-x/${filename} ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp/html/; done && pushd ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp/html && for f in *; do mv -- "$f" "${f%.*}.html"; done && mv *.html ../ && popd && rmdir ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp/html/ && mv ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp/* ${localgitsiteDIRPATH}/000_1-sds-3-x/ && rmdir ${localgitsiteDIRPATH}/000_1-sds-3-x/tmp/ &&
cd ${localgitsiteDIRPATH} && bash ${localgitsiteDIRPATH}/makeMds.sh ${localgitsiteDIRPATH}/000_1-sds-3-x 
echo "done for now"



