package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class MongoDBFlapDoodleTest {

	private MongoClient _mongo = FlapDoodleEmbeddedMongoClient.MONGOCLIENT.get();
	
	@Before
	public void setUp() {
		_mongo.getDatabase("test").drop();
	}
	
	@Test
	public void testCreateDocument() {
		MongoDatabase db = _mongo.getDatabase("test");
		db.createCollection("testCol", new CreateCollectionOptions().capped(false));
		MongoCollection<Document> col = db.getCollection("testCol");
		Document document = new Document("name", "Café Con Leche");
		col.insertOne(document);
		ObjectId _id = document.getObjectId("_id");
		assertTrue(_id!=null);
		assertTrue(col.countDocuments() == 1);
	}

	@Test
	public void testReadDocument() {
		MongoDatabase db = _mongo.getDatabase("test");
		db.createCollection("testCol", new CreateCollectionOptions().capped(false));
		MongoCollection<Document> col = db.getCollection("testCol");
		Document document = new Document("name", "Test");
		col.insertOne(document);
		Document readDoc = col.find(Filters.eq("name", "Test")).first();
		assertNotNull(readDoc);
	}

	@Test
	public void testUpdateDocument() {
		MongoDatabase db = _mongo.getDatabase("test");
		db.createCollection("testCol", new CreateCollectionOptions().capped(false));
		MongoCollection<Document> col = db.getCollection("testCol");
		Document document = new Document("name", "Test");
		col.insertOne(document);
		col.updateOne(Filters.eq("name", "Test"),
				Updates.combine(Updates.set("name", "Updated"), Updates.currentDate("lastModified")));
		Document readDoc = col.find(Filters.eq("name", "Updated")).first();
		assertTrue(col.countDocuments() == 1);
		assertNotNull(readDoc);
	}

	@Test
	public void testDeleteDocument() {
		MongoDatabase db = _mongo.getDatabase("test");
		db.createCollection("testCol", new CreateCollectionOptions().capped(false));
		MongoCollection<Document> col = db.getCollection("testCol");
		Document document = new Document("name", "Test");
		col.insertOne(document);
		assertTrue(col.countDocuments() == 1);
		col.deleteOne(Filters.eq("name", "Test"));
		assertTrue(col.countDocuments() == 0);
	}

}