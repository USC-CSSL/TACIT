package edu.usc.cssl.tacit.domain.web.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.jsoup.Jsoup;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class TacitDBHandler extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			String org = request.getParameter("orgname");
			String emailid = request.getParameter("emailid");
			String orgType = request.getParameter("orgtype");
			// to let users from cssl to test freely
			if(!emailid.equals("cssl@usc.edu")) {
				
			MongoClient mongoClient = new MongoClient();
			String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
			MongoDatabase database = mongoClient.getDatabase("downloads");
			MongoCollection<Document> collection = database.getCollection("tacit");
			Document doc = new Document("organization", org).append("emailid",
					emailid).append("orgtype", orgType).append("createdDate", date);
			collection.insertOne(doc);
			}
			// Set response content type
			response.setContentType("text/html");

			// Actual logic goes here.
			PrintWriter out = response.getWriter();
			InputStream in = getServletContext().getResourceAsStream("/download240389.txt");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
	//		org.jsoup.nodes.Document download2 = Jsoup.parse(in,"UTF-8",null);
			out.println(sb.toString());
	}
	public static void main(String[] args) {
		File in = new File("./WebContent/download2.txt");
		try {
			org.jsoup.nodes.Document download2 = Jsoup.parse(in, null);
			System.out.println(download2.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}