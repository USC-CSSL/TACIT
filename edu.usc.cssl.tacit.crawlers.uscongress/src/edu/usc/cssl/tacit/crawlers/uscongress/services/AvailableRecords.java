package edu.usc.cssl.tacit.crawlers.uscongress.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
			if(keyMatcher.find() && valueMatcher.find()) {
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
	
	public static String[] getSenators(String congressString) throws IOException {
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
		senatorDet = SenatorDetails.getSenatorDetails(); // to populate all senator details
		HashMap<String, String> newSenMap = new HashMap<String, String>();
		for(String s : senArray) {
			String temp = new String();
			String tempSenateName = s.substring(0, s.lastIndexOf('(')-1);	
			if(null == newSenMap.get(tempSenateName)) {
				if(null != senatorDet.get(tempSenateName)) {				
					temp =  tempSenateName + " (" + senatorDet.get(tempSenateName) + ")";
				} else {
					temp = s;
				}
				newSenMap.put(temp, s); // new value, old value
			}
		}
		congressSenatorMap.put(congressString, newSenMap);
		// return only unique values from here
		ArrayList<String> uniqueSenators = new ArrayList<String>();
		uniqueSenators.addAll(newSenMap.keySet());
		String[] tempSenators =  uniqueSenators.toArray(new String[uniqueSenators.size()]);
		Arrays.sort(tempSenators);
		return tempSenators;
	}
	
	public static String[] getAllSenators(String[] congresses) throws IOException{
		ArrayList<String> senators = new ArrayList<String>();
		for (String cong : congresses){
			if (cong.trim().equals("All"))
				continue;
			
			String[] tempSenators =  getSenators(cong.trim());
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
			
			String[] tempReps =  getRepresentatives(cong.trim());
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
	
	public static String[] getRepresentatives(String congressString) throws IOException {
		congressString = congressString.replace("\u00A0", "");
		int congress = Integer.parseInt(congressString);
		System.out.println("Extracting Representatives of Congress "+congress);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congress).timeout(10*1000).get();
		Elements repList = doc.getElementsByAttributeValue("name", "HSpeaker").select("option");
		String[] repArray = new String[repList.size()-1];
		int index = 0;
		for (Element repItem : repList) {
			String repText = repItem.text().replace("\u00A0", " ");
			if (repText.equals("Any Representative"))
				continue;
			repArray[index++] = repText;
		}
		
		representativeDet = RepresentativeDetails.getRepersentativeDetails(); // to populate all representative details
		HashMap<String, String> newRepMap = new HashMap<String, String>();
		for(String s : repArray) {
			String temp = new String();
			String tempRepName = (s.lastIndexOf('(')!=-1) ? s.substring(0, s.lastIndexOf('(')) : s;
			if(tempRepName.charAt(tempRepName.length()-1) == ' ')
				tempRepName = tempRepName.substring(0, tempRepName.length()-1);
			if(null == newRepMap.get(tempRepName)) {
				if(null != representativeDet.get(tempRepName)) {
					temp =  tempRepName + " (" + representativeDet.get(tempRepName) + ")";
				} else {
					/*int start = s.lastIndexOf('(')+1;
					int end = s.lastIndexOf(')');
					if(end>start) {
						//representativeDet.put("Young, Todd", "R-IN");
						System.out.println(s+" representativeDet.put(\""+tempRepName + "\", \"" + s.substring(start, end) +"\");");
					}*/
					temp = s;
				}
				newRepMap.put(temp, s); // new value, old value
			}
		}
		congressRepMap.put(congressString, newRepMap);
		// return only unique values from here
		ArrayList<String> uniqueReps = new ArrayList<String>();
		uniqueReps.addAll(newRepMap.keySet());
		String[] tempReps =  uniqueReps.toArray(new String[uniqueReps.size()]);
		Arrays.sort(tempReps);
		return tempReps;
	}
	
	public static String[] getActiveCongresses() throws IOException{
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record").timeout(10*1000).get();
		Elements congList = doc.select("p.nav");
		String congString = congList.text().split(":")[1];
		return congString.split("\\|");
	}
}
