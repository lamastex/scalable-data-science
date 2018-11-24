# Broken stuff dbc -> zeppelin

**Note**: Dan Lilja tested this on spark 2.2.0 to be as close as possible to the
original databricks environment in zeppelin.

- displayHTML()
  - This can be fixed by either dbcflatten or dbcadder with appropriate function.
- Getting weird errors with sparkr. Probably just a problem on my end.
- display(x: dataframe)
  - This can also be fixed by dbcadder.
- dbutils
- Anything living in dbfs.
- Errors in 008 due to sqlContext.read.load(path) giving an "illegal start of
  definition" error on the .load part. Guessing it's because of the filepath
  used is in dbfs. The entirety of 008 is basically broken because of this and
  the many uses of display().
- The %python from databricks should be converted to %pyspark to work correctly,
  otherwise it will try to use normal python and hence fail on all spark related
  things. This is probably a thing that can be fixed in pinot if databricks
  always uses pyspark when running a command with %py and not just normal
  python.
