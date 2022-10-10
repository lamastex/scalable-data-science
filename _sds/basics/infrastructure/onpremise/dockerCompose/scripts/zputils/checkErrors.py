#! /usr/bin/python3
import zputils
import argparse
import time


'''
    This script runs through all notebooks in a zeppelin server and reports those paragraphs that failed to execute.
    If called with the --dir flag you can specify a folder, or individual notebook to be checked


'''

parser = argparse.ArgumentParser(description = "Finds and reports errors in Zeppelin notebooks.")
parser.add_argument('--host', default = 'localhost', dest = 'host', help = "Address of server runnin Zeppelin. Default: localhost")
parser.add_argument('--port', default = '8080', dest = 'port', help = "Port used by Zeppelin. Default: 8080")
parser.add_argument('--dir', default = '', dest = 'path', help = "Specify which notebooks to check, can be at folder level or individual notebooks.")

args = parser.parse_args()

host = "http://{}:{}".format(args.host, args.port)


if __name__ == "__main__":

    notebooks = zputils.get_notebook_ids(host, args.path)

    for id in notebooks:
        zputils.run_notebook(host, id)
        time.sleep(5)
        paragraphs = zputils.get_paragraphs(host, id)
        paragraphs_text = [x['text'] for x in paragraphs]
        print("\n\n"+notebooks[id])
        for n in range(len(paragraphs)):
            status = [p['status'] for p in paragraphs]
            while (status[n] == "PENDING"):
                update_paragraphs = zputils.get_paragraphs(host, id)
                status = [p['status'] for p in update_paragraphs]
                time.sleep(5)
                print("...")
            if (status[n] == "ERROR"):
                print("\tparagraph {}\t\t{}\t\t{}".format(n, status[n], ' '.join(paragraphs_text[n].split()[:10])))

      
            
