package edu.usc.cssl.tacit.topicmodel.zlda.services;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;


public class ZlabelLDA {

	/* ToDo - when giving options for LDA - give option to use word indexes as in dictionary (This is what it will most likely be), to use regular indexes and to give stopwords */
	private double[][] alpha, beta; /* ToDo - check to make sure all the lists in this array are of the same size */ 
	private double eta;	/* Confidence score */
	private int[][] documents;
	private int[] fLabel = null; /* ToDo - figure out what to do for non-standard LDA */
	private int[][][] topicSeeds;
	private int numsamp;
	private int numberOfDocuments;
	private int T; /* Number of Topics */
	private int W; /* Number of Words in the vocabulary */
	private int F;
	private double[] alphaSum, betaSum;
	private int[][] init;
	private Random random;
	private int[][] sample;
	private double[][] theta;
	private double[][] phi;
	private Counts counts;
	private Date date;
	private long prevTime;
	private long currentTime;
	
	private class Counts{
		
		int[][] nw;
		int[][] nd;
		int[] nwColSum;
		
		public Counts(Integer w, int t, int d){
			nw = new int[(int)w][t];
			nd = new int[d][t];
			nwColSum = new int[t];
			
			/* Initialize the arrays with 0 values */
			for(int i=0; i<w; i++){
				for(int j=0; j<t; j++){
					nw[i][j] = 0;
				}
			}
			
			for(int i=0; i<d; i++){
				for(int j=0; j<t; j++){
					nd[i][j] = 0;
				}
			}
			
			for(int i=0; i<t; i++){
				nwColSum[i] = 0;
			}
		}
		
	}
	
	/* ToDo - also need phi, theta and sample - write those out */
	
	public int[][] getSample() {
		return sample;
	}

	public double[][] getTheta() {
		return theta;
	}

	public double[][] getPhi() {
		return phi;
	}

	/*  ToDo - Overload this to kingdom come also remember to do init thingy for the arguments */
	public ZlabelLDA(int[][] docs, int[][][] zValues, double eta, double[][] alpha, double[][] beta, int numsamp, int[][] initSample) throws NullPointerException{
		
		if(initSample == null){
			this.init = null;
		}
	
		if(docs == null || zValues == null || alpha == null || beta == null){
			throw new NullPointerException();
		}
		
		documents = docs;
		topicSeeds = zValues;
		this.eta = eta;
		this.alpha = alpha;
		this.beta = beta;
		this.numsamp = numsamp;
		random = new Random(194582);
		sample = new int[docs.length][];
		numberOfDocuments = documents.length;
		for(int i=0; i<documents.length; i++){
			sample[i] = new int[documents[i].length];
		}
		date = new Date();
		
	}
	
	public ZlabelLDA(int[][] docs, int[][][] zValues, double eta, double[][] alpha, double[][] beta, int numsamp) throws NullPointerException{
		this(docs, zValues, eta, alpha, beta, numsamp, null);
	}
	
	private double unif(){
		return random.nextFloat();
	}
	
	private boolean givenInit(){
		
		if(init.length != documents.length){
			ConsoleView.printlInConsoleln("Number of documents/number of init samples mismatch");
			return false;
		}
		
		counts = new Counts(W, T, documents.length);
		
		int[] docInit, docSample;
		int[] doc; 
		int zi;
		int word; 
		
		for(int d=0; d<documents.length; d++){
			
			docInit = init[d];
			doc = documents[d];
			docSample = sample[d];
			
			if(docInit.length != documents[d].length){
				ConsoleView.printlInConsoleln("Init sample/doc-length mismatch");
				return false;
			}
			
			for(int i=0; i<doc.length; i++){
				zi = docInit[i];
				if(zi < 0 || zi >= T){
					ConsoleView.printlInConsoleln("Non-numeric or out of range sample value");
					return false;
				}
				
				word = doc[i];
				docSample[i] = zi;
				counts.nw[(int)word][zi]++;
				counts.nd[d][zi]++;
				counts.nwColSum[zi]++;
			
			}
			
		}
		
		return true;
	
	}
	
