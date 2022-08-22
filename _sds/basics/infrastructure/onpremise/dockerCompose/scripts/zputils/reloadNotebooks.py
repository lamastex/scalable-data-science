#! /usr/bin/python3
import argparse
import zputils

'''
    Reloads all notebooks on the given zeppelin server.


'''

parser = argparse.ArgumentParser(description = "Reloads all notebooks on the given zeppelin server.")
parser.add_argument('--host', default = 'localhost', dest = 'host', help = "Address of server runnin Zeppelin. Default: localhost")
parser.add_argument('--port', default = '8080', dest = 'port', help = "Port used by Zeppelin. Default: 8080")

args = parser.parse_args()

host = "http://{}:{}".format(args.host, args.port)


if __name__ == "__main__":
    r = zputils.reload_notebooks(host)

    if r.status_code != 200:
        print("Failed to reload notebooks")
