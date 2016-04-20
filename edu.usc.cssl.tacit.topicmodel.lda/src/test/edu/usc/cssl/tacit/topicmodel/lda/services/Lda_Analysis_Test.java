package test.edu.usc.cssl.tacit.topicmodel.lda.services;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.topicmodel.lda.services.LdaAnalysis;
//This class just checks if the LDA tool finishes running successfully. If an exception is generated, it indicates that the test failed.
public class Lda_Analysis_Test {
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void doLDATest() {

		int noOfTopics = 3;
		String preFix = "Generated";
		final boolean wordWeightFile = false;
		LdaAnalysis lda = new LdaAnalysis() {
			protected void createRunReport(Date dateObj) {}
			protected String generateFileName(String fileName, Date dateObj){
				return directoryPath + File.separator +"Hello.csv";
			}
			protected String generateKeysFileName(String fileName, Date dateObj){
				return directoryPath + File.separator +"GeneratedTopicKeys.csv";
			}
			protected String generateCompositionFileName(String fileName, Date dateObj){
				return directoryPath + File.separator +"GeneratedTopicComposition.csv";
			}
		};
		lda.initialize(directoryPath + File.separator
				+ "InputFiles", noOfTopics, directoryPath, preFix, wordWeightFile);

		Date dateObj = new Date();
		Object exception = null;
		try {
			lda.doLDA(new NullProgressMonitor(), dateObj);
		} catch (FileNotFoundException e) {
			exception = e;
		} catch (IOException e) {
			exception = e;
		}
		assertEquals("Checking if any exception occured", exception, null);
	}
	
}