package edu.usc.cssl.tacit.topicmodel.turbotopics.test;

import static org.junit.Assert.*;

import java.beans.DefaultPersistenceDelegate;
import java.io.File;

import org.junit.Test;

import edu.usc.cssl.tacit.topicmodel.turbotopics.services.LDAtopics;

public class TurboTopicsTest {

	final String directoryPath = new File("TestData").getAbsolutePath(); 
	@Test
	public void test() {
		String corpusFile = directoryPath + File.separator + "corpus";
		String vocabFile = directoryPath + File.separator + "vocab";
		String wordTopicFile = directoryPath + File.separator + "word_topic";
		LDAtopics lda = new LDAtopics(corpusFile, wordTopicFile, vocabFile, directoryPath, 5, 25, 0.01, true);
		Exception exception = null;
		try {
			lda.generateTurboTopics();
		} catch (Exception e) {
			e.printStackTrace();
			exception = e;
		}
		assertEquals("Checking if exception was generated", null, exception);
		File file = new File(directoryPath+File.separator+".topics");
		file.delete();
	}
}
