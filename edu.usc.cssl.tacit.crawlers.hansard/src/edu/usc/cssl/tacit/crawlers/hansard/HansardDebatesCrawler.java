package edu.usc.cssl.tacit.crawlers.hansard;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class HansardDebatesCrawler {
	static String strDate;
	static String strName;
	static String strTitle;
	static String strMetaData;
	static String strLink;
	static String link = "https://hansard.parliament.uk/search/Debates";
	static Map<String, Integer> map;
	JsonGenerator jsonGenerator;
	JsonFactory jsonfactory;
	String outputDir = "";
	
	
	private void setDir() {
		// Instantiate JSON writer
		String output = outputDir + File.separator + "hansard.json";
		File streamFile = new File(output);
		jsonfactory = new JsonFactory();
		try {
			jsonGenerator = jsonfactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		map = new HashMap<String, Integer>();
		map.put("Both", 0);
		map.put("Commons", 1);
		map.put("Lords", 2);
	}

	
	void extractInfo() throws IOException{
		String url = "https://hansard.parliament.uk"+strLink;
		Element t = null;
		String body = "";
		try{

			Element ele = Jsoup.connect(url).timeout(120000).get().body().child(1).child(1).child(0).child(0).child(0).child(1);
			strTitle = ele.child(1).child(1).child(0).text();
			strDate = ele.child(2).text();
			strMetaData = ele.child(3).text();
			body = "";
			t = ele.child(4).child(0).child(1);
			boolean flag = false;
			if(t.select("li").size()==0) {
				body = t.child(5).child(1).text();
			}
			else {

				for(Element x : t.children()){
					if(flag){
						body = body + x.text();
						flag = false;
					}
					if(x.tagName().equals("li")) {
		
						String p = "";
						for (Element  y : x.getElementsByClass("inner").get(0).child(0).getElementsByTag("p"))
						{
							p = p + y.text();
						}
						if(p.trim().compareTo("")==0){
							flag = true;
						} else{
						}
						body = body + p;
					}
				}
			}
			
		}
		catch(Exception e) {

			body = t.text();
		}
		jsonGenerator.writeStartObject();
		jsonGenerator.writeStringField("Title", strTitle); 
		jsonGenerator.writeStringField("Date", strDate); 
		jsonGenerator.writeStringField("MetaData", strMetaData);
		jsonGenerator.writeStringField("Body", body);
		jsonGenerator.writeEndObject();
		
	}
	
	public void crawl(String outputDir, String searchTerm, String house, String startDate, String endDate, IProgressMonitor monitor) throws IOException{
		this.outputDir = outputDir;
		setDir();
		Elements elements = null;
		Connection conn = Jsoup.connect("https://hansard.parliament.uk/search/Debates").data("SearchTerm",searchTerm).data("HouseFilter",map.get(house)+"").data("StartDate",startDate).data("EndDate",endDate);
		Element e = conn.timeout(120000).post().body().child(1).child(1).child(0).child(0).child(2);
		String s = e.child(0).child(0).child(0).text();
		int numberOfPages = Integer.parseInt(s.substring(s.indexOf("(")+1, s.indexOf(")")).split(" ")[3]);
		System.out.println(s);
		int progressMonitorIncrement = 20000/numberOfPages;
		System.out.println("number of pages "+numberOfPages);
		System.out.println("Increment "+progressMonitorIncrement);
		elements = e.child(1).children();
		for(Element element:elements) {
	
			try{
				strTitle = element.child(0).child(0).child(0).text();
				strMetaData = element.child(0).child(0).child(1).text();
				strLink = element.child(0).attr("href");
				extractInfo();
			} catch(Exception exception){
				System.out.println("Exception occurred");
			}
		}
		monitor.worked(progressMonitorIncrement);
		System.out.println(link);
		String updateLink = link;
		for(int i=2; i<=numberOfPages; i++)
		{
			System.out.println("Page " + i);
			link = updateLink + "?";
			if(!startDate.equals("")){
				link = link + "startDate="+startDate.substring(6)+"-"+startDate.substring(3,5)+"-"+startDate.substring(0,2)+"&&";
			}
			if(!endDate.equals(""))
				link = link + "endDate="+endDate.substring(6)+"-"+endDate.substring(3,5)+"-"+endDate.substring(0,2)+"&&";
			if(!house.equals("Both"))
				link = link + "house="+house+"&&";
			if(!searchTerm.equals(""))
				link = link + "searchTerm="+searchTerm+"&&";
			link = link + "page="+i+"&&";
			if(link.endsWith("&&"))
			{
				link = link.substring(0, link.length()-2);
			}
			conn = Jsoup.connect(link).timeout(120000);
			e = conn.get().body().child(1).child(1).child(0).child(0).child(2);
			
			//System.out.println(e);
			System.out.println(link);
			for(Element element:e.child(1).children()) {
				try{
					strTitle = element.child(0).child(0).child(0).text();
					strMetaData = element.child(0).child(0).child(1).text();
					strLink = element.child(0).attr("href");
					extractInfo();
				} catch(Exception exception){
					System.out.println("Exception occurred");
				}
			}
			monitor.worked(progressMonitorIncrement);
		}
		
		try {
			jsonGenerator.writeEndArray();
			jsonGenerator.flush();
			jsonGenerator.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
			
	}
	
}
