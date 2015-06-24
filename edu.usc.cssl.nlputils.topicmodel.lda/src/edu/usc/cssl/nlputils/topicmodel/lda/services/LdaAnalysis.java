package edu.usc.cssl.nlputils.topicmodel.lda.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.nlputils.common.TacitUtility;


public class LdaAnalysis {
	private StringBuilder readMe = new StringBuilder();
	private String sourceDir;
	private int numTopics;
	private String outputDir;
	private String label;
	private boolean wordWeights;
	
	public void initialize(String sourceDir, int numTopics, String outputDir, String label, boolean wordWeights){
		this.sourceDir = sourceDir;
		this.numTopics = numTopics;
		this.outputDir = outputDir;
		this.label = label;
		this.wordWeights = wordWeights;
	}
	
	public void doLDA(IProgressMonitor monitor) throws FileNotFoundException, IOException{
		Calendar cal = Calendar.getInstance();
		String dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR)+"-"+System.currentTimeMillis();
		String outputPath = outputDir+System.getProperty("file.separator")+label+"-"+dateString;
		
		String keepSeq = "TRUE", stopWords = "FALSE", preserveCase = "TRUE";
		
		/*
		if (removeStopwords){
			stopWords = "TRUE";
		}
		if (doLowercase){
			preserveCase = "FALSE";
		}*/
		
		String[] t2vArgs = {"--input",sourceDir,"--output",outputPath+".mallet","--keep-sequence",keepSeq,"--remove-stopwords",stopWords,"--preserve-case",preserveCase};
		String[] v2tArgs = {"--input",outputPath+".mallet","--num-topics",String.valueOf(numTopics),"--optimize-interval","20","--output-state",outputPath+".topic-state.gz",
				"--output-topic-keys",outputPath+".topic_keys.txt","--output-doc-topics",outputPath+".topic_composition.txt","--topic-word-weights-file",outputPath+".word_weights.txt","--word-topic-counts-file",outputPath+".word_counts.txt"};
		monitor.subTask("Performing text to vector conversion");
		//--input pathway\to\the\directory\with\the\files --output tutorial.mallet --keep-sequence --remove-stopwords
		Text2Vectors.main(t2vArgs);
		monitor.worked(15);
		monitor.subTask("Performing vector to topics conversion");
		//--input tutorial.mallet --num-topics 20 --output-state topic-state.gz --output-topic-keys tutorial_keys.txt --output-doc-topics tutorial_compostion.txt
		Vectors2Topics.main(v2tArgs);
		monitor.worked(5);
		monitor.subTask("Created complete state file "+outputPath+".topic-state.gz");
//		ConsoleView.printlInConsoleln("Created complete state file "+outputPath+".topic-state.gz");
//		ConsoleView.printlInConsoleln("Created topic keys file "+outputPath+".topic_keys.txt");
//		ConsoleView.printlInConsoleln("Created topic composition file "+outputPath+".topic_composition.txt");
//		ConsoleView.printlInConsoleln("Created topic word counts file "+outputPath+".word_counts.txt");

		
		monitor.subTask("Convert "+outputPath+".topic_keys to csv");
		convertKeys2csv(outputPath+".topic_keys");
		ConsoleView.printlInConsoleln("Created topic keys file "+outputPath+".topic_keys.csv");
		monitor.worked(5);
		monitor.subTask("Convert "+outputPath+".topic_composition to csv");
		convertComposition2csv(outputPath+".topic_composition");
		ConsoleView.printlInConsoleln("Created topic composition file "+outputPath+".topic_composition.csv");
		monitor.worked(5);
		monitor.subTask("Convert "+outputPath+".word_counts to csv");
		if (wordWeights) {
			convertWeights2csv(outputPath+".word_weights");
			ConsoleView.printlInConsoleln("Created word weights file "+outputPath+".word_weights.csv");
		}
		
		monitor.worked(5);
		
