package edu.usc.cssl.nlputils.crawlers.senate.services;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class AvailableRecords {
	
	public static String[] getAllCongresses() throws IOException {
			Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record").timeout(10*1000).get();
			Elements congList = doc.select("p.nav");
			String congString = " All|"+congList.text().split(":")[1];
			return congString.split("\\|");
	}
}
