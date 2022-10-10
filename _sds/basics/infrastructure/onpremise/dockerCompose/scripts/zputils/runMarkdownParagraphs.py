#! /usr/bin/python3
import argparse
import zputils

'''
    This script executes all markdown paragraphs and hides the editor of those paragraphs.

    
'''


parser = argparse.ArgumentParser(description = "Runs all markdown paragraphs in given zeppelin notebooks and hides the editor.")
parser.add_argument('--host', default = 'localhost', dest = 'host', help = "Address of server runnin Zeppelin. Default: localhost")
parser.add_argument('--port', default = '8080', dest = 'port', help = "Port used by Zeppelin. Default: 8080")
parser.add_argument('--dir', default = '', dest = 'path', help = "Specify which notebooks to check, can be at folder level or individual notebooks.")

args = parser.parse_args()

host = "http://{}:{}".format(args.host, args.port)


if __name__ == "__main__":
    notebooks = zputils.get_notebook_ids(host, args.path)
    print("Executing markdown paragraphs in notebook:")
    for id in notebooks:
        print("\t{}".format(notebooks[id]))
        zputils.run_markdown_paragraphs(host, id)



