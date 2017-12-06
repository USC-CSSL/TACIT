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
	private long offset = 0;
	private long billCount = 0;
	
	public ProPublicaCrawler(String apiKey) {
		network = new ProPublicaNetwork(apiKey);
	}
	
	private long crawlBills(String congress, String chamber, String type, long billCountStartValue, FileWriter fileWriter, IProgressMonitor monitor, Long limit) throws Exception {

		String response = null;
		String endpoint = ProPublicaEnpoints.getAllBillsForCongressEndpoint(congress, chamber, type,"0");
		boolean first = true;
		
		offset = 0;
		
		//Initialize bill count with a start value. This is used only when we have to crawl all the congresses. For single congress, we can have start value of 0
		billCount = billCountStartValue;
		int monitorInc = 0;
		while(true) {
			monitorInc = 0;
			if (monitor.isCanceled()){
				throw new OperationCanceledException();
			}
			
			if (billCount >= limit) {
				break;
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
				break;
			}
			
			results = results.getJSONObject(0).getJSONArray("bills");
			
			
			Iterator<Object> iterator =  results.iterator();
			while(iterator.hasNext()) {
				JSONObject obj = (JSONObject)iterator.next();
				obj.put("congress", congress);
				billCount++;
				monitorInc++;
				monitor.subTask("Bill " + billCount + ":\t" + obj.getString("title"));
				ConsoleView.printlInConsoleln("Bill " + billCount + ":\t" + obj.getString("title"));
			}
			
			String bills =  results.toString();
			
			if (!first) {
				fileWriter.write(",");
			}
			
			monitor.worked(monitorInc);
			
			fileWriter.write(bills.substring(bills.indexOf("[")+1,bills.lastIndexOf("]")).trim());
			offset += 20;
			first = false;
			//Thread.sleep(100);
		}
		
		return billCount;
		
	}
	
	
	private void searchBills(String query, FileWriter fileWriter, IProgressMonitor monitor, long limit) throws Exception {

		String response = null;
		String endpoint = ProPublicaEnpoints.getSearchBillsEndpoint(query, "0");
		boolean first = true;
		
		offset = 0;
		billCount = 0;
		int monitorInc = 0;
		while(true) {
			monitorInc = 0;
			if (monitor.isCanceled()){
				throw new OperationCanceledException();
			}
			
			if (billCount >= limit) {
				break;
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
				break;
			}
			
			results = results.getJSONObject(0).getJSONArray("bills");
			
			Iterator<Object> iterator =  results.iterator();
			while(iterator.hasNext()) {
				JSONObject obj = (JSONObject)iterator.next();
				billCount++;
				monitorInc++;
				monitor.subTask("Bill " + billCount + ":\t" + obj.getString("title"));
				ConsoleView.printlInConsoleln("Bill " + billCount + ":\t" + obj.getString("title"));
			}
			
			
			String bills =  results.toString();
			
			if (!first) {
				fileWriter.write(",");
			}
			
			monitor.worked(monitorInc);
			
			fileWriter.write(bills.substring(bills.indexOf("[")+1,bills.lastIndexOf("]")).trim());
			offset += 20;
			first = false;
			//Thread.sleep(100);
			
		}
		
	}
	
	public long getLastCrawlCount() {
		return billCount;
	}
	
	
	public void crawlBillsForSingleCongress(String congress, String chamber, String type, String location, IProgressMonitor monitor,long limit) throws Exception {
		if (limit == -1) {
			limit = Long.MAX_VALUE;
		}
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		crawlBills(congress, chamber, type,0,fileWriter,monitor, limit);
		fileWriter.write("]");
		fileWriter.close();
		
	}
	
	public void crawlBillsForAllCongress(String chamber, String type, String location, IProgressMonitor monitor, long limit) throws Exception {
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		long billCountStart = 0;
		for (int i = ProPublicaConstants.START_CONGRESS ; i <= ProPublicaConstants.END_CONGRESS ; i++){
			if (monitor.isCanceled()){
				throw new OperationCanceledException();
			}
			billCountStart = crawlBills(i+"", chamber, type, billCountStart, fileWriter,monitor, limit);
			if (i<115)fileWriter.write(",");
		}
		fileWriter.write("]");
		fileWriter.close();
	}
	
	public void searchBillsForAllCongress(String query, String location, IProgressMonitor monitor, long limit) throws Exception {
		if (limit == -1) {
			limit = Long.MAX_VALUE;
		}
		FileWriter fileWriter = new FileWriter(new File(location));
		fileWriter.write("[");
		query = query.replace(" ", "%20"); 
		searchBills(query, fileWriter,monitor, limit);
		fileWriter.write("]");
		fileWriter.close();	
	}
	
	public boolean isAPIKeyValid() throws Exception{
		String checkerEnpoint = "https://api.propublica.org/congress/v1/105/house/bills/introduced.json?offset=0";
		int statusCode = network.getResponseCode(checkerEnpoint);
		
		return statusCode<400;
		
	}
	

}
