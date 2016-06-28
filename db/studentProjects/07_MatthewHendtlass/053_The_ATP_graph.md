// Databricks notebook source exported at Tue, 28 Jun 2016 11:14:04 UTC


# The Association of Tennis Professionals graph


### Scalable data science project by Matthew Hendtlass
[Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)





The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/07_MatthewHendtlass/053_The_ATP_graph.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):

[![sds/uji/studentProjects/07_MatthewHendtlass/053_The_ATP_graph](http://img.youtube.com/vi/rnpa6YsDXWY/0.jpg)](https://www.youtube.com/v/rnpa6YsDXWY?rel=0&autoplay=1&modestbranding=1&start=927&end=2585)





In this notebook we play with open era (1968 - present) match data from the Association of Tennis Professionals (ATP), which despite the name only covers male players. Ultimately we're interested in exploring the ATP match data as a graph.

# Outline
 1. Grab atp player and match data.
 2. Explore how tennis has changed over the years.
 3. Build two graphs with players as vertices and with directed edges:
    (i) all matches, pointing from the loser to the winner,
    (ii) with edges representing player head-to-head records.
 4. Run a few page ranks on graph (i). Is pagerank a good measure of player performance?
 5. Run some motif queries on graph (ii).
 
All data we use can be scraped from the [ATP website](http://www.atpworldtour.com/) and is subject to their [terms and conditions](http://www.atpworldtour.com/en/terms-and-conditions). Thankfully Jeff Sackmann has done the hard work for us: we will grab our data from [here](https://github.com/JeffSackmann/tennis_atp). (Jeff Scakmann also has a tennis [blog](http://www.tennisabstract.com/blog/).)


```scala

//This allows easy embedding of publicly available information into any other notebook
//when viewing in git-book just ignore this block - you may have to manually chase the URL in frameIt("URL").
//Example usage:
// displayHTML(frameIt("https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation#Topics_in_LDA",250))
def frameIt( u:String, h:Int ) : String = {
      """<iframe 
 src=""""+ u+""""
 width="95%" height="""" + h + """"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe>"""
   }

```


 ### A little background on tennis -- homework

**(watch later: 5:20):** The greatest tennis match ever?

  [![](http://img.youtube.com/vi/IWMnLedLhCM/0.jpg)](https://www.youtube.com/v/IWMnLedLhCM?rel=0&autoplay=1&modestbranding=1&t=10m31s)





## 1. ETL

 * Create a dbfs folder for our data.
 * Grab player data and make a table.
 * Grab match data for 1968 - 2015 and make one table.





First we create a folder in the Databricks file system (dbfs).


```scala

//dbutils.fs.mkdirs("dbfs:/datasets/atp")

```



#### Player data
We now start grabbing the data and organising it. We start with player data (the nodes of our graph).


```scala

%sh 
## Grab the player data from Jeff Sackmann's GitHub repository.
# wget https://raw.githubusercontent.com/JeffSackmann/tennis_atp/master/atp_players.csv

```
```scala

// And copy the player data to our dbfs folder.
//dbutils.fs.cp("file:/databricks/driver/atp_players.csv", "dbfs:/datasets/atp/")

```
```scala

sc.textFile("dbfs:/datasets/atp/atp_players.csv").take(2) // We grab the first two lines of the csv (hoping to find a header).

```



We have a csv with no header. I don't know Gardnar Malloy or Pancho Segura, but it seems that each row has (unique id, forename, surname, handedness, date of birth, country). Though perhaps handedness is not the right term, since, for example, Rafael Nadal is right handed but plays [left handed](https://en.wikipedia.org/wiki/Rafael_Nadal#Early_life) (which gives some advantages, especially against Federer), while Maria Sharapova is left handed but played [right handed](http://www.worldtennismagazine.com/archives/10410). The date of birth should be formatted, but it is still easy to work with this way (as an integer with YYYYMMDD).


```scala

// Import packages for using SQL.
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType}

// Define the schema for our csv.
val playersSchema = StructType(Array(
    StructField("playerID", IntegerType, true),
    StructField("forename", StringType, true),
    StructField("surname", StringType, true),
    StructField("hand", StringType, true),
    StructField("dob", IntegerType, true),
    StructField("country", StringType, true)))

// And read the csv
val atp_players_raw = sqlContext.read              // using the spark.csv package:
      .format("com.databricks.spark.csv")          // set format to csv,
      .schema(playersSchema)                       // pass our schema,
      .load("dbfs:/datasets/atp/atp_players.csv")  // and pass the file.

// We register our data frame as a temporary table.
atp_players_raw.registerTempTable("atp_players_raw")
display(atp_players_raw)

```



We register our data frames as tables so we can manipulate data in SQL. Later we will filter our player table and add player heights, from the match data.





#### On to the match data

Good, it seems we got the names right (I could definitely believe a Malloy Gardnar, but not a Laver Rod). So next we want the edges: the match data. Jeff Sackmann has grouped matches by year, so we need to grab 48 files (we going to forget about 2016 -- as a Federer fan I'd also like to forget about 2013, but I'm trying to be objective :) and paste them together.


```scala

/*
%sh
## We loop over the years, using wget to grab the csv (atp_matches_[year].csv) for each year.
for i in $(seq 1968 2015) ; do
  wget https://raw.githubusercontent.com/JeffSackmann/tennis_atp/master/atp_matches_${i}.csv
done

pwd && ls # List all files to check they're all here (scroll to the bottom).
*/

```



Now we move them over to our dbfs folder, and take a look at what we have.


```scala

//dbutils.fs.cp("file:/databricks/driver/", "dbfs:/datasets/atp/", recurse = true) // Setting recurse = true tells Databricks to copy everything in the first directory into the second.
sc.textFile("dbfs:/datasets/atp/atp_matches_2015.csv").take(2) // We're going to assume all the files have the same format.

```



Yay! we have a header, and some players I recognise. So let's read these all in, glue it all together, and register a temp table.


```scala

// List out all years, 
val years = List.range(1968, 2016) 
// and then map each year to it's csv file and store these in atpDFs.
val atpDFs = years.map(i => sqlContext.read    
                       .format("com.databricks.spark.csv") // Use spark.csv package.
                       .option("header", "true")           // Use first line of all files as header.
                       .option("inferSchema", "true")      // Automatically infer data types.
                       .load("dbfs:/datasets/atp/atp_matches_".concat(i.toString).concat(".csv")))
// Finally we reduce atpDFs, using unionAll, to get all our data into one data frame.
val atpDF = atpDFs.reduce((aggDF, DF) => aggDF.unionAll(DF))
// As before, we register this as a temp table, so we can explore and manipulate it using SQL.
atpDF.registerTempTable("atp_matches")

```



Quick sanity check: do we have data from each year?


```scala

atpDF.select("tourney_date")
     .map(x => x.toString.take(5).drop(1).toInt) // Get only the year from each date integer.
     .distinct                                   
     .collect()
     .sorted

```


 
We print the schema below. Most of the columns have the right type, we should change the tournament date (and player DOB) into a date format, but the integer representation we have is very easy to work with (the function Dates \\( \rightarrow \mathbf{Z} \\) mapping DD-MM-YYYY to YYYYMMDD, so for example the treaty of [Waitangi](https://en.wikipedia.org/wiki/Treaty_of_Waitangi) was signed 18400206, preserves chronoligical order). If we want to do some machine learning we might want to change some of the integer columns, like player heights, into double; but this can wait for another day.


```scala

atpDF.printSchema()

```



So there's a lot here! We going to drop most of the match specifics.


```scala

// We restrict our match data to the following columns...
val atp_matches = atpDF.select($"tourney_id", $"tourney_name", $"surface", $"tourney_level", $"tourney_date", $"winner_id", $"winner_seed", $"winner_name", $"winner_hand", $"winner_ht", $"winner_ioc", $"winner_age", $"winner_rank", $"loser_id", $"loser_seed", $"loser_name", $"loser_hand", $"loser_ht", $"loser_ioc", $"loser_age", $"loser_rank", $"score", $"best_of", $"round", $"minutes", $"w_ace", $"l_ace")

// ... and register this restricted data frame as a temporary table.
atp_matches.registerTempTable("atp_matches")

```



We have 27 columns:
  * tourney_id: A unique identifier for the tournament, with four digits for the year, a hyphen and then a unique 3 digit code for the tournament. 
  * tourney_name: The name of the tournament. This can change from year to year; for example, we see above that what is now the Australian Open was known as the Australian Championships in 1968.
  * surface: The type of court the match was played on; can be hard, grass, clay, or carpet (which is no longer used).
  * tourney_level: The level of the tournament. G for grand slam, M for masters 1000, A for ATP 500 and 250 tournaments, D for Davis cup ties, and C.
  * winner_id: The player ID of the winner.
  * winner_seed: The seed of the winner.
  * winner_name: The name of the winner.
  * winner_hand: The handedness of the winner; L, R, or U for left, right, and unknown.
  * winner_ht: The height of the winner in cm.
  * winner_ioc: The country the winner represents.
  * winner_age: The winner's age at the start of the tournament.
  * winner_rank: The winner's rank at the start of the tournament.
  * loser_id, loser_seed, loser_name, loser_hand, loser_ht, loser_ioc, loser_age, loser_age, loser_rank are analogous to the winner columns.
  * score: The match score, which is between two and five n-m pairs each of which indicates that in the corresponding set the winner won n games and the loser m games.
  * best_of: The match type, either best-of 3, in which the first player to win 2 sets is the winner, or best-of 5.
  * round: Indicates the number of players that make it at least this far in the tournament. So for example the seven rounds in Wimbledon are round of 128, 64, 32, 16, 8, 4, and 2; the first four are denoted R128, R64, R32, and R16, while the other three are QF, SF, and F for quarter-final, semi-final, and final.
  * minutes: The duration of the match in minutes.
  * w_ace: The number of aces struck by the winner during the match.
  * l_ace: As for w_ace.





### 2. Exploring

Before we build and explore our two ATP graphs, let's see if we can spot any interesting temporal patterns.





How has the average winners age changed? For the mean we count a player's age once for each match they won. Interestingly the average winner's age has increased year-on-year since 2008 and in 2015 (at 27.88 years) was  higher than any previous year since 1968. (Is this because of fundamental changes in the game? -- players are now fitter and stronger than ever, and fitness and strnegth take time to develop -- or was the generation from the first half of the 80's, those 30-35 in 2015, particularly strong? or maybe the c. 1990 generation is particularly weak?)

The lowest average winner's age was 23.9 in 1986, when I was also at my youngest.


```scala

%sql -- We use floor(tourney_date / 10000) to extract the year from our date integers.
select floor(tourney_date / 10000) as year, 
       min(winner_age) as min_age, 
       max(winner_age) as max_age, 
       avg(winner_age) as mean_age 
from atp_matches 
group by floor(tourney_date / 10000)
-- We present the data as a line chart with key year, and the other three columns as values.

```



Are there more aces now than in the days of Pistol Pete (Sampras)? There was definitely an increase during the nineties, but average aces per match is still increasing. On average a match winner had around 2.18 more aces per match than a loser. (For comparison, Ivo Karlovic averages, at the time of writing, 19.3 aces per match.)

Much of our match data, such as the number of aces per match, has only been recorded since 1990.


```scala

%sql 
select floor(tourney_date / 10000) as year, 
       avg(w_ace) as winner_aces, 
       avg(l_ace) as loser_aces 
from atp_matches 
group by floor(tourney_date / 10000)

```



Do left-handers or right-handers have the advantage? In the general population around 13% of men are left-handed, so it would seem that left-handers were underperforming before 2000, but have been holding there own since. Of course the proportion of tennis players that play left handed may differ from the proportion of left-handers in the general public.


```scala

%sql 
select floor(tourney_date / 10000) as year, 
       sum(cast(winner_hand = 'R' as INT)) / (sum(cast(winner_hand = 'R' as INT)) + sum(cast(winner_hand = 'L' as INT))) as RH_percent 
from atp_matches 
group by floor(tourney_date / 10000)

```



Was there ever a time when I was tall enough to be a (male) tennis professional? Alas no, even in 1976, which seems to be the best year to be a short tennis player, the average winner was about 11.5cm taller than me. Though Rod Laver, who was a dominant force in tennis at the start of the open era, was only a little taller than me. (And [Renée Richards](https://en.wikipedia.org/wiki/Ren%C3%A9e_Richards) competed on both the mens and womens tours, and I'm definitely taller than Sara Erani, so there's another option. :) 

Being tall is clearly an advantage in tennis (except for on all those international flights): in 2015 the winners were on average 4mm taller than losers, while in 2009 the difference was almost a centimeter!


```scala

%sql 
select floor(tourney_date / 10000) as year, 
       avg(winner_ht) as winner_height, 
       avg(loser_ht) as loser_height, 
       min(loser_ht) as min_height, 
       max(loser_ht) as max_height 
from atp_matches 
group by floor(tourney_date / 10000)

```



Match lengths don't seem to have changed much since records began in 1990, although something strange happened in 1994 -- perhaps this is a data quality issue.


```scala

%sql 
select floor(tourney_date / 10000) as year, 
       avg(minutes) as mean_length,
       best_of
from atp_matches 
group by best_of, floor(tourney_date / 10000)

```



Let's see how impressive were the record breaking exploits of Isner and Mahut (so impressive that this is the only topic that Terry Tao and Tim Gowers have independently written blogs on) -- they played for more than 11 hours over three days, with Isner winning 70-68 in the fith set (without the introduction of tie breaks it might have been even longer!).


```scala

%sql 
select * 
from atp_edges 
where minutes > 180 
order by minutes desc

```



Why is mens tennis no longer very popular in the US? Let's look at the success of American men on the ATP tour by year.


```scala

%sql 
select floor(tourney_date / 10000) as year, 
       count(*) as USA_count
from atp_matches 
where winner_ioc == 'USA'
group by floor(tourney_date / 10000)

```


 
Compare this with the total number of matches below. 
In 1982 American men won 1725 of the 4109 matches played, while in 2015 they won only 200 of 2958.


```scala

%sql 
select count(*), 
       floor(tourney_date / 10000) as year
from atp_matches
group by floor(tourney_date / 10000)

```



Let's have a quick look at the Wimbledon seeds through the years.


```scala

%sql 
drop table wimbledon_seeds;

-- We build a table with only seeded players at Wimbledon.
-- We restrict to taking players from round of 128 matches to ensure we count each seed exactly once.
create table wimbledon_seeds as
select floor(tourney_date / 10000) as year,
       case
         when winner_seed is not null then winner_seed
         when loser_seed is not null then loser_seed
       end as seed,
       case
         when winner_seed is not null then winner_age
         when loser_seed is not null then loser_age
       end as age,
       case
         when winner_seed is not null then winner_ht
         when loser_seed is not null then loser_ht
       end as height
from atp_matches 
where tourney_name = 'Wimbledon' 
  and round = 'R128' 
  and (winner_seed is not null or loser_seed is not null)

```


 
Below is the age of top ten seeds, minimums, means, and maximums. Can you spot Federer? 


```scala

%sql 
select year, 
       avg(age) as mean_age, 
       min(age) as min_age, 
       max(age) as max_age 
from wimbledon_seeds 
where seed < 11 
group by year 
order by year

```


 
(Federer has been the oldest top ten seed since 2011 and was also the youngest top-ten seed in 2002.)





### 3. Graph building

Ok, now it's time to build our two graphs. We start by building the vertices data frame of player data, which will be the same for both graphs.





####Vertices
The players table contains also players that have only played in Futures and Challengers tournaments, which are below the ATP and Grand Slam level tournaments that are our focus. So we will filter our table to only include palyers with at least one match in our match data.


```scala

%sql 
drop table atp_players0;
drop table atp_players;

create table atp_players0 as
select distinct winner_id as players 
from atp_matches 
     union 
select distinct loser_id as players 
from atp_matches;
     
create table atp_players as
select b.* 
from (select distinct players from atp_players0) a
join atp_players_raw b
on a.players = b.playerID

```



We will also add player height to our player table. Just in case a player is still growing ([even into their 30's!](http://www.tennisworldusa.org/At-611-Ivo-Karlovic-is-still-growing-taller-articolo14009.html)), we take the greatest height recorded for any match.


```scala

%sql 
drop table atp_vertices;

create table atp_vertices as
select a.*, b.height
from atp_players a
join (select c.playerID, max(d.loser_ht) as height
      from atp_players c
      left join atp_matches d
      on c.playerID = d.loser_id
      group by c.playerID) b
on a.playerID = b.playerID;
     
select * from atp_vertices 

```



So how many open era mens tennis players have there been on the ATP?


```scala

%sql 
select count(*) 
from atp_vertices

```



#### Match graph edges

Before we form the edge set, I need to understand what the tournament levels mean. (We could use these as edge weights, with, for example, wins at a grand slam having more weight.)


```scala

%sql 
-- We look at all tournaments and their levels from 2014
select distinct tourney_name, tourney_level, tourney_date
from atp_matches 
where floor(tourney_date / 10000) = 2014 
order by tourney_name

```


 
We will give tournaments weights based on the ranking points the winner currently receives, except for Davis cup matches and 'C' tournaments. Since ATP 250 and 500 tournaments are lumped together we'll give them all weights of 500 (though we could use tournament names to separate these).

Hmmm, there were no 'C' tournaments in 2014.


```scala

%sql 
-- So we look at all touranments with level 'C'.
select * 
from atp_matches 
where tourney_level = 'C'

```



The player rankings seem quite low, but since there's a New Zealand match winner we'll lump these touranments together with the ATP 250 and 500 level tournaments.





##### Edges table


```scala

%sql 
drop table atp_edges0;
drop table atp_edges;

create table atp_edges0 as
select cast(substring(tourney_id, 6, 3) as int) as tourney_id, tourney_name, surface, 
       case tourney_level      -- here we define the weightings for wins by tournament
           when 'A' then 500   -- ATP 250 and 500 level tournaments are lumped together
           when 'C' then 500
           when 'D' then 500   -- Davis cup matches should probably have a range of weights...
           when 'M' then 1000  -- Masters 1000
           when 'F' then 1500  -- ATP finals
           when 'G' then 2000  -- Grand slams
       end as edge_weight,
       tourney_date, 
       winner_id as dst, 
       loser_id as src, 
       score, best_of, round, minutes
from atp_matches;

create table atp_edges as
select a.*, 
       concat(b.forename, b.surname) as dst_name 
from (select c.*, 
             concat(d.forename, d.surname) as src_name 
      from atp_edges0 c 
      join atp_vertices d
      on c.src = d.playerID) a
join atp_vertices b
on a.dst = b.playerID

```



We use the player ids, instead of names, for the edge sources and destinations just in case there are ever two players with the same name (which almost happened during a [match in 2015](http://edition.cnn.com/2015/05/18/tennis/joao-sousa-v-joao-souza-geneva-open/)).


```scala

%sql 
-- Just having a look at what we've got:
select src_name, dst_name, tourney_name, surface, edge_weight, 
       tourney_date, score, best_of, round, minutes 
from atp_edges

```



We can finally build our ATP match graph.


```scala

// Import SQL functions and the graph frames package.
import org.apache.spark.sql.functions._
import org.graphframes._

// Define vertice and edge data frames...
val vertices = sqlContext.sql("select playerID as id, forename, surname, hand, dob, country, height from atp_vertices").distinct().cache()
val edges = sqlContext.sql("select src, dst, tourney_id, tourney_name, surface, edge_weight, tourney_date, score, best_of, round, minutes from atp_edges").cache()

// and build the graph!
val atpGraph = GraphFrame(vertices, edges)

```


 
#### Head-to-head graph edges

I also want a restricted graph with an arrow from player A to player B only if B leads their head-to-head (or A and B are even).


```scala

%sql 
drop table temp;
drop table atp_h2h;

-- We first collpase mulitple directed edges between players to edges labelled with 
-- the total number of wins for the destination.
create table temp as
select count(src, dst) as count, 
       src, dst 
from atp_edges 
group by src, dst;

-- We then join this table to itself to give all match data between two players in a 
-- single row. Note each head-to-head occurs in two rows, one with each player as the 
-- sourse (resp. destination).
create table atp_h2h as
select b.count as src_wins, 
       c.count as dst_wins, 
       b.src, b.dst
from temp b
join temp c
on b.src = c.dst
and b.dst = c.src;

select * from atp_h2h

```



Later on we want to restrict this graph to edges where there have been many matches between players and where one player has a big(ish) lead over the other. To make this easier, our egde attributes will be the total number of matches between the two players (total) and the difference between the number of wins of the two players (diff) -- the destination will always have at least as many wins as the source. 


```scala

%sql
drop table atp_h2h_edges;

create table atp_h2h_edges as
select src, dst, src_wins + dst_wins as total, dst_wins - src_wins as diff
from atp_h2h
where src_wins <= dst_wins
     union
select dst as src, src as dst, src_wins + dst_wins as total, src_wins - dst_wins as diff
from atp_h2h
where dst_wins <= src_wins;

select * from atp_h2h_edges;

```



So here is our head-to-head graph:


```scala

val edgesH2H = sqlContext.sql("select * from atp_h2h_edges").cache()
val atpH2HGraph = GraphFrame(vertices, edgesH2H)

```


 
### 4. The matches graph
Before we run our PageRank queries, let's have a quick look at our graph.


```scala

println(s"Players: ${atpGraph.vertices.count()}")
println(s"Matches: ${atpGraph.edges.count()}")

```



We have just over 5500 players (we've lost one?) and 160k matches, so on average a player played a little under 60 matches.





A sanity check... the biggest winners: Jimmy Connors, Ivan Lendl, Roger Federer, Guillermo Vilas, ...


```scala

// Since edges point to the winner, the in-degree gives the number of wins.
val winners = atpGraph.inDegrees.sort($"inDegree".desc).limit(20)  // We take the top 20.

display(winners.join(vertices, winners("id") === vertices("id"))   // Here we join back to the vertices data frame to get names.
               .select("inDegree", "forename", "surname", "dob", "country", "height", "hand"))

```


 
We seem to have lost a few matches (27 of Jimmy Conners are lost, while we have found some extra wins for Vilas and McEnroe) and picked up a few others! (note that Federer and Nadal, ... have picked up more match wins this year). The differences are relativley small, so we're just going to ignore them for now.


```scala

displayHTML(frameIt("https://en.wikipedia.org/wiki/List_of_ATP_Tour_players_by_career_match_wins",250))

```


 
I don't know who has the most match loses. Let's find out:


```scala

val losers = atpGraph.outDegrees
                     .sort($"outDegree".desc)
                     .limit(20)

display(losers.join(vertices, losers("id") === vertices("id"))
              .select("outDegree", "forename", "surname", "dob", "country", "height", "hand"))

```



Eagle-eyed viewers may have noticed that Fabrice Santoro also makes Wikipedia's list of biggest winners, in 48th with 470 match wins (Mikhail Youzhny, with 468, is catching up though).





#### PageRank
The source code for the Spark implementation of PageRank can be found in [GitHub](https://github.com/apache/spark/blob/v1.6.1/graphx/src/main/scala/org/apache/spark/graphx/lib/PageRank.scala).


```scala

displayHTML(frameIt("https://en.wikipedia.org/wiki/PageRank", 300))

```


 
For connected graphs PageRank is a generalisation of the [eigenvector centrality](https://en.wikipedia.org/wiki/Centrality#Eigenvector_centrality). For a strongly connected graph, eigenvector centrality, which is so-called beacuase it is given by the values of the dominant eigenvector of the graphs adjacency matrix, is the proportion of times you visit each node in an infinite random walk of the graph, where at each step outgoing edges are weighted equally. In PageRank we are also making a random walk of our graph and calculating the proportion of times we visit each node, the difference is that for each step we make there is a chance (determined by the reset probability) that we will jump to a new randomly chosen node (that may even be the same node). This reset probability is a parameter to be chosen, and a bit surprisingly in some graphs it can have a [huge affect](http://www.sciencedirect.com/science/article/pii/S1570866709000926) on the resultant ranking (note though that for any graph the map from reset probability to PageRank values is continuous). (**Homework**: Try to construct a graph where the reset value makes a big difference in the resulting ranking. How does the variation in ranking depend on the graph size and density?)

For large graphs simulating a random walk may be much easier than calculating the eigenvectors of the (very large) adjacency matrix, but this is not an efficient way to calculate PageRank. We describe Apache Sparks iterative message passing algorithm for a strongly connected graph and with reset probability 0.

We begin by initialising all PageRank values to 1. For each iteration step, all nodes split their current PageRank value evenly among their outgoing edges passing this value along each outgoing edge. Then each node collects all the values it has been passed and sums them up to get their new PageRank score. Iterations continue until no node's value changes more than the tolerance. More formally, let \\(V\\) be the set of nodes and \\(E\\) the set of edge pairs, and denote the PageRank of \\(i \in V\\) by \\(p_i\\). Avoiding indicating the iteration, to save indices, we have:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![](https://dl.dropboxusercontent.com/u/61723687/PageRank_formula1.png)]()


Non-zero reset probabilities are incorporated in the aggregation of passed PageRank values:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![](https://dl.dropboxusercontent.com/u/61723687/PageRank_formula2.png)]()




 
##### PageRank on the whole graph

Who's the GOAT (Greatest Of All Time)? (Here's a cryptic clue for my opinion: Gave the stuttering queen the goat? (7))

PageRank was invented by Larry Page and Sergey Brin, the founders of Google, as a way of ranking websites by their 'link popularity'. The central idea is that a link from one website to another can be seen as an endorsement of the destination site by the source site. In our ATP match graph, each edge is a link from the loser to the winner, so it can likewise be seen as an endorsement of the destination player by the source player. Thus if we run PageRank on this graph players that have won more, better players(?), should have a higher PageRank value...


```scala

val atpPageRank = atpGraph.pageRank
                   .resetProbability(0.15) // We set the reset value to 0.15, which is the standard value.
                   .tol(0.01)              // We set the tolerance to 0.01, so PageRank values should be accurate to around 0.02.
                   .run()

```
```scala

// We grab the top 20 players by PageRank, in descending order.
val top20 = atpPageRank.vertices
                       .select("id", "pagerank")
                       .sort($"pagerank".desc)
                       .limit(20)

top20.registerTempTable("PageRankTop20")

display(top20.join(vertices, top20("id") === vertices("id"))
             .select("pagerank", "forename", "surname", "dob", "country", "height", "hand"))

```


 
The Top 20 are undeniably good players, but something is not quite right: PageRank seems to favour players with longer successful careers. So Jimmy Connors comes out on top, while David Ferrer (who is unlucky not to have won a major) beats Andy Murray (who is unlucky to have won only two), and they both beat Bjorn Borg (with 11 major titles) who doesn't make the list (and retired at the age of 26). This is not really surprising since losing a match doesn't (directly) harm a players PageRank (and could even improve it!).





##### Plotting the (PageRank) top 20
We're going to borrow some code from [this]("http://opiateforthemass.es/articles/analyzing-golden-state-warriors-passing-network-using-graphframes-in-spark/") awesome post of Yuki Katoh.


```scala

package d3
// We use a package object so that we can define top level classes like Edge that need to be used in other cells

import org.apache.spark.sql._
import com.databricks.backend.daemon.driver.EnhancedRDDFunctions.displayHTML

case class Edge(PLAYER: String, PASS_TO: String, PASS: Long, label: Long, pagerank: Double)

case class Node(name: String, label: Long, pagerank: Double)
case class Link(source: Int, target: Int, value: Long)
case class Graph(nodes: Seq[Node], links: Seq[Link])

object graphs {
val sqlContext = SQLContext.getOrCreate(org.apache.spark.SparkContext.getOrCreate())  
import sqlContext.implicits._
  
def force(network: Dataset[Edge], height: Int = 100, width: Int = 960): Unit = {
  val data = network.collect()
//   val nodes = (data.map(_.PLAYER) ++ data.map(_.PASS_TO)).map(_.replaceAll("_", " ")).toSet.toSeq.map(Node)
  val nodes = data.map { t =>
    Node(t.PLAYER, t.label, t.pagerank)}.distinct
  val links = data.map { t =>
    Link(nodes.indexWhere(_.name == t.PLAYER), nodes.indexWhere(_.name == t.PASS_TO), t.PASS / 20 + 1)
  }
  //     Link(nodes.indexWhere(_.name == t.PLAYER.replaceAll("_", " ")), nodes.indexWhere(_.name == t.PASS_TO.replaceAll("_", " ")), t.PASS / 20 + 1)
  showGraph(height, width, Seq(Graph(nodes, links)).toDF().toJSON.first())
}

/**
 * Displays a force directed graph using d3
 * input: {"nodes": [{"name": "..."}], "links": [{"source": 1, "target": 2, "value": 0}]}
 */
def showGraph(height: Int, width: Int, graph: String): Unit = {

displayHTML(s"""
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Polish Books Themes - an Interactive Map</title>
  <meta charset="utf-8">
<style>

.node_circle {
  stroke: #777;
  stroke-width: 1.3px;
}

.node_label {
  pointer-events: none;
}

.link {
  stroke: #777;
  stroke-opacity: .2;
}

.node_count {
  stroke: #777;
  stroke-width: 1.0px;
  fill: #999;
}

text.legend {
  font-family: Verdana;
  font-size: 13px;
  fill: #000;
}

.node text {
  font-family: "Helvetica Neue","Helvetica","Arial",sans-serif;
  font-size: 17px;
  font-weight: 200;
}

</style>
</head>

<body>
<script src="//d3js.org/d3.v3.min.js"></script>
<script>

var graph = $graph;

var width = $width,
    height = $height;

var color = d3.scale.category10();

var force = d3.layout.force()
    .charge(-700)
    .linkDistance(350)
    .size([width, height]);

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);
    
force
    .nodes(graph.nodes)
    .links(graph.links)
    .start();

var link = svg.selectAll(".link")
    .data(graph.links)
    .enter().append("line")
    .attr("class", "link")
    .style("stroke-width", function(d) { return Math.sqrt(d.value); });

var node = svg.selectAll(".node")
    .data(graph.nodes)
    .enter().append("g")
    .attr("class", "node")
    .call(force.drag);

node.append("circle")
    .attr("r", function(d) { return d.pagerank*10+4 ;})
    .style("fill", function(d) { return color(d.label);})
    .style("opacity", 0.5)

node.append("text")
      .attr("dx", 10)
      .attr("dy", ".35em")
      .text(function(d) { return d.name });
      
//Now we are giving the SVGs co-ordinates - the force layout is generating the co-ordinates which this code is using to update the attributes of the SVG elements
force.on("tick", function () {
    link.attr("x1", function (d) {
        return d.source.x;
    })
        .attr("y1", function (d) {
        return d.source.y;
    })
        .attr("x2", function (d) {
        return d.target.x;
    })
        .attr("y2", function (d) {
        return d.target.y;
    });
    d3.selectAll("circle").attr("cx", function (d) {
        return d.x;
    })
        .attr("cy", function (d) {
        return d.y;
    });
    d3.selectAll("text").attr("x", function (d) {
        return d.x;
    })
        .attr("y", function (d) {
        return d.y;
    });
});
</script>
</html>
""")
}
  
  def help() = {
displayHTML("""
<p>
Produces a force-directed graph given a collection of edges of the following form:</br>
<tt><font color="#a71d5d">case class</font> <font color="#795da3">Edge</font>(<font color="#ed6a43">PLAYER</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">PASS_TO</font>: <font color="#a71d5d">String</font>, <font color="#ed6a43">PASS</font>: <font color="#a71d5d">Long</font>, <font color="#ed6a43">label</font>: <font color="#a71d5d">Double</font>, <font color="#ed6a43">pagerank</font>: <font color="#a71d5d">Double</font>)</tt>
</p>
<p>Usage:<br/>
<tt>%scala</tt></br>
<tt><font color="#a71d5d">import</font> <font color="#ed6a43">d3._</font></tt><br/>
<tt><font color="#795da3">graphs.force</font>(</br>
&nbsp;&nbsp;<font color="#ed6a43">height</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">width</font> = <font color="#795da3">500</font>,<br/>
&nbsp;&nbsp;<font color="#ed6a43">clicks</font>: <font color="#795da3">Dataset</font>[<font color="#795da3">Edge</font>])</tt>
</p>""")
  }
}

```


 
**Homework**: Understand this code and cater it to our graph.




 
One downside of using someone else's code to hack a pretty graph is that we need to reshape our data quite a bit.


```scala

%sql 
drop table top20_vertices;
drop table top20_edges0;
drop table top20_edges;

-- We first collect some player info for our top 20.
create table top20_vertices as
select a.pagerank, b.*
from PageRankTop20 a
join (select playerID as id, 
      concat(forename, surname) as name, 
      hand, dob, country, height 
      from atp_vertices) b
on a.id = b.id;

-- And then restrict our graph to matches between top 20 players.
create table top20_edges0 as
select a.pagerank, 
       a.name as loser, 
       a.dob as loser_dob, 
       floor(a.dob / 100000) as decade,
       b.src_wins as number, 
       b.dst
from top20_vertices a
join atp_h2h b
on a.id = b.src
where b.dst in (100284, 103819, 100656, 100581, 104745, 
                104925, 100282, 100119, 101736, 100437, 
                101222, 101948, 101414, 100074, 100126, 
                100174, 103970, 100261, 104918, 103720);

create table top20_edges as
select a.*, 
       b.name as winner
from top20_edges0 a
join top20_vertices b
on a.dst = b.id

```
```scala

%sql 
select * 
from top20_edges

```


 
We use the d3 package -- defined in the cell above the last homewrok and shamelessly taken from Yuki Katoh's blog -- for plotting our top 20 graph.


```scala

import d3._
d3.graphs.help()

```


 
The node size is determined by PageRank score and the edge width by the total number of matches between pairs. The 2001 match between Federer and Sampras (and others?) does not appear, perhaps because this edge is to thin. The rivalry of the 'Big 4' really stands out (only the Sampras-Agassi and McEnroe-Connors edges are a similar width to those between Federer, Nadal, and Djokovic).


```scala

import d3._

graphs.force(
  height = 1000,
  width = 1000,
  network = sql("""
    select
      loser as PLAYER, 
      winner as PASS_TO, 
      decade as label,   
      number * 100 as PASS, 
      pagerank / 10 as pagerank
    from top20_edges
    """).as[Edge])

```



The reset value may make a difference to the PageRank results, so let's try a slightly smaller value.


```scala

val atpPageRank2 = atpGraph.pageRank
                           .resetProbability(0.1)    // Now we set the reset value to 0.1.
                           .tol(0.01)                // We set the tolerance to 0.01, so PageRank values should be accurate to around 0.02.
                           .run()
// We grab the top 20 players by PageRank, in descending order.
val top20_2 = atpPageRank2.vertices
                          .select("id", "pagerank")
                          .sort($"pagerank".desc)
                          .limit(20)

display(top20_2.join(vertices, top20_2("id") === vertices("id"))
               .select("pagerank", "forename", "surname", "dob", "country", "height", "hand"))

```



That looks better, at least it got the top 1 right :P There's also a strong pattern in the handedness of the top eight players!





##### What about on clay? It's got to be Rafa, no?

We can easily rank players on each surface by applying PageRank on the subgraph with only clay court matches in the edge data frame. We look at who is the clay court king.


```scala

val atp_edges_clay   = atpGraph.edges
                               .filter("surface = 'Clay'")  // Filter the edge data frame to get only clay court matches.
val atpGraph_clay    = GraphFrame(vertices, atp_edges_clay) // Build our new graph.

val atpPageRank_clay = atpGraph_clay.pageRank
                                    .resetProbability(0.15)
                                    .tol(0.01)
                                    .run()                   // We run PageRank with the same parameter valueas as before.
val clay_top20       = atpPageRank_clay.vertices
                                       .select("id", "pagerank")
                                       .sort($"pagerank".desc)
                                       .limit(20)            // And grab the top 20 clay courters by PageRank.

display(clay_top20.join(vertices, clay_top20("id") === vertices("id"))
                  .select("pagerank", "forename", "surname", "dob", "country", "height", "hand"))

```



Again the number of matches played is having a big influence (Vilas won 659 matches on clay!). At least Bjorn Borg makes it onto this list, though he's still below David Ferrer. It seems that to be a great clay courter you need to play left-handed (of course uncle Toni already knew that).





##### PageRank for ranking current players
The ATP ranking system is based on points accrued (dependent on tournament level and how deep a player goes in the tournament) in a moving one-year window. This in many ways is not very satisfying (for example your favourite player could potentially win this fortnights Grand slam, Roland Garros as it happens, and go backwards in the rankings -- well not right now). So we're going to try a 52 week page rank, picking 'at random' all matches from 2006.

Looking at a single year, the number of matches should be less significant, since to win many matches players needed to go deep in many tournaments.


```scala

val atp_edges2006 = atpGraph.edges
                            .filter("tourney_date > 20060000")
                            .filter("tourney_date < 20070000") // Filtering matches to those in 2006.
val atpGraph2006 = GraphFrame(vertices, atp_edges2006)

val atpPageRank2006 = atpGraph2006.pageRank
                                  .resetProbability(0.15)
                                  .tol(0.01)
                                  .run()                        // Run PageRank.
val top20_2006 = atpPageRank2006.vertices
                                .select("id", "pagerank")
                                .sort($"pagerank".desc)
                                .limit(20)                      // And grab the top 20.

display(top20_2006.join(vertices, top20_2006("id") === vertices("id"))
                  .select("pagerank", "forename", "surname", "dob", "country", "height", "hand"))

```


 
So possibly the greatest tennis player of all time in his best year on tour was only the second best player. For comparison [here](http://www.atpworldtour.com/en/rankings/singles?rankDate=2006-12-25) is the 2006 year end top 100.

This oddity is probably due to Nadal winning 4 of the 6 matches between him and Federer in 2006 (Federer had a 92-5 record for the year, the only non-Nadal loss was to Andy Murray).

So perhaps PageRank is not the best way to rank tennis players.




 
**Homework**: Allow more control over edge weights in PageRank. So, for example, we can use match importance to weight edges.




 
#### Community detection by label propogation

While we're here we'll run label propogation to see if we can find  any ATP communities.


```scala

val communities = atpGraph.labelPropagation
                          .maxIter(10)      // Label propagation need not converge, so we set a maximum number of iterations.
                          .run()

communities.registerTempTable("communities")

communities.select("label")
           .distinct
           .count                           // We have 58 communities.

```
```scala

%sql 
select count(*), 
       label 
from communities 
group by label

```



We have 58 communities, but only four have more than 2 members. Let's look into the smallest of these four, which has 99 members.


```scala

%sql 
select * 
from communities 
where label = 106580

```


 
I don't know any of these players, but from the country column it seems we've found a group of players that met frequently in tournaments in Asia and the Middle East.

**Homework**: Examine the three biggest clusters and give each a catchy name.




 
##### Big 4 subgraph

Finally, let's have a look at the 'Big 4': Federer, Nadal, Djokovic, and Murray.


```scala

%sql 
drop table big4_vertices;
drop table big4_edges;

-- We restrict the vertices to the bid four...
create table big4_vertices as
select * 
from atp_vertices
where playerID in (103819, 104745, 104925, 104918);

-- and the matches to those between the big 4.
create table big4_edges as
select * 
from atp_edges
where src in (103819, 104745, 104925, 104918)
and dst in (103819, 104745, 104925, 104918);

```
```scala

val big4_edges    = sqlContext.sql("select * from big4_edges")      // Grab the edges
val big4_vertices = sqlContext.sql("select * from big4_vertices")   // and vertices,
                              .withColumnRenamed("playerID", "id")
                              .distinct()
val big4Graph     = GraphFrame(big4_vertices, big4_edges)           // then build the graph.

```
```scala

val big4PageRank = big4Graph.pageRank                               // Run PageRank.
                            .resetProbability(0.15)
                            .tol(0.01)
                            .run()

```
```scala

val big4PageRankVertices = big4PageRank.vertices
                                       .select("id", "pagerank")
big4PageRankVertices.registerTempTable("big4PRV")

display(big4PageRankVertices.join(vertices, big4PageRankVertices("id") === vertices("id"))
                  .select("pagerank", "forename", "surname", "dob", "country", "height", "hand"))

```



PageRank has finally given us a good summary of our graph. Of course a four node graph isn't so difficult to interpret.





### 5. Motif finding on the head2head graph




 
Our head-to-head graph has the 5554 vertices from before, but only 23920 edges, which gives an upper bound for the number of 'rivalries'.


```scala

println(s"Players: ${atpH2HGraph.vertices.count()}")
println(s"Matches: ${atpH2HGraph.edges.count()}")

```



For a brief, wonderful, time in tennis there was no clear winner among the 'Big 3': Nadal would beat Federer, Federer would beat Djokovic, and Djokovic would beat Nadal. Alas injury and age have hampered Nadal and Federer, while Djokovic has been dominant and now holds a winning record over both Nadal and Federer. Are there any interesting and persisting breakdowns of transitivity (if A beats B and B beats C, then A beats C) in the open era? For this exercise interesting and persistant is defined as: the two players have met at least ten times and one players has at least 3 more wins in their head-to-head than the other.


```scala

val edgesH2H_restricted = sqlContext.sql("select * from atp_h2h_edges where total > 9 and diff > 2")   // We restrict to interesting edges, as defined above.
val atpH2Hgraph2 = GraphFrame(vertices, edgesH2H_restricted)

```



The simplest breakdown in transitivity is a directed loop with three vertices (a triangle). 


```scala

val transitivityFailure = atpH2Hgraph2.find("(a)-[e]->(b); (b)-[e2]->(c); (c)-[e3]->(a)")  // Setting our triangle motif, and searching.

display(transitivityFailure.filter("a.dob < b.dob")                                        // We restrict to when vertice a is the youngest
                           .filter("a.dob < c.dob")                                        // to avoid repetition.
                           .select("a.forename", "a.surname", "e.total", "e.diff",
                                   "b.forename", "b.surname", "e2.total", "e2.diff",
                                   "c.forename", "c.surname", "e3.total", "e3.diff"))

```



We have only three examples! involving only six players. Harold Solomon appears twice with four rivalries, while the rivalry between Brian Gottfried and Eddie Dibbs occurs in two examples. It would seem that Gottfried's dominence of Dibbs (winning 8 times to 5) is an abberation.





Since our graph is relatively sparse (so not all pairs have played each other), it is possible to have breakdowns of transitivity involving any number of players. 

Any breakdown in transitivity will look like a directed loop in our graph. Are there any breakdowns in transitivity between four players?


```scala

val transitivityFailure4 = atpH2Hgraph2.find("(a)-[e]->(b); (b)-[e2]->(c); (c)-[e3]->(d); (d)-[e4]->(a)")
display(transitivityFailure4.filter("a.dob < b.dob").filter("a.dob < c.dob").filter("a.dob < d.dob")
                            .select("a.forename", "a.surname", "e.total", "e.diff",
                                    "b.forename", "b.surname", "e2.total", "e2.diff",
                                    "c.forename", "c.surname", "e3.total", "e3.diff",
                                    "d.forename", "d.surname", "e4.total", "e4.diff"))

```



There are two, but we only have one more player, Raul Ramirez, and three more edges from those present in the triangles.

**Homework**: Draw the subgraph of all players in our intransitive relationships.




 
##### A stateful motif

With players that have an extensive and fairly even head-to-head record (such as Djokovic and Nadal, which Djokovic leads 26 to 23) there are often periods of dominence for each player. So even though both players have many match wins there may be few 'switch points' where each player won one of two consecutive matches. (Nadal and Djokovic have 17 switch points, with Djokovic having won their last 7 encounters.) 

I'm interested in which two players have had the most switch points, but we'll begin by looking for pairs with at least four switch points.


```scala

// Load packages.
import org.apache.spark.sql.Column
//import org.apache.spark.sql.functions.{col, when}    // We have already loaded this one.

// Set up our motif. For four switches we need five matches with alternating winners.
val chain5 = atpGraph.find("(v1)-[e1]->(v2); (v2)-[e2]->(v1); (v1)-[e3]->(v2);(v2)-[e4]->(v1); (v1)-[e5]->(v2)")

// Next we want a way of updating our state to ensure that these alternating matches are in chronological order.
// We do this by letting the state be the date of the most recent match if all matches so far are in order, and
// setting the state to infinity (= 100000000 in our case, since this is bigger than all match dates) if something
// goes wrong. If, after traversing all edges, the state is less than infinity (100000000), then they must all be 
// in chronological order.
def After(state: Column, next_date: Column): Column = {
  when(state < next_date, next_date).otherwise(100000000)
}

// We use the sequence operation to apply the method to the sequence of elements in our motif.
val condition = Seq("e1", "e2", "e3", "e4", "e5")
                    .foldLeft(lit(0))((state, e) => After(state, col(e)("tourney_date")))

// And filter the data frame to remove non-chronological sequences, where condition > 100000000.
val chronologicalChain = chain5.where(condition < 100000000)

display(chronologicalChain.select("v1", "v2"))

```


 
Orantes and Ramimez, who we have seen before, have precisely 4 switch points. However they appear many times in our chronologicalChain becuase when constructing a maximal chronological chain each block of consecutive wins by one of the players can be represented by any of the matches in that block. As a result, there are many ways (the product of the block sizes) we can get a chronological chain of length four from Orantes and Ramirez's rivalry. If there are more than four switch points, then we get many more chains from choosing different subsets of the available blocks.





**Homework**: Can we avoid counting each rivalry, that meets our condition, so many times? This will require considering more than one edge at a time (for example we could always extend our chain with the oldest match satisfying our condition).


```scala

display(chronologicalChain.select("v1", "v2").distinct())

```



It is now easy, for us, to increase our motif to capture \\( n\\) switch points. To find the most switch points we need only look at pairs with at least 17, since the Nadal-Djokovic rivalry provides an upper bound.

Here's code for \\( n = 17\\) (which I haven't run).


```scala

// We add more alternating matches to the chain,
val chain18 = atpGraph.find("(v1)-[e1]->(v2); (v2)-[e2]->(v1); (v1)-[e3]->(v2); (v2)-[e4]->(v1); (v1)-[e5]->(v2); (v2)-[e6]->(v1); (v1)-[e7]->(v2); (v2)-[e8]->(v1); (v1)-[e9]->(v2); (v2)-[e10]->(v1); (v1)-[e11]->(v2); (v2)-[e12]->(v1); (v1)-[e13]->(v2); (v2)-[e14]->(v1); (v1)-[e15]->(v2); (v2)-[e16]->(v1); (v1)-[e17]->(v2); (v2)-[e18]->(v1)")
// and the new edges to the condition.
val condition18 = Seq("e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "e10", "e11", "e12",
                      "e13", "e14", "e15", "e16", "e17", "e18")
                        .foldLeft(lit(0))((state, e) => After(state, col(e)("tourney_date")))
// Then run as before.
val chronologicalChain18 = chain18.where(condition18 < 100000000)

display(chronologicalChain18.select("v1", "v2").distinct())

```


 
We might want to filter our graph before doing such a query. We could use the head-to-head data to find players with at least one rivalry with 33 or more matches where each player won at least 16 matches, then filter the match graph to only these players and apply the above query to this new, much smaller, graph.





#### Some house-keeping

So we've had some fun, but now it's time to tidy up and go home. We're going to delete all of our tables.


```scala

sqlContext.tables.show

```
```scala

%sql
drop table atp_edges;
drop table atp_edges0;
drop table atp_h2h;
drop table atp_h2h_edges;
drop table atp_h2h_vertices;
drop table atp_players;
drop table atp_players0;
drop table atp_vertices;
drop table big4_edges;
drop table big4_vertices;
drop table temp;
drop table top20_edges;
drop table top20_edges0;
drop table top20_vertices;
drop table wimbledon_seeds

```





# [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)



### Course Project by Matthew Hendtlass 
*supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
and 
[![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)
