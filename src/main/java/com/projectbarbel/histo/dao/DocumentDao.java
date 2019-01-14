package com.projectbarbel.histo.dao;

import com.projectbarbel.histo.model.AbstractValueObject;

public interface DocumentDao<T extends AbstractValueObject> { 

    T createDocument(T document);

    T updateDocument(T document);

    void deleteDocument(String uniqueId);

    void readDocument(String uniqueId);
    
}
