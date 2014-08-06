package edu.usc.pil.nlputils.plugins.lda.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.CharSubsequence;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SaveDataInSource;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

public class LDA {
	public void doLDA(String sourceDir, boolean removeStopwords, boolean doLowercase, String numTopics, String outputDir) throws FileNotFoundException, IOException{
		Calendar cal = Calendar.getInstance();
		String dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR);
		String outputPath = outputDir+System.getProperty("file.separator")+sourceDir.substring(sourceDir.lastIndexOf(System.getProperty("file.separator"))+1)+"-"+dateString+"-"+System.currentTimeMillis();
		
		String keepSeq = "TRUE", stopWords = "FALSE", preserveCase = "TRUE";
		
		if (removeStopwords){
			stopWords = "TRUE";
		}
		if (doLowercase){
			preserveCase = "FALSE";
		}
		String[] t2vArgs = {"--input",sourceDir,"--output",outputPath+".mallet","--keep-sequence",keepSeq,"--remove-stopwords",stopWords,"--preserve-case",preserveCase};
		String[] v2tArgs = {"--input",outputPath+".mallet","--num-topics",numTopics,"--output-state",outputPath+".topic-state.gz",
				"--output-topic-keys",outputPath+".topic_keys.txt","--output-doc-topics",outputPath+".topic_composition.txt"};
		
		//--input pathway\to\the\directory\with\the\files --output tutorial.mallet --keep-sequence --remove-stopwords
		Text2Vectors.main(t2vArgs);
		//--input tutorial.mallet --num-topics 20 --output-state topic-state.gz --output-topic-keys tutorial_keys.txt --output-doc-topics tutorial_compostion.txt
		Vectors2Topics.main(v2tArgs);
		
		System.out.println("Created complete state file "+outputPath+".topic-state.gz");
		System.out.println("Created topic keys file "+outputPath+".topic_keys.txt");
		System.out.println("Created topic composition file "+outputPath+".topic_composition.txt");
		
		appendLog("Created complete state file "+outputPath+".topic-state.gz");
		appendLog("Created topic keys file "+outputPath+".topic_keys.txt");
		appendLog("Created topic composition file "+outputPath+".topic_composition.txt");
	}
	
	
	public static void main(String[] args) throws Exception {
/* Ignore this function. Not implemented. */
		File classDirs = new File("c:\\mallet\\dirs");
		File[] directories = classDirs.listFiles();
		System.out.println(directories[0]);
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        pipeList.add(new Target2Label());
        pipeList.add( new SaveDataInSource() );
        pipeList.add( new Input2CharSequence("UTF-8"));
        pipeList.add( new CharSubsequence(CharSubsequence.SKIP_HEADER) );
        //pipeList.add( new CharSequenceRemoveHTML() );
        
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("c:\\mallet\\stoplists\\en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

        /*
        Reader fileReader = new InputStreamReader(new FileInputStream(new File("c:\\mallet\\smallTest.txt")), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields
        */
        instances.addThruPipe (new FileIterator (directories, FileIterator.STARTING_DIRECTORIES, true));

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 100;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(50);
        model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;
        
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        System.out.println(out);
        
        // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            System.out.println(out);
        }
        
        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }

        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        System.out.println("0\t" + testProbabilities[0]);
    }
	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		IEclipseContext parent = context.getParent();
		//System.out.println(parent.get("consoleMessage"));
		String currentMessage = (String) parent.get("consoleMessage"); 
		if (currentMessage==null)
			parent.set("consoleMessage", message);
		else {
			if (currentMessage.equals(message)) {
				// Set the param to null before writing the message if it is the same as the previous message. 
				// Else, the change handler will not be called.
				parent.set("consoleMessage", null);
				parent.set("consoleMessage", message);
			}
			else
				parent.set("consoleMessage", message);
		}
	}
}
