#!/usr/bin/env bash

# example of the run script for running the word count

# I'll execute my programs, with the input directory tweet_input and output the files in the directory tweet_output

cd src
javac WordsInsight.java
java WordsInsight ../tweet_input/tweets.txt ../tweet_output
cd ..






