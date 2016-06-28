// Databricks notebook source exported at Tue, 28 Jun 2016 11:14:04 UTC
// MAGIC %md
// MAGIC 
// MAGIC # The Association of Tennis Professionals graph
// MAGIC 
// MAGIC 
// MAGIC ### Scalable data science project by Matthew Hendtlass
// MAGIC [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/) *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC The [html source url](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/db/studentProjects/07_MatthewHendtlass/053_The_ATP_graph.html) of this databricks notebook and its recorded Uji ![Image of Uji, Dogen's Time-Being](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/UjiTimeBeingDogen.png "uji"):
// MAGIC 
// MAGIC [![sds/uji/studentProjects/07_MatthewHendtlass/053_The_ATP_graph](http://img.youtube.com/vi/rnpa6YsDXWY/0.jpg)](https://www.youtube.com/v/rnpa6YsDXWY?rel=0&autoplay=1&modestbranding=1&start=927&end=2585)

// COMMAND ----------

// MAGIC %md
// MAGIC In this notebook we play with open era (1968 - present) match data from the Association of Tennis Professionals (ATP), which despite the name only covers male players. Ultimately we're interested in exploring the ATP match data as a graph.
// MAGIC 
// MAGIC # Outline
// MAGIC  1. Grab atp player and match data.
// MAGIC  2. Explore how tennis has changed over the years.
// MAGIC  3. Build two graphs with players as vertices and with directed edges:
// MAGIC     (i) all matches, pointing from the loser to the winner,
// MAGIC     (ii) with edges representing player head-to-head records.
// MAGIC  4. Run a few page ranks on graph (i). Is pagerank a good measure of player performance?
// MAGIC  5. Run some motif queries on graph (ii).
// MAGIC  
// MAGIC All data we use can be scraped from the [ATP website](http://www.atpworldtour.com/) and is subject to their [terms and conditions](http://www.atpworldtour.com/en/terms-and-conditions). Thankfully Jeff Sackmann has done the hard work for us: we will grab our data from [here](https://github.com/JeffSackmann/tennis_atp). (Jeff Scakmann also has a tennis [blog](http://www.tennisabstract.com/blog/).)

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md ### A little background on tennis -- homework
// MAGIC 
// MAGIC **(watch later: 5:20):** The greatest tennis match ever?
// MAGIC 
// MAGIC   [![](http://img.youtube.com/vi/IWMnLedLhCM/0.jpg)](https://www.youtube.com/v/IWMnLedLhCM?rel=0&autoplay=1&modestbranding=1&t=10m31s)

// COMMAND ----------

// MAGIC %md
// MAGIC ## 1. ETL
// MAGIC 
// MAGIC  * Create a dbfs folder for our data.
// MAGIC  * Grab player data and make a table.
// MAGIC  * Grab match data for 1968 - 2015 and make one table.

// COMMAND ----------

// MAGIC %md
// MAGIC First we create a folder in the Databricks file system (dbfs).

// COMMAND ----------

//dbutils.fs.mkdirs("dbfs:/datasets/atp")

// COMMAND ----------

// MAGIC %md
// MAGIC #### Player data
// MAGIC We now start grabbing the data and organising it. We start with player data (the nodes of our graph).

// COMMAND ----------

// MAGIC %sh 
// MAGIC ## Grab the player data from Jeff Sackmann's GitHub repository.
// MAGIC # wget https://raw.githubusercontent.com/JeffSackmann/tennis_atp/master/atp_players.csv

// COMMAND ----------

// And copy the player data to our dbfs folder.
//dbutils.fs.cp("file:/databricks/driver/atp_players.csv", "dbfs:/datasets/atp/")

// COMMAND ----------

sc.textFile("dbfs:/datasets/atp/atp_players.csv").take(2) // We grab the first two lines of the csv (hoping to find a header).

// COMMAND ----------

// MAGIC %md
// MAGIC We have a csv with no header. I don't know Gardnar Malloy or Pancho Segura, but it seems that each row has (unique id, forename, surname, handedness, date of birth, country). Though perhaps handedness is not the right term, since, for example, Rafael Nadal is right handed but plays [left handed](https://en.wikipedia.org/wiki/Rafael_Nadal#Early_life) (which gives some advantages, especially against Federer), while Maria Sharapova is left handed but played [right handed](http://www.worldtennismagazine.com/archives/10410). The date of birth should be formatted, but it is still easy to work with this way (as an integer with YYYYMMDD).

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md
// MAGIC We register our data frames as tables so we can manipulate data in SQL. Later we will filter our player table and add player heights, from the match data.

// COMMAND ----------

// MAGIC %md
// MAGIC #### On to the match data
// MAGIC 
// MAGIC Good, it seems we got the names right (I could definitely believe a Malloy Gardnar, but not a Laver Rod). So next we want the edges: the match data. Jeff Sackmann has grouped matches by year, so we need to grab 48 files (we going to forget about 2016 -- as a Federer fan I'd also like to forget about 2013, but I'm trying to be objective :) and paste them together.

// COMMAND ----------

/*
%sh
## We loop over the years, using wget to grab the csv (atp_matches_[year].csv) for each year.
for i in $(seq 1968 2015) ; do
  wget https://raw.githubusercontent.com/JeffSackmann/tennis_atp/master/atp_matches_${i}.csv
done

pwd && ls # List all files to check they're all here (scroll to the bottom).
*/

// COMMAND ----------

// MAGIC %md
// MAGIC Now we move them over to our dbfs folder, and take a look at what we have.

// COMMAND ----------

//dbutils.fs.cp("file:/databricks/driver/", "dbfs:/datasets/atp/", recurse = true) // Setting recurse = true tells Databricks to copy everything in the first directory into the second.
sc.textFile("dbfs:/datasets/atp/atp_matches_2015.csv").take(2) // We're going to assume all the files have the same format.

// COMMAND ----------

// MAGIC %md
// MAGIC Yay! we have a header, and some players I recognise. So let's read these all in, glue it all together, and register a temp table.

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md
// MAGIC Quick sanity check: do we have data from each year?

// COMMAND ----------

