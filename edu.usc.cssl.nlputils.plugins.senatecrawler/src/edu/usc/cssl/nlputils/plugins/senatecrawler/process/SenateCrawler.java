/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */

package edu.usc.cssl.nlputils.plugins.senatecrawler.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.nlputils.utilities.Log;

public class SenateCrawler {
	private StringBuilder readMe = new StringBuilder();
	ArrayList<Integer> congresses = new ArrayList<Integer>();
	String dateFrom, dateTo;
	int maxDocs = 10;
	String outputDir;
	BufferedWriter csvWriter;
	HashSet<String> irrelevantLinks = new HashSet<String>(Arrays.asList("Next Document","New CR Search","Prev Document","HomePage","Help","GPO's PDF"));
	private String senText;
	private int congressNum;
	//Feinstein test
	//private boolean Feinstein = false;
	
	public void initialize(int maxDocs, String dateFrom, String dateTo, String outputDir) throws IOException{
		this.outputDir = outputDir;
		this.maxDocs = maxDocs;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		checkPath(outputDir);
		
		csvWriter  = new BufferedWriter(new FileWriter(new File(outputDir + System.getProperty("file.separator") + "records.csv")));
		csvWriter.write("Congress,Date,Senator,Attributes,Title,File");
		csvWriter.newLine();
		getCongresses();
		for (int congress : congresses){
			getSenators(congress);
			//break;	// Remove to unleash
		}
		csvWriter.close();
	}
	
	private void checkPath(String outputDir) {
		File outputPath = new File(outputDir);
		if (!outputPath.exists()){
			outputPath.mkdirs();
		}
	}

