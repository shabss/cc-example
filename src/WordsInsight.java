
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
//import java.util.HashMap;
import java.util.TreeMap;
import java.util.HashSet;

public class WordsInsight
{
    //Constants
    public static final int MEDIANS_FLUSH_THRESH = 100000; //make sure this is even number
    public static final int UNIQUE_WORD_COUNT_THRESH = 4096;
    //tweets can max be 140 chars so max 70 words
    public static final int MAX_UNIQUE_WORD_COUNT_BUCKETS = 70;
    public static final boolean DO_PROFILE = false;
    
    //Data structures for medians calculation
    protected ArrayList<Float> medians;
    BufferedWriter mediansFile;
    protected int[] uniqueWordCountBuckets;
    protected int totalTweets;
    
    //Data structures for word frequency per tweet
    protected TreeMap<String, Integer> frequency;
    
    //Misc data structures
    String outDir;
    //debugging, profiling data structures
    protected int[] uniqueWordCountPerTweet;
    int uwcOffset;
    BufferedWriter uwcFile;
    
    public WordsInsight(String outputDir) {
        outDir = outputDir;
        //medians code
        medians = new ArrayList<Float>();
        mediansFile = null;
        uniqueWordCountBuckets = new int[MAX_UNIQUE_WORD_COUNT_BUCKETS]; 
        for (int i = 0; i < uniqueWordCountBuckets.length; i++)  
            uniqueWordCountBuckets[i] = 0;
        totalTweets = 0;

        //word frequency code
        frequency = new TreeMap<String, Integer>();
        
        //debugging, profiling data structures
        if (DO_PROFILE) {
            uniqueWordCountPerTweet = new int[UNIQUE_WORD_COUNT_THRESH];
            uwcOffset = 0;
            uwcFile = null;
        }
    }
    
    public void destruct() throws IOException {
        if (mediansFile != null) {
            mediansFile.close();
            mediansFile = null;
        }
        
        if (uwcFile != null) {
            uwcFile.close();
            uwcFile = null;
        }
    }
        
    public static String[] split(String line) {
        //To do: Try out different ways and measure performance
        return line.split("\\s+");
    }
    
    public void writeToUWCFile() throws IOException {
        if (!DO_PROFILE) {
            return;
        }
        if (uwcFile == null) {
            uwcFile = new BufferedWriter(new FileWriter(outDir + "/uwc.txt"));
        }
        for (int uwc = 0; uwc < uwcOffset; uwc++) {
            uwcFile.write("" + uniqueWordCountPerTweet[uwc] + "\n");
            uwcFile.flush();
        }
        uwcOffset = 0;
    }
    
    public void writeMediansToFile() throws IOException {
        if (mediansFile == null) {
            mediansFile = new BufferedWriter(new FileWriter(outDir + "/ft2.txt"));
        }
        for (int med = 0; med < medians.size(); med++) {
            mediansFile.write("" + medians.get(med) + "\n");
            mediansFile.flush();
        }
        medians.clear();
    }
        
    public int getWordCountForTweet(int tweetNumber) {
        int bucket = 0;
        for (; bucket < uniqueWordCountBuckets.length; bucket++) {
            //System.out.println("getWC:" + tweetNumber + "," + 
            //    bucket + "," + uniqueWordCountBuckets[bucket]);
            tweetNumber -= uniqueWordCountBuckets[bucket];
            if (tweetNumber <= 0) {
                break;
            }
        }
        return bucket;
    }
    
    public  void CalcMedian(String[] words) throws IOException {

        //find number of unique words in this tweet
        HashSet<String> set = new HashSet<String>();
        for (String word: words) set.add(word);
        int unique = set.size();
        
        //add this tweet word count the bucket
        uniqueWordCountBuckets[unique]++;
        totalTweets ++;
        
        //find median
        boolean oddTweets = (totalTweets % 2) == 1; 
        int wc1 = -1, wc2 = -1;
        float medwc;
        medwc = wc1 = getWordCountForTweet((totalTweets / 2) + 1);
        if (!oddTweets) {
            wc2 = getWordCountForTweet(totalTweets / 2);
            medwc = wc1 + wc2;
            medwc /= 2;
        }
        //System.out.println(unique + ":(" + medwc + ",(" + wc1 + "," + wc2 +"))");
        medians.add(new Float(medwc));

        //save on disk IO and flush infrequently
        if (medians.size() >= MEDIANS_FLUSH_THRESH) {
            writeMediansToFile();
        }
        
        if (DO_PROFILE) {
            uniqueWordCountPerTweet[uwcOffset++] = unique;
            if (uwcOffset >= UNIQUE_WORD_COUNT_THRESH) {
                writeToUWCFile();
            }
        }
    }
    
    public void CalcFreq(String[] words) {
        for (String word : words) {
            Integer wf = frequency.get(word);
            if (wf == null) {
                wf = new Integer(1);
                frequency.put(word, wf);
            } else {
                frequency.put(word, new Integer(wf.intValue() + 1));
            }
        }
    }
    
    public void writeToFreqFile() throws IOException
    {
        BufferedWriter freqFile = new BufferedWriter(new FileWriter(outDir + "/ft1.txt"));
        for (String k : frequency.keySet()) {
            freqFile.write(k + " " + frequency.get(k) + "\n");
            //System.out.println(k + "\t\t" + frequency.get(k));
        }
        freqFile.close();
    }
    
    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println("Usage:\n");
            System.out.println("\tjava WordsInsight <input-file> <output-dir>\n");
            System.out.println("Example:\n");
            System.out.println("\tjava WordsInsight ../tweet_input/tweets.txt ../tweet_output/\n");
            return;
        }
        
        String strFile = args[0];
        String outDir = args[1];
        BufferedReader file = null;
        WordsInsight wi = new WordsInsight(outDir);
        
        try {
            file = new BufferedReader(new FileReader(strFile));
            String line = null;
        
            while ((line = file.readLine()) != null) {
                String[] words = split(line);
                //for (String word : words) System.out.print(word); System.out.println("");
                wi.CalcMedian(words);
                wi.CalcFreq(words);
            }
            
            wi.writeToFreqFile();
            wi.writeMediansToFile();
            
            if (DO_PROFILE) {
                wi.writeToUWCFile();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File " + strFile + " not found.");
            System.out.print(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
                wi.destruct();
            } catch (Exception e) {
                System.out.print(e);
            }
        }
    }
}


