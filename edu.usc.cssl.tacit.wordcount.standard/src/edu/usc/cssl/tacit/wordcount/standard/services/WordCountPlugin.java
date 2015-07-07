package edu.usc.cssl.tacit.wordcount.standard.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class WordCountPlugin {

	SentenceModel sentenceModel;
	TokenizerModel tokenizerModel;
	POSModel posModel;
	
	public int countWords(){
		InputStream sentenceIs,tokenIs,posIs;
		
		//File[] test = new File(ResourcesPlugin.getWorkspace().getRoot()).listFiles();
		
		try {
			sentenceIs = new FileInputStream("../../en-sent.bin");
			tokenIs = new FileInputStream("../../en-token.bin");
			posIs = new FileInputStream("../../en-pos-maxent.bin");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Model file not found");
			return -1;
		}
		
		try {
			sentenceModel = new SentenceModel(sentenceIs);
			tokenizerModel = new TokenizerModel(tokenIs);
			posModel = new POSModel(posIs);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String testString = "Hi! My name is Anurag. This is a test string to check sentence breaking. Will it work? Let's see!";
		SentenceDetectorME sentDetector = new SentenceDetectorME(sentenceModel);
		String[] results = sentDetector.sentDetect(testString);
		
		System.out.println("Here are the Strings: ");
		for (String string : results) {
			System.out.println(string);
		}
		return 0;
	}
}