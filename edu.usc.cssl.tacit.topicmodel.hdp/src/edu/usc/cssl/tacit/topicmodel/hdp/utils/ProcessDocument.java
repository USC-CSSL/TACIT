package edu.usc.cssl.tacit.topicmodel.hdp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ProcessDocument {
	private static Pattern delimiters = Pattern.compile("[\\\\.,;\"!-\\)\\(\\]\\[\\{\\}:\\?'/\\`~$%#@&*_=\\+<>]");

	public static void main(String args[])throws IOException{
		ArrayList<String> a = new ArrayList<String>();
		a.add(HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"1");
		a.add(HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"2");
		a.add(HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"3");
		a.add(HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"4");
		a.add(HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"5");
		a.add(HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"6");
		a.add(HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"7");
		a.add(HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"8");
//		process(a);
	}
	
	public static void process(List<String> inFiles, String filename) {
		BufferedReader br;
		BufferedWriter bw;
//		String filename = HDPConstants.DEFAULT_CORPUS_LOCATION+File.separator+"output.txt";  
		File output = new File(filename);
		try {
			bw = new BufferedWriter(new FileWriter(output));
			for (int i = 0; i < inFiles.size(); i++) {
				HashMap<String, Integer> wordtoInt = new HashMap<String, Integer>();
				HashMap<Integer, Integer> wordCount = new HashMap<Integer, Integer>();
				File input = new File(inFiles.get(i));
				String line1;
				int id = 0;
				String terms[];
				br = new BufferedReader(new FileReader(input));
				while((line1 = br.readLine()) != null) {
					String line = delimiters.matcher(line1).replaceAll("");
					terms = line.toLowerCase().split(" ");
					for (int j = 0; j < terms.length; j++) {
						if (wordtoInt.containsKey(terms[j])) {
							System.out.print(terms[j]+",");
							int l =wordtoInt.get(terms[j]);
							System.out.print(l+",");
							int count = wordCount.get(l);
							System.out.println(count);
							wordCount.put(l,count+1);
						} else {
							id= id+1;
							wordtoInt.put(terms[j], id);
							wordCount.put(id, 1);
						}

					}
				}
				br.close();
				bw.write(wordCount.size()+" ");
				for(int k=1; k<=wordCount.size();k++){
					bw.write(k+":"+wordCount.get(k)+" ");
				}
				bw.write("\n");
			}
		bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
