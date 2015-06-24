package edu.usc.nlputils.common;

import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.ICommonUiConstants;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.cssl.nlputils.repository.Activator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.eclipse.core.runtime.Platform;

public class TacitUtility {

	
	public static void createReadMe (String location, String title) {
		
		Boolean createReadMe;
		String readMeStr = CommonUiActivator.getDefault().getPreferenceStore().getString(ICommonUiConstants.CREATE_README);
		if (readMeStr == null || readMeStr == "") createReadMe = true;
		else createReadMe = Boolean.valueOf(readMeStr);
		
		if (!createReadMe) return;
		
		StringBuilder readMe = new StringBuilder();
		File readme = new File(location + "/README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String appV = "TACIT v" + Platform
					.getBundle(Activator.PLUGIN_ID).getHeaders()
					.get("Bundle-Version");
			Date date = new Date();
			bw.write(title+" Output\n--------------------------\n\nApplication: "
					+ appV + "\nDate: " + date.toString() + "\n\n");
			bw.write(readMe.toString());
			bw.close();
			ConsoleView.printlInConsoleln("Finished creating README file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
