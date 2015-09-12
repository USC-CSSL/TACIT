package edu.usc.cssl.tacit.domain.web.services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class TacitDBHandler extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String org = request.getParameter("orgname");
		String emailid = request.getParameter("emailid");
		String orgType = request.getParameter("orgtype");
		MongoClient mongoClient = new MongoClient();

		MongoDatabase database = mongoClient.getDatabase("test");
		MongoCollection<Document> collection = database.getCollection("tacit");
		Document doc = new Document("organization", org).append("emailid", emailid).append("orgtype", orgType);
		collection.insertOne(doc);
		response.sendRedirect("download2.html");
	}
}