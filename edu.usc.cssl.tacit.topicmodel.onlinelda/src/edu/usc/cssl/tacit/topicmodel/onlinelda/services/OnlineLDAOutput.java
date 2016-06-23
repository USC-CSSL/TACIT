package edu.usc.cssl.tacit.topicmodel.onlinelda.services;

import edu.usc.cssl.tacit.topicmodel.onlinelda.services.Matrix;
import edu.usc.cssl.tacit.topicmodel.onlinelda.services.LinearArray;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.topicmodel.onlinelda.services.Documents;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
/*
 * An encapsulating class to hold the results of updateLambda method in the OnlineLda class
 */
public class OnlineLDAOutput {

	private final Matrix lambda;
	private final Matrix gamma;
    private final double perplexity;
    private final Documents documents; 
    private final int totalWordCount;
    private final int D;
    
    final static NumberFormat  NF = new DecimalFormat ("##.####");
    
	public OnlineLDAOutput(Matrix lambda, Matrix gamma, double bound, Documents docs, int D) {
        this.D = D;
		this.lambda = lambda; 
        this.gamma = gamma;
        this.documents = docs;
        this.totalWordCount = docs.getWordCount();
        double perWordBound = (bound * docs.size())  / (D * totalWordCount);
        this.perplexity = Math.exp(-perWordBound);
        System.out.println("Perplexity: "+perplexity);
        ConsoleView.printlInConsoleln("Perplexity: "+perplexity);
	}
	
	public void printTopics(PrintWriter p1, PrintWriter p2, ArrayList<Documents> dList, Vocabulary v, int wordsPerTopic, IProgressMonitor monitor){
		
		int [][] topicWords = new int[lambda.numOfRows()][wordsPerTopic];
		
		for(int k=0; k<lambda.numOfRows(); k++){
			monitor.subTask("Writing Topic " + k);
			p2.println("Topic "+k);
			ConsoleView.printlInConsoleln("Topic "+k);
			int num = 0;
			LinearArray lambdak = lambda.getRow(k);
			lambdak = lambdak.div(lambdak.sum());
			for(int i=0;i<lambdak.size();i++){
				p1.print(lambdak.values[i]+" ");
			}
			p1.print("\n");

			HashMap<Integer, Double> topicWordsWts = new HashMap<Integer, Double>();
			for(int i=0; i<lambdak.size(); i++){
				double value = lambdak.values[i];
				topicWordsWts.put(i, value);
			}
			
			Map<Integer, Double> results = reverseSortByValue(topicWordsWts);
            
			Iterator i = results.entrySet().iterator();

			while (i.hasNext() && num<wordsPerTopic) {
		        Map.Entry me = (Map.Entry)i.next();
		        int y = (int)me.getKey();
		        
		        p2.print(v.getToken(y)+"="+me.getValue());
		        p2.print("\n");
		        ConsoleView.printlInConsoleln(v.getToken(y)+"="+me.getValue());
		        num++;
		    }
			ConsoleView.printlInConsoleln();
			
			monitor.worked(50);
			if (monitor.isCanceled()){
				throw new OperationCanceledException();
			}
		}
		p1.close();
		p2.close();
	}
	
	
	public static <K, V extends Comparable<? super V>> Map<K, V> reverseSortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
				return -(e1.getValue()).compareTo(e2.getValue());
			}
		});
	 
		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
	 
		return result;
	}
}
