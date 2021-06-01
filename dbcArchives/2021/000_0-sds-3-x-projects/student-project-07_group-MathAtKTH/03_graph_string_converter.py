# Databricks notebook source
# MAGIC %md
# MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

# COMMAND ----------

# MAGIC %md
# MAGIC # Intro
# MAGIC 
# MAGIC In this notebook we create a code-writing program. Coding a specific motif generally makes for very long strings, the length grows as \\(n^2\\). Thus coding them directly is very inefficient. We solved this problem via a simple program. Here we can simply feed the adjacancy matrix of the motif we want to find, and it gives us the scala code.

# COMMAND ----------

import numpy as np

# COMMAND ----------

# MAGIC %md
# MAGIC In this funtion we input the adjacancy matrix of the motif we want to find. Then it produces the text of a scala command. Than command can then be copy-pasted into another notebook.
# MAGIC 
# MAGIC Here we simply go through the adjacancy matrix and fix the string thereafter. In this function the motif we want to find is induced by the graph. Thus each edge is either wanted or forbidden. 
# MAGIC 
# MAGIC loops - Do we care about loops or not.

# COMMAND ----------


def matrix_to_string(input_matrix, input_function_name, loops = True):
  ret_string = "def " + input_function_name + "(input_graph: GraphFrame) = {\n\tval string_" +input_function_name+ " = \""
  filter_string = ""
  pos_edges = ""
  neg_edges = ""
  alphabet = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z']
  
  for i in range(len(input_matrix)):
    for j in range(len(input_matrix[i])):
      if i == j:
        if loops and input_matrix[i][j] != 0:
          pos_edges += "(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
        elif loops:
          neg_edges += "!(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      elif input_matrix[i][j] != 0:
        pos_edges += "(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      else:
        neg_edges += "!(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      if i > j:
        filter_string += ".filter(\"" + alphabet[j] + ".id != " + alphabet[i] + ".id\")"
  ret_string += pos_edges + neg_edges
  ret_string = ret_string[0:-2]
  ret_string += "\"\n\tinput_graph.find(g_1)"
  ret_string += filter_string
  ret_string += ".count\n}\n"
  return ret_string
  
  
  

# COMMAND ----------

# MAGIC %md 
# MAGIC In this funtion we input the signed adjacancy matrix of the motif we want to find. Then it produces the text of a scala command. Than command can then be copy-pasted into another notebook.
# MAGIC 
# MAGIC Here we simply go through the adjacancy matrix and fix the string thereafter. In this function each edge can either be demanded, forbidden, or allowed. The three states are represented by 1, -1, and all other values.
# MAGIC 
# MAGIC loops - Do we care about loops or not.

# COMMAND ----------

def matrix_to_string_signed(input_matrix, input_function_name):
  ret_string = "def " + input_function_name + "(input_graph: GraphFrame) = {\n\tval string_" +input_function_name+ " = \""
  filter_string = ""
  pos_edges = ""
  neg_edges = ""
  alphabet = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z']
  
  for i in range(len(input_matrix)):
    for j in range(len(input_matrix[i])):
      if input_matrix[i][j] == 1:
        pos_edges += "(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      elif input_matrix[i][j] == -1:
        neg_edges += "!(" + alphabet[i] + ")-[]->(" + alphabet[j] + "); "
      if i > j:
        filter_string += ".filter(\"" + alphabet[j] + ".id != " + alphabet[i] + ".id\")"
  ret_string += pos_edges + neg_edges
  ret_string = ret_string[0:-2]
  ret_string += "\"\n\tinput_graph.find(g_1)"
  ret_string += filter_string
  ret_string += ".count\n}\n"
  return ret_string

# COMMAND ----------

# MAGIC %md 
# MAGIC # Examples
# MAGIC 
# MAGIC Here is a quick example where we have coded for a "claw". Another name for the claw is a star of size 3, meaning it has 4 vertices.

# COMMAND ----------

print(matrix_to_string([[0,0,0,1],[0,0,0,1],[0,0,0,1],[0,0,0,0]], "count_claw", loops = False))


# COMMAND ----------

# MAGIC %md
# MAGIC Here we cave coded for a v-structure.

# COMMAND ----------

print(matrix_to_string([[0,0,1],[0,0,1],[0,0,0]], "count_v_struc", loops = False))

# COMMAND ----------

# MAGIC %md
# MAGIC Counting how many loops

# COMMAND ----------

print(matrix_to_string([[1]], "count_loop"))

# COMMAND ----------

# MAGIC %md
# MAGIC A very important thing we want to look at is complete graphs. 
# MAGIC 
# MAGIC As these can grow big, we write a function to generate them.

# COMMAND ----------

def adj_matrix_complete_graph(size):
  adj_matrix = np.zeros((size,size))
  for i in range(len(adj_matrix)):
    for j in range(i+1, len(adj_matrix[i])):
      adj_matrix[i][j] = 1
  return adj_matrix
  

# COMMAND ----------

# MAGIC %md
# MAGIC Now we can generate the code of interest.

# COMMAND ----------

for i in range(3,7+1):
  print(matrix_to_string_signed(adj_matrix_complete_graph(i), "count_K"+str(i)))

# COMMAND ----------

