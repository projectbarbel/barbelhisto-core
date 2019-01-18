package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;

import io.github.benas.randombeans.api.EnhancedRandom;

public class MongoDocumentDaoImpl_Delete_IntegrationTest {

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
		col = _mongo.getDatabase("test").getCollection("testCol",DefaultMongoValueObject.class);
	}

	@Test(expected = NullPointerException.class)
	public void testCreateDocument_null() {
		dao.createDocument(null);
	}

	@Test
	public void testDeleteDocument() {
		DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		Optional<ObjectId> oid = dao.createDocument(object);
		DefaultMongoValueObject doc = col.find(Filters.eq("objectId", oid.get())).first();
		assertNotNull(doc);
		dao.deleteDocument(oid.get());
		boolean hasnext = col.find(Filters.eq("objectId", oid.get())).iterator().hasNext();
		assertFalse(hasnext);
	}

}
