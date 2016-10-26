package edu.usc.cssl.tacit.crawlers.govtrack.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.IProgressMonitor;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;



public class GovTrackCrawler {
	static String strDate;
	static String strName;
	static String strTitle;
	JsonGenerator jsonGenerator;
	JsonFactory jsonfactory;
	String outputDir = "";

	
	private void setDir() {
		// Instantiate JSON writer
		String output = outputDir + File.separator + "govtrack.json";
		File streamFile = new File(output);
		jsonfactory = new JsonFactory();
		try {
			jsonGenerator = jsonfactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean crawl(String outputDir, String searchTerm, String congressNumber, String billType, String currentStatus, int limit, IProgressMonitor monitor) throws IOException, KeyManagementException, NoSuchAlgorithmException{
		boolean flag = true;
		this.outputDir = outputDir;
		setDir();
		
		
		
		
		
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
        // Install the all-trusting trust manager
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        //3059249

        Reader reader = null;
        BufferedReader br = null;  
		
		String appendQuery = "";
		if(!billType.isEmpty())
			appendQuery+="&bill_type="+billType;
		if(!currentStatus.isEmpty())
			appendQuery+="&current_status="+currentStatus;
		if(!searchTerm.isEmpty())
			appendQuery+="&q="+searchTerm;
		if(!congressNumber.isEmpty())
			appendQuery+="&congress="+congressNumber;
		System.out.println("Secret of searchTerm is --"+searchTerm+"--");
        System.out.println(appendQuery);
        System.out.println("Limit is "+limit);
        
        int offset = 0;
        int i = 0;
        boolean first = true;
        int totalRecords = limit;
        int progressIncrement = -1;
    HERE:    while (true) {
        	
        	String url = "https://www.govtrack.us/api/v2/bill?limit=1000&offset="+offset+appendQuery;
			URL obj = new URL(url);
	        URLConnection con = obj.openConnection();
	        reader = new InputStreamReader(con.getInputStream());
	        br = new BufferedReader(reader); 
	        String line = "";
	        StringBuilder json = new StringBuilder("");
	        while ((line = br.readLine()) != null) {
	            //System.out.println(line);
	        	//bw.write(line);
	        	
	        	json.append(line);
	        }        
	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode actualObj = mapper.readTree(json.toString());
	        JsonNode array = actualObj.get("objects");
	        if(first) {
	        	totalRecords = actualObj.get("meta").get("total_count").asInt();
	        	if(totalRecords==0) {
	        		flag = false;
	        		break HERE;
	        	}
	        	progressIncrement = 500338/totalRecords;
	        }
	        boolean flagCheck = true;
	        for (final JsonNode objNode : array) {
	        	flagCheck = false;
	        	String title = objNode.get("title").asText();
	        	String date = objNode.get("introduced_date").asText();
	        	String congress = objNode.get("congress").asText();
	        	String bill_Type = objNode.get("bill_type").asText();
	        	String billResolutionType = objNode.get("bill_resolution_type").asText();
	        	
	        	String sponsorName = "NA";
	        	try {
	        		sponsorName = objNode.get("sponsor").get("name").asText();
	        	} catch(Exception e) {
	        		System.out.println(objNode.asText() +" --------- bad sponsor");
	        	}
	        	String sponsorParty = "NA";
	        	try {
	        		sponsorParty = objNode.get("sponsor_role").get("party").asText();
	        	} catch(Exception e){
	        		System.out.println(objNode.asText() +" --------- bad sponsor party");
	        	}
	        	i+=1;
	        	monitor.subTask("Writing Bill: "+title);
				monitor.worked(progressIncrement);
	        	jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("Bill_Name", title); 
				jsonGenerator.writeStringField("Introduced_Date", date); 
				jsonGenerator.writeStringField("Congress", congress); 
				jsonGenerator.writeStringField("Bill_Type", bill_Type); 
				jsonGenerator.writeStringField("Bill_Resolution_Type", billResolutionType); 
				jsonGenerator.writeStringField("Sponsor_Name", sponsorName);
				jsonGenerator.writeStringField("Sponsor_Party", sponsorParty);
				jsonGenerator.writeEndObject();
				
	        	if(i==limit)
	        		break HERE;
	        }
	        if (flagCheck)
	        	break HERE;
	        System.out.println("------------------------------------------------------------------"+i);
        	offset+=1000;
        }
        
        if(i==0)
        	flag = false;
        System.out.println(i);
        
        br.close();
		
        try {
			jsonGenerator.writeEndArray();
			jsonGenerator.flush();
			jsonGenerator.close();
			ConsoleView.printlInConsoleln(i + " bill(s) downloaded.");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
        
        
		return flag;
			
	}
	

}

/*


 
 
 
 
 
 
 
 
 
 
 
 
 
 */
