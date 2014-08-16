package edu.usc.pil.nlputils.plugins.senatecrawler.test;

import java.io.IOException;

import edu.usc.pil.nlputils.plugins.senatecrawler.process.SenateCrawler;

public class SaneTest {

	public static void main(String[] args) {
		try {
			SenateCrawler sc = new SenateCrawler();
			//sc.doCrawl();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
