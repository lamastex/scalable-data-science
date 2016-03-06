#!/bin/bash

bin="`dirname "$0"`"
ROOT_DIR="`cd "$bin/../"; pwd`"

# find out if user has python installed
export WHICH_PYTHON=$(which python)
if [ -z "$WHICH_PYTHON" ]; then
    echo "[ERROR] Python is not found. Cannot run script without Python"
    exit 1
fi

eval "$WHICH_PYTHON $ROOT_DIR/dev/parse_notebook.py $1 $2"
