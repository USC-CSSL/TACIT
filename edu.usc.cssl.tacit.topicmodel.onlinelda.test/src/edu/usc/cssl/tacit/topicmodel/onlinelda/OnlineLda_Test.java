package edu.usc.cssl.tacit.topicmodel.onlinelda;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.topicmodel.onlinelda.services.OnlineLDA;

public class OnlineLda_Test {
	final String directoryPath = new File("TestData").getAbsolutePath();
	Exception exceptionObj = null;
	@Test
	public void doOnlineLdaTest() {
		String dictionaryLocation = directoryPath + File.separator + "OnlineLDADictionary.txt";
		String outputLocation =  directoryPath + File.separator + "TestOutputFiles";
		File outputFolder = new File(outputLocation);
		
		if (!outputFolder.exists()){
			outputFolder.mkdir();
		}
		
		List<String> documentList = new ArrayList<String>();
		documentList.add(directoryPath + File.separator + "TestInputFiles" + File.separator + "OnlineLDATestFile1");
		documentList.add(directoryPath + File.separator + "TestInputFiles" + File.separator + "OnlineLDATestFile2");
		documentList.add(directoryPath + File.separator + "TestInputFiles" + File.separator + "OnlineLDATestFile3");
		documentList.add(directoryPath + File.separator + "TestInputFiles" + File.separator + "OnlineLDATestFile4");
		documentList.add(directoryPath + File.separator + "TestInputFiles" + File.separator + "OnlineLDATestFile5");
		
		exceptionObj = null;
		try{
			OnlineLDA onlineLDA = new OnlineLDA(documentList, dictionaryLocation, outputLocation, 10, 5);
			onlineLDA.invokeOnlineLDA(new NullProgressMonitor());
			//FileUtils.deleteDirectory(outputFolder);
		}catch(Exception e){
			exceptionObj = e; 
		}
		
		assertEquals("Checking if any exception occured", exceptionObj, null);
		
		
	}
}