atpDF.select("tourney_date")
     .map(x => x.toString.take(5).drop(1).toInt) // Get only the year from each date integer.
     .distinct                                   
     .collect()
     .sorted

// COMMAND ----------

// MAGIC %md 
// MAGIC We print the schema below. Most of the columns have the right type, we should change the tournament date (and player DOB) into a date format, but the integer representation we have is very easy to work with (the function Dates \\( \rightarrow \mathbf{Z} \\) mapping DD-MM-YYYY to YYYYMMDD, so for example the treaty of [Waitangi](https://en.wikipedia.org/wiki/Treaty_of_Waitangi) was signed 18400206, preserves chronoligical order). If we want to do some machine learning we might want to change some of the integer columns, like player heights, into double; but this can wait for another day.

// COMMAND ----------

atpDF.printSchema()

// COMMAND ----------

// MAGIC %md
// MAGIC So there's a lot here! We going to drop most of the match specifics.

// COMMAND ----------

// We restrict our match data to the following columns...
val atp_matches = atpDF.select($"tourney_id", $"tourney_name", $"surface", $"tourney_level", $"tourney_date", $"winner_id", $"winner_seed", $"winner_name", $"winner_hand", $"winner_ht", $"winner_ioc", $"winner_age", $"winner_rank", $"loser_id", $"loser_seed", $"loser_name", $"loser_hand", $"loser_ht", $"loser_ioc", $"loser_age", $"loser_rank", $"score", $"best_of", $"round", $"minutes", $"w_ace", $"l_ace")

// ... and register this restricted data frame as a temporary table.
atp_matches.registerTempTable("atp_matches")

// COMMAND ----------

// MAGIC %md
// MAGIC We have 27 columns:
// MAGIC   * tourney_id: A unique identifier for the tournament, with four digits for the year, a hyphen and then a unique 3 digit code for the tournament. 
// MAGIC   * tourney_name: The name of the tournament. This can change from year to year; for example, we see above that what is now the Australian Open was known as the Australian Championships in 1968.
// MAGIC   * surface: The type of court the match was played on; can be hard, grass, clay, or carpet (which is no longer used).
// MAGIC   * tourney_level: The level of the tournament. G for grand slam, M for masters 1000, A for ATP 500 and 250 tournaments, D for Davis cup ties, and C.
// MAGIC   * winner_id: The player ID of the winner.
// MAGIC   * winner_seed: The seed of the winner.
// MAGIC   * winner_name: The name of the winner.
// MAGIC   * winner_hand: The handedness of the winner; L, R, or U for left, right, and unknown.
// MAGIC   * winner_ht: The height of the winner in cm.
// MAGIC   * winner_ioc: The country the winner represents.
// MAGIC   * winner_age: The winner's age at the start of the tournament.
// MAGIC   * winner_rank: The winner's rank at the start of the tournament.
// MAGIC   * loser_id, loser_seed, loser_name, loser_hand, loser_ht, loser_ioc, loser_age, loser_age, loser_rank are analogous to the winner columns.
// MAGIC   * score: The match score, which is between two and five n-m pairs each of which indicates that in the corresponding set the winner won n games and the loser m games.
// MAGIC   * best_of: The match type, either best-of 3, in which the first player to win 2 sets is the winner, or best-of 5.
// MAGIC   * round: Indicates the number of players that make it at least this far in the tournament. So for example the seven rounds in Wimbledon are round of 128, 64, 32, 16, 8, 4, and 2; the first four are denoted R128, R64, R32, and R16, while the other three are QF, SF, and F for quarter-final, semi-final, and final.
// MAGIC   * minutes: The duration of the match in minutes.
// MAGIC   * w_ace: The number of aces struck by the winner during the match.
// MAGIC   * l_ace: As for w_ace.

// COMMAND ----------

// MAGIC %md
// MAGIC ### 2. Exploring
// MAGIC 
// MAGIC Before we build and explore our two ATP graphs, let's see if we can spot any interesting temporal patterns.

// COMMAND ----------

// MAGIC %md
// MAGIC How has the average winners age changed? For the mean we count a player's age once for each match they won. Interestingly the average winner's age has increased year-on-year since 2008 and in 2015 (at 27.88 years) was  higher than any previous year since 1968. (Is this because of fundamental changes in the game? -- players are now fitter and stronger than ever, and fitness and strnegth take time to develop -- or was the generation from the first half of the 80's, those 30-35 in 2015, particularly strong? or maybe the c. 1990 generation is particularly weak?)
// MAGIC 
// MAGIC The lowest average winner's age was 23.9 in 1986, when I was also at my youngest.

// COMMAND ----------

// MAGIC %sql -- We use floor(tourney_date / 10000) to extract the year from our date integers.
// MAGIC select floor(tourney_date / 10000) as year, 
// MAGIC        min(winner_age) as min_age, 
// MAGIC        max(winner_age) as max_age, 
// MAGIC        avg(winner_age) as mean_age 
// MAGIC from atp_matches 
// MAGIC group by floor(tourney_date / 10000)
// MAGIC -- We present the data as a line chart with key year, and the other three columns as values.

// COMMAND ----------

// MAGIC %md
// MAGIC Are there more aces now than in the days of Pistol Pete (Sampras)? There was definitely an increase during the nineties, but average aces per match is still increasing. On average a match winner had around 2.18 more aces per match than a loser. (For comparison, Ivo Karlovic averages, at the time of writing, 19.3 aces per match.)
// MAGIC 
// MAGIC Much of our match data, such as the number of aces per match, has only been recorded since 1990.

// COMMAND ----------

// MAGIC %sql 
// MAGIC select floor(tourney_date / 10000) as year, 
// MAGIC        avg(w_ace) as winner_aces, 
// MAGIC        avg(l_ace) as loser_aces 
// MAGIC from atp_matches 
// MAGIC group by floor(tourney_date / 10000)

// COMMAND ----------

// MAGIC %md
// MAGIC Do left-handers or right-handers have the advantage? In the general population around 13% of men are left-handed, so it would seem that left-handers were underperforming before 2000, but have been holding there own since. Of course the proportion of tennis players that play left handed may differ from the proportion of left-handers in the general public.

