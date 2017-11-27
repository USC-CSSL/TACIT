package edu.usc.cssl.tacit.crawlers.govtrack.services;
import java.io.File;
import java.io.FileWriter;

public class ProPublicaEnpoints {
		
	public static String getAllBillsForCongressEndpoint(String congress, String chamber,String type,String offset) throws Exception{
		if (congress == null ||congress.equals("") || chamber == null || chamber.equals("") ||  type == null || type.equals("")) {
			throw new Exception("API parameters missing...");
		}
		
		StringBuilder sb = new StringBuilder(ProPublicaConstants.BASE_URL);
		sb.append("/" + congress);
		sb.append("/" + chamber);
		sb.append("/bills");
		sb.append("/" + type + ".json");
		sb.append("?offset="+offset);
		
		return sb.toString();
	}
	
	public static String getSearchBillsEndpoint(String query,String offset) throws Exception{
		if (query == null ||query.equals("") ) {
			throw new Exception("API parameters missing...");
		}
		
		StringBuilder sb = new StringBuilder(ProPublicaConstants.BASE_URL);
		sb.append("/bills/search.json?");
		sb.append("query=" + query);
		sb.append("&offset="+offset);
		
		return sb.toString();
	} 

}
