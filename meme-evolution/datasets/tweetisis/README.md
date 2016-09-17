# How ISIS Uses Twitter
## Analyze how ISIS fanboys have been using Twitter since 2015 Paris Attacks
### by Khuram

[Released Under CC0: Public Domain License](https://creativecommons.org/publicdomain/zero/1.0/)

This is from: https://www.kaggle.com/kzaman/how-isis-uses-twitter

### Description

We scraped over 17,000 tweets from 100+ pro-ISIS fanboys from all over the world since the November 2015 Paris Attacks. We are working with content producers and influencers to develop effective counter-messaging measures against violent extremists at home and abroad. In order to maximize our impact, we need assistance in quickly analyzing message frames.

The dataset includes the following:

    Name
    Username
    Description
    Location
    Number of followers at the time the tweet was downloaded
    Number of statuses by the user when the tweet was downloaded
    Date and timestamp of the tweet
    The tweet itself

Based on this data, here are some useful ways of deriving insights and analysis:

    Social Network Cluster Analysis: Who are the major players in the pro-ISIS twitter network? Ideally, we would like this visualized via a cluster network with the biggest influencers scaled larger than smaller influencers.
    Keyword Analysis: Which keywords derived from the name, username, description, location, and tweets were the most commonly used by ISIS fanboys? Examples include: "baqiyah", "dabiq", "wilayat", "amaq"
    Data Categorization of Links: Which websites are pro-ISIS fanboys linking to? Categories include: Mainstream Media, Altermedia, Jihadist Websites, Image Upload, Video Upload,
    Sentiment Analysis: Which clergy do pro-ISIS fanboys quote the most and which ones do they hate the most? Search the tweets for names of prominent clergy and classify the tweet as positive, negative, or neutral and if negative, include the reasons why. Examples of clergy they like the most: "Anwar Awlaki", "Ahmad Jibril", "Ibn Taymiyyah", "Abdul Wahhab". Examples of clergy that they hate the most: "Hamza Yusuf", "Suhaib Webb", "Yaser Qadhi", "Nouman Ali Khan", "Yaqoubi".
    Timeline View: Visualize all the tweets over a timeline and identify peak moments

Further Reading: "ISIS Has a Twitter Strategy and It is Terrifying [Infographic]"

### About Fifth Tribe

Fifth Tribe is a digital agency based out of DC that serves businesses, non-profits, and government agencies. We provide our clients with product development, branding, web/mobile development, and digital marketing services. Our client list includes Oxfam, Ernst and Young, Kaiser Permanente, Aetna Innovation Health, the U.S. Air Force, and the U.S. Peace Corps. Along with Goldman Sachs International and IBM, we serve on the Private Sector Committee of the Board of the Global Community Engagement and Resilience Fund (GCERF), the first global effort to support local, community-level initiatives aimed at strengthening resilience against violent extremism. In December 2014, we won the anti-ISIS "Hedaya Hack" organized by Affinis Labs and hosted at the "Global Countering Violent Extremism (CVE) Expo " in Abu Dhabi. Since then, we've been actively involved in working with the open-source community and community content producers in developing counter-messaging campaigns and tools. 

### Download the csv file
This csv file has been obtained from the tweets.xls.zip file with all strings encapsulated by `"` in order to make reading into Spark easier (due to the end of line characters in the 'tweet` field).

```
wget http://lamastex.org/lmse/mep/fighting-hate/extremist-files/ideology/islamic-state/tweets.csv.tgz
```
