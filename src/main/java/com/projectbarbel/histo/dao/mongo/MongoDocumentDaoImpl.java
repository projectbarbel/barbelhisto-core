package com.projectbarbel.histo.dao.mongo;

import com.mongodb.client.MongoClient;
import com.projectbarbel.histo.dao.DocumentDao;
import com.projectbarbel.histo.model.AbstractValueObject;

public class MongoDocumentDaoImpl<T extends AbstractValueObject> implements DocumentDao<T> {

	private MongoClient client;
	
	public MongoDocumentDaoImpl(MongoClient client) {
		this.client = client;
	}

	@Override
	public T createDocument(T document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T updateDocument(T document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteDocument(String uniqueId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readDocument(String uniqueId) {
		// TODO Auto-generated method stub
		
	}
	
}