// COMMAND ----------

// MAGIC %sql 
// MAGIC select floor(tourney_date / 10000) as year, 
// MAGIC        sum(cast(winner_hand = 'R' as INT)) / (sum(cast(winner_hand = 'R' as INT)) + sum(cast(winner_hand = 'L' as INT))) as RH_percent 
// MAGIC from atp_matches 
// MAGIC group by floor(tourney_date / 10000)

// COMMAND ----------

// MAGIC %md
// MAGIC Was there ever a time when I was tall enough to be a (male) tennis professional? Alas no, even in 1976, which seems to be the best year to be a short tennis player, the average winner was about 11.5cm taller than me. Though Rod Laver, who was a dominant force in tennis at the start of the open era, was only a little taller than me. (And [Renée Richards](https://en.wikipedia.org/wiki/Ren%C3%A9e_Richards) competed on both the mens and womens tours, and I'm definitely taller than Sara Erani, so there's another option. :) 
// MAGIC 
// MAGIC Being tall is clearly an advantage in tennis (except for on all those international flights): in 2015 the winners were on average 4mm taller than losers, while in 2009 the difference was almost a centimeter!

// COMMAND ----------

// MAGIC %sql 
// MAGIC select floor(tourney_date / 10000) as year, 
// MAGIC        avg(winner_ht) as winner_height, 
// MAGIC        avg(loser_ht) as loser_height, 
// MAGIC        min(loser_ht) as min_height, 
// MAGIC        max(loser_ht) as max_height 
// MAGIC from atp_matches 
// MAGIC group by floor(tourney_date / 10000)

// COMMAND ----------

// MAGIC %md
// MAGIC Match lengths don't seem to have changed much since records began in 1990, although something strange happened in 1994 -- perhaps this is a data quality issue.

// COMMAND ----------

// MAGIC %sql 
// MAGIC select floor(tourney_date / 10000) as year, 
// MAGIC        avg(minutes) as mean_length,
// MAGIC        best_of
// MAGIC from atp_matches 
// MAGIC group by best_of, floor(tourney_date / 10000)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's see how impressive were the record breaking exploits of Isner and Mahut (so impressive that this is the only topic that Terry Tao and Tim Gowers have independently written blogs on) -- they played for more than 11 hours over three days, with Isner winning 70-68 in the fith set (without the introduction of tie breaks it might have been even longer!).

// COMMAND ----------

// MAGIC %sql 
// MAGIC select * 
// MAGIC from atp_edges 
// MAGIC where minutes > 180 
// MAGIC order by minutes desc

// COMMAND ----------

// MAGIC %md
// MAGIC Why is mens tennis no longer very popular in the US? Let's look at the success of American men on the ATP tour by year.

// COMMAND ----------

// MAGIC %sql 
// MAGIC select floor(tourney_date / 10000) as year, 
// MAGIC        count(*) as USA_count
// MAGIC from atp_matches 
// MAGIC where winner_ioc == 'USA'
// MAGIC group by floor(tourney_date / 10000)

// COMMAND ----------

// MAGIC %md 
// MAGIC Compare this with the total number of matches below. 
// MAGIC In 1982 American men won 1725 of the 4109 matches played, while in 2015 they won only 200 of 2958.

// COMMAND ----------

// MAGIC %sql 
// MAGIC select count(*), 
// MAGIC        floor(tourney_date / 10000) as year
// MAGIC from atp_matches
// MAGIC group by floor(tourney_date / 10000)

// COMMAND ----------

// MAGIC %md
// MAGIC Let's have a quick look at the Wimbledon seeds through the years.

// COMMAND ----------

// MAGIC %sql 
// MAGIC drop table wimbledon_seeds;
// MAGIC 
// MAGIC -- We build a table with only seeded players at Wimbledon.
// MAGIC -- We restrict to taking players from round of 128 matches to ensure we count each seed exactly once.
// MAGIC create table wimbledon_seeds as
// MAGIC select floor(tourney_date / 10000) as year,
// MAGIC        case
// MAGIC          when winner_seed is not null then winner_seed
// MAGIC          when loser_seed is not null then loser_seed
// MAGIC        end as seed,
// MAGIC        case
// MAGIC          when winner_seed is not null then winner_age
// MAGIC          when loser_seed is not null then loser_age
// MAGIC        end as age,
// MAGIC        case
// MAGIC          when winner_seed is not null then winner_ht
// MAGIC          when loser_seed is not null then loser_ht
// MAGIC        end as height
// MAGIC from atp_matches 
// MAGIC where tourney_name = 'Wimbledon' 
// MAGIC   and round = 'R128' 
// MAGIC   and (winner_seed is not null or loser_seed is not null)

// COMMAND ----------

// MAGIC %md 
// MAGIC Below is the age of top ten seeds, minimums, means, and maximums. Can you spot Federer? 

// COMMAND ----------

// MAGIC %sql 
// MAGIC select year, 
// MAGIC        avg(age) as mean_age, 
// MAGIC        min(age) as min_age, 
// MAGIC        max(age) as max_age 
// MAGIC from wimbledon_seeds 
// MAGIC where seed < 11 
// MAGIC group by year 
// MAGIC order by year

// COMMAND ----------

// MAGIC %md 
// MAGIC (Federer has been the oldest top ten seed since 2011 and was also the youngest top-ten seed in 2002.)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3. Graph building
// MAGIC 
// MAGIC Ok, now it's time to build our two graphs. We start by building the vertices data frame of player data, which will be the same for both graphs.

// COMMAND ----------

// MAGIC %md
// MAGIC ####Vertices
// MAGIC The players table contains also players that have only played in Futures and Challengers tournaments, which are below the ATP and Grand Slam level tournaments that are our focus. So we will filter our table to only include palyers with at least one match in our match data.

// COMMAND ----------

// MAGIC %sql 
// MAGIC drop table atp_players0;
// MAGIC drop table atp_players;
// MAGIC 
// MAGIC create table atp_players0 as
// MAGIC select distinct winner_id as players 
// MAGIC from atp_matches 
// MAGIC      union 
// MAGIC select distinct loser_id as players 
// MAGIC from atp_matches;
// MAGIC      
// MAGIC create table atp_players as
// MAGIC select b.* 
// MAGIC from (select distinct players from atp_players0) a
// MAGIC join atp_players_raw b
// MAGIC on a.players = b.playerID

