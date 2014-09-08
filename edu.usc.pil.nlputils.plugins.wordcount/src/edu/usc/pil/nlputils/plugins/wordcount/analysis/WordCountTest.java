package edu.usc.pil.nlputils.plugins.wordcount.analysis;

import edu.usc.pil.nlputils.plugins.wordcount.utilities.Trie;

import java.io.IOException;
import java.util.ArrayList;

import snowballstemmer.PorterStemmer;



public class WordCountTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/**/
		//mainTest();
		//trieTest();
		multiTest();
		/*
		PorterStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent("legalization");
		if (stemmer.stem()){
			System.out.println(stemmer.getCurrent());
		}
		*/
	}
	
	public static void multiTest(){
		System.out.println("Starting word count");
		WordCount wc = new WordCount();
		String files[];
		files = new String[]{"c:/LIWC/test file.txt"};
		//files = new String[]{"c:/test/indian summer.txt"};
		try {
		wc.wordCount(files,"c:/LIWC/TabbedDictionary.dic","","c:/LIWC/numberOutWin","",true, true, false, false, false);
		//wc.wordCount(files,"c:/LIWC/TabbedDictionary.dic","","c:/LIWC/numberOutWin"," .,;\"!-()[]{}:?",true, true, false, false, false);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void mainTest(){
		WordCount wc = new WordCount();
		try {
			/*
			Test Data
			inputFile = "c:/test/indian summer.txt";
			dictionaryFile = "c:/test/dictionary.dic";
			stopWordsFile = "c:/test/stop words.txt";
			outputFile = "c:/test/output.csv";
			delimiters = " .,;'\"!-()[]{}:?";
			*/
		wc.oldWordCount("c:/test/indian summer.txt","c:/test/dictionary.dic","c:/test/stop words.txt","c:/test/output.csv"," .,;'\"!-()[]{}:?",true);	
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void trieTest(){
		ArrayList i = new ArrayList();
		i.add(5);
		ArrayList j = new ArrayList();
		j.add(6);
		j.add(8);
		Trie test = new Trie();
		test.insert("a", i);
		test.insert("abs", i);
		test.insert("Healo", i);
		test.insert("Heal", i);
		test.insert("Helper", i);
		test.insert("Hellogoodbye", i);
		test.insert("Abominat", i);
		test.printTrie();
		test.insert("assist", i);
		test.insert("assignm*", j);
		test.printTrie();
		System.out.println(test.query("assist"));
		System.out.println(test.query("assign"));
		System.out.println(test.query("assignm"));
		System.out.println(test.query("assignment"));
		System.out.println(test.query("Healo"));
		System.out.println(test.query("Heal"));
		System.out.println(test.query("Helper"));
		System.out.println(test.query("Hellogoodbye"));
		
	}

}
