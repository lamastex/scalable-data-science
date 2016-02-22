// Databricks notebook source exported at Mon, 22 Feb 2016 04:12:15 UTC
// This is a sequence of functions that can be accessed by other notebooks using the following %run command:
// %run "/scalable-data-science/xtraResources/support/sdsFunctions"

// COMMAND ----------

//This allows easy embedding of publicly available information into any other notebook
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="100%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/ml-features.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }