package edu.usc.cssl.tacit.topicmodel.slda.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.core.runtime.NullProgressMonitor;

import cc.mallet.classify.Classifier;
import edu.usc.cssl.tacit.topicmodel.lda.services.LdaAnalysis;

public class SLDA {
	public void startSlda(String computationalFolder,  String output){
		double threshold;
		File trainFile = null;
		String corpus = computationalFolder+File.separator+"temp";
		LdaAnalysis lda = new LdaAnalysis();
		String name =  computationalFolder+ File.separator+ "OutputLDAData";
		
		File f = new File(name);
		if(!f.exists()){
			f.mkdirs();
		}
		
		lda.initialize(corpus , 5, name, false);
		threshold = 1.0/5;
		Date dateObj = new Date();
		try {
			lda.doLDA(new NullProgressMonitor(), dateObj);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//Writing to a training/testing Folders is remaining.
		File[] files = f.listFiles();  
		for(File file : files){
			if(file.getName().contains("topic-composition") && file.getName().contains(".csv"))
				{
				try {
					String line;
					BufferedReader br = new BufferedReader(new FileReader(file));
					trainFile = new File(computationalFolder+File.separator+"train");
					File testFile = new File(computationalFolder+File.separator+"test");
					if(!testFile.exists())
						testFile.mkdirs();
					BufferedWriter bw ;
					int count = 0;
					while((line = br.readLine())!=null){
						{	if(count==0){
								count++;
								continue;
							}
							String contentString;
							String []content = line.split(",");
							if(content[1].contains(".DS_Store"))
								continue;
							contentString = content[1]+", ";
							int dirStart = content[1].lastIndexOf(File.separator);
							String directory = content[1].substring(0, dirStart);
							int dirEnd = directory.lastIndexOf(File.separator);
							String label = content[1].substring(dirEnd+1, dirStart);
							File f1;
							if(content[1].contains("train")){
								f1= new File(trainFile+File.separator+label+File.separator+content[1].substring(dirStart+1));
								File dir = new File(trainFile+File.separator+label);
								if(!dir.exists())
									dir.mkdirs();
							}
							else{
								f1 = new File(testFile+File.separator+content[1].substring(dirStart+1));
							}
							bw = new BufferedWriter(new FileWriter(f1));
							contentString += label+", ";
							for(int i = 2 ;i <content.length; i++){
								if(content[i].equals("null"))
									continue;
								if(Double.parseDouble(content[i])>threshold)
									bw.write((i-2)+" ");
							}
							bw.flush();
							bw.close();
							
						}
						
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				}
		}
		
		NaiveBayesTest nb = new NaiveBayesTest();
		Classifier classifier = nb.trainClassifier(nb.formatData(computationalFolder+File.separator+"train"));
		File dir = new File(computationalFolder+File.separator+"test");
		try {
			nb.printLabelings(classifier, new File[]{dir}, output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
