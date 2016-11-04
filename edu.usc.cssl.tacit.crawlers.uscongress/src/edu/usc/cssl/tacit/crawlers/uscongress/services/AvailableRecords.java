package edu.usc.cssl.tacit.crawlers.uscongress.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AvailableRecords {
	static HashMap<String, String> senatorDet = new HashMap<String, String>();
	static HashMap<String, HashMap<String, String>> congressSenatorMap = new HashMap<String, HashMap<String, String>>();
	
	public static  HashMap<String, HashMap<String, String>> getCongressSenatorMap() {
		return congressSenatorMap;
	}
	
	static HashMap<String, String> representativeDet = new HashMap<String, String>();
	static HashMap<String, HashMap<String, String>> congressRepMap = new HashMap<String, HashMap<String, String>>();
	
	public static  HashMap<String, HashMap<String, String>> getcongressRepMap() {
		return congressRepMap;
	}	
	
	/*
	 * @brief: This will resolve the website change issue, Only this link is broken "http://thomas.loc.gov/home/faqlist.html#10". Other links that we use is stable and working!
	 * @author: Yuva
	 */
	public static HashMap<String, String> getAllCongresses() throws IOException {	
		HashMap<String, String> map =  new LinkedHashMap<String, String>();
		map.put("All", "None");
		Document doc = Jsoup.connect("https://www.congress.gov/browse#browse_legislation").timeout(10*1000).get(); // new link which gives what we look
		Element congElt = doc.getElementById("congresses");
		Elements selectOptions = congElt.children();
		
		// Patterns to locate the congress number and its respective years
		String keyPattern = "(\\d+)";
		String valuePattern = "(\\d+-\\d+)";
		Pattern keyPatternObj = Pattern.compile(keyPattern);
		Pattern valuePatternObj = Pattern.compile(valuePattern);
		int minYear = Integer.MAX_VALUE;
		int maxYear = Integer.MIN_VALUE;
		
		for(Element elt : selectOptions) {
			String text = elt.text();
			Matcher keyMatcher = keyPatternObj.matcher(text);
			Matcher valueMatcher = valuePatternObj.matcher(text);
			if(keyMatcher.find() && valueMatcher.find() && Integer.parseInt(keyMatcher.group(0))>100) {
				map.put(keyMatcher.group(0), valueMatcher.group(0));
				String tempYear[] = valueMatcher.group(0).split("-");
				minYear = Math.min(minYear, Integer.parseInt(tempYear[0]));
				maxYear = Math.max(maxYear, Integer.parseInt(tempYear[1]));
			}
		}
		map.put("All", minYear+"-"+maxYear);
		return map;
	}
	
	public static HashMap<String, String> getAllCongresses_old() throws IOException {		
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/faqlist.html#10").timeout(10*1000).get();
		Elements congList = doc.select("blockquote");
		String temp[] = congList.text().split(" ");
		HashMap<String, String> map =  new LinkedHashMap<String, String>();
		map.put("All", "None");
				
		String keyPattern = "(\\d+)";
		String valuePattern = "(\\d+-\\d+)";
		Pattern keyPatternObj = Pattern.compile(keyPattern);
		Pattern valuePatternObj = Pattern.compile(valuePattern);
		
		String[] activeCongresses = getActiveCongresses(); // gives only the active congress details
		for(int index = 0; index<activeCongresses.length; index++) {
			Matcher keyMatcher = keyPatternObj.matcher(activeCongresses[index]);			
			if(keyMatcher.find()) {
				activeCongresses[index] = keyMatcher.group(0).trim();
			}			
		}
		
		int minYear = 0;
		int maxYear = 0;
		for(int index = temp.length-2; index>=0; index=index-2) {				
			temp[index] = temp[index].trim();
			temp[index+1] = temp[index+1].trim();
			Matcher keyMatcher = keyPatternObj.matcher(temp[index+1]);
			Matcher valueMatcher = valuePatternObj.matcher(temp[index]);
			if(keyMatcher.find() && valueMatcher.find() && Arrays.asList(activeCongresses).contains(keyMatcher.group(0))) {
				map.put(keyMatcher.group(0), valueMatcher.group(0));
				String tempYear[] = valueMatcher.group(0).split("-");
				if(minYear == 0)
					minYear= Integer.parseInt(tempYear[0]);
				else
					minYear = Math.min(minYear, Integer.parseInt(tempYear[0]));
				
				if(maxYear == 0)
					maxYear = Integer.parseInt(tempYear[1]);
				else
					maxYear = Math.max(maxYear, Integer.parseInt(tempYear[1]));
			}
		}		
		map.put("All", minYear+"-"+maxYear);
		return map;
	}
	
	public static List<String> getSenatorNames(String congressString) throws IOException {
		congressString = congressString.replace("\u00A0", "");
		int congress = Integer.parseInt(congressString);
		System.out.println("Extracting Senators of Congress "+congress);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&c="+congress).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SMEMB").select("option");
		List<String> senNames = new ArrayList<String>();
		if(senList.size() == 0) return new ArrayList<String>();
		for (Element senItem : senList){
			String senText = senItem.text().replace("\u00A0", " ");
			if (senText.equals("Any Senator"))
				continue;
			senNames.add(senText);
		}
		return senNames;
	}
	
	public static List<String> getSenators(String congressString) throws IOException {
		congressString = congressString.replace("\u00A0", "");
		int congress = Integer.parseInt(congressString);
		System.out.println("Extracting Senators of Congress "+congress);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congress).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		List<String> senators = new ArrayList<String>();
		if(senList.size() == 0) return senators;
		for (Element senItem : senList){
			String senText = senItem.text().replace("\u00A0", " ");
			if (senText.equals("Any Senator"))
				continue;
			senators.add(senText);
		}
