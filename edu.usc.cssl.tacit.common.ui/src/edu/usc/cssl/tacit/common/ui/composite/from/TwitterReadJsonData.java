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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParseException;

public class TwitterReadJsonData {
	DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
	Date dateobj = new Date();

	public List<String> retrieveTwitterData(String location) {
		List<String> result = new ArrayList<String>();
		/*** read from file ***/
		JSONParser jParser;
		try {

			jParser = new JSONParser();

			// loop until token equal to "}"

			String path = new File(location) + File.separator + "twitter_";
			File[] fileList = new File(location).listFiles();
			for (int i = 0; i < fileList.length; i++) {
				String fileName = fileList[i].getAbsolutePath();
				if (!fileList[i].getAbsolutePath().endsWith(".json"))
					continue;
				JSONArray objects = (JSONArray) jParser.parse(new FileReader(
						fileName));
				for (Object obj : objects) {
					JSONObject twitterStream = (JSONObject) obj;
					dateobj = new Date();
					File file = new File(path + df.format(dateobj));
					if (file.exists()) {
						file.delete();
					}
					file.createNewFile();

					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(twitterStream.get("Text").toString());
					bw.close();
					result.add(file.getAbsolutePath());

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

		return result;
	}

}
