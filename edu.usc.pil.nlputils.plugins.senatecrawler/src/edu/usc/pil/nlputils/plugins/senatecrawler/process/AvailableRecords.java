package edu.usc.pil.nlputils.plugins.senatecrawler.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AvailableRecords {
	
	public static String[] getAllCongresses() throws IOException{
			Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record").timeout(10*1000).get();
			Elements congList = doc.select("p.nav");
			//System.out.println(congList);
			String congString = " All|"+congList.text().split(":")[1];
			return congString.split("\\|");
	}
	
	public static String[] getSenators(String congressString) throws IOException {
		congressString = congressString.replace("\u00A0", "");
		int congress = Integer.parseInt(congressString);
		System.out.println("Extracting Senators of Congress "+congress);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congress).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		String[] senArray = new String[senList.size()-1];
		int index = 0;
		for (Element senItem : senList){
			String senText = senItem.text().replace("\u00A0", " ");
			if (senText.equals("Any Senator"))
				continue;
			senArray[index++] = senText;
		}
		return senArray;
	}
	
	public static String[] getAllSenators(String[] congresses) throws IOException{
		TreeSet<String> senators = new TreeSet<String>();
		for (String cong : congresses){
			if (cong.trim().equals("All"))
				continue;
			String[] temp = getSenators(cong.trim());
			for (String senator : temp){
				if (senator.equals("Any Senator"))
					continue;
				senators.add(senator);
			}
		}
		String[] senatorArray = new String[senators.size()];
		senators.toArray(senatorArray);
		return senatorArray;
	}
}
