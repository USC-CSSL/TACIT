package edu.usc.cssl.tacit.common.ui.composite.from;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RedditJsonHandler {
	Date dateObj;
	String invalidFilenameCharacters = "[\\/:*?\"<>|]+";
	public String retrieveRedditData(String location) { // tacit location of the corpus
		JSONParser jParser;
		dateObj = new Date();
		String path = System.getProperty("user.dir") + System.getProperty("file.separator")+ "tacit_temp_files" + File.separator + "reddit_data_" + dateObj.getTime();
			jParser = new JSONParser();
			new File(path).mkdirs();
			File[] fileList = new File(location).listFiles();
			for (File tempFile : fileList) {
				String fileName = tempFile.getAbsolutePath();
				if (!fileName.endsWith(".json"))
					continue;
				
				JSONObject redditStream;
				dateObj = new Date();	
				
				String postTitle = null;
				String[] postComments = null;			
				FileWriter fw = null;
				BufferedWriter bw = null;
				
				try {
					redditStream = (JSONObject) jParser.parse(new FileReader(fileName));
					postTitle = getPostTitle(redditStream); 
					postComments = getPostComments(redditStream);
					int titleLimit = postTitle.length()>20 ? 20 : postTitle.length();
					File file = new File(path + System.getProperty("file.separator") + postTitle.substring(0, titleLimit).replaceAll(invalidFilenameCharacters, "") + "-" + dateObj.getTime() + ".txt");

					fw = new FileWriter(file.getAbsoluteFile());
					bw = new BufferedWriter(fw);
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
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassCastException e) {
					//ignore summary file
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		return path;
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
	
	public static void main(String[] args) {
		RedditJsonHandler rh = new RedditJsonHandler();
		System.out.println(rh.retrieveRedditData("C:\\Program Files (x86)\\eclipse\\tacit_corpora\\Reddit_TOP_1443140353236\\top"));
	}	
}
