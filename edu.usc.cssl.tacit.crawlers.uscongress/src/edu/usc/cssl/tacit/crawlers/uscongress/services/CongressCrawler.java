package edu.usc.cssl.tacit.crawlers.uscongress.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CongressCrawler {

	static int docCount;
	boolean first =true;

	public void crawl(String outputDir, int limit, String party, String congress, IProgressMonitor monitor, boolean random ) throws IOException{
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
			String site = "https://www.congress.gov/search?q={\"source\":\"legislation\",\"party\":\"" + party
					+ "\",\"congress\":\"" + congress + "\",\"type\":[\"bills\",\"resolutions\",\"concurrent-resolutions\",\"joint-resolutions\"]}&pageSize=250&page=" + page;
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
			if(limit==-1){
				monitor.beginTask("Running US Congress Crawler...", results+30);
				monitor.worked(10);
				monitor.subTask("Crawling...");
				if(results>10000)
					limit = results-1000;
				else
					limit = results -2000;
				totalPages = results/250;
			}
			first = false;
			}
			Elements title = d.getElementsByClass("results_list");
			Elements links = title.select("h2").select("a");
		 tag:	for (Element link : links) {
				bw = new BufferedWriter(new FileWriter(new File(outputDir+File.separator+"congressCrawl" + docCount)));
				String data = link.toString();
				int start = data.indexOf("=\"");
				int end = data.indexOf("?resultIndex=");
				Document docJournalAbstract = null;
				String contentLink = data.substring(start + 2, end);
				Document doc;
				while (true) {
					try {
						doc = Jsoup.connect(contentLink + "/text?format=txt").timeout(4000).get();
						docJournalAbstract = Jsoup.parse(doc.body().child(1).child(1).child(1).child(5).toString());
						break;
					} catch (SocketTimeoutException e) {
						continue;
					}catch (Exception e) {
						continue tag; 
					}
				}
				if (doc.body().child(1).child(1).child(1).child(5).childNodeSize() > 6) {
					Element element = docJournalAbstract.getElementById("billTextContainer");
					bw.write(Jsoup.parse(element.toString()).text());
				} else {
					continue;
				}
				docCount++;
				monitor.worked(1);
				if (docCount >= limit)
					break;
				bw.flush();
				bw.close();
			}
			if(random)
				page = (int) (Math.random()*totalPages+1);
			else
				page++;
			if (docCount >= limit)
				break;
		}
	}
}
