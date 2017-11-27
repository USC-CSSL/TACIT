package edu.usc.cssl.tacit.crawlers.govtrack.services;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProPublicaCrawler {
	
	ProPublicaNetwork network;
	
	public ProPublicaCrawler(String apiKey) {
		network = new ProPublicaNetwork(apiKey);
	}
	
	private void crawlBills(String congress, String chamber, String type, FileWriter fileWriter) throws Exception {

		String response = null;
		String endpoint = ProPublicaEnpoints.getAllBillsForCongressEndpoint(congress, chamber, type,"0");
		boolean first = true;
		
		int offset = 0;
		while(true) {
			endpoint = ProPublicaEnpoints.getAllBillsForCongressEndpoint(congress, chamber, type,offset+"");
			System.out.println(endpoint);
			response = network.sendGETRequest(endpoint);
			JSONObject responseJSON = null;
			try {
				responseJSON = new JSONObject(response);
			}catch(Exception e) {
				offset += 20;
				continue;
			}
			JSONArray results = (JSONArray)responseJSON.get("results");
			
			if (results.getJSONObject(0).getInt("num_results") == 0) break;
			
			results = results.getJSONObject(0).getJSONArray("bills");
			
			String bills =  results.toString();
			
			if (!first) {
				fileWriter.write(",");
			}
			fileWriter.write(bills.substring(bills.indexOf("[")+1,bills.lastIndexOf("]")).trim());
			offset += 20;
			first = false;
		}
		
	}
	
	
	private void searchBills(String query, FileWriter fileWriter) throws Exception {

		String response = null;
		String endpoint = ProPublicaEnpoints.getSearchBillsEndpoint(query, "0");
		boolean first = true;
		
		int offset = 0;
		while(true) {
			endpoint = ProPublicaEnpoints.getSearchBillsEndpoint(query, offset + "");;
			System.out.println(endpoint);
			response = network.sendGETRequest(endpoint);
			JSONObject responseJSON = null;
			try {
				responseJSON = new JSONObject(response);
			}catch(Exception e) {
				offset += 20;
				continue;
			}
			JSONArray results = (JSONArray)responseJSON.get("results");
			
			if (results.getJSONObject(0).getInt("num_results") == 0) break;
			
			results = results.getJSONObject(0).getJSONArray("bills");
			
			String bills =  results.toString();
			
			if (!first) {
				fileWriter.write(",");
			}
			fileWriter.write(bills.substring(bills.indexOf("[")+1,bills.lastIndexOf("]")).trim());
			offset += 20;
			first = false;
		}
		
	}
	
	
	public void crawlBillsForSingleCongress(String congress, String chamber, String type, String location) throws Exception {
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		crawlBills(congress, chamber, type, fileWriter);
		fileWriter.write("]");
		fileWriter.close();
		
	}
	
	public void crawlBillsForAllCongress(String chamber, String type, String location) throws Exception {
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		for (int i = ProPublicaConstants.START_CONGRESS ; i <= ProPublicaConstants.END_CONGRESS ; i++){
			crawlBills(i+"", chamber, type, fileWriter);
			if (i<115)fileWriter.write(",");
		}
		fileWriter.write("]");
		fileWriter.close();
	}
	
	public void searchBillsForAllCongress(String query, String location) throws Exception {
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		query = query.replace(" ", "%20"); 
		searchBills(query, fileWriter);
		fileWriter.write("]");
		fileWriter.close();	
	}
	

}
