package edu.usc.cssl.tacit.crawlers.stackexchange.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.simple.JSONObject;

public class StackExchangeCrawler {
	
//	//this code allows to break limit if client jdk/jre has no unlimited policy files for JCE.
//	//it should be run once. So this static section is always execute during the class loading process.
//	//this code is useful when working with Bouncycastle library.
//	static {
//	    try {
//	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
//	        field.setAccessible(true);
//	        field.set(null, java.lang.Boolean.FALSE);
//	    } catch (Exception ex) {
//	    }
//	}
	
	static StackExchangeApi api;
	private static String API_KEY = "";// enter key here
	private static String AUTH_TOKEN = "";// enter auth toker here
	static StackExchangeCrawler cr;

	public static void main(String args[]) {
		cr = new StackExchangeCrawler();
		cr.returnUsers(null);
		
	}

	public  void initiateCrawler(String[] parameters) {
		// enter auth token here
		api = new StackExchangeApi(API_KEY);// enter key here
		returnUsers(cr.stackoverflow(api, ""));
		// api.authorize(AUTH_TOKEN);

	}

	public  StackExchangeSite stackoverflow(StackExchangeApi api, String site) {
		// site can be configured to access other sites as well
		StackExchangeSite siteService = api.getSiteService(StackExchangeSite.STACK_OVERFLOW);
		return siteService;
	}

	// to obtain all users
	public void returnUsers(StackExchangeSite siteService) {
		
	}

}
