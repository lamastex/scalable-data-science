<div class="cell markdown">

ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

</div>

<div class="cell markdown">

Conclusion
==========

We identified 6 clusters of tweets. Interestingly the most common emoticon 😂 was placed in the not happy cluster. This emoji is supposed to mean "laughing with tears of joy". However it seems it oftentimes appear in tweets together with unhappy looking emojis. Perhaps it is often used to indicate a sense of humorous despair. Considering this one should be a bit skeptical about using our emoji clusters to estimate sentiment of individual tweets, since our model just assigns each emojis one of six meanings (or clusters).

By plotting the collected data as cartograms/choropleth maps across different time intervals we were able to visualize change in tweet frequency around the holidays. In particular we noticed an increase in happy tweets around Christmas and New Year's Eve, in many countries. So although our simple model has some issues it does seem to work to some extent.

We also identified a spike in twitter activity in Saudi Arabia on December 28th, which we speculate may have been caused by the jail sentence given to the activist Loujain al-Hathloul. To tell if this is the case it would be necessary to analyze the particular tweets and hashtags from Saudi Arabia in our dataset.

Mapping the sentiment clusters showed that in particular african countries had a large proportion of unhappy tweets. It also showed that the US, tweets significantly less happy tweets than other "western" countries, which may be related to the recent divisive political campaigns. However, both cases might also be caused by regional differences in use of for example the crying while laughing emoji.

We were probably a bit too cautious when only collecting data for three minutes per hour. We were afraid of spending too many of the courses resources. However, in the end both the size and processing time of our dataset was modest, so we could easily have been collecting data continuously, which would have allowed us to generate animations at a better time resolution. Due to the same concerns regarding computational resources we only collected data for around 10 days. This unfortunately meant that we didn't record the storming of the US congress or Donald Trumps ban from twitter, which would have been interesting to study. But we did learn that you can never have too much data 😂.

</div>
