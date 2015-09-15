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

public class TacitContactHandler extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("personname");
		String emailid = request.getParameter("email");
		String orgName = request.getParameter("orgname");
		String message = request.getParameter("message");
		MongoClient mongoClient = new MongoClient();

		MongoDatabase database = mongoClient.getDatabase("test");
		MongoCollection<Document> collection = database.getCollection("tacit_contact");
		Document doc = new Document("personname", name).append("email", emailid).append("orgname", orgName).append("message", message);
		collection.insertOne(doc);
		response.sendRedirect("index.html");
	}
}

