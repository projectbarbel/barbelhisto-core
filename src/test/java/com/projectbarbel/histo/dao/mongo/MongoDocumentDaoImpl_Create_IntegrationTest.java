package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;

import io.github.benas.randombeans.api.EnhancedRandom;

public class MongoDocumentDaoImpl_Create_IntegrationTest {

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
	public void testCreateDocument() {
		DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		dao.createDocument(object);
		DefaultMongoValueObject doc = col.find(Filters.eq("objectId", object.getObjectId())).first();
		assertNotNull(doc);
	}

	@Test
	public void testCreateDocument_findOne_byEffectiveFrom() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		dao.createDocument(object);
		DefaultMongoValueObject readObj = col.find(Filters.eq("bitemporalStamp.effectiveFrom.seconds",
						object.getBitemporalStamp().getEffectiveFrom().getEpochSecond())).first();
		assertEquals(object, readObj);
	}

	@Test
	public void testCreateDocument_findNone_byEffectiveFrom() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		dao.createDocument(object);
		// try find one where stored effective date is equal to specified value, when
		// specified value is non equal
		DefaultMongoValueObject doc = col.find(Filters.eq("bitemporalStamp.effectiveFrom.seconds", 010101L)).first();
		assertNull(doc);
	}

	@Test
	public void testCreateDocument_findOne_byEffectiveFrom_GreaterEquals_EffectiveDate_Equal() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond();
		dao.createDocument(object);
		// try find one where stored effective date is greater/equals to specified
		// value, when specified value is equal
		DefaultMongoValueObject doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNotNull(doc);
	}

	@Test
	public void testCreateDocument_findNone_byEffectiveFrom_GreaterEquals_EffectiveDate_Greater() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond() + 2L;
		dao.createDocument(object);
		// try find one where stored effective date is greater/equals to specified
		// value, when specified value is higher
		DefaultMongoValueObject doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNull(doc);
	}

	@Test
	public void testCreateDocument_findOne_byEffectiveFrom_GreaterEquals_EffectiveDate_Lower() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond() - 2L;
		dao.createDocument(object);
		// try find one where stored effective date is greater/equals to specified
		// value, when specified value is lower
		DefaultMongoValueObject doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNotNull(doc);
	}

	@Test
	public void testCreateDocument_findOne_byEffectiveFrom_LowerEquals_EffectiveDate_Greater() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond() + 2L;
		dao.createDocument(object);
		// try find one where stored effective date is lower/equals to specified value,
		// when specified value is higher
		DefaultMongoValueObject doc = col.find(Filters.lte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNotNull(doc);
	}

	@Test
	public void testCreateDocument_findOne_byEffectiveFrom_LowerEquals_EffectiveDate_Equals() {
	    DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond();
		dao.createDocument(object);
		// try find one where stored effective date is lower/equals to specified value,
		// when specified value is equal
		DefaultMongoValueObject doc = col.find(Filters.lte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNotNull(doc);
	}

}
