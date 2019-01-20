package com.projectbarbel.histo.dao.mongo;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.projectbarbel.histo.dao.DocumentDao;

public class MongoDocumentDaoImpl implements DocumentDao<DefaultMongoValueObject, ObjectId> {

	private final MongoClient client;
	private final String databaseName;
	private final String collectionName;
	
	public MongoDocumentDaoImpl(MongoClient client, String databaseName, String collectionName) {
		this.client = Objects.requireNonNull(client, "client must not be null in Mongo DAO");
		this.databaseName = Objects.requireNonNull(databaseName, "databaseName must not be null in Mongo DAO");
		this.collectionName = Objects.requireNonNull(collectionName, "collectionName must not be null in Mongo DAO");
	}

	@Override
	public Optional<ObjectId> createDocument(DefaultMongoValueObject document) {
		Validate.notNull(document, "document must not be null when creating a document");
		MongoCollection<DefaultMongoValueObject> col = client.getDatabase(databaseName).getCollection(collectionName, DefaultMongoValueObject.class);
		col.insertOne(document);
		return Optional.of(document.getObjectId());
	}

	@Override
	public Optional<ObjectId> updateDocument(ObjectId objectId, DefaultMongoValueObject document) {
	    Validate.notNull(objectId, "objectId must not be null when updating a document");
	    Validate.notNull(document, "document must not be null when updating a document");
		MongoCollection<DefaultMongoValueObject> col = client.getDatabase(databaseName).getCollection(collectionName, DefaultMongoValueObject.class);
		DefaultMongoValueObject newDoc = col.findOneAndReplace(Filters.eq("objectId", objectId), document);
		return Optional.of(newDoc.getObjectId());
	}

	@Override
	public long deleteDocument(ObjectId objectId) {
	    Validate.notNull(objectId, "objectId must not be null when deleting a document");
		MongoCollection<Document> col = client.getDatabase(databaseName).getCollection(collectionName);
		return col.deleteOne(Filters.eq("objectId", objectId)).getDeletedCount();
	}

	@Override
	public Optional<DefaultMongoValueObject> readDocument(ObjectId objectId) {
	    Validate.notNull(objectId, "objectId must not be null when deleting a document");
		MongoCollection<DefaultMongoValueObject> col = client.getDatabase(databaseName).getCollection(collectionName, DefaultMongoValueObject.class);
		FindIterable<DefaultMongoValueObject> docs = col.find(Filters.eq("objectId", objectId));
		if (docs.iterator().hasNext()) {
			return Optional.of(docs.first());
		} else {
			return Optional.empty();
		}
	}

}
