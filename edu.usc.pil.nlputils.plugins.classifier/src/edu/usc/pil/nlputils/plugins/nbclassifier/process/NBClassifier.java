package edu.usc.pil.nlputils.plugins.nbclassifier.process;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;

import bsh.EvalError;
import cc.mallet.topics.tui.Vectors2Topics;
import edu.usc.pil.nlputils.plugins.nbclassifier.process.Text2Vectors;

public class NBClassifier {
	
	public void doClassification(String sourceDir1, String sourceDir2, String testDir1, String testDir2, String outputDir, boolean doLowercase, boolean keepSequence, boolean removeStopwords) throws FileNotFoundException, IOException, EvalError{
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
		Vectors2Classify.main(v2cArgs);
		System.out.println("Created classifier output file "+outputPath+".out");
		appendLog("Created classifier output file "+outputPath+".out");
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
