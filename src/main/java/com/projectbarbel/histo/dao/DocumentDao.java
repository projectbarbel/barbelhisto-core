package com.projectbarbel.histo.dao;

import com.projectbarbel.histo.model.Bitemporal;

/**
 * 
 * @author niklasschlimm
 *
 * @param <T> the bi-temporal document type to store with a concrete DAO implementation
 * @param <O> the type of object id that is used by the concrete data store
 */
public interface DocumentDao<T extends Bitemporal, O> { 

    O createDocument(T document);

    O updateDocument(T document);

    void deleteDocument(O objectId);

    void readDocument(O objectId);
    
}
