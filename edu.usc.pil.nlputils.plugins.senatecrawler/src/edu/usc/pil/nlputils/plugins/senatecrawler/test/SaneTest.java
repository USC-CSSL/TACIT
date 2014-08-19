package edu.usc.pil.nlputils.plugins.senatecrawler.test;

import java.io.IOException;

import edu.usc.pil.nlputils.plugins.senatecrawler.process.SenateCrawler;

public class SaneTest {

	public static void main(String[] args) {
		//System.out.println(Integer.valueOf("test"));
		
		try {
			//SenateCrawler sc = new SenateCrawler(2, "07/01/2013","07/10/2014","c:\\senate");
			//SenateCrawler sc = new SenateCrawler(2, 112,null,"","","c:\\senate");
			//SenateCrawler sc = new SenateCrawler(2, 113, "Cowan, William M. (D-MA)", "07/01/2013","07/10/2014","c:\\senate");
			SenateCrawler sc = new SenateCrawler();
			//sc.initialize(2, 113, "Alexander, Lamar (R-TN)", "6/17/2014","7/17/2014","c:\\senate");
			
			//sc.initialize(-1, -1, "All Independents", "","","c:\\senate\\inde");
			
			sc.initialize(2,113, "Alexander, Lamar (R-TN)", "5/19/2014", "5/24/2014","c:\\senate\\smallinde");
			sc.initialize(2,113, "Baldwin, Tammy (D-WI)", "5/19/2014", "5/24/2014","c:\\senate\\smallinde");
			sc.initialize(2,113, "Enzi, Michael B. (R-WY)", "5/19/2014", "5/24/2014","c:\\senate\\smallinde");
			sc.initialize(2,113, "Murphy, Christopher S. (D-CT)", "5/19/2014", "5/24/2014","c:\\senate\\smallinde");
			sc.initialize(2,113, "King, Angus S., Jr. (I-ME)", "5/19/2014", "5/24/2014","c:\\senate\\smallinde");
			//sc.getAll(113, "All Independents");
			//sc.searchSenatorRecords(113, senText)
			
			//sc.doCrawl();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
