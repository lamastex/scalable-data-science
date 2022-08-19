import requests

'''
    A collection of functions to help interface with the Zeppelin 0.10.0 REST API 
    Currently only covers the "notebook" section.
    Read the documentation at https://zeppelin.apache.org/docs/0.10.0/usage/rest_api/notebook.html

    - K.T 2022
'''



'''
    Returns a list with basic information of every notebook on the zeppelin server
    https://zeppelin.apache.org/docs/0.10.0/usage/rest_api/notebook.html#list-of-the-notes
'''
def list_notebooks(host):
    requestURL = "{}/api/notebook".format(host)
    r = requests.get(requestURL)
    return r



'''
    Returns a map of id:path pairs of every notebook on the zeppelin server
'''
def get_notebook_ids(host, path):
    r = list_notebooks(host)
    return {note['id']:note['path'] for note in r.json()['body'] if path in note['path']}



'''
    Run all cells in a given notebook. Cannot go past cells that don't compile.
    https://zeppelin.apache.org/docs/0.10.0/usage/rest_api/notebook.html#run-all-paragraphs
'''
def run_notebook(host, nb_id):
    requestURL = "{}/api/notebook/job/{}".format(host, nb_id)
    r = requests.post(requestURL)
    return r



'''
    Run all cells in the given notebook. Cannot go past cells that don't compile.
    https://zeppelin.apache.org/docs/0.10.0/usage/rest_api/notebook.html#run-all-paragraphs
'''
def get_notebook_status(host, nb_id):
    requestURL = "{}/api/notebook/{}".format(host, nb_id)
    r = requests.get(requestURL)
    return r



'''
    Returns a list of detailed information of every paragraph in a notebook
'''
def get_paragraphs(host, nb_id):
    return get_notebook_status(host, nb_id).json()['body']['paragraphs']



'''
    Updates the configuration of a paragraph
    https://zeppelin.apache.org/docs/0.10.0/usage/rest_api/notebook.html#update-paragraph-configuration
'''
def update_paragraph_config(host, nb_id, par_id, data):
    requestURL = "{}/api/notebook/{}/paragraph/{}/config".format(host, nb_id, par_id)  
    r = requests.put(requestURL, data=data)
    return r



'''
    Updates the contents of a paragraph
    https://zeppelin.apache.org/docs/0.10.0/usage/rest_api/notebook.html#update-paragraph
'''
def update_paragraph_contents(host, nb_id, par_id, data):
    requestURL = "{}/api/notebook/{}/paragraph/{}".format(host, nb_id, par_id)  
    r = requests.put(requestURL, data=data)
    return r



'''
    Creates a new paragraph
    https://zeppelin.apache.org/docs/0.10.0/usage/rest_api/notebook.html#create-a-new-paragraph
'''
def add_paragraph(host, nb_id, data):
    requestURL = "{}/api/notebook/{}/paragraph".format(host, nb_id)
    r = requests.post(requestURL, data = data)  
    return r


'''
    Finds all markdown paragraphs, runs them and hides the editor. 
'''
def run_markdown_paragraphs(host, nb_id):
    paragraphs = get_paragraphs(host, nb_id)
    markdown_id = [p['id'] for p in paragraphs if p['text'].split()[0] == "%md"]
    for par_id in markdown_id: # paragraph_id
        update_paragraph_config(host, nb_id, par_id, '''{"editorHide": "true"}''')
        requestURL = "{}/api/notebook/run/{}/{}".format(host, nb_id, par_id)
        requests.post(requestURL)



