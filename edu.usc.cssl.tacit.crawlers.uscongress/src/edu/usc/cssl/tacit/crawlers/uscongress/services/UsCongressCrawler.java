package edu.usc.cssl.tacit.crawlers.uscongress.services;

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
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;


public class UsCongressCrawler {
	public int totalFilesDownloaded = 0;
	ArrayList<Integer> congresses = new ArrayList<Integer>();
	String dateFrom, dateTo;
	int maxDocs = 10;
	String outputDir;
	BufferedWriter csvWriter;
	String sortType;
	HashSet<String> irrelevantLinks = new HashSet<String>(Arrays.asList("Next Document","New CR Search","Prev Document","HomePage","Help","GPO's PDF"));
	private ArrayList<String> congressMembers;
	private int congressNum;
	IProgressMonitor monitor;
	int progressSize;
	boolean isSenate;
	String crawlSenateRecords;
	String crawlHouseRepRecords;
	String crawlDailyDigest;
	String crawlExtension;	
	
	HashMap<String, HashMap<String, String>> congressSenatorMap = AvailableRecords.getCongressSenatorMap(); 
	HashMap<String, String> senatorDetails = SenatorDetails.getSenatorDetails(); // to populate all senator details
	
	HashMap<String, HashMap<String, String>> congressRepresentativeMap = AvailableRecords.getcongressRepMap();
	HashMap<String, String> representativeDetails = RepresentativeDetails.getRepersentativeDetails();
	
	private void formatMembersList() {
		ArrayList<String> tempMembers =  new ArrayList<String>();
		for(String member : this.congressMembers) {
			tempMembers.add(member);
		}
		
		if(tempMembers.contains("All Senators")) {
			congressMembers.removeAll(congressMembers);
			congressMembers.add("All Senators");
		} else if(tempMembers.contains("All Representatives")) {
				congressMembers.removeAll(congressMembers);
				congressMembers.add("All Representatives");
		} else  {
			if(tempMembers.contains("All Democrats")) {
				// remove all the remaining democrats
				for (Iterator<String> it = tempMembers.iterator(); it.hasNext(); ) {
				    String s = it.next();
					if(s.contains("(D-") || s.contains("D/")) {
						it.remove();
					}
				}
			} 
			if(tempMembers.contains("All Republicans")) {
				for (Iterator<String> it = tempMembers.iterator(); it.hasNext(); ) {
				    String s = it.next();
					if(s.contains("(R-") || s.contains("R/")) {
						it.remove();
					}
				}
			}
			if(tempMembers.contains("All Independents")) {
				for (Iterator<String> it = tempMembers.iterator(); it.hasNext(); ) {
				    String s = it.next();
					if(s.contains("(I-") || s.contains("I/")) {
						it.remove();
					}
				}
			}
		}
		this.congressMembers = tempMembers;
	}
	
