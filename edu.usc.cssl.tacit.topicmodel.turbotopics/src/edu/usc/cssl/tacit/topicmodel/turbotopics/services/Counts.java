package edu.usc.cssl.tacit.topicmodel.turbotopics.services;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by msamak on 3/6/16.
 */
public class Counts {
    Map<Object,Object> vocab; //vocabulary with multi token terms like "new york"
    Map<Object,Object> marg; //marginal counts of words
    Map<Object,Object> next_marg; //marginal next word counts
    Map<Object,Object> bigram; //Bigram counts

    public Counts(){
        vocab = new HashMap<Object,Object>();
        reset_counts();
    }

    /**
     *  update the bigram and marginal counts with a line of text.
     * takes two filter functions and does not count words or next
     * words if they do not pass through the filter.  (e.g., the
     * filter can be used to remove stop words.)
     * @param doc - A line of string which is considered as a document
     * @param root_filter - A function of that returns true or false for a given string
     * @param next_filter - A function of that returns true or false for a given string
     */
    public void update_counts(String doc, Function<String,Boolean>root_filter, Function<String,Boolean>next_filter){
        String[] words = Turbotopics.word_list(doc,vocab);
        for(int pos = 0; pos < words.length; pos++){
            String w = words[pos];
            if(root_filter != null && !root_filter.apply(w)){
                continue;
            }
            if(!marg.containsKey(w)){
                marg.put(w,0);
            }
            marg.put(w,(Integer)marg.get(w)+1);
            if(pos == words.length - 1){
                break;
            }
            String w_next = words[pos+1];
            if(next_filter != null && !next_filter.apply(w_next)){
                continue;
            }
            if(!bigram.containsKey(w)){
                bigram.put(w,new HashMap<Object,Object>());
            }
            Map<Object,Object> bigram_w = (Map<Object,Object>)bigram.get(w);
            if(!bigram_w.containsKey(w_next)){
                bigram_w.put(w_next,0);
            }
            bigram_w.put(w_next,(Integer)bigram_w.get(w_next)+1);
            if(!next_marg.containsKey(w_next)){
                next_marg.put(w_next,0);
            }
            next_marg.put(w_next,(Integer)next_marg.get(w_next)+1);
        }
    }

    /** Same as above method but with 1 filters */
    public void update_counts(String doc, Function<String,Boolean>root_filter){
        update_counts(doc,root_filter,null);
    }

    /** Same as above method but with no filters **/
    public void update_counts(String doc){
        update_counts(doc,null,null);
    }

    public void reset_counts(){
        marg = new HashMap<Object,Object>();
        next_marg = new HashMap<Object,Object>();
        bigram = new HashMap<Object,Object>();
    }


    public Map<Object,Object> sig_bigrams(String word, LikelihoodRatio sig_test, int min){
        return sig_bigrams(word,sig_test,min,true);
    }

    /**
     * compute significant bigrams from a word.
     * requires a significance tester object which has the following:
     *  - score : (next_marg, next_bigram) -> [words->reals]
     *  - null_score : (next_marg, next_bigram, pvalue) -> null_value
     * @param word : input word
     * @param sig_test : The significance tester object which is an object of Likelihood ratio
     * @param min : min count
     * @return: A bigram object
     */
    public Map<Object,Object> sig_bigrams(String word, LikelihoodRatio sig_test, int min, boolean recursive){
        if(!bigram.containsKey(word)){
            return new HashMap<Object,Object>();
        }
        Map<Object,Object> marg = (Map<Object,Object>) Turbotopics.deepCopy(this.next_marg);
        Map<Object,Object> bigram_w = new HashMap<Object,Object>();
        if(this.bigram.containsKey(word)){
            bigram_w = (Map<Object,Object>) Turbotopics.deepCopy(this.bigram.get(word));
        }
        Integer marg_w =0;
        for(Object val: bigram_w.values()){
            marg_w = marg_w + (Integer)val;
        }
        Integer total = 0;
        for(Object val: marg.values()){
            total = total + (Integer)val;
        }
        Map<Object,Object> selected = new HashMap<Object,Object>();
        Map<Object,Object> scores = sig_test.score(marg_w,marg,bigram_w,total,min);
        ArrayList<Object>items = Turbotopics.items(scores);
        items.sort(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Object[]a1 = (Object[])o1;
                Object[]a2 = (Object[])o2;
                Double v1 = (Double)a1[1];
                Double v2 = (Double)a2[1];
                if(v1-v2== 0)
                    return 0;
                else if(v1-v2 > 0)
                    return 1;
                else
                    return -1;
            }
        });
        ArrayList<Object[]> filteredScores = new ArrayList<Object[]>();
        for(Object item: items){
            Object[] arr = (Object[])item;
            if((Double)arr[1] > 0){
                filteredScores.add(arr);
            }
        }
        for(Object[] s: filteredScores){
            String cand = (String)s[0];
            Double max_score = (Double)s[1];
            if((Integer)bigram_w.get(cand) < min){
                continue;
            }
            Double null_score = sig_test.null_score(marg_w, marg, total);
            System.out.println(word+" "+cand+": marg = ["+marg_w+", "+marg.get(cand)+"]; bigram = "+bigram_w.get(cand)+";");
            System.out.println("val = "+max_score+"; null = "+null_score);
            if(max_score <= null_score){
                System.out.println("rejected");
            }else{
                String new_word = word+ " "+ cand;
                selected.put(new_word,bigram_w.get(cand));
                System.out.println("selected *");
            }
            if(recursive){
                marg_w = marg_w - (Integer)bigram_w.get(cand);
                total = total - (Integer)bigram_w.get(cand);
                bigram_w.remove(cand);
            }
        }
        return selected;
    }


}