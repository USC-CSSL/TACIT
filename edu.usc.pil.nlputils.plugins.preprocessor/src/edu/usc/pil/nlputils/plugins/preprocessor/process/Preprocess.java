package edu.usc.pil.nlputils.plugins.preprocessor.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;

import snowballstemmer.PorterStemmer;

public class Preprocess {
	private boolean doLowercase = false;
	private boolean doStemming = false;
	private boolean doStopWords = false;
	private String delimiters = " .,;'\"!-()[]{}:?";
	private String[] inputFiles;
	private String stopwordsFile;
	private String outputPath;
	private String suffix;
	private HashSet<String> stopWordsSet = new HashSet<String>();
	PorterStemmer stemmer = new PorterStemmer();
	
	public Preprocess(String[] inputFiles, String stopwordsFile, String outputPath, String suffix, String delimiters, boolean doLowercase, boolean doStemming){
		this.inputFiles = inputFiles;
		this.stopwordsFile = stopwordsFile;
		this.outputPath = outputPath;
		this.suffix = suffix;
		this.delimiters=delimiters;
		this.doLowercase = doLowercase;
		this.doStemming = doStemming;
	}
	
	public int doPreprocess() throws IOException{
		
		File sFile = new File(stopwordsFile);
		if (!sFile.exists() || sFile.isDirectory()){
			System.out.println("Error in stopwords file path "+sFile.getAbsolutePath());
			appendLog("Error in stopwords file path "+sFile.getAbsolutePath());
			return -2;
		} else {
			doStopWords = true;
			String currentLine;
			BufferedReader br = new BufferedReader(new FileReader(sFile));
			while ((currentLine = br.readLine())!=null){
				stopWordsSet.add(currentLine.trim().toLowerCase());
			}
			br.close();
		}
		
		for (String inputFile:inputFiles){
			System.out.println("Preprocessing file "+inputFile);
			appendLog("Preprocessing file "+inputFile);
			File iFile = new File(inputFile);
			if (!iFile.exists() || iFile.isDirectory()){
				System.out.println("Error in input file path "+iFile.getAbsolutePath());
				appendLog("Error in input file path "+iFile.getAbsolutePath());
				return -1;
			}
			
			int dindex = inputFile.lastIndexOf('.');
			int findex = inputFile.lastIndexOf('\\');
			String fname = inputFile.substring(findex,dindex);
			String ext = inputFile.substring(dindex+1);
			File oFile = new File(outputPath+"\\"+fname+suffix+"."+ext);
			System.out.println("Creating out file "+oFile.getAbsolutePath());
			appendLog("Creating out file "+oFile.getAbsolutePath());
			
			
			BufferedReader br = new BufferedReader(new FileReader(iFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(oFile));
			
			String currentLine;
			while((currentLine = br.readLine())!=null){
				//System.out.println(currentLine);
				for (char c:delimiters.toCharArray())
					currentLine = currentLine.replace(c, ' ');
				if (doLowercase)
					currentLine = currentLine.toLowerCase();
				if (doStopWords)
					currentLine = removeStopWords(currentLine);
				if (doStemming)
					currentLine = stem(currentLine);
				bw.write(currentLine);
				bw.newLine();
			}
			
			br.close();
			bw.close();
		}
		return 1;
	}

	private String stem(String currentLine) {
		StringBuilder returnString = new StringBuilder();
		String[] wordArray = currentLine.split("\\s+");
		for (String word: wordArray){
			stemmer.setCurrent(word);
			String stemmedWord = "";
			if(stemmer.stem())
				 stemmedWord = stemmer.getCurrent();
			if (!stemmedWord.equals(""))
				word = stemmedWord;
			returnString.append(word);
			returnString.append(' ');
		}
		return returnString.toString();
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
	
	// This function updates the consoleMessage parameter of the context.
		@Inject IEclipseContext context;
		private void appendLog(String message){
			IEclipseContext parent = context.getParent();
			parent.set("consoleMessage", message);
		}
}
