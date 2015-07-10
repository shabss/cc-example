#!/usr/bin/env bash

# example of the run script for running the word count

# I'll execute my programs, with the input directory tweet_input and output the files in the directory tweet_output

cd src
mkdir -p ../test_output
javac WordsInsight.java
java WordsInsight ../tweet_input/test_tweets.txt ../test_output profile
diff ../test_output/fts1.txt ../test_output/test1-1.txt
diff ../test_output/fts2.txt ../test_output/test1-2.txt
cd ..




