package edu.usc.cssl.nlputils.crawlers.senate.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
	public static HashMap<String, String> getAllCongresses() throws IOException {		
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
		for(int index = 0; index<temp.length; index=index+2) {				
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
			//System.out.println(s);
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

		File readme = new File("C:\\Users\\Yuva.DNIPCXPS\\Desktop\\Senator Parsing\\senators.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			for(String s:congressSenatorMap.keySet()){
				//System.out.println("Congress # :"+ s);
				bw.write("Congress # :"+ s);
				bw.newLine();
				for(String senator : congressSenatorMap.get(s).keySet()) {
					//System.out.println("New :"+ senator + "Old :"+ congressSenatorMap.get(s).get(senator));
					bw.write("New :"+ senator + "Old :"+ congressSenatorMap.get(s).get(senator));
					bw.newLine();
				}
				bw.newLine();
			}
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		for(String s : senators) {
			//System.out.println(s);
		}
		String[] tempSenators = senators.toArray(new String[senators.size()]);
		Arrays.sort(tempSenators);
		return tempSenators;
	}
	
	public static String[] getActiveCongresses() throws IOException{
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record").timeout(10*1000).get();
		Elements congList = doc.select("p.nav");
		String congString = congList.text().split(":")[1];
		return congString.split("\\|");
	}
	
}
