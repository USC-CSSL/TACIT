package edu.usc.cssl.nlputils.topicmodel.lda.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;


public class LdaAnalysis {
	private StringBuilder readMe = new StringBuilder();
	private String sourceDir;
	private int numTopics;
	private String outputDir;
	private String label;
	
	public void initialize(String sourceDir, int numTopics, String outputDir, String label){
		this.sourceDir = sourceDir;
		this.numTopics = numTopics;
		this.outputDir = outputDir;
		this.label = label;
	}
	
	public void doLDA(IProgressMonitor monitor) throws FileNotFoundException, IOException{
		Calendar cal = Calendar.getInstance();
		String dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR);
		String outputPath = outputDir+System.getProperty("file.separator")+label+"-"+dateString+"-"+System.currentTimeMillis();
		
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
		ConsoleView.writeInConsole("Created complete state file "+outputPath+".topic-state.gz");
		ConsoleView.writeInConsole("Created topic keys file "+outputPath+".topic_keys.txt");
		ConsoleView.writeInConsole("Created topic composition file "+outputPath+".topic_composition.txt");
		ConsoleView.writeInConsole("Created topic word counts file "+outputPath+".word_counts.txt");

		monitor.subTask("Convert "+outputPath+".topic_keys to csv");
		convert2csv(outputPath+".topic_keys",false);
		monitor.worked(5);
		monitor.subTask("Convert "+outputPath+".topic_composition to csv");
		convert2csv(outputPath+".topic_composition",false);
		monitor.worked(5);
		monitor.subTask("Convert "+outputPath+".word_counts to csv");
		convert2csv(outputPath+".word_counts", true);
		monitor.worked(5);
		
		ConsoleView.writeInConsole("Created topic keys csv file "+outputPath+".topic_keys.csv");
		ConsoleView.writeInConsole("Created topic composition csv file "+outputPath+".topic_composition.csv");
		monitor.subTask("writing Read Me File");
		writeReadMe(outputPath);
		monitor.worked(5);
	}
	
	
	private void convert2csv(String string, boolean space) {
		try {
			BufferedReader br;
			BufferedWriter bw;
			br = new BufferedReader(new FileReader(new File(string+".txt")));
			bw = new BufferedWriter(new FileWriter(new File(string+".csv")));
			String currentLine;
			while((currentLine = br.readLine())!=null){
				if (space)
					bw.write(currentLine.replace(' ', ','));
				else
				bw.write(currentLine.replace('\t', ','));
				bw.newLine();
			}
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}

	
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
