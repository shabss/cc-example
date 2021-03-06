---
title: "Tweet's Words Analysis"
author: "Shabbir Suterwala"
date: "Friday, July 10, 2015"
output: html_document
---

## Executive Summary
This is a submission of The InsightDataEngineering Coding Challenge. The program written in java is memory efficient and high performing. Twitter feed of 2.5 million tweets was analyzed and design considerations were made based on this analysis. It is calculated that 2.5 million tweets will use about 140 MB of memory. Larger amounts of data will not use much more memory than this.

The program is tested on Oracle JRE 1.7 32 bit on Windows 7,  OpenJDK v1.7 64 bit on CentOS 7 and OpenJDK 1.7 32 bit on Fedora 15.

## The Challenge

This challenge is to implement two features:

1. Calculate the total number of times each word has been tweeted.
2. Calculate the median number of *unique* words per tweet, and update this median as tweets come in. 

For example, suppose the following three tweets come in, one after the other

- is #bigdata finally the answer to end poverty? @lavanyarathnam http://ow.ly/o8gt3  #analytics  
- interview: xia wang, astrazeneca on #bigdata and the promise of effective healthcare #kdn http://ow.ly/ot2uj  
- big data is not just for big business. on how #bigdata is being deployed for small businesses: http://bddy.me/1bzukb3  @cxotodayalerts #smb  

The first feature would produce the following total count for each word:

    #analytics  				1
	#bigdata 					3
	#kdn 						1
	#smb 						1
	@cxotodayalerts 			1
	@lavanyarathnam 			1
	and 						1
	answer  					1
	astrazeneca 				1
	being 						1
	big 						2
	business. 					1 
	businesses: 				1
	data 						1
	deployed 					1
	effective 					1
	end 						1
	finally 					1
	for 						2
	healthcare 					1
	how 						1
	http://bddy.me/1bzukb3  	1
	http://ow.ly/o8gt3 	 		1
	http://ow.ly/ot2uj  		1
	interview: 					1
	is  						3
	just 						1
	not 						1
	of 							1
	on 							2
	poverty? 					1
	promise 					1
	small 						1
	the  						2
	to  						1
	wang,						1
	xia 						1

For the second feature, the number of unique words in each tweet is 11, 14, and 17 (since the words 'is', 'big', and 'for' appear twice in the third tweet).  This means that the set of unique words per tweet is {11} after the first tweet arrives, is {11, 14} after the second tweet arrives, and is {11, 14, 17} after all three tweets arrive.  Thus, the second feature would produce the following output:

	11
	12.5
	14

Recall that the median of a set with an even number of items is the mean of the two middle elements (e.g. the median of {11, 14} is 12.5). In this challenge we have made a few assumptions to make things simpler:

- Each tweet only contains lowercase letters, numbers, and ASCII characters like ':', '@', and '#'.
- A word is defined as anything separated by whitespace. 

