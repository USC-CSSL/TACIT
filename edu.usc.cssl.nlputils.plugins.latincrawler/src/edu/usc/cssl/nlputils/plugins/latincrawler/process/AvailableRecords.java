/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
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
				authorString[i] = authorsList.get(i).text();
			}
			//String authorsString = " All|"+authorsList.text().split(":")[1];
			return authorString;
	}
	
	public static String[] getBooks(String congressString) throws IOException {
		congressString = congressString.replace("\u00A0", "");
		int congress = Integer.parseInt(congressString);
		System.out.println("Extracting Senators of Congress "+congress);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congress).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		String[] senArray = new String[senList.size()-1];
		int index = 0;
		for (Element senItem : senList){
			String senText = senItem.text().replace("\u00A0", " ");
			if (senText.equals("Any Senator"))
				continue;
			senArray[index++] = senText;
		}
		return senArray;
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
