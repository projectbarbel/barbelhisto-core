package com.projectbarbel.histo.persistence.api;

import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;

public interface PersistenceService<T extends Bitemporal<?>> {

    DocumentJournal<T> loadJournal(String documentId);
    long updateJournal(DocumentJournal<T> journal);
    
}