Note that the output of the first feature is outputted in order, according to the [ASCII Code](http://www.ascii-code.com).   

## Code and Execution

The code for this challenge is written in Java and is tested on Java v1.7. No special libraries are needed besides the JRE jar files. Please excute ./run.sh file. The program will be compiled and then executed.

## Design

Following performance considerations were made when designing the solution:

1. The total memory required for both features
2. The IO requirements for both features

2.6 million twitter feeds were analyized and 

### Feature 1

We obtained twitter data with following characteristics:

```r
setwd("src/")
tfile <- "../../edu/ds-capstone/Coursera-SwiftKey/final/en_US/en_US.twitter.txt"
(o <- system(paste("wc -lc", tfile), intern=TRUE))
```

```
## [1] "  2360148 167105338 ../../edu/ds-capstone/Coursera-SwiftKey/final/en_US/en_US.twitter.txt"
```

```r
o <- system(paste("java WordsInsight", tfile,
                  "../test_output/ profile", sep=" "), intern=TRUE)
setwd("..")
```

Running the code with profiling enabled on above twitter feed produced the appropiate f1.txt. Analyzing f1.txt yeilds:


```r
library(stringr)
wordFreq <- readLines("test_output/ft1.txt")
wordFreq <- str_match(wordFreq, "(.+) ([0-9]+)")
wordFreq <- as.data.frame(wordFreq[complete.cases(wordFreq), c(2,3)], stringsAsFactors=FALSE)
wordFreq[,2] <- as.integer(wordFreq[,2])

totBytesWords <- sum(nchar(wordFreq[,1]) * wordFreq[,2])
totBytesFreq <- nrow(wordFreq) * 8 #long integers
totalBytes <- totBytesWords + totBytesFreq;
types <- c("Words", "FreqCounts", "Total")
bytes <- c(totBytesWords, totBytesFreq, totalBytes)
bytesMB <- bytes / 10^6

cbind(types, bytes, bytesMB)
```

```
##      types        bytes       bytesMB     
## [1,] "Words"      "134371579" "134.371579"
## [2,] "FreqCounts" "8640696"   "8.640696"  
## [3,] "Total"      "143012275" "143.012275"
```

Thus for feature f1, the total bytes required for typical twitter ascii data would be around 140 MB, which is within range of most machines. This number should be similar for larger data feeds, since the frequency is stored in a dictionary object.

Note that this just computes the memory requirements based on the data. We have not computed program overhead related to buffer copies etc. This is discussed below.

### Feature 2

When run with profiling enabled, the program produces uwc.txt, which contains the unique word count for each tweet. Analyzing this data:


```r
uwc <- read.table("test_output/uwc.txt")
uwc <- uwc[,1]
summary(uwc)
```

```
##    Min. 1st Qu.  Median    Mean 3rd Qu.    Max. 
##    1.00    7.00   12.00   12.21   17.00   37.00
```

```r
buckets <- as.data.frame(table(uwc))
names(buckets) <- c("word.count", "freq")
buckets
```

```
##    word.count   freq
## 1           1   2080
## 2           2  58425
## 3           3 101575
## 4           4 118971
## 5           5 124261
## 6           6 132944
## 7           7 131869
## 8           8 129644
## 9           9 127359
## 10         10 123221
## 11         11 118091
## 12         12 113127
## 13         13 109043
## 14         14 104538
## 15         15 102000
## 16         16  98225
## 17         17  97175
## 18         18  94843
## 19         19  93014
## 20         20  89057
## 21         21  81193
## 22         22  70103
## 23         23  54792
## 24         24  38212
## 25         25  23986
## 26         26  12960
## 27         27   6058
## 28         28   2309
## 29         29    766
## 30         30    230
## 31         31     52
## 32         32     16
## 33         33      7
## 34         34      1
## 35         37      1
```

Given that a tweet can be at most 140 characters the maximum number of unique words in it can 70. If we allow for zero lenght tweets then we need a data strucutre of maximum 71 buckets to represent word count in tweets. 

With this intuition the algorithm to find running median is designed as follows:

1. Create a 71 "buckets" numbered 0-71 (a total of 72). Initialize each bucket's value to zero
2. When a tweet arrives count unique words in it.
3. Find the bucket whose number matches the unique word count for this tweet.
4. Increment the bucket's value.

To find the running median:

1. We calualte the median positon = (total tweets / 2) and 
2. Scan for the bucket that contains the median position
   + Start from bucket#0 and iteratively subtract each bukect's value from median position.
   + Stop when median position <= zero
3. Once we find the bucket, we return its number (not the value)

With the use of bucket datastructure, we dont need to keep the per tweet running median in memory. We therefore flush the medians to disk allowing efficent use of memory.

## Open Issues
Following issues currently exist in the code:

1. Use of imutable objects require memory alloc/dealloc. These are java.lang.Long objects used to store word frequencies
2. Calcualte and improve performance of word frequency data structures
3. Calcualte and improve performance of split function


## Conclusion

This is a submission of The InsightDataEngineering Coding Challenge. The program written in java is memory efficient and high performing. 
