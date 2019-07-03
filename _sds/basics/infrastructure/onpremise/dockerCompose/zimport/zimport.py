#! /usr/bin/python3

import argparse
import json
import requests
import time
import os
from os.path import isfile, join

parser = argparse.ArgumentParser(description = "Import one or more Zeppelin \
    notebooks into a running Zeppelin server. The imported files will be found \
    in the folder set in ZEPPELIN_NOTEBOOK_DIR in the zeppelin-env.sh \
    configuration file, defaulting to the notebook folder in the zeppelin \
    root folder.")
parser.add_argument('-a', '--host', default = 'localhost', dest = 'host',
                    help = "Address of server running Zeppelin. Default: localhost")
parser.add_argument('-p', '--port', default = '8080', dest = 'port',
                    help = "Port used by Zeppelin. Default: 8080")
parser.add_argument('notebook_dir', metavar = "Input directory",
                    help = "Path to directory containing Zeppelin notebooks to be imported.")
args = parser.parse_args()

host = "http://{}:{}".format(args.host, args.port)

def import_notebook(note):
    requestURL = "{}/api/notebook/import".format(host)
    r = requests.post(requestURL, data = note.encode('utf-8')).json()
    if r["status"] == "OK":
        return r["body"]
    else:
        raise IOError(str(r))

if __name__ == "__main__":
    files = [join(args.notebook_dir, f) for f in os.listdir(args.notebook_dir)
             if isfile(join(args.notebook_dir, f))]
    for f in files:
        json = open(f, "r").read()
        import_notebook(json)


