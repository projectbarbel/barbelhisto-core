package com.projectbarbel.histo.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoDocumentDaoImplTest {

	private static final MongodStarter starter = MongodStarter.getDefaultInstance();

	private static MongodExecutable _mongodExe;
	private static MongodProcess _mongod;
	private static MongoClient _mongo;

	@BeforeClass
	public static void before() throws IOException {
		try {
			_mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)
					.net(new Net("localhost", 12345, Network.localhostIsIPv6())).build());
			_mongod = _mongodExe.start();
			_mongo = MongoClients.create("mongodb://localhost:12345");
		} catch (Exception e) {
			_mongod.stop();
			_mongodExe.stop();
			throw e;
		}
	}

	@AfterClass 
	public static void after() {
		_mongod.stop();
		_mongodExe.stop();
	}

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
