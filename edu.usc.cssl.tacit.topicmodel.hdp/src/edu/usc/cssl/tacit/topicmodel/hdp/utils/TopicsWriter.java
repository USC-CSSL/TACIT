package edu.usc.cssl.tacit.topicmodel.hdp.utils;



import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class TopicsWriter  {
	

	private String outFileStr;


	public TopicsWriter(String workingDir) {
		this.outFileStr = workingDir;
	}
	
	public void writeWordCountByTopicAndTerm(int[][] wordCountByTopicAndTerm, int K, int V) 
	throws FileNotFoundException {
		PrintStream file = new PrintStream(outFileStr);
		for (int k = 0; k < K; k++) {
			for (int w = 0; w < V; w++)
				file.format("%05d ",wordCountByTopicAndTerm[k][w]);
			file.println();
		}
		file.close();
	}
	
	
}
