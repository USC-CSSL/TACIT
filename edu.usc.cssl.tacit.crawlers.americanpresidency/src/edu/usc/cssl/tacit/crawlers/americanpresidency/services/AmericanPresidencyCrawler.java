package edu.usc.cssl.tacit.crawlers.americanpresidency.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

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
			Connection conn = Jsoup.connect(url);
			conn.timeout(5000000);
			Document doc1 = conn.get();
			Element element = doc1.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1).child(5).child(0).child(2).child(0).child(1);
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("Date", strDate); 
			jsonGenerator.writeStringField("Name", strName); 
			jsonGenerator.writeStringField("Title", strTitle);
			String body = element.ownText();
			
			
			for (Element el : element.getElementsByTag("p"))
				body += el.text() + " \n ";
			jsonGenerator.writeStringField("Body", body);
			
			
			/*int index = body.indexOf(":");
			if(index == -1)
				index = body.indexOf(";"); //Because ; has been used on the website at certain places by mistake

			if(strTitle.toLowerCase().contains("debate")&&index!=-1) {	//If it's a debate, then the json content is broken down by speaker. -1 because some debates are not formatted well, hence not possible to break them.

				jsonGenerator.writeArrayFieldStart("Body");
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("Speaker",body.substring(0, index));
				jsonGenerator.writeStringField("Text", body.substring(index+1));
				jsonGenerator.writeEndObject();
				String speaker = ""; 
				for (Element el : element.getElementsByTag("p"))
				{
					if(index!=-1)
						speaker = body.substring(0, index);
					body = el.text();
					
					index = body.indexOf(":");
					if(index == -1)
						index = body.indexOf(";");
					
					jsonGenerator.writeStartObject();
					if(index == -1)//if no speaker for current statement, assume it's the previous one
					 {
						jsonGenerator.writeStringField("Speaker", speaker);
						jsonGenerator.writeStringField("Text", body);
					 }
					else{
						jsonGenerator.writeStringField("Speaker", body.substring(0, index));
						jsonGenerator.writeStringField("Text", body.substring(index+1));
					
					}
					jsonGenerator.writeEndObject();
				}
				jsonGenerator.writeEndArray();
			
			} else {
				jsonGenerator.writeArrayFieldStart("Body");
				for (Element el : element.getElementsByTag("p"))
					body += el.text() + " \n ";
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("Speaker","NA");
				jsonGenerator.writeStringField("Text", body);
				jsonGenerator.writeEndObject();
				jsonGenerator.writeEndArray();
			}
			*/
			jsonGenerator.writeEndObject();
			
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public boolean crawlSearch(String outputDir, String searchTerm1, String searchTerm2, String operator, Calendar from, Calendar to, String presidentName, String documentCategory, IProgressMonitor monitor) throws IOException{
		boolean flag = true;
		this.outputDir = outputDir;
		setDir();
		Elements elements = null;
		Connection conn = Jsoup.connect("http://www.presidency.ucsb.edu/ws/index.php").data("ty",documentCategory).data("pres",presidentName).data("includepres","1").data("includecampaign","1");

		int progressMonitorIncrement = 890;
			if(!searchTerm1.equals("")) {
				conn = conn.data("searchterm",searchTerm1).data("bool",operator).data("searchterm1",searchTerm2);
			}


			if(from!=null)
			{
				conn = conn.data("monthstart",months[from.get(Calendar.MONTH)]).data("daystart",days[from.get(Calendar.DATE)-1]).data("yearstart",from.get(Calendar.YEAR)+"").data("monthend",months[to.get(Calendar.MONTH)]).data("dayend",days[to.get(Calendar.DATE)-1]).data("yearend",to.get(Calendar.YEAR)+"");
				conn.timeout(5000000);
				Document e = conn.post();
				
				Element et = e.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1);
				
				if(!searchTerm1.equals(""))
					elements = et.child(2).child(0).children();
				else
					elements = et.child(1).child(0).children();
					
			}
			else
			{
				Element e = conn.post().body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1);
				conn.timeout(5000000);
				if(!searchTerm1.equals("")){
					elements = e.child(2).child(0).children();// This seems correct only when search term is present
				} else {
					elements = e.child(2).child(0).children();// else this is correct
				}
				
			}

			int number = 0;
			if(elements!=null&&elements.size()!=0)	//Needed when no results are found
			{
				progressMonitorIncrement = 116317/(elements.size());
				monitor.worked(progressMonitorIncrement);
				elements.remove(0);
				if(progressMonitorIncrement<0)
					progressMonitorIncrement = 1;
				for (Element element : elements)
				{

					try{ 
						
						strDate = element.child(0).text();
						strName = element.child(1).text();			
						strTitle = element.child(3).child(0).child(0).text();
						extractInfo(element.child(3).child(0).child(0).attr("href"));
						ConsoleView.printlInConsoleln("Writing Paper: "+strTitle);
						number +=1;
					}catch(Exception e){
						System.out.println("Exception handled successfully");
					}

					
					monitor.worked(progressMonitorIncrement);
				}
			}
			else
				flag = false;
			try {
				jsonGenerator.writeEndArray();
				jsonGenerator.flush();
				jsonGenerator.close();
				ConsoleView.printlInConsoleln(number + " paper(s) downloaded.");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return flag;
			
	}
	
	public boolean crawlBrowse(String outputDir, int month, String day, String year, String president, String documentCategory, IProgressMonitor monitor) throws IOException{
		boolean flag = true;
		int progressMonitorIncrement = 890;
		this.outputDir = outputDir;
		setDir();
		Connection conn = Jsoup.connect("http://www.presidency.ucsb.edu/ws/index.php").data("includecampaign","1").data("includepress","1").data("ty",documentCategory).data("pres",president);
		Elements elements;
			conn = conn.data("month",month==-1?"":months[month]).data("daynum",day).data("year",year);
			elements = conn.post().body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1).child(2).child(0).children();
		int number = 0;
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
					ConsoleView.printlInConsoleln("Writing Paper: "+strTitle);
					number += 1;
				}catch(Exception e){}

				monitor.worked(progressMonitorIncrement);
			}
		}
		else{
			flag = false;
		}
		
		try {
			jsonGenerator.writeEndArray();
			jsonGenerator.flush();
			jsonGenerator.close();

			ConsoleView.printlInConsoleln(number + " paper(s) downloaded.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return flag;
		
	}
}

/*


 
 
 
 
 
 
 
 
 
 
 
 
 
 */
