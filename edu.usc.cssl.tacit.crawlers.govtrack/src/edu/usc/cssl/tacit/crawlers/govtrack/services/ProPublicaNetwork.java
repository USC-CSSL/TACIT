package edu.usc.cssl.tacit.crawlers.govtrack.services;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ProPublicaNetwork {
	
	private String apiKey;
	
	private CloseableHttpClient client = HttpClients.custom().build();
	
	private HttpGet httpGet;
	
	public ProPublicaNetwork(String apiKey) {
		this.apiKey =  apiKey;
	}
	
	
	public String sendGETRequest(String endpoint) throws Exception {
		StringBuilder body = new StringBuilder();
		
		httpGet = new HttpGet(endpoint);
		httpGet.setHeader("X-API-Key", apiKey);
		
		HttpResponse response = client.execute(httpGet); // execute httpGet
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            HttpEntity e = response.getEntity();
            String entity = EntityUtils.toString(e);
            body.append(entity);
        } else {
            throw new Exception("Something went wrong.Status code: "+statusCode);
        }
        httpGet.releaseConnection();
        return body.toString();
		
	}
	
	public int getResponseCode(String endpoint) throws Exception{
		httpGet = new HttpGet(endpoint);
		httpGet.setHeader("X-API-Key", apiKey);
		
		HttpResponse response = client.execute(httpGet); // execute httpGet
        StatusLine statusLine = response.getStatusLine();
        
        return statusLine.getStatusCode();
	}

}
