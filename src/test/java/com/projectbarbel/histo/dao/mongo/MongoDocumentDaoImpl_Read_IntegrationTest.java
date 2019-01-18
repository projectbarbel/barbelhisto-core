package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;

import io.github.benas.randombeans.api.EnhancedRandom;

public class MongoDocumentDaoImpl_Read_IntegrationTest {

	private static MongoDocumentDaoImpl dao;
	private final static MongoClient _mongo = FlapDoodleEmbeddedMongoClient.MONGOCLIENT.get();
	private MongoCollection<DefaultMongoValueObject> col;

	@BeforeClass
	public static void beforeClass() {
		dao = new MongoDocumentDaoImpl(FlapDoodleEmbeddedMongoClient.MONGOCLIENT.get(), "test", "testCol");
	}

	@Before
	public void setUp() {
		_mongo.getDatabase("test").drop();
		_mongo.getDatabase("test").createCollection("testCol", new CreateCollectionOptions().capped(false));
		col = _mongo.getDatabase("test").getCollection("testCol", DefaultMongoValueObject.class);
	}

	@Test
	public void testReadDocument() {
		DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		col.insertOne(object);
		assertTrue("document should be present",dao.readDocument(object.getObjectId()).isPresent());
	}

}
