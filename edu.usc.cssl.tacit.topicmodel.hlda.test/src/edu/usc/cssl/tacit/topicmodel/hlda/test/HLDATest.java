package edu.usc.cssl.tacit.topicmodel.hlda.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.topicmodel.hlda.services.HierarchicalLDAModel;

public class HLDATest {
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void doHLDATest() throws IOException{
		HierarchicalLDAModel hlda = new HierarchicalLDAModel();
		List<List<String>> documentList = new ArrayList<List<String>>();
		for(File file:new File(directoryPath).listFiles()){
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
	
			List<String> sfile = new ArrayList<String>();
			while((line = br.readLine())!=null){
				String array[] = line.split(" ");
				for(String word:array){
					sfile.add(word);
				}
			}
			br.close();
			documentList.add(sfile);
		}
		Exception exception = null;
		try {
			hlda.readDocuments(documentList);
			hlda.doGibbsSampling(5, new NullProgressMonitor());
		} catch (Exception e) {
			exception = e;
		}
		assertEquals("Checking if any exception occured", exception, null);
	}
}
