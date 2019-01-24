package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.projectbarbel.histo.BarbelTestHelper;

public class MongoPojoIntegrationTests {

	private final static MongoClient _mongo = FlapDoodleEmbeddedMongoClientDaoSupplier.MONGOCLIENT.getMongo();
	private MongoCollection<DefaultMongoValueObject> col;

	@Before
	public void setUp() {
		_mongo.getDatabase("test").drop();
		_mongo.getDatabase("test").createCollection("testCol", new CreateCollectionOptions().capped(false));
		col = _mongo.getDatabase("test").getCollection("testCol", DefaultMongoValueObject.class);
//		_mongo.getDatabase("test").createCollection("testCol", new CreateCollectionOptions().capped(false));
	}

	@Test
	public void testReadDocument_byData() {
		DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
		col.insertOne(object);
		assertTrue("should be found by data", col.find(Filters.eq("data", object.getData())).iterator().hasNext());
	}

	@Test
	public void testReadDocument_byId() {
	    DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
	    col.insertOne(object);
	    assertFalse("should not be found by id", col.find(Filters.eq("id", object.getVersionId())).iterator().hasNext());
	}
	
	@Test
	public void testReadDocument_by_id() {
	    DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
	    col.insertOne(object);
	    assertTrue("should be found by _id", col.find(Filters.eq("objectId", object.getVersionId())).iterator().hasNext());
	}
	
}