	/**
	 * Do an "online" init of Gibbs chain, adding one word
	 * position at a time and then sampling for each new position
	 */
	private void onlineInit(){
		
		/* Initialize variables for use in the loop */
		counts = new Counts(W, T, documents.length);
		
		double[] numerator = new double[T];
		int[] doc;
		int f;
		int word;
		double normSum, alphaJ, betaI, currBetaSum, denomL;
		int[][] docSeeds;
		int[] wordTopicSeeds, docSample;
		boolean foundTopic;
		int sampleValue;	
		
		/* Iterate through the documents */
		for(int d=0; d<documents.length; d++){
			
			doc = documents[d];
			f = fLabel[d];
			docSeeds = topicSeeds[d];
			docSample = sample[d];
			
			for(int i=0; i<doc.length; i++){
				
				word = doc[i];
				normSum = 0;
				
				/* Calculate numerator for each topic */
				for(int j=0; j<T; j++){
					
					/* Initialize variables for this calculation */
					alphaJ = alpha[f][j];
					betaI = beta[j][word];
					currBetaSum = betaSum[j];
					denomL = counts.nwColSum[j] + currBetaSum;
					
					/**
					 *  
					 *  Calculate numerator for this topic 
					 *  Note : alpha denom omitted because it is the same for all topics
					 *   
					 **/
					numerator[j] = (counts.nw[(int)word][j] + betaI)/denomL;
					numerator[j] = numerator[j]*(counts.nd[d][j] + alphaJ);
					
					/* Add a multiplicative penalty if applicable */
					if(docSeeds[i] != null){
						wordTopicSeeds = docSeeds[i];
						foundTopic = false;
						/* Look for the current topic we're looking at in the topic seeds for this word */
						for(int k=0; k<wordTopicSeeds.length; k++){
							if(j == wordTopicSeeds[k]){
								foundTopic = true;
								
							}
						}
						/* Penalize if the topics associated with this word isn't the current topic */
						if(foundTopic == false){
							numerator[j] = numerator[j]*(1 - eta);
						}
					}
					
					/* Add the computed numerator value to norm sum */
					normSum += numerator[j];
					
					
				}
				/* Draw sample and update the count/cache matrices and initial sample vector */
				sampleValue = multSample(numerator, normSum);
				docSample[i] = sampleValue;
				
				counts.nw[(int)word][sampleValue]++;
				counts.nd[d][sampleValue]++;
				counts.nwColSum[sampleValue]++;
			}
			
		}
				
	}
	
	private void gibbsChain(){
		
		double[] numerator = new double[T];
		int[] doc;
		int f, zi;
		Integer word;
		double normSum, alphaJ, betaI, currBetaSum, denomL;
		int[][] docSeeds;
		int[] wordTopicSeeds, docSample;
		boolean foundTopic;
		int sampleValue;
		
		for(int d=0; d<documents.length; d++){
			doc = documents[d];
			f = fLabel[d];
			docSeeds = topicSeeds[d];
			docSample = sample[d];

			for(int i=0; i<doc.length; i++){
				
				zi = docSample[i];
				word = doc[i];
				counts.nw[(int)word][zi]--;
				counts.nd[d][zi]--;
				counts.nwColSum[zi]--;
				
				normSum = 0;
				
				for(int j=0; j<T; j++){
					
					alphaJ = alpha[f][j];
					betaI = beta[j][word];
					currBetaSum = betaSum[j];
					denomL = counts.nwColSum[j] + currBetaSum;
					
					/**
					 *  
					 *  Calculate numerator for this topic 
					 *  Note : alpha denom omitted because it is the same for all topics
					 *   
					 **/
					numerator[j] = (counts.nw[(int)word][j] + betaI)/denomL;
					numerator[j] = numerator[j]*(counts.nd[d][j] + alphaJ);
					
					if(docSeeds[i] != null){
						
						wordTopicSeeds = docSeeds[i];
						foundTopic = false;
						/* Look for the current topic we're looking at in the topic seeds for this word */
						for(int k=0; k<wordTopicSeeds.length; k++){
							
							if(j == wordTopicSeeds[k]){
								foundTopic = true;
							}
						}
						/* Penalize if the topics associated with this word isn't the current topic */
						if(foundTopic == false){
							numerator[j] = numerator[j]*(1 - eta);
						}
					}
					
					/* Add the computed numerator value to norm sum */
					normSum += numerator[j];

				}
				
				/* Draw sample and update the count/cache matrices and initial sample vector */
				sampleValue = multSample(numerator, normSum);
				docSample[i] = sampleValue;
				
				counts.nw[(int)word][sampleValue]++;
				counts.nd[d][sampleValue]++;
				counts.nwColSum[sampleValue]++;

			}
			
		}
		
	}
	
	
	/**
	 * Use final sample to estimate phi = P(w|z)
	 */
	private void estPhi(){
		
		phi = new double[T][(int)W];
		
		Integer colSum, nwct;
		double currBetaSum, betaW;
		
		for(int t=0; t<T; t++){
			
			colSum = counts.nwColSum[t];
			currBetaSum = betaSum[t];
			
			for(int w=0; w<W; w++){
				betaW = beta[t][w];
				nwct = counts.nw[(int)w][t];
				phi[t][w] = (betaW + nwct)/(currBetaSum + colSum);
			}
			
			
		}
		
		return;
		
	}
	
