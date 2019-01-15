package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.bson.Document;
import org.bson.types.ObjectId;
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

	private static MongoDocumentDaoImpl dao;
	private final static MongoClient _mongo = FlapDoodleEmbeddedMongoClient.MONGOCLIENT.get();
	private MongoCollection<Document> col;
	private final static Gson gson = new Gson();

	@BeforeClass
	public static void beforeClass() {
		dao = new MongoDocumentDaoImpl(FlapDoodleEmbeddedMongoClient.MONGOCLIENT.get(), "test", "testCol", gson);
	}

	@Before
	public void setUp() {
		_mongo.getDatabase("test").drop();
		_mongo.getDatabase("test").createCollection("testCol", new CreateCollectionOptions().capped(false));
		col = _mongo.getDatabase("test").getCollection("testCol");
	}

	@Test(expected = NullPointerException.class)
	public void testCreateDocument_null() {
		dao.createDocument(null);
	}

	@Test
	public void testCreateDocument() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		ObjectId oid = dao.createDocument(object);
		Document doc = col.find(Filters.eq("_id", oid)).first();
		assertNotNull(doc);
	}

	@Test
	public void testCreateDocument_findOne_byEffectiveFrom() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		dao.createDocument(object);
		DefaultValueObject readObj = gson.fromJson(
				col.find(Filters.eq("bitemporalStamp.effectiveFrom.seconds",
						object.getBitemporalStamp().getEffectiveFrom().getEpochSecond())).first().toJson(),
				DefaultValueObject.class);
		assertEquals(object, readObj);
	}

	@Test
	public void testCreateDocument_findNone_byEffectiveFrom() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		dao.createDocument(object);
		// try find one where stored effective date is equal to specified value, when specified value is non equal
		Document doc = col.find(Filters.eq("bitemporalStamp.effectiveFrom.seconds", 010101L)).first();
		assertNull(doc);
	}

	@Test
	public void testCreateDocument_findOne_byEffectiveFrom_GreaterEquals_EffectiveDate_Equal() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond();
		dao.createDocument(object);
		// try find one where stored effective date is greater/equals to specified value, when specified value is equal
		Document doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNotNull(doc);
	}
	
	@Test
	public void testCreateDocument_findNone_byEffectiveFrom_GreaterEquals_EffectiveDate_Greater() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond()+2L;
		dao.createDocument(object);
		// try find one where stored effective date is greater/equals to specified value, when specified value is higher
		Document doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNull(doc);
	}
	
	@Test
	public void testCreateDocument_findOne_byEffectiveFrom_GreaterEquals_EffectiveDate_Lower() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond()-2L;
		dao.createDocument(object);
		// try find one where stored effective date is greater/equals to specified value, when specified value is lower
		Document doc = col.find(Filters.gte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNotNull(doc);
	}
	
	@Test
	public void testCreateDocument_findOne_byEffectiveFrom_LowerEquals_EffectiveDate_Greater() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond()+2L;
		dao.createDocument(object);
		// try find one where stored effective date is lower/equals to specified value, when specified value is higher
		Document doc = col.find(Filters.lte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNotNull(doc);
	}
	
	@Test
	public void testCreateDocument_findOne_byEffectiveFrom_LowerEquals_EffectiveDate_Equals() {
		DefaultValueObject object = EnhancedRandom.random(DefaultValueObject.class);
		long epocheseconds = object.getBitemporalStamp().getEffectiveFrom().getEpochSecond();
		dao.createDocument(object);
		// try find one where stored effective date is lower/equals to specified value, when specified value is equal
		Document doc = col.find(Filters.lte("bitemporalStamp.effectiveFrom.seconds", epocheseconds)).first();
		assertNotNull(doc);
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
