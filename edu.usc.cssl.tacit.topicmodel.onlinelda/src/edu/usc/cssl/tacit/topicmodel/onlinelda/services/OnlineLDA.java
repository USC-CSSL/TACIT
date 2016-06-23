package edu.usc.cssl.tacit.topicmodel.onlinelda.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class OnlineLDA {
	
	private List<String> documentList;
	private String dictionaryLocation;
	private String outputLocation;
	private int noOfTopics = 10;
	private int noOfWordsPerTopic = 10;
	private int batchSize = 5;
	
	public OnlineLDA(List<String> documentList, String dictionaryLocation, String outputLocation,int noOfTopics,int noOfWordsPerTopic){
		this.documentList = documentList;
		this.dictionaryLocation = dictionaryLocation;
		this.outputLocation = outputLocation;
		this.noOfTopics = noOfTopics;
		this.noOfWordsPerTopic = noOfWordsPerTopic;
		
	}

	public void invokeOnlineLDA(IProgressMonitor monitor)throws OperationCanceledException,Exception{
		
		ConsoleView.printlInConsoleln("Number of topics: " + noOfTopics );
		ConsoleView.printlInConsoleln("Number of tokens per topics: " + noOfWordsPerTopic );
		ConsoleView.printlInConsoleln("Document batch size: " + batchSize);
		
		List<String> docs = getDocs(documentList,monitor);
		ConsoleView.printlInConsoleln("Total documents extracted" + docs.size());
		Vocabulary vocab = new Vocabulary(dictionaryLocation);

		int W = vocab.words.size();
		int D = docs.size();
		double alpha = 1.d/noOfTopics;
		double eta = 1.d/noOfTopics;
		double tau = 1024.;
		double kappa = 0.7d;
				
		int docsToAnalyze = (int)D/batchSize;
		
		OnlineLDAAnalysis olda = new OnlineLDAAnalysis(W,noOfTopics,D,alpha,eta,tau,kappa);
		OnlineLDAOutput out = null;
		
		ArrayList<Documents> dList = new ArrayList<Documents>();
		
		for(int i=0; i*batchSize<docs.size(); i++){
			//System.out.println(i);
			int max = Math.min((i+1)*batchSize, docs.size());
			Documents d = new Documents(docs.subList(i*batchSize, max), vocab, monitor);
			dList.add(d);
			out = olda.updateLambda(d);
		}
		
		Date dateObj = new Date();
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		PrintWriter writer1 = new PrintWriter(outputLocation+File.separator+"lambda_"+df.format(dateObj)+".txt", "UTF-8");
		PrintWriter writer2 = new PrintWriter(outputLocation+File.separator+"output_"+df.format(dateObj)+".txt", "UTF-8");
		out.printTopics(writer1, writer2,dList,vocab,noOfWordsPerTopic, monitor);

		
	}
	
	//To read each files data into a String and adding it to a List of Strings
	public static List<String> getDocs(List<String> documentList, IProgressMonitor monitor) throws IOException{
		List<String> data = new ArrayList<String>();
		ConsoleView.printlInConsoleln("Document extraction started..");
		monitor.subTask("Extracting document...");
		for(String docPath: documentList){
			File document = new File(docPath);
			if (document.getName().contains("DS_Store")){
				continue;
			}
			
			String line;
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader(document));
			
			while((line = br.readLine()) != null){
				sb.append(line);
				sb.append(' ');
			}
			br.close();
			data.add(sb.toString());
			
			monitor.worked(1);
			if (monitor.isCanceled()){
				throw new OperationCanceledException();
			}
		}
		
		return data;
	}
}
