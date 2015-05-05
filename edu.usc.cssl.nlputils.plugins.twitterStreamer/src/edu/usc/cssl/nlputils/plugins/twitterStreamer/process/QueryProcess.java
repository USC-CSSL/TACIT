package edu.usc.cssl.nlputils.plugins.twitterStreamer.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.jayway.jsonpath.*;
import com.jayway.jsonassert.*;

import net.minidev.json.writer.*;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class QueryProcess  {
	public void Query (String input, String output, String query) throws IOException{
		System.out.println("In the Query Process Function\n");
		// what to do for bigdata?
		Path inputPath  = Paths.get(input);
		String dataset;
		byte[] encoded = Files.readAllBytes(inputPath);
		dataset = new String(encoded, "UTF-8");
		
		System.out.println("after reading data before parsing JSon\n");
		
		
		// Instantiate JSON writer
		JsonFactory jsonfactory = new JsonFactory();
		File outputFile = new File(output);
		final JsonGenerator jsonGenerator = jsonfactory.createGenerator(outputFile, JsonEncoding.UTF8);
		jsonGenerator.useDefaultPrettyPrinter();
		jsonGenerator.writeStartArray();
		
		System.out.println("after parser setup before writing\n");
		// Process and write query
		try{
			List<String> list = JsonPath.read(dataset, query);
			for(String s: list)
				jsonGenerator.writeString(s);
		}
		catch (ClassCastException e){
			try{
				jsonGenerator.writeString(JsonPath.read(dataset, query));
			}
			catch (ClassCastException e2) {
				System.out.println("Returned type is not String list. This query can only anything of type string.");
			}
		}
		//List<String> list = JsonPath.parse(dataset).read(query);
		
		//jsonGenerator.writeString(list.get(0));
		jsonGenerator.writeEndArray();
		jsonGenerator.close();
		System.out.println("Query processing DONE :D \n");
		

	}
}
