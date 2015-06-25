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
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;


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
	String[] senList;
	int progressSize;
	
	HashMap<String, HashMap<String, String>> congressSenatorMap = AvailableRecords.getCongressSenatorMap(); 
	HashMap<String, String> senatorDetails = SenatorDetails.getSenatorDetails(); // to populate all senator details
	
	public void crawl() throws IOException{
		if(null != monitor && monitor.isCanceled()) {
			monitor.subTask("Cancelling.. ");
			return;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String dateString = dateFormat.format(date);
		
		csvWriter  = new BufferedWriter(new FileWriter(new File(outputDir + System.getProperty("file.separator") + "records_"+dateString+".csv")));
		csvWriter.write("Congress,Date,Senator,Political Affiliation,Attributes,Title,File");
		csvWriter.newLine();
		if (senText.equals("All Senators") || senText.equals("All Republicans") || senText.equals("All Democrats") || senText.equals("All Independents")){
			if (congressNum != -1) {
				if(null != monitor && monitor.isCanceled()) {
					monitor.subTask("Cancelling.. ");
					return;
				}
				getAll(congressNum, senText, this.progressSize);
		    }else {
				for (int congress : congresses) {
					if(null != monitor && monitor.isCanceled()) {
						monitor.subTask("Cancelling.. ");
						return;
					}
					getAll(congress, senText, this.progressSize/congresses.size());
				}
			}
			if(null != monitor && monitor.isCanceled()) {
				monitor.subTask("Cancelling.. ");
				return;
			}
		} 		
		else {
			String politicalAffiliation =  "";
			if(senText.lastIndexOf('(')!=-1) {
				String affiliation = senText.substring(senText.lastIndexOf('(')+1, senText.length()-1);
				if(-1 != affiliation.indexOf('-'))
					politicalAffiliation = affiliation.split("-")[0];
				else 
					politicalAffiliation = senatorDetails.get(senText).split("-")[0];
			}
			
			if (congressNum == -1) { // All congress
				for (int congress: congresses) {
					if(null != monitor && monitor.isCanceled()) {
						monitor.subTask("Cancelling.. ");
						return;
					}
					if(monitor!=null) 
						monitor.worked(80/congresses.size());
					System.out.println("Extracting Records from Congress "+congress+"...");
					String senatorName = congressSenatorMap.get(String.valueOf(congress)).get(senText);
					searchSenatorRecords(congress, senatorName, this.progressSize/congresses.size(), politicalAffiliation);
				}
			} else {
				if(null != monitor && monitor.isCanceled()) {
					monitor.subTask("Cancelling.. ");
					return;
				}
				System.out.println("Extracting Records from Congress "+congressNum+"...");
				String senatorName = congressSenatorMap.get(String.valueOf(congressNum)).get(senText);
				searchSenatorRecords(congressNum, senatorName, this.progressSize, politicalAffiliation);
			}
			if(null != monitor && monitor.isCanceled()) {
				monitor.subTask("Cancelling.. ");
				return;
			}
		}
		csvWriter.close();
	}
	public void getAll(int congressNum, String senText, int maxProgressLimit) throws IOException{
	/*	ConsoleView.printlInConsoleln("Extracting Senators of Congress "+congressNum);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congressNum).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		*/
		boolean foundSenator = false;
		for (String senator : congressSenatorMap.get(String.valueOf(congressNum)).keySet()) {
			String senatorName =  senator;
			if(null != monitor && monitor.isCanceled()) return;
			//String senator = senItem.text().replace("\u00A0", " ");
			if (senator.contains("Any Senator"))		// We just need the senator names
				continue;
			if (senText.contains("All Republicans")){
				if (!senatorName.contains("(R-"))
					continue;
			}
			if (senText.contains("All Democrats")){
				if (!senatorName.contains("(D-"))
					continue;
			}
			if (senText.contains("All Independents")){
				if (!senatorName.contains("(I-"))
					continue;
			}
			String politicalAffiliation =  "";
			if(senatorName.lastIndexOf('(')!=-1) {
				String affiliation = senatorName.substring(senatorName.lastIndexOf('(')+1, senatorName.length()-1);
				if(-1 != affiliation.indexOf('-'))
					politicalAffiliation = affiliation.split("-")[0];
				else 
					politicalAffiliation = senatorDetails.get(senatorName).split("-")[0];
			}
			searchSenatorRecords(congressNum, congressSenatorMap.get(String.valueOf(congressNum)).get(senator), maxProgressLimit/congressSenatorMap.get(String.valueOf(congressNum)).keySet().size(), politicalAffiliation);
			foundSenator = true;
		}
		if(!foundSenator){
			if(senText.contains("All Republicans")) {
				ConsoleView.printlInConsoleln("No republicans found");
			}			
			else if(senText.contains("All Democrats")) {
				ConsoleView.printlInConsoleln("No democrats found");
			}
			else if(senText.contains("All Independents")) {
				ConsoleView.printlInConsoleln("No independents found");
			}else {
				ConsoleView.printlInConsoleln("No senators found");
			}
		}			
	}
	
	public void initialize(String sortType, int maxDocs, int congressNum, String senText, String dateFrom, String dateTo, String outputDir, ArrayList<Integer> allCongresses, IProgressMonitor monitor, int progressSize) throws IOException {
		this.outputDir = outputDir;
		this.maxDocs = maxDocs;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.senText = senText;
		this.congressNum = congressNum;
		this.sortType = sortType;
		this.congresses = allCongresses;
		this.monitor = monitor;
		this.progressSize = progressSize;
		
		System.out.println("Congress num :"+ congressNum);
		System.out.println("Senator name :"+ senText);
		System.out.println("Max docs :"+ maxDocs);
		System.out.println("Sort Type : "+ sortType);		
		System.out.println("From date :"+ dateFrom);
		System.out.println("To Date: "+ dateTo);		
		if(null != monitor && monitor.isCanceled()) {
			monitor.subTask("Cancelling.. ");
			return;
		}
	}

	public void searchSenatorRecords(int congress,String senText, int progressSize, String politicalAffiliation) throws IOException, NullPointerException{
		ConsoleView.printlInConsoleln("Current Senator - "+senText);
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
			ConsoleView.printlInConsoleln("No Records Found.");
			return;
		}
		
		links = relevantLinks;
		
		String senatorAttribs = senText.split("\\(")[1].replace(")", "").trim();

		int count = 0;
		int tempCount = 0;
		// Process each search result
		for (Element link : links) {
			if(null != monitor && monitor.isCanceled()) {
				monitor.subTask("Cancelling.. ");
				return;
			}
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
				csvWriter.write(congress+","+recordDate+","+lastName+","+politicalAffiliation+","+senatorAttribs+","+title+","+fileName);
				csvWriter.newLine();
				csvWriter.flush();
			}
			
			tempCount++;
			tempCount = updateWork(maxDocs, links.size(), progressSize, tempCount);
		}
	}

	private int updateWork(int maxDocs, int totalLinks, int progressSize, int tempCount) {
		int tempMaxDocs = maxDocs == -1 ? 2000 : maxDocs;				
		int numDocs2Download = tempMaxDocs > totalLinks ? totalLinks : tempMaxDocs;	
		if(numDocs2Download>progressSize) { // worked should be 1 
			int totalCount = numDocs2Download/progressSize;
			totalCount++;
			if(tempCount % totalCount == 0) {
				tempCount = 0;
				monitor.worked(1);
			}
		} else {
			
			monitor.worked((progressSize/numDocs2Download)-1);
		}
		return tempCount;
	}
	private void writeToFile(String fileName, String[] contents) throws IOException {
		ConsoleView.printlInConsoleln("Writing senator data - "+fileName);
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
		try {
			Integer.parseInt(word);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
