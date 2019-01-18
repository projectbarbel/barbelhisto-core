package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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

public class MongoDocumentDaoImpl_Update_IntegrationTest {

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

	@Test(expected = NullPointerException.class)
	public void testCreateDocument_null() {
		dao.createDocument(null);
	}

	@Test
	public void testUpdateDocument_updateData() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		Optional<ObjectId> oid = dao.createDocument(object);
		DefaultMongoValueObject doc = col.find(Filters.eq("_id", oid.orElse(new ObjectId()))).first();
		DefaultMongoValueObject changedObj = new DefaultMongoValueObject(object.getObjectId(), object.getBitemporalStamp(), "new data");
		Optional<ObjectId> newId = dao.updateDocument(oid.orElse(new ObjectId()), changedObj);
		DefaultMongoValueObject updatedObj = col.find(Filters.eq("objectId", newId.orElse(new ObjectId()))).first();
		assertEquals(changedObj, updatedObj);
		assertTrue(updatedObj.getData().equals("new data"));
		assertNotEquals(updatedObj, doc);
	}

	@Test
	public void testUpdateDocument_noUpdates() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
	    Optional<ObjectId> oid = dao.createDocument(object);
		Optional<ObjectId> updatedOid = dao.updateDocument(oid.orElse(new ObjectId()), object);
		DefaultMongoValueObject updatedDoc = col.find(Filters.eq("objectId", updatedOid.orElse(new ObjectId()))).first();
		assertEquals(updatedDoc.getData(), object.getData());
		assertEquals(updatedDoc, object);
	}

}
