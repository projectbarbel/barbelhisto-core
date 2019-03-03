package org.projectbarbel.histo.extension;

import org.projectbarbel.histo.event.EventType.BarbelInitializedEvent;
import org.projectbarbel.histo.event.EventType.InitializeJournalEvent;
import org.projectbarbel.histo.event.EventType.RetrieveDataEvent;
import org.projectbarbel.histo.model.Bitemporal;

import com.google.common.eventbus.Subscribe;

public interface LazyLoadingListenerProtocol<R, T> {
    
    Iterable<T> queryJournal(Object id);

    R createResource();

    Iterable<T> queryAll();
    
    Bitemporal toPersistedType(T document);
    
    @Subscribe
    void handleInitialization(BarbelInitializedEvent event);
    
    @Subscribe
    void handleRetrieveData(RetrieveDataEvent event);
    
    @Subscribe
    void handleInitializeJournal(InitializeJournalEvent event);
    
}
