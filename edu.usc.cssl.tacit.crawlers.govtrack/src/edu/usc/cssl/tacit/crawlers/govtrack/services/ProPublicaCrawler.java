package edu.usc.cssl.tacit.crawlers.govtrack.services;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class ProPublicaCrawler {
	
	ProPublicaNetwork network;
	private int offset = 0;
	
	public ProPublicaCrawler(String apiKey) {
		network = new ProPublicaNetwork(apiKey);
	}
	
	private void crawlBills(String congress, String chamber, String type, FileWriter fileWriter, IProgressMonitor monitor) throws Exception {

		String response = null;
		String endpoint = ProPublicaEnpoints.getAllBillsForCongressEndpoint(congress, chamber, type,"0");
		boolean first = true;
		
		offset = 0;
		while(true) {
			if (monitor.isCanceled()){
				throw new OperationCanceledException();
			}
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
			
			if (results.getJSONObject(0).getInt("num_results") == 0) {
				offset -= 20;
				break;
			}
			
			results = results.getJSONObject(0).getJSONArray("bills");
			
			String bills =  results.toString();
			
			if (!first) {
				fileWriter.write(",");
			}
			
			
			monitor.subTask("Documents Downloaded: " + offset);
			monitor.worked(offset);
			ConsoleView.printlInConsoleln("Documents Downloaded: " + offset);
			
			fileWriter.write(bills.substring(bills.indexOf("[")+1,bills.lastIndexOf("]")).trim());
			offset += 20;
			first = false;
		}
		
	}
	
	
	private void searchBills(String query, FileWriter fileWriter, IProgressMonitor monitor) throws Exception {

		String response = null;
		String endpoint = ProPublicaEnpoints.getSearchBillsEndpoint(query, "0");
		boolean first = true;
		
		offset = 0;
		while(true) {
			if (monitor.isCanceled()){
				throw new OperationCanceledException();
			}
			
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
			
			if (results.getJSONObject(0).getInt("num_results") == 0) { 
				offset -= 20;
				break;
			}
			
			results = results.getJSONObject(0).getJSONArray("bills");
			
			String bills =  results.toString();
			
			if (!first) {
				fileWriter.write(",");
			}
			
			monitor.subTask("Documents Downloaded: " + offset);
			monitor.worked(offset);
			ConsoleView.printlInConsoleln("Documents Downloaded: " + offset);
			
			fileWriter.write(bills.substring(bills.indexOf("[")+1,bills.lastIndexOf("]")).trim());
			offset += 20;
			first = false;
			Thread.sleep(200);
			
		}
		
	}
	
	public int getLastCrawlCount() {
		return offset;
	}
	
	
	public void crawlBillsForSingleCongress(String congress, String chamber, String type, String location, IProgressMonitor monitor) throws Exception {
		
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		crawlBills(congress, chamber, type, fileWriter,monitor);
		fileWriter.write("]");
		fileWriter.close();
		
	}
	
	public void crawlBillsForAllCongress(String chamber, String type, String location, IProgressMonitor monitor) throws Exception {
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		for (int i = ProPublicaConstants.START_CONGRESS ; i <= ProPublicaConstants.END_CONGRESS ; i++){
			if (monitor.isCanceled()){
				throw new OperationCanceledException();
			}
			crawlBills(i+"", chamber, type, fileWriter,monitor);
			if (i<115)fileWriter.write(",");
		}
		fileWriter.write("]");
		fileWriter.close();
	}
	
	public void searchBillsForAllCongress(String query, String location, IProgressMonitor monitor) throws Exception {
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		query = query.replace(" ", "%20"); 
		searchBills(query, fileWriter,monitor);
		fileWriter.write("]");
		fileWriter.close();	
	}
	

}
