package edu.usc.cssl.tacit.crawlers.frontier.services;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class FrontierCrawl {
	JsonFactory jsonFactory;
	JsonGenerator jsonGenerator;
	IProgressMonitor monitor;
	
	/**
	 * This method crawls the static website for all the years from the current year. 
	 * @param dir
	 * @param domain
	 * @param limit
	 * @param monitor
	 */
	public void crawl(String dir, String domain, int limit, IProgressMonitor monitor, boolean[] jsonFilter){
		int downloadCount = 0;
		jsonFactory = new JsonFactory();
		this.monitor = monitor;
		File streamFile = new File(dir+File.separator+domain+".json");
		try {
			jsonGenerator = jsonFactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		int count = 1;
		int year = 2016;
		File f=null;
		while(true){
			if(limit!=-1 && count>limit)
				break;
			try {
				
				String name = FrontierConstants.site2Link.get(domain)+"."+year+"."+String.format("%05d", count);
				f = new File(dir+File.separator+name+".txt");
				String site = IFrontierConstants.BASE_URL+name+"/full";
				Document d = Jsoup.connect(site).get();
				Document docJournalAbstract = Jsoup.parse(d.body().child(2).child(4).child(0).child(1).child(1).child(0).child(0).child(0).child(1).toString());
				Elements title = docJournalAbstract.select("h1");
				Elements abs = docJournalAbstract.select("p");
				ConsoleView.printlInConsoleln("Writing topic: "+ Jsoup.parse(title.toString()).text());
				jsonGenerator.writeStartObject();
				if(jsonFilter[0])
					jsonGenerator.writeObjectField("title", Jsoup.parse(title.toString()).text());
				if(jsonFilter[1])
					jsonGenerator.writeObjectField("abstract_body", Jsoup.parse(abs.toString()).text());
				String abstractBody = d.body().child(2).child(4).child(0).child(1).child(1).child(0).child(0).child(0).child(2).toString();
				int i = abstractBody.indexOf("References");
				if(i != -1){
				String journalBody = abstractBody.substring(0, i);
				if(jsonFilter[2])
					jsonGenerator.writeObjectField("journal_body", Jsoup.parse(journalBody).text());
				if(jsonFilter[3])
					jsonGenerator.writeObjectField("references", Jsoup.parse(abstractBody.substring(i+10)).text());
				}else{
					if(jsonFilter[2])
						jsonGenerator.writeObjectField("journal_body", Jsoup.parse(abstractBody).text());
				}
				jsonGenerator.writeEndObject();
				count++;
				downloadCount++;
				monitor.worked(1);
	}catch(HttpStatusException e1){
		f.delete();
		if(e1.getStatusCode() == 412){
			count++;
			continue;
		}
		else if(count != 1){
			year--;
			count = 1; 
			continue;
		}
		else
			break;
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
		try {
			jsonGenerator.writeEndArray();
			jsonGenerator.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ConsoleView.printlInConsoleln(downloadCount+" file(s) downloaded for "+domain);
}
	
}
