/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.nlputils.plugins.preprocessorService.services;

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
import java.util.HashSet;

import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import snowballstemmer.DanishStemmer;
import snowballstemmer.DutchStemmer;
import snowballstemmer.EnglishStemmer;
import snowballstemmer.FinnishStemmer;
import snowballstemmer.FrenchStemmer;
import snowballstemmer.GermanStemmer;
import snowballstemmer.HungarianStemmer;
import snowballstemmer.ItalianStemmer;
import snowballstemmer.NorwegianStemmer;
import snowballstemmer.SnowballStemmer;
import snowballstemmer.TurkishStemmer;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import edu.usc.cssl.nlputils.plugins.preprocessorService.UI.PPDialog;

public class PreprocessorService {
	public OptionObject options;
	public boolean doPP = false;
	private boolean doStopWords = false;
	private HashSet<String> stopWordsSet = new HashSet<String>();
	private boolean doLangDetect = false;
	SnowballStemmer stemmer=null;
	private boolean initial = true;
	private PPDialog ppDialog = null;
	
	public void setOptions(Shell shell) {
		//System.out.println("Doing Preprocessing");
		
		if (initial){
			System.out.println("New Settings Dialog");
			ppDialog = new PPDialog(shell);
		} 
		ppDialog.open();
		options = ppDialog.getOptions();
		doPP = ppDialog.doPP;
		initial = false;
	}
	
	public String getOptions(){
		return options.getStopFile();
	}
	
	// for File as well as Directory
	public String doPreprocessing(String path) throws IOException{
		File[] files;
		File input = new File(path);
		String outputPath;
		if (input.isDirectory()){
			files = input.listFiles();
			outputPath = path+System.getProperty("file.separator")+"_preprocessed";
		} else {
			files = new File[1];
			files[0] = input;
			//outputPath = path.substring(0, path.lastIndexOf(System.getProperty("file.separator")))+System.getProperty("file.separator")+"preprocessed";
			outputPath = input.getParentFile().getAbsolutePath()+System.getProperty("file.separator")+"_preprocessed";
		}
		
		if (new File(outputPath).mkdir()){
			System.out.println("Output path created successfully.");
		}
			
		
		if (options.getStopFile()!=null && options.getStopFile()!="" && options.getStopFile().length()>3){
			doStopWords = true;
			String currentLine;
			BufferedReader br = new BufferedReader(new FileReader(new File(options.getStopFile())));
			while ((currentLine = br.readLine())!=null){
				stopWordsSet.add(currentLine.trim().toLowerCase());
			}
			br.close();
		}
		
		if (options.isDoStemming()){	// If stemming has to be performed, find the appropriate stemmer.
			if (options.getStemLang().equals("Auto Detect Language")){
				appendLog("Initializing Language Detection...");
				doLangDetect = true;
				Bundle bundle = Platform.getBundle("edu.usc.cssl.nlputils.plugins.preprocessor");
				URL url = FileLocator.find(bundle, new Path("profiles"),null);
				URL fileURL = FileLocator.toFileURL(url);
				System.out.println(fileURL.getPath());
				//System.out.println(System.getProperty("user.dir"));
				//System.out.println(this.getClass().getResource("").getPath());
//				URL main = Preprocess.class.getResource("Preprocess.class");
//				if (!"file".equalsIgnoreCase(main.getProtocol()))
//				  throw new IllegalStateException("Main class is not stored in a file.");
//				File path = new File(main.getPath());
//				System.out.println(path);
				try{
				DetectorFactory.loadProfile(fileURL.getPath());
				} catch (com.cybozu.labs.langdetect.LangDetectException ex){
					//ex.printStackTrace();
					System.out.println("Exception code - "+ex.getCode());
					//ex.getCode().toString() -> is not visible!
				}
			} else{
				doLangDetect = false;
				stemmer = stemSelect(options.getStemLang());
			}
			}
		
		for (File f : files){
			
			// Mac cache file filtering
			if (f.getAbsolutePath().contains("DS_Store"))
				continue;

			if ("_preprocessed".equals(f.getName()))
				continue;
			String inputFile = f.getAbsolutePath();
			System.out.println("Preprocessing "+inputFile);
			
			
			// doLangDetect only if doStemming is true
			if (doLangDetect) {
				try {
					stemmer = findLangStemmer(f);
				} catch (LangDetectException e) {
					e.printStackTrace();
				}
				if (stemmer==null){
					appendLog("Failed to detect the language. Please select manually.");
				}
			}
			
			
			File iFile = new File(inputFile);
			if (!iFile.exists() || iFile.isDirectory()){
				System.out.println("Error in input file path "+iFile.getAbsolutePath());
				appendLog("Error in input file path "+iFile.getAbsolutePath());
				continue;
			}
			
		
			File oFile = new File(outputPath+System.getProperty("file.separator")+f.getName());
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(iFile), "UTF8"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(oFile),"UTF-8"));
			
