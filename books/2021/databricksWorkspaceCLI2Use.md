If anyone is wondering how to hack the following mysterious command (to save yourself from finger/hand pains...)
 `databricks workspace export -f HTML`
so that you can download every `.html` inside a databricks workspace directory using the CLI, then you may find this useful. 

Here we are grabbing all the `html` exports of each notebook inside a workspace folder named `/scalable-data-science/000_1-sds-3-x/` into `tmp/html` directory after having already done 
`databricks workspace export_dir -o /scalable-data-science/000_1-sds-3-x tmp/`

```
rm -rf tmp/html && mkdir -p tmp/html && for filename in $(ls tmp/*.* | cut -d'/' -f2 | cut -d'.' -f1 | head -n 100); do echo $filename && databricks workspace export -f HTML -o /scalable-data-science/000_1-sds-3-x/${filename} tmp/html/; done && pushd tmp/html && for f in *; do mv -- "$f" "${f%.*}.html"; done && mv *.html ../ && popd

rmdir tmp/html/
mv tmp/* ../../_sds/3/x/db/000_1-sds-3-x/
```
