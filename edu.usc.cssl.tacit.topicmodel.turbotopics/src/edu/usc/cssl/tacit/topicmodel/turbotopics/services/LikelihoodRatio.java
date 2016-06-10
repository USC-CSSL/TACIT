package edu.usc.cssl.tacit.topicmodel.turbotopics.services;
import java.util.*;

/**
 * Created by msamak on 3/7/16.
 */
public class LikelihoodRatio {
    Double pvalue;
    Map<Object,Object> perms;
    double perm_hash = 10;
    boolean use_perm;

    public LikelihoodRatio(Double pvalue, boolean use_perm, double perm_hash){
        this(pvalue,use_perm);
        this.perm_hash = perm_hash;
    }

    public LikelihoodRatio(Double pvalue, boolean use_perm){
        perms = new HashMap<Object, Object>();
        this.pvalue = pvalue;
        this.use_perm = use_perm;
    }

    /** reset the permutation cache **/
    public void reset(){
        perms = new HashMap<Object, Object>();
    }

    /** returns log of a value. If value is 0, return -1000000 **/
    private double mylog(double x){
        return (x==0)?-1000000:Math.log(x);
    }

    /**
     * computes the likelihood score.
     * logic for calulating the likelihood score:
     * for each bigram, compute 2 times the log likelihood ratio of
     * modeling it as a bigram versus not modeling it as a bigram
     * @param count : count of root words
     * @param unigram : unigram counts of next word
     * @param bigram: bigram counts of next word
     * @param total : total words
     * @param min_count : threshold of word count
     * @return: Likelihood score for each input bigram
     */
    public Map<Object,Object> score(int count, Map<Object,Object> unigram, Map<Object,Object> bigram, int total, int min_count){
        Map<Object,Object> val = new HashMap<Object,Object>();
        for(Object v: bigram.keySet()){
            int uni = 0;
            if(unigram.containsKey(v)){
                uni = (Integer)unigram.get(v);
            }
            int big = 0;
            if(bigram.containsKey(v)){
                big = (Integer)bigram.get(v);
            }
            if(big < min_count){
                continue;
            }
            assert (uni >= big);
            double log_pi_vu = mylog(big) - mylog(count);
            double log_pi_vnu = mylog(uni - big) - mylog(total - big);
            double log_pi_v_old = mylog(uni) - mylog(total);
            double log_1mp_v = mylog(1 - Math.exp(log_pi_vnu));
            double log_1mp_vu = mylog(1 - Math.exp(log_pi_vu));
            double value = 2 * (big * log_pi_vu + (uni - big) * log_pi_vnu - uni * log_pi_v_old + (count - big) * (log_1mp_vu - log_1mp_v));
            val.put((String)v,value);
        }
        return val;
    }

    /**
     * returns the maximum maximum-score achieved by a permutation
     */
    private Double null_score_perm(int count, Map<Object,Object> marg, int total){
        Integer perm_key = (int)(count/perm_hash);
        if(perms.containsKey(perm_key)){
            return (Double)perms.get(perm_key);
        }
        double max_score = 0;
        int nperm = (int)(1/pvalue);
        ArrayList<Object[]> table = new ArrayList<Object[]>();
        for(Object key: marg.keySet()){
            Object[] items = new Object[2];
            items[0] = key;
            items[1] = marg.get(key);
            table.add(items);
        }
        table.sort(new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                if ((Integer)o1[1] - (Integer)o2[1] == 0)
                    return 0;
                else if (((Integer)o1[1] - (Integer)o2[1]) > 0)
                    return 1;
                else
                    return -1;
            }
        });
        for(int perm=0; perm < nperm; perm++){
            Map<Object,Object> perm_bigram = Turbotopics.sample_no_replace(total,table,count);
            Map<Object,Object> obs= score(count,marg,perm_bigram,total,1);
            Comparator<Object> comp = new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    if((Double)o1 -(Double)o2 == 0)
                        return 0;
                    else if((Double)o1 - (Double)o2 > 0)
                        return 1;
                    else
                        return -1;
                }
            };
            List<Object> obs_val_list = new ArrayList<Object>();
            for (Object obj: obs.values()){
                obs_val_list.add(obj);
            }
            Collections.sort(obs_val_list,comp);
            Double obs_score = (Double)obs_val_list.get(0);
            if(obs_score > max_score || perm == 0){
                max_score = obs_score;
            }
        }
        perms.put(perm_key,max_score);
        return max_score;
    }

    /** returns the chi squared null score */
    private Double null_score_chi_sq(int count, Map<Object,Object>marg, int total){
        return (Double) Turbotopics.get_chi_sq_table().get(pvalue);

    }

    public Double null_score(int count, Map<Object,Object>marg,int total){
        if(this.use_perm){
            return null_score_perm(count,marg,total);
        }else{
            return null_score_chi_sq(count,marg,total);
        }
    }
}