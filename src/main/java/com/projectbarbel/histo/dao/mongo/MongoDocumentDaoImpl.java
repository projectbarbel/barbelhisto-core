package com.projectbarbel.histo.dao.mongo;

import java.util.Objects;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.projectbarbel.histo.dao.DocumentDao;
import com.projectbarbel.histo.model.DefaultValueObject;

public class MongoDocumentDaoImpl implements DocumentDao<DefaultValueObject, ObjectId> {

	private final MongoClient client;
	private final String databaseName;
	private final String collectionName;
	private final Gson gson;
	
	public MongoDocumentDaoImpl(MongoClient client, String databaseName, String collectionName, Gson gson) {
		Objects.requireNonNull(client, "client must not be null in Mongo DAO");
		Objects.requireNonNull(databaseName, "databaseName must not be null in Mongo DAO");
		Objects.requireNonNull(collectionName, "collectionName must not be null in Mongo DAO");
		Objects.requireNonNull(gson, "gson must not be null in Mongo DAO");
		this.client = client;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.gson = gson;
	}

	@Override
	public ObjectId createDocument(DefaultValueObject document) {
		Objects.requireNonNull(document, "document must not be null when creating a document");
		MongoCollection<Document> col = client.getDatabase(databaseName).getCollection(collectionName);
		Document doc = Document.parse(gson.toJson(document));
		col.insertOne(doc);
		return doc.getObjectId("_id");
	}

	@Override
	public ObjectId updateDocument(DefaultValueObject document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteDocument(ObjectId objectId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readDocument(ObjectId objectId) {
		// TODO Auto-generated method stub
		
	}
	
}
