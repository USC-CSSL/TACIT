package edu.usc.pil.nlputils.plugins.CooccurrenceAnalysis.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CooccurrenceAnalysis {

	public static void main(String[] args) {

		calculateCooccurrences(
				"C://Users//carlosg//Desktop//CSSL//svm//testham//",
				"C://Users//carlosg//Desktop//CSSL//Clusering//seed.txt", 6,
				"C://Users//carlosg//Desktop//CSSL//Clusering//");
	}

	public static boolean calculateCooccurrences(String inputDir,
			String seedFile, int windowSize, String outputPath) {
		String currentLine = null;
		Map<String, Integer> seedWords = new HashMap<String, Integer>();
		Queue<String> q = new LinkedList<String>();
		Map<String, Map<String, Integer>> wordMat = new HashMap<String, Map<String, Integer>>();
		List<String> phrase = new ArrayList<String>();

		try {
			String[] seeds = null;
			BufferedReader br = new BufferedReader(new FileReader(new File(
					seedFile)));
			while ((currentLine = br.readLine()) != null) {
				seeds = currentLine.split(" ");
				for (String seed : seeds) {
					if (!seedWords.containsKey(seed)) {
						
						seedWords.put(seed, 1);
					}
				}

			}
			br.close();
			

			File dir = new File(inputDir);
			File[] listOfFiles = dir.listFiles();

			Map<String, Integer> vec = null;
			int i, len, count;
			String first;
			StringBuilder match;
			int size = seedWords.size();
			for (File f : listOfFiles) {
				List<String> words = new ArrayList<String>();
				br = new BufferedReader(new FileReader(f));
				while ((currentLine = br.readLine()) != null) {
					for (String s : currentLine.split(" "))
						if(!s.isEmpty())
						words.add(s);
				}
				System.out.println(words);
				len = count = 0;
				for (String word : words) {
					if(len<windowSize){
						q.add(word);
						if(seedWords.containsKey(word)){
							if(seedWords.get(word) != 0){
								count++;
								seedWords.put(word, 0);
							}
						}
						len++;
					}
					else{
						if(count ==  size){
							match = new StringBuilder();
							for(String s: q)
								match.append(s+' '); 
							phrase.add(match.toString());
						}
						first = q.remove();
						if(seedWords.containsKey(first)){
							if(seedWords.get(first) == 0){
								count--;
								seedWords.put(first, 1);
							}
						}
						q.add(word);
						if(seedWords.containsKey(word)){
							if(seedWords.get(word) != 0){
								count++;
								seedWords.put(word, 0);
							}
						}
					}
					
					if (wordMat.containsKey(word)) {
						vec = wordMat.get(word);
					} else {
						vec = new HashMap<String, Integer>();
					}
					for (String second : words) {
						if (second == word)
							continue;
						if (vec.containsKey(second)) {
							vec.put(second, vec.get(second) + 1);
						} else {
							vec.put(second, 1);
						}
					}
					wordMat.put(word, vec);
				}

			}

			SortedSet<String> keys = new TreeSet<String>(wordMat.keySet());
			System.out.println(keys.size());
			try {
				FileWriter fw = new FileWriter(new File(outputPath	+ "\\word-to-word-matrix.csv"));
				fw.write("start,");
				for (String key : keys) {
					fw.write(key + ",");
				}
				fw.write("\n\n");

				for (String key : keys) {
					fw.write(key + ",");
					vec = wordMat.get(key);
					for (String value : keys) {
						if (vec.containsKey(value)) {
							fw.write(vec.get(value) + ",");
						} else {
							fw.write("0,");
						}
					}
					fw.write("\n");
				}

				fw.close();
			} catch (IOException e) {
				System.out.println("Error writing output to files" + e);
			}
			
			try {
				FileWriter fw = new FileWriter(new File(outputPath	+ "\\phrases.csv"));
				for(String p:phrase)
					fw.write(p+"\n");
				fw.close();
			} catch (IOException e) {
				System.out.println("Error writing output to files" + e);
			}
			
			System.out.println(phrase.size());
			for(String s:phrase){
				System.out.println(s);
			}

			return true;
		} catch (Exception e) {
			System.out.println("Exception occurred in Cooccurrence Analysis "
					+ e);
		}
		return false;
	}
}