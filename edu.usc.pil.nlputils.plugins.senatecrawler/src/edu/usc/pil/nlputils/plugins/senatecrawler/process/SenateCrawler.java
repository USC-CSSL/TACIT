package edu.usc.pil.nlputils.plugins.senatecrawler.process;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SenateCrawler {
	
	public SenateCrawler(){
		
	}
	
	public void doCrawl() throws IOException{
		Document doc = Jsoup.connect("http://thomas.loc.gov/cgi-bin/thomas2")
				.data("xss","query")		// Important. If removed, "301 Moved permanently" error
				.data("queryr113","")		// Important. 113 - congress number. Make this auto? If removed, "Database Missing" error
				.data("MaxDocs","2000")
				.data("Stemming","No")
				.data("HSpeaker","")
				.data("SSpeaker","Alexander, Lamar (R-TN)")
				.data("member","speaking")	// speaking | all  -- all occurrences
				.data("relation","or")		// or | and  -- when there are multiple speakers in the query
				.data("SenateSection","1")
				//.data("HouseSection","2")
				//.data("ExSection","4")
				//.data("DigestSection","8")
				.data("LBDateSel","")		// "" | 1st | 2nd  -- all sessions, 1st session, 2nd session
				.data("DateFrom","")
				.data("DateTo","")
				.data("sort","Default")		// Default | Date
				.data("submit","SEARCH")
				.userAgent("Mozilla")
				.post();
		Elements links = doc.getElementById("content").getElementsByTag("a");
		
		// Removing unnecessary links
		links.remove(0);
		links.remove(0);
		links.remove(0);
		links.remove(links.size()-1);
		links.remove(links.size()-1);
		links.remove(links.size()-1);
		
		// Process each search result
		for (Element link : links){
			System.out.println("Processing "+link.text());
			Document record = Jsoup.connect("http://thomas.loc.gov"+link.attr("href")).get();
			Elements tabLinks = record.getElementById("content").select("a[href]");
			
			String extractLink="";
			// Find Printer Friendly Display
			for (Element tabLink:tabLinks){
				if (tabLink.text().equals("Printer Friendly Display")){
					extractLink = tabLink.attr("href");
					break;
				}
			}
			
			extract(extractLink);
			//break;
		}
			
	}

	private void extract(String extractLink) throws IOException {
		Document page = Jsoup.connect("http://thomas.loc.gov"+extractLink).get();
		//System.out.println(page.getElementById("container"));
		//System.out.println(page.getElementById("container").text());
		String title = page.getElementById("container").select("b").text();
		StringBuilder content = new StringBuilder();
		Elements lines = page.getElementById("container").select("p");
		for (Element line : lines) {
			content.append(line.text()+"\n");
		}
		System.out.println(extractLink.substring(extractLink.lastIndexOf('/')+2));
		System.out.println(title);
		System.out.println(content.toString());
	}
}
