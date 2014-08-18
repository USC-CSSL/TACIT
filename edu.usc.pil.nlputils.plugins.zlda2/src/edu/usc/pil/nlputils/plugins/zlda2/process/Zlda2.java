/*
 * Z-label LDA wrapper for Prof. Kenji Sagae's zlab.py
 * Author: Aswin Rajkumar, aswin.rajkumar@usc.edu
 * 
 * Requires Python 2.7.8 and numpy, not the ones that come with Mac OS X
 * Build the extensions by following the install.sh script file
 * Mac OS X keeps using the bundled python, thus throwing errors.
 * Running the new python by giving its complete path did not work -- new ProcessBuilder("/Library/Frameworks/Python.framework/Versions/2.7/bin/python","zlab.py",topicFile,inputDir,maxTopics);
 * 
 * Solution: Start a processBuilder instance and call bash. Get the environment variable PATH, add the new Python directory to it, 
 * Redirect error and output streams to a text file. Else it will not be displayed.
 * Wait for the process to end.
 * 
 * Problems: Mac specific. Have to build the application for each platform. also, no Bash for windows. Use cmd
 * When run through the GUI, the working directory changes. Hence, currently the path is hardcoded. Maybe we can save the installation directory and then use getResource for the rest of the path.
 * Check if the files will be bundled in the jar file.
 */

package edu.usc.pil.nlputils.plugins.zlda2.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class Zlda2 {
	public void callPython(String topicFile, String inputDir, String maxTopics, String outputFile){
		/* None of these techniques worked
		System.out.println(System.getProperty("user.dir"));
		System.out.println(this.getClass().getResource("..").getPath());
		File root = new File(this.getClass().getResource("..").getFile());
		File[] contents = root.listFiles();
		for (File file:contents)
		System.out.println(file.getAbsolutePath());
		*/
		
		File cheatFile = new File("cheatFile.txt");
		System.out.println(cheatFile.getAbsolutePath());
		File output = new File(outputFile);
		File error = new File("/Users/ashmaverick/Downloads/zlab-0.1/error.txt");
		//File commands = new File("commands.txt");
		
		//ProcessBuilder cmd = new ProcessBuilder("/bin/bash","-c","echo $PATH");
		//ProcessBuilder cmd = new ProcessBuilder("/bin/bash","-c","python -V");
		//String command = "python "+System.getProperty("user.dir")+System.getProperty("file.separator")+"zlab.py "+topicFile+" "+inputDir+" "+maxTopics;
		String command = "python "+"/Users/ashmaverick/git/USC-NLPUtils/edu.usc.pil.nlputils.plugins.zlda2/"+"zlab.py "+topicFile+" "+inputDir+" "+maxTopics;
		System.out.println(command);
		ProcessBuilder cmd = new ProcessBuilder("/bin/bash","-c",command);
		Map<String,String> env = cmd.environment();
		env.put("Name","Aswin");
		env.put("PATH", "/Library/Frameworks/Python.framework/Versions/2.7/bin:"+env.get("PATH"));
		cmd.redirectError(error);
		cmd.redirectOutput(output);
		//cmd.directory(new File(System.getProperty("user.dir")));
		//System.out.println("Setting working directory "+System.getProperty("user.dir"));
		
		cmd.directory(new File("/Users/ashmaverick/git/USC-NLPUtils/edu.usc.pil.nlputils.plugins.zlda2"));

		File zlab = new File(System.getProperty("user.dir")+System.getProperty("file.separator")+"zlab.py");
		System.out.println(zlab.exists());
		
		/* No files except Eclipse and Eclipse.ini
		File[] files = new File(System.getProperty("user.dir")).listFiles();
		for (File file:files)
		System.out.println(file.getAbsolutePath());
		*/
		//cmd.redirectInput(commands);
		//cmd.redirectError(error);
		//cmd.redirectOutput(output);
		///Library/Frameworks/Python.framework/Versions/2.7/bin/
		ProcessBuilder pb = new ProcessBuilder("/Library/Frameworks/Python.framework/Versions/2.7/bin/python","zlab.py",topicFile,inputDir,maxTopics);
		pb.directory(new File("/Users/ashmaverick/Downloads/zlab-0.1/"));
		pb.redirectOutput(output);
		pb.redirectError(error);
		try {
			Process p = cmd.start();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
