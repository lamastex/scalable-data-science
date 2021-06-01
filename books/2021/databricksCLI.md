# databricks CLI to avoid GUIs

```
#pip3 install databricks-cli
## curl -X GET -H 'Authorization: Bearer <token>' https:/<shardID>.cloud.databricks.com/api/2.0/clusters/list
#databricks configure --token
#cat ~/.databrickscfg 
#databricks fs -h
#databricks workspace -h
#databricks workspace list
databricks workspace list /scalable-data-science
databricks workspace list /scalable-data-science/000_1-sds-3-x
mkdir -p ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_1-sds-3-x
databricks workspace export_dir -o /scalable-data-science/000_1-sds-3-x ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_1-sds-3-x/
mkdir ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_2-sds-3-x-ml
databricks workspace export_dir -o /scalable-data-science/000_2-sds-3-x-ml ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_2-sds-3-x-ml/
databricks workspace import_dir -o ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_1-sds-3-x /scalable-data-science/000_1-sds-3-x
mkdir ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_3-sds-3-x-st/
mkdir ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_4-sds-3-x-ss/
databricks workspace export_dir -o /scalable-data-science/000_3-sds-3-x-st ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_3-sds-3-x-st/
databricks workspace export_dir -o /scalable-data-science/000_4-sds-3-x-ss ~/all/git/lamastex/scalable-data-science/dbcArchives/2021/000_4-sds-3-x-ss/
```