// COMMAND ----------

// MAGIC %md
// MAGIC We will also add player height to our player table. Just in case a player is still growing ([even into their 30's!](http://www.tennisworldusa.org/At-611-Ivo-Karlovic-is-still-growing-taller-articolo14009.html)), we take the greatest height recorded for any match.

// COMMAND ----------

// MAGIC %sql 
// MAGIC drop table atp_vertices;
// MAGIC 
// MAGIC create table atp_vertices as
// MAGIC select a.*, b.height
// MAGIC from atp_players a
// MAGIC join (select c.playerID, max(d.loser_ht) as height
// MAGIC       from atp_players c
// MAGIC       left join atp_matches d
// MAGIC       on c.playerID = d.loser_id
// MAGIC       group by c.playerID) b
// MAGIC on a.playerID = b.playerID;
// MAGIC      
// MAGIC select * from atp_vertices 

// COMMAND ----------

// MAGIC %md
// MAGIC So how many open era mens tennis players have there been on the ATP?

// COMMAND ----------

// MAGIC %sql 
// MAGIC select count(*) 
// MAGIC from atp_vertices

// COMMAND ----------

// MAGIC %md
// MAGIC #### Match graph edges
// MAGIC 
// MAGIC Before we form the edge set, I need to understand what the tournament levels mean. (We could use these as edge weights, with, for example, wins at a grand slam having more weight.)

// COMMAND ----------

// MAGIC %sql 
// MAGIC -- We look at all tournaments and their levels from 2014
// MAGIC select distinct tourney_name, tourney_level, tourney_date
// MAGIC from atp_matches 
// MAGIC where floor(tourney_date / 10000) = 2014 
// MAGIC order by tourney_name

// COMMAND ----------

// MAGIC %md 
// MAGIC We will give tournaments weights based on the ranking points the winner currently receives, except for Davis cup matches and 'C' tournaments. Since ATP 250 and 500 tournaments are lumped together we'll give them all weights of 500 (though we could use tournament names to separate these).
// MAGIC 
// MAGIC Hmmm, there were no 'C' tournaments in 2014.

// COMMAND ----------

// MAGIC %sql 
// MAGIC -- So we look at all touranments with level 'C'.
// MAGIC select * 
// MAGIC from atp_matches 
// MAGIC where tourney_level = 'C'

// COMMAND ----------

// MAGIC %md
// MAGIC The player rankings seem quite low, but since there's a New Zealand match winner we'll lump these touranments together with the ATP 250 and 500 level tournaments.

// COMMAND ----------

// MAGIC %md
// MAGIC ##### Edges table

// COMMAND ----------

// MAGIC %sql 
// MAGIC drop table atp_edges0;
// MAGIC drop table atp_edges;
// MAGIC 
// MAGIC create table atp_edges0 as
// MAGIC select cast(substring(tourney_id, 6, 3) as int) as tourney_id, tourney_name, surface, 
// MAGIC        case tourney_level      -- here we define the weightings for wins by tournament
// MAGIC            when 'A' then 500   -- ATP 250 and 500 level tournaments are lumped together
// MAGIC            when 'C' then 500
// MAGIC            when 'D' then 500   -- Davis cup matches should probably have a range of weights...
// MAGIC            when 'M' then 1000  -- Masters 1000
// MAGIC            when 'F' then 1500  -- ATP finals
// MAGIC            when 'G' then 2000  -- Grand slams
// MAGIC        end as edge_weight,
// MAGIC        tourney_date, 
// MAGIC        winner_id as dst, 
// MAGIC        loser_id as src, 
// MAGIC        score, best_of, round, minutes
// MAGIC from atp_matches;
// MAGIC 
// MAGIC create table atp_edges as
// MAGIC select a.*, 
// MAGIC        concat(b.forename, b.surname) as dst_name 
// MAGIC from (select c.*, 
// MAGIC              concat(d.forename, d.surname) as src_name 
// MAGIC       from atp_edges0 c 
// MAGIC       join atp_vertices d
// MAGIC       on c.src = d.playerID) a
// MAGIC join atp_vertices b
// MAGIC on a.dst = b.playerID

// COMMAND ----------

// MAGIC %md
// MAGIC We use the player ids, instead of names, for the edge sources and destinations just in case there are ever two players with the same name (which almost happened during a [match in 2015](http://edition.cnn.com/2015/05/18/tennis/joao-sousa-v-joao-souza-geneva-open/)).

// COMMAND ----------

// MAGIC %sql 
// MAGIC -- Just having a look at what we've got:
// MAGIC select src_name, dst_name, tourney_name, surface, edge_weight, 
// MAGIC        tourney_date, score, best_of, round, minutes 
// MAGIC from atp_edges

// COMMAND ----------

// MAGIC %md
// MAGIC We can finally build our ATP match graph.

// COMMAND ----------

// Import SQL functions and the graph frames package.
import org.apache.spark.sql.functions._
import org.graphframes._

// Define vertice and edge data frames...
val vertices = sqlContext.sql("select playerID as id, forename, surname, hand, dob, country, height from atp_vertices").distinct().cache()
val edges = sqlContext.sql("select src, dst, tourney_id, tourney_name, surface, edge_weight, tourney_date, score, best_of, round, minutes from atp_edges").cache()

// and build the graph!
val atpGraph = GraphFrame(vertices, edges)

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Head-to-head graph edges
// MAGIC 
// MAGIC I also want a restricted graph with an arrow from player A to player B only if B leads their head-to-head (or A and B are even).

// COMMAND ----------

