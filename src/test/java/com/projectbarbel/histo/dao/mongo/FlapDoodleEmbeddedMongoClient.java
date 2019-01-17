package com.projectbarbel.histo.dao.mongo;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.InstantCodec;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
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

    private final CodecRegistry pojoCodecRegistry = fromRegistries(
            CodecRegistries.fromCodecs(new InstantCodec(), new BitemporalCodec()),
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    private final MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry)
            .applyConnectionString(new ConnectionString("mongodb://localhost:12345")).build();

    private FlapDoodleEmbeddedMongoClient() {
        init();
    }

    private void init() {
        try {
            _mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                    .net(new Net("localhost", 12345, Network.localhostIsIPv6())).build());
            _mongod = _mongodExe.start();
            _mongo = MongoClients.create(settings);
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
