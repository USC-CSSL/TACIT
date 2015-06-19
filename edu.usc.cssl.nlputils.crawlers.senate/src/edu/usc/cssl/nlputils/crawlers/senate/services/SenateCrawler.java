package edu.usc.cssl.nlputils.crawlers.senate.services;

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

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class SenateCrawler {
	ArrayList<Integer> congresses = new ArrayList<Integer>();
	String dateFrom, dateTo;
	int maxDocs = 10;
	String outputDir;
	BufferedWriter csvWriter;
	String sortType;
	HashSet<String> irrelevantLinks = new HashSet<String>(Arrays.asList("Next Document","New CR Search","Prev Document","HomePage","Help","GPO's PDF"));
	private String senText;
	private int congressNum;
	IProgressMonitor monitor;
	
	private void getSenators(int congress) throws IOException {
		System.out.println("Extracting Senators of Congress "+congress+"...");
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congress).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		
		for (Element senItem : senList){
			String senText = senItem.text().replace("\u00A0", " ");
			if (senText.contains("Any Senator"))		// We just need the senator names
				continue;
			searchSenatorRecords(congress,senText);
		}
	}

	public void crawl() throws IOException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String dateString = dateFormat.format(date);
		
		csvWriter  = new BufferedWriter(new FileWriter(new File(outputDir + System.getProperty("file.separator") + "records_"+dateString+".csv")));
		csvWriter.write("Congress,Date,Senator,Attributes,Title,File");
		csvWriter.newLine();
		if (senText.equals("All Senators") || senText.equals("All Republicans") || senText.equals("All Democrats") || senText.equals("All Independents")){
			if (congressNum != -1)
				getAll(congressNum, senText);
			else {
				for (int congress : congresses) {
					if(null != monitor && monitor.isCanceled()) return;
					getAll(congress, senText);
				}
			}
		} 		
		else {
			if (congressNum == -1) {
				for (int congress: congresses) {
					if(null != monitor && monitor.isCanceled()) return;
					System.out.println("Extracting Records from Congress "+congress+"...");
					searchSenatorRecords(congress, senText);
				}
			} else {
				System.out.println("Extracting Records from Congress "+congressNum+"...");
				searchSenatorRecords(congressNum, senText);
			}
		}
		csvWriter.close();
	}
	public void getAll(int congressNum, String senText) throws IOException{
		System.out.println("Extracting Senators of Congress "+congressNum+"...");
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congressNum).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		
		for (Element senItem : senList){
			if(null != monitor && monitor.isCanceled()) return;
			String senator = senItem.text().replace("\u00A0", " ");
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
			searchSenatorRecords(congressNum, senator);
		}
	}
	
	public void initialize(String sortType, int maxDocs, int congressNum, String senText, String dateFrom, String dateTo, String outputDir, ArrayList<Integer> allCongresses) throws IOException {
		this.outputDir = outputDir;
		this.maxDocs = maxDocs;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.senText = senText;
		this.congressNum = congressNum;
		this.sortType = sortType;
		this.congresses = allCongresses;
		
		System.out.println("Congress num :"+ congressNum);
		System.out.println("Senator name :"+ senText);
		System.out.println("Max docs :"+ maxDocs);
		System.out.println("Sort Type : "+ sortType);		
		System.out.println("From date :"+ dateFrom);
		System.out.println("To Date: "+ dateTo);		
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
				.data("sort",sortType)		// Default | Date
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
		
		if (relevantLinks.size() == 0){
			System.out.println("No Records Found.");
			return;
		}
		
		links = relevantLinks;
		
		String senatorAttribs = senText.split("\\(")[1].replace(")", "").trim();
		
		int count = 0;
		// Process each search result
		for (Element link : links) {
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
				String fileName = congress+"-"+lastName+"-"+senatorAttribs+"-"+recordDate+"-"+shortTitle+"-"+(System.currentTimeMillis()%1000)+".txt";
				writeToFile(fileName, contents);
				csvWriter.write(congress+","+recordDate+","+lastName+","+senatorAttribs+","+title+","+fileName);
				csvWriter.newLine();
				csvWriter.flush();
			}
		}
	}

	private void writeToFile(String fileName, String[] contents) throws IOException {
		System.out.println("Writing senator data - "+fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDir+System.getProperty("file.separator")+fileName)));
		bw.write(contents[0]);
		bw.newLine();
		bw.newLine();
		bw.write(contents[1]);
		bw.close();
	}

	private String[] extract(String extractLink, String lastName) throws IOException {
		Document page = Jsoup.connect("http://thomas.loc.gov"+extractLink).timeout(10*1000).get();
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

}
