package com.projectbarbel.histo.persistence;

import java.util.ServiceLoader;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.persistence.spi.JournalStoreProvider;

public class JournalStoreService<T extends Bitemporal<?>> implements JournalStore<T>{
    
    private static JournalStoreService<?> service;
    private ServiceLoader<JournalStoreProvider> loader;
    private JournalStoreProvider provider;
 
    private JournalStoreService() {
        loader = ServiceLoader.load(JournalStoreProvider.class);
        provider = loader.iterator().next();
    }
 
    @SuppressWarnings("unchecked")
    public static synchronized <T extends Bitemporal<?>> JournalStoreService<T> getInstance() {
        if (service == null) {
            service = new JournalStoreService<T>();
        }
        return (JournalStoreService<T>)service;
    }
 
 
    @SuppressWarnings("unchecked")
    @Override
    public DocumentJournal<T> loadJournal(String documentId) {
        return (DocumentJournal<T>)provider.loadJournal(documentId);
    }

    @Override
    public long persistJournal(DocumentJournal<T> journal) {
        return 0;
    }
}
