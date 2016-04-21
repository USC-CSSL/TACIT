package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.ICommonUiConstants;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;

public class ManageCorpora {
	private static boolean stop;
	private static String printDot = ".";
	private static String rootDir = CommonUiActivator.getDefault().getPreferenceStore()
			.getString(ICommonUiConstants.CORPUS_LOCATION) + System.getProperty("file.separator");
	private static String oldLoc = rootDir;
	private static Long fCount;

	public static void saveCorpora(ArrayList<Corpus> corporaList) {
		for (Corpus corpus : corporaList) {
			saveCorpus(corpus);
		}
	}

	@SuppressWarnings("unchecked")
	public static void saveCorpus(final Corpus corpus) {
		String corpusName = corpus.getCorpusName();
		long fileCorpusCount = 0;
		String corpusLocation = rootDir + corpusName;
		if (!(new File(corpusLocation).exists())) {
			// Add code for everything
			new File(corpusLocation).mkdir();
			String metaFile = corpusLocation + System.getProperty("file.separator") + "meta.txt";

			File metaFp = new File(metaFile);

			JSONObject jsonObj = new JSONObject();
			jsonObj.put("corpus_name", corpus.getCorpusName());
			jsonObj.put("corpus_id", corpus.getCorpusId());
			jsonObj.put("data_type", corpus.getDatatype().toString());
			jsonObj.put("num_classes", corpus.getClasses().size());

			int numClasses = corpus.getClasses().size();
			ArrayList<ICorpusClass> corporaClasses = (ArrayList<ICorpusClass>) corpus.getClasses();

			JSONArray classArray = new JSONArray();

			for (int i = 0; i < numClasses; i++) {
				CorpusClass currClass = (CorpusClass) corporaClasses.get(i);

				
				File dir = new File(currClass.getClassPath());
				fCount = 0L;
				fCount = getFileCount(dir);
				
				fileCorpusCount += fCount;
				currClass.setNoOfFiles(fCount);					
				JSONObject classObj = new JSONObject();
				classObj.put("class_name", currClass.getClassName());
				classObj.put("original_loc", currClass.getClassPath());
				classObj.put("tacit_loc",
						corpusLocation + System.getProperty("file.separator") + currClass.getClassName());
				classObj.put("data_key", currClass.getKeyTextFields());
				classObj.put("no_of_files", fCount);
				classArray.add(classObj);
			}
			corpus.setNoOfFiles(fileCorpusCount);
			System.out.println("Total files"+ fileCorpusCount);
			jsonObj.put("class_details", classArray);
			jsonObj.put("total_no_of_files", fileCorpusCount);
			jsonObj.put("num_analysis", 0);
			JSONArray analysisArray = new JSONArray();
			jsonObj.put("prev_analysis", analysisArray);

			try {
				FileOutputStream bw = new FileOutputStream(metaFp);
				bw.write(jsonObj.toString().getBytes());
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			copyCorpus(jsonObj);

		} else {
			// corpus already exits, check what has changed

			String metaFile = corpusLocation + System.getProperty("file.separator") + "meta.txt";
			JSONArray analysisArray = new JSONArray();
			int numAnalysis = 0;
			if (!new File(metaFile).exists()) {
				JSONParser parser = new JSONParser();
				try {
					analysisArray = (JSONArray) ((JSONObject) parser.parse(new FileReader(metaFile)))
							.get("prev_analysis");
					numAnalysis = (Integer) ((JSONObject) parser.parse(new FileReader(metaFile))).get("num_analysis");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			File metaFp = new File(metaFile);

			JSONObject jsonObj = new JSONObject();
			jsonObj.put("corpus_name", corpus.getCorpusName());
			jsonObj.put("corpus_id", corpus.getCorpusId());
			jsonObj.put("data_type", corpus.getDatatype().toString());
			jsonObj.put("num_classes", corpus.getClasses().size());

			int numClasses = corpus.getClasses().size();
			ArrayList<ICorpusClass> corporaClasses = (ArrayList<ICorpusClass>) corpus.getClasses();
			JSONArray classArray = new JSONArray();

			for (int i = 0; i < numClasses; i++) {
				CorpusClass currClass = (CorpusClass) corporaClasses.get(i);
				
				File dir = new File(currClass.getClassPath());
				fCount = 0L;
				fCount = getFileCount(dir);
				
				fileCorpusCount += fCount;
				currClass.setNoOfFiles(fCount);	
				
				JSONObject classObj = new JSONObject();
				classObj.put("class_name", currClass.getClassName());
				classObj.put("original_loc", currClass.getClassPath());
				classObj.put("tacit_loc",
						corpusLocation + System.getProperty("file.separator") + currClass.getClassName());
				classObj.put("data_key", currClass.getKeyTextFields());
				classObj.put("no_of_files", fCount);
				classArray.add(classObj);
			}
			corpus.setNoOfFiles(fileCorpusCount);
			System.out.println(fileCorpusCount);
			jsonObj.put("total_no_of_files", fileCorpusCount);
			jsonObj.put("class_details", classArray);
			jsonObj.put("num_analysis", numAnalysis);
			jsonObj.put("prev_analysis", analysisArray);

			try {
				FileOutputStream bw = new FileOutputStream(metaFp);
				bw.write(jsonObj.toString().getBytes());
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			copyCorpus(jsonObj);
			removeCorpus(corpus, false);
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if(null!=corpus.getViewer())
					corpus.getViewer().refresh();
			};
		});
	}
	
	public static Long getFileCount(File dir){
		File[] files = dir.listFiles();
		if (files != null) {
			for (File obj : files) {
				if (!obj.isDirectory())
					fCount++;
				else{
					fCount += getFileCount(obj);
				}
			}
		}
		return fCount;
		
	}

	public static boolean isCorpusLocChanged(String newPath) {
		if (rootDir.equals(newPath + System.getProperty("file.separator")))
			return false;
		return true;
	}

	public static void moveCorpora() {
		oldLoc = rootDir;
		rootDir = CommonUiActivator.getDefault().getPreferenceStore().getString(ICommonUiConstants.CORPUS_LOCATION)
				+ System.getProperty("file.separator");

		if (rootDir.equals(oldLoc))
			return;

		try {
			FileUtils.copyDirectory(new File(oldLoc), new File(rootDir));
			FileUtils.deleteDirectory(new File(oldLoc));
		} catch (IOException e) {
			e.printStackTrace();
		}

		File[] corpora = new File(rootDir).listFiles();

		for (File corpus : corpora) {
			if (corpus.isDirectory()) {
				String metaDataFilePath = corpus.getAbsolutePath() + File.separator + "meta.txt";

				FileReader metaDataFile;
				try {
					metaDataFile = new FileReader(metaDataFilePath);
				} catch (FileNotFoundException e) {
					continue;
				}

				if (null == metaDataFile)
					continue; // if there is no metadata file inside the folder

				JSONParser jsonParser = new JSONParser();
				JSONObject oldJsonObject;
				JSONObject newJsonObject = new JSONObject();

				try {
					oldJsonObject = (JSONObject) jsonParser.parse(metaDataFile);
				} catch (Exception e) {
					continue;
				}
				if (null == oldJsonObject)
					continue;

				newJsonObject.put("corpus_name", oldJsonObject.get("corpus_name"));
				newJsonObject.put("corpus_id", oldJsonObject.get("corpus_id"));
				newJsonObject.put("data_type", oldJsonObject.get("data_type"));
				newJsonObject.put("num_classes", oldJsonObject.get("num_classes"));
				newJsonObject.put("num_analysis", oldJsonObject.get("num_analysis"));
				long numClasses = (Long) oldJsonObject.get("num_classes");
				if (numClasses > 0) {

					JSONArray oldClasses = (JSONArray) oldJsonObject.get("class_details");
					JSONArray newClasses = new JSONArray();

					Iterator<JSONObject> classItr = oldClasses.iterator();
					while (classItr.hasNext()) {
						JSONObject oldCC = classItr.next();
						JSONObject newCC = new JSONObject();
						if (null == oldCC)
							continue;

						String oldTacitLoc = (String) oldCC.get("tacit_loc");
						String newTacitLoc = corpus.getAbsolutePath() + File.separator
								+ oldTacitLoc.substring(oldTacitLoc.lastIndexOf(File.separator), oldTacitLoc.length());

						newCC.put("class_name", oldCC.get("class_name"));
						newCC.put("original_loc", oldCC.get("original_loc"));
						newCC.put("tacit_loc", newTacitLoc);
						if (oldCC.containsKey("data_key")) {
							newCC.put("data_key", oldCC.get("data_key"));
						}
						newClasses.add(newCC);
					}
					newJsonObject.put("class_details", newClasses);
					newJsonObject.put("prev_analysis", oldJsonObject.get("prev_analysis"));
				}
				try {
					metaDataFile.close();
					FileWriter metaFP = new FileWriter(metaDataFilePath);
					metaFP.write(newJsonObject.toString());
					metaFP.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// public static void main(String[] args) {
	// try {
	// FileUtils.copyDirectory(new
	// File("/Users/anurag/Desktop/toolkit/textFiles"), new
	// File("/Users/anurag/Desktop/toolkit/newLoc"));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	@SuppressWarnings("unchecked")
	public static void removeCorpus(final Corpus corpus, Boolean isCorpus) {
		String corpusLocation = rootDir + corpus.getCorpusName();
		if (isCorpus) {
			try {
				new File(corpusLocation + System.getProperty("file.separator") + "meta.txt").delete();
				FileUtils.deleteDirectory(new File(corpusLocation));
			} catch (IOException e) {
				e.printStackTrace();
			}

			return;
		}

		File[] classes = new File(corpusLocation).listFiles();
		HashMap<String, Boolean> oldClasses = new HashMap<String, Boolean>();
		if (classes == null) { // corpus is not created yet
			// so dont do anything, just return
			return;
		}
		for (File f : classes) {
			if (f.isDirectory()) {
				oldClasses.put(f.getAbsolutePath(), false);
			}
		}

		ArrayList<String> newClasses = new ArrayList<String>();
		int numNewClass = corpus.getClasses().size();

		for (int i = 0; i < numNewClass; i++) {
			String classPath = corpus.getClasses().get(i).getTacitLocation();
			newClasses.add(classPath);

			if (oldClasses.get(classPath) != null) {
				oldClasses.put(classPath, true);
			}
		}

		// Delete the data from file system of the removed classes
		Iterator<Entry<String, Boolean>> it = oldClasses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Boolean> curr = it.next();
			String classPath = curr.getKey();

			if (!curr.getValue()) {
				try {
					FileUtils.deleteDirectory(new File(classPath));
					it.remove();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Save the new meta file
		String metaFile = corpusLocation + System.getProperty("file.separator") + "meta.txt";
		JSONArray analysisArray = new JSONArray();
		int numAnalysis = 0;
		if (!new File(metaFile).exists()) {
			JSONParser parser = new JSONParser();
			try {
				analysisArray = (JSONArray) ((JSONObject) parser.parse(new FileReader(metaFile))).get("prev_analysis");
				numAnalysis = (Integer) ((JSONObject) parser.parse(new FileReader(metaFile))).get("num_analysis");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		File metaFp = new File(metaFile);

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("corpus_name", corpus.getCorpusName());
		jsonObj.put("corpus_id", corpus.getCorpusId());
		jsonObj.put("data_type", corpus.getDatatype().toString());
		jsonObj.put("num_classes", corpus.getClasses().size());

		int numClasses = corpus.getClasses().size();
		ArrayList<ICorpusClass> corporaClasses = (ArrayList<ICorpusClass>) corpus.getClasses();
		JSONArray classArray = new JSONArray();
		Long totalNum = 0L;
		for (int i = 0; i < numClasses; i++) {
			CorpusClass currClass = (CorpusClass) corporaClasses.get(i);
			JSONObject classObj = new JSONObject();
			classObj.put("class_name", currClass.getClassName());
			classObj.put("original_loc", currClass.getClassPath());
			classObj.put("no_of_files", currClass.getNoOfFiles());
			totalNum+=currClass.getNoOfFiles();
			classObj.put("tacit_loc", corpusLocation + System.getProperty("file.separator") + currClass.getClassName());
			classObj.put("data_key", currClass.getKeyTextFields());
			classArray.add(classObj);
		}
		jsonObj.put("total_no_of_files", totalNum);
		jsonObj.put("class_details", classArray);
		jsonObj.put("num_analysis", numAnalysis);
		jsonObj.put("prev_analysis", analysisArray);

		try {
			FileOutputStream bw = new FileOutputStream(metaFp);
			bw.write(jsonObj.toString().getBytes());
			bw.flush();
			bw.close();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					corpus.getViewer().refresh();
				};
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void copyCorpus(JSONObject jsonObj) {
		int numClasses = ((Integer) jsonObj.get("num_classes"));
		JSONArray classArray = (JSONArray) jsonObj.get("class_details");

		for (int i = 0; i < numClasses; i++) {
			String originalLoc = (String) ((JSONObject) classArray.get(i)).get("original_loc");
			String tacitLoc = (String) ((JSONObject) classArray.get(i)).get("tacit_loc");

			if (!(new File(tacitLoc).exists())) {
				new File(tacitLoc).mkdir();

				try {
					final Job pr = new Job("run") {

						@Override
						protected IStatus run(IProgressMonitor arg0) {
							int i = 0;
							arg0.beginTask("Crawling Twitter ...", 100);
							while (stop) {
								if (i == 0) {
									printDot = "";
									i++;
								} else {
									printDot = ".";
									i++;
									if (i == 10000) {
										i = 0;
									}
								}
								arg0.setTaskName(printDot);

							}

							return Status.OK_STATUS;
						}
					};
					pr.schedule();
					FileUtils.copyDirectory(new File(originalLoc), new File(tacitLoc));
					stop = false;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public List<ICorpus> getAllCorpusDetails() {
		File[] classses = new File(rootDir).listFiles();
		List<ICorpus> corpuses = new ArrayList<ICorpus>();
		if (null == classses)
			return corpuses;

		for (File corpusClass : classses) {
			if (corpusClass.isDirectory()) { // class
				Corpus corpora = new Corpus();
				String metaDataFilePath = corpusClass.getAbsolutePath() + File.separator + "meta.txt";
				FileReader metaDataFile;
				try {
					metaDataFile = new FileReader(metaDataFilePath); // get the
																		// meta
																		// data
																		// file
				} catch (FileNotFoundException e) {
					continue;
				}
				if (null == metaDataFile)
					continue; // if there is no metadata file inside the folder
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject;
				try {
					jsonObject = (JSONObject) jsonParser.parse(metaDataFile);
				} catch (Exception e) { // if there is a parsing issue, just
										// ignore this corpus and look for next
					continue;
				}
				if (null == jsonObject)
					continue;
				corpora.setNoOfFiles((Long)jsonObject.get("total_no_of_files"));
				corpora.setCorpusName((String) jsonObject.get("corpus_name"));
				corpora.setCorpusId((String) jsonObject.get("corpus_id"));
				corpora.setDataType(CMDataType.get((String) jsonObject.get("data_type")));
				long numClasses = (Long) jsonObject.get("num_classes");
				if (numClasses > 0)
					parseClassDetails(corpora, (JSONArray) jsonObject.get("class_details"));
				corpuses.add(corpora);
				try {
					metaDataFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return corpuses;
	}

	private void parseClassDetails(Corpus corpus, JSONArray classes) {
		if (null == corpus || null == classes)
			return;
		Iterator<JSONObject> classItr = classes.iterator();
		while (classItr.hasNext()) {
			JSONObject corpusClassObj = classItr.next();
			if (null == corpusClassObj)
				continue;
			CorpusClass cc = new CorpusClass();
			cc.setClassName((String) corpusClassObj.get("class_name"));
			cc.setClassPath((String) corpusClassObj.get("original_loc"));
			cc.setNoOfFiles((Long)corpusClassObj.get("no_of_files"));
			cc.setTacitLocation((String) corpusClassObj.get("tacit_loc"));
			if (corpusClassObj.containsKey("data_key")) {
				cc.setKeyTextFields((String) corpusClassObj.get("data_key"));
			}
			cc.setParent(corpus);
			corpus.getClasses().add(cc);
		}
	}

	public String[] getNames() {
		List<ICorpus> readCorpusList = readCorpusList();
		List<String> names = new ArrayList<String>();
		for (ICorpus iCorpus : readCorpusList) {
			names.add(iCorpus.getCorpusName());
		}
		return names.toArray(new String[names.size()]);
	}

	public List<ICorpus> readCorpusList() {
		try {
			return getAllCorpusDetails();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public ICorpus readCorpusById(String id) { // why? - to get updated corpus
												// instead of stale data at tool
		List<ICorpus> readCorpusList = readCorpusList();
		for (ICorpus iCorpus : readCorpusList) {
			if (iCorpus.getCorpusName().equals(id)) {
				return iCorpus;
			}
		}
		return null;
	}

	public CMDataType getCorpusDataType(String location) {

		String metaDataFilePath = new File(location).getParent() + File.separator + "meta.txt";
		FileReader metaDataFile;
		try {
			metaDataFile = new FileReader(metaDataFilePath); // get the meta
																// data file
		} catch (FileNotFoundException e) {
			return null;
		}
		if (null == metaDataFile)
			return null; // if there is no metadata file inside the folder
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) jsonParser.parse(metaDataFile);
			String dataType = (String) jsonObject.get("data_type");
			metaDataFile.close();
			return CMDataType.get(dataType);
		} catch (Exception e) { // if there is a parsing issue, just ignore this
								// corpus and look for next
			return null;
		}

	}

}
