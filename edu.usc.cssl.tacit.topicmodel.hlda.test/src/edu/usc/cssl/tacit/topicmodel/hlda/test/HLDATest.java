package edu.usc.cssl.tacit.topicmodel.hlda.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.topicmodel.hlda.services.HLDA;

public class HLDATest {
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void doHLDATest() throws IOException{

		Exception exception = null;
		HLDA hlda = new HLDA(directoryPath, 10, 5, new NullProgressMonitor());
		hlda.buildPipe();
		hlda.readDirectory(new File("TestData"));
		try {
			hlda.runHLDA();
		} catch (IOException e1) {
			exception = e1;
		}
		File file = new File(directoryPath+File.separator+"GeneratedTopics.txt");
		file.delete();
		
		assertEquals("Checking if any exception occured", exception, null);
	}
}
