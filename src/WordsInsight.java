
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
    
    //Data structures for medians calculation
    protected ArrayList<Float> medians;
    BufferedWriter mediansFile;
    protected int[] uniqueWordCountBuckets;
    protected int totalTweets;
    
    //Data structures for word frequency per tweet
    protected TreeMap<String, Integer> frequency;
    
    //Misc data structures
    String outDir;      //output directory
    boolean doProfile;  //flag to generate extra data for perf/profiling
    
    //debugging, profiling data structures
    protected int[] uniqueWordCountPerTweet;
    int uwcOffset;
    BufferedWriter uwcFile;
    
    public WordsInsight(String outputDir, boolean profile) {
        doProfile = profile;
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
        if (doProfile) {
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
        String[] splits = line.trim().split("\\s+");
        
        //for (String spl : splits) System.out.print("'" + spl + "', ");
        //System.out.println("");
        
        //Empty lines will have splits=[""]. return null in this case
        if ((splits.length == 1) && (splits[0].equals(""))) {
            splits = null;
        }
        
        return splits;
    }
    
    public void writeToUWCFile() throws IOException {
        if (!doProfile) {
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

        float medwc = 0;
        int unique = 0;
        
        if (words != null) { //if line was not empty
            //find number of unique words in this tweet
            HashSet<String> set = new HashSet<String>();
            for (String word: words) set.add(word);
            unique = set.size();
        }   
        //add this tweet word count the bucket
        uniqueWordCountBuckets[unique]++;

        totalTweets ++;        
        //find median
        boolean oddTweets = (totalTweets % 2) == 1; 
        int wc1 = -1, wc2 = -1;
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
        
        if (doProfile) {
            uniqueWordCountPerTweet[uwcOffset++] = unique;
            if (uwcOffset >= UNIQUE_WORD_COUNT_THRESH) {
                writeToUWCFile();
            }
        }
    }
    
    public void CalcFreq(String[] words) {
        if (words == null) {
            return;
        }
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
    
    public void writeToFreqFile() throws IOException {
        BufferedWriter freqFile = new BufferedWriter(new FileWriter(outDir + "/ft1.txt"));
        for (String k : frequency.keySet()) {
            freqFile.write(k + " " + frequency.get(k) + "\n");
            //System.out.println(k + "\t\t" + frequency.get(k));
        }
        freqFile.close();
    }
    
    public void writeResults() throws IOException {
        writeToFreqFile();
        writeMediansToFile();
        if (doProfile) {
            writeToUWCFile();
        }
    }
        
    public static void main(String args[]) {
        
        //To Do: Improve parameter parsing
        if (args.length < 2) {
            System.out.println("Usage:\n");
            System.out.println("\tjava WordsInsight <input-file> <output-dir>\n");
            System.out.println("Example:\n");
            System.out.println("\tjava WordsInsight ../tweet_input/tweets.txt ../tweet_output/\n");
            return;
        }
        
        String strFile = args[0];
        String outDir = args[1];
        //To Do: anything in 3rd argument means do profile. Improve it
        boolean profile = args.length == 3; 
        
        BufferedReader file = null;
        WordsInsight wi = new WordsInsight(outDir, profile);
        
        try {
            file = new BufferedReader(new FileReader(strFile));
            String line = null;
        
            while ((line = file.readLine()) != null) {
                String[] words = split(line);
                //for (String word : words) System.out.print(word); System.out.println("");
                wi.CalcMedian(words);
                wi.CalcFreq(words);
            }
            
            wi.writeResults();            
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


