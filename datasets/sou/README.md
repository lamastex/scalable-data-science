## A bit of bash and lynx to achieve the scraping of the state of the union addresses of the US Presidents
### by Paul Brouwers

The code below is mainly there to show how the text content of each state of the union address was scraped from the following URL:
* [http://stateoftheunion.onetwothree.net/texts/index.html](http://stateoftheunion.onetwothree.net/texts/index.html)

Such data acquisition tasks is usually the first and cucial step in a data scientist's workflow.

We have done this and put the data in the distributed file system for easy loading into our notebooks for further analysis.  This keeps us from having to install unix programs like ``lynx``, ``sed``, etc. that are needed in the shell script below.

```%sh
for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html | grep texts | grep -v index | sed 's/.*http/http/') ; do lynx --dump $i | tail -n+13 | head -n-14 | sed 's/^\s\+//' | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt ; done
```

Or in a more atomic form:

```%sh
for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html \

        | grep texts \

        | grep -v index \

        | sed 's/.*http/http/')

do 

        lynx --dump $i \

               | tail -n+13 \

               | head -n-14 \

               | sed 's/^\s\+//' \

               | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' \

               > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt

done
```
**Don't re-evaluate!**

The following BASH (shell) script can be made to work on databricks cloud directly by installing the dependencies such as ``lynx``, etc.  Since we have already scraped it and put the data in our distributed file system **let's not evaluate or ``<Ctrl+Enter>`` the cell below**.  The cell is mainly there to show how it can be done (you may want to modify it to scrape other sites for other text data).

```%sh 
#remove the hash character from the line below to evaluate when needed
#for i in $(lynx --dump http://stateoftheunion.onetwothree.net/texts/index.html | grep texts | grep -v index | sed 's/.*http/http/') ; do lynx --dump $i | tail -n+13 | head -n-14 | sed 's/^\s\+//' | sed -e ':a;N;$!ba;s/\(.\)\n/\1 /g' -e 's/\n/\n\n/' > $(echo $i | sed 's/.*\([0-9]\{8\}\).*/\1/').txt ; done
```
We can just grab the data as a tarball (gnuZipped tar archive) file ``sou.tar.gz`` using wget as follows:

```%sh
wget http://www.math.canterbury.ac.nz/~r.sainudiin/datasets/public/SOU/sou.tar.gz

wget https://dl.dropboxusercontent.com/u/3531607/datasets/StateOfUnionAddressesUSPresidentsUntil2016/sou.tar.gz

wget https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/datasets/sou/sou.tar.gz
```

To get the fileneames in dir sou/ inorder to download and move each file one at a time as a hack around databricks Community Edition's limited space in local directories needed for transferring files from the internet into the distributed file system dbfs, do the following:

To get filenames in GNU/Linux:
```
tar zxvf sou.tar.gz 
ls sou/ |  sed -e "s/'/'\\\\''/g;s/\(.*\)/'\1'/" |  tr '\n' ', ' > fileNames
```

Open fileNames in vi for example and replace all `'` by `"` and delete the last ','.
```
vi fileNames
:%s/'/"/g
```

Raazesh Sainudiin

Fri Feb 19 11:08:00 NZDT 2016, Christchurch, NZ

Fri Aug 26 01:20:24 CEST 2016, London, UK