	public void crawl() throws IOException {
		if(null != monitor && monitor.isCanceled()) {
			monitor.subTask("Cancelling.. ");
			return;
		}
		
		formatMembersList();
				
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
		Date dateobj = new Date();
		
		csvWriter  = new BufferedWriter(new FileWriter(new File(outputDir + System.getProperty("file.separator") + "congress-crawler-summary-"+df.format(dateobj)+".csv")));
		if(isSenate) csvWriter.write("Congress,Date,Senator,Political Affiliation,Congressional Section,State,Title,File");
		else csvWriter.write("Congress,Date,Representative,Political Affiliation,Congressional Section,State,Title,File");
		csvWriter.newLine();
		for(String memberText : congressMembers) {
			int tempProgressSize = progressSize/congressMembers.size();
			if (memberText.equals("All Representatives") || memberText.equals("All Senators") || memberText.equals("All Republicans") || memberText.equals("All Democrats") || memberText.equals("All Independents")){
				if (congressNum != -1) {
					if(null != monitor && monitor.isCanceled()) {
						monitor.subTask("Cancelling.. ");
						return;
					}
					if(isSenate) getAllSenators(congressNum, memberText, tempProgressSize);
					else getAllRepresentatives(congressNum, memberText, tempProgressSize);
			    }else {
					for (int congress : congresses) {
						if(null != monitor && monitor.isCanceled()) {
							monitor.subTask("Cancelling.. ");
							return;
						}
						if(isSenate) getAllSenators(congress, memberText, tempProgressSize/congresses.size());
						else getAllRepresentatives(congress, memberText, tempProgressSize/congresses.size());					
					}
				}
				if(null != monitor && monitor.isCanceled()) {
					monitor.subTask("Cancelling.. ");
					return;
				}
			} 		
			else {
				String politicalAffiliation =  "";
				if(memberText.lastIndexOf('(')!=-1) {
					String affiliation = memberText.substring(memberText.lastIndexOf('(')+1, memberText.length()-1);
					if(-1 != affiliation.indexOf('-'))
						politicalAffiliation = affiliation.split("-")[0];
					else 
						politicalAffiliation = (isSenate) ? senatorDetails.get(memberText).split("-")[0] : representativeDetails.get(memberText).split("-")[0];
				}
				
				if (congressNum == -1) { // All congress
					for (int congress: congresses) {
						if(null != monitor && monitor.isCanceled()) {
							monitor.subTask("Cancelling.. ");
							return;
						}
						System.out.println("Extracting Records from Congress "+congress+"...");
						String memberName = (isSenate) ? congressSenatorMap.get(String.valueOf(congress)).get(memberText) : congressRepresentativeMap.get(String.valueOf(congress)).get(memberText);
						if(null != memberName) {
							if(isSenate) searchRecords(congress, memberName, "", tempProgressSize/congresses.size(), politicalAffiliation);
							else  searchRecords(congress, "", memberName, tempProgressSize/congresses.size(), politicalAffiliation);
						}
					}
				} else {
					if(null != monitor && monitor.isCanceled()) {
						monitor.subTask("Cancelling.. ");
						return;
					}
					System.out.println("Extracting Records from Congress "+congressNum+"...");
					String memberName = (isSenate) ? congressSenatorMap.get(String.valueOf(congressNum)).get(memberText) : congressRepresentativeMap.get(String.valueOf(congressNum)).get(memberText);
					if(null != memberName) {
						if(isSenate) searchRecords(congressNum, memberName, "", tempProgressSize, politicalAffiliation);
						else searchRecords(congressNum, "", memberName, tempProgressSize, politicalAffiliation);
					}
				}
				if(null != monitor && monitor.isCanceled()) {
					monitor.subTask("Cancelling.. ");
					return;
				}
			}
		}
		csvWriter.close();
	}
	public void getAllSenators(int congressNum, String senText, int maxProgressLimit) throws IOException{
		boolean foundSenator = false;
		for (String senator : congressSenatorMap.get(String.valueOf(congressNum)).keySet()) {
			String senatorName =  senator;
			if(null != monitor && monitor.isCanceled()) return;
			if (senator.contains("Any Senator"))		// We just need the senator names
				continue;
			if (senText.contains("All Republicans")){
				if (!senatorName.contains("(R-") && !senatorName.contains("R/"))
					continue;
			}
			if (senText.contains("All Democrats")){
				if (!senatorName.contains("(D-") && !senatorName.contains("D/"))
					continue;
			}
			if (senText.contains("All Independents")){
				if (!senatorName.contains("(I-") && !senatorName.contains("I/"))
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
			if(null != congressSenatorMap.get(String.valueOf(congressNum)).get(senator)) {
				searchRecords(congressNum, congressSenatorMap.get(String.valueOf(congressNum)).get(senator), "", maxProgressLimit/congressSenatorMap.get(String.valueOf(congressNum)).keySet().size(), politicalAffiliation);
				foundSenator = true;
			}
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
	
	public void getAllRepresentatives(int congressNum, String repText, int maxProgressLimit) throws IOException{
		boolean foundRep = false;
		for (String rep : congressRepresentativeMap.get(String.valueOf(congressNum)).keySet()) {
			String repName =  rep;
			if(null != monitor && monitor.isCanceled()) return;
			if (rep.contains("Any Representative"))		// We just need the senator names
				continue;
			if (repText.contains("All Republicans")) {
				if (!repName.contains("(R-") && !repName.contains("R/"))
					continue;
			}
			if (repText.contains("All Democrats")) {
				if (!repName.contains("(D-") && !repName.contains("D/"))
					continue;
			}
			if (repText.contains("All Independents")) {
				if (!repName.contains("(I-") && !repName.contains("I/"))
					continue;
			}
			String politicalAffiliation =  "";
			if(repName.lastIndexOf('(') != -1) {
				String affiliation = repName.substring(repName.lastIndexOf('(')+1, repName.length()-1);
				if(-1 != affiliation.indexOf('-'))
					politicalAffiliation = affiliation.split("-")[0];
				else 
					politicalAffiliation = representativeDetails.get(repName).split("-")[0];
			}
			if(null != congressRepresentativeMap.get(String.valueOf(congressNum)).get(rep)) {
				searchRecords(congressNum, "", congressRepresentativeMap.get(String.valueOf(congressNum)).get(rep), maxProgressLimit/congressRepresentativeMap.get(String.valueOf(congressNum)).keySet().size(), politicalAffiliation);
				foundRep = true;
			}
		}
		if(!foundRep){
			if(repText.contains("All Republicans")) {
				ConsoleView.printlInConsoleln("No republicans found");
			}			
			else if(repText.contains("All Democrats")) {
				ConsoleView.printlInConsoleln("No democrats found");
			}
			else if(repText.contains("All Independents")) {
				ConsoleView.printlInConsoleln("No independents found");
			}else {
				ConsoleView.printlInConsoleln("No representatives found");
			}
		}			
	}	
	
	public void initialize(String sortType, int maxDocs, int congressNum, ArrayList<String> congressMemberDetails, String dateFrom, String dateTo, String outputDir, ArrayList<Integer> allCongresses, IProgressMonitor monitor, int progressSize, boolean isSenate, boolean crawlSenateRecords, boolean crawlHouseRepRecords, boolean crawlDailyDigest, boolean crawlExtension) throws IOException {
		this.outputDir = outputDir;
		this.maxDocs = maxDocs;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.congressMembers = congressMemberDetails;
		this.congressNum = congressNum;
		this.sortType = sortType;
		this.congresses = allCongresses;
		this.monitor = monitor;
		this.progressSize = progressSize;
		this.isSenate = isSenate;
		this.crawlSenateRecords = (crawlSenateRecords)? "1" : "0";
		this.crawlHouseRepRecords = (crawlHouseRepRecords) ? "2" : "0";
		this.crawlExtension = (crawlExtension) ? "4" : "0";
		this.crawlDailyDigest = (crawlDailyDigest) ? "8" : "0";
		
		if(null != monitor && monitor.isCanceled()) {
			monitor.subTask("Cancelling.. ");
			return;
		}
	}

	public void searchRecords(int congress, String senText, String repText, int progressSize, String politicalAffiliation) throws IOException, NullPointerException{
		if((null == senText || senText.isEmpty()) && (null == repText || repText.isEmpty())) return;
		String memText = (null == senText || senText.isEmpty()) ? repText : senText;
		ConsoleView.printlInConsoleln("Current Congress Member - "+ memText);
		String memberDir = this.outputDir + File.separator + memText;
		if(!new File(memberDir).exists()) {
			new File(memberDir).mkdir();
		}
				
		if(null != monitor && !monitor.isCanceled()) {
			monitor.subTask("Crawling data for " + memText + "...");
		}
		Document doc = Jsoup.connect("http://thomas.loc.gov/cgi-bin/thomas2")
				.data("xss", "query")		// Important. If removed, "301 Moved permanently" error
				.data("queryr"+congress, "")	// Important. 113 - congress number. Make this auto? If removed, "Database Missing" error
				.data("MaxDocs", "2000")		// Doesn't seem to be working
				.data("Stemming", "No")
				.data("HSpeaker", repText)
				.data("SSpeaker", senText)
				.data("member", "speaking")	// speaking | all  -- all occurrences
				.data("relation", "or")		// or | and  -- when there are multiple speakers in the query
				.data("SenateSection", crawlSenateRecords)
				.data("HouseSection", crawlHouseRepRecords)
				.data("ExSection", crawlExtension)
				.data("DigestSection",crawlDailyDigest)
				.data("LBDateSel", "Thru")		// "" | 1st | 2nd | Thru -- all sessions, 1st session, 2nd session, range
				.data("DateFrom", dateFrom)
				.data("DateTo", dateTo)
				.data("sort", sortType)		// Default | Date
				.data("submit", "SEARCH")
				.userAgent("Mozilla")
				.timeout(10*1000)
				.post();

		Elements links = doc.getElementById("content").getElementsByTag("a");		
		
		// Extracting the relevant links
		Elements relevantLinks = new Elements();
		for (Element link:links) {
			if (!irrelevantLinks.contains(link.text()))
				if (link.text().contains("Senate") || link.text().contains("House of Representatives") || link.text().contains("Extensions of Remarks") || link.text().contains("Daily Digest"))
					relevantLinks.add(link);
		}
		
		if (relevantLinks.size() == 0){
			ConsoleView.printlInConsoleln("No Records Found.");
			return;
		}
		
		links = relevantLinks;
		
		String memberAttribs = memText.split("\\(")[1].replace(")", "").trim();
		String memberState = memberAttribs;
		if(-1 != memberAttribs.indexOf('-')) {
			memberState = memberAttribs.split("-")[1];
		}

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
			String recordDate = "";
			String recordType = "";
			if (link.text().contains("Senate")) {
				recordDate = link.text().replace("(Senate - ", "").replace(",", "").replace(")", "").trim();
				recordType = "Senate";
			}
			else if(link.text().contains("House of Representatives")) {
				recordDate = link.text().replace("(House of Representatives - ", "").replace(",", "").replace(")", "").trim();
				recordType = "House";
			}
			else if(link.text().contains("Extensions of Remarks")) {
				recordDate = link.text().replace("(Extensions of Remarks - ", "").replace(",", "").replace(")", "").trim();
				recordType = "Extension of Remarks";
			}
			else if(link.text().contains("Daily Digest")) {
				recordDate = link.text().replace("(Daily Digest - ", "").replace(",", "").replace(")", "").trim();
				recordType = "Daily Digest";
			}
			
			Document record = Jsoup.connect("http://thomas.loc.gov"+link.attr("href")).timeout(10*1000).get();
			Elements tabLinks = record.getElementById("content").select("a[href]");
			
			String extractLink="";
			for (Element tabLink:tabLinks) {
				if (tabLink.text().equals("Printer Friendly Display")) {
					extractLink = tabLink.attr("href");
					break;
				}
			}
			
			String lastName = memText.split(",")[0];
			String[] contents = extract(extractLink,lastName);

			if (contents[1].length()==0)
				count--;
			else {
				String[] split = contents[0].split("-");
				String title = split[0].trim();
				title = title.replaceAll(",", "");
				title = title.replaceAll("\\.", "");
				String shortTitle = title;
				if (title.length()>15)
					shortTitle = title.substring(0, 15).trim().replaceAll("[^\\w\\s]", "");
				shortTitle.replaceAll("[.,;\"!-(){}:?'/\\`~$%#@&*_=+<>]", ""); // replaces all special characters
				String fileName = congress+"-"+lastName+"-"+memberAttribs+"-"+recordDate+"-"+shortTitle+"-"+(System.currentTimeMillis()%1000)+".txt";
				writeToFile(memberDir, fileName, contents);
				csvWriter.write(congress+","+recordDate+","+lastName+","+politicalAffiliation+","+recordType+","+memberState+","+title+","+fileName);
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
		if(numDocs2Download>progressSize && 0!= progressSize) { // worked should be 1 
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
	private void writeToFile(String senatorOutputDir, String fileName, String[] contents) throws IOException {
		//ConsoleView.printlInConsoleln("Writing senator data - "+fileName);
		ConsoleView.printlInConsoleln("Writing "+ senatorOutputDir + File.separator + fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(senatorOutputDir+System.getProperty("file.separator")+fileName)));
		bw.write(contents[0]);
		bw.newLine();
		bw.newLine();
		bw.write(contents[1]);
		bw.close();
		totalFilesDownloaded++;
	}

	private String[] extract(String extractLink, String lastName) throws IOException {
		Document page = Jsoup.connect("http://thomas.loc.gov"+extractLink).timeout(10*1000).get();
		String title = page.getElementById("container").select("b").text();
		StringBuilder content = new StringBuilder();
		/* Elements lines = page.getElementById("container").select("p");
		String currentLine;
		boolean extractFlag = false;
		for (Element line : lines) {
			currentLine = line.text().trim();
			if (currentLine!=null && !currentLine.isEmpty()){
				String[] words = currentLine.replaceAll("\u00A0", "").trim().split(" ");
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
		} */	
		String[] contents = new String[2];
		contents[0] = title;
		contents[1] = page.getElementById("container").text();
		
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
