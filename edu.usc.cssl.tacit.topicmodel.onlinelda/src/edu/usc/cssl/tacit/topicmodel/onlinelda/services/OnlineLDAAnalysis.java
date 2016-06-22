package edu.usc.cssl.tacit.topicmodel.onlinelda.services;

import edu.usc.cssl.tacit.topicmodel.onlinelda.services.Matrix;
import static edu.usc.cssl.tacit.topicmodel.onlinelda.services.OnlineLDAUtils.*;

import java.util.Arrays;

public class OnlineLDAAnalysis {
	/*
    Arguments:
    K: Number of topics
    vocab: A set of words to recognize. When analyzing documents, any word
       not in this set will be ignored.
    W: vocabulary length
    D: Total number of documents in the population. For a fixed corpus,
       this is the size of the corpus. In the truly online setting, this
       can be an estimate of the maximum number of documents that
       could ever be seen.
       
    alpha: Hyperparameter for prior on weight vectors theta
    eta: Hyperparameter for prior on topics beta
    tau0: A (positive) learning parameter that downweights early iterations
    kappa: Learning rate: exponential decay rate---should be between
         (0.5, 1.0] to guarantee asymptotic convergence.

    Note that if you pass the same set of D documents in every time and
    set kappa=0 this class can also be used to do batch VB.
    */
	
		
	public final static double MEAN_CHANGE_THRESHOLD = 0.001;//1e-5;
	public final static int NUM_ITERATIONS = 100;

    private int batchCount;
    
    private final int W;
    private final int K;
    private final int  D;
    
    private final double alpha;
    private final double eta;
    private final double tau0;
    private final double kappa;
    private double rhot;
    
    private Matrix lambda;
    private Matrix eLogBeta;
    private Matrix expELogBeta;
    private Matrix gamma;
    private Matrix sStats;
    
    private double updatect = 0;
    
    public OnlineLDAAnalysis(int W, int K, int D, double alpha, double eta, double tau, double kappa) {
    	this.W = W;
    	this.K = K;
		this.D = D;
		this.alpha = alpha;
		this.eta = eta;
		this.tau0 = tau + 1;
		this.kappa = kappa;
		
		this.batchCount = 0;
		
		//initialize the variational distribution q(beta|lambda)
		this.lambda = gammaSample(K, W);
		//printMatrix(lambda);
		this.eLogBeta = dirichletExpectation(lambda.values);
		this.expELogBeta = exp(eLogBeta.values);
		
	}
    
    public void eStep(Documents docs){
    	
    	int batchD = docs.size(); //batch size
    	int [][] wordIds = docs.getWordIds();
        int [][] wordCts = docs.getWordCts();
        
    	//Initialize the variational distribution q(theta|gamma) for the mini-batch
        this.gamma = gammaSample(batchD, K);
        //printMatrix(gamma);
        Matrix eLogTheta = dirichletExpectation(gamma.values);
        Matrix expELogTheta = exp(eLogTheta.values);
        
       this.sStats = lambda.zeros();
       
       double meanchange = 0;
       
       //Now, for each document d update that document's gamma and phi
       for (int d=0; d<batchD; d++){
    	   int[] ids = wordIds[d];
    	   LinearArray lids = new LinearArray(wordIds[d]); 
    	   
    	   //if(ids.length == 0)
               //continue;
    	   
    	   LinearArray cts = new LinearArray(wordCts[d]); 
    	   
    	   LinearArray gammaD = gamma.getRow(d);
    	   LinearArray eLogThetaD = eLogTheta.getRow(d);
    	   LinearArray expELogThetaD = expELogTheta.getRow(d);
    	   Matrix expELogBetaD = this.expELogBeta.getCols(ids);
    	   LinearArray phiNorm = expELogThetaD.dot(expELogBetaD).add(1E-100);    	   
    	   
           //Iterate between gamma and phi until convergence	
    	   for(int it=0; it<NUM_ITERATIONS; it++){
    		   LinearArray lastGamma = gammaD;
    		   LinearArray temp1 = cts.div(phiNorm).dot(expELogBetaD.transpose());
    		   gammaD = expELogThetaD.product(temp1).add(alpha);
    		   eLogThetaD = dirichletExpectation(gammaD.values);
    		   expELogThetaD = exp(eLogThetaD.values);
    		   phiNorm = expELogThetaD.dot(expELogBetaD).add(1E-100);
    		   
    		   //If gamma hasn't changed much, we're done.
               meanchange = (gammaD.sub(lastGamma)).mean();
               if (meanchange < MEAN_CHANGE_THRESHOLD)
            	   break;
    	   }
    	   
    	   gamma.setRow(d,gammaD);    	   
    	   Matrix out = expELogThetaD.outer(cts.div(phiNorm));
    	   sStats.incCols(ids, out);
       }
       
       sStats = sStats.product(expELogBeta);
    }
    