// MAGIC %sql 
// MAGIC drop table temp;
// MAGIC drop table atp_h2h;
// MAGIC 
// MAGIC -- We first collpase mulitple directed edges between players to edges labelled with 
// MAGIC -- the total number of wins for the destination.
// MAGIC create table temp as
// MAGIC select count(src, dst) as count, 
// MAGIC        src, dst 
// MAGIC from atp_edges 
// MAGIC group by src, dst;
// MAGIC 
// MAGIC -- We then join this table to itself to give all match data between two players in a 
// MAGIC -- single row. Note each head-to-head occurs in two rows, one with each player as the 
// MAGIC -- sourse (resp. destination).
// MAGIC create table atp_h2h as
// MAGIC select b.count as src_wins, 
// MAGIC        c.count as dst_wins, 
// MAGIC        b.src, b.dst
// MAGIC from temp b
// MAGIC join temp c
// MAGIC on b.src = c.dst
// MAGIC and b.dst = c.src;
// MAGIC 
// MAGIC select * from atp_h2h

// COMMAND ----------

// MAGIC %md
// MAGIC Later on we want to restrict this graph to edges where there have been many matches between players and where one player has a big(ish) lead over the other. To make this easier, our egde attributes will be the total number of matches between the two players (total) and the difference between the number of wins of the two players (diff) -- the destination will always have at least as many wins as the source. 

// COMMAND ----------

// MAGIC %sql
// MAGIC drop table atp_h2h_edges;
// MAGIC 
// MAGIC create table atp_h2h_edges as
// MAGIC select src, dst, src_wins + dst_wins as total, dst_wins - src_wins as diff
// MAGIC from atp_h2h
// MAGIC where src_wins <= dst_wins
// MAGIC      union
// MAGIC select dst as src, src as dst, src_wins + dst_wins as total, src_wins - dst_wins as diff
// MAGIC from atp_h2h
// MAGIC where dst_wins <= src_wins;
// MAGIC 
// MAGIC select * from atp_h2h_edges;

// COMMAND ----------

// MAGIC %md
// MAGIC So here is our head-to-head graph:

// COMMAND ----------

val edgesH2H = sqlContext.sql("select * from atp_h2h_edges").cache()
val atpH2HGraph = GraphFrame(vertices, edgesH2H)

// COMMAND ----------

// MAGIC %md 
// MAGIC ### 4. The matches graph
// MAGIC Before we run our PageRank queries, let's have a quick look at our graph.

// COMMAND ----------

println(s"Players: ${atpGraph.vertices.count()}")
println(s"Matches: ${atpGraph.edges.count()}")

// COMMAND ----------

// MAGIC %md
// MAGIC We have just over 5500 players (we've lost one?) and 160k matches, so on average a player played a little under 60 matches.

// COMMAND ----------

// MAGIC %md
// MAGIC A sanity check... the biggest winners: Jimmy Connors, Ivan Lendl, Roger Federer, Guillermo Vilas, ...

// COMMAND ----------

// Since edges point to the winner, the in-degree gives the number of wins.
val winners = atpGraph.inDegrees.sort($"inDegree".desc).limit(20)  // We take the top 20.

display(winners.join(vertices, winners("id") === vertices("id"))   // Here we join back to the vertices data frame to get names.
               .select("inDegree", "forename", "surname", "dob", "country", "height", "hand"))

// COMMAND ----------

// MAGIC %md 
// MAGIC We seem to have lost a few matches (27 of Jimmy Conners are lost, while we have found some extra wins for Vilas and McEnroe) and picked up a few others! (note that Federer and Nadal, ... have picked up more match wins this year). The differences are relativley small, so we're just going to ignore them for now.

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/List_of_ATP_Tour_players_by_career_match_wins",250))

// COMMAND ----------

// MAGIC %md 
// MAGIC I don't know who has the most match loses. Let's find out:

// COMMAND ----------

val losers = atpGraph.outDegrees
                     .sort($"outDegree".desc)
                     .limit(20)

display(losers.join(vertices, losers("id") === vertices("id"))
              .select("outDegree", "forename", "surname", "dob", "country", "height", "hand"))

// COMMAND ----------

// MAGIC %md
// MAGIC Eagle-eyed viewers may have noticed that Fabrice Santoro also makes Wikipedia's list of biggest winners, in 48th with 470 match wins (Mikhail Youzhny, with 468, is catching up though).

// COMMAND ----------

// MAGIC %md
// MAGIC #### PageRank
// MAGIC The source code for the Spark implementation of PageRank can be found in [GitHub](https://github.com/apache/spark/blob/v1.6.1/graphx/src/main/scala/org/apache/spark/graphx/lib/PageRank.scala).

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/PageRank", 300))

// COMMAND ----------

// MAGIC %md 
// MAGIC For connected graphs PageRank is a generalisation of the [eigenvector centrality](https://en.wikipedia.org/wiki/Centrality#Eigenvector_centrality). For a strongly connected graph, eigenvector centrality, which is so-called beacuase it is given by the values of the dominant eigenvector of the graphs adjacency matrix, is the proportion of times you visit each node in an infinite random walk of the graph, where at each step outgoing edges are weighted equally. In PageRank we are also making a random walk of our graph and calculating the proportion of times we visit each node, the difference is that for each step we make there is a chance (determined by the reset probability) that we will jump to a new randomly chosen node (that may even be the same node). This reset probability is a parameter to be chosen, and a bit surprisingly in some graphs it can have a [huge affect](http://www.sciencedirect.com/science/article/pii/S1570866709000926) on the resultant ranking (note though that for any graph the map from reset probability to PageRank values is continuous). (**Homework**: Try to construct a graph where the reset value makes a big difference in the resulting ranking. How does the variation in ranking depend on the graph size and density?)
// MAGIC 
// MAGIC For large graphs simulating a random walk may be much easier than calculating the eigenvectors of the (very large) adjacency matrix, but this is not an efficient way to calculate PageRank. We describe Apache Sparks iterative message passing algorithm for a strongly connected graph and with reset probability 0.
// MAGIC 
// MAGIC We begin by initialising all PageRank values to 1. For each iteration step, all nodes split their current PageRank value evenly among their outgoing edges passing this value along each outgoing edge. Then each node collects all the values it has been passed and sums them up to get their new PageRank score. Iterations continue until no node's value changes more than the tolerance. More formally, let \\(V\\) be the set of nodes and \\(E\\) the set of edge pairs, and denote the PageRank of \\(i \in V\\) by \\(p_i\\). Avoiding indicating the iteration, to save indices, we have:
// MAGIC 
// MAGIC &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![](https://dl.dropboxusercontent.com/u/61723687/PageRank_formula1.png)]()
// MAGIC 
// MAGIC 
// MAGIC Non-zero reset probabilities are incorporated in the aggregation of passed PageRank values:
// MAGIC 
// MAGIC &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![](https://dl.dropboxusercontent.com/u/61723687/PageRank_formula2.png)]()

