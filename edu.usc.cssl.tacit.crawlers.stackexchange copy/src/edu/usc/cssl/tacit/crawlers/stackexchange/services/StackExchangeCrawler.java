package edu.usc.cssl.tacit.crawlers.stackexchange.services;

import java.io.IOException;

import retrofit2.Call;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Item;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.User;

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
	static StackExchangeCrawler cr;

	public static void main(String args[]) {
		cr = new StackExchangeCrawler();
		cr.returnUsers(cr.stackoverflow(new StackExchangeApi(), null));
		
	}

	public  StackExchangeSite stackoverflow(StackExchangeApi api, String site) {
		StackExchangeSite siteService = api.getSiteService(StackExchangeSite.STACK_OVERFLOW);
		return siteService;
	}

	// to obtain all users
	public void returnUsers(StackExchangeSite siteService) {
		try {
			Call<Item> call = siteService.getUsers();
			Item i = call.execute().body();
			for(User user: i.items){
				System.out.println(user.id);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}

}
