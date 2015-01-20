package edu.usc.cssl.nlputils.plugins.supremeCrawler.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SupremeCrawler {
	String filter, url, outputDir;
	boolean truncate, downloadAudio;
	public SupremeCrawler(String filter, String outputDir, boolean truncate, boolean downloadAudio){
		this.filter = filter;
		this.outputDir = outputDir;
		this.truncate = truncate;
		this.downloadAudio = downloadAudio;
		// http://www.oyez.org/cases/2009?page=1
		// http://www.oyez.org/cases/2009?order=field_argument_value&sort=asc&page=1&0=
		this.url = "http://www.oyez.org"+filter+"?order=title&sort=asc";
	}
	
	public void looper() throws IOException{
		int lastIndex = 0;
		System.out.println(url);
		appendLog("Crawling "+url);
		Document doc = Jsoup.connect(url).timeout(10*1000).get();
		Elements pages = doc.getElementsByClass("pager-last");
		Element pageList;
		// Sometimes the pager element is absent
		if (!pages.isEmpty()){
			pageList = pages.get(0);
			Pattern pattern = Pattern.compile("page=([0-9]+)");
			Matcher matcher = pattern.matcher(pageList.toString());
			if(matcher.find()){
			//System.out.println(matcher.start());
			//System.out.println(matcher.group());
			//System.out.println(matcher.group());
			lastIndex = Integer.parseInt(matcher.group(1));
			}
		}
		//System.out.println(lastIndex);
		for (int i = 0;i<=lastIndex;i++){
			//System.out.println(url+"&page="+i);
			System.out.println("\nPage "+(i+1));
			appendLog("\nPage"+(i+1));
			crawl(url+"&page="+i);
		}
		
	}
	
	public void crawl(String url) throws IOException{
		Document doc = Jsoup.connect(url).timeout(10*1000).get();
		Element table = doc.select("tbody").get(0);
		Elements rows = table.select("tr");
		for (Element row:rows){
			//System.out.println(row.select("a").get(0).attr("href"));
			String contenturl = "http://www.oyez.org"+row.select("a").get(0).attr("href");
			//System.out.println(row.select("td").get(1).text().trim());
			String date = row.select("td").get(2).text().trim();
			// Skip if no argument date
			if(date.equals(""))
				continue;
			String filename = row.select("td").get(1).text().trim()+"_"+date.substring(6)+date.substring(0,2)+date.substring(3,5);
			System.out.println(contenturl+", "+filename);
			appendLog(contenturl);
			// Fixing the unhandled exception without cascading.
			try{
			getFiles(contenturl, filename);
			}catch (IOException e) {
				System.out.println("Error Accessing the URL "+contenturl);
				appendLog("Error Accessing the URL "+contenturl);
				e.printStackTrace();
			}
			//break;
		}
	}
	
	private void getFiles(String contenturl, String filename) throws IOException {
		File trans = new File(outputDir+"/"+filename+"-transcript.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(trans));
		
		Document doc = Jsoup.connect(contenturl).timeout(10*1000).get();
		
		Elements hidden = doc.select("div.hidden");
		if (hidden.size()==0){
			System.out.println("No data. Skipping page "+contenturl);
			appendLog("No data. Skipping page "+contenturl);
			bw.close();
			return;
		}
		
		//"-transcript.txt"
		System.out.println("Writing "+outputDir+"/"+filename+"-transcript.txt");
		appendLog("Writing "+outputDir+"/"+filename+"-transcript.txt");
		
		//Element transcript = doc.select("div.hidden").get(0);
		Element transcript = hidden.get(0);
		Elements lines = transcript.select("p");
		for (Element line:lines){
			//System.out.println(line.text());
			bw.write(line.text()+"\n");
		}
		bw.close();
		
		if(downloadAudio){
			//"-argument.mp3"
			Elements links = doc.select(".audio");
			for (Element mp3:links){
				if(mp3.attr("href").contains(".mp3")){
					System.out.println("Downloading "+"http://www.oyez.org"+mp3.attr("href"));
					appendLog("Downloading "+"http://www.oyez.org"+mp3.attr("href"));
					Response audio;
					if (!truncate)
						audio = Jsoup.connect("http://www.oyez.org"+mp3.attr("href")).cookie("oyez-tos","1.0").maxBodySize(0).ignoreContentType(true).execute();
					else
						audio = Jsoup.connect("http://www.oyez.org"+mp3.attr("href")).cookie("oyez-tos","1.0").ignoreContentType(true).execute();
					FileOutputStream fos = new FileOutputStream(new File(outputDir+"/"+filename+"-argument.mp3"));
					fos.write(audio.bodyAsBytes());
					fos.close();
					break; // Once mp3 found, no need to continue for loop
				}
			}
		}
		
	}
	
	public static String[] filters(String view){
		String[] list = null;
		try {
			Document doc = Jsoup.connect("http://www.oyez.org/"+view).get();
			Element itemList = doc.select(".exmenu").get(0);
			//System.out.println(itemList);
			Elements items = itemList.select("a");
			System.out.println(items.size());
			list = new String[items.size()+1];
			list[0] ="All";
			for (int i = 1; i<= items.size(); i++){
				Element item = items.get(i-1);
				list[i] = item.attr("href").trim();
				System.out.println(list[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void main(String[] args) {
		filters("cases");
		//SupremeCrawler sc = new SupremeCrawler("cases","","title","asc","/Users/aswinrajkumar/Desktop/supreme/");
		//http://www.oyez.org/issues/criminal_procedure/confrontation/confession_error
		SupremeCrawler sc = new SupremeCrawler("/issues/criminal_procedure/confrontation/confession_error","/Users/aswinrajkumar/Desktop/Stupidoutput/Output",true, true);
		try {
			//sc.looper();
			//sc.crawl("http://www.oyez.org/cases/2010-2019?page=3");
			//sc.getFiles("http://www.oyez.org/cases/2000-2009/2007/2007_07_21", "somefile");
			sc.getFiles("http://www.oyez.org/cases/1960-1969/1962/1962_124", "somefile");
			//sc.crawl("http://www.oyez.org/issues/?order=title&sort=asc&page=142");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");
	}
	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		if (context == null)
			return;
		IEclipseContext parent = context.getParent();
		String currentMessage = (String) parent.get("consoleMessage"); 
		if (currentMessage==null)
			parent.set("consoleMessage", message);
		else {
			if (currentMessage.equals(message)) {
				// Set the param to null before writing the message if it is the same as the previous message. 
				// Else, the change handler will not be called.
				parent.set("consoleMessage", null);
				parent.set("consoleMessage", message);
			}
			else
				parent.set("consoleMessage", message);
		}
	}
	

}
