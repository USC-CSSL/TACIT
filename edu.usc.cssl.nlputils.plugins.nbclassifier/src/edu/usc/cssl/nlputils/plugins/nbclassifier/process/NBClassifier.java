/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.nlputils.plugins.nbclassifier.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;

import bsh.EvalError;
import cc.mallet.topics.tui.Vectors2Topics;
import edu.usc.cssl.nlputils.plugins.nbclassifier.process.Text2Vectors;

public class NBClassifier {
	private StringBuilder readMe = new StringBuilder();
	
	public void doClassification(String sourceDir1, String sourceDir2, String testDir1, String testDir2, String outputDir, boolean removeStopwords, boolean doLowercase) throws FileNotFoundException, IOException, EvalError{
		Calendar cal = Calendar.getInstance();
		String dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR);
		String outputPath = outputDir+System.getProperty("file.separator")+sourceDir1.substring(sourceDir1.lastIndexOf(System.getProperty("file.separator"))+1)+"_"+sourceDir2.substring(sourceDir2.lastIndexOf(System.getProperty("file.separator"))+1)+"-"+dateString+"-"+System.currentTimeMillis();
		
		String keepSeq = "FALSE", stopWords = "FALSE", preserveCase = "TRUE";
		
		if (removeStopwords){
			stopWords = "TRUE";
		}
		if (doLowercase){
			preserveCase = "FALSE";
		}
		
		String[] t2vArgs = {"--input",sourceDir1,sourceDir2,"--output",outputPath+".train","--keep-sequence",keepSeq,"--remove-stopwords",stopWords,"--preserve-case",preserveCase};
		String[] t2vArgs_test = {"--input",testDir1,testDir2,"--output",outputPath+".test","--keep-sequence",keepSeq,"--remove-stopwords",stopWords,"--preserve-case",preserveCase,"--use-pipe-from",outputPath+".train"};
		String[] v2cArgs = {"--training-file",outputPath+".train","--testing-file",outputPath+".test","--output-classifier",outputPath+".out"};
		
		Text2Vectors.main(t2vArgs);
		System.out.println("Created training file "+outputPath+".train");
		appendLog("Created training file "+outputPath+".train");
		Text2Vectors.main(t2vArgs_test);
		System.out.println("Created test file "+outputPath+".test");
		appendLog("Created test file "+outputPath+".test");
		ArrayList<String> result = Vectors2Classify.main(v2cArgs);
		System.out.println("Created classifier output file "+outputPath+".out");
		appendLog("Created classifier output file "+outputPath+".out");
		
		System.out.println(result.get(0));
		appendLog(result.get(0));
		writeReadMe(outputPath);
	}

	public void doValidation(String sourceDir1, String sourceDir2, String valDir, String outputDir, boolean removeStopwords, boolean doLowercase) throws FileNotFoundException, IOException, EvalError{
		Calendar cal = Calendar.getInstance();
		String dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR);
		String outputPath = outputDir+System.getProperty("file.separator")+sourceDir1.substring(sourceDir1.lastIndexOf(System.getProperty("file.separator"))+1)+"_"+sourceDir2.substring(sourceDir2.lastIndexOf(System.getProperty("file.separator"))+1)+"-"+dateString+"-"+System.currentTimeMillis();
		
		String keepSeq = "FALSE", stopWords = "FALSE", preserveCase = "TRUE";

		if (removeStopwords){
			stopWords = "TRUE";
		}
		if (doLowercase){
			preserveCase = "FALSE";
		}
		
		String[] t2vArgs = {"--input",sourceDir1,sourceDir2,"--output",outputPath+".train","--keep-sequence",keepSeq,"--remove-stopwords",stopWords,"--preserve-case",preserveCase};
		String[] t2vArgs_test = {"--input",valDir,"--output",outputPath+".test","--keep-sequence",keepSeq,"--remove-stopwords",stopWords,"--preserve-case",preserveCase,"--use-pipe-from",outputPath+".train"};
		String[] v2cArgs = {"--training-file",outputPath+".train","--testing-file",outputPath+".test","--output-classifier",outputPath+".out","--report","test:raw"}; // Accuracy is irrelevant to validation. Just classify with raw report
		
		
		Text2Vectors.main(t2vArgs);
		System.out.println("Created training file "+outputPath+".train");
		appendLog("Created training file "+outputPath+".train");
		Text2Vectors.main(t2vArgs_test);
		System.out.println("Created validation file "+outputPath+".test");
		appendLog("Created validation file "+outputPath+".test");
		ArrayList<String> result = Vectors2Classify.main(v2cArgs);
		System.out.println("Created classifier output file "+outputPath+".out");
		appendLog("Created classifier output file "+outputPath+".out");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath+"_output.csv")));
		bw.write("File, Predicted Class, Other Classes\n");
		for (String s:result)
			bw.write(s+"\n");
		bw.close();
		
		System.out.println("Created prediction CSV file "+outputPath+"_output.csv");
		appendLog("Created prediction CSV file "+outputPath+"_output.csv");
		writeReadMe(outputPath);
	}

	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		if (context == null)
			return;
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
			readMe.append(message+"\n");
		}
	}
	
	public void writeReadMe(String location){
		File readme = new File(location+"_README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform.getBundle("edu.usc.cssl.nlputils.plugins.nbclassifier").getHeaders().get("Bundle-Version");
			String appV = Platform.getBundle("edu.usc.cssl.nlputils.application").getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("Naive Bayes Output\n------------------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
