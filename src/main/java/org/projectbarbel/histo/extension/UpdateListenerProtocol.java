package org.projectbarbel.histo.extension;

import java.util.List;

import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.OnLoadOperationEvent;
import org.projectbarbel.histo.event.EventType.UnLoadOperationEvent;
import org.projectbarbel.histo.event.EventType.UpdateFinishedEvent;
import org.projectbarbel.histo.model.Bitemporal;

import com.google.common.eventbus.Subscribe;

public interface UpdateListenerProtocol<R, T> {

    @Subscribe
    void handleUpdate(UpdateFinishedEvent event);
    
    @Subscribe
    void handleUnLoadOperation(UnLoadOperationEvent event);
    
    @Subscribe
    void handleLoadOperation(OnLoadOperationEvent event);
    
    @Subscribe
    void handleInitialization(BarbelInitializedEvent event);
    
    R getExternalDataResource();

    long delete(String versionId);

    long deleteJournal(Object documentId);

    void insertDocuments(List<T> documentsToInsert);

    Iterable<T> queryJournal(Object documentId);

    Bitemporal fromStoredDocumentToPersistenceObject(T storedDocument);

}
