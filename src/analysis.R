
library(stringr)
wordFreq <- readLines("test_output/ft1.txt")
wordFreq <- str_match(wordFreq, "(.+) ([0-9]+)")
nrow(wordFreq); sum(complete.cases(wordFreq));head(wordFreq)
wordFreq <- as.data.frame(wordFreq[complete.cases(wordFreq), c(2,3)], stringsAsFactors=FALSE)
wordFreq[,2] <- as.integer(wordFreq[,2])
nrow(wordFreq); sum(complete.cases(wordFreq));head(wordFreq)

totBytes <- sum(nchar(wordFreq[,1]) * wordFreq[,2])
totBytes / 10^6

uwc <- read.table("../test_output/uwc.txt")
uwc <- uwc[,1]
summary(uwc)
uwc.orig <- uwc
buckets <- as.data.frame(table(uwc.orig))
names(buckets) <- c("word.count", "freq")
buckets
uwc <- uwc.orig[1:10000]

uwc.sorted <- c()
uwc.medians <- sapply(1:length(uwc), function(x) {
    uwc.sorted <<- sort(c(uwc[x], uwc.sorted))
    #print (cat("At:", x, ":", uwc[x], ":", uwc.sorted))
    med <- (x %/% 2) + 1
    med <- if (x %% 2 == 0) c(med - 1, med) else c(med, med)
    median(uwc.sorted[med])
})

cbind(uwc, uwc.medians)
