Followed tips here:

[https://github.com/mmistakes/minimal-mistakes/issues/585](https://github.com/mmistakes/minimal-mistakes/issues/585)

Specifically:

```
There's no Jekyll magic here, favicons are loaded like you would for any other site (e.g. placing favicon.ico in your site's root).

On the theme demo site I used http://realfavicongenerator.net/ to generate all of the sized icons from a single file, placed them in assets/images and then added the html it spit out to the _includes/head/custom.html

There's a bunch of ways to approach it, which you can search around for if the favicon generator I linked to above doesn't meet your needs.
```

After gernerating the favicon with background color set to white, etc. Did the following as instructed:

```
Extract this package in <web site>/assets/images/favicons/. If your site is http://www.example.com, you should be able to access a file named http://www.example.com/assets/images/favicons/favicon.ico.


Insert the following code in the <head> section of your pages:
<link rel="apple-touch-icon" sizes="180x180" href="/assets/images/favicons/apple-touch-icon.png">
<link rel="icon" type="image/png" sizes="32x32" href="/assets/images/favicons/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="16x16" href="/assets/images/favicons/favicon-16x16.png">
<link rel="manifest" href="/assets/images/favicons/manifest.json">
<link rel="mask-icon" href="/assets/images/favicons/safari-pinned-tab.svg" color="#798487">
<link rel="shortcut icon" href="/assets/images/favicons/favicon.ico">
<meta name="msapplication-config" content="/assets/images/favicons/browserconfig.xml">
<meta name="theme-color" content="#ffffff">

```
