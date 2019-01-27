package com.projectbarbel.histo.persistence.api;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.DocumentJournal;

public interface PersistenceService<T extends Bitemporal<O>, O> {

    DocumentJournal<T, O> loadJournal(String documentId);
    long updateJournal(DocumentJournal<T, O> journal);
    
}
