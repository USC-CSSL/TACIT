package edu.usc.cssl.tacit.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class CSVtoJSON {
	JsonFactory jsonfactory;
	JsonGenerator jsonGenerator;
	
//	public static void main(String args[]){
//		new CSVtoJSON().convert("input.csv");
//	}
	
	public void convert(String input, String output1){
		File f = new File(input);
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;
			File output = new File(output1);
			jsonfactory = new JsonFactory();
			jsonGenerator = jsonfactory.createGenerator(output, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
			String[] header = null,fields=null;
			int count=1;
			while((line = br.readLine())!= null){
				if(count==1)
					header = line.split(",");
				else{
					fields = line.split(",");
					jsonGenerator.writeStartObject();
					for(int i=0; i<fields.length;i++)
						jsonGenerator.writeStringField(header[i], fields[i]);
					jsonGenerator.writeEndObject();
				}
					count++;
				
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.flush();
			jsonGenerator.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
