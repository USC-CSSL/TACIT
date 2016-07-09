package edu.usc.cssl.tacit.topicmodel.turbotopics.services;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by msamak on 3/7/16.
 */
public class Turbotopics {
    private static Map<String,String> stop_words = null;
    /**
     * @return: Returns a chi_sq_table filled with default values
     */
    public static Map<Object,Object> get_chi_sq_table(){
        Map<Object,Object> _chi_sq_table = new HashMap<Object,Object>();
        _chi_sq_table.put(0.1,2.70554345);
        _chi_sq_table.put(0.01,6.634897);
        _chi_sq_table.put(0.001,10.82757);
        _chi_sq_table.put(0.0001,15.13671);
        _chi_sq_table.put(0.00001,19.51142);
        _chi_sq_table.put(0.000001,23.92813);
        _chi_sq_table.put(0.0000001,28.37399);
        return _chi_sq_table;
    }

    /**
     * @param doc : A document on line
     * @param vocab: A map of vocabulary {'new':{'york':{}},'long':{'island': {'city':{}, 'railroad':{}}}})
     * @return A list of words in that document. note: n-grams are matched from left to right and longest
     */
    public static String[] word_list(String doc, Map<Object,Object> vocab){
        doc = strip_text(doc);
        String[] singles = doc.split(" ");
        ArrayList<String> words = new ArrayList<String>();
        int pos = 0;
        while(pos < singles.length){
            String w = singles[pos];
            pos++;
            String word = w;
            if(!vocab.containsKey(w)){
                vocab.put(w,new HashMap<Object,Object>());
            }
            Object state = vocab.get(w);
            while((pos < singles.length) && ( state instanceof Map) && (((Map<Object,Object>) state).containsKey(singles[pos]))){
                state = ((Map<Object,Object>)state).get(singles[pos]);
                word = word + " " +singles[pos];
                pos = pos + 1;
            }
            words.add(word);
        }
        String[] result = new String[words.size()];
        return words.toArray(result);
    }

    /**
     * strips out all non alphabetic characters from a string,
     * lower cases it, and removes extra whitespace characters.
     * @param text - unstripped text
     * @return - stripped text
     */
    public static String strip_text(String text){
        text = text.toLowerCase();
        text = text.replaceAll("_"," ");
        text = text.replaceAll("[^A-Za-z0-9 ]","");
        text = text.replaceAll("\\s+"," ");
        text = text.trim();
        return text;
    }

    /**
     * sample without replacement from a list of items and counts
     * @param total: Max number for sampling
     * @param table: List of item,count pairs
     * @param nitems: Number of items required in the sample
     * @return: A map of word,count pairs
     */
    public static Map<Object,Object> sample_no_replace(int total, ArrayList<Object[]>table, int nitems){
        Map<Object,Object> count = new HashMap<Object,Object>();
        for(Integer n: getSample(total,nitems)){
            String w = nth_item_from_table(n,table);
            if(!count.containsKey(w)){
                count.put(w,0);
            }
            count.put(w,(Integer)count.get(w)+1);
        }
        return count;
    }

    /**
     * Sampling method
     * @param range
     * @param n
     * @return : A random sample of n integers between 0 to range
     */
    private static ArrayList<Integer> getSample(int range, int n){
        Set<Integer> randomNumbers = new HashSet<Integer>();
        Random random = new Random();
        while(randomNumbers.size() != n){
            randomNumbers.add(random.nextInt(range));
        }
        return new ArrayList<Integer>(randomNumbers);
    }

    private static String nth_item_from_table(int n, ArrayList<Object[]>table){
        double sum = 0;
        for(Object[] item: table){
            sum = sum + (Integer)item[1];
            if(n < sum){
                return (String)item[0];
            }
        }
        assert (false);
        return null;
    }

    /**
     * Get stop words as a map of words.
     * Return an empty map if there is not file for stop words
     * @return
     */
    public static Map<String,String> getStopWords(){
        try {
            if (stop_words == null) {
                stop_words = new HashMap<String,String>();
                BufferedReader br = new BufferedReader(new FileReader("/Users/msamak/work/TACIT_TEMP/src/main/java/turbotopics/stop_words"));
                String line = br.readLine();
                while (line != null) {
                    stop_words.put(line.trim(), "");
                    line = br.readLine();
                }
            }
        }catch(Exception e){
            return new HashMap<String,String>();
        }
        return stop_words;
    }

