package edu.usc.cssl.nlputils.plugins.CooccurrenceAnalysis.process;

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

import org.eclipse.swt.custom.Bullet;

public class CooccurrenceAnalysis {
	static String delimeters = " .,;\"!-()\\[\\]{}:?'/\\`~$%#@&*_=+<>";
	static boolean doPhrases = false;

	public static void main(String[] args) {

		calculateCooccurrences(
				"C://Users//carlosg//Desktop//CSSL//svm//testham//",
				"C://Users//carlosg//Desktop//CSSL//Clusering//seed.txt", 6,
				"C://Users//carlosg//Desktop//CSSL//Clusering//", 1, true);
	}

	public static boolean calculateCooccurrences(String inputDir,
			String seedFile, int windowSize, String outputPath, int threshold, boolean buildMatrix) {
		String currentLine = null;
		Map<String, Integer> seedWords = new HashMap<String, Integer>();
		Queue<String> q = new LinkedList<String>();
		Map<String, Map<String, Integer>> wordMat = new HashMap<String, Map<String, Integer>>();
		List<String> phrase = new ArrayList<String>();
		BufferedReader br = null;

		//build the seed word dictionary
		try {
			String[] seeds = null;
			if (windowSize > 0) {
				File sf = new File(seedFile);
				if (sf != null && sf.exists()) {
					if(sf.isDirectory()){
						seedFile = sf.listFiles()[0].getAbsolutePath();
					}
					br = new BufferedReader(new FileReader(new File(seedFile)));
					while ((currentLine = br.readLine()) != null) {
						seeds = currentLine.split(" ");
						for (String seed : seeds) {
							if (!seedWords.containsKey(seed)) {

								seedWords.put(seed, 1);
							}
						}

					}
					br.close();
					doPhrases = true;
				}
			}

			if(windowSize == 0 || seedWords.size() == 0)
				doPhrases = false;

			File dir = new File(inputDir);
			File[] listOfFiles = dir.listFiles();

			Map<String, Integer> vec = null;
			int i, len, count;
			String first;
			StringBuilder match;
			int size = seedWords.size();
			for (File f : listOfFiles) {
				//System.out.println(words);
				len = count = 0;
				int line_no = 0;
				if (f.getAbsolutePath().contains("DS_Store"))
					continue;
				System.out.println("Processing");
				List<String> words = new ArrayList<String>();
				if(!f.exists() || f.isDirectory())
					continue;
				br = new BufferedReader(new FileReader(f));
				while ((currentLine = br.readLine()) != null) {
					if(currentLine.isEmpty() || currentLine.equals(""))
						continue;
					line_no++;
					for (String word : currentLine.split(" ")){
						if(word.isEmpty() || word.equals(""))
							continue;

						word.replaceAll("[.,;\"!-()\\[\\]{}:?'/\\`~$%#@&*_=+<>*$]", "");
						if(buildMatrix)
							words.add(word);


						if(doPhrases){

							if(size!=0 && (count >= threshold || count>= size) ){
								match = new StringBuilder();
								for(String s: q)
									match.append(s+' '); 
								phrase.add(f.getName() + "  " + line_no + " " + match.toString() );
								q.clear();
								count =0;
								for(String s: seedWords.keySet()){
									seedWords.put(s, 1);
								}
							}else if(q.size() >= windowSize){
								first = q.remove();
								if(seedWords.containsKey(first)){
									if(seedWords.get(first) == 0){
										count--;
										seedWords.put(first, 1);
									}
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

						if(buildMatrix){
							//	System.out.println("Building word mat for " + word);
							if (wordMat.containsKey(word)) {
								vec = wordMat.get(word);
							} else {
								vec = new HashMap<String, Integer>();
								wordMat.put(word, vec);
							}
							for (String second : words) {
								if (vec.containsKey(second)) {
									vec.put(second, vec.get(second) + 1);
								} else {
									vec.put(second, 1);
								}
								Map<String, Integer> temp =  wordMat.get(second);
								if(temp.containsKey(word)){
									temp.put(word, temp.get(word) + 1);
								}else{
									temp.put(word, 1);
								}

							}
						}

					}
				}

			}

			if(buildMatrix){
				SortedSet<String> keys = new TreeSet<String>(wordMat.keySet());
				System.out.println(keys.size());
				try {
					FileWriter fw = new FileWriter(new File(outputPath	+ File.separator + "word-to-word-matrix.csv"));
					fw.write(" ,");
					for (String key : keys) {
						fw.write(key + ",");
					}
					fw.write("\n");

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
			}
			if(seedFile!= "" && !seedFile.isEmpty() && phrase.size()>0)
			{
				try {
					FileWriter fw = new FileWriter(new File(outputPath	+ File.separator + "phrases.txt"));
					for(String p:phrase)
						fw.write(p+"\n");
					fw.close();
				} catch (IOException e) {
					System.out.println("Error writing output to file phrases.txt " + e);
				}
			}

			System.out.println(phrase.size());
			//for(String s:phrase){
			//	System.out.println(s);
			//}

			return true;
		} catch (Exception e) {
			System.out.println("Exception occurred in Cooccurrence Analysis "+ e);
		}
		return false;
	}
}