// COMMAND ----------

// MAGIC %md 
// MAGIC ##### PageRank on the whole graph
// MAGIC 
// MAGIC Who's the GOAT (Greatest Of All Time)? (Here's a cryptic clue for my opinion: Gave the stuttering queen the goat? (7))
// MAGIC 
// MAGIC PageRank was invented by Larry Page and Sergey Brin, the founders of Google, as a way of ranking websites by their 'link popularity'. The central idea is that a link from one website to another can be seen as an endorsement of the destination site by the source site. In our ATP match graph, each edge is a link from the loser to the winner, so it can likewise be seen as an endorsement of the destination player by the source player. Thus if we run PageRank on this graph players that have won more, better players(?), should have a higher PageRank value...

// COMMAND ----------

val atpPageRank = atpGraph.pageRank
                   .resetProbability(0.15) // We set the reset value to 0.15, which is the standard value.
                   .tol(0.01)              // We set the tolerance to 0.01, so PageRank values should be accurate to around 0.02.
                   .run()

// COMMAND ----------

// We grab the top 20 players by PageRank, in descending order.
val top20 = atpPageRank.vertices
                       .select("id", "pagerank")
                       .sort($"pagerank".desc)
                       .limit(20)

top20.registerTempTable("PageRankTop20")

display(top20.join(vertices, top20("id") === vertices("id"))
             .select("pagerank", "forename", "surname", "dob", "country", "height", "hand"))

// COMMAND ----------

// MAGIC %md 
// MAGIC The Top 20 are undeniably good players, but something is not quite right: PageRank seems to favour players with longer successful careers. So Jimmy Connors comes out on top, while David Ferrer (who is unlucky not to have won a major) beats Andy Murray (who is unlucky to have won only two), and they both beat Bjorn Borg (with 11 major titles) who doesn't make the list (and retired at the age of 26). This is not really surprising since losing a match doesn't (directly) harm a players PageRank (and could even improve it!).

// COMMAND ----------

// MAGIC %md
// MAGIC ##### Plotting the (PageRank) top 20
// MAGIC We're going to borrow some code from [this]("http://opiateforthemass.es/articles/analyzing-golden-state-warriors-passing-network-using-graphframes-in-spark/") awesome post of Yuki Katoh.

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md 
// MAGIC **Homework**: Understand this code and cater it to our graph.

// COMMAND ----------

// MAGIC %md 
// MAGIC One downside of using someone else's code to hack a pretty graph is that we need to reshape our data quite a bit.

// COMMAND ----------

// MAGIC %sql 
// MAGIC drop table top20_vertices;
// MAGIC drop table top20_edges0;
// MAGIC drop table top20_edges;
// MAGIC 
// MAGIC -- We first collect some player info for our top 20.
// MAGIC create table top20_vertices as
// MAGIC select a.pagerank, b.*
// MAGIC from PageRankTop20 a
// MAGIC join (select playerID as id, 
// MAGIC       concat(forename, surname) as name, 
// MAGIC       hand, dob, country, height 
// MAGIC       from atp_vertices) b
// MAGIC on a.id = b.id;
// MAGIC 
// MAGIC -- And then restrict our graph to matches between top 20 players.
// MAGIC create table top20_edges0 as
// MAGIC select a.pagerank, 
// MAGIC        a.name as loser, 
// MAGIC        a.dob as loser_dob, 
// MAGIC        floor(a.dob / 100000) as decade,
// MAGIC        b.src_wins as number, 
// MAGIC        b.dst
// MAGIC from top20_vertices a
// MAGIC join atp_h2h b
// MAGIC on a.id = b.src
// MAGIC where b.dst in (100284, 103819, 100656, 100581, 104745, 
// MAGIC                 104925, 100282, 100119, 101736, 100437, 
// MAGIC                 101222, 101948, 101414, 100074, 100126, 
// MAGIC                 100174, 103970, 100261, 104918, 103720);
// MAGIC 
// MAGIC create table top20_edges as
// MAGIC select a.*, 
// MAGIC        b.name as winner
// MAGIC from top20_edges0 a
// MAGIC join top20_vertices b
// MAGIC on a.dst = b.id

// COMMAND ----------

// MAGIC %sql 
// MAGIC select * 
// MAGIC from top20_edges

// COMMAND ----------

// MAGIC %md 
// MAGIC We use the d3 package -- defined in the cell above the last homewrok and shamelessly taken from Yuki Katoh's blog -- for plotting our top 20 graph.

// COMMAND ----------

import d3._
d3.graphs.help()

// COMMAND ----------

// MAGIC %md 
// MAGIC The node size is determined by PageRank score and the edge width by the total number of matches between pairs. The 2001 match between Federer and Sampras (and others?) does not appear, perhaps because this edge is to thin. The rivalry of the 'Big 4' really stands out (only the Sampras-Agassi and McEnroe-Connors edges are a similar width to those between Federer, Nadal, and Djokovic).

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md
// MAGIC The reset value may make a difference to the PageRank results, so let's try a slightly smaller value.

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md
// MAGIC That looks better, at least it got the top 1 right :P There's also a strong pattern in the handedness of the top eight players!

// COMMAND ----------

// MAGIC %md
// MAGIC ##### What about on clay? It's got to be Rafa, no?
// MAGIC 
// MAGIC We can easily rank players on each surface by applying PageRank on the subgraph with only clay court matches in the edge data frame. We look at who is the clay court king.

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md
// MAGIC Again the number of matches played is having a big influence (Vilas won 659 matches on clay!). At least Bjorn Borg makes it onto this list, though he's still below David Ferrer. It seems that to be a great clay courter you need to play left-handed (of course uncle Toni already knew that).

