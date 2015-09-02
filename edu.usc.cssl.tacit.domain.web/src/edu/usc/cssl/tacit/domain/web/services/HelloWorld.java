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

public class HelloWorld extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String univ = request.getParameter("universityname");
		String emailid = request.getParameter("emailid");
		MongoClient mongoClient = new MongoClient();

		MongoDatabase database = mongoClient.getDatabase("test");
		MongoCollection<Document> collection = database.getCollection("tacit");
		Document doc = new Document("university", univ).append("emailid", emailid);
		collection.insertOne(doc);
		response.sendRedirect("download2.html");
	}
}