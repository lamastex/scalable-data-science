import imageio
import visvis as vv
import json
import os

f = open('spark_notebook.json')
data = json.load(f)

try:
    os.rmdir("./gif")
except OSError as e:
    pass

try:
    os.mkdir(os.path.join("./","gif"))
except OSError as e:
    pass

print("// Databricks notebook source")

for cell in data['cells']:
    #start COMMAND -----
    #print("===== begin of a cell =======")
    print("\n// COMMAND ----------\n")
    print("// MAGIC %md")
    for img_num in cell['slideshow']:
        if(len(img_num.split("-"))==2):

            # make gif here
            startImg = img_num.split("-")[0]
            endImg = img_num.split("-")[1]
            images = []
            # print(f'make gif for {startImg} - {endImg}')

            for i in range(int(startImg), int(endImg)+1):
                images.append(imageio.imread("https://github.com/lamastex/scalable-data-science/blob/master/db/visualapi/med/visualapi-"+str(i)+".png?raw=true"))

            # save gif to local dir
            # eg. visualapi-93_102.gif
            gifName = "visualapi-"+str(startImg)+"_"+str(endImg)
            imageio.mimsave('./gif/'+gifName+'.gif', images, duration=5)

            # DO THIS MANUALLY <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            # Push gif to github in the following dir <<<<<<<<<<<<<<<<<<<<<<<<<<<<
            #
            # https://github.com/lamastex/scalable-data-science/blob/master/db/visualapi/med/xxx.gif
            #
            # You may push it after the spark_notebook.scala is done.
            #

            # insert it to the cell
            link_to_img = "https://github.com/lamastex/scalable-data-science/blob/master/db/visualapi/med/gif/"+gifName+".gif?raw=true"
            print(f'// MAGIC ![]({link_to_img})')

        else:
            # print(f'just insert {img_num} into the same cell')

            # just insert it to the cell
            link_to_img = "https://github.com/lamastex/scalable-data-science/blob/master/db/visualapi/med/visualapi-"+str(img_num)+".png?raw=true"
            print(f'// MAGIC ![]({link_to_img})')

        # print("---")
    #end COMMAND -----
    #print("===== end of a cell =======")
    print("\n// COMMAND ----------\n")

f.close()
