package edu.usc.cssl.tacit.crawlers.wikipedia.services;
import java.io.File;
/**
* This class creates a class for crawling a certain item. 
* It integrates with all necessary parameters that are
* required by class Crawler and Categorizer. 
*
* @author  Yongshun
* @version 1.0
* @since   2016-05-01 
*/
import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;

import org.eclipse.core.runtime.IProgressMonitor;

public class Mode1 {
	private boolean returnCategory;
	private boolean returnContent;
	private int filterStrategy;
	private String itemName;
	private String accessPoint="https://en.wikipedia.org/wiki/";
	private String filePath;
	
	public Mode1(String itemName, boolean returnContent, boolean returnCategory, int filterStrategy,String filePath) {
		this.itemName=itemName;
		this.returnCategory=returnCategory;
		this.returnContent=returnContent;
		this.filterStrategy=filterStrategy;
		this.filePath=filePath;
	}
	
	private	String Format(String s) {
		return s.replace((char)8211, (char)45).replaceAll("\\n", "").replaceAll("\"", "\\\\\"").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\{", "").replaceAll("\\}", "");
	}
	
	public String GetJson() {
		System.out.println("Crawling Started.");
		JsonWriter w=new JsonWriter();
		Crawler crawler=new Crawler(accessPoint+itemName);
		String isValid;
		if (crawler.valid)
			isValid="true";
		else
			isValid="false";
		w.writeString("{\"name\":\"");
		w.write(itemName);
		w.writeString("\",\"valid:\":"+isValid);
		if (crawler.valid) {
			w.writeString("\",\"url:\":\"");
			w.write(accessPoint+itemName.replace((char)8211, (char)45).replace(' ', '_')+"\"");
			if (returnContent) {
				String content;
				if (filterStrategy==1)
					content=crawler.text();
				else
					content=crawler.highValueText();
				w.writeString(",\"content\":\"");
				w.write(Format(content));
				w.writeString("\"");
			}
			if (returnCategory) {
				Categorizer categorizer=new Categorizer(itemName,500);
				w.writeString(",\"category\":");
				if (categorizer.number>0) {
					w.writeString("[{\"category_name\":\"");
					w.write(Format(categorizer.category.get(0)));
					w.writeString("\"}");
					for(int i=1;i<categorizer.number;i++) {
						w.writeString(",{\"category_name\":\"");
						w.write(Format(categorizer.category.get(i)));
						w.writeString("\"}");
					}
					w.writeString("]");
				}
				else w.writeString("null");

			}
		}
		w.writeString("}");
		System.out.println("Crawling Completed.");
		return w.toString();
	}
	
	public void Write() {
		FileWriter file;
		try {
			file = new FileWriter(filePath+File.separator+"Page-"+itemName+".json");
			file.write(GetJson());
			file.flush();
	        file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
