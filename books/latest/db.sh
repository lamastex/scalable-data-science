#!/bin/bash 

set -x -v

echo ${dbProfile0}
echo ${localgitdbcDIRPATH}
echo ${localgitsiteDIRPATH}
echo "done echoing env variables in use inside docker"

delimiterField=13 #12
sdsCourseModule=000_0-sds-3-x-projects/student-project-02_group-LiUUmeaSceneGraphMotifs
#
#done already .. 000_1-sds-3-x 000_2-sds-3-x-ml 000_3-sds-3-x-st

## this just lists the workspace/scalable-data-science
#databricks --profile ${dbProfile0} workspace list /scalable-data-science && 
## this just lists the workspace/scalable-data-science/${sdsCourseModule}
#databricks --profile ${dbProfile0} workspace list /scalable-data-science/${sdsCourseModule} &&
## make dir .../tmp to download sources and also their html versions with databricks CLI
mkdir -p ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp &&
databricks --profile ${dbProfile0} workspace export_dir -o /scalable-data-science/${sdsCourseModule} ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp &&
rm -rf ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp/html && mkdir -p ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp/html && for filename in $(ls ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp/*.* | cut -d'/' -f${delimiterField} | cut -d'.' -f1 | head -n 100); do echo $filename && databricks --profile ${dbProfile0} workspace export -f HTML -o /scalable-data-science/${sdsCourseModule}/${filename} ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp/html/; done && pushd ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp/html && for f in *; do mv -- "$f" "${f%.*}.html"; done && mv *.html ../ && popd && rmdir ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp/html/ && mv ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp/* ${localgitsiteDIRPATH}/${sdsCourseModule}/ && rmdir ${localgitsiteDIRPATH}/${sdsCourseModule}/tmp/ &&
# make the mark down for each html of each notebook so it can be embedded in Iframe
cd ${localgitsiteDIRPATH} && bash ${localgitsiteDIRPATH}/makeMds.sh ${localgitsiteDIRPATH}/${sdsCourseModule} 
echo "done for now with sites for ${sdsCourseModule}"


