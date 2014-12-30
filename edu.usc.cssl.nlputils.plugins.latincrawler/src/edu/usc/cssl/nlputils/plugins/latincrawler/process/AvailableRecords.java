/**
 * @author Niki Parmar <nikijitp@usc.edu>
 */
package edu.usc.cssl.nlputils.plugins.latincrawler.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AvailableRecords {
	
	public static void main(String[] args) throws IOException{
		getAllAuthors();
	}
	
	public static String[] getAllAuthors() throws IOException{
			int i, size = 0;
		
			Document doc = Jsoup.connect("http://www.thelatinlibrary.com/").timeout(10*1000).get();
			Elements authorsList = doc.getElementsByTag("option");
			System.out.println(authorsList);
			size = authorsList.size();
			String[] authorString = new String[size];
			for(i =0;i<size;i++)
			{
				authorString[i] = authorsList.get(i).attr("abs:href");
			}
			//String authorsString = " All|"+authorsList.text().split(":")[1];
			return authorString;
	}
	

	
	/*public static String[] getAllBooks(String[] authors) throws IOException{
		TreeSet<String> books = new TreeSet<String>();
		for (String cong : authors){
			String[] temp = getBooks(cong.trim());
			for (String senator : temp){
				if (senator.equals("Any Senator"))
					continue;
				senators.add(senator);
			}
		}
		String[] senatorArray = new String[senators.size()];
		senators.toArray(senatorArray);
		return senatorArray;
	}*/
}
