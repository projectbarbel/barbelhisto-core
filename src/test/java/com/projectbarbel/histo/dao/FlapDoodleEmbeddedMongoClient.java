package com.projectbarbel.histo.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class FlapDoodleEmbeddedMongoClient {

	private static final MongodStarter starter = MongodStarter.getDefaultInstance();

	private MongodExecutable _mongodExe;
	private MongodProcess _mongod;
	private MongoClient _mongo;

	public final static FlapDoodleEmbeddedMongoClient MONGOCLIENT = new FlapDoodleEmbeddedMongoClient();
	
	private FlapDoodleEmbeddedMongoClient() {
		init();
	}
	
	private void init() {
		try {
			_mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)
					.net(new Net("localhost", 12345, Network.localhostIsIPv6())).build());
			_mongod = _mongodExe.start();
			_mongo = MongoClients.create("mongodb://localhost:12345");
		} catch (Exception e) {
			_mongod.stop();
			_mongodExe.stop();
			throw new RuntimeException("Could not create mongo client", e);
		}
	}

	public MongoClient get() {
		return _mongo;
	}
	
	public void close() {
		_mongod.stop();
		_mongodExe.stop();
	}
	
}
