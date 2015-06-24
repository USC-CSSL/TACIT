package edu.usc.cssl.nlputils.crawlers.senate.services;

import java.io.IOException;
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
	
	public static HashMap<String, String> getSenators(String congressString) throws IOException {
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
		senatorDetails(); // to populate all senator details
		HashMap<String, String> newSenMap = new HashMap<String, String>();
		for(String s : senArray) {
			String temp = new String();
			String tempSenateName = s.substring(0, s.indexOf('(')-1);			
			if(null != senatorDet.get(tempSenateName)) {				
				temp =  tempSenateName + " (" + senatorDet.get(tempSenateName) + ")";
			} else {
				temp = s;
			}
			newSenMap.put(s, temp);			
		}
		return newSenMap;
	}
	
	public static HashMap<String, String> getAllSenators(String[] congresses) throws IOException{
		HashMap<String, String> senators = new HashMap<String, String>();
		for (String cong : congresses){
			if (cong.trim().equals("All"))
				continue;
		
			HashMap<String, String> tempSenators =  getSenators(cong.trim());
			for (String senator : tempSenators.keySet()) {
				if (senator.equals("Any Senator"))
					continue;
				senators.put(senator, tempSenators.get(senator));
			}
		}
		
		return senators;
	}
	
	public static String[] getActiveCongresses() throws IOException{
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record").timeout(10*1000).get();
		Elements congList = doc.select("p.nav");
		String congString = congList.text().split(":")[1];
		return congString.split("\\|");
	}
	
	public static HashMap<String, String> senatorDetails() {
		
		//101
		senatorDet.put("Gorton, Slade", "R-WA");
		senatorDet.put("Lott, Trent", "R-MS");
		senatorDet.put("Jeffords, Jim", "R/I-VT");
		senatorDet.put("Coats, Dan", "R-IN");
		senatorDet.put("Mack, Connie", "R-FL");
		senatorDet.put("Bryan, Richard H.", "D-NV");
		senatorDet.put("Robb, Charles", "D-VA");
		senatorDet.put("Kerrey, Bob", "D-NE");
		senatorDet.put("Kohl, Herb", "D-WI");
		senatorDet.put("Lieberman, Joseph I.", "D-CT");
		senatorDet.put("Burns, Conrad", "R-MT");		
		senatorDet.put("Akaka, Daniel K.", "D-HI");
		senatorDet.put("Smith, Robert C.", "R-NH");
		//102
		senatorDet.put("Brown, Hank", "R-CO");
		senatorDet.put("Craig, Larry E.", "R-ID");
		senatorDet.put("Wellstone, Paul", "D-MN");
		senatorDet.put("Seymour, John", "R-CA");
		senatorDet.put("Wofford, Harris", "D-PA");
		senatorDet.put("Burdick, Jocelyn", "D-ND");		
		senatorDet.put("Feinstein, Dianne", "D-CA");
		senatorDet.put("Conrad, Kent", "D-ND");
		senatorDet.put("Dorgan, Byron L.", "D-ND");
		senatorDet.put("Mathews, Harlan", "D-TN");
		//103			
		senatorDet.put("Boxer, Barbara", "D-CA");
		senatorDet.put("Gregg, Judd", "R-NH");
		senatorDet.put("Campbell, Ben Nighthorse", "D/R-CO");
		senatorDet.put("Moseley Braun, Carol", "D-IL");
		senatorDet.put("Faircloth, Lauch", "R-NC");		
		senatorDet.put("Coverdell, Paul", "R-GA");
		senatorDet.put("Murray, Patty", "D-WA");		
		senatorDet.put("Feingold, Russell", "D-WI");
		senatorDet.put("Bennett, Robert", "R-UT");
		senatorDet.put("Kempthorne, Dirk", "R-ID");		
		senatorDet.put("Krueger, Robert C.", "D-TX");
		senatorDet.put("Hutchison, Kay Bailey", "R-TX");
		senatorDet.put("Inhofe, James", "R-OK");
		senatorDet.put("Thompson, Fred", "R-TN");
		//104
		senatorDet.put("Snowe, Olympia", "R-ME");
		senatorDet.put("Kyl, Jon", "R-AZ");
		senatorDet.put("DeWine, Mike", "R-OH");
		senatorDet.put("Thomas, Craig", "R-WY");
		senatorDet.put("Santorum, Rick", "R-PA");
		senatorDet.put("Grams, Rod", "R-MN");
		senatorDet.put("Ashcroft, John", "R-MO");
		senatorDet.put("Abraham, Spencer", "R-MI");
		senatorDet.put("Frist, Bill", "R-TN");
		senatorDet.put("Wyden, Ron", "D-OR");
		senatorDet.put("Frahm, Sheila", "R-KS");
		senatorDet.put("Brownback, Sam", "R-KS");
		//105
		senatorDet.put("Roberts, Pat", "R-KS");
		senatorDet.put("Durbin, Richard", "D-IL");
		senatorDet.put("Torricelli, Robert", "D-NJ");
		senatorDet.put("Johnson, Tim", "D-SD");
		senatorDet.put("Allard, Wayne", "R-CO");
		senatorDet.put("Reed, Jack", "D-RI");
		senatorDet.put("Hutchinson, Tim", "R-AR");
		senatorDet.put("Cleland, Max", "D-GA");
		senatorDet.put("Landrieu, Mary", "D-LA");
		senatorDet.put("Sessions, Jeff", "R-AL");
		senatorDet.put("Smith, Gordon", "R-OR");
		senatorDet.put("Hagel, Chuck", "R-NE");
		senatorDet.put("Collins, Susan", "R-ME");
		senatorDet.put("Enzi, Michael", "R-WY");
		//106
		senatorDet.put("Schumer, Charles", "D-NY");
		senatorDet.put("Bunning, Jim", "R-KY");
		senatorDet.put("Crapo, Mike", "R-ID");
		senatorDet.put("Lincoln, Blanche", "D-AR");
		senatorDet.put("Voinovich, George", "R-OH");
		senatorDet.put("Bayh, Evan", "D-IN");
		senatorDet.put("Fitzgerald, Peter", "R-IL");
		senatorDet.put("Edwards, John", "D-NC");
		senatorDet.put("Chafee, Lincoln", "R-RI");
		senatorDet.put("Miller, Zell", "D-GA");
		//107
		senatorDet.put("Nelson, Bill", "D-FL");
		senatorDet.put("Carper, Tom", "D-DE");
		senatorDet.put("Stabenow, Debbie", "D-MI");
		senatorDet.put("Ensign, John", "R-NV");
		senatorDet.put("Allen, George", "R-VA");
		senatorDet.put("Cantwell, Maria", "D-WA");
		senatorDet.put("Nelson, Ben", "D-NE");
		senatorDet.put("Clinton, Hillary Rodham", "D-NY");
		senatorDet.put("Corzine, Jon", "D-NJ");
		senatorDet.put("Carnahan, Jean", "D-MO");
		senatorDet.put("Dayton, Mark", "D-MN");
		senatorDet.put("Barkley, Dean", "I-MN");
		senatorDet.put("Talent, James", "R-MO");
		senatorDet.put("Cornyn, John", "R-TX");
		senatorDet.put("Murkowski, Lisa", "R-AK");
		//108		
		senatorDet.put("Lautenberg, Frank", "D-NJ");
		senatorDet.put("Chambliss, Saxby", "R-GA");
		senatorDet.put("Graham, Lindsey", "R-SC");
		senatorDet.put("Sununu, John", "R-NH");
		senatorDet.put("Alexander, Lamar", "R-TN");
		senatorDet.put("Dole, Elizabeth", "R-NC");
		senatorDet.put("Coleman, Norm", "R-MN");
		senatorDet.put("Pryor, Mark", "D-AR");
		
		// Additional senators
		senatorDet.put("Lautenberg, Frank", "D-NJ");
		
		// > 109 has an affiliation with name
		return senatorDet;
		
	}
	
}
