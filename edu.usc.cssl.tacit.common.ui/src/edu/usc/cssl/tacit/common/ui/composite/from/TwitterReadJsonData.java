package edu.usc.cssl.tacit.common.ui.composite.from;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParseException;

public class TwitterReadJsonData {
	DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
	Date dateobj = new Date();

	public String retrieveTwitterData(String location) {
		/*** read from file ***/
		String path = null;
		JSONParser jParser;
		try {

			jParser = new JSONParser();

			// loop until token equal to "}"
			dateobj = new Date();
			 path = new File(System.getProperty("user.dir")
					+ System.getProperty("file.separator") + "tacit_temp_files")
					+ System.getProperty("file.separator")
					+ "twitter_" + UUID.randomUUID().toString()+ "_"
					+ df.format(dateobj);
			new File(path).mkdir();
			File[] fileList = new File(location).listFiles();
			for (int i = 0; i < fileList.length; i++) {
				String fileName = fileList[i].getAbsolutePath();
				if (!fileList[i].getAbsolutePath().endsWith(".json"))
					continue;
				JSONArray objects = (JSONArray) jParser.parse(new FileReader(
						fileName));
				int j=0;
				for (Object obj : objects) {
					JSONObject twitterStream = (JSONObject) obj;
					dateobj = new Date();
					File file = new File(path + File.separator+"twitter_"+j+"-" + df.format(dateobj));
					j++;
					if (file.exists()) {
						file.delete();
					}
					

					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(twitterStream.get("Text").toString());
					bw.close();
					

				}
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return path;
	}

}