// COMMAND ----------

// MAGIC %md
// MAGIC ##### PageRank for ranking current players
// MAGIC The ATP ranking system is based on points accrued (dependent on tournament level and how deep a player goes in the tournament) in a moving one-year window. This in many ways is not very satisfying (for example your favourite player could potentially win this fortnights Grand slam, Roland Garros as it happens, and go backwards in the rankings -- well not right now). So we're going to try a 52 week page rank, picking 'at random' all matches from 2006.
// MAGIC 
// MAGIC Looking at a single year, the number of matches should be less significant, since to win many matches players needed to go deep in many tournaments.

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md 
// MAGIC So possibly the greatest tennis player of all time in his best year on tour was only the second best player. For comparison [here](http://www.atpworldtour.com/en/rankings/singles?rankDate=2006-12-25) is the 2006 year end top 100.
// MAGIC 
// MAGIC This oddity is probably due to Nadal winning 4 of the 6 matches between him and Federer in 2006 (Federer had a 92-5 record for the year, the only non-Nadal loss was to Andy Murray).
// MAGIC 
// MAGIC So perhaps PageRank is not the best way to rank tennis players.

// COMMAND ----------

// MAGIC %md 
// MAGIC **Homework**: Allow more control over edge weights in PageRank. So, for example, we can use match importance to weight edges.

// COMMAND ----------

// MAGIC %md 
// MAGIC #### Community detection by label propogation
// MAGIC 
// MAGIC While we're here we'll run label propogation to see if we can find  any ATP communities.

// COMMAND ----------

val communities = atpGraph.labelPropagation
                          .maxIter(10)      // Label propagation need not converge, so we set a maximum number of iterations.
                          .run()

communities.registerTempTable("communities")

communities.select("label")
           .distinct
           .count                           // We have 58 communities.

// COMMAND ----------

// MAGIC %sql 
// MAGIC select count(*), 
// MAGIC        label 
// MAGIC from communities 
// MAGIC group by label

// COMMAND ----------

// MAGIC %md
// MAGIC We have 58 communities, but only four have more than 2 members. Let's look into the smallest of these four, which has 99 members.

// COMMAND ----------

// MAGIC %sql 
// MAGIC select * 
// MAGIC from communities 
// MAGIC where label = 106580

// COMMAND ----------

// MAGIC %md 
// MAGIC I don't know any of these players, but from the country column it seems we've found a group of players that met frequently in tournaments in Asia and the Middle East.
// MAGIC 
// MAGIC **Homework**: Examine the three biggest clusters and give each a catchy name.

// COMMAND ----------

// MAGIC %md 
// MAGIC ##### Big 4 subgraph
// MAGIC 
// MAGIC Finally, let's have a look at the 'Big 4': Federer, Nadal, Djokovic, and Murray.

// COMMAND ----------

// MAGIC %sql 
// MAGIC drop table big4_vertices;
// MAGIC drop table big4_edges;
// MAGIC 
// MAGIC -- We restrict the vertices to the bid four...
// MAGIC create table big4_vertices as
// MAGIC select * 
// MAGIC from atp_vertices
// MAGIC where playerID in (103819, 104745, 104925, 104918);
// MAGIC 
// MAGIC -- and the matches to those between the big 4.
// MAGIC create table big4_edges as
// MAGIC select * 
// MAGIC from atp_edges
// MAGIC where src in (103819, 104745, 104925, 104918)
// MAGIC and dst in (103819, 104745, 104925, 104918);

// COMMAND ----------

val big4_edges    = sqlContext.sql("select * from big4_edges")      // Grab the edges
val big4_vertices = sqlContext.sql("select * from big4_vertices")   // and vertices,
                              .withColumnRenamed("playerID", "id")
                              .distinct()
val big4Graph     = GraphFrame(big4_vertices, big4_edges)           // then build the graph.

// COMMAND ----------

val big4PageRank = big4Graph.pageRank                               // Run PageRank.
                            .resetProbability(0.15)
                            .tol(0.01)
                            .run()

// COMMAND ----------

val big4PageRankVertices = big4PageRank.vertices
                                       .select("id", "pagerank")
big4PageRankVertices.registerTempTable("big4PRV")

display(big4PageRankVertices.join(vertices, big4PageRankVertices("id") === vertices("id"))
                  .select("pagerank", "forename", "surname", "dob", "country", "height", "hand"))

// COMMAND ----------

// MAGIC %md
// MAGIC PageRank has finally given us a good summary of our graph. Of course a four node graph isn't so difficult to interpret.

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5. Motif finding on the head2head graph

// COMMAND ----------

// MAGIC %md 
// MAGIC Our head-to-head graph has the 5554 vertices from before, but only 23920 edges, which gives an upper bound for the number of 'rivalries'.

// COMMAND ----------

println(s"Players: ${atpH2HGraph.vertices.count()}")
println(s"Matches: ${atpH2HGraph.edges.count()}")

// COMMAND ----------

// MAGIC %md
// MAGIC For a brief, wonderful, time in tennis there was no clear winner among the 'Big 3': Nadal would beat Federer, Federer would beat Djokovic, and Djokovic would beat Nadal. Alas injury and age have hampered Nadal and Federer, while Djokovic has been dominant and now holds a winning record over both Nadal and Federer. Are there any interesting and persisting breakdowns of transitivity (if A beats B and B beats C, then A beats C) in the open era? For this exercise interesting and persistant is defined as: the two players have met at least ten times and one players has at least 3 more wins in their head-to-head than the other.

// COMMAND ----------

val edgesH2H_restricted = sqlContext.sql("select * from atp_h2h_edges where total > 9 and diff > 2")   // We restrict to interesting edges, as defined above.
val atpH2Hgraph2 = GraphFrame(vertices, edgesH2H_restricted)

// COMMAND ----------

// MAGIC %md
// MAGIC The simplest breakdown in transitivity is a directed loop with three vertices (a triangle). 

// COMMAND ----------

val transitivityFailure = atpH2Hgraph2.find("(a)-[e]->(b); (b)-[e2]->(c); (c)-[e3]->(a)")  // Setting our triangle motif, and searching.

