package edu.usc.cssl.tacit.topicmodel.hlda.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.util.CommandOption;
import cc.mallet.util.Randoms;

public class HLDA {

	private static int wordsPerTopic;
	private static int maxTopicHeirarchy;
	private InstanceList instances;	
	private String outputDirectory;
    private Pipe pipe;
    private IProgressMonitor monitor;
    
	public HLDA(String outputDirectory, int wordsPerTopic, int maxTopicHeirarchy, IProgressMonitor monitor){
		this.outputDirectory = outputDirectory;
		HLDA.wordsPerTopic = wordsPerTopic;
		HLDA.maxTopicHeirarchy = maxTopicHeirarchy;
		this.monitor = monitor;
	}
	static CommandOption.Integer randomSeed = new CommandOption.Integer
		(HLDA.class, "random-seed", "INTEGER", true, 0,
		 "The random seed for the Gibbs sampler.  Default is 0, which will use the clock.", null);
	
	static CommandOption.Double alpha = new CommandOption.Double
		(HLDA.class, "alpha", "DECIMAL", true, 10.0,
		 "Alpha parameter: smoothing over level distributions.", null);

	static CommandOption.Double gamma = new CommandOption.Double
		(HLDA.class, "gamma", "DECIMAL", true, 1.0,
		 "Gamma parameter: CRP smoothing parameter; number of imaginary customers at next, as yet unused table", null);

	static CommandOption.Double eta = new CommandOption.Double
		(HLDA.class, "eta", "DECIMAL", true, 0.1,
		 "Eta parameter: smoothing over topic-word distributions", null);
	
	public void runHLDA() throws java.io.IOException {

		InstanceList testing = null;
		HierarchicalLDA hlda = new HierarchicalLDA(outputDirectory, monitor);
		
		hlda.setAlpha(alpha.value());
		hlda.setGamma(gamma.value());
		hlda.setEta(eta.value());
		
		// Display preferences
		hlda.setTopicDisplay(50, 10);
		hlda.setProgressDisplay(true);
		hlda.setTopicDisplay(50, wordsPerTopic);

		// Initialize random number generator
		Randoms random = null;
		if (randomSeed.value() == 0) {
			random = new Randoms();
		}
		else {
			random = new Randoms(randomSeed.value());
		}
		
		// Initialize and start the sampler
		hlda.initialize(instances, testing, maxTopicHeirarchy, random);
		hlda.estimate(1000);
		
		// Output results
		hlda.printNodesToFile();

	}
	
    /** This class illustrates how to build a simple file filter */
    class TxtFilter implements FileFilter {

        /** Test whether the string representation of the file 
         *   ends with the correct extension. Note that {@ref FileIterator}
         *   will only call this filter if the file is not a directory,
         *   so we do not need to test that it is a file.
         */
        public boolean accept(File file) {
            return file.toString().endsWith(".txt");
        }
    }

    public void buildPipe() {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers, 
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern =
            Pattern.compile("[\\p{L}\\p{N}_]+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field: 
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
        pipeList.add(new Target2Label());

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        //pipeList.add(new FeatureSequence2FeatureVector());

        // Print out the features and the label
        pipeList.add(new PrintInputAndTarget());

        pipe = new SerialPipes(pipeList);
    }

    public InstanceList readCSV(File file){

            // Create a new iterator that will read raw instance data from                                     
            //  the lines of a file.                                                                           
            // Lines should be formatted as:                                                                   
            //                                                                                                 
            //   [name] [label] [data ... ]                                                                    
            //                                                                                                 
            //  in this case, "label" is ignored.                                                              
    	InstanceList  instances = null;
            CsvIterator reader;
			try {
				reader = new CsvIterator(new FileReader(file),"(\\w+)\\s+(\\w+)\\s+(.*)", 3,2,1);
				instances = new InstanceList(pipe);
				instances.addThruPipe(reader);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  // (data, label, name) field indices               

            return instances;
    }
    
    public void readDirectory(File directory) {
        instances = readDirectories(new File[] {directory});
    }

    public InstanceList readDirectories(File[] directories) {
        
        // Construct a file iterator, starting with the 
        //  specified directories, and recursing through subdirectories.
        // The second argument specifies a FileFilter to use to select
        //  files within a directory.
        // The third argument is a Pattern that is applied to the 
        //   filename to produce a class label. In this case, I've 
        //   asked it to use the last directory name in the path.
        FileIterator iterator =
            new FileIterator(directories,
                             new TxtFilter(),
                             FileIterator.LAST_DIRECTORY);

        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipe);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        return instances;
    }
}
