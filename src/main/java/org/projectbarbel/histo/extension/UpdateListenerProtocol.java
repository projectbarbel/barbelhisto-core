package org.projectbarbel.histo.extension;

import java.util.List;

import org.projectbarbel.histo.model.Bitemporal;

public interface UpdateListenerProtocol<R,T> {

    R createResource();

    long delete(String versionId);

    long deleteJournal(Object documentId);

    void insertDocuments(List<T> documentsToInsert);

    Iterable<T> queryJournal(Object documentId);
    
    Bitemporal fromStoredDocumentToPersistedType(T storedDocument);    

    T fromJsonToStoredDocument(String json);

    String fromStroredDocumentToJson(T storedDocument);

}
