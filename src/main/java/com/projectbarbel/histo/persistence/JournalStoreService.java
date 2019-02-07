package com.projectbarbel.histo.persistence;

import java.util.ServiceLoader;

import com.google.gson.Gson;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.persistence.api.JournalStoreProvider;

public class JournalStoreService {
    
    private static JournalStoreService service = new JournalStoreService();
    @SuppressWarnings("rawtypes")
    private ServiceLoader<JournalStoreProvider> loader;
    @SuppressWarnings("rawtypes")
    private JournalStoreProvider provider;
    private Gson gson = new Gson();
 
    private JournalStoreService() {
        loader = ServiceLoader.load(JournalStoreProvider.class);
        provider = loader.iterator().next();
    }
 
    public static JournalStoreService getInstance() {
        return service;
    }

    public DocumentJournal loadJournal(String documentId) {
        String persistenceJournal = gson.toJson(provider.loadJournal(documentId));
        DocumentJournal journal = gson.fromJson(persistenceJournal, DocumentJournal.class);
        return journal;
    }

    public long persistJournal(DocumentJournal journal) {
        return 0;
    }

}
