package edu.usc.pil.nlputils.plugins.svmClassifier.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Convertor {
	
	private HashSet<String> stopWordsSet = new HashSet<String>();
	private String delimiters = " .,;'\"!-()[]{}:?";
	
	// Should convert each text file to wordcount (Bag of words) map (bag of words)
	public HashMap<String, Integer> fileToBOW (File inputFile) throws IOException{
		HashMap<String, Integer> hashMap = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String currentLine;
		StringBuilder fullFile = new StringBuilder();
		while((currentLine = br.readLine())!=null){
			fullFile.append(currentLine);
		}
		System.out.println(fullFile);
/*		for (char c:delimiters.toCharArray())
			currentLine = currentLine.replace(c, ' ');
		currentLine = removeStopWords(currentLine);
	*/	
		return null;
	}
	
	public String removeStopWords(String currentLine){
		StringBuilder returnString = new StringBuilder();
		String[] wordArray = currentLine.split("\\s+");
		for (String word : wordArray){
			if (!stopWordsSet.contains(word.toLowerCase())){
				returnString.append(word);
				returnString.append(' ');
			}
		}
		return returnString.toString();
	}
	
	public static void main() throws IOException{
		Convertor c = new Convertor();
		File file = new File("c:\\test\\testsmall.txt");
		c.fileToBOW(file);
	}
}
