package edu.usc.cssl.tacit.topicmodel.zlda.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.topicmodel.zlda.services.ZlabelTopicModelAnalysis;
//This class just checks if the LDA tool finishes running successfully. If an exception is generated, it indicates that the test failed.
public class Zlda_Test {
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void doLDATest() {

		int noOfTopics = 3;
		String preFix = "Generated";
		ZlabelTopicModelAnalysis zlda = new ZlabelTopicModelAnalysis(new SubProgressMonitor(new NullProgressMonitor(), 70)){
			protected void createRunReport(String outputdir, Date dateObj) {}
			protected String generateTopicWordsFileName(String outputdir, String date){
				return outputdir + File.separator + "GeneratedTopicWords.csv";	
			} 
			protected String generateTopicsPerDocumentFileName(String outputdir, String date){
				return outputdir + File.separator + "GeneratedTopicsPerDocument.csv";	
			} 
			protected String generateWordsInTopicFileName(String outputdir, String date){
				return outputdir + File.separator + "GeneratedWordsInTopic.csv";	
			}
		};
		Date dateObj = new Date();
		Object exception = null;
		try {
			zlda.invokeLDA(directoryPath + File.separator + "InputFiles", directoryPath+ File.separator +"ZLDASeedInput.txt",
				noOfTopics, directoryPath, dateObj);
		} catch (Exception e) {
			exception = e;
		}
		assertEquals("Checking if any exception occured", exception, null);
	}
}