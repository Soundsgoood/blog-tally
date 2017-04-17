# blog-tally
Tumblr is a microblogging site in which users may "reblog" a blog post on their own blog, exposing the blog post to the new blog's followers. This helps spread original artwork and ideas across an interconnected web of blogs. Independent of reblogging is the option to "Like" a blog post, which is simply a favorites mechanism. Unfortunately, when browsing your Likes, the name attached to each Like is the last blog to reblog the post before you Liked it, rather than that of the original artist. This is unfortunate if you enjoy a certain art style or theme but haven't had the chance to dig up some names.
This app tallies the number of times a certain Tumblr blog is the **original uploader** (as opposed to reblogger) of one of your Likes.

Created by Orion Guan on February 20th, 2017.

## Requirements
* Requires json-simple-1.1.1.jar library in the directory of BlogTally.java. (included in repository)
* The blog must have "Share Posts You Like" enabled under blog settings.
* You must supply a blog URL and an API key.

## Future Work
* This app may get its own API key.
* Error messages will become more specific to certain API error messages.
* The tally will include information on whether or not you have followed the listed blogs.

