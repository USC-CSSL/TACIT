/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.nlputils.plugins.preprocessor.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.Bundle;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import snowballstemmer.DanishStemmer;
import snowballstemmer.DutchStemmer;
import snowballstemmer.EnglishStemmer;
import snowballstemmer.FinnishStemmer;
import snowballstemmer.FrenchStemmer;
import snowballstemmer.HungarianStemmer;
import snowballstemmer.ItalianStemmer;
import snowballstemmer.NorwegianStemmer;
//import snowballstemmer.PorterStemmer;
import snowballstemmer.SnowballStemmer;
import snowballstemmer.GermanStemmer;
import snowballstemmer.TurkishStemmer;

public class Preprocess {
	private StringBuilder readMe = new StringBuilder();
	private boolean doLowercase = false;
	private boolean doStemming = false;
	private boolean doStopWords = false;
	private boolean doLangDetect = false;
	private String delimiters = " .,;'\"!-()[]{}:?";
	private String[] inputFiles;
	private String stopwordsFile;
	private String outputPath;
	private String suffix;
	private HashSet<String> stopWordsSet = new HashSet<String>();
	SnowballStemmer stemmer=null;
	String stemLang;
	
	
	public Preprocess(String[] inputFiles, String stopwordsFile, String outputPath, String suffix, String delimiters, boolean doLowercase, boolean doStemming, String stemLang){
		this.inputFiles = inputFiles;
		this.stopwordsFile = stopwordsFile;
		this.outputPath = outputPath;
		this.suffix = suffix;
		this.delimiters=delimiters;
		this.doLowercase = doLowercase;
		this.doStemming = doStemming;
		this.stemLang = stemLang.toUpperCase();
	}
	
	public int doPreprocess() throws IOException, LangDetectException{
		
		if (stopwordsFile != null && !stopwordsFile.isEmpty()){
		// If stopwordsFile is not given, doStopWords is false by default. Check only if it's not empty
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
		}
		
		if (doStemming){	// If stemming has to be performed, find the appropriate stemmer.
		if (stemLang.equals("AUTO DETECT LANGUAGE")){
			appendLog("Initializing Language Detection...");
			doLangDetect = true;
			Bundle bundle = Platform.getBundle("edu.usc.cssl.nlputils.plugins.preprocessor");
			URL url = FileLocator.find(bundle, new Path("profiles"),null);
			URL fileURL = FileLocator.toFileURL(url);
			System.out.println(fileURL.getPath());
			//System.out.println(System.getProperty("user.dir"));
			//System.out.println(this.getClass().getResource("").getPath());
//			URL main = Preprocess.class.getResource("Preprocess.class");
//			if (!"file".equalsIgnoreCase(main.getProtocol()))
//			  throw new IllegalStateException("Main class is not stored in a file.");
//			File path = new File(main.getPath());
//			System.out.println(path);
			try{
			DetectorFactory.loadProfile(fileURL.getPath());
			} catch (com.cybozu.labs.langdetect.LangDetectException ex){
				//ex.printStackTrace();
				System.out.println("Exception code - "+ex.getCode());
				//ex.getCode().toString() -> is not visible!
			}
		} else{
			doLangDetect = false;
			stemmer = stemSelect(stemLang);
		}
		}
		
		for (String inputFile:inputFiles){
			
			// Mac cache file filtering
			if (inputFile.contains("DS_Store"))
				continue;
			
			System.out.println("Preprocessing file "+inputFile);
			appendLog("Preprocessing file "+inputFile);
			File iFile = new File(inputFile);
			if (!iFile.exists() || iFile.isDirectory()){
				System.out.println("Error in input file path "+iFile.getAbsolutePath());
				appendLog("Error in input file path "+iFile.getAbsolutePath());
				return -1;
			}
			
			int dindex = inputFile.lastIndexOf('.');
			int findex = inputFile.lastIndexOf(System.getProperty("file.separator"));
			String fname = inputFile.substring(findex,dindex);
			String ext = inputFile.substring(dindex+1);
			File oFile = new File(outputPath+System.getProperty("file.separator")+fname+suffix+"."+ext);
			System.out.println("Creating out file "+oFile.getAbsolutePath());
			appendLog("Creating out file "+oFile.getAbsolutePath());
			
			// doLangDetect only if doStemming is true
			if (doLangDetect) {
				stemmer = findLangStemmer(iFile);
				if (stemmer==null){
					appendLog("Failed to detect the language. Please select manually.");
					return -3;
				}
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(iFile), "UTF8"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(oFile),"UTF-8"));
			
			String currentLine;
			while((currentLine = br.readLine())!=null){
				//System.out.println(currentLine);
				for (char c:delimiters.toCharArray())
					currentLine = currentLine.replace(c, ' ');
				if (doLowercase){
					//Locale trLocale = Locale.forLanguageTag("tr_TR");
					//System.out.println(currentLine);
					currentLine = currentLine.toLowerCase();
					//System.out.println(currentLine);
				}
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
		writeReadMe(outputPath);
		return 1;
	}

	private SnowballStemmer stemSelect(String lang){
		switch(lang.toUpperCase()){
		case "EN":
			appendLog("Language - English.");
			return new EnglishStemmer();
		case "DE":
			appendLog("Language - German.");
			return new GermanStemmer();
		case "FR":
			appendLog("Language - French.");
			return new FrenchStemmer();
		case "IT":
			appendLog("Language - Italian.");
			return new ItalianStemmer();
		case "DA":
			appendLog("Language - Dannish.");
			return new DanishStemmer();
		case "NL":
			appendLog("Language - Dutch.");
			return new DutchStemmer();
		case "FI":
			appendLog("Language - Finnish.");
			return new FinnishStemmer();
		case "HU":
			appendLog("Language - Hungarian.");
			return new HungarianStemmer();
		case "NO":
			appendLog("Language - Norwegian.");
			return new NorwegianStemmer();
		case "TR":
			appendLog("Language - Turkish.");
			return new TurkishStemmer();
		}
		return null;
	}
	
	private SnowballStemmer findLangStemmer(File iFile) throws IOException, LangDetectException {
		BufferedReader br = new BufferedReader(new FileReader(iFile));
		String sampleText="";
		for (int i = 0;i<3;i++){
			String currentLine = br.readLine();
			if (currentLine == null)
				break;
			sampleText = sampleText+ currentLine.trim().replace('\n', ' ');
		}
		appendLog("Detecting language...");
		Detector detector = DetectorFactory.create();
		detector.append(sampleText);
		String lang = detector.detect();
		return stemSelect(lang);
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
			if (context!=null){
			IEclipseContext parent = context.getParent();
			parent.set("consoleMessage", message);
			readMe.append(message+"\n");
			}
		}
		
		public void writeReadMe(String path){
			File readme = new File(path+"/README.txt");
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
				String plugV = Platform.getBundle("edu.usc.cssl.nlputils.plugins.preprocessor").getHeaders().get("Bundle-Version");
				String appV = Platform.getBundle("edu.usc.cssl.nlputils.application").getHeaders().get("Bundle-Version");
				Date date = new Date();
				bw.write("Preprocessor Output\n-------------------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
				bw.write(readMe.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

}