    /**
     * Writes a file with terms and its counts from a given term,count map
     * @param v: An map of terms and its counts
     * @param outname: Output file name
     * @param incl_stop: Decision as to include the stop words or not
     * @throws Exception
     */
    public static void write_vocab(Map<Object,Object> v, String outname, Boolean incl_stop, int topic) throws Exception {
        //PrintWriter pw = new PrintWriter(outname);\
    	FileWriter fw = new FileWriter(new File(outname), true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("----- Topic "+topic+" -----");
        bw.newLine();
        bw.newLine();

        bw.write("WORD \t WORD COUNT");
        bw.newLine();
        bw.newLine();
        ArrayList<Object> items =  items(v);
        Collections.sort(items, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Object[] a1 = (Object[])o1;
                Object[] a2 = (Object[])o2;
                if ((Integer)a1[1] - (Integer)a2[1] == 0)
                    return 0;
                else if (((Integer)a1[1] - (Integer)a2[1]) < 0)
                    return 1;
                else
                    return -1;
            }
        });
        Map<String,String> stop_words = getStopWords();
        for(Object item: items){
            Object[] itemArray = (Object[])item;
            if(incl_stop || stop_words == null || !stop_words.containsKey((String)itemArray[0])){
                bw.write(itemArray[0]+"\t"+(int)Float.parseFloat(itemArray[1].toString())+"\n");
            }
        }
        bw.newLine();
        bw.newLine();
        bw.close();
        fw.close();
    }

    public static void write_vocab(Map<Object,Object> v, String outname, int topic)throws Exception{
        write_vocab(v,outname,false, topic);
    }

    /**
     *finds nested significant bigrams.
     *Given a iterator list which contains the required arguments.
     *and a function to update the counts based on that iterator
     * @param iter_generator: The iterator list is a list of objects.So the item in the iterator
     * can be any object and the nested_sig_bigrams method doesn't have to know about it
     * @param update_fun: A function to update counts based on the iterator
     * @param sig_test: An object of LikelihoodRatio.
     * @param min: minimum count
     * @return An the Counts object in which the significant bigrams are stored
     */
    public static Counts nested_sig_bigrams(ArrayList<Object[]>iter_generator, TestingBiconsumer<Counts,Object[]> update_fun, LikelihoodRatio sig_test, Integer min){
        System.out.println("computing initial counts\n");
        Counts counts = new Counts();
        ArrayList<String> terms = new ArrayList<String>();
        int ccc =0;
        for(Object[] doc: iter_generator){
            update_fun.accept(counts,doc);
            ccc++;
        }
        ArrayList<Object> items = items(counts.marg);
        terms = getTerms(items,min);
        while(terms.size() > 0){
            Map<Object,Object> new_vocab = new HashMap<Object,Object>();
            sig_test.reset();
            System.out.println("analysing "+terms.size()+" terms");
            for(String v: terms){
                Map<Object,Object> sig_bigrams = counts.sig_bigrams(v, sig_test, min);
                new_vocab.putAll(sig_bigrams);
            }

            for(Object selected : new_vocab.keySet()) {
                System.out.println("bigram : "+selected);
                update_vocab((String)selected, counts.vocab);
            }
            counts.reset_counts();
            for(Object[] doc: iter_generator){
                update_fun.accept(counts,doc);
            }
            items = items(new_vocab);
            Collections.sort(items,new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    Object[] a1 = (Object[])o1;
                    Object[] a2 = (Object[])o2;
                    if ((Integer)a1[1] - (Integer)a2[1] == 0)
                        return 0;
                    else if (((Integer)a1[1] - (Integer)a2[1]) > 0)
                        return 1;
                    else
                        return -1;
                }
            });
            terms = new ArrayList<String>();
            for(Object item: items){
                Object[] arr = (Object[])item;
                if((Integer)arr[1] >= min) {
                    terms.add((String) arr[0]);
                }
            }
        }
        return counts;
    }

    /**
     * Returns list of only those terms whose occurences is >= min
     * @param items: An object list of items where each item is an list of 2 objects.
     *             1st object is the term, 2nd object is the count of that term
     * @param min: treshold of term count
     * @return - A filtered list of terms whose count >= min
     */
    private static ArrayList<String> getTerms(ArrayList<Object> items, int min){
        ArrayList<String> terms = new ArrayList<String>();
        Collections.sort(items, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Object[]a1 = (Object[])o1;
                Object[]a2 = (Object[])o2;
                Integer v1 = (Integer) a1[1];
                Integer v2 = (Integer)a2[1];
                if(v1-v2== 0)
                    return 0;
                else if(v1-v2 > 0)
                    return 1;
                else
                    return -1;
            }
        });
        for(Object item: items){
            Object[] it = (Object[]) item;
            if((Integer)it[1] >= min){
                terms.add((String)it[0]);
            }
        }
        return terms;
    }

    /**
     * Makes a deep copy of the given map.
     */
    public static Object deepCopy(Object obj){
        if(!(obj instanceof Map)){
            return obj;
        }
        Map<Object,Object> map = (Map<Object,Object>)obj;
        Map<Object,Object> newMap = new HashMap<Object,Object>();
        for(Map.Entry<Object,Object> pair: map.entrySet()){
            newMap.put(pair.getKey(),deepCopy(pair.getValue()));
        }
        return newMap;
    }

    /**
     * updates a vocabulary transition with an n-gram
     * @param word: An n-gram string
     * @param vocab: Existing Vocabulary
     */
    private static void update_vocab(String word, Map<Object,Object>vocab){
        String[] words = word.split(" ");
        Map<Object,Object> mach = vocab;
        int i = 0;
        while (i < words.length){
            String w = words[i];
            if (!mach.containsKey(w)){
                mach.put(w,new HashMap<Object,Object>());
            }
            mach = (Map<Object,Object>)mach.get(w);
            i = i + 1;
        }
    }

    /**
     * Converts a map of object to object -> array list of objects. Each object is an array of 2 objects.
     * 1st object is the key, 2nd object is the value
     * @param map
     * @return
     */
    public static ArrayList<Object> items(Map<Object,Object>map){
        ArrayList<Object> items = new ArrayList<Object>();
        for(Map.Entry<Object,Object> pair: map.entrySet()){
            Object[] comps = new Object[2];
            comps[0] = pair.getKey();
            comps[1] = pair.getValue();
            items.add(comps);
        }
        return items;
    }


}