display(transitivityFailure.filter("a.dob < b.dob")                                        // We restrict to when vertice a is the youngest
                           .filter("a.dob < c.dob")                                        // to avoid repetition.
                           .select("a.forename", "a.surname", "e.total", "e.diff",
                                   "b.forename", "b.surname", "e2.total", "e2.diff",
                                   "c.forename", "c.surname", "e3.total", "e3.diff"))

// COMMAND ----------

// MAGIC %md
// MAGIC We have only three examples! involving only six players. Harold Solomon appears twice with four rivalries, while the rivalry between Brian Gottfried and Eddie Dibbs occurs in two examples. It would seem that Gottfried's dominence of Dibbs (winning 8 times to 5) is an abberation.

// COMMAND ----------

// MAGIC %md
// MAGIC Since our graph is relatively sparse (so not all pairs have played each other), it is possible to have breakdowns of transitivity involving any number of players. 
// MAGIC 
// MAGIC Any breakdown in transitivity will look like a directed loop in our graph. Are there any breakdowns in transitivity between four players?

// COMMAND ----------

val transitivityFailure4 = atpH2Hgraph2.find("(a)-[e]->(b); (b)-[e2]->(c); (c)-[e3]->(d); (d)-[e4]->(a)")
display(transitivityFailure4.filter("a.dob < b.dob").filter("a.dob < c.dob").filter("a.dob < d.dob")
                            .select("a.forename", "a.surname", "e.total", "e.diff",
                                    "b.forename", "b.surname", "e2.total", "e2.diff",
                                    "c.forename", "c.surname", "e3.total", "e3.diff",
                                    "d.forename", "d.surname", "e4.total", "e4.diff"))

// COMMAND ----------

// MAGIC %md
// MAGIC There are two, but we only have one more player, Raul Ramirez, and three more edges from those present in the triangles.
// MAGIC 
// MAGIC **Homework**: Draw the subgraph of all players in our intransitive relationships.

// COMMAND ----------

// MAGIC %md 
// MAGIC ##### A stateful motif
// MAGIC 
// MAGIC With players that have an extensive and fairly even head-to-head record (such as Djokovic and Nadal, which Djokovic leads 26 to 23) there are often periods of dominence for each player. So even though both players have many match wins there may be few 'switch points' where each player won one of two consecutive matches. (Nadal and Djokovic have 17 switch points, with Djokovic having won their last 7 encounters.) 
// MAGIC 
// MAGIC I'm interested in which two players have had the most switch points, but we'll begin by looking for pairs with at least four switch points.

// COMMAND ----------

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

// COMMAND ----------

// MAGIC %md 
// MAGIC Orantes and Ramimez, who we have seen before, have precisely 4 switch points. However they appear many times in our chronologicalChain becuase when constructing a maximal chronological chain each block of consecutive wins by one of the players can be represented by any of the matches in that block. As a result, there are many ways (the product of the block sizes) we can get a chronological chain of length four from Orantes and Ramirez's rivalry. If there are more than four switch points, then we get many more chains from choosing different subsets of the available blocks.

// COMMAND ----------

// MAGIC %md
// MAGIC **Homework**: Can we avoid counting each rivalry, that meets our condition, so many times? This will require considering more than one edge at a time (for example we could always extend our chain with the oldest match satisfying our condition).

// COMMAND ----------

display(chronologicalChain.select("v1", "v2").distinct())

// COMMAND ----------

// MAGIC %md
// MAGIC It is now easy, for us, to increase our motif to capture \\( n\\) switch points. To find the most switch points we need only look at pairs with at least 17, since the Nadal-Djokovic rivalry provides an upper bound.
// MAGIC 
// MAGIC Here's code for \\( n = 17\\) (which I haven't run).

// COMMAND ----------

// We add more alternating matches to the chain,
val chain18 = atpGraph.find("(v1)-[e1]->(v2); (v2)-[e2]->(v1); (v1)-[e3]->(v2); (v2)-[e4]->(v1); (v1)-[e5]->(v2); (v2)-[e6]->(v1); (v1)-[e7]->(v2); (v2)-[e8]->(v1); (v1)-[e9]->(v2); (v2)-[e10]->(v1); (v1)-[e11]->(v2); (v2)-[e12]->(v1); (v1)-[e13]->(v2); (v2)-[e14]->(v1); (v1)-[e15]->(v2); (v2)-[e16]->(v1); (v1)-[e17]->(v2); (v2)-[e18]->(v1)")
// and the new edges to the condition.
val condition18 = Seq("e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "e10", "e11", "e12",
                      "e13", "e14", "e15", "e16", "e17", "e18")
                        .foldLeft(lit(0))((state, e) => After(state, col(e)("tourney_date")))
// Then run as before.
val chronologicalChain18 = chain18.where(condition18 < 100000000)

display(chronologicalChain18.select("v1", "v2").distinct())

// COMMAND ----------

// MAGIC %md 
// MAGIC We might want to filter our graph before doing such a query. We could use the head-to-head data to find players with at least one rivalry with 33 or more matches where each player won at least 16 matches, then filter the match graph to only these players and apply the above query to this new, much smaller, graph.

// COMMAND ----------

// MAGIC %md
// MAGIC #### Some house-keeping
// MAGIC 
// MAGIC So we've had some fun, but now it's time to tidy up and go home. We're going to delete all of our tables.

// COMMAND ----------

sqlContext.tables.show

// COMMAND ----------

// MAGIC %sql
// MAGIC drop table atp_edges;
// MAGIC drop table atp_edges0;
// MAGIC drop table atp_h2h;
// MAGIC drop table atp_h2h_edges;
// MAGIC drop table atp_h2h_vertices;
// MAGIC drop table atp_players;
// MAGIC drop table atp_players0;
// MAGIC drop table atp_vertices;
// MAGIC drop table big4_edges;
// MAGIC drop table big4_vertices;
// MAGIC drop table temp;
// MAGIC drop table top20_edges;
// MAGIC drop table top20_edges0;
// MAGIC drop table top20_vertices;
// MAGIC drop table wimbledon_seeds

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC 
// MAGIC ### Course Project by Matthew Hendtlass 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)