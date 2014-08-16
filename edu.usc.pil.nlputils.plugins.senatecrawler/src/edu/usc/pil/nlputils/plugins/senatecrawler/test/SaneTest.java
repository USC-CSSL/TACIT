package edu.usc.pil.nlputils.plugins.senatecrawler.test;

import java.io.IOException;

import edu.usc.pil.nlputils.plugins.senatecrawler.process.SenateCrawler;

public class SaneTest {

	public static void main(String[] args) {
		//System.out.println(Integer.valueOf("test"));
		
		try {
			SenateCrawler sc = new SenateCrawler(2, "07/01/2013","07/10/2014","c:\\senate");
			//SenateCrawler sc = new SenateCrawler(10, "","");
			//sc.doCrawl();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
