package com.projectbarbel.histo.persistence.impl.mongo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.persistence.api.DocumentDao;

public class MongoDocumentDaoImpl implements DocumentDao<DefaultMongoValueObject, ObjectId> {

	private final MongoClient client;
	private final String databaseName;
	private final String collectionName;
    private MongoCollection<DefaultMongoValueObject> col;
	
	public MongoCollection<DefaultMongoValueObject> getCol() {
        return col;
    }

    public MongoDocumentDaoImpl(MongoClient client, String databaseName, String collectionName) {
		this.client = Objects.requireNonNull(client, "client must not be null in Mongo DAO");
		this.databaseName = Objects.requireNonNull(databaseName, "databaseName must not be null in Mongo DAO");
		this.collectionName = Objects.requireNonNull(collectionName, "collectionName must not be null in Mongo DAO");
        col = client.getDatabase(databaseName).getCollection(collectionName, DefaultMongoValueObject.class);
	}

	@Override
	public Optional<ObjectId> createDocument(DefaultMongoValueObject document) {
		Validate.notNull(document, "document must not be null when creating a document");
		col.insertOne(document);
		return Optional.of(document.getVersionId());
	}

	@Override
	public Optional<ObjectId> updateDocument(ObjectId objectId, DefaultMongoValueObject document) {
	    Validate.notNull(objectId, "objectId must not be null when updating a document");
	    Validate.notNull(document, "document must not be null when updating a document");
		DefaultMongoValueObject newDoc = col.findOneAndReplace(Filters.eq("objectId", objectId), document);
		return Optional.of(newDoc.getVersionId());
	}

	@Override
	public long deleteDocument(ObjectId objectId) {
	    Validate.notNull(objectId, "objectId must not be null when deleting a document");
		return col.deleteOne(Filters.eq("objectId", objectId)).getDeletedCount();
	}

	@Override
	public Optional<DefaultMongoValueObject> readDocument(ObjectId objectId) {
	    Validate.notNull(objectId, "objectId must not be null when reading a document");
		FindIterable<DefaultMongoValueObject> docs = col.find(Filters.eq("objectId", objectId));
		if (docs.iterator().hasNext()) {
			return Optional.of(docs.first());
		} else {
			return Optional.empty();
		}
	}

    @Override
    public void reset() {
        client.getDatabase(databaseName).drop();
        client.getDatabase(databaseName).createCollection(collectionName, new CreateCollectionOptions().capped(false));
        col = client.getDatabase(databaseName).getCollection(collectionName, DefaultMongoValueObject.class);
    }

    @Override
    public List<DefaultDocument> readJournal() {
        // TODO Auto-generated method stub
        return null;
    }

}
