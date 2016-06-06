package edu.usc.cssl.tacit.crawlers.americanpresidency.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.internal.runtime.Log;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class AmericanPresidencyCrawler {
	static String strDate;
	static String strName;
	static String strTitle;
	JsonGenerator jsonGenerator;
	JsonFactory jsonfactory;
	String outputDir = "";
	String months[]={"01","02","03","04","05","06","07","08","09","10","11","12"};
	String days[]={"01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};

	private void setDir() {
		// TODO Auto-generated constructor stub
		// Instantiate JSON writer
		String output = outputDir + File.separator + "americanpresidency.json";
		File streamFile = new File(output);
		jsonfactory = new JsonFactory();
		try {
			jsonGenerator = jsonfactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	void extractInfo(String link)throws Exception{
		try{
			String url = "http://www.presidency.ucsb.edu/ws/"+link;
			Document doc1 = Jsoup.connect(url).get();
			Element element = doc1.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1).child(5).child(0).child(2).child(0).child(1);
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("Date", strDate); 
			jsonGenerator.writeStringField("Name", strName); 
			jsonGenerator.writeStringField("Title", strTitle);
			String body = element.text();
			
			for (Element el : element.getElementsByTag("p"))
			{
				body += el.text();
			}
			jsonGenerator.writeStringField("Body", body);
			jsonGenerator.writeEndObject();
		}
		catch(Exception e){
			throw e;
		}
	}
	
	public void crawlSearch(String outputDir, String searchTerm1, String searchTerm2, String operator, Calendar from, Calendar to, String presidentName, String documentCategory, boolean flag1, boolean flag2, IProgressMonitor monitor) throws IOException{
		this.outputDir = outputDir;
		System.out.println("--------------"+outputDir);
		setDir();
		Elements elements = null;
		Connection conn = Jsoup.connect("http://www.presidency.ucsb.edu/ws/index.php").data("searchterm",searchTerm1).data("bool",operator).data("searchterm1",searchTerm2).data("ty",documentCategory).data("pres",presidentName);
		int progressMonitorIncrement = 890;
		if(flag1)
			conn = conn.data("includepress","1");
		if(flag2)
			conn = conn.data("includecampaign","1");
		if(from!=null)
		{
			conn = conn.data("monthstart",months[from.get(Calendar.MONTH)]).data("daystart",days[from.get(Calendar.DATE)-1]).data("yearstart",from.get(Calendar.YEAR)+"").data("monthend",months[to.get(Calendar.MONTH)]).data("dayend",days[to.get(Calendar.DATE)-1]).data("yearend",to.get(Calendar.YEAR)+"");
			Element e = conn.post().body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1);
			//System.out.println(e);
			elements = e.child(1).child(0).children();
		}
		else
		{
			Element e = conn.post().body().child(0).child(0).child(1).child(0).child(0).child(0).child(0);
			elements = e.child(1).child(1).child(0).children();
		}
		int i = 1;
		if(elements!=null&&elements.size()!=0)	//Needed when no results are found
		{
			progressMonitorIncrement = 980/(elements.size());
			elements.remove(0);
			
			for (Element element : elements)
			{
				try{
					strDate = element.child(0).text();
					strName = element.child(1).text();			
					strTitle = element.child(3).child(0).child(0).text();
					extractInfo(element.child(3).child(0).child(0).attr("href"));
					
				}catch(Exception e){
					System.out.println("Exception occurred");
				}
				//Monitor only accepts integer increments, but progressMonitorIncrement could have taken a double value
				if (i%4==0)
					monitor.worked(progressMonitorIncrement+1);
				else
					monitor.worked(progressMonitorIncrement);
				i++;
			}
			//monitor.
		}
		try {
			jsonGenerator.writeEndArray();
			jsonGenerator.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(i+"---------------"+elements.size());
	}
	
	public void crawlBrowse(String outputDir, Calendar date, String president, String documentCategory, boolean flag1, boolean flag2, IProgressMonitor monitor) throws IOException{
		int progressMonitorIncrement = 890;
		this.outputDir = outputDir;
		setDir();
		Connection conn = Jsoup.connect("http://www.presidency.ucsb.edu/ws/index.php").data("ty",documentCategory).data("pres",president);
		Elements elements;
		if(flag1)
			conn = conn.data("includepress","1");
		if(flag2)
			conn = conn.data("includecampaign","1");
		if (date!=null)
		{
			conn = conn.data("month",months[date.get(Calendar.MONTH)]).data("daynum",days[date.get(Calendar.DATE)-1]).data("year",date.get(Calendar.YEAR)+"");
			elements = conn.post().body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1).child(2).child(0).children();

		}
		else{
			Element e = conn.post().body().child(0).child(0).child(1).child(0).child(0).child(0).child(0);
			elements = e.child(1).child(1).child(0).children();
		}
		int i=1;
		if(elements!=null&&elements.size()!=0)	//Needed when no results are found
		{
			elements.remove(0);
			
			for (Element element : elements)
			{
				strDate = "";
				try{
					strDate = element.child(0).text();
					strName = element.child(1).text();			
					strTitle = element.child(3).child(0).child(0).text();
					extractInfo(element.child(3).child(0).child(0).attr("href"));
				}catch(Exception e){}

				//Monitor only accepts integer increments, but progressMonitorIncrement could have taken a double value
				if (i%4==0)
					monitor.worked(progressMonitorIncrement+1);
				else
					monitor.worked(progressMonitorIncrement);
				i++;
			}
		}
		
		try {
			jsonGenerator.writeEndArray();
			jsonGenerator.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}


//1000
//-10 At start
//890 divided by number of results crawled
//-100 At the end