		deleteFiles(outputPath);
		TacitUtility.createReadMe(outputPath, "LDA Analysis");
		monitor.worked(5);
	}
	
	private void deleteFiles(String outputPath) {
		File toDel = new File(outputPath+".topic-state.gz");
		toDel.delete();
		toDel = new File(outputPath+".word_counts.txt");
		toDel.delete();
		toDel = new File(outputPath+".topic_keys.txt");
		toDel.delete();
		toDel = new File(outputPath+".topic_composition.txt");
		toDel.delete();
		toDel = new File(outputPath+".word_weights.txt");
		toDel.delete();
		toDel = new File(outputPath+".mallet");
		toDel.delete();
	}
	
	private void convertWeights2csv(String fileName) {
		
		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new FileReader(new File(fileName+".txt")));
			bw = new BufferedWriter(new FileWriter(new File(fileName+".csv")));
			
			String currentLine = "Topic,Word,Weight";
			bw.write(currentLine);
			bw.newLine();
			while((currentLine = br.readLine())!=null){
				currentLine = currentLine.replace('\t', ',');
				List<String> wordList = Arrays.asList(currentLine.split(","));
				bw.write(wordList.get(0)+","+wordList.get(1)+","+wordList.get(2));
				bw.newLine();
			}
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void convertKeys2csv(String fileName) {
		
		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new FileReader(new File(fileName+".txt")));
			bw = new BufferedWriter(new FileWriter(new File(fileName+".csv")));
			
			String currentLine = "Topic,Keywords";
			bw.write(currentLine);
			bw.newLine();
			while((currentLine = br.readLine())!=null){
				currentLine = currentLine.replace('\t', ',');
				List<String> wordList = Arrays.asList(currentLine.split(","));
				bw.write(wordList.get(0)+","+wordList.get(2));
				bw.newLine();
			}
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void convertComposition2csv(String fileName) {
		
		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new FileReader(new File(fileName+".txt")));
			bw = new BufferedWriter(new FileWriter(new File(fileName+".csv")));
			
			String currentLine = br.readLine();
			currentLine = "Number,File Name";
			for (int i=0; i < numTopics; i++){
				currentLine = currentLine+","+"Topic "+i+" Probability";
			}
			bw.write(currentLine);
			bw.newLine();
			while((currentLine = br.readLine())!=null){
				currentLine = currentLine.replace('\t', ',');
				List<String> wordList = Arrays.asList(currentLine.split(","));
				
				HashMap<String, String> probabilities = new HashMap<String, String>();
				
				for (int i=2; i<wordList.size(); i=i+2){
					probabilities.put(wordList.get(i), wordList.get(i+1));
				}
				currentLine = wordList.get(0)+","+wordList.get(1);
				//bw.write(wordList.get(0)+","+wordList.get(1));
				for (int i=0; i<numTopics; i++){
					String keyVal = probabilities.get(Integer.toString(i));
					currentLine = currentLine+","+keyVal;
				}
				bw.write(currentLine);
				bw.newLine();
			}
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
//	private void convert2csv(String string, boolean space, String header) {
//		try {
//			BufferedReader br;
//			BufferedWriter bw;
//			br = new BufferedReader(new FileReader(new File(string+".txt")));
//			bw = new BufferedWriter(new FileWriter(new File(string+".csv")));
//			String currentLine;
//			while((currentLine = br.readLine())!=null){
//				if (space)
//					bw.write(currentLine.replace(' ', ','));
//				else
//				bw.write(currentLine.replace('\t', ','));
//				bw.newLine();
//			}
//			br.close();
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		}

	
	public void writeReadMe(String location){
		File readme = new File(location+"_README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform.getBundle("edu.usc.cssl.nlputils.topicmodel.lda").getHeaders().get("Bundle-Version");
			String appV = Platform.getBundle("edu.usc.cssl.nlputils.repository").getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("LDA Output\n----------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
