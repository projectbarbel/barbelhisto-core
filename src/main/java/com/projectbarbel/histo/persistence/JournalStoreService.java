package com.projectbarbel.histo.persistence;

import java.util.ServiceLoader;

import com.google.gson.Gson;
import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.persistence.api.JournalStoreProvider;

public class JournalStoreService<T extends Bitemporal<?>> {
    
    private static JournalStoreService<?> service = new JournalStoreService<Bitemporal<?>>();
    private ServiceLoader<JournalStoreProvider> loader;
    private JournalStoreProvider provider;
    private Gson gson = new Gson();
 
    private JournalStoreService() {
        loader = ServiceLoader.load(JournalStoreProvider.class);
        provider = loader.iterator().next();
    }
 
    @SuppressWarnings("unchecked")
    public static <T extends Bitemporal<?>> JournalStoreService<T> getInstance() {
        return (JournalStoreService<T>)service;
    }

    @SuppressWarnings("unchecked")
    public DocumentJournal<T> loadJournal(String documentId) {
        String persistenceJournal = gson.toJson(provider.loadJournal(documentId));
        DocumentJournal<?> journal = gson.fromJson(persistenceJournal, DocumentJournal.class);
        return (DocumentJournal<T>)journal;
    }

    public long persistJournal(DocumentJournal<T> journal) {
        return 0;
    }

}
