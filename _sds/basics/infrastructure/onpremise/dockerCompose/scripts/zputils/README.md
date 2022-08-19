# Zeppelin REST API utility scripts

### Run any script with the -h flag for more usage instructions

## injectHelperFunctions.py

    This injects a couple helper functions so that "display", and "displayHTML" works.
```
python3 injectHelperFunctions.py
```

## checkErrors.py

    This runs through the given notebooks and reports on paragraphs that don't execute
    Notebooks are unable to execute past a paragraph that gives an error. 
```
python3 python3 checkErrors.py

```
```
python3 checkErrors.py --dir 000_1-sds-3-x-spark

```
```
python3 checkErrors.py --dir 000_1-sds-3-x-spark/004_RDDsTransformationsActions

```


## runMarkdownParagraphs.py

    This finds and executes all markdown paragraphs, then it hides the editor of those paragraphs.
    It's use is to help package the notebooks with a more finished appearance.

```
python3 runMarkdownParagraphs.py

```
```
python3 runMarkdownParagraphs.py --dir 000_1-sds-3-x-spark/

```


## zputils.py

    A module containing helper functions for the Zeppelin 0.10.0 REST API. 