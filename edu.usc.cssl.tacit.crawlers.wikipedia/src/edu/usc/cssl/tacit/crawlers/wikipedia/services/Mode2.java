package edu.usc.cssl.tacit.crawlers.wikipedia.services;
/**
* This class creates a class for searching about a keyword. 
* It integrates with all parameters that are needed by Searcher.
* It provides several easy-understanding methods to do the crawling.
*
* @author  Yongshun
* @version 1.0
* @since   2016-05-01 
*/

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

public class Mode2 {
	private boolean returnCategory;
	private boolean returnContent;
	private int filterStrategy;
	private String keyword;
	private int limit;
	private String accessPoint="https://en.wikipedia.org/wiki/";
	private String filePath;
	IProgressMonitor monitor;
	
	public Mode2(String keyword, boolean returnContent, boolean returnCategory, int filterStrategy,int limit, String filePath, IProgressMonitor monitor) {
		this.keyword=keyword;
		this.monitor = monitor;
		this.returnCategory=returnCategory;
		this.returnContent=returnContent;
		this.filterStrategy=filterStrategy;
		this.limit=limit;
		this.filePath=filePath;
	}
	
	public String GetJson() {
		System.out.println("Search Started.");
		JsonWriter w=new JsonWriter();
		Searcher s=new Searcher(keyword,limit);
		w.writeString("{\"result_number\":");
		w.writeString(Integer.toString(s.number));
		w.writeString(",\"search_result\":");
		if (s.number>0) {
			w.writeString("[");
			for(int i=0;i<s.number;i++) {
				if (i>0)
					w.writeString(",");
//				System.out.println(s.title.get(i));
				Crawler crawler=new Crawler(accessPoint+s.title.get(i));
				String isValid;
				if (crawler.valid)
					isValid="true";
				else
					isValid="false";
				w.writeString("{\"result_name\":\"");
				w.write(s.title.get(i).replace((char)8211, (char)45));
				w.writeString("\",\"valid\":\""+isValid);
				if (crawler.valid) {
					w.writeString("\",\"url\":\"");
					w.write(accessPoint+s.title.get(i).replace((char)8211, (char)45).replace(' ', '_')+"\"");
					if (returnContent) {
						String content;
						if (filterStrategy==1)
							content=crawler.text();
						else
							content=crawler.highValueText();
						w.writeString(",\"result_content\":\"");
						w.write(Format(content).replace((char)8211, (char)45));
						w.writeString("\"");
					}
					if (returnCategory) {
						Categorizer categorizer=new Categorizer(s.title.get(i),500);
						w.writeString(",\"result_category\":");
						if (categorizer.number>0) {
							w.writeString("[{\"category_name\":\"");
							w.write(Format(categorizer.category.get(0)));
							w.writeString("\"}");
							for(int j=1;j<categorizer.number;j++) {
								w.writeString(",{\"category_name\":\"");
								w.write(Format(categorizer.category.get(j)));
								w.writeString("\"}");
							}
							w.writeString("]");
						}
						else w.writeString("null");

					}
				}
				w.writeString("}");
				System.out.printf("\tSearch Result No.%4d Completed.\n",i+1);
				monitor.worked(i+1);
			}
			w.writeString("]}");
		}
		else {
			w.writeString("null}");
		}
		System.out.println("Search Completed.");
		return w.toString();
	}
	
	public void Write() {
		FileWriter file;
		try {
			file = new FileWriter(filePath+File.separator+"Search_Results-"+keyword+".json");
			file.write(GetJson());
			file.flush();
	        file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private	String Format(String s) {
		return s.replaceAll("\\n", "").replaceAll("\"", "\\\\\"").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\{", "").replaceAll("\\}", "");
	}
}