    public OnlineLDAOutput updateLambda(Documents docs){
    	/*
        First does an E step on the mini-batch given in wordids and
        wordcts, then uses the result of that E step to update the
        variational parameter matrix lambda.

        Arguments:
        docs:  List of D documents. Each document must be represented
               as a string. (Word order is unimportant.) Any
               words not in the vocabulary will be ignored.

        Returns gamma, the parameters to the variational distribution
        over the topic weights theta for the documents analyzed in this
        update.

        Also returns an estimate of the variational bound for the
        entire corpus for the OLD setting of lambda based on the
        documents passed in. This can be used as a (possibly very
        noisy) estimate of held-out likelihood.
        */
    	
    	/*rhot will be between 0 and 1, and says how much to weight
          the information we got from this mini-batch.*/
    	this.rhot = Math.pow(this.tau0 + this.updatect, -this.kappa);
    	//System.out.println("Rhot="+rhot);
    	
    	/*Do an E step to update gamma, phi | lambda for this
          mini-batch. This also returns the information about phi that
          we need to update lambda.*/
    	eStep(docs);

    	/*Estimate held-out likelihood for current values of lambda.*/
        double bound = approxBound(docs);
        /*Update lambda based on documents.*/
        Matrix t1 = this.lambda.product(1-rhot);
        Matrix t2 = (sStats.product((double)D/docs.size())).add(eta);
        t2 = t2.product(rhot);
        this.lambda = t1.add(t2);
        
        this.eLogBeta = dirichletExpectation(lambda.values);
        this.expELogBeta = exp(eLogBeta.values);
        
        this.updatect += 1;
        
        return new OnlineLDAOutput(lambda, gamma, bound, docs, D);
    }
    
    public double approxBound(Documents docs){
    	int batchD = docs.size(); //batch size
    	    	
    	int [][] wordIds = docs.getWordIds();
        int [][] wordCts = docs.getWordCts();
    	
        double score = 0d;
        
        Matrix eLogTheta = dirichletExpectation(gamma.values);
        Matrix expELogTheta = exp(eLogTheta.values);
        double tMax = 0;
        //E[log p(docs | theta, beta)]
        for (int d=0; d<batchD; d++){
        	LinearArray ids = new LinearArray(wordIds[d]);
     	   	LinearArray cts = new LinearArray(wordCts[d]);
     	   	LinearArray gammaD = gamma.getRow(d);
     	    LinearArray phiNorm = new LinearArray(ids.size());
     	    
     	    for(int i=0; i<ids.size(); i++){
     	    	LinearArray eLogThetaD = eLogTheta.getRow(d);
     	    	LinearArray eLogBetaC = eLogBeta.getCol(wordIds[d][i]);
     	    	LinearArray temp = eLogThetaD.add(eLogBetaC);
     	    	//LinearArray temp = eLogBetaC.add(eLogThetaD);
     	    	tMax = temp.max();
     	    	double sum = Math.log((exp((temp.add(-tMax)).values)).sum()) + tMax;
     	    	phiNorm.set(i, sum);
     	    	
     	    }
     	   score += (cts.product(phiNorm)).sum();
        }
        
        score -= (gamma.add(-alpha).product(eLogTheta)).sum();
        score += (gammaLn(gamma.values).add(-gammaLn(alpha))).sum();
        score -= (gammaLn((gamma.sumByRows()).values).add(-gammaLn(alpha * K))).sum();
        score *= D/(double)docs.size();
        score -= (lambda.add(-eta).product(eLogBeta)).sum();
        score += (gammaLn(lambda.values).add(-gammaLn(eta))).sum();
        score -= (gammaLn((lambda.sumByRows()).values).add(-gammaLn(eta * W))).sum();
      
        return score; 
    }
    
    //TODO: Delet this code
    public static void printMatrix(Matrix x){
    	double[][] m = x.values;
    	
    	for (int i=0;i<m.length;i++){
    		System.out.println(Arrays.toString(m[i]));
    	}
    	
    };
}
