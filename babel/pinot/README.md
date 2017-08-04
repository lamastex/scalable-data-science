Software somehwat in the same vein as [pandoc](http://pandoc.org/), but for notebooks (Jupyter, Zeppelin, ...) rather than markup formats. Currently only has very rudimentary support for [Zeppelin](https://zeppelin.apache.org/) and [Databricks](https://community.cloud.databricks.com/) as well as outputting markdown.

Copy of the following repository:
[https://github.com/TiloWiklund/pinot](https://github.com/TiloWiklund/pinot)

If you install [Stack](https://haskellstack.org/) you should be able to simply (in the source code folder) run:
```
stack setup
stack build
```
to build the project.

Run `stack exec pinot` for basic usage information. The following example converts a databricks archive to a (collection of) Zeppelin notebook(s):
```
stack exec pinot -- --from databricks --to zeppelin databricks_notebook.dbc -o output_folder
```
where `databricks_notebook.dbc` is what you get by selecting `DBC Archive` in the `export` section of databricks.