	/**
	 * Use final sample to estimate theta = P(z|d)
	 */
	private void estTheta(){
		
		theta = new double[documents.length][T];
		double[] rowSums = new double[counts.nd.length];
		double rowSum, currAlphaSum, alphaT;
		int f;
		Integer ndct;
		
		for(int i=0; i<documents.length; i++){
			rowSum = 0;
			for(int j=0; j<counts.nd[i].length; j++){
				rowSum = rowSum + counts.nd[i][j];
			}
			rowSums[i] = rowSum;
		}
		
		for(int d=0; d<documents.length; d++){
			rowSum = rowSums[d];
			f = fLabel[d];
			currAlphaSum = alphaSum[f];
			
			for(int t=0; t<T; t++){
				alphaT = alpha[f][t];
				ndct = counts.nd[d][t];
				theta[d][t] = (ndct + alphaT)/(rowSum + currAlphaSum);
			}
			
		}
		
		return;
		
	}
	
	private int multSample( double[] vals, double norm_sum) {
		
		double rand_sample = unif()*norm_sum;
		double tmp_sum = 0;
		int i = 0;
		while(tmp_sum < rand_sample || i == 0){
			tmp_sum += vals[i];
			i++;
		}
		return i-1;
		
	}
	
	private boolean validateInput(){
		
		/* Check if fLabel array has size equal to number of documents */
		
		/* If f-labels not provided, initialize to 0 */
		Integer fmax = 0;
		if(fLabel == null){
			fLabel = new int[documents.length];
			for(int i=0; i<numberOfDocuments; i++){
				fLabel[i] = 0;
			}
		}
		else{
		/* If f-label is provided, check validity - non-negative values, etc. */
			if(fLabel.length != numberOfDocuments){
				ConsoleView.printlInConsoleln("f-label array has size less than the number of documents");
				appendLog("f-label array has size less than the number of documents");
				return false;
			}
			else{
				for(int i=0; i<fLabel.length; i++){
					if(fLabel[i] < 0){
						ConsoleView.printlInConsoleln("Negative f-label - not valid input");
						appendLog("Negative f-label - not valid input");
						return false;
					}
					else if(fLabel[i] > fmax){
						fmax = fLabel[i];
					}
				}
			}
		}
		
		/* The number of maps in topicSeeds should be the same as the number of documents */
		if(topicSeeds.length != documents.length){
			ConsoleView.printlInConsoleln("Topic Seeds array/ no. of documents size mismatch");
			appendLog("Topic Seeds array/ no. of documents size mismatch");
			return false;
		}
		
		/* Get information from parameters and check dimensionality agreement */
		
		if(alpha[0] == null || beta[0] == null){
			ConsoleView.printlInConsoleln("Invalid alpha or beta value");
			appendLog("Invalid alpha or beta value");
			return false;
		}
		else {
			F = alpha.length;
			T = alpha[0].length;
			W = beta[0].length;
		}
		
		
		/* fmax needs to be the same as the dimensions of alpha */
		if(F-1 != fmax){
			ConsoleView.printlInConsoleln("Alpha/f dimensionality mismatch");
			appendLog("Alpha/f dimensionality mismatch");
			return false;
		}
		
		/**
		 *  Check all elements of alpha, beta etc. have same size between them 
		 **/
		
		/* Beta must have the same number of rows as the number of topics we want */
		if(T != beta.length){
			ConsoleView.printlInConsoleln("Beta size/no. of topics mismatch");
			appendLog("Beta size/no. of topics mismatch");
			return false;
		}
		
		for(int i=1; i<alpha.length; i++){
			/* The lists in alpha must have the same dimensions */
			if(alpha[i].length != T){
				ConsoleView.printlInConsoleln("Alpha arrays do not have the same dimensionality");
				appendLog("Alpha arrays do not have the same dimensionality");
				return false;
			}
		}
		
		for(int i=1; i<beta.length; i++){
			/* The lists in beta must have the same dimensions */
			if(beta[i].length != W){
				ConsoleView.printlInConsoleln("Beta arrays do not have the same dimensionality");
				appendLog("Beta arrays do not have the same dimensionality");
				return false;
			}
		}
		
		/* all alpha and beta values must be +ve */
		for(int i=0; i<alpha.length; i++){
			for(int j=0; j<alpha[i].length; j++){
				if(alpha[i][j] < 0){
					ConsoleView.printlInConsoleln("Invalid value in the alpha array");
					appendLog("Invalid value in the alpha array");
					return false;
				}
			}
		}
		
		for(int i=0; i<beta.length; i++){
			for(int j=0; j<beta[i].length; j++){
				if(beta[i][j] < 0){
					ConsoleView.printlInConsoleln("Invalid value in the beta array");
					appendLog("Invalid value in the beta array");
					return false;
				}
			}
		}
		
		
		/* Validate that the zlabels are all positive, and that none of the values is larger than T */
		
		
		for(int i=0; i<topicSeeds.length; i++){
			for(int j=0; j<topicSeeds[i].length; j++){
				if(topicSeeds[i][j] != null){
					for(int k=0; k<topicSeeds[i][j].length; k++){
						if(topicSeeds[i][j][k] < 0 || topicSeeds[i][j][k] >= T ){
							ConsoleView.printlInConsoleln("The topic seed value is invalid");
							appendLog("The topic seed value is invalid");
							return false;
						}
					}
				}
			}
		}
		
		/* Validate that the document entries all have postive values, and values within the size of vocabulary */
		
		for(int i=0; i<documents.length; i++){
			for(int j=0; j<documents[i].length; j++){
				if(documents[i][j] < 0 || documents[i][j] >= W){
					ConsoleView.printlInConsoleln("The word value in document is invalid");
					appendLog("The word value in document is invalid");
					return false;
				}
			}
		}
		
		/* Compute alphaSum and betaSum to prep the data-set */
		double sum;
		
		/* All input is alright, okay to create new lists for alphaColSum and betaColSum */
		alphaSum = new double[alpha.length];
		betaSum = new double[beta.length];
		
		for(int i=0; i<alpha.length; i++){
			sum = 0;
			for(int j=0; j<alpha[i].length; j++){
				sum = sum + alpha[i][j];
			}
			alphaSum[i] = sum;
		}
		
		for(int i=0; i<beta.length; i++){
			sum = 0;
			for(int j=0; j<beta[i].length; j++){
				sum = sum + beta[i][j];
			}
			betaSum[i] = sum;
		}
		
		return true;
		
	}
	
	public boolean zLDA(SubProgressMonitor monitor){
	
		if(validateInput() != true){
			ConsoleView.printlInConsoleln("Invalid Input");
			return false; 
		}
		prevTime = 0;
		currentTime = System.currentTimeMillis();
		if(init == null){
			onlineInit();
		}
		else {
			if(givenInit() == false){
				return false;
			}
		}
		 
		prevTime = currentTime; 
		currentTime = System.currentTimeMillis();
		for(int si=1; si<=numsamp; si++){
			gibbsChain();
			monitor.subTask("Calculating Z label ... Ran Gibbs Sampler "+si+ " time(s)");
		}
		
		estPhi();
		estTheta();
		
		return true;
		
	}
	
	private StringBuilder readMe = new StringBuilder();
	private void appendLog(String message){
		ConsoleView.printlInConsoleln(message);
			readMe.append(message+"\n");
	}
	public void writeReadMe(String location){
		File readme = new File(location+"/README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform.getBundle("edu.usc.cssl.tacit.plugins.zlda").getHeaders().get("Bundle-Version");
			String appV = Platform.getBundle("edu.usc.cssl.tacit.application").getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("Zlabel LDA Output\n--------------------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
