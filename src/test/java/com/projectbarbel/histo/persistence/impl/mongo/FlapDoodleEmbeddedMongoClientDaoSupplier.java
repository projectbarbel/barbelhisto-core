package com.projectbarbel.histo.persistence.impl.mongo;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.function.Supplier;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.InstantCodec;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.projectbarbel.histo.persistence.impl.DocumentDao;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class FlapDoodleEmbeddedMongoClientDaoSupplier implements Supplier<DocumentDao<DefaultMongoValueObject, ObjectId>>{

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static MongodExecutable _mongodExe;
    private static MongodProcess _mongod;
    private static MongoClient _mongo;
        
    private final static CodecRegistry pojoCodecRegistry = fromRegistries(
            CodecRegistries.fromCodecs(new InstantCodec(), new BitemporalCodec()),
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    
    private final static MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry)
            .applyConnectionString(new ConnectionString("mongodb://localhost:12345")).build();

    public static final FlapDoodleEmbeddedMongoClientDaoSupplier MONGOCLIENT = init();
    
    private static FlapDoodleEmbeddedMongoClientDaoSupplier init() {
        try {
            _mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                    .net(new Net("localhost", 12345, Network.localhostIsIPv6())).build());
            _mongod = _mongodExe.start();
            _mongo = MongoClients.create(settings);
        } catch (Exception e) {
            System.out.println(e);
            _mongod.stop();
            _mongodExe.stop();
            throw new RuntimeException("Could not create mongo client", e);
        }
        return new FlapDoodleEmbeddedMongoClientDaoSupplier();
    }

    public MongoClient getMongo() {
        return _mongo;
    }
    
    public MongoDocumentDaoImpl get() {
        return new MongoDocumentDaoImpl(_mongo, "test", "testCol");
    }

}
