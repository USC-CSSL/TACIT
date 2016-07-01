package edu.usc.cssl.tacit.topicmodel.hdp.utils;



import java.io.IOException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class WordAssignmentsWriter {
	
	private String workioutFileStr;
	private PrintStream file = null;


	public WordAssignmentsWriter(String workingDir) {
		this.workioutFileStr = workingDir;
	}


	public void writeAssignment(int docID, int term, int topic, int table) {
		file.println(docID + " " + term + " " + topic + " " + table);
	}


	public void openForIteration() throws IOException {
		if (file != null)
			file.close(); // throw new IOException("closeIteration() must be called before opening an new iteration");
		this.file = new PrintStream(workioutFileStr);
		file.println("d w z t");
	}


	public void closeIteration() throws IOException {
		file.close();
		file = null;
	}

}
