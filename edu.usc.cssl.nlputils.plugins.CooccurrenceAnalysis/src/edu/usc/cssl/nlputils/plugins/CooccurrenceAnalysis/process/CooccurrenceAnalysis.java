package edu.usc.cssl.nlputils.plugins.CooccurrenceAnalysis.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

public class CooccurrenceAnalysis {
	
	private static String delimiters = "";//" .,;\"!-()\\[\\]{}:?'/\\`~$%#@&*_=+<>";
	private boolean doPhrases;
	private Map<String, Integer> seedWords;
	private String outputPath;
	private Map<String, Map<String, Integer>> wordMat;
	
	public static void main(String[] args) {
     	CooccurrenceAnalysis analysisObj = new CooccurrenceAnalysis();
		analysisObj.calculateCooccurrences(
				"C:\\Users\\LindaPulickal\\Desktop\\coocuurence\\",
				"C:\\Users\\LindaPulickal\\Desktop\\coocuurence\\seeds\\seeds.txt", 3,
				"C:\\Users\\LindaPulickal\\Desktop\\coocuurence\\out\\", 1, true);
	}
	
	/**
	 * This function populates the seedWords map with seed words mentioned in the input file. 
	 * @param seedFile - absolute filepath of seedFile. 
	 * @return boolean value indicating success or failure
	 * @throws IOException
	 */
	private boolean setSeedWords(String seedFile) throws IOException {
		String[] seeds = null;
		String currentLine = null;
		
		seedFile = validateFile(seedFile);
		if(seedFile.equals("<false>")) 
			return false;
		
		BufferedReader br = new BufferedReader(new FileReader(new File(seedFile)));
		while ((currentLine = br.readLine()) != null) {
			seeds = currentLine.split(" ");
			for (String seed : seeds) {
				if (!seedWords.containsKey(seed)) {
					seedWords.put(seed, 1);
				}
			}
		}
		br.close();
		return (seedWords.size() > 0)? true: false; 
	}
	
	public CooccurrenceAnalysis() {
		seedWords = new HashMap<String, Integer>(); 
		wordMat   = new HashMap<String, Map<String, Integer>>();		
		doPhrases = false;
	}
	