			String linear;
			while ((linear = br.readLine()) != null) {
				if (linear != "") {
					if (options.isDoLowercase())
						linear = linear.toLowerCase();
					for (char c : options.getDelimiters().toCharArray())
						linear = linear.replace(c, ' ');
					if (doStopWords)
						linear = removeStopWords(linear);
					if (options.isDoStemming() && stemmer!=null)
						linear = stem(linear);
					bw.write(linear + "\n");
				}
			}
			System.out.println(outputPath
					+ System.getProperty("file.separator") + f.getName());
			
			br.close();
			bw.close();
		}
		System.out.println("Preprocessed files stored in "+outputPath);
		return outputPath;
	}

	private SnowballStemmer findLangStemmer(File iFile) throws IOException, LangDetectException {
		BufferedReader br = new BufferedReader(new FileReader(iFile));
		String sampleText="";
		for (int i = 0;i<10;i++){
			String currentLine = br.readLine();
			if (currentLine == null)
				break;
			sampleText = sampleText+ currentLine.trim().replace('\n', ' ');
		}
		appendLog("Detecting language... ("+iFile.getAbsolutePath()+")");
		Detector detector = DetectorFactory.create();
		detector.append(sampleText);
		String lang = detector.detect();
		br.close();
		return stemSelect(lang);
	}

	private String stem(String linear) {
			if (linear.isEmpty())
				return "";
			StringBuilder returnString = new StringBuilder();
			String[] wordArray = linear.split("\\s+");
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
		
	private SnowballStemmer stemSelect(String stemLang) {
		switch(stemLang.toUpperCase()){
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

	private String removeStopWords(String linear) {
		StringBuilder returnString = new StringBuilder();
		String[] wordArray = linear.split("\\s+");
		for (String word : wordArray){
			if (!stopWordsSet.contains(word.toLowerCase())){
				returnString.append(word);
				returnString.append(' ');
			}
		}
		return returnString.toString();
	}

	private String makeLinear(File f) throws IOException {
		String currentLine;
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(f));
		while((currentLine = br.readLine())!=null)
			sb.append(currentLine.trim()+' ');
		br.close();
		return sb.toString();
	}
	
	public void clean(String ppDir){
		File toDel = new File(ppDir);
		// if folder, delete contents too
		if (toDel.isDirectory()){
			for (File f:toDel.listFiles()){
				f.delete();
			}
		} 		
		toDel.delete();
	}
	
	public boolean doCleanUp() {
		return options.doCleanUp();
	}
	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		IEclipseContext parent = context.getParent();
		//System.out.println(parent.get("consoleMessage"));
		String currentMessage = (String) parent.get("consoleMessage"); 
		if (currentMessage==null)
			parent.set("consoleMessage", message);
		else {
			if (currentMessage.equals(message)) {
				// Set the param to null before writing the message if it is the same as the previous message. 
				// Else, the change handler will not be called.
				parent.set("consoleMessage", null);
				parent.set("consoleMessage", message);
			}
			else
				parent.set("consoleMessage", message);
		}
	}

}
