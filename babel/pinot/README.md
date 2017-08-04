I have (very basic!) `databricks -> Zeppelin` conversion working. Both
formats are, as far as I can tell, more or less undocumented, so I had
to manually reverse engineer them. I've only tried them on a few
examples and it only does *very basic* conversion and currently only
*from* databricks (not *to* databricks). The problem with converting to
databricks is that it uses some sort of custom JAR-archive format.

The source is available here

[https://github.com/TiloWiklund/pinot](https://github.com/TiloWiklund/pinot)

if you install stack (https://haskellstack.org/) you should be able to
simply (in the source code folder) run:

```
stack setup
stack build
stack exec pinot -- -f databricks -t zeppelin < input_notebook.json > output_notebook.json
```

where you get the `input_notebook.json` file(s) by running
```
jar -xf your_databricks_notebook.dbc
```
where `your_databrucks_notebook.dbc` is what you get by running 
`"export -> DBC Archive"` in databricks.

If you try it out I'd be interested in knowing where and how it breaks
down. In case you can't get it to compile/run but want to try it out
feel free to ask me for a binary.


I now have basic DBC-archive support and markdown support in pinot (I've also copied and pushed the new source to the scalable-data-science repo).

I had to change the command line interface a bit, since I need to write multiple files (each directory contains many files):
```
stack exec pinot -- -f databricks -t markdown databricks_archive_file.dbc -o output_directory
```