	private void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
		createIfMissing(outputPath); 
	}
	
	public boolean calculateCooccurrences(String inputDir, String seedFile, 
						int windowSize, String outputPath, int threshold, boolean buildMatrix) {
		
		String currentLine = null;
		Queue<String> q = new LinkedList<String>();
		List<String> phrase = new ArrayList<String>();
		
		setOutputPath(outputPath);
		
		//build the seed word dictionary
		try {
			
			boolean ret = false;
			if (windowSize > 0) { //TODO : prevent from GUI
				ret = setSeedWords(seedFile);
			}
			if(ret) {
				doPhrases = true;
			}
			
			File dir = new File(inputDir);
			File[] listOfFiles = dir.listFiles();
			int seedWordCount = seedWords.size();
			int count;
			for (File f : listOfFiles) {
				count = 0;	
				if (f.getAbsolutePath().contains("DS_Store"))
					continue;
				System.out.println("Processing");
				
				List<String> words = new ArrayList<String>();
				if(!f.exists() || f.isDirectory())
					continue;
				BufferedReader br = new BufferedReader(new FileReader(f));
				
				int line_no = 0;
				while ((currentLine = br.readLine()) != null) {
					if(currentLine.isEmpty() || currentLine.equals(""))
						continue;
					line_no++;
					for (String word : currentLine.split(" ")) {
						if(word.isEmpty() || word.equals(""))
							continue;

						word.replaceAll(delimiters, "");
						if(buildMatrix)
							words.add(word);

						if(doPhrases) {
							
							if(count >= threshold || count>= seedWordCount ) {
								StringBuilder match = new StringBuilder();
								for(String str: q)
									match.append(str+' '); 
								phrase.add(f.getName() + "  " + line_no + " " + match.toString() );
								q.clear();
								count = 0;
								for(String s: seedWords.keySet()){
									seedWords.put(s, 1);
								}
							}else if(q.size() >= windowSize){
								String first = q.remove();
								if(seedWords.containsKey(first)){
									if(seedWords.get(first) == 0){
										count--;
										seedWords.put(first, 1);
									}
								}
							}
							q.add(word);
							if(seedWords.containsKey(word)){
								if(seedWords.get(word) != 0){
									count++;
									seedWords.put(word, 0);
								}
							}

						}
						
						if(buildMatrix) {
							
							Map<String, Integer> vec = null;
							//	System.out.println("Building word mat for " + word);
							if (wordMat.containsKey(word)) {
								vec = wordMat.get(word);
							} else {
								vec = new HashMap<String, Integer>();
								wordMat.put(word, vec);
							}
							for (String second : words) {
								if (vec.containsKey(second)) {
									vec.put(second, vec.get(second) + 1);
								} else {
									vec.put(second, 1);
								}
								Map<String, Integer> temp =  wordMat.get(second);
								if(temp.containsKey(word)){
									temp.put(word, temp.get(word) + 1);
								}else{
									temp.put(word, 1);
								}

							}
						}

					}
				}
				br.close();
			}
			
			if(buildMatrix) {
				writeWordMatrix();
			}
			if(ret && phrase.size()>0){
				writePhrases(phrase);
			}

			System.out.println(phrase.size());
			return true;
		} catch (Exception e) {
			System.out.println("Exception occurred in Cooccurrence Analysis "+ e);
		}
		
		return false;
	}
	
	/**
	 * write the phrases into file phrases.txt
	 * @param phrases - phrases to be written
	 */
	private void writePhrases(List<String> phrases) {
		try {
			FileWriter fw = new FileWriter(new File(outputPath	+ File.separator + "phrases.txt"));
			for(String p:phrases) {
				fw.write(p+"\n");
			}
			fw.close();
		} catch (IOException e) {
			System.out.println("Error writing output to file phrases.txt " + e);
		}
	}
	
	/**
	 * The function checks whether a file exists. If not return <false>.
	 * If input filepath is directory, return the first file in the directory.  
	 * @param filePath - absolute path of file to be validated.
	 * @return
	 */
	private String validateFile(String filePath) {
		
		File file = new File(filePath);
		String finalFilePath = filePath;
		if (file == null || !file.exists()) 
			return "<false>";
		if(file.isDirectory()){ //TODO : prevent from GUI
			finalFilePath = file.listFiles()[0].getAbsolutePath();
		}
		else
			return finalFilePath;
		
		return validateFile(finalFilePath); //loop the function to get only file and not directory. 
	}
	
	/**
	 * Creates a directory in the file system if it does not already exists
	 * @param folder : full path of the directory which has to be created. 
	 */
	private void createIfMissing(String folder) {
		File path = new File(folder);
		if (!path.exists()){
			path.mkdirs();
		}
	}
	
	/**
	 * write the word matrix into the file word-to-word-matrix.csv 
	 */
	private void writeWordMatrix() {
		
		SortedSet<String> keys = new TreeSet<String>(wordMat.keySet());
		Map<String, Integer> vec = null;
		
		System.out.println(keys.size());
		try {
			FileWriter fw = new FileWriter(new File(outputPath	+ File.separator + "word-to-word-matrix.csv"));
			fw.write(" ,");
			for (String key : keys) {
				fw.write(key + ",");
			}
			fw.write("\n");
	
			for (String key : keys) {
				fw.write(key + ",");
				vec = wordMat.get(key);
				for (String value : keys) {
					if (vec.containsKey(value)) {
						fw.write(vec.get(value) + ",");
					} else {
						fw.write("0,");
					}
				}
				fw.write("\n");
			}
			fw.close();
		} catch (IOException e) {
			System.out.println("Error writing output to files" + e);
		}
	}
}