//		senatorDet = SenatorDetails.getSenatorDetails();
		HashMap<String, String> senMap = new HashMap<String, String>();
		for(String s : senators) {
			String temp = new String();
			String tempSenateName = s.substring(0, s.lastIndexOf('(')-1);	
			if(null == senMap.get(tempSenateName)) {
				if(null != senatorDet.get(tempSenateName))			
					temp =  tempSenateName + " (" + senatorDet.get(tempSenateName) + ")";
				else 
					temp = s;
			}
			senMap.put(temp, s);
		}
		congressSenatorMap.put(congressString, senMap);
		ArrayList<String> uniqueSenators = new ArrayList<String>();
		uniqueSenators.addAll(senMap.keySet());
		Collections.sort(uniqueSenators);
		return uniqueSenators;
	}
	
	public static String[] getAllSenators(String[] congresses) throws IOException{
		ArrayList<String> senators = new ArrayList<String>();
		for (String cong : congresses){
			if (cong.trim().equals("All"))
				continue;
			
			List<String> tempSenators =  getSenators(cong.trim());
			for (String senator : tempSenators) {
				if (senator.equals("Any Senator") || senators.contains(senator)) {
					continue;
				}
				senators.add(senator);
			}
		}
		String[] tempSenators = senators.toArray(new String[senators.size()]);
		Arrays.sort(tempSenators);
		return tempSenators;
	}
	
	public static String[] getAllRepresentatives(String[] congresses) throws IOException{
		ArrayList<String> representatives = new ArrayList<String>();
		for (String cong : congresses){
			if (cong.trim().equals("All"))
				continue;
			
			List<String> tempReps =  getRepresentatives(cong.trim());
			for (String rep : tempReps) {
				if (rep.equals("Any Representative") || representatives.contains(rep)) {
					continue;
				}
				representatives.add(rep);
			}
		}
		String[] tempReps = representatives.toArray(new String[representatives.size()]);
		Arrays.sort(tempReps);
		return tempReps;
	}	
	
	public static List<String> getRepresentatives(String congressString) throws IOException {
		congressString = congressString.replace("\u00A0", "");
		int congress = Integer.parseInt(congressString);
		System.out.println("Extracting Representatives of Congress "+congress);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congress).timeout(10*1000).get();
		Elements repList = doc.getElementsByAttributeValue("name", "HSpeaker").select("option");
		List<String> representatives = new ArrayList<String>();
		if(repList.size() == 0) return representatives;
		for (Element repItem : repList) {
			String repText = repItem.text().replace("\u00A0", " ");
			if (repText.equals("Any Representative"))
				continue;
			representatives.add(repText);
		}
		
		representativeDet = RepresentativeDetails.getRepersentativeDetails(); // to populate all representative details
		HashMap<String, String> newRepMap = new HashMap<String, String>();
		for(String s : representatives) {
			String temp = new String();
			String tempRepName = (s.lastIndexOf('(')!=-1) ? s.substring(0, s.lastIndexOf('(')) : s;
			if(tempRepName.charAt(tempRepName.length()-1) == ' ')
				tempRepName = tempRepName.substring(0, tempRepName.length()-1);
			if(null == newRepMap.get(tempRepName)) {
				if(null != representativeDet.get(tempRepName)) {
					temp =  tempRepName + " (" + representativeDet.get(tempRepName) + ")";
				} else
					temp = s;
			}
			newRepMap.put(temp, s); // new value, old value
		}
		congressRepMap.put(congressString, newRepMap);
		ArrayList<String> uniqueReps = new ArrayList<String>();
		uniqueReps.addAll(newRepMap.keySet());
		Collections.sort(uniqueReps);
		return uniqueReps;
	}
	
	public static String[] getActiveCongresses() throws IOException{
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record").timeout(10*1000).get();
		Elements congList = doc.select("p.nav");
		String congString = congList.text().split(":")[1];
		return congString.split("\\|");
	}
}
