package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.bson.Document;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.projectbarbel.histo.dao.FlapDoodleEmbeddedMongoClient;
import com.projectbarbel.histo.model.DefaultValueObject;

import io.github.benas.randombeans.api.EnhancedRandom;

public class MongoDocumentDaoImplIntegrationTest {

	private static MongoDocumentDaoImpl<DefaultValueObject> dao;
	private static MongoClient _mongo = FlapDoodleEmbeddedMongoClient.MONGOCLIENT.get();
	private MongoCollection<Document> col;
	private Gson gson = new Gson();
	
	@BeforeClass
	public static void beforeClass() {
		dao = new MongoDocumentDaoImpl<DefaultValueObject>(FlapDoodleEmbeddedMongoClient.MONGOCLIENT.get());
	}
	
	@Before
	public void setUp() {
		_mongo.getDatabase("test").drop();
		_mongo.getDatabase("test").createCollection("testCol", new CreateCollectionOptions().capped(false));
		col = _mongo.getDatabase("test").getCollection("testCol");
	}
	
	@Test
	public void testCreateDocument() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		col.insertOne(Document.parse(gson.toJson(object)));
		DefaultValueObject readObj = gson.fromJson(col.find(Filters.eq("documentId", object.getDocumentId())).first().toJson(),DefaultValueObject.class);
		assertEquals(object, readObj);
	}

	@Test
	public void testUpdateDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadDocument() {
		fail("Not yet implemented");
	}

}