	public void crawl() throws IOException{
		checkPath(outputDir);
		appendLog("Crawling...");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String dateString = dateFormat.format(date);
		
		csvWriter  = new BufferedWriter(new FileWriter(new File(outputDir + System.getProperty("file.separator") + "records_"+dateString+".csv")));
		csvWriter.write("Congress,Date,Senator,Attributes,Title,File");
		csvWriter.newLine();
		if (senText.equals("All Senators") || senText.equals("All Republicans") || senText.equals("All Democrats") || senText.equals("All Independents")){
			if (congressNum != -1)
				getAll(congressNum,senText);
			else {
				getCongresses();
				for (int congress:congresses)
					getAll(congress,senText);
			}
		} 
		
		else {
			if (congressNum == -1){
				getCongresses();
				for (int congress: congresses){
					System.out.println("Extracting Records from Congress "+congress+"...");
					appendLog("Extracting Records from Congress "+congress);
					searchSenatorRecords(congress,senText);
				}
			} else {
				System.out.println("Extracting Records from Congress "+congressNum+"...");
				appendLog("Extracting Records from Congress "+congressNum);
				searchSenatorRecords(congressNum,senText);
			}
		}
		csvWriter.close();
		appendLog("Records written successfully to "+outputDir + System.getProperty("file.separator") + "records_"+dateString+".csv");
		writeReadMe(outputDir+"/README_"+dateString+".txt");
	}
	public void getAll(int congressNum, String senText) throws IOException{
		System.out.println("Extracting Senators of Congress "+congressNum+"...");
		appendLog("Extracting Senators of Congress "+congressNum);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congressNum).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		
		for (Element senItem : senList){
			String senator = senItem.text().replace("\u00A0", " ");
			/*Feinstein test*/
			//if (senator.contains("Feinstein"))
			//	Feinstein = true;
			//if (!Feinstein)
			//	continue;

			if (senator.contains("Any Senator"))		// We just need the senator names
				continue;
			if (senText.contains("All Republicans")){
				if (!senator.contains("(R-"))
					continue;
			}
			if (senText.contains("All Democrats")){
				if (!senator.contains("(D-"))
					continue;
			}
			if (senText.contains("All Independents")){
				if (!senator.contains("(I-"))
					continue;
			}
			searchSenatorRecords(congressNum,senator);
			//System.out.println(congressNum+" - "+senator);
		}
	}
	
	public void initialize(int maxDocs, int congressNum, String senText, String dateFrom, String dateTo, String outputDir) {
		this.outputDir = outputDir;
		this.maxDocs = maxDocs;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.senText = senText;
		this.congressNum = congressNum;
	}
	
	private void getSenators(int congress) throws IOException {
		System.out.println("Extracting Senators of Congress "+congress+"...");
		appendLog("Extracting Senators of Congress "+congress);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congress).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		
		for (Element senItem : senList){
			String senText = senItem.text().replace("\u00A0", " ");
			if (senText.contains("Any Senator"))		// We just need the senator names
				continue;
			//System.out.println(senatorName+" "+senatorAttribs);
			searchSenatorRecords(congress,senText);
			//break;	// Remove to unleash
		}
	}

	private void getCongresses() throws IOException {
		appendLog("Extracting available congresses...");
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record").timeout(10*1000).get();
		Elements congList = doc.select("p.nav");
		//System.out.println(congList);
		String[] congressNumbers = congList.text().split(":")[1].split("\\|");
		for (String cNum : congressNumbers){
			cNum = cNum.replaceAll("\u00A0", "");			// Invisible &nbsp; characters
			congresses.add(Integer.parseInt(cNum.trim()));
		}
		System.out.println(congresses);
	}

	public void searchSenatorRecords(int congress,String senText) throws IOException{
		System.out.println("Current Senator - "+senText);
		Document doc = Jsoup.connect("http://thomas.loc.gov/cgi-bin/thomas2")
				.data("xss","query")		// Important. If removed, "301 Moved permanently" error
				.data("queryr"+congress,"")	// Important. 113 - congress number. Make this auto? If removed, "Database Missing" error
				.data("MaxDocs","2000")		// Doesn't seem to be working
				.data("Stemming","No")
				.data("HSpeaker","")
				.data("SSpeaker",senText)
				.data("member","speaking")	// speaking | all  -- all occurrences
				.data("relation","or")		// or | and  -- when there are multiple speakers in the query
				.data("SenateSection","1")
				//.data("HouseSection","2")
				//.data("ExSection","4")
				//.data("DigestSection","8")
				.data("LBDateSel","Thru")		// "" | 1st | 2nd | Thru -- all sessions, 1st session, 2nd session, range
				.data("DateFrom",dateFrom)
				.data("DateTo",dateTo)
				.data("sort","Date")		// Default | Date
				.data("submit","SEARCH")
				.userAgent("Mozilla")
				.timeout(10*1000)
				.post();
		Elements links = doc.getElementById("content").getElementsByTag("a");
		
		
		// Extracting the relevant links
		Elements relevantLinks = new Elements();
		for (Element link:links){
			if (!irrelevantLinks.contains(link.text()))
				if (link.text().contains("Senate"))
					relevantLinks.add(link);
		}
		//System.out.println(relevantLinks);
		//System.out.println(relevantLinks.size());
		
		if (relevantLinks.size() == 0){
			System.out.println("No Records Found.");
			return;
		}
		
		links = relevantLinks;
//		if (links.size()>6){
//		// Removing unnecessary links
//		links.remove(0);
//		links.remove(0);
//		links.remove(0);
//		
//		// Remove the bottom links that pop up when the number of rows is above 20
//		if (links.size()>20){
//			links.remove(links.size()-1);
//			links.remove(links.size()-1);
//			links.remove(links.size()-1);
//		}
//		} else {
//			System.out.println("No records found.");
//			return;
//		}
		String senatorName = senText.split("\\(")[0].trim();
		String senatorAttribs = senText.split("\\(")[1].replace(")", "").trim();
		
		int count = 0;
		// Process each search result
		for (Element link : links){
			// Max Docs for each senator
			if (maxDocs==-1)
				count=-2000;
			if (count++>=maxDocs)
				break;
			String recordDate = link.text().replace("(Senate - ", "").replace(",", "").replace(")", "").trim();
			System.out.println(recordDate);
			System.out.println("Processing "+link.text());
			Document record = Jsoup.connect("http://thomas.loc.gov"+link.attr("href")).timeout(10*1000).get();
			Elements tabLinks = record.getElementById("content").select("a[href]");
			
			String extractLink="";
			// Find Printer Friendly Display
			for (Element tabLink:tabLinks){
				if (tabLink.text().equals("Printer Friendly Display")){
					extractLink = tabLink.attr("href");
					break;
				}
			}
			
			String lastName = senText.split(",")[0];
			String[] contents = extract(extractLink,lastName);

			if (contents[1].length()==0)
				count--;
			else{
				String[] split = contents[0].split("-");
				String title = split[0].trim();
				title = title.replaceAll(",", "");
				title = title.replaceAll("\\.", "");
				String shortTitle = title;
				if (title.length()>15)
					shortTitle = title.substring(0, 15).trim().replaceAll("[^\\w\\s]", "");
//				String date = contents[0].split("\\(Senate - ")[1].trim();
//				date = date.replace(",", "");
//				date = date.replace(")", "");
//				System.out.println(date);
//				if (date==null || date.isEmpty())
//					System.out.println("date is empty");
//				date = date.split(" ")[0]+" "+date.split(" ")[1]+" "+date.split(" ")[2];
//				date = date.trim();
				String fileName = congress+"-"+lastName+"-"+senatorAttribs+"-"+recordDate+"-"+shortTitle+"-"+(System.currentTimeMillis()%1000)+".txt";
				writeToFile(fileName, contents);
				csvWriter.write(congress+","+recordDate+","+lastName+","+senatorAttribs+","+title+","+fileName);
				csvWriter.newLine();
				csvWriter.flush();
			}
			//break;
		}
	}

	private void writeToFile(String fileName, String[] contents) throws IOException {
		System.out.println("Writing senator data - "+fileName);
		appendLog("Writing senator data - "+fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDir+System.getProperty("file.separator")+fileName)));
		bw.write(contents[0]);
		bw.newLine();
		bw.newLine();
		bw.write(contents[1]);
		bw.close();
	}

	private String[] extract(String extractLink, String lastName) throws IOException {
		Document page = Jsoup.connect("http://thomas.loc.gov"+extractLink).timeout(10*1000).get();
		//System.out.println(page.getElementById("container"));
		//System.out.println(page.getElementById("container").text());
		String title = page.getElementById("container").select("b").text();
		StringBuilder content = new StringBuilder();
		Elements lines = page.getElementById("container").select("p");
		String currentLine;
		boolean extractFlag = false;
		for (Element line : lines) {
			currentLine = line.text();
			if (currentLine!=null && !currentLine.isEmpty()){
				String[] words = currentLine.split(" ");
				if (words.length>1){
					String currentName = words[1].trim().replace(".", "");	// Check the second word of the sentence.
					currentName = currentName.replace(",", "");
					String firstWord = words[0].trim().replace(".", "");
					
					if (currentName.equals(lastName.toUpperCase())) {
						// Found senator dialogue
						extractFlag = true;
						content.append(currentLine.replace("\u00A0", "").trim()+"\n");
					} else {
						// If first word is uppercase too, stop extracting.
						if (firstWord.length()<=1 && !firstWord.equals(firstWord.toUpperCase()) ){
							extractFlag = false;
						}
						// If "I", continue extracting. 
					if (!currentName.equals("I") && !isNumeric(currentName) && currentName.equals(currentName.toUpperCase())){
						// Next speaker.
						extractFlag = false;
					}
					// if already extracting, continue until end of file or until next speaker's dialogue
					if (extractFlag)
						content.append(currentLine.replace("\u00A0", "").trim()+"\n");
					}
				}
			}
		}
		
		//System.out.println(title);
		//System.out.println(content.toString());
		

		String[] contents = new String[2];
		contents[0] = title;
		contents[1] = content.toString();
		
		return contents;
	}
	
	private boolean isNumeric(String word){
		try{
			Integer.parseInt(word);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	

	@Inject IEclipseContext context;	
	private void appendLog(String message){
		Log.append(context, message);
	}
	
	public void writeReadMe(String location){
		File readme = new File(location);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform.getBundle("edu.usc.cssl.nlputils.plugins.senatecrawler").getHeaders().get("Bundle-Version");
			String appV = Platform.getBundle("edu.usc.cssl.nlputils.application").getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("READ ME\n-------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
