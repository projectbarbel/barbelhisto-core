package com.projectbarbel.histo.persistence.impl.mongo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class MongoDBFlapDoodleTest {

	private FlapDoodleEmbeddedMongoClientDaoSupplier _mongo = FlapDoodleEmbeddedMongoClientDaoSupplier.MONGOCLIENT;
	
	@Before
	public void setUp() {
		_mongo.getMongo().getDatabase("test").drop();
	}
	
	@Test
	public void testCreateDocument() {
		MongoDatabase db = _mongo.getMongo().getDatabase("test");
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
		MongoDatabase db = _mongo.getMongo().getDatabase("test");
		db.createCollection("testCol", new CreateCollectionOptions().capped(false));
		MongoCollection<Document> col = db.getCollection("testCol");
		Document document = new Document("name", "BlockTest");
		col.insertOne(document);
		Document readDoc = col.find(Filters.eq("name", "BlockTest")).first();
		assertNotNull(readDoc);
	}

	@Test
	public void testUpdateDocument() {
		MongoDatabase db = _mongo.getMongo().getDatabase("test");
		db.createCollection("testCol", new CreateCollectionOptions().capped(false));
		MongoCollection<Document> col = db.getCollection("testCol");
		Document document = new Document("name", "BlockTest");
		col.insertOne(document);
		col.updateOne(Filters.eq("name", "BlockTest"),
				Updates.combine(Updates.set("name", "Updated"), Updates.currentDate("lastModified")));
		Document readDoc = col.find(Filters.eq("name", "Updated")).first();
		assertTrue(col.countDocuments() == 1);
		assertNotNull(readDoc);
	}

	@Test
	public void testDeleteDocument() {
		MongoDatabase db = _mongo.getMongo().getDatabase("test");
		db.createCollection("testCol", new CreateCollectionOptions().capped(false));
		MongoCollection<Document> col = db.getCollection("testCol");
		Document document = new Document("name", "BlockTest");
		col.insertOne(document);
		assertTrue(col.countDocuments() == 1);
		col.deleteOne(Filters.eq("name", "BlockTest"));
		assertTrue(col.countDocuments() == 0);
	}

}
