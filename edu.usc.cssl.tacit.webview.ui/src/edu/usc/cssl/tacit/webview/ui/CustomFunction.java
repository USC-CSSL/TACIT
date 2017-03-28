package edu.usc.cssl.tacit.webview.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.osgi.framework.Bundle;

public class CustomFunction extends BrowserFunction {

	Browser browser = null;
	String functionName = null;

	public CustomFunction(Browser browser, String name) {
		super(browser, name);
		this.browser = browser;
		this.functionName = name;
	}

	public Object function (Object[] args)
	{
		Bundle bundle = Platform.getBundle("edu.usc.cssl.tacit.webview.ui");
		URL fileURL = bundle.getEntry("test");
		try {
			
			fileURL = new URL(FileLocator.resolve(fileURL).toString().replace(" ", "%20"));
		} catch (Exception e) {
			
		}
			
		//System.out.println(fileURL.toString());
		File file = new File(fileURL.toString().substring(5));

		FileReader fr = null;
		String retValue = "";
		try {
			fr = new FileReader(file);
		
		BufferedReader br = new BufferedReader(fr);
		retValue = br.readLine().trim();
		
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		return retValue;
	}
}