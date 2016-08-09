package edu.usc.cssl.tacit.crawlers.uscongress.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class RecordCongressCrawl {
	static JsonGenerator jsonGenerator;
	static JsonFactory jsonfactory;
	static int docCount;
	static boolean first =true;
	
	public static void main(String args[]){
	 try {
		crawl(null, 10, "", "", "114", false);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }
 
 static void crawl(String outputDir, int limit, String member, String search, String congress, boolean random) throws IOException{
			File streamFile = new File("congressCrawl.json"); 
			jsonfactory = new JsonFactory();
			jsonGenerator = jsonfactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
			String houseRemark = "";
			if(!member.equals("")){
				member = member.replaceAll(" ", "+");
				houseRemark = ",\"crHouseMemberRemarks\":\""+member+"\"";
			}
			String query = "";
			if(!search.equals("")){
				query = ",\"search\":\""+search+"\"";
			}
			docCount= 0;
			int page = 1;
			int totalPages = 0;
			BufferedWriter bw;
			Document d = null;
			boolean connected = false;
			while (true) {
				connected = false;
				if(random)
					page = (int) (Math.random()*25);
				String site = "https://www.congress.gov/search?q={\"source\":\"congrecord\",\"congress\":\"" + congress + "\",\"chamber\":\"House\""+ houseRemark + query +"}&pageSize=25&page=" + page;
				while (!connected) {
					try {
						d = Jsoup.connect(site).timeout(50000).get();
						connected = true;
					} catch (Exception e) {
						System.out.println(e);
						continue;
					}
				}
				
				if(first){
				Elements number = d.getElementsByClass("results-number");
				
				String num = Jsoup.parse(number.toString()).text();
				int results = Integer.parseInt(num.substring(num.indexOf("of")+3).replaceAll(",", ""));
				System.out.println(num+"------"+results);
				totalPages = results/25;
				first = false;
				}
				Elements title = d.getElementsByClass("results_list");
				Elements links = title.select("h2").select("a");
				for (Element link : links) {
				 	
					String data = link.toString();
					int start = data.indexOf("=\"");
					int end = data.indexOf("resultIndex=");
					Elements docJournalAbstract = null;
					String contentLink = data.substring(start + 2, end-1);
					System.out.println(contentLink);
					String dateText;
					Document doc;
						try {
							doc = Jsoup.connect(contentLink).timeout(4000).get();
							docJournalAbstract =  doc.getElementsByClass("txt-box");
							Elements dates = doc.getElementsByClass("wrapper_std");
							jsonGenerator.writeStartObject();
							for(Element date: dates){
								dateText = Jsoup.parse(date.child(1).select("span").toString()).text();
								jsonGenerator.writeStringField("date", dateText);
								String title1 = (date.child(1)).toString().substring(4,date.child(1).toString().indexOf("<br"));
								jsonGenerator.writeStringField("title", title1);
							}
							System.out.println(Jsoup.parse(docJournalAbstract.toString()).text());
							jsonGenerator.writeStringField("body", Jsoup.parse(docJournalAbstract.toString()).text());
							jsonGenerator.writeEndObject();
						} catch (SocketTimeoutException e) {
							continue;
						}catch (Exception e) {
							continue; 
						}
					docCount++;
					if (limit!= -1 && docCount >= limit){					
						break;
					}
				}
				if(random)
					page = (int) (Math.random()*totalPages+1);
				else
					page++;
				if (page>totalPages || docCount >= limit)
					break;
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.flush();
			jsonGenerator.close();
		
 }
}
