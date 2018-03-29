[SDS-2.2, Scalable Data Science](https://lamastex.github.io/scalable-data-science/sds/2/2/)
===========================================================================================

Archived YouTube video (No Sound - Sorry!) of this live unedited lab-lecture:

[![Archived YouTube video of this live unedited lab-lecture](http://img.youtube.com/vi/cVXjBz4B2q4/0.jpg)](https://www.youtube.com/embed/cVXjBz4B2q4?start=0&end=5910&autoplay=1)

Movie Recommender using Alternating Least Squares
=================================================

Most of the content below is just a scarification of from Module 5 of Anthony Joseph's edX course CS100-1x from the Community Edition of databricks.

What is Collaborarive Filtering?
--------------------------------

<p class="htmlSandbox"><iframe 
 src="https://en.wikipedia.org/wiki/Collaborative_filtering"
 width="95%" height="450"
 sandbox>
  <p>
    <a href="http://spark.apache.org/docs/latest/index.html">
      Fallback link for browsers that, unlikely, don't support frames
    </a>
  </p>
</iframe></p>

SOURCE: Module 5 of AJ's course.

**Collaborative Filtering**
---------------------------

**(watch now 61 seconds - from 18 to 79 seconds)**:

[![AJ's What is Collaborative Filtering](http://img.youtube.com/vi/0HGELVQSHb8/0.jpg)](https://www.youtube.com/watch?v=0HGELVQSHb8)

Let us use MLlib to make personalized movie recommendations.

We are going to use a technique called [collaborative filtering](https://en.wikipedia.org/?title=Collaborative_filtering). Collaborative filtering is a method of making automatic predictions (filtering) about the interests of a user by collecting preferences or taste information from many users (collaborating). The underlying assumption of the collaborative filtering approach is that if a person A has the same opinion as a person B on an issue, A is more likely to have B's opinion on a different issue x than to have the opinion on x of a person chosen randomly. You can read more about collaborative filtering [here](http://recommender-systems.org/collaborative-filtering/).

The image below (from [Wikipedia](https://en.wikipedia.org/?title=Collaborative_filtering)) shows an example of predicting of the user's rating using collaborative filtering. At first, people rate different items (like videos, images, games). After that, the system is making predictions about a user's rating for an item, which the user has not rated yet. These predictions are built upon the existing ratings of other users, who have similar ratings with the active user. For instance, in the image below the system has made a prediction, that the active user will not like the video.

![collaborative filtering](https://courses.edx.org/c4x/BerkeleyX/CS100.1x/asset/Collaborative_filtering.gif)

**Resources:**

-   [mllib](https://spark.apache.org/mllib/)
-   [Wikipedia - collaborative filtering](https://en.wikipedia.org/?title=Collaborative_filtering)
-   [Recommender Systems - collaborative filtering](http://recommender-systems.org/collaborative-filtering/)

For movie recommendations, we start with a matrix whose entries are movie ratings by users (shown in red in the diagram below). Each row represents a user and each column represents a particular movie. Thus the entry $$r_{ij}$$ represents the rating of user $$i$$ for movie $$j$$.

Since not all users have rated all movies, we do not know all of the entries in this matrix, which is precisely why we need collaborative filtering. For each user, we have ratings for only a subset of the movies. With collaborative filtering, *the idea is to approximate the ratings matrix by factorizing it as the product of two matrices*: one that describes properties of each user (shown in green), and one that describes properties of each movie (shown in blue).

![factorization](http://spark-mooc.github.io/web-assets/images/matrix_factorization.png)

We want to select these two matrices such that the error for the users/movie pairs where we know the correct ratings is minimized.
The [Alternating Least Squares](https://bugra.github.io/work/notes/2014-04-19/alternating-least-squares-method-for-collaborative-filtering/) algorithm expands on the [least squares method](https://en.wikiversity.org/wiki/Least-Squares_Method) by:

1.  first randomly filling the users matrix with values and then
2.  optimizing the value of the movies such that the error is minimized.
3.  Then, it holds the movies matrix constant and optimizes the value of the users matrix.

This alternation between which matrix to optimize is the reason for the "alternating" in the name.

This optimization is what's being shown on the right in the image above. Given a fixed set of user factors (i.e., values in the users matrix), we use the known ratings to find the best values for the movie factors using the optimization written at the bottom of the figure. Then we "alternate" and pick the best user factors given fixed movie factors.

For a simple example of what the users and movies matrices might look like, check out the [videos from Lecture 8](https://courses.edx.org/courses/BerkeleyX/CS100.1x/1T2015/courseware/00eb8b17939b4889a41a6d8d2f35db83/3bd3bba368be4102b40780550d3d8da6/) or the [slides from Lecture 8](https://courses.edx.org/c4x/BerkeleyX/CS100.1x/asset/Week4Lec8.pdf) of AJ's Introduction to Data Science course.

See
<http://spark.apache.org/docs/latest/mllib-collaborative-filtering.html#collaborative-filtering>.

``` scala
display(dbutils.fs.ls("/databricks-datasets/cs100/lab4/data-001/")) // The data is already here
```

| path                                                         | name           | size      |
|--------------------------------------------------------------|----------------|-----------|
| dbfs:/databricks-datasets/cs100/lab4/data-001/movies.dat     | movies.dat     | 171308.0  |
| dbfs:/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz | ratings.dat.gz | 2837683.0 |

#### **Import**

Let us import the relevant libraries for `mllib`.

``` scala
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
```

>     import org.apache.spark.mllib.recommendation.ALS
>     import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
>     import org.apache.spark.mllib.recommendation.Rating

#### **Preliminaries**

We read in each of the files and create an RDD consisting of parsed lines.
Each line in the ratings dataset (`ratings.dat.gz`) is formatted as:
`UserID::MovieID::Rating::Timestamp`
Each line in the movies (`movies.dat`) dataset is formatted as:
`MovieID::Title::Genres`
The `Genres` field has the format
`Genres1|Genres2|Genres3|...`
The format of these files is uniform and simple, so we can use `split()`.

Parsing the two files yields two RDDs

-   For each line in the ratings dataset, we create a tuple of (UserID, MovieID, Rating). We drop the timestamp because we do not need it for this exercise.
-   For each line in the movies dataset, we create a tuple of (MovieID, Title). We drop the Genres because we do not need them for this exercise.

``` scala
// take a peek at what's in the rating file
sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line => line.split("::") }.take(5)
```

>     res21: Array[Array[String]] = Array(Array(1, 1193, 5, 978300760), Array(1, 661, 3, 978302109), Array(1, 914, 3, 978301968), Array(1, 3408, 4, 978300275), Array(1, 2355, 5, 978824291))

``` scala
val timedRatingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }
```

>     timedRatingsRDD: org.apache.spark.rdd.RDD[(Long, org.apache.spark.mllib.recommendation.Rating)] = MapPartitionsRDD[50245] at map at <console>:40

We have a look at the first 10 entries in the Ratings RDD to check it's ok

``` scala
timedRatingsRDD.take(10).map(println)
```

>     (0,Rating(1,1193,5.0))
>     (9,Rating(1,661,3.0))
>     (8,Rating(1,914,3.0))
>     (5,Rating(1,3408,4.0))
>     (1,Rating(1,2355,5.0))
>     (8,Rating(1,1197,3.0))
>     (9,Rating(1,1287,5.0))
>     (9,Rating(1,2804,5.0))
>     (8,Rating(1,594,4.0))
>     (8,Rating(1,919,4.0))
>     res22: Array[Unit] = Array((), (), (), (), (), (), (), (), (), ())

The timestamp is unused here so we want to remove it

``` scala
val ratingsRDD = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: Rating(userId, movieId, rating)
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }
```

>     ratingsRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[50251] at map at <console>:40

Now our final ratings RDD looks as follows:

``` scala
ratingsRDD.take(10).map(println)
```

>     Rating(1,1193,5.0)
>     Rating(1,661,3.0)
>     Rating(1,914,3.0)
>     Rating(1,3408,4.0)
>     Rating(1,2355,5.0)
>     Rating(1,1197,3.0)
>     Rating(1,1287,5.0)
>     Rating(1,2804,5.0)
>     Rating(1,594,4.0)
>     Rating(1,919,4.0)
>     res23: Array[Unit] = Array((), (), (), (), (), (), (), (), (), ())

A similar command is used to format the movies. We ignore the genres in this recommender

``` scala
val movies = sc.textFile("/databricks-datasets/cs100/lab4/data-001/movies.dat").map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect.toMap
```

>     movies: scala.collection.immutable.Map[Int,String] = Map(2163 -> Attack of the Killer Tomatoes! (1980), 645 -> Nelly & Monsieur Arnaud (1995), 892 -> Twelfth Night (1996), 69 -> Friday (1995), 2199 -> Phoenix (1998), 3021 -> Funhouse, The (1981), 1322 -> Amityville 1992: It's About Time (1992), 1665 -> Bean (1997), 1036 -> Die Hard (1988), 2822 -> Medicine Man (1992), 2630 -> Besieged (L' Assedio) (1998), 3873 -> Cat Ballou (1965), 1586 -> G.I. Jane (1997), 1501 -> Keys to Tulsa (1997), 2452 -> Gate II: Trespassers, The (1990), 809 -> Fled (1996), 1879 -> Hanging Garden, The (1997), 1337 -> Body Snatcher, The (1945), 1718 -> Stranger in the House (1997), 2094 -> Rocketeer, The (1991), 3944 -> Bootmen (2000), 1411 -> Hamlet (1996), 629 -> Rude (1995), 3883 -> Catfish in Black Bean Sauce (2000), 2612 -> Mildred Pierce (1945), 1024 -> Three Caballeros, The (1945), 365 -> Little Buddha (1993), 2744 -> Otello (1986), 1369 -> I Can't Sleep (J'ai pas sommeil) (1994), 138 -> Neon Bible, The (1995), 2889 -> Mystery, Alaska (1999), 1190 -> Tie Me Up! Tie Me Down! (1990), 1168 -> Bad Moon (1996), 2295 -> Impostors, The (1998), 2306 -> Holy Man (1998), 3053 -> Messenger: The Story of Joan of Arc, The (1999), 3345 -> Charlie, the Lonesome Cougar (1967), 760 -> Stalingrad (1993), 2341 -> Dancing at Lughnasa (1998), 101 -> Bottle Rocket (1996), 2336 -> Elizabeth (1998), 3008 -> Last Night (1998), 2109 -> Jerk, The (1979), 2131 -> Autumn Sonata (H�stsonaten ) (1978), 1454 -> SubUrbia (1997), 2031 -> $1,000,000 Duck (1971), 1633 -> Ulee's Gold (1997), 2778 -> Never Talk to Strangers (1995), 2072 -> 'burbs, The (1989), 3661 -> Puppet Master II (1990), 1767 -> Music From Another Room (1998), 3399 -> Sesame Street Presents Follow That Bird (1985), 1995 -> Poltergeist II: The Other Side (1986), 2263 -> Seventh Sign, The (1988), 3930 -> Creature From the Black Lagoon, The (1954), 479 -> Judgment Night (1993), 3798 -> What Lies Beneath (2000), 1559 -> Next Step, The (1995), 1105 -> Children of the Corn IV: The Gathering (1996), 347 -> Bitter Moon (1992), 3666 -> Retro Puppetmaster (1999), 1729 -> Jackie Brown (1997), 3434 -> Death Wish V: The Face of Death (1994), 3167 -> Carnal Knowledge (1971), 2412 -> Rocky V (1990), 2876 -> Thumbelina (1994), 1237 -> Seventh Seal, The (Sjunde inseglet, Det) (1957), 846 -> Flirt (1995), 909 -> Apartment, The (1960), 2921 -> High Plains Drifter (1972), 3477 -> Empire Records (1995), 3698 -> Running Man, The (1987), 333 -> Tommy Boy (1995), 628 -> Primal Fear (1996), 1031 -> Bedknobs and Broomsticks (1971), 249 -> Immortal Beloved (1994), 2463 -> Ruthless People (1986), 3397 -> Great Muppet Caper, The (1981), 1899 -> Passion in the Desert (1998), 893 -> Mother Night (1996), 1840 -> He Got Game (1998), 3581 -> Human Traffic (1999), 1315 -> Paris Was a Woman (1995), 3863 -> Cell, The (2000), 3830 -> Psycho Beach Party (2000), 2787 -> Cat's Eye (1985), 2595 -> Photographer (Fotoamator) (1998), 518 -> Road to Wellville, The (1994), 1850 -> I Love You, Don't Touch Me! (1998), 2499 -> God Said 'Ha!' (1998), 2427 -> Thin Red Line, The (1998), 2480 -> Dry Cleaning (Nettoyage � sec) (1997), 1083 -> Great Race, The (1965), 962 -> They Made Me a Criminal (1939), 1982 -> Halloween (1978), 468 -> Englishman Who Went Up a Hill, But Came Down a Mountain, The (1995), 2559 -> King and I, The (1999), 3449 -> Good Mother, The (1988), 234 -> Exit to Eden (1994), 2544 -> School of Flesh, The (L' �cole de la chair) (1998), 941 -> Mark of Zorro, The (1940), 3927 -> Fantastic Voyage (1966), 1179 -> Grifters, The (1990), 2331 -> Living Out Loud (1998), 777 -> Pharaoh's Army (1995), 3566 -> Big Kahuna, The (2000), 555 -> True Romance (1993), 666 -> All Things Fair (1996), 1295 -> Unbearable Lightness of Being, The (1988), 1956 -> Ordinary People (1980), 1950 -> In the Heat of the Night (1967), 88 -> Black Sheep (1996), 1549 -> Rough Magic (1995), 2280 -> Clay Pigeons (1998), 1554 -> Pillow Book, The (1995), 1110 -> Bird of Prey (1996), 3172 -> Ulysses (Ulisse) (1954), 1686 -> Red Corner (1997), 481 -> Kalifornia (1993), 352 -> Crooklyn (1994), 2250 -> Men Don't Leave (1990), 2363 -> Godzilla (Gojira) (1954), 1855 -> Krippendorf's Tribe (1998), 3680 -> Decline of Western Civilization Part II: The Metal Years, The (1988), 1200 -> Aliens (1986), 2077 -> Journey of Natty Gann, The (1985), 3534 -> 28 Days (2000), 1750 -> Star Kid (1997), 3185 -> Snow Falling on Cedars (1999), 408 -> 8 Seconds (1994), 977 -> Moonlight Murder (1936), 170 -> Hackers (1995), 3681 -> For a Few Dollars More (1965), 1211 -> Wings of Desire (Der Himmel �ber Berlin) (1987), 523 -> Ruby in Paradise (1993), 1158 -> Here Comes Cookie (1935), 2309 -> Inheritors, The (Die Siebtelbauern) (1998), 2512 -> Ballad of Narayama, The (Narayama Bushiko) (1982), 582 -> Metisse (Caf� au Lait) (1993), 2976 -> Bringing Out the Dead (1999), 762 -> Striptease (1996), 3072 -> Moonstruck (1987), 1924 -> Plan 9 from Outer Space (1958), 1005 -> D3: The Mighty Ducks (1996), 2210 -> Sabotage (1936), 2117 -> Nineteen Eighty-Four (1984), 2940 -> Gilda (1946), 1596 -> Career Girls (1997), 1406 -> C�r�monie, La (1995), 115 -> Happiness Is in the Field (1995), 2104 -> Tex (1982), 3317 -> Wonder Boys (2000), 683 -> Eye of Vichy, The (Oeil de Vichy, L') (1993), 730 -> Low Life, The (1994), 1290 -> Some Kind of Wonderful (1987), 1882 -> Godzilla (1998), 217 -> Babysitter, The (1995), 276 -> Milk Money (1994), 2231 -> Rounders (1998), 2622 -> Midsummer Night's Dream, A (1999), 1068 -> Crossfire (1947), 3858 -> Cecil B. Demented (2000), 2808 -> Universal Soldier (1992), 3905 -> Specials, The (2000), 1522 -> Ripe (1996), 3762 -> Daughter of Dr. Jeckyll (1957), 2644 -> Dracula (1931), 3813 -> Interiors (1978), 2495 -> Fantastic Planet, The (La Plan�te sauvage) (1973), 3230 -> Odessa File, The (1974), 2381 -> Police Academy 4: Citizens on Patrol (1987), 2395 -> Rushmore (1998), 2908 -> Boys Don't Cry (1999), 2062 -> Governess, The (1998), 3549 -> Guys and Dolls (1955), 1443 -> Tickle in the Heart, A (1996), 2776 -> Marcello Mastroianni: I Remember Yes, I Remember (1997), 2659 -> It Came from Hollywood (1982), 3417 -> Crimson Pirate, The (1952), 3509 -> Black and White (1999), 3285 -> Beach, The (2000), 3377 -> Hangmen Also Die (1943), 994 -> Big Night (1996), 2527 -> Westworld (1973), 3040 -> Meatballs (1979), 3153 -> 7th Voyage of Sinbad, The (1958), 2953 -> Home Alone 2: Lost in New York (1992), 2590 -> Hideous Kinky (1998), 1401 -> Ghosts of Mississippi (1996), 3460 -> Hillbillys in a Haunted House (1967), 1422 -> Murder at 1600 (1997), 308 -> Three Colors: White (1994), 2947 -> Goldfinger (1964), 1569 -> My Best Friend's Wedding (1997), 1939 -> Best Years of Our Lives, The (1946), 2248 -> Say Anything... (1989), 3912 -> Beautiful (2000), 3098 -> Natural, The (1984), 741 -> Ghost in the Shell (Kokaku kidotai) (1995), 1073 -> Willy Wonka and the Chocolate Factory (1971), 2671 -> Notting Hill (1999), 1544 -> Lost World: Jurassic Park, The (1997), 2676 -> Instinct (1999), 2014 -> Freaky Friday (1977), 3910 -> Dancer in the Dark (2000), 5 -> Father of the Bride Part II (1995), 1728 -> Winter Guest, The (1997), 3108 -> Fisher King, The (1991), 873 -> Shadow of Angels (Schatten der Engel) (1976), 3012 -> Battling Butler (1926), 1205 -> Transformers: The Movie, The (1986), 449 -> Fear of a Black Hat (1993), 120 -> Race the Sun (1996), 2099 -> Song of the South (1946), 2282 -> Pecker (1998), 247 -> Heavenly Creatures (1994), 1591 -> Spawn (1997), 2114 -> Outsiders, The (1983), 2837 -> Bedrooms & Hallways (1998), 1142 -> Get Over It (1996), 379 -> Timecop (1994), 1269 -> Arsenic and Old Lace (1944), 878 -> Bye-Bye (1995), 440 -> Dave (1993), 655 -> Mutters Courage (1995), 3498 -> Midnight Express (1978), 511 -> Program, The (1993), 2380 -> Police Academy 3: Back in Training (1986), 1971 -> Nightmare on Elm Street 4: The Dream Master, A (1988), 1793 -> Welcome to Woop-Woop (1997), 3402 -> Turtle Diary (1985), 2854 -> Don't Look in the Basement! (1973), 1533 -> Promise, The (La Promesse) (1996), 614 -> Loaded (1994), 1692 -> Alien Escape (1995), 269 -> My Crazy Life (Mi vida loca) (1993), 1305 -> Paris, Texas (1984), 202 -> Total Eclipse (1995), 597 -> Pretty Woman (1990), 1437 -> Cement Garden, The (1993), 1041 -> Secrets & Lies (1996), 861 -> Supercop (1992), 3382 -> Song of Freedom (1936), 1173 -> Cook the Thief His Wife & Her Lover, The (1989), 1486 -> Quiet Room, The (1996), 3848 -> Silent Fall (1994), 1497 -> Double Team (1997), 10 -> GoldenEye (1995), 2195 -> Dirty Work (1998), 1705 -> Guy (1996), 3017 -> Creepshow 2 (1987), 1078 -> Bananas (1971), 1788 -> Men With Guns (1997), 1426 -> Zeus and Roxanne (1997), 3217 -> Star Is Born, A (1937), 3530 -> Smoking/No Smoking (1993), 1671 -> Deceiver (1997), 3794 -> Chuck & Buck (2000), 1608 -> Air Force One (1997), 3439 -> Teenage Mutant Ninja Turtles II: The Secret of the Ooze (1991), 385 -> Man of No Importance, A (1994), 384 -> Bad Company (1995), 56 -> Kids of the Round Table (1995), 1655 -> Phantoms (1998), 3120 -> Distinguished Gentleman, The (1992), 3429 -> Creature Comforts (1990), 3049 -> How I Won the War (1967), 3745 -> Titan A.E. (2000), 1137 -> Hustler White (1996), 2607 -> Get Real (1998), 2686 -> Red Violin, The (Le Violon rouge) (1998), 1756 -> Prophecy II, The (1998), 1310 -> Hype! (1996), 533 -> Shadow, The (1994), 3004 -> Bachelor, The (1999), 2035 -> Blackbeard's Ghost (1968), 3332 -> Legend of Lobo, The (1962), 3367 -> Devil's Brigade, The (1968), 3648 -> Abominable Snowman, The (1957), 3135 -> Great Santini, The (1979), 3942 -> Sorority House Massacre II (1990), 550 -> Threesome (1994), 3649 -> American Gigolo (1980), 3365 -> Searchers, The (1956), 142 -> Shadows (Cienie) (1988), 2740 -> Kindred, The (1986), 2918 -> Ferris Bueller's Day Off (1986), 3044 -> Dead Again (1991), 1735 -> Great Expectations (1998), 1867 -> Tarzan and the Lost City (1998), 500 -> Mrs. Doubtfire (1993), 2184 -> Trouble with Harry, The (1955), 3175 -> Galaxy Quest (1999), 1164 -> Two or Three Things I Know About Her (1966), 1999 -> Exorcist III, The (1990), 797 -> Old Lady Who Walked in the Sea, The (Vieille qui marchait dans la mer, La) (1991), 2316 -> Practical Magic (1998), 3446 -> Funny Bones (1995), 715 -> Horseman on the Roof, The (Hussard sur le toit, Le) (1995), 2448 -> Virus (1999), 3338 -> For All Mankind (1989), 2933 -> Fire Within, The (Le Feu Follet) (1963), 1275 -> Highlander (1986), 2141 -> American Tail, An (1986), 2434 -> Down in the Delta (1998), 1872 -> Go Now (1995), 2712 -> Eyes Wide Shut (1999), 472 -> I'll Do Anything (1994), 3616 -> Loser (2000), 1233 -> Boat, The (Das Boot) (1981), 3781 -> Shaft in Africa (1973), 814 -> Boy Called Hate, A (1995), 1327 -> Amityville Horror, The (1979), 3693 -> Toxic Avenger, The (1985), 2476 -> Heartbreak Ridge (1986), 2627 -> Endurance (1998), 2168 -> Dance with Me (1998), 1260 -> M (1931), 698 -> Delta of Venus (1994), 1919 -> Madeline (1998), 3253 -> Wayne's World (1992), 1988 -> Hello Mary Lou: Prom Night II (1987), 3826 -> Hollow Man (2000), 2459 -> Texas Chainsaw Massacre, The (1974), 3414 -> Love Is a Many-Splendored Thing (1955), 1342 -> Candyman (1992), 3121 -> Hitch-Hiker, The (1953), 747 -> Stupids, The (1996), 913 -> Maltese Falcon, The (1941), 3481 -> High Fidelity (2000), 3562 -> Committed (2000), 1640 -> How to Be a Player (1997), 2580 -> Go (1999), 2901 -> Phantasm (1979), 945 -> Top Hat (1935), 3313 -> Class Reunion (1982), 1063 -> Johns (1996), 1954 -> Rocky (1976), 2844 -> Minus Man, The (1999), 340 -> War, The (1994), 3577 -> Two Moon Juction (1988), 2042 -> D2: The Mighty Ducks (1994), 538 -> Six Degrees of Separation (1993), 1354 -> Breaking the Waves (1996), 153 -> Batman Forever (1995), 2146 -> St. Elmo's Fire (1985), 1507 -> Paradise Road (1997), 1222 -> Full Metal Jacket (1987), 930 -> Notorious (1946), 3895 -> Watcher, The (2000), 3717 -> Gone in 60 Seconds (2000), 2360 -> Celebration, The (Festen) (1998), 1458 -> Touch (1997), 670 -> World of Apu, The (Apur Sansar) (1959), 3276 -> Gun Shy (2000), 2575 -> Dreamlife of Angels, The (La Vie r�v�e des anges) (1998), 829 -> Joe's Apartment (1996), 3370 -> Betrayed (1988), 174 -> Jury Duty (1995), 1095 -> Glengarry Glen Ross (1992), 404 -> Brother Minister: The Assassination of Malcolm X (1994), 3335 -> Jail Bait (1954), 1196 -> Star Wars: Episode V - The Empire Strikes Back (1980), 1746 -> Senseless (1998), 3103 -> Stanley & Iris (1990), 898 -> Philadelphia Story, The (1940), 185 -> Net, The (1995), 1835 -> City of Angels (1998), 3836 -> Kelly's Heroes (1970), 2216 -> Skin Game, The (1931), 3236 -> Zachariah (1971), 3629 -> Gold Rush, The (1925), 2348 -> Sid and Nancy (1986), 3350 -> Raisin in the Sun, A (1961), 1001 -> Associate, The (L'Associe)(1982), 3545 -> Cabaret (1972), 2723 -> Mystery Men (1999), 2972 -> Red Sorghum (Hong Gao Liang) (1987), 3634 -> Seven Days in May (1964), 2046 -> Flight of the Navigator (1986), 3766 -> Missing in Action (1984), 2432 -> Stepmom (1998), 1914 -> Smoke Signals (1998), 2491 -> Simply Irresistible (1999), 1243 -> Rosencrantz and Guildenstern Are Dead (1990), 1127 -> Abyss, The (1989), 2763 -> Thomas Crown Affair, The (1999), 3915 -> Girlfight (2000), 2548 -> Rage: Carrie 2, The (1999), 1782 -> Little City (1998), 3502 -> My Life (1993), 42 -> Dead Presidents (1995), 2985 -> Robocop (1987), 2227 -> Lodger, The (1926), 1391 -> Mars Attacks! (1996), 3249 -> Hand That Rocks the Cradle, The (1992), 782 -> Fan, The (1996), 1441 -> Benny & Joon (1993), 709 -> Oliver & Company (1988), 2020 -> Dangerous Liaisons (1988), 841 -> Eyes Without a Face (1959), 3513 -> Rules of Engagement (2000), 417 -> Barcelona (1994), 24 -> Powder (1995), 973 -> Meet John Doe (1941), 885 -> Bogus (1996), 3088 -> Harvey (1950), 3777 -> Nekromantik (1987), 1046 -> Beautiful Thing (1996), 288 -> Natural Born Killers (1994), 1613 -> Star Maps (1997), 3308 -> Flamingo Kid, The (1984), 2010 -> Metropolis (1926), 1935 -> How Green Was My Valley (1941), 3947 -> Get Carter (1971), 1650 -> Washington Square (1997), 3730 -> Conversation, The (1974), 1645 -> Devil's Advocate, The (1997), 1921 -> Pi (1998), 2886 -> Adventures of Elmo in Grouchland, The (1999), 1359 -> Jingle All the Way (1996), 1601 -> Hoodlum (1997), 1386 -> Terror in a Texas Town (1958), 3281 -> Brandon Teena Story, The (1998), 3466 -> Heart and Souls (1993), 301 -> Picture Bride (1995), 3898 -> Bait (2000), 2082 -> Mighty Ducks, The (1992), 3140 -> Three Ages, The (1923), 1724 -> Full Speed (1996), 1475 -> Kama Sutra: A Tale of Love (1996), 320 -> Suture (1993), 3809 -> What About Bob? (1991), 2173 -> Navigator: A Mediaeval Odyssey, The (1988), 565 -> Cronos (1992), 1366 -> Crucible, The (1996), 2516 -> Children of the Corn III (1994), 2400 -> Prancer (1989), 2067 -> Doctor Zhivago (1965), 2825 -> Rosie (1998), 1529 -> Nowhere (1997), 1967 -> Labyrinth (1986), 2819 -> Three Days of the Condor (1975), 436 -> Color of Night (1994), 3880 -> Ballad of Ramblin' Jack, The (2000), 3878 -> X: The Unknown (1956), 2136 -> Nutty Professor, The (1963), 37 -> Across the Sea of Time (1995), 1904 -> Henry Fool (1997), 1518 -> Breakdown (1997), 1265 -> Groundhog Day (1993), 1703 -> For Richer or Poorer (1997), 2531 -> Battle for the Planet of the Apes (1973), 2405 -> Jewel of the Nile, The (1985), 1228 -> Raging Bull (1980), 1623 -> Wishmaster (1997), 1482 -> Van, The (1996), 3597 -> Whipped (2000), 25 -> Leaving Las Vegas (1995), 1254 -> Treasure of the Sierra Madre, The (1948), 3688 -> Porky's (1981), 2869 -> Separation, The (La S�paration) (1994), 1887 -> Almost Heroes (1998), 651 -> Superweib, Das (1996), 2957 -> Sparrows (1926), 257 -> Just Cause (1995), 3820 -> Thomas and the Magic Railroad (2000), 3891 -> Turn It Up (2000), 2654 -> Wolf Man, The (1941), 389 -> Colonel Chabert, Le (1994), 1628 -> Locusts, The (1997), 3248 -> Sister Act 2: Back in the Habit (1993), 52 -> Mighty Aphrodite (1995), 1055 -> Shadow Conspiracy (1997), 1409 -> Michael (1996), 724 -> Craft, The (1996), 3204 -> Boys from Brazil, The (1978), 3442 -> Band of the Hand (1986), 3263 -> White Men Can't Jump (1992), 1287 -> Ben-Hur (1959), 3221 -> Draughtsman's Contract, The (1982), 14 -> Nixon (1995), 1709 -> Legal Deceit (1997), 2603 -> N� (1998), 2328 -> Vampires (1998), 2916 -> Total Recall (1990), 3938 -> Slumber Party Massacre, The (1982), 3195 -> Tess of the Storm Country (1922), 3029 -> Nighthawks (1981), 570 -> Slingshot, The (K�disbellan ) (1993), 3425 -> Mo' Better Blues (1990), 1792 -> U.S. Marshalls (1998), 1430 -> Underworld (1997), 3644 -> Dark Command (1940), 2735 -> Golden Child, The (1986), 2814 -> Bat, The (1959), 1985 -> Halloween 4: The Return of Michael Myers (1988), 2039 -> Cheetah (1989), 3275 -> Boondock Saints, The (1999), 184 -> Nadja (1994), 2698 -> Zone 39 (1997), 1760 -> Spice World (1997), 3131 -> Broadway Damage (1997), 1660 -> Eve's Bayou (1997), 1298 -> Pink Floyd - The Wall (1982), 3116 -> Miss Julie (1999), 2667 -> Mole People, The (1956), 719 -> Multiplicity (1996), 2339 -> I'll Be Home For Christmas (1998), 785 -> Kingpin (1996), 3046 -> Incredibly True Adventure of Two Girls in Love, The (1995), 3689 -> Porky's II: The Next Day (1983), 3342 -> Birdy (1984), 2269 -> Indecent Proposal (1993), 2755 -> Light of Day (1987), 372 -> Reality Bites (1994), 3495 -> Roadside Prophets (1992), 504 -> No Escape (1994), 1871 -> Friend of the Deceased, A (1997), 3574 -> Carnosaur 3: Primal Species (1996), 3061 -> Holiday Inn (1942), 110 -> Braveheart (1995), 2708 -> Autumn Tale, An (Conte d'automne) (1998), 3821 -> Nutty Professor II: The Klumps (2000), 1907 -> Mulan (1998), 2501 -> October Sky (1999), 3617 -> Road Trip (2000), 1330 -> April Fool's Day (1986), 2444 -> 24 7: Twenty Four Seven (1997), 1860 -> Character (Karakter) (1997), 1264 -> Diva (1981), 2217 -> Elstree Calling (1930), 587 -> Ghost (1990), 1323 -> Amityville 3-D (1983), 3612 -> Slipper and the Rose, The (1976), 3148 -> Cider House Rules, The (1999), 3653 -> Endless Summer, The (1966), 619 -> Ed (1996), 838 -> Emma (1996), 1511 -> A Chef in Love (1996), 2274 -> Lilian's Story (1995), 917 -> Little Princess, The (1939), 702 -> Faces (1968), 751 -> Careful (1992), 3485 -> Autopsy (Macchie Solari) (1975), 802 -> Phenomenon (1996), 125 -> Flirting With Disaster (1996), 344 -> Ace Ventura: Pet Detective (1994), 3776 -> Melody Time (1948), 2682 -> Limbo (1999), 3125 -> End of the Affair, The (1999), 1826 -> Barney's Great Adventure (1998), 3542 -> Coming Apart (1969), 1313 -> Mad Dog Time (1996), 1279 -> Night on Earth (1991), 3410 -> Soft Fruit (1999), 2185 -> I Confess (1953), 1577 -> Mondo (1996), 1455 -> Hotel de Love (1996), 3280 -> Baby, The (1973), 3749 -> Time Regained (Le Temps Retrouv�) (1999), 3845 -> And God Created Woman (Et Dieu&#8230;Cr�a la Femme) (1956), 2562 -> Bandits (1997), 3089 -> Bicycle Thief, The (Ladri di biciclette) (1948), 2488 -> Peeping Tom (1960), 1832 -> Heaven's Burning (1997), 2897 -> And the Ship Sails On (E la nave va) (1984), 934 -> Father of the Bride (1950), 357 -> Four Weddings and a Funeral (1994), 1191 -> Madonna: Truth or Dare (1991), 3744 -> Shaft (2000), 1992 -> Child's Play 2 (1990), 3353 -> Closer You Get, The (2000), 3803 -> Greaser's Palace (1972), 2466 -> Belizaire the Cajun (1986), 2126 -> Snake Eyes (1998), 196 -> Species (1995), 1462 -> Unforgotten: Twenty-Five Years After Willowbrook (1996), 1059 -> William Shakespeare's Romeo and Juliet (1996), 1132 -> Manon of the Spring (Manon des sources) (1986), 3043 -> Meatballs 4 (1992), 3855 -> Affair of Love, An (Une Liaison Pornographique) (1999), 949 -> East of Eden (1955), 2745 -> Mission, The (1986), 2571 -> Matrix, The (1999), 3713 -> Long Walk Home, The (1990), 2356 -> Celebrity (1998), 542 -> Son in Law (1993), 460 -> Getting Even with Dad (1994), 157 -> Canadian Bacon (1994), 2703 -> Broken Vessels (1998), 1545 -> Ponette (1996), 2420 -> Karate Kid, The (1984), 1922 -> Whatever (1998), 2691 -> Legend of 1900, The (Leggenda del pianista sull'oceano) (1998), 902 -> Breakfast at Tiffany's (1961), 559 -> Paris, France (1993), 3099 -> Shampoo (1975), 2713 -> Lake Placid (1999), 3517 -> Bells, The (1926), 638 -> Jack and Sarah (1995), 853 -> Dingo (1992), 3840 -> Pumpkinhead (1988), 1892 -> Perfect Murder, A (1998), 3463 -> Last Resort (1994), 1379 -> Young Guns II (1990), 3374 -> Daughters of the Dust (1992), 3385 -> Volunteers (1985), 2857 -> Yellow Submarine (1968), 2169 -> Dead Man on Campus (1998), 2403 -> First Blood (1982), 3638 -> Moonraker (1979), 1087 -> Madame Butterfly (1995), 1514 -> Temptress Moon (Feng Yue) (1996), 189 -> Reckless (1995), 20 -> Money Train (1995), 1147 -> When We Were Kings (1996), 1247 -> Graduate, The (1967), 2049 -> Happiest Millionaire, The (1967), 2552 -> My Boyfriend's Back (1993), 1319 -> Kids of Survival (1993), 1704 -> Good Will Hunting (1997), 421 -> Black Beauty (1994), 870 -> Gone Fishin' (1997), 1890 -> Little Boy Blue (1997), 2767 -> Illuminata (1998), 1479 -> Saint, The (1997), 46 -> How to Make an American Quilt (1995), 1609 -> 187 (1997), 969 -> African Queen, The (1951), 93 -> Vampire in Brooklyn (1995), 2373 -> Red Sonja (1985), 606 -> Candyman: Farewell to the Flesh (1995), 1347 -> Nightmare on Elm Street, A (1984), 1572 -> Contempt (Le M�pris) (1963), 3919 -> Hellraiser III: Hell on Earth (1992), 1013 -> Parent Trap, The (1961), 3216 -> Vampyros Lesbos (Las Vampiras) (1970), 284 -> New York Cop (1996), 770 -> Costa Brava (1946), 1741 -> Midaq Alley (Callej�n de los milagros, El) (1995), 1398 -> In Love and War (1996), 881 -> First Kid (1996), 3084 -> Home Page (1999), 416 -> Bad Girls (1994), 1115 -> Sleepover (1995), 2635 -> Mummy's Curse, The (1944), 2929 -> Reds (1981), 325 -> National Lampoon's Senior Trip (1995), 3470 -> Dersu Uzala (1974), 3312 -> McCullochs, The (1975), 3902 -> Goya in Bordeaux (Goya en Bodeos) (1999), 3657 -> Pandora and the Flying Dutchman (1951), 1931 -> Mutiny on the Bounty (1935), 152 -> Addiction, The (1995), 1568 -> MURDER and murder (1996), 3734 -> Prince of the City (1981), 3014 -> Bustin' Loose (1981), 2520 -> Airport (1970), 2730 -> Barry Lyndon (1975), 228 -> Destiny Turns on the Radio (1995), 3951 -> Two Family House (2000), 2158 -> Henry: Portrait of a Serial Killer, Part 2 (1996), 2024 -> Rapture, The (1991), 1641 -> Full Monty, The (1997), 3835 -> Crush, The (1993), 3708 -> Firestarter (1984), 289 -> Only You (1994), 1773 -> Tokyo Fist (1995), 448 -> Fearless (1993), 2301 -> History of the World: Part I (1981), 1815 -> Eden (1997), 1494 -> Sixth Man, The (1997), 57 -> Home for the Holidays (1995), 2989 -> For Your Eyes Only (1981), 316 -> Stargate (1994), 1362 -> Garden of Finzi-Contini, The (Giardino dei Finzi-Contini, Il) (1970), 1963 -> Take the Money and Run (1969), 3180 -> Play it to the Bone (1999), 3602 -> G. I. Blues (1960), 3557 -> Jennifer 8 (1992), 78 -> Crossing Guard, The (1995), 1875 -> Clockwatchers (1997), 2852 -> Soldier's Story, A (1984), 3231 -> Saphead, The (1920), 2388 -> Steam: The Turkish Bath (Hamam) (1997), 3866 -> Sunset Strip (2000), 2533 -> Escape from the Planet of the Apes (1971), 1100 -> Days of Thunder (1990), 261 -> Little Women (1994), 1232 -> Stalker (1979), 3268 -> Stop! Or My Mom Will Shoot (1992), 3258 -> Death Becomes Her (1992), 1847 -> Ratchet (1996), 29 -> City of Lost Children, The (1995), 2829 -> Muse, The (1999), 3788 -> Blowup (1966), 2456 -> Fly II, The (1989), 2007 -> Polish Wedding (1998), 2178 -> Frenzy (1972), 2222 -> Champagne (1928), 216 -> Billy Madison (1995), 1489 -> Cats Don't Dance (1997), 2950 -> Blue Lagoon, The (1980), 2237 -> Without Limits (1998), 1415 -> Thieves (Voleurs, Les) (1996), 475 -> In the Name of the Father (1993), 924 -> 2001: A Space Odyssey (1968), 3322 -> 3 Strikes (2000), 2616 -> Dick Tracy (1990), 1946 -> Marty (1955), 492 -> Manhattan Murder Mystery (1993), 164 -> Devil in a Blue Dress (1995), 1465 -> Rosewood (1997), 3524 -> Arthur (1981), 3431 -> Death Wish II (1982), 1302 -> Field of Dreams (1989), 756 -> Carmen Miranda: Bananas Is My Business (1994), 1664 -> N�nette et Boni (1996), 2794 -> European Vacation (1985), 3887 -> Went to Coney Island on a Mission From God... Be Back by Five (1998), 2324 -> Life Is Beautiful (La Vita � bella) (1997), 1714 -> Never Met Picasso (1996), 2027 -> Mafia! (1998), 3199 -> Pal Joey (1957), 789 -> I, Worst of All (Yo, la peor de todas) (1990), 3934 -> Kronos (1957), 2105 -> Tron (1982), 3075 -> Repulsion (1965), 1811 -> Niagara, Niagara (1997), 179 -> Mad Love (1995), 1450 -> Prisoner of the Mountains (Kavkazsky Plennik) (1996), 2090 -> Rescuers, The (1977), 591 -> Tough and Deadly (1995), 3393 -> Date with an Angel (1987), 1373 -> Star Trek V: The Final Frontier (1989), 2996 -> Music of the Heart (1999), 443 -> Endless Summer 2, The (1994), 3724 -> Coming Home (1978), 1911 -> Doctor Dolittle (1998), 3295 -> Raining Stones (1993), 2782 -> Pit and the Pendulum (1961), 1040 -> Secret Agent, The (1996), 1109 -> Charm's Incidents (1996), 2893 -> Plunkett & MaCleane (1999), 1632 -> Smile Like Yours, A (1997), 321 -> Strawberry and Chocolate (Fresa y chocolate) (1993), 2469 -> Peggy Sue Got Married (1986), 3670 -> Story of G.I. Joe, The (1945), 376 -> River Wild, The (1994), 2259 -> Blame It on Rio (1984), 3163 -> Topsy-Turvy (1999), 2650 -> Ghost of Frankenstein, The (1942), 3453 -> Here on Earth (2000), 2451 -> Gate, The (1987), 2236 -> Simon Birch (1998), 623 -> Modern Affair, A (1995), 1334 -> Blob, The (1958), 211 -> Browning Version, The (1994), 3527 -> Predator (1987), 3473 -> Jonah Who Will Be 25 in the Year 2000 (1976), 3702 -> Mad Max (1979), 253 -> Interview with the Vampire (1994), 2880 -> Operation Condor 2 (Longxiong hudi) (1990), 1682 -> Truman Show, The (1998), 485 -> Last Action Hero (1993), 1136 -> Monty Python and the Holy Grail (1974), 834 -> Phat Beach (1996), 1858 -> Mr. Nice Guy (1997), 3438 -> Teenage Mutant Ninja Turtles (1990), 3525 -> Bachelor Party (1984), 1864 -> Sour Grapes (1998), 2122 -> Children of the Corn (1984), 106 -> Nobody Loves Me (Keiner liebt mich) (1994), 238 -> Far From Home: The Adventures of Yellow Dog (1995), 3621 -> Possession (1981), 2925 -> Conformist, The (Il Conformista) (1970), 1978 -> Friday the 13th Part V: A New Beginning (1985), 121 -> Boys of St. Vincent, The (1993), 514 -> Ref, The (1994), 1151 -> Faust (1994), 937 -> Love in the Afternoon (1957), 2291 -> Edward Scissorhands (1990), 1383 -> Adrenalin: Fear the Rush (1996), 1020 -> Cool Runnings (1993), 348 -> Bullets Over Broadway (1994), 3357 -> East-West (Est-ouest) (1999), 574 -> Spanking the Monkey (1994), 2254 -> Choices (1981), 3569 -> Idiots, The (Idioterne) (1998), 1677 -> Critical Care (1997), 2540 -> Corruptor, The (1999), 2034 -> Black Hole, The (1979), 3923 -> Return of the Fly (1959), 1960 -> Last Emperor, The (1987), 2555 -> Baby Geniuses (1999), 3290 -> Soft Toilet Seats (1999), 806 -> American Buffalo (1996), 84 -> Last Summer in the Hamptons (1995), 3300 -> Pitch Black (2000), 3093 -> McCabe & Mrs. Miller (1971), 1581 -> Out to Sea (1997), 353 -> Crow, The (1994), 2335 -> Waterboy, The (1998), 966 -> Walk in the Sun, A (1945), 2777 -> Cobra (1925), 3152 -> Last Picture Show, The (1971), 1183 -> English Patient, The (1996), 1283 -> High Noon (1952), 2073 -> Fandango (1985), 1051 -> Trees Lounge (1996), 3771 -> Golden Voyage of Sinbad, The (1974), 821 -> Crude Oasis, The (1995), 3851 -> I'm the One That I Want (2000), 2017 -> Babes in Toyland (1961), 905 -> It Happened One Night (1934), 480 -> Jurassic Park (1993), 3389 -> Let's Get Harry (1986), 602 -> Great Day in Harlem, A (1994), 2508 -> Breaks, The (1999), 981 -> Dangerous Ground (1997), 766 -> I Shot Andy Warhol (1996), 2964 -> Julien Donkey-Boy (1999), 147 -> Basketball Diaries, The (1995), 1843 -> Slappy and the Stinkers (1998), 2058 -> Negotiator, The (1998), 1169 -> American Dream (1990), 397 -> Fear, The (1995), 687 -> Country Life (1994), 2367 -> King Kong (1976), 2437 -> Wilde (1997), 3537 -> Where the Money Is (2000), 1928 -> Cimarron (1931), 3068 -> Verdict, The (1982), 1377 -> Batman Returns (1992), 280 -> Murder in the First (1995), 2618 -> Castle, The (1997), 2833 -> Lucie Aubrac (1997), 3189 -> My Dog Skip (1999), 3739 -> Trouble in Paradise (1932), 2281 -> Monument Ave. (1998), 2861 -> For Love of the Game (1999), 1215 -> Army of Darkness (1993), 61 -> Eye for an Eye (1996), 634 -> Theodore Rex (1995), 1896 -> Cousin Bette (1998), 877 -> Girls Town (1996), 692 -> Solo (1996), 2190 -> Why Do Fools Fall In Love? (1998), 3421 -> Animal House (1978), 734 -> Getting Away With Murder (1996), 3712 -> Soapdish (1991), 1447 -> Gridlock'd (1997), 2483 -> Day of the Beast, The (El D�a de la bestia) (1995), 293 -> Professional, The (a.k.a. Leon: The Professional) (1994), 956 -> Penny Serenade (1941), 3505 -> No Way Out (1987), 3685 -> Prizzi's Honor (1985), 866 -> Bound (1996), 1886 -> I Got the Hook Up (1998), 3808 -> Two Women (La Ciociara) (1961), 2523 -> Rollercoaster (1977), 1796 -> In God's Hands (1998), 3036 -> Quest for Fire (1981), 2530 -> Beneath the Planet of the Apes (1970), 453 -> For Love or Money (1993), 1119 -> Drunks (1997), 132 -> Jade (1995), 1622 -> Kicked in the Head (1997), 2424 -> You've Got Mail (1998), 774 -> Wend Kuuni (God's Gift) (1982), 2371 -> Fletch (1985), 2979 -> Body Shots (1999), 1394 -> Raising Arizona (1987), 2772 -> Detroit Rock City (1999), 3157 -> Stuart Little (1999), 396 -> Fall Time (1995), 1526 -> Fathers' Day (1997), 3676 -> Eraserhead (1977), 2137 -> Charlotte's Web (1973), 89 -> Nick of Time (1995), 1351 -> Blood & Wine (1997), 133 -> Nueba Yol (1995), 660 -> August (1996), 3361 -> Bull Durham (1988), 2002 -> Lethal Weapon 3 (1992), 998 -> Set It Off (1996), 3756 -> Golden Bowl, The (2000), 3243 -> Encino Man (1992), 2809 -> Love Stinks (1999), 2242 -> Grandview, U.S.A. (1984), 411 -> You So Crazy (1994), 1345 -> Carrie (1976), 988 -> Grace of My Heart (1996), 2799 -> Problem Child 2 (1991), 2286 -> Fiendish Plot of Dr. Fu Manchu, The (1980), 2639 -> Mommie Dearest (1981), 3327 -> Beyond the Mat (2000), 1433 -> Machine, The (1994), 116 -> Anne Frank Remembered (1995), 2103 -> Tall Tale (1994), 2750 -> Radio Days (1987), 243 -> Gordy (1995), 1405 -> Beavis and Butt-head Do America (1996), 3226 -> Hellhounds on My Trail (1999), 1943 -> Greatest Show on Earth, The (1952), 428 -> Bronx Tale, A (1993), 1201 -> Good, The Bad and The Ugly, The (1966), 2804 -> Christmas Story, A (1983), 2965 -> Omega Code, The (1999), 1 -> Toy Story (1995), 1600 -> She's So Lovely (1997), 3870 -> Our Town (1940), 3588 -> King of Marvin Gardens, The (1972), 1104 -> Streetcar Named Desire, A (1951), 1697 -> Big Bang Theory, The (1994), 1242 -> Glory (1989), 265 -> Like Water for Chocolate (Como agua para chocolate) (1992), 3559 -> Limelight (1952), 849 -> Escape from L.A. (1996), 3606 -> On the Town (1949), 3211 -> Cry in the Dark, A (1988), 2392 -> Jack Frost (1998), 2154 -> How Stella Got Her Groove Back (1998), 3906 -> Under Suspicion (2000), 507 -> Perfect World, A (1993), 527 -> Schindler's List (1993), 312 -> Stuart Saves His Family (1995), 74 -> Bed of Roses (1996), 3492 -> Son of the Sheik, The (1926), 2718 -> Drop Dead Gorgeous (1999), 3406 -> Captain Horatio Hornblower (1951), 2205 -> Mr. & Mrs. Smith (1941), 1696 -> Bent (1997), 3184 -> Montana (1998), 206 -> Unzipped (1995), 2586 -> Goodbye, Lover (1999), 2982 -> Guardian, The (1990), 2884 -> Dog Park (1998), 1975 -> Friday the 13th Part 2 (1981), 3510 -> Frequency (2000), 1564 -> Roseanna's Grave (For Roseanna) (1997), 2762 -> Sixth Sense, The (1999), 3378 -> Ogre, The (Der Unhold) (1996), 1863 -> Major League: Back to the Minors (1998), 3435 -> Double Indemnity (1944), 2954 -> Penitentiary (1979), 307 -> Three Colors: Blue (1993), 1651 -> Telling Lies in America (1997), 1604 -> Money Talks (1997), 3899 -> Circus (2000), 2922 -> Hang 'em High (1967), 3151 -> Bat Whispers, The (1930), 2011 -> Back to the Future Part II (1989), 3097 -> Shop Around the Corner, The (1940), 3582 -> Jails, Hospitals & Hip-Hop (2000), 292 -> Outbreak (1995), 3229 -> Another Man's Poison (1952), 3011 -> They Shoot Horses, Don't They? (1969), 1206 -> Clockwork Orange, A (1971), 233 -> Exotica (1994), 452 -> Widows' Peak (1994), 6 -> Heat (1995), 920 -> Gone with the Wind (1939), 248 -> Houseguest (1994), 3857 -> Bless the Child (2000), 60 -> Indian in the Cupboard, The (1995), 380 -> True Lies (1994), 3450 -> Grumpy Old Men (1993), 3503 -> Solaris (Solyaris) (1972), 3459 -> Gothic (1986), 117 -> Young Poisoner's Handbook, The (1995), 512 -> Robert A. Heinlein's The Puppet Masters (1994), 942 -> Laura (1944), 439 -> Dangerous Game (1993), 3591 -> Mr. Mom (1983), 2910 -> Ennui, L' (1998), 3301 -> Whole Nine Yards, The (2000), 2204 -> Saboteur (1942), 3303 -> Black Tar Heroin: The Dark End of the Street (1999), 678 -> Some Folks Call It a Sling Blade (1993), 2473 -> Soul Man (1986), 270 -> Love Affair (1994), 529 -> Searching for Bobby Fischer (1993), 3567 -> Bossa Nova (1999), 2574 -> Out-of-Towners, The (1999), 1438 -> Dante's Peak (1997), 3720 -> Sunshine (1999), 661 -> James and the Giant Peach (1996), 546 -> Super Mario Bros. (1993), 793 -> My Life and Times With Antonin Artaud (En compagnie d'Antonin Artaud) (1993), 3169 -> Falcon and the Snowman, The (1984), 3265 -> Hard-Boiled (Lashou shentan) (1992), 2646 -> House of Dracula (1945), 925 -> Golden Earrings (1947), 3852 -> Tao of Steve, The (2000), 85 -> Angels and Insects (1995), 1306 -> Until the End of the World (Bis ans Ende der Welt) (1991), 201 -> Three Wishes (1995), 3531 -> All the Vermeers in New York (1990))

Let's make a data frame to visually explore the data next.

``` scala
sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line => line.split("::") }.take(5)
```

>     res24: Array[Array[String]] = Array(Array(1, 1193, 5, 978300760), Array(1, 661, 3, 978302109), Array(1, 914, 3, 978301968), Array(1, 3408, 4, 978300275), Array(1, 2355, 5, 978824291))

``` scala
val timedRatingsDF = sc.textFile("/databricks-datasets/cs100/lab4/data-001/ratings.dat.gz").map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong, fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.toDF("timestamp", "userId", "movieId", "rating")
```

>     timedRatingsDF: org.apache.spark.sql.DataFrame = [timestamp: bigint, userId: int ... 2 more fields]

``` scala
display(timedRatingsDF)
```

| timestamp    | userId | movieId | rating |
|--------------|--------|---------|--------|
| 9.7830076e8  | 1.0    | 1193.0  | 5.0    |
| 9.78302109e8 | 1.0    | 661.0   | 3.0    |
| 9.78301968e8 | 1.0    | 914.0   | 3.0    |
| 9.78300275e8 | 1.0    | 3408.0  | 4.0    |
| 9.78824291e8 | 1.0    | 2355.0  | 5.0    |
| 9.78302268e8 | 1.0    | 1197.0  | 3.0    |
| 9.78302039e8 | 1.0    | 1287.0  | 5.0    |
| 9.78300719e8 | 1.0    | 2804.0  | 5.0    |
| 9.78302268e8 | 1.0    | 594.0   | 4.0    |
| 9.78301368e8 | 1.0    | 919.0   | 4.0    |
| 9.78824268e8 | 1.0    | 595.0   | 5.0    |
| 9.78301752e8 | 1.0    | 938.0   | 4.0    |
| 9.78302281e8 | 1.0    | 2398.0  | 4.0    |
| 9.78302124e8 | 1.0    | 2918.0  | 4.0    |
| 9.78301753e8 | 1.0    | 1035.0  | 5.0    |
| 9.78302188e8 | 1.0    | 2791.0  | 4.0    |
| 9.78824268e8 | 1.0    | 2687.0  | 3.0    |
| 9.78301777e8 | 1.0    | 2018.0  | 4.0    |
| 9.78301713e8 | 1.0    | 3105.0  | 5.0    |
| 9.78302039e8 | 1.0    | 2797.0  | 4.0    |
| 9.78302205e8 | 1.0    | 2321.0  | 3.0    |
| 9.7830076e8  | 1.0    | 720.0   | 3.0    |
| 9.78300055e8 | 1.0    | 1270.0  | 5.0    |
| 9.78824195e8 | 1.0    | 527.0   | 5.0    |
| 9.78300103e8 | 1.0    | 2340.0  | 3.0    |
| 9.78824351e8 | 1.0    | 48.0    | 5.0    |
| 9.78301953e8 | 1.0    | 1097.0  | 4.0    |
| 9.78300055e8 | 1.0    | 1721.0  | 4.0    |
| 9.78824139e8 | 1.0    | 1545.0  | 4.0    |
| 9.78824268e8 | 1.0    | 745.0   | 3.0    |

Truncated to 30 rows

Here we simply check the size of the datasets we are using

``` scala
val numRatings = ratingsRDD.count
val numUsers = ratingsRDD.map(_.user).distinct.count
val numMovies = ratingsRDD.map(_.product).distinct.count

println("Got " + numRatings + " ratings from "
        + numUsers + " users on " + numMovies + " movies.")
```

>     Got 487650 ratings from 2999 users on 3615 movies.
>     numRatings: Long = 487650
>     numUsers: Long = 2999
>     numMovies: Long = 3615

Now that we have the dataset we need, let's make a recommender system.

**Creating a Training Set, test Set and Validation Set**

Before we jump into using machine learning, we need to break up the `ratingsRDD` dataset into three pieces:

-   A training set (RDD), which we will use to train models
-   A validation set (RDD), which we will use to choose the best model
-   A test set (RDD), which we will use for our experiments

To randomly split the dataset into the multiple groups, we can use the `randomSplit()` transformation. `randomSplit()` takes a set of splits and seed and returns multiple RDDs.

``` scala
val Array(trainingRDD, validationRDD, testRDD) = ratingsRDD.randomSplit(Array(0.60, 0.20, 0.20), 0L)
```

>     trainingRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[50285] at randomSplit at <console>:44
>     validationRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[50286] at randomSplit at <console>:44
>     testRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.recommendation.Rating] = MapPartitionsRDD[50287] at randomSplit at <console>:44

After splitting the dataset, your training set has about 293,000 entries and the validation and test sets each have about 97,000 entries (the exact number of entries in each dataset varies slightly due to the random nature of the `randomSplit()` transformation.

``` scala
// let's find the exact sizes we have next
println(" training data size = " + trainingRDD.count() +
        ", validation data size = " + validationRDD.count() +
        ", test data size = " + testRDD.count() + ".")
```

>      training data size = 291854, validation data size = 97914, test data size = 97882.

See <http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.recommendation.ALS>.

**(2c) Using ALS.train()**

In this part, we will use the MLlib implementation of Alternating Least Squares, [ALS.train()](https://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.recommendation.ALS). ALS takes a training dataset (RDD) and several parameters that control the model creation process. To determine the best values for the parameters, we will use ALS to train several models, and then we will select the best model and use the parameters from that model in the rest of this lab exercise.

The process we will use for determining the best model is as follows:

-   Pick a set of model parameters. The most important parameter to `ALS.train()` is the *rank*, which is the number of rows in the Users matrix (green in the diagram above) or the number of columns in the Movies matrix (blue in the diagram above). (In general, a lower rank will mean higher error on the training dataset, but a high rank may lead to [overfitting](https://en.wikipedia.org/wiki/Overfitting).) We will train models with ranks of 4, 8, and 12 using the `trainingRDD` dataset.
-   Create a model using `ALS.train(trainingRDD, rank, seed=seed, iterations=iterations, lambda_=regularizationParameter)` with three parameters: an RDD consisting of tuples of the form (UserID, MovieID, rating) used to train the model, an integer rank (4, 8, or 12), a number of iterations to execute (we will use 5 for the `iterations` parameter), and a regularization coefficient (we will use 0.1 for the `regularizationParameter`).
-   For the prediction step, create an input RDD, `validationForPredictRDD`, consisting of (UserID, MovieID) pairs that you extract from `validationRDD`. You will end up with an RDD of the form: `[(1, 1287), (1, 594), (1, 1270)]`
-   Using the model and `validationForPredictRDD`, we can predict rating values by calling [model.predictAll()](https://spark.apache.org/docs/latest/api/python/pyspark.mllib.html#pyspark.mllib.recommendation.MatrixFactorizationModel.predictAll) with the `validationForPredictRDD` dataset, where `model` is the model we generated with ALS.train(). `predictAll` accepts an RDD with each entry in the format (userID, movieID) and outputs an RDD with each entry in the format (userID, movieID, rating).

``` scala
// Build the recommendation model using ALS by fitting to the training data
// using a fixed rank=10, numIterations=10 and regularisation=0.01
val rank = 10
val numIterations = 10
val model = ALS.train(trainingRDD, rank, numIterations, 0.01)
```

>     rank: Int = 10
>     numIterations: Int = 10
>     model: org.apache.spark.mllib.recommendation.MatrixFactorizationModel = org.apache.spark.mllib.recommendation.MatrixFactorizationModel@7f28ff63

``` scala
// Evaluate the model on test data
val usersProductsTest = testRDD.map { case Rating(user, product, rate) =>
  (user, product)
}
```

>     usersProductsTest: org.apache.spark.rdd.RDD[(Int, Int)] = MapPartitionsRDD[50495] at map at <console>:41

``` scala
usersProductsTest.take(10) // Checking
```

>     res28: Array[(Int, Int)] = Array((1,1193), (1,3408), (1,1197), (1,1270), (1,527), (1,1097), (1,1721), (1,3186), (1,2762), (1,1))

``` scala
// get the predictions on test data
val predictions =
  model.predict(usersProductsTest).map { case Rating(user, product, rate) =>
    ((user, product), rate)
  }
```

>     predictions: org.apache.spark.rdd.RDD[((Int, Int), Double)] = MapPartitionsRDD[50504] at map at <console>:44

``` scala
// find the actual ratings and join with predictions
val ratesAndPreds = testRDD.map { case Rating(user, product, rate) =>
  ((user, product), rate)
}.join(predictions)
```

>     ratesAndPreds: org.apache.spark.rdd.RDD[((Int, Int), (Double, Double))] = MapPartitionsRDD[50508] at join at <console>:45

``` scala
ratesAndPreds.take(10).map(println) // print first 10 pairs of (user,product) and (true_rating, predicted_rating)
```

>     ((2326,1371),(5.0,2.8999842970365486))
>     ((1447,1641),(3.0,3.9233266733285976))
>     ((1392,3578),(5.0,3.774613110951701))
>     ((803,1408),(4.0,3.470061553574756))
>     ((1545,1210),(3.0,2.8606966680123334))
>     ((1179,2944),(3.0,3.7859474849439234))
>     ((319,342),(3.0,3.234144990467005))
>     ((1010,2719),(1.0,0.5235917604065505))
>     ((611,2028),(4.0,4.214341830226695))
>     ((412,1127),(5.0,4.483818055974711))
>     res29: Array[Unit] = Array((), (), (), (), (), (), (), (), (), ())

Let's evaluate the model using Mean Squared Error metric.

``` scala
val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
  val err = (r1 - r2)
  err * err
}.mean()
println("Mean Squared Error = " + MSE)
```

>     Mean Squared Error = 0.9726067242384565
>     MSE: Double = 0.9726067242384565

Can we improve the MSE by changing one of the hyper parameters?

``` scala
// Build the recommendation model using ALS by fitting to the validation data
// just trying three different hyper-parameter (rank) values to optimise over
val ranks = List(4, 8, 12); 
var rank=0;
for ( rank <- ranks ){
  val numIterations = 10
  val regularizationParameter = 0.01
  val model = ALS.train(trainingRDD, rank, numIterations, regularizationParameter)

  // Evaluate the model on test data
  val usersProductsValidate = validationRDD.map { case Rating(user, product, rate) =>
                                              (user, product)
  }

  // get the predictions on test data
  val predictions = model.predict(usersProductsValidate)
                         .map { case Rating(user, product, rate)
                                     => ((user, product), rate)
    }

  // find the actual ratings and join with predictions
  val ratesAndPreds = validationRDD.map { case Rating(user, product, rate) 
                                     => ((user, product), rate)
                                   }.join(predictions)
  

  val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
    val err = (r1 - r2)
    err * err
  }.mean()
  
  println("rank and Mean Squared Error = " +  rank + " and " + MSE)
} // end of loop over ranks
```

>     rank and Mean Squared Error = 4 and 0.8420886227872475
>     rank and Mean Squared Error = 8 and 0.9278106413100932
>     rank and Mean Squared Error = 12 and 1.0065537220437444
>     ranks: List[Int] = List(4, 8, 12)
>     rank: Int = 0

Now let us try to apply this to the test data and find the MSE for the best model.

``` scala
  val rank = 4
  val numIterations = 10
  val regularizationParameter = 0.01
  val model = ALS.train(trainingRDD, rank, numIterations, regularizationParameter)

  // Evaluate the model on test data
  val usersProductsTest = testRDD.map { case Rating(user, product, rate) =>
                                              (user, product)
  }

  // get the predictions on test data
  val predictions = model.predict(usersProductsTest)
                         .map { case Rating(user, product, rate)
                                     => ((user, product), rate)
    }

  // find the actual ratings and join with predictions
  val ratesAndPreds = testRDD.map { case Rating(user, product, rate) 
                                     => ((user, product), rate)
                                   }.join(predictions)

  val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
    val err = (r1 - r2)
    err * err
  }.mean()
  
  println("rank and Mean Squared Error for test data = " +  rank + " and " + MSE)
```

>     rank and Mean Squared Error for test data = 4 and 0.8361243043966726
>     rank: Int = 4
>     numIterations: Int = 10
>     regularizationParameter: Double = 0.01
>     model: org.apache.spark.mllib.recommendation.MatrixFactorizationModel = org.apache.spark.mllib.recommendation.MatrixFactorizationModel@7d68dd5c
>     usersProductsTest: org.apache.spark.rdd.RDD[(Int, Int)] = MapPartitionsRDD[46124] at map at <console>:57
>     predictions: org.apache.spark.rdd.RDD[((Int, Int), Double)] = MapPartitionsRDD[46133] at map at <console>:63
>     ratesAndPreds: org.apache.spark.rdd.RDD[((Int, Int), (Double, Double))] = MapPartitionsRDD[46137] at join at <console>:70
>     MSE: Double = 0.8361243043966726

\*\* Potential flaws of CF \*\*

-   Cold start for users and items
-   Gray sheep: [https://en.wikipedia.org/wiki/Collaborative*filtering\#Gray*sheep](https://en.wikipedia.org/wiki/Collaborative_filtering#Gray_sheep)
-   Shilling attacks: [https://en.wikipedia.org/wiki/Collaborative*filtering\#Shilling*attacks](https://en.wikipedia.org/wiki/Collaborative_filtering#Shilling_attacks)
-   Positive feedback problems (rich-get-richer effect): [https://en.wikipedia.org/wiki/Collaborative*filtering\#Diversity*and*the*long\_tail](https://en.wikipedia.org/wiki/Collaborative_filtering#Diversity_and_the_long_tail)

\*\* Areas to improve upon \*\*

-   Works in theory but didn't manage to produce a system that takes user info and outputs suggestions
-   More complete models would include analysing the genres to give better recommendations.
-   For first time users, the program could give the top rated movies over all users.
-   Could have used bigger dataset