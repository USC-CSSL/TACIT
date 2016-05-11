package edu.usc.cssl.tacit.classify.svm;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.classify.svm.services.CrossValidator;
import edu.usc.cssl.tacit.classify.svm.services.SVMClassify;

public class Svm_Test {
	HashMap<String, Double> expectedHashMap;
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void svmSansFeaturesTest() throws IOException {
		int kValueInt = 2;
		Date dateObj = Calendar.getInstance().getTime();
		String class1NameStr = "Data1";
		String class2NameStr = "Data2";
		boolean featureFile = false;
		final SVMClassify svm = new SVMClassify(class1NameStr,
				class2NameStr, directoryPath);
		File[] files1 = new File[2];
		files1[0] = new File(directoryPath + File.separator +"SVMData1.txt");
		files1[1] = new File(directoryPath + File.separator +"SVMData2.txt");
		File[] files2 = new File[2];
		files2[0] = new File(directoryPath + File.separator +"SVMData3.txt");
		files2[1] = new File(directoryPath + File.separator +"SVMData4.txt");
		final CrossValidator cv = new CrossValidator(){
			protected void createRunReport(String outputPath, Date dateObj){}
			protected String createOutputFileName(String output, Date dateObj){

				return directoryPath + File.separator + "GeneratedSVMClassificationOutput.csv";}
		};
		cv.doCross(svm, class1NameStr, files1,
				class2NameStr, files2, kValueInt,
				featureFile, directoryPath, new NullProgressMonitor(), dateObj);
		File generatedSVMOutput = new File(directoryPath + File.separator
				+ "GeneratedSVMClassificationOutput.csv");
		File expectedSVMOutput = new File(directoryPath + File.separator
				+ "ExpectedSVMClassificationOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedSVMOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedSVMOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();
		
		assertEquals("Comparing svm output", expectedOutput, generatedOutput);
	}
	
	@Test
	public void svmWithFeaturesTest() throws IOException {

		int kValueInt = 2;
		Date dateObj = Calendar.getInstance().getTime();
		String class1NameStr = "Data1";
		String class2NameStr = "Data2";
		boolean featureFile = true;
		final SVMClassify svm = new SVMClassify(class1NameStr,
				class2NameStr, directoryPath){
			protected String createSVMWeightFileName(DateFormat df, String kVal, String intermediatePath, Date dateObj){
				return directoryPath + File.separator + "GeneratedSVMWeightFile"+kVal+".csv";
			}
		};
		File[] files1 = new File[2];
		files1[0] = new File(directoryPath + File.separator +"SVMData1.txt");
		files1[1] = new File(directoryPath + File.separator +"SVMData2.txt");
		File[] files2 = new File[2];
		files2[0] = new File(directoryPath + File.separator +"SVMData3.txt");
		files2[1] = new File(directoryPath + File.separator +"SVMData4.txt");
		final CrossValidator cv = new CrossValidator(){
			protected void createRunReport(String outputPath, Date dateObj){}
			protected String createOutputFileName(String output, Date dateObj){

				return directoryPath + File.separator + "GeneratedSVMClassificationOutput.csv";}
		};
		cv.doCross(svm, class1NameStr, files1,
				class2NameStr, files2, kValueInt,
				featureFile, directoryPath, new NullProgressMonitor(), dateObj);
		File generatedSVMWeightFileOutput = new File(directoryPath + File.separator
				+ "GeneratedSVMWeightFilek1.csv");
		File expectedSVMWeightFileOutput = new File(directoryPath + File.separator
				+ "ExpectedSVMWeightFile1.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedSVMWeightFileOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedSVMWeightFileOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();
		assertEquals("Comparing svm weight file 1 output", expectedOutput, generatedOutput);
		
		generatedSVMWeightFileOutput = new File(directoryPath + File.separator
				+ "GeneratedSVMWeightFilek2.csv");
		expectedSVMWeightFileOutput = new File(directoryPath + File.separator
				+ "ExpectedSVMWeightFile2.csv");
		reader = new BufferedReader(new FileReader(generatedSVMWeightFileOutput));
		generatedOutput = "";
		expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedSVMWeightFileOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();
		assertEquals("Comparing svm weight file 2 output", expectedOutput, generatedOutput);
		
		buildExpectedHashMap();
		HashMap<String, Double> generatedHashMap = svm.fileToBow(files1[0]);

		Set<Entry<String, Double>> generatedSet = generatedHashMap.entrySet();
		boolean flag = true;
		 Iterator<Entry<String, Double>> it = expectedHashMap.entrySet().iterator();
		    while (it.hasNext() ){
		        Object a = it.next();
		        if (generatedSet.contains(a)){
		            generatedSet.remove(a);		       
		        } else {
		            flag = false;
		            break;
		        }
		    }
		assertEquals("Checking set contents", true, flag);
	}
	@Test
	public void svmFileToBowTest() throws IOException {

		String class1NameStr = "Data1";
		String class2NameStr = "Data2";
		final SVMClassify svm = new SVMClassify(class1NameStr,
				class2NameStr, directoryPath){
			protected String createSVMWeightFileName(DateFormat df, String kVal, String intermediatePath, Date dateObj){
				return directoryPath + File.separator + "GeneratedSVMWeightFile"+kVal+".csv";
			}
			protected void testMethod()
			{
				noOfDocuments = 2;
				doTfidf = true;
				buildDFMap(dfMap);
			}
			
		};
		File[] files1 = new File[2];
		files1[0] = new File(directoryPath + File.separator +"SVMData1.txt");
		files1[1] = new File(directoryPath + File.separator +"SVMData2.txt");
		File[] files2 = new File[2];
		files2[0] = new File(directoryPath + File.separator +"SVMData3.txt");
		files2[1] = new File(directoryPath + File.separator +"SVMData4.txt");

		buildExpectedHashMap();
		HashMap<String, Double> generatedHashMap = svm.fileToBow(files1[0]);

		Set<Entry<String, Double>> generatedSet = generatedHashMap.entrySet();
		boolean flag = true;
		 Iterator<Entry<String, Double>> it = expectedHashMap.entrySet().iterator();
		    while (it.hasNext() ){
		        Object a = it.next();
		        if (generatedSet.contains(a)){
		            generatedSet.remove(a);		       
		        } else {
		            flag = false;
		            break;
		        }
		    }
		assertEquals("Checking set contents", true, flag);
	}
	
	@Test
	public void svmBowToStringTest() throws IOException {

		String class1NameStr = "Data1";
		String class2NameStr = "Data2";
		final SVMClassify svm = new SVMClassify(class1NameStr,
				class2NameStr, directoryPath){
			protected void testMethod()
			{
				buildFeatureMap(featureMap);
			}		
		};
		buildExpectedHashMap();
			
		String output = svm.BowToString(expectedHashMap);

		assertEquals("Checking set contents", output, "1:0.3010299956639812 2:0.3010299956639812 3:0.3010299956639812 4:0.3010299956639812 5:0.3010299956639812 6:0.0 7:0.6020599913279624 8:0.0 9:0.0 10:0.3010299956639812 11:0.0 12:0.3010299956639812 13:0.9030899869919435 14:0.9030899869919435 15:0.3010299956639812 16:0.0 17:0.9030899869919435 18:0.6020599913279624 19:0.3010299956639812 20:0.3010299956639812 21:0.3010299956639812 22:0.3010299956639812 23:0.3010299956639812 24:0.3010299956639812 25:0.6020599913279624 26:0.3010299956639812 27:0.3010299956639812 28:0.3010299956639812 29:0.3010299956639812 30:0.0 31:0.3010299956639812 32:0.3010299956639812 33:0.0 34:0.3010299956639812 35:0.3010299956639812 36:0.3010299956639812 37:0.6020599913279624 38:0.3010299956639812 39:0.3010299956639812 40:0.6020599913279624 41:0.9030899869919435 42:0.0 43:0.3010299956639812 44:0.0 45:0.3010299956639812 46:0.3010299956639812 47:0.9030899869919435 48:0.0 49:0.3010299956639812 50:0.3010299956639812 51:0.3010299956639812 52:0.6020599913279624 53:0.6020599913279624 54:0.0 55:0.0 56:0.0 57:0.6020599913279624 58:0.3010299956639812 59:0.3010299956639812 60:0.3010299956639812 61:0.3010299956639812 62:0.3010299956639812 63:0.3010299956639812 64:0.3010299956639812 65:0.6020599913279624 66:0.0 67:0.3010299956639812 68:0.3010299956639812 69:0.3010299956639812 70:0.6020599913279624 71:0.0 72:0.3010299956639812 73:0.6020599913279624 74:0.0 75:0.3010299956639812 76:0.3010299956639812 77:0.3010299956639812 78:0.0 79:0.3010299956639812");
	}
	@Test
	public void svmBowToTestStringTest() throws IOException {

		String class1NameStr = "Data1";
		String class2NameStr = "Data2";
		final SVMClassify svm = new SVMClassify(class1NameStr,
				class2NameStr, directoryPath){
			protected void testMethod()
			{
				buildFeatureMap(featureMap);
			}		
		};
		buildExpectedHashMap();
		String output = svm.BowToTestString(expectedHashMap);
		assertEquals("Checking set contents", output, "1:0.3010299956639812 2:0.3010299956639812 3:0.3010299956639812 4:0.3010299956639812 5:0.3010299956639812 6:0.0 7:0.6020599913279624 8:0.0 9:0.0 10:0.3010299956639812 11:0.0 12:0.3010299956639812 13:0.9030899869919435 14:0.9030899869919435 15:0.3010299956639812 16:0.0 17:0.9030899869919435 18:0.6020599913279624 19:0.3010299956639812 20:0.3010299956639812 21:0.3010299956639812 22:0.3010299956639812 23:0.3010299956639812 24:0.3010299956639812 25:0.6020599913279624 26:0.3010299956639812 27:0.3010299956639812 28:0.3010299956639812 29:0.3010299956639812 30:0.0 31:0.3010299956639812 32:0.3010299956639812 33:0.0 34:0.3010299956639812 35:0.3010299956639812 36:0.3010299956639812 37:0.6020599913279624 38:0.3010299956639812 39:0.3010299956639812 40:0.6020599913279624 41:0.9030899869919435 42:0.0 43:0.3010299956639812 44:0.0 45:0.3010299956639812 46:0.3010299956639812 47:0.9030899869919435 48:0.0 49:0.3010299956639812 50:0.3010299956639812 51:0.3010299956639812 52:0.6020599913279624 53:0.6020599913279624 54:0.0 55:0.0 56:0.0 57:0.6020599913279624 58:0.3010299956639812 59:0.3010299956639812 60:0.3010299956639812 61:0.3010299956639812 62:0.3010299956639812 63:0.3010299956639812 64:0.3010299956639812 65:0.6020599913279624 66:0.0 67:0.3010299956639812 68:0.3010299956639812 69:0.3010299956639812 70:0.6020599913279624 71:0.0 72:0.3010299956639812 73:0.6020599913279624 74:0.0 75:0.3010299956639812 76:0.3010299956639812 77:0.3010299956639812 78:0.0 79:0.3010299956639812");
		}

	@Test
	public void svmComputePredictiveWeightsTest() throws IOException {

		String class1NameStr = "Data1";
		String class2NameStr = "Data2";
		final SVMClassify svm = new SVMClassify(class1NameStr,
				class2NameStr, directoryPath){	
		};
		File modelFile = new File(directoryPath + File.separator +"TestData.model");
		HashMap<Integer, Double> generatedOutput = svm.computePredictiveWeights(modelFile);
		HashMap<Integer, Double> expectedOutput = new HashMap<Integer, Double>();
		expectedOutput.put(1,0.006017985679143767);
		expectedOutput.put(2,0.006017985679143767);
		expectedOutput.put(3,0.006017985679143767);
		expectedOutput.put(4,0.006017985679143767);
		expectedOutput.put(5,0.006017985679143767);
		expectedOutput.put(6,0.0);
		expectedOutput.put(7,0.012035971358287535);
		expectedOutput.put(8,0.0);
		expectedOutput.put(9,0.0);
		expectedOutput.put(10,0.006017985679143767);
		expectedOutput.put(11,0.0);
		expectedOutput.put(12,0.006017985679143767);
		expectedOutput.put(13,0.018053957037431304);
		expectedOutput.put(14,0.018053957037431304);
		expectedOutput.put(15,0.006017985679143767);
		expectedOutput.put(16,0.0);
		expectedOutput.put(17,0.018053957037431304);
		expectedOutput.put(18,0.012035971358287535);
		expectedOutput.put(19,0.006017985679143767);
		expectedOutput.put(20,0.006017985679143767);
		expectedOutput.put(21,0.006017985679143767);
		expectedOutput.put(22,0.006017985679143767);
		expectedOutput.put(23,0.006017985679143767);
		expectedOutput.put(24,0.006017985679143767);
		expectedOutput.put(25,0.012035971358287535);
		expectedOutput.put(26,0.006017985679143767);
		expectedOutput.put(27,0.006017985679143767);
		expectedOutput.put(28,0.006017985679143767);
		expectedOutput.put(29,0.006017985679143767);
		expectedOutput.put(30,0.0);
		expectedOutput.put(31,0.006017985679143767);
		expectedOutput.put(32,0.006017985679143767);
		expectedOutput.put(33,0.0);
		expectedOutput.put(34,0.006017985679143767);
		expectedOutput.put(35,0.006017985679143767);
		expectedOutput.put(36,0.006017985679143767);
		expectedOutput.put(37,0.012035971358287535);
		expectedOutput.put(38,0.006017985679143767);
		expectedOutput.put(39,0.006017985679143767);
		expectedOutput.put(40,0.012035971358287535);
		expectedOutput.put(41,0.018053957037431304);
		expectedOutput.put(42,0.0);
		expectedOutput.put(43,0.006017985679143767);
		expectedOutput.put(44,0.0);
		expectedOutput.put(45,0.006017985679143767);
		expectedOutput.put(46,0.006017985679143767);
		expectedOutput.put(47,0.018053957037431304);
		expectedOutput.put(48,0.0);
		expectedOutput.put(49,0.006017985679143767);
		expectedOutput.put(50,0.006017985679143767);
		expectedOutput.put(51,0.006017985679143767);
		expectedOutput.put(52,0.012035971358287535);
		expectedOutput.put(53,0.012035971358287535);
		expectedOutput.put(54,0.0);
		expectedOutput.put(55,0.0);
		expectedOutput.put(56,0.0);
		expectedOutput.put(57,0.012035971358287535);
		expectedOutput.put(58,0.006017985679143767);
		expectedOutput.put(59,0.006017985679143767);
		expectedOutput.put(60,0.006017985679143767);
		expectedOutput.put(61,0.006017985679143767);
		expectedOutput.put(62,0.006017985679143767);
		expectedOutput.put(63,0.006017985679143767);
		expectedOutput.put(64,0.006017985679143767);
		expectedOutput.put(65,0.012035971358287535);
		expectedOutput.put(66,0.0);
		expectedOutput.put(67,0.006017985679143767);
		expectedOutput.put(68,0.006017985679143767);
		expectedOutput.put(69,0.006017985679143767);
		expectedOutput.put(70,0.012035971358287535);
		expectedOutput.put(71,0.0);
		expectedOutput.put(72,0.006017985679143767);
		expectedOutput.put(73,0.012035971358287535);
		expectedOutput.put(74,0.0);
		expectedOutput.put(75,0.006017985679143767);
		expectedOutput.put(76,0.006017985679143767);
		expectedOutput.put(77,0.006017985679143767);
		expectedOutput.put(78,0.0);
		expectedOutput.put(79,0.006017985679143767);
		expectedOutput.put(80,-0.006017985679143767);
		expectedOutput.put(81,-0.006017985679143767);
		expectedOutput.put(82,-0.006017985679143767);
		expectedOutput.put(83,-0.006017985679143767);
		expectedOutput.put(84,-0.006017985679143767);
		expectedOutput.put(85,-0.006017985679143767);
		expectedOutput.put(86,-0.006017985679143767);
		expectedOutput.put(87,-0.006017985679143767);
		expectedOutput.put(88,-0.006017985679143767);
		expectedOutput.put(89,-0.006017985679143767);
		expectedOutput.put(90,-0.006017985679143767);
		expectedOutput.put(91,-0.006017985679143767);
		expectedOutput.put(92,-0.02407194271657507);
		expectedOutput.put(93,-0.006017985679143767);
		expectedOutput.put(94,-0.018053957037431304);
		expectedOutput.put(95,-0.006017985679143767);
		expectedOutput.put(96,-0.006017985679143767);
		expectedOutput.put(97,-0.012035971358287535);
		expectedOutput.put(98,-0.006017985679143767);
		expectedOutput.put(99,-0.006017985679143767);
		expectedOutput.put(100,-0.006017985679143767);
		expectedOutput.put(101,-0.012035971358287535);
		expectedOutput.put(102,-0.006017985679143767);
		expectedOutput.put(103,-0.006017985679143767);
		expectedOutput.put(104,-0.03610791407486261);
		expectedOutput.put(105,-0.006017985679143767);
		expectedOutput.put(106,-0.006017985679143767);
		expectedOutput.put(107,-0.006017985679143767);
		expectedOutput.put(108,-0.006017985679143767);
		expectedOutput.put(109,-0.006017985679143767);
		expectedOutput.put(110,-0.03610791407486261);
		expectedOutput.put(111,-0.006017985679143767);
		expectedOutput.put(112,-0.030089928395718838);
		expectedOutput.put(113,-0.006017985679143767);
		expectedOutput.put(114,-0.006017985679143767);
		expectedOutput.put(115,-0.04212589975400637);
		expectedOutput.put(116,-0.006017985679143767);
		expectedOutput.put(117,-0.006017985679143767);
		expectedOutput.put(118,-0.03610791407486261);
		expectedOutput.put(119,-0.006017985679143767);
		expectedOutput.put(120,-0.03610791407486261);
		expectedOutput.put(121,-0.006017985679143767);
		expectedOutput.put(122,-0.006017985679143767);
		expectedOutput.put(123,-0.006017985679143767);
		expectedOutput.put(124,-0.006017985679143767);
		expectedOutput.put(125,-0.006017985679143767);
		expectedOutput.put(126,-0.030089928395718838);
		expectedOutput.put(127,-0.006017985679143767);
		expectedOutput.put(128,-0.03610791407486261);
		expectedOutput.put(129,-0.006017985679143767);
		expectedOutput.put(130,-0.006017985679143767);
		expectedOutput.put(131,-0.006017985679143767);
		expectedOutput.put(132,-0.006017985679143767);
		expectedOutput.put(133,-0.006017985679143767);
		expectedOutput.put(134,-0.03610791407486261);
		expectedOutput.put(135,-0.006017985679143767);
		expectedOutput.put(136,-0.04212589975400637);
		expectedOutput.put(137,-0.006017985679143767);
		expectedOutput.put(138,-0.006017985679143767);
		expectedOutput.put(139,-0.03610791407486261);
		expectedOutput.put(140,-0.006017985679143767);
		expectedOutput.put(141,-0.012035971358287535);
		expectedOutput.put(142,-0.10832374222458782);
		expectedOutput.put(143,-0.04212589975400637);
		expectedOutput.put(144,-0.006017985679143767);
		expectedOutput.put(145,-0.006017985679143767);
		expectedOutput.put(146,-0.006017985679143767);
		expectedOutput.put(147,-0.006017985679143767);
		expectedOutput.put(148,-0.04212589975400637);
		expectedOutput.put(149,-0.006017985679143767);
		expectedOutput.put(150,-0.006017985679143767);
		expectedOutput.put(151,-0.006017985679143767);
		expectedOutput.put(152,-0.006017985679143767);
		expectedOutput.put(153,-0.012035971358287535);
		expectedOutput.put(154,-0.006017985679143767);
		expectedOutput.put(155,-0.03610791407486261);
		expectedOutput.put(156,-0.006017985679143767);
		expectedOutput.put(157,-0.006017985679143767);
		expectedOutput.put(158,-0.006017985679143767);
		expectedOutput.put(159,-0.006017985679143767);
		expectedOutput.put(160,-0.006017985679143767);
		expectedOutput.put(161,-0.006017985679143767);
		expectedOutput.put(162,-0.012035971358287535);
		expectedOutput.put(163,-0.012035971358287535);
		expectedOutput.put(164,-0.006017985679143767);
		
		assertEquals("Checking set contents", generatedOutput, expectedOutput);
		}
	void buildFeatureMap(HashMap<String, Integer> featureMap){

		featureMap.put("Res", 1);
		featureMap.put("friendship", 2);
		featureMap.put("05", 3);
		featureMap.put("considered", 4);
		featureMap.put("disability", 5);
		featureMap.put("United", 6);
		featureMap.put("Wives", 7);
		featureMap.put("Page", 8);
		featureMap.put("resolution", 9);
		featureMap.put("honors", 10);
		featureMap.put("States", 11);
		featureMap.put("DESIGNATING", 12);
		featureMap.put("Armed", 13);
		featureMap.put("members", 14);
		featureMap.put("WIVES", 15);
		featureMap.put("which", 16);
		featureMap.put("spouses", 17);
		featureMap.put("Gold", 18);
		featureMap.put("SENATE", 19);
		featureMap.put("made", 20);
		featureMap.put("RESOLUTION", 21);
		featureMap.put("active", 22);
		featureMap.put("is", 23);
		featureMap.put("April", 24);
		featureMap.put("Senate", 25);
		featureMap.put("AS", 26);
		featureMap.put("himself", 27);
		featureMap.put("as", 28);
		featureMap.put("mission", 29);
		featureMap.put("submitted", 30);
		featureMap.put("provide", 31);
		featureMap.put("5", 32);
		featureMap.put("following", 33);
		featureMap.put("SUBMITTED", 34);
		featureMap.put("support", 35);
		featureMap.put("S1675", 36);
		featureMap.put("Inc", 37);
		featureMap.put("who", 38);
		featureMap.put("primary", 39);
		featureMap.put("veterans", 40);
		featureMap.put("Whereas", 41);
		featureMap.put("Mr", 42);
		featureMap.put("RESOLUTIONS", 43);
		featureMap.put("for", 44);
		featureMap.put("died", 45);
		featureMap.put("BURR", 46);
		featureMap.put("Forces", 47);
		featureMap.put("Mrs", 48);
		featureMap.put("represents", 49);
		featureMap.put("result", 50);
		featureMap.put("S", 51);
		featureMap.put("Star", 52);
		featureMap.put("413", 53);
		featureMap.put("and", 54);
		featureMap.put("by", 55);
		featureMap.put("of", 56);
		featureMap.put("America", 57);
		featureMap.put("have", 58);
		featureMap.put("``GOLD", 59);
		featureMap.put("agreed", 60);
		featureMap.put("APRIL", 61);
		featureMap.put("BOXER", 62);
		featureMap.put("on", 63);
		featureMap.put("sacrifices", 64);
		featureMap.put("a", 65);	
		featureMap.put("or", 66);
		featureMap.put("HELLER", 67);
		featureMap.put("was", 68);
		featureMap.put("services", 69);
		featureMap.put("families", 70);
		featureMap.put("the", 71);
		featureMap.put("connected", 72);
		featureMap.put("fallen", 73);
		featureMap.put("2016", 74);
		featureMap.put("STAR", 75);
		featureMap.put("service", 76);
		featureMap.put("duty", 77);
		featureMap.put("to", 78);
		featureMap.put("DAY", 79);
	}
	void buildDFMap(HashMap<String, Integer> dfMap){
		dfMap.put("05", 1);
		dfMap.put("considered", 1);
		dfMap.put("United", 2);
		dfMap.put("resolution", 2);
		dfMap.put("   By", 1);
		dfMap.put("granted", 1);
		dfMap.put("States", 2);
		dfMap.put("DESIGNATING", 1);
		dfMap.put("Armed", 1);
		dfMap.put("Department", 1);
		dfMap.put("Laws", 1);
		dfMap.put("foregoing", 1);
		dfMap.put("4841", 1);
		dfMap.put("4842", 1);
		dfMap.put("4843", 1);
		dfMap.put("ROTHFUS", 1);
		dfMap.put("4844", 1);
		dfMap.put("4845", 1);
		dfMap.put("4846", 1);
		dfMap.put("Constitution", 1);
		dfMap.put("4847", 1);
		dfMap.put("SENATE", 1);
		dfMap.put("in", 2);
		dfMap.put("made", 1);
		dfMap.put("RESOLUTION", 1);
		dfMap.put("18", 1);
		dfMap.put("active", 1);
		dfMap.put("bill", 1);
		dfMap.put("is", 1);
		dfMap.put("House", 1);
		dfMap.put("BARLETTA", 1);
		dfMap.put("COMSTOCK", 1);
		dfMap.put("AUTHORITY", 1);
		dfMap.put("1", 1);
		dfMap.put("AS", 1);
		dfMap.put("himself", 1);
		dfMap.put("as", 1);				
		dfMap.put("mission", 1);
		dfMap.put("submitted", 2);
		dfMap.put("3", 1);
		dfMap.put("provide", 1);
		dfMap.put("5", 1);
		dfMap.put("following", 2);
		dfMap.put("7", 1);
		dfMap.put("8", 1);
		dfMap.put("SUBMITTED", 1);
		dfMap.put("S1675", 1);
		dfMap.put("primary", 1);
		dfMap.put("23", 1);
		dfMap.put("PATRICK", 1);
		dfMap.put("other", 1);
		dfMap.put("Whereas", 1);
		dfMap.put("be", 1);
		dfMap.put("CARTWRIGHT", 1);
		dfMap.put("H", 1);
		dfMap.put("March", 1);
		dfMap.put("I", 1);
		dfMap.put("FARENTHOLD", 1);
		dfMap.put("Mrs", 2);
		dfMap.put("result", 1);
		dfMap.put("into", 1);
		dfMap.put("R", 1);
		dfMap.put("S", 1);
		dfMap.put("Star", 1);
		dfMap.put("CONSTITUTIONAL", 1);
		dfMap.put("are", 1);
		dfMap.put("413", 1);
		dfMap.put("by", 2);
		dfMap.put("have", 1);
		dfMap.put("``GOLD", 1);
		dfMap.put("power", 1);
		dfMap.put("agreed", 1);
		dfMap.put("   Pursuant", 1);				
		dfMap.put("BOXER", 1);
		dfMap.put("legislation", 1);
		dfMap.put("clause", 1);
		dfMap.put("a", 1);
		dfMap.put("necessary", 1);
		dfMap.put("H1584", 1);
		dfMap.put("Government", 1);
		dfMap.put("HELLER", 1);
		dfMap.put("services", 1);
		dfMap.put("Rules", 1);
		dfMap.put("Section", 1);
		dfMap.put("the", 2);
		dfMap.put("connected", 1);
		dfMap.put("STAR", 1);
		dfMap.put("MALONEY", 1);			
		dfMap.put("By", 1);
		dfMap.put("XII", 1);
		dfMap.put("to", 2);
		dfMap.put("powers", 1);
		dfMap.put("DAY", 1);
		dfMap.put("STATEMENT", 1);
		dfMap.put("Res", 1);
		dfMap.put("New", 1);
		dfMap.put("friendship", 1);
		dfMap.put("joint", 1);
		dfMap.put("disability", 1);
		dfMap.put("pursuant", 1);
		dfMap.put("Wives", 1);
		dfMap.put("Page", 2);
		dfMap.put("section", 1);
		dfMap.put("enact", 1);
		dfMap.put("honors", 1);
		dfMap.put("KIRKPATRICK", 1);
		dfMap.put("Execution", 1);
		dfMap.put("members", 1);
		dfMap.put("has", 1);
		dfMap.put("To", 1);
		dfMap.put("WIVES", 1);
		dfMap.put("which", 2);
		dfMap.put("spouses", 1);
		dfMap.put("all", 1);
		dfMap.put("Gold", 1);
		dfMap.put("   ", 1);
		dfMap.put("this", 1);
		dfMap.put("shall", 1);
		dfMap.put("April", 1);
		dfMap.put("regarding", 1);
		dfMap.put("Senate", 1);
		dfMap.put("thereof", 1);
		dfMap.put("support", 1);
		dfMap.put("Inc", 1);
		dfMap.put("who", 1);
		dfMap.put("carrying", 1);
		dfMap.put("Congress", 1);
		dfMap.put("veterans", 1);
		dfMap.put("Mr", 2);
		dfMap.put("York", 1);
		dfMap.put("RESOLUTIONS", 1);
		dfMap.put("for",2);
		dfMap.put("rule", 1);
		dfMap.put("proper", 1);
		dfMap.put("statements", 1);
		dfMap.put("died", 1);
		dfMap.put("Representatives", 1);
		dfMap.put("BURR", 1);
		dfMap.put("Forces", 1);
		dfMap.put("vested", 1);
		dfMap.put("   Article", 1);
		dfMap.put("represents", 1);
		dfMap.put("and", 2);
		dfMap.put("of", 2);
		dfMap.put("America", 1);
		dfMap.put("SEAN", 1);
		dfMap.put("accompanying",1);
		dfMap.put("make", 1);
		dfMap.put("APRIL", 1);
		dfMap.put("on", 1);
		dfMap.put("sacrifices", 1);
		dfMap.put("or", 2);
		dfMap.put("   H", 1);
		dfMap.put("was", 1);
		dfMap.put("families", 1);
		dfMap.put("specific", 1);
		dfMap.put("any", 1);
		dfMap.put("fallen", 1);
		dfMap.put("2016", 2);
		dfMap.put("service", 1);
		dfMap.put("duty", 1);
		dfMap.put("Powers",1);
		dfMap.put("Clause", 1);
		dfMap.put("Officer", 1);
	}

	void buildExpectedHashMap(){
		expectedHashMap = new HashMap<String, Double>();
		expectedHashMap.put("Res", 0.3010299956639812);
		expectedHashMap.put("friendship", 0.3010299956639812);
		expectedHashMap.put("05", 0.3010299956639812);
		expectedHashMap.put("considered", 0.3010299956639812);
		expectedHashMap.put("disability", 0.3010299956639812);
		expectedHashMap.put("United", 0.0);
		expectedHashMap.put("Wives", 0.6020599913279624);
		expectedHashMap.put("Page", 0.0);
		expectedHashMap.put("resolution", 0.0);
		expectedHashMap.put("honors", 0.3010299956639812);
		expectedHashMap.put("States", 0.0);
		expectedHashMap.put("DESIGNATING", 0.3010299956639812);
		expectedHashMap.put("Armed", 0.9030899869919435);
		expectedHashMap.put("members", 0.9030899869919435);
		expectedHashMap.put("WIVES", 0.3010299956639812);
		expectedHashMap.put("which", 0.0);
		expectedHashMap.put("spouses", 0.9030899869919435);
		expectedHashMap.put("Gold", 0.6020599913279624);
		expectedHashMap.put("made", 0.3010299956639812);
		expectedHashMap.put("RESOLUTION", 0.3010299956639812);
		expectedHashMap.put("active", 0.3010299956639812);
		expectedHashMap.put("is", 0.3010299956639812);
		expectedHashMap.put("April", 0.3010299956639812);
		expectedHashMap.put("Senate", 0.6020599913279624);
		expectedHashMap.put("AS", 0.3010299956639812);
		expectedHashMap.put("himself", 0.3010299956639812);
		expectedHashMap.put("as", 0.3010299956639812);
		expectedHashMap.put("mission", 0.3010299956639812);
		expectedHashMap.put("submitted", 0.0);
		expectedHashMap.put("provide", 0.3010299956639812);
		expectedHashMap.put("5", 0.3010299956639812);
		expectedHashMap.put("following", 0.0);
		expectedHashMap.put("SUBMITTED", 0.3010299956639812);
		expectedHashMap.put("support", 0.3010299956639812);
		expectedHashMap.put("S1675", 0.3010299956639812);
		expectedHashMap.put("Inc", 0.6020599913279624);
		expectedHashMap.put("who", 0.3010299956639812);
		expectedHashMap.put("primary", 0.3010299956639812);
		expectedHashMap.put("veterans", 0.6020599913279624);
		expectedHashMap.put("Mr", 0.0);
		expectedHashMap.put("RESOLUTIONS", 0.3010299956639812);
		expectedHashMap.put("for", 0.0);
		expectedHashMap.put("died", 0.3010299956639812);
		expectedHashMap.put("BURR", 0.3010299956639812);
		expectedHashMap.put("Forces", 0.9030899869919435);
		expectedHashMap.put("Mrs", 0.0);
		expectedHashMap.put("represents", 0.3010299956639812);
		expectedHashMap.put("result", 0.3010299956639812);
		expectedHashMap.put("Star", 0.6020599913279624);
		expectedHashMap.put("413", 0.6020599913279624);
		expectedHashMap.put("and", 0.0);
		expectedHashMap.put("by", 0.0);
		expectedHashMap.put("of", 0.0);
		expectedHashMap.put("America", 0.6020599913279624);
		expectedHashMap.put("have", 0.3010299956639812);
		expectedHashMap.put("``GOLD", 0.3010299956639812);
		expectedHashMap.put("agreed", 0.3010299956639812);
		expectedHashMap.put("APRIL", 0.3010299956639812);
		expectedHashMap.put("BOXER", 0.3010299956639812);
		expectedHashMap.put("on", 0.3010299956639812);
		expectedHashMap.put("sacrifices", 0.3010299956639812);
		expectedHashMap.put("a", 0.6020599913279624);
		expectedHashMap.put("or", 0.0);
		expectedHashMap.put("HELLER", 0.3010299956639812);
		expectedHashMap.put("was", 0.3010299956639812);
		expectedHashMap.put("services", 0.3010299956639812);
		expectedHashMap.put("families", 0.6020599913279624);
		expectedHashMap.put("the", 0.0);
		expectedHashMap.put("connected", 0.3010299956639812);
		expectedHashMap.put("fallen", 0.6020599913279624);
		expectedHashMap.put("2016", 0.0);
		expectedHashMap.put("STAR", 0.3010299956639812);
		expectedHashMap.put("service", 0.3010299956639812);
		expectedHashMap.put("duty", 0.3010299956639812);
		expectedHashMap.put("to", 0.0);
		expectedHashMap.put("DAY", 0.3010299956639812);
		expectedHashMap.put("Whereas", 0.9030899869919435);
		expectedHashMap.put("SENATE", 0.3010299956639812);
		expectedHashMap.put("Mr", 0.0);
		expectedHashMap.put("S", 0.3010299956639812);
	}
}
