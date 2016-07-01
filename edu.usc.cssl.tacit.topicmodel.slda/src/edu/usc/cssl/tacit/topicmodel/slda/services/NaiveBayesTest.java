package edu.usc.cssl.tacit.topicmodel.slda.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class NaiveBayesTest {
	
	public Classifier trainClassifier(InstanceList trainingInstances) {
		ClassifierTrainer trainer = new NaiveBayesTrainer();
		return trainer.train(trainingInstances);
	}

	public InstanceList formatData(String dir){
		 ImportExample importer = new ImportExample();
	     InstanceList instances = importer.readDirectory(new File(dir));
	     return instances;
	}
	
	public void printLabelings(Classifier classifier, File[] directories, String output) throws IOException {

		// Create a new iterator that will read raw instance data from
		// the lines of a file.
		// Lines should be formatted as:
		//
		// [name] [label] [data ... ]
		//
		// in this case, "label" is ignored.
		//		CsvIterator reader = new CsvIterator(new FileReader(file), "(\\w+)\\s+(\\w+)\\s+(.*)", 3, 2, 1);  // (data, label, name) field indices
		File out = new File(output+File.separator+"predictions");
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		FileIterator iterator = new FileIterator(directories,new TxtFilter(),FileIterator.LAST_DIRECTORY);
		// Create an iterator that will pass each instance through
		// the same pipe that was used to create the training data
		// for the classifier.
		Iterator instances = classifier.getInstancePipe().newIteratorFrom(iterator);
		// Classifier.classify() returns a Classification object
		// that includes the instance, the classifier, and the
		// classification results (the labeling). Here we only
		// care about the Labeling.
		String files[] = directories[0].list();
		int count= 0;
		while (instances.hasNext()) {
			Labeling labeling = classifier.classify(instances.next()).getLabeling();

			// print the labels with their weights in descending order (ie best
			// first)
			bw.write(files[count]);
			bw.newLine();
			for (int rank = 0; rank < labeling.numLocations(); rank++) {
				bw.write(labeling.getLabelAtRank(rank) + ":" +
                        labeling.getValueAtRank(rank) + " ");
			}
			bw.newLine();
			count++;

		}
		bw.flush();
		bw.close();
	}
	
	public void printLabelingsCSV(Classifier classifier, File file) throws IOException {

        // Create a new iterator that will read raw instance data from                                     
        //  the lines of a file.                                                                           
        // Lines should be formatted as:                                                                   
        //                                                                                                 
        //   [name] [label] [data ... ]                                                                    
        //                                                                                                 
        //  in this case, "label" is ignored.                                                              

        CsvIterator reader =
            new CsvIterator(new FileReader(file),
                            "(\\w+)\\s+(\\w+)\\s+(.*)",
                            3,2,1);  // (data, label, name) field indices               

        // Create an iterator that will pass each instance through                                         
        //  the same pipe that was used to create the training data                                        
        //  for the classifier.                                                                            
        Iterator instances =
            classifier.getInstancePipe().newIteratorFrom(reader);

        // Classifier.classify() returns a Classification object                                           
        //  that includes the instance, the classifier, and the                                            
        //  classification results (the labeling). Here we only                                            
        //  care about the Labeling.                                                                       
        while (instances.hasNext()) {
            Labeling labeling = classifier.classify(instances.next()).getLabeling();

            // print the labels with their weights in descending order (ie best first)                     

            for (int rank = 0; rank < labeling.numLocations(); rank++){
                ConsoleView.printlInConsole(labeling.getLabelAtRank(rank) + ":" +
                                 labeling.getValueAtRank(rank) + " ");
            }
            ConsoleView.printlInConsoleln();

        }
    }
	
    /** This class illustrates how to build a simple file filter */
    class TxtFilter implements FileFilter {

        /** Test whether the string representation of the file 
         *   ends with the correct extension. Note that {@ref FileIterator}
         *   will only call this filter if the file is not a directory,
         *   so we do not need to test that it is a file.
         */
        public boolean accept(File file) {
        	if(file.toString().contains(".DS_Store"))
        		return false;
//            return file.toString().endsWith(".txt");
        	return true;
        }
    }
    
	public static void main(String args[]){
		NaiveBayesTest nb = new NaiveBayesTest();
		System.out.println("333444444");
		Classifier trainer = nb.trainClassifier(nb.formatData(args[0]));
		File dir = new File(args[0]);
//		try {
//			nb.printLabelings(trainer, new File[] {dir});
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
	}
}
