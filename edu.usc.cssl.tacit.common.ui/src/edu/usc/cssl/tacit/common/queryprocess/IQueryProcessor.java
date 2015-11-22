package edu.usc.cssl.tacit.common.queryprocess;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public interface IQueryProcessor {

	public List<String> getJsonKeys() throws JsonSyntaxException,
			JsonIOException, FileNotFoundException;

	public String applyFilter() throws FileNotFoundException, IOException,
			ParseException;

}
