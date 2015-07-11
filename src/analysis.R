

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
