package edu.usc.nlputils.common;

import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.ICommonUiConstants;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.cssl.nlputils.repository.Activator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.Platform;

public class TacitUtility {

	
	public static void createRunReport (String location, String title, Date dateObj) {
		
		Boolean createReadMe;
		String readMeStr = CommonUiActivator.getDefault().getPreferenceStore().getString(ICommonUiConstants.CREATE_RUNREPORT);
		if (readMeStr == null || readMeStr == "") createReadMe = true;
		else createReadMe = Boolean.valueOf(readMeStr);
		
		if (!createReadMe) return;
		
		DateFormat df = new SimpleDateFormat("dd-MM-yy-HH-mm-ss");
		
		File readme = new File(location + System.getProperty("file.separator")+title.replace(" ", "-")+"-run-report-"+df.format(dateObj)+".txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String appV = "TACIT v" + Platform
					.getBundle(Activator.PLUGIN_ID).getHeaders()
					.get("Bundle-Version");
			Date date = new Date();
			bw.write(title+" Output");
			bw.newLine();
			bw.write("--------------------------");
			bw.newLine();
			bw.newLine();
			bw.write("Application: "+ appV );
			bw.newLine();
			bw.write("Date: " + date.toString());
			bw.newLine();
			bw.close();
			ConsoleView.printlInConsoleln("Finished creating run report file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
