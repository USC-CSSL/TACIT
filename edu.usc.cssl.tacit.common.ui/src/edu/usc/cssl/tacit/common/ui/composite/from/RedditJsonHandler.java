package edu.usc.cssl.tacit.common.ui.composite.from;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RedditJsonHandler {
	DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
	Date dateobj;
	
	public List<String> retrieveRedditData(String location) {
		List<String> result = new ArrayList<String>();
		JSONParser jParser;
		try {
			jParser = new JSONParser();
			String path = new File(location) + File.separator + "reddit_";
			File[] fileList = new File(location).listFiles();
			for (File tempFile : fileList) {
				String fileName = tempFile.getAbsolutePath();
				if (!fileName.endsWith(".json"))
					continue;
				
				JSONObject redditStream = (JSONObject) jParser.parse(new FileReader(fileName));
				dateobj = new Date();				
				File file = new File(path + UUID.randomUUID().toString() + "-" + df.format(dateobj) + ".txt");
				System.out.println(file.getAbsolutePath());
				if (file.exists())
					file.delete();
				file.createNewFile();

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				String postTitle = getPostTitle(redditStream); 
				String[] postComments = getPostComments(redditStream);
				
				if(null != postTitle) {
					bw.write(postTitle); // description
					bw.write("\n");
				}
				
				for(String commentBody : postComments) {
					if(null == commentBody) continue;
					bw.write(commentBody); // comment body
					bw.write("\n");					
				}				
				bw.close();
				result.add(file.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String[] getPostComments(JSONObject redditStream) {
		if(null == redditStream) return null;
		JSONArray comments = (JSONArray) redditStream.get("comments");
		if(null == comments) return null;
		String[] commentBody = new String[comments.size()];
		int index = -1;
		for(Object obj : comments) {
			JSONObject comment = (JSONObject) obj;
			if(null == comment) continue;
			commentBody[++index] = comment.get("body").toString();
		}
		return commentBody;
	}

	private String getPostTitle(JSONObject redditStream) {
		if(null == redditStream) return null;
		JSONObject post = (JSONObject) redditStream.get("post");
		if(null == post) return null;
		return post.get("title").toString();
	}
}
