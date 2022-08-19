#! /usr/bin/python3
import argparse
import zputils

'''
    This script injects a couple of helper functions at the top of each notebook to better fascilitate the conversion
    from dbc to zeppelin notebooks.

'''


parser = argparse.ArgumentParser(description = "Injects helper functions for easier dbc -> zeppelin workflows.")
parser.add_argument('--host', default = 'localhost', dest = 'host', help = "Address of server runnin Zeppelin. Default: localhost")
parser.add_argument('--port', default = '8080', dest = 'port', help = "Port used by Zeppelin. Default: 8080")
parser.add_argument('--dir', default = '', dest = 'path', help = "Specify which notebooks to check, can be at folder level or individual notebooks.")

args = parser.parse_args()

host = "http://{}:{}".format(args.host, args.port)


if __name__ == "__main__":
    
    notebooks = zputils.get_notebook_ids(host, args.path)

    data_str_0 = '''{
        "title": "helper function injection", 
        "text": "%spark\n// helper functions\n'''
    func_str_0 = 'def display(df: Any) = z.show(df)\n'
    func_str_1 = 'def displayHTML(input: String):Unit = print(\\"%html \\" + input)'
    data_str_1 = '''",
        "index": 1
        } 
    '''   

    data = data_str_0 + func_str_0 + func_str_1 + data_str_1

    for id in notebooks:
        paragraphs = zputils.get_paragraphs(host, id)
        if 'title' in paragraphs[1] and paragraphs[1]['title'] == "helper function injection":
            zputils.update_paragraph_contents(host, id, paragraphs[1]['id'], data)       
        else:
            r = zputils.add_paragraph(host, id, data)
            if (r.status_code != 200):
                print("failed to inject cell in notebook: {}".format(notebooks[id]))


    

