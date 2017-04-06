package edu.usc.cssl.tacit.common.ui.preprocessor;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Spelling2 {

	private final HashMap<String, Integer> nWords = new HashMap<String, Integer>();
	String dict;
	public Spelling2(String file) throws IOException {
		dict=file;
		BufferedReader in = new BufferedReader(new FileReader(new File(file)));
		Pattern p = Pattern.compile("\\w+");
		for(String temp = ""; temp != null; temp = in.readLine()){
			Matcher m = p.matcher(temp.toLowerCase());
			while(m.find()) nWords.put((temp = m.group()), nWords.containsKey(temp) ? nWords.get(temp) + 1 : 1);
		}
		in.close();
	}

	private final ArrayList<String> edits(String word) {
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i < word.length(); ++i) result.add(word.substring(0, i) + word.substring(i+1));
		for(int i=0; i < word.length()-1; ++i) result.add(word.substring(0, i) + word.substring(i+1, i+2) + word.substring(i, i+1) + word.substring(i+2));
		for(int i=0; i < word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i+1));
		for(int i=0; i <= word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
		return result;
	}

	public final String correct(String word) {
		if(nWords.containsKey(word)) return word;
		ArrayList<String> list = edits(word);
		HashMap<Integer, String> candidates = new HashMap<Integer, String>();
		for(String s : list) if(nWords.containsKey(s)) candidates.put(nWords.get(s),s);
		if(candidates.size() > 0) return candidates.get(Collections.max(candidates.keySet()));
		for(String s : list) for(String w : edits(s)) if(nWords.containsKey(w)) candidates.put(nWords.get(w),w);
		return candidates.size() > 0 ? candidates.get(Collections.max(candidates.keySet())) : word;
	}

	public String SpellCorrector(String a) throws IOException {
		//String a = "homs to do are yoe, chirag, mangoes ; Chirag: is definitely& a good # boy$";
		//System.out.println(a);
		String[] arr = a.split("\\b");
		//System.out.println(Arrays.toString(arr));
		List<String> list = new ArrayList<String>();
		
		for(int i = 0; i < arr.length; i += 1)
		{
		     
		     String b = arr[i];
		     if (b.matches("\\w+")){
		    	 String c = new Spelling2(dict).correct(b);
		 		list.add(c);
		     }
		     
		     else {
			     //if(punctuation.contains(b)){
			     list.add(b);
			    	 //System.out.println(b);
		     }
		
		}
		String [] op = list.toArray(new String[list.size()]);
		//System.out.println(" c         "+Arrays.toString(op));
		StringBuilder finalop = new StringBuilder();
		for ( int i = 0; i < op.length ; i++)
		{
			finalop.append(op[i]);
		}
	    System.out.println("Final " + finalop.toString());
	    return finalop.toString();
	}
	




}