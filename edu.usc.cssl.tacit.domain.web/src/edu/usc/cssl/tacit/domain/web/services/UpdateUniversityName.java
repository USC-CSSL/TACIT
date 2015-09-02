package edu.usc.cssl.tacit.domain.web.services;

import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class UpdateUniversityName {
	
	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();

		MongoDatabase database = mongoClient.getDatabase("test");
		MongoCollection<Document> collection = database.getCollection("download");
		Document doc = new Document("university", "USC")
	               .append("emailid", "abc@usc.edu");
		collection.insertOne(doc);
	}